package com.spanishapp.ui.components

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spanishapp.R
import com.spanishapp.data.prefs.VoicePreferences
import com.spanishapp.data.prefs.VoiceSettings
import com.spanishapp.service.RemoteTtsService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.util.Locale
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@EntryPoint
@InstallIn(SingletonComponent::class)
private interface VoicePrefsEntryPoint {
    fun voicePreferences(): VoicePreferences
    fun remoteTtsService(): RemoteTtsService
}

private fun voicePreferences(context: Context): VoicePreferences =
    EntryPointAccessors.fromApplication(
        context.applicationContext,
        VoicePrefsEntryPoint::class.java
    ).voicePreferences()

internal fun remoteTtsFrom(context: Context): RemoteTtsService =
    EntryPointAccessors.fromApplication(
        context.applicationContext,
        VoicePrefsEntryPoint::class.java
    ).remoteTtsService()

/**
 * Создаёт TTS, привязанный к жизненному циклу composable, и применяет к нему
 * пользовательские настройки голоса (DataStore). Любое изменение настроек
 * мгновенно отражается на TTS.
 */
@Composable
fun rememberSpanishTts(): TextToSpeech? {
    val context = LocalContext.current
    val ttsState = remember { mutableStateOf<TextToSpeech?>(null) }

    val prefs = remember(context) { voicePreferences(context) }
    val settings by prefs.settings.collectAsStateWithLifecycle(initialValue = VoiceSettings())

    DisposableEffect(Unit) {
        var instance: TextToSpeech? = null
        instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val tts = instance ?: return@TextToSpeech
                // Применяем без retry (sync) — будет retry через LaunchedEffect ниже
                applyVoiceImmediate(tts, settings.selectedVoiceName)
                tts.setSpeechRate(settings.rate)
                tts.setPitch(settings.pitch)
                ttsState.value = tts
            }
        }
        onDispose {
            instance?.stop()
            instance?.shutdown()
            ttsState.value = null
        }
    }

    // v1.18.11: При изменении settings ИЛИ когда TTS только что init'илось —
    // применяем preferred voice с RETRY. Раньше если voice list ещё не
    // загружен на момент init (race condition), fallback на «лучший» давал
    // **разный голос на разных экранах**. Юзер жаловался: «где-то женский,
    // где-то мужской». Теперь до 1.5 сек ждём пока preferred появится.
    LaunchedEffect(settings, ttsState.value) {
        val tts = ttsState.value ?: return@LaunchedEffect
        applyBestVoiceWithRetry(tts, settings.selectedVoiceName)
        tts.setSpeechRate(settings.rate)
        tts.setPitch(settings.pitch)
    }

    return ttsState.value
}

/** Sync (без retry) — для callback из TextToSpeech.onInit. */
private fun applyVoiceImmediate(tts: TextToSpeech, preferredName: String?) {
    val voices = tts.voices ?: emptySet()
    if (preferredName != null) {
        val match = voices.firstOrNull { it.name == preferredName }
        if (match != null) {
            tts.voice = match
            return
        }
    }
    // Никакого fallback здесь — пусть LaunchedEffect retry попробует найти
    // preferred. Если совсем не получится — установит language только.
    tts.language = Locale("es", "ES")
}

/**
 * Async с retry — preferred voice пытаемся применить до 5 раз
 * (1.5 сек total). Это решает проблему когда tts.voices ещё пуст на
 * момент первого вызова — после установки voice pack или на медленных
 * устройствах список загружается с задержкой.
 */
private suspend fun applyBestVoiceWithRetry(tts: TextToSpeech, preferredName: String?) {
    // 1. Пытаемся применить выбранный пользователем голос (с retry).
    if (preferredName != null) {
        repeat(5) {
            val voices = tts.voices ?: emptySet()
            val match = voices.firstOrNull { it.name == preferredName }
            if (match != null) {
                tts.voice = match
                return
            }
            delay(300)
        }
        // Preferred not found after retries — fallthrough на «best available»
    }

    // 2. Fallback — лучший доступный (HD/Neural в приоритете).
    val voices = tts.voices ?: emptySet()
    val spanishVoices = voices.filter { v ->
        v.locale.language == "es" &&
        !v.features.contains(android.speech.tts.TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
    }
    fun isNeural(v: android.speech.tts.Voice): Boolean {
        val n = v.name.lowercase()
        return n.contains("wavenet") || n.contains("neural") ||
               n.contains("network") || n.contains("hd")
    }
    // v1.25.97 FIX (audit M6): канон диалекта — Spain Madrid (es-ES).
    // Раньше сортировка только по качеству: HD-голос es-US/es-MX обгонял
    // стандартный es-ES и ломал канон. Страна ES — первый критерий.
    val best = spanishVoices.sortedWith(
        compareByDescending<android.speech.tts.Voice> { it.locale.country == "ES" }
            .thenByDescending { it.quality }
            .thenByDescending { isNeural(it) }
            .thenBy { it.latency }
    ).firstOrNull()

    if (best != null) {
        tts.voice = best
    } else {
        tts.language = Locale("es", "ES")
    }
}

/**
 * Умный инфер того, что именно нужно передавать в TTS для строки из урока.
 *
 * Три случая:
 *  1. Алфавитная карточка "A a", "Ll ll" → одна уникальная буква → говорим только её
 *  2. Реальное испанское слово/фраза → говорим полностью
 *  3. Русское правило или перевод → null, кнопка не показывается
 */
fun inferSpeakText(text: String?): String? {
    if (text.isNullOrBlank()) return null
    val cleaned = text.replace("_", " ").trim()
    if (cleaned.isEmpty()) return null

    val letters = cleaned.filter { it.isLetter() }
    if (letters.isEmpty()) return null

    val cyrillic = letters.count { it in 'Ѐ'..'ӿ' }
    val latin = letters.count { it !in 'Ѐ'..'ӿ' }

    // Полностью русский текст — не озвучиваем
    if (latin == 0) return null
    // Преимущественно русский (>50%) — не озвучиваем
    if (cyrillic.toDouble() / letters.length > 0.5) return null

    // Алфавитная карточка: одна уникальная буква в разных регистрах ("A a", "Ll ll", "RR rr")
    val uniqueFolded = letters.filter { it !in 'Ѐ'..'ӿ' }.map { it.lowercaseChar() }.toSet()
    if (uniqueFolded.size == 1) {
        // Говорим ровно одну букву — это и есть нужный звук
        return uniqueFolded.first().toString()
    }

    // Реальное испанское слово или фраза
    if (!Regex("[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]{2,}").containsMatchIn(cleaned)) return null

    return sanitizeForTts(cleaned)
}

/** Проверяет, стоит ли показывать кнопку озвучки для данной строки. */
fun isSpanishSpeakable(text: String?): Boolean = inferSpeakText(text) != null

/**
 * Safe wrapper for raw `TextToSpeech.speak()` callsites.
 *
 * Filters through `inferSpeakText()` first so Russian text, blank strings,
 * empty placeholders ("___"), or pronunciation guides ("mu-si-ca") never
 * reach the Spanish engine — those would otherwise be mispronounced or
 * spoken with the wrong language.
 *
 * Returns true if the engine accepted the utterance, false if the text was
 * filtered out (caller can show a fallback / no-op silently).
 */
fun TextToSpeech.speakSpanish(text: String?, utteranceId: String = "spk"): Boolean {
    val cleaned = inferSpeakText(text) ?: return false
    val engine = this
    // v1.18.24: глобальный routing — все 30+ call sites автоматически
    // идут через premium Google Cloud TTS если он зарегистрирован.
    // v1.25.98 (audit tts-H1): при полном сетевом провале remote-сегментов
    // играем через системный движок — offline больше не означает тишину.
    val remoteAccepted = com.spanishapp.service.AppTtsRouter.speakIfReady(
        cleaned,
        onAllFailed = {
            runCatching {
                com.spanishapp.radio.player.RadioCoordinator.pauseForTts()
                // v1.26.1: движок общий с speakRussian — фиксируем локаль
                // перед каждым системным воспроизведением.
                engine.language = java.util.Locale("es", "ES")
                engine.speak(cleaned, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            }
        },
    )
    if (remoteAccepted) return true
    // Fallback: системный Android TTS как раньше.
    com.spanishapp.radio.player.RadioCoordinator.pauseForTts()
    language = java.util.Locale("es", "ES")
    speak(cleaned, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    return true
}

/**
 * v1.26.1: озвучить РУССКИЙ текст (WoD-квиз «Фраза»: юзер слышит перевод на
 * родном языке, потом собирает испанскую фразу).
 *
 * Сначала — ПРЕМИУМ-ДИКТОРЫ через AppTtsRouter: RemoteTtsService сегментирует
 * текст по алфавиту (кириллица → выбранный юзером русский голос из
 * Settings→Голос, с R2/локальным mp3-кэшем — как в AI-чате). Фолбэк —
 * системный движок с ru-RU локалью (offline). Локаль ставится per-call;
 * speakSpanish ставит es-ES — общий движок безопасен.
 */
fun TextToSpeech.speakRussian(text: String?, utteranceId: String = "spk_ru"): Boolean {
    val cleaned = text?.trim()?.takeIf { it.isNotBlank() } ?: return false
    val engine = this
    val remoteAccepted = com.spanishapp.service.AppTtsRouter.speakIfReady(
        cleaned,
        onAllFailed = {
            runCatching {
                com.spanishapp.radio.player.RadioCoordinator.pauseForTts()
                engine.language = java.util.Locale("ru", "RU")
                engine.speak(cleaned, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            }
        },
    )
    if (remoteAccepted) return true
    com.spanishapp.radio.player.RadioCoordinator.pauseForTts()
    language = java.util.Locale("ru", "RU")
    speak(cleaned, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    return true
}

/**
 * v1.18.22: озвучить через premium TTS (Google Cloud) если настроено,
 * иначе через переданный системный TextToSpeech. Все курсы должны идти
 * через premium с выбранным TutorPersonality + полом голоса.
 */
internal fun speakViaPremiumOrFallback(
    context: Context,
    text: String,
    fallbackTts: TextToSpeech?,
    utteranceId: String = "spk_${System.currentTimeMillis()}",
) {
    val cleaned = inferSpeakText(text) ?: return
    val remote = runCatching { remoteTtsFrom(context) }.getOrNull()
    if (remote != null && remote.isReady.value) {
        // v1.25.98 (audit tts-H1): offline-fallback на системный движок.
        remote.speak(cleaned, onAllFailed = {
            runCatching {
                com.spanishapp.radio.player.RadioCoordinator.pauseForTts()
                fallbackTts?.speak(cleaned, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            }
        })
        return
    }
    com.spanishapp.radio.player.RadioCoordinator.pauseForTts()
    fallbackTts?.speak(cleaned, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
}

/** Очищает текст перед озвучкой: убирает плейсхолдеры. */
fun sanitizeForTts(text: String): String =
    text.replace("___", " ")
        .replace(Regex("\\s+"), " ")
        .trim()

/**
 * Wraps a card/row that contains a Spanish word + speaker icon so tapping
 * ANYWHERE on the row triggers TTS — not just the speaker icon. The speaker
 * icon stays visible as a visual cue. Adds light haptic on tap.
 *
 * Usage:
 *     Card(modifier = Modifier.tappableForSpeak { vm.speak(word) }) {
 *         Row { Text(word.spanish); SpeakerIcon() }
 *     }
 *
 * If the row already contains an inner IconButton, both still work — the
 * inner clickable consumes the event when tapped directly.
 */
fun Modifier.tappableForSpeak(onSpeak: () -> Unit): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    this.clickable(
        interactionSource = interaction,
        indication = ripple(bounded = true)
    ) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onSpeak()
    }
}

@Composable
fun SpeakerButton(
    text: String,
    tts: TextToSpeech?,
    tint: Color = Color(0xFF9E9E9E),
    modifier: Modifier = Modifier
) {
    val speakText = remember(text) { inferSpeakText(text) } ?: return

    val context = LocalContext.current
    var speaking by remember { mutableStateOf(false) }

    val iconTint by animateColorAsState(
        targetValue = if (speaking) tint else tint.copy(alpha = 0.5f),
        animationSpec = tween(200),
        label = "tint"
    )

    LaunchedEffect(speaking) {
        if (speaking) {
            delay(600)
            speaking = false
        }
    }

    IconButton(
        onClick = {
            tts?.stop()
            speakViaPremiumOrFallback(context, speakText, tts)
            speaking = true
        },
        modifier = modifier.size(36.dp),
        enabled = tts != null || runCatching { remoteTtsFrom(context).isReady.value }.getOrDefault(false)
    ) {
        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = stringResource(R.string.speaker_button_cd),
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
    }
}
