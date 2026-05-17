package com.spanishapp.radio.player

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes as Media3AudioAttributes
import androidx.media3.common.C
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
 * Состояние воспроизведения (агрегат над ExoPlayer.PlaybackState + isPlaying).
 *
 * UI должен показывать:
 *  - IDLE       → «—» (ничего не выбрано)
 *  - BUFFERING  → «Загрузка…» (со спиннером)
 *  - PLAYING    → «LIVE» (зелёный пульсирующий dot)
 *  - PAUSED     → «PAUSED»
 *  - ENDED      → «Поток прерван» (редкий случай — обычно радио бесконечно)
 *  - ERROR      → «ERROR» (красная плашка)
 */
enum class RadioPlaybackState { IDLE, BUFFERING, PLAYING, PAUSED, ENDED, ERROR }

/**
 * Тонкая обёртка над ExoPlayer для воспроизведения радио-потоков.
 *
 * Singleton (Hilt). Создаёт ОДИН ExoPlayer, к которому привязывается
 * MediaSession внутри RadioPlayerService.
 *
 * Что обрабатывает:
 *  - Запуск foreground service для lock screen controls + фона
 *  - Audio focus (пауза при звонке/уведомлении, ducking при тихих)
 *  - Becoming-noisy (выдернули наушники → авто-пауза)
 *  - Buffering / playing / paused / ended / error состояния
 *  - ICY metadata из потока («Сейчас играет [track name]»)
 *  - Трекинг listening sessions для статистики
 */
@OptIn(UnstableApi::class)
class RadioPlayerController(private val context: Context) {

    // ────────────────────── Public state ──────────────────────

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackState = MutableStateFlow(RadioPlaybackState.IDLE)
    val playbackState: StateFlow<RadioPlaybackState> = _playbackState.asStateFlow()

    private val _currentStation = MutableStateFlow<Station?>(null)
    val currentStation: StateFlow<Station?> = _currentStation.asStateFlow()

    private val _hasError = MutableStateFlow(false)
    val hasError: StateFlow<Boolean> = _hasError.asStateFlow()

    /**
     * Текущий трек/программа из ICY metadata потока. null если поток
     * метаданные не отдаёт (большинство talk-станций) или ещё не пришло.
     */
    private val _nowPlaying = MutableStateFlow<String?>(null)
    val nowPlaying: StateFlow<String?> = _nowPlaying.asStateFlow()

    /** Callback для статистики прослушивания. */
    var onSessionEnded: ((startedAt: Long, endedAt: Long, stationId: String) -> Unit)? = null

    // ────────────────────── Session tracking ──────────────────────

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

    // ────────────────────── Audio focus ──────────────────────

    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    /**
     * Был ли плеер на паузе из-за временной потери фокуса (звонок,
     * уведомление). Если да — возобновим автоматом когда фокус вернётся.
     */
    private var pausedByFocusLoss = false

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Постоянная потеря (другое приложение взяло аудио надолго).
                // Не возобновляем автоматом — юзер сам решит когда вернуться.
                pausedByFocusLoss = false
                pauseInternal(releaseFocus = false)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Временная потеря (звонок, навигатор, короткое уведомление).
                // Запоминаем чтобы возобновить при GAIN.
                pausedByFocusLoss = player.isPlaying
                pauseInternal(releaseFocus = false)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Тихое уведомление — понижаем громкость вместо паузы.
                // Радио продолжает играть фоном.
                player.volume = 0.25f
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                player.volume = 1.0f
                if (pausedByFocusLoss) {
                    pausedByFocusLoss = false
                    player.play()
                }
            }
        }
    }

    private val focusRequest: AudioFocusRequest? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(focusChangeListener)
            .build()
    } else null

    /** Запросить audio focus. true = можем играть, false = система не дала. */
    private fun requestFocus(): Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && focusRequest != null) {
            audioManager.requestAudioFocus(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                focusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN,
            )
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && focusRequest != null) {
            audioManager.abandonAudioFocusRequest(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(focusChangeListener)
        }
    }

    // ────────────────────── ExoPlayer ──────────────────────

    /**
     * Player для RadioPlayerService (он привязывает к нему MediaSession).
     * UI с ним напрямую НЕ работает, только через методы контроллера.
     *
     * Настроен с:
     *  - setHandleAudioBecomingNoisy(true) → выдернули наушники → авто-пауза
     *  - AudioAttributes USAGE_MEDIA → правильная маршрутизация
     *  - Wake mode NETWORK → не засыпать пока играем (CPU + Wi-Fi lock)
     */
    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            Media3AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            /* handleAudioFocus = */ false, // мы делаем сами через AudioManager
        )
        .setHandleAudioBecomingNoisy(true)
        .setWakeMode(C.WAKE_MODE_NETWORK)
        .build()
        .apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    _isPlaying.value = playing
                    if (playing) {
                        _hasError.value = false
                        _currentStation.value?.let { startSession(it.id) }
                    } else {
                        endSessionIfActive()
                    }
                    updatePlaybackState()
                }

                override fun onPlaybackStateChanged(state: Int) {
                    updatePlaybackState()
                }

                override fun onPlayerError(error: PlaybackException) {
                    _hasError.value = true
                    _isPlaying.value = false
                    _playbackState.value = RadioPlaybackState.ERROR
                    endSessionIfActive()
                }

                /**
                 * ICY metadata из потока — название текущего трека/программы.
                 * Поле title заполняется автоматически Media3 из ICY headers
                 * (Icy-MetaData: 1 → периодические StreamTitle блоки).
                 */
                override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                    val raw = mediaMetadata.title?.toString()
                        ?: mediaMetadata.displayTitle?.toString()
                    _nowPlaying.value = sanitizeNowPlaying(raw)
                }
            })
        }

    private fun updatePlaybackState() {
        _playbackState.value = when {
            _hasError.value -> RadioPlaybackState.ERROR
            player.playbackState == Player.STATE_IDLE -> RadioPlaybackState.IDLE
            player.playbackState == Player.STATE_BUFFERING -> RadioPlaybackState.BUFFERING
            player.playbackState == Player.STATE_ENDED -> RadioPlaybackState.ENDED
            player.isPlaying -> RadioPlaybackState.PLAYING
            else -> RadioPlaybackState.PAUSED
        }
    }

    // ────────────────────── Public commands ──────────────────────

    /**
     * Играть станцию. Запрашивает audio focus, передаёт metadata в плеер,
     * стартует foreground service (для lock screen + фона).
     */
    fun play(station: Station) {
        if (!requestFocus()) {
            // Система не дала фокус (например идёт звонок). Не падаем,
            // просто отмечаем что играть нельзя. Юзер увидит «PAUSED».
            return
        }

        _hasError.value = false
        _currentStation.value = station
        _nowPlaying.value = null // сбрасываем — новый трек придёт из ICY

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

        // Поднимаем foreground service. Идемпотентно.
        runCatching {
            ContextCompat.startForegroundService(
                context,
                Intent(context, RadioPlayerService::class.java),
            )
        }
    }

    fun pause() {
        pausedByFocusLoss = false
        pauseInternal(releaseFocus = true)
    }

    /**
     * Внутренняя пауза. releaseFocus=false когда паузим из-за временной
     * потери фокуса — не отдаём фокус чтобы вернулся когда отыграет звонок.
     */
    private fun pauseInternal(releaseFocus: Boolean) {
        if (player.isPlaying) player.pause()
        if (releaseFocus) abandonFocus()
    }

    fun resume() {
        if (requestFocus()) {
            player.play()
        }
    }

    fun togglePlayback() {
        if (player.isPlaying) pause() else resume()
    }

    fun release() {
        abandonFocus()
        player.release()
    }
}

/**
 * Чистит текст метаданных перед показом юзеру.
 *  - null/blank → null
 *  - ограничиваем длину 120 символов (защита от вредных больших title)
 *  - убираем control characters и RTL-override (защита от спуфинга)
 *  - убираем «typical noise» из ICY: ID, dashes без контекста
 *
 * Visible for testing.
 */
internal fun sanitizeNowPlaying(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    val cleaned = raw
        .replace(
            Regex(
                "[\\p{Cntrl}" +          // ASCII control characters
                "\\u200B-\\u200D" +      // zero-width space / non-joiner / joiner
                "\\u200E\\u200F" +       // LTR / RTL marks
                "\\u202A-\\u202E" +      // bidi overrides (RTL spoofing)
                "\\u2060" +              // word joiner
                "\\uFEFF" +              // byte-order mark / ZWNBSP
                "]"
            ),
            ""
        )
        .trim()
        .take(120)
    if (cleaned.isBlank()) return null
    // Типичный noise от Icecast: «- », «unknown», «no title»
    val lower = cleaned.lowercase()
    if (lower in setOf("unknown", "no title", "-", "—", "n/a")) return null
    return cleaned
}
