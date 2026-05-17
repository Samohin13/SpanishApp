package com.spanishapp.radio.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val Context.radioBlocklistDataStore by preferencesDataStore("radio_blocklist")

/**
 * Persistent blocklist «мёртвых» радиостанций.
 *
 * Раньше (v1.11.1) blocklist жил только в памяти `RadioViewModel`.
 * Рестарт приложения → мертвецы снова в карусели → auto-skip опять
 * сжигает 7 сек на каждый. Раздражение для юзера.
 *
 * Сейчас: храним в DataStore как JSON `{stationId → blockedAt_ms}`.
 * TTL 48 часов — после истечения станция возвращается в каталог.
 * Если за 48ч ничего не изменилось — auto-skip снова сработает, но
 * это редко (URL обычно либо жив, либо помер навсегда).
 *
 * При сохранении автоматически чистим expired записи — не растёт
 * бесконечно.
 */
@Singleton
class RadioBlocklistPrefs @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val keyJson = stringPreferencesKey(KEY_JSON)
    private val json = Json { ignoreUnknownKeys = true }
    // Explicit serializer для Map<String, Long> — reified inline иногда не выводит
    private val mapSerializer = MapSerializer(String.serializer(), Long.serializer())

    /**
     * Flow активных (не expired) ID. Обновляется при каждом изменении
     * DataStore — UI/VM реактивно перестраивается.
     */
    val activeIds: Flow<Set<String>> = context.radioBlocklistDataStore.data.map { prefs ->
        val raw = prefs[keyJson] ?: return@map emptySet()
        val map = decodeSafe(raw)
        val cutoff = System.currentTimeMillis() - TTL_MS
        map.filter { it.value >= cutoff }.keys
    }

    /** Заблокировать станцию + chistka expired по пути. */
    suspend fun block(stationId: String) {
        context.radioBlocklistDataStore.edit { prefs ->
            val current = prefs[keyJson]?.let { decodeSafe(it) } ?: emptyMap()
            val cutoff = System.currentTimeMillis() - TTL_MS
            val cleaned = current.filter { it.value >= cutoff }
            val updated = cleaned + (stationId to System.currentTimeMillis())
            prefs[keyJson] = json.encodeToString(mapSerializer, updated)
        }
    }

    /** Ручной unblock (на будущее — UI «вернуть в каталог»). */
    suspend fun unblock(stationId: String) {
        context.radioBlocklistDataStore.edit { prefs ->
            val current = prefs[keyJson]?.let { decodeSafe(it) } ?: emptyMap()
            val updated = current - stationId
            prefs[keyJson] = if (updated.isEmpty()) "" else json.encodeToString(mapSerializer, updated)
        }
    }

    /** Очистить весь blocklist (debugging / settings reset). */
    suspend fun clearAll() {
        context.radioBlocklistDataStore.edit { prefs ->
            prefs.remove(keyJson)
        }
    }

    private fun decodeSafe(raw: String): Map<String, Long> = runCatching {
        if (raw.isBlank()) emptyMap() else json.decodeFromString(mapSerializer, raw)
    }.getOrElse { emptyMap() }

    companion object {
        private const val KEY_JSON = "blocklist_json"
        private const val TTL_MS = 48L * 60 * 60 * 1000  // 48 часов
    }
}
