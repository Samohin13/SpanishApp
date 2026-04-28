package com.spanishapp.ui.games

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathGameScreen(
    navController: NavHostController,
    viewModel: MathViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showDifficultySelection by remember { mutableStateOf(true) }
    var inputVal by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cálculo Auditivo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F8FA)),
            contentAlignment = Alignment.Center
        ) {
            if (showDifficultySelection) {
                DifficultySelection { diff ->
                    viewModel.startGame(diff)
                    showDifficultySelection = false
                }
            } else if (state.isGameOver) {
                MathGameOverContent(state.score) {
                    navController.popBackStack()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header: Score and Streak
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Очки: ${state.score}", fontWeight = FontWeight.Bold, color = Color(0xFF7B2FBE))
                            if (state.streak > 1) {
                                Text("Комбо: x${state.streak}", color = Color(0xFFFF9500), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("Раунд: ${state.currentRound}/${state.totalRounds}", color = Color.Gray)
                    }

                    // Timer
                    LinearProgressIndicator(
                        progress = { state.timeLeft },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (state.timeLeft < 0.3f) Color.Red else Color(0xFFE040FB),
                        trackColor = Color(0xFFE5E5EA)
                    )

                    // Expression
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                state.expressionText,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 28.sp
                            )
                            if (state.level == "B2-C1") {
                                Spacer(Modifier.height(8.dp))
                                IconButton(
                                    onClick = { viewModel.repeatQuestion() },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, "Listen", tint = Color(0xFF7B2FBE))
                                }
                            }
                        }
                    }

                    // Feedback or Input Display
                    Box(modifier = Modifier.height(50.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        if (state.lastCorrect != null) {
                            val isCorrect = state.lastCorrect ?: false
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                                    null,
                                    tint = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
                                )
                                Text(
                                    if (isCorrect) "¡Excelente!" else "Incorrecto (era ${state.correctAnswer})",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5EA)),
                                modifier = Modifier.width(140.dp).height(50.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        inputVal.ifEmpty { "?" },
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (inputVal.isEmpty()) Color.LightGray else Color(0xFF7B2FBE)
                                    )
                                }
                            }
                        }
                    }

                    // Keypad
                    MathKeypad(
                        onDigit = { digit ->
                            if (state.lastCorrect == null && inputVal.length < 5) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                inputVal += digit
                            }
                        },
                        onDelete = {
                            if (state.lastCorrect == null && inputVal.isNotEmpty()) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                inputVal = inputVal.dropLast(1)
                            }
                        },
                        onClear = {
                            if (state.lastCorrect == null) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                inputVal = ""
                            }
                        },
                        onSubmit = {
                            if (state.lastCorrect == null && inputVal.isNotBlank()) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val ans = inputVal.toIntOrNull()
                                viewModel.submitAnswer(ans)
                                inputVal = ""
                            }
                        },
                        enabled = state.lastCorrect == null
                    )
                }
            }
        }
    }
}

@Composable
fun MathKeypad(
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                row.forEach { char ->
                    KeyButton(
                        text = char,
                        modifier = Modifier.weight(1f).height(60.dp),
                        onClick = {
                            when (char) {
                                "C" -> onClear()
                                "DEL" -> onDelete()
                                else -> onDigit(char)
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
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 4.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B2FBE))
        ) {
            Text("ОТВЕТИТЬ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun KeyButton(
    text: String,
    modifier: Modifier,
    onClick: () -> Unit,
    enabled: Boolean,
    isAction: Boolean = false
) {
    Surface(
        modifier = modifier.clickable(enabled = enabled) { onClick() },
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

@Composable
fun DifficultySelection(onSelect: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Выберите сложность", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        
        DifficultyButton("Básico (A1)", "Числа 0-20, + и -", Color(0xFF4CAF50)) { onSelect("A1") }
        DifficultyButton("Medio (A2-B1)", "Числа до 100, x и /", Color(0xFF2196F3)) { onSelect("A2-B1") }
        DifficultyButton("Experto (B2-C1)", "Комбинированные задачи", Color(0xFFE91E63)) { onSelect("B2-C1") }
    }
}

@Composable
fun DifficultyButton(title: String, desc: String, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(color))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(desc, color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun MathGameOverContent(score: Int, onFinish: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🧮", fontSize = 64.sp)
        Text("Математика окончена!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(
            "Ваш результат: $score очков",
            fontSize = 18.sp,
            color = Color.Gray
        )
        Text(
            "+${(score / 5).coerceAtLeast(5)} XP",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF7B2FBE)
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onFinish,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.width(200.dp).height(50.dp)
        ) {
            Text("В меню")
        }
    }
}
