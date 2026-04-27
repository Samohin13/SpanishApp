package com.spanishapp.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*

@Composable
fun LessonIntroScreen(
    navController: NavHostController,
    title: String,
    type: String,
    category: String = "all"
) {
    val haptic = LocalHapticFeedback.current
    
    // Lottie Animation
    val lottieUrl = when(type) {
        "vocab" -> "https://lottie.host/575239a2-5b92-491c-99c5-84631383777f/2mInRjJ968.json" // Book/Study
        "grammar" -> "https://lottie.host/8e3126f5-5730-4e3a-9653-5d51d1822c95/f4mH8i3K0I.json" // Puzzle
        else -> "https://lottie.host/640103b4-4e14-4112-9e9d-111162d08a0d/7VzD6iE1T2.json" // Trophy/Quiz
    }
    
    val composition by rememberLottieComposition(LottieCompositionSpec.Url(lottieUrl))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )

    val accentColor = when(type) {
        "vocab" -> MaterialTheme.colorScheme.primary
        "grammar" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiary
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
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(accentColor.copy(alpha = 0.05f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(140.dp)
                )
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
                    
                    val route = if (type == "quiz") {
                        "quiz?type=$category"
                    } else if (type == "grammar") {
                        "grammar" // Usually leads to grammar list, but can be specific lesson if needed
                    } else {
                        "flashcards_session?level=A1&category=$category&direction=ES_TO_RU&weak=false"
                    }

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
