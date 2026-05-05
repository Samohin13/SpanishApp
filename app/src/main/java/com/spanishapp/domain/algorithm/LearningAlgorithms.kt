package com.spanishapp.domain.algorithm

import com.spanishapp.data.db.entity.WordEntity
import kotlin.math.max
import kotlin.math.roundToInt

// ═════════════════════════════════════════════════════════════
//  SM-2 SPACED REPETITION ALGORITHM
//  Quality: 0-2 = fail, 3 = hard, 4 = good, 5 = easy
// ═════════════════════════════════════════════════════════════
object SM2 {

    fun review(word: WordEntity, quality: Int): WordEntity {
        require(quality in 0..5)

        val newRepetitions: Int
        val newInterval: Int
        val newEF: Float

        if (quality < 3) {
            // Failed — reset repetitions, review tomorrow
            newRepetitions = 0
            newInterval = 1
            newEF = word.easeFactor   // EF unchanged on fail
        } else {
            newRepetitions = word.repetitions + 1
            newInterval = when (word.repetitions) {
                0    -> 1
                1    -> 6
                else -> (word.interval * word.easeFactor).roundToInt()
            }
            // EF formula: EF' = EF + (0.1 - (5-q)*(0.08 + (5-q)*0.02))
            val delta = 0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f)
            newEF = max(1.3f, word.easeFactor + delta)
        }

        val nextMs = System.currentTimeMillis() + newInterval * 86_400_000L

        return word.copy(
            repetitions    = newRepetitions,
            interval       = newInterval,
            easeFactor     = newEF,
            nextReview     = nextMs,
            isLearned      = newRepetitions >= 3,
            totalReviews   = word.totalReviews + 1,
            correctReviews = if (quality >= 3) word.correctReviews + 1 else word.correctReviews
        )
    }

    // Map simple 3-button UI (hard / good / easy) to quality
    fun qualityFromButton(button: ReviewButton): Int = when (button) {
        ReviewButton.HARD -> 2
        ReviewButton.GOOD -> 4
        ReviewButton.EASY -> 5
    }
}

enum class ReviewButton { HARD, GOOD, EASY }

// ═════════════════════════════════════════════════════════════
//  XP SYSTEM
// ═════════════════════════════════════════════════════════════
object XpSystem {

    // XP awarded per action
    const val WORD_CORRECT     = 5
    const val WORD_EASY        = 10
    const val LESSON_COMPLETE  = 25
    const val DIALOGUE_PERFECT = 40
    const val DIALOGUE_PASS    = 20
    const val DAILY_GOAL_HIT   = 15
    const val STREAK_BONUS_PER_DAY = 2   // extra XP per streak day, up to 30
    const val CONJUGATION_CORRECT = 8
    const val AI_CHAT_MESSAGE  = 3       // small reward for practicing conversation

    // Level thresholds (XP needed to reach each level)
    private val LEVEL_THRESHOLDS = intArrayOf(
        0, 100, 250, 450, 700, 1000,       // 1–6
        1350, 1750, 2200, 2700, 3250,      // 7–11
        3850, 4500, 5200, 5950, 6750,      // 12–16
        7600, 8500, 9450, 10_450, 11_500,  // 17–21
        12_600, 13_750, 14_950, 16_200,    // 22–25
        17_500, 18_850, 20_250, 21_700, 23_200  // 26–30
    )

    fun levelForXp(totalXp: Int): Int {
        for (i in LEVEL_THRESHOLDS.indices.reversed()) {
            if (totalXp >= LEVEL_THRESHOLDS[i]) return i + 1
        }
        return 1
    }

    fun xpForNextLevel(totalXp: Int): Int {
        val lvl = levelForXp(totalXp) - 1
        return if (lvl + 1 < LEVEL_THRESHOLDS.size) LEVEL_THRESHOLDS[lvl + 1] else Int.MAX_VALUE
    }

    fun progressToNextLevel(totalXp: Int): Float {
        val lvl = levelForXp(totalXp) - 1
        val current = LEVEL_THRESHOLDS.getOrElse(lvl) { 0 }
        val next = LEVEL_THRESHOLDS.getOrElse(lvl + 1) { current + 1000 }
        return ((totalXp - current).toFloat() / (next - current)).coerceIn(0f, 1f)
    }

    fun streakBonus(streak: Int): Int =
        minOf(streak, 30) * STREAK_BONUS_PER_DAY
}

// ═════════════════════════════════════════════════════════════
//  STREAK MANAGER
// ═════════════════════════════════════════════════════════════
object StreakManager {

    /**
     * Returns (newStreak, bonusXp)
     * Call once per study session, after saving that the user studied today.
     */
    fun calculateStreak(lastStudyEpochMs: Long, currentStreak: Int): Pair<Int, Int> {
        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L
        val daysSinceLast = ((now - lastStudyEpochMs) / dayMs).toInt()

        val newStreak = when {
            daysSinceLast == 0 -> currentStreak          // same day, no change
            daysSinceLast == 1 -> currentStreak + 1      // consecutive day ✓
            else               -> 1                       // streak broken
        }

        val bonus = if (daysSinceLast == 1) XpSystem.streakBonus(newStreak) else 0
        return newStreak to bonus
    }

    fun isStreakAtRisk(lastStudyEpochMs: Long): Boolean {
        val dayMs = 86_400_000L
        val elapsed = System.currentTimeMillis() - lastStudyEpochMs
        // At risk if 20+ hours without study (warn with notification)
        return elapsed > (20 * 3600_000L) && elapsed < (2 * dayMs)
    }
}

// ═════════════════════════════════════════════════════════════
//  ADAPTIVE LEARNING ENGINE
//  Decides what content to show next based on user state
// ═════════════════════════════════════════════════════════════
object AdaptiveLearning {

    data class SessionPlan(
        val newWords: Int,
        val reviewWords: Int,
        val includeConjugation: Boolean,
        val includeDialogue: Boolean,
        val estimatedMinutes: Int
    )

    fun planSession(
        dueWordsCount: Int,
        dailyGoalMinutes: Int,
        currentLevel: String,
        studiedTodayMinutes: Int,
        weakWordsCount: Int
    ): SessionPlan {
        val remaining = (dailyGoalMinutes - studiedTodayMinutes).coerceAtLeast(5)

        // ~1 min per word review, ~2 min per new word, ~5 min for conjugation
        val reviewBudget = minOf(dueWordsCount, remaining * 60 / 90)
        val newBudget    = minOf(10, (remaining - reviewBudget) / 2).coerceAtLeast(0)

        val includeConj = currentLevel in listOf("A2", "B1", "B2") && remaining > 10
        val includeDialog = remaining > 8 && dueWordsCount < 5  // dialogue when review pile is small

        return SessionPlan(
            newWords             = newBudget,
            reviewWords          = reviewBudget,
            includeConjugation   = includeConj,
            includeDialogue      = includeDialog,
            estimatedMinutes     = remaining
        )
    }

    // Suggest Spanish level upgrade
    fun shouldLevelUp(
        wordsLearned: Int,
        lessonsCompleted: Int,
        currentLevel: String
    ): Boolean = when (currentLevel) {
        "A1" -> wordsLearned >= 200 && lessonsCompleted >= 5
        "A2" -> wordsLearned >= 500 && lessonsCompleted >= 12
        "B1" -> wordsLearned >= 900 && lessonsCompleted >= 20
        else -> false
    }
}

// ═════════════════════════════════════════════════════════════
//  SKILL RATING SYSTEM (ELO-подобный, с медленным затуханием)
//  Растёт за правильные ответы на сложные слова, падает за
//  ошибки на лёгких; затухает после 3 дней простоя.
// ═════════════════════════════════════════════════════════════
object SkillRatingSystem {

    const val START_RATING = 1000
    const val FLOOR_RATING = 800
    const val CEILING_RATING = 3000
    const val K_FACTOR = 8

    // Затухание
    const val DECAY_GRACE_DAYS = 3
    const val DECAY_PER_DAY = 2
    const val DECAY_PEAK_BUFFER = 200   // не падаем ниже peak - 200

    private const val DAY_MS = 86_400_000L

    /**
     * Изменяет рейтинг по результату одного ответа.
     * @param easeFactor SM-2 ease factor (1.3..3+). Чем ниже — тем сложнее слово.
     * @param quality SM-2 quality 0..5. <3 = ошибка.
     */
    fun applyAnswer(currentRating: Int, easeFactor: Float, quality: Int): Int {
        val difficulty = (2.5f - easeFactor).coerceIn(-1.2f, 1.2f)
        val deltaF = when {
            quality >= 5 -> K_FACTOR * (1.0f + difficulty * 0.5f)
            quality == 4 -> K_FACTOR * (0.6f + difficulty * 0.4f)
            quality == 3 -> K_FACTOR * 0.3f
            quality == 2 -> K_FACTOR * 0.2f
            else         -> -K_FACTOR * (1.0f - difficulty * 0.4f)
        }
        val delta = deltaF.roundToInt()
        return (currentRating + delta).coerceIn(FLOOR_RATING, CEILING_RATING)
    }

    /**
     * Затухание после долгого простоя.
     * @return новый рейтинг (или тот же, если простой <= GRACE).
     */
    fun applyDecay(currentRating: Int, peakRating: Int, lastUpdateMs: Long, nowMs: Long): Int {
        if (lastUpdateMs <= 0L) return currentRating
        val days = ((nowMs - lastUpdateMs) / DAY_MS).toInt()
        if (days <= DECAY_GRACE_DAYS) return currentRating
        val penalty = (days - DECAY_GRACE_DAYS) * DECAY_PER_DAY
        val floor = max(FLOOR_RATING, peakRating - DECAY_PEAK_BUFFER)
        return max(floor, currentRating - penalty)
    }
}

// ═════════════════════════════════════════════════════════════
//  ЛИГИ — «Путь до Мадрида»
//  Персональный путь по городам Испании от окраины к столице.
//  Не еженедельный сброс — постоянная привязка к skillRating.
// ═════════════════════════════════════════════════════════════
data class League(
    val tier: Int,
    val city: String,        // отображается в UI
    val region: String,      // подпись
    val ratingFrom: Int,
    val ratingTo: Int,       // включительно для последней
    val emoji: String,
    val accentColorHex: Long // 0xFFRRGGBB
)

object LeagueResolver {

    val LEAGUES: List<League> = listOf(
        League(1, "Aldea perdida",          "Extremadura",            0,    1099, "🏚️", 0xFF8D6E63),
        League(2, "Santiago de Compostela", "Galicia",             1100,    1299, "⛪",  0xFF4DB6AC),
        League(3, "Bilbao",                 "País Vasco",          1300,    1499, "⚓",  0xFF455A64),
        League(4, "Zaragoza",               "Aragón",              1500,    1699, "🏛️", 0xFF8E24AA),
        League(5, "Valencia",               "Comunidad Valenciana",1700,    1899, "🍊",  0xFFFF7043),
        League(6, "Sevilla",                "Andalucía",           1900,    2099, "💃",  0xFFD81B60),
        League(7, "Barcelona",              "Cataluña",            2100,    2299, "🏰",  0xFF1976D2),
        League(8, "Madrid — ¡La Capital!",  "Madrid",              2300, 999999, "👑",  0xFFC62828)
    )

    fun fromRating(rating: Int): League =
        LEAGUES.firstOrNull { rating in it.ratingFrom..it.ratingTo } ?: LEAGUES.first()

    fun fromTier(tier: Int): League =
        LEAGUES.firstOrNull { it.tier == tier } ?: LEAGUES.first()

    fun next(current: League): League? =
        LEAGUES.firstOrNull { it.tier == current.tier + 1 }

    /** Прогресс внутри текущей лиги: 0f..1f (для прогресс-бара). Madrid = 1f. */
    fun progressInLeague(rating: Int): Float {
        val l = fromRating(rating)
        if (l.tier == LEAGUES.size) return 1f
        val span = (l.ratingTo - l.ratingFrom).coerceAtLeast(1)
        return ((rating - l.ratingFrom).toFloat() / span).coerceIn(0f, 1f)
    }
}

// ═════════════════════════════════════════════════════════════
//  MASTERY RATING (испанские флаги по категориям, 0..5)
// ═════════════════════════════════════════════════════════════
object MasteryRating {

    private val THRESHOLDS = floatArrayOf(0.10f, 0.30f, 0.50f, 0.75f, 0.90f)

    /** Комбинированный score: 60% покрытия + 40% точности (если ревью >= 5). */
    fun score(total: Int, learned: Int, totalReviews: Int, correctReviews: Int): Float {
        if (total <= 0) return 0f
        val coverage = (learned.toFloat() / total).coerceIn(0f, 1f)
        return if (totalReviews >= 5) {
            val accuracy = (correctReviews.toFloat() / totalReviews).coerceIn(0f, 1f)
            0.6f * coverage + 0.4f * accuracy
        } else {
            0.6f * coverage
        }
    }

    /** Количество испанских флагов 0..5. */
    fun flags(total: Int, learned: Int, totalReviews: Int, correctReviews: Int): Int {
        val s = score(total, learned, totalReviews, correctReviews)
        var count = 0
        for (t in THRESHOLDS) if (s >= t) count++
        return count
    }
}