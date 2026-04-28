package com.spanishapp.ui.games

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

private val Purple = Color(0xFF7B2FBE)
private val BgGray = Color(0xFFF8F8FA)
private val TextMain = Color(0xFF1A1A1A)
private val TextGray = Color(0xFF8E8E93)
private val CardBorder = Color(0xFFE5E5EA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerbTrainingScreen(
    navController: NavHostController,
    viewModel: VerbViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verbos: Тренажер", fontWeight = FontWeight.Bold) },
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
                .padding(padding)
                .background(BgGray)
        ) {
            when {
                state.showSetup -> VerbSetupPremium(state.config, viewModel)
                state.isGameOver -> VerbResultScreen(state, onFinish = { navController.popBackStack() })
                else -> VerbActiveTraining(state, viewModel)
            }
        }
    }
}

@Composable
fun VerbSetupPremium(config: VerbWorkoutConfig, viewModel: VerbViewModel) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Upper button
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                "начать с прежними настройками",
                fontSize = 11.sp,
                color = TextGray,
                modifier = Modifier
                    .border(0.5.dp, CardBorder, RoundedCornerShape(4.dp))
                    .clickable { viewModel.startTraining() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            // LEFT COLUMN
            Column(modifier = Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Времена:", color = Purple, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                
                Panel(title = "Modo Indicativo:") {
                    TenseCheck("Presente", "presente", config, viewModel)
                    TenseCheck("Pretérito Perfecto", "preterito_perfecto", config, viewModel)
                    TenseCheck("Pretérito Indefinido", "preterito", config, viewModel)
                    TenseCheck("Pretérito Imperfecto", "imperfecto", config, viewModel)
                    TenseCheck("Pretérito Pluscuamperfecto", "pluscuamperfecto", config, viewModel)
                    TenseCheck("Pretérito Anterior", "anterior", config, viewModel)
                }

                Panel(title = "Futuros:") {
                    TenseCheck("Futuro Simple", "futuro", config, viewModel)
                    TenseCheck("Futuro Compuesto", "futuro_compuesto", config, viewModel)
                    TenseCheck("Condicional Simple", "condicional", config, viewModel)
                    TenseCheck("Condicional Compuesto", "condicional_compuesto", config, viewModel)
                }

                Panel(title = "Modo Imperativo:") {
                    TenseCheck("Afirmativo", "afirmativo", config, viewModel)
                    TenseCheck("Negativo", "negativo", config, viewModel)
                }

                Panel(title = "Modo Subjuntivo:") {
                    TenseCheck("Presente", "subjuntivo_presente", config, viewModel)
                    TenseCheck("Perfecto", "subjuntivo_perfecto", config, viewModel)
                    TenseCheck("Imperfecto", "subjuntivo", config, viewModel)
                    TenseCheck("Pluscuamperfecto", "subjuntivo_pluscuamperfecto", config, viewModel)
                }
            }

            Spacer(Modifier.width(12.dp))

            // RIGHT COLUMN
            Column(modifier = Modifier.weight(0.9f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Panel(title = "Тип глаголов:") {
                    GroupSwitch("правильные", VerbGroup.REGULAR, config, viewModel)
                    GroupSwitch("отклоняющиеся", VerbGroup.STEM, config, viewModel)
                    GroupSwitch("неправильные", VerbGroup.IRREGULAR, config, viewModel)
                }

                Panel(title = "Возвратность:") {
                    ReflexiveSwitch("невозвратные", false, config, viewModel)
                    ReflexiveSwitch("возвратные", true, config, viewModel)
                }

                Panel(title = "Список глаголов:") {
                    val limits = listOf("топ-50", "топ-100", "топ-200", "все", "свой список")
                    limits.forEach { limit ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.updateConfig(config.copy(limitType = limit)) }
                        ) {
                            RadioButton(
                                selected = config.limitType == limit,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = Purple),
                                modifier = Modifier.scale(0.8f)
                            )
                            Text(limit, color = TextMain, fontSize = 13.sp)
                        }
                    }
                }

                Panel(title = "Режим тренировки:") {
                    ModeSwitch("выбрать форму", VerbTrainingMode.CHOICE, config, viewModel)
                    ModeSwitch("расставить формы", VerbTrainingMode.ASSEMBLY, config, viewModel)
                    ModeSwitch("вписать форму", VerbTrainingMode.INPUT, config, viewModel)
                }

                Panel(title = "Самостоятельная проверка") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = config.selfCheck,
                            onCheckedChange = { viewModel.updateConfig(config.copy(selfCheck = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Purple),
                            modifier = Modifier.scale(0.8f)
                        )
                        Text("включить", color = TextMain, fontSize = 13.sp)
                    }
                }
            }
        }

        // Bottom Panels
        Panel(title = "Аргентинский диалект") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = config.isVoseo,
                    onCheckedChange = { viewModel.updateConfig(config.copy(isVoseo = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Purple),
                    modifier = Modifier.scale(0.8f)
                )
                Text("включить", color = TextMain, fontSize = 14.sp)
            }
        }

        Panel(title = "Таймер") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = config.hasTimer,
                    onCheckedChange = { viewModel.updateConfig(config.copy(hasTimer = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Purple),
                    modifier = Modifier.scale(0.8f)
                )
                Spacer(Modifier.width(16.dp))
                IconButton(
                    onClick = { if(config.timerValue > 1) viewModel.updateConfig(config.copy(timerValue = config.timerValue - 1)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ChevronLeft, null, tint = Purple)
                }
                Surface(
                    color = Purple.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        String.format("%02d", config.timerValue),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Bold,
                        color = TextMain,
                        fontSize = 16.sp
                    )
                }
                IconButton(
                    onClick = { viewModel.updateConfig(config.copy(timerValue = config.timerValue + 1)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ChevronRight, null, tint = Purple)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { viewModel.startTraining() },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple),
            enabled = config.selectedTenses.isNotEmpty() && config.groups.isNotEmpty()
        ) {
            Text("НАЧАТЬ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun Panel(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, color = Purple, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
fun TenseCheck(label: String, tense: String, config: VerbWorkoutConfig, viewModel: VerbViewModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val new = if (tense in config.selectedTenses) config.selectedTenses - tense else config.selectedTenses + tense
                viewModel.updateConfig(config.copy(selectedTenses = new))
            }
    ) {
        Checkbox(
            checked = tense in config.selectedTenses,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(checkedColor = Purple),
            modifier = Modifier.scale(0.8f).size(24.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(label, color = TextMain, fontSize = 11.sp, lineHeight = 13.sp)
    }
}

@Composable
fun GroupSwitch(label: String, group: VerbGroup, config: VerbWorkoutConfig, viewModel: VerbViewModel) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(32.dp)) {
        Switch(
            checked = group in config.groups,
            onCheckedChange = { 
                val new = if (it) config.groups + group else config.groups - group
                viewModel.updateConfig(config.copy(groups = new))
            },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Purple),
            modifier = Modifier.scale(0.7f)
        )
        Text(label, color = TextMain, fontSize = 12.sp, modifier = Modifier.padding(start = 2.dp))
    }
}

@Composable
fun ReflexiveSwitch(label: String, reflexive: Boolean, config: VerbWorkoutConfig, viewModel: VerbViewModel) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(32.dp)) {
        Switch(
            checked = reflexive in config.reflexive,
            onCheckedChange = { 
                val new = if (it) config.reflexive + reflexive else config.reflexive - reflexive
                viewModel.updateConfig(config.copy(reflexive = new))
            },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Purple),
            modifier = Modifier.scale(0.7f)
        )
        Text(label, color = TextMain, fontSize = 12.sp, modifier = Modifier.padding(start = 2.dp))
    }
}

@Composable
fun ModeSwitch(label: String, mode: VerbTrainingMode, config: VerbWorkoutConfig, viewModel: VerbViewModel) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(32.dp)) {
        Switch(
            checked = config.mode == mode,
            onCheckedChange = { if(it) viewModel.updateConfig(config.copy(mode = mode)) },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Purple),
            modifier = Modifier.scale(0.7f)
        )
        Text(label, color = TextMain, fontSize = 12.sp, modifier = Modifier.padding(start = 2.dp))
    }
}

@Composable
fun VerbActiveTraining(state: VerbTrainingState, viewModel: VerbViewModel) {
    val q = state.questions.getOrNull(state.currentIndex) ?: return
    var inputText by remember(state.currentIndex) { mutableStateOf("") }
    val assemblyValues = remember(state.currentIndex) { mutableStateListOf(*q.allUserValues.toTypedArray()) }
    val green = Color(0xFF4CAF50)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${state.currentIndex + 1}/${state.questions.size}", color = Purple, fontWeight = FontWeight.Bold)
            if (state.config.hasTimer) {
                Text(
                    String.format("%02d:%02d", state.timeLeftSeconds / 60, state.timeLeftSeconds % 60),
                    color = if (state.timeLeftSeconds < 15) Color.Red else TextMain,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Очки: ${state.score}", color = Purple, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = { (state.currentIndex + 1).toFloat() / state.questions.size },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = Purple,
            trackColor = CardBorder
        )
        
        Spacer(Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(q.conjugation.tense.uppercase(), color = Purple, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Text(q.conjugation.verb, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = TextMain)
                if (state.config.mode != VerbTrainingMode.ASSEMBLY) {
                    Spacer(Modifier.height(4.dp))
                    Text("${viewModel.getPronoun(q.pronounIndex)} → ?", color = TextGray, fontSize = 20.sp)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        when (state.config.mode) {
            VerbTrainingMode.CHOICE -> ChoiceContent(q, viewModel)
            VerbTrainingMode.INPUT -> InputContent(q, inputText, onValueChange = { inputText = it }, viewModel)
            VerbTrainingMode.ASSEMBLY -> AssemblyContent(q, assemblyValues, viewModel)
        }

        Spacer(Modifier.height(32.dp))

        val isAnswered = if (state.config.mode == VerbTrainingMode.ASSEMBLY) q.allChecked else q.isChecked
        if (isAnswered) {
            Button(
                onClick = { viewModel.nextQuestion() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                Text("ДАЛЕЕ", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ChoiceContent(q: VerbQuestion, viewModel: VerbViewModel) {
    val green = Color(0xFF4CAF50)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        q.options.forEach { option ->
            val isSelected = q.isChecked && q.userValue == option
            val isCorrect = q.isChecked && option == q.correctAnswer
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clickable(enabled = !q.isChecked) { viewModel.submitAnswer(option) },
                shape = RoundedCornerShape(16.dp),
                color = when {
                    isCorrect -> green
                    isSelected -> Color(0xFFF44336)
                    else -> Color.White
                },
                border = if (!isCorrect && !isSelected) androidx.compose.foundation.BorderStroke(1.dp, CardBorder) else null
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        option,
                        fontSize = 18.sp,
                        color = if (isCorrect || isSelected) Color.White else TextMain,
                        fontWeight = if (isCorrect || isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        if (q.isChecked && q.isCorrect == false) {
            Text("Верно: ${q.correctAnswer}", color = green, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun InputContent(q: VerbQuestion, text: String, onValueChange: (String) -> Unit, viewModel: VerbViewModel) {
    val green = Color(0xFF4CAF50)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Введите форму") },
            enabled = !q.isChecked,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Purple,
                unfocusedBorderColor = CardBorder
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { viewModel.submitAnswer(text) })
        )
        if (q.isChecked) {
            val color = if (q.isCorrect == true) green else Color(0xFFF44336)
            Text(
                if (q.isCorrect == true) "¡Excelente!" else "Ошибка! Верно: ${q.correctAnswer}",
                color = color,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Button(
                onClick = { viewModel.submitAnswer(text) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("ПРОВЕРИТЬ", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AssemblyContent(q: VerbQuestion, values: MutableList<String>, viewModel: VerbViewModel) {
    val green = Color(0xFF4CAF50)
    val correctForms = listOf(q.conjugation.yo, q.conjugation.tu, q.conjugation.el, q.conjugation.nosotros, q.conjugation.vosotros, q.conjugation.ellos)
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        correctForms.forEachIndexed { index, correct ->
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        viewModel.getPronoun(index),
                        modifier = Modifier.width(80.dp),
                        fontWeight = FontWeight.Bold,
                        color = Purple
                    )
                    OutlinedTextField(
                        value = values[index],
                        onValueChange = { if (!q.allChecked) values[index] = it },
                        modifier = Modifier.weight(1f),
                        enabled = !q.allChecked,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = if (q.allChecked) {
                                if (q.allResults[index] == true) green else Color(0xFFF44336)
                            } else CardBorder,
                            focusedBorderColor = Purple
                        )
                    )
                }
                if (q.allChecked && q.allResults[index] == false) {
                    Text(
                        correct,
                        color = green,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 88.dp, top = 2.dp)
                    )
                }
            }
        }
        
        if (!q.allChecked) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { viewModel.submitAssembly(values.toList()) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("ПРОВЕРИТЬ ВСЁ", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun VerbResultScreen(state: VerbTrainingState, onFinish: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎓", fontSize = 80.sp)
        Spacer(Modifier.height(16.dp))
        Text("Тренировка окончена", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextMain)
        Text("Ваш результат: ${state.score}", fontSize = 20.sp, color = Purple, fontWeight = FontWeight.SemiBold)
        
        Spacer(Modifier.height(40.dp))
        
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("В МЕНЮ", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
