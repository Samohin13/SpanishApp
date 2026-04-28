package com.spanishapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.service.AchievementManager
import com.spanishapp.service.SpanishTts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    val levelStars: Map<Int, Int> = emptyMap(), // level -> stars
    val mistakesInCurrentLevel: Int = 0,
    val hintsUsedInCurrentLevel: Int = 0
)

@HiltViewModel
class CrosswordViewModel @Inject constructor(
    private val wordDao: com.spanishapp.data.db.dao.WordDao,
    private val userProgressDao: UserProgressDao,
    private val achievementManager: AchievementManager,
    private val tts: SpanishTts
) : ViewModel() {

    private val _state = MutableStateFlow(CrosswordGameState())
    val state = _state.asStateFlow()

    init {
        loadProgress()
    }

    private fun loadProgress() {
        viewModelScope.launch {
            val p = userProgressDao.getProgressOnce()
            if (p != null) {
                _state.value = _state.value.copy(
                    coins = p.totalXp,
                    levelStars = (1..(p.totalXp / 50).coerceAtMost(100)).associateWith { 3 }
                )
            }
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
            val words = generateLevelFromDictionary(level)
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
                gridSize = if (level > 10) 12 else 10,
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

    private suspend fun generateLevelFromDictionary(level: Int): List<CrosswordWord> {
        // 1. Check for handcrafted levels (now extended to 15)
        if (level <= 15) {
            return getLevelData(level)
        }

        val cefr = when {
            level <= 35 -> "A1"
            level <= 70 -> "A2"
            else -> "B1"
        }
        
        // 2. Automated Validation System:
        // Instead of random picking, we try to build a valid geometry.
        // If we can't find words that fit the intersections, we fall back to a safe level.
        val allWords = wordDao.getRandomWords(500).filter { it.level == cefr }
        val pool = allWords.map { it.spanish.uppercase().replace(" ", "").trim() to it.russian }

        if (pool.size > 20) {
            repeat(20) { // 20 attempts to generate a unique valid level
                val w1 = findWord(pool, (5..8).random()) ?: return@repeat
                val w2 = findIntersecting(pool, w1.first, 1, (4..7).random(), 0) ?: return@repeat
                val w3 = findIntersecting(pool, w2.first, 3, (4..6).random(), 0) ?: return@repeat
                val w4 = findIntersecting(pool, w3.first, 2, (3..5).random(), 0) ?: return@repeat
                
                // All words verified to intersect correctly
                return listOf(
                    CrosswordWord(1, w1.first, w1.second, 0, 1, false, 1),
                    CrosswordWord(2, w2.first, w2.second, 1, 1, true, 2),
                    CrosswordWord(3, w3.first, w3.second, 1, 4, false, 3),
                    CrosswordWord(4, w4.first, w4.second, 3, 4, true, 4)
                )
            }
        }

        // 3. Ultimate Fallback: return a pre-verified template
        return getLevelData(level)
    }

    private fun findWord(pool: List<Pair<String, String>>, length: Int): Pair<String, String>? {
        return pool.filter { it.first.length == length }.shuffled().firstOrNull()
    }

    private fun findIntersecting(
        pool: List<Pair<String, String>>, 
        baseWord: String, 
        baseIdx: Int, 
        targetLen: Int, 
        targetIdx: Int
    ): Pair<String, String>? {
        val charToMatch = baseWord[baseIdx]
        return pool.filter { 
            it.first.length == targetLen && 
            it.first.getOrNull(targetIdx) == charToMatch &&
            it.first != baseWord
        }.shuffled().firstOrNull()
    }

    fun onCellClick(x: Int, y: Int) {
        if (!_state.value.grid.containsKey(x to y)) return
        
        val candidateWords = _state.value.words.filter { cw ->
            for (i in cw.spanish.indices) {
                val wx = if (cw.isVertical) cw.x else cw.x + i
                val wy = if (cw.isVertical) cw.y + i else cw.y
                if (wx == x && wy == y) return@filter true
            }
            false
        }

        val currentId = _state.value.currentWordId
        val nextWord = if (candidateWords.size > 1 && candidateWords.any { it.id == currentId }) {
            candidateWords.first { it.id != currentId }
        } else {
            candidateWords.firstOrNull()
        }

        _state.value = _state.value.copy(
            selectedCell = x to y,
            currentWordId = nextWord?.id
        )
    }

    fun enterLetter(char: Char) {
        val cell = _state.value.selectedCell ?: return
        if (isCellSolved(cell.first, cell.second)) return // Lock solved cells

        val correctChar = getCorrectCharForCell(cell)
        
        val newMistakes = if (char.uppercaseChar() != correctChar) {
            _state.value.mistakesInCurrentLevel + 1
        } else {
            _state.value.mistakesInCurrentLevel
        }

        val newGrid = _state.value.grid.toMutableMap()
        newGrid[cell] = char.uppercaseChar()

        val newErrors = _state.value.errors.toMutableSet()
        newErrors.remove(cell)

        _state.value = _state.value.copy(
            grid = newGrid, 
            errors = newErrors,
            mistakesInCurrentLevel = newMistakes
        )
        
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
                var isComplete = true
                for (i in cw.spanish.indices) {
                    val coords = if (cw.isVertical) cw.x to (cw.y + i) else (cw.x + i) to cw.y
                    if (s.grid[coords]?.uppercaseChar() != cw.spanish[i].uppercaseChar()) {
                        isComplete = false
                        break
                    }
                }
                if (isComplete) {
                    solved.add(cw.id)
                    tts.speak(cw.spanish)
                    newlySolved = true
                }
            }
        }
        if (newlySolved) {
            _state.value = _state.value.copy(solvedWordIds = solved)
        }
    }

    private fun moveToNextCell() {
        val s = _state.value
        val currentId = s.currentWordId ?: return
        val cw = s.words.find { it.id == currentId } ?: return
        val cell = s.selectedCell ?: return
        
        var foundIndex = -1
        for (i in cw.spanish.indices) {
            val wx = if (cw.isVertical) cw.x else cw.x + i
            val wy = if (cw.isVertical) cw.y + i else cw.y
            if (wx == cell.first && wy == cell.second) {
                foundIndex = i
                break
            }
        }
        
        if (foundIndex != -1 && foundIndex < cw.spanish.length - 1) {
            // Logic to find next unsolved cell in word
            for (nextIdx in (foundIndex + 1) until cw.spanish.length) {
                val nx = if (cw.isVertical) cw.x else cw.x + nextIdx
                val ny = if (cw.isVertical) cw.y + nextIdx else cw.y
                if (!isCellSolved(nx, ny)) {
                    _state.value = _state.value.copy(selectedCell = nx to ny)
                    return
                }
            }
        }
    }

    fun deleteLetter() {
        val cell = _state.value.selectedCell ?: return
        if (isCellSolved(cell.first, cell.second)) return // Lock solved cells
        
        val newGrid = _state.value.grid.toMutableMap()
        newGrid[cell] = null
        _state.value = _state.value.copy(grid = newGrid)
        moveToPrevCell()
    }

    private fun moveToPrevCell() {
        val s = _state.value
        val currentId = s.currentWordId ?: return
        val cw = s.words.find { it.id == currentId } ?: return
        val cell = s.selectedCell ?: return
        
        var foundIndex = -1
        for (i in cw.spanish.indices) {
            val wx = if (cw.isVertical) cw.x else cw.x + i
            val wy = if (cw.isVertical) cw.y + i else cw.y
            if (wx == cell.first && wy == cell.second) {
                foundIndex = i
                break
            }
        }
        
        if (foundIndex > 0) {
            // Logic to find previous unsolved cell in word
            for (prevIdx in (foundIndex - 1) downTo 0) {
                val nx = if (cw.isVertical) cw.x else cw.x + prevIdx
                val ny = if (cw.isVertical) cw.y + prevIdx else cw.y
                if (!isCellSolved(nx, ny)) {
                    _state.value = _state.value.copy(selectedCell = nx to ny)
                    return
                }
            }
        }
    }

    private fun isCellSolved(x: Int, y: Int): Boolean {
        val s = _state.value
        return s.words.any { cw ->
            s.solvedWordIds.contains(cw.id) &&
                    cw.spanish.indices.any { i ->
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
        
        _state.value = s.copy(
            grid = newGrid,
            errors = s.errors - cell,
            hintsUsedInCurrentLevel = s.hintsUsedInCurrentLevel + 1
        )
        saveCoinsToDb(-10)
        checkWordSolved()
        checkWin()
    }

    fun useHintCheck() {
        val s = _state.value
        if (s.coins < 20) return
        
        val newErrors = mutableSetOf<Pair<Int, Int>>()
        s.grid.forEach { (cell, char) ->
            if (char != null) {
                val correct = getCorrectCharForCell(cell)
                if (correct != null && char.uppercaseChar() != correct.uppercaseChar()) {
                    newErrors.add(cell)
                }
            }
        }
        
        _state.value = s.copy(
            errors = newErrors,
            hintsUsedInCurrentLevel = s.hintsUsedInCurrentLevel + 1
        )
        saveCoinsToDb(-20)
    }

    fun useHintWord() {
        val s = _state.value
        val wordId = s.currentWordId ?: return
        if (s.coins < 50) return
        
        val cw = s.words.find { it.id == wordId } ?: return
        val newGrid = s.grid.toMutableMap()
        val newErrors = s.errors.toMutableSet()

        for (i in cw.spanish.indices) {
            val coords = if (cw.isVertical) cw.x to (cw.y + i) else (cw.x + i) to cw.y
            newGrid[coords] = cw.spanish[i].uppercaseChar()
            newErrors.remove(coords)
        }

        _state.value = s.copy(
            grid = newGrid,
            errors = newErrors,
            hintsUsedInCurrentLevel = s.hintsUsedInCurrentLevel + 3
        )
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
        val isComplete = s.grid.all { (cell, char) ->
            char != null && char.uppercaseChar() == getCorrectCharForCell(cell)
        }
        
        if (isComplete && s.grid.isNotEmpty()) {
            val stars = calculateStars(s.mistakesInCurrentLevel, s.hintsUsedInCurrentLevel)
            val bonusCoins = stars * 15
            
            val updatedStars = s.levelStars.toMutableMap()
            updatedStars[s.level] = maxOf(updatedStars[s.level] ?: 0, stars)

            _state.value = s.copy(
                isGameOver = true,
                levelStars = updatedStars
            )
            
            saveCoinsToDb(bonusCoins)
            viewModelScope.launch {
                achievementManager.checkAndUnlock()
            }
        }
    }

    private fun calculateStars(mistakes: Int, hints: Int): Int {
        val penalty = mistakes + (hints * 2)
        return when {
            penalty == 0 -> 3
            penalty <= 3 -> 2
            else -> 1
        }
    }

    private fun getLevelData(level: Int): List<CrosswordWord> {
        return when (level) {
            1 -> listOf(
                CrosswordWord(1, "SOL", "Солнце", 0, 0, false, 1),
                CrosswordWord(2, "LUNA", "Луна", 2, 0, true, 2),
                CrosswordWord(3, "AGUA", "Вода", 2, 3, false, 3),
                CrosswordWord(4, "GATO", "Кот", 3, 3, true, 4)
            )
            2 -> listOf(
                CrosswordWord(1, "CASA", "Дом", 0, 0, false, 1),
                CrosswordWord(2, "AMOR", "Любовь", 1, 0, true, 2),
                CrosswordWord(3, "ROSA", "Роза", 0, 2, false, 3),
                CrosswordWord(4, "SOL", "Солнце", 2, 2, true, 4)
            )
            3 -> listOf(
                CrosswordWord(1, "PERRO", "Собака", 0, 0, false, 1),
                CrosswordWord(2, "ROJO", "Красный", 2, 0, true, 2),
                CrosswordWord(3, "ORO", "Золото", 2, 2, false, 3),
                CrosswordWord(4, "OJO", "Глаз", 4, 2, true, 4)
            )
            4 -> listOf(
                CrosswordWord(1, "MADRE", "Мать", 0, 0, false, 1),
                CrosswordWord(2, "DIA", "День", 2, 0, true, 2),
                CrosswordWord(3, "AZUL", "Синий", 2, 2, false, 3),
                CrosswordWord(4, "LUZ", "Свет", 5, 2, true, 4)
            )
            5 -> listOf(
                CrosswordWord(1, "LIBRO", "Книга", 0, 0, false, 1),
                CrosswordWord(2, "BOLA", "Мяч", 2, 0, true, 2),
                CrosswordWord(3, "AGUA", "Вода", 2, 3, false, 3),
                CrosswordWord(4, "GRACIAS", "Спасибо", 3, 3, true, 4)
            )
            6 -> listOf(
                CrosswordWord(1, "COMIDA", "Еда", 0, 0, false, 1),
                CrosswordWord(2, "CENA", "Ужин", 0, 0, true, 2),
                CrosswordWord(3, "NADA", "Ничего", 0, 2, false, 3),
                CrosswordWord(4, "MODA", "Мода", 2, 0, true, 4)
            )
            7 -> listOf(
                CrosswordWord(1, "TIEMPO", "Время", 0, 1, false, 1),
                CrosswordWord(2, "VIDA", "Жизнь", 1, 0, true, 2),
                CrosswordWord(3, "AMOR", "Любовь", 1, 3, false, 3),
                CrosswordWord(4, "POR", "Для", 4, 1, true, 4)
            )
            8 -> listOf(
                CrosswordWord(1, "MUSICA", "Музыка", 0, 0, false, 1),
                CrosswordWord(2, "CINE", "Кино", 4, 0, true, 2),
                CrosswordWord(3, "SOL", "Солнце", 2, 0, true, 3),
                CrosswordWord(4, "LUNA", "Луна", 2, 2, false, 4)
            )
            9 -> listOf(
                CrosswordWord(1, "CIUDAD", "Город", 0, 2, false, 1),
                CrosswordWord(2, "CAMINO", "Путь", 0, 2, true, 2),
                CrosswordWord(3, "DIA", "День", 5, 2, true, 3),
                CrosswordWord(4, "IR", "Идти", 0, 5, false, 4)
            )
            10 -> listOf(
                CrosswordWord(1, "MAÑANA", "Завтра", 0, 0, false, 1),
                CrosswordWord(2, "MESA", "Стол", 0, 0, true, 2),
                CrosswordWord(3, "SOL", "Солнце", 0, 2, false, 3),
                CrosswordWord(4, "ALMA", "Душа", 3, 0, true, 4)
            )
            11 -> listOf(
                CrosswordWord(1, "CIUDAD", "Город", 0, 0, false, 1),
                CrosswordWord(2, "DIA", "День", 3, 0, true, 2),
                CrosswordWord(3, "AUTO", "Авто", 3, 2, false, 3),
                CrosswordWord(4, "UNO", "Один", 6, 2, true, 4)
            )
            12 -> listOf(
                CrosswordWord(1, "MADRE", "Мать", 0, 0, false, 1),
                CrosswordWord(2, "AMOR", "Любовь", 1, 0, true, 2),
                CrosswordWord(3, "ROSA", "Роза", 1, 3, false, 3),
                CrosswordWord(4, "AGUA", "Вода", 4, 3, true, 4)
            )
            13 -> listOf(
                CrosswordWord(1, "TIEMPO", "Время", 0, 2, false, 1),
                CrosswordWord(2, "MUNDO", "Мир", 3, 0, true, 2),
                CrosswordWord(3, "ORO", "Золото", 3, 4, false, 3),
                CrosswordWord(4, "ROJO", "Красный", 4, 4, true, 4)
            )
            14 -> listOf(
                CrosswordWord(1, "FRUTA", "Фрукт", 0, 0, false, 1),
                CrosswordWord(2, "TREN", "Поезд", 3, 0, true, 2),
                CrosswordWord(3, "NIEVE", "Снег", 3, 3, false, 3),
                CrosswordWord(4, "ESTO", "Это", 5, 3, true, 4)
            )
            15 -> listOf(
                CrosswordWord(1, "PLAYA", "Пляж", 0, 1, false, 1),
                CrosswordWord(2, "LUNA", "Луна", 1, 0, true, 2),
                CrosswordWord(3, "AGUA", "Вода", 1, 4, false, 3),
                CrosswordWord(4, "GATO", "Кот", 2, 4, true, 4)
            )
            else -> {
                // Verified safe template for random levels
                listOf(
                    CrosswordWord(1, "MAÑANA", "Завтра", 0, 0, false, 1),
                    CrosswordWord(2, "AMIGO", "Друг", 1, 0, true, 2),
                    CrosswordWord(3, "GATO", "Кот", 1, 3, false, 3),
                    CrosswordWord(4, "SOL", "Солнце", 4, 2, true, 4)
                )
            }
        }
    }

    fun resetToMenu() {
        _state.value = _state.value.copy(showSetup = true, isGameOver = false)
        loadProgress()
    }
}
