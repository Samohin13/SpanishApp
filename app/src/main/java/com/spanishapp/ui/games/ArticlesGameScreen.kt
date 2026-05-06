package com.spanishapp.ui.games

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import com.spanishapp.data.db.entity.ArticleWordEntity
import com.spanishapp.domain.games.GameId
import com.spanishapp.ui.games.common.*
import java.text.Normalizer

// ── Цвета ─────────────────────────────────────────────────────
private val ACCENT        = Color(0xFF7B2FBE)
private val BG            = Color(0xFFF0EEF8)
private val COLOR_EL      = Color(0xFF1565C0)
private val COLOR_LA      = Color(0xFFB71C1C)
private val COLOR_LOS     = Color(0xFF00695C)
private val COLOR_LAS     = Color(0xFF6A1B9A)
private val COLOR_CORRECT = Color(0xFF2E7D32)
private val COLOR_WRONG   = Color(0xFFC62828)
private val DUO_GREEN     = Color(0xFF58CC02)   // фирменный зелёный Duolingo
private val DUO_RED       = Color(0xFFFF4B4B)   // фирменный красный Duolingo

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
                              { { viewModel.startLevel(state.level + 1, isTransition = true) } } else null,
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

    // ── Тряска карточки при ошибке ────────────────────────────
    val shakeOffset = rememberShakeOffset(
        trigger = state.answerHistory.size,
        isWrong = state.lastCorrect == false
    )

    // ── Конфетти: новый ключ при каждом верном ответе ─────────
    var confettiTrigger by remember { mutableIntStateOf(0) }
    LaunchedEffect(state.answerHistory.size) {
        if (state.lastCorrect == true) confettiTrigger++
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BG)
    ) {
        // ── Контент ───────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Хедер: назад + точки-прогресс + счётчик
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 20.dp, bottom = 8.dp), // top=20 — не лезет в статус-бар
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color(0xFF3A3A3C))
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    ProgressDots(
                        history = state.answerHistory,
                        total   = state.totalRounds,
                        accent  = ACCENT
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "${state.currentRound}/${state.totalRounds}",
                    fontSize = 13.sp, color = Color(0xFF8E8E93), fontWeight = FontWeight.SemiBold
                )
            }

            // ── XP + верных — крупно, над картинкой ──────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "⚡ ${state.score} XP",
                    fontSize = 22.sp, color = ACCENT, fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "✅ ${state.correctCount} верных",
                    fontSize = 20.sp, color = Color(0xFF3A3A3C), fontWeight = FontWeight.Bold
                )
            }

            // Картинка (с тряской) — квадратная
            word?.let { w ->
                val context   = LocalContext.current
                val imageFile = stripAccents(w.word.lowercase())
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .offset(x = shakeOffset.dp)
                        .aspectRatio(1f)              // квадрат
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
                    // Лёгкий градиент снизу
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.10f))
                                )
                            )
                    )
                    // Бейдж уровня
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
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

            Spacer(Modifier.height(20.dp))

            // Слово (с тряской)
            Text(
                text = word?.word ?: "",
                fontSize = 46.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A1A2E),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .offset(x = shakeOffset.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Комбо-бейдж
            AnimatedVisibility(
                visible = state.streak >= 3,
                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit  = scaleOut() + fadeOut()
            ) {
                ComboBadge(streak = state.streak)
            }

            Spacer(Modifier.weight(1f))

            // Кнопки — прячутся после ответа
            AnimatedVisibility(
                visible = !answered,
                enter = fadeIn(),
                exit  = fadeOut(tween(150))
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 30.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (showPlural) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ArticleBtn("el",  COLOR_EL,  haptic, { viewModel.submitAnswer("el")  }, Modifier.weight(1f))
                            ArticleBtn("la",  COLOR_LA,  haptic, { viewModel.submitAnswer("la")  }, Modifier.weight(1f))
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ArticleBtn("los", COLOR_LOS, haptic, { viewModel.submitAnswer("los") }, Modifier.weight(1f))
                            ArticleBtn("las", COLOR_LAS, haptic, { viewModel.submitAnswer("las") }, Modifier.weight(1f))
                        }
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            ArticleBtn("el", COLOR_EL, haptic, { viewModel.submitAnswer("el") },
                                Modifier.weight(1f), height = 96.dp, textSize = 36.sp)
                            ArticleBtn("la", COLOR_LA, haptic, { viewModel.submitAnswer("la") },
                                Modifier.weight(1f), height = 96.dp, textSize = 36.sp)
                        }
                    }
                }
            }

            // Placeholder-высота пока шит скрыт (чтобы кнопки не прыгали)
            if (!answered) Spacer(Modifier.height(0.dp))
        }

        // ── Конфетти ─────────────────────────────────────────
        ConfettiEffect(trigger = if (isCorrect) confettiTrigger else 0)

        // ── Duolingo-шит снизу ────────────────────────────────
        Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
            AnimatedVisibility(
                visible = answered,
                enter   = slideInVertically { it } + fadeIn(),
                exit    = slideOutVertically { it } + fadeOut()
            ) {
                AnswerSheet(
                    isCorrect  = isCorrect,
                    word       = word,
                    xpGain     = state.lastXpGain,
                    onContinue = { viewModel.continueToNext() }
                )
            }
        }
    }
}

// ── Лист ответа (Duolingo style) ─────────────────────────────

@Composable
private fun AnswerSheet(
    isCorrect: Boolean,
    word: ArticleWordEntity?,
    xpGain: Int,
    onContinue: () -> Unit
) {
    val sheetColor = if (isCorrect) DUO_GREEN else DUO_RED

    Surface(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color     = sheetColor,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Иконка + текст результата
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color  = Color.White.copy(alpha = 0.25f),
                    shape  = CircleShape,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint     = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        if (isCorrect) "¡Correcto!" else "Incorrecto",
                        color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp
                    )
                    if (isCorrect && xpGain > 0) {
                        Text("+$xpGain XP", color = Color.White.copy(alpha = 0.9f), fontSize = 15.sp)
                    }
                    if (!isCorrect && word != null) {
                        Text(
                            "${word.article} ${word.word}",
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 17.sp, fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Правило (если ошибка)
            if (!isCorrect && word?.ruleHint?.isNotBlank() == true) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "💡 ${word.ruleHint}",
                        modifier = Modifier.padding(12.dp),
                        color    = Color.White.copy(alpha = 0.92f),
                        fontSize = 13.sp, lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // CONTINUAR
            Button(
                onClick  = onContinue,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor   = sheetColor
                ),
                shape     = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text("CONTINUAR", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, letterSpacing = 1.sp)
            }
        }
    }
}

// ── Кнопка артикля ───────────────────────────────────────────

@Composable
private fun ArticleBtn(
    label: String,
    color: Color,
    haptic: HapticFeedback,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 72.dp,
    textSize: TextUnit = 28.sp
) {
    Surface(
        modifier = modifier
            .height(height)
            .clickable {
                onClick()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
        shape          = RoundedCornerShape(20.dp),
        color          = color,
        shadowElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontSize = textSize, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
    }
}

// ─────────────────────────────────────────────────────────────

private fun stripAccents(s: String): String {
    val normalized = Normalizer.normalize(s, Normalizer.Form.NFD)
    return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
}
