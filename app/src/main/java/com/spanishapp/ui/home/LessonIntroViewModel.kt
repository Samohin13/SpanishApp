package com.spanishapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.LessonProgressDao
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.entity.LessonProgressEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonIntroViewModel @Inject constructor(
    private val lessonProgressDao: LessonProgressDao,
    private val userProgressDao: UserProgressDao
) : ViewModel() {

    // Вызывается в момент нажатия «ПОЕХАЛИ!»
    fun markLessonComplete(unitId: Int, lessonIndex: Int) {
        viewModelScope.launch {
            val key = "u${unitId}_l${lessonIndex}"
            lessonProgressDao.markComplete(
                LessonProgressEntity(
                    lessonKey   = key,
                    unitId      = unitId,
                    lessonIndex = lessonIndex
                )
            )
            // +15 XP за прохождение урока
            userProgressDao.addXpAndWords(xp = 15, words = 0)
        }
    }
}
