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
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.room.Room
import com.spanishapp.BuildConfig
import com.spanishapp.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * v1.14.0 Widget #3: Streak Flame 2×2.
 *
 * Огромная цифра дней подряд + 🔥. Мотивация смотреть каждое утро —
 * «не разбить streak». Тап → открыть приложение (home).
 *
 * Читает currentStreak из user_progress (cross-process Room).
 */
class StreakFlameWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val streak = getStreak(context)
        provideContent {
            WidgetContent(context, streak)
        }
    }

    private suspend fun getStreak(context: Context): Int =
        withContext(Dispatchers.IO) {
            try {
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
                        AppDatabase.MIGRATION_21_22,AppDatabase.MIGRATION_22_23,
                        AppDatabase.MIGRATION_24_25,
                        AppDatabase.MIGRATION_23_24,
                        AppDatabase.MIGRATION_25_26,
                        AppDatabase.MIGRATION_26_27,
                    )
                    .apply { if (BuildConfig.DEBUG) fallbackToDestructiveMigration() }
                    .build()
                try {
                    db.userProgressDao().getProgressOnce()?.currentStreak ?: 0
                } finally { db.close() }
            } catch (_: Throwable) { 0 }
        }

    @Composable
    private fun WidgetContent(context: Context, streak: Int) {
        val orange = Color(0xFFFF6B35)
        val red = Color(0xFFFF3B30)
        val dark = Color(0xFF1C1C1E)
        val white = Color(0xFFFFFFFF)
        val dim = Color(0xFFAEAEB2)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(dark)
                .cornerRadius(16.dp)
                .padding(14.dp)
                .clickable(actionStartActivity(
                    WidgetIntents.intentFor(context, WidgetIntents.TARGET_HOME)
                )),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                Text(
                    text = "🔥",
                    style = TextStyle(fontSize = 32.sp),
                )
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    text = streak.toString(),
                    style = TextStyle(
                        color = ColorProvider(if (streak >= 7) orange else white),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                    )
                )
                Text(
                    text = pluralDays(streak),
                    style = TextStyle(
                        color = ColorProvider(dim),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                )
                if (streak == 0) {
                    Spacer(GlanceModifier.height(6.dp))
                    Text(
                        text = "Начни сегодня",
                        style = TextStyle(
                            color = ColorProvider(red),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    )
                }
            }
        }
    }

    /** Русская плюрализация: «1 день / 2 дня / 5 дней подряд». */
    private fun pluralDays(n: Int): String {
        val mod10 = n % 10
        val mod100 = n % 100
        return when {
            mod100 in 11..14 -> "дней подряд"
            mod10 == 1 -> "день подряд"
            mod10 in 2..4 -> "дня подряд"
            else -> "дней подряд"
        }
    }
}

class StreakFlameWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = StreakFlameWidget()
}
