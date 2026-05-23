package com.spanishapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.spanishapp.widget.WidgetIntents
import java.util.concurrent.TimeUnit

/**
 * Пуш-уведомление от NPC через 24 часа после провала чекпоинта.
 *
 * v1.22.20: цель — превратить fail из «техническая надпись» в эмоцию
 * («персонаж обиделся, ждёт меня»). Уникальная фишка для языкового
 * приложения — переносит механику «narrative consequence» из RPG.
 *
 * Поведение:
 *   • При Fail чекпоинта → планируется задача на +24 часа
 *   • Если юзер вернулся и пересдал ДО 24 часов → задача отменяется
 *     (через cancel() из ViewModel при загрузке/запуске CP)
 *   • REPLACE policy: повторный fail того же CP перезаписывает таймер
 *   • Тап по пушу → deep link через widgetTarget → CheckpointScreen cpId
 */
class CheckpointReminderWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "checkpoint_reminder"
        const val KEY_CP_ID = "cp_id"

        private const val DELAY_HOURS = 24L

        /** Уникальное имя job для конкретного CP, чтобы можно было отменить точечно. */
        private fun workName(cpId: String) = "cp_reminder_$cpId"

        /** Уникальный notif id для конкретного CP (cp1=2001, cp16=2016). */
        private fun notifId(cpId: String): Int {
            val n = cpId.removePrefix("cp").toIntOrNull() ?: 0
            return 2000 + n
        }

        /**
         * Запланировать «NPC ждёт тебя» через 24 часа.
         * Вызывается из CheckpointViewModel при Fail-исходе.
         */
        fun scheduleIn24h(context: Context, cpId: String) {
            if (CheckpointReminderTexts.forCp(cpId).isEmpty()) return  // no texts → no push
            val data = workDataOf(KEY_CP_ID to cpId)
            val request = OneTimeWorkRequestBuilder<CheckpointReminderWorker>()
                .setInitialDelay(DELAY_HOURS, TimeUnit.HOURS)
                .setInputData(data)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                workName(cpId),
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }

        /**
         * Отменить запланированное напоминание (юзер сам вернулся в CP).
         * Безопасно вызывать даже если ничего не было запланировано.
         */
        fun cancel(context: Context, cpId: String) {
            WorkManager.getInstance(context).cancelUniqueWork(workName(cpId))
        }
    }

    override suspend fun doWork(): Result {
        val cpId = inputData.getString(KEY_CP_ID).orEmpty()
        if (cpId.isBlank()) return Result.success()
        val text = CheckpointReminderTexts.randomFor(cpId) ?: return Result.success()
        showNotification(cpId, text)
        return Result.success()
    }

    private fun showNotification(cpId: String, text: CheckpointReminderTexts.PushText) {
        val ctx = applicationContext
        val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "NPC ждут возврата",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Персонажи курса напоминают о незакрытых чекпоинтах"
            }
            manager.createNotificationChannel(channel)
        }

        // Deep link → MainActivity с EXTRA_NAV_TARGET = "checkpoint/cpN".
        // Переиспользуем widgetTarget pipeline (см. SpanishAppRoot.LaunchedEffect),
        // который уже умеет навигировать по любому route'у NavHost'а.
        val intent = Intent(ctx, com.spanishapp.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(WidgetIntents.EXTRA_NAV_TARGET, "checkpoint/$cpId")
        }
        val pendingIntent = PendingIntent.getActivity(
            ctx,
            notifId(cpId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(text.title)
            .setContentText(text.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text.body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(notifId(cpId), notif)
    }
}
