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
    /**
     * v1.26.1 FIX (audit): исходный размер колоды (заморожен на старте сессии).
     * sessionSize растёт от requeue HARD-слов — для счётчика «N из M»,
     * прогресс-бара и знаменателя процента нужен неизменный размер.
     */
    val deckSize: Int = 0,
    /** v1.26.1 FIX (audit): название сета для топ-бара (null = generic заголовок). */
    val sessionTitle: String? = null,
    /**
     * v1.26.1 redesign: суммарный XP пользователя ПОСЛЕ начисления за сессию —
     * для полосы «до следующего уровня» на финальном экране. 0 = ещё не прочитан.
     */
    val totalXpAfter: Int = 0,
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

    /**
     * v1.26.1 FIX (audit): ключ аргументов активной сессии. Поворот экрана
     * пересоздаёт Activity → LaunchedEffect экрана заново зовёт start*, и
     * сессия молча перезапускалась с карточки 1 (терялись индекс и счёт).
     * Если аргументы те же и сессия ещё идёт — не перезагружаем.
     */
    private var lastSessionKey: String? = null

    /** v1.26.1 FIX (audit): исходный размер колоды — знаменатель процента/звёзд. */
    private var originalDeckSize = 0

    fun startSession(
        level: String,
        category: String,
        direction: FlashcardDirection,
        sessionSize: Int = 20,
        weakOnly: Boolean = false,
    ) {
        // v1.26.1 FIX (audit): guard от рестарта при recreate Activity (rotation).
        // restart() проходит guard, т.к. на финальном экране isFinished = true.
        val sessionKey = "cat|$level|$category|$direction|$sessionSize|$weakOnly"
        val prev = _state.value
        if (sessionKey == lastSessionKey && prev.cards.isNotEmpty() && !prev.isFinished) return
        lastSessionKey = sessionKey
        mode = direction
        activeSetId = null
        lastWeakOnly = weakOnly
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
            originalDeckSize = cards.size   // v1.26.1 FIX (audit): фиксируем до requeue
            _state.value = FlashcardsUiState(
                isLoading = false,
                cards = cards,
                currentIndex = 0,
                showBack = false,
                currentDirection = resolveDirection(direction),
                level = level,
                category = category,
                sessionSize = cards.size,
                deckSize = cards.size
            )
        }
    }

    /**
     * Start a session for a Daily Set: deck = exactly the words listed in the
     * set's [FlashcardSet.wordsSpanish], in shuffled order, no SM-2 mixing.
     * After the session ends, [maybeSaveSetCompletion] persists stars + best %.
     */
    fun startSetSession(setId: String, direction: FlashcardDirection) {
        // v1.26.1 FIX (audit): guard от рестарта при recreate Activity (rotation).
        val sessionKey = "set|$setId|$direction"
        val prev = _state.value
        if (sessionKey == lastSessionKey && prev.cards.isNotEmpty() && !prev.isFinished) return
        lastSessionKey = sessionKey
        mode = direction
        activeSetId = setId
        lastWeakOnly = false
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
            // v1.26.1 FIX (фидбэк владельца): колода сета — в АВТОРСКОМ порядке
            // списка, а не shuffled(). «Местоимения» встречали случайным
            // «vosotros» вместо yo → tú → él…, «Числа 1-20» начинались с
            // catorce — выглядело «коряво и не все».
            val byKey = wordDao.findBySpanishMany(
                set.wordsSpanish.map { it.lowercase().trim() }
            ).associateBy { it.spanish.trim().lowercase() }
            val cards = set.wordsSpanish.mapNotNull { byKey[it.lowercase().trim()] }
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
            originalDeckSize = cards.size   // v1.26.1 FIX (audit): фиксируем до requeue
            _state.value = FlashcardsUiState(
                isLoading = false,
                cards = cards,
                currentIndex = 0,
                showBack = false,
                currentDirection = resolveDirection(direction),
                level = set.level,
                category = "set",
                sessionSize = cards.size,
                deckSize = cards.size,
                // v1.26.1 FIX (audit): тема сета в топ-баре — юзер открыл
                // «Местоимения» и не видел, какой это сет.
                sessionTitle = set.title
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

        // v1.26.1 FIX (audit): lapsed-слова (SM-2 fail → repetitions=0, но
        // total_reviews>0) теперь попадают в due-выборку И в «новые»
        // (repetitions=0) — дедупим по id, чтобы слово не встретилось дважды.
        return (due + fresh + extra).distinctBy { it.id }.take(sessionSize).shuffled()
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
            // v1.26.1 FIX (audit): EASY давал те же +5, что и GOOD — теперь
            // честные +10 по XpSystem.WORD_EASY.
            ReviewButton.EASY -> XpSystem.WORD_EASY
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

        // Re-queue HARD words 3 positions ahead (each word re-inserted at most once).
        // v1.26.1 FIX (audit): в очередь идёт post-fail `updated`, а не устаревший
        // снапшот `current` — иначе повторный ответ гнал SM2.review по состоянию
        // ДО провала и стирал зафиксированный fail (ложный isLearned, раздутый
        // words_learned).
        val updatedCards = if (quality < 3 && current.id !in requeuedIds) {
            requeuedIds.add(current.id)
            val mutable = s.cards.toMutableList()
            val insertAt = (s.currentIndex + 3).coerceAtMost(mutable.size)
            mutable.add(insertAt, updated)
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
                // v1.26.1 redesign: суммарный XP после начисления — финал рисует
                // полосу «Уровень N · до следующего K XP».
                userProgressDao.getProgressOnce()?.let { p ->
                    _state.value = _state.value.copy(totalXpAfter = p.totalXp)
                }
                // If this was a Daily Set session, persist stars + best %.
                activeSetId?.let { setId ->
                    // v1.26.1 FIX (audit): знаменатель — исходная колода со старта
                    // сессии. Прежний deckSizeBeforeRequeue снимался на ПОСЛЕДНЕМ
                    // ответе и уже включал requeue-вставки — best_percent/звёзды
                    // занижались.
                    // v1.26.1 redesign: процент — «с первой попытки». correctCount
                    // включает верные ответы на requeue-повторах: провалил 3 →
                    // карты вернулись → ответил верно → «16 из 16» и 100% при
                    // 3 реальных ошибках. wrongWords = дедуп-список провалённых
                    // хотя бы раз — честный числитель: total - wrongWords.
                    val total = originalDeckSize
                    val correct = (total - _state.value.wrongWords.size).coerceIn(0, total)
                    val percent = if (total > 0) (correct * 100 / total).coerceIn(0, 100) else 0
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

    /** true если текущая сессия была weak-words (для честного restart). */
    private var lastWeakOnly = false

    fun restart() {
        val s = _state.value
        // v1.25.98 FIX (audit cards-H2): «Повторить сет» после set-сессии всегда
        // давал «Нет слов для сессии» — restart() шёл через startSession с
        // category="set", которой нет в words. Теперь set-сессии рестартуются
        // через startSetSession, а weak-сессии сохраняют weakOnly-флаг (раньше
        // «ещё раз» слабых слов молча превращался в обычную колоду A1/all).
        val setId = activeSetId
        if (setId != null) {
            startSetSession(setId, mode)
            return
        }
        startSession(
            level = s.level,
            category = s.category,
            direction = mode,
            sessionSize = 20,
            weakOnly = lastWeakOnly,
        )
    }

    private fun resolveDirection(d: FlashcardDirection): FlashcardDirection = when (d) {
        FlashcardDirection.MIXED ->
            if ((0..1).random() == 0) FlashcardDirection.ES_TO_RU else FlashcardDirection.RU_TO_ES
        else -> d
    }
}
