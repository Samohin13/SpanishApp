package com.spanishapp.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.spanishapp.service.XpTracker
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay

@EntryPoint
@InstallIn(SingletonComponent::class)
interface XpTrackerEntryPoint {
    fun xpTracker(): XpTracker
}

/**
 * Считает минуты, проведённые на учебном экране, и записывает их в [XpTracker].
 * Тикает раз в 60 секунд. Поставь в начало любого учебного Composable —
 * Flashcards / Practice / Lesson / Libro / Game.
 */
@Composable
fun TrackStudyMinutes() {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val tracker = entryPoint(context).xpTracker()
        while (true) {
            delay(60_000L)
            runCatching { tracker.recordMinute() }
        }
    }
}

private fun entryPoint(context: Context): XpTrackerEntryPoint =
    EntryPointAccessors.fromApplication(context.applicationContext, XpTrackerEntryPoint::class.java)
