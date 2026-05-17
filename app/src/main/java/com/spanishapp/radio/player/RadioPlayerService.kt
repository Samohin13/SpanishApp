package com.spanishapp.radio.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.spanishapp.MainActivity
import com.spanishapp.R
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Фоновый сервис для воспроизведения радио при заблокированном экране
 * и свёрнутом приложении.
 *
 * Использует ОДИН ExoPlayer из RadioPlayerController (Singleton через Hilt),
 * чтобы у системы и у UI был общий источник правды.
 *
 * ВАЖНО про foreground:
 *  Android 12+ требует вызвать startForeground() в течение 5 секунд после
 *  startForegroundService(). Media3 публикует media-notification только
 *  КОГДА плеер реально заиграет — а радио-поток (HLS/буфер) обычно
 *  прогревается 5-10 сек. Если ждать Media3 — система крашит приложение
 *  через ForegroundServiceDidNotStartInTimeException.
 *
 *  Решение: в onStartCommand сразу постим placeholder «Подключение…»,
 *  Media3 потом сам обновит эту нотификацию media-controls'ами когда
 *  плеер заиграет.
 */
@OptIn(UnstableApi::class)
class RadioPlayerService : MediaSessionService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface PlayerEntryPoint {
        fun controller(): RadioPlayerController
    }

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()
        try {
            val controller = EntryPointAccessors
                .fromApplication(applicationContext, PlayerEntryPoint::class.java)
                .controller()
            // SessionActivity → тап на media-notification открывает приложение
            // (без этого notification «мёртвая», нельзя вернуться в радио).
            val openAppIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pi = PendingIntent.getActivity(
                this, 0, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            mediaSession = MediaSession.Builder(this, controller.player)
                .setSessionActivity(pi)
                .build()
        } catch (e: Exception) {
            runCatching {
                com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                    .recordException(RuntimeException("[RadioService] onCreate MediaSession failed", e))
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Закрываем 5-секундное окно Android 12+ сразу.
        // Media3 потом обновит эту нотификацию своей media-нотификацией
        // когда плеер реально заиграет.
        //
        // ВАЖНО: PendingIntent → тап открывает приложение, иначе нотификация
        // «мёртвая». setContentIntent обязателен иначе на некоторых OEM-прошивках
        // (Xiaomi/Samsung) уведомление вообще не показывается.
        val openIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val contentPi = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ESPEAK Radio")
            .setContentText("Подключение к станции…")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(contentPi)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIF_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
                )
            } else {
                startForeground(NOTIF_ID, notification)
            }
        } catch (e: Exception) {
            // На редких устройствах startForeground может упасть (например
            // если процесс уже считается background). Логируем в Crashlytics
            // и продолжаем — Media3 может позвать ещё раз когда плеер заиграет.
            runCatching {
                com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                    .recordException(RuntimeException("[RadioService] startForeground failed", e))
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    /**
     * При свайпе из recent apps НЕ убиваем сервис если плеер играет.
     * Поведение как у Spotify/YouTube Music.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            // НЕ release() сам player — Singleton, им владеет controller.
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        // Чистим старый IMPORTANCE_LOW канал от v1.9.0-v1.10.0
        runCatching { manager.deleteNotificationChannel("radio_playback") }
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        // IMPORTANCE_DEFAULT (не LOW!) — на Xiaomi/Samsung LOW-каналы
        // часто скрываются из шторки и lock screen полностью.
        // setSound(null, null) + enableVibration(false) — без звука/вибры
        // (это музыкальное уведомление, не алёрт).
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Радио",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Воспроизведение радиостанции"
            setShowBadge(false)
            setSound(null, null)
            enableVibration(false)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val NOTIF_ID = 1001
        // Новый channel ID для v1.10.1 — Android не позволяет менять
        // IMPORTANCE существующего канала. У юзеров с прошлых версий
        // остался "radio_playback" на IMPORTANCE_LOW (часто скрывается
        // на Xiaomi/Samsung). Переход на новый ID = создаётся свежий
        // канал с DEFAULT, виден на lock screen + shade.
        private const val CHANNEL_ID = "radio_playback_v2"
    }
}
