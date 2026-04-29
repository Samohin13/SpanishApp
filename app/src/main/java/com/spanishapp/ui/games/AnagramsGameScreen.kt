package com.spanishapp.ui.games

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AnagramsGameScreen(
    navController: NavHostController,
    viewModel: AnagramViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showSetup by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Palabra Maestra") },
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
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Сложность слов", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    listOf("A1", "A2", "B1", "B2", "C1").forEach { level ->
                        Button(
                            onClick = { 
                                viewModel.startGame(level)
                                showSetup = false
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(level, fontSize = 18.sp)
                        }
                    }
                }
            } else {
                AnagramGameContent(state, viewModel)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnagramGameContent(state: AnagramPremiumState, viewModel: AnagramViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            state.translation.uppercase(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF7B2FBE)
        )
        
        Spacer(Modifier.height(40.dp))

        // Slots
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.assembledLetters.forEachIndexed { index, letter ->
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .padding(2.dp)
                        .clickable { viewModel.undo() },
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        state.isCorrect == true -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        state.isCorrect == false -> Color(0xFFF44336).copy(alpha = 0.2f)
                        else -> Color.White
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, 
                        if (state.isCorrect == true) Color(0xFF4CAF50) else Color(0xFFE5E5EA)
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            letter ?: "",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(48.dp))

        // Letters
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.shuffledLetters.forEachIndexed { index, letter ->
                if (letter.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .size(50.dp)
                            .padding(4.dp)
                            .clickable { viewModel.onLetterClick(index) },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(letter, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Spacer(Modifier.size(50.dp).padding(4.dp))
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Hint
        if (state.hint.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFF9C4))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Lightbulb, null, tint = Color(0xFFFBC02D))
                Spacer(Modifier.width(8.dp))
                Text(state.hint, fontSize = 14.sp, color = Color(0xFF5D4037))
            }
        }
    }
}
