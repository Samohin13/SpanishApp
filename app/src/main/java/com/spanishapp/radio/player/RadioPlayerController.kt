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

    private val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
                if (playing) _hasError.value = false
            }
            override fun onPlayerError(error: PlaybackException) {
                _hasError.value = true
                _isPlaying.value = false
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
