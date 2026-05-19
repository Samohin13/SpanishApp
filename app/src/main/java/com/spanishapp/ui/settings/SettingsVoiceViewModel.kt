package com.spanishapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.prefs.VoicePreferences
import com.spanishapp.data.prefs.VoiceSettings
import com.spanishapp.data.repository.AuthRepository
import com.spanishapp.domain.voice.TutorPersonality
import com.spanishapp.domain.voice.VoiceGender
import com.spanishapp.service.RemoteTtsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsVoiceViewModel @Inject constructor(
    private val prefs: VoicePreferences,
    private val authRepository: AuthRepository,
    private val remoteTts: RemoteTtsService,
) : ViewModel() {

    val settings: StateFlow<VoiceSettings> = prefs.settings.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = VoiceSettings()
    )

    /** v1.18.20: выбранный характер репетитора. */
    val personality: StateFlow<TutorPersonality> = authRepository.tutorPersonality
        .map { TutorPersonality.byId(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TutorPersonality.DEFAULT
        )

    /** v1.18.21: пол голоса (общий для ru+es). */
    val voiceGender: StateFlow<VoiceGender> = authRepository.voiceGender
        .map { VoiceGender.byId(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = VoiceGender.DEFAULT
        )

    /** v1.18.25: пользовательский множитель скорости (0.5..1.5). */
    val voiceSpeedMultiplier: StateFlow<Float> = authRepository.voiceSpeedMultiplier
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 1.0f
        )

    fun setVoiceSpeedMultiplier(value: Float) = viewModelScope.launch {
        authRepository.setVoiceSpeedMultiplier(value)
    }

    val isPremiumTtsReady: StateFlow<Boolean> = remoteTts.isReady
    val isPreviewPlaying: StateFlow<Boolean> = remoteTts.isPlaying

    fun selectPersonality(personality: TutorPersonality) = viewModelScope.launch {
        authRepository.setTutorPersonality(personality.id)
    }

    fun selectVoiceGender(gender: VoiceGender) = viewModelScope.launch {
        authRepository.setVoiceGender(gender.id)
    }

    /** Прослушать seed-фразу выбранного характера (mix ru + es). */
    fun previewPersonality(personality: TutorPersonality) {
        val sample = when (personality) {
            TutorPersonality.STRICT -> "Здравствуйте. Сегодня изучаем глагол ser. Hola, ¿cómo está usted?"
            TutorPersonality.POLITE -> "Привет, давай начнём урок. Hola, ¿cómo estás?"
            TutorPersonality.FRIENDLY -> "Хей, погнали учить испанский! ¡Hola, qué tal!"
            TutorPersonality.ROMANTIC -> "Какое прекрасное утро. Hola, mi querido amigo."
        }
        remoteTts.speak(sample)
    }

    fun stopPreview() = remoteTts.stop()

    fun selectVoice(name: String) = viewModelScope.launch { prefs.setVoiceName(name) }
    fun setRate(rate: Float)      = viewModelScope.launch { prefs.setRate(rate) }
    fun setPitch(pitch: Float)    = viewModelScope.launch { prefs.setPitch(pitch) }
    fun markPromptSeen()          = viewModelScope.launch { prefs.markPromptSeen() }
}
