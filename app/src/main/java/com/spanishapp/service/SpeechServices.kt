package com.spanishapp.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

// ═════════════════════════════════════════════════════════════
//  TEXT-TO-SPEECH  —  Spanish pronunciation
// ═════════════════════════════════════════════════════════════
@Singleton
class SpanishTts @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    // Supported Spanish locales in priority order
    private val preferredLocales = listOf(
        Locale("es", "ES"),   // Spain
        Locale("es", "MX"),   // Mexico
        Locale("es", "US"),   // US Spanish
        Locale("es")          // Generic
    )

    init { initialize() }

    private fun initialize() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = preferredLocales.firstOrNull { loc ->
                    tts?.isLanguageAvailable(loc) == TextToSpeech.LANG_AVAILABLE ||
                            tts?.isLanguageAvailable(loc) == TextToSpeech.LANG_COUNTRY_AVAILABLE
                } ?: Locale("es")

                tts?.language = locale
                tts?.setSpeechRate(0.9f)     // slightly slower for learners
                tts?.setPitch(1.0f)
                _isReady.value = true
            }
        }
    }

    /**
     * Speak Spanish text aloud.
     * @param slow If true, speaks at 0.6x speed for pronunciation practice
     */
    fun speak(text: String, slow: Boolean = false) {
        if (!_isReady.value) return
        tts?.setSpeechRate(if (slow) 0.6f else 0.9f)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utterance_$text")
    }

    /** Suspend until speaking is done */
    suspend fun speakAndWait(text: String, slow: Boolean = false) =
        suspendCancellableCoroutine { cont ->
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
            tts?.setSpeechRate(if (slow) 0.6f else 0.9f)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
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
//  SPEECH RECOGNITION  —  check user pronunciation / free input
// ═════════════════════════════════════════════════════════════
sealed class SpeechResult {
    data class Success(val text: String, val confidence: Float) : SpeechResult()
    data class Error(val message: String) : SpeechResult()
    object Cancelled : SpeechResult()
}

@Singleton
class SpanishSpeechRecognizer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    /**
     * Listens once and returns the recognized Spanish text.
     * Requires RECORD_AUDIO permission.
     */
    suspend fun listenOnce(): SpeechResult = suspendCancellableCoroutine { cont ->
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            cont.resume(SpeechResult.Error("Распознавание речи недоступно на этом устройстве"))
            return@suspendCancellableCoroutine
        }

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-ES")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_RESULTS, true)
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
                val scores = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                val best = matches?.firstOrNull()
                if (best != null) {
                    cont.resume(SpeechResult.Success(best, scores?.firstOrNull() ?: 1f))
                } else {
                    cont.resume(SpeechResult.Error("Речь не распознана"))
                }
                recognizer.destroy()
            }

            override fun onError(error: Int) {
                _isListening.value = false
                val msg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO             -> "Ошибка аудио"
                    SpeechRecognizer.ERROR_NO_MATCH          -> "Речь не распознана, попробуй ещё раз"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT    -> "Тайм-аут, говори громче"
                    SpeechRecognizer.ERROR_NETWORK           -> "Нет интернета для распознавания"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Нет разрешения на микрофон"
                    else -> "Ошибка распознавания ($error)"
                }
                cont.resume(SpeechResult.Error(msg))
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

    /**
     * Pronunciation check: compare what user said vs expected word.
     * Returns 0.0–1.0 similarity score.
     */
    suspend fun checkPronunciation(expected: String): PronunciationResult {
        return when (val result = listenOnce()) {
            is SpeechResult.Success -> {
                val similarity = stringSimilarity(
                    result.text.lowercase().trim(),
                    expected.lowercase().trim()
                )
                PronunciationResult(
                    recognized = result.text,
                    expected   = expected,
                    score      = similarity,
                    passed     = similarity >= 0.75f
                )
            }
            is SpeechResult.Error   -> PronunciationResult(
                recognized = "", expected = expected, score = 0f, passed = false, error = result.message
            )
            SpeechResult.Cancelled  -> PronunciationResult(
                recognized = "", expected = expected, score = 0f, passed = false
            )
        }
    }

    // Levenshtein-based similarity 0.0–1.0
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

data class PronunciationResult(
    val recognized: String,
    val expected: String,
    val score: Float,       // 0.0–1.0
    val passed: Boolean,
    val error: String = ""
)