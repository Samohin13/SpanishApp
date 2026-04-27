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
    NavItem("dictionary", "Словарь",  Icons.Outlined.MenuBook,      Icons.Filled.MenuBook),
    NavItem("profile",    "Профиль",  Icons.Outlined.Person,        Icons.Filled.Person)
)

// ═══════════════════════════════════════════════════════════════
//  SPANISH ANIMATED BACKGROUND
// ═══════════════════════════════════════════════════════════════
@Composable
fun SpanishBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val bgColor = MaterialTheme.colorScheme.background
    
    val infiniteTransition = rememberInfiniteTransition(label = "atoms")
    
    // Глобальная фаза для синхронизации
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart),
        label = "phase"
    )

    // Генерация параметров для 12 "атомов" (неона)
    // Используем remember, чтобы они не перегенерировались каждый кадр
    val atoms = remember {
        List(12) { i ->
            Atom(
                xSeed = (0..100).random() / 100f,
                ySeed = (0..100).random() / 100f,
                sizeBase = (50..250).random().toFloat(),
                speed = (0.5f..1.5f).random(),
                color = when (i % 3) {
                    0 -> Color(0xFFFF1744) // Terracotta
                    1 -> Color(0xFFFFEA00) // Ochre
                    else -> Color(0xFF76FF03) // Olive
                },
                alpha = (0.04f..0.1f).random()
            )
        }
    }

    Box(modifier = modifier.fillMaxSize().background(bgColor)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            atoms.forEach { atom ->
                val moveX = sin(phase * 2 * Math.PI.toFloat() * atom.speed + atom.xSeed * 10) * 80.dp.toPx()
                val moveY = cos(phase * 2 * Math.PI.toFloat() * atom.speed + atom.ySeed * 10) * 80.dp.toPx()
                val pulse = 1f + 0.2f * sin(phase * 4 * Math.PI.toFloat() * atom.speed)

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            atom.color.copy(alpha = if (isDark) atom.alpha * 1.5f else atom.alpha),
                            Color.Transparent
                        ),
                        center = Offset(
                            x = width * atom.xSeed + moveX,
                            y = height * atom.ySeed + moveY
                        ),
                        radius = atom.sizeBase.dp.toPx() * pulse
                    )
                )
            }
        }
        content()
    }
}

private data class Atom(
    val xSeed: Float,
    val ySeed: Float,
    val sizeBase: Float,
    val speed: Float,
    val color: Color,
    val alpha: Float
)

private fun ClosedRange<Float>.random() = 
    (Math.random() * (endInclusive - start) + start).toFloat()

// ═══════════════════════════════════════════════════════════════
//  UNIFIED BOTTOM BAR
// ═══════════════════════════════════════════════════════════════
@Composable
fun SpanishBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .height(68.dp)
            .shadow(32.dp, RoundedCornerShape(28.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute.startsWith(item.route)
                val animatedColor by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.primary 
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    label = "color"
                )

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
                            modifier = Modifier.size(24.dp),
                            tint = animatedColor
                        )
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  OTHER SHARED COMPONENTS (Simplified for brevity)
// ═══════════════════════════════════════════════════════════════

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
