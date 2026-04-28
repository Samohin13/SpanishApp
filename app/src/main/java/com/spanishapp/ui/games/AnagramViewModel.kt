package com.spanishapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.service.AchievementManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnagramPremiumState(
    val originalWord: String = "",
    val translation: String = "",
    val hint: String = "",
    val shuffledLetters: List<String> = emptyList(),
    val assembledLetters: List<String?> = emptyList(),
    val score: Int = 0,
    val isCorrect: Boolean? = null,
    val isGameOver: Boolean = false,
    val cefr: String = "A1"
)

@HiltViewModel
class AnagramViewModel @Inject constructor(
    private val wordDao: WordDao,
    private val userProgressDao: UserProgressDao,
    private val achievementManager: AchievementManager
) : ViewModel() {

    private val _state = MutableStateFlow(AnagramPremiumState())
    val state = _state.asStateFlow()

    fun startGame(cefr: String) {
        _state.value = AnagramPremiumState(cefr = cefr)
        nextRound()
    }

    private fun nextRound() {
        viewModelScope.launch {
            val words = wordDao.getRandomWords(20).filter { it.level == _state.value.cefr }.ifEmpty { wordDao.getRandomWords(5) }
            val word = words.randomOrNull() ?: return@launch
            
            // Premium: Keep accents (tildes) but shuffle them as separate tiles
            val cleanWord = stripArticle(word.spanish).lowercase().trim()
            val letters = cleanWord.map { it.toString() }.shuffled()
            
            _state.value = _state.value.copy(
                originalWord = cleanWord,
                translation = word.russian,
                hint = word.example,
                shuffledLetters = letters,
                assembledLetters = List(cleanWord.length) { null },
                isCorrect = null
            )
        }
    }

    fun onLetterClick(letterIndex: Int) {
        val s = _state.value
        if (s.isCorrect == true) return
        
        val letter = s.shuffledLetters[letterIndex]
        if (letter.isEmpty()) return
        
        val nextSlot = s.assembledLetters.indexOfFirst { it == null }
        if (nextSlot != -1) {
            val newList = s.assembledLetters.toMutableList()
            newList[nextSlot] = letter
            
            val newShuffled = s.shuffledLetters.toMutableList()
            newShuffled[letterIndex] = ""
            
            _state.value = s.copy(
                assembledLetters = newList,
                shuffledLetters = newShuffled
            )
            
            if (newList.all { it != null }) {
                checkResult()
            }
        }
    }

    fun undo() {
        val s = _state.value
        val lastFilled = s.assembledLetters.indexOfLast { it != null }
        if (lastFilled != -1) {
            val letter = s.assembledLetters[lastFilled] ?: return
            val newList = s.assembledLetters.toMutableList()
            newList[lastFilled] = null
            
            val newShuffled = s.shuffledLetters.toMutableList()
            val emptySlot = newShuffled.indexOfFirst { it == "" }
            if (emptySlot != -1) newShuffled[emptySlot] = letter
            
            _state.value = s.copy(
                assembledLetters = newList,
                shuffledLetters = newShuffled,
                isCorrect = null
            )
        }
    }

    private fun checkResult() {
        val s = _state.value
        val assembled = s.assembledLetters.joinToString("")
        val isCorrect = assembled == s.originalWord
        
        _state.value = s.copy(isCorrect = isCorrect)
        
        if (isCorrect) {
            viewModelScope.launch {
                delay(1000)
                addXp(10)
                nextRound()
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
