package com.spanishapp.radio.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spanishapp.radio.player.RadioPlayerController
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

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
    val hiddenBySwipe by player.miniPlayerHidden.collectAsState()

    val visible = station != null && !isOnRadioScreen && !hiddenBySwipe

    // Swipe-to-dismiss state. Threshold 30% ширины контейнера → закрыть радио.
    // Иначе snap-back к исходной позиции (легкий случайный жест не закрывает).
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    // Сбрасываем offset когда станция меняется (новый mini-player after stop+play)
    LaunchedEffect(station?.id) {
        if (station != null && offsetX.value != 0f) offsetX.snapTo(0f)
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val containerWidthPx = constraints.maxWidth.toFloat()
            val dismissThreshold = containerWidthPx * 0.30f
            // Fade text/elements по мере свайпа — визуальный отклик
            val swipeAlpha = 1f - (abs(offsetX.value) / containerWidthPx).coerceIn(0f, 0.7f)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .alpha(swipeAlpha)
                .pointerInput(containerWidthPx) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (abs(offsetX.value) >= dismissThreshold) {
                                    // СКРЫВАЕМ (не останавливаем!) — радио продолжает играть
                                    // в фоне, notification на месте. Mini-player вернётся
                                    // когда юзер откроет radio-экран (LaunchedEffect там
                                    // сбросит miniPlayerHidden в false).
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val targetX = if (offsetX.value > 0) containerWidthPx
                                                  else -containerWidthPx
                                    offsetX.animateTo(targetX, tween(durationMillis = 200))
                                    player.hideMiniPlayer()
                                    // Сбрасываем offset обратно к 0 чтобы при следующем
                                    // show'е mini-player был на правильном месте
                                    offsetX.snapTo(0f)
                                } else {
                                    // Snap back — лёгкий spring без haptic
                                    offsetX.animateTo(
                                        0f,
                                        spring(dampingRatio = 0.7f, stiffness = 400f),
                                    )
                                }
                            }
                        },
                        onHorizontalDrag = { change, delta ->
                            change.consume()
                            scope.launch { offsetX.snapTo(offsetX.value + delta) }
                        }
                    )
                }
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

            // v1.13.1: mini-player крупнее на планшете
            val isWide = com.spanishapp.ui.adaptive.isWideScreen()
            val playerPadH = if (isWide) 16.dp else 10.dp
            val playerPadV = if (isWide) 10.dp else 6.dp
            val artworkSize = if (isWide) 56.dp else 40.dp
            val artworkFont = if (isWide) 14.sp else 10.sp
            val titleFont = if (isWide) 16.sp else 13.sp
            val metaFont = if (isWide) 12.sp else 10.sp
            val dotSize = if (isWide) 7.dp else 5.dp

            Row(
                modifier = Modifier.padding(horizontal = playerPadH, vertical = playerPadV),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Station artwork
                Box(
                    modifier = Modifier
                        .size(artworkSize)
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
                        fontSize = artworkFont,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                }
                Spacer(Modifier.width(if (isWide) 14.dp else 10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        station?.name ?: "—",
                        fontSize = titleFont,
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
                                    .size(dotSize)
                                    .clip(CircleShape)
                                    .background(Green.copy(alpha = alpha))
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                "LIVE · ${station?.program ?: ""}",
                                fontSize = metaFont,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        } else {
                            Text(
                                station?.program ?: "—",
                                fontSize = metaFont,
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
        }  // close BoxWithConstraints
    }
}

@Composable
private fun MiniControlButton(
    icon: ImageVector,
    cd: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    // v1.13.1: mini-player кнопки крупнее на планшете
    val isWide = com.spanishapp.ui.adaptive.isWideScreen()
    val btnSize = if (isWide) 56.dp else 40.dp
    val iconSize = if (isWide) 32.dp else 24.dp
    Box(
        modifier = Modifier
            .size(btnSize)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = cd,
            modifier = Modifier.size(iconSize),
            tint = tint,
        )
    }
}
