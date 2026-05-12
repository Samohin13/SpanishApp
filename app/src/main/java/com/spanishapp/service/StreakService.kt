package com.spanishapp.service

import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.entity.UserProgressEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Результат "касания" стрика после любого учебного действия.
 *
 * @param newStreak новое значение текущей серии
 * @param usedFreeze была ли применена заморозка (пропущенный день)
 * @param isNewRecord достигнут ли новый рекорд (longestStreak)
 * @param freezesAvailable сколько осталось заморозок
 */
data class StreakResult(
    val newStreak: Int,
    val usedFreeze: Boolean,
    val isNewRecord: Boolean,
    val freezesAvailable: Int
)

/**
 * Единая точка обновления стрика.
 * Любая тренировка/урок/игра должны вызывать [touchStreak] по завершении.
 *
 * Логика:
 * - В тот же день — без изменений
 * - Через 1 день — +1
 * - Через 2 дня + есть заморозка — −1 заморозка, +1
 * - Через >1 день без заморозки — сброс в 1
 * - По понедельникам пополняем до 2 заморозок
 */
@Singleton
class StreakService @Inject constructor(
    private val userProgressDao: UserProgressDao,
    // dagger.Lazy avoids a construction-time cycle: AchievementManager
    // depends on userProgressDao too, but we only need it at runtime.
    private val achievementManager: dagger.Lazy<AchievementManager>
) {
    private val _streakEvents = MutableSharedFlow<StreakResult>(extraBufferCapacity = 4)
    val streakEvents: SharedFlow<StreakResult> = _streakEvents.asSharedFlow()

    suspend fun touchStreak(now: LocalDate = LocalDate.now()): StreakResult {
        val p: UserProgressEntity = userProgressDao.getProgressOnce()
            ?: UserProgressEntity().also { userProgressDao.insert(it) }
                .also { return touchStreak(now) }

        // Восполнение заморозок раз в неделю (по понедельникам)
        var freezes = p.streakFreezesAvailable
        val mondayThisWeek = now.with(DayOfWeek.MONDAY).toString()
        var resetDate = p.weeklyFreezeResetDate
        if (resetDate != mondayThisWeek) {
            freezes = MAX_FREEZES
            resetDate = mondayThisWeek
        }

        val today = now.toString()
        val last = p.lastStreakUpdateDate

        // Этот день уже учитан — обновим только дату восполнения если сменилась.
        if (last == today) {
            if (resetDate != p.weeklyFreezeResetDate || freezes != p.streakFreezesAvailable) {
                userProgressDao.updateStreakFull(
                    streak = p.currentStreak,
                    lastStudyMs = p.lastStudyDate,
                    freezes = freezes,
                    lastUpdateDate = last,
                    resetDate = resetDate
                )
            }
            val res = StreakResult(p.currentStreak, false, false, freezes)
            _streakEvents.tryEmit(res)
            return res
        }

        val newStreak: Int
        var usedFreeze = false
        if (last.isBlank()) {
            // Самый первый учебный день
            newStreak = 1
        } else {
            val lastDate = runCatching { LocalDate.parse(last) }.getOrNull()
            val days = lastDate?.let { ChronoUnit.DAYS.between(it, now).toInt() } ?: 99
            newStreak = when {
                days == 1 -> p.currentStreak + 1
                days == 2 && freezes > 0 -> {
                    freezes -= 1
                    usedFreeze = true
                    p.currentStreak + 1
                }
                else -> 1
            }
        }

        val isRecord = newStreak > p.longestStreak
        userProgressDao.updateStreakFull(
            streak = newStreak,
            lastStudyMs = System.currentTimeMillis(),
            freezes = freezes,
            lastUpdateDate = today,
            resetDate = resetDate
        )

        val res = StreakResult(newStreak, usedFreeze, isRecord, freezes)
        _streakEvents.tryEmit(res)
        // streak_3 / streak_7 / streak_30 / streak_100 used to wait for the
        // next HomeScreen open to fire — now they unlock the moment the
        // streak actually advances, regardless of which screen triggered it.
        if (newStreak != p.currentStreak) {
            runCatching { achievementManager.get().checkAndUnlock() }
        }
        return res
    }

    companion object {
        const val MAX_FREEZES = 2
    }
}
