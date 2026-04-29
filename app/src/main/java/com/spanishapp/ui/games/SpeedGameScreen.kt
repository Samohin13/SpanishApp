package com.spanishapp.ui.games

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
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
fun SpeedGameScreen(
    navController: NavHostController,
    viewModel: SpeedViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showSetup by remember { mutableStateOf(true) }
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rápido: Когнитивный спринт") },
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
            if (showSetup) {
                SpeedSetupContent { speed, cefr ->
                    viewModel.startGame(speed, cefr)
                    showSetup = false
                }
            } else if (state.isGameOver) {
                SpeedResultContent(state) {
                    navController.popBackStack()
                }
            } else {
                SpeedGameContent(state, viewModel, haptic)
            }
        }
    }
}

@Composable
fun SpeedSetupContent(onStart: (SpeedLevel, String) -> Unit) {
    var selectedSpeed by remember { mutableStateOf(SpeedLevel.LENTO) }
    var selectedCefr by remember { mutableStateOf("A1") }

    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Выберите режим скорости", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SpeedLevel.entries.forEach { level ->
                FilterChip(
                    selected = selectedSpeed == level,
                    onClick = { selectedSpeed = level },
                    label = { Text(level.label) }
                )
            }
        }

        Text("Сложность лексики", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("A1", "A2", "B1", "B2", "C1").forEach { level ->
                FilterChip(
                    selected = selectedCefr == level,
                    onClick = { selectedCefr = level },
                    label = { Text(level) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onStart(selectedSpeed, selectedCefr) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("¡Vamos!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SpeedGameContent(state: SpeedPremiumState, viewModel: SpeedViewModel, haptic: HapticFeedback) {
    // Pulse haptic if time is low
    LaunchedEffect(state.timeLeft) {
        if (state.timeLeft in 0.01f..0.2f && !state.isGameOver) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalFireDepartment, null, tint = Color(0xFFFF9500))
                Text("${state.streak}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                if (state.multiplier > 1f) {
                    Text(" x${state.multiplier}", color = Color(0xFFFF9500), fontWeight = FontWeight.Bold)
                }
            }
            Text("XP: ${state.score}", fontWeight = FontWeight.Bold, color = Color(0xFF7B2FBE))
        }

        Spacer(Modifier.height(16.dp))

        // Timer
        LinearProgressIndicator(
            progress = { state.timeLeft },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
            color = if (state.timeLeft < 0.3f) Color.Red else Color(state.level.color),
            trackColor = Color(0xFFE5E5EA)
        )

        Spacer(Modifier.weight(0.5f))

        // Word
        state.currentWord?.let { word ->
            Text(
                word.spanish,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 48.sp
            )
        }

        Spacer(Modifier.weight(0.5f))

        // Options
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.options.forEach { option ->
                val isCorrect = state.lastCorrect != null && option == state.currentWord?.russian
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clickable(enabled = state.lastCorrect == null) { viewModel.submitAnswer(option) },
                    shape = RoundedCornerShape(16.dp),
                    color = when {
                        isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        state.lastCorrect == false && option == state.currentWord?.russian -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                        else -> Color.White
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when {
                            isCorrect -> Color(0xFF4CAF50)
                            else -> Color(0xFFE5E5EA)
                        }
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(option, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun SpeedResultContent(state: SpeedPremiumState, onFinish: () -> Unit) {
    val avgTime = if (state.reactionTimes.isNotEmpty()) state.reactionTimes.average().toInt() else 0
    
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚡", fontSize = 72.sp)
        Text("Результаты спринта", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        
        Spacer(Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ResultStat("XP", "${state.score}")
            ResultStat("Ср. время", "${avgTime}мс")
            ResultStat("Серия", "${state.streak}")
        }

        if (state.weakWords.isNotEmpty()) {
            Spacer(Modifier.height(32.dp))
            Text("Зоны роста:", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
            state.weakWords.distinctBy { it.id }.take(5).forEach { word ->
                Text("• ${word.spanish} — ${word.russian}", color = Color.Gray, modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("В меню")
        }
    }
}

@Composable
fun ResultStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF7B2FBE))
    }
}
