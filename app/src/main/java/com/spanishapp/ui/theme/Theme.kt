package com.spanishapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AppColors {
    // ── Purple spectrum (primary) ─────────────────────────────
    val Purple       = Color(0xFF7B2FBE)
    val PurpleLight  = Color(0xFF9C4FDC)
    val PurplePale   = Color(0xFFF3E8FF)
    val PurplePill   = Color(0xFFEDE0F8)

    // ── Gradient accent ───────────────────────────────────────
    val Pink         = Color(0xFFE040FB)

    // ── Stats ─────────────────────────────────────────────────
    val Gold         = Color(0xFFFF9500)
    val Orange       = Color(0xFFFF6B00)

    // ── Backgrounds & surfaces ────────────────────────────────
    val BgWhite      = Color(0xFFFFFFFF)
    val BgLight      = Color(0xFFF8F8FA)
    val CardBg       = Color(0xFFFFFFFF)
    val BorderColor  = Color(0xFFE5E5EA)

    // ── Text ──────────────────────────────────────────────────
    val TextPrimary   = Color(0xFF1A1A1A)
    val TextSecondary = Color(0xFF8E8E93)
    val TextTertiary  = Color(0xFFAEAEB2)
    val LockGray      = Color(0xFFC7C7CC)

    // ── Compatibility aliases (used by game screens) ──────────
    val Amber      = Gold
    val Olive      = Purple
    val Terracotta = Purple
    val Ochre      = Gold
    val Teal       = PurpleLight
    val Primary    = Purple
    val GoldDark   = Gold
    val Info       = PurpleLight
    val Success    = Color(0xFF34C759)
    val Warning    = Gold
    val Error      = Color(0xFFFF3B30)
    val Red        = Color(0xFFFF3B30)
    val Coral      = Color(0xFFFF6B6B)

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
    val Surface3   = Color(0xFFEEEEF2)
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

private val LightColors = lightColorScheme(
    primary              = AppColors.Purple,
    onPrimary            = Color.White,
    primaryContainer     = AppColors.PurplePale,
    onPrimaryContainer   = AppColors.Purple,

    secondary            = AppColors.Pink,
    onSecondary          = Color.White,
    secondaryContainer   = AppColors.PurplePill,
    onSecondaryContainer = AppColors.Purple,

    tertiary             = AppColors.Gold,
    onTertiary           = Color.White,

    background           = AppColors.BgWhite,
    onBackground         = AppColors.TextPrimary,

    surface              = AppColors.CardBg,
    onSurface            = AppColors.TextPrimary,
    surfaceVariant       = AppColors.BgLight,
    surfaceContainer     = AppColors.BgLight,
    onSurfaceVariant     = AppColors.TextSecondary,

    outline              = AppColors.BorderColor,
    outlineVariant       = AppColors.BorderColor,

    error                = AppColors.Error
)

@Composable
fun SpanishAppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography  = AppTypography,
        shapes = Shapes(
            small  = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(16.dp),
            large  = RoundedCornerShape(24.dp)
        ),
        content = content
    )
}
