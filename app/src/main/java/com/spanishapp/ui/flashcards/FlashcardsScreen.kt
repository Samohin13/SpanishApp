package com.spanishapp.ui.flashcards

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import com.spanishapp.data.db.entity.WordEntity
import com.spanishapp.domain.algorithm.LeaguePromotion
import com.spanishapp.domain.algorithm.ReviewButton
import com.spanishapp.ui.components.LeaguePromotionDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    navController: NavHostController,
    level: String,
    category: String,
    direction: FlashcardDirection,
    setId: String? = null,
    viewModel: FlashcardsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = com.spanishapp.ui.components.rememberCheckedHaptic()
    val sound = com.spanishapp.ui.components.rememberAnswerSound()
    com.spanishapp.ui.components.TrackStudyMinutes()

    var leaguePromotion by remember { mutableStateOf<LeaguePromotion?>(null) }
    LaunchedEffect(viewModel) {
        viewModel.leaguePromotions.collect { leaguePromotion = it }
    }
    leaguePromotion?.let { promo ->
        LeaguePromotionDialog(from = promo.from, to = promo.to, onDismiss = { leaguePromotion = null })
    }

    LaunchedEffect(level, category, direction, setId) {
        if (setId != null) {
            viewModel.startSetSession(setId, direction)
        } else {
            viewModel.startSession(level, category, direction)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Изучение слов",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val pos = (state.currentIndex).coerceAtMost(state.sessionSize)
                        Text(
                            "$pos из ${state.sessionSize}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, null)
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
                state.isLoading -> LoadingBody()
                state.isFinished -> SessionCompleteBody(
                    total = state.sessionSize,
                    correct = state.correctCount,
                    wrong = state.wrongCount,
                    xp = state.earnedXp,
                    error = state.error,
                    onRestart = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.restart()
                    },
                    onExit = { navController.popBackStack() }
                )
                else -> SessionBody(
                    state = state,
                    onFlip = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.flip()
                    },
                    onSpeak = { viewModel.speakCurrent() },
                    onSpeakExample = viewModel::speakExample,
                    onAnswer = { button ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        when (button) {
                            ReviewButton.GOOD, ReviewButton.EASY -> sound.correct()
                            ReviewButton.HARD -> sound.wrong()
                        }
                        viewModel.answer(button)
                    }
                )
            }
        }
    }
}

@Composable
private fun LoadingBody() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(strokeWidth = 3.dp)
    }
}

@Composable
private fun SessionCompleteBody(
    total: Int,
    correct: Int,
    wrong: Int,
    xp: Int,
    error: String?,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (error != null) {
            Text(error, style = MaterialTheme.typography.bodyLarge)
        } else {
            val accuracy = if (total > 0) (correct * 100 / total) else 0
            Text("Сессия завершена!", fontWeight = FontWeight.SemiBold, fontSize = 24.sp)
            Spacer(Modifier.height(20.dp))
            com.spanishapp.ui.components.CompletionBadge(
                accuracyPercent = accuracy,
                size = 180.dp
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "$correct правильных из $total  ·  +$xp XP",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Нужно повторить: $wrong",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onRestart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Ещё раз", fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = onExit) {
            Text("Назад", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String, icon: ImageVector, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun SessionBody(
    state: FlashcardsUiState,
    onFlip: () -> Unit,
    onSpeak: () -> Unit,
    onSpeakExample: () -> Unit,
    onAnswer: (ReviewButton) -> Unit
) {
    val word = state.cards.getOrNull(state.currentIndex) ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val progress by animateFloatAsState(
            targetValue = if (state.sessionSize > 0) state.currentIndex.toFloat() / state.sessionSize else 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "progress"
        )
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        Spacer(Modifier.weight(0.5f))

        // Swipe-жесты на перевернутой карточке:
        //   →  GOOD ("Помню")
        //   ←  HARD ("Забыл")
        //   ↑  EASY ("Легко")
        val scope = rememberCoroutineScope()
        val offsetX = remember { Animatable(0f) }
        val offsetY = remember { Animatable(0f) }
        // Lower threshold (was 120dp) so even small drags from the edge trigger
        // an answer — important for one-handed thumb reach.
        val swipeThreshold = with(LocalDensity.current) { 80.dp.toPx() }

        // При смене карточки — мгновенно сбрасываем offset, новая карточка
        // начинает с центра.
        LaunchedEffect(state.currentIndex) {
            offsetX.snapTo(0f)
            offsetY.snapTo(0f)
        }

        // OUTER wrapper captures swipes across the FULL width/height of the
        // session area — not just the card surface. Lets one-handed users start
        // a drag from anywhere on the screen edges and still trigger the action,
        // mirror-comfortable for both right- and left-handed grips.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2.5f)
                .pointerInput(state.currentIndex) {
                    detectDragGestures(
                        onDragEnd = {
                            val xPx = offsetX.value
                            val yPx = offsetY.value
                            val ans = when {
                                yPx < -swipeThreshold              -> ReviewButton.EASY
                                xPx >  swipeThreshold              -> ReviewButton.GOOD
                                xPx < -swipeThreshold              -> ReviewButton.HARD
                                else                               -> null
                            }
                            if (ans != null) {
                                scope.launch {
                                    val targetX = if (ans == ReviewButton.EASY) xPx else xPx * 4f
                                    val targetY = if (ans == ReviewButton.EASY) -1500f else yPx
                                    offsetX.animateTo(targetX, tween(220))
                                    offsetY.animateTo(targetY, tween(220))
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
                        scope.launch { offsetY.snapTo(offsetY.value + drag.y.coerceAtMost(0f)) }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // INNER box only carries the visual transform — keeps card visually
            // attached to the finger while the gesture is read from the OUTER area.
            Box(
                modifier = Modifier.graphicsLayer {
                    translationX = offsetX.value
                    translationY = offsetY.value
                    rotationZ = (offsetX.value / 30f).coerceIn(-15f, 15f)
                    alpha = (1f - (kotlin.math.abs(offsetX.value) / 800f)).coerceIn(0.4f, 1f)
                }
            ) {
                FlipCard(
                    word = word,
                    direction = state.currentDirection,
                    showBack = state.showBack,
                    onFlip = onFlip,
                    onSpeak = onSpeak,
                    onSpeakExample = onSpeakExample
                )
            }
        }

        // Permanent swipe legend — works on both front and back of the card.
        // User can tap to flip & peek, but the primary interaction is the swipe.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, top = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SwipeLegend("←", "Забыл",  MaterialTheme.colorScheme.error)
            SwipeLegend("↑", "Легко",  MaterialTheme.colorScheme.tertiary)
            SwipeLegend("→", "Помню", MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SwipeLegend(arrow: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(arrow, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
        Text(
            label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RatingAction(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = contentColor)
            Spacer(Modifier.height(4.dp))
            Text(text, color = contentColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        }
    }
}

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
            stiffness = Spring.StiffnessLow
        ),
        label = "flip"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onFlip
            )
    ) {
        if (rotation <= 90f) {
            CardSurface {
                CardFront(word, direction, onSpeak)
            }
        } else {
            CardSurface(
                modifier = Modifier.graphicsLayer { rotationY = 180f }
            ) {
                CardBack(word, direction, onSpeak, onSpeakExample)
            }
        }
    }
}

@Composable
private fun CardSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(32.dp),
        // tonalElevation overlays primary tint = brown in M3 dark. Use shadow only.
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        content()
    }
}

@Composable
private fun CardFront(
    word: WordEntity,
    direction: FlashcardDirection,
    onSpeak: () -> Unit
) {
    val frontText = when (direction) {
        FlashcardDirection.ES_TO_RU -> word.spanish
        FlashcardDirection.RU_TO_ES -> word.russian
        FlashcardDirection.MIXED -> word.spanish
    }
    // Strip article so emoji lookup matches ("el gato" → "gato").
    val emoji = remember(word.spanish) {
        val cleaned = word.spanish
            .replace(Regex("^(el|la|los|las|un|una)\\s+", RegexOption.IGNORE_CASE), "")
            .trim()
        com.spanishapp.ui.games.WordEmoji.get(cleaned)
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Big illustrative emoji (when known) — same approach Tobo uses,
            // gives the word a visual hook without requiring per-word PNGs.
            if (emoji != null) {
                Text(emoji, fontSize = 96.sp)
            }
            Text(
                frontText,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            "НАЖМИ, ЧТОБЫ ПРОВЕРИТЬ",
            modifier = Modifier.align(Alignment.BottomCenter),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            letterSpacing = 1.sp
        )
    }
}

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
        FlashcardDirection.MIXED -> word.russian
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            word.spanish,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            answerText,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        if (word.example.isNotBlank()) {
            Spacer(Modifier.height(24.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "“${word.example}”",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onSpeakExample) {
                        Icon(Icons.Default.VolumeUp, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        
        FilledIconButton(
            onClick = onSpeak,
            modifier = Modifier.size(56.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Icon(Icons.Default.VolumeUp, null)
        }
    }
}
