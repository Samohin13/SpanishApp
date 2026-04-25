package com.spanishapp.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val wordOfDay by viewModel.wordOfTheDay.collectAsStateWithLifecycle()

    // Kick off streak update when screen opens
    LaunchedEffect(Unit) { viewModel.onSessionStarted() }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AppColors.Terracotta)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {

        // ── HEADER ────────────────────────────────────────────
        item {
            HomeHeader(
                displayName     = state.displayName,
                streak          = state.currentStreak,
                spanishLevel    = state.spanishLevel,
                onSettingsClick = { navController.navigate("settings") }
            )
        }

        // ── XP + GOAL (one row) ───────────────────────────────
        item {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
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
            Spacer(Modifier.height(20.dp))
        }

        // ── WORD OF DAY ───────────────────────────────────────
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
                Spacer(Modifier.height(20.dp))
            }
        }

        // ── STATS ─────────────────────────────────────────────
        item {
            StatsRow(
                wordsLearned     = state.wordsLearned,
                longestStreak    = state.longestStreak,
                lessonsCompleted = state.nextLessons.size,
                modifier         = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(20.dp))
        }

        // ── LEVEL UP ──────────────────────────────────────────
        if (state.shouldLevelUp) {
            item {
                LevelUpBanner(
                    currentLevel = state.spanishLevel,
                    onClick      = { navController.navigate("settings") },
                    modifier     = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        // ── LEARNING MODES ────────────────────────────────────
        item {
            SectionHeader(
                title    = "Учиться",
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(10.dp))
        }

        item {
            LearningModes(
                dueCount      = state.dueWordsCount,
                sessionPlan   = state.sessionPlan,
                navController = navController,
                modifier      = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(20.dp))
        }

        // ── QUICK ACTIONS ─────────────────────────────────────
        item {
            SectionHeader(
                title    = "Ещё",
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(10.dp))
        }

        item {
            QuickActions(
                navController = navController,
                modifier      = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// HEADER
// ─────────────────────────────────────────────────────────────
@Composable
private fun HomeHeader(
    displayName: String,
    streak: Int,
    spanishLevel: String,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, top = 52.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text  = greetingByTime(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text       = if (displayName.isNotEmpty()) displayName else "Estudiante",
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                LevelBadge(level = spanishLevel)
            }
        }

        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StreakBadge(streak = streak)
            IconButton(onClick = onSettingsClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Настройки",
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// DailyGoalSection удалена — кольцо встроено в XP-строку

// ─────────────────────────────────────────────────────────────
// STATS ROW
// ─────────────────────────────────────────────────────────────
@Composable
private fun StatsRow(
    wordsLearned: Int,
    longestStreak: Int,
    lessonsCompleted: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard("слов", wordsLearned.toString(), "📚", Modifier.weight(1f), AppColors.Teal)
        StatCard("рекорд", "$longestStreak д.", "🔥", Modifier.weight(1f), AppColors.Gold)
        StatCard("уроков", lessonsCompleted.toString(), "✅", Modifier.weight(1f), AppColors.Terracotta)
    }
}

// ─────────────────────────────────────────────────────────────
// LEVEL UP BANNER
// ─────────────────────────────────────────────────────────────
@Composable
private fun LevelUpBanner(
    currentLevel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nextLevel = when (currentLevel) {
        "A1" -> "A2"; "A2" -> "B1"; "B1" -> "B2"; else -> "C1"
    }

    Surface(
        onClick  = onClick,
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        color    = AppColors.Gold.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("🏆", fontSize = 28.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = "Готов перейти на $nextLevel?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.GoldDark
                )
                Text(
                    text  = "Ты освоил уровень $currentLevel. Попробуй более сложные слова.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = AppColors.GoldDark,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// LEARNING MODES  —  Hero card + 2×2 grid
// ─────────────────────────────────────────────────────────────
@Composable
private fun LearningModes(
    dueCount: Int,
    sessionPlan: com.spanishapp.domain.algorithm.AdaptiveLearning.SessionPlan,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {

        // Hero — Flashcards
        HeroFeatureCard(
            title         = "Карточки",
            subtitle      = if (dueCount > 0)
                                "$dueCount слов к повторению · ${sessionPlan.newWords} новых"
                            else "Все повторения выполнены ✓",
            icon          = "🃏",
            onClick       = { navController.navigate("flashcards") },
            badgeText     = if (dueCount > 0) "$dueCount" else null,
            gradientStart = AppColors.Terracotta,
            gradientEnd   = AppColors.TerracottaDark
        )

        // 2×2 grid
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FeatureCard(
                title       = "Спряжения",
                subtitle    = "160 глаголов · 6 времён",
                icon        = "📝",
                onClick     = { navController.navigate("conjugation") },
                accentColor = AppColors.Teal,
                modifier    = Modifier.weight(1f)
            )
            FeatureCard(
                title       = "Диалоги",
                subtitle    = "15 ситуаций",
                icon        = "💬",
                onClick     = { navController.navigate("dialogues") },
                accentColor = AppColors.Gold,
                modifier    = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FeatureCard(
                title       = "Грамматика",
                subtitle    = "A1 · A2 · B1",
                icon        = "📖",
                onClick     = { navController.navigate("grammar") },
                accentColor = AppColors.Info,
                modifier    = Modifier.weight(1f)
            )
            FeatureCard(
                title       = "Произноше-ние",
                subtitle    = "Говори и слушай",
                icon        = "🎤",
                onClick     = { navController.navigate("pronunciation") },
                accentColor = AppColors.Terracotta,
                modifier    = Modifier.weight(1f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// QUICK ACTIONS
// ─────────────────────────────────────────────────────────────
@Composable
private fun QuickActions(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple("weak_words",   "Слабые слова", "⚠️"),
        Triple("dictionary",   "Словарь",      "🔍"),
        Triple("quiz",         "Тест",         "🎯"),
        Triple("achievements", "Достижения",   "🏅")
    )
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEach { (route, label, icon) ->
            Surface(
                onClick  = { navController.navigate(route) },
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(16.dp),
                color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ) {
                Column(
                    modifier            = Modifier.padding(vertical = 14.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(icon, fontSize = 22.sp)
                    Text(
                        text      = label,
                        style     = MaterialTheme.typography.labelSmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        maxLines  = 2
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────
private fun greetingByTime(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 6  -> "Доброй ночи,"
        hour < 12 -> "Доброе утро,"
        hour < 18 -> "Добрый день,"
        else      -> "Добрый вечер,"
    }
}