package com.spanishapp.ui.games

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.spanishapp.domain.games.GameId
import com.spanishapp.ui.games.common.LevelCompleteSheet
import com.spanishapp.ui.games.common.LevelMapScreen
import kotlinx.coroutines.delay
import java.text.Normalizer

private val ACCENT        = Color(0xFF7B2FBE)
private val BG            = Color(0xFFF0EEF8)   // лёгкий фиолетовый фон

private val COLOR_EL      = Color(0xFF1565C0)
private val COLOR_LA      = Color(0xFFB71C1C)
private val COLOR_LOS     = Color(0xFF00695C)
private val COLOR_LAS     = Color(0xFF6A1B9A)

private val COLOR_CORRECT = Color(0xFF2E7D32)
private val COLOR_WRONG   = Color(0xFFC62828)

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

// ── Основной экран ────────────────────────────────────────────

@Composable
private fun ArticlesGameContent(
    state: ArticlesPremiumState,
    viewModel: ArticlesViewModel,
    onBack: () -> Unit
) {
    val haptic     = LocalHapticFeedback.current
    val answered   = state.lastCorrect != null
    val isCorrect  = state.lastCorrect == true
    val word       = state.currentWord
    val showPlural = word?.block?.let { it != "A1-base" } ?: false

    // XP popup
    var showXp by remember { mutableStateOf(false) }
    LaunchedEffect(state.score) {
        if (state.lastXpGain > 0) {
            showXp = true
            delay(1200)
            showXp = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BG)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Хедер ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 10.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color(0xFF3A3A3C))
                }
                // Прогресс-точки
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)
                ) {
                    val total = state.totalRounds
                    repeat(total) { i ->
                        val dot = when {
                            i < state.answerHistory.size -> state.answerHistory[i]
                            else -> null
                        }
                        ProgressDot(state = dot, isCurrent = i == state.answerHistory.size)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "${state.currentRound}/${state.totalRounds}",
                    fontSize = 13.sp, color = Color(0xFF8E8E93), fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(10.dp))

            // ── Картинка отдельно ─────────────────────────────
            word?.let { w ->
                val context   = LocalContext.current
                val imageFile = stripAccents(w.word.lowercase())

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(220.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data("file:///android_asset/word_images/$imageFile.png")
                            .crossfade(true)
                            .build(),
                        contentDescription = w.word,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Лёгкий градиент снизу для красоты
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.12f))
                                )
                            )
                    )
                    // Уровень — бейдж в углу
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = ACCENT.copy(alpha = 0.85f)
                    ) {
                        Text(
                            "Lvl ${state.level}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── Слово ─────────────────────────────────────────
            Text(
                text = word?.word ?: "",
                fontSize = 46.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A1A2E),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            // ── XP-попап ─────────────────────────────────────
            Box(modifier = Modifier.height(36.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedVisibility(
                        visible = showXp && state.lastXpGain > 0,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit  = fadeOut() + slideOutVertically { -it }
                    ) {
                        Text(
                            "+${state.lastXpGain} XP ✨",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFB300)
                        )
                    }
                }
            }

            // ── Combo-бейдж ──────────────────────────────────
            AnimatedVisibility(
                visible = state.streak >= 3,
                enter = scaleIn() + fadeIn(),
                exit  = scaleOut() + fadeOut()
            ) {
                ComboBadge(streak = state.streak)
            }

            // ── Фидбэк после ответа ───────────────────────────
            AnimatedVisibility(
                visible = answered,
                enter = expandVertically() + fadeIn(),
                exit  = shrinkVertically() + fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                            null,
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

            // ── XP-итого ─────────────────────────────────────
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 4.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("⚡ ${state.score} XP", fontSize = 13.sp, color = ACCENT, fontWeight = FontWeight.SemiBold)
                Text(
                    "${state.correctCount} / ${state.currentRound.coerceAtLeast(1) - if (answered) 0 else 0} верных",
                    fontSize = 13.sp, color = Color(0xFF8E8E93)
                )
            }

            // ── Кнопки ───────────────────────────────────────
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 30.dp, top = 8.dp),
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
}

// ── Комбо-бейдж ──────────────────────────────────────────────

@Composable
private fun ComboBadge(streak: Int) {
    val (emoji, label, bg) = when {
        streak >= 10 -> Triple("🏆", "×$streak CAMPEÓN!", Color(0xFFFFD700))
        streak >= 7  -> Triple("🌟", "×$streak PERFECTO!", Color(0xFFFF6F00))
        streak >= 5  -> Triple("⚡", "×$streak SERIE!", Color(0xFFE91E63))
        else         -> Triple("🔥", "×$streak COMBO", Color(0xFFFF5722))
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            tween(600, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "scale"
    )

    Surface(
        modifier = Modifier
            .scale(scale)
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = bg
    ) {
        Text(
            "$emoji $label",
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 7.dp),
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

// ── Точка прогресса ───────────────────────────────────────────

@Composable
private fun ProgressDot(state: Boolean?, isCurrent: Boolean) {
    val color = when {
        state == true  -> COLOR_CORRECT
        state == false -> COLOR_WRONG
        isCurrent      -> ACCENT
        else           -> Color(0xFFD1D1D6)
    }
    val size = if (isCurrent) 10.dp else 8.dp
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
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
    textSize: TextUnit = 28.sp
) {
    val answered = state.lastCorrect != null
    val isChosen = state.chosenArticle == article
    val isRight  = state.currentWord?.article == article

    val bgColor = when {
        answered && isRight              -> COLOR_CORRECT
        answered && isChosen && !isRight -> COLOR_WRONG
        answered                         -> baseColor.copy(alpha = 0.18f)
        else                             -> baseColor
    }
    val textColor = if (answered && !isRight && !isChosen) baseColor.copy(alpha = 0.28f)
                    else Color.White

    // Пульс при правильном ответе
    val scale by animateFloatAsState(
        targetValue = if (answered && isRight) 1.04f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "btn_scale"
    )

    Surface(
        modifier = modifier
            .height(height)
            .scale(scale)
            .clickable(enabled = !answered) {
                onSubmit(article)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        shadowElevation = if (!answered) 4.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    label,
                    fontSize = textSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )
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

// ─────────────────────────────────────────────────────────────

private fun stripAccents(s: String): String {
    val normalized = Normalizer.normalize(s, Normalizer.Form.NFD)
    return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
}
