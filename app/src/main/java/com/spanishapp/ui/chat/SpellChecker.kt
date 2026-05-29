package com.spanishapp.ui.chat

import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

/**
 * Spell checker для клавиатуры. После каждого word-boundary (пробел / точка)
 * проверяет последнее слово против ExpandedDictionary. Если найден кандидат
 * с edit distance ≤ 2 и confidence > threshold — предлагает замену.
 *
 * Алгоритм:
 *  1. Если typed уже в словаре → правильно, ничего не делаем
 *  2. Иначе ищем кандидаты с editDistance ≤ 2
 *  3. confidence = freqBoost / (1 + distance) — выше для коротких слов
 *  4. Если top candidate confidence > 0.5 → suggest correction
 *
 * Возвращает [SpellSuggestion] или null если не уверен.
 *
 * @see GlideMatcher.levenshtein (используется отсюда)
 */
object SpellChecker {

    data class SpellSuggestion(
        val original: String,
        val correction: String,
        val confidence: Float,
    )

    // v1.25.61: MAX_DISTANCE 2 → 1 (юзер набрал "давай" — autocorrect
    // менял на "диван" т.к. distance=2). Distance=1 ловит реальные
    // опечатки (одна буква мимо), но не насилует валидные слова которых
    // нет в словаре.
    private const val MAX_DISTANCE = 1
    // v1.25.61: confidence threshold повышен 0.5 → 0.7 (меньше false
    // positives — autocorrect должен срабатывать только когда мы УВЕРЕНЫ).
    private const val MIN_CONFIDENCE = 0.7f

    /**
     * Проверить слово. Возвращает suggestion если найдено лучшее, null иначе.
     *
     * @param word введённое слово (lowercase) — без пробелов
     * @param language "ES" / "RU" / детект по первой букве
     * @param userFreq частоты пользователя (boost его слов)
     */
    fun check(
        word: String,
        language: String? = null,
        userFreq: Map<String, Int> = emptyMap(),
    ): SpellSuggestion? {
        if (word.length < 3) return null  // слишком короткое для autocorrect
        val w = word.lowercase()
        // Детект языка
        val isRu = language == "RU" || (language == null && w.first() in 'а'..'я')
        val dict = if (isRu) ExpandedDictionary.RU else ExpandedDictionary.ES

        // Уже в словаре? — правильно
        if (w in dict) return null
        // У юзера в часто-используемых? — тоже правильно
        if (userFreq[w] != null && userFreq[w]!! >= 2) return null

        // Поиск кандидатов с edit distance ≤ MAX_DISTANCE
        var best: String? = null
        var bestScore = 0f

        for (candidate in dict) {
            // Skip кандидатов сильно отличающихся по длине (быстрый прунинг)
            if (kotlin.math.abs(candidate.length - w.length) > MAX_DISTANCE) continue
            // Skip если первая буква разная (юзер обычно правильно начинает)
            if (candidate.first() != w.first()) continue

            val dist = GlideMatcher.levenshtein(w, candidate)
            if (dist > MAX_DISTANCE) continue

            // Confidence: -distance + freqBoost
            val freq = (userFreq[candidate] ?: 0).toFloat()
            val freqBoost = ln(freq + 1f) * 0.3f
            val lengthFactor = 1f / (1 + kotlin.math.abs(candidate.length - w.length) * 0.2f)
            val score = (1f - dist.toFloat() / max(w.length, candidate.length)) * lengthFactor + freqBoost

            if (score > bestScore) {
                bestScore = score
                best = candidate
            }
        }

        val final = best ?: return null
        if (bestScore < MIN_CONFIDENCE) return null
        return SpellSuggestion(w, final, bestScore)
    }
}
