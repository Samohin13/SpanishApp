package com.spanishapp.ui.games

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.spanishapp.R
import com.spanishapp.domain.games.GameId
import com.spanishapp.ui.games.common.ComboBadge
import com.spanishapp.ui.games.common.ConfettiEffect
import com.spanishapp.ui.games.common.LevelCompleteSheet
import com.spanishapp.ui.games.common.LevelMapScreen
import com.spanishapp.ui.games.common.ProgressDots

/** Фирменный акцент El Oído — голубой градиентной иконки. */
private val ACCENT = Color(0xFF4EA1FF)
private val OK_GREEN = Color(0xFF4CAF50)
private val ERR_RED = Color(0xFFF44336)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OidoScreen(
    navController: NavHostController,
    viewModel: OidoViewModel = hiltViewModel()
) {
    com.spanishapp.service.TrackActivity(com.spanishapp.service.ActivityType.GAME)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val mistakesCount by viewModel.mistakesCount.collectAsStateWithLifecycle()
    val isPro by com.spanishapp.ui.games.common.rememberIsProState()

    when {
        state.showLevelMap -> {
            LevelMapScreen(
                gameId  = GameId.OIDO,
                title   = stringResource(R.string.oido_levels_title),
                accent  = ACCENT,
                manager = viewModel.levelManager,
                onBack  = { navController.popBackStack() },
                onLevelStart = { lvl -> viewModel.startLevel(lvl) },
                mistakesCount = mistakesCount,
                onMistakesPractice = { viewModel.startMistakesPractice() },
                mistakesUnit = com.spanishapp.ui.games.common.MistakesUnit.WORDS,
                isPro     = isPro,
                onPaywall = { navController.navigate("paywall") { launchSingleTop = true } },
            )
        }
        state.isGameOver -> {
            OidoContent(state, viewModel, onBack = { viewModel.openLevelMap() })
            LevelCompleteSheet(
                level   = state.level,
                stars   = state.finalStars,
                percent = state.finalPercent,
                accent  = ACCENT,
                onRetry = { viewModel.startLevel(state.level) },
                onNext  = when {
                    state.isMistakesPractice && mistakesCount > 0 -> {
                        { viewModel.startMistakesPractice() }
                    }
                    !state.isMistakesPractice && state.finalStars > 0 && state.level < 100 -> {
                        if (!isPro && state.level + 1 > com.spanishapp.ui.games.common.FREE_GAME_LEVELS) {
                            { navController.navigate("paywall") { launchSingleTop = true } }
                        } else {
                            { viewModel.startLevel(state.level + 1) }
                        }
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
        else -> OidoContent(state, viewModel, onBack = { viewModel.openLevelMap() })
    }
}

@Composable
private fun modeLabel(task: OidoTask?): String = when (task) {
    is OidoTask.Choice -> stringResource(R.string.oido_mode_choice)
    is OidoTask.Dictation -> stringResource(R.string.oido_mode_dictation)
    is OidoTask.MinimalPair -> stringResource(R.string.oido_mode_pairs)
    is OidoTask.Number -> stringResource(R.string.oido_mode_number)
    is OidoTask.Time -> stringResource(R.string.oido_mode_time)
    null -> ""
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OidoContent(
    state: OidoState,
    viewModel: OidoViewModel,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        if (state.isMistakesPractice) {
                            Text(stringResource(R.string.mistakes_practice_title), fontWeight = FontWeight.Bold)
                            Text(
                                "${state.currentRound.coerceAtLeast(1)} / ${state.totalRounds}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(stringResource(R.string.oido_level_of, state.level), fontWeight = FontWeight.Bold)
                            Text(
                                stringResource(R.string.oido_tempo, state.rate) + " · " + modeLabel(state.task),
                                fontSize = 11.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    val hintBalance by viewModel.hintBalance.collectAsStateWithLifecycle()
                    com.spanishapp.ui.components.HintBadge(
                        count = hintBalance,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                }
            )
        }
    ) { padding ->
        var confettiKey by remember { mutableIntStateOf(0) }
        var showNoHints by remember { mutableStateOf(false) }
        if (showNoHints) {
            AlertDialog(
                onDismissRequest = { showNoHints = false },
                title = { Text(stringResource(R.string.hint_bank_empty_title)) },
                text = { Text(stringResource(R.string.hint_bank_empty_msg)) },
                confirmButton = {
                    TextButton(onClick = { showNoHints = false }) { Text("OK") }
                },
            )
        }
        val hapticVm: com.spanishapp.ui.components.HapticPrefViewModel =
            androidx.hilt.navigation.compose.hiltViewModel()
        val hapticPercent by hapticVm.intensity.collectAsStateWithLifecycle()
        LaunchedEffect(state.answerHistory.size) {
            when (state.lastCorrect) {
                true -> {
                    confettiKey++
                    hapticVm.vibrator.pulse(hapticPercent)
                }
                false -> hapticVm.vibrator.tick((hapticPercent * 130 / 100).coerceAtMost(100))
                else -> {}
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = ACCENT,
                    modifier = Modifier.align(Alignment.Center)
                )
                return@Box
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.oido_score, state.score), fontWeight = FontWeight.Bold, color = ACCENT)
                    Text("${state.currentRound} / ${state.totalRounds}", color = Color.Gray)
                }

                ProgressDots(
                    history = state.answerHistory,
                    total   = state.totalRounds,
                    accent  = ACCENT
                )

                AnimatedVisibility(visible = state.streak >= 3, enter = fadeIn(), exit = fadeOut()) {
                    ComboBadge(streak = state.streak, accentColor = ACCENT)
                }

                // ── Динамик ─────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (state.replaysLeft > 0 && state.lastCorrect == null) ACCENT
                                    else ACCENT.copy(alpha = 0.35f),
                            modifier = Modifier
                                .size(84.dp)
                                .clickable(enabled = state.replaysLeft > 0 && state.lastCorrect == null) {
                                    viewModel.replay()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = stringResource(R.string.oido_listen_again),
                                    tint = Color.White,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            stringResource(R.string.oido_replays_left, state.replaysLeft),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ── Фидбэк ──────────────────────────────────
                if (state.lastCorrect != null) {
                    val ok = state.lastCorrect == true
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = (if (ok) OK_GREEN else ERR_RED).copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, (if (ok) OK_GREEN else ERR_RED).copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                if (ok) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (ok) OK_GREEN else ERR_RED
                            )
                            Column {
                                Text(
                                    if (ok) {
                                        if (state.accentWarning) stringResource(R.string.oido_accent_note)
                                        else stringResource(R.string.oido_correct_prefix)
                                    } else stringResource(R.string.oido_wrong_prefix),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (ok) OK_GREEN else ERR_RED
                                )
                                state.feedback?.let {
                                    Text(
                                        it,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Подсказки (💡 общий банк всех игр) ──────
                val answered = state.lastCorrect != null
                if (!answered && state.task != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                    ) {
                        if (state.task is OidoTask.Choice && state.disabledOptions.isEmpty()) {
                            HintChip(stringResource(R.string.oido_hint_fifty)) {
                                viewModel.useFiftyFifty { showNoHints = true }
                            }
                        }
                        HintChip(stringResource(R.string.oido_hint_reveal)) {
                            viewModel.useRevealAnswer { showNoHints = true }
                        }
                    }
                }

                // ── Ввод по режиму ──────────────────────────
                when (val task = state.task) {
                    is OidoTask.Choice -> OidoChoiceInput(task, answered, state.disabledOptions) { viewModel.submitChoice(it) }
                    is OidoTask.MinimalPair -> OidoPairInput(task, answered) { viewModel.submitPair(it) }
                    is OidoTask.Dictation -> OidoDictationInput(state.currentRound, answered) {
                        viewModel.submitDictation(it)
                    }
                    is OidoTask.Number -> OidoDigitsInput(
                        key = state.currentRound, answered = answered, timeMode = false,
                    ) { viewModel.submitDigits(it) }
                    is OidoTask.Time -> OidoDigitsInput(
                        key = state.currentRound, answered = answered, timeMode = true,
                    ) { viewModel.submitDigits(it) }
                    null -> {}
                }

                Spacer(Modifier.height(48.dp))
            }

            ConfettiEffect(trigger = confettiKey)
        }
    }
}

// ── Чип-кнопка подсказки ─────────────────────────────────────

@Composable
private fun HintChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(100.dp),
        color = Color(0xFFFF6B35).copy(alpha = 0.14f),
        border = BorderStroke(1.dp, Color(0xFFFF6B35).copy(alpha = 0.5f)),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF6B35),
        )
    }
}

// ── Режим «выбор перевода» ───────────────────────────────────

@Composable
private fun OidoChoiceInput(
    task: OidoTask.Choice,
    answered: Boolean,
    disabledOptions: Set<String>,
    onPick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        task.options.forEach { option ->
            val showState = answered && option == task.correctRu
            val killed = option in disabledOptions   // погашен подсказкой 50/50
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !answered && !killed) { onPick(option) },
                shape = RoundedCornerShape(14.dp),
                color = if (showState) OK_GREEN.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceContainerHighest.copy(
                            alpha = if (killed) 0.35f else 1f
                        ),
                border = BorderStroke(
                    1.dp,
                    if (showState) OK_GREEN
                    else MaterialTheme.colorScheme.outline.copy(alpha = if (killed) 0.15f else 0.4f)
                )
            ) {
                Text(
                    option,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (killed) 0.3f else 1f)
                )
            }
        }
    }
}

// ── Режим «минимальная пара» ─────────────────────────────────

@Composable
private fun OidoPairInput(
    task: OidoTask.MinimalPair,
    answered: Boolean,
    onPick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Порядок карточек фиксированный (a, b) — не зависит от того, что играло
        listOf(
            task.pair.a to task.pair.ruA,
            task.pair.b to task.pair.ruB,
        ).forEach { (word, ru) ->
            val isCorrectCard = answered && word == task.correctWord
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !answered) { onPick(word) },
                shape = RoundedCornerShape(16.dp),
                color = if (isCorrectCard) OK_GREEN.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceContainerHighest,
                border = BorderStroke(
                    1.5.dp,
                    if (isCorrectCard) OK_GREEN
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        word,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isCorrectCard) OK_GREEN else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        ru,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── Режим «диктант» ──────────────────────────────────────────

@Composable
private fun OidoDictationInput(
    key: Int,
    answered: Boolean,
    onSubmit: (String) -> Unit
) {
    var typed by rememberSaveable(key) { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = typed,
            onValueChange = { if (!answered) typed = it },
            modifier = Modifier.fillMaxWidth(),
            enabled = !answered,
            singleLine = true,
            placeholder = { Text(stringResource(R.string.oido_type_hint)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (typed.isNotBlank()) onSubmit(typed)
            }),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ACCENT,
                cursorColor = ACCENT,
            )
        )
        Button(
            onClick = { if (typed.isNotBlank()) onSubmit(typed) },
            enabled = !answered && typed.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ACCENT)
        ) {
            Text(stringResource(R.string.oido_check), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        }
    }
}

// ── Режим «числа и время» ────────────────────────────────────

@Composable
private fun OidoDigitsInput(
    key: Int,
    answered: Boolean,
    timeMode: Boolean,
    onSubmit: (Int?) -> Unit
) {
    var digits by rememberSaveable(key) { mutableStateOf("") }
    val maxLen = if (timeMode) 4 else 3

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Дисплей набранного
        val shown = when {
            digits.isEmpty() -> if (timeMode) "-:--" else "?"
            timeMode && digits.length >= 3 ->
                "${digits.dropLast(2)}:${digits.takeLast(2)}"
            else -> digits
        }
        Text(
            shown,
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (digits.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else ACCENT,
            textAlign = TextAlign.Center
        )

        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("DEL", "0", "OK"),
        )
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                row.forEach { keyChar ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !answered) {
                                when (keyChar) {
                                    "DEL" -> if (digits.isNotEmpty()) digits = digits.dropLast(1)
                                    "OK" -> if (digits.isNotEmpty()) onSubmit(digits.toIntOrNull())
                                    else -> if (digits.length < maxLen) digits += keyChar
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = when (keyChar) {
                            "OK" -> ACCENT
                            "DEL" -> MaterialTheme.colorScheme.surfaceVariant
                            else -> MaterialTheme.colorScheme.surfaceContainerHighest
                        },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            when (keyChar) {
                                "DEL" -> Icon(
                                    Icons.AutoMirrored.Filled.Backspace, null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                "OK" -> Icon(
                                    Icons.Default.Check, null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                else -> Text(
                                    keyChar,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
