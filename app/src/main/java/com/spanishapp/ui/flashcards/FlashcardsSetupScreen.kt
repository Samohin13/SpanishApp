package com.spanishapp.ui.flashcards

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.spanishapp.ui.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private val LEVELS = listOf("A1", "A2", "B1", "B2")
private val LEVEL_EMOJIS = mapOf("A1" to "🐣", "A2" to "🐥", "B1" to "🦜", "B2" to "🦅")
private const val UNLOCK_THRESHOLD = 0.8f

data class LevelInfo(
    val key: String,
    val unlocked: Boolean,
    val masteredRatio: Float,
    val masteredCount: Int,
    val totalCount: Int,
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

    val categories by viewModel.categories.collectAsState()
    val levels by viewModel.levels.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.loadLevels() }
    LaunchedEffect(level) {
        category = "all"
        viewModel.loadCategories(level)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Тренировка слов", 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Уровни ──────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionLabel("Сложность")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val displayLevels = levels.ifEmpty { 
                        LEVELS.mapIndexed { i, k -> LevelInfo(k, i == 0, 0f, 0, 0) } 
                    }
                    displayLevels.forEach { info ->
                        LevelSelectorItem(
                            info = info,
                            isSelected = level == info.key,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (info.unlocked) level = info.key
                                else {
                                    val prev = LEVELS.getOrNull(LEVELS.indexOf(info.key) - 1) ?: "A1"
                                    scope.launch { snackbarHost.showSnackbar("Пройди $prev на 80%") }
                                }
                            }
                        )
                    }
                }
                
                levels.firstOrNull { it.key == level }?.let { 
                    CompactProgressIndicator(it)
                }
            }

            // ── Категории ───────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionLabel("Категория")
                LazyRow(
                    contentPadding = PaddingValues(end = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(listOf("all") + categories) { key ->
                        val info = CategoryMeta.infoFor(key)
                        CategorySelectionChip(
                            label = info.label,
                            icon = info.icon,
                            isSelected = category == key,
                            onClick = { category = key }
                        )
                    }
                }
            }

            // ── Режим ──────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionLabel("Направление")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val modes = listOf(
                        Triple("ES → RU", Icons.Default.Translate, FlashcardDirection.ES_TO_RU),
                        Triple("RU → ES", Icons.Default.Create, FlashcardDirection.RU_TO_ES),
                        Triple("MIX", Icons.Default.Shuffle, FlashcardDirection.MIXED)
                    )
                    modes.forEach { (label, icon, value) ->
                        ModeSelectorItem(
                            label = label,
                            icon = icon,
                            isSelected = direction == value,
                            modifier = Modifier.weight(1f),
                            onClick = { direction = value }
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Кнопка Старт ────────────────────────────────────
            Button(
                onClick = {
                    navController.navigate("flashcards_session?level=$level&category=$category&direction=${direction.name}")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Ochre),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp)
            ) {
                Text("НАЧАТЬ ТРЕНИРОВКУ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        letterSpacing = 1.2.sp
    )
}

@Composable
private fun LevelSelectorItem(
    info: LevelInfo,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val targetScale = if (isSelected) 1.05f else 1f
    val scale by animateFloatAsState(targetScale, label = "scale")
    val bgColor by animateColorAsState(
        if (isSelected) AppColors.Ochre else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
        label = "color"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .height(72.dp),
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = if (!isSelected) borderStroke() else null,
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (!info.unlocked) {
                Icon(Icons.Default.Lock, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text(LEVEL_EMOJIS[info.key] ?: "", fontSize = 20.sp)
            }
            Text(
                info.key,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CategorySelectionChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        if (isSelected) AppColors.Ochre.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
        label = "bg"
    )
    val contentColor by animateColorAsState(
        if (isSelected) AppColors.Ochre else MaterialTheme.colorScheme.onSurface,
        label = "content"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) AppColors.Ochre else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier.height(52.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = contentColor)
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun ModeSelectorItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
        label = "bg"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = if (!isSelected) borderStroke() else null
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon, null,
                modifier = Modifier.size(18.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CompactProgressIndicator(info: LevelInfo) {
    val pct = (info.masteredRatio * 100).toInt().coerceIn(0, 100)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LinearProgressIndicator(
            progress = { info.masteredRatio.coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(CircleShape),
            color = AppColors.Ochre,
            trackColor = AppColors.Ochre.copy(alpha = 0.1f)
        )
        Text(
            "$pct%",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = AppColors.Ochre
        )
    }
}

@Composable
private fun borderStroke() = androidx.compose.foundation.BorderStroke(
    1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
)
