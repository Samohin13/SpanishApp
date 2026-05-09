package com.spanishapp.ui.flashcards

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.spanishapp.domain.algorithm.SM2
import com.spanishapp.service.SpanishTts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── State ──────────────────────────────────────────────────────

/** Three practice formats — randomized across the session for variety. */
enum class PracticeMode {
    MULTIPLE_CHOICE,   // ES word → pick the right RU translation
    TYPING,            // RU word → assemble Spanish from scrambled letters
    LISTENING          // audio (TTS) → pick the right RU translation
}

data class PracticeRound(
    val word: WordEntity,
    val mode: PracticeMode,
    val options: List<String>,    // 4 RU options for MC/LISTENING (empty for TYPING)
    val correctIndex: Int,        // index in options (for MC/LISTENING)
    val scrambledLetters: List<Char> // for TYPING
)

data class PracticeState(
    val isLoading: Boolean = true,
    val isFinished: Boolean = false,
    val rounds: List<PracticeRound> = emptyList(),
    val currentIndex: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val pickedIndex: Int? = null,        // last MC/LISTENING pick
    val typedAnswer: String = "",        // current typing buffer
    val typingChecked: Boolean = false,  // user pressed Enter — show feedback
    val typingCorrect: Boolean = false,
    val error: String? = null
)

// ── ViewModel ──────────────────────────────────────────────────

@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val wordDao: WordDao,
    private val tts: SpanishTts
) : ViewModel() {

    private val _state = MutableStateFlow(PracticeState())
    val state: StateFlow<PracticeState> = _state.asStateFlow()

    init { loadSession() }

    private fun loadSession() {
        viewModelScope.launch {
            val weak = wordDao.getAllWeak(limit = 20)
            if (weak.isEmpty()) {
                _state.value = PracticeState(
                    isLoading = false,
                    isFinished = true,
                    error = "Слабых слов пока нет — занимайся карточками, и они появятся здесь автоматически."
                )
                return@launch
            }
            // Mix the three modes so the session feels varied. Cycle pattern:
            // MC → TYPING → LISTENING → MC → TYPING → ... (deterministic, all
            // weak words seen in every mode they're suitable for).
            val modes = arrayOf(
                PracticeMode.MULTIPLE_CHOICE,
                PracticeMode.TYPING,
                PracticeMode.LISTENING
            )
            val rounds = weak.mapIndexed { idx, word ->
                val mode = modes[idx % modes.size]
                val distractors = wordDao.randomDistractors(
                    level = word.level,
                    excludeId = word.id,
                    limit = 3
                )
                val ruOptions = (listOf(word) + distractors).map { it.russian }.shuffled()
                // Strip articles for typing mode so user types just "casa" not "la casa".
                // TTS still reads the full form for LISTENING.
                val target = stripArticle(word.spanish).lowercase()
                val scrambled = target.toList().shuffled()
                PracticeRound(
                    word = word,
                    mode = mode,
                    options = if (mode == PracticeMode.TYPING) emptyList() else ruOptions,
                    correctIndex = if (mode == PracticeMode.TYPING) -1
                                   else ruOptions.indexOf(word.russian),
                    scrambledLetters = scrambled
                )
            }.shuffled()
            _state.value = PracticeState(
                isLoading = false,
                rounds = rounds
            )
        }
    }

    private fun stripArticle(spanish: String): String =
        spanish.trim().replace(
            Regex("^(el|la|los|las|un|una)\\s+", RegexOption.IGNORE_CASE), ""
        )

    fun speakCurrent() {
        val s = _state.value
        val word = s.rounds.getOrNull(s.currentIndex)?.word ?: return
        tts.speak(word.spanish, slow = false)
    }

    fun pick(index: Int) {
        val s = _state.value
        if (s.pickedIndex != null) return  // locked
        val round = s.rounds.getOrNull(s.currentIndex) ?: return
        val correct = index == round.correctIndex

        viewModelScope.launch {
            // SM-2 update: correct = quality 4 (good), wrong = quality 1 (forgotten).
            val quality = if (correct) 4 else 1
            val updated = SM2.review(round.word, quality)
            wordDao.update(updated)
        }

        _state.value = s.copy(
            pickedIndex = index,
            correctCount = s.correctCount + if (correct) 1 else 0,
            wrongCount = s.wrongCount + if (!correct) 1 else 0
        )
    }

    /** TYPING mode: append a letter to the buffer. */
    fun typeLetter(c: Char) {
        val s = _state.value
        if (s.typingChecked) return
        _state.value = s.copy(typedAnswer = s.typedAnswer + c)
    }

    /** TYPING mode: pop last letter. */
    fun typeBackspace() {
        val s = _state.value
        if (s.typingChecked || s.typedAnswer.isEmpty()) return
        _state.value = s.copy(typedAnswer = s.typedAnswer.dropLast(1))
    }

    /** TYPING mode: clear buffer. */
    fun typeClear() {
        val s = _state.value
        if (s.typingChecked) return
        _state.value = s.copy(typedAnswer = "")
    }

    /** TYPING mode: submit answer for verification. */
    fun typeCheck() {
        val s = _state.value
        if (s.typingChecked) return
        val round = s.rounds.getOrNull(s.currentIndex) ?: return
        if (s.typedAnswer.isBlank()) return
        val target = stripArticle(round.word.spanish).lowercase().trim()
        val typed = s.typedAnswer.lowercase().trim()
        val correct = normalizeSpanish(typed) == normalizeSpanish(target)

        viewModelScope.launch {
            val quality = if (correct) 4 else 1
            wordDao.update(SM2.review(round.word, quality))
        }
        _state.value = s.copy(
            typingChecked = true,
            typingCorrect = correct,
            correctCount = s.correctCount + if (correct) 1 else 0,
            wrongCount = s.wrongCount + if (!correct) 1 else 0
        )
    }

    /** Loose comparison: ignore accents so "papa" == "papá". */
    private fun normalizeSpanish(s: String): String =
        s.replace('á', 'a').replace('é', 'e').replace('í', 'i')
         .replace('ó', 'o').replace('ú', 'u').replace('ü', 'u')

    fun next() {
        val s = _state.value
        val nextIdx = s.currentIndex + 1
        val finished = nextIdx >= s.rounds.size
        _state.value = s.copy(
            currentIndex = nextIdx,
            pickedIndex = null,
            typedAnswer = "",
            typingChecked = false,
            typingCorrect = false,
            isFinished = finished
        )
    }

    fun restart() {
        _state.value = PracticeState()
        loadSession()
    }
}

// ── Screen ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    navController: NavHostController,
    vm: PracticeViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val haptic = com.spanishapp.ui.components.rememberCheckedHaptic()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Практика", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (!state.isLoading && !state.isFinished && state.rounds.isNotEmpty()) {
                            Text(
                                "${state.currentIndex + 1} / ${state.rounds.size}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
                state.isFinished -> FinishedView(
                    correct = state.correctCount,
                    total = state.rounds.size,
                    error = state.error,
                    onRestart = { vm.restart() },
                    onExit = { navController.popBackStack() }
                )
                else -> {
                    val round = state.rounds.getOrNull(state.currentIndex)
                    if (round != null) {
                        when (round.mode) {
                            PracticeMode.MULTIPLE_CHOICE,
                            PracticeMode.LISTENING -> ChoiceRoundView(
                                round = round,
                                picked = state.pickedIndex,
                                onPick = { idx ->
                                    haptic.performHapticFeedback(
                                        if (idx == round.correctIndex)
                                            androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress
                                        else
                                            androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove
                                    )
                                    vm.pick(idx)
                                },
                                onSpeak = { vm.speakCurrent() },
                                onNext = { vm.next() }
                            )
                            PracticeMode.TYPING -> TypingRoundView(
                                round = round,
                                typed = state.typedAnswer,
                                checked = state.typingChecked,
                                isCorrect = state.typingCorrect,
                                onLetter = { vm.typeLetter(it) },
                                onBackspace = { vm.typeBackspace() },
                                onClear = { vm.typeClear() },
                                onCheck = {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    vm.typeCheck()
                                },
                                onNext = { vm.next() },
                                onSpeak = { vm.speakCurrent() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChoiceRoundView(
    round: PracticeRound,
    picked: Int?,
    onPick: (Int) -> Unit,
    onSpeak: () -> Unit,
    onNext: () -> Unit
) {
    val isListening = round.mode == PracticeMode.LISTENING
    // Auto-play TTS once when a LISTENING round opens.
    LaunchedEffect(round.word.id, isListening) {
        if (isListening) onSpeak()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.6f))

        if (isListening) {
            // LISTENING — hide the word, show only a big play button.
            Text(
                "🎧 Послушай и выбери перевод",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
            IconButton(
                onClick = onSpeak,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    Icons.Default.VolumeUp, null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            // Only reveal the actual word AFTER the user picks.
            if (picked != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    round.word.spanish,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            // MULTIPLE_CHOICE — show the Spanish word + speaker for optional play.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    round.word.spanish,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.width(10.dp))
                IconButton(onClick = onSpeak, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Default.VolumeUp, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        if (round.word.example.isNotBlank() && picked != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                "💬 ${round.word.example}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        // Options
        round.options.forEachIndexed { i, text ->
            OptionTile(
                text = text,
                isCorrect = i == round.correctIndex,
                isPicked = i == picked,
                pickedAny = picked != null,
                onClick = { onPick(i) }
            )
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.weight(0.4f))

        // Next button (only after picking)
        if (picked != null) {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    if (picked == round.correctIndex) "Верно — далее →" else "Запомню — далее →",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OptionTile(
    text: String,
    isCorrect: Boolean,
    isPicked: Boolean,
    pickedAny: Boolean,
    onClick: () -> Unit
) {
    val (bg, fg, border) = when {
        !pickedAny       -> Triple(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        isCorrect        -> Triple(Color(0xFF1B5E20), Color.White, Color(0xFF4CAF50))
        isPicked         -> Triple(Color(0xFF8B0000), Color.White, Color(0xFFFF5252))
        else             -> Triple(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    }

    Surface(
        onClick = onClick,
        enabled = !pickedAny,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = fg,
                modifier = Modifier.weight(1f)
            )
            if (pickedAny && isCorrect) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
            } else if (pickedAny && isPicked) {
                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun FinishedView(
    correct: Int,
    total: Int,
    error: String?,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    val composition by com.airbnb.lottie.compose.rememberLottieComposition(
        com.airbnb.lottie.compose.LottieCompositionSpec.RawRes(com.spanishapp.R.raw.lottie_victory)
    )
    val lottieProgress by com.airbnb.lottie.compose.animateLottieCompositionAsState(composition)

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (error != null) {
            // Empty state — no trophy. Plain Material icon for the leaf.
            Icon(
                Icons.Default.Refresh,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                error,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(32.dp))
            Button(onClick = onExit, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("Назад", fontWeight = FontWeight.Bold)
            }
        } else {
            // Lottie trophy — same animation used at the end of flashcard sessions
            // for visual consistency across the app's reward screens.
            Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                com.airbnb.lottie.compose.LottieAnimation(
                    composition = composition,
                    progress = { lottieProgress },
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "$correct / $total",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "правильных ответов",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Ещё раз", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onExit) {
                Text("Назад", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── Typing mode (assemble Spanish from scrambled letters) ──────

@Composable
private fun TypingRoundView(
    round: PracticeRound,
    typed: String,
    checked: Boolean,
    isCorrect: Boolean,
    onLetter: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onCheck: () -> Unit,
    onNext: () -> Unit,
    onSpeak: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))

        Text(
            "✏️ Собери испанское слово из букв",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        Text(
            round.word.russian,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(0.5f))

        // Typed buffer with feedback color after check.
        val bufferColor = when {
            !checked  -> MaterialTheme.colorScheme.surface
            isCorrect -> Color(0xFF1B5E20)
            else      -> Color(0xFF8B0000)
        }
        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
            shape = RoundedCornerShape(14.dp),
            color = bufferColor,
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    typed.ifEmpty { "—" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (checked || typed.isNotEmpty()) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // After check: show correct answer if wrong + speaker.
        if (checked && !isCorrect) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Правильно: ${round.word.spanish}",
                    fontSize = 15.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onSpeak, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.VolumeUp, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(Modifier.weight(0.5f))

        // Letter keys grid — 5 per row.
        if (!checked) {
            val rows = round.scrambledLetters.chunked(5)
            rows.forEach { rowChars ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                ) {
                    rowChars.forEach { c ->
                        LetterKey(c) { onLetter(c) }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Action row: Clear / Backspace / Check
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClear,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Очистить") }
                OutlinedButton(
                    onClick = onBackspace,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("⌫") }
                Button(
                    onClick = onCheck,
                    enabled = typed.isNotBlank(),
                    modifier = Modifier.weight(1.4f).height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Проверить", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    if (isCorrect) "Верно — далее →" else "Запомню — далее →",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LetterKey(c: Char, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(width = 52.dp, height = 56.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        shadowElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                c.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
