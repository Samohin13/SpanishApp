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
     * Reactive flag: true if the app can proceed to home with the content
     * it has on hand (either built-in seeded data, or downloaded OTA packs).
     *
     * Default = TRUE — the app always ships with a complete built-in dataset
     * (DatabaseSeeder + ModernVocab + LessonContentData + LibrosData), so a
     * fresh install can fully function without ever talking to the network.
     * OTA content packs are an optional enhancement, triggered manually from
     * Settings or silently in the background via ContentSyncWorker.
     *
     * markContentReady() is still called by the downloader on success — and
     * markContentSkipped() lets the user escape if the download screen ever
     * gets shown and Firebase is unreachable.
     */
    val contentReady: Flow<Boolean> =
        context.contentVersionDataStore.data.map { it[readyKey] ?: true }

    suspend fun markContentReady() {
        context.contentVersionDataStore.edit { it[readyKey] = true }
    }
}
