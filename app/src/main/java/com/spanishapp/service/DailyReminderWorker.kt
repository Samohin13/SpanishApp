package com.spanishapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Ежедневное напоминание заниматься испанским.
 * Время задаётся в Settings (по умолчанию 19:00).
 * Простой Worker без Hilt (нет DI-зависимостей — нет KSP-проблем).
 */
class DailyReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "daily_reminder"
        const val NOTIF_ID   = 1001
        const val WORK_NAME  = "daily_reminder_work"

        /**
         * Запланировать ежедневное напоминание на указанное время.
         * Если уже было запланировано — заменяет (REPLACE policy),
         * чтобы изменения времени в Settings вступили в силу сразу.
         */
        fun schedule(context: Context, hour: Int = 19, minute: Int = 0) {
            val now    = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
                set(Calendar.MINUTE, minute.coerceIn(0, 59))
                set(Calendar.SECOND, 0)
                if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
            }
            val delayMs = target.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                request
            )
        }

        /** Отменить напоминания (юзер выключил toggle). */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        /**
         * Мгновенный тестовый запуск — для кнопки «Проверить напоминание»
         * в Settings. One-time work без задержки → push должен прилететь
         * в течение нескольких секунд. Если не пришёл — значит проблема
         * с разрешениями / Do-Not-Disturb / каналом уведомлений.
         */
        fun fireOnce(context: Context) {
            val request = OneTimeWorkRequestBuilder<DailyReminderWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }

    override suspend fun doWork(): Result {
        showNotification()
        return Result.success()
    }

    private fun showNotification() {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ежедневные напоминания",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Напоминание заниматься испанским" }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("¡Hola! 👋 Время испанского")
            .setContentText("Несколько минут практики — и ты лучше, чем вчера!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIF_ID, notification)
    }
}
