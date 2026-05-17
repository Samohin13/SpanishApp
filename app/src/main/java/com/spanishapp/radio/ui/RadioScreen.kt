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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.spanishapp.radio.data.CefrLevel
import com.spanishapp.radio.data.Country
import com.spanishapp.radio.data.Station

private val Accent = Color(0xFFFF5722)
private val Green = Color(0xFF4CAF50)

@Composable
fun RadioScreen(navController: NavHostController) {
    val vm: RadioViewModel = hiltViewModel()

    val country by vm.country.collectAsState()
    val station by vm.currentStation.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()
    val hasError by vm.hasError.collectAsState()
    val stations by vm.stations.collectAsState()
    val favoriteIds by vm.favoriteIds.collectAsState()
    val discoveryState by vm.discoveryState.collectAsState()
    val discoveryProgress by vm.discoveryProgress.collectAsState()
    val discoveryFoundCount by vm.discoveryFoundCount.collectAsState()
    val totalCaughtWords by vm.totalCaughtWords.collectAsState()
    val totalListeningMinutes by vm.totalListeningMinutes.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
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
                Spacer(Modifier.weight(1f))
                // Refresh button — повторный auto-discovery
                IconButton(
                    onClick = { vm.refreshCatalog() },
                    enabled = discoveryState != RadioViewModel.DiscoveryState.LOADING,
                ) {
                    Text(
                        "🔄",
                        fontSize = 20.sp,
                        color = if (discoveryState == RadioViewModel.DiscoveryState.LOADING)
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Discovery progress banner
            if (discoveryState == RadioViewModel.DiscoveryState.LOADING) {
                Surface(
                    color = Accent.copy(alpha = 0.10f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔍", fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Подбираем станции для тебя…",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Accent,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                "${(discoveryProgress * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Accent,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        // Прогресс-бар
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Accent.copy(alpha = 0.18f)),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(discoveryProgress.coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .background(Accent)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            } else if (discoveryState == RadioViewModel.DiscoveryState.READY && discoveryFoundCount > 0) {
                // Краткое уведомление о завершении (исчезает в течение 3 сек через animation)
                Text(
                    "✓ Найдено $discoveryFoundCount рабочих станций",
                    fontSize = 11.sp,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp),
                )
            }

            // Country chips
            CountrySelector(
                current = country,
                onSelect = vm::selectCountry,
                modifier = Modifier.padding(horizontal = 18.dp),
            )

            Spacer(Modifier.height(20.dp))

            // ─── HERO ───
            HeroArtwork(
                station = station,
                isPlaying = isPlaying,
                hasError = hasError,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp),
            )

            Spacer(Modifier.height(20.dp))

            // ─── Station info ───
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
                        if (hasError) "Станция временно недоступна — следующая…" else st.program,
                        fontSize = 13.sp,
                        color = if (hasError) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    StationTags(station = st)
                }
            }

            Spacer(Modifier.height(24.dp))

            // ─── PLAYER CONTROLS ───
            PlayerControls(
                isPlaying = isPlaying,
                isFavorite = station?.id?.let { favoriteIds.contains(it) } ?: false,
                onPrev = { vm.previousStation() },
                onPlayPause = { vm.togglePlayback() },
                onNext = { vm.nextStation() },
                onToggleFavorite = { station?.let { vm.toggleFavorite(it.id) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            )

            Spacer(Modifier.height(20.dp))

            // ─── ОБУЧАЮЩАЯ СЕКЦИЯ — Поймал слово + статистика ───
            WordCatchCard(
                totalCaught = totalCaughtWords,
                totalMinutes = totalListeningMinutes,
                onCatchWord = { vm.catchWord() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
            )

            Spacer(Modifier.height(20.dp))

            // ─── СТАНЦИИ horizontal scroll ───
            Text(
                "ВСЕ СТАНЦИИ · ${country.displayName}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(start = 22.dp, bottom = 10.dp),
            )
            StationCarousel(
                stations = stations,
                currentStationId = station?.id,
                favoriteIds = favoriteIds,
                onStationClick = { vm.tuneToStationDirect(it) },
            )

            Spacer(Modifier.height(140.dp))
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  HERO
// ════════════════════════════════════════════════════════════════

@Composable
private fun HeroArtwork(
    station: Station?,
    isPlaying: Boolean,
    hasError: Boolean,
    modifier: Modifier = Modifier,
) {
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
        // Большие буквы кода
        Text(
            station?.shortCode ?: "—",
            fontSize = 72.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = (-2).sp,
        )
        // LIVE / ERROR pill
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp),
        ) {
            if (hasError) ErrorPill() else LivePillCompact(isPlaying)
        }
        // Frequency
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
private fun LivePillCompact(isPlaying: Boolean) {
    val color = if (isPlaying) Green else Color(0xFF888888)
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = Color.Black.copy(alpha = 0.35f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isPlaying) {
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
                        .background(color.copy(alpha = alpha))
                )
                Spacer(Modifier.width(6.dp))
                Text("LIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(Modifier.width(6.dp))
                Text("PAUSED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun ErrorPill() {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = Color(0xFFE53935).copy(alpha = 0.95f),
    ) {
        Text(
            "ERROR",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

@Composable
private fun StationTags(station: Station) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Tag("${station.country.emoji} ${station.country.displayName}", Accent.copy(alpha = 0.12f), Accent)
        val cefrColor = when (station.level) {
            CefrLevel.A2 -> Color(0xFFFFC107)
            CefrLevel.B1 -> Accent
            CefrLevel.B2 -> Color(0xFFE53935)
        }
        Tag(station.level.name, cefrColor.copy(alpha = 0.15f), cefrColor)
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
        CircularIconButton(
            symbol = if (isFavorite) "★" else "☆",
            size = 44.dp,
            iconSize = 22.sp,
            color = if (isFavorite) Accent else MaterialTheme.colorScheme.onSurfaceVariant,
            bg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            onClick = onToggleFavorite,
        )
        CircularIconButton(
            symbol = "⏮",
            size = 52.dp,
            iconSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface,
            bg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            onClick = onPrev,
        )
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
        CircularIconButton(
            symbol = "⏭",
            size = 52.dp,
            iconSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface,
            bg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            onClick = onNext,
        )
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
//  StationCarousel — горизонтальный скролл всех станций страны
// ════════════════════════════════════════════════════════════════

@Composable
private fun StationCarousel(
    stations: List<Station>,
    currentStationId: String?,
    favoriteIds: Set<String>,
    onStationClick: (Station) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(stations, key = { it.id }) { st ->
            StationCard(
                station = st,
                isPlaying = st.id == currentStationId,
                isFavorite = favoriteIds.contains(st.id),
                onClick = { onStationClick(st) },
            )
        }
    }
}

@Composable
private fun StationCard(
    station: Station,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
) {
    val gradient = when (station.country) {
        Country.SPAIN -> listOf(Color(0xFFFF5722), Color(0xFFD32F2F))
        Country.MEXICO -> listOf(Color(0xFF388E3C), Color(0xFFD32F2F))
        Country.ARGENTINA -> listOf(Color(0xFF1976D2), Color(0xFF64B5F6))
    }
    Column(
        modifier = Modifier
            .width(108.dp)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(108.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(gradient)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                station.shortCode,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
            )
            // ⭐ badge top-right
            if (isFavorite) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                        .padding(4.dp),
                ) {
                    Text("★", fontSize = 11.sp, color = Color(0xFFFFC107))
                }
            }
            // Active border
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.95f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("▶", fontSize = 20.sp, color = Accent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            station.name,
            fontSize = 11.sp,
            fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isPlaying) Accent else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            "%.1f · ${station.level.name}".format(station.frequency),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

// ════════════════════════════════════════════════════════════════
//  CountrySelector
// ════════════════════════════════════════════════════════════════

// ════════════════════════════════════════════════════════════════
//  WordCatchCard — обучающий элемент: «Поймал слово!» + stats
// ════════════════════════════════════════════════════════════════

@Composable
private fun WordCatchCard(
    totalCaught: Int,
    totalMinutes: Long,
    onCatchWord: () -> Boolean,
    modifier: Modifier = Modifier,
) {
    // Pulse-анимация когда юзер тапнул
    var pulse by remember { mutableStateOf(false) }
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pulse) 1.08f else 1f,
        animationSpec = androidx.compose.animation.core.tween(150),
        label = "catch_pulse",
    )
    LaunchedEffect(pulse) {
        if (pulse) {
            kotlinx.coroutines.delay(200)
            pulse = false
        }
    }

    Column(modifier = modifier) {
        // Big "Поймал слово!" button
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Accent.copy(alpha = 0.10f),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Accent.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val ok = onCatchWord()
                    if (ok) pulse = true
                },
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .scale(scale),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("💬", fontSize = 26.sp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Поймал слово!",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Тапни когда услышал знакомое",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Accent.copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Accent.copy(alpha = 0.4f)),
                ) {
                    Text(
                        "+5 XP",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Accent,
                    )
                }
            }
        }

        // Mini stats row
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatChip(
                emoji = "💬",
                value = totalCaught.toString(),
                label = "слов",
                modifier = Modifier.weight(1f),
            )
            StatChip(
                emoji = "⏱",
                value = formatMinutes(totalMinutes),
                label = "прослушано",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatChip(
    emoji: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(emoji, fontSize = 18.sp)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    label,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun formatMinutes(minutes: Long): String {
    if (minutes < 60) return "$minutes мин"
    val h = minutes / 60
    val m = minutes % 60
    return if (m == 0L) "$h ч" else "$h ч $m мин"
}

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
