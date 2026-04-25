package com.spanishapp.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.room.Room
import com.spanishapp.MainActivity
import com.spanishapp.data.db.AppDatabase
import java.time.LocalDate

class WordOfDayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Читаем слово дня прямо из БД (виджет работает в отдельном процессе)
        val word = getWordOfDay(context)

        provideContent {
            WidgetContent(
                spanish = word.first,
                russian = word.second
            )
        }
    }

    private fun getWordOfDay(context: Context): Pair<String, String> {
        return try {
            val db = Room.databaseBuilder(context, AppDatabase::class.java, "spanish_app.db")
                .allowMainThreadQueries()
                .build()

            // Детерминированный выбор по дню года — тот же алгоритм что в DatabaseSeeder
            val dayOfYear = LocalDate.now().dayOfYear
            val words = db.wordDao().getWordsByLevelSync("A1")
            db.close()

            if (words.isNotEmpty()) {
                val word = words[dayOfYear % words.size]
                word.spanish to word.russian
            } else {
                "hola" to "привет"
            }
        } catch (e: Exception) {
            "hola" to "привет"
        }
    }
}

@Composable
private fun WidgetContent(spanish: String, russian: String) {
    val terracotta = Color(0xFFFF5722)
    val white      = Color(0xFFFFFFFF)
    val lightBg    = Color(0xFFFFF3EF)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(lightBg)
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.Start
        ) {
            // Заголовок
            Text(
                text = "📅 Слово дня",
                style = TextStyle(
                    color = ColorProvider(terracotta),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.height(6.dp))
            // Испанское слово
            Text(
                text = spanish,
                style = TextStyle(
                    color = ColorProvider(Color(0xFF212121)),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.height(4.dp))
            // Перевод
            Text(
                text = russian,
                style = TextStyle(
                    color = ColorProvider(Color(0xFF757575)),
                    fontSize = 14.sp
                )
            )
            Spacer(GlanceModifier.height(8.dp))
            // Кнопка
            Box(
                modifier = GlanceModifier
                    .background(terracotta)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Учить →",
                    style = TextStyle(
                        color = ColorProvider(white),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

class WordOfDayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = WordOfDayWidget()
}
