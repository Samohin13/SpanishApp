package com.spanishapp.data.repository

import android.util.Log
import com.spanishapp.BuildConfig
import com.spanishapp.data.db.dao.ChatMessageDao
import com.spanishapp.data.db.entity.ChatMessageEntity
import com.spanishapp.domain.chat.ChatScenario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий ИИ-чата. Один источник правды для отправки сообщений в Gemini
 * через Cloudflare Worker и сохранения истории в Room.
 *
 * Контракт ответа от модели:
 *   Чистый текст ответа на испанском + русский перевод,
 *   опционально завершается блоком CORRECTIONS_JSON:[{...},{...}].
 *
 * Парсер вырезает CORRECTIONS_JSON из видимого текста и кладёт в
 * ChatMessageEntity.correctionJson отдельным полем.
 */
@Singleton
class AiChatRepository @Inject constructor(
    private val dao: ChatMessageDao,
    private val okHttp: OkHttpClient,
) {

    private companion object {
        // v1.25.58: gemini-2.5-flash — current stable. Worker имеет
        // fallback на 2.5-flash-lite и 2.0-flash при rate-limit.
        const val MODEL = "gemini-2.5-flash"
        const val TAG = "AiChatRepo"

        fun apiUrl(): String {
            val proxy = BuildConfig.AI_PROXY_URL.trim().trimEnd('/')
            if (proxy.isNotEmpty()) {
                return "$proxy/v1beta/models/$MODEL:generateContent"
            }
            require(BuildConfig.DEBUG) {
                "AI_PROXY_URL must be configured for release (AiChatRepository)."
            }
            return "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent" +
                "?key=${BuildConfig.GEMINI_API_KEY}"
        }
    }

    fun observeSession(sessionId: String) = dao.getSession(sessionId)

    suspend fun clearSession(sessionId: String) = dao.clearSession(sessionId)

    /**
     * Отправляет сообщение пользователя в Gemini, парсит ответ, сохраняет обе
     * записи в БД и возвращает результат.
     *
     * @param userText текст пользователя (может быть на испанском, русском, любом)
     * @param scenario выбранный сценарий — влияет на system prompt и session_id
     * @param level    декларируемый CEFR уровень (A1/A2/B1/B2) для адаптации
     */
    suspend fun sendMessage(
        userText: String,
        scenario: ChatScenario,
        level: String = "B1",
        tutorName: String = "Tutor",
    ): Result<ChatTurn> = withContext(Dispatchers.IO) {
        try {
            // 1. Сохранить сообщение пользователя
            dao.insert(
                ChatMessageEntity(
                    role = "user",
                    content = userText,
                    sessionId = scenario.id,
                )
            )

            // 2. Собрать историю для Gemini
            val history = dao.getSessionOnce(scenario.id)
            val systemPrompt = buildSystemPrompt(scenario, level, tutorName)

            // 3. Вызвать модель
            val rawReply = callGemini(systemPrompt, history)
                ?: return@withContext Result.failure(IllegalStateException("Пустой ответ от ИИ"))

            // 4. Распарсить CORRECTIONS_JSON
            val (visibleText, correctionsJson) = extractCorrections(rawReply)

            // 5. Сохранить ответ
            dao.insert(
                ChatMessageEntity(
                    role = "assistant",
                    content = visibleText,
                    sessionId = scenario.id,
                    correctionJson = correctionsJson,
                )
            )

            Result.success(ChatTurn(visibleText, correctionsJson))
        } catch (e: Exception) {
            Log.e(TAG, "sendMessage failed", e)
            Result.failure(e)
        }
    }

    private fun buildSystemPrompt(scenario: ChatScenario, level: String, tutorName: String): String = buildString {
        // v1.25.65: balance — диалог как друг, обучающие фишки ИНОГДА когда
        // реально полезно. Не на каждом сообщении. Юзер: "это важный фактор
        // поведения, не везде подряд".
        val name = scenario.characterName ?: tutorName
        appendLine("Ты — $name из Мадрида. Болтай с другом на испанском, как в WhatsApp.")
        appendLine("Уровень собеседника: CEFR $level (адаптируй лексику).")
        appendLine()
        appendLine("ОСНОВНОЕ — ЖИВОЙ ДИАЛОГ:")
        appendLine("• 1-2 коротких предложения, естественно, эмоционально.")
        appendLine("• Разговорные слова: vale, hombre, tío, claro, ¡qué guay!")
        appendLine("• Tutéo. Задавай встречные вопросы — это беседа, не монолог.")
        appendLine()
        appendLine("ЯЗЫК ОТВЕТА (важно!):")
        appendLine("• Обычная беседа на испанском → отвечай на испанском.")
        appendLine("• Юзер написал ПО-РУССКИ (просит объяснить правило, спрашивает")
        appendLine("  «как сказать», «что значит», «объясни») → отвечай ПО-РУССКИ,")
        appendLine("  грамотно и профессионально. Beginner не поймёт пояснение")
        appendLine("  на испанском. Испанские примеры/слова приводи внутри ответа.")
        appendLine("• Юзер смешал языки → определи доминирующий язык вопроса.")
        appendLine()
        appendLine("ОБУЧАЮЩИЕ ФИШКИ — ИЗРЕДКА, КОГДА ПОЛЕЗНО:")
        appendLine("Это важный фактор твоего поведения. Используй их экономно, не подряд,")
        appendLine("не на каждом сообщении — только когда реально помогают:")
        appendLine()
        appendLine("• ПРАВКИ: если ученик сделал серьёзную ошибку (меняет смысл, не род,")
        appendLine("  неправильное время) — мягко исправь. Формат в конце ответа:")
        appendLine("  CORRECTIONS_JSON:[{\"original\":\"\",\"corrected\":\"\",\"explanation\":\"\"}]")
        appendLine("  Максимум 1 правка. Мелкие опечатки игнорируй.")
        appendLine("  Не правь каждое сообщение — только если действительно важно.")
        appendLine()
        appendLine("• ПОЯСНЕНИЯ: если используешь редкое/сложное слово выше уровня — кратко")
        appendLine("  поясни в скобках одним русским словом. Пример: «¿Te apetece (хочется)")
        appendLine("  un café?». Не пояснять очевидные слова и не больше 1 раз за ответ.")
        appendLine()
        appendLine("• СНОСКИ/ПРАВИЛА: если ученик прямо спросил «как правильно?», «когда")
        appendLine("  использовать ser vs estar?», «объясни X» — дай ГРАМОТНОЕ и")
        appendLine("  ПРОФЕССИОНАЛЬНОЕ объяснение на ЯЗЫКЕ ВОПРОСА юзера (русский если")
        appendLine("  спросил по-русски). 2-4 предложения, с примерами в скобках.")
        appendLine("  Без запроса — не объясняй грамматику.")
        appendLine()
        appendLine("• MARKDOWN: можно **выделять** ключевое слово 1 раз за ответ если оно")
        appendLine("  действительно ключевое для понимания. Без злоупотребления.")
        appendLine()
        appendLine("ЧЕГО НЕ ДЕЛАТЬ НИКОГДА:")
        appendLine("• Не дублируй ответ полным переводом — это раздражает.")
        appendLine("• Не давай упражнений с пропусками без запроса ученика.")
        appendLine("• Не вставляй ссылки на внешние ресурсы.")
        appendLine("• Не превращай ответ в лекцию.")

        if (scenario.systemPromptExtra.isNotBlank()) {
            appendLine()
            appendLine(scenario.systemPromptExtra)
        }
    }

    private suspend fun callGemini(systemPrompt: String, history: List<ChatMessageEntity>): String? {
        val payload = JSONObject().apply {
            put("system_instruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
            })
            put("contents", JSONArray().apply {
                history.forEach { msg ->
                    put(JSONObject().apply {
                        put("role", if (msg.role == "assistant") "model" else "user")
                        put("parts", JSONArray().put(JSONObject().put("text", msg.content)))
                    })
                }
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.85)
                put("maxOutputTokens", 600)
            })
        }

        // v1.25.31: X-App-Secret + Firebase ID token. Раньше код ничего не
        // прикладывал → Cloudflare Worker возвращал 403 (missing secret)
        // / 401 (missing Firebase token). Юзер: "не работает ии".
        // GeminiTranslator уже отправлял secret, AiChatRepository нет — баг
        // от рефакторинга прокси.
        val builder = Request.Builder()
            .url(apiUrl())
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
        val secret = BuildConfig.AI_PROXY_SECRET.trim()
        if (secret.isNotEmpty()) {
            builder.header("X-App-Secret", secret)
        }
        // v1.25.33: если юзер ни разу не заходил в Profile/Settings,
        // currentUser = null → token = null → 401 от worker'а. Sign-in
        // anonymously здесь решает проблему первого открытия чата.
        val idToken = runCatching {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val user = auth.currentUser
                ?: auth.signInAnonymously().await().user
            user?.getIdToken(false)?.await()?.token
        }.onFailure { Log.w(TAG, "Firebase token error", it) }
            .getOrNull()
        if (!idToken.isNullOrEmpty()) {
            builder.header("Authorization", "Bearer $idToken")
        }
        val req = builder.build()

        okHttp.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                val errBody = resp.body?.string().orEmpty().take(200)
                Log.w(TAG, "Gemini HTTP ${resp.code}: $errBody")
                return null
            }
            val body = resp.body?.string().orEmpty()
            return parseGeminiText(body)
        }
    }

    private fun parseGeminiText(body: String): String? = try {
        JSONObject(body)
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
            .trim()
    } catch (e: Exception) {
        Log.w(TAG, "parseGeminiText failed", e)
        null
    }

    /**
     * Вырезает CORRECTIONS_JSON:[...] из конца ответа, возвращает пару
     * (видимый текст без JSON-блока, сам JSON или пустая строка).
     */
    private fun extractCorrections(raw: String): Pair<String, String> {
        val regex = Regex("""CORRECTIONS_JSON\s*:\s*(\[.*?])""", RegexOption.DOT_MATCHES_ALL)
        val match = regex.find(raw)
            ?: return raw.trim() to ""

        val json = match.groupValues[1]
        val visible = raw.removeRange(match.range).trim()
        return visible to json
    }
}

/** Результат одного хода диалога. */
data class ChatTurn(
    val assistantText: String,
    val correctionsJson: String,
)

/** Парсит CORRECTIONS_JSON в типизированный список. */
data class ChatCorrection(
    val original: String,
    val corrected: String,
    val explanation: String,
)

fun parseCorrections(json: String): List<ChatCorrection> {
    if (json.isBlank()) return emptyList()
    return runCatching {
        val arr = JSONArray(json)
        (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            ChatCorrection(
                original = obj.optString("original"),
                corrected = obj.optString("corrected"),
                explanation = obj.optString("explanation"),
            )
        }.filter { it.original.isNotBlank() && it.corrected.isNotBlank() }
    }.getOrDefault(emptyList())
}
