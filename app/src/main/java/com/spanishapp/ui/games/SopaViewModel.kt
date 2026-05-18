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
            val sopaCefr = sopaCefrFor(level)

            gameStartTime = SystemClock.elapsedRealtime()
            lastFindTime  = gameStartTime

            // v1.15.4: ДЕТЕРМИНИРОВАННАЯ генерация уровней.
            // Юзер: "проверь на дубликаты, чтобы каждый уровень был
            // уникальным и соответственно на сложность".
            //
            // Алгоритм:
            // 1. Pool слов CEFR группы — стабильный (sortedBy length + id),
            //    каждый раз одни и те же слова в одном порядке.
            // 2. Slice для конкретного уровня — берём окно индексом
            //    (levelInGroup * wordsPerLevel) по длине слов:
            //    уровень 1 = слова #0..#7 (короткие),
            //    уровень 2 = #8..#15, и т.д.
            // 3. Сложность растёт по длине слов внутри CEFR.
            // 4. Random(seed = level) для размещения на grid → одинаковая
            //    сетка для одного уровня (юзер может вернуться и узнать).
            val maxWordLen = config.gridSize - 1
            // v1.15.5: getWordsByLevelSync — синхронная функция Room.
            // Room запрещает её вызов с main thread (IllegalStateException
            // = краш приложения). viewModelScope.launch по default Main —
            // оборачиваем БД-вызов в Dispatchers.IO.
            val rawWords = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                sopaCefr.flatMap { wordDao.getWordsByLevelSync(it) }
            }
            val allPool = rawWords
                .filter { it.russian.isNotBlank() && it.spanish.isNotBlank() }
                .map {
                    SopaWord(
                        id          = it.id,
                        word        = stripArticle(it.spanish).uppercase().replace(" ", "").replace("-", ""),
                        translation = it.russian
                    )
                }
                .filter { it.word.length in 3..maxWordLen }
                .distinctBy { it.word }
                .sortedWith(compareBy({ it.word.length }, { it.id }))  // короткие → длинные

            if (allPool.isEmpty()) return@launch

            // Какой slice взять для этого уровня?
            val (groupStart, levelsInGroup) = groupForLevel(level)
            val levelInGroup = level - groupStart
            val startIdx = (levelInGroup * config.targetWords) % allPool.size
            // Берём wrap-around если выходим за конец pool
            val candidates = (allPool.drop(startIdx) + allPool.take(startIdx))
                .take(config.targetWords * 3)  // запас на случай если не уместятся

            // Random с seed = level для детерминированного размещения
            val seededRandom = Random(level.toLong())

            // ── Пытаемся уместить нужное число слов на сетке ───
            // v1.15.6: если не удалось разместить targetWords из исходного
            // slice, добиваем из расширенного pool (все слова CEFR кроме
            // уже использованных). Это гарантирует что юзер всегда увидит
            // ровно targetWords слов на grid (не "ghost" слова в panel).
            val grid = Array(config.gridSize) { CharArray(config.gridSize) { ' ' } }
            val placed = mutableListOf<SopaWord>()
            for (sw in candidates) {
                if (placed.size >= config.targetWords) break
                val path = mutableListOf<Pair<Int, Int>>()
                if (placeWordStraight(grid, sw.word, path, seededRandom)) {
                    placed.add(sw)
                }
            }
            // Fallback — если placed < targetWords, добиваем из всего pool
            if (placed.size < config.targetWords) {
                val placedIds = placed.map { it.id }.toSet()
                val fallback = allPool.filterNot { it.id in placedIds }
                for (sw in fallback) {
                    if (placed.size >= config.targetWords) break
                    val path = mutableListOf<Pair<Int, Int>>()
                    if (placeWordStraight(grid, sw.word, path, seededRandom)) {
                        placed.add(sw)
                    }
                }
            }
            if (placed.isEmpty()) return@launch
            fillEmptyCells(grid, seededRandom)

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
     * v1.15.2: Конфигурация уровней по требованию юзера.
     *  1-10  → 8 слов A1,  grid 10
     *  11-20 → 10 слов A1, grid 11
     *  21-40 → 14 слов A2, grid 13
     *  41-60 → 16 слов A2, grid 14
     *  61-80 → 18 слов B1, grid 15
     *  81-100→ 20 слов B2, grid 16
     * Таймер растёт с уровнем (нет таймера на туториале 1-10).
     */
    private fun configFor(level: Int): SopaLevelConfig = when {
        level <= 10  -> SopaLevelConfig(gridSize = 10, targetWords = 8,  timeSec = 0,   ghost = false)
        level <= 20  -> SopaLevelConfig(gridSize = 11, targetWords = 10, timeSec = 300, ghost = false)
        level <= 40  -> SopaLevelConfig(gridSize = 13, targetWords = 14, timeSec = 360, ghost = false)
        level <= 60  -> SopaLevelConfig(gridSize = 14, targetWords = 16, timeSec = 360, ghost = false)
        level <= 80  -> SopaLevelConfig(gridSize = 15, targetWords = 18, timeSec = 420, ghost = false)
        else         -> SopaLevelConfig(gridSize = 16, targetWords = 20, timeSec = 420, ghost = true)
    }

    /**
     * v1.15.2: CEFR pool по требованию юзера (для Sopa специфично,
     * не переиспользует общий LevelDifficulty.cefr).
     */
    private fun sopaCefrFor(level: Int): List<String> = when {
        level <= 20  -> listOf("A1")
        level <= 60  -> listOf("A2")
        level <= 80  -> listOf("B1")
        else         -> listOf("B2")
    }

    /**
     * v1.15.4: возвращает (firstLevelOfGroup, countInGroup) для уровня.
     * Используется для расчёта slice слов внутри CEFR группы.
     */
    private fun groupForLevel(level: Int): Pair<Int, Int> = when {
        level <= 10  -> 1 to 10   // A1 группа 1: уровни 1-10
        level <= 20  -> 11 to 10  // A1 группа 2: уровни 11-20
        level <= 40  -> 21 to 20  // A2 группа 1: уровни 21-40
        level <= 60  -> 41 to 20  // A2 группа 2: уровни 41-60
        level <= 80  -> 61 to 20  // B1: уровни 61-80
        else         -> 81 to 20  // B2: уровни 81-100
    }

    /**
     * v1.15.3: Размещение слова **прямой линией в 8 направлениях**
     * (классический word search / sopa de letras):
     *  →  ←  ↓  ↑  ↘  ↙  ↗  ↖
     *
     * Юзер: "слова должны быть расположены не чисто вертикально или
     * горизонтально". В v1.15.2 было только → и ↓ (слишком ограниченно).
     * Раньше использовалась змейка (snake) — слово могло извиваться
     * буквой Z, юзер не мог его найти. Теперь — прямая линия, но
     * любая из 8 осей. Это правильная механика word search.
     */
    private fun placeWordStraight(
        grid: Array<CharArray>,
        word: String,
        path: MutableList<Pair<Int, Int>>,
        rng: Random = Random,
    ): Boolean {
        val size = grid.size
        if (word.length > size) return false

        // 8 направлений: 4 ортогональные + 4 диагональные.
        val directions = listOf(
            0 to 1,   // →
            0 to -1,  // ←
            1 to 0,   // ↓
            -1 to 0,  // ↑
            1 to 1,   // ↘
            1 to -1,  // ↙
            -1 to 1,  // ↗
            -1 to -1, // ↖
        )

        var attempts = 0
        while (attempts < 400) {
            attempts++
            val dir = directions[rng.nextInt(directions.size)]
            val startR = rng.nextInt(size)
            val startC = rng.nextInt(size)
            // Проверяем что слово целиком умещается в направлении
            val endR = startR + dir.first * (word.length - 1)
            val endC = startC + dir.second * (word.length - 1)
            if (endR !in 0 until size || endC !in 0 until size) continue

            // Проверяем что все ячейки либо пусты, либо совпадают с буквой
            var canPlace = true
            for (i in word.indices) {
                val r = startR + dir.first * i
                val c = startC + dir.second * i
                if (grid[r][c] != ' ' && grid[r][c] != word[i]) {
                    canPlace = false
                    break
                }
            }
            if (!canPlace) continue

            // Размещаем
            path.clear()
            for (i in word.indices) {
                val r = startR + dir.first * i
                val c = startC + dir.second * i
                grid[r][c] = word[i]
                path.add(r to c)
            }
            return true
        }
        return false
    }

    private fun fillEmptyCells(grid: Array<CharArray>, rng: Random = Random) {
        val freq = "EEEEAAAAOOOOOSSSSRRRRNNNNIIIIIDDDDLLLLCCCCTTTTUUUUMMMM"
        val alphabet = ('A'..'Z').toList()
        for (r in grid.indices) for (c in grid[r].indices) {
            if (grid[r][c] == ' ') {
                grid[r][c] = if (rng.nextInt(100) < 85) freq[rng.nextInt(freq.length)]
                             else alphabet[rng.nextInt(alphabet.size)]
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
        val first = s.selectedCells.first()

        // v1.15.6: ЛИНЕЙНЫЙ режим (классика word search).
        // Раньше требовался шаг ровно 1 от последней клетки — при
        // быстром drag по диагонали палец перескакивал клетки и
        // выделение прерывалось (юзер: "GOL, BAR невозможно собрать").
        // Теперь от first до (r,c) автоматически строится прямая
        // линия в любом из 8 направлений.
        val deltaR = r - first.first
        val deltaC = c - first.second
        val absDr = abs(deltaR)
        val absDc = abs(deltaC)

        // Если current = first → reset до 1 клетки
        if (absDr == 0 && absDc == 0) {
            _state.value = s.copy(selectedCells = listOf(first))
            return
        }

        // Допустима только прямая линия (8 направлений):
        // - горизонталь (deltaR == 0)
        // - вертикаль (deltaC == 0)
        // - диагональ (absDr == absDc)
        val isStraight = deltaR == 0 || deltaC == 0 || absDr == absDc
        if (!isStraight) return

        // Строим линию от first до (r,c) шагом 1
        val len = kotlin.math.max(absDr, absDc) + 1
        val dirR = if (deltaR == 0) 0 else deltaR / absDr
        val dirC = if (deltaC == 0) 0 else deltaC / absDc

        val line = (0 until len).map { i ->
            (first.first + dirR * i) to (first.second + dirC * i)
        }

        if (line != s.selectedCells) {
            _state.value = s.copy(selectedCells = line)
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
