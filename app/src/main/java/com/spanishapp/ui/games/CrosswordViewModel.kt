package com.spanishapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.service.AchievementManager
import com.spanishapp.service.SpanishTts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random
import javax.inject.Inject

data class CrosswordWord(
    val id: Int,
    val spanish: String,
    val russian: String,
    val x: Int,
    val y: Int,
    val isVertical: Boolean,
    val number: Int
)

data class CrosswordGameState(
    val level: Int = 1,
    val words: List<CrosswordWord> = emptyList(),
    val grid: Map<Pair<Int, Int>, Char?> = emptyMap(),
    val gridSize: Int = 10,
    val selectedCell: Pair<Int, Int>? = null,
    val currentWordId: Int? = null,
    val solvedWordIds: Set<Int> = emptySet(),
    val errors: Set<Pair<Int, Int>> = emptySet(),
    val coins: Int = 0,
    val isGameOver: Boolean = false,
    val showSetup: Boolean = true,
    val levelStars: Map<Int, Int> = emptyMap(),
    val mistakesInCurrentLevel: Int = 0,
    val hintsUsedInCurrentLevel: Int = 0
)

// Internal helper used only during crossword construction
private data class PlacedWord(
    val word: String,
    val translation: String,
    val x: Int,
    val y: Int,
    val isVertical: Boolean
) {
    fun cells(): Map<Pair<Int, Int>, Char> = buildMap {
        word.indices.forEach { i ->
            val coords = if (isVertical) x to (y + i) else (x + i) to y
            put(coords, word[i])
        }
    }
}

@HiltViewModel
class CrosswordViewModel @Inject constructor(
    private val wordDao: WordDao,
    private val userProgressDao: UserProgressDao,
    private val achievementManager: AchievementManager,
    private val tts: SpanishTts
) : ViewModel() {

    private val _state = MutableStateFlow(CrosswordGameState())
    val state = _state.asStateFlow()

    init { loadProgress() }

    private fun loadProgress() {
        viewModelScope.launch {
            val p = userProgressDao.getProgressOnce() ?: return@launch
            _state.value = _state.value.copy(
                coins = p.totalXp,
                levelStars = (1..(p.totalXp / 50).coerceAtMost(100)).associateWith { 3 }
            )
        }
    }

    private fun saveCoinsToDb(amountChange: Int) {
        viewModelScope.launch {
            val p = userProgressDao.getProgressOnce() ?: return@launch
            val newXp = (p.totalXp + amountChange).coerceAtLeast(0)
            userProgressDao.update(p.copy(totalXp = newXp))
            _state.value = _state.value.copy(coins = newXp)
        }
    }

    fun startLevel(level: Int) {
        viewModelScope.launch {
            val gridSize = if (level > 10) 12 else 10
            val words = generateLevelFromDictionary(level, gridSize)
            val initialGrid = mutableMapOf<Pair<Int, Int>, Char?>()
            words.forEach { cw ->
                for (i in cw.spanish.indices) {
                    val coords = if (cw.isVertical) cw.x to (cw.y + i) else (cw.x + i) to cw.y
                    initialGrid[coords] = null
                }
            }
            _state.value = _state.value.copy(
                level = level,
                words = words,
                grid = initialGrid,
                gridSize = gridSize,
                selectedCell = null,
                currentWordId = null,
                solvedWordIds = emptySet(),
                errors = emptySet(),
                isGameOver = false,
                showSetup = false,
                mistakesInCurrentLevel = 0,
                hintsUsedInCurrentLevel = 0
            )
        }
    }

    // ── Crossword generation from dictionary ──────────────────────────────

    private suspend fun generateLevelFromDictionary(level: Int, gridSize: Int): List<CrosswordWord> {
        // Seed is fixed per level → same level always produces the same crossword
        val rng = Random(seed = level.toLong() * 31337L)

        val cefr = when {
            level <= 20 -> "A1"
            level <= 50 -> "A2"
            level <= 80 -> "B1"
            else -> "B2"
        }
        val targetWords = when {
            level <= 5  -> 4
            level <= 15 -> 5
            else        -> 6
        }
        val maxWordLen = if (level <= 10) 6 else 8

        // Deterministic fetch: ORDER BY id ASC, then shuffle with seeded rng
        val rawWords = wordDao.getWordsOrdered(500)
            .let { all ->
                val cefrFiltered = all.filter { it.level == cefr }
                if (cefrFiltered.size >= 40) cefrFiltered else all
            }
            .map { it.spanish.uppercase().trim() to it.russian }
            .filter { (sp, _) -> sp.length in 3..maxWordLen && sp.all { c -> c.isLetter() } }
            .distinctBy { it.first }
            .shuffled(rng)

        if (rawWords.size < 8) return staticFallback()

        // Multiple attempts with the same rng → sequence of attempts is also deterministic
        repeat(50) {
            val result = buildCrossword(rawWords, gridSize, targetWords, rng)
            if (result != null) return result
        }

        return staticFallback()
    }

    private fun buildCrossword(
        pool: List<Pair<String, String>>,
        gridSize: Int,
        targetWords: Int,
        rng: Random
    ): List<CrosswordWord>? {
        // Pick a first word 4-6 letters, place it horizontally at grid center
        val first = pool.firstOrNull { it.first.length in 4..6 } ?: return null
        val startX = (gridSize - first.first.length) / 2
        val startY = gridSize / 2
        val pw1 = PlacedWord(first.first, first.second, startX, startY, false)
        val placed = mutableListOf(pw1)
        val usedCells = pw1.cells().toMutableMap()

        val remaining = pool.filter { it.first != first.first }.shuffled(rng)
        for (candidate in remaining) {
            if (placed.size >= targetWords) break
            val pw = tryAddToGrid(candidate, placed, usedCells, gridSize, rng) ?: continue
            placed.add(pw)
            usedCells.putAll(pw.cells())
        }

        if (placed.size < 4) return null

        return placed.mapIndexed { idx, pw ->
            CrosswordWord(idx + 1, pw.word, pw.translation, pw.x, pw.y, pw.isVertical, idx + 1)
        }
    }

    private fun tryAddToGrid(
        candidate: Pair<String, String>,
        placed: List<PlacedWord>,
        usedCells: Map<Pair<Int, Int>, Char>,
        gridSize: Int,
        rng: Random
    ): PlacedWord? {
        val (word, translation) = candidate
        for (anchor in placed.shuffled(rng)) {
            val newIsVertical = !anchor.isVertical
            for (anchorIdx in anchor.word.indices.shuffled(rng)) {
                val charNeeded = anchor.word[anchorIdx]
                for (wordIdx in word.indices) {
                    if (word[wordIdx] != charNeeded) continue
                    val (nx, ny) = if (anchor.isVertical) {
                        (anchor.x - wordIdx) to (anchor.y + anchorIdx)
                    } else {
                        (anchor.x + anchorIdx) to (anchor.y - wordIdx)
                    }
                    if (isValidPlacement(word, nx, ny, newIsVertical, usedCells, gridSize)) {
                        return PlacedWord(word, translation, nx, ny, newIsVertical)
                    }
                }
            }
        }
        return null
    }

    private fun isValidPlacement(
        word: String,
        x: Int, y: Int,
        isVertical: Boolean,
        usedCells: Map<Pair<Int, Int>, Char>,
        gridSize: Int
    ): Boolean {
        if (x < 0 || y < 0) return false
        val endX = if (isVertical) x else x + word.length - 1
        val endY = if (isVertical) y + word.length - 1 else y
        if (endX >= gridSize || endY >= gridSize) return false

        // Endpoints along the word's axis must be clear
        val beforeCell = if (isVertical) x to (y - 1) else (x - 1) to y
        val afterCell  = if (isVertical) x to (y + word.length) else (x + word.length) to y
        if (usedCells.containsKey(beforeCell)) return false
        if (usedCells.containsKey(afterCell))  return false

        var hasIntersection = false
        for (i in word.indices) {
            val cx = if (isVertical) x else x + i
            val cy = if (isVertical) y + i else y
            val existing = usedCells[cx to cy]
            if (existing != null) {
                if (existing != word[i]) return false  // character conflict
                hasIntersection = true
                continue  // valid intersection — skip adjacency check for this cell
            }
            // New cell: perpendicular neighbors must be empty (no parallel word adjacency)
            if (isVertical) {
                if (usedCells.containsKey((cx - 1) to cy)) return false
                if (usedCells.containsKey((cx + 1) to cy)) return false
            } else {
                if (usedCells.containsKey(cx to (cy - 1))) return false
                if (usedCells.containsKey(cx to (cy + 1))) return false
            }
        }
        return hasIntersection
    }

    // Emergency fallback — only reached when dictionary has fewer than 8 suitable words
    private fun staticFallback(): List<CrosswordWord> = listOf(
        CrosswordWord(1, "MESA",  "Стол",    0, 2, false, 1),
        CrosswordWord(2, "AMOR",  "Любовь",  1, 0, true,  2),
        CrosswordWord(3, "ROPA",  "Одежда",  1, 3, false, 3),
        CrosswordWord(4, "AGUA",  "Вода",    4, 3, true,  4)
    )

    // ── Game interaction ──────────────────────────────────────────────────

    fun onCellClick(x: Int, y: Int) {
        if (!_state.value.grid.containsKey(x to y)) return
        val candidateWords = _state.value.words.filter { cw ->
            cw.spanish.indices.any { i ->
                val wx = if (cw.isVertical) cw.x else cw.x + i
                val wy = if (cw.isVertical) cw.y + i else cw.y
                wx == x && wy == y
            }
        }
        val currentId = _state.value.currentWordId
        val nextWord = if (candidateWords.size > 1 && candidateWords.any { it.id == currentId }) {
            candidateWords.first { it.id != currentId }
        } else {
            candidateWords.firstOrNull()
        }
        _state.value = _state.value.copy(selectedCell = x to y, currentWordId = nextWord?.id)
    }

    fun enterLetter(char: Char) {
        val cell = _state.value.selectedCell ?: return
        if (isCellSolved(cell.first, cell.second)) return

        val correctChar = getCorrectCharForCell(cell)
        val newMistakes = if (char.uppercaseChar() != correctChar) {
            _state.value.mistakesInCurrentLevel + 1
        } else {
            _state.value.mistakesInCurrentLevel
        }

        val newGrid = _state.value.grid.toMutableMap()
        newGrid[cell] = char.uppercaseChar()
        val newErrors = _state.value.errors.toMutableSet().also { it.remove(cell) }

        _state.value = _state.value.copy(grid = newGrid, errors = newErrors, mistakesInCurrentLevel = newMistakes)
        checkWordSolved()
        moveToNextCell()
        checkWin()
    }

    private fun checkWordSolved() {
        val s = _state.value
        val solved = s.solvedWordIds.toMutableSet()
        var newlySolved = false
        s.words.forEach { cw ->
            if (cw.id !in solved) {
                val isComplete = cw.spanish.indices.all { i ->
                    val coords = if (cw.isVertical) cw.x to (cw.y + i) else (cw.x + i) to cw.y
                    s.grid[coords]?.uppercaseChar() == cw.spanish[i].uppercaseChar()
                }
                if (isComplete) {
                    solved.add(cw.id)
                    tts.speak(cw.spanish)
                    newlySolved = true
                }
            }
        }
        if (newlySolved) _state.value = _state.value.copy(solvedWordIds = solved)
    }

    private fun moveToNextCell() {
        val s = _state.value
        val cw = s.words.find { it.id == s.currentWordId } ?: return
        val cell = s.selectedCell ?: return
        val foundIndex = cw.spanish.indices.firstOrNull { i ->
            val wx = if (cw.isVertical) cw.x else cw.x + i
            val wy = if (cw.isVertical) cw.y + i else cw.y
            wx == cell.first && wy == cell.second
        } ?: return

        for (nextIdx in (foundIndex + 1) until cw.spanish.length) {
            val nx = if (cw.isVertical) cw.x else cw.x + nextIdx
            val ny = if (cw.isVertical) cw.y + nextIdx else cw.y
            if (!isCellSolved(nx, ny)) {
                _state.value = _state.value.copy(selectedCell = nx to ny)
                return
            }
        }
    }

    fun deleteLetter() {
        val cell = _state.value.selectedCell ?: return
        if (isCellSolved(cell.first, cell.second)) return
        val newGrid = _state.value.grid.toMutableMap()
        newGrid[cell] = null
        _state.value = _state.value.copy(grid = newGrid)
        moveToPrevCell()
    }

    private fun moveToPrevCell() {
        val s = _state.value
        val cw = s.words.find { it.id == s.currentWordId } ?: return
        val cell = s.selectedCell ?: return
        val foundIndex = cw.spanish.indices.firstOrNull { i ->
            val wx = if (cw.isVertical) cw.x else cw.x + i
            val wy = if (cw.isVertical) cw.y + i else cw.y
            wx == cell.first && wy == cell.second
        } ?: return

        for (prevIdx in (foundIndex - 1) downTo 0) {
            val nx = if (cw.isVertical) cw.x else cw.x + prevIdx
            val ny = if (cw.isVertical) cw.y + prevIdx else cw.y
            if (!isCellSolved(nx, ny)) {
                _state.value = _state.value.copy(selectedCell = nx to ny)
                return
            }
        }
    }

    private fun isCellSolved(x: Int, y: Int): Boolean {
        val s = _state.value
        return s.words.any { cw ->
            cw.id in s.solvedWordIds && cw.spanish.indices.any { i ->
                val wx = if (cw.isVertical) cw.x else cw.x + i
                val wy = if (cw.isVertical) cw.y + i else cw.y
                wx == x && wy == y
            }
        }
    }

    fun useHintLetter() {
        val s = _state.value
        val cell = s.selectedCell ?: return
        if (s.coins < 10) return
        val correctChar = getCorrectCharForCell(cell) ?: return
        val newGrid = s.grid.toMutableMap()
        newGrid[cell] = correctChar
        _state.value = s.copy(grid = newGrid, errors = s.errors - cell, hintsUsedInCurrentLevel = s.hintsUsedInCurrentLevel + 1)
        saveCoinsToDb(-10)
        checkWordSolved()
        checkWin()
    }

    fun useHintCheck() {
        val s = _state.value
        if (s.coins < 20) return
        val newErrors = s.grid.entries
            .filter { (cell, char) ->
                char != null && char.uppercaseChar() != getCorrectCharForCell(cell)
            }
            .map { it.key }
            .toSet()
        _state.value = s.copy(errors = newErrors, hintsUsedInCurrentLevel = s.hintsUsedInCurrentLevel + 1)
        saveCoinsToDb(-20)
    }

    fun useHintWord() {
        val s = _state.value
        val cw = s.words.find { it.id == s.currentWordId } ?: return
        if (s.coins < 50) return
        val newGrid = s.grid.toMutableMap()
        val newErrors = s.errors.toMutableSet()
        for (i in cw.spanish.indices) {
            val coords = if (cw.isVertical) cw.x to (cw.y + i) else (cw.x + i) to cw.y
            newGrid[coords] = cw.spanish[i].uppercaseChar()
            newErrors.remove(coords)
        }
        _state.value = s.copy(grid = newGrid, errors = newErrors, hintsUsedInCurrentLevel = s.hintsUsedInCurrentLevel + 3)
        saveCoinsToDb(-50)
        checkWordSolved()
        checkWin()
    }

    private fun getCorrectCharForCell(cell: Pair<Int, Int>): Char? {
        _state.value.words.forEach { cw ->
            for (i in cw.spanish.indices) {
                val wx = if (cw.isVertical) cw.x else cw.x + i
                val wy = if (cw.isVertical) cw.y + i else cw.y
                if (wx == cell.first && wy == cell.second) return cw.spanish[i].uppercaseChar()
            }
        }
        return null
    }

    private fun checkWin() {
        val s = _state.value
        val isComplete = s.grid.isNotEmpty() && s.grid.all { (cell, char) ->
            char != null && char.uppercaseChar() == getCorrectCharForCell(cell)
        }
        if (!isComplete) return

        val stars = calculateStars(s.mistakesInCurrentLevel, s.hintsUsedInCurrentLevel)
        val bonusCoins = stars * 15
        val updatedStars = s.levelStars.toMutableMap()
        updatedStars[s.level] = maxOf(updatedStars[s.level] ?: 0, stars)
        _state.value = s.copy(isGameOver = true, levelStars = updatedStars)
        saveCoinsToDb(bonusCoins)
        viewModelScope.launch { achievementManager.checkAndUnlock() }
    }

    private fun calculateStars(mistakes: Int, hints: Int): Int {
        val penalty = mistakes + hints * 2
        return when {
            penalty == 0  -> 3
            penalty <= 3  -> 2
            else          -> 1
        }
    }

    fun resetToMenu() {
        _state.value = _state.value.copy(showSetup = true, isGameOver = false)
        loadProgress()
    }
}
