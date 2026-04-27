package com.spanishapp.ui.games

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.spanishapp.data.repository.SentenceItem
import com.spanishapp.data.repository.SentencesRepository
import com.spanishapp.ui.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── ViewModel ────────────────────────────────────────────────

data class ListeningState(
    val sentences: List<SentenceItem> = emptyList(),
    val currentIndex: Int = 0,
    val options: List<String> = emptyList(),
    val selected: String? = null,
    val score: Int = 0,
    val finished: Boolean = false
) {
    val current get() = sentences.getOrNull(currentIndex)
    val total get() = sentences.size
    val isCorrect get() = selected?.lowercase() == current?.cloze?.lowercase()
}

@HiltViewModel
class ListeningGameViewModel @Inject constructor(
    private val repo: SentencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ListeningState())
    val state: StateFlow<ListeningState> = _state

    init { startSession() }

    private fun startSession() {
        val sentences = repo.getSession(count = 10)
        _state.value = ListeningState(sentences = sentences)
        buildOptions()
    }

    private fun buildOptions() {
        val s = _state.value
        val current = s.current ?: return
        val wrong = repo.distractors(current.cloze, 3)
        val opts = (wrong + current.cloze).shuffled()
        _state.value = s.copy(options = opts, selected = null)
    }

    fun select(option: String) {
        val s = _state.value
        if (s.selected != null) return
        val correct = option.lowercase() == s.current?.cloze?.lowercase()
        _state.value = s.copy(
            selected = option,
            score = if (correct) s.score + 1 else s.score
        )
        viewModelScope.launch {
            delay(1400)
            next()
        }
    }

    private fun next() {
        val s = _state.value
        if (s.currentIndex + 1 >= s.total) {
            _state.value = s.copy(finished = true)
        } else {
            _state.value = s.copy(currentIndex = s.currentIndex + 1)
            buildOptions()
        }
    }

    fun restart() { startSession() }
}

// ── Screen ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListeningGameScreen(
    navController: NavHostController,
    vm: ListeningGameViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current

    if (state.finished) {
        FinishedScreen(state.score, state.total, onRestart = vm::restart, onBack = { navController.popBackStack() })
        return
    }

    val sentence = state.current ?: return

    // Auto-play audio when sentence changes
    LaunchedEffect(sentence.audio) {
        playAudio(ctx, sentence.audio)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Аудирование") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (state.currentIndex + 1f) / state.total },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = AppColors.Olive,
                trackColor = AppColors.Olive.copy(alpha = 0.15f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${state.currentIndex + 1} / ${state.total}   Очков: ${state.score}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            // Audio button
            FilledIconButton(
                onClick = { playAudio(ctx, sentence.audio) },
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = AppColors.Olive
                )
            ) {
                Icon(
                    Icons.Default.VolumeUp,
                    contentDescription = "Воспроизвести",
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
                )
            }

            Spacer(Modifier.height(8.dp))
            Text("Нажмите для повтора", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(28.dp))

            // Sentence with blank
            val displayEs = sentence.es.replace(
                Regex("(?i)\\b${Regex.escape(sentence.cloze)}\\b"),
                "___"
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        displayEs,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        sentence.en,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "Выберите пропущенное слово:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            // Options grid
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.options.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { opt ->
                            OptionButton(
                                text = opt,
                                selected = state.selected,
                                correctAnswer = sentence.cloze,
                                modifier = Modifier.weight(1f),
                                onClick = { vm.select(opt) }
                            )
                        }
                        // If odd number of options, fill remaining weight
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionButton(
    text: String,
    selected: String?,
    correctAnswer: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isSelected = selected == text
    val isCorrect = text.lowercase() == correctAnswer.lowercase()
    val bgColor = when {
        selected == null -> MaterialTheme.colorScheme.surface
        isCorrect -> Color(0xFF2E7D32)
        isSelected -> Color(0xFFC62828)
        else -> MaterialTheme.colorScheme.surface
    }
    val textColor = when {
        selected != null && (isCorrect || isSelected) -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    Button(
        onClick = onClick,
        enabled = selected == null,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            disabledContainerColor = bgColor,
            contentColor = textColor,
            disabledContentColor = textColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun FinishedScreen(score: Int, total: Int, onRestart: () -> Unit, onBack: () -> Unit) {
    val percent = (score * 100f / total).toInt()
    val emoji = when {
        percent >= 90 -> "🏆"
        percent >= 70 -> "🌟"
        percent >= 50 -> "👍"
        else -> "💪"
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text("Результат", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "$score из $total правильно ($percent%)",
            style = MaterialTheme.typography.titleLarge,
            color = AppColors.Olive
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
            Text("Ещё раз")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("В меню игр")
        }
    }
}

// ── Audio helper ─────────────────────────────────────────────

private var currentPlayer: MediaPlayer? = null

private fun playAudio(context: Context, filename: String) {
    if (filename.isBlank()) return
    try {
        currentPlayer?.release()
        val player = MediaPlayer()
        val afd = context.assets.openFd("sentences_audio/$filename")
        player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()
        player.prepare()
        player.start()
        player.setOnCompletionListener { it.release() }
        currentPlayer = player
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
