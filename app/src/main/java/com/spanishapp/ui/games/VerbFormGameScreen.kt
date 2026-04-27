package com.spanishapp.ui.games

import androidx.compose.animation.*
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.spanishapp.data.db.dao.ConjugationDao
import com.spanishapp.data.db.entity.ConjugationEntity
import com.spanishapp.ui.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Вопрос ────────────────────────────────────────────────────

data class VerbQuestion(
    val sentence: String,          // полное предложение с правильным глаголом (для объяснения)
    val sentenceWithBlank: String, // предложение с "___"
    val hint: String,              // подсказка: infinitiv + лицо
    val correct: String,           // правильная форма
    val options: List<String>,     // 4 варианта
    val tenseRu: String            // название времени по-русски
)

// ── Шаблоны предложений ───────────────────────────────────────

private data class SentenceTemplate(
    val template: String,   // "Yo ___ en Madrid." — ___ заменяется формой
    val verb: String,
    val tense: String,
    val person: String,     // yo, tú, él, nosotros, vosotros, ellos
    val hintRu: String,     // "быть (я, настоящее)"
    val tenseRu: String
)

private val TEMPLATES = listOf(
    SentenceTemplate("Yo ___ estudiante.", "ser", "presente", "yo", "ser (yo, настоящее)", "Настоящее"),
    SentenceTemplate("Ella ___ muy inteligente.", "ser", "presente", "él", "ser (ella, настоящее)", "Настоящее"),
    SentenceTemplate("Nosotros ___ amigos.", "ser", "presente", "nosotros", "ser (nosotros, настоящее)", "Настоящее"),
    SentenceTemplate("Yo ___ en casa.", "estar", "presente", "yo", "estar (yo, настоящее)", "Настоящее"),
    SentenceTemplate("¿Tú ___ bien?", "estar", "presente", "tú", "estar (tú, настоящее)", "Настоящее"),
    SentenceTemplate("Ellos ___ cansados.", "estar", "presente", "ellos", "estar (ellos, настоящее)", "Настоящее"),
    SentenceTemplate("Yo ___ un coche.", "tener", "presente", "yo", "tener (yo, настоящее)", "Настоящее"),
    SentenceTemplate("Ella ___ veinte años.", "tener", "presente", "él", "tener (ella, настоящее)", "Настоящее"),
    SentenceTemplate("Nosotros ___ hambre.", "tener", "presente", "nosotros", "tener (nosotros, настоящее)", "Настоящее"),
    SentenceTemplate("Yo ___ al supermercado.", "ir", "presente", "yo", "ir (yo, настоящее)", "Настоящее"),
    SentenceTemplate("¿Tú ___ a la fiesta?", "ir", "presente", "tú", "ir (tú, настоящее)", "Настоящее"),
    SentenceTemplate("Ellos ___ a Madrid mañana.", "ir", "futuro", "ellos", "ir (ellos, будущее)", "Будущее"),
    SentenceTemplate("Yo ___ hablar español.", "poder", "presente", "yo", "poder (yo, настоящее)", "Настоящее"),
    SentenceTemplate("¿Tú ___ ayudarme?", "poder", "presente", "tú", "poder (tú, настоящее)", "Настоящее"),
    SentenceTemplate("Yo ___ estudiar más.", "querer", "presente", "yo", "querer (yo, настоящее)", "Настоящее"),
    SentenceTemplate("Ella ___ un café.", "querer", "presente", "él", "querer (ella, настоящее)", "Настоящее"),
    SentenceTemplate("Ayer yo ___ al trabajo.", "ir", "preterito", "yo", "ir (я, прошедшее)", "Прошедшее"),
    SentenceTemplate("El año pasado ella ___ profesora.", "ser", "preterito", "él", "ser (она, прошедшее)", "Прошедшее"),
    SentenceTemplate("Antes nosotros ___ en París.", "vivir", "imperfecto", "nosotros", "vivir (nosotros, прошедшее незав.)", "Прошедшее незав."),
    SentenceTemplate("Cuando era niño, ___ mucho.", "jugar", "imperfecto", "él", "jugar (él, прошедшее незав.)", "Прошедшее незав."),
    SentenceTemplate("Mañana yo ___ temprano.", "levantarse", "futuro", "yo", "levantarse (yo, будущее)", "Будущее"),
    SentenceTemplate("Si tuviera dinero, ___ un viaje.", "hacer", "condicional", "yo", "hacer (yo, условное)", "Условное"),
    SentenceTemplate("Espero que tú ___ bien.", "estar", "subjuntivo", "tú", "estar (tú, сослагат.)", "Сослагательное"),
    SentenceTemplate("Quiero que ella ___ la verdad.", "decir", "subjuntivo", "él", "decir (ella, сослагат.)", "Сослагательное"),
    SentenceTemplate("Yo ___ la tarea ayer.", "hacer", "preterito", "yo", "hacer (yo, прошедшее)", "Прошедшее"),
)

// ── Утилита: извлечь форму по лицу ───────────────────────────

private fun ConjugationEntity.formByPerson(person: String): String = when (person) {
    "yo"       -> yo
    "tú"       -> tu
    "él"       -> el
    "nosotros" -> nosotros
    "vosotros" -> vosotros
    "ellos"    -> ellos
    else       -> yo
}

// ── State & ViewModel ─────────────────────────────────────────

data class VerbFormState(
    val question: VerbQuestion? = null,
    val selectedOption: String? = null,
    val isCorrect: Boolean? = null,
    val score: Int = 0,
    val streak: Int = 0,
    val totalAnswered: Int = 0,
    val isFinished: Boolean = false,
    val isLoading: Boolean = true,
    val totalQuestions: Int = 15
)

@HiltViewModel
class VerbFormViewModel @Inject constructor(
    private val conjugationDao: ConjugationDao
) : ViewModel() {

    private val _state = MutableStateFlow(VerbFormState())
    val state: StateFlow<VerbFormState> = _state.asStateFlow()

    private var questions: List<VerbQuestion> = emptyList()
    private var qIndex = 0

    init { loadQuestions() }

    private fun loadQuestions() = viewModelScope.launch {
        val conjugations = conjugationDao.getAll()
        // Индекс: verb+tense → ConjugationEntity
        val conjMap = conjugations.associateBy { "${it.verb}_${it.tense}" }

        val built = TEMPLATES.mapNotNull { tmpl ->
            val conj = conjMap["${tmpl.verb}_${tmpl.tense}"] ?: return@mapNotNull null
            val correct = conj.formByPerson(tmpl.person)

            // Диstractors — другие формы того же глагола или других глаголов
            val distractors = conjugations
                .filter { it.verb == tmpl.verb }
                .flatMap { c ->
                    listOf(c.yo, c.tu, c.el, c.nosotros, c.ellos)
                }
                .filter { it != correct && it.isNotBlank() }
                .distinct()
                .shuffled()
                .take(3)

            if (distractors.size < 3) return@mapNotNull null

            val options = (distractors + correct).shuffled()

            VerbQuestion(
                sentence          = tmpl.template.replace("___", correct),
                sentenceWithBlank = tmpl.template,
                hint              = tmpl.hintRu,
                correct           = correct,
                options           = options,
                tenseRu           = tmpl.tenseRu
            )
        }.shuffled().take(15)

        questions = built
        qIndex = 0

        if (questions.isEmpty()) {
            _state.value = _state.value.copy(isLoading = false, isFinished = true)
        } else {
            showNext()
        }
    }

    private fun showNext() {
        if (qIndex >= questions.size) {
            _state.value = _state.value.copy(isFinished = true)
            return
        }
        _state.value = _state.value.copy(
            question       = questions[qIndex],
            selectedOption = null,
            isCorrect      = null,
            isLoading      = false
        )
    }

    fun select(option: String) = viewModelScope.launch {
        val s = _state.value
        if (s.selectedOption != null || s.question == null) return@launch
        val correct = option == s.question.correct
        _state.value = s.copy(
            selectedOption = option,
            isCorrect      = correct,
            score          = if (correct) s.score + 10 else s.score,
            streak         = if (correct) s.streak + 1 else 0,
            totalAnswered  = s.totalAnswered + 1
        )
        delay(1200)
        qIndex++
        showNext()
    }

    fun restart() {
        qIndex = 0
        questions = questions.shuffled()
        _state.value = VerbFormState()
        loadQuestions()
    }
}

// ── Screen ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerbFormGameScreen(
    navController: NavHostController,
    vm: VerbFormViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Правильная форма") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    if (!state.isFinished) {
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
                state.isLoading  -> CircularProgressIndicator(color = AppColors.Teal)
                state.isFinished -> VerbFormResult(state, vm::restart) { navController.popBackStack() }
                state.question != null -> VerbFormQuestion(state, vm::select)
            }
        }
    }
}

// ── Вопрос ────────────────────────────────────────────────────

@Composable
private fun VerbFormQuestion(state: VerbFormState, onSelect: (String) -> Unit) {
    val q = state.question ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Прогресс
        LinearProgressIndicator(
            progress  = { state.totalAnswered / state.totalQuestions.toFloat() },
            modifier  = Modifier.fillMaxWidth().height(6.dp),
            color     = AppColors.Teal,
            trackColor = AppColors.Teal.copy(alpha = 0.15f)
        )
        Text(
            "${state.totalAnswered + 1} / ${state.totalQuestions}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.weight(0.15f))

        // Метка времени
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = AppColors.Teal.copy(alpha = 0.12f)
        ) {
            Text(
                q.tenseRu,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Teal,
                fontWeight = FontWeight.Bold
            )
        }

        // Карточка с предложением
        AnimatedContent(targetState = q.sentenceWithBlank, label = "vf_sentence") { sentence ->
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = AppColors.Teal.copy(alpha = 0.07f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Предложение с выделенным пропуском
                    val parts = sentence.split("___")
                    Text(
                        buildAnnotatedString {
                            if (parts.isNotEmpty()) append(parts[0])
                            withStyle(SpanStyle(
                                color = AppColors.Teal,
                                fontWeight = FontWeight.ExtraBold
                            )) {
                                // После ответа показываем правильный глагол
                                if (state.selectedOption != null) {
                                    append(q.correct)
                                } else {
                                    append("___")
                                }
                            }
                            if (parts.size > 1) append(parts[1])
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    // Подсказка
                    Text(
                        "💡 ${q.hint}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.weight(0.2f))

        // Варианты ответов
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            q.options.forEach { option ->
                val answered  = state.selectedOption != null
                val isSelected = state.selectedOption == option
                val isCorrect  = q.correct == option

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
                    onClick  = { onSelect(option) },
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
                        style      = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isCorrect && answered) FontWeight.Bold else FontWeight.Normal,
                        color      = when {
                            isCorrect && answered -> AppColors.Teal
                            isSelected && answered -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }

        Spacer(Modifier.weight(0.1f))
    }
}

// ── Результат ─────────────────────────────────────────────────

@Composable
private fun VerbFormResult(
    state: VerbFormState,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    val correct = state.score / 10
    val total   = state.totalAnswered
    val pct     = if (total > 0) correct * 100 / total else 0

    val emoji   = when { pct >= 90 -> "🏆"; pct >= 70 -> "🎉"; pct >= 50 -> "👍"; else -> "💪" }
    val message = when { pct >= 90 -> "Спрягаешь как носитель!"; pct >= 70 -> "Отлично!"; pct >= 50 -> "Неплохо!"; else -> "Ещё немного практики!" }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 72.sp)
        Spacer(Modifier.height(16.dp))
        Text("$correct / $total", style = MaterialTheme.typography.displayMedium,
             fontWeight = FontWeight.Bold)
        Text("правильных ответов", style = MaterialTheme.typography.bodyMedium,
             color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.titleMedium,
             color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
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
