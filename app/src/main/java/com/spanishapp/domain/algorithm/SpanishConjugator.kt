package com.spanishapp.domain.algorithm

import com.spanishapp.data.db.entity.ConjugationEntity

/**
 * Spanish conjugator driven by [SpanishVerbBank]. For every verb in the
 * bank the engine knows the conjugation pattern (VerbKind) and applies
 * exact rules. Verbs absent from the bank → returns null and the caller
 * skips them.
 *
 * "AUTHORED" kind = signal that the verb is fully irregular and must be
 * served from the conjugations DB table; the engine returns null in that
 * case so caller falls through to authored data.
 *
 * Covers 6 tenses: presente, preterito, imperfecto, futuro, condicional,
 * subjuntivo (presente).
 */
object SpanishConjugator {

    val supportedTenses = listOf(
        "presente", "preterito", "imperfecto", "futuro",
        "condicional", "subjuntivo",
    )

    /**
     * Compound-verb parents: prefix → (parent infinitive, parent's kind).
     * Used so that compound forms (mantener, componer, prever, etc.)
     * are conjugated by stripping the prefix, conjugating the parent,
     * then re-attaching the prefix to each form.
     *
     * The parent verb itself MUST be authored in ConjugationData so the
     * caller has correct base forms — these mappings only kick in when
     * the conjugator is asked for the compound directly (rules engine).
     */
    private val COMPOUND_PARENTS: Map<String, String> = mapOf(
        "tener"  to "tener",
        "poner"  to "poner",
        "venir"  to "venir",
        "decir"  to "decir",
        "hacer"  to "hacer",
        "traer"  to "traer",
        "ver"    to "ver",
        "caer"   to "caer",
        "salir"  to "salir",
    )

    /**
     * Detects if [verb] is a known compound of an authored irregular.
     * Returns the prefix (e.g. "man" for "mantener") and the parent name,
     * or null if [verb] isn't a recognised compound.
     */
    fun detectCompound(verb: String): Pair<String, String>? {
        val raw = verb.trim().lowercase()
        for ((suffix, parent) in COMPOUND_PARENTS) {
            if (raw.length > suffix.length && raw.endsWith(suffix)) {
                val prefix = raw.dropLast(suffix.length)
                if (prefix.length >= 2) return prefix to parent
            }
        }
        return null
    }

    /** Returns ConjugationEntity for [verb] in [tense], or null if the
     *  verb isn't in our bank OR is marked AUTHORED (use DB instead). */
    fun conjugate(verb: String, tense: String): ConjugationEntity? {
        val raw = verb.trim().lowercase()
        val info = SpanishVerbBank.byInfinitive[raw] ?: return null
        if (info.kind == VerbKind.AUTHORED) return null  // fall through to DB

        return conjugateByKind(verb, info.kind, tense)
    }

    /** Returns the set of all verbs the engine can safely produce (i.e.
     *  not AUTHORED). UI uses this to know which dictionary verbs to
     *  expose in tier selectors. */
    fun knownVerbs(): Set<String> =
        SpanishVerbBank.all
            .filter { it.kind != VerbKind.AUTHORED }
            .map { it.infinitive.lowercase() }
            .toSet()

    // ── Core engine ────────────────────────────────────────────────

    private fun conjugateByKind(
        verb: String, kind: VerbKind, tense: String,
    ): ConjugationEntity? {
        val raw = verb.trim().lowercase()
        // Strip reflexive -se / -arse / -erse / -irse.
        val inf = if (raw.endsWith("se") && raw.length > 4) raw.dropLast(2) else raw

        val (stem, ending) = when {
            inf.endsWith("ar") -> inf.dropLast(2) to "ar"
            inf.endsWith("er") -> inf.dropLast(2) to "er"
            inf.endsWith("ir") -> inf.dropLast(2) to "ir"
            else -> return null
        }
        if (stem.isEmpty()) return null

        val isE_IE   = kind == VerbKind.STEM_E_IE
        val isO_UE   = kind == VerbKind.STEM_O_UE
        val isE_I    = kind == VerbKind.STEM_E_I
        val isCar    = kind == VerbKind.SPELL_CAR
        val isGar    = kind == VerbKind.SPELL_GAR
        val isZar    = kind == VerbKind.SPELL_ZAR
        val isZc     = kind == VerbKind.ZC
        val isDucir  = kind == VerbKind.DUCIR
        val isUir    = kind == VerbKind.UIR

        fun shifted(): String = when {
            isE_IE -> changeLastVowel(stem, 'e', "ie")
            isO_UE -> changeLastVowel(stem, 'o', "ue")
            isE_I  -> changeLastVowel(stem, 'e', "i")
            else   -> stem
        }

        val forms: List<String> = when (tense) {
            // ── PRESENTE ──
            "presente" -> when {
                isZc || isDucir -> {
                    val zcStem = stem.dropLast(1) + "zc"   // "parec" → "parezc"
                    when (ending) {
                        "er" -> listOf(zcStem + "o", stem + "es", stem + "e",
                                       stem + "emos", stem + "éis", stem + "en")
                        "ir" -> listOf(zcStem + "o", stem + "es", stem + "e",
                                       stem + "imos", stem + "ís", stem + "en")
                        else -> return null
                    }
                }
                isUir -> {
                    // huir → huyo, huyes, huye, huimos, huís, huyen
                    val yStem = stem + "y"
                    listOf(yStem + "o", yStem + "es", yStem + "e",
                           stem + "imos", stem + "ís", yStem + "en")
                }
                else -> {
                    val s1 = shifted()
                    when (ending) {
                        "ar" -> listOf(s1 + "o", s1 + "as", s1 + "a",
                                       stem + "amos", stem + "áis", s1 + "an")
                        "er" -> listOf(s1 + "o", s1 + "es", s1 + "e",
                                       stem + "emos", stem + "éis", s1 + "en")
                        "ir" -> listOf(s1 + "o", s1 + "es", s1 + "e",
                                       stem + "imos", stem + "ís", s1 + "en")
                        else -> return null
                    }
                }
            }

            // ── PRETERITO ──
            "preterito" -> when {
                isDucir -> {
                    // conducir → conduje, condujiste, condujo, condujimos, condujisteis, condujeron
                    val j = stem.dropLast(1) + "j"
                    listOf(j + "e", j + "iste", j + "o", j + "imos", j + "isteis", j + "eron")
                }
                isUir -> {
                    // huir → huí, huiste, huyó, huimos, huisteis, huyeron
                    listOf(stem + "í", stem + "iste", stem + "yó",
                           stem + "imos", stem + "isteis", stem + "yeron")
                }
                ending == "ar" -> {
                    val yo = when {
                        isCar -> stem.dropLast(1) + "qué"
                        isGar -> stem + "ué"
                        isZar -> stem.dropLast(1) + "cé"
                        else  -> stem + "é"
                    }
                    listOf(yo, stem + "aste", stem + "ó",
                           stem + "amos", stem + "asteis", stem + "aron")
                }
                ending == "er" || ending == "ir" -> {
                    // -ir stem-changers shift 3rd-person stem
                    val s3 = when {
                        ending == "ir" && (isE_I || isE_IE) -> changeLastVowel(stem, 'e', "i")
                        ending == "ir" && isO_UE             -> changeLastVowel(stem, 'o', "u")
                        else -> stem
                    }
                    listOf(stem + "í", stem + "iste", s3 + "ió",
                           stem + "imos", stem + "isteis", s3 + "ieron")
                }
                else -> return null
            }

            // ── IMPERFECTO ──
            "imperfecto" -> when (ending) {
                "ar"       -> listOf("aba","abas","aba","ábamos","abais","aban").map { stem + it }
                "er", "ir" -> listOf("ía","ías","ía","íamos","íais","ían").map { stem + it }
                else       -> return null
            }

            // ── FUTURO ──
            "futuro" -> listOf("é","ás","á","emos","éis","án").map { inf + it }

            // ── CONDICIONAL ──
            "condicional" -> listOf("ía","ías","ía","íamos","íais","ían").map { inf + it }

            // ── SUBJUNTIVO PRESENTE ──
            "subjuntivo" -> when {
                isZc || isDucir -> {
                    val zc = stem.dropLast(1) + "zc"
                    listOf(zc + "a", zc + "as", zc + "a",
                           zc + "amos", zc + "áis", zc + "an")
                }
                isUir -> {
                    val y = stem + "y"
                    listOf(y + "a", y + "as", y + "a",
                           y + "amos", y + "áis", y + "an")
                }
                ending == "ar" -> {
                    val s1 = shifted()
                    val (sb, sn) = when {
                        isCar -> (stem.dropLast(1) + "qu") to (stem.dropLast(1) + "qu")
                        isGar -> (stem + "u") to (stem + "u")
                        isZar -> (stem.dropLast(1) + "c") to (stem.dropLast(1) + "c")
                        else  -> s1 to stem
                    }
                    listOf(sb + "e", sb + "es", sb + "e",
                           sn + "emos", sn + "éis", sb + "en")
                }
                ending == "er" -> {
                    val s1 = shifted()
                    listOf(s1 + "a", s1 + "as", s1 + "a",
                           stem + "amos", stem + "áis", s1 + "an")
                }
                ending == "ir" -> {
                    val s1 = shifted()
                    val nosStem = when {
                        isE_IE -> changeLastVowel(stem, 'e', "i")
                        isO_UE -> changeLastVowel(stem, 'o', "u")
                        isE_I  -> changeLastVowel(stem, 'e', "i")
                        else   -> stem
                    }
                    listOf(s1 + "a", s1 + "as", s1 + "a",
                           nosStem + "amos", nosStem + "áis", s1 + "an")
                }
                else -> return null
            }

            else -> return null
        }

        return ConjugationEntity(
            id = 0, verb = verb, tense = tense,
            yo = forms[0], tu = forms[1], el = forms[2],
            nosotros = forms[3], vosotros = forms[4], ellos = forms[5],
            isIrregular = kind != VerbKind.REGULAR_AR &&
                          kind != VerbKind.REGULAR_ER &&
                          kind != VerbKind.REGULAR_IR,
            note = "auto",
        )
    }

    /** Replace the LAST occurrence of [from] in [stem] with [replacement]. */
    private fun changeLastVowel(stem: String, from: Char, replacement: String): String {
        val idx = stem.lastIndexOf(from)
        if (idx < 0) return stem
        return stem.substring(0, idx) + replacement + stem.substring(idx + 1)
    }
}
