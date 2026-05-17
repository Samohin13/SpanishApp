package com.spanishapp.radio.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spanishapp.radio.player.RadioPlayerController
import dagger.hilt.android.EntryPointAccessors

private val Accent = Color(0xFFFF5722)
private val Green = Color(0xFF4CAF50)

/**
 * Карточка-приглашение на экране Home.
 * Два состояния:
 *  - Ничего не играет → «📻 Послушай испанское радио»
 *  - Играет → «🟢 LIVE · Cadena SER · 88.7» с pulse-точкой
 * Тап → переход на экран радио.
 */
@Composable
fun HomeRadioCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val player = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            RadioPlayerEntryPoint::class.java,
        ).radioPlayer()
    }
    val station by player.currentStation.collectAsState()
    val isPlaying by player.isPlaying.collectAsState()

    val nowPlaying = station != null && isPlaying

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (nowPlaying)
            Accent.copy(alpha = 0.12f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(
            width = if (nowPlaying) 1.5.dp else 0.5.dp,
            color = if (nowPlaying) Accent.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Big radio icon with gradient
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Accent, Accent.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("📻", fontSize = 24.sp)
            }
            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (nowPlaying) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // pulsing LIVE dot
                        val transition = rememberInfiniteTransition(label = "pulse")
                        val alpha by transition.animateFloat(
                            initialValue = 1f, targetValue = 0.3f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(900, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse,
                            ),
                            label = "pulse_alpha",
                        )
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Green.copy(alpha = alpha))
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "LIVE СЕЙЧАС",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Green,
                            letterSpacing = 1.2.sp,
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        station?.name ?: "—",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                    Text(
                        "%.1f MHz · ${station?.country?.displayName ?: ""}".format(station?.frequency ?: 0f),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        "Радио на испанском",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Слушай живой эфир и учись по-настоящему",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Arrow / play indicator
            Text(
                "→",
                fontSize = 20.sp,
                color = Accent,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
