package com.spanishapp

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.spanishapp.ui.chat.KeyboardLogic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit-тесты для всех pure-функций SpanishKeyboard.
 * Покрывают каждую логическую операцию: вставку, удаление, движение курсора,
 * auto-cap, shift, раскладки. Никакого Compose runtime не требуется.
 */
class KeyboardLogicTest {

    // ── insertAt ────────────────────────────────────────────────

    @Test fun `insertAt в пустую строку`() {
        val v = TextFieldValue("", TextRange(0))
        val r = KeyboardLogic.insertAt(v, "a")
        assertEquals("a", r.text)
        assertEquals(1, r.selection.start)
    }

    @Test fun `insertAt в конец`() {
        val v = TextFieldValue("hola", TextRange(4))
        val r = KeyboardLogic.insertAt(v, "!")
        assertEquals("hola!", r.text)
        assertEquals(5, r.selection.start)
    }

    @Test fun `insertAt в начало`() {
        val v = TextFieldValue("ola", TextRange(0))
        val r = KeyboardLogic.insertAt(v, "h")
        assertEquals("hola", r.text)
        assertEquals(1, r.selection.start)
    }

    @Test fun `insertAt в середину`() {
        val v = TextFieldValue("hla", TextRange(1))
        val r = KeyboardLogic.insertAt(v, "o")
        assertEquals("hola", r.text)
        assertEquals(2, r.selection.start)
    }

    @Test fun `insertAt заменяет выделение`() {
        val v = TextFieldValue("buenos días", TextRange(7, 11))  // выделено "días"
        val r = KeyboardLogic.insertAt(v, "noches")
        assertEquals("buenos noches", r.text)
        assertEquals(13, r.selection.start)
    }

    @Test fun `insertAt многобайтовый испанский символ`() {
        val v = TextFieldValue("ano", TextRange(1))
        val r = KeyboardLogic.insertAt(v, "ñ")
        assertEquals("añno", r.text)
    }

    @Test fun `insertAt многосимвольную подсказку`() {
        val v = TextFieldValue("ho", TextRange(2))
        val r = KeyboardLogic.insertAt(v, "la ")
        assertEquals("hola ", r.text)
        assertEquals(5, r.selection.start)
    }

    // ── backspaceChar ───────────────────────────────────────────

    @Test fun `backspaceChar в пустой строке - no-op`() {
        val v = TextFieldValue("", TextRange(0))
        val r = KeyboardLogic.backspaceChar(v)
        assertEquals("", r.text)
    }

    @Test fun `backspaceChar в начале - no-op`() {
        val v = TextFieldValue("hola", TextRange(0))
        val r = KeyboardLogic.backspaceChar(v)
        assertEquals("hola", r.text)
    }

    @Test fun `backspaceChar в конце`() {
        val v = TextFieldValue("hola", TextRange(4))
        val r = KeyboardLogic.backspaceChar(v)
        assertEquals("hol", r.text)
        assertEquals(3, r.selection.start)
    }

    @Test fun `backspaceChar в середине`() {
        val v = TextFieldValue("hola", TextRange(2))
        val r = KeyboardLogic.backspaceChar(v)
        assertEquals("hla", r.text)
        assertEquals(1, r.selection.start)
    }

    @Test fun `backspaceChar удаляет выделение`() {
        val v = TextFieldValue("hola", TextRange(1, 3))  // выделено "ol"
        val r = KeyboardLogic.backspaceChar(v)
        assertEquals("ha", r.text)
        assertEquals(1, r.selection.start)
    }

    @Test fun `backspaceChar испанский диакрит`() {
        val v = TextFieldValue("café", TextRange(4))
        val r = KeyboardLogic.backspaceChar(v)
        assertEquals("caf", r.text)
    }

    // ── moveCursor ──────────────────────────────────────────────

    @Test fun `moveCursor вправо`() {
        val v = TextFieldValue("hola", TextRange(0))
        val r = KeyboardLogic.moveCursor(v, 2)
        assertEquals(2, r.selection.start)
    }

    @Test fun `moveCursor влево`() {
        val v = TextFieldValue("hola", TextRange(4))
        val r = KeyboardLogic.moveCursor(v, -2)
        assertEquals(2, r.selection.start)
    }

    @Test fun `moveCursor clamp в начале`() {
        val v = TextFieldValue("hola", TextRange(1))
        val r = KeyboardLogic.moveCursor(v, -5)
        assertEquals(0, r.selection.start)
    }

    @Test fun `moveCursor clamp в конце`() {
        val v = TextFieldValue("hola", TextRange(2))
        val r = KeyboardLogic.moveCursor(v, 10)
        assertEquals(4, r.selection.start)  // length=4
    }

    @Test fun `moveCursor не меняет текст`() {
        val v = TextFieldValue("hola", TextRange(2))
        val r = KeyboardLogic.moveCursor(v, 1)
        assertEquals("hola", r.text)
    }

    // ── shouldAutoCapAfter ──────────────────────────────────────

    @Test fun `autoCap в начале текста`() {
        assertTrue(KeyboardLogic.shouldAutoCapAfter("", 0))
    }

    @Test fun `autoCap после первой буквы`() {
        assertFalse(KeyboardLogic.shouldAutoCapAfter("h", 1))
    }

    @Test fun `autoCap после точки и пробела`() {
        assertTrue(KeyboardLogic.shouldAutoCapAfter("Hola. ", 6))
    }

    @Test fun `autoCap после восклицательного и пробела`() {
        assertTrue(KeyboardLogic.shouldAutoCapAfter("Hola! ", 6))
    }

    @Test fun `autoCap после вопросительного и пробела`() {
        assertTrue(KeyboardLogic.shouldAutoCapAfter("¿Cómo? ", 7))
    }

    @Test fun `autoCap НЕ после запятой и пробела`() {
        assertFalse(KeyboardLogic.shouldAutoCapAfter("hola, ", 6))
    }

    @Test fun `autoCap НЕ после буквы и пробела`() {
        assertFalse(KeyboardLogic.shouldAutoCapAfter("hola ", 5))
    }

    @Test fun `autoCap НЕ после двух пробелов`() {
        assertFalse(KeyboardLogic.shouldAutoCapAfter("a  ", 3))
    }

    // ── applyShift ──────────────────────────────────────────────

    @Test fun `applyShift с shifted=true делает верхний регистр`() {
        assertEquals("A", KeyboardLogic.applyShift("a", shifted = true, capsLock = false, isNumericLayout = false))
    }

    @Test fun `applyShift с shifted=false оставляет нижний`() {
        assertEquals("a", KeyboardLogic.applyShift("a", shifted = false, capsLock = false, isNumericLayout = false))
    }

    @Test fun `applyShift с capsLock=true делает верхний`() {
        assertEquals("B", KeyboardLogic.applyShift("b", shifted = false, capsLock = true, isNumericLayout = false))
    }

    @Test fun `applyShift НЕ работает на NUM слое`() {
        assertEquals("1", KeyboardLogic.applyShift("1", shifted = true, capsLock = true, isNumericLayout = true))
        assertEquals("@", KeyboardLogic.applyShift("@", shifted = true, capsLock = false, isNumericLayout = true))
    }

    @Test fun `applyShift с ñ испанской`() {
        assertEquals("Ñ", KeyboardLogic.applyShift("ñ", shifted = true, capsLock = false, isNumericLayout = false))
    }

    @Test fun `applyShift с русской буквой`() {
        assertEquals("П", KeyboardLogic.applyShift("п", shifted = true, capsLock = false, isNumericLayout = false))
    }

    // ── Раскладка ES ────────────────────────────────────────────

    @Test fun `ES раскладка - первый ряд 10 клавиш qwerty`() {
        assertEquals(listOf("q","w","e","r","t","y","u","i","o","p"), KeyboardLogic.esRows()[0])
    }

    @Test fun `ES раскладка - второй ряд содержит ñ`() {
        val row = KeyboardLogic.esRows()[1]
        assertEquals(10, row.size)
        assertTrue("ñ должна быть в ES раскладке", row.contains("ñ"))
    }

    @Test fun `ES раскладка - третий ряд 7 клавиш`() {
        assertEquals(7, KeyboardLogic.esRows()[2].size)
    }

    @Test fun `ES раскладка - все 26 английских + ñ`() {
        val all = KeyboardLogic.esRows().flatten()
        val expected = ("abcdefghijklmnopqrstuvwxyz").map { it.toString() } + "ñ"
        expected.forEach {
            assertTrue("Буква '$it' должна быть в ES раскладке", all.contains(it))
        }
        assertEquals(27, all.size)  // 26 латинских + ñ
    }

    // ── Раскладка RU ────────────────────────────────────────────

    @Test fun `RU раскладка - 33 буквы кроме ё`() {
        // ё обычно отдельная клавиша, не на основной раскладке мобильной клавы
        val all = KeyboardLogic.ruRows().flatten()
        val expected = "йцукенгшщзхъфывапролджэячсмитьбю".map { it.toString() }
        expected.forEach {
            assertTrue("Буква '$it' должна быть в RU", all.contains(it))
        }
    }

    @Test fun `RU раскладка - первый ряд 12 клавиш`() {
        assertEquals(12, KeyboardLogic.ruRows()[0].size)
    }

    @Test fun `RU раскладка - второй ряд 11 клавиш`() {
        assertEquals(11, KeyboardLogic.ruRows()[1].size)
    }

    @Test fun `RU раскладка - третий ряд 9 клавиш`() {
        assertEquals(9, KeyboardLogic.ruRows()[2].size)
    }

    // ── Раскладка NUM ───────────────────────────────────────────

    @Test fun `NUM первый ряд - цифры 1 до 0`() {
        assertEquals(listOf("1","2","3","4","5","6","7","8","9","0"), KeyboardLogic.numRows()[0])
    }

    @Test fun `NUM содержит ¿ и ¡`() {
        val all = KeyboardLogic.numRows().flatten()
        assertTrue("¿ должна быть", all.contains("¿"))
        assertTrue("¡ должна быть", all.contains("¡"))
    }

    @Test fun `NUM содержит знаки препинания`() {
        val all = KeyboardLogic.numRows().flatten()
        listOf(".", ",", "!", "?", ":", ";", "(", ")", "@", "#", "&", "*").let {
            // не все обязательны на раскладке но хотя бы половина
            val present = it.count { sym -> all.contains(sym) }
            assertTrue("Должно быть много знаков препинания, нашли $present из ${it.size}",
                present >= 8)
        }
    }

    // ── Акценты ─────────────────────────────────────────────────

    @Test fun `Акценты для a`() {
        assertEquals(listOf("á","à","ä","â"), KeyboardLogic.esAccents("a"))
    }

    @Test fun `Акценты для e`() {
        assertEquals(listOf("é","è","ë","ê"), KeyboardLogic.esAccents("e"))
    }

    @Test fun `Акценты для i o u`() {
        assertTrue(KeyboardLogic.esAccents("i").contains("í"))
        assertTrue(KeyboardLogic.esAccents("o").contains("ó"))
        assertTrue(KeyboardLogic.esAccents("u").contains("ú"))
        assertTrue("Должна быть ü (диерезис)", KeyboardLogic.esAccents("u").contains("ü"))
    }

    @Test fun `Акценты для n`() {
        assertEquals(listOf("ñ"), KeyboardLogic.esAccents("n"))
    }

    @Test fun `Акценты для других букв - пусто`() {
        assertTrue(KeyboardLogic.esAccents("b").isEmpty())
        assertTrue(KeyboardLogic.esAccents("z").isEmpty())
        assertTrue(KeyboardLogic.esAccents("q").isEmpty())
    }

    // ── Симуляция полного flow печати ──────────────────────────

    @Test fun `Симуляция - быстрый ввод hola без stale closures`() {
        // Имитируем рапид-фир печати, как делает реальный юзер.
        // Это и был root-cause bug v1.24.9: после 3 букв печать стопилась.
        var value = TextFieldValue("", TextRange(0))
        var shifted = true  // auto-cap at start
        val capsLock = false
        val isNum = false

        fun press(letter: String) {
            val shifted_str = KeyboardLogic.applyShift(letter, shifted, capsLock, isNum)
            value = KeyboardLogic.insertAt(value, shifted_str)
            if (shifted && !capsLock) shifted = false
            if (KeyboardLogic.shouldAutoCapAfter(value.text, value.selection.start)) shifted = true
        }

        press("h")
        press("o")
        press("l")
        press("a")

        assertEquals("Hola", value.text)
        assertEquals(4, value.selection.start)
    }

    @Test fun `Симуляция - 2 предложения с auto-cap`() {
        var value = TextFieldValue("", TextRange(0))
        var shifted = true

        fun press(letter: String) {
            val s = KeyboardLogic.applyShift(letter, shifted, false, false)
            value = KeyboardLogic.insertAt(value, s)
            if (shifted) shifted = false
            if (KeyboardLogic.shouldAutoCapAfter(value.text, value.selection.start)) shifted = true
        }

        listOf("h","o","l","a","."," ").forEach { press(it) }
        listOf("a","d","i","o","s").forEach { press(it) }

        // После ". " auto-cap включился → "Adios" с заглавной
        assertEquals("Hola. Adios", value.text)
    }

    @Test fun `Симуляция - удаление зажатием`() {
        var value = TextFieldValue("hola amigo", TextRange(10))
        repeat(5) { value = KeyboardLogic.backspaceChar(value) }
        assertEquals("hola ", value.text)
        assertEquals(5, value.selection.start)
    }

    @Test fun `Симуляция - swipe курсора влево и вставка в середину`() {
        var value = TextFieldValue("hla", TextRange(3))
        // swipe назад на 2 позиции
        value = KeyboardLogic.moveCursor(value, -2)
        assertEquals(1, value.selection.start)
        // вставить o
        value = KeyboardLogic.insertAt(value, "o")
        assertEquals("hola", value.text)
    }
}
