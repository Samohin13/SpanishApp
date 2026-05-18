package com.spanishapp.widget

import android.content.Context
import android.content.Intent
import com.spanishapp.MainActivity

/**
 * v1.14.0: Deep-link infrastructure для AppWidget'ов.
 *
 * Все виджеты открывают MainActivity с extra string [EXTRA_NAV_TARGET].
 * MainActivity читает его в onCreate и навигирует на нужный экран.
 *
 * Целевые маршруты совпадают с NavHost route'ами в Navigation.kt:
 * - "home"
 * - "dictionary"
 * - "ai_chat_sessions"
 * - "radio"
 *
 * Использование из Glance:
 * ```kotlin
 * modifier = GlanceModifier.clickable(
 *     actionStartActivity(WidgetIntents.intentFor(context, "dictionary"))
 * )
 * ```
 */
object WidgetIntents {
    const val EXTRA_NAV_TARGET = "espeak.widget.nav_target"

    const val TARGET_HOME = "home"
    const val TARGET_DICTIONARY = "dictionary"
    const val TARGET_AI_CHAT = "ai_chat_sessions"
    const val TARGET_RADIO = "radio"

    fun intentFor(context: Context, target: String): Intent =
        Intent(context, MainActivity::class.java).apply {
            // SINGLE_TOP + CLEAR_TOP — переиспользуем existing instance если есть.
            // Иначе на каждый тап виджета открывался бы новый активити в стеке.
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAV_TARGET, target)
        }
}
