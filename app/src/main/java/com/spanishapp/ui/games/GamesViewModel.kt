package com.spanishapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.entity.WordEntity
import com.spanishapp.service.AchievementManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArticlesGameState(
    val currentWord: WordEntity? = null,
    val options: List<String> = listOf("el", "la"),
    val score: Int = 0,
    val totalRounds: Int = 10,
    val currentRound: Int = 0,
    val isGameOver: Boolean = false,
    val lastCorrect: Boolean? = null
)

data class SpeedGameState(
    val currentWord: WordEntity? = null,
    val options: List<String> = emptyList(),
    val correctTranslation: String = "",
    val timeLeft: Float = 1f,
    val score: Int = 0,
    val currentRound: Int = 0,
    val isGameOver: Boolean = false
)

@HiltViewModel
class GamesViewModel @Inject constructor(
    private val wordDao: WordDao,
    private val userProgressDao: UserProgressDao,
    private val achievementManager: AchievementManager,
    private val xpTracker: com.spanishapp.service.XpTracker
) : ViewModel() {

    // ── Articles Game ─────────────────────────────────────────
    private val _articlesState = MutableStateFlow(ArticlesGameState())
    val articlesState = _articlesState.asStateFlow()

    fun startArticlesGame() {
        _articlesState.value = ArticlesGameState()
        nextArticlesRound()
    }

    fun submitArticle(selected: String) {
        val state = _articlesState.value
        val correct = getCorrectArticle(state.currentWord?.spanish ?: "").ifBlank { 
            guessArticle(stripArticle(state.currentWord?.spanish ?: "")) 
        }
        
        val isCorrect = selected == correct
        val newScore = if (isCorrect) state.score + 1 else state.score
        
        _articlesState.value = state.copy(
            lastCorrect = isCorrect,
            score = newScore
        )
        
        viewModelScope.launch {
            kotlinx.coroutines.delay(800)
            if (state.currentRound + 1 >= state.totalRounds) {
                finishArticlesGame(newScore)
            } else {
                nextArticlesRound()
            }
        }
    }

    private fun nextArticlesRound() {
        viewModelScope.launch {
            val words = wordDao.getRandomWords(50).filter { 
                val art = getCorrectArticle(it.spanish)
                art == "el" || art == "la"
            }
            if (words.isNotEmpty()) {
                val word = words.random()
                _articlesState.value = _articlesState.value.copy(
                    currentWord = word,
                    currentRound = _articlesState.value.currentRound + 1,
                    lastCorrect = null
                )
            }
        }
    }

    private fun finishArticlesGame(score: Int) {
        _articlesState.value = _articlesState.value.copy(isGameOver = true)
        // v1.22.16: единая формула XP за игровой уровень (раньше score*2).
        val total = _articlesState.value.currentRound.coerceAtLeast(1)
        val correctOut = (score.toFloat() / total).coerceIn(0f, 1f)
        addXp(com.spanishapp.domain.algorithm.XpSystem.gameLevelXp(correctOut, total))
    }

    // ── Speed Game ────────────────────────────────────────────
    private val _speedState = MutableStateFlow(SpeedGameState())
    val speedState = _speedState.asStateFlow()

    fun startSpeedGame() {
        _speedState.value = SpeedGameState()
        nextSpeedRound()
    }

    fun submitSpeedAnswer(answer: String) {
        val state = _speedState.value
        val isCorrect = answer == state.correctTranslation
        val newScore = if (isCorrect) state.score + (state.timeLeft * 10).toInt().coerceAtLeast(5) else state.score
        
        if (state.currentRound >= 10) {
            _speedState.value = state.copy(score = newScore, isGameOver = true)
            // v1.22.16: единая формула (раньше newScore/2). 10 раундов,
            // считаем долю правильных по score.
            val correctOut = (newScore.toFloat() / (10 * 30)).coerceIn(0f, 1f) // ~30 - средний reward
            addXp(com.spanishapp.domain.algorithm.XpSystem.gameLevelXp(correctOut, 10))
        } else {
            _speedState.value = state.copy(score = newScore)
            nextSpeedRound()
        }
    }

    private fun nextSpeedRound() {
        viewModelScope.launch {
            val words = wordDao.getRandomWords(4)
            if (words.size >= 4) {
                val correctWord = words.random()
                _speedState.value = _speedState.value.copy(
                    currentWord = correctWord,
                    correctTranslation = correctWord.russian,
                    options = words.map { it.russian }.shuffled(),
                    currentRound = _speedState.value.currentRound + 1,
                    timeLeft = 1f
                )
            }
        }
    }

    fun updateSpeedTimer(delta: Float) {
        val state = _speedState.value
        if (state.isGameOver) return
        val newTime = (state.timeLeft - delta).coerceAtLeast(0f)
        if (newTime <= 0f) {
            submitSpeedAnswer("") // Timeout
        } else {
            _speedState.value = state.copy(timeLeft = newTime)
        }
    }

    private fun addXp(amount: Int) {
        viewModelScope.launch {
            xpTracker.add(xp = amount, words = 0)
            achievementManager.checkAndUnlock()
        }
    }
}
