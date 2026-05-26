package com.spanishapp.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.spanishapp.R
import com.spanishapp.ui.components.tappableForSpeak
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
private fun verbModeTitle(mode: VerbTrainingMode): String = when (mode) {
    VerbTrainingMode.CONJUGAR -> stringResource(R.string.verb_mode_conjugar_title)
    VerbTrainingMode.INVERSO  -> stringResource(R.string.verb_mode_inverso_title)
    VerbTrainingMode.HUECO    -> stringResource(R.string.verb_mode_hueco_title)
    VerbTrainingMode.AUDITIVO -> stringResource(R.string.verb_mode_auditivo_title)
}

@Composable
private fun verbModeDesc(mode: VerbTrainingMode): String = when (mode) {
    VerbTrainingMode.CONJUGAR -> stringResource(R.string.verb_mode_conjugar_desc)
    VerbTrainingMode.INVERSO  -> stringResource(R.string.verb_mode_inverso_desc)
    VerbTrainingMode.HUECO    -> stringResource(R.string.verb_mode_hueco_desc)
    VerbTrainingMode.AUDITIVO -> stringResource(R.string.verb_mode_auditivo_desc)
}

private val ACCENT      = Color(0xFF2196F3)
private val ACCENT_DARK = Color(0xFF1565C0)
private val BgGray
    @Composable get() = MaterialTheme.colorScheme.background
private val TextMain
    @Composable get() = MaterialTheme.colorScheme.onSurface
private val TextGray
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val CardBorder
    @Composable get() = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
private val CardSurface
    @Composable get() = MaterialTheme.colorScheme.surfaceContainerHighest
private val Green       = Color(0xFF4CAF50)
private val Red         = Color(0xFFF44336)
private val Orange      = Color(0xFFFF9500)

private val ACCENT_KEYS = listOf("á", "é", "í", "ó", "ú", "ñ", "ü")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerbTrainingScreen(
    navController: NavHostController,
    viewModel: VerbViewModel = hiltViewModel()
) {
    com.spanishapp.service.TrackActivity(com.spanishapp.service.ActivityType.GAME)
    val state by viewModel.state.collectAsStateWithLifecycle()

    // v1.23.6: VerbTraining целиком PRO-feature (по продуктовому решению).
    // 1327 глаголов + 159 с таблицами 6 времён — это не A1-контент.
    // Free-юзер при попытке зайти сразу редиректится на paywall.
    val isPro by com.spanishapp.ui.games.common.rememberIsProState()
    LaunchedEffect(isPro) {
        if (!isPro) {
            navController.navigate("paywall") {
                popUpTo("games") { inclusive = false }
                launchSingleTop = true
            }
        }
    }
    if (!isPro) return  // не рендерим контент пока редиректим

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.verb_title), fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.showSetup || state.isGameOver) navController.popBackStack()
                        else viewModel.openSetup()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    if (!state.showSetup && !state.isGameOver) {
                        IconButton(onClick = { viewModel.openSetup() }) {
                            Icon(Icons.Default.Settings, null)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).background(BgGray)) {
            when {
                state.showSetup  -> SetupContent(state, viewModel)
                state.isGameOver -> ResultsContent(state, viewModel) { navController.popBackStack() }
                else             -> TrainingContent(state, viewModel)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  НАСТРОЙКИ
// ══════════════════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SetupContent(state: VerbTrainingState, vm: VerbViewModel) {
    val cfg = state.config
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(stringResource(R.string.verb_settings), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextMain)

        SectionCard(title = stringResource(R.string.verb_section_mode)) {
            VerbTrainingMode.entries.forEach { mode ->
                val sel = cfg.mode == mode
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { vm.updateConfig(cfg.copy(mode = mode)) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (sel) ACCENT.copy(alpha = 0.10f) else CardSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        if (sel) 2.dp else 1.dp,
                        if (sel) ACCENT else CardBorder
                    )
                ) {
                    Row(modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = sel, onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = ACCENT))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(verbModeTitle(mode), fontWeight = FontWeight.SemiBold, color = TextMain)
                            Text(verbModeDesc(mode), fontSize = 12.sp, color = TextGray)
                        }
                    }
                }
            }
        }

        SectionCard(title = stringResource(R.string.verb_section_tenses)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                vm.availableTenses().forEach { t ->
                    val sel = t in cfg.selectedTenses
                    FilterChip(
                        selected = sel,
                        onClick = {
                            val newSet = if (sel) cfg.selectedTenses - t else cfg.selectedTenses + t
                            if (newSet.isNotEmpty()) vm.updateConfig(cfg.copy(selectedTenses = newSet))
                        },
                        label = { Text(prettyTense(t)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ACCENT,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        SectionCard(title = stringResource(R.string.verb_section_level)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val tierOptions = listOf(
                    1 to stringResource(R.string.verb_level_1),
                    2 to stringResource(R.string.verb_level_2),
                    3 to stringResource(R.string.verb_level_3),
                    4 to stringResource(R.string.verb_level_4),
                    5 to stringResource(R.string.verb_level_5),
                )
                tierOptions.forEach { (tier, label) ->
                    val selected = cfg.maxTier == tier
                    androidx.compose.material3.Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) ACCENT.copy(alpha = 0.18f)
                                else androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { vm.updateConfig(cfg.copy(maxTier = tier)) },
                        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, ACCENT) else null,
                    ) {
                        Row(
                            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = { vm.updateConfig(cfg.copy(maxTier = tier)) },
                                colors = RadioButtonDefaults.colors(selectedColor = ACCENT),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(label, fontSize = 14.sp,
                                 fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                        }
                    }
                }
            }
        }

        SectionCard(title = stringResource(R.string.verb_section_groups)) {
            Column {
                ToggleRow(stringResource(R.string.verb_group_regular), VerbGroup.REGULAR in cfg.groups) { on ->
                    val g = if (on) cfg.groups + VerbGroup.REGULAR else cfg.groups - VerbGroup.REGULAR
                    if (g.isNotEmpty()) vm.updateConfig(cfg.copy(groups = g))
                }
                ToggleRow(stringResource(R.string.verb_group_irregular), VerbGroup.IRREGULAR in cfg.groups) { on ->
                    val g = if (on) cfg.groups + VerbGroup.IRREGULAR else cfg.groups - VerbGroup.IRREGULAR
                    if (g.isNotEmpty()) vm.updateConfig(cfg.copy(groups = g))
                }
            }
        }

        SectionCard(title = stringResource(R.string.verb_section_extra)) {
            Column {
                ToggleRow(stringResource(R.string.verb_include_reflexive), cfg.includeReflexive) {
                    vm.updateConfig(cfg.copy(includeReflexive = it))
                }
                ToggleRow(stringResource(R.string.verb_voseo), cfg.isVoseo) {
                    vm.updateConfig(cfg.copy(isVoseo = it))
                }
                ToggleRow(stringResource(R.string.verb_accept_no_accent), cfg.acceptNoAccent) {
                    vm.updateConfig(cfg.copy(acceptNoAccent = it))
                }
            }
        }

        SectionCard(title = stringResource(R.string.verb_section_count)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(10, 20, 30).forEach { n ->
                    FilterChip(
                        selected = cfg.questionCount == n,
                        onClick = { vm.updateConfig(cfg.copy(questionCount = n)) },
                        label = { Text(n.toString()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ACCENT,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { vm.startTraining() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ACCENT),
            enabled = cfg.selectedTenses.isNotEmpty() && cfg.groups.isNotEmpty()
        ) {
            Text("¡EMPEZAR!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, fontSize = 15.sp, color = TextGray, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = CardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) { Column(modifier = Modifier.padding(12.dp)) { content() } }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 15.sp, color = TextMain)
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = ACCENT)
        )
    }
}

private fun prettyTense(t: String): String = when (t) {
    "presente"    -> "Presente"
    "preterito"   -> "Indefinido"
    "imperfecto"  -> "Imperfecto"
    "futuro"      -> "Futuro"
    "condicional" -> "Condicional"
    "subjuntivo"  -> "Subjuntivo"
    else          -> t
}

// ══════════════════════════════════════════════════════════════
//  ТРЕНИРОВКА
// ══════════════════════════════════════════════════════════════

@Composable
private fun TrainingContent(state: VerbTrainingState, vm: VerbViewModel) {
    val q = state.questions.getOrNull(state.currentIndex) ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.verb_question_of, state.currentIndex + 1, state.total),
                color = ACCENT, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(stringResource(R.string.verb_score, state.score), color = ACCENT, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { (state.currentIndex + 1).toFloat() / state.total },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = ACCENT,
            trackColor = CardBorder
        )

        Spacer(Modifier.height(8.dp))

        when (state.config.mode) {
            VerbTrainingMode.CONJUGAR -> ConjugarCard(q, vm)
            VerbTrainingMode.HUECO    -> HuecoCard(q, vm)
            VerbTrainingMode.AUDITIVO -> AuditivoCard(q, vm)
            VerbTrainingMode.INVERSO  -> InversoCard(q, vm)
        }
    }
}

@Composable
private fun ConjugarCard(q: VerbQuestion, vm: VerbViewModel) {
    QuestionCard(
        title   = prettyTense(q.conjugation.tense),
        bigLine = "${vm.getPronoun(q.pronounIndex)} · ${q.conjugation.verb}",
        hint    = q.conjugation.note.ifBlank { null }
    )
    AnswerArea(q, vm)
}

@Composable
private fun HuecoCard(q: VerbQuestion, vm: VerbViewModel) {
    QuestionCard(
        title   = prettyTense(q.conjugation.tense),
        bigLine = q.sentenceTemplate,
        hint    = "(${q.conjugation.verb})"
    )
    AnswerArea(q, vm)
}

@Composable
private fun AuditivoCard(q: VerbQuestion, vm: VerbViewModel) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .tappableForSpeak { vm.replayAudio() },
        shape = RoundedCornerShape(20.dp),
        color = CardSurface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(prettyTense(q.conjugation.tense), fontSize = 12.sp, color = TextGray)
            Spacer(Modifier.height(8.dp))
            IconButton(
                onClick = { vm.replayAudio() },
                modifier = Modifier.size(72.dp)
            ) {
                Icon(Icons.Default.VolumeUp, stringResource(R.string.verb_listen),
                    tint = ACCENT, modifier = Modifier.size(56.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.verb_listen_and_write),
                fontSize = 15.sp, color = TextGray, textAlign = TextAlign.Center)
        }
    }
    AnswerArea(q, vm)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InversoCard(q: VerbQuestion, vm: VerbViewModel) {
    var pickedInf   by remember(q) { mutableStateOf("") }
    var pickedTense by remember(q) { mutableStateOf("") }
    var pickedPron  by remember(q) { mutableIntStateOf(-1) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(20.dp),
        color = CardSurface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.verb_inverso_question),
                fontSize = 15.sp, color = TextGray, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(q.correctAnswer,
                fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = ACCENT_DARK)
        }
    }

    Spacer(Modifier.height(8.dp))
    DropdownPicker(stringResource(R.string.verb_dropdown_infinitive), q.conjugation.verb,
        listOf(q.conjugation.verb), pickedInf, isTense = false) { pickedInf = it }
    DropdownPicker(stringResource(R.string.verb_dropdown_tense), q.conjugation.tense,
        listOf("presente","preterito","imperfecto","futuro","condicional","subjuntivo"),
        pickedTense, isTense = true) { pickedTense = it }
    DropdownPicker(stringResource(R.string.verb_dropdown_person), "yo/tú/...",
        (0..5).map { vm.getPronoun(it) },
        if (pickedPron < 0) "" else vm.getPronoun(pickedPron),
        isTense = false
    ) { selected ->
        pickedPron = (0..5).firstOrNull { vm.getPronoun(it) == selected } ?: -1
    }

    Spacer(Modifier.height(12.dp))

    if (q.isChecked) {
        FeedbackBanner(q)
        Button(
            onClick = { vm.nextQuestion() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ACCENT),
            shape = RoundedCornerShape(14.dp)
        ) { Text(stringResource(R.string.verb_next), fontWeight = FontWeight.Bold) }
    } else {
        Button(
            onClick = { vm.submitInverso(pickedInf, pickedTense, pickedPron) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ACCENT),
            shape = RoundedCornerShape(14.dp),
            enabled = pickedInf.isNotBlank() && pickedTense.isNotBlank() && pickedPron >= 0
        ) { Text(stringResource(R.string.verb_check), fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun QuestionCard(title: String, bigLine: String, hint: String?) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(20.dp),
        color = CardSurface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = ACCENT.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ACCENT_DARK,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(bigLine,
                fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = TextMain,
                textAlign = TextAlign.Center)
            if (hint != null) {
                Spacer(Modifier.height(8.dp))
                Text(hint, fontSize = 15.sp, color = TextGray, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun AnswerArea(q: VerbQuestion, vm: VerbViewModel) {
    var input by remember(q) { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(q) {
        if (!q.isChecked) {
            try { focusRequester.requestFocus() } catch (_: Exception) { /* noop */ }
        }
    }

    OutlinedTextField(
        value = input,
        onValueChange = { if (!q.isChecked) input = it },
        placeholder = { Text(stringResource(R.string.verb_input_placeholder)) },
        singleLine = true,
        readOnly = q.isChecked,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
        shape = RoundedCornerShape(14.dp)
    )

    Spacer(Modifier.height(8.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()) {
        ACCENT_KEYS.forEach { ch ->
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clickable(enabled = !q.isChecked) {
                        val before = input.text.substring(0, input.selection.start)
                        val after  = input.text.substring(input.selection.end)
                        val combined = before + ch + after
                        input = TextFieldValue(combined,
                            selection = TextRange(before.length + ch.length))
                    },
                shape = RoundedCornerShape(8.dp),
                color = CardSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(ch, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ACCENT)
                }
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    if (q.isChecked) {
        FeedbackBanner(q)
        Button(
            onClick = { vm.nextQuestion() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ACCENT),
            shape = RoundedCornerShape(14.dp)
        ) { Text(stringResource(R.string.verb_next), fontWeight = FontWeight.Bold) }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { vm.submitAnswer("") },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text(stringResource(R.string.verb_dont_know)) }

            Button(
                onClick = { vm.submitAnswer(input.text) },
                modifier = Modifier.weight(1.5f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ACCENT),
                shape = RoundedCornerShape(14.dp),
                enabled = input.text.isNotBlank()
            ) { Text(stringResource(R.string.verb_check), fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun FeedbackBanner(q: VerbQuestion) {
    val (color, icon, text) = when {
        q.isCorrect == true && q.nearMiss ->
            Triple(Orange, Icons.Default.Check, stringResource(R.string.verb_feedback_near_miss, q.correctAnswer))
        q.isCorrect == true ->
            Triple(Green, Icons.Default.Check, stringResource(R.string.verb_feedback_correct, q.correctAnswer))
        else ->
            Triple(Red, Icons.Default.Close, stringResource(R.string.verb_feedback_wrong, q.correctAnswer))
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color)
            Spacer(Modifier.width(8.dp))
            Text(text, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DropdownPicker(
    label: String,
    hint: String,
    options: List<String>,
    selected: String,
    isTense: Boolean = false,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    fun pretty(raw: String): String = if (isTense) prettyTense(raw) else raw
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            color = CardSurface
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(label, fontSize = 11.sp, color = TextGray)
                Text(if (selected.isBlank()) "—  $hint" else pretty(selected),
                    fontSize = 16.sp,
                    color = if (selected.isBlank()) TextGray else TextMain,
                    fontWeight = FontWeight.SemiBold)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { o ->
                DropdownMenuItem(
                    text = { Text(pretty(o)) },
                    onClick = { onSelect(o); expanded = false }
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  РЕЗУЛЬТАТЫ
// ══════════════════════════════════════════════════════════════

@Composable
private fun ResultsContent(state: VerbTrainingState, vm: VerbViewModel, onExit: () -> Unit) {
    val total = state.total
    val percent = if (total > 0)
        ((state.correctCount + state.nearMissCount * 0.5f) * 100 / total).toInt() else 0
    val weak = state.questions.filter { it.isCorrect == false }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎯", fontSize = 64.sp)
        Text(stringResource(R.string.verb_results_title),
            fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextMain)
        Spacer(Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround) {
            Stat(stringResource(R.string.verb_stat_accuracy), "$percent%")
            Stat(stringResource(R.string.verb_stat_correct), "${state.correctCount}/$total")
            if (state.nearMissCount > 0) Stat(stringResource(R.string.verb_stat_no_accent), "${state.nearMissCount}")
        }

        Spacer(Modifier.height(28.dp))

        if (weak.isNotEmpty()) {
            Text(stringResource(R.string.verb_weak_forms), fontSize = 15.sp, color = TextGray,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    weak.forEach { q ->
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                "${vm.getPronoun(q.pronounIndex)} · ${q.conjugation.verb}",
                                fontSize = 15.sp, color = TextMain
                            )
                            Text(
                                q.correctAnswer,
                                fontSize = 15.sp, color = ACCENT,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onExit,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text(stringResource(R.string.verb_to_menu)) }

            OutlinedButton(
                onClick = { vm.openSetup() },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Replay, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.verb_again))
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ACCENT)
        Text(label, fontSize = 12.sp, color = TextGray)
    }
}
