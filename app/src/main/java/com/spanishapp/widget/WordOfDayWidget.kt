package com.spanishapp.widget

import android.content.Context
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.*
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.spanishapp.MainActivity
import com.spanishapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ─────────────────────────────────────────────────────────────
// WORD OF THE DAY  —  home screen widget (2×1 and 4×2)
// ─────────────────────────────────────────────────────────────
class WordOfDayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val word = fetchWordOfDay(context)

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.background)
                        .padding(12.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    verticalAlignment   = Alignment.Vertical.CenterVertically,
                    horizontalAlignment = Alignment.Horizontal.Start
                ) {
                    // Header row
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Vertical.CenterVertically
                    ) {
                        Image(
                            provider    = ImageProvider(R.drawable.ic_flag_es),
                            contentDescription = "Spanish",
                            modifier    = GlanceModifier.size(16.dp)
                        )
                        Spacer(GlanceModifier.width(4.dp))
                        Text(
                            text  = "Слово дня",
                            style = TextStyle(
                                fontSize    = TextUnit(11f, TextUnitType.Sp),
                                color       = ColorProvider(R.color.widget_label)
                            )
                        )
                    }

                    Spacer(GlanceModifier.height(6.dp))

                    // Spanish word (large)
                    Text(
                        text  = word.spanish,
                        style = TextStyle(
                            fontSize   = TextUnit(22f, TextUnitType.Sp),
                            fontWeight = FontWeight.Bold,
                            color      = ColorProvider(R.color.widget_spanish)
                        )
                    )

                    // Russian translation
                    Text(
                        text  = word.russian,
                        style = TextStyle(
                            fontSize = TextUnit(14f, TextUnitType.Sp),
                            color    = ColorProvider(R.color.widget_russian)
                        )
                    )

                    Spacer(GlanceModifier.height(4.dp))

                    // Example sentence (truncated)
                    if (word.example.isNotEmpty()) {
                        Text(
                            text  = "«${word.example}»",
                            style = TextStyle(
                                fontSize   = TextUnit(11f, TextUnitType.Sp),
                                fontStyle  = FontStyle.Italic,
                                color      = ColorProvider(R.color.widget_example)
                            ),
                            maxLines = 2
                        )
                    }

                    Spacer(GlanceModifier.height(8.dp))

                    // Tap to learn button
                    Text(
                        text  = "Tap to practice →",
                        style = TextStyle(
                            fontSize = TextUnit(11f, TextUnitType.Sp),
                            color    = ColorProvider(R.color.widget_cta)
                        )
                    )
                }
            }
        }
    }

    private suspend fun fetchWordOfDay(context: Context): WidgetWord =
        withContext(Dispatchers.IO) {
            // In production: inject Room and fetch real word
            // For now returns placeholder
            WidgetWord("hablar", "говорить", "Hablo español todos los días.")
        }
}

data class WidgetWord(
    val spanish: String,
    val russian: String,
    val example: String
)

// ─────────────────────────────────────────────────────────────
// Widget receiver
// ─────────────────────────────────────────────────────────────
class WordOfDayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = WordOfDayWidget()
}