package com.spanishapp.service

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.spanishapp.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * v1.18.17: Premium TTS через Cloudflare Worker → Google Cloud TTS Neural2.
 *
 * Зачем: Android системный TTS звучит «пластиково», читает эмодзи как
 * слова, переключение между ru/es voices даёт разрывы. Neural2 — нейронный
 * голос с естественной интонацией.
 *
 * Архитектура:
 *   Каждый speak(text, voice) → POST /tts на Worker → MP3 bytes
 *   → файл-кэш cacheDir/tts/<sha1>.mp3 → MediaPlayer.start()
 *
 * Кэш — повторные тапы на тот же ответ не тратят квоту.
 * Fallback — если /tts failed, caller использует Android TTS.
 */
@Singleton
class RemoteTtsService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
) {
    private val cacheDir: File by lazy {
        File(context.cacheDir, "tts").apply { mkdirs() }
    }

    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isReady = MutableStateFlow(BuildConfig.AI_PROXY_URL.isNotBlank())
    /** true если Worker URL сконфигурирован (premium TTS доступен). */
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    /**
     * Озвучить текст премиум-голосом.
     * @return true если успешно начал воспроизведение, false — fallback нужен.
     */
    suspend fun speak(
        text: String,
        voice: String = DEFAULT_VOICE,
        speed: Float = 1.0f,
    ): Boolean = withContext(Dispatchers.IO) {
        if (BuildConfig.AI_PROXY_URL.isBlank()) {
            Log.w(TAG, "AI_PROXY_URL not configured — skipping remote TTS")
            return@withContext false
        }
        if (text.isBlank()) return@withContext false

        val mp3 = runCatching { fetchMp3(text.take(2000), voice, speed) }
            .getOrNull()
            ?: return@withContext false

        withContext(Dispatchers.Main) { playMp3(mp3) }
        true
    }

    /** Остановить воспроизведение. */
    fun stop() {
        runCatching { mediaPlayer?.stop() }
        runCatching { mediaPlayer?.release() }
        mediaPlayer = null
        _isPlaying.value = false
    }

    private suspend fun fetchMp3(text: String, voice: String, speed: Float): File {
        val hash = sha1("$voice|$speed|$text")
        val file = File(cacheDir, "$hash.mp3")
        if (file.exists() && file.length() > 0) {
            Log.d(TAG, "cache HIT (${file.length()}b) for voice=$voice text='${text.take(40)}...'")
            return file
        }

        val proxyUrl = BuildConfig.AI_PROXY_URL.trim().trimEnd('/')
        val url = "$proxyUrl/tts"
        val bodyJson = """
            {"text":${jsonStr(text)},"voice":${jsonStr(voice)},"speed":$speed}
        """.trimIndent()
        val request = Request.Builder()
            .url(url)
            .post(bodyJson.toRequestBody("application/json".toMediaType()))
            .header("Content-Type", "application/json")
            .apply {
                val secret = BuildConfig.AI_PROXY_SECRET.trim()
                if (secret.isNotEmpty()) header("X-App-Secret", secret)
            }
            .build()

        okHttpClient.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) {
                val errBody = resp.body?.string()?.take(200)
                Log.w(TAG, "TTS failed HTTP ${resp.code}: $errBody")
                error("TTS HTTP ${resp.code}")
            }
            val bytes = resp.body?.bytes() ?: error("Empty TTS body")
            file.writeBytes(bytes)
            Log.d(TAG, "cache STORE ${bytes.size}b for voice=$voice text='${text.take(40)}...'")
        }
        return file
    }

    private fun playMp3(file: File) {
        runCatching { mediaPlayer?.release() }
        // Радио ставим на паузу — не озвучивать поверх музыки.
        runCatching { com.spanishapp.radio.player.RadioCoordinator.pauseForTts() }

        val mp = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            setOnPreparedListener { it.start() }
            setOnCompletionListener {
                _isPlaying.value = false
                runCatching { it.release() }
                if (mediaPlayer === it) mediaPlayer = null
            }
            setOnErrorListener { mp, what, extra ->
                Log.w(TAG, "MediaPlayer error: what=$what extra=$extra")
                _isPlaying.value = false
                runCatching { mp.release() }
                true
            }
            prepareAsync()
        }
        mediaPlayer = mp
        _isPlaying.value = true
    }

    private fun sha1(s: String): String {
        val digest = MessageDigest.getInstance("SHA-1").digest(s.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun jsonStr(s: String): String {
        val escaped = s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
        return "\"$escaped\""
    }

    companion object {
        private const val TAG = "RemoteTts"
        const val DEFAULT_VOICE = "es-ES-Neural2-A"   // женский, нейронный, чистый
    }
}
