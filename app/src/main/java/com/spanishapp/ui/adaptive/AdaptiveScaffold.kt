package com.spanishapp.ui.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Adaptive scaffold который переключает навигацию по ширине экрана.
 *
 * - **Compact** (телефон portrait): bottomBar внизу — как раньше, юзер
 *   на S24 Ultra ничего не замечает.
 * - **Medium / Expanded** (планшет, foldable): navigationRail слева, контент
 *   справа. Bottom slot уходит наверх (mini-player) — занимает позицию
 *   над rail'ом по высоте.
 *
 * Slot'ы:
 * - [bottomBar] — содержимое для bottom slot в Compact (наш SpanishBottomBar)
 * - [navigationRail] — содержимое для левой колонки в Medium/Expanded (наша
 *   AdaptiveNavigationRail)
 * - [topOverlay] — что показать над навигацией (mini-player). На Compact
 *   рисуется над bottomBar, на Medium/Expanded — над navigationRail в Column.
 * - [content] — основной контент экрана
 *
 * Использование в MainActivity:
 * ```kotlin
 * AdaptiveScaffold(
 *     bottomBar = { SpanishBottomBar(...) },
 *     navigationRail = { SpanishNavigationRail(...) },
 *     topOverlay = { RadioMiniPlayer(...) },
 * ) { paddingValues -> SpanishNavHost(...) }
 * ```
 */
@Composable
fun AdaptiveScaffold(
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    navigationRail: @Composable () -> Unit = {},
    topOverlay: @Composable () -> Unit = {},
    showNavigation: Boolean = true,
    content: @Composable (PaddingValues) -> Unit,
) {
    if (isCompactWidth()) {
        // ─── Compact (телефон portrait) — как было до v1.12.0 ───
        Scaffold(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (showNavigation) {
                    Column {
                        topOverlay()
                        bottomBar()
                    }
                }
            },
            content = content,
        )
    } else {
        // ─── Medium / Expanded — NavigationRail слева ───
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            if (showNavigation) {
                // Колонка слева: mini-player сверху (если есть) + nav rail
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .windowInsetsPadding(WindowInsets.systemBars),
                ) {
                    Box(modifier = Modifier.width(96.dp)) {
                        topOverlay()
                    }
                    navigationRail()
                }
            }
            // Контент занимает остаток ширины + auto-centered с max-width.
            // v1.12.1: оборачиваем в Box с adaptiveContentWidth() чтобы ВСЕ
            // 49 экранов автоматически получили cap без необходимости
            // править каждый файл вручную. Box(fillMaxSize) + inner Box
            // (adaptiveContentWidth) → контент центрируется + по бокам
            // фон Scaffold.
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .adaptiveContentWidth(),
                ) {
                    content(PaddingValues(0.dp))
                }
            }
        }
    }
}
