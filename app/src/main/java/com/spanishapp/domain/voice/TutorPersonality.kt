package com.spanishapp.domain.voice

/**
 * v1.18.29: каталог премиум-голосов через Edge TTS (Azure Neural quality).
 *
 * Юзер выбирает 1 русский + 1 испанский голос в Settings → Голос диктора.
 * Эти два голоса применяются везде в приложении (AI Chat + курсы + игры).
 *
 * Все 8 голосов — Azure Neural качество, бесплатно через unofficial
 * Microsoft endpoint (тот же что Edge browser использует для Read aloud).
 */
object PremiumVoiceCatalog {
    data class Voice(
        val id: String,
        val displayName: String,
        val description: String,
        val isMale: Boolean,
    )

    val RU_VOICES = listOf(
        Voice(
            id = "ru-RU-Wavenet-B",
            displayName = "Дмитрий",
            description = "Низкий, спокойный, формальный",
            isMale = true,
        ),
        Voice(
            id = "ru-RU-Wavenet-D",
            displayName = "Михаил",
            description = "Энергичный, средний тембр",
            isMale = true,
        ),
        Voice(
            id = "ru-RU-Wavenet-A",
            displayName = "Анна",
            description = "Живая, бодрая, средне-высокая",
            isMale = false,
        ),
        Voice(
            id = "ru-RU-Wavenet-C",
            displayName = "Мария",
            description = "Тёплая, мягкая, чуть ниже",
            isMale = false,
        ),
    )

    val ES_VOICES = listOf(
        Voice(
            id = "es-ES-Polyglot-1",
            displayName = "Carlos",
            description = "Глубокий, многоязычный — премиум",
            isMale = true,
        ),
        Voice(
            id = "es-ES-Neural2-B",
            displayName = "Pablo",
            description = "Молодой, естественный",
            isMale = true,
        ),
        Voice(
            id = "es-ES-Neural2-D",
            displayName = "Lucía",
            description = "Ясная, живая, средне-высокая",
            isMale = false,
        ),
        Voice(
            id = "es-ES-Wavenet-C",
            displayName = "Sofía",
            description = "Мягкая, тёплая, чуть ниже",
            isMale = false,
        ),
    )

    // Defaults — friendly female для нового юзера
    const val DEFAULT_RU_VOICE = "ru-RU-Wavenet-A"
    const val DEFAULT_ES_VOICE = "es-ES-Neural2-D"

    fun ruVoiceById(id: String?): Voice =
        RU_VOICES.firstOrNull { it.id == id } ?: RU_VOICES.firstOrNull { it.id == DEFAULT_RU_VOICE }!!

    fun esVoiceById(id: String?): Voice =
        ES_VOICES.firstOrNull { it.id == id } ?: ES_VOICES.firstOrNull { it.id == DEFAULT_ES_VOICE }!!
}

// ── Legacy compat — TutorPersonality оставлен как stub т.к. на него
// ссылаются AiChatRepository и старые prefs. Все методы возвращают
// нейтральные значения. Будет удалено когда AiChat refactor завершён.
@Suppress("unused")
enum class TutorPersonality(val id: String) {
    STRICT("strict"),
    POLITE("polite"),
    FRIENDLY("friendly"),
    ROMANTIC("romantic");

    val displayName: String get() = "Дружелюбный"
    val emoji: String get() = "😊"
    val description: String get() = "Универсальный"
    val speed: Float get() = 1.0f
    val pitch: Float get() = 0f
    val toneInstructions: String get() = ""

    fun esVoice(gender: VoiceGender): String = PremiumVoiceCatalog.DEFAULT_ES_VOICE
    fun ruVoice(gender: VoiceGender): String = PremiumVoiceCatalog.DEFAULT_RU_VOICE

    companion object {
        val DEFAULT = FRIENDLY
        fun byId(id: String?): TutorPersonality =
            entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}

@Suppress("unused")
enum class VoiceGender(val id: String) {
    FEMALE("female"),
    MALE("male");

    companion object {
        val DEFAULT = FEMALE
        fun byId(id: String?): VoiceGender =
            entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}
