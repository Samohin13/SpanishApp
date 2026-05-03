package com.spanishapp.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object BrandIcons {
    val Google: ImageVector = ImageVector.Builder(
        name = "Google",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color(0xFF4285F4))) {
            moveTo(23.49f, 12.27f)
            curveTo(23.49f, 11.48f, 23.42f, 10.73f, 23.3f, 10f)
            lineTo(12f, 10f)
            lineTo(12f, 14.51f)
            lineTo(18.47f, 14.51f)
            curveTo(18.18f, 15.99f, 17.34f, 17.25f, 16.08f, 18.1f)
            lineTo(16.08f, 21.09f)
            lineTo(19.93f, 21.09f)
            curveTo(22.19f, 19.01f, 23.49f, 15.92f, 23.49f, 12.27f)
            close()
        }
        path(fill = SolidColor(Color(0xFF34A853))) {
            moveTo(12f, 24f)
            curveTo(15.24f, 24f, 17.95f, 22.92f, 19.93f, 21.09f)
            lineTo(16.08f, 18.1f)
            curveTo(14.99f, 18.83f, 13.62f, 19.27f, 12f, 19.27f)
            curveTo(8.87f, 19.27f, 6.22f, 17.16f, 5.27f, 14.31f)
            lineTo(1.31f, 14.31f)
            lineTo(1.31f, 17.38f)
            curveTo(3.29f, 21.31f, 7.33f, 24f, 12f, 24f)
            close()
        }
        path(fill = SolidColor(Color(0xFFFBBC05))) {
            moveTo(5.27f, 14.31f)
            curveTo(5.03f, 13.59f, 4.9f, 12.82f, 4.9f, 12f)
            curveTo(4.9f, 11.18f, 5.03f, 10.41f, 5.27f, 9.69f)
            lineTo(5.27f, 6.62f)
            lineTo(1.31f, 6.62f)
            curveTo(0.48f, 8.24f, 0f, 10.06f, 0f, 12f)
            curveTo(0f, 13.94f, 0.48f, 15.76f, 1.31f, 17.38f)
            lineTo(5.27f, 14.31f)
            close()
        }
        path(fill = SolidColor(Color(0xFFEA4335))) {
            moveTo(12f, 4.73f)
            curveTo(13.77f, 4.73f, 15.35f, 5.34f, 16.6f, 6.53f)
            lineTo(20.01f, 3.12f)
            curveTo(17.95f, 1.19f, 15.24f, 0f, 12f, 0f)
            curveTo(7.33f, 0f, 3.29f, 2.69f, 1.31f, 6.62f)
            lineTo(5.27f, 9.69f)
            curveTo(6.22f, 6.84f, 8.87f, 4.73f, 12f, 4.73f)
            close()
        }
    }.build()

    val Apple: ImageVector = ImageVector.Builder(
        name = "Apple",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(18.71f, 19.5f)
            curveTo(17.88f, 20.74f, 17f, 21.95f, 15.66f, 21.97f)
            curveTo(14.32f, 22f, 13.89f, 21.18f, 12.37f, 21.18f)
            curveTo(10.84f, 21.18f, 10.37f, 21.95f, 9.1f, 22f)
            curveTo(7.79f, 22.05f, 6.8f, 20.68f, 5.96f, 19.47f)
            curveTo(4.25f, 17f, 2.94f, 12.45f, 4.7f, 9.39f)
            curveTo(5.57f, 7.87f, 7.13f, 6.91f, 8.82f, 6.88f)
            curveTo(10.1f, 6.86f, 11.32f, 7.75f, 12.11f, 7.75f)
            curveTo(12.89f, 7.75f, 14.37f, 6.68f, 15.92f, 6.84f)
            curveTo(16.57f, 6.87f, 18.39f, 7.1f, 19.56f, 8.82f)
            curveTo(19.47f, 8.88f, 17.39f, 10.1f, 17.41f, 12.63f)
            curveTo(17.44f, 15.65f, 20.06f, 16.66f, 20.09f, 16.67f)
            curveTo(20.06f, 16.74f, 19.67f, 18.11f, 18.71f, 19.5f)
            close()
            moveTo(12f, 6.39f)
            curveTo(11.95f, 4.31f, 13.71f, 2.54f, 15.69f, 2.33f)
            curveTo(15.89f, 4.45f, 13.57f, 6.18f, 12f, 6.39f)
            close()
        }
    }.build()
}
