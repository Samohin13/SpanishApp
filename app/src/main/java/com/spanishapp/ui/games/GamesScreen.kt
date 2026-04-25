package com.spanishapp.ui.games

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.spanishapp.ui.theme.AppColors

// ── Модель ────────────────────────────────────────────────────

data class GameCard(
    val route: String,
    val emoji: String,
    val title: String,
    val description: String,
    val color: androidx.compose.ui.graphics.Color,
    val isReady: Boolean = true
)

private val games = listOf(
    GameCard(
        route       = "game_articles",
        emoji       = "🏷️",
        title       = "Артикли",
        description = "el · la · un · una\nВыбери правильный артикль для слова",
        color       = AppColors.Teal,
        isReady     = true
    ),
    GameCard(
        route       = "game_speed",
        emoji       = "⚡",
        title       = "На скорость",
        description = "4 варианта, таймер\nУспей выбрать верный перевод",
        color       = AppColors.Terracotta,
        isReady     = true
    ),
    GameCard(
        route       = "game_anagram",
        emoji       = "🔤",
        title       = "Анаграмма",
        description = "Собери слово из\nперемешанных букв",
        color       = AppColors.Gold,
        isReady     = true
    ),
    GameCard(
        route       = "quiz",
        emoji       = "🎯",
        title       = "Тест",
        description = "10 вопросов с 4 вариантами\nПроверь знание переводов",
        color       = AppColors.Info,
        isReady     = true
    ),
    GameCard(
        route       = "dialogues",
        emoji       = "💬",
        title       = "Диалоги",
        description = "Ситуационные диалоги\nВ кафе, аэропорту, магазине…",
        color       = androidx.compose.ui.graphics.Color(0xFF7C5CBF),
        isReady     = true
    )
)

// ── Экран хаба ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Игры") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Тренируйся играя",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(games) { game ->
                    GameCardItem(game) {
                        if (game.isReady) navController.navigate(game.route)
                    }
                }
            }
        }
    }
}

// ── Карточка игры ─────────────────────────────────────────────

@Composable
private fun GameCardItem(game: GameCard, onClick: () -> Unit) {
    Surface(
        onClick  = onClick,
        shape    = RoundedCornerShape(20.dp),
        color    = game.color.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Emoji
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = game.color.copy(alpha = 0.15f),
                modifier = Modifier.size(60.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(game.emoji, fontSize = 28.sp)
                }
            }
            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    game.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = game.color
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    game.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight, null,
                tint = game.color.copy(alpha = 0.6f)
            )
        }
    }
}
