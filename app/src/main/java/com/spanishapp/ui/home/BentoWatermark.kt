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
import androidx.compose.ui.graphics.drawscope.rotate

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
    GAME_FRASE, GAME_OIDO,

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
            WatermarkTheme.GAME_FRASE      -> drawWordTiles(origin, area, accent)
            WatermarkTheme.GAME_OIDO       -> drawSoundWaves(origin, area, accent)

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

/** v1.27 Frase Loca: три плитки-слова под углами (мотив логотипа). */
private fun DrawScope.drawWordTiles(origin: Offset, area: Size, accent: Color) {
    val color = accent.copy(alpha = 0.13f)
    val side = area.width.coerceAtMost(area.height) * 0.42f
    val corner = androidx.compose.ui.geometry.CornerRadius(side * 0.22f, side * 0.22f)
    val cx = origin.x + area.width * 0.45f
    val cy = origin.y + area.height * 0.45f

    fun tile(dx: Float, dy: Float, deg: Float, alpha: Float) {
        rotate(degrees = deg, pivot = Offset(cx + dx + side / 2f, cy + dy + side / 2f)) {
            drawRoundRect(
                color = accent.copy(alpha = alpha),
                topLeft = Offset(cx + dx, cy + dy),
                size = Size(side, side),
                cornerRadius = corner,
            )
        }
    }
    tile(-side * 0.75f, -side * 0.35f, -10f, 0.13f)
    tile(side * 0.30f, -side * 0.55f, 7f, 0.18f)
    tile(-side * 0.15f, side * 0.45f, -4f, 0.13f)
}

/** v1.27 El Oído: три расходящиеся звуковые дуги. */
private fun DrawScope.drawSoundWaves(origin: Offset, area: Size, accent: Color) {
    val side = area.width.coerceAtMost(area.height)
    val cx = origin.x + area.width * 0.30f
    val cy = origin.y + area.height * 0.60f
    val stroke = side * 0.07f
    // Точка-источник
    drawCircle(accent.copy(alpha = 0.18f), radius = side * 0.08f, center = Offset(cx, cy))
    // Три дуги вправо-вверх
    for (i in 1..3) {
        val r = side * (0.20f + i * 0.16f)
        drawArc(
            color = accent.copy(alpha = if (i == 2) 0.16f else 0.12f),
            startAngle = -55f,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = Offset(cx - r, cy - r),
            size = Size(r * 2, r * 2),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = stroke,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            ),
        )
    }
}

/**
 * Trophy silhouette — classic chalice. Was a pill-shaped mug with stroked
 * outline circles for handles which the user said "не очень понятно".
 * New version: tapered chalice cup + filled C-shaped handles + narrow
 * stem + 2-tier base + a small star on the cup face. Reads instantly
 * as "trophy".
 */
private fun DrawScope.drawTrophy(origin: Offset, area: Size, accent: Color) {
    val color   = accent.copy(alpha = 0.13f)
    val accent1 = accent.copy(alpha = 0.18f)

    // Constrain to a SQUARE sub-area sized to the smaller dimension —
    // otherwise on wide tiles (e.g. SkillRatingTile, ~340×160dp) the
    // trophy stretched horizontally into a giant chalice. Anchor to the
    // right edge inside the watermark area.
    val side = area.width.coerceAtMost(area.height) * 0.85f
    val originX = origin.x + (area.width - side) - side * 0.05f
    val originY = origin.y + (area.height - side) / 2f
    val cx = originX + side / 2f

    val cupTopY  = originY + side * 0.06f
    val cupBotY  = originY + side * 0.55f
    val stemBotY = originY + side * 0.72f
    val baseMidY = originY + side * 0.82f
    val baseBotY = originY + side * 0.92f

    val cupTopW = side * 0.62f
    val cupBotW = side * 0.30f
    val stemW   = side * 0.16f
    val baseW1  = side * 0.54f
    val baseW2  = side * 0.72f

    // Cup — chalice trapezoid (wider at top, taper to stem)
    val cupPath = Path().apply {
        moveTo(cx - cupTopW / 2f, cupTopY)
        lineTo(cx + cupTopW / 2f, cupTopY)
        lineTo(cx + cupBotW / 2f, cupBotY)
        lineTo(cx - cupBotW / 2f, cupBotY)
        close()
    }
    drawPath(cupPath, color)

    // Top rim band — slightly stronger so the opening reads
    drawRect(
        color = accent1,
        topLeft = Offset(cx - cupTopW / 2f, cupTopY),
        size = Size(cupTopW, side * 0.05f)
    )

    // Two C-shaped handles flanking the upper cup
    val handleR  = side * 0.16f
    val handleCY = originY + side * 0.22f
    val handleW  = handleR * 0.40f
    drawArc(
        color = color,
        startAngle = 60f, sweepAngle = 240f, useCenter = false,
        topLeft = Offset(cx - cupTopW / 2f - handleR, handleCY - handleR),
        size = Size(handleR * 2f, handleR * 2f),
        style = Stroke(width = handleW, cap = StrokeCap.Round)
    )
    drawArc(
        color = color,
        startAngle = -120f, sweepAngle = 240f, useCenter = false,
        topLeft = Offset(cx + cupTopW / 2f - handleR, handleCY - handleR),
        size = Size(handleR * 2f, handleR * 2f),
        style = Stroke(width = handleW, cap = StrokeCap.Round)
    )

    // Star on the cup face — final clincher that "this is a trophy"
    drawStar(
        center = Offset(cx, originY + side * 0.30f),
        outerR = side * 0.08f,
        innerR = side * 0.034f,
        color = accent.copy(alpha = 0.22f)
    )

    // Stem
    drawRect(
        color = color,
        topLeft = Offset(cx - stemW / 2f, cupBotY),
        size = Size(stemW, stemBotY - cupBotY)
    )

    // Two-tier base — premium pedestal feel
    drawRect(
        color = color,
        topLeft = Offset(cx - baseW1 / 2f, stemBotY),
        size = Size(baseW1, baseMidY - stemBotY)
    )
    drawRect(
        color = accent1,
        topLeft = Offset(cx - baseW2 / 2f, baseMidY),
        size = Size(baseW2, baseBotY - baseMidY)
    )
}

/** 5-point star polygon. */
private fun DrawScope.drawStar(
    center: Offset,
    outerR: Float,
    innerR: Float,
    color: Color
) {
    val path = Path()
    val steps = 10
    for (i in 0 until steps) {
        val r = if (i % 2 == 0) outerR else innerR
        val angle = Math.PI / 2.0 - i * (Math.PI / 5.0)
        val x = center.x + (r * Math.cos(angle)).toFloat()
        val y = center.y - (r * Math.sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
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
// Each watermark uses a DELIBERATELY different visual structure so the four
// pages don't blur together: LESSON = horizontal layered composition,
// BOOK ↦ drawBookStack (existing, vertical), FLASHCARD_SET = single bold
// rotated focal element, WEAK_WORD = vertical bar rhythm. Keeps the eye
// from reading them as "the same illustrator drew all four".

/**
 * LESSON — open-book composition: two page panels meeting at a centre spine,
 * left page has a few horizontal text rules, right page has two short angled
 * "diagram" strokes. Calligraphic, academic.
 */
private fun DrawScope.drawGradCap(origin: Offset, area: Size, accent: Color) {
    val side = area.width.coerceAtMost(area.height)
    val pageW = side * 0.32f
    val pageH = side * 0.65f
    val cx = origin.x + area.width * 0.55f
    val cy = origin.y + area.height * 0.55f
    val r  = pageW * 0.10f

    // Two pages — leaning slightly outward to suggest an open spread.
    val gap = pageW * 0.04f
    drawRoundRect(
        color = accent.copy(alpha = 0.13f),
        topLeft = Offset(cx - pageW - gap, cy - pageH / 2f),
        size = Size(pageW, pageH),
        cornerRadius = CornerRadius(r, r)
    )
    drawRoundRect(
        color = accent.copy(alpha = 0.10f),
        topLeft = Offset(cx + gap, cy - pageH / 2f),
        size = Size(pageW, pageH),
        cornerRadius = CornerRadius(r, r)
    )
    // Spine — thin vertical strip in the middle.
    drawRect(
        color = accent.copy(alpha = 0.20f),
        topLeft = Offset(cx - gap, cy - pageH / 2f),
        size = Size(gap * 2f, pageH)
    )
    // Text rulings on the LEFT page (3 lines).
    val lineCol = accent.copy(alpha = 0.16f)
    repeat(3) { i ->
        val ly = cy - pageH * 0.25f + i * pageH * 0.18f
        drawLine(
            color = lineCol,
            start = Offset(cx - pageW - gap + pageW * 0.15f, ly),
            end   = Offset(cx - gap - pageW * 0.10f, ly),
            strokeWidth = pageH * 0.025f,
            cap = StrokeCap.Round
        )
    }
    // Two short angled marks on RIGHT page — like a sketched diagram.
    val sketchCol = accent.copy(alpha = 0.18f)
    drawLine(
        color = sketchCol,
        start = Offset(cx + pageW * 0.35f, cy + pageH * 0.05f),
        end   = Offset(cx + pageW * 0.75f, cy - pageH * 0.20f),
        strokeWidth = pageH * 0.03f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = sketchCol,
        start = Offset(cx + pageW * 0.40f, cy + pageH * 0.20f),
        end   = Offset(cx + pageW * 0.85f, cy + pageH * 0.10f),
        strokeWidth = pageH * 0.03f,
        cap = StrokeCap.Round
    )
}

/**
 * FLASHCARD_SET — single bold tilted card with a corner-fold + ghost cards
 * peeking from behind. Strong central focal point (was: 5 fanned cards which
 * read as a brown blob).
 */
private fun DrawScope.drawCardStack(origin: Offset, area: Size, accent: Color) {
    val side = area.width.coerceAtMost(area.height)
    val w = side * 0.62f
    val h = side * 0.78f
    val cx = origin.x + area.width * 0.55f
    val cy = origin.y + area.height * 0.55f
    // Two ghost cards behind, tilted opposite ways, very faint.
    fun rotatedCard(deg: Float, alpha: Float, scale: Float = 1f) {
        val rad = Math.toRadians(deg.toDouble())
        val cos = Math.cos(rad).toFloat()
        val sin = Math.sin(rad).toFloat()
        val hw = w * scale / 2f; val hh = h * scale / 2f
        fun pt(dx: Float, dy: Float) = Offset(
            cx + dx * cos - dy * sin,
            cy + dx * sin + dy * cos
        )
        val card = Path().apply {
            moveTo(pt(-hw, -hh).x, pt(-hw, -hh).y)
            lineTo(pt(hw, -hh).x, pt(hw, -hh).y)
            lineTo(pt(hw, hh).x, pt(hw, hh).y)
            lineTo(pt(-hw, hh).x, pt(-hw, hh).y)
            close()
        }
        drawPath(card, accent.copy(alpha = alpha))
    }
    rotatedCard(-22f, 0.06f, scale = 0.92f)
    rotatedCard(14f,  0.09f, scale = 0.96f)

    // Foreground card — bold, slight tilt for energy.
    val fgRad = Math.toRadians(-7.0)
    val fgCos = Math.cos(fgRad).toFloat()
    val fgSin = Math.sin(fgRad).toFloat()
    fun pt(dx: Float, dy: Float) = Offset(
        cx + dx * fgCos - dy * fgSin,
        cy + dx * fgSin + dy * fgCos
    )
    val hw = w / 2f; val hh = h / 2f
    val foldSize = w * 0.24f

    // Card body MINUS top-right corner (so the fold flap shows clearly).
    val card = Path().apply {
        moveTo(pt(-hw, -hh).x, pt(-hw, -hh).y)
        lineTo(pt(hw - foldSize, -hh).x, pt(hw - foldSize, -hh).y)
        lineTo(pt(hw, -hh + foldSize).x, pt(hw, -hh + foldSize).y)
        lineTo(pt(hw, hh).x, pt(hw, hh).y)
        lineTo(pt(-hw, hh).x, pt(-hw, hh).y)
        close()
    }
    drawPath(card, accent.copy(alpha = 0.16f))

    // The corner-fold triangle itself, slightly darker — adds depth.
    val fold = Path().apply {
        moveTo(pt(hw - foldSize, -hh).x, pt(hw - foldSize, -hh).y)
        lineTo(pt(hw, -hh + foldSize).x, pt(hw, -hh + foldSize).y)
        lineTo(pt(hw - foldSize * 0.4f, -hh + foldSize * 0.4f).x,
               pt(hw - foldSize * 0.4f, -hh + foldSize * 0.4f).y)
        close()
    }
    drawPath(fold, accent.copy(alpha = 0.22f))

    // Two thin lines on the card face — "Q / A" hint.
    val lineCol = accent.copy(alpha = 0.20f)
    drawLine(
        color = lineCol,
        start = pt(-hw * 0.45f, -hh * 0.20f),
        end   = pt( hw * 0.30f, -hh * 0.20f),
        strokeWidth = h * 0.025f, cap = StrokeCap.Round
    )
    drawLine(
        color = lineCol,
        start = pt(-hw * 0.45f,  hh * 0.10f),
        end   = pt( hw * 0.10f,  hh * 0.10f),
        strokeWidth = h * 0.025f, cap = StrokeCap.Round
    )
}

/**
 * WEAK_WORD — equalizer / sound-bar composition: 7 vertical bars of varying
 * heights, taller in the middle, with rounded tops. Reads as "pronouncing /
 * speaking aloud". Replaces the previous radar-ripple which was too
 * symmetrical and similar to the bullseye motif used elsewhere.
 */
private fun DrawScope.drawFlame(origin: Offset, area: Size, accent: Color) {
    val barCount = 7
    val totalW = area.width * 0.78f
    val gap = totalW * 0.05f
    val barW = (totalW - gap * (barCount - 1)) / barCount
    val baseY = origin.y + area.height * 0.92f
    val maxBarH = area.height * 0.78f

    // Heights follow a smooth bell curve — peak in the middle.
    val heightFactors = listOf(0.30f, 0.55f, 0.85f, 1.00f, 0.78f, 0.48f, 0.22f)
    val alphas         = listOf(0.10f, 0.13f, 0.17f, 0.22f, 0.18f, 0.14f, 0.10f)

    val startX = origin.x + area.width * 0.55f - totalW / 2f
    repeat(barCount) { i ->
        val barH = maxBarH * heightFactors[i]
        val left = startX + i * (barW + gap)
        drawRoundRect(
            color = accent.copy(alpha = alphas[i]),
            topLeft = Offset(left, baseY - barH),
            size = Size(barW, barH),
            cornerRadius = CornerRadius(barW * 0.45f, barW * 0.45f)
        )
    }
    // Subtle "ground" line under the bars.
    drawLine(
        color = accent.copy(alpha = 0.10f),
        start = Offset(startX, baseY + area.height * 0.02f),
        end   = Offset(startX + totalW, baseY + area.height * 0.02f),
        strokeWidth = area.height * 0.012f
    )
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
 * BLOCK_ROCKET — restrained ascent.
 * Off-centre soft sun, single elegant cubic-Bezier trajectory rising into it,
 * and three small fading sparks. Less is more.
 */
private fun DrawScope.drawRocket(origin: Offset, area: Size, accent: Color) {
    val ox = origin.x; val oy = origin.y
    val w = area.width; val h = area.height
    val cx = ox + w * 0.75f
    val cy = oy + h * 0.40f
    val r = w.coerceAtMost(h) * 0.40f

    // Single subtle disc — no double layer
    drawCircle(accent.copy(alpha = 0.08f), r * 0.95f, Offset(cx, cy))

    // One elegant Bezier arc sweeping up into the disc
    val path = Path().apply {
        moveTo(ox + w * 0.04f, oy + h * 0.96f)
        cubicTo(
            ox + w * 0.30f, oy + h * 0.55f,
            ox + w * 0.55f, oy + h * 0.30f,
            ox + w * 0.92f, oy + h * 0.18f
        )
    }
    drawPath(path, accent.copy(alpha = 0.10f),
        style = Stroke(width = w * 0.014f, cap = StrokeCap.Round))

    // Three sparse stars — descending alpha
    listOf(
        Offset(ox + w * 0.18f, oy + h * 0.22f) to 0.12f,
        Offset(ox + w * 0.42f, oy + h * 0.14f) to 0.10f,
        Offset(ox + w * 0.55f, oy + h * 0.42f) to 0.08f
    ).forEachIndexed { i, (pt, a) ->
        drawCircle(accent.copy(alpha = a), w * (0.013f - i * 0.002f), pt)
    }
}

/**
 * BLOCK_HOME — quiet orbits.
 * Off-centre composition: two concentric rings plus a single small satellite.
 * No meridians, no extra dots — restrained.
 */
private fun DrawScope.drawHouse(origin: Offset, area: Size, accent: Color) {
    val cx = origin.x + area.width * 0.72f
    val cy = origin.y + area.height * 0.58f
    val maxR = area.width.coerceAtMost(area.height) * 0.42f
    val sw = maxR * 0.035f

    // Two soft orbital rings
    drawCircle(accent.copy(alpha = 0.07f), maxR,         Offset(cx, cy), style = Stroke(sw))
    drawCircle(accent.copy(alpha = 0.10f), maxR * 0.62f, Offset(cx, cy), style = Stroke(sw))

    // Single soft core
    drawCircle(accent.copy(alpha = 0.12f), maxR * 0.18f, Offset(cx, cy))

    // One small satellite drifting on the outer orbit
    val ang = Math.toRadians(-35.0)
    drawCircle(
        accent.copy(alpha = 0.11f),
        maxR * 0.07f,
        Offset(
            cx + maxR * Math.cos(ang).toFloat(),
            cy + maxR * Math.sin(ang).toFloat()
        )
    )
}

/**
 * BLOCK_LIGHTNING — quiet speed.
 * One long Bezier sweep, three short curved echoes beneath, one off-centre
 * chevron. Hard angles softened into curves.
 */
private fun DrawScope.drawLightning(origin: Offset, area: Size, accent: Color) {
    val ox = origin.x; val oy = origin.y
    val w = area.width; val h = area.height

    // Main sweep — single cubic Bezier
    val sweep = Path().apply {
        moveTo(ox + w * 0.05f, oy + h * 0.85f)
        cubicTo(
            ox + w * 0.35f, oy + h * 0.55f,
            ox + w * 0.55f, oy + h * 0.40f,
            ox + w * 0.95f, oy + h * 0.18f
        )
    }
    drawPath(sweep, accent.copy(alpha = 0.12f),
        style = Stroke(width = h * 0.022f, cap = StrokeCap.Round))

    // Three shorter curved echoes — varying alpha, all Bezier
    listOf(
        Triple(0.10f, 0.18f, 0.08f),
        Triple(0.08f, 0.12f, 0.06f),
        Triple(0.06f, 0.10f, 0.05f)
    ).forEachIndexed { i, (alpha, _, _) ->
        val yShift = h * (0.12f + i * 0.10f)
        val echo = Path().apply {
            moveTo(ox + w * (0.20f + i * 0.04f), oy + h * 0.92f - yShift * 0.2f)
            cubicTo(
                ox + w * 0.45f, oy + h * 0.78f - yShift * 0.3f,
                ox + w * 0.65f, oy + h * 0.65f - yShift * 0.3f,
                ox + w * 0.92f, oy + h * 0.42f - yShift * 0.2f
            )
        }
        drawPath(echo, accent.copy(alpha = alpha),
            style = Stroke(width = h * 0.014f, cap = StrokeCap.Round))
    }

    // One off-centre chevron — quiet stamp
    val chvX = ox + w * 0.78f
    val chvY = oy + h * 0.55f
    val chvSize = h * 0.16f
    val chevron = Path().apply {
        moveTo(chvX - chvSize * 0.5f, chvY - chvSize)
        lineTo(chvX + chvSize * 0.35f, chvY)
        lineTo(chvX - chvSize * 0.5f, chvY + chvSize)
    }
    drawPath(chevron, accent.copy(alpha = 0.10f),
        style = Stroke(width = h * 0.020f, cap = StrokeCap.Round))
}

/**
 * BLOCK_MOUNTAIN — gentle horizon.
 * One off-centre soft moon, two flowing Bezier ridges (back + foreground)
 * instead of four sharp triangles. Restrained.
 */
private fun DrawScope.drawMountain(origin: Offset, area: Size, accent: Color) {
    val ox = origin.x; val oy = origin.y
    val w = area.width; val h = area.height
    val baseY = oy + h * 0.92f

    // Single soft moon, off-centre
    drawCircle(accent.copy(alpha = 0.07f), h * 0.22f, Offset(ox + w * 0.78f, oy + h * 0.20f))

    // Background ridge — wide gentle Bezier curve
    val backRidge = Path().apply {
        moveTo(ox - w * 0.05f, baseY)
        cubicTo(
            ox + w * 0.20f, oy + h * 0.45f,
            ox + w * 0.55f, oy + h * 0.30f,
            ox + w * 1.05f, baseY
        )
        close()
    }
    drawPath(backRidge, accent.copy(alpha = 0.08f))

    // Foreground ridge — closer, lower, slightly off-centre right
    val frontRidge = Path().apply {
        moveTo(ox - w * 0.05f, baseY)
        cubicTo(
            ox + w * 0.30f, oy + h * 0.62f,
            ox + w * 0.70f, oy + h * 0.55f,
            ox + w * 1.05f, baseY
        )
        close()
    }
    drawPath(frontRidge, accent.copy(alpha = 0.12f))
}

// ─── Path-to-Madrid city glyphs ────────────────────────────────
// Mini icons (~24dp Canvas) drawn on the Path tile in profile.
// Each helper draws inside a square of `size` centred on `center`.
// `filled = true` paints solid for current/passed cities; outline otherwise.

private fun DrawScope.cityStrokeWidth(s: Float) = s * 0.10f

/** 1. Aldea perdida — small house: triangular roof + square wall. */
fun DrawScope.drawCityHouse(center: Offset, size: Float, color: Color, filled: Boolean) {
    val s = size
    val sw = cityStrokeWidth(s)
    val style = if (filled) androidx.compose.ui.graphics.drawscope.Fill else Stroke(sw)
    val roof = Path().apply {
        moveTo(center.x - s * 0.45f, center.y - s * 0.05f)
        lineTo(center.x,              center.y - s * 0.45f)
        lineTo(center.x + s * 0.45f, center.y - s * 0.05f)
        close()
    }
    drawPath(roof, color, style = style)
    drawRect(
        color = color,
        topLeft = Offset(center.x - s * 0.32f, center.y - s * 0.05f),
        size = Size(s * 0.64f, s * 0.45f),
        style = style
    )
}

/** 2. Santiago de Compostela — cathedral: 2 towers + dome between. */
fun DrawScope.drawCityCathedral(center: Offset, size: Float, color: Color, filled: Boolean) {
    val s = size
    val sw = cityStrokeWidth(s)
    val style = if (filled) androidx.compose.ui.graphics.drawscope.Fill else Stroke(sw)
    val towerW = s * 0.18f
    val towerH = s * 0.55f
    val baseY = center.y + s * 0.40f
    // Left tower
    drawRect(color, Offset(center.x - s * 0.42f, baseY - towerH), Size(towerW, towerH), style = style)
    // Right tower
    drawRect(color, Offset(center.x + s * 0.42f - towerW, baseY - towerH), Size(towerW, towerH), style = style)
    // Dome (semicircle) in the middle
    val domeR = s * 0.22f
    drawArc(
        color = color,
        startAngle = 180f, sweepAngle = 180f, useCenter = true,
        topLeft = Offset(center.x - domeR, center.y - s * 0.05f - domeR),
        size = Size(domeR * 2f, domeR * 2f),
        style = style
    )
    // Connecting base
    drawRect(color, Offset(center.x - s * 0.42f, baseY - sw * 0.6f), Size(s * 0.84f, sw * 0.6f), style = androidx.compose.ui.graphics.drawscope.Fill)
}

/** 3. Bilbao — anchor: vertical shaft + crossbar + bottom arc. */
fun DrawScope.drawCityAnchor(center: Offset, size: Float, color: Color, filled: Boolean) {
    val s = size
    val sw = cityStrokeWidth(s) * (if (filled) 1.4f else 1f)
    // Vertical shaft
    drawLine(color, Offset(center.x, center.y - s * 0.40f), Offset(center.x, center.y + s * 0.30f),
        strokeWidth = sw, cap = StrokeCap.Round)
    // Crossbar near top
    drawLine(color, Offset(center.x - s * 0.25f, center.y - s * 0.25f),
        Offset(center.x + s * 0.25f, center.y - s * 0.25f),
        strokeWidth = sw, cap = StrokeCap.Round)
    // Top ring
    drawCircle(color, s * 0.08f, Offset(center.x, center.y - s * 0.42f),
        style = if (filled) androidx.compose.ui.graphics.drawscope.Fill else Stroke(sw * 0.7f))
    // Bottom arc (U-shape)
    val arcR = s * 0.30f
    drawArc(
        color = color,
        startAngle = 20f, sweepAngle = 140f, useCenter = false,
        topLeft = Offset(center.x - arcR, center.y - arcR + s * 0.20f),
        size = Size(arcR * 2f, arcR * 2f),
        style = Stroke(sw, cap = StrokeCap.Round)
    )
}

/** 4. Zaragoza — bridge: 3 arches in a row. */
fun DrawScope.drawCityBridge(center: Offset, size: Float, color: Color, filled: Boolean) {
    val s = size
    val sw = cityStrokeWidth(s) * (if (filled) 1.3f else 1f)
    val baseY = center.y + s * 0.30f
    // Deck (top line)
    drawLine(color, Offset(center.x - s * 0.45f, center.y - s * 0.20f),
        Offset(center.x + s * 0.45f, center.y - s * 0.20f),
        strokeWidth = sw, cap = StrokeCap.Round)
    // Three arches
    val archW = s * 0.30f
    val archH = s * 0.45f
    repeat(3) { i ->
        val ax = center.x - s * 0.45f + i * archW
        drawArc(
            color = color,
            startAngle = 180f, sweepAngle = 180f, useCenter = false,
            topLeft = Offset(ax, baseY - archH),
            size = Size(archW, archH),
            style = Stroke(sw * 0.8f, cap = StrokeCap.Round)
        )
    }
    // Base line
    drawLine(color, Offset(center.x - s * 0.48f, baseY),
        Offset(center.x + s * 0.48f, baseY),
        strokeWidth = sw * 0.6f, cap = StrokeCap.Round)
}

/** 5. Valencia — orange: circle + small leaf-triangle on top. */
fun DrawScope.drawCityOrange(center: Offset, size: Float, color: Color, filled: Boolean) {
    val s = size
    val sw = cityStrokeWidth(s)
    val r = s * 0.36f
    val cy = center.y + s * 0.05f
    drawCircle(color, r, Offset(center.x, cy),
        style = if (filled) androidx.compose.ui.graphics.drawscope.Fill else Stroke(sw))
    // Leaf
    val leaf = Path().apply {
        moveTo(center.x - s * 0.05f, cy - r)
        lineTo(center.x + s * 0.18f, cy - r - s * 0.18f)
        lineTo(center.x + s * 0.05f, cy - r + s * 0.02f)
        close()
    }
    drawPath(leaf, color, style = if (filled) androidx.compose.ui.graphics.drawscope.Fill else Stroke(sw * 0.8f))
}

/** 6. Sevilla — Giralda tower: tall narrow rectangle + flag/finial on top. */
fun DrawScope.drawCityGiralda(center: Offset, size: Float, color: Color, filled: Boolean) {
    val s = size
    val sw = cityStrokeWidth(s)
    val style = if (filled) androidx.compose.ui.graphics.drawscope.Fill else Stroke(sw)
    val towerW = s * 0.28f
    val towerH = s * 0.70f
    val baseY = center.y + s * 0.40f
    drawRect(color, Offset(center.x - towerW / 2f, baseY - towerH),
        Size(towerW, towerH), style = style)
    // Flag pole
    drawLine(color, Offset(center.x, baseY - towerH),
        Offset(center.x, baseY - towerH - s * 0.20f),
        strokeWidth = sw * 0.7f, cap = StrokeCap.Round)
    // Flag (small triangle)
    val flag = Path().apply {
        moveTo(center.x, baseY - towerH - s * 0.20f)
        lineTo(center.x + s * 0.22f, baseY - towerH - s * 0.13f)
        lineTo(center.x, baseY - towerH - s * 0.06f)
        close()
    }
    drawPath(flag, color, style = androidx.compose.ui.graphics.drawscope.Fill)
}

/** 7. Barcelona — Sagrada Familia: 4 sharp narrow spires. */
fun DrawScope.drawCitySagrada(center: Offset, size: Float, color: Color, filled: Boolean) {
    val s = size
    val sw = cityStrokeWidth(s)
    val style = if (filled) androidx.compose.ui.graphics.drawscope.Fill else Stroke(sw * 0.8f)
    val baseY = center.y + s * 0.40f
    val spireWidth = s * 0.12f
    val heights = listOf(0.55f, 0.80f, 0.70f, 0.45f)
    val xs = listOf(-0.36f, -0.12f, 0.12f, 0.36f)
    heights.forEachIndexed { i, hf ->
        val cx = center.x + s * xs[i]
        val h = s * hf
        val spire = Path().apply {
            moveTo(cx - spireWidth / 2f, baseY)
            lineTo(cx + spireWidth / 2f, baseY)
            lineTo(cx, baseY - h)
            close()
        }
        drawPath(spire, color, style = style)
    }
}

/** 8. Madrid — crown: 3 peaks of varying height + base bar. */
fun DrawScope.drawCityCrown(center: Offset, size: Float, color: Color, filled: Boolean) {
    val s = size
    val sw = cityStrokeWidth(s)
    val style = if (filled) androidx.compose.ui.graphics.drawscope.Fill else Stroke(sw * 0.8f)
    val baseY = center.y + s * 0.30f
    val baseH = s * 0.18f
    // Crown body (peaks)
    val crown = Path().apply {
        moveTo(center.x - s * 0.45f, baseY)
        lineTo(center.x - s * 0.45f, center.y - s * 0.10f)
        lineTo(center.x - s * 0.25f, center.y + s * 0.05f)
        lineTo(center.x - s * 0.05f, center.y - s * 0.30f)
        lineTo(center.x + s * 0.15f, center.y + s * 0.05f)
        lineTo(center.x + s * 0.35f, center.y - s * 0.20f)
        lineTo(center.x + s * 0.45f, center.y + s * 0.05f)
        lineTo(center.x + s * 0.45f, baseY)
        close()
    }
    drawPath(crown, color, style = style)
    // Base bar
    drawRect(color, Offset(center.x - s * 0.48f, baseY),
        Size(s * 0.96f, baseH),
        style = if (filled) androidx.compose.ui.graphics.drawscope.Fill else Stroke(sw * 0.8f))
    // Three jewels at peak tips (only if filled)
    if (filled) {
        drawCircle(color, s * 0.05f, Offset(center.x - s * 0.05f, center.y - s * 0.30f))
        drawCircle(color, s * 0.05f, Offset(center.x + s * 0.35f, center.y - s * 0.20f))
        drawCircle(color, s * 0.05f, Offset(center.x - s * 0.45f, center.y - s * 0.10f))
    }
}

// ─── Trophy backdrop variant for Path tile ─────────────────────

/**
 * Large, very-low-alpha trophy occupying the upper-left quadrant of the
 * canvas. Used as a backdrop for the Path-to-Madrid tile so it doesn't
 * collide with the city glyphs lined up across the bottom.
 */
@Composable
fun PathTileTrophyBackdrop(accent: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        // Position: upper-left, sized big enough to feel ambient.
        val areaW = w * 0.55f
        val areaH = h * 0.85f
        val origin = Offset(-areaW * 0.10f, -areaH * 0.05f)
        val area = Size(areaW, areaH)
        drawTrophyFaint(origin, area, accent)
    }
}

private fun DrawScope.drawTrophyFaint(origin: Offset, area: Size, accent: Color) {
    val color = accent.copy(alpha = 0.06f)
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
    drawCircle(color, handleR, Offset(cupRect.left - handleR * 0.4f, cupTop + cupH * 0.35f),
        style = Stroke(handleR * 0.45f))
    drawCircle(color, handleR, Offset(cupRect.right + handleR * 0.4f, cupTop + cupH * 0.35f),
        style = Stroke(handleR * 0.45f))
    drawRect(color, Offset(cx - cupW * 0.10f, cupTop + cupH),
        Size(cupW * 0.20f, area.height * 0.10f))
    drawRect(color, Offset(cx - baseW / 2f, baseY),
        Size(baseW, area.height * 0.06f))
}
