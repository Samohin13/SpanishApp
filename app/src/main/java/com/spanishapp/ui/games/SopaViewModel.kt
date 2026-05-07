package com.spanishapp.ui.games

import android.os.SystemClock
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.domain.games.GameId
import com.spanishapp.domain.games.GameLevelManager
import com.spanishapp.domain.games.LevelDifficulty
import com.spanishapp.domain.games.LevelParams
import com.spanishapp.domain.algorithm.RatingUpdater
import com.spanishapp.service.AchievementManager
import com.spanishapp.service.SpanishTts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.random.Random

data class SopaWord(
    val id: Int,
    val word: String,
    val translation: String = "",
    val isFound: Boolean = false,
    val findTime: Long = 0L,
    val color: Color = Color.Transparent
)

data class FoundWord(
    val word: String,
    val cells: List<Pair<Int, Int>>,
    val color: Color
)

/**
 * Параметры конкретного уровня Sopa, выводятся из общего LevelDifficulty.
 */
data class SopaLevelConfig(
    val gridSize: Int,        // 6..16
    val targetWords: Int,     // желаемое число слов
    val timeSec: Int,         // 0 = без таймера
    val ghost: Boolean        // прячем список слов
)

data class SopaGameState(
    val params: LevelParams = LevelDifficulty.forLevel(1),
    val config: SopaLevelConfig = SopaLevelConfig(6, 4, 0, false),
    val grid: List<List<Char>> = emptyList(),
    val words: List<SopaWord> = emptyList(),
    val selectedCells: List<Pair<Int, Int>> = emptyList(),
    val foundWords: List<FoundWord> = emptyList(),
    val hintCells: Set<Pair<Int, Int>> = emptySet(),
    val isGameOver: Boolean = false,
    val score: Int = 0,
    val combo: Int = 0,
    val timeLeftSeconds: Int = 0,
    val showLevelMap: Boolean = true,
    val finalStars: Int = 0,
    val finalPercent: Int = 0
) {
    val level: Int get() = params.level
    val hasTimer: Boolean get() = config.timeSec > 0
}

@HiltViewModel
class SopaViewModel @Inject constructor(
    private val wordDao: WordDao,
    private val userProgressDao: UserProgressDao,
    private val achievementManager: AchievementManager,
    private val tts: SpanishTts,
    private val ratingUpdater: RatingUpdater,
    val levelManager: GameLevelManager
) : ViewModel() {

    private val _state = MutableStateFlow(SopaGameState())
    val state = _state.asStateFlow()

    private var timerJob: Job? = null
    private var lastFindTime = 0L
    private var gameStartTime = 0L

    private val wordColors = listOf(
        Color(0xFFF44336), Color(0xFF2196F3), Color(0xFF4CAF50),
        Color(0xFFFF9800), Color(0xFF9C27B0), Color(0xFF00BCD4),
        Color(0xFFE91E63), Color(0xFF795548), Color(0xFF607D8B),
        Color(0xFF4DB6AC), Color(0xFF8BC34A), Color(0xFFCDDC39)
    )

    fun startLevel(level: Int) {
        timerJob?.cancel()
        viewModelScope.launch {
            val params = LevelDifficulty.forLevel(level)
            val config = configFor(level)

            gameStartTime = SystemClock.elapsedRealtime()
            lastFindTime  = gameStartTime

            // ── Получаем большой пул слов нужного CEFR-слоя ───
            val pool = wordDao.getRandomWords(300).filter {
                it.level in params.cefr && it.russian.isNotBlank() && it.spanish.isNotBlank()
            }
            val maxWordLen = config.gridSize - 1   // оставляем запас
            val candidates = pool.map {
                SopaWord(
                    id          = it.id,
                    word        = stripArticle(it.spanish).uppercase().replace(" ", "").replace("-", ""),
                    translation = it.russian
                )
            }.filter { it.word.length in 3..maxWordLen }
             .distinctBy { it.word }
             .shuffled()

            // ── Пытаемся уместить нужное число слов на сетке ───
            val grid = Array(config.gridSize) { CharArray(config.gridSize) { ' ' } }
            val placed = mutableListOf<SopaWord>()
            for (sw in candidates) {
                if (placed.size >= config.targetWords) break
                val path = mutableListOf<Pair<Int, Int>>()
                if (placeWordSnake(grid, sw.word, path)) {
                    placed.add(sw)
                }
            }
            if (placed.isEmpty()) return@launch
            fillEmptyCells(grid)

            _state.value = SopaGameState(
                params       = params,
                config       = config,
                grid         = grid.map { it.toList() },
                words        = placed,
                showLevelMap = false,
                timeLeftSeconds = config.timeSec
            )

            if (config.timeSec > 0) startTimer()
        }
    }

    fun openLevelMap() {
        timerJob?.cancel()
        _state.value = _state.value.copy(showLevelMap = true, isGameOver = false)
    }

    /**
     * Конфигурация уровня для Sopa.
     * Сетка плавно растёт 6→16, количество слов 4→18, таймер сжимается.
     */
    private fun configFor(level: Int): SopaLevelConfig = when {
        level <= 10  -> SopaLevelConfig(gridSize = 7,  targetWords = 4,  timeSec = 0,   ghost = false)
        level <= 25  -> SopaLevelConfig(gridSize = 8,  targetWords = 6,  timeSec = 300, ghost = false)
        level <= 40  -> SopaLevelConfig(gridSize = 10, targetWords = 8,  timeSec = 280, ghost = false)
        level <= 55  -> SopaLevelConfig(gridSize = 11, targetWords = 10, timeSec = 260, ghost = false)
        level <= 70  -> SopaLevelConfig(gridSize = 12, targetWords = 12, timeSec = 240, ghost = false)
        level <= 85  -> SopaLevelConfig(gridSize = 14, targetWords = 14, timeSec = 240, ghost = false)
        else         -> SopaLevelConfig(gridSize = 15, targetWords = 16, timeSec = 240, ghost = true)
    }

    private fun placeWordSnake(grid: Array<CharArray>, word: String, path: MutableList<Pair<Int, Int>>): Boolean {
        val size = grid.size
        var attempts = 0
        while (attempts < 200) {
            attempts++
            val startR = Random.nextInt(size)
            val startC = Random.nextInt(size)
            path.clear()
            if (findPath(grid, word, 0, startR, startC, path)) {
                path.forEachIndexed { i, pos -> grid[pos.first][pos.second] = word[i] }
                return true
            }
        }
        return false
    }

    private fun findPath(grid: Array<CharArray>, word: String, index: Int, r: Int, c: Int, path: MutableList<Pair<Int, Int>>): Boolean {
        if (index == word.length) return true
        if (r !in grid.indices || c !in grid[0].indices) return false
        if (grid[r][c] != ' ') return false
        if (path.contains(r to c)) return false

        path.add(r to c)
        val dirs = listOf(0 to 1, 0 to -1, 1 to 0, -1 to 0, 1 to 1, 1 to -1, -1 to 1, -1 to -1).shuffled()
        for (d in dirs) {
            if (findPath(grid, word, index + 1, r + d.first, c + d.second, path)) return true
        }
        path.removeAt(path.size - 1)
        return false
    }

    private fun fillEmptyCells(grid: Array<CharArray>) {
        val freq = "EEEEAAAAOOOOOSSSSRRRRNNNNIIIIIDDDDLLLLCCCCTTTTUUUUMMMM"
        for (r in grid.indices) for (c in grid[r].indices) {
            if (grid[r][c] == ' ') {
                grid[r][c] = if (Random.nextInt(100) < 85) freq.random() else ('A'..'Z').random()
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.timeLeftSeconds > 0 && !_state.value.isGameOver) {
                delay(1000)
                _state.value = _state.value.copy(timeLeftSeconds = _state.value.timeLeftSeconds - 1)
            }
            if (_state.value.timeLeftSeconds <= 0 && _state.value.hasTimer) finishGame()
        }
    }

    fun onDragStart(r: Int, c: Int) {
        if (_state.value.isGameOver) return
        _state.value = _state.value.copy(selectedCells = listOf(r to c))
    }

    fun onDragUpdate(r: Int, c: Int) {
        val s = _state.value
        if (s.isGameOver || s.selectedCells.isEmpty()) return
        val last = s.selectedCells.last()
        if (last == (r to c)) return
        if (abs(r - last.first) <= 1 && abs(c - last.second) <= 1) {
            if (s.selectedCells.size > 1 && s.selectedCells[s.selectedCells.size - 2] == (r to c)) {
                _state.value = s.copy(selectedCells = s.selectedCells.dropLast(1))
            } else if (!s.selectedCells.contains(r to c)) {
                _state.value = s.copy(selectedCells = s.selectedCells + (r to c))
            }
        }
    }

    fun onDragEnd() {
        val s = _state.value
        if (s.selectedCells.isNotEmpty()) checkWord(s.selectedCells)
        _state.value = _state.value.copy(selectedCells = emptyList())
    }

    private fun checkWord(cells: List<Pair<Int, Int>>) {
        val s = _state.value
        val wordStr = cells.map { (r, c) -> s.grid[r][c] }.joinToString("")
        val reversedStr = wordStr.reversed()
        val foundWordIndex = s.words.indexOfFirst { (it.word == wordStr || it.word == reversedStr) && !it.isFound }

        if (foundWordIndex != -1) {
            val foundWord = s.words[foundWordIndex]
            tts.speak(foundWord.word)
            viewModelScope.launch { ratingUpdater.applyGameAnswer(true) }
            val now = SystemClock.elapsedRealtime()
            val isCombo = now - lastFindTime < 5000
            lastFindTime = now
            val newCombo = if (isCombo) s.combo + 1 else 1
            val assignedColor = wordColors[s.foundWords.size % wordColors.size]
            val newWords = s.words.toMutableList()
            newWords[foundWordIndex] = foundWord.copy(isFound = true, findTime = now, color = assignedColor)
            val newFoundWords = s.foundWords + FoundWord(foundWord.word, cells, assignedColor)
            val newHintCells = s.hintCells.filterNot { cells.contains(it) }.toSet()

            _state.value = s.copy(
                words      = newWords,
                foundWords = newFoundWords,
                hintCells  = newHintCells,
                score      = s.score + 15 * newCombo,
                combo      = newCombo
            )
            if (newWords.all { it.isFound }) finishGame()
        }
    }

    private fun finishGame() {
        val s = _state.value
        val total = s.words.size.coerceAtLeast(1)
        val found = s.words.count { it.isFound }
        val percent = (found * 100) / total
        val bonus = if (s.hasTimer) s.timeLeftSeconds / 5 else 0

        viewModelScope.launch {
            val stars = levelManager.completeLevel(GameId.SOPA, s.level, percent)

            val p = userProgressDao.getProgressOnce()
            if (p != null) {
                userProgressDao.update(p.copy(totalXp = p.totalXp + (s.score / 6).coerceIn(15, 60)))
                achievementManager.checkAndUnlock()
            }

            _state.value = s.copy(
                isGameOver   = true,
                score        = s.score + bonus,
                finalStars   = stars,
                finalPercent = percent
            )
        }
    }

    fun useHint() {
        val s = _state.value
        if (s.score < 30) return
        val targetWord = s.words.find { !it.isFound } ?: return
        for (r in 0 until s.config.gridSize) {
            for (c in 0 until s.config.gridSize) {
                if (s.grid[r][c] == targetWord.word[0]) {
                    if (s.foundWords.none { it.cells.contains(r to c) } && !s.hintCells.contains(r to c)) {
                        _state.value = s.copy(score = s.score - 30, hintCells = s.hintCells + (r to c))
                        return
                    }
                }
            }
        }
    }

    fun clearSelection() {
        _state.value = _state.value.copy(selectedCells = emptyList())
    }

    private fun stripArticle(s: String): String {
        val regex = Regex("^(el|la|los|las|un|una|unos|unas)\\s+", RegexOption.IGNORE_CASE)
        return s.trim().replace(regex, "").trim()
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
