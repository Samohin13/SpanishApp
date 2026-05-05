package com.spanishapp.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.voiceDataStore by preferencesDataStore(name = "voice_prefs")

data class VoiceSettings(
    val selectedVoiceName: String? = null,   // системное имя выбранного TTS-голоса
    val rate: Float = 0.85f,
    val pitch: Float = 1.0f,
    val seenInstallPrompt: Boolean = false
)

@Singleton
class VoicePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val VOICE_NAME = stringPreferencesKey("voice_name")
        val RATE       = floatPreferencesKey("rate")
        val PITCH      = floatPreferencesKey("pitch")
        val SEEN_PROMPT = booleanPreferencesKey("seen_install_prompt")
    }

    val settings: Flow<VoiceSettings> = context.voiceDataStore.data.map { prefs ->
        VoiceSettings(
            selectedVoiceName = prefs[Keys.VOICE_NAME],
            rate              = prefs[Keys.RATE]  ?: 0.85f,
            pitch             = prefs[Keys.PITCH] ?: 1.0f,
            seenInstallPrompt = prefs[Keys.SEEN_PROMPT] ?: false
        )
    }

    suspend fun setVoiceName(name: String) {
        context.voiceDataStore.edit { it[Keys.VOICE_NAME] = name }
    }

    suspend fun setRate(rate: Float) {
        context.voiceDataStore.edit { it[Keys.RATE] = rate }
    }

    suspend fun setPitch(pitch: Float) {
        context.voiceDataStore.edit { it[Keys.PITCH] = pitch }
    }

    suspend fun markPromptSeen() {
        context.voiceDataStore.edit { it[Keys.SEEN_PROMPT] = true }
    }
}
