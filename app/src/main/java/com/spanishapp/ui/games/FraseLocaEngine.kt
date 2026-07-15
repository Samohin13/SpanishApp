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

    /** Число фраз в уровне — растёт с ярусом. */
    fun roundsForLevel(level: Int): Int = when {
        level <= 10 -> 4
        level <= 50 -> 5
        else        -> 6
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
     * Фразы уровня: скользящее окно по пулу темы (пул отсортирован от
     * простых к сложным). Уровень 1 темы берёт начало пула, уровень 5 —
     * хвост; соседние уровни пересекаются — это осознанное повторение.
     */
    fun phrasesForLevel(level: Int): List<FrasePhrase> {
        val theme = themeForLevel(level)
        val pool = theme.phrases
        val rounds = roundsForLevel(level).coerceAtMost(pool.size)
        val levelIdx = (level - 1) % LEVELS_PER_THEME               // 0..4
        val maxStart = (pool.size - rounds).coerceAtLeast(0)
        val start = if (LEVELS_PER_THEME <= 1) 0
                    else (levelIdx * maxStart) / (LEVELS_PER_THEME - 1)
        return pool.subList(start, start + rounds)
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
