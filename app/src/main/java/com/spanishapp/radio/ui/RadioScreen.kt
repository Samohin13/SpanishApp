package com.spanishapp.radio.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.spanishapp.radio.data.CefrLevel
import com.spanishapp.radio.data.Country
import com.spanishapp.radio.player.HapticManager
import kotlin.math.abs
import kotlin.math.roundToInt

private val Accent = Color(0xFFFF5722)
private val Green  = Color(0xFF4CAF50)
private val Yellow = Color(0xFFFFC107)

@Composable
fun RadioScreen(navController: NavHostController) {
    val vm: RadioViewModel = viewModel()
    val context = LocalContext.current
    val haptic = remember { HapticManager(context) }

    val country by vm.country.collectAsState()
    val freq by vm.frequency.collectAsState()
    val station by vm.currentStation.collectAsState()
    val signal by vm.signal.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()
    val stations by vm.stations.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ─── Top bar ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
                Spacer(Modifier.width(4.dp))
                Text("Радио", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            // Type chip
            Row(modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 8.dp)) {
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = Accent.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Accent.copy(alpha = 0.3f)),
                ) {
                    Text(
                        "📻 LIVE FM",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            // Country selector
            CountrySelector(
                current = country,
                onSelect = vm::selectCountry,
                modifier = Modifier.padding(horizontal = 18.dp),
            )

            Spacer(Modifier.height(14.dp))

            // Display panel
            DisplayPanel(
                frequency = freq,
                station = station,
                signal = signal,
                modifier = Modifier.padding(horizontal = 18.dp),
            )

            Spacer(Modifier.height(12.dp))

            // Frequency dial
            FrequencyDial(
                currentFreq = freq,
                fmMin = vm.fmMin,
                fmMax = vm.fmMax,
                stationFreqs = stations.map { it.frequency },
                signal = signal,
                modifier = Modifier.padding(horizontal = 18.dp),
            )

            Spacer(Modifier.height(14.dp))

            // Tuner wheel
            TunerWheel(
                onScroll = { delta ->
                    vm.onScrollFrequency(delta)
                    haptic.tickLight()
                },
                onScrollStop = { vm.onScrollStop(); haptic.stationHit() },
                modifier = Modifier.padding(horizontal = 18.dp),
            )

            Spacer(Modifier.height(14.dp))

            // Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CtrlButton(text = "⏮", weight = 1f, onClick = { vm.previousStation(); haptic.stationHit() })
                CtrlButton(
                    text = if (isPlaying) "⏸" else "▶",
                    weight = 1.4f,
                    isPlay = true,
                    onClick = { vm.togglePlayback() }
                )
                CtrlButton(text = "⏭", weight = 1f, onClick = { vm.nextStation(); haptic.stationHit() })
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  CountrySelector
// ════════════════════════════════════════════════════════════════

@Composable
private fun CountrySelector(
    current: Country,
    onSelect: (Country) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Country.values().forEach { c ->
            val isActive = c == current
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = if (isActive) Accent.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = if (isActive)
                    androidx.compose.foundation.BorderStroke(1.5.dp, Accent.copy(alpha = 0.5f))
                else null,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(c) },
            ) {
                Text(
                    "${c.emoji} ${c.displayName}",
                    modifier = Modifier.padding(vertical = 9.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isActive) Accent else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  DisplayPanel
// ════════════════════════════════════════════════════════════════

@Composable
private fun DisplayPanel(
    frequency: Float,
    station: com.spanishapp.radio.data.Station?,
    signal: SignalStatus,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SignalPill(signal)
                Spacer(Modifier.weight(1f))
                SignalBars(signal)
                station?.let {
                    Spacer(Modifier.width(8.dp))
                    CefrBadge(it.level, signal != SignalStatus.NO_SIGNAL)
                }
            }
            Spacer(Modifier.height(10.dp))

            val freqColor = when (signal) {
                SignalStatus.ON_STATION -> Color.White
                SignalStatus.WEAK -> Color.White.copy(alpha = 0.7f)
                SignalStatus.NO_SIGNAL -> Color.White.copy(alpha = 0.35f)
            }
            Text(
                "%.1f".format(frequency),
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                color = freqColor,
                letterSpacing = (-2.5).sp,
            )
            Spacer(Modifier.height(6.dp))

            when (signal) {
                SignalStatus.ON_STATION -> {
                    Text(
                        station?.name ?: "—",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        station?.program ?: "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                SignalStatus.WEAK -> {
                    Text(
                        station?.name?.let { "…${it.take(3)}…" } ?: "…",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                    Text(
                        "приближаешься к станции",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                SignalStatus.NO_SIGNAL -> {
                    Text(
                        "🔇 нет сигнала",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.4f),
                    )
                    Text(
                        "крути дальше — поймаешь станцию",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SignalPill(signal: SignalStatus) {
    val (label, color) = when (signal) {
        SignalStatus.ON_STATION -> "LIVE" to Green
        SignalStatus.WEAK -> "WEAK" to Yellow
        SignalStatus.NO_SIGNAL -> "NO SIGNAL" to Color(0xFF888888)
    }
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = color.copy(alpha = 0.15f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Pulsing dot
            val transition = rememberInfiniteTransition(label = "pulse")
            val alpha by transition.animateFloat(
                initialValue = 1f, targetValue = 0.4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "pulse_alpha",
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = if (signal == SignalStatus.NO_SIGNAL) 0.5f else alpha))
            )
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun SignalBars(signal: SignalStatus) {
    val lit = when (signal) {
        SignalStatus.ON_STATION -> 4
        SignalStatus.WEAK -> 2
        SignalStatus.NO_SIGNAL -> 0
    }
    val color = when (signal) {
        SignalStatus.ON_STATION -> Green
        SignalStatus.WEAK -> Yellow
        SignalStatus.NO_SIGNAL -> Color(0xFF555555)
    }
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        listOf(3, 5, 7, 10).forEachIndexed { i, h ->
            Box(
                modifier = Modifier
                    .size(width = 3.dp, height = h.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (i < lit) color else Color.White.copy(alpha = 0.15f))
            )
        }
    }
}

@Composable
private fun CefrBadge(level: CefrLevel, visible: Boolean) {
    val color = if (visible) Accent else Color(0xFF555555)
    val text = if (visible) level.name else "—"
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f)),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

// ════════════════════════════════════════════════════════════════
//  FrequencyDial — тонкая горизонталь со станциями + стрелка
// ════════════════════════════════════════════════════════════════

@Composable
private fun FrequencyDial(
    currentFreq: Float,
    fmMin: Float,
    fmMax: Float,
    stationFreqs: List<Float>,
    signal: SignalStatus,
    modifier: Modifier = Modifier,
) {
    val needleColor = when (signal) {
        SignalStatus.ON_STATION -> Accent
        SignalStatus.WEAK -> Yellow
        SignalStatus.NO_SIGNAL -> Color.White.copy(alpha = 0.5f)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                "FM %.1f — %.1f MHz".format(fmMin, fmMax),
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
            ) {
                val w = size.width
                val h = size.height
                val cy = h / 2

                // Central line
                drawLine(
                    color = Color.White.copy(alpha = 0.25f),
                    start = Offset(0f, cy),
                    end = Offset(w, cy),
                    strokeWidth = 1.dp.toPx(),
                )

                // Station markers
                stationFreqs.forEach { f ->
                    val x = ((f - fmMin) / (fmMax - fmMin)) * w
                    drawCircle(
                        color = Accent.copy(alpha = 0.6f),
                        radius = 4.dp.toPx(),
                        center = Offset(x, cy),
                    )
                }

                // Needle
                val needleX = ((currentFreq - fmMin) / (fmMax - fmMin)) * w
                drawLine(
                    color = needleColor,
                    start = Offset(needleX, 0f),
                    end = Offset(needleX, h),
                    strokeWidth = 2.dp.toPx(),
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  TunerWheel — embedded knurled barrel
// ════════════════════════════════════════════════════════════════

@Composable
private fun TunerWheel(
    onScroll: (delta: Float) -> Unit,
    onScrollStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF44464C).copy(alpha = 0.45f),
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(modifier = Modifier.padding(vertical = 22.dp)) {
            // Slot (paz)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .height(74.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = { onScrollStop() },
                            onDragCancel = { onScrollStop() },
                        ) { _, dragAmount ->
                            // delta положителен вправо → инвертируем для частоты
                            val delta = -dragAmount.x / with(density) { 30.dp.toPx() }
                            onScroll(delta)
                        }
                    },
            ) {
                // Drum surface — vertical gradient simulating cylinder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF8C8C96).copy(alpha = 0.55f),
                                    Color(0xFF646470).copy(alpha = 0.45f),
                                    Color(0xFF464650).copy(alpha = 0.5f),
                                    Color(0xFF323238).copy(alpha = 0.6f),
                                    Color(0xFF23232A).copy(alpha = 0.7f),
                                    Color(0xFF14141A).copy(alpha = 0.8f),
                                    Color(0xFF08080A).copy(alpha = 0.9f),
                                )
                            )
                        ),
                ) {
                    // Knurled ticks — uniform
                    Canvas(modifier = Modifier.fillMaxSize().padding(vertical = 8.dp)) {
                        val tickColor = Color.Black.copy(alpha = 0.85f)
                        val highlight = Color.White.copy(alpha = 0.12f)
                        val tickWidth = 1.5.dp.toPx()
                        val tickGap = 5.dp.toPx()
                        val tickHeight = size.height
                        val numTicks = (size.width / (tickWidth + tickGap)).toInt()
                        for (i in 0..numTicks) {
                            val x = i * (tickWidth + tickGap)
                            drawLine(
                                color = tickColor,
                                start = Offset(x, 0f),
                                end = Offset(x, tickHeight),
                                strokeWidth = tickWidth,
                            )
                            drawLine(
                                color = highlight,
                                start = Offset(x + tickWidth, 0f),
                                end = Offset(x + tickWidth, tickHeight),
                                strokeWidth = 1f,
                            )
                        }
                    }

                    // Center highlight line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .align(Alignment.Center)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.35f),
                                        Color.Transparent,
                                    )
                                )
                            )
                    )

                    // Left/right edge fade
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    0.0f to Color.Black.copy(alpha = 0.6f),
                                    0.14f to Color.Transparent,
                                    0.86f to Color.Transparent,
                                    1.0f to Color.Black.copy(alpha = 0.6f),
                                )
                            )
                    )
                }
            }

            // Needle in center
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 28.dp)
                    .fillMaxWidth()
                    .height(84.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Accent)
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  Control buttons
// ════════════════════════════════════════════════════════════════

@Composable
private fun RowScope.CtrlButton(
    text: String,
    weight: Float,
    isPlay: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = if (isPlay)
            androidx.compose.foundation.BorderStroke(2.dp, Accent)
        else null,
        modifier = Modifier
            .weight(weight)
            .clickable(onClick = onClick),
    ) {
        Text(
            text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            color = if (isPlay) Accent else Color.White,
            fontWeight = if (isPlay) FontWeight.ExtraBold else FontWeight.Normal,
        )
    }
}
