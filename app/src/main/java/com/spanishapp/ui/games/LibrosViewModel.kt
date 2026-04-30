package com.spanishapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.LibroProgressDao
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.entity.LibroProgressEntity
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
    val visible: Boolean = false
)

// ── ViewModel ─────────────────────────────────────────────────

@HiltViewModel
class LibrosViewModel @Inject constructor(
    private val dao: LibroProgressDao,
    private val wordDao: WordDao
) : ViewModel() {

    // ── Список рассказов + прогресс ───────────────────────────

    private val _filterLevel = MutableStateFlow("Все")
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
            if (level == "Все") list else list.filter { it.libro.level == level }
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
        }
    }

    // ── Перевод слова / предложения ───────────────────────────

    private val _translation = MutableStateFlow(TranslationState())
    val translation: StateFlow<TranslationState> = _translation.asStateFlow()

    fun lookupWord(word: String, sentence: String) {
        viewModelScope.launch {
            // 1. Ищем само слово
            val cleaned = word.trim().trimEnd { !it.isLetter() }.lowercase()
            val wordResult = wordDao.search(cleaned).first().firstOrNull()
            val wordRu = wordResult?.russian ?: ""

            // 2. Разбираем предложение на значимые слова и ищем каждое
            val sentenceWords = extractContentWords(sentence)
                .distinct()
                .take(10)
                .mapNotNull { w ->
                    val r = wordDao.search(w.lowercase()).first().firstOrNull()
                    r?.let { w to it.russian }
                }

            _translation.value = TranslationState(
                word = word,
                wordRu = wordRu,
                sentence = sentence,
                sentenceWords = sentenceWords,
                visible = true
            )
        }
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
