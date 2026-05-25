package com.spanishapp.ui.minitest

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.spanishapp.domain.minitest.MiniTestGenerator
import com.spanishapp.ui.home.Exercise
import com.spanishapp.ui.home.ExerciseType

private val Green  = Color(0xFF4CAF50)
private val Red    = Color(0xFFF44336)
private val Accent = Color(0xFFFF8A3D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniTestScreen(
    navController: NavHostController,
    unitId: String,
    position: Int,
    viewModel: MiniTestViewModel = hiltViewModel(),
) {
    LaunchedEffect(unitId, position) {
        viewModel.load(unitId, position)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.miniTest?.title ?: "Мини-тест",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Accent)
                    }
                }
                state.miniTest == null -> {
                    EmptyMiniTest(onBack = { navController.popBackStack() })
                }
                else -> when (state.phase) {
                    MiniTestPhase.INTRO -> IntroScreen(
                        title = state.miniTest!!.title,
                        questionCount = state.miniTest!!.exercises.size,
                        onStart = { viewModel.start() },
                        onSkip = { navController.popBackStack() },
                    )
                    MiniTestPhase.PLAYING -> PlayingScreen(
                        state = state,
                        onSubmit = { viewModel.submitAnswer(it) },
                    )
                    MiniTestPhase.RESULT -> ResultScreen(
                        state = state,
                        onClose = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyMiniTest(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Мини-тест пока недоступен",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Пройди ещё несколько уроков из этого блока и возвращайся.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBack) { Text("Назад") }
    }
}

@Composable
private fun IntroScreen(
    title: String,
    questionCount: Int,
    onStart: () -> Unit,
    onSkip: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("🎯", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            title,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Быстрая проверка — $questionCount заданий",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(28.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                InfoRow("⏱", "~3 минуты")
                Spacer(Modifier.height(10.dp))
                InfoRow("⭐", "+${MiniTestGenerator.XP_REWARD} XP за прохождение")
                Spacer(Modifier.height(10.dp))
                InfoRow("✅", "Pass: ${(MiniTestGenerator.PASS_THRESHOLD * 100).toInt()}%")
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text("НАЧАТЬ", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text("Пропустить", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun InfoRow(icon: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 22.sp)
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PlayingScreen(
    state: MiniTestUiState,
    onSubmit: (Boolean) -> Unit,
) {
    val mt = state.miniTest ?: return
    val idx = state.currentIndex.coerceAtMost(mt.exercises.lastIndex)
    val exercise = mt.exercises[idx]

    Column(modifier = Modifier.fillMaxSize()) {
        // Progress bar
        val progress = (idx + 1).toFloat() / mt.exercises.size
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = Accent,
            trackColor = Accent.copy(alpha = 0.15f),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Вопрос ${idx + 1} из ${mt.exercises.size}",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(12.dp))

        // Render the current exercise. We `key` on idx so renderer state
        // resets between questions (selected option, typed text, ...).
        key(idx) {
            MiniExerciseCard(
                exercise = exercise,
                onAnswered = onSubmit,
            )
        }
    }
}

/**
 * Simplified exercise renderer for mini-tests. Supports the
 * [MiniTestGenerator.SUPPORTED_TYPES] subset. Auto-advances ~700ms
 * after the user picks/submits an answer via [onAnswered].
 */
@Composable
private fun MiniExerciseCard(
    exercise: Exercise,
    onAnswered: (Boolean) -> Unit,
) {
    var answered by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<String?>(null) }
    var typed by remember { mutableStateOf("") }

    fun finish(correct: Boolean) {
        if (answered) return
        answered = true
        isCorrect = correct
    }

    // Auto-advance after a short pause so user sees feedback colour.
    LaunchedEffect(answered) {
        if (answered) {
            kotlinx.coroutines.delay(800)
            onAnswered(isCorrect)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        // v1.22.29: увеличены размеры шрифтов по фидбеку юзера —
        // мини-тесты читались слишком мелко.
        Text(
            exercise.instruction.ifBlank { "Выбери правильный ответ" },
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(14.dp))

        val bigText = if (exercise.question.isNotBlank()) exercise.question else exercise.instruction
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Accent.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(Modifier.padding(24.dp)) {
                Text(
                    text = bigText,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 32.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (exercise.hint.isNotBlank() && exercise.type != ExerciseType.SPOT_THE_ERROR) {
            Spacer(Modifier.height(10.dp))
            Text("💡 ${exercise.hint}", fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.height(20.dp))

        when (exercise.type) {
            ExerciseType.MULTIPLE_CHOICE,
            ExerciseType.TAP_MISSING_WORD,
            ExerciseType.READ_NUMBER,
            ExerciseType.SPOT_THE_ERROR -> {
                val options = remember(exercise) {
                    val raw = if (exercise.type == ExerciseType.SPOT_THE_ERROR &&
                        exercise.errorVariants.isNotEmpty()
                    ) exercise.errorVariants else exercise.options
                    val seed = (exercise.correctAnswer + raw.joinToString()).hashCode().toLong()
                    raw.shuffled(kotlin.random.Random(seed))
                }
                options.forEach { opt ->
                    val correct = opt == exercise.correctAnswer
                    val isSelected = selected == opt
                    val bg = when {
                        !answered && isSelected -> Accent.copy(alpha = 0.12f)
                        answered && correct -> Green.copy(alpha = 0.14f)
                        answered && isSelected && !correct -> Red.copy(alpha = 0.14f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    }
                    val border = when {
                        answered && correct -> Green
                        answered && isSelected && !correct -> Red
                        isSelected -> Accent
                        else -> Color.Transparent
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = bg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .border(
                                width = if (isSelected || (answered && correct)) 2.dp else 0.dp,
                                color = border,
                                shape = RoundedCornerShape(14.dp),
                            )
                            .clickable(enabled = !answered) {
                                selected = opt
                                finish(opt.trim().equals(exercise.correctAnswer.trim(), ignoreCase = true))
                            },
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                opt,
                                fontSize = 19.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                            )
                            if (answered && correct) {
                                Spacer(Modifier.width(8.dp))
                                Text("✓", color = Green, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            } else if (answered && isSelected && !correct) {
                                Spacer(Modifier.width(8.dp))
                                Text("✗", color = Red, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }

            ExerciseType.TRANSLATE -> {
                OutlinedTextField(
                    value = typed,
                    onValueChange = { typed = it },
                    enabled = !answered,
                    label = { Text("Твой ответ") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            finish(typed.trim().equals(exercise.correctAnswer.trim(), ignoreCase = true))
                        },
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                if (!answered) {
                    Button(
                        onClick = {
                            finish(typed.trim().equals(exercise.correctAnswer.trim(), ignoreCase = true))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = typed.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    ) { Text("ПРОВЕРИТЬ", fontWeight = FontWeight.ExtraBold) }
                } else {
                    AnswerFeedback(
                        correct = isCorrect,
                        correctAnswer = exercise.correctAnswer,
                    )
                }
            }

            ExerciseType.BUILD_SENTENCE,
            ExerciseType.ORDER_LETTERS -> {
                BuildFromTilesInput(
                    tiles = if (exercise.type == ExerciseType.BUILD_SENTENCE) {
                        exercise.words
                    } else {
                        // Shuffle letters of the answer.
                        exercise.correctAnswer.replace(" ", "")
                            .toCharArray().map { it.toString() }
                            .shuffled(kotlin.random.Random(exercise.correctAnswer.hashCode().toLong()))
                    },
                    correctAnswer = exercise.correctAnswer,
                    answered = answered,
                    joinWithSpaces = exercise.type == ExerciseType.BUILD_SENTENCE,
                    onAnswer = { built ->
                        val want = if (exercise.type == ExerciseType.BUILD_SENTENCE) {
                            exercise.correctAnswer.trim()
                        } else exercise.correctAnswer.replace(" ", "").trim()
                        val got = built.trim()
                        finish(got.equals(want, ignoreCase = true))
                    },
                )
                if (answered) {
                    Spacer(Modifier.height(12.dp))
                    AnswerFeedback(
                        correct = isCorrect,
                        correctAnswer = exercise.correctAnswer,
                    )
                }
            }

            else -> {
                // Fallback — shouldn't happen because SUPPORTED_TYPES is enforced
                // by the generator, but render a graceful skip in case the
                // catalog grows new types.
                Text(
                    "Этот тип задания пока не поддерживается в мини-тесте. Пропустим.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LaunchedEffect(Unit) { finish(true) }
            }
        }
    }
}

@Composable
private fun AnswerFeedback(correct: Boolean, correctAnswer: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = (if (correct) Green else Red).copy(alpha = 0.12f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (correct) "✓ Верно!" else "✗ Не совсем",
                color = if (correct) Green else Red,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
            )
            if (!correct) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Правильно: $correctAnswer",
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun BuildFromTilesInput(
    tiles: List<String>,
    correctAnswer: String,
    answered: Boolean,
    joinWithSpaces: Boolean,
    onAnswer: (String) -> Unit,
) {
    var bank by remember { mutableStateOf(tiles) }
    var picked by remember { mutableStateOf<List<String>>(emptyList()) }

    fun assembled(): String =
        if (joinWithSpaces) picked.joinToString(" ") else picked.joinToString("")

    // Assembled view
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
    ) {
        Box(Modifier.padding(14.dp)) {
            if (picked.isEmpty()) {
                Text(
                    "Собери ответ из тайлов ниже",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                )
            } else {
                Text(
                    assembled(),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
    Spacer(Modifier.height(12.dp))

    // Bank of tiles
    FlowLikeRow(items = bank) { tile, index ->
        TileChip(
            text = tile,
            enabled = !answered,
            onClick = {
                picked = picked + tile
                bank = bank.toMutableList().also { it.removeAt(index) }
            },
        )
    }
    if (picked.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        FlowLikeRow(items = picked) { tile, index ->
            TileChip(
                text = tile,
                enabled = !answered,
                selected = true,
                onClick = {
                    picked = picked.toMutableList().also { it.removeAt(index) }
                    bank = bank + tile
                },
            )
        }
    }

    Spacer(Modifier.height(16.dp))
    if (!answered) {
        Button(
            onClick = { onAnswer(assembled()) },
            modifier = Modifier.fillMaxWidth(),
            enabled = picked.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
        ) { Text("ПРОВЕРИТЬ", fontWeight = FontWeight.ExtraBold) }
    }
}

/**
 * Lightweight wrap-row replacement so we don't depend on accompanist-flowrow.
 * Splits items into rows of up to 5 (vocabulary tiles are short).
 */
@Composable
private fun FlowLikeRow(
    items: List<String>,
    content: @Composable (String, Int) -> Unit,
) {
    val rows = items.withIndex().chunked(5)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (idx, item) ->
                    content(item, idx)
                }
            }
        }
    }
}

@Composable
private fun TileChip(
    text: String,
    enabled: Boolean,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) Accent.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface,
        shadowElevation = if (selected) 0.dp else 1.dp,
        border = if (selected) BorderStroke(1.dp, Accent) else null,
        modifier = Modifier.clickable(enabled = enabled) { onClick() },
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ResultScreen(
    state: MiniTestUiState,
    onClose: () -> Unit,
) {
    val mt = state.miniTest!!
    val emoji = when {
        state.score >= 0.95f -> "🎉"
        state.score >= MiniTestGenerator.PASS_THRESHOLD -> "👍"
        else -> "💪"
    }
    val title = when {
        state.score >= 0.95f -> "Отлично!"
        state.score >= MiniTestGenerator.PASS_THRESHOLD -> "Хорошо!"
        else -> "Ещё попытка не помешает"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(emoji, fontSize = 72.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "${state.correctCount}/${state.total} — $title",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        if (state.passed) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Green.copy(alpha = 0.14f),
            ) {
                Text(
                    "+${MiniTestGenerator.XP_REWARD} XP",
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    color = Green,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                )
            }
        } else {
            Text(
                "Pass: ${(MiniTestGenerator.PASS_THRESHOLD * 100).toInt()}% — попробуй ещё раз позже",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(24.dp))

        // Weak lessons hint
        if (state.correctCount < state.total) {
            Text(
                "Темы для повторения:",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(mt.coverageLessons) { key ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            "📖 $key",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        } else {
            Spacer(Modifier.weight(1f))
        }

        Button(
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text("ГОТОВО", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

