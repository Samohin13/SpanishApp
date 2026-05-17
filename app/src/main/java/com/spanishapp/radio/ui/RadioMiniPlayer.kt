package com.spanishapp.radio.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spanishapp.radio.player.RadioPlayerController
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

private val Accent = Color(0xFFFF5722)
private val Green = Color(0xFF4CAF50)

@EntryPoint
@InstallIn(SingletonComponent::class)
interface RadioPlayerEntryPoint {
    fun radioPlayer(): RadioPlayerController
}

/**
 * Mini-player в стиле Spotify/Apple Music — без резкой обводки.
 * Тонкая accent-полоска по верху, мягкие тени, компактный layout.
 *
 * v1.10.1: Material иконки (вместо ⏸/▶ эмодзи) + skip prev/next кнопки
 * для быстрого переключения станций не открывая полный экран.
 */
@Composable
fun RadioMiniPlayer(
    isOnRadioScreen: Boolean,
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
    val context_stations by player.stationContext.collectAsState()
    val canSkip = context_stations.size > 1

    val visible = station != null && !isOnRadioScreen

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0.0f to Accent.copy(alpha = 0.04f),
                        0.4f to MaterialTheme.colorScheme.surface,
                        1.0f to MaterialTheme.colorScheme.surface,
                    )
                )
                .clickable(onClick = onClick),
        ) {
            // Тонкая accent-линия сверху (1dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            0.0f to Color.Transparent,
                            0.5f to Accent.copy(alpha = 0.4f),
                            1.0f to Color.Transparent,
                        )
                    )
            )

            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Station artwork
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Accent.copy(alpha = 0.85f),
                                    Accent.copy(alpha = 0.55f),
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        station?.shortCode ?: "—",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        station?.name ?: "—",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isPlaying) {
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
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Green.copy(alpha = alpha))
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                "LIVE · ${station?.program ?: ""}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        } else {
                            Text(
                                station?.program ?: "—",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }
                }

                // Контролы: prev / play-pause / next (Material icons)
                if (canSkip) {
                    MiniControlButton(
                        icon = Icons.Filled.SkipPrevious,
                        cd = "Предыдущая",
                        onClick = { player.previousStation() },
                    )
                }
                MiniControlButton(
                    icon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    cd = if (isPlaying) "Пауза" else "Играть",
                    tint = Accent,
                    onClick = {
                        if (player.isPlaying.value) player.pause() else player.resume()
                    },
                )
                if (canSkip) {
                    MiniControlButton(
                        icon = Icons.Filled.SkipNext,
                        cd = "Следующая",
                        onClick = { player.nextStation() },
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniControlButton(
    icon: ImageVector,
    cd: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = cd,
            modifier = Modifier.size(24.dp),
            tint = tint,
        )
    }
}
