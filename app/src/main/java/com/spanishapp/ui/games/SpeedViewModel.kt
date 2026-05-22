package com.spanishapp.ui.games

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.entity.WordEntity
import com.spanishapp.domain.games.GameId
import com.spanishapp.domain.games.GameLevelManager
import com.spanishapp.domain.games.LevelDifficulty
import com.spanishapp.domain.games.LevelParams
import com.spanishapp.domain.algorithm.RatingUpdater
import com.spanishapp.service.AchievementManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import javax.inject.Inject

data class SpeedPremiumState(
    val params: LevelParams = LevelDifficulty.forLevel(1),
    val currentWord: WordEntity? = null,
    val options: List<String> = emptyList(),
    val timeLeft: Float = 1f,
    val score: Int = 0,
    val correctCount: Int = 0,
    val streak: Int = 0,
    val multiplier: Float = 1.0f,
    val currentRound: Int = 0,
    val isGameOver: Boolean = false,
    val lastCorrect: Boolean? = null,
    /**
     * v1.22.5: что юзер выбрал в этом раунде (для красной подсветки
     * именно его выбора, а не только зелёной на правильном).
     */
    val lastAnswer: String? = null,
    val reactionTimes: MutableList<Long> = mutableListOf(),
    val weakWords: MutableList<WordEntity> = mutableListOf(),
    val finalStars: Int = 0,
    val finalPercent: Int = 0,
    val showLevelMap: Boolean = true,
    val isMistakesPractice: Boolean = false,
) {
    val totalRounds: Int get() = params.rounds
    val level: Int get() = params.level
}

/** Один раунд игры Rapido: правильный перевод + 3 отвлечения (детерминированно). */
internal data class SpeedRound(
    val word: String,
    val russian: String,
    val distractors: List<String>,
)

@HiltViewModel
class SpeedViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val wordDao: WordDao,
    private val userProgressDao: UserProgressDao,
    private val achievementManager: AchievementManager,
    private val ratingUpdater: RatingUpdater,
    private val tts: com.spanishapp.service.SpanishTts,
    private val mistakesDao: com.spanishapp.data.db.dao.GameMistakesDao,
    val levelManager: GameLevelManager
) : ViewModel() {

    private val _state = MutableStateFlow(SpeedPremiumState())
    val state = _state.asStateFlow()

    val mistakesCount: kotlinx.coroutines.flow.StateFlow<Int> = mistakesDao
        .observeCount(GameId.SPEED)
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0)

    private var timerJob: Job? = null
    private var roundStartTime = 0L
    @Volatile private var roundResolved = false
    private var mistakesBatch: List<com.spanishapp.data.db.entity.GameMistakeEntity> = emptyList()

    /** Кэш детерминированных уровней. См. scripts/build_speed_levels.py. */
    @Volatile private var levelsCache: List<List<SpeedRound>>? = null
    /** Заранее сгенерированные раунды текущего уровня. */
    private var levelRounds: List<SpeedRound> = emptyList()

    private suspend fun loadLevels(): List<List<SpeedRound>> {
        levelsCache?.let { return it }
        return withContext(Dispatchers.IO) {
            val raw = try {
                appContext.assets.open("speed_levels.json")
                    .bufferedReader(Charsets.UTF_8)
                    .use { it.readText() }
            } catch (_: Exception) {
                ""
            }
            if (raw.isBlank()) return@withContext emptyList<List<SpeedRound>>()
            val arr = JSONArray(raw)
            val result = ArrayList<List<SpeedRound>>(arr.length())
            for (i in 0 until arr.length()) {
                val lvlObj = arr.getJSONObject(i)
                val wordsArr = lvlObj.getJSONArray("words")
                val list = ArrayList<SpeedRound>(wordsArr.length())
                for (j in 0 until wordsArr.length()) {
                    val w = wordsArr.getJSONObject(j)
                    val distArr = w.getJSONArray("distractors")
                    val distractors = ArrayList<String>(distArr.length())
                    for (k in 0 until distArr.length()) distractors += distArr.getString(k)
                    list += SpeedRound(
                        word = w.getString("word").lowercase(),
                        russian = w.getString("russian"),
                        distractors = distractors,
                    )
                }
                result += list
            }
            levelsCache = result
            result
        }
    }

    fun startLevel(level: Int) {
        timerJob?.cancel()
        roundResolved = false
        viewModelScope.launch {
            val levels = loadLevels()
            val idx = (level - 1).coerceIn(0, (levels.size - 1).coerceAtLeast(0))
            levelRounds = levels.getOrNull(idx) ?: emptyList()
            val baseParams = LevelDifficulty.forLevel(level)
            val params = baseParams.copy(rounds = levelRounds.size.coerceAtLeast(1))
            _state.value = SpeedPremiumState(params = params, showLevelMap = false)
            nextRound()
        }
    }

    fun openLevelMap() {
        timerJob?.cancel()
        _state.value = _state.value.copy(showLevelMap = true, isGameOver = false)
    }

    /** v1.22.0: «Работа над ошибками» — 5 слов из mistakes по очереди. */
    fun startMistakesPractice() {
        timerJob?.cancel()
        roundResolved = false
        viewModelScope.launch {
            val batch = mistakesDao.getNextBatch(GameId.SPEED, 5)
            if (batch.isEmpty()) return@launch
            mistakesBatch = batch
            val params = LevelDifficulty.forLevel(1).copy(rounds = batch.size, timePerRoundSec = 0f)
            _state.value = SpeedPremiumState(
                params = params,
                showLevelMap = false,
                isMistakesPractice = true,
            )
            nextMistakeRound()
        }
    }

    private fun nextMistakeRound() {
        val s = _state.value
        if (s.currentRound >= mistakesBatch.size) {
            finishGame()
            return
        }
        roundResolved = false
        viewModelScope.launch {
            val mistake = mistakesBatch[s.currentRound]
            // v1.22.2: itemId = lowercase(spanish), как в PalabraMaestra
            val sp = mistake.itemId.lowercase()
            val word = wordDao.findBySpanish(sp) ?: WordEntity(
                spanish = sp,
                russian = mistake.displayHint,
                level   = "A1",
            )
            // 3 случайных дистрактора из пула (фильтруем мусор: цифры, пустоты)
            val distractors = wordDao.getRandomWords(20)
                .filter {
                    it.russian.isNotBlank()
                        && it.russian != word.russian
                        && it.spanish.none { c -> c.isDigit() }
                        && it.russian.none { c -> c.isDigit() }
                }
                .map { it.russian }
                .distinct()
                .take(3)
            val options = (distractors + word.russian).shuffled()
            _state.value = s.copy(
                currentWord = word,
                options = options,
                currentRound = s.currentRound + 1,
                lastCorrect = null,
                lastAnswer = null,
                timeLeft = 1f,
            )
            roundStartTime = System.currentTimeMillis()
        }
    }

    private fun nextRound() {
        val s = _state.value
        if (s.currentRound >= s.totalRounds) {
            finishGame()
            return
        }
        val round = levelRounds.getOrNull(s.currentRound) ?: run {
            finishGame()
            return
        }

        viewModelScope.launch {
            // v1.22.2: ДЕТЕРМИНИРОВАННАЯ выборка из speed_levels.json.
            // Раньше брали wordDao.getRandomWords(80) + случайные distractors
            // → один и тот же level каждый раз показывал РАЗНЫЕ слова и
            // РАЗНОЕ их количество. Теперь 100 уровней × 10 слов из ассета.
            //
            // Защита: если в БД от старых debug-сборок остался мусор
            // (WordEntity с цифрами вместо слова), синтетика из JSON
            // перебивает её — даже если findBySpanish что-то вернёт.
            val dbWord = wordDao.findBySpanish(round.word)
            val dbValid = dbWord != null
                && dbWord.spanish.isNotBlank()
                && dbWord.spanish.none { it.isDigit() }
                && dbWord.russian.isNotBlank()
            val word = if (dbValid) dbWord!! else WordEntity(
                spanish = round.word,
                russian = round.russian,
                level   = s.params.cefr.firstOrNull() ?: "A1",
            )
            // Перестановка кнопок зависит от round+level — стабильна между запусками.
            val seed = s.level * 10_000L + s.currentRound
            val options = (round.distractors + round.russian)
                .distinct()
                .shuffled(kotlin.random.Random(seed))

            roundResolved = false
            _state.value = s.copy(
                currentWord  = word,
                options      = options,
                currentRound = s.currentRound + 1,
                timeLeft     = 1f,
                lastCorrect  = null,
                lastAnswer   = null,
            )
            runCatching { tts.speak(word.spanish) }
            roundStartTime = System.currentTimeMillis()
            startTimer()
        }
    }


    private fun startTimer() {
        timerJob?.cancel()
        val baseSec = _state.value.params.timePerRoundSec
        if (baseSec <= 0f) return   // уровень без таймера (1-10)
        timerJob = viewModelScope.launch {
            val step = 0.05f
            while (_state.value.timeLeft > 0 && !roundResolved) {
                delay(50)
                val newTime = (_state.value.timeLeft - (step / baseSec)).coerceAtLeast(0f)
                _state.value = _state.value.copy(timeLeft = newTime)
            }
            if (!roundResolved) submitAnswer("")   // тайм-аут
        }
    }

    fun submitAnswer(answer: String) {
        if (roundResolved) return
        roundResolved = true
        timerJob?.cancel()
        val s = _state.value
        val correctTranslation = s.currentWord?.russian ?: ""
        val isCorrect = answer == correctTranslation

        val reactionTime = System.currentTimeMillis() - roundStartTime
        if (isCorrect) s.reactionTimes.add(reactionTime)
        else s.currentWord?.let { s.weakWords.add(it) }

        // v1.22.0: «Работа над ошибками»
        // v1.22.2: itemId = lowercase(spanish) — синтетические WordEntity из
        // JSON могут иметь id=0, что приводило бы к коллизиям.
        viewModelScope.launch {
            val w = s.currentWord
            if (w != null) {
                val itemId = w.spanish.lowercase()
                if (isCorrect && s.isMistakesPractice) {
                    mistakesDao.removeMistake(GameId.SPEED, itemId)
                } else if (!isCorrect) {
                    mistakesDao.recordMistake(
                        gameId = GameId.SPEED,
                        itemId = itemId,
                        hint = w.russian,
                        main = w.spanish,
                    )
                }
            }
        }

        val newStreak = if (isCorrect) s.streak + 1 else 0
        val newMultiplier = 1.0f + (newStreak / 5) * 0.2f
        val points = if (isCorrect) (10 * newMultiplier).toInt() else 0

        _state.value = s.copy(
            score        = s.score + points,
            correctCount = if (isCorrect) s.correctCount + 1 else s.correctCount,
            streak       = newStreak,
            multiplier   = newMultiplier,
            lastCorrect  = isCorrect,
            lastAnswer   = answer,
        )

        // Feed the rating system — Speed game was the only mini-game NOT
        // calling RatingUpdater, so it granted 0 skill rating per answer.
        viewModelScope.launch {
            runCatching { ratingUpdater.applyGameAnswer(isCorrect) }
        }

        viewModelScope.launch {
            delay(if (isCorrect) 600 else 1200)
            if (_state.value.isMistakesPractice) nextMistakeRound()
            else nextRound()
        }
    }

    private fun finishGame() {
        val s = _state.value
        val percent = if (s.totalRounds > 0) (s.correctCount * 100) / s.totalRounds else 0

        viewModelScope.launch {
            // v1.22.4: режим «работа над ошибками» — не помечаем уровень,
            // +3 XP за каждое разобранное слово.
            val stars = if (s.isMistakesPractice) 0
                        else levelManager.completeLevel(GameId.SPEED, s.level, percent)

            val p = userProgressDao.getProgressOnce()
            if (p != null) {
                val xpDelta = if (s.isMistakesPractice) {
                    s.correctCount * 3
                } else {
                    // Floor XP at 5 so completion always feels rewarding
                    // even after a poor run.
                    (s.score / 2).coerceAtLeast(5)
                }
                userProgressDao.update(p.copy(totalXp = p.totalXp + xpDelta))
                achievementManager.checkAndUnlock()
            }

            _state.value = s.copy(
                isGameOver   = true,
                finalStars   = stars,
                finalPercent = percent
            )
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
