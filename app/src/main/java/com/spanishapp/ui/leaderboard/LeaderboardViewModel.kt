package com.spanishapp.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.repository.LeaderboardData
import com.spanishapp.data.repository.LeaderboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
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

    // v1.22.3: НЕ вызываем deviceCountryCode() в инициализаторе StateFlow —
    // это срабатывает на main thread в момент создания ViewModel. На некоторых
    // девайсах (особенно с двойной SIM / без сети / с roaming) TelephonyManager
    // блокирует поток на ~3-7 секунд → ANR. Теперь стартовое значение "XX",
    // а реальная страна подгружается асинхронно в loadInitial().
    private val _state = MutableStateFlow(LeaderboardUiState(deviceCountry = "XX"))
    val state: StateFlow<LeaderboardUiState> = _state.asStateFlow()

    init {
        loadInitial()
    }

    private fun loadInitial() {
        viewModelScope.launch {
            val progress = userProgressDao.getProgressOnce()
            // deviceCountryCode дёргает TelephonyManager — оборачиваем в IO,
            // чтобы main thread не ждал. На большинстве устройств это быстро,
            // но защита нужна на edge-кейсах (плохая SIM, roaming, отсутствие сети).
            val country = withContext(Dispatchers.IO) {
                runCatching { repo.deviceCountryCode() }.getOrDefault("XX")
            }
            _state.value = _state.value.copy(
                optedIn = progress?.leaderboardOptIn == true,
                displayName = progress?.displayName.orEmpty().ifBlank { "Estudiante" },
                deviceCountry = country
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
            // v1.22.3: общий таймаут 15 секунд. Раньше при плохом интернете
            // Firestore await мог висеть «вечно» — индикатор крутился, юзер
            // тыкал refresh ещё раз, накапливались coroutine'ы → главный поток
            // в итоге блокировался Firebase internals → ANR.
            val data = withTimeoutOrNull(15_000L) {
                runCatching {
                    if (_state.value.optedIn) repo.syncSelf()
                    repo.fetch()
                }.getOrNull()
            }
            if (data != null) {
                _state.value = _state.value.copy(isLoading = false, data = data)
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Не удалось загрузить — проверь интернет"
                )
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
