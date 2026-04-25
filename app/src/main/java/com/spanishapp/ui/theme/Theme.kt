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
//  SOBREMESA — Design System
//  Сдержанно. Тепло. Со вкусом. В меру объёмно.
// ═══════════════════════════════════════════════════════════════

object AppColors {

    // ── Нейтрали — основа всего ────────────────────────────────
    // Фоны — тёплая бумага, не холодный белый
    val Paper           = Color(0xFFFAF7F2)   // основной фон, цвет старой бумаги
    val Surface         = Color(0xFFFFFFFF)   // карточки
    val SurfaceMuted    = Color(0xFFF2EEE7)   // вторичные поверхности
    val Border          = Color(0xFFE8E2D8)   // тонкие разделители

    // Текст — тёплые тёмные, без чёрного
    val Ink             = Color(0xFF1C1917)   // основной текст
    val InkMid          = Color(0xFF57534E)   // вторичный
    val InkLight        = Color(0xFF8C8278)   // подсказки
    val InkFaint        = Color(0xFFC8BFB3)   // отключённое

    // ── Акценты — приглушённые, благородные ───────────────────
    // Терракота — главный акцент. Мягкий, не кричащий.
    val Terracotta      = Color(0xFFB8442D)
    val TerracottaSoft  = Color(0xFFE8C4B8)
    val TerracottaBg    = Color(0xFFF7EDE7)
    val TerracottaDark  = Color(0xFF8E2F1E)

    // Олива — успех, прогресс. Средиземноморский зелёный.
    val Olive           = Color(0xFF6B7D5A)
    val OliveSoft       = Color(0xFFC8D2BD)
    val OliveBg         = Color(0xFFF1F3EC)
    val OliveDark       = Color(0xFF4A5840)

    // Охра — стрик, золото, достижения. Дижон.
    val Ochre           = Color(0xFFB8851F)
    val OchreSoft       = Color(0xFFE8D396)
    val OchreBg         = Color(0xFFF7F0DC)
    val OchreDark       = Color(0xFF8C6418)

    // Индиго — информация, премиум. Тёмно-синий.
    val Indigo          = Color(0xFF3D4A75)
    val IndigoSoft      = Color(0xFFC4CBDC)
    val IndigoBg        = Color(0xFFEDF0F5)
    val IndigoDark      = Color(0xFF2A3354)

    // ── Тёмная тема ───────────────────────────────────────────
    val DarkBg          = Color(0xFF14110E)   // тёплый чёрный
    val DarkSurf        = Color(0xFF1F1B17)
    val DarkSurfMuted   = Color(0xFF2A2520)
    val DarkBorder      = Color(0xFF3D362F)
    val DarkInk         = Color(0xFFF5F0E8)
    val DarkInkMid      = Color(0xFFB8B0A4)
    val DarkInkLight    = Color(0xFF8C8278)

    // ── Семантика ─────────────────────────────────────────────
    val Success         = Olive
    val Warning         = Ochre
    val Error           = Terracotta
    val Info            = Indigo

    // ── Совместимость со старым кодом ─────────────────────────
    val TerracottaLight = TerracottaSoft
    val Gold            = Ochre
    val GoldLight       = OchreSoft
    val GoldDark        = OchreDark
    val Teal            = Olive
    val TealLight       = OliveSoft
    val TealDark        = OliveDark
    val Coral           = Terracotta
    val CoralLight      = TerracottaSoft
    val CoralDark       = TerracottaDark
    val CoralSurface    = TerracottaBg
    val Amber           = Ochre
    val AmberLight      = OchreSoft
    val AmberDark       = OchreDark
    val AmberSurface    = OchreBg
    val Jade            = Olive
    val JadeLight       = OliveSoft
    val JadeDark        = OliveDark
    val JadeSurface     = OliveBg
    val Violet          = Indigo
    val VioletLight     = IndigoSoft
    val VioletDark      = IndigoDark
    val VioletSurface   = IndigoBg
    val Sky             = Indigo
    val SkySurface      = IndigoBg
    val InkMedium       = InkMid
    val Cream           = Paper
    val CreamMid        = SurfaceMuted
    val CreamDark       = SurfaceMuted
    val CreamDeep       = Border
    val DarkBackground  = DarkBg
    val DarkSurface     = DarkSurf
    val DarkSurface2    = DarkSurfMuted
    val DarkSurf2       = DarkSurfMuted
    val DarkSurf3       = DarkBorder
    val DarkText        = DarkInk
    val XpGradientStart = Ochre
    val XpGradientEnd   = Terracotta
}

// ═══════════════════════════════════════════════════════════════
//  TYPOGRAPHY  —  Сдержанная, читаемая, без перегиба
// ═══════════════════════════════════════════════════════════════
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 32.sp,
        lineHeight    = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontWeight    = FontWeight.Bold,
        fontSize      = 28.sp,
        lineHeight    = 36.sp,
        letterSpacing = (-0.4).sp
    ),
    headlineLarge = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 24.sp,
        lineHeight    = 32.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineMedium = TextStyle(
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 20.sp,
        lineHeight    = 28.sp
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
    primary             = AppColors.Terracotta,
    onPrimary           = Color.White,
    primaryContainer    = AppColors.TerracottaBg,
    onPrimaryContainer  = AppColors.TerracottaDark,

    secondary           = AppColors.Olive,
    onSecondary         = Color.White,
    secondaryContainer  = AppColors.OliveBg,
    onSecondaryContainer = AppColors.OliveDark,

    tertiary            = AppColors.Ochre,
    onTertiary          = Color.White,
    tertiaryContainer   = AppColors.OchreBg,
    onTertiaryContainer = AppColors.OchreDark,

    background          = AppColors.Paper,
    onBackground        = AppColors.Ink,

    surface             = AppColors.Surface,
    onSurface           = AppColors.Ink,
    surfaceVariant      = AppColors.SurfaceMuted,
    onSurfaceVariant    = AppColors.InkMid,

    surfaceContainer        = AppColors.SurfaceMuted,
    surfaceContainerHigh    = AppColors.Border,
    surfaceContainerHighest = AppColors.Border,

    error               = AppColors.Terracotta,
    onError             = Color.White,

    outline             = AppColors.Border,
    outlineVariant      = AppColors.SurfaceMuted
)

// ═══════════════════════════════════════════════════════════════
//  DARK
// ═══════════════════════════════════════════════════════════════
private val DarkColors = darkColorScheme(
    primary             = Color(0xFFE07A5F),
    onPrimary           = AppColors.TerracottaDark,
    primaryContainer    = AppColors.TerracottaDark,
    onPrimaryContainer  = AppColors.TerracottaSoft,

    secondary           = AppColors.OliveSoft,
    onSecondary         = AppColors.OliveDark,
    secondaryContainer  = AppColors.OliveDark,
    onSecondaryContainer = AppColors.OliveSoft,

    tertiary            = AppColors.OchreSoft,
    onTertiary          = AppColors.OchreDark,
    tertiaryContainer   = AppColors.OchreDark,
    onTertiaryContainer = AppColors.OchreSoft,

    background          = AppColors.DarkBg,
    onBackground        = AppColors.DarkInk,

    surface             = AppColors.DarkSurf,
    onSurface           = AppColors.DarkInk,
    surfaceVariant      = AppColors.DarkSurfMuted,
    onSurfaceVariant    = AppColors.DarkInkMid,

    surfaceContainer        = AppColors.DarkSurfMuted,
    surfaceContainerHigh    = AppColors.DarkBorder,
    surfaceContainerHighest = AppColors.DarkBorder,

    error               = Color(0xFFE07A5F),
    onError             = Color(0xFF690005),

    outline             = AppColors.DarkBorder,
    outlineVariant      = AppColors.DarkSurfMuted
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
