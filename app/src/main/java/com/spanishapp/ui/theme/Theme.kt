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
    val BgLight      = Color(0xFFFFF8F2)   // Warm peach tint for page wrappers
    val CardBg       = Color(0xFFFFFFFF)
    val BorderColor  = Color(0xFFE8E5E0)   // Warm gray border

    // ── Text ──────────────────────────────────────────────────
    val TextPrimary   = Color(0xFF264653)  // Ocean — headlines, body text
    val TextSecondary = Color(0xFF8A8A93)
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

private val LightColors = lightColorScheme(
    primary              = AppColors.Purple,        // Orange #FF6B35
    onPrimary            = Color.White,
    primaryContainer     = AppColors.PurplePale,    // Peach #FFF1E6
    onPrimaryContainer   = Color(0xFF8B2500),       // Deep burnt orange for text on peach

    secondary            = AppColors.Pink,          // Magenta #D62867
    onSecondary          = Color.White,
    secondaryContainer   = AppColors.PurplePill,    // Peach dark #FFDECC
    onSecondaryContainer = AppColors.Pink,

    tertiary             = AppColors.Gold,          // Sun #FFB400
    onTertiary           = Color.White,

    background           = AppColors.BgWhite,
    onBackground         = AppColors.TextPrimary,   // Ocean #264653

    surface              = AppColors.CardBg,
    onSurface            = AppColors.TextPrimary,
    surfaceVariant       = AppColors.BgLight,       // Warm peach tint #FFF8F2
    surfaceContainer     = AppColors.BgLight,
    onSurfaceVariant     = AppColors.TextSecondary,

    outline              = AppColors.BorderColor,   // Warm gray #E8E5E0
    outlineVariant       = AppColors.BorderColor,

    error                = AppColors.Error
)

@Composable
fun SpanishAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColors // Currently only light theme supported as per colors
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
