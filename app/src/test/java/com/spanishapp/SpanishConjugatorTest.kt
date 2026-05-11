package com.spanishapp

import com.spanishapp.domain.algorithm.SpanishConjugator
import org.junit.Assert.*
import org.junit.Test

/** Spot-checks that the conjugator produces canonical Spanish forms.
 *  If any of these fail, we have a real correctness bug. */
class SpanishConjugatorTest {

    private fun forms(verb: String, tense: String): List<String> {
        val c = SpanishConjugator.conjugate(verb, tense)
            ?: error("Verb $verb / $tense not in known list")
        return listOf(c.yo, c.tu, c.el, c.nosotros, c.vosotros, c.ellos)
    }

    @Test fun `hablar presente`() {
        assertEquals(
            listOf("hablo","hablas","habla","hablamos","habláis","hablan"),
            forms("hablar", "presente")
        )
    }

    @Test fun `comer presente`() {
        assertEquals(
            listOf("como","comes","come","comemos","coméis","comen"),
            forms("comer", "presente")
        )
    }

    @Test fun `vivir presente`() {
        assertEquals(
            listOf("vivo","vives","vive","vivimos","vivís","viven"),
            forms("vivir", "presente")
        )
    }

    @Test fun `pensar presente — e→ie boot`() {
        assertEquals(
            listOf("pienso","piensas","piensa","pensamos","pensáis","piensan"),
            forms("pensar", "presente")
        )
    }

    @Test fun `contar presente — o→ue boot`() {
        assertEquals(
            listOf("cuento","cuentas","cuenta","contamos","contáis","cuentan"),
            forms("contar", "presente")
        )
    }

    @Test fun `pedir presente — e→i`() {
        assertEquals(
            listOf("pido","pides","pide","pedimos","pedís","piden"),
            forms("pedir", "presente")
        )
    }

    @Test fun `dormir preterito — o→u in 3rd person`() {
        assertEquals(
            listOf("dormí","dormiste","durmió","dormimos","dormisteis","durmieron"),
            forms("dormir", "preterito")
        )
    }

    @Test fun `pagar preterito — gar→gué in yo`() {
        assertEquals(
            listOf("pagué","pagaste","pagó","pagamos","pagasteis","pagaron"),
            forms("pagar", "preterito")
        )
    }

    @Test fun `buscar preterito — car→qué in yo`() {
        assertEquals(
            listOf("busqué","buscaste","buscó","buscamos","buscasteis","buscaron"),
            forms("buscar", "preterito")
        )
    }

    @Test fun `empezar preterito — zar→cé in yo`() {
        assertEquals(
            listOf("empecé","empezaste","empezó","empezamos","empezasteis","empezaron"),
            forms("empezar", "preterito")
        )
    }

    @Test fun `hablar imperfecto`() {
        assertEquals(
            listOf("hablaba","hablabas","hablaba","hablábamos","hablabais","hablaban"),
            forms("hablar", "imperfecto")
        )
    }

    @Test fun `vivir futuro — add to infinitive`() {
        assertEquals(
            listOf("viviré","vivirás","vivirá","viviremos","viviréis","vivirán"),
            forms("vivir", "futuro")
        )
    }

    @Test fun `hablar condicional`() {
        assertEquals(
            listOf("hablaría","hablarías","hablaría","hablaríamos","hablaríais","hablarían"),
            forms("hablar", "condicional")
        )
    }

    @Test fun `parecer presente — c→zc in yo`() {
        assertEquals(
            listOf("parezco","pareces","parece","parecemos","parecéis","parecen"),
            forms("parecer", "presente")
        )
    }

    @Test fun `conducir presente — c→zc in yo`() {
        assertEquals(
            listOf("conduzco","conduces","conduce","conducimos","conducís","conducen"),
            forms("conducir", "presente")
        )
    }

    @Test fun `conducir preterito — full -j- stem`() {
        assertEquals(
            listOf("conduje","condujiste","condujo","condujimos","condujisteis","condujeron"),
            forms("conducir", "preterito")
        )
    }

    @Test fun `parecer subjuntivo — zc throughout`() {
        assertEquals(
            listOf("parezca","parezcas","parezca","parezcamos","parezcáis","parezcan"),
            forms("parecer", "subjuntivo")
        )
    }

    @Test fun `huir presente — y-insert`() {
        assertEquals(
            listOf("huyo","huyes","huye","huimos","huís","huyen"),
            forms("huir", "presente")
        )
    }

    @Test fun `huir preterito — y in 3rd person`() {
        assertEquals(
            listOf("huí","huiste","huyó","huimos","huisteis","huyeron"),
            forms("huir", "preterito")
        )
    }

    @Test fun `construir subjuntivo — y throughout`() {
        assertEquals(
            listOf("construya","construyas","construya",
                   "construyamos","construyáis","construyan"),
            forms("construir", "subjuntivo")
        )
    }

    @Test fun `authored verbs return null (caller uses DB)`() {
        // ser, tener, etc. live in ConjugationData — engine should defer.
        assertNull(SpanishConjugator.conjugate("ser", "presente"))
        assertNull(SpanishConjugator.conjugate("tener", "presente"))
    }

    @Test fun `unknown verb returns null`() {
        // "raporrear" — made-up, must NOT be silently conjugated
        assertNull(SpanishConjugator.conjugate("raporrear", "presente"))
    }

    @Test fun `known verb count`() {
        val all = SpanishConjugator.knownVerbs()
        println("Conjugator covers ${all.size} verbs")
        assertTrue("at least 100 known verbs", all.size >= 100)
    }
}
