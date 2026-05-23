package com.spanishapp.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
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
    private val voicePreferences: VoicePreferences,
    private val remoteTts: RemoteTtsService,
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
     * Решает, что озвучивать через испанский TTS.
     *
     * v1.18.3 (BUG-024): AI Chat ответы — смешанный русский + испанские
     * слова в скобках/после двоеточия. Старая логика «cyrillic > 50% →
     * null» молчала на 100% ответов ИИ.
     *
     * Новая логика:
     *  1. Чистый испанский (≤15% кириллицы) — озвучить целиком.
     *  2. Смешанный (русский + испанские фрагменты) — extract все
     *     испанские слова/фразы (последовательности латинских букв с
     *     accent marks, длиной 2+), склеить через паузы.
     *  3. Если латиницы нет совсем → null.
     */
    private fun inferSpeakText(text: String?): String? {
        if (text.isNullOrBlank()) return null
        val cleaned = text.replace("_", " ").trim()
        val letters = cleaned.filter { it.isLetter() }
        if (letters.isEmpty()) return null
        val cyrillicCount = letters.count { it in 'Ѐ'..'ӿ' }
        val latinCount = letters.count { it !in 'Ѐ'..'ӿ' }
        if (latinCount == 0) return null

        // Чистый испанский / латиница — озвучиваем целиком
        if (cyrillicCount.toDouble() / letters.length <= 0.15) {
            val uniqueFolded = letters.map { it.lowercaseChar() }.toSet()
            if (uniqueFolded.size == 1) return uniqueFolded.first().toString()
            if (!Regex("[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]{2,}").containsMatchIn(cleaned)) return null
            return cleaned.replace("___", " ").replace(Regex("\\s+"), " ").trim()
        }

        // Смешанный текст — extract испанские фрагменты.
        // Регулярка: последовательность латинских (с accent marks),
        // включая дефисы и апострофы (cómo, l'apostrophe, well-known),
        // длиной 2+ символа. Punctuation ¿?¡! берём чтобы интонация
        // прозвучала естественно.
        val spanishRegex = Regex(
            "[¿¡]?[A-Za-zÁÉÍÓÚÜÑáéíóúüñ][A-Za-zÁÉÍÓÚÜÑáéíóúüñ'-]+(?:[?!.,;:]?\\s+[¿¡]?[A-Za-zÁÉÍÓÚÜÑáéíóúüñ][A-Za-zÁÉÍÓÚÜÑáéíóúüñ'-]+)*[?!.]?"
        )
        val fragments = spanishRegex.findAll(cleaned)
            .map { it.value.trim() }
            .filter { it.length >= 2 && Regex("[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]{2,}").containsMatchIn(it) }
            .toList()

        if (fragments.isEmpty()) return null
        // Склеиваем через ". " — TTS добавит паузу между фразами.
        return fragments.joinToString(". ").take(500)  // safety cap
    }

    /**
     * Speak Spanish text aloud.
     * @param slow — 0.66× rate for careful listening.
     * @param fullMixed — если true, читает текст ЦЕЛИКОМ переключая язык
     *                   между русскими и испанскими сегментами (для AI Chat).
     *                   Если false (default) — только испанские части.
     */
    fun speak(text: String, slow: Boolean = false, fullMixed: Boolean = false, esVoiceOverride: String? = null) {
        if (!enabled) {
            Log.d(TAG_ROUTE, "speak() blocked: ttsEnabled=false")
            return
        }

        // v1.18.23: ВСЕ курсы тоже идут через premium TTS (Google Cloud
        // с выбранным TutorPersonality + полом голоса).
        // v1.22.20: esVoiceOverride — per-NPC голос для чекпоинтов.
        val remoteReady = remoteTts.isReady.value
        Log.d(TAG_ROUTE, "speak() text='${text.take(40)}' slow=$slow mixed=$fullMixed remote=$remoteReady override=$esVoiceOverride")
        if (remoteReady) {
            val speakText = if (fullMixed) sanitizeForFullSpeech(text) else inferSpeakText(text)
            if (!speakText.isNullOrBlank()) {
                val ok = if (slow) {
                    remoteTts.speak(speakText, speed = 0.7f, esVoiceOverride = esVoiceOverride)
                } else {
                    remoteTts.speak(speakText, speed = null, esVoiceOverride = esVoiceOverride)
                }
                Log.d(TAG_ROUTE, "→ remoteTts.speak() returned $ok")
                if (ok) return
            } else {
                Log.d(TAG_ROUTE, "→ skipped (inferSpeakText returned null/blank)")
            }
        }

        // Fallback: системный Android TTS.
        val t = tts ?: return
        if (!_isReady.value) return
        val rate = if (slow) (voiceCfg.rate * 0.7f).coerceIn(0.3f, 2.0f)
                   else voiceCfg.rate.coerceIn(0.3f, 2.0f)
        t.setSpeechRate(rate)

        if (fullMixed) {
            val clean = sanitizeForFullSpeech(text)
            if (clean.isBlank()) return
            speakFullSingleVoice(t, clean)
        } else {
            val speakText = inferSpeakText(text) ?: return
            t.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, "utterance_${System.currentTimeMillis()}")
        }
    }

    /**
     * v1.18.16: очистка текста перед полной озвучкой.
     *  • Убирает все эмодзи (Unicode emoji blocks) — TTS читает их как
     *    «эмодзи лица улыбки», «эмодзи поднятые руки» — раздражает.
     *  • Убирает markdown маркеры **bold** и [перевод-в-скобках].
     *    Скобки оставляют только испанский фрагмент без перевода —
     *    юзер уже видит перевод в чате, читать его второй раз не нужно.
     *  • Сжимает множественные пробелы.
     */
    private fun sanitizeForFullSpeech(raw: String): String {
        var s = raw
        // Markdown **bold** → просто текст без звёздочек
        s = s.replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1")
        // [перевод] — убираем целиком (русский внутри дублирует объяснение)
        s = s.replace(Regex("\\[[^\\]]+\\]"), "")
        // Эмодзи и misc symbols — убираем
        // Unicode blocks: Emoticons, Misc Symbols, Transport, Supplemental, etc.
        s = s.replace(Regex("[\\p{So}\\p{Sk}]"), "")
        // Surrogates (multi-codepoint emoji)
        s = s.replace(Regex("[\\uD800-\\uDFFF]"), "")
        // CORRECTIONS_JSON / PROFILE_UPDATE_JSON tail — safety
        s = s.substringBefore("CORRECTIONS_JSON:").substringBefore("PROFILE_UPDATE_JSON:")
        // Сжать whitespace
        s = s.replace(Regex("\\s+"), " ").trim()
        return s
    }

    /**
     * v1.18.16: читает текст одним русским голосом (всё подряд).
     * Раньше переключал ru/es между сегментами — звучало как разные
     * дикторы, разрывы между фразами. Теперь один голос:
     *  • Русский TTS читает русские слова естественно
     *  • Испанские слова читает с русским акцентом (mejor = «мейор»)
     *  • НЕТ разрывов и переключений
     *
     * Если ru-RU не установлен → fallback на испанский голос.
     */
    private fun speakFullSingleVoice(tts: TextToSpeech, text: String) {
        val ruLocale = java.util.Locale("ru", "RU")
        val ruAvailable = tts.isLanguageAvailable(ruLocale) >= TextToSpeech.LANG_AVAILABLE
        if (ruAvailable) {
            tts.language = ruLocale
        }
        // safety cap чтоб TTS не подавился
        val capped = text.take(1000)
        tts.speak(capped, TextToSpeech.QUEUE_FLUSH, null, "full_${System.currentTimeMillis()}")
    }

    private fun hasCyrillic(text: String): Boolean =
        text.any { it in 'Ѐ'..'ӿ' }

    /**
     * v1.18.15: озвучивает смешанный текст (русский + испанские слова)
     * переключая локаль TTS между сегментами. Поднимает русский для русских
     * частей и испанский для **palabra** / [перевод] / латинских фрагментов.
     *
     * Если ru-RU не установлен на устройстве → русские сегменты молча
     * пропускаются (читается только испанский, как раньше).
     */
    private fun speakBilingual(tts: TextToSpeech, text: String) {
        // Сегментация: подряд кириллица = ru, подряд латиница = es,
        // знаки препинания приклеиваются к соседнему сегменту.
        data class Segment(val text: String, val isSpanish: Boolean)
        val segments = mutableListOf<Segment>()
        val sb = StringBuilder()
        var currentSpanish: Boolean? = null
        for (ch in text) {
            val isLetter = ch.isLetter()
            val isLatin = isLetter && ch !in 'Ѐ'..'ӿ'
            val isCyrillic = isLetter && ch in 'Ѐ'..'ӿ'
            val charSpanish: Boolean? = when {
                isLatin -> true
                isCyrillic -> false
                else -> null  // punct/digit/space → присоединяем к текущему
            }
            if (charSpanish != null && currentSpanish != null && charSpanish != currentSpanish) {
                if (sb.isNotBlank()) segments.add(Segment(sb.toString().trim(), currentSpanish!!))
                sb.clear()
            }
            if (charSpanish != null) currentSpanish = charSpanish
            sb.append(ch)
        }
        if (sb.isNotBlank() && currentSpanish != null) {
            segments.add(Segment(sb.toString().trim(), currentSpanish!!))
        }

        // Чистим — убираем пустые/мусор сегменты
        val clean = segments.filter { seg ->
            val letterCount = seg.text.count { it.isLetter() }
            letterCount >= 1 && seg.text.length <= 500
        }
        if (clean.isEmpty()) return

        val ruLocale = java.util.Locale("ru", "RU")
        val esLocale = java.util.Locale("es", "ES")
        val originalVoice = tts.voice  // сохраним выбранный испанский голос
        val ruAvailable = tts.isLanguageAvailable(ruLocale) >= TextToSpeech.LANG_AVAILABLE

        // Очередь сегментов через QUEUE_ADD + смена локали перед каждым.
        // Сначала FLUSH чтобы прервать предыдущее воспроизведение.
        clean.forEachIndexed { idx, seg ->
            val mode = if (idx == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val id = "mixed_${System.currentTimeMillis()}_$idx"
            if (seg.isSpanish) {
                tts.language = esLocale
                if (originalVoice != null) tts.voice = originalVoice
                tts.speak(seg.text, mode, null, id)
            } else if (ruAvailable) {
                tts.language = ruLocale
                tts.speak(seg.text, mode, null, id)
            }
            // Если ru недоступен — просто пропускаем русский сегмент.
        }
        // После очереди — restore испанский voice/locale для следующего вызова.
        // Делаем через scheduled task — TextToSpeech не имеет «после очереди» callback
        // без UtteranceProgressListener. Простой подход: при следующем speak() мы и так
        // переустанавливаем locale, так что restore тут не критичен.
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

            // v1.18.22: premium TTS если доступно.
            // Ждём завершения через isPlaying StateFlow (см. observer ниже).
            if (remoteTts.isReady.value) {
                val speedMul = if (slow) 0.7f else 1.0f
                remoteTts.speak(speakText, speed = speedMul)
                val observerJob = scope.launch {
                    // Ждём пока isPlaying станет true, потом обратно false
                    var sawPlaying = false
                    remoteTts.isPlaying.collect { playing ->
                        if (playing) sawPlaying = true
                        if (!playing && sawPlaying) {
                            if (cont.isActive) cont.resume(Unit)
                            return@collect
                        }
                    }
                }
                cont.invokeOnCancellation {
                    runCatching { observerJob.cancel() }
                    runCatching { remoteTts.stop() }
                }
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

    fun stop() {
        // v1.18.22: stop работает для обоих движков
        runCatching { remoteTts.stop() }
        runCatching { tts?.stop() }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        _isReady.value = false
    }

    companion object {
        private const val TAG_ROUTE = "SpanishTtsRoute"
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
     * v1.18.4: текущий уровень входного сигнала в dB (приблизительно -2..10).
     * Обновляется через RecognitionListener.onRmsChanged ~10×/сек.
     * UI рисует waveform на основе истории этих значений.
     */
    private val _rmsDb = MutableStateFlow(0f)
    val rmsDb: StateFlow<Float> = _rmsDb

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

        // Защита от двойного вызова на OEM-устройствах: некоторые прошивки
        // отправляют onResults дважды или onResults после onError.
        // Второй cont.resume() кинул бы IllegalStateException → краш.
        val finished = java.util.concurrent.atomic.AtomicBoolean(false)
        fun finishOnce(result: SpeechResult) {
            if (!finished.compareAndSet(false, true)) return
            _isListening.value = false
            runCatching { recognizer.destroy() }
            if (cont.isActive) cont.resume(result)
        }

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
            override fun onRmsChanged(rmsdB: Float) { _rmsDb.value = rmsdB }
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { _isListening.value = false; _rmsDb.value = 0f }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val scores  = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                val result = if (!matches.isNullOrEmpty()) {
                    val alts = matches.mapIndexed { i, t ->
                        t to (scores?.getOrNull(i) ?: 1f)
                    }
                    SpeechResult.Success(alts[0].first, alts[0].second, alts)
                } else {
                    SpeechResult.Error("Не расслышал, попробуй ещё раз")
                }
                finishOnce(result)
            }

            override fun onError(error: Int) {
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
                finishOnce(SpeechResult.Error(msg, isSilence))
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        recognizer.startListening(intent)

        cont.invokeOnCancellation {
            // НЕ зовём cont.resume() — continuation уже cancelled.
            // Просто чистим ресурсы. compareAndSet гарантирует что recognizer.destroy
            // зовётся ровно один раз даже если onError/onResults тоже сработает.
            if (finished.compareAndSet(false, true)) {
                _isListening.value = false
                runCatching {
                    recognizer.cancel()
                    recognizer.destroy()
                }
            }
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
