package com.spanishapp.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.repository.AiChatRepository
import com.spanishapp.data.repository.ChatCorrection
import com.spanishapp.data.repository.parseCorrections
import com.spanishapp.domain.chat.ChatScenario
import com.spanishapp.domain.chat.ChatScenarios
import com.spanishapp.data.db.dao.ChatMessageDao
import com.spanishapp.data.db.entity.ChatMessageEntity
import com.spanishapp.data.prefs.UserWordFrequency
import com.spanishapp.service.AiChatLimiter
import com.spanishapp.service.SpanishSpeechRecognizer
import com.spanishapp.service.SpanishTts
import com.spanishapp.service.SpeechResult
import com.spanishapp.service.SubscriptionManager
import com.spanishapp.service.VoicePlayer
import com.spanishapp.service.VoiceRecorder
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
    private val limiter: AiChatLimiter,
    private val subscriptionManager: SubscriptionManager,
    private val chatDao: ChatMessageDao,
    val userWordFrequency: UserWordFrequency,
    val voiceRecorder: VoiceRecorder,
    val voicePlayer: VoicePlayer,
) : ViewModel() {

    // ── Voice messages ────────────────────────────────────────
    val voiceIsRecording: StateFlow<Boolean> = voiceRecorder.isRecording
    val voiceElapsedMs: StateFlow<Long> = voiceRecorder.elapsedMs
    val voiceAmpRec: StateFlow<Float> = voiceRecorder.amplitude

    /** Старт записи. UI должен показать overlay. */
    fun startVoiceRecord(): Boolean = voiceRecorder.start() != null

    /** Завершить запись + отправить (сохранить сообщение с audioPath). */
    fun stopAndSendVoiceMessage() {
        val result = voiceRecorder.stop() ?: return
        val (path, duration) = result
        viewModelScope.launch {
            chatDao.insert(
                ChatMessageEntity(
                    role = "user",
                    content = "",
                    sessionId = _scenario.value.id,
                    audioPath = path,
                    audioDurationMs = duration,
                )
            )
        }
    }

    /** Отмена — удаляет файл записи. */
    fun cancelVoiceRecord() { voiceRecorder.cancel() }

    /** Toggle play/pause для воспроизведения голосового. */
    fun toggleVoicePlay(path: String) {
        if (voicePlayer.currentPath.value == path) {
            if (voicePlayer.isPlaying.value) voicePlayer.pause()
            else voicePlayer.resume()
        } else {
            voicePlayer.play(path)
        }
    }

    override fun onCleared() {
        voicePlayer.stop()
        voiceRecorder.cancel()
        super.onCleared()
    }

    /** PRO-юзер обходит лимит. */
    val isPro: StateFlow<Boolean> = subscriptionManager.isProActive
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Сколько запросов осталось сегодня. PRO видит -1 (не показываем). */
    val remainingMessages: StateFlow<Int> = limiter.remainingToday
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AiChatLimiter.DAILY_LIMIT)

    // ── Выбранный сценарий ─────────────────────────────────────
    private val _scenario = MutableStateFlow(ChatScenarios.DEFAULT)
    val scenario: StateFlow<ChatScenario> = _scenario.asStateFlow()

    fun selectScenario(scenario: ChatScenario) {
        // v1.24.18: PRO-guard — free-юзер не может выбрать PRO сценарий
        if (scenario.isPro && !isPro.value) {
            _error.value = "Этот сценарий доступен только в PRO. Обнови подписку."
            return
        }
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
            // PRO-юзеры обходят лимит. Free → проверяем.
            if (!isPro.value && limiter.isExhausted()) {
                _error.value = "Достигнут дневной лимит ${AiChatLimiter.DAILY_LIMIT} сообщений. " +
                    "Лимит обновится завтра, либо подключи PRO для безлимита."
                return@launch
            }

            _isSending.value = true
            _error.value = null

            repo.sendMessage(trimmed, _scenario.value, level, "ESPEAK")
                .onSuccess {
                    if (!isPro.value) limiter.increment()
                    userWordFrequency.recordText(trimmed)  // learn from sent message
                }
                .onFailure { _error.value = "Не удалось получить ответ. Проверь интернет." }

            _isSending.value = false
        }
    }

    fun dismissError() { _error.value = null }

    // ── Голосовой ввод: разовая запись ─────────────────────────
    // language: "es-ES" (default) или "ru-RU". Передаётся из UI на основе
    // текущей раскладки клавиатуры или предыдущего ввода.
    fun startVoice(language: String = "es-ES", onTextReady: (String) -> Unit) {
        viewModelScope.launch {
            when (val r = stt.listenOnce(language = language)) {
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
