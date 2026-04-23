package com.spanishapp.data.prefs

/**
 * Fixed Neural2 voice personas. Voice names map directly to Google Cloud TTS Neural2 voices.
 * Gender confirmed from Google's official voice list.
 */
data class VoicePersona(
    val id: String,
    val displayName: String,
    val cloudVoiceName: String,   // Google Cloud TTS Neural2 voice name
    val isMale: Boolean
)

object VoicePersonas {

    const val DEFAULT_ID = "carmen"

    val ALL: List<VoicePersona> = listOf(
        // ── Female (Spain Spanish) ──────────────────────────────
        VoicePersona("sofia",     "Sofía",     "es-ES-Neural2-A", isMale = false),
        VoicePersona("carmen",    "Carmen",    "es-ES-Neural2-C", isMale = false),
        VoicePersona("valentina", "Valentina", "es-ES-Neural2-E", isMale = false),
        // ── Male (Spain + LatAm for vocal variety) ──────────────
        VoicePersona("pablo",  "Pablo",  "es-ES-Neural2-B", isMale = true),
        VoicePersona("carlos", "Carlos", "es-ES-Neural2-F", isMale = true),
        VoicePersona("diego",  "Diego",  "es-US-Neural2-B", isMale = true),
    )

    fun byId(id: String?): VoicePersona =
        ALL.firstOrNull { it.id == id } ?: ALL.first { it.id == DEFAULT_ID }

    fun byVoiceName(name: String?): VoicePersona? =
        ALL.firstOrNull { it.cloudVoiceName == name }
}
