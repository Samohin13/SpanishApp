package com.spanishapp.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.spanishapp.ui.components.*
import com.spanishapp.ui.theme.AppColors

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state    by viewModel.uiState.collectAsStateWithLifecycle()
    val wordOfDay by viewModel.wordOfTheDay.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.onSessionStarted() }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AppColors.Coral, strokeWidth = 3.dp)
        }
        return
    }

    LazyColumn(
        modifier       = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 100.dp)   // запас под floating bottom bar
    ) {

        // ── HERO HEADER ──────────────────────────────────────
        item {
            HeroHeader(
                displayName     = state.displayName,
                streak          = state.currentStreak,
                spanishLevel    = state.spanishLevel,
                onSettingsClick = { navController.navigate("settings") }
            )
        }

        // ── XP + ЦЕЛЬ ─────────────────────────────────────────
        item {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                XpProgressBar(
                    level    = state.appLevel,
                    progress = state.levelProgress,
                    totalXp  = state.totalXp,
                    modifier = Modifier.weight(1f)
                )
                DailyGoalRing(
                    todayMinutes = state.todayMinutes,
                    goalMinutes  = state.dailyGoalMinutes
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        // ── СТАТИСТИКА ────────────────────────────────────────
        item {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    icon        = "📚",
                    value       = "${state.wordsLearned}",
                    label       = "слов",
                    accentColor = AppColors.Jade,
                    modifier    = Modifier.weight(1f)
                )
                StatCard(
                    icon        = "🔥",
                    value       = "${state.longestStreak}д",
                    label       = "рекорд",
                    accentColor = AppColors.Amber,
                    modifier    = Modifier.weight(1f)
                )
                StatCard(
                    icon        = "⭐",
                    value       = "${state.totalXp}",
                    label       = "очков XP",
                    accentColor = AppColors.Coral,
                    modifier    = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        // ── СЛОВО ДНЯ ─────────────────────────────────────────
        wordOfDay?.let { word ->
            item {
                WordOfDayCard(
                    spanish      = word.spanish,
                    russian      = word.russian,
                    example      = word.example,
                    wasPracticed = word.wasPracticed,
                    onSpeak      = { },
                    onPractice   = { navController.navigate("flashcards") },
                    modifier     = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(24.dp))
            }
        }

        // ── LEVEL UP БАННЕР ───────────────────────────────────
        if (state.shouldLevelUp) {
            item {
                LevelUpBanner(
                    currentLevel = state.spanishLevel,
                    onClick      = { navController.navigate("settings") },
                    modifier     = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(24.dp))
            }
        }

        // ── УЧИТЬСЯ ───────────────────────────────────────────
        item {
            SectionHeader(
                title    = "Учиться",
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(12.dp))
        }

        item {
            LearningModes(
                dueCount      = state.dueWordsCount,
                sessionPlan   = state.sessionPlan,
                navController = navController,
                modifier      = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(24.dp))
        }

        // ── ЕЩЁ ──────────────────────────────────────────────
        item {
            SectionHeader(
                title    = "Ещё",
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(12.dp))
        }

        item {
            QuickActions(
                navController = navController,
                modifier      = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  HERO HEADER  —  большой градиентный блок
// ═══════════════════════════════════════════════════════════════
@Composable
private fun HeroHeader(
    displayName: String,
    streak: Int,
    spanishLevel: String,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(
                Brush.linearGradient(
                    0f   to AppColors.Coral,
                    0.6f to Color(0xFFE83060),
                    1f   to Color(0xFF9B2190)
                )
            )
    ) {
        // Декоративные круги
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )
        Box(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-20).dp, y = 30.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
        )

        Column(
            modifier = Modifier.padding(
                start = 22.dp, end = 16.dp,
                top   = 56.dp, bottom = 26.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Строка: приветствие + настройки
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text  = greetingByTime(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text       = if (displayName.isNotEmpty()) displayName else "Estudiante",
                            style      = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color      = Color.White
                        )
                        LevelBadge(level = spanishLevel)
                    }
                }

                IconButton(
                    onClick  = onSettingsClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Настройки",
                        tint     = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Строка: стрик
            StreakBadge(streak = streak)
        }
    }

    Spacer(Modifier.height(24.dp))
}

// ═══════════════════════════════════════════════════════════════
//  LEVEL UP BANNER
// ═══════════════════════════════════════════════════════════════
@Composable
private fun LevelUpBanner(
    currentLevel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nextLevel = when (currentLevel) {
        "A1" -> "A2"; "A2" -> "B1"; "B1" -> "B2"; else -> "C1"
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(listOf(AppColors.AmberDark, AppColors.Amber))
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("🏆", fontSize = 30.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Готов перейти на $nextLevel?",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Color.White
                )
                Text(
                    "Ты освоил $currentLevel. Попробуй сложнее!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.82f)
                )
            }
            Icon(
                Icons.Default.ArrowForward, null,
                tint     = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  LEARNING MODES  —  Hero + 2×2 сетка
// ═══════════════════════════════════════════════════════════════
@Composable
private fun LearningModes(
    dueCount: Int,
    sessionPlan: com.spanishapp.domain.algorithm.AdaptiveLearning.SessionPlan,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // Hero — Карточки
        HeroFeatureCard(
            title         = "Карточки",
            subtitle      = if (dueCount > 0)
                                "$dueCount к повторению · ${sessionPlan.newWords} новых"
                            else "Все повторения на сегодня выполнены ✓",
            icon          = "🃏",
            onClick       = { navController.navigate("flashcards") },
            badgeText     = if (dueCount > 0) "$dueCount" else null,
            gradientStart = AppColors.Coral,
            gradientEnd   = AppColors.CoralDark
        )

        // 2×2 сетка
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FeatureCard(
                title       = "Спряжения",
                subtitle    = "160 глаголов · 6 времён",
                icon        = "📝",
                onClick     = { navController.navigate("conjugation") },
                accentColor = AppColors.Jade,
                modifier    = Modifier.weight(1f)
            )
            FeatureCard(
                title       = "Диалоги",
                subtitle    = "Реальные ситуации",
                icon        = "💬",
                onClick     = { navController.navigate("dialogues") },
                accentColor = AppColors.Amber,
                modifier    = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FeatureCard(
                title       = "Грамматика",
                subtitle    = "A1 · A2 · B1",
                icon        = "📖",
                onClick     = { navController.navigate("grammar") },
                accentColor = AppColors.Sky,
                modifier    = Modifier.weight(1f)
            )
            FeatureCard(
                title       = "Произноше-ние",
                subtitle    = "Говори и слушай",
                icon        = "🎤",
                onClick     = { navController.navigate("pronunciation") },
                accentColor = AppColors.Violet,
                modifier    = Modifier.weight(1f)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  QUICK ACTIONS  —  ряд маленьких кнопок
// ═══════════════════════════════════════════════════════════════
@Composable
private fun QuickActions(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple("weak_words", "Слабые\nслова",  "⚠️"),
        Triple("quiz",       "Тест",           "🎯"),
        Triple("games",      "Игры",           "🕹️"),
        Triple("ai_chat",    "ИИ-\nрепетитор", "🤖")
    )
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEach { (route, label, icon) ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .shadow(3.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { navController.navigate(route) }
                    .padding(vertical = 16.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(icon, fontSize = 24.sp)
                    Text(
                        text      = label,
                        style     = MaterialTheme.typography.labelSmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color     = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        maxLines  = 2
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  HELPERS
// ═══════════════════════════════════════════════════════════════
private fun greetingByTime(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 6  -> "Доброй ночи,"
        hour < 12 -> "Доброе утро,"
        hour < 18 -> "Добрый день,"
        else      -> "Добрый вечер,"
    }
}
