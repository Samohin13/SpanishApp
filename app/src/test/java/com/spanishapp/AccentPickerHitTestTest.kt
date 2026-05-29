package com.spanishapp

import com.spanishapp.ui.chat.AccentPickerHitTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v1.25.54 — exhaustive coverage hit-test:
 *  - Все размеры (0..6 accents)
 *  - Все cell positions достижимы
 *  - Threshold boundary precise
 *  - Coerce'ы на extreme finger positions
 *  - Real ES/RU keyboard data (все клавиши которые имеют accents)
 */
class AccentPickerHitTestTest {

    // Typical units. cellW=48 (44+4), key 36dp wide.
    private val cellW = 48f
    private val padPx = 6f
    private val upThreshold = 20f
    private val keyWidth = 36f

    private fun hitAt(
        accentsCount: Int,
        x: Float,
        y: Float,
    ) = AccentPickerHitTest.computeHoveredIdx(
        accentsCount = accentsCount,
        fingerLocalX = x,
        fingerLocalY = y,
        downLocalY = 0f,
        keyWidthPx = keyWidth,
        cellWidthPx = cellW,
        padPx = padPx,
        upThresholdPx = upThreshold,
    )

    /**
     * Центр cell в local-coords (gesture).
     * popup centered над key, row 0 — top (нужен upward swipe), row 1 — bottom (default).
     */
    private fun cellCenterX(accentsCount: Int, col: Int): Float {
        val rowsCount = if (accentsCount > 3) 2 else 1
        val cellsPerRow = (accentsCount + rowsCount - 1) / rowsCount
        val popupWidth = cellsPerRow * cellW + 2 * padPx
        val popupLeftLocal = (keyWidth - popupWidth) / 2f
        return popupLeftLocal + padPx + col * cellW + cellW / 2f
    }

    // ────────────────────────────────────────────────────
    //  EXHAUSTIVE: каждая cell достижима
    // ────────────────────────────────────────────────────
    @Test
    fun `exhaustive — every cell reachable for 1 accent`() {
        assertEquals(0, hitAt(1, x = cellCenterX(1, 0), y = 0f))
    }

    @Test
    fun `exhaustive — every cell reachable for 2 accents`() {
        // 2 accents → 1 row, 2 cols
        assertEquals(0, hitAt(2, x = cellCenterX(2, 0), y = 0f))
        assertEquals(1, hitAt(2, x = cellCenterX(2, 1), y = 0f))
    }

    @Test
    fun `exhaustive — every cell reachable for 3 accents`() {
        // 3 → 1 row, 3 cols
        assertEquals(0, hitAt(3, x = cellCenterX(3, 0), y = 0f))
        assertEquals(1, hitAt(3, x = cellCenterX(3, 1), y = 0f))
        assertEquals(2, hitAt(3, x = cellCenterX(3, 2), y = 0f))
    }

    @Test
    fun `exhaustive — every cell reachable for 4 accents (2x2)`() {
        // 4 → 2 rows × 2 cols. cellsPerRow = (4+1)/2 = 2
        // top row (rowIdx=0, y < -20): idx 0, 1
        assertEquals(0, hitAt(4, x = cellCenterX(4, 0), y = -30f))
        assertEquals(1, hitAt(4, x = cellCenterX(4, 1), y = -30f))
        // bottom row (rowIdx=1, y=0): idx 2, 3
        assertEquals(2, hitAt(4, x = cellCenterX(4, 0), y = 0f))
        assertEquals(3, hitAt(4, x = cellCenterX(4, 1), y = 0f))
    }

    @Test
    fun `exhaustive — every cell reachable for 5 accents (3 plus 2)`() {
        // 5 → 2 rows. cellsPerRow = (5+1)/2 = 3. Top has 3, bottom has 2.
        // top: 0,1,2 ; bottom: 3,4 (and col 2 of bottom coerced to 4)
        assertEquals(0, hitAt(5, x = cellCenterX(5, 0), y = -30f))
        assertEquals(1, hitAt(5, x = cellCenterX(5, 1), y = -30f))
        assertEquals(2, hitAt(5, x = cellCenterX(5, 2), y = -30f))
        assertEquals(3, hitAt(5, x = cellCenterX(5, 0), y = 0f))
        assertEquals(4, hitAt(5, x = cellCenterX(5, 1), y = 0f))
    }

    @Test
    fun `exhaustive — every cell reachable for 6 accents (3 plus 3)`() {
        // 6 → 2 rows × 3 cols
        for (col in 0..2) {
            assertEquals("top row col=$col", col, hitAt(6, x = cellCenterX(6, col), y = -30f))
            assertEquals("bottom row col=$col", 3 + col, hitAt(6, x = cellCenterX(6, col), y = 0f))
        }
    }

    // ────────────────────────────────────────────────────
    //  THRESHOLD BOUNDARY
    // ────────────────────────────────────────────────────
    @Test
    fun `threshold dy strictly less than -upThreshold switches to top`() {
        // Just past threshold: dy = -upThreshold - epsilon → top
        assertEquals(1, hitAt(6, x = cellCenterX(6, 1), y = -20.01f))  // < -20 → top
        assertEquals(4, hitAt(6, x = cellCenterX(6, 1), y = -20f))      // == -20 → bottom (strict)
        assertEquals(4, hitAt(6, x = cellCenterX(6, 1), y = -19.99f))   // > -20 → bottom
    }

    // ────────────────────────────────────────────────────
    //  COERCE — finger за пределами cells
    // ────────────────────────────────────────────────────
    @Test
    fun `coerce — finger far left clamps to col 0`() {
        assertEquals(3, hitAt(6, x = -9999f, y = 0f))  // bottom row, col 0
    }

    @Test
    fun `coerce — finger far right clamps to last col`() {
        assertEquals(5, hitAt(6, x = 9999f, y = 0f))  // bottom row, col 2 → idx 5
    }

    @Test
    fun `coerce — top row far right`() {
        assertEquals(2, hitAt(6, x = 9999f, y = -50f))  // top row, last col
    }

    // ────────────────────────────────────────────────────
    //  EDGE CASES
    // ────────────────────────────────────────────────────
    @Test
    fun `0 accents returns 0 safely`() {
        assertEquals(0, hitAt(0, x = 0f, y = 0f))
        assertEquals(0, hitAt(0, x = 9999f, y = -9999f))
    }

    @Test
    fun `single accent ignores all positions`() {
        for (x in -1000..1000 step 100) {
            for (y in -1000..1000 step 100) {
                assertEquals("at ($x,$y)", 0, hitAt(1, x.toFloat(), y.toFloat()))
            }
        }
    }

    @Test
    fun `2 accents single row ignores up-swipe`() {
        // rowsCount=1 → rowIdx always 0
        assertEquals(0, hitAt(2, x = cellCenterX(2, 0), y = -50f))  // up swipe — still col 0
        assertEquals(1, hitAt(2, x = cellCenterX(2, 1), y = -50f))
    }

    @Test
    fun `3 accents single row ignores up-swipe`() {
        assertEquals(0, hitAt(3, x = cellCenterX(3, 0), y = -50f))
        assertEquals(1, hitAt(3, x = cellCenterX(3, 1), y = -50f))
        assertEquals(2, hitAt(3, x = cellCenterX(3, 2), y = -50f))
    }

    // ────────────────────────────────────────────────────
    //  REAL KEYBOARD DATA — ES & RU
    // ────────────────────────────────────────────────────
    /**
     * Все ES keys с accents должны давать pickable picker без падений.
     * Тест: для каждого accent count в реальных данных — итерируем по cells.
     */
    @Test
    fun `real ES keyboard — every accents-key has reachable cells`() {
        val accentCounts = listOf(
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,  // single-accent keys
            3,  // y: / ý ÿ
            5,  // u: < ú ù û ü
            5,  // i: > í ì î ï
            6,  // o: [ ó ò ô ö õ
            6,  // a: ! á à â ã ä
            6,  // e: ´ é è ê ë €
            4,  // n: ? ñ ń ň
        )
        for (count in accentCounts) {
            val rowsCount = if (count > 3) 2 else 1
            val cellsPerRow = (count + rowsCount - 1) / rowsCount
            // Все cells должны быть достижимы
            for (i in 0 until count) {
                val row = i / cellsPerRow
                val col = i % cellsPerRow
                val y = if (row == 0 && rowsCount > 1) -30f else 0f
                val hit = hitAt(count, x = cellCenterX(count, col), y = y)
                assertEquals("accents=$count idx=$i (row=$row col=$col)", i, hit)
            }
        }
    }

    /**
     * RU keyboard: тот же exhaustive check для размеров accents.
     */
    @Test
    fun `real RU keyboard — every accents-key has reachable cells`() {
        // Размеры из ruAccents (после v1.25.46): {1,1,1,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,2,2,2,2,3,3,3,2,2}
        // Берём уникальные размеры.
        val uniqueCounts = setOf(1, 2, 3)
        for (count in uniqueCounts) {
            val rowsCount = if (count > 3) 2 else 1
            val cellsPerRow = (count + rowsCount - 1) / rowsCount
            for (i in 0 until count) {
                val row = i / cellsPerRow
                val col = i % cellsPerRow
                val y = if (row == 0 && rowsCount > 1) -30f else 0f
                val hit = hitAt(count, x = cellCenterX(count, col), y = y)
                assertEquals("RU count=$count idx=$i", i, hit)
            }
        }
    }

    // ────────────────────────────────────────────────────
    //  RESULT MONOTONICITY — сглаживание slide
    // ────────────────────────────────────────────────────
    @Test
    fun `monotonic — slide right increments col`() {
        // 6 accents, bottom row. Slide справа налево и обратно.
        val xs = (-100..100 step 10).map { it.toFloat() }
        var lastIdx = -1
        for (x in xs) {
            val idx = hitAt(6, x = x, y = 0f)
            // Идём слева направо → idx должен не уменьшаться
            assertTrue("Slide non-monotonic at x=$x: was $lastIdx, now $idx",
                idx >= lastIdx || idx == 3)  // wrap around to col 0 at right boundary doesn't apply
            lastIdx = idx
        }
    }

    @Test
    fun `monotonic — slide upward triggers row switch ONCE`() {
        // 6 accents. Slide finger UP. Check transition точно одна.
        val yValues = (0 downTo -50 step 1).map { it.toFloat() }
        var switches = 0
        var lastRow = -1
        for (y in yValues) {
            val idx = hitAt(6, x = cellCenterX(6, 1), y = y)
            val row = idx / 3
            if (lastRow != -1 && row != lastRow) switches++
            lastRow = row
        }
        assertEquals("Should be exactly 1 row switch during upward slide", 1, switches)
    }
}
