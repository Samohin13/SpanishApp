package com.spanishapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * v1.17.0: AppPalette — единый источник theme-aware цветов.
 *
 * ## Зачем
 * В коде накопилось много hardcoded `Color(0xFF1C1C1E)` для тёмного фона,
 * `Color(0xFFAEAEB2)` для серого текста и т.д. Они работали только в
 * dark theme — в light выглядели чужеродными «тёмными пятнами на белом».
 *
 * AppPalette даёт парный helper: в dark возвращает оригинальный hex
 * (бит-в-бит как было), в light — premium Apple/Material 3 light value.
 *
 * ## Гарантия безопасности
 * Dark ветка содержит ИМЕННО ТЕ ЖЕ hex-литералы, что разбросаны по
 * коду. После миграции `Color(0xFF1C1C1E)` → `AppPalette.surface()`
 * в dark theme юзер видит ровно тот же оттенок. Light получает новый.
 *
 * ## Использование
 * ```kotlin
 * // БЫЛО:
 * Box(modifier = Modifier.background(Color(0xFF1C1C1E)))
 * // СТАЛО:
 * Box(modifier = Modifier.background(AppPalette.surface()))
 * ```
 *
 * ## Palette philosophy
 * Light тема построена по принципам Apple HIG + Material 3:
 *  - чисто-белый background (#FFFFFF, без warm tint)
 *  - 5-уровневая иерархия surfaces (Material 3 elevation tokens)
 *  - iOS-style тонкие тени для elevation
 *  - Apple secondary text #6E6E73 (опт 65% black)
 *  - тонкие separators #C6C6C8
 *  - brand orange сохраняется (#FF6B35) — отлично читается на обеих
 */
object AppPalette {
    // ──────────────────────────────────────────────────────────
    //  SURFACES (фоны)
    // ──────────────────────────────────────────────────────────

    /** Корневой фон экранов (статус-бар, фон под Scaffold). */
    @Composable @ReadOnlyComposable
    fun background(): Color =
        if (isSystemInDarkTheme()) Color(0xFF0F0F11) else Color(0xFFFFFFFF)

    /** Карточки 1-го уровня (item cards, dialogs). */
    @Composable @ReadOnlyComposable
    fun surface(): Color =
        if (isSystemInDarkTheme()) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)

    /** Лёгкий контейнер (subtle surfaces в списках). */
    @Composable @ReadOnlyComposable
    fun surfaceContainerLow(): Color =
        if (isSystemInDarkTheme()) Color(0xFF1C1C1E) else Color(0xFFF8F8FA)

    /** Контейнер уровня списка / chip group. Apple iOS systemGrouped. */
    @Composable @ReadOnlyComposable
    fun surfaceContainer(): Color =
        if (isSystemInDarkTheme()) Color(0xFF2A2A2D) else Color(0xFFF2F2F7)

    /** Приподнятые карточки (Bento tiles, главный contentcard). */
    @Composable @ReadOnlyComposable
    fun surfaceElevated(): Color =
        if (isSystemInDarkTheme()) Color(0xFF2C2C2E) else Color(0xFFEAEAEF)

    /** Самый верхний уровень (top app bar если выделена, input fields). */
    @Composable @ReadOnlyComposable
    fun surfaceHighest(): Color =
        if (isSystemInDarkTheme()) Color(0xFF3A3A3C) else Color(0xFFE5E5EA)

    // ──────────────────────────────────────────────────────────
    //  TEXT / ICONS
    // ──────────────────────────────────────────────────────────

    /** Основной текст на surface. Apple #1C1C1E (не чёрный — мягче). */
    @Composable @ReadOnlyComposable
    fun onSurface(): Color =
        if (isSystemInDarkTheme()) Color(0xFFFFFFFF) else Color(0xFF1C1C1E)

    /** Вторичный текст (subtitle, meta). iOS secondary 65% black. */
    @Composable @ReadOnlyComposable
    fun onSurfaceDim(): Color =
        if (isSystemInDarkTheme()) Color(0xFFAEAEB2) else Color(0xFF6E6E73)

    /** Третичный текст (helper, placeholder). iOS tertiary 30%. */
    @Composable @ReadOnlyComposable
    fun onSurfaceTertiary(): Color =
        if (isSystemInDarkTheme()) Color(0xFF8E8E93) else Color(0xFFC7C7CC)

    // ──────────────────────────────────────────────────────────
    //  OUTLINES / DIVIDERS
    // ──────────────────────────────────────────────────────────

    /** Видимая граница (border вокруг карточки). */
    @Composable @ReadOnlyComposable
    fun outline(): Color =
        if (isSystemInDarkTheme()) Color(0xFF3A3A3C) else Color(0xFFC6C6C8)

    /** Слабый разделитель (HorizontalDivider между списком). */
    @Composable @ReadOnlyComposable
    fun divider(): Color =
        if (isSystemInDarkTheme()) Color(0xFF2A2A2D) else Color(0xFFE5E5EA)

    // ──────────────────────────────────────────────────────────
    //  BRAND
    // ──────────────────────────────────────────────────────────

    /** Brand orange — одинаков в обеих темах. Sunset over Barcelona. */
    val brand: Color = Color(0xFFFF6B35)

    /** Светло-розовый акцент (B2 уровень, premium accents). */
    val brandPink: Color = Color(0xFFEC4899)

    /** Brand orange при низкой непрозрачности — chip backgrounds. */
    @Composable @ReadOnlyComposable
    fun brandTint(): Color =
        if (isSystemInDarkTheme()) Color(0x33FF6B35) else Color(0xFFFFE5DA)

    /** Очень светлый brand tint (sub-chips, hover state). */
    @Composable @ReadOnlyComposable
    fun brandPale(): Color =
        if (isSystemInDarkTheme()) Color(0x1AFF6B35) else Color(0xFFFFF1E6)

    /** Brand orange на dark theme сохраняет насыщенность, на light — чуть глубже. */
    @Composable @ReadOnlyComposable
    fun brandStrong(): Color =
        if (isSystemInDarkTheme()) Color(0xFFFF6B35) else Color(0xFFE5552A)

    // ──────────────────────────────────────────────────────────
    //  SEMANTIC
    // ──────────────────────────────────────────────────────────

    /** Зелёный успех (galочки, found words). */
    @Composable @ReadOnlyComposable
    fun success(): Color =
        if (isSystemInDarkTheme()) Color(0xFF4CAF50) else Color(0xFF34C759)

    /** Красный ошибка. */
    @Composable @ReadOnlyComposable
    fun error(): Color =
        if (isSystemInDarkTheme()) Color(0xFFFF453A) else Color(0xFFFF3B30)

    /** Жёлтый warning. */
    @Composable @ReadOnlyComposable
    fun warning(): Color =
        if (isSystemInDarkTheme()) Color(0xFFFFD60A) else Color(0xFFFFB400)

    // ──────────────────────────────────────────────────────────
    //  SPECIAL — для watermarks, glow effects
    // ──────────────────────────────────────────────────────────

    /** Watermark цвет (silhouettes of cities, thematic overlays). */
    @Composable @ReadOnlyComposable
    fun watermark(): Color =
        if (isSystemInDarkTheme()) Color(0xFFFFFFFF).copy(alpha = 0.10f)
        else Color(0xFF1C1C1E).copy(alpha = 0.06f)

    /** Тень под elevated cards (Material elevation). */
    @Composable @ReadOnlyComposable
    fun shadow(): Color =
        if (isSystemInDarkTheme()) Color(0xFF000000).copy(alpha = 0.5f)
        else Color(0xFF000000).copy(alpha = 0.08f)
}
