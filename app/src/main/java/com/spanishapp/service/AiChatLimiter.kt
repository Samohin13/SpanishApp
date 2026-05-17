package com.spanishapp.service

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.aiLimitDataStore by preferencesDataStore(name = "ai_chat_limit")

/**
 * Клиентский счётчик AI-сообщений с дневным лимитом 50/день.
 *
 * Это **не** защита от злоумышленника (юзер может стереть data) — реальный
 * лимит будет в Cloudflare Worker через KV. Этот счётчик нужен чтобы:
 *   • Дать юзеру обратную связь «осталось 47/50 сегодня»
 *   • Защитить юзера от случайного перерасхода (попадает в Worker-лимит
 *     общий на всех юзеров → блокирует и других)
 *   • Подготовить UI для будущего PRO-лимита (50 free → unlimited PRO)
 *
 * Хранение: дата (YYYY-MM-DD UTC) + счётчик. При смене дня счётчик
 * сбрасывается автоматически.
 */
@Singleton
class AiChatLimiter @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        const val DAILY_LIMIT = 50
        private val DATE_KEY  = stringPreferencesKey("date")
        private val COUNT_KEY = intPreferencesKey("count")
    }

    /** Реальное число сообщений сегодня (с auto-сбросом при смене дня). */
    val usedToday: Flow<Int> = context.aiLimitDataStore.data.map { prefs ->
        val today = LocalDate.now().toString()
        if (prefs[DATE_KEY] == today) prefs[COUNT_KEY] ?: 0 else 0
    }

    /** Сколько ещё можно отправить сегодня. */
    val remainingToday: Flow<Int> = usedToday.map { (DAILY_LIMIT - it).coerceAtLeast(0) }

    /** True если уже исчерпан лимит. Вызывать перед отправкой. */
    suspend fun isExhausted(): Boolean {
        val used = usedToday.first()
        return used >= DAILY_LIMIT
    }

    /** Инкремент счётчика — вызывать ПОСЛЕ успешной отправки. */
    suspend fun increment() {
        val today = LocalDate.now().toString()
        context.aiLimitDataStore.edit { prefs ->
            val storedDate = prefs[DATE_KEY]
            val current = if (storedDate == today) prefs[COUNT_KEY] ?: 0 else 0
            prefs[DATE_KEY]  = today
            prefs[COUNT_KEY] = current + 1
        }
    }
}
