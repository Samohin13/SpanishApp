package com.spanishapp.ui.settings

import android.speech.tts.Voice
import com.spanishapp.data.prefs.VoiceSlot

/**
 * Resolves [VoiceSlot]s to actual installed TTS voices.
 *
 * resolve() uses direct code-pattern lookup against the voice name (e.g. "sfec" in
 * "es-es-x-sfec-local"). This mirrors how Google names its voices and avoids fragile
 * gender heuristics. Patterns are tried in priority order; first match wins.
 *
 * classifyStrict() is used only for the diagnostics banner.
 */
object VoiceSlotResolver {

    // Priority-ordered code fragments to look for in voice names per slot.
    // Female slots target a/b/e/f… variants; male slots target c/d/g/h… variants.
    // Each slot has its own first-choice code so FEMALE_1 ≠ FEMALE_2 when two female voices exist.
    private val SLOT_PATTERNS = mapOf(
        VoiceSlot.FEMALE_1 to listOf("sfea", "eea", "eef", "esc", "esh", "sfia"),
        VoiceSlot.FEMALE_2 to listOf("sfeb", "eeb", "eeg", "esf", "esi", "sfib"),
        VoiceSlot.MALE_1   to listOf("sfec", "eec", "eeh", "esd", "esj", "sfic"),
        VoiceSlot.MALE_2   to listOf("sfed", "eed", "eei", "esg", "esk", "sfid"),
    )

    // Fallback: if pattern lookup finds nothing, use these
    private val FEMALE_CODES = setOf("sfea","sfeb","sfef","sfeg","eea","eeb","eef","eeg","esc","esf","esh","esi")
    private val MALE_CODES   = setOf("sfec","sfed","sfeh","sfei","eec","eed","eeh","eei","esd","esg","esj","esk")

    data class Classification(val female: List<String>, val male: List<String>, val unknown: List<String>)

    fun classifyStrict(voices: List<Voice>): Classification {
        val female  = mutableListOf<String>()
        val male    = mutableListOf<String>()
        val unknown = mutableListOf<String>()
        for (v in voices) when (genderOf(v)) {
            Gender.FEMALE  -> female  += v.name
            Gender.MALE    -> male    += v.name
            Gender.UNKNOWN -> unknown += v.name
        }
        return Classification(female, male, unknown)
    }

    fun resolve(slot: VoiceSlot, voices: List<Voice>): Voice? {
        if (voices.isEmpty()) return null
        val sorted = voices.sortedBy { it.name }

        // 1. Try each priority pattern for this slot
        for (pattern in SLOT_PATTERNS[slot].orEmpty()) {
            val match = sorted.firstOrNull { voice ->
                extractCode(voice.name)?.contains(pattern) == true ||
                voice.name.lowercase().contains(pattern)
            }
            if (match != null) return match
        }

        // 2. Fallback: pick any voice of the right gender class
        val isFemaleSlot = slot == VoiceSlot.FEMALE_1 || slot == VoiceSlot.FEMALE_2
        val femaleVoices = sorted.filter { genderOf(it) == Gender.FEMALE }
        val maleVoices   = sorted.filter { genderOf(it) == Gender.MALE }

        return if (isFemaleSlot) {
            val idx = if (slot == VoiceSlot.FEMALE_2) 1 else 0
            femaleVoices.getOrNull(idx) ?: femaleVoices.firstOrNull()
                ?: sorted.firstOrNull()
        } else {
            val idx = if (slot == VoiceSlot.MALE_2) 1 else 0
            maleVoices.getOrNull(idx) ?: maleVoices.firstOrNull()
                ?: sorted.lastOrNull()
        }
    }

    private fun extractCode(name: String): String? =
        Regex("-x-([a-z]+)-").find(name.lowercase())?.groupValues?.getOrNull(1)

    private enum class Gender { FEMALE, MALE, UNKNOWN }

    private fun genderOf(v: Voice): Gender {
        val n = v.name.lowercase()
        if (n.contains("female")) return Gender.FEMALE
        if (n.contains("male"))   return Gender.MALE
        val code = extractCode(n) ?: return Gender.UNKNOWN
        return when {
            FEMALE_CODES.any { code.startsWith(it) } -> Gender.FEMALE
            MALE_CODES.any   { code.startsWith(it) } -> Gender.MALE
            code.last() in setOf('a','b','e','f','i','j','m','n') -> Gender.FEMALE
            code.last() in setOf('c','d','g','h','k','l','o','p') -> Gender.MALE
            else -> Gender.UNKNOWN
        }
    }
}
