package com.spanishapp.ui.profile


import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
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

    /** Локальный Uri выбранного фото — показываем мгновенно, пока идёт загрузка. */
    private val _localPhotoUri = MutableStateFlow<Uri?>(null)
    val localPhotoUri: StateFlow<Uri?> = _localPhotoUri.asStateFlow()

    private val _isPhotoUploading = MutableStateFlow(false)
    val isPhotoUploading: StateFlow<Boolean> = _isPhotoUploading.asStateFlow()

    /**
     * Photo picker callback: показываем сразу локально, грузим в Firebase Storage,
     * сохраняем URL в DataStore (его подхватит и HomeScreen). Если Firebase падает —
     * локальный Uri остаётся как fallback.
     */
    fun onPhotoPicked(context: android.content.Context, uri: Uri) {
        _localPhotoUri.value = uri
        _isPhotoUploading.value = true
        viewModelScope.launch {
            try {
                val auth = FirebaseAuth.getInstance()
                var user = auth.currentUser
                if (user == null) {
                    user = auth.signInAnonymously().await().user
                }
                val uid = user?.uid ?: throw IllegalStateException("No user uid")

                val bitmap = context.contentResolver.openInputStream(uri).use { stream ->
                    BitmapFactory.decodeStream(stream)
                } ?: throw IllegalStateException("Can't decode bitmap")

                val baos = ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, baos)
                val data = baos.toByteArray()

                val storage = FirebaseStorage.getInstance()
                val ref = storage.reference.child("users/$uid/avatar.jpg")
                ref.putBytes(data).await()
                val downloadUrl = ref.downloadUrl.await().toString()
                authRepository.setUserPhotoUrl(downloadUrl)
                Log.d("ProfileVM", "Avatar uploaded: $downloadUrl")
            } catch (e: Exception) {
                Log.w("ProfileVM", "Avatar upload failed, keeping local Uri", e)
                // Фоллбэк: сохраним локальный Uri как «фото» — Coil умеет читать content://
                runCatching { authRepository.setUserPhotoUrl(uri.toString()) }
            } finally {
                _isPhotoUploading.value = false
            }
        }
    }

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
                labelRes = info.labelRes,
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
    @androidx.annotation.StringRes val labelRes: Int,
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
    val localPhotoUri by vm.localPhotoUri.collectAsState()
    val isPhotoUploading by vm.isPhotoUploading.collectAsState()
    val p = state.progress
    val context = LocalContext.current

    // Modern photo picker — нет runtime permission, поддерживается с Android 13,
    // на старых версиях системой подменяется на legacy intent автоматически.
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) vm.onPhotoPicked(context, uri)
    }
    val effectivePhotoUrl: String? = localPhotoUri?.toString() ?: state.photoUrl
    val todayXp = xpHistory.lastOrNull()?.xp ?: 0
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
                title = {
                    com.spanishapp.ui.components.AnimatedScreenTitle(
                        text = "👤 Mi Perfil",
                        fontSize = 18.sp
                    )
                },
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
            HeroBlock(
                name = state.authName.ifBlank { p.displayName }.ifBlank { androidx.compose.ui.res.stringResource(com.spanishapp.R.string.profile_default_name) },
                photoUrl = effectivePhotoUrl,
                isPhotoUploading = isPhotoUploading,
                league = league,
                skillRating = p.skillRating,
                appLevel = appLevel,
                appLevelProgress = progress,
                onAvatarClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
            Spacer(Modifier.height(20.dp))
            // ── 3 цветные counter-pill ─────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CounterPill(
                    icon = "🔥",
                    value = "${p.currentStreak}",
                    label = "Серия",
                    bg = Color(0xFFFFE0CC),
                    fg = Color(0xFFB8431B),
                    modifier = Modifier.weight(1f)
                )
                CounterPill(
                    icon = "⭐",
                    value = "${p.totalXp}",
                    label = "XP всего",
                    bg = Color(0xFFFFF1C2),
                    fg = Color(0xFF8A6A00),
                    modifier = Modifier.weight(1f)
                )
                CounterPill(
                    icon = "🎯",
                    value = "$todayXp",
                    label = "XP сегодня",
                    bg = Color(0xFFD7F0DC),
                    fg = Color(0xFF1F7A3A),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(16.dp))
            // ── Прогресс до следующей лиги ─────────────────────
            LeagueProgressCard(
                league = league,
                leagueProgress = leagueProgress,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(16.dp))
            // ── Mini-stats: 3 колонки ──────────────────────────
            MiniStatsCard(
                wordsLearned = state.learnedCount,
                lessonsDone = p.lessonsCompleted,
                longestStreak = p.longestStreak,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(20.dp))
            WeeklyActivityChart(
                history = xpHistory,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
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
        shadowElevation = 2.dp
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
        shadowElevation = 2.dp
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
        shadowElevation = 2.dp
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
                            Text(androidx.compose.ui.res.stringResource(item.labelRes), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
                shadowElevation = 2.dp
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
    Surface(modifier = modifier, shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 3.dp) {
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
        shadowElevation = 2.dp
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

// ── HERO: большой аватар + ник + лига + skill rating ───────
@Composable
private fun HeroBlock(
    name: String,
    photoUrl: String?,
    isPhotoUploading: Boolean,
    league: League,
    skillRating: Int,
    appLevel: Int,
    appLevelProgress: Float,
    onAvatarClick: () -> Unit
) {
    val context = LocalContext.current
    val accent = Color(league.accentColorHex)
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                modifier = Modifier.size(96.dp).clickable { onAvatarClick() },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 4.dp
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
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            name.take(1).uppercase(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                if (isPhotoUploading) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f))) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), color = Color.White, strokeWidth = 3.dp)
                    }
                }
            }
            // FAB-камера снизу справа (как маркер «можно тапнуть»)
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(6.dp))
        // Лига badge
        Row(
            modifier = Modifier.clip(CircleShape).background(accent.copy(alpha = 0.15f)).padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(league.emoji, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Text(league.city, color = accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(Modifier.height(8.dp))
        // Skill rating большим шрифтом
        Text(
            skillRating.toString(),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text("Skill rating", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        // Тонкий прогресс-бар уровня XP
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            LinearProgressIndicator(
                progress = { appLevelProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Nivel $appLevel · ещё ${((1f - appLevelProgress) * 100).toInt()} XP",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Цветная пилюля счётчика ────────────────────────────────
@Composable
private fun CounterPill(
    icon: String,
    value: String,
    label: String,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = bg,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 20.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = fg)
            Text(label, fontSize = 10.sp, color = fg.copy(alpha = 0.8f))
        }
    }
}

// ── Карточка прогресса до следующей лиги (с анимацией) ────
@Composable
private fun LeagueProgressCard(
    league: League,
    leagueProgress: Float,
    modifier: Modifier = Modifier
) {
    val accent = Color(league.accentColorHex)
    val next = LeagueResolver.next(league)
    val animated by animateFloatAsState(
        targetValue = leagueProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "leagueProgress"
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (next != null) "До следующей лиги" else "👑 Высшая лига",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${(animated * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = accent
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { animated },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                color = accent,
                trackColor = accent.copy(alpha = 0.15f)
            )
            if (next != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Следующая остановка: ${next.emoji} ${next.city}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Mini-stats: 3 колонки ──────────────────────────────────
@Composable
private fun MiniStatsCard(
    wordsLearned: Int,
    lessonsDone: Int,
    longestStreak: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            MiniStatColumn(
                icon = Icons.Default.CheckCircle,
                value = wordsLearned.toString(),
                label = "Слов",
                tint = Color(0xFF1F7A3A),
                modifier = Modifier.weight(1f)
            )
            VerticalDivider()
            MiniStatColumn(
                icon = Icons.Default.School,
                value = lessonsDone.toString(),
                label = "Уроков",
                tint = Color(0xFF3D5AFE),
                modifier = Modifier.weight(1f)
            )
            VerticalDivider()
            MiniStatColumn(
                icon = Icons.Default.LocalFireDepartment,
                value = longestStreak.toString(),
                label = "Макс. серия",
                tint = Color(0xFFB8431B),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MiniStatColumn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    )
}

@Composable
private fun AchievementsSection(unlocked: Int, total: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    com.spanishapp.ui.components.PressableCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp
    ) {
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
