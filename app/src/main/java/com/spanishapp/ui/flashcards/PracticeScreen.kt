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

data class PracticeRound(
    val word: WordEntity,
    val options: List<String>,   // 4 Russian translations
    val correctIndex: Int
)

data class PracticeState(
    val isLoading: Boolean = true,
    val isFinished: Boolean = false,
    val rounds: List<PracticeRound> = emptyList(),
    val currentIndex: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val pickedIndex: Int? = null,    // user's last pick — locks UI for ~600ms before next
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
            val rounds = weak.map { word ->
                // Pick 3 distractors from the same level so the choice is plausible.
                val distractors = wordDao.randomDistractors(
                    level = word.level,
                    excludeId = word.id,
                    limit = 3
                )
                val all = (listOf(word) + distractors).map { it.russian }.shuffled()
                PracticeRound(
                    word = word,
                    options = all,
                    correctIndex = all.indexOf(word.russian)
                )
            }
            _state.value = PracticeState(
                isLoading = false,
                rounds = rounds
            )
        }
    }

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

    fun next() {
        val s = _state.value
        val nextIdx = s.currentIndex + 1
        val finished = nextIdx >= s.rounds.size
        _state.value = s.copy(
            currentIndex = nextIdx,
            pickedIndex = null,
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
                        RoundView(
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
                                // Auto-advance after a short pause so the user
                                // sees the feedback color.
                            },
                            onSpeak = { vm.speakCurrent() },
                            onNext = { vm.next() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundView(
    round: PracticeRound,
    picked: Int?,
    onPick: (Int) -> Unit,
    onSpeak: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.6f))

        // Spanish word + speaker
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
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (error != null) {
            Text("🌱", fontSize = 64.sp)
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
            val percent = if (total > 0) (correct * 100 / total) else 0
            Text(
                if (percent >= 80) "🏆" else if (percent >= 50) "👍" else "💪",
                fontSize = 72.sp
            )
            Spacer(Modifier.height(16.dp))
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
