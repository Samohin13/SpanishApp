package com.spanishapp.service

import com.spanishapp.data.prefs.SubscriptionPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * v1.23.0: фасад над SubscriptionPreferences.
 *
 * Единая точка для запроса «есть ли у юзера PRO» из любого экрана.
 * Все ViewModels подписываются на [isProActive] и реактивно прячут/
 * показывают paywall, баннеры, замочки на A2+ контенте.
 *
 * Реальная синхронизация с Google Play Billing (queryPurchasesAsync,
 * BillingClient callbacks) будет добавлена в Фазе 5.
 *
 * Контентные правила (что входит в PRO):
 *  - Уроки A2, B1, B2 (180 шт)
 *  - Грамматика A2/B1/B2, диалоги A2/B1/B2
 *  - Книги A2/B1/B2 (75 рассказов)
 *  - Спряжение всех 1327 глаголов (free = базовое)
 *  - Все 100 уровней игр (free = первые 10)
 *  - SM-2 карточки A2/B1/B2 слов
 *  - AI-репетитор безлимит (free = 50/день)
 */
@Singleton
class SubscriptionManager @Inject constructor(
    private val prefs: SubscriptionPreferences,
) {
    /** Главный флаг — PRO активен прямо сейчас. */
    val isProActive: Flow<Boolean> = prefs.isPro

    /** Тип подписки: "MONTH" | "YEAR" | "" */
    val plan: Flow<String> = prefs.plan

    /** ms timestamp когда подписка истекает (0 если не активна). */
    val expiresAt: Flow<Long> = prefs.expiresAt

    /** Активен ли trial-период. */
    val inTrial: Flow<Boolean> = prefs.inTrial

    /** ms timestamp окончания trial. */
    val trialEndsAt: Flow<Long> = prefs.trialEndsAt

    /** Полное состояние одним объектом для UI. */
    val state: Flow<SubscriptionState> = combine(
        isProActive, plan, expiresAt, inTrial, trialEndsAt
    ) { isPro, plan, expires, inTrial, trialEnds ->
        SubscriptionState(
            isPro = isPro,
            plan = plan,
            expiresAt = expires,
            inTrial = inTrial,
            trialEndsAt = trialEnds,
        )
    }

    /** Проверка доступности контента по CEFR-уровню. A1 = free, остальное = PRO. */
    fun isContentUnlocked(level: String, isPro: Boolean): Boolean {
        if (isPro) return true
        return level.equals("A1", ignoreCase = true)
    }

    /** Доступен ли уровень игры. Free = первые 10. */
    fun isGameLevelUnlocked(level: Int, isPro: Boolean): Boolean {
        if (isPro) return true
        return level <= 10
    }

    /** Debug-only: для QA тестирования gate-логики. */
    suspend fun debugSetPro(enabled: Boolean) {
        prefs.debugSetPro(enabled)
    }
}

data class SubscriptionState(
    val isPro: Boolean = false,
    val plan: String = "",
    val expiresAt: Long = 0L,
    val inTrial: Boolean = false,
    val trialEndsAt: Long = 0L,
) {
    val planLabel: String get() = when (plan) {
        "YEAR" -> "PRO Annual"
        "MONTH" -> "PRO Monthly"
        else -> "Free"
    }
}
