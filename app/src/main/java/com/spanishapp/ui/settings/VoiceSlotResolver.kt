package com.spanishapp.ui.settings

import android.speech.tts.Voice
import com.spanishapp.data.prefs.VoiceSlot

/**
 * Resolves abstract [VoiceSlot]s (FEMALE_1/2, MALE_1/2) to actual installed TTS voices.
 *
 * Android's TTS API doesn't expose gender. We classify by:
 * 1. Explicit "female"/"male" substrings in the voice name
 * 2. Known Google TTS code prefixes (-eea/-eeb = female, -eec/-eed = male, and similar
 *    for LatAm Spanish -esc/-esd female, -esf/-esg male) — empirical convention
 * 3. Fallback: split the voice list in half, low half = female, high half = male
 */
object VoiceSlotResolver {

    // Empirical Google-TTS Spanish code prefixes. These are the common codes
    // seen on Android devices; not all are present on every phone.
    private val FEMALE_CODE_PREFIXES = listOf(
        "eea", "eeb", "eef", "eeg", "esc", "esf", "esh", "esi", "esl", "esm"
    )
    private val MALE_CODE_PREFIXES = listOf(
        "eec", "eed", "eeh", "eei", "esd", "esg", "esj", "esk", "esn", "eso"
    )

    data class Classification(val female: List<String>, val male: List<String>, val unknown: List<String>)

    /** Strict classification — no fallback, only voices we confidently identified. */
    fun classifyStrict(voices: List<Voice>): Classification {
        val female = mutableListOf<String>()
        val male   = mutableListOf<String>()
        val unknown = mutableListOf<String>()
        for (v in voices) when (genderOf(v)) {
            Gender.FEMALE  -> female += v.name
            Gender.MALE    -> male   += v.name
            Gender.UNKNOWN -> unknown += v.name
        }
        return Classification(female, male, unknown)
    }

    fun resolve(slot: VoiceSlot, voices: List<Voice>): Voice? {
        if (voices.isEmpty()) return null
        val (female, male) = classify(voices)

        return when (slot) {
            VoiceSlot.FEMALE_1 -> female.getOrNull(0) ?: female.firstOrNull() ?: voices.first()
            VoiceSlot.FEMALE_2 -> female.getOrNull(1) ?: female.firstOrNull() ?: voices.first()
            VoiceSlot.MALE_1   -> male.getOrNull(0)   ?: male.firstOrNull()   ?: voices.last()
            VoiceSlot.MALE_2   -> male.getOrNull(1)   ?: male.firstOrNull()   ?: voices.last()
        }
    }

    private fun classify(voices: List<Voice>): Pair<List<Voice>, List<Voice>> {
        val female = mutableListOf<Voice>()
        val male   = mutableListOf<Voice>()
        val unknown = mutableListOf<Voice>()

        for (v in voices) {
            when (genderOf(v)) {
                Gender.FEMALE -> female += v
                Gender.MALE   -> male   += v
                Gender.UNKNOWN -> unknown += v
            }
        }

        // If we have neither bucket classified, split evenly (low names = female, high = male)
        if (female.isEmpty() && male.isEmpty() && unknown.size >= 2) {
            val half = (unknown.size + 1) / 2
            return unknown.sortedBy { it.name }.take(half) to unknown.sortedBy { it.name }.drop(half)
        }

        // Distribute unknowns to the smaller bucket first (keeps gender diversity)
        for (v in unknown) {
            if (female.size <= male.size) female += v else male += v
        }

        return female to male
    }

    private enum class Gender { FEMALE, MALE, UNKNOWN }

    private fun genderOf(v: Voice): Gender {
        val n = v.name.lowercase()
        if (n.contains("female")) return Gender.FEMALE
        if (n.contains("male"))   return Gender.MALE
        // Google pattern: es-es-x-XXX-... — grab the code between -x- and next -
        val code = Regex("-x-([a-z]+)-").find(n)?.groupValues?.getOrNull(1)
        if (code != null) {
            if (FEMALE_CODE_PREFIXES.any { code.startsWith(it) }) return Gender.FEMALE
            if (MALE_CODE_PREFIXES.any   { code.startsWith(it) }) return Gender.MALE
        }
        return Gender.UNKNOWN
    }
}
