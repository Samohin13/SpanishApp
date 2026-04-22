package com.spanishapp

import android.app.Application
import com.spanishapp.data.db.DatabaseSeeder
import com.spanishapp.service.DailyReminderWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SpanishApp : Application() {

    @Inject lateinit var databaseSeeder: DatabaseSeeder

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        DailyReminderWorker.schedule(this)
        appScope.launch { databaseSeeder.seedIfNeeded() }
    }
}