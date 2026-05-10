package com.spanishapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
 * Use inside any LazyColumn/Column where multiple children should
 * cascade onto the screen on first composition.
 *
 * @param index 0-based position used to compute the per-item delay.
 * @param staggerMillis ms between each child's entrance (default 60ms).
 */
@Composable
fun StaggeredEntrance(
    index: Int,
    staggerMillis: Long = 60L,
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
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
            initialOffsetY = { it / 3 }
        ) + fadeIn(animationSpec = tween(300))
    ) {
        content()
    }
}
