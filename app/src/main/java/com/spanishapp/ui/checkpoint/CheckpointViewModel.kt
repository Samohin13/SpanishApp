package com.spanishapp.ui.checkpoint

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.prefs.CheckpointCooldownPrefs
import com.spanishapp.data.repository.LeaderboardRepository
import com.spanishapp.domain.algorithm.LeagueResolver
import com.spanishapp.domain.checkpoint.CheckpointData
import com.spanishapp.domain.checkpoint.CheckpointEngine
import com.spanishapp.domain.checkpoint.CheckpointPersonalizer
import com.spanishapp.domain.checkpoint.CheckpointRepository
import com.spanishapp.domain.checkpoint.CheckpointState
import com.spanishapp.domain.checkpoint.CountryMap
import com.spanishapp.service.CheckpointReminderWorker
import com.spanishapp.service.SpanishSpeechRecognizer
import com.spanishapp.service.SpanishTts
import com.spanishapp.service.SpeechResult
import com.spanishapp.service.XpTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    @ApplicationContext private val appContext: android.content.Context,
    private val repository: CheckpointRepository,
    private val engine: CheckpointEngine,
    private val personalizer: CheckpointPersonalizer,
    private val leaderboardRepository: LeaderboardRepository,
    private val tts: SpanishTts,
    private val xpTracker: XpTracker,
    private val speechRecognizer: SpanishSpeechRecognizer,
    private val cooldownPrefs: CheckpointCooldownPrefs,
    private val userProgressDao: UserProgressDao,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /** Текущий cpId — нужен чтобы отменять / планировать reminder. */
    private var currentCpId: String? = null

    /**
     * Cooldown (epoch ms когда можно снова попробовать) для текущего
     * чекпоинта. 0L = нет cooldown. Обновляется реактивно из DataStore.
     */
    private val _cooldownUntilMs = MutableStateFlow(0L)
    val cooldownUntilMs: StateFlow<Long> = _cooldownUntilMs.asStateFlow()

    /**
     * Использовал ли юзер бесплатную пересдачу для текущего CP.
     * v1.22.30: каждый CP даёт 1 бесплатную пересдачу — UI показывает
     * «Бесплатная пересдача» вместо «-50 рейтинга» / «24h ожидания»
     * пока этот флаг false.
     */
    private val _freeRetryUsed = MutableStateFlow(false)
    val freeRetryUsed: StateFlow<Boolean> = _freeRetryUsed.asStateFlow()

    /**
     * Текущий skill rating юзера — нужен чтобы решить, доступна ли кнопка
     * «Сейчас за -50 рейтинга» (требуется rating ≥ 50).
     */
    val currentRating: StateFlow<Int> = userProgressDao.getProgress()
        .map { it?.skillRating ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

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
            currentCpId = checkpointId
            // v1.22.20: юзер сам вернулся в чекпоинт → отменяем 24h-напоминание
            // (если оно было запланировано после прошлого fail). Не назойливо.
            runCatching { CheckpointReminderWorker.cancel(appContext, checkpointId) }
            // Подписываемся на cooldown DataStore — если ещё активен после
            // прошлого fail, UI покажет timer вместо «Попробовать снова».
            observeCooldown(checkpointId)

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
            val cpId = currentCpId
            when (outcome) {
                is com.spanishapp.domain.checkpoint.CheckpointOutcome.Pass -> {
                    // Начисляем XP только при первом прохождении (TODO: check DB)
                    viewModelScope.launch {
                        xpTracker.add(outcome.xpAwarded)
                    }
                    // v1.22.20: успешно сдал → отменяем любые pending reminder
                    // (вдруг был с прошлого fail и юзер сразу пересдал)
                    if (cpId != null) {
                        runCatching { CheckpointReminderWorker.cancel(appContext, cpId) }
                        // Pass снимает cooldown — следующий retry свободен.
                        viewModelScope.launch {
                            runCatching { cooldownPrefs.clearCooldown(cpId) }
                        }
                    }
                }
                is com.spanishapp.domain.checkpoint.CheckpointOutcome.Fail -> {
                    // v1.22.20: NPC «обиделся» — пришлёт пуш через 24 часа
                    // если юзер не вернётся сам. REPLACE policy: повторный
                    // fail перезапишет таймер на новый 24h-период.
                    if (cpId != null) {
                        runCatching { CheckpointReminderWorker.scheduleIn24h(appContext, cpId) }
                        // 24h cooldown в DataStore — UI блокирует retry-кнопку.
                        viewModelScope.launch {
                            runCatching {
                                cooldownPrefs.setCooldown(cpId, outcome.cooldownUntilMs)
                            }
                        }
                    }
                }
                null -> { /* should not happen — isFinished implies outcome */ }
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
        // v1.22.20: голос NPC — мужские/женские персонажи звучат разно.
        // Для LISTEN формата озвучка обязательна (это тест на аудирование).
        val npcVoice = com.spanishapp.domain.voice.NpcVoiceMap.voiceFor(state.data.npc.id)
        runCatching { tts.speak(line, esVoiceOverride = npcVoice) }
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

    /**
     * Подписка на per-checkpoint cooldown DataStore. Каждый emit
     * обновляет `_cooldownUntilMs`. Запускается один раз при load() —
     * последующие изменения (например, после fail) приходят автоматом.
     *
     * Один job на ViewModel — если юзер загружает другой cpId, мы просто
     * перезаписываем `currentCpId` и StateFlow начнёт ловить значения
     * другого ключа после следующего setCooldown / clearCooldown.
     */
    private var cooldownJob: kotlinx.coroutines.Job? = null
    private var freeRetryJob: kotlinx.coroutines.Job? = null
    private fun observeCooldown(cpId: String) {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            cooldownPrefs.cooldownFor(cpId).collect { until ->
                _cooldownUntilMs.value = until
            }
        }
        freeRetryJob?.cancel()
        freeRetryJob = viewModelScope.launch {
            cooldownPrefs.freeRetryUsed(cpId).collect { used ->
                _freeRetryUsed.value = used
            }
        }
    }

    /**
     * Использовать бесплатную пересдачу (доступна 1 раз на каждый CP).
     * Снимает cooldown + помечает что bonus использован → следующий fail
     * приведёт к стандартному 24h cooldown / -50 рейтинга.
     */
    suspend fun useFreeRetry(): Boolean {
        val cpId = currentCpId ?: return false
        cooldownPrefs.markFreeRetryUsed(cpId)
        cooldownPrefs.clearCooldown(cpId)
        runCatching { CheckpointReminderWorker.cancel(appContext, cpId) }
        return true
    }

    /**
     * Заплатить -50 рейтинга чтобы пропустить cooldown. Возвращает true
     * если оплата прошла (rating ≥ 50). UI после успеха должен вызвать
     * `load(cpId)` чтобы перезапустить чекпоинт.
     *
     * Используем прямое чтение/запись через DAO (не RatingUpdater) —
     * это не «ответ» в обучении, а штрафная транзакция. Не трогаем
     * daily cap / per-word cooldown / weekly leagues.
     */
    suspend fun payRatingCostForRetry(): Boolean {
        val cpId = currentCpId ?: return false
        val progress = userProgressDao.getProgressOnce() ?: return false
        if (progress.skillRating < RETRY_RATING_COST) return false

        val newRating = (progress.skillRating - RETRY_RATING_COST).coerceAtLeast(0)
        val newLeague = LeagueResolver.fromRating(newRating)
        userProgressDao.updateSkillRating(
            rating = newRating,
            league = newLeague.tier,
            ts = System.currentTimeMillis(),
        )
        cooldownPrefs.clearCooldown(cpId)
        runCatching { CheckpointReminderWorker.cancel(appContext, cpId) }
        return true
    }

    companion object {
        /** Цена пропуска cooldown в очках skill rating. */
        const val RETRY_RATING_COST = 50
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
