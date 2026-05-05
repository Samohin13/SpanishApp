package com.spanishapp.ui.games.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * Универсальный диалог «уровень пройден / провален».
 *
 * @param level       номер уровня
 * @param stars       заработанные звёзды (0..3)
 * @param percent     процент правильных ответов
 * @param accent      цвет игры
 * @param onRetry     повторить тот же уровень
 * @param onNext      перейти к следующему (null если уровень не пройден или последний)
 * @param onExit      вернуться на карту уровней
 */
@Composable
fun LevelCompleteSheet(
    level: Int,
    stars: Int,
    percent: Int,
    accent: Color,
    onRetry: () -> Unit,
    onNext: (() -> Unit)?,
    onExit: () -> Unit
) {
    val passed = stars > 0
    Dialog(onDismissRequest = onExit) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .widthIn(max = 320.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (passed) "Уровень $level пройден!" else "Уровень $level не пройден",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (passed) accent else MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(20.dp))

                // 3 звезды
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { i ->
                        Icon(
                            Icons.Default.Star, null,
                            tint = if (i < stars) Color(0xFFFFC107)
                                   else Color(0xFFE5E5EA),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Точность: $percent%",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(24.dp))

                // Кнопки
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onExit,
                        modifier = Modifier.weight(1f)
                    ) { Text("Меню") }

                    OutlinedButton(
                        onClick = onRetry,
                        modifier = Modifier.weight(1f)
                    ) { Text("Снова") }

                    if (passed && onNext != null) {
                        Button(
                            onClick = onNext,
                            modifier = Modifier.weight(1.4f),
                            colors = ButtonDefaults.buttonColors(containerColor = accent)
                        ) { Text("Дальше →") }
                    }
                }
            }
        }
    }
}
