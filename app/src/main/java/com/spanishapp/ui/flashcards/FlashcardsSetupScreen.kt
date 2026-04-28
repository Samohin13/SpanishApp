package com.spanishapp.ui.flashcards

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

// ── Design tokens ──────────────────────────────────────────────
private val Purple     = Color(0xFF7B2FBE)
private val PurplePale = Color(0xFFF3E8FF)
private val Pink       = Color(0xFFE040FB)
private val TextMain   = Color(0xFF1A1A1A)
private val TextGray   = Color(0xFF8E8E93)
private val BgGray     = Color(0xFFF8F8FA)
private val CardBorder = Color(0xFFE5E5EA)

// ── ViewModel ──────────────────────────────────────────────────

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

data class CategoryProgress(
    val key: String,
    val label: String,
    val total: Int,
    val mastered: Int,
) {
    val ratio: Float get() = if (total > 0) mastered.toFloat() / total else 0f
}

@HiltViewModel
class FlashcardsSetupViewModel @Inject constructor(
    private val wordDao: WordDao
) : ViewModel() {

    private val _levels = MutableStateFlow<List<LevelInfo>>(emptyList())
    val levels: StateFlow<List<LevelInfo>> = _levels.asStateFlow()

    private val _categoryProgress = MutableStateFlow<List<CategoryProgress>>(emptyList())
    val categoryProgress: StateFlow<List<CategoryProgress>> = _categoryProgress.asStateFlow()

    // keep for compatibility with FlashcardsViewModel
    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    fun loadLevels() {
        viewModelScope.launch {
            val result = mutableListOf<LevelInfo>()
            var prevMastered = true
            for (lvl in LEVELS) {
                val total    = wordDao.countByLevel(lvl)
                val mastered = if (total > 0) wordDao.countMasteredByLevel(lvl) else 0
                val ratio    = if (total > 0) mastered.toFloat() / total else 0f
                result += LevelInfo(lvl, prevMastered, ratio, mastered, total)
                prevMastered = ratio >= UNLOCK_THRESHOLD
            }
            _levels.value = result
        }
    }

    fun loadCategoriesWithProgress(level: String) {
        viewModelScope.launch {
            val cats       = wordDao.categoriesForLevel(level)
            val allTotal   = wordDao.countByLevel(level)
            val allMastered = wordDao.countMasteredByLevel(level)
            _categories.value = cats

            val list = buildList {
                add(CategoryProgress("all", "Все слова", allTotal, allMastered))
                cats.forEach { cat ->
                    val t = wordDao.countByLevelAndCategory(level, cat)
                    val m = wordDao.countMasteredByLevelAndCategory(level, cat)
                    val info = CategoryMeta.infoFor(cat)
                    add(CategoryProgress(cat, info.label, t, m))
                }
            }
            _categoryProgress.value = list
        }
    }
}

// ── Screen ─────────────────────────────────────────────────────

@Composable
fun FlashcardsSetupScreen(
    navController: NavHostController,
    viewModel: FlashcardsSetupViewModel = hiltViewModel()
) {
    var selectedLevel by remember { mutableStateOf("A1") }
    val levels        by viewModel.levels.collectAsState()
    val catProgress   by viewModel.categoryProgress.collectAsState()
    val snackbarHost  = remember { SnackbarHostState() }
    val scope         = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.loadLevels() }
    LaunchedEffect(selectedLevel) { viewModel.loadCategoriesWithProgress(selectedLevel) }

    val levelInfo = levels.firstOrNull { it.key == selectedLevel }
    val catCount  = catProgress.size.coerceAtLeast(1) - 1  // subtract "all"

    Scaffold(
        containerColor = BgGray,
        snackbarHost   = { SnackbarHost(snackbarHost) }
    ) { padding ->
        LazyColumn(
            modifier        = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
            contentPadding  = PaddingValues(bottom = 32.dp)
        ) {

            // ── Header ─────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp)
                ) {
                    Text(
                        "Карточки",
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextMain
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Учи новые слова по уровням",
                        fontSize = 15.sp,
                        color    = TextGray
                    )
                }
            }

            // ── Level tabs ─────────────────────────────────────
            item {
                Row(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val displayLevels = levels.ifEmpty {
                        LEVELS.mapIndexed { i, k -> LevelInfo(k, i == 0, 0f, 0, 0) }
                    }
                    displayLevels.forEach { info ->
                        LevelTab(
                            info       = info,
                            isSelected = selectedLevel == info.key,
                            modifier   = Modifier.weight(1f),
                            onClick    = {
                                if (info.unlocked) {
                                    selectedLevel = info.key
                                } else {
                                    val prev = LEVELS.getOrNull(LEVELS.indexOf(info.key) - 1) ?: "A1"
                                    scope.launch { snackbarHost.showSnackbar("Пройди $prev на 80%") }
                                }
                            }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }

            // ── Level info row ─────────────────────────────────
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape  = RoundedCornerShape(16.dp),
                    color  = Color.White,
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier          = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Purple),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                selectedLevel,
                                fontSize   = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color      = Color.White
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Уровень $selectedLevel",
                                fontSize   = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color      = TextMain
                            )
                            Text(
                                "$catCount категорий",
                                fontSize = 13.sp,
                                color    = TextGray
                            )
                        }
                        // Level progress
                        levelInfo?.let {
                            Text(
                                "${it.masteredCount}/${it.totalCount}",
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = Purple
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }

            // ── Category rows ──────────────────────────────────
            items(catProgress) { cat ->
                CategoryRow(
                    cat     = cat,
                    onClick = {
                        navController.navigate(
                            "flashcards_session?level=$selectedLevel&category=${cat.key}&direction=ES_TO_RU"
                        )
                    }
                )
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

// ── Level tab ──────────────────────────────────────────────────

@Composable
private fun LevelTab(
    info: LevelInfo,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue   = if (isSelected) Purple else Color.White,
        animationSpec = tween(200),
        label         = "level_bg"
    )
    val textColor by animateColorAsState(
        targetValue   = if (isSelected) Color.White else TextGray,
        animationSpec = tween(200),
        label         = "level_text"
    )

    Surface(
        onClick  = onClick,
        modifier = modifier.height(44.dp),
        shape    = RoundedCornerShape(12.dp),
        color    = bgColor,
        border   = BorderStroke(1.dp, if (isSelected) Purple else CardBorder)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!info.unlocked) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint     = if (isSelected) Color.White else TextGray,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Text(
                    info.key,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = textColor
                )
            }
        }
    }
}

// ── Category row ───────────────────────────────────────────────

@Composable
private fun CategoryRow(cat: CategoryProgress, onClick: () -> Unit) {
    val info = if (cat.key == "all") {
        CategoryMeta.infoFor("all")
    } else {
        CategoryMeta.infoFor(cat.key)
    }

    Surface(
        onClick  = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape  = RoundedCornerShape(16.dp),
        color  = Color.White,
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                Icon(
                    info.icon,
                    contentDescription = null,
                    tint     = Purple,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    info.label,
                    modifier   = Modifier.weight(1f),
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextMain
                )
                Text(
                    "${cat.mastered}/${cat.total}",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (cat.ratio >= 1f) Purple else TextGray
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint     = Color(0xFFC7C7CC),
                    modifier = Modifier.size(18.dp)
                )
            }

            if (cat.total > 0) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(PurplePale)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(cat.ratio.coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(listOf(Purple, Pink))
                            )
                    )
                }
            }
        }
    }
}
