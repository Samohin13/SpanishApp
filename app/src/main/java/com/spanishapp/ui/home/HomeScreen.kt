package com.spanishapp.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue
import java.time.LocalDate
import java.time.LocalTime
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.spanishapp.ui.components.*

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
//  Palette — Sunset over Barcelona
// ═══════════════════════════════════════════════════════════════

private val Orange      = Color(0xFFFF6B35)  // Primary CTA
private val Purple      = Color(0xFFFF6B35)  // alias
private val Pink        = Color(0xFFD62867)  // Magenta accent
private val GoldColor   = Color(0xFFFF9500)  // Sun / XP
private val OrangeColor = Color(0xFFFF6B00)  // Streak fire
private val TextMain    = Color(0xFF1A1A1A)  // Near-black primary text
private val TextGray    = Color(0xFF8E8E93)  // Secondary text
private val LockGray    = Color(0xFFC7C7CC)
private val BgGray      = Color(0xFFF0F0F5)  // Cool gray home wrapper
private val BgLight     = Color(0xFFF8F8FA)  // SpanishBackground

// CEFR level gradients — vivid + contrasting per level (3.9).
// A1 purple, A2 teal, B1 green, B2 orange.
private val A1Start     = Color(0xFF7C3AED)  // bright violet
private val A1End       = Color(0xFF5B21B6)  // deep violet
private val A2Start     = Color(0xFF06B6D4)  // bright teal
private val A2End       = Color(0xFF0E7490)  // deep teal
private val B1Start     = Color(0xFF22C55E)  // bright green
private val B1End       = Color(0xFF15803D)  // deep green
private val B2Start     = Color(0xFFF97316)  // bright orange
private val B2End       = Color(0xFFC2410C)  // deep orange

// ═══════════════════════════════════════════════════════════════
//  HOME SCREEN
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val wordOfDay by viewModel.wordOfTheDay.collectAsStateWithLifecycle()
    var expandedUnitId by remember { mutableStateOf<String?>(null) }
    val tts = rememberSpanishTts()

    // Локализованные подписи карточек курсов — читаются здесь (в @Composable
    // контексте HomeScreen), потому что внутри LazyColumn item-lambda
    // stringResource() недоступен.
    val courseDataLocal = listOf(
        CourseCardData("A1", stringResource(R.string.course_a1_subtitle), "🚀", A1Start, A1End,
            stringResource(R.string.course_lessons_60_micro), stringResource(R.string.course_blocks_4)),
        CourseCardData("A2", stringResource(R.string.course_a2_subtitle), "🌍", A2Start, A2End,
            stringResource(R.string.course_lessons_60),       stringResource(R.string.course_blocks_4)),
        CourseCardData("B1", stringResource(R.string.course_b1_subtitle), "📚", B1Start, B1End,
            stringResource(R.string.course_lessons_soon),     stringResource(R.string.course_blocks_4)),
        CourseCardData("B2", stringResource(R.string.course_b2_subtitle), "🎓", B2Start, B2End,
            stringResource(R.string.course_lessons_soon),     stringResource(R.string.course_blocks_4))
    )
    val courseLockedLabel = stringResource(R.string.course_locked)
    val courseStartLabel = stringResource(R.string.course_start_learning)
    val course60LessonsLabel = stringResource(R.string.course_lessons_60)
    val homeWordOfDayLabel = stringResource(R.string.home_word_of_day)

    // Time-of-day greeting + daily-rotating motivation (3.1).
    // Recomputed once per recomposition; the day-of-epoch key keeps the
    // animation in AnimatedScreenTitle from replaying on every state tick.
    val today = remember { LocalDate.now() }
    val greeting = remember(today) { greetingFor(LocalTime.now()) }
    val motivation = remember(today) { motivationFor(today) }

    Box(modifier = Modifier.fillMaxSize()) {
    // Faint Spain-cities skyline behind the whole feed (3.5).
    com.spanishapp.ui.components.SpanishCitiesWatermark(
        modifier = Modifier.fillMaxSize()
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 96.dp)  // extra space so FAB doesn't cover content
    ) {
        // ── Header ─────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                val context = LocalContext.current
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { navController.navigate("profile") }
                        ),
                    shape = CircleShape,
                    color = Purple,
                    tonalElevation = 2.dp
                ) {
                    if (state.userPhotoUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(state.userPhotoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Профиль",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatPill("✨", "${state.totalXp} XP", Color(0xFFFFF1E6), GoldColor, GoldColor.copy(.3f))
                    StatPill("🔥", "${state.currentStreak}", Color(0xFFFFF1E6), OrangeColor, OrangeColor.copy(.3f))
                }
            }
        }

        // ── Greeting ───────────────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(start = 20.dp, end = 20.dp, top = 2.dp, bottom = 20.dp)
            ) {
                // Animated entrance — replayKey on greeting so the morning →
                // afternoon transition triggers the staggered re-reveal (3.8).
                com.spanishapp.ui.components.AnimatedScreenTitle(
                    text = greeting,
                    fontSize = 26.sp,
                    bold = true,
                    replayKey = greeting
                )
                Spacer(Modifier.height(2.dp))
                Text(motivation, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item { Spacer(Modifier.height(16.dp)) }

        // ── Streak card ────────────────────────────────────────
        item {
            StreakCard(
                streak       = state.currentStreak,
                studiedToday = state.studiedToday,
                todayMinutes = state.todayMinutes,
                goalMinutes  = state.dailyGoalMinutes,
                freezes      = state.streakFreezes,
                todayXp      = state.todayXp
            )
            Spacer(Modifier.height(12.dp))
        }

        // ── Word of day card ───────────────────────────────────
        wordOfDay?.let { word ->
            item {
                WordOfDayCard(word = word, tts = tts)
                Spacer(Modifier.height(12.dp))
            }
        }

        // ── Course pager ───────────────────────────────────────
        // Horizontal carousel: ~85% width per page, peek of neighbours,
        // current page scales up while siblings fade and shrink (3.2).
        val courseData = courseDataLocal
        item {
            val pagerState = rememberPagerState(pageCount = { courseData.size })
            Column {
                HorizontalPager(
                    state = pagerState,
                    pageSize = PageSize.Fixed(320.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp),
                    pageSpacing = 12.dp,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val course = courseData[page]
                    val pageOffset =
                        (pagerState.currentPage - page + pagerState.currentPageOffsetFraction)
                            .absoluteValue
                            .coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier.graphicsLayer {
                            val s = lerp(0.92f, 1f, 1f - pageOffset)
                            scaleX = s
                            scaleY = s
                            alpha = lerp(0.6f, 1f, 1f - pageOffset)
                        }
                    ) {
                        CourseCard(
                            course = course,
                            unitsCount = state.roadmapUnits.count { it.cefrLevel == course.level },
                            isLocked = false,
                            onClick = { navController.navigate("course_detail/${course.level}") },
                            onPremiumClick = { /* premium убран — все курсы открыты */ }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                // Dots indicator — active dot uses the current course's start color.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    courseData.forEachIndexed { idx, course ->
                        val isActive = pagerState.currentPage == idx
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isActive) 10.dp else 7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) course.colorStart
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                )
                        )
                    }
                }
            }
        }
    }

        // ── AI-Chat FAB ─────────────────────────────────────────
        // Spanish-themed bull icon (game-icons:taurus, CC-BY 3.0).
        // Animations:
        //  1) Entrance — spring overshoot 0 → 1.1 → 1.0 (delay 400ms).
        //  2) Continuous breathing pulse 1.0 ↔ 1.06 every 2s + synced shadow.
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
        // Shadow elevation pulses in sync with scale for a soft glow effect.
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
}

// ── Data class for course card ────────────────────────────────

data class CourseCardData(
    val level: String,
    val title: String,
    val icon: String,
    val colorStart: Color,
    val colorEnd: Color,
    val subtitle: String,
    val blocksLabel: String
)

// ── Course card ────────────────────────────────────────────────

@Composable
private fun CourseCard(
    course: CourseCardData,
    unitsCount: Int,
    isLocked: Boolean,
    onClick: () -> Unit,
    onPremiumClick: () -> Unit = {}
) {
    // Diagonal gradient with a third (lighter) accent stop for depth (3.6).
    val headerBrush = if (isLocked)
        Brush.linearGradient(listOf(Color(0xFFDDDDDD), Color(0xFFCCCCCC)))
    else
        Brush.linearGradient(
            colors = listOf(
                course.colorStart,
                course.colorEnd,
                course.colorStart.copy(alpha = 0.85f)
            ),
            start = Offset.Zero,
            end = Offset(1000f, 1000f)
        )

    val accentColor = if (isLocked) LockGray else course.colorStart

    PressableCard(
        onClick = if (isLocked) onPremiumClick else onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = if (isLocked) 2.dp else 8.dp
    ) {
        Column {
            // ── Header — diagonal gradient + skyline watermark ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(headerBrush)
            ) {
                // Subtle white skyline overlay on each card header (3.5).
                com.spanishapp.ui.components.SpanishCitiesWatermark(
                    color = Color.White.copy(alpha = 0.10f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(course.icon, fontSize = 36.sp)
                            CefrBadge(course.level)
                        }
                        Spacer(Modifier.height(6.dp))
                        // Headline-feel title — 28sp ExtraBold tight tracking (3.4).
                        Text(
                            course.title,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (isLocked) {
                        Icon(Icons.Default.Lock, null, tint = Color.White.copy(.8f), modifier = Modifier.size(24.dp))
                    } else {
                        // Just the unit count with a packet emoji — no "блоков" label (3.3).
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "📦 $unitsCount",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // ── Body ────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                Text(
                    course.subtitle,
                    fontSize = 15.sp,
                    color = if (isLocked) TextGray.copy(.6f) else TextGray,
                    maxLines = 2,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (isLocked) stringResource(R.string.course_locked) else stringResource(R.string.course_start_learning),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
            }
        }
    }
}

// ── Stat pill ──────────────────────────────────────────────────

@Composable
private fun StatPill(emoji: String, value: String, bgColor: Color, textColor: Color, borderColor: Color) {
    Surface(shape = RoundedCornerShape(20.dp), color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 14.sp)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  TOPIC CARD  —  главная карточка блока
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .shadow(
                elevation = if (unit.isLocked) 2.dp else 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = accentColor.copy(alpha = 0.35f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
    ) {
        Column {

            // ── Цветная шапка ──────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(
                        if (unit.isLocked)
                            Brush.horizontalGradient(listOf(Color(0xFFDDDDDD), Color(0xFFCCCCCC)))
                        else
                            Brush.horizontalGradient(
                                listOf(accentColor, accentColor.copy(alpha = 0.72f))
                            )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Большой эмодзи-иконка блока
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(unit.icon, fontSize = 22.sp)
                        }

                        Column {
                            // Если title уже начинается с "Блок" — не повторяем
                            // подпись "Блок N" (3.7: убираем дубликат).
                            val titleStartsWithBlock = unit.title.trimStart().startsWith("Блок", ignoreCase = true)
                            if (!titleStartsWithBlock) {
                                Text(
                                    text = stringResource(R.string.home_block_n, unit.id),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.80f)
                                )
                            }
                            // Название блока
                            Text(
                                text = unit.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Правая часть шапки: CEFR + замок/кол-во
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

            // ── Тело карточки ──────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {

                // Описание блока
                Text(
                    text = unit.description,
                    fontSize = 13.sp,
                    color = if (unit.isLocked) TextGray.copy(.6f) else TextGray,
                    maxLines = 2,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(10.dp))

                // Прогресс-бар + стрелка
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Прогресс-трек
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

                    // Процент или "Заблокировано"
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

                    // Стрелка раскрытия
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint     = if (unit.isLocked) LockGray else accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // ── Развёрнутые уроки ──────────────────────────
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

// ── CEFR badge ────────────────────────────────────────────────

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

// ═══════════════════════════════════════════════════════════════
//  SUB-LESSON ROW  —  строка урока внутри блока
// ═══════════════════════════════════════════════════════════════

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
            // Круг с номером / галочкой
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

            // Название урока
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

            // Плашки: Теория + Практика
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

            // Правый значок
            when {
                lesson.isPremium   -> Icon(Icons.Default.Lock, null, tint = Color(0xFFFF9500), modifier = Modifier.size(15.dp))
                isLocked           -> Icon(Icons.Default.Lock, null, tint = LockGray, modifier = Modifier.size(15.dp))
                lesson.isCompleted -> {}
                else               -> Icon(Icons.Default.ChevronRight, null, tint = unitColor, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  STREAK CARD
// ═══════════════════════════════════════════════════════════════

@Composable
private fun StreakCard(
    streak: Int,
    studiedToday: Boolean,
    todayMinutes: Int,
    goalMinutes: Int,
    freezes: Int,
    todayXp: Int
) {
    val flameScale by rememberInfiniteTransition(label = "flame").animateFloat(
        initialValue = 1f,
        targetValue  = if (streak > 0) 1.12f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameScale"
    )

    val progress = if (goalMinutes > 0) (todayMinutes.toFloat() / goalMinutes).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "ringProgress"
    )
    val goalReached = progress >= 1f

    // Flame gradient background: orange → yellow accent for "fire" feel when active.
    val flameBg = if (streak > 0) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFF1E6),
                Color(0xFFFFE0B2),
                Color(0xFFFFCC80).copy(alpha = 0.55f)
            )
        )
    } else {
        Brush.linearGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
    ) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .background(flameBg)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flame icon circle with gradient — emoji pulses 1.0 ↔ 1.15 every 1.5s.
            val emojiPulse by rememberInfiniteTransition(label = "emojiPulse").animateFloat(
                initialValue = 1f,
                targetValue = if (streak > 0) 1.15f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "emojiPulseScale"
            )
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFFFFD23F), Color(0xFFFF6B00)))
                    )
                    .scale(if (streak > 0) flameScale else 1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (streak > 0) "🔥" else "💤",
                    fontSize = 30.sp,
                    modifier = Modifier.graphicsLayer {
                        scaleX = emojiPulse
                        scaleY = emojiPulse
                    }
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "$streak",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (streak > 0) OrangeColor else TextGray,
                        lineHeight = 50.sp
                    )
                    Text(
                        text = stringResource(R.string.home_streak_days),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Spacer(Modifier.height(2.dp))
                // Freezes row: ❄❄ visualisation
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(2) { idx ->
                        Text(
                            text = if (idx < freezes) "❄" else "·",
                            fontSize = 14.sp,
                            color = if (idx < freezes) Color(0xFF42A5F5) else TextGray.copy(.5f)
                        )
                    }
                    Text(
                        text = stringResource(R.string.home_freezes_label, freezes),
                        fontSize = 11.sp,
                        color = TextGray
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (goalReached) stringResource(R.string.home_goal_reached)
                           else if (studiedToday) stringResource(R.string.home_studied_today, todayMinutes, goalMinutes)
                           else stringResource(R.string.home_not_studied_yet),
                    fontSize = 12.sp,
                    color = if (goalReached) Color(0xFF2E7D32) else if (studiedToday) Color(0xFF2E7D32) else TextGray
                )
            }

            // ── Daily goal ring (Phase 2) ──
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(64.dp),
                    color = GoldColor.copy(alpha = 0.18f),
                    strokeWidth = 6.dp,
                    trackColor = Color.Transparent
                )
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(64.dp),
                    color = OrangeColor,
                    strokeWidth = 6.dp,
                    trackColor = Color.Transparent
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$todayMinutes/$goalMinutes",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (goalReached) Color(0xFF2E7D32) else OrangeColor
                    )
                    Text(stringResource(R.string.home_goal_unit), fontSize = 9.sp, color = TextGray)
                }
            }
        }
    }
        // 🏆 неделя! badge for streaks ≥ 7 days.
        if (streak >= 7) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 6.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFD700),
                shadowElevation = 3.dp
            ) {
                Text(
                    text = "🏆 неделя!",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF5D4037),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  WORD OF DAY CARD
// ═══════════════════════════════════════════════════════════════

@Composable
private fun WordOfDayCard(word: WordOfDay, tts: android.speech.tts.TextToSpeech?) {
    val WordBlue = Purple                 // Orange #FF6B35
    val WordBg   = Color(0xFFFFF1E6)     // Peach tint

    // Slide-in + fade entrance, 350ms.
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val anim = androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "wodIn"
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .graphicsLayer {
                alpha = anim.value
                translationY = (1f - anim.value) * 32f
            },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            // Label — meta line "🇪🇸 Слово дня · A1"
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("🇪🇸", fontSize = 14.sp)
                Text(
                    stringResource(R.string.home_word_of_day) + " · A1",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WordBlue
                )
            }

            Spacer(Modifier.height(10.dp))

            // Spanish word — big 32sp ExtraBold + 56dp circular speak button.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = word.spanish,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WordBlue,
                    modifier = Modifier.weight(1f),
                    lineHeight = 36.sp
                )
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(WordBlue),
                    contentAlignment = Alignment.Center
                ) {
                    SpeakerButton(text = word.spanish, tts = tts, tint = Color.White)
                }
            }

            // Russian translation — 16sp under big word.
            Text(
                text = word.russian,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (word.example.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                // Example sentence — clickable, длинный тап = TTS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(WordBg)
                        .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "\"${word.example}\"",
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        color = WordBlue.copy(alpha = 0.85f),
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f).padding(vertical = 6.dp)
                    )
                    SpeakerButton(text = word.example, tts = tts, tint = WordBlue)
                }
            }

            if (word.wasPracticed) {
                Spacer(Modifier.height(8.dp))
                Text("✓ Уже практиковал", fontSize = 12.sp, color = Color(0xFF2E7D32))
            }
        }
    }
}
