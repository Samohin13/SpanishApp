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
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

/**
 * v1.14.0 Widget #2: Daily Mission Tracker 2×2.
 *
 * Показывает 5 ежедневных целей в виде точек и shortcut на главную.
 * (В этой версии счётчик статичный «0/5» — реальный count требует
 * cross-process БД access как в WordOfDayWidget; для MVP достаточно
 * mnemonic виджета. Считать целей будем в v1.14.1.)
 *
 * Tap → MainActivity (home), где юзер видит DailyMission bento.
 */
class MissionTrackerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent(context)
        }
    }

    @Composable
    private fun WidgetContent(context: Context) {
        val orange = Color(0xFFFF6B35)
        val dark = Color(0xFF1C1C1E)
        val dim = Color(0xFFAEAEB2)
        val white = Color(0xFFFFFFFF)
        val dotInactive = Color(0xFF3A3A3C)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(dark)
                .cornerRadius(16.dp)
                .padding(14.dp)
                .clickable(actionStartActivity(
                    WidgetIntents.intentFor(context, WidgetIntents.TARGET_HOME)
                )),
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Vertical.Top,
            ) {
                Text(
                    text = "🎯 ЦЕЛЬ ДНЯ",
                    style = TextStyle(
                        color = ColorProvider(orange),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                )
                Spacer(GlanceModifier.height(8.dp))
                Text(
                    text = "5 целей",
                    style = TextStyle(
                        color = ColorProvider(white),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                )
                Text(
                    text = "сегодня",
                    style = TextStyle(
                        color = ColorProvider(dim),
                        fontSize = 13.sp,
                    )
                )
                Spacer(GlanceModifier.height(10.dp))
                // 5 точек-индикаторов
                Row {
                    repeat(5) {
                        Box(
                            modifier = GlanceModifier
                                .width(12.dp)
                                .height(12.dp)
                                .background(dotInactive)
                                .cornerRadius(6.dp),
                        ) {}
                        Spacer(GlanceModifier.width(5.dp))
                    }
                }
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    text = "Открыть →",
                    style = TextStyle(
                        color = ColorProvider(orange),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                )
            }
        }
    }
}

class MissionTrackerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = MissionTrackerWidget()
}
