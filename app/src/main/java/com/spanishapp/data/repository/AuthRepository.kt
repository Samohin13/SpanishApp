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
    // v1.18.11: поля для AI auto-learn — заполняются из чата ИИ
    private val USER_INTERESTS = stringPreferencesKey("user_interests")  // CSV
    private val USER_GOAL = stringPreferencesKey("user_goal")            // конкретная цель
    private val USER_NOTES = stringPreferencesKey("user_notes")          // стиль общения, особенности
    // v1.18.11: gender для правильного грамматического рода в AI ответах
    private val USER_GENDER = stringPreferencesKey("user_gender")        // "male" | "female"
    // v1.18.20: выбранный характер репетитора (TutorPersonality.id)
    private val TUTOR_PERSONALITY = stringPreferencesKey("tutor_personality")
    // v1.18.21: пол голоса репетитора (VoiceGender.id — "female" | "male")
    private val VOICE_GENDER = stringPreferencesKey("voice_gender")
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
    // v1.18.11: AI-learned поля
    val userInterests: Flow<String?> = context.dataStore.data.map { it[USER_INTERESTS] }
    val userGoal: Flow<String?> = context.dataStore.data.map { it[USER_GOAL] }
    val userNotes: Flow<String?> = context.dataStore.data.map { it[USER_NOTES] }

    suspend fun setUserInterests(interests: String) {
        context.dataStore.edit { it[USER_INTERESTS] = interests }
    }
    suspend fun setUserGoal(goal: String) {
        context.dataStore.edit { it[USER_GOAL] = goal }
    }
    suspend fun setUserNotes(notes: String) {
        context.dataStore.edit { it[USER_NOTES] = notes }
    }

    val userGender: Flow<String?> = context.dataStore.data.map { it[USER_GENDER] }
    suspend fun setUserGender(gender: String) {
        context.dataStore.edit { it[USER_GENDER] = gender }
    }

    // v1.18.20: характер репетитора
    val tutorPersonality: Flow<String?> = context.dataStore.data.map { it[TUTOR_PERSONALITY] }
    suspend fun setTutorPersonality(id: String) {
        context.dataStore.edit { it[TUTOR_PERSONALITY] = id }
    }

    // v1.18.21: пол голоса репетитора (общий для ru+es)
    val voiceGender: Flow<String?> = context.dataStore.data.map { it[VOICE_GENDER] }
    suspend fun setVoiceGender(gender: String) {
        context.dataStore.edit { it[VOICE_GENDER] = gender }
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_COMPLETED] ?: false }

    val userPhotoUrl: Flow<String?> = context.dataStore.data.map { it[USER_PHOTO] }

    suspend fun setUserPhotoUrl(url: String) {
        context.dataStore.edit { it[USER_PHOTO] = url }
    }

    /** Removes the stored photo URL (so the avatar falls back to placeholder). */
    suspend fun clearUserPhoto() {
        context.dataStore.edit { it.remove(USER_PHOTO) }
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
