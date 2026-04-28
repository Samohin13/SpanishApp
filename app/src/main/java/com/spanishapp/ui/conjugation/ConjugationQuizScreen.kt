package com.spanishapp.ui.conjugation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.spanishapp.data.db.dao.ConjugationDao
import com.spanishapp.data.db.entity.ConjugationEntity
import com.spanishapp.service.SpanishTts
import com.spanishapp.ui.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Model ──────────────────────────────────────────────────────

private val PRONOUNS = listOf(
    "yo", "tú", "él/ella", "nosotros", "vosotros", "ellos"
)

private val TENSE_LABELS = mapOf(
    "presente"    to "Presente",
    "preterito"   to "Pretérito Indefinido",
    "imperfecto"  to "Imperfecto",
    "futuro"      to "Futuro Simple",
    "condicional" to "Condicional",
    "subjuntivo"  to "Subjuntivo"
)

data class ConjQuestion(
    val entity: ConjugationEntity,
    val pronounIndex: Int,            // 0-5
    val correct: String,
    val options: List<String>
) {
    val pronoun get() = PRONOUNS[pronounIndex]
    val tenseLabel get() = TENSE_LABELS[entity.tense] ?: entity.tense
}

data class ConjQuizState(
    val questions: List<ConjQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedIndex: Int? = null,
    val score: Int = 0,
    val isFinished: Boolean = false
) {
    val current get() = questions.getOrNull(currentIndex)
    val progress get() = if (questions.isEmpty()) 0f else currentIndex.toFloat() / questions.size
}

// ── ViewModel ──────────────────────────────────────────────────

@HiltViewModel
class ConjugationQuizViewModel @Inject constructor(
    private val dao: ConjugationDao,
    private val tts: SpanishTts
) : ViewModel() {

    private val _state = MutableStateFlow(ConjQuizState())
    val state: StateFlow<ConjQuizState> = _state.asStateFlow()

    init { loadQuiz() }

    fun loadQuiz() = viewModelScope.launch {
        val all = dao.getAll()
        if (all.isEmpty()) return@launch

        val pool = all.shuffled().take(30)

        val questions = pool.flatMap { entity ->
            val forms = listOf(entity.yo, entity.tu, entity.el, entity.nosotros, entity.vosotros, entity.ellos)
            val pronounIndex = (0..5).random()
            val correct = forms[pronounIndex]

            // distractors from other forms of same verb or other verbs
            val distractors = (pool - entity)
                .flatMap { e ->
                    listOf(e.yo, e.tu, e.el, e.nosotros, e.vosotros, e.ellos)
                }
                .filter { it != correct }
                .distinct()
                .shuffled()
                .take(3)

            if (distractors.size < 3) return@flatMap emptyList()

            val opts = (distractors + correct).shuffled()
            listOf(
                ConjQuestion(
                    entity        = entity,
                    pronounIndex  = pronounIndex,
                    correct       = correct,
                    options       = opts
                )
            )
        }.take(12)

        _state.value = ConjQuizState(questions = questions)
    }

    fun select(index: Int) {
        val s = _state.value
        if (s.selectedIndex != null) return
        val isCorrect = s.current?.options?.get(index) == s.current?.correct
        _state.value = s.copy(
            selectedIndex = index,
            score = if (isCorrect) s.score + 1 else s.score
        )
        if (isCorrect) {
            val word = "${s.current?.pronoun} ${s.current?.correct}"
            tts.speak(word)
        }
    }

    fun next() {
        val s = _state.value
        val next = s.currentIndex + 1
        _state.value = if (next >= s.questions.size)
            s.copy(isFinished = true)
        else
            s.copy(currentIndex = next, selectedIndex = null)
    }

    fun restart() = loadQuiz()
}

// ── Screen ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConjugationQuizScreen(
    navController: NavHostController,
    vm: ConjugationQuizViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verbos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.questions.isEmpty() -> ConjLoadingState()
                state.isFinished -> ConjResultState(
                    score   = state.score,
                    total   = state.questions.size,
                    onRetry = vm::restart,
                    onBack  = { navController.popBackStack() }
                )
                else -> ConjQuestionState(
                    state    = state,
                    onSelect = vm::select,
                    onNext   = vm::next
                )
            }
        }
    }
}

// ── Question ───────────────────────────────────────────────────

@Composable
private fun ConjQuestionState(
    state: ConjQuizState,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit
) {
    val q = state.current ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Progress bar
        LinearProgressIndicator(
            progress = { state.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color      = AppColors.Teal,
            trackColor = AppColors.Teal.copy(alpha = 0.15f)
        )
        Text(
            "${state.currentIndex + 1} / ${state.questions.size}  ·  ${state.score} верно",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.weight(0.2f))

        // Question card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tense badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AppColors.Teal.copy(alpha = 0.15f)
                ) {
                    Text(
                        q.tenseLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.Teal,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    q.entity.verb,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AppColors.Terracotta.copy(alpha = 0.12f)
                    ) {
                        Text(
                            q.pronoun,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = AppColors.Terracotta,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        "→ ?",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (q.entity.isIrregular) {
                    Text(
                        "⚡ Неправильный глагол",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.Gold
                    )
                }
            }
        }

        Spacer(Modifier.weight(0.2f))

        // Answer options
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            q.options.forEachIndexed { i, option ->
                val answered   = state.selectedIndex != null
                val isSelected = state.selectedIndex == i
                val isCorrect  = option == q.correct

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
                        brush = SolidColor(borderColor)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text(
                        option,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isCorrect && answered) FontWeight.Bold else FontWeight.Normal,
                        color      = if (isCorrect && answered) AppColors.Teal
                                     else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(Modifier.weight(0.3f))

        AnimatedVisibility(state.selectedIndex != null) {
            Button(
                onClick  = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = AppColors.Teal)
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

// ── Result ─────────────────────────────────────────────────────

@Composable
private fun ConjResultState(
    score: Int,
    total: Int,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    val pct = if (total > 0) score * 100 / total else 0
    val emoji = when {
        pct >= 90 -> "🏆"
        pct >= 70 -> "🎉"
        pct >= 50 -> "👍"
        else      -> "💪"
    }
    val message = when {
        pct >= 90 -> "Отлично! Спряжения освоены!"
        pct >= 70 -> "Хороший результат!"
        pct >= 50 -> "Неплохо, но повтори ещё раз"
        else      -> "Продолжай тренироваться!"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 72.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            "$score / $total",
            style      = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            message,
            style     = MaterialTheme.typography.titleMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress   = { pct / 100f },
            modifier   = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .padding(horizontal = 32.dp),
            color      = AppColors.Teal,
            trackColor = AppColors.Teal.copy(alpha = 0.15f)
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick  = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape    = RoundedCornerShape(14.dp)
        ) {
            Text("Ещё раз", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick  = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape    = RoundedCornerShape(14.dp)
        ) {
            Text("Назад к спряжениям")
        }
    }
}

@Composable
private fun ConjLoadingState() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = AppColors.Teal)
        Spacer(Modifier.height(12.dp))
        Text("Загружаем глаголы…", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
