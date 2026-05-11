package com.spanishapp.ui.home

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

    fun generate(lessonId: String, content: LessonContent): List<Exercise> {
        val seed = lessonId.hashCode().toLong()
        val random = Random(seed)

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

        return out
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
