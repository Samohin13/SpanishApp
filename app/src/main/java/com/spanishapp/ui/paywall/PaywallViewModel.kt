package com.spanishapp.ui.paywall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.service.SubscriptionManager
import dagger.hilt.android.lifecycle.HiltViewModel
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
) : ViewModel() {

    private val _state = MutableStateFlow(PaywallState())
    val state: StateFlow<PaywallState> = _state.asStateFlow()

    /** One-shot события: успешная покупка → закрыть paywall. */
    private val _events = Channel<PurchaseEvent>(Channel.BUFFERED)
    val events: Flow<PurchaseEvent> = _events.receiveAsFlow()

    fun selectPlan(plan: PaywallPlan) {
        _state.update { it.copy(selectedPlan = plan) }
    }

    /**
     * Старт покупки. Фаза 1 — debug-симуляция через SubscriptionManager
     * (без Play Billing). Фаза 5 заменит на launchBillingFlow.
     */
    fun startPurchase() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            // TODO Фаза 5: реальный BillingClient.launchBillingFlow
            //  productId = "espeak_pro_${state.value.selectedPlan.name.lowercase()}"
            subscriptionManager.debugSetPro(true)
            _state.update { it.copy(isLoading = false, purchased = true) }
            _events.send(PurchaseEvent.Purchased)
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
