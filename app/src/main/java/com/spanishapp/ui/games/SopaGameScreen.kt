package com.spanishapp.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.spanishapp.R
import com.spanishapp.domain.games.GameId
import com.spanishapp.ui.games.common.LevelCompleteSheet
import com.spanishapp.ui.games.common.LevelMapScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val ACCENT = Color(0xFF4CAF50)
private val BgGray
    @Composable get() = MaterialTheme.colorScheme.background
private val TextMain
    @Composable get() = MaterialTheme.colorScheme.onSurface
private val CardBorder
    @Composable get() = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
private val CardSurface
    @Composable get() = MaterialTheme.colorScheme.surfaceContainerHighest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SopaGameScreen(
    navController: NavHostController,
    viewModel: SopaViewModel = hiltViewModel()
) {
    com.spanishapp.service.TrackActivity(com.spanishapp.service.ActivityType.GAME)
    val state by viewModel.state.collectAsStateWithLifecycle()
    // v1.25.97 (audit H1): isPro поднят наверх — нужен и для кнопки «Дальше».
    val isPro by com.spanishapp.ui.games.common.rememberIsProState()

    when {
        state.showLevelMap -> {
            LevelMapScreen(
                gameId  = GameId.SOPA,
                title   = stringResource(R.string.sopa_levels_title),
                accent  = ACCENT,
                manager = viewModel.levelManager,
                onBack  = { navController.popBackStack() },
                onLevelStart = { lvl -> viewModel.startLevel(lvl) },
                isPro     = isPro,
                onPaywall = { navController.navigate("paywall") { launchSingleTop = true } },
            )
        }
        state.isGameOver -> {
            SopaGameContent(state, viewModel, onBack = { viewModel.openLevelMap() })
            LevelCompleteSheet(
                level   = state.level,
                stars   = state.finalStars,
                percent = state.finalPercent,
                accent  = ACCENT,
                onRetry = { viewModel.startLevel(state.level) },
                // v1.25.97 (audit H1): free после 10 уровня → paywall.
                onNext  = if (state.finalStars > 0 && state.level < 100) {
                              if (!isPro && state.level + 1 > com.spanishapp.ui.games.common.FREE_GAME_LEVELS) {
                                  { navController.navigate("paywall") { launchSingleTop = true } }
                              } else {
                                  { viewModel.startLevel(state.level + 1) }
                              }
                          } else null,
                onExit  = { viewModel.openLevelMap() }
            )
        }
        else -> SopaGameContent(state, viewModel,
            onBack = { viewModel.openLevelMap() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SopaGameContent(
    state: SopaGameState,
    viewModel: SopaViewModel,
    onBack: () -> Unit
) {
    val haptic = com.spanishapp.ui.components.rememberCheckedHaptic()
    var gridSize by remember { mutableStateOf(IntSize.Zero) }
    val cells = state.config.gridSize

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.sopa_level_of, state.level), fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.sopa_subtitle, state.params.cefr.joinToString("+"), cells, state.words.size),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    // v1.16.0: HintBadge + hint button (вместо score-based hint)
                    val hintBalance by viewModel.hintBalance.collectAsStateWithLifecycle()
                    var showNoHintsDialog by remember { mutableStateOf(false) }
                    if (showNoHintsDialog) {
                        AlertDialog(
                            onDismissRequest = { showNoHintsDialog = false },
                            title = { Text(stringResource(R.string.hint_bank_empty_title)) },
                            text = { Text(stringResource(R.string.hint_bank_empty_msg)) },
                            confirmButton = {
                                TextButton(onClick = { showNoHintsDialog = false }) {
                                    Text("OK")
                                }
                            }
                        )
                    }
                    if (!state.isGameOver) {
                        com.spanishapp.ui.components.HintBadge(
                            count = hintBalance,
                            modifier = Modifier.padding(end = 4.dp),
                        )
                        IconButton(onClick = {
                            viewModel.useHint(onNoHints = { showNoHintsDialog = true })
                        }) {
                            Icon(Icons.Default.Lightbulb, "Hint",
                                tint = if (hintBalance > 0) ACCENT else Color.Gray)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BgGray)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Шапка: очки + таймер
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(stringResource(R.string.sopa_score, state.score), fontWeight = FontWeight.Bold, color = ACCENT)
                    if (state.combo > 1) Text(stringResource(R.string.sopa_combo, state.combo),
                        color = Color(0xFFF44336),
                        fontWeight = FontWeight.Black, fontSize = 12.sp)
                    Text(stringResource(R.string.sopa_found, state.words.count { it.isFound }, state.words.size),
                        fontSize = 12.sp, color = Color.Gray)
                }
                if (state.hasTimer) {
                    Surface(
                        color = if (state.timeLeftSeconds < 30) Color.Red.copy(alpha = 0.1f)
                                else ACCENT.copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("${state.timeLeftSeconds}",
                                color = if (state.timeLeftSeconds < 30) Color.Red else ACCENT,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Сетка с букв-свайпом ─────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .onGloballyPositioned { gridSize = it.size }
                    .pointerInput(cells) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val cell = getCellFromOffset(offset, gridSize, cells)
                                if (cell != null) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.onDragStart(cell.first, cell.second)
                                }
                            },
                            onDrag = { change, _ ->
                                val cell = getCellFromOffset(change.position, gridSize, cells)
                                if (cell != null) viewModel.onDragUpdate(cell.first, cell.second)
                            },
                            onDragEnd = { viewModel.onDragEnd() }
                        )
                    }
            ) {
                val cellSizePx = if (gridSize.width > 0) gridSize.width.toFloat() / cells else 0f

                Column {
                    for (r in 0 until cells) {
                        Row {
                            for (c in 0 until cells) {
                                val ch = state.grid[r][c]
                                val isSelected = state.selectedCells.contains(r to c)
                                val foundWord = state.foundWords.find { it.cells.contains(r to c) }
                                val isHinted = state.hintCells.contains(r to c)

                                Box(
                                    modifier = Modifier
                                        .size(with(LocalDensity.current) { cellSizePx.toDp() })
                                        .padding(1.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when {
                                                isSelected     -> ACCENT.copy(alpha = 0.30f)
                                                foundWord != null -> foundWord.color.copy(alpha = 0.20f)
                                                isHinted       -> Color.Yellow.copy(alpha = 0.5f)
                                                else           -> Color.Transparent
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // v1.15.3: на планшете шрифт крупнее.
                                    // Юзер: "буквы слишком мелкие".
                                    val isWideSopa = com.spanishapp.ui.adaptive.isWideScreen()
                                    val cellFontSize = when {
                                        cells > 14 -> if (isWideSopa) 18.sp else 11.sp
                                        cells > 12 -> if (isWideSopa) 20.sp else 13.sp
                                        cells > 9  -> if (isWideSopa) 22.sp else 14.sp
                                        else       -> if (isWideSopa) 26.sp else 18.sp
                                    }
                                    Text(
                                        ch.toString(),
                                        fontSize = cellFontSize,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = when {
                                            foundWord != null -> foundWord.color.copy(alpha = 0.85f)
                                            isHinted       -> Color.Black
                                            else           -> TextMain
                                        },
                                        textDecoration = if (foundWord != null)
                                            TextDecoration.LineThrough else null
                                    )
                                }
                            }
                        }
                    }
                }

                // Линия выделения
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (state.selectedCells.size > 1) {
                        for (i in 0 until state.selectedCells.size - 1) {
                            val s = state.selectedCells[i]
                            val e = state.selectedCells[i + 1]
                            drawLine(
                                color = ACCENT.copy(alpha = 0.4f),
                                start = Offset((s.second + 0.5f) * cellSizePx, (s.first + 0.5f) * cellSizePx),
                                end   = Offset((e.second + 0.5f) * cellSizePx, (e.first + 0.5f) * cellSizePx),
                                strokeWidth = 12f
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Список слов (если не ghost) ─────────────────
            if (!state.config.ghost) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = CardSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    val scroll = rememberScrollState()
                    Column(modifier = Modifier
                        .padding(12.dp)
                        .verticalScroll(scroll)) {
                        Text(stringResource(R.string.sopa_find_words), color = ACCENT, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.height(8.dp))

                        val words = state.words
                        val cols = 2
                        val rows = (words.size + cols - 1) / cols
                        for (r in 0 until rows) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (c in 0 until cols) {
                                    val idx = r + c * rows
                                    if (idx < words.size) {
                                        val w = words[idx]
                                        Column(modifier = Modifier
                                            .weight(1f)
                                            .padding(vertical = 4.dp, horizontal = 4.dp)) {
                                            Text(
                                                w.word,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (w.isFound) w.color else TextMain,
                                                textDecoration = if (w.isFound)
                                                    TextDecoration.LineThrough else null,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                w.translation,
                                                fontSize = 11.sp,
                                                color = Color.Gray,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Spacer(Modifier.weight(1f))
                Text(stringResource(R.string.sopa_ghost),
                    color = Color.Gray, fontSize = 12.sp)
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = { viewModel.clearSelection() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.sopa_clear_selection))
            }
        }
    }
}

private fun getCellFromOffset(offset: Offset, gridSize: IntSize, gridCount: Int): Pair<Int, Int>? {
    if (gridSize.width <= 0) return null
    val cellW = gridSize.width.toFloat() / gridCount
    val cellH = gridSize.height.toFloat() / gridCount
    val col = (offset.x / cellW).toInt()
    val row = (offset.y / cellH).toInt()
    return if (row in 0 until gridCount && col in 0 until gridCount) row to col else null
}
