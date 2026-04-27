package com.spanishapp.ui.games

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Helpers ───────────────────────────────────────────────────

/** Убирает артикль из начала испанского слова, если он есть */
fun stripArticle(spanish: String): String {
    val prefixes = listOf("el ", "la ", "un ", "una ", "los ", "las ", "unos ", "unas ")
    val s = spanish.trim()
    return prefixes.firstOrNull { s.startsWith(it, ignoreCase = true) }
        ?.let { s.substring(it.length) } ?: s
}

/** Возвращает "el" или "la" на основе окончания испанского слова (без артикля).
 *  Null — слово не подходит для игры. */
fun guessArticle(spanish: String): String? {
    val w = stripArticle(spanish).lowercase().trim()
    return when {
        w in listOf("día", "mapa", "idioma", "problema", "tema", "sistema",
                    "programa", "clima", "drama", "planeta", "poema") -> "el"
        w.endsWith("ión")  -> "la"
        w.endsWith("ción") -> "la"
        w.endsWith("sión") -> "la"
        w.endsWith("dad")  -> "la"
        w.endsWith("tad")  -> "la"
        w.endsWith("tud")  -> "la"
        w.endsWith("umbre")-> "la"
        w.endsWith("a") && !w.endsWith("ma") && !w.endsWith("pa") -> "la"
        w.endsWith("o")    -> "el"
        w.endsWith("or")   -> "el"
        w.endsWith("és")   -> "el"
        w.endsWith("án")   -> "el"
        w.endsWith("ón") && !w.endsWith("ción") && !w.endsWith("sión") -> "el"
        w.endsWith("aje")  -> "el"
        w.endsWith("al")   -> "el"
        w.endsWith("ar")   -> "el"
        else -> null
    }
}


// ── State & ViewModel ─────────────────────────────────────────

data class ArticlesState(
    val word: WordEntity? = null,
    val correctArticle: String = "",
    val selectedArticle: String? = null,
    val score: Int = 0,
    val streak: Int = 0,
    val totalAnswered: Int = 0,
    val isCorrect: Boolean? = null,
    val isFinished: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class ArticlesGameViewModel @Inject constructor(
    private val wordDao: WordDao
) : ViewModel() {

    private val _state = MutableStateFlow(ArticlesState())
    val state: StateFlow<ArticlesState> = _state.asStateFlow()

    private var pool: List<Pair<WordEntity, String>> = emptyList()
    private var poolIndex = 0

    init { loadPool() }

    private fun loadPool() = viewModelScope.launch {
        val words = wordDao.getRandomWords(300)
        pool = words
            .filter { it.wordType == "noun" || it.wordType == "sustantivo" }
            .mapNotNull { word ->
                val article = guessArticle(word.spanish)
                if (article != null) word to article else null
            }
            .shuffled()
            .take(20)

        if (pool.isEmpty()) {
            pool = words.mapNotNull { word ->
                val article = guessArticle(word.spanish)
                if (article != null) word to article else null
            }.shuffled().take(20)
        }

        poolIndex = 0
        showNext()
    }

    private fun showNext() {
        if (poolIndex >= pool.size) {
            _state.value = _state.value.copy(isFinished = true)
            return
        }
        val (word, article) = pool[poolIndex]
        _state.value = _state.value.copy(
            word = word,
            correctArticle = article,
            selectedArticle = null,
            isCorrect = null,
            isLoading = false
        )
    }

    fun select(article: String) = viewModelScope.launch {
        val s = _state.value
        if (s.selectedArticle != null || s.word == null) return@launch
        val correct = article == s.correctArticle
        _state.value = s.copy(
            selectedArticle = article,
            isCorrect = correct,
            score = if (correct) s.score + 10 else s.score,
            streak = if (correct) s.streak + 1 else 0,
            totalAnswered = s.totalAnswered + 1
        )
        delay(900)
        poolIndex++
        showNext()
    }

    fun restart() {
        pool = pool.shuffled()
        poolIndex = 0
        _state.value = ArticlesState()
        loadPool()
    }
}

// ── Screen ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesGameScreen(
    navController: NavHostController,
    vm: ArticlesGameViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Артикли 🏷️") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    if (!state.isFinished) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            if (state.streak >= 2) {
                                Text("🔥 ${state.streak}", style = MaterialTheme.typography.titleMedium)
                            }
                            Text(
                                "⭐ ${state.score}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Gold
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
                state.isLoading   -> CircularProgressIndicator(color = AppColors.Teal)
                state.isFinished  -> ArticlesResult(state, vm::restart) { navController.popBackStack() }
                state.word != null -> ArticlesQuestion(state, vm::select)
            }
        }
    }
}

// ── Вопрос ────────────────────────────────────────────────────

@Composable
private fun ArticlesQuestion(state: ArticlesState, onSelect: (String) -> Unit) {
    val word = state.word ?: return
    val cleanWord = stripArticle(word.spanish)   // убираем артикль из отображения


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Прогресс
        val total = 20
        LinearProgressIndicator(
            progress = { state.totalAnswered / total.toFloat() },
            modifier  = Modifier.fillMaxWidth().height(6.dp),
            color     = AppColors.Teal,
            trackColor = AppColors.Teal.copy(alpha = 0.15f)
        )
        Text(
            "${state.totalAnswered + 1} / $total",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.weight(0.2f))

        // Инструкция
        Text(
            "Выбери артикль:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Карточка слова с эмодзи-визуалом
        AnimatedContent(targetState = cleanWord, label = "word") { w ->
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = AppColors.Teal.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Слово БЕЗ артикля
                    Text(
                        "__ $w",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        word.russian,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.weight(0.3f))

        // Кнопки артиклей — 2×2 сетка
        val articles = listOf("el", "la", "un", "una")
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            articles.chunked(2).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    row.forEach { article ->
                        ArticleButton(
                            article  = article,
                            state    = state,
                            modifier = Modifier.weight(1f),
                            onClick  = { onSelect(article) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(0.2f))
    }
}

@Composable
private fun ArticleButton(
    article: String,
    state: ArticlesState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val answered = state.selectedArticle != null
    val isSelected = state.selectedArticle == article
    val isCorrect  = state.correctArticle == article

    val containerColor = when {
        !answered  -> MaterialTheme.colorScheme.surface
        isCorrect  -> AppColors.Teal.copy(alpha = 0.2f)
        isSelected -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        else       -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        !answered  -> MaterialTheme.colorScheme.outlineVariant
        isCorrect  -> AppColors.Teal
        isSelected -> MaterialTheme.colorScheme.error
        else       -> MaterialTheme.colorScheme.outlineVariant
    }
    val textColor = when {
        !answered  -> MaterialTheme.colorScheme.onSurface
        isCorrect  -> AppColors.Teal
        isSelected -> MaterialTheme.colorScheme.error
        else       -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    OutlinedButton(
        onClick  = onClick,
        enabled  = !answered,
        shape    = RoundedCornerShape(16.dp),
        colors   = ButtonDefaults.outlinedButtonColors(containerColor = containerColor),
        border   = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(borderColor)
        ),
        modifier = modifier.height(64.dp)
    ) {
        Text(
            article,
            fontSize   = 22.sp,
            fontWeight = FontWeight.Bold,
            color      = textColor
        )
    }
}

// ── Результат ─────────────────────────────────────────────────

@Composable
private fun ArticlesResult(
    state: ArticlesState,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    val correct = state.score / 10
    val total   = state.totalAnswered
    val pct     = if (total > 0) correct * 100 / total else 0

    val emoji = when {
        pct >= 90 -> "🏆"; pct >= 70 -> "🎉"; pct >= 50 -> "👍"; else -> "💪"
    }
    val message = when {
        pct >= 90 -> "Артикли освоены!"; pct >= 70 -> "Отличный результат!"
        pct >= 50 -> "Неплохо, продолжай!"; else -> "Ещё раз — и станет лучше!"
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 72.sp)
        Spacer(Modifier.height(16.dp))
        Text("$correct / $total", style = MaterialTheme.typography.displayMedium,
             fontWeight = FontWeight.Bold)
        Text("правильных ответов", style = MaterialTheme.typography.bodyMedium,
             color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.titleMedium,
             color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
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
