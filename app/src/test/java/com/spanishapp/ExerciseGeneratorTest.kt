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
                    }
                    ExerciseType.ORDER_LETTERS -> {
                        anagramCount++
                        assertFalse("anagram has space for $id",
                            ex.correctAnswer.contains(' '))
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
                    else -> fail("unexpected generated type: ${ex.type} in $id")
                }
            }
        }

        println("Generator results: $totalLessons total lessons, $lessonsTouched got " +
                "extras (${listenCount} listen + ${anagramCount} anagram + ${matchCount} match)")

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
