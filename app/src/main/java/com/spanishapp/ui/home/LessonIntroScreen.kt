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
    unitId: Int,
    lessonIndex: Int,
    viewModel: LessonIntroViewModel
) {
    val haptic = LocalHapticFeedback.current

    // Достаём данные из RoadmapData по индексам
    val unit   = remember(unitId) { RoadmapData.units.getOrNull(unitId - 1) }
    val lesson = remember(unit, lessonIndex) { unit?.lessons?.getOrNull(lessonIndex) }

    if (unit == null || lesson == null) {
        navController.popBackStack()
        return
    }

    val lottieUrl = when (lesson.type) {
        "vocab"   -> "https://lottie.host/575239a2-5b92-491c-99c5-84631383777f/2mInRjJ968.json"
        "grammar" -> "https://lottie.host/8e3126f5-5730-4e3a-9653-5d51d1822c95/f4mH8i3K0I.json"
        else      -> "https://lottie.host/640103b4-4e14-4112-9e9d-111162d08a0d/7VzD6iE1T2.json"
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.Url(lottieUrl))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    val accentColor = when (lesson.type) {
        "vocab"   -> MaterialTheme.colorScheme.primary
        "grammar" -> MaterialTheme.colorScheme.secondary
        else      -> MaterialTheme.colorScheme.tertiary
    }

    val description = when (lesson.type) {
        "vocab"   -> "Изучи новые слова и фразы для общения. Мы подобрали самые важные выражения для этой темы."
        "grammar" -> "Разберись, как строятся предложения. Грамматика — это скелет языка."
        "phrase"  -> "Потренируй готовые разговорные фразы — настоящий язык живых людей."
        else      -> "Проверь свои знания! Пройди тест, чтобы открыть следующий раздел."
    }

    val cefrBadge = unit.cefrLevel  // "A1", "A2", "B1", "B2"

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // CEFR badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = cefrBadge,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Spacer(Modifier.height(24.dp))

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

            Spacer(Modifier.height(32.dp))

            Text(
                text = lesson.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = unit.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(32.dp))

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
                    RewardItem("🔓", "Следующий\nурок")
                    RewardItem("🎯", "Прогресс\nблока")
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                    val route = buildActivityRoute(lesson, unit.cefrLevel, unitId, lessonIndex)

                    // Content-уроки сами отмечают себя выполненными (кнопка «Понятно!»)
                    // Остальные — отмечаем здесь при старте
                    if (lesson.type != "content") {
                        viewModel.markLessonComplete(unitId, lessonIndex)
                    }

                    navController.navigate(route) {
                        popUpTo("lesson_intro/{unitId}/{lessonIndex}") { inclusive = true }
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

private fun buildActivityRoute(
    lesson: RoadmapLesson,
    cefrLevel: String,
    unitId: Int,
    lessonIndex: Int
): String {
    val cat = lesson.category
    return when (lesson.type) {
        "content" -> "lesson_content/$unitId/$lessonIndex"
        "vocab"   -> "flashcards_session?level=$cefrLevel&category=$cat&direction=ES_TO_RU"
        "phrase"  -> "flashcards_session?level=$cefrLevel&category=$cat&direction=MIXED"
        "grammar" -> "grammar"
        "quiz"    -> "quiz?type=$cat"
        else      -> "flashcards_session?level=$cefrLevel&category=$cat&direction=ES_TO_RU"
    }
}

@Composable
private fun RewardItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
