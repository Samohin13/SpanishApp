package com.spanishapp.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.unit.ColorProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.room.Room
import com.spanishapp.BuildConfig
import com.spanishapp.MainActivity
import com.spanishapp.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class WordOfDayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Читаем слово дня прямо из БД (виджет работает в отдельном процессе)
        val (spanish, russian, streak) = getWordOfDayAndStreak(context)

        provideContent {
            WidgetContent(
                spanish = spanish,
                russian = russian,
                streak  = streak,
            )
        }
    }

    /**
     * Возвращает (spanish, russian, wodStreak). Стрик берём из user_progress
     * — он обновляется только когда юзер прошёл WoD-флоу в приложении.
     * Виджет ничего не пишет в БД, только читает.
     *
     * Раньше использовали allowMainThreadQueries() и runBlocking — это
     * вызывало ANR на медленных устройствах при первой загрузке виджета
     * (БД ~30 МБ, ~6500 слов). Теперь чтение полностью в Dispatchers.IO,
     * vidjet остаётся отзывчивым.
     */
    private suspend fun getWordOfDayAndStreak(context: Context): Triple<String, String, Int> =
        withContext(Dispatchers.IO) {
            try {
                // Все миграции обязательны — иначе на свежеустановленном APK
                // виджет упадёт с IllegalStateException ещё до Application.onCreate.
                val db = Room.databaseBuilder(context, AppDatabase::class.java, "spanish_app.db")
                    // v1.26.1: единый список — AppDatabase.ALL_MIGRATIONS.
                    .addMigrations(*AppDatabase.ALL_MIGRATIONS)
                    .apply { if (BuildConfig.DEBUG) fallbackToDestructiveMigration() }
                    .build()

                try {
                    // Детерминированный выбор по дню года — тот же алгоритм что в DatabaseSeeder
                    val dayOfYear = LocalDate.now().dayOfYear
                    val words = db.wordDao().getWordsByLevelSync("A1")
                    val progress = db.userProgressDao().getProgressOnce()

                    val streak = progress?.wodStreak ?: 0
                    if (words.isNotEmpty()) {
                        val word = words[dayOfYear % words.size]
                        Triple(word.spanish, word.russian, streak)
                    } else {
                        Triple("hola", "привет", streak)
                    }
                } finally {
                    db.close()
                }
            } catch (e: Exception) {
                // На любое падение (миграция, повреждённый файл, нет места)
                // возвращаем дефолт, чтобы виджет хотя бы что-то показал.
                Triple("hola", "привет", 0)
            }
        }
}

@Composable
private fun WidgetContent(spanish: String, russian: String, streak: Int) {
    val orange = Color(0xFFFF6B35)
    val white  = Color(0xFFFFFFFF)
    val dark   = Color(0xFF1C1C1E)
    val dim    = Color(0xFFAEAEB2)

    // v1.14.1: переделан под dark theme как остальные виджеты ESPEAK.
    // Раньше был кремовый (lightBg) и выбивался из set'а.
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(dark)
            .cornerRadius(20.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.Vertical.Top,
            horizontalAlignment = Alignment.Horizontal.Start
        ) {
            // Заголовок + стрик-бэйдж справа
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                Text(
                    text = "📅 СЛОВО ДНЯ",
                    style = TextStyle(
                        color = ColorProvider(orange),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.defaultWeight())
                if (streak > 0) {
                    Text(
                        text = "🔥 $streak",
                        style = TextStyle(
                            color = ColorProvider(orange),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            Spacer(GlanceModifier.height(8.dp))
            // Испанское слово
            Text(
                text = spanish,
                style = TextStyle(
                    color = ColorProvider(white),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.height(4.dp))
            // Перевод
            Text(
                text = russian,
                style = TextStyle(
                    color = ColorProvider(dim),
                    fontSize = 14.sp
                )
            )
            Spacer(GlanceModifier.defaultWeight())
            // Кнопка-пилюля
            Box(
                modifier = GlanceModifier
                    .background(orange)
                    .cornerRadius(12.dp)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Учить →",
                    style = TextStyle(
                        color = ColorProvider(white),
                        fontSize = 12.sp,
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
