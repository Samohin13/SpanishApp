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
        "AE" to ("ОАЭ"           to "🇦🇪")
    )

    fun nameOf(iso: String): String =
        DATA[iso.uppercase()]?.first ?: "Моя страна"

    fun flagOf(iso: String): String =
        DATA[iso.uppercase()]?.second ?: "🏳️"

    fun displayWithFlag(iso: String): String =
        "${flagOf(iso)} ${nameOf(iso)}"
}
