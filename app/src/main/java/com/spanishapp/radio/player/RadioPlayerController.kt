package com.spanishapp.radio.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
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
 * Singleton-используется через AppModule (Hilt). Подключается к
 * RadioPlayerService для фонового воспроизведения через MediaSession.
 */
@OptIn(UnstableApi::class)
class RadioPlayerController(context: Context) {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentStation = MutableStateFlow<Station?>(null)
    val currentStation: StateFlow<Station?> = _currentStation.asStateFlow()

    private val _hasError = MutableStateFlow(false)
    val hasError: StateFlow<Boolean> = _hasError.asStateFlow()

    /**
     * Callback для трекинга времени прослушивания.
     * Вызывается когда заканчивается «сессия» (стоп/пауза/смена станции).
     * Передаёт стартовое время, конечное время и stationId.
     */
    var onSessionEnded: ((startedAt: Long, endedAt: Long, stationId: String) -> Unit)? = null

    private var sessionStartedAt: Long = 0L
    private var sessionStationId: String? = null

    private fun startSession(stationId: String) {
        // Если уже была активная сессия другой станции — закрываем её
        endSessionIfActive()
        sessionStartedAt = System.currentTimeMillis()
        sessionStationId = stationId
    }

    private fun endSessionIfActive() {
        val started = sessionStartedAt
        val stationId = sessionStationId
        if (started > 0 && stationId != null) {
            val ended = System.currentTimeMillis()
            // Минимум 5 секунд считаем за «прослушал»
            if (ended - started >= 5_000) {
                onSessionEnded?.invoke(started, ended, stationId)
            }
        }
        sessionStartedAt = 0L
        sessionStationId = null
    }

    private val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
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

    fun play(station: Station) {
        _hasError.value = false
        _currentStation.value = station
        player.setMediaItem(MediaItem.fromUri(station.streamUrl))
        player.prepare()
        player.playWhenReady = true
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
