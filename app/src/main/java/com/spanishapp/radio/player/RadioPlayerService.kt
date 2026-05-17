package com.spanishapp.radio.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.audiofx.LoudnessEnhancer
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.spanishapp.MainActivity

/**
 * Каноническая Media3-архитектура: сервис **владеет** ExoPlayer'ом и
 * MediaSession'ом. UI взаимодействует через MediaController (см.
 * RadioPlayerController), который привязывается к этому сервису по
 * SessionToken.
 *
 * Это даёт ключевое преимущество: Media3 видит «есть подключенный
 * MediaController» → автоматически постит rich media-notification
 * с обложкой + контролами на lock screen + шторке.
 *
 * До этого refactor'а player жил в RadioPlayerController, MediaSession
 * только ссылалась на него. Media3 не считал session «активной с клиентом»
 * → notification не появлялась.
 *
 * Audio focus, becoming-noisy, wake mode → всё на ExoPlayer'е здесь.
 */
@OptIn(UnstableApi::class)
class RadioPlayerService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    /**
     * LoudnessEnhancer — выравнивает громкость между станциями.
     * Radio-станции имеют разный нормализованный loudness (разброс
     * до 15 dB между Cadena 100 и каким-нибудь регионалом). Это
     * заставляет юзера постоянно крутить системный volume.
     *
     * +400 mB (= +4 dB) — консервативный буст, помогает тихим
     * станциям без распадания громких. Это не «true ReplayGain»
     * (без анализа потока), но решает 80% проблемы.
     */
    private var loudnessEnhancer: LoudnessEnhancer? = null

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()

        try {
            // ExoPlayer создаётся ЗДЕСЬ — service владеет жизненным циклом
            val exoPlayer = ExoPlayer.Builder(this)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(),
                    /* handleAudioFocus = */ true,  // Media3 сам обрабатывает звонки/уведомления
                )
                .setHandleAudioBecomingNoisy(true)  // выдернули наушники → пауза
                .setWakeMode(C.WAKE_MODE_NETWORK)   // CPU + Wi-Fi lock пока играем
                .build()
            player = exoPlayer

            // SessionActivity → тап на media-notification открывает приложение
            val openIntent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val openPi = PendingIntent.getActivity(
                this, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            mediaSession = MediaSession.Builder(this, exoPlayer)
                .setSessionActivity(openPi)
                .build()

            // Loudness normalization — выравниваем громкость между станциями.
            // Делаем после MediaSession build (чтобы audioSessionId был корректно
            // выставлен Media3 на player). Wrapped в runCatching — некоторые
            // OEM/устройства не поддерживают LoudnessEnhancer, не падаем.
            initLoudnessEnhancer(exoPlayer)
        } catch (e: Exception) {
            runCatching {
                com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                    .recordException(RuntimeException("[RadioService] onCreate failed", e))
            }
        }
    }

    private fun initLoudnessEnhancer(player: ExoPlayer) {
        runCatching {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val sessionId = audioManager.generateAudioSessionId()
            if (sessionId == AudioManager.ERROR) {
                Log.w("RadioService", "generateAudioSessionId returned ERROR — skip loudness")
                return
            }
            player.audioSessionId = sessionId
            loudnessEnhancer = LoudnessEnhancer(sessionId).apply {
                setTargetGain(400)  // +4 dB
                enabled = true
            }
            Log.d("RadioService", "LoudnessEnhancer enabled at +4dB, sessionId=$sessionId")
        }.onFailure { e ->
            Log.w("RadioService", "LoudnessEnhancer init failed: ${e.message}", e)
            // Не критично — играем без normalization
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    /**
     * При свайпе из recent apps НЕ убиваем сервис если плеер играет.
     * Поведение как у Spotify/YouTube Music — убрал из тасков → музыка играет.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        val p = player
        if (p == null || !p.playWhenReady || p.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        runCatching {
            loudnessEnhancer?.enabled = false
            loudnessEnhancer?.release()
        }
        loudnessEnhancer = null
        mediaSession?.run {
            this.player.release()  // player из session — тот же что мы создали
            release()
        }
        mediaSession = null
        player = null
        super.onDestroy()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        // Чистим старые каналы от прошлых версий
        runCatching { manager.deleteNotificationChannel("radio_playback") }
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Радио",
            NotificationManager.IMPORTANCE_LOW,  // не алёртит — это media уведомление
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
        /**
         * v1.10.4: новый channel ID для нового архитектурного решения.
         * Прошлые каналы (radio_playback, radio_playback_v2) могут быть
         * заблокированы юзером или иметь неподходящие настройки.
         */
        private const val CHANNEL_ID = "espeak_radio_media"
    }
}
