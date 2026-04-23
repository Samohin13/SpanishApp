package com.spanishapp.service

import android.content.Context
import android.media.MediaPlayer
import android.util.Base64
import com.spanishapp.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google Cloud Text-to-Speech with Neural2 voices.
 * Audio is cached on disk — each unique (text, voice) pair is fetched once,
 * then played from the local file on every subsequent call.
 */
@Singleton
class GoogleCloudTtsService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient
) {
    private val apiKey: String = BuildConfig.GOOGLE_TTS_API_KEY
    val isEnabled: Boolean get() = apiKey.isNotBlank()

    private val cacheDir = File(context.cacheDir, "tts_audio").also { it.mkdirs() }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var player: MediaPlayer? = null

    fun stop() {
        player?.stop()
        player?.release()
        player = null
    }

    /**
     * Speak [text] using [voiceName] (e.g. "es-ES-Neural2-B").
     * Plays from cache if available; otherwise fetches from Google, caches, then plays.
     * Calls [onFallback] if the network request fails so the caller can use Android TTS.
     */
    fun speak(text: String, voiceName: String, onFallback: () -> Unit = {}) {
        stop()
        val file = cacheFile(text, voiceName)
        if (file.exists()) {
            playFile(file)
            return
        }
        scope.launch {
            try {
                val bytes = withContext(Dispatchers.IO) { fetchAudio(text, voiceName) }
                file.writeBytes(bytes)
                playFile(file)
            } catch (e: Exception) {
                onFallback()
            }
        }
    }

    private fun cacheFile(text: String, voiceName: String): File {
        val key = "$voiceName|$text"
        val hash = MessageDigest.getInstance("MD5")
            .digest(key.toByteArray())
            .joinToString("") { "%02x".format(it) }
        return File(cacheDir, "$hash.mp3")
    }

    private fun fetchAudio(text: String, voiceName: String): ByteArray {
        val langCode = voiceName.take(5)   // "es-ES" or "es-US"
        val body = buildString {
            append("""{"input":{"text":""")
            append(JSONObject.quote(text))
            append("""},"voice":{"languageCode":"$langCode","name":"$voiceName"}""")
            append(""","audioConfig":{"audioEncoding":"MP3","speakingRate":1.0}}""")
        }
        val request = Request.Builder()
            .url("https://texttospeech.googleapis.com/v1/text:synthesize?key=$apiKey")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        val json = response.body?.string() ?: error("Empty response from Google TTS")
        val audio = JSONObject(json).optString("audioContent", "")
        check(audio.isNotBlank()) { "Google TTS error: $json" }
        return Base64.decode(audio, Base64.DEFAULT)
    }

    private fun playFile(file: File) {
        stop()
        player = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepare()
            start()
        }
    }
}
