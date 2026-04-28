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

data class AnagramGameState(
    val originalWord: String = "",
    val translation: String = "",
    val shuffledLetters: List<String> = emptyList(),
    val assembledLetters: List<String?> = emptyList(),
    val score: Int = 0,
    val isCorrect: Boolean? = null,
    val isGameOver: Boolean = false
)

@HiltViewModel
class GamesViewModel @Inject constructor(
    private val wordDao: WordDao,
    private val userProgressDao: UserProgressDao,
    private val achievementManager: AchievementManager
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
        addXp(score * 2)
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
            addXp(newScore / 2)
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

    // ── Anagram Game ──────────────────────────────────────────
    private val _anagramState = MutableStateFlow(AnagramGameState())
    val anagramState = _anagramState.asStateFlow()

    fun startAnagramGame() {
        nextAnagramRound()
    }

    private fun nextAnagramRound() {
        viewModelScope.launch {
            val word = wordDao.getRandomWords(1).firstOrNull() ?: return@launch
            val cleanWord = stripArticle(word.spanish).lowercase().replace(" ", "")
            val letters = cleanWord.map { it.toString() }.shuffled()
            _anagramState.value = AnagramGameState(
                originalWord = cleanWord,
                translation = word.russian,
                shuffledLetters = letters,
                assembledLetters = List(cleanWord.length) { null },
                score = _anagramState.value.score
            )
        }
    }

    fun onLetterClick(letterIndex: Int) {
        val state = _anagramState.value
        val letter = state.shuffledLetters[letterIndex]
        if (letter.isEmpty()) return
        
        val nextSlot = state.assembledLetters.indexOfFirst { it == null }
        if (nextSlot != -1) {
            val newList = state.assembledLetters.toMutableList()
            newList[nextSlot] = letter
            
            val newShuffled = state.shuffledLetters.toMutableList()
            newShuffled[letterIndex] = "" // Mark as used
            
            _anagramState.value = state.copy(
                assembledLetters = newList.toList(),
                shuffledLetters = newShuffled.toList()
            )
            
            if (newList.all { it != null }) {
                checkAnagram()
            }
        }
    }

    fun clearAssembled() {
        val state = _anagramState.value
        val originalLetters = state.originalWord.map { it.toString() }.shuffled()
        _anagramState.value = state.copy(
            assembledLetters = List(state.originalWord.length) { null },
            shuffledLetters = originalLetters,
            isCorrect = null
        )
    }

    private fun checkAnagram() {
        val state = _anagramState.value
        val assembled = state.assembledLetters.joinToString("")
        val isCorrect = assembled == state.originalWord
        _anagramState.value = state.copy(isCorrect = isCorrect)
        
        if (isCorrect) {
            val newScore = state.score + 10
            _anagramState.value = _anagramState.value.copy(score = newScore)
            viewModelScope.launch {
                kotlinx.coroutines.delay(1000)
                nextAnagramRound()
                addXp(5)
            }
        }
    }

    private fun addXp(amount: Int) {
        viewModelScope.launch {
            val p = userProgressDao.getProgressOnce() ?: return@launch
            userProgressDao.update(p.copy(totalXp = p.totalXp + amount))
            achievementManager.checkAndUnlock()
        }
    }
}
