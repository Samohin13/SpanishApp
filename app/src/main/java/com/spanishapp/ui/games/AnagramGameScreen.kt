package com.spanishapp.ui.games

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val hint: Boolean = false,          // показать перевод
    val showExample: Boolean = false,   // показать пример-подсказку
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
                s.length in 3..9
                    && !s.contains(' ')
                    && s.all { it.isLetter() || it == 'ñ' || it == 'ü' }
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
        pool = pool.shuffled()
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
        topBar = {
            TopAppBar(
                title = { Text("Анаграмма 🔤") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    if (!state.isFinished) {
                        Text(
                            "⭐ ${state.score}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Gold,
                            modifier = Modifier.padding(end = 16.dp)
                        )
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
                state.isLoading  -> CircularProgressIndicator(color = AppColors.Gold)
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
    val emoji = categoryEmoji(word.category)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Прогресс
        LinearProgressIndicator(
            progress  = { state.totalAnswered / 12f },
            modifier  = Modifier.fillMaxWidth().height(5.dp),
            color     = AppColors.Gold,
            trackColor = AppColors.Gold.copy(alpha = 0.15f)
        )
        Text(
            "${state.totalAnswered + 1} / 12",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.weight(0.1f))

        Text(
            "Собери испанское слово:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Эмодзи категории как визуальная подсказка
        Text(emoji, fontSize = 52.sp)

        // Подсказки: перевод и пример
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Подсказка: перевод
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AppColors.Gold.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (state.hint) {
                        Text(
                            word.russian,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.GoldDark
                        )
                    } else {
                        Text("🔤", fontSize = 14.sp)
                        TextButton(onClick = onHint, contentPadding = PaddingValues(0.dp)) {
                            Text("Перевод", style = MaterialTheme.typography.labelSmall,
                                 color = AppColors.GoldDark)
                        }
                    }
                }
            }

            // Подсказка: пример предложения (если есть)
            if (word.example.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AppColors.Teal.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (state.showExample) {
                            Text(
                                "«${word.example}»",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.Teal
                            )
                        } else {
                            Text("💡", fontSize = 14.sp)
                            TextButton(onClick = onExample, contentPadding = PaddingValues(0.dp)) {
                                Text("Пример", style = MaterialTheme.typography.labelSmall,
                                     color = AppColors.Teal)
                            }
                        }
                    }
                }
            }
        }

        // Поле ввода — собранные буквы
        val resultColor = when (state.isCorrect) {
            true  -> AppColors.Teal
            false -> MaterialTheme.colorScheme.error
            null  -> MaterialTheme.colorScheme.outlineVariant
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = resultColor.copy(alpha = 0.07f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                val display = if (state.inputLetters.isEmpty()) "_ _ _"
                              else state.inputLetters.joinToString(" ")
                AnimatedContent(targetState = display, label = "input") { text ->
                    Text(
                        text,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = resultColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Статус ответа
        AnimatedVisibility(state.isCorrect != null) {
            Text(
                if (state.isCorrect == true) "✅ Правильно!" else "❌ ${word.spanish}",
                style = MaterialTheme.typography.titleMedium,
                color = if (state.isCorrect == true) AppColors.Teal else MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.weight(0.1f))

        // ── Буквы в два ряда по центру ────────────────────────
        val letters = state.shuffledLetters
        val rowSize = (letters.size + 1) / 2   // верхний ряд немного больше если нечётное
        val topRow    = letters.subList(0, rowSize)
        val bottomRow = letters.subList(rowSize, letters.size)
        val topIndices    = (0 until rowSize).toList()
        val bottomIndices = (rowSize until letters.size).toList()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Верхний ряд
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.wrapContentWidth()
            ) {
                topRow.forEachIndexed { localIdx, letter ->
                    val globalIdx = topIndices[localIdx]
                    LetterTile(letter, globalIdx, state, onTap)
                }
            }
            // Нижний ряд
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.wrapContentWidth()
            ) {
                bottomRow.forEachIndexed { localIdx, letter ->
                    val globalIdx = bottomIndices[localIdx]
                    LetterTile(letter, globalIdx, state, onTap)
                }
            }
        }

        Spacer(Modifier.weight(0.1f))

        // Кнопки управления
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick  = onRemove,
                enabled  = state.inputLetters.isNotEmpty() && state.isCorrect == null,
                shape    = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Icon(Icons.Default.Backspace, null, modifier = Modifier.size(20.dp))
            }
            Button(
                onClick  = onCheck,
                enabled  = state.inputLetters.isNotEmpty() && state.isCorrect == null,
                shape    = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(2f).height(50.dp)
            ) {
                Text("Проверить", style = MaterialTheme.typography.titleSmall)
            }
            OutlinedButton(
                onClick  = onSkip,
                enabled  = state.isCorrect == null,
                shape    = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Text("→", fontSize = 18.sp)
            }
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
    Surface(
        onClick = { onTap(globalIndex) },
        shape   = RoundedCornerShape(12.dp),
        color   = if (used) MaterialTheme.colorScheme.surfaceVariant
                  else AppColors.Gold.copy(alpha = 0.15f),
        enabled = !used && state.isCorrect == null,
        modifier = Modifier.size(52.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                letter.uppercaseChar().toString(),
                fontSize   = 24.sp,
                fontWeight = FontWeight.Bold,
                color      = if (used) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                             else AppColors.GoldDark
            )
        }
    }
}

// ── Результат ─────────────────────────────────────────────────

@Composable
private fun AnagramResult(state: AnagramState, onRetry: () -> Unit, onBack: () -> Unit) {
    val correct = state.score / 15
    val total   = state.totalAnswered
    val pct     = if (total > 0) correct * 100 / total else 0

    val emoji   = when { pct >= 80 -> "🏆"; pct >= 60 -> "🎉"; pct >= 40 -> "👍"; else -> "💪" }
    val message = when { pct >= 80 -> "Ты мастер слов!"; pct >= 60 -> "Отличный результат!"; pct >= 40 -> "Неплохо!"; else -> "Ещё немного тренировки!" }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 72.sp)
        Spacer(Modifier.height(16.dp))
        Text("$correct / $total", style = MaterialTheme.typography.displayMedium,
             fontWeight = FontWeight.Bold)
        Text("собрано верно", style = MaterialTheme.typography.bodyMedium,
             color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.titleMedium,
             color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Surface(shape = RoundedCornerShape(12.dp), color = AppColors.Gold.copy(alpha = 0.15f)) {
            Text("⭐ ${state.score} очков",
                 modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                 style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                 color = AppColors.GoldDark)
        }
        Spacer(Modifier.height(32.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().height(52.dp),
               shape = RoundedCornerShape(14.dp)) {
            Text("Ещё раз", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(52.dp),
                       shape = RoundedCornerShape(14.dp)) {
            Text("К играм")
        }
    }
}
