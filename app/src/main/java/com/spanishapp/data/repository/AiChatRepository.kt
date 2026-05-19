package com.spanishapp.data.repository

import com.spanishapp.BuildConfig
import com.spanishapp.data.db.dao.ChatMessageDao
import com.spanishapp.data.db.entity.ChatMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
    private val okHttpClient: OkHttpClient
) {

    companion object {
        // gemini-flash-latest — Google-managed alias на актуальную бесплатную Flash-модель.
        // Старое имя "gemini-1.5-flash" убрано из v1beta; "gemini-2.0-flash" имеет квоту 0
        // на текущем ключе. Этот alias работает без сюрпризов.
        // v1.18.7: переход с gemini-flash-latest на 2.5-flash-lite —
        // 2-3× быстрее, та же квота, хорошее качество для A1/A2 tutor-чата.
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
         * Adds the X-App-Secret header when the proxy is in use AND a secret
         * was configured at build time. The Worker rejects mismatching
         * requests with 403 — protects the proxy from random callers who
         * stumble onto the public URL.
         */
        private fun Request.Builder.withProxySecret(): Request.Builder {
            val proxy = BuildConfig.AI_PROXY_URL.trim()
            val secret = BuildConfig.AI_PROXY_SECRET.trim()
            if (proxy.isNotEmpty() && secret.isNotEmpty()) {
                header("X-App-Secret", secret)
            }
            return this
        }

        /**
         * Превращает технический HTTP-error от Gemini в понятное юзеру
         * сообщение. Никаких raw JSON, кодов, stack traces — короткая
         * человеческая фраза которую можно показать в чате.
         */
        internal fun humanizeError(code: Int, body: String): String {
            val bodyLower = body.lowercase()
            return when {
                // Ключ зарепортен/заблокирован Google
                bodyLower.contains("reported as leaked") ||
                bodyLower.contains("api key not valid") ->
                    "ИИ-репетитор временно недоступен — обновляем ключ доступа. " +
                    "Попробуй через несколько минут."

                // Превышение квоты
                code == 429 || bodyLower.contains("quota") ->
                    "Слишком много запросов сейчас. Подожди минуту и попробуй снова 🙏"

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

            val body = buildGeminiRequest(history)

            val request = Request.Builder()
                .url(apiUrl())
                .post(body)
                .header("Content-Type", "application/json")
                .withProxySecret()
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                return@withContext Result.failure(Exception(humanizeError(response.code, errBody)))
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
            val cleanText = assistantText.substringBefore("CORRECTIONS_JSON:").trim()

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
        val body = buildGeminiRequest(history, extraSystemPrompt = extraSystemPrompt)

        val request = Request.Builder()
            .url(streamUrl())
            .post(body)
            .header("Content-Type", "application/json")
            .header("Accept", "text/event-stream")
            .withProxySecret()
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val errBody = response.body?.string() ?: ""
            throw Exception(humanizeError(response.code, errBody))
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

        // Persist the final message (strip CORRECTIONS_JSON tail).
        val full = accumulated.toString()
        val correctionJson = extractCorrections(full)
        val cleanText = full.substringBefore("CORRECTIONS_JSON:").trim()
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
                    .withProxySecret()
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

    // ── Build Gemini REST body ────────────────────────────────
    private fun buildGeminiRequest(
        messages: List<ChatMessageEntity>,
        withSystem: Boolean = true,
        extraSystemPrompt: String = ""
    ): RequestBody {
        val json = buildJsonObject {
            // System instruction (Gemini 1.5 supports it). Optional theme-specific
            // prompt is appended so e.g. "Travel" mode adds airport scenarios.
            if (withSystem) {
                val fullPrompt = if (extraSystemPrompt.isBlank()) SYSTEM_PROMPT
                                 else "$SYSTEM_PROMPT\n\n$extraSystemPrompt"
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
