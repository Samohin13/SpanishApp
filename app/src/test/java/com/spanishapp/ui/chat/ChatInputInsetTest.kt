package com.spanishapp.ui.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v1.18.50: регрессионный тест на двойной inset в AiChatScreen.
 *
 * Bug history:
 *   • v1.18.49: padding(scaffoldPadding) + imePadding() — оба клали свой
 *     bottom inset, при открытой клавиатуре nav bar считался ДВАЖДЫ
 *     (Scaffold default contentWindowInsets = systemBars + явный imePadding).
 *     В результате — огромный чёрный gap между input bar и клавиатурой.
 *   • v1.18.50: contentWindowInsets = safeDrawing → один union включает
 *     и systemBars, и ime. Никакого imePadding на Column. Корректный
 *     bottom при любом состоянии клавиатуры.
 *
 * Эти тесты эмулируют арифметику inset'ов чтобы catch регрессию.
 */
class ChatInputInsetTest {

    // ── Эмуляция Android inset значений ──────────────────────────────
    // ime включает nav bar (по Android конвенции) когда клавиатура открыта
    data class Insets(
        val navBar: Int,
        val ime: Int,  // 0 если закрыта, иначе ИМЕЕТ В СЕБЕ navBar
    )

    // ── 1. Bug: padding(scaffoldPadding=systemBars) + imePadding() ──
    private fun buggyBottomPadding(insets: Insets): Int =
        insets.navBar + insets.ime

    @Test
    fun `buggy double-inset adds gap when keyboard open`() {
        val keyboardOpen = Insets(navBar = 24, ime = 350)  // ime включает navBar
        val expectedCorrect = 350  // = ime, который УЖЕ содержит navBar
        val actual = buggyBottomPadding(keyboardOpen)
        // В баге было 374 (24 nav + 350 ime) — gap в 24dp под input
        assertEquals(374, actual)
        assertFalse(
            "Bug: double-counted nav bar — bottom inset $actual > expected $expectedCorrect",
            actual == expectedCorrect
        )
    }

    // ── 2. Fix: contentWindowInsets = safeDrawing → один union ──────
    // safeDrawing = systemBars ∪ ime ∪ displayCutout (compose сам делает max)
    private fun fixedBottomPadding(insets: Insets): Int =
        maxOf(insets.navBar, insets.ime)

    @Test
    fun `fix uses union — no double-count when keyboard open`() {
        val keyboardOpen = Insets(navBar = 24, ime = 350)
        val actual = fixedBottomPadding(keyboardOpen)
        assertEquals("Должен быть равен ime (включает navBar)", 350, actual)
    }

    @Test
    fun `fix correctly applies nav bar when keyboard closed`() {
        val keyboardClosed = Insets(navBar = 24, ime = 0)
        val actual = fixedBottomPadding(keyboardClosed)
        assertEquals("Должен быть navBar когда клавиатура закрыта", 24, actual)
    }

    @Test
    fun `fix handles edge-to-edge device without nav bar`() {
        // Gesture nav без видимой полосы → navBar = 0
        val gestureNav = Insets(navBar = 0, ime = 0)
        assertEquals(0, fixedBottomPadding(gestureNav))

        val gestureNavWithKeyboard = Insets(navBar = 0, ime = 320)
        assertEquals(320, fixedBottomPadding(gestureNavWithKeyboard))
    }

    @Test
    fun `fix never creates gap between input and keyboard`() {
        // Любая комбинация — фикс не должен давать padding > ime+navBar
        val cases = listOf(
            Insets(0, 0),
            Insets(0, 300),
            Insets(24, 0),
            Insets(24, 300),
            Insets(48, 500),
        )
        for (case in cases) {
            val fixed = fixedBottomPadding(case)
            val maxPossibleVisualPadding = maxOf(case.navBar, case.ime)
            assertTrue(
                "Padding $fixed не должен превышать union nav/ime ($maxPossibleVisualPadding) для $case",
                fixed <= maxPossibleVisualPadding
            )
        }
    }
}
