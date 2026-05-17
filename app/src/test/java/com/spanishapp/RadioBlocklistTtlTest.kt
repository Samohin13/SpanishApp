package com.spanishapp

import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit-тесты для логики TTL в RadioBlocklistPrefs.
 *
 * Не тестируем сам DataStore (требует Android instrumentation), но
 * проверяем чистую логику фильтрации по timestamp + JSON serialization
 * map<stationId, blockedAt> — критичные части где могут быть баги.
 */
class RadioBlocklistTtlTest {

    private val ttlMs = 48L * 60 * 60 * 1000  // 48 часов
    private val json = Json { ignoreUnknownKeys = true }
    private val mapSerializer = MapSerializer(String.serializer(), Long.serializer())

    @Test
    fun `fresh entry is active`() {
        val now = System.currentTimeMillis()
        val map = mapOf("st1" to now - 1000)  // блочена 1 сек назад
        val active = filterActive(map, now, ttlMs)
        assertTrue("st1" in active)
    }

    @Test
    fun `expired entry is filtered out`() {
        val now = System.currentTimeMillis()
        val map = mapOf("st1" to now - ttlMs - 1000)  // expired 1 сек назад
        val active = filterActive(map, now, ttlMs)
        assertFalse("st1" in active)
    }

    @Test
    fun `boundary case at exactly TTL is still active`() {
        val now = System.currentTimeMillis()
        val map = mapOf("st1" to now - ttlMs)  // ровно граница
        val active = filterActive(map, now, ttlMs)
        assertTrue("на границе ещё активна", "st1" in active)
    }

    @Test
    fun `mixed map filters correctly`() {
        val now = System.currentTimeMillis()
        val map = mapOf(
            "fresh" to now - 1000,                    // 1 сек назад - активна
            "expired" to now - ttlMs - 1000,          // 48ч+1сек назад - expired
            "old_but_active" to now - ttlMs + 10000,  // 47ч 59 мин назад - активна
        )
        val active = filterActive(map, now, ttlMs)
        assertEquals(setOf("fresh", "old_but_active"), active)
    }

    @Test
    fun `empty map returns empty set`() {
        val active = filterActive(emptyMap(), System.currentTimeMillis(), ttlMs)
        assertTrue(active.isEmpty())
    }

    @Test
    fun `json serialization roundtrip`() {
        val original = mapOf(
            "auto_abc123" to 1779000000000L,
            "auto_def456" to 1779000100000L,
        )
        val encoded = json.encodeToString(mapSerializer, original)
        val decoded = json.decodeFromString(mapSerializer, encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun `json decode handles malformed gracefully`() {
        val result = runCatching {
            json.decodeFromString(mapSerializer, "not json")
        }.getOrNull()
        assertEquals(null, result)
    }

    /** Чистая логика фильтрации — копия того что в RadioBlocklistPrefs.activeIds. */
    private fun filterActive(map: Map<String, Long>, now: Long, ttlMs: Long): Set<String> {
        val cutoff = now - ttlMs
        return map.filter { it.value >= cutoff }.keys
    }
}
