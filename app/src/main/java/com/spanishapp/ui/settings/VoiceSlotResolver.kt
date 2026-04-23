package com.spanishapp.ui.settings

import android.speech.tts.Voice
import com.spanishapp.data.prefs.VoiceSlot

/**
 * Picks the best available TTS voice for each persona slot.
 *
 * Priority: highest quality first (network > local), then alphabetical.
 * Female/male classification uses Google TTS code patterns in the voice name.
 * If gender can't be determined, splits the sorted list in half.
 */
object VoiceSlotResolver {

    private val FEMALE_CODES = setOf(
        "sfea","sfeb","sfef","sfeg",
        "eea","eeb","eef","eeg","esc","esf","esh","esi","esl","esm"
    )
    private val MALE_CODES = setOf(
        "sfec","sfed","sfeh","sfei",
        "eec","eed","eeh","eei","esd","esg","esj","esk","esn","eso"
    )

    fun resolve(slot: VoiceSlot, voices: List<Voice>): Voice? {
        if (voices.isEmpty()) return null

        val sorted = voices.sortedWith(
            compareByDescending<Voice> { it.quality }.thenBy { it.name }
        )

        val female = sorted.filter { classify(it) == Gender.FEMALE }
        val male   = sorted.filter { classify(it) == Gender.MALE   }

        // If gender detection found nothing, split sorted list in half
        val resolvedFemale = female.ifEmpty { sorted.take((sorted.size + 1) / 2) }
        val resolvedMale   = male.ifEmpty   { sorted.drop((sorted.size + 1) / 2).ifEmpty { sorted } }

        return when (slot) {
            VoiceSlot.FEMALE_1 -> resolvedFemale.getOrElse(0) { sorted.first() }
            VoiceSlot.FEMALE_2 -> resolvedFemale.getOrElse(1) { resolvedFemale.first() }
            VoiceSlot.MALE_1   -> resolvedMale.getOrElse(0)   { sorted.last() }
            VoiceSlot.MALE_2   -> resolvedMale.getOrElse(1)   { resolvedMale.first() }
        }
    }

    private enum class Gender { FEMALE, MALE, UNKNOWN }

    private fun classify(v: Voice): Gender {
        val n = v.name.lowercase()
        if (n.contains("female")) return Gender.FEMALE
        if (n.contains("male"))   return Gender.MALE
        val code = Regex("-x-([a-z]+)-").find(n)?.groupValues?.getOrNull(1) ?: return Gender.UNKNOWN
        return when {
            FEMALE_CODES.any { code.startsWith(it) } -> Gender.FEMALE
            MALE_CODES.any   { code.startsWith(it) } -> Gender.MALE
            else -> Gender.UNKNOWN
        }
    }
}
