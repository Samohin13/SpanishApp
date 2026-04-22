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
        private const val API_URL = "https://api.anthropic.com/v1/messages"
        private const val MODEL   = "claude-sonnet-4-20250514"

        // System prompt: AI is a Spanish tutor
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
            CORRECTIONS_JSON:[{"original":"texto con error","corrected":"texto correcto","explanation":"explicación en ruso"}]
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

            // Build conversation history for API
            val history = chatMessageDao.getSessionOnce(sessionId)
                .takeLast(20)  // last 20 messages for context window
                .map { buildMessageJson(it) }

            val body = buildRequestBody(history)

            val request = Request.Builder()
                .url(API_URL)
                .post(body)
                .header("x-api-key", BuildConfig.ANTHROPIC_API_KEY)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("API error: ${response.code}"))
            }

            val responseJson = Json.parseToJsonElement(response.body!!.string()).jsonObject
            val assistantText = responseJson["content"]!!
                .jsonArray[0]
                .jsonObject["text"]!!
                .jsonPrimitive.content

            // Extract corrections JSON if present
            val correctionJson = extractCorrections(assistantText)
            val cleanText = assistantText
                .substringBefore("CORRECTIONS_JSON:").trim()

            // Save assistant response
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

    // ── Grammar check only (no conversation saved) ───────────
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

                val msgList = listOf(
                    buildJsonObject {
                        put("role", "user")
                        put("content", prompt)
                    }
                )

                val body = buildRequestBody(msgList, maxTokens = 500)

                val request = Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .header("x-api-key", BuildConfig.ANTHROPIC_API_KEY)
                    .header("anthropic-version", "2023-06-01")
                    .header("content-type", "application/json")
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseText = Json.parseToJsonElement(response.body!!.string())
                    .jsonObject["content"]!!
                    .jsonArray[0]
                    .jsonObject["text"]!!
                    .jsonPrimitive.content

                val result = Json.decodeFromString<GrammarCheckResult>(responseText)
                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // ── New conversation session ──────────────────────────────
    suspend fun clearSession(sessionId: String) {
        chatMessageDao.clearSession(sessionId)
    }

    // ── Helpers ───────────────────────────────────────────────
    private fun buildRequestBody(
        messages: List<JsonObject>,
        maxTokens: Int = 1000
    ): RequestBody {
        val json = buildJsonObject {
            put("model", MODEL)
            put("max_tokens", maxTokens)
            put("system", SYSTEM_PROMPT)
            putJsonArray("messages") {
                messages.forEach { add(it) }
            }
        }.toString()

        return json.toRequestBody("application/json".toMediaType())
    }

    private fun buildMessageJson(entity: ChatMessageEntity): JsonObject =
        buildJsonObject {
            put("role", entity.role)
            put("content", entity.content)
        }

    private fun extractCorrections(text: String): String {
        val marker = "CORRECTIONS_JSON:"
        val idx = text.indexOf(marker)
        return if (idx >= 0) text.substring(idx + marker.length).trim() else ""
    }
}

// ─────────────────────────────────────────────────────────────
// Grammar check result model
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