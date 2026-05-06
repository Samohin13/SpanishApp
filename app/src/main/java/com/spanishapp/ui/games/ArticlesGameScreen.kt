package com.spanishapp.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.spanishapp.domain.games.GameId
import com.spanishapp.ui.games.common.LevelCompleteSheet
import com.spanishapp.ui.games.common.LevelMapScreen
import java.text.Normalizer

private val ACCENT = Color(0xFF7B2FBE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesGameScreen(
    navController: NavHostController,
    viewModel: ArticlesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val haptic = LocalHapticFeedback.current

    when {
        // ── Карта уровней ─────────────────────────────────────
        state.showLevelMap -> {
            LevelMapScreen(
                gameId  = GameId.ARTICLES,
                title   = "Artículos · уровни",
                accent  = ACCENT,
                manager = viewModel.levelManager,
                onBack  = { navController.popBackStack() },
                onLevelStart = { lvl -> viewModel.startLevel(lvl) }
            )
        }

        // ── Финальный диалог ──────────────────────────────────
        state.isGameOver -> {
            // Сначала показать сам экран (фон) + диалог поверх
            ArticlesGameContent(state, viewModel, haptic, onBack = { viewModel.openLevelMap() })
            LevelCompleteSheet(
                level   = state.level,
                stars   = state.finalStars,
                percent = state.finalPercent,
                accent  = ACCENT,
                onRetry = { viewModel.startLevel(state.level) },
                onNext  = if (state.finalStars > 0 && state.level < 100)
                              { { viewModel.startLevel(state.level + 1) } }
                          else null,
                onExit  = { viewModel.openLevelMap() }
            )
        }

        // ── Игра ──────────────────────────────────────────────
        else -> ArticlesGameContent(state, viewModel, haptic,
            onBack = { viewModel.openLevelMap() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticlesGameContent(
    state: ArticlesPremiumState,
    viewModel: ArticlesViewModel,
    haptic: HapticFeedback,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Уровень ${state.level} / 100",
                            fontWeight = FontWeight.Bold)
                        Text("${state.params.cefr.joinToString("+")} · ${state.params.mode.name.lowercase()}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F8FA))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Прогресс раундов
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("XP: ${state.score}", fontWeight = FontWeight.Bold, color = ACCENT)
                Text("Раунд ${state.currentRound} / ${state.totalRounds}",
                    color = Color.Gray, fontSize = 13.sp)
            }
            LinearProgressIndicator(
                progress = { state.currentRound.toFloat() / state.totalRounds.coerceAtLeast(1) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = ACCENT,
                trackColor = Color(0xFFE5E5EA)
            )

            // Серия
            if (state.streak > 1) {
                Text("🔥 серия: ${state.streak} (×${"%.1f".format(state.multiplier)})",
                    color = Color(0xFFFF9500), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(Modifier.height(8.dp))

            // Карточка слова
            state.currentWord?.let { word ->
                val context = LocalContext.current
                val imageFile = stripAccents(word.word.lowercase())
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 6.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Картинка из assets
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data("file:///android_asset/word_images/$imageFile.png")
                                .crossfade(true)
                                .build(),
                            contentDescription = word.word,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        )
                        // Название слова
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = word.word,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Подсказка / результат
            Box(modifier = Modifier.height(80.dp), contentAlignment = Alignment.Center) {
                if (state.academicHint?.isNotBlank() == true) {
                    Surface(
                        color = Color(0xFFFFF9C4),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = Color(0xFFFBC02D))
                            Spacer(Modifier.width(8.dp))
                            Text(state.academicHint, fontSize = 13.sp, color = Color(0xFF5D4037))
                        }
                    }
                } else if (state.lastCorrect != null) {
                    val ok = state.lastCorrect == true
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            if (ok) Icons.Default.Check else Icons.Default.Close, null,
                            tint = if (ok) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                        Text(
                            if (ok) "¡Excelente!" else "Incorrecto",
                            fontWeight = FontWeight.Bold,
                            color = if (ok) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Кнопки EL / LA / LOS / LAS — сетка 2×2
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ArticleButton(
                        label = "EL",
                        gradient = Brush.verticalGradient(
                            listOf(Color(0xFF2196F3), Color(0xFF1976D2))),
                        enabled = state.lastCorrect == null,
                        onClick = {
                            viewModel.submitAnswer("el")
                            triggerHaptic(haptic, state.currentWord?.article == "el")
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ArticleButton(
                        label = "LA",
                        gradient = Brush.verticalGradient(
                            listOf(Color(0xFFFF8A65), Color(0xFFD84315))),
                        enabled = state.lastCorrect == null,
                        onClick = {
                            viewModel.submitAnswer("la")
                            triggerHaptic(haptic, state.currentWord?.article == "la")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ArticleButton(
                        label = "LOS",
                        gradient = Brush.verticalGradient(
                            listOf(Color(0xFF4FC3F7), Color(0xFF0288D1))),
                        enabled = state.lastCorrect == null,
                        onClick = {
                            viewModel.submitAnswer("los")
                            triggerHaptic(haptic, state.currentWord?.article == "los")
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ArticleButton(
                        label = "LAS",
                        gradient = Brush.verticalGradient(
                            listOf(Color(0xFFF06292), Color(0xFFC2185B))),
                        enabled = state.lastCorrect == null,
                        onClick = {
                            viewModel.submitAnswer("las")
                            triggerHaptic(haptic, state.currentWord?.article == "las")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ArticleButton(
    label: String,
    gradient: Brush,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(80.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled) gradient
                    else Brush.verticalGradient(listOf(Color.LightGray, Color.Gray))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(label, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
    }
}

/** gato → gato, pájaro → pajaro, etc. */
private fun stripAccents(s: String): String {
    val normalized = Normalizer.normalize(s, Normalizer.Form.NFD)
    return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
}

private fun triggerHaptic(haptic: HapticFeedback, success: Boolean) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    @Suppress("UNUSED_PARAMETER") val unused = success   // оставлено для совместимости
}
