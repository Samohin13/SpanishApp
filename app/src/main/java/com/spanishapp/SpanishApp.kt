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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SpanishApp : Application() {

    @Inject lateinit var databaseSeeder: DatabaseSeeder
    @Inject lateinit var appPreferences: AppPreferences

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            // Запускаем worker на сохранённое время (по умолчанию 19:00).
            val enabled = appPreferences.remindersEnabled.first()
            if (enabled) {
                val hour = appPreferences.reminderHour.first()
                val minute = appPreferences.reminderMinute.first()
                DailyReminderWorker.schedule(this@SpanishApp, hour, minute)
            }
        }
        RatingDecayWorker.schedule(this)
        appScope.launch { databaseSeeder.seedIfNeeded() }
    }
}
