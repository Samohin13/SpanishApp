package com.spanishapp.ui.games

/**
 * Frase Loca — «собери фразу из плиток». Модель контента.
 *
 * Банк: 20 тем × 12 фраз = 240 авторских фраз (A1→B2), каждая тема
 * покрывает 5 уровней (20 × 5 = 100). Внутри темы фразы отсортированы
 * от простых к сложным; уровень берёт «скользящее окно» из пула.
 *
 * Ловушки (traps) — лишние плитки, авторски подобранные под типичные
 * ошибки русскоязычных (род, ser/estar, Indefinido/Imperfecto, артикли,
 * subjuntivo...). Каждая ловушка несёт объяснение — ошибка превращается
 * в микро-урок. Количество активных ловушек растёт с уровнем
 * (см. [FraseLocaEngine.trapLimitForLevel]).
 *
 * Контент по-русски — приложение русскоязычное по контенту (заморозка
 * локализации контента, решение владельца 2026-05-17). UI-хром игры —
 * через strings.xml, как у остальных игр.
 */
data class FraseTrap(
    /** Слово-ловушка (плитка). НЕ должно совпадать ни с одним токеном фразы. */
    val word: String,
    /** Короткое объяснение по-русски, почему это ошибка. */
    val explanation: String,
)

data class FrasePhrase(
    /** Русский промпт («Вчера я купил красную машину»). */
    val ru: String,
    /** Испанские плитки в правильном порядке (пунктуация приклеена к словам). */
    val tokens: List<String>,
    /** Авторские ловушки (0..3). Движок активирует первые N по уровню. */
    val traps: List<FraseTrap> = emptyList(),
) {
    /** Собранная фраза — для записи в mistakes и показа в результате. */
    val sentence: String get() = tokens.joinToString(" ")
}

data class FraseTheme(
    val id: String,
    /** Название темы по-русски — показывается в шапке уровня. */
    val title: String,
    val cefr: String,
    /** 12 фраз, отсортированных от простых к сложным. */
    val phrases: List<FrasePhrase>,
)

/** Компактные фабрики для банков: фраза целиком строкой, токены = split. */
internal fun flp(ru: String, es: String, vararg traps: FraseTrap) =
    FrasePhrase(ru = ru, tokens = es.split(" "), traps = traps.toList())

internal fun flt(word: String, explanation: String) = FraseTrap(word, explanation)

/**
 * Полный контент игры: 20 тем в порядке уровней (тема i покрывает
 * уровни i*5+1 .. i*5+5).
 */
object FraseLocaContent {
    val themes: List<FraseTheme> by lazy {
        FraseLocaBankA1.themes + FraseLocaBankA2.themes +
            FraseLocaBankB1.themes + FraseLocaBankB2.themes
    }
}
