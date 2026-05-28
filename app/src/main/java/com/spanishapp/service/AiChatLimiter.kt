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

/**
 * Клиентский счётчик AI-сообщений в чате: 50 сообщений в день для free-юзеров.
 * PRO-юзеры обходят лимит (бесконечно), счётчик не ведётся.
 *
 * Хранит:
 *  - last_date: дата последнего использования (YYYY-MM-DD) — сбрасывает счётчик на новый день
 *  - count_today: сколько сообщений отправлено сегодня
 *
 * Используется в:
 *  - AiChatViewModel: проверка isExhausted() перед отправкой
 *  - UI: показ остатка только free-юзерам
 */
private val Context.aiLimitDataStore by preferencesDataStore(name = "ai_chat_limit")

@Singleton
class AiChatLimiter @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val LAST_DATE = stringPreferencesKey("last_date")
    private val COUNT = intPreferencesKey("count_today")

    /** Сколько ещё запросов осталось сегодня. */
    val remainingToday: Flow<Int> = context.aiLimitDataStore.data.map { p ->
        val today = LocalDate.now().toString()
        val storedDate = p[LAST_DATE]
        val count = if (storedDate == today) (p[COUNT] ?: 0) else 0
        (DAILY_LIMIT - count).coerceAtLeast(0)
    }

    suspend fun isExhausted(): Boolean = remainingToday.first() <= 0

    suspend fun increment() {
        val today = LocalDate.now().toString()
        context.aiLimitDataStore.edit { p ->
            val storedDate = p[LAST_DATE]
            val cur = if (storedDate == today) (p[COUNT] ?: 0) else 0
            p[LAST_DATE] = today
            p[COUNT] = cur + 1
        }
    }

    /** Сброс — для debug/тестов. */
    suspend fun reset() {
        context.aiLimitDataStore.edit { it.clear() }
    }

    companion object {
        const val DAILY_LIMIT = 50
    }
}
