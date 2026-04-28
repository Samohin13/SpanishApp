package com.spanishapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ═══════════════════════════════════════════════════════════════
//  ESPAÑA MODERN — Design System (Material 3)
//  Палитра: Тёплая терракота, оливковый, глубокий золотой.
// ═══════════════════════════════════════════════════════════════

object AppColors {
    // ── Fire spectrum: gold → amber → orange → red ────────────
    val Gold       = Color(0xFFFFD60A)   // яркое золото
    val Amber      = Color(0xFFFF9F0A)   // тёплый янтарь
    val Orange     = Color(0xFFFF6B00)   // глубокий оранж
    val Coral      = Color(0xFFFF4D30)   // кораллово-красный
    val Red        = Color(0xFFFF3B30)   // насыщенный красный

    // ── Тёмные поверхности (iOS Dark / Samsung One UI) ────────
    val BgDeep     = Color(0xFF0D0D0D)   // основной фон
    val Surface1   = Color(0xFF1C1C1E)   // карточки — iOS dark
    val Surface2   = Color(0xFF2C2C2E)   // elevated
    val Surface3   = Color(0xFF3A3A3C)   // ещё выше
    val Divider    = Color(0x14FFFFFF)   // белый 8% — тонкие границы

    // ── Текст ──────────────────────────────────────────────────
    val TextPrimary   = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF8E8E93)   // iOS secondary
    val TextTertiary  = Color(0xFF636366)   // iOS tertiary

    // ── Алиасы для совместимости с остальным кодом ─────────────
    val Terracotta  = Red
    val Olive       = Amber
    val Ochre       = Gold
    val Teal        = Orange
    val GoldDark    = Amber
    val Info        = Amber
    val Success     = Orange
    val Warning     = Gold
    val Error       = Red
    val Primary     = Amber

    // ── Legacy surface names ────────────────────────────────────
    val D_Bg             = BgDeep
    val D_Surface        = Surface1
    val D_SurfaceVariant = Surface2
    val D_Ink            = TextPrimary
    val L_Bg             = Color(0xFFFDFCF9)
    val L_Surface        = Color(0xFFFFFFFF)
    val L_Ink            = Color(0xFF1A1C1E)
}

val AppTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, letterSpacing = (-1).sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
)

private val LightColors = lightColorScheme(
    primary = AppColors.Terracotta,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD4),
    onPrimaryContainer = Color(0xFF410001),
    secondary = AppColors.Olive,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E8DE),
    onSecondaryContainer = Color(0xFF121F16),
    tertiary = AppColors.Ochre,
    onTertiary = Color.White,
    background = AppColors.L_Bg,
    surface = Color.White,
    onSurface = AppColors.L_Ink,
    surfaceVariant = Color(0xFFF5EEEE),
    onSurfaceVariant = Color(0xFF534343),
    outline = Color(0xFF857372)
)

private val DarkColors = darkColorScheme(
    primary            = AppColors.Amber,
    onPrimary          = Color(0xFF1A0A00),
    primaryContainer   = AppColors.Orange.copy(alpha = 0.25f),
    onPrimaryContainer = AppColors.Gold,

    secondary          = AppColors.Gold,
    onSecondary        = Color(0xFF1A1200),
    secondaryContainer = AppColors.Gold.copy(alpha = 0.15f),
    onSecondaryContainer = AppColors.Gold,

    tertiary           = AppColors.Coral,
    onTertiary         = Color(0xFF1A0500),

    background         = AppColors.BgDeep,
    surface            = AppColors.Surface1,
    onSurface          = AppColors.TextPrimary,
    surfaceVariant     = AppColors.Surface2,
    surfaceContainer   = AppColors.Surface1,
    onSurfaceVariant   = AppColors.TextSecondary,
    outline            = AppColors.Divider,
    error              = AppColors.Red
)

@Composable
fun SpanishAppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(24.dp),
            large = RoundedCornerShape(32.dp)
        ),
        content = content
    )
}
