package com.spanishapp.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Создаём DataStore один раз на весь App
private val Context.dataStore by preferencesDataStore(name = "app_preferences")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
    }

    /** Глобальный тумблер звука (TTS). По умолчанию — включён. */
    val ttsEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[TTS_ENABLED] ?: true }

    suspend fun setTtsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[TTS_ENABLED] = enabled
        }
    }
}
