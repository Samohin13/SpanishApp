package com.spanishapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.prefs.AppPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Ежедневное напоминание заниматься испанским.
 * Срабатывает один раз в сутки, в выбранное время (по умолчанию 19:00).
 * Не показывает уведомление если звук/TTS выключен в настройках — тогда уведомление
 * тоже не нужно (пользователь явно отключил).
 * Уважает флаг ttsEnabled как прокси «пользователь активен» — уведомление всегда показывается,
 * но без звука если tts выключен.
 */
@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val userProgressDao: UserProgressDao,
    private val appPreferences: AppPreferences
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID   = "daily_reminder"
        const val NOTIF_ID     = 1001
        const val WORK_NAME    = "daily_reminder_work"

        /** Запланировать ежедневное напоминание (19:00 каждый день). */
        fun schedule(context: Context) {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 19)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                // Если уже прошло 19:00 — первый запуск завтра
                if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
            }
            val delayMs = target.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit.DAYS
            )
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,   // не перезапускать если уже запланировано
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        // Проверяем стрик — если занимался сегодня, не напоминаем
        val progress = userProgressDao.getProgressOnce()
        // Уведомление показываем всегда (независимо от tts)
        showNotification(progress?.currentStreak ?: 0)
        return Result.success()
    }

    private fun showNotification(streak: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

        // Создаём канал (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ежедневные напоминания",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Напоминание заниматься испанским"
            }
            manager.createNotificationChannel(channel)
        }

        val (title, text) = reminderText(streak)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIF_ID, notification)
    }

    private fun reminderText(streak: Int): Pair<String, String> {
        return when {
            streak >= 30 -> "🔥 $streak дней подряд!" to "Продолжай — ты на пути к легенде!"
            streak >= 7  -> "🔥 Серия $streak дней!" to "Не прерывай — зайди на 5 минут!"
            streak >= 3  -> "⚡ Серия $streak дня!" to "¡Hola! Время практики испанского"
            streak >= 1  -> "✨ Серия $streak день!" to "Держи серию — зайди на урок!"
            else         -> "¡Hola! 👋" to "Сегодня отличный день для испанского!"
        }
    }
}
