package com.spanishapp.service

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.ui.test.filter
import androidx.privacysandbox.tools.core.generator.build
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.spanishapp.data.prefs.SubscriptionPreferences
import com.spanishapp.data.repository.SubscriptionVerifier
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google Play Billing — реальные PRO подписки.
 *
 * Setup в Play Console:
 *  1. Создать subscription с id PRODUCT_ID (espeak_pro)
 *  2. Создать 2 base-plans:
 *     - monthly (basePlanId=monthly): $4.99
 *     - yearly (basePlanId=yearly): $34.99, опционально с 7-day free trial
 *  3. Internal testing track + добавить tester accounts
 *  4. В debug-сборке для тестирования: тестовый аккаунт + license testing
 *
 * Поток:
 *  • startConnection() — устанавливает BillingClient connection
 *  • queryProducts() — получает actual prices (locale-aware)
 *  • launchPurchase(activity, plan) — открывает Play purchase sheet
 *  • PurchasesUpdatedListener получает результат → acknowledgement
 *  • restorePurchases() — query existing purchases on app start
 *  • Синхронизация с SubscriptionPreferences (isPro)
 *
 * ⚠️ Acknowledgement в течение 3 дней ОБЯЗАТЕЛЕН (Play rule).
 * Иначе purchase автоматически рефандится.
 */
@Singleton
class PlayBillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val subscriptionPrefs: SubscriptionPreferences,
    // Lazy чтобы избежать циклической зависимости (Verifier → OkHttp → ...).
    private val verifier: Lazy<SubscriptionVerifier>,
) {
    private val TAG = "PlayBillingManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _productDetails = MutableStateFlow<List<ProductDetails>>(emptyList())
    val productDetails: StateFlow<List<ProductDetails>> = _productDetails.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val purchaseListener = PurchasesUpdatedListener { result, purchases ->
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { handlePurchase(it) }
        } else if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled purchase")
        } else {
            Log.w(TAG, "Purchase failed: ${result.debugMessage} (code=${result.responseCode})")
        }
    }

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchaseListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED, FAILED }

    /** Подключиться к Play Billing. Вызвать из Application или MainActivity onCreate. */
    fun start() {
        if (_connectionState.value == ConnectionState.CONNECTED) return
        _connectionState.value = ConnectionState.CONNECTING
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    _connectionState.value = ConnectionState.CONNECTED
                    Log.d(TAG, "Billing connected")
                    scope.launch {
                        queryProducts()
                        restorePurchases()
                    }
                } else {
                    _connectionState.value = ConnectionState.FAILED
                    Log.e(TAG, "Billing setup failed: ${result.debugMessage}")
                }
            }
            override fun onBillingServiceDisconnected() {
                _connectionState.value = ConnectionState.DISCONNECTED
                // Можно реконнектиться экспоненциально, но Play SDK сам это делает
            }
        })
    }

    /** Запросить детали подписки (цены, base plans). */
    suspend fun queryProducts() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()
        val result = client.queryProductDetails(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            _productDetails.value = result.productDetailsList ?: emptyList()
            Log.d(TAG, "Loaded ${_productDetails.value.size} products")
        } else {
            Log.e(TAG, "queryProducts failed: ${result.billingResult.debugMessage}")
        }
    }

    /**
     * Запустить purchase flow.
     * @param activity host activity (нужно Play UI)
     * @param basePlanId "monthly" или "yearly" — соответствует Play Console
     */
    fun launchPurchase(activity: Activity, basePlanId: String) {
        val product = _productDetails.value.firstOrNull { it.productId == PRODUCT_ID } ?: run {
            Log.e(TAG, "Product not loaded yet")
            return
        }

        // v1.25.85 FIX: Ищем оффер с бесплатным триалом (цена == 0).
        val offer = product.subscriptionOfferDetails?.let { offers ->
            offers.filter { it.basePlanId == basePlanId }
                .find { off ->
                    off.pricingPhases.pricingPhaseList.any { it.priceAmountMicros == 0L }
                } ?: offers.firstOrNull { it.basePlanId == basePlanId }
        } ?: product.subscriptionOfferDetails?.firstOrNull() ?: run {
            Log.e(TAG, "No subscription offers for $basePlanId")
            return
        }

        val params = com.android.billingclient.api.BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    com.android.billingclient.api.BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(product)
                        .setOfferToken(offer.offerToken)
                        .build()
                )
            )
            .build()
        client.launchBillingFlow(activity, params)
    }

    /** Restore — на старте app или по запросу юзера ("Restore purchases" в Settings). */
    suspend fun restorePurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        val result = client.queryPurchasesAsync(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val active = result.purchasesList.any { p ->
                p.purchaseState == Purchase.PurchaseState.PURCHASED &&
                    p.products.contains(PRODUCT_ID)
            }
            // v1.25.79 fix: в debug-сборках НЕ перетираем PRO статус через
            // setPro(false). Иначе debug PRO toggle сбрасывался при каждом
            // app start (restorePurchases вызывается из start()) → юзер
            // включал PRO в Settings и сразу терял его после перезапуска.
            // В release setPro(active) работает как раньше.
            if (com.spanishapp.BuildConfig.DEBUG && !active) {
                Log.d(TAG, "Debug build: skip setPro(false) to preserve debug toggle")
            } else {
                scope.launch { subscriptionPrefs.setPro(active) }
            }
            result.purchasesList.forEach { handlePurchase(it) }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        if (!purchase.products.contains(PRODUCT_ID)) return

        // 1. Активируем PRO в DataStore (оптимистично, чтоб UI не ждал сервер)
        scope.launch { subscriptionPrefs.setPro(true) }

        // 2. SEC-1 (v1.25.76): server-side verification.
        //    Шлём purchaseToken на worker → worker проверяет через Google Play
        //    Developer API → пишет PRO в Cloudflare KV (привязка к Firebase UID).
        //
        //    v1.25.83 fix: НЕ отменяем PRO при unclear ответе сервера.
        //    Reality:
        //     - Play API часто отдаёт state=null если подписка свежая
        //       (синхронизация занимает до 48ч после первой покупки)
        //     - Service Account permissions распространяются 1-24ч
        //     - Сетевые ошибки бывают
        //    Если сервер не может однозначно сказать «это refund/expired/canceled»
        //    — доверяем Google Play Billing локально. Это реальная покупка,
        //    отменять её из-за лагов API = выстрелить себе в ногу.
        //
        //    Отзываем PRO ТОЛЬКО при явных negative state:
        //     - SUBSCRIPTION_STATE_EXPIRED
        //     - SUBSCRIPTION_STATE_CANCELED
        //     - "valid":false с конкретным state, не null.
        scope.launch {
            try {
                val result = verifier.get().verifyPurchase(
                    purchaseToken = purchase.purchaseToken,
                    productId = PRODUCT_ID,
                )
                val isDefinitelyInvalid = !result.valid && result.state in setOf(
                    "SUBSCRIPTION_STATE_EXPIRED",
                    "SUBSCRIPTION_STATE_CANCELED",
                    "SUBSCRIPTION_STATE_REVOKED",
                )
                if (result.valid) {
                    Log.d(TAG, "Server verify OK: state=${result.state} expires=${result.expiryTime}")
                } else if (isDefinitelyInvalid) {
                    Log.w(TAG, "Server verify FAILED definitively: state=${result.state}, revoking PRO")
                    subscriptionPrefs.setProVerified(false, System.currentTimeMillis())
                } else {
                    Log.w(TAG, "Server verify UNCLEAR (state=${result.state}, error=${result.error}) — trusting Google Play Billing locally")
                    // Не отменяем PRO. Реальная покупка прошла — Play API
                    // догонит через несколько часов и при следующем app start
                    // verifyPurchase вернёт корректный SUBSCRIPTION_STATE_ACTIVE.
                }
            } catch (e: Exception) {
                Log.w(TAG, "Server verify exception (network?), trusting Play locally", e)
            }
        }

        // 3. Acknowledgement в течение 3 дней — обязательно (Play rule)
        if (!purchase.isAcknowledged) {
            scope.launch {
                val ackParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                val ackResult = client.acknowledgePurchase(ackParams)
                if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Purchase acknowledged")
                } else {
                    Log.e(TAG, "Acknowledge failed: ${ackResult.debugMessage}")
                }
            }
        }
    }

    fun stop() {
        try { client.endConnection() } catch (_: Exception) {}
    }

    companion object {
        /** Subscription product id в Play Console. */
        const val PRODUCT_ID = "espeak_pro"
        const val PLAN_MONTHLY = "monthly"
        const val PLAN_YEARLY = "yearly"
    }
}
