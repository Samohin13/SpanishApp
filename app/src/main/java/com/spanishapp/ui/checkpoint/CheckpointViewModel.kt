package com.spanishapp.ui.checkpoint

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.repository.LeaderboardRepository
import com.spanishapp.domain.checkpoint.CheckpointData
import com.spanishapp.domain.checkpoint.CheckpointEngine
import com.spanishapp.domain.checkpoint.CheckpointPersonalizer
import com.spanishapp.domain.checkpoint.CheckpointRepository
import com.spanishapp.domain.checkpoint.CheckpointState
import com.spanishapp.domain.checkpoint.CountryMap
import com.spanishapp.service.SpanishSpeechRecognizer
import com.spanishapp.service.SpanishTts
import com.spanishapp.service.SpeechResult
import com.spanishapp.service.XpTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Управляет одной сессией прохождения чекпоинта.
 *
 * Состояния:
 *   - LOADING — грузим JSON
 *   - INTRO — показываем стартовый экран
 *   - PLAYING — идут раунды
 *   - FINISHED — есть outcome
 *
 * Сейчас БЕЗ авто-сохранения между раундами. Это TODO для следующей
 * итерации (нужна Room таблица checkpoint_session_state).
 */
@HiltViewModel
class CheckpointViewModel @Inject constructor(
    private val repository: CheckpointRepository,
    private val engine: CheckpointEngine,
    private val personalizer: CheckpointPersonalizer,
    private val leaderboardRepository: LeaderboardRepository,
    private val tts: SpanishTts,
    private val xpTracker: XpTracker,
    private val speechRecognizer: SpanishSpeechRecognizer,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CheckpointUiState>(CheckpointUiState.Loading)
    val uiState: StateFlow<CheckpointUiState> = _uiState.asStateFlow()

    /** STT: слушаем ли сейчас (для индикации волной/анимацией). */
    val isListening: StateFlow<Boolean> = speechRecognizer.isListening
    val sttRmsDb: StateFlow<Float> = speechRecognizer.rmsDb

    /** Последний распознанный текст для текущего VOICE-раунда (показать юзеру). */
    private val _lastVoiceText = MutableStateFlow<String?>(null)
    val lastVoiceText: StateFlow<String?> = _lastVoiceText.asStateFlow()

    /** Ошибка STT (нет интернета, нет разрешения) — показать ниже кнопки. */
    private val _voiceError = MutableStateFlow<String?>(null)
    val voiceError: StateFlow<String?> = _voiceError.asStateFlow()

    private var roundStartMs = 0L

    /** Грузит чекпоинт по id ("cp1"..) и показывает intro. */
    fun load(checkpointId: String) {
        viewModelScope.launch {
            val rawData = repository.getById(checkpointId)
            if (rawData == null) {
                _uiState.value = CheckpointUiState.Error("Чекпоинт $checkpointId не найден")
                return@launch
            }
            // v1.22.14: персонализация по стране юзера.
            // Юзер из Казахстана играет за «kazaja de Kazajistán», а не «rusa de Rusia».
            // Country определяется по сим-карте / сети устройства (TelephonyManager).
            val isoCode = runCatching { leaderboardRepository.deviceCountryCode() }.getOrNull() ?: "RU"
            val country = CountryMap.byIsoCode(isoCode)
            val personalizedData = personalizer.personalize(rawData, country)
            _uiState.value = CheckpointUiState.Intro(personalizedData)
        }
    }

    /** Старт первого раунда. */
    fun startSession() {
        val intro = _uiState.value as? CheckpointUiState.Intro ?: return
        val initial = CheckpointState(data = intro.data)
        roundStartMs = System.currentTimeMillis()
        _uiState.value = CheckpointUiState.Playing(initial)
        speakCurrentNpcLine(initial)
    }

    /** Юзер ответил. Engine считает, переходим дальше или финишируем. */
    fun submit(userAnswer: String) {
        val playing = _uiState.value as? CheckpointUiState.Playing ?: return
        val timeMs = System.currentTimeMillis() - roundStartMs
        val newState = engine.submitAnswer(playing.state, userAnswer, timeMs)

        if (newState.isFinished) {
            val outcome = newState.outcome
            if (outcome is com.spanishapp.domain.checkpoint.CheckpointOutcome.Pass) {
                // Начисляем XP только при первом прохождении (TODO: check DB)
                viewModelScope.launch {
                    xpTracker.add(outcome.xpAwarded)
                }
            }
            _uiState.value = CheckpointUiState.Finished(newState)
        } else {
            roundStartMs = System.currentTimeMillis()
            _uiState.value = CheckpointUiState.Playing(newState)
            speakCurrentNpcLine(newState)
        }
    }

    /** Повтор прослушивания реплики NPC (если есть). */
    fun replayNpcLine() {
        val playing = _uiState.value as? CheckpointUiState.Playing ?: return
        speakCurrentNpcLine(playing.state)
    }

    private fun speakCurrentNpcLine(state: CheckpointState) {
        val line = state.currentRound?.npcLineEs ?: return
        if (line.isBlank()) return
        runCatching { tts.speak(line) }
    }

    /**
     * Запустить STT для VOICE-раунда. По итогам:
     *  - Success → submit распознанного текста (engine применит fuzzy match)
     *  - Error (silence/no_match) → показать ошибку, дать переспросить
     *  - Cancelled → ничего
     */
    fun startVoiceCapture() {
        _voiceError.value = null
        _lastVoiceText.value = null
        viewModelScope.launch {
            when (val result = speechRecognizer.listenOnce("es-ES")) {
                is SpeechResult.Success -> {
                    _lastVoiceText.value = result.text
                    submit(result.text)
                }
                is SpeechResult.Error -> {
                    _voiceError.value = when {
                        result.isSilence -> "Не слышу. Нажми и говори громче."
                        else -> result.message
                    }
                }
                is SpeechResult.Cancelled -> Unit
            }
        }
    }

    fun clearVoiceState() {
        _voiceError.value = null
        _lastVoiceText.value = null
    }
}

/** Состояния UI чекпоинта. */
sealed class CheckpointUiState {
    object Loading : CheckpointUiState()
    data class Error(val message: String) : CheckpointUiState()
    data class Intro(val data: CheckpointData) : CheckpointUiState()
    data class Playing(val state: CheckpointState) : CheckpointUiState()
    data class Finished(val state: CheckpointState) : CheckpointUiState()
}
