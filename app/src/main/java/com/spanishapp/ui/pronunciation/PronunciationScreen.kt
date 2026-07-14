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
import androidx.compose.ui.res.stringResource
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
import com.spanishapp.ui.components.tappableForSpeak
import com.spanishapp.ui.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// ── Утилита сравнения произношения ────────────────────────────

/**
 * v1.25.98 FIX (audit pron-C1): нормализация ОБЕИХ сторон одинаково.
 * Раньше: (1) replace("la ") вырезал подстроку ГДЕ УГОДНО — «habla más»
 * превращалось в «habmás», 376 эталонов корректировались неправильно;
 * (2) артикль стригся только у эталона, а юзер его произносит (карточка
 * показывает «el gato») → «el gato» vs «gato» = 57% и незаслуженный fail;
 * (3) los/las не стриглись вовсе; (4) пунктуация «¡¿…» в эталоне, которую
 * STT никогда не вернёт, гарантированно штрафовала.
 */
private val LEADING_ARTICLE = Regex("^(el|la|los|las|un|una)\\s+")
private val PUNCTUATION = Regex("[¿?¡!.,;:…()\"«»]")

private fun normalizeForScore(raw: String): String =
    raw.lowercase().trim()
        .replace(PUNCTUATION, " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .replace(LEADING_ARTICLE, "")
        // v1.26.1: фолдим акценты — расставляет их STT, а не юзер («esta» vs
        // «está» звучит одинаково для распознавателя; штрафовать нечестно).
        .replace('á','a').replace('é','e').replace('í','i')
        .replace('ó','o').replace('ú','u').replace('ü','u')

/** Возвращает 0..100 — насколько похоже произношение на эталон. */
private fun pronunciationScore(spoken: String, target: String): Int {
    val s = normalizeForScore(spoken)
    val t = normalizeForScore(target)
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
    private val stt: SpanishSpeechRecognizer,
    private val xpTracker: com.spanishapp.service.XpTracker,
) : ViewModel() {

    private val _state = MutableStateFlow(PronunciationState())
    val state: StateFlow<PronunciationState> = _state.asStateFlow()

    private var wordPool: List<WordEntity> = emptyList()
    private var poolIndex = 0

    init { loadPool() }

    private fun loadPool() = viewModelScope.launch {
        // First-launch race: the database seeder is async, so the words table
        // can still be empty when this VM is constructed. Retry briefly so
        // the screen doesn't sit on a permanent spinner.
        var attempt = 0
        var words: List<WordEntity> = emptyList()
        while (attempt < 8 && words.isEmpty()) {
            words = wordDao.getRandomWords(200).filter { it.spanish.isNotBlank() }
            if (words.isNotEmpty()) break
            kotlinx.coroutines.delay(500L)
            attempt++
        }
        if (words.isEmpty()) {
            _state.value = _state.value.copy(
                isLoading = false,
                errorMessage = "Словарь ещё загружается. Попробуйте через минуту."
            )
            return@launch
        }
        wordPool = words.shuffled()
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
                // v1.26.1: скорим ВСЕ альтернативы распознавания (до 3) и берём
                // лучшую. Google часто ставит верный вариант вторым — раньше
                // оценивался только первый → незаслуженные низкие проценты
                // («плохо отрабатывает»).
                val candidates = (result.alternatives.map { it.first } + result.text).distinct()
                val best = candidates.maxByOrNull { pronunciationScore(it, word.spanish) } ?: result.text
                val score = pronunciationScore(best, word.spanish)
                _state.value = _state.value.copy(
                    phase      = PronunciationPhase.RESULT,
                    spokenText = best,
                    score      = score,
                    totalPracticed = _state.value.totalPracticed + 1
                )
                // v1.22.16: XP за успешную попытку. Раньше произношение не
                // давало XP вообще, при том что это ключевой моторный навык.
                if (score >= 70) {
                    xpTracker.add(
                        xp = com.spanishapp.domain.algorithm.XpSystem.PRONUNCIATION_GOOD,
                        words = 0,
                    )
                }
            }
            is SpeechResult.Error -> {
                // v1.25.98 FIX (audit pron-M4): раньше в баннер шли сырые
                // машинные токены «silence»/«no_match».
                _state.value = _state.value.copy(
                    phase        = PronunciationPhase.IDLE,
                    errorMessage = when {
                        result.isSilence -> "Не слышу. Нажми и говори громче."
                        result.message == "no_match" -> "Не разобрал. Попробуй ещё раз."
                        else -> result.message
                    }
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
    val state by vm.state.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    // RECORD_AUDIO permission flow. Without this the recognizer fails with
    // ERROR_INSUFFICIENT_PERMISSIONS and the screen shows a dead-end error
    // banner instead of prompting the user.
    val micPermLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) vm.startListening()
    }
    fun launchMic() {
        val granted = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (granted) vm.startListening()
        else micPermLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(com.spanishapp.R.string.pron_title), fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    Text(
                        stringResource(com.spanishapp.R.string.pron_word_count, state.totalPracticed),
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
                state.word != null -> PronunciationContent(state, vm, ::launchMic)
            }
        }
    }
}

// ── Основной контент ──────────────────────────────────────────

@Composable
private fun PronunciationContent(
    state: PronunciationState,
    vm: PronunciationViewModel,
    onStartListening: () -> Unit
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
                PronunciationPhase.IDLE      -> stringResource(com.spanishapp.R.string.pron_idle_hint)
                PronunciationPhase.LISTENING -> stringResource(com.spanishapp.R.string.pron_now_speak)
                PronunciationPhase.RESULT    -> stringResource(com.spanishapp.R.string.pron_phase_result)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // Карточка слова — тап в любую точку = озвучить
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = AppColors.Terracotta.copy(alpha = 0.08f),
            modifier = Modifier
                .fillMaxWidth()
                .tappableForSpeak { vm.playWord() }
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

        // Кнопки TTS (послушать обычно + медленно).
        // v1.26.1: full-width + 52dp + 16sp — были мелкие и нечитаемые.
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = { vm.playWord() },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Icon(Icons.Default.VolumeUp, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(com.spanishapp.R.string.pron_listen_btn), fontSize = 16.sp)
            }
            OutlinedButton(
                onClick = { vm.playWordSlow() },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Icon(Icons.Default.VolumeUp, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(com.spanishapp.R.string.pron_listen_slow), fontSize = 16.sp)
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
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) { Text(stringResource(com.spanishapp.R.string.pron_again), fontSize = 16.sp) }
                    Button(
                        onClick = { vm.nextWordAction() },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) { Text(stringResource(com.spanishapp.R.string.pron_next), fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
            else -> {
                MicButton(
                    isListening = state.phase == PronunciationPhase.LISTENING,
                    onClick     = onStartListening
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
        if (isListening) stringResource(com.spanishapp.R.string.pron_listening) else stringResource(com.spanishapp.R.string.pron_press_speak),
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
        score == 100 -> "🏆"
        score >= 90  -> "🌟"
        score >= 70  -> "👍"
        score >= 50  -> "🙂"
        else         -> "💪"
    }
    // v1.26.1 FIX: при 100% писало «Почти идеально» — противоречие. Теперь
    // 100 = «Идеально!», 90-99 = «Отлично! Почти идеально!».
    val msg = when {
        score == 100 -> stringResource(com.spanishapp.R.string.pron_score_perfect)
        score >= 90  -> stringResource(com.spanishapp.R.string.pron_score_excellent)
        score >= 70  -> stringResource(com.spanishapp.R.string.pron_score_good)
        score >= 50  -> stringResource(com.spanishapp.R.string.pron_score_ok)
        else         -> stringResource(com.spanishapp.R.string.pron_score_try_again)
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
            // v1.26.1: крупнее и контрастнее — bodySmall на onSurfaceVariant
            // было нечитаемо на тёмной теме.
            Text(
                msg,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (spokenText.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(com.spanishapp.R.string.pron_you_said, spokenText),
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    stringResource(com.spanishapp.R.string.pron_target, target),
                    fontSize = 15.sp,
                    color = color,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
