package com.spanishapp.ui.flashcards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
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

/** One row in the sets list. */
data class SetRowUi(
    val set: FlashcardSet,
    val mastered: Int,    // count of words from the set already learned
    val total: Int,       // size of the set (= set.wordsSpanish.size, capped to DB hits)
    val stars: Int,       // 0..3 from FlashcardSetProgressEntity
    val unlocked: Boolean,
    val isNext: Boolean
) {
    val ratio: Float get() = if (total > 0) mastered.toFloat() / total else 0f
}

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

    fun selectLevel(level: String) {
        _selectedLevel.value = level
        loadSetsFor(level)
    }

    fun loadSetsFor(level: String) {
        viewModelScope.launch {
            val sets = FlashcardSetData.byLevel(level)
            val progressMap = setDao.getAll().associateBy { it.setId }

            // Find the next-to-do (first locked-in-progress) set so we highlight it.
            var prevReady = true
            val rows = sets.map { set ->
                val words = wordDao.findBySpanishMany(
                    set.wordsSpanish.map { it.lowercase().trim() }
                )
                val total = words.size
                val mastered = words.count { it.isLearned }
                val ratio = if (total > 0) mastered.toFloat() / total else 0f
                val unlocked = prevReady
                val isNext = unlocked && ratio < FlashcardSetData.UNLOCK_RATIO
                val row = SetRowUi(
                    set = set,
                    mastered = mastered,
                    total = total,
                    stars = progressMap[set.id]?.stars ?: 0,
                    unlocked = unlocked,
                    isNext = isNext && prevReady
                )
                prevReady = ratio >= FlashcardSetData.UNLOCK_RATIO
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
            // ── Header ─────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
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

            // ── Level tabs ─────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
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
            items(sets, key = { it.set.id }) { row ->
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
        color = if (selected) accent else MaterialTheme.colorScheme.surface,
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
        backgroundColor = MaterialTheme.colorScheme.surface,
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
                    // Progress text + bar
                    Text(
                        "${row.mastered} / ${row.total} слов",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
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
                                .fillMaxWidth(row.ratio.coerceIn(0f, 1f))
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

            // Stars
            if (row.unlocked) {
                Spacer(Modifier.width(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(3) { i ->
                        Icon(
                            Icons.Default.Star, null,
                            modifier = Modifier.size(14.dp),
                            tint = if (i < row.stars) Color(0xFFFFC107)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}
