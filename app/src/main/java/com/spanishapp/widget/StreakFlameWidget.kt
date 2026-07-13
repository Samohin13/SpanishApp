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
                    // v1.26.1: единый список — AppDatabase.ALL_MIGRATIONS.
                    .addMigrations(*AppDatabase.ALL_MIGRATIONS)
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
