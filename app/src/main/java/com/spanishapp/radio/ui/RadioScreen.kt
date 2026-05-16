package com.spanishapp.radio.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.spanishapp.radio.data.CefrLevel
import com.spanishapp.radio.data.Country
import com.spanishapp.radio.data.Station
import com.spanishapp.radio.player.HapticManager

private val Accent = Color(0xFFFF5722)
private val Green = Color(0xFF4CAF50)
private val Yellow = Color(0xFFFFC107)

@Composable
fun RadioScreen(navController: NavHostController) {
    val vm: RadioViewModel = hiltViewModel()
    val context = LocalContext.current
    val haptic = remember { HapticManager(context) }

    val country by vm.country.collectAsState()
    val freq by vm.frequency.collectAsState()
    val station by vm.currentStation.collectAsState()
    val signal by vm.signal.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()
    val stations by vm.stations.collectAsState()
    val favoriteIds by vm.favoriteIds.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()         // отступ под status bar (часы, батарея)
                .verticalScroll(rememberScrollState()),
        ) {
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

            // Country chips
            CountrySelector(
                current = country,
                onSelect = vm::selectCountry,
                modifier = Modifier.padding(horizontal = 18.dp),
            )

            Spacer(Modifier.height(20.dp))

            // ─── HERO — большая «обложка» станции (Apple Music style) ───
            HeroArtwork(
                station = station,
                signal = signal,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp),
            )

            Spacer(Modifier.height(20.dp))

            // ─── Station info: name + program + tags ───
            station?.let { st ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        st.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        st.program,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    StationTags(station = st, signal = signal)
                }
            }

            Spacer(Modifier.height(24.dp))

            // ─── PLAYER CONTROLS — Spotify-style ───
            PlayerControls(
                isPlaying = isPlaying,
                isFavorite = station?.id?.let { favoriteIds.contains(it) } ?: false,
                onPrev = { vm.previousStation(); haptic.stationHit() },
                onPlayPause = { vm.togglePlayback() },
                onNext = { vm.nextStation(); haptic.stationHit() },
                onToggleFavorite = { station?.let { vm.toggleFavorite(it.id) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            )

            Spacer(Modifier.height(28.dp))

            // ─── TUNER section (secondary) — ниже как «scan band» ───
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
            ) {
                Text(
                    "СКАНИРОВАНИЕ FM",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 10.dp, start = 4.dp),
                )

                // Frequency dial
                FrequencyDial(
                    currentFreq = freq,
                    fmMin = vm.fmMin,
                    fmMax = vm.fmMax,
                    stationFreqs = stations.map { it.frequency },
                    signal = signal,
                )

                Spacer(Modifier.height(10.dp))

                // Tuner wheel
                TunerWheel(
                    onScroll = { delta ->
                        vm.onScrollFrequency(delta)
                        haptic.tickLight()
                    },
                    onScrollStop = { vm.onScrollStop(); haptic.stationHit() },
                )
            }

            // Отступ под BottomBar (62dp) + mini-player (~56dp когда виден) + nav bar
            // 140dp хватает чтобы wheel не залезал под навигацию
            Spacer(Modifier.height(140.dp))
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  HERO — большая «обложка» станции (gradient + код)
// ════════════════════════════════════════════════════════════════

@Composable
private fun HeroArtwork(
    station: Station?,
    signal: SignalStatus,
    modifier: Modifier = Modifier,
) {
    // Гадиент в зависимости от страны → даёт визуальное разнообразие
    val gradient = when (station?.country) {
        Country.SPAIN -> listOf(Color(0xFFFF5722), Color(0xFFD32F2F))
        Country.MEXICO -> listOf(Color(0xFF388E3C), Color(0xFFD32F2F))
        Country.ARGENTINA -> listOf(Color(0xFF1976D2), Color(0xFF64B5F6))
        null -> listOf(Color(0xFF666666), Color(0xFF444444))
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(gradient)),
        contentAlignment = Alignment.Center,
    ) {
        // Watermark — большие буквы кода станции
        Text(
            station?.shortCode ?: "—",
            fontSize = 72.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = (-2).sp,
        )
        // LIVE-pill в верхнем углу
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp),
        ) {
            LivePillCompact(signal)
        }
        // Frequency tag в нижнем углу
        station?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(14.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(
                    "%.1f MHz".format(it.frequency),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun LivePillCompact(signal: SignalStatus) {
    val (label, color) = when (signal) {
        SignalStatus.ON_STATION -> "LIVE" to Green
        SignalStatus.WEAK -> "WEAK" to Yellow
        SignalStatus.NO_SIGNAL -> "—" to Color(0xFF888888)
    }
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = Color.Black.copy(alpha = 0.35f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  StationTags — пилюли страны / уровня / жанра
// ════════════════════════════════════════════════════════════════

@Composable
private fun StationTags(station: Station, signal: SignalStatus) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        // Country
        Tag("${station.country.emoji} ${station.country.displayName}", Accent.copy(alpha = 0.12f), Accent)
        // CEFR
        val cefrColor = when (station.level) {
            CefrLevel.A2 -> Color(0xFFFFC107)
            CefrLevel.B1 -> Accent
            CefrLevel.B2 -> Color(0xFFE53935)
        }
        Tag(station.level.name, cefrColor.copy(alpha = 0.15f), cefrColor)
        // Genre
        Tag(
            "${station.genre.emoji} ${station.genre.displayName}",
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Tag(text: String, bg: Color, color: Color) {
    Surface(shape = RoundedCornerShape(100.dp), color = bg) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

// ════════════════════════════════════════════════════════════════
//  PlayerControls — Spotify-style (big play + skip + favorite)
// ════════════════════════════════════════════════════════════════

@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    isFavorite: Boolean,
    onPrev: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Favorite ⭐
        CircularIconButton(
            symbol = if (isFavorite) "★" else "☆",
            size = 44.dp,
            iconSize = 22.sp,
            color = if (isFavorite) Accent else MaterialTheme.colorScheme.onSurfaceVariant,
            bg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            onClick = onToggleFavorite,
        )
        // Previous
        CircularIconButton(
            symbol = "⏮",
            size = 52.dp,
            iconSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface,
            bg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            onClick = onPrev,
        )
        // Play/Pause — БОЛЬШАЯ кнопка (Spotify-style)
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Accent, Accent.copy(alpha = 0.75f))
                    )
                )
                .clickable(onClick = onPlayPause),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (isPlaying) "⏸" else "▶",
                fontSize = 32.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
        // Next
        CircularIconButton(
            symbol = "⏭",
            size = 52.dp,
            iconSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface,
            bg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            onClick = onNext,
        )
        // Spacer for symmetry with favorite
        Spacer(modifier = Modifier.size(44.dp))
    }
}

@Composable
private fun CircularIconButton(
    symbol: String,
    size: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.TextUnit,
    color: Color,
    bg: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, fontSize = iconSize, color = color)
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
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(
                "%.1f MHz · FM %.1f — %.1f".format(currentFreq, fmMin, fmMax),
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
            ) {
                val w = size.width
                val h = size.height
                val cy = h / 2

                drawLine(
                    color = Color.White.copy(alpha = 0.25f),
                    start = Offset(0f, cy),
                    end = Offset(w, cy),
                    strokeWidth = 1.dp.toPx(),
                )

                stationFreqs.forEach { f ->
                    val x = ((f - fmMin) / (fmMax - fmMin)) * w
                    drawCircle(
                        color = Accent.copy(alpha = 0.6f),
                        radius = 3.5.dp.toPx(),
                        center = Offset(x, cy),
                    )
                }

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
    var tickOffsetPx by remember { mutableStateOf(0f) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF44464C).copy(alpha = 0.4f),
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(modifier = Modifier.padding(vertical = 18.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = { onScrollStop() },
                            onDragCancel = { onScrollStop() },
                        ) { _, dragAmount ->
                            tickOffsetPx += dragAmount.x
                            val delta = -dragAmount.x / with(density) { 30.dp.toPx() }
                            onScroll(delta)
                        }
                    },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF8C8C96).copy(alpha = 0.5f),
                                    Color(0xFF464650).copy(alpha = 0.5f),
                                    Color(0xFF232328).copy(alpha = 0.7f),
                                    Color(0xFF08080A).copy(alpha = 0.9f),
                                )
                            )
                        ),
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(vertical = 8.dp)) {
                        val tickColor = Color.Black.copy(alpha = 0.85f)
                        val highlight = Color.White.copy(alpha = 0.12f)
                        val tickWidth = 1.5.dp.toPx()
                        val tickGap = 5.dp.toPx()
                        val tickStep = tickWidth + tickGap
                        val tickHeight = size.height
                        val baseOffset = tickOffsetPx.mod(tickStep)
                        val numTicks = (size.width / tickStep).toInt() + 2
                        for (i in -1..numTicks) {
                            val x = baseOffset + i * tickStep
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
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .height(70.dp),
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
