package com.spanishapp.ui.theory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.TheoryProgressDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel для библиотеки теорий (TheoryLibraryScreen).
 *
 * Подписывается на список прочитанных теорий → UI рисует ✅ напротив тех,
 * что юзер уже открывал. Сами карточки берутся из TheoryContentData
 * (статический объект, не нужен Flow).
 */
@HiltViewModel
class TheoryLibraryViewModel @Inject constructor(
    private val theoryProgressDao: TheoryProgressDao,
) : ViewModel() {

    val readLessonIds: StateFlow<Set<String>> = theoryProgressDao
        .observeAll()
        .map { list -> list.filter { it.firstReadAt > 0 }.map { it.lessonId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())
}
