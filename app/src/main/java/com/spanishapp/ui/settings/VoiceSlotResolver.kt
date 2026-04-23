package com.spanishapp.ui.settings

import android.speech.tts.Voice
import com.spanishapp.data.prefs.VoiceSlot

/**
 * Resolves abstract [VoiceSlot]s (FEMALE_1/2, MALE_1/2) to actual installed TTS voices.
 *
 * Gender detection priority:
 * 1. "female" / "male" substring in voice name (most reliable)
 * 2. Known Google TTS code prefixes in -x-<code>- segment
 * 3. Last-character heuristic on the code: a/b/e/f… → female, c/d/g/h… → male
 *    (Google encodes variants in alphabetical pairs: aa/ab = female, ac/ad = male)
 * 4. Fallback: split list in half (low half = female, high half = male)
 */
object VoiceSlotResolver {

    // Google TTS Spanish code prefixes — classic (ee*) and newer (sf*) naming.
    private val FEMALE_CODE_PREFIXES = listOf(
        // Spain Spanish (es-es)
        "eea", "eeb", "eef", "eeg", "eej", "eek",
        // LatAm Spanish (es-us)
        "esc", "esf", "esh", "esi", "esl", "esm",
        // Newer Google TTS format (2023+)
        "sfea", "sfeb", "sfef", "sfeg", "sfia", "sfib"
    )
    private val MALE_CODE_PREFIXES = listOf(
        // Spain Spanish
        "eec", "eed", "eeh", "eei", "eel", "eem",
        // LatAm Spanish
        "esd", "esg", "esj", "esk", "esn", "eso",
        // Newer Google TTS format (2023+)
        "sfec", "sfed", "sfeh", "sfei", "sfic", "sfid"
    )

    // Chars at even pair-positions (0,1 mod 4) tend to be female in Google naming.
    private val FEMALE_LAST_CHARS = setOf('a', 'b', 'e', 'f', 'i', 'j', 'm', 'n', 'q', 'r')
    private val MALE_LAST_CHARS   = setOf('c', 'd', 'g', 'h', 'k', 'l', 'o', 'p', 's', 't')

    data class Classification(val female: List<String>, val male: List<String>, val unknown: List<String>)

    /** Strict classification — only voices we confidently identified (no fallback). */
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
        val (female, male) = classify(voices)

        return when (slot) {
            VoiceSlot.FEMALE_1 -> female.getOrNull(0) ?: female.firstOrNull() ?: voices.first()
            VoiceSlot.FEMALE_2 -> female.getOrNull(1) ?: female.firstOrNull() ?: voices.first()
            VoiceSlot.MALE_1   -> male.getOrNull(0)   ?: male.firstOrNull()   ?: voices.last()
            VoiceSlot.MALE_2   -> male.getOrNull(1)   ?: male.firstOrNull()   ?: voices.last()
        }
    }

    private fun classify(voices: List<Voice>): Pair<List<Voice>, List<Voice>> {
        val female  = mutableListOf<Voice>()
        val male    = mutableListOf<Voice>()
        val unknown = mutableListOf<Voice>()

        for (v in voices) {
            when (genderOf(v)) {
                Gender.FEMALE  -> female  += v
                Gender.MALE    -> male    += v
                Gender.UNKNOWN -> unknown += v
            }
        }

        // No voices classified at all → split evenly
        if (female.isEmpty() && male.isEmpty() && unknown.size >= 2) {
            val half = (unknown.size + 1) / 2
            return unknown.sortedBy { it.name }.take(half) to
                   unknown.sortedBy { it.name }.drop(half)
        }

        // Distribute remaining unknowns to keep gender diversity
        for (v in unknown) {
            if (female.size <= male.size) female += v else male += v
        }

        return female to male
    }

    private enum class Gender { FEMALE, MALE, UNKNOWN }

    private fun genderOf(v: Voice): Gender {
        val n = v.name.lowercase()

        // 1. Explicit keyword
        if (n.contains("female")) return Gender.FEMALE
        if (n.contains("male"))   return Gender.MALE

        // 2. Google -x-<code>- segment
        val code = Regex("-x-([a-z]+)-").find(n)?.groupValues?.getOrNull(1)
        if (code != null) {
            if (FEMALE_CODE_PREFIXES.any { code.startsWith(it) }) return Gender.FEMALE
            if (MALE_CODE_PREFIXES.any   { code.startsWith(it) }) return Gender.MALE

            // 3. Last-char heuristic — covers unknown prefix variants
            val last = code.last()
            if (last in FEMALE_LAST_CHARS) return Gender.FEMALE
            if (last in MALE_LAST_CHARS)   return Gender.MALE
        }

        return Gender.UNKNOWN
    }
}
