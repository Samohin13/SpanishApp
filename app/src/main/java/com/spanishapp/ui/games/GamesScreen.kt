package com.spanishapp.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    val color: Color,
    val isReady: Boolean = true
)

private val games = listOf(
    GameCard(
        route       = "game_articles",
        emoji       = "🏷️",
        title       = "Артикли El/La",
        description = "Мужской или женский род?",
        color       = AppColors.Teal
    ),
    GameCard(
        route       = "game_speed",
        emoji       = "⚡",
        title       = "На скорость",
        description = "Успей за таймером",
        color       = AppColors.Terracotta
    ),
    GameCard(
        route       = "game_anagram",
        emoji       = "🔤",
        title       = "Анаграмма",
        description = "Собери слово",
        color       = AppColors.Gold
    ),
    GameCard(
        route       = "quiz",
        emoji       = "🎯",
        title       = "Тест",
        description = "10 вопросов",
        color       = Color(0xFF2196F3)
    ),
    GameCard(
        route       = "dialogues",
        emoji       = "💬",
        title       = "Диалоги",
        description = "Ситуации из жизни",
        color       = Color(0xFF7C5CBF)
    ),
    GameCard(
        route       = "game_verb_form",
        emoji       = "🔧",
        title       = "Глаголы",
        description = "Формы и времена",
        color       = Color(0xFF2E7D32)
    ),
    GameCard(
        route       = "game_listening",
        emoji       = "🎧",
        title       = "Аудирование",
        description = "Слушай — вставь слово",
        color       = Color(0xFF1565C0)
    )
)

// ── Экран ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(navController: NavHostController) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Игровая зона", 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            SectionLabel("Выбери активность")

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(games) { game ->
                    GameHubItem(game) {
                        navController.navigate(game.route)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        letterSpacing = 1.2.sp
    )
}

@Composable
private fun GameHubItem(game: GameCard, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Иконка в круге
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(game.color.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
                    .border(1.dp, game.color.copy(alpha = 0.2f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(game.emoji, fontSize = 32.sp)
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                game.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                game.description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
