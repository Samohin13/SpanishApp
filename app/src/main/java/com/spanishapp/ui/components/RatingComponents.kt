package com.spanishapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spanishapp.domain.algorithm.League
import com.spanishapp.domain.algorithm.LeagueResolver

// ═════════════════════════════════════════════════════════════
//  Spanish flag mastery rating (0–5 mini-flags)
//  Заполненные = трёхполосный флаг (красный/жёлтый/красный),
//  пустые = серый контур.
// ═════════════════════════════════════════════════════════════

private val FLAG_RED    = Color(0xFFC60B1E)
private val FLAG_YELLOW = Color(0xFFFFC400)
private val FLAG_EMPTY  = Color(0xFFD1D1D6)

@Composable
fun SpanishFlagRating(
    filled: Int,
    of: Int = 5,
    flagWidthDp: Int = 18,
    flagHeightDp: Int = 12,
    spacingDp: Int = 4,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacingDp.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(of) { idx ->
            SpanishFlag(
                filled = idx < filled,
                widthDp = flagWidthDp,
                heightDp = flagHeightDp
            )
        }
    }
}

@Composable
private fun SpanishFlag(filled: Boolean, widthDp: Int, heightDp: Int) {
    Canvas(modifier = Modifier.size(widthDp.dp, heightDp.dp)) {
        if (filled) {
            // 3 horizontal stripes: red 1/4, yellow 1/2, red 1/4
            val w = size.width
            val h = size.height
            val redH = h * 0.25f
            val yellowH = h * 0.50f
            drawRect(color = FLAG_RED,    topLeft = Offset(0f, 0f),                size = Size(w, redH))
            drawRect(color = FLAG_YELLOW, topLeft = Offset(0f, redH),              size = Size(w, yellowH))
            drawRect(color = FLAG_RED,    topLeft = Offset(0f, redH + yellowH),    size = Size(w, h - redH - yellowH))
            // namek na gerb — small circle in yellow stripe
            drawCircle(
                color = FLAG_RED.copy(alpha = 0.65f),
                radius = h * 0.08f,
                center = Offset(w * 0.32f, redH + yellowH * 0.5f)
            )
        } else {
            // empty outline
            drawRect(
                color = FLAG_EMPTY,
                topLeft = Offset(0f, 0f),
                size = Size(size.width, size.height),
                style = Stroke(width = 1.5f)
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════
//  League badge — пилюля с эмодзи + город + регион
// ═════════════════════════════════════════════════════════════

@Composable
fun LeagueBadge(
    league: League,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val accent = Color(league.accentColorHex)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(if (compact) 12.dp else 16.dp),
        color = accent.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 8.dp else 12.dp,
                vertical = if (compact) 4.dp else 8.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp)
        ) {
            Text(league.emoji, fontSize = if (compact) 14.sp else 18.sp)
            Column {
                Text(
                    league.city,
                    fontSize = if (compact) 12.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
                if (!compact) {
                    Text(
                        league.region,
                        fontSize = 10.sp,
                        color = accent.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
//  League promotion dialog — «¡Has llegado a {ciudad}!»
// ═════════════════════════════════════════════════════════════

@Composable
fun LeaguePromotionDialog(
    from: League,
    to: League,
    onDismiss: () -> Unit
) {
    val accent = Color(to.accentColorHex)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("¡Vamos!", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(to.emoji, fontSize = 56.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "¡Has llegado a ${to.city.substringBefore("—").trim()}!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accent
                )
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "${from.city}  →  ${to.city.substringBefore("—").trim()}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Регион: ${to.region}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = accent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "  +50 XP бонус за повышение!  ",
                        modifier = Modifier.padding(8.dp),
                        fontWeight = FontWeight.SemiBold,
                        color = accent
                    )
                }
            }
        }
    )
}

// ═════════════════════════════════════════════════════════════
//  Path-to-Madrid: горизонтальный список всех 8 ступеней
// ═════════════════════════════════════════════════════════════

@Composable
fun LeaguePath(
    currentTier: Int,
    peakTier: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LeagueResolver.LEAGUES.forEach { l ->
            val isCurrent = l.tier == currentTier
            val isPassed  = l.tier < currentTier || l.tier <= peakTier
            val color = when {
                isCurrent -> Color(l.accentColorHex)
                isPassed  -> Color(l.accentColorHex).copy(alpha = 0.55f)
                else      -> Color(0xFFD1D1D6)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(50))
                        .background(color.copy(alpha = if (isCurrent) 0.25f else 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (isPassed || isCurrent) l.emoji else "·",
                        fontSize = if (isCurrent) 18.sp else 14.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    l.tier.toString(),
                    fontSize = 9.sp,
                    color = color,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
