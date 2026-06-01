package com.spanishapp.ui.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.service.PlayBillingManager
import com.spanishapp.service.SubscriptionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * v1.23.1: ViewModel paywall'а с event-channel паттерном (audit Bug 10).
 *
 * Раньше startPurchase принимал onSuccess callback который вызывался из
 * viewModelScope.launch — race condition при rotation/process death:
 * navController.popBackStack мог сработать на destroyed lifecycle.
 *
 * Теперь: VM эмитит [PurchaseEvent] в Channel, Composable собирает их
 * через LaunchedEffect — события доставляются только в alive lifecycle.
 */
@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val subscriptionManager: SubscriptionManager,
    private val billing: PlayBillingManager,
) : ViewModel() {

    private val _state = MutableStateFlow(PaywallState())
    val state: StateFlow<PaywallState> = _state.asStateFlow()

    private val _events = Channel<PurchaseEvent>(Channel.BUFFERED)
    val events: Flow<PurchaseEvent> = _events.receiveAsFlow()

    init {
        // Подписываемся на isPro — когда PlayBilling обновит state после
        // успешной покупки → эмитим event и закрываем paywall.
        viewModelScope.launch {
            subscriptionManager.isProActive.collect { active ->
                if (active && _state.value.isLoading) {
                    val plan = _state.value.selectedPlan
                    _state.update { it.copy(isLoading = false, purchased = true) }
                    _events.send(PurchaseEvent.Purchased)
                    // v1.25.84 ANL-1: успешная покупка
                    com.spanishapp.service.Analytics.paywallPurchaseSuccess(
                        if (plan == PaywallPlan.MONTH) "monthly" else "yearly"
                    )
                }
            }
        }
    }

    fun selectPlan(plan: PaywallPlan) {
        _state.update { it.copy(selectedPlan = plan) }
        // v1.25.84 ANL-1: paywall funnel analytics
        com.spanishapp.service.Analytics.paywallPlanSelected(
            if (plan == PaywallPlan.MONTH) "monthly" else "yearly"
        )
    }

    /**
     * v1.25.4: launch реального Play Billing flow.
     * Activity нужен — Play UI рисуется поверх app.
     * После успеха PurchasesUpdatedListener в PlayBillingManager
     * вызовет subscriptionPrefs.setPro(true) → isPro StateFlow обновится
     * → init-collector эмитит Purchased event.
     */
    fun startPurchase(activity: Activity) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val plan = state.value.selectedPlan
            val basePlanId = when (plan) {
                PaywallPlan.MONTH -> PlayBillingManager.PLAN_MONTHLY
                PaywallPlan.YEAR -> PlayBillingManager.PLAN_YEARLY
            }
            // v1.25.84 ANL-1: log "trial_started" перед открытием Google Play UI
            com.spanishapp.service.Analytics.paywallTrialStarted(
                if (plan == PaywallPlan.MONTH) "monthly" else "yearly"
            )
            billing.launchPurchase(activity, basePlanId)
            // если юзер отменит — state.isLoading=true остаётся блокированным
            // пока не вернётся snap из listener. Резервный timeout-сброс через 60s:
            kotlinx.coroutines.delay(60_000)
            if (_state.value.isLoading) {
                _state.update { it.copy(isLoading = false) }
                // v1.25.84 ANL-1: timeout = юзер закрыл Play UI без покупки
                com.spanishapp.service.Analytics.paywallPurchaseCancelled(
                    if (plan == PaywallPlan.MONTH) "monthly" else "yearly"
                )
            }
        }
    }
}

enum class PaywallPlan { MONTH, YEAR }

data class PaywallState(
    val selectedPlan: PaywallPlan = PaywallPlan.YEAR,
    val isLoading: Boolean = false,
    val purchased: Boolean = false,
)

sealed class PurchaseEvent {
    object Purchased : PurchaseEvent()
}
