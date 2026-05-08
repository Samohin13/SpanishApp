package com.spanishapp.service

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps the platform Vibrator with intensity levels (0..3) and amplitude support
 * on API 26+. On older devices, falls back to scaled timings.
 */
@Singleton
class VibrationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Tactile tick scaled by [percent] (0..100). 0 = off (no-op).
     * Single short pulse — for button presses, toggles, answer feedback.
     */
    fun tick(percent: Int) {
        val v = vibrator ?: return
        if (!v.hasVibrator() || percent <= 0) return

        val p = percent.coerceIn(1, 100)
        // Duration 10..40ms scales linearly with intensity.
        val durationMs = (10 + (p * 30 / 100)).toLong()
        // Amplitude must be ≥1 on API 26+ (1..255).
        val amplitude = (p * 255 / 100).coerceIn(1, 255)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = if (v.hasAmplitudeControl()) {
                VibrationEffect.createOneShot(durationMs, amplitude)
            } else {
                VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
            }
            v.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(durationMs)
        }
    }

    /** A heavier "success" pattern — for level-up/correct events. */
    fun pulse(percent: Int) {
        val v = vibrator ?: return
        if (!v.hasVibrator() || percent <= 0) return

        val amp = (percent * 255 / 100).coerceIn(1, 255)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 25, 60, 25)
            val amplitudes = intArrayOf(0, amp, 0, amp)
            val effect = if (v.hasAmplitudeControl()) {
                VibrationEffect.createWaveform(pattern, amplitudes, -1)
            } else {
                VibrationEffect.createWaveform(pattern, -1)
            }
            v.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(longArrayOf(0, 25, 60, 25), -1)
        }
    }
}
