package com.spanishapp

import com.spanishapp.ui.chat.AccentPickerHitTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * v1.25.53 — тесты hit-test logic для accent picker'а.
 *
 * Покрывает:
 *  - 1-row pickers (≤3 accents)
 *  - 2-row pickers (4-6 accents)
 *  - dy threshold для row switching
 *  - col selection через absolute X coords
 *  - edge cases (finger вне popup, coerce'ы)
 */
class AccentPickerHitTestTest {

    // ── Default setup для типичного scenario ─────────
    // Key shirina ~36dp, cell shirina 48dp (44dp + 4dp gap),
    // padPx 6dp, upThreshold 20dp.
    // 6 accents picker (3+3 rows) расположен над key.
    // Density ~2.75 для типичного device.
    // Все coords в float pixels для simplicity test'ов.

    private val cellW = 48f      // 1px = 1dp for simplicity
    private val padPx = 6f
    private val upThreshold = 20f
    private val keyRootX = 100f   // key starts at x=100
    private val keyWidth = 36f
    private val popupLeftX = keyRootX - 60f  // popup wider than key, centered

    @Test
    fun `1 row picker — 3 accents, bottom only`() {
        // 3 accents → 1 row. Always rowIdx=0.
        // Finger directly above key center → col=1 (middle)
        val idx = AccentPickerHitTest.computeHoveredIdx(
            accentsCount = 3,
            fingerLocalX = keyWidth / 2f,  // local center of key
            fingerLocalY = 0f,
            downLocalY = 0f,
            keyRootX = keyRootX,
            popupLeftX = popupLeftX,
            cellWidthPx = cellW,
            padPx = padPx,
            upThresholdPx = upThreshold,
        )
        assertEquals("Middle of 3-cell row should be idx=1", 1, idx)
    }

    @Test
    fun `2 row picker — 6 accents, bottom row default`() {
        // 6 accents → 2 rows × 3 cols. dy=0 → bottom row (rowIdx=1).
        val idx = AccentPickerHitTest.computeHoveredIdx(
            accentsCount = 6,
            fingerLocalX = keyWidth / 2f,
            fingerLocalY = 0f,  // no upward movement
            downLocalY = 0f,
            keyRootX = keyRootX,
            popupLeftX = popupLeftX,
            cellWidthPx = cellW,
            padPx = padPx,
            upThresholdPx = upThreshold,
        )
        // rowIdx=1, colIdx=1 (middle) → flat = 1*3+1 = 4 (5th accent)
        assertEquals(4, idx)
    }

    @Test
    fun `2 row picker — swipe up past threshold switches to top`() {
        // dy = -25 (past threshold of -20) → top row
        val idx = AccentPickerHitTest.computeHoveredIdx(
            accentsCount = 6,
            fingerLocalX = keyWidth / 2f,
            fingerLocalY = -25f,
            downLocalY = 0f,
            keyRootX = keyRootX,
            popupLeftX = popupLeftX,
            cellWidthPx = cellW,
            padPx = padPx,
            upThresholdPx = upThreshold,
        )
        // rowIdx=0, colIdx=1 → flat = 0*3+1 = 1 (2nd accent)
        assertEquals(1, idx)
    }

    @Test
    fun `2 row picker — slight upward swipe stays in bottom`() {
        // dy = -10 (less than -20 threshold) → still bottom
        val idx = AccentPickerHitTest.computeHoveredIdx(
            accentsCount = 6,
            fingerLocalX = keyWidth / 2f,
            fingerLocalY = -10f,
            downLocalY = 0f,
            keyRootX = keyRootX,
            popupLeftX = popupLeftX,
            cellWidthPx = cellW,
            padPx = padPx,
            upThresholdPx = upThreshold,
        )
        assertEquals(4, idx)  // bottom row middle
    }

    @Test
    fun `swipe right moves col to right`() {
        // Сдвигаем finger вправо на 2 ширины cell — должны попасть в col=2
        val idx = AccentPickerHitTest.computeHoveredIdx(
            accentsCount = 6,
            fingerLocalX = keyWidth / 2f + 2f * cellW,  // shifted right by 2 cells
            fingerLocalY = 0f,
            downLocalY = 0f,
            keyRootX = keyRootX,
            popupLeftX = popupLeftX,
            cellWidthPx = cellW,
            padPx = padPx,
            upThresholdPx = upThreshold,
        )
        // bottom row, col=2 → flat = 1*3+2 = 5 (last)
        assertEquals(5, idx)
    }

    @Test
    fun `swipe left clamps to col 0`() {
        // Очень далеко влево — coerced до col=0
        val idx = AccentPickerHitTest.computeHoveredIdx(
            accentsCount = 6,
            fingerLocalX = -500f,
            fingerLocalY = 0f,
            downLocalY = 0f,
            keyRootX = keyRootX,
            popupLeftX = popupLeftX,
            cellWidthPx = cellW,
            padPx = padPx,
            upThresholdPx = upThreshold,
        )
        // bottom row, col=0 → flat = 3 (4th accent)
        assertEquals(3, idx)
    }

    @Test
    fun `swipe right clamps to last col`() {
        // Очень далеко вправо — coerced до cellsPerRow-1 = 2
        val idx = AccentPickerHitTest.computeHoveredIdx(
            accentsCount = 6,
            fingerLocalX = 5000f,
            fingerLocalY = 0f,
            downLocalY = 0f,
            keyRootX = keyRootX,
            popupLeftX = popupLeftX,
            cellWidthPx = cellW,
            padPx = padPx,
            upThresholdPx = upThreshold,
        )
        assertEquals(5, idx)
    }

    @Test
    fun `swipe up-right combo lands in top-right`() {
        val idx = AccentPickerHitTest.computeHoveredIdx(
            accentsCount = 6,
            fingerLocalX = keyWidth / 2f + 2f * cellW,
            fingerLocalY = -30f,  // past threshold
            downLocalY = 0f,
            keyRootX = keyRootX,
            popupLeftX = popupLeftX,
            cellWidthPx = cellW,
            padPx = padPx,
            upThresholdPx = upThreshold,
        )
        // top row col 2 → flat = 0*3+2 = 2
        assertEquals(2, idx)
    }

    @Test
    fun `5 accents — bottom row has 2 cells, accessible`() {
        // 5 accents → 2 rows, cellsPerRow=3, top=3, bottom=2
        // Bottom row последняя позиция должна быть idx=4
        val idx = AccentPickerHitTest.computeHoveredIdx(
            accentsCount = 5,
            fingerLocalX = keyWidth / 2f + 2f * cellW,  // far right
            fingerLocalY = 0f,
            downLocalY = 0f,
            keyRootX = keyRootX,
            popupLeftX = popupLeftX,
            cellWidthPx = cellW,
            padPx = padPx,
            upThresholdPx = upThreshold,
        )
        // bottom row col 2 → flat = 1*3+2 = 5, coerced to 4 (last)
        assertEquals(4, idx)
    }

    @Test
    fun `4 accents — 2 rows × 2 cells each`() {
        // 4 accents → rowsCount=2 (>3), cellsPerRow = (4+1)/2 = 2
        // Top has 2 cells, bottom has 2 cells.
        val idx = AccentPickerHitTest.computeHoveredIdx(
            accentsCount = 4,
            fingerLocalX = keyWidth / 2f + cellW,  // shift right by 1 cell
            fingerLocalY = 0f,
            downLocalY = 0f,
            keyRootX = keyRootX,
            popupLeftX = popupLeftX,
            cellWidthPx = cellW,
            padPx = padPx,
            upThresholdPx = upThreshold,
        )
        // bottom row, col 1 → flat = 1*2+1 = 3 (last)
        assertEquals(3, idx)
    }

    @Test
    fun `2 accents — single row of 2 cells`() {
        // 2 accents → 1 row. rowIdx always 0. col either 0 or 1.
        val idx = AccentPickerHitTest.computeHoveredIdx(
            accentsCount = 2,
            fingerLocalX = keyWidth / 2f + cellW,
            fingerLocalY = -25f,  // even if up-swipe, no top row exists
            downLocalY = 0f,
            keyRootX = keyRootX,
            popupLeftX = popupLeftX,
            cellWidthPx = cellW,
            padPx = padPx,
            upThresholdPx = upThreshold,
        )
        // rowIdx forced to 0 (rowsCount=1), col=1 → flat = 1
        assertEquals(1, idx)
    }

    @Test
    fun `1 accent — always returns 0`() {
        val idx = AccentPickerHitTest.computeHoveredIdx(
            accentsCount = 1,
            fingerLocalX = 999f,  // any position
            fingerLocalY = -999f,
            downLocalY = 0f,
            keyRootX = keyRootX,
            popupLeftX = popupLeftX,
            cellWidthPx = cellW,
            padPx = padPx,
            upThresholdPx = upThreshold,
        )
        assertEquals(0, idx)
    }

    @Test
    fun `0 accents — returns 0 safely`() {
        val idx = AccentPickerHitTest.computeHoveredIdx(
            accentsCount = 0,
            fingerLocalX = 0f,
            fingerLocalY = 0f,
            downLocalY = 0f,
            keyRootX = keyRootX,
            popupLeftX = popupLeftX,
            cellWidthPx = cellW,
            padPx = padPx,
            upThresholdPx = upThreshold,
        )
        assertEquals(0, idx)
    }

    @Test
    fun `threshold boundary — dy exactly at -20 stays in bottom`() {
        // Boundary: dy < -upThreshold (strict). dy = -20 не меньше -20 → bottom
        val idx = AccentPickerHitTest.computeHoveredIdx(
            accentsCount = 6,
            fingerLocalX = keyWidth / 2f,
            fingerLocalY = -20f,  // exactly at threshold
            downLocalY = 0f,
            keyRootX = keyRootX,
            popupLeftX = popupLeftX,
            cellWidthPx = cellW,
            padPx = padPx,
            upThresholdPx = upThreshold,
        )
        assertEquals(4, idx)  // bottom row (not switched yet)
    }

    @Test
    fun `threshold boundary — dy just past -20 switches to top`() {
        val idx = AccentPickerHitTest.computeHoveredIdx(
            accentsCount = 6,
            fingerLocalX = keyWidth / 2f,
            fingerLocalY = -21f,  // just past
            downLocalY = 0f,
            keyRootX = keyRootX,
            popupLeftX = popupLeftX,
            cellWidthPx = cellW,
            padPx = padPx,
            upThresholdPx = upThreshold,
        )
        assertEquals(1, idx)  // top row middle
    }

    @Test
    fun `downward swipe stays in bottom`() {
        val idx = AccentPickerHitTest.computeHoveredIdx(
            accentsCount = 6,
            fingerLocalX = keyWidth / 2f,
            fingerLocalY = 50f,  // moving DOWN
            downLocalY = 0f,
            keyRootX = keyRootX,
            popupLeftX = popupLeftX,
            cellWidthPx = cellW,
            padPx = padPx,
            upThresholdPx = upThreshold,
        )
        assertEquals(4, idx)  // bottom row middle
    }
}
