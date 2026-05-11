package com.spanishapp

import com.spanishapp.domain.algorithm.LeagueResolver
import com.spanishapp.domain.algorithm.MasteryRating
import com.spanishapp.domain.algorithm.SkillRatingSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SkillRatingSystemTest {

    @Test
    fun easyAnswerOnHardWordIncreasesRatingMore() {
        val start = 1500
        // EF=1.4 → сложное слово, difficulty ≈ 1.1
        val hardEasy = SkillRatingSystem.applyAnswer(start, easeFactor = 1.4f, quality = 5)
        // EF=2.8 → лёгкое слово, difficulty ≈ -0.3
        val easyEasy = SkillRatingSystem.applyAnswer(start, easeFactor = 2.8f, quality = 5)
        assertTrue("hard easy ($hardEasy) should beat easy easy ($easyEasy)", hardEasy - start > easyEasy - start)
    }

    @Test
    fun mistakeOnEasyWordHurtsMore() {
        val start = 1500
        val mistakeOnEasy = SkillRatingSystem.applyAnswer(start, easeFactor = 2.8f, quality = 1)
        val mistakeOnHard = SkillRatingSystem.applyAnswer(start, easeFactor = 1.4f, quality = 1)
        assertTrue("mistake on easy ($mistakeOnEasy) should be worse than on hard ($mistakeOnHard)",
            mistakeOnEasy < mistakeOnHard)
    }

    @Test
    fun ratingFloorIsRespected() {
        val r = SkillRatingSystem.applyAnswer(SkillRatingSystem.FLOOR_RATING, easeFactor = 2.5f, quality = 0)
        assertEquals(SkillRatingSystem.FLOOR_RATING, r)
    }

    @Test
    fun ratingCeilingIsRespected() {
        val r = SkillRatingSystem.applyAnswer(SkillRatingSystem.CEILING_RATING, easeFactor = 1.4f, quality = 5)
        assertEquals(SkillRatingSystem.CEILING_RATING, r)
    }

    @Test
    fun decayDoesNothingDuringGracePeriod() {
        val now = 100L * 86_400_000L
        val twoDaysAgo = now - 2 * 86_400_000L
        val r = SkillRatingSystem.applyDecay(currentRating = 1500, peakRating = 1500, lastUpdateMs = twoDaysAgo, nowMs = now)
        assertEquals(1500, r)
    }

    @Test
    fun decayKicksInAfterGracePeriod() {
        val now = 100L * 86_400_000L
        val tenDaysAgo = now - 10 * 86_400_000L
        // 10 - 3 grace = 7 days × 2 = -14
        val r = SkillRatingSystem.applyDecay(currentRating = 1500, peakRating = 1500, lastUpdateMs = tenDaysAgo, nowMs = now)
        assertEquals(1486, r)
    }

    @Test
    fun decayDoesNotGoBelowPeakBuffer() {
        val now = 2000L * 86_400_000L
        val long_ago = now - 1000L * 86_400_000L   // 1000 days ago, NOT zero
        // peak 1500 → floor max(800, 1300) = 1300. Currently 1310.
        // 1000 days × 2/day - 3 grace = -1994 penalty, clamped to floor 1300.
        val r = SkillRatingSystem.applyDecay(currentRating = 1310, peakRating = 1500, lastUpdateMs = long_ago, nowMs = now)
        assertEquals(1300, r)
    }

    @Test
    fun decayWithNoLastUpdateNoOp() {
        val r = SkillRatingSystem.applyDecay(currentRating = 1500, peakRating = 1500, lastUpdateMs = 0L, nowMs = 100L * 86_400_000L)
        assertEquals(1500, r)
    }
}

class LeagueResolverTest {

    @Test
    fun startingRatingMapsToAldea() {
        val l = LeagueResolver.fromRating(1000)
        assertEquals(1, l.tier)
        assertEquals("Aldea perdida", l.city)
    }

    @Test
    fun ratingThresholdsAreContiguous() {
        // 1099 → tier 1, 1100 → tier 2
        assertEquals(1, LeagueResolver.fromRating(1099).tier)
        assertEquals(2, LeagueResolver.fromRating(1100).tier)
        assertEquals(7, LeagueResolver.fromRating(2299).tier)
        assertEquals(8, LeagueResolver.fromRating(2300).tier)
    }

    @Test
    fun veryHighRatingIsMadrid() {
        assertEquals("Madrid — ¡La Capital!", LeagueResolver.fromRating(5000).city)
    }

    @Test
    fun nextOfMadridIsNull() {
        val madrid = LeagueResolver.fromTier(8)
        assertNull(LeagueResolver.next(madrid))
    }

    @Test
    fun nextOfBilbaoIsZaragoza() {
        val bilbao = LeagueResolver.fromTier(3)
        val next = LeagueResolver.next(bilbao)
        assertNotNull(next)
        assertEquals("Zaragoza", next!!.city)
    }

    @Test
    fun progressInLeagueRespectsBounds() {
        // 1100 = старт Santiago (tier 2). Progress ≈ 0.0
        assertTrue(LeagueResolver.progressInLeague(1100) < 0.05f)
        // 1299 = конец Santiago. Progress ≈ 1.0
        assertTrue(LeagueResolver.progressInLeague(1299) > 0.95f)
        // Madrid всегда 1.0
        assertEquals(1f, LeagueResolver.progressInLeague(5000), 0.001f)
    }
}

class MasteryRatingTest {

    @Test
    fun zeroTotalGivesZeroFlags() {
        assertEquals(0, MasteryRating.flags(total = 0, learned = 0, totalReviews = 0, correctReviews = 0))
    }

    @Test
    fun fewReviewsIgnoreAccuracy() {
        // 50% покрытия, точность мусорная — но totalReviews < 5, поэтому только покрытие.
        // 0.6 * 0.5 = 0.3 → 2 флага (0.30 порог)
        val flags = MasteryRating.flags(total = 100, learned = 50, totalReviews = 2, correctReviews = 0)
        assertEquals(2, flags)
    }

    @Test
    fun fullMasteryGives5Flags() {
        val flags = MasteryRating.flags(total = 100, learned = 100, totalReviews = 200, correctReviews = 200)
        assertEquals(5, flags)
    }

    @Test
    fun lowCoverageGives0or1Flags() {
        // 5/100 покрытия = 0.05, нет ревью → score = 0.03 → 0 флагов
        assertEquals(0, MasteryRating.flags(total = 100, learned = 5, totalReviews = 0, correctReviews = 0))
        // 20/100 покрытия = 0.2 → score = 0.12 → 1 флаг (порог 0.10)
        assertEquals(1, MasteryRating.flags(total = 100, learned = 20, totalReviews = 0, correctReviews = 0))
    }

    @Test
    fun thresholdsAreOrdered() {
        // Постепенно растущий learned должен давать всё больше флагов (или столько же).
        var prev = -1
        for (learned in 0..100 step 5) {
            val f = MasteryRating.flags(total = 100, learned = learned, totalReviews = 50, correctReviews = 45)
            assertTrue("flags should be non-decreasing: $f vs $prev (learned=$learned)", f >= prev)
            prev = f
        }
    }
}
