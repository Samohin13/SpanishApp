package com.spanishapp.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

/**
 * Карточка-виджет «Поговори с Lucía» на главном экране.
 *
 * Вставляется в HomeScreen.kt сразу после блока статистики (streak / XP / level).
 * При тапе — навигация на маршрут "ai_chat".
 *
 * Визуально соответствует .ai-widget в мокапе HTML (espeak_chat_v4.html):
 *   • Градиент orange→pink, mеcho тень primary с alpha
 *   • Glass-avatar (🐂) c обводкой и backdrop-blur
 *   • Tag «ИИ-репетитор · online» с пульсирующей точкой
 *   • Цитата на испанском в полупрозрачной плашке
 *   • Stats row снизу + кнопка «Открыть»
 */
@Composable
fun AiTutorWidgetCard(
    navController: NavHostController,
    chatCount: Int = 0,
    level: String = "B1",
    remaining: Int = 50,
    dailyLimit: Int = 50,
    modifier: Modifier = Modifier,
) {
    val primary = Color(0xFFFF6B35)
    val primary2 = Color(0xFFFF7E45)
    val pink = Color(0xFFD62867)
    val success = Color(0xFF4ADE80)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { navController.navigate("ai_chat") },
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        shadowElevation = 8.dp,
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(primary, primary2, pink),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(900f, 700f),
                    )
                )
                .padding(18.dp),
        ) {
            Column {
                // ── TOP: avatar + tag + title ──
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.3f),
                                        Color.White.copy(alpha = 0.05f),
                                    )
                                ),
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("🐂", fontSize = 26.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        LiveTag(success)
                        Text(
                            "Поговори с Lucía",
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 22.sp,
                            letterSpacing = (-0.4).sp,
                        )
                        Text(
                            "Твой помощник в мире испанского",
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ── QUOTE ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.22f))
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💬", fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "¿De qué quieres hablar hoy?",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── FOOTER: stats + open button ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                        StatChip("💬 $chatCount")
                        StatChip("🎯 $level")
                        StatChip("⚡ $remaining/$dailyLimit")
                    }

                    OpenButton(onClick = { navController.navigate("ai_chat") }, primary = primary)
                }
            }
        }
    }
}

@Composable
private fun LiveTag(success: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.22f))
            .padding(horizontal = 9.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        val transition = rememberInfiniteTransition(label = "dot")
        val alpha by transition.animateFloat(
            initialValue = 0.7f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "dotAlpha",
        )
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(success.copy(alpha = alpha))
        )
        Text(
            "ИИ-РЕПЕТИТОР · ONLINE",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.6.sp,
        )
    }
}

@Composable
private fun StatChip(text: String) {
    Text(
        text,
        color = Color.White.copy(alpha = 0.92f),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun OpenButton(onClick: () -> Unit, primary: Color) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        modifier = Modifier.clickable { onClick() },
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                "Открыть",
                color = primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text("→", color = primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
