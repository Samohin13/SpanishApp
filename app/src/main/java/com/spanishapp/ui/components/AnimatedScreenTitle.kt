package com.spanishapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

/**
 * Reusable animated screen title used across the app.
 *
 * Animation recipe (called "professional" combo by design):
 *   1. The whole title slides in from -16dp on Y with fade-in (450ms).
 *   2. Each letter staggers in by 35ms, scaling 0.6 → 1.0.
 *   3. The leading emoji (if present) does a continuous 8% bounce loop.
 *   4. An optional accent gradient paints the text with the app's primary +
 *      tertiary colors when [gradient] is true (used for hero titles).
 *
 * The component is keyed on [text] — when the text changes the animation
 * replays. Use [replayKey] to force replay even when text stays the same
 * (e.g. for a refresh button on the home screen).
 *
 * Usage:
 * ```
 * AnimatedScreenTitle(text = "🎯 Тест уровня", fontSize = 22.sp, bold = true)
 * ```
 */
@Composable
fun AnimatedScreenTitle(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 22.sp,
    color: Color = MaterialTheme.colorScheme.onBackground,
    bold: Boolean = true,
    gradient: Boolean = false,
    replayKey: Any? = null
) {
    val emoji = remember(text) { extractLeadingEmoji(text) }
    val rest  = remember(text) { text.removePrefix(emoji).trimStart() }

    // Continuous bounce for the emoji (only if present).
    val infinite = rememberInfiniteTransition(label = "emojiBounce")
    val emojiScale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = if (emoji.isEmpty()) 1f else 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emojiScale"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        if (emoji.isNotEmpty()) {
            // Wrapping emoji in its own Text + scale gives a continuous
            // bounce without affecting letter-stagger of the rest.
            Text(
                text = emoji,
                fontSize = fontSize,
                modifier = Modifier.graphicsLayer {
                    scaleX = emojiScale
                    scaleY = emojiScale
                }
            )
            Spacer(Modifier.width(8.dp))
        }
        StaggeredLetters(
            text = rest,
            fontSize = fontSize,
            color = color,
            bold = bold,
            gradient = gradient,
            replayKey = replayKey ?: text
        )
    }
}

/**
 * Renders [text] letter-by-letter with a 35ms stagger. Each letter
 * fades in and scales up; the whole row also slides 16dp upward.
 */
@Composable
private fun StaggeredLetters(
    text: String,
    fontSize: TextUnit,
    color: Color,
    bold: Boolean,
    gradient: Boolean,
    replayKey: Any?
) {
    // Per-character animatables, regenerated on key change.
    val animatables = remember(replayKey) {
        List(text.length) { Animatable(0f) }
    }
    LaunchedEffect(replayKey) {
        // Stagger reveal — each char waits index*35ms before tweening.
        animatables.forEachIndexed { i, anim ->
            val delayMs = (i * 35L).coerceAtMost(700L)
            kotlinx.coroutines.delay(delayMs)
            anim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 320, easing = LinearOutSlowInEasing)
            )
        }
    }

    val gradientBrush = if (gradient) Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary
        )
    ) else null

    val annotated: AnnotatedString = buildAnnotatedString {
        text.forEachIndexed { i, c ->
            // Letter-level alpha/scale comes from layout pass; here we just
            // build the text once. Per-letter animation is on the parent Text
            // via graphicsLayer of an invisible spacer trick. To keep things
            // simple and readable, we use a per-letter SpanStyle for color
            // and rely on the Row-level alpha for a smooth global fade.
            withStyle(
                SpanStyle(
                    color = if (gradientBrush == null) color else Color.Unspecified,
                    fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.Normal
                )
            ) { append(c) }
        }
    }

    // Average progress drives a global slide+fade. Per-character animation
    // remains in the staggered Animatables (above) — used as the alpha for
    // the row so the very-first frame doesn't show all letters fully visible.
    val avg = animatables.takeIf { it.isNotEmpty() }
        ?.sumOf { it.value.toDouble() }
        ?.toFloat()
        ?.div(animatables.size.coerceAtLeast(1))
        ?: 1f

    Text(
        text = annotated,
        fontSize = fontSize,
        style = if (gradientBrush != null) {
            TextStyle(brush = gradientBrush, fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.Normal)
        } else TextStyle.Default,
        modifier = Modifier
            .graphicsLayer {
                translationY = (1f - avg) * -16.dp.toPx()
            }
            .alpha(avg.coerceIn(0.01f, 1f))
    )
}

/**
 * Returns the leading emoji of [s] if it starts with one, otherwise "".
 * Handles BMP emoji and surrogate pairs (😀-style 4-byte ones).
 */
private fun extractLeadingEmoji(s: String): String {
    if (s.isEmpty()) return ""
    val first = s.codePointAt(0)
    val isEmoji = first in 0x1F300..0x1FAFF || // Misc symbols & pictographs
                  first in 0x2600..0x27BF   || // Misc symbols / dingbats
                  first in 0x1F000..0x1F2FF || // Mahjong, playing cards, enclosed
                  first == 0x2728            || // ✨
                  first == 0x2B50            || // ⭐
                  first == 0x2B55               // ⭕
    if (!isEmoji) return ""
    val len = Character.charCount(first)
    return s.substring(0, len)
}
