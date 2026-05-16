package com.spanishapp.ui.checkpoint

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.spanishapp.data.checkpoint.CheckpointAct
import com.spanishapp.data.checkpoint.CheckpointActType
import com.spanishapp.data.checkpoint.CheckpointOption
import com.spanishapp.ui.components.rememberSpanishTts
import com.spanishapp.ui.components.speakSpanish

/**
 * Экран прохождения 18-актного чекпоинт-сценария.
 *
 * Структура:
 *   • TopBar с прогресс-баром (act/total) + scene title
 *   • Narration + NPC реплика + аудио
 *   • Input в зависимости от типа акта
 *   • После ответа — explanation + кнопка «Дальше»
 *   • В финале — экран результата с XP, accuracy, сертификатом
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckpointSessionScreen(
    navController: NavHostController,
    vm: CheckpointSessionViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val tts = rememberSpanishTts()

    val terra = Color(0xFFFF5722)
    val gold = Color(0xFFFFC107)

    if (state.notFound) {
        NotFoundView(navController); return
    }
    val content = state.content ?: return

    if (state.finished) {
        FinishedView(state, content, navController, terra, gold); return
    }

    val totalActs = remember(content) { content.scenes.sumOf { it.acts.size } }
    val curScene = content.scenes.getOrNull(state.sceneIndex) ?: return
    val curAct = curScene.acts.getOrNull(state.actIndex) ?: return
    val actNumber = remember(state.sceneIndex, state.actIndex) {
        content.scenes.take(state.sceneIndex).sumOf { it.acts.size } + state.actIndex + 1
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("${content.emoji} ${content.title}",
                            fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text("${curScene.title} · акт $actNumber/$totalActs",
                            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    // Combo-счётчик + общий прогресс актов
                    com.spanishapp.ui.components.ComboBadge(serial = state.correctCount, accentColor = terra)
                    if (state.wrongCount > 0) {
                        Text("❌ ${state.wrongCount}",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 12.dp))
                    }
                },
            )
        }
    ) { padding ->
        // Сеттинг-градиент: меняется по эмодзи сцены/сценария
        val sceneEmoji = remember(curScene.title, content.emoji) {
            "${curScene.title} ${content.emoji}"
        }
        val bgBrush = com.spanishapp.ui.components.sceneGradientFor(sceneEmoji)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(bgBrush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Прогресс-бар с градиентом и эмодзи
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    LinearProgressIndicator(
                        progress = { actNumber / totalActs.toFloat() },
                        modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = terra,
                        trackColor = terra.copy(alpha = 0.15f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "$actNumber/$totalActs",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = terra,
                    )
                }
                Spacer(Modifier.height(16.dp))

                // Сеттинг сцены (показываем только на первом акте сцены)
                if (state.actIndex == 0) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.7f),
                        shadowElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(
                                curScene.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = terra,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                curScene.setting,
                                fontSize = 13.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }

                // Narration в стилизованной карточке
                if (curAct.narration.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            "💭 ${curAct.narration}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // NPC реплика — мессенджер-стиль chat-bubble
                if (curAct.npcLine.isNotBlank()) {
                    val emoji = curAct.npcSpeaker.split(" ").firstOrNull()?.takeIf {
                        it.codePoints().anyMatch { c -> c > 127 }
                    } ?: "👤"
                    val name = curAct.npcSpeaker.replace(emoji, "").trim()
                    com.spanishapp.ui.components.ChatBubble(
                        speaker = name,
                        text = curAct.npcLine,
                        translation = curAct.npcTranslation,
                        isMine = false,
                        avatar = emoji,
                        accentColor = terra,
                        showTapHint = true,
                        onTap = { tts?.speakSpanish(curAct.npcLine, "npc") },
                    )
                    Spacer(Modifier.height(14.dp))
                }

                // Input по типу
                ActInput(
                    act = curAct,
                    answered = state.answered,
                    lastCorrect = state.lastAnswerCorrect,
                    accentColor = terra,
                    onAnswer = vm::submitAnswer,
                    tts = tts,
                )

                // Реакция NPC + объяснение + кнопка
                AnimatedVisibility(
                    visible = state.answered,
                    enter = androidx.compose.animation.fadeIn() +
                        androidx.compose.animation.slideInVertically(initialOffsetY = { it / 3 }),
                ) {
                    Column {
                        Spacer(Modifier.height(14.dp))
                        // Эмоциональная реакция NPC
                        com.spanishapp.ui.components.NpcReaction(
                            isCorrect = state.lastAnswerCorrect == true,
                            customText = if (state.lastAnswerCorrect == true)
                                listOf("¡Perfecto!", "¡Muy bien!", "¡Excelente!", "¡Eso es!").random()
                            else
                                listOf("Casi... Mira la respuesta correcta",
                                    "No exactamente", "Ahora ves la fórmula").random()
                        )
                        if (curAct.explanation.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                                    Text("💡", fontSize = 16.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        curAct.explanation,
                                        fontSize = 13.sp,
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = { vm.nextAct() },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = terra),
                        ) {
                            Text(
                                if (actNumber == totalActs) "Завершить 🏆" else "Дальше →",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Suppress("unused")  // оставлен для возможного reuse — основной flow использует ChatBubble
private fun NpcBubble(act: CheckpointAct, tts: android.speech.tts.TextToSpeech?, accent: Color) {
    Surface(
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth().clickable { tts?.speakSpanish(act.npcLine, "npc") }
    ) {
        Column(Modifier.padding(14.dp)) {
            if (act.npcSpeaker.isNotBlank()) {
                Text(act.npcSpeaker, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accent)
                Spacer(Modifier.height(2.dp))
            }
            Text(act.npcLine, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            if (act.npcTranslation.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(act.npcTranslation, fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
            Spacer(Modifier.height(4.dp))
            Text("🔊 тапни чтобы прослушать", fontSize = 10.sp, color = accent)
        }
    }
}

@Composable
private fun ActInput(
    act: CheckpointAct,
    answered: Boolean,
    lastCorrect: Boolean?,
    accentColor: Color,
    onAnswer: (Boolean) -> Unit,
    tts: android.speech.tts.TextToSpeech?,
) {
    when (act.type) {
        CheckpointActType.PICK_PHRASE,
        CheckpointActType.LISTEN_AND_PICK,
        CheckpointActType.WHAT_HAPPENS -> {
            // Авто-проиграть аудио для LISTEN_AND_PICK
            LaunchedEffect(act) {
                if (act.type == CheckpointActType.LISTEN_AND_PICK && act.npcLine.isNotBlank()) {
                    kotlinx.coroutines.delay(400)
                    tts?.speakSpanish(act.npcLine, "auto")
                }
            }
            OptionsList(act.options, answered, accentColor, onAnswer)
        }
        CheckpointActType.TYPE_REPLY -> {
            TypeReplyInput(act.expectedReply, answered, accentColor, onAnswer)
        }
        CheckpointActType.SAY_OUT_LOUD -> {
            // STT тяжело, упрощаем — кнопка «прослушать эталон» + «понял»
            SayOutLoudFallback(act.expectedReply, tts, answered, accentColor, onAnswer)
        }
        CheckpointActType.BUILD_REPLY -> {
            BuildReplyInput(act.expectedReply, act.replyTokens, answered, accentColor, onAnswer)
        }
        CheckpointActType.NARRATION_ONLY -> {
            // Кнопка «Понял»
            Button(onClick = { onAnswer(true) }, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)) {
                Text("Понятно ✓")
            }
        }
    }
}

@Composable
private fun OptionsList(
    options: List<CheckpointOption>,
    answered: Boolean,
    accentColor: Color,
    onAnswer: (Boolean) -> Unit,
) {
    var picked by remember(options) { mutableStateOf<CheckpointOption?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEach { opt ->
            val isPicked = picked == opt
            val bg = when {
                !answered && isPicked -> accentColor.copy(alpha = 0.15f)
                answered && opt.isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.18f)
                answered && isPicked && !opt.isCorrect -> Color(0xFFFF5252).copy(alpha = 0.18f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = bg,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !answered) {
                        picked = opt
                        onAnswer(opt.isCorrect)
                    }
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(opt.spanish, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    if (opt.russian.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(opt.russian, fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeReplyInput(
    expected: String,
    answered: Boolean,
    accentColor: Color,
    onAnswer: (Boolean) -> Unit,
) {
    var input by remember { mutableStateOf(TextFieldValue("")) }
    Column {
        OutlinedTextField(
            value = input,
            onValueChange = { if (!answered) input = it },
            label = { Text("Напечатай ответ по-испански") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !answered,
            singleLine = false,
        )
        Spacer(Modifier.height(8.dp))
        if (!answered) {
            Button(
                onClick = {
                    val correct = input.text.trim()
                        .replace(Regex("[¿?¡!.,]"), "")
                        .equals(expected.trim().replace(Regex("[¿?¡!.,]"), ""), ignoreCase = true)
                    onAnswer(correct)
                },
                enabled = input.text.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            ) { Text("Проверить") }
        } else {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Эталон: $expected",
                    fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(12.dp))
            }
        }
    }
}

@Composable
private fun SayOutLoudFallback(
    expected: String,
    tts: android.speech.tts.TextToSpeech?,
    answered: Boolean,
    accentColor: Color,
    onAnswer: (Boolean) -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()) {
        Text("🎙 Произнеси вслух:",
            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Surface(shape = RoundedCornerShape(14.dp),
            color = accentColor.copy(alpha = 0.10f),
            modifier = Modifier.fillMaxWidth()) {
            Text(expected, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp).fillMaxWidth())
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { tts?.speakSpanish(expected, "ref") },
                modifier = Modifier.weight(1f)) {
                Text("🔊 Эталон")
            }
            Button(onClick = { onAnswer(true) },
                modifier = Modifier.weight(1f),
                enabled = !answered,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)) {
                Text("Произнёс ✓")
            }
        }
    }
}

@Composable
private fun BuildReplyInput(
    expected: String,
    tokens: List<String>,
    answered: Boolean,
    accentColor: Color,
    onAnswer: (Boolean) -> Unit,
) {
    val shuffled = remember(tokens) { tokens.shuffled() }
    val chosen = remember(tokens) { mutableStateListOf<String>() }
    val pool = remember(tokens) { mutableStateListOf(*shuffled.toTypedArray()) }

    val built = chosen.joinToString(" ")

    Column {
        // Зона собранного
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp)
        ) {
            Box(Modifier.padding(12.dp)) {
                if (chosen.isEmpty()) {
                    Text("Тапай слова в порядке", fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(built, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        // Тайлы из пула
        FlowRowSimpleCp(items = pool.toList()) { tile ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = accentColor.copy(alpha = 0.12f),
                modifier = Modifier.padding(end = 6.dp, bottom = 6.dp)
                    .clickable(enabled = !answered) {
                        chosen.add(tile)
                        pool.remove(tile)
                    }
            ) {
                Text(tile, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
        Row {
            OutlinedButton(onClick = {
                if (chosen.isNotEmpty()) {
                    val last = chosen.removeAt(chosen.lastIndex); pool.add(last)
                }
            }, enabled = !answered && chosen.isNotEmpty(),
                modifier = Modifier.weight(1f)) { Text("← Удалить") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                val correct = built.trim()
                    .replace(Regex("[¿?¡!.,]"), "")
                    .equals(expected.trim().replace(Regex("[¿?¡!.,]"), ""), ignoreCase = true)
                onAnswer(correct)
            }, enabled = !answered && pool.isEmpty(),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)) {
                Text("Проверить")
            }
        }
    }
}

@Composable
private fun FlowRowSimpleCp(
    items: List<String>,
    itemContent: @Composable (String) -> Unit,
) {
    val rows = items.chunked(3)
    Column {
        rows.forEach { row ->
            Row { row.forEach { itemContent(it) } }
        }
    }
}

@Composable
private fun NotFoundView(navController: NavHostController) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🔍 Чекпоинт не найден", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { navController.popBackStack() }) { Text("Назад") }
        }
    }
}

@Composable
private fun FinishedView(
    state: CheckpointSessionState,
    content: com.spanishapp.data.checkpoint.CheckpointContent,
    navController: NavHostController,
    accentColor: Color,
    gold: Color,
) {
    Box(
        Modifier.fillMaxSize().background(
            androidx.compose.ui.graphics.Brush.verticalGradient(
                listOf(accentColor.copy(alpha = 0.10f), Color.Transparent)
            )
        ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp).verticalScroll(rememberScrollState())
        ) {
            Text(
                if (state.passed) "🏆🎉" else "💪",
                fontSize = 80.sp,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                if (state.passed) "ПРОЙДЕНО!" else "Почти получилось",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (state.passed) gold else accentColor,
            )
            Spacer(Modifier.height(8.dp))
            Text(content.title, fontSize = 16.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    StatRow("Точность", "${state.accuracyPercent}%")
                    StatRow("Правильно", "✅ ${state.correctCount}")
                    StatRow("Ошибок", "❌ ${state.wrongCount}")
                    if (state.passed && state.xpEarned > 0) {
                        Spacer(Modifier.height(8.dp))
                        Text("+${state.xpEarned} XP", fontSize = 22.sp,
                            fontWeight = FontWeight.Bold, color = gold)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                if (state.passed) content.outroSuccess else content.outroPartial,
                fontSize = 14.sp, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            ) {
                Text("Продолжить", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
