package com.spanishapp.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Утверждённые логотипы игр Frase Loca и El Oído (2026-07-14).
 * Вектор 1-в-1 с макетом: те же градиенты, цвета и композиция.
 *  • Frase Loca — оранжевый градиент + три плитки-буквы F/L/! под углами,
 *    одна плитка белая-перевёрнутая (намёк на ловушку).
 *  • El Oído — голубой градиент + наушники, один «наушник» белый и от
 *    него идут звуковые волны.
 * Масштабируются под любой размер через BoxWithConstraints.
 */

// Палитра логотипов (hex из утверждённого макета)
private val FRASE_GRADIENT = Brush.linearGradient(
    listOf(Color(0xFFFFA35C), Color(0xFFFF8A3D), Color(0xFFE86A1C))
)
private val OIDO_GRADIENT = Brush.linearGradient(
    listOf(Color(0xFF6FB4FF), Color(0xFF4EA1FF), Color(0xFF2B7BDD))
)
private val TILE_DARK = Color(0xFF12161D)
private val OIDO_DARK = Color(0xFF0D1B2E)

/** Логотип Frase Loca. [size] — сторона квадрата иконки. */
@Composable
fun FraseLocaLogo(size: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    val corner = size * 0.24f
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(FRASE_GRADIENT),
        contentAlignment = Alignment.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            val tile = size * 0.34f
            val tileCorner = RoundedCornerShape(tile * 0.24f)
            val fontSize = with(androidx.compose.ui.platform.LocalDensity.current) {
                (tile * 0.55f).toSp()
            }

            // Плитка F — тёмная, наклон влево
            Box(
                modifier = Modifier
                    .offset(x = -tile * 0.62f, y = -tile * 0.28f)
                    .size(tile)
                    .rotate(-10f)
                    .clip(tileCorner)
                    .background(TILE_DARK),
                contentAlignment = Alignment.Center
            ) {
                Text("F", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = fontSize)
            }
            // Плитка L — белая («ловушка»), наклон вправо
            Box(
                modifier = Modifier
                    .offset(x = tile * 0.34f, y = -tile * 0.45f)
                    .size(tile)
                    .rotate(7f)
                    .clip(tileCorner)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("L", color = Color(0xFFE86A1C), fontWeight = FontWeight.ExtraBold, fontSize = fontSize)
            }
            // Плитка ! — тёмная, лёгкий наклон
            Box(
                modifier = Modifier
                    .offset(x = -tile * 0.10f, y = tile * 0.52f)
                    .size(tile)
                    .rotate(-4f)
                    .clip(tileCorner)
                    .background(TILE_DARK),
                contentAlignment = Alignment.Center
            ) {
                Text("!", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = fontSize)
            }
        }
    }
}

/** Логотип El Oído. [size] — сторона квадрата иконки. */
@Composable
fun ElOidoLogo(size: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    val corner = size * 0.24f
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(OIDO_GRADIENT),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val stroke = w * 0.09f

            // Дужка наушников (тёмная)
            val arc = Path().apply {
                moveTo(w * 0.20f, h * 0.60f)
                lineTo(w * 0.20f, h * 0.52f)
                cubicTo(
                    w * 0.20f, h * 0.24f,
                    w * 0.80f, h * 0.24f,
                    w * 0.80f, h * 0.52f,
                )
                lineTo(w * 0.80f, h * 0.60f)
            }
            drawPath(arc, OIDO_DARK, style = Stroke(width = stroke, cap = StrokeCap.Round))

            // Левый «наушник» — тёмный
            drawRoundRect(
                color = OIDO_DARK,
                topLeft = Offset(w * 0.12f, h * 0.57f),
                size = Size(w * 0.18f, h * 0.28f),
                cornerRadius = CornerRadius(w * 0.09f, w * 0.09f),
            )
            // Правый «наушник» — белый (звук приходит сюда)
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(w * 0.70f, h * 0.57f),
                size = Size(w * 0.18f, h * 0.28f),
                cornerRadius = CornerRadius(w * 0.09f, w * 0.09f),
            )

            // Звуковые волны от белого наушника
            val waveStroke = w * 0.045f
            val wave1 = Path().apply {
                moveTo(w * 0.50f, h * 0.71f)
                quadraticTo(w * 0.56f, h * 0.71f, w * 0.56f, h * 0.65f)
            }
            drawPath(wave1, Color.White.copy(alpha = 0.9f), style = Stroke(waveStroke, cap = StrokeCap.Round))
            val wave2 = Path().apply {
                moveTo(w * 0.50f, h * 0.80f)
                quadraticTo(w * 0.635f, h * 0.80f, w * 0.635f, h * 0.65f)
            }
            drawPath(wave2, Color.White.copy(alpha = 0.55f), style = Stroke(waveStroke, cap = StrokeCap.Round))
        }
    }
}
