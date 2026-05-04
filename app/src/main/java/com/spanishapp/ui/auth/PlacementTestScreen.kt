package com.spanishapp.ui.auth

import androidx.compose.animation.*
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

private data class Question(
    val text: String,
    val options: List<String>,
    val correctIndex: Int
)

private val QUESTIONS = listOf(
    // A1
    Question("Что значит «familia»?", listOf("Еда", "Семья", "Работа", "Город"), 1),
    Question("Как сказать «Здравствуйте» по-испански?", listOf("Gracias", "Adiós", "Hola", "Por favor"), 2),
    // A2
    Question("«Ayer yo ___ al mercado» (ir)", listOf("voy", "iré", "vaya", "fui"), 3),
    Question("Что значит «¿Cuánto cuesta?»", listOf("Как тебя зовут?", "Который час?", "Где находится?", "Сколько стоит?"), 3),
    // B1
    Question("«Es importante que tú ___ (estudiar)»", listOf("estudias", "estudies", "estudiará", "estudié"), 1),
    Question("Что значит «a lo mejor»?", listOf("Никогда", "Всегда", "Возможно", "Обязательно"), 2),
    // B2
    Question("«No hay mal que por bien no venga» означает:", listOf("Всё проходит", "Чем хуже, тем лучше", "Нет худа без добра", "Удача переменчива"), 2),
    Question("Выберите правильный вариант: «Si hubiera sabido, ___ antes»", listOf("vendría", "vengo", "vine", "habría venido"), 3),
)

private fun calcLevel(correct: Int) = when {
    correct >= 7 -> "B2"
    correct >= 5 -> "B1"
    correct >= 3 -> "A2"
    else -> "A1"
}

@Composable
fun PlacementTestScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var selectedIndex by remember { mutableIntStateOf(-1) }
    var answered by remember { mutableStateOf(false) }

    val question = QUESTIONS[currentIndex]
    val progress = (currentIndex + 1).toFloat() / QUESTIONS.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BgWhite)
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            "Вопрос ${currentIndex + 1} из ${QUESTIONS.size}",
            fontSize = 13.sp,
            color = AppColors.TextSecondary
        )
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = AppColors.Purple,
            trackColor = AppColors.PurplePale
        )

        Spacer(Modifier.height(40.dp))

        Text(
            question.text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 28.sp
        )

        Spacer(Modifier.height(32.dp))

        question.options.forEachIndexed { index, option ->
            val bgColor = when {
                !answered -> if (selectedIndex == index) AppColors.PurplePale else Color.White
                index == question.correctIndex -> Color(0xFFE8F5E9)
                index == selectedIndex && selectedIndex != question.correctIndex -> Color(0xFFFFEBEE)
                else -> Color.White
            }
            val borderColor = when {
                !answered -> if (selectedIndex == index) AppColors.Purple else AppColors.BorderColor
                index == question.correctIndex -> Color(0xFF4CAF50)
                index == selectedIndex && selectedIndex != question.correctIndex -> AppColors.Error
                else -> AppColors.BorderColor
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                    .clickable(enabled = !answered) {
                        selectedIndex = index
                        answered = true
                        if (index == question.correctIndex) correctCount++
                    }
                    .padding(16.dp)
            ) {
                Text(option, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.weight(1f))

        AnimatedVisibility(visible = answered) {
            Button(
                onClick = {
                    if (currentIndex < QUESTIONS.size - 1) {
                        currentIndex++
                        selectedIndex = -1
                        answered = false
                    } else {
                        val level = calcLevel(correctCount)
                        navController.navigate("placement_result/$level") {
                            popUpTo("placement_test") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple)
            ) {
                Text(
                    if (currentIndex < QUESTIONS.size - 1) "Следующий вопрос" else "Узнать результат",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PlacementResultScreen(
    navController: NavHostController,
    level: String,
    viewModel: AuthViewModel = hiltViewModel()
) {

    val (emoji, title, description) = when (level) {
        "B2" -> Triple("🏆", "Впечатляет!", "Ты на продвинутом уровне.\nПрограмма настроена на B2.")
        "B1" -> Triple("🚀", "Ты уже многое знаешь!", "Хороший средний уровень.\nПрограмма настроена на B1.")
        "A2" -> Triple("⭐", "Хорошая база!", "Ты знаешь основы испанского.\nПрограмма настроена на A2.")
        else -> Triple("🌱", "Отличное начало!", "Всё начинается с первого шага.\nПрограмма настроена на A1.")
    }

    val isPremiumLevel = level in listOf("A2", "B1", "B2")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BgWhite)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 72.sp)
        Spacer(Modifier.height(24.dp))
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .background(AppColors.PurplePale, RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                "Уровень $level",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.Purple
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(
            description,
            fontSize = 15.sp,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        if (isPremiumLevel) {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .background(Color(0xFFFFF8E1), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    "⏳ Контент $level скоро появится.\nПока начнём с повторения основ на A1 — это всегда полезно!",
                    fontSize = 13.sp,
                    color = Color(0xFF795548),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = {
                viewModel.selectLevel(level)
                viewModel.completeOnboarding()
                navController.navigate("home") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple)
        ) {
            Text("Начать обучение", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = {
            navController.navigate("level_selection") {
                popUpTo("placement_result/$level") { inclusive = true }
            }
        }) {
            Text("Изменить уровень", color = AppColors.TextSecondary)
        }
    }
}
