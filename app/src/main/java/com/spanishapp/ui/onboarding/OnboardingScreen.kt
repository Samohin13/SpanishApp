package com.spanishapp.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.entity.UserProgressEntity
import com.spanishapp.ui.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── ViewModel ─────────────────────────────────────────────────

data class OnboardingState(
    val page: Int = 0,
    val name: String = "",
    val selectedLevel: String = "A1",
    val selectedGoal: Int = 10
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userProgressDao: UserProgressDao,
    private val authRepository: com.spanishapp.data.repository.AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun setName(n: String) { _state.value = _state.value.copy(name = n) }
    fun setLevel(l: String) { _state.value = _state.value.copy(selectedLevel = l) }
    fun setGoal(g: Int) { _state.value = _state.value.copy(selectedGoal = g) }
    fun nextPage() { _state.value = _state.value.copy(page = _state.value.page + 1) }
    fun prevPage() { _state.value = _state.value.copy(page = (_state.value.page - 1).coerceAtLeast(0)) }

    fun finish(onDone: () -> Unit) = viewModelScope.launch {
        val s = _state.value
        val existing = userProgressDao.getProgressOnce()
        val entry = existing?.copy(
            displayName      = s.name.ifBlank { "Estudiante" },
            currentLevel     = s.selectedLevel,
            dailyGoalMinutes = s.selectedGoal
        ) ?: UserProgressEntity(
            displayName      = s.name.ifBlank { "Estudiante" },
            currentLevel     = s.selectedLevel,
            dailyGoalMinutes = s.selectedGoal
        )
        if (existing == null) userProgressDao.insert(entry) else userProgressDao.update(entry)
        
        // Помечаем онбординг как завершенный!
        authRepository.setOnboardingCompleted(true)
        authRepository.setUserName(s.name.ifBlank { "Estudiante" })
        authRepository.setUserLevel(s.selectedLevel)

        onDone()
    }
}

// ── Screen ────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(
    navController: NavHostController,
    vm: OnboardingViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        AppColors.Terracotta.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // Точки прогресса
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { i ->
                    Box(
                        modifier = Modifier
                            .size(if (i == state.page) 24.dp else 8.dp, 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (i == state.page) AppColors.Terracotta
                                else AppColors.Terracotta.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Контент страницы
            AnimatedContent(
                targetState = state.page,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it } + fadeOut()
                },
                label = "onboarding_page"
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> NamePage(state.name, vm::setName)
                    2 -> LevelGoalPage(state.selectedLevel, state.selectedGoal, vm::setLevel, vm::setGoal)
                }
            }

            Spacer(Modifier.weight(1f))

            // Кнопки навигации
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.page > 0) {
                    OutlinedButton(
                        onClick = vm::prevPage,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) { Text("Назад") }
                }

                Button(
                    onClick = {
                        if (state.page < 2) vm.nextPage()
                        else vm.finish {
                            navController.navigate("home") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    },
                    shape    = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(if (state.page > 0) 2f else 1f).height(52.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = AppColors.Terracotta)
                ) {
                    Text(
                        if (state.page < 2) "Далее →" else "Начать учиться!",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Страница 1: Приветствие ────────────────────────────────────

@Composable
private fun WelcomePage() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🇪🇸", fontSize = 80.sp)
        Text(
            "¡Hola!",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.ExtraBold,
            color = AppColors.Terracotta
        )
        Text(
            "Добро пожаловать в\nприложение для изучения\nиспанского языка",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        // Фичи
        listOf(
            "🃏 Tarjetas с интервальным повторением",
            "🎮 Juegos: артикли, скорость, анаграммы",
            "🎙️ Тренажёр произношения",
            "🔊 Озвучка испанских слов"
        ).forEach { feature ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AppColors.Terracotta.copy(alpha = 0.08f)
                ) {
                    Text(
                        feature,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// ── Страница 2: Имя ───────────────────────────────────────────

@Composable
private fun NamePage(name: String, onNameChange: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("👋", fontSize = 64.sp)
        Text(
            "Как тебя зовут?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            "Это имя будет отображаться\nв твоём профиле",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        OutlinedTextField(
            value         = name,
            onValueChange = { if (it.length <= 20) onNameChange(it) },
            placeholder   = { Text("Твоё имя") },
            singleLine    = true,
            shape         = RoundedCornerShape(14.dp),
            modifier      = Modifier.fillMaxWidth()
        )
        if (name.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AppColors.Teal.copy(alpha = 0.1f)
            ) {
                Text(
                    "¡Hola, $name! Будем учиться вместе 🎉",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Teal,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Страница 3: Уровень + Цель ────────────────────────────────

@Composable
private fun LevelGoalPage(
    selectedLevel: String,
    selectedGoal: Int,
    onLevelChange: (String) -> Unit,
    onGoalChange: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("📊", fontSize = 56.sp)
        Text(
            "Твой уровень испанского?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        val levels = listOf(
            "A1" to "Начинающий — знаю пару слов",
            "A2" to "Основы — могу представиться",
            "B1" to "Средний — понимаю простые тексты",
            "B2" to "Выше среднего — могу общаться"
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            levels.forEach { (lvl, desc) ->
                Surface(
                    onClick  = { onLevelChange(lvl) },
                    shape    = RoundedCornerShape(14.dp),
                    color    = if (selectedLevel == lvl) AppColors.Teal.copy(alpha = 0.12f)
                               else MaterialTheme.colorScheme.surface,
                    border   = if (selectedLevel == lvl)
                                   ButtonDefaults.outlinedButtonBorder.copy(
                                       brush = androidx.compose.ui.graphics.SolidColor(AppColors.Teal)
                                   )
                               else ButtonDefaults.outlinedButtonBorder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(selected = selectedLevel == lvl, onClick = { onLevelChange(lvl) })
                        Column {
                            Text(lvl, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text(desc, style = MaterialTheme.typography.bodySmall,
                                 color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        HorizontalDivider()

        Text(
            "Дневная цель (минут):",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(5, 10, 15, 20, 30).forEach { min ->
                FilterChip(
                    selected = selectedGoal == min,
                    onClick  = { onGoalChange(min) },
                    label    = { Text("$min мин") }
                )
            }
        }
    }
}
