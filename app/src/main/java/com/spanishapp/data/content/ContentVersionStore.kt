package com.spanishapp.data.content

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.contentVersionDataStore by preferencesDataStore("content_versions")

/**
 * Persists the version number of each locally-applied content pack so the
 * downloader can skip unchanged packs on subsequent launches.
 *
 * Also stores a [contentReady] boolean — true once the initial download has
 * completed at least once. Drives the first-launch gate that forces the
 * download screen before the user can use the app.
 *
 * Stored as `pack_{id}` → Int, plus `content_ready` → Bool.
 */
@Singleton
class ContentVersionStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private fun key(packId: String) = intPreferencesKey("pack_$packId")
    private val readyKey = booleanPreferencesKey("content_ready")

    suspend fun getVersion(packId: String): Int? =
        context.contentVersionDataStore.data.first()[key(packId)]

    suspend fun setVersion(packId: String, version: Int) {
        context.contentVersionDataStore.edit { it[key(packId)] = version }
    }

    suspend fun clearAll() {
        context.contentVersionDataStore.edit { it.clear() }
    }

    /**
     * Reactive flag: true once initial pack download completed at least once.
     *
     * Default = TRUE — all app content ships inside the APK via DatabaseSeeder
     * (CleanVocab, VocabExtra1-12, GrammarContent, etc.). The download screen
     * exists for future OTA updates but must NOT gate first-time users:
     *   • The Firebase Storage URLs may not yet have the packs uploaded.
     *   • Downloaded JSON files are not yet applied to Room DB anyway.
     * Re-enable the gate (change ?: true → ?: false) once the import pipeline
     * is fully implemented and the CDN is loaded.
     */
    val contentReady: Flow<Boolean> =
        context.contentVersionDataStore.data.map { it[readyKey] ?: true }

    suspend fun markContentReady() {
        context.contentVersionDataStore.edit { it[readyKey] = true }
    }
}
