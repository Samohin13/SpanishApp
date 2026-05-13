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
import androidx.compose.ui.res.stringResource
import com.spanishapp.R
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.spanishapp.ui.components.speakSpanish
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

@EntryPoint
@InstallIn(SingletonComponent::class)
private interface RatingEntryPoint {
    fun ratingUpdater(): com.spanishapp.domain.algorithm.RatingUpdater
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
    com.spanishapp.ui.components.TrackStudyMinutes()
    val unit    = remember(unitId) { RoadmapData.units.getOrNull(unitId - 1) }
    val lesson  = remember(unit, lessonIndex) { unit?.lessons?.getOrNull(lessonIndex) }
    val content = remember(unitId, lessonIndex) { LessonContentData.lessons["u${unitId}_l${lessonIndex}"] }

    if (unit == null || lesson == null || content == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    val accentColor = unit.color
    val sections    = content.sections
    // Authored + auto-generated, interleaved so generated items don't all
    // bunch at the end. Pattern: 1 authored, 1 generated, 1 authored, ...
    val exercises   = remember(unitId, lessonIndex) {
        val lessonKey = "u${unitId}_l${lessonIndex}"
        val generated = ExerciseGenerator.generate(lessonKey, content).toMutableList()
        val authored  = content.exercises.toMutableList()
        val mixed = mutableListOf<Exercise>()
        while (authored.isNotEmpty() || generated.isNotEmpty()) {
            if (authored.isNotEmpty()) mixed += authored.removeAt(0)
            if (generated.isNotEmpty()) mixed += generated.removeAt(0)
        }
        mixed
    }

    val totalSteps     = sections.size + exercises.size + 1
    var stepIndex      by remember { mutableStateOf(0) }
    var xpEarned       by remember { mutableStateOf(0) }
    var correctCount   by remember { mutableStateOf(0) }
    var comboCount     by remember { mutableStateOf(0) }
    var bestCombo      by remember { mutableStateOf(0) }
    var showQuitDialog by remember { mutableStateOf(false) }

    val ctxForRating = LocalContext.current.applicationContext
    val scope        = rememberCoroutineScope()

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
                        // Lesson exercises now feed the rating system the same
                        // way Practice / Flashcards / Games do — without this
                        // hook, completing a lesson granted XP but zero skill
                        // rating, leaving the in-app rating tied only to
                        // standalone activities.
                        val ratingUpdater = remember {
                            EntryPointAccessors.fromApplication(
                                ctxForRating,
                                RatingEntryPoint::class.java
                            ).ratingUpdater()
                        }
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
                                scope.launch {
                                    runCatching { ratingUpdater.applyGameAnswer(true) }
                                }
                                stepIndex++
                            },
                            onWrong     = {
                                comboCount = 0
                                scope.launch {
                                    runCatching { ratingUpdater.applyGameAnswer(false) }
                                }
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
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.ls_close_cd),
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
                            fontSize = 15.sp,
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

        // Section headings use the unit's own colour. We previously forced
        // amber on dark theme everywhere because A1 deep purples got lost
        // against the black background — but A1 is now yellow (already
        // high-contrast), and amber on A2/B1/B2 lessons broke the per-level
        // identity (cyan/green/pink units showed yellow headings).
        val readableAccent = accentColor

        Text(
            text       = section.heading,
            fontWeight = FontWeight.ExtraBold,
            fontSize   = 22.sp,
            color      = readableAccent
        )
        Spacer(Modifier.height(12.dp))

        section.items.forEach { item ->
            Surface(
                shape    = RoundedCornerShape(14.dp),
                color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    // Whole row plays TTS — no need to aim at the speaker icon.
                    .clickable {
                        com.spanishapp.ui.components.inferSpeakText(item.left)?.let { t ->
                            tts?.speakSpanish(t, "item")
                        }
                    }
            ) {
                Row(
                    Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Speaker icon stays as a visual cue; tap is on the whole row
                    SpeakerButton(
                        text = item.left,
                        tts  = tts,
                        tint = readableAccent
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
                                fontSize = 15.sp,
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
            Text(stringResource(R.string.ls_got_it), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
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
    val haptic = com.spanishapp.ui.components.rememberCheckedHaptic()

    var selectedOption by remember { mutableStateOf<String?>(null) }
    var answered       by remember { mutableStateOf(false) }

    // Вспышка при ответе
    var flashColor by remember { mutableStateOf(Color.Transparent) }
    val animFlash by animateColorAsState(
        targetValue   = flashColor,
        animationSpec = tween(300),
        label         = "flash"
    )
    fun checkCorrect(selected: String?): Boolean {
        if (selected == null) return false
        return when (exercise.type) {
            // For ORDER_LETTERS the user assembles letters (no spaces) while
            // the stored correctAnswer may contain spaces — strip both sides.
            ExerciseType.ORDER_LETTERS ->
                selected.replace(" ", "")
                    .equals(exercise.correctAnswer.replace(" ", ""), ignoreCase = true)
            else ->
                selected.trim().equals(exercise.correctAnswer.trim(), ignoreCase = true)
        }
    }

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

            // Инструкция + бейдж типа (визуальная идентичность каждому типу)
            Row(verticalAlignment = Alignment.CenterVertically) {
                ExerciseTypeBadge(exercise.type, accentColor)
                Spacer(Modifier.width(8.dp))
                Text(
                    text       = exercise.instruction,
                    fontSize   = 14.sp,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    modifier   = Modifier.weight(1f),
                )
            }
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
                    fontSize = 15.sp,
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
                                        tts?.speakSpanish(t, "ans")
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
                                    tts?.speakSpanish(t, "ans")
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
                                    tts?.speakSpanish(t, "ans")
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
                                tts?.speakSpanish(t, "ans")
                            }
                        }
                    )
                }

                ExerciseType.SPEAKING -> {
                    SpeakingInput(
                        wordToSay   = exercise.correctAnswer,
                        accentColor = accentColor,
                        tts         = tts,
                        onPassed    = {
                            // Только успех завершает упражнение
                            selectedOption = exercise.correctAnswer
                            answered = true
                        },
                        onSkipped   = {
                            // После 3 провалов — принудительный пропуск как ошибка
                            selectedOption = "__failed__"
                            answered = true
                        }
                    )
                }

                ExerciseType.LISTEN_PICK -> {
                    ListenPickInput(
                        audioText = exercise.audioText.ifBlank { exercise.correctAnswer },
                        options = exercise.options,
                        correctAnswer = exercise.correctAnswer,
                        accentColor = accentColor,
                        answered = answered,
                        selectedOption = selectedOption,
                        tts = tts,
                        onAnswer = { picked ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedOption = picked
                            answered = true
                            inferSpeakText(exercise.correctAnswer)?.let { t ->
                                tts?.speakSpanish(t, "ans")
                            }
                        }
                    )
                }

                ExerciseType.ORDER_LETTERS -> {
                    OrderLettersInput(
                        correctAnswer = exercise.correctAnswer,
                        accentColor = accentColor,
                        answered = answered,
                        onAnswer = { built ->
                            selectedOption = built
                            answered = true
                            if (built.equals(exercise.correctAnswer.replace(" ", ""), ignoreCase = true)) {
                                inferSpeakText(exercise.correctAnswer)?.let { t ->
                                    tts?.speakSpanish(t, "ans")
                                }
                            }
                        }
                    )
                }

                ExerciseType.MATCH_PAIRS -> {
                    MatchPairsInput(
                        pairs = exercise.pairs,
                        accentColor = accentColor,
                        answered = answered,
                        onAnswer = { _ ->
                            // The user successfully paired everything (only
                            // way the input emits onAnswer). Partial mistakes
                            // along the way are tolerated — pairing is by
                            // nature trial-and-error and we'd punish the
                            // normal exploration pattern.
                            selectedOption = exercise.correctAnswer
                            answered = true
                        },
                    )
                }

                ExerciseType.TAP_MISSING_WORD -> {
                    TapMissingWordInput(
                        sentence = exercise.question,
                        options = exercise.options,
                        correctAnswer = exercise.correctAnswer,
                        accentColor = accentColor,
                        answered = answered,
                        selectedOption = selectedOption,
                        onAnswer = { picked ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedOption = picked
                            answered = true
                            inferSpeakText(exercise.correctAnswer)?.let { t ->
                                tts?.speakSpanish(t, "ans")
                            }
                        }
                    )
                }

                ExerciseType.LISTEN_TYPE -> {
                    ListenAndTypeInput(
                        audioText = exercise.audioText.ifBlank { exercise.correctAnswer },
                        correctAnswer = exercise.correctAnswer,
                        accentColor = accentColor,
                        answered = answered,
                        tts = tts,
                        onAnswer = { typed ->
                            selectedOption = typed
                            answered = true
                            if (typed.trim().equals(exercise.correctAnswer.trim(), ignoreCase = true)) {
                                inferSpeakText(exercise.correctAnswer)?.let { t ->
                                    tts?.speakSpanish(t, "ans")
                                }
                            }
                        }
                    )
                }

                ExerciseType.CONJUGATION_GRID -> {
                    // hint encodes "infinitive | tense" via Exercise.hint
                    val parts = exercise.hint.split("|").map { it.trim() }
                    val infinitive = parts.getOrNull(0) ?: exercise.correctAnswer
                    val tense = parts.getOrNull(1) ?: ""
                    ConjugationGridInput(
                        infinitive = infinitive,
                        tense = tense,
                        correctForms = exercise.conjugationForms,
                        accentColor = accentColor,
                        answered = answered,
                        onAnswer = { allCorrect ->
                            selectedOption = if (allCorrect) exercise.correctAnswer else "__partial__"
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
                                fontSize = 15.sp,
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
                        text       = if (isCorrectAnswer) stringResource(R.string.ls_continue) else stringResource(R.string.ls_got_continue),
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
// Правило: только верное произношение позволяет идти дальше.
// После MAX_FAILS неудачных попыток появляется кнопка «Пропустить».
private const val MAX_FAILS = 3

@Composable
private fun SpeakingInput(
    wordToSay: String,
    accentColor: Color,
    tts: TextToSpeech?,
    onPassed:  () -> Unit,
    onSkipped: () -> Unit
) {
    val ctx   = LocalContext.current
    val scope = rememberCoroutineScope()
    val noMicPermissionStr = stringResource(R.string.ls_no_mic_permission)
    val recognizer = remember(ctx) {
        EntryPointAccessors.fromApplication(
            ctx.applicationContext,
            SpeechRecognizerEntryPoint::class.java
        ).spanishSpeechRecognizer()
    }

    var isListening  by remember { mutableStateOf(false) }
    var recognized   by remember { mutableStateOf("") }
    var lastPassed   by remember { mutableStateOf(false) }
    var quality      by remember { mutableStateOf(com.spanishapp.service.PronunciationQuality.WRONG) }
    var isSilence    by remember { mutableStateOf(false) }
    var errorMsg     by remember { mutableStateOf("") }
    var failCount    by remember { mutableStateOf(0) }  // только реальные ошибки (не тишина)
    var showResult   by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "mic")
    val micScale by infiniteTransition.animateFloat(
        initialValue  = 1f, targetValue = 1.22f,
        animationSpec = infiniteRepeatable(tween(550), RepeatMode.Reverse),
        label = "micScale"
    )

    fun handleResult(r: com.spanishapp.service.PronunciationResult) {
        isListening = false
        isSilence   = r.isSilence
        errorMsg    = r.error
        if (r.isSilence) return          // тишина → не считаем, просто показываем подсказку

        recognized = r.recognized
        lastPassed = r.passed
        quality    = r.quality
        showResult = true
        if (r.passed) {
            onPassed()                   // верно → идём дальше
        } else {
            failCount++                  // неверно → счётчик попыток
        }
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) scope.launch {
            doListen(recognizer, wordToSay,
                onStart  = { isListening = true; isSilence = false; errorMsg = ""; showResult = false },
                onResult = ::handleResult)
        } else errorMsg = noMicPermissionStr
    }

    fun startListening() {
        if (isListening) return
        isSilence = false; errorMsg = ""; showResult = false
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED) {
            scope.launch {
                doListen(recognizer, wordToSay,
                    onStart  = { isListening = true },
                    onResult = ::handleResult)
            }
        } else permLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {

        // Карточка со словом
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = accentColor.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(wordToSay, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, color = accentColor)
                Spacer(Modifier.height(4.dp))
                SpeakerButton(text = wordToSay, tts = tts, tint = accentColor)
                Text(stringResource(R.string.ls_listen_then_repeat),
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Тишина
        AnimatedVisibility(visible = isSilence) {
            Surface(shape = RoundedCornerShape(14.dp), color = Orange.copy(0.12f),
                modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🤔", fontSize = 22.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            if (failCount == 0) stringResource(R.string.ls_distracted)
                            else stringResource(R.string.ls_louder),
                            fontWeight = FontWeight.SemiBold, fontSize = 15.sp
                        )
                        Text(stringResource(R.string.ls_not_counted),
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Техническая ошибка
        AnimatedVisibility(visible = errorMsg.isNotEmpty() && !isSilence) {
            Text("⚠️ $errorMsg", color = Red, fontSize = 15.sp,
                modifier = Modifier.padding(top = 4.dp))
        }

        // Результат попытки (только неверные — верные сразу уходят через onPassed)
        AnimatedVisibility(visible = showResult && !lastPassed) {
            Surface(shape = RoundedCornerShape(14.dp), color = Red.copy(0.08f),
                modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    val msg = when {
                        failCount == 1 -> stringResource(R.string.ls_try_msg_1)
                        failCount == 2 -> stringResource(R.string.ls_try_msg_2)
                        else           -> stringResource(R.string.ls_try_msg_3)
                    }
                    Text("❌  $msg", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Red)
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.ls_recognized, recognized), fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.ls_needed, wordToSay), fontSize = 15.sp,
                        color = accentColor, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Кнопка микрофона
        Box(contentAlignment = Alignment.Center) {
            if (isListening) {
                Box(Modifier.size(88.dp).scale(micScale).clip(CircleShape)
                    .background(accentColor.copy(0.15f)))
            }
            IconButton(
                onClick  = { startListening() },
                enabled  = !isListening,
                modifier = Modifier.size(72.dp).clip(CircleShape)
                    .background(if (isListening) accentColor else accentColor.copy(0.15f))
            ) {
                Icon(Icons.Default.Mic, contentDescription = stringResource(R.string.ls_speak_cd),
                    tint = if (isListening) Color.White else accentColor,
                    modifier = Modifier.size(32.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = when {
                isListening  -> stringResource(R.string.ls_listening_dots)
                isSilence    -> stringResource(R.string.ls_tap_again)
                failCount > 0 -> stringResource(R.string.ls_try_again_count, failCount, MAX_FAILS)
                else         -> stringResource(R.string.ls_tap_to_say)
            },
            fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Кнопка «Пропустить» — только после MAX_FAILS провалов
        AnimatedVisibility(visible = failCount >= MAX_FAILS) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.ls_hard_word_hint),
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    onClick = onSkipped,
                    shape   = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.ls_skip_as_wrong),
                        fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private suspend fun doListen(
    recognizer: com.spanishapp.service.SpanishSpeechRecognizer,
    word: String,
    onStart: () -> Unit,
    onResult: (com.spanishapp.service.PronunciationResult) -> Unit
) {
    onStart()
    onResult(recognizer.checkPronunciation(word))
}

// ─── Ввод слова (FILL_BLANK / TRANSLATE) ──────────────────────────────────
@Composable
private fun FillBlankInput(
    correctAnswer: String,
    accentColor: Color,
    answered: Boolean,
    onAnswer: (String) -> Unit
) {
    // Re-key on `correctAnswer` so consecutive exercises sharing this
    // composable in the same slot don't leak previous typed text.
    var typed by remember(correctAnswer) { mutableStateOf("") }
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
        placeholder   = { Text(stringResource(R.string.ls_input_answer), color = MaterialTheme.colorScheme.onSurfaceVariant) },
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
            text = stringResource(R.string.ls_correct_is, correctAnswer),
            color = Green,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
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
            Text(stringResource(R.string.ls_check), fontWeight = FontWeight.ExtraBold)
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
    // Re-key chosen on `words` too — previously chosen was un-keyed,
    // so tokens from the prior BUILD_SENTENCE exercise leaked into the next.
    val chosen   = remember(words) { mutableStateListOf<String>() }
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
                Text(stringResource(R.string.ls_tap_words_below), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
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
            text = stringResource(R.string.ls_correct_is, correctAnswer),
            color = Green,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
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
            ) { Text(stringResource(R.string.ls_check), fontWeight = FontWeight.ExtraBold) }
        }
    }
}

// ─── ExerciseTypeBadge: маленький эмодзи-чип в шапке упражнения ────────────
@Composable
private fun ExerciseTypeBadge(type: ExerciseType, accent: Color) {
    // Badge keeps the unit's own colour. Previously forced to amber on dark
    // theme because A1 deep-purple was unreadable; A1 is now yellow itself
    // and the others (cyan/green/orange/pink) have enough contrast already.
    val readable = accent
    val (emoji, label) = when (type) {
        ExerciseType.MULTIPLE_CHOICE   -> "✏️" to androidx.compose.ui.res.stringResource(com.spanishapp.R.string.exercise_type_choice)
        ExerciseType.FILL_BLANK        -> "📝" to androidx.compose.ui.res.stringResource(com.spanishapp.R.string.exercise_type_fill_blank)
        ExerciseType.TRANSLATE         -> "🌐" to androidx.compose.ui.res.stringResource(com.spanishapp.R.string.exercise_type_translate)
        ExerciseType.BUILD_SENTENCE    -> "🧱" to androidx.compose.ui.res.stringResource(com.spanishapp.R.string.exercise_type_build)
        ExerciseType.SPEAKING          -> "🎤" to androidx.compose.ui.res.stringResource(com.spanishapp.R.string.exercise_type_speaking)
        ExerciseType.LISTEN_PICK       -> "🔊" to androidx.compose.ui.res.stringResource(com.spanishapp.R.string.exercise_type_listen_pick)
        ExerciseType.ORDER_LETTERS     -> "🔤" to androidx.compose.ui.res.stringResource(com.spanishapp.R.string.exercise_type_order_letters)
        ExerciseType.MATCH_PAIRS       -> "🔗" to androidx.compose.ui.res.stringResource(com.spanishapp.R.string.exercise_type_match_pairs)
        ExerciseType.TAP_MISSING_WORD  -> "📌" to androidx.compose.ui.res.stringResource(com.spanishapp.R.string.exercise_type_missing_word)
        ExerciseType.LISTEN_TYPE       -> "🎧" to androidx.compose.ui.res.stringResource(com.spanishapp.R.string.exercise_type_listen_type)
        ExerciseType.CONJUGATION_GRID  -> "📊" to androidx.compose.ui.res.stringResource(com.spanishapp.R.string.exercise_type_conjugation)
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = readable.copy(alpha = 0.18f),
        border = androidx.compose.foundation.BorderStroke(1.dp, readable.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(emoji, fontSize = 13.sp)
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = readable,
                letterSpacing = 0.4.sp,
            )
        }
    }
}

// ─── ListenPickInput: TTS играет → тапни правильный из 4 написанных ────────
@Composable
private fun ListenPickInput(
    audioText: String,
    options: List<String>,
    correctAnswer: String,
    accentColor: Color,
    answered: Boolean,
    selectedOption: String?,
    tts: TextToSpeech?,
    onAnswer: (String) -> Unit,
) {
    // Auto-play once on first show
    LaunchedEffect(audioText) {
        delay(250)
        tts?.speakSpanish(audioText, "listen_pick")
    }

    // Large replay button
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = accentColor.copy(alpha = 0.10f),
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clickable(enabled = !answered) {
                tts?.speakSpanish(audioText, "listen_pick_replay")
            }
    ) {
        Row(
            Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                contentAlignment = Alignment.Center,
            ) {
                Text("🔊", fontSize = 26.sp)
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.ls_tap_to_replay),
                color = accentColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
        }
    }

    Spacer(Modifier.height(20.dp))

    // Options grid (vertical list of chips)
    options.forEach { option ->
        val isSelected = selectedOption == option
        val isCorrect  = option == correctAnswer
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
            shape = RoundedCornerShape(14.dp),
            color = bgColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
                .border(
                    width = if (isSelected || (answered && isCorrect)) 2.dp else 0.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable(enabled = !answered) { onAnswer(option) }
        ) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text       = option,
                    fontSize   = 17.sp,
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

// ─── OrderLettersInput: анаграмма — собери слово из тайлов букв ────────────
@Composable
private fun OrderLettersInput(
    correctAnswer: String,
    accentColor: Color,
    answered: Boolean,
    onAnswer: (String) -> Unit,
) {
    // Stable shuffled order (re-randomized only if correct answer changes)
    val letters = remember(correctAnswer) {
        correctAnswer.toList()
            .filter { it.isLetter() }
            .map { it.toString() }
            .shuffled()
    }
    val chosen = remember(correctAnswer) { mutableStateListOf<IndexedValue<String>>() }
    val poolUsed = remember(correctAnswer) { mutableStateListOf<Int>() }

    val built     = chosen.joinToString("") { it.value }
    val isCorrect = built.equals(correctAnswer.replace(" ", ""), ignoreCase = true)

    // Built word area
    Surface(
        shape    = RoundedCornerShape(14.dp),
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
    ) {
        Box(Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
            if (chosen.isEmpty()) {
                Text(
                    text = stringResource(R.string.ls_tap_letters_below),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp,
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(chosen.toList()) { iv ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accentColor.copy(alpha = 0.15f),
                            modifier = Modifier.clickable(enabled = !answered) {
                                chosen.remove(iv)
                                poolUsed.remove(iv.index)
                            }
                        ) {
                            Text(
                                text = iv.value,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = accentColor,
                            )
                        }
                    }
                }
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    // Letter pool
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        itemsIndexed(letters) { idx, letter ->
            val used = idx in poolUsed
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (used) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .clickable(enabled = !answered && !used) {
                        poolUsed.add(idx)
                        chosen.add(IndexedValue(idx, letter))
                    }
                    .border(
                        1.dp,
                        if (used) Color.Transparent else accentColor.copy(alpha = 0.3f),
                        RoundedCornerShape(10.dp)
                    )
            ) {
                Text(
                    text = if (used) " " else letter,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 36.dp, minHeight = 44.dp)
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    // Wrong answer reveal
    AnimatedVisibility(visible = answered && !isCorrect) {
        Text(
            text = stringResource(R.string.ls_correct_is, correctAnswer),
            color = Green,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
        )
    }

    if (!answered) {
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { chosen.clear(); poolUsed.clear() },
                modifier = Modifier.height(52.dp),
                shape = RoundedCornerShape(14.dp),
            ) { Text("↺") }
            Button(
                onClick = { if (chosen.isNotEmpty()) onAnswer(built) },
                enabled = chosen.isNotEmpty(),
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            ) {
                Text(stringResource(R.string.ls_check), fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

// ─── MatchPairsInput: соедини es ↔ ru ──────────────────────────────────────
@Composable
private fun MatchPairsInput(
    pairs: List<Pair<String, String>>,
    accentColor: Color,
    answered: Boolean,
    onAnswer: (allCorrect: Boolean) -> Unit,
) {
    if (pairs.isEmpty()) return

    val leftItems  = remember(pairs) { pairs.map { it.first } }
    val rightItems = remember(pairs) { pairs.map { it.second }.shuffled() }

    var selectedLeft  by remember { mutableStateOf<String?>(null) }
    var selectedRight by remember { mutableStateOf<String?>(null) }
    val matched       = remember { mutableStateListOf<Pair<String, String>>() }
    var wrongTick     by remember { mutableStateOf(0) }     // for brief red flash
    var mistakes      by remember { mutableStateOf(0) }
    val scope         = rememberCoroutineScope()

    // Reset state when the exercise content changes (next session)
    LaunchedEffect(pairs) {
        selectedLeft = null
        selectedRight = null
        matched.clear()
        wrongTick = 0
        mistakes = 0
    }

    // Try-match whenever both sides selected.
    LaunchedEffect(selectedLeft, selectedRight) {
        val l = selectedLeft
        val r = selectedRight
        if (l != null && r != null) {
            val correct = pairs.any { it.first == l && it.second == r }
            if (correct) {
                matched += (l to r)
                selectedLeft = null
                selectedRight = null
                if (matched.size == pairs.size) {
                    delay(200)
                    onAnswer(mistakes == 0)
                }
            } else {
                wrongTick++
                mistakes++
                delay(450)
                selectedLeft = null
                selectedRight = null
            }
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 360.dp),    // expand so 4–5 pair chips fill the area
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Left column (Spanish)
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            leftItems.forEach { item ->
                val isMatched  = matched.any { it.first == item }
                val isSelected = selectedLeft == item
                val isWrong    = isSelected && wrongTick > 0 && selectedRight != null
                PairChip(
                    text = item,
                    matched = isMatched,
                    selected = isSelected,
                    wrong = isWrong,
                    accentColor = accentColor,
                    onClick = {
                        if (!isMatched && !answered) selectedLeft = item
                    },
                )
            }
        }
        // Right column (Russian)
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            rightItems.forEach { item ->
                val isMatched  = matched.any { it.second == item }
                val isSelected = selectedRight == item
                val isWrong    = isSelected && wrongTick > 0 && selectedLeft != null
                PairChip(
                    text = item,
                    matched = isMatched,
                    selected = isSelected,
                    wrong = isWrong,
                    accentColor = accentColor,
                    onClick = {
                        if (!isMatched && !answered) selectedRight = item
                    },
                )
            }
        }
    }

    if (mistakes > 0) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Ошибок: $mistakes",
            color = Red.copy(alpha = 0.75f),
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

@Composable
private fun PairChip(
    text: String,
    matched: Boolean,
    selected: Boolean,
    wrong: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
) {
    val bg = when {
        matched  -> Green.copy(alpha = 0.18f)
        wrong    -> Red.copy(alpha = 0.18f)
        selected -> accentColor.copy(alpha = 0.18f)
        else     -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val border = when {
        matched  -> Green
        wrong    -> Red
        selected -> accentColor
        else     -> Color.Transparent
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected || matched || wrong) 2.dp else 0.dp,
                color = border,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(enabled = !matched, onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp),     // bigger touch target — was ~40dp
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 18.dp),
                fontSize = 17.sp,
                fontWeight = if (selected || matched) FontWeight.Bold else FontWeight.Medium,
                color = if (matched) Green
                        else if (wrong) Red
                        else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ─── TapMissingWordInput: предложение с пропуском, тап нужное слово ────────
@Composable
private fun TapMissingWordInput(
    sentence: String,           // содержит "___" в месте пропуска
    options: List<String>,
    correctAnswer: String,
    accentColor: Color,
    answered: Boolean,
    selectedOption: String?,
    onAnswer: (String) -> Unit,
) {
    // Render sentence with inline blank/answer.
    val displayed = remember(sentence, selectedOption, answered) {
        when {
            answered && selectedOption != null ->
                sentence.replace("___", selectedOption)
            else -> sentence
        }
    }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = displayed,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }

    Spacer(Modifier.height(16.dp))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = selectedOption == option
            val isCorrect  = option == correctAnswer
            val bgColor = when {
                !answered && isSelected              -> accentColor.copy(alpha = 0.12f)
                answered && isCorrect                -> Green.copy(alpha = 0.14f)
                answered && isSelected && !isCorrect -> Red.copy(alpha = 0.14f)
                else                                 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            }
            val borderColor = when {
                answered && isCorrect                -> Green
                answered && isSelected && !isCorrect -> Red
                isSelected                           -> accentColor
                else                                 -> Color.Transparent
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = bgColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isSelected || (answered && isCorrect)) 2.dp else 0.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .clickable(enabled = !answered) { onAnswer(option) }
            ) {
                Text(
                    text = option,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    fontSize = 17.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// ─── ListenAndTypeInput: TTS играет → напечатай услышанное ─────────────────
@Composable
private fun ListenAndTypeInput(
    audioText: String,
    correctAnswer: String,
    accentColor: Color,
    answered: Boolean,
    tts: TextToSpeech?,
    onAnswer: (String) -> Unit,
) {
    // Auto-play once
    LaunchedEffect(audioText) {
        delay(300)
        tts?.speakSpanish(audioText, "listen_type")
    }

    // Speaker / replay button
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = accentColor.copy(alpha = 0.10f),
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clickable(enabled = !answered) {
                tts?.speakSpanish(audioText, "listen_type_replay")
            }
    ) {
        Row(
            Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                contentAlignment = Alignment.Center,
            ) { Text("🔊", fontSize = 24.sp) }
            Spacer(Modifier.width(14.dp))
            Text(
                text = stringResource(R.string.ls_listen_and_type),
                color = accentColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
        }
    }

    Spacer(Modifier.height(18.dp))

    FillBlankInput(
        correctAnswer = correctAnswer,
        accentColor = accentColor,
        answered = answered,
        onAnswer = onAnswer,
    )
}

// ─── ConjugationGridInput: заполни все 6 форм глагола ──────────────────────
@Composable
private fun ConjugationGridInput(
    infinitive: String,
    tense: String,
    correctForms: List<String>,        // size 6: yo, tu, el, nosotros, vosotros, ellos
    accentColor: Color,
    answered: Boolean,
    onAnswer: (allCorrect: Boolean) -> Unit,
) {
    if (correctForms.size != 6) return

    val pronouns = listOf("yo", "tú", "él/ella", "nosotros", "vosotros", "ellos/ellas")
    val typed = remember(correctForms) { mutableStateListOf("", "", "", "", "", "") }
    val keyboard = LocalSoftwareKeyboardController.current

    fun isCellCorrect(i: Int): Boolean =
        typed[i].trim().equals(correctForms[i].trim(), ignoreCase = true)

    val allFilled = typed.all { it.isNotBlank() }

    // Header card with infinitive + tense
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = accentColor.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(infinitive, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = accentColor)
            Text(tense, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    Spacer(Modifier.height(14.dp))

    // 6 rows
    pronouns.forEachIndexed { i, pronoun ->
        val cellCorrect = isCellCorrect(i)
        val borderColor = when {
            !answered -> accentColor.copy(alpha = 0.4f)
            cellCorrect -> Green
            else -> Red
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = pronoun,
                modifier = Modifier.width(108.dp),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = typed[i],
                onValueChange = { if (!answered) typed[i] = it },
                enabled = !answered,
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = borderColor,
                    unfocusedBorderColor = borderColor.copy(alpha = 0.6f),
                    disabledBorderColor = borderColor,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                ),
                trailingIcon = {
                    if (answered) {
                        Text(
                            if (cellCorrect) "✓" else correctForms[i],
                            color = if (cellCorrect) Green else Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(end = 12.dp),
                        )
                    }
                },
            )
        }
    }

    if (!answered) {
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = {
                keyboard?.hide()
                // Pass with ≥4/6 correct — single typo or vosotros confusion
                // shouldn't void the whole grid. UI still flags wrong cells
                // in red so the user knows which ones missed.
                val correctCount = (0 until 6).count { isCellCorrect(it) }
                onAnswer(correctCount >= 4)
            },
            enabled = allFilled,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
        ) {
            Text(stringResource(R.string.ls_check), fontWeight = FontWeight.ExtraBold)
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
    val haptic = com.spanishapp.ui.components.rememberCheckedHaptic()

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

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(24.dp))

        Text("Сессия завершена!", fontWeight = FontWeight.SemiBold, fontSize = 24.sp)

        Spacer(Modifier.height(20.dp))

        com.spanishapp.ui.components.CompletionBadge(
            accuracyPercent = accuracy,
            size = 180.dp
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text      = lessonTitle,
            fontSize  = 14.sp,
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
                    label      = stringResource(R.string.ls_stat_accuracy),
                    icon       = "🎯",
                    accentColor = if (accuracy >= 75) Green else Orange,
                    modifier   = Modifier.weight(1f)
                )
            }
            if (bestCombo >= 2) {
                StatTile(
                    value      = "🔥$bestCombo",
                    label      = stringResource(R.string.ls_stat_combo),
                    icon       = "",
                    accentColor = Orange,
                    modifier   = Modifier.weight(1f)
                )
            } else {
                StatTile(
                    value      = "$correctCount/$totalExercises",
                    label      = stringResource(R.string.ls_stat_correct),
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
                    .height(56.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(stringResource(R.string.ls_next_lesson), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(8.dp))
        }

        TextButton(onClick = onFinish) {
            Text(
                text     = if (hasNextLesson) stringResource(R.string.ls_exit_to_menu) else stringResource(R.string.ls_done_caps),
                fontSize = 15.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
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
        title   = { Text(stringResource(R.string.ls_quit_title), fontWeight = FontWeight.Bold) },
        text    = { Text(stringResource(R.string.ls_quit_text)) },
        confirmButton = {
            TextButton(onClick = onQuit) {
                Text(stringResource(R.string.ls_quit_confirm), color = Red, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(onClick = onResume) { Text(stringResource(R.string.ls_quit_resume)) }
        }
    )
}
