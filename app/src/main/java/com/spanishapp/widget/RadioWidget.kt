package com.spanishapp.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

/**
 * v1.14.0 Widget #5: Radio shortcut 4×1.
 *
 * Открывает Radio screen. Полноценный «Now Playing» с inline play/pause
 * + station artwork требует интеграции с RadioPlayerController (MediaSession)
 * через RemoteViews — это отдельная задача (~6ч). Для MVP — простой
 * shortcut с эмодзи 📻 и кнопкой «Открыть».
 */
class RadioWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent(context)
        }
    }

    @Composable
    private fun WidgetContent(context: Context) {
        val orange = Color(0xFFFF6B35)
        val dark = Color(0xFF1C1C1E)
        val white = Color(0xFFFFFFFF)
        val dim = Color(0xFFAEAEB2)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(4.dp)
                .clickable(actionStartActivity(
                    WidgetIntents.intentFor(context, WidgetIntents.TARGET_RADIO)
                )),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(dark)
                    .cornerRadius(20.dp)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                // Artwork-placeholder с эмодзи радио
                Box(
                    modifier = GlanceModifier
                        .width(40.dp)
                        .height(40.dp)
                        .background(orange)
                        .cornerRadius(10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "📻",
                        style = TextStyle(fontSize = 22.sp),
                    )
                }
                Spacer(GlanceModifier.width(12.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "Radio España",
                        style = TextStyle(
                            color = ColorProvider(white),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    )
                    Text(
                        text = "Слушай и учись →",
                        style = TextStyle(
                            color = ColorProvider(dim),
                            fontSize = 12.sp,
                        )
                    )
                }
                // Триugольник Play
                Box(
                    modifier = GlanceModifier
                        .width(36.dp)
                        .height(36.dp)
                        .background(orange)
                        .cornerRadius(18.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "▶",
                        style = TextStyle(
                            color = ColorProvider(white),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    )
                }
            }
        }
    }
}

class RadioWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = RadioWidget()
}
