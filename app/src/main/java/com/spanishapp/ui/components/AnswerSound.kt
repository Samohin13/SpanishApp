package com.spanishapp.ui.components

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

/**
 * Lightweight feedback beeps for correct/wrong answers across all quiz
 * surfaces. Uses ToneGenerator (built-in, no asset files). Volume kept
 * at 60% so it complements TTS without overpowering it.
 */
class AnswerSoundPlayer {
    private val tone: ToneGenerator =
        ToneGenerator(AudioManager.STREAM_MUSIC, 60)

    fun correct() {
        // Short pleasant rising chirp.
        tone.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
    }

    fun wrong() {
        // Lower, quick double-buzz.
        tone.startTone(ToneGenerator.TONE_PROP_NACK, 200)
    }

    fun release() = tone.release()
}

@Composable
fun rememberAnswerSound(): AnswerSoundPlayer {
    val player = remember { AnswerSoundPlayer() }
    DisposableEffect(player) {
        onDispose { player.release() }
    }
    return player
}
