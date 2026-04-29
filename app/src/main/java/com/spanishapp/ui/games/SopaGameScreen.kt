package com.spanishapp.ui.games

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

private val Purple = Color(0xFF7B2FBE)
private val BgGray = Color(0xFFF8F8FA)
private val TextMain = Color(0xFF1A1A1A)
private val CardBorder = Color(0xFFE5E5EA)
private val Green = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SopaGameScreen(
    navController: NavHostController,
    viewModel: SopaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sopa de Letras", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    if (!state.showSetup && !state.isGameOver) {
                        IconButton(onClick = { viewModel.useHint() }, enabled = state.score >= 30) {
                            Icon(Icons.Default.Lightbulb, "Hint", tint = if (state.score >= 30) Purple else Color.Gray)
                        }
                    }
                }
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
                state.showSetup -> SopaSetup(viewModel)
                state.isGameOver -> SopaResultScreen(state, onFinish = { navController.popBackStack() })
                else -> SopaGameContent(state, viewModel)
            }
        }
    }
}

@Composable
fun SopaSetup(viewModel: SopaViewModel) {
    var selectedLevel by remember { mutableStateOf("A1") }
    var selectedDifficulty by remember { mutableStateOf(SopaDifficulty.PRINCIPIANTE) }
    var isGhostMode by remember { mutableStateOf(false) }
    var hasTimer by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Настройка поиска", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextMain)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Уровень сложности:", color = Purple, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("A1", "A2", "B1", "B2").forEach { lvl ->
                    FilterChip(
                        selected = selectedLevel == lvl,
                        onClick = { selectedLevel = lvl },
                        label = { Text(lvl) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Purple, selectedLabelColor = Color.White)
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Размер поля:", color = Purple, fontWeight = FontWeight.SemiBold)
            SopaDifficulty.entries.forEach { diff ->
                val isSelected = selectedDifficulty == diff
                Surface(
                    onClick = { selectedDifficulty = diff },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Purple.copy(alpha = 0.1f) else Color.White,
                    border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Purple else CardBorder)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = isSelected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = Purple))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(diff.title, fontWeight = FontWeight.Bold)
                            Text("${diff.size}x${diff.size}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Игра на время", fontWeight = FontWeight.Bold)
                        Text(if (hasTimer) "Таймер включен" else "Без ограничений", fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = hasTimer,
                        onCheckedChange = { hasTimer = it },
                        thumbContent = { Icon(if (hasTimer) Icons.Default.Timer else Icons.Default.TimerOff, null, Modifier.size(16.dp)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Purple)
                    )
                }
                HorizontalDivider(color = BgGray)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Modo Fantasma", fontWeight = FontWeight.Bold)
                        Text("Скрывает список слов", fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isGhostMode,
                        onCheckedChange = { isGhostMode = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Purple)
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { viewModel.startGame(selectedLevel, selectedDifficulty, isGhostMode, hasTimer) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple)
        ) {
            Text("EMPEZAR", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SopaGameContent(state: SopaGameState, viewModel: SopaViewModel) {
    val haptic = LocalHapticFeedback.current
    var gridSize by remember { mutableStateOf(IntSize.Zero) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Info Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Очки: ${state.score}", fontWeight = FontWeight.Bold, color = Purple)
                if (state.combo > 1) Text("Combo x${state.combo}!", color = Color(0xFFF44336), fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
            if (state.hasTimer) {
                Surface(
                    color = if (state.timeLeftSeconds < 30) Color.Red.copy(alpha = 0.1f) else Purple.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("${state.timeLeftSeconds}", color = if (state.timeLeftSeconds < 30) Color.Red else Purple, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // GRID WITH SWIPE LOGIC
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .onGloballyPositioned { gridSize = it.size }
                .pointerInput(state.difficulty.size) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val cell = getCellFromOffset(offset, gridSize, state.difficulty.size)
                            if (cell != null) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.onDragStart(cell.first, cell.second)
                            }
                        },
                        onDrag = { change, _ ->
                            val cell = getCellFromOffset(change.position, gridSize, state.difficulty.size)
                            if (cell != null) viewModel.onDragUpdate(cell.first, cell.second)
                        },
                        onDragEnd = { viewModel.onDragEnd() }
                    )
                }
        ) {
            val size = state.difficulty.size
            val cellSize = if (gridSize.width > 0) gridSize.width.toFloat() / size else 0f

            Column {
                for (r in 0 until size) {
                    Row {
                        for (c in 0 until size) {
                            val char = state.grid[r][c]
                            val isSelected = state.selectedCells.contains(r to c)
                            val foundWord = state.foundWords.find { it.cells.contains(r to c) }
                            val isHinted = state.hintCells.contains(r to c)
                            
                            Box(
                                modifier = Modifier
                                    .size(with(androidx.compose.ui.platform.LocalDensity.current) { cellSize.toDp() })
                                    .padding(1.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when {
                                            isSelected -> Purple.copy(alpha = 0.3f)
                                            foundWord != null -> foundWord.color.copy(alpha = 0.2f)
                                            isHinted -> Color.Yellow.copy(alpha = 0.5f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .border(
                                        if (isHinted) androidx.compose.foundation.BorderStroke(2.dp, Color.Yellow) else androidx.compose.foundation.BorderStroke(0.dp, Color.Transparent),
                                        RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    char.toString(),
                                    fontSize = if (size > 12) 11.sp else 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = when {
                                        foundWord != null -> foundWord.color.copy(alpha = 0.8f)
                                        isHinted -> Color.Black
                                        else -> TextMain
                                    },
                                    textDecoration = if (foundWord != null) TextDecoration.LineThrough else null
                                )
                            }
                        }
                    }
                }
            }
            
            // Snake selection line
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (state.selectedCells.size > 1) {
                    for (i in 0 until state.selectedCells.size - 1) {
                        val start = state.selectedCells[i]
                        val end = state.selectedCells[i+1]
                        drawLine(
                            color = Purple.copy(alpha = 0.4f),
                            start = Offset((start.second + 0.5f) * cellSize, (start.first + 0.5f) * cellSize),
                            end = Offset((end.second + 0.5f) * cellSize, (end.first + 0.5f) * cellSize),
                            strokeWidth = 12f
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Word List below the grid in columns
        if (!state.isGhostMode) {
            Surface(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                val scrollState = rememberScrollState()
                Column(modifier = Modifier.padding(12.dp).verticalScroll(scrollState)) {
                    Text("Найдите слова:", color = Purple, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    
                    val words = state.words
                    val columns = 2
                    val rows = (words.size + columns - 1) / columns
                    
                    for (r in 0 until rows) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (c in 0 until columns) {
                                val index = r + c * rows
                                if (index < words.size) {
                                    val word = words[index]
                                    Column(
                                        modifier = Modifier.weight(1f).padding(vertical = 4.dp, horizontal = 4.dp)
                                    ) {
                                        Text(
                                            word.word,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (word.isFound) word.color else TextMain,
                                            textDecoration = if (word.isFound) TextDecoration.LineThrough else null,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            word.translation,
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
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = { viewModel.clearSelection() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, null)
            Spacer(Modifier.width(8.dp))
            Text("Сбросить выделение")
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

@Composable
fun SopaResultScreen(state: SopaGameState, onFinish: () -> Unit) {
    val isWin = state.words.all { it.isFound }
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(if (isWin) "🎉" else "⏰", fontSize = 80.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            if (isWin) "¡Enhorabuena!" else "¡Se acabó el tiempo!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextMain,
            textAlign = TextAlign.Center
        )
        Text("Ваш результат: ${state.score}", fontSize = 20.sp, color = Purple, fontWeight = FontWeight.Bold)
        
        Spacer(Modifier.height(32.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                state.words.forEach { word ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(word.word, fontWeight = FontWeight.Bold, color = if (word.isFound) word.color else Color.Gray, textDecoration = if (word.isFound) TextDecoration.LineThrough else null)
                        Text(word.translation, color = Color.Gray, fontSize = 12.sp)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = BgGray)
                }
            }
        }

        Spacer(Modifier.height(40.dp))
        
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("В МЕНЮ", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
