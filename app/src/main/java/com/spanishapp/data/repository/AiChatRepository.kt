package com.spanishapp.data.repository

import com.spanishapp.BuildConfig
import com.spanishapp.data.db.dao.ChatMessageDao
import com.spanishapp.data.db.entity.ChatMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
        // Gemini 1.5 Flash — бесплатно: 15 RPM, 1500 RPD
        private const val MODEL = "gemini-1.5-flash"
        private fun apiUrl() =
            "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent" +
            "?key=${BuildConfig.GEMINI_API_KEY}"

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
