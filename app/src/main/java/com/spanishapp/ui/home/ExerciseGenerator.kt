package com.spanishapp.ui.home

import com.spanishapp.data.repository.ConjugationData
import kotlin.random.Random

/**
 * Auto-generates additional exercises for a lesson from its own vocab so
 * the user gets variety beyond the hand-authored multiple-choice items.
 *
 * Inputs:
 *   • lessonId — used as the random seed so the SAME lesson always produces
 *                the SAME generated set within a session (predictable retry),
 *                but different lessons stay distinct.
 *   • content  — the LessonContent, scanned for `LessonItem`s that look like
 *                Spanish→Russian vocab pairs.
 *
 * Output: a list of new Exercise objects to APPEND to the lesson's
 * authored exercises. Caller decides ordering (current wiring just
 * concatenates).
 *
 * Generation rules (Phase 1):
 *   • Need ≥4 distinct vocab pairs to emit ListenAndPick.
 *   • Emit up to 2 LISTEN_PICK exercises with 3 distractors each.
 *   • Emit 1 ORDER_LETTERS for a short (3–8 chars, no spaces) word.
 *   • Skip if the lesson is short on vocab — better to author MC than
 *     generate weak items.
 */
object ExerciseGenerator {

    private data class VocabPair(val es: String, val ru: String)

    /** Levels: A1 = units 1–4, A2 = 5–8, B1 = 9–12, B2 = 13+. */
    private fun cefrOf(lessonId: String): String {
        val m = Regex("""^u(\d+)_l\d+$""").find(lessonId) ?: return "A1"
        val unit = m.groupValues[1].toInt()
        return when {
            unit <= 4 -> "A1"
            unit <= 8 -> "A2"
            unit <= 12 -> "B1"
            else -> "B2"
        }
    }

    /** Which generated types are permitted at each CEFR level. */
    private fun allowsType(cefr: String, type: ExerciseType): Boolean = when (type) {
        ExerciseType.LISTEN_PICK,
        ExerciseType.ORDER_LETTERS,
        ExerciseType.MATCH_PAIRS,
        ExerciseType.TAP_MISSING_WORD -> true                          // any level
        ExerciseType.BUILD_SENTENCE   -> cefr != "A1" || true          // ok at A1 with short phrases too
        ExerciseType.TRANSLATE,
        ExerciseType.LISTEN_TYPE,
        ExerciseType.CONJUGATION_GRID -> cefr != "A1"                  // skip in early A1
        else -> true
    }

    fun generate(lessonId: String, content: LessonContent): List<Exercise> {
        val seed = lessonId.hashCode().toLong()
        val random = Random(seed)
        val cefr = cefrOf(lessonId)

        val vocab = content.sections
            .flatMap { it.items }
            .filter { isVocabItem(it) }
            .map { VocabPair(es = it.left.trim(), ru = it.right.trim()) }
            .distinctBy { it.es.lowercase() }

        val out = mutableListOf<Exercise>()

        // ── LISTEN_PICK ──
        if (vocab.size >= 4) {
            val targets = vocab.shuffled(random).take(2)
            for (target in targets) {
                val pool = vocab.filter { it.es != target.es }
                val distractors = pool.shuffled(random).take(3).map { it.es }
                val options = (distractors + target.es).shuffled(random)
                out += Exercise(
                    type = ExerciseType.LISTEN_PICK,
                    instruction = "Послушай и выбери слово",
                    question = "",
                    options = options,
                    correctAnswer = target.es,
                    audioText = target.es,
                    explanation = if (target.ru.isNotBlank()) "${target.es} — ${target.ru}" else target.es,
                )
            }
        }

        // ── ORDER_LETTERS ──
        val anagram = vocab
            .filter { it.es.length in 3..10 && !it.es.contains(' ') }
            .shuffled(random)
            .firstOrNull()
        if (anagram != null) {
            val hint = if (anagram.ru.isNotBlank()) anagram.ru else "?"
            out += Exercise(
                type = ExerciseType.ORDER_LETTERS,
                instruction = "Собери слово из букв",
                question = hint,
                correctAnswer = anagram.es,
                explanation = if (anagram.ru.isNotBlank()) "${anagram.es} — ${anagram.ru}" else anagram.es,
            )
        }

        // ── TAP_MISSING_WORD: article gap-fill for "el/la/los/las + noun" items ──
        val articles = setOf("el", "la", "los", "las", "un", "una", "unos", "unas")
        val articleCandidate = vocab
            .map { it to it.es.split(" ", limit = 2) }
            .firstOrNull { (_, parts) ->
                parts.size == 2 && parts[0].lowercase() in articles
            }
        if (articleCandidate != null) {
            val (pair, parts) = articleCandidate
            val correctArticle = parts[0].lowercase()
            // Sensible distractor set: opposite-gender / number article.
            val distractors = when (correctArticle) {
                "el" -> listOf("la", "los")
                "la" -> listOf("el", "las")
                "los" -> listOf("las", "el")
                "las" -> listOf("los", "la")
                "un" -> listOf("una", "unos")
                "una" -> listOf("un", "unas")
                "unos" -> listOf("unas", "un")
                "unas" -> listOf("unos", "una")
                else -> listOf("el", "la")
            }
            val opts = (distractors + correctArticle).shuffled(random)
            out += Exercise(
                type = ExerciseType.TAP_MISSING_WORD,
                instruction = "Выбери правильный артикль",
                question = "___ ${parts[1]}",
                options = opts,
                correctAnswer = correctArticle,
                explanation = if (pair.ru.isNotBlank()) "${pair.es} — ${pair.ru}" else pair.es,
            )
        }

        // ── BUILD_SENTENCE: short Spanish phrase from lesson items ──
        // Look at ALL items (not just the 3-word vocab heuristic) for 3-6 word
        // phrases with a Russian translation — those are usable as build-sentence.
        val phrasePool = content.sections
            .flatMap { it.items }
            .map { Triple(it.left.trim(), it.right.trim(), it.note) }
            .filter { (es, ru, _) ->
                val words = es.split(Regex("\\s+"))
                val isSpanishPhrase = words.size in 3..6 &&
                    es.any { it.isLetter() } &&
                    es.none { it in 'Ѐ'..'ӿ' }
                val hasRu = ru.any { it in 'Ѐ'..'ӿ' }
                isSpanishPhrase && hasRu
            }
        val phraseCandidate = phrasePool.shuffled(random).firstOrNull()
        if (phraseCandidate != null) {
            val (es, ru, _) = phraseCandidate
            val clean = es.trim().trimEnd('.', '!', '?', ',', ';', ':')
            val tokens = clean.split(Regex("\\s+"))

            // For B1/B2: add 2-3 distractor words from the same lesson to
            // make it harder. The word pool keeps the correct tokens but
            // appends extras that must be IGNORED.
            val isAdvanced = cefr in listOf("B1", "B2")
            val distractors = if (isAdvanced) {
                // Pull short tokens from OTHER phrases or vocab items
                val tokenSet = tokens.map { it.lowercase() }.toSet()
                val others = phrasePool
                    .filter { (otherEs, _, _) -> otherEs != es }
                    .flatMap { it.first.split(Regex("\\s+")) }
                    .map { it.trim().trimEnd('.', '!', '?', ',', ';', ':') }
                    .filter { it.length in 2..10 && it.lowercase() !in tokenSet
                              && it.none { ch -> ch in 'Ѐ'..'ӿ' } }
                    .distinct()
                    .shuffled(random)
                    .take(if (cefr == "B2") 3 else 2)
                others
            } else emptyList()

            val finalWords = (tokens + distractors).shuffled(random)
            out += Exercise(
                type = ExerciseType.BUILD_SENTENCE,
                instruction = if (distractors.isEmpty()) "Собери предложение"
                              else "Собери предложение (есть лишние слова!)",
                question = ru,
                words = finalWords,
                correctAnswer = tokens.joinToString(" "),
                explanation = "$clean — $ru",
            )
        }

        // ── TRANSLATE (ru→es): single-word translation typing ──
        // Only for short words; avoid the word already chosen for other typing exercises.
        if (vocab.size >= 5) {
            val taken = setOfNotNull(anagram?.es)
            val translateCandidate = vocab
                .filter { it.es.length in 3..12 && !it.es.contains(' ')
                          && it.es !in taken
                          && it.ru.isNotBlank() }
                .shuffled(random)
                .firstOrNull()
            if (translateCandidate != null) {
                out += Exercise(
                    type = ExerciseType.TRANSLATE,
                    instruction = "Переведи на испанский",
                    question = translateCandidate.ru,
                    correctAnswer = translateCandidate.es,
                    explanation = "${translateCandidate.es} — ${translateCandidate.ru}",
                )
            }
        }

        // ── LISTEN_TYPE: dictation of a short Spanish word (≥6 vocab lessons only) ──
        if (vocab.size >= 6) {
            // Pick a word that wasn't used for OrderLetters to avoid same-word reuse
            val anagramEs = anagram?.es
            val dictation = vocab
                .filter { it.es.length in 3..10 && !it.es.contains(' ') && it.es != anagramEs }
                .shuffled(random)
                .firstOrNull()
            if (dictation != null) {
                out += Exercise(
                    type = ExerciseType.LISTEN_TYPE,
                    instruction = "Напечатай что слышишь",
                    question = "",
                    correctAnswer = dictation.es,
                    audioText = dictation.es,
                    explanation = if (dictation.ru.isNotBlank()) "${dictation.es} — ${dictation.ru}" else dictation.es,
                )
            }
        }

        // ── CONJUGATION_GRID: if the lesson is about a known verb, drop in
        //    one full-table exercise (yo, tú, él, nosotros, vosotros, ellos). ──
        run {
            val text = buildString {
                append(content.intro).append(" ")
                content.sections.forEach { s ->
                    append(s.heading).append(" ")
                    s.items.forEach { append(it.left).append(" ") }
                }
            }.lowercase()

            // Match infinitive in lesson text. Prefer longer matches first.
            val knownVerbs = ConjugationData.getAll()
                .map { it.verb.lowercase() }
                .toSet()
                .sortedByDescending { it.length }
            val verbMatch = knownVerbs.firstOrNull { v ->
                Regex("\\b${Regex.escape(v)}\\b").containsMatchIn(text)
            }
            if (verbMatch != null) {
                // Pick a tense — presente by default, otherwise any available
                val available = ConjugationData.getAll().filter { it.verb.lowercase() == verbMatch }
                val pres = available.firstOrNull { it.tense == "presente" } ?: available.firstOrNull()
                if (pres != null) {
                    out += Exercise(
                        type = ExerciseType.CONJUGATION_GRID,
                        instruction = "Заполни таблицу спряжений",
                        question = "",
                        hint = "${pres.verb} | ${pres.tense}",
                        correctAnswer = pres.verb,   // sentinel for checkCorrect
                        conjugationForms = listOf(
                            pres.yo, pres.tu, pres.el,
                            pres.nosotros, pres.vosotros, pres.ellos,
                        ),
                        explanation = pres.note,
                    )
                }
            }
        }

        // ── MATCH_PAIRS ──
        // Need ≥4 pairs with distinct short Spanish words and clear RU translations
        if (vocab.size >= 4) {
            val matchPool = vocab
                .filter { it.es.length in 2..18 && it.ru.length in 1..24 }
                .distinctBy { it.ru.lowercase() }  // avoid duplicate RU labels
            if (matchPool.size >= 4) {
                val picked = matchPool.shuffled(random).take(if (matchPool.size >= 5) 5 else 4)
                out += Exercise(
                    type = ExerciseType.MATCH_PAIRS,
                    instruction = "Соедини пары",
                    question = "",
                    pairs = picked.map { it.es to it.ru },
                    correctAnswer = "match_pairs_ok",
                    explanation = "",
                )
            }
        }

        // ── CEFR gate: drop types that are too advanced for the level ──
        return out.filter { allowsType(cefr, it.type) }
    }

    // ── Vocab-item heuristic (mirrors LessonContentData.isVocabItem) ────
    private fun isVocabItem(item: LessonItem): Boolean {
        val left = item.left
        val right = item.right
        val letters = left.filter { it.isLetter() }
        val latin = letters.count { it !in 'Ѐ'..'ӿ' }
        val cyrillic = letters.count { it in 'Ѐ'..'ӿ' }
        val unique = letters.map { it.lowercaseChar() }.toSet()
        val wordCount = left.trim().split(Regex("\\s+")).size
        val leftIsSpanish = latin >= 4 && unique.size >= 3 && cyrillic == 0
        val rightIsRussian = right.any { it in 'Ѐ'..'ӿ' }
        return leftIsSpanish && rightIsRussian && wordCount <= 3
    }
}
