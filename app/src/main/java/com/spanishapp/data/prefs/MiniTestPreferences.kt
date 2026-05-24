package com.spanishapp.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.miniTestDataStore by preferencesDataStore(name = "minitest_prefs")

private object MiniTestKeys {
    /** Set of mini-test ids that the user has passed (≥60%). */
    val PASSED = stringSetPreferencesKey("passed_ids")
}

/**
 * Tracks which mini-tests the user has passed.
 *
 * Mini-tests are optional, lightweight quizzes between regular lessons
 * (see [com.spanishapp.domain.minitest.MiniTest]). Completion is stored
 * here rather than in Room because it carries no schema migration risk
 * and is conceptually preference-grade data.
 */
@Singleton
class MiniTestPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /** All passed mini-test ids as a Flow. */
    val passedIds: Flow<Set<String>> = context.miniTestDataStore.data.map {
        it[MiniTestKeys.PASSED] ?: emptySet()
    }

    /** Flow that emits whether a specific mini-test has been passed. */
    fun isPassed(id: String): Flow<Boolean> =
        passedIds.map { id in it }

    /** Mark a mini-test as passed (idempotent — set semantics). */
    suspend fun markPassed(id: String) {
        context.miniTestDataStore.edit { prefs ->
            val current = prefs[MiniTestKeys.PASSED] ?: emptySet()
            prefs[MiniTestKeys.PASSED] = current + id
        }
    }
}
