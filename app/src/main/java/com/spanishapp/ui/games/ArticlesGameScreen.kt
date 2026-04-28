package com.spanishapp.ui.games

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
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
fun ArticlesGameScreen(
    navController: NavHostController,
    viewModel: ArticlesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showLevelSelection by remember { mutableStateOf(true) }
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artículos Premium") },
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
            if (showLevelSelection) {
                ArticleLevelSelection { level ->
                    viewModel.startGame(level)
                    showLevelSelection = false
                }
            } else if (state.isGameOver) {
                ArticleGameOverContent(state.score) {
                    navController.popBackStack()
                }
            } else {
                ArticlesGameContent(state, viewModel, haptic)
            }
        }
    }
}

@Composable
fun ArticleLevelSelection(onSelect: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Выберите уровень (CEFR)", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        
        LevelButton("A1 (Génesis)", "Базовые окончания -o/-a", Color(0xFF43A047)) { onSelect("A1") }
        LevelButton("A2 (Desafío)", "Частотные аномалии и исключения", Color(0xFF1E88E5)) { onSelect("A2") }
        LevelButton("B1 (Estructura)", "Морфология суффиксов (-ma, -dad)", Color(0xFF7B1FA2)) { onSelect("B1") }
        LevelButton("B2 (Dominio)", "Фонетическая эстетика (Á/HA)", Color(0xFFFBC02D)) { onSelect("B2") }
        LevelButton("C1 (Maestría)", "Семантическая точность (Омонимы)", Color(0xFFD32F2F)) { onSelect("C1") }
    }
}

@Composable
fun LevelButton(title: String, desc: String, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(desc, color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun ArticlesGameContent(state: ArticlesPremiumState, viewModel: ArticlesViewModel, haptic: HapticFeedback) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Stats Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("XP: ${state.score}", fontWeight = FontWeight.Bold, color = Color(0xFF7B2FBE))
                if (state.streak > 0) {
                    Text("Streak: ${state.streak} (x${state.multiplier})", color = Color(0xFFFF9500), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Text("Уровень: ${state.level}", color = Color.Gray)
        }

        // Progress bar
        LinearProgressIndicator(
            progress = { state.currentRound.toFloat() / state.totalRounds },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = Color(0xFF7B2FBE),
            trackColor = Color(0xFFE5E5EA)
        )

        Spacer(Modifier.height(16.dp))

        // Word Card
        state.currentWord?.let { word ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        word.word,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Academic Hint
        Box(modifier = Modifier.height(80.dp), contentAlignment = Alignment.Center) {
            if (state.academicHint != null) {
                Surface(
                    color = Color(0xFFFFF9C4),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = Color(0xFFFBC02D))
                        Spacer(Modifier.width(8.dp))
                        Text(state.academicHint, fontSize = 14.sp, color = Color(0xFF5D4037))
                    }
                }
            } else if (state.lastCorrect != null) {
                val isCorrect = state.lastCorrect ?: false
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                        null,
                        tint = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                    Text(
                        if (isCorrect) "¡Excelente!" else "Incorrecto",
                        fontWeight = FontWeight.Bold,
                        color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Premium Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ArticleButton(
                label = "EL",
                gradient = Brush.verticalGradient(listOf(Color(0xFF2196F3), Color(0xFF1976D2))),
                onClick = { 
                    viewModel.submitAnswer("el")
                    triggerHaptic(haptic, state.currentWord?.article == "el")
                },
                enabled = state.lastCorrect == null,
                modifier = Modifier.weight(1f)
            )
            ArticleButton(
                label = "LA",
                gradient = Brush.verticalGradient(listOf(Color(0xFFFF8A65), Color(0xFFD84315))),
                onClick = { 
                    viewModel.submitAnswer("la")
                    triggerHaptic(haptic, state.currentWord?.article == "la")
                },
                enabled = state.lastCorrect == null,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ArticleButton(label: String, gradient: Brush, onClick: () -> Unit, enabled: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(80.dp).clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(if (enabled) gradient else Brush.verticalGradient(listOf(Color.LightGray, Color.Gray))),
            contentAlignment = Alignment.Center
        ) {
            Text(label, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
    }
}

fun triggerHaptic(haptic: HapticFeedback, success: Boolean) {
    if (success) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress) // Light "tick"
    } else {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress) // Double would be nice but limited here
    }
}

@Composable
fun ArticleGameOverContent(score: Int, onFinish: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🏆", fontSize = 72.sp)
        Text("Сессия завершена", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Ваш результат: $score XP", color = Color.Gray)
        
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
