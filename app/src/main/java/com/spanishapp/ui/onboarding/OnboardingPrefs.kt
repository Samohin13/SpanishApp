package com.spanishapp.ui.onboarding

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * First-launch onboarding state. Shown ONLY on first install of the app.
 *
 * Separate from auth-onboarding (NameEntry / Age / Reason / Level) — this
 * runs BEFORE auth and is gated by [isCompleted]. After completion the
 * user is dropped into the standard auth flow (or directly into home if
 * already logged in) and may be routed to an adaptive starting lesson
 * based on [startingLevel].
 *
 * Pref names use the `onboarding_*` prefix and live in their own DataStore
 * file (`onboarding_prefs`) so a future reset of auth onboarding (which is
 * tied to the Firebase user document) doesn't accidentally clear this flag.
 */
private val Context.onboardingDataStore by preferencesDataStore(name = "onboarding_prefs")

@Singleton
class OnboardingPrefs @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        val COMPLETED = booleanPreferencesKey("onboarding_completed")
        val STARTING_LEVEL = stringPreferencesKey("starting_level")
        val DAILY_MINUTES = intPreferencesKey("daily_minutes_goal")

        const val LEVEL_BEGINNER = "beginner"
        const val LEVEL_BASICS = "basics"
        const val LEVEL_A1 = "A1"
        const val LEVEL_A2 = "A2"

        const val DEFAULT_LEVEL = LEVEL_BEGINNER
        const val DEFAULT_DAILY_MINUTES = 10
    }

    /** Has the user completed (or explicitly skipped) the first-launch flow? */
    val isCompleted: Flow<Boolean> =
        context.onboardingDataStore.data.map { it[COMPLETED] ?: false }

    /** Self-declared starting level — used by adaptive entry point. */
    val startingLevel: Flow<String> =
        context.onboardingDataStore.data.map { it[STARTING_LEVEL] ?: DEFAULT_LEVEL }

    /** Daily minutes goal (5/10/20/30/60). */
    val dailyMinutesGoal: Flow<Int> =
        context.onboardingDataStore.data.map { it[DAILY_MINUTES] ?: DEFAULT_DAILY_MINUTES }

    suspend fun markCompleted() {
        context.onboardingDataStore.edit { it[COMPLETED] = true }
    }

    suspend fun saveLevel(level: String) {
        context.onboardingDataStore.edit { it[STARTING_LEVEL] = level }
    }

    suspend fun saveDailyGoal(minutes: Int) {
        context.onboardingDataStore.edit { it[DAILY_MINUTES] = minutes }
    }

    /**
     * Maps the self-declared level to a starting lesson route.
     * Returns null for "beginner" — meaning "use standard home flow"
     * (the user starts from the top of the roadmap as normal).
     */
    fun adaptiveEntryRoute(level: String): String? = when (level) {
        LEVEL_BASICS -> "lesson_session/1/4"  // u1_l4 — Greetings
        LEVEL_A1     -> "lesson_session/5/0"  // u5_l0 — Indefinido (start of A2)
        LEVEL_A2     -> "lesson_session/9/0"  // u9_l0 — Subjuntivo (start of B1)
        else         -> null                  // beginner — standard u1_l0 (home)
    }
}
