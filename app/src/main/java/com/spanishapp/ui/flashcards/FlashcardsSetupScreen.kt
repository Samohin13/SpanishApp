package com.spanishapp.ui.flashcards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import com.spanishapp.ui.components.StaggeredEntrance
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val LEVELS = listOf("A1", "A2", "B1", "B2")

// v1.14.2: используем единую палитру CefrColors (см. ui/theme/CefrColors.kt).
private fun levelAccent(level: String): Color =
    com.spanishapp.ui.theme.CefrColors.forLevel(level)

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

    /**
     * Tracks the in-flight loadSetsFor coroutine so we can cancel it when the
     * user switches levels quickly — prevents a stale A1 query from overwriting
     * an already-displayed A2 result (race condition).
     */
    private var loadJob: kotlinx.coroutines.Job? = null

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
        loadJob?.cancel()           // cancel any in-flight query for another level
        loadJob = viewModelScope.launch {
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
    val selectedLevel by viewModel.selectedLevel.collectAsStateWithLifecycle()
    val sets          by viewModel.setsForLevel.collectAsStateWithLifecycle()
    val weakCount     by viewModel.weakCount.collectAsStateWithLifecycle()

    // loadSetsFor is already called from ViewModel.selectLevel() and from the
    // observeAll-collect watcher in init — no need to call it again here.
    // Calling it here causes a double-load and potential stale-data race:
    // if the user switches A1→A2 quickly, an in-flight A1 query could overwrite
    // the A2 result after it had already been shown.

    // v1.12.4: 1/2/2 cols (было 1/2/3 — на 3 cols Сет 1 unlocked
    // высокий, остальные locked короткие → рваная геометрия).
    val cols = com.spanishapp.ui.adaptive.adaptiveColumns(compact = 1, medium = 2, expanded = 2)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(cols),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Stagger entrance was disabled per user feedback — flashcard set
            // rows are tall and the slide-up cascade looked off. All items
            // now appear instantly. .animateItem() is preserved on rows for
            // smooth re-ordering when a set's progress changes.

            // ── Header ─────────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
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
                        stringResource(com.spanishapp.R.string.flashcards_setup_subtitle),
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Practice tile ─────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
                PracticeTile(
                    weakCount = weakCount,
                    onClick = { navController.navigate("practice") },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // ── Level tabs ─────────────────────────────────────
            item(span = { GridItemSpan(maxLineSpan) }) {
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

            item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(12.dp)) }

            // ── Empty state ─────────────────────────────────────
            if (sets.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(com.spanishapp.R.string.flashcards_coming_soon_template, selectedLevel),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // ── Sets list (grid 1/2/3 cols по ширине экрана) ─────
            gridItemsIndexed(sets, key = { _, it -> it.set.id }) { _, row ->
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
                    modifier = Modifier.animateItem()
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
    val accent = levelAccent(label)
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) accent
                else MaterialTheme.colorScheme.surfaceContainerHighest,
        border = if (selected) null
                 else BorderStroke(1.5.dp, accent.copy(alpha = 0.45f))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                label,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (selected) Color.White else accent
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
    val accent = levelAccent(row.set.level)

    // v1.15.1: ужал ещё (юзер: "что я попросил сделать с этим, исправляй").
    // Карточка stays минимально-функциональной — только высота emoji+text,
    // никакого лишнего пространства. Реально как «световой паттерн».
    val isWide = com.spanishapp.ui.adaptive.isWideScreen()
    val cardMinHeight = if (isWide) 68.dp else 72.dp

    com.spanishapp.ui.components.PressableCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = cardMinHeight),
        shape = RoundedCornerShape(14.dp),
        backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        shadowElevation = if (row.unlocked) 3.dp else 0.dp,
        enabled = row.unlocked
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .drawBehind {
                    if (row.unlocked) {
                        // Left accent stripe (level colour)
                        drawRect(
                            color = accent,
                            size  = Size(4.dp.toPx(), size.height)
                        )
                        // Subtle radial glow from top-right
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(accent.copy(alpha = 0.11f), Color.Transparent),
                                center = Offset(size.width, 0f),
                                radius = size.width * 0.75f
                            ),
                            size = size
                        )
                    }
                }
        ) {
            Row(
                modifier = Modifier.padding(
                    start = 14.dp, end = 12.dp, top = 10.dp, bottom = 10.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // v1.15.1: ещё компактнее (минимум для читабельности)
                val emojiSize = if (isWide) 40.dp else 40.dp
                val emojiFont = if (isWide) 20.sp else 20.sp
                val lockSize = if (isWide) 18.dp else 18.dp
                val titleFont = if (isWide) 14.sp else 14.sp
                val metaFont = if (isWide) 11.sp else 11.sp
                Box(
                    modifier = Modifier
                        .size(emojiSize)
                        .clip(CircleShape)
                        .background(
                            when {
                                !row.unlocked -> MaterialTheme.colorScheme.surfaceVariant
                                row.isNext   -> accent
                                else         -> accent.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!row.unlocked) {
                        Icon(
                            Icons.Default.Lock, null,
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(lockSize)
                        )
                    } else {
                        Text(row.set.emoji, fontSize = emojiFont)
                    }
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(com.spanishapp.R.string.flashcards_set_row_title_template, row.set.order, row.set.title),
                        fontSize   = titleFont,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (row.unlocked) MaterialTheme.colorScheme.onSurface
                                     else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                    Spacer(Modifier.height(2.dp))
                    if (row.unlocked) {
                        val wordOne = stringResource(com.spanishapp.R.string.word_count_one)
                        val wordFew = stringResource(com.spanishapp.R.string.word_count_few)
                        val wordMany = stringResource(com.spanishapp.R.string.word_count_many)
                        val statusText = when {
                            row.seenCount == 0        -> stringResource(
                                com.spanishapp.R.string.flashcards_set_unstarted_template,
                                row.total, pluralRu(row.total, wordOne, wordFew, wordMany))
                            row.seenCount < row.total -> stringResource(
                                com.spanishapp.R.string.flashcards_set_progress_template,
                                row.seenCount, row.total,
                                pluralRu(row.total, wordOne, wordFew, wordMany))
                            else                      -> stringResource(
                                com.spanishapp.R.string.flashcards_set_completed_template,
                                row.total, pluralRu(row.total, wordOne, wordFew, wordMany))
                        }
                        Text(
                            statusText,
                            fontSize = metaFont,
                            color    = if (row.seenCount >= row.total && row.total > 0)
                                           Color(0xFF4CAF50)
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        // Progress bar — тонкий 4dp, занимает мало места
                        val progressFraction =
                            if (row.total > 0) row.seenCount.toFloat() / row.total else 0f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progressFraction.coerceIn(0f, 1f))
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(accent)
                            )
                        }
                    } else {
                        Text(
                            stringResource(com.spanishapp.R.string.flashcards_set_locked),
                            fontSize = metaFont,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (row.unlocked && row.tier != TrophyTier.NONE) {
                    Spacer(Modifier.width(10.dp))
                    TrophyBadge(row.tier)
                }
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
    val gold = Color(0xFFFF6B35)   // brand orange — same as progress bar / buttons

    com.spanishapp.ui.components.PressableCard(
        onClick       = onClick,
        modifier      = modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(18.dp),
        backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 4.dp
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .drawBehind {
                    // Left gold stripe
                    drawRect(color = gold, size = Size(5.dp.toPx(), size.height))
                    // Very subtle warm hint top-right (barely visible)
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(gold.copy(alpha = 0.08f), Color.Transparent),
                            center = Offset(size.width, 0f),
                            radius = size.width * 0.85f
                        ),
                        size = size
                    )
                }
        ) {
            Row(
                modifier          = Modifier.padding(
                    start = 20.dp, end = 16.dp, top = 16.dp, bottom = 16.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pencil icon circle — "рука с ручкой"
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(gold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit, null,
                        tint     = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    // Bento-style label chip
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = gold.copy(alpha = 0.18f)
                    ) {
                        Text(
                            stringResource(com.spanishapp.R.string.flashcards_practice_badge),
                            fontSize      = 10.sp,
                            fontWeight    = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp,
                            color         = gold,
                            modifier      = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                    val wOne = stringResource(com.spanishapp.R.string.word_count_one)
                    val wFew = stringResource(com.spanishapp.R.string.word_count_few)
                    val wMany = stringResource(com.spanishapp.R.string.word_count_many)
                    Text(
                        if (weakCount == 0)
                            stringResource(com.spanishapp.R.string.flashcards_practice_empty)
                        else
                            stringResource(com.spanishapp.R.string.flashcards_practice_ready_template,
                                weakCount, pluralRu(weakCount, wOne, wFew, wMany)),
                        fontSize = 13.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Slavic-style pluralization: pick one of three forms based on count.
 * Caller passes locale-resolved forms (stringResource(...)) so the
 * function works for ru/uk/en/es alike — for languages without a
 * distinct "few" form (en/es), one/few/many resolve to the same word.
 */
private fun pluralRu(n: Int, one: String, few: String, many: String): String {
    val mod10 = n % 10
    val mod100 = n % 100
    return when {
        mod100 in 11..14 -> many
        mod10 == 1       -> one
        mod10 in 2..4    -> few
        else             -> many
    }
}
