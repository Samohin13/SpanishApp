package com.spanishapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.prefs.AppPreferences
import com.spanishapp.service.VibrationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Exposes vibration intensity (0..3) and the [VibrationHelper] to Composables.
 */
@HiltViewModel
class HapticPrefViewModel @Inject constructor(
    appPreferences: AppPreferences,
    val vibrator: VibrationHelper
) : ViewModel() {
    val intensity: StateFlow<Int> = appPreferences.vibrationIntensity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)
}

/**
 * Returns a [HapticFeedback] wrapper that respects the user's vibration
 * intensity setting. Level 0 = no-op. Levels 1..3 use [VibrationHelper]
 * with scaled amplitude (and fall back to the framework's [LocalHapticFeedback]
 * for `TextHandleMove` to keep selection handles native).
 */
@Composable
fun rememberCheckedHaptic(): HapticFeedback {
    val real = LocalHapticFeedback.current
    val vm: HapticPrefViewModel = hiltViewModel()
    val level by vm.intensity.collectAsState()
    val helper = vm.vibrator

    return remember(real, level, helper) {
        object : HapticFeedback {
            override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
                if (level <= 0) return
                when (hapticFeedbackType) {
                    HapticFeedbackType.LongPress -> helper.tick(level)
                    HapticFeedbackType.TextHandleMove -> real.performHapticFeedback(hapticFeedbackType)
                    else -> helper.tick(level)
                }
            }
        }
    }
}
