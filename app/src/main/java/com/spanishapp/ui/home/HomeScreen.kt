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
                displayName   = state.displayName,
                streak        = state.currentStreak,
                spanishLevel  = state.spanishLevel,
                onSettingsClick = { navController.navigate("settings") }
            )
        }

        // ── XP BAR ────────────────────────────────────────────
        item {
            XpProgressBar(
                level    = state.appLevel,
                progress = state.levelProgress,
                totalXp  = state.totalXp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(20.dp))
        }

        // ── DAILY GOAL ────────────────────────────────────────
        item {
            DailyGoalSection(
                todayMinutes = state.todayMinutes,
                goalMinutes  = state.dailyGoalMinutes,
                dueCount     = state.dueWordsCount,
                onStartSession = { navController.navigate("flashcards") }
            )
            Spacer(Modifier.height(20.dp))
        }

        // ── WORD OF DAY ───────────────────────────────────────
        wordOfDay?.let { word ->
            item {
                SectionHeader(
                    title = "Слово дня",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(8.dp))
                WordOfDayCard(
                    spanish     = word.spanish,
                    russian     = word.russian,
                    example     = word.example,
                    wasPracticed= word.wasPracticed,
                    onSpeak     = { /* TTS handled in ViewModel */ },
                    onPractice  = { navController.navigate("flashcards") },
                    modifier    = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        // ── STATS ROW ─────────────────────────────────────────
        item {
            StatsRow(
                wordsLearned     = state.wordsLearned,
                longestStreak    = state.longestStreak,
                lessonsCompleted = state.nextLessons.size,
                modifier         = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(20.dp))
        }

        // ── LEVEL UP BANNER ───────────────────────────────────
        if (state.shouldLevelUp) {
            item {
                LevelUpBanner(
                    currentLevel = state.spanishLevel,
                    onClick = { navController.navigate("settings") },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        // ── MAIN FEATURES ─────────────────────────────────────
        item {
            SectionHeader(
                title = "Заниматься",
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(8.dp))
        }

        item {
            FeaturesGrid(
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
                title       = "Дополнительно",
                actionLabel = "Все",
                onAction    = { },
                modifier    = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(8.dp))
        }

        item {
            QuickActions(
                navController = navController,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// HEADER  —  greeting + streak + level badge
// ─────────────────────────────────────────────────────────────
@Composable
private fun HomeHeader(
    displayName: String,
    streak: Int,
    spanishLevel: String,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        AppColors.Terracotta.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(start = 20.dp, end = 20.dp, top = 52.dp, bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text  = greetingByTime(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text  = if (displayName.isNotEmpty()) displayName else "Estudiante",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    LevelBadge(level = spanishLevel)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StreakBadge(streak = streak)
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Настройки",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// DAILY GOAL SECTION
// ─────────────────────────────────────────────────────────────
@Composable
private fun DailyGoalSection(
    todayMinutes: Int,
    goalMinutes: Int,
    dueCount: Int,
    onStartSession: () -> Unit
) {
    Surface(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape     = RoundedCornerShape(20.dp),
        color     = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(20.dp)
            )
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DailyGoalRing(
                    todayMinutes = todayMinutes,
                    goalMinutes  = goalMinutes
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = "Цель на сегодня",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text  = if (todayMinutes >= goalMinutes) "Выполнено! 🎉"
                        else "$dueCount слов к повторению",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text  = "$todayMinutes из $goalMinutes минут",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (dueCount > 0) {
                    FilledTonalButton(
                        onClick  = onStartSession,
                        shape    = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text  = "Начать",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STATS ROW  —  3 metric tiles
// ─────────────────────────────────────────────────────────────
@Composable
private fun StatsRow(
    wordsLearned: Int,
    longestStreak: Int,
    lessonsCompleted: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            label = "слов изучено",
            value = wordsLearned.toString(),
            icon  = "📚",
            accentColor = AppColors.Teal,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "рекорд стрик",
            value = "$longestStreak д.",
            icon  = "🔥",
            accentColor = AppColors.Gold,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "уроков",
            value = lessonsCompleted.toString(),
            icon  = "✅",
            accentColor = AppColors.Terracotta,
            modifier = Modifier.weight(1f)
        )
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
// FEATURES GRID  —  main learning modes
// ─────────────────────────────────────────────────────────────
@Composable
private fun FeaturesGrid(
    dueCount: Int,
    sessionPlan: com.spanishapp.domain.algorithm.AdaptiveLearning.SessionPlan,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Flashcards — primary action
        FeatureCard(
            title     = "Карточки",
            subtitle  = if (dueCount > 0) "$dueCount слов к повторению · ${sessionPlan.newWords} новых"
            else "Все повторено на сегодня ✓",
            icon      = "🃏",
            onClick   = { navController.navigate("flashcards") },
            badgeText = if (dueCount > 0) "$dueCount" else null,
            accentColor = AppColors.Terracotta
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FeatureCard(
                title     = "Спряжения",
                subtitle  = "20 глаголов · 6 времён",
                icon      = "📝",
                onClick   = { navController.navigate("conjugation") },
                accentColor = AppColors.Teal,
                modifier  = Modifier.weight(1f)
            )
            FeatureCard(
                title     = "Диалоги",
                subtitle  = "Реальные ситуации",
                icon      = "💬",
                onClick   = { navController.navigate("dialogues") },
                accentColor = AppColors.Gold,
                modifier  = Modifier.weight(1f)
            )
        }

        FeatureCard(
            title     = "ИИ-репетитор",
            subtitle  = "Разговорная практика с коррекцией ошибок",
            icon      = "🤖",
            onClick   = { navController.navigate("ai_chat") },
            accentColor = AppColors.Info,
            badgeText = "Claude"
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FeatureCard(
                title     = "Грамматика",
                subtitle  = "Уроки по уровням",
                icon      = "📖",
                onClick   = { navController.navigate("grammar") },
                accentColor = AppColors.Teal,
                modifier  = Modifier.weight(1f)
            )
            FeatureCard(
                title     = "Произноше-ние",
                subtitle  = "Говори и слушай",
                icon      = "🎤",
                onClick   = { navController.navigate("pronunciation") },
                accentColor = AppColors.Terracotta,
                modifier  = Modifier.weight(1f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// QUICK ACTIONS  —  secondary navigation row
// ─────────────────────────────────────────────────────────────
@Composable
private fun QuickActions(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf(
            Triple("weak_words", "Слабые\nслова",   "⚠️"),
            Triple("dictionary", "Словарь",          "🔍"),
            Triple("quiz",       "Тест",             "🎯"),
            Triple("achievements","Ачивки",          "🏅")
        ).forEach { (route, label, icon) ->
            Surface(
                onClick  = { navController.navigate(route) },
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(14.dp),
                color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(icon, fontSize = 20.sp)
                    Text(
                        text      = label,
                        style     = MaterialTheme.typography.labelSmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant
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