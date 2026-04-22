package com.spanishapp

import android.app.Application
import com.spanishapp.service.DailyReminderWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SpanishApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DailyReminderWorker.schedule(this)
    }
}