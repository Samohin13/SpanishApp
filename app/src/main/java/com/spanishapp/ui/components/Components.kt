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
    
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart),
        label = "phase"
    )

    Box(modifier = modifier.fillMaxSize().background(bgColor)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            // Пятно 1 (Terracotta)
            drawCircle(
                color = Color(0xFFC62828).copy(alpha = if (isDark) 0.04f else 0.05f),
                radius = width * 0.9f,
                center = Offset(
                    x = width * (0.1f + 0.15f * sin(phase * 2 * Math.PI.toFloat())),
                    y = height * (0.2f + 0.1f * sin(phase * 2 * Math.PI.toFloat()))
                )
            )
            
            // Пятно 2 (Olive)
            drawCircle(
                color = Color(0xFF558B2F).copy(alpha = if (isDark) 0.03f else 0.04f),
                radius = width * 0.7f,
                center = Offset(
                    x = width * (0.8f + 0.1f * sin((phase + 0.5f) * 2 * Math.PI.toFloat())),
                    y = height * (0.6f + 0.1f * sin((phase + 0.5f) * 2 * Math.PI.toFloat()))
                )
            )
        }
        content()
    }
}

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
