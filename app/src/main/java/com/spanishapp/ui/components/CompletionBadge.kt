package com.spanishapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Achievement medal shown on session-completion screens.
 *
 * Replaces the generic Lottie trophy that felt redundant with the bronze/
 * silver/gold cup tiers used in the flashcard set list. Renders entirely in
 * Compose Canvas — no asset cost — and tints the medal by accuracy:
 *   90-100%: gold, 70-89%: silver, 50-69%: bronze, <50%: steel.
 *
 * Animation: medal scale-in with overshoot spring, ribbon slide-in from below
 * after a short delay, stars stagger-reveal one by one.
 */
@Composable
fun CompletionBadge(
    accuracyPercent: Int,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    label: String = "¡COMPLETADO!",
    // v1.26.1 FIX (audit): «достоинство финала» — флэшкарты скрывают ленту
    // ниже 100% и глушат золото при <50% (0% сессия выглядела как праздник).
    // Дефолты сохраняют прежнее поведение остальных экранов (LessonSession).
    showRibbon: Boolean = true,
    mutedWhenLow: Boolean = false,
) {
    val acc = accuracyPercent.coerceIn(0, 100)
    // v1.26.1 FIX (audit): приглушённый тон темы вместо «медали» при провале.
    val muted = mutedWhenLow && acc < 50

    // Tier colors based on accuracy. Each pair = (lighter, darker) for the
    // inner gradient fill — keeps medals visually distinct at a glance.
    val (innerStart, innerEnd) = when {
        acc >= 90 -> Color(0xFFFFD700) to Color(0xFFFFA500) // Gold
        acc >= 70 -> Color(0xFFC0C0C0) to Color(0xFF808080) // Silver
        acc >= 50 -> Color(0xFFCD7F32) to Color(0xFF8B4513) // Bronze
        muted     -> MaterialTheme.colorScheme.surfaceContainerHighest to
                     MaterialTheme.colorScheme.surfaceContainerHighest // Muted (<50%)
        else      -> Color(0xFF778899) to Color(0xFF2F4F4F) // Steel
    }

    // v1.26.1 FIX (audit): кольцо и текст — не золотые, когда финал приглушён.
    val ringColors =
        if (muted) {
            val c = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
            listOf(c, c)
        } else {
            listOf(Color(0xFFFFD700), Color(0xFFB8860B))
        }
    val contentColor = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else Color.White

    // Star count: 4 thresholds — 4 = perfect, 3 = great, 2 = ok, 1 = passed,
    // 0 = struggling. Matches the four tiers above.
    val starCount = when {
        acc >= 90 -> 4
        acc >= 70 -> 3
        acc >= 50 -> 2
        acc >= 25 -> 1
        else      -> 0
    }
    val starColor = if (acc >= 90) Color(0xFFFFD700) else contentColor

    // Medal scale-in with overshoot.
    val medalScale = remember { Animatable(0f) }
    // Star reveal flags — flip true one-by-one with stagger delay.
    val starsRevealed = remember { mutableStateOf(0) }
    var ribbonVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch {
            medalScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            delay(200)
            ribbonVisible = true
        }
        launch {
            // Stagger stars after the medal lands.
            delay(350)
            for (i in 1..4) {
                starsRevealed.value = i
                delay(80)
            }
        }
    }

    val ribbonColor = MaterialTheme.colorScheme.primary

    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        if (acc == 100) {
            Confetti100Burst(modifier = Modifier.fillMaxSize())
        }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Medal itself ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = medalScale.value
                    scaleY = medalScale.value
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = this.size.width
                val h = this.size.height
                val center = Offset(w / 2f, h / 2f)
                val outerRadius = (minOf(w, h) / 2f) - 2.dp.toPx()
                val ringWidth = 8.dp.toPx()
                val innerRadius = outerRadius - ringWidth / 2f - 2.dp.toPx()

                // Inner gradient fill (the medal "face").
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(innerStart, innerEnd),
                        center = center,
                        radius = innerRadius
                    ),
                    radius = innerRadius,
                    center = center
                )

                // Outer ring — golden by default, muted tone при провале (v1.26.1).
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = ringColors,
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    ),
                    radius = outerRadius - ringWidth / 2f,
                    center = center,
                    style = Stroke(width = ringWidth)
                )
            }

            // Percent + stars overlay.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${acc}%",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(4) { idx ->
                        val filled = idx < starCount && idx < starsRevealed.value
                        Text(
                            text = if (filled) "★" else "☆",
                            fontSize = 16.sp,
                            color = if (filled) starColor else contentColor.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }

        // ── Ribbon banner ──────────────────────────────────────────
        // Slight negative offset so the ribbon visually overlaps the bottom of
        // the medal — classic "медаль на ленте" look.
        // v1.26.1 FIX (audit): вызывающий может скрыть ленту (флэшкарты — только 100%).
        AnimatedVisibility(
            visible = ribbonVisible && showRibbon,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 350)
            ) + fadeIn(animationSpec = tween(350))
        ) {
            Box(
                modifier = Modifier
                    .offset(y = (-18).dp)
                    .widthIn(min = 140.dp)
            ) {
                RibbonShape(color = ribbonColor) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
    }
}

/**
 * Rectangular banner with two triangular notches cut from the bottom edges —
 * the classic ribbon shape for award badges. Content is laid on top, the
 * Canvas behind paints the shape.
 */
@Composable
private fun RibbonShape(
    color: Color,
    content: @Composable () -> Unit
) {
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = this.size.width
            val h = this.size.height
            val notchW = h * 0.45f
            val notchH = h * 0.32f
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h)
                // Right notch (triangle cut going up-and-left).
                lineTo(w - notchW, h - notchH)
                lineTo(w - notchW * 2f, h)
                // Left mirror.
                lineTo(notchW * 2f, h)
                lineTo(notchW, h - notchH)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(color, color.copy(alpha = 0.85f)),
                    startY = 0f,
                    endY = h
                )
            )
        }
        content()
    }
}

@Composable
private fun Confetti100Burst(modifier: Modifier = Modifier) {
    val pieces = remember {
        List(60) {
            ConfettiPiece(
                startX = (Math.random() * 2 - 1).toFloat(),
                startY = -1f,
                color = listOf(
                    Color(0xFFFFD700), Color(0xFFFF6B6B), Color(0xFF4ECDC4),
                    Color(0xFF95E1D3), Color(0xFFC56CF0), Color(0xFFFFA45B)
                ).random(),
                horizDrift = (Math.random() * 0.6 - 0.3).toFloat(),
                fallSpeed = (0.6 + Math.random() * 0.4).toFloat(),
                rotationSpeed = (Math.random() * 720 - 360).toFloat(),
                shape = if (Math.random() > 0.5) ConfettiShape.RECT else ConfettiShape.CIRCLE
            )
        }
    }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(2400, easing = LinearOutSlowInEasing))
    }
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        pieces.forEach { p ->
            val t = progress.value
            val x = w / 2f + (p.startX * w / 2f) + (p.horizDrift * w * t)
            val y = -h * 0.3f + (h * 1.4f * t * p.fallSpeed)
            val rot = p.rotationSpeed * t
            rotate(rot, pivot = Offset(x, y)) {
                when (p.shape) {
                    ConfettiShape.RECT -> drawRect(
                        color = p.color.copy(alpha = (1f - t * 0.4f).coerceAtLeast(0.3f)),
                        topLeft = Offset(x - 6f, y - 3f),
                        size = Size(12f, 6f)
                    )
                    ConfettiShape.CIRCLE -> drawCircle(
                        color = p.color.copy(alpha = (1f - t * 0.4f).coerceAtLeast(0.3f)),
                        radius = 5f,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}

private enum class ConfettiShape { RECT, CIRCLE }
private data class ConfettiPiece(
    val startX: Float, val startY: Float,
    val color: Color,
    val horizDrift: Float, val fallSpeed: Float, val rotationSpeed: Float,
    val shape: ConfettiShape
)
