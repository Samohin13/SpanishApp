package com.spanishapp.domain.algorithm

import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.entity.UserProgressEntity
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
    private val userProgressDao: UserProgressDao
) {

    suspend fun applyAnswer(easeFactor: Float, quality: Int): LeaguePromotion? {
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
}
