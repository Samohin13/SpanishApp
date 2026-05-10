package com.spanishapp.domain.algorithm

import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.entity.UserProgressEntity
import com.spanishapp.data.repository.SyncRepository
import com.spanishapp.service.AchievementManager
import com.spanishapp.service.StreakService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Событие повышения лиги — для показа баннера в UI.
 */
data class LeaguePromotion(val from: League, val to: League)

/**
 * Применяет результат одного ответа к skillRating пользователя.
 * Используется во всех тренировках (Flashcards, Libros, Listening и др.).
 *
 * @return [LeaguePromotion] если пользователь поднялся в новую лигу, иначе null.
 */
@Singleton
class RatingUpdater @Inject constructor(
    private val userProgressDao: UserProgressDao,
    private val streakService: StreakService,
    private val achievementManager: AchievementManager,
    private val syncRepository: SyncRepository
) {
    private val bgScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun applyAnswer(easeFactor: Float, quality: Int): LeaguePromotion? {
        // Любой ответ — это учебная активность, обновляем стрик и проверяем ачивки.
        runCatching { streakService.touchStreak() }
        runCatching { achievementManager.checkAndUnlock() }
        // Fire-and-forget sync с дебаунсом 1/мин внутри SyncRepository.
        bgScope.launch { runCatching { syncRepository.uploadAll() } }

        val progress: UserProgressEntity = userProgressDao.getProgressOnce() ?: return null

        val oldRating = progress.skillRating
        val oldLeague = LeagueResolver.fromTier(progress.currentLeague)

        var newRating = SkillRatingSystem.applyAnswer(oldRating, easeFactor, quality)
        var rawDelta = newRating - oldRating

        // Daily cap on POSITIVE gains only — losses are never capped (so
        // mistakes still hurt). Resets at midnight (date change in
        // LocalDate.now()).
        if (rawDelta > 0) {
            val today = java.time.LocalDate.now().toString()
            val gainedToday =
                if (progress.dailyRatingGainDate == today) progress.dailyRatingGain else 0
            val remainingCap =
                (SkillRatingSystem.DAILY_GAIN_CAP - gainedToday).coerceAtLeast(0)
            if (remainingCap == 0) {
                // Cap exhausted — drop the gain entirely but still update
                // last-rating-update so decay doesn't fire.
                rawDelta = 0
                newRating = oldRating
            } else if (rawDelta > remainingCap) {
                rawDelta = remainingCap
                newRating = oldRating + rawDelta
            }
            userProgressDao.bumpDailyRatingGain(
                date = today,
                addedToday = gainedToday + rawDelta
            )
        }

        val newLeague = LeagueResolver.fromRating(newRating)

        if (newRating == oldRating && newLeague.tier == oldLeague.tier) return null

        userProgressDao.updateSkillRating(
            rating = newRating,
            league = newLeague.tier,
            ts = System.currentTimeMillis()
        )

        return if (newLeague.tier > oldLeague.tier) LeaguePromotion(oldLeague, newLeague) else null
    }

    /**
     * Wrapper для игр. Игры — это тренировка реакции/памяти, не настоящее
     * SRS-изучение слов, поэтому quality занижен:
     *  • правильный ответ → quality 3 (вместо 4) — даёт ~30% от полного гейна
     *  • ошибка             → quality 2           — мелкая потеря
     */
    suspend fun applyGameAnswer(correct: Boolean): LeaguePromotion? =
        applyAnswer(easeFactor = 2.5f, quality = if (correct) 3 else 2)
}
