package com.spanishapp.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "app_preferences")

enum class ThemeMode { AUTO, LIGHT, DARK }

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val TTS_ENABLED      = booleanPreferencesKey("tts_enabled")
        val THEME_MODE       = stringPreferencesKey("theme_mode")
        val SOUND_EFFECTS    = booleanPreferencesKey("sound_effects")
        val BG_MUSIC         = booleanPreferencesKey("bg_music")
        val VIBRATION        = booleanPreferencesKey("vibration")
        val REMINDERS        = booleanPreferencesKey("reminders")
        val FONT_SIZE        = stringPreferencesKey("font_size") // SMALL, MEDIUM, LARGE
    }

    val soundEffectsEnabled: Flow<Boolean> = context.dataStore.data.map { it[SOUND_EFFECTS] ?: true }
    suspend fun setSoundEffectsEnabled(enabled: Boolean) = context.dataStore.edit { it[SOUND_EFFECTS] = enabled }

    val bgMusicEnabled: Flow<Boolean> = context.dataStore.data.map { it[BG_MUSIC] ?: false }
    suspend fun setBgMusicEnabled(enabled: Boolean) = context.dataStore.edit { it[BG_MUSIC] = enabled }

    val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { it[VIBRATION] ?: true }
    suspend fun setVibrationEnabled(enabled: Boolean) = context.dataStore.edit { it[VIBRATION] = enabled }

    val remindersEnabled: Flow<Boolean> = context.dataStore.data.map { it[REMINDERS] ?: true }
    suspend fun setRemindersEnabled(enabled: Boolean) = context.dataStore.edit { it[REMINDERS] = enabled }

    val fontSize: Flow<String> = context.dataStore.data.map { it[FONT_SIZE] ?: "MEDIUM" }
    suspend fun setFontSize(size: String) = context.dataStore.edit { it[FONT_SIZE] = size }

    /** Глобальный тумблер звука (TTS). По умолчанию — включён. */
    val ttsEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[TTS_ENABLED] ?: true }

    suspend fun setTtsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[TTS_ENABLED] = enabled }
    }

    /** Тема: AUTO / LIGHT / DARK. По умолчанию — AUTO. */
    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { prefs ->
            when (prefs[THEME_MODE]) {
                "LIGHT" -> ThemeMode.LIGHT
                "DARK"  -> ThemeMode.DARK
                else    -> ThemeMode.AUTO
            }
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs -> prefs[THEME_MODE] = mode.name }
    }
}
