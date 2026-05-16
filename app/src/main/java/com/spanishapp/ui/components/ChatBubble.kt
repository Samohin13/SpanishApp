package com.spanishapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Мессенджер-стиль чат-бубль.
 *
 * Используется в:
 *   • DialogueFillInput (упражнения с диалогом)
 *   • CheckpointSessionScreen (NPC реплики)
 *
 * Параметры:
 *   isMine = true  → справа, синий градиент, без хвостика слева
 *   isMine = false → слева, серый/цветной, с хвостиком слева вниз
 *
 * Анимация: scale 0.8→1 + fade-in 200ms при появлении.
 */
@Composable
fun ChatBubble(
    speaker: String,
    text: String,
    isMine: Boolean,
    translation: String = "",
    avatar: String = "",
    accentColor: Color = Color(0xFFFF5722),
    highlightWord: String = "",            // подсвечивается выбранным ответом (например "Bien")
    showTapHint: Boolean = false,           // tap-to-listen иконка
    onTap: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!isMine) {
            ChatAvatar(emoji = avatar, accentColor = accentColor)
            Spacer(Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            // Имя говорящего над бублем
            if (speaker.isNotBlank()) {
                Text(
                    text = speaker,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isMine) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                )
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isMine) 18.dp else 4.dp,
                    bottomEnd = if (isMine) 4.dp else 18.dp,
                ),
                color = Color.Transparent,
                modifier = Modifier.then(
                    if (onTap != null) Modifier
                        .clip(RoundedCornerShape(
                            topStart = 18.dp, topEnd = 18.dp,
                            bottomStart = if (isMine) 18.dp else 4.dp,
                            bottomEnd = if (isMine) 4.dp else 18.dp,
                        ))
                        .background(
                            if (isMine) Brush.linearGradient(
                                listOf(accentColor, accentColor.copy(alpha = 0.85f))
                            ) else Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                )
                            )
                        )
                        .let { it } else Modifier.background(
                            if (isMine) Brush.linearGradient(
                                listOf(accentColor, accentColor.copy(alpha = 0.85f))
                            ) else Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                )
                            )
                        )
                ),
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Текст реплики с возможной подсветкой
                    val displayText = if (highlightWord.isNotBlank())
                        text.replace("___", highlightWord)
                    else text
                    Text(
                        text = displayText,
                        fontSize = 15.sp,
                        color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                    )
                    if (translation.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = translation,
                            fontSize = 11.sp,
                            color = if (isMine) Color.White.copy(alpha = 0.85f)
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        )
                    }
                    if (showTapHint) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "🔊 тапни прослушать",
                            fontSize = 9.sp,
                            color = if (isMine) Color.White.copy(alpha = 0.7f)
                            else accentColor,
                        )
                    }
                }
            }
        }

        if (isMine) {
            Spacer(Modifier.width(8.dp))
            ChatAvatar(emoji = avatar.ifBlank { "🙋" }, accentColor = accentColor, isMine = true)
        }
    }
}

/**
 * Кружок-аватар с эмодзи. 36×36 + accent-border для NPC, белый для меня.
 */
@Composable
fun ChatAvatar(
    emoji: String,
    accentColor: Color,
    isMine: Boolean = false,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                if (isMine) accentColor
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = 2.dp,
                color = if (isMine) accentColor
                else accentColor.copy(alpha = 0.4f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji.ifBlank { "👤" }, fontSize = 18.sp)
    }
}

/**
 * Индикатор «собеседник печатает» — 3 точки с анимацией.
 * Показывается перед появлением новой NPC реплики.
 */
@Composable
fun TypingIndicator(
    avatar: String = "👤",
    accentColor: Color = Color(0xFFFF5722),
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        ChatAvatar(emoji = avatar, accentColor = accentColor)
        Spacer(Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp, topEnd = 18.dp,
                bottomStart = 4.dp, bottomEnd = 18.dp,
            ),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(3) { i ->
                    val infiniteTransition = rememberInfiniteTransition(label = "dot$i")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 600, delayMillis = i * 200, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = alpha))
                    )
                }
            }
        }
    }
}

/**
 * Цветной фон для сцен в зависимости от сеттинга.
 * Используется в CheckpointSessionScreen + DialogueFillInput.
 *
 * Поддерживаемые сеттинги (по emoji):
 *   🍽 — ресторан/кафе  → тёплый оранж
 *   🏨 — отель           → голубой
 *   🚖 — такси/транспорт → зелёный
 *   🏥 — больница/врач  → мятный
 *   🛂 — паспортный/офиц.→ серо-синий
 *   🏠 — дом              → пастельный жёлтый
 *   💼 — работа           → бежевый
 *   ❤ — личное           → розовый
 */
@Composable
fun sceneGradientFor(emoji: String): Brush {
    val (top, bottom) = when {
        emoji.contains("🍽") || emoji.contains("☕") || emoji.contains("🍷")
            -> Color(0xFFFFF4E5) to Color(0xFFFFE0B2)
        emoji.contains("🏨") || emoji.contains("🛏")
            -> Color(0xFFE3F2FD) to Color(0xFFBBDEFB)
        emoji.contains("🚖") || emoji.contains("🚇") || emoji.contains("🚗") || emoji.contains("✈")
            -> Color(0xFFE8F5E9) to Color(0xFFC8E6C9)
        emoji.contains("🏥") || emoji.contains("🤒") || emoji.contains("💊")
            -> Color(0xFFE0F7FA) to Color(0xFFB2EBF2)
        emoji.contains("🛂") || emoji.contains("📋") || emoji.contains("📝")
            -> Color(0xFFECEFF1) to Color(0xFFCFD8DC)
        emoji.contains("🏠") || emoji.contains("🚪") || emoji.contains("✍")
            -> Color(0xFFFFF9C4) to Color(0xFFFFF59D)
        emoji.contains("💼") || emoji.contains("🤝") || emoji.contains("👔")
            -> Color(0xFFEFEBE9) to Color(0xFFD7CCC8)
        emoji.contains("❤") || emoji.contains("💌") || emoji.contains("💕")
            -> Color(0xFFFCE4EC) to Color(0xFFF8BBD0)
        emoji.contains("🌅") || emoji.contains("🌄")
            -> Color(0xFFFFECB3) to Color(0xFFFFCC80)
        emoji.contains("🌙") || emoji.contains("🌃")
            -> Color(0xFFE1BEE7) to Color(0xFFCE93D8)
        emoji.contains("🏆") || emoji.contains("🌟")
            -> Color(0xFFFFF9C4) to Color(0xFFFFD54F)
        else -> Color(0xFFFFFFFF) to Color(0xFFF5F5F5)
    }
    return Brush.verticalGradient(listOf(top, bottom))
}

/**
 * Combo-счётчик в верхнем правом углу.
 * Показывается когда serial ≥ 2. Анимируется на изменение.
 */
@Composable
fun ComboBadge(
    serial: Int,
    accentColor: Color = Color(0xFFFF5722),
) {
    if (serial < 2) return
    val scale by animateFloatAsState(
        targetValue = 1f + (serial.coerceAtMost(10) * 0.05f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "comboScale"
    )
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFC107),
        shadowElevation = 4.dp,
        modifier = Modifier.padding(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("🔥", fontSize = (18 * scale).sp)
            Spacer(Modifier.width(4.dp))
            Text(
                "x$serial",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
            )
        }
    }
}

/**
 * Полноэкранная мини-плашка реакции NPC после ответа.
 * ✅ для правильного, ❌ для неправильного. С анимацией.
 */
@Composable
fun NpcReaction(
    isCorrect: Boolean,
    customText: String = "",
) {
    val (emoji, text, color) = if (isCorrect) {
        Triple("✅", customText.ifBlank { "¡Muy bien!" }, Color(0xFF4CAF50))
    } else {
        Triple("❌", customText.ifBlank { "Casi... Intenta otra vez" }, Color(0xFFFF5252))
    }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(2.dp, color),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(emoji, fontSize = 28.sp)
            Spacer(Modifier.width(12.dp))
            Text(
                text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
