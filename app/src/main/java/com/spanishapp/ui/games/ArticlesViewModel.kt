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
import com.spanishapp.domain.algorithm.RatingUpdater
import com.spanishapp.service.AchievementManager
import com.spanishapp.service.SoundPlayer
import com.spanishapp.service.SpanishTts
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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
    val showLevelMap: Boolean = true,
    /** v1.22.0: режим «Работа над ошибками». В этом режиме верный ответ удаляет
     *  слово из mistakes, не начисляет звёзды за уровень, и не учитывается в
     *  GameLevelManager. */
    val isMistakesPractice: Boolean = false,
    val mistakesCount: Int = 0
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
    private val soundPlayer: SoundPlayer,
    private val ratingUpdater: RatingUpdater,
    private val mistakesDao: com.spanishapp.data.db.dao.GameMistakesDao,
    val levelManager: GameLevelManager
) : ViewModel() {

    private val _state = MutableStateFlow(ArticlesPremiumState())
    val state = _state.asStateFlow()

    /** Live-счётчик ошибок текущей игры — для бейджа на карте уровней. */
    val mistakesCount: kotlinx.coroutines.flow.StateFlow<Int> = mistakesDao
        .observeCount(GameId.ARTICLES)
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0)

    private var questionStartTime = 0L
    /** Буфер слов для режима «Работа над ошибками» — 5 за раз. */
    private var mistakesBatch: List<com.spanishapp.data.db.entity.GameMistakeEntity> = emptyList()

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

    /**
     * Запустить режим «Работа над ошибками» — берём первые 5 mistakes из БД,
     * прогоняем через тот же UI игры. Верный ответ удаляет из mistakes;
     * неверный — оставляет (attempts++).
     */
    fun startMistakesPractice() {
        viewModelScope.launch {
            val batch = mistakesDao.getNextBatch(GameId.ARTICLES, limit = 5)
            if (batch.isEmpty()) return@launch
            mistakesBatch = batch
            // Используем псевдо-уровень 0 (отключает level-progression логику)
            // params используем как у первого уровня для UI, но не сохраняем результат
            val params = LevelDifficulty.forLevel(1)
            _state.value = ArticlesPremiumState(
                params = params,
                showLevelMap = false,
                isMistakesPractice = true,
                mistakesCount = batch.size,
                answerHistory = emptyList()
            )
            nextMistakeRound()
        }
    }

    private fun nextMistakeRound() {
        val s = _state.value
        if (s.currentRound >= mistakesBatch.size) {
            // Закончили пятёрку — закрыть, вернуться на levelMap
            _state.value = s.copy(
                isGameOver = true,
                finalPercent = if (s.totalRounds == 0) 0
                               else (s.correctCount * 100 / mistakesBatch.size),
                finalStars = 0    // в режиме практики звёзды не даются
            )
            return
        }
        viewModelScope.launch {
            val mistake = mistakesBatch[s.currentRound]
            // Восстановим слово из ArticleWordEntity (если оно есть в БД)
            val word = dao.findByWord(mistake.itemId) ?: return@launch
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

    /** Запустить уровень (1..100). isTransition=true — переход с предыдущего уровня (играет джингл). */
    fun startLevel(level: Int, isTransition: Boolean = false) {
        if (isTransition) soundPlayer.playLevelUp()
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
            // позиция в раунде = currentRound. Пересеиваем ТОЛЬКО если слов
            // нет вообще (первый запуск до сида).
            var words = dao.getWordsForGameLevel(s.params.level)
            if (words.isEmpty()) {
                seedFromJson()
                words = dao.getWordsForGameLevel(s.params.level)
            }
            if (words.isEmpty()) {
                // No words at all — finish gracefully instead of hanging
                // mid-round with a blank screen.
                finishGame()
                return@launch
            }
            // v1.25.97 FIX (audit H3): asset содержит ровно 10 слов на уровень,
            // а LevelDifficulty для 41+ задаёт 12-15 раундов. Раньше нехватка
            // триггерила ПОВТОРНЫЙ seedFromJson() → INSERT IGNORE с autoincrement
            // PK дублировал все 1000 строк (таблица росла при каждом уровне 41+),
            // и уровень спрашивал одни слова по 2 раза. Кэпим раунды словами.
            val base = if (words.size < s.totalRounds)
                s.copy(params = s.params.copy(rounds = words.size))
            else s
            if (base.currentRound >= base.totalRounds) {
                _state.value = base
                finishGame()
                return@launch
            }
            // Детерминированный шафл: один и тот же уровень → всегда один и тот же порядок слов
            val shuffled = words.shuffled(java.util.Random(base.params.level.toLong() * 31337L))
            val idx  = base.currentRound.coerceIn(0, shuffled.size - 1)
            val word = shuffled[idx]

            _state.value = base.copy(
                currentWord = word,
                currentRound = base.currentRound + 1,
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
                soundPlayer.playCorrect()
                tts.speak("${word.article} ${word.word}")
                word.errorWeight = (word.errorWeight - 1).coerceAtLeast(0)
                // Если играем в режиме «Работа над ошибками» — удаляем из mistakes
                if (s.isMistakesPractice) {
                    mistakesDao.removeMistake(GameId.ARTICLES, word.word)
                }
            } else {
                soundPlayer.playWrong()
                word.errorWeight += 2
                // Регистрируем ошибку: всегда (даже в обычной игре)
                mistakesDao.recordMistake(
                    gameId = GameId.ARTICLES,
                    itemId = word.word,                // ключ без артикля
                    hint = word.russian,                // перевод для отображения
                    main = "${word.article} ${word.word}",
                )
            }
            dao.updateWord(word)
            ratingUpdater.applyGameAnswer(isCorrect)

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
        if (_state.value.isMistakesPractice) nextMistakeRound()
        else nextRound()
    }

    private fun finishGame() {
        val s = _state.value
        val percent = if (s.totalRounds > 0) (s.correctCount * 100) / s.totalRounds else 0

        viewModelScope.launch {
            // v1.22.4: режим «работа над ошибками» — НЕ помечаем CEFR-уровень
            // и не даём звёзды. XP начисляем символически (+3 за разобранное).
            val stars = if (s.isMistakesPractice) 0
                        else levelManager.completeLevel(GameId.ARTICLES, s.level, percent)

            if (!s.isMistakesPractice) {
                // Совместимость со старым `article_level_progress` (главный экран)
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
            }

            val p = userProgressDao.getProgressOnce()
            if (p != null) {
                val xpDelta = if (s.isMistakesPractice) s.correctCount * 3 else s.score
                userProgressDao.update(p.copy(totalXp = p.totalXp + xpDelta))
                achievementManager.checkAndUnlock()
            }

            // Звук финала только для обычных уровней
            if (!s.isMistakesPractice) {
                if (stars == 3) soundPlayer.playPerfect()
                else if (stars > 0) soundPlayer.playLevelDone()
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
