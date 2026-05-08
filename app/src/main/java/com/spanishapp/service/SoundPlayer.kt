package com.spanishapp.service

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.spanishapp.data.prefs.AppPreferences
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * Генерирует и воспроизводит короткие музыкальные звуки через AudioTrack.
 * Без внешних файлов — чистые синусоидальные тона с огибающей (без щелчков).
 *
 * Уважает настройку `soundEffectsEnabled` в AppPreferences:
 * если выключено — все play*() становятся no-op.
 */
@Singleton
class SoundPlayer @Inject constructor(
    private val appPreferences: AppPreferences
) {

    private val scope      = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val sampleRate = 44100

    @Volatile private var enabled: Boolean = true

    init {
        scope.launch {
            appPreferences.soundEffectsEnabled.collect { enabled = it }
        }
    }

    // ── Публичные звуки ───────────────────────────────────────

    /** C5→E5→G5→C6 — переход на следующий уровень (весёлый восходящий аккорд) */
    fun playLevelUp() = play(
        Note(523f, 90),
        Note(659f, 90),
        Note(784f, 90),
        Note(1047f, 240)
    )

    /** C5→E5→G5→B5→C6 — 3 звезды, идеальный результат */
    fun playPerfect() = play(
        Note(523f, 70),
        Note(659f, 70),
        Note(784f, 70),
        Note(988f, 70),
        Note(1047f, 280)
    )

    /** C5→E5 — уровень пройден (1-2 звезды) */
    fun playLevelDone() = play(
        Note(523f, 100),
        Note(659f, 100),
        Note(784f, 260)
    )

    /** A5→C6 — короткий "дзынь" при верном ответе (тихо, чтобы не мешать TTS) */
    fun playCorrect() = play(
        Note(880f, 60, volume = 0.35f),
        Note(1047f, 110, volume = 0.35f)
    )

    /** E4→D4 — короткое низкое "бу" при ошибке */
    fun playWrong() = play(
        Note(330f, 90, volume = 0.4f),
        Note(247f, 170, volume = 0.4f)
    )

    // ── Внутренняя реализация ─────────────────────────────────

    private data class Note(val freq: Float, val ms: Int, val volume: Float = 0.55f)

    private fun play(vararg notes: Note) {
        if (!enabled) return
        scope.launch {
            for (n in notes) renderAndPlay(n.freq, n.ms, n.volume)
        }
    }

    private suspend fun renderAndPlay(freq: Float, ms: Int, volume: Float) =
        withContext(Dispatchers.IO) {
            val total  = sampleRate * ms / 1000
            val fade   = minOf(sampleRate * 15 / 1000, total / 3)   // 15 мс огибающая

            val buffer = ShortArray(total) { i ->
                val envelope = when {
                    i < fade         -> i.toFloat() / fade
                    i > total - fade -> (total - i).toFloat() / fade
                    else             -> 1f
                }
                val sine = sin(2.0 * PI * i * freq / sampleRate)
                (sine * Short.MAX_VALUE * volume * envelope).toInt().toShort()
            }

            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(maxOf(minBuf, buffer.size * 2))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            delay(ms.toLong())
            runCatching { track.stop(); track.release() }
        }
}
