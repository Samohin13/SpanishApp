package com.spanishapp.ui.home

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
            CircularProgressIndicator(color = AppColors.Terracotta, strokeWidth = 2.5.dp)
        }
        return
    }

    LazyColumn(
        modifier       = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {

        // ── ЧИСТЫЙ ХЕДЕР ─────────────────────────────────────
        item {
            Header(
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
                horizontalArrangement = Arrangement.spacedBy(16.dp),
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
            Spacer(Modifier.height(28.dp))
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
                    accentColor = AppColors.Olive,
                    modifier    = Modifier.weight(1f)
                )
                StatCard(
                    icon        = "🔥",
                    value       = "${state.longestStreak} дн",
                    label       = "рекорд",
                    accentColor = AppColors.Ochre,
                    modifier    = Modifier.weight(1f)
                )
                StatCard(
                    icon        = "✦",
                    value       = "${state.totalXp}",
                    label       = "очки XP",
                    accentColor = AppColors.Terracotta,
                    modifier    = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(28.dp))
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
                Spacer(Modifier.height(28.dp))
            }
        }

        // ── LEVEL UP ──────────────────────────────────────────
        if (state.shouldLevelUp) {
            item {
                LevelUpBanner(
                    currentLevel = state.spanishLevel,
                    onClick      = { navController.navigate("settings") },
                    modifier     = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(28.dp))
            }
        }

        // ── УЧИТЬСЯ ───────────────────────────────────────────
        item {
            SectionHeader(
                title    = "Учиться",
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(14.dp))
        }

        item {
            LearningModes(
                dueCount      = state.dueWordsCount,
                sessionPlan   = state.sessionPlan,
                navController = navController,
                modifier      = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(28.dp))
        }

        // ── ЕЩЁ ──────────────────────────────────────────────
        item {
            SectionHeader(
                title    = "Ещё",
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(14.dp))
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
//  HEADER  —  спокойный, без градиентов
// ═══════════════════════════════════════════════════════════════
@Composable
private fun Header(
    displayName: String,
    streak: Int,
    spanishLevel: String,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, top = 56.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text  = greetingByTime(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.InkLight
                )
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text       = if (displayName.isNotEmpty()) displayName else "Estudiante",
                        style      = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color      = AppColors.Ink
                    )
                    LevelBadge(level = spanishLevel)
                }
            }

            IconButton(
                onClick  = onSettingsClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Настройки",
                    tint     = AppColors.InkMid,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        StreakBadge(streak = streak, large = true)
    }
}

// ═══════════════════════════════════════════════════════════════
//  LEVEL UP BANNER  —  ненавязчивый
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
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.OchreBg)
            .border(1.dp, AppColors.OchreSoft, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("🏆", fontSize = 24.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Готов перейти на $nextLevel?",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = AppColors.OchreDark
                )
                Text(
                    "Ты освоил $currentLevel — можно повышать сложность",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.InkMid
                )
            }
            Icon(
                Icons.Default.ArrowForward, null,
                tint     = AppColors.OchreDark,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  LEARNING MODES
// ═══════════════════════════════════════════════════════════════
@Composable
private fun LearningModes(
    dueCount: Int,
    sessionPlan: com.spanishapp.domain.algorithm.AdaptiveLearning.SessionPlan,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {

        HeroFeatureCard(
            title         = "Карточки",
            subtitle      = if (dueCount > 0)
                                "$dueCount к повторению · ${sessionPlan.newWords} новых"
                            else "Все повторения на сегодня выполнены",
            icon          = "🃏",
            onClick       = { navController.navigate("flashcards") },
            badgeText     = if (dueCount > 0) "$dueCount" else null
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FeatureCard(
                title       = "Спряжения",
                subtitle    = "160 глаголов",
                icon        = "📝",
                onClick     = { navController.navigate("conjugation") },
                accentColor = AppColors.Olive,
                modifier    = Modifier.weight(1f)
            )
            FeatureCard(
                title       = "Диалоги",
                subtitle    = "Реальные ситуации",
                icon        = "💬",
                onClick     = { navController.navigate("dialogues") },
                accentColor = AppColors.Ochre,
                modifier    = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FeatureCard(
                title       = "Грамматика",
                subtitle    = "A1 · A2 · B1",
                icon        = "📖",
                onClick     = { navController.navigate("grammar") },
                accentColor = AppColors.Indigo,
                modifier    = Modifier.weight(1f)
            )
            FeatureCard(
                title       = "Произношение",
                subtitle    = "Говори и слушай",
                icon        = "🎤",
                onClick     = { navController.navigate("pronunciation") },
                accentColor = AppColors.Terracotta,
                modifier    = Modifier.weight(1f)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  QUICK ACTIONS  —  чистые маленькие кнопки
// ═══════════════════════════════════════════════════════════════
@Composable
private fun QuickActions(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple("weak_words", "Слабые слова",  "⚠"),
        Triple("quiz",       "Тест",          "✓"),
        Triple("games",      "Игры",          "♟"),
        Triple("ai_chat",    "ИИ-репетитор",  "✨")
    )
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEach { (route, label, icon) ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, AppColors.Border, RoundedCornerShape(14.dp))
                    .clickable { navController.navigate(route) }
                    .padding(vertical = 14.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(icon, fontSize = 18.sp, color = AppColors.Terracotta, fontWeight = FontWeight.Bold)
                    Text(
                        text      = label,
                        style     = MaterialTheme.typography.labelSmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color     = AppColors.InkMid,
                        fontWeight = FontWeight.Medium,
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
