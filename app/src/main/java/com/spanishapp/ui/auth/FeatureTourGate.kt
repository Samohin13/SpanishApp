package com.spanishapp.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.spanishapp.data.prefs.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Лёгкий gate-Composable, который при первом входе на главный экран
 * проверяет featureTourSeen и, если ещё не показан, делает navigate(
 * "feature_tour"). Вызывается из HomeScreen один раз через LaunchedEffect.
 *
 * Зачем выделено отдельно: HomeViewModel и без того перегружен 18-ю
 * зависимостями. Этот gate — single-purpose, без захвата ничего лишнего.
 */
@Composable
fun FeatureTourGate(
    navController: NavHostController,
    vm: FeatureTourGateViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        if (!vm.hasSeen()) {
            navController.navigate("feature_tour") {
                launchSingleTop = true
            }
        }
    }
}

@HiltViewModel
class FeatureTourGateViewModel @Inject constructor(
    private val prefs: AppPreferences,
) : ViewModel() {
    suspend fun hasSeen(): Boolean = prefs.featureTourSeen.first()
}
