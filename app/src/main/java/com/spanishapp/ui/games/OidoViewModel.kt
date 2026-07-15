package com.spanishapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.domain.algorithm.RatingUpdater
import com.spanishapp.domain.games.GameId
import com.spanishapp.domain.games.GameLevelManager
import com.spanishapp.service.AchievementManager
import com.spanishapp.service.SoundPlayer
import com.spanishapp.service.SpanishTts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

/** Одно задание El Oído. [spoken] — текст для диктора. */
sealed class OidoTask {
    abstract val spoken: String

    /** Услышал слово → выбери перевод (4 варианта по-русски). */
    data class Choice(
        val word: String,
        val correctRu: String,
        val options: List<String>,
    ) : OidoTask() {
        override val spoken: String get() = word
    }

    /** Услышал слово → напиши по-испански. */
    data class Dictation(
        val word: String,
        val ru: String,
    ) : OidoTask() {
        override val spoken: String get() = word
    }

    /** Минимальная пара: что прозвучало — a или b? */
    data class MinimalPair(
        val pair: OidoPair,
        val playA: Boolean,
    ) : OidoTask() {
        override val spoken: String get() = if (playA) pair.a else pair.b
        val correctWord: String get() = if (playA) pair.a else pair.b
    }

    /** Услышал число → набери цифрами. */
    data class Number(
        val value: Int,
        override val spoken: String,
    ) : OidoTask()

    /** Услышал время → набери цифрами (8:30 → 830). */
    data class Time(
        val hour: Int,
        val minute: Int,
        override val spoken: String,
    ) : OidoTask() {
        val expected: Int get() = TimeToSpanish.expectedDigits(hour, minute)
        val displayTime: String get() = TimeToSpanish.display(hour, minute)
    }
}

data class OidoState(
    val level: Int = 1,
    val rate: Float = 0.75f,
    val task: OidoTask? = null,
    val currentRound: Int = 0,
    val totalRounds: Int = OidoEngine.TASKS_PER_LEVEL,
    /** Сколько повторов осталось (2 на задание; второй — медленный). */
    val replaysLeft: Int = 2,
    val lastCorrect: Boolean? = null,
    /** Пояснение после ответа: правильный вариант / заметка пары / написание. */
    val feedback: String? = null,
    /** Диктант засчитан, но без акцентов/ñ — показать правильное написание. */
    val accentWarning: Boolean = false,
    /** 50/50: варианты, погашенные подсказкой (только режим «выбор»). */
    val disabledOptions: Set<String> = emptySet(),
    val score: Int = 0,
    val correctCount: Int = 0,
    val streak: Int = 0,
    val answerHistory: List<Boolean> = emptyList(),
    val isGameOver: Boolean = false,
    val finalStars: Int = 0,
    val finalPercent: Int = 0,
    val showLevelMap: Boolean = true,
    val isMistakesPractice: Boolean = false,
    val isLoading: Boolean = false,
)

@HiltViewModel
class OidoViewModel @Inject constructor(
    private val userProgressDao: UserProgressDao,
    private val wordDao: WordDao,
    private val achievementManager: AchievementManager,
    private val ratingUpdater: RatingUpdater,
    private val mistakesDao: com.spanishapp.data.db.dao.GameMistakesDao,
    private val soundPlayer: SoundPlayer,
    private val tts: SpanishTts,
    private val hintBank: com.spanishapp.service.HintBankManager,
    val levelManager: GameLevelManager,
) : ViewModel() {

    private val _state = MutableStateFlow(OidoState())
    val state = _state.asStateFlow()

    /** Баланс 💡 для бейджа и кнопок подсказок. */
    val hintBalance: kotlinx.coroutines.flow.StateFlow<Int> = hintBank.hintsFlow
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0)

    val mistakesCount: kotlinx.coroutines.flow.StateFlow<Int> = mistakesDao
        .observeCount(GameId.OIDO)
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0)

    private var tasks: List<OidoTask> = emptyList()
    private var currentMistakeKey: String? = null
    private var advanceJob: kotlinx.coroutines.Job? = null
    @Volatile private var roundResolved = false

    /** Ответ куплен за 💡 — очков не даёт и серию не продолжает. */
    @Volatile private var revealUsed = false

    fun startLevel(level: Int) {
        advanceJob?.cancel()
        _state.value = OidoState(
            level = level,
            rate = OidoEngine.rateForLevel(level),
            showLevelMap = false,
            isLoading = true,
        )
        viewModelScope.launch {
            tasks = buildTasks(level)
            if (tasks.isEmpty()) {
                // Словарь ещё не засеян — вернёмся на карту, не крашимся.
                _state.value = _state.value.copy(showLevelMap = true, isLoading = false)
                return@launch
            }
            _state.value = _state.value.copy(
                totalRounds = tasks.size,
                isLoading = false,
            )
            nextTask()
        }
    }

    /** «Работа над ошибками»: слушаем и пишем/набираем то, где споткнулись. */
    fun startMistakesPractice() {
        advanceJob?.cancel()
        viewModelScope.launch {
            val batch = mistakesDao.getNextBatch(GameId.OIDO, 5)
            if (batch.isEmpty()) return@launch
            tasks = batch.mapNotNull { m ->
                val digits = m.displayHint.trim().toIntOrNull()
                when {
                    digits != null -> OidoTask.Number(value = digits, spoken = m.itemId)
                    m.itemId.isNotBlank() -> OidoTask.Dictation(word = m.itemId, ru = m.displayHint)
                    else -> null
                }
            }
            if (tasks.isEmpty()) return@launch
            _state.value = OidoState(
                level = 1,
                rate = 0.9f,
                totalRounds = tasks.size,
                showLevelMap = false,
                isMistakesPractice = true,
            )
            nextTask()
        }
    }

    fun openLevelMap() {
        advanceJob?.cancel()
        tts.stop()
        _state.value = _state.value.copy(showLevelMap = true, isGameOver = false)
    }

    /** Повтор: первый — обычный темп, второй — медленный, но это всё. */
    fun replay() {
        val s = _state.value
        val task = s.task ?: return
        if (s.replaysLeft <= 0 || s.lastCorrect != null) return
        val slowFactor = if (s.replaysLeft == 1) 0.75f else 1f
        tts.speak(task.spoken, rateMultiplier = s.rate * slowFactor)
        _state.value = s.copy(replaysLeft = s.replaysLeft - 1)
    }

    // ── Подсказки (💡 HintBank — общая валюта всех игр) ──────

    /** 50/50 за 50💡: гасит два неверных варианта (только режим «выбор»). */
    fun useFiftyFifty(onNoHints: () -> Unit) {
        val s = _state.value
        val task = s.task as? OidoTask.Choice ?: return
        if (s.lastCorrect != null || s.disabledOptions.isNotEmpty()) return
        viewModelScope.launch {
            if (!hintBank.tryConsume(50)) {
                onNoHints()
                return@launch
            }
            val wrong = task.options.filter { it != task.correctRu }.shuffled().take(2)
            _state.value = _state.value.copy(disabledOptions = wrong.toSet())
        }
    }

    /** Верный ответ за 100💡: задание закрывается как решённое. */
    fun useRevealAnswer(onNoHints: () -> Unit) {
        val s = _state.value
        val task = s.task ?: return
        if (s.lastCorrect != null) return
        viewModelScope.launch {
            if (!hintBank.tryConsume(100)) {
                onNoHints()
                return@launch
            }
            revealUsed = true
            when (task) {
                is OidoTask.Choice -> submitChoice(task.correctRu)
                is OidoTask.MinimalPair -> submitPair(task.correctWord)
                is OidoTask.Dictation -> submitDictation(task.word)
                is OidoTask.Number -> submitDigits(task.value)
                is OidoTask.Time -> submitDigits(task.expected)
            }
        }
    }

    override fun onCleared() {
        advanceJob?.cancel()
        tts.stop()
        super.onCleared()
    }

    // ── Ответы ────────────────────────────────────────────────

    fun submitChoice(option: String) {
        val task = _state.value.task as? OidoTask.Choice ?: return
        resolve(
            isCorrect = option == task.correctRu,
            correctShown = "${task.word} — ${task.correctRu}",
            mistakeItem = task.word,
            mistakeHint = task.correctRu,
        )
    }

    fun submitPair(word: String) {
        val task = _state.value.task as? OidoTask.MinimalPair ?: return
        resolve(
            isCorrect = word == task.correctWord,
            correctShown = "«${task.correctWord}». ${task.pair.note}",
            mistakeItem = task.correctWord,
            mistakeHint = if (task.playA) task.pair.ruA else task.pair.ruB,
        )
    }

    fun submitDictation(typed: String) {
        val task = _state.value.task as? OidoTask.Dictation ?: return
        when (OidoEngine.matchDictation(task.word, typed)) {
            OidoMatch.EXACT -> resolve(
                isCorrect = true,
                correctShown = "${task.word} — ${task.ru}",
                mistakeItem = task.word,
                mistakeHint = task.ru,
            )
            OidoMatch.ACCENT_LOOSE -> resolve(
                isCorrect = true,
                correctShown = "${task.word} — ${task.ru}",
                mistakeItem = task.word,
                mistakeHint = task.ru,
                accentNote = task.word,
            )
            OidoMatch.NONE -> resolve(
                isCorrect = false,
                correctShown = "${task.word} — ${task.ru}",
                mistakeItem = task.word,
                mistakeHint = task.ru,
            )
        }
    }

    fun submitDigits(value: Int?) {
        when (val task = _state.value.task) {
            is OidoTask.Number -> resolve(
                isCorrect = value == task.value,
                correctShown = "${task.spoken} = ${task.value}",
                mistakeItem = task.spoken,
                mistakeHint = task.value.toString(),
            )
            is OidoTask.Time -> resolve(
                isCorrect = value == task.expected,
                correctShown = "${task.spoken} = ${task.displayTime}",
                mistakeItem = task.spoken,
                mistakeHint = task.expected.toString(),
            )
            else -> return
        }
    }

    private fun resolve(
        isCorrect: Boolean,
        correctShown: String,
        mistakeItem: String,
        mistakeHint: String,
        accentNote: String? = null,
    ) {
        if (roundResolved) return
        roundResolved = true
        val hinted = revealUsed
        revealUsed = false
        val s = _state.value
        // Купленный ответ: уровень засчитывается, но очков и серии не даёт.
        val newStreak = if (isCorrect && !hinted) s.streak + 1 else 0
        val points = if (isCorrect && !hinted) (10 * (1 + newStreak * 0.1f)).toInt() else 0

        if (isCorrect) soundPlayer.playCorrect() else soundPlayer.playWrong()

        _state.value = s.copy(
            lastCorrect = isCorrect,
            feedback = correctShown,
            accentWarning = accentNote != null,
            score = s.score + points,
            correctCount = if (isCorrect) s.correctCount + 1 else s.correctCount,
            streak = newStreak,
            answerHistory = s.answerHistory + isCorrect,
        )

        viewModelScope.launch {
            if (isCorrect && s.isMistakesPractice) {
                currentMistakeKey?.let {
                    runCatching { mistakesDao.removeMistake(GameId.OIDO, it) }
                }
            } else if (!isCorrect && !s.isMistakesPractice) {
                mistakesDao.recordMistake(
                    gameId = GameId.OIDO,
                    itemId = mistakeItem,
                    hint = mistakeHint,
                    main = mistakeItem,
                )
            }
            ratingUpdater.applyGameAnswer(isCorrect)
        }

        advanceJob?.cancel()
        advanceJob = viewModelScope.launch {
            delay(if (isCorrect) 1300 else 2200)
            nextTask()
        }
    }

    // ── Генерация ────────────────────────────────────────────

    private fun nextTask() {
        val s = _state.value
        if (s.currentRound >= tasks.size) {
            finishGame()
            return
        }
        roundResolved = false
        val task = tasks[s.currentRound]
        currentMistakeKey = when (task) {
            is OidoTask.Dictation -> task.word
            is OidoTask.Number -> task.spoken
            else -> null
        }
        _state.value = s.copy(
            task = task,
            currentRound = s.currentRound + 1,
            replaysLeft = 2,
            lastCorrect = null,
            feedback = null,
            accentWarning = false,
            disabledOptions = emptySet(),
        )
        tts.speak(task.spoken, rateMultiplier = s.rate)
    }

    private suspend fun buildTasks(level: Int): List<OidoTask> {
        val plan = OidoEngine.planForLevel(level)
        val rng = Random(level * 17L)

        // Пул слов уровня. Просим с запасом: часть отсеется фильтрами.
        val pool = runCatching {
            wordDao.getRandomByLevels(OidoEngine.cefrForLevel(level), 80)
        }.getOrDefault(emptyList())
            .filter { it.spanish.isNotBlank() && it.russian.isNotBlank() }

        val dictationPool = pool.filter { OidoEngine.isDictationFriendly(it.spanish) }
            .distinctBy { it.spanish.lowercase() }
            .toMutableList()
        val choicePool = pool.distinctBy { it.spanish.lowercase() }.toMutableList()
        val pairs = OidoEngine.pairsForLevel(level, plan.count { it == OidoMode.PAIRS })
            .toMutableList()

        // Заглавная буква — как в остальном UI приложения
        fun cap(s: String) = s.trim().replaceFirstChar { it.uppercaseChar() }

        val result = mutableListOf<OidoTask>()
        for (mode in plan) {
            when (mode) {
                OidoMode.CHOICE -> {
                    if (choicePool.size < 4) continue
                    val word = choicePool.removeAt(0)
                    val correctRu = cap(word.russian)
                    val targetWords = word.russian.trim().split(' ').size
                    // Дистракторы: без дублей, без совпадения с ответом и
                    // соразмерные по длине (чтобы «У меня есть бронь» не
                    // выдавала себя рядом с одиночными словами).
                    val distractors = choicePool.asSequence()
                        .map { cap(it.russian) }
                        .filter { it.lowercase() != correctRu.lowercase() }
                        .filter { kotlin.math.abs(it.split(' ').size - targetWords) <= 1 }
                        .distinctBy { it.lowercase() }
                        .take(3)
                        .toList()
                    if (distractors.size < 3) continue
                    result += OidoTask.Choice(
                        word = word.spanish,
                        correctRu = correctRu,
                        options = (distractors + correctRu).shuffled(rng),
                    )
                }
                OidoMode.DICTATION -> {
                    if (dictationPool.isEmpty()) continue
                    val word = dictationPool.removeAt(0)
                    choicePool.removeAll { it.spanish == word.spanish }
                    result += OidoTask.Dictation(word = word.spanish, ru = cap(word.russian))
                }
                OidoMode.PAIRS -> {
                    if (pairs.isEmpty()) continue
                    val pair = pairs.removeAt(0)
                    result += OidoTask.MinimalPair(pair = pair, playA = rng.nextBoolean())
                }
                OidoMode.NUMBER -> {
                    val n = OidoEngine.numberForLevel(level, rng)
                    result += OidoTask.Number(value = n, spoken = NumberToSpanish.convert(n))
                }
                OidoMode.TIME -> {
                    val hour = rng.nextInt(1, 13)
                    val minute = listOf(0, 15, 30, 45)[rng.nextInt(4)]
                    result += OidoTask.Time(
                        hour = hour,
                        minute = minute,
                        spoken = TimeToSpanish.convert(hour, minute),
                    )
                }
            }
        }
        return result
    }

    private fun finishGame() {
        advanceJob?.cancel()
        val s = _state.value
        val percent = if (s.totalRounds > 0) (s.correctCount * 100) / s.totalRounds else 0

        viewModelScope.launch {
            val stars = if (s.isMistakesPractice) 0
                        else levelManager.completeLevel(GameId.OIDO, s.level, percent)

            val p = userProgressDao.getProgressOnce()
            if (p != null) {
                val xpGain = if (s.isMistakesPractice) s.correctCount * 3
                             else (percent / 10) * 5
                if (xpGain > 0) {
                    userProgressDao.update(p.copy(totalXp = p.totalXp + xpGain))
                }
                achievementManager.checkAndUnlock()
            }

            if (stars > 0) soundPlayer.playLevelDone()

            _state.value = s.copy(
                isGameOver = true,
                finalStars = stars,
                finalPercent = percent,
            )
        }
    }
}
