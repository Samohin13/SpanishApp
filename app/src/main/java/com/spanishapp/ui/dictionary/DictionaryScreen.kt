package com.spanishapp.ui.dictionary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.entity.WordEntity
import com.spanishapp.service.SpanishTts
import com.spanishapp.ui.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── ViewModel ─────────────────────────────────────────────────

data class DictionaryFilter(
    val query: String = "",
    val level: String = "all",   // all / A1 / A2 / B1 / B2
    val type: String  = "all"    // all / noun / verb / adjective / phrase
)

@HiltViewModel
class DictionaryViewModel @Inject constructor(
    private val wordDao: WordDao,
    private val tts: SpanishTts
) : ViewModel() {

    private val _filter = MutableStateFlow(DictionaryFilter())
    val filter: StateFlow<DictionaryFilter> = _filter.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val words: StateFlow<List<WordEntity>> = _filter
        .debounce(200)
        .flatMapLatest { f ->
            when {
                f.query.length >= 2 -> wordDao.search(f.query)
                f.level == "all" && f.type == "all" -> wordDao.getDueWords(limit = 500)
                f.level != "all" && f.type != "all" -> wordDao.getByLevelAndType(f.level, f.type)
                f.level != "all" -> wordDao.getNewWords(f.level, limit = 500)
                else -> wordDao.getByType(f.type, limit = 500)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(q: String) = _filter.update { it.copy(query = q) }
    fun setLevel(l: String) = _filter.update { it.copy(level = l) }
    fun setType(t: String)  = _filter.update { it.copy(type = t) }

    fun speak(word: WordEntity) = viewModelScope.launch { tts.speak(word.spanish) }
}

// ── Screen ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(
    navController: NavHostController,
    vm: DictionaryViewModel = hiltViewModel()
) {
    val filter by vm.filter.collectAsState()
    val words  by vm.words.collectAsState()

    val levels = listOf("all" to "Все", "A1" to "A1", "A2" to "A2", "B1" to "B1", "B2" to "B2")
    val types  = listOf("all" to "Все", "noun" to "Сущ.", "verb" to "Глагол",
                        "adjective" to "Прил.", "phrase" to "Фраза")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Словарь") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Search bar ───────────────────────────────────
            OutlinedTextField(
                value = filter.query,
                onValueChange = vm::setQuery,
                placeholder = { Text("Поиск на испанском или русском…") },
                leadingIcon  = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    AnimatedVisibility(filter.query.isNotEmpty()) {
                        IconButton(onClick = { vm.setQuery("") }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // ── Level chips ──────────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                items(levels) { (key, label) ->
                    FilterChip(
                        selected = filter.level == key,
                        onClick  = { vm.setLevel(key) },
                        label    = { Text(label) }
                    )
                }
            }

            // ── Type chips ───────────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(types) { (key, label) ->
                    FilterChip(
                        selected = filter.type == key,
                        onClick  = { vm.setType(key) },
                        label    = { Text(label) }
                    )
                }
            }

            // ── Count ────────────────────────────────────────
            Text(
                "${words.size} слов",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // ── Word list ────────────────────────────────────
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(words, key = { it.id }) { word ->
                    WordCard(word = word, onSpeak = { vm.speak(word) })
                }
            }
        }
    }
}

// ── Word card ─────────────────────────────────────────────────

@Composable
private fun WordCard(word: WordEntity, onSpeak: () -> Unit) {
    val accuracy = if (word.totalReviews > 0)
        (word.correctReviews * 100f / word.totalReviews).toInt() else -1

    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = if (word.isLearned) 0.dp else 1.dp,
        color = if (word.isLearned)
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
        else MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        word.spanish,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    LevelBadgeSmall(word.level)
                    if (word.isLearned) {
                        Text("✓", color = MaterialTheme.colorScheme.primary,
                             style = MaterialTheme.typography.bodySmall)
                    }
                }
                Text(
                    word.russian,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (word.example.isNotBlank()) {
                    Text(
                        word.example,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                if (accuracy >= 0) {
                    Spacer(Modifier.height(4.dp))
                    AccuracyBar(accuracy)
                }
            }

            IconButton(onClick = onSpeak) {
                Icon(
                    Icons.Default.VolumeUp,
                    null,
                    tint = AppColors.Terracotta,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun LevelBadgeSmall(level: String) {
    val color = when (level) {
        "A1" -> AppColors.Teal
        "A2" -> AppColors.Info
        "B1" -> AppColors.Gold
        "B2" -> AppColors.Terracotta
        else -> MaterialTheme.colorScheme.outline
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 5.dp, vertical = 1.dp)
    ) {
        Text(level, style = MaterialTheme.typography.labelSmall, color = color,
             fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AccuracyBar(accuracy: Int) {
    val color = when {
        accuracy >= 80 -> AppColors.Teal
        accuracy >= 60 -> AppColors.Gold
        else -> AppColors.Terracotta
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        LinearProgressIndicator(
            progress = { accuracy / 100f },
            modifier = Modifier
                .width(80.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
        Text(
            "$accuracy%",
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
