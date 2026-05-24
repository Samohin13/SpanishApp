package com.spanishapp.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.service.XpTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** State machine for the first-launch onboarding. */
enum class OnboardingStep { Welcome, LevelSelect, Commitment, FirstWin, Done }

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.Welcome,
    val selectedLevel: String = OnboardingPrefs.DEFAULT_LEVEL,
    val dailyMinutes: Int = OnboardingPrefs.DEFAULT_DAILY_MINUTES,
    /** Number of cognates the user solved correctly in FirstWin (0..5). */
    val cognatesSolved: Int = 0,
    /** True once we've awarded the +50 XP — prevents double-credit. */
    val xpAwarded: Boolean = false,
    /** True when persistence is finished and host should navigate away. */
    val completed: Boolean = false,
    /** Adaptive route to navigate to after finish (null = default home flow). */
    val adaptiveRoute: String? = null,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: OnboardingPrefs,
    private val xpTracker: XpTracker,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    fun goTo(step: OnboardingStep) {
        _state.value = _state.value.copy(step = step)
    }

    fun next() {
        val current = _state.value.step
        val next = when (current) {
            OnboardingStep.Welcome     -> OnboardingStep.LevelSelect
            OnboardingStep.LevelSelect -> OnboardingStep.Commitment
            OnboardingStep.Commitment  -> OnboardingStep.FirstWin
            OnboardingStep.FirstWin    -> OnboardingStep.Done
            OnboardingStep.Done        -> OnboardingStep.Done
        }
        _state.value = _state.value.copy(step = next)
    }

    fun selectLevel(level: String) {
        _state.value = _state.value.copy(selectedLevel = level)
        viewModelScope.launch { prefs.saveLevel(level) }
    }

    fun selectDailyMinutes(minutes: Int) {
        _state.value = _state.value.copy(dailyMinutes = minutes)
        viewModelScope.launch { prefs.saveDailyGoal(minutes) }
    }

    fun cognateSolved() {
        val newCount = (_state.value.cognatesSolved + 1).coerceAtMost(5)
        _state.value = _state.value.copy(cognatesSolved = newCount)
    }

    /** Award the +50 XP for completing FirstWin — idempotent. */
    fun awardFirstWinXp() {
        if (_state.value.xpAwarded) return
        _state.value = _state.value.copy(xpAwarded = true)
        viewModelScope.launch {
            runCatching { xpTracker.add(xp = 50, words = 0) }
        }
    }

    /**
     * Persist completion state and signal the host to navigate.
     * Called from the Done screen "Начать" button OR from "Пропустить"
     * on any earlier step (in which case defaults are kept).
     */
    fun finish() {
        val s = _state.value
        viewModelScope.launch {
            prefs.saveLevel(s.selectedLevel)
            prefs.saveDailyGoal(s.dailyMinutes)
            prefs.markCompleted()
            val route = prefs.adaptiveEntryRoute(s.selectedLevel)
            _state.value = _state.value.copy(completed = true, adaptiveRoute = route)
        }
    }

    /**
     * Skip the entire flow. Saves defaults + marks completed so the
     * onboarding never shows again.
     */
    fun skip() {
        viewModelScope.launch {
            prefs.saveLevel(OnboardingPrefs.DEFAULT_LEVEL)
            prefs.saveDailyGoal(OnboardingPrefs.DEFAULT_DAILY_MINUTES)
            prefs.markCompleted()
            _state.value = _state.value.copy(completed = true, adaptiveRoute = null)
        }
    }
}
