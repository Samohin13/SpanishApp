package com.spanishapp.ui.games

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

// ── Уровни ────────────────────────────────────────────────────

enum class SpeedLevel(
    val label: String,
    val emoji: String,
    val description: String,
    val timeLimitMs: Long,
    val color: Color
) {
    SLOW(   "Медленно", "🐢", "12 сек на ответ", 12_000L, Color(0xFF4CAF50)),
    NORMAL( "Нормально","⚡", "6 сек на ответ",   6_000L, Color(0xFFFFC107)),
    FAST(   "Быстро",   "🔥", "3 сек на ответ",  3_000L, Color(0xFFF44336))
}

// ── State & ViewModel ─────────────────────────────────────────

data class SpeedState(
    val speedLevel: SpeedLevel? = null,
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

    init { loadPool() }

    private fun loadPool() = viewModelScope.launch {
        pool = wordDao.getRandomWords(200)
            .filter { it.russian.isNotBlank() }
            .shuffled()
            .take(15)
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
        val distractors = pool.filter { it.id != word.id }.shuffled().take(3).map { it.russian }
        val allOptions = (distractors + word.russian).shuffled()
        val correctIdx = allOptions.indexOf(word.russian)

        _state.value = _state.value.copy(
            word          = word,
            options       = allOptions,
            correctIndex  = correctIdx,
            selectedIndex = null,
            isCorrect     = null,
            timeLeft      = 1f
        )
        startTimer()
    }

    private fun startTimer() {
        val limit = _state.value.speedLevel?.timeLimitMs ?: 6000L
        timerJob = viewModelScope.launch {
            val steps = (limit / 50).toInt()
            for (i in steps downTo 0) {
                _state.value = _state.value.copy(timeLeft = i / steps.toFloat())
                delay(50)
            }
            if (_state.value.selectedIndex == null) timeUp()
        }
    }

    private fun timeUp() = viewModelScope.launch {
        _state.value = _state.value.copy(selectedIndex = -1, isCorrect = false, streak = 0, totalAnswered = _state.value.totalAnswered + 1)
        delay(800)
        poolIndex++
        showNext()
    }

    fun select(index: Int) = viewModelScope.launch {
        val s = _state.value
        if (s.selectedIndex != null) return@launch
        timerJob?.cancel()

        val correct = index == s.correctIndex
        val bonus = (s.timeLeft * 15).toInt().coerceAtLeast(5)

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
        poolIndex = 0
        _state.value = SpeedState()
        loadPool()
    }

    override fun onCleared() { timerJob?.cancel() }
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
                title = { Text("На скорость", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    if (!state.isFinished && state.speedLevel != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AppColors.Terracotta.copy(alpha = 0.15f),
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Text(
                                "⭐ ${state.score}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppColors.Terracotta
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
                state.speedLevel == null -> SpeedLevelSelector(vm::selectSpeed)
                state.word != null -> SpeedQuestion(state, vm::select)
            }
        }
    }
}

// ── Выбор уровня ──────────────────────────────────────────────

@Composable
private fun SpeedLevelSelector(onSelect: (SpeedLevel) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚡", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text("ВЫБЕРИ СКОРОСТЬ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(32.dp))

        SpeedLevel.entries.forEach { level ->
            Surface(
                onClick = { onSelect(level) },
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                border = androidx.compose.foundation.BorderStroke(1.dp, level.color.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(level.emoji, fontSize = 32.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(level.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = level.color)
                        Text(level.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.graphicsLayer { rotationZ = 180f }, tint = level.color.copy(alpha = 0.5f))
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
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Прогресс
        LinearProgressIndicator(
            progress  = { state.totalAnswered / 15f },
            modifier  = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
            color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            trackColor = Color.Transparent
        )
        
        Spacer(Modifier.height(12.dp))

        // Таймер
        val timerColor by animateColorAsState(
            if (state.timeLeft > 0.3f) AppColors.Teal else MaterialTheme.colorScheme.error,
            label = "timer"
        )
        LinearProgressIndicator(
            progress = { state.timeLeft },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = timerColor,
            trackColor = timerColor.copy(alpha = 0.1f)
        )

        Spacer(Modifier.weight(0.15f))

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            border = borderStroke(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "КАК ПЕРЕВОДИТСЯ?",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    word.spanish.uppercase(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.weight(0.2f))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.options.forEachIndexed { i, option ->
                val answered = state.selectedIndex != null
                val isSelected = state.selectedIndex == i
                val isCorrect = state.correctIndex == i
                
                Surface(
                    onClick = { onSelect(i) },
                    enabled = !answered,
                    shape = RoundedCornerShape(18.dp),
                    color = when {
                        !answered -> MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                        isCorrect -> AppColors.Teal.copy(alpha = 0.2f)
                        isSelected -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected || (answered && isCorrect)) 2.dp else 1.dp,
                        color = if (answered && isCorrect) AppColors.Teal
                                else if (isSelected) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            option,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isCorrect && answered) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isCorrect && answered) AppColors.Teal else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(0.3f))
    }
}

@Composable
private fun SpeedResult(state: SpeedState, onRetry: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🚀", fontSize = 72.sp)
        Spacer(Modifier.height(16.dp))
        Text("Финиш!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Surface(shape = RoundedCornerShape(12.dp), color = AppColors.Terracotta.copy(alpha = 0.15f)) {
            Text("⭐ ${state.score} очков",
                 modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                 style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold,
                 color = AppColors.Terracotta)
        }
        
        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Terracotta)
        ) {
            Text("ПОПРОБОВАТЬ СНОВА", fontWeight = FontWeight.ExtraBold)
        }
        
        TextButton(onClick = onBack, modifier = Modifier.padding(top = 12.dp)) {
            Text("ВЫЙТИ В МЕНЮ", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun borderStroke() = androidx.compose.foundation.BorderStroke(
    1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
)
