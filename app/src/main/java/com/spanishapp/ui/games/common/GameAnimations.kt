package com.spanishapp.ui.games.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

// ─────────────────────────────────────────────────────────────
//  GameAnimations.kt  —  общие анимации для всех игр
//
//  Использование в любом игровом экране:
//
//  1. ConfettiEffect — конфетти при правильном ответе
//     ConfettiEffect(trigger = confettiKey)
//
//  2. ComboBadge — пульсирующий бейдж серии
//     ComboBadge(streak = state.streak, accentColor = ACCENT)
//
//  3. ProgressDots — ряд точек прогресса
//     ProgressDots(history = state.answerHistory, total = state.totalRounds, accent = ACCENT)
//
//  4. rememberShakeOffset — тряска карточки при ошибке
//     val shake = rememberShakeOffset(trigger = state.answerHistory.size, isWrong = state.lastCorrect == false)
//     Box(Modifier.offset(x = shake.dp)) { ... }
//
// ─────────────────────────────────────────────────────────────

// ── 1. Конфетти ───────────────────────────────────────────────

private val CONFETTI_COLORS = listOf(
    Color(0xFFFF6B6B), Color(0xFFFFE66D), Color(0xFF4ECDC4),
    Color(0xFF45B7D1), Color(0xFF96CEB4), Color(0xFFFF9F43),
    Color(0xFFA29BFE), Color(0xFFFF6EB4)
)

private data class ConfettiParticle(
    val startX: Float, val color: Color, val size: Float,
    val speed: Float, val wobble: Float, val wobbleSpeed: Float,
    val rotStart: Float, val rotSpeed: Float
)

/**
 * Конфетти сверху вниз. [trigger] — инкрементируй при каждом верном ответе.
 * При trigger == 0 ничего не отображается.
 *
 * Пример:
 * ```
 * var confettiKey by remember { mutableIntStateOf(0) }
 * LaunchedEffect(state.answerHistory.size) {
 *     if (state.lastCorrect == true) confettiKey++
 * }
 * ConfettiEffect(trigger = confettiKey)
 * ```
 */
@Composable
fun ConfettiEffect(trigger: Int) {
    if (trigger == 0) return

    val particles = remember(trigger) {
        List(65) {
            ConfettiParticle(
                startX      = Random.nextFloat(),
                color       = CONFETTI_COLORS[Random.nextInt(CONFETTI_COLORS.size)],
                size        = Random.nextFloat() * 9f + 5f,
                speed       = Random.nextFloat() * 0.45f + 0.40f,
                wobble      = Random.nextFloat() * 90f + 20f,
                wobbleSpeed = Random.nextFloat() * 3f + 1f,
                rotStart    = Random.nextFloat() * 360f,
                rotSpeed    = Random.nextFloat() * 360f - 180f
            )
        }
    }
    var progress by remember(trigger) { mutableFloatStateOf(0f) }
    LaunchedEffect(trigger) {
        val start = System.currentTimeMillis()
        while (progress < 1f) {
            progress = ((System.currentTimeMillis() - start) / 2400f).coerceIn(0f, 1f)
            delay(16)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val t     = (progress / p.speed).coerceIn(0f, 1f)
            val x     = p.startX * size.width + sin(t * p.wobbleSpeed * PI.toFloat()) * p.wobble
            val y     = t * (size.height + 100f) - 60f
            val alpha = if (t > 0.70f) ((1f - t) / 0.30f).coerceIn(0f, 1f) else 1f
            val rot   = p.rotStart + p.rotSpeed * t
            withTransform({
                translate(x, y)
                rotate(rot, pivot = Offset.Zero)
            }) {
                drawRect(
                    color   = p.color.copy(alpha = alpha),
                    topLeft = Offset(-p.size / 2f, -p.size * 0.35f),
                    size    = Size(p.size, p.size * 0.65f)
                )
            }
        }
    }
}

// ── 2. Комбо-бейдж ────────────────────────────────────────────

/**
 * Пульсирующий бейдж серии. Появляется при streak >= 3.
 * [accentColor] — цвет акцента игры (используется если streak < 3, иначе свои цвета).
 *
 * Пример:
 * ```
 * AnimatedVisibility(visible = state.streak >= 3) {
 *     ComboBadge(streak = state.streak)
 * }
 * ```
 */
@Composable
fun ComboBadge(streak: Int, accentColor: Color = Color(0xFFFF5722)) {
    val (emoji, label, bg) = when {
        streak >= 10 -> Triple("🏆", "×$streak CAMPEÓN!", Color(0xFFFFD700))
        streak >= 7  -> Triple("🌟", "×$streak PERFECTO!", Color(0xFFFF6F00))
        streak >= 5  -> Triple("⚡", "×$streak SERIE!", Color(0xFFE91E63))
        streak >= 3  -> Triple("🔥", "×$streak COMBO", accentColor)
        else         -> Triple("🔥", "×$streak", accentColor)
    }
    val inf   = rememberInfiniteTransition(label = "combo_pulse")
    val scale by inf.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.07f,
        animationSpec = infiniteRepeatable(
            tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "combo_scale"
    )
    Surface(
        modifier = Modifier.scale(scale).padding(vertical = 6.dp),
        shape    = RoundedCornerShape(20.dp),
        color    = bg
    ) {
        Text(
            "$emoji $label",
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 7.dp),
            fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White
        )
    }
}

// ── 3. Точки прогресса ────────────────────────────────────────

/**
 * Ряд из [total] точек. Зелёные = верно, красные = ошибка, фиолетовая = текущая, серые = предстоящие.
 *
 * Пример:
 * ```
 * ProgressDots(
 *     history = state.answerHistory,
 *     total   = state.totalRounds,
 *     accent  = ACCENT
 * )
 * ```
 */
@Composable
fun ProgressDots(
    history: List<Boolean>,
    total: Int,
    accent: Color,
    correctColor: Color = Color(0xFF2E7D32),
    wrongColor: Color   = Color(0xFFC62828)
) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        repeat(total) { i ->
            val dotState  = if (i < history.size) history[i] else null
            val isCurrent = i == history.size
            val color = when {
                dotState == true  -> correctColor
                dotState == false -> wrongColor
                isCurrent         -> accent
                else              -> Color(0xFFD1D1D6)
            }
            Box(
                modifier = Modifier
                    .size(if (isCurrent) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

// ── 4. Тряска карточки ────────────────────────────────────────

/**
 * Возвращает горизонтальное смещение (dp) для анимации тряски при ошибке.
 * [trigger] — обычно `state.answerHistory.size`, меняется при каждом ответе.
 * [isWrong] — `state.lastCorrect == false`.
 *
 * Пример:
 * ```
 * val shakeX = rememberShakeOffset(
 *     trigger = state.answerHistory.size,
 *     isWrong = state.lastCorrect == false
 * )
 * Box(Modifier.offset(x = shakeX.dp)) { /* карточка */ }
 * ```
 */
@Composable
fun rememberShakeOffset(trigger: Int, isWrong: Boolean): Float {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (isWrong) {
            anim.animateTo(0f, animationSpec = keyframes {
                durationMillis = 420
                0f   at 0
                -16f at 55
                16f  at 110
                -11f at 165
                11f  at 220
                -6f  at 295
                6f   at 350
                0f   at 420
            })
        }
    }
    return anim.value
}
