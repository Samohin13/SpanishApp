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
    @androidx.annotation.StringRes val labelRes: Int,
    val icon: ImageVector,
    val iconSelected: ImageVector
)

val bottomNavItems = listOf(
    NavItem("home",       com.spanishapp.R.string.nav_home,       Icons.Outlined.Home,          Icons.Filled.Home),
    NavItem("games",      com.spanishapp.R.string.nav_games,      Icons.Outlined.Gamepad,       Icons.Filled.Gamepad),
    // "Tarjetas" — испанское название, бренд карточек, не локализуем
    NavItem("flashcards", com.spanishapp.R.string.title_flashcards, Icons.Outlined.Style,       Icons.Filled.Style),
    NavItem("radio",      com.spanishapp.R.string.nav_radio,      Icons.Outlined.Radio,         Icons.Filled.Radio),
    NavItem("dictionary", com.spanishapp.R.string.nav_dictionary, Icons.AutoMirrored.Outlined.MenuBook, Icons.AutoMirrored.Filled.MenuBook),
    NavItem("profile",    com.spanishapp.R.string.nav_profile,    Icons.Outlined.Person,        Icons.Filled.Person)
)

// ═══════════════════════════════════════════════════════════════
//  BACKGROUND — adaptive (light: warm off-white, dark: dark surface)
// ═══════════════════════════════════════════════════════════════

@Composable
fun SpanishBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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

    // Sub-routes that don't have their own bottom-nav tab — visually keep the parent tab highlighted.
    val effectiveRoute = when {
        currentRoute.startsWith("settings")     -> "profile"
        currentRoute.startsWith("achievements") -> "profile"
        currentRoute.startsWith("leaderboard")  -> "profile"
        currentRoute.startsWith("rating")       -> "profile"
        currentRoute.startsWith("weak_words")   -> "dictionary"
        else -> currentRoute
    }
    val activeIdx = bottomNavItems.indexOfFirst { effectiveRoute.startsWith(it.route) }.coerceAtLeast(0)

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
        // Theme-aware: in dark mode the surface color matches the GamesScreen cards
        // (a slightly-elevated dark gray); in light mode it's near-white.
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
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
                    val selected = effectiveRoute.startsWith(item.route)

                    val iconColor by animateColorAsState(
                        targetValue   = if (selected) activeColor else inactive,
                        animationSpec = tween(200),
                        label         = "color_${item.route}"
                    )
                    // Bouncy "title" scale: 1.0 → 1.20 → 1.0 burst on selection.
                    val burstAnim = remember { Animatable(1f) }
                    LaunchedEffect(selected) {
                        if (selected) {
                            burstAnim.animateTo(
                                targetValue = 1.20f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioHighBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                            burstAnim.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        } else {
                            burstAnim.snapTo(1f)
                        }
                    }
                    // Ripple ring: expands + fades when this tab becomes selected.
                    val ripple = remember { Animatable(0f) }
                    LaunchedEffect(selected) {
                        if (selected) {
                            ripple.snapTo(0f)
                            ripple.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
                            )
                        }
                    }

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
                        // Expanding ripple ring behind the active icon.
                        if (selected && ripple.value < 1f) {
                            val ringAlpha = (1f - ripple.value).coerceIn(0f, 1f) * 0.6f
                            val ringSize = 20.dp + (32.dp * ripple.value)
                            Box(
                                modifier = Modifier
                                    .size(ringSize)
                                    .clip(CircleShape)
                                    .background(activeColor.copy(alpha = ringAlpha * 0.25f))
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.graphicsLayer {
                                scaleX = burstAnim.value
                                scaleY = burstAnim.value
                            }
                        ) {
                            val itemLabel = androidx.compose.ui.res.stringResource(item.labelRes)
                            Icon(
                                imageVector        = if (selected) item.iconSelected else item.icon,
                                contentDescription = itemLabel,
                                modifier           = Modifier.size(24.dp),
                                tint               = iconColor
                            )
                            Text(
                                text       = itemLabel,
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

// ═══════════════════════════════════════════════════════════════
//  NAVIGATION RAIL — для планшетов (v1.12.0 Phase 1 adaptive)
// ═══════════════════════════════════════════════════════════════

/**
 * Vertical navigation для планшетов и foldables (Medium/Expanded widths).
 * Использует Material3 NavigationRail с теми же brand colors что SpanishBottomBar.
 *
 * Активируется через AdaptiveScaffold (см. ui/adaptive/AdaptiveScaffold.kt).
 * Compact (телефон portrait) продолжает использовать SpanishBottomBar.
 */
@Composable
fun SpanishNavigationRail(
    currentRoute: String,
    onNavigate: (String) -> Unit,
) {
    val activeColor = Color(0xFFFF6B35)
    val inactive = Color(0xFFAEAEB2)

    // Sub-routes которые мапятся на parent tab (как в SpanishBottomBar)
    val effectiveRoute = when {
        currentRoute.startsWith("settings")     -> "profile"
        currentRoute.startsWith("achievements") -> "profile"
        currentRoute.startsWith("leaderboard")  -> "profile"
        currentRoute.startsWith("rating")       -> "profile"
        currentRoute.startsWith("weak_words")   -> "dictionary"
        else -> currentRoute
    }

    androidx.compose.material3.NavigationRail(
        modifier = Modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        // Top spacer чтобы иконки начинались чуть ниже top edge
        Spacer(modifier = Modifier.height(16.dp))

        bottomNavItems.forEach { item ->
            val selected = effectiveRoute.startsWith(item.route)
            androidx.compose.material3.NavigationRailItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        if (selected) item.iconSelected else item.icon,
                        contentDescription = androidx.compose.ui.res.stringResource(item.labelRes),
                        modifier = Modifier.size(26.dp),
                    )
                },
                label = {
                    Text(
                        androidx.compose.ui.res.stringResource(item.labelRes),
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
                colors = androidx.compose.material3.NavigationRailItemDefaults.colors(
                    selectedIconColor = activeColor,
                    selectedTextColor = activeColor,
                    unselectedIconColor = inactive,
                    unselectedTextColor = inactive,
                    indicatorColor = activeColor.copy(alpha = 0.12f),
                ),
            )
        }
    }
}
