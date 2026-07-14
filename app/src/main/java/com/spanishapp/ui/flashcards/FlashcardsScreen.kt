package com.spanishapp.ui.flashcards

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.spanishapp.data.db.entity.WordEntity
import com.spanishapp.domain.algorithm.LeaguePromotion
import com.spanishapp.domain.algorithm.ReviewButton
import com.spanishapp.ui.components.LeaguePromotionDialog
import com.spanishapp.ui.home.ThematicWatermark
import com.spanishapp.ui.home.WatermarkTheme
import kotlinx.coroutines.launch

// ── Brand colour aliases (local) ───────────────────────────────
private val BrandOrange   = Color(0xFFFF6B35)   // front face accent + progress bar + buttons
private val BrandOrange2  = Color(0xFFFF9A6C)   // completion header gradient end
private val CardBackAccent = Color(0xFF34C759)  // back face accent stripe + glow (iOS green)
private val EasyAmber     = Color(0xFFFFB300)   // v1.26.1: кнопка «Легко» (третья градация SM-2)

// ═══════════════════════════════════════════════════════════════
//  ENTRY POINT
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    navController: NavHostController,
    level: String,
    category: String,
    direction: FlashcardDirection,
    setId: String? = null,
    weakOnly: Boolean = false,
    viewModel: FlashcardsViewModel = hiltViewModel()
) {
    com.spanishapp.service.TrackActivity(com.spanishapp.service.ActivityType.FLASHCARDS)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = com.spanishapp.ui.components.rememberCheckedHaptic()
    com.spanishapp.ui.components.TrackStudyMinutes()

    var leaguePromotion by remember { mutableStateOf<LeaguePromotion?>(null) }
    LaunchedEffect(viewModel) {
        viewModel.leaguePromotions.collect { leaguePromotion = it }
    }
    leaguePromotion?.let { promo ->
        LeaguePromotionDialog(
            from = promo.from, to = promo.to,
            onDismiss = { leaguePromotion = null }
        )
    }

    LaunchedEffect(level, category, direction, setId, weakOnly) {
        when {
            setId != null -> viewModel.startSetSession(setId, direction)
            weakOnly      -> viewModel.startSession(level, category, direction, weakOnly = true)
            else          -> viewModel.startSession(level, category, direction)
        }
    }

    // v1.26.1 FIX (audit): выход посреди сессии молча терял XP — теперь
    // подтверждение (и на крестик, и на системный Back).
    var showExitDialog by remember { mutableStateOf(false) }
    val mustConfirmExit = state.currentIndex > 0 && !state.isFinished && state.error == null
    androidx.activity.compose.BackHandler(enabled = mustConfirmExit) { showExitDialog = true }
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Выйти из сессии?") },
            text = { Text("Прогресс этой сессии не сохранится") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    navController.popBackStack()
                }) { Text("Выйти") }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("Остаться") }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            // v1.26.1 FIX (audit): для set-сессии показываем тему сета
                            // («Местоимения»), а не generic «Изучение слов».
                            state.sessionTitle
                                ?: stringResource(com.spanishapp.R.string.flashcards_title),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            // v1.26.1 FIX (audit): 1-based счётчик по замороженной
                            // колоде (deckSize) — requeue не раздувает знаменатель.
                            "${(state.currentIndex + 1).coerceAtMost(state.deckSize)} из ${state.deckSize}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (mustConfirmExit) showExitDialog = true
                        else navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading  -> LoadingBody()
                state.isFinished -> SessionCompleteBody(
                    // v1.26.1 FIX (audit): процент финала — от исходной колоды,
                    // а не от sessionSize, раздутого requeue-ами.
                    total      = if (state.deckSize > 0) state.deckSize else state.sessionSize,
                    correct    = state.correctCount,
                    wrong      = state.wrongCount,
                    xp         = state.earnedXp,
                    error      = state.error,
                    wrongWords = state.wrongWords,
                    hasNextSet = viewModel.nextSetId != null,
                    onRestart  = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.restart()
                    },
                    onNextSet  = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.startNextSet()
                    },
                    onExit     = { navController.popBackStack() }
                )
                else             -> SessionBody(
                    state          = state,
                    onFlip         = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.flip()
                    },
                    onSpeak        = { viewModel.speakCurrent() },
                    onSpeakExample = viewModel::speakExample,
                    onAnswer       = { button ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.answer(button)
                    },
                    onUndo         = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.undo()
                    }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  LOADING
// ═══════════════════════════════════════════════════════════════

@Composable
private fun LoadingBody() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(strokeWidth = 3.dp, color = BrandOrange)
    }
}

// ═══════════════════════════════════════════════════════════════
//  COMPLETION SCREEN
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SessionCompleteBody(
    total: Int,
    correct: Int,
    wrong: Int,
    xp: Int,
    error: String?,
    wrongWords: List<WordEntity>,
    hasNextSet: Boolean,
    onRestart: () -> Unit,
    onNextSet: () -> Unit,
    onExit: () -> Unit
) {
    // v1.26.1 FIX (audit): звёзды по шкале VM (90/70/50) управляют тоном финала —
    // «Сессия завершена! 🎉» + золото на 0% выглядели насмешкой.
    val accuracy = (if (total > 0) correct * 100 / total else 0).coerceIn(0, 100)
    val stars = when {
        accuracy >= 90 -> 3
        accuracy >= 70 -> 2
        accuracy >= 50 -> 1
        else           -> 0
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // ── Orange header strip — matches HomeScreen / SetRow style ──
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(BrandOrange, BrandOrange2))
                    )
                    .padding(vertical = 22.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    when {
                        // v1.26.1 FIX (audit): ошибка — её текст и есть заголовок,
                        // без ложного «Сессия завершена».
                        error != null -> error
                        stars >= 1    -> stringResource(com.spanishapp.R.string.flashcards_session_complete)
                        // v1.26.1 FIX (audit): 0 звёзд — без 🎉.
                        else          -> "Сет пройден — нужно повторение"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }

        // ── Results + buttons ────────────────────────────────────────
        item {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (error != null) {
                    // v1.26.1 FIX (audit): тупик ошибки — единственный CTA «Назад».
                    // «Ещё раз» скрыт: restart зациклил бы тот же падающий запрос.
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = onExit,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                    ) {
                        Text(stringResource(com.spanishapp.R.string.practice_back), fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(Modifier.height(24.dp))
                    com.spanishapp.ui.components.CompletionBadge(
                        accuracyPercent = accuracy,
                        size = 160.dp,
                        // v1.26.1 FIX (audit): лента «¡COMPLETADO!» только за 100%,
                        // приглушённый тон вместо золота при <50%.
                        showRibbon = accuracy == 100,
                        mutedWhenLow = true
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "$correct правильных из $total  ·  +$xp XP",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (wrong > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(com.spanishapp.R.string.flashcards_need_to_repeat_template, wrong.toString()),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(Modifier.height(28.dp))
                    if (hasNextSet) {
                        Button(
                            onClick = onNextSet,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                        ) {
                            Text(stringResource(com.spanishapp.R.string.flashcards_next_set), fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onRestart,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(stringResource(com.spanishapp.R.string.flashcards_repeat_set))
                        }
                    } else {
                        Button(
                            onClick = onRestart,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                        ) {
                            Text(stringResource(com.spanishapp.R.string.flashcards_again), fontWeight = FontWeight.Bold)
                        }
                    }
                    TextButton(onClick = onExit) {
                        Text(
                            stringResource(com.spanishapp.R.string.flashcards_back_to_cards),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ── Mistake review list ──────────────────────────────────────
        if (wrongWords.isNotEmpty()) {
            item {
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔁", fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(com.spanishapp.R.string.flashcards_need_to_repeat_count_template, wrongWords.size),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
            items(wrongWords) { word ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                word.spanish,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(word.russian, fontSize = 15.sp)
                        }
                        if (word.example.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                word.example,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  SESSION BODY  (card + progress + action buttons)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SessionBody(
    state: FlashcardsUiState,
    onFlip: () -> Unit,
    onSpeak: () -> Unit,
    onSpeakExample: () -> Unit,
    onAnswer: (ReviewButton) -> Unit,
    onUndo: () -> Unit
) {
    val word = state.cards.getOrNull(state.currentIndex) ?: return
    val cardHaptic = com.spanishapp.ui.components.rememberCheckedHaptic()

    // Auto-play Spanish pronunciation when card flips to back.
    LaunchedEffect(state.showBack, state.currentIndex) {
        if (state.showBack) onSpeak()
    }

    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val swipeThreshold = with(LocalDensity.current) { 80.dp.toPx() }

    // v1.26.1 FIX (audit): актуальный showBack внутри gesture-корутины —
    // pointerInput перезапускается только по currentIndex, и без этого свайп
    // читал бы устаревший снапшот state после переворота карточки.
    val showBackNow by rememberUpdatedState(state.showBack)

    LaunchedEffect(state.currentIndex) {
        offsetX.snapTo(0f)
        offsetY.snapTo(0f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Orange progress bar ───────────────────────────────────
        // v1.26.1 FIX (audit): доля от замороженной колоды (deckSize) — прогресс
        // не откатывается и знаменатель не растёт при requeue. Дублирующий
        // счётчик у бара удалён — каноничный живёт в топ-баре (1-based).
        Spacer(Modifier.height(8.dp))
        val progress by animateFloatAsState(
            targetValue = if (state.deckSize > 0)
                (state.currentIndex.toFloat() / state.deckSize).coerceIn(0f, 1f) else 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "progress"
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = BrandOrange,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        // ── Swipeable card area ───────────────────────────────────
        // Gesture is detected on the OUTER box (full width/height) so users
        // can start a drag from anywhere on screen, not just the card surface.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(state.currentIndex) {
                    detectDragGestures(
                        onDragEnd = {
                            val xPx = offsetX.value
                            val yPx = offsetY.value
                            val ans = when {
                                xPx >  swipeThreshold -> ReviewButton.GOOD
                                xPx < -swipeThreshold -> ReviewButton.HARD
                                else                  -> null
                            }
                            if (ans != null && !showBackNow) {
                                // v1.26.1 FIX (audit): свайп по НЕоткрытой карточке —
                                // переворот, а не оценка вслепую.
                                scope.launch { offsetX.animateTo(0f, spring()) }
                                scope.launch { offsetY.animateTo(0f, spring()) }
                                onFlip()
                            } else if (ans != null) {
                                scope.launch {
                                    offsetX.animateTo(xPx * 4f, tween(220))
                                    offsetY.animateTo(yPx, tween(220))
                                    onAnswer(ans)
                                }
                            } else {
                                scope.launch { offsetX.animateTo(0f, spring()) }
                                scope.launch { offsetY.animateTo(0f, spring()) }
                            }
                        }
                    ) { change, drag ->
                        change.consume()
                        scope.launch { offsetX.snapTo(offsetX.value + drag.x) }
                        // v1.26.1 FIX (audit): клампим РЕЗУЛЬТАТ, а не приращение —
                        // старый вариант резал drag.y и работал храповиком:
                        // карточка поднималась, но вернуть её вниз было нельзя.
                        scope.launch { offsetY.snapTo((offsetY.value + drag.y).coerceAtMost(0f)) }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.graphicsLayer {
                    translationX = offsetX.value
                    translationY = offsetY.value
                    rotationZ    = (offsetX.value / 30f).coerceIn(-15f, 15f)
                    alpha        = (1f - (kotlin.math.abs(offsetX.value) / 800f)).coerceIn(0.4f, 1f)
                }
            ) {
                FlipCard(
                    word           = word,
                    direction      = state.currentDirection,
                    showBack       = state.showBack,
                    onFlip         = onFlip,
                    onSpeak        = onSpeak,
                    onSpeakExample = onSpeakExample
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Action buttons (swipe still works in parallel) ────────
        // v1.13.4: на планшете кнопки крупнее (52dp → 72dp, font 14sp → 18sp)
        val isWideActions = com.spanishapp.ui.adaptive.isWideScreen()
        val actionHeight = if (isWideActions) 72.dp else 52.dp
        val actionFont = if (isWideActions) 18.sp else 14.sp
        val actionIcon = if (isWideActions) 22.dp else 15.dp

        // v1.26.1 FIX (audit): Undo вынесен из ряда кнопок в слот фиксированной
        // высоты НАД ними — кнопки больше не прыгают при появлении чипа.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp),
            contentAlignment = Alignment.Center
        ) {
            // top-level overload: внутри Box (вложенного в Column) implicit
            // receiver перехватывал бы ColumnScope.AnimatedVisibility.
            androidx.compose.animation.AnimatedVisibility(visible = state.canUndo) {
                Surface(
                    onClick = onUndo,
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "↩",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            stringResource(com.spanishapp.R.string.flashcards_action_undo),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // v1.26.1 FIX (audit): три градации SM-2 — Забыл / Знаю / Легко.
        // Пока карточка не открыта, любая кнопка ПЕРЕВОРАЧИВАЕТ её, а не
        // оценивает вслепую (grade-while-hidden).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // HARD — outlined, error-red
            OutlinedButton(
                onClick = {
                    if (!state.showBack) {
                        onFlip()
                    } else {
                        cardHaptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onAnswer(ReviewButton.HARD)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(actionHeight),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(
                    1.5.dp,
                    MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    Icons.Default.Close, null,
                    modifier = Modifier.size(actionIcon)
                )
                Spacer(Modifier.width(4.dp))
                Text(stringResource(com.spanishapp.R.string.flashcards_action_forgot), fontWeight = FontWeight.SemiBold, fontSize = actionFont)
            }

            // GOOD — filled green (уверенное «Знаю»)
            Button(
                onClick = {
                    if (!state.showBack) {
                        onFlip()
                    } else {
                        cardHaptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onAnswer(ReviewButton.GOOD)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(actionHeight),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardBackAccent)
            ) {
                Text(stringResource(com.spanishapp.R.string.flashcards_action_know), fontWeight = FontWeight.Bold, fontSize = actionFont)
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Default.Check, null,
                    modifier = Modifier.size(15.dp)
                )
            }

            // EASY — amber, третья градация (interval × EF + бонус, +10 XP)
            Button(
                onClick = {
                    if (!state.showBack) {
                        onFlip()
                    } else {
                        cardHaptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onAnswer(ReviewButton.EASY)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(actionHeight),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EasyAmber,
                    contentColor = Color(0xFF3E2723)
                )
            ) {
                Text("Легко", fontWeight = FontWeight.Bold, fontSize = actionFont)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  CARD  (flip animation wrapper)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun FlipCard(
    word: WordEntity,
    direction: FlashcardDirection,
    showBack: Boolean,
    onFlip: () -> Unit,
    onSpeak: () -> Unit,
    onSpeakExample: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (showBack) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "flip"
    )

    // v1.13.4: на планшете карточка квадратная (cap 560dp + aspectRatio 1.2),
    // а не растянутый прямоугольник 3:1. Юзер: "Карточки стали
    // прямоугольными, а должен быть квадрат."
    val isWide = com.spanishapp.ui.adaptive.isWideScreen()
    val cardModifier = if (isWide) {
        Modifier
            .widthIn(max = 560.dp)
            .fillMaxWidth()
            .aspectRatio(1.2f)
    } else {
        Modifier
            .fillMaxWidth()
            // v1.26.1 FIX (audit): фикс-высота 400dp переполняла компактные
            // экраны — берём доступное место, но не больше 400dp (на высоких
            // экранах картинка не меняется).
            .heightIn(max = 400.dp)
            .fillMaxHeight()
    }
    Box(
        modifier = cardModifier
            .graphicsLayer {
                rotationY    = rotation
                cameraDistance = 12f * density
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onFlip
            )
    ) {
        if (rotation <= 90f) {
            CardSurface(accent = BrandOrange) {
                CardFront(word, direction, onSpeak)
            }
        } else {
            CardSurface(
                accent   = CardBackAccent,
                modifier = Modifier.graphicsLayer { rotationY = 180f }
            ) {
                CardBack(word, direction, onSpeak, onSpeakExample)
            }
        }
    }
}

// ── Shared surface shell — three-layer bento tile formula ─────
//  Layer 0 (base):  surfaceContainerHigh — exact shade of HomeScreen bento tiles
//  Layer 1 (glow):  radial gradient from accent, anchored top-right
//  Layer 2 (stripe): 5dp left accent bar — same as course / game cards
//  Layer 3 (art):   Canvas card-stack watermark (bottom-right, 0.12 alpha)

@Composable
private fun CardSurface(
    accent: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier        = modifier.fillMaxSize(),
        shape           = RoundedCornerShape(22.dp),
        shadowElevation = 6.dp,
        color           = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Box(Modifier.fillMaxSize()) {
            // Layer 1 — soft radial glow, top-right corner
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = 0.18f), Color.Transparent),
                            center = Offset(Float.POSITIVE_INFINITY, 0f),
                            radius = 480f
                        )
                    )
            )
            // Layer 2 — left accent stripe
            Box(
                Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp))
                    .background(accent)
            )
            // Layer 3 — thematic watermark (card-stack illustration)
            ThematicWatermark(
                theme  = WatermarkTheme.FLASHCARD_SET,
                accent = accent
            )
            // Content on top of all layers
            content()
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  CARD FRONT  (question side)
//  Pattern: small accent chip  +  emoji  +  large word  +  dim hint.
//  Same language as HomeScreen "Слово дня" / course tiles.
// ═══════════════════════════════════════════════════════════════

@Composable
private fun CardFront(
    word: WordEntity,
    direction: FlashcardDirection,
    onSpeak: () -> Unit
) {
    val frontText = when (direction) {
        FlashcardDirection.ES_TO_RU -> word.spanish
        FlashcardDirection.RU_TO_ES -> word.russian
        FlashcardDirection.MIXED    -> word.spanish
    }
    val mixedLabel = stringResource(com.spanishapp.R.string.flashcards_direction_mixed)
    val directionLabel = when (direction) {
        FlashcardDirection.ES_TO_RU -> "ES → RU"
        FlashcardDirection.RU_TO_ES -> "RU → ES"
        FlashcardDirection.MIXED    -> mixedLabel
    }
    val emoji = remember(word.spanish) {
        val cleaned = word.spanish
            .replace(Regex("^(el|la|los|las|un|una)\\s+", RegexOption.IGNORE_CASE), "")
            .trim()
        com.spanishapp.ui.games.WordEmoji.get(cleaned)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Bento-tile label chip — ExtraBold + letter-spacing ───────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = BrandOrange.copy(alpha = 0.18f)
            ) {
                Text(
                    directionLabel.uppercase(),
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    color         = BrandOrange,
                    modifier      = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            // v1.26.1 FIX (audit): если на фронте испанский (ES→RU / MIXED) —
            // даём прослушать слово ДО ответа (кнопка в стиле задней стороны).
            // Для RU→ES не показываем: испанского на фронте нет.
            if (direction != FlashcardDirection.RU_TO_ES) {
                IconButton(
                    onClick  = onSpeak,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.VolumeUp, null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ── Main content — centered in remaining space ─────────────
        Box(
            modifier         = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Emoji as a decorative visual hook — no coloured background
                if (emoji != null) {
                    Text(emoji, fontSize = 56.sp)
                    Spacer(Modifier.height(12.dp))
                }
                Text(
                    frontText,
                    style      = if (frontText.length > 14)
                        MaterialTheme.typography.headlineLarge
                    else
                        MaterialTheme.typography.displayMedium,
                    fontWeight    = FontWeight.ExtraBold,
                    textAlign     = TextAlign.Center,
                    color         = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // ── Dim bottom hint ────────────────────────────────────────
        // v1.26.1 FIX (audit): подсказка была почти невидима (alpha 0.35) —
        // новички не понимали, что карточку нужно тапнуть.
        Text(
            stringResource(com.spanishapp.R.string.flashcards_tap_to_check),
            style         = MaterialTheme.typography.labelSmall,
            fontSize      = 12.sp,
            color         = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
            letterSpacing = 1.sp
        )
    }
}

// ═══════════════════════════════════════════════════════════════
//  CARD BACK  (answer side)
//  Header row: Spanish + speaker.  Divider.  Answer centered.
//  Example in a subtle surface pill — no coloured blocks.
// ═══════════════════════════════════════════════════════════════

@Composable
private fun CardBack(
    word: WordEntity,
    direction: FlashcardDirection,
    onSpeak: () -> Unit,
    onSpeakExample: () -> Unit
) {
    val answerText = when (direction) {
        FlashcardDirection.ES_TO_RU -> word.russian
        FlashcardDirection.RU_TO_ES -> word.spanish
        FlashcardDirection.MIXED    -> word.russian
    }
    val primary = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // ── Header: Spanish word + speaker button ─────────────────
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                word.spanish,
                style      = MaterialTheme.typography.titleMedium,
                color      = primary,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.weight(1f)
            )
            IconButton(
                onClick  = onSpeak,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.VolumeUp, null,
                    tint     = primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        HorizontalDivider(
            color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            thickness = 1.dp
        )

        // ── Answer — centered in the remaining space ───────────────
        Box(
            modifier         = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                answerText,
                style      = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign  = TextAlign.Center,
                color      = MaterialTheme.colorScheme.onSurface
            )
        }

        // ── Example pill — subtle, no heavy border ─────────────────
        if (word.example.isNotBlank()) {
            Surface(
                shape    = RoundedCornerShape(10.dp),
                color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier          = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "\"${word.example}\"",
                        style     = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        modifier  = Modifier.weight(1f),
                        color     = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick  = onSpeakExample,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.VolumeUp, null,
                            tint     = primary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}
