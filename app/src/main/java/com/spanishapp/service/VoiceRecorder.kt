package com.spanishapp.service

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Запись голосового сообщения через MediaRecorder.
 *
 * Конфигурация: AAC-LC в MP4 контейнере (.m4a), моно 22050 Гц, 64 kbps —
 * качество речи без излишков, ~480 КБ за минуту.
 *
 * Состояния:
 *  - isRecording: пишем ли сейчас
 *  - elapsedMs: длительность текущей записи
 *  - amplitude: текущая громкость 0..1 (нормализованная) для UI-waveform
 */
@Singleton
class VoiceRecorder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: VoiceMessageStorage,
) {
    private val TAG = "VoiceRecorder"

    private var recorder: MediaRecorder? = null
    private var currentPath: String? = null
    private var startTime: Long = 0L
    private var amplitudePoller: Thread? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _elapsedMs = MutableStateFlow(0L)
    val elapsedMs: StateFlow<Long> = _elapsedMs.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    /**
     * Старт записи. Возвращает путь к будущему файлу или null если уже идёт запись
     * либо инициализация упала.
     */
    fun start(): String? {
        if (_isRecording.value) return null
        val path = storage.newFilePath()
        currentPath = path
        try {
            recorder = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(64_000)
                setAudioSamplingRate(22_050)
                setAudioChannels(1)
                setOutputFile(path)
                prepare()
                start()
            }
            startTime = System.currentTimeMillis()
            _isRecording.value = true
            startAmplitudePolling()
            return path
        } catch (e: Exception) {
            Log.e(TAG, "start failed", e)
            cleanup()
            return null
        }
    }

    /**
     * Остановка записи. Возвращает (path, durationMs) если успешно,
     * иначе null. Удалит файл если запись < 500ms (случайный тап).
     */
    fun stop(): Pair<String, Long>? {
        if (!_isRecording.value) return null
        val duration = System.currentTimeMillis() - startTime
        val path = currentPath
        try {
            recorder?.stop()
            recorder?.release()
        } catch (e: Exception) {
            // stop() throws если запись была слишком коротка → файл невалидный
            Log.w(TAG, "stop failed (probably too short)", e)
            storage.delete(path)
            cleanup()
            return null
        }
        cleanup()
        if (duration < 500L || path == null) {
            storage.delete(path)
            return null
        }
        return path to duration
    }

    /** Отмена — удаляет файл. */
    fun cancel() {
        if (!_isRecording.value) return
        val path = currentPath
        try {
            recorder?.stop()
            recorder?.release()
        } catch (_: Exception) { /* swallow */ }
        cleanup()
        storage.delete(path)
    }

    private fun cleanup() {
        amplitudePoller?.interrupt()
        amplitudePoller = null
        recorder = null
        currentPath = null
        _isRecording.value = false
        _elapsedMs.value = 0L
        _amplitude.value = 0f
    }

    private fun startAmplitudePolling() {
        val t = Thread {
            while (!Thread.currentThread().isInterrupted && _isRecording.value) {
                try {
                    val amp = recorder?.maxAmplitude ?: 0
                    // maxAmplitude максимум 32767 → нормализуем в [0..1] с лёгким log-сжатием
                    val normalized = (kotlin.math.ln(amp.toDouble().coerceAtLeast(1.0)) / kotlin.math.ln(32767.0))
                        .coerceIn(0.0, 1.0)
                        .toFloat()
                    _amplitude.value = normalized
                    _elapsedMs.value = System.currentTimeMillis() - startTime
                    Thread.sleep(80)
                } catch (_: InterruptedException) {
                    break
                } catch (_: Exception) {
                    break
                }
            }
        }
        amplitudePoller = t
        t.start()
    }
}
