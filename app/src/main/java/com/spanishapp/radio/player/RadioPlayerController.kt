package com.spanishapp.radio.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Metadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.spanishapp.radio.data.Station
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.ArrayDeque
import java.util.concurrent.Executor

private const val TAG_ICY = "RadioICY"

/**
 * Состояние воспроизведения (агрегат над Player state + isPlaying).
 */
enum class RadioPlaybackState { IDLE, BUFFERING, PLAYING, PAUSED, ENDED, ERROR }

/**
 * Facade для UI над радио-плеером. Внутри держит MediaController,
 * который привязывается к RadioPlayerService через SessionToken.
 *
 * Архитектура v1.10.4 (канонический Media3):
 *   UI → RadioPlayerController → MediaController ──binder──> Service (player + session)
 *
 * Раньше (v1.6-v1.10.3) UI работал с ExoPlayer напрямую через controller.
 * Media3 не считал session «активной» (нет подключенного клиента) →
 * не публиковал media-notification → lock screen был пуст.
 *
 * Сейчас MediaController = подключенный клиент → Media3 автоматически
 * постит rich media notification с обложкой + кнопками play/pause/skip.
 *
 * Singleton (Hilt). MediaController создаётся LAZY на первый play() —
 * сервис может ещё не быть запущен.
 */
@OptIn(UnstableApi::class)
class RadioPlayerController(private val context: Context) {

    // ────────────────────── Public state (для UI) ──────────────────────

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackState = MutableStateFlow(RadioPlaybackState.IDLE)
    val playbackState: StateFlow<RadioPlaybackState> = _playbackState.asStateFlow()

    private val _currentStation = MutableStateFlow<Station?>(null)
    val currentStation: StateFlow<Station?> = _currentStation.asStateFlow()

    private val _hasError = MutableStateFlow(false)
    val hasError: StateFlow<Boolean> = _hasError.asStateFlow()

    /**
     * Текущий трек из ICY metadata потока. null если поток метаданные
     * не отдаёт (talk-станции обычно нет) или ещё не пришло.
     */
    private val _nowPlaying = MutableStateFlow<String?>(null)
    val nowPlaying: StateFlow<String?> = _nowPlaying.asStateFlow()

    /**
     * Скрыт ли mini-player (юзер свайпнул его в сторону).
     * Радио продолжает играть, notification на месте — просто mini-player
     * не отображается на главной/других экранах. Сбрасывается в false
     * когда юзер заходит в радио-экран (RadioScreen.LaunchedEffect).
     *
     * Session-scoped — не персистится. App restart → false.
     */
    private val _miniPlayerHidden = MutableStateFlow(false)
    val miniPlayerHidden: StateFlow<Boolean> = _miniPlayerHidden.asStateFlow()

    fun hideMiniPlayer() { _miniPlayerHidden.value = true }
    fun showMiniPlayer() { _miniPlayerHidden.value = false }

    /** Callback для статистики прослушивания. */
    var onSessionEnded: ((startedAt: Long, endedAt: Long, stationId: String) -> Unit)? = null

    /**
     * Callback когда станция признана «мёртвой» — auto-reconnect исчерпал
     * все попытки. ViewModel блокирует её в displayedStations + auto-skip
     * на следующую рабочую.
     */
    var onStationDead: ((Station) -> Unit)? = null

    /**
     * Контекст станций для next/previous (отфильтрованный список из VM).
     * Mini-player и Service media-notification кнопки skip работают по нему.
     */
    private val _stationContext = MutableStateFlow<List<Station>>(emptyList())
    val stationContext: StateFlow<List<Station>> = _stationContext.asStateFlow()

    fun setStationContext(stations: List<Station>) {
        _stationContext.value = stations
    }

    // ────────────────────── Coroutine scope ──────────────────────

    /**
     * Scope для фоновых задач controller'а (auto-reconnect, etc).
     * SupervisorJob — падение одной корутины не убивает остальные.
     * Main dispatcher — все вызовы MediaController должны быть на main.
     */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // ────────────────────── Auto-reconnect ──────────────────────

    /**
     * Авто-переподключение при разрыве потока (CDN flap, network blip).
     * Раньше: stream упал → ERROR навсегда пока юзер не тапнет skip.
     * Стало: 3 попытки с exponential backoff (1s, 2s, 4s).
     */
    private var reconnectAttempts = 0
    private var reconnectJob: Job? = null
    private val maxReconnectAttempts = 3

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        val station = _currentStation.value ?: return
        if (reconnectAttempts >= maxReconnectAttempts) {
            Log.w(TAG_ICY, "auto-reconnect: max attempts reached, station ${station.name} is dead")
            reconnectAttempts = 0
            // Сообщаем VM что станция мёртвая → она заблочит + переключит
            onStationDead?.invoke(station)
            return
        }
        val attempt = reconnectAttempts + 1
        val delayMs = 1000L * (1L shl reconnectAttempts)  // 1s, 2s, 4s
        Log.d(TAG_ICY, "auto-reconnect: attempt $attempt in ${delayMs}ms for ${station.name}")
        reconnectJob = scope.launch {
            delay(delayMs)
            reconnectAttempts++
            play(station)
        }
    }

    private fun resetReconnect() {
        reconnectAttempts = 0
        reconnectJob?.cancel()
        reconnectJob = null
    }

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

    // ────────────────────── MediaController connection ──────────────────────

    private var mediaController: MediaController? = null
    private var connectionFuture: ListenableFuture<MediaController>? = null

    /** Команды, ожидающие подключения. Выполняются после connect. */
    private val pendingCommands = ArrayDeque<(MediaController) -> Unit>()

    private val executor: Executor = ContextCompat.getMainExecutor(context)

    /**
     * Lazy подключение к RadioPlayerService через MediaController.
     * Если ещё не подключены — запускаем service + buildAsync,
     * команда выполнится в callback после connect.
     */
    private fun ensureConnectedAndRun(command: (MediaController) -> Unit) {
        // Уже подключены — выполняем сразу
        mediaController?.let { c ->
            if (c.isConnected) {
                command(c)
                return
            }
        }

        pendingCommands.addLast(command)

        // Если уже идёт connect — просто ждём
        if (connectionFuture != null) return

        // Стартуем service явно (на случай если ещё не запущен) +
        // запускаем connect к session token
        runCatching {
            ContextCompat.startForegroundService(
                context, Intent(context, RadioPlayerService::class.java),
            )
        }

        val token = SessionToken(
            context,
            ComponentName(context, RadioPlayerService::class.java),
        )
        val future = MediaController.Builder(context, token).buildAsync()
        connectionFuture = future
        future.addListener({
            runCatching {
                val controller = future.get()
                mediaController = controller
                attachListeners(controller)
                // Выполняем все накопленные команды
                while (pendingCommands.isNotEmpty()) {
                    pendingCommands.removeFirst().invoke(controller)
                }
            }.onFailure { e ->
                pendingCommands.clear()
                runCatching {
                    com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                        .recordException(RuntimeException("[RadioController] MediaController connect failed", e))
                }
            }
            connectionFuture = null
        }, executor)
    }

    /** Подписка на изменения состояния плеера через MediaController. */
    private fun attachListeners(controller: MediaController) {
        controller.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
                if (playing) {
                    _hasError.value = false
                    resetReconnect()  // успешно играем → счётчик попыток в 0
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
                Log.w(TAG_ICY, "player error: ${error.message}", error)
                _hasError.value = true
                _isPlaying.value = false
                _playbackState.value = RadioPlaybackState.ERROR
                endSessionIfActive()
                // Auto-reconnect — поток мог временно упасть (CDN flap, сеть)
                scheduleReconnect()
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                Log.d(TAG_ICY, "onMediaMetadataChanged: title=${mediaMetadata.title}, artist=${mediaMetadata.artist}, displayTitle=${mediaMetadata.displayTitle}")
                val raw = mediaMetadata.title?.toString()
                    ?: mediaMetadata.displayTitle?.toString()
                handleIncomingTitle(raw)
            }

            /**
             * Прямой listener на raw ICY/ID3 frames из аудио-потока.
             * Это БОЛЕЕ НАДЁЖНЫЙ путь чем onMediaMetadataChanged для радио:
             *   - HTTP Shoutcast/Icecast потоки шлют ICY frames с StreamTitle
             *   - HLS потоки шлют ID3 TextInformationFrame (TIT2, TPE1)
             *   - onMediaMetadataChanged их МЕРДЖИТ но не всегда корректно
             *     для radio (он рассчитан больше на VOD контент)
             */
            override fun onMetadata(metadata: Metadata) {
                Log.d(TAG_ICY, "onMetadata: ${metadata.length()} entries")
                for (i in 0 until metadata.length()) {
                    val entry = metadata.get(i)
                    val className = entry::class.simpleName ?: "?"
                    Log.d(TAG_ICY, "  [$i] $className: $entry")
                    val title = extractTitleFromMetadataEntry(entry)
                    if (title != null) {
                        Log.d(TAG_ICY, "  extracted title: '$title'")
                        handleIncomingTitle(title)
                    }
                }
            }

            /**
             * Срабатывает когда плеер сам переключился на следующий/предыдущий
             * media item (например юзер нажал skip на media-notification).
             * Обновляем _currentStation чтобы UI и StateFlow синхронизировались.
             */
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val mediaId = mediaItem?.mediaId ?: return
                val station = _stationContext.value.find { it.id == mediaId }
                if (station != null) {
                    _currentStation.value = station
                    _nowPlaying.value = null  // новый ICY придёт
                    _hasError.value = false
                }
            }
        })
    }

    /**
     * Применяем title из ICY/ID3 — но только если это похоже на трек,
     * а не на name самой станции (наш initial value из MediaItem).
     */
    private fun handleIncomingTitle(raw: String?) {
        val cleaned = sanitizeNowPlaying(raw)
        val stationName = _currentStation.value?.name
        val isStationNameEcho = cleaned != null && stationName != null &&
            (cleaned.equals(stationName, ignoreCase = true) ||
             cleaned.contains(stationName, ignoreCase = true) ||
             stationName.contains(cleaned, ignoreCase = true))
        _nowPlaying.value = when {
            cleaned == null -> null
            isStationNameEcho -> null
            else -> cleaned
        }
        Log.d(TAG_ICY, "nowPlaying = ${_nowPlaying.value} (raw='$raw', cleaned='$cleaned', echo=$isStationNameEcho)")
    }

    private fun updatePlaybackState() {
        val c = mediaController ?: run {
            _playbackState.value = RadioPlaybackState.IDLE
            return
        }
        _playbackState.value = when {
            _hasError.value -> RadioPlaybackState.ERROR
            c.playbackState == Player.STATE_IDLE -> RadioPlaybackState.IDLE
            c.playbackState == Player.STATE_BUFFERING -> RadioPlaybackState.BUFFERING
            c.playbackState == Player.STATE_ENDED -> RadioPlaybackState.ENDED
            c.isPlaying -> RadioPlaybackState.PLAYING
            else -> RadioPlaybackState.PAUSED
        }
    }

    // ────────────────────── Public commands ──────────────────────

    /**
     * Играть станцию. Загружает ВСЮ карусель (stationContext) как playlist,
     * выбирая нужную как стартовую. Это даёт:
     *  - Media3 показывает skip prev/next кнопки в notification (queue > 1)
     *  - Bluetooth headset / Android Auto navigation работает
     *  - seekToNext/Previous переключают между станциями
     *
     * Если stationContext пуст или станция не в нём — играем как single item.
     */
    fun play(station: Station) {
        _hasError.value = false
        _currentStation.value = station
        _nowPlaying.value = null  // новый трек придёт из ICY
        // Если play() вызван НЕ из reconnect-loop (новая станция, тап юзера) —
        // сбрасываем счётчик попыток
        val isReconnecting = reconnectJob?.isActive == true
        if (!isReconnecting) resetReconnect()

        val context = _stationContext.value
        val playStation = station
        val (items, startIndex) = if (context.size <= 1 || !context.any { it.id == playStation.id }) {
            listOf(playStation.toMediaItem()) to 0
        } else {
            context.map { it.toMediaItem() } to context.indexOfFirst { it.id == playStation.id }
        }

        ensureConnectedAndRun { controller ->
            // v1.15.3: явно playWhenReady=true (защита от прошлого prepareOnly)
            controller.playWhenReady = true
            controller.setMediaItems(items, startIndex.coerceAtLeast(0), 0L)
            controller.prepare()
            controller.play()
        }
    }

    /**
     * v1.12.5: «Подготовить» станцию БЕЗ автоплея.
     * Используется при заходе на /radio чтобы UI знал «текущую» станцию,
     * но не начал играть автоматически. Юзер сам нажмёт ▶.
     *
     * v1.15.3: ВАЖНО — `controller.playWhenReady = false` ДО prepare().
     * ExoPlayer по умолчанию имеет playWhenReady=true: prepare() сразу
     * запускает воспроизведение когда станция загружена. Юзер жаловался
     * «радио включается само при переходе на экран» — это и было причиной.
     */
    fun prepareOnly(station: Station) {
        _hasError.value = false
        _currentStation.value = station
        _nowPlaying.value = null
        // НЕ сбрасываем reconnect — это «pre-load», а не активная сессия.
        val context = _stationContext.value
        val playStation = station
        val (items, startIndex) = if (context.size <= 1 || !context.any { it.id == playStation.id }) {
            listOf(playStation.toMediaItem()) to 0
        } else {
            context.map { it.toMediaItem() } to context.indexOfFirst { it.id == playStation.id }
        }
        ensureConnectedAndRun { controller ->
            controller.playWhenReady = false  // КРИТИЧНО — не играть auto после prepare
            controller.setMediaItems(items, startIndex.coerceAtLeast(0), 0L)
            controller.prepare()
        }
    }

    /** Station → MediaItem с stable mediaId (для onMediaItemTransition matching). */
    private fun Station.toMediaItem(): MediaItem = MediaItem.Builder()
        .setMediaId(id)
        .setUri(streamUrl)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(name)
                .setArtist("${country.emoji} ${country.displayName} · ${genre.displayName}")
                .setStation(name)
                .setIsBrowsable(false)
                .setIsPlayable(true)
                .build()
        )
        .build()

    fun pause() {
        mediaController?.takeIf { it.isConnected }?.pause()
    }

    fun resume() {
        ensureConnectedAndRun { it.play() }
    }

    fun togglePlayback() {
        val c = mediaController
        if (c != null && c.isConnected) {
            if (c.isPlaying) c.pause() else c.play()
        } else {
            resume()
        }
    }

    /**
     * Переключение на следующую станцию в контексте.
     * Если подключены и queue > 1 — используем нативный seekToNextMediaItem
     * (тот же метод что зовётся из media notification skip-кнопки).
     * Иначе fallback на play() с manual ресетом queue.
     */
    fun nextStation() {
        val mc = mediaController
        if (mc != null && mc.isConnected && mc.mediaItemCount > 1) {
            if (mc.hasNextMediaItem()) {
                mc.seekToNextMediaItem()
            } else {
                mc.seekTo(0, 0L)  // wrap to first
            }
            return
        }
        // Fallback (queue не загружен ещё)
        val list = _stationContext.value
        if (list.isEmpty()) return
        val current = _currentStation.value
        val idx = list.indexOfFirst { it.id == current?.id }
        val next = if (idx < 0) list.first() else list.getOrNull(idx + 1) ?: list.first()
        play(next)
    }

    fun previousStation() {
        val mc = mediaController
        if (mc != null && mc.isConnected && mc.mediaItemCount > 1) {
            if (mc.hasPreviousMediaItem()) {
                mc.seekToPreviousMediaItem()
            } else {
                mc.seekTo(mc.mediaItemCount - 1, 0L)  // wrap to last
            }
            return
        }
        // Fallback
        val list = _stationContext.value
        if (list.isEmpty()) return
        val current = _currentStation.value
        val idx = list.indexOfFirst { it.id == current?.id }
        val prev = if (idx < 0) list.last() else list.getOrNull(idx - 1) ?: list.last()
        play(prev)
    }

    fun release() {
        mediaController?.release()
        mediaController = null
        connectionFuture?.cancel(true)
        connectionFuture = null
        pendingCommands.clear()
    }

    /**
     * Полная остановка радио — для swipe-to-dismiss на mini-player.
     * 1. Cancel pending reconnect
     * 2. Stop player + release MediaController
     * 3. Clear все StateFlow → UI hide mini-player
     * 4. Stop foreground service → notification исчезает
     *
     * После этого радио в состоянии «не запущено». Юзер должен явно
     * тапнуть станцию чтобы возобновить.
     */
    fun stop() {
        // Cancel auto-reconnect
        reconnectJob?.cancel()
        reconnectJob = null
        reconnectAttempts = 0

        // Stop player через MediaController
        runCatching {
            val mc = mediaController
            if (mc != null && mc.isConnected) {
                mc.stop()
                mc.clearMediaItems()
            }
        }

        // Release controller binding → отвязывает session
        runCatching { mediaController?.release() }
        mediaController = null
        connectionFuture?.cancel(true)
        connectionFuture = null
        pendingCommands.clear()

        // Сбрасываем StateFlow — UI триггерит hide mini-player через
        // visible = (station != null && !isOnRadioScreen)
        endSessionIfActive()
        _isPlaying.value = false
        _hasError.value = false
        _nowPlaying.value = null
        _playbackState.value = RadioPlaybackState.IDLE
        _currentStation.value = null

        // Stop foreground service → notification исчезает
        runCatching {
            context.stopService(Intent(context, RadioPlayerService::class.java))
        }

        Log.d(TAG_ICY, "stop() — player fully released")
    }
}

/**
 * Извлечь title из Metadata.Entry — работает для всех типов которые
 * Media3 может прислать из радио-потока:
 *
 *   IcyInfo            — Shoutcast/Icecast ICY frames (плоский HTTP MP3/AAC)
 *   TextInformationFrame — ID3v2 frames (TIT2 = title, TPE1 = artist), HLS-стримы
 *   PrivFrame          — private ID3 frames с custom payload
 *
 * Используем reflection / instanceof через имя класса чтобы не зависеть
 * жёстко от конкретных media3-extractor классов в импортах.
 */
@OptIn(UnstableApi::class)
internal fun extractTitleFromMetadataEntry(entry: Metadata.Entry): String? {
    val className = entry::class.simpleName ?: return null
    val text = entry.toString()

    return when {
        className == "IcyInfo" -> {
            // IcyInfo.toString() формата: "ICY: title=\"Artist - Track\", url=\"...\""
            extractQuoted(text, "title=")
        }
        className == "IcyHeaders" -> {
            // IcyHeaders несёт name/genre/url — обычно name = station name, не нужно
            null
        }
        className == "TextInformationFrame" -> {
            // TextInformationFrame { id=TIT2, value=Artist - Track }
            // или id=TPE1 для artist. Берём value если id=TIT2 (title).
            val id = extractField(text, "id=")
            if (id == "TIT2" || id == "TT2") {
                extractField(text, "value=")
            } else null
        }
        else -> null
    }
}

private fun extractQuoted(s: String, key: String): String? {
    val idx = s.indexOf(key)
    if (idx < 0) return null
    val rest = s.substring(idx + key.length)
    if (!rest.startsWith("\"")) return null
    val endIdx = rest.indexOf('"', 1)
    if (endIdx <= 1) return null
    return rest.substring(1, endIdx).takeIf { it.isNotBlank() }
}

private fun extractField(s: String, key: String): String? {
    val idx = s.indexOf(key)
    if (idx < 0) return null
    val rest = s.substring(idx + key.length)
    val endIdx = rest.indexOfAny(charArrayOf(',', '}', ' '))
    val raw = if (endIdx < 0) rest else rest.substring(0, endIdx)
    return raw.trim('"', ' ', ',', '}').takeIf { it.isNotBlank() }
}

/**
 * Чистит текст метаданных перед показом юзеру.
 *  - null/blank → null
 *  - ограничиваем длину 120 символов
 *  - убираем control characters, RTL-override, zero-width chars
 *  - убираем «typical noise» из ICY: unknown, no title, "-"
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
    val lower = cleaned.lowercase()
    if (lower in setOf("unknown", "no title", "-", "—", "n/a")) return null
    return cleaned
}
