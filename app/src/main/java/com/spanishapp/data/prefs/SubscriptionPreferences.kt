package com.spanishapp.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.subscriptionDataStore by preferencesDataStore(name = "subscription_prefs")

/**
 * v1.23.0: PRO subscription state.
 *
 * v1.23.1 (audit Bug 18): рефакторинг — все 5 полей читаются из одного
 * `all` Flow, а per-field flows derive через .map { it.X }.distinctUntilChanged().
 * Раньше каждое поле подписывалось отдельно на DataStore.data → 5 параллельных
 * collector'ов, каждое обновление трогало все 5. Теперь DataStore читается
 * один раз, distinct'ed-фильтрация per-field в памяти.
 *
 * Local cache of subscription status. Реальные платежи через
 * Google Play Billing Library (Фаза 5) будут обновлять эти поля.
 *
 * До интеграции Billing — поля управляются только debug toggle для
 * QA-тестирования gate-логики.
 */
@Singleton
class SubscriptionPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val IS_PRO = booleanPreferencesKey("is_pro")
        val PLAN = stringPreferencesKey("plan") // "MONTH" | "YEAR" | ""
        val EXPIRES_AT = longPreferencesKey("expires_at") // ms
        val PURCHASED_AT = longPreferencesKey("purchased_at") // ms
        val IN_TRIAL = booleanPreferencesKey("in_trial")
        val TRIAL_ENDS_AT = longPreferencesKey("trial_ends_at") // ms
    }

    /** Снапшот всех полей одним объектом. Источник для остальных Flow. */
    data class Snapshot(
        val isPro: Boolean = false,
        val plan: String = "",
        val expiresAt: Long = 0L,
        val inTrial: Boolean = false,
        val trialEndsAt: Long = 0L,
    )

    /** Единый Flow на всё состояние подписки. Один collector на DataStore. */
    val all: Flow<Snapshot> = context.subscriptionDataStore.data.map { p ->
        Snapshot(
            isPro = p[Keys.IS_PRO] ?: false,
            plan = p[Keys.PLAN] ?: "",
            expiresAt = p[Keys.EXPIRES_AT] ?: 0L,
            inTrial = p[Keys.IN_TRIAL] ?: false,
            trialEndsAt = p[Keys.TRIAL_ENDS_AT] ?: 0L,
        )
    }

    /** Активна ли подписка прямо сейчас. */
    val isPro: Flow<Boolean> = all.map { it.isPro }.distinctUntilChanged()

    /** "MONTH" / "YEAR" / "" */
    val plan: Flow<String> = all.map { it.plan }.distinctUntilChanged()

    /** ms timestamp когда подписка истекает. 0 если не активна. */
    val expiresAt: Flow<Long> = all.map { it.expiresAt }.distinctUntilChanged()

    /** Сейчас в trial-периоде (первые 7 дней YEAR-плана). */
    val inTrial: Flow<Boolean> = all.map { it.inTrial }.distinctUntilChanged()

    val trialEndsAt: Flow<Long> = all.map { it.trialEndsAt }.distinctUntilChanged()

    /** Установить полный набор полей после успешной покупки. */
    suspend fun activate(
        plan: String,
        purchasedAt: Long,
        expiresAt: Long,
        inTrial: Boolean,
        trialEndsAt: Long,
    ) {
        context.subscriptionDataStore.edit { p ->
            p[Keys.IS_PRO] = true
            p[Keys.PLAN] = plan
            p[Keys.PURCHASED_AT] = purchasedAt
            p[Keys.EXPIRES_AT] = expiresAt
            p[Keys.IN_TRIAL] = inTrial
            p[Keys.TRIAL_ENDS_AT] = trialEndsAt
        }
    }

    /** Сбросить — подписка отменена / истекла / не куплена. */
    suspend fun deactivate() {
        context.subscriptionDataStore.edit { p ->
            p[Keys.IS_PRO] = false
            p[Keys.PLAN] = ""
            p[Keys.EXPIRES_AT] = 0L
            p[Keys.IN_TRIAL] = false
            p[Keys.TRIAL_ENDS_AT] = 0L
        }
    }

    /**
     * v1.25.4: Production purchase setter (вызывается PlayBillingManager).
     * Без debug-меток — реальное состояние подписки.
     */
    suspend fun setPro(active: Boolean) {
        context.subscriptionDataStore.edit { p ->
            p[Keys.IS_PRO] = active
            if (active) {
                p[Keys.PURCHASED_AT] = System.currentTimeMillis()
            }
        }
    }

    /** Debug-only: переключить PRO для QA gate-логики (без реальной покупки). */
    suspend fun debugSetPro(enabled: Boolean) {
        context.subscriptionDataStore.edit { p ->
            p[Keys.IS_PRO] = enabled
            if (enabled) {
                p[Keys.PLAN] = "YEAR"
                p[Keys.PURCHASED_AT] = System.currentTimeMillis()
                p[Keys.EXPIRES_AT] = System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000
                p[Keys.IN_TRIAL] = false
            } else {
                p[Keys.PLAN] = ""
                p[Keys.EXPIRES_AT] = 0L
                p[Keys.IN_TRIAL] = false
            }
        }
    }
}
