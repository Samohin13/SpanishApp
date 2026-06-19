package com.spanishapp.data.repository

import android.util.Log
import com.spanishapp.BuildConfig
import com.spanishapp.data.prefs.SubscriptionPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SEC-1 (v1.25.76): server-side verification покупок через Cloudflare worker.
 *
 * Архитектура (Spotify/Netflix style):
 *  1. После успешной покупки в Google Play → app шлёт purchaseToken на
 *     /verify-purchase endpoint worker'а.
 *  2. Worker проверяет токен через Google Play Developer API.
 *  3. Worker записывает PRO статус в Cloudflare KV (привязан к Firebase UID).
 *  4. Worker возвращает {valid, state, expiryTime}.
 *  5. v1.25.90: на УСПЕХЕ кэшируем PRO=true + verifiedAt в SubscriptionPreferences.
 *     На неуспехе НИЧЕГО НЕ пишем — отзыв PRO целиком на совести caller'а
 *     (PlayBillingManager.handlePurchase делает это через свой
 *     isDefinitelyInvalid check, см. v1.25.83 fix).
 *
 * Offline-friendly поведение (Spotify-стиль):
 *  • Если intern — доверяем локальному кэшу до GRACE_PERIOD_MS (30 дней).
 *  • Если int есть и кэш старше REFRESH_INTERVAL_MS (24ч) → форсим verify.
 *  • Если int НЕТ и кэш старше 30 дней → PRO выключается ("Подключитесь к интернету").
 *
 * Безопасность:
 *  • Реверс-инженер не может подделать "PRO=true" локально — worker всё равно
 *    скажет 403 на /v1beta/models/.../generateContent если purchaseToken
 *    не зарегистрирован в KV.
 *  • Пиратский APK не сможет генерить чат вообще.
 */
@Singleton
class SubscriptionVerifier @Inject constructor(
    private val okHttp: OkHttpClient,
    private val subscriptionPrefs: SubscriptionPreferences,
) {

    companion object {
        private const val TAG = "SubVerifier"
        /** Как часто форсим refresh когда есть интернет. */
        const val REFRESH_INTERVAL_MS = 24L * 60 * 60 * 1000 // 24 часа
        /** Сколько доверяем локальному кэшу без интернета. */
        const val GRACE_PERIOD_MS = 30L * 24 * 60 * 60 * 1000 // 30 дней

        private fun verifyUrl(): String? {
            val proxy = BuildConfig.AI_PROXY_URL.trim().trimEnd('/')
            return if (proxy.isNotEmpty()) "$proxy/verify-purchase" else null
        }
    }

    data class VerifyResult(
        val valid: Boolean,
        val state: String? = null,
        val expiryTime: String? = null,
        val error: String? = null,
    )

    /**
     * Отправить purchaseToken на сервер для валидации.
     * Вызывать после каждой успешной покупки в [PlayBillingManager].
     */
    suspend fun verifyPurchase(
        purchaseToken: String,
        productId: String,
    ): VerifyResult = withContext(Dispatchers.IO) {
        val url = verifyUrl()
        if (url == null) {
            Log.w(TAG, "verifyPurchase: AI_PROXY_URL not configured, skipping server-side check")
            // Fallback: доверяем локальной покупке (development mode)
            return@withContext VerifyResult(valid = true, error = "proxy not configured")
        }
        val uid = currentFirebaseUid() ?: run {
            Log.w(TAG, "verifyPurchase: no Firebase uid, can't verify")
            return@withContext VerifyResult(valid = false, error = "no uid")
        }
        val idToken = firebaseIdToken() ?: run {
            Log.w(TAG, "verifyPurchase: no Firebase token, can't verify")
            return@withContext VerifyResult(valid = false, error = "no token")
        }
        val payload = JSONObject().apply {
            put("purchaseToken", purchaseToken)
            put("productId", productId)
            put("uid", uid)
        }
        val builder = Request.Builder()
            .url(url)
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .header("Authorization", "Bearer $idToken")
        val secret = BuildConfig.AI_PROXY_SECRET.trim()
        if (secret.isNotEmpty()) builder.header("X-App-Secret", secret)
        try {
            okHttp.newCall(builder.build()).execute().use { resp ->
                val body = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    Log.w(TAG, "verifyPurchase HTTP ${resp.code}: ${body.take(200)}")
                    return@withContext VerifyResult(valid = false, error = "HTTP ${resp.code}")
                }
                val json = JSONObject(body)
                val valid = json.optBoolean("valid", false)
                val state = json.optString("state").takeIf { it.isNotEmpty() }
                val expiryTime = json.optString("expiryTime").takeIf { it.isNotEmpty() }
                if (valid) {
                    subscriptionPrefs.setProVerified(true, System.currentTimeMillis())
                    Log.d(TAG, "Purchase verified: state=$state expires=$expiryTime")
                } else {
                    // v1.25.90 fix: НЕ отзываем PRO здесь. Раньше любой valid=false
                    // (включая HTTP 5xx, сетевые ошибки, 48ч лаг Play API со state=null)
                    // ронял isPro у реального платящего юзера. Теперь решение об
                    // отзыве PRO принимает ТОЛЬКО PlayBillingManager.handlePurchase
                    // через свой isDefinitelyInvalid check (state in EXPIRED/CANCELED/
                    // REVOKED). Сюда просто отдаём VerifyResult — пусть caller решает.
                    Log.w(TAG, "Purchase NOT valid: state=$state — caller decides revocation")
                }
                VerifyResult(valid, state, expiryTime)
            }
        } catch (e: Exception) {
            Log.w(TAG, "verifyPurchase network error", e)
            VerifyResult(valid = false, error = e.message)
        }
    }

    /**
     * Решает можно ли доверять локальному PRO статусу без обращения к серверу.
     * Логика:
     *  - Если в кэше "не PRO" → false (без вопросов).
     *  - Если в кэше "PRO" и verifiedAt < REFRESH_INTERVAL_MS назад → true.
     *  - Если в кэше "PRO" и verifiedAt < GRACE_PERIOD_MS назад → true (offline grace).
     *  - Если в кэше "PRO" но verifiedAt > GRACE_PERIOD_MS назад → false (force re-verify).
     */
    suspend fun isLocallyTrustedPro(): Boolean {
        val snap = subscriptionPrefs.snapshot()
        if (!snap.isPro) return false
        val age = System.currentTimeMillis() - snap.verifiedAt
        return age < GRACE_PERIOD_MS
    }

    /**
     * Нужно ли форсить refresh с сервера (например при app start).
     * true если есть интернет и кэш устарел.
     */
    suspend fun shouldRefresh(): Boolean {
        val snap = subscriptionPrefs.snapshot()
        if (snap.verifiedAt == 0L) return true // ни разу не верифицировали
        val age = System.currentTimeMillis() - snap.verifiedAt
        return age > REFRESH_INTERVAL_MS
    }

    private suspend fun currentFirebaseUid(): String? = runCatching {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val user = auth.currentUser ?: auth.signInAnonymously().await().user
        user?.uid
    }.getOrNull()

    private suspend fun firebaseIdToken(): String? = runCatching {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val user = auth.currentUser ?: auth.signInAnonymously().await().user
        user?.getIdToken(false)?.await()?.token
    }.getOrNull()
}
