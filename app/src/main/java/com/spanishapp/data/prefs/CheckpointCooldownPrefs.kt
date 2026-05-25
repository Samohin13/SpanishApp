package com.spanishapp.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Хранит per-checkpoint:
 *   1. cooldown (epoch ms когда можно повторить после fail)
 *   2. free retry use flag (юзер уже использовал 1 бесплатную пересдачу
 *      для этого CP — после она уже за -50 рейтинга или 24 часа)
 *
 * Используется CheckpointViewModel + ResultView для блокировки кнопок.
 *
 * v1.22.30: добавлен freeRetryUsed flag — каждый CP даёт 1 бесплатную
 * пересдачу новичкам, чтобы 0-rating юзеры не блокировались на 24ч
 * после первого fail. После использования — стандартный flow (-50 или 24ч).
 */
private val Context.checkpointCooldownDataStore by preferencesDataStore(
    name = "checkpoint_cooldown_prefs"
)

@Singleton
class CheckpointCooldownPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private fun cooldownKey(cpId: String) = longPreferencesKey("cooldown_$cpId")
    private fun freeUsedKey(cpId: String) = booleanPreferencesKey("free_used_$cpId")

    /** Epoch ms когда retry снова разрешён. 0L = нет cooldown. */
    fun cooldownFor(cpId: String): Flow<Long> =
        context.checkpointCooldownDataStore.data.map { it[cooldownKey(cpId)] ?: 0L }

    suspend fun setCooldown(cpId: String, untilMs: Long) {
        context.checkpointCooldownDataStore.edit { it[cooldownKey(cpId)] = untilMs }
    }

    suspend fun clearCooldown(cpId: String) {
        context.checkpointCooldownDataStore.edit { it.remove(cooldownKey(cpId)) }
    }

    /** true если юзер уже использовал бесплатную пересдачу для этого CP. */
    fun freeRetryUsed(cpId: String): Flow<Boolean> =
        context.checkpointCooldownDataStore.data.map { it[freeUsedKey(cpId)] ?: false }

    suspend fun markFreeRetryUsed(cpId: String) {
        context.checkpointCooldownDataStore.edit { it[freeUsedKey(cpId)] = true }
    }
}
