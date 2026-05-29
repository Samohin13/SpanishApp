package com.spanishapp.ui.chat

/**
 * v1.25.53 — pure-function hit-test для accent picker'а.
 *
 * Извлечён из KeyButton.pointerInput для unit-тестирования.
 *
 * Алгоритм:
 *  - ROW: gesture-relative от точки down. dy < -upThreshold → top row,
 *    иначе bottom row. (Только если accentsCount > 3, иначе 1 row.)
 *  - COL: absolute finger x относительно popup-cells.
 *  - flatIdx = rowIdx * cellsPerRow + colIdx, coerced в [0, count-1].
 *
 * Координаты:
 *  - finger local — относительно KeyButton (где сработал DOWN)
 *  - keyRootX — absolute X положение KeyButton в окне
 *  - popupLeftX — absolute X левого края popup
 */
object AccentPickerHitTest {

    fun computeHoveredIdx(
        accentsCount: Int,
        fingerLocalX: Float,
        fingerLocalY: Float,
        downLocalY: Float,
        keyRootX: Float,
        popupLeftX: Float,
        cellWidthPx: Float,
        padPx: Float,
        upThresholdPx: Float,
    ): Int {
        if (accentsCount <= 0) return 0

        val rowsCount = if (accentsCount > 3) 2 else 1
        val cellsPerRow = (accentsCount + rowsCount - 1) / rowsCount

        // ROW: dy от точки нажатия. upward swipe → top row.
        val dy = fingerLocalY - downLocalY
        val rowIdx = if (rowsCount > 1 && dy < -upThresholdPx) 0
                     else (rowsCount - 1)

        // COL: absolute finger x → relative к popup → cell index
        val fingerRootX = fingerLocalX + keyRootX
        val relX = fingerRootX - popupLeftX - padPx
        val colIdx = (relX / cellWidthPx).toInt().coerceIn(0, cellsPerRow - 1)

        return (rowIdx * cellsPerRow + colIdx).coerceIn(0, accentsCount - 1)
    }
}
