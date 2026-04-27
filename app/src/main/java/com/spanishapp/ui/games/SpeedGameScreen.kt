package com.spanishapp.ui.games

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.entity.WordEntity
import com.spanishapp.ui.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Уровни скорости ───────────────────────────────────────────

enum class SpeedLevel(
    val label: String,
    val emoji: String,
    val description: String,
    val timeLimitMs: Long
) {
    SLOW(   "Медленно", "🐢", "12 секунд на ответ", 12_000L),
    NORMAL( "Нормально","⚡", "6 секунд на ответ",   6_000L),
    FAST(   "Быстро",   "🔥", "3 секунды на ответ",  3_000L)
}

// ── State & ViewModel ─────────────────────────────────────────

data class SpeedState(
    val speedLevel: SpeedLevel? = null,      // null = ещё не выбрано
    val word: WordEntity? = null,
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val selectedIndex: Int? = null,
    val isCorrect: Boolean? = null,
    val score: Int = 0,
    val streak: Int = 0,
    val totalAnswered: Int = 0,
    val timeLeft: Float = 1f,
    val isFinished: Boolean = false,
    val isLoading: Boolean = true,
    val totalQuestions: Int = 15
)

@HiltViewModel
class SpeedGameViewModel @Inject constructor(
    private val wordDao: WordDao
) : ViewModel() {

    private val _state = MutableStateFlow(SpeedState())
    val state: StateFlow<SpeedState> = _state.asStateFlow()

    private var pool: List<WordEntity> = emptyList()
    private var poolIndex = 0
    private var timerJob: Job? = null

    companion object {
        const val TICK_MS = 50L
    }

    init { loadPool() }

    private fun loadPool() = viewModelScope.launch {
        pool = wordDao.getRandomWords(200)
            .filter { it.russian.isNotBlank() }
            .shuffled()
            .take(15)
        poolIndex = 0
        // Ждём выбора уровня скорости — не показываем вопрос
        _state.value = _state.value.copy(isLoading = false)
    }

    fun selectSpeed(level: SpeedLevel) {
        _state.value = _state.value.copy(speedLevel = level)
        showNext()
    }

    private fun showNext() {
        timerJob?.cancel()
        if (poolIndex >= pool.size) {
            _state.value = _state.value.copy(isFinished = true)
            return
        }
        val word = pool[poolIndex]
        val distractors = pool
            .filter { it.id != word.id }
            .shuffled()
            .take(3)
            .map { it.russian }
        val allOptions = (distractors + word.russian).shuffled()
        val correctIdx = allOptions.indexOf(word.russian)

        _state.value = _state.value.copy(
            word          = word,
            options       = allOptions,
            correctIndex  = correctIdx,
            selectedIndex = null,
            isCorrect     = null,
            timeLeft      = 1f,
            isLoading     = false
        )
        startTimer()
    }

    private fun startTimer() {
        val timeLimitMs = _state.value.speedLevel?.timeLimitMs ?: SpeedLevel.NORMAL.timeLimitMs
        timerJob = viewModelScope.launch {
            val steps = timeLimitMs / TICK_MS
            for (i in steps downTo 0) {
                _state.value = _state.value.copy(timeLeft = i / steps.toFloat())
                delay(TICK_MS)
            }
            if (_state.value.selectedIndex == null) timeUp()
        }
    }

    private fun timeUp() = viewModelScope.launch {
        val s = _state.value
        _state.value = s.copy(
            selectedIndex = -1,
            isCorrect = false,
            streak = 0,
            totalAnswered = s.totalAnswered + 1
        )
        delay(800)
        poolIndex++
        showNext()
    }

    fun select(index: Int) = viewModelScope.launch {
        val s = _state.value
        if (s.selectedIndex != null || s.word == null) return@launch
        timerJob?.cancel()

        val correct = index == s.correctIndex
        val bonus = when {
            s.timeLeft > 0.66f -> 15
            s.timeLeft > 0.33f -> 10
            else               -> 5
        }

        _state.value = s.copy(
            selectedIndex = index,
            isCorrect     = correct,
            score         = if (correct) s.score + bonus else s.score,
            streak        = if (correct) s.streak + 1 else 0,
            totalAnswered = s.totalAnswered + 1
        )
        delay(700)
        poolIndex++
        showNext()
    }

    fun restart() {
        timerJob?.cancel()
        pool = pool.shuffled()
        poolIndex = 0
        _state.value = SpeedState()
        loadPool()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

// ── Screen ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedGameScreen(
    navController: NavHostController,
    vm: SpeedGameViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("На скорость ⚡") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    if (!state.isFinished && state.speedLevel != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            if (state.streak >= 2) {
                                Text("🔥 ${state.streak}", style = MaterialTheme.typography.titleMedium)
                            }
                            Text(
                                "⭐ ${state.score}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Gold
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading   -> CircularProgressIndicator(color = AppColors.Terracotta)
                state.isFinished  -> SpeedResult(state, vm::restart) { navController.popBackStack() }
                // Экран выбора скорости — пока не выбран уровень
                state.speedLevel == null -> SpeedLevelSelector(vm::selectSpeed)
                state.word != null -> SpeedQuestion(state, vm::select)
            }
        }
    }
}

// ── Выбор уровня скорости ─────────────────────────────────────

@Composable
private fun SpeedLevelSelector(onSelect: (SpeedLevel) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚡", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            "Выбери скорость",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            "Чем быстрее — тем больше очков за правильный ответ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        SpeedLevel.entries.forEach { level ->
            val color = when (level) {
                SpeedLevel.SLOW   -> AppColors.Teal
                SpeedLevel.NORMAL -> AppColors.Gold
                SpeedLevel.FAST   -> AppColors.Terracotta
            }
            Surface(
                onClick = { onSelect(level) },
                shape = RoundedCornerShape(18.dp),
                color = color.copy(alpha = 0.1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(level.emoji, fontSize = 32.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            level.label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                        Text(
                            level.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Бонус за скорость
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = color.copy(alpha = 0.15f)
                    ) {
                        Text(
                            when (level) {
                                SpeedLevel.SLOW   -> "+5–15"
                                SpeedLevel.NORMAL -> "+5–15"
                                SpeedLevel.FAST   -> "+5–15"
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

// ── Вопрос ────────────────────────────────────────────────────

@Composable
private fun SpeedQuestion(state: SpeedState, onSelect: (Int) -> Unit) {
    val word = state.word ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Прогресс сессии
        LinearProgressIndicator(
            progress  = { state.totalAnswered / state.totalQuestions.toFloat() },
            modifier  = Modifier.fillMaxWidth().height(5.dp),
            color     = AppColors.Terracotta,
            trackColor = AppColors.Terracotta.copy(alpha = 0.15f)
        )

        // Таймер-бар (меняет цвет)
        val timerColor by animateColorAsState(
            targetValue = when {
                state.timeLeft > 0.5f  -> AppColors.Teal
                state.timeLeft > 0.25f -> AppColors.Gold
                else                   -> MaterialTheme.colorScheme.error
            },
            label = "timer_color"
        )
        LinearProgressIndicator(
            progress  = { state.timeLeft },
            modifier  = Modifier.fillMaxWidth().height(8.dp),
            color     = timerColor,
            trackColor = timerColor.copy(alpha = 0.15f)
        )

        Spacer(Modifier.weight(0.2f))

        // Карточка слова
        AnimatedContent(targetState = word.spanish, label = "speed_word") { spanish ->
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = AppColors.Terracotta.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Переведи:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        spanish,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.weight(0.2f))

        // Варианты ответов
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.options.forEachIndexed { i, option ->
                val answered  = state.selectedIndex != null
                val isSelected = state.selectedIndex == i
                val isCorrect  = state.correctIndex == i

                val containerColor = when {
                    !answered  -> MaterialTheme.colorScheme.surface
                    isCorrect  -> AppColors.Teal.copy(alpha = 0.2f)
                    isSelected -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    else       -> MaterialTheme.colorScheme.surface
                }
                val borderColor = when {
                    !answered  -> MaterialTheme.colorScheme.outlineVariant
                    isCorrect  -> AppColors.Teal
                    isSelected -> MaterialTheme.colorScheme.error
                    else       -> MaterialTheme.colorScheme.outlineVariant
                }

                OutlinedButton(
                    onClick  = { onSelect(i) },
                    enabled  = !answered,
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(containerColor = containerColor),
                    border   = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(borderColor)
                    ),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    Text(
                        option,
                        style      = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isCorrect && answered) FontWeight.Bold else FontWeight.Normal,
                        color      = if (isCorrect && answered) AppColors.Teal
                                     else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(Modifier.weight(0.1f))
    }
}

// ── Результат ─────────────────────────────────────────────────

@Composable
private fun SpeedResult(state: SpeedState, onRetry: () -> Unit, onBack: () -> Unit) {
    val total = state.totalAnswered
    val pct   = if (total > 0) state.score * 100 / (total * 15) else 0

    val emoji   = when { pct >= 80 -> "🏆"; pct >= 60 -> "🎉"; pct >= 40 -> "👍"; else -> "💪" }
    val message = when { pct >= 80 -> "Молниеносно!"; pct >= 60 -> "Отлично!"; pct >= 40 -> "Неплохо!"; else -> "Тренируйся ещё!" }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 72.sp)
        Spacer(Modifier.height(16.dp))
        Text("$total вопросов", style = MaterialTheme.typography.displayMedium,
             fontWeight = FontWeight.Bold)
        Text(message, style = MaterialTheme.typography.titleMedium,
             color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Surface(shape = RoundedCornerShape(12.dp), color = AppColors.Gold.copy(alpha = 0.15f)) {
            Text("⭐ ${state.score} очков",
                 modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                 style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                 color = AppColors.GoldDark)
        }
        Spacer(Modifier.height(32.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().height(52.dp),
               shape = RoundedCornerShape(14.dp)) {
            Text("Ещё раз", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(52.dp),
                       shape = RoundedCornerShape(14.dp)) {
            Text("К играм")
        }
    }
}
