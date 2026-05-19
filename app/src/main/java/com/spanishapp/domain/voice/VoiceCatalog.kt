package com.spanishapp.domain.voice

import android.speech.tts.Voice
import java.util.Locale

enum class Gender { MALE, FEMALE, UNKNOWN }

data class FriendlyVoice(
    val systemName: String,        // оригинальное имя из Voice.name
    val displayName: String,       // «Кармен»
    val region: String,            // «Испания», «Мексика», ...
    val flag: String,              // 🇪🇸
    val gender: Gender,
    val isHighQuality: Boolean,    // VERY_HIGH или HIGH
    val isNeural: Boolean          // wavenet/neural/network
)

object VoiceCatalog {

    // Эвристика для имени и пола Google TTS-голосов.
    // Имена системных голосов: "es-es-x-eef-network", "es-us-x-esd-local" и т.п.
    //
    // 1.1.1: расширенные подсказки. Раньше «Хорхе» (Jorge) показывался без
    // пола (Gender.UNKNOWN) → отображалось «Голос» вместо «Мужской голос».
    // Добавлены все коды Google TTS Spanish suffixes:
    //   eef/esd/esf/eed = female
    //   eem/esm/esa/esb/esc/eea/eec = male (определено эмпирически)
    // v1.18.11: чистка mappings — оставлены ТОЛЬКО проверенные суффиксы.
    // Юзер сообщил «Пабло — голос женский, имя мужское». eec/eea/een
    // ранее были помечены как male, но Google TTS изменил их на female
    // в свежих voice packs. Эти три суффикса теперь в FEMALE_HINTS.
    private val FEMALE_HINTS = setOf(
        "eef", "eed", "eea", "eec", "een",  // es-ES female (Google TTS обновлённый mapping)
        "esd", "esf",                        // es-US female
        "ana", "lupe", "marisol", "valentina", "mia", "sofia",
        "carmen", "elena", "lucia", "lusia", "maria",
    )
    private val MALE_HINTS = setOf(
        "eem",                               // es-ES male (единственный надёжный)
        "esm", "esa", "esb", "esc",          // es-US male
        "diego", "carlos", "miguel", "jorge", "хорхе",
        "andres", "andrés", "pablo", "antonio", "alberto",
        "luis", "javier",
    )

    private val REGION_NAMES = mapOf(
        "ES" to ("Испания (Кастильский)" to "🇪🇸"),
        "MX" to ("Мексика" to "🇲🇽"),
        "US" to ("США (Латино)" to "🇺🇸"),
        "AR" to ("Аргентина" to "🇦🇷"),
        "CO" to ("Колумбия" to "🇨🇴"),
        "PE" to ("Перу" to "🇵🇪"),
        "CL" to ("Чили" to "🇨🇱"),
        "VE" to ("Венесуэла" to "🇻🇪")
    )

    // v1.18.11: дружелюбные имена обновлены под актуальный Google TTS mapping.
    // Раньше eec→Пабло, eea→Антонио, een→Альберто (мужские) — но Google
    // изменил их на женские. Соответствующие имена тоже изменены на женские,
    // чтобы display name всегда совпадал с фактическим гендером голоса.
    private val FRIENDLY_NAMES = mapOf(
        // es-ES
        "es-es-x-eef" to "Кармен",   // female (стабильно)
        "es-es-x-eed" to "Лусия",    // female (стабильно)
        "es-es-x-eea" to "Елена",    // female (было «Антонио» — неверно)
        "es-es-x-een" to "Изабель",  // female (было «Альберто» — неверно)
        "es-es-x-eec" to "Пабла",    // female (было «Пабло» — неверно; женский вариант)
        "es-es-x-eem" to "Карлос",   // male (единственный мужской в es-ES)
        // es-US (латино)
        "es-us-x-esd" to "София",
        "es-us-x-esf" to "Лупе",
        "es-us-x-esa" to "Диего",
        "es-us-x-esb" to "Мигель",
        "es-us-x-esc" to "Хорхе",
        "es-us-x-ese" to "Валентина"
    )

    fun toFriendly(voice: Voice): FriendlyVoice {
        val name = voice.name
        val country = voice.locale.country.ifEmpty { "ES" }.uppercase()
        val (region, flag) = REGION_NAMES[country] ?: ("Испанский" to "🌍")

        // Гендер: ищем подсказки в имени
        val nameLower = name.lowercase()
        val gender = when {
            FEMALE_HINTS.any { nameLower.contains(it) } -> Gender.FEMALE
            MALE_HINTS.any { nameLower.contains(it)   } -> Gender.MALE
            else -> Gender.UNKNOWN
        }

        // Дружелюбное имя ищем по префиксу
        val friendly = FRIENDLY_NAMES.entries
            .firstOrNull { nameLower.startsWith(it.key) }
            ?.value
            ?: defaultName(gender, country)

        val isNeural = nameLower.let {
            it.contains("network") || it.contains("wavenet") ||
            it.contains("neural")  || it.contains("hd")
        }

        // Quality в Android TTS: VERY_HIGH=500, HIGH=400, NORMAL=300
        val isHighQuality = voice.quality >= 400 || isNeural

        return FriendlyVoice(
            systemName    = name,
            displayName   = friendly,
            region        = region,
            flag          = flag,
            gender        = gender,
            isHighQuality = isHighQuality,
            isNeural      = isNeural
        )
    }

    private fun defaultName(gender: Gender, country: String): String {
        return when (gender) {
            Gender.FEMALE  -> if (country == "ES") "Голос (ж)" else "Voz (f)"
            Gender.MALE    -> if (country == "ES") "Голос (м)" else "Voz (m)"
            Gender.UNKNOWN -> "Испанский голос"
        }
    }

    /** Сортируем голоса так, чтобы лучшие были сверху списка. */
    fun rank(voice: Voice): Int {
        val friendly = toFriendly(voice)
        var score = voice.quality
        if (friendly.isNeural) score += 50
        if (friendly.gender != Gender.UNKNOWN) score += 5
        return score
    }
}
