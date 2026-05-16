package com.spanishapp.radio.data

enum class Country(val emoji: String, val displayName: String) {
    SPAIN("🇪🇸", "España"),
    MEXICO("🇲🇽", "México"),
    ARGENTINA("🇦🇷", "Argentina"),
}

enum class Genre(val displayName: String, val emoji: String) {
    MUSIC("Музыка", "🎵"),
    TALK("Разговор", "🎙"),
    NEWS("Новости", "📰"),
    SPORTS("Спорт", "⚽"),
    CULTURE("Культура", "🎭"),
}

/**
 * CEFR-уровень контента станции — определяет насколько сложно слушать.
 * A2 = простой (музыка, рекламы, поп-хиты)
 * B1 = средний (общие новости, ток-шоу обычным темпом)
 * B2 = сложный (быстрые ток-шоу, политика, экономика)
 */
enum class CefrLevel { A2, B1, B2 }

data class Station(
    /** Стабильный ID для БД и сохранения избранного. */
    val id: String,
    /** Короткий код для иконки (3-4 буквы, "SER", "RNE", "40"). */
    val shortCode: String,
    /** Полное название. */
    val name: String,
    /** Что сейчас идёт в эфире (опционально, для отображения). */
    val program: String,
    /** Частота FM в МГц (например 88.7). */
    val frequency: Float,
    val country: Country,
    val genre: Genre,
    val level: CefrLevel,
    /** URL потока (mp3 или m3u8 HLS). */
    val streamUrl: String,
)
