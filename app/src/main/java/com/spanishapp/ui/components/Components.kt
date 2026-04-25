package com.spanishapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spanishapp.ui.theme.AppColors

// ═══════════════════════════════════════════════════════════════
//  XP PROGRESS BAR  —  ровный прогресс, без пафоса
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
        // Уровень — спокойный круглый жетон
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AppColors.TerracottaBg)
                .border(1.dp, AppColors.Border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = "$level",
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                color      = AppColors.TerracottaDark
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
                    color      = AppColors.InkMid,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(AppColors.SurfaceMuted)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animProgress)
                        .clip(RoundedCornerShape(3.dp))
                        .background(AppColors.Terracotta)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  STREAK BADGE  —  тёплый чип, без огненных градиентов
// ═══════════════════════════════════════════════════════════════
@Composable
fun StreakBadge(
    streak: Int,
    modifier: Modifier = Modifier,
    large: Boolean = false
) {
    val active = streak > 0
    val bgColor = if (active) AppColors.OchreBg else AppColors.SurfaceMuted
    val textColor = if (active) AppColors.OchreDark else AppColors.InkLight
    val borderColor = if (active) AppColors.OchreSoft else AppColors.Border

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
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
//  STAT CARD  —  чистая карточка с тонким бордюром
// ═══════════════════════════════════════════════════════════════
@Composable
fun StatCard(
    label: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier,
    accentColor: Color = AppColors.Terracotta
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, AppColors.Border, RoundedCornerShape(16.dp))
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
                color = AppColors.InkLight
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
    val ringColor = if (done) AppColors.Olive else AppColors.Terracotta

    Box(modifier = modifier.size(60.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress    = { animProgress },
            modifier    = Modifier.fillMaxSize(),
            strokeWidth = 4.dp,
            color       = ringColor,
            trackColor  = AppColors.SurfaceMuted
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
                    text  = "мин",
                    fontSize = 9.sp,
                    color = AppColors.InkLight
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
                    color      = AppColors.Terracotta,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  HERO FEATURE CARD  —  главная карточка, мягкая заливка
// ═══════════════════════════════════════════════════════════════
@Composable
fun HeroFeatureCard(
    title: String,
    subtitle: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier   = Modifier,
    badgeText: String?   = null,
    gradientStart: Color = AppColors.Terracotta,
    gradientEnd: Color   = AppColors.TerracottaDark
) {
    // Карточка с тёплым кремовым фоном и акцентной полоской
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape     = RoundedCornerShape(20.dp),
                spotColor = AppColors.Terracotta.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, AppColors.Border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier              = Modifier.padding(20.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Иконка в мягком акцентном квадрате
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppColors.TerracottaBg),
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
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    if (badgeText != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppColors.Terracotta)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text       = badgeText,
                                style      = MaterialTheme.typography.labelSmall,
                                color      = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.InkMid
                )
            }

            Icon(
                Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint     = AppColors.InkLight,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  FEATURE CARD  —  ячейка сетки, чистая, с тонким бордюром
// ═══════════════════════════════════════════════════════════════
@Composable
fun FeatureCard(
    title: String,
    subtitle: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier  = Modifier,
    badgeText: String?  = null,
    accentColor: Color  = AppColors.Terracotta,
    enabled: Boolean    = true
) {
    val accentBg = when (accentColor) {
        AppColors.Terracotta -> AppColors.TerracottaBg
        AppColors.Olive      -> AppColors.OliveBg
        AppColors.Ochre      -> AppColors.OchreBg
        AppColors.Indigo     -> AppColors.IndigoBg
        else -> accentColor.copy(alpha = 0.08f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, AppColors.Border, RoundedCornerShape(18.dp))
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
                        .background(if (enabled) accentBg else AppColors.SurfaceMuted),
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
                            color      = accentColor,
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
                                 else AppColors.InkFaint
                )
                Text(
                    text     = subtitle,
                    style    = MaterialTheme.typography.labelSmall,
                    color    = AppColors.InkLight,
                    maxLines = 2
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  LEVEL BADGE  —  маленький уровень CEFR
// ═══════════════════════════════════════════════════════════════
@Composable
fun LevelBadge(level: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (level) {
        "A1" -> AppColors.OliveBg     to AppColors.OliveDark
        "A2" -> AppColors.IndigoBg    to AppColors.IndigoDark
        "B1" -> AppColors.OchreBg     to AppColors.OchreDark
        "B2" -> AppColors.TerracottaBg to AppColors.TerracottaDark
        "C1" -> AppColors.IndigoBg    to AppColors.IndigoDark
        else -> AppColors.SurfaceMuted to AppColors.InkLight
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
//  WORD OF DAY CARD  —  благородная, тёплая, без крикливых градиентов
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
            .shadow(
                elevation = 2.dp,
                shape     = RoundedCornerShape(20.dp),
                spotColor = AppColors.Terracotta.copy(alpha = 0.06f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.TerracottaBg)
            .border(1.dp, AppColors.TerracottaSoft.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier            = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Шапка
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
                        color      = AppColors.TerracottaDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (wasPracticed) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppColors.OliveBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "✓ изучено",
                            style      = MaterialTheme.typography.labelSmall,
                            color      = AppColors.OliveDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(2.dp))

            // Слово + кнопка озвучки
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text       = spanish,
                    style      = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color      = AppColors.Ink
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(onClick = onSpeak),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.VolumeUp,
                        contentDescription = "Произнести",
                        tint     = AppColors.Terracotta,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text       = russian,
                style      = MaterialTheme.typography.bodyLarge,
                color      = AppColors.InkMid,
                fontWeight = FontWeight.Medium
            )

            if (example.isNotEmpty()) {
                Text(
                    text      = "« $example »",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = AppColors.InkLight,
                    fontStyle = FontStyle.Italic
                )
            }

            if (!wasPracticed) {
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.Terracotta)
                        .clickable(onClick = onPractice)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Тренировать слово",
                        style      = MaterialTheme.typography.labelLarge,
                        color      = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  BOTTOM NAVIGATION BAR  —  чистая, с тонкой границей сверху
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(
                BorderStroke(1.dp, AppColors.Border),
                shape = RoundedCornerShape(0.dp)
            )
    ) {
        // Тонкая верхняя граница
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AppColors.Border)
        )

        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            modifier       = Modifier.height(64.dp)
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
                        selectedIconColor   = AppColors.Terracotta,
                        selectedTextColor   = AppColors.Terracotta,
                        unselectedIconColor = AppColors.InkLight,
                        unselectedTextColor = AppColors.InkLight,
                        indicatorColor      = AppColors.TerracottaBg
                    )
                )
            }
        }
    }
}
