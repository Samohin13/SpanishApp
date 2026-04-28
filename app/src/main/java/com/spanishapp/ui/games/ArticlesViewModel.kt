package com.spanishapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.ArticleGameDao
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.entity.ArticleLevelProgressEntity
import com.spanishapp.data.db.entity.ArticleWordEntity
import com.spanishapp.service.AchievementManager
import com.spanishapp.service.SpanishTts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArticlesPremiumState(
    val currentWord: ArticleWordEntity? = null,
    val level: String = "A1",
    val score: Int = 0,
    val streak: Int = 0,
    val multiplier: Float = 1.0f,
    val currentRound: Int = 0,
    val totalRounds: Int = 10,
    val isGameOver: Boolean = false,
    val lastCorrect: Boolean? = null,
    val academicHint: String? = null,
    val lastResponseTime: Long = 0L
)

@HiltViewModel
class ArticlesViewModel @Inject constructor(
    private val dao: ArticleGameDao,
    private val userProgressDao: UserProgressDao,
    private val achievementManager: AchievementManager,
    private val tts: SpanishTts
) : ViewModel() {

    private val _state = MutableStateFlow(ArticlesPremiumState())
    val state = _state.asStateFlow()

    private var questionStartTime = 0L

    init {
        viewModelScope.launch {
            if (dao.getWordCount() == 0) {
                seedWords()
            }
        }
    }

    fun startGame(level: String) {
        _state.value = ArticlesPremiumState(level = level)
        nextRound()
    }

    private fun nextRound() {
        val s = _state.value
        if (s.currentRound >= s.totalRounds) {
            finishGame()
            return
        }

        viewModelScope.launch {
            val words = dao.getWordsForLevel(s.level, 10)
            if (words.isNotEmpty()) {
                val word = words.random()
                _state.value = s.copy(
                    currentWord = word,
                    currentRound = s.currentRound + 1,
                    lastCorrect = null,
                    academicHint = null
                )
                questionStartTime = System.currentTimeMillis()
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
        var newStreak = if (isCorrect) s.streak + 1 else 0
        var newMultiplier = 1.0f + (newStreak / 5) * 0.5f
        
        // Intuition bonus
        if (isCorrect && responseTime < 1200) {
            xp += 5
        }

        val totalXpGain = (xp * newMultiplier).toInt()

        viewModelScope.launch {
            if (isCorrect) {
                tts.speak("${word.article} ${word.word}")
                // Adaptive weight: decrease if correct
                word.errorWeight = (word.errorWeight - 1).coerceAtLeast(0)
            } else {
                // Adaptive weight: increase if wrong
                word.errorWeight += 2
            }
            dao.updateWord(word)

            _state.value = s.copy(
                lastCorrect = isCorrect,
                score = s.score + totalXpGain,
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
        _state.value = s.copy(isGameOver = true)
        viewModelScope.launch {
            val p = userProgressDao.getProgressOnce() ?: return@launch
            userProgressDao.update(p.copy(totalXp = p.totalXp + s.score))
            achievementManager.checkAndUnlock()
        }
    }

    private suspend fun seedWords() {
        val words = listOf(
            // A1 - Génesis
            ArticleWordEntity(word = "casa", article = "la", level = "A1", ruleHint = "Слова на -a обычно женского рода."),
            ArticleWordEntity(word = "perro", article = "el", level = "A1", ruleHint = "Слова на -o обычно мужского рода."),
            ArticleWordEntity(word = "gato", article = "el", level = "A1", ruleHint = "Слова на -o обычно мужского рода."),
            ArticleWordEntity(word = "mesa", article = "la", level = "A1", ruleHint = "Слова на -a обычно женского рода."),
            // A2 - Desafío
            ArticleWordEntity(word = "mapa", article = "el", level = "A2", ruleHint = "Исключение: el mapa."),
            ArticleWordEntity(word = "foto", article = "la", level = "A2", ruleHint = "Исключение: la foto (сокращение от la fotografía)."),
            ArticleWordEntity(word = "noche", article = "la", level = "A2", ruleHint = "Слова на -e часто женского рода, нужно запоминать."),
            ArticleWordEntity(word = "luz", article = "la", level = "A2", ruleHint = "Слова на -z часто женского рода."),
            // B1 - Estructura
            ArticleWordEntity(word = "problema", article = "el", level = "B1", ruleHint = "Слова греческого происхождения на -ma мужского рода."),
            ArticleWordEntity(word = "sistema", article = "el", level = "B1", ruleHint = "Слова греческого происхождения на -ma мужского рода."),
            ArticleWordEntity(word = "planeta", article = "el", level = "B1", ruleHint = "Слова греческого происхождения на -ta мужского рода (el planeta)."),
            ArticleWordEntity(word = "libertad", article = "la", level = "B1", ruleHint = "Суффикс -dad/-tad всегда женского рода."),
            ArticleWordEntity(word = "nación", article = "la", level = "B1", ruleHint = "Суффикс -ción всегда женского рода."),
            ArticleWordEntity(word = "costumbre", article = "la", level = "B1", ruleHint = "Суффикс -umbre всегда женского рода."),
            ArticleWordEntity(word = "actitud", article = "la", level = "B1", ruleHint = "Суффикс -tud всегда женского рода."),
            // B2 - Dominio
            ArticleWordEntity(word = "agua", article = "el", level = "B2", ruleHint = "Перед ударной 'a' используется 'el' для красоты звучания, но слово остается женского рода."),
            ArticleWordEntity(word = "hacha", article = "el", level = "B2", ruleHint = "Перед ударной 'ha' используется 'el' для благозвучия."),
            ArticleWordEntity(word = "águila", article = "el", level = "B2", ruleHint = "Перед ударной 'á' используем 'el'."),
            // C1 - Maestría
            ArticleWordEntity(word = "capital (деньги)", article = "el", level = "C1", ruleHint = "El capital означает финансовый капитал."),
            ArticleWordEntity(word = "capital (город)", article = "la", level = "C1", ruleHint = "La capital означает главный город страны."),
            ArticleWordEntity(word = "orden (порядок)", article = "el", level = "C1", ruleHint = "El orden — порядок/организация."),
            ArticleWordEntity(word = "orden (приказ)", article = "la", level = "C1", ruleHint = "La orden — приказ или религиозный орден.")
        )
        dao.insertWords(words)
        
        // Unlock A1 progress
        dao.upsertProgress(ArticleLevelProgressEntity("A1", isUnlocked = true))
        dao.upsertProgress(ArticleLevelProgressEntity("A2", isUnlocked = true))
        dao.upsertProgress(ArticleLevelProgressEntity("B1", isUnlocked = true))
        dao.upsertProgress(ArticleLevelProgressEntity("B2", isUnlocked = true))
        dao.upsertProgress(ArticleLevelProgressEntity("C1", isUnlocked = true))
    }
}
