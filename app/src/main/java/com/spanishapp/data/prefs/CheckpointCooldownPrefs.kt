package com.spanishapp.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Хранит per-checkpoint cooldown (epoch ms когда можно повторить
 * после fail). Используется CheckpointViewModel + ResultView для
 * блокировки кнопки «Попробовать снова» на 24 часа.
 *
 * Юзер может пропустить ожидание заплатив -50 skill rating
 * (см. CheckpointViewModel.payRatingCostForRetry).
 */
private val Context.checkpointCooldownDataStore by preferencesDataStore(
    name = "checkpoint_cooldown_prefs"
)

@Singleton
class CheckpointCooldownPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private fun keyFor(cpId: String) = longPreferencesKey("cooldown_$cpId")

    /** Epoch ms когда retry снова разрешён. 0L = нет cooldown. */
    fun cooldownFor(cpId: String): Flow<Long> =
        context.checkpointCooldownDataStore.data.map { it[keyFor(cpId)] ?: 0L }

    suspend fun setCooldown(cpId: String, untilMs: Long) {
        context.checkpointCooldownDataStore.edit { it[keyFor(cpId)] = untilMs }
    }

    suspend fun clearCooldown(cpId: String) {
        context.checkpointCooldownDataStore.edit { it.remove(keyFor(cpId)) }
    }
}
