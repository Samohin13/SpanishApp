package com.spanishapp.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appLockDataStore by preferencesDataStore(name = "app_lock_prefs")

private object Keys {
    val ENABLED = booleanPreferencesKey("enabled")
}

/**
 * Хранит, включён ли App Lock (биометрический замок при входе в приложение).
 *
 * Отдельно от Firebase сессии: пользователь остаётся залогинен, но при
 * каждом холодном старте/возвращении из background показывается
 * биометрический prompt.
 */
@Singleton
class AppLockPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val isEnabled: Flow<Boolean> = context.appLockDataStore.data.map {
        it[Keys.ENABLED] ?: false
    }

    suspend fun setEnabled(enabled: Boolean) {
        context.appLockDataStore.edit { it[Keys.ENABLED] = enabled }
    }
}
