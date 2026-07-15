package com.spanishapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
 * Минимальная плашка «доступно обновление» по запросу владельца.
 * Без описания фич — только версия и 2 кнопки.
 *
 * Показывается из MainActivity после успешной проверки
 * [AppUpdateChecker.checkForUpdate].
 *
 * UX:
 *   - Закрыть нельзя свайпом / тапом снаружи (dismissOnBackPress=true,
 *     но dismissOnClickOutside=false) — чтобы юзер сделал явный выбор.
 *   - «Позже» — скрыть на сессию.
 *   - «Обновить» — Play Store flexible update flow.
 */
@Composable
fun UpdatePromptDialog(
    availableVersion: Int,
    onLater: () -> Unit,
    onUpdate: () -> Unit,
) {
    Dialog(
        onDismissRequest = onLater,
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        )
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Доступно обновление",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Версия $availableVersion",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onLater,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Позже", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onUpdate,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6B1A),
                            contentColor = Color.White,
                        ),
                    ) {
                        Text("Обновить", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

/**
 * v1.26.2: второй шаг flexible-обновления — пакет скачан, для установки
 * нужен перезапуск ([AppUpdateChecker.completeUpdate]). Без этого шага
 * скачанное обновление никогда не устанавливалось, а повторный тап
 * «Обновить» ловил ошибку Play «не может установиться/обновиться».
 */
@Composable
fun UpdateReadyDialog(
    onLater: () -> Unit,
    onRestart: () -> Unit,
) {
    Dialog(
        onDismissRequest = onLater,
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        )
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Обновление готово",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Перезапустим приложение, чтобы установить новую версию",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onLater,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Позже", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onRestart,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6B1A),
                            contentColor = Color.White,
                        ),
                    ) {
                        Text("Перезапустить", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}
