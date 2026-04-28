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
    val sentence: String,
    val sentenceWithBlank: String,
    val hint: String,
    val correct: String,
    val options: List<String>,
    val tenseRu: String
)

private data class SentenceTemplate(
    val template: String,
    val verb: String,
    val tense: String,
    val person: String,
    val hintRu: String,
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
    SentenceTemplate("¿Tú ___ ayudarме?", "poder", "presente", "tú", "pодer (tú, настоящее)", "Настоящее"),
    SentenceTemplate("Yo ___ estudiar más.", "querer", "presente", "yo", "querer (yo, настоящее)", "Настоящее"),
    SentenceTemplate("Ella ___ un café.", "querer", "presente", "él", "querer (ella, настоящее)", "Настоящее"),
    SentenceTemplate("Ayer yo ___ al trabajo.", "ir", "preterito", "yo", "ir (я, прошедшее)", "Прошедшее"),
    SentenceTemplate("El año pasado ella ___ profesora.", "ser", "preterito", "él", "ser (она, прошедшее)", "Прошедшее"),
    SentenceTemplate("Antes nosotros ___ en París.", "vivir", "imperfecto", "nosotros", "vivir (nosotros, прошедшее незав.)", "Прошедшее незав."),
    SentenceTemplate("Когда era niño, ___ mucho.", "jugar", "imperfecto", "él", "jugar (él, прошедшее незав.)", "Прошедшее незав."),
    SentenceTemplate("Mañana yo ___ temprano.", "levantarse", "futuro", "yo", "levantarse (yo, будущее)", "Будущее"),
    SentenceTemplate("Si tuviera dinero, ___ un viaje.", "hacer", "condicional", "yo", "hacer (yo, условное)", "Условное"),
    SentenceTemplate("Espero que tú ___ bien.", "estar", "subjuntivo", "tú", "estar (tú, сослагат.)", "Сослагательное"),
    SentenceTemplate("Quiero que ella ___ la verdad.", "decir", "subjuntivo", "él", "decir (ella, сослагат.)", "Сослагательное"),
    SentenceTemplate("Yo ___ la tarea ayer.", "hacer", "preterito", "yo", "hacer (yo, прошедшее)", "Прошедшее"),
)

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
        val conjMap = conjugations.associateBy { "${it.verb}_${it.tense}" }

        questions = TEMPLATES.mapNotNull { tmpl ->
            val conj = conjMap["${tmpl.verb}_${tmpl.tense}"] ?: return@mapNotNull null
            val correct = conj.formByPerson(tmpl.person)
            val distractors = conjugations.filter { it.verb == tmpl.verb }
                .flatMap { listOf(it.yo, it.tu, it.el, it.nosotros, it.ellos) }
                .filter { it != correct && it.isNotBlank() }.distinct().shuffled().take(3)
            if (distractors.size < 3) return@mapNotNull null
            VerbQuestion(
                sentence = tmpl.template.replace("___", correct),
                sentenceWithBlank = tmpl.template,
                hint = tmpl.hintRu,
                correct = correct,
                options = (distractors + correct).shuffled(),
                tenseRu = tmpl.tenseRu
            )
        }.shuffled().take(15)

        qIndex = 0
        showNext()
    }

    private fun showNext() {
        if (qIndex >= questions.size) {
            _state.value = _state.value.copy(isFinished = true)
            return
        }
        _state.value = _state.value.copy(
            question = questions[qIndex],
            selectedOption = null,
            isCorrect = null,
            isLoading = false
        )
    }

    fun select(option: String) = viewModelScope.launch {
        if (_state.value.selectedOption != null) return@launch
        val correct = option == _state.value.question?.correct
        _state.value = _state.value.copy(
            selectedOption = option,
            isCorrect = correct,
            score = if (correct) _state.value.score + 10 else _state.value.score,
            streak = if (correct) _state.value.streak + 1 else 0,
            totalAnswered = _state.value.totalAnswered + 1
        )
        delay(1000)
        qIndex++
        showNext()
    }

    fun restart() {
        qIndex = 0
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
                title = { Text("Глаголы", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    if (!state.isFinished) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AppColors.Teal.copy(alpha = 0.15f),
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Text(
                                "⭐ ${state.score}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppColors.Teal
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
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Прогресс
        LinearProgressIndicator(
            progress  = { state.totalAnswered / 15f },
            modifier  = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color     = AppColors.Teal,
            trackColor = AppColors.Teal.copy(alpha = 0.1f)
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
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Метка времени
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AppColors.Teal.copy(alpha = 0.1f)
                ) {
                    Text(
                        q.tenseRu.uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.Teal,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Предложение
                val parts = q.sentenceWithBlank.split("___")
                Text(
                    buildAnnotatedString {
                        if (parts.isNotEmpty()) append(parts[0])
                        withStyle(SpanStyle(color = AppColors.Teal, fontWeight = FontWeight.ExtraBold)) {
                            if (state.selectedOption != null) append(q.correct) else append("___")
                        }
                        if (parts.size > 1) append(parts[1])
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    "💡 ${q.hint}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Варианты ответов
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            q.options.forEach { option ->
                val answered  = state.selectedOption != null
                val isSelected = state.selectedOption == option
                val isCorrect  = q.correct == option

                VerbOptionItem(
                    label = option,
                    answered = answered,
                    isCorrect = isCorrect,
                    isSelected = isSelected,
                    onClick = { onSelect(option) }
                )
            }
        }

        Spacer(Modifier.weight(0.5f))
    }
}

@Composable
private fun VerbOptionItem(
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
private fun VerbFormResult(state: VerbFormState, onRetry: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎓", fontSize = 72.sp)
        Spacer(Modifier.height(16.dp))
        Text("Урок завершен", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Surface(shape = RoundedCornerShape(12.dp), color = AppColors.Teal.copy(alpha = 0.15f)) {
            Text("⭐ ${state.score} очков",
                 modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                 style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold,
                 color = AppColors.Teal)
        }
        
        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Teal)
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
