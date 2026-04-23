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
    val voiceName: String? = null,       // null = default / auto
    val speechRate: Float = 0.9f,        // 0.5 .. 1.5
    val pitch: Float = 1.0f              // 0.5 .. 2.0
)

@Singleton
class VoicePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object K {
        val VOICE_NAME = stringPreferencesKey("voice_name")
        val RATE       = floatPreferencesKey("rate")
        val PITCH      = floatPreferencesKey("pitch")
    }

    val settings: Flow<VoiceSettings> = context.voiceDataStore.data.map { p ->
        VoiceSettings(
            voiceName  = p[K.VOICE_NAME],
            speechRate = p[K.RATE] ?: 0.9f,
            pitch      = p[K.PITCH] ?: 1.0f
        )
    }

    suspend fun setVoiceName(name: String?) = update {
        if (name == null) it.remove(K.VOICE_NAME) else it[K.VOICE_NAME] = name
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
