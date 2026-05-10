package com.spanishapp.ui.profile


import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.spanishapp.data.db.dao.AchievementDao
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.entity.AchievementEntity
import com.spanishapp.data.db.entity.UserProgressEntity
import com.spanishapp.data.repository.AuthRepository
import com.spanishapp.domain.algorithm.League
import com.spanishapp.domain.algorithm.LeagueResolver
import com.spanishapp.domain.algorithm.MasteryRating
import com.spanishapp.domain.algorithm.XpSystem
import com.spanishapp.ui.components.PressableCard
import com.spanishapp.ui.components.StaggeredEntrance
import com.spanishapp.ui.flashcards.CategoryMeta
import com.spanishapp.ui.home.PathTileTrophyBackdrop
import com.spanishapp.ui.home.drawCityAnchor
import com.spanishapp.ui.home.drawCityBridge
import com.spanishapp.ui.home.drawCityCathedral
import com.spanishapp.ui.home.drawCityCrown
import com.spanishapp.ui.home.drawCityGiralda
import com.spanishapp.ui.home.drawCityHouse
import com.spanishapp.ui.home.drawCityOrange
import com.spanishapp.ui.home.drawCitySagrada
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════
//  ViewModel — без изменений в логике, только добавлен поток
//  достижений для тизера-секции на экране.
// ═══════════════════════════════════════════════════════════

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

    private val _localPhotoUri = MutableStateFlow<Uri?>(null)
    val localPhotoUri: StateFlow<Uri?> = _localPhotoUri.asStateFlow()

    private val _isPhotoUploading = MutableStateFlow(false)
    val isPhotoUploading: StateFlow<Boolean> = _isPhotoUploading.asStateFlow()

    val achievements: StateFlow<List<AchievementEntity>> =
        achievementDao.getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
                runCatching { authRepository.setUserPhotoUrl(uri.toString()) }
            } finally {
                _isPhotoUploading.value = false
            }
        }
    }

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
        viewModelScope.launch { refreshCategoryRatings() }
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

data class DailyXpPoint(
    val date: java.time.LocalDate,
    val xp: Int
)

// ═══════════════════════════════════════════════════════════
//  Палитра акцентов — те же оттенки, что и на HomeScreen.
// ═══════════════════════════════════════════════════════════
private val AccentOrange  = Color(0xFFFF9500)   // brand-warm — kept for Path-to-Madrid only
private val AccentGold    = Color(0xFFFFC107)   // legacy, no longer used in section accents
private val AccentTeal    = Color(0xFF06B6D4)
private val AccentGreen   = Color(0xFF22C55E)
private val AccentBlue    = Color(0xFF3B82F6)
private val AccentPurple  = Color(0xFF7C3AED)
private val AccentViolet  = Color(0xFF8B5CF6)
private val AccentEmerald = Color(0xFF10B981)
private val AccentRose    = Color(0xFFF43F5E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    vm: ProfileViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val xpHistory by vm.xpHistory.collectAsState()
    val achievements by vm.achievements.collectAsState()
    val localPhotoUri by vm.localPhotoUri.collectAsState()
    val isPhotoUploading by vm.isPhotoUploading.collectAsState()
    val p = state.progress
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> if (uri != null) vm.onPhotoPicked(context, uri) }

    val effectivePhotoUrl: String? = localPhotoUri?.toString() ?: state.photoUrl
    val todayXp = xpHistory.lastOrNull()?.xp ?: 0
    val appLevel = XpSystem.levelForXp(p.totalXp)
    val appLevelProgress = XpSystem.progressToNextLevel(p.totalXp)
    val league = LeagueResolver.fromTier(p.currentLeague.coerceAtLeast(1))
    val peakLeague = LeagueResolver.fromTier(p.peakLeague.coerceAtLeast(1))
    val leagueProgress = LeagueResolver.progressInLeague(p.skillRating)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                title = { Text("Профиль", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
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
                .padding(bottom = 32.dp)
        ) {
            StaggeredEntrance(index = 0) {
                Column {
                    HeroBlock(
                        name = state.authName.ifBlank { p.displayName }.ifBlank {
                            androidx.compose.ui.res.stringResource(com.spanishapp.R.string.profile_default_name)
                        },
                        photoUrl = effectivePhotoUrl,
                        isPhotoUploading = isPhotoUploading,
                        league = league,
                        onAvatarClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }

            // ── SKILL RATING ────────────────────────────────────
            StaggeredEntrance(index = 1) {
                Column {
                    SectionHeader("SKILL RATING", AccentPurple, modifier = Modifier.padding(horizontal = 24.dp))
                    Spacer(Modifier.height(8.dp))
                    SkillRatingTile(
                        rating = p.skillRating,
                        appLevel = appLevel,
                        appLevelProgress = appLevelProgress,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(20.dp))
                }
            }

            // ── АКТИВНОСТЬ ──────────────────────────────────────
            StaggeredEntrance(index = 2) {
                Column {
                    SectionHeader("АКТИВНОСТЬ", AccentViolet, modifier = Modifier.padding(horizontal = 24.dp))
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActivityStatTile("🔥", p.currentStreak.toString(), "СЕРИЯ ДНЕЙ", AccentViolet,  Modifier.weight(1f))
                        ActivityStatTile("⭐", p.totalXp.toString(),       "XP ВСЕГО",    AccentEmerald, Modifier.weight(1f))
                        ActivityStatTile("🎯", todayXp.toString(),         "XP СЕГОДНЯ",  AccentTeal,    Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }

            // ── ПУТЬ ДО МАДРИДА (единственная тёплая секция) ────
            StaggeredEntrance(index = 3) {
                Column {
                    SectionHeader("ПУТЬ ДО МАДРИДА", AccentOrange, modifier = Modifier.padding(horizontal = 24.dp))
                    Spacer(Modifier.height(8.dp))
                    PathToMadridTile(
                        league = league,
                        peakLeague = peakLeague,
                        leagueProgress = leagueProgress,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(20.dp))
                }
            }

            // ── СТАТИСТИКА ──────────────────────────────────────
            StaggeredEntrance(index = 4) {
                Column {
                    SectionHeader("СТАТИСТИКА", AccentGreen, modifier = Modifier.padding(horizontal = 24.dp))
                    Spacer(Modifier.height(8.dp))
                    StatsTile(
                        wordsLearned = state.learnedCount,
                        lessonsDone = p.lessonsCompleted,
                        longestStreak = p.longestStreak,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(20.dp))
                }
            }

            // ── АКТИВНОСТЬ ЗА НЕДЕЛЮ ────────────────────────────
            StaggeredEntrance(index = 5) {
                Column {
                    SectionHeader("АКТИВНОСТЬ ЗА НЕДЕЛЮ", AccentBlue, modifier = Modifier.padding(horizontal = 24.dp))
                    Spacer(Modifier.height(8.dp))
                    WeeklyHeatmapTile(
                        history = xpHistory,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(20.dp))
                }
            }

            // ── ДОСТИЖЕНИЯ ──────────────────────────────────────
            StaggeredEntrance(index = 6) {
                Column {
                    SectionHeader(
                        "ДОСТИЖЕНИЯ",
                        AccentRose,
                        trailing = "↗",
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    AchievementsTeaserTile(
                        achievements = achievements,
                        unlocked = state.unlockedAchievements,
                        total = state.totalAchievements,
                        onClick = { navController.navigate("achievements") },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  HERO — аватар + ник + лига-pill
// ═══════════════════════════════════════════════════════════
@Composable
private fun HeroBlock(
    name: String,
    photoUrl: String?,
    isPhotoUploading: Boolean,
    league: League,
    onAvatarClick: () -> Unit
) {
    val context = LocalContext.current
    val accent = Color(league.accentColorHex)
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            // Neutral dark grey base + radial gradient with the league's
            // accent for personality. Was `primaryContainer` which renders
            // as a muddy brown in dark theme — exactly what the user
            // disliked.
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .border(BorderStroke(2.dp, accent), CircleShape)
                    .clickable { onAvatarClick() },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shadowElevation = 4.dp
            ) {
                if (photoUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(photoUrl).crossfade(true).build(),
                        contentDescription = "Аватар",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.radialGradient(
                                    colors = listOf(accent.copy(alpha = 0.35f), Color.Transparent),
                                    radius = 140f
                                )
                            )
                    ) {
                        Text(
                            name.take(1).uppercase(),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (isPhotoUploading) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f))
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), color = Color.White, strokeWidth = 3.dp)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            name,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = accent.copy(alpha = 0.15f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(league.emoji, fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text(league.city, color = accent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  ProfileTile — переиспользуемый bento-tile с полоской слева
//  (зеркалит BentoTile из HomeScreen).
// ═══════════════════════════════════════════════════════════
@Composable
private fun ProfileTile(
    accent: Color,
    modifier: Modifier = Modifier,
    height: Dp? = null,
    onClick: (() -> Unit)? = null,
    watermark: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val baseColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val tileMod = if (height != null) modifier.height(height) else modifier

    val inner: @Composable () -> Unit = {
        Box(modifier = Modifier.fillMaxSize()) {
            // Радиальное свечение accent в правом-верхнем углу.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = 0.16f), Color.Transparent),
                            center = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, 0f),
                            radius = 320f
                        )
                    )
            )
            // Optional thematic watermark (drawn ABOVE the radial glow but
            // BELOW the foreground content + left accent stripe).
            if (watermark != null) {
                watermark()
            }
            // Левая полоска 6dp.
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(accent)
            )
            Column(
                modifier = Modifier.fillMaxSize().padding(start = 18.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                content = content
            )
        }
    }

    if (onClick != null) {
        PressableCard(
            onClick = onClick,
            modifier = tileMod,
            shape = RoundedCornerShape(22.dp),
            backgroundColor = baseColor,
            shadowElevation = 6.dp
        ) { inner() }
    } else {
        Surface(
            modifier = tileMod,
            shape = RoundedCornerShape(22.dp),
            color = baseColor,
            shadowElevation = 6.dp
        ) { inner() }
    }
}

// ═══════════════════════════════════════════════════════════
//  Section header — UPPERCASE label с опц. trailing значком.
// ═══════════════════════════════════════════════════════════
@Composable
private fun SectionHeader(
    label: String,
    accent: Color,
    trailing: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp,
            color = accent,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            Text(trailing, fontSize = 14.sp, color = accent, fontWeight = FontWeight.Bold)
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  ⭐ SKILL RATING tile
// ═══════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SkillRatingTile(
    rating: Int,
    appLevel: Int,
    appLevelProgress: Float,
    modifier: Modifier = Modifier
) {
    var showInfo by remember { mutableStateOf(false) }

    ProfileTile(
        accent = AccentPurple,
        modifier = modifier.fillMaxWidth(),
        height = 160.dp,
        watermark = {
            // Trophy silhouette in the upper-right area — same vector used
            // by the bento RATING tile, painted faintly so it doesn't
            // collide with the 64sp hero number on the left.
            com.spanishapp.ui.home.ThematicWatermark(
                theme = com.spanishapp.ui.home.WatermarkTheme.RATING,
                accent = AccentPurple
            )
        }
    ) {
        Text(
            rating.toString(),
            fontSize = 64.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-2).sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 64.sp,
            modifier = Modifier.clickable { showInfo = true }
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Nivel $appLevel · ещё ${((1f - appLevelProgress) * 100).toInt()} XP",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { appLevelProgress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = AccentPurple,
            trackColor = AccentPurple.copy(alpha = 0.18f)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "${(appLevelProgress * 100).toInt()}%",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AccentPurple
        )
    }

    if (showInfo) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ModalBottomSheet(
            onDismissRequest = { showInfo = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            RatingInfoSheetContent(accent = AccentPurple)
        }
    }
}

@Composable
private fun RatingInfoSheetContent(accent: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            "КАК РАБОТАЕТ РЕЙТИНГ?",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color = accent
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Рейтинг растёт за правильные ответы и падает за ошибки. " +
                "Чем выше лига — тем меньше прирост за каждый ответ.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        Spacer(Modifier.height(16.dp))
        Text(
            "K-фактор по лигам",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        val tiers = listOf(
            "Aldea" to "±12",
            "Santiago" to "±8",
            "Bilbao" to "±6",
            "Zaragoza" to "±5",
            "Valencia" to "±4",
            "Sevilla" to "±3",
            "Barcelona" to "±2.5",
            "Madrid" to "±2"
        )
        tiers.forEach { (city, k) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    city,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    k,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        Spacer(Modifier.height(16.dp))
        RatingInfoLine("⏱", "Дневной лимит: +40 рейтинга в день")
        RatingInfoLine("🎯", "Близко к новой лиге: гейн × 0.5")
        RatingInfoLine("⏰", "За одно слово — раз в 24ч")
        RatingInfoLine("🎮", "Игры: половинный гейн (это тренажёр)")
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        Spacer(Modifier.height(16.dp))
        RatingInfoLine("📉", "Если не заниматься > 3 дней — −2/день")
    }
}

@Composable
private fun RatingInfoLine(emoji: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 18.sp)
        Spacer(Modifier.width(12.dp))
        Text(
            text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 20.sp
        )
    }
}

// ═══════════════════════════════════════════════════════════
//  Activity stat tile (3 в ряд)
// ═══════════════════════════════════════════════════════════
@Composable
private fun ActivityStatTile(
    emoji: String,
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    ProfileTile(accent = accent, modifier = modifier, height = 110.dp) {
        Text(emoji, fontSize = 18.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        Spacer(Modifier.weight(1f))
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

// ═══════════════════════════════════════════════════════════
//  🏛 PATH TO MADRID — горизонтальный путь + следующая остановка
// ═══════════════════════════════════════════════════════════
@Composable
private fun PathToMadridTile(
    league: League,
    peakLeague: League,
    leagueProgress: Float,
    modifier: Modifier = Modifier
) {
    val accent = AccentOrange
    val next = LeagueResolver.next(league)
    val cities = LeagueResolver.LEAGUES
    val shortNames = listOf("Aldea", "Sant.", "Bilbao", "Zar.", "Val.", "Sev.", "Barc.", "Mad.")
    val outlineFaint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

    ProfileTile(
        accent = accent,
        modifier = modifier.fillMaxWidth(),
        watermark = {
            PathTileTrophyBackdrop(accent = accent)
        }
    ) {
        // City glyphs + connector lines
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            cities.forEachIndexed { index, city ->
                val isCurrent = city.tier == league.tier
                val isPassed = city.tier < league.tier ||
                        (peakLeague.tier >= city.tier && city.tier <= league.tier)
                val glyphColor = when {
                    isCurrent -> accent
                    isPassed  -> accent.copy(alpha = 0.7f)
                    else      -> outlineFaint
                }
                val filled = isCurrent || isPassed
                val glyphSizeDp = if (isCurrent) 26.dp else 22.dp
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.size(glyphSizeDp)
                ) {
                    val s = size.minDimension
                    val c = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                    when (index) {
                        0 -> drawCityHouse(c, s, glyphColor, filled)
                        1 -> drawCityCathedral(c, s, glyphColor, filled)
                        2 -> drawCityAnchor(c, s, glyphColor, filled)
                        3 -> drawCityBridge(c, s, glyphColor, filled)
                        4 -> drawCityOrange(c, s, glyphColor, filled)
                        5 -> drawCityGiralda(c, s, glyphColor, filled)
                        6 -> drawCitySagrada(c, s, glyphColor, filled)
                        else -> drawCityCrown(c, s, glyphColor, filled)
                    }
                }
                if (index < cities.lastIndex) {
                    val lineColor =
                        if (city.tier < league.tier) accent.copy(alpha = 0.55f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(lineColor)
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        // Подписи под точками
        Row(modifier = Modifier.fillMaxWidth()) {
            shortNames.forEachIndexed { i, label ->
                val isCurrent = (i + 1) == league.tier
                Text(
                    label,
                    fontSize = 9.sp,
                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Normal,
                    color = if (isCurrent) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        if (next != null) {
            Text(
                "Следующая остановка:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${next.emoji} ${next.city}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { leagueProgress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = accent,
                trackColor = accent.copy(alpha = 0.18f)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${(leagueProgress * 100).toInt()}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        } else {
            Text(
                "👑 Ты дошёл до столицы!",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  📊 СТАТИСТИКА — 3 hero числа в ряд
// ═══════════════════════════════════════════════════════════
@Composable
private fun StatsTile(
    wordsLearned: Int,
    lessonsDone: Int,
    longestStreak: Int,
    modifier: Modifier = Modifier
) {
    ProfileTile(accent = AccentGreen, modifier = modifier.fillMaxWidth(), height = 110.dp) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatColumn(wordsLearned.toString(),  "СЛОВ",        Modifier.weight(1f))
            StatColumn(lessonsDone.toString(),   "УРОКОВ",      Modifier.weight(1f))
            StatColumn(longestStreak.toString(), "МАКС. СЕРИЯ", Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatColumn(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

// ═══════════════════════════════════════════════════════════
//  📈 WEEKLY HEATMAP — 7 квадратиков
// ═══════════════════════════════════════════════════════════
@Composable
private fun WeeklyHeatmapTile(
    history: List<DailyXpPoint>,
    modifier: Modifier = Modifier
) {
    val points = if (history.size == 7) history else (0..6).map { offset ->
        DailyXpPoint(
            date = java.time.LocalDate.now().minusDays((6 - offset).toLong()),
            xp = 0
        )
    }
    val total = points.sumOf { it.xp }
    val labels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    ProfileTile(accent = AccentBlue, modifier = modifier.fillMaxWidth(), height = 130.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            Text(
                "+$total XP",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { point ->
                val color = when {
                    point.xp == 0  -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    point.xp < 10  -> AccentBlue.copy(alpha = 0.35f)
                    point.xp < 30  -> AccentBlue.copy(alpha = 0.65f)
                    else           -> AccentBlue
                }
                val dayIdx = point.date.dayOfWeek.value - 1
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        labels[dayIdx],
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
//  🏆 ACHIEVEMENTS TEASER — топ-3 + кнопка
// ═══════════════════════════════════════════════════════════
@Composable
private fun AchievementsTeaserTile(
    achievements: List<AchievementEntity>,
    unlocked: Int,
    total: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Сортировка: разблокированные сверху, потом самые «близкие» к разблокировке.
    val top3 = achievements.take(3)
    ProfileTile(accent = AccentRose, modifier = modifier.fillMaxWidth(), onClick = onClick) {
        if (top3.isEmpty()) {
            Text(
                "Достижения скоро появятся.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            top3.forEachIndexed { index, ach ->
                AchievementRow(ach = ach, index = index)
                if (index < top3.lastIndex) Spacer(Modifier.height(8.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Показать все ($unlocked / $total) →",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun AchievementRow(ach: AchievementEntity, index: Int) {
    val medal = when (index) {
        0 -> "🥇"
        1 -> "🥈"
        2 -> "🥉"
        else -> "🏅"
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(medal, fontSize = 18.sp)
        Spacer(Modifier.width(10.dp))
        Text(
            ach.titleRu,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        if (ach.isUnlocked) {
            Text(
                "Получено",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGreen
            )
        } else {
            val req = ach.requirement.coerceAtLeast(1)
            Text(
                "0/$req",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
