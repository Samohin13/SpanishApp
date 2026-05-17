package com.spanishapp.radio.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.spanishapp.radio.data.CefrLevel
import com.spanishapp.radio.data.Country
import com.spanishapp.radio.data.Genre
import com.spanishapp.radio.data.Station

private val Accent = Color(0xFFFF5722)
private val Green = Color(0xFF4CAF50)

/**
 * Главный экран радио — статичный single-view layout (без вертикального
 * скролла). Сверху TopBar/чипы, в середине Hero+контролы, снизу карусель
 * станций. Чуть похоже на Spotify Now Playing.
 *
 * Все размеры подобраны так, чтобы layout помещался на экранах от 360×640dp
 * (минимальный поддерживаемый Android-телефон). На больших — Spacer'ы
 * растягиваются, hero остаётся пропорциональным.
 */
@Composable
fun RadioScreen(navController: NavHostController) {
    val vm: RadioViewModel = hiltViewModel()

    val country by vm.country.collectAsState()
    val station by vm.currentStation.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()
    val hasError by vm.hasError.collectAsState()
    val playbackState by vm.playbackState.collectAsState()
    val nowPlaying by vm.nowPlaying.collectAsState()
    val displayedStations by vm.displayedStations.collectAsState()
    val favoriteIds by vm.favoriteIds.collectAsState()
    val discoveryState by vm.discoveryState.collectAsState()
    val discoveryProgress by vm.discoveryProgress.collectAsState()
    val discoveryStage by vm.discoveryStage.collectAsState()
    val discoveryFoundCount by vm.discoveryFoundCount.collectAsState()
    val discoveryError by vm.discoveryError.collectAsState()
    val selectedGenres by vm.selectedGenres.collectAsState()
    val showOnlyFavorites by vm.showOnlyFavorites.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            TopBar(
                onBack = { navController.popBackStack() },
                onRefresh = { vm.refreshCatalog() },
                isLoading = discoveryState == RadioViewModel.DiscoveryState.LOADING,
            )

            // Тонкий прогресс-бар при поиске
            AnimatedVisibility(
                visible = discoveryState == RadioViewModel.DiscoveryState.LOADING,
                enter = fadeIn(), exit = fadeOut(),
            ) {
                LoadingBanner(
                    progress = discoveryProgress,
                    stage = discoveryStage,
                    foundCount = discoveryFoundCount,
                )
            }

            // Баннер ошибки если discovery вернул 0 — раньше молчал
            AnimatedVisibility(
                visible = discoveryError != null && discoveryState != RadioViewModel.DiscoveryState.LOADING,
                enter = fadeIn(), exit = fadeOut(),
            ) {
                ErrorBanner(
                    message = discoveryError ?: "",
                    onRetry = {
                        vm.dismissError()
                        vm.refreshCatalog()
                    },
                    onDismiss = vm::dismissError,
                )
            }

            CountryChips(
                current = country,
                onSelect = vm::selectCountry,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )

            FilterChipsRow(
                selectedGenres = selectedGenres,
                showOnlyFavorites = showOnlyFavorites,
                onToggleGenre = vm::toggleGenreFilter,
                onToggleFav = vm::toggleFavoritesFilter,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )

            // Hero + контролы — забирают остаток пространства
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                HeroArtwork(
                    station = station,
                    playbackState = playbackState,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .aspectRatio(1f),
                )

                Spacer(Modifier.height(16.dp))

                station?.let { st ->
                    Text(
                        st.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.dp))
                    // Под названием станции — что сейчас играет (ICY) или программа.
                    // ICY метаданные приоритетны если есть.
                    val subtitle = when {
                        hasError -> "Станция недоступна — следующая…"
                        playbackState == com.spanishapp.radio.player.RadioPlaybackState.BUFFERING -> "Загружаем поток…"
                        nowPlaying != null -> "🎵 $nowPlaying"
                        else -> st.program
                    }
                    Text(
                        subtitle,
                        fontSize = 12.sp,
                        fontWeight = if (nowPlaying != null) FontWeight.SemiBold else FontWeight.Normal,
                        color = when {
                            hasError -> Color(0xFFE53935)
                            nowPlaying != null -> MaterialTheme.colorScheme.onSurface
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(8.dp))
                    StationTags(station = st)
                } ?: Text(
                    "Выбери станцию ниже",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(16.dp))

                PlayerControls(
                    isPlaying = isPlaying,
                    isFavorite = station?.id?.let { favoriteIds.contains(it) } ?: false,
                    canControl = station != null,
                    onPrev = vm::previousStation,
                    onPlayPause = vm::togglePlayback,
                    onNext = vm::nextStation,
                    onToggleFavorite = { station?.let { vm.toggleFavorite(it.id) } },
                )
            }

            // Карусель — фиксированный блок снизу
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    "СТАНЦИИ · ${country.displayName} · ${displayedStations.size}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.4.sp,
                    modifier = Modifier.padding(start = 18.dp, bottom = 8.dp),
                )
                StationCarousel(
                    stations = displayedStations,
                    currentStationId = station?.id,
                    favoriteIds = favoriteIds,
                    onStationClick = { vm.tuneToStationDirect(it) },
                    onFindMore = { vm.discoverMore() },
                    isLoadingMore = discoveryState == RadioViewModel.DiscoveryState.LOADING,
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  TopBar
// ════════════════════════════════════════════════════════════════

@Composable
private fun TopBar(
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    isLoading: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            "Радио",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.weight(1f))
        // Refresh — крутится при LOADING (анимация даёт фидбэк что работа идёт),
        // но кнопка остаётся active чтобы юзер мог тапнуть ещё раз / отменить
        if (isLoading) {
            val transition = rememberInfiniteTransition(label = "spin")
            val rotation by transition.animateFloat(
                initialValue = 0f, targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "rotation",
            )
            IconButton(onClick = onRefresh) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = "Обновляем…",
                    tint = Accent,
                    modifier = Modifier.graphicsLayer(rotationZ = rotation),
                )
            }
        } else {
            IconButton(onClick = onRefresh) {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = "Обновить каталог",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    val red = Color(0xFFE53935)
    Surface(
        color = red.copy(alpha = 0.10f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, red.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Не удалось подобрать станции",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = red,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    message,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = red.copy(alpha = 0.18f),
                modifier = Modifier.clickable(onClick = onRetry),
            ) {
                Text(
                    "Повторить",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = red,
                )
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Закрыть",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LoadingBanner(
    progress: Float,
    stage: com.spanishapp.radio.data.RadioCatalogRepository.DiscoveryStage,
    foundCount: Int,
) {
    val stageText = when (stage) {
        com.spanishapp.radio.data.RadioCatalogRepository.DiscoveryStage.IDLE ->
            "Готовимся…"
        com.spanishapp.radio.data.RadioCatalogRepository.DiscoveryStage.DETECTING_COUNTRY ->
            "Определяем регион…"
        com.spanishapp.radio.data.RadioCatalogRepository.DiscoveryStage.FETCHING_CATALOG ->
            "Запрашиваем каталог станций…"
        com.spanishapp.radio.data.RadioCatalogRepository.DiscoveryStage.PROBING ->
            if (foundCount > 0) "Проверяем станции… (живых: $foundCount)"
            else "Проверяем доступность станций…"
        com.spanishapp.radio.data.RadioCatalogRepository.DiscoveryStage.DONE ->
            "Готово"
    }

    Surface(
        color = Accent.copy(alpha = 0.10f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stageText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Accent,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${(progress * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Accent,
                )
            }
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Accent.copy(alpha = 0.18f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(Accent),
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  Country + Filter chips
// ════════════════════════════════════════════════════════════════

@Composable
private fun CountryChips(
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
                color = if (isActive) Accent.copy(alpha = 0.14f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = if (isActive) BorderStroke(1.5.dp, Accent.copy(alpha = 0.5f)) else null,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(c) },
            ) {
                Text(
                    "${c.emoji} ${c.displayName}",
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isActive) Accent else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * 6 чипов фильтра: 5 жанров (Music/Talk/News/Sports/Culture) + Favorites.
 * Multi-select. Пустой выбор = «все станции».
 */
@Composable
private fun FilterChipsRow(
    selectedGenres: Set<Genre>,
    showOnlyFavorites: Boolean,
    onToggleGenre: (Genre) -> Unit,
    onToggleFav: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item {
            FilterChip(
                icon = Icons.Filled.MusicNote,
                label = "Музыка",
                selected = Genre.MUSIC in selectedGenres,
                onClick = { onToggleGenre(Genre.MUSIC) },
            )
        }
        item {
            FilterChip(
                icon = Icons.Filled.RecordVoiceOver,
                label = "Разговор",
                selected = Genre.TALK in selectedGenres,
                onClick = { onToggleGenre(Genre.TALK) },
            )
        }
        item {
            FilterChip(
                icon = Icons.Filled.Newspaper,
                label = "Новости",
                selected = Genre.NEWS in selectedGenres,
                onClick = { onToggleGenre(Genre.NEWS) },
            )
        }
        item {
            FilterChip(
                icon = Icons.Filled.SportsBasketball,
                label = "Спорт",
                selected = Genre.SPORTS in selectedGenres,
                onClick = { onToggleGenre(Genre.SPORTS) },
            )
        }
        item {
            FilterChip(
                icon = Icons.Filled.TheaterComedy,
                label = "Культура",
                selected = Genre.CULTURE in selectedGenres,
                onClick = { onToggleGenre(Genre.CULTURE) },
            )
        }
        item {
            FilterChip(
                icon = Icons.Filled.Favorite,
                label = "Избранное",
                selected = showOnlyFavorites,
                onClick = onToggleFav,
            )
        }
    }
}

@Composable
private fun FilterChip(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = if (selected) Accent.copy(alpha = 0.18f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = if (selected) BorderStroke(1.2.dp, Accent.copy(alpha = 0.6f)) else null,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (selected) Accent else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(5.dp))
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) Accent else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  HERO
// ════════════════════════════════════════════════════════════════

@Composable
private fun HeroArtwork(
    station: Station?,
    playbackState: com.spanishapp.radio.player.RadioPlaybackState,
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
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(gradient)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            station?.shortCode ?: "—",
            fontSize = 56.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = (-2).sp,
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
        ) {
            StatePill(playbackState)
        }
    }
}

/**
 * Универсальный pill в верхнем углу hero — отображает все состояния
 * плеера (LIVE / BUFFERING / PAUSED / ERROR / ENDED).
 */
@Composable
private fun StatePill(state: com.spanishapp.radio.player.RadioPlaybackState) {
    when (state) {
        com.spanishapp.radio.player.RadioPlaybackState.PLAYING -> LivePillCompact()
        com.spanishapp.radio.player.RadioPlaybackState.BUFFERING -> BufferingPill()
        com.spanishapp.radio.player.RadioPlaybackState.PAUSED -> StaticPill("PAUSED", Color(0xFF888888))
        com.spanishapp.radio.player.RadioPlaybackState.IDLE -> StaticPill("READY", Color(0xFF888888))
        com.spanishapp.radio.player.RadioPlaybackState.ENDED -> StaticPill("ENDED", Color(0xFFE53935))
        com.spanishapp.radio.player.RadioPlaybackState.ERROR -> ErrorPill()
    }
}

@Composable
private fun LivePillCompact() {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = Color.Black.copy(alpha = 0.35f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
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
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Green.copy(alpha = alpha)),
            )
            Spacer(Modifier.width(5.dp))
            Text("LIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun BufferingPill() {
    val transition = rememberInfiniteTransition(label = "buf")
    val dotShift by transition.animateFloat(
        initialValue = 0f, targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "buf_dot",
    )
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = Color.Black.copy(alpha = 0.35f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Имитация бегущих точек — bouncing dot
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFC107).copy(alpha = 0.6f + (dotShift / 10f))),
            )
            Spacer(Modifier.width(5.dp))
            Text("LOADING", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun StaticPill(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = Color.Black.copy(alpha = 0.35f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Spacer(Modifier.width(5.dp))
            Text(text, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

@Composable
private fun StationTags(station: Station) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Tag(station.country.displayName, Accent.copy(alpha = 0.12f), Accent)
        val cefrColor = when (station.level) {
            CefrLevel.A2 -> Color(0xFFFFC107)
            CefrLevel.B1 -> Accent
            CefrLevel.B2 -> Color(0xFFE53935)
        }
        Tag(station.level.name, cefrColor.copy(alpha = 0.15f), cefrColor)
        Tag(
            station.genre.displayName,
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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

// ════════════════════════════════════════════════════════════════
//  Controls
// ════════════════════════════════════════════════════════════════

@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    isFavorite: Boolean,
    canControl: Boolean,
    onPrev: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleBtn(
            icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            cd = if (isFavorite) "В избранном" else "Добавить в избранное",
            size = 44.dp,
            iconSize = 22.dp,
            tint = if (isFavorite) Accent else MaterialTheme.colorScheme.onSurfaceVariant,
            bg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            enabled = canControl,
            onClick = onToggleFavorite,
        )
        CircleBtn(
            icon = Icons.Filled.SkipPrevious,
            cd = "Предыдущая",
            size = 52.dp,
            iconSize = 28.dp,
            tint = MaterialTheme.colorScheme.onSurface,
            bg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            enabled = canControl,
            onClick = onPrev,
        )
        // Big play/pause (gradient)
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Accent, Accent.copy(alpha = 0.75f))))
                .clickable(enabled = canControl, onClick = onPlayPause),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Пауза" else "Играть",
                modifier = Modifier.size(36.dp),
                tint = Color.White,
            )
        }
        CircleBtn(
            icon = Icons.Filled.SkipNext,
            cd = "Следующая",
            size = 52.dp,
            iconSize = 28.dp,
            tint = MaterialTheme.colorScheme.onSurface,
            bg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            enabled = canControl,
            onClick = onNext,
        )
        // Spacer чтобы play остался по центру (компенсация за favorite слева)
        Spacer(Modifier.size(44.dp))
    }
}

@Composable
private fun CircleBtn(
    icon: ImageVector,
    cd: String,
    size: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
    tint: Color,
    bg: Color,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = cd,
            modifier = Modifier.size(iconSize),
            tint = if (enabled) tint else tint.copy(alpha = 0.4f),
        )
    }
}

// ════════════════════════════════════════════════════════════════
//  Carousel
// ════════════════════════════════════════════════════════════════

@Composable
private fun StationCarousel(
    stations: List<Station>,
    currentStationId: String?,
    favoriteIds: Set<String>,
    onStationClick: (Station) -> Unit,
    onFindMore: () -> Unit,
    isLoadingMore: Boolean,
) {
    val listState = rememberLazyListState()

    // Авто-скролл к активной станции при её смене (например тапнули чип/skip)
    LaunchedEffect(currentStationId, stations) {
        if (currentStationId == null) return@LaunchedEffect
        val idx = stations.indexOfFirst { it.id == currentStationId }
        if (idx >= 0) listState.animateScrollToItem(idx)
    }

    LazyRow(
        state = listState,
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
        item("find_more") {
            FindMoreTile(loading = isLoadingMore, onClick = onFindMore)
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
            .width(96.dp)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(gradient))
                .then(
                    if (isPlaying) Modifier.border(
                        BorderStroke(2.dp, Accent),
                        RoundedCornerShape(14.dp),
                    ) else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                station.shortCode,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
            )
            if (isFavorite) {
                // Используем то же ♥ что и в контролах — было ★, путало юзера
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(3.dp),
                ) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "В избранном",
                        modifier = Modifier.size(11.dp),
                        tint = Accent,
                    )
                }
            }
            if (isPlaying) {
                // Overlay с большой play-кнопкой → мгновенный визуальный отклик
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.95f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Accent,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(
            station.name,
            fontSize = 10.sp,
            fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isPlaying) Accent else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            station.level.name,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

/**
 * Последний тайл карусели — «+ Найти ещё». Тап → discoverMore(20).
 * Пока идёт поиск показываем тонкий пульс через alpha.
 */
@Composable
private fun FindMoreTile(loading: Boolean, onClick: () -> Unit) {
    val pulse = if (loading) {
        val transition = rememberInfiniteTransition(label = "more_pulse")
        transition.animateFloat(
            initialValue = 0.4f, targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "more_alpha",
        ).value
    } else 1f

    Column(
        modifier = Modifier
            .width(96.dp)
            .clickable(enabled = !loading, onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Accent.copy(alpha = 0.12f * pulse))
                .border(
                    BorderStroke(1.5.dp, Accent.copy(alpha = 0.5f * pulse)),
                    RoundedCornerShape(14.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Найти ещё станции",
                modifier = Modifier.size(32.dp),
                tint = Accent.copy(alpha = pulse),
            )
        }
        Spacer(Modifier.height(5.dp))
        Text(
            if (loading) "Подбираем…" else "Найти ещё",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Accent,
            maxLines = 1,
        )
        Text(
            if (loading) "Подожди немного" else "+20 станций",
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}
