package com.spanishapp

import com.spanishapp.ui.games.extractWordAt
import com.spanishapp.ui.games.extractSentenceAt
import org.junit.Assert.*
import org.junit.Test

class LibroTextHelpersTest {

    // ── extractWordAt ─────────────────────────────────────────

    @Test fun `начало слова`()        { assertEquals("hola",  extractWordAt("hola mundo", 0)) }
    @Test fun `середина слова`()      { assertEquals("hola",  extractWordAt("hola mundo", 2)) }
    @Test fun `конец слова`()         { assertEquals("hola",  extractWordAt("hola mundo", 3)) }
    @Test fun `второе слово`()        { assertEquals("mundo", extractWordAt("hola mundo", 5)) }
    @Test fun `пробел — пусто`()      { assertEquals("",      extractWordAt("hola mundo", 4)) }
    @Test fun `запятая — пусто`()     { assertEquals("",      extractWordAt("hola, mundo", 4)) }
    @Test fun `отрицательный offset`(){ assertEquals("",      extractWordAt("hola", -1)) }
    @Test fun `offset за длиной`()    { assertEquals("",      extractWordAt("hola", 10)) }
    @Test fun `одно слово`()          { assertEquals("Juan",  extractWordAt("Juan", 2)) }
    @Test fun `последнее слово`()     { assertEquals("Juan",  extractWordAt("yo soy Juan", 9)) }

    @Test fun `слово с акцентом`() {
        // "María" — позиция 3 = буква 'í', ожидаем слово целиком
        assertEquals("María", extractWordAt("María está aquí", 3))
    }

    @Test fun `пустой текст`() {
        assertEquals("", extractWordAt("", 0))
    }

    // ── extractSentenceAt ─────────────────────────────────────

    @Test fun `одно предложение`() {
        assertEquals("Hola mundo.", extractSentenceAt("Hola mundo.", 5))
    }

    @Test fun `первое из двух`() {
        assertEquals("Soy Juan.", extractSentenceAt("Soy Juan. Me llamo Juan.", 3))
    }

    @Test fun `второе из двух`() {
        assertEquals("Me llamo Juan.", extractSentenceAt("Soy Juan. Me llamo Juan.", 12))
    }

    @Test fun `пустой текст sentence`() {
        assertEquals("", extractSentenceAt("", 0))
    }

    @Test fun `разделитель новая строка`() {
        assertEquals("Primera línea", extractSentenceAt("Primera línea\nSegunda línea", 5))
    }

    // ── Интеграция: сценарий нажатия ─────────────────────────

    @Test fun `нажал на Juan в середине текста`() {
        val text = "Me llamo Juan. Tengo veinte años."
        val offset = 9  // позиция 'J' в "Juan"
        assertEquals("Juan",         extractWordAt(text, offset))
        assertEquals("Me llamo Juan.", extractSentenceAt(text, offset))
    }

    @Test fun `нажал на знак — слово пустое`() {
        val text = "Hola, mundo."
        assertEquals("", extractWordAt(text, 4))  // запятая
    }

    @Test fun `слово в конце предложения`() {
        val text = "Tengo veinte años."
        assertEquals("años", extractWordAt(text, 13))
    }
}
