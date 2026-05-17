package com.spanishapp.ui.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.spanishapp.data.prefs.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 3-экранный feature-tour, показывается ОДИН РАЗ после auth-онбординга
 * (имя → уровень) перед первым входом на главный.
 *
 * Зачем нужен:
 *   • Юзер увидел только Welcome + ввод имени + placement-test и попадает
 *     на главный с 6 секциями. Без тура он не понимает что есть Слово дня,
 *     рассказы, игры — конверсия в первое упражнение низкая.
 *   • 3 swipe'а с эмодзи + одной строкой = 20 секунд внимания, но
 *     запоминается «что мне дальше делать».
 *
 * После 3-го экрана кнопка «Начать!» → ставит featureTourSeen=true →
 * popBackStack до 'home'.
 */
@Composable
fun FeatureTourScreen(
    navController: NavHostController,
    vm: FeatureTourViewModel = hiltViewModel(),
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val pages = listOf(
        TourPage(
            emoji = "📅",
            title = "Слово дня — каждое утро",
            body = "Короткий 5-стадийный квиз на 2 минуты. Запомнишь слово навсегда — push напомнит через час.",
            gradient = listOf(Color(0xFFFF6B35), Color(0xFFFF8F65)),
        ),
        TourPage(
            emoji = "🎮",
            title = "6 игр и 100 рассказов",
            body = "Закрепляй слова в реальных контекстах: кроссворды, спряжения, чтение с переводом по тапу.",
            gradient = listOf(Color(0xFF7C3AED), Color(0xFFB084FF)),
        ),
        TourPage(
            emoji = "🚀",
            title = "Начни с урока A1.1",
            body = "240 уроков от A1 до B2 — с озвучкой, упражнениями и адаптивным повтором по SM-2.",
            gradient = listOf(Color(0xFF06B6D4), Color(0xFF22D3EE)),
        ),
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Skip button — top right
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = {
                    vm.markSeen()
                    navController.popBackStack("feature_tour", inclusive = true)
                }) {
                    Text("Пропустить", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) { page ->
                TourPageContent(pages[page])
            }

            // Dot indicators
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(3) { i ->
                    val active = pagerState.currentPage == i
                    val width by animateFloatAsState(if (active) 28f else 8f, label = "dot-width")
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (active) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            // CTA — на последней странице меняется текст
            val isLast = pagerState.currentPage == 2
            Button(
                onClick = {
                    if (isLast) {
                        vm.markSeen()
                        navController.popBackStack("feature_tour", inclusive = true)
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text(
                    if (isLast) "Начать!" else "Дальше →",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun TourPageContent(page: TourPage) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Большой эмодзи на градиентном круге
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(page.gradient)),
            contentAlignment = Alignment.Center,
        ) {
            Text(page.emoji, fontSize = 64.sp)
        }
        Spacer(Modifier.height(40.dp))
        Text(
            page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            page.body,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )
    }
}

private data class TourPage(
    val emoji: String,
    val title: String,
    val body: String,
    val gradient: List<Color>,
)

@HiltViewModel
class FeatureTourViewModel @Inject constructor(
    private val prefs: AppPreferences,
) : ViewModel() {
    fun markSeen() {
        viewModelScope.launch { prefs.setFeatureTourSeen(true) }
    }
}
