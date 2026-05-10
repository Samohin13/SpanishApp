package com.spanishapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Stylised watermark of three Spanish-city skylines (Madrid / Barcelona / Sevilla)
 * docked to the BOTTOM of the parent in a fixed-height strip and softened with
 * a vertical gradient that fades the skyline tops into the background colour —
 * gives the illusion of a city horizon emerging from haze just above the
 * bottom nav bar.
 *
 * @param bgColor the colour of the screen behind this watermark — used to
 *   blend the top of the skyline into the surrounding canvas. Pass
 *   `MaterialTheme.colorScheme.background` from the calling composable.
 * @param stripHeight how tall the silhouette band is. Defaults to 130.dp.
 * @param color paint colour for the buildings themselves.
 */
@Composable
fun SpanishCitiesWatermark(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.10f),
    bgColor: Color = MaterialTheme.colorScheme.background,
    stripHeight: Dp = 130.dp
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(stripHeight)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val baseline = h * 0.98f
                val skylineHeight = h * 0.85f

                drawSkylineMadrid(
                    origin = Offset(w * 0.02f, baseline - skylineHeight),
                    width = w * 0.32f, height = skylineHeight, color = color
                )
                drawSkylineBarcelona(
                    origin = Offset(w * 0.36f, baseline - skylineHeight),
                    width = w * 0.30f, height = skylineHeight, color = color
                )
                drawSkylineSevilla(
                    origin = Offset(w * 0.68f, baseline - skylineHeight),
                    width = w * 0.30f, height = skylineHeight, color = color
                )

                // Vertical haze gradient on top of the skylines: bgColor at
                // the very top of the strip (full opacity) → transparent at
                // ~60% down. The skyline silhouettes fade in softly from
                // above instead of having a hard horizon line.
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(bgColor, Color.Transparent),
                        startY = 0f,
                        endY   = h * 0.60f
                    ),
                    size = Size(w, h)
                )
            }
        }
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
