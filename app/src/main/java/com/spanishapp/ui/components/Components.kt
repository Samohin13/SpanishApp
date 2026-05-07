package com.spanishapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── NAVIGATION MODELS ─────────────────────────────────────────

data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val iconSelected: ImageVector
)

val bottomNavItems = listOf(
    NavItem("home",       "Главная",  Icons.Outlined.Home,          Icons.Filled.Home),
    NavItem("games",      "Игры",     Icons.Outlined.Gamepad,       Icons.Filled.Gamepad),
    NavItem("flashcards", "Tarjetas", Icons.Outlined.Style,         Icons.Filled.Style),
    NavItem("dictionary", "Словарь",  Icons.AutoMirrored.Outlined.MenuBook, Icons.AutoMirrored.Filled.MenuBook),
    NavItem("profile",    "Профиль",  Icons.Outlined.Person,        Icons.Filled.Person)
)

// ═══════════════════════════════════════════════════════════════
//  BACKGROUND — warm off-white (light theme)
// ═══════════════════════════════════════════════════════════════

@Composable
fun SpanishBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.fillMaxSize().background(Color(0xFFFFF8F2))) {
        content()
    }
}

// ═══════════════════════════════════════════════════════════════
//  BOTTOM BAR — sunset orange active, gliding pill, tap-color
// ═══════════════════════════════════════════════════════════════

@Composable
fun SpanishBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    // Sunset palette
    val activeColor  = Color(0xFFFF6B35)  // Orange primary
    val inactive     = Color(0xFFAEAEB2)
    val pillBg       = Color(0x1AFF6B35)  // 10% orange tint
    val pillBorder   = Color(0x33FF6B35)  // 20% orange border

    val activeIdx = bottomNavItems.indexOfFirst { currentRoute.startsWith(it.route) }.coerceAtLeast(0)

    // Gliding pill offset — animates between tab positions
    val pillOffset by animateFloatAsState(
        targetValue   = activeIdx.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "pill_offset"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE8E5E0))
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding()
                .height(62.dp)
        ) {
            val tabWidth = maxWidth / bottomNavItems.size

            // Gliding pill behind active tab
            Box(
                modifier = Modifier
                    .offset(x = tabWidth * pillOffset + (tabWidth - 48.dp) / 2, y = 9.dp)
                    .size(48.dp, 38.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(pillBg)
                    .then(
                        Modifier.graphicsLayer {
                            // Subtle border via outline
                        }
                    )
            )

            Row(
                modifier              = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                bottomNavItems.forEachIndexed { idx, item ->
                    val selected = currentRoute.startsWith(item.route)

                    val iconColor by animateColorAsState(
                        targetValue   = if (selected) activeColor else inactive,
                        animationSpec = tween(200),
                        label         = "color_${item.route}"
                    )
                    // Spring scale: selected → 1.08, normal → 1.0
                    val scale by animateFloatAsState(
                        targetValue   = if (selected) 1.08f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness    = Spring.StiffnessMediumLow
                        ),
                        label = "scale_${item.route}"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null,
                                onClick           = { onNavigate(item.route) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
                        ) {
                            Icon(
                                imageVector        = if (selected) item.iconSelected else item.icon,
                                contentDescription = item.label,
                                modifier           = Modifier.size(24.dp),
                                tint               = iconColor
                            )
                            Text(
                                text       = item.label,
                                fontSize   = 10.sp,
                                color      = iconColor,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines   = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun XpProgressBar(level: Int, progress: Float, totalXp: Int, modifier: Modifier = Modifier) {
    val animProgress by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), label = "xp")
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape)
                .background(Color(0xFFFF6B35)),  // Orange primary
            contentAlignment = Alignment.Center
        ) {
            Text("$level", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
        }
        Box(modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFFFF1E6))) {
            Box(
                modifier = Modifier.fillMaxHeight().fillMaxWidth(animProgress)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFFF6B35), Color(0xFFD62867))))
            )
        }
    }
}
