package com.spanishapp.ui.flashcards

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.spanishapp.domain.algorithm.ReviewButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    navController: NavHostController,
    level: String,
    category: String,
    direction: FlashcardDirection,
    onlyWeak: Boolean,
    viewModel: FlashcardsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(level, category, direction, onlyWeak) {
        viewModel.startSession(level, category, direction, onlyWeak)
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
    val composition by rememberLottieComposition(LottieCompositionSpec.Url("https://lottie.host/7ca331be-49c0-4822-835f-1481b4737f7a/2A8XlF5X9r.json"))
    val progress by animateLottieCompositionAsState(composition)

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
            // Анимированная иконка триумфа
            Box(
                modifier = Modifier
                    .size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Отличная работа!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(32.dp))

            ResultRow("Всего карточек", total.toString(), Icons.Default.Style)
            ResultRow("Правильно", correct.toString(), Icons.Default.CheckCircle, MaterialTheme.colorScheme.primary)
            ResultRow("Нужно повторить", wrong.toString(), Icons.Default.Error, MaterialTheme.colorScheme.error)
            ResultRow("Получено опыта", "+$xp XP", Icons.Default.Stars, MaterialTheme.colorScheme.tertiary)
        }

        Spacer(Modifier.height(48.dp))

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
            Text("Вернуться на главную", color = MaterialTheme.colorScheme.onSurfaceVariant)
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

        FlipCard(
            word = word,
            direction = state.currentDirection,
            showBack = state.showBack,
            onFlip = onFlip,
            onSpeak = onSpeak,
            onSpeakExample = onSpeakExample
        )

        Spacer(Modifier.weight(1f))

        AnimatedContent(
            targetState = state.showBack,
            transitionSpec = {
                (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
            },
            label = "controls"
        ) { isShowingBack ->
            if (!isShowingBack) {
                Button(
                    onClick = onFlip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Показать перевод", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RatingAction(
                        text = "Забыл",
                        icon = Icons.Default.Close,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    ) { onAnswer(ReviewButton.HARD) }

                    RatingAction(
                        text = "Помню",
                        icon = Icons.Default.Check,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    ) { onAnswer(ReviewButton.GOOD) }

                    RatingAction(
                        text = "Легко",
                        icon = Icons.Default.AutoAwesome,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.weight(1f)
                    ) { onAnswer(ReviewButton.EASY) }
                }
            }
        }
        
        Spacer(Modifier.height(20.dp))
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
        tonalElevation = 8.dp,
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface,
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
    
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            frontText,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            "НАЖМИ, ЧТОБЫ ПЕРЕВЕРНУТЬ",
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
