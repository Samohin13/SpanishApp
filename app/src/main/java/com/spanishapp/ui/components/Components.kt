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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spanishapp.ui.theme.AppColors

// ═══════════════════════════════════════════════════════════════
//  XP PROGRESS BAR  —  широкая, яркая, с анимацией
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
        animationSpec = tween(1100, easing = FastOutSlowInEasing),
        label         = "xp"
    )

    Row(
        modifier          = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Уровень — круглый жетон с градиентом
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(AppColors.Amber, AppColors.Coral)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = "$level",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Black,
                color      = Color.White
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
                    style      = MaterialTheme.typography.labelLarge,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "✨ $totalXp XP",
                    style      = MaterialTheme.typography.labelLarge,
                    color      = AppColors.AmberDark,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            // Трек
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animProgress)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(AppColors.Amber, AppColors.Coral)
                            )
                        )
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  STREAK BADGE  —  огонь стрика
// ═══════════════════════════════════════════════════════════════
@Composable
fun StreakBadge(
    streak: Int,
    modifier: Modifier = Modifier,
    large: Boolean = false
) {
    val active = streak > 0
    val bgColor = if (active)
        Brush.linearGradient(listOf(AppColors.Amber, AppColors.Coral))
    else
        Brush.linearGradient(listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.surfaceVariant
        ))

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .padding(horizontal = if (large) 14.dp else 10.dp, vertical = if (large) 8.dp else 6.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text     = if (active) "🔥" else "💤",
                fontSize = if (large) 20.sp else 14.sp
            )
            Text(
                text       = "$streak",
                fontSize   = if (large) 18.sp else 14.sp,
                fontWeight = FontWeight.Black,
                color      = if (active) Color.White
                             else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (large) {
                Text(
                    "дней",
                    fontSize = 12.sp,
                    color    = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  STAT CARD  —  метрика с крупным числом
// ═══════════════════════════════════════════════════════════════
@Composable
fun StatCard(
    label: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier      = modifier,
        shape         = RoundedCornerShape(20.dp),
        color         = accentColor.copy(alpha = 0.09f),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier            = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(icon, fontSize = 24.sp)
            Text(
                text       = value,
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color      = accentColor
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  DAILY GOAL RING  —  кольцо дневной цели
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
        animationSpec = tween(1100, easing = FastOutSlowInEasing),
        label         = "ring"
    )
    val done    = progress >= 1f
    val ringColor = if (done) AppColors.Jade else AppColors.Coral

    Box(modifier = modifier.size(68.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress    = { animProgress },
            modifier    = Modifier.fillMaxSize(),
            strokeWidth = 6.dp,
            color       = ringColor,
            trackColor  = MaterialTheme.colorScheme.surfaceVariant
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = if (done) "✓" else "$todayMinutes",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color      = ringColor
            )
            if (!done) {
                Text(
                    text  = "мин",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color      = MaterialTheme.colorScheme.onBackground
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text(
                    text       = actionLabel,
                    style      = MaterialTheme.typography.labelLarge,
                    color      = AppColors.Coral,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  HERO FEATURE CARD  —  главная карточка с насыщенным градиентом
// ═══════════════════════════════════════════════════════════════
@Composable
fun HeroFeatureCard(
    title: String,
    subtitle: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier   = Modifier,
    badgeText: String?   = null,
    gradientStart: Color = AppColors.Coral,
    gradientEnd: Color   = AppColors.CoralDark
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(gradientStart, gradientEnd)))
            .clickable(onClick = onClick)
    ) {
        // Декоративный круг в углу
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-30).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 20.dp, y = 20.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )

        Row(
            modifier              = Modifier.padding(22.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Иконка
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 32.sp)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text       = title,
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color      = Color.White
                    )
                    if (badgeText != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.28f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text       = badgeText,
                                style      = MaterialTheme.typography.labelMedium,
                                color      = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.82f)
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

// ═══════════════════════════════════════════════════════════════
//  FEATURE CARD  —  ячейка 2×2 сетки
// ═══════════════════════════════════════════════════════════════
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
    Box(
        modifier = modifier
            .shadow(if (enabled) 4.dp else 0.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (enabled) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        // Верхняя цветная полоска
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    if (enabled) accentColor
                    else accentColor.copy(alpha = 0.2f)
                )
        )

        Column(
            modifier            = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (enabled) accentColor.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 22.sp)
                }
                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text       = badgeText,
                            style      = MaterialTheme.typography.labelSmall,
                            color      = accentColor,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color      = if (enabled) MaterialTheme.colorScheme.onSurface
                                 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                )
                Text(
                    text     = subtitle,
                    style    = MaterialTheme.typography.labelMedium,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  LEVEL BADGE
// ═══════════════════════════════════════════════════════════════
@Composable
fun LevelBadge(level: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (level) {
        "A1" -> Pair(AppColors.Jade.copy(alpha = 0.15f),   AppColors.JadeDark)
        "A2" -> Pair(AppColors.Sky.copy(alpha = 0.15f),    AppColors.Sky)
        "B1" -> Pair(AppColors.Amber.copy(alpha = 0.18f),  AppColors.AmberDark)
        "B2" -> Pair(AppColors.Coral.copy(alpha = 0.15f),  AppColors.CoralDark)
        "C1" -> Pair(AppColors.Violet.copy(alpha = 0.15f), AppColors.VioletDark)
        else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text       = level,
            style      = MaterialTheme.typography.labelMedium,
            color      = textColor,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

// ═══════════════════════════════════════════════════════════════
//  WORD OF DAY CARD  —  красивая карточка слова дня
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
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    0f   to AppColors.Violet,
                    0.7f to AppColors.VioletDark,
                    1f   to Color(0xFF3D1A8C)
                )
            )
    ) {
        // Декор
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.TopEnd)
                .offset(x = 25.dp, y = (-25).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )

        Column(
            modifier            = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    Text("✨", fontSize = 14.sp)
                    Text(
                        "Слово дня",
                        style      = MaterialTheme.typography.labelLarge,
                        color      = Color.White.copy(alpha = 0.75f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (wasPracticed) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "✓ изучено",
                            style      = MaterialTheme.typography.labelSmall,
                            color      = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Слово + кнопка озвучки
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text       = spanish,
                    style      = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color      = Color.White
                )
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f))
                        .clickable(onClick = onSpeak),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.VolumeUp,
                        contentDescription = "Произнести",
                        tint     = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text       = russian,
                style      = MaterialTheme.typography.bodyLarge,
                color      = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.SemiBold
            )

            if (example.isNotEmpty()) {
                Text(
                    text      = "« $example »",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = Color.White.copy(alpha = 0.65f),
                    fontStyle = FontStyle.Italic
                )
            }

            if (!wasPracticed) {
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .clickable(onClick = onPractice)
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Тренировать слово →",
                        style      = MaterialTheme.typography.labelLarge,
                        color      = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  BOTTOM NAVIGATION BAR  —  плавающая, с тенью
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
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(20.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            NavigationBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
                modifier       = Modifier.height(64.dp)
            ) {
                bottomNavItems.forEach { item ->
                    val selected = currentRoute.startsWith(item.route)
                    NavigationBarItem(
                        selected = selected,
                        onClick  = { onNavigate(item.route) },
                        icon = {
                            AnimatedContent(
                                targetState = selected,
                                transitionSpec = {
                                    scaleIn(tween(200)) + fadeIn(tween(200)) togetherWith
                                    scaleOut(tween(200)) + fadeOut(tween(200))
                                },
                                label = "nav_icon"
                            ) { isSelected ->
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(AppColors.Coral.copy(alpha = 0.12f))
                                            .padding(horizontal = 12.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector        = item.iconSelected,
                                            contentDescription = item.label,
                                            tint               = AppColors.Coral,
                                            modifier           = Modifier.size(22.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector        = item.icon,
                                        contentDescription = item.label,
                                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier           = Modifier.size(22.dp)
                                    )
                                }
                            }
                        },
                        label = {
                            Text(
                                text       = item.label,
                                style      = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Normal,
                                color      = if (selected) AppColors.Coral
                                             else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = AppColors.Coral,
                            selectedTextColor   = AppColors.Coral,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor      = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}
