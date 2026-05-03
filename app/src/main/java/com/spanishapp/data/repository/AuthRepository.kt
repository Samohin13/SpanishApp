package com.spanishapp.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    private val USER_LEVEL = stringPreferencesKey("user_level")
    private val USER_NAME = stringPreferencesKey("user_name")
    private val USER_PHOTO = stringPreferencesKey("user_photo_url")
    private val USER_AGE = intPreferencesKey("user_age")
    private val USER_REASON = stringPreferencesKey("user_reason")
    private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_LOGGED_IN] ?: false
        }

    val userLevel: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_LEVEL]
        }

    val userName: Flow<String?> = context.dataStore.data.map { it[USER_NAME] }
    val userAge: Flow<Int?> = context.dataStore.data.map { it[USER_AGE] }
    val userReason: Flow<String?> = context.dataStore.data.map { it[USER_REASON] }
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_COMPLETED] ?: false }

    val userPhotoUrl: Flow<String?> = context.dataStore.data.map { it[USER_PHOTO] }

    suspend fun setUserPhotoUrl(url: String) {
        context.dataStore.edit { it[USER_PHOTO] = url }
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = loggedIn
        }
    }

    suspend fun setUserLevel(level: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_LEVEL] = level
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[USER_NAME] = name }
    }

    suspend fun setUserAge(age: Int) {
        context.dataStore.edit { it[USER_AGE] = age }
    }

    suspend fun setUserReason(reason: String) {
        context.dataStore.edit { it[USER_REASON] = reason }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[ONBOARDING_COMPLETED] = completed }
    }
}
