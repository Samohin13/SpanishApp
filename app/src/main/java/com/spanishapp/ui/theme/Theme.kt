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
//  LUMEN — Design System
//  Вдохновлено Linear, Stripe, Notion, Apple.
//  Глубокие чёрные. Чистые поверхности. Один яркий акцент.
//  Без бордюров — только уровни.
// ═══════════════════════════════════════════════════════════════

object AppColors {

    // ────────────────────────────────────────────────────────
    //  LIGHT — нейтрали (Tailwind neutral)
    // ────────────────────────────────────────────────────────
    val L_Bg            = Color(0xFFFAFAFA)   // фон
    val L_Surface       = Color(0xFFFFFFFF)   // карточки
    val L_Surface2      = Color(0xFFF5F5F5)   // вторичные поверхности
    val L_Surface3      = Color(0xFFE5E5E5)   // ещё выше
    val L_Ink           = Color(0xFF0A0A0A)   // текст основной
    val L_InkMid        = Color(0xFF525252)   // вторичный
    val L_InkDim        = Color(0xFF737373)   // подсказки
    val L_InkFaint      = Color(0xFFA3A3A3)   // отключённое
    val L_Divider       = Color(0xFFEDEDED)   // тонкие линии (только когда нужно)

    // ────────────────────────────────────────────────────────
    //  DARK — настоящие OLED-чёрные
    // ────────────────────────────────────────────────────────
    val D_Bg            = Color(0xFF0A0A0A)   // глубокий чёрный
    val D_Surface       = Color(0xFF171717)   // карточки
    val D_Surface2      = Color(0xFF262626)   // вторичные поверхности
    val D_Surface3      = Color(0xFF404040)   // ещё выше
    val D_Ink           = Color(0xFFFAFAFA)   // основной текст
    val D_InkMid        = Color(0xFFA3A3A3)   // вторичный
    val D_InkDim        = Color(0xFF737373)   // подсказки
    val D_InkFaint      = Color(0xFF525252)   // отключённое
    val D_Divider       = Color(0xFF262626)

    // ────────────────────────────────────────────────────────
    //  BRAND — Rose. Один акцент через всё приложение.
    //  Адаптируется к теме: насыщенный на свету, мягче в тьме.
    // ────────────────────────────────────────────────────────
    val Rose            = Color(0xFFE11D48)   // главный — light mode
    val RoseLight       = Color(0xFFFB7185)   // — dark mode
    val RoseDeep        = Color(0xFFBE123C)   // нажатое состояние
    val RoseTintLight   = Color(0xFFFFE4E6)   // подложки в light
    val RoseTintDark    = Color(0xFF4C1322)   // подложки в dark

    // ────────────────────────────────────────────────────────
    //  ACCENTS — для категорий, спокойные, не кричащие
    // ────────────────────────────────────────────────────────
    val Emerald         = Color(0xFF059669)   // успех
    val EmeraldL        = Color(0xFF34D399)   // dark mode
    val EmeraldTintL    = Color(0xFFD1FAE5)
    val EmeraldTintD    = Color(0xFF064E3B)

    val Amber           = Color(0xFFD97706)   // стрик, золото
    val AmberL          = Color(0xFFFBBF24)
    val AmberTintL      = Color(0xFFFEF3C7)
    val AmberTintD      = Color(0xFF78350F)

    val Sky             = Color(0xFF0284C7)   // информация
    val SkyL            = Color(0xFF38BDF8)
    val SkyTintL        = Color(0xFFE0F2FE)
    val SkyTintD        = Color(0xFF0C4A6E)

    val Violet          = Color(0xFF7C3AED)   // премиум, AI
    val VioletL         = Color(0xFFA78BFA)
    val VioletTintL     = Color(0xFFEDE9FE)
    val VioletTintD     = Color(0xFF4C1D95)

    // ────────────────────────────────────────────────────────
    //  Совместимость со старым кодом
    // ────────────────────────────────────────────────────────
    val Terracotta      = Rose
    val TerracottaLight = RoseLight
    val TerracottaDark  = RoseDeep
    val TerracottaSoft  = RoseTintLight
    val TerracottaBg    = RoseTintLight
    val Coral           = Rose
    val CoralLight      = RoseLight
    val CoralDark       = RoseDeep
    val CoralSurface    = RoseTintLight
    val Olive           = Emerald
    val OliveSoft       = EmeraldTintL
    val OliveBg         = EmeraldTintL
    val OliveDark       = Color(0xFF065F46)
    val Ochre           = Amber
    val OchreSoft       = AmberTintL
    val OchreBg         = AmberTintL
    val OchreDark       = Color(0xFF92400E)
    val Indigo          = Sky
    val IndigoSoft      = SkyTintL
    val IndigoBg        = SkyTintL
    val IndigoDark      = Color(0xFF075985)
    val VioletSurface   = VioletTintL
    val VioletDark      = Color(0xFF5B21B6)
    val SkySurface      = SkyTintL
    val Gold            = Amber
    val GoldLight       = AmberL
    val GoldDark        = Color(0xFF92400E)
    val Teal            = Emerald
    val TealLight       = EmeraldL
    val TealDark        = Color(0xFF065F46)
    val Jade            = Emerald
    val JadeLight       = EmeraldL
    val JadeDark        = Color(0xFF065F46)
    val JadeSurface     = EmeraldTintL
    val AmberSurface    = AmberTintL
    val Info            = Sky
    val Success         = Emerald
    val Warning         = Amber
    val Error           = Rose
    val Paper           = L_Bg
    val Surface         = L_Surface
    val SurfaceMuted    = L_Surface2
    val Border          = L_Divider
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
    val XpGradientStart = Amber
    val XpGradientEnd   = Rose
}

// ═══════════════════════════════════════════════════════════════
//  TYPOGRAPHY — Inter-style: чистая, точная, иерархическая
// ═══════════════════════════════════════════════════════════════
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 36.sp,
        lineHeight    = 44.sp,
        letterSpacing = (-0.8).sp
    ),
    displayMedium = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 30.sp,
        lineHeight    = 38.sp,
        letterSpacing = (-0.6).sp
    ),
    headlineLarge = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 24.sp,
        lineHeight    = 32.sp,
        letterSpacing = (-0.3).sp
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
//  LIGHT
// ═══════════════════════════════════════════════════════════════
private val LightColors = lightColorScheme(
    primary             = AppColors.Rose,
    onPrimary           = Color.White,
    primaryContainer    = AppColors.RoseTintLight,
    onPrimaryContainer  = AppColors.RoseDeep,

    secondary           = AppColors.Emerald,
    onSecondary         = Color.White,
    secondaryContainer  = AppColors.EmeraldTintL,
    onSecondaryContainer = Color(0xFF064E3B),

    tertiary            = AppColors.Amber,
    onTertiary          = Color.White,
    tertiaryContainer   = AppColors.AmberTintL,
    onTertiaryContainer = Color(0xFF78350F),

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

    error               = AppColors.Rose,
    onError             = Color.White,

    outline             = AppColors.L_Divider,
    outlineVariant      = AppColors.L_Divider
)

// ═══════════════════════════════════════════════════════════════
//  DARK
// ═══════════════════════════════════════════════════════════════
private val DarkColors = darkColorScheme(
    primary             = AppColors.RoseLight,
    onPrimary           = Color.White,
    primaryContainer    = AppColors.RoseTintDark,
    onPrimaryContainer  = AppColors.RoseLight,

    secondary           = AppColors.EmeraldL,
    onSecondary         = Color(0xFF064E3B),
    secondaryContainer  = AppColors.EmeraldTintD,
    onSecondaryContainer = AppColors.EmeraldL,

    tertiary            = AppColors.AmberL,
    onTertiary          = Color(0xFF78350F),
    tertiaryContainer   = AppColors.AmberTintD,
    onTertiaryContainer = AppColors.AmberL,

    background          = AppColors.D_Bg,
    onBackground        = AppColors.D_Ink,

    surface             = AppColors.D_Surface,
    onSurface           = AppColors.D_Ink,
    surfaceVariant      = AppColors.D_Surface2,
    onSurfaceVariant    = AppColors.D_InkMid,

    surfaceContainer        = AppColors.D_Surface2,
    surfaceContainerLow     = AppColors.D_Surface,
    surfaceContainerHigh    = AppColors.D_Surface3,
    surfaceContainerHighest = AppColors.D_Surface3,

    error               = AppColors.RoseLight,
    onError             = Color(0xFF690005),

    outline             = AppColors.D_Divider,
    outlineVariant      = AppColors.D_Divider
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
