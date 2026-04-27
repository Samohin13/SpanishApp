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
//  OLIVA — Design System
//  Палитра из 5 цветов: #F6C445 · #F05A28 · #8BC34A · #FFF3D6 · #3B2F2F
//  Тёплая, испанская, природная. Без холодных серых.
// ═══════════════════════════════════════════════════════════════

object AppColors {

    // ────────────────────────────────────────────────────────
    //  БРЕНД-ЦВЕТА из палитры (неизменны)
    // ────────────────────────────────────────────────────────
    val PaletteYellow  = Color(0xFFF6C445)   // золотисто-жёлтый
    val PaletteOrange  = Color(0xFFF05A28)   // оранжево-красный
    val PaletteGreen   = Color(0xFF8BC34A)   // оливковый
    val PaletteCream   = Color(0xFFFFF3D6)   // тёплый крем
    val PaletteEspresso= Color(0xFF3B2F2F)   // тёмный эспрессо

    // ────────────────────────────────────────────────────────
    //  LIGHT MODE  —  кремовый фон, зелёные акценты
    // ────────────────────────────────────────────────────────
    val L_Bg            = Color(0xFFF8F9FA)   // Светлый, почти белый фон (как в премиум-приложениях)
    val L_Surface       = Color(0xFFFFFFFF)
    val L_Surface2      = Color(0xFFF1F3F5)
    val L_Surface3      = Color(0xFFE9ECEF)
    val L_Ink           = Color(0xFF1A1C1E)   // Глубокий темный
    val L_InkMid        = Color(0xFF44474E)
    val L_InkDim        = Color(0xFF74777F)
    val L_InkFaint      = Color(0xFFC4C6D0)

    // ────────────────────────────────────────────────────────
    //  DARK MODE  —  почти чёрный с еле слышной теплотой.
    //  Коричневого НЕТ — только нейтральная тьма + яркие акценты.
    //  Уровни поверхностей создают глубину без теней.
    // ────────────────────────────────────────────────────────
    val D_Bg            = Color(0xFF0E0E0C)   // почти чёрный, чуть тёплый
    val D_Surface       = Color(0xFF181816)   // карточки — заметный шаг
    val D_Surface2      = Color(0xFF232320)   // вторичные
    val D_Surface3      = Color(0xFF2E2E2A)   // ещё выше — ощутимый подъём
    val D_Ink           = Color(0xFFF2EEE8)   // почти белый, чуть тёплый
    val D_InkMid        = Color(0xFFB0AA9E)   // вторичный
    val D_InkDim        = Color(0xFF787068)   // подсказки
    val D_InkFaint      = Color(0xFF504A44)   // отключённое

    // ────────────────────────────────────────────────────────
    //  PRIMARY — зелёный оливковый
    // ────────────────────────────────────────────────────────
    val Green           = Color(0xFF8BC34A)   // из палитры
    val GreenDark       = Color(0xFF558B2F)   // тёмный (нажатие)
    val GreenOnDark     = Color(0xFFB4E05A)   // ярче на почти-чёрном — горит!
    val GreenTintL      = Color(0xFFE8F5D0)   // подложка light
    val GreenTintD      = Color(0xFF1A3005)   // тёмная подложка: почти чёрный с зелёным

    // ────────────────────────────────────────────────────────
    //  SECONDARY — оранжевый (акцент)
    // ────────────────────────────────────────────────────────
    val Orange          = Color(0xFFF05A28)   // из палитры
    val OrangeDark      = Color(0xFFBF3600)
    val OrangeOnDark    = Color(0xFFFF7043)   // насыщенный — виден на чёрном
    val OrangeTintL     = Color(0xFFFFE0D4)
    val OrangeTintD     = Color(0xFF3A1200)   // глубокий тёмный контейнер

    // ────────────────────────────────────────────────────────
    //  TERTIARY — жёлтый (стрик, достижения, XP)
    // ────────────────────────────────────────────────────────
    val Yellow          = Color(0xFFF6C445)   // из палитры
    val YellowDark      = Color(0xFFBF8E00)
    val YellowOnDark    = Color(0xFFFFCF33)   // золото на чёрном — роскошно
    val YellowTintL     = Color(0xFFFEF3C7)
    val YellowTintD     = Color(0xFF2E2000)   // почти чёрный с золотым

    // ────────────────────────────────────────────────────────
    //  Псевдонимы (совместимость со старым кодом)
    // ────────────────────────────────────────────────────────
    val Terracotta      = Orange
    val TerracottaLight = OrangeOnDark
    val TerracottaDark  = OrangeDark
    val TerracottaSoft  = OrangeTintL
    val TerracottaBg    = OrangeTintL
    val Coral           = Orange
    val CoralLight      = OrangeOnDark
    val CoralDark       = OrangeDark
    val CoralSurface    = OrangeTintL
    val Olive           = Green
    val OliveSoft       = GreenTintL
    val OliveBg         = GreenTintL
    val OliveDark       = GreenDark
    val Ochre           = Yellow
    val OchreSoft       = YellowTintL
    val OchreBg         = YellowTintL
    val OchreDark       = YellowDark
    val Indigo          = Green
    val IndigoSoft      = GreenTintL
    val IndigoBg        = GreenTintL
    val IndigoDark      = GreenDark
    val Rose            = Orange
    val RoseLight       = OrangeOnDark
    val RoseDeep        = OrangeDark
    val RoseTintLight   = OrangeTintL
    val RoseTintDark    = OrangeTintD
    val Violet          = Green
    val VioletL         = GreenOnDark
    val VioletTintL     = GreenTintL
    val VioletTintD     = GreenTintD
    val VioletSurface   = GreenTintL
    val VioletDark      = GreenDark
    val Gold            = Yellow
    val GoldLight       = YellowOnDark
    val GoldDark        = YellowDark
    val Teal            = Green
    val TealLight       = GreenOnDark
    val TealDark        = GreenDark
    val Jade            = Green
    val JadeLight       = GreenOnDark
    val JadeDark        = GreenDark
    val JadeSurface     = GreenTintL
    val Sky             = Green
    val SkyL            = GreenOnDark
    val SkyTintL        = GreenTintL
    val SkyTintD        = GreenTintD
    val SkySurface      = GreenTintL
    val Emerald         = Green
    val EmeraldL        = GreenOnDark
    val EmeraldTintL    = GreenTintL
    val EmeraldTintD    = GreenTintD
    val Amber           = Yellow
    val AmberL          = YellowOnDark
    val AmberTintL      = YellowTintL
    val AmberTintD      = YellowTintD
    val AmberSurface    = YellowTintL
    val Info            = Green
    val Success         = Green
    val Warning         = Yellow
    val Error           = Orange
    val Paper           = L_Bg
    val Surface         = L_Surface
    val SurfaceMuted    = L_Surface2
    val Border          = L_Surface3
    val Ink             = L_Ink
    val InkMid          = L_InkMid
    val InkLight        = L_InkDim
    val InkFaint        = L_InkFaint
    val InkMedium       = L_InkMid
    val Cream           = L_Bg
    val CreamMid        = L_Surface2
    val CreamDark       = L_Surface2
    val CreamDeep       = L_Surface3
    val DarkBg          = D_Bg
    val DarkSurf        = D_Surface
    val DarkSurfMuted   = D_Surface2
    val DarkBorder      = D_Surface2
    val DarkInk         = D_Ink
    val DarkInkMid      = D_InkMid
    val DarkInkLight    = D_InkDim
    val DarkBackground  = D_Bg
    val DarkSurface     = D_Surface
    val DarkSurface2    = D_Surface2
    val DarkSurf2       = D_Surface2
    val DarkSurf3       = D_Surface3
    val DarkText        = D_Ink
    val XpGradientStart = Yellow
    val XpGradientEnd   = Orange
}

// ═══════════════════════════════════════════════════════════════
//  TYPOGRAPHY — тёплая, чёткая иерархия
// ═══════════════════════════════════════════════════════════════
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 34.sp,
        lineHeight    = 40.sp,
        letterSpacing = (-1).sp
    ),
    displayMedium = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 28.sp,
        lineHeight    = 34.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 22.sp,
        lineHeight    = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 20.sp,
        lineHeight    = 28.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineSmall = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 18.sp,
        lineHeight    = 26.sp
    ),
    titleLarge = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 17.sp,
        lineHeight    = 24.sp
    ),
    titleMedium = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 15.sp,
        lineHeight    = 22.sp
    ),
    titleSmall = TextStyle(
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        lineHeight    = 20.sp
    ),
    bodyLarge = TextStyle(
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        lineHeight    = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 21.sp
    ),
    bodySmall = TextStyle(
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 18.sp
    ),
    labelLarge = TextStyle(
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight    = FontWeight.Medium,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.2.sp
    ),
    labelSmall = TextStyle(
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 15.sp,
        letterSpacing = 0.3.sp
    )
)

// ═══════════════════════════════════════════════════════════════
//  LIGHT — кремовый фон, зелёный primary, оранжевый secondary
// ═══════════════════════════════════════════════════════════════
private val LightColors = lightColorScheme(
    primary             = AppColors.Green,
    onPrimary           = Color.White,
    primaryContainer    = AppColors.GreenTintL,
    onPrimaryContainer  = AppColors.GreenDark,

    secondary           = AppColors.Orange,
    onSecondary         = Color.White,
    secondaryContainer  = AppColors.OrangeTintL,
    onSecondaryContainer = AppColors.OrangeDark,

    tertiary            = AppColors.Yellow,
    onTertiary          = AppColors.PaletteEspresso,
    tertiaryContainer   = AppColors.YellowTintL,
    onTertiaryContainer = AppColors.YellowDark,

    background          = AppColors.L_Bg,
    onBackground        = AppColors.L_Ink,

    surface             = AppColors.L_Surface,
    onSurface           = AppColors.L_Ink,
    surfaceVariant      = AppColors.L_Surface2,
    onSurfaceVariant    = AppColors.L_InkMid,

    surfaceContainer        = AppColors.L_Surface2,
    surfaceContainerLow     = AppColors.L_Surface,
    surfaceContainerHigh    = AppColors.L_Surface3,
    surfaceContainerHighest = AppColors.L_Surface3,

    error               = AppColors.Orange,
    onError             = Color.White,

    outline             = AppColors.L_Surface3,
    outlineVariant      = AppColors.L_Surface2
)

// ═══════════════════════════════════════════════════════════════
//  DARK — глубокий эспрессо, мягкий зелёный, яркий оранжевый
// ═══════════════════════════════════════════════════════════════
private val DarkColors = darkColorScheme(
    // Зелёный горит на почти-чёрном — выглядит дорого
    primary             = AppColors.GreenOnDark,
    onPrimary           = Color(0xFF0A1F00),
    primaryContainer    = AppColors.GreenTintD,
    onPrimaryContainer  = Color(0xFFCCF080),

    // Оранжевый — яркий акцент
    secondary           = AppColors.OrangeOnDark,
    onSecondary         = Color(0xFF200800),
    secondaryContainer  = AppColors.OrangeTintD,
    onSecondaryContainer = Color(0xFFFFB59A),

    // Золото — стрик, XP, трофеи
    tertiary            = AppColors.YellowOnDark,
    onTertiary          = Color(0xFF180E00),
    tertiaryContainer   = AppColors.YellowTintD,
    onTertiaryContainer = Color(0xFFFFE580),

    background          = AppColors.D_Bg,
    onBackground        = AppColors.D_Ink,

    surface             = AppColors.D_Surface,
    onSurface           = AppColors.D_Ink,
    surfaceVariant      = AppColors.D_Surface2,
    onSurfaceVariant    = AppColors.D_InkMid,

    // 4 уровня поверхностей — создают глубину без теней
    surfaceContainer        = AppColors.D_Surface2,
    surfaceContainerLow     = AppColors.D_Surface,
    surfaceContainerHigh    = AppColors.D_Surface3,
    surfaceContainerHighest = Color(0xFF383832),

    error               = AppColors.OrangeOnDark,
    onError             = Color(0xFF200800),

    outline             = AppColors.D_Surface3,
    outlineVariant      = AppColors.D_Surface2
)

// ═══════════════════════════════════════════════════════════════
@Composable
fun SpanishAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = AppTypography,
        shapes      = Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small      = RoundedCornerShape(12.dp),
            medium     = RoundedCornerShape(16.dp),
            large      = RoundedCornerShape(20.dp),
            extraLarge = RoundedCornerShape(28.dp)
        ),
        content = content
    )
}
