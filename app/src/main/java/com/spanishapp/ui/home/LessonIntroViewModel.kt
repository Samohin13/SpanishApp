package com.spanishapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.LessonProgressDao
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.entity.LessonProgressEntity
import com.spanishapp.domain.algorithm.RatingUpdater
import com.spanishapp.service.XpTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonIntroViewModel @Inject constructor(
    private val lessonProgressDao: LessonProgressDao,
    private val userProgressDao: UserProgressDao,
    private val ratingUpdater: RatingUpdater,
    private val xpTracker: XpTracker
) : ViewModel() {

    /**
     * Вызывается в момент завершения урока (экран победы).
     * Помечает урок пройденным, добавляет XP и поднимает skillRating.
     */
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
            // +15 XP за прохождение урока (с дневным трекингом)
            xpTracker.add(xp = 15, words = 0)
            // Skill rating: 5 правильных ответов в среднем за урок.
            repeat(5) { ratingUpdater.applyGameAnswer(correct = true) }
        }
    }

    /**
     * Можно вызвать из LessonSessionScreen на каждое упражнение,
     * чтобы skillRating рос плавнее по ходу урока.
     */
    fun recordExerciseAnswer(correct: Boolean) {
        viewModelScope.launch {
            ratingUpdater.applyGameAnswer(correct)
        }
    }
}
