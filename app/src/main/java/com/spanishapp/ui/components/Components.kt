package com.spanishapp.ui.components

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spanishapp.ui.theme.AppColors

// ─────────────────────────────────────────────────────────────
//  XP PROGRESS BAR
// ─────────────────────────────────────────────────────────────
@Composable
fun XpProgressBar(
    level: Int,
    progress: Float,
    totalXp: Int,
    modifier: Modifier = Modifier
) {
    val animProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label         = "xp"
    )

    Surface(
        modifier      = modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(16.dp),
        color         = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier            = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Level circle
            Box(
                modifier         = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(listOf(AppColors.Gold, AppColors.Terracotta))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = "$level",
                    style      = MaterialTheme.typography.labelLarge,
                    color      = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Уровень $level",
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "$totalXp XP",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.GoldDark,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animProgress)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(listOf(AppColors.Gold, AppColors.Terracotta))
                            )
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  STREAK BADGE
// ─────────────────────────────────────────────────────────────
@Composable
fun StreakBadge(
    streak: Int,
    modifier: Modifier = Modifier,
    large: Boolean = false
) {
    val active = streak > 0
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(12.dp),
        color    = if (active) AppColors.Gold.copy(alpha = 0.13f)
                   else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = if (active) "🔥" else "○", fontSize = if (large) 22.sp else 16.sp)
            Text(
                text       = "$streak",
                fontSize   = if (large) 18.sp else 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = if (active) AppColors.GoldDark
                             else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (large) {
                Text(
                    "дн.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  STAT CARD  —  compact metric tile
// ─────────────────────────────────────────────────────────────
@Composable
fun StatCard(
    label: String,
    value: String,
    icon: String,
    modifier: Modifier  = Modifier,
    accentColor: Color  = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier      = modifier,
        shape         = RoundedCornerShape(18.dp),
        color         = accentColor.copy(alpha = 0.07f)
    ) {
        Column(
            modifier            = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(icon, fontSize = 22.sp)
            Text(
                text       = value,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color      = accentColor
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  DAILY GOAL RING
// ─────────────────────────────────────────────────────────────
@Composable
fun DailyGoalRing(
    todayMinutes: Int,
    goalMinutes: Int,
    modifier: Modifier = Modifier
) {
    val progress    = (todayMinutes.toFloat() / goalMinutes).coerceIn(0f, 1f)
    val animProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label         = "ring"
    )
    val done = progress >= 1f

    Box(modifier = modifier.size(72.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress    = { animProgress },
            modifier    = Modifier.fillMaxSize(),
            strokeWidth = 5.dp,
            color       = if (done) AppColors.Teal else AppColors.Terracotta,
            trackColor  = MaterialTheme.colorScheme.surfaceVariant
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text       = "$todayMinutes",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text  = "мин",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  SECTION HEADER
// ─────────────────────────────────────────────────────────────
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
            fontWeight = FontWeight.Bold
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text(
                    text  = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  HERO FEATURE CARD  —  primary wide card with gradient
// ─────────────────────────────────────────────────────────────
@Composable
fun HeroFeatureCard(
    title: String,
    subtitle: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier  = Modifier,
    badgeText: String?  = null,
    gradientStart: Color = AppColors.Terracotta,
    gradientEnd: Color   = AppColors.TerracottaDark
) {
    Surface(
        onClick  = onClick,
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(22.dp),
        color    = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(gradientStart, gradientEnd))
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon bubble
                Box(
                    modifier         = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 28.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text       = title,
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                        if (badgeText != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text     = badgeText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style    = MaterialTheme.typography.labelMedium,
                                    color    = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text  = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Icon(
                    Icons.Default.ArrowForwardIos,
                    contentDescription = null,
                    tint     = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  COMPACT FEATURE CARD  —  small grid cell
// ─────────────────────────────────────────────────────────────
@Composable
fun FeatureCard(
    title: String,
    subtitle: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier  = Modifier,
    badgeText: String?  = null,
    accentColor: Color  = MaterialTheme.colorScheme.primary,
    enabled: Boolean    = true
) {
    Surface(
        onClick   = onClick,
        enabled   = enabled,
        modifier  = modifier,
        shape     = RoundedCornerShape(20.dp),
        color     = accentColor.copy(alpha = 0.08f),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Box(
                    modifier         = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 22.sp)
                }
                if (badgeText != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text     = badgeText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = if (enabled) MaterialTheme.colorScheme.onSurface
                                 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  BOTTOM NAVIGATION BAR
// ─────────────────────────────────────────────────────────────
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
    Surface(
        color         = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute.startsWith(item.route)
                NavigationBarItem(
                    selected = selected,
                    onClick  = { onNavigate(item.route) },
                    icon = {
                        Icon(
                            imageVector    = if (selected) item.iconSelected else item.icon,
                            contentDescription = item.label,
                            modifier       = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text  = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = AppColors.Terracotta,
                        selectedTextColor   = AppColors.Terracotta,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor      = AppColors.Terracotta.copy(alpha = 0.1f)
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  LEVEL BADGE
// ─────────────────────────────────────────────────────────────
@Composable
fun LevelBadge(level: String, modifier: Modifier = Modifier) {
    val color = when (level) {
        "A1" -> AppColors.Teal
        "A2" -> AppColors.Info
        "B1" -> AppColors.Gold
        "B2" -> AppColors.Terracotta
        else -> AppColors.InkLight
    }
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(8.dp),
        color    = color.copy(alpha = 0.15f)
    ) {
        Text(
            text     = level,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style    = MaterialTheme.typography.labelSmall,
            color    = color,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  WORD OF DAY CARD
// ─────────────────────────────────────────────────────────────
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(22.dp),
        color    = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            AppColors.Terracotta.copy(alpha = 0.12f),
                            AppColors.Gold.copy(alpha = 0.08f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Header row
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("📅", fontSize = 14.sp)
                        Text(
                            "Слово дня",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (wasPracticed) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AppColors.Teal.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "✓ изучено",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = AppColors.Teal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Word + speak button
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text       = spanish,
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color      = AppColors.TerracottaDark
                    )
                    IconButton(
                        onClick  = onSpeak,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AppColors.Terracotta.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            Icons.Filled.VolumeUp,
                            contentDescription = "Произнести",
                            tint     = AppColors.Terracotta,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text  = russian,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )

                if (example.isNotEmpty()) {
                    Text(
                        text      = "«$example»",
                        style     = MaterialTheme.typography.bodySmall,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }

                if (!wasPracticed) {
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick  = onPractice,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Terracotta
                        )
                    ) {
                        Text(
                            "Тренировать слово",
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
