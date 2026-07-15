package com.spanishapp.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp

/**
 * Утверждённые логотипы всех 10 карточек игрового хаба (дизайн-система
 * v3.3, финал 2026-07-15). Перенос 1-в-1 с утверждённой HTML-страницы:
 * та же координатная сетка 100×100, те же градиенты, тени и световой
 * слой плитки. НЕ МЕНЯТЬ геометрию без нового утверждения владельцем.
 *
 * Система: градиентная плитка (скругление 24%) + тёмные фигуры
 * #12161D-семейства + ровно один белый смысловой элемент («твист») +
 * общий свет: блик сверху-слева, виньетка снизу, светлая кромка.
 */

// ── Материалы (hex из утверждённой страницы) ────────────────────
private val D_TOP = Color(0xFF232B3C)
private val D_MID = Color(0xFF141924)
private val D_BOT = Color(0xFF0B0E15)
private val W_TOP = Color(0xFFFFFFFF)
private val W_BOT = Color(0xFFE4E9F2)
private val INK_DARK = Color(0xFF0A0D13)
private val TILE_DARK = Color(0xFF12161D)

// Градиенты плиток по играм (диагональ ~145°, растягивается на размер)
private fun tileBrush(c1: Long, c2: Long, c3: Long) = Brush.linearGradient(
    listOf(Color(c1), Color(c2), Color(c3)),
)

private val BG_FRASE = tileBrush(0xFFFFA35C, 0xFFFF8A3D, 0xFFE86A1C)
private val BG_OIDO = tileBrush(0xFF6FB4FF, 0xFF4EA1FF, 0xFF2B7BDD)
private val BG_ARTICULOS = tileBrush(0xFFB06AF0, 0xFF8B3FD6, 0xFF6A24B8)
private val BG_RAPIDO = tileBrush(0xFFF06AFC, 0xFFE040FB, 0xFFB520D6)
private val BG_SOPA = tileBrush(0xFF7BD98A, 0xFF4CAF50, 0xFF357F3B)
private val BG_PALABRA = tileBrush(0xFFFFD25C, 0xFFFFB400, 0xFFE68F00)
private val BG_CALCULO = tileBrush(0xFFFF7A6E, 0xFFF44336, 0xFFC62828)
private val BG_CRUCI = tileBrush(0xFF5FD6C8, 0xFF26A69A, 0xFF17786E)
private val BG_VERBOS = tileBrush(0xFF64B5F6, 0xFF2196F3, 0xFF1565C0)
private val BG_LIBROS = tileBrush(0xFFD9C24A, 0xFFBEA62F, 0xFF96801E)

// ── Хелперы отрисовки (координаты в юнитах 0..100) ──────────────

private fun DrawScope.u() = size.minDimension / 100f

private fun darkV(u: Float, topU: Float, botU: Float) = Brush.verticalGradient(
    0f to D_TOP, .5f to D_MID, 1f to D_BOT,
    startY = topU * u, endY = botU * u,
)

private fun whiteV(u: Float, topU: Float, botU: Float) = Brush.verticalGradient(
    listOf(W_TOP, W_BOT), startY = topU * u, endY = botU * u,
)

/** Скруглённый прямоугольник в юнитах. */
private fun DrawScope.rectU(
    brush: Brush, x: Float, y: Float, w: Float, h: Float, r: Float,
    alpha: Float = 1f,
) {
    val k = u()
    drawRoundRect(
        brush = brush,
        topLeft = Offset(x * k, y * k),
        size = Size(w * k, h * k),
        cornerRadius = CornerRadius(r * k, r * k),
        alpha = alpha,
    )
}

private fun DrawScope.rectU(
    color: Color, x: Float, y: Float, w: Float, h: Float, r: Float,
    alpha: Float = 1f,
) {
    val k = u()
    drawRoundRect(
        color = color,
        topLeft = Offset(x * k, y * k),
        size = Size(w * k, h * k),
        cornerRadius = CornerRadius(r * k, r * k),
        alpha = alpha,
    )
}

private fun DrawScope.lineU(
    color: Color, x1: Float, y1: Float, x2: Float, y2: Float,
    width: Float, cap: StrokeCap = StrokeCap.Round, alpha: Float = 1f,
) {
    val k = u()
    drawLine(
        color = color,
        start = Offset(x1 * k, y1 * k),
        end = Offset(x2 * k, y2 * k),
        strokeWidth = width * k,
        cap = cap,
        alpha = alpha,
    )
}

private fun DrawScope.circleU(
    brush: Brush, cx: Float, cy: Float, r: Float, alpha: Float = 1f,
) {
    val k = u()
    drawCircle(brush = brush, radius = r * k, center = Offset(cx * k, cy * k), alpha = alpha)
}

private fun DrawScope.circleU(
    color: Color, cx: Float, cy: Float, r: Float, alpha: Float = 1f,
) {
    val k = u()
    drawCircle(color = color, radius = r * k, center = Offset(cx * k, cy * k), alpha = alpha)
}

private fun DrawScope.ellipseU(
    color: Color, cx: Float, cy: Float, rx: Float, ry: Float, alpha: Float = 1f,
) {
    val k = u()
    drawOval(
        color = color,
        topLeft = Offset((cx - rx) * k, (cy - ry) * k),
        size = Size(rx * 2 * k, ry * 2 * k),
        alpha = alpha,
    )
}

private fun DrawScope.ellipseU(
    brush: Brush, cx: Float, cy: Float, rx: Float, ry: Float, alpha: Float = 1f,
) {
    val k = u()
    drawOval(
        brush = brush,
        topLeft = Offset((cx - rx) * k, (cy - ry) * k),
        size = Size(rx * 2 * k, ry * 2 * k),
        alpha = alpha,
    )
}

/** Текст (жирный) с базовой линией как в SVG. rotate — вокруг (x,y). */
private fun DrawScope.textU(
    text: String, x: Float, y: Float, sizeU: Float, color: Color,
    rotateDeg: Float = 0f, alpha: Float = 1f,
) {
    val k = u()
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        textSize = sizeU * k
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.create(
            android.graphics.Typeface.DEFAULT_BOLD, android.graphics.Typeface.BOLD
        )
        this.color = color.copy(alpha = color.alpha * alpha).toArgbCompat()
    }
    drawContext.canvas.nativeCanvas.apply {
        save()
        if (rotateDeg != 0f) rotate(rotateDeg, x * k, y * k)
        drawText(text, x * k, y * k, paint)
        restore()
    }
}

private fun Color.toArgbCompat(): Int = android.graphics.Color.argb(
    (alpha * 255).toInt(), (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt()
)

/** Мягкая тень: смещённая полупрозрачная копия фигуры (без blur). */
private fun DrawScope.shadowRectU(x: Float, y: Float, w: Float, h: Float, r: Float, dy: Float = 2.6f) {
    rectU(Color.Black, x, y + dy, w, h, r, alpha = .28f)
}

/**
 * Световой слой плитки (plate-fx): радиальный блик сверху-слева,
 * виньетка снизу, светлая верхняя кромка. Рисуется ПОСЛЕДНИМ.
 */
private fun DrawScope.plateFx() {
    val k = u()
    // блик
    drawRect(
        brush = Brush.radialGradient(
            0f to Color.White.copy(alpha = .34f),
            .35f to Color.White.copy(alpha = .10f),
            .7f to Color.Transparent,
            center = Offset(28f * k, 12f * k),
            radius = 110f * k,
        ),
        size = size,
    )
    // виньетка
    drawRect(
        brush = Brush.verticalGradient(
            .55f to Color.Transparent,
            1f to Color.Black.copy(alpha = .30f),
            startY = 0f, endY = size.height,
        ),
        size = size,
    )
    // верхняя кромка
    drawRoundRect(
        brush = Brush.verticalGradient(
            0f to Color.White.copy(alpha = .55f),
            .18f to Color.Transparent,
            startY = 0f, endY = size.height,
        ),
        topLeft = Offset(1.2f * k, 1.2f * k),
        size = Size(size.width - 2.4f * k, size.height - 2.4f * k),
        cornerRadius = CornerRadius(22.5f * k, 22.5f * k),
        style = Stroke(width = 2.4f * k),
    )
}

/** Каркас логотипа: плитка-градиент + контент + свет. */
@Composable
private fun LogoTile(
    sizeDp: Dp,
    background: Brush,
    modifier: Modifier = Modifier,
    content: DrawScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(RoundedCornerShape(percent = 24))
            .background(background)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            content()
            plateFx()
        }
    }
}

// ═════════════════════════════════════════════════════════════════
//  1. FRASE LOCA — рассыпанные плитки F / L / !
// ═════════════════════════════════════════════════════════════════
@Composable
fun FraseLocaLogo(size: Dp, modifier: Modifier = Modifier) = LogoTile(size, BG_FRASE, modifier) {
    // F — тёмная, -10°
    rotate(-10f, pivot = Offset(34f * u(), 38f * u())) {
        shadowRectU(20f, 24f, 28f, 28f, 7.5f)
        rectU(darkV(u(), 24f, 52f), 20f, 24f, 28f, 28f, 7.5f)
        rectU(Color.White, 20f, 24f, 28f, 10f, 7.5f, alpha = .07f)
        textU("F", 34f, 44f, 18f, Color.White)
    }
    // L — белая («ловушка»), +7°
    rotate(7f, pivot = Offset(62f * u(), 34f * u())) {
        shadowRectU(48f, 20f, 28f, 28f, 7.5f, dy = 4f)
        rectU(whiteV(u(), 20f, 48f), 48f, 20f, 28f, 28f, 7.5f)
        textU("L", 62f, 40f, 18f, Color(0xFFE86A1C))
    }
    // ! — тёмная, -4°
    rotate(-4f, pivot = Offset(50f * u(), 66f * u())) {
        shadowRectU(36f, 52f, 28f, 28f, 7.5f)
        rectU(darkV(u(), 52f, 80f), 36f, 52f, 28f, 28f, 7.5f)
        rectU(Color.White, 36f, 52f, 28f, 10f, 7.5f, alpha = .07f)
        textU("!", 50f, 72f, 18f, Color.White)
    }
}

// ═════════════════════════════════════════════════════════════════
//  2. EL OÍDO — наушники, белый наушник ловит волны
// ═════════════════════════════════════════════════════════════════
@Composable
fun ElOidoLogo(size: Dp, modifier: Modifier = Modifier) = LogoTile(size, BG_OIDO, modifier) {
    val k = u()
    // дужка
    val band = Path().apply {
        moveTo(24f * k, 58f * k)
        lineTo(24f * k, 50f * k)
        arcTo(Rect(24f * k, 24f * k, 76f * k, 76f * k), 180f, 180f, false)
        lineTo(76f * k, 58f * k)
    }
    drawPath(band, Color(0xFF0D1B2E), style = Stroke(9f * k, cap = StrokeCap.Round))
    // блик на дужке
    val bandHl = Path().apply {
        moveTo(26f * k, 52f * k)
        lineTo(26f * k, 50f * k)
        arcTo(Rect(26f * k, 26f * k, 74f * k, 74f * k), 180f, 180f, false)
    }
    drawPath(bandHl, Color.White.copy(alpha = .18f), style = Stroke(1.6f * k, cap = StrokeCap.Round))
    // тёмный наушник
    shadowRectU(16f, 56f, 17f, 26f, 8f)
    rectU(darkV(u(), 56f, 82f), 16f, 56f, 17f, 26f, 8f)
    // белый наушник (твист)
    shadowRectU(67f, 56f, 17f, 26f, 8f, dy = 4f)
    rectU(whiteV(u(), 56f, 82f), 67f, 56f, 17f, 26f, 8f)
    // волны
    val w1 = Path().apply {
        moveTo(50f * k, 70f * k)
        quadraticBezierTo(56f * k, 70f * k, 56f * k, 64f * k)
    }
    drawPath(w1, Color.White.copy(alpha = .95f), style = Stroke(4.5f * k, cap = StrokeCap.Round))
    val w2 = Path().apply {
        moveTo(50f * k, 79f * k)
        quadraticBezierTo(63f * k, 79f * k, 63f * k, 64f * k)
    }
    drawPath(w2, Color.White.copy(alpha = .6f), style = Stroke(4.5f * k, cap = StrokeCap.Round))
}

// ═════════════════════════════════════════════════════════════════
//  3. ARTÍCULOS — бирки el / la на гвоздике
// ═════════════════════════════════════════════════════════════════
private fun DrawScope.tagPath(apexX: Float, apexY: Float): Path {
    val k = u()
    return Path().apply {
        moveTo(apexX * k, apexY * k)
        lineTo((apexX + 15f) * k, (apexY + 8f) * k)
        lineTo((apexX + 15f) * k, (apexY + 30f) * k)
        arcTo(Rect((apexX + 5f) * k, (apexY + 25f) * k, (apexX + 15f) * k, (apexY + 35f) * k), 0f, 90f, false)
        lineTo((apexX - 10f) * k, (apexY + 35f) * k)
        arcTo(Rect((apexX - 15f) * k, (apexY + 25f) * k, (apexX - 5f) * k, (apexY + 35f) * k), 90f, 90f, false)
        lineTo((apexX - 15f) * k, (apexY + 8f) * k)
        close()
    }
}

@Composable
fun ArticulosLogo(size: Dp, modifier: Modifier = Modifier) = LogoTile(size, BG_ARTICULOS, modifier) {
    val k = u()
    // гвоздик
    circleU(
        Brush.radialGradient(
            listOf(Color(0xFF2C3547), Color(0xFF161B26), Color(0xFF0A0D13)),
            center = Offset(48.6f * k, 13.6f * k), radius = 6f * k,
        ),
        50f, 15f, 4.6f,
    )
    circleU(Color.White, 48.6f, 13.6f, 1.3f, alpha = .55f)
    // нити
    lineU(TILE_DARK, 50f, 17f, 36.5f, 39f, 2.6f)
    lineU(TILE_DARK, 50f, 17f, 63.5f, 40.5f, 2.6f)
    // бирка el — тёмная, -12°
    rotate(-12f, pivot = Offset(33f * k, 56f * k)) {
        drawPath(tagPath(33f, 38f), Color.Black, alpha = .25f)
        withTransform({ translate(0f, -2.4f * k) }) {
            drawPath(tagPath(33f, 40.4f), darkV(u(), 38f, 73f))
        }
        circleU(Color(0xFF0B0E13), 33f, 44.5f, 2.6f)
        textU("el", 33f, 66f, 13.5f, Color.White)
    }
    // бирка la — белая, +11°
    rotate(11f, pivot = Offset(67f * k, 58f * k)) {
        drawPath(tagPath(67f, 40f), Color.Black, alpha = .22f)
        withTransform({ translate(0f, -2.6f * k) }) {
            drawPath(tagPath(67f, 42.6f), whiteV(u(), 40f, 75f))
        }
        circleU(Color(0xFFB9C2D4), 67f, 46.5f, 2.6f)
        textU("la", 67f, 68f, 13.5f, Color(0xFF6A24B8))
    }
}

// ═════════════════════════════════════════════════════════════════
//  4. RÁPIDO — бумажный самолётик с белой петлёй следа
// ═════════════════════════════════════════════════════════════════
@Composable
fun RapidoLogo(size: Dp, modifier: Modifier = Modifier) = LogoTile(size, BG_RAPIDO, modifier) {
    val k = u()
    // белый след-петля
    val trail = Path().apply {
        moveTo(10f * k, 86f * k)
        cubicTo(28f * k, 88f * k, 36f * k, 78f * k, 28f * k, 71f * k)
        cubicTo(20f * k, 64f * k, 12f * k, 76f * k, 27f * k, 78f * k)
        cubicTo(46f * k, 81f * k, 52f * k, 68f * k, 58f * k, 60f * k)
    }
    drawPath(trail, Color.White.copy(alpha = .95f), style = Stroke(5f * k, cap = StrokeCap.Round))
    // штрихи скорости
    lineU(Color.White, 64f, 54f, 69f, 49f, 2.4f, alpha = .4f)
    lineU(Color.White, 70f, 60f, 76f, 54f, 2.4f, alpha = .4f)
    // дротик
    val upper = Path().apply {
        moveTo(88f * k, 12f * k); lineTo(28f * k, 44f * k); lineTo(52f * k, 52f * k); close()
    }
    val lower = Path().apply {
        moveTo(88f * k, 12f * k); lineTo(52f * k, 52f * k); lineTo(58f * k, 74f * k); close()
    }
    val facet = Path().apply {
        moveTo(88f * k, 12f * k); lineTo(28f * k, 44f * k); lineTo(38f * k, 47.3f * k); close()
    }
    // тень под дротиком
    withTransform({ translate(0f, 2.6f * k) }) {
        drawPath(upper, Color.Black, alpha = .18f)
        drawPath(lower, Color.Black, alpha = .18f)
    }
    drawPath(upper, darkV(u(), 12f, 52f))
    drawPath(lower, Color(0xFF0A0D13))
    drawPath(facet, Color.White, alpha = .10f)
    lineU(Color.White, 88f, 12f, 52f, 52f, 2f, alpha = .4f)
}

// ═════════════════════════════════════════════════════════════════
//  5. SOPA DE LETRAS — суп из букв
// ═════════════════════════════════════════════════════════════════
@Composable
fun SopaLogo(size: Dp, modifier: Modifier = Modifier) = LogoTile(size, BG_SOPA, modifier) {
    val k = u()
    // пар
    val steam1 = Path().apply {
        moveTo(36f * k, 18f * k)
        cubicTo(32f * k, 23f * k, 40f * k, 25f * k, 36f * k, 30f * k)
    }
    drawPath(steam1, Color.White.copy(alpha = .5f), style = Stroke(4f * k, cap = StrokeCap.Round))
    val steam2 = Path().apply {
        moveTo(60f * k, 14f * k)
        cubicTo(56f * k, 19f * k, 64f * k, 21f * k, 60f * k, 26f * k)
    }
    drawPath(steam2, Color.White.copy(alpha = .3f), style = Stroke(4f * k, cap = StrokeCap.Round))
    // найденная буква
    textU("O", 50f, 38f, 24f, Color.White, rotateDeg = 5f)
    // ручки
    shadowRectU(8f, 55f, 12f, 7f, 3.5f)
    rectU(darkV(u(), 55f, 62f), 8f, 55f, 12f, 7f, 3.5f)
    shadowRectU(80f, 55f, 12f, 7f, 3.5f)
    rectU(darkV(u(), 55f, 62f), 80f, 55f, 12f, 7f, 3.5f)
    // тарелка: обод + нижняя чаша
    ellipseU(Color.Black, 50f, 60.6f, 33f, 8.5f, alpha = .2f)
    ellipseU(darkV(u(), 49.5f, 66.5f), 50f, 58f, 33f, 8.5f)
    val bowl = Path().apply {
        moveTo(17f * k, 58f * k)
        arcTo(Rect(17f * k, 31f * k, 83f * k, 85f * k), 180f, -180f, false)
        close()
    }
    drawPath(bowl, darkV(u(), 58f, 85f))
    // поверхность супа
    ellipseU(Color(0xFF232C3C), 50f, 58f, 27.5f, 6f)
    val surfHl = Path().apply {
        moveTo(26f * k, 56.6f * k)
        quadraticBezierTo(36f * k, 52.4f * k, 48f * k, 53.2f * k)
    }
    drawPath(surfHl, Color.White.copy(alpha = .22f), style = Stroke(2f * k, cap = StrokeCap.Round))
    // буквы, тонущие в супе
    textU("s", 34f, 61f, 14f, Color(0xFF0B0E13), rotateDeg = -10f)
    textU("p", 65f, 62f, 14f, Color(0xFF0B0E13), rotateDeg = 9f)
    // блик на боку
    val sideHl = Path().apply {
        moveTo(24f * k, 68f * k)
        quadraticBezierTo(28f * k, 76f * k, 40f * k, 79f * k)
    }
    drawPath(sideHl, Color.White.copy(alpha = .10f), style = Stroke(2.4f * k, cap = StrokeCap.Round))
    // ножка
    lineU(Color(0xFF0A0D13), 34f, 91f, 66f, 91f, 6f)
}

// ═════════════════════════════════════════════════════════════════
//  6. PALABRA MAESTRA — лупа поймала букву A
// ═════════════════════════════════════════════════════════════════
@Composable
fun PalabraLogo(size: Dp, modifier: Modifier = Modifier) = LogoTile(size, BG_PALABRA, modifier) {
    val k = u()
    // рассыпанные буквы (вне фокуса)
    textU("b", 20f, 26f, 13f, TILE_DARK, rotateDeg = -14f, alpha = .5f)
    textU("r", 78f, 30f, 12f, TILE_DARK, rotateDeg = 10f, alpha = .5f)
    textU("s", 85f, 62f, 13f, TILE_DARK, alpha = .5f)
    // ручка
    lineU(Color.Black, 61f, 63.6f, 84f, 86.6f, 12f, alpha = .22f)
    drawLine(
        brush = darkV(u(), 59f, 86f),
        start = Offset(61f * k, 61f * k), end = Offset(84f * k, 84f * k),
        strokeWidth = 12f * k, cap = StrokeCap.Round,
    )
    lineU(Color.White, 62f, 59.6f, 83f, 80.6f, 2f, alpha = .14f)
    // затемнение под стеклом (контраст для A)
    circleU(Color(0xFF0B0E13), 42f, 42f, 23f, alpha = .30f)
    // стекло
    circleU(
        Brush.radialGradient(
            0f to Color.White.copy(alpha = .38f),
            .45f to Color.White.copy(alpha = .10f),
            1f to Color.White.copy(alpha = .03f),
            center = Offset((42f - 8f) * k, (42f - 10f) * k),
            radius = 40f * k,
        ),
        42f, 42f, 27f,
    )
    // оправа
    val kk = u()
    drawCircle(
        brush = darkV(u(), 15f, 69f),
        radius = 27f * kk,
        center = Offset(42f * kk, 42f * kk),
        style = Stroke(8.5f * kk),
    )
    // дуга-отражение
    val shine = Path().apply {
        moveTo(25f * k, 30f * k)
        quadraticBezierTo(28f * k, 24f * k, 37f * k, 22f * k)
    }
    drawPath(shine, Color.White.copy(alpha = .45f), style = Stroke(3.4f * k, cap = StrokeCap.Round))
    // A — резкая, без свечения, с тонкой тенью
    textU("A", 42.6f, 52.9f, 27f, Color.Black, alpha = .35f)
    textU("A", 42f, 52f, 27f, Color.White)
}

// ═════════════════════════════════════════════════════════════════
//  7. CÁLCULO — счёты, белая костяшка-ответ
// ═════════════════════════════════════════════════════════════════
@Composable
fun CalculoLogo(size: Dp, modifier: Modifier = Modifier) = LogoTile(size, BG_CALCULO, modifier) {
    val k = u()
    // рамка
    drawRoundRect(
        color = Color.Black,
        topLeft = Offset(15f * k, 19.6f * k),
        size = Size(70f * k, 66f * k),
        cornerRadius = CornerRadius(14f * k, 14f * k),
        style = Stroke(7.5f * k),
        alpha = .22f,
    )
    drawRoundRect(
        brush = darkV(u(), 17f, 83f),
        topLeft = Offset(15f * k, 17f * k),
        size = Size(70f * k, 66f * k),
        cornerRadius = CornerRadius(14f * k, 14f * k),
        style = Stroke(7.5f * k),
    )
    rectU(Color.White, 17.6f, 19.4f, 64.8f, 4f, 2f, alpha = .10f)
    // прутья
    lineU(Color(0xFF0E1218), 19f, 36f, 81f, 36f, 4f, cap = StrokeCap.Butt)
    lineU(Color(0xFF0E1218), 19f, 53f, 81f, 53f, 4f, cap = StrokeCap.Butt)
    lineU(Color(0xFF0E1218), 19f, 70f, 81f, 70f, 4f, cap = StrokeCap.Butt)
    // костяшки
    fun bead(cx: Float, cy: Float, r: Float = 6.5f) {
        circleU(Color.Black, cx, cy + 1.8f, r, alpha = .25f)
        circleU(
            Brush.radialGradient(
                listOf(Color(0xFF2C3547), Color(0xFF161B26), Color(0xFF0A0D13)),
                center = Offset((cx - r * .3f) * k, (cy - r * .35f) * k),
                radius = r * 2f * k,
            ),
            cx, cy, r,
        )
    }
    bead(30f, 36f); bead(45f, 36f); bead(72f, 36f)
    bead(28f, 53f); bead(73f, 53f)
    bead(30f, 70f); bead(45f, 70f); bead(60f, 70f)
    // белая костяшка-ответ
    circleU(Color.Black, 57f, 55f, 7.5f, alpha = .28f)
    circleU(
        Brush.radialGradient(
            listOf(Color.White, Color(0xFFEDF1F7), Color(0xFFC9D2E0)),
            center = Offset(54.6f * k, 50.4f * k),
            radius = 15f * k,
        ),
        57f, 53f, 7.5f,
    )
    circleU(Color.White, 54.6f, 50.4f, 2f)
}

// ═════════════════════════════════════════════════════════════════
//  8. CRUCIGRAMA — карандаш вписывает Ñ в белую клетку
// ═════════════════════════════════════════════════════════════════
@Composable
fun CrucigramaLogo(size: Dp, modifier: Modifier = Modifier) = LogoTile(size, BG_CRUCI, modifier) {
    val k = u()
    // клетки
    shadowRectU(13f, 44f, 26f, 26f, 6.5f)
    rectU(darkV(u(), 44f, 70f), 13f, 44f, 26f, 26f, 6.5f)
    rectU(Color.White, 13f, 44f, 26f, 9f, 6.5f, alpha = .06f)
    rectU(darkV(u(), 72f, 98f), 41f, 72f, 26f, 26f, 6.5f, alpha = .5f)
    // белая клетка с Ñ
    shadowRectU(41f, 44f, 26f, 26f, 6.5f, dy = 4f)
    rectU(whiteV(u(), 44f, 70f), 41f, 44f, 26f, 26f, 6.5f)
    textU("Ñ", 54f, 63f, 17f, Color(0xFF17786E))
    // карандаш: собран горизонтально, повёрнут -45° вокруг (62,40)
    rotate(-45f, pivot = Offset(62f * k, 40f * k)) {
        // тень
        rectU(Color.Black, 78f, 36.1f, 22f, 13f, 0f, alpha = .2f)
        // грифель
        val tip = Path().apply {
            moveTo(62f * k, 40f * k); lineTo(69f * k, 36.8f * k); lineTo(69f * k, 43.2f * k); close()
        }
        drawPath(tip, Color(0xFF0A0D13))
        // дерево
        val wood = Path().apply {
            moveTo(69f * k, 36.8f * k); lineTo(78f * k, 33.5f * k)
            lineTo(78f * k, 46.5f * k); lineTo(69f * k, 43.2f * k); close()
        }
        drawPath(wood, Color(0xFFE8C9A0))
        val woodHl = Path().apply {
            moveTo(69f * k, 36.8f * k); lineTo(78f * k, 33.5f * k)
            lineTo(78f * k, 37f * k); lineTo(69f * k, 39f * k); close()
        }
        drawPath(woodHl, Color.White, alpha = .25f)
        // корпус
        rectU(darkV(u(), 33.5f, 46.5f), 78f, 33.5f, 22f, 13f, 0f)
        rectU(Color.White, 78f, 34.8f, 22f, 2.4f, 0f, alpha = .16f)
        rectU(Color.Black, 78f, 44f, 22f, 2.5f, 0f, alpha = .28f)
        // кольцо + ластик
        rectU(Color(0xFF8F99AD), 100f, 33.5f, 4.5f, 13f, 0f)
        rectU(whiteV(u(), 33.5f, 46.5f), 104.5f, 33.5f, 7f, 13f, 3.4f)
    }
}

// ═════════════════════════════════════════════════════════════════
//  9. VERBOS — гантель спряжений ar · er · ir
// ═════════════════════════════════════════════════════════════════
@Composable
fun VerbosLogo(size: Dp, modifier: Modifier = Modifier) = LogoTile(size, BG_VERBOS, modifier) {
    // тень-пол
    ellipseU(Color.Black, 50f, 79f, 32f, 4f, alpha = .25f)
    // гриф
    rectU(darkV(u(), 46f, 55f), 35f, 46f, 30f, 9f, 4.5f)
    rectU(Color.White, 36.5f, 47.3f, 27f, 2f, 1f, alpha = .15f)
    // замки
    rectU(Color(0xFF0A0D13), 29f, 41f, 6f, 19f, 2.5f)
    rectU(Color(0xFF0A0D13), 65f, 41f, 6f, 19f, 2.5f)
    // внутренние блины
    shadowRectU(19f, 31f, 10f, 39f, 4f)
    rectU(darkV(u(), 31f, 70f), 19f, 31f, 10f, 39f, 4f)
    shadowRectU(71f, 31f, 10f, 39f, 4f)
    rectU(darkV(u(), 31f, 70f), 71f, 31f, 10f, 39f, 4f)
    // внешние блины: левый тёмный, правый БЕЛЫЙ (твист)
    shadowRectU(9f, 37f, 10f, 27f, 4f)
    rectU(darkV(u(), 37f, 64f), 9f, 37f, 10f, 27f, 4f)
    shadowRectU(81f, 37f, 10f, 27f, 4f, dy = 3.4f)
    rectU(whiteV(u(), 37f, 64f), 81f, 37f, 10f, 27f, 4f)
    // гравировка
    textU("ar · er · ir", 50f, 70f, 8f, Color(0xFF0A0D13), alpha = .55f)
}

// ═════════════════════════════════════════════════════════════════
//  10. LIBROS — книга с белой закладкой
// ═════════════════════════════════════════════════════════════════
@Composable
fun LibrosLogo(size: Dp, modifier: Modifier = Modifier) = LogoTile(size, BG_LIBROS, modifier) {
    val k = u()
    // тень-пол
    ellipseU(Color.Black, 50f, 86f, 26f, 3.6f, alpha = .28f)
    // блок страниц
    rectU(Color(0xFF4A5568), 70f, 22f, 6f, 60f, 2f)
    lineU(Color(0xFF65758C), 72f, 25f, 72f, 79f, 1f, cap = StrokeCap.Butt)
    lineU(Color(0xFF65758C), 74f, 25f, 74f, 79f, 1f, cap = StrokeCap.Butt)
    // обложка
    shadowRectU(26f, 18f, 46f, 66f, 6f, dy = 3.4f)
    rectU(darkV(u(), 18f, 84f), 26f, 18f, 46f, 66f, 6f)
    rectU(Color.White, 26f, 18f, 46f, 10f, 6f, alpha = .06f)
    // корешок
    rectU(Color(0xFF0A0D13), 26f, 18f, 9f, 66f, 4f)
    lineU(Color.White, 35f, 22f, 35f, 80f, 1.2f, alpha = .08f, cap = StrokeCap.Butt)
    // тиснёные строки заголовка
    rectU(Color.White, 42f, 32f, 22f, 4.6f, 2.3f, alpha = .16f)
    rectU(Color.White, 42f, 40f, 14f, 4.6f, 2.3f, alpha = .10f)
    // белая закладка (твист)
    val ribbonShadow = Path().apply {
        moveTo(54f * k, 20.6f * k); lineTo(64f * k, 20.6f * k); lineTo(64f * k, 50.6f * k)
        lineTo(59f * k, 44.6f * k); lineTo(54f * k, 50.6f * k); close()
    }
    drawPath(ribbonShadow, Color.Black, alpha = .22f)
    val ribbon = Path().apply {
        moveTo(54f * k, 18f * k); lineTo(64f * k, 18f * k); lineTo(64f * k, 48f * k)
        lineTo(59f * k, 42f * k); lineTo(54f * k, 48f * k); close()
    }
    drawPath(ribbon, whiteV(u(), 18f, 48f))
}
