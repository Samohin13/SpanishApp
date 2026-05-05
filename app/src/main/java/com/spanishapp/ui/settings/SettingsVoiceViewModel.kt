package com.spanishapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.prefs.VoicePreferences
import com.spanishapp.data.prefs.VoiceSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsVoiceViewModel @Inject constructor(
    private val prefs: VoicePreferences
) : ViewModel() {

    val settings: StateFlow<VoiceSettings> = prefs.settings.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = VoiceSettings()
    )

    fun selectVoice(name: String) = viewModelScope.launch { prefs.setVoiceName(name) }
    fun setRate(rate: Float)      = viewModelScope.launch { prefs.setRate(rate) }
    fun setPitch(pitch: Float)    = viewModelScope.launch { prefs.setPitch(pitch) }
    fun markPromptSeen()          = viewModelScope.launch { prefs.markPromptSeen() }
}
