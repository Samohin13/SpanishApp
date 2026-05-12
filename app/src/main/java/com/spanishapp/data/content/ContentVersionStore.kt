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
     * Reactive flag: true once the initial content download has completed.
     *
     * Default = FALSE — forces the DownloadScreen after registration.
     * Navigation ensures download only happens AFTER the user is logged in
     * (Firebase auth token is available → Firebase Storage rules pass).
     * Flow: welcome → register → onboarding → DownloadScreen → home.
     *
     * After a successful syncContent() + ContentImporter.apply(),
     * markContentReady() sets this to true permanently. Subsequent launches
     * go straight to home; new packs are synced silently via ContentSyncWorker.
     */
    val contentReady: Flow<Boolean> =
        context.contentVersionDataStore.data.map { it[readyKey] ?: false }

    suspend fun markContentReady() {
        context.contentVersionDataStore.edit { it[readyKey] = true }
    }
}
