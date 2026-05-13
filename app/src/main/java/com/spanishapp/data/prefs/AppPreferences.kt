package com.spanishapp.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
        val VIBRATION_INTENSITY = intPreferencesKey("vibration_intensity") // 0=off,1=light,2=medium,3=strong
        val REMINDERS        = booleanPreferencesKey("reminders")
        val REMINDER_HOUR    = intPreferencesKey("reminder_hour")    // 0..23
        val REMINDER_MINUTE  = intPreferencesKey("reminder_minute")  // 0..59
        val FONT_SIZE        = stringPreferencesKey("font_size") // SMALL, MEDIUM, LARGE
        val UI_LANGUAGE      = stringPreferencesKey("ui_language") // "ru", "en", "system"
    }

    val soundEffectsEnabled: Flow<Boolean> = context.dataStore.data.map { it[SOUND_EFFECTS] ?: true }
    suspend fun setSoundEffectsEnabled(enabled: Boolean) = context.dataStore.edit { it[SOUND_EFFECTS] = enabled }

    val bgMusicEnabled: Flow<Boolean> = context.dataStore.data.map { it[BG_MUSIC] ?: false }
    suspend fun setBgMusicEnabled(enabled: Boolean) = context.dataStore.edit { it[BG_MUSIC] = enabled }

    /**
     * Vibration intensity in percent: 0=off … 100=max.
     * Migrates from legacy boolean (true→70, false→0) and from old 0..3 levels (1→33, 2→66, 3→100).
     */
    val vibrationIntensity: Flow<Int> = context.dataStore.data.map {
        val stored = it[VIBRATION_INTENSITY]
        when {
            stored == null -> if (it[VIBRATION] != false) 70 else 0
            stored in 1..3 -> stored * 33   // legacy 1/2/3 → 33/66/99
            else           -> stored.coerceIn(0, 100)
        }
    }
    suspend fun setVibrationIntensity(percent: Int) = context.dataStore.edit {
        it[VIBRATION_INTENSITY] = percent.coerceIn(0, 100)
        it[VIBRATION] = percent > 0  // keep legacy in sync
    }

    /** Backward-compat boolean derived from intensity. */
    val vibrationEnabled: Flow<Boolean> = vibrationIntensity.map { it > 0 }
    suspend fun setVibrationEnabled(enabled: Boolean) = setVibrationIntensity(if (enabled) 70 else 0)

    val remindersEnabled: Flow<Boolean> = context.dataStore.data.map { it[REMINDERS] ?: true }
    suspend fun setRemindersEnabled(enabled: Boolean) = context.dataStore.edit { it[REMINDERS] = enabled }

    /** Час напоминания (0..23). По умолчанию 19:00. */
    val reminderHour: Flow<Int> = context.dataStore.data.map { it[REMINDER_HOUR] ?: 19 }
    val reminderMinute: Flow<Int> = context.dataStore.data.map { it[REMINDER_MINUTE] ?: 0 }
    suspend fun setReminderTime(hour: Int, minute: Int) = context.dataStore.edit {
        it[REMINDER_HOUR] = hour.coerceIn(0, 23)
        it[REMINDER_MINUTE] = minute.coerceIn(0, 59)
    }

    val fontSize: Flow<String> = context.dataStore.data.map { it[FONT_SIZE] ?: "MEDIUM" }
    suspend fun setFontSize(size: String) = context.dataStore.edit { it[FONT_SIZE] = size }

    /**
     * Язык UI: "ru" / "en" / "uk" / "es" / "system".
     *
     * Default — "ru". В v1 приложение архитектурно RU→ES: всё содержимое
     * (словарь, уроки, рассказы, упражнения) на русском. Локализация
     * UI-chrome на en/uk/es есть в repo и работает, но без перевода
     * контента не-русскоязычный пользователь получит «половина-на-
     * половину» опыт. Поэтому defaults в "ru" даже на испанском телефоне.
     *
     * Переключатель остаётся в Settings (для продвинутых пользователей
     * и для будущего, когда будем переводить контент). На Welcome убран,
     * чтобы новый юзер не пробовал en/uk/es и не получил «битый» UX.
     */
    val uiLanguage: Flow<String> = context.dataStore.data.map { it[UI_LANGUAGE] ?: "ru" }
    suspend fun setUiLanguage(lang: String) = context.dataStore.edit { it[UI_LANGUAGE] = lang }

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
