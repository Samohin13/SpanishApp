package com.spanishapp.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class WordOfDayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent()
        }
    }
}

@Composable
private fun WidgetContent() {
    GlanceTheme {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.Start
        ) {
            Text(text = "Слово дня")
            Text(text = "hablar")
            Text(text = "говорить")
        }
    }
}

class WordOfDayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = WordOfDayWidget()
}