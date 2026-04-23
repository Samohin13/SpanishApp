package com.spanishapp.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
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
    val personaId: String = VoicePersonas.DEFAULT_ID,
    val voiceName: String? = null,       // resolved TTS voice for current persona slot
    val speechRate: Float = 0.9f,        // 0.5 .. 1.5  (persona default, user-tweakable)
    val pitch: Float = 1.0f              // 0.5 .. 2.0  (persona default, user-tweakable)
)

@Singleton
class VoicePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object K {
        val PERSONA_ID = stringPreferencesKey("persona_id")
        val VOICE_NAME = stringPreferencesKey("voice_name")
        val RATE       = floatPreferencesKey("rate")
        val PITCH      = floatPreferencesKey("pitch")
    }

    val settings: Flow<VoiceSettings> = context.voiceDataStore.data.map { p ->
        val personaId = p[K.PERSONA_ID] ?: VoicePersonas.DEFAULT_ID
        VoiceSettings(
            personaId  = personaId,
            voiceName  = p[K.VOICE_NAME],
            speechRate = p[K.RATE] ?: 0.9f,
            pitch      = p[K.PITCH] ?: 1.0f
        )
    }

    /** Pick a persona: store the resolved TTS voice. */
    suspend fun selectPersona(personaId: String, resolvedVoiceName: String?) {
        context.voiceDataStore.edit { p ->
            p[K.PERSONA_ID] = personaId
            if (resolvedVoiceName != null) p[K.VOICE_NAME] = resolvedVoiceName else p.remove(K.VOICE_NAME)
        }
    }

    suspend fun setVoiceName(name: String?) = update { p ->
        if (name != null) p[K.VOICE_NAME] = name else p.remove(K.VOICE_NAME)
    }

    suspend fun setRate(r: Float) = update { it[K.RATE] = r }
    suspend fun setPitch(p: Float) = update { it[K.PITCH] = p }

    suspend fun resetToDefaults() {
        context.voiceDataStore.edit { it.clear() }
    }

    private suspend fun update(block: (MutablePreferences) -> Unit) {
        context.voiceDataStore.edit(block)
    }
}
