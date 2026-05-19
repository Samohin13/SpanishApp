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

    // v1.18.12: расширенный mapping + проверенные суффиксы Google TTS 2024.
    // Все es-ES voices у Google сейчас FEMALE (мужские были удалены в 2023).
    // Если в системе есть мужской — это сторонний TTS engine.
    private val FRIENDLY_NAMES = mapOf(
        // es-ES (Google — все female в 2024)
        "es-es-x-eef" to "Кармен",
        "es-es-x-eed" to "Лусия",
        "es-es-x-eea" to "Елена",
        "es-es-x-een" to "Изабель",
        "es-es-x-eec" to "Софи́я",
        "es-es-x-eem" to "Мария",
        "es-es-x-ana" to "Ана",
        "es-es-x-elf" to "Эльвира",
        "es-es-x-axb" to "Беатрис",
        "es-es-x-eed-network" to "Лусия",
        "es-es-x-eef-local" to "Кармен",
        // es-US (латино)
        "es-us-x-esd" to "София",
        "es-us-x-esf" to "Лупе",
        "es-us-x-esa" to "Диего",
        "es-us-x-esb" to "Мигель",
        "es-us-x-esc" to "Хорхе",
        "es-us-x-ese" to "Валентина",
    )

    /**
     * v1.18.12: pool красивых женских имён для fallback. Если voice не в
     * FRIENDLY_NAMES — выбираем стабильно по hashCode имени системы.
     * Это убирает уродливый «Испанский голос» / «Голос (ж)».
     */
    private val FEMALE_POOL_ES = listOf(
        "Андреа", "Беатрис", "Кларита", "Долорес", "Эулалия",
        "Фернанда", "Габриэла", "Хулия", "Лаура", "Марина",
        "Нурия", "Палома", "Росита", "Сара", "Тереса"
    )
    private val MALE_POOL_ES = listOf(
        "Адриан", "Бруно", "Сезар", "Даниэль", "Эстебан",
        "Фернандо", "Гонсало", "Хавьер", "Леонардо", "Маркос",
        "Николас", "Оскар", "Рауль", "Себастьян", "Виктор"
    )

    fun toFriendly(voice: Voice): FriendlyVoice {
        val name = voice.name
        val country = voice.locale.country.ifEmpty { "ES" }.uppercase()
        val (region, flag) = REGION_NAMES[country] ?: ("Испанский" to "🌍")

        // Гендер: ищем подсказки в имени.
        // v1.18.12: для es-ES если не найдены подсказки — default FEMALE
        // (Google TTS убрал все мужские es-ES в 2023, осталось только female).
        val nameLower = name.lowercase()
        val gender = when {
            FEMALE_HINTS.any { nameLower.contains(it) } -> Gender.FEMALE
            MALE_HINTS.any { nameLower.contains(it)   } -> Gender.MALE
            country == "ES" -> Gender.FEMALE  // safe default для Испании
            else -> Gender.UNKNOWN
        }

        // Дружелюбное имя — сначала маппинг, потом fallback из pool
        // по стабильному hashCode имени системы (один и тот же voice = одно и
        // то же имя при каждой загрузке).
        val mapped = FRIENDLY_NAMES.entries
            .firstOrNull { nameLower.startsWith(it.key) }
            ?.value
        val friendly = mapped ?: pickFromPool(name, gender)

        val isNeural = nameLower.let {
            it.contains("network") || it.contains("wavenet") ||
            it.contains("neural")  || it.contains("hd") ||
            it.contains("local")  // v1.18.12: local TTS pack на Android = HD
        }

        // Quality в Android TTS: VERY_HIGH=500, HIGH=400, NORMAL=300
        // v1.18.12: понижен порог до 300 — на Samsung даже NORMAL voices
        // звучат прилично если установлен HD pack.
        val isHighQuality = voice.quality >= 300 || isNeural

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

    /**
     * v1.18.12: выбирает имя из pool детерминистически на основе системного
     * имени voice. Один и тот же voice всегда получит одно имя.
     */
    private fun pickFromPool(systemName: String, gender: Gender): String {
        val pool = when (gender) {
            Gender.MALE -> MALE_POOL_ES
            else -> FEMALE_POOL_ES  // FEMALE и UNKNOWN → female pool
        }
        // Stable hash — одинаковый result для одного и того же systemName
        val idx = (systemName.hashCode().toLong() and 0x7FFFFFFFL) % pool.size
        return pool[idx.toInt()]
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
