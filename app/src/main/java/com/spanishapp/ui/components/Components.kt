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
//  SPANISH ATOMIC BACKGROUND (4K FEEL)
// ═══════════════════════════════════════════════════════════════
@Composable
fun SpanishBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val bgColor = MaterialTheme.colorScheme.background
    
    val infiniteTransition = rememberInfiniteTransition(label = "atoms")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Restart),
        label = "phase"
    )

    val atoms = remember {
        List(20) { i ->
            Atom(
                xSeed = (0..100).random() / 100f,
                ySeed = (0..100).random() / 100f,
                sizeBase = (30..120).random().toFloat(),
                speed = (0.8f..2.5f).random(),
                color = when (i % 4) {
                    0 -> Color(0xFFFF1744)
                    1 -> Color(0xFFFFEA00)
                    2 -> Color(0xFF76FF03)
                    else -> Color(0xFFFF5722)
                },
                alpha = (0.15f..0.25f).random()
            )
        }
    }

    Box(modifier = modifier.fillMaxSize().background(bgColor)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            atoms.forEach { atom ->
                val angle = phase * 2 * Math.PI.toFloat() * atom.speed + (atom.xSeed * 100)
                val moveX = sin(angle) * 120.dp.toPx()
                val moveY = cos(angle * 0.7f) * 120.dp.toPx()
                val pulse = 1f + 0.3f * sin(phase * 6 * Math.PI.toFloat() * atom.speed)

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            atom.color.copy(alpha = if (isDark) atom.alpha else atom.alpha * 0.8f),
                            Color.Transparent
                        ),
                        center = Offset(
                            x = (width * atom.xSeed + moveX).coerceIn(-100f, width + 100f),
                            y = (height * atom.ySeed + moveY).coerceIn(-100f, height + 100f)
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
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .height(60.dp)
                .widthIn(max = 400.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            tonalElevation = 0.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
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
                                        .padding(top = 2.dp)
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
