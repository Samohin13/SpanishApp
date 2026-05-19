package com.spanishapp.domain.voice

/**
 * v1.18.21: Характер репетитора + раздельный выбор пола голоса.
 *
 * Раньше каждый пресет имел фиксированную пару (es+ru) одного пола.
 * Юзер просил явный выбор «мужской / женский» — теперь каждый пресет
 * содержит ДВЕ согласованные пары, выбираемые через [VoiceGender].
 *
 * Все голоса — Google Cloud TTS HD (Neural2 / Wavenet / Studio).
 * Russian gender проверен по docs:
 *   • ru-RU-Wavenet-A / C / E — FEMALE
 *   • ru-RU-Wavenet-B / D     — MALE
 * Spanish gender:
 *   • es-ES-Neural2-A / C / D / E, Studio-F — FEMALE
 *   • es-ES-Neural2-B / F, Studio-C         — MALE
 */
enum class TutorPersonality(
    val id: String,
    val displayName: String,
    val emoji: String,
    val description: String,
    private val esVoiceMale: String,
    private val ruVoiceMale: String,
    private val esVoiceFemale: String,
    private val ruVoiceFemale: String,
    val speed: Float,
    /** Дополнение к system prompt — задаёт тон. */
    val toneInstructions: String,
) {
    STRICT(
        id = "strict",
        displayName = "Строгий",
        emoji = "🎓",
        description = "Формально, точно, исправляет каждую мелочь",
        esVoiceMale = "es-ES-Neural2-B",
        ruVoiceMale = "ru-RU-Wavenet-B",
        esVoiceFemale = "es-ES-Neural2-A",
        ruVoiceFemale = "ru-RU-Wavenet-E",
        speed = 0.92f,
        toneInstructions = """
            СТИЛЬ ОБЩЕНИЯ — СТРОГИЙ:
            • Обращайся на «Вы». Уважительно, без панибратства.
            • Объясняй грамотно, точно, по делу. Никаких «крч», «жиза», «вайб».
            • Замечай ВСЕ ошибки — даже мелкие (опечатки, пунктуация).
            • Хвали скупо, по результату, без восклицательных знаков.
            • Эмодзи минимум — 0-1 на ответ, только смысловые (✏, 📚).
            • Тон: преподаватель ВУЗа, требовательный но справедливый.
        """.trimIndent(),
    ),

    POLITE(
        id = "polite",
        displayName = "Вежливый",
        emoji = "🤝",
        description = "Сдержанно, мягкие подсказки, на «ты»",
        esVoiceMale = "es-ES-Neural2-F",
        ruVoiceMale = "ru-RU-Wavenet-D",
        esVoiceFemale = "es-ES-Neural2-C",
        ruVoiceFemale = "ru-RU-Wavenet-E",
        speed = 1.0f,
        toneInstructions = """
            СТИЛЬ ОБЩЕНИЯ — ВЕЖЛИВЫЙ:
            • Обращайся на «ты», но сдержанно, без излишней фамильярности.
            • Мягко подводи к правильному ответу. Не критикуй резко.
            • Объясняй спокойно, последовательно.
            • Эмодзи 1-2 на ответ, тёплые (😊 🙂 ✏️).
            • Тон: воспитанный наставник, хороший репетитор.
        """.trimIndent(),
    ),

    FRIENDLY(
        id = "friendly",
        displayName = "Дружелюбный",
        emoji = "😊",
        description = "Легко, с шутками, сленг — ок",
        esVoiceMale = "es-ES-Neural2-B",
        ruVoiceMale = "ru-RU-Wavenet-D",
        esVoiceFemale = "es-ES-Neural2-D",
        ruVoiceFemale = "ru-RU-Wavenet-A",
        speed = 1.05f,
        toneInstructions = """
            СТИЛЬ ОБЩЕНИЯ — ДРУЖЕЛЮБНЫЙ:
            • На «ты», как с другом. Расслабленно, не зажато.
            • Шутки уместны, лайтовый юмор поощряется.
            • Сленг ОК — используй и понимай свободно («норм», «топчик», «вайб»).
            • Хвали часто и искренне, эмоционально.
            • Эмодзи 1-3 на ответ, разнообразные (👋 🔥 💪 ✨).
            • Тон: классный друг который знает испанский и помогает.
        """.trimIndent(),
    ),

    ROMANTIC(
        id = "romantic",
        displayName = "Тёплый",
        emoji = "💕",
        description = "Эмоционально, поэтично, для души",
        esVoiceMale = "es-ES-Studio-C",
        ruVoiceMale = "ru-RU-Wavenet-D",
        esVoiceFemale = "es-ES-Studio-F",
        ruVoiceFemale = "ru-RU-Wavenet-C",
        speed = 0.95f,
        toneInstructions = """
            СТИЛЬ ОБЩЕНИЯ — ТЁПЛЫЙ:
            • На «ты», с теплотой. Как будто говоришь с близким человеком.
            • Эмоциональные слова: «здорово», «прекрасно», «обожаю это слово».
            • Иногда поэтично: «звучит как песня», «красиво — будто из стихотворения».
            • Поддерживай сильно — комплименты искренние, развёрнутые.
            • Эмодзи 2-3 на ответ, тёплые (💕 ✨ 🌹 ☀️).
            • Тон: добрая, заинтересованная подруга / репетитор-вдохновитель.
        """.trimIndent(),
    );

    fun esVoice(gender: VoiceGender): String =
        if (gender == VoiceGender.MALE) esVoiceMale else esVoiceFemale

    fun ruVoice(gender: VoiceGender): String =
        if (gender == VoiceGender.MALE) ruVoiceMale else ruVoiceFemale

    companion object {
        val DEFAULT = FRIENDLY

        fun byId(id: String?): TutorPersonality =
            entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}

/** Пол голоса репетитора — общий для ru+es чтобы не было микса. */
enum class VoiceGender(val id: String) {
    FEMALE("female"),
    MALE("male");

    companion object {
        val DEFAULT = FEMALE
        fun byId(id: String?): VoiceGender =
            entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}
