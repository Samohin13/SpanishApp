package com.spanishapp.ui.games

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.spanishapp.R

// Accent colour for Crucigrama. MUST match the icon tint used in
// GamesScreen.kt (Crucigrama icon = 0xFF26A69A teal). The variable name
// "Purple" is legacy; kept to minimise diff. — see fix(colors) phase 4.
private val Purple = Color(0xFF26A69A)
private val BgGray
    @Composable get() = MaterialTheme.colorScheme.background
private val TextMain
    @Composable get() = MaterialTheme.colorScheme.onSurface
private val TextGray
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val CardBorder
    @Composable get() = MaterialTheme.colorScheme.outline
private val Gold = Color(0xFFFF9500)
private val SuccessGreen = Color(0xFF4CAF50)

// Fixed cell/gap sizes in dp — scaling is done via graphicsLayer, not layout
private val CELL_DP = 38.dp
private val GAP_DP  = 5.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrosswordGameScreen(
    navController: NavHostController,
    viewModel: CrosswordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showRules by remember { mutableStateOf(false) }

    if (showRules) RulesDialog(onDismiss = { showRules = false })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.crw_title), fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.showSetup) navController.popBackStack()
                        else viewModel.resetToMenu()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { showRules = true }) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = stringResource(R.string.crw_rules))
                    }
                    Surface(
                        color = Gold.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Stars, null, tint = Gold, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("${state.coins}", color = Gold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                state.showSetup  -> CrosswordLevelSelection(state, viewModel)
                state.isGameOver -> CrosswordVictory(state, viewModel) { viewModel.resetToMenu() }
                else             -> CrosswordActiveContent(state, viewModel)
            }
        }
    }
}

// ── Rules dialog ──────────────────────────────────────────────────────────────

@Composable
fun RulesDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.crw_how_to_play), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextMain) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RuleItem(Icons.Default.TouchApp, stringResource(R.string.crw_rule_select_title),
                    stringResource(R.string.crw_rule_select_body))
                RuleItem(Icons.Default.SwapHoriz, stringResource(R.string.crw_rule_swap_title),
                    stringResource(R.string.crw_rule_swap_body))
                RuleItem(Icons.Default.Keyboard, stringResource(R.string.crw_rule_keyboard_title),
                    stringResource(R.string.crw_rule_keyboard_body))
                RuleItem(Icons.Default.Lightbulb, stringResource(R.string.crw_rule_hints_title),
                    stringResource(R.string.crw_rule_hints_body))
                RuleItem(Icons.Default.ZoomIn, stringResource(R.string.crw_rule_zoom_title),
                    stringResource(R.string.crw_rule_zoom_body))
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                shape = RoundedCornerShape(12.dp)
            ) { Text(stringResource(R.string.crw_understood), color = Color.White, fontWeight = FontWeight.Bold) }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun RuleItem(icon: ImageVector, title: String, body: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = Purple, modifier = Modifier.size(20.dp).padding(top = 2.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextMain)
            Text(body, fontSize = 15.sp, color = TextGray, lineHeight = 18.sp)
        }
    }
}

// ── Level selection ───────────────────────────────────────────────────────────

@Composable
fun CrosswordLevelSelection(state: CrosswordGameState, viewModel: CrosswordViewModel) {
    // Use the same 5-column layout + GameLevelCell as the rest of the games
    // (Articulos / Math / Speed / Sopa / Palabra / Verbos) for visual consistency.
    val maxCleared = state.levelStars.size  // crossword tracks levelStars: Map<Int, Int>
    val nextLevel = (maxCleared + 1).coerceAtMost(100)

    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(5),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(100) { index ->
            val level = index + 1
            val stars = state.levelStars[level] ?: 0
            val unlocked = level <= nextLevel
            val isNext = level == nextLevel

            com.spanishapp.ui.games.common.GameLevelCell(
                level    = level,
                stars    = stars,
                unlocked = unlocked,
                isNext   = isNext,
                accent   = Purple,
                onClick  = { if (unlocked) viewModel.startLevel(level) }
            )
        }
    }
}

// ── Active game ───────────────────────────────────────────────────────────────

@Composable
fun CrosswordActiveContent(state: CrosswordGameState, viewModel: CrosswordViewModel) {
    val density = LocalDensity.current

    // ── Zoom/pan state — reset when a new level starts ─────────────────────
    var scale by remember(state.level) { mutableFloatStateOf(1f) }
    var panX  by remember(state.level) { mutableFloatStateOf(0f) }
    var panY  by remember(state.level) { mutableFloatStateOf(0f) }

    // Box size in px — filled via onSizeChanged (0 until first measure)
    var boxWPx by remember { mutableFloatStateOf(0f) }
    var boxHPx by remember { mutableFloatStateOf(0f) }

    // Cell/gap in px
    val cellPx = with(density) { CELL_DP.toPx() }
    val gapPx  = with(density) { GAP_DP.toPx()  }

    // Grid dimensions from current state
    val cells = state.grid.keys
    val minX  = if (cells.isEmpty()) 0 else cells.minOf { it.first  }
    val maxX  = if (cells.isEmpty()) 0 else cells.maxOf { it.first  }
    val minY  = if (cells.isEmpty()) 0 else cells.minOf { it.second }
    val maxY  = if (cells.isEmpty()) 0 else cells.maxOf { it.second }
    val cols  = maxX - minX + 1
    val rows  = maxY - minY + 1
    val gridWPx = cols * cellPx + (cols - 1) * gapPx
    val gridHPx = rows * cellPx + (rows - 1) * gapPx

    // Auto-fit: run once per level after the box size is known
    LaunchedEffect(state.level, boxWPx, boxHPx) {
        if (cells.isNotEmpty() && boxWPx > 0f && boxHPx > 0f) {
            val fitH = (boxHPx * 0.95f) / gridHPx
            val fitW = (boxWPx * 0.95f) / gridWPx
            scale = minOf(fitW, fitH, 1.0f).coerceAtLeast(0.25f)
            panX  = 0f
            panY  = 0f
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Grid area ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds()
                .onSizeChanged { boxWPx = it.width.toFloat(); boxHPx = it.height.toFloat() }
                // ① Pinch-to-zoom + pan (two fingers / pinch)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.2f, 5f)
                        panX += pan.x
                        panY += pan.y
                    }
                }
                // ② Tap → map back to grid cell (graphicsLayer is visual only,
                //    so we compute which cell the finger actually touched)
                .pointerInput(state.level, minX, minY, cols, rows, cellPx, gapPx) {
                    detectTapGestures { tapOffset ->
                        val s  = scale
                        val px = panX
                        val py = panY
                        // Visual top-left of the grid inside the box
                        val originX = boxWPx / 2f + px - gridWPx * s / 2f
                        val originY = boxHPx / 2f + py - gridHPx * s / 2f
                        // Coordinates inside the unscaled grid
                        val localX = (tapOffset.x - originX) / s
                        val localY = (tapOffset.y - originY) / s
                        val col = (localX / (cellPx + gapPx)).toInt() + minX
                        val row = (localY / (cellPx + gapPx)).toInt() + minY
                        if (col in minX..maxX && row in minY..maxY &&
                            state.grid.containsKey(col to row)) {
                            viewModel.onCellClick(col, row)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Grid rendered via graphicsLayer — no layout change during zoom,
            // so cells never deform and no recomposition on every gesture frame
            Box(
                modifier = Modifier
                    .size(
                        width  = with(density) { gridWPx.toDp() },
                        height = with(density) { gridHPx.toDp() }
                    )
                    .graphicsLayer(
                        scaleX       = scale,
                        scaleY       = scale,
                        translationX = panX,
                        translationY = panY
                    )
            ) {
                CrosswordGrid(state)  // no clickable — touch handled above
            }
        }

        // ── Bottom panel ─────────────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            shadowElevation = 12.dp
        ) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                val currentWord = state.words.find { it.id == state.currentWordId }

                // Translation
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        if (currentWord?.isVertical == true) Icons.Default.ArrowDownward
                        else Icons.AutoMirrored.Filled.ArrowForward,
                        null, tint = Purple, modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        currentWord?.russian ?: stringResource(R.string.crw_choose_cell),
                        fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = TextMain, textAlign = TextAlign.Center
                    )
                }

                // Accent hint
                val accentedChars = currentWord?.spanish
                    ?.filter { it in "ÁÉÍÓÚÜÑ" }?.toSet()?.sorted()
                if (!accentedChars.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Info, null, tint = Gold, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.crw_hold_for_input, accentedChars.joinToString(", ")),
                            fontSize = 12.sp, color = Gold)
                    }
                }

                // Hint chips
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HintChip(Icons.Default.Lightbulb,    stringResource(R.string.crw_hint_letter), state.coins >= 10,  Modifier.weight(1f)) { viewModel.useHintLetter() }
                    HintChip(Icons.Default.AutoFixHigh,  stringResource(R.string.crw_hint_word), state.coins >= 50,  Modifier.weight(1f)) { viewModel.useHintWord()   }
                }

                Spacer(Modifier.height(8.dp))
                IntegratedSpanishKeyboard(
                    onKey    = { viewModel.enterLetter(it) },
                    onDelete = { viewModel.deleteLetter()  }
                )
            }
        }
    }
}

// ── Hint chip ─────────────────────────────────────────────────────────────────

@Composable
fun HintChip(icon: ImageVector, label: String, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(40.dp).clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (enabled) BgGray else BgGray.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = if (enabled) Purple else TextGray, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                color = if (enabled) TextMain else TextGray)
        }
    }
}

// ── Crossword grid (visual only — no clickable modifiers) ─────────────────────

@Composable
fun CrosswordGrid(state: CrosswordGameState) {
    val cells = state.grid.keys
    if (cells.isEmpty()) return

    val minX = cells.minOf { it.first  }
    val maxX = cells.maxOf { it.first  }
    val minY = cells.minOf { it.second }
    val maxY = cells.maxOf { it.second }

    Column(verticalArrangement = Arrangement.spacedBy(GAP_DP)) {
        for (y in minY..maxY) {
            Row(horizontalArrangement = Arrangement.spacedBy(GAP_DP)) {
                for (x in minX..maxX) {
                    val char      = state.grid[x to y]
                    val isCell    = state.grid.containsKey(x to y)
                    val isSolved  = state.words.any { cw ->
                        state.solvedWordIds.contains(cw.id) &&
                        cw.spanish.indices.any { i ->
                            val wx = if (cw.isVertical) cw.x else cw.x + i
                            val wy = if (cw.isVertical) cw.y + i else cw.y
                            wx == x && wy == y
                        }
                    }
                    val isSelected = state.selectedCell == (x to y)
                    val isError    = state.errors.contains(x to y)
                    val wordStart  = state.words.find { it.x == x && it.y == y }

                    Box(
                        modifier = Modifier
                            .size(CELL_DP)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    !isCell    -> Color.Transparent
                                    isSelected -> Purple
                                    isSolved   -> SuccessGreen.copy(alpha = 0.2f)
                                    else       -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                            .border(
                                1.dp,
                                when {
                                    !isCell    -> Color.Transparent
                                    isSelected -> Purple
                                    isError    -> Color.Red
                                    isSolved   -> SuccessGreen
                                    else       -> CardBorder.copy(alpha = 0.4f)
                                },
                                RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCell) {
                            wordStart?.let {
                                Text(
                                    it.number.toString(),
                                    modifier = Modifier.align(Alignment.TopStart).padding(2.dp),
                                    fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White.copy(alpha = 0.7f) else TextGray
                                )
                            }
                            Text(
                                text = char?.toString() ?: "",
                                fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
                                color = when {
                                    isSelected -> Color.White
                                    isError    -> Color.Red
                                    isSolved   -> SuccessGreen
                                    else       -> TextMain
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Keyboard ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IntegratedSpanishKeyboard(onKey: (Char) -> Unit, onDelete: () -> Unit) {
    val haptic = com.spanishapp.ui.components.rememberCheckedHaptic()
    var accentMenuKey by remember { mutableStateOf<Char?>(null) }

    val rows = listOf("QWERTYUIOP", "ASDFGHJKLÑ", "ZXCVBNM")
    val accentsMap = mapOf(
        'A' to listOf('Á'), 'E' to listOf('É'), 'I' to listOf('Í'),
        'O' to listOf('Ó'), 'U' to listOf('Ú', 'Ü'), 'N' to listOf('Ñ')
    )

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                if (row == "ZXCVBNM") Spacer(Modifier.width(8.dp))
                row.forEach { char ->
                    Box(modifier = Modifier.weight(1f)) {
                        KeyItem(
                            char.toString(),
                            modifier = Modifier.fillMaxWidth(),
                            onLongClick = {
                                if (accentsMap.containsKey(char)) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    accentMenuKey = char
                                }
                            },
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onKey(char)
                            }
                        )
                        if (accentMenuKey == char) {
                            Popup(alignment = Alignment.TopCenter,
                                onDismissRequest = { accentMenuKey = null }) {
                                Surface(modifier = Modifier.padding(bottom = 8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest, shape = RoundedCornerShape(8.dp),
                                    shadowElevation = 8.dp,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder.copy(alpha = 0.3f))) {
                                    Row(modifier = Modifier.padding(4.dp)) {
                                        accentsMap[char]?.forEach { acc ->
                                            Box(
                                                modifier = Modifier.size(44.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable { onKey(acc); accentMenuKey = null },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(acc.toString(), fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold, color = Purple)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (row == "ZXCVBNM") KeyItem("⌫", Modifier.width(44.dp), BgGray) { onDelete() }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeyItem(
    text: String, modifier: Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    onLongClick: (() -> Unit)? = null, onClick: () -> Unit
) {
    Surface(
        modifier = modifier.height(44.dp).combinedClickable(onClick = onClick, onLongClick = onLongClick),
        color = color, shape = RoundedCornerShape(6.dp), shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder.copy(alpha = 0.3f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextMain)
        }
    }
}

// ── Victory screen ────────────────────────────────────────────────────────────

@Composable
fun CrosswordVictory(state: CrosswordGameState, viewModel: CrosswordViewModel, onHome: () -> Unit) {
    val stars = state.levelStars[state.level] ?: 0
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🏆", fontSize = 80.sp)
        Text("¡Excelente!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextMain)
        Text(stringResource(R.string.crw_completed), fontSize = 18.sp, color = TextGray)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { i ->
                Icon(Icons.Default.Star, null, modifier = Modifier.size(48.dp),
                    tint = if (i < stars) Gold else MaterialTheme.colorScheme.outline)
            }
        }
        Spacer(Modifier.height(32.dp))
        Surface(color = Gold.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp)) {
            Text(stringResource(R.string.crw_coins_reward, stars * 15),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                color = Gold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Spacer(Modifier.height(48.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onHome, modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Purple)) {
                Icon(Icons.Default.Home, null, tint = Purple)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.crw_menu), color = Purple, fontWeight = FontWeight.Bold)
            }
            Button(onClick = { viewModel.startLevel(state.level + 1) },
                modifier = Modifier.weight(1.4f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                shape = RoundedCornerShape(16.dp)) {
                Text(stringResource(R.string.crw_next), color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
            }
        }
    }
}
