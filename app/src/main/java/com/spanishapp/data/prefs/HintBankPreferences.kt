package com.spanishapp.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.hintBankDataStore by preferencesDataStore(name = "hint_bank_prefs")

private object HintKeys {
    val HINTS = intPreferencesKey("hints")
}

/**
 * v1.16.0: Hint Bank — единая валюта подсказок для всех игр.
 *
 * Юзер «зарабатывает» 💡 за обучающие действия (уроки, флэшкарды,
 * теория, книги, streak, ачивки) и «тратит» в играх (Sopa, Crossword,
 * Palabra, Articles, Math).
 *
 * Skill rating и XP НЕ трогаются — это индикаторы прогресса для
 * лидерборда. Hint bank — отдельная currency.
 *
 * Стартовый бонус — 5 💡 (юзер сразу может попробовать подсказки в игре).
 */
@Singleton
class HintBankPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val INITIAL_HINTS = 5
    }

    val hintsFlow: Flow<Int> = context.hintBankDataStore.data.map {
        it[HintKeys.HINTS] ?: INITIAL_HINTS
    }

    suspend fun get(): Int = context.hintBankDataStore.data.map {
        it[HintKeys.HINTS] ?: INITIAL_HINTS
    }.let { flow ->
        var result = INITIAL_HINTS
        flow.collect { result = it; return@collect }
        result
    }

    /** Atomically изменить count. Возвращает новое значение. */
    suspend fun update(transform: (Int) -> Int): Int {
        var newValue = INITIAL_HINTS
        context.hintBankDataStore.edit { prefs ->
            val current = prefs[HintKeys.HINTS] ?: INITIAL_HINTS
            newValue = transform(current).coerceAtLeast(0)
            prefs[HintKeys.HINTS] = newValue
        }
        return newValue
    }
}
