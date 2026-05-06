package com.spanishapp.ui.games

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

private val ACCENT        = Color(0xFF7B2FBE)
private val COLOR_CORRECT = Color(0xFF2E7D32)
private val COLOR_WRONG   = Color(0xFFC62828)
private val COLOR_EL      = Color(0xFF1565C0)
private val COLOR_LA      = Color(0xFFC62828)
private val COLOR_LOS     = Color(0xFF00695C)
private val COLOR_LAS     = Color(0xFF6A1B9A)

// ─────────────────────────────────────────────────────────────

@Composable
fun ArticlesGameScreen(
    navController: NavHostController,
    viewModel: ArticlesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    when {
        state.showLevelMap -> LevelMapScreen(
            gameId       = GameId.ARTICLES,
            title        = "Artículos · уровни",
            accent       = ACCENT,
            manager      = viewModel.levelManager,
            onBack       = { navController.popBackStack() },
            onLevelStart = { lvl -> viewModel.startLevel(lvl) }
        )
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
    val showPlural = word?.block?.let { it != "A1-base" } ?: false

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ── Хедер: назад + прогресс ───────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 16.dp, top = 10.dp, bottom = 8.dp),
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
                fontSize = 13.sp, color = Color(0xFF8E8E93), fontWeight = FontWeight.Medium
            )
        }

        // ── Карточка: картинка + слово ────────────────────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // Картинка
                word?.let { w ->
                    val context   = LocalContext.current
                    val imageFile = stripAccents(w.word.lowercase())
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data("file:///android_asset/word_images/$imageFile.png")
                            .crossfade(true)
                            .build(),
                        contentDescription = w.word,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    )
                }

                // Слово
                Text(
                    text = word?.word ?: "",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1C1C1E),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
                )
            }
        }

        // ── Фидбэк (компактный, только если ответили) ─────────
        if (answered) {
            Spacer(Modifier.height(12.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (isCorrect) COLOR_CORRECT else COLOR_WRONG,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            if (isCorrect) "¡Correcto!  ${word?.article} ${word?.word}"
                            else "Правильно:  ${word?.article} ${word?.word}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isCorrect) COLOR_CORRECT else COLOR_WRONG
                        )
                        if (!isCorrect && word?.ruleHint?.isNotBlank() == true) {
                            Text(
                                "💡 ${word.ruleHint}",
                                fontSize = 12.sp,
                                color = Color(0xFF555555),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // ── Кнопки артиклей ───────────────────────────────────
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (showPlural) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ArticleBtn("el",  COLOR_EL,  "el",  state, haptic, viewModel::submitAnswer, Modifier.weight(1f))
                    ArticleBtn("la",  COLOR_LA,  "la",  state, haptic, viewModel::submitAnswer, Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ArticleBtn("los", COLOR_LOS, "los", state, haptic, viewModel::submitAnswer, Modifier.weight(1f))
                    ArticleBtn("las", COLOR_LAS, "las", state, haptic, viewModel::submitAnswer, Modifier.weight(1f))
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ArticleBtn("el", COLOR_EL, "el", state, haptic, viewModel::submitAnswer,
                        Modifier.weight(1f), height = 96.dp, textSize = 36.sp)
                    ArticleBtn("la", COLOR_LA, "la", state, haptic, viewModel::submitAnswer,
                        Modifier.weight(1f), height = 96.dp, textSize = 36.sp)
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
    height: androidx.compose.ui.unit.Dp = 72.dp,
    textSize: androidx.compose.ui.unit.TextUnit = 28.sp
) {
    val answered = state.lastCorrect != null
    val isChosen = state.chosenArticle == article
    val isRight  = state.currentWord?.article == article

    val bgColor = when {
        answered && isRight              -> COLOR_CORRECT
        answered && isChosen && !isRight -> COLOR_WRONG
        answered                         -> baseColor.copy(alpha = 0.20f)
        else                             -> baseColor
    }
    val textColor = if (answered && !isRight && !isChosen) baseColor.copy(alpha = 0.30f)
                    else Color.White

    Surface(
        modifier = modifier
            .height(height)
            .clickable(enabled = !answered) {
                onSubmit(article)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        shadowElevation = if (!answered) 3.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(label, fontSize = textSize, fontWeight = FontWeight.ExtraBold, color = textColor)
                if (answered && isRight) {
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.Check, null, tint = Color.White,
                        modifier = Modifier.size(if (height >= 90.dp) 26.dp else 18.dp))
                } else if (answered && isChosen && !isRight) {
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.Close, null, tint = Color.White,
                        modifier = Modifier.size(if (height >= 90.dp) 26.dp else 18.dp))
                }
            }
        }
    }
}

private fun stripAccents(s: String): String {
    val normalized = Normalizer.normalize(s, Normalizer.Form.NFD)
    return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
}
