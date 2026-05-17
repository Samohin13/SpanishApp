package com.spanishapp

import com.spanishapp.radio.data.isSafeStreamUrl
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * URL whitelist для радио-потоков. radio-browser.info — это user-submitted
 * каталог, в нём теоретически может оказаться malicious URL (javascript:,
 * file://, content://). ExoPlayer обработал бы их безопасно, но мы отсекаем
 * явно для defense in depth.
 */
class RadioUrlSafetyTest {

    @Test
    fun `http allowed`() {
        assertTrue(isSafeStreamUrl("http://stream.example.com/radio.mp3"))
    }

    @Test
    fun `https allowed`() {
        assertTrue(isSafeStreamUrl("https://stream.example.com/radio.mp3"))
    }

    @Test
    fun `rtsp allowed`() {
        assertTrue(isSafeStreamUrl("rtsp://stream.example.com/live"))
    }

    @Test
    fun `case insensitive scheme`() {
        assertTrue(isSafeStreamUrl("HTTPS://example.com/stream"))
        assertTrue(isSafeStreamUrl("Http://example.com/stream"))
    }

    @Test
    fun `javascript blocked`() {
        assertFalse(isSafeStreamUrl("javascript:alert(1)"))
        assertFalse(isSafeStreamUrl("javascript://evil"))
    }

    @Test
    fun `file blocked`() {
        assertFalse(isSafeStreamUrl("file:///etc/passwd"))
        assertFalse(isSafeStreamUrl("file://localhost/sdcard/secret"))
    }

    @Test
    fun `content blocked`() {
        assertFalse(isSafeStreamUrl("content://com.android.contacts/data"))
    }

    @Test
    fun `data uri blocked`() {
        assertFalse(isSafeStreamUrl("data:audio/mp3;base64,SGVsbG8="))
    }

    @Test
    fun `ftp blocked`() {
        // FTP не нужен для радио и часто проблематичен
        assertFalse(isSafeStreamUrl("ftp://stream.example.com/radio.mp3"))
    }

    @Test
    fun `malformed url returns false`() {
        assertFalse(isSafeStreamUrl(""))
        assertFalse(isSafeStreamUrl("not-a-url"))
        assertFalse(isSafeStreamUrl("://"))
    }

    @Test
    fun `real radio urls work`() {
        // Реальные URLs из нашего fallback каталога
        assertTrue(isSafeStreamUrl("https://playerservices.streamtheworld.com/api/livestream-redirect/CADENA100AAC.aac"))
        assertTrue(isSafeStreamUrl("https://stream.zeno.fm/0r0xa792kwzuv"))
        assertTrue(isSafeStreamUrl("http://stream.flamencoradio.com:8000/flamenco"))
    }
}
