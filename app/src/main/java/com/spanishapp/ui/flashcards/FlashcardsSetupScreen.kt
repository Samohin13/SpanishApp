package com.spanishapp.ui.flashcards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.spanishapp.data.db.dao.WordDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private val LEVELS = listOf("A1", "A2", "B1", "B2")
private const val UNLOCK_THRESHOLD = 0.8f

data class LevelInfo(
    val key: String,
    val unlocked: Boolean,
    val masteredRatio: Float, // 0..1
    val masteredCount: Int,
    val totalCount: Int
)

private val DIRECTIONS = listOf(
    "ES → RU" to FlashcardDirection.ES_TO_RU,
    "RU → ES" to FlashcardDirection.RU_TO_ES,
    "Смешанный" to FlashcardDirection.MIXED
)

@HiltViewModel
class FlashcardsSetupViewModel @Inject constructor(
    private val wordDao: WordDao
) : ViewModel() {
    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _levels = MutableStateFlow<List<LevelInfo>>(emptyList())
    val levels: StateFlow<List<LevelInfo>> = _levels.asStateFlow()

    fun loadCategories(level: String) {
        viewModelScope.launch {
            _categories.value = wordDao.categoriesForLevel(level)
        }
    }

    fun loadLevels() {
        viewModelScope.launch {
            val result = mutableListOf<LevelInfo>()
            var prevMastered = true
            for (lvl in LEVELS) {
                val total = wordDao.countByLevel(lvl)
                val mastered = if (total > 0) wordDao.countMasteredByLevel(lvl) else 0
                val ratio = if (total > 0) mastered.toFloat() / total else 0f
                result += LevelInfo(
                    key = lvl,
                    unlocked = prevMastered,
                    masteredRatio = ratio,
                    masteredCount = mastered,
                    totalCount = total
                )
                prevMastered = ratio >= UNLOCK_THRESHOLD
            }
            _levels.value = result
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsSetupScreen(
    navController: NavHostController,
    viewModel: FlashcardsSetupViewModel = hiltViewModel()
) {
    var level by remember { mutableStateOf("A1") }
    var category by remember { mutableStateOf("all") }
    var direction by remember { mutableStateOf(FlashcardDirection.ES_TO_RU) }
    var onlyWeak by remember { mutableStateOf(false) }

    val categories by viewModel.categories.collectAsState()
    val levels by viewModel.levels.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.loadLevels() }

    LaunchedEffect(level) {
        category = "all"
        viewModel.loadCategories(level)
    }

    val selectedLevelInfo = levels.firstOrNull { it.key == level }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Карточки") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            SectionTitle("Уровень")
            LevelRow(
                levels = levels,
                selected = level,
                onSelect = { info ->
                    if (info.unlocked) {
                        level = info.key
                    } else {
                        val prev = LEVELS.getOrNull(LEVELS.indexOf(info.key) - 1) ?: "A1"
                        val pct = ((levels.firstOrNull { it.key == prev }?.masteredRatio ?: 0f) * 100).toInt()
                        scope.launch {
                            snackbarHost.showSnackbar(
                                "Открой все слова $prev, чтобы разблокировать ${info.key} (прогресс $pct%)"
                            )
                        }
                    }
                }
            )
            selectedLevelInfo?.let { LevelProgressBar(it) }

            SectionTitle("Категория")
            CategoryCarousel(
                options = listOf("all") + categories,
                selected = category,
                onSelect = { category = it }
            )

            SectionTitle("Направление перевода")
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DIRECTIONS.forEach { (label, value) ->
                    DirectionOption(
                        label = label,
                        selected = direction == value,
                        onClick = { direction = value }
                    )
                }
            }

            Surface(
                tonalElevation = 1.dp,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Только слабые слова",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(checked = onlyWeak, onCheckedChange = { onlyWeak = it })
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    navController.navigate(
                        "flashcards_session?level=$level&category=$category" +
                                "&direction=${direction.name}&weak=$onlyWeak"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text("Начать сессию", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun LevelRow(
    levels: List<LevelInfo>,
    selected: String,
    onSelect: (LevelInfo) -> Unit
) {
    // Fallback: while levels are loading, render placeholder chips so layout doesn't jump.
    val display = if (levels.isNotEmpty()) levels
    else LEVELS.mapIndexed { i, k -> LevelInfo(k, i == 0, 0f, 0, 0) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        display.forEach { info ->
            LevelChip(
                info = info,
                selected = info.key == selected,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(info) }
            )
        }
    }
}

@Composable
private fun LevelChip(
    info: LevelInfo,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = when {
        !info.unlocked -> MaterialTheme.colorScheme.surfaceVariant
        selected       -> MaterialTheme.colorScheme.primary
        else           -> MaterialTheme.colorScheme.surface
    }
    val fg = when {
        !info.unlocked -> MaterialTheme.colorScheme.onSurfaceVariant
        selected       -> MaterialTheme.colorScheme.onPrimary
        else           -> MaterialTheme.colorScheme.onSurface
    }
    val border = if (!selected && info.unlocked)
        androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    else null

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg,
        tonalElevation = if (selected) 3.dp else 0.dp,
        border = border,
        modifier = modifier
            .height(56.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (!info.unlocked) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = "Заблокирован",
                    tint = fg,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                info.key,
                color = fg,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun LevelProgressBar(info: LevelInfo) {
    val pct = (info.masteredRatio * 100).toInt().coerceIn(0, 100)
    val nextLevel = LEVELS.getOrNull(LEVELS.indexOf(info.key) + 1)
    val hint = when {
        info.totalCount == 0 -> "Нет слов на этом уровне"
        info.masteredRatio >= UNLOCK_THRESHOLD && nextLevel != null ->
            "Уровень ${info.key} пройден — $nextLevel открыт"
        info.masteredRatio >= UNLOCK_THRESHOLD ->
            "Уровень ${info.key} пройден! 🎉"
        nextLevel != null ->
            "Освоено ${info.masteredCount} из ${info.totalCount} · до открытия $nextLevel осталось ${(UNLOCK_THRESHOLD * info.totalCount).toInt() - info.masteredCount} слов"
        else ->
            "Освоено ${info.masteredCount} из ${info.totalCount}"
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Прогресс уровня ${info.key}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                "$pct%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        LinearProgressIndicator(
            progress = { info.masteredRatio.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
        Text(
            hint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CategoryCarousel(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    if (options.isEmpty()) return

    val size = options.size
    // Pseudo-infinite looped carousel: huge item count, modulo-indexed.
    val virtualCount = if (size <= 1) size else Int.MAX_VALUE
    val startIndex = if (size <= 1) 0 else (Int.MAX_VALUE / 2).let { it - it % size }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(virtualCount) { idx ->
            val key = options[idx % size]
            val info = CategoryMeta.infoFor(key)
            CategoryCard(
                info = info,
                selected = key == selected,
                onClick = { onSelect(key) }
            )
        }
    }
}

@Composable
private fun CategoryCard(
    info: CategoryInfo,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bg,
        tonalElevation = if (selected) 4.dp else 1.dp,
        modifier = Modifier
            .size(width = 96.dp, height = 100.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = info.icon,
                contentDescription = info.label,
                tint = fg,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                info.label,
                color = fg,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun DirectionOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        tonalElevation = if (selected) 3.dp else 0.dp,
        shape = RoundedCornerShape(12.dp),
        color = if (selected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = null)
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
