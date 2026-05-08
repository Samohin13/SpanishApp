package com.spanishapp.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

// Уровни, контент для которых ещё в активной разработке.
// Юзер их видит, может выбрать — но получит подсказку «контент скоро,
// пока пользуйся A1». Никаких фейковых цен / подписки тут нет.
private val UPCOMING_LEVELS = setOf("A2", "B1", "B2")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectionScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var selected by remember { mutableStateOf<String?>(null) }
    var infoLevel by remember { mutableStateOf<LevelOption?>(null) }

    fun finalize(levelCode: String) {
        viewModel.selectLevel(levelCode)
        viewModel.completeOnboarding()
        navController.navigate("home") {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Выбор уровня") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppColors.BgWhite)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                androidx.compose.ui.res.stringResource(com.spanishapp.R.string.level_select_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                androidx.compose.ui.res.stringResource(com.spanishapp.R.string.level_select_change_later),
                fontSize = 13.sp,
                color = AppColors.TextSecondary
            )
            Spacer(Modifier.height(32.dp))

            LEVELS.forEach { level ->
                val isSelected = selected == level.code
                val isUpcoming = level.code in UPCOMING_LEVELS

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
                        Column(Modifier.weight(1f)) {
                            Text(level.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(level.subtitle, fontSize = 13.sp, color = AppColors.TextSecondary)
                        }
                        if (isUpcoming) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFF3E0)
                            ) {
                                Text(
                                    androidx.compose.ui.res.stringResource(com.spanishapp.R.string.level_upcoming_badge),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFE65100),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    val code = selected ?: return@Button
                    if (code in UPCOMING_LEVELS) {
                        // Сначала покажем диалог что контент в разработке.
                        infoLevel = LEVELS.first { it.code == code }
                    } else {
                        finalize(code)
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

    infoLevel?.let { level ->
        UpcomingLevelDialog(
            level = level,
            onChooseAnyway = {
                infoLevel = null
                finalize(level.code)
            },
            onSwitchToA1 = {
                infoLevel = null
                selected = "A1"
                finalize("A1")
            },
            onDismiss = { infoLevel = null }
        )
    }
}

@Composable
private fun UpcomingLevelDialog(
    level: LevelOption,
    onChooseAnyway: () -> Unit,
    onSwitchToA1: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⏳", fontSize = 36.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    androidx.compose.ui.res.stringResource(com.spanishapp.R.string.level_upcoming_dialog_title, level.code),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Text(
                "Программа обучения для ${level.code} ещё дополняется. Если ты хочешь начать сразу, можешь выбрать ${level.code} — но материала пока меньше, чем на A1.\n\nРекомендуем начать с A1 и переключиться позже в настройках.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = AppColors.TextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onSwitchToA1) {
                Text(androidx.compose.ui.res.stringResource(com.spanishapp.R.string.level_upcoming_continue_a1), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onChooseAnyway) {
                Text(androidx.compose.ui.res.stringResource(com.spanishapp.R.string.level_upcoming_anyway, level.code))
            }
        }
    )
}
