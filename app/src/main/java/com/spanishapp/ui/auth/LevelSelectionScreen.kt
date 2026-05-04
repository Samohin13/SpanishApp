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
import kotlinx.coroutines.launch

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

// TODO: Удалить PREMIUM_LEVELS когда контент A2/B1/B2 будет готов
private val PREMIUM_LEVELS = setOf("A2", "B1", "B2")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectionScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var selected by remember { mutableStateOf<String?>(null) }
    var sheetLevel by remember { mutableStateOf<LevelOption?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun confirmAndGo(levelCode: String) {
        viewModel.selectLevel(levelCode)
        viewModel.completeOnboarding()
        navController.navigate("home") {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BgWhite)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Text(
            "Выбери свой уровень",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text("Можно изменить позже в настройках", fontSize = 13.sp, color = AppColors.TextSecondary)
        Spacer(Modifier.height(32.dp))

        LEVELS.forEach { level ->
            val isSelected = selected == level.code
            val isPremium = level.code in PREMIUM_LEVELS // TODO: удалить когда контент готов

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
                    .clickable {
                        selected = level.code
                        if (isPremium) {
                            sheetLevel = level
                            scope.launch { sheetState.show() }
                        }
                    }
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(level.emoji, fontSize = 28.sp)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(level.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(level.subtitle, fontSize = 13.sp, color = AppColors.TextSecondary)
                    }
                    // TODO: удалить иконку замка когда контент готов
                    if (isPremium) {
                        Text("🔒", fontSize = 18.sp)
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                selected?.let { levelCode ->
                    if (levelCode in PREMIUM_LEVELS) {
                        val lvl = LEVELS.find { it.code == levelCode }
                        sheetLevel = lvl
                        scope.launch { sheetState.show() }
                    } else {
                        confirmAndGo(levelCode)
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

    // TODO: Удалить весь этот ModalBottomSheet когда контент A2/B1/B2 будет готов
    sheetLevel?.let { level ->
        ModalBottomSheet(
            onDismissRequest = {
                sheetLevel = null
                selected = "A1"
            },
            sheetState = sheetState,
            containerColor = AppColors.BgWhite
        ) {
            PremiumLevelSheet(
                level = level,
                onConfirm = {
                    scope.launch { sheetState.hide() }
                    sheetLevel = null
                    confirmAndGo(level.code)
                },
                onDismiss = {
                    scope.launch { sheetState.hide() }
                    sheetLevel = null
                    selected = "A1"
                }
            )
        }
    }
}

// TODO: Удалить этот composable когда контент A2/B1/B2 будет готов
@Composable
private fun PremiumLevelSheet(
    level: LevelOption,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🚧", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "Уровень ${level.code} в разработке",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Мы активно работаем над программой ${level.code}.\nСкоро она появится в приложении!",
            fontSize = 14.sp,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(24.dp))

        // Что будет при выходе
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.PurplePale, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Column {
                Text("Когда выйдет:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Row {
                    Text("✅  ", fontSize = 14.sp)
                    Text("1–3 урока бесплатно для всех", fontSize = 14.sp)
                }
                Spacer(Modifier.height(4.dp))
                Row {
                    Text("⭐  ", fontSize = 14.sp)
                    Text("Полный доступ по подписке", fontSize = 14.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Тарифы
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PriceCard(
                modifier = Modifier.weight(1f),
                label = "Месяц",
                price = "$4.99",
                note = "в месяц"
            )
            PriceCard(
                modifier = Modifier.weight(1f),
                label = "Год",
                price = "$29.99",
                note = "≈ $2.50 / мес",
                highlighted = true
            )
        }

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple)
        ) {
            Text("Всё равно выбрать ${level.code}", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Начать с A1 (бесплатно)", color = AppColors.TextSecondary)
        }
    }
}

@Composable
private fun PriceCard(
    modifier: Modifier = Modifier,
    label: String,
    price: String,
    note: String,
    highlighted: Boolean = false
) {
    val bg = if (highlighted) AppColors.Purple else Color.White
    val border = if (highlighted) AppColors.Purple else AppColors.BorderColor
    val textColor = if (highlighted) Color.White else AppColors.TextPrimary
    val noteColor = if (highlighted) Color.White.copy(alpha = 0.8f) else AppColors.TextSecondary

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (highlighted) {
            Text("🔥 Выгодно", fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
            Spacer(Modifier.height(4.dp))
        }
        Text(label, fontSize = 13.sp, color = noteColor)
        Spacer(Modifier.height(4.dp))
        Text(price, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
        Text(note, fontSize = 11.sp, color = noteColor)
    }
}
