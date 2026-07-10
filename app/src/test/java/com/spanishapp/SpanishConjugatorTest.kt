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

    @Test fun `detectCompound finds tener compounds`() {
        assertEquals("man" to "tener", SpanishConjugator.detectCompound("mantener"))
        assertEquals("ob" to "tener", SpanishConjugator.detectCompound("obtener"))
        assertEquals("sos" to "tener", SpanishConjugator.detectCompound("sostener"))
        assertEquals("de" to "tener", SpanishConjugator.detectCompound("detener"))
    }

    @Test fun `detectCompound finds poner compounds`() {
        assertEquals("com" to "poner", SpanishConjugator.detectCompound("componer"))
        assertEquals("su" to "poner", SpanishConjugator.detectCompound("suponer"))
        assertEquals("pro" to "poner", SpanishConjugator.detectCompound("proponer"))
    }

    @Test fun `detectCompound ignores non-compounds`() {
        assertNull(SpanishConjugator.detectCompound("hablar"))
        assertNull(SpanishConjugator.detectCompound("comer"))
        assertNull(SpanishConjugator.detectCompound("ver"))   // ver itself, prefix too short
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

    // ═══════════════════════════════════════════════════════════════
    // v1.25.98 (audit games-C1): орфография g→j / gu→g / c→z перед a/o
    // + комбинации «стем-сдвиг + spelling». До фикса генератор учил
    // НЕПРАВИЛЬНЫМ формам: «dirigo», «cogo», «venco», «empece», «almorzo».
    // Каждая форма проверена по RAE.
    // ═══════════════════════════════════════════════════════════════

    @Test fun `dirigir presente — g→j in yo only`() {
        assertEquals(
            listOf("dirijo","diriges","dirige","dirigimos","dirigís","dirigen"),
            forms("dirigir", "presente")
        )
    }

    @Test fun `dirigir subjuntivo — j throughout`() {
        assertEquals(
            listOf("dirija","dirijas","dirija","dirijamos","dirijáis","dirijan"),
            forms("dirigir", "subjuntivo")
        )
    }

    @Test fun `coger escoger proteger exigir fingir — g→j in yo`() {
        assertEquals("cojo",    forms("coger", "presente")[0])
        assertEquals("coges",   forms("coger", "presente")[1])
        assertEquals("coja",    forms("coger", "subjuntivo")[0])
        assertEquals("escojo",  forms("escoger", "presente")[0])
        assertEquals("protejo", forms("proteger", "presente")[0])
        assertEquals("exijo",   forms("exigir", "presente")[0])
        assertEquals("finjo",   forms("fingir", "presente")[0])
    }

    @Test fun `corregir — stem e→i PLUS g→j`() {
        assertEquals(
            listOf("corrijo","corriges","corrige","corregimos","corregís","corrigen"),
            forms("corregir", "presente")
        )
        assertEquals(
            listOf("corrija","corrijas","corrija","corrijamos","corrijáis","corrijan"),
            forms("corregir", "subjuntivo")
        )
    }

    @Test fun `elegir presente — elijo`() {
        assertEquals("elijo", forms("elegir", "presente")[0])
    }

    @Test fun `distinguir — gu→g in yo and subjuntivo`() {
        assertEquals(
            listOf("distingo","distingues","distingue","distinguimos","distinguís","distinguen"),
            forms("distinguir", "presente")
        )
        assertEquals("distinga",    forms("distinguir", "subjuntivo")[0])
        assertEquals("distingamos", forms("distinguir", "subjuntivo")[3])
    }

    @Test fun `seguir — stem e→i PLUS gu→g`() {
        assertEquals(
            listOf("sigo","sigues","sigue","seguimos","seguís","siguen"),
            forms("seguir", "presente")
        )
        assertEquals(
            listOf("siga","sigas","siga","sigamos","sigáis","sigan"),
            forms("seguir", "subjuntivo")
        )
    }

    @Test fun `perseguir conseguir — persigo consigo`() {
        assertEquals("persigo", forms("perseguir", "presente")[0])
        assertEquals("consigo", forms("conseguir", "presente")[0])
    }

    @Test fun `vencer convencer — consonant+cer c→z`() {
        assertEquals(
            listOf("venzo","vences","vence","vencemos","vencéis","vencen"),
            forms("vencer", "presente")
        )
        assertEquals(
            listOf("venza","venzas","venza","venzamos","venzáis","venzan"),
            forms("vencer", "subjuntivo")
        )
        assertEquals("convenzo", forms("convencer", "presente")[0])
    }

    @Test fun `empezar presente — stem shift restored (was 'empezo')`() {
        assertEquals(
            listOf("empiezo","empiezas","empieza","empezamos","empezáis","empiezan"),
            forms("empezar", "presente")
        )
    }

    @Test fun `empezar subjuntivo — empiece + empecemos (shift AND z→c)`() {
        assertEquals(
            listOf("empiece","empieces","empiece","empecemos","empecéis","empiecen"),
            forms("empezar", "subjuntivo")
        )
    }

    @Test fun `almorzar — almuerzo almorcé almuerce`() {
        assertEquals("almuerzo",   forms("almorzar", "presente")[0])
        assertEquals("almorzamos", forms("almorzar", "presente")[3])
        assertEquals("almorcé",    forms("almorzar", "preterito")[0])
        assertEquals("almuerce",   forms("almorzar", "subjuntivo")[0])
        assertEquals("almorcemos", forms("almorzar", "subjuntivo")[3])
    }

    @Test fun `colgar rogar negar — stem shift plus g→gu`() {
        assertEquals("cuelgo",    forms("colgar", "presente")[0])
        assertEquals("colgué",    forms("colgar", "preterito")[0])
        assertEquals("cuelgue",   forms("colgar", "subjuntivo")[0])
        assertEquals("colguemos", forms("colgar", "subjuntivo")[3])
        assertEquals("ruego",     forms("rogar", "presente")[0])
        assertEquals("niego",     forms("negar", "presente")[0])
        assertEquals("niegue",    forms("negar", "subjuntivo")[0])
    }

    @Test fun `volcar fregar cegar comenzar — remaining stem+spell combos`() {
        assertEquals("vuelco",   forms("volcar", "presente")[0])
        assertEquals("volqué",   forms("volcar", "preterito")[0])
        assertEquals("vuelque",  forms("volcar", "subjuntivo")[0])
        assertEquals("friego",   forms("fregar", "presente")[0])
        assertEquals("ciego",    forms("cegar", "presente")[0])
        assertEquals("comienzo", forms("comenzar", "presente")[0])
        assertEquals("comience", forms("comenzar", "subjuntivo")[0])
    }

    @Test fun `pure spelling verbs unaffected — busque llegue realice`() {
        assertEquals("busque",  forms("buscar", "subjuntivo")[0])
        assertEquals("llegue",  forms("llegar", "subjuntivo")[0])
        assertEquals("realice", forms("realizar", "subjuntivo")[0])
    }

    @Test fun `imperativo — dirige dirija, empieza empiece, sigue siga`() {
        val dir = forms("dirigir", "imperativo")
        assertEquals("dirige",    dir[1])  // tú
        assertEquals("dirija",    dir[2])  // usted
        assertEquals("dirijamos", dir[3])  // nosotros
        assertEquals("dirigid",   dir[4])  // vosotros
        assertEquals("dirijan",   dir[5])  // ustedes
        val emp = forms("empezar", "imperativo")
        assertEquals("empieza",   emp[1])
        assertEquals("empiece",   emp[2])
        val seg = forms("seguir", "imperativo")
        assertEquals("sigue", seg[1])
        assertEquals("siga",  seg[2])
    }

    @Test fun `subjuntivo imperfecto — siguiera empezara`() {
        assertEquals("hablara",    forms("hablar", "subjuntivo_imperfecto")[0])
        assertEquals("habláramos", forms("hablar", "subjuntivo_imperfecto")[3])
        assertEquals("siguiera",   forms("seguir", "subjuntivo_imperfecto")[0])
        assertEquals("empezara",   forms("empezar", "subjuntivo_imperfecto")[0])
    }

    @Test fun `jugar avergonzar now AUTHORED — no wrong generated forms`() {
        // jugar: u→ue не выражается VerbKind — раньше генерил «juga».
        assertNull(SpanishConjugator.conjugate("jugar", "presente"))
        // avergonzar: o→üe с диерезисом — исключён из генератора.
        assertNull(SpanishConjugator.conjugate("avergonzar", "presente"))
    }
}
