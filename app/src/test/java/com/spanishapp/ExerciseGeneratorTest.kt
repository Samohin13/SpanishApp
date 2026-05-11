package com.spanishapp

import com.spanishapp.ui.home.ExerciseGenerator
import com.spanishapp.ui.home.ExerciseType
import com.spanishapp.ui.home.LessonContentData
import org.junit.Assert.*
import org.junit.Test

/**
 * Smoke-tests the auto-generator across every authored lesson.
 *
 * It must NEVER produce malformed exercises (empty options, missing answer,
 * answer not in options) and it should add real variety on top of MC.
 */
class ExerciseGeneratorTest {

    @Test
    fun `every lesson generator produces well-formed exercises`() {
        var listenCount = 0
        var anagramCount = 0
        var matchCount = 0
        var articleCount = 0
        var buildSentenceCount = 0
        var listenTypeCount = 0
        var translateCount = 0
        var conjugationCount = 0
        var totalLessons = 0
        var lessonsTouched = 0

        for ((id, content) in LessonContentData.lessons) {
            totalLessons++
            val generated = ExerciseGenerator.generate(id, content)
            if (generated.isNotEmpty()) lessonsTouched++

            for (ex in generated) {
                assertTrue("correctAnswer empty for $id", ex.correctAnswer.isNotBlank())
                when (ex.type) {
                    ExerciseType.LISTEN_PICK -> {
                        listenCount++
                        assertEquals("listen_pick options size for $id",
                            4, ex.options.size)
                        assertTrue("listen_pick correct not in options for $id",
                            ex.correctAnswer in ex.options)
                        assertTrue("listen_pick audioText for $id",
                            ex.audioText.isNotBlank())
                        // Options must be unique — duplicates make the test
                        // ambiguous and visually broken.
                        assertEquals("listen_pick options must be unique for $id",
                            ex.options.size, ex.options.toSet().size)
                    }
                    ExerciseType.ORDER_LETTERS -> {
                        anagramCount++
                        assertTrue("anagram all letters for $id (was '${ex.correctAnswer}')",
                            ex.correctAnswer.all { it.isLetter() })
                        assertTrue("anagram length 3..10 for $id",
                            ex.correctAnswer.length in 3..10)
                    }
                    ExerciseType.MATCH_PAIRS -> {
                        matchCount++
                        assertTrue("match_pairs pairs size 4-6 for $id",
                            ex.pairs.size in 4..6)
                        assertEquals("match_pairs unique RU labels for $id",
                            ex.pairs.size, ex.pairs.map { it.second.lowercase() }.toSet().size)
                    }
                    ExerciseType.TAP_MISSING_WORD -> {
                        articleCount++
                        assertTrue("tap_missing options for $id",
                            ex.options.size == 3)
                        assertTrue("tap_missing correct in options for $id",
                            ex.correctAnswer in ex.options)
                        assertTrue("tap_missing sentence has blank for $id",
                            ex.question.contains("___"))
                    }
                    ExerciseType.BUILD_SENTENCE -> {
                        buildSentenceCount++
                        // 3-6 correct tokens + up to 3 distractors = up to 9
                        assertTrue("build_sentence words 3-9 for $id",
                            ex.words.size in 3..9)
                        // Every token of correctAnswer must appear in words
                        val correctTokens = ex.correctAnswer.split(" ")
                        val wordsLower = ex.words.map { it.lowercase() }
                        for (t in correctTokens) {
                            assertTrue("build_sentence missing token '$t' for $id",
                                t.lowercase() in wordsLower)
                        }
                    }
                    ExerciseType.LISTEN_TYPE -> {
                        listenTypeCount++
                        assertTrue("listen_type audioText for $id", ex.audioText.isNotBlank())
                        assertFalse("listen_type no space for $id",
                            ex.correctAnswer.contains(' '))
                    }
                    ExerciseType.TRANSLATE -> {
                        translateCount++
                        assertTrue("translate question non-empty for $id",
                            ex.question.isNotBlank())
                        // Translate exercises should never carry a hyphenated
                        // pronunciation guide as the target — those would be
                        // un-typeable.
                        assertFalse("translate hyphen-free target for $id (was '${ex.correctAnswer}')",
                            ex.correctAnswer.contains('-'))
                    }
                    ExerciseType.CONJUGATION_GRID -> {
                        conjugationCount++
                        assertEquals("conjugation 6 forms for $id",
                            6, ex.conjugationForms.size)
                        assertTrue("conjugation forms non-empty for $id",
                            ex.conjugationForms.all { it.isNotBlank() })
                        assertTrue("conjugation hint has '|' for $id",
                            ex.hint.contains("|"))
                    }
                    else -> fail("unexpected generated type: ${ex.type} in $id")
                }
            }
        }

        println("Generator results: $totalLessons total lessons, $lessonsTouched got " +
                "extras (${listenCount} listen + ${anagramCount} anagram + ${matchCount} match + " +
                "${articleCount} article + ${buildSentenceCount} build + ${listenTypeCount} type + " +
                "${translateCount} translate + ${conjugationCount} conjugation)")

        // Must touch a meaningful share of lessons or the generator is broken.
        assertTrue("Generator touched too few lessons: $lessonsTouched/$totalLessons",
            lessonsTouched * 2 > totalLessons)  // > 50%
    }

    @Test
    fun `generator output is deterministic per lesson`() {
        val lessonId = "u1_l0"
        val content = LessonContentData.lessons[lessonId] ?: return
        val a = ExerciseGenerator.generate(lessonId, content)
        val b = ExerciseGenerator.generate(lessonId, content)
        assertEquals(a.size, b.size)
        a.zip(b).forEach { (x, y) ->
            assertEquals(x.type, y.type)
            assertEquals(x.correctAnswer, y.correctAnswer)
            assertEquals(x.options, y.options)
        }
    }
}
