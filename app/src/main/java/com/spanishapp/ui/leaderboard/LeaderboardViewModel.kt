package com.spanishapp.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.prefs.CountryPreferences
import com.spanishapp.data.repository.LeaderboardData
import com.spanishapp.data.repository.LeaderboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class LeaderboardUiState(
    val isLoading: Boolean = false,
    val data: LeaderboardData? = null,
    val error: String? = null,
    val optedIn: Boolean = false,
    val deviceCountry: String = "XX",
    val displayName: String = "Estudiante",
    /** Открыта ли модалка обязательного ввода имени (когда оно дефолтное). */
    val needsNamePrompt: Boolean = false,
    /** Открыт ли picker для смены страны. */
    val showCountryPicker: Boolean = false,
    /** v1.26.1 (Model B): гость нажал «участвовать» — нужен аккаунт. */
    val needsAccount: Boolean = false,
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val repo: LeaderboardRepository,
    private val userProgressDao: UserProgressDao,
    private val countryPrefs: CountryPreferences,
    private val subscriptionManager: com.spanishapp.service.SubscriptionManager,
    // v1.26.1 (Model B): гейт участия для гостя.
    private val authRepository: com.spanishapp.data.repository.AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LeaderboardUiState(deviceCountry = "XX"))
    val state: StateFlow<LeaderboardUiState> = _state.asStateFlow()

    /** v1.23.0: PRO-статус собственного юзера для короны 👑 в лидерборде.
     *  Для других юзеров будет добавлен в Фазе 5 через Firestore. */
    val isMePro: StateFlow<Boolean> = subscriptionManager.isProActive
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadInitial()
    }

    private fun loadInitial() {
        viewModelScope.launch {
            val progress = userProgressDao.getProgressOnce()
            // Сначала показываем sync-результат мгновенно — потом обновляем
            // через async detect (IP-API fallback может занять до 4 сек).
            val syncCountry = withContext(Dispatchers.IO) {
                runCatching { repo.deviceCountryCode() }.getOrDefault("XX")
            }
            _state.value = _state.value.copy(
                optedIn = progress?.leaderboardOptIn == true,
                displayName = progress?.displayName.orEmpty().ifBlank { "Estudiante" },
                deviceCountry = syncCountry
            )
            // Async refine — может вернуть VN/TH/ID и другие где SIM пустой,
            // но IP-API определяет уверенно
            val asyncCountry = withContext(Dispatchers.IO) {
                runCatching { repo.deviceCountryCodeAsync() }.getOrDefault(syncCountry)
            }
            if (asyncCountry != syncCountry) {
                _state.value = _state.value.copy(deviceCountry = asyncCountry)
            }
            if (progress?.leaderboardOptIn == true) refresh()
        }
    }

    /**
     * Opt-in с защитой от race condition имени:
     *  • Если displayName пустое или дефолтное "Estudiante" — открываем
     *    модалку, не делаем opt-in пока юзер не введёт имя
     *  • Если имя нормальное — выполняем DAO update СЕКВЕНЦИАЛЬНО (через
     *    .join() suspend-вызовов), потом syncSelf
     */
    fun consumeNeedsAccount() { _state.value = _state.value.copy(needsAccount = false) }

    fun optIn() {
        val current = _state.value
        viewModelScope.launch {
            // v1.26.1 (Model B): гость не участвует в рейтинге (ghost-дубли) —
            // сначала регистрация.
            if (authRepository.guestMode.first()) {
                _state.value = _state.value.copy(needsAccount = true)
                return@launch
            }
            val name = current.displayName.trim()
            if (name.isBlank() || name == "Estudiante") {
                _state.value = _state.value.copy(needsNamePrompt = true)
                return@launch
            }
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

    /**
     * Гарантированно-секвенциальное обновление имени:
     *  1) DAO update — suspend, ждём завершения
     *  2) state.copy() — обновляем UI
     *  3) Если в opt-in — syncSelf(force=true), ждём
     *  4) refresh()
     *
     * Раньше п.3 шёл параллельно с п.1 → race condition.
     */
    fun updateDisplayName(name: String) {
        viewModelScope.launch {
            val trimmed = name.trim()
            if (trimmed.isBlank()) return@launch
            // 1) Гарантируем что DAO успеет записать ДО syncSelf
            userProgressDao.updateDisplayName(trimmed)
            // 2) Обновляем UI + закрываем модалку имени
            _state.value = _state.value.copy(displayName = trimmed, needsNamePrompt = false)
            // 3) Если opt-in — syncSelf. Если ещё нет — это была prompt-модалка,
            //    значит сразу делаем opt-in (юзер же нажал «Сохранить и присоединиться»)
            if (_state.value.optedIn) {
                runCatching { repo.syncSelf(force = true) }
                refresh()
            } else {
                userProgressDao.setLeaderboardOptIn(true)
                _state.value = _state.value.copy(optedIn = true)
                runCatching { repo.syncSelf(force = true) }
                refresh()
            }
        }
    }

    /** Юзер выбрал страну вручную через picker → сохраняем override + refresh. */
    fun setCountryOverride(iso: String) {
        viewModelScope.launch {
            countryPrefs.setOverride(iso)
            _state.value = _state.value.copy(deviceCountry = iso, showCountryPicker = false)
            if (_state.value.optedIn) {
                runCatching { repo.syncSelf(force = true) }
                refresh()
            }
        }
    }

    fun showCountryPicker() {
        _state.value = _state.value.copy(showCountryPicker = true)
    }

    fun dismissCountryPicker() {
        _state.value = _state.value.copy(showCountryPicker = false)
    }

    fun dismissNamePrompt() {
        _state.value = _state.value.copy(needsNamePrompt = false)
    }
}
