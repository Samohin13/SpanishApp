package com.spanishapp.service

import com.spanishapp.data.prefs.HintBankPreferences
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * v1.16.0: Hint Bank Manager — фасад над [HintBankPreferences]
 * с дополнительной шиной событий «получил N подсказок».
 *
 * UI слой (HintEarnHost) подписан на [earnEvents] и показывает
 * pop-up «+N 💡 подсказки» когда событие приходит. Это даёт
 * мгновенный feedback юзеру: «учился → получил награду».
 *
 * **Зарабатывание** (через [award]):
 *  - Урок завершён (+2)
 *  - Теория прочитана (+1)
 *  - Сет флэшкард на 100% (+3)
 *  - Книга — quiz пройден (+2)
 *  - Streak +1 день (+2)
 *  - Word of Day разгадано (+1)
 *  - Достижение разблокировано (+5)
 *  - Уровень в игре с 3 звёздами (+5)
 *
 * **Трата** (через [tryConsume]):
 *  - Sopa hint = 1
 *  - Crucigrama hint = 1
 *  - Palabra Maestra hint = 1
 *  - Articles hint = 1
 *  - Math hint = 2
 */
@Singleton
class HintBankManager @Inject constructor(
    private val prefs: HintBankPreferences,
) {
    /** Reactive поток текущего количества 💡 для UI badge. */
    val hintsFlow: Flow<Int> = prefs.hintsFlow

    /** Событие «начислено N подсказок» — UI hook для toast/popup. */
    private val _earnEvents = MutableSharedFlow<HintEarnEvent>(replay = 0, extraBufferCapacity = 4)
    val earnEvents: kotlinx.coroutines.flow.SharedFlow<HintEarnEvent> = _earnEvents.asSharedFlow()

    /**
     * Начислить юзеру [amount] подсказок и эмитнуть UI-событие.
     *
     * [reason] — отображается в pop-up и идёт в аналитику.
     * Дубликаты допустимы: можно вызвать `award(2, "lesson_complete")`
     * несколько раз — каждый раз начислится по 2.
     */
    suspend fun award(amount: Int, reason: HintEarnReason) {
        if (amount <= 0) return
        prefs.update { it + amount }
        _earnEvents.emit(HintEarnEvent(amount, reason))
    }

    /**
     * Попытаться потратить [cost] подсказок. Возвращает true если
     * было достаточно и cost списан, иначе false (UI должен показать
     * сообщение «нет подсказок, заработайте в учёбе»).
     */
    suspend fun tryConsume(cost: Int = 1): Boolean {
        if (cost <= 0) return true
        var ok = false
        prefs.update { current ->
            if (current >= cost) {
                ok = true
                current - cost
            } else {
                current
            }
        }
        return ok
    }
}

data class HintEarnEvent(
    val amount: Int,
    val reason: HintEarnReason,
)

enum class HintEarnReason(val labelRes: Int) {
    LESSON_COMPLETE(com.spanishapp.R.string.hint_earn_lesson),
    THEORY_READ(com.spanishapp.R.string.hint_earn_theory),
    FLASHCARD_SET_100(com.spanishapp.R.string.hint_earn_flashcard),
    LIBRO_QUIZ_PASSED(com.spanishapp.R.string.hint_earn_libro),
    STREAK_DAY(com.spanishapp.R.string.hint_earn_streak),
    WORD_OF_DAY(com.spanishapp.R.string.hint_earn_wod),
    ACHIEVEMENT(com.spanishapp.R.string.hint_earn_achievement),
    GAME_3_STARS(com.spanishapp.R.string.hint_earn_3stars),
}
