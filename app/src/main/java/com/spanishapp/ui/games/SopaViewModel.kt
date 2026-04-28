package com.spanishapp.ui.games

import android.os.SystemClock
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.dao.WordDao
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

enum class SopaDifficulty(val size: Int, val title: String) {
    PRINCIPIANTE(8, "Principiante"),
    INTERMEDIO(12, "Intermedio"),
    AVANZADO(15, "Avanzado")
}

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

data class SopaGameState(
    val grid: List<List<Char>> = emptyList(),
    val words: List<SopaWord> = emptyList(),
    val selectedCells: List<Pair<Int, Int>> = emptyList(),
    val foundWords: List<FoundWord> = emptyList(),
    val hintCells: Set<Pair<Int, Int>> = emptySet(),
    val isGameOver: Boolean = false,
    val difficulty: SopaDifficulty = SopaDifficulty.PRINCIPIANTE,
    val isGhostMode: Boolean = false,
    val hasTimer: Boolean = true,
    val score: Int = 0,
    val combo: Int = 0,
    val timeLeftSeconds: Int = 0,
    val showSetup: Boolean = true,
    val level: String = "A1"
)

@HiltViewModel
class SopaViewModel @Inject constructor(
    private val wordDao: WordDao,
    private val userProgressDao: UserProgressDao,
    private val achievementManager: AchievementManager,
    private val tts: SpanishTts
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

    fun startGame(level: String, difficulty: SopaDifficulty, isGhostMode: Boolean, hasTimer: Boolean) {
        viewModelScope.launch {
            gameStartTime = SystemClock.elapsedRealtime()
            lastFindTime = gameStartTime
            
            val targetCount = when(difficulty) {
                SopaDifficulty.PRINCIPIANTE -> 10
                SopaDifficulty.INTERMEDIO -> 15
                SopaDifficulty.AVANZADO -> 20
            }
            
            // 1. IMPROVED FETCHING: Only words with translation and non-empty spanish
            val wordsFromDb = wordDao.getRandomWords(200)
                .filter { it.level == level && it.russian.isNotBlank() && it.spanish.isNotBlank() }
            
            val processedWords = wordsFromDb.map { 
                SopaWord(
                    id = it.id,
                    word = stripArticle(it.spanish).uppercase().replace(" ", "").replace("-", ""),
                    translation = it.russian
                )
            }.filter { it.word.length in 3..difficulty.size }.take(targetCount)

            if (processedWords.isEmpty()) return@launch

            val size = difficulty.size
            val grid = Array(size) { CharArray(size) { ' ' } }
            val wordPositions = mutableMapOf<String, List<Pair<Int, Int>>>()
            
            processedWords.forEach { sopaWord ->
                val path = mutableListOf<Pair<Int, Int>>()
                if (placeWordSnake(grid, sopaWord.word, path)) {
                    wordPositions[sopaWord.word] = path.toList()
                }
            }

            fillEmptyCells(grid)

            _state.value = SopaGameState(
                grid = grid.map { it.toList() },
                words = processedWords,
                difficulty = difficulty,
                isGhostMode = isGhostMode,
                hasTimer = hasTimer,
                showSetup = false,
                level = level,
                timeLeftSeconds = if (difficulty == SopaDifficulty.PRINCIPIANTE) 240 else if (difficulty == SopaDifficulty.INTERMEDIO) 400 else 600
            )
            
            if (hasTimer) startTimer()
        }
    }

    private fun placeWordSnake(grid: Array<CharArray>, word: String, path: MutableList<Pair<Int, Int>>): Boolean {
        val size = grid.size
        var placed = false
        var attempts = 0
        
        while (!placed && attempts < 150) {
            attempts++
            val startR = Random.nextInt(size)
            val startC = Random.nextInt(size)
            
            path.clear()
            if (findPath(grid, word, 0, startR, startC, path)) {
                path.forEachIndexed { i, pos ->
                    grid[pos.first][pos.second] = word[i]
                }
                placed = true
            }
        }
        return placed
    }

    private fun findPath(grid: Array<CharArray>, word: String, index: Int, r: Int, c: Int, path: MutableList<Pair<Int, Int>>): Boolean {
        if (index == word.length) return true
        if (r !in grid.indices || c !in grid[0].indices) return false
        if (grid[r][c] != ' ') return false
        if (path.contains(r to c)) return false

        path.add(r to c)
        val dirs = listOf(0 to 1, 0 to -1, 1 to 0, -1 to 0, 1 to 1, 1 to -1, -1 to 1, -1 to -1).shuffled()
        for (dir in dirs) {
            if (findPath(grid, word, index + 1, r + dir.first, c + dir.second, path)) return true
        }
        path.removeAt(path.size - 1)
        return false
    }

    private fun fillEmptyCells(grid: Array<CharArray>) {
        val spanishFreq = "EEEEAAAAAOOOOOOSSSSSRRRRRNNNNNIIIIIDDDDDLLLLLCCCCCTTTTTUUUUUMMMMM".uppercase()
        for (r in grid.indices) {
            for (c in grid[r].indices) {
                if (grid[r][c] == ' ') grid[r][c] = if (Random.nextInt(100) < 85) spanishFreq.random() else ('A'..'Z').random()
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
            val now = SystemClock.elapsedRealtime()
            val isCombo = now - lastFindTime < 5000
            lastFindTime = now
            val newCombo = if (isCombo) s.combo + 1 else 1
            val assignedColor = wordColors[s.foundWords.size % wordColors.size]
            val newWords = s.words.toMutableList()
            newWords[foundWordIndex] = foundWord.copy(isFound = true, findTime = now, color = assignedColor)
            val newFoundWords = s.foundWords + FoundWord(foundWord.word, cells, assignedColor)
            
            // Clear hints when word is found
            val newHintCells = s.hintCells.filterNot { cells.contains(it) }.toSet()
            
            _state.value = s.copy(
                words = newWords, 
                foundWords = newFoundWords, 
                hintCells = newHintCells,
                score = s.score + 15 * newCombo, 
                combo = newCombo
            )
            if (newWords.all { it.isFound }) finishGame()
        }
    }

    private fun finishGame() {
        val s = _state.value
        val bonus = if (s.hasTimer) s.timeLeftSeconds / 5 else 0
        _state.value = s.copy(isGameOver = true, score = s.score + bonus)
        viewModelScope.launch {
            var lastTime = gameStartTime
            s.words.filter { it.isFound }.sortedBy { it.findTime }.forEach { word ->
                val timeTaken = word.findTime - lastTime
                lastTime = word.findTime
                if (timeTaken > 25000) {
                    wordDao.getById(word.id)?.let { entity ->
                        wordDao.update(entity.copy(easeFactor = (entity.easeFactor - 0.2f).coerceAtLeast(1.3f), nextReview = System.currentTimeMillis()))
                    }
                }
            }
            val p = userProgressDao.getProgressOnce() ?: return@launch
            userProgressDao.update(p.copy(totalXp = p.totalXp + (s.score / 6).coerceIn(15, 60)))
            achievementManager.checkAndUnlock()
        }
    }

    // 4. IMPROVED HINT: Now it actually highlights the first letter visually
    fun useHint() {
        val s = _state.value
        if (s.score < 30) return
        val targetWord = s.words.find { !it.isFound } ?: return
        
        // Let's find the first character of the word in the grid that isn't already hinted
        for (r in 0 until s.difficulty.size) {
            for (c in 0 until s.difficulty.size) {
                if (s.grid[r][c] == targetWord.word[0]) {
                    // Make sure it's not already found
                    if (s.foundWords.none { it.cells.contains(r to c) } && !s.hintCells.contains(r to c)) {
                        _state.value = s.copy(
                            score = s.score - 30,
                            hintCells = s.hintCells + (r to c)
                        )
                        return
                    }
                }
            }
        }
    }

    // 3. FUNCTIONAL RESET: Explicitly clears selection
    fun clearSelection() {
        _state.value = _state.value.copy(selectedCells = emptyList())
    }

    // 2. ROBUST ARTICLE STRIPPING: Handles various cases and extra spaces
    private fun stripArticle(s: String): String {
        val regex = Regex("^(el|la|los|las|un|una|unos|unas)\\s+", RegexOption.IGNORE_CASE)
        return s.trim().replace(regex, "").trim()
    }
}
