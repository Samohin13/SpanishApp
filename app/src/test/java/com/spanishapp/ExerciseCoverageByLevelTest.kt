package com.spanishapp

import com.spanishapp.ui.home.ExerciseGenerator
import com.spanishapp.ui.home.ExerciseType
import com.spanishapp.ui.home.LessonContentData
import org.junit.Test

/** Reports per-CEFR generator coverage so we can see what B1/B2 actually get. */
class ExerciseCoverageByLevelTest {

    @Test
    fun `report generator coverage per CEFR level`() {
        data class Stats(
            var lessons: Int = 0,
            var touched: Int = 0,
            val byType: MutableMap<ExerciseType, Int> = mutableMapOf(),
        )
        val stats = mutableMapOf<String, Stats>()
        for ((id, content) in LessonContentData.lessons) {
            val cefr = cefrOf(id)
            val s = stats.getOrPut(cefr) { Stats() }
            s.lessons++
            val gen = ExerciseGenerator.generate(id, content)
            if (gen.isNotEmpty()) s.touched++
            for (ex in gen) {
                s.byType[ex.type] = (s.byType[ex.type] ?: 0) + 1
            }
        }
        println()
        println("──────────────────────────────────────────────")
        println(" CEFR coverage of ExerciseGenerator")
        println("──────────────────────────────────────────────")
        listOf("A1", "A2", "B1", "B2").forEach { lvl ->
            val s = stats[lvl] ?: return@forEach
            println(" $lvl  lessons=${s.lessons}  touched=${s.touched}  generated=${s.byType.values.sum()}")
            s.byType.toList().sortedByDescending { it.second }.forEach { (t, n) ->
                println("       $n   $t")
            }
        }
        println("──────────────────────────────────────────────")
    }

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
}
