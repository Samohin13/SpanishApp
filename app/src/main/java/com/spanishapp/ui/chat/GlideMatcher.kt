package com.spanishapp.ui.chat

import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.min

/**
 * Glide-typing matcher: палец прошёл через последовательность клавиш →
 * находит наиболее вероятное слово из словаря.
 *
 * Алгоритм (упрощённый, без ML):
 *  1. Из позиций пальца строится sequence клавиш (snap каждой точки к
 *     ближайшей клавише + dedupe consecutive).
 *  2. Кандидаты: все слова словаря, которые:
 *     - Начинаются на ту же букву что и trace
 *     - Заканчиваются на ту же букву что и trace
 *     - Длина в диапазоне ±50% от trace length
 *  3. Для каждого кандидата считается score:
 *     - Базовый: -levenshteinDistance(traceLetters, word)
 *     - Boost от user frequency: +log(count) * 0.5
 *     - Penalty от длины: -|len(word) - len(trace)| * 0.3
 *  4. Лучший по score — победитель. Если score слишком низкий — null.
 *
 * Не претендует на Gboard-уровень — там используется HMM + n-gram модели.
 * Для нашего use-case (короткие сообщения 5-15 букв) достаточно.
 */
object GlideMatcher {

    /**
     * Главный entry point. Возвращает наиболее вероятное слово или null.
     *
     * @param traceLetters последовательность букв через которые прошёл палец
     *                     (уже dedup'нутая и lowercase)
     * @param dictionary все доступные слова (статичный + user frequency)
     * @param userFreq частоты пользовательских слов (для boost)
     */
    fun matchBestWord(
        traceLetters: List<Char>,
        dictionary: List<String>,
        userFreq: Map<String, Int> = emptyMap(),
    ): String? {
        if (traceLetters.size < 2) return null
        val traceStr = traceLetters.joinToString("")
        val firstLetter = traceLetters.first()
        val lastLetter = traceLetters.last()
        val traceLen = traceLetters.size

        val candidates = dictionary
            .asSequence()
            .map { it.lowercase() }
            .filter { word ->
                word.length >= 2 &&
                    word.length in (traceLen / 2)..(traceLen * 2 + 2) &&
                    word.first() == firstLetter &&
                    word.last() == lastLetter
            }
            .distinct()
            .toList()

        if (candidates.isEmpty()) return null

        var bestWord: String? = null
        var bestScore = Double.NEGATIVE_INFINITY

        for (word in candidates) {
            val dist = levenshtein(traceStr, word)
            val lenPenalty = abs(word.length - traceLen) * 0.3
            val freqBoost = userFreq[word]?.let { ln(it.toDouble() + 1) * 0.5 } ?: 0.0
            val score = -dist.toDouble() - lenPenalty + freqBoost
            if (score > bestScore) {
                bestScore = score
                bestWord = word
            }
        }

        // Минимальный threshold: score не должен быть катастрофически низким.
        // Эмпирическая граница: -traceLen (т.е. distance > длины trace = мусор)
        if (bestWord != null && bestScore < -traceLen.toDouble()) return null
        return bestWord
    }

    /** Top-N подсказок для glide. Используется в suggestion strip. */
    fun topMatches(
        traceLetters: List<Char>,
        dictionary: List<String>,
        userFreq: Map<String, Int> = emptyMap(),
        n: Int = 3,
    ): List<String> {
        if (traceLetters.size < 2) return emptyList()
        val traceStr = traceLetters.joinToString("")
        val firstLetter = traceLetters.first()
        val traceLen = traceLetters.size

        return dictionary
            .asSequence()
            .map { it.lowercase() }
            .distinct()
            .filter { word ->
                word.length >= 2 &&
                    word.length in (traceLen / 2)..(traceLen * 2 + 2) &&
                    word.first() == firstLetter
            }
            .map { word ->
                val dist = levenshtein(traceStr, word)
                val lenPenalty = abs(word.length - traceLen) * 0.3
                val freqBoost = userFreq[word]?.let { ln(it.toDouble() + 1) * 0.5 } ?: 0.0
                word to (-dist.toDouble() - lenPenalty + freqBoost)
            }
            .sortedByDescending { it.second }
            .take(n)
            .map { it.first }
            .toList()
    }

    /**
     * Свернуть последовательные одинаковые буквы (палец немного дрожит на
     * клавише → несколько одинаковых snap'ов).
     */
    fun dedupeConsecutive(letters: List<Char>): List<Char> {
        if (letters.isEmpty()) return emptyList()
        val result = mutableListOf<Char>()
        result.add(letters.first())
        for (i in 1 until letters.size) {
            if (letters[i] != letters[i - 1]) result.add(letters[i])
        }
        return result
    }

    /** Минимальный Levenshtein distance — классический DP. */
    fun levenshtein(a: String, b: String): Int {
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        val cost = IntArray(b.length + 1) { it }
        var prev = 0
        for (i in 1..a.length) {
            prev = cost[0]
            cost[0] = i
            for (j in 1..b.length) {
                val sub = if (a[i - 1] == b[j - 1]) 0 else 1
                val tmp = cost[j]
                cost[j] = min(
                    min(cost[j] + 1, cost[j - 1] + 1),
                    prev + sub,
                )
                prev = tmp
            }
        }
        return cost[b.length]
    }
}
