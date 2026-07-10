package com.spanishapp.service

import android.util.Log

/**
 * v1.18.24: Глобальный роутер TTS — позволяет любому коду
 * (включая extension functions без context) проверить готов ли premium
 * Google Cloud TTS и роутить туда вместо системного.
 *
 * Регистрируется один раз в [com.spanishapp.SpanishApp.onCreate] через
 * [register]. После этого `TextToSpeech.speakSpanish()` extension вызывает
 * [speakIfReady] чтобы попробовать premium перед fallback на системный TTS.
 */
object AppTtsRouter {
    @Volatile private var remoteTts: RemoteTtsService? = null

    fun register(service: RemoteTtsService) {
        remoteTts = service
        Log.d(TAG, "registered: isReady=${service.isReady.value}")
    }

    /**
     * Если premium TTS зарегистрирован и готов — играем через него и
     * возвращаем true. Caller тогда НЕ должен играть через системный TTS.
     *
     * @param onAllFailed v1.25.98 (audit tts-H1): пробрасывается в
     *   RemoteTtsService — вызывается при полном сетевом провале, чтобы
     *   caller сыграл через системный TTS (offline больше не = тишина).
     */
    fun speakIfReady(text: String, onAllFailed: (() -> Unit)? = null): Boolean {
        val rt = remoteTts ?: return false
        if (!rt.isReady.value) return false
        return rt.speak(text, onAllFailed = onAllFailed)
    }

    fun stop() {
        runCatching { remoteTts?.stop() }
    }

    private const val TAG = "AppTtsRouter"
}
