package com.spanishapp.ui.components

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
 * Определяет, стоит ли озвучивать строку испанским TTS.
 *  • Минимум 2 латинские буквы подряд (отсекает "a", "—", "?", "1", "___")
 *  • Без кириллицы (это перевод на русский — не для es-TTS)
 */
fun isSpanishSpeakable(text: String?): Boolean {
    if (text.isNullOrBlank()) return false
    val cleaned = text.replace("_", " ").trim()
    if (cleaned.isEmpty()) return false
    if (cleaned.any { it in 'Ѐ'..'ӿ' }) return false
    return Regex("[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]{2,}").containsMatchIn(cleaned)
}

/** Очищает текст перед озвучкой: убирает плейсхолдеры. */
fun sanitizeForTts(text: String): String =
    text.replace("___", " ")
        .replace(Regex("\\s+"), " ")
        .trim()

@Composable
fun SpeakerButton(
    text: String,
    tts: TextToSpeech?,
    tint: Color = Color(0xFF9E9E9E),
    modifier: Modifier = Modifier
) {
    if (!isSpanishSpeakable(text)) return

    val cleanText = remember(text) { sanitizeForTts(text) }
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
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "speak_${System.currentTimeMillis()}")
            speaking = true
        },
        modifier = modifier.size(36.dp),
        enabled = tts != null
    ) {
        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "Произнести",
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
    }
}
