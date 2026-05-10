package com.spanishapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Stylised watermark of three Spanish-city skylines (Madrid / Barcelona / Sevilla)
 * drawn in the lower half of the parent. Uses very low alpha so it blends into
 * the background without competing with content.
 *
 * Pass an explicit [color] when overlaying onto a coloured surface (e.g. white
 * over a course-card gradient). Default uses the theme's onBackground at 5% alpha.
 */
@Composable
fun SpanishCitiesWatermark(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        // Three skylines spaced across the lower half.
        // Each skyline occupies ~30% width and sits along the bottom edge.
        val skylineHeight = (h * 0.18f).coerceAtMost(160f)
        val baseline = h * 0.92f

        drawSkylineMadrid(
            origin = Offset(w * 0.02f, baseline - skylineHeight),
            width = w * 0.32f,
            height = skylineHeight,
            color = color
        )
        drawSkylineBarcelona(
            origin = Offset(w * 0.36f, baseline - skylineHeight),
            width = w * 0.30f,
            height = skylineHeight,
            color = color
        )
        drawSkylineSevilla(
            origin = Offset(w * 0.68f, baseline - skylineHeight),
            width = w * 0.30f,
            height = skylineHeight,
            color = color
        )
    }
}

// Madrid — wide rectangular blocks + one central tower.
private fun DrawScope.drawSkylineMadrid(origin: Offset, width: Float, height: Float, color: Color) {
    val (ox, oy) = origin.x to origin.y
    val unit = width / 8f
    // ground line
    drawRect(color, topLeft = Offset(ox, oy + height * 0.95f), size = Size(width, height * 0.05f))
    // buildings (x-fraction, height-fraction)
    val blocks = listOf(
        0.00f to 0.55f,
        0.12f to 0.75f,
        0.26f to 0.45f,
        0.38f to 0.85f, // tower
        0.52f to 0.50f,
        0.66f to 0.65f,
        0.80f to 0.40f
    )
    blocks.forEach { (xFrac, hFrac) ->
        val bx = ox + width * xFrac
        val bw = unit * 0.95f
        val bh = height * hFrac
        drawRect(color, topLeft = Offset(bx, oy + height - bh), size = Size(bw, bh))
    }
    // small dome on the central tower
    val domeCx = ox + width * 0.38f + unit * 0.475f
    val domeCy = oy + height * 0.13f
    drawCircle(color, radius = unit * 0.55f, center = Offset(domeCx, domeCy))
}

// Barcelona — Sagrada-Família-ish: cluster of tall thin spires.
private fun DrawScope.drawSkylineBarcelona(origin: Offset, width: Float, height: Float, color: Color) {
    val (ox, oy) = origin.x to origin.y
    drawRect(color, topLeft = Offset(ox, oy + height * 0.95f), size = Size(width, height * 0.05f))
    val spireWidth = width / 14f
    val spires = listOf(
        0.10f to 0.70f,
        0.22f to 0.85f,
        0.34f to 0.95f,
        0.46f to 0.80f,
        0.58f to 0.65f,
        0.72f to 0.55f
    )
    spires.forEach { (xFrac, hFrac) ->
        val bx = ox + width * xFrac
        val bh = height * hFrac
        // body
        drawRect(color, topLeft = Offset(bx, oy + height - bh + spireWidth * 0.5f), size = Size(spireWidth, bh - spireWidth * 0.5f))
        // pointed tip via triangle path
        val tip = Path().apply {
            moveTo(bx, oy + height - bh + spireWidth * 0.6f)
            lineTo(bx + spireWidth / 2f, oy + height - bh - spireWidth * 0.4f)
            lineTo(bx + spireWidth, oy + height - bh + spireWidth * 0.6f)
            close()
        }
        drawPath(tip, color)
    }
}

// Sevilla — Giralda + low arched buildings.
private fun DrawScope.drawSkylineSevilla(origin: Offset, width: Float, height: Float, color: Color) {
    val (ox, oy) = origin.x to origin.y
    drawRect(color, topLeft = Offset(ox, oy + height * 0.95f), size = Size(width, height * 0.05f))
    // low buildings with rounded tops
    val lowBlocks = listOf(
        0.00f to 0.40f,
        0.18f to 0.55f,
        0.36f to 0.45f,
        0.70f to 0.50f,
        0.86f to 0.35f
    )
    val unit = width / 8f
    lowBlocks.forEach { (xFrac, hFrac) ->
        val bx = ox + width * xFrac
        val bh = height * hFrac
        val by = oy + height - bh
        drawRoundRect(
            color = color,
            topLeft = Offset(bx, by),
            size = Size(unit * 0.95f, bh),
            cornerRadius = CornerRadius(unit * 0.4f, unit * 0.4f)
        )
    }
    // Giralda tower (tall + small dome)
    val towerW = unit * 1.1f
    val towerH = height * 0.92f
    val towerX = ox + width * 0.52f
    drawRect(color, topLeft = Offset(towerX, oy + height - towerH), size = Size(towerW, towerH))
    drawCircle(color, radius = unit * 0.5f, center = Offset(towerX + towerW / 2f, oy + height - towerH - unit * 0.2f))
}
