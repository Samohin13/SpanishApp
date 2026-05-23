package com.spanishapp.domain.checkpoint

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Информация о стране юзера для подмены в чекпоинте.
 * Содержит исп. название страны + национальность в м.р. и ж.р.
 *
 * Используется чтобы юзер из Казахстана не видел везде «Я русская из России»,
 * а играл за Andrea kazaja de Kazajistán.
 */
data class CountryInfo(
    val isoCode: String,
    val countryEs: String,
    val nationalityM: String,
    val nationalityF: String,
)

/**
 * Маппинг ISO-кодов стран → испанские названия + национальности.
 *
 * v1.22.14: покрытие СНГ + основные испаноязычные + Европа/США.
 * Юзеры из стран не в списке получают дефолтный персонаж (Rusia/rusa) —
 * это не блокирует игру.
 */
object CountryMap {
    private val data = listOf(
        // СНГ + Россия
        CountryInfo("RU", "Rusia",       "ruso",         "rusa"),
        CountryInfo("BY", "Bielorrusia", "bielorruso",   "bielorrusa"),
        CountryInfo("UA", "Ucrania",     "ucraniano",    "ucraniana"),
        CountryInfo("KZ", "Kazajistán",  "kazajo",       "kazaja"),
        CountryInfo("KG", "Kirguistán",  "kirguís",      "kirguisa"),
        CountryInfo("UZ", "Uzbekistán",  "uzbeko",       "uzbeka"),
        CountryInfo("TJ", "Tayikistán",  "tayiko",       "tayika"),
        CountryInfo("TM", "Turkmenistán","turcomano",    "turcomana"),
        CountryInfo("AM", "Armenia",     "armenio",      "armenia"),
        CountryInfo("GE", "Georgia",     "georgiano",    "georgiana"),
        CountryInfo("AZ", "Azerbaiyán",  "azerbaiyano",  "azerbaiyana"),
        CountryInfo("MD", "Moldavia",    "moldavo",      "moldava"),
        // Испаноязычные
        CountryInfo("ES", "España",      "español",      "española"),
        CountryInfo("MX", "México",      "mexicano",     "mexicana"),
        CountryInfo("AR", "Argentina",   "argentino",    "argentina"),
        CountryInfo("CO", "Colombia",    "colombiano",   "colombiana"),
        CountryInfo("CL", "Chile",       "chileno",      "chilena"),
        CountryInfo("PE", "Perú",        "peruano",      "peruana"),
        CountryInfo("VE", "Venezuela",   "venezolano",   "venezolana"),
        CountryInfo("EC", "Ecuador",     "ecuatoriano",  "ecuatoriana"),
        CountryInfo("CU", "Cuba",        "cubano",       "cubana"),
        // Европа + США
        CountryInfo("FR", "Francia",     "francés",      "francesa"),
        CountryInfo("GB", "Inglaterra",  "inglés",       "inglesa"),
        CountryInfo("US", "Estados Unidos","estadounidense","estadounidense"),
        CountryInfo("DE", "Alemania",    "alemán",       "alemana"),
        CountryInfo("IT", "Italia",      "italiano",     "italiana"),
        CountryInfo("PL", "Polonia",     "polaco",       "polaca"),
        CountryInfo("TR", "Turquía",     "turco",        "turca"),
        CountryInfo("BR", "Brasil",      "brasileño",    "brasileña"),
    )
    private val byIso = data.associateBy { it.isoCode }

    /** Дефолт — Россия / рус. */
    val DEFAULT = data.first()

    fun byIsoCode(code: String?): CountryInfo {
        if (code.isNullOrBlank()) return DEFAULT
        return byIso[code.trim().uppercase()] ?: DEFAULT
    }
}

/**
 * Применяет персонализацию страны/национальности к контенту чекпоинта.
 *
 * Подменяет в строках всех раундов:
 *   "Rusia" → countryEs
 *   "rusa"  → nationalityF  (ж.р.)
 *   "ruso"  → nationalityM  (м.р.)
 *
 * Не трогает дистракторы типа «Soy español», «Soy de Inglaterra» —
 * они и должны оставаться неверными вариантами разных национальностей.
 *
 * Идемпотентно — повторное применение не ломает. Возвращает новую копию
 * CheckpointData (исходный JSON не модифицируется).
 */
@Singleton
class CheckpointPersonalizer @Inject constructor() {

    fun personalize(data: CheckpointData, country: CountryInfo): CheckpointData {
        if (country.isoCode == "RU") return data   // дефолт уже совпадает, no-op

        return data.copy(
            rounds = data.rounds.map { round -> personalizeRound(round, country) }
        )
    }

    private fun personalizeRound(r: CheckpointRound, c: CountryInfo): CheckpointRound = r.copy(
        npcLineEs = r.npcLineEs?.let { sub(it, c) },
        npcLineRu = r.npcLineRu?.let { subRu(it, c) },
        promptRu = subRu(r.promptRu, c),
        promptTextRu = subRu(r.promptTextRu, c),
        promptTextEs = sub(r.promptTextEs, c),
        correctAnswer = sub(r.correctAnswer, c),
        distractors = r.distractors.map { sub(it, c) },
        acceptableAlternatives = r.acceptableAlternatives.map { sub(it, c) },
        wordBank = r.wordBank.map { sub(it, c) },
        sentenceTemplate = sub(r.sentenceTemplate, c),
        translationAfterAnswerRu = subRu(r.translationAfterAnswerRu, c),
        explanationOnFailRu = subRu(r.explanationOnFailRu, c),
        reactionCorrectRu = subRu(r.reactionCorrectRu, c),
        reactionWrongRu = subRu(r.reactionWrongRu, c),
    )

    /** Подмена в испанских строках. Учитываем регистр и word-boundary. */
    private fun sub(s: String, c: CountryInfo): String {
        if (s.isBlank()) return s
        return s
            .replaceWord("Rusia", c.countryEs)
            .replaceWord("rusa", c.nationalityF)
            .replaceWord("Rusa", c.nationalityF.replaceFirstChar { it.uppercase() })
            .replaceWord("ruso", c.nationalityM)
            .replaceWord("Ruso", c.nationalityM.replaceFirstChar { it.uppercase() })
    }

    /** Подмена в русских строках. */
    private fun subRu(s: String, c: CountryInfo): String {
        if (s.isBlank()) return s
        val countryRu = countryRuName(c.isoCode)
        val (nationalityM, nationalityF) = nationalityRuName(c.isoCode)
        return s
            .replaceWord("Россия", countryRu.nominative)
            .replaceWord("России", countryRu.genitive)
            .replaceWord("русская", nationalityF)
            .replaceWord("русский", nationalityM)
    }

    private data class RuCountryForms(val nominative: String, val genitive: String)

    private fun countryRuName(iso: String): RuCountryForms = when (iso) {
        "BY" -> RuCountryForms("Беларусь", "Беларуси")
        "UA" -> RuCountryForms("Украина", "Украины")
        "KZ" -> RuCountryForms("Казахстан", "Казахстана")
        "KG" -> RuCountryForms("Киргизия", "Киргизии")
        "UZ" -> RuCountryForms("Узбекистан", "Узбекистана")
        "TJ" -> RuCountryForms("Таджикистан", "Таджикистана")
        "TM" -> RuCountryForms("Туркменистан", "Туркменистана")
        "AM" -> RuCountryForms("Армения", "Армении")
        "GE" -> RuCountryForms("Грузия", "Грузии")
        "AZ" -> RuCountryForms("Азербайджан", "Азербайджана")
        "MD" -> RuCountryForms("Молдавия", "Молдавии")
        "ES" -> RuCountryForms("Испания", "Испании")
        "MX" -> RuCountryForms("Мексика", "Мексики")
        "AR" -> RuCountryForms("Аргентина", "Аргентины")
        "CO" -> RuCountryForms("Колумбия", "Колумбии")
        "CL" -> RuCountryForms("Чили", "Чили")
        "PE" -> RuCountryForms("Перу", "Перу")
        "VE" -> RuCountryForms("Венесуэла", "Венесуэлы")
        "EC" -> RuCountryForms("Эквадор", "Эквадора")
        "CU" -> RuCountryForms("Куба", "Кубы")
        "FR" -> RuCountryForms("Франция", "Франции")
        "GB" -> RuCountryForms("Англия", "Англии")
        "US" -> RuCountryForms("США", "США")
        "DE" -> RuCountryForms("Германия", "Германии")
        "IT" -> RuCountryForms("Италия", "Италии")
        "PL" -> RuCountryForms("Польша", "Польши")
        "TR" -> RuCountryForms("Турция", "Турции")
        "BR" -> RuCountryForms("Бразилия", "Бразилии")
        else -> RuCountryForms("Россия", "России")
    }

    /** Returns (masc, fem) Russian-language nationality. */
    private fun nationalityRuName(iso: String): Pair<String, String> = when (iso) {
        "BY" -> "белорус" to "белоруска"
        "UA" -> "украинец" to "украинка"
        "KZ" -> "казах" to "казашка"
        "KG" -> "киргиз" to "киргизка"
        "UZ" -> "узбек" to "узбечка"
        "TJ" -> "таджик" to "таджичка"
        "TM" -> "туркмен" to "туркменка"
        "AM" -> "армянин" to "армянка"
        "GE" -> "грузин" to "грузинка"
        "AZ" -> "азербайджанец" to "азербайджанка"
        "MD" -> "молдаванин" to "молдаванка"
        "ES" -> "испанец" to "испанка"
        "MX" -> "мексиканец" to "мексиканка"
        "AR" -> "аргентинец" to "аргентинка"
        "CO" -> "колумбиец" to "колумбийка"
        "CL" -> "чилиец" to "чилийка"
        "PE" -> "перуанец" to "перуанка"
        "VE" -> "венесуэлец" to "венесуэлка"
        "EC" -> "эквадорец" to "эквадорка"
        "CU" -> "кубинец" to "кубинка"
        "FR" -> "француз" to "француженка"
        "GB" -> "англичанин" to "англичанка"
        "US" -> "американец" to "американка"
        "DE" -> "немец" to "немка"
        "IT" -> "итальянец" to "итальянка"
        "PL" -> "поляк" to "полька"
        "TR" -> "турок" to "турчанка"
        "BR" -> "бразилец" to "бразильянка"
        else -> "русский" to "русская"
    }

    /** Замена «целого слова» — не трогает части других слов. */
    private fun String.replaceWord(word: String, replacement: String): String {
        if (word == replacement) return this
        // Простая граница: до/после — не буква/цифра
        val regex = Regex("(?<![\\p{L}\\p{N}])${Regex.escape(word)}(?![\\p{L}\\p{N}])")
        return regex.replace(this, replacement)
    }
}
