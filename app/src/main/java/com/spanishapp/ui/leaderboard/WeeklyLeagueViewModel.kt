package com.spanishapp.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.entity.WeeklyLeagueStateEntity
import com.spanishapp.service.WeeklyLeagueService
import com.spanishapp.service.WeeklyMember
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class WeeklyLeagueUiState(
    val isLoading: Boolean = false,
    val optedIn: Boolean = false,
    val state: WeeklyLeagueStateEntity? = null,
    val members: List<WeeklyMember> = emptyList(),
    val daysRemaining: Int = 0,
    val error: String? = null
)

@HiltViewModel
class WeeklyLeagueViewModel @Inject constructor(
    private val service: WeeklyLeagueService
) : ViewModel() {

    private val _ui = MutableStateFlow(WeeklyLeagueUiState())
    val ui: StateFlow<WeeklyLeagueUiState> = _ui.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true, error = null)
            try {
                service.ensureCurrentWeek()
                val state = service.getState()
                val members = if (state?.optedIn == true) service.getCohortLeaderboard()
                              else emptyList()
                _ui.value = WeeklyLeagueUiState(
                    isLoading = false,
                    optedIn = state?.optedIn == true,
                    state = state,
                    members = members,
                    daysRemaining = computeDaysRemaining()
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(isLoading = false, error = e.message ?: "Ошибка")
            }
        }
    }

    fun optIn() {
        viewModelScope.launch {
            service.setOptedIn(true)
            refresh()
        }
    }

    fun optOut() {
        viewModelScope.launch {
            service.setOptedIn(false)
            refresh()
        }
    }

    private fun computeDaysRemaining(): Int {
        val today = LocalDate.now()
        val nextMonday = today.with(DayOfWeek.MONDAY).plusWeeks(
            if (today.dayOfWeek == DayOfWeek.MONDAY) 1 else 1
        )
        return ChronoUnit.DAYS.between(today, nextMonday).toInt().coerceAtLeast(0)
    }
}
