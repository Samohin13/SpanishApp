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
// Designed as ABSTRACT compositions (not literal "cap / flame") because the
// previous literal icons looked primitive. Each is a layered geometric motif
// using rounded shapes + multiple opacity tiers for depth.

/**
 * LESSON — abstract notebook: 4 stacked rounded rectangles fanning slightly
 * outward, with thin horizontal "ruling" lines on the front-most one.
 * Reads as "open lesson material" without being a literal book.
 */
private fun DrawScope.drawGradCap(origin: Offset, area: Size, accent: Color) {
    val side = area.width.coerceAtMost(area.height)
    val w = side * 0.78f
    val h = side * 0.62f
    val cx = origin.x + area.width * 0.55f
    val cy = origin.y + area.height * 0.55f
    val r = w * 0.10f

    // Stack of 4 page-like rounded rects, offset diagonally.
    val tiers = listOf(
        Triple(-w * 0.18f,  h * 0.18f, 0.06f),
        Triple(-w * 0.06f,  h * 0.06f, 0.09f),
        Triple( w * 0.06f, -h * 0.06f, 0.13f),
        Triple( w * 0.18f, -h * 0.18f, 0.18f)
    )
    tiers.forEach { (dx, dy, a) ->
        drawRoundRect(
            color = accent.copy(alpha = a),
            topLeft = Offset(cx - w / 2f + dx, cy - h / 2f + dy),
            size = Size(w, h),
            cornerRadius = CornerRadius(r, r)
        )
    }
    // Ruling lines on the front page — gives "text content" feel.
    val front = tiers.last()
    val frontLeft = cx - w / 2f + front.first
    val frontTop  = cy - h / 2f + front.second
    val lineCol = accent.copy(alpha = 0.10f)
    repeat(3) { i ->
        val ly = frontTop + h * (0.30f + i * 0.18f)
        drawLine(
            color = lineCol,
            start = Offset(frontLeft + w * 0.18f, ly),
            end   = Offset(frontLeft + w * 0.78f - i * w * 0.10f, ly),
            strokeWidth = h * 0.04f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * FLASHCARD_SET — five rounded cards fanned in an arc like a hand of cards,
 * each rotated incrementally and with progressively higher opacity so the
 * front card stands out. More dramatic than the old 3-card stack.
 */
private fun DrawScope.drawCardStack(origin: Offset, area: Size, accent: Color) {
    val cx = origin.x + area.width * 0.55f
    val cy = origin.y + area.height * 0.70f      // anchor near bottom — fan rises up
    val w = area.width * 0.42f
    val h = area.height * 0.55f
    val angles = listOf(-28f, -14f, 0f, 14f, 28f)
    val alphas = listOf(0.07f, 0.10f, 0.15f, 0.10f, 0.07f)

    angles.forEachIndexed { i, deg ->
        val rad = Math.toRadians(deg.toDouble())
        val cos = Math.cos(rad).toFloat()
        val sin = Math.sin(rad).toFloat()
        // pivot near bottom-centre so cards fan outward at the top
        val pivotY = h * 0.45f
        fun pt(dx: Float, dy: Float): Offset {
            val ny = dy - pivotY            // shift so rotation pivots near bottom
            return Offset(
                cx + dx * cos - ny * sin,
                cy + dx * sin + ny * cos + pivotY
            )
        }
        val hw = w / 2f
        val card = Path().apply {
            val p1 = pt(-hw, -h); val p2 = pt(hw, -h)
            val p3 = pt(hw, 0f);  val p4 = pt(-hw, 0f)
            moveTo(p1.x, p1.y); lineTo(p2.x, p2.y); lineTo(p3.x, p3.y); lineTo(p4.x, p4.y); close()
        }
        drawPath(card, accent.copy(alpha = alphas[i]))
    }
}

/**
 * WEAK_WORD — abstract pulse / radar ripple. Replaces the previous literal
 * flame droplet (which read as a strange water drop). 5 concentric arcs
 * radiating from bottom-right, alternating opacity, with a small filled
 * core. Suggests "echo / repetition / resurfacing".
 */
private fun DrawScope.drawFlame(origin: Offset, area: Size, accent: Color) {
    val cx = origin.x + area.width * 0.55f
    val cy = origin.y + area.height * 0.55f
    val maxR = area.width.coerceAtMost(area.height) * 0.55f

    // Five rings, opacity fading outward, alternating ring widths.
    val rings = listOf(
        maxR        to 0.05f,
        maxR * 0.82f to 0.08f,
        maxR * 0.64f to 0.11f,
        maxR * 0.46f to 0.14f,
        maxR * 0.28f to 0.18f
    )
    rings.forEach { (r, a) ->
        drawCircle(accent.copy(alpha = a), r, Offset(cx, cy), style = Stroke(width = maxR * 0.04f))
    }
    // Filled core dot.
    drawCircle(accent.copy(alpha = 0.22f), maxR * 0.10f, Offset(cx, cy))

    // Three tiny "sparks" floating up-left to add detail / asymmetry.
    val sparkCol = accent.copy(alpha = 0.18f)
    drawCircle(sparkCol, maxR * 0.05f, Offset(cx - maxR * 0.55f, cy - maxR * 0.55f))
    drawCircle(sparkCol, maxR * 0.04f, Offset(cx - maxR * 0.75f, cy - maxR * 0.30f))
    drawCircle(sparkCol, maxR * 0.03f, Offset(cx - maxR * 0.30f, cy - maxR * 0.80f))
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
// Redesigned in v2 — previous block watermarks were literal icons (rocket /
// house / lightning / mountain) with only 3-5 primitives. The new versions
// are abstract layered compositions (7-10 primitives, 3-5 opacity tiers,
// cubic Bezier curves) so each block reads as a "thematic space" instead of
// a tiny clip-art glyph. All primitives stay clipped inside `area`.

/**
 * BLOCK_ROCKET — abstract upward motion.
 * • One large soft "sun" disc tucked into the upper-right corner.
 * • A swept arc trajectory rising bottom-left → upper-right (cubic Bezier).
 * • Three thin parallel diagonals (~75°) acting as motion lines.
 * • Six scattered dot-stars across the upper half at varying alpha.
 */
private fun DrawScope.drawRocket(origin: Offset, area: Size, accent: Color) {
    val ox = origin.x; val oy = origin.y
    val w = area.width; val h = area.height

    // Soft sun in upper-right
    drawCircle(accent.copy(alpha = 0.07f), w * 0.34f, Offset(ox + w * 0.88f, oy + h * 0.18f))
    drawCircle(accent.copy(alpha = 0.10f), w * 0.18f, Offset(ox + w * 0.88f, oy + h * 0.18f))

    // Trajectory arc — cubic Bezier swept upward
    val arc = Path().apply {
        moveTo(ox + w * 0.06f, oy + h * 0.92f)
        cubicTo(
            ox + w * 0.30f, oy + h * 0.90f,
            ox + w * 0.45f, oy + h * 0.20f,
            ox + w * 0.92f, oy + h * 0.10f
        )
    }
    drawPath(arc, accent.copy(alpha = 0.22f), style = Stroke(width = h * 0.025f, cap = StrokeCap.Round))

    // Three parallel diagonal motion lines (~75° from horizontal)
    val angle = Math.toRadians(-75.0)
    val dx = Math.cos(angle).toFloat()
    val dy = Math.sin(angle).toFloat()
    val len = h * 0.30f
    val baseAlphas = listOf(0.18f, 0.13f, 0.09f)
    listOf(0f, w * 0.10f, w * 0.20f).forEachIndexed { i, off ->
        val sx = ox + w * 0.18f + off
        val sy = oy + h * 0.78f
        drawLine(
            color = accent.copy(alpha = baseAlphas[i]),
            start = Offset(sx, sy),
            end = Offset(sx + dx * len, sy + dy * len),
            strokeWidth = h * 0.018f,
            cap = StrokeCap.Round
        )
    }

    // Star dots scattered across upper half
    val stars = listOf(
        Triple(0.22f, 0.18f, 0.20f),
        Triple(0.40f, 0.08f, 0.14f),
        Triple(0.62f, 0.32f, 0.18f),
        Triple(0.74f, 0.06f, 0.10f),
        Triple(0.30f, 0.40f, 0.12f),
        Triple(0.55f, 0.50f, 0.16f)
    )
    stars.forEach { (px, py, a) ->
        drawCircle(accent.copy(alpha = a), h * 0.018f, Offset(ox + w * px, oy + h * py))
    }
}

/**
 * BLOCK_HOME — concentric / orbital composition.
 * • 3 concentric stroke circles (orbits) at decreasing alpha.
 * • Filled core dot (the "planet/home").
 * • 3 small dots placed on the different orbits (objects).
 * • 2 thin diagonal "meridian" lines crossing the centre at low alpha.
 */
private fun DrawScope.drawHouse(origin: Offset, area: Size, accent: Color) {
    val cx = origin.x + area.width * 0.50f
    val cy = origin.y + area.height * 0.55f
    val maxR = area.width.coerceAtMost(area.height) * 0.45f
    val sw = maxR * 0.04f

    // Three orbital rings
    drawCircle(accent.copy(alpha = 0.06f), maxR,         Offset(cx, cy), style = Stroke(sw))
    drawCircle(accent.copy(alpha = 0.10f), maxR * 0.74f, Offset(cx, cy), style = Stroke(sw))
    drawCircle(accent.copy(alpha = 0.14f), maxR * 0.48f, Offset(cx, cy), style = Stroke(sw))

    // Two crossing meridians
    val merCol = accent.copy(alpha = 0.05f)
    val mw = sw * 0.8f
    drawLine(merCol, Offset(cx - maxR * 0.95f, cy - maxR * 0.35f),
        Offset(cx + maxR * 0.95f, cy + maxR * 0.35f), strokeWidth = mw)
    drawLine(merCol, Offset(cx - maxR * 0.95f, cy + maxR * 0.35f),
        Offset(cx + maxR * 0.95f, cy - maxR * 0.35f), strokeWidth = mw)

    // Filled core
    drawCircle(accent.copy(alpha = 0.28f), maxR * 0.13f, Offset(cx, cy))
    drawCircle(accent.copy(alpha = 0.10f), maxR * 0.22f, Offset(cx, cy))

    // Objects on orbits
    drawCircle(accent.copy(alpha = 0.24f), maxR * 0.07f,
        Offset(cx + maxR * 0.74f * Math.cos(Math.toRadians(-30.0)).toFloat(),
               cy + maxR * 0.74f * Math.sin(Math.toRadians(-30.0)).toFloat()))
    drawCircle(accent.copy(alpha = 0.20f), maxR * 0.06f,
        Offset(cx + maxR * Math.cos(Math.toRadians(140.0)).toFloat(),
               cy + maxR * Math.sin(Math.toRadians(140.0)).toFloat()))
    drawCircle(accent.copy(alpha = 0.18f), maxR * 0.05f,
        Offset(cx + maxR * 0.48f * Math.cos(Math.toRadians(70.0)).toFloat(),
               cy + maxR * 0.48f * Math.sin(Math.toRadians(70.0)).toFloat()))
}

/**
 * BLOCK_LIGHTNING — dynamic / speed-line composition.
 * • 6 horizontal speed-lines at the centre, varying length and alpha.
 * • 1 long diagonal sweep crossing the whole area (cubic Bezier curve).
 * • One large filled chevron ">" stamped at the right anchored to the rightmost line.
 * • 3 small triangular "splash" sparks on the left side.
 */
private fun DrawScope.drawLightning(origin: Offset, area: Size, accent: Color) {
    val ox = origin.x; val oy = origin.y
    val w = area.width; val h = area.height

    // Six speed-lines, lengths shorter at top/bottom
    val speedY = listOf(0.18f, 0.30f, 0.42f, 0.54f, 0.66f, 0.78f)
    val speedLen = listOf(0.55f, 0.78f, 0.92f, 0.92f, 0.78f, 0.55f)
    val speedAlpha = listOf(0.08f, 0.13f, 0.20f, 0.20f, 0.13f, 0.08f)
    speedY.forEachIndexed { i, yPct ->
        val y = oy + h * yPct
        val len = w * speedLen[i]
        drawLine(
            accent.copy(alpha = speedAlpha[i]),
            Offset(ox + w - len, y),
            Offset(ox + w * 0.95f, y),
            strokeWidth = h * 0.022f,
            cap = StrokeCap.Round
        )
    }

    // Diagonal cubic-Bezier sweep across the whole tile
    val sweep = Path().apply {
        moveTo(ox + w * 0.05f, oy + h * 0.95f)
        cubicTo(
            ox + w * 0.30f, oy + h * 0.55f,
            ox + w * 0.55f, oy + h * 0.40f,
            ox + w * 0.95f, oy + h * 0.05f
        )
    }
    drawPath(sweep, accent.copy(alpha = 0.16f),
        style = Stroke(width = h * 0.03f, cap = StrokeCap.Round))

    // Large chevron ">" stamped at the right, mid-height
    val chvX = ox + w * 0.78f
    val chvY = oy + h * 0.48f
    val chvSize = h * 0.22f
    val chevron = Path().apply {
        moveTo(chvX - chvSize * 0.6f, chvY - chvSize)
        lineTo(chvX + chvSize * 0.4f, chvY)
        lineTo(chvX - chvSize * 0.6f, chvY + chvSize)
    }
    drawPath(chevron, accent.copy(alpha = 0.22f),
        style = Stroke(width = h * 0.028f, cap = StrokeCap.Round))

    // Three little triangular splash sparks on the left
    val sparkAlpha = accent.copy(alpha = 0.18f)
    listOf(
        Triple(0.04f, 0.20f, 0.05f),
        Triple(0.02f, 0.50f, 0.04f),
        Triple(0.08f, 0.82f, 0.05f)
    ).forEach { (px, py, sz) ->
        val x = ox + w * px; val y = oy + h * py; val s = h * sz
        val tri = Path().apply {
            moveTo(x, y - s)
            lineTo(x + s * 1.4f, y)
            lineTo(x, y + s)
            close()
        }
        drawPath(tri, sparkAlpha)
    }
}

/**
 * BLOCK_MOUNTAIN — layered geological composition.
 * • Big half-disc "moon/sun" near the top at very low alpha.
 * • 4 overlapping triangular ridges, each a different alpha tier (back to
 *   front: 0.07 → 0.10 → 0.14 → 0.20) so they read as receding ranges.
 * • Two thin cubic-Bezier "contour" curves drawn beneath the ridges.
 * • 3 tiny filled circles high in the sky (stars / birds).
 */
private fun DrawScope.drawMountain(origin: Offset, area: Size, accent: Color) {
    val ox = origin.x; val oy = origin.y
    val w = area.width; val h = area.height
    val baseY = oy + h * 0.88f

    // Soft half-disc "moon" in the sky
    drawCircle(accent.copy(alpha = 0.05f), h * 0.35f, Offset(ox + w * 0.72f, oy + h * 0.05f))
    drawCircle(accent.copy(alpha = 0.08f), h * 0.18f, Offset(ox + w * 0.72f, oy + h * 0.05f))

    // Stars / birds
    listOf(
        Triple(0.18f, 0.12f, 0.18f),
        Triple(0.32f, 0.22f, 0.14f),
        Triple(0.48f, 0.08f, 0.16f)
    ).forEach { (px, py, a) ->
        drawCircle(accent.copy(alpha = a), h * 0.014f, Offset(ox + w * px, oy + h * py))
    }

    // Contour curves under the ridges (rolling hills feel)
    val contour1 = Path().apply {
        moveTo(ox, oy + h * 0.78f)
        cubicTo(ox + w * 0.25f, oy + h * 0.72f,
                ox + w * 0.55f, oy + h * 0.84f,
                ox + w, oy + h * 0.74f)
    }
    val contour2 = Path().apply {
        moveTo(ox, oy + h * 0.92f)
        cubicTo(ox + w * 0.30f, oy + h * 0.86f,
                ox + w * 0.65f, oy + h * 0.94f,
                ox + w, oy + h * 0.88f)
    }
    drawPath(contour1, accent.copy(alpha = 0.06f),
        style = Stroke(width = h * 0.012f, cap = StrokeCap.Round))
    drawPath(contour2, accent.copy(alpha = 0.06f),
        style = Stroke(width = h * 0.012f, cap = StrokeCap.Round))

    // 4 layered ridges, back-to-front
    fun ridge(leftXPct: Float, peakXPct: Float, rightXPct: Float, peakYPct: Float, alpha: Float) {
        val p = Path().apply {
            moveTo(ox + w * leftXPct, baseY)
            lineTo(ox + w * peakXPct, oy + h * peakYPct)
            lineTo(ox + w * rightXPct, baseY)
            close()
        }
        drawPath(p, accent.copy(alpha = alpha))
    }
    ridge(0.50f, 0.78f, 1.05f, 0.20f, 0.07f)   // farthest, tallest right
    ridge(0.20f, 0.50f, 0.85f, 0.32f, 0.10f)   // mid-back
    ridge(-0.05f, 0.28f, 0.62f, 0.42f, 0.14f)  // mid-front
    ridge(0.10f, 0.40f, 0.78f, 0.56f, 0.20f)   // foreground
}
