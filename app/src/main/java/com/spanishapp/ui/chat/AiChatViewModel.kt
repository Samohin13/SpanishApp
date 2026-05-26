package com.spanishapp.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.repository.AiChatRepository
import com.spanishapp.data.repository.ChatCorrection
import com.spanishapp.data.repository.parseCorrections
import com.spanishapp.domain.chat.ChatScenario
import com.spanishapp.domain.chat.ChatScenarios
import com.spanishapp.service.SpanishSpeechRecognizer
import com.spanishapp.service.SpanishTts
import com.spanishapp.service.SpeechResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@kotlinx.coroutines.ExperimentalCoroutinesApi
class AiChatViewModel @Inject constructor(
    private val repo: AiChatRepository,
    private val tts: SpanishTts,
    private val stt: SpanishSpeechRecognizer,
) : ViewModel() {

    // ── Выбранный сценарий ─────────────────────────────────────
    private val _scenario = MutableStateFlow(ChatScenarios.DEFAULT)
    val scenario: StateFlow<ChatScenario> = _scenario.asStateFlow()

    fun selectScenario(scenario: ChatScenario) {
        _scenario.value = scenario
    }

    // ── История сообщений (реактивно из БД) ────────────────────
    val messages = _scenario
        .flatMapLatest { repo.observeSession(it.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Состояния UI ───────────────────────────────────────────
    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ── Голосовой ввод ─────────────────────────────────────────
    val isListening: StateFlow<Boolean> = stt.isListening
    val voiceAmplitude: StateFlow<Float> = stt.rmsDb

    // ── Отправка сообщения ─────────────────────────────────────
    fun send(text: String, level: String = "B1") {
        val trimmed = text.trim()
        if (trimmed.isBlank() || _isSending.value) return

        viewModelScope.launch {
            _isSending.value = true
            _error.value = null

            repo.sendMessage(trimmed, _scenario.value, level)
                .onFailure { _error.value = "Не удалось получить ответ. Проверь интернет." }

            _isSending.value = false
        }
    }

    fun dismissError() { _error.value = null }

    // ── Голосовой ввод: разовая запись ─────────────────────────
    fun startVoice(onTextReady: (String) -> Unit) {
        viewModelScope.launch {
            when (val r = stt.listenOnce(language = "es-ES")) {
                is SpeechResult.Success   -> onTextReady(r.text)
                is SpeechResult.Error     -> if (!r.isSilence) _error.value = r.message
                is SpeechResult.Cancelled -> { /* юзер отменил */ }
            }
        }
    }

    // ── TTS озвучка ────────────────────────────────────────────
    fun speak(text: String) { tts.speak(text) }

    // ── Очистка сценария ───────────────────────────────────────
    fun clearCurrentSession() {
        viewModelScope.launch {
            repo.clearSession(_scenario.value.id)
        }
    }

    // ── Парсинг коррекций для UI ───────────────────────────────
    fun corrections(json: String): List<ChatCorrection> = parseCorrections(json)
}
