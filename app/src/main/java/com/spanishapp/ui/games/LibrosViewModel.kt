package com.spanishapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.LibroProgressDao
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.entity.LibroProgressEntity
import com.spanishapp.data.repository.GeminiTranslator
import com.spanishapp.domain.algorithm.LeaguePromotion
import com.spanishapp.domain.algorithm.RatingUpdater
import com.spanishapp.domain.algorithm.SpanishLemmatizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI-модели ─────────────────────────────────────────────────

data class LibroUiItem(
    val libro: Libro,
    val isCompleted: Boolean,
    val bestScore: Int   // 0–100 %
)

data class TranslationState(
    val word: String = "",
    val wordRu: String = "",
    val sentence: String = "",
    val sentenceWords: List<Pair<String, String>> = emptyList(), // es → ru
    val visible: Boolean = false,
    val isLoadingAi: Boolean = false,
    val fromAi: Boolean = false  // перевод получен через Gemini-fallback
)

// ── ViewModel ─────────────────────────────────────────────────

@HiltViewModel
class LibrosViewModel @Inject constructor(
    private val dao: LibroProgressDao,
    private val wordDao: WordDao,
    private val ratingUpdater: RatingUpdater,
    private val geminiTranslator: GeminiTranslator,
    private val achievementManager: com.spanishapp.service.AchievementManager,
) : ViewModel() {

    private val _leaguePromotions = MutableSharedFlow<LeaguePromotion>(replay = 0, extraBufferCapacity = 1)
    val leaguePromotions: SharedFlow<LeaguePromotion> = _leaguePromotions.asSharedFlow()

    // ── Список рассказов + прогресс ───────────────────────────

    // "all" = stable sentinel meaning «show every level». Locale-independent
    // so the filter survives a UI language switch. UI resolves it to a
    // localized label via stringResource at display time.
    private val _filterLevel = MutableStateFlow("all")
    val filterLevel: StateFlow<String> = _filterLevel

    val items: StateFlow<List<LibroUiItem>> = dao.getAll()
        .map { progressList ->
            val progressMap = progressList.associateBy { it.libroId }
            LibrosData.all.map { libro ->
                val p = progressMap[libro.id]
                LibroUiItem(libro, p?.isCompleted ?: false, p?.bestScore ?: 0)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredItems: StateFlow<List<LibroUiItem>> =
        combine(items, filterLevel) { list, level ->
            if (level == "all") list else list.filter { it.libro.level == level }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(level: String) { _filterLevel.value = level }

    // ── Сохранение результата теста ───────────────────────────

    fun saveResult(libroId: Int, correctCount: Int, totalCount: Int) {
        val score = if (totalCount > 0) correctCount * 100 / totalCount else 0
        val passed = correctCount >= LibrosData.PASS_CORRECT
        viewModelScope.launch {
            val existing = dao.getById(libroId)
            dao.upsert(
                LibroProgressEntity(
                    libroId = libroId,
                    isCompleted = passed || (existing?.isCompleted == true),
                    bestScore = maxOf(score, existing?.bestScore ?: 0),
                    completedAt = System.currentTimeMillis()
                )
            )
            // Skill rating: один «ответ» за каждый правильный/неправильный
            // ease=2.5 (нейтральная сложность для текстовых вопросов).
            repeat(correctCount) {
                val promo = ratingUpdater.applyAnswer(easeFactor = 2.5f, quality = 4)
                if (promo != null) _leaguePromotions.tryEmit(promo)
            }
            val mistakes = totalCount - correctCount
            repeat(mistakes) {
                val promo = ratingUpdater.applyAnswer(easeFactor = 2.5f, quality = 1)
                if (promo != null) _leaguePromotions.tryEmit(promo)
            }
            // Trigger reading-related achievement check after every Libro
            // completion (was silently missing — owner-reported gap).
            runCatching { achievementManager.checkAndUnlock() }
        }
    }

    // ── Перевод слова / предложения ───────────────────────────

    private val _translation = MutableStateFlow(TranslationState())
    val translation: StateFlow<TranslationState> = _translation.asStateFlow()

    fun lookupWord(word: String, sentence: String) {
        viewModelScope.launch {
            val cleaned = word.trim().trimEnd { !it.isLetter() }.trimStart { !it.isLetter() }.lowercase()

            // 1. Ищем слово: сначала точное совпадение, затем лемматизация
            val wordResult = findWithLemmatization(cleaned)
            val localTranslation = wordResult?.russian ?: ""

            // 2. Разбираем предложение на значимые слова и ищем каждое
            val sentenceWords = extractContentWords(sentence)
                .distinct()
                .take(10)
                .mapNotNull { w ->
                    val r = findWithLemmatization(w.lowercase())
                    r?.let { w to it.russian }
                }

            // 3. Сразу показываем local-результат — даже если перевод пуст,
            // у юзера откроется бокс с "загрузка AI…" вместо тишины.
            val noLocal = localTranslation.isBlank()
            _translation.value = TranslationState(
                word = word,
                wordRu = localTranslation,
                sentence = sentence,
                sentenceWords = sentenceWords,
                visible = true,
                isLoadingAi = noLocal,
                fromAi = false
            )

            // 4. Fallback на Gemini, если локально не нашли
            if (noLocal) {
                val aiTranslation = geminiTranslator.translateWord(cleaned, sentence)
                // Юзер мог уже закрыть подсказку — обновим только если она ещё видна
                _translation.update { current ->
                    if (current.visible && current.word == word) {
                        current.copy(
                            wordRu = aiTranslation.ifBlank { "—" },
                            isLoadingAi = false,
                            fromAi = aiTranslation.isNotBlank()
                        )
                    } else current
                }
            }
        }
    }

    private suspend fun findWithLemmatization(word: String): com.spanishapp.data.db.entity.WordEntity? {
        // 1. Exact match (already lowercase).
        wordDao.findBySpanish(word)?.let { return it }

        // 2. Many noun entries are stored WITH an article ("el nombre", "la casa").
        //    Try each article prefix BEFORE falling back to verb lemmatization —
        //    otherwise "nombre" would be wrongly resolved to the verb "nombrar"
        //    (called "называть") instead of the noun "имя".
        for (article in listOf("el ", "la ", "los ", "las ", "un ", "una ")) {
            wordDao.findBySpanish(article + word)?.let { return it }
        }

        // 3. Lemmatization candidates (verb infinitives, noun base forms, etc.).
        //    Skip the first candidate — it's the same as `word`, already tried.
        val candidates = SpanishLemmatizer.candidates(word).drop(1)
        for (candidate in candidates) {
            wordDao.findBySpanish(candidate)?.let { return it }
            // Also try article-prefixed for any candidate that might be a noun.
            for (article in listOf("el ", "la ")) {
                wordDao.findBySpanish(article + candidate)?.let { return it }
            }
        }

        // 4. Final fallback: LIKE contains search (catches compound words, etc.).
        return wordDao.search(word).first().firstOrNull()
    }

    fun dismissTranslation() {
        _translation.value = TranslationState()
    }

    private fun extractContentWords(sentence: String): List<String> {
        val stopWords = setOf(
            "el", "la", "los", "las", "un", "una", "unos", "unas",
            "en", "de", "a", "que", "y", "o", "se", "le", "lo", "me", "te",
            "su", "sus", "mi", "tu", "es", "son", "al", "del", "por", "para",
            "con", "sin", "pero", "si", "no", "ya", "hay", "muy", "más", "tan",
            "bien", "mal", "todo", "toda", "cuando", "como", "donde", "que"
        )
        return sentence
            .split(Regex("[\\s.,;:!?¡¿\n\"'()\\-]+"))
            .map { it.trim().trimEnd { c -> !c.isLetter() }.trimStart { c -> !c.isLetter() } }
            .filter { it.length > 2 && it.lowercase() !in stopWords }
    }
}
