package com.spanishapp.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tappable card surface used across the app for consistent micro-interactions:
 *
 * - Theme-aware background ([MaterialTheme.colorScheme.surface]) so it stays the
 *   same gray as the Games screen tiles in dark mode.
 * - Soft drop-shadow (subtle depth, not pronounced like Apple glass).
 * - Scale animation on press (0.96 → 1.0 spring).
 * - Haptic tick on tap that respects the user's vibration intensity setting.
 * - Material ripple keeps the standard touch feedback for accessibility.
 *
 * Drop-in replacement for `Card`/`Surface { Row { ... } }` patterns when the
 * tile is interactive.
 *
 * @param onClick fires after haptic and visual press feedback.
 * @param shape   corner shape; defaults to 20dp rounded.
 * @param backgroundColor override for the surface; defaults to theme surface.
 * @param shadowElevation soft drop-shadow strength.
 * @param contentPadding caller-controlled internal padding (apply inside content).
 * @param enabled when false, click + ripple + haptic are disabled.
 */
@Composable
fun PressableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    shadowElevation: Dp = 3.dp,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val haptic = rememberCheckedHaptic()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "press_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (enabled) shadowElevation else 0.dp,
                shape = shape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.06f),
                spotColor = Color.Black.copy(alpha = 0.10f)
            )
            .clip(shape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
    ) {
        content()
    }
}
