package com.spanishapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.spanishapp.service.StreakResult
import com.spanishapp.service.StreakService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * v1.25.5: глобальный hostBanner, показывает уведомление когда freeze
 * применился ("❄ Стрик сохранён за счёт заморозки"). Без этого юзер не
 * знал что freeze тратился — теперь explicit feedback.
 *
 * Mount'ится в MainActivity рядом с AchievementUnlockHost.
 * Слушает StreakService.streakEvents. При usedFreeze=true → показывает
 * баннер сверху на 4 секунды + slide-in/fade-out анимации.
 */

@HiltViewModel
class StreakFreezePopupViewModel @Inject constructor(
    streakService: StreakService,
) : ViewModel() {
    private val _currentBanner = MutableStateFlow<StreakResult?>(null)
    val currentBanner: StateFlow<StreakResult?> = _currentBanner.asStateFlow()

    init {
        viewModelScope.launch {
            streakService.streakEvents.collect { result ->
                if (result.usedFreeze) {
                    _currentBanner.value = result
                    // Hide после 4 секунд
                    delay(4_000)
                    if (_currentBanner.value == result) {
                        _currentBanner.value = null
                    }
                }
            }
        }
    }

    fun dismiss() {
        _currentBanner.value = null
    }
}

@Composable
fun StreakFreezePopupHost(vm: StreakFreezePopupViewModel = hiltViewModel()) {
    val banner by vm.currentBanner.collectAsStateWithLifecycle()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        AnimatedVisibility(
            visible = banner != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
        ) {
            banner?.let { result ->
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF60A5FA), Color(0xFF2563EB)),
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text("❄", fontSize = 24.sp)
                        Column {
                            Text(
                                "Стрик сохранён!",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "Замороз использован · осталось ${result.freezesAvailable}/${StreakService.MAX_FREEZES}",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.5.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}
