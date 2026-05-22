package com.spanishapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.domain.games.GameId
import com.spanishapp.domain.games.GameLevelManager
import com.spanishapp.domain.games.LevelDifficulty
import com.spanishapp.domain.games.LevelParams
import com.spanishapp.domain.algorithm.RatingUpdater
import com.spanishapp.service.AchievementManager
import com.spanishapp.service.SpanishTts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

/**
 * Что показывается на экране для текущего уровня:
 * NUMBERS  — арабские цифры (legacy, не используется): «5 + 3»
 * SPANISH  — испанский текст     (1-25): «cinco más tres»
 * AUDIO    — только символ «?»  (26-100): пользователь слышит TTS
 *
 * Примеры всегда словесные — пользователь учится понимать числа на испанском.
 * Ответ всегда вводится цифрами.
 */
enum class MathDisplayMode { NUMBERS, SPANISH, AUDIO }

data class MathGameState(
    val params: LevelParams = LevelDifficulty.forLevel(1),
    val expressionText: String = "",     // текст для отображения (числа/испанский/«?»)
    val expressionSpoken: String = "",   // текст для TTS (всегда испанский)
    val correctAnswer: Int = 0,
    val timeLeft: Float = 1f,
    val score: Int = 0,
    val correctCount: Int = 0,
    val streak: Int = 0,
    val currentRound: Int = 0,
    val isGameOver: Boolean = false,
    val lastCorrect: Boolean? = null,
    val displayMode: MathDisplayMode = MathDisplayMode.SPANISH,
    val audioEnabled: Boolean = true,
    val answerHistory: List<Boolean> = emptyList(),  // для ProgressDots
    val finalStars: Int = 0,
    val finalPercent: Int = 0,
    val showLevelMap: Boolean = true,
    val isMistakesPractice: Boolean = false,
) {
    val totalRounds: Int get() = params.rounds
    val level: Int get() = params.level
}

@HiltViewModel
class MathViewModel @Inject constructor(
    private val userProgressDao: UserProgressDao,
    private val achievementManager: AchievementManager,
    private val tts: SpanishTts,
    private val ratingUpdater: RatingUpdater,
    private val mistakesDao: com.spanishapp.data.db.dao.GameMistakesDao,
    val levelManager: GameLevelManager
) : ViewModel() {

    private val _state = MutableStateFlow(MathGameState())
    val state = _state.asStateFlow()

    val mistakesCount: kotlinx.coroutines.flow.StateFlow<Int> = mistakesDao
        .observeCount(GameId.MATH)
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0)

    private var mistakesBatch: List<com.spanishapp.data.db.entity.GameMistakeEntity> = emptyList()

    /** v1.22.0: «Работа над ошибками» — 5 заданий из mistakes. */
    fun startMistakesPractice() {
        timerJob?.cancel()
        roundResolved = false
        viewModelScope.launch {
            val batch = mistakesDao.getNextBatch(GameId.MATH, 5)
            if (batch.isEmpty()) return@launch
            mistakesBatch = batch
            val params = LevelDifficulty.forLevel(1).copy(rounds = batch.size, timePerRoundSec = 0f)
            _state.value = MathGameState(
                params = params,
                displayMode = MathDisplayMode.SPANISH,
                audioEnabled = _state.value.audioEnabled,
                showLevelMap = false,
                isMistakesPractice = true,
            )
            nextMistakeQuestion()
        }
    }

    private fun nextMistakeQuestion() {
        val s = _state.value
        if (s.currentRound >= mistakesBatch.size) {
            finishGame()
            return
        }
        roundResolved = false
        val mistake = mistakesBatch[s.currentRound]
        // itemId хранит выражение типа "3 + 5", main — то же. correctAnswer
        // не сохраняем, пересчитываем из выражения.
        val expr = mistake.itemId
        val correctAnswer = evaluateSimpleExpression(expr) ?: run {
            _state.value = s.copy(currentRound = s.currentRound + 1)
            nextMistakeQuestion()
            return
        }
        _state.value = s.copy(
            currentRound = s.currentRound + 1,
            expressionText = expr,
            expressionSpoken = expr,
            correctAnswer = correctAnswer,
            lastCorrect = null,
            timeLeft = 1f,
        )
    }

    /** Считает простое выражение «3 + 5» / «12 - 4» / «6 * 2» / «10 / 2». */
    private fun evaluateSimpleExpression(expr: String): Int? {
        val parts = expr.trim().split(Regex("\\s+"))
        if (parts.size != 3) return null
        val a = parts[0].toIntOrNull() ?: return null
        val b = parts[2].toIntOrNull() ?: return null
        return when (parts[1]) {
            "+" -> a + b
            "-" -> a - b
            "*", "×" -> a * b
            "/", "÷" -> if (b != 0) a / b else null
            else -> null
        }
    }

    private var timerJob: kotlinx.coroutines.Job? = null
    @Volatile private var roundResolved = false

    fun startLevel(level: Int) {
        timerJob?.cancel()
        roundResolved = false
        val params = LevelDifficulty.forLevel(level)
        _state.value = MathGameState(
            params      = params,
            displayMode = displayModeFor(level),
            audioEnabled = _state.value.audioEnabled,
            showLevelMap = false
        )
        nextQuestion()
    }

    /** Включить / выключить автоматическую озвучку. Кнопка «повторить» работает всегда. */
    fun toggleAudio() {
        val newValue = !_state.value.audioEnabled
        _state.value = _state.value.copy(audioEnabled = newValue)
        if (!newValue) tts.stop()  // прервать текущее произнесение
    }

    fun openLevelMap() {
        timerJob?.cancel()
        tts.stop()
        _state.value = _state.value.copy(showLevelMap = true, isGameOver = false)
    }

    override fun onCleared() {
        timerJob?.cancel()
        tts.stop()
        super.onCleared()
    }

    /** Повторить произнесение задания. Доступно на всех уровнях. */
    fun repeatQuestion() {
        val s = _state.value
        if (s.expressionSpoken.isNotBlank()) tts.speak(s.expressionSpoken)
    }

    private fun displayModeFor(level: Int): MathDisplayMode = when {
        level <= 25 -> MathDisplayMode.SPANISH
        else        -> MathDisplayMode.AUDIO
    }

    private fun nextQuestion() {
        val s = _state.value
        if (s.currentRound >= s.totalRounds) {
            finishGame()
            return
        }

        // v1.22.2: ДЕТЕРМИНИРОВАННАЯ генерация. Seed = (level, round) →
        // одно и то же выражение для одного и того же раунда. Раньше
        // использовался kotlin.random.Random.Default → каждый запуск
        // одного уровня давал новые цифры (и иногда РАЗНОЕ их количество
        // т.к. в Math количество фиксировано через params.rounds, но юзер
        // жаловался на «вариативность» именно из-за этой случайности).
        val seed = s.level * 10_000L + s.currentRound
        val (display, spoken, answer) = generateExpression(
            s.level,
            s.displayMode,
            Random(seed),
        )
        roundResolved = false

        _state.value = s.copy(
            expressionText   = display,
            expressionSpoken = spoken,
            correctAnswer    = answer,
            timeLeft         = 1f,
            currentRound     = s.currentRound + 1,
            lastCorrect      = null
        )

        // Озвучка примера на испанском, если пользователь не выключил звук.
        // На уровнях 26+ (AUDIO) звук критичен — отключение там делает игру непроходимой,
        // но это выбор пользователя; кнопка «повторить» работает всегда.
        if (s.audioEnabled) {
            tts.speak(spoken)
        }

        startTimer()
    }

    private data class Expr(val display: String, val spoken: String, val answer: Int)

    private fun generateExpression(level: Int, mode: MathDisplayMode, rng: Random): Expr {
        // ── Подбор выражения по диапазону уровней ────────────
        val (display, spoken, answer) = when {
            level <= 10 -> {
                // +/- от 1 до 10 (словами)
                val a = rng.nextInt(1, 11)
                val b = rng.nextInt(1, 11)
                if (rng.nextBoolean()) {
                    Triple(
                        "${NumberToSpanish.convert(a)} + ${NumberToSpanish.convert(b)}",
                        "${NumberToSpanish.convert(a)} más ${NumberToSpanish.convert(b)}",
                        a + b
                    )
                } else {
                    val mx = maxOf(a, b); val mn = minOf(a, b)
                    Triple(
                        "${NumberToSpanish.convert(mx)} - ${NumberToSpanish.convert(mn)}",
                        "${NumberToSpanish.convert(mx)} menos ${NumberToSpanish.convert(mn)}",
                        mx - mn
                    )
                }
            }
            level <= 25 -> {
                // +/- от 1 до 20
                val a = rng.nextInt(1, 21); val b = rng.nextInt(1, 21)
                if (rng.nextBoolean()) {
                    Triple(
                        "${NumberToSpanish.convert(a)} + ${NumberToSpanish.convert(b)}",
                        "${NumberToSpanish.convert(a)} más ${NumberToSpanish.convert(b)}",
                        a + b
                    )
                } else {
                    val mx = maxOf(a, b); val mn = minOf(a, b)
                    Triple(
                        "${NumberToSpanish.convert(mx)} - ${NumberToSpanish.convert(mn)}",
                        "${NumberToSpanish.convert(mx)} menos ${NumberToSpanish.convert(mn)}",
                        mx - mn
                    )
                }
            }
            level <= 40 -> {
                // +/- от 1 до 50
                val a = rng.nextInt(1, 51); val b = rng.nextInt(1, 51)
                if (rng.nextBoolean()) {
                    Triple("?", "${NumberToSpanish.convert(a)} más ${NumberToSpanish.convert(b)}", a + b)
                } else {
                    val mx = maxOf(a, b); val mn = minOf(a, b)
                    Triple("?", "${NumberToSpanish.convert(mx)} menos ${NumberToSpanish.convert(mn)}", mx - mn)
                }
            }
            level <= 70 -> {
                // ×/÷ : до 10 (уровни 41-55) или до 12 (уровни 56-70)
                val maxFactor = if (level <= 55) 11 else 13
                val a = rng.nextInt(2, maxFactor); val b = rng.nextInt(2, maxFactor)
                if (rng.nextBoolean()) {
                    Triple("?", "${NumberToSpanish.convert(a)} por ${NumberToSpanish.convert(b)}", a * b)
                } else {
                    val prod = a * b
                    Triple("?", "${NumberToSpanish.convert(prod)} dividido entre ${NumberToSpanish.convert(a)}", b)
                }
            }
            level <= 85 -> {
                // la mitad / el doble / el triple
                when (rng.nextInt(3)) {
                    0 -> {
                        val a = rng.nextInt(10, 101)
                        Triple("?", "La mitad de ${NumberToSpanish.convert(a * 2)}", a)
                    }
                    1 -> {
                        val a = rng.nextInt(5, 51)
                        Triple("?", "El doble de ${NumberToSpanish.convert(a)}", a * 2)
                    }
                    else -> {
                        val a = rng.nextInt(3, 31)
                        Triple("?", "El triple de ${NumberToSpanish.convert(a)}", a * 3)
                    }
                }
            }
            else -> {
                // Комбинированные
                when (rng.nextInt(3)) {
                    0 -> {
                        // «Тройное a минус b»
                        val a = rng.nextInt(5, 31); val b = rng.nextInt(2, 8)
                        Triple("?", "El triple de ${NumberToSpanish.convert(a)} menos ${NumberToSpanish.convert(b)}", a * 3 - b)
                    }
                    1 -> {
                        // «Двойное a плюс b»
                        val a = rng.nextInt(10, 51); val b = rng.nextInt(5, 21)
                        Triple("?", "El doble de ${NumberToSpanish.convert(a)} más ${NumberToSpanish.convert(b)}", a * 2 + b)
                    }
                    else -> {
                        // «Половина a минус b», a всегда чётное
                        val a = rng.nextInt(10, 51) * 2; val b = rng.nextInt(2, 11)
                        Triple("?", "La mitad de ${NumberToSpanish.convert(a)} menos ${NumberToSpanish.convert(b)}", a / 2 - b)
                    }
                }
            }
        }
        // В AUDIO-режиме скрываем выражение (только «?»), в SPANISH показываем словами.
        return when (mode) {
            MathDisplayMode.AUDIO -> Expr("?", spoken, answer)
            else                  -> Expr(display, spoken, answer)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        val baseSec = _state.value.params.timePerRoundSec
        if (baseSec <= 0f) return   // 1-10 без таймера
        timerJob = viewModelScope.launch {
            val step = 0.05f
            while (_state.value.timeLeft > 0 && !roundResolved) {
                delay(50)
                val newTime = (_state.value.timeLeft - (step / baseSec)).coerceAtLeast(0f)
                _state.value = _state.value.copy(timeLeft = newTime)
            }
            if (!roundResolved) submitAnswer(null)
        }
    }

    fun submitAnswer(answer: Int?) {
        if (roundResolved) return
        roundResolved = true
        timerJob?.cancel()
        val s = _state.value
        val isCorrect = answer == s.correctAnswer
        val newStreak = if (isCorrect) s.streak + 1 else 0
        val points = if (isCorrect) (10 * (1 + newStreak * 0.1f)).toInt() else 0

        _state.value = s.copy(
            lastCorrect  = isCorrect,
            score        = s.score + points,
            correctCount = if (isCorrect) s.correctCount + 1 else s.correctCount,
            streak       = newStreak,
            answerHistory = s.answerHistory + isCorrect
        )

        // v1.22.0: «Работа над ошибками»
        viewModelScope.launch {
            val itemId = s.expressionSpoken
            if (itemId.isNotBlank()) {
                if (isCorrect && s.isMistakesPractice) {
                    mistakesDao.removeMistake(GameId.MATH, itemId)
                } else if (!isCorrect) {
                    mistakesDao.recordMistake(
                        gameId = GameId.MATH,
                        itemId = itemId,
                        hint = "= ${s.correctAnswer}",
                        main = itemId,
                    )
                }
            }
        }

        viewModelScope.launch {
            ratingUpdater.applyGameAnswer(isCorrect)
            // На правильном ответе короче (поощряем темп), на ошибке — даём прочитать
            delay(if (isCorrect) 800 else 1500)
            if (_state.value.isMistakesPractice) nextMistakeQuestion()
            else nextQuestion()
        }
    }

    private fun finishGame() {
        val s = _state.value
        val percent = if (s.totalRounds > 0) (s.correctCount * 100) / s.totalRounds else 0

        viewModelScope.launch {
            val stars = levelManager.completeLevel(GameId.MATH, s.level, percent)

            val p = userProgressDao.getProgressOnce()
            if (p != null) {
                val xpGain = (s.score / 5).coerceAtLeast(5)
                userProgressDao.update(p.copy(totalXp = p.totalXp + xpGain))
                achievementManager.checkAndUnlock()
            }

            _state.value = s.copy(
                isGameOver   = true,
                finalStars   = stars,
                finalPercent = percent
            )
        }
    }
}
