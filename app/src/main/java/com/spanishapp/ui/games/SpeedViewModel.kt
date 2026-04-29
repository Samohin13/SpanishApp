package com.spanishapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.entity.WordEntity
import com.spanishapp.service.AchievementManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SpeedLevel(val label: String, val baseTime: Float, val color: Long) {
    LENTO("Lento", 4.5f, 0xFF4CAF50),
    FLUIDO("Fluido", 2.2f, 0xFF2196F3),
    RAYO("Rayo", 1.0f, 0xFFF44336)
}

data class SpeedPremiumState(
    val currentWord: WordEntity? = null,
    val options: List<String> = emptyList(),
    val level: SpeedLevel = SpeedLevel.LENTO,
    val cefr: String = "A1",
    val timeLeft: Float = 1f,
    val score: Int = 0,
    val streak: Int = 0,
    val multiplier: Float = 1.0f,
    val currentRound: Int = 0,
    val totalRounds: Int = 15,
    val isGameOver: Boolean = false,
    val lastCorrect: Boolean? = null,
    val reactionTimes: MutableList<Long> = mutableListOf(),
    val weakWords: MutableList<WordEntity> = mutableListOf()
)

@HiltViewModel
class SpeedViewModel @Inject constructor(
    private val wordDao: WordDao,
    private val userProgressDao: UserProgressDao,
    private val achievementManager: AchievementManager
) : ViewModel() {

    private val _state = MutableStateFlow(SpeedPremiumState())
    val state = _state.asStateFlow()

    private var timerJob: Job? = null
    private var roundStartTime = 0L
    private var dynamicTimeFactor = 1.0f

    fun startGame(speed: SpeedLevel, cefr: String) {
        timerJob?.cancel()
        dynamicTimeFactor = 1.0f
        _state.value = SpeedPremiumState(level = speed, cefr = cefr)
        nextRound()
    }

    private fun nextRound() {
        val s = _state.value
        if (s.currentRound >= s.totalRounds) {
            finishGame()
            return
        }

        viewModelScope.launch {
            // Fetch words based on CEFR
            val words = wordDao.getRandomWords(20).filter { it.level == s.cefr }.ifEmpty { wordDao.getRandomWords(10) }
            if (words.size >= 4) {
                val correct = words.random()
                val options = (words.filter { it.id != correct.id }.shuffled().take(3) + correct).map { it.russian }.shuffled()
                
                _state.value = s.copy(
                    currentWord = correct,
                    options = options,
                    currentRound = s.currentRound + 1,
                    timeLeft = 1f,
                    lastCorrect = null
                )
                roundStartTime = System.currentTimeMillis()
                startTimer()
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        val base = _state.value.level.baseTime * dynamicTimeFactor
        timerJob = viewModelScope.launch {
            val step = 0.05f
            while (_state.value.timeLeft > 0) {
                delay(50)
                val newTime = (_state.value.timeLeft - (step / base)).coerceAtLeast(0f)
                _state.value = _state.value.copy(timeLeft = newTime)
            }
            submitAnswer("") // Timeout
        }
    }

    fun submitAnswer(answer: String) {
        timerJob?.cancel()
        val s = _state.value
        val correctTranslation = s.currentWord?.russian ?: ""
        val isCorrect = answer == correctTranslation
        
        val reactionTime = System.currentTimeMillis() - roundStartTime
        if (isCorrect) s.reactionTimes.add(reactionTime)
        else s.currentWord?.let { s.weakWords.add(it) }

        val newStreak = if (isCorrect) s.streak + 1 else 0
        // Dynamic acceleration: -10% every 10 correct
        if (isCorrect && newStreak > 0 && newStreak % 10 == 0) {
            dynamicTimeFactor *= 0.9f
        }

        val newMultiplier = 1.0f + (newStreak / 5) * 0.2f
        val points = if (isCorrect) (10 * newMultiplier).toInt() else 0

        _state.value = s.copy(
            score = s.score + points,
            streak = newStreak,
            multiplier = newMultiplier,
            lastCorrect = isCorrect
        )

        viewModelScope.launch {
            delay(if (isCorrect) 600 else 1200)
            nextRound()
        }
    }

    private fun finishGame() {
        val s = _state.value
        _state.value = s.copy(isGameOver = true)
        viewModelScope.launch {
            val p = userProgressDao.getProgressOnce() ?: return@launch
            userProgressDao.update(p.copy(totalXp = p.totalXp + s.score / 2))
            achievementManager.checkAndUnlock()
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
