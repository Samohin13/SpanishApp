package com.spanishapp.ui.games

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    val academicHint: String? = null,
    // финальный экран
    val finalStars: Int = 0,
    val finalPercent: Int = 0,
    val showLevelMap: Boolean = true   // экран выбора уровня по умолчанию
) {
    val totalRounds: Int get() = params.rounds
    val level: Int get() = params.level
}

@HiltViewModel
class ArticlesViewModel @Inject constructor(
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
            if (dao.getWordCount() < 200) {
                seedFromDictionary()
            }
        }
    }

    /** Запустить уровень (1..100). */
    fun startLevel(level: Int) {
        val params = LevelDifficulty.forLevel(level)
        _state.value = ArticlesPremiumState(params = params, showLevelMap = false)
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
            val words = dao.getWordsForLevels(s.params.cefr, 30)
            if (words.isNotEmpty()) {
                val word = words.random()
                _state.value = s.copy(
                    currentWord = word,
                    currentRound = s.currentRound + 1,
                    lastCorrect = null,
                    academicHint = null
                )
                questionStartTime = System.currentTimeMillis()
            } else {
                // Запасной вариант — добиваем seed
                seedFromDictionary()
                nextRound()
            }
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
                score = s.score + totalXpGain,
                correctCount = if (isCorrect) s.correctCount + 1 else s.correctCount,
                streak = newStreak,
                multiplier = newMultiplier,
                academicHint = if (!isCorrect) word.ruleHint else null
            )

            kotlinx.coroutines.delay(if (isCorrect) 1000 else 2500)
            nextRound()
        }
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
     * Засев `article_words` из основного словаря — извлекаем все
     * существительные с артиклем `el / la` и сохраняем как кандидатов.
     * Запускается при первом старте если в БД пусто.
     */
    private suspend fun seedFromDictionary() {
        // ВАЖНО: getAllWordsOnce фильтрует по total_reviews > 0 — при первом
        // запуске вернёт пусто. Используем getRandomWords без фильтра.
        val all = wordDao.getRandomWords(12000)
        val articles = all.mapNotNull { w ->
            if (w.wordType != "noun") return@mapNotNull null
            val s = w.spanish.trim().lowercase()
            val (article, rest) = when {
                s.startsWith("el ")  -> "el"  to s.removePrefix("el ").trim()
                s.startsWith("la ")  -> "la"  to s.removePrefix("la ").trim()
                else -> return@mapNotNull null
            }
            // отбрасываем многословные и спец-конструкции
            if (rest.isBlank() || rest.contains(' ')) return@mapNotNull null
            ArticleWordEntity(
                word     = rest,
                article  = article,
                level    = w.level.ifBlank { "A1" },
                ruleHint = ""
            )
        }
        if (articles.isNotEmpty()) {
            dao.insertWords(articles)
        }
        // и сами уровни уровней (legacy совместимость)
        listOf("A1", "A2", "B1", "B2", "C1").forEach {
            dao.upsertProgress(ArticleLevelProgressEntity(it, isUnlocked = true))
        }
    }
}
