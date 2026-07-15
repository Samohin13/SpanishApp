package com.spanishapp.ui.games

import kotlin.random.Random

/**
 * Frase Loca — чистая игровая логика (без Android-зависимостей, покрыта
 * unit-тестами). Отвечает за: тема ↔ уровень, окно фраз внутри темы,
 * лимит ловушек по уровню, детерминированную раскладку плиток.
 */
object FraseLocaEngine {

    /** Сколько уровней покрывает одна тема. 20 тем × 5 = 100. */
    const val LEVELS_PER_THEME = 5

    /** Тема для уровня 1..100. */
    fun themeForLevel(level: Int): FraseTheme {
        val idx = ((level.coerceIn(1, 100) - 1) / LEVELS_PER_THEME)
            .coerceIn(0, FraseLocaContent.themes.size - 1)
        return FraseLocaContent.themes[idx]
    }

    /**
     * Число фраз в уровне. Максимум 5: при пуле темы в 12 фраз это
     * гарантирует, что СОСЕДНИЕ уровни не делят ни одной фразы
     * (см. phrasesForLevel). Сложность растёт длиной фраз и ловушками.
     */
    fun roundsForLevel(level: Int): Int = when {
        level <= 10 -> 4
        else        -> 5
    }

    /**
     * Сколько ловушек активируется в фразе на данном уровне.
     * 1–10 — учимся без ловушек; дальше по нарастающей до 3 на B2.
     */
    fun trapLimitForLevel(level: Int): Int = when {
        level <= 10 -> 0
        level <= 25 -> 1
        level <= 75 -> 2
        else        -> 3
    }

    /**
     * Фразы уровня (v1.27.1, фидбэк владельца: «последние 2 фразы
     * переходят в начало следующего уровня» — убрано).
     *
     * Пул темы один раз детерминированно перемешивается, уровни берут
     * ПОСЛЕДОВАТЕЛЬНЫЕ непересекающиеся срезы с закольцовыванием:
     * уровень i → [i·rounds, i·rounds + rounds). При rounds ≤ 6 и пуле
     * из 12 фраз соседние уровни гарантированно не делят ни одной фразы;
     * повторы возможны только через 2+ уровня (это осознанное
     * интервальное повторение, не баг).
     */
    fun phrasesForLevel(level: Int): List<FrasePhrase> {
        val themeIdx = ((level.coerceIn(1, 100) - 1) / LEVELS_PER_THEME)
            .coerceIn(0, FraseLocaContent.themes.size - 1)
        val pool = FraseLocaContent.themes[themeIdx].phrases
            .shuffled(Random(themeIdx * 911L + 17))
        val rounds = roundsForLevel(level).coerceAtMost(pool.size)
        val levelIdx = (level - 1) % LEVELS_PER_THEME               // 0..4
        val start = (levelIdx * rounds) % pool.size
        return List(rounds) { pool[(start + it) % pool.size] }
    }

    /**
     * Активные ловушки фразы на уровне: первые N авторских, отфильтрованные
     * от случайных коллизий с токенами (ловушка не должна совпадать с
     * настоящей плиткой — иначе её нельзя отличить).
     */
    fun activeTraps(phrase: FrasePhrase, level: Int): List<FraseTrap> {
        val limit = trapLimitForLevel(level)
        if (limit == 0) return emptyList()
        val tokensLower = phrase.tokens.map { it.lowercase() }.toSet()
        return phrase.traps
            .filter { it.word.lowercase() !in tokensLower }
            .take(limit)
    }

    /**
     * Раскладка плиток: токены + активные ловушки, перемешанные
     * детерминированно (level, roundIndex) — перепрохождение уровня
     * выглядит одинаково, как в Cálculo (v1.22.2 паттерн).
     */
    fun tilesFor(phrase: FrasePhrase, level: Int, roundIndex: Int): List<String> {
        val traps = activeTraps(phrase, level).map { it.word }
        val seed = level * 7919L + roundIndex * 131L
        return (phrase.tokens + traps).shuffled(Random(seed))
    }
}
