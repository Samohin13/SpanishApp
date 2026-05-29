package com.spanishapp.ui.chat

/**
 * v1.25.54 — pure hit-test для accent picker'а.
 * Координаты ТОЛЬКО local (от KeyButton). Не зависит от popupRect (раньше
 * onGloballyPositioned race'илось с pointer events → застревал на cells).
 *
 * Predпосылки:
 *  - Popup всегда визуально центрирован над key (cap 6 accents = ≤296dp wide,
 *    fits любой phone screen)
 *  - Popup top = key.top - popupHeight - 4dp gap
 *  - Cells layout: 6dp padding + cells + 6dp gap между rows + cells + 6dp padding
 *
 * Алгоритм:
 *  - ROW: dy < -upThreshold → top, иначе bottom (для multi-row)
 *  - COL: finger.x relative к popup-cells (popup centered над key)
 */
object AccentPickerHitTest {

    fun computeHoveredIdx(
        accentsCount: Int,
        fingerLocalX: Float,
        fingerLocalY: Float,
        downLocalY: Float,
        keyWidthPx: Float,
        cellWidthPx: Float,
        padPx: Float,
        upThresholdPx: Float,
    ): Int {
        if (accentsCount <= 0) return 0

        val rowsCount = if (accentsCount > 3) 2 else 1
        val cellsPerRow = (accentsCount + rowsCount - 1) / rowsCount

        // Popup width = N cells × cellW + 2 padding (между cells gap baked в cellW=48 для 44dp+4)
        val popupWidth = cellsPerRow * cellWidthPx + 2f * padPx
        // Popup centered над key: его left edge в local coords:
        val popupLeftLocal = (keyWidthPx - popupWidth) / 2f

        // ROW: dy от точки down (gesture-relative)
        val dy = fingerLocalY - downLocalY
        val rowIdx = if (rowsCount > 1 && dy < -upThresholdPx) 0
                     else (rowsCount - 1)

        // COL: finger x relative к popup-cells
        val relX = fingerLocalX - popupLeftLocal - padPx
        val colIdx = (relX / cellWidthPx).toInt().coerceIn(0, cellsPerRow - 1)

        return (rowIdx * cellsPerRow + colIdx).coerceIn(0, accentsCount - 1)
    }
}
