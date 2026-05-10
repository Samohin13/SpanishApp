package com.spanishapp.ui.games

import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
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
    @androidx.annotation.StringRes val descriptionRes: Int,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

// Описания через @StringRes — переключаются по языку. Названия игр
// (Artículos, Rápido, etc.) — испанский бренд, не локализуем.
private val GAMES: List<Game> = listOf(
    Game("Artículos",      com.spanishapp.R.string.game_articles_desc,  Icons.Default.Category,   Color(0xFF7B2FBE), "game_articles"),
    Game("Rápido",         com.spanishapp.R.string.game_speed_desc,     Icons.Default.Timer,      Color(0xFFE040FB), "game_speed"),
    Game("Verbos",         com.spanishapp.R.string.game_verbos_desc,    Icons.Default.Translate,  Color(0xFF2196F3), "conjugation_quiz"),
    Game("Sopa de Letras", com.spanishapp.R.string.game_sopa_desc,      Icons.Default.GridOn,     Color(0xFF4CAF50), "game_sopa"),
    Game("Palabra Maestra",com.spanishapp.R.string.game_palabra_desc,   Icons.Default.TextFields, Color(0xFFFF9500), "game_palabra"),
    Game("Cálculo",        com.spanishapp.R.string.game_math_desc,      Icons.Default.Calculate,  Color(0xFFF44336), "game_math"),
    Game("Crucigrama",     com.spanishapp.R.string.game_crossword_desc, Icons.Default.BorderAll,  Color(0xFF26A69A), "game_crossword"),
    Game("Libros",         com.spanishapp.R.string.game_libros_desc,    Icons.Default.MenuBook,   Color(
        0xFFBEA62F
    ), "game_libros")
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
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            Column {
                Text(
                    androidx.compose.ui.res.stringResource(com.spanishapp.R.string.games_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
            }
        }

        itemsIndexed(GAMES, key = { _, g -> g.route }) { idx, g ->
            // Staggered entrance: each card slides in 60ms after the previous.
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(60L * idx)
                visible = true
            }
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 3 }
                ) + fadeIn(animationSpec = tween(300))
            ) {
                GameCard(
                    game = g,
                    progress = progress[g.route],
                    onClick = { navController.navigate(g.route) },
                    wobblePhase = (idx * 0.37f) % 1f
                )
            }
        }
    }
}

@Composable
private fun GameCard(
    game: Game,
    progress: GameProgressInfo?,
    onClick: () -> Unit,
    wobblePhase: Float = 0f
) {
    // Continuous micro-wobble: -3° ↔ +3° over 2.5s, phase-shifted per card so
    // they don't sway in lock-step.
    val infinite = rememberInfiniteTransition(label = "wobble_${game.route}")
    val wobble by infinite.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset((wobblePhase * 2500).toInt())
        ),
        label = "wobble_angle_${game.route}"
    )

    com.spanishapp.ui.components.PressableCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 4.dp
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
                    .background(game.color.copy(alpha = 0.12f))
                    .graphicsLayer { rotationZ = wobble },
                contentAlignment = Alignment.Center
            ) {
                Icon(game.icon, null, tint = game.color, modifier = Modifier.size(24.dp))
            }

            Column {
                Text(
                    game.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    androidx.compose.ui.res.stringResource(game.descriptionRes),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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

