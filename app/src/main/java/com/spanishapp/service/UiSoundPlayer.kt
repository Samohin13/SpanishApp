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

    @Volatile private var enabled: Boolean = true

    private val audioManager: AudioManager? =
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4) // 4 overlapping sounds is plenty for UI cues
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    /** Map<Sound, soundPoolId>. Filled at construction; entries
     *  appear after SoundPool.load(...) returns and onLoadComplete fires. */
    private val soundIds: MutableMap<Sound, Int> = mutableMapOf()

    init {
        // Preload all 15 sounds immediately. SoundPool.load() returns a
        // soundId before decoding finishes; play() before onLoadComplete
        // will silently no-op (acceptable — UI cues are not critical).
        for (s in Sound.values()) {
            runCatching {
                val id = pool.load(context, s.rawRes, 1)
                soundIds[s] = id
            }
        }

        // Observe user toggle. Reuses the existing soundEffectsEnabled
        // preference so the Settings switch (settings_sound_effects)
        // gates both the legacy generated-tone SoundPlayer AND this one.
        scope.launch {
            appPreferences.soundEffectsEnabled.collect { enabled = it }
        }
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
        if (!enabled) return
        if (audioManager?.ringerMode == AudioManager.RINGER_MODE_SILENT) return
        val id = soundIds[sound] ?: return
        val v = volume.coerceIn(0f, 1f)
        runCatching {
            pool.play(id, v, v, /*priority=*/1, /*loop=*/0, /*rate=*/1f)
        }
    }

    /** Release SoundPool. Called rarely — singleton lives for app lifetime. */
    fun release() {
        runCatching { pool.release() }
        soundIds.clear()
    }
}
