package com.spanishapp.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.spanishapp.R

/**
 * Floating Action Button для ИИ-чата.
 *
 * Использование в HomeScreen.kt:
 *   Box(modifier = Modifier.fillMaxSize()) {
 *       // ... content ...
 *       AiChatFab(
 *           navController = navController,
 *           modifier = Modifier.align(Alignment.BottomEnd).padding(end = 18.dp, bottom = 92.dp)
 *       )
 *   }
 *
 * • Оранжевый градиент primary→primary2
 * • Бесконечная пульсация (scale 1.0 → 1.05)
 * • Иконка ic_bull (бык, 30dp)
 * • Тень с цветом primary, повышенная при пульсации
 * • Navigate на "ai_chat"
 */
@Composable
fun AiChatFab(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val primary = Color(0xFFFF6B35)
    val primary2 = Color(0xFFFF8B5C)

    // Бесконечная пульсация
    val transition = rememberInfiniteTransition(label = "fab_pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "fab_scale",
    )

    // Entrance scale (одноразовый при появлении)
    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        entrance.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
    }
    val combinedScale = scale * entrance.value

    FloatingActionButton(
        onClick = { navController.navigate("ai_chat") },
        containerColor = Color.Transparent,
        modifier = modifier
            .size(60.dp)
            .graphicsLayer {
                scaleX = combinedScale
                scaleY = combinedScale
            }
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                spotColor = primary,
                ambientColor = primary,
            )
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(primary, primary2))),
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_bull),
            contentDescription = "Спросить у Lucía",
            tint = Color.White,
            modifier = Modifier.size(30.dp),
        )
    }
}
