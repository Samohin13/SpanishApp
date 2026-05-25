package com.spanishapp.ui.paywall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.service.SubscriptionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * v1.23.0: ViewModel paywall'а.
 *
 * Держит state выбранного плана (MONTH / YEAR) и в Фазе 5 будет
 * вызывать BillingClient.launchBillingFlow. Сейчас (Фаза 1) — кнопка
 * «Начать» через debugSetPro симулирует покупку для QA.
 */
@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val subscriptionManager: SubscriptionManager,
) : ViewModel() {

    private val _state = MutableStateFlow(PaywallState())
    val state: StateFlow<PaywallState> = _state.asStateFlow()

    fun selectPlan(plan: PaywallPlan) {
        _state.update { it.copy(selectedPlan = plan) }
    }

    /**
     * Старт покупки. Фаза 1 — debug-симуляция через SubscriptionManager
     * (без Play Billing). Фаза 5 заменит на launchBillingFlow.
     */
    fun startPurchase(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            // TODO Фаза 5: реальный BillingClient.launchBillingFlow
            //  productId = "espeak_pro_${state.value.selectedPlan.name.lowercase()}"
            subscriptionManager.debugSetPro(true)
            _state.update { it.copy(isLoading = false, purchased = true) }
            onSuccess()
        }
    }
}

enum class PaywallPlan { MONTH, YEAR }

data class PaywallState(
    val selectedPlan: PaywallPlan = PaywallPlan.YEAR,
    val isLoading: Boolean = false,
    val purchased: Boolean = false,
)
