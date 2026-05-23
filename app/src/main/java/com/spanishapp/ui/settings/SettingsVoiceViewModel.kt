package com.spanishapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.repository.AuthRepository
import com.spanishapp.domain.voice.PremiumVoiceCatalog
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
    private val authRepository: AuthRepository,
    private val remoteTts: RemoteTtsService,
) : ViewModel() {

    val selectedRuVoice: StateFlow<String> = authRepository.selectedRuVoice
        .map { it ?: PremiumVoiceCatalog.DEFAULT_RU_VOICE }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PremiumVoiceCatalog.DEFAULT_RU_VOICE
        )

    val selectedEsVoice: StateFlow<String> = authRepository.selectedEsVoice
        .map { it ?: PremiumVoiceCatalog.DEFAULT_ES_VOICE }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PremiumVoiceCatalog.DEFAULT_ES_VOICE
        )

    val voiceSpeedMultiplier: StateFlow<Float> = authRepository.voiceSpeedMultiplier
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 1.0f
        )

    val isPremiumTtsReady: StateFlow<Boolean> = remoteTts.isReady
    val isPreviewPlaying: StateFlow<Boolean> = remoteTts.isPlaying
    val isPreviewLoading: StateFlow<Boolean> = remoteTts.isLoading

    init {
        // v1.22.7: прогреваем превью всех 8 голосов как только экран открыт.
        // Файлы качаются в фоне → к моменту первого тапа ▶ они уже на диске,
        // плеер стартует за ~100-200ms вместо 2-3 секунд.
        if (remoteTts.isReady.value) {
            viewModelScope.launch {
                val ruSample = "Привет, давай учить испанский вместе."
                val esSample = "Hola, vamos a aprender español juntos."
                PremiumVoiceCatalog.RU_VOICES.forEach { v ->
                    remoteTts.prefetchPreview(ruSample, v.id)
                }
                PremiumVoiceCatalog.ES_VOICES.forEach { v ->
                    remoteTts.prefetchPreview(esSample, v.id)
                }
            }
        }
    }

    fun selectRuVoice(voiceId: String) = viewModelScope.launch {
        authRepository.setSelectedRuVoice(voiceId)
    }

    fun selectEsVoice(voiceId: String) = viewModelScope.launch {
        authRepository.setSelectedEsVoice(voiceId)
    }

    fun setVoiceSpeedMultiplier(value: Float) = viewModelScope.launch {
        authRepository.setVoiceSpeedMultiplier(value)
    }

    /** Прослушать сэмпл выбранного голоса (русский или испанский). */
    fun previewVoice(voiceId: String) {
        val isRussian = voiceId.startsWith("ru-RU") || voiceId.contains("Multilingual")
        val sample = if (isRussian)
            "Привет, давай учить испанский вместе."
        else
            "Hola, vamos a aprender español juntos."
        remoteTts.previewVoice(sample, voiceId)
    }

    fun stopPreview() = remoteTts.stop()
}
