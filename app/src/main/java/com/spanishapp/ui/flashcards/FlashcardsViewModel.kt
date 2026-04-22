package com.spanishapp.ui.flashcards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.entity.WordEntity
import com.spanishapp.domain.algorithm.ReviewButton
import com.spanishapp.domain.algorithm.SM2
import com.spanishapp.domain.algorithm.XpSystem
import com.spanishapp.service.SpanishTts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FlashcardDirection {
    ES_TO_RU,   // показываем испанский → вспоминаем русский
    RU_TO_ES,   // показываем русский → вспоминаем испанский
    MIXED       // случайно для каждой карточки
}

data class FlashcardsUiState(
    val isLoading: Boolean = true,
    val isFinished: Boolean = false,
    val cards: List<WordEntity> = emptyList(),
    val currentIndex: Int = 0,
    val showBack: Boolean = false,
    val currentDirection: FlashcardDirection = FlashcardDirection.ES_TO_RU,
    val level: String = "A1",
    val category: String = "all",
    val sessionSize: Int = 20,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val earnedXp: Int = 0,
    val error: String? = null
)

@HiltViewModel
class FlashcardsViewModel @Inject constructor(
    private val wordDao: WordDao,
    private val userProgressDao: UserProgressDao,
    private val tts: SpanishTts
) : ViewModel() {

    private val _state = MutableStateFlow(FlashcardsUiState())
    val state: StateFlow<FlashcardsUiState> = _state.asStateFlow()

    private var mode: FlashcardDirection = FlashcardDirection.ES_TO_RU

    fun startSession(
        level: String,
        category: String,
        direction: FlashcardDirection,
        onlyWeak: Boolean,
        sessionSize: Int = 20
    ) {
        mode = direction
        viewModelScope.launch {
            val cards = buildSessionDeck(level, category, onlyWeak, sessionSize)
            if (cards.isEmpty()) {
                _state.value = FlashcardsUiState(
                    isLoading = false,
                    isFinished = true,
                    level = level,
                    category = category,
                    error = "Нет слов для сессии. Попробуй другой уровень или категорию."
                )
                return@launch
            }
            _state.value = FlashcardsUiState(
                isLoading = false,
                cards = cards,
                currentIndex = 0,
                showBack = false,
                currentDirection = resolveDirection(direction),
                level = level,
                category = category,
                sessionSize = cards.size
            )
        }
    }

    private suspend fun buildSessionDeck(
        level: String,
        category: String,
        onlyWeak: Boolean,
        sessionSize: Int
    ): List<WordEntity> {
        if (onlyWeak) {
            return wordDao.getWeakForSession(category, sessionSize)
        }
        // Смесь: 70% повторение (due) + 30% новые. Если чего-то не хватает — добираем.
        val reviewBudget = (sessionSize * 0.7).toInt().coerceAtLeast(1)
        val newBudget = sessionSize - reviewBudget

        val due = wordDao.getDueForSession(level, category, reviewBudget)
        val fresh = wordDao.getNewForSession(level, category, newBudget + (reviewBudget - due.size))
        val missing = sessionSize - due.size - fresh.size
        val extra = if (missing > 0)
            wordDao.getDueForSession(level, category, missing + 5)
                .filter { it !in due } else emptyList()

        return (due + fresh + extra).take(sessionSize).shuffled()
    }

    fun flip() {
        _state.value = _state.value.copy(showBack = !_state.value.showBack)
    }

    fun speakCurrent(slow: Boolean = false) {
        val s = _state.value
        val word = s.cards.getOrNull(s.currentIndex) ?: return
        tts.speak(word.spanish, slow = slow)
    }

    fun speakExample() {
        val s = _state.value
        val word = s.cards.getOrNull(s.currentIndex) ?: return
        if (word.example.isNotBlank()) tts.speak(word.example)
    }

    fun answer(button: ReviewButton) {
        val s = _state.value
        val current = s.cards.getOrNull(s.currentIndex) ?: return

        val quality = SM2.qualityFromButton(button)
        val updated = SM2.review(current, quality)

        val xpDelta = when (button) {
            ReviewButton.HARD -> 0
            ReviewButton.GOOD -> XpSystem.WORD_CORRECT
            ReviewButton.EASY -> XpSystem.WORD_EASY
        }

        viewModelScope.launch { wordDao.update(updated) }

        val nextIdx = s.currentIndex + 1
        val finished = nextIdx >= s.cards.size

        _state.value = s.copy(
            currentIndex = nextIdx,
            showBack = false,
            currentDirection = if (!finished) resolveDirection(mode) else s.currentDirection,
            correctCount = s.correctCount + if (quality >= 3) 1 else 0,
            wrongCount = s.wrongCount + if (quality < 3) 1 else 0,
            earnedXp = s.earnedXp + xpDelta,
            isFinished = finished
        )

        if (finished) {
            viewModelScope.launch {
                val learnedDelta = _state.value.correctCount
                userProgressDao.addXpAndWords(_state.value.earnedXp, learnedDelta)
            }
        }
    }

    fun restart() {
        val s = _state.value
        startSession(
            level = s.level,
            category = s.category,
            direction = mode,
            onlyWeak = false,
            sessionSize = 20
        )
    }

    private fun resolveDirection(d: FlashcardDirection): FlashcardDirection = when (d) {
        FlashcardDirection.MIXED ->
            if ((0..1).random() == 0) FlashcardDirection.ES_TO_RU else FlashcardDirection.RU_TO_ES
        else -> d
    }
}
