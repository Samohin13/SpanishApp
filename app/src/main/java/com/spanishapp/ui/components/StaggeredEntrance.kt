package com.spanishapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * Wraps [content] in a staggered slide-in + fade-in animation.
 *
 * Tuning notes (after user feedback that Flashcards felt jumpy):
 *  - 50ms stagger (was 60ms) so the cascade ends sooner overall.
 *  - 420ms slide / 380ms fade with EaseOutCubic — gentler tail than
 *    FastOutSlowInEasing, no perceptible "snap" at the end.
 *  - Initial offset reduced from `it / 3` to `it / 8` — cards drift up
 *    a small amount instead of leaping a third of their height. This
 *    is the main reason tall set-rows looked twitchy.
 */
@Composable
fun StaggeredEntrance(
    index: Int,
    staggerMillis: Long = 50L,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(staggerMillis * index)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = tween(durationMillis = 420, easing = EaseOutCubic),
            initialOffsetY = { it / 8 }
        ) + fadeIn(animationSpec = tween(durationMillis = 380, easing = EaseOutCubic))
    ) {
        content()
    }
}
