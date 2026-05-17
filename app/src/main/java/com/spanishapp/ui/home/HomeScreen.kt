package com.spanishapp.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import java.time.LocalDate
import java.time.LocalTime
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.spanishapp.R
import com.spanishapp.data.db.entity.WordEntity
import com.spanishapp.ui.components.*
import kotlinx.coroutines.launch

// ── Roadmap Data Model ────────────────────────────────────────

data class RoadmapUnit(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val cefrLevel: String = "A1",
    val isLocked: Boolean = true,
    val progress: Float = 0f,
    val color: Color,
    val lessons: List<RoadmapLesson> = emptyList()
)

data class RoadmapLesson(
    val title: String,
    val type: String,
    val category: String = "general",
    val isCompleted: Boolean = false,
    val isPremium: Boolean = false
)

// ═══════════════════════════════════════════════════════════════
//  Palette
// ═══════════════════════════════════════════════════════════════

private val Orange      = Color(0xFFFF6B35)
private val Purple      = Color(0xFFFF6B35)
private val GoldColor   = Color(0xFFFF9500)
private val OrangeColor = Color(0xFFFF6B00)
private val TextGray    = Color(0xFF8E8E93)
private val LockGray    = Color(0xFFC7C7CC)

// CEFR pill colours.
// A1 = yellow, A2 = cyan, B1 = green, B2 = magenta/rose.
// B2 was previously orange — but every B2 unit inside RoadmapData uses
// a pink/rose shade (#9F1239..#E11D48). Pill now matches content.
private val A1Color = Color(0xFFEAB308)
private val A2Color = Color(0xFF06B6D4)
private val B1Color = Color(0xFF22C55E)
private val B2Color = Color(0xFFDB2777)

// Continue-pager per-page accents.
private val LessonAccent = Color(0xFFEAB308)
private val BookAccent   = Color(0xFF22C55E)
private val SetAccent    = Color(0xFFF97316)
private val WeakAccent   = Color(0xFF06B6D4)

// Bento per-tile accents — must all differ visually so the 2×2 doesn't look
// like two pairs. GoalAccent is coral-red to be distinct from RatingAccent.
private val RatingAccent = Color(0xFFFFC107)   // amber-gold
private val GoalAccent   = Color(0xFFEF4444)   // coral red

// League names per current_league index (1..8).
private val LEAGUE_NAMES = listOf(
    "Aldea", "Santiago", "Bilbao", "Zaragoza",
    "Valencia", "Sevilla", "Barcelona", "Madrid"
)

// ═══════════════════════════════════════════════════════════════
//  HOME SCREEN
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // ВНИМАНИЕ: FeatureTourGate отключён намеренно (v1.0.6).
    // В 1.0.5 автопоказ feature-tour ломал старт у уже зарегистрированных
    // юзеров (race с auth-flow → краш). Маршрут "feature_tour" остаётся
    // в Navigation.kt — можно добавить кнопку «Показать обзор» в Settings,
    // если решим возвращать tour. Не вызывать FeatureTourGate автоматически.

    val state         by viewModel.uiState.collectAsStateWithLifecycle()
    val wordOfDay     by viewModel.wordOfTheDay.collectAsStateWithLifecycle()
    val lastLesson    by viewModel.lastLessonInProgress.collectAsStateWithLifecycle()
    val lastBook      by viewModel.lastBookInProgress.collectAsStateWithLifecycle()
    val nextSet       by viewModel.nextIncompleteSet.collectAsStateWithLifecycle()
    val weakWord      by viewModel.weakSampleWord.collectAsStateWithLifecycle()
    val recentWords   by viewModel.recentWords.collectAsStateWithLifecycle()
    val weeklyMinutes by viewModel.weeklyMinutes.collectAsStateWithLifecycle()
    val dailyGoals    by viewModel.dailyGoals.collectAsStateWithLifecycle()
    val tts = rememberSpanishTts()
    val scope = rememberCoroutineScope()

    val today = remember { LocalDate.now() }
    val greetingRes = remember(today) { greetingResFor(LocalTime.now()) }
    val motivationRes = remember(today) { motivationResFor(today) }
    val greeting = stringResource(greetingRes)
    val motivation = stringResource(motivationRes)

    // For random-word and word-detail bottom sheets.
    var randomWord by remember { mutableStateOf<WordEntity?>(null) }
    var sheetWord by remember { mutableStateOf<WordEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        SpanishCitiesWatermark(modifier = Modifier.fillMaxSize())

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // ── Compact header (~72dp) ─────────────────────────
            item {
                StaggeredEntrance(index = 0) {
                    CompactHeader(
                        greeting       = greeting,
                        motivation     = motivation,
                        photoUrl       = state.userPhotoUrl,
                        onAvatar       = { navController.navigate("profile") { launchSingleTop = true } },
                        onSettings     = { navController.navigate("settings") { launchSingleTop = true } }
                    )
                }
            }

            // ── Stats bar (single row) ─────────────────────────
            item {
                StaggeredEntrance(index = 1) {
                    Column {
                        Spacer(Modifier.height(8.dp))
                        StatsBar(
                            streak       = state.currentStreak,
                            todayMinutes = state.todayMinutes,
                            goalMinutes  = state.dailyGoalMinutes,
                            todayXp      = state.todayXp,
                            skillRating  = state.skillRating,
                            league       = state.currentLeague,
                            hasPracticed = state.totalXp > 0,
                            onClick      = { navController.navigate("profile") { launchSingleTop = true } },
                            streakFreezes = state.streakFreezes,
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            // ── Continue Pager ─────────────────────────────────
            item {
                StaggeredEntrance(index = 2) {
                    Column {
                        ContinuePager(
                            lastLesson  = lastLesson,
                            lastBook    = lastBook,
                            nextSet     = nextSet,
                            weakWord    = weakWord,
                            onLesson    = { l ->
                                navController.navigate("lesson_intro/${l.unitId}/${l.lessonIndex}")
                            },
                            onBook      = { id -> navController.navigate("libro/$id") },
                            onSet       = { setId ->
                                navController.navigate(
                                    "flashcards_session?level=A1&category=all&direction=ES_TO_RU&setId=$setId"
                                )
                            },
                            onWeak      = { navController.navigate("practice") }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            // ── Word of Day mega-card with quiz pager ─────────
            wordOfDay?.let { word ->
                item {
                    StaggeredEntrance(index = 3) {
                        Column {
                            WordOfDayQuizCard(
                                word          = word,
                                tts           = tts,
                                viewModel     = viewModel,
                                navController = navController,
                                wodStreak     = state.wodStreak,
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }

            // ── Course pills (above Bento per user feedback) ──────────
            item {
                StaggeredEntrance(index = 4) {
                    Column {
                        CoursePills(
                            activeLevel = state.spanishLevel,
                            onClick = { lvl -> navController.navigate("course_detail/$lvl") }
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            // ── Bento 2×2 (premium redesign) ──────────────────────────
            item {
                StaggeredEntrance(index = 5) {
                    Column {
                        BentoRow(
                            book          = lastBook,
                            rating        = state.skillRating,
                            league        = state.currentLeague,
                            hasPracticed  = state.totalXp > 0,
                            recent        = recentWords,
                            goals         = dailyGoals,
                            onBookClick   = {
                                lastBook?.let {
                                    navController.navigate("libro/${it.libroId}") { launchSingleTop = true }
                                } ?: navController.navigate("game_libros") { launchSingleTop = true }
                            },
                            onLeagueClick = { navController.navigate("leaderboard") { launchSingleTop = true } },
                            onDictClick   = { navController.navigate("dictionary") { launchSingleTop = true } },
                            onWordChip    = { w -> sheetWord = w },
                            onGoalClick   = {
                                // Smart routing: jump to the next undone task.
                                fun nav(r: String) = navController.navigate(r) { launchSingleTop = true }
                                when {
                                    !dailyGoals.lessonCompleted        -> nav("course_detail/A1")
                                    !dailyGoals.flashcardSetCompleted  -> nav("flashcards")
                                    !dailyGoals.bookPageRead           -> nav("game_libros")
                                    !dailyGoals.wordOfDaySolved        -> { /* word-of-day is on Home itself */ }
                                    else -> { /* all done — informational only */ }
                                }
                            }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            // ── Streak heatmap ─────────────────────────────────
            item {
                StaggeredEntrance(index = 6) {
                    Column {
                        WeekHeatmap(weeklyMinutes)
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            // ── Quick Actions (very bottom per user feedback) ─────────
            item {
                StaggeredEntrance(index = 7) {
                    Column {
                        QuickActionsRow(
                            onRandom = {
                                scope.launch { randomWord = viewModel.pickRandomWord() }
                            },
                            onPronounce = { navController.navigate("pronunciation") { launchSingleTop = true } },
                            onWeak      = { navController.navigate("practice") },
                            onGame      = {
                                val games = listOf(
                                    "game_articles", "game_speed", "game_math",
                                    "game_crossword", "game_sopa", "game_palabra"
                                )
                                navController.navigate(games.random())
                            }
                        )
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }

        // ── AI-Chat FAB ─────────────────────────────────────────
        val entranceScale = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(400)
            entranceScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        val pulse by rememberInfiniteTransition(label = "fab_pulse").animateFloat(
            initialValue = 1f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "fab_pulse_scale"
        )
        val combinedScale = entranceScale.value * pulse
        val shadowDp = 8.dp + ((pulse - 1f) * 80f).dp

        FloatingActionButton(
            onClick = { navController.navigate("ai_chat_sessions") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .graphicsLayer {
                    scaleX = combinedScale
                    scaleY = combinedScale
                }
                .shadow(
                    elevation = shadowDp,
                    shape = CircleShape,
                    spotColor = Orange,
                    ambientColor = Orange
                )
                .size(60.dp),
            containerColor = Orange,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(R.drawable.ic_bull),
                contentDescription = stringResource(R.string.title_ai_chat),
                modifier = Modifier.size(30.dp)
            )
        }
    }

    // Bottom sheets for random / dictionary chip lookups.
    randomWord?.let { w ->
        WordPeekSheet(
            word = w,
            tts = tts,
            onDismiss = { randomWord = null },
            onOpen = {
                randomWord = null
                navController.navigate("dictionary") { launchSingleTop = true }
            }
        )
    }
    sheetWord?.let { w ->
        WordPeekSheet(
            word = w,
            tts = tts,
            onDismiss = { sheetWord = null },
            onOpen = {
                sheetWord = null
                navController.navigate("dictionary") { launchSingleTop = true }
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════════
//  PHASE 1 — Header / Stats / Continue Pager
// ═══════════════════════════════════════════════════════════════

@Composable
private fun CompactHeader(
    greeting: String,
    motivation: String,
    photoUrl: String?,
    onAvatar: () -> Unit,
    onSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val context = LocalContext.current
        // Matches Profile + Settings: surfaceContainerHighest base + primary
        // accent ring. Was solid Purple — clashed with the rest of the
        // home palette and made the avatar look like a CTA button.
        Surface(
            modifier = Modifier
                .size(44.dp)
                .border(
                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onAvatar
                ),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            shadowElevation = 2.dp
        ) {
            if (photoUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.home_profile_cd),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Animated greeting reused — replayKey on greeting so morning →
            // afternoon transitions re-trigger the staggered reveal.
            Text(
                greeting,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
            Text(
                motivation,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onSettings) {
            Icon(
                Icons.Default.Settings,
                contentDescription = stringResource(R.string.home_settings_cd),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatsBar(
    streak: Int,
    todayMinutes: Int,
    goalMinutes: Int,
    todayXp: Int,
    skillRating: Int,
    league: Int,
    hasPracticed: Boolean,
    onClick: () -> Unit,
    streakFreezes: Int = 0,
) {
    val progress = if (goalMinutes > 0) (todayMinutes.toFloat() / goalMinutes).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(progress, tween(500), label = "stats_ring")
    // Until the user does the first review, hide the seed-1000 rating —
    // it confused users into thinking they had already played.
    val leagueName = if (hasPracticed) LEAGUE_NAMES.getOrElse(league - 1) { "Aldea" } else "—"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .heightIn(min = 56.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Стрик с бэйджем заморозок: ❄ × N — даёт юзеру понять что
            // у него есть «страховка» (раньше freezes тратились молча,
            // юзер не знал об их существовании).
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                StatCell(emoji = "🔥", text = "$streak", color = OrangeColor, modifier = Modifier.weight(1f, fill = false))
                if (streakFreezes > 0) {
                    Text(
                        text = "❄$streakFreezes",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF60A5FA),  // лёд-голубой
                        modifier = Modifier.padding(start = 2.dp),
                    )
                }
            }

            // Daily-goal mini ring + minutes label.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1.2f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(24.dp),
                        color = OrangeColor.copy(alpha = 0.18f),
                        strokeWidth = 3.dp,
                        trackColor = Color.Transparent
                    )
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(24.dp),
                        color = OrangeColor,
                        strokeWidth = 3.dp,
                        trackColor = Color.Transparent
                    )
                }
                Text(
                    stringResource(R.string.home_daily_minutes_template, todayMinutes, goalMinutes),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            StatCell(emoji = "⭐", text = "$todayXp XP", color = GoldColor, modifier = Modifier.weight(1f))

            // League cell — emoji + city + rating.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1.4f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("🏅", fontSize = 14.sp)
                Column {
                    Text(
                        leagueName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (hasPracticed) "$skillRating" else "—",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCell(emoji: String, text: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(emoji, fontSize = 14.sp)
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color, maxLines = 1)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ContinuePager(
    lastLesson: ContinueLesson?,
    lastBook: com.spanishapp.data.db.entity.LibroProgressEntity?,
    nextSet: com.spanishapp.ui.flashcards.FlashcardSet?,
    weakWord: WordEntity?,
    onLesson: (ContinueLesson) -> Unit,
    onBook: (Int) -> Unit,
    onSet: (String) -> Unit,
    onWeak: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
        Text(
            stringResource(R.string.home_continue_label),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(140.dp),
            pageSpacing = 8.dp
        ) { page ->
            when (page) {
                0 -> ContinueCard(
                    title    = stringResource(R.string.home_tile_lesson_title),
                    subtitle = lastLesson?.let { "${it.unitTitle} · ${it.lessonTitle}" }
                        ?: stringResource(R.string.home_tile_lesson_empty),
                    cta      = stringResource(R.string.home_tile_continue),
                    accent   = LessonAccent,
                    theme    = WatermarkTheme.LESSON,
                    enabled  = lastLesson != null,
                    onClick  = { lastLesson?.let(onLesson) }
                )
                1 -> ContinueCard(
                    title    = stringResource(R.string.home_tile_book_title),
                    subtitle = lastBook?.let { "Libro #${it.libroId} · ${it.bestScore}%" }
                        ?: stringResource(R.string.home_tile_book_empty),
                    cta      = stringResource(R.string.home_tile_book_cta),
                    accent   = BookAccent,
                    theme    = WatermarkTheme.BOOK,
                    enabled  = true,
                    onClick  = { lastBook?.let { onBook(it.libroId) } ?: onBook(1) }
                )
                2 -> ContinueCard(
                    title    = stringResource(R.string.home_tile_flashcard_title),
                    subtitle = nextSet?.let { "${it.emoji} ${it.title} · ${it.wordsSpanish.size} слов" }
                        ?: stringResource(R.string.home_tile_flashcard_empty),
                    cta      = stringResource(R.string.home_tile_flashcard_cta),
                    accent   = SetAccent,
                    theme    = WatermarkTheme.FLASHCARD_SET,
                    enabled  = nextSet != null,
                    onClick  = { nextSet?.let { onSet(it.id) } }
                )
                3 -> ContinueCard(
                    title    = stringResource(R.string.home_tile_weak_title),
                    subtitle = weakWord?.let { "${it.spanish} — ${it.russian}" }
                        ?: stringResource(R.string.home_tile_weak_empty),
                    cta      = stringResource(R.string.home_tile_weak_cta),
                    accent   = WeakAccent,
                    theme    = WatermarkTheme.WEAK_WORD,
                    enabled  = weakWord != null,
                    onClick  = onWeak
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        DotsIndicator(count = 4, current = pagerState.currentPage, accent = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ContinueCard(
    title: String,
    subtitle: String,
    cta: String,
    accent: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    theme: WatermarkTheme? = null
) {
    PressableCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(140.dp),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 4.dp,
        enabled = enabled
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Radial accent glow in the top-right corner — same "premium
            // light spot" effect used by BentoTile, applied here so the
            // Continue cards visually match the bento section below.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = 0.18f), Color.Transparent),
                            center = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, 0f),
                            radius = 320f
                        )
                    )
            )
            // Per-theme thematic watermark in the bottom-right area —
            // same system used by BentoTile so Continue cards visually
            // match the rest of the home surface.
            if (theme != null) {
                ThematicWatermark(theme = theme, accent = accent)
            }
            // Soft accent stripe on the left edge.
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(accent)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 18.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    title.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    letterSpacing = 1.sp
                )
                Text(
                    subtitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    lineHeight = 20.sp
                )
                Text(
                    cta,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) accent else TextGray
                )
            }
        }
    }
}

@Composable
private fun DotsIndicator(count: Int, current: Int, accent: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { i ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (current == i) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (current == i) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  PHASE 2 — Word of Day mega-card with quiz pager
// ═══════════════════════════════════════════════════════════════

/**
 * Compact Word of Day card. Tapping anywhere opens a bottom-sheet with the
 * full 4-mode quiz (was previously embedded in the card itself, but that
 * made the card ~360dp tall with visual noise the user disliked).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun WordOfDayQuizCard(
    word: WordOfDay,
    tts: android.speech.tts.TextToSpeech?,
    viewModel: HomeViewModel,
    navController: NavHostController,
    wodStreak: Int = 0,
) {
    var showQuiz by remember { mutableStateOf(false) }

    Surface(
        onClick = { if (word.wordId != 0) showQuiz = true },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 4.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left brand stripe — primary accent.
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
            )
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(R.string.wod_label),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LevelPill(word.level)
                    Spacer(Modifier.weight(1f))
                    // 🔥 Стрик закреплённых слов дня — эмоциональная награда
                    // ровно там, где юзер закончил WoD-квиз. Показываем только
                    // если серия уже больше нуля, чтобы не мозолить глаза
                    // новичкам.
                    if (wodStreak > 0) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFFF6B35).copy(alpha = 0.14f),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                            ) {
                                Text("🔥", fontSize = 12.sp)
                                Text(
                                    wodStreak.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFF6B35),
                                )
                            }
                        }
                    }
                    if (word.wasPracticed) {
                        Text("✓", fontSize = 16.sp, color = Color(0xFF4CAF50))
                    }
                }

                Spacer(Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        word.spanish,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        lineHeight = 32.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        SpeakerButton(
                            text = word.spanish,
                            tts = tts,
                            tint = Color.White
                        )
                    }
                }

                Text(
                    word.russian,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        if (word.wasPracticed) stringResource(R.string.wod_tap_again) else stringResource(R.string.wod_tap_first),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("→", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    if (showQuiz) {
        WordOfDayQuizSheet(
            word = word,
            tts = tts,
            viewModel = viewModel,
            navController = navController,
            onDismiss = { showQuiz = false }
        )
    }
}

/**
 * Bottom sheet for Word-of-Day learning. 6-stage flow designed for
 * actual MEMORIZATION, not just "tap and forget":
 *
 *   0. REVEAL    — большая карточка слова + auto-TTS + перевод + пример
 *   1. LISTEN    — слышишь, выбираешь правильное написание
 *   2. TRANSLATE — выбираешь перевод (distractors из той же категории)
 *   3. SENTENCE  — вставить слово в реальное предложение
 *   4. TYPE      — печатаешь слово на слух (с прощением акцентов)
 *   5. COMPLETE  — 🎉 «Запомнил!» + XP + куда идти дальше
 *
 * Все стадии автоматически прогружаются вперёд по правильному ответу
 * (без ручного свайпа). Свайп назад — разрешён, на случай если хочется
 * послушать ещё раз.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun WordOfDayQuizSheet(
    word: WordOfDay,
    tts: android.speech.tts.TextToSpeech?,
    viewModel: HomeViewModel,
    navController: NavHostController,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val pagerState = rememberPagerState(pageCount = { 6 })
    val scope = rememberCoroutineScope()

    var distractors by remember(word.wordId) { mutableStateOf<List<WordEntity>>(emptyList()) }
    LaunchedEffect(word.wordId) {
        if (word.wordId != 0) distractors = viewModel.loadDistractors(word)
    }

    // Авто-переход к следующей стадии через 600мс после правильного
    // ответа — даёт юзеру моргнуть глазом и осознать «правильно», не
    // дёргаясь сразу. Reveal-стадия (page 0) переходит сама по таймеру.
    val autoAdvance: () -> Unit = {
        scope.launch {
            kotlinx.coroutines.delay(600)
            val next = pagerState.currentPage + 1
            if (next < 6) pagerState.animateScrollToPage(next)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        // Тёмная сцена за модальным окном — иначе главный экран
        // просвечивал и юзер жаловался «не читабельно».
        scrimColor = Color.Black.copy(alpha = 0.72f),
        contentWindowInsets = { androidx.compose.foundation.layout.WindowInsets(0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .padding(bottom = 24.dp)
        ) {
            // Compact header — название стадии + LevelPill + dots
            val stageLabel = when (pagerState.currentPage) {
                0 -> "✨ Знакомство"
                1 -> "🔊 На слух"
                2 -> "🌐 Перевод"
                3 -> "📝 В предложении"
                4 -> "⌨️ Напечатай"
                else -> "🎉 Готово"
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    stageLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                LevelPill(word.level)
                Text(
                    "${pagerState.currentPage + 1}/6",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(10.dp))
            // Линейный прогресс-бар вместо точек — лучше видно «куда дошёл»
            LinearProgressIndicator(
                progress = { (pagerState.currentPage + 1) / 6f },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
            Spacer(Modifier.height(16.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().height(360.dp),
                pageSpacing = 12.dp
            ) { page ->
                when (page) {
                    0 -> WodRevealStage(word, tts, onContinue = {
                        scope.launch { pagerState.animateScrollToPage(1) }
                    })
                    1 -> PronunciationQuiz(word, distractors, tts) {
                        viewModel.markWordOfDayPractised(); autoAdvance()
                    }
                    2 -> TranslationQuiz(word, distractors) {
                        viewModel.markWordOfDayPractised(); autoAdvance()
                    }
                    3 -> FillBlankQuiz(word, distractors, tts) {
                        viewModel.markWordOfDayPractised(); autoAdvance()
                    }
                    4 -> LetterAssemblyQuiz(word) {
                        viewModel.markWordOfDayPractised(); autoAdvance()
                    }
                    5 -> WodCompletionStage(
                        word = word,
                        onLessonClick = {
                            onDismiss()
                            navController.navigate("course_a1") { launchSingleTop = true }
                        },
                        onCardsClick = {
                            onDismiss()
                            navController.navigate("flashcards") { launchSingleTop = true }
                        },
                        onClose = onDismiss
                    )
                }
            }
        }
    }
}

/**
 * Stage 0 — REVEAL. Тихий «знакомительный» экран: огромное слово, авто-аудио,
 * перевод появляется с задержкой, под ним пример употребления. Цель —
 * включить визуальную, аудиальную и контекстную память одновременно.
 */
@Composable
private fun WodRevealStage(
    word: WordOfDay,
    tts: android.speech.tts.TextToSpeech?,
    onContinue: () -> Unit
) {
    var showRussian by remember(word.wordId) { mutableStateOf(false) }
    // Spaced exposure — 5-секундный countdown перед тем как разрешить
    // переход к проверке. Принуждает мозг подержать слово в фокусе,
    // не нажимая «дальше» механически.
    var secondsLeft by remember(word.wordId) { mutableStateOf(5) }

    // Авто-проигрывание озвучки при появлении (двукратно: сразу + через 1.5с)
    LaunchedEffect(word.wordId) {
        kotlinx.coroutines.delay(300)
        tts?.speak(word.spanish, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "wod-reveal-1")
        kotlinx.coroutines.delay(1500)
        showRussian = true
        kotlinx.coroutines.delay(800)
        tts?.speak(word.spanish, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "wod-reveal-2")
    }

    // Countdown 5 → 0
    LaunchedEffect(word.wordId) {
        while (secondsLeft > 0) {
            kotlinx.coroutines.delay(1000)
            secondsLeft -= 1
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Огромное слово с тенью
        Text(
            word.spanish,
            fontSize = 38.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            lineHeight = 42.sp
        )

        // Кнопка "повторить аудио"
        Surface(
            onClick = {
                tts?.speak(word.spanish, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "wod-replay")
            },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.VolumeUp,
                    contentDescription = "Прослушать",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Перевод появляется с задержкой
        AnimatedVisibility(visible = showRussian) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    word.russian,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                if (word.example.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Text(
                            "«${word.example}»",
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Подсказка-таймер пока countdown идёт
        if (secondsLeft > 0) {
            Text(
                "Запоминай... $secondsLeft",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
        }

        // Кнопка перехода к первой проверке (активна только после countdown)
        val ready = secondsLeft <= 0
        Button(
            onClick = onContinue,
            enabled = ready,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                disabledContentColor = Color.White.copy(alpha = 0.7f),
            )
        ) {
            Text(
                if (ready) "Готов проверить →" else "Готов проверить ($secondsLeft)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

/**
 * Stage 5 — COMPLETION. 🎉-экран после прохождения всех стадий.
 * Главное — даёт чёткое «что дальше», иначе юзер выпадает в пустоту.
 */
@Composable
private fun WodCompletionStage(
    word: WordOfDay,
    onLessonClick: () -> Unit,
    onCardsClick: () -> Unit,
    onClose: () -> Unit
) {
    // Triumph beep — играем 1 раз при первом показе финальной стадии.
    val sound = rememberAnswerSound()
    LaunchedEffect(word.wordId) { sound.correct() }

    Column(
        modifier = Modifier.fillMaxSize().padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎉", fontSize = 64.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "Запомнил!",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "«${word.spanish}» — ${word.russian}",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
        ) {
            Text(
                "+15 XP",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        Text(
            "Что дальше?",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onLessonClick,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text("📖 Урок", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            Button(
                onClick = onCardsClick,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) { Text("🃏 Карточки", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth().height(40.dp),
            shape = RoundedCornerShape(12.dp)
        ) { Text("Закрыть", fontSize = 13.sp) }
    }
}

@Composable
private fun LevelPill(level: String) {
    val color = when (level) {
        "A1" -> A1Color; "A2" -> A2Color; "B1" -> B1Color; "B2" -> B2Color
        else -> A1Color
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.85f))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(level, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
    }
}

// ── Quiz Mode 1 — Fill-in-blank ────────────────────────────────
@Composable
private fun FillBlankQuiz(
    word: WordOfDay,
    distractors: List<WordEntity>,
    tts: android.speech.tts.TextToSpeech?,
    onSolved: () -> Unit
) {
    val sentence = remember(word.wordId) {
        val ex = word.example.takeIf { it.isNotBlank() }
            ?: "${word.spanish} es muy útil"
        // Replace the target word with a blank (simple case-insensitive token swap).
        ex.replace(Regex("\\b${Regex.escape(word.spanish.substringAfterLast(' '))}\\b", RegexOption.IGNORE_CASE), "____")
    }
    val target = remember(word.wordId) { word.spanish.substringAfterLast(' ') }
    val options = remember(word.wordId, distractors) {
        (listOf(target) + distractors.map { it.spanish.substringAfterLast(' ') })
            .distinct().shuffled()
    }
    var picked by remember(word.wordId) { mutableStateOf<String?>(null) }
    var solved by remember(word.wordId) { mutableStateOf(false) }
    val sound = rememberAnswerSound()

    Column(
        modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            sentence,
            fontSize = 15.sp,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(12.dp))
        OptionButtons(
            options = options,
            correct = target,
            picked  = picked,
            onPick  = { p ->
                if (p == target) sound.correct() else sound.wrong()
                if (picked == null || picked != target) picked = p
                if (p == target && !solved) { solved = true; onSolved() }
            }
        )
    }
}

// ── Quiz Mode 2 — Translation choice ───────────────────────────
@Composable
private fun TranslationQuiz(
    word: WordOfDay,
    distractors: List<WordEntity>,
    onSolved: () -> Unit
) {
    val target = word.russian
    val options = remember(word.wordId, distractors) {
        (listOf(target) + distractors.map { it.russian }).distinct().shuffled()
    }
    var picked by remember(word.wordId) { mutableStateOf<String?>(null) }
    var solved by remember(word.wordId) { mutableStateOf(false) }
    val sound = rememberAnswerSound()

    Column(
        modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Что значит «${word.spanish}»?",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(Modifier.height(12.dp))
        OptionButtons(
            options = options,
            correct = target,
            picked  = picked,
            onPick  = { p ->
                if (p == target) sound.correct() else sound.wrong()
                if (picked == null || picked != target) picked = p
                if (p == target && !solved) { solved = true; onSolved() }
            }
        )
    }
}

// ── Quiz Mode 3 — Pronunciation choice ─────────────────────────
// Simplified: one big "Послушай" button that speaks the target word,
// then four Russian translation options to match.
@Composable
private fun PronunciationQuiz(
    word: WordOfDay,
    distractors: List<WordEntity>,
    tts: android.speech.tts.TextToSpeech?,
    onSolved: () -> Unit
) {
    val target = word.russian
    val options = remember(word.wordId, distractors) {
        (listOf(target) + distractors.map { it.russian }).distinct().shuffled()
    }
    var picked by remember(word.wordId) { mutableStateOf<String?>(null) }
    var solved by remember(word.wordId) { mutableStateOf(false) }
    val sound = rememberAnswerSound()

    Column(
        modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            onClick = {
                tts?.stop()
                tts?.speakSpanish(word.spanish, "wod_pron")
            },
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.18f),
            modifier = Modifier.heightIn(min = 56.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.VolumeUp,
                    null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    stringResource(R.string.wod_listen),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        OptionButtons(
            options = options,
            correct = target,
            picked  = picked,
            onPick  = { p ->
                if (p == target) sound.correct() else sound.wrong()
                if (picked == null || picked != target) picked = p
                if (p == target && !solved) { solved = true; onSolved() }
            }
        )
    }
}

// ── Quiz Mode 4 — Letter assembly ──────────────────────────────
@Composable
private fun LetterAssemblyQuiz(
    word: WordOfDay,
    onSolved: () -> Unit
) {
    val target = remember(word.wordId) {
        word.spanish.substringAfterLast(' ').lowercase()
    }
    // Track which positions in the scrambled bank have been used so each
    // letter can only be tapped once per round (was: a→b→r→i→g→o; tapping
    // the same "a" twice produced "aa..." and broke the check).
    val scrambled = remember(word.wordId) { target.toList().shuffled() }
    val used = remember(word.wordId) { mutableStateListOf<Int>() }
    var typed by remember(word.wordId) { mutableStateOf("") }
    var checked by remember(word.wordId) { mutableStateOf(false) }
    val correct = checked && typed.equals(target, ignoreCase = true)
    val sound = rememberAnswerSound()
    LaunchedEffect(checked) {
        if (checked) {
            if (correct) { sound.correct(); onSolved() } else sound.wrong()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            stringResource(R.string.wod_build_template, word.russian),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))

        // Buffer panel — fixed height so it can't blow up to fill the column.
        // Was using fillMaxSize() inside which expanded the Surface to absorb
        // the whole pager page — letter buttons disappeared off-screen.
        val bufferBg = when {
            !checked -> MaterialTheme.colorScheme.surfaceContainerHighest
            correct  -> Color(0xFF1B5E20)
            else     -> Color(0xFF8B0000)
        }
        val bufferTextColor = if (!checked && typed.isEmpty())
            MaterialTheme.colorScheme.onSurfaceVariant else Color.White
        Surface(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp),
            color = bufferBg,
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    typed.ifEmpty { stringResource(R.string.wod_tap_letters) },
                    fontSize = if (typed.isEmpty()) 13.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = bufferTextColor,
                    letterSpacing = if (typed.isEmpty()) 0.sp else 3.sp
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        if (!checked) {
            // Letter bank — flow style, max 8 per row. Tapping a letter
            // adds it to the buffer and marks that index as used so it
            // greys out and can't be pressed again.
            val rows = scrambled.withIndex().toList().chunked(8)
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                ) {
                    rowItems.forEach { (idx, c) ->
                        val isUsed = used.contains(idx)
                        Surface(
                            onClick = {
                                if (!isUsed && typed.length < target.length) {
                                    used.add(idx)
                                    typed += c
                                }
                            },
                            enabled = !isUsed,
                            shape = RoundedCornerShape(8.dp),
                            color = if (isUsed) MaterialTheme.colorScheme.surfaceContainerHigh
                                    else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(width = 36.dp, height = 42.dp),
                            shadowElevation = if (isUsed) 0.dp else 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    c.toString(),
                                    color = if (isUsed) MaterialTheme.colorScheme.outline else Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                if (rows.size > 1) Spacer(Modifier.height(6.dp))
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = {
                    typed = ""
                    used.clear()
                }) { Text("Очистить", fontSize = 13.sp) }
                Button(
                    onClick = { checked = true },
                    enabled = typed.length == target.length,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Проверить", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Spacer(Modifier.height(8.dp))
            Text(
                if (correct) "✓ Верно!" else "Правильно: $target",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (correct) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = {
                typed = ""
                checked = false
                used.clear()
            }) { Text("Ещё раз") }
        }
    }
}

@Composable
private fun OptionButtons(
    options: List<String>,
    correct: String,
    picked: String?,
    onPick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        options.forEach { opt ->
            val isPicked = picked == opt
            val isCorrect = opt == correct
            // Use surfaceContainerHighest (lighter than the card's
            // surfaceContainer) so options visibly pop, plus a 1.5dp border
            // to guarantee separation from the card. Earlier "surface" blended
            // into the brown card and the 4th option looked invisible.
            val bg = when {
                picked == null -> MaterialTheme.colorScheme.surfaceContainerHighest
                isPicked && isCorrect -> Color(0xFFC8E6C9)
                isPicked && !isCorrect -> Color(0xFFFFCDD2)
                isCorrect && picked != correct -> Color(0xFFC8E6C9)
                else -> MaterialTheme.colorScheme.surfaceContainerHighest
            }
            val borderColor = when {
                picked == null -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                isCorrect      -> Color(0xFF4CAF50)
                isPicked       -> Color(0xFFE53935)
                else           -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            }
            // Когда фон зелёный/красный (светлая пастель) — белый текст
            // дефолтной dark-темы становится нечитаемым. Принудительно
            // тёмный цвет в этом случае. Для нейтрального состояния и
            // в светлой теме оставляем системный onSurface.
            val isHighlighted = bg == Color(0xFFC8E6C9) || bg == Color(0xFFFFCDD2)
            val textColor = if (isHighlighted) Color(0xFF1B1B1B)
                            else MaterialTheme.colorScheme.onSurface
            Surface(
                onClick = { if (picked != correct) onPick(opt) },
                shape = RoundedCornerShape(10.dp),
                color = bg,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        opt,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                        modifier = Modifier.weight(1f),
                        maxLines = 2
                    )
                    when {
                        picked != null && isCorrect ->
                            Text("✓", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        isPicked && !isCorrect ->
                            Text("✗", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  PHASE 3 — Bento + Heatmap + Quick Actions + Course pills
// ═══════════════════════════════════════════════════════════════

@Composable
private fun BentoRow(
    book: com.spanishapp.data.db.entity.LibroProgressEntity?,
    rating: Int,
    league: Int,
    hasPracticed: Boolean,
    recent: List<WordEntity>,
    goals: DailyGoals,
    onBookClick: () -> Unit,
    onLeagueClick: () -> Unit,
    onDictClick: () -> Unit,
    onWordChip: (WordEntity) -> Unit,
    onGoalClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // ── BOOK tile (green) ─────────────────────────────────
            BentoTile(modifier = Modifier.weight(1f), accent = BookAccent, theme = BentoTheme.BOOK, onClick = onBookClick) {
                BentoHeader(emoji = "📚", label = stringResource(R.string.bento_label_book), accent = BookAccent)
                Spacer(Modifier.height(10.dp))
                Text(
                    book?.let { stringResource(R.string.bento_libro_template, it.libroId) }
                        ?: stringResource(R.string.bento_open_book),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = (-0.3).sp
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (book?.bestScore ?: 0) / 100f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = BookAccent,
                    trackColor = BookAccent.copy(alpha = 0.18f)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.home_book_progress_template, book?.bestScore ?: 0),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── RATING tile (amber-gold, hero number) ──────────────
            BentoTile(modifier = Modifier.weight(1f), accent = RatingAccent, theme = BentoTheme.RATING, onClick = onLeagueClick) {
                BentoHeader(emoji = "🏅", label = stringResource(R.string.bento_label_rating), accent = RatingAccent)
                Spacer(Modifier.height(10.dp))
                // Pre-first-review: show «—» instead of the seed-1000 rating.
                // The default 1000 confused new users — "откуда у меня рейтинг
                // если я ничего не делал?" — same applies to the league chip.
                Text(
                    if (hasPracticed) "$rating" else "—",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-1.5).sp,
                    lineHeight = 42.sp
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(RatingAccent))
                    Text(
                        if (hasPracticed) LEAGUE_NAMES.getOrElse(league - 1) { "Aldea" } else "—",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // ── DICTIONARY tile (cyan-blue) ────────────────────────
            BentoTile(modifier = Modifier.weight(1f), accent = WeakAccent, theme = BentoTheme.DICTIONARY, onClick = onDictClick) {
                BentoHeader(emoji = "🔍", label = stringResource(R.string.bento_label_dictionary), accent = WeakAccent)
                Spacer(Modifier.height(10.dp))
                if (recent.isEmpty()) {
                    Text(
                        stringResource(R.string.bento_dictionary_find),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp,
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.bento_dictionary_open),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        recent.take(3).forEach { w ->
                            Surface(
                                onClick = { onWordChip(w) },
                                shape = RoundedCornerShape(8.dp),
                                color = WeakAccent.copy(alpha = 0.14f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WeakAccent.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    w.spanish,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    val wordOne = stringResource(R.string.word_count_one)
                    val wordFew = stringResource(R.string.word_count_few)
                    val wordMany = stringResource(R.string.word_count_many)
                    Text(
                        stringResource(
                            R.string.home_dictionary_history_template,
                            recent.size,
                            pluralRu(recent.size, wordOne, wordFew, wordMany)
                        ),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── GOAL tile (coral red — distinct from gold rating) ──
            BentoTile(modifier = Modifier.weight(1f), accent = GoalAccent, theme = BentoTheme.GOAL, onClick = onGoalClick) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BentoHeader(emoji = "🎯", label = stringResource(R.string.bento_label_goal), accent = GoalAccent, modifier = Modifier.weight(1f))
                    Text(
                        "${goals.completedCount}/${goals.total}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (goals.allDone) Color(0xFF2E7D32) else GoalAccent
                    )
                }
                Spacer(Modifier.height(8.dp))
                GoalLine(stringResource(R.string.bento_goal_lesson), goals.lessonCompleted)
                GoalLine(stringResource(R.string.bento_goal_flashcard), goals.flashcardSetCompleted)
                GoalLine(stringResource(R.string.bento_goal_book_page), goals.bookPageRead)
                GoalLine(stringResource(R.string.bento_goal_wod), goals.wordOfDaySolved)
                if (goals.allDone) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF2E7D32).copy(alpha = 0.18f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "🎉 +25 XP бонус",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun pluralRu(n: Int, one: String, few: String, many: String): String {
    val mod10 = n % 10; val mod100 = n % 100
    return when {
        mod100 in 11..14 -> many
        mod10 == 1       -> one
        mod10 in 2..4    -> few
        else             -> many
    }
}

/**
 * Premium bento tile: 180dp tall, soft gradient bg from accent to surface,
 * 6dp shadow, 20dp corners. Accent border on the LEFT edge gives identity.
 */
@Composable
private fun BentoTile(
    modifier: Modifier = Modifier,
    accent: Color,
    theme: BentoTheme? = null,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val baseColor = MaterialTheme.colorScheme.surfaceContainerHigh
    PressableCard(
        onClick = onClick,
        modifier = modifier.height(180.dp),
        shape = RoundedCornerShape(22.dp),
        backgroundColor = baseColor,
        shadowElevation = 6.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Radial accent glow in the top-right corner — depth.
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
            // Per-theme thematic watermark in the bottom-right area —
            // book stack, trophy, magnifier, bullseye etc.
            if (theme != null) {
                BentoWatermark(theme = theme, accent = accent)
            }
            // Left accent stripe — 6dp (was 3dp before, user wanted thicker).
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
}

@Composable
private fun BentoHeader(emoji: String, label: String, accent: Color, modifier: Modifier = Modifier) {
    // `emoji` parameter retained for source-compat; intentionally not rendered
    // (per design feedback: drop leading emojis from section labels).
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp,
            color = accent
        )
    }
}

@Composable
private fun GoalLine(text: String, done: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(vertical = 1.dp)
    ) {
        Text(if (done) "✓" else "○", fontSize = 13.sp,
             fontWeight = FontWeight.Bold,
             color = if (done) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline)
        Text(
            text,
            fontSize = 12.sp,
            fontWeight = if (done) FontWeight.SemiBold else FontWeight.Normal,
            color = if (done) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun WeekHeatmap(minutes: List<Int>) {
    // Locale-aware short weekday labels (Mon=Пн, Tue=Вт, ... resolves
    // automatically based on the active resources locale).
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0]
    val labels = remember(locale) {
        listOf(
            java.time.DayOfWeek.MONDAY,
            java.time.DayOfWeek.TUESDAY,
            java.time.DayOfWeek.WEDNESDAY,
            java.time.DayOfWeek.THURSDAY,
            java.time.DayOfWeek.FRIDAY,
            java.time.DayOfWeek.SATURDAY,
            java.time.DayOfWeek.SUNDAY,
        ).map {
            it.getDisplayName(java.time.format.TextStyle.SHORT, locale)
                .replaceFirstChar { c -> c.uppercase(locale) }
        }
    }
    val totalMin = minutes.sum()
    val activeDays = minutes.count { it > 0 }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                stringResource(R.string.home_weekly_heatmap_title),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                minutes.forEachIndexed { i, m ->
                    val color = when {
                        m == 0      -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        m < 5       -> Orange.copy(alpha = 0.3f)
                        m < 15      -> Orange.copy(alpha = 0.6f)
                        else        -> Orange
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            labels[i],
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.home_weekly_summary_template, activeDays, totalMin),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActionsRow(
    onRandom: () -> Unit,
    onPronounce: () -> Unit,
    onWeak: () -> Unit,
    onGame: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickAction("🎯", stringResource(R.string.quick_action_random), onRandom)
        QuickAction("🎤", stringResource(R.string.quick_action_pronounce), onPronounce)
        QuickAction("🔄", stringResource(R.string.quick_action_weak5), onWeak)
        QuickAction("🎲", stringResource(R.string.quick_action_game), onGame)
    }
}

@Composable
private fun QuickAction(emoji: String, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 4.dp,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = 24.sp)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun CoursePills(activeLevel: String, onClick: (String) -> Unit) {
    val levels = listOf("A1", "A2", "B1", "B2")
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        levels.forEach { lvl ->
            val isActive = lvl == activeLevel
            val color = when (lvl) {
                "A1" -> A1Color; "A2" -> A2Color; "B1" -> B1Color; else -> B2Color
            }
            // Fully uniform outlined style across ALL pills — same border
            // width and alpha regardless of selection. Active state is
            // signalled only by a stronger shadow + brighter text. Owner's
            // request: "make A1 border the same as the others" (yellow felt
            // too aggressive when its border was thicker).
            Surface(
                onClick = { onClick(lvl) },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shadowElevation = if (isActive) 4.dp else 1.dp,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = color.copy(alpha = if (isActive) 0.7f else 0.4f)
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        lvl,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = color
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  Word peek bottom sheet (random / dictionary chip)
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WordPeekSheet(
    word: WordEntity,
    tts: android.speech.tts.TextToSpeech?,
    onDismiss: () -> Unit,
    onOpen: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    word.spanish,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                SpeakerButton(text = word.spanish, tts = tts, tint = Orange)
            }
            Text(
                word.russian,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (word.example.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "\"${word.example}\"",
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.home_open_in_dictionary))
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  TopicCard / SubLessonRow / CefrBadge — kept for CourseDetail reuse
// ═══════════════════════════════════════════════════════════════

@Composable
internal fun TopicCard(
    unit: RoadmapUnit,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onLessonClick: (Int) -> Unit,
    onPremiumClick: () -> Unit = {}
) {
    val accentColor   = if (unit.isLocked) LockGray else unit.color
    val completedCount = unit.lessons.count { it.isCompleted }
    val totalCount     = unit.lessons.size

    // Slightly tighter outer margin (8dp instead of 14dp) so the card uses
    // more screen real-estate without overflowing edge. Bigger header height
    // and font follow below.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .shadow(
                elevation = if (unit.isLocked) 2.dp else 6.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = accentColor.copy(alpha = 0.35f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
    ) {
        Column {
            // Position of this block within its course (1..4).
            // Handles all id schemes:
            //   • A1 → "1".."4"
            //   • A2 → "a2_1".."a2_4"
            //   • B1 → "9".."12"
            //   • B2 → "13".."16"
            // Strategy: take the trailing number (after last underscore if any),
            // then collapse to ((n-1) % 4) + 1 so 9→1, 10→2, 13→1, 16→4 etc.
            val rawTail = unit.id.substringAfterLast('_')
            val tailNum = rawTail.toIntOrNull() ?: 1
            val blockPos = ((tailNum - 1).coerceAtLeast(0) % 4) + 1
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
                    .background(
                        if (unit.isLocked)
                            Brush.verticalGradient(listOf(Color(0xFFDDDDDD), Color(0xFFCCCCCC)))
                        else
                            Brush.verticalGradient(
                                listOf(accentColor, accentColor.copy(alpha = 0.75f))
                            )
                    )
            ) {
                // Subtle dark overlay tames the brightness for a more premium tone.
                if (!unit.isLocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.10f))
                    )
                }
                // Block-position-based thematic watermark (rocket / house /
                // lightning / mountain) painted over the gradient. White
                // accent so it reads against the coloured header.
                if (!unit.isLocked) {
                    val blockTheme = when (blockPos) {
                        1 -> WatermarkTheme.BLOCK_ROCKET
                        2 -> WatermarkTheme.BLOCK_HOME
                        3 -> WatermarkTheme.BLOCK_LIGHTNING
                        else -> WatermarkTheme.BLOCK_MOUNTAIN
                    }
                    ThematicWatermark(theme = blockTheme, accent = Color.White)
                }
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            val label = blockPos.toString().padStart(2, '0')
                            Text(
                                label,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            val titleStartsWithBlock = unit.title.trimStart().startsWith("Блок", ignoreCase = true)
                            if (!titleStartsWithBlock) {
                                Text(
                                    text = stringResource(R.string.home_block_n, blockPos.toString()),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.80f)
                                )
                            }
                            Text(
                                text = unit.title,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                letterSpacing = (-0.3).sp
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        CefrBadge(unit.cefrLevel)
                        if (unit.isLocked) {
                            Icon(Icons.Default.Lock, null, tint = Color.White.copy(.8f), modifier = Modifier.size(18.dp))
                        } else {
                            Text(
                                "$completedCount/$totalCount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Text(
                    text = unit.description,
                    fontSize = 14.sp,
                    color = if (unit.isLocked) TextGray.copy(.6f) else TextGray,
                    maxLines = 2,
                    lineHeight = 19.sp
                )

                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                    ) {
                        if (!unit.isLocked && unit.progress > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(unit.progress)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(listOf(accentColor, accentColor.copy(.7f)))
                                    )
                            )
                        }
                    }

                    Spacer(Modifier.width(10.dp))

                    if (unit.isLocked) {
                        Text(stringResource(R.string.course_locked), fontSize = 11.sp, color = LockGray)
                    } else {
                        Text(
                            "${(unit.progress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint     = if (unit.isLocked) LockGray else accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter   = expandVertically(animationSpec = tween(220)) + fadeIn(tween(220)),
                    exit    = shrinkVertically(animationSpec = tween(180)) + fadeOut(tween(180))
                ) {
                    Column(
                        modifier = Modifier.padding(top = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(color = accentColor.copy(.12f))
                        Spacer(Modifier.height(2.dp))

                        unit.lessons.forEachIndexed { idx, lesson ->
                            SubLessonRow(
                                number       = idx + 1,
                                lesson       = lesson,
                                isLocked     = unit.isLocked,
                                unitColor    = accentColor,
                                onClick      = { onLessonClick(idx) },
                                onPremiumClick = onPremiumClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun CefrBadge(level: String) {
    val (bg, text) = when (level) {
        "A1" -> Color(0xFF2E7D32) to Color.White
        "A2" -> Color(0xFF0277BD) to Color.White
        "B1" -> Color(0xFFE65100) to Color.White
        "B2" -> Color(0xFF6A1B9A) to Color.White
        else -> Color(0xFF37474F) to Color.White
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg.copy(alpha = 0.85f))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(level, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = text)
    }
}

@Composable
private fun SubLessonRow(
    number: Int,
    lesson: RoadmapLesson,
    isLocked: Boolean,
    unitColor: Color,
    onClick: () -> Unit,
    onPremiumClick: () -> Unit = {}
) {
    val typeBg = when (lesson.type) {
        "vocab"   -> Color(0xFFE8F5E9)
        "grammar" -> Color(0xFFE3F2FD)
        "phrase"  -> Color(0xFFF3E5F5)
        "content" -> Color(0xFFE8EAF6)
        else      -> Color(0xFFFFF3E0)
    }
    val typeTextColor = when (lesson.type) {
        "vocab"   -> Color(0xFF2E7D32)
        "grammar" -> Color(0xFF0277BD)
        "phrase"  -> Color(0xFF6A1B9A)
        "content" -> Color(0xFF283593)
        else      -> Color(0xFFE65100)
    }

    val effectiveLocked = isLocked || lesson.isPremium
    Surface(
        onClick = when {
            isLocked         -> {{}}
            lesson.isPremium -> onPremiumClick
            else             -> onClick
        },
        modifier  = Modifier.fillMaxWidth(),
        color     = if (effectiveLocked)
            MaterialTheme.colorScheme.surfaceVariant
        else
            MaterialTheme.colorScheme.surface,
        shape     = RoundedCornerShape(14.dp),
        shadowElevation = if (effectiveLocked) 0.dp else 1.dp
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            lesson.isCompleted -> unitColor
                            effectiveLocked    -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            else               -> unitColor.copy(alpha = 0.12f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (lesson.isCompleted) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text(
                        text       = "$number",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (effectiveLocked) LockGray else unitColor
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = lesson.title,
                    fontSize   = 14.sp,
                    fontWeight = if (lesson.isCompleted) FontWeight.Normal else FontWeight.Medium,
                    color      = if (effectiveLocked) TextGray.copy(.55f) else MaterialTheme.colorScheme.onSurface,
                    maxLines   = 2,
                    lineHeight = 18.sp
                )
            }

            Spacer(Modifier.width(10.dp))

            if (!effectiveLocked) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(typeBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "📖 Теория",
                            fontSize  = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color     = typeTextColor
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFF3E0))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "✏️ Практика",
                            fontSize  = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color     = Color(0xFFE65100)
                        )
                    }
                }
            }

            Spacer(Modifier.width(6.dp))

            when {
                lesson.isPremium   -> Icon(Icons.Default.Lock, null, tint = Color(0xFFFF9500), modifier = Modifier.size(15.dp))
                isLocked           -> Icon(Icons.Default.Lock, null, tint = LockGray, modifier = Modifier.size(15.dp))
                lesson.isCompleted -> {}
                else               -> Icon(Icons.Default.ChevronRight, null, tint = unitColor, modifier = Modifier.size(18.dp))
            }
        }
    }
}
