package com.spanishapp.ui.games

import kotlin.random.Random

/** Режимы заданий El Oído. */
enum class OidoMode { CHOICE, DICTATION, PAIRS, NUMBER, TIME }

/** Результат сравнения ответа диктанта. */
enum class OidoMatch {
    /** Точное совпадение (после lowercase/trim). */
    EXACT,
    /** Совпало без учёта акцентов/ñ — засчитываем, но показываем написание. */
    ACCENT_LOOSE,
    /** Не совпало. */
    NONE,
}

/**
 * El Oído — чистая логика (без Android): темп речи, план режимов уровня,
 * выбор пар, генерация чисел и времени, проверка диктанта.
 */
object OidoEngine {

    /** Заданий в уровне. */
    const val TASKS_PER_LEVEL = 10

    /** Множитель темпа речи — сложность El Oído растёт скоростью диктора. */
    fun rateForLevel(level: Int): Float = when {
        level <= 25 -> 0.75f
        level <= 50 -> 0.9f
        level <= 75 -> 1.0f
        else        -> 1.15f
    }

    /** CEFR-пулы слов для выбора/диктанта. */
    fun cefrForLevel(level: Int): List<String> = when {
        level <= 25 -> listOf("A1")
        level <= 50 -> listOf("A1", "A2")
        level <= 75 -> listOf("A2", "B1")
        else        -> listOf("B1", "B2")
    }

    /**
     * План уровня: какие режимы и сколько. Перемешан детерминированно —
     * перепрохождение уровня выглядит одинаково.
     */
    fun planForLevel(level: Int): List<OidoMode> {
        val plan = when {
            level <= 25 -> List(6) { OidoMode.CHOICE } + List(4) { OidoMode.DICTATION }
            level <= 50 -> List(2) { OidoMode.CHOICE } + List(4) { OidoMode.PAIRS } +
                           List(2) { OidoMode.DICTATION } + List(2) { OidoMode.NUMBER }
            else        -> List(3) { OidoMode.PAIRS } + List(3) { OidoMode.DICTATION } +
                           List(2) { OidoMode.NUMBER } + List(2) { OidoMode.TIME }
        }
        return plan.shuffled(Random(level * 31L))
    }

    /** Пары уровня — детерминированная выборка нужной сложности. */
    fun pairsForLevel(level: Int, count: Int): List<OidoPair> {
        val pool = if (level <= 50) {
            OidoPairsBank.pairs.filter { it.category in OidoPairsBank.easyCategories }
        } else {
            OidoPairsBank.pairs
        }
        if (pool.isEmpty()) return emptyList()
        return pool.shuffled(Random(level * 131L)).take(count.coerceAtMost(pool.size))
    }

    /** Диапазон чисел для NUMBER-режима. */
    fun numberForLevel(level: Int, rng: Random): Int = when {
        level <= 50 -> rng.nextInt(0, 101)     // 0..100
        level <= 75 -> rng.nextInt(0, 500)     // 0..499
        else        -> rng.nextInt(0, 1000)    // 0..999
    }

    /** Пригодно ли слово из словаря для диктанта. */
    fun isDictationFriendly(spanish: String): Boolean {
        val w = spanish.trim()
        if (w.length !in 3..10) return false
        // Одно слово, только буквы (испанские включительно)
        return w.all { it.isLetter() } && !w.contains(' ')
    }

    /** Убирает диакритику для «мягкого» сравнения. */
    fun foldAccents(s: String): String = buildString(s.length) {
        for (ch in s) append(
            when (ch) {
                'á' -> 'a'; 'é' -> 'e'; 'í' -> 'i'; 'ó' -> 'o'; 'ú' -> 'u'; 'ü' -> 'u'
                'ñ' -> 'n'
                'Á' -> 'a'; 'É' -> 'e'; 'Í' -> 'i'; 'Ó' -> 'o'; 'Ú' -> 'u'; 'Ü' -> 'u'
                'Ñ' -> 'n'
                else -> ch.lowercaseChar()
            }
        )
    }

    /** Проверка диктанта: точное / без-акцентов / мимо. */
    fun matchDictation(expected: String, typed: String): OidoMatch {
        val e = expected.trim().lowercase()
        val t = typed.trim().lowercase()
        if (t.isEmpty()) return OidoMatch.NONE
        if (e == t) return OidoMatch.EXACT
        if (foldAccents(e) == foldAccents(t)) return OidoMatch.ACCENT_LOOSE
        return OidoMatch.NONE
    }
}

/**
 * Время по-испански (для режима «который час»).
 * Минуты только «круглые»: 00, 15, 30, 45 — как учат на A2.
 */
object TimeToSpanish {

    private val HOURS = listOf(
        "una", "dos", "tres", "cuatro", "cinco", "seis",
        "siete", "ocho", "nueve", "diez", "once", "doce",
    )

    private fun hourWord(hour12: Int): String = HOURS[(hour12 - 1).coerceIn(0, 11)]

    /**
     * Фраза для часов [hour12] (1..12) и минут (0/15/30/45).
     * «8:45» → «Son las nueve menos cuarto» (без четверти девять).
     */
    fun convert(hour12: Int, minute: Int): String {
        val h = ((hour12 - 1).mod(12)) + 1
        return when (minute) {
            0 -> if (h == 1) "Es la una en punto" else "Son las ${hourWord(h)} en punto"
            15 -> if (h == 1) "Es la una y cuarto" else "Son las ${hourWord(h)} y cuarto"
            30 -> if (h == 1) "Es la una y media" else "Son las ${hourWord(h)} y media"
            45 -> {
                val next = (h.mod(12)) + 1
                if (next == 1) "Es la una menos cuarto"
                else "Son las ${hourWord(next)} menos cuarto"
            }
            else -> throw IllegalArgumentException("minute must be 0/15/30/45, got $minute")
        }
    }

    /** Ожидаемый ввод цифрами: 8:30 → 830, 12:15 → 1215. */
    fun expectedDigits(hour12: Int, minute: Int): Int {
        val h = ((hour12 - 1).mod(12)) + 1
        return h * 100 + minute
    }

    /** Формат подсказки «8:30» для показа после ответа. */
    fun display(hour12: Int, minute: Int): String {
        val h = ((hour12 - 1).mod(12)) + 1
        return "%d:%02d".format(h, minute)
    }
}
