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
        VoicePersona("lucia",     "Lucía",     VoiceCategory.GIRL,  "Лёгкая, энергичная",    VoiceSlot.FEMALE_1, pitch = 1.20f, rate = 1.05f),
        VoicePersona("sofia",     "Sofía",     VoiceCategory.GIRL,  "Мягкая, нежная",        VoiceSlot.FEMALE_2, pitch = 1.15f, rate = 0.95f),
        VoicePersona("maria",     "María",     VoiceCategory.WOMAN, "Учитель, спокойная",    VoiceSlot.FEMALE_1, pitch = 1.00f, rate = 0.90f),
        VoicePersona("valentina", "Valentina", VoiceCategory.WOMAN, "Уверенная, дикторская", VoiceSlot.FEMALE_2, pitch = 0.95f, rate = 0.90f),
        VoicePersona("mateo",     "Mateo",     VoiceCategory.BOY,   "Весёлый, быстрый",      VoiceSlot.MALE_1,   pitch = 1.15f, rate = 1.05f),
        VoicePersona("diego",     "Diego",     VoiceCategory.BOY,   "Разговорный, живой",    VoiceSlot.MALE_2,   pitch = 1.10f, rate = 1.00f),
        VoicePersona("carlos",    "Carlos",    VoiceCategory.MAN,   "Серьёзный, дикторский", VoiceSlot.MALE_1,   pitch = 0.95f, rate = 0.90f),
        VoicePersona("santiago",  "Santiago",  VoiceCategory.MAN,   "Низкий, глубокий",      VoiceSlot.MALE_2,   pitch = 0.85f, rate = 0.90f),
    )

    fun byId(id: String?): VoicePersona =
        ALL.firstOrNull { it.id == id } ?: ALL.first { it.id == DEFAULT_ID }
}
