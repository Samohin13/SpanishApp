package com.spanishapp.ui.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Adaptive scaffold — **Duolingo-style** (v1.13.0).
 *
 * До v1.12.x мы переключали навигацию по ширине: BottomBar на телефоне,
 * NavigationRail на планшете. Юзеры пожаловались — rail сливается с
 * topbar, оранжевый brand mark «E» не нужен, UX непоследовательный.
 *
 * Теперь — **BottomBar ВЕЗДЕ**, как у Duolingo / Spotify / Netflix.
 * Контент центрируется через [adaptiveContentWidth] (max 900dp на
 * Expanded). Single UX = consistent UX.
 *
 * Slot'ы:
 * - [bottomBar] — SpanishBottomBar (один для всех width).
 * - [topOverlay] — mini-player над bottomBar (везде одинаково).
 * - [content] — основной контент, автоматически центрируется с cap.
 *
 * Параметр [navigationRail] оставлен для обратной совместимости с
 * MainActivity, но НЕ используется. Будет удалён в v1.14.
 */
@Composable
fun AdaptiveScaffold(
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    @Suppress("UNUSED_PARAMETER")
    navigationRail: @Composable () -> Unit = {},
    topOverlay: @Composable () -> Unit = {},
    showNavigation: Boolean = true,
    content: @Composable (PaddingValues) -> Unit,
) {
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
    ) { paddingValues ->
        // Контент центрируется с adaptiveContentWidth (cap 900dp на
        // Expanded). На Compact width = Dp.Unspecified → full-width.
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .adaptiveContentWidth(),
            ) {
                content(paddingValues)
            }
        }
    }
}
