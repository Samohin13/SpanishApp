package com.spanishapp.ui.games

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController

private val LibroGreen  = Color(0xFF43A047)
private val LibroRed    = Color(0xFFE53935)
private val LibroPurple = Color(0xFF7B2FBE)

// ── Состояние экрана ─────────────────────────────────────────
private sealed interface ReadState {
    object Reading : ReadState
    data class Quiz(val qIndex: Int, val answers: List<Int?>) : ReadState
    data class Result(val correct: Int, val total: Int) : ReadState
}

// ── Вспомогательные функции ──────────────────────────────────

private fun extractWordWithRange(text: String, offset: Int): Pair<String, IntRange> {
    if (offset < 0 || offset >= text.length) return "" to 0..0
    if (!text[offset].isLetter()) return "" to 0..0
    var start = offset
    var end = offset
    while (start > 0 && text[start - 1].isLetter()) start--
    while (end < text.length - 1 && text[end + 1].isLetter()) end++
    return text.substring(start, end + 1) to start..end
}

private fun extractSentenceAt(text: String, offset: Int): String {
    if (offset < 0 || offset >= text.length) return ""
    val delimiters = setOf('.', '!', '?', '\n')
    var start = offset
    var end = offset
    while (start > 0 && text[start - 1] !in delimiters) start--
    while (end < text.length - 1 && text[end] !in delimiters) end++
    return text.substring(start, end + 1).trim()
}

// ── TranslatableText ─────────────────────────────────────────

@Composable
private fun TranslatableText(
    text: String,
    modifier: Modifier = Modifier,
    onLongPress: (word: String, sentence: String) -> Unit,
    onTap: () -> Unit = {}
) {
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var highlightRange by remember { mutableStateOf<IntRange?>(null) }

    val annotated = remember(text, highlightRange) {
        buildAnnotatedString {
            append(text)
            highlightRange?.let { r ->
                addStyle(
                    SpanStyle(
                        background = Color(0xFFE3F2FD),
                        color = Color(0xFF1565C0)
                    ),
                    r.first,
                    minOf(r.last + 1, text.length)
                )
            }
        }
    }

    Text(
        text = annotated,
        fontSize = 17.sp,
        lineHeight = 26.sp,
        color = Color(0xFF1A1A1A),
        modifier = modifier.pointerInput(text) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val longPressMs = viewConfiguration.longPressTimeoutMillis
                val up = withTimeoutOrNull(longPressMs) {
                    waitForUpOrCancellation()
                }
                if (up == null) {
                    // Long press — ищем слово под пальцем
                    layoutResult?.let { layout ->
                        val charPos = layout.getOffsetForPosition(down.position)
                        val (word, range) = extractWordWithRange(text, charPos)
                        val sentence = extractSentenceAt(text, charPos)
                        if (word.isNotEmpty()) {
                            highlightRange = range
                            onLongPress(word, sentence)
                        }
                    }
                    // Дожидаемся отпускания пальца
                    do {
                        val event = awaitPointerEvent()
                    } while (event.changes.any { it.pressed })
                } else {
                    // Tap — убираем подсветку
                    highlightRange = null
                    onTap()
                }
            }
        },
        onTextLayout = { layoutResult = it }
    )
}

// ── TranslationBanner ────────────────────────────────────────

@Composable
private fun TranslationBanner(
    translation: TranslationState,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = translation.visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A237E)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Слово + перевод
                    Column(Modifier.weight(1f)) {
                        Text(
                            translation.word,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        if (translation.wordRu.isNotEmpty()) {
                            Text(
                                translation.wordRu,
                                fontSize = 14.sp,
                                color = Color(0xFFB0BEC5)
                            )
                        } else {
                            Text(
                                "не найдено в словаре",
                                fontSize = 13.sp,
                                color = Color(0xFF78909C)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Color(0xFF90A4AE))
                    }
                }

                // Слова предложения
                if (translation.sentenceWords.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFF283593))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Слова в предложении:",
                        fontSize = 11.sp,
                        color = Color(0xFF78909C),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(translation.sentenceWords) { (es, ru) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF283593))
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(es, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text(ru, fontSize = 11.sp, color = Color(0xFFB0BEC5))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Главный экран ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibroReadScreen(
    navController: NavHostController,
    libroId: Int,
    vm: LibrosViewModel = hiltViewModel()
) {
    val libro = remember(libroId) { LibrosData.getById(libroId) }

    if (libro == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    val levelColor = mapOf(
        "A1" to Color(0xFF43A047), "A2" to Color(0xFF1E88E5),
        "B1" to Color(0xFFE65100), "B2" to Color(0xFF6A1B9A)
    )[libro.level] ?: LibroPurple

    var state: ReadState by remember { mutableStateOf(ReadState.Reading) }
    val translation by vm.translation.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(libro.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.clip(RoundedCornerShape(5.dp))
                                    .background(levelColor)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) { Text(libro.level, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                            Spacer(Modifier.width(6.dp))
                            DifficultyDots(libro.difficulty, size = 8)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F8FA)
    ) { padding ->

        when (val s = state) {

            // ── Режим чтения ─────────────────────────────────
            is ReadState.Reading -> {
                Column(
                    Modifier.fillMaxSize().padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Баннер перевода (сверху, анимированный)
                    TranslationBanner(
                        translation = translation,
                        onDismiss = { vm.dismissTranslation() }
                    )

                    Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                        // Подсказка для новых пользователей
                        if (!translation.visible) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEDE7F6))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💡", fontSize = 14.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Удержите слово для перевода",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6A1B9A)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                        }

                        // Карточка с текстом
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📖", fontSize = 22.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Читаем",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.height(14.dp))
                                TranslatableText(
                                    text = libro.text.trim(),
                                    onLongPress = { word, sentence ->
                                        vm.lookupWord(word, sentence)
                                    },
                                    onTap = { vm.dismissTranslation() }
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Тема
                        Row(
                            Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(levelColor.copy(alpha = 0.08f))
                                .padding(12.dp)
                        ) {
                            Text("🏷️ Тема: ", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = levelColor)
                            Text(libro.topic, fontSize = 13.sp, color = Color(0xFF555555))
                        }

                        Spacer(Modifier.height(16.dp))

                        // Подсказка
                        Text(
                            "Прочитайте рассказ внимательно — затем ответьте на ${libro.questions.size} вопроса. " +
                            "Для зачёта нужно ${LibrosData.PASS_CORRECT} из ${libro.questions.size} правильных ответов.",
                            fontSize = 13.sp,
                            color = Color(0xFF8E8E93),
                            lineHeight = 20.sp
                        )

                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = {
                                vm.dismissTranslation()
                                state = ReadState.Quiz(0, List(libro.questions.size) { null })
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = levelColor)
                        ) {
                            Text("Начать тест →", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── Режим теста ──────────────────────────────────
            is ReadState.Quiz -> {
                val q = libro.questions[s.qIndex]
                val totalQ = libro.questions.size
                val selectedAnswer = s.answers[s.qIndex]

                Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {

                    // Прогресс
                    Text(
                        "Вопрос ${s.qIndex + 1} из $totalQ",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (s.qIndex + 1f) / totalQ },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = levelColor,
                        trackColor = levelColor.copy(alpha = 0.2f)
                    )

                    Spacer(Modifier.height(24.dp))

                    // Карточка вопроса
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text("❓", fontSize = 24.sp)
                            Spacer(Modifier.height(10.dp))
                            Text(q.question, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Варианты ответа A / B / C
                    val labels = listOf("A", "B", "C")
                    q.options.forEachIndexed { idx, option ->
                        val isSelected = selectedAnswer == idx
                        val bgColor = if (isSelected) levelColor else Color.White
                        val textColor = if (isSelected) Color.White else Color(0xFF1A1A1A)
                        val borderColor = if (isSelected) levelColor else Color(0xFFE0E0E0)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
                                .background(bgColor)
                                .clickable {
                                    val newAnswers = s.answers.toMutableList()
                                    newAnswers[s.qIndex] = idx
                                    state = s.copy(answers = newAnswers)
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color.White.copy(alpha = 0.25f) else levelColor.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(labels[idx], fontWeight = FontWeight.Bold, fontSize = 13.sp,
                                    color = if (isSelected) Color.White else levelColor)
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(option, fontSize = 15.sp, color = textColor)
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Кнопки навигации по вопросам
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (s.qIndex > 0) {
                            OutlinedButton(
                                onClick = { state = s.copy(qIndex = s.qIndex - 1) },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) { Text("← Назад") }
                        }

                        val isLast = s.qIndex == totalQ - 1
                        val canProceed = selectedAnswer != null

                        Button(
                            onClick = {
                                if (isLast) {
                                    val correct = s.answers.zip(libro.questions)
                                        .count { (ans, q) -> ans == q.correctIndex }
                                    vm.saveResult(libro.id, correct, totalQ)
                                    state = ReadState.Result(correct, totalQ)
                                } else {
                                    state = s.copy(qIndex = s.qIndex + 1)
                                }
                            },
                            enabled = canProceed,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = levelColor)
                        ) {
                            Text(if (isLast) "Завершить" else "Дальше →", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── Экран результата ─────────────────────────────
            is ReadState.Result -> {
                val passed = s.correct >= LibrosData.PASS_CORRECT
                val pct = s.correct * 100 / s.total

                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(if (passed) "🎉" else "💪", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (passed) "Отлично! Рассказ прочитан!" else "Почти! Попробуй ещё раз",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = if (passed) LibroGreen else Color(0xFF1A1A1A)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Правильных ответов: ${s.correct} из ${s.total} ($pct%)",
                        fontSize = 16.sp,
                        color = Color(0xFF555555)
                    )
                    Spacer(Modifier.height(8.dp))

                    if (passed) {
                        Box(
                            Modifier.clip(RoundedCornerShape(12.dp))
                                .background(LibroGreen.copy(alpha = 0.1f))
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                "✓ Рассказ отмечен как прочитанный в вашей библиотеке",
                                color = LibroGreen,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Box(
                            Modifier.clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF3E0))
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                "Нужно ${LibrosData.PASS_CORRECT} из ${s.total}. Перечитайте текст и попробуйте снова!",
                                color = Color(0xFFE65100),
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (passed) LibroGreen else levelColor
                        )
                    ) {
                        Text("← К списку рассказов", fontWeight = FontWeight.Bold)
                    }

                    if (!passed) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { state = ReadState.Reading },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("Перечитать рассказ") }
                    }
                }
            }
        }
    }
}
