package com.spanishapp.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.repository.LeaderboardData
import com.spanishapp.data.repository.LeaderboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeaderboardUiState(
    val isLoading: Boolean = false,
    val data: LeaderboardData? = null,
    val error: String? = null,
    val optedIn: Boolean = false,
    val deviceCountry: String = "XX",
    val displayName: String = "Estudiante"
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val repo: LeaderboardRepository,
    private val userProgressDao: UserProgressDao
) : ViewModel() {

    private val _state = MutableStateFlow(LeaderboardUiState(deviceCountry = repo.deviceCountryCode()))
    val state: StateFlow<LeaderboardUiState> = _state.asStateFlow()

    init {
        loadInitial()
    }

    private fun loadInitial() {
        viewModelScope.launch {
            val progress = userProgressDao.getProgressOnce()
            _state.value = _state.value.copy(
                optedIn = progress?.leaderboardOptIn == true,
                displayName = progress?.displayName.orEmpty().ifBlank { "Estudiante" },
                deviceCountry = repo.deviceCountryCode()
            )
            if (progress?.leaderboardOptIn == true) refresh()
        }
    }

    fun optIn() {
        viewModelScope.launch {
            userProgressDao.setLeaderboardOptIn(true)
            _state.value = _state.value.copy(optedIn = true)
            try {
                repo.syncSelf(force = true)
                refresh()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Не удалось подключиться")
            }
        }
    }

    fun optOut() {
        viewModelScope.launch {
            userProgressDao.setLeaderboardOptIn(false)
            try { repo.deleteSelf() } catch (_: Exception) {}
            _state.value = _state.value.copy(optedIn = false, data = null)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                if (_state.value.optedIn) repo.syncSelf()
                val data = repo.fetch()
                _state.value = _state.value.copy(isLoading = false, data = data)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun updateDisplayName(name: String) {
        viewModelScope.launch {
            userProgressDao.updateDisplayName(name)
            _state.value = _state.value.copy(displayName = name)
            if (_state.value.optedIn) {
                runCatching { repo.syncSelf(force = true) }
                refresh()
            }
        }
    }
}
