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
//  BACKGROUND — clean white (light theme)
// ═══════════════════════════════════════════════════════════════

@Composable
fun SpanishBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.fillMaxSize().background(Color(0xFFF8F8FA))) {
        content()
    }
}

// ═══════════════════════════════════════════════════════════════
//  BOTTOM BAR — light, purple active
// ═══════════════════════════════════════════════════════════════

@Composable
fun SpanishBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val purple   = Color(0xFF7B2FBE)
    val inactive = Color(0xFFAEAEB2)

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE5E5EA))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding()
                .height(62.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute.startsWith(item.route)

                val iconColor by animateColorAsState(
                    targetValue  = if (selected) purple else inactive,
                    animationSpec = tween(200),
                    label        = "color_${item.route}"
                )
                val scale by animateFloatAsState(
                    targetValue   = if (selected) 1.08f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label         = "scale_${item.route}"
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
                            imageVector     = if (selected) item.iconSelected else item.icon,
                            contentDescription = item.label,
                            modifier        = Modifier.size(24.dp),
                            tint            = iconColor
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

@Composable
fun XpProgressBar(level: Int, progress: Float, totalXp: Int, modifier: Modifier = Modifier) {
    val animProgress by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), label = "xp")
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape)
                .background(Color(0xFF7B2FBE)),
            contentAlignment = Alignment.Center
        ) {
            Text("$level", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
        }
        Box(modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFF0E8FF))) {
            Box(
                modifier = Modifier.fillMaxHeight().fillMaxWidth(animProgress)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF7B2FBE), Color(0xFFE040FB))))
            )
        }
    }
}
