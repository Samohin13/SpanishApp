package com.spanishapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

// ── NAVIGATION MODELS ─────────────────────────────────────────

data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val iconSelected: ImageVector
)

val bottomNavItems = listOf(
    NavItem("home",       "Главная",  Icons.Outlined.Home,          Icons.Filled.Home),
    NavItem("flashcards", "Слова",    Icons.Outlined.Style,         Icons.Filled.Style),
    NavItem("games",      "Игры",     Icons.Outlined.SportsEsports, Icons.Filled.SportsEsports),
    NavItem("dictionary", "Словарь",  Icons.AutoMirrored.Outlined.MenuBook, Icons.AutoMirrored.Filled.MenuBook),
    NavItem("profile",    "Профиль",  Icons.Outlined.Person,        Icons.Filled.Person)
)

// ═══════════════════════════════════════════════════════════════
//  SPANISH ATOMIC BACKGROUND (FULL SCREEN DYNAMICS)
// ═══════════════════════════════════════════════════════════════
@Composable
fun SpanishBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val bgColor = MaterialTheme.colorScheme.background
    
    val infiniteTransition = rememberInfiniteTransition(label = "atoms")
    
    // Фаза для глобального движения
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(40000, easing = LinearEasing), RepeatMode.Restart),
        label = "phase"
    )

    val atoms = remember {
        List(25) { i ->
            Atom(
                xStart = (0..100).random() / 100f,
                yStart = (0..100).random() / 100f,
                sizeBase = (40..150).random().toFloat(),
                speedX = (-10..10).random() / 50f, // Дрейф по X
                speedY = (-10..10).random() / 50f, // Дрейф по Y
                color = when (i % 4) {
                    0 -> Color(0xFFFF1744) // Terracotta Neon
                    1 -> Color(0xFFFFEA00) // Golden Neon
                    2 -> Color(0xFF76FF03) // Olive Neon
                    else -> Color(0xFFFF5722) // Orange Neon
                },
                alpha = (0.1f..0.25f).random()
            )
        }
    }

    Box(modifier = modifier.fillMaxSize().background(bgColor)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            atoms.forEach { atom ->
                // Рассчитываем позицию с учетом бесконечного дрейфа (wrap around)
                var x = (width * (atom.xStart + phase * atom.speedX * 5)) % width
                var y = (height * (atom.yStart + phase * atom.speedY * 5)) % height
                
                // Коррекция отрицательного остатка
                if (x < 0) x += width
                if (y < 0) y += height

                // Добавляем органическое "дрожание" (wiggle)
                val wiggleX = sin(phase * 15 * Math.PI.toFloat() * atom.speedX.coerceAtLeast(0.1f)) * 30.dp.toPx()
                val wiggleY = cos(phase * 12 * Math.PI.toFloat() * atom.speedY.coerceAtLeast(0.1f)) * 30.dp.toPx()

                // Дыхание
                val pulse = 1f + 0.4f * sin(phase * 20 * Math.PI.toFloat() * (atom.speedX + atom.speedY).coerceAtLeast(0.1f))

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            atom.color.copy(alpha = if (isDark) atom.alpha else atom.alpha * 0.7f),
                            Color.Transparent
                        ),
                        center = Offset(x + wiggleX, y + wiggleY),
                        radius = atom.sizeBase.dp.toPx() * pulse
                    )
                )
            }
        }
        content()
    }
}

private data class Atom(
    val xStart: Float,
    val yStart: Float,
    val sizeBase: Float,
    val speedX: Float,
    val speedY: Float,
    val color: Color,
    val alpha: Float
)

private fun ClosedRange<Float>.random() = 
    (Math.random() * (endInclusive - start) + start).toFloat()

// ═══════════════════════════════════════════════════════════════
//  ULTRA-SLIM GLASS BOTTOM BAR
// ═══════════════════════════════════════════════════════════════
@Composable
fun SpanishBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .height(60.dp)
                .widthIn(max = 420.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = if(isSystemInDarkTheme()) 0.05f else 0.2f)),
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEach { item ->
                    val selected = currentRoute.startsWith(item.route)
                    val color = if (selected) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onNavigate(item.route) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (selected) item.iconSelected else item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(if(selected) 26.dp else 22.dp),
                                tint = color
                            )
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// COMMON COMPONENTS
// ──────────────────────────────────────────────────────────────

@Composable
fun XpProgressBar(level: Int, progress: Float, totalXp: Int, modifier: Modifier = Modifier) {
    val animProgress by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), label = "xp")
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Text("$level", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Box(modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(animProgress).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
        }
    }
}

@Composable
fun StreakBadge(streak: Int, modifier: Modifier = Modifier, large: Boolean = false) {
    Row(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)).padding(horizontal = 10.dp, vertical = 6.dp)) {
        Text("🔥 $streak", fontWeight = FontWeight.Bold, fontSize = if(large) 14.sp else 12.sp)
    }
}

@Composable
fun LevelBadge(level: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(level, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, modifier = modifier)
}
