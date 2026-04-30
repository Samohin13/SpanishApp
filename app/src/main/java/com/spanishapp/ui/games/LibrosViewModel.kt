package com.spanishapp.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.LibroProgressDao
import com.spanishapp.data.db.entity.LibroProgressEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibroUiItem(
    val libro: Libro,
    val isCompleted: Boolean,
    val bestScore: Int   // 0–100
)

@HiltViewModel
class LibrosViewModel @Inject constructor(
    private val dao: LibroProgressDao
) : ViewModel() {

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

    val filteredItems: StateFlow<List<LibroUiItem>> = combine(items, filterLevel) { list, level ->
        if (level == "Все") list else list.filter { it.libro.level == level }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(level: String) { _filterLevel.value = level }

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
}
