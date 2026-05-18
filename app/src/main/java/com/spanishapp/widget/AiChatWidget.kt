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
 * v1.14.0 Widget #4: AI Chat Quick-input 4×1.
 *
 * Тап → открыть AI Chat sessions list (юзер выбирает существующую
 * сессию или создаёт новую).
 *
 * (RemoteInput с inline text field — backlog для v1.14.1. Glance
 * 1.1 не поддерживает inline TextField без кастомного RemoteViews.
 * Для MVP — простой shortcut.)
 */
class AiChatWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent(context)
        }
    }

    @Composable
    private fun WidgetContent(context: Context) {
        val orange = Color(0xFFFF6B35)
        val dark = Color(0xFF2A1810)  // тёмно-коричневый, под бренд
        val white = Color(0xFFFFFFFF)
        val dim = Color(0xFFAEAEB2)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(4.dp)
                .clickable(actionStartActivity(
                    WidgetIntents.intentFor(context, WidgetIntents.TARGET_AI_CHAT)
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
                // Оранжевый круг с быком (бренд-маркер AI)
                Box(
                    modifier = GlanceModifier
                        .width(40.dp)
                        .height(40.dp)
                        .background(orange)
                        .cornerRadius(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "♉",
                        style = TextStyle(
                            color = ColorProvider(white),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    )
                }
                Spacer(GlanceModifier.width(12.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "Репетитор",
                        style = TextStyle(
                            color = ColorProvider(white),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    )
                    Text(
                        text = "Спросить на испанском →",
                        style = TextStyle(
                            color = ColorProvider(dim),
                            fontSize = 12.sp,
                        )
                    )
                }
            }
        }
    }
}

class AiChatWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = AiChatWidget()
}
