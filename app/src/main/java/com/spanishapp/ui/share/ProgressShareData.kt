package com.spanishapp.ui.share

/**
 * Данные для генерации share-картинки (Spotify-Wrapped-style milestone).
 *
 * ВАЖНО — юридически:
 * Это НЕ сертификат и НЕ диплом. Никаких слов «certificate», «diploma»,
 * «qualification», «official». Только информационный milestone: «модуль закрыт»,
 * «N раундов», «X минут», «+Y XP». Для соцсетей, не для CV/работодателя.
 *
 * Создаётся при passing любого чекпоинта (gold/silver/bronze) — см.
 * [CheckpointScreen.ResultView] кнопка «Поделиться достижением».
 */
data class ProgressShareData(
    val userName: String,          // displayName из UserProgress, fallback "Студент"
    val cpId: String,              // "cp1".."cp16"
    val cpTitle: String,           // "Один день в Мадриде"
    val cefr: String,              // "A1"/"A2"/"B1"/"B2"
    val isModuleFinal: Boolean,    // true для CP4/CP8/CP12/CP16 (финал блока CEFR)
    val tier: String,              // "gold"/"silver"/"bronze"
    val accuracy: Int,             // 0..100
    val xpEarned: Int,
    val totalRounds: Int,
    val timeMinutes: Int,
    val dateLocalized: String,     // "24 мая 2026"
) {
    companion object {
        /** Финал блока CEFR — каждый 4-й чекпоинт. */
        fun isFinalForCpId(cpId: String): Boolean = cpId in setOf(
            "cp4", "cp8", "cp12", "cp16"
        )
    }
}
