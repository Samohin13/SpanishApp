package com.spanishapp.ui.home

import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Mic
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.core.content.ContextCompat
import com.spanishapp.service.SpanishSpeechRecognizer
import com.spanishapp.ui.components.SpeakerButton
import com.spanishapp.ui.components.VoiceInstallPromptHost
import com.spanishapp.ui.components.inferSpeakText
import com.spanishapp.ui.components.rememberSpanishTts
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Green  = Color(0xFF4CAF50)
private val Red    = Color(0xFFF44336)
private val Purple = Color(0xFF7C4DFF)
private val Orange = Color(0xFFFF9800)

@EntryPoint
@InstallIn(SingletonComponent::class)
private interface SpeechRecognizerEntryPoint {
    fun spanishSpeechRecognizer(): SpanishSpeechRecognizer
}

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

    val totalSteps     = sections.size + exercises.size + 1
    var stepIndex      by remember { mutableStateOf(0) }
    var xpEarned       by remember { mutableStateOf(0) }
    var correctCount   by remember { mutableStateOf(0) }
    var comboCount     by remember { mutableStateOf(0) }
    var bestCombo      by remember { mutableStateOf(0) }
    var showQuitDialog by remember { mutableStateOf(false) }

    // TTS — инициализируется один раз для всего экрана
    val tts = rememberSpanishTts()

    // Один раз показывает диалог установки HD-пакета, если он не установлен
    VoiceInstallPromptHost(tts)

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
            // ─── Прогресс-бар + комбо ──────────────────────────────────────
            if (currentStep !is SessionStep.Victory) {
                SessionTopBar(
                    progress    = stepIndex.toFloat() / (totalSteps - 1).coerceAtLeast(1),
                    accentColor = accentColor,
                    comboCount  = comboCount,
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
                            tts         = tts,
                            onNext      = { stepIndex++ }
                        )
                    }
                    is SessionStep.ExerciseStep -> {
                        ExerciseCard(
                            exercise    = exercises[step.index],
                            accentColor = accentColor,
                            comboCount  = comboCount,
                            tts         = tts,
                            onCorrect   = {
                                correctCount++
                                xpEarned += 10
                                comboCount++
                                if (comboCount > bestCombo) bestCombo = comboCount
                                stepIndex++
                            },
                            onWrong     = {
                                comboCount = 0
                                stepIndex++
                            }
                        )
                    }
                    SessionStep.Victory -> {
                        val nextLessonIndex = lessonIndex + 1
                        val hasNextInUnit   = nextLessonIndex < (unit.lessons.size)
                        val nextUnitId      = unitId + 1
                        val hasNextUnit     = RoadmapData.units.getOrNull(nextUnitId - 1) != null

                        VictoryScreen(
                            lessonTitle    = lesson.title,
                            xpEarned       = xpEarned + 15,
                            correctCount   = correctCount,
                            totalExercises = exercises.size,
                            bestCombo      = bestCombo,
                            accentColor    = accentColor,
                            hasNextLesson  = hasNextInUnit || hasNextUnit,
                            onNextLesson   = {
                                viewModel.markLessonComplete(unitId, lessonIndex)
                                if (hasNextInUnit) {
                                    navController.navigate("lesson_intro/$unitId/$nextLessonIndex") {
                                        popUpTo("lesson_session/$unitId/$lessonIndex") { inclusive = true }
                                    }
                                } else if (hasNextUnit) {
                                    navController.navigate("lesson_intro/$nextUnitId/0") {
                                        popUpTo("lesson_session/$unitId/$lessonIndex") { inclusive = true }
                                    }
                                } else {
                                    navController.popBackStack()
                                }
                            },
                            onFinish = {
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

// ─── Топ-бар с прогрессом + комбо-бейдж ────────────────────────────────────
@Composable
private fun SessionTopBar(
    progress: Float,
    accentColor: Color,
    comboCount: Int,
    onClose: () -> Unit
) {
    val animProgress by animateFloatAsState(
        targetValue    = progress,
        animationSpec  = tween(400),
        label          = "progress"
    )
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Закрыть",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(8.dp))
            LinearProgressIndicator(
                progress   = { animProgress },
                modifier   = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color      = accentColor,
                trackColor = accentColor.copy(alpha = 0.15f)
            )
            Spacer(Modifier.width(8.dp))
            // Слот для комбо-бейджа справа
            AnimatedContent(
                targetState = comboCount,
                transitionSpec = { fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut() },
                label = "combo"
            ) { combo ->
                if (combo >= 2) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Orange.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text     = "🔥 $combo",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color    = Orange
                        )
                    }
                } else {
                    Box(Modifier.size(32.dp))
                }
            }
        }
    }
}

// ─── Карточка теории ───────────────────────────────────────────────────────
@Composable
private fun TheoryCard(
    section: LessonSection,
    intro: String?,
    accentColor: Color,
    tts: TextToSpeech?,
    onNext: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(16.dp))

        if (intro != null) {
            Surface(
                shape    = RoundedCornerShape(16.dp),
                color    = accentColor.copy(alpha = 0.08f),
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
        Spacer(Modifier.height(12.dp))

        section.items.forEach { item ->
            Surface(
                shape    = RoundedCornerShape(14.dp),
                color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Кнопка озвучки испанского слова
                    SpeakerButton(
                        text = item.left,
                        tts  = tts,
                        tint = accentColor
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text       = item.left,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 17.sp
                        )
                        if (item.note.isNotEmpty()) {
                            Text(
                                text     = item.note,
                                fontSize = 13.sp,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text       = item.right,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 15.sp,
                        color      = accentColor,
                        modifier   = Modifier.padding(end = 8.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick  = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .height(56.dp),
            shape  = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
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
    comboCount: Int,
    tts: TextToSpeech?,
    onCorrect: () -> Unit,
    onWrong: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    var selectedOption by remember { mutableStateOf<String?>(null) }
    var answered       by remember { mutableStateOf(false) }

    // Вспышка при ответе
    var flashColor by remember { mutableStateOf(Color.Transparent) }
    val animFlash by animateColorAsState(
        targetValue   = flashColor,
        animationSpec = tween(300),
        label         = "flash"
    )
    fun checkCorrect(selected: String?) =
        selected?.trim().equals(exercise.correctAnswer.trim(), ignoreCase = true) == true

    LaunchedEffect(answered) {
        if (answered) {
            flashColor = if (checkCorrect(selectedOption)) Green.copy(alpha = 0.08f)
                         else Red.copy(alpha = 0.08f)
            delay(500)
            flashColor = Color.Transparent
        }
    }

    val isCorrectAnswer = checkCorrect(selectedOption)

    Box(
        Modifier
            .fillMaxSize()
            .background(animFlash)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(20.dp))

            // Инструкция
            Text(
                text       = exercise.instruction,
                fontSize   = 14.sp,
                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(12.dp))

            // Вопрос + кнопка озвучки
            Surface(
                shape    = RoundedCornerShape(20.dp),
                color    = accentColor.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(Modifier.padding(20.dp)) {
                    Text(
                        text       = exercise.question,
                        modifier   = Modifier.fillMaxWidth(),
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign  = TextAlign.Center,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    // Озвучка вопроса — в правом верхнем углу
                    SpeakerButton(
                        text     = exercise.question.replace("___", ""),
                        tts      = tts,
                        tint     = accentColor,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }

            if (exercise.hint.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text     = "💡 ${exercise.hint}",
                    fontSize = 13.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
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
                            !answered && isSelected              -> accentColor.copy(alpha = 0.1f)
                            answered && isCorrect                -> Green.copy(alpha = 0.12f)
                            answered && isSelected && !isCorrect -> Red.copy(alpha = 0.12f)
                            else                                 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        }
                        val borderColor = when {
                            answered && isCorrect                -> Green
                            answered && isSelected && !isCorrect -> Red
                            isSelected                           -> accentColor
                            else                                 -> Color.Transparent
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
                                    inferSpeakText(option)?.let { t ->
                                        tts?.speak(t, TextToSpeech.QUEUE_FLUSH, null, "ans")
                                    }
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

                ExerciseType.FILL_BLANK -> {
                    FillBlankInput(
                        correctAnswer = exercise.correctAnswer,
                        accentColor   = accentColor,
                        answered      = answered,
                        onAnswer      = { typed ->
                            selectedOption = typed
                            answered = true
                            if (typed.trim().equals(exercise.correctAnswer.trim(), ignoreCase = true)) {
                                inferSpeakText(exercise.correctAnswer)?.let { t ->
                                    tts?.speak(t, TextToSpeech.QUEUE_FLUSH, null, "ans")
                                }
                            }
                        }
                    )
                }

                ExerciseType.BUILD_SENTENCE -> {
                    BuildSentenceInput(
                        words         = exercise.words,
                        correctAnswer = exercise.correctAnswer,
                        accentColor   = accentColor,
                        answered      = answered,
                        onAnswer      = { built ->
                            selectedOption = built
                            answered = true
                            if (built.trim().equals(exercise.correctAnswer.trim(), ignoreCase = true)) {
                                inferSpeakText(exercise.correctAnswer)?.let { t ->
                                    tts?.speak(t, TextToSpeech.QUEUE_FLUSH, null, "ans")
                                }
                            }
                        }
                    )
                }

                ExerciseType.TRANSLATE -> {
                    FillBlankInput(
                        correctAnswer = exercise.correctAnswer,
                        accentColor   = accentColor,
                        answered      = answered,
                        onAnswer      = { typed ->
                            selectedOption = typed
                            answered = true
                            inferSpeakText(exercise.correctAnswer)?.let { t ->
                                tts?.speak(t, TextToSpeech.QUEUE_FLUSH, null, "ans")
                            }
                        }
                    )
                }

                ExerciseType.SPEAKING -> {
                    SpeakingInput(
                        wordToSay   = exercise.correctAnswer,
                        accentColor = accentColor,
                        tts         = tts,
                        answered    = answered,
                        onAnswer    = { passed ->
                            selectedOption = if (passed) exercise.correctAnswer else "__failed__"
                            answered = true
                        }
                    )
                }
            }

            // Объяснение
            AnimatedVisibility(
                visible = answered && exercise.explanation.isNotEmpty(),
                enter   = fadeIn() + expandVertically()
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCorrectAnswer) Green.copy(alpha = 0.08f)
                                else Red.copy(alpha = 0.08f)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                            Text(
                                text     = if (isCorrectAnswer) "✅ " else "❌ ",
                                fontSize = 16.sp
                            )
                            Text(
                                text     = exercise.explanation,
                                fontSize = 14.sp,
                                color    = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Кнопка ДАЛЕЕ
            AnimatedVisibility(
                visible = answered,
                enter   = fadeIn() + slideInVertically { it }
            ) {
                Button(
                    onClick  = { if (isCorrectAnswer) onCorrect() else onWrong() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .height(56.dp),
                    shape  = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCorrectAnswer) Green else Red
                    )
                ) {
                    Text(
                        text       = if (isCorrectAnswer) "ДАЛЕЕ →" else "ПОНЯЛ, ДАЛЕЕ →",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            if (!answered) Spacer(Modifier.height(20.dp))
        }
    }
}

// ─── Произнеси слово (SPEAKING) ───────────────────────────────────────────
@Composable
private fun SpeakingInput(
    wordToSay: String,
    accentColor: Color,
    tts: TextToSpeech?,
    answered: Boolean,
    onAnswer: (Boolean) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val recognizer = remember(ctx) {
        EntryPointAccessors.fromApplication(
            ctx.applicationContext,
            SpeechRecognizerEntryPoint::class.java
        ).spanishSpeechRecognizer()
    }

    var isListening  by remember { mutableStateOf(false) }
    var recognized   by remember { mutableStateOf("") }
    var score        by remember { mutableStateOf(0f) }
    var errorMsg     by remember { mutableStateOf("") }

    // Анимация пульсации микрофона
    val infiniteTransition = rememberInfiniteTransition(label = "mic")
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.18f,
        animationSpec = infiniteRepeatable(
            animation  = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micScale"
    )

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch {
                isListening = true
                val result = recognizer.checkPronunciation(wordToSay)
                isListening  = false
                recognized   = result.recognized
                score        = result.score
                errorMsg     = result.error
                onAnswer(result.passed)
            }
        } else {
            errorMsg = "Нет разрешения на микрофон"
        }
    }

    fun startListening() {
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED) {
            scope.launch {
                isListening = true
                val result = recognizer.checkPronunciation(wordToSay)
                isListening  = false
                recognized   = result.recognized
                score        = result.score
                errorMsg     = result.error
                onAnswer(result.passed)
            }
        } else {
            permLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {

        // Слово для произношения
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = accentColor.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎤", fontSize = 28.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text       = wordToSay,
                    fontSize   = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = accentColor
                )
                Spacer(Modifier.height(6.dp))
                // Послушать перед тем как говорить
                SpeakerButton(text = wordToSay, tts = tts, tint = accentColor)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Результат распознавания
        AnimatedVisibility(visible = answered && recognized.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (score >= 0.75f) Green.copy(0.1f) else Red.copy(0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (score >= 0.75f) "Отлично! 👏" else "Почти!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (score >= 0.75f) Green else Red
                    )
                    Text(
                        text = "Распознано: «$recognized»",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (score < 0.75f) {
                        Text(
                            text = "Нужно: «$wordToSay»",
                            fontSize = 14.sp,
                            color = Green,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = errorMsg.isNotEmpty()) {
            Text(errorMsg, color = Red, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(Modifier.height(20.dp))

        // Кнопка микрофона
        if (!answered) {
            Box(contentAlignment = Alignment.Center) {
                if (isListening) {
                    Box(
                        Modifier
                            .size(80.dp)
                            .scale(micScale)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f))
                    )
                }
                IconButton(
                    onClick  = { if (!isListening) startListening() },
                    enabled  = !isListening,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(if (isListening) accentColor else accentColor.copy(0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Произнести",
                        tint = if (isListening) Color.White else accentColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (isListening) "Слушаю..." else "Нажми и произнеси",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Ввод слова (FILL_BLANK / TRANSLATE) ──────────────────────────────────
@Composable
private fun FillBlankInput(
    correctAnswer: String,
    accentColor: Color,
    answered: Boolean,
    onAnswer: (String) -> Unit
) {
    var typed by remember { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current
    val isCorrect = typed.trim().equals(correctAnswer.trim(), ignoreCase = true)

    val borderColor = when {
        !answered -> accentColor
        isCorrect -> Green
        else      -> Red
    }

    OutlinedTextField(
        value         = typed,
        onValueChange = { if (!answered) typed = it },
        enabled       = !answered,
        singleLine    = true,
        placeholder   = { Text("Введи ответ...", color = Color.Gray) },
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(14.dp),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = borderColor,
            unfocusedBorderColor = borderColor.copy(alpha = 0.5f),
            disabledBorderColor  = borderColor,
            disabledTextColor    = MaterialTheme.colorScheme.onSurface
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            if (typed.isNotBlank() && !answered) {
                keyboard?.hide()
                onAnswer(typed.trim())
            }
        }),
        trailingIcon = {
            if (answered) {
                Text(
                    if (isCorrect) "✓" else "✗",
                    color = if (isCorrect) Green else Red,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        }
    )

    // Показываем правильный ответ если ошибся
    AnimatedVisibility(visible = answered && !isCorrect) {
        Text(
            text = "Правильно: $correctAnswer",
            color = Green,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 6.dp, start = 4.dp)
        )
    }

    if (!answered) {
        Spacer(Modifier.height(12.dp))
        Button(
            onClick  = {
                if (typed.isNotBlank()) {
                    keyboard?.hide()
                    onAnswer(typed.trim())
                }
            },
            enabled  = typed.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text("ПРОВЕРИТЬ", fontWeight = FontWeight.ExtraBold)
        }
    }
}

// ─── Составь предложение из плиток (BUILD_SENTENCE) ───────────────────────
@Composable
private fun BuildSentenceInput(
    words: List<String>,
    correctAnswer: String,
    accentColor: Color,
    answered: Boolean,
    onAnswer: (String) -> Unit
) {
    val shuffled = remember(words) { words.shuffled() }
    val chosen   = remember { mutableStateListOf<String>() }
    val pool     = remember(words) { mutableStateListOf(*shuffled.toTypedArray()) }

    val built     = chosen.joinToString(" ")
    val isCorrect = built.trim().equals(correctAnswer.trim(), ignoreCase = true)

    // Зона собранного предложения
    Surface(
        shape    = RoundedCornerShape(14.dp),
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp)
    ) {
        Box(Modifier.padding(12.dp)) {
            if (chosen.isEmpty()) {
                Text("Нажимай на слова ниже →", color = Color.Gray, fontSize = 14.sp)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(chosen) { word ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accentColor.copy(alpha = 0.15f),
                            modifier = Modifier.clickable(enabled = !answered) {
                                chosen.remove(word)
                                pool.add(word)
                            }
                        ) {
                            Text(
                                text = word,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = accentColor
                            )
                        }
                    }
                }
            }
        }
    }

    Spacer(Modifier.height(10.dp))

    // Пул слов
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(pool.toList()) { word ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .clickable(enabled = !answered) {
                        pool.remove(word)
                        chosen.add(word)
                    }
                    .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            ) {
                Text(
                    text = word,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // Показываем правильный ответ если ошибся
    AnimatedVisibility(visible = answered && !isCorrect) {
        Text(
            text = "Правильно: $correctAnswer",
            color = Green,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
        )
    }

    if (!answered) {
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick  = { chosen.clear(); pool.clear(); pool.addAll(shuffled) },
                modifier = Modifier.height(52.dp),
                shape    = RoundedCornerShape(14.dp)
            ) { Text("↺") }
            Button(
                onClick  = { if (chosen.isNotEmpty()) onAnswer(built) },
                enabled  = chosen.isNotEmpty(),
                modifier = Modifier.weight(1f).height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) { Text("ПРОВЕРИТЬ", fontWeight = FontWeight.ExtraBold) }
        }
    }
}

// ─── Экран победы ──────────────────────────────────────────────────────────
@Composable
private fun VictoryScreen(
    lessonTitle: String,
    xpEarned: Int,
    correctCount: Int,
    totalExercises: Int,
    bestCombo: Int,
    accentColor: Color,
    hasNextLesson: Boolean,
    onNextLesson: () -> Unit,
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

    val accuracy = if (totalExercises > 0)
        (correctCount * 100 / totalExercises) else 100

    // Иконка и сообщение зависят от точности
    val (trophy, verdict) = when {
        accuracy == 100 -> "🏆" to "Идеально!"
        accuracy >= 75  -> "⭐" to "Отличный результат!"
        accuracy >= 50  -> "👍" to "Хорошая работа!"
        else            -> "💪" to "Не сдавайся!"
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(24.dp))

        Box(
            Modifier
                .size(120.dp)
                .background(accentColor.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(trophy, fontSize = 56.sp)
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text       = verdict,
            fontSize   = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color      = accentColor
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text      = lessonTitle,
            fontSize  = 15.sp,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        // Плитки статистики
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatTile(
                value      = "+$displayedXp",
                label      = "XP",
                icon       = "⚡",
                accentColor = accentColor,
                modifier   = Modifier.weight(1f)
            )
            if (totalExercises > 0) {
                StatTile(
                    value      = "$accuracy%",
                    label      = "Точность",
                    icon       = "🎯",
                    accentColor = if (accuracy >= 75) Green else Orange,
                    modifier   = Modifier.weight(1f)
                )
            }
            if (bestCombo >= 2) {
                StatTile(
                    value      = "🔥$bestCombo",
                    label      = "Комбо",
                    icon       = "",
                    accentColor = Orange,
                    modifier   = Modifier.weight(1f)
                )
            } else {
                StatTile(
                    value      = "$correctCount/$totalExercises",
                    label      = "Верно",
                    icon       = "✓",
                    accentColor = accentColor,
                    modifier   = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        if (hasNextLesson) {
            Button(
                onClick  = onNextLesson,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape  = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("СЛЕДУЮЩИЙ УРОК →", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(12.dp))
        }

        OutlinedButton(
            onClick  = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape  = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text(
                text       = if (hasNextLesson) "Выйти в меню" else "ГОТОВО",
                fontSize   = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StatTile(
    value: String,
    label: String,
    icon: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape    = RoundedCornerShape(16.dp),
        color    = accentColor.copy(alpha = 0.10f),
        modifier = modifier
    ) {
        Column(
            Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon.isNotEmpty()) Text(icon, fontSize = 18.sp)
            Text(
                text       = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 20.sp,
                color      = accentColor
            )
            Text(
                text      = label,
                fontSize  = 11.sp,
                color     = accentColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
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
