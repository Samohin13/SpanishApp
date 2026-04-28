package com.spanishapp.ui.games

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.spanishapp.ui.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── State & ViewModel ─────────────────────────────────────────

data class AnagramState(
    val word: WordEntity? = null,
    val shuffledLetters: List<Char> = emptyList(),
    val usedIndices: List<Int> = emptyList(),
    val inputLetters: List<Char> = emptyList(),
    val isCorrect: Boolean? = null,
    val score: Int = 0,
    val totalAnswered: Int = 0,
    val hint: Boolean = false,
    val showExample: Boolean = false,
    val isFinished: Boolean = false,
    val isLoading: Boolean = true
) {
    val inputWord: String get() = inputLetters.joinToString("")
    val targetWord: String get() = word?.spanish ?: ""
}

@HiltViewModel
class AnagramGameViewModel @Inject constructor(
    private val wordDao: WordDao
) : ViewModel() {

    private val _state = MutableStateFlow(AnagramState())
    val state: StateFlow<AnagramState> = _state.asStateFlow()

    private var pool: List<WordEntity> = emptyList()
    private var poolIndex = 0

    init { loadPool() }

    private fun loadPool() = viewModelScope.launch {
        pool = wordDao.getRandomWords(300)
            .filter { w ->
                val s = w.spanish.trim()
                s.length in 3..9 && !s.contains(' ') && s.all { it.isLetter() || it == 'ñ' || it == 'ü' }
            }
            .shuffled()
            .take(12)
        poolIndex = 0
        showNext()
    }

    private fun showNext() {
        if (poolIndex >= pool.size) {
            _state.value = _state.value.copy(isFinished = true)
            return
        }
        val word = pool[poolIndex]
        val letters = word.spanish.lowercase().toMutableList()
        var shuffled = letters.shuffled()
        var attempts = 0
        while (shuffled.joinToString("") == word.spanish.lowercase() && attempts < 10) {
            shuffled = letters.shuffled()
            attempts++
        }

        _state.value = _state.value.copy(
            word            = word,
            shuffledLetters = shuffled,
            usedIndices     = emptyList(),
            inputLetters    = emptyList(),
            isCorrect       = null,
            hint            = false,
            showExample     = false,
            isLoading       = false
        )
    }

    fun tapLetter(index: Int) {
        val s = _state.value
        if (s.isCorrect != null) return
        if (index in s.usedIndices) return
        val letter = s.shuffledLetters[index]
        val newUsed  = s.usedIndices + index
        val newInput = s.inputLetters + letter
        _state.value = s.copy(usedIndices = newUsed, inputLetters = newInput)
        if (newInput.size == s.shuffledLetters.size) checkAnswer()
    }

    fun removeLast() {
        val s = _state.value
        if (s.isCorrect != null || s.usedIndices.isEmpty()) return
        _state.value = s.copy(
            usedIndices  = s.usedIndices.dropLast(1),
            inputLetters = s.inputLetters.dropLast(1)
        )
    }

    fun checkAnswer() = viewModelScope.launch {
        val s = _state.value
        if (s.inputLetters.size != s.shuffledLetters.size) return@launch
        val correct = s.inputWord.equals(s.targetWord, ignoreCase = true)
        _state.value = s.copy(
            isCorrect     = correct,
            score         = if (correct) s.score + 15 else s.score,
            totalAnswered = s.totalAnswered + 1
        )
        delay(1200)
        poolIndex++
        showNext()
    }

    fun showHint()    { _state.value = _state.value.copy(hint = true) }
    fun showExample() { _state.value = _state.value.copy(showExample = true) }

    fun skip() = viewModelScope.launch {
        val s = _state.value
        _state.value = s.copy(isCorrect = false, totalAnswered = s.totalAnswered + 1)
        delay(600)
        poolIndex++
        showNext()
    }

    fun restart() {
        poolIndex = 0
        _state.value = AnagramState()
        loadPool()
    }
}

// ── Screen ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnagramGameScreen(
    navController: NavHostController,
    vm: AnagramGameViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Анаграмма", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    if (!state.isFinished) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AppColors.Ochre.copy(alpha = 0.15f),
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Text(
                                "⭐ ${state.score}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppColors.Ochre
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading  -> CircularProgressIndicator(color = AppColors.Ochre)
                state.isFinished -> AnagramResult(state, vm::restart) { navController.popBackStack() }
                state.word != null -> AnagramQuestion(
                    state, vm::tapLetter, vm::removeLast,
                    vm::checkAnswer, vm::showHint, vm::showExample, vm::skip
                )
            }
        }
    }
}

// ── Вопрос ────────────────────────────────────────────────────

@Composable
private fun AnagramQuestion(
    state: AnagramState,
    onTap: (Int) -> Unit,
    onRemove: () -> Unit,
    onCheck: () -> Unit,
    onHint: () -> Unit,
    onExample: () -> Unit,
    onSkip: () -> Unit
) {
    val word = state.word ?: return

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Прогресс
        LinearProgressIndicator(
            progress  = { state.totalAnswered / 12f },
            modifier  = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color     = AppColors.Ochre,
            trackColor = AppColors.Ochre.copy(alpha = 0.1f)
        )
        Spacer(Modifier.height(24.dp))

        // Основная карточка с заданием
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            border = borderStroke(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "СОБЕРИ СЛОВО",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    letterSpacing = 1.2.sp
                )

                // Поле ввода
                val resultColor = when (state.isCorrect) {
                    true  -> AppColors.Teal
                    false -> MaterialTheme.colorScheme.error
                    null  -> MaterialTheme.colorScheme.onSurface
                }

                Text(
                    text = if (state.inputLetters.isEmpty()) "• • •" else state.inputWord.uppercase(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = resultColor,
                    textAlign = TextAlign.Center
                )

                if (state.isCorrect != null) {
                    Text(
                        if (state.isCorrect == true) "¡Excelente!" else word.spanish.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = resultColor
                    )
                }

                // Подсказки
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HintChip(
                        label = if (state.hint) word.russian else "ПЕРЕВОД",
                        icon = if (state.hint) null else Icons.Default.Check,
                        onClick = onHint,
                        isSelected = state.hint
                    )
                    if (word.example.isNotBlank()) {
                        HintChip(
                            label = if (state.showExample) word.example else "ПРИМЕР",
                            onClick = onExample,
                            isSelected = state.showExample,
                            color = AppColors.Teal
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Сетка букв
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val letters = state.shuffledLetters
            val rows = letters.chunked(if (letters.size > 6) (letters.size + 1) / 2 else letters.size)
            
            rows.forEachIndexed { rowIndex, rowLetters ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowLetters.forEachIndexed { colIndex, letter ->
                        val globalIdx = rowIndex * rows[0].size + colIndex
                        if (globalIdx < letters.size) {
                            LetterTile(letter, globalIdx, state, onTap)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Кнопки управления
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            IconButton(
                onClick = onRemove,
                enabled = state.inputLetters.isNotEmpty() && state.isCorrect == null,
                modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Default.Backspace, null)
            }
            
            Button(
                onClick = onCheck,
                enabled = state.inputLetters.size == state.shuffledLetters.size && state.isCorrect == null,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Ochre)
            ) {
                Text("ПРОВЕРИТЬ", fontWeight = FontWeight.ExtraBold)
            }

            IconButton(
                onClick = onSkip,
                enabled = state.isCorrect == null,
                modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            ) {
                Text("→", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HintChip(
    label: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    isSelected: Boolean,
    color: Color = AppColors.Ochre
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.1f) else Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun LetterTile(
    letter: Char,
    globalIndex: Int,
    state: AnagramState,
    onTap: (Int) -> Unit
) {
    val used = globalIndex in state.usedIndices
    val scale by animateFloatAsState(if (used) 0.9f else 1f, label = "tile")
    
    Surface(
        onClick = { onTap(globalIndex) },
        enabled = !used && state.isCorrect == null,
        shape   = RoundedCornerShape(16.dp),
        color   = if (used) Color.Transparent else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        modifier = Modifier
            .size(54.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .border(
                width = 1.dp,
                color = if (used) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f) 
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                letter.uppercaseChar().toString(),
                fontSize   = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = if (used) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f) 
                             else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AnagramResult(state: AnagramState, onRetry: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🏁", fontSize = 72.sp)
        Spacer(Modifier.height(16.dp))
        Text("Игра окончена", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Твой результат: ${state.score} очков",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Ochre)
        ) {
            Text("ПОПРОБОВАТЬ СНОВА", fontWeight = FontWeight.ExtraBold)
        }
        
        TextButton(onClick = onBack, modifier = Modifier.padding(top = 12.dp)) {
            Text("ВЫЙТИ В МЕНЮ", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun borderStroke() = androidx.compose.foundation.BorderStroke(
    1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
)
