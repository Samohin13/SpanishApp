package com.spanishapp.domain.rating

/**
 * Сопоставление ISO-2 кода страны → русское название + эмодзи флага.
 * Покрывает основные страны для лидерборда. Для неизвестных кодов —
 * fallback «Моя страна» 🏳️.
 */
object CountryNames {

    private val DATA: Map<String, Pair<String, String>> = mapOf(
        "KZ" to ("Казахстан"     to "🇰🇿"),
        "RU" to ("Россия"        to "🇷🇺"),
        "BY" to ("Беларусь"      to "🇧🇾"),
        "UA" to ("Украина"       to "🇺🇦"),
        "UZ" to ("Узбекистан"    to "🇺🇿"),
        "KG" to ("Кыргызстан"    to "🇰🇬"),
        "TJ" to ("Таджикистан"   to "🇹🇯"),
        "TM" to ("Туркменистан"  to "🇹🇲"),
        "AZ" to ("Азербайджан"   to "🇦🇿"),
        "AM" to ("Армения"       to "🇦🇲"),
        "GE" to ("Грузия"        to "🇬🇪"),
        "MD" to ("Молдова"       to "🇲🇩"),
        "ES" to ("España"        to "🇪🇸"),
        "FR" to ("Франция"       to "🇫🇷"),
        "DE" to ("Германия"      to "🇩🇪"),
        "IT" to ("Италия"        to "🇮🇹"),
        "PT" to ("Португалия"    to "🇵🇹"),
        "GB" to ("Великобритания" to "🇬🇧"),
        "US" to ("США"           to "🇺🇸"),
        "CA" to ("Канада"        to "🇨🇦"),
        "MX" to ("México"        to "🇲🇽"),
        "AR" to ("Argentina"     to "🇦🇷"),
        "CL" to ("Chile"         to "🇨🇱"),
        "CO" to ("Colombia"      to "🇨🇴"),
        "PE" to ("Perú"          to "🇵🇪"),
        "VE" to ("Venezuela"     to "🇻🇪"),
        "EC" to ("Ecuador"       to "🇪🇨"),
        "BR" to ("Бразилия"      to "🇧🇷"),
        "PL" to ("Польша"        to "🇵🇱"),
        "NL" to ("Нидерланды"    to "🇳🇱"),
        "BE" to ("Бельгия"       to "🇧🇪"),
        "CH" to ("Швейцария"     to "🇨🇭"),
        "AT" to ("Австрия"       to "🇦🇹"),
        "SE" to ("Швеция"        to "🇸🇪"),
        "NO" to ("Норвегия"      to "🇳🇴"),
        "FI" to ("Финляндия"     to "🇫🇮"),
        "DK" to ("Дания"         to "🇩🇰"),
        "CZ" to ("Чехия"         to "🇨🇿"),
        "GR" to ("Греция"        to "🇬🇷"),
        "TR" to ("Турция"        to "🇹🇷"),
        "JP" to ("Япония"        to "🇯🇵"),
        "KR" to ("Корея"         to "🇰🇷"),
        "CN" to ("Китай"         to "🇨🇳"),
        "IN" to ("Индия"         to "🇮🇳"),
        "AU" to ("Австралия"     to "🇦🇺"),
        "NZ" to ("Новая Зеландия" to "🇳🇿"),
        "IL" to ("Израиль"       to "🇮🇱"),
        "AE" to ("ОАЭ"           to "🇦🇪"),
        // ── Юго-Восточная Азия / South Asia ─────────────────────
        "VN" to ("Вьетнам"       to "🇻🇳"),
        "TH" to ("Таиланд"       to "🇹🇭"),
        "ID" to ("Индонезия"     to "🇮🇩"),
        "MY" to ("Малайзия"      to "🇲🇾"),
        "SG" to ("Сингапур"      to "🇸🇬"),
        "PH" to ("Филиппины"     to "🇵🇭"),
        "PK" to ("Пакистан"      to "🇵🇰"),
        "BD" to ("Бангладеш"     to "🇧🇩"),
        "LK" to ("Шри-Ланка"     to "🇱🇰"),
        // ── Ближний Восток / Африка ─────────────────────────────
        "EG" to ("Египет"        to "🇪🇬"),
        "MA" to ("Марокко"       to "🇲🇦"),
        "IR" to ("Иран"          to "🇮🇷"),
        "SA" to ("Саудовская Аравия" to "🇸🇦"),
        "QA" to ("Катар"         to "🇶🇦"),
        "ZA" to ("ЮАР"           to "🇿🇦"),
        // ── Европа (расширение) ─────────────────────────────────
        "IE" to ("Ирландия"      to "🇮🇪"),
        "IS" to ("Исландия"      to "🇮🇸"),
        "EE" to ("Эстония"       to "🇪🇪"),
        "LV" to ("Латвия"        to "🇱🇻"),
        "LT" to ("Литва"         to "🇱🇹"),
        "HU" to ("Венгрия"       to "🇭🇺"),
        "RO" to ("Румыния"       to "🇷🇴"),
        "BG" to ("Болгария"      to "🇧🇬"),
        "RS" to ("Сербия"        to "🇷🇸"),
        "HR" to ("Хорватия"      to "🇭🇷"),
        "SK" to ("Словакия"      to "🇸🇰"),
        "SI" to ("Словения"      to "🇸🇮"),
        "LU" to ("Люксембург"    to "🇱🇺"),
        "CY" to ("Кипр"          to "🇨🇾"),
        "MT" to ("Мальта"        to "🇲🇹"),
        // ── Латинская Америка (расширение) ──────────────────────
        "UY" to ("Уругвай"       to "🇺🇾"),
        "PY" to ("Парагвай"      to "🇵🇾"),
        "BO" to ("Боливия"       to "🇧🇴"),
        "CR" to ("Коста-Рика"    to "🇨🇷"),
        "PA" to ("Панама"        to "🇵🇦"),
        "DO" to ("Доминикана"    to "🇩🇴"),
        "CU" to ("Куба"          to "🇨🇺"),
        "GT" to ("Гватемала"     to "🇬🇹"),
        "HN" to ("Гондурас"      to "🇭🇳"),
        "NI" to ("Никарагуа"     to "🇳🇮"),
        "SV" to ("Сальвадор"     to "🇸🇻"),
        "PR" to ("Пуэрто-Рико"   to "🇵🇷"),
    )

    fun nameOf(iso: String): String =
        DATA[iso.uppercase()]?.first ?: "Моя страна"

    fun flagOf(iso: String): String =
        DATA[iso.uppercase()]?.second ?: "🏳️"

    fun displayWithFlag(iso: String): String =
        "${flagOf(iso)} ${nameOf(iso)}"

    /** Все страны для picker'а в лидерборде — отсортированы по русскому названию. */
    data class CountryOption(val iso: String, val name: String, val flag: String)

    fun allCountries(): List<CountryOption> =
        DATA.entries
            .map { (iso, pair) -> CountryOption(iso, pair.first, pair.second) }
            .sortedBy { it.name.lowercase() }

    /** Проверить, есть ли код в нашей таблице — для логики «известная страна?» */
    fun isKnown(iso: String): Boolean = DATA.containsKey(iso.uppercase())
}
