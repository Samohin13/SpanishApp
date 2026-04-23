package com.spanishapp.ui.quiz

import androidx.compose.animation.*
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
import com.spanishapp.service.SpanishTts
import com.spanishapp.ui.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Model ─────────────────────────────────────────────────────

data class QuizQuestion(
    val word: WordEntity,
    val options: List<String>,   // 4 options in Russian
    val correctIndex: Int
)

data class QuizState(
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedIndex: Int? = null,
    val score: Int = 0,
    val isFinished: Boolean = false
) {
    val current get() = questions.getOrNull(currentIndex)
    val progress get() = if (questions.isEmpty()) 0f else currentIndex.toFloat() / questions.size
}

// ── ViewModel ─────────────────────────────────────────────────

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val wordDao: WordDao,
    private val tts: SpanishTts
) : ViewModel() {

    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state.asStateFlow()

    init { loadQuiz() }

    fun loadQuiz() = viewModelScope.launch {
        val pool = wordDao.getQuizWords(40)
        if (pool.size < 4) return@launch

        val questions = pool.take(10).map { word ->
            val distractors = pool
                .filter { it.id != word.id }
                .shuffled()
                .take(3)
                .map { it.russian }
            val allOptions = (distractors + word.russian).shuffled()
            QuizQuestion(
                word         = word,
                options      = allOptions,
                correctIndex = allOptions.indexOf(word.russian)
            )
        }
        _state.value = QuizState(questions = questions)
    }

    fun select(index: Int) {
        val s = _state.value
        if (s.selectedIndex != null) return   // already answered
        val correct = s.current?.correctIndex == index
        _state.value = s.copy(
            selectedIndex = index,
            score = if (correct) s.score + 1 else s.score
        )
        if (correct) tts.speak(s.current?.word?.spanish ?: "")
    }

    fun next() {
        val s = _state.value
        val nextIndex = s.currentIndex + 1
        _state.value = if (nextIndex >= s.questions.size)
            s.copy(isFinished = true)
        else
            s.copy(currentIndex = nextIndex, selectedIndex = null)
    }

    fun restart() = loadQuiz()
}

// Add quiz helper to DAO via extension call
suspend fun WordDao.getQuizWords(limit: Int): List<WordEntity> =
    getRandomWords(limit)

// ── Screen ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    navController: NavHostController,
    vm: QuizViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Тест") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
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
                state.questions.isEmpty() -> LoadingState()
                state.isFinished         -> ResultState(
                    score   = state.score,
                    total   = state.questions.size,
                    onRetry = vm::restart,
                    onBack  = { navController.popBackStack() }
                )
                else -> QuestionState(
                    state    = state,
                    onSelect = vm::select,
                    onNext   = vm::next
                )
            }
        }
    }
}

// ── Question ──────────────────────────────────────────────────

@Composable
private fun QuestionState(
    state: QuizState,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit
) {
    val q = state.current ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Progress
        LinearProgressIndicator(
            progress = { state.progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).padding(bottom = 4.dp),
            color    = AppColors.Terracotta,
            trackColor = AppColors.Terracotta.copy(alpha = 0.15f)
        )
        Text(
            "${state.currentIndex + 1} / ${state.questions.size}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.weight(0.3f))

        // Question word
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Переведи слово:", style = MaterialTheme.typography.bodyMedium,
                     color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Text(q.word.spanish, style = MaterialTheme.typography.displaySmall,
                     fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                if (q.word.example.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text("«${q.word.example}»",
                         style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant,
                         textAlign = TextAlign.Center)
                }
            }
        }

        Spacer(Modifier.weight(0.3f))

        // Options
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            q.options.forEachIndexed { i, option ->
                val answered = state.selectedIndex != null
                val isSelected = state.selectedIndex == i
                val isCorrect  = q.correctIndex == i

                val containerColor = when {
                    !answered           -> MaterialTheme.colorScheme.surface
                    isCorrect           -> AppColors.Teal.copy(alpha = 0.2f)
                    isSelected          -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    else                -> MaterialTheme.colorScheme.surface
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
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(
                        option,
                        style    = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isCorrect && answered) FontWeight.Bold else FontWeight.Normal,
                        color    = if (isCorrect && answered) AppColors.Teal
                                   else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(Modifier.weight(0.4f))

        // Next button
        AnimatedVisibility(state.selectedIndex != null) {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    if (state.currentIndex + 1 >= state.questions.size) "Результаты"
                    else "Следующий →",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

// ── Result ────────────────────────────────────────────────────

@Composable
private fun ResultState(score: Int, total: Int, onRetry: () -> Unit, onBack: () -> Unit) {
    val pct = score * 100 / total
    val emoji = when {
        pct >= 90 -> "🏆"
        pct >= 70 -> "🎉"
        pct >= 50 -> "👍"
        else      -> "💪"
    }
    val message = when {
        pct >= 90 -> "Отлично! Ты молодец!"
        pct >= 70 -> "Хороший результат!"
        pct >= 50 -> "Неплохо, но есть куда расти"
        else      -> "Продолжай тренироваться!"
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 72.sp)
        Spacer(Modifier.height(16.dp))
        Text("$score / $total", style = MaterialTheme.typography.displayMedium,
             fontWeight = FontWeight.Bold)
        Text(message, style = MaterialTheme.typography.titleMedium,
             color = MaterialTheme.colorScheme.onSurfaceVariant,
             textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().height(52.dp),
               shape = RoundedCornerShape(14.dp)) {
            Text("Ещё раз", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(52.dp),
                       shape = RoundedCornerShape(14.dp)) {
            Text("На главную")
        }
    }
}

@Composable
private fun LoadingState() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = AppColors.Terracotta)
        Spacer(Modifier.height(12.dp))
        Text("Загружаем вопросы…", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
