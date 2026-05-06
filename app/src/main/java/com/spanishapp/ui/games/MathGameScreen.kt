package com.spanishapp.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Replay
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.spanishapp.domain.games.GameId
import com.spanishapp.ui.games.common.LevelCompleteSheet
import com.spanishapp.ui.games.common.LevelMapScreen

private val ACCENT = Color(0xFFF44336)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathGameScreen(
    navController: NavHostController,
    viewModel: MathViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var inputVal by remember(state.currentRound) { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    when {
        state.showLevelMap -> {
            LevelMapScreen(
                gameId  = GameId.MATH,
                title   = "Cálculo · уровни",
                accent  = ACCENT,
                manager = viewModel.levelManager,
                onBack  = { navController.popBackStack() },
                onLevelStart = { lvl -> viewModel.startLevel(lvl) }
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
                onNext  = if (state.finalStars > 0 && state.level < 100)
                              { { viewModel.startLevel(state.level + 1) } }
                          else null,
                onExit  = { viewModel.openLevelMap() }
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
                        Text("Уровень ${state.level} / 100", fontWeight = FontWeight.Bold)
                        Text("${state.params.cefr.joinToString("+")} · ${state.params.mode.name.lowercase()}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            if (state.audioEnabled) Icons.AutoMirrored.Filled.VolumeUp
                            else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = if (state.audioEnabled) "Выключить звук" else "Включить звук",
                            tint = if (state.audioEnabled) ACCENT else Color.Gray
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F8FA))
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Шапка
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Очки: ${state.score}", fontWeight = FontWeight.Bold, color = ACCENT)
                    if (state.streak > 1) {
                        Text("Комбо: ×${state.streak}",
                            color = Color(0xFFFF9500),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold)
                    }
                }
                Text("Раунд ${state.currentRound}/${state.totalRounds}", color = Color.Gray)
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
                    trackColor = Color(0xFFE5E5EA)
                )
            }

            // Выражение + кнопка озвучки
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
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
                                else Color(0xFF1A1A1A)
                    )
                    Spacer(Modifier.height(8.dp))
                    IconButton(onClick = { viewModel.repeatQuestion() }) {
                        Icon(Icons.Default.Replay, "Повторить",
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
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5EA)),
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
        }
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
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                row.forEach { char ->
                    KeyButton(
                        text = char,
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
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
                .height(56.dp)
                .padding(top = 4.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(containerColor = ACCENT)
        ) {
            Text("ОТВЕТИТЬ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun KeyButton(
    text: String,
    modifier: Modifier,
    onClick: () -> Unit,
    enabled: Boolean,
    isAction: Boolean = false
) {
    Surface(
        modifier = modifier.clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isAction) Color(0xFFE5E5EA) else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5EA))
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (text == "DEL") {
                Icon(Icons.Default.Backspace, null, tint = Color.DarkGray)
            } else {
                Text(
                    text,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAction) Color.DarkGray else Color.Black
                )
            }
        }
    }
}
