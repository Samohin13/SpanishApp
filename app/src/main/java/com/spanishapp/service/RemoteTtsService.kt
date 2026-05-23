package com.spanishapp.service

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.spanishapp.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * v1.18.18: Premium TTS с **сегментацией ru/es** + работающим stop.
 *
 * Что изменилось vs v1.18.17:
 *  • Сегментирует текст по unicode-блокам: cyrillic → ru voice,
 *    latin → es voice. Каждый сегмент отдельный MP3, воспроизводятся
 *    последовательно. Русский больше не звучит как «испанский акцент».
 *  • stop() реально прерывает текущее воспроизведение + отменяет
 *    очередь следующих сегментов.
 *  • isPlaying StateFlow — UI знает «сейчас говорит» для toggle-кнопки.
 *
 * Кэш файлов в cacheDir/tts/<sha1>.mp3 — повторные ответы мгновенно.
 */
@Singleton
class RemoteTtsService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val authRepository: com.spanishapp.data.repository.AuthRepository,
) {
    private val cacheDir: File by lazy {
        File(context.cacheDir, "tts").apply { mkdirs() }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var playJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    /**
     * v1.22.7: «загружается с сервера» — для мгновенного индикатора в UI.
     * Включается на тапе ▶ ДО того как mp3 скачался → пользователь видит,
     * что приложение реагирует. Выключается когда mp3 готов и плеер
     * начал играть (тогда уже isPlaying=true даёт обратную связь).
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isReady = MutableStateFlow(BuildConfig.AI_PROXY_URL.isNotBlank())
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    /**
     * Озвучить текст. Делит на ru/es сегменты, скачивает каждый mp3,
     * воспроизводит последовательно. Любой повторный вызов или stop()
     * прерывает текущее.
     *
     * @return true если хотя бы один сегмент успешно начал играть.
     */
    fun speak(
        text: String,
        speed: Float? = null,
    ): Boolean {
        if (BuildConfig.AI_PROXY_URL.isBlank()) {
            Log.d(TAG, "speak() blocked: AI_PROXY_URL blank")
            return false
        }
        if (text.isBlank()) return false

        Log.d(TAG, "speak() text='${text.take(60)}' speed=$speed")

        // Прерываем предыдущее воспроизведение
        stop()

        playJob = scope.launch {
            // v1.18.20: голос/темп из текущего TutorPersonality.
            // Personality читается из DataStore при каждом speak — изменение
            // в Settings применяется без рестарта.
            // v1.18.29: прямой выбор голоса (без пресетов)
            val ruVoiceId = authRepository.selectedRuVoice.firstOrNull()
                ?: com.spanishapp.domain.voice.PremiumVoiceCatalog.DEFAULT_RU_VOICE
            val esVoiceId = authRepository.selectedEsVoice.firstOrNull()
                ?: com.spanishapp.domain.voice.PremiumVoiceCatalog.DEFAULT_ES_VOICE
            val userMult = authRepository.voiceSpeedMultiplier.firstOrNull() ?: 1.0f
            val finalSpeed = (speed ?: userMult).coerceIn(0.25f, 4.0f)
            val finalPitch = 0f
            val segments = segmentByLanguage(
                text = text.take(2000),
                esVoice = esVoiceId,
                ruVoice = ruVoiceId,
            )
            Log.d(TAG, "voices ru=$ruVoiceId es=$esVoiceId segments=${segments.size}")
            if (segments.isEmpty()) {
                _isPlaying.value = false
                return@launch
            }
            runCatching { com.spanishapp.radio.player.RadioCoordinator.pauseForTts() }
            _isPlaying.value = true
            try {
                for (seg in segments) {
                    if (!coroutineContext.isActive) break
                    val mp3 = runCatching { fetchMp3(seg.text, seg.voice, finalSpeed, finalPitch) }
                        .onFailure { Log.w(TAG, "fetchMp3 failed for seg='${seg.text.take(40)}' voice=${seg.voice}", it) }
                        .getOrNull() ?: continue
                    Log.d(TAG, "playing seg='${seg.text.take(40)}' voice=${seg.voice} pitch=$finalPitch file=${mp3.length()}b")
                    playFileAndWait(mp3)
                }
            } finally {
                _isPlaying.value = false
            }
        }
        return true
    }

    /**
     * Sample preview — озвучить демо одного voice (используется в Settings
     * при выборе характера). НЕ прерывает текущее основное воспроизведение
     * только если оно уже остановлено.
     */
    fun previewVoice(text: String, voice: String, speed: Float = 1.0f) {
        if (BuildConfig.AI_PROXY_URL.isBlank()) return
        stop()
        _isLoading.value = true
        playJob = scope.launch {
            val mp3 = runCatching { fetchMp3(text.take(200), voice, speed) }
                .getOrNull() ?: run {
                _isLoading.value = false
                return@launch
            }
            _isLoading.value = false
            _isPlaying.value = true
            try { playFileAndWait(mp3) } finally { _isPlaying.value = false }
        }
    }

    /**
     * v1.22.7: тихо прогревает локальный кэш для пары (text, voice).
     * Используется при открытии экрана с превью голосов — все 8 семплов
     * качаются в фоне, пока юзер ещё ничего не нажал. На тапе ▶ файл
     * уже на диске → плеер стартует за ~100-200ms вместо 2-3 секунд.
     *
     * Никогда не воспроизводит, не мешает текущему playback, тихо падает
     * на ошибках.
     */
    fun prefetchPreview(text: String, voice: String, speed: Float = 1.0f) {
        if (BuildConfig.AI_PROXY_URL.isBlank()) return
        scope.launch {
            runCatching { fetchMp3(text.take(200), voice, speed) }
        }
    }

    /** Прервать воспроизведение и очистить очередь. */
    fun stop() {
        playJob?.cancel()
        playJob = null
        runCatching { mediaPlayer?.stop() }
        runCatching { mediaPlayer?.release() }
        mediaPlayer = null
        _isPlaying.value = false
        _isLoading.value = false
    }

    private suspend fun playFileAndWait(file: File) = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine<Unit> { cont ->
            runCatching { mediaPlayer?.release() }
            val mp = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnPreparedListener { it.start() }
                setOnCompletionListener {
                    runCatching { it.release() }
                    if (mediaPlayer === it) mediaPlayer = null
                    if (cont.isActive) cont.resume(Unit)
                }
                setOnErrorListener { mp, what, extra ->
                    Log.w(TAG, "MediaPlayer error what=$what extra=$extra")
                    runCatching { mp.release() }
                    if (cont.isActive) cont.resume(Unit)
                    true
                }
                prepareAsync()
            }
            mediaPlayer = mp
            cont.invokeOnCancellation {
                runCatching { mp.stop() }
                runCatching { mp.release() }
                if (mediaPlayer === mp) mediaPlayer = null
            }
        }
    }

    private suspend fun fetchMp3(text: String, voice: String, speed: Float, pitch: Float = 0f): File =
        withContext(Dispatchers.IO) {
            val hash = sha1("$voice|$speed|$pitch|$text")
            val file = File(cacheDir, "$hash.mp3")
            if (file.exists() && file.length() > 0) return@withContext file

            val proxyUrl = BuildConfig.AI_PROXY_URL.trim().trimEnd('/')
            val url = "$proxyUrl/tts"
            val bodyJson = """{"text":${jsonStr(text)},"voice":${jsonStr(voice)},"speed":$speed,"pitch":$pitch}"""
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
            }
            file
        }

    private data class Segment(val text: String, val voice: String)

    /**
     * v1.18.20: разбивает текст на куски по unicode-блокам с указанными
     * voices для каждого языка (берутся из TutorPersonality).
     *
     * - Cyrillic → ruVoice
     * - Latin → esVoice
     * - Микро-сегменты (<2 letters) приклеиваются к соседним
     * - Punctuation/whitespace присоединяются к текущему сегменту
     */
    private fun segmentByLanguage(
        text: String,
        esVoice: String,
        ruVoice: String,
    ): List<Segment> {
        val raw = mutableListOf<Pair<String, Boolean>>()  // text + isSpanish
        val sb = StringBuilder()
        var currentSpanish: Boolean? = null
        for (ch in text) {
            val isLetter = ch.isLetter()
            val isLatin = isLetter && ch !in 'Ѐ'..'ӿ'
            val isCyrillic = isLetter && ch in 'Ѐ'..'ӿ'
            val charSpanish: Boolean? = when {
                isLatin -> true
                isCyrillic -> false
                else -> null  // punct/space/digit → приклеиваем к текущему
            }
            if (charSpanish != null && currentSpanish != null && charSpanish != currentSpanish) {
                if (sb.isNotBlank()) raw.add(sb.toString() to currentSpanish!!)
                sb.clear()
            }
            if (charSpanish != null) currentSpanish = charSpanish
            sb.append(ch)
        }
        if (sb.isNotBlank() && currentSpanish != null) {
            raw.add(sb.toString() to currentSpanish!!)
        }

        // Merge: микро-сегменты (<2 letters) приклеиваем к соседним если возможно
        if (raw.isEmpty()) return emptyList()
        val merged = mutableListOf<Pair<String, Boolean>>()
        for ((segText, segSpanish) in raw) {
            val letterCount = segText.count { it.isLetter() }
            if (letterCount < 2 && merged.isNotEmpty()) {
                // Приклеиваем к предыдущему сегменту (сохраняя его язык)
                val prev = merged.removeAt(merged.lastIndex)
                merged.add((prev.first + segText) to prev.second)
            } else {
                merged.add(segText to segSpanish)
            }
        }

        return merged.mapNotNull { (segText, isSpanish) ->
            val trimmed = segText.trim()
            if (trimmed.isBlank() || trimmed.count { it.isLetter() } < 1) null
            else Segment(
                text = trimmed,
                voice = if (isSpanish) esVoice else ruVoice,
            )
        }
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
        // v1.18.20: voices теперь берутся из TutorPersonality (см. speak).
        // Эти константы остались как fallback если нет personality.
    }
}
