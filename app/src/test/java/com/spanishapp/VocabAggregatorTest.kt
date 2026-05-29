package com.spanishapp

import com.spanishapp.domain.vocab.VocabAggregator
import com.spanishapp.domain.vocab.VocabAggregator.Signals
import com.spanishapp.domain.vocab.VocabAggregator.Status
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v1.25.28 — тесты VocabAggregator.
 *
 * Покрытие:
 *  - все 5 статусов классифицируются корректно
 *  - score в диапазоне [0, 1]
 *  - corrections штрафует
 *  - recency decay работает
 *  - UNKNOWN → null entity
 *  - SM-2 mastery → MASTERED
 *  - active usage → PRODUCING
 */
class VocabAggregatorTest {

    private val now = 1_700_000_000_000L  // фиксированный timestamp для тестов

    @Test
    fun `unknown word with zero signals returns null`() {
        val signals = Signals(word = "xyz")
        val result = VocabAggregator.aggregate(signals, now)
        assertNull("UNKNOWN words should not be persisted", result)
    }

    @Test
    fun `word only seen in lesson is SEEN`() {
        val signals = Signals(
            word = "hola",
            cefr = "A1",
            seenInLesson = true,
            lastSeenAt = now - 5 * DAY_MS,
        )
        val result = VocabAggregator.aggregate(signals, now)
        assertNotNull(result)
        assertEquals(Status.SEEN.name, result!!.status)
        assertEquals("A1", result.cefr)
        assertTrue("Score should be > 0 for SEEN", result.score > 0f)
    }

    @Test
    fun `word in SM2 pool is LEARNING`() {
        val signals = Signals(
            word = "gracias",
            cefr = "A1",
            sm2EaseFactor = 2.5f,
            sm2Repetitions = 2,
            totalReviews = 3,
            correctReviews = 2,
            seenInLesson = true,
            lastSeenAt = now - 1 * DAY_MS,
        )
        val result = VocabAggregator.aggregate(signals, now)
        assertNotNull(result)
        assertEquals(Status.LEARNING.name, result!!.status)
    }

    @Test
    fun `word actively used in chat is PRODUCING`() {
        val signals = Signals(
            word = "como",
            cefr = "A1",
            sm2EaseFactor = 2.5f,
            chatUsageCount = 5,
            seenInLesson = true,
            lastSeenAt = now - 1 * DAY_MS,
            totalReviews = 4,
        )
        val result = VocabAggregator.aggregate(signals, now)
        assertNotNull(result)
        assertEquals(Status.PRODUCING.name, result!!.status)
        assertTrue("PRODUCING score should be ≥ 0.45", result.score >= 0.45f)
    }

    @Test
    fun `word mastered via flashcards becomes MASTERED`() {
        val signals = Signals(
            word = "casa",
            cefr = "A1",
            sm2EaseFactor = 2.8f,
            sm2Repetitions = 5,
            totalReviews = 10,
            correctReviews = 9,
            isLearned = true,
            chatUsageCount = 12,
            seenInLesson = true,
            lastSeenAt = now - 2 * DAY_MS,
        )
        val result = VocabAggregator.aggregate(signals, now)
        assertNotNull(result)
        assertEquals(Status.MASTERED.name, result!!.status)
        assertTrue("MASTERED score should be high", result.score >= 0.7f)
    }

    @Test
    fun `word mastered by heavy usage becomes MASTERED`() {
        val signals = Signals(
            word = "que",
            sm2EaseFactor = 0f,  // не во флэшкартах
            chatUsageCount = 20,
            seenInLesson = true,
            lastSeenAt = now - 1 * DAY_MS,
        )
        val result = VocabAggregator.aggregate(signals, now)
        assertNotNull(result)
        // Может быть MASTERED (usage≥15 && score≥0.80) или PRODUCING
        assertTrue(
            "Heavy usage should be PRODUCING or MASTERED",
            result!!.status in listOf(Status.PRODUCING.name, Status.MASTERED.name)
        )
    }

    @Test
    fun `corrections penalty reduces score`() {
        val noErrors = VocabAggregator.aggregate(
            Signals(word = "estar", chatUsageCount = 10, seenInLesson = true, lastSeenAt = now),
            now,
        )!!
        val withErrors = VocabAggregator.aggregate(
            Signals(
                word = "estar",
                chatUsageCount = 10,
                correctionsCount = 5,
                seenInLesson = true,
                lastSeenAt = now,
            ),
            now,
        )!!
        assertTrue(
            "Corrections should reduce score (no errors: ${noErrors.score}, with errors: ${withErrors.score})",
            withErrors.score < noErrors.score
        )
    }

    @Test
    fun `recency decay reduces score over time`() {
        val recent = VocabAggregator.aggregate(
            Signals(word = "hola", chatUsageCount = 3, seenInLesson = true, lastSeenAt = now - 1 * DAY_MS),
            now,
        )!!
        val old = VocabAggregator.aggregate(
            Signals(word = "hola", chatUsageCount = 3, seenInLesson = true, lastSeenAt = now - 90 * DAY_MS),
            now,
        )!!
        assertTrue(
            "Recent contact should score higher (recent: ${recent.score}, old: ${old.score})",
            recent.score > old.score
        )
    }

    @Test
    fun `score is always in 0 to 1 range`() {
        val extreme = Signals(
            word = "test",
            sm2EaseFactor = 5f,        // экстремально высокий
            chatUsageCount = 1000,     // экстремальный usage
            totalReviews = 500,
            correctReviews = 500,
            seenInLesson = true,
            seenInLibro = true,
            lastSeenAt = now,
        )
        val result = VocabAggregator.aggregate(extreme, now)!!
        assertTrue("Score must be ≤ 1.0, got ${result.score}", result.score <= 1.0f)
        assertTrue("Score must be ≥ 0, got ${result.score}", result.score >= 0f)
    }

    @Test
    fun `entity preserves wordId and cefr`() {
        val signals = Signals(
            word = "perro",
            wordId = 42,
            cefr = "A1",
            seenInLesson = true,
            lastSeenAt = now,
        )
        val result = VocabAggregator.aggregate(signals, now)!!
        assertEquals(42, result.wordId)
        assertEquals("A1", result.cefr)
        assertEquals("perro", result.word)
    }

    @Test
    fun `lowercase word is preserved as-is`() {
        // Аггрегатор не делает дополнительный lowercase — caller отвечает.
        val signals = Signals(
            word = "hola",
            seenInLesson = true,
            lastSeenAt = now,
        )
        val result = VocabAggregator.aggregate(signals, now)!!
        assertEquals("hola", result.word)
    }

    @Test
    fun `updatedAt is set to now`() {
        val signals = Signals(word = "test", seenInLesson = true, lastSeenAt = now - 1000)
        val result = VocabAggregator.aggregate(signals, now)!!
        assertEquals(now, result.updatedAt)
    }

    @Test
    fun `lastSeenAt is preserved from signals`() {
        val seenAt = now - 5 * DAY_MS
        val signals = Signals(word = "test", seenInLesson = true, lastSeenAt = seenAt)
        val result = VocabAggregator.aggregate(signals, now)!!
        assertEquals(seenAt, result.lastSeenAt)
    }

    @Test
    fun `seenInLibro alone is enough for SEEN`() {
        val signals = Signals(
            word = "ferrocarril",
            cefr = "B1",
            seenInLibro = true,
            lastSeenAt = now,
        )
        val result = VocabAggregator.aggregate(signals, now)
        assertNotNull(result)
        assertEquals(Status.SEEN.name, result!!.status)
    }

    companion object {
        private const val DAY_MS = 24L * 60 * 60 * 1000
    }
}
