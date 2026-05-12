package com.spanishapp.ui.flashcards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import com.spanishapp.ui.components.StaggeredEntrance
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.spanishapp.data.db.dao.FlashcardSetProgressDao
import com.spanishapp.data.db.dao.WordDao
import com.spanishapp.data.db.entity.WordEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private val LEVELS = listOf("A1", "A2", "B1", "B2")

// ── UI state types ─────────────────────────────────────────────

/** Trophy tier earned by the best session for this set. */
enum class TrophyTier {
    NONE,    // never practiced or below 50%
    BRONZE,  // 50-69%
    SILVER,  // 70-89%
    GOLD     // 90-100%
}

/** One row in the sets list. */
data class SetRowUi(
    val set: FlashcardSet,
    val total: Int,            // size of the set (= words actually present in DB)
    val seenCount: Int,        // words with ≥1 correct review (progress within the set)
    val bestPercent: Int,      // 0..100, best session result so far
    val tier: TrophyTier,
    val isCompleted: Boolean,  // true if user finished at least one session
    val unlocked: Boolean,
    val isNext: Boolean
)

// ── ViewModel ──────────────────────────────────────────────────

@HiltViewModel
class FlashcardsSetupViewModel @Inject constructor(
    private val wordDao: WordDao,
    private val setDao: FlashcardSetProgressDao
) : ViewModel() {

    private val _selectedLevel = MutableStateFlow("A1")
    val selectedLevel: StateFlow<String> = _selectedLevel.asStateFlow()

    private val _setsForLevel = MutableStateFlow<List<SetRowUi>>(emptyList())
    val setsForLevel: StateFlow<List<SetRowUi>> = _setsForLevel.asStateFlow()

    /** Number of weak words across the whole vocabulary (drives Practice tile). */
    private val _weakCount = MutableStateFlow(0)
    val weakCount: StateFlow<Int> = _weakCount.asStateFlow()

    init {
        viewModelScope.launch { _weakCount.value = wordDao.countPracticePool() }
        // Re-emit set list whenever ANY set's progress changes — so completing
        // a session and navigating back instantly refreshes stars/unlocks.
        viewModelScope.launch {
            setDao.observeAll().collect {
                loadSetsFor(_selectedLevel.value)
                _weakCount.value = wordDao.countPracticePool()
            }
        }
    }

    fun selectLevel(level: String) {
        _selectedLevel.value = level
        loadSetsFor(level)
    }

    fun loadSetsFor(level: String) {
        viewModelScope.launch {
            val sets = FlashcardSetData.byLevel(level)
            val progressMap = setDao.getAll().associateBy { it.setId }

            // Unlock = previous set was completed at least once (any session).
            // We don't gate on accuracy — the user is free to retry weak sets
            // anytime via Practice mode without being blocked from progressing.
            var prevCompleted = true   // first set always unlocked
            val rows = sets.map { set ->
                val words = wordDao.findBySpanishMany(
                    set.wordsSpanish.map { it.lowercase().trim() }
                )
                val total = words.size
                val seenCount = words.count { it.correctReviews > 0 }
                val progress = progressMap[set.id]
                val bestPercent = progress?.bestPercent ?: 0
                val isCompleted = (progress?.completedAt ?: 0L) > 0L
                val tier = when {
                    bestPercent >= 90 -> TrophyTier.GOLD
                    bestPercent >= 70 -> TrophyTier.SILVER
                    bestPercent >= 50 -> TrophyTier.BRONZE
                    else              -> TrophyTier.NONE
                }
                val unlocked = prevCompleted
                val isNext = unlocked && !isCompleted
                val row = SetRowUi(
                    set = set,
                    total = total,
                    seenCount = seenCount,
                    bestPercent = bestPercent,
                    tier = tier,
                    isCompleted = isCompleted,
                    unlocked = unlocked,
                    isNext = isNext
                )
                prevCompleted = isCompleted
                row
            }
            _setsForLevel.value = rows
        }
    }
}

// ── Screen ─────────────────────────────────────────────────────

@Composable
fun FlashcardsSetupScreen(
    navController: NavHostController,
    viewModel: FlashcardsSetupViewModel = hiltViewModel()
) {
    val selectedLevel by viewModel.selectedLevel.collectAsState()
    val sets          by viewModel.setsForLevel.collectAsState()
    val weakCount     by viewModel.weakCount.collectAsState()

    LaunchedEffect(selectedLevel) { viewModel.loadSetsFor(selectedLevel) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Stagger entrance was disabled per user feedback — flashcard set
            // rows are tall and the slide-up cascade looked off. All items
            // now appear instantly. .animateItem() is preserved on rows for
            // smooth re-ordering when a set's progress changes.

            // ── Header ─────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp)
                ) {
                    Text(
                        "Tarjetas",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Маленькие наборы слов на каждый день",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Practice tile ─────────────────────────────────
            item {
                PracticeTile(
                    weakCount = weakCount,
                    onClick = { navController.navigate("practice") },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // ── Level tabs ─────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LEVELS.forEach { lvl ->
                        LevelChip(
                            label = lvl,
                            selected = selectedLevel == lvl,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.selectLevel(lvl) }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }

            // ── Empty state ─────────────────────────────────────
            if (sets.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Сеты для уровня $selectedLevel скоро появятся 🚧",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // ── Sets list ─────────────────────────────────────
            itemsIndexed(sets, key = { _, it -> it.set.id }) { _, row ->
                SetRow(
                    row = row,
                    onClick = {
                        if (row.unlocked) {
                            navController.navigate(
                                "flashcards_session?level=${row.set.level}" +
                                    "&category=set&direction=ES_TO_RU&setId=${row.set.id}"
                            )
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .animateItem()
                )
            }
        }
    }
}

// ── Cells ──────────────────────────────────────────────────────

@Composable
private fun LevelChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) accent else MaterialTheme.colorScheme.surfaceContainerHighest,
        border = if (selected) null
            else androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SetRow(
    row: SetRowUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    com.spanishapp.ui.components.PressableCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        shadowElevation = if (row.unlocked) 3.dp else 0.dp,
        enabled = row.unlocked
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Big emoji circle
            val accent = MaterialTheme.colorScheme.primary
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (!row.unlocked) MaterialTheme.colorScheme.surfaceVariant
                        else if (row.isNext) accent
                        else accent.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!row.unlocked) {
                    Icon(
                        Icons.Default.Lock,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(row.set.emoji, fontSize = 28.sp)
                }
            }

            Spacer(Modifier.width(14.dp))

            // Title + progress
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Сет ${row.set.order} · ${row.set.title}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (row.unlocked) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                if (row.unlocked) {
                    // "X/N знаю" — word-level mastery progress visible at a glance.
                    val statusText = when {
                        row.seenCount == 0 -> "${row.total} слов · ещё не начат"
                        row.seenCount < row.total -> "${row.seenCount}/${row.total} слов знаю"
                        else -> "✓ Все ${row.total} слов знаю"
                    }
                    Text(
                        statusText,
                        fontSize = 12.sp,
                        color = if (row.seenCount >= row.total && row.total > 0)
                            androidx.compose.ui.graphics.Color(0xFF4CAF50)
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    // Progress bar shows word mastery (seen/total), not just best %.
                    val progressFraction = if (row.total > 0) row.seenCount.toFloat() / row.total else 0f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progressFraction.coerceIn(0f, 1f))
                                .clip(RoundedCornerShape(3.dp))
                                .background(accent)
                        )
                    }
                } else {
                    Text(
                        "Откроется после прохождения предыдущего сета",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Trophy tier (Bronze / Silver / Gold) — replaces the previous 3-star row.
            if (row.unlocked && row.tier != TrophyTier.NONE) {
                Spacer(Modifier.width(10.dp))
                TrophyBadge(row.tier)
            }
        }
    }
}

@Composable
private fun TrophyBadge(tier: TrophyTier) {
    val (color, label) = when (tier) {
        TrophyTier.GOLD   -> Color(0xFFFFC107) to "GOLD"
        TrophyTier.SILVER -> Color(0xFFB0BEC5) to "SILVER"
        TrophyTier.BRONZE -> Color(0xFFCD7F32) to "BRONZE"
        TrophyTier.NONE   -> Color.Transparent to ""
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun PracticeTile(
    weakCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = Color(0xFFE8A317)  // gold — matches Tobo's reference
    com.spanishapp.ui.components.PressableCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = accent,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Refresh, null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Практика",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    if (weakCount == 0)
                        "Пройди хотя бы один сет — слова появятся здесь"
                    else
                        "$weakCount ${pluralWords(weakCount)} готовы к повторению",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

/** Russian pluralization for "слово" — 1 слово / 2-4 слова / 5+ слов. */
private fun pluralWords(n: Int): String {
    val mod10 = n % 10
    val mod100 = n % 100
    return when {
        mod100 in 11..14 -> "слов"
        mod10 == 1       -> "слово"
        mod10 in 2..4    -> "слова"
        else             -> "слов"
    }
}
