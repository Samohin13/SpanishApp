package com.spanishapp.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import com.spanishapp.R
import com.spanishapp.data.prefs.AppPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mixkit-based UI sound player.
 *
 * Plays short (<2s) MP3 cues from `res/raw/snd_*.mp3` through Android SoundPool.
 *
 * Why SoundPool (not MediaPlayer):
 *  - <50ms latency vs MediaPlayer's ~200ms
 *  - lock-free concurrent playback (up to maxStreams overlapping sounds)
 *  - in-memory preloaded — no disk I/O on each play()
 *  - no per-play Player object allocation
 *
 * Respects:
 *  - User toggle: AppPreferences.soundEffectsEnabled (same flag as legacy
 *    generated tones in [SoundPlayer]). Toggle OFF → all play() become no-op.
 *  - Silent ringer: skipped if device ringer mode is RINGER_MODE_SILENT.
 *
 * All 15 sounds preloaded at @Inject time (~900KB resident memory).
 */
@Singleton
class UiSoundPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences,
) {

    enum class Sound(val rawRes: Int) {
        CORRECT(R.raw.snd_correct),
        WRONG(R.raw.snd_wrong),
        XP(R.raw.snd_xp),
        TRANSITION(R.raw.snd_transition),
        TAP(R.raw.snd_tap),
        PAGE(R.raw.snd_page),
        GOLD(R.raw.snd_gold),
        SILVER(R.raw.snd_silver),
        BRONZE(R.raw.snd_bronze),
        FAIL(R.raw.snd_fail),
        STREAK(R.raw.snd_streak),
        LEVEL_UP(R.raw.snd_levelup),
        REC_START(R.raw.snd_rec_start),
        REC_STOP(R.raw.snd_rec_stop),
        LISTEN(R.raw.snd_listen),
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** v1.23.3 (audit Bug 9): enabled теперь StateFlow через stateIn
     *  вместо manual scope.launch{collect{}} — раньше тот коллектор был
     *  permanently leaked (UiSoundPlayer @Singleton, scope никогда не
     *  cancellable). Теперь подписка через stateIn-stream сам управляет
     *  collector lifecycle. Eagerly = подписка стартует сразу как только
     *  кто-то получит UiSoundPlayer через DI. */
    private val enabledState: StateFlow<Boolean> =
        appPreferences.soundEffectsEnabled
            .stateIn(scope, kotlinx.coroutines.flow.SharingStarted.Eagerly, true)

    private val audioManager: AudioManager? =
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    /** SoundPool создаётся лениво в фоне — не блокирует main thread на старте.
     *  v1.22.32: было `private val pool = SoundPool.Builder()...build()` —
     *  eager создание + 15× pool.load() в init выполнялось на main thread
     *  во время SpanishApp.onCreate() (Hilt eager singleton). На медленных
     *  устройствах это вызывало ANR. Теперь всё в IO-scope. */
    @Volatile private var pool: SoundPool? = null

    /** Map<Sound, soundPoolId>. Filled in background after pool is created.
     *  play() безопасно no-op'ает если звук ещё не загружен. */
    private val soundIds: MutableMap<Sound, Int> = java.util.concurrent.ConcurrentHashMap()

    init {
        // Всё инициализируется в фоне — main thread не блокируется.
        // На первом play() сразу после старта приложения SoundPool может
        // быть ещё не готов — это OK, звук просто не сыграет (UI-cues
        // не критичны, в первые ~100ms после старта юзер не успевает тапнуть).
        scope.launch {
            runCatching {
                val newPool = SoundPool.Builder()
                    .setMaxStreams(4) // 4 overlapping sounds is plenty for UI cues
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .build()
                for (s in Sound.values()) {
                    runCatching {
                        val id = newPool.load(context, s.rawRes, 1)
                        soundIds[s] = id
                    }
                }
                pool = newPool
            }
        }
        // v1.23.3: убран старый scope.launch{collect} — заменён на
        // enabledState (stateIn) выше. Один collector, нет лика.
    }

    /**
     * Play [sound]. No-op if:
     *  - user disabled sound effects in Settings
     *  - device ringer is in SILENT mode (respect user wishes)
     *  - sound hasn't finished loading yet (rare; ignored)
     *
     * @param volume 0f..1f (linear), default 1f
     */
    fun play(sound: Sound, volume: Float = 1f) {
        if (!enabledState.value) return
        if (audioManager?.ringerMode == AudioManager.RINGER_MODE_SILENT) return
        val p = pool ?: return // pool ещё не готов — no-op
        val id = soundIds[sound] ?: return
        val v = volume.coerceIn(0f, 1f)
        runCatching {
            p.play(id, v, v, /*priority=*/1, /*loop=*/0, /*rate=*/1f)
        }
    }

    /** Release SoundPool. Called rarely — singleton lives for app lifetime. */
    fun release() {
        runCatching { pool?.release() }
        pool = null
        soundIds.clear()
    }
}
