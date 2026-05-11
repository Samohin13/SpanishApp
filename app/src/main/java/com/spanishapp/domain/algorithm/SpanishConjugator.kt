package com.spanishapp.domain.algorithm

import com.spanishapp.data.db.entity.ConjugationEntity

/**
 * Rules-based Spanish conjugator for REGULAR -ar/-er/-ir verbs.
 *
 * Covers the six tenses also stored in the ConjugationData table:
 *   presente, preterito (indefinido), imperfecto, futuro,
 *   condicional, subjuntivo presente
 *
 * Irregular verbs (ser, estar, tener, ir, hacer, decir, ...) are still
 * served from ConjugationData where authored — this engine is a fallback
 * for every dictionary verb that doesn't have a hand-curated entry.
 *
 * Limitations (intentional, for Phase 1):
 *   • No stem changes (querer → quiero etc.) — those need explicit data
 *   • No spelling shifts (sacar → saqué, pagar → pagué) — minor regulars
 *   • Reflexive -se is stripped before conjugating; pronouns are NOT added
 *     (the VerbTraining UI shows reflexive pronouns separately)
 *
 * Output shape mirrors ConjugationEntity so callers can mix-and-match it
 * with rows from the DB in one collection.
 */
object SpanishConjugator {

    /** Tenses the engine knows how to produce. */
    val supportedTenses = listOf(
        "presente", "preterito", "imperfecto", "futuro",
        "condicional", "subjuntivo"
    )

    /** Returns a ConjugationEntity-shaped result, or null if the verb's
     *  infinitive isn't recognisable as a regular -ar/-er/-ir form. */
    fun conjugate(verb: String, tense: String): ConjugationEntity? {
        val raw = verb.trim().lowercase()
        // Strip reflexive -se / -arse / -erse / -irse — conjugate the bare form.
        val infinitive = if (raw.endsWith("se") && raw.length > 4) raw.dropLast(2) else raw

        val (stem, ending) = when {
            infinitive.endsWith("ar") -> infinitive.dropLast(2) to "ar"
            infinitive.endsWith("er") -> infinitive.dropLast(2) to "er"
            infinitive.endsWith("ir") -> infinitive.dropLast(2) to "ir"
            else -> return null
        }
        if (stem.isEmpty()) return null

        val forms: List<String> = when (tense) {
            "presente" -> when (ending) {
                "ar" -> listOf("o", "as", "a", "amos", "áis", "an")
                "er" -> listOf("o", "es", "e", "emos", "éis", "en")
                "ir" -> listOf("o", "es", "e", "imos", "ís", "en")
                else -> return null
            }.map { stem + it }

            "preterito" -> when (ending) {
                "ar" -> listOf("é", "aste", "ó", "amos", "asteis", "aron")
                "er", "ir" -> listOf("í", "iste", "ió", "imos", "isteis", "ieron")
                else -> return null
            }.map { stem + it }

            "imperfecto" -> when (ending) {
                "ar" -> listOf("aba", "abas", "aba", "ábamos", "abais", "aban")
                "er", "ir" -> listOf("ía", "ías", "ía", "íamos", "íais", "ían")
                else -> return null
            }.map { stem + it }

            "futuro" ->
                // Future tense: add to full infinitive, not stem
                listOf("é", "ás", "á", "emos", "éis", "án").map { infinitive + it }

            "condicional" ->
                // Conditional: add to full infinitive
                listOf("ía", "ías", "ía", "íamos", "íais", "ían").map { infinitive + it }

            "subjuntivo" -> when (ending) {
                "ar" -> listOf("e", "es", "e", "emos", "éis", "en")
                "er", "ir" -> listOf("a", "as", "a", "amos", "áis", "an")
                else -> return null
            }.map { stem + it }

            else -> return null
        }

        return ConjugationEntity(
            id = 0,
            verb = verb,
            tense = tense,
            yo = forms[0],
            tu = forms[1],
            el = forms[2],
            nosotros = forms[3],
            vosotros = forms[4],
            ellos = forms[5],
            isIrregular = false,
            note = "auto-generated",
        )
    }

    /** Convenience: full set of 6-tense conjugations. */
    fun conjugateAll(verb: String): List<ConjugationEntity> =
        supportedTenses.mapNotNull { conjugate(verb, it) }
}
