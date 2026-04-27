package com.spanishapp.ui.games

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.spanishapp.data.db.dao.ArticleGameDao
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.entity.ArticleLevelProgressEntity
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

fun stripArticle(spanish: String): String {
    val prefixes = listOf("el ", "la ", "un ", "una ", "los ", "las ", "unos ", "unas ")
    val s = spanish.trim()
    return prefixes.firstOrNull { s.startsWith(it, ignoreCase = true) }
        ?.let { s.substring(it.length) } ?: s
}

fun guessArticle(spanish: String): String? {
    val w = stripArticle(spanish).lowercase().trim()
    return when {
        w in listOf("día", "mapa", "idioma", "problema", "tema", "sistema", "programa", "clima", "drama", "planeta", "poema") -> "el"
        w.endsWith("ión") || w.endsWith("ción") || w.endsWith("sión") || w.endsWith("dad") || w.endsWith("tad") || w.endsWith("tud") || w.endsWith("umbre") -> "la"
        w.endsWith("a") && !w.endsWith("ma") && !w.endsWith("pa") -> "la"
        w.endsWith("o") || w.endsWith("or") || w.endsWith("és") || w.endsWith("án") || w.endsWith("aje") || w.endsWith("al") || w.endsWith("ar") -> "el"
        w.endsWith("ón") && !w.endsWith("ción") && !w.endsWith("sión") -> "el"
        else -> null
    }
}

data class ArticlesState(
    val levelId: Int = 1,
    val word: WordEntity? = null,
    val correctArticle: String = "",
    val selectedArticle: String? = null,
    val score: Int = 0,
    val correctCount: Int = 0,
    val totalAnswered: Int = 0,
    val isCorrect: Boolean? = null,
    val isFinished: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ArticlesGameViewModel @Inject constructor(
    private val wordDao: WordDao,
    private val gameDao: ArticleGameDao
) : ViewModel() {

    private val _state = MutableStateFlow(ArticlesState())
    val state: StateFlow<ArticlesState> = _state.asStateFlow()

    private var pool: List<Pair<WordEntity, String>> = emptyList()
    private var poolIndex = 0

    fun startLevel(id: Int) = viewModelScope.launch {
        _state.value = ArticlesState(levelId = id, isLoading = true)
        
        try {
            val limit = 10
            // Ищем существительные. Если база пуста или слов мало, берем любые.
            val allWords = wordDao.getRandomWords(500)
                .filter { it.wordType == "noun" || it.wordType == "sustantivo" || it.wordType == "general" }
                .mapNotNull { w -> 
                    val art = guessArticle(w.spanish)
                    if (art != null) w to art else null 
                }
            
            if (allWords.isEmpty()) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Не удалось загрузить слова. Проверьте базу данных."
                )
                return@launch
            }

            pool = allWords.shuffled().take(limit)
            poolIndex = 0
            showNext()
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, error = e.message)
        }
    }

    private fun showNext() {
        if (poolIndex >= pool.size) {
            if (pool.isEmpty()) {
                 _state.value = _state.value.copy(isFinished = true, isLoading = false)
            } else {
                 finishLevel()
            }
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
            score = if (correct) s.score + 15 else s.score,
            correctCount = if (correct) s.correctCount + 1 else s.correctCount,
            totalAnswered = s.totalAnswered + 1
        )
        delay(800)
        poolIndex++
        showNext()
    }

    private fun finishLevel() = viewModelScope.launch {
        val s = _state.value
        val pct = if (s.totalAnswered > 0) s.correctCount.toFloat() / s.totalAnswered else 0f
        val stars = when {
            pct >= 1.0f -> 3
            pct >= 0.66f -> 2
            pct >= 0.33f -> 1
            else -> 0
        }
        
        try {
            val current = gameDao.getProgress(s.levelId) ?: ArticleLevelProgressEntity(s.levelId)
            gameDao.upsertProgress(current.copy(
                stars = maxOf(current.stars, stars),
                isUnlocked = true,
                bestScore = maxOf(current.bestScore, s.score)
            ))
            
            if (stars >= 1 && s.levelId < 100) {
                gameDao.unlockLevel(s.levelId + 1)
            }
        } catch (e: Exception) {
            // Ignore DB errors for now to allow finish
        }
        
        _state.value = s.copy(isFinished = true)
    }

    fun restart() {
        startLevel(_state.value.levelId)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesGameScreen(
    navController: NavHostController,
    levelId: Int,
    vm: ArticlesGameViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(levelId) {
        vm.startLevel(levelId)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Уровень ${state.levelId}", fontWeight = FontWeight.ExtraBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
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
                state.isLoading -> CircularProgressIndicator(color = AppColors.Teal)
                state.error != null -> ErrorBody(state.error) { vm.startLevel(levelId) }
                state.isFinished  -> ArticlesResult(state, vm::restart) { navController.popBackStack() }
                state.word != null -> ArticlesQuestion(state, vm::select)
                else -> Text("Нет данных для уровня")
            }
        }
    }
}

@Composable
private fun ErrorBody(message: String?, onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Ошибка: ${message ?: "Неизвестно"}", color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, null)
            Spacer(Modifier.width(8.dp))
            Text("Попробовать снова")
        }
    }
}

@Composable
private fun ArticlesQuestion(state: ArticlesState, onSelect: (String) -> Unit) {
    val word = state.word ?: return
    val cleanWord = stripArticle(word.spanish)

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = { state.totalAnswered / 10f },
            modifier  = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
            color     = AppColors.Teal,
            trackColor = AppColors.Teal.copy(alpha = 0.1f)
        )
        Spacer(Modifier.height(32.dp))

        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            shadowElevation = 4.dp,
            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Карточка слова
                val wordEmoji = remember(word.wordType) {
                    when (word.wordType) {
                        "verb" -> "🔤"
                        "adjective" -> "🎨"
                        "phrase" -> "💬"
                        else -> "📖"
                    }
                }
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(
                                    AppColors.Teal.copy(alpha = 0.25f),
                                    AppColors.Teal.copy(alpha = 0.08f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(wordEmoji, fontSize = 80.sp)
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "¿... $cleanWord?",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    word.russian,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ArticleButton(label = "el", state = state, modifier = Modifier.weight(1f)) { onSelect("el") }
            ArticleButton(label = "la", state = state, modifier = Modifier.weight(1f)) { onSelect("la") }
        }
    }
}

@Composable
private fun ArticleButton(label: String, state: ArticlesState, modifier: Modifier, onClick: () -> Unit) {
    val answered = state.selectedArticle != null
    val isSelected = state.selectedArticle == label
    val isCorrect = state.correctArticle == label
    val scale by animateFloatAsState(if (isSelected) 1.05f else 1f, label = "scale")

    val bgColor = when {
        !answered -> MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        isCorrect -> AppColors.Teal.copy(alpha = 0.3f)
        isSelected -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
    }

    Surface(
        onClick = onClick,
        enabled = !answered,
        modifier = modifier.height(84.dp).graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(24.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected || (answered && isCorrect)) 3.dp else 1.dp, 
            if (isCorrect && answered) AppColors.Teal 
            else if (isSelected) MaterialTheme.colorScheme.error 
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        shadowElevation = if (isSelected) 8.dp else 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label.uppercase(), 
                style = MaterialTheme.typography.headlineMedium, 
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
private fun ArticlesResult(state: ArticlesState, onRetry: () -> Unit, onBack: () -> Unit) {
    val pct = state.correctCount.toFloat() / 10f
    val stars = when { pct >= 1f -> 3; pct >= 0.66f -> 2; pct >= 0.33f -> 1; else -> 0 }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) { i ->
                Icon(
                    Icons.Default.Star, null,
                    modifier = Modifier.size(72.dp),
                    tint = if (i < stars) Color(0xFFFFD700) else Color.LightGray.copy(alpha = 0.4f)
                )
            }
        }
        Spacer(Modifier.height(32.dp))
        Text("Уровень ${state.levelId} пройден!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text("Верных ответов: ${state.correctCount} из 10", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(Modifier.height(56.dp))
        Button(
            onClick = onRetry, 
            modifier = Modifier.fillMaxWidth().height(64.dp), 
            shape = RoundedCornerShape(20.dp), 
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Teal)
        ) {
            Text("ПОВТОРИТЬ УРОВЕНЬ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = onBack, 
            modifier = Modifier.fillMaxWidth().height(64.dp), 
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, AppColors.Teal)
        ) {
            Text("КАРТА УРОВНЕЙ", color = AppColors.Teal, fontWeight = FontWeight.Bold)
        }
    }
}
