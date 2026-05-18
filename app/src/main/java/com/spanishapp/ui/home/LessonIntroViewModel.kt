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
    private val xpTracker: XpTracker,
    private val achievementManager: com.spanishapp.service.AchievementManager,
    private val hintBank: com.spanishapp.service.HintBankManager,
) : ViewModel() {

    /**
     * Вызывается в момент завершения урока (экран победы).
     * Помечает урок пройденным, добавляет XP и поднимает skillRating.
     */
    fun markLessonComplete(unitId: Int, lessonIndex: Int) {
        viewModelScope.launch {
            val key = "u${unitId}_l${lessonIndex}"

            // ── Idempotency guard (фикс бага «1 урок → счётчик 3») ──
            // markLessonComplete вызывался из 5 мест (LessonSession, Intro,
            // Content) → каждый вызов делал `lessonsCompleted += 1`.
            // Также юзер мог пройти один и тот же урок повторно — счётчик
            // снова прибавлялся. Теперь: если урок уже в lesson_progress
            // — НЕ инкрементим счётчик и НЕ начисляем XP/рейтинг повторно.
            val alreadyDone = lessonProgressDao.isAlreadyCompleted(key)

            lessonProgressDao.markComplete(
                LessonProgressEntity(
                    lessonKey   = key,
                    unitId      = unitId,
                    lessonIndex = lessonIndex
                )
            )

            if (alreadyDone) {
                // Повторное прохождение — обновили только completed_at,
                // никаких бонусов. Тихо выходим.
                return@launch
            }

            // Первое успешное прохождение — даём всё:
            userProgressDao.getProgressOnce()?.let { p ->
                userProgressDao.update(p.copy(lessonsCompleted = p.lessonsCompleted + 1))
            }
            xpTracker.add(xp = 15, words = 0)
            repeat(5) { ratingUpdater.applyGameAnswer(correct = true) }
            achievementManager.checkAndUnlock()
            // v1.16.0: +2 💡 за прохождение урока (Hint Bank)
            hintBank.award(2, com.spanishapp.service.HintEarnReason.LESSON_COMPLETE)

            com.spanishapp.service.Analytics.lessonCompleted(
                lessonId = key,
                accuracyPercent = 100,
            )
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
