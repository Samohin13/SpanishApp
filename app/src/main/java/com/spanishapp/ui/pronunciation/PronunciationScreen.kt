package com.spanishapp.ui.pronunciation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import com.spanishapp.service.SpanishSpeechRecognizer
import com.spanishapp.service.SpanishTts
import com.spanishapp.service.SpeechResult
import com.spanishapp.ui.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Утилита сравнения произношения ────────────────────────────

/** Возвращает 0..100 — насколько похоже произношение на эталон. */
private fun pronunciationScore(spoken: String, target: String): Int {
    val s = spoken.lowercase().trim()
    val t = target.lowercase().trim()
        .replace("el ", "").replace("la ", "")
        .replace("un ", "").replace("una ", "")
    if (s == t) return 100
    // Простое расстояние Левенштейна
    val m = s.length
    val n = t.length
    val dp = Array(m + 1) { IntArray(n + 1) }
    for (i in 0..m) dp[i][0] = i
    for (j in 0..n) dp[0][j] = j
    for (i in 1..m) for (j in 1..n) {
        dp[i][j] = if (s[i-1] == t[j-1]) dp[i-1][j-1]
        else minOf(dp[i-1][j-1], dp[i-1][j], dp[i][j-1]) + 1
    }
    val maxLen = maxOf(m, n).coerceAtLeast(1)
    return ((1f - dp[m][n].toFloat() / maxLen) * 100).toInt().coerceIn(0, 100)
}

// ── State ─────────────────────────────────────────────────────

enum class PronunciationPhase {
    IDLE,       // ждём нажатия «Послушать»
    LISTENING,  // идёт запись речи
    RESULT      // показываем результат
}

data class PronunciationState(
    val word: WordEntity? = null,
    val phase: PronunciationPhase = PronunciationPhase.IDLE,
    val spokenText: String = "",
    val score: Int? = null,           // 0..100
    val errorMessage: String? = null,
    val totalPracticed: Int = 0,
    val isLoading: Boolean = true
)

// ── ViewModel ─────────────────────────────────────────────────

@HiltViewModel
class PronunciationViewModel @Inject constructor(
    private val wordDao: WordDao,
    private val tts: SpanishTts,
    private val stt: SpanishSpeechRecognizer
) : ViewModel() {

    private val _state = MutableStateFlow(PronunciationState())
    val state: StateFlow<PronunciationState> = _state.asStateFlow()

    private var wordPool: List<WordEntity> = emptyList()
    private var poolIndex = 0

    init { loadPool() }

    private fun loadPool() = viewModelScope.launch {
        val words = wordDao.getRandomWords(200)
            .filter { it.spanish.isNotBlank() }
            .shuffled()
        wordPool = words
        poolIndex = 0
        nextWord()
    }

    private fun nextWord() {
        if (poolIndex >= wordPool.size) poolIndex = 0
        val word = wordPool.getOrNull(poolIndex) ?: return
        _state.value = _state.value.copy(
            word         = word,
            phase        = PronunciationPhase.IDLE,
            spokenText   = "",
            score        = null,
            errorMessage = null,
            isLoading    = false
        )
    }

    /** Озвучить слово через TTS */
    fun playWord() = viewModelScope.launch {
        val word = _state.value.word ?: return@launch
        tts.speak(word.spanish, slow = false)
    }

    /** Озвучить медленно */
    fun playWordSlow() = viewModelScope.launch {
        val word = _state.value.word ?: return@launch
        tts.speak(word.spanish, slow = true)
    }

    /** Начать запись речи */
    fun startListening() = viewModelScope.launch {
        val word = _state.value.word ?: return@launch
        _state.value = _state.value.copy(
            phase = PronunciationPhase.LISTENING,
            errorMessage = null
        )
        when (val result = stt.listenOnce()) {
            is SpeechResult.Success -> {
                val score = pronunciationScore(result.text, word.spanish)
                _state.value = _state.value.copy(
                    phase      = PronunciationPhase.RESULT,
                    spokenText = result.text,
                    score      = score,
                    totalPracticed = _state.value.totalPracticed + 1
                )
            }
            is SpeechResult.Error -> {
                _state.value = _state.value.copy(
                    phase        = PronunciationPhase.IDLE,
                    errorMessage = result.message
                )
            }
            is SpeechResult.Cancelled -> {
                _state.value = _state.value.copy(phase = PronunciationPhase.IDLE)
            }
        }
    }

    fun nextWordAction() {
        poolIndex++
        nextWord()
    }

    fun tryAgain() {
        _state.value = _state.value.copy(
            phase      = PronunciationPhase.IDLE,
            spokenText = "",
            score      = null
        )
    }
}

// ── Screen ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PronunciationScreen(
    navController: NavHostController,
    vm: PronunciationViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Произношение 🎤") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    Text(
                        "Слов: ${state.totalPracticed}",
                        modifier = Modifier.padding(end = 16.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(color = AppColors.Terracotta)
                state.word != null -> PronunciationContent(state, vm)
            }
        }
    }
}

// ── Основной контент ──────────────────────────────────────────

@Composable
private fun PronunciationContent(
    state: PronunciationState,
    vm: PronunciationViewModel
) {
    val word = state.word ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.weight(0.1f))

        // Инструкция
        Text(
            when (state.phase) {
                PronunciationPhase.IDLE      -> "Нажми 🔊 чтобы услышать, затем 🎤 чтобы повторить"
                PronunciationPhase.LISTENING -> "Говори сейчас…"
                PronunciationPhase.RESULT    -> "Результат"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // Карточка слова
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = AppColors.Terracotta.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    word.spanish,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    word.russian,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                if (word.example.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "💬 ${word.example}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Кнопки TTS (послушать обычно + медленно)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { vm.playWord() },
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.VolumeUp, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Послушать")
            }
            OutlinedButton(
                onClick = { vm.playWordSlow() },
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.VolumeUp, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("Медленно 🐢")
            }
        }

        Spacer(Modifier.weight(0.15f))

        // Результат произношения
        AnimatedVisibility(visible = state.phase == PronunciationPhase.RESULT) {
            state.score?.let { score ->
                PronunciationResultBadge(
                    score      = score,
                    spokenText = state.spokenText,
                    target     = word.spanish
                )
            }
        }

        // Сообщение об ошибке
        state.errorMessage?.let { error ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
            ) {
                Text(
                    error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.weight(0.1f))

        // Кнопка микрофона (большая, пульсирует когда слушает)
        when (state.phase) {
            PronunciationPhase.RESULT -> {
                // После результата: «Ещё раз» и «Следующее»
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { vm.tryAgain() },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) { Text("Ещё раз") }
                    Button(
                        onClick = { vm.nextWordAction() },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) { Text("Следующее →") }
                }
            }
            else -> {
                MicButton(
                    isListening = state.phase == PronunciationPhase.LISTENING,
                    onClick     = { vm.startListening() }
                )
            }
        }

        Spacer(Modifier.weight(0.1f))
    }
}

// ── Кнопка микрофона ─────────────────────────────────────────

@Composable
private fun MicButton(isListening: Boolean, onClick: () -> Unit) {
    val pulse by rememberInfiniteTransition(label = "mic_pulse").animateFloat(
        initialValue = 1f,
        targetValue  = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val color = if (isListening) AppColors.Terracotta else MaterialTheme.colorScheme.primary

    Button(
        onClick  = onClick,
        enabled  = !isListening,
        shape    = CircleShape,
        colors   = ButtonDefaults.buttonColors(containerColor = color),
        modifier = Modifier
            .size(88.dp)
            .scale(if (isListening) pulse else 1f)
    ) {
        Icon(
            Icons.Default.Mic, null,
            modifier = Modifier.size(36.dp)
        )
    }
    Spacer(Modifier.height(8.dp))
    Text(
        if (isListening) "Слушаю…" else "Нажми и говори",
        style = MaterialTheme.typography.labelLarge,
        color = if (isListening) AppColors.Terracotta
        else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// ── Бейдж результата ─────────────────────────────────────────

@Composable
private fun PronunciationResultBadge(score: Int, spokenText: String, target: String) {
    val color = when {
        score >= 80 -> AppColors.Teal
        score >= 50 -> AppColors.Gold
        else        -> MaterialTheme.colorScheme.error
    }
    val emoji = when {
        score >= 90 -> "🌟"
        score >= 70 -> "👍"
        score >= 50 -> "🙂"
        else        -> "💪"
    }
    val msg = when {
        score >= 90 -> "Отлично! Почти идеально!"
        score >= 70 -> "Хорошо! Продолжай!"
        score >= 50 -> "Неплохо, ещё попробуй"
        else        -> "Послушай ещё раз и повтори"
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(emoji, fontSize = 28.sp)
                Text(
                    "$score%",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Text(msg, style = MaterialTheme.typography.bodyMedium,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (spokenText.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Ты сказал(а): \"$spokenText\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Эталон: \"$target\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
