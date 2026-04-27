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
    val Terracotta = Color(0xFFC62828)
    val Olive      = Color(0xFF558B2F)
    val Ochre      = Color(0xFFF9A825)
    
    // Совместимость
    val Teal       = Olive
    val Gold       = Ochre
    val GoldDark   = Ochre
    val Info       = Olive
    val Success    = Olive
    val Warning    = Ochre
    val Error      = Terracotta

    // Светлая тема
    val L_Bg            = Color(0xFFFDFCF9)
    val L_Surface       = Color(0xFFFFFFFF)
    val L_Ink           = Color(0xFF1A1C1E)
    
    // Тёмная тема — "Midnight Premium"
    val D_Bg            = Color(0xFF121212)   // Тёмный серый, по стандартам Material
    val D_Surface       = Color(0xFF1E1E1E)   // Карточки заметно светлее
    val D_SurfaceVariant= Color(0xFF2C2C2C)   // Уроки еще светлее
    val D_Ink           = Color(0xFFE0E0E0)   // Мягкий белый
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
    primary = Color(0xFFFFB4A9),       // Нежный розово-красный
    onPrimary = Color(0xFF690002),
    primaryContainer = Color(0xFF930006),
    onPrimaryContainer = Color(0xFFFFDAD4),
    
    secondary = Color(0xFFBACCB3),      // Пастельный оливковый
    onSecondary = Color(0xFF283420),
    secondaryContainer = Color(0xFF3E4A35),
    onSecondaryContainer = Color(0xFFD7E8DE),
    
    tertiary = Color(0xFFFFCC00),
    onTertiary = Color(0xFF422C00),
    
    background = AppColors.D_Bg,
    surface = AppColors.D_Surface,
    onSurface = AppColors.D_Ink,
    surfaceVariant = AppColors.D_SurfaceVariant,
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF444444)
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
