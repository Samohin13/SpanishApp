package com.spanishapp.ui.components

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
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun rememberSpanishTts(): TextToSpeech? {
    val context = LocalContext.current
    val ttsState = remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(Unit) {
        var instance: TextToSpeech? = null
        instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val tts = instance ?: return@TextToSpeech

                // Выбираем лучший испанский голос из доступных:
                // сортируем по качеству (выше = лучше), потом по латентности (меньше = лучше)
                val bestVoice = tts.voices
                    ?.filter { voice ->
                        voice.locale.language == "es" &&
                        !voice.isNetworkConnectionRequired &&
                        !voice.features.contains(android.speech.tts.TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
                    }
                    ?.sortedWith(compareByDescending<android.speech.tts.Voice> { it.quality }
                        .thenBy { it.latency })
                    ?.firstOrNull()

                if (bestVoice != null) {
                    tts.voice = bestVoice
                } else {
                    // Fallback: просто испанский язык
                    tts.language = Locale("es", "ES")
                }

                tts.setSpeechRate(0.82f)   // чуть медленнее для ясности
                tts.setPitch(1.0f)
                ttsState.value = tts
            }
        }
        onDispose {
            instance?.stop()
            instance?.shutdown()
            ttsState.value = null
        }
    }
    return ttsState.value
}

@Composable
fun SpeakerButton(
    text: String,
    tts: TextToSpeech?,
    tint: Color = Color(0xFF9E9E9E),
    modifier: Modifier = Modifier
) {
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
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "speak_${System.currentTimeMillis()}")
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
