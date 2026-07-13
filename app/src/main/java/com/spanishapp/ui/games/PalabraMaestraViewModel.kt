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
import com.spanishapp.service.SpanishTts
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
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
    val finalPercent: Int = 0,
    val isMistakesPractice: Boolean = false,
) {
    val level: Int get() = params.level
    val totalRounds: Int get() = questions.size.coerceAtLeast(1)
    val isAutoValidate: Boolean get() = params.level <= 40   // первые 40 уровней — поддавки
}

@HiltViewModel
class PalabraMaestraViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val wordDao: WordDao,
    private val userProgressDao: UserProgressDao,
    private val achievementManager: AchievementManager,
    private val tts: SpanishTts,
    private val ratingUpdater: RatingUpdater,
    private val mistakesDao: com.spanishapp.data.db.dao.GameMistakesDao,
    val levelManager: GameLevelManager
) : ViewModel() {

    private val _state = MutableStateFlow(PalabraState())
    val state = _state.asStateFlow()

    /**
     * Кэш детерминированных уровней из assets/palabra_levels.json.
     * 100 уровней × 10 слов = 1000 уникальных. Грузим один раз.
     * Источник правды: scripts/build_palabra_levels.py.
     */
    @Volatile private var levelsCache: List<List<Pair<String, String>>>? = null

    private suspend fun loadLevels(): List<List<Pair<String, String>>> {
        levelsCache?.let { return it }
        return withContext(Dispatchers.IO) {
            val raw = try {
                appContext.assets.open("palabra_levels.json")
                    .bufferedReader(Charsets.UTF_8)
                    .use { it.readText() }
            } catch (_: Exception) {
                ""
            }
            if (raw.isBlank()) return@withContext emptyList<List<Pair<String, String>>>()
            val arr = JSONArray(raw)
            val result = ArrayList<List<Pair<String, String>>>(arr.length())
            for (i in 0 until arr.length()) {
                val lvlObj = arr.getJSONObject(i)
                val wordsArr = lvlObj.getJSONArray("words")
                val list = ArrayList<Pair<String, String>>(wordsArr.length())
                for (j in 0 until wordsArr.length()) {
                    val w = wordsArr.getJSONObject(j)
                    list += w.getString("word").lowercase() to w.getString("russian")
                }
                result += list
            }
            levelsCache = result
            result
        }
    }

    val mistakesCount: kotlinx.coroutines.flow.StateFlow<Int> = mistakesDao
        .observeCount(GameId.PALABRA)
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0)

    /** v1.22.0: «Работа над ошибками» — 5 слов из mistakes по очереди. */
    fun startMistakesPractice() {
        viewModelScope.launch {
            val batch = mistakesDao.getNextBatch(GameId.PALABRA, 5)
            if (batch.isEmpty()) return@launch
            // Восстановим WordEntity по испанскому слову из main.
            // v1.26.1 FIX (audit M6): нерезолвнутые слова (ушли из БД после
            // реcида) раньше молча выпадали из практики, но ОСТАВАЛИСЬ в пуле —
            // счётчик ошибок залипал и его нельзя было обнулить. Теперь чистим.
            val words = mutableListOf<com.spanishapp.data.db.entity.WordEntity>()
            for (m in batch) {
                val clean = stripArticle(m.itemId).lowercase()
                val w = wordDao.findBySpanish(m.displayMain) ?: wordDao.findBySpanish(clean)
                if (w != null) words.add(w)
                else runCatching { mistakesDao.removeMistake(GameId.PALABRA, m.itemId) }
            }
            if (words.isEmpty()) return@launch
            val questions = words.map { word ->
                val clean = stripArticle(word.spanish).lowercase()
                val chars = clean.map { it.toString() }
                PalabraQuestion(
                    word = word,
                    targetWord = clean,
                    shuffledLetters = shuffleLetters(chars),
                    assembledLetters = List(chars.size) { null },
                )
            }
            val params = LevelDifficulty.forLevel(1).copy(rounds = questions.size)
            _state.value = PalabraState(
                params = params,
                questions = questions,
                showLevelMap = false,
                isMistakesPractice = true,
            )
        }
    }

    fun startLevel(level: Int) {
        viewModelScope.launch {
            val baseParams = LevelDifficulty.forLevel(level)

            // v1.22.2: ДЕТЕРМИНИРОВАННАЯ загрузка из palabra_levels.json.
            // Раньше startLevel брал wordDao.getRandomWords(400) + фильтр по
            // длине → один и тот же «уровень 5» давал РАЗНОЕ число слов в
            // зависимости от случайной выборки. Теперь 100 уровней × 10 слов
            // ровно из ассета (источник правды: scripts/build_palabra_levels.py).
            val levels = loadLevels()
            val idx = (level - 1).coerceIn(0, (levels.size - 1).coerceAtLeast(0))
            val items = levels.getOrNull(idx) ?: emptyList()
            if (items.isEmpty()) return@launch

            // Для каждого слова берём WordEntity из БД (для TTS/level), а если
            // нет — строим синтетический. PalabraQuestion использует только
            // word.spanish / word.russian, так что синтетика безопасна.
            val questions = items.map { (sp, ru) ->
                val clean = sp.lowercase()
                val dbWord = wordDao.findBySpanish(clean) ?: wordDao.findBySpanish("el $clean")
                // Защита от мусора в БД (старые тестовые сборки могли
                // насеять WordEntity с цифрами). Если DB-запись подозрительная,
                // используем синтетику прямо из JSON.
                val dbValid = dbWord != null
                    && dbWord.russian.isNotBlank()
                    && dbWord.spanish.none { it.isDigit() }
                val word = if (dbValid) dbWord!! else WordEntity(
                    spanish = clean,
                    russian = ru,
                    level   = baseParams.cefr.firstOrNull() ?: "A1",
                )
                val chars = clean.map { it.toString() }
                PalabraQuestion(
                    word             = word,
                    targetWord       = clean,
                    shuffledLetters  = shuffleLetters(chars),
                    assembledLetters = List(chars.size) { null },
                    startTime        = System.currentTimeMillis()
                )
            }

            // Фиксируем точное число раундов из ассета (= 10), чтобы UI
            // и LevelDifficulty были согласованы.
            val params = baseParams.copy(rounds = questions.size)

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
            // Авто-завершение, как только слово собрано целиком — успех или ошибка.
            // Раньше при ошибке ничего не происходило (юзер видел красную
            // подсветку букв и догадывался). Теперь подсветки нет → нужно
            // явно показать isChecked=false вердикт, чтобы вся плашка стала
            // красной. Юзер сбрасывает буквы и пробует снова.
            if (newAssembled.all { it != null }) {
                val assembled = newAssembled.joinToString("") { it?.char ?: "" }.lowercase()
                val isCorrect = assembled == q.targetWord.lowercase()
                if (isCorrect) {
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
                    viewModelScope.launch { ratingUpdater.applyGameAnswer(true) }
                    tts.speak(q.word.spanish)
                    // v1.22.0: «Работа над ошибками»
                    viewModelScope.launch {
                        val itemId = q.targetWord.lowercase()
                        if (s.isMistakesPractice) {
                            // Чистое прохождение в режиме практики → удаляем из mistakes
                            if (updated.mistakesCount == 0) {
                                mistakesDao.removeMistake(GameId.PALABRA, itemId)
                            }
                        } else if (updated.mistakesCount > 0) {
                            // В обычной игре зафиксировал ошибки при сборке → запоминаем
                            mistakesDao.recordMistake(
                                gameId = GameId.PALABRA,
                                itemId = itemId,
                                hint = q.word.russian,
                                main = q.word.spanish,
                            )
                        }
                    }
                } else {
                    // Слово собрано полностью, но неправильно. Показываем красным,
                    // даём юзеру возможность сбросить буквы и попробовать снова.
                    // НЕ переходим к следующему слову, НЕ начисляем очки и
                    // НЕ применяем negative-rating — это просто визуальный
                    // фидбэк «попробуй ещё раз». Юзер тапает по буквам чтобы
                    // их вернуть в палитру и пробует другое сочетание.
                    updated = updated.copy(
                        isChecked = true,
                        isCorrect = false,
                    )
                }
            }
        }
        updateCurrentQuestion(updated)
    }

    fun removeLetter(index: Int) {
        val s = _state.value
        val q = s.questions.getOrNull(s.currentIndex) ?: return

        // Особый случай: в auto-validate режиме слово было помечено как
        // неправильное (целиком собрано но не совпало). Тап по любой букве
        // сбрасывает все слоты обратно в палитру → юзер пробует заново.
        if (q.isChecked && q.isCorrect == false && s.isAutoValidate) {
            val resetAssembled = MutableList<LetterItem?>(q.targetWord.length) { null }
            val resetShuffled = q.shuffledLetters.map { it.copy(isUsed = false) }
            updateCurrentQuestion(q.copy(
                assembledLetters = resetAssembled,
                shuffledLetters  = resetShuffled,
                isChecked        = false,
                isCorrect        = null,
            ))
            return
        }

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
        // Guard against double-fire: auto-validate path at line 189 already
        // emits applyGameAnswer when the last tile drops, and the user may
        // then ALSO press the "Check" button — without this guard the rating
        // system gets two events per question.
        if (q.isChecked) return
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
        viewModelScope.launch { ratingUpdater.applyGameAnswer(isCorrect) }

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
            // v1.22.4: в режиме «работа над ошибками» НЕ помечаем уровень
            // пройденным (это не уровень) и начисляем символический XP
            // только за фактически разобранные слова (по +3 XP/слово).
            val stars = if (s.isMistakesPractice) 0
                        else levelManager.completeLevel(GameId.PALABRA, s.level, percent)

            val p = userProgressDao.getProgressOnce()
            if (p != null) {
                val xpDelta = if (s.isMistakesPractice) s.correctCount * 3 else s.score
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
