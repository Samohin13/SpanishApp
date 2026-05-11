package com.spanishapp.ui.onboarding

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import com.spanishapp.R

/**
 * Plays a looped background Spanish-flavoured music track while the
 * DownloadScreen is on screen.
 *
 * Track file: `app/src/main/res/raw/download_loop.mp3` — owner-supplied
 * royalty-free flamenco/Spanish guitar loop. If the file is missing the
 * function silently no-ops (no crash, no error).
 *
 * Usage:
 *   @Composable
 *   fun Screen() { rememberDownloadMusic() }
 */
@Composable
fun rememberDownloadMusic() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val player = startLoopPlayer(context)
        onDispose { player?.runCatching { stop(); release() } }
    }
}

private fun startLoopPlayer(context: Context): MediaPlayer? {
    val resId = context.resources
        .getIdentifier("download_loop", "raw", context.packageName)
    if (resId == 0) return null   // file not bundled — silent
    return runCatching {
        MediaPlayer.create(context, resId)?.apply {
            isLooping = true
            setVolume(0.55f, 0.55f)
            start()
        }
    }.getOrNull()
}
