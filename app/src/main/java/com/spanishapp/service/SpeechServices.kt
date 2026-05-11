package com.spanishapp.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.spanishapp.data.prefs.AppPreferences
import com.spanishapp.data.prefs.VoicePreferences
import com.spanishapp.data.prefs.VoiceSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

// ═════════════════════════════════════════════════════════════
//  TEXT-TO-SPEECH  —  simple Spanish pronunciation, default voice
// ═════════════════════════════════════════════════════════════
@Singleton
class SpanishTts @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences,
    private val voicePreferences: VoicePreferences
) {
    private var tts: TextToSpeech? = null
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    // Кэшируем состояние тоггла «Голос диктора» — обновляется при каждом
    // изменении настройки. Если выключено — speak() становится no-op.
    @Volatile private var enabled: Boolean = true

    // Latest user-picked voice config from SettingsVoice. Applied on every
    // speak() so the singleton actually respects the persona/rate/pitch
    // chosen in Settings — previously this class hard-coded rate=0.9.
    @Volatile private var voiceCfg: VoiceSettings = VoiceSettings()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val preferredLocales = listOf(
        Locale("es", "ES"),
        Locale("es", "MX"),
        Locale("es", "US"),
        Locale("es")
    )

    init {
        initialize()
        // Подписываемся на изменения настройки
        scope.launch {
            appPreferences.ttsEnabled.collect { enabled = it }
        }
        scope.launch {
            voicePreferences.settings.collect { cfg ->
                voiceCfg = cfg
                applyVoiceConfig()
            }
        }
    }

    /**
     * Applies the user-selected voice name onto the underlying TextToSpeech.
     * Rate and pitch are also pushed live so SettingsVoice sliders take
     * effect immediately on every screen that uses this singleton.
     */
    private fun applyVoiceConfig() {
        val t = tts ?: return
        if (!_isReady.value) return
        val cfg = voiceCfg
        val targetName = cfg.selectedVoiceName
        if (!targetName.isNullOrBlank()) {
            val v = t.voices?.firstOrNull { it.name == targetName }
            if (v != null) {
                runCatching { t.voice = v }
            }
        }
        t.setSpeechRate(cfg.rate.coerceIn(0.3f, 2.0f))
        t.setPitch(cfg.pitch.coerceIn(0.5f, 2.0f))
    }

    private fun initialize() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = preferredLocales.firstOrNull { loc ->
                    tts?.isLanguageAvailable(loc) == TextToSpeech.LANG_AVAILABLE ||
                            tts?.isLanguageAvailable(loc) == TextToSpeech.LANG_COUNTRY_AVAILABLE
                } ?: Locale("es")
                tts?.language = locale
                tts?.setSpeechRate(0.9f)
                tts?.setPitch(1.0f)
                _isReady.value = true
                // Apply any voice config that arrived before init finished.
                applyVoiceConfig()
            }
        }
    }

    /**
     * Решает, имеет ли смысл озвучивать строку через испанский TTS.
     *  • Минимум 2 латинские буквы подряд (отсекает "a", "?", "1", "___")
     *  • Без кириллицы (русский перевод не озвучиваем)
     */
    private fun inferSpeakText(text: String?): String? {
        if (text.isNullOrBlank()) return null
        val cleaned = text.replace("_", " ").trim()
        val letters = cleaned.filter { it.isLetter() }
        if (letters.isEmpty()) return null
        val cyrillic = letters.count { it in 'Ѐ'..'ӿ' }
        val latin = letters.count { it !in 'Ѐ'..'ӿ' }
        if (latin == 0) return null
        if (cyrillic.toDouble() / letters.length > 0.5) return null
        val uniqueFolded = letters.filter { it !in 'Ѐ'..'ӿ' }.map { it.lowercaseChar() }.toSet()
        if (uniqueFolded.size == 1) return uniqueFolded.first().toString()
        if (!Regex("[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]{2,}").containsMatchIn(cleaned)) return null
        return cleaned.replace("___", " ").replace(Regex("\\s+"), " ").trim()
    }

    /** Speak Spanish text aloud. @param slow — 0.66× rate for careful listening. */
    fun speak(text: String, slow: Boolean = false) {
        if (!enabled) return  // Юзер отключил голос диктора в настройках.
        val t = tts ?: return
        if (!_isReady.value) return
        val speakText = inferSpeakText(text) ?: return
        t.setSpeechRate(if (slow) (voiceCfg.rate * 0.7f).coerceIn(0.3f, 2.0f) else voiceCfg.rate.coerceIn(0.3f, 2.0f))
        t.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, "utterance_${System.currentTimeMillis()}")
    }

    suspend fun speakAndWait(text: String, slow: Boolean = false) =
        suspendCancellableCoroutine { cont ->
            if (!enabled) {  // Юзер отключил голос диктора — не блокируем сценарий
                if (cont.isActive) cont.resume(Unit)
                return@suspendCancellableCoroutine
            }
            val speakText = inferSpeakText(text)
            if (speakText == null) {
                if (cont.isActive) cont.resume(Unit)
                return@suspendCancellableCoroutine
            }
            val id = "wait_${System.currentTimeMillis()}"
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    if (utteranceId == id && cont.isActive) cont.resume(Unit)
                }
                override fun onError(utteranceId: String?) {
                    if (utteranceId == id && cont.isActive) cont.resume(Unit)
                }
            })
            tts?.setSpeechRate(if (slow) (voiceCfg.rate * 0.7f).coerceIn(0.3f, 2.0f) else voiceCfg.rate.coerceIn(0.3f, 2.0f))
            tts?.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, id)
            cont.invokeOnCancellation { tts?.stop() }
        }

    fun stop() { tts?.stop() }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        _isReady.value = false
    }
}

// ═════════════════════════════════════════════════════════════
//  SPEECH RECOGNITION
// ═════════════════════════════════════════════════════════════
sealed class SpeechResult {
    data class Success(
        val text: String,
        val confidence: Float,
        val alternatives: List<Pair<String, Float>> = emptyList()
    ) : SpeechResult()
    data class Error(val message: String, val isSilence: Boolean = false) : SpeechResult()
    object Cancelled : SpeechResult()
}

@Singleton
class SpanishSpeechRecognizer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    /**
     * Recognize speech once.
     *
     * @param language BCP-47 locale tag. Default `es-ES` (Spanish-Spain) for
     *   pronunciation games. Pass `ru-RU` for AI-chat dictation when the user
     *   speaks Russian, or other locales as needed.
     */
    suspend fun listenOnce(language: String = "es-ES"): SpeechResult = suspendCancellableCoroutine { cont ->
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            cont.resume(SpeechResult.Error("Распознавание речи недоступно на этом устройстве"))
            return@suspendCancellableCoroutine
        }

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { _isListening.value = true }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { _isListening.value = false }

            override fun onResults(results: Bundle?) {
                _isListening.value = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val scores  = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                if (!matches.isNullOrEmpty()) {
                    val alts = matches.mapIndexed { i, t ->
                        t to (scores?.getOrNull(i) ?: 1f)
                    }
                    cont.resume(SpeechResult.Success(alts[0].first, alts[0].second, alts))
                } else {
                    cont.resume(SpeechResult.Error("Не расслышал, попробуй ещё раз"))
                }
                recognizer.destroy()
            }

            override fun onError(error: Int) {
                _isListening.value = false
                val isSilence = error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT ||
                                error == SpeechRecognizer.ERROR_NO_MATCH
                val msg = when (error) {
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT    -> "silence"
                    SpeechRecognizer.ERROR_NO_MATCH          -> "no_match"
                    SpeechRecognizer.ERROR_AUDIO             -> "Ошибка микрофона, попробуй ещё раз"
                    SpeechRecognizer.ERROR_NETWORK           -> "Нет интернета для распознавания речи"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Нет разрешения на микрофон"
                    else -> "Ошибка ($error), попробуй ещё раз"
                }
                cont.resume(SpeechResult.Error(msg, isSilence))
                recognizer.destroy()
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        recognizer.startListening(intent)

        cont.invokeOnCancellation {
            _isListening.value = false
            recognizer.cancel()
            recognizer.destroy()
            cont.resume(SpeechResult.Cancelled)
        }
    }

    suspend fun checkPronunciation(expected: String): PronunciationResult {
        return when (val result = listenOnce()) {
            is SpeechResult.Success -> {
                val expNorm = phoneticallyNormalize(expected)
                val threshold = when {
                    expNorm.length <= 4 -> 0.90f
                    expNorm.length <= 7 -> 0.82f
                    else                -> 0.75f
                }

                // Проверяем все альтернативы и берём лучшую по фонетическому сходству
                val bestAlt = result.alternatives.maxByOrNull { (text, _) ->
                    stringSimilarity(phoneticallyNormalize(text), expNorm)
                } ?: (result.text to result.confidence)

                val bestNorm   = phoneticallyNormalize(bestAlt.first)
                val similarity = stringSimilarity(bestNorm, expNorm)

                // Первая буква должна совпадать для коротких слов
                val firstLetterOk = expNorm.length > 5 ||
                    (bestNorm.isNotEmpty() && expNorm.isNotEmpty() &&
                     bestNorm.first() == expNorm.first())

                val passed = similarity >= threshold && firstLetterOk

                // Качество произношения по confidence + similarity
                val quality = when {
                    !passed                          -> PronunciationQuality.WRONG
                    similarity >= 0.95f && result.confidence >= 0.8f -> PronunciationQuality.PERFECT
                    similarity >= 0.85f              -> PronunciationQuality.GOOD
                    else                             -> PronunciationQuality.ACCEPTABLE
                }

                PronunciationResult(
                    recognized = bestAlt.first,
                    expected   = expected,
                    score      = similarity,
                    passed     = passed,
                    quality    = quality
                )
            }
            is SpeechResult.Error -> PronunciationResult(
                recognized = "",
                expected   = expected,
                score      = 0f,
                passed     = false,
                isSilence  = result.isSilence,
                error      = if (result.isSilence) "" else result.message
            )
            SpeechResult.Cancelled -> PronunciationResult(
                recognized = "", expected = expected, score = 0f, passed = false
            )
        }
    }

    /**
     * Фонетическая нормализация испанского слова перед сравнением:
     *  • нижний регистр
     *  • убираем ударения (á→a, é→e, ...)
     *  • ñ→n (STT может вернуть n вместо ñ)
     *  • убираем немую H в начале слова (hola → ola, hablar → ablar)
     */
    private fun phoneticallyNormalize(word: String): String =
        word.lowercase()
            .replace("á", "a").replace("é", "e").replace("í", "i")
            .replace("ó", "o").replace("ú", "u").replace("ü", "u")
            .replace("ñ", "n").replace("¿", "").replace("¡", "")
            .trim()
            .let { if (it.startsWith("h")) it.substring(1) else it }

    private fun stringSimilarity(a: String, b: String): Float {
        if (a == b) return 1f
        if (a.isEmpty() || b.isEmpty()) return 0f
        val dist = levenshtein(a, b)
        return 1f - dist.toFloat() / maxOf(a.length, b.length)
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) for (j in 1..b.length) {
            dp[i][j] = if (a[i-1] == b[j-1]) dp[i-1][j-1]
            else 1 + minOf(dp[i-1][j], dp[i][j-1], dp[i-1][j-1])
        }
        return dp[a.length][b.length]
    }
}

enum class PronunciationQuality { PERFECT, GOOD, ACCEPTABLE, WRONG }

data class PronunciationResult(
    val recognized: String,
    val expected:   String,
    val score:      Float,
    val passed:     Boolean,
    val quality:    PronunciationQuality = PronunciationQuality.WRONG,
    val isSilence:  Boolean = false,
    val error:      String  = ""
)
