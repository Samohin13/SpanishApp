package com.spanishapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.service.AchievementManager
import com.spanishapp.service.SpanishTts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class MathGameState(
    val expressionText: String = "",
    val correctAnswer: Int = 0,
    val timeLeft: Float = 1f,
    val score: Int = 0,
    val streak: Int = 0,
    val level: String = "A1",
    val currentRound: Int = 0,
    val totalRounds: Int = 10,
    val isGameOver: Boolean = false,
    val lastCorrect: Boolean? = null
)

@HiltViewModel
class MathViewModel @Inject constructor(
    private val userProgressDao: UserProgressDao,
    private val achievementManager: AchievementManager,
    private val tts: SpanishTts
) : ViewModel() {

    private val _state = MutableStateFlow(MathGameState())
    val state = _state.asStateFlow()

    private var timerJob: kotlinx.coroutines.Job? = null
    private var totalTime = 10f

    fun startGame(difficulty: String) {
        totalTime = when(difficulty) {
            "A1" -> 15f
            "A2-B1" -> 10f
            else -> 7f
        }
        _state.value = MathGameState(level = difficulty)
        nextQuestion()
    }

    private fun nextQuestion() {
        val s = _state.value
        if (s.currentRound >= s.totalRounds) {
            finishGame()
            return
        }

        val (expression, answer) = generateExpression(s.level)
        
        _state.value = s.copy(
            expressionText = expression,
            correctAnswer = answer,
            timeLeft = 1f,
            currentRound = s.currentRound + 1,
            lastCorrect = null
        )

        if (s.level == "B2-C1") {
            tts.speak(expression)
        }
        
        startTimer()
    }

    private fun generateExpression(difficulty: String): Pair<String, Int> {
        return when (difficulty) {
            "A1" -> {
                val a = Random.nextInt(1, 21)
                val b = Random.nextInt(1, 21)
                if (Random.nextBoolean()) {
                    "${NumberToSpanish.convert(a)} + ${NumberToSpanish.convert(b)}" to (a + b)
                } else {
                    val max = maxOf(a, b)
                    val min = minOf(a, b)
                    "${NumberToSpanish.convert(max)} - ${NumberToSpanish.convert(min)}" to (max - min)
                }
            }
            "A2-B1" -> {
                val a = Random.nextInt(2, 11)
                val b = Random.nextInt(2, 13)
                if (Random.nextBoolean()) {
                    "${NumberToSpanish.convert(a)} x ${NumberToSpanish.convert(b)}" to (a * b)
                } else {
                    val prod = a * b
                    "${NumberToSpanish.convert(prod)} / ${NumberToSpanish.convert(a)}" to b
                }
            }
            else -> { // B2-C1
                val type = Random.nextInt(3)
                when(type) {
                    0 -> {
                        val a = Random.nextInt(10, 101)
                        "La mitad de ${NumberToSpanish.convert(a * 2)}" to a
                    }
                    1 -> {
                        val a = Random.nextInt(5, 51)
                        "El doble de ${NumberToSpanish.convert(a)}" to (a * 2)
                    }
                    else -> {
                        val b = Random.nextInt(1, 31)
                        "El triple de ${NumberToSpanish.convert(b)} menos ${NumberToSpanish.convert(5)}" to (b * 3 - 5)
                    }
                }
            }
        }
    }

    fun repeatQuestion() {
        if (_state.value.level == "B2-C1") {
            tts.speak(_state.value.expressionText)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.timeLeft > 0) {
                delay(50)
                val newTime = (_state.value.timeLeft - (0.05f / totalTime)).coerceAtLeast(0f)
                _state.value = _state.value.copy(timeLeft = newTime)
            }
            onTimeOut()
        }
    }

    private fun onTimeOut() {
        submitAnswer(null)
    }

    fun submitAnswer(answer: Int?) {
        timerJob?.cancel()
        val isCorrect = answer == _state.value.correctAnswer
        val newStreak = if (isCorrect) _state.value.streak + 1 else 0
        val points = if (isCorrect) (10 * (1 + newStreak * 0.1f)).toInt() else 0
        
        _state.value = _state.value.copy(
            lastCorrect = isCorrect,
            score = _state.value.score + points,
            streak = newStreak
        )

        viewModelScope.launch {
            delay(1200)
            nextQuestion()
        }
    }

    private fun finishGame() {
        _state.value = _state.value.copy(isGameOver = true)
        val finalScore = _state.value.score
        viewModelScope.launch {
            val p = userProgressDao.getProgressOnce() ?: return@launch
            val xpGain = (finalScore / 5).coerceAtLeast(5)
            userProgressDao.update(p.copy(totalXp = p.totalXp + xpGain))
            achievementManager.checkAndUnlock()
        }
    }
}
