package com.spanishapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.entity.WordEntity
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
import java.util.UUID
import javax.inject.Inject

data class LetterItem(
    val id: String = UUID.randomUUID().toString(),
    val char: String,
    val isUsed: Boolean = false
)

data class PalabraQuestion(
    val word: WordEntity,
    val targetWord: String,
    val shuffledLetters: List<LetterItem>,
    val assembledLetters: List<LetterItem?> = emptyList(),
    val isChecked: Boolean = false,
    val isCorrect: Boolean? = null,
    val mistakesCount: Int = 0,
    val startTime: Long = 0,
    val endTime: Long = 0
)

data class PalabraState(
    val params: LevelParams = LevelDifficulty.forLevel(1),
    val questions: List<PalabraQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val score: Int = 0,
    val correctCount: Int = 0,
    val isGameOver: Boolean = false,
    val precisionCount: Int = 0,        // слов без единой ошибки
    val translationHintVisible: Boolean = false,
    val ruleHint: String? = null,
    val showLevelMap: Boolean = true,
    val finalStars: Int = 0,
    val finalPercent: Int = 0
) {
    val level: Int get() = params.level
    val totalRounds: Int get() = questions.size.coerceAtLeast(1)
    val isAutoValidate: Boolean get() = params.level <= 40   // первые 40 уровней — поддавки
}

@HiltViewModel
class PalabraMaestraViewModel @Inject constructor(
    private val wordDao: WordDao,
    private val userProgressDao: UserProgressDao,
    private val achievementManager: AchievementManager,
    private val tts: SpanishTts,
    val levelManager: GameLevelManager
) : ViewModel() {

    private val _state = MutableStateFlow(PalabraState())
    val state = _state.asStateFlow()

    fun startLevel(level: Int) {
        viewModelScope.launch {
            val params = LevelDifficulty.forLevel(level)
            val (minLen, maxLen) = lengthRange(level)
            val needAccent = level in 56..70
            val needDouble = level in 71..85

            // Большой пул из нужного CEFR-слоя
            val pool = wordDao.getRandomWords(400).filter {
                it.level in params.cefr && it.spanish.isNotBlank() && it.russian.isNotBlank()
            }
            val candidates = pool.filter { w ->
                val clean = stripArticle(w.spanish)
                if (clean.contains(' ') || clean.contains('-')) return@filter false
                if (clean.length !in minLen..maxLen) return@filter false
                if (needAccent && !hasAccent(clean)) return@filter false
                if (needDouble && !hasDoubleLetter(clean)) return@filter false
                true
            }.shuffled().distinctBy { stripArticle(it.spanish).lowercase() }

            // Сколько раундов — из общей шкалы. Если кандидатов меньше — берём сколько есть.
            val target = params.rounds.coerceAtMost(candidates.size).coerceAtLeast(1)
            val picked = candidates.take(target)
            if (picked.isEmpty()) return@launch

            val questions = picked.map { word ->
                val clean = stripArticle(word.spanish).lowercase()
                val chars = clean.map { it.toString() }
                PalabraQuestion(
                    word            = word,
                    targetWord      = clean,
                    shuffledLetters = shuffleLetters(chars),
                    assembledLetters = List(chars.size) { null },
                    startTime       = System.currentTimeMillis()
                )
            }

            _state.value = PalabraState(
                params       = params,
                questions    = questions,
                showLevelMap = false
            )
        }
    }

    fun openLevelMap() {
        _state.value = _state.value.copy(showLevelMap = true, isGameOver = false)
    }

    /**
     * Длина целевого слова для уровня. Максимум 11 — длиннее на ассемблере
     * становится больше пасьянсом, чем тренировкой.
     */
    private fun lengthRange(level: Int): Pair<Int, Int> = when {
        level <= 10  -> 3 to 4
        level <= 25  -> 4 to 5
        level <= 40  -> 5 to 6
        level <= 55  -> 6 to 7
        level <= 70  -> 7 to 8
        level <= 85  -> 8 to 9
        else         -> 9 to 11
    }

    private fun hasAccent(s: String): Boolean =
        s.lowercase().any { it in "áéíóúñü" }

    /** Двойные буквы (ll, rr, cc, nn) — отметка для уровней 71-85. */
    private fun hasDoubleLetter(s: String): Boolean {
        val l = s.lowercase()
        return l.contains("ll") || l.contains("rr") ||
               l.contains("cc") || l.contains("nn")
    }

    private fun shuffleLetters(chars: List<String>): List<LetterItem> {
        var shuffled = chars.shuffled()
        var attempts = 0
        while (attempts < 5 && shuffled.joinToString("") == chars.joinToString("") && chars.size > 1) {
            shuffled = chars.shuffled()
            attempts++
        }
        return shuffled.map { LetterItem(char = it) }
    }

    fun onLetterClick(letter: LetterItem) {
        val s = _state.value
        val q = s.questions.getOrNull(s.currentIndex) ?: return
        if (q.isChecked) return

        val emptyIndex = q.assembledLetters.indexOfFirst { it == null }
        if (emptyIndex == -1) return

        val newAssembled = q.assembledLetters.toMutableList()
        newAssembled[emptyIndex] = letter
        val newShuffled = q.shuffledLetters.map {
            if (it.id == letter.id) it.copy(isUsed = true) else it
        }
        var updated = q.copy(assembledLetters = newAssembled, shuffledLetters = newShuffled)

        if (s.isAutoValidate) {
            val expected = q.targetWord[emptyIndex].toString().lowercase()
            if (letter.char.lowercase() != expected) {
                updated = updated.copy(mistakesCount = updated.mistakesCount + 1)
            }
            // Авто-завершение, если слово целиком собрано верно
            if (newAssembled.all { it != null }) {
                val assembled = newAssembled.joinToString("") { it?.char ?: "" }.lowercase()
                if (assembled == q.targetWord.lowercase()) {
                    updated = updated.copy(
                        isChecked = true,
                        isCorrect = true,
                        endTime   = System.currentTimeMillis()
                    )
                    _state.value = s.copy(
                        score          = s.score + calculateScore(updated),
                        correctCount   = s.correctCount + 1,
                        precisionCount = s.precisionCount + (if (updated.mistakesCount == 0) 1 else 0)
                    )
                    tts.speak(q.word.spanish)
                }
            }
        }
        updateCurrentQuestion(updated)
    }

    fun removeLetter(index: Int) {
        val s = _state.value
        val q = s.questions.getOrNull(s.currentIndex) ?: return
        if (q.isChecked) return
        val letter = q.assembledLetters[index] ?: return

        val newAssembled = q.assembledLetters.toMutableList(); newAssembled[index] = null
        val newShuffled = q.shuffledLetters.map {
            if (it.id == letter.id) it.copy(isUsed = false) else it
        }
        updateCurrentQuestion(q.copy(assembledLetters = newAssembled, shuffledLetters = newShuffled))
    }

    fun checkWord() {
        val s = _state.value
        val q = s.questions.getOrNull(s.currentIndex) ?: return
        if (q.assembledLetters.any { it == null }) return

        val assembled = q.assembledLetters.joinToString("") { it?.char ?: "" }
        val isCorrect = assembled == q.targetWord
        val updated = q.copy(
            isChecked = true,
            isCorrect = isCorrect,
            endTime   = System.currentTimeMillis()
        )

        _state.value = s.copy(
            score          = s.score + (if (isCorrect) calculateScore(updated) else 0),
            correctCount   = s.correctCount + (if (isCorrect) 1 else 0),
            precisionCount = s.precisionCount + (if (isCorrect && updated.mistakesCount == 0) 1 else 0)
        )

        if (isCorrect) tts.speak(q.word.spanish)
        updateCurrentQuestion(updated)
    }

    private fun calculateScore(q: PalabraQuestion): Int {
        val sec = (q.endTime - q.startTime) / 1000
        val mult = when {
            sec < 5  -> 3.0
            sec < 15 -> 1.5
            else     -> 1.0
        }
        return (10 * mult).toInt()
    }

    private fun updateCurrentQuestion(q: PalabraQuestion) {
        val list = _state.value.questions.toMutableList()
        list[_state.value.currentIndex] = q
        _state.value = _state.value.copy(questions = list)
    }

    fun nextQuestion() {
        val s = _state.value
        if (s.currentIndex + 1 < s.questions.size) {
            _state.value = s.copy(
                currentIndex = s.currentIndex + 1,
                translationHintVisible = false,
                ruleHint = null
            )
        } else {
            finishGame()
        }
    }

    private fun finishGame() {
        val s = _state.value
        val percent = if (s.questions.isNotEmpty())
            (s.correctCount * 100) / s.questions.size else 0

        viewModelScope.launch {
            val stars = levelManager.completeLevel(GameId.PALABRA, s.level, percent)

            val p = userProgressDao.getProgressOnce()
            if (p != null) {
                userProgressDao.update(p.copy(totalXp = p.totalXp + s.score))
                achievementManager.checkAndUnlock()
            }

            _state.value = s.copy(
                isGameOver   = true,
                finalStars   = stars,
                finalPercent = percent
            )
        }
    }

    // ── Подсказки ──────────────────────────────────────────────
    fun showTranslation() {
        _state.value = _state.value.copy(translationHintVisible = true)
    }

    fun playAudio() {
        val q = _state.value.questions.getOrNull(_state.value.currentIndex) ?: return
        tts.speak(q.word.spanish)
    }

    fun useFirstLetterHint() {
        val s = _state.value
        val q = s.questions.getOrNull(s.currentIndex) ?: return
        if (q.isChecked) return
        val firstChar = q.targetWord[0].toString().lowercase()
        val letterItem = q.shuffledLetters.find { it.char == firstChar && !it.isUsed } ?: return
        if (q.assembledLetters[0] != null) removeLetter(0)
        val newAssembled = _state.value.questions[s.currentIndex].assembledLetters.toMutableList()
        newAssembled[0] = letterItem
        val newShuffled = _state.value.questions[s.currentIndex].shuffledLetters.map {
            if (it.id == letterItem.id) it.copy(isUsed = true) else it
        }
        updateCurrentQuestion(_state.value.questions[s.currentIndex].copy(
            assembledLetters = newAssembled,
            shuffledLetters  = newShuffled
        ))
    }

    fun showRuleHint() {
        val q = _state.value.questions.getOrNull(_state.value.currentIndex) ?: return
        val word = q.targetWord
        val rule = when {
            word.contains('h') -> "Буква 'H' в испанском всегда немая (не произносится)."
            word.contains('ñ') -> "Буква 'Ñ' читается как мягкое 'нь' (как в слове каньон)."
            "áéíóú".any { it in word } ->
                "Графическое ударение (tilde) выделяет ударный слог вопреки общим правилам."
            word.contains("ll") -> "Двойная 'LL' во многих диалектах читается как 'й'."
            word.contains("rr") -> "Двойная 'RR' произносится как раскатистое 'рр'."
            else -> "Внимательно следите за порядком букв!"
        }
        _state.value = _state.value.copy(ruleHint = rule)
    }

    fun reset() {
        _state.value = _state.value.copy(showLevelMap = true, isGameOver = false)
    }

    private fun stripArticle(s: String): String {
        val regex = Regex("^(el|la|los|las|un|una|unos|unas)\\s+", RegexOption.IGNORE_CASE)
        return s.trim().replace(regex, "").trim()
    }
}
