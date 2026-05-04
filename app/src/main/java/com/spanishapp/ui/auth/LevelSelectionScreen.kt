package com.spanishapp.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

data class SpanishLevel(
    val code: String,
    val title: String,
    val description: String,
    val color: Color
)

val levels = listOf(
    SpanishLevel("A1", "Principiante (A1)", "Никогда не изучал испанский или знаю пару слов.", Color(0xFF4CAF50)),
    SpanishLevel("A2", "Elemental (A2)", "Понимаю простые фразы и могу немного говорить.", Color(0xFF2196F3)),
    SpanishLevel("B1", "Intermedio (B1)", "Могу общаться в большинстве ситуаций во время путешествия.", Color(0xFFFF9800)),
    SpanishLevel("B2", "Intermedio Alto (B2)", "Понимаю сложные тексты и свободно общаюсь.", Color(0xFF9C27B0))
)

@Composable
fun LevelSelectionScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var selectedLevelCode by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8FA))
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        
        Text(
            "Какой у тебя уровень?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )
        
        Spacer(Modifier.height(12.dp))
        
        Text(
            "Это поможет нам подобрать подходящие уроки для тебя",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        levels.forEach { level ->
            LevelCard(
                level = level,
                isSelected = selectedLevelCode == level.code,
                onClick = { selectedLevelCode = level.code }
            )
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                selectedLevelCode?.let {
                    viewModel.selectLevel(it)
                    navController.navigate("home") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium,
            enabled = selectedLevelCode != null
        ) {
            Text("Продолжить", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        TextButton(onClick = { navController.navigate("placement_test") }) {
            Text("Не уверен? Пройти тест")
        }
    }
}

@Composable
fun LevelCard(
    level: SpanishLevel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val actualBorderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.4f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(borderWidth, actualBorderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = Color.White,
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(level.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    level.code,
                    color = level.color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column {
                Text(
                    level.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    level.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
