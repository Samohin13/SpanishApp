package com.spanishapp.ui.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.EmojiFlags
import androidx.compose.material.icons.filled.Leaderboard
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.spanishapp.data.db.dao.AchievementDao
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.entity.UserProgressEntity
import com.spanishapp.data.repository.AuthRepository
import com.spanishapp.domain.algorithm.League
import com.spanishapp.domain.algorithm.LeagueResolver
import com.spanishapp.domain.algorithm.MasteryRating
import com.spanishapp.domain.algorithm.XpSystem
import com.spanishapp.ui.components.LeagueBadge
import com.spanishapp.ui.components.LeaguePath
import com.spanishapp.ui.components.SpanishBackground
import com.spanishapp.ui.components.SpanishFlagRating
import com.spanishapp.ui.flashcards.CategoryMeta
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ... (ViewModel remains the same)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProgressDao: UserProgressDao,
    private val wordDao: WordDao,
    private val achievementDao: AchievementDao,
    private val authRepository: AuthRepository,
    private val dailyXpDao: com.spanishapp.data.db.dao.DailyXpDao
) : ViewModel() {

    private val _categoryRatings = MutableStateFlow<List<CategoryRatingUi>>(emptyList())
    val categoryRatings: StateFlow<List<CategoryRatingUi>> = _categoryRatings.asStateFlow()

    /**
     * История XP за последние 7 дней. Возвращает 7 элементов даже если
     * в БД нет данных за какие-то дни (заполняем нулями).
     */
    val xpHistory: StateFlow<List<DailyXpPoint>> = run {
        val sinceDay = java.time.LocalDate.now().minusDays(6).toString()
        dailyXpDao.observeSince(sinceDay)
            .map { rows ->
                val byDay = rows.associateBy { it.day }
                (0..6).map { offset ->
                    val day = java.time.LocalDate.now().minusDays((6 - offset).toLong())
                    val key = day.toString()
                    DailyXpPoint(
                        date = day,
                        xp = byDay[key]?.xp ?: 0
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    val state: StateFlow<ProfileUiState> = combine(
        userProgressDao.getProgress(),
        wordDao.learnedCount(),
        achievementDao.unlockedCount(),
        achievementDao.getAll().map { it.size },
        authRepository.userPhotoUrl,
        authRepository.userName
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val progress = values[0] as UserProgressEntity?
        val learned = values[1] as Int
        val unlocked = values[2] as Int
        val total = values[3] as Int
        val photoUrl = values[4] as String?
        val authName = (values[5] as String?).orEmpty()
        ProfileUiState(
            progress             = progress ?: UserProgressEntity(),
            learnedCount         = learned,
            unlockedAchievements = unlocked,
            totalAchievements    = total,
            photoUrl             = photoUrl,
            authName             = authName
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    init {
        viewModelScope.launch {
            refreshCategoryRatings()
        }
    }

    suspend fun refreshCategoryRatings() {
        val rows = wordDao.getCategoryStats()
        val ui = rows.map { row ->
            val info = CategoryMeta.infoFor(row.category)
            CategoryRatingUi(
                key = row.category,
                label = info.label,
                icon = info.icon,
                flags = MasteryRating.flags(row.total, row.learned, row.totalReviews, row.correctReviews),
                score = MasteryRating.score(row.total, row.learned, row.totalReviews, row.correctReviews),
                total = row.total,
                learned = row.learned
            )
        }.sortedBy { it.score }
        _categoryRatings.value = ui
    }
}

data class ProfileUiState(
    val progress: UserProgressEntity = UserProgressEntity(),
    val learnedCount: Int = 0,
    val unlockedAchievements: Int = 0,
    val totalAchievements: Int = 0,
    val photoUrl: String? = null,
    val authName: String = ""
)

data class CategoryRatingUi(
    val key: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val flags: Int,
    val score: Float,
    val total: Int,
    val learned: Int
)

/** Одна точка графика XP за день. */
data class DailyXpPoint(
    val date: java.time.LocalDate,
    val xp: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    vm: ProfileViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val categoryRatings by vm.categoryRatings.collectAsState()
    val xpHistory by vm.xpHistory.collectAsState()
    val p = state.progress
    val haptic = com.spanishapp.ui.components.rememberCheckedHaptic()
    val appLevel  = XpSystem.levelForXp(p.totalXp)
    val progress  = XpSystem.progressToNextLevel(p.totalXp)
    val league = LeagueResolver.fromTier(p.currentLeague.coerceAtLeast(1))
    val peakLeague = LeagueResolver.fromTier(p.peakLeague.coerceAtLeast(1))
    val leagueProgress = LeagueResolver.progressInLeague(p.skillRating)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                title = { Text("Mi Perfil", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileHeader(
                name = state.authName.ifBlank { p.displayName }.ifBlank { androidx.compose.ui.res.stringResource(com.spanishapp.R.string.profile_default_name) },
                level = p.currentLevel,
                appLevel = appLevel,
                progress = progress,
                photoUrl = state.photoUrl,
                onAvatarClick = { navController.navigate("settings") }
            )
            Spacer(Modifier.height(24.dp))
            WeeklyActivityChart(
                history = xpHistory,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatBox(value = "${state.learnedCount}", label = androidx.compose.ui.res.stringResource(com.spanishapp.R.string.profile_stat_words), icon = "📚", modifier = Modifier.weight(1f))
                StatBox(value = "${p.currentStreak}", label = androidx.compose.ui.res.stringResource(com.spanishapp.R.string.profile_stat_days), icon = "🔥", modifier = Modifier.weight(1f) )
                StatBox(value = "${p.totalStudyMinutes}", label = androidx.compose.ui.res.stringResource(com.spanishapp.R.string.profile_stat_minutes), icon = "⏱", modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(24.dp))
            // ── Путь до Мадрида ─────────────────────────────────
            PathToMadridCard(
                league = league,
                peakLeague = peakLeague,
                leagueProgress = leagueProgress,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(16.dp))
            // ── Skill Rating ────────────────────────────────────
            SkillRatingCard(
                current = p.skillRating,
                peak = p.peakSkillRating,
                onLeaderboardClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    navController.navigate("leaderboard")
                },
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(16.dp))
            // ── Mastery по темам (флаги) ────────────────────────
            CategoryRatingCard(
                items = categoryRatings,
                onSeeAll = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    navController.navigate("rating_full")
                },
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(16.dp))
            AchievementsSection(unlocked = state.unlockedAchievements, total = state.totalAchievements, onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                navController.navigate("achievements")
            }, modifier = Modifier.padding(horizontal = 24.dp))
        }
    }
}

// ── Путь до Мадрида ─────────────────────────────────────────
@Composable
private fun PathToMadridCard(
    league: League,
    peakLeague: League,
    leagueProgress: Float,
    modifier: Modifier = Modifier
) {
    val accent = Color(league.accentColorHex)
    val next = LeagueResolver.next(league)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiFlags, null, tint = accent, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Путь до Мадрида", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(league.emoji, fontSize = 36.sp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(league.city, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = accent)
                    Text(league.region, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { leagueProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = accent,
                trackColor = accent.copy(alpha = 0.15f)
            )
            Spacer(Modifier.height(8.dp))
            if (next != null) {
                Text(
                    "Следующая остановка: ${next.emoji} ${next.city}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    "👑 Ты дошёл до столицы!",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }
            Spacer(Modifier.height(16.dp))
            LeaguePath(currentTier = league.tier, peakTier = peakLeague.tier)
        }
    }
}

// ── Skill Rating + кнопка к лидерборду ──────────────────────
@Composable
private fun SkillRatingCard(
    current: Int,
    peak: Int,
    onLeaderboardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Мой рейтинг", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(current.toString(), fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Text("Лучший: $peak", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FilledTonalButton(onClick = onLeaderboardClick) {
                Icon(Icons.Default.Leaderboard, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Лидеры")
            }
        }
    }
}

// ── Прогресс по темам (испанские флаги) ─────────────────────
@Composable
private fun CategoryRatingCard(
    items: List<CategoryRatingUi>,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Прогресс по темам", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = onSeeAll) { Text("Все") }
            }
            Spacer(Modifier.height(8.dp))
            if (items.isEmpty()) {
                Text("Начни тренировки, чтобы увидеть свой прогресс по темам.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                items.take(6).forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (item.icon != null) {
                            Icon(item.icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("${item.learned}/${item.total} слов", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        SpanishFlagRating(filled = item.flags)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(name: String, level: String, appLevel: Int, progress: Float, photoUrl: String?, onAvatarClick: (() -> Unit)? = null) {
    val context = LocalContext.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                modifier = Modifier.size(100.dp).then(
                    if (onAvatarClick != null) Modifier.clickable { onAvatarClick() } else Modifier
                ),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 2.dp
            ) {
                if (photoUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Аватар",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text(name.take(1).uppercase(), fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            Box(modifier = Modifier.offset(x = (-4).dp, y = (-4).dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(level, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(name, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.ExtraBold)
        Text("Nivel $appLevel", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text("Следующий уровень через ${( (1f-progress)*100).toInt()} XP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun StatBox(value: String, label: String, icon: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 4.dp, shadowElevation = 1.dp) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(icon, fontSize = 24.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun WeeklyActivityChart(
    history: List<DailyXpPoint>,
    modifier: Modifier = Modifier
) {
    // Если истории ещё нет (первый запуск, нет данных) — показываем
    // 7 пустых столбиков с подписями последних 7 дней.
    val points = if (history.size == 7) history else {
        (0..6).map { offset ->
            DailyXpPoint(
                date = java.time.LocalDate.now().minusDays((6 - offset).toLong()),
                xp = 0
            )
        }
    }
    val maxXp = (points.maxOfOrNull { it.xp } ?: 0).coerceAtLeast(1)
    val ruDayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val total = points.sumOf { it.xp }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Активность за неделю",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "+$total XP",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                points.forEach { point ->
                    val ratio = point.xp.toFloat() / maxXp
                    val dayIdx = point.date.dayOfWeek.value - 1  // 1..7 → 0..6
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Минимальная высота 4% чтобы пустые дни были видны как точки
                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .fillMaxHeight(ratio.coerceAtLeast(0.04f))
                                .clip(CircleShape)
                                .background(
                                    if (point.xp > 0)
                                        Brush.verticalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                            )
                                        )
                                    else
                                        Brush.verticalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                            )
                                        )
                                )
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            ruDayNames[dayIdx],
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementsSection(unlocked: Int, total: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.EmojiEvents, null, tint = Color.White)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Logros", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Desbloqueado $unlocked de $total", style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(16.dp).graphicsLayer(rotationZ = 180f))
        }
    }
}
