package com.spanishapp

import com.spanishapp.ui.pronunciation.normalizeForScore
import com.spanishapp.ui.pronunciation.phoneticFoldEs
import com.spanishapp.ui.pronunciation.pronunciationScore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v1.26.1: тесты фонетической оценки произношения. Оценка сравнивает ЗВУКИ,
 * а не буквы: STT может вернуть другую орфографию того же звучания — юзер
 * не должен терять проценты за b/v, немую h, ll/y, сесео, акценты.
 */
class PronunciationScoreTest {

    // ── Орфографические варианты одного звучания → 100 ──────────

    @Test fun `b equals v`() =
        assertEquals(100, pronunciationScore("baca", "vaca"))

    @Test fun `silent h ignored`() =
        assertEquals(100, pronunciationScore("asta", "hasta"))

    @Test fun `ll equals y`() =
        assertEquals(100, pronunciationScore("yamar", "llamar"))

    @Test fun `seseo - z equals s`() =
        assertEquals(100, pronunciationScore("sapato", "zapato"))

    @Test fun `seseo - ce equals se`() =
        assertEquals(100, pronunciationScore("sena", "cena"))

    @Test fun `accents from STT do not penalize`() =
        assertEquals(100, pronunciationScore("esta", "está"))

    @Test fun `leading article optional`() =
        assertEquals(100, pronunciationScore("gato", "el gato"))

    @Test fun `punctuation in target ignored`() =
        assertEquals(100, pronunciationScore("como estas", "¿Cómo estás?"))

    @Test fun `exact multiword match`() =
        assertEquals(100, pronunciationScore("el efecto especial", "el efecto especial"))

    // ── ch защищён, реальные различия НЕ прощаются ───────────────

    @Test fun `ch survives folding`() =
        assertEquals("mucho", phoneticFoldEs(normalizeForScore("mucho")))

    @Test fun `enye is NOT folded - ano vs anio differ`() {
        assertTrue(pronunciationScore("ano", "año") < 100)
    }

    @Test fun `rr is NOT folded - pero vs perro differ`() {
        assertTrue(pronunciationScore("pero", "perro") < 100)
    }

    @Test fun `wrong word scores low`() {
        assertTrue(pronunciationScore("biblioteca", "gato") < 50)
    }

    @Test fun `close but imperfect is between 50 and 99`() {
        val s = pronunciationScore("efekto espesal", "el efecto especial")
        assertTrue("score=$s", s in 50..99)
    }

    // ── qu/c/g правила ───────────────────────────────────────────

    @Test fun `qu equals k sound`() =
        assertEquals(100, pronunciationScore("kiero", "quiero"))

    @Test fun `ca equals ka sound`() =
        assertEquals(100, pronunciationScore("kasa", "casa"))

    @Test fun `ge folds to je`() =
        assertEquals(100, pronunciationScore("jente", "gente"))

    @Test fun `gui folds to gi - guitarra`() =
        assertEquals(100, pronunciationScore("gitarra", "guitarra"))
}
