package com.spanishapp.service

import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Воспроизведение голосовых сообщений (один в один момент).
 *
 * Состояние:
 *  - currentPath: какой файл сейчас играется (null если ничего)
 *  - positionMs: текущая позиция в файле
 *  - durationMs: общая длительность
 *  - isPlaying: бьётся ли сейчас звук
 *
 * Single-player: новая команда play() останавливает предыдущую.
 */
@Singleton
class VoicePlayer @Inject constructor() {
    private val TAG = "VoicePlayer"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var pollJob: Job? = null
    private var player: MediaPlayer? = null

    private val _currentPath = MutableStateFlow<String?>(null)
    val currentPath: StateFlow<String?> = _currentPath.asStateFlow()

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    /** Запустить воспроизведение. Если уже играет — стопаем тот файл. */
    fun play(path: String) {
        stop()
        try {
            player = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                setOnCompletionListener {
                    _isPlaying.value = false
                    _positionMs.value = _durationMs.value
                    pollJob?.cancel()
                }
                start()
            }
            _currentPath.value = path
            _durationMs.value = (player?.duration ?: 0).toLong()
            _isPlaying.value = true
            startPolling()
        } catch (e: Exception) {
            Log.e(TAG, "play failed: $path", e)
            stop()
        }
    }

    /** Полный стоп + release. */
    fun stop() {
        try {
            player?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (_: Exception) {}
        player = null
        pollJob?.cancel()
        pollJob = null
        _isPlaying.value = false
        _currentPath.value = null
        _positionMs.value = 0L
        _durationMs.value = 0L
    }

    fun pause() {
        try {
            player?.takeIf { it.isPlaying }?.pause()
            _isPlaying.value = false
            pollJob?.cancel()
        } catch (_: Exception) {}
    }

    fun resume() {
        try {
            player?.let {
                if (!it.isPlaying) {
                    it.start()
                    _isPlaying.value = true
                    startPolling()
                }
            }
        } catch (_: Exception) {}
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = scope.launch {
            while (_isPlaying.value) {
                _positionMs.value = (player?.currentPosition ?: 0).toLong()
                delay(80)
            }
        }
    }
}
