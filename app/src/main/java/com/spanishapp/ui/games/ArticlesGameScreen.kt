package com.spanishapp.ui.games

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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

// ── Цвета ────────────────────────────────────────────────────
private val ACCENT        = Color(0xFF7B2FBE)
private val COLOR_CORRECT = Color(0xFF2E7D32)
private val COLOR_WRONG   = Color(0xFFC62828)

private val COLOR_EL  = Color(0xFF1565C0)   // синий
private val COLOR_LA  = Color(0xFFC62828)   // красный
private val COLOR_LOS = Color(0xFF00695C)   // бирюзовый
private val COLOR_LAS = Color(0xFF6A1B9A)   // фиолетовый

// ─────────────────────────────────────────────────────────────

@Composable
fun ArticlesGameScreen(
    navController: NavHostController,
    viewModel: ArticlesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    when {
        state.showLevelMap -> {
            LevelMapScreen(
                gameId       = GameId.ARTICLES,
                title        = "Artículos · уровни",
                accent       = ACCENT,
                manager      = viewModel.levelManager,
                onBack       = { navController.popBackStack() },
                onLevelStart = { lvl -> viewModel.startLevel(lvl) }
            )
        }
        state.isGameOver -> {
            ArticlesGameContent(state, viewModel, onBack = { viewModel.openLevelMap() })
            LevelCompleteSheet(
                level   = state.level,
                stars   = state.finalStars,
                percent = state.finalPercent,
                accent  = ACCENT,
                onRetry = { viewModel.startLevel(state.level) },
                onNext  = if (state.finalStars > 0 && state.level < 100)
                              { { viewModel.startLevel(state.level + 1) } } else null,
                onExit  = { viewModel.openLevelMap() }
            )
        }
        else -> ArticlesGameContent(state, viewModel, onBack = { viewModel.openLevelMap() })
    }
}

// ── Основной экран игры ───────────────────────────────────────

@Composable
private fun ArticlesGameContent(
    state: ArticlesPremiumState,
    viewModel: ArticlesViewModel,
    onBack: () -> Unit
) {
    val haptic    = LocalHapticFeedback.current
    val answered  = state.lastCorrect != null
    val isCorrect = state.lastCorrect == true
    val word      = state.currentWord

    // Показывать множественные артикли только когда уровень их включает
    val showPlural = word?.block?.let { it != "A1-base" } ?: false

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Шапка: назад + прогресс + счётчик ────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color(0xFF3A3A3C))
                }
                LinearProgressIndicator(
                    progress = { state.currentRound.toFloat() / state.totalRounds.coerceAtLeast(1) },
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = ACCENT,
                    trackColor = Color(0xFFD1D1D6)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "${state.currentRound}/${state.totalRounds}",
                    fontSize = 13.sp,
                    color = Color(0xFF8E8E93),
                    fontWeight = FontWeight.Medium
                )
            }

            // ── XP + серия + уровень ──────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "⚡ ${state.score} XP",
                    fontSize = 13.sp, color = ACCENT, fontWeight = FontWeight.SemiBold
                )
                if (state.streak >= 2) {
                    Surface(
                        color = Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "🔥 ${state.streak} × ${"%.1f".format(state.multiplier)}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            fontSize = 12.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    "Уровень ${state.level}",
                    fontSize = 13.sp, color = Color(0xFF8E8E93)
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Карточка слова ────────────────────────────────
            word?.let { w ->
                val context   = LocalContext.current
                val imageFile = stripAccents(w.word.lowercase())

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    border = when {
                        answered && isCorrect  -> BorderStroke(2.5.dp, COLOR_CORRECT)
                        answered && !isCorrect -> BorderStroke(2.5.dp, COLOR_WRONG)
                        else -> null
                    },
                    shadowElevation = if (answered) 0.dp else 4.dp
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data("file:///android_asset/word_images/$imageFile.png")
                                .crossfade(true)
                                .build(),
                            contentDescription = w.word,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(185.dp)
                                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = w.word,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1C1C1E),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        if (w.russian.isNotBlank()) {
                            Text(
                                text = w.russian,
                                fontSize = 16.sp,
                                color = Color(0xFF8E8E93),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                    }
                }

                // ── Фидбэк после ответа ───────────────────────
                AnimatedVisibility(
                    visible = answered,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Surface(
                        color = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (isCorrect) COLOR_CORRECT else COLOR_WRONG,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (isCorrect) "¡Correcto!  ${w.article} ${w.word}"
                                    else "Правильно:  ${w.article} ${w.word}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isCorrect) COLOR_CORRECT else COLOR_WRONG
                                )
                            }
                            if (!isCorrect && w.ruleHint.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "💡 ${w.ruleHint}",
                                    fontSize = 13.sp,
                                    color = Color(0xFF555555),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Подпись ───────────────────────────────────────
            if (!answered) {
                Text(
                    "Выбери правильный артикль",
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = Color(0xFF8E8E93)
                )
            }

            // ── Кнопки артиклей ───────────────────────────────
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (showPlural) {
                    // 4 кнопки: 2×2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ArticleBtn(
                            label = "el", baseColor = COLOR_EL, article = "el",
                            state = state, haptic = haptic, onSubmit = viewModel::submitAnswer,
                            modifier = Modifier.weight(1f)
                        )
                        ArticleBtn(
                            label = "la", baseColor = COLOR_LA, article = "la",
                            state = state, haptic = haptic, onSubmit = viewModel::submitAnswer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ArticleBtn(
                            label = "los", baseColor = COLOR_LOS, article = "los",
                            state = state, haptic = haptic, onSubmit = viewModel::submitAnswer,
                            modifier = Modifier.weight(1f)
                        )
                        ArticleBtn(
                            label = "las", baseColor = COLOR_LAS, article = "las",
                            state = state, haptic = haptic, onSubmit = viewModel::submitAnswer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    // 2 большие кнопки: el / la
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ArticleBtn(
                            label = "el", baseColor = COLOR_EL, article = "el",
                            state = state, haptic = haptic, onSubmit = viewModel::submitAnswer,
                            modifier = Modifier.weight(1f), height = 96.dp, fontSize = 36.sp
                        )
                        ArticleBtn(
                            label = "la", baseColor = COLOR_LA, article = "la",
                            state = state, haptic = haptic, onSubmit = viewModel::submitAnswer,
                            modifier = Modifier.weight(1f), height = 96.dp, fontSize = 36.sp
                        )
                    }
                }
            }
        }
    }
}

// ── Кнопка артикля ───────────────────────────────────────────

@Composable
private fun ArticleBtn(
    label: String,
    baseColor: Color,
    article: String,
    state: ArticlesPremiumState,
    haptic: HapticFeedback,
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 72.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 26.sp
) {
    val answered  = state.lastCorrect != null
    val isChosen  = state.chosenArticle == article
    val isRight   = state.currentWord?.article == article

    val btnColor = when {
        answered && isRight              -> COLOR_CORRECT
        answered && isChosen && !isRight -> COLOR_WRONG
        answered                         -> baseColor.copy(alpha = 0.22f)
        else                             -> baseColor
    }
    val textColor = if (answered && !isRight && !isChosen) baseColor.copy(alpha = 0.35f)
                    else Color.White

    Surface(
        modifier = modifier
            .height(height)
            .clickable(enabled = !answered) {
                onSubmit(article)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
        shape = RoundedCornerShape(20.dp),
        color = btnColor,
        shadowElevation = if (!answered) 3.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    fontSize = fontSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )
                if (answered && isRight) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Default.Check, contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(if (height >= 96.dp) 26.dp else 18.dp)
                    )
                } else if (answered && isChosen && !isRight) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Default.Close, contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(if (height >= 96.dp) 26.dp else 18.dp)
                    )
                }
            }
        }
    }
}

// ── Утилиты ───────────────────────────────────────────────────

private fun stripAccents(s: String): String {
    val normalized = Normalizer.normalize(s, Normalizer.Form.NFD)
    return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
}
