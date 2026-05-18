package com.spanishapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * v1.16.0: HintBadge — пилюля с 💡 N для отображения баланса Hint Bank.
 *
 * Используется в TopAppBar каждой игры и в stats row на HomeScreen.
 *
 * @param count Текущее количество подсказок
 * @param compact Если true — без рамки, для inline в strip stats.
 *                Если false — с оранжевой border, для TopAppBar.
 */
@Composable
fun HintBadge(
    count: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val orange = Color(0xFFFF6B35)
    val bg = orange.copy(alpha = 0.15f)

    val pillModifier = modifier
        .clip(RoundedCornerShape(14.dp))
        .background(bg)
        .let { if (!compact) it.border(1.5.dp, orange.copy(alpha = 0.5f), RoundedCornerShape(14.dp)) else it }
        .padding(horizontal = if (compact) 8.dp else 10.dp, vertical = if (compact) 4.dp else 5.dp)

    Row(
        modifier = pillModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            "💡",
            fontSize = if (compact) 12.sp else 14.sp,
        )
        Text(
            count.toString(),
            color = orange,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 12.sp else 14.sp,
        )
    }
}
