package com.spanishapp.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.spanishapp.ui.theme.AppColors

private data class LevelOption(
    val code: String,
    val emoji: String,
    val title: String,
    val subtitle: String
)

private val LEVELS = listOf(
    LevelOption("A1", "🌱", "A1 — Новичок", "Первые слова и фразы"),
    LevelOption("A2", "⭐", "A2 — Основы", "Простые разговоры и покупки"),
    LevelOption("B1", "🚀", "B1 — Средний", "Свободное общение на большинство тем"),
    LevelOption("B2", "🏆", "B2 — Выше среднего", "Сложные тексты и дискуссии"),
)

@Composable
fun LevelSelectionScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var selected by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BgWhite)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Text("Выбери свой уровень", fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("Можно изменить позже в настройках", fontSize = 13.sp, color = AppColors.TextSecondary)
        Spacer(Modifier.height(32.dp))

        LEVELS.forEach { level ->
            val isSelected = selected == level.code
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) AppColors.PurplePale else Color.White)
                    .border(
                        2.dp,
                        if (isSelected) AppColors.Purple else AppColors.BorderColor,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { selected = level.code }
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(level.emoji, fontSize = 28.sp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(level.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(level.subtitle, fontSize = 13.sp, color = AppColors.TextSecondary)
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                selected?.let { level ->
                    viewModel.selectLevel(level)
                    viewModel.completeOnboarding()
                    navController.navigate("home") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple),
            enabled = selected != null
        ) {
            Text("Подтвердить", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
