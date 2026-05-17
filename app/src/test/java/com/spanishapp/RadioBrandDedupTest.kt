package com.spanishapp

import com.spanishapp.radio.data.CefrLevel
import com.spanishapp.radio.data.Country
import com.spanishapp.radio.data.DiscoveredStation
import com.spanishapp.radio.data.Genre
import com.spanishapp.radio.data.brandKey
import com.spanishapp.radio.data.deduplicateByBrand
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * radio-browser.info часто возвращает много региональных вариантов одной
 * сети («Cadena SER España», «Cadena SER Madrid», «Cadena SER Valencia»).
 *
 * Эти тесты гарантируют что:
 *  - brandKey корректно нормализует названия в «семью»
 *  - deduplicateByBrand оставляет не более N штук на бренд
 */
class RadioBrandDedupTest {

    @Test
    fun `brandKey takes first two words`() {
        assertEquals("cadena ser", brandKey("Cadena SER España"))
        assertEquals("cadena ser", brandKey("Cadena SER Madrid"))
        assertEquals("cadena ser", brandKey("CADENA SER - Radio Valencia"))
        assertEquals("rne radio", brandKey("RNE Radio 1"))
        assertEquals("rne radio", brandKey("RNE Radio 2"))
        assertEquals("los 40", brandKey("Los 40 Principales"))
        assertEquals("los 40", brandKey("Los 40 Classic"))
    }

    @Test
    fun `brandKey handles single word names`() {
        assertEquals("caracol", brandKey("Caracol"))
        assertEquals("cope", brandKey("COPE"))
    }

    @Test
    fun `brandKey strips punctuation`() {
        assertEquals("cadena ser", brandKey("Cadena SER - Radio"))
        assertEquals("cadena ser", brandKey("Cadena SER, Madrid!"))
        assertEquals("onda cero", brandKey("Onda Cero / Madrid"))
    }

    @Test
    fun `brandKey preserves diacritics`() {
        assertEquals("radio españa", brandKey("Radio España Nacional"))
    }

    @Test
    fun `brandKey collapses whitespace`() {
        assertEquals("cadena ser", brandKey("  Cadena   SER  Madrid  "))
    }

    @Test
    fun `brandKey handles empty input`() {
        assertEquals("", brandKey(""))
        assertEquals("", brandKey("   "))
        assertEquals("", brandKey("...---..."))
    }

    @Test
    fun `deduplicate keeps max 2 per brand`() {
        val stations = listOf(
            station("ser1", "Cadena SER España"),
            station("ser2", "Cadena SER Madrid"),
            station("ser3", "Cadena SER Valencia"),       // должна выпасть — 3-я SER
            station("ser4", "Cadena SER Zaragoza"),       // выпадает
            station("cope1", "COPE"),
            station("rne1", "RNE Radio 1"),
            station("rne2", "RNE Radio 2"),
            station("rne3", "RNE Radio 3"),               // 3-я RNE — выпадает
            station("onda1", "Onda Cero"),
        )
        val result = deduplicateByBrand(stations, maxPerBrand = 2)
        assertEquals(
            listOf("ser1", "ser2", "cope1", "rne1", "rne2", "onda1"),
            result.map { it.id },
        )
    }

    @Test
    fun `deduplicate preserves order of first occurrence`() {
        val stations = listOf(
            station("a", "Brand X"),
            station("b", "Brand Y"),
            station("c", "Brand X"),
        )
        val result = deduplicateByBrand(stations, maxPerBrand = 2)
        assertEquals(listOf("a", "b", "c"), result.map { it.id })
    }

    @Test
    fun `deduplicate gives variety from saturated brand pool`() {
        // 10 SER + 1 COPE → должны увидеть 2 SER + 1 COPE = 3 (а не 10 SER, как раньше)
        val ser = (1..10).map { station("ser$it", "Cadena SER Region$it") }
        val cope = listOf(station("cope1", "COPE"))
        val result = deduplicateByBrand(ser + cope, maxPerBrand = 2)
        assertEquals(3, result.size)
        assertEquals(2, result.count { brandKey(it.name) == "cadena ser" })
        assertEquals(1, result.count { brandKey(it.name) == "cope" })
    }

    @Test
    fun `deduplicate respects custom limit`() {
        val stations = listOf(
            station("a1", "Brand X 1"),
            station("a2", "Brand X 2"),
            station("a3", "Brand X 3"),
            station("a4", "Brand X 4"),
        )
        // С maxPerBrand=3 оставляем 3 из 4
        val result3 = deduplicateByBrand(stations, maxPerBrand = 3)
        assertEquals(3, result3.size)
        // С maxPerBrand=1 — только первый
        val result1 = deduplicateByBrand(stations, maxPerBrand = 1)
        assertEquals(1, result1.size)
    }

    private fun station(id: String, name: String): DiscoveredStation = DiscoveredStation(
        id = id,
        shortCode = id.take(4).uppercase(),
        name = name,
        program = "Test",
        frequency = 100f,
        country = Country.SPAIN,
        genre = Genre.MUSIC,
        level = CefrLevel.A2,
        streamUrl = "https://stream.example.com/$id",
        bitrate = 128,
    )
}
