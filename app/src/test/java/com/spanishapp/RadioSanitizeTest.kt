package com.spanishapp

import com.spanishapp.radio.player.sanitizeNowPlaying
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit-тесты для sanitizeNowPlaying — фильтрует ICY metadata из радио-потока
 * перед показом юзеру. Защищает от:
 *  - вредных control characters / RTL override (визуальный спуфинг)
 *  - чересчур длинных строк (OOM / визуальный мусор)
 *  - типичного «noise» от Icecast-серверов (unknown, no title, "-")
 */
class RadioSanitizeTest {

    @Test
    fun `null returns null`() {
        assertNull(sanitizeNowPlaying(null))
    }

    @Test
    fun `blank returns null`() {
        assertNull(sanitizeNowPlaying(""))
        assertNull(sanitizeNowPlaying("   "))
        assertNull(sanitizeNowPlaying("\t\n"))
    }

    @Test
    fun `normal title passes through`() {
        assertEquals(
            "Enrique Iglesias - Bailamos",
            sanitizeNowPlaying("Enrique Iglesias - Bailamos"),
        )
    }

    @Test
    fun `title trimmed`() {
        assertEquals(
            "Bailamos",
            sanitizeNowPlaying("  Bailamos  "),
        )
    }

    @Test
    fun `length limit 120 chars`() {
        val long = "a".repeat(300)
        val result = sanitizeNowPlaying(long)
        assertEquals(120, result?.length)
    }

    @Test
    fun `control characters stripped`() {
        //  - SOH,  - BEL,  - ESC
        assertEquals(
            "Clean Title",
            sanitizeNowPlaying("Clean Title"),
        )
    }

    @Test
    fun `RTL override stripped to prevent spoofing`() {
        // ‮ - RIGHT-TO-LEFT OVERRIDE — может маскировать exe.txt как txt.exe
        val malicious = "Song‮evil.com"
        val result = sanitizeNowPlaying(malicious)
        assertEquals("Songevil.com", result)
    }

    @Test
    fun `zero-width characters stripped`() {
        // ​ - ZWSP, ‎ - LTR mark
        assertEquals(
            "Title",
            sanitizeNowPlaying("‎Tit​le"),
        )
    }

    @Test
    fun `unknown noise filtered`() {
        assertNull(sanitizeNowPlaying("unknown"))
        assertNull(sanitizeNowPlaying("Unknown"))
        assertNull(sanitizeNowPlaying("UNKNOWN"))
        assertNull(sanitizeNowPlaying("no title"))
        assertNull(sanitizeNowPlaying("-"))
        assertNull(sanitizeNowPlaying("—"))
        assertNull(sanitizeNowPlaying("n/a"))
    }

    @Test
    fun `spanish characters preserved`() {
        assertEquals(
            "Canción de España",
            sanitizeNowPlaying("Canción de España"),
        )
    }

    @Test
    fun `emoji preserved`() {
        assertEquals(
            "Song 🎵",
            sanitizeNowPlaying("Song 🎵"),
        )
    }
}
