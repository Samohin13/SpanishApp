package com.spanishapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ═══════════════════════════════════════════════════════════════
//  LUMEN — компоненты без бордюров. Только уровни поверхностей.
//  Тёмная тема — настоящий чёрный, светлая — почти белый.
// ═══════════════════════════════════════════════════════════════

// ═══════════════════════════════════════════════════════════════
//  XP PROGRESS BAR
// ═══════════════════════════════════════════════════════════════
@Composable
fun XpProgressBar(
    level: Int,
    progress: Float,
    totalXp: Int,
    modifier: Modifier = Modifier
) {
    val animProgress by animateFloatAsState(
        targetValue   = progress.coerceIn(0f, 1f),
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label         = "xp"
    )

    Row(
        modifier              = modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = "$level",
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "Уровень $level",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "$totalXp XP",
                    style      = MaterialTheme.typography.labelMedium,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animProgress)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  STREAK BADGE
// ═══════════════════════════════════════════════════════════════
@Composable
fun StreakBadge(
    streak: Int,
    modifier: Modifier = Modifier,
    large: Boolean = false
) {
    val active = streak > 0
    val bgColor   = if (active) MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerHigh
    val textColor = if (active) MaterialTheme.colorScheme.onTertiaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .padding(
                horizontal = if (large) 14.dp else 10.dp,
                vertical   = if (large) 8.dp else 6.dp
            ),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text     = if (active) "🔥" else "·",
            fontSize = if (large) 16.sp else 13.sp
        )
        Text(
            text       = if (large) "$streak дней подряд" else "$streak",
            fontSize   = if (large) 14.sp else 13.sp,
            fontWeight = FontWeight.SemiBold,
            color      = textColor
        )
    }
}

// ═══════════════════════════════════════════════════════════════
//  STAT CARD
// ═══════════════════════════════════════════════════════════════
@Composable
fun StatCard(
    label: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(icon, fontSize = 18.sp)
            Text(
                text       = value,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  DAILY GOAL RING
// ═══════════════════════════════════════════════════════════════
@Composable
fun DailyGoalRing(
    todayMinutes: Int,
    goalMinutes: Int,
    modifier: Modifier = Modifier
) {
    val progress     = (todayMinutes.toFloat() / goalMinutes.coerceAtLeast(1)).coerceIn(0f, 1f)
    val animProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label         = "ring"
    )
    val done = progress >= 1f
    val ringColor = if (done) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.primary

    Box(modifier = modifier.size(60.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress    = { animProgress },
            modifier    = Modifier.fillMaxSize(),
            strokeWidth = 4.dp,
            color       = ringColor,
            trackColor  = MaterialTheme.colorScheme.surfaceContainerHighest
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = if (done) "✓" else "$todayMinutes",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = ringColor
            )
            if (!done) {
                Text(
                    text     = "мин",
                    fontSize = 9.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  SECTION HEADER
// ═══════════════════════════════════════════════════════════════
@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text       = title,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onBackground
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 4.dp)) {
                Text(
                    text       = actionLabel,
                    style      = MaterialTheme.typography.labelMedium,
                    color      = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  HERO FEATURE CARD  —  главная карточка, заливка primary
// ═══════════════════════════════════════════════════════════════
@Composable
fun HeroFeatureCard(
    title: String,
    subtitle: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier   = Modifier,
    badgeText: String?   = null,
    gradientStart: Color = Color.Unspecified,
    gradientEnd: Color   = Color.Unspecified
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier              = Modifier.padding(20.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 26.sp)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text       = title,
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimary
                    )
                    if (badgeText != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.22f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text       = badgeText,
                                style      = MaterialTheme.typography.labelSmall,
                                color      = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
            }

            Icon(
                Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint     = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  FEATURE CARD  —  ячейка сетки, без бордюров
// ═══════════════════════════════════════════════════════════════
@Composable
fun FeatureCard(
    title: String,
    subtitle: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier  = Modifier,
    badgeText: String?  = null,
    accentColor: Color  = Color.Unspecified,
    enabled: Boolean    = true
) {
    val resolvedAccent = if (accentColor == Color.Unspecified)
        MaterialTheme.colorScheme.primary else accentColor
    val accentBg       = resolvedAccent.copy(alpha = 0.14f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (enabled) accentBg
                            else MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 20.sp)
                }
                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(accentBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text       = badgeText,
                            style      = MaterialTheme.typography.labelSmall,
                            color      = resolvedAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (enabled) MaterialTheme.colorScheme.onSurface
                                 else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text     = subtitle,
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  LEVEL BADGE — CEFR
// ═══════════════════════════════════════════════════════════════
@Composable
fun LevelBadge(level: String, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val (bgColor, textColor) = when (level) {
        "A1" -> cs.secondaryContainer to cs.onSecondaryContainer
        "A2" -> cs.primaryContainer   to cs.onPrimaryContainer
        "B1" -> cs.tertiaryContainer  to cs.onTertiaryContainer
        "B2" -> cs.primaryContainer   to cs.onPrimaryContainer
        "C1" -> cs.tertiaryContainer  to cs.onTertiaryContainer
        else -> cs.surfaceContainerHigh to cs.onSurfaceVariant
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text       = level,
            style      = MaterialTheme.typography.labelSmall,
            color      = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

// ═══════════════════════════════════════════════════════════════
//  WORD OF DAY CARD
// ═══════════════════════════════════════════════════════════════
@Composable
fun WordOfDayCard(
    spanish: String,
    russian: String,
    example: String,
    wasPracticed: Boolean,
    onSpeak: () -> Unit,
    onPractice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier            = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("📖", fontSize = 13.sp)
                    Text(
                        "Слово дня",
                        style      = MaterialTheme.typography.labelMedium,
                        color      = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (wasPracticed) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "✓ изучено",
                            style      = MaterialTheme.typography.labelSmall,
                            color      = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(2.dp))

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text       = spanish,
                    style      = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable(onClick = onSpeak),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.VolumeUp,
                        contentDescription = "Произнести",
                        tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text       = russian,
                style      = MaterialTheme.typography.bodyLarge,
                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            if (example.isNotEmpty()) {
                Text(
                    text      = "« $example »",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            }

            if (!wasPracticed) {
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onPractice)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Тренировать слово",
                        style      = MaterialTheme.typography.labelLarge,
                        color      = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  BOTTOM NAVIGATION BAR — без бордюров, на surface
// ═══════════════════════════════════════════════════════════════
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

@Composable
fun SpanishBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier       = Modifier.height(72.dp)
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute.startsWith(item.route)
            NavigationBarItem(
                selected = selected,
                onClick  = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector        = if (selected) item.iconSelected else item.icon,
                        contentDescription = item.label,
                        modifier           = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text       = item.label,
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = MaterialTheme.colorScheme.primary,
                    selectedTextColor   = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor      = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
