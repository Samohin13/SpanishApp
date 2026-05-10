package com.spanishapp.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Thematic background watermark for the bento tiles on the home screen.
 *
 * Each theme draws a single, tile-appropriate vector illustration anchored
 * to the bottom-right corner at ~50% width and ~75% height, painted with
 * the tile's accent colour at very low alpha (0.10-0.14) so it reads as a
 * subtle decorative texture rather than a foreground element.
 *
 * Replaces the previous shared `SpanishCitiesWatermark`, which sat behind
 * the entire LazyColumn and produced a generic "city skyline" strip the
 * user wanted swapped for per-tile themes.
 */
enum class BentoTheme { BOOK, RATING, DICTIONARY, GOAL }

@Composable
fun BentoWatermark(
    theme: BentoTheme,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // Anchor watermark to bottom-right; takes ~55% of width, ~75% of height.
        val w = size.width
        val h = size.height
        val areaWidth  = w * 0.55f
        val areaHeight = h * 0.75f
        val originX    = w - areaWidth - 6f
        val originY    = h - areaHeight - 6f
        val area = Size(areaWidth, areaHeight)
        val origin = Offset(originX, originY)

        when (theme) {
            BentoTheme.BOOK       -> drawBookStack(origin, area, accent)
            BentoTheme.RATING     -> drawTrophy   (origin, area, accent)
            BentoTheme.DICTIONARY -> drawLensGlass(origin, area, accent)
            BentoTheme.GOAL       -> drawBullseye (origin, area, accent)
        }
    }
}

// ─── Drawings ───────────────────────────────────────────────────

/** Three offset rectangles forming a stacked-books motif. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBookStack(
    origin: Offset, area: Size, accent: Color
) {
    val color = accent.copy(alpha = 0.13f)
    val bookH = area.height / 4f
    val gap   = bookH * 0.3f

    // Three books, each slightly narrower and offset.
    repeat(3) { i ->
        val width  = area.width - i * area.width * 0.12f
        val left   = origin.x + i * area.width * 0.06f
        val top    = origin.y + (3 - i) * (bookH + gap) - bookH
        drawRect(
            color = color,
            topLeft = Offset(left, top + area.height - 3 * (bookH + gap)),
            size = Size(width, bookH)
        )
        // Spine line
        drawRect(
            color = accent.copy(alpha = 0.18f),
            topLeft = Offset(left, top + area.height - 3 * (bookH + gap)),
            size = Size(width * 0.04f, bookH)
        )
    }
}

/** A simple trophy silhouette — cup body, handles, base. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTrophy(
    origin: Offset, area: Size, accent: Color
) {
    val color = accent.copy(alpha = 0.13f)
    val cx = origin.x + area.width / 2f
    val cupTop  = origin.y + area.height * 0.10f
    val cupH    = area.height * 0.55f
    val cupW    = area.width * 0.55f
    val baseY   = origin.y + area.height * 0.85f
    val baseW   = cupW * 0.85f

    // Cup body (rounded rectangle approximation: rect + arcs)
    val cupRect = androidx.compose.ui.geometry.Rect(
        left   = cx - cupW / 2f,
        top    = cupTop,
        right  = cx + cupW / 2f,
        bottom = cupTop + cupH
    )
    val cupPath = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                cupRect,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cupW * 0.45f, cupH * 0.4f)
            )
        )
    }
    drawPath(cupPath, color)

    // Handles — two small circles either side
    val handleR = cupW * 0.18f
    drawCircle(color, handleR, Offset(cupRect.left - handleR * 0.4f, cupTop + cupH * 0.35f), style = Stroke(width = handleR * 0.45f))
    drawCircle(color, handleR, Offset(cupRect.right + handleR * 0.4f, cupTop + cupH * 0.35f), style = Stroke(width = handleR * 0.45f))

    // Stem
    drawRect(
        color = color,
        topLeft = Offset(cx - cupW * 0.10f, cupTop + cupH),
        size = Size(cupW * 0.20f, area.height * 0.10f)
    )
    // Base
    drawRect(
        color = color,
        topLeft = Offset(cx - baseW / 2f, baseY),
        size = Size(baseW, area.height * 0.06f)
    )
}

/** Magnifying glass — circle frame with diagonal handle. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLensGlass(
    origin: Offset, area: Size, accent: Color
) {
    val color = accent.copy(alpha = 0.14f)
    val r = area.width.coerceAtMost(area.height) * 0.35f
    val cx = origin.x + area.width * 0.45f
    val cy = origin.y + area.height * 0.40f

    // Glass frame
    drawCircle(color, r, Offset(cx, cy), style = Stroke(width = r * 0.18f))

    // Inner highlight (slightly brighter)
    drawCircle(
        accent.copy(alpha = 0.06f),
        r * 0.65f,
        Offset(cx, cy)
    )

    // Handle — diagonal stroke from circle edge to bottom-right
    val angle = Math.toRadians(45.0)
    val startX = cx + (r * Math.cos(angle)).toFloat()
    val startY = cy + (r * Math.sin(angle)).toFloat()
    val endX   = startX + r * 0.9f
    val endY   = startY + r * 0.9f
    drawLine(
        color = color,
        start = Offset(startX, startY),
        end   = Offset(endX, endY),
        strokeWidth = r * 0.22f,
        cap = androidx.compose.ui.graphics.StrokeCap.Round
    )
}

/** Bullseye / dartboard — concentric circles with a tiny dart in centre. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBullseye(
    origin: Offset, area: Size, accent: Color
) {
    val maxR = area.width.coerceAtMost(area.height) * 0.45f
    val cx = origin.x + area.width * 0.5f
    val cy = origin.y + area.height * 0.5f

    // 4 concentric rings — alpha alternates so they read as a target.
    val ringSteps = listOf(
        maxR        to accent.copy(alpha = 0.14f),
        maxR * 0.75f to accent.copy(alpha = 0.06f),
        maxR * 0.50f to accent.copy(alpha = 0.14f),
        maxR * 0.25f to accent.copy(alpha = 0.06f)
    )
    ringSteps.forEach { (radius, color) ->
        drawCircle(color, radius, Offset(cx, cy))
    }
    // Bullseye centre dot
    drawCircle(accent.copy(alpha = 0.22f), maxR * 0.10f, Offset(cx, cy))
}
