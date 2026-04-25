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
//  ESPAÑA VIVA — Design System
//  Тёплый, живой, притягивающий. Один язык для детей и взрослых.
// ═══════════════════════════════════════════════════════════════

object AppColors {

    // ── Coral — главный бренд-цвет ────────────────────────────
    val Coral           = Color(0xFFFF5340)   // яркий, как фламенко
    val CoralLight      = Color(0xFFFF8575)
    val CoralDark       = Color(0xFFCC2E1D)
    val CoralSurface    = Color(0xFFFFF1EF)   // очень светлый фон с коралловым оттенком

    // ── Amber — солнце, стрик, достижения ─────────────────────
    val Amber           = Color(0xFFFFB800)
    val AmberLight      = Color(0xFFFFD166)
    val AmberDark       = Color(0xFFCC8F00)
    val AmberSurface    = Color(0xFFFFF9E6)

    // ── Jade — успех, прогресс, правильно ─────────────────────
    val Jade            = Color(0xFF00C896)
    val JadeLight       = Color(0xFF70E4C4)
    val JadeDark        = Color(0xFF009970)
    val JadeSurface     = Color(0xFFE8FAF5)

    // ── Violet — AI, премиум, особые фичи ─────────────────────
    val Violet          = Color(0xFF7C4DFF)
    val VioletLight     = Color(0xFFAB8AFF)
    val VioletDark      = Color(0xFF5A2ECC)
    val VioletSurface   = Color(0xFFF2EEFF)

    // ── Sky — информация, ссылки ───────────────────────────────
    val Sky             = Color(0xFF2D8EFF)
    val SkySurface      = Color(0xFFEEF5FF)

    // ── Нейтралы — тёплые, не холодные ───────────────────────
    val Ink             = Color(0xFF1A0F0A)   // почти чёрный с теплотой
    val InkMid          = Color(0xFF5C3D2E)   // тёплый коричнево-серый
    val InkLight        = Color(0xFFA07B6A)   // средний тёплый серый
    val InkFaint        = Color(0xFFD9C5BC)   // светлый тёплый серый

    // ── Фоны — кремово-тёплые ─────────────────────────────────
    val Cream           = Color(0xFFFFFAF7)   // основной фон (не холодный белый)
    val CreamMid        = Color(0xFFF5ECE6)   // поверхности, карточки
    val CreamDeep       = Color(0xFFEEE2DA)   // бордюры, разделители

    // ── Тёмный режим ──────────────────────────────────────────
    val DarkBg          = Color(0xFF120C0A)   // очень тёмный тёплый чёрный
    val DarkSurf        = Color(0xFF1E1512)   // тёмная поверхность
    val DarkSurf2       = Color(0xFF2A1F1B)   // чуть светлее
    val DarkSurf3       = Color(0xFF382A25)   // карточки
    val DarkText        = Color(0xFFF7F0EC)   // тёплый белый для текста

    // ── Семантика ─────────────────────────────────────────────
    val Success         = Jade
    val Warning         = Amber
    val Error           = Coral
    val Info            = Sky

    // ── XP градиент ───────────────────────────────────────────
    val XpGradientStart = Amber
    val XpGradientEnd   = Coral

    // ── Совместимость (старые имена, чтобы не сломать другие экраны) ──
    val Terracotta      = Coral
    val TerracottaLight = CoralLight
    val TerracottaDark  = CoralDark
    val Gold            = Amber
    val GoldLight       = AmberLight
    val GoldDark        = AmberDark
    val Teal            = Jade
    val TealLight       = JadeLight
    val TealDark        = JadeDark
    val InkMedium       = InkMid
    val Surface         = Color(0xFFFFFFFF)
    val CreamDark       = CreamMid
    val DarkBackground  = DarkBg
    val DarkSurface     = DarkSurf
    val DarkSurface2    = DarkSurf2
    val DarkInk         = DarkText
}

// ═══════════════════════════════════════════════════════════════
//  TYPOGRAPHY  —  Крупная, жирная, дружелюбная
// ═══════════════════════════════════════════════════════════════
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontWeight    = FontWeight.Black,
        fontSize      = 42.sp,
        lineHeight    = 50.sp,
        letterSpacing = (-1).sp
    ),
    displayMedium = TextStyle(
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 34.sp,
        lineHeight    = 42.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 28.sp,
        lineHeight    = 36.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineMedium = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 24.sp,
        lineHeight    = 32.sp
    ),
    headlineSmall = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 20.sp,
        lineHeight    = 28.sp
    ),
    titleLarge = TextStyle(
        fontWeight    = FontWeight.ExtraBold,
        fontSize      = 18.sp,
        lineHeight    = 26.sp
    ),
    titleMedium = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 16.sp,
        lineHeight    = 22.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 14.sp,
        lineHeight    = 20.sp
    ),
    bodyLarge = TextStyle(
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        lineHeight    = 25.sp
    ),
    bodyMedium = TextStyle(
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 22.sp
    ),
    bodySmall = TextStyle(
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 18.sp
    ),
    labelLarge = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight    = FontWeight.Medium,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 15.sp,
        letterSpacing = 0.4.sp
    )
)

// ═══════════════════════════════════════════════════════════════
//  LIGHT COLOR SCHEME
// ═══════════════════════════════════════════════════════════════
private val LightColors = lightColorScheme(
    primary             = AppColors.Coral,
    onPrimary           = Color.White,
    primaryContainer    = AppColors.CoralSurface,
    onPrimaryContainer  = AppColors.CoralDark,

    secondary           = AppColors.Jade,
    onSecondary         = Color.White,
    secondaryContainer  = AppColors.JadeSurface,
    onSecondaryContainer = AppColors.JadeDark,

    tertiary            = AppColors.Amber,
    onTertiary          = AppColors.Ink,
    tertiaryContainer   = AppColors.AmberSurface,
    onTertiaryContainer = AppColors.AmberDark,

    background          = AppColors.Cream,
    onBackground        = AppColors.Ink,

    surface             = Color.White,
    onSurface           = AppColors.Ink,
    surfaceVariant      = AppColors.CreamMid,
    onSurfaceVariant    = AppColors.InkMid,

    surfaceContainer        = AppColors.CreamMid,
    surfaceContainerHigh    = AppColors.CreamDeep,
    surfaceContainerHighest = AppColors.CreamDeep,

    error               = AppColors.Coral,
    onError             = Color.White,

    outline             = AppColors.InkFaint,
    outlineVariant      = AppColors.CreamDeep
)

// ═══════════════════════════════════════════════════════════════
//  DARK COLOR SCHEME
// ═══════════════════════════════════════════════════════════════
private val DarkColors = darkColorScheme(
    primary             = AppColors.CoralLight,
    onPrimary           = AppColors.CoralDark,
    primaryContainer    = AppColors.CoralDark,
    onPrimaryContainer  = AppColors.CoralLight,

    secondary           = AppColors.JadeLight,
    onSecondary         = AppColors.JadeDark,
    secondaryContainer  = AppColors.JadeDark,
    onSecondaryContainer = AppColors.JadeLight,

    tertiary            = AppColors.AmberLight,
    onTertiary          = AppColors.AmberDark,
    tertiaryContainer   = AppColors.AmberDark,
    onTertiaryContainer = AppColors.AmberLight,

    background          = AppColors.DarkBg,
    onBackground        = AppColors.DarkText,

    surface             = AppColors.DarkSurf,
    onSurface           = AppColors.DarkText,
    surfaceVariant      = AppColors.DarkSurf2,
    onSurfaceVariant    = AppColors.DarkText.copy(alpha = 0.7f),

    surfaceContainer        = AppColors.DarkSurf2,
    surfaceContainerHigh    = AppColors.DarkSurf3,
    surfaceContainerHighest = AppColors.DarkSurf3,

    error               = Color(0xFFFF8A80),
    onError             = Color(0xFF690005),

    outline             = Color.White.copy(alpha = 0.15f),
    outlineVariant      = Color.White.copy(alpha = 0.08f)
)

// ═══════════════════════════════════════════════════════════════
//  THEME
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
            medium     = RoundedCornerShape(18.dp),
            large      = RoundedCornerShape(24.dp),
            extraLarge = RoundedCornerShape(32.dp)
        ),
        content = content
    )
}
