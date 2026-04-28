package com.spanishapp.ui.games

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.entity.WordEntity
import com.spanishapp.service.AchievementManager
import com.spanishapp.service.SpanishTts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

enum class PalabraLevel(val title: String, val tag: String, val desc: String) {
    A1("Básico", "A1", "3–5 букв, простые слоги"),
    A2("Intermedio", "A2", "6–8 букв, B/V, H"),
    B1("Acentuación", "B1", "5–9 букв, ударения (á, é...)"),
    B2("Avanzado", "B2", "9–12 букв, CC, NN, LL"),
    C1("Maestría", "C1", "12+ букв, термины")
}

data class LetterItem(
    val id: String = UUID.randomUUID().toString(),
    val char: String,
    val isUsed: Boolean = false
)

data class PalabraQuestion(
    val word: WordEntity,
    val targetWord: String,
    val shuffledLetters: List<LetterItem>,
    val assembledLetters: List<LetterItem?> = emptyList(),
    val isChecked: Boolean = false,
    val isCorrect: Boolean? = null,
    val mistakesCount: Int = 0,
    val startTime: Long = 0,
    val endTime: Long = 0
)

data class PalabraState(
    val selectedLevel: PalabraLevel = PalabraLevel.A1,
    val questions: List<PalabraQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val score: Int = 0,
    val showSetup: Boolean = true,
    val isGameOver: Boolean = false,
    val precisionCount: Int = 0, // Words completed without errors
    val translationHintVisible: Boolean = false,
    val ruleHint: String? = null
)

@HiltViewModel
class PalabraMaestraViewModel @Inject constructor(
    private val wordDao: WordDao,
    private val userProgressDao: UserProgressDao,
    private val achievementManager: AchievementManager,
    private val tts: SpanishTts
) : ViewModel() {

    private val _state = MutableStateFlow(PalabraState())
    val state = _state.asStateFlow()

    fun setLevel(level: PalabraLevel) {
        _state.value = _state.value.copy(selectedLevel = level)
    }

    fun startGame() {
        viewModelScope.launch {
            val level = _state.value.selectedLevel
            val allWords = withContext(Dispatchers.IO) {
                wordDao.getWordsByLevelSync(level.tag)
            }
            
            val filtered = when(level) {
                PalabraLevel.A1 -> allWords.filter { stripArticle(it.spanish).length in 3..5 }
                PalabraLevel.A2 -> allWords.filter { stripArticle(it.spanish).length in 6..8 }
                PalabraLevel.B1 -> allWords.filter { w -> 
                    val clean = stripArticle(w.spanish)
                    clean.length in 5..9 && hasAccent(clean)
                }
                PalabraLevel.B2 -> allWords.filter { stripArticle(it.spanish).length in 9..12 }
                PalabraLevel.C1 -> allWords.filter { stripArticle(it.spanish).length >= 12 }
            }.shuffled().take(10)

            val questions = filtered.map { word ->
                val cleanWord = stripArticle(word.spanish).lowercase()
                val chars = cleanWord.map { it.toString() }
                val shuffled = shuffleLetters(chars)
                PalabraQuestion(
                    word = word,
                    targetWord = cleanWord,
                    shuffledLetters = shuffled,
                    assembledLetters = List(chars.size) { null },
                    startTime = System.currentTimeMillis()
                )
            }

            if (questions.isNotEmpty()) {
                _state.value = _state.value.copy(
                    questions = questions,
                    currentIndex = 0,
                    score = 0,
                    showSetup = false,
                    isGameOver = false,
                    precisionCount = 0
                )
            }
        }
    }

    private fun hasAccent(s: String): Boolean {
        val accents = listOf('á', 'é', 'í', 'ó', 'ú', 'ñ', 'ü')
        return s.lowercase().any { it in accents }
    }

    private fun shuffleLetters(chars: List<String>): List<LetterItem> {
        var shuffled = chars.shuffled()
        while (shuffled.joinToString("") == chars.joinToString("") && chars.size > 1) {
            shuffled = chars.shuffled()
        }
        return shuffled.map { LetterItem(char = it) }
    }

    fun onLetterClick(letter: LetterItem) {
        val s = _state.value
        val q = s.questions.getOrNull(s.currentIndex) ?: return
        if (q.isChecked) return

        val emptyIndex = q.assembledLetters.indexOfFirst { it == null }
        if (emptyIndex == -1) return

        // Add to assembled
        val newAssembled = q.assembledLetters.toMutableList()
        newAssembled[emptyIndex] = letter
        
        val newShuffled = q.shuffledLetters.map { 
            if (it.id == letter.id) it.copy(isUsed = true) else it 
        }

        var updatedQ = q.copy(
            assembledLetters = newAssembled,
            shuffledLetters = newShuffled
        )

        val isAutoValidate = s.selectedLevel == PalabraLevel.A1 || s.selectedLevel == PalabraLevel.A2

        if (isAutoValidate) {
            val expectedChar = q.targetWord[emptyIndex].toString().lowercase()
            if (letter.char.lowercase() != expectedChar) {
                updatedQ = updatedQ.copy(mistakesCount = updatedQ.mistakesCount + 1)
            }
            
            // Auto-finish only if word is complete and correct
            if (newAssembled.all { it != null }) {
                val assembledStr = newAssembled.joinToString("") { it?.char ?: "" }
                if (assembledStr.lowercase() == q.targetWord.lowercase()) {
                    updatedQ = updatedQ.copy(
                        isChecked = true,
                        isCorrect = true,
                        endTime = System.currentTimeMillis()
                    )
                    _state.value = s.copy(
                        score = s.score + calculateScore(updatedQ),
                        precisionCount = s.precisionCount + (if (updatedQ.mistakesCount == 0) 1 else 0)
                    )
                    tts.speak(q.word.spanish)
                }
            }
        }

        updateCurrentQuestion(updatedQ)
    }

    fun removeLetter(index: Int) {
        val s = _state.value
        val q = s.questions.getOrNull(s.currentIndex) ?: return
        if (q.isChecked) return
        
        val letter = q.assembledLetters[index] ?: return
        
        val newAssembled = q.assembledLetters.toMutableList()
        newAssembled[index] = null
        
        val newShuffled = q.shuffledLetters.map { 
            if (it.id == letter.id) it.copy(isUsed = false) else it 
        }
        
        updateCurrentQuestion(q.copy(assembledLetters = newAssembled, shuffledLetters = newShuffled))
    }

    fun checkWord() {
        val s = _state.value
        val q = s.questions.getOrNull(s.currentIndex) ?: return
        if (q.assembledLetters.any { it == null }) return

        val assembledStr = q.assembledLetters.joinToString("") { it?.char ?: "" }
        val isCorrect = assembledStr == q.targetWord
        
        val updatedQ = q.copy(
            isChecked = true,
            isCorrect = isCorrect,
            endTime = System.currentTimeMillis()
        )

        _state.value = s.copy(
            score = s.score + (if (isCorrect) calculateScore(updatedQ) else 0),
            precisionCount = s.precisionCount + (if (isCorrect && updatedQ.mistakesCount == 0) 1 else 0)
        )
        
        if (isCorrect) tts.speak(q.word.spanish)
        updateCurrentQuestion(updatedQ)
    }

    private fun calculateScore(q: PalabraQuestion): Int {
        val durationSec = (q.endTime - q.startTime) / 1000
        val base = 10
        val multiplier = when {
            durationSec < 5 -> 3.0
            durationSec < 15 -> 1.5
            else -> 1.0
        }
        return (base * multiplier).toInt()
    }

    private fun updateCurrentQuestion(q: PalabraQuestion) {
        val updatedList = _state.value.questions.toMutableList()
        updatedList[_state.value.currentIndex] = q
        _state.value = _state.value.copy(questions = updatedList)
    }

    fun nextQuestion() {
        val s = _state.value
        if (s.currentIndex + 1 < s.questions.size) {
            _state.value = s.copy(
                currentIndex = s.currentIndex + 1,
                translationHintVisible = false,
                ruleHint = null
            )
        } else {
            finishGame()
        }
    }

    private fun finishGame() {
        _state.value = _state.value.copy(isGameOver = true)
        viewModelScope.launch {
            val p = userProgressDao.getProgressOnce() ?: return@launch
            userProgressDao.update(p.copy(totalXp = p.totalXp + _state.value.score))
            achievementManager.checkAndUnlock()
        }
    }

    // --- Hints ---

    fun showTranslation() {
        _state.value = _state.value.copy(translationHintVisible = true)
    }

    fun playAudio() {
        val q = _state.value.questions.getOrNull(_state.value.currentIndex) ?: return
        tts.speak(q.word.spanish)
    }

    fun useFirstLetterHint() {
        val s = _state.value
        val q = s.questions.getOrNull(s.currentIndex) ?: return
        if (q.isChecked) return
        
        val firstChar = q.targetWord[0].toString().lowercase()
        // Find this letter in shuffled (not used)
        val letterItem = q.shuffledLetters.find { it.char == firstChar && !it.isUsed } ?: return
        
        // If something else is in the first slot, remove it
        if (q.assembledLetters[0] != null) {
            removeLetter(0)
        }
        
        // Add it to first slot
        val newAssembled = _state.value.questions[s.currentIndex].assembledLetters.toMutableList()
        newAssembled[0] = letterItem
        
        val newShuffled = _state.value.questions[s.currentIndex].shuffledLetters.map { 
            if (it.id == letterItem.id) it.copy(isUsed = true) else it 
        }
        
        updateCurrentQuestion(_state.value.questions[s.currentIndex].copy(
            assembledLetters = newAssembled,
            shuffledLetters = newShuffled
        ))
    }

    fun showRuleHint() {
        val q = _state.value.questions.getOrNull(_state.value.currentIndex) ?: return
        val word = q.targetWord
        val rule = when {
            word.contains('h') -> "Буква 'H' в испанском всегда немая (не произносится)."
            word.contains('ñ') -> "Буква 'Ñ' читается как мягкое 'нь' (как в слове каньон)."
            word.contains('á') || word.contains('é') || word.contains('í') || word.contains('ó') || word.contains('ú') -> 
                "Графическое ударение (tilde) ставится для выделения ударного слога вопреки общим правилам."
            word.contains("ll") -> "Двойная 'LL' во многих диалектах читается как 'й'."
            else -> "Внимательно следите за порядком букв!"
        }
        _state.value = _state.value.copy(ruleHint = rule)
    }

    fun reset() {
        _state.value = PalabraState()
    }
}
