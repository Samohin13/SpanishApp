package com.spanishapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.spanishapp.data.db.DatabaseSeeder
import com.spanishapp.service.DailyReminderWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SpanishApp : Application(), Configuration.Provider {

    @Inject lateinit var databaseSeeder: DatabaseSeeder
    @Inject lateinit var workerFactory: HiltWorkerFactory

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        DailyReminderWorker.schedule(this)
        appScope.launch { databaseSeeder.seedIfNeeded() }
    }
}
