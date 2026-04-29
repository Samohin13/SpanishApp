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
            val gridSize = when {
                level <= 15 -> 10
                level <= 45 -> 11
                level <= 75 -> 12
                else        -> 13
            }
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
    //
    // Design: каждый уровень получает СВОЙ скользящий срез словаря.
    //   offset  = (level-1) * STEP   →  level 1 = слова 0-299,
    //                                    level 2 = слова 30-329, …
    //                                    level 100 = слова 2970-3269
    // Это гарантирует, что каждый уровень использует РАЗНЫЕ исходные слова,
    // а не только переставляет одно и то же множество.
    // Seed = level * prime ─ постоянство: один уровень = один кроссворд всегда.

    private suspend fun generateLevelFromDictionary(level: Int, gridSize: Int): List<CrosswordWord> {
        val rng = Random(seed = level.toLong() * 31337L)

        // ── Шкала сложности 1→100 ──────────────────────────────────────────
        val targetWords = when {
            level <= 5  -> 4
            level <= 20 -> 5
            else        -> 6
        }
        // Диапазон длин слов. Нижняя граница — 3 для всех уровней, чтобы
        // в пуле всегда было достаточно кандидатов для пересечений.
        val wordLenRange: IntRange = when {
            level <= 15 -> 3..5
            level <= 35 -> 3..6
            level <= 60 -> 4..7
            level <= 80 -> 4..7
            else        -> 5..8
        }
        val anchorLenRange: IntRange = when {
            level <= 10 -> 3..4
            level <= 25 -> 4..5
            level <= 55 -> 4..6
            else        -> 5..7
        }

        // ── Эксклюзивное окно — нет CEFR-фильтра, только длина ───────────
        // Корень проблемы "одинаковых кроссвордов":
        //   CEFR-фильтр + узкий диапазон длин давали 10-15 кандидатов →
        //   генератор находил единственную валидную комбинацию при любом seed.
        //
        // Решение:
        //   • Убираем CEFR-фильтр — длина слова уже контролирует сложность.
        //   • STEP = 60 → каждые 60 слов в DB ≠ соседний уровень.
        //   • WINDOW = 180 → ~60-90 слов проходят length-фильтр,
        //     что даёт генератору тысячи уникальных комбинаций per level.
        val STEP   = 60
        val WINDOW = 180
        val offset = (level - 1) * STEP  // уровень 1→0, 2→60, 100→5940

        val windowWords = wordDao.getWordsOrderedWithOffset(WINDOW, offset % 4800)
            .map { it.spanish.uppercase().trim() to it.russian }
            .filter { (sp, _) -> sp.length in wordLenRange && sp.all { c -> c.isLetter() } }
            .distinctBy { it.first }

        val pool = if (windowWords.size >= 10) {
            windowWords.shuffled(rng)
        } else {
            // Окно разреженное (конец словаря): берём большой глобальный пул
            wordDao.getWordsOrderedWithOffset(WINDOW * 3, (offset / 3) % 4000)
                .map { it.spanish.uppercase().trim() to it.russian }
                .filter { (sp, _) -> sp.length in wordLenRange && sp.all { c -> c.isLetter() } }
                .distinctBy { it.first }
                .shuffled(rng)
        }

        if (pool.size < 8) return staticFallback()

        repeat(60) {
            val result = buildCrossword(pool, gridSize, targetWords, anchorLenRange, rng)
            if (result != null) return result
        }

        return staticFallback()
    }

    private fun buildCrossword(
        pool: List<Pair<String, String>>,
        gridSize: Int,
        targetWords: Int,
        anchorLenRange: IntRange,
        rng: Random
    ): List<CrosswordWord>? {
        // Якорное слово — горизонтально по центру сетки
        val first = pool.firstOrNull { it.first.length in anchorLenRange }
            ?: pool.firstOrNull { it.first.length in 3..8 }
            ?: return null
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

        val words = placed.mapIndexed { idx, pw ->
            CrosswordWord(idx + 1, pw.word, pw.translation, pw.x, pw.y, pw.isVertical, idx + 1)
        }

        // Финальная проверка геометрии через валидатор
        if (!CrosswordValidator.isValid(words, gridSize)) return null

        return words
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

    // Emergency fallback — only reached when dictionary has fewer than 8 suitable words.
    // Layout verified by hand (all intersections correct, grid 10x10):
    //   PATO horizontal (0,0): P(0,0) A(1,0) T(2,0) O(3,0)
    //   PLAYA vertical  (0,0): P(0,0) L(0,1) A(0,2) Y(0,3) A(0,4)
    //   AMOR horizontal (0,2): A(0,2) M(1,2) O(2,2) R(3,2)
    //   OTRO vertical   (3,0): O(3,0) T(3,1) R(3,2) O(3,3)
    // Intersections: PATO∩PLAYA@(0,0)=P, PATO∩OTRO@(3,0)=O,
    //                AMOR∩PLAYA@(0,2)=A, AMOR∩OTRO@(3,2)=R
    private fun staticFallback(): List<CrosswordWord> = listOf(
        CrosswordWord(1, "PATO",  "Утка",   0, 0, false, 1),
        CrosswordWord(2, "PLAYA", "Пляж",   0, 0, true,  2),
        CrosswordWord(3, "AMOR",  "Любовь", 0, 2, false, 3),
        CrosswordWord(4, "OTRO",  "Другой", 3, 0, true,  4)
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
