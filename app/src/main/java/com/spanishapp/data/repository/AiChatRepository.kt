package com.spanishapp.data.repository

import com.spanishapp.BuildConfig
import com.spanishapp.data.db.dao.ChatMessageDao
import com.spanishapp.data.db.entity.ChatMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiChatRepository @Inject constructor(
    private val chatMessageDao: ChatMessageDao,
    private val okHttpClient: OkHttpClient,
    private val authRepository: AuthRepository,
    private val userProgressDao: com.spanishapp.data.db.dao.UserProgressDao,
) {

    companion object {
        // gemini-flash-latest — Google-managed alias на актуальную бесплатную Flash-модель.
        // Старое имя "gemini-1.5-flash" убрано из v1beta; "gemini-2.0-flash" имеет квоту 0
        // на текущем ключе. Этот alias работает без сюрпризов.
        // v1.18.9: переход на gemini-2.5-flash-lite — 2-3× быстрее flash-latest,
        // оптимизирована для коротких ответов (идеально для A1/A2 tutor-чата).
        // Worker whitelist обновлён (deploy 2026-05-19).
        private const val MODEL = "gemini-2.5-flash-lite"

        /**
         * If [BuildConfig.AI_PROXY_URL] is set, use it — proxy hides the API
         * key from APK. В release-сборке fallback на direct Gemini ЗАПРЕЩЁН:
         * если proxy не сконфигурирован, лучше показать ошибку чем запечь
         * ключ в production APK (откуда его легко достать через jadx).
         *
         * В debug-сборке direct call разрешён для удобства разработки.
         */
        private fun apiUrl(): String {
            val proxy = BuildConfig.AI_PROXY_URL.trim().trimEnd('/')
            if (proxy.isNotEmpty()) {
                return "$proxy/v1beta/models/$MODEL:generateContent"
            }
            require(BuildConfig.DEBUG) {
                "AI_PROXY_URL must be configured for release builds. " +
                "Direct Gemini calls expose the API key in the APK."
            }
            return "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent" +
                "?key=${BuildConfig.GEMINI_API_KEY}"
        }

        /** Server-Sent-Events streaming endpoint. */
        private fun streamUrl(): String {
            val proxy = BuildConfig.AI_PROXY_URL.trim().trimEnd('/')
            if (proxy.isNotEmpty()) {
                return "$proxy/v1beta/models/$MODEL:streamGenerateContent"
            }
            require(BuildConfig.DEBUG) {
                "AI_PROXY_URL must be configured for release builds."
            }
            return "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:streamGenerateContent" +
                "?key=${BuildConfig.GEMINI_API_KEY}&alt=sse"
        }

        /**
         * Adds X-App-Secret (фильтр случайных гостей) и Authorization: Bearer
         * с Firebase ID Token (доказательство что запрос от настоящего юзера
         * приложения, а не из извлечённого APK секрета).
         *
         * Двухслойная защита:
         *  • X-App-Secret  — общий секрет приложения, легко достать из APK
         *  • Firebase ID Token — JWT уникальный для каждого юзера, привязан к
         *    Firebase UID, имеет 1-час exp, нельзя массово получить без
         *    регистрации в Firebase Auth.
         *
         * Если Firebase Auth ещё не инициализирован (cold start) — отправляем
         * только X-App-Secret, Worker может пропустить (если FIREBASE_PROJECT
         * env не задан) или отвергнуть с 401.
         */
        private suspend fun Request.Builder.withProxyAuth(): Request.Builder {
            val proxy = BuildConfig.AI_PROXY_URL.trim()
            val secret = BuildConfig.AI_PROXY_SECRET.trim()
            if (proxy.isEmpty()) return this  // direct Gemini в debug — без auth
            if (secret.isNotEmpty()) {
                header("X-App-Secret", secret)
            }
            // Firebase ID Token (если юзер залогинен — anonymous или Google).
            // JWT уникальный для каждого юзера, нельзя массово получить без
            // регистрации в Firebase Auth. Доказывает что запрос от настоящего
            // юзера приложения, а не из извлечённого секрета APK.
            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (user != null) {
                try {
                    val token = awaitTask(user.getIdToken(false))
                    token.token?.let { header("Authorization", "Bearer $it") }
                } catch (_: Exception) {
                    // Тихий failover — без токена Worker может отказать,
                    // но это лучше чем краш UI на ровном месте.
                }
            }
            return this
        }

        /** Самописный await для Task<T> (без зависимости от kotlinx-coroutines-play-services). */
        private suspend fun <T> awaitTask(task: com.google.android.gms.tasks.Task<T>): T =
            kotlinx.coroutines.suspendCancellableCoroutine { cont ->
                task.addOnSuccessListener { result ->
                    @Suppress("UNCHECKED_CAST")
                    cont.resumeWith(Result.success(result as T))
                }.addOnFailureListener { cont.resumeWith(Result.failure(it)) }
            }

        /**
         * Превращает технический HTTP-error от Gemini в понятное юзеру
         * сообщение. Никаких raw JSON, кодов, stack traces — короткая
         * человеческая фраза которую можно показать в чате.
         */
        internal fun humanizeError(
            code: Int,
            body: String,
            quotaResetUtcMs: Long? = null,
        ): String {
            val bodyLower = body.lowercase()
            return when {
                // Ключ зарепортен/заблокирован Google
                bodyLower.contains("reported as leaked") ||
                bodyLower.contains("api key not valid") ->
                    "ИИ-репетитор временно недоступен — обновляем ключ доступа. " +
                    "Попробуй через несколько минут."

                // Превышение квоты Gemini — все fallback модели исчерпаны
                code == 429 || bodyLower.contains("quota") ||
                bodyLower.contains("resource_exhausted") ->
                    formatQuotaError(quotaResetUtcMs)

                // Permission / authorization issues
                code == 401 || code == 403 ->
                    "ИИ-репетитор временно недоступен. Мы уже чиним 🔧"

                // Server-side errors
                code in 500..599 ->
                    "Сервер ИИ не отвечает. Проверь интернет или попробуй позже."

                // Network / unknown
                else ->
                    "Не удалось получить ответ от ИИ. Попробуй ещё раз через минуту."
            }
        }

        /**
         * v1.18.34: форматирует «сбросится через Xч Yм» от UTC timestamp.
         * Локальное время юзера вычисляется автоматически (System.currentTimeMillis()
         * сравнивается с UTC reset moment — разница не зависит от часового пояса).
         */
        private fun formatQuotaError(quotaResetUtcMs: Long?): String {
            if (quotaResetUtcMs == null || quotaResetUtcMs <= System.currentTimeMillis()) {
                return "Сегодня лимит ИИ-чата исчерпан. Попробуй чуть позже 🙏"
            }
            val diffMs = quotaResetUtcMs - System.currentTimeMillis()
            val totalMinutes = (diffMs / 60_000).toInt()
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            val relative = when {
                hours >= 1 && minutes > 0 -> "${hours}ч ${minutes}м"
                hours >= 1 -> "${hours}ч"
                else -> "${minutes}м"
            }
            return "Сегодня лимит ИИ-чата исчерпан. Сбросится через $relative 🙏"
        }

        // v1.18.4: prompt сокращён ~60% для уменьшения latency first-token.
        // Раньше 50+ строк с длинными примерами — каждый запрос «съедал» эти
        // токены. Gemini Flash 8B всё равно понимает короткие инструкции.
        private val SYSTEM_PROMPT = """
            Ты — дружелюбный репетитор испанского для русскоязычных новичков (A1/A2).

            ПРАВИЛА:
            • Отвечай по-РУССКИ, испанские слова пиши как **palabra** [перевод].
            • Коротко — 3-5 строк, без длинных лекций.
            • Один вопрос за раз, без «¡Hola! ¡Bienvenido!» в каждом ответе.
            • Эмодзи 1-2 на ответ для тёплого тона.
            • Ошибки помечай одной строкой: ✏️ Правильно: **correcto** [перевод]

            СЛЕНГ:
            • Понимай русский разговорный («норм», «крч», «жиза», «база», «кринж»,
              «вайб», «топчик» и др.) без зацикливания на стиле.
            • На просьбу сленга давай настоящий разговорный: **tío/guay/mola/vale/
              currar/pasta** (Испания), **órale/chido/wey** (Мексика). Помечай
              регион если локальное.

            ФОРМАТ ИСПРАВЛЕНИЙ (только при ошибке, в конце):
            CORRECTIONS_JSON:[{"original":"anos","corrected":"años","explanation":"тильда"}]

            ЗАПОМИНАНИЕ УЧЕНИКА (важно!):
            • Если ученик НОВЫЙ (нет данных в «О ТВОЁМ УЧЕНИКЕ») — мягко
              познакомься в первых 2-3 сообщениях: спроси имя, цель изучения,
              сколько уже знает. Не как анкета — естественно по ходу разговора.
            • Если ученик упомянул что-то о себе (имя, возраст, город, работу,
              хобби, цель изучения, любимые темы, что давно не учит и т.п.) —
              в КОНЦЕ ответа добавь маркер JSON (один или несколько полей):

            PROFILE_UPDATE_JSON:{"name":"Сергей","gender":"male","interests":["футбол","кино"],"goal":"путешествие в Барселону","level":"A2","notes":"стесняется говорить"}

            ВАЖНО про gender: если из чата ПОНЯТНО мужчина или женщина
            (форма глаголов «готов»/«готова», «сказал»/«сказала», имя)
            — обязательно отметь "gender":"male" или "female". Это нужно
            чтобы ты в дальнейшем обращался в правильном роде.

            • Поля опциональны — заполняй только те что узнал. notes — свободный
              текст об особенностях ученика (его стиль общения, любимые темы,
              чего избегает) для следующих сессий.
            • Данные из «О ТВОЁМ УЧЕНИКЕ» могут быть устаревшие/неверные.
              Если ученик сказал что его зовут иначе — доверяй чату, не Settings.
            • Не показывай юзеру что ты записываешь — это незаметно.
        """.trimIndent()
    }

    // ── Get chat history as Flow ──────────────────────────────
    fun getMessages(sessionId: String): Flow<List<ChatMessageEntity>> =
        chatMessageDao.getSession(sessionId)

    // ── Send message and get AI response ─────────────────────
    suspend fun sendMessage(
        userText: String,
        sessionId: String = "default"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Pre-flight: if neither the proxy nor the direct key is configured,
            // Gemini returns HTTP 400 with a vague body that the UI maps to a
            // generic error. Fail fast with a tagged exception the UI can
            // localize as `chat_error_invalid_key`.
            if (BuildConfig.AI_PROXY_URL.isBlank() && BuildConfig.GEMINI_API_KEY.isBlank()) {
                return@withContext Result.failure(Exception("401: missing API key"))
            }
            // Save user message
            chatMessageDao.insert(
                ChatMessageEntity(role = "user", content = userText, sessionId = sessionId)
            )

            // Build conversation history
            val history = chatMessageDao.getSessionOnce(sessionId)
                .takeLast(10)  // v1.18.4: 20→10 для ускорения first-token

            // v1.18.10: persistent user profile в каждом запросе — ИИ помнит
            // имя/уровень/цель даже если они вне 10-message окна.
            val profileBlock = buildUserProfileBlock()
            val body = buildGeminiRequest(history, userProfileBlock = profileBlock)

            val request = Request.Builder()
                .url(apiUrl())
                .post(body)
                .header("Content-Type", "application/json")
                .withProxyAuth()
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                val quotaReset = response.header("X-Quota-Reset-Utc")?.toLongOrNull()
                return@withContext Result.failure(Exception(humanizeError(response.code, errBody, quotaReset)))
            }

            // v1.17.5: safe-navigation. Раньше было 7×!! — крашилось NPE/IOOBE
            // если Gemini менял структуру ответа или возвращал пустой candidates.
            val bodyStr = response.body?.string() ?: error("Empty response body")
            val json = Json.parseToJsonElement(bodyStr).jsonObject
            val assistantText = json["candidates"]
                ?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("content")
                ?.jsonObject?.get("parts")
                ?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("text")
                ?.jsonPrimitive?.content
                ?: error("Gemini response missing candidates[0].content.parts[0].text")

            val correctionJson = extractCorrections(assistantText)
            // v1.18.11: парсим и применяем PROFILE_UPDATE_JSON ДО strip'а
            extractAndApplyProfileUpdate(assistantText)
            val cleanText = assistantText
                .substringBefore("CORRECTIONS_JSON:")
                .substringBefore("PROFILE_UPDATE_JSON:")
                .trim()

            chatMessageDao.insert(
                ChatMessageEntity(
                    role           = "assistant",
                    content        = cleanText,
                    sessionId      = sessionId,
                    correctionJson = correctionJson
                )
            )

            Result.success(cleanText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Streaming send ────────────────────────────────────────
    /**
     * Streams Gemini's response token-by-token. Each emit is the FULL
     * accumulated text so far (caller can render progressively without
     * tracking deltas itself).
     *
     * On completion, persists the final assistant message to Room.
     * On any error, throws — caller wraps with try/catch + UI error state.
     */
    fun streamMessage(
        userText: String,
        sessionId: String = "default",
        extraSystemPrompt: String = ""
    ): Flow<String> = flow {
        // Save user message first so it appears in UI immediately.
        chatMessageDao.insert(
            ChatMessageEntity(role = "user", content = userText, sessionId = sessionId)
        )

        val history = chatMessageDao.getSessionOnce(sessionId).takeLast(10)  // v1.18.4: 20→10 для ускорения first-token
        val profileBlock = buildUserProfileBlock()  // v1.18.10: persistent profile
        val body = buildGeminiRequest(history, extraSystemPrompt = extraSystemPrompt, userProfileBlock = profileBlock)

        val request = Request.Builder()
            .url(streamUrl())
            .post(body)
            .header("Content-Type", "application/json")
            .header("Accept", "text/event-stream")
            .withProxyAuth()
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val errBody = response.body?.string() ?: ""
            val quotaReset = response.header("X-Quota-Reset-Utc")?.toLongOrNull()
            throw Exception(humanizeError(response.code, errBody, quotaReset))
        }

        val source = response.body?.source()
            ?: throw Exception("Empty response body")

        val accumulated = StringBuilder()
        val parser = Json { ignoreUnknownKeys = true }

        // SSE format: each event is "data: {json}\n\n". Read line-by-line.
        while (!source.exhausted()) {
            val line = source.readUtf8Line() ?: break
            if (!line.startsWith("data:")) continue
            val payload = line.removePrefix("data:").trim()
            if (payload.isEmpty() || payload == "[DONE]") continue

            // Each chunk: { "candidates": [{ "content": { "parts": [{"text":"..."}]}}] }
            runCatching {
                val obj = parser.parseToJsonElement(payload).jsonObject
                val text = obj["candidates"]
                    ?.jsonArray?.firstOrNull()
                    ?.jsonObject?.get("content")
                    ?.jsonObject?.get("parts")
                    ?.jsonArray?.firstOrNull()
                    ?.jsonObject?.get("text")
                    ?.jsonPrimitive?.content
                    ?: ""
                if (text.isNotEmpty()) {
                    accumulated.append(text)
                    emit(accumulated.toString())
                }
            }
        }

        // Persist the final message (strip CORRECTIONS_JSON + PROFILE_UPDATE_JSON tail).
        val full = accumulated.toString()
        val correctionJson = extractCorrections(full)
        // v1.18.11: применяем профиль обновления ДО strip
        extractAndApplyProfileUpdate(full)
        val cleanText = full
            .substringBefore("CORRECTIONS_JSON:")
            .substringBefore("PROFILE_UPDATE_JSON:")
            .trim()
        if (cleanText.isNotEmpty()) {
            chatMessageDao.insert(
                ChatMessageEntity(
                    role           = "assistant",
                    content        = cleanText,
                    sessionId      = sessionId,
                    correctionJson = correctionJson
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    // ── Grammar check only ────────────────────────────────────
    suspend fun checkGrammar(spanishText: String): Result<GrammarCheckResult> =
        withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    Analiza el siguiente texto en español escrito por un estudiante ruso.
                    Responde ÚNICAMENTE en JSON con este formato exacto:
                    {
                      "isCorrect": true/false,
                      "correctedText": "texto corregido",
                      "errors": [
                        {"original": "error", "correction": "corrección", "explanationRu": "объяснение на русском"}
                      ],
                      "overallFeedbackRu": "общий отзыв на русском"
                    }

                    Texto a analizar: "$spanishText"
                """.trimIndent()

                val fakeMsg = ChatMessageEntity(role = "user", content = prompt)
                val body = buildGeminiRequest(listOf(fakeMsg), withSystem = false)

                val request = Request.Builder()
                    .url(apiUrl())
                    .post(body)
                    .header("Content-Type", "application/json")
                    .withProxyAuth()
                    .build()

                val response = okHttpClient.newCall(request).execute()
                // v1.17.5: safe-navigation. См. комментарий в send().
                val bodyStr = response.body?.string() ?: error("Empty response body")
                val raw = Json.parseToJsonElement(bodyStr).jsonObject["candidates"]
                    ?.jsonArray?.firstOrNull()
                    ?.jsonObject?.get("content")
                    ?.jsonObject?.get("parts")
                    ?.jsonArray?.firstOrNull()
                    ?.jsonObject?.get("text")
                    ?.jsonPrimitive?.content
                    ?: error("Gemini response missing candidates[0].content.parts[0].text")

                // Strip possible markdown code block
                val jsonText = raw
                    .removePrefix("```json").removePrefix("```")
                    .removeSuffix("```").trim()

                val result = Json { ignoreUnknownKeys = true }
                    .decodeFromString<GrammarCheckResult>(jsonText)
                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // ── Clear session ─────────────────────────────────────────
    suspend fun clearSession(sessionId: String) {
        chatMessageDao.clearSession(sessionId)
    }

    // ── User profile (v1.18.10) ───────────────────────────────
    /**
     * Собирает блок «О ученике» который добавляется в system prompt при
     * КАЖДОМ запросе. Так ИИ знает имя, уровень, возраст, цель — даже
     * если они упомянуты только в Settings/Onboarding, а не в текущем
     * 10-сообщений-окне history.
     *
     * Пустой блок если юзер ничего не заполнил — ИИ работает как раньше.
     */
    private suspend fun buildUserProfileBlock(): String {
        val name = authRepository.userName.first()?.takeIf { it.isNotBlank() }
        val age = authRepository.userAge.first()
        val level = authRepository.userLevel.first()?.takeIf { it.isNotBlank() }
        val reason = authRepository.userReason.first()?.takeIf { it.isNotBlank() }
        // v1.18.11: AI-learned поля
        val interests = authRepository.userInterests.first()?.takeIf { it.isNotBlank() }
        val goal = authRepository.userGoal.first()?.takeIf { it.isNotBlank() }
        val notes = authRepository.userNotes.first()?.takeIf { it.isNotBlank() }
        val gender = authRepository.userGender.first()?.takeIf { it.isNotBlank() }
        // v1.18.20: характер репетитора — задаёт тон ответов
        val personalityId = authRepository.tutorPersonality.first()
        val personality = com.spanishapp.domain.voice.TutorPersonality.byId(personalityId)
        val progress = userProgressDao.getProgressOnce()
        val wordsLearned = progress?.wordsLearned ?: 0
        val streak = progress?.currentStreak ?: 0

        val parts = buildList {
            if (name != null) add("имя — **$name**")
            if (gender != null) {
                val g = when (gender.lowercase()) {
                    "male", "m", "мужской", "мужчина" -> "мужчина (обращаться в МУЖСКОМ роде)"
                    "female", "f", "женский", "женщина" -> "женщина (обращаться в ЖЕНСКОМ роде)"
                    else -> gender
                }
                add(g)
            }
            if (age != null) add("возраст — $age лет")
            if (level != null) add("уровень — $level")
            if (goal != null) add("цель — $goal")
            else if (reason != null) {
                val reasonText = when (reason.lowercase()) {
                    "travel" -> "учит для путешествий"
                    "work" -> "учит для работы"
                    "family" -> "учит ради семьи/любви"
                    "hobby" -> "учит как хобби"
                    "study" -> "учит для учёбы/экзаменов"
                    else -> "цель: $reason"
                }
                add(reasonText)
            }
            if (interests != null) add("интересы: $interests")
            if (wordsLearned > 0) add("выучил $wordsLearned слов")
            if (streak > 0) add("серия занятий — $streak дней")
        }
        if (parts.isEmpty() && notes == null) return ""

        return buildString {
            // v1.18.20: блок персонажа всегда добавляется
            append("\n\n")
            append(personality.toneInstructions)
            append("\n")
            if (parts.isNotEmpty() || notes != null) {
                append("\nО ТВОЁМ УЧЕНИКЕ (помни это в каждом ответе):\n")
                if (parts.isNotEmpty()) {
                    append(parts.joinToString(", "))
                    append(".\n")
                }
                if (notes != null) {
                    append("Заметки: $notes\n")
                }
                append("• Обращайся по имени когда уместно (не каждое сообщение).\n")
                append("• Адаптируй сложность под уровень.\n")
                append("• Используй интересы ученика в примерах.\n")
                append("• Иногда хвали за прогресс.\n")
            }
        }
    }

    /**
     * v1.18.11: парсит PROFILE_UPDATE_JSON маркер из ответа ИИ и применяет
     * изменения к AuthRepository. ИИ сам решает когда обновлять (когда
     * узнал имя/предпочтения из чата). Юзеру это незаметно — маркер
     * уже отрезан от display через substringBefore() в ViewModel.
     */
    private suspend fun extractAndApplyProfileUpdate(rawResponse: String) {
        val marker = "PROFILE_UPDATE_JSON:"
        val idx = rawResponse.indexOf(marker)
        if (idx < 0) return
        val jsonStart = idx + marker.length
        // Берём JSON object — от первой { до соответствующей }
        val openBrace = rawResponse.indexOf('{', jsonStart)
        if (openBrace < 0) return
        var depth = 0
        var endIdx = -1
        for (i in openBrace until rawResponse.length) {
            when (rawResponse[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) {
                        endIdx = i
                        break
                    }
                }
            }
        }
        if (endIdx < 0) return
        val jsonStr = rawResponse.substring(openBrace, endIdx + 1)
        runCatching {
            val obj = Json.parseToJsonElement(jsonStr).jsonObject
            obj["name"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }?.let {
                authRepository.setUserName(it)
            }
            obj["level"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }?.let {
                authRepository.setUserLevel(it)
            }
            obj["gender"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }?.let {
                authRepository.setUserGender(it)
            }
            obj["goal"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }?.let {
                authRepository.setUserGoal(it)
            }
            obj["interests"]?.let { el ->
                val csv = when (el) {
                    is JsonArray -> el.mapNotNull { it.jsonPrimitive.contentOrNull }.joinToString(", ")
                    else -> el.jsonPrimitive.contentOrNull ?: ""
                }
                if (csv.isNotBlank()) authRepository.setUserInterests(csv)
            }
            obj["notes"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }?.let {
                // Merge с существующими notes (не перезаписываем, добавляем)
                val existing = authRepository.userNotes.first().orEmpty()
                val merged = if (existing.isBlank()) it
                             else "$existing | $it"
                authRepository.setUserNotes(merged.take(500))  // safety cap
            }
        }.onFailure { e ->
            android.util.Log.w("AiChatRepo", "Failed to parse PROFILE_UPDATE_JSON: ${e.message}")
        }
    }

    // ── Build Gemini REST body ────────────────────────────────
    private fun buildGeminiRequest(
        messages: List<ChatMessageEntity>,
        withSystem: Boolean = true,
        extraSystemPrompt: String = "",
        userProfileBlock: String = "",
    ): RequestBody {
        val json = buildJsonObject {
            // System instruction (Gemini 1.5 supports it). Optional theme-specific
            // prompt is appended so e.g. "Travel" mode adds airport scenarios.
            if (withSystem) {
                val fullPrompt = buildString {
                    append(SYSTEM_PROMPT)
                    if (userProfileBlock.isNotBlank()) {
                        append("\n")
                        append(userProfileBlock)
                    }
                    if (extraSystemPrompt.isNotBlank()) {
                        append("\n\n")
                        append(extraSystemPrompt)
                    }
                }
                // Gemini's REST schema requires `parts` to be an ARRAY of objects,
                // not a single object. Flash is lenient and accepts both, but Pro
                // and future stricter validators silently drop scenario prompts
                // (waiter / hotel / doctor) when sent as object form.
                putJsonObject("system_instruction") {
                    putJsonArray("parts") {
                        addJsonObject { put("text", fullPrompt) }
                    }
                }
            }
            putJsonArray("contents") {
                messages.forEach { msg ->
                    addJsonObject {
                        // Gemini uses "user" / "model" roles
                        put("role", if (msg.role == "assistant") "model" else "user")
                        putJsonArray("parts") {
                            addJsonObject { put("text", msg.content) }
                        }
                    }
                }
            }
            putJsonObject("generationConfig") {
                put("maxOutputTokens", 600)  // shorter answers
                put("temperature", 0.7)
                // Disable Gemini "thinking" — Flash spends 30-70% of latency on
                // hidden reasoning tokens that don't reach the user. Off = noticeably faster.
                putJsonObject("thinkingConfig") {
                    put("thinkingBudget", 0)
                }
            }
        }
        return json.toString().toRequestBody("application/json".toMediaType())
    }

    private fun extractCorrections(text: String): String {
        val marker = "CORRECTIONS_JSON:"
        val idx = text.indexOf(marker)
        return if (idx >= 0) text.substring(idx + marker.length).trim() else ""
    }
}

// ─────────────────────────────────────────────────────────────
@Serializable
data class GrammarCheckResult(
    val isCorrect: Boolean,
    val correctedText: String,
    val errors: List<GrammarError> = emptyList(),
    val overallFeedbackRu: String = ""
)

@Serializable
data class GrammarError(
    val original: String,
    val correction: String,
    val explanationRu: String
)
