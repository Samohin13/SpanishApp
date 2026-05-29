package com.spanishapp.data.repository

import android.util.Log
import android.util.LruCache
import com.spanishapp.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Узкоспециализированный fallback для перевода испанских слов и
 * предложений через Gemini, когда локального словаря недостаточно.
 *
 * Кэширует результаты в LruCache (256 записей) — повторные запросы
 * по одному и тому же слову / предложению уходят без сети.
 *
 * Использование: только когда WordDao не нашёл слово даже после
 * лемматизации. Для частых слов локальный поиск всегда быстрее.
 */
@Singleton
class GeminiTranslator @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val cache = LruCache<String, String>(256)

    private val json = Json { ignoreUnknownKeys = true }

    private companion object {
        // gemini-1.5-flash was removed from v1beta — use the maintained alias.
        const val MODEL = "gemini-flash-latest"

        /**
         * Routes through the Cloudflare Worker proxy when configured.
         * В release-сборке fallback на direct call ЗАПРЕЩЁН (как в AiChatRepository) —
         * иначе ключ ушёл бы в APK. Только debug разрешает direct.
         */
        fun apiUrl(): String {
            val proxy = BuildConfig.AI_PROXY_URL.trim().trimEnd('/')
            if (proxy.isNotEmpty()) {
                return "$proxy/v1beta/models/$MODEL:generateContent"
            }
            require(BuildConfig.DEBUG) {
                "AI_PROXY_URL must be configured for release builds (GeminiTranslator)."
            }
            return "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent" +
                "?key=${BuildConfig.GEMINI_API_KEY}"
        }
    }

    /**
     * Возвращает короткий русский перевод слова в контексте предложения,
     * либо пустую строку при ошибке. Безопасно вызывать на UI-нити —
     * сама делает переключение на IO.
     */
    suspend fun translateWord(word: String, sentence: String): String =
        withContext(Dispatchers.IO) {
            val key = "w|${word.lowercase()}"
            cache.get(key)?.let { return@withContext it }

            val prompt = buildString {
                append("Переведи испанское слово '")
                append(word)
                append("' на русский. Контекст предложения: \"")
                append(sentence)
                append("\". Ответь ОДНИМ словом или короткой фразой (max 5 слов) на русском, без объяснений и кавычек.")
            }

            val result = callGemini(prompt) ?: ""
            if (result.isNotBlank()) cache.put(key, result)
            result
        }

    /**
     * Полный перевод предложения. Можно вызывать опционально, например
     * если локальный лукап вернул мало слов.
     */
    suspend fun translateSentence(sentence: String): String =
        withContext(Dispatchers.IO) {
            val key = "s|${sentence.lowercase()}"
            cache.get(key)?.let { return@withContext it }

            val prompt = "Переведи на русский: \"$sentence\". Ответь только переводом."
            val result = callGemini(prompt) ?: ""
            if (result.isNotBlank()) cache.put(key, result)
            result
        }

    private suspend fun callGemini(prompt: String): String? {
        // Allow direct mode (key in BuildConfig) OR proxy mode (URL in BuildConfig).
        val hasProxy = BuildConfig.AI_PROXY_URL.isNotBlank()
        val hasKey   = BuildConfig.GEMINI_API_KEY.isNotBlank()
        if (!hasProxy && !hasKey) {
            Log.w("GeminiTranslator", "Neither AI_PROXY_URL nor GEMINI_API_KEY set, skipping")
            return null
        }
        val payload = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().put("parts", JSONArray().put(
                    JSONObject().put("text", prompt)
                ))
            ))
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
                put("maxOutputTokens", 60)
            })
        }.toString()
        val body = payload.toRequestBody("application/json".toMediaType())

        return runCatching {
            val builder = Request.Builder()
                .url(apiUrl())
                .post(body)
                .header("Content-Type", "application/json")
            // Same shared-secret check as AiChatRepository — proxy needs it.
            val proxy = BuildConfig.AI_PROXY_URL.trim()
            val secret = BuildConfig.AI_PROXY_SECRET.trim()
            if (proxy.isNotEmpty() && secret.isNotEmpty()) {
                builder.header("X-App-Secret", secret)
            }
            // v1.25.31: deployed worker также требует Firebase ID token.
            // v1.25.33: anonymous sign-in fallback если currentUser=null.
            val idToken = runCatching {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                val user = auth.currentUser
                    ?: auth.signInAnonymously().await().user
                user?.getIdToken(false)?.await()?.token
            }.getOrNull()
            if (!idToken.isNullOrEmpty()) {
                builder.header("Authorization", "Bearer $idToken")
            }
            val request = builder.build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@runCatching null
                val text = response.body?.string() ?: return@runCatching null
                json.parseToJsonElement(text).jsonObject["candidates"]
                    ?.jsonArray?.firstOrNull()
                    ?.jsonObject?.get("content")
                    ?.jsonObject?.get("parts")
                    ?.jsonArray?.firstOrNull()
                    ?.jsonObject?.get("text")
                    ?.jsonPrimitive?.content
                    ?.trim()
                    ?.removeSurrounding("\"")
            }
        }.onFailure { Log.w("GeminiTranslator", "translate failed", it) }.getOrNull()
    }
}
