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
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

/**
 * v1.14.0 Widget #1: Dictionary Search 4×1.
 *
 * Поисковая строка-shortcut на главном экране. Тап → открывает
 * MainActivity?target=dictionary (NavHost навигирует на DictionaryScreen
 * с фокусом в поле поиска).
 *
 * Дизайн — как Google Search bar на лаунчере: capsule pill,
 * иконка лупы слева + placeholder «Найти слово…».
 */
class DictionarySearchWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent(context)
        }
    }

    @Composable
    private fun WidgetContent(context: Context) {
        val orange = Color(0xFFFF6B35)
        val pillBg = Color(0xFF2A2A2D)
        val placeholder = Color(0xFFAEAEB2)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(4.dp)
                .clickable(actionStartActivity(
                    WidgetIntents.intentFor(context, WidgetIntents.TARGET_DICTIONARY)
                )),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(pillBg)
                    .cornerRadius(28.dp)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                // 🔍 Иконка лупы (emoji вместо drawable — Glance ограничен)
                Text(
                    text = "🔍",
                    style = TextStyle(fontSize = 18.sp),
                )
                Spacer(GlanceModifier.width(12.dp))
                Text(
                    text = "Найти слово…",
                    style = TextStyle(
                        color = ColorProvider(placeholder),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                    )
                )
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    text = "ESPEAK",
                    style = TextStyle(
                        color = ColorProvider(orange),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                )
            }
        }
    }
}

class DictionarySearchWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = DictionarySearchWidget()
}
