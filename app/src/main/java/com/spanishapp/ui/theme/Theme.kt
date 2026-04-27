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
    // Испанская палитра (Modern & Balanced)
    val Terracotta = Color(0xFFC62828)  // Глубокий красный (Кровь и песок)
    val Olive      = Color(0xFF558B2F)  // Оливковый (Природа Испании)
    val Ochre      = Color(0xFFF9A825)  // Золотистая охра (Солнце)
    
    // Совместимость со старым кодом
    val Teal       = Olive
    val Gold       = Ochre
    val GoldDark   = Ochre

    // Светлая тема
    val L_Bg            = Color(0xFFFDFCF9)
    val L_Surface       = Color(0xFFFFFFFF)
    val L_Ink           = Color(0xFF201A1A)
    
    // Тёмная тема
    val D_Bg            = Color(0xFF141212)
    val D_Surface       = Color(0xFF1C1B1F)
    val D_Ink           = Color(0xFFEDE0E0)
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
    tertiaryContainer = Color(0xFFFFE082),
    onTertiaryContainer = Color(0xFF261900),
    
    background = AppColors.L_Bg,
    surface = Color.White,
    onSurface = AppColors.L_Ink,
    surfaceVariant = Color(0xFFF5EEEE),
    onSurfaceVariant = Color(0xFF534343),
    outline = Color(0xFF857372)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB4A9),
    onPrimary = Color(0xFF690002),
    primaryContainer = Color(0xFF930006),
    onPrimaryContainer = Color(0xFFFFDAD4),
    
    secondary = Color(0xFFBACCB3),
    onSecondary = Color(0xFF283420),
    secondaryContainer = Color(0xFF3E4A35),
    onSecondaryContainer = Color(0xFFD7E8DE),
    
    tertiary = Color(0xFFFFCC00),
    onTertiary = Color(0xFF422C00),
    
    background = AppColors.D_Bg,
    surface = AppColors.D_Surface,
    onSurface = AppColors.D_Ink,
    surfaceVariant = Color(0xFF534343),
    onSurfaceVariant = Color(0xFFD8C2C1),
    outline = Color(0xFFA08C8B)
)

@Composable
fun SpanishAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
