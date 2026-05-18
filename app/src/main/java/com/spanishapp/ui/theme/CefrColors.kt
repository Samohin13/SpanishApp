package com.spanishapp.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * v1.14.2: Единая палитра CEFR уровней для всего приложения.
 *
 * Юзер: "У нас каждый модуль имеет свой уникальный цвет — почему
 * эта логика рушится в книгах? Исправить."
 *
 * Раньше каждый screen определял свои цвета:
 * - FlashcardsSetupScreen: A1=amber, A2=cyan, B1=green, B2=pink
 * - LibrosScreen: A1=green, A2=blue, B1=orange, B2=purple
 *
 * Теперь единый источник правды. Палитра выбрана как в Flashcards
 * (Duolingo-style, контрастная, читается на dark/light theme).
 *
 * Использование:
 * ```kotlin
 * val accent = CefrColors.forLevel("B2")
 * Box(modifier = Modifier.background(accent)) { ... }
 * ```
 */
object CefrColors {
    /** A1 (beginner) — amber-yellow. Тёплый «старт». */
    val A1 = Color(0xFFEAB308)

    /** A2 (elementary) — cyan. Холодный «прохладный» оттенок. */
    val A2 = Color(0xFF06B6D4)

    /** B1 (intermediate) — green. «Растёшь». */
    val B1 = Color(0xFF22C55E)

    /** B2 (upper-intermediate) — pink/magenta. «Премиум». */
    val B2 = Color(0xFFEC4899)

    /** Default fallback (если уровень неизвестен). */
    val Default = Color(0xFFFF6B35)

    /**
     * Вернуть цвет по level string ("A1" / "A2" / "B1" / "B2").
     * Регистр игнорируется, blank/unknown → Default.
     */
    fun forLevel(level: String?): Color = when (level?.uppercase()) {
        "A1" -> A1
        "A2" -> A2
        "B1" -> B1
        "B2" -> B2
        else -> Default
    }
}
