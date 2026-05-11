package com.spanishapp.domain.algorithm

import com.spanishapp.data.db.entity.ConjugationEntity

/**
 * Rules-based Spanish conjugator — strictly conservative.
 *
 * Goal: never produce a WRONG form. We'd rather skip a verb entirely
 * than guess incorrectly.
 *
 * Strategy:
 *   1. Three explicit lookup tables: KNOWN_REGULAR, STEM_E_IE, STEM_O_UE,
 *      STEM_E_I, SPELL_CAR/GAR/ZAR.
 *   2. If a verb appears in one of them → conjugate via the matching ruleset.
 *   3. Otherwise → return null. Caller treats it as "no data" and skips.
 *
 * Authored irregulars (ser, estar, tener, ir, hacer, ...) live in
 * ConjugationData and take priority over this engine.
 */
object SpanishConjugator {

    val supportedTenses = listOf(
        "presente", "preterito", "imperfecto", "futuro",
        "condicional", "subjuntivo"
    )

    // ── Verbs that are textbook regular -ar/-er/-ir without stem changes
    //    or spelling shifts. Every form follows pure rules. ─────────────
    private val KNOWN_REGULAR = setOf(
        // -ar
        "hablar", "trabajar", "estudiar", "comprar", "tomar", "pasar",
        "llevar", "dejar", "esperar", "entrar", "preguntar", "ayudar",
        "viajar", "cantar", "bailar", "caminar", "escuchar", "mirar",
        "necesitar", "olvidar", "amar", "andar", "cocinar", "cuidar",
        "descansar", "desear", "enseñar", "explicar", "felicitar",
        "ganar", "gastar", "guardar", "lavar", "limpiar", "llamar",
        "llorar", "mejorar", "mirar", "nadar", "ocupar", "ordenar",
        "parar", "pasear", "preparar", "presentar", "quedar", "regalar",
        "regresar", "saludar", "terminar", "tirar", "tocar", "tratar",
        "usar", "visitar", "votar",
        // -er
        "aprender", "beber", "comprender", "correr", "creer", "deber",
        "leer", "meter", "prometer", "responder", "romper", "vender",
        "barrer", "comer", "coser", "esconder", "temer", "toser",
        // -ir
        "abrir", "asistir", "compartir", "decidir", "describir",
        "discutir", "escribir", "existir", "insistir", "ocurrir",
        "partir", "permitir", "prohibir", "recibir", "subir",
        "sufrir", "vivir",
    )

    // ── Stem-changing verbs (e → ie in stressed positions) ─────────────
    private val STEM_E_IE = setOf(
        "pensar", "cerrar", "comenzar", "empezar", "sentar", "despertar",
        "merendar", "negar", "nevar", "perder", "querer", "entender",
        "defender", "encender", "sentir", "preferir", "mentir", "herir",
        "advertir", "convertir", "divertir", "hervir", "sugerir",
    )

    // ── Stem-changing verbs (o → ue in stressed positions) ─────────────
    private val STEM_O_UE = setOf(
        "contar", "costar", "encontrar", "mostrar", "recordar",
        "soñar", "volar", "rogar", "almorzar",
        "mover", "morder", "doler", "llover", "envolver",
        "resolver", "volver", "devolver",
        "dormir", "morir",
    )

    // ── Stem-changing -ir verbs (e → i, only in -ir verbs) ─────────────
    private val STEM_E_I = setOf(
        "pedir", "servir", "repetir", "vestir", "seguir", "conseguir",
        "perseguir", "elegir", "corregir", "medir", "reír", "freír",
        "despedir", "gemir", "impedir", "render", "rendir",
    )

    // ── Spelling-shift verbs (preterito 1st person, subjuntivo all) ────
    private val SPELL_CAR = setOf("buscar", "sacar", "tocar", "explicar", "indicar", "comunicar", "atacar", "secar", "marcar", "practicar", "publicar")
    private val SPELL_GAR = setOf("pagar", "llegar", "jugar", "apagar", "entregar", "negar", "obligar", "rogar")
    private val SPELL_ZAR = setOf("almorzar", "comenzar", "empezar", "abrazar", "alcanzar", "cazar", "cruzar", "lanzar", "rezar", "tropezar")

    fun conjugate(verb: String, tense: String): ConjugationEntity? {
        val raw = verb.trim().lowercase()
        // Strip reflexive -se / -arse / -erse / -irse — conjugate the bare form.
        val inf = if (raw.endsWith("se") && raw.length > 4) raw.dropLast(2) else raw

        val (stem, ending) = when {
            inf.endsWith("ar") -> inf.dropLast(2) to "ar"
            inf.endsWith("er") -> inf.dropLast(2) to "er"
            inf.endsWith("ir") -> inf.dropLast(2) to "ir"
            else -> return null
        }
        if (stem.isEmpty()) return null

        val isKnownRegular  = inf in KNOWN_REGULAR
        val isE_IE          = inf in STEM_E_IE
        val isO_UE          = inf in STEM_O_UE
        val isE_I           = inf in STEM_E_I
        val isCar           = inf in SPELL_CAR
        val isGar           = inf in SPELL_GAR
        val isZar           = inf in SPELL_ZAR

        // Verb must be in at least one known bucket. Otherwise skip — we
        // refuse to guess.
        if (!isKnownRegular && !isE_IE && !isO_UE && !isE_I && !isCar && !isGar && !isZar) {
            return null
        }

        // Apply stem change for the boot positions in present-style tenses.
        fun shiftedStem(): String = when {
            isE_IE -> changeLastVowel(stem, 'e', "ie")
            isO_UE -> changeLastVowel(stem, 'o', "ue")
            isE_I  -> changeLastVowel(stem, 'e', "i")
            else   -> stem
        }

        val forms: List<String> = when (tense) {
            "presente" -> {
                // Boot: yo, tú, él, ellos → stem-change. nosotros/vosotros → plain.
                val s1 = shiftedStem()
                val s0 = stem
                when (ending) {
                    "ar" -> listOf(s1 + "o", s1 + "as", s1 + "a", s0 + "amos", s0 + "áis", s1 + "an")
                    "er" -> listOf(s1 + "o", s1 + "es", s1 + "e", s0 + "emos", s0 + "éis", s1 + "en")
                    "ir" -> listOf(s1 + "o", s1 + "es", s1 + "e", s0 + "imos", s0 + "ís", s1 + "en")
                    else -> return null
                }
            }

            "preterito" -> when (ending) {
                "ar" -> {
                    // Spelling shift in yo only: -car → -qué, -gar → -gué, -zar → -cé
                    val yo = when {
                        isCar -> stem.dropLast(1) + "qué"
                        isGar -> stem + "ué"
                        isZar -> stem.dropLast(1) + "cé"
                        else  -> stem + "é"
                    }
                    listOf(yo, stem + "aste", stem + "ó", stem + "amos", stem + "asteis", stem + "aron")
                }
                "er", "ir" -> {
                    // -ir stem-changers: e→i / o→u in 3rd sg/pl. Others plain.
                    val s3 = when {
                        ending == "ir" && isE_I  -> changeLastVowel(stem, 'e', "i")
                        ending == "ir" && isE_IE -> changeLastVowel(stem, 'e', "i")  // sentir → sintió
                        ending == "ir" && isO_UE -> changeLastVowel(stem, 'o', "u")  // dormir → durmió
                        else -> stem
                    }
                    listOf(stem + "í", stem + "iste", s3 + "ió", stem + "imos", stem + "isteis", s3 + "ieron")
                }
                else -> return null
            }

            "imperfecto" -> when (ending) {
                "ar"        -> listOf("aba","abas","aba","ábamos","abais","aban").map { stem + it }
                "er", "ir"  -> listOf("ía","ías","ía","íamos","íais","ían").map { stem + it }
                else        -> return null
            }

            "futuro"      -> listOf("é","ás","á","emos","éis","án").map { inf + it }
            "condicional" -> listOf("ía","ías","ía","íamos","íais","ían").map { inf + it }

            "subjuntivo" -> {
                // Subjuntivo: opposite-vowel endings. Stem-changers shift in all
                // positions for -ir, in boot for -ar/-er.
                val s1 = shiftedStem()
                when (ending) {
                    "ar" -> {
                        // -car/-gar/-zar spelling shift in ALL persons
                        val baseStem = when {
                            isCar -> stem.dropLast(1) + "qu"
                            isGar -> stem + "u"
                            isZar -> stem.dropLast(1) + "c"
                            else  -> s1
                        }
                        val baseN = when {
                            isCar -> stem.dropLast(1) + "qu"
                            isGar -> stem + "u"
                            isZar -> stem.dropLast(1) + "c"
                            else  -> stem
                        }
                        listOf(
                            baseStem + "e", baseStem + "es", baseStem + "e",
                            baseN + "emos", baseN + "éis", baseStem + "en",
                        )
                    }
                    "er" -> listOf(
                        s1 + "a", s1 + "as", s1 + "a",
                        stem + "amos", stem + "áis", s1 + "an",
                    )
                    "ir" -> {
                        // -ir: stem change in ALL persons (including nos/vos for e→i/o→u)
                        val nosStem = when {
                            isE_IE -> changeLastVowel(stem, 'e', "i")    // sentir → sintamos
                            isO_UE -> changeLastVowel(stem, 'o', "u")    // dormir → durmamos
                            isE_I  -> changeLastVowel(stem, 'e', "i")
                            else   -> stem
                        }
                        listOf(
                            s1 + "a", s1 + "as", s1 + "a",
                            nosStem + "amos", nosStem + "áis", s1 + "an",
                        )
                    }
                    else -> return null
                }
            }

            else -> return null
        }

        return ConjugationEntity(
            id = 0, verb = verb, tense = tense,
            yo = forms[0], tu = forms[1], el = forms[2],
            nosotros = forms[3], vosotros = forms[4], ellos = forms[5],
            isIrregular = !isKnownRegular,
            note = "auto",
        )
    }

    /** Replace the LAST occurrence of [from] in [stem] with [replacement]. */
    private fun changeLastVowel(stem: String, from: Char, replacement: String): String {
        val idx = stem.lastIndexOf(from)
        if (idx < 0) return stem
        return stem.substring(0, idx) + replacement + stem.substring(idx + 1)
    }

    /** All known infinitives the engine can safely conjugate. */
    fun knownVerbs(): Set<String> =
        KNOWN_REGULAR + STEM_E_IE + STEM_O_UE + STEM_E_I + SPELL_CAR + SPELL_GAR + SPELL_ZAR
}
