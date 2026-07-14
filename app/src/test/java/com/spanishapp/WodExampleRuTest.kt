package com.spanishapp

import com.spanishapp.data.repository.WodExampleRu
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v1.26.1: целостность встроенных переводов примеров (стадия «Фраза» WoD).
 */
class WodExampleRuTest {

    @Test
    fun `map has all 938 entries`() {
        assertEquals(938, WodExampleRu.MAP.size)
    }

    @Test
    fun `keys are normalized - lowercase and trimmed`() {
        WodExampleRu.MAP.keys.forEach { k ->
            assertEquals("key not trimmed: [$k]", k, k.trim())
            assertEquals("key not lowercase: [$k]", k, k.lowercase())
        }
    }

    @Test
    fun `values are non-blank cyrillic sentences without double quotes`() {
        WodExampleRu.MAP.forEach { (k, v) ->
            assertTrue("blank value for $k", v.isNotBlank())
            assertFalse("double quote in value for $k", v.contains('"'))
            assertTrue("no cyrillic in value for $k: $v", v.any { it in 'а'..'я' || it in 'А'..'Я' || it == 'ё' || it == 'Ё' })
        }
    }

    @Test
    fun `spot-check known entries`() {
        assertEquals("Она смотрится в зеркало.", WodExampleRu.MAP["el espejo"])
        assertTrue(WodExampleRu.MAP.containsKey("la fiesta"))
        assertTrue(WodExampleRu.MAP.containsKey("hola"))
    }
}
