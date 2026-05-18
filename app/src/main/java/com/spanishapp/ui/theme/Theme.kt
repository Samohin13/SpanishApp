package com.spanishapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

object AppColors {
    // ── Sunset over Barcelona (primary) ──────────────────────
    val Purple       = Color(0xFFFF6B35)   // Orange — primary CTAs, nav, active states
    val PurpleLight  = Color(0xFFFF8B5C)   // Orange-light — gradient second stop
    val PurplePale   = Color(0xFFFFF1E6)   // Peach — pill backgrounds, tints
    val PurplePill   = Color(0xFFFFDECF)   // Peach dark — selected chip background

    // ── Accent ────────────────────────────────────────────────
    val Pink         = Color(0xFFD62867)   // Magenta — premium, accents, gradient end

    // ── Stats ─────────────────────────────────────────────────
    val Gold         = Color(0xFFFFB400)   // Sun — XP, level badges
    val Orange       = Color(0xFFFF5C35)   // Deep orange — streak fire

    // ── Backgrounds & surfaces ────────────────────────────────
    val BgWhite      = Color(0xFFFFFFFF)
    val BgLight      = Color(0xFFF8F8FA)   // Cool gray — SpanishBackground
    val BgGray       = Color(0xFFF0F0F5)   // Home wrapper
    val CardBg       = Color(0xFFFFFFFF)
    val BorderColor  = Color(0xFFE5E5EA)   // Cool gray border

    // ── Text ──────────────────────────────────────────────────
    val TextPrimary   = Color(0xFF1A1A1A)  // Near-black primary text
    val TextSecondary = Color(0xFF8E8E93)
    val TextTertiary  = Color(0xFFAEAEB2)
    val LockGray      = Color(0xFFC7C7CC)

    // ── Compatibility aliases ─────────────────────────────────
    val Amber      = Gold
    val Olive      = Purple
    val Terracotta = Purple
    val Ochre      = Gold
    val Teal       = Purple
    val Primary    = Purple
    val GoldDark   = Gold
    val Info       = Pink
    val Success    = Color(0xFF34C759)
    val Warning    = Gold
    val Error      = Color(0xFFFF3B30)
    val Red        = Color(0xFFFF3B30)
    val Coral      = Pink

    // ── Legacy surface names ──────────────────────────────────
    val D_Bg             = BgWhite
    val D_Surface        = BgLight
    val D_SurfaceVariant = BorderColor
    val D_Ink            = TextPrimary
    val L_Bg             = BgWhite
    val L_Surface        = CardBg
    val L_Ink            = TextPrimary

    // ── Legacy dark surface names (kept for compatibility) ────
    val BgDeep     = BgWhite
    val Surface1   = BgLight
    val Surface2   = BorderColor
    val Surface3   = Color(0xFFEEECE8)
    val Divider    = BorderColor
}

val AppTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, letterSpacing = (-1).sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 28.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 24.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 20.sp),
    titleLarge  = TextStyle(fontWeight = FontWeight.Bold,        fontSize = 18.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold,    fontSize = 16.sp),
    bodyLarge   = TextStyle(fontWeight = FontWeight.Normal,      fontSize = 16.sp),
    bodyMedium  = TextStyle(fontWeight = FontWeight.Normal,      fontSize = 14.sp),
    labelLarge  = TextStyle(fontWeight = FontWeight.SemiBold,    fontSize = 13.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium,      fontSize = 12.sp),
    labelSmall  = TextStyle(fontWeight = FontWeight.Bold,        fontSize = 11.sp, letterSpacing = 0.5.sp)
)

// v1.17.0: Premium light theme — Apple HIG × Material 3 hybrid.
// Чисто-белый background, 5-уровневая иерархия surfaces, iOS-style
// secondary text (#6E6E73), тонкие separators (#C6C6C8).
private val LightColors = lightColorScheme(
    primary              = AppColors.Purple,            // #FF6B35 brand orange
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFFFE5DA),           // Мягкий персиковый tint
    onPrimaryContainer   = Color(0xFFC24A1A),           // Deep burnt orange — текст на tint
    inversePrimary       = Color(0xFFFFAB8B),

    secondary            = AppColors.Pink,              // #D62867 magenta
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFFFD0DC),
    onSecondaryContainer = Color(0xFF8A1944),

    tertiary             = AppColors.Gold,              // #FFB400
    onTertiary           = Color(0xFF1A1100),
    tertiaryContainer    = Color(0xFFFFE7A6),
    onTertiaryContainer  = Color(0xFF6D4C00),

    background           = Color(0xFFFFFFFF),           // Чисто-белый Apple
    onBackground         = Color(0xFF1C1C1E),           // Apple primary text

    surface                  = Color(0xFFFFFFFF),       // Карточки
    onSurface                = Color(0xFF1C1C1E),
    surfaceVariant           = Color(0xFFF2F2F7),       // iOS systemGrouped
    onSurfaceVariant         = Color(0xFF6E6E73),       // iOS secondary 65%
    surfaceContainerLowest   = Color(0xFFFFFFFF),
    surfaceContainerLow      = Color(0xFFF8F8FA),
    surfaceContainer         = Color(0xFFF2F2F7),
    surfaceContainerHigh     = Color(0xFFEAEAEF),
    surfaceContainerHighest  = Color(0xFFE5E5EA),

    outline              = Color(0xFFC6C6C8),           // iOS-style separator
    outlineVariant       = Color(0xFFE5E5EA),           // Слабый divider

    inverseSurface       = Color(0xFF1C1C1E),
    inverseOnSurface     = Color(0xFFFFFFFF),

    error                = Color(0xFFFF3B30),
    onError              = Color.White,
    errorContainer       = Color(0xFFFFD8D4),
    onErrorContainer     = Color(0xFF8B1F1A),

    scrim                = Color(0xFF000000)
)

// v1.17.0: Dark theme — НЕ ТРОГАЕМ значения (тщательная гарантия что
// существующая dark выглядит идентично). Добавлены только новые slots
// (surfaceContainerLow/High/Highest) с тёмными hex'ами в общем стиле.
private val DarkColors = darkColorScheme(
    primary              = AppColors.Purple,           // Keep brand orange — readable on dark
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFF5C2A0F),          // Deep burnt orange container
    onPrimaryContainer   = Color(0xFFFFDECF),
    inversePrimary       = Color(0xFFFFAB8B),

    secondary            = AppColors.Pink,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFF4A0F2A),
    onSecondaryContainer = Color(0xFFFFD0DE),

    tertiary             = AppColors.Gold,
    onTertiary           = Color(0xFF1A1100),
    tertiaryContainer    = Color(0xFF6D4C00),
    onTertiaryContainer  = Color(0xFFFFE7A6),

    background           = Color(0xFF121212),
    onBackground         = Color(0xFFEDEDED),

    surface                  = Color(0xFF1C1C1E),
    onSurface                = Color(0xFFEDEDED),
    surfaceVariant           = Color(0xFF2C2C2E),
    onSurfaceVariant         = Color(0xFFB0B0B0),
    surfaceContainerLowest   = Color(0xFF0F0F11),
    surfaceContainerLow      = Color(0xFF1C1C1E),
    surfaceContainer         = Color(0xFF1C1C1E),
    surfaceContainerHigh     = Color(0xFF2C2C2E),
    surfaceContainerHighest  = Color(0xFF3A3A3C),

    outline              = Color(0xFF3A3A3C),
    outlineVariant       = Color(0xFF2C2C2E),

    inverseSurface       = Color(0xFFEDEDED),
    inverseOnSurface     = Color(0xFF1C1C1E),

    error                = AppColors.Error,
    onError              = Color.White,
    errorContainer       = Color(0xFF5C1A1A),
    onErrorContainer     = Color(0xFFFFD8D4),

    scrim                = Color(0xFF000000)
)

@Composable
fun SpanishAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        shapes = Shapes(
            small  = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(16.dp),
            large  = RoundedCornerShape(24.dp)
        ),
        content = content
    )
}
