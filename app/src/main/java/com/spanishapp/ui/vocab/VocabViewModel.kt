package com.spanishapp.ui.vocab

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.UserVocabStateDao
import com.spanishapp.data.db.entity.UserVocabStateEntity
import com.spanishapp.service.VocabAggregatorWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * v1.25.28 — ViewModel экрана «Мой словарный запас».
 *
 * При первом открытии форсирует прогон VocabAggregatorWorker чтобы
 * user_vocab_state была актуальной (иначе таблица пуста пока daily
 * worker не отработает первый раз).
 *
 * Combine 5 источников из DAO + считает breakdown'ы.
 */
@HiltViewModel
class VocabViewModel @Inject constructor(
    private val app: Application,
    private val dao: UserVocabStateDao,
) : AndroidViewModel(app) {

    init {
        // Триггерим one-shot worker run — заполнит таблицу из текущих данных юзера
        VocabAggregatorWorker.runNow(app)
    }

    val state: StateFlow<VocabUi> = combine(
        dao.observeAll(),
        dao.observeAddedSince(weekAgo()),
        dao.observeStatusCounts(),
        dao.observeCefrCounts(),
        dao.observeTopUsed(8),
    ) { all, addedThisWeek, statusCounts, cefrCounts, topUsed ->
        val statusMap = statusCounts.associate { it.status to it.cnt }
        val cefrMap = cefrCounts.associate { (it.cefr ?: "?") to it.cnt }
        VocabUi(
            totalKnown = all.size,
            addedThisWeek = addedThisWeek,
            mastered = statusMap["MASTERED"] ?: 0,
            producing = statusMap["PRODUCING"] ?: 0,
            learning = statusMap["LEARNING"] ?: 0,
            seen = statusMap["SEEN"] ?: 0,
            cefrA1 = cefrMap["A1"] ?: 0,
            cefrA2 = cefrMap["A2"] ?: 0,
            cefrB1 = cefrMap["B1"] ?: 0,
            cefrB2 = cefrMap["B2"] ?: 0,
            topUsed = topUsed,
            estimatedCefr = estimateCefr(cefrMap),
            loaded = true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), VocabUi())

    val forgotten: StateFlow<List<UserVocabStateEntity>> =
        dao.observeForgotten(thresholdMs = monthAgo(), limit = 10)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun weekAgo() = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
    private fun monthAgo() = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000

    /**
     * Простая эвристика "твой реальный уровень":
     * - >=80% A1 + >=50% A2 → A2.1
     * - >=80% A1 + <50% A2 → A1.x
     * И т.д.
     * Знаменатели — приблизительные размеры лексикона на каждом уровне.
     */
    private fun estimateCefr(cefrCounts: Map<String, Int>): String {
        val a1 = cefrCounts["A1"] ?: 0
        val a2 = cefrCounts["A2"] ?: 0
        val b1 = cefrCounts["B1"] ?: 0
        val b2 = cefrCounts["B2"] ?: 0
        val a1pct = a1 / 250.0
        val a2pct = a2 / 400.0
        val b1pct = b1 / 600.0
        val b2pct = b2 / 800.0
        return when {
            b2pct >= 0.5 -> "B2"
            b1pct >= 0.5 -> if (b2pct >= 0.2) "B1.2" else "B1"
            a2pct >= 0.5 -> if (b1pct >= 0.2) "A2.2 → B1" else "A2.1"
            a1pct >= 0.5 -> if (a2pct >= 0.2) "A1.2 → A2" else "A1"
            a1pct > 0 -> "A1 начало"
            else -> "—"
        }
    }
}

data class VocabUi(
    val loaded: Boolean = false,
    val totalKnown: Int = 0,
    val addedThisWeek: Int = 0,
    /** breakdown по статусу */
    val mastered: Int = 0,
    val producing: Int = 0,
    val learning: Int = 0,
    val seen: Int = 0,
    /** breakdown по CEFR */
    val cefrA1: Int = 0,
    val cefrA2: Int = 0,
    val cefrB1: Int = 0,
    val cefrB2: Int = 0,
    val topUsed: List<UserVocabStateEntity> = emptyList(),
    val estimatedCefr: String = "—",
)
