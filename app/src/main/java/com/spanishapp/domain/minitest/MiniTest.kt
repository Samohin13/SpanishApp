package com.spanishapp.domain.minitest

import com.spanishapp.ui.home.Exercise
import com.spanishapp.ui.home.ExerciseType
import com.spanishapp.ui.home.LessonContentData
import com.spanishapp.ui.home.RoadmapData
import kotlin.random.Random

/**
 * Mini-test = quick quiz between regular lessons.
 *
 * 16 blocks × 3 mini-tests (after lessons 5, 10, 15) = 48 mini-tests
 * generated at runtime by sampling exercises from the previous 5 lessons.
 *
 * No new authored content — we reuse existing exercises from
 * [LessonContentData], so the system scales automatically as the
 * lesson catalog grows.
 *
 * Mini-tests are OPTIONAL — user can skip; completion is tracked in
 * a lightweight DataStore (not Room).
 *
 * @property id stable identifier, e.g. `u1_mt5` (block 1, after lesson 5)
 * @property unitId block id "1".."16"
 * @property position 5, 10 or 15 (after which lesson in the block)
 * @property title display title, e.g. "Мини-тест 5/15"
 * @property coverageLessons lesson keys whose exercises are pooled
 * @property exercises 5 sampled exercises (or fewer if pool is small)
 */
data class MiniTest(
    val id: String,
    val unitId: String,
    val position: Int,
    val title: String,
    val coverageLessons: List<String>,
    val exercises: List<Exercise>,
)

object MiniTestGenerator {

    /** Positions inside a block where a mini-test appears. */
    val POSITIONS: List<Int> = listOf(5, 10, 15)

    /** Number of sampled exercises per mini-test. */
    const val QUESTION_COUNT = 5

    /** Pass threshold (60% — see CourseDetailScreen UI copy). */
    const val PASS_THRESHOLD = 0.6f

    /** XP awarded on pass. */
    const val XP_REWARD = com.spanishapp.domain.algorithm.XpSystem.MINI_TEST_PASSED

    /**
     * Exercise types we are happy to render in a quick mini-test.
     * Excludes long-form (CONJUGATION_GRID), audio-dependent (LISTEN_*),
     * mic-dependent (SPEAKING/SPEAK_REPEAT) and visual-grid-heavy types
     * so the test stays uniform and fast.
     */
    private val SUPPORTED_TYPES: Set<ExerciseType> = setOf(
        ExerciseType.MULTIPLE_CHOICE,
        ExerciseType.TAP_MISSING_WORD,
        ExerciseType.TRANSLATE,
        ExerciseType.BUILD_SENTENCE,
        ExerciseType.SPOT_THE_ERROR,
        ExerciseType.READ_NUMBER,
        ExerciseType.ORDER_LETTERS,
    )

    /**
     * Build the list of all 48 mini-tests for the static roadmap
     * (16 blocks × 3 positions). Missing blocks/positions are silently
     * skipped — generator returns null when the lesson pool is empty.
     */
    fun all(): List<MiniTest> {
        val out = mutableListOf<MiniTest>()
        for (unit in 1..16) {
            for (pos in POSITIONS) {
                generate(unit.toString(), pos)?.let(out::add)
            }
        }
        return out
    }

    /**
     * Generate one mini-test by sampling 5 exercises from the previous
     * 5 lessons (`[position-5, position-1]` by ROADMAP ORDER, which
     * includes `_5` bridge lessons like `u1_l13_5`, `u4_l13_5` etc).
     *
     * Lesson IDs are resolved through [RoadmapData] so a single source
     * of truth controls both navigation and mini-test coverage —
     * `_5` bridge lessons that sit between numerically-named lessons
     * DO count toward the 5/10/15 trigger and ARE sampled into the pool.
     *
     * Sampling is deterministic per (unitId, position) so the same test
     * is shown to all users — this matches lesson content semantics and
     * keeps QA reproducible.
     *
     * Returns null if no supported exercises exist in that range.
     */
    fun generate(unitId: String, position: Int): MiniTest? {
        if (position !in POSITIONS) return null

        // Resolve lesson IDs via the canonical roadmap order
        // (preserves `_5` bridge lessons in sequential position).
        val unit = RoadmapData.units.firstOrNull { it.id == unitId } ?: return null
        val firstIdx = position - 5             // inclusive
        val lastIdx  = position - 1             // inclusive
        if (firstIdx < 0 || firstIdx >= unit.lessons.size) return null

        val coverage: List<String> = (firstIdx..lastIdx)
            .mapNotNull { idx ->
                unit.lessons.getOrNull(idx)?.let { lesson ->
                    lesson.id ?: "u${unitId}_l${idx}"
                }
            }

        // Pool exercises from the 5 source lessons (merged V1+V2 map).
        val pool: List<Exercise> = coverage
            .flatMap { key -> LessonContentData.lessons[key]?.exercises.orEmpty() }
            .filter { it.type in SUPPORTED_TYPES }
            // Drop exercises with missing/empty correctAnswer — safety net.
            .filter { it.correctAnswer.isNotBlank() }

        if (pool.isEmpty()) return null

        // Deterministic seed per (unitId, position) — same test for everyone.
        val seed = ("${unitId}_${position}").hashCode().toLong()
        val sampled = pool.shuffled(Random(seed))
            .take(QUESTION_COUNT)

        val id = "u${unitId}_mt${position}"
        val title = "Мини-тест ${position}/15"
        return MiniTest(
            id = id,
            unitId = unitId,
            position = position,
            title = title,
            coverageLessons = coverage,
            exercises = sampled,
        )
    }
}
