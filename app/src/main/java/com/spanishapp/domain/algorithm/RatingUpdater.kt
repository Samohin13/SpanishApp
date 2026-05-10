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

        val newRating = SkillRatingSystem.applyAnswer(oldRating, easeFactor, quality)
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
     * Упрощённая обёртка для игр без контекста SM2:
     * easeFactor=2.5 (нейтральный), quality 4 на правильный ответ и 2 на ошибку.
     */
    suspend fun applyGameAnswer(correct: Boolean): LeaguePromotion? =
        applyAnswer(easeFactor = 2.5f, quality = if (correct) 4 else 2)
}
