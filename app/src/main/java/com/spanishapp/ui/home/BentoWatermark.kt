package com.spanishapp.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Thematic background watermark.
 *
 * Originally lived only inside the home-screen bento tiles (hence the legacy
 * `BentoTheme` / `BentoWatermark` aliases below). Session 9 expanded the
 * system to:
 *   • Continue Pager cards (LESSON / FLASHCARD_SET / WEAK_WORD)
 *   • GameCard list (8 game-specific motifs)
 *   • Course block headers (4 motifs picked by block index)
 *
 * Each theme draws a single, tile-appropriate vector illustration anchored
 * to the bottom-right corner at ~55% width and ~75% height, painted with
 * the tile's accent colour at very low alpha (0.10-0.14) so it reads as a
 * subtle decorative texture rather than a foreground element.
 */
enum class WatermarkTheme {
    // Bento (legacy)
    BOOK, RATING, DICTIONARY, GOAL,

    // Continue Pager
    LESSON, FLASHCARD_SET, WEAK_WORD,

    // Games
    GAME_ARTICLES, GAME_SPEED, GAME_VERBS, GAME_SOPA,
    GAME_PALABRA, GAME_MATH, GAME_CROSSWORD, GAME_LIBROS,

    // Course blocks
    BLOCK_ROCKET, BLOCK_HOME, BLOCK_LIGHTNING, BLOCK_MOUNTAIN
}

/** Legacy alias kept for source-compat with the original bento call-sites. */
typealias BentoTheme = WatermarkTheme

@Composable
fun ThematicWatermark(
    theme: WatermarkTheme,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val areaWidth  = w * 0.55f
        val areaHeight = h * 0.75f
        val originX    = w - areaWidth - 6f
        val originY    = h - areaHeight - 6f
        val area = Size(areaWidth, areaHeight)
        val origin = Offset(originX, originY)

        when (theme) {
            WatermarkTheme.BOOK            -> drawBookStack(origin, area, accent)
            WatermarkTheme.RATING          -> drawTrophy(origin, area, accent)
            WatermarkTheme.DICTIONARY      -> drawLensGlass(origin, area, accent)
            WatermarkTheme.GOAL            -> drawBullseye(origin, area, accent)

            WatermarkTheme.LESSON          -> drawGradCap(origin, area, accent)
            WatermarkTheme.FLASHCARD_SET   -> drawCardStack(origin, area, accent)
            WatermarkTheme.WEAK_WORD       -> drawFlame(origin, area, accent)

            WatermarkTheme.GAME_ARTICLES   -> drawBigA(origin, area, accent)
            WatermarkTheme.GAME_SPEED      -> drawStopwatch(origin, area, accent)
            WatermarkTheme.GAME_VERBS      -> drawTranslateArrows(origin, area, accent)
            WatermarkTheme.GAME_SOPA       -> drawDotGrid(origin, area, accent)
            WatermarkTheme.GAME_PALABRA    -> drawBigTt(origin, area, accent)
            WatermarkTheme.GAME_MATH       -> drawMathOps(origin, area, accent)
            WatermarkTheme.GAME_CROSSWORD  -> drawCrossword(origin, area, accent)
            WatermarkTheme.GAME_LIBROS     -> drawOpenBook(origin, area, accent)

            WatermarkTheme.BLOCK_ROCKET    -> drawRocket(origin, area, accent)
            WatermarkTheme.BLOCK_HOME      -> drawHouse(origin, area, accent)
            WatermarkTheme.BLOCK_LIGHTNING -> drawLightning(origin, area, accent)
            WatermarkTheme.BLOCK_MOUNTAIN  -> drawMountain(origin, area, accent)
        }
    }
}

/** Legacy wrapper — old call sites used `BentoWatermark`. */
@Composable
fun BentoWatermark(
    theme: BentoTheme,
    accent: Color,
    modifier: Modifier = Modifier
) = ThematicWatermark(theme, accent, modifier)

// ─── Drawings ───────────────────────────────────────────────────

/** Three offset rectangles forming a stacked-books motif. */
private fun DrawScope.drawBookStack(origin: Offset, area: Size, accent: Color) {
    val color = accent.copy(alpha = 0.13f)
    val bookH = area.height / 4f
    val gap   = bookH * 0.3f
    repeat(3) { i ->
        val width  = area.width - i * area.width * 0.12f
        val left   = origin.x + i * area.width * 0.06f
        val top    = origin.y + (3 - i) * (bookH + gap) - bookH
        drawRect(color, Offset(left, top + area.height - 3 * (bookH + gap)), Size(width, bookH))
        drawRect(accent.copy(alpha = 0.18f), Offset(left, top + area.height - 3 * (bookH + gap)), Size(width * 0.04f, bookH))
    }
}

/** Trophy silhouette — cup body, handles, base. */
private fun DrawScope.drawTrophy(origin: Offset, area: Size, accent: Color) {
    val color = accent.copy(alpha = 0.13f)
    val cx = origin.x + area.width / 2f
    val cupTop = origin.y + area.height * 0.10f
    val cupH   = area.height * 0.55f
    val cupW   = area.width * 0.55f
    val baseY  = origin.y + area.height * 0.85f
    val baseW  = cupW * 0.85f
    val cupRect = Rect(cx - cupW / 2f, cupTop, cx + cupW / 2f, cupTop + cupH)
    val cupPath = Path().apply {
        addRoundRect(RoundRect(cupRect, CornerRadius(cupW * 0.45f, cupH * 0.4f)))
    }
    drawPath(cupPath, color)
    val handleR = cupW * 0.18f
    drawCircle(color, handleR, Offset(cupRect.left - handleR * 0.4f, cupTop + cupH * 0.35f), style = Stroke(handleR * 0.45f))
    drawCircle(color, handleR, Offset(cupRect.right + handleR * 0.4f, cupTop + cupH * 0.35f), style = Stroke(handleR * 0.45f))
    drawRect(color, Offset(cx - cupW * 0.10f, cupTop + cupH), Size(cupW * 0.20f, area.height * 0.10f))
    drawRect(color, Offset(cx - baseW / 2f, baseY), Size(baseW, area.height * 0.06f))
}

/** Magnifying glass — circle frame with diagonal handle. */
private fun DrawScope.drawLensGlass(origin: Offset, area: Size, accent: Color) {
    val color = accent.copy(alpha = 0.14f)
    val r  = area.width.coerceAtMost(area.height) * 0.35f
    val cx = origin.x + area.width * 0.45f
    val cy = origin.y + area.height * 0.40f
    drawCircle(color, r, Offset(cx, cy), style = Stroke(r * 0.18f))
    drawCircle(accent.copy(alpha = 0.06f), r * 0.65f, Offset(cx, cy))
    val angle  = Math.toRadians(45.0)
    val startX = cx + (r * Math.cos(angle)).toFloat()
    val startY = cy + (r * Math.sin(angle)).toFloat()
    drawLine(color, Offset(startX, startY), Offset(startX + r * 0.9f, startY + r * 0.9f),
        strokeWidth = r * 0.22f, cap = StrokeCap.Round)
}

/** Bullseye / dartboard — concentric circles. */
private fun DrawScope.drawBullseye(origin: Offset, area: Size, accent: Color) {
    val maxR = area.width.coerceAtMost(area.height) * 0.45f
    val cx = origin.x + area.width * 0.5f
    val cy = origin.y + area.height * 0.5f
    listOf(
        maxR        to accent.copy(alpha = 0.14f),
        maxR * 0.75f to accent.copy(alpha = 0.06f),
        maxR * 0.50f to accent.copy(alpha = 0.14f),
        maxR * 0.25f to accent.copy(alpha = 0.06f)
    ).forEach { (radius, color) -> drawCircle(color, radius, Offset(cx, cy)) }
    drawCircle(accent.copy(alpha = 0.22f), maxR * 0.10f, Offset(cx, cy))
}

// ─── Continue Pager themes ─────────────────────────────────────

/** Graduation cap — flat square mortarboard + base + tassel. */
private fun DrawScope.drawGradCap(origin: Offset, area: Size, accent: Color) {
    val color = accent.copy(alpha = 0.13f)
    val cx = origin.x + area.width * 0.5f
    val cy = origin.y + area.height * 0.5f
    val w = area.width * 0.65f
    val h = area.height * 0.18f
    // Mortarboard — diamond
    val board = Path().apply {
        moveTo(cx, cy - h)
        lineTo(cx + w / 2f, cy)
        lineTo(cx, cy + h)
        lineTo(cx - w / 2f, cy)
        close()
    }
    drawPath(board, color)
    // Cap base — trapezoid below
    val baseTop = cy + h * 0.6f
    val base = Path().apply {
        moveTo(cx - w * 0.32f, baseTop)
        lineTo(cx + w * 0.32f, baseTop)
        lineTo(cx + w * 0.22f, baseTop + h * 1.6f)
        lineTo(cx - w * 0.22f, baseTop + h * 1.6f)
        close()
    }
    drawPath(base, color)
    // Tassel
    drawLine(accent.copy(alpha = 0.20f), Offset(cx, cy),
        Offset(cx + w * 0.45f, cy + h * 1.6f), strokeWidth = h * 0.18f, cap = StrokeCap.Round)
    drawCircle(accent.copy(alpha = 0.22f), h * 0.22f, Offset(cx + w * 0.45f, cy + h * 1.7f))
}

/** Stack of three tilted cards. */
private fun DrawScope.drawCardStack(origin: Offset, area: Size, accent: Color) {
    val color = accent.copy(alpha = 0.13f)
    val cx = origin.x + area.width * 0.5f
    val cy = origin.y + area.height * 0.55f
    val w = area.width * 0.55f
    val h = area.height * 0.50f
    val angles = listOf(-12f, 0f, 10f)
    angles.forEachIndexed { i, deg ->
        val rad = Math.toRadians(deg.toDouble())
        val cos = Math.cos(rad).toFloat()
        val sin = Math.sin(rad).toFloat()
        // Approximate rotated rect using a Path
        val hw = w / 2f; val hh = h / 2f
        fun pt(dx: Float, dy: Float) = Offset(cx + dx * cos - dy * sin, cy + dx * sin + dy * cos)
        val card = Path().apply {
            val p1 = pt(-hw, -hh); val p2 = pt(hw, -hh)
            val p3 = pt(hw, hh);   val p4 = pt(-hw, hh)
            moveTo(p1.x, p1.y); lineTo(p2.x, p2.y); lineTo(p3.x, p3.y); lineTo(p4.x, p4.y); close()
        }
        drawPath(card, if (i == 1) accent.copy(alpha = 0.18f) else color)
    }
}

/** Flame / pulsing droplet shape for "weak word". */
private fun DrawScope.drawFlame(origin: Offset, area: Size, accent: Color) {
    val color = accent.copy(alpha = 0.13f)
    val cx = origin.x + area.width * 0.5f
    val cy = origin.y + area.height * 0.55f
    val r = area.width.coerceAtMost(area.height) * 0.40f
    val flame = Path().apply {
        moveTo(cx, cy - r)
        cubicTo(cx + r * 0.9f, cy - r * 0.2f, cx + r * 0.7f, cy + r, cx, cy + r)
        cubicTo(cx - r * 0.7f, cy + r, cx - r * 0.9f, cy - r * 0.2f, cx, cy - r)
        close()
    }
    drawPath(flame, color)
    drawCircle(accent.copy(alpha = 0.20f), r * 0.30f, Offset(cx, cy + r * 0.35f))
}

// ─── Game themes ───────────────────────────────────────────────

/** Big stylised letter "A" — two diagonals + cross-bar. */
private fun DrawScope.drawBigA(origin: Offset, area: Size, accent: Color) {
    val color = accent.copy(alpha = 0.16f)
    val cx = origin.x + area.width * 0.5f
    val baseY = origin.y + area.height * 0.85f
    val topY  = origin.y + area.height * 0.15f
    val halfW = area.width * 0.32f
    val sw = area.width * 0.10f
    drawLine(color, Offset(cx - halfW, baseY), Offset(cx, topY), strokeWidth = sw, cap = StrokeCap.Round)
    drawLine(color, Offset(cx, topY), Offset(cx + halfW, baseY), strokeWidth = sw, cap = StrokeCap.Round)
    // Crossbar around 60%
    val barY = topY + (baseY - topY) * 0.62f
    drawLine(accent.copy(alpha = 0.22f),
        Offset(cx - halfW * 0.55f, barY), Offset(cx + halfW * 0.55f, barY),
        strokeWidth = sw * 0.7f, cap = StrokeCap.Round)
}

/** Stopwatch — dial circle, top button, two hands, four ticks. */
private fun DrawScope.drawStopwatch(origin: Offset, area: Size, accent: Color) {
    val color = accent.copy(alpha = 0.13f)
    val cx = origin.x + area.width * 0.5f
    val cy = origin.y + area.height * 0.58f
    val r = area.width.coerceAtMost(area.height) * 0.34f
    drawCircle(color, r, Offset(cx, cy), style = Stroke(r * 0.13f))
    drawRect(color, Offset(cx - r * 0.12f, cy - r * 1.25f), Size(r * 0.24f, r * 0.18f))
    listOf(0f to -1f, 1f to 0f, 0f to 1f, -1f to 0f).forEach { (dx, dy) ->
        drawCircle(accent.copy(alpha = 0.20f), r * 0.05f, Offset(cx + dx * r * 0.85f, cy + dy * r * 0.85f))
    }
    drawLine(accent.copy(alpha = 0.22f), Offset(cx, cy), Offset(cx - r * 0.5f, cy),
        strokeWidth = r * 0.08f, cap = StrokeCap.Round)
    drawLine(accent.copy(alpha = 0.22f), Offset(cx, cy), Offset(cx, cy - r * 0.7f),
        strokeWidth = r * 0.08f, cap = StrokeCap.Round)
}

/** Two horizontal arrows facing each other = translation. */
private fun DrawScope.drawTranslateArrows(origin: Offset, area: Size, accent: Color) {
    val color = accent.copy(alpha = 0.16f)
    val cx = origin.x + area.width * 0.5f
    val cy = origin.y + area.height * 0.5f
    val len = area.width * 0.36f
    val gap = area.width * 0.06f
    val sw = area.width * 0.08f
    val head = sw * 1.4f
    // Top arrow → right
    val y1 = cy - area.height * 0.16f
    drawLine(color, Offset(cx - len, y1), Offset(cx - gap, y1), strokeWidth = sw, cap = StrokeCap.Round)
    drawLine(color, Offset(cx - gap, y1), Offset(cx - gap - head, y1 - head), strokeWidth = sw, cap = StrokeCap.Round)
    drawLine(color, Offset(cx - gap, y1), Offset(cx - gap - head, y1 + head), strokeWidth = sw, cap = StrokeCap.Round)
    // Bottom arrow ← left
    val y2 = cy + area.height * 0.16f
    drawLine(color, Offset(cx + gap, y2), Offset(cx + len, y2), strokeWidth = sw, cap = StrokeCap.Round)
    drawLine(color, Offset(cx + gap, y2), Offset(cx + gap + head, y2 - head), strokeWidth = sw, cap = StrokeCap.Round)
    drawLine(color, Offset(cx + gap, y2), Offset(cx + gap + head, y2 + head), strokeWidth = sw, cap = StrokeCap.Round)
}

/** 5×5 grid of dots — letter-soup motif. */
private fun DrawScope.drawDotGrid(origin: Offset, area: Size, accent: Color) {
    val color = accent.copy(alpha = 0.18f)
    val n = 5
    val cell = area.width.coerceAtMost(area.height) * 0.7f / n
    val startX = origin.x + (area.width - cell * n) * 0.5f + cell * 0.5f
    val startY = origin.y + (area.height - cell * n) * 0.5f + cell * 0.5f
    val r = cell * 0.22f
    for (row in 0 until n) for (col in 0 until n) {
        drawCircle(color, r, Offset(startX + col * cell, startY + row * cell))
    }
}

/** Big "Tt" — uppercase T with a smaller t beside it. */
private fun DrawScope.drawBigTt(origin: Offset, area: Size, accent: Color) {
    val color = accent.copy(alpha = 0.16f)
    val sw = area.width * 0.08f
    // Capital T
    val tx = origin.x + area.width * 0.34f
    val tTop = origin.y + area.height * 0.18f
    val tBot = origin.y + area.height * 0.82f
    val tHalf = area.width * 0.20f
    drawLine(color, Offset(tx - tHalf, tTop), Offset(tx + tHalf, tTop), strokeWidth = sw, cap = StrokeCap.Round)
    drawLine(color, Offset(tx, tTop), Offset(tx, tBot), strokeWidth = sw, cap = StrokeCap.Round)
    // Lowercase t — smaller, to the right
    val lx = origin.x + area.width * 0.78f
    val lTop = origin.y + area.height * 0.36f
    val lBot = origin.y + area.height * 0.85f
    val lHalf = area.width * 0.10f
    val lsw = sw * 0.7f
    drawLine(accent.copy(alpha = 0.20f),
        Offset(lx - lHalf, lTop + (lBot - lTop) * 0.18f),
        Offset(lx + lHalf, lTop + (lBot - lTop) * 0.18f),
        strokeWidth = lsw, cap = StrokeCap.Round)
    drawLine(accent.copy(alpha = 0.20f), Offset(lx, lTop), Offset(lx, lBot),
        strokeWidth = lsw, cap = StrokeCap.Round)
}

/** Plus / minus / multiply / divide arranged in a cross. */
private fun DrawScope.drawMathOps(origin: Offset, area: Size, accent: Color) {
    val color = accent.copy(alpha = 0.18f)
    val cx = origin.x + area.width * 0.5f
    val cy = origin.y + area.height * 0.5f
    val r = area.width.coerceAtMost(area.height) * 0.18f
    val sw = r * 0.30f
    val off = r * 1.6f
    // Plus — top
    drawLine(color, Offset(cx, cy - off - r * 0.5f), Offset(cx, cy - off + r * 0.5f), sw, cap = StrokeCap.Round)
    drawLine(color, Offset(cx - r * 0.5f, cy - off), Offset(cx + r * 0.5f, cy - off), sw, cap = StrokeCap.Round)
    // Minus — left
    drawLine(color, Offset(cx - off - r * 0.5f, cy), Offset(cx - off + r * 0.5f, cy), sw, cap = StrokeCap.Round)
    // Times — right (X)
    drawLine(color, Offset(cx + off - r * 0.4f, cy - r * 0.4f),
        Offset(cx + off + r * 0.4f, cy + r * 0.4f), sw, cap = StrokeCap.Round)
    drawLine(color, Offset(cx + off + r * 0.4f, cy - r * 0.4f),
        Offset(cx + off - r * 0.4f, cy + r * 0.4f), sw, cap = StrokeCap.Round)
    // Divide — bottom
    drawLine(color, Offset(cx - r * 0.5f, cy + off), Offset(cx + r * 0.5f, cy + off), sw, cap = StrokeCap.Round)
    drawCircle(color, r * 0.16f, Offset(cx, cy + off - r * 0.5f))
    drawCircle(color, r * 0.16f, Offset(cx, cy + off + r * 0.5f))
}

/** 4×4 chequered crossword grid (alternating filled cells). */
private fun DrawScope.drawCrossword(origin: Offset, area: Size, accent: Color) {
    val n = 4
    val cell = area.width.coerceAtMost(area.height) * 0.75f / n
    val startX = origin.x + (area.width - cell * n) * 0.5f
    val startY = origin.y + (area.height - cell * n) * 0.5f
    // Frame
    val frame = accent.copy(alpha = 0.13f)
    drawRect(frame, Offset(startX, startY), Size(cell * n, cell * n), style = Stroke(cell * 0.06f))
    // Filled "black" cells — chequered
    val black = accent.copy(alpha = 0.18f)
    for (r in 0 until n) for (c in 0 until n) {
        if ((r + c) % 2 == 0) {
            drawRect(black, Offset(startX + c * cell, startY + r * cell), Size(cell, cell))
        }
    }
}

/** Open book — two pages forming a V. */
private fun DrawScope.drawOpenBook(origin: Offset, area: Size, accent: Color) {
    val color = accent.copy(alpha = 0.14f)
    val cx = origin.x + area.width * 0.5f
    val cy = origin.y + area.height * 0.55f
    val w = area.width * 0.42f
    val h = area.height * 0.42f
    // Left page — slight tilt
    val left = Path().apply {
        moveTo(cx - 1f, cy - h)
        lineTo(cx - 1f, cy + h)
        lineTo(cx - w, cy + h * 0.85f)
        lineTo(cx - w, cy - h * 0.85f)
        close()
    }
    val right = Path().apply {
        moveTo(cx + 1f, cy - h)
        lineTo(cx + 1f, cy + h)
        lineTo(cx + w, cy + h * 0.85f)
        lineTo(cx + w, cy - h * 0.85f)
        close()
    }
    drawPath(left, color)
    drawPath(right, color)
    // Spine
    drawLine(accent.copy(alpha = 0.20f),
        Offset(cx, cy - h), Offset(cx, cy + h),
        strokeWidth = area.width * 0.012f)
    // Text lines
    val lineColor = accent.copy(alpha = 0.10f)
    val lineSW = area.width * 0.012f
    repeat(3) { i ->
        val yy = cy - h * 0.45f + i * h * 0.35f
        drawLine(lineColor, Offset(cx - w * 0.85f, yy), Offset(cx - w * 0.18f, yy), lineSW)
        drawLine(lineColor, Offset(cx + w * 0.18f, yy), Offset(cx + w * 0.85f, yy), lineSW)
    }
}

// ─── Course block themes ───────────────────────────────────────

/** Rocket — body + nose triangle + fins + flame. */
private fun DrawScope.drawRocket(origin: Offset, area: Size, accent: Color) {
    val color = accent.copy(alpha = 0.16f)
    val cx = origin.x + area.width * 0.5f
    val topY = origin.y + area.height * 0.10f
    val bodyTop = origin.y + area.height * 0.30f
    val bodyBot = origin.y + area.height * 0.72f
    val bodyW = area.width * 0.26f
    // Nose cone
    val nose = Path().apply {
        moveTo(cx, topY)
        lineTo(cx + bodyW / 2f, bodyTop)
        lineTo(cx - bodyW / 2f, bodyTop)
        close()
    }
    drawPath(nose, color)
    // Body
    drawRect(color, Offset(cx - bodyW / 2f, bodyTop), Size(bodyW, bodyBot - bodyTop))
    // Fins — left + right triangles
    val finH = area.height * 0.16f
    val finW = bodyW * 0.6f
    val leftFin = Path().apply {
        moveTo(cx - bodyW / 2f, bodyBot - finH)
        lineTo(cx - bodyW / 2f - finW, bodyBot)
        lineTo(cx - bodyW / 2f, bodyBot)
        close()
    }
    val rightFin = Path().apply {
        moveTo(cx + bodyW / 2f, bodyBot - finH)
        lineTo(cx + bodyW / 2f + finW, bodyBot)
        lineTo(cx + bodyW / 2f, bodyBot)
        close()
    }
    drawPath(leftFin, color); drawPath(rightFin, color)
    // Window
    drawCircle(accent.copy(alpha = 0.06f), bodyW * 0.28f,
        Offset(cx, bodyTop + (bodyBot - bodyTop) * 0.30f))
    // Flame
    val flame = Path().apply {
        moveTo(cx - bodyW * 0.30f, bodyBot)
        lineTo(cx, origin.y + area.height * 0.95f)
        lineTo(cx + bodyW * 0.30f, bodyBot)
        close()
    }
    drawPath(flame, accent.copy(alpha = 0.20f))
}

/** Simple house — square + triangle roof + door. */
private fun DrawScope.drawHouse(origin: Offset, area: Size, accent: Color) {
    val color = accent.copy(alpha = 0.16f)
    val cx = origin.x + area.width * 0.5f
    val side = area.width.coerceAtMost(area.height) * 0.55f
    val roofH = side * 0.55f
    val bodyTop = origin.y + area.height * 0.5f - side * 0.3f
    // Body
    drawRect(color, Offset(cx - side / 2f, bodyTop), Size(side, side))
    // Roof
    val roof = Path().apply {
        moveTo(cx - side * 0.62f, bodyTop)
        lineTo(cx, bodyTop - roofH)
        lineTo(cx + side * 0.62f, bodyTop)
        close()
    }
    drawPath(roof, color)
    // Door
    val doorW = side * 0.22f
    val doorH = side * 0.42f
    drawRect(accent.copy(alpha = 0.22f),
        Offset(cx - doorW / 2f, bodyTop + side - doorH), Size(doorW, doorH))
}

/** Lightning bolt — zig-zag. */
private fun DrawScope.drawLightning(origin: Offset, area: Size, accent: Color) {
    val color = accent.copy(alpha = 0.20f)
    val cx = origin.x + area.width * 0.5f
    val top = origin.y + area.height * 0.10f
    val bot = origin.y + area.height * 0.90f
    val w = area.width * 0.42f
    val bolt = Path().apply {
        moveTo(cx + w * 0.10f, top)
        lineTo(cx - w * 0.45f, top + (bot - top) * 0.50f)
        lineTo(cx - w * 0.05f, top + (bot - top) * 0.50f)
        lineTo(cx - w * 0.30f, bot)
        lineTo(cx + w * 0.50f, top + (bot - top) * 0.42f)
        lineTo(cx + w * 0.05f, top + (bot - top) * 0.42f)
        close()
    }
    drawPath(bolt, color)
}

/** 2-3 mountain peaks with a snow cap on the highest. */
private fun DrawScope.drawMountain(origin: Offset, area: Size, accent: Color) {
    val color = accent.copy(alpha = 0.16f)
    val baseY = origin.y + area.height * 0.85f
    val left  = origin.x + area.width * 0.10f
    val right = origin.x + area.width * 0.95f
    // Front (left) peak
    val p1 = Path().apply {
        moveTo(left, baseY)
        lineTo(origin.x + area.width * 0.42f, origin.y + area.height * 0.30f)
        lineTo(origin.x + area.width * 0.70f, baseY)
        close()
    }
    // Back (right) peak — taller
    val peakX = origin.x + area.width * 0.66f
    val peakY = origin.y + area.height * 0.18f
    val p2 = Path().apply {
        moveTo(origin.x + area.width * 0.36f, baseY)
        lineTo(peakX, peakY)
        lineTo(right, baseY)
        close()
    }
    drawPath(p2, accent.copy(alpha = 0.13f))
    drawPath(p1, color)
    // Snow cap on tallest
    val snow = Path().apply {
        moveTo(peakX, peakY)
        lineTo(peakX + area.width * 0.10f, peakY + area.height * 0.16f)
        lineTo(peakX + area.width * 0.04f, peakY + area.height * 0.14f)
        lineTo(peakX, peakY + area.height * 0.18f)
        lineTo(peakX - area.width * 0.04f, peakY + area.height * 0.14f)
        lineTo(peakX - area.width * 0.10f, peakY + area.height * 0.16f)
        close()
    }
    drawPath(snow, accent.copy(alpha = 0.22f))
}
