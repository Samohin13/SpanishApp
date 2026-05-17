package com.spanishapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * One-shot напоминание «Помнишь слово дня?», которое срабатывает через 1 час
 * после того как пользователь закрыл WoD-флоу. Цель — точечное повторение
 * на пике кривой забывания (через ~1 час теряется ~50% свежей лексики).
 *
 * Простой Worker без Hilt — текст слова и его перевод приходят через
 * input Data, чтобы не таскать в Worker базу.
 *
 * Поведение:
 *   • REPLACE policy — если за день юзер закрыл несколько подходов WoD,
 *     актуальным останется только последний (нет смысла напоминать про
 *     старое слово).
 *   • Тап по уведомлению → открывает приложение (HomeScreen).
 */
class WoDReminderWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "wod_reminder"
        const val NOTIF_ID   = 1101
        const val WORK_NAME  = "wod_reminder_work"

        const val KEY_SPANISH = "spanish"
        const val KEY_RUSSIAN = "russian"

        /**
         * Запланировать «Вспомни через час» для конкретного слова дня.
         * Если уже было запланировано более старое — REPLACE гарантирует,
         * что юзер не получит несколько уведомлений подряд.
         */
        fun scheduleInOneHour(context: Context, spanish: String, russian: String) {
            val data = workDataOf(
                KEY_SPANISH to spanish,
                KEY_RUSSIAN to russian,
            )
            val request = OneTimeWorkRequestBuilder<WoDReminderWorker>()
                .setInitialDelay(1, TimeUnit.HOURS)
                .setInputData(data)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }

        /** Отменить, если юзер выключил «Помнить слово через час». */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result {
        val spanish = inputData.getString(KEY_SPANISH).orEmpty()
        val russian = inputData.getString(KEY_RUSSIAN).orEmpty()
        if (spanish.isBlank()) return Result.success()

        showNotification(spanish, russian)
        return Result.success()
    }

    private fun showNotification(spanish: String, russian: String) {
        val ctx = applicationContext
        val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Слово дня — повторение",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Напоминание о слове дня через 1 час" }
            manager.createNotificationChannel(channel)
        }

        // Открыть приложение по тапу. Используем launch intent вместо Class<MainActivity>
        // чтобы не таскать зависимость на ui-модуль из service-пакета.
        val launchIntent = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                ctx,
                NOTIF_ID,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        val title = "Помнишь слово дня?"
        val text  = if (russian.isNotBlank()) "$spanish — $russian" else spanish

        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (pendingIntent != null) builder.setContentIntent(pendingIntent)

        manager.notify(NOTIF_ID, builder.build())
    }
}
