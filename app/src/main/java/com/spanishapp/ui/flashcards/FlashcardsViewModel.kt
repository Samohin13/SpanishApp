package com.spanishapp.ui.flashcards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.entity.WordEntity
import com.spanishapp.domain.algorithm.LeaguePromotion
import com.spanishapp.domain.algorithm.RatingUpdater
import com.spanishapp.domain.algorithm.ReviewButton
import com.spanishapp.domain.algorithm.SM2
import com.spanishapp.domain.algorithm.XpSystem
import com.spanishapp.service.SpanishTts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    val error: String? = null,
    /** Words answered HARD — shown in post-session review. */
    val wrongWords: List<WordEntity> = emptyList(),
    /** True immediately after an answer — shows the undo chip. */
    val canUndo: Boolean = false,
)

@HiltViewModel
class FlashcardsViewModel @Inject constructor(
    private val wordDao: WordDao,
    private val userProgressDao: UserProgressDao,
    private val tts: SpanishTts,
    private val ratingUpdater: RatingUpdater,
    private val xpTracker: com.spanishapp.service.XpTracker,
    private val setProgressDao: com.spanishapp.data.db.dao.FlashcardSetProgressDao,
    private val hintBank: com.spanishapp.service.HintBankManager,
) : ViewModel() {

    /** Set ID being practiced this session (null = legacy category mode). */
    private var activeSetId: String? = null

    /**
     * ID of the next set in the level chain (order + 1), or null if this is
     * the last set or session isn't a set-session. Used to show
     * «Следующий сет» button on completion screen.
     */
    val nextSetId: String?
        get() {
            val current = activeSetId?.let { FlashcardSetData.byId(it) } ?: return null
            return FlashcardSetData.byLevel(current.level)
                .firstOrNull { it.order == current.order + 1 }
                ?.id
        }

    /** Switch this VM to the next set in the chain, without reconstructing. */
    fun startNextSet() {
        val next = nextSetId ?: return
        startSetSession(next, mode)
    }

    private val _state = MutableStateFlow(FlashcardsUiState())
    val state: StateFlow<FlashcardsUiState> = _state.asStateFlow()

    /** Snapshot saved before each answer — allows one-level undo. */
    private var undoSnapshot: Pair<FlashcardsUiState, WordEntity>? = null

    /** Words already re-queued this session — each word returns at most once. */
    private val requeuedIds = mutableSetOf<Int>()

    /**
     * v1.25.98 FIX (audit xp-H3): слова, у которых isLearned перевернулся
     * false→true В ЭТОЙ сессии. Раньше в words_learned шёл correctCount —
     * ежедневный повтор одних и тех же 20 знакомых слов давал «+20 выученных
     * слов в день», и ачивка words_1000 («уровень носителя») фармилась за
     * недели без реального обучения. Настоящий сигнал — SM-2 переход
     * repetitions ≥ 3.
     */
    private val newlyLearnedIds = mutableSetOf<Int>()

    private val _leaguePromotions = MutableSharedFlow<LeaguePromotion>(replay = 0, extraBufferCapacity = 1)
    val leaguePromotions: SharedFlow<LeaguePromotion> = _leaguePromotions.asSharedFlow()

    private var mode: FlashcardDirection = FlashcardDirection.ES_TO_RU

    fun startSession(
        level: String,
        category: String,
        direction: FlashcardDirection,
        sessionSize: Int = 20,
        weakOnly: Boolean = false,
    ) {
        mode = direction
        activeSetId = null
        // v1.25.98: per-session счётчики живут в VM — чистим на новой сессии
        // (restart() переиспользует тот же VM instance).
        requeuedIds.clear()
        newlyLearnedIds.clear()
        viewModelScope.launch {
            val cards = if (weakOnly) {
                // Pulls accuracy < 60% words from any level/category.
                wordDao.getAllWeak(sessionSize)
            } else {
                buildSessionDeck(level, category, sessionSize)
            }
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

    /**
     * Start a session for a Daily Set: deck = exactly the words listed in the
     * set's [FlashcardSet.wordsSpanish], in shuffled order, no SM-2 mixing.
     * After the session ends, [maybeSaveSetCompletion] persists stars + best %.
     */
    fun startSetSession(setId: String, direction: FlashcardDirection) {
        mode = direction
        activeSetId = setId
        requeuedIds.clear()
        newlyLearnedIds.clear()
        viewModelScope.launch {
            val set = FlashcardSetData.byId(setId)
            if (set == null) {
                _state.value = FlashcardsUiState(
                    isLoading = false,
                    isFinished = true,
                    level = "A1",
                    category = "set",
                    error = "Сет не найден"
                )
                return@launch
            }
            val cards = wordDao.findBySpanishMany(
                set.wordsSpanish.map { it.lowercase().trim() }
            ).shuffled()
            if (cards.isEmpty()) {
                _state.value = FlashcardsUiState(
                    isLoading = false,
                    isFinished = true,
                    level = set.level,
                    category = "set",
                    error = "В этом сете пока нет слов в словаре"
                )
                return@launch
            }
            _state.value = FlashcardsUiState(
                isLoading = false,
                cards = cards,
                currentIndex = 0,
                showBack = false,
                currentDirection = resolveDirection(direction),
                level = set.level,
                category = "set",
                sessionSize = cards.size
            )
        }
    }

    private suspend fun buildSessionDeck(
        level: String,
        category: String,
        sessionSize: Int
    ): List<WordEntity> {
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

        // Save snapshot for undo BEFORE any state change.
        undoSnapshot = Pair(s, current)

        val quality = SM2.qualityFromButton(button)
        val updated = SM2.review(current, quality)

        // v1.25.98 (audit xp-H3): фиксируем реальный переход «выучено».
        if (!current.isLearned && updated.isLearned) {
            newlyLearnedIds.add(current.id)
        }

        val xpDelta = when (button) {
            ReviewButton.HARD -> 0
            ReviewButton.GOOD -> XpSystem.WORD_CORRECT
            ReviewButton.EASY -> XpSystem.WORD_CORRECT
        }

        viewModelScope.launch {
            wordDao.update(updated)
            val promotion = ratingUpdater.applyAnswer(
                easeFactor = current.easeFactor,
                quality = quality,
                wordId = current.id
            )
            if (promotion != null) _leaguePromotions.tryEmit(promotion)
        }

        // Capture deck size BEFORE requeue — used below for set-completion %.
        // Using _state.value.cards.size after the update is wrong because requeued
        // HARD cards inflate the total, making accuracy look artificially low.
        val deckSizeBeforeRequeue = s.cards.size

        // Re-queue HARD words 3 positions ahead (each word re-inserted at most once).
        val updatedCards = if (quality < 3 && current.id !in requeuedIds) {
            requeuedIds.add(current.id)
            val mutable = s.cards.toMutableList()
            val insertAt = (s.currentIndex + 3).coerceAtMost(mutable.size)
            mutable.add(insertAt, current)
            mutable
        } else s.cards

        val nextIdx = s.currentIndex + 1
        val finished = nextIdx >= updatedCards.size

        // Track wrong words for post-session review (deduplicated).
        val newWrongWords = if (quality < 3 && s.wrongWords.none { it.id == current.id })
            s.wrongWords + current else s.wrongWords

        _state.value = s.copy(
            cards = updatedCards,
            currentIndex = nextIdx,
            showBack = false,
            sessionSize = updatedCards.size,
            currentDirection = if (!finished) resolveDirection(mode) else s.currentDirection,
            correctCount = s.correctCount + if (quality >= 3) 1 else 0,
            wrongCount = s.wrongCount + if (quality < 3) 1 else 0,
            earnedXp = s.earnedXp + xpDelta,
            wrongWords = newWrongWords,
            canUndo = !finished,   // no undo on the completion screen
            isFinished = finished
        )

        if (finished) {
            viewModelScope.launch {
                // v1.25.98 FIX (audit xp-H3): «выучено» = переход isLearned
                // false→true (SM-2 repetitions ≥ 3), а не каждый верный ответ.
                val learnedDelta = newlyLearnedIds.size
                xpTracker.add(_state.value.earnedXp, learnedDelta)
                // If this was a Daily Set session, persist stars + best %.
                activeSetId?.let { setId ->
                    val total = deckSizeBeforeRequeue   // original deck, not inflated by requeues
                    val correct = _state.value.correctCount
                    val percent = if (total > 0) (correct * 100 / total) else 0
                    val stars = when {
                        percent >= 90 -> 3
                        percent >= 70 -> 2
                        percent >= 50 -> 1
                        else          -> 0
                    }
                    val existing = setProgressDao.getOne(setId)
                    setProgressDao.upsert(
                        com.spanishapp.data.db.entity.FlashcardSetProgressEntity(
                            setId = setId,
                            stars = maxOf(existing?.stars ?: 0, stars),
                            bestPercent = maxOf(existing?.bestPercent ?: 0, percent),
                            completedAt = System.currentTimeMillis()
                        )
                    )
                    // v1.16.0: +3 💡 за прохождение сета на 100% (perfect)
                    if (percent == 100) {
                        hintBank.award(3, com.spanishapp.service.HintEarnReason.FLASHCARD_SET_100)
                    }
                }
            }
        }
    }

    /** Revert the last answered card — restores DB word and previous state. */
    fun undo() {
        val (prevState, originalWord) = undoSnapshot ?: return
        undoSnapshot = null
        requeuedIds.remove(originalWord.id)
        // v1.25.98: откат ответа отменяет и «выучено» этого слова.
        newlyLearnedIds.remove(originalWord.id)
        viewModelScope.launch {
            wordDao.update(originalWord)           // revert SM-2 changes
        }
        // Restore to the moment the card was flipped, ready to re-answer.
        _state.value = prevState.copy(showBack = true, canUndo = false)
    }

    fun restart() {
        val s = _state.value
        startSession(
            level = s.level,
            category = s.category,
            direction = mode,
            sessionSize = 20
        )
    }

    private fun resolveDirection(d: FlashcardDirection): FlashcardDirection = when (d) {
        FlashcardDirection.MIXED ->
            if ((0..1).random() == 0) FlashcardDirection.ES_TO_RU else FlashcardDirection.RU_TO_ES
        else -> d
    }
}
