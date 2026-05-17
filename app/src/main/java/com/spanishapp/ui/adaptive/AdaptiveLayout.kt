package com.spanishapp.ui.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adaptive layout инфраструктура (Phase 0 — v1.12.0 tablet-first redesign).
 *
 * ## Зачем
 * Юзер на Samsung S24 Ultra видел всё нормально, на Samsung Tab S9 — broken
 * (растянутые кнопки, огромные карточки, плохие пропорции). Эта инфраструктура
 * — фундамент для редизайна всех 54 экранов под разные ширины.
 *
 * ## Архитектура
 * - [LocalWindowSizeClass] — глобальный CompositionLocal, рассчитанный в
 *   MainActivity один раз. Все Composable читают через `LocalWindowSizeClass.current`.
 * - [isCompact] / [isMedium] / [isExpanded] — extension'ы для удобной проверки.
 * - [adaptiveContentWidth] — Modifier для cap'а ширины контента на больших экранах
 *   (текст не растягивается на 1280dp — выглядит «пусто»).
 * - [adaptiveColumns] — количество колонок для Grid (1 на телефоне, 4 на планшете).
 *
 * ## Использование
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     val sizeClass = LocalWindowSizeClass.current
 *     when (sizeClass.widthSizeClass) {
 *         Compact -> MyScreenCompact()    // телефон portrait
 *         Medium -> MyScreenMedium()      // телефон landscape / маленький планшет
 *         Expanded -> MyScreenExpanded()  // планшет
 *     }
 * }
 * ```
 *
 * ## Границы (Material Design 3)
 * - Compact: < 600dp width (телефон portrait, S24 Ultra ≈ 412dp = Compact)
 * - Medium: 600-840dp (телефон landscape, маленький планшет, foldable открытый узкий)
 * - Expanded: 840dp+ (планшет landscape, Tab S9 ≈ 1280dp = Expanded)
 */

/**
 * Глобальный WindowSizeClass для всего приложения.
 *
 * Default — Compact (fallback если кто-то использует до того как MainActivity провайдит).
 * Безопасно — все наши screens сейчас сделаны под Compact.
 */
val LocalWindowSizeClass = staticCompositionLocalOf<WindowSizeClass> {
    error("LocalWindowSizeClass not provided. Wrap your Composable tree in CompositionLocalProvider in MainActivity.")
}

// ────────────── Convenience extensions ──────────────

@Composable
fun isCompactWidth(): Boolean =
    LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Compact

@Composable
fun isMediumWidth(): Boolean =
    LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Medium

@Composable
fun isExpandedWidth(): Boolean =
    LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Expanded

/** true если экран больше телефона portrait (Medium или Expanded). */
@Composable
fun isWideScreen(): Boolean =
    LocalWindowSizeClass.current.widthSizeClass != WindowWidthSizeClass.Compact

// ────────────── Adaptive sizing helpers ──────────────

/**
 * Максимальная ширина читаемого контента — основное правило типографики.
 * Текст шире 600dp читается плохо, тратит ширину впустую.
 * - Compact (телефон): без ограничения (контент = вся ширина)
 * - Medium (планшет portrait): 600dp max, центрируется
 * - Expanded (планшет landscape): 720dp max, центрируется
 *
 * Использование:
 * ```kotlin
 * Column(modifier = Modifier.adaptiveContentWidth()) { ... }
 * ```
 */
@Composable
fun Modifier.adaptiveContentWidth(): Modifier {
    val sizeClass = LocalWindowSizeClass.current
    val maxWidth = when (sizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> Dp.Unspecified  // без ограничения
        WindowWidthSizeClass.Medium -> 720.dp
        // v1.12.1: было 720dp → стало 900dp по фидбэку юзера (Tab S9 1280dp
        // — 720 это 56% ширины, 44% по бокам пусто). 900dp = ~70% ширины,
        // выглядит сбалансированно, не «одиноко» в центре.
        WindowWidthSizeClass.Expanded -> 900.dp
        else -> Dp.Unspecified
    }
    return if (maxWidth != Dp.Unspecified) {
        this.widthIn(max = maxWidth)
    } else this
}

/**
 * Wrapper для центровки контента с adaptive max-width.
 * Удобно когда нужно обернуть весь экран:
 * ```kotlin
 * AdaptiveContent {
 *     Column { ... }
 * }
 * ```
 */
@Composable
fun AdaptiveContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(modifier = Modifier.adaptiveContentWidth()) {
            content()
        }
    }
}

/**
 * Количество колонок для LazyVerticalGrid в зависимости от ширины.
 * - Compact: 2 (стандарт для карточек на телефоне)
 * - Medium: 3
 * - Expanded: 4
 *
 * Используй для сеток карточек: stations, lessons, libros, achievements.
 */
@Composable
fun adaptiveColumns(
    compact: Int = 2,
    medium: Int = 3,
    expanded: Int = 4,
): Int = when (LocalWindowSizeClass.current.widthSizeClass) {
    WindowWidthSizeClass.Compact -> compact
    WindowWidthSizeClass.Medium -> medium
    WindowWidthSizeClass.Expanded -> expanded
    else -> compact
}

/**
 * Adaptive horizontal padding для экранов.
 * - Compact: 16dp (текущий стандарт)
 * - Medium: 24dp
 * - Expanded: 32dp
 */
@Composable
fun adaptiveHorizontalPadding(): Dp = when (LocalWindowSizeClass.current.widthSizeClass) {
    WindowWidthSizeClass.Compact -> 16.dp
    WindowWidthSizeClass.Medium -> 24.dp
    WindowWidthSizeClass.Expanded -> 32.dp
    else -> 16.dp
}
