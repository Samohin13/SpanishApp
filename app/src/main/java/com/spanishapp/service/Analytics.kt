package com.spanishapp.service

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * Тонкая обёртка над Firebase Analytics — единая точка для всех «продуктовых»
 * событий. Имена и параметры зафиксированы здесь, чтобы:
 *   • не плодить «свободные» строки event-name по коду (опечатался — событие
 *     потерялось навсегда);
 *   • было одно место где видно «что мы вообще измеряем»;
 *   • Firebase Console сразу группировал события без сюрпризов.
 *
 * Принципы:
 *   • Имена событий snake_case, ≤32 символов (требование Firebase).
 *   • Параметры primitive (String / Long / Double), без вложенных Bundle.
 *   • НЕ логируем PII (имя, email, токены, тексты сообщений). Только
 *     анонимные счётчики и id контента.
 *
 * Что измеряем (10 ключевых):
 *   1. lesson_started        — открыт урок (lesson_id, level)
 *   2. lesson_completed      — урок пройден до конца (lesson_id, accuracy)
 *   3. wod_completed         — слово дня закреплено (level)
 *   4. game_started          — открыта мини-игра (game_id)
 *   5. game_level_completed  — пройден уровень игры (game_id, level)
 *   6. streak_lost           — ежедневная серия прервалась (was_days)
 *   7. profile_opened        — заход в Профиль (no params)
 *   8. language_changed      — смена UI-языка (language)
 *   9. subscription_clicked  — клик по PRO/подписке (source)
 *  10. settings_opened       — открыты настройки (no params)
 */
object Analytics {

    private val fa: FirebaseAnalytics by lazy { Firebase.analytics }

    // ── 1. Уроки ──────────────────────────────────────────────────────
    fun lessonStarted(lessonId: String, level: String) =
        log("lesson_started") {
            putString("lesson_id", lessonId)
            putString("level", level)
        }

    fun lessonCompleted(lessonId: String, accuracyPercent: Int) =
        log("lesson_completed") {
            putString("lesson_id", lessonId)
            putLong("accuracy", accuracyPercent.toLong())
        }

    // ── 2. Слово дня ──────────────────────────────────────────────────
    fun wodCompleted(level: String) =
        log("wod_completed") {
            putString("level", level)
        }

    // ── 3. Игры ───────────────────────────────────────────────────────
    fun gameStarted(gameId: String) =
        log("game_started") {
            putString("game_id", gameId)
        }

    fun gameLevelCompleted(gameId: String, level: Int) =
        log("game_level_completed") {
            putString("game_id", gameId)
            putLong("level", level.toLong())
        }

    // ── 4. Стрик ──────────────────────────────────────────────────────
    fun streakLost(daysWas: Int) =
        log("streak_lost") {
            putLong("was_days", daysWas.toLong())
        }

    // ── 5. Навигация ──────────────────────────────────────────────────
    fun profileOpened() = log("profile_opened")
    fun settingsOpened() = log("settings_opened")

    // ── 6. Язык ───────────────────────────────────────────────────────
    fun languageChanged(toLang: String) =
        log("language_changed") {
            putString("language", toLang)
        }

    // ── 7. Монетизация ────────────────────────────────────────────────
    /** source: "profile", "settings", "home_banner" — где юзер кликнул */
    fun subscriptionClicked(source: String) =
        log("subscription_clicked") {
            putString("source", source)
        }

    // ── helpers ───────────────────────────────────────────────────────
    private fun log(name: String, build: Bundle.() -> Unit = {}) {
        runCatching {
            fa.logEvent(name, Bundle().apply(build))
        }
        // Глотаем ошибки — analytics никогда не должна валить юзер-флоу.
    }
}
