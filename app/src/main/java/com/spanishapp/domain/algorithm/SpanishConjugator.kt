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
        // v1.25.78: B2 времена
        "subjuntivo_imperfecto",  // hablara/hablase — основа B2 для Si conditionals
        "imperativo",             // ¡habla! — для команд и инструкций
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
        val isZc     = kind == VerbKind.ZC
        val isDucir  = kind == VerbKind.DUCIR
        val isUir    = kind == VerbKind.UIR

        // v1.25.98 FIX (audit games-C1): орфографические изменения в испанском
        // ПОЛНОСТЬЮ предсказуемы из написания инфинитива, поэтому определяем их
        // по spelling, а не только по VerbKind. Это решает и проблему комбинаций
        // «stem-change + spelling-change» (empezar = e→ie + z→c): kind несёт
        // стем-изменение, орфография накладывается автоматически.
        val isCar    = kind == VerbKind.SPELL_CAR || inf.endsWith("car")
        val isGar    = kind == VerbKind.SPELL_GAR || inf.endsWith("gar")
        val isZar    = kind == VerbKind.SPELL_ZAR || inf.endsWith("zar")

        fun shifted(): String = when {
            isE_IE -> changeLastVowel(stem, 'e', "ie")
            isO_UE -> changeLastVowel(stem, 'o', "ue")
            isE_I  -> changeLastVowel(stem, 'e', "i")
            else   -> stem
        }

        // v1.25.98 FIX (audit games-C1): орфография перед окончаниями на a/o.
        // Раньше отсутствовала полностью → генератор учил НЕПРАВИЛЬНЫМ формам:
        // «dirigo» (надо dirijo), «cogo» (cojo), «venco» (venzo), «distinguo»
        // (distingo), «corrigo» (corrijo). Правила:
        //   -ger/-gir  → g→j  перед a/o (dirigir → dirijo, dirija)
        //   -guir      → gu→g перед a/o (distinguir → distingo, distinga)
        //   -cer/-cir после согласной → c→z перед a/o (vencer → venzo, venza);
        //     после гласной работает ZC/DUCIR (parezco) — их не трогаем.
        fun orthoAO(s: String): String = when {
            inf.endsWith("guir") && s.endsWith("gu") -> s.dropLast(1)
            (inf.endsWith("ger") || inf.endsWith("gir")) && s.endsWith("g") -> s.dropLast(1) + "j"
            (inf.endsWith("cer") || inf.endsWith("cir")) && !isZc && !isDucir && s.endsWith("c") ->
                s.dropLast(1) + "z"
            else -> s
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
                    // yo-форма: единственная в presente с окончанием -o →
                    // здесь срабатывает орфография g→j / gu→g / c→z.
                    val yo = orthoAO(s1)
                    when (ending) {
                        "ar" -> listOf(yo + "o", s1 + "as", s1 + "a",
                                       stem + "amos", stem + "áis", s1 + "an")
                        "er" -> listOf(yo + "o", s1 + "es", s1 + "e",
                                       stem + "emos", stem + "éis", s1 + "en")
                        "ir" -> listOf(yo + "o", s1 + "es", s1 + "e",
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

            // ── SUBJUNTIVO IMPERFECTO (-ra forms) ──
            // v1.25.78: образуется от 3-го лица мн. ч. Pretérito Indefinido
            // путём замены "-ron" на "-ra/ras/ra/ramos(акцент)/rais/ran".
            // Используется в Si type 2 (Si tuviera tiempo, viajaría), Ojalá,
            // como si, после verbos в прошедшем времени.
            "subjuntivo_imperfecto" -> {
                // Получаем форму ellos из претерита через рекурсию.
                val pret = conjugateByKind(verb, kind, "preterito") ?: return null
                val ellos3 = pret.ellos.lowercase()
                if (!ellos3.endsWith("ron")) return null
                val base = ellos3.dropLast(3)  // hablaron → habla
                // Для -ar: hablaron → habla + ra/ras/ra/'ramos/rais/ran
                // Для -er/-ir: comieron → comie + ra/ras/ra/'ramos/rais/ran (но с акцентом é)
                // Универсально: base + ra, base + ras, base + ra,
                //                base(last-vowel-acc) + ramos, base + rais, base + ran
                val nosAcc = addAcuteToLastVowel(base)
                listOf(
                    base + "ra", base + "ras", base + "ra",
                    nosAcc + "ramos", base + "rais", base + "ran"
                )
            }

            // ── IMPERATIVO (positive — tú/usted/nosotros/vosotros/ustedes) ──
            // v1.25.78: только утвердительный (positive). Отрицательный
            // imperativo = subjuntivo presente с "no" впереди и в коде не
            // дублируется. yo формы у imperativo нет, кладём пустую строку.
            //
            // tú (positive)  = 3-е лицо ед. ч. presente (habla, come, vive)
            //                  ИРРЕГ глаголы (ven, di, ten, pon, sal, haz) — AUTHORED
            // usted          = 3-е лицо ед. ч. subjuntivo (hable, coma, viva)
            // nosotros       = 1-е лицо мн. ч. subjuntivo (hablemos, comamos)
            // vosotros (pos) = infinitivo без -r + -d (hablad, comed, vivid)
            // ustedes        = 3-е лицо мн. ч. subjuntivo (hablen, coman, vivan)
            "imperativo" -> {
                val pres = conjugateByKind(verb, kind, "presente") ?: return null
                val subj = conjugateByKind(verb, kind, "subjuntivo") ?: return null
                val vosotrosPos = inf.dropLast(1) + "d"   // hablar→hablad, comer→comed
                // Layout: yo(empty), tú, él(usted), nos, vos(vosotros pos), ellos(ustedes)
                listOf("", pres.el, subj.el, subj.nosotros, vosotrosPos, subj.ellos)
            }

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
                    // v1.25.98 FIX: орфография (c→qu / g→gu / z→c перед e)
                    // накладывается ПОВЕРХ стем-сдвига, а не вместо него.
                    // Раньше SPELL_ZAR ронял сдвиг: empezar → «empece»
                    // (надо empiece), almorzar → «almorce» (надо almuerce).
                    fun orthoE(s: String): String = when {
                        isCar -> s.dropLast(1) + "qu"
                        isGar -> s + "u"
                        isZar -> s.dropLast(1) + "c"
                        else  -> s
                    }
                    val sb = orthoE(shifted())  // empiez → empiec
                    val sn = orthoE(stem)       // empez  → empec
                    listOf(sb + "e", sb + "es", sb + "e",
                           sn + "emos", sn + "éis", sb + "en")
                }
                ending == "er" -> {
                    // Все окончания subj -er начинаются с a → орфография
                    // применяется ко всем лицам (vencer → venza..venzan).
                    val s1 = orthoAO(shifted())
                    val ns = orthoAO(stem)
                    listOf(s1 + "a", s1 + "as", s1 + "a",
                           ns + "amos", ns + "áis", s1 + "an")
                }
                ending == "ir" -> {
                    // Аналогично -er: dirigir → dirija..dirijan,
                    // seguir → siga..sigan, corregir → corrija..corrijan.
                    val s1 = orthoAO(shifted())
                    val nosStem = orthoAO(when {
                        isE_IE -> changeLastVowel(stem, 'e', "i")
                        isO_UE -> changeLastVowel(stem, 'o', "u")
                        isE_I  -> changeLastVowel(stem, 'e', "i")
                        else   -> stem
                    })
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

    /**
     * v1.25.78: ставит акут на последнюю гласную в slove.
     * Используется в Subjuntivo Imperfecto для nosotros формы:
     *   habla → háblá (habláramos), comie → comié (comiéramos).
     */
    private fun addAcuteToLastVowel(stem: String): String {
        val vowels = "aeiou"
        for (i in stem.indices.reversed()) {
            if (stem[i] in vowels) {
                val acc = when (stem[i]) {
                    'a' -> 'á'; 'e' -> 'é'; 'i' -> 'í'; 'o' -> 'ó'; 'u' -> 'ú'
                    else -> stem[i]
                }
                return stem.substring(0, i) + acc + stem.substring(i + 1)
            }
        }
        return stem
    }

    /**
     * v1.25.78: возвращает короткое русское объяснение правила глагола
     * для показа после ошибки в тренажёре.
     *
     * @param kind тип спряжения из VerbKind
     * @param tense время для уточнения контекста (опционально)
     * @return текст в 1-2 предложениях, например "e→ie в 1,2,3,6 лицах"
     */
    fun explainKind(kind: VerbKind, tense: String = ""): String = when (kind) {
        VerbKind.REGULAR_AR -> "Регулярный глагол на -ar. Окончания: -o, -as, -a, -amos, -áis, -an."
        VerbKind.REGULAR_ER -> "Регулярный глагол на -er. Окончания: -o, -es, -e, -emos, -éis, -en."
        VerbKind.REGULAR_IR -> "Регулярный глагол на -ir. Окончания: -o, -es, -e, -imos, -ís, -en."
        VerbKind.STEM_E_IE -> "Глагол с изменением корня e→ie в 1, 2, 3, 6 лицах. Пример: pensar → pienso, piensas, piensa, pensamos, pensáis, piensan."
        VerbKind.STEM_O_UE -> "Глагол с изменением корня o→ue в 1, 2, 3, 6 лицах. Пример: contar → cuento, cuentas, cuenta, contamos, contáis, cuentan."
        VerbKind.STEM_E_I -> "Глагол с изменением корня e→i в 1, 2, 3, 6 лицах (только -ir). Пример: pedir → pido, pides, pide, pedimos, pedís, piden."
        VerbKind.SPELL_CAR -> "Орфографическое изменение c→qu перед e. Пример: buscar → busqué (yo, претерит), busque (subjuntivo)."
        VerbKind.SPELL_GAR -> "Орфографическое изменение g→gu перед e. Пример: llegar → llegué (yo, претерит), llegue (subjuntivo)."
        VerbKind.SPELL_ZAR -> "Орфографическое изменение z→c перед e. Пример: empezar → empecé (yo, претерит), empiece (subjuntivo)."
        VerbKind.ZC -> "В 1-м лице ед. ч. presente и во всём subjuntivo вставляется -zc-. Пример: parecer → parezco, parezca."
        VerbKind.DUCIR -> "В претерите основа на -j-: conducir → conduje, condujiste, condujo, condujimos, condujisteis, condujeron."
        VerbKind.UIR -> "Между гласными вставляется -y- (кроме nosotros/vosotros). Пример: huir → huyo, huyes, huimos, huyen."
        VerbKind.AUTHORED -> "Полностью неправильный глагол — формы нужно запомнить отдельно."
    }
}
