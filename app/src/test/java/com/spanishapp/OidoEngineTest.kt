package com.spanishapp

import com.spanishapp.ui.games.OidoEngine
import com.spanishapp.ui.games.OidoMatch
import com.spanishapp.ui.games.OidoMode
import com.spanishapp.ui.games.OidoPairsBank
import com.spanishapp.ui.games.TimeToSpanish
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * El Oído: движок, банк минимальных пар, время по-испански, диктант.
 */
class OidoEngineTest {

    // ── Темп и план ──────────────────────────────────────────

    @Test
    fun `rate grows by tier`() {
        assertEquals(0.75f, OidoEngine.rateForLevel(1))
        assertEquals(0.75f, OidoEngine.rateForLevel(25))
        assertEquals(0.9f, OidoEngine.rateForLevel(26))
        assertEquals(1.0f, OidoEngine.rateForLevel(51))
        assertEquals(1.15f, OidoEngine.rateForLevel(76))
        assertEquals(1.15f, OidoEngine.rateForLevel(100))
    }

    @Test
    fun `plan always has 10 tasks and is deterministic`() {
        for (level in 1..100) {
            val plan1 = OidoEngine.planForLevel(level)
            val plan2 = OidoEngine.planForLevel(level)
            assertEquals(OidoEngine.TASKS_PER_LEVEL, plan1.size)
            assertEquals("plan not deterministic at $level", plan1, plan2)
        }
    }

    @Test
    fun `tier1 plan is only choice and dictation`() {
        for (level in listOf(1, 10, 25)) {
            OidoEngine.planForLevel(level).forEach { mode ->
                assertTrue(mode == OidoMode.CHOICE || mode == OidoMode.DICTATION)
            }
        }
    }

    @Test
    fun `tier3plus plan includes time tasks`() {
        for (level in listOf(51, 76, 100)) {
            assertTrue(OidoEngine.planForLevel(level).contains(OidoMode.TIME))
        }
    }

    @Test
    fun `number range respects tier`() {
        val rng = Random(1)
        repeat(200) {
            assertTrue(OidoEngine.numberForLevel(30, rng) in 0..100)
            assertTrue(OidoEngine.numberForLevel(60, rng) in 0..499)
            assertTrue(OidoEngine.numberForLevel(90, rng) in 0..999)
        }
    }

    // ── Банк пар ─────────────────────────────────────────────

    @Test
    fun `pairs bank is well-formed`() {
        assertTrue("bank too small: ${OidoPairsBank.pairs.size}", OidoPairsBank.pairs.size >= 50)
        OidoPairsBank.pairs.forEach { p ->
            assertTrue("blank a", p.a.isNotBlank())
            assertTrue("blank b in ${p.a}", p.b.isNotBlank())
            assertTrue("a==b: ${p.a}", p.a != p.b)
            assertTrue("blank ruA for ${p.a}", p.ruA.isNotBlank())
            assertTrue("blank ruB for ${p.b}", p.ruB.isNotBlank())
            assertTrue("blank note for ${p.a}/${p.b}", p.note.isNotBlank())
        }
        val combos = OidoPairsBank.pairs.map { it.a to it.b }
        assertEquals("duplicate pairs", combos.size, combos.distinct().size)
    }

    @Test
    fun `early levels use only contrast categories`() {
        val pairs = OidoEngine.pairsForLevel(30, 10)
        assertTrue(pairs.isNotEmpty())
        pairs.forEach { p ->
            assertTrue(
                "hard category ${p.category} at level 30",
                p.category in OidoPairsBank.easyCategories
            )
        }
    }

    @Test
    fun `pair selection is deterministic and distinct`() {
        val a = OidoEngine.pairsForLevel(60, 5)
        val b = OidoEngine.pairsForLevel(60, 5)
        assertEquals(a, b)
        assertEquals(a.size, a.distinct().size)
    }

    // ── Диктант ──────────────────────────────────────────────

    @Test
    fun `dictation matching handles exact case and accents`() {
        assertEquals(OidoMatch.EXACT, OidoEngine.matchDictation("café", "café"))
        assertEquals(OidoMatch.EXACT, OidoEngine.matchDictation("Café", " CAFÉ "))
        assertEquals(OidoMatch.ACCENT_LOOSE, OidoEngine.matchDictation("café", "cafe"))
        assertEquals(OidoMatch.ACCENT_LOOSE, OidoEngine.matchDictation("mañana", "manana"))
        assertEquals(OidoMatch.NONE, OidoEngine.matchDictation("perro", "pero"))
        assertEquals(OidoMatch.NONE, OidoEngine.matchDictation("casa", ""))
    }

    @Test
    fun `dictation friendliness filter`() {
        assertTrue(OidoEngine.isDictationFriendly("ventana"))
        assertTrue(OidoEngine.isDictationFriendly("añadir"))
        assertTrue(!OidoEngine.isDictationFriendly("el perro"))   // два слова
        assertTrue(!OidoEngine.isDictationFriendly("no"))          // короткое
        assertTrue(!OidoEngine.isDictationFriendly("¿qué?"))       // пунктуация
        assertTrue(!OidoEngine.isDictationFriendly("extraordinario")) // длинное
    }

    // ── Время ────────────────────────────────────────────────

    @Test
    fun `time phrases follow castilian pattern`() {
        assertEquals("Es la una en punto", TimeToSpanish.convert(1, 0))
        assertEquals("Son las tres y cuarto", TimeToSpanish.convert(3, 15))
        assertEquals("Son las ocho y media", TimeToSpanish.convert(8, 30))
        assertEquals("Son las diez menos cuarto", TimeToSpanish.convert(9, 45))
        assertEquals("Es la una menos cuarto", TimeToSpanish.convert(12, 45))
        assertEquals("Son las doce en punto", TimeToSpanish.convert(12, 0))
        assertEquals("Es la una y media", TimeToSpanish.convert(1, 30))
    }

    @Test
    fun `expected digits are the real clock time not the spoken hour`() {
        assertEquals(830, TimeToSpanish.expectedDigits(8, 30))
        assertEquals(945, TimeToSpanish.expectedDigits(9, 45))   // «diez menos cuarto»
        assertEquals(1245, TimeToSpanish.expectedDigits(12, 45))
        assertEquals(100, TimeToSpanish.expectedDigits(1, 0))
    }

    @Test
    fun `time display is human readable`() {
        assertEquals("8:30", TimeToSpanish.display(8, 30))
        assertEquals("12:15", TimeToSpanish.display(12, 15))
        assertEquals("1:00", TimeToSpanish.display(1, 0))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `time rejects non-quarter minutes`() {
        TimeToSpanish.convert(5, 20)
    }
}
