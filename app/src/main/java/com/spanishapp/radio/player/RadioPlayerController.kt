package com.spanishapp.radio.player

import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.spanishapp.radio.data.Station
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Тонкая обёртка над ExoPlayer для воспроизведения радио-потоков.
 *
 * Singleton (Hilt). Создаёт ОДИН ExoPlayer, к которому привязывается
 * MediaSession внутри RadioPlayerService. Это даёт:
 *  - notification + lock screen controls (через MediaSession)
 *  - выживание процесса в фоне (через startForegroundService)
 *  - тот же player виден UI (через RadioCoordinator.setPlayer)
 *
 * До рефакторинга были два независимых ExoPlayer — один в контроллере
 * (которым реально играл UI), другой пустой в сервисе. Из-за этого
 * системе не показывались media-controls и Android убивал процесс
 * при блокировке экрана.
 */
@OptIn(UnstableApi::class)
class RadioPlayerController(private val context: Context) {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentStation = MutableStateFlow<Station?>(null)
    val currentStation: StateFlow<Station?> = _currentStation.asStateFlow()

    private val _hasError = MutableStateFlow(false)
    val hasError: StateFlow<Boolean> = _hasError.asStateFlow()

    /**
     * Callback для трекинга времени прослушивания.
     * Вызывается когда заканчивается «сессия» (стоп/пауза/смена станции).
     */
    var onSessionEnded: ((startedAt: Long, endedAt: Long, stationId: String) -> Unit)? = null

    private var sessionStartedAt: Long = 0L
    private var sessionStationId: String? = null

    private fun startSession(stationId: String) {
        endSessionIfActive()
        sessionStartedAt = System.currentTimeMillis()
        sessionStationId = stationId
    }

    private fun endSessionIfActive() {
        val started = sessionStartedAt
        val stationId = sessionStationId
        if (started > 0 && stationId != null) {
            val ended = System.currentTimeMillis()
            if (ended - started >= 5_000) {
                onSessionEnded?.invoke(started, ended, stationId)
            }
        }
        sessionStartedAt = 0L
        sessionStationId = null
    }

    /**
     * ExoPlayer — публичный для RadioPlayerService (он привязывает к нему
     * MediaSession). UI с ним напрямую НЕ работает, только через методы
     * контроллера и StateFlow-ы.
     */
    val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
                if (playing) {
                    _hasError.value = false
                    _currentStation.value?.let { startSession(it.id) }
                } else {
                    endSessionIfActive()
                }
            }
            override fun onPlayerError(error: PlaybackException) {
                _hasError.value = true
                _isPlaying.value = false
                endSessionIfActive()
            }
        })
    }

    /**
     * Играть станцию. Стартует foreground service (если не запущен)
     * чтобы Android не убил процесс при блокировке экрана и появились
     * системные media-controls.
     */
    fun play(station: Station) {
        _hasError.value = false
        _currentStation.value = station

        // Метаданные → попадают в notification + lock screen
        val mediaItem = MediaItem.Builder()
            .setUri(station.streamUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(station.name)
                    .setArtist("${station.country.emoji} ${station.country.displayName} · ${station.genre.displayName}")
                    .setStation(station.name)
                    .setIsBrowsable(false)
                    .setIsPlayable(true)
                    .build()
            )
            .build()
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true

        // Поднимаем foreground service для фона + lock screen controls.
        // Идемпотентно: повторные вызовы безопасны, сервис уже создан.
        runCatching {
            ContextCompat.startForegroundService(
                context,
                Intent(context, RadioPlayerService::class.java)
            )
        }
    }

    fun pause() {
        player.pause()
    }

    fun resume() {
        player.play()
    }

    fun togglePlayback() {
        if (player.isPlaying) pause() else resume()
    }

    fun release() {
        player.release()
    }
}
