package com.spanishapp.domain.checkpoint

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Грузит контент чекпоинтов из `assets/checkpoints/cpN_*.json`.
 * Контент задеплоен в APK при сборке, по сети не ходим.
 */
@Singleton
class CheckpointRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val json = Json {
        ignoreUnknownKeys = true     // позволяет добавлять поля в JSON не ломая старый код
        isLenient = true
        coerceInputValues = true     // null/missing → default из data class
    }

    /** Кэш загруженных чекпоинтов в памяти. Грузим один раз за процесс. */
    @Volatile private var cache: Map<String, CheckpointData>? = null

    /**
     * Список чекпоинтов в порядке прохождения (cp1 → cp4).
     * Для UI карты чекпоинтов на главном экране.
     */
    suspend fun listAll(): List<CheckpointData> = loadAll().values
        .sortedBy { it.block }

    /** Загрузить один чекпоинт по id ("cp1".."cp4"). null если не найден. */
    suspend fun getById(id: String): CheckpointData? = loadAll()[id]

    /** Какой CP открывается после прохождения N-го блока (cp1 после блока 1, etc). */
    suspend fun getByBlock(block: Int): CheckpointData? = loadAll().values
        .firstOrNull { it.block == block }

    private suspend fun loadAll(): Map<String, CheckpointData> {
        cache?.let { return it }
        return withContext(Dispatchers.IO) {
            cache?.let { return@withContext it }   // double-check inside coroutine
            val fileNames = listOf(
                "checkpoints/cp1_a1_passport.json",
                "checkpoints/cp2_a1_apartment.json",
                "checkpoints/cp3_a1_restaurant.json",
                "checkpoints/cp4_a1_madrid_day.json",
            )
            val loaded = mutableMapOf<String, CheckpointData>()
            for (path in fileNames) {
                val raw = runCatching {
                    context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
                }.getOrNull() ?: continue
                val data = runCatching { json.decodeFromString(CheckpointData.serializer(), raw) }
                    .getOrNull() ?: continue
                loaded[data.id] = data
            }
            cache = loaded
            loaded
        }
    }
}
