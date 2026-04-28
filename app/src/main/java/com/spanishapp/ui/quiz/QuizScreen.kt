package com.spanishapp.ui.quiz

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
    val options: List<String>,
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
        val pool = wordDao.getRandomWords(40)
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
        if (s.selectedIndex != null) return
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
        if (nextIndex >= s.questions.size) {
            _state.value = s.copy(isFinished = true)
        } else {
            _state.value = s.copy(currentIndex = nextIndex, selectedIndex = null)
        }
    }

    fun restart() = loadQuiz()
}

// ── Screen ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    navController: NavHostController,
    vm: QuizViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Тест", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
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
                state.questions.isEmpty() -> CircularProgressIndicator(color = Color(0xFF2196F3))
                state.isFinished         -> QuizResult(
                    score   = state.score,
                    total   = state.questions.size,
                    onRetry = vm::restart,
                    onBack  = { navController.popBackStack() }
                )
                else -> QuizQuestionContent(
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
private fun QuizQuestionContent(
    state: QuizState,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit
) {
    val q = state.current ?: return

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Прогресс
        LinearProgressIndicator(
            progress = { state.progress },
            modifier  = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color     = Color(0xFF2196F3),
            trackColor = Color(0xFF2196F3).copy(alpha = 0.1f)
        )
        Spacer(Modifier.height(24.dp))

        // Основная карточка
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            border = borderStroke(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "КАК ПЕРЕВОДИТСЯ?",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    letterSpacing = 1.2.sp
                )

                Text(
                    q.word.spanish.uppercase(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                
                if (q.word.example.isNotBlank()) {
                    Text(
                        "“${q.word.example}”",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Варианты ответов
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            q.options.forEachIndexed { i, option ->
                val answered = state.selectedIndex != null
                val isSelected = state.selectedIndex == i
                val isCorrect  = q.correctIndex == i

                QuizOptionItem(
                    label = option,
                    answered = answered,
                    isCorrect = isCorrect,
                    isSelected = isSelected,
                    onClick = { onSelect(i) }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Кнопка Далее
        AnimatedVisibility(
            visible = state.selectedIndex != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 0.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text(
                    if (state.currentIndex + 1 >= state.questions.size) "РЕЗУЛЬТАТЫ"
                    else "СЛЕДУЮЩИЙ ВОПРОС",
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        
        if (state.selectedIndex == null) {
            Spacer(Modifier.height(56.dp)) // Reserve space
        } else {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuizOptionItem(
    label: String,
    answered: Boolean,
    isCorrect: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val targetScale = if (isSelected) 1.05f else 1f
    val scale by animateFloatAsState(targetScale, label = "scale")

    val bgColor = when {
        !answered -> MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
        isCorrect -> AppColors.Teal.copy(alpha = 0.2f)
        isSelected -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
    }

    Surface(
        onClick = onClick,
        enabled = !answered,
        modifier = Modifier.fillMaxWidth().height(60.dp).graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected || (answered && isCorrect)) 2.dp else 1.dp,
            color = if (answered && isCorrect) AppColors.Teal 
                    else if (isSelected) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isCorrect && answered) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isCorrect && answered) AppColors.Teal else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun QuizResult(score: Int, total: Int, onRetry: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🏁", fontSize = 72.sp)
        Spacer(Modifier.height(16.dp))
        Text("Тест завершен", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF2196F3).copy(alpha = 0.15f)) {
            Text("$score из $total верных",
                 modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                 style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold,
                 color = Color(0xFF2196F3))
        }
        
        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
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
