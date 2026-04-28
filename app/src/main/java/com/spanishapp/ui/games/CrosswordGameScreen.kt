package com.spanishapp.ui.games

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

private val Purple = Color(0xFF7B2FBE)
private val BgGray = Color(0xFFF8F8FA)
private val TextMain = Color(0xFF1A1A1A)
private val TextGray = Color(0xFF8E8E93)
private val CardBorder = Color(0xFFE5E5EA)
private val Gold = Color(0xFFFF9500)
private val SuccessGreen = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrosswordGameScreen(
    navController: NavHostController,
    viewModel: CrosswordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crucigrama", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (state.showSetup) navController.popBackStack() 
                        else viewModel.resetToMenu()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
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
                            Text("${state.coins}", color = Gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BgGray)
        ) {
            when {
                state.showSetup -> CrosswordLevelSelection(state, viewModel)
                state.isGameOver -> CrosswordVictory(state, viewModel, onHome = { viewModel.resetToMenu() })
                else -> CrosswordActiveContent(state, viewModel)
            }
        }
    }
}

@Composable
fun CrosswordLevelSelection(state: CrosswordGameState, viewModel: CrosswordViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Selecciona Nivel",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextMain,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(100) { index ->
                val level = index + 1
                val stars = state.levelStars[level] ?: 0
                // For demo/sim, let's say levels are unlocked by XP or previous level completion
                val isLocked = level > (state.levelStars.size + 1) && level > 1

                LevelCell(
                    level = level,
                    stars = stars,
                    isLocked = isLocked,
                    onClick = { if (!isLocked) viewModel.startLevel(level) }
                )
            }
        }
    }
}

@Composable
fun LevelCell(level: Int, stars: Int, isLocked: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(enabled = !isLocked) { onClick() }
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(20.dp),
            color = if (isLocked) Color(0xFFE5E5EA) else Color.White,
            border = if (isLocked) null else androidx.compose.foundation.BorderStroke(2.dp, Purple),
            shadowElevation = if (isLocked) 0.dp else 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isLocked) {
                    Icon(Icons.Default.Lock, null, tint = TextGray, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        level.toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Purple
                    )
                }
            }
        }
        
        Spacer(Modifier.height(4.dp))
        
        // Stars
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(3) { i ->
                Icon(
                    Icons.Default.Star,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = if (i < stars) Gold else Color(0xFFE5E5EA)
                )
            }
        }
    }
}

@Composable
fun CrosswordActiveContent(state: CrosswordGameState, viewModel: CrosswordViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 1. Grid Area (Centered)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CrosswordGrid(state, viewModel)
        }

        // 2. Info & Controls
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White,
            shadowElevation = 12.dp
        ) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                // Hint display
                val currentWord = state.words.find { it.id == state.currentWordId }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        if (currentWord?.isVertical == true) Icons.Default.ArrowDownward else Icons.AutoMirrored.Filled.ArrowForward,
                        null,
                        tint = Purple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        currentWord?.russian ?: "Выберите ячейку",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                // Hints
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HintChip(Icons.Default.Lightbulb, "Буква (10)", state.coins >= 10, Modifier.weight(1f)) { viewModel.useHintLetter() }
                    HintChip(Icons.Default.AutoFixHigh, "Слово (50)", state.coins >= 50, Modifier.weight(1f)) { viewModel.useHintWord() }
                }

                Spacer(Modifier.height(8.dp))

                // Keyboard
                IntegratedSpanishKeyboard(
                    onKey = { viewModel.enterLetter(it) },
                    onDelete = { viewModel.deleteLetter() }
                )
            }
        }
    }
}

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
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (enabled) TextMain else TextGray)
        }
    }
}

@Composable
fun CrosswordGrid(state: CrosswordGameState, viewModel: CrosswordViewModel) {
    val cells = state.grid.keys
    if (cells.isEmpty()) return

    val minX = cells.minOf { it.first }
    val maxX = cells.maxOf { it.first }
    val minY = cells.minOf { it.second }
    val maxY = cells.maxOf { it.second }

    val cols = maxX - minX + 1
    val rows = maxY - minY + 1
    
    val maxDim = maxOf(cols, rows)
    val cellSize = when {
        maxDim <= 4 -> 64.dp
        maxDim <= 6 -> 54.dp
        maxDim <= 8 -> 42.dp
        maxDim <= 10 -> 34.dp
        else -> 28.dp
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (y in minY..maxY) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (x in minX..maxX) {
                    val char = state.grid[x to y]
                    val isCell = state.grid.containsKey(x to y)
                    
                    val isSolved = state.words.any { cw -> 
                        state.solvedWordIds.contains(cw.id) && 
                        cw.spanish.indices.any { i ->
                            val wx = if (cw.isVertical) cw.x else cw.x + i
                            val wy = if (cw.isVertical) cw.y + i else cw.y
                            wx == x && wy == y
                        }
                    }
                    
                    val isSelected = state.selectedCell == (x to y)
                    val isError = state.errors.contains(x to y)
                    val wordStart = state.words.find { it.x == x && it.y == y }

                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .clip(RoundedCornerShape(if (maxDim <= 6) 8.dp else 4.dp))
                            .background(
                                when {
                                    !isCell -> Color.Transparent
                                    isSelected -> Purple
                                    isSolved -> SuccessGreen.copy(alpha = 0.2f)
                                    else -> Color.White
                                }
                            )
                            .border(
                                1.dp,
                                when {
                                    !isCell -> Color.Transparent
                                    isSelected -> Purple
                                    isError -> Color.Red
                                    isSolved -> SuccessGreen
                                    else -> CardBorder
                                },
                                RoundedCornerShape(if (maxDim <= 6) 8.dp else 4.dp)
                            )
                            .clickable(enabled = isCell) { viewModel.onCellClick(x, y) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCell) {
                            wordStart?.let {
                                Text(
                                    it.number.toString(),
                                    modifier = Modifier.align(Alignment.TopStart).padding(2.dp),
                                    fontSize = if (maxDim <= 6) 10.sp else 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White.copy(alpha = 0.7f) else TextGray
                                )
                            }
                            
                            Text(
                                text = char?.toString() ?: "",
                                fontSize = if (maxDim <= 6) 24.sp else if (maxDim <= 8) 20.sp else 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = when {
                                    isSelected -> Color.White
                                    isError -> Color.Red
                                    isSolved -> SuccessGreen
                                    else -> TextMain
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IntegratedSpanishKeyboard(onKey: (Char) -> Unit, onDelete: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var accentMenuKey by remember { mutableStateOf<Char?>(null) }
    
    val rows = listOf(
        "QWERTYUIOP",
        "ASDFGHJKLÑ",
        "ZXCVBNM"
    )

    val accentsMap = mapOf(
        'A' to listOf('Á'),
        'E' to listOf('É'),
        'I' to listOf('Í'),
        'O' to listOf('Ó'),
        'U' to listOf('Ú', 'Ü'),
        'N' to listOf('Ñ')
    )

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                if (row == "ZXCVBNM") {
                    Spacer(Modifier.width(8.dp))
                }
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
                        
                        // Accent Popup
                        if (accentMenuKey == char) {
                            Popup(
                                alignment = Alignment.TopCenter,
                                onDismissRequest = { accentMenuKey = null }
                            ) {
                                Surface(
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    color = Color.White,
                                    shape = RoundedCornerShape(8.dp),
                                    shadowElevation = 8.dp,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                                ) {
                                    Row(modifier = Modifier.padding(4.dp)) {
                                        accentsMap[char]?.forEach { acc ->
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        onKey(acc)
                                                        accentMenuKey = null
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(acc.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Purple)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (row == "ZXCVBNM") {
                    KeyItem("⌫", Modifier.width(44.dp), BgGray) { onDelete() }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeyItem(
    text: String, 
    modifier: Modifier, 
    color: Color = Color.White, 
    onLongClick: (() -> Unit)? = null, 
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = color,
        shape = RoundedCornerShape(6.dp),
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextMain)
        }
    }
}

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
        Text("Кроссворд пройден", fontSize = 18.sp, color = TextGray)
        
        Spacer(Modifier.height(16.dp))
        
        // Stars
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { i ->
                Icon(
                    Icons.Default.Star,
                    null,
                    modifier = Modifier.size(48.dp),
                    tint = if (i < stars) Gold else Color(0xFFE5E5EA)
                )
            }
        }

        Spacer(Modifier.height(32.dp))
        
        Surface(
            color = Gold.copy(alpha = 0.1f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "+${stars * 15} монет",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                color = Gold,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
        
        Spacer(Modifier.height(48.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Home Button
            OutlinedButton(
                onClick = onHome,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Purple)
            ) {
                Icon(Icons.Default.Home, null, tint = Purple)
                Spacer(Modifier.width(8.dp))
                Text("МЕНЮ", color = Purple, fontWeight = FontWeight.Bold)
            }

            // Next Level Button
            Button(
                onClick = { viewModel.startLevel(state.level + 1) },
                modifier = Modifier.weight(1.4f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("ДАЛЕЕ", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
            }
        }
    }
}
