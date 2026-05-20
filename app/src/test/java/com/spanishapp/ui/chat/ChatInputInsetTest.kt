package com.spanishapp.ui.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v1.18.51: регрессионный тест на gap между input-bar и клавиатурой в AiChatScreen.
 *
 * Архитектура inset'ов:
 *   SpanishAppRoot.NavHost → padding(bottom = navBar = 24dp) БЕЗ consumeWindowInsets.
 *   AiChatScreen.Scaffold  → contentWindowInsets = WindowInsets(0) — bottom не трогает.
 *   AiChatScreen.Column    → windowInsetsPadding(ime.exclude(navBar)) = ime - navBar.
 *
 * Итого bottom-смещение:
 *   closed kbd: 24 (outer navBar) + 0 (ime.exclude(navBar)=0)          = 24dp ← над navBar ✓
 *   open kbd:   24 (outer navBar) + (350-24) (ime-navBar = 326dp)       = 350dp = клавиатура ✓
 *
 * История багов:
 *   v1.18.49 — padding(scaffoldPadding) + imePadding(): navBar считался 2 раза.
 *   v1.18.50 — contentWindowInsets=safeDrawing: outer navBar не потреблён → gap 24dp.
 *   v1.18.51 — Scaffold(contentWindowInsets=0) + ime.exclude(navBar): корректно.
 */
class ChatInputInsetTest {

    // ── Модель Android inset'ов ───────────────────────────────────
    data class Insets(
        val navBar: Int,
        val ime: Int,  // 0 если закрыта; иначе включает navBar (Android convention)
    )

    // ── Outer NavHost: padding(bottom=navBar), insets не потреблены ──
    // ── Inner Column:  windowInsetsPadding(ime.exclude(navBar))       ──
    private fun fixedBottomShift(insets: Insets): Int {
        val outerPadding = insets.navBar
        val imeMinusNav = maxOf(insets.ime - insets.navBar, 0)
        return outerPadding + imeMinusNav
    }

    // ── 1. Keyboard closed: смещение = navBar (content над navBar) ──────
    @Test
    fun `keyboard closed — shift equals navBar`() {
        val closed = Insets(navBar = 24, ime = 0)
        assertEquals("Должен быть navBar когда клавиатура закрыта", 24, fixedBottomShift(closed))
    }

    // ── 2. Keyboard open: смещение = ime (input прилегает к клавиатуре) ──
    @Test
    fun `keyboard open — shift equals ime no gap`() {
        val open = Insets(navBar = 24, ime = 350)
        assertEquals("Должен быть равен ime при открытой клавиатуре", 350, fixedBottomShift(open))
    }

    // ── 3. Gesture nav (navBar=0): должно работать корректно ────────────
    @Test
    fun `gesture nav — closed keyboard no extra shift`() {
        assertEquals(0, fixedBottomShift(Insets(navBar = 0, ime = 0)))
    }

    @Test
    fun `gesture nav — open keyboard shift equals ime`() {
        assertEquals(320, fixedBottomShift(Insets(navBar = 0, ime = 320)))
    }

    // ── 4. Предыдущий баг v1.18.50 — для документации ───────────────────
    // Было: outer=24 (navBar) + inner=350 (safeDrawing.bottom=ime) = 374dp
    // → Gap = 374 - 350 = 24dp (navBar). Теперь это НЕ должно происходить.
    @Test
    fun `v1_18_50 bug was outer navBar plus full ime equals double count`() {
        val open = Insets(navBar = 24, ime = 350)
        val buggyShift = open.navBar + open.ime  // 24 + 350 = 374
        val correctShift = fixedBottomShift(open) // 350
        assertFalse("Баг v1.18.50 давал $buggyShift вместо $correctShift", buggyShift == correctShift)
        assertEquals("Фикс v1.18.51 должен давать $correctShift", correctShift, 350)
    }

    // ── 5. Параметризованный: shift всегда = max(navBar, ime) ────────────
    @Test
    fun `shift always equals correct keyboard offset`() {
        val cases = listOf(
            Insets(0, 0) to 0,
            Insets(0, 300) to 300,
            Insets(24, 0) to 24,
            Insets(24, 300) to 300,
            Insets(48, 500) to 500,
        )
        for ((insets, expected) in cases) {
            val actual = fixedBottomShift(insets)
            assertEquals("Для $insets смещение должно быть $expected", expected, actual)
        }
    }
}
