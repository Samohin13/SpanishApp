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
        private const val MODEL = "gemini-flash-latest"

        /**
         * If [BuildConfig.AI_PROXY_URL] is set in local.properties, use it —
         * the proxy hides the API key from the APK. Otherwise fall back to
         * direct Gemini calls with the bundled key (dev/local builds only).
         */
        private fun apiUrl(): String {
            val proxy = BuildConfig.AI_PROXY_URL.trim().trimEnd('/')
            return if (proxy.isNotEmpty()) {
                "$proxy/v1beta/models/$MODEL:generateContent"
            } else {
                "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent" +
                    "?key=${BuildConfig.GEMINI_API_KEY}"
            }
        }

        /** Server-Sent-Events streaming endpoint. */
        private fun streamUrl(): String {
            val proxy = BuildConfig.AI_PROXY_URL.trim().trimEnd('/')
            return if (proxy.isNotEmpty()) {
                "$proxy/v1beta/models/$MODEL:streamGenerateContent"
            } else {
                "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:streamGenerateContent" +
                    "?key=${BuildConfig.GEMINI_API_KEY}&alt=sse"
            }
        }

        private val SYSTEM_PROMPT = """
            Eres un tutor de español amigable y paciente para hablantes de ruso.

            REGLAS:
            1. Responde SIEMPRE en español, pero incluye traducción al ruso entre [corchetes] para palabras difíciles.
            2. Si el usuario escribe en ruso, responde primero en español y luego explica en ruso.
            3. Corrige los errores gramaticales del usuario de forma AMABLE:
               - Primero valida lo que dijo bien.
               - Luego muestra la versión corregida con ✏️
               - Explica brevemente el error en ruso.
            4. Adapta el nivel: si el usuario parece principiante (A1/A2), usa frases simples.
            5. Haz preguntas para mantener la conversación activa.
            6. Al final de cada respuesta, incluye una "Palabra del día" relevante al tema.

            FORMATO DE CORRECCIÓN (JSON al final del mensaje si hay errores):
            CORRECTIONS_JSON:[{"original":"texto con error","corrected":"texto correcto","explanation":"объяснение на русском"}]
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
            // Save user message
            chatMessageDao.insert(
                ChatMessageEntity(role = "user", content = userText, sessionId = sessionId)
            )

            // Build conversation history
            val history = chatMessageDao.getSessionOnce(sessionId)
                .takeLast(20)

            val body = buildGeminiRequest(history)

            val request = Request.Builder()
                .url(apiUrl())
                .post(body)
                .header("Content-Type", "application/json")
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                return@withContext Result.failure(Exception("Gemini error ${response.code}: $errBody"))
            }

            val json = Json.parseToJsonElement(response.body!!.string()).jsonObject
            val assistantText = json["candidates"]!!
                .jsonArray[0]
                .jsonObject["content"]!!
                .jsonObject["parts"]!!
                .jsonArray[0]
                .jsonObject["text"]!!
                .jsonPrimitive.content

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
        sessionId: String = "default"
    ): Flow<String> = flow {
        // Save user message first so it appears in UI immediately.
        chatMessageDao.insert(
            ChatMessageEntity(role = "user", content = userText, sessionId = sessionId)
        )

        val history = chatMessageDao.getSessionOnce(sessionId).takeLast(20)
        val body = buildGeminiRequest(history)

        val request = Request.Builder()
            .url(streamUrl())
            .post(body)
            .header("Content-Type", "application/json")
            .header("Accept", "text/event-stream")
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val errBody = response.body?.string() ?: ""
            throw Exception("Gemini error ${response.code}: $errBody")
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
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val raw = Json.parseToJsonElement(response.body!!.string())
                    .jsonObject["candidates"]!!
                    .jsonArray[0]
                    .jsonObject["content"]!!
                    .jsonObject["parts"]!!
                    .jsonArray[0]
                    .jsonObject["text"]!!
                    .jsonPrimitive.content

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
        withSystem: Boolean = true
    ): RequestBody {
        val json = buildJsonObject {
            // System instruction (Gemini 1.5 supports it)
            if (withSystem) {
                putJsonObject("system_instruction") {
                    putJsonObject("parts") {
                        put("text", SYSTEM_PROMPT)
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
                put("maxOutputTokens", 1000)
                put("temperature", 0.7)
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
