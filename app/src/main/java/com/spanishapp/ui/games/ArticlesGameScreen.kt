package com.spanishapp.ui.games

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.spanishapp.R
import com.spanishapp.data.db.entity.ArticleWordEntity
import com.spanishapp.domain.games.GameId
import com.spanishapp.ui.games.common.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// ── Цвета ─────────────────────────────────────────────────────
private val ACCENT        = Color(0xFF7B2FBE)
private val BG
    @Composable get() = MaterialTheme.colorScheme.background
private val ImageCardBg
    @Composable get() = MaterialTheme.colorScheme.surfaceContainerHighest
private val PrimaryText
    @Composable get() = MaterialTheme.colorScheme.onSurface
private val SecondaryText
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
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
    val state by viewModel.state.collectAsStateWithLifecycle()
    val mistakesCount by viewModel.mistakesCount.collectAsStateWithLifecycle()
    when {
        state.showLevelMap -> LevelMapScreen(
            gameId       = GameId.ARTICLES,
            title        = stringResource(R.string.art_levels_title),
            accent       = ACCENT,
            manager      = viewModel.levelManager,
            onBack       = { navController.popBackStack() },
            onLevelStart = { lvl -> viewModel.startLevel(lvl) },
            mistakesCount = mistakesCount,
            onMistakesPractice = { viewModel.startMistakesPractice() },
        )
        state.isGameOver -> {
            ArticlesGameContent(state, viewModel, onBack = { viewModel.openLevelMap() })
            LevelCompleteSheet(
                level   = state.level,
                stars   = state.finalStars,
                percent = state.finalPercent,
                accent  = ACCENT,
                onRetry = { viewModel.startLevel(state.level) },
                onNext  = when {
                    state.isMistakesPractice && mistakesCount > 0 -> { { viewModel.startMistakesPractice() } }
                    !state.isMistakesPractice && state.finalStars > 0 && state.level < 100 -> {
                        { viewModel.startLevel(state.level + 1, isTransition = true) }
                    }
                    else -> null
                },
                onExit  = { viewModel.openLevelMap() },
                isMistakesPractice = state.isMistakesPractice,
                mistakesCorrect = state.correctCount,
                mistakesTotal = state.totalRounds,
                mistakesPoolLeft = mistakesCount,
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
    val haptic     = com.spanishapp.ui.components.rememberCheckedHaptic()
    val answered   = state.lastCorrect != null
    val isCorrect  = state.lastCorrect == true
    val word       = state.currentWord
    val showPlural = word?.block?.let { it != "A1-base" } ?: false
    var showRulesSheet by remember { mutableStateOf(false) }

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
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Хедер: назад + точки-прогресс + счётчик
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PrimaryText)
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
                    fontSize = 15.sp, color = SecondaryText, fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(4.dp))
                // 💡 — постоянно доступная подсказка с правилом артиклей
                IconButton(onClick = { showRulesSheet = true }) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = "Правила артиклей",
                        tint = ACCENT,
                    )
                }
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
                    stringResource(R.string.art_correct_count, state.correctCount),
                    fontSize = 20.sp, color = PrimaryText, fontWeight = FontWeight.Bold
                )
            }

            // Карточка слова — flip-механика (тап → перевод + пример)
            // Картинки удалены, на лицевой стороне крупное испанское слово
            // на фиолетовом градиенте с уровнем-бейджем.
            word?.let { w ->
                var flipped by remember(w.word) { mutableStateOf(false) }
                val rotation by animateFloatAsState(
                    targetValue = if (flipped) 180f else 0f,
                    animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
                    label = "card_flip"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .offset(x = shakeOffset.dp)
                        .aspectRatio(1.4f)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12f * density
                        }
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF8B3FCE), Color(0xFF4F1F7F))
                            )
                        )
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            flipped = !flipped
                        }
                ) {
                    if (rotation <= 90f) {
                        // Лицевая сторона — слово крупно
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = w.word,
                                fontSize = 56.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        // Бейдж уровня
                        Surface(
                            modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Text(
                                if (state.isMistakesPractice) "📝"
                                else "Lvl ${state.level}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold
                            )
                        }
                        // Подсказка «тап для перевода»
                        Surface(
                            modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black.copy(alpha = 0.25f)
                        ) {
                            Text(
                                "👆 перевод",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp, color = Color.White
                            )
                        }
                    } else {
                        // Обратная сторона — артикль + перевод + пример
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { rotationY = 180f }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "${w.article} ${w.word}",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    w.russian.ifBlank { "—" },
                                    fontSize = 20.sp,
                                    color = Color.White.copy(alpha = 0.95f),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(14.dp))
                                Text(
                                    "Este es ${w.article} ${w.word}.",
                                    fontSize = 15.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

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

        // ── Bottom-sheet с правилом артиклей (кнопка 💡 в топбаре) ──
        if (showRulesSheet) {
            ArticlesRulesSheet(onDismiss = { showRulesSheet = false })
        }
    }
}

/**
 * Грамотное правило испанских артиклей — одно общее на всю игру.
 * Открывается тапом на 💡 в топбаре, доступно в любой момент уровня.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticlesRulesSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💡", fontSize = 28.sp)
                Spacer(Modifier.width(12.dp))
                Text(
                    "Правила артиклей",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryText,
                )
            }
            Spacer(Modifier.height(20.dp))

            RuleSectionHeader("Базовое правило", ACCENT)
            RuleRow(article = "EL", color = COLOR_EL, descr = "Мужской род: окончание -o, или согласный",
                examples = "el libro, el coche, el sol, el hotel")
            RuleRow(article = "LA", color = COLOR_LA, descr = "Женский род: окончание -a",
                examples = "la casa, la mesa, la flor, la mujer")

            Spacer(Modifier.height(20.dp))
            RuleSectionHeader("Окончания → артикль", ACCENT)
            RuleRow(article = "EL", color = COLOR_EL,
                descr = "-aje (paisaje), -ón (corazón)",
                examples = "el paisaje, el corazón, el camión")
            RuleRow(article = "LA", color = COLOR_LA,
                descr = "-ción, -sión, -dad, -tad, -tud, -ez",
                examples = "la canción, la ciudad, la libertad, la vejez")

            Spacer(Modifier.height(20.dp))
            RuleSectionHeader("Исключения (нужно запомнить)", ACCENT)
            RuleRow(article = "EL", color = COLOR_EL,
                descr = "Греческие на -ma: всегда EL",
                examples = "el problema, el sistema, el tema, el clima, el idioma, el mapa")
            RuleRow(article = "EL", color = COLOR_EL,
                descr = "Один знаменитый «не на -o»",
                examples = "el día")
            RuleRow(article = "LA", color = COLOR_LA,
                descr = "Сокращения от женских слов",
                examples = "la foto (← fotografía), la moto (← motocicleta), la radio")
            RuleRow(article = "LA", color = COLOR_LA,
                descr = "Один знаменитый «не на -a»",
                examples = "la mano")

            Spacer(Modifier.height(20.dp))
            RuleSectionHeader("Особый случай: ударное а-/ha-", ACCENT)
            Text(
                "Слово начинается на ударное «a-» или «ha-» → в единственном числе ставим EL для созвучия, НО слово остаётся женского рода (прилагательное — женское).",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = PrimaryText,
            )
            Spacer(Modifier.height(8.dp))
            ExampleBlock("el agua (но: el agua fría)\nel águila, el alma, el hambre, el área\nВо мн.ч.: las aguas, las águilas")

            Spacer(Modifier.height(20.dp))
            RuleSectionHeader("Множественное число", ACCENT)
            RuleRow(article = "LOS", color = COLOR_LOS, descr = "Мужской мн.", examples = "los libros, los coches")
            RuleRow(article = "LAS", color = COLOR_LAS, descr = "Женский мн.", examples = "las casas, las mujeres")
            Spacer(Modifier.height(8.dp))
            Text(
                "Окончание мн.числа:\n• Гласный → +s (libro → libros)\n• Согласный → +es (mujer → mujeres, ciudad → ciudades)",
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = PrimaryText,
            )
        }
    }
}

@Composable
private fun RuleSectionHeader(title: String, accent: Color) {
    Text(
        title,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        color = accent,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 10.dp),
    )
}

@Composable
private fun RuleRow(article: String, color: Color, descr: String, examples: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            color = color,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(top = 2.dp),
        ) {
            Text(
                article,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.ExtraBold,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(descr, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
            Spacer(Modifier.height(2.dp))
            Text(examples, fontSize = 13.sp, color = SecondaryText, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        }
    }
}

@Composable
private fun ExampleBlock(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text,
            modifier = Modifier.padding(12.dp),
            fontSize = 13.sp,
            lineHeight = 20.sp,
            color = PrimaryText,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
        )
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
                        fontSize = 15.sp, lineHeight = 18.sp
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

