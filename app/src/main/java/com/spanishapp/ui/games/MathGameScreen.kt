package com.spanishapp.ui.games

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.spanishapp.R
import com.spanishapp.domain.games.GameId
import com.spanishapp.ui.adaptive.adaptiveContentWidth
import com.spanishapp.ui.games.common.ComboBadge
import com.spanishapp.ui.games.common.ConfettiEffect
import com.spanishapp.ui.games.common.LevelCompleteSheet
import com.spanishapp.ui.games.common.LevelMapScreen
import com.spanishapp.ui.games.common.ProgressDots
import com.spanishapp.ui.games.common.rememberShakeOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val ACCENT = Color(0xFFF44336)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathGameScreen(
    navController: NavHostController,
    viewModel: MathViewModel = hiltViewModel()
) {
    com.spanishapp.service.TrackActivity(com.spanishapp.service.ActivityType.GAME)
    val state by viewModel.state.collectAsStateWithLifecycle()
    // rememberSaveable переживает ротацию экрана; ключ — currentRound, чтобы
    // ввод сбрасывался при смене раунда.
    var inputVal by rememberSaveable(state.currentRound) { mutableStateOf("") }
    val haptic = com.spanishapp.ui.components.rememberCheckedHaptic()
    val mistakesCount by viewModel.mistakesCount.collectAsStateWithLifecycle()

    when {
        state.showLevelMap -> {
            val isPro by com.spanishapp.ui.games.common.rememberIsProState()
            LevelMapScreen(
                gameId  = GameId.MATH,
                title   = stringResource(R.string.math_levels_title),
                accent  = ACCENT,
                manager = viewModel.levelManager,
                onBack  = { navController.popBackStack() },
                onLevelStart = { lvl -> viewModel.startLevel(lvl) },
                mistakesCount = mistakesCount,
                onMistakesPractice = { viewModel.startMistakesPractice() },
                mistakesUnit = com.spanishapp.ui.games.common.MistakesUnit.TASKS,
                isPro     = isPro,
                onPaywall = { navController.navigate("paywall") { launchSingleTop = true } },
            )
        }
        state.isGameOver -> {
            MathGameContent(state, viewModel, haptic, inputVal,
                onInput = { inputVal = it },
                onBack  = { viewModel.openLevelMap() })
            LevelCompleteSheet(
                level   = state.level,
                stars   = state.finalStars,
                percent = state.finalPercent,
                accent  = ACCENT,
                onRetry = { viewModel.startLevel(state.level) },
                onNext  = when {
                    state.isMistakesPractice && mistakesCount > 0 -> { { viewModel.startMistakesPractice() } }
                    !state.isMistakesPractice && state.finalStars > 0 && state.level < 100 -> {
                        { viewModel.startLevel(state.level + 1) }
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
        else -> MathGameContent(state, viewModel, haptic, inputVal,
            onInput = { inputVal = it },
            onBack  = { viewModel.openLevelMap() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MathGameContent(
    state: MathGameState,
    viewModel: MathViewModel,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    inputVal: String,
    onInput: (String) -> Unit,
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
                            Text(stringResource(R.string.math_level_of, state.level), fontWeight = FontWeight.Bold)
                            Text("${state.params.cefr.joinToString("+")} · ${state.params.mode.name.lowercase()}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleAudio() }) {
                        Icon(
                            if (state.audioEnabled) Icons.Default.VolumeUp
                            else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = if (state.audioEnabled) stringResource(R.string.math_audio_off) else stringResource(R.string.math_audio_on),
                            tint = if (state.audioEnabled) ACCENT else Color.Gray
                        )
                    }
                }
            )
        }
    ) { padding ->
        // Конфетти при правильном ответе + тактильный фидбэк (v1.22.8 — pulse/tick)
        var confettiKey by remember { mutableIntStateOf(0) }
        val hapticVm: com.spanishapp.ui.components.HapticPrefViewModel =
            androidx.hilt.navigation.compose.hiltViewModel()
        val hapticPercent by hapticVm.intensity.collectAsStateWithLifecycle()
        LaunchedEffect(state.answerHistory.size) {
            when (state.lastCorrect) {
                true  -> {
                    confettiKey++
                    hapticVm.vibrator.pulse(hapticPercent)
                }
                false -> hapticVm.vibrator.tick((hapticPercent * 130 / 100).coerceAtMost(100))
                else  -> {}
            }
        }

        // Тряска карточки при ошибке
        val shakeX = rememberShakeOffset(
            trigger = state.answerHistory.size,
            isWrong = state.lastCorrect == false
        )

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(MaterialTheme.colorScheme.background)
        ) {
            // v1.13.2: full-width — клавиатура внизу должна быть на
            // всю ширину для удобства тапа. Cap нежелателен.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Шапка: счёт + раунд
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.math_score, state.score), fontWeight = FontWeight.Bold, color = ACCENT)
                    Text(stringResource(R.string.math_round_of, state.currentRound, state.totalRounds), color = Color.Gray)
                }

                // Точки прогресса
                ProgressDots(
                    history = state.answerHistory,
                    total   = state.totalRounds,
                    accent  = ACCENT
                )

                // Комбо-бейдж (от 3+)
                AnimatedVisibility(
                    visible = state.streak >= 3,
                    enter = fadeIn(), exit = fadeOut()
                ) {
                    ComboBadge(streak = state.streak, accentColor = ACCENT)
                }

                // Таймер
                if (state.params.timePerRoundSec > 0f) {
                    LinearProgressIndicator(
                        progress = { state.timeLeft },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (state.timeLeft < 0.3f) Color.Red else ACCENT,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }

                // Выражение + кнопка озвучки (с тряской при ошибке)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(x = shakeX.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            state.expressionText,
                            fontSize = if (state.displayMode == MathDisplayMode.AUDIO) 56.sp else 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = if (state.displayMode == MathDisplayMode.AUDIO) 60.sp else 30.sp,
                            color = if (state.displayMode == MathDisplayMode.AUDIO) ACCENT
                                    else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        IconButton(onClick = { viewModel.repeatQuestion() }) {
                            Icon(Icons.Default.Replay, stringResource(R.string.math_repeat),
                                tint = ACCENT, modifier = Modifier.size(28.dp))
                        }
                    }
                }

            // Поле ввода / результат
            Box(modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (state.lastCorrect != null) {
                    val ok = state.lastCorrect == true
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            if (ok) Icons.Default.Check else Icons.Default.Close, null,
                            tint = if (ok) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                        Text(
                            if (ok) "¡Excelente!" else "Incorrecto (era ${state.correctAnswer})",
                            fontWeight = FontWeight.Bold,
                            color = if (ok) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .width(140.dp)
                            .height(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                inputVal.ifEmpty { "?" },
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (inputVal.isEmpty()) Color.LightGray else ACCENT
                            )
                        }
                    }
                }
            }

            // Цифровая клавиатура
            MathKeypad(
                onDigit = { d ->
                    if (state.lastCorrect == null && inputVal.length < 5) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onInput(inputVal + d)
                    }
                },
                onDelete = {
                    if (state.lastCorrect == null && inputVal.isNotEmpty()) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onInput(inputVal.dropLast(1))
                    }
                },
                onClear = {
                    if (state.lastCorrect == null) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onInput("")
                    }
                },
                onSubmit = {
                    if (state.lastCorrect == null && inputVal.isNotBlank()) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.submitAnswer(inputVal.toIntOrNull())
                        onInput("")
                    }
                },
                enabled = state.lastCorrect == null
            )
            } // Column

            // Конфетти как оверлей поверх всего экрана
            ConfettiEffect(trigger = confettiKey)
        } // Box
    }
}

@Composable
private fun MathKeypad(
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    enabled: Boolean
) {
    // v1.13.4: keypad крупнее на планшете. Юзер: "Кнопки выглядят
    // слишком плоско, на планшете сложно тянуться к верху чтобы
    // нажать кнопку." Делаем кнопки выше + ниже на экране.
    val isWide = com.spanishapp.ui.adaptive.isWideScreen()
    val keyHeight = if (isWide) 88.dp else 60.dp
    val keyFont = if (isWide) 28.sp else 20.sp
    val submitHeight = if (isWide) 76.dp else 56.dp
    val submitFont = if (isWide) 22.sp else 18.sp
    val keySpacing = if (isWide) 12.dp else 8.dp
    val maxKeypadWidth = if (isWide) 560.dp else androidx.compose.ui.unit.Dp.Unspecified

    Column(
        modifier = if (isWide) Modifier.fillMaxWidth().widthIn(max = maxKeypadWidth) else Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(keySpacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("C", "0", "DEL")
        )

        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(keySpacing, Alignment.CenterHorizontally)) {
                row.forEach { char ->
                    KeyButton(
                        text = char,
                        modifier = Modifier
                            .weight(1f)
                            .height(keyHeight),
                        textSize = keyFont,
                        onClick = {
                            when (char) {
                                "C"   -> onClear()
                                "DEL" -> onDelete()
                                else  -> onDigit(char)
                            }
                        },
                        enabled = enabled,
                        isAction = char == "C" || char == "DEL"
                    )
                }
            }
        }

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(submitHeight)
                .padding(top = 4.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(containerColor = ACCENT)
        ) {
            Text(stringResource(R.string.math_submit), fontSize = submitFont, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun KeyButton(
    text: String,
    modifier: Modifier,
    onClick: () -> Unit,
    enabled: Boolean,
    isAction: Boolean = false,
    textSize: androidx.compose.ui.unit.TextUnit = 22.sp,
) {
    Surface(
        modifier = modifier.clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isAction) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceContainerHighest,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (text == "DEL") {
                Icon(Icons.Default.Backspace, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(if (textSize.value >= 24f) 32.dp else 24.dp))
            } else {
                Text(
                    text,
                    fontSize = textSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
