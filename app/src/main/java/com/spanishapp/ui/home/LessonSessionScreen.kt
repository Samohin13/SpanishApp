package com.spanishapp.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay

private val Green  = Color(0xFF4CAF50)
private val Red    = Color(0xFFF44336)
private val Purple = Color(0xFF7C4DFF)

// ─── Шаги сессии ───────────────────────────────────────────────────────────
private sealed class SessionStep {
    data class Theory(val sectionIndex: Int, val total: Int) : SessionStep()
    data class ExerciseStep(val index: Int, val total: Int)  : SessionStep()
    object Victory                                           : SessionStep()
}

@Composable
fun LessonSessionScreen(
    navController: NavHostController,
    unitId: Int,
    lessonIndex: Int,
    viewModel: LessonIntroViewModel
) {
    val unit    = remember(unitId) { RoadmapData.units.getOrNull(unitId - 1) }
    val lesson  = remember(unit, lessonIndex) { unit?.lessons?.getOrNull(lessonIndex) }
    val content = remember(unitId, lessonIndex) { LessonContentData.lessons["u${unitId}_l${lessonIndex}"] }

    if (unit == null || lesson == null || content == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    val accentColor = unit.color
    val sections    = content.sections
    val exercises   = content.exercises

    val totalSteps  = sections.size + exercises.size + 1  // +1 для Victory
    var stepIndex   by remember { mutableStateOf(0) }
    var xpEarned    by remember { mutableStateOf(0) }
    var correctCount by remember { mutableStateOf(0) }
    var showQuitDialog by remember { mutableStateOf(false) }

    // Считаем текущий шаг
    val currentStep: SessionStep = when {
        stepIndex < sections.size ->
            SessionStep.Theory(stepIndex, sections.size)
        stepIndex < sections.size + exercises.size ->
            SessionStep.ExerciseStep(stepIndex - sections.size, exercises.size)
        else ->
            SessionStep.Victory
    }

    BackHandler { showQuitDialog = true }

    if (showQuitDialog) {
        QuitDialog(
            onQuit   = { navController.popBackStack() },
            onResume = { showQuitDialog = false }
        )
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ─── Прогресс-бар ───────────────────────────────────────────
            if (currentStep !is SessionStep.Victory) {
                SessionTopBar(
                    progress    = stepIndex.toFloat() / (totalSteps - 1).coerceAtLeast(1),
                    accentColor = accentColor,
                    onClose     = { showQuitDialog = true }
                )
            }

            // ─── Контент шага ────────────────────────────────────────────
            AnimatedContent(
                targetState = stepIndex,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it } + fadeOut()
                },
                label = "step"
            ) { idx ->
                val step = when {
                    idx < sections.size ->
                        SessionStep.Theory(idx, sections.size)
                    idx < sections.size + exercises.size ->
                        SessionStep.ExerciseStep(idx - sections.size, exercises.size)
                    else ->
                        SessionStep.Victory
                }

                when (step) {
                    is SessionStep.Theory -> {
                        TheoryCard(
                            section     = sections[step.sectionIndex],
                            intro       = if (step.sectionIndex == 0) content.intro else null,
                            accentColor = accentColor,
                            onNext      = { stepIndex++ }
                        )
                    }
                    is SessionStep.ExerciseStep -> {
                        ExerciseCard(
                            exercise    = exercises[step.index],
                            accentColor = accentColor,
                            onCorrect   = {
                                correctCount++
                                xpEarned += 10
                                stepIndex++
                            },
                            onWrong     = {
                                stepIndex++
                            }
                        )
                    }
                    SessionStep.Victory -> {
                        VictoryScreen(
                            lessonTitle  = lesson.title,
                            xpEarned     = xpEarned + 15,
                            correctCount = correctCount,
                            totalExercises = exercises.size,
                            accentColor  = accentColor,
                            onFinish     = {
                                viewModel.markLessonComplete(unitId, lessonIndex)
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}

// ─── Топ-бар с прогрессом ──────────────────────────────────────────────────
@Composable
private fun SessionTopBar(
    progress: Float,
    accentColor: Color,
    onClose: () -> Unit
) {
    val animProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(400),
        label = "progress"
    )
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(8.dp))
        LinearProgressIndicator(
            progress    = { animProgress },
            modifier    = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(6.dp)),
            color       = accentColor,
            trackColor  = accentColor.copy(alpha = 0.15f)
        )
        Spacer(Modifier.width(8.dp))
        Box(Modifier.size(32.dp))
    }
}

// ─── Карточка теории ───────────────────────────────────────────────────────
@Composable
private fun TheoryCard(
    section: LessonSection,
    intro: String?,
    accentColor: Color,
    onNext: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Spacer(Modifier.height(16.dp))

            if (intro != null) {
                Surface(
                    shape  = RoundedCornerShape(16.dp),
                    color  = accentColor.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text     = intro,
                        modifier = Modifier.padding(16.dp),
                        style    = MaterialTheme.typography.bodyLarge,
                        color    = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            Text(
                text       = section.heading,
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 22.sp,
                color      = accentColor
            )
            Spacer(Modifier.height(16.dp))

            section.items.forEach { item ->
                Surface(
                    shape    = RoundedCornerShape(14.dp),
                    color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text       = item.left,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 17.sp
                            )
                            if (item.note.isNotEmpty()) {
                                Text(
                                    text  = item.note,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text       = item.right,
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 16.sp,
                            color      = accentColor
                        )
                    }
                }
            }
        }

        Button(
            onClick  = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
                .height(56.dp),
            shape    = RoundedCornerShape(16.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text("ПОНЯТНО!", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

// ─── Карточка упражнения ───────────────────────────────────────────────────
@Composable
private fun ExerciseCard(
    exercise: Exercise,
    accentColor: Color,
    onCorrect: () -> Unit,
    onWrong: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // null = не ответил, true = верно, false = неверно
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var answered       by remember { mutableStateOf(false) }

    LaunchedEffect(selectedOption) {
        if (selectedOption != null) {
            delay(900)
            if (selectedOption == exercise.correctAnswer) onCorrect() else onWrong()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Spacer(Modifier.height(20.dp))

            // Инструкция
            Text(
                text     = exercise.instruction,
                fontSize = 14.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(12.dp))

            // Вопрос
            Surface(
                shape    = RoundedCornerShape(20.dp),
                color    = accentColor.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text       = exercise.question,
                    modifier   = Modifier.padding(20.dp),
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign  = TextAlign.Center,
                    color      = MaterialTheme.colorScheme.onSurface
                )
            }

            if (exercise.hint.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = "💡 ${exercise.hint}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(24.dp))

            // Варианты ответа
            when (exercise.type) {
                ExerciseType.MULTIPLE_CHOICE -> {
                    exercise.options.forEach { option ->
                        val isSelected = selectedOption == option
                        val isCorrect  = option == exercise.correctAnswer
                        val bgColor = when {
                            !answered && isSelected -> accentColor.copy(alpha = 0.1f)
                            answered && isCorrect   -> Green.copy(alpha = 0.12f)
                            answered && isSelected && !isCorrect -> Red.copy(alpha = 0.12f)
                            else                   -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        }
                        val borderColor = when {
                            answered && isCorrect              -> Green
                            answered && isSelected && !isCorrect -> Red
                            isSelected                         -> accentColor
                            else                               -> Color.Transparent
                        }

                        Surface(
                            shape    = RoundedCornerShape(14.dp),
                            color    = bgColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .border(
                                    width = if (isSelected || (answered && isCorrect)) 2.dp else 0.dp,
                                    color = borderColor,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable(enabled = !answered) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedOption = option
                                    answered = true
                                }
                        ) {
                            Row(
                                Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text       = option,
                                    fontSize   = 16.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier   = Modifier.weight(1f)
                                )
                                if (answered && isCorrect) {
                                    Text("✓", color = Green, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                } else if (answered && isSelected && !isCorrect) {
                                    Text("✗", color = Red, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
                else -> {
                    // Заглушка для других типов упражнений
                    Text("Упражнение в разработке", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Объяснение после ответа
            if (answered && exercise.explanation.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedOption == exercise.correctAnswer)
                        Green.copy(alpha = 0.08f) else Red.copy(alpha = 0.08f)
                ) {
                    Text(
                        text     = exercise.explanation,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp,
                        color    = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

// ─── Экран победы ──────────────────────────────────────────────────────────
@Composable
private fun VictoryScreen(
    lessonTitle: String,
    xpEarned: Int,
    correctCount: Int,
    totalExercises: Int,
    accentColor: Color,
    onFinish: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    var displayedXp by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val step = (xpEarned / 20).coerceAtLeast(1)
        while (displayedXp < xpEarned) {
            delay(40)
            displayedXp = (displayedXp + step).coerceAtMost(xpEarned)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Звезда / трофей
        Box(
            Modifier
                .size(120.dp)
                .background(accentColor.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("🏆", fontSize = 56.sp)
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text       = "Урок пройден!",
            fontSize   = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color      = accentColor
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text      = lessonTitle,
            fontSize  = 16.sp,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        // Статистика
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("⚡ +$displayedXp", "XP", accentColor)
            if (totalExercises > 0) {
                StatItem("$correctCount/$totalExercises", "Верно", accentColor)
            }
            StatItem("🔓", "Открыт\nурок", accentColor)
        }

        Spacer(Modifier.height(40.dp))

        Button(
            onClick  = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape    = RoundedCornerShape(18.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text("ПРОДОЛЖИТЬ", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, accentColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = accentColor.copy(alpha = 0.1f)
        ) {
            Text(
                text       = value,
                modifier   = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 20.sp,
                color      = accentColor
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text      = label,
            fontSize  = 12.sp,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ─── Диалог выхода ─────────────────────────────────────────────────────────
@Composable
private fun QuitDialog(onQuit: () -> Unit, onResume: () -> Unit) {
    AlertDialog(
        onDismissRequest = onResume,
        title   = { Text("Выйти из урока?", fontWeight = FontWeight.Bold) },
        text    = { Text("Прогресс этого урока не сохранится.") },
        confirmButton = {
            TextButton(onClick = onQuit) {
                Text("Выйти", color = Red, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(onClick = onResume) { Text("Продолжить") }
        }
    )
}
