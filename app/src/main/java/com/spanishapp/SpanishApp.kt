package com.spanishapp

import android.app.Application
import com.spanishapp.data.db.DatabaseSeeder
import com.spanishapp.data.prefs.AppPreferences
import com.spanishapp.service.DailyReminderWorker
import com.spanishapp.service.RatingDecayWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SpanishApp : Application() {

    @Inject lateinit var databaseSeeder: DatabaseSeeder
    @Inject lateinit var appPreferences: AppPreferences

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Готов ли databaseSeeder.seedIfNeeded() — на первом старте seeding
     * 10К+ слов + 1300 глаголов + 100 рассказов занимает несколько секунд
     * на старых устройствах. MainActivity подписывается и держит splash
     * с прогрессом до true. На последующих запусках seedIfNeeded
     * отрабатывает мгновенно (всё уже в БД).
     */
    private val _seedReady = MutableStateFlow(false)
    val seedReady: StateFlow<Boolean> = _seedReady.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        // ВСЕ background-инициализации обёрнуты в try/catch.
        // Цель: даже если что-то падает (миграция, seeder, worker scheduling),
        // приложение всё равно стартует и юзер видит UI. Стектрейс пишется
        // в Crashlytics для диагностики без полного crash.
        appScope.launch {
            runCatching {
                val enabled = appPreferences.remindersEnabled.first()
                if (enabled) {
                    val hour = appPreferences.reminderHour.first()
                    val minute = appPreferences.reminderMinute.first()
                    DailyReminderWorker.schedule(this@SpanishApp, hour, minute)
                }
            }.onFailure { e ->
                com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                    .recordException(RuntimeException("[SpanishApp] DailyReminderWorker scheduling failed", e))
            }
        }
        runCatching { RatingDecayWorker.schedule(this) }.onFailure { e ->
            com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                .recordException(RuntimeException("[SpanishApp] RatingDecayWorker scheduling failed", e))
        }
        appScope.launch {
            runCatching {
                databaseSeeder.seedIfNeeded()
            }.onFailure { e ->
                // КРИТИЧНО: даже если seeder упал, отпускаем splash чтобы юзер
                // не застрял на загрузке. Может быть partial seed но это лучше
                // чем зависший splash.
                com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
                    .recordException(RuntimeException("[SpanishApp] seedIfNeeded FAILED", e))
            }
            _seedReady.value = true
        }
    }
}
