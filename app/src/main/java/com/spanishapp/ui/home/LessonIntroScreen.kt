package com.spanishapp.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun LessonIntroScreen(
    navController: NavHostController,
    title: String,
    type: String
) {
    val haptic = LocalHapticFeedback.current
    
    // Анимация масштаба для иконки
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val accentColor = when(type) {
        "vocab" -> MaterialTheme.colorScheme.primary
        "grammar" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiary
    }

    val icon = when(type) {
        "vocab" -> "📚"
        "grammar" -> "🧩"
        else -> "🏆"
    }

    val description = when(type) {
        "vocab" -> "Изучи новые слова и фразы для общения. Мы подобрали самые важные выражения для этой темы."
        "grammar" -> "Разберись, как строятся предложения. Грамматика — это скелет языка."
        else -> "Проверь свои знания! Пройди финальный тест, чтобы открыть следующий раздел."
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Эстетичное свечение сзади иконки
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(scale)
                        .background(accentColor.copy(alpha = 0.1f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 48.sp)
                }
            }

            Spacer(Modifier.height(48.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(48.dp))

            // Карточка с бонусами за урок
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RewardItem("✨ +15 XP", "Опыт")
                    RewardItem("🔥 +1", "Стрик")
                    RewardItem("🎯 100%", "Цель")
                }
            }

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    // Навигация к самой тренировке
                    val route = if (type == "quiz") "quiz" else "flashcards"
                    navController.navigate(route) {
                        popUpTo("lesson_intro/{title}/{type}") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("ПОЕХАЛИ!", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }

            TextButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Не сейчас", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun RewardItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
