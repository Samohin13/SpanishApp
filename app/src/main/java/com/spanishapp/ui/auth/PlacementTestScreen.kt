package com.spanishapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@Composable
fun PlacementTestScreen(navController: NavHostController, viewModel: AuthViewModel = hiltViewModel()) {
    // В реальном приложении здесь был бы список вопросов
    var currentQuestion by remember { mutableStateOf(1) }
    val totalQuestions = 5

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LinearProgressIndicator(
            progress = { currentQuestion.toFloat() / totalQuestions },
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
        )

        Text("Вопрос $currentQuestion из $totalQuestions", color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text(
            "Как правильно перевести 'Я изучаю испанский'?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        val options = listOf("Yo estudio español", "Yo comer manzana", "Hola amigo", "Gracias")
        options.forEach { option ->
            OutlinedButton(
                onClick = { 
                    if (currentQuestion < totalQuestions) {
                        currentQuestion++
                    } else {
                        viewModel.selectLevel("A2") // Имитация результата
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(option)
            }
        }
    }
}
