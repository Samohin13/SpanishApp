package com.spanishapp.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Персональный словарь: каждое отправленное в чат слово +1 к частоте.
 * Подсказки клавы используют этот словарь + статический WordSuggester.
 *
 * Хранение: JSON в DataStore preferences (1 файл, 1 ключ).
 * Топ-MAX_ENTRIES слов по count, остальное эвиктится при save.
 *
 * In-memory StateFlow обновляется сразу → suggest() работает мгновенно
 * без I/O. Запись в DataStore — асинхронно в фоне.
 */
private val Context.userWordFreqStore by preferencesDataStore("user_word_freq")

@Singleton
class UserWordFrequency @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val FREQ_KEY = stringPreferencesKey("word_freq_json")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** In-memory кэш карты {слово → счётчик}. */
    private val _freq = MutableStateFlow<Map<String, Int>>(emptyMap())
    val freq: StateFlow<Map<String, Int>> = _freq

    init {
        scope.launch { _freq.value = loadFromDataStore() }
    }

    /**
     * Записать все слова из текста (split по non-letter). Слова короче 2
     * символов или длиннее 30 игнорируются. Lowercase.
     */
    fun recordText(text: String) {
        scope.launch {
            val words = extractWords(text)
            if (words.isEmpty()) return@launch
            val updated = _freq.value.toMutableMap()
            words.forEach { w -> updated[w] = (updated[w] ?: 0) + 1 }
            _freq.value = updated
            saveToDataStore(updated)
        }
    }

    /**
     * Подсказки по prefix. Возвращает топ слов сортированных по count desc.
     * Дёшево — работает на in-memory кэше, без I/O.
     */
    fun suggest(prefix: String, limit: Int = 3): List<String> {
        if (prefix.length < 2) return emptyList()
        val p = prefix.lowercase()
        return _freq.value.entries
            .asSequence()
            .filter { it.key.startsWith(p) && it.key != p }
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key }
            .toList()
    }

    private fun extractWords(text: String): List<String> {
        return text.lowercase()
            .split(Regex("[^\\p{L}]+"))
            .filter { it.length in 2..30 }
    }

    private suspend fun loadFromDataStore(): Map<String, Int> {
        val json = context.userWordFreqStore.data.first()[FREQ_KEY] ?: return emptyMap()
        return try {
            val obj = JSONObject(json)
            val map = mutableMapOf<String, Int>()
            obj.keys().forEach { k -> map[k] = obj.optInt(k, 0) }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private suspend fun saveToDataStore(map: Map<String, Int>) {
        // Топ MAX_ENTRIES, иначе DataStore разрастётся
        val pruned = if (map.size > MAX_ENTRIES) {
            map.entries.sortedByDescending { it.value }.take(MAX_ENTRIES)
                .associate { it.toPair() }
        } else map

        val json = JSONObject().apply {
            pruned.forEach { (k, v) -> put(k, v) }
        }
        context.userWordFreqStore.edit { it[FREQ_KEY] = json.toString() }
    }

    companion object {
        const val MAX_ENTRIES = 500
    }
}
