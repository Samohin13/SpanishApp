package com.spanishapp.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spanishapp.domain.chat.ChatWallpaper
import com.spanishapp.domain.chat.ChatWallpapers

/**
 * v1.18.38: фон чата — рисует градиент + тайлит векторный паттерн.
 *
 * Использование:
 *   ChatWallpaperBackground(wallpaper = ...) {
 *     // содержимое чата
 *   }
 */
@Composable
fun ChatWallpaperBackground(
    wallpaper: ChatWallpaper,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    Box(
        modifier = modifier
            .clipToBounds()  // v1.18.46: чтобы тайлы не вылазили за нижнюю границу
            .background(wallpaper.gradient)
            .drawBehind {
                val tilePx = with(density) { wallpaper.tileSize.dp.toPx() }
                val cols = (size.width / tilePx).toInt() + 2
                val rows = (size.height / tilePx).toInt() + 2
                val scale = with(density) { 1.dp.toPx() }
                for (r in 0..rows) {
                    for (c in 0..cols) {
                        translate(left = c * tilePx, top = r * tilePx) {
                            wallpaper.drawTile(this, scale)
                        }
                    }
                }
            },
        content = content,
    )
}

/**
 * Bottom sheet с превью всех wallpaper'ов. Тап = live preview через
 * onPick, второй раз тап применять не надо — сохранение происходит сразу.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatWallpaperPickerSheet(
    currentId: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                "Фон чата",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Text(
                "Выбери стиль — применится мгновенно",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(ChatWallpapers.ALL, key = { it.id }) { wp ->
                    WallpaperPickerCard(
                        wallpaper = wp,
                        isSelected = wp.id == currentId,
                        onClick = { onPick(wp.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WallpaperPickerCard(
    wallpaper: ChatWallpaper,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val brandOrange = Color(0xFFFF7A1A)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.62f)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .border(
                BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) brandOrange else MaterialTheme.colorScheme.outlineVariant,
                ),
                RoundedCornerShape(14.dp),
            )
    ) {
        ChatWallpaperBackground(
            wallpaper = wallpaper,
            modifier = Modifier.fillMaxSize(),
        ) {}

        // Gradient overlay + label at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .align(Alignment.BottomCenter)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f),
                        ),
                    ),
                ),
        )
        Text(
            text = wallpaper.displayName,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .size(24.dp)
                    .clip(RoundedCornerShape(50))
                    .background(brandOrange)
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
