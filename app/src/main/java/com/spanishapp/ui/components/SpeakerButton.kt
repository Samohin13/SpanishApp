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
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.util.Locale

@EntryPoint
@InstallIn(SingletonComponent::class)
private interface VoicePrefsEntryPoint {
    fun voicePreferences(): VoicePreferences
}

private fun voicePreferences(context: Context): VoicePreferences =
    EntryPointAccessors.fromApplication(
        context.applicationContext,
        VoicePrefsEntryPoint::class.java
    ).voicePreferences()

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
    val settings by prefs.settings.collectAsState(initial = VoiceSettings())

    DisposableEffect(Unit) {
        var instance: TextToSpeech? = null
        instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val tts = instance ?: return@TextToSpeech
                applyBestVoice(tts, settings.selectedVoiceName)
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

    // При изменении настроек — переприменяем
    LaunchedEffect(settings) {
        ttsState.value?.let { tts ->
            applyBestVoice(tts, settings.selectedVoiceName)
            tts.setSpeechRate(settings.rate)
            tts.setPitch(settings.pitch)
        }
    }

    return ttsState.value
}

private fun applyBestVoice(tts: TextToSpeech, preferredName: String?) {
    val voices = tts.voices ?: emptySet()

    // 1. Пытаемся применить выбранный пользователем голос
    if (preferredName != null) {
        val match = voices.firstOrNull { it.name == preferredName }
        if (match != null) {
            tts.voice = match
            return
        }
    }

    // 2. Иначе — лучший доступный (HD/Neural в приоритете)
    val spanishVoices = voices.filter { v ->
        v.locale.language == "es" &&
        !v.features.contains(android.speech.tts.TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
    }
    fun isNeural(v: android.speech.tts.Voice): Boolean {
        val n = v.name.lowercase()
        return n.contains("wavenet") || n.contains("neural") ||
               n.contains("network") || n.contains("hd")
    }
    val best = spanishVoices.sortedWith(
        compareByDescending<android.speech.tts.Voice> { it.quality }
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
            tts?.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, "speak_${System.currentTimeMillis()}")
            speaking = true
        },
        modifier = modifier.size(36.dp),
        enabled = tts != null
    ) {
        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = stringResource(R.string.speaker_button_cd),
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
    }
}
