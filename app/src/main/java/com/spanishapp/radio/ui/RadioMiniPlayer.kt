package com.spanishapp.radio.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spanishapp.radio.player.RadioPlayerController
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Hilt EntryPoint для доступа к singleton RadioPlayerController
 * из обычного Composable (без ViewModel).
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface RadioPlayerEntryPoint {
    fun radioPlayer(): RadioPlayerController
}

/**
 * Маленький постоянный плеер над BottomBar.
 * Показывается ТОЛЬКО когда:
 *   1) Есть активная станция (player.currentStation != null)
 *   2) Юзер НЕ на экране радио (где есть полный плеер)
 */
@Composable
fun RadioMiniPlayer(
    isOnRadioScreen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val player = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            RadioPlayerEntryPoint::class.java,
        ).radioPlayer()
    }
    val station by player.currentStation.collectAsState()
    val isPlaying by player.isPlaying.collectAsState()

    val visible = station != null && !isOnRadioScreen

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = Color(0xFFFF5722).copy(alpha = 0.3f))
                .clickable(onClick = onClick),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Station icon (short code)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFF5722).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        station?.shortCode ?: "—",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFF5722),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${station?.name ?: "—"} · %.1f".format(station?.frequency ?: 0f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) Color(0xFF4CAF50) else Color(0xFF888888))
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            if (isPlaying) "LIVE · ${station?.program ?: ""}" else "Пауза",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
                // Play/Pause
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (player.isPlaying.value) player.pause() else player.resume()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (isPlaying) "⏸" else "▶",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
