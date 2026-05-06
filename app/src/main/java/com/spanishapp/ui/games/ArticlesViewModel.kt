package com.spanishapp.ui.games

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.ArticleGameDao
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.entity.ArticleLevelProgressEntity
import com.spanishapp.data.db.entity.ArticleWordEntity
import com.spanishapp.domain.games.GameId
import com.spanishapp.domain.games.GameLevelManager
import com.spanishapp.domain.games.LevelDifficulty
import com.spanishapp.domain.games.LevelParams
import com.spanishapp.service.AchievementManager
import com.spanishapp.service.SpanishTts
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject

data class ArticlesPremiumState(
    val currentWord: ArticleWordEntity? = null,
    val params: LevelParams = LevelDifficulty.forLevel(1),
    val score: Int = 0,
    val correctCount: Int = 0,
    val streak: Int = 0,
    val multiplier: Float = 1.0f,
    val currentRound: Int = 0,
    val isGameOver: Boolean = false,
    val lastCorrect: Boolean? = null,
    val chosenArticle: String? = null,
    val lastXpGain: Int = 0,                    // XP за последний ответ (для анимации)
    val answerHistory: List<Boolean> = emptyList(), // история ответов текущего уровня
    val academicHint: String? = null,
    val finalStars: Int = 0,
    val finalPercent: Int = 0,
    val showLevelMap: Boolean = true
) {
    val totalRounds: Int get() = params.rounds
    val level: Int get() = params.level
}

@HiltViewModel
class ArticlesViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val dao: ArticleGameDao,
    private val wordDao: WordDao,
    private val userProgressDao: UserProgressDao,
    private val achievementManager: AchievementManager,
    private val tts: SpanishTts,
    val levelManager: GameLevelManager
) : ViewModel() {

    private val _state = MutableStateFlow(ArticlesPremiumState())
    val state = _state.asStateFlow()

    private var questionStartTime = 0L

    init {
        viewModelScope.launch {
            val total    = dao.getWordCount()
            val firstLvl = dao.getWordsForGameLevel(1)
            // Ресид если: БД пустая, уровень 1 пустой, ИЛИ уровень 1 содержит только "el" (старые данные)
            val needReseed = total < 100
                || firstLvl.isEmpty()
                || firstLvl.none { it.article == "la" || it.article == "las" }
            if (needReseed) {
                dao.deleteAllWords()
                seedFromJson()
            }
        }
    }

    /** Запустить уровень (1..100). */
    fun startLevel(level: Int) {
        val params = LevelDifficulty.forLevel(level)
        _state.value = ArticlesPremiumState(
            params = params,
            showLevelMap = false,
            answerHistory = emptyList()
        )
        nextRound()
    }

    /** Вернуться к карте уровней. */
    fun openLevelMap() {
        _state.value = _state.value.copy(showLevelMap = true, isGameOver = false)
    }

    private fun nextRound() {
        val s = _state.value
        if (s.currentRound >= s.totalRounds) {
            finishGame()
            return
        }

        viewModelScope.launch {
            // Детерминированный набор слов для уровня (1..100), без RANDOM:
            // позиция в раунде = currentRound (0..9). Если в БД < 10 слов на уровне —
            // пересеиваем и берём первое слово.
            var words = dao.getWordsForGameLevel(s.params.level)
            if (words.size < s.totalRounds) {
                seedFromJson()
                words = dao.getWordsForGameLevel(s.params.level)
            }
            if (words.isEmpty()) return@launch
            // Детерминированный шафл: один и тот же уровень → всегда один и тот же порядок слов
            val shuffled = words.shuffled(java.util.Random(s.params.level.toLong() * 31337L))
            val idx  = s.currentRound.coerceIn(0, shuffled.size - 1)
            val word = shuffled[idx]
            _state.value = s.copy(
                currentWord = word,
                currentRound = s.currentRound + 1,
                lastCorrect = null,
                chosenArticle = null,
                academicHint = null
            )
            questionStartTime = System.currentTimeMillis()
        }
    }

    fun submitAnswer(article: String) {
        val s = _state.value
        val word = s.currentWord ?: return
        if (s.lastCorrect != null) return

        val isCorrect = article == word.article
        val responseTime = System.currentTimeMillis() - questionStartTime

        var xp = if (isCorrect) 10 else 0
        val newStreak = if (isCorrect) s.streak + 1 else 0
        val newMultiplier = 1.0f + (newStreak / 5) * 0.5f

        // бонус за быстрый ответ (только если не штрафной режим)
        if (isCorrect && responseTime < 1200) xp += 5

        val totalXpGain = (xp * newMultiplier).toInt()

        viewModelScope.launch {
            if (isCorrect) {
                tts.speak("${word.article} ${word.word}")
                word.errorWeight = (word.errorWeight - 1).coerceAtLeast(0)
            } else {
                word.errorWeight += 2
            }
            dao.updateWord(word)

            _state.value = s.copy(
                lastCorrect = isCorrect,
                chosenArticle = article,
                lastXpGain = if (isCorrect) totalXpGain else 0,
                answerHistory = s.answerHistory + isCorrect,
                score = s.score + totalXpGain,
                correctCount = if (isCorrect) s.correctCount + 1 else s.correctCount,
                streak = newStreak,
                multiplier = newMultiplier,
                academicHint = if (!isCorrect) word.ruleHint else null
            )

            // Авто-переход убран — теперь пользователь нажимает CONTINUAR (как в Duolingo)
        }
    }

    /** Вызывается кнопкой CONTINUAR после ответа. */
    fun continueToNext() {
        nextRound()
    }

    private fun finishGame() {
        val s = _state.value
        val percent = if (s.totalRounds > 0) (s.correctCount * 100) / s.totalRounds else 0

        viewModelScope.launch {
            val stars = levelManager.completeLevel(GameId.ARTICLES, s.level, percent)

            // Совместимость со старым `article_level_progress` (используется на главном экране)
            val cefrTag = s.params.cefr.first()
            val existing = dao.getProgress(cefrTag)
            dao.upsertProgress(
                ArticleLevelProgressEntity(
                    levelId    = cefrTag,
                    stars      = maxOf(existing?.stars ?: 0, stars),
                    isUnlocked = true,
                    bestScore  = maxOf(existing?.bestScore ?: 0, percent)
                )
            )

            val p = userProgressDao.getProgressOnce()
            if (p != null) {
                userProgressDao.update(p.copy(totalXp = p.totalXp + s.score))
                achievementManager.checkAndUnlock()
            }

            _state.value = s.copy(
                isGameOver  = true,
                finalStars  = stars,
                finalPercent = percent
            )
        }
    }

    /**
     * Засев `article_words` из ассета `articles_levels.json` — детерминированно
     * по 100 уровням × 10 раундов. Источник правды: docs/articles_levels.json.
     * Запускается один раз: при первом старте или после миграции 10→11.
     */
    private suspend fun seedFromJson() {
        val raw = try {
            appContext.assets.open("articles_levels.json")
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
        } catch (_: Exception) {
            return  // ассет не найден — оставляем БД пустой, UI покажет сообщение
        }
        val arr = JSONArray(raw)
        val rows = mutableListOf<ArticleWordEntity>()
        for (i in 0 until arr.length()) {
            val lvlObj = arr.getJSONObject(i)
            val levelNum = lvlObj.getInt("level")
            val block = lvlObj.optString("block", "")
            val ruleHint = lvlObj.optString("rule_hint", "")
            val cefr = when {
                levelNum <= 30 -> "A1"
                levelNum <= 50 -> "A2"
                levelNum <= 80 -> "B1"
                else            -> "B2"
            }
            val words = lvlObj.getJSONArray("words")
            for (pos in 0 until words.length()) {
                val w = words.getJSONObject(pos)
                rows += ArticleWordEntity(
                    word      = w.getString("word"),
                    article   = w.getString("article"),
                    level     = cefr,
                    ruleHint  = ruleHint,
                    levelNum  = levelNum,
                    position  = pos,
                    isPlural  = w.optBoolean("is_plural", false),
                    russian   = w.optString("russian", ""),
                    block     = block
                )
            }
        }
        if (rows.isNotEmpty()) {
            dao.insertWords(rows)
        }
        // legacy совместимость — все CEFR-уровни в article_level_progress открыты
        listOf("A1", "A2", "B1", "B2", "C1").forEach {
            dao.upsertProgress(ArticleLevelProgressEntity(it, isUnlocked = true))
        }
    }
}
