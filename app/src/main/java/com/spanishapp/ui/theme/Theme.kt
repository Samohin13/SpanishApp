package com.spanishapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────
// COLORS
// ─────────────────────────────────────────────────────────────
object AppColors {
    // Spanish flag palette — warm terracotta + gold
    val Terracotta     = Color(0xFFD85A30)
    val TerracottaLight= Color(0xFFF0997B)
    val TerracottaDark = Color(0xFF993C1D)

    val Gold           = Color(0xFFEF9F27)
    val GoldLight      = Color(0xFFFAC775)
    val GoldDark       = Color(0xFFBA7517)

    val Teal           = Color(0xFF1D9E75)
    val TealLight      = Color(0xFF9FE1CB)
    val TealDark       = Color(0xFF0F6E56)

    val Ink            = Color(0xFF1A1A18)
    val InkMedium      = Color(0xFF3D3D3A)
    val InkLight       = Color(0xFF73726C)

    val Cream          = Color(0xFFF9F7F2)
    val CreamDark      = Color(0xFFEFEDE8)
    val Surface        = Color(0xFFFFFFFF)

    // Semantic
    val Success        = Color(0xFF1D9E75)
    val Warning        = Color(0xFFEF9F27)
    val Error          = Color(0xFFE24B4A)
    val Info           = Color(0xFF378ADD)

    // XP / Level bar
    val XpStart        = Color(0xFFEF9F27)
    val XpEnd          = Color(0xFFD85A30)

    // Dark mode surfaces
    val DarkBackground = Color(0xFF141412)
    val DarkSurface    = Color(0xFF1E1E1B)
    val DarkSurface2   = Color(0xFF282824)
    val DarkInk        = Color(0xFFF0EEE8)
}

// ─────────────────────────────────────────────────────────────
// TYPOGRAPHY  —  Nunito (rounded, friendly for learning app)
// Add Nunito to res/font/ or use Google Fonts
// ─────────────────────────────────────────────────────────────
// If you don't have Nunito yet, use this fallback until you add the font file:
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 24.sp,
        lineHeight = 32.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 20.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// ─────────────────────────────────────────────────────────────
// LIGHT COLOR SCHEME
// ─────────────────────────────────────────────────────────────
private val LightColors = lightColorScheme(
    primary          = AppColors.Terracotta,
    onPrimary        = Color.White,
    primaryContainer = AppColors.TerracottaLight,
    onPrimaryContainer = AppColors.TerracottaDark,

    secondary        = AppColors.Teal,
    onSecondary      = Color.White,
    secondaryContainer = AppColors.TealLight,
    onSecondaryContainer = AppColors.TealDark,

    tertiary         = AppColors.Gold,
    onTertiary       = AppColors.Ink,
    tertiaryContainer= AppColors.GoldLight,
    onTertiaryContainer = AppColors.GoldDark,

    background       = AppColors.Cream,
    onBackground     = AppColors.Ink,
    surface          = AppColors.Surface,
    onSurface        = AppColors.Ink,
    surfaceVariant   = AppColors.CreamDark,
    onSurfaceVariant = AppColors.InkMedium,

    error            = AppColors.Error,
    onError          = Color.White,

    outline          = AppColors.InkLight.copy(alpha = 0.3f),
    outlineVariant   = AppColors.InkLight.copy(alpha = 0.15f)
)

// ─────────────────────────────────────────────────────────────
// DARK COLOR SCHEME
// ─────────────────────────────────────────────────────────────
private val DarkColors = darkColorScheme(
    primary          = AppColors.TerracottaLight,
    onPrimary        = AppColors.TerracottaDark,
    primaryContainer = AppColors.TerracottaDark,
    onPrimaryContainer = AppColors.TerracottaLight,

    secondary        = AppColors.TealLight,
    onSecondary      = AppColors.TealDark,
    secondaryContainer = AppColors.TealDark,
    onSecondaryContainer = AppColors.TealLight,

    tertiary         = AppColors.GoldLight,
    onTertiary       = AppColors.GoldDark,
    tertiaryContainer= AppColors.GoldDark,
    onTertiaryContainer = AppColors.GoldLight,

    background       = AppColors.DarkBackground,
    onBackground     = AppColors.DarkInk,
    surface          = AppColors.DarkSurface,
    onSurface        = AppColors.DarkInk,
    surfaceVariant   = AppColors.DarkSurface2,
    onSurfaceVariant = AppColors.DarkInk.copy(alpha = 0.7f),

    error            = Color(0xFFFF8A80),
    onError          = Color(0xFF690005),

    outline          = Color.White.copy(alpha = 0.2f),
    outlineVariant   = Color.White.copy(alpha = 0.1f)
)

// ─────────────────────────────────────────────────────────────
// THEME COMPOSABLE
// ─────────────────────────────────────────────────────────────
@Composable
fun SpanishAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        shapes      = Shapes(
            extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
            small      = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            medium     = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            large      = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
        ),
        content = content
    )
}