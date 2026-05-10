package com.spanishapp.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
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
    val subtitle: String,
    val gradientStart: Color,
    val gradientEnd:   Color
)

// Brand colours mirror the CourseCard gradients on HomeScreen so the user
// sees the same level palette they'll see throughout the app.
private val LEVELS = listOf(
    LevelOption("A1", "🌱", "A1 — Новичок",
        "Первые слова и фразы",
        Color(0xFF7C3AED), Color(0xFFA855F7)),
    LevelOption("A2", "⭐", "A2 — Основы",
        "Простые разговоры и покупки",
        Color(0xFF06B6D4), Color(0xFF0EA5E9)),
    LevelOption("B1", "🚀", "B1 — Средний",
        "Свободное общение на большинство тем",
        Color(0xFF22C55E), Color(0xFF16A34A)),
    LevelOption("B2", "🏆", "B2 — Выше среднего",
        "Сложные тексты и дискуссии",
        Color(0xFFF97316), Color(0xFFEA580C)),
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
                title = {
                    com.spanishapp.ui.components.AnimatedScreenTitle(
                        text = "🌟 Выбор уровня",
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(animationSpec = tween(400)) +
                    slideInVertically(animationSpec = tween(400), initialOffsetY = { it / 8 })
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                androidx.compose.ui.res.stringResource(com.spanishapp.R.string.level_select_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                androidx.compose.ui.res.stringResource(com.spanishapp.R.string.level_select_change_later),
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))

            LEVELS.forEach { level ->
                val isSelected = selected == level.code
                val isUpcoming = level.code in UPCOMING_LEVELS

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { selected = level.code },
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = if (isSelected) 12.dp else 8.dp,
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(level.gradientStart, level.gradientEnd)
                                )
                            )
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 22.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(level.emoji, fontSize = 36.sp)
                            Spacer(Modifier.width(18.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    level.title,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 28.sp,
                                    color = Color.White
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    level.subtitle,
                                    fontSize = 15.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (isUpcoming) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.22f)
                                ) {
                                    Text(
                                        androidx.compose.ui.res.stringResource(com.spanishapp.R.string.level_upcoming_badge),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
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
                fontSize = 15.sp,
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
