package com.spanishapp.ui.games

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.spanishapp.R
import com.spanishapp.domain.algorithm.LeaguePromotion
import com.spanishapp.service.SpanishSpeechRecognizer
import com.spanishapp.service.SpeechResult
import com.spanishapp.ui.components.LeaguePromotionDialog
import com.spanishapp.ui.components.rememberSpanishTts
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

private val LibroGreen  = Color(0xFF43A047)
private val LibroPurple = Color(0xFF7B2FBE)

private sealed interface ReadState {
    object Reading : ReadState
    data class Quiz(val qIndex: Int, val answers: List<Int?>) : ReadState
    data class Result(val correct: Int, val total: Int) : ReadState
    data class ReadingAloud(
        val sentences: List<String>,
        val currentIdx: Int = 0,
        val results: List<SentenceResult?> = emptyList(),
        val isListening: Boolean = false,
        val recognizedText: String = ""
    ) : ReadState
    data class ReadingDone(
        val sentences: List<String>,
        val results: List<SentenceResult>
    ) : ReadState
}

data class SentenceResult(val wordChecks: List<Pair<String, Boolean>>)

// ── STT entry point ───────────────────────────────────────────

@EntryPoint
@InstallIn(SingletonComponent::class)
private interface LibroSpeechEntryPoint {
    fun speechRecognizer(): SpanishSpeechRecognizer
}

// ── Вспомогательные функции ───────────────────────────────────

private fun splitSentences(text: String): List<String> =
    text.split(Regex("(?<=[.!?])\\s+"))
        .map { it.trim() }
        .filter { it.isNotBlank() && it.any { c -> c.isLetter() } }

private fun normalizeWord(w: String): String =
    w.lowercase()
        .replace('á','a').replace('é','e').replace('í','i')
        .replace('ó','o').replace('ú','u').replace('ü','u').replace('ñ','n')
        .filter { it.isLetter() }

private fun levenshtein(a: String, b: String): Int {
    val dp = Array(a.length + 1) { IntArray(b.length + 1) }
    for (i in 0..a.length) dp[i][0] = i
    for (j in 0..b.length) dp[0][j] = j
    for (i in 1..a.length) for (j in 1..b.length)
        dp[i][j] = if (a[i-1] == b[j-1]) dp[i-1][j-1]
                   else 1 + minOf(dp[i-1][j], dp[i][j-1], dp[i-1][j-1])
    return dp[a.length][b.length]
}

private fun wordMatch(exp: String, rec: String): Boolean {
    val a = normalizeWord(exp); val b = normalizeWord(rec)
    if (a.isEmpty() || b.isEmpty()) return false
    val sim = 1f - levenshtein(a, b).toFloat() / maxOf(a.length, b.length)
    return sim >= 0.80f
}

private fun compareToExpected(expected: String, recognized: String): List<Pair<String, Boolean>> {
    val expTokens = expected.split(Regex("[\\s,;:!?¡¿.«»\"'()\\-]+")).filter { it.isNotBlank() }
    val recTokens = recognized.split(Regex("\\s+")).filter { it.isNotBlank() }.toMutableList()
    return expTokens.map { expWord ->
        val matchIdx = recTokens.indexOfFirst { wordMatch(expWord, it) }
        if (matchIdx >= 0) {
            recTokens.removeAt(matchIdx)
            expWord to true
        } else expWord to false
    }
}

// покрыты тестами в LibroTextHelpersTest
internal fun extractWordAt(text: String, offset: Int): String {
    if (offset < 0 || offset >= text.length || !text[offset].isLetter()) return ""
    var start = offset; var end = offset
    while (start > 0 && text[start - 1].isLetter()) start--
    while (end < text.length - 1 && text[end + 1].isLetter()) end++
    return text.substring(start, end + 1)
}

internal fun extractSentenceAt(text: String, offset: Int): String {
    if (offset < 0 || offset >= text.length) return ""
    val d = setOf('.', '!', '?', '\n')
    var start = offset; var end = offset
    while (start > 0 && text[start - 1] !in d) start--
    while (end < text.length - 1 && text[end] !in d) end++
    return text.substring(start, end + 1).trim()
}

// ── Текст с подсветкой и long-press ──────────────────────────
// Работает БЕЗ конфликта, потому что родительский Column не имеет
// verticalScroll — вся страница вписывается в экран.

@Composable
private fun StoryText(
    text: String,
    highlightRange: IntRange?,
    onLongPress: (word: String, sentence: String) -> Unit
) {
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val annotated = remember(text, highlightRange) {
        buildAnnotatedString {
            append(text)
            highlightRange?.let { r ->
                addStyle(
                    SpanStyle(background = Color(0xFFE8EAF6), color = Color(0xFF283593)),
                    r.first, minOf(r.last + 1, text.length)
                )
            }
        }
    }

    Text(
        text = annotated,
        fontSize = 17.sp,
        lineHeight = 26.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(text) {
                detectTapGestures(
                    onLongPress = { offset ->
                        val layout = layoutResult ?: return@detectTapGestures
                        val pos = layout.getOffsetForPosition(offset)
                            .coerceIn(0, maxOf(0, text.length - 1))
                        val word = extractWordAt(text, pos)
                        if (word.isNotEmpty()) onLongPress(word, extractSentenceAt(text, pos))
                    }
                )
            },
        onTextLayout = { layoutResult = it }
    )
}

// ── Бокс перевода внизу экрана ────────────────────────────────

@Composable
private fun BottomTranslationBox(
    translation: TranslationState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = translation.visible,
        modifier = modifier,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit  = slideOutVertically(targetOffsetY  = { it }) + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A237E)),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                // Заголовок: слово + закрыть
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                translation.word,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                            if (translation.fromAi) {
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    color = Color(0xFFFF9500).copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        "✨ AI",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFAB40),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        when {
                            translation.isLoadingAi -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFFFFAB40)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        stringResource(R.string.lread_asking_ai),
                                        fontSize = 13.sp,
                                        color = Color(0xFFB0BEC5)
                                    )
                                }
                            }
                            translation.wordRu.isNotEmpty() && translation.wordRu != "—" -> {
                                Text(
                                    translation.wordRu,
                                    fontSize = 16.sp,
                                    color = Color(0xFFB0BEC5)
                                )
                            }
                            else -> {
                                Text(
                                    stringResource(R.string.lread_not_in_dict),
                                    fontSize = 14.sp,
                                    color = Color(0xFF78909C)
                                )
                            }
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Color(0xFF90A4AE))
                    }
                }

                // Слова предложения
                if (translation.sentenceWords.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFF283593))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.lread_words_in_sentence),
                        fontSize = 11.sp,
                        color = Color(0xFF78909C),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(translation.sentenceWords) { (es, ru) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF283593))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
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

// ── Режим «Читать вслух» ──────────────────────────────────────

@Composable
private fun ReadingAloudPanel(
    state: ReadState.ReadingAloud,
    stt: SpanishSpeechRecognizer,
    tts: TextToSpeech?,
    levelColor: Color,
    onUpdate: (ReadState.ReadingAloud) -> Unit,
    onDone: (List<SentenceResult>) -> Unit,
    onExit: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val ctx   = LocalContext.current
    val sentence = state.sentences.getOrNull(state.currentIdx) ?: return
    val result   = state.results.getOrNull(state.currentIdx)
    var sttError by remember(state.currentIdx) { mutableStateOf<String?>(null) }
    val errSilence = stringResource(R.string.lread_stt_silence)
    val errRecog = stringResource(R.string.lread_stt_error)
    val errNoMic = stringResource(R.string.lread_no_mic_perm)

    val infiniteTransition = rememberInfiniteTransition(label = "mic")
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.22f,
        animationSpec = infiniteRepeatable(tween(550), RepeatMode.Reverse),
        label = "micScale"
    )

    fun startListening() {
        if (state.isListening) return
        sttError = null
        onUpdate(state.copy(isListening = true, recognizedText = ""))
        scope.launch {
            when (val r = stt.listenOnce()) {
                is SpeechResult.Success -> {
                    val checks = compareToExpected(sentence, r.text)
                    val newResults = state.results.toMutableList()
                    if (state.currentIdx < newResults.size)
                        newResults[state.currentIdx] = SentenceResult(checks)
                    onUpdate(state.copy(
                        isListening = false, recognizedText = r.text, results = newResults
                    ))
                }
                is SpeechResult.Error -> {
                    sttError = if (r.isSilence) errSilence
                               else r.message.ifBlank { errRecog }
                    onUpdate(state.copy(isListening = false, recognizedText = ""))
                }
                SpeechResult.Cancelled -> onUpdate(state.copy(isListening = false))
            }
        }
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startListening()
        else sttError = errNoMic
    }

    fun checkPermAndListen() {
        if (ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.RECORD_AUDIO)
            == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            startListening()
        } else {
            permLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Прогресс ──
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                stringResource(R.string.lread_sentence_of, state.currentIdx + 1, state.sentences.size),
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onExit, contentPadding = PaddingValues(0.dp)) {
                Text(stringResource(R.string.lread_exit), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (state.currentIdx + 1f) / state.sentences.size },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = levelColor, trackColor = levelColor.copy(alpha = 0.15f)
        )

        Spacer(Modifier.height(12.dp))

        // ── Кнопка «послушать как звучит» ──
        OutlinedButton(
            onClick = {
                tts?.stop()
                tts?.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, "ra_sentence")
            },
            modifier = Modifier.height(38.dp),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, levelColor.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.VolumeUp, null, tint = levelColor, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.lread_listen), color = levelColor, fontSize = 13.sp)
        }

        Spacer(Modifier.height(10.dp))

        // ── Карточка с предложением ──
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                if (result != null) {
                    // Цветная подсветка слов
                    val annotated = buildAnnotatedString {
                        result.wordChecks.forEach { (word, correct) ->
                            withStyle(SpanStyle(
                                color = if (correct) Color(0xFF1B5E20) else Color(0xFFC62828),
                                background = if (correct) Color(0xFFE8F5E9) else Color(0xFFFCE4EC),
                                fontWeight = if (!correct) FontWeight.Bold else FontWeight.Normal
                            )) { append(word) }
                            append(" ")
                        }
                    }
                    val correct = result.wordChecks.count { it.second }
                    val total   = result.wordChecks.size
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            if (correct == total) stringResource(R.string.lread_excellent)
                            else stringResource(R.string.lread_words_correct, correct, total),
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = if (correct == total) Color(0xFF2E7D32) else Color(0xFFE65100)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(annotated, fontSize = 18.sp, lineHeight = 28.sp)
                } else {
                    Text(sentence, fontSize = 18.sp, lineHeight = 28.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // ── Что распознано ──
        if (state.recognizedText.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background).padding(10.dp)
            ) {
                Text(
                    stringResource(R.string.lread_you_said, state.recognizedText),
                    fontSize = 13.sp, color = Color(0xFF555555), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }

        // ── Ошибка STT (тишина / разрешение / сеть) ──
        if (sttError != null && result == null) {
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFF3E0)).padding(10.dp)
            ) {
                Text("⚠ $sttError", fontSize = 13.sp, color = Color(0xFFE65100))
            }
        }

        Spacer(Modifier.weight(1f))

        // ── Кнопка микрофона ──
        if (result == null) {
            Text(
                if (state.isListening) stringResource(R.string.lread_listening)
                else stringResource(R.string.lread_press_mic_hint),
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(90.dp)) {
                if (state.isListening) {
                    Box(
                        Modifier.scale(micScale).size(80.dp).clip(CircleShape)
                            .background(Color(0xFFC62828).copy(alpha = 0.18f))
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = if (state.isListening) Color(0xFFC62828) else levelColor,
                    shadowElevation = 6.dp,
                    modifier = Modifier.size(68.dp)
                        .clickable(enabled = !state.isListening) { checkPermAndListen() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (state.isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = stringResource(R.string.lread_microphone),
                            tint = Color.White, modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // ── Кнопки после результата ──
        if (result != null) {
            val isLast = state.currentIdx == state.sentences.size - 1
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (isLast) {
                        onDone(state.results.filterNotNull())
                    } else {
                        onUpdate(state.copy(currentIdx = state.currentIdx + 1, recognizedText = ""))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = levelColor)
            ) {
                Text(if (isLast) stringResource(R.string.lread_see_result) else stringResource(R.string.lread_next_sentence), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    val newResults = state.results.toMutableList()
                    if (state.currentIdx < newResults.size) newResults[state.currentIdx] = null
                    onUpdate(state.copy(results = newResults, recognizedText = ""))
                },
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text(stringResource(R.string.lread_repeat_sentence)) }
        }
    }
}

// ── Итог «Читать вслух» ───────────────────────────────────────

@Composable
private fun ReadingDonePanel(
    sentences: List<String>,
    results: List<SentenceResult>,
    levelColor: Color,
    onStartQuiz: () -> Unit,
    onBack: () -> Unit
) {
    val totalWords   = results.sumOf { it.wordChecks.size }
    val correctWords = results.sumOf { it.wordChecks.count { p -> p.second } }
    val pct = if (totalWords > 0) correctWords * 100 / totalWords else 0

    val emoji = when {
        pct >= 90 -> "🌟"; pct >= 75 -> "👏"; pct >= 55 -> "👍"; else -> "💪"
    }
    val comment = when {
        pct >= 90 -> stringResource(R.string.lread_pron_excellent)
        pct >= 75 -> stringResource(R.string.lread_pron_good)
        pct >= 55 -> stringResource(R.string.lread_pron_ok)
        else      -> stringResource(R.string.lread_pron_practice)
    }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Text(emoji, fontSize = 52.sp)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.lread_reading_result), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(comment, fontSize = 15.sp, color = Color(0xFF555555))
        Spacer(Modifier.height(16.dp))

        Text("$pct%", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = levelColor)
        Text(stringResource(R.string.lread_words_summary, correctWords, totalWords), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(20.dp))

        // Разбивка по предложениям
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(results) { i, r ->
                    val sc = r.wordChecks.count { it.second }
                    val st = r.wordChecks.size
                    val allOk = sc == st
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (allOk) Color(0xFFE8F5E9) else Color(0xFFFFF3E0))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (allOk) "✓" else "✗", fontSize = 16.sp,
                            color = if (allOk) Color(0xFF2E7D32) else Color(0xFFE65100),
                            modifier = Modifier.width(24.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                sentences.getOrElse(i) { "" },
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(stringResource(R.string.lread_words_short, sc, st), fontSize = 11.sp,
                                color = if (allOk) Color(0xFF2E7D32) else Color(0xFFE65100),
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onStartQuiz,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = levelColor)
        ) { Text(stringResource(R.string.lread_start_quiz), fontWeight = FontWeight.Bold) }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape = RoundedCornerShape(14.dp)
        ) { Text(stringResource(R.string.lread_reread)) }
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
    if (libro == null) { LaunchedEffect(Unit) { navController.popBackStack() }; return }

    val levelColor = mapOf(
        "A1" to Color(0xFF43A047), "A2" to Color(0xFF1E88E5),
        "B1" to Color(0xFFE65100), "B2" to Color(0xFF6A1B9A)
    )[libro.level] ?: LibroPurple

    var state: ReadState by remember { mutableStateOf(ReadState.Reading) }
    val translation by vm.translation.collectAsStateWithLifecycle()
    var highlightRange by remember { mutableStateOf<IntRange?>(null) }

    // TTS для «Слушать весь текст»
    val tts = rememberSpanishTts()
    var isSpeaking by remember { mutableStateOf(false) }
    LaunchedEffect(tts) {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) { isSpeaking = true }
            override fun onDone(id: String?)  { isSpeaking = false }
            override fun onError(id: String?) { isSpeaking = false }
        })
    }

    // Останавливаем TTS при выходе с экрана
    DisposableEffect(tts) {
        onDispose { tts?.stop() }
    }

    // STT для «Читать вслух»
    val context = LocalContext.current
    val stt = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext, LibroSpeechEntryPoint::class.java
        ).speechRecognizer()
    }

    // League promotion dialog
    var leaguePromotion by remember { mutableStateOf<LeaguePromotion?>(null) }
    LaunchedEffect(vm) {
        vm.leaguePromotions.collect { leaguePromotion = it }
    }
    leaguePromotion?.let { promo ->
        LeaguePromotionDialog(from = promo.from, to = promo.to, onDismiss = { leaguePromotion = null })
    }

    // Сбрасываем подсветку когда баннер закрывается
    if (!translation.visible) highlightRange = null

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
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.lread_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        when (val s = state) {

            // ── Режим чтения ─────────────────────────────────
            is ReadState.Reading -> {
                // Box: контент + бокс перевода снизу
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Контент без verticalScroll → long press работает
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .padding(top = 12.dp, bottom = 12.dp)
                    ) {
                        // Карточка с текстом (вес 1 — занимает всё свободное место)
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(3.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Column(
                                Modifier
                                    .fillMaxSize()
                                    .padding(20.dp)
                                    .verticalScroll(rememberScrollState()) // скролл только внутри карточки
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📖", fontSize = 22.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        stringResource(R.string.lread_long_press_hint),
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.height(14.dp))
                                StoryText(
                                    text = libro.text.trim(),
                                    highlightRange = highlightRange,
                                    onLongPress = { word, sentence ->
                                        // Найти позицию слова для подсветки
                                        val idx = libro.text.trim().indexOf(word)
                                        if (idx >= 0) highlightRange = idx until idx + word.length
                                        vm.lookupWord(word, sentence)
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Тема
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(levelColor.copy(alpha = 0.08f))
                                .padding(12.dp)
                        ) {
                            Text(stringResource(R.string.lread_topic_label), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = levelColor)
                            Text(libro.topic, fontSize = 13.sp, color = Color(0xFF555555))
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            stringResource(R.string.lread_intro_hint, libro.questions.size, LibrosData.PASS_CORRECT),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Spacer(Modifier.height(12.dp))

                        // ── Слушать весь текст + Читать вслух ──
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (isSpeaking) {
                                        tts?.stop(); isSpeaking = false
                                    } else {
                                        tts?.speak(
                                            libro.text.trim(),
                                            TextToSpeech.QUEUE_FLUSH, null, "libro_full"
                                        )
                                        isSpeaking = true
                                    }
                                },
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp, if (isSpeaking) Color(0xFFC62828) else levelColor
                                )
                            ) {
                                Icon(
                                    if (isSpeaking) Icons.Default.Stop else Icons.Default.Headphones,
                                    contentDescription = null,
                                    tint = if (isSpeaking) Color(0xFFC62828) else levelColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    if (isSpeaking) stringResource(R.string.lread_stop) else stringResource(R.string.lread_audiobook),
                                    color = if (isSpeaking) Color(0xFFC62828) else levelColor,
                                    fontSize = 14.sp
                                )
                            }

                            Button(
                                onClick = {
                                    tts?.stop(); isSpeaking = false
                                    vm.dismissTranslation()
                                    val sentences = splitSentences(libro.text.trim())
                                    state = ReadState.ReadingAloud(
                                        sentences = sentences,
                                        results   = List(sentences.size) { null }
                                    )
                                },
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = levelColor)
                            ) {
                                Icon(Icons.Default.Mic, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.lread_read_aloud), fontSize = 14.sp)
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Button(
                            onClick = {
                                tts?.stop(); isSpeaking = false
                                vm.dismissTranslation()
                                state = ReadState.Quiz(0, List(libro.questions.size) { null })
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = levelColor)
                        ) {
                            Text(stringResource(R.string.lread_start_quiz), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Бокс перевода — всплывает снизу поверх контента
                    BottomTranslationBox(
                        translation = translation,
                        onDismiss = { vm.dismissTranslation() },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }

            // ── Режим «Читать вслух» ─────────────────────────
            is ReadState.ReadingAloud -> {
                Box(Modifier.fillMaxSize().padding(padding)) {
                    ReadingAloudPanel(
                        state     = s,
                        stt       = stt,
                        tts       = tts,
                        levelColor = levelColor,
                        onUpdate  = { state = it },
                        onDone    = { results ->
                            tts?.stop()
                            state = ReadState.ReadingDone(s.sentences, results)
                        },
                        onExit    = { tts?.stop(); state = ReadState.Reading }
                    )
                }
            }

            // ── Итог «Читать вслух» ──────────────────────────
            is ReadState.ReadingDone -> {
                Box(Modifier.fillMaxSize().padding(padding)) {
                    ReadingDonePanel(
                        sentences = s.sentences,
                        results   = s.results,
                        levelColor = levelColor,
                        onStartQuiz = {
                            state = ReadState.Quiz(0, List(libro.questions.size) { null })
                        },
                        onBack = { state = ReadState.Reading }
                    )
                }
            }

            // ── Режим теста ──────────────────────────────────
            is ReadState.Quiz -> {
                val q = libro.questions[s.qIndex]
                val totalQ = libro.questions.size
                val selectedAnswer = s.answers[s.qIndex]

                Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
                    Text(
                        stringResource(R.string.lread_question_of, s.qIndex + 1, totalQ),
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

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text("❓", fontSize = 24.sp)
                            Spacer(Modifier.height(10.dp))
                            Text(q.question, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp)
                        }
                    }
                    Spacer(Modifier.height(20.dp))

                    val labels = listOf("A", "B", "C")
                    q.options.forEachIndexed { idx, option ->
                        val isSelected  = selectedAnswer == idx
                        val bgColor     = if (isSelected) levelColor else MaterialTheme.colorScheme.surface
                        val textColor   = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        val borderColor = if (isSelected) levelColor else MaterialTheme.colorScheme.outline
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
                                .background(bgColor)
                                .clickable {
                                    val a = s.answers.toMutableList(); a[s.qIndex] = idx
                                    state = s.copy(answers = a)
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(28.dp).clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color.White.copy(alpha = 0.25f) else levelColor.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) { Text(labels[idx], fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isSelected) Color.White else levelColor) }
                            Spacer(Modifier.width(12.dp))
                            Text(option, fontSize = 15.sp, color = textColor)
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (s.qIndex > 0) {
                            OutlinedButton(
                                onClick = { state = s.copy(qIndex = s.qIndex - 1) },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) { Text(stringResource(R.string.lread_quiz_back)) }
                        }
                        Button(
                            onClick = {
                                if (s.qIndex == totalQ - 1) {
                                    val correct = s.answers.zip(libro.questions).count { (ans, q) -> ans == q.correctIndex }
                                    vm.saveResult(libro.id, correct, totalQ)
                                    state = ReadState.Result(correct, totalQ)
                                } else {
                                    state = s.copy(qIndex = s.qIndex + 1)
                                }
                            },
                            enabled = selectedAnswer != null,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = levelColor)
                        ) { Text(if (s.qIndex == totalQ - 1) stringResource(R.string.lread_finish) else stringResource(R.string.lread_next), fontWeight = FontWeight.Bold) }
                    }
                }
            }

            // ── Результат ─────────────────────────────────────
            is ReadState.Result -> {
                val passed = s.correct >= LibrosData.PASS_CORRECT
                val pct    = s.correct * 100 / s.total
                Column(
                    Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(if (passed) "🎉" else "💪", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (passed) stringResource(R.string.lread_passed_title) else stringResource(R.string.lread_failed_title),
                        fontSize = 22.sp, fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = if (passed) LibroGreen else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.lread_correct_answers, s.correct, s.total, pct), fontSize = 16.sp, color = Color(0xFF555555))
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier.clip(RoundedCornerShape(12.dp))
                            .background(if (passed) LibroGreen.copy(alpha = 0.1f) else Color(0xFFFFF3E0))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            if (passed) stringResource(R.string.lread_passed_note)
                            else stringResource(R.string.lread_failed_note, LibrosData.PASS_CORRECT, s.total),
                            color = if (passed) LibroGreen else Color(0xFFE65100),
                            fontSize = 13.sp
                        )
                    }
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (passed) LibroGreen else levelColor)
                    ) { Text(stringResource(R.string.lread_back_to_list), fontWeight = FontWeight.Bold) }
                    if (!passed) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { state = ReadState.Reading },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text(stringResource(R.string.lread_reread_short)) }
                    }
                }
            }
        }
    }
}
