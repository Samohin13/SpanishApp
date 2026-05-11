package com.spanishapp.data.content

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.contentVersionDataStore by preferencesDataStore("content_versions")

/**
 * Persists the version number of each locally-applied content pack so the
 * downloader can skip unchanged packs on subsequent launches.
 *
 * Stored as `pack_{id}` → Int.
 */
@Singleton
class ContentVersionStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private fun key(packId: String) = intPreferencesKey("pack_$packId")

    suspend fun getVersion(packId: String): Int? =
        context.contentVersionDataStore.data.first()[key(packId)]

    suspend fun setVersion(packId: String, version: Int) {
        context.contentVersionDataStore.edit { it[key(packId)] = version }
    }

    suspend fun clearAll() {
        context.contentVersionDataStore.edit { it.clear() }
    }
}
