package com.spanishapp.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

private data class Game(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

private val GAMES: List<Game> = listOf(
    Game("Artículos", "Артикли el / la",
        Icons.Default.Category, Color(0xFF7B2FBE), "game_articles"),
    Game("Rápido", "Перевод на время",
        Icons.Default.Timer, Color(0xFFE040FB), "game_speed"),
    Game("Verbos", "Спряжения глаголов",
        Icons.Default.Translate, Color(0xFF2196F3), "conjugation_quiz"),
    Game("Sopa de Letras", "Филворд",
        Icons.Default.GridOn, Color(0xFF4CAF50), "game_sopa"),
    Game("Palabra Maestra", "Орфография",
        Icons.Default.TextFields, Color(0xFFFF9500), "game_palabra"),
    Game("Cálculo", "Математика на слух",
        Icons.Default.Calculate, Color(0xFFF44336), "game_math"),
    Game("Crucigrama", "100 уровней",
        Icons.Default.BorderAll, Color(0xFF26A69A), "game_crossword"),
    Game("Libros", "Рассказы + тесты",
        Icons.Default.MenuBook, Color(0xFF7B2FBE), "game_libros")
)

@Composable
fun GamesScreen(
    navController: NavHostController,
    vm: GamesScreenViewModel = hiltViewModel()
) {
    val progress by vm.gameProgress.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8FA))
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            Column {
                Text(
                    "Juegos 🎮",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(Modifier.height(4.dp))
            }
        }

        items(GAMES, key = { it.route }) { g ->
            GameCard(
                game = g,
                progress = progress[g.route],
                onClick = { navController.navigate(g.route) }
            )
        }
    }
}

@Composable
private fun GameCard(
    game: Game,
    progress: GameProgressInfo?,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(game.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(game.icon, null, tint = game.color, modifier = Modifier.size(24.dp))
            }

            Column {
                Text(
                    game.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    maxLines = 1
                )
                Text(
                    game.description,
                    fontSize = 12.sp,
                    color = Color(0xFF8E8E93),
                    lineHeight = 14.sp,
                    maxLines = 2
                )
            }

            // Прогресс / лучший результат
            if (progress != null && progress.label.isNotBlank()) {
                Surface(
                    color = game.color.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        progress.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = game.color,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            } else {
                Spacer(Modifier.height(0.dp))
            }
        }
    }
}

