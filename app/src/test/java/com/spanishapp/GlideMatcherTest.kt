package com.spanishapp

import com.spanishapp.ui.chat.GlideMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Тесты для glide-typing матчинга.
 */
class GlideMatcherTest {

    private val testDict = listOf(
        "hola", "hello", "hey", "amigo", "amiga", "casa", "comer", "como",
        "comprar", "buenos", "días", "noches", "tardes", "gracias", "por",
        "favor", "привет", "пока", "спасибо", "пожалуйста", "доброе",
    )

    // ── levenshtein ──

    @Test fun `levenshtein - одинаковые строки = 0`() {
        assertEquals(0, GlideMatcher.levenshtein("hola", "hola"))
    }

    @Test fun `levenshtein - одно изменение`() {
        assertEquals(1, GlideMatcher.levenshtein("hola", "hila"))
    }

    @Test fun `levenshtein - один пропуск`() {
        assertEquals(1, GlideMatcher.levenshtein("hla", "hola"))
    }

    @Test fun `levenshtein - пустая vs строка`() {
        assertEquals(4, GlideMatcher.levenshtein("", "hola"))
        assertEquals(4, GlideMatcher.levenshtein("hola", ""))
    }

    // ── dedupeConsecutive ──

    @Test fun `dedupe - убирает повторы`() {
        assertEquals(
            listOf('h', 'o', 'l', 'a'),
            GlideMatcher.dedupeConsecutive(listOf('h', 'h', 'o', 'o', 'l', 'l', 'l', 'a')),
        )
    }

    @Test fun `dedupe - без повторов = identity`() {
        assertEquals(
            listOf('a', 'b', 'c'),
            GlideMatcher.dedupeConsecutive(listOf('a', 'b', 'c')),
        )
    }

    @Test fun `dedupe - пустой = пустой`() {
        assertEquals(emptyList<Char>(), GlideMatcher.dedupeConsecutive(emptyList()))
    }

    // ── matchBestWord ──

    @Test fun `match - точная последовательность hola`() {
        val r = GlideMatcher.matchBestWord(
            traceLetters = listOf('h', 'o', 'l', 'a'),
            dictionary = testDict,
        )
        assertEquals("hola", r)
    }

    @Test fun `match - с пропуском буквы hla → hola`() {
        val r = GlideMatcher.matchBestWord(
            traceLetters = listOf('h', 'l', 'a'),
            dictionary = testDict,
        )
        // hla → hola (одна вставка)
        assertEquals("hola", r)
    }

    @Test fun `match - amigo с trace amig`() {
        val r = GlideMatcher.matchBestWord(
            traceLetters = listOf('a', 'm', 'i', 'g', 'o'),
            dictionary = testDict,
        )
        assertEquals("amigo", r)
    }

    @Test fun `match - короткий trace - null`() {
        assertNull(GlideMatcher.matchBestWord(listOf('h'), testDict))
        assertNull(GlideMatcher.matchBestWord(emptyList(), testDict))
    }

    @Test fun `match - мусорный trace не матчится`() {
        // Random sequence that doesn't match anything
        val r = GlideMatcher.matchBestWord(
            traceLetters = listOf('z', 'q', 'x', 'w', 'k', 'j'),
            dictionary = testDict,
        )
        assertNull(r)
    }

    @Test fun `match - русское слово привет`() {
        val r = GlideMatcher.matchBestWord(
            traceLetters = listOf('п', 'р', 'и', 'в', 'е', 'т'),
            dictionary = testDict,
        )
        assertEquals("привет", r)
    }

    @Test fun `match - user frequency boost меняет результат`() {
        // Два слова одинаково подходят. Высокая user-frequency → выбираем то.
        val dict = listOf("hola", "hora")
        val trace = listOf('h', 'o', 'a')  // обе подходят равно — last='a', start='h'
        val withoutBoost = GlideMatcher.matchBestWord(trace, dict, emptyMap())
        assertNotNull(withoutBoost)
        // Сильный boost для hora → выбираем hora
        val withBoost = GlideMatcher.matchBestWord(trace, dict, mapOf("hora" to 500))
        assertEquals("hora", withBoost)
    }

    // ── topMatches ──

    @Test fun `topMatches - возвращает несколько кандидатов`() {
        val r = GlideMatcher.topMatches(
            traceLetters = listOf('c', 'o'),
            dictionary = testDict,
            n = 3,
        )
        // Слова на 'c': casa, comer, como, comprar
        // Лучшие 3 (по distance к "co")
        assertTrue("Должно быть несколько результатов на c*", r.size in 1..3)
        assertTrue("Должны начинаться на c", r.all { it.startsWith("c") })
    }

    @Test fun `topMatches - пустой trace - empty`() {
        assertEquals(emptyList<String>(), GlideMatcher.topMatches(emptyList(), testDict))
    }

    // ── Полный flow симуляция ──

    @Test fun `Симуляция - palец прошёл h-o-l-a с дрожанием`() {
        // Палец дрожал на каждой клавише → много повторных snap'ов
        val rawTrace = listOf('h', 'h', 'h', 'o', 'o', 'l', 'l', 'l', 'a', 'a')
        val deduped = GlideMatcher.dedupeConsecutive(rawTrace)
        assertEquals(listOf('h', 'o', 'l', 'a'), deduped)
        val matched = GlideMatcher.matchBestWord(deduped, testDict)
        assertEquals("hola", matched)
    }

    @Test fun `Симуляция - двуязычный словарь, испанский trace выбирает hola`() {
        val mixedDict = testDict + listOf("hello", "high", "happy")
        val r = GlideMatcher.matchBestWord(
            traceLetters = listOf('h', 'o', 'l', 'a'),
            dictionary = mixedDict,
        )
        assertEquals("hola", r)
    }
}
