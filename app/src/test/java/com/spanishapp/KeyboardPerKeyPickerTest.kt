package com.spanishapp

import com.spanishapp.ui.chat.AccentPickerHitTest
import com.spanishapp.ui.chat.esAccents
import com.spanishapp.ui.chat.esLetterRows
import com.spanishapp.ui.chat.numRows
import com.spanishapp.ui.chat.ruAccents
import com.spanishapp.ui.chat.ruLetterRows
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * v1.25.55 — exhaustive per-key tests.
 * Для КАЖДОЙ конкретной клавиши в ES/RU/NUM:
 *  - Берём её реальные accents
 *  - Проверяем что КАЖДЫЙ accent reachable через legal finger gesture
 *  - Проверяем что pickerWidth fits типичный экран
 *
 * Если хоть один accent не достижим — тест fails с именем клавиши.
 */
class KeyboardPerKeyPickerTest {

    private val cellW = 48f
    private val padPx = 6f
    private val upThreshold = 20f
    private val keyWidth = 36f

    private fun cellCenterX(accentsCount: Int, col: Int): Float {
        val rowsCount = if (accentsCount > 3) 2 else 1
        val cellsPerRow = (accentsCount + rowsCount - 1) / rowsCount
        val popupWidth = cellsPerRow * cellW + 2 * padPx
        val popupLeftLocal = (keyWidth - popupWidth) / 2f
        return popupLeftLocal + padPx + col * cellW + cellW / 2f
    }

    private fun verifyReachable(layoutName: String, keyLabel: String, accents: List<String>) {
        if (accents.isEmpty()) return  // нет accents — нечего проверять
        val rowsCount = if (accents.size > 3) 2 else 1
        val cellsPerRow = (accents.size + rowsCount - 1) / rowsCount

        for (i in accents.indices) {
            val row = i / cellsPerRow
            val col = i % cellsPerRow
            // Y: для top row нужен upward swipe, для bottom row — 0
            val y = if (row == 0 && rowsCount > 1) -30f else 0f
            val hit = AccentPickerHitTest.computeHoveredIdx(
                accentsCount = accents.size,
                fingerLocalX = cellCenterX(accents.size, col),
                fingerLocalY = y,
                downLocalY = 0f,
                keyWidthPx = keyWidth,
                cellWidthPx = cellW,
                padPx = padPx,
                upThresholdPx = upThreshold,
            )
            if (hit != i) {
                fail(
                    "[$layoutName] key='$keyLabel' accents=$accents " +
                    "expected idx=$i ('${accents[i]}') row=$row col=$col, got hit=$hit ('${accents[hit]}')"
                )
            }
        }
    }

    private fun verifyPopupFits(layoutName: String, keyLabel: String, accents: List<String>) {
        if (accents.isEmpty()) return
        val rowsCount = if (accents.size > 3) 2 else 1
        val cellsPerRow = (accents.size + rowsCount - 1) / rowsCount
        val popupWidth = cellsPerRow * cellW + 2 * padPx
        // Минимальный экран — типично ~360dp для compact phone
        val minScreenWidth = 360f
        assertTrue(
            "[$layoutName] key='$keyLabel' picker ${popupWidth}dp exceeds $minScreenWidth screen",
            popupWidth <= minScreenWidth,
        )
    }

    // ────────────────────────────────────────────────
    //  ES — каждая буква
    // ────────────────────────────────────────────────
    @Test
    fun `ES — every letter accents reachable`() {
        val rows = esLetterRows()
        var totalKeys = 0
        var keysWithAccents = 0
        for (row in rows) {
            for (key in row) {
                totalKeys++
                val accents = esAccents(key.label)
                if (accents.isNotEmpty()) {
                    keysWithAccents++
                    verifyReachable("ES", key.label, accents)
                    verifyPopupFits("ES", key.label, accents)
                }
            }
        }
        println("ES: $totalKeys keys total, $keysWithAccents with accents")
        assertTrue("ES should have >20 letter keys", totalKeys >= 20)
    }

    // ────────────────────────────────────────────────
    //  RU — каждая буква
    // ────────────────────────────────────────────────
    @Test
    fun `RU — every letter accents reachable`() {
        val rows = ruLetterRows()
        var totalKeys = 0
        var keysWithAccents = 0
        for (row in rows) {
            for (key in row) {
                totalKeys++
                val accents = ruAccents(key.label)
                if (accents.isNotEmpty()) {
                    keysWithAccents++
                    verifyReachable("RU", key.label, accents)
                    verifyPopupFits("RU", key.label, accents)
                }
            }
        }
        println("RU: $totalKeys keys total, $keysWithAccents with accents")
        assertEquals("RU mobile layout = 31 letters (11+11+9)", 31, totalKeys)
    }

    // ────────────────────────────────────────────────
    //  NUM — каждая цифра/символ
    // ────────────────────────────────────────────────
    @Test
    fun `NUM — every key accents reachable`() {
        val rows = numRows()
        var totalKeys = 0
        var keysWithAccents = 0
        for (row in rows) {
            for (key in row) {
                totalKeys++
                if (key.accents.isNotEmpty()) {
                    keysWithAccents++
                    verifyReachable("NUM", key.label, key.accents)
                    verifyPopupFits("NUM", key.label, key.accents)
                }
            }
        }
        println("NUM: $totalKeys keys total, $keysWithAccents with accents")
        assertTrue("NUM should have ≥20 keys", totalKeys >= 20)
    }

    // ────────────────────────────────────────────────
    //  TOTAL COUNT — verify покрытие
    // ────────────────────────────────────────────────
    @Test
    fun `total keys covered across all layouts`() {
        val esCount = esLetterRows().sumOf { it.size }
        val ruCount = ruLetterRows().sumOf { it.size }
        val numCount = numRows().sumOf { it.size }
        val total = esCount + ruCount + numCount
        println("TOTAL keys tested: ES=$esCount + RU=$ruCount + NUM=$numCount = $total")
        assertTrue("Should cover >60 keys total", total >= 60)
    }

    // ────────────────────────────────────────────────
    //  PER-KEY DETAIL — каждая клавиша как отдельный sub-test
    //  (печатает имена в pass/fail для clarity)
    // ────────────────────────────────────────────────
    @Test
    fun `ES detail — explicit per-letter coverage`() {
        val keysToCheck = listOf("q","w","e","r","t","y","u","i","o","p",
                                  "a","s","d","f","g","h","j","k","l","ñ",
                                  "z","x","c","v","b","n","m")
        for (letter in keysToCheck) {
            val accents = esAccents(letter)
            verifyReachable("ES", letter, accents)
        }
    }

    @Test
    fun `RU detail — explicit per-letter coverage`() {
        val keysToCheck = listOf("й","ц","у","к","е","н","г","ш","щ","з","х",
                                  "ф","ы","в","а","п","р","о","л","д","ж","э",
                                  "я","ч","с","м","и","т","ь","б","ю")
        for (letter in keysToCheck) {
            val accents = ruAccents(letter)
            verifyReachable("RU", letter, accents)
        }
    }
}
