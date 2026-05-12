package com.spanishapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spanishapp.domain.algorithm.RatingUpdater
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay

@EntryPoint
@InstallIn(SingletonComponent::class)
interface RatingPopupEntryPoint {
    fun ratingUpdater(): RatingUpdater
}

/**
 * Глобальный хост для "+N ⭐" / "−N" попапа.
 * Подписывается на [RatingUpdater.ratingDeltas] и показывает анимированный
 * бейдж по центру экрана, когда рейтинг реально изменился.
 *
 * Положить в корень UI поверх контента (MainActivity / SpanishAppRoot).
 */
@Composable
fun RatingPopupHost(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val updater = remember {
        EntryPointAccessors
            .fromApplication(context.applicationContext, RatingPopupEntryPoint::class.java)
            .ratingUpdater()
    }
    var current by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(updater) {
        updater.ratingDeltas.collect { delta ->
            current = delta
            delay(1500L)
            current = null
        }
    }

    Box(modifier = modifier.fillMaxSize().padding(top = 72.dp, end = 16.dp), contentAlignment = Alignment.TopEnd) {
        AnimatedVisibility(
            visible = current != null,
            enter = scaleIn(
                initialScale = 0.6f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ) + fadeIn(tween(180)),
            exit = scaleOut(targetScale = 1.4f, animationSpec = tween(400)) +
                    fadeOut(tween(400))
        ) {
            current?.let { delta ->
                val (label, color) = when {
                    delta > 0 -> "⭐ +$delta" to Color(0xFFFFC107)
                    else      -> "$delta" to Color(0xFFEF4444)
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f),
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.5.dp, color)
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = color
                    )
                }
            }
        }
    }
}
