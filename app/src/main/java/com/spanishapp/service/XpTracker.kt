package com.spanishapp.service

import com.spanishapp.data.db.dao.DailyXpDao
import com.spanishapp.data.db.dao.UserProgressDao
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Единая точка начисления XP. Помимо обновления `user_progress.totalXp`
 * пишет столько же в `daily_xp` за сегодняшний день — для графика
 * прогресса в ProfileScreen.
 *
 * Использовать вместо прямого вызова `userProgressDao.addXpAndWords()`.
 */
@Singleton
class XpTracker @Inject constructor(
    private val userProgressDao: UserProgressDao,
    private val dailyXpDao: DailyXpDao
) {
    /**
     * Прибавить amount XP пользователю + дозаписать в дневную статистику.
     * @param words опционально — счётчик "выучил X новых слов".
     */
    suspend fun add(xp: Int, words: Int = 0) {
        if (xp <= 0 && words <= 0) return
        if (xp > 0 || words > 0) {
            userProgressDao.addXpAndWords(xp = xp, words = words)
        }
        if (xp > 0) {
            dailyXpDao.addXp(LocalDate.now().toString(), xp)
        }
    }

    /** Зафиксировать N минут учёбы за сегодня. */
    suspend fun addStudyMinutes(minutes: Int) {
        if (minutes <= 0) return
        dailyXpDao.addMinutes(LocalDate.now().toString(), minutes)
    }

    /** Удобный alias: +1 минута учёбы (для LaunchedEffect-таймера на учебных экранах). */
    suspend fun recordMinute() = addStudyMinutes(1)
}
