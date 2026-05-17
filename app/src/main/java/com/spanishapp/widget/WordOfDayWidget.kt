package com.spanishapp.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.unit.ColorProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.room.Room
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
                    .addMigrations(
                        AppDatabase.MIGRATION_1_2,  AppDatabase.MIGRATION_2_3,
                        AppDatabase.MIGRATION_3_4,  AppDatabase.MIGRATION_4_5,
                        AppDatabase.MIGRATION_5_6,  AppDatabase.MIGRATION_6_7,
                        AppDatabase.MIGRATION_7_8,  AppDatabase.MIGRATION_8_9,
                        AppDatabase.MIGRATION_9_10, AppDatabase.MIGRATION_10_11,
                        AppDatabase.MIGRATION_11_12,AppDatabase.MIGRATION_12_13,
                        AppDatabase.MIGRATION_13_14,AppDatabase.MIGRATION_14_15,
                        AppDatabase.MIGRATION_15_16,AppDatabase.MIGRATION_16_17,
                        AppDatabase.MIGRATION_17_18,AppDatabase.MIGRATION_18_19,
                        AppDatabase.MIGRATION_19_20,AppDatabase.MIGRATION_20_21,
                        AppDatabase.MIGRATION_21_22, AppDatabase.MIGRATION_22_23,
                        AppDatabase.MIGRATION_23_24,
                    )
                    .fallbackToDestructiveMigration()
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
            // Заголовок + стрик-бэйдж справа
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                Text(
                    text = "📅 Слово дня",
                    style = TextStyle(
                        color = ColorProvider(terracotta),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.defaultWeight())
                if (streak > 0) {
                    Text(
                        text = "🔥 $streak",
                        style = TextStyle(
                            color = ColorProvider(terracotta),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
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
