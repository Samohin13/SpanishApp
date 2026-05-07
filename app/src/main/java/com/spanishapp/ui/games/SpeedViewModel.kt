package com.spanishapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.entity.WordEntity
import com.spanishapp.domain.games.GameId
import com.spanishapp.domain.games.GameLevelManager
import com.spanishapp.domain.games.LevelDifficulty
import com.spanishapp.domain.games.LevelParams
import com.spanishapp.domain.algorithm.RatingUpdater
import com.spanishapp.service.AchievementManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SpeedPremiumState(
    val params: LevelParams = LevelDifficulty.forLevel(1),
    val currentWord: WordEntity? = null,
    val options: List<String> = emptyList(),
    val timeLeft: Float = 1f,
    val score: Int = 0,
    val correctCount: Int = 0,
    val streak: Int = 0,
    val multiplier: Float = 1.0f,
    val currentRound: Int = 0,
    val isGameOver: Boolean = false,
    val lastCorrect: Boolean? = null,
    val reactionTimes: MutableList<Long> = mutableListOf(),
    val weakWords: MutableList<WordEntity> = mutableListOf(),
    val finalStars: Int = 0,
    val finalPercent: Int = 0,
    val showLevelMap: Boolean = true
) {
    val totalRounds: Int get() = params.rounds
    val level: Int get() = params.level
}

@HiltViewModel
class SpeedViewModel @Inject constructor(
    private val wordDao: WordDao,
    private val userProgressDao: UserProgressDao,
    private val achievementManager: AchievementManager,
    private val ratingUpdater: RatingUpdater,
    val levelManager: GameLevelManager
) : ViewModel() {

    private val _state = MutableStateFlow(SpeedPremiumState())
    val state = _state.asStateFlow()

    private var timerJob: Job? = null
    private var roundStartTime = 0L
    @Volatile private var roundResolved = false

    fun startLevel(level: Int) {
        timerJob?.cancel()
        roundResolved = false
        val params = LevelDifficulty.forLevel(level)
        _state.value = SpeedPremiumState(params = params, showLevelMap = false)
        nextRound()
    }

    fun openLevelMap() {
        timerJob?.cancel()
        _state.value = _state.value.copy(showLevelMap = true, isGameOver = false)
    }

    private fun nextRound() {
        val s = _state.value
        if (s.currentRound >= s.totalRounds) {
            finishGame()
            return
        }

        viewModelScope.launch {
            // Берём 80 случайных слов и фильтруем по нужному CEFR-слою.
            val pool = wordDao.getRandomWords(80)
            val cefrPool = pool.filter {
                it.level in s.params.cefr && it.russian.isNotBlank() && it.spanish.isNotBlank()
            }
            // Фолбэк на остальной словарь, если в CEFR-слое мало.
            val candidates = if (cefrPool.size >= 4) cefrPool
                             else (cefrPool + pool.filter { it.russian.isNotBlank() })
                                  .distinctBy { it.id }
                                  .take(20)
            if (candidates.size < 4) {
                _state.value = s.copy(currentRound = s.currentRound + 1)
                nextRound()
                return@launch
            }

            // Выбираем правильное слово, потом 3 ОТВЛЕЧЕНИЯ С УНИКАЛЬНЫМ переводом.
            val correct = candidates.random()
            val distractors = candidates
                .filter { it.id != correct.id && it.russian != correct.russian }
                .distinctBy { it.russian }
                .shuffled()
                .take(3)
            if (distractors.size < 3) {
                // Не нашлось 3 уникальных переводов — пропускаем этот раунд.
                _state.value = s.copy(currentRound = s.currentRound + 1)
                nextRound()
                return@launch
            }
            val options = (distractors + correct).map { it.russian }.shuffled()

            roundResolved = false
            _state.value = s.copy(
                currentWord  = correct,
                options      = options,
                currentRound = s.currentRound + 1,
                timeLeft     = 1f,
                lastCorrect  = null
            )
            roundStartTime = System.currentTimeMillis()
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        val baseSec = _state.value.params.timePerRoundSec
        if (baseSec <= 0f) return   // уровень без таймера (1-10)
        timerJob = viewModelScope.launch {
            val step = 0.05f
            while (_state.value.timeLeft > 0 && !roundResolved) {
                delay(50)
                val newTime = (_state.value.timeLeft - (step / baseSec)).coerceAtLeast(0f)
                _state.value = _state.value.copy(timeLeft = newTime)
            }
            if (!roundResolved) submitAnswer("")   // тайм-аут
        }
    }

    fun submitAnswer(answer: String) {
        if (roundResolved) return
        roundResolved = true
        timerJob?.cancel()
        val s = _state.value
        val correctTranslation = s.currentWord?.russian ?: ""
        val isCorrect = answer == correctTranslation

        val reactionTime = System.currentTimeMillis() - roundStartTime
        if (isCorrect) s.reactionTimes.add(reactionTime)
        else s.currentWord?.let { s.weakWords.add(it) }

        val newStreak = if (isCorrect) s.streak + 1 else 0
        val newMultiplier = 1.0f + (newStreak / 5) * 0.2f
        val points = if (isCorrect) (10 * newMultiplier).toInt() else 0

        _state.value = s.copy(
            score        = s.score + points,
            correctCount = if (isCorrect) s.correctCount + 1 else s.correctCount,
            streak       = newStreak,
            multiplier   = newMultiplier,
            lastCorrect  = isCorrect
        )

        viewModelScope.launch {
            delay(if (isCorrect) 600 else 1200)
            nextRound()
        }
    }

    private fun finishGame() {
        val s = _state.value
        val percent = if (s.totalRounds > 0) (s.correctCount * 100) / s.totalRounds else 0

        viewModelScope.launch {
            val stars = levelManager.completeLevel(GameId.SPEED, s.level, percent)

            val p = userProgressDao.getProgressOnce()
            if (p != null) {
                userProgressDao.update(p.copy(totalXp = p.totalXp + s.score / 2))
                achievementManager.checkAndUnlock()
            }

            _state.value = s.copy(
                isGameOver   = true,
                finalStars   = stars,
                finalPercent = percent
            )
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
