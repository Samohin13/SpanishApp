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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Тонкая ViewModel чтобы прокинуть `vibrationEnabled` в Composable.
 */
@HiltViewModel
class HapticPrefViewModel @Inject constructor(
    appPreferences: AppPreferences
) : ViewModel() {
    val enabled: StateFlow<Boolean> = appPreferences.vibrationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
}

/**
 * Возвращает обёртку над [LocalHapticFeedback], которая уважает настройку
 * `vibrationEnabled` из Settings. Если выключено — performHapticFeedback
 * становится no-op.
 *
 * Использование (вместо `LocalHapticFeedback.current`):
 *   val haptic = rememberCheckedHaptic()
 *   haptic.performHapticFeedback(HapticFeedbackType.LongPress)
 */
@Composable
fun rememberCheckedHaptic(): HapticFeedback {
    val real = LocalHapticFeedback.current
    val vm: HapticPrefViewModel = hiltViewModel()
    val enabled by vm.enabled.collectAsState()

    return remember(real, enabled) {
        object : HapticFeedback {
            override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
                if (enabled) real.performHapticFeedback(hapticFeedbackType)
            }
        }
    }
}
