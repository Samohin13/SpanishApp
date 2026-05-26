package com.spanishapp.domain.chat

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

/**
 * v1.18.38: каталог фонов чата в испанском стиле.
 *
 * Каждый wallpaper = градиент (Brush) + векторный паттерн (DrawScope).
 * Паттерн тайлится по экрану, чисто SVG-стиль без bitmap'ов.
 * Для дальнейшей кастомизации добавляй новые ChatWallpaper в [ALL].
 */
data class ChatWallpaper(
    val id: String,
    val displayName: String,
    val description: String,
    val gradient: Brush,
    /** Размер тайла в логических пикселях (px = dp * density). */
    val tileSize: Float = 160f,
    /** Рисует один тайл паттерна в координатах [0, tileSize]^2. */
    val drawTile: DrawScope.(scale: Float) -> Unit,
)

object ChatWallpapers {

    // ─── 1. Atardecer — закат с солнцем и облаками ──────────────────
    val ATARDECER = ChatWallpaper(
        id = "atardecer",
        displayName = "Atardecer",
        description = "Закат — солнце и облака",
        gradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFD89B),
                Color(0xFFFFA07A),
                Color(0xFFFF6B6B),
                Color(0xFFC44569),
            ),
        ),
        drawTile = { s ->
            val white = Color.White.copy(alpha = 0.22f)
            // sun
            drawCircle(white, 6f * s, Offset(25f * s, 25f * s))
            // sun rays
            for (i in 0..7) {
                val angle = (i * PI / 4).toFloat()
                val r1 = 9f * s
                val r2 = 13f * s
                drawLine(
                    white,
                    Offset(25f * s + cos(angle) * r1, 25f * s + sin(angle) * r1),
                    Offset(25f * s + cos(angle) * r2, 25f * s + sin(angle) * r2),
                    strokeWidth = 1.5f * s,
                )
            }
            // cloud 1 @ (110, 45)
            drawCloud(this, 110f * s, 45f * s, 18f * s, white)
            // cloud 2 @ (50, 95)
            drawCloud(this, 50f * s, 95f * s, 16f * s, white)
            // cloud 3 @ (115, 120)
            drawCloud(this, 115f * s, 120f * s, 14f * s, white)
        }
    )

    // ─── 2. Azulejos Noche — мавританские плитки ────────────────────
    val AZULEJOS = ChatWallpaper(
        id = "azulejos",
        displayName = "Azulejos Noche",
        description = "Темно-синий — андалусские плитки",
        gradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFF0F2027),
                Color(0xFF203A43),
                Color(0xFF2C5364),
            ),
        ),
        tileSize = 100f,
        drawTile = { s ->
            val accent = Color(0xFFA4D4ED).copy(alpha = 0.35f)
            // diamond 1
            drawDiamond(this, 50f * s, 32f * s, 12f * s, accent, strokeWidth = 1.2f * s)
            drawCircle(accent, 3f * s, Offset(50f * s, 32f * s))
            // diamond 2
            drawDiamond(this, 32f * s, 65f * s, 12f * s, accent, strokeWidth = 1.2f * s)
            drawCircle(accent, 3f * s, Offset(32f * s, 65f * s))
            // diamond 3
            drawDiamond(this, 82f * s, 80f * s, 12f * s, accent, strokeWidth = 1.2f * s)
        }
    )

    // ─── 3. Corazón Flamenco — сердца и веера ───────────────────────
    val CORAZON = ChatWallpaper(
        id = "corazon",
        displayName = "Corazón Flamenco",
        description = "Бордо — сердца и веера",
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF200122), Color(0xFF6F0000)),
        ),
        tileSize = 140f,
        drawTile = { s ->
            val pink = Color(0xFFFFB4B4).copy(alpha = 0.30f)
            val light = Color(0xFFFFD4D4).copy(alpha = 0.30f)
            drawHeart(this, 30f * s, 35f * s, 14f * s, pink)
            drawHeart(this, 110f * s, 95f * s, 16f * s, pink)
            drawFan(this, 100f * s, 50f * s, 18f * s, light)
            drawFan(this, 40f * s, 100f * s, 16f * s, light)
        }
    )

    // ─── 4. Mediterráneo Verde — оливки, лимоны, виноград ───────────
    val MEDITERRANEO = ChatWallpaper(
        id = "mediterraneo",
        displayName = "Mediterráneo Verde",
        description = "Зелёный — оливки и лимоны",
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF134E5E), Color(0xFF71B280)),
        ),
        tileSize = 140f,
        drawTile = { s ->
            val olive = Color(0xFFD4E8C8).copy(alpha = 0.40f)
            val lemon = Color(0xFFFFE88A).copy(alpha = 0.40f)
            val leaf = Color(0xFFC4D4A8).copy(alpha = 0.40f)
            val grape = Color(0xFFD4A888).copy(alpha = 0.40f)
            // olive @ (25, 30)
            drawEllipse(this, 25f * s, 30f * s, 10f * s, 4f * s, olive, -30f)
            drawEllipse(this, 32f * s, 35f * s, 8f * s, 3f * s, olive, -30f)
            // lemon @ (100, 50)
            drawEllipse(this, 100f * s, 50f * s, 9f * s, 6f * s, lemon, 0f)
            // leaf @ (40, 95)
            drawEllipse(this, 40f * s, 95f * s, 11f * s, 4f * s, leaf, 20f)
            drawEllipse(this, 48f * s, 100f * s, 9f * s, 3f * s, leaf, 20f)
            // grape cluster @ (105, 115)
            drawCircle(grape, 5f * s, Offset(105f * s, 110f * s))
            drawCircle(grape, 4f * s, Offset(112f * s, 115f * s))
            drawCircle(grape, 4f * s, Offset(108f * s, 118f * s))
        }
    )

    // ─── 5. Alhambra Noche — золотые звёзды ─────────────────────────
    val ALHAMBRA = ChatWallpaper(
        id = "alhambra",
        displayName = "Alhambra Noche",
        description = "Фиолет — золотые звёзды",
        gradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFF0F0C29),
                Color(0xFF302B63),
                Color(0xFF24243E),
            ),
        ),
        tileSize = 120f,
        drawTile = { s ->
            val gold = Color(0xFFD4AF37).copy(alpha = 0.55f)
            drawEightStar(this, 35f * s, 35f * s, 15f * s, gold, strokeWidth = 1f * s)
            drawEightStar(this, 95f * s, 95f * s, 15f * s, gold, strokeWidth = 1f * s)
            drawCircle(
                color = gold,
                radius = 10f * s,
                center = Offset(95f * s, 30f * s),
                style = Stroke(width = 1f * s),
            )
            drawCircle(
                color = gold,
                radius = 10f * s,
                center = Offset(30f * s, 95f * s),
                style = Stroke(width = 1f * s),
            )
        }
    )

    // ─── 6. Fiesta — гитары, барабаны ───────────────────────────────
    val FIESTA = ChatWallpaper(
        id = "fiesta",
        displayName = "Fiesta",
        description = "Жёлтый — гитары и барабаны",
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFFFFA751), Color(0xFFFFE259)),
        ),
        tileSize = 160f,
        drawTile = { s ->
            val brown = Color(0xFF8B4513).copy(alpha = 0.30f)
            drawGuitar(this, 30f * s, 45f * s, s, brown)
            drawDrum(this, 110f * s, 50f * s, 15f * s, brown, strokeWidth = 1.5f * s)
            drawMaraca(this, 40f * s, 115f * s, s, brown)
        }
    )

    // ─── 7. Costa Brava — волны, рыбы ───────────────────────────────
    val COSTA = ChatWallpaper(
        id = "costa",
        displayName = "Costa Brava",
        description = "Бирюза — волны и рыбки",
        gradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFF2E86AB),
                Color(0xFF6CB4D4),
                Color(0xFFB8E0E8),
            ),
        ),
        tileSize = 140f,
        drawTile = { s ->
            val white = Color.White.copy(alpha = 0.35f)
            drawWave(this, 10f * s, 30f * s, 40f * s, white, strokeWidth = 1.3f * s)
            drawWave(this, 55f * s, 30f * s, 40f * s, white, strokeWidth = 1.3f * s)
            drawFish(this, 30f * s, 95f * s, s, white)
            drawBoat(this, 95f * s, 110f * s, s, white)
        }
    )

    // ─── 8. Sevilla Rosa — розы, веера, бутоны ──────────────────────
    val SEVILLA = ChatWallpaper(
        id = "sevilla",
        displayName = "Sevilla Rosa",
        description = "Розовый — розы и веера",
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFFF2709C), Color(0xFFFF9472)),
        ),
        tileSize = 140f,
        drawTile = { s ->
            val rose = Color(0xFFC44569).copy(alpha = 0.40f)
            val fan = Color(0xFFFFC4D4).copy(alpha = 0.35f)
            val bud = Color(0xFFFF8AA8).copy(alpha = 0.40f)
            drawRose(this, 30f * s, 35f * s, 8f * s, rose)
            drawFan(this, 100f * s, 55f * s, 14f * s, fan)
            drawBud(this, 45f * s, 100f * s, s, bud)
            drawBud(this, 105f * s, 110f * s, s, bud)
        }
    )

    // v1.23.27: Conversa — теплый paper-фон в стиле Conversa mockup.
    // Cream базовый, мягкие радиальные подсветки terracotta+ochre.
    // Дефолт для редизайна "как в Telegram но в paper-стиле".
    val CONVERSA = ChatWallpaper(
        id = "conversa",
        displayName = "Conversa",
        description = "Тёплая paper-бумага",
        gradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFFF4EAD5),  // cream
                Color(0xFFEDE0C4),  // cream-soft
            ),
        ),
        drawTile = { s ->
            // Мягкие точки-зёрнышки (paper texture)
            val grain = Color(0xFF2B1E14).copy(alpha = 0.025f)
            for (i in 0..15) {
                val x = (i * 11 % 130).toFloat() * s
                val y = (i * 17 % 130).toFloat() * s
                drawCircle(grain, 0.8f * s, Offset(x, y))
            }
        }
    )

    // v1.23.6: «Стандартный» обой — чисто Material-фон без градиента
    // и паттерна. Юзеру по умолчанию хотел этот — потом сам сменит на
    // декоративный через wallpaper picker.
    val STANDARD = ChatWallpaper(
        id = "standard",
        displayName = "Стандартный",
        description = "Без декора",
        gradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFF0E0E12),  // bg
                Color(0xFF0E0E12),
            ),
        ),
        drawTile = { /* пустой — без паттерна */ },
    )

    val ALL = listOf(
        CONVERSA, STANDARD, ATARDECER, AZULEJOS, CORAZON, MEDITERRANEO,
        ALHAMBRA, FIESTA, COSTA, SEVILLA,
    )

    // v1.23.27: дефолт = Conversa (paper-эстетика, как просил юзер).
    const val DEFAULT_ID = "conversa"

    fun byId(id: String?): ChatWallpaper =
        ALL.firstOrNull { it.id == id } ?: ALL.first()
}

// ─── Хелперы для рисования примитивов ────────────────────────────────

private fun drawCloud(
    scope: DrawScope,
    cx: Float, cy: Float, radius: Float, color: Color,
) = with(scope) {
    val p = Path().apply {
        moveTo(cx - radius * 1.4f, cy)
        cubicTo(cx - radius * 1.6f, cy - radius * 0.6f,
                cx - radius * 0.4f, cy - radius * 1.0f,
                cx, cy - radius * 0.4f)
        cubicTo(cx + radius * 0.4f, cy - radius * 1.2f,
                cx + radius * 1.6f, cy - radius * 0.6f,
                cx + radius * 1.4f, cy)
        cubicTo(cx + radius * 1.8f, cy + radius * 0.4f,
                cx - radius * 1.8f, cy + radius * 0.4f,
                cx - radius * 1.4f, cy)
        close()
    }
    drawPath(p, color)
}

private fun drawDiamond(
    scope: DrawScope,
    cx: Float, cy: Float, size: Float, color: Color, strokeWidth: Float,
) = with(scope) {
    val p = Path().apply {
        moveTo(cx, cy - size)
        lineTo(cx + size * 0.7f, cy)
        lineTo(cx, cy + size)
        lineTo(cx - size * 0.7f, cy)
        close()
    }
    drawPath(p, color, style = Stroke(width = strokeWidth))
}

private fun drawHeart(
    scope: DrawScope,
    cx: Float, cy: Float, size: Float, color: Color,
) = with(scope) {
    val p = Path().apply {
        moveTo(cx, cy + size * 0.3f)
        cubicTo(cx - size * 1.2f, cy - size * 0.8f,
                cx - size * 0.6f, cy - size * 1.5f, cx, cy - size * 0.5f)
        cubicTo(cx + size * 0.6f, cy - size * 1.5f,
                cx + size * 1.2f, cy - size * 0.8f, cx, cy + size * 0.3f)
        close()
    }
    drawPath(p, color)
}

private fun drawFan(
    scope: DrawScope,
    cx: Float, cy: Float, size: Float, color: Color,
) = with(scope) {
    // 5 spokes from pivot at bottom
    val pivot = Offset(cx, cy + size * 0.8f)
    for (i in 0..4) {
        val angle = (-80f + i * 40f) * (PI / 180).toFloat()
        drawLine(
            color = color,
            start = pivot,
            end = Offset(
                pivot.x + sin(angle) * size * 1.3f,
                pivot.y - cos(angle) * size * 1.3f
            ),
            strokeWidth = 1.2f,
        )
    }
    // arc on top
    val arcPath = Path().apply {
        moveTo(pivot.x - size * 1.3f, pivot.y - size * 0.3f)
        quadraticBezierTo(pivot.x, pivot.y - size * 1.6f, pivot.x + size * 1.3f, pivot.y - size * 0.3f)
    }
    drawPath(arcPath, color, style = Stroke(width = 1.2f))
}

private fun drawEllipse(
    scope: DrawScope,
    cx: Float, cy: Float, rx: Float, ry: Float, color: Color, rotation: Float,
) = with(scope) {
    rotate(rotation, pivot = Offset(cx, cy)) {
        drawOval(
            color = color,
            topLeft = Offset(cx - rx, cy - ry),
            size = Size(rx * 2, ry * 2),
        )
    }
}

private fun drawEightStar(
    scope: DrawScope,
    cx: Float, cy: Float, radius: Float, color: Color, strokeWidth: Float,
) = with(scope) {
    val outerR = radius
    val innerR = radius * 0.42f
    val p = Path()
    for (i in 0..15) {
        val angle = (i * PI / 8 - PI / 2).toFloat()
        val r = if (i % 2 == 0) outerR else innerR
        val x = cx + cos(angle) * r
        val y = cy + sin(angle) * r
        if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
    }
    p.close()
    drawPath(p, color, style = Stroke(width = strokeWidth))
}

private fun drawGuitar(
    scope: DrawScope, cx: Float, cy: Float, s: Float, color: Color,
) = with(scope) {
    // body (ellipse)
    drawOval(
        color = color,
        topLeft = Offset(cx - 12f * s, cy - 15f * s),
        size = Size(24f * s, 30f * s),
        style = Stroke(width = 1.5f * s),
    )
    // hole
    drawCircle(color, 3f * s, Offset(cx, cy))
    // neck
    drawLine(color, Offset(cx, cy + 15f * s), Offset(cx, cy + 40f * s), strokeWidth = 2f * s)
    // head
    drawRect(
        color = color,
        topLeft = Offset(cx - 3f * s, cy + 40f * s),
        size = Size(6f * s, 14f * s),
        style = Stroke(width = 1.5f * s),
    )
}

private fun drawDrum(
    scope: DrawScope, cx: Float, cy: Float, radius: Float, color: Color, strokeWidth: Float,
) = with(scope) {
    drawCircle(color, radius, Offset(cx, cy), style = Stroke(width = strokeWidth))
    drawCircle(color, radius * 0.7f, Offset(cx, cy), style = Stroke(width = strokeWidth))
    drawLine(color, Offset(cx - radius, cy - radius * 0.5f), Offset(cx + radius, cy - radius * 0.5f), strokeWidth)
    drawLine(color, Offset(cx - radius, cy + radius * 0.5f), Offset(cx + radius, cy + radius * 0.5f), strokeWidth)
}

private fun drawMaraca(
    scope: DrawScope, cx: Float, cy: Float, s: Float, color: Color,
) = with(scope) {
    drawOval(
        color = color,
        topLeft = Offset(cx - 8f * s, cy - 10f * s),
        size = Size(16f * s, 20f * s),
        style = Stroke(width = 1.5f * s),
    )
    drawLine(color, Offset(cx, cy + 10f * s), Offset(cx, cy + 20f * s), strokeWidth = 1.5f * s)
    drawLine(color, Offset(cx - 3f * s, cy + 20f * s), Offset(cx + 3f * s, cy + 20f * s), strokeWidth = 1.5f * s)
}

private fun drawWave(
    scope: DrawScope, x0: Float, y: Float, width: Float, color: Color, strokeWidth: Float,
) = with(scope) {
    val p = Path().apply {
        moveTo(x0, y)
        quadraticBezierTo(x0 + width * 0.25f, y - width * 0.15f, x0 + width * 0.5f, y)
        quadraticBezierTo(x0 + width * 0.75f, y + width * 0.15f, x0 + width, y)
    }
    drawPath(p, color, style = Stroke(width = strokeWidth))
}

private fun drawFish(
    scope: DrawScope, cx: Float, cy: Float, s: Float, color: Color,
) = with(scope) {
    val p = Path().apply {
        moveTo(cx, cy)
        quadraticBezierTo(cx + 8f * s, cy - 4f * s, cx + 16f * s, cy)
        quadraticBezierTo(cx + 8f * s, cy + 4f * s, cx, cy)
        close()
    }
    drawPath(p, color)
    // tail
    val tail = Path().apply {
        moveTo(cx + 16f * s, cy)
        lineTo(cx + 22f * s, cy - 4f * s)
        lineTo(cx + 22f * s, cy + 4f * s)
        close()
    }
    drawPath(tail, color)
    drawCircle(Color.White.copy(alpha = 0.7f), 1.2f * s, Offset(cx + 4f * s, cy - 1f * s))
}

private fun drawBoat(
    scope: DrawScope, cx: Float, cy: Float, s: Float, color: Color,
) = with(scope) {
    // hull
    val hull = Path().apply {
        moveTo(cx - 12f * s, cy + 4f * s)
        lineTo(cx + 12f * s, cy + 4f * s)
        lineTo(cx + 8f * s, cy + 12f * s)
        lineTo(cx - 8f * s, cy + 12f * s)
        close()
    }
    drawPath(hull, color, style = Stroke(width = 1.3f * s))
    // mast
    drawLine(color, Offset(cx, cy + 4f * s), Offset(cx, cy - 8f * s), strokeWidth = 1.3f * s)
    // sail
    val sail = Path().apply {
        moveTo(cx, cy + 2f * s)
        lineTo(cx + 8f * s, cy + 2f * s)
        lineTo(cx, cy - 7f * s)
        close()
    }
    drawPath(sail, color, style = Stroke(width = 1.3f * s))
}

private fun drawRose(
    scope: DrawScope, cx: Float, cy: Float, size: Float, color: Color,
) = with(scope) {
    drawCircle(color, size * 0.5f, Offset(cx, cy))
    // 4 small petals
    for (i in 0..3) {
        val angle = (i * 90f + 45f) * (PI / 180).toFloat()
        val px = cx + cos(angle) * size * 0.6f
        val py = cy + sin(angle) * size * 0.6f
        drawCircle(color, size * 0.35f, Offset(px, py))
    }
}

private fun drawBud(
    scope: DrawScope, cx: Float, cy: Float, s: Float, color: Color,
) = with(scope) {
    drawOval(
        color = color,
        topLeft = Offset(cx - 4f * s, cy - 6f * s),
        size = Size(8f * s, 12f * s),
    )
    drawLine(
        color = Color(0xFFA4744A).copy(alpha = 0.4f),
        start = Offset(cx, cy + 6f * s),
        end = Offset(cx, cy + 12f * s),
        strokeWidth = 1f * s,
    )
}
