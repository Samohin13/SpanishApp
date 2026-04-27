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
    
    // Единый цвет для баров (Top & Bottom)
    val BarColorL  = Color(0xFFFFFFFF)  // Чистый белый для светлой темы
    val BarColorD  = Color(0xFF1C1B1F)  // Глубокий темный для темной темы

    // Светлая тема
    val L_Bg            = Color(0xFFFDFCF9)   // Тёплый "бумажный" белый
    val L_Surface       = Color(0xFFFFFFFF)
    val L_Ink           = Color(0xFF201A1A)   // Почти чёрный с красным подтоном
    
    // Тёмная тема
    val D_Bg            = Color(0xFF141212)
    val D_Surface       = Color(0xFF1C1B1F)
    val D_Ink           = Color(0xFFEDE0E0)
}

val AppTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, letterSpacing = (-1).sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
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
    
    tertiary = AppColors.Ochre,
    onTertiary = Color.Black,
    
    background = AppColors.L_Bg,
    surface = AppColors.BarColorL,
    onSurface = AppColors.L_Ink,
    surfaceVariant = Color(0xFFF5EEEE)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB4A9),
    onPrimary = Color(0xFF690002),
    
    secondary = Color(0xFFBACCB3),
    onSecondary = Color(0xFF283420),
    
    background = AppColors.D_Bg,
    surface = AppColors.BarColorD,
    onSurface = AppColors.D_Ink
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
            medium = RoundedCornerShape(24.dp),
            large = RoundedCornerShape(32.dp)
        ),
        content = content
    )
}
