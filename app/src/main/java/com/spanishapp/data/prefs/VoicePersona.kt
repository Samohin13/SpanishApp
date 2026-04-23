package com.spanishapp.data.prefs

/**
 * Fixed Neural2 voice personas. Voice names map directly to Google Cloud TTS Neural2 voices.
 * Gender confirmed from Google's official voice list.
 */
data class VoicePersona(
    val id: String,
    val displayName: String,
    val cloudVoiceName: String,   // Google Cloud TTS Neural2 voice name
    val isMale: Boolean,
    // Android TTS fallback — used when Google Cloud TTS is unavailable
    val fallbackPitch: Float = 1.0f,
    val fallbackRate: Float  = 0.9f
)

object VoicePersonas {

    const val DEFAULT_ID = "carmen"

    val ALL: List<VoicePersona> = listOf(
        // ── Female (Spain Spanish) ──────────────────────────────
        VoicePersona("sofia",     "Sofía",     "es-ES-Neural2-A", isMale = false, fallbackPitch = 1.20f, fallbackRate = 0.95f),
        VoicePersona("carmen",    "Carmen",    "es-ES-Neural2-C", isMale = false, fallbackPitch = 1.10f, fallbackRate = 0.90f),
        VoicePersona("valentina", "Valentina", "es-ES-Neural2-E", isMale = false, fallbackPitch = 1.15f, fallbackRate = 1.00f),
        // ── Male (Spain + LatAm for vocal variety) ──────────────
        VoicePersona("pablo",  "Pablo",  "es-ES-Neural2-B", isMale = true, fallbackPitch = 0.80f, fallbackRate = 0.95f),
        VoicePersona("carlos", "Carlos", "es-ES-Neural2-F", isMale = true, fallbackPitch = 0.72f, fallbackRate = 0.90f),
        VoicePersona("diego",  "Diego",  "es-US-Neural2-B", isMale = true, fallbackPitch = 0.85f, fallbackRate = 1.00f),
    )

    fun byId(id: String?): VoicePersona =
        ALL.firstOrNull { it.id == id } ?: ALL.first { it.id == DEFAULT_ID }

    fun byVoiceName(name: String?): VoicePersona? =
        ALL.firstOrNull { it.cloudVoiceName == name }
}
