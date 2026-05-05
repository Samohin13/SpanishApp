package com.spanishapp.ui.games.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spanishapp.data.db.entity.GameLevelProgressEntity
import com.spanishapp.domain.games.GameLevelManager
import com.spanishapp.domain.games.LevelDifficulty
import com.spanishapp.domain.games.LevelMode
import kotlinx.coroutines.launch

/**
 * Универсальный экран выбора уровня (10×10 сетка). Используется всеми играми.
 *
 * @param gameId       идентификатор игры (см. GameId)
 * @param title        название игры в шапке
 * @param accent       цвет акцента (соответствует карточке игры)
 * @param manager      инжектится из ViewModel
 * @param onBack       вернуться к списку игр
 * @param onLevelStart колбэк на старт уровня
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelMapScreen(
    gameId: String,
    title: String,
    accent: Color,
    manager: GameLevelManager,
    onBack: () -> Unit,
    onLevelStart: (Int) -> Unit
) {
    var progress by remember { mutableStateOf<Map<Int, GameLevelProgressEntity>>(emptyMap()) }
    var nextLevel by remember { mutableIntStateOf(1) }
    var totalStars by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(gameId) {
        progress = manager.getProgressMap(gameId)
        nextLevel = manager.nextLevel(gameId)
        totalStars = manager.totalStars(gameId)
    }

    Scaffold(
        containerColor = Color(0xFFF8F8FA),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, fontWeight = FontWeight.Bold)
                        Text(
                            "★ $totalStars / 300",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items((1..100).toList()) { level ->
                val entry = progress[level]
                val unlocked = level <= nextLevel
                val isNext = level == nextLevel

                LevelCell(
                    level    = level,
                    stars    = entry?.stars ?: 0,
                    unlocked = unlocked,
                    isNext   = isNext,
                    accent   = accent,
                    onClick  = {
                        if (unlocked) {
                            scope.launch { onLevelStart(level) }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LevelCell(
    level: Int,
    stars: Int,
    unlocked: Boolean,
    isNext: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    val params = LevelDifficulty.forLevel(level)
    val bgColor = when {
        !unlocked -> Color(0xFFE5E5EA)
        stars > 0 -> accent.copy(alpha = 0.18f)
        isNext    -> accent
        else      -> Color.White
    }
    val textColor = when {
        !unlocked -> Color(0xFFC7C7CC)
        isNext    -> Color.White
        else      -> Color(0xFF1A1A1A)
    }

    Surface(
        shape    = RoundedCornerShape(14.dp),
        color    = bgColor,
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(enabled = unlocked, onClick = onClick),
        shadowElevation = if (isNext) 4.dp else 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (!unlocked) {
                Icon(Icons.Default.Lock, null,
                    tint = Color(0xFFC7C7CC),
                    modifier = Modifier.size(20.dp))
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = level.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    if (stars > 0) {
                        Row {
                            repeat(3) { i ->
                                Icon(
                                    Icons.Default.Star, null,
                                    tint = if (i < stars) Color(0xFFFFC107)
                                           else Color(0xFFE5E5EA),
                                    modifier = Modifier.size(8.dp)
                                )
                            }
                        }
                    } else if (isNext) {
                        // подпись режима для следующего уровня
                        Text(
                            params.mode.label(),
                            fontSize = 8.sp,
                            color = textColor.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

private fun LevelMode.label(): String = when (this) {
    LevelMode.TUTORIAL -> "TUT"
    LevelMode.EASY     -> "EASY"
    LevelMode.NORMAL   -> "NORM"
    LevelMode.HARD     -> "HARD"
    LevelMode.EXPERT   -> "EXP"
    LevelMode.MASTER   -> "★"
}
