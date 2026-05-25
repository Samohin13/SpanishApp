package com.spanishapp.service

import com.spanishapp.data.db.dao.DailyXpDao
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.repository.LeaderboardRepository
import com.spanishapp.domain.algorithm.XpSystem
import dagger.Lazy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Единая точка начисления XP. Помимо обновления `user_progress.totalXp`
 * пишет столько же в `daily_xp` за сегодняшний день — для графика
 * прогресса в ProfileScreen.
 *
 * v1.21.1: после каждого изменения XP проактивно вызываем
 * LeaderboardRepository.syncSelf() (rate-limited 30с) — раньше синк
 * был только при открытии Leaderboard, тестеры не появлялись в таблице
 * пока не открывали её повторно.
 *
 * Использовать вместо прямого вызова `userProgressDao.addXpAndWords()`.
 */
@Singleton
class XpTracker @Inject constructor(
    private val userProgressDao: UserProgressDao,
    private val dailyXpDao: DailyXpDao,
    // Lazy чтобы не было цикла DI: LeaderboardRepository не зависит от XpTracker.
    private val leaderboardRepository: Lazy<LeaderboardRepository>,
    private val uiSound: UiSoundPlayer,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Прибавить amount XP пользователю + дозаписать в дневную статистику.
     * @param words опционально — счётчик "выучил X новых слов".
     */
    suspend fun add(xp: Int, words: Int = 0) {
        if (xp <= 0 && words <= 0) return
        // Снимаем «до» снимок чтобы определить переход уровня после записи.
        val before = userProgressDao.getProgressOnce()
        val levelBefore = if (before != null) XpSystem.levelForXp(before.totalXp) else 1

        if (xp > 0 || words > 0) {
            userProgressDao.addXpAndWords(xp = xp, words = words)
        }
        if (xp > 0) {
            dailyXpDao.addXp(LocalDate.now().toString(), xp)
        }

        // SFX: XP gain → короткий «дзинь». Отложен на 700мс чтобы не
        // налипнуть на CORRECT-звук (правильный ответ → 5-10 XP).
        if (xp > 0) {
            scope.launch {
                delay(700)
                uiSound.play(UiSoundPlayer.Sound.XP, volume = 0.7f)
                // Level up — звук фанфар поверх XP. Сравниваем уровень
                // до и после изменения; если поднялся — играем.
                val after = userProgressDao.getProgressOnce()
                if (after != null) {
                    val levelAfter = XpSystem.levelForXp(after.totalXp)
                    if (levelAfter > levelBefore) {
                        delay(400)
                        uiSound.play(UiSoundPlayer.Sound.LEVEL_UP)
                    }
                }
            }
        }
        // Сетевой синк в фоне — не блокируем UI. Внутри есть rate-limit 30с.
        scope.launch {
            try { leaderboardRepository.get().syncSelf() } catch (_: Exception) {}
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
