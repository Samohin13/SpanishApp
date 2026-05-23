package com.spanishapp.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.statsDataStore by preferencesDataStore(name = "stats_prefs")

private object StatsKeys {
    val PERIOD = stringPreferencesKey("period")
}

@Singleton
class StatsPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val period: Flow<String> = context.statsDataStore.data.map {
        it[StatsKeys.PERIOD] ?: "WEEK"
    }

    suspend fun setPeriod(period: String) {
        context.statsDataStore.edit { it[StatsKeys.PERIOD] = period }
    }
}
