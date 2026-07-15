package com.spanishapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.UserProgressDao
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

/** Плитка на экране. id нужен из-за возможных слов-дублей во фразе. */
data class FraseTile(
    val id: Int,
    val word: String,
    val used: Boolean = false,
)

data class FraseLocaState(
    val level: Int = 1,
    val themeTitle: String = "",
    val cefr: String = "A1",
    /** Русский промпт текущей фразы. */
    val promptRu: String = "",
    /** Правильные токены текущей фразы (для проверки и результата). */
    val tokens: List<String> = emptyList(),
    /** Плитки: токены + ловушки, перемешанные детерминированно. */
    val tiles: List<FraseTile> = emptyList(),
    /** Слова, уже поставленные в строку ответа (по порядку). */
    val placed: List<String> = emptyList(),
    /** Активные ловушки текущей фразы: слово → объяснение. */
    val traps: Map<String, String> = emptyMap(),
    val lives: Int = 3,
    /** Объяснение ловушки, на которую наступил игрок (баннер). */
    val trapMessage: String? = null,
    /** Счётчик неверных тапов — триггер тряски. */
    val wrongTapCount: Int = 0,
    val score: Int = 0,
    /** Сколько фраз собрано чисто (без единой ошибки). */
    val cleanCount: Int = 0,
    val streak: Int = 0,
    val currentRound: Int = 0,
    val totalRounds: Int = 4,
    /** История фраз для ProgressDots: true = собрана чисто. */
    val answerHistory: List<Boolean> = emptyList(),
    /** Фраза собрана — пауза-фидбэк перед следующей. */
    val phraseSolved: Boolean = false,
    val isGameOver: Boolean = false,
    val finalStars: Int = 0,
    val finalPercent: Int = 0,
    val showLevelMap: Boolean = true,
    val isMistakesPractice: Boolean = false,
)

@HiltViewModel
class FraseLocaViewModel @Inject constructor(
    private val userProgressDao: UserProgressDao,
    private val achievementManager: AchievementManager,
    private val ratingUpdater: RatingUpdater,
    private val mistakesDao: com.spanishapp.data.db.dao.GameMistakesDao,
    private val soundPlayer: SoundPlayer,
    private val tts: SpanishTts,
    val levelManager: GameLevelManager,
) : ViewModel() {

    private val _state = MutableStateFlow(FraseLocaState())
    val state = _state.asStateFlow()

    val mistakesCount: kotlinx.coroutines.flow.StateFlow<Int> = mistakesDao
        .observeCount(GameId.FRASE)
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0)

    /** Фразы текущей попытки (окно уровня или батч практики ошибок). */
    private var phrases: List<FrasePhrase> = emptyList()

    /** Была ли ошибка в текущей фразе (для clean-статистики и mistakes). */
    private var phraseHadError = false

    /** item_id текущей фразы практики — для removeMistake при чистой сборке. */
    private var currentMistakeKey: String? = null

    private var advanceJob: kotlinx.coroutines.Job? = null

    fun startLevel(level: Int) {
        advanceJob?.cancel()
        val theme = FraseLocaEngine.themeForLevel(level)
        phrases = FraseLocaEngine.phrasesForLevel(level)
        _state.value = FraseLocaState(
            level = level,
            themeTitle = theme.title,
            cefr = theme.cefr,
            totalRounds = phrases.size,
            showLevelMap = false,
        )
        nextPhrase()
    }

    /** «Работа над ошибками»: 5 фраз из пула, без ловушек и без звёзд. */
    fun startMistakesPractice() {
        advanceJob?.cancel()
        viewModelScope.launch {
            val batch = mistakesDao.getNextBatch(GameId.FRASE, 5)
            if (batch.isEmpty()) return@launch
            phrases = batch.map { m ->
                FrasePhrase(
                    ru = m.displayHint.ifBlank { m.displayMain },
                    tokens = m.itemId.split(" ").filter { it.isNotBlank() },
                )
            }.filter { it.tokens.size >= 2 }
            if (phrases.isEmpty()) return@launch
            _state.value = FraseLocaState(
                level = 1,
                themeTitle = "",
                totalRounds = phrases.size,
                showLevelMap = false,
                isMistakesPractice = true,
            )
            nextPhrase()
        }
    }

    fun openLevelMap() {
        advanceJob?.cancel()
        tts.stop()
        _state.value = _state.value.copy(showLevelMap = true, isGameOver = false)
    }

    /** Озвучить собранную часть фразы (или всю — после сборки). */
    fun speakPhrase() {
        val s = _state.value
        val text = if (s.phraseSolved) s.tokens.joinToString(" ")
                   else s.placed.joinToString(" ")
        if (text.isNotBlank()) tts.speak(text)
    }

    override fun onCleared() {
        advanceJob?.cancel()
        tts.stop()
        super.onCleared()
    }

    private fun nextPhrase() {
        val s = _state.value
        if (s.currentRound >= phrases.size || s.lives <= 0) {
            finishGame()
            return
        }
        val phrase = phrases[s.currentRound]
        phraseHadError = false
        currentMistakeKey = if (s.isMistakesPractice) phrase.sentence else null
        val traps = if (s.isMistakesPractice) emptyList()
                    else FraseLocaEngine.activeTraps(phrase, s.level)
        val tileWords =
            if (s.isMistakesPractice) phrase.tokens.shuffled(kotlin.random.Random(phrase.sentence.hashCode().toLong()))
            else FraseLocaEngine.tilesFor(phrase, s.level, s.currentRound)
        _state.value = s.copy(
            promptRu = phrase.ru,
            tokens = phrase.tokens,
            tiles = tileWords.mapIndexed { i, w -> FraseTile(id = i, word = w) },
            placed = emptyList(),
            traps = traps.associate { it.word to it.explanation },
            trapMessage = null,
            currentRound = s.currentRound + 1,
            phraseSolved = false,
        )
    }

    fun tapTile(tileId: Int) {
        val s = _state.value
        if (s.phraseSolved || s.isGameOver || s.showLevelMap || s.lives <= 0) return
        val tile = s.tiles.getOrNull(tileId) ?: return
        if (tile.used) return

        val expected = s.tokens.getOrNull(s.placed.size) ?: return
        when {
            tile.word == expected -> onCorrectTap(s, tile)
            s.traps.containsKey(tile.word) -> onTrapTap(s, tile)
            else -> onWrongOrderTap(s)
        }
    }

    private fun onCorrectTap(s: FraseLocaState, tile: FraseTile) {
        val newPlaced = s.placed + tile.word
        val solved = newPlaced.size == s.tokens.size
        val clean = solved && !phraseHadError
        _state.value = s.copy(
            tiles = s.tiles.map { if (it.id == tile.id) it.copy(used = true) else it },
            placed = newPlaced,
            trapMessage = null,
            phraseSolved = solved,
            cleanCount = if (clean) s.cleanCount + 1 else s.cleanCount,
            streak = if (solved) (if (clean) s.streak + 1 else 0) else s.streak,
            score = if (solved) s.score + (if (clean) 20 + s.streak * 5 else 10) else s.score,
            answerHistory = if (solved) s.answerHistory + clean else s.answerHistory,
        )
        if (solved) {
            soundPlayer.playCorrect()
            tts.speak(s.tokens.joinToString(" "))
            viewModelScope.launch {
                ratingUpdater.applyGameAnswer(clean)
                if (_state.value.isMistakesPractice && clean) {
                    currentMistakeKey?.let {
                        runCatching { mistakesDao.removeMistake(GameId.FRASE, it) }
                    }
                }
            }
            advanceJob?.cancel()
            advanceJob = viewModelScope.launch {
                delay(1600)
                nextPhrase()
            }
        }
    }

    private fun onTrapTap(s: FraseLocaState, tile: FraseTile) {
        phraseHadError = true
        soundPlayer.playWrong()
        val newLives = (s.lives - 1).coerceAtLeast(0)
        _state.value = s.copy(
            lives = newLives,
            trapMessage = s.traps[tile.word],
            wrongTapCount = s.wrongTapCount + 1,
            streak = 0,
        )
        recordPhraseMistake(s)
        if (newLives <= 0) {
            // Жизни кончились — уровень завершается с текущим результатом.
            advanceJob?.cancel()
            advanceJob = viewModelScope.launch {
                delay(1600)
                finishGame()
            }
        }
    }

    private fun onWrongOrderTap(s: FraseLocaState) {
        phraseHadError = true
        soundPlayer.playWrong()
        _state.value = s.copy(
            wrongTapCount = s.wrongTapCount + 1,
            trapMessage = null,
            streak = 0,
        )
        recordPhraseMistake(s)
    }

    /** Первая ошибка во фразе → фраза уходит в «Работу над ошибками». */
    private fun recordPhraseMistake(s: FraseLocaState) {
        if (s.isMistakesPractice) return
        val sentence = s.tokens.joinToString(" ")
        if (sentence.isBlank()) return
        viewModelScope.launch {
            mistakesDao.recordMistake(
                gameId = GameId.FRASE,
                itemId = sentence,
                hint = s.promptRu,
                main = sentence,
            )
        }
    }

    private fun finishGame() {
        advanceJob?.cancel()
        val s = _state.value
        val percent = if (s.totalRounds > 0) (s.cleanCount * 100) / s.totalRounds else 0

        viewModelScope.launch {
            val stars = if (s.isMistakesPractice) 0
                        else levelManager.completeLevel(GameId.FRASE, s.level, percent)

            val p = userProgressDao.getProgressOnce()
            if (p != null) {
                // XP ровно как показывает LevelCompleteSheet: (percent/10)*5,
                // в практике — 3 XP за чисто собранную фразу.
                val xpGain = if (s.isMistakesPractice) s.cleanCount * 3
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
