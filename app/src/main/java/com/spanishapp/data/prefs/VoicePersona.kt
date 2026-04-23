package com.spanishapp.data.prefs

enum class VoiceSlot { FEMALE_1, FEMALE_2, MALE_1, MALE_2 }

// Порядок: Sofía (Ж молодая), Pablo (М молодой), Carmen (Ж зрелая), Carlos (М зрелый)
data class VoicePersona(
    val id: String,
    val displayName: String,
    val tagline: String,      // short card subtitle
    val description: String,  // shown in tune sheet
    val slot: VoiceSlot,
    val pitch: Float,
    val rate: Float
)

object VoicePersonas {

    const val DEFAULT_ID = "carmen"

    val ALL: List<VoicePersona> = listOf(
        VoicePersona(
            id          = "sofia",
            displayName = "Sofía",
            tagline     = "Живая и современная",
            description = "Говорит бодро и чётко. Хороша для быстрого запоминания слов.",
            slot        = VoiceSlot.FEMALE_1,
            pitch       = 1.12f,
            rate        = 1.05f
        ),
        VoicePersona(
            id          = "pablo",
            displayName = "Pablo",
            tagline     = "Дружелюбный репетитор",
            description = "Живой и разговорный. Объясняет легко, без пафоса.",
            slot        = VoiceSlot.MALE_1,
            pitch       = 1.00f,
            rate        = 1.00f
        ),
        VoicePersona(
            id          = "carmen",
            displayName = "Carmen",
            tagline     = "Опытный преподаватель",
            description = "Спокойная, чёткая дикция. Идеальна для работы над произношением.",
            slot        = VoiceSlot.FEMALE_2,
            pitch       = 0.96f,
            rate        = 0.88f
        ),
        VoicePersona(
            id          = "carlos",
            displayName = "Carlos",
            tagline     = "Профессор испанистики",
            description = "Неторопливый и вдумчивый. Каждое слово — с расстановкой.",
            slot        = VoiceSlot.MALE_2,
            pitch       = 1.00f,
            rate        = 0.80f
        ),
    )

    fun byId(id: String?): VoicePersona =
        ALL.firstOrNull { it.id == id } ?: ALL.first { it.id == DEFAULT_ID }
}
