package com.spanishapp.ui.checkpoint

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.spanishapp.domain.checkpoint.*
import com.spanishapp.ui.components.rememberCheckedHaptic
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Главный экран чекпоинта. Маршрутизирует по uiState:
 *  Intro → стартовый экран
 *  Playing → раунд с одним из 6 форматов
 *  Finished → результат
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckpointScreen(
    navController: NavHostController,
    checkpointId: String,
    viewModel: CheckpointViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(checkpointId) {
        viewModel.load(checkpointId)
    }

    when (val s = state) {
        is CheckpointUiState.Loading -> LoadingView()
        is CheckpointUiState.Error -> ErrorView(s.message) { navController.popBackStack() }
        is CheckpointUiState.Intro -> IntroView(
            data = s.data,
            onStart = { viewModel.startSession() },
            onBack = { navController.popBackStack() },
        )
        is CheckpointUiState.Playing -> PlayingView(
            state = s.state,
            onSubmit = { viewModel.submit(it) },
            onReplayAudio = { viewModel.replayNpcLine() },
            onBack = { navController.popBackStack() },
        )
        is CheckpointUiState.Finished -> ResultView(
            state = s.state,
            onExit = { navController.popBackStack() },
            onRetry = { viewModel.load(checkpointId) },
        )
    }
}

// ════════════════════════════════════════════════════════════════════
//  STATE VIEWS
// ════════════════════════════════════════════════════════════════════

@Composable
private fun LoadingView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorView(message: String, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = MaterialTheme.colorScheme.error, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onBack) { Text("Назад") }
        }
    }
}

@Composable
private fun IntroView(
    data: CheckpointData,
    onStart: () -> Unit,
    onBack: () -> Unit,
) {
    val scroll = rememberScrollState()
    val haptic = rememberCheckedHaptic()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scroll)
        ) {
            // Hero (gradient placeholder вместо реального фото пока)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF8B4513), Color(0xFFD4A373))
                        )
                    )
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Box(
                    modifier = Modifier.align(Alignment.Center),
                ) {
                    Text(
                        "⛳",
                        fontSize = 80.sp,
                    )
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFF6B1A),
                    modifier = Modifier.padding(bottom = 10.dp),
                ) {
                    Text(
                        "ЧЕКПОИНТ ${data.cefr}",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }

                Text(
                    data.titleRu,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    data.descriptionRu,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp,
                )

                if (data.stakesRu.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        color = Color(0xFFE54848).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                "СТАВКА",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFE54848),
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(data.stakesRu, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Info card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("📋 ${data.rounds.size} заданий", fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("👤 ${data.npc.name} · ${data.npc.roleRu}", fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "⭐ Pass: ${data.thresholds.bronzePercent}% правильных",
                            fontSize = 13.sp,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "🎁 Награда: +${data.rewards.bronzeXp} XP + бейдж",
                            fontSize = 13.sp,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onStart()
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B1A)),
                ) {
                    Text("Начать", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PlayingView(
    state: CheckpointState,
    onSubmit: (String) -> Unit,
    onReplayAudio: () -> Unit,
    onBack: () -> Unit,
) {
    val round = state.currentRound ?: return
    val haptic = rememberCheckedHaptic()
    var answered by remember(state.currentRoundIndex) { mutableStateOf(false) }
    var userAnswer by remember(state.currentRoundIndex) { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // Scene top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    Brush.linearGradient(listOf(Color(0xFF1A1B1F), Color(0xFF3A3B3F)))
                )
        ) {
            IconButton(onClick = onBack, modifier = Modifier.padding(8.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            // Sympathy stars
            Row(
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.55f),
                ) {
                    Text(
                        "★".repeat(state.sympathyStars) + "☆".repeat(5 - state.sympathyStars),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = Color(0xFFF4B400),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            // Place + round
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(14.dp)) {
                Text(
                    state.data.scene.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Раунд ${state.currentRoundIndex + 1} / ${state.totalRounds}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                )
            }
            // Progress dots
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(14.dp)
                    .width(100.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                repeat(state.totalRounds) { i ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                when {
                                    i < state.currentRoundIndex -> Color(0xFF2EB872)
                                    i == state.currentRoundIndex -> Color.White
                                    else -> Color.White.copy(alpha = 0.3f)
                                }
                            )
                    )
                }
            }
        }

        // NPC bubble (если не audio_only с скрытым текстом)
        if (round.npcLineEs != null && !round.audioOnly) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF6B1A)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(state.data.npc.name.first().toString(), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${state.data.npc.name.uppercase()} · ${state.data.npc.roleRu.uppercase()}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(round.npcLineEs, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        if (round.npcLineRu != null) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                round.npcLineRu,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    IconButton(onClick = onReplayAudio) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color(0xFFFF6B1A))
                    }
                }
            }
        } else if (round.audioOnly) {
            // Audio-only — большая кнопка прослушать
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(listOf(Color(0xFFFF6B1A), Color(0xFFFF8533)))
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onReplayAudio()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Прослушать", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(4.dp))

            if (round.promptRu.isNotBlank()) {
                Text(
                    round.promptRu,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }

            when (round.format) {
                RoundFormat.CHOICE,
                RoundFormat.LISTEN -> ChoicePicker(
                    round = round,
                    enabled = !answered,
                    onPick = { picked ->
                        userAnswer = picked
                        answered = true
                        onSubmit(picked)
                    }
                )

                RoundFormat.CONJUGATE -> ConjugatePicker(
                    round = round,
                    enabled = !answered,
                    onPick = { picked ->
                        userAnswer = picked
                        answered = true
                        onSubmit(picked)
                    }
                )

                RoundFormat.BUILD -> SentenceBuilder(
                    round = round,
                    enabled = !answered,
                    onSubmit = { built ->
                        userAnswer = built
                        answered = true
                        onSubmit(built)
                    }
                )

                RoundFormat.TRANSLATE_RU_ES,
                RoundFormat.TRANSLATE_ES_RU -> TranslateInput(
                    round = round,
                    enabled = !answered,
                    onSubmit = { typed ->
                        userAnswer = typed
                        answered = true
                        onSubmit(typed)
                    }
                )

                RoundFormat.VOICE -> {
                    // TODO: voice STT, пока показываем сообщение
                    Text("Голосовой ответ — будет добавлен позже. Пропуск раунда.",
                        color = MaterialTheme.colorScheme.error)
                    LaunchedEffect(round.round) { onSubmit("") }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  FORMAT RENDERERS
// ════════════════════════════════════════════════════════════════════

@Composable
private fun ChoicePicker(round: CheckpointRound, enabled: Boolean, onPick: (String) -> Unit) {
    val options = remember(round) {
        (round.distractors + round.correctAnswer).shuffled(kotlin.random.Random(round.round.toLong()))
    }
    val haptic = rememberCheckedHaptic()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { opt ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                onClick = {
                    if (enabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPick(opt)
                    }
                }
            ) {
                Text(
                    opt,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ConjugatePicker(round: CheckpointRound, enabled: Boolean, onPick: (String) -> Unit) {
    val options = remember(round) {
        (round.distractors + round.correctAnswer).shuffled(kotlin.random.Random(round.round.toLong()))
    }
    val haptic = rememberCheckedHaptic()
    Column {
        // Sentence template
        if (round.sentenceTemplate.isNotBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
            ) {
                Text(
                    round.sentenceTemplate,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        // 2-column grid for conjugation options
        val rows = options.chunked(2)
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { opt ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        onClick = {
                            if (enabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onPick(opt)
                            }
                        },
                    ) {
                        Text(
                            opt,
                            modifier = Modifier.padding(14.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))   // pad odd row
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun SentenceBuilder(round: CheckpointRound, enabled: Boolean, onSubmit: (String) -> Unit) {
    val haptic = rememberCheckedHaptic()
    val bank = remember(round) {
        round.wordBank.shuffled(kotlin.random.Random(round.round.toLong()))
    }
    val placed = remember(round) { mutableStateListOf<String>() }
    val used = remember(round) { mutableStateListOf<Int>() }

    Column {
        // Target area
        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(min = 70.dp),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                placed.forEachIndexed { idx, word ->
                    Surface(
                        shape = RoundedCornerShape(9.dp),
                        color = Color(0xFFFF6B1A),
                        onClick = {
                            if (enabled) {
                                // Remove word, restore index in bank
                                val bankIdx = bank.indexOf(word)
                                used.remove(bankIdx)
                                placed.removeAt(idx)
                            }
                        }
                    ) {
                        Text(
                            word,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Bank
        androidx.compose.foundation.layout.FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            bank.forEachIndexed { idx, word ->
                if (idx !in used) {
                    Surface(
                        shape = RoundedCornerShape(9.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        onClick = {
                            if (enabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                placed.add(word)
                                used.add(idx)
                            }
                        }
                    ) {
                        Text(
                            word,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSubmit(placed.joinToString(" "))
            },
            enabled = placed.isNotEmpty() && enabled,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B1A)),
        ) {
            Text("Готово", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TranslateInput(round: CheckpointRound, enabled: Boolean, onSubmit: (String) -> Unit) {
    val haptic = rememberCheckedHaptic()
    var text by remember(round) { mutableStateOf(TextFieldValue("")) }

    Column {
        // Prompt
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (round.format == RoundFormat.TRANSLATE_RU_ES) "Русский → Испанский" else "Испанский → Русский",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (round.format == RoundFormat.TRANSLATE_RU_ES) round.promptTextRu else round.promptTextEs,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (round.hintRu.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Подсказка: ${round.hintRu}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Твой ответ...") },
            enabled = enabled,
            shape = RoundedCornerShape(14.dp),
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSubmit(text.text)
            },
            enabled = text.text.isNotBlank() && enabled,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B1A)),
        ) {
            Text("Проверить", fontWeight = FontWeight.Bold)
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  RESULT VIEW
// ════════════════════════════════════════════════════════════════════

@Composable
private fun ResultView(
    state: CheckpointState,
    onExit: () -> Unit,
    onRetry: () -> Unit,
) {
    val outcome = state.outcome ?: return
    val haptic = rememberCheckedHaptic()
    val isPass = outcome is CheckpointOutcome.Pass

    val heroGradient = when {
        outcome is CheckpointOutcome.Pass && outcome.tier == "gold" -> listOf(Color(0xFFD97706), Color(0xFFFBBF24))
        outcome is CheckpointOutcome.Pass && outcome.tier == "silver" -> listOf(Color(0xFF6B7280), Color(0xFF9CA3AF))
        outcome is CheckpointOutcome.Pass -> listOf(Color(0xFF92400E), Color(0xFFB45309))
        else -> listOf(Color(0xFFD32F2F), Color(0xFF7D1414))
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Hero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Brush.linearGradient(heroGradient)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (isPass) "✓" else "✗",
                    fontSize = 56.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    when (outcome.tier) {
                        "gold" -> "¡Bienvenida!"
                        "silver" -> "Bienvenida"
                        "bronze" -> "Pase"
                        "near_pass" -> "Acompáñeme"
                        "low" -> "Sala aparte"
                        else -> "Espere aquí"
                    },
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${outcome.percent}% правильных",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // NPC quote
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "${state.data.npc.name.uppercase()} ГОВОРИТ",
                        fontSize = 10.sp,
                        color = Color(0xFFFF6B1A),
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(outcome.outcomeData.npcLineEs, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        outcome.outcomeData.npcLineRu,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    )
                    if (outcome.outcomeData.sceneDescriptionRu.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            outcome.outcomeData.sceneDescriptionRu,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Stats
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    StatRow("Точность", "${outcome.percent}% · ${state.correctCount}/${state.totalRounds}")
                    if (outcome is CheckpointOutcome.Pass) {
                        StatRow("Награда XP", "+${outcome.xpAwarded}")
                        StatRow("Бейдж", state.data.rewards.badgeNameRu)
                    } else if (outcome is CheckpointOutcome.Fail && outcome.weakLessons.isNotEmpty()) {
                        StatRow("Слабые уроки", outcome.weakLessons.take(3).joinToString(", "))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Actions
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onExit()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B1A)),
            ) {
                Text(
                    if (isPass) "Продолжить →" else "Закрыть",
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            if (!isPass) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Попробовать снова")
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
