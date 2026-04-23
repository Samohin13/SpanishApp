package com.spanishapp.ui.conjugation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── ViewModel ─────────────────────────────────────────────────

@HiltViewModel
class ConjugationViewModel @Inject constructor(
    private val dao: ConjugationDao,
    private val tts: SpanishTts
) : ViewModel() {

    val verbs: StateFlow<List<String>> = dao.getAllVerbs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedVerb = MutableStateFlow<String?>(null)
    val selectedVerb: StateFlow<String?> = _selectedVerb.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val conjugations: StateFlow<List<ConjugationEntity>> = _selectedVerb
        .flatMapLatest { verb ->
            if (verb != null) dao.getForVerb(verb) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectVerb(verb: String) {
        _selectedVerb.value = if (_selectedVerb.value == verb) null else verb
    }

    fun speak(text: String) = viewModelScope.launch { tts.speak(text) }
}

// ── Screen ────────────────────────────────────────────────────

private val TENSE_NAMES = mapOf(
    "presente"   to "Настоящее",
    "preterito"  to "Прошедшее (индефинидо)",
    "imperfecto" to "Прошедшее (имперфект)",
    "futuro"     to "Будущее",
    "condicional" to "Условное",
    "subjuntivo" to "Сослагательное"
)

private val TENSE_ORDER = listOf(
    "presente","preterito","imperfecto","futuro","condicional","subjuntivo"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConjugationScreen(
    navController: NavHostController,
    vm: ConjugationViewModel = hiltViewModel()
) {
    val verbs        by vm.verbs.collectAsState()
    val selectedVerb by vm.selectedVerb.collectAsState()
    val conjugations by vm.conjugations.collectAsState()

    // Group by tense, sorted by TENSE_ORDER
    val byTense = remember(conjugations) {
        conjugations.sortedBy { TENSE_ORDER.indexOf(it.tense) }
            .groupBy { it.tense }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Спряжения") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Hint ─────────────────────────────────────────
            item {
                Text(
                    "Нажми на глагол чтобы раскрыть таблицу",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // ── Verb list ─────────────────────────────────────
            items(verbs, key = { it }) { verb ->
                val isSelected = verb == selectedVerb
                VerbCard(
                    verb       = verb,
                    isSelected = isSelected,
                    byTense    = if (isSelected) byTense else emptyMap(),
                    onToggle   = { vm.selectVerb(verb) },
                    onSpeak    = { vm.speak(it) }
                )
            }
        }
    }
}

// ── Verb card (collapsible) ────────────────────────────────────

@Composable
private fun VerbCard(
    verb: String,
    isSelected: Boolean,
    byTense: Map<String, List<ConjugationEntity>>,
    onToggle: () -> Unit,
    onSpeak: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = if (isSelected) 4.dp else 1.dp,
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(onClick = onToggle)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        verb,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (isSelected && byTense.values.flatten().firstOrNull()?.isIrregular == true) {
                        Text(
                            "⚡ Неправильный глагол",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.Gold
                        )
                    }
                }
                IconButton(onClick = { onSpeak(verb) }) {
                    Icon(Icons.Default.VolumeUp, null,
                         tint = AppColors.Terracotta, modifier = Modifier.size(20.dp))
                }
                Icon(
                    if (isSelected) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Conjugation tables
            if (isSelected && byTense.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                byTense.forEach { (tense, rows) ->
                    val entity = rows.first()
                    TenseTable(
                        tenseName = TENSE_NAMES[tense] ?: tense,
                        entity    = entity,
                        onSpeak   = onSpeak
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

// ── Tense table ───────────────────────────────────────────────

@Composable
private fun TenseTable(
    tenseName: String,
    entity: ConjugationEntity,
    onSpeak: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                tenseName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.Teal
            )
            if (entity.isIrregular) {
                Text("⚡", fontSize = 12.sp)
            }
        }

        val forms = listOf(
            "yo"        to entity.yo,
            "tú"        to entity.tu,
            "él/ella"   to entity.el,
            "nosotros"  to entity.nosotros,
            "vosotros"  to entity.vosotros,
            "ellos"     to entity.ellos
        )

        forms.forEachIndexed { i, (pronoun, form) ->
            if (i > 0) HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSpeak("$pronoun $form") }
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    pronoun,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(72.dp)
                )
                Text(
                    form,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    Icons.Default.VolumeUp, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        if (entity.note.isNotBlank()) {
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Text(
                "📝 ${entity.note}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
