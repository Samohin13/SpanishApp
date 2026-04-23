package com.spanishapp.data.prefs

enum class VoiceSlot { FEMALE_1, FEMALE_2, MALE_1, MALE_2 }

enum class VoiceCategory(val title: String) {
    GIRL("Девушки"),
    WOMAN("Женщины"),
    BOY("Юноши"),
    MAN("Мужчины")
}

data class VoicePersona(
    val id: String,
    val displayName: String,
    val category: VoiceCategory,
    val description: String,
    val slot: VoiceSlot,
    val pitch: Float,
    val rate: Float
)

object VoicePersonas {

    const val DEFAULT_ID = "maria"

    val ALL: List<VoicePersona> = listOf(
        // Females — natural bright range
        VoicePersona("lucia",     "Lucía",     VoiceCategory.GIRL,  "Лёгкая, энергичная",    VoiceSlot.FEMALE_1, pitch = 1.15f, rate = 1.05f),
        VoicePersona("sofia",     "Sofía",     VoiceCategory.GIRL,  "Мягкая, нежная",        VoiceSlot.FEMALE_2, pitch = 1.08f, rate = 0.95f),
        VoicePersona("maria",     "María",     VoiceCategory.WOMAN, "Учитель, добрая",       VoiceSlot.FEMALE_1, pitch = 1.00f, rate = 0.88f),
        VoicePersona("valentina", "Valentina", VoiceCategory.WOMAN, "Уверенная, спокойная",  VoiceSlot.FEMALE_2, pitch = 0.95f, rate = 0.88f),
        // Males — natural lower range to stay human (pitch below ~0.80 on a female
        // base voice starts sounding robotic). Character comes from rate + slot.
        VoicePersona("mateo",     "Mateo",     VoiceCategory.BOY,   "Живой, бодрый",         VoiceSlot.MALE_1,   pitch = 0.93f, rate = 1.00f),
        VoicePersona("diego",     "Diego",     VoiceCategory.BOY,   "Разговорный, тёплый",   VoiceSlot.MALE_2,   pitch = 0.90f, rate = 0.95f),
        VoicePersona("carlos",    "Carlos",    VoiceCategory.MAN,   "Профессор, добрый",     VoiceSlot.MALE_1,   pitch = 0.85f, rate = 0.83f),
        VoicePersona("santiago",  "Santiago",  VoiceCategory.MAN,   "Учитель, спокойный",    VoiceSlot.MALE_2,   pitch = 0.82f, rate = 0.82f),
    )

    fun byId(id: String?): VoicePersona =
        ALL.firstOrNull { it.id == id } ?: ALL.first { it.id == DEFAULT_ID }
}
