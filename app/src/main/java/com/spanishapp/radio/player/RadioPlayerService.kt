package com.spanishapp.radio.player

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Фоновый сервис для воспроизведения радио при заблокированном экране
 * и свёрнутом приложении.
 *
 * Использует ОДИН ExoPlayer из RadioPlayerController (Singleton через Hilt),
 * чтобы у системы и у UI был общий источник правды. Привязывает к нему
 * MediaSession → Android рисует:
 *  - media-notification (sticky, не свайпается пока играет)
 *  - large controls на lock screen с обложкой/названием
 *  - Bluetooth/наушники/Android Auto media buttons
 *
 * MediaSessionService сам создаёт и держит foreground notification —
 * нам не нужно вручную вызывать startForeground().
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
        // Берём общий player из Hilt-graph
        val controller = EntryPointAccessors
            .fromApplication(applicationContext, PlayerEntryPoint::class.java)
            .controller()
        mediaSession = MediaSession.Builder(this, controller.player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    /**
     * Когда пользователь свайпает приложение из recent apps — НЕ убиваем сервис
     * если плеер всё ещё играет. Это поведение как у Spotify/YouTube Music:
     * убрал из тасков → музыка продолжает.
     */
    override fun onTaskRemoved(rootIntent: android.content.Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            // НЕ release() сам player — он Singleton и им владеет контроллер.
            // Если release() сделать здесь, controller сломается на следующем play().
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
