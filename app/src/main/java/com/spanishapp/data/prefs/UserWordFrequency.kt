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
 * Персональный словарь юзера + bigram (предсказание фраз).
 *
 * v1.25.27: добавлено bigram-обучение — для каждого слова X запоминаем
 * какие слова Y часто следуют после него. Это даёт T9-style phrase
 * prediction: после "buenos" предлагаем "días", после "como" — "estás".
 *
 * Хранение:
 *  - word_freq_json: {слово → count}  (unigrams)
 *  - bigram_json: {prev → {next → count}}  (bigrams)
 *
 * In-memory StateFlow обновляется сразу. Запись асинхронно.
 */
private val Context.userWordFreqStore by preferencesDataStore("user_word_freq")

@Singleton
class UserWordFrequency @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val FREQ_KEY = stringPreferencesKey("word_freq_json")
    private val BIGRAM_KEY = stringPreferencesKey("bigram_json")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** In-memory кэш карты {слово → счётчик}. */
    private val _freq = MutableStateFlow<Map<String, Int>>(emptyMap())
    val freq: StateFlow<Map<String, Int>> = _freq

    /** In-memory кэш bigrams: prev word → {next word → count}. */
    private val _bigrams = MutableStateFlow<Map<String, Map<String, Int>>>(emptyMap())
    val bigrams: StateFlow<Map<String, Map<String, Int>>> = _bigrams

    init {
        scope.launch { _freq.value = loadFreqFromDataStore() }
        scope.launch { _bigrams.value = loadBigramsFromDataStore() }
    }

    /**
     * v1.25.27: записать ВСЁ — unigrams + bigrams.
     * Для текста "hola como estas":
     *  unigrams: hola+1, como+1, estas+1
     *  bigrams: (hola→como)+1, (como→estas)+1
     */
    fun recordText(text: String) {
        scope.launch {
            val words = extractWords(text)
            if (words.isEmpty()) return@launch

            // Unigrams
            val freqUpdated = _freq.value.toMutableMap()
            words.forEach { w -> freqUpdated[w] = (freqUpdated[w] ?: 0) + 1 }
            _freq.value = freqUpdated
            saveFreqToDataStore(freqUpdated)

            // Bigrams: для каждой пары (words[i], words[i+1]) +1
            if (words.size >= 2) {
                val bigramUpdated = _bigrams.value.mapValues { it.value.toMutableMap() }
                    .toMutableMap()
                for (i in 0 until words.size - 1) {
                    val prev = words[i]
                    val next = words[i + 1]
                    val inner = bigramUpdated.getOrPut(prev) { mutableMapOf() } as MutableMap<String, Int>
                    inner[next] = (inner[next] ?: 0) + 1
                }
                _bigrams.value = bigramUpdated
                saveBigramsToDataStore(bigramUpdated)
            }
        }
    }

    /**
     * Подсказки по prefix (unigrams). Топ слов по count desc.
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

    /**
     * v1.25.27: предсказание СЛЕДУЮЩЕГО слова после prevWord.
     * Используется когда юзер только что нажал пробел — показываем
     * частые продолжения фразы.
     *
     * Пример: после "buenos" → "días" (если юзер часто это писал).
     */
    fun suggestNext(prevWord: String, limit: Int = 3): List<String> {
        if (prevWord.isBlank()) return emptyList()
        val key = prevWord.lowercase()
        val nextMap = _bigrams.value[key] ?: return emptyList()
        return nextMap.entries
            .asSequence()
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key }
            .toList()
    }

    /**
     * v1.25.27: подсказки с УЧЁТОМ контекста предыдущего слова.
     * Совмещает bigram score + unigram score + prefix match.
     *
     * Пример: ввод "como e" с предыдущим словом "como" →
     * комбинирует bigram[como→estas/eres] + unigram match на "e*".
     */
    fun suggestWithContext(
        prevWord: String,
        prefix: String,
        limit: Int = 3,
    ): List<String> {
        if (prefix.length < 2) {
            // Только bigram next-word prediction
            return suggestNext(prevWord, limit)
        }
        val p = prefix.lowercase()
        val prev = prevWord.lowercase()
        val nextMap = _bigrams.value[prev] ?: emptyMap()

        // Score: bigram count (×3 weight) + unigram count
        val scored = mutableMapOf<String, Int>()
        _freq.value.forEach { (word, freq) ->
            if (word.startsWith(p) && word != p) {
                scored[word] = freq + (nextMap[word] ?: 0) * 3
            }
        }
        return scored.entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key }
    }

    private fun extractWords(text: String): List<String> {
        return text.lowercase()
            .split(Regex("[^\\p{L}]+"))
            .filter { it.length in 2..30 }
    }

    private suspend fun loadFreqFromDataStore(): Map<String, Int> {
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

    private suspend fun loadBigramsFromDataStore(): Map<String, Map<String, Int>> {
        val json = context.userWordFreqStore.data.first()[BIGRAM_KEY] ?: return emptyMap()
        return try {
            val obj = JSONObject(json)
            val map = mutableMapOf<String, MutableMap<String, Int>>()
            obj.keys().forEach { prev ->
                val inner = obj.getJSONObject(prev)
                val innerMap = mutableMapOf<String, Int>()
                inner.keys().forEach { next -> innerMap[next] = inner.optInt(next, 0) }
                map[prev] = innerMap
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private suspend fun saveFreqToDataStore(map: Map<String, Int>) {
        val pruned = if (map.size > MAX_ENTRIES) {
            map.entries.sortedByDescending { it.value }.take(MAX_ENTRIES)
                .associate { it.toPair() }
        } else map

        val json = JSONObject().apply {
            pruned.forEach { (k, v) -> put(k, v) }
        }
        context.userWordFreqStore.edit { it[FREQ_KEY] = json.toString() }
    }

    private suspend fun saveBigramsToDataStore(map: Map<String, Map<String, Int>>) {
        // Prune: для каждого prev оставляем топ-10 next слов
        val pruned = map.mapValues { (_, inner) ->
            if (inner.size > MAX_BIGRAM_PER_WORD) {
                inner.entries.sortedByDescending { it.value }
                    .take(MAX_BIGRAM_PER_WORD).associate { it.toPair() }
            } else inner
        }.entries
            .sortedByDescending { it.value.values.sum() }
            .take(MAX_BIGRAM_PREV)
            .associate { it.toPair() }

        val json = JSONObject().apply {
            pruned.forEach { (prev, inner) ->
                val innerJson = JSONObject().apply {
                    inner.forEach { (next, count) -> put(next, count) }
                }
                put(prev, innerJson)
            }
        }
        context.userWordFreqStore.edit { it[BIGRAM_KEY] = json.toString() }
    }

    companion object {
        const val MAX_ENTRIES = 500
        const val MAX_BIGRAM_PREV = 500  // топ 500 "предыдущих" слов
        const val MAX_BIGRAM_PER_WORD = 10  // для каждого — топ 10 следующих
    }
}
