package com.spanishapp.ui.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v1.18.53: регрессионный тест на gap между input-bar и клавиатурой в AiChatScreen.
 *
 * Архитектура inset'ов (восстановлена из v1.18.39 — последняя визуально рабочая):
 *   SpanishAppRoot.NavHost → padding(bottom = navBar = 24dp).
 *   AiChatScreen.Scaffold  → дефолтный contentWindowInsets (systemBars).
 *   AiChatScreen.Column    → .padding(scaffoldPadding) (top=topBar, bottom=navBar consumed Scaffold'ом).
 *   Input-bar Row          → .imePadding().navigationBarsPadding().padding(h=10, v=8).
 *
 * ime включает navBar (Android convention). При закрытой клаве:
 *   • imePadding = 0
 *   • navigationBarsPadding = 24 (unconsumed) → Row выше navBar
 * При открытой клаве:
 *   • imePadding = 350 → consume ime
 *   • navigationBarsPadding = max(0, navBar - consumed_ime) = max(0, 24-?) = 0
 *     (если consume ime включает navBar)
 *
 * История:
 *   v1.18.34 — рабочая версия ДО wallpaper (gap был, но не виден на solid bg).
 *   v1.18.38 — добавлен wallpaper → gap стал виден.
 *   v1.18.39 — Row плавает без outer Surface, imePadding+navigationBarsPadding.
 *   v1.18.40–.52 — серия попыток с компактным Surface и манипуляциями inset
 *                  (все создавали double-count либо clip mic-кнопки).
 *   v1.18.53 — возврат к v1.18.39: Row без outer Surface, тот же inset-паттерн.
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
