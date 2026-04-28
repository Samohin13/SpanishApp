package com.spanishapp.ui.games

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

private val Purple = Color(0xFF7B2FBE)
private val BgGray = Color(0xFFF8F8FA)
private val TextMain = Color(0xFF1A1A1A)
private val TextGray = Color(0xFF8E8E93)
private val CardBorder = Color(0xFFE5E5EA)
private val Green = Color(0xFF4CAF50)
private val Red = Color(0xFFF44336)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PalabraMaestraScreen(
    navController: NavHostController,
    viewModel: PalabraMaestraViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Palabra Maestra", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (state.showSetup) navController.popBackStack() 
                        else viewModel.reset() 
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
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
                state.showSetup -> PalabraSetup(state, viewModel)
                state.isGameOver -> PalabraResults(state, onFinish = { navController.popBackStack() })
                else -> PalabraActiveGame(state, viewModel)
            }
        }
    }
}

@Composable
fun PalabraSetup(state: PalabraState, viewModel: PalabraMaestraViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Выберите уровень сложности",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextMain,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        PalabraLevel.values().forEach { level ->
            val isSelected = state.selectedLevel == level
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { viewModel.setLevel(level) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) Purple.copy(alpha = 0.1f) else Color.White,
                border = androidx.compose.foundation.BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) Purple else CardBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Purple else BgGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            level.tag,
                            color = if (isSelected) Color.White else Purple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(level.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextMain)
                        Text(level.desc, fontSize = 12.sp, color = TextGray)
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { viewModel.startGame() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple)
        ) {
            Text("НАЧАТЬ ИГРУ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PalabraActiveGame(state: PalabraState, viewModel: PalabraMaestraViewModel) {
    val q = state.questions.getOrNull(state.currentIndex) ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Слово ${state.currentIndex + 1}/${state.questions.size}", color = Purple, fontWeight = FontWeight.Bold)
            Text("Очки: ${state.score}", color = Purple, fontWeight = FontWeight.Bold)
        }
        
        LinearProgressIndicator(
            progress = { (state.currentIndex + 1).toFloat() / state.questions.size },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Purple,
            trackColor = CardBorder
        )

        Spacer(Modifier.height(24.dp))

        // Assembled slots
        PalabraFlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            q.assembledLetters.forEachIndexed { index, letter ->
                Surface(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(45.dp)
                        .clickable(enabled = !q.isChecked && letter != null) { viewModel.removeLetter(index) },
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        q.isChecked && q.isCorrect == true -> Green
                        q.isChecked && q.isCorrect == false -> Red
                        letter != null -> {
                            val isAutoValidate = state.selectedLevel == PalabraLevel.A1 || state.selectedLevel == PalabraLevel.A2
                            if (isAutoValidate && letter.char.lowercase() != q.targetWord.getOrNull(index)?.toString()?.lowercase()) {
                                Red.copy(alpha = 0.4f)
                            } else {
                                Purple.copy(alpha = 0.05f)
                            }
                        }
                        else -> Color.White
                    },
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (letter != null) Purple else CardBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            letter?.char?.uppercase() ?: "",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (q.isChecked) Color.White else TextMain
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Shuffled letters
        if (!q.isChecked) {
            PalabraFlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                q.shuffledLetters.forEach { letter ->
                    val isUsed = letter.isUsed
                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(50.dp)
                            .clickable(enabled = !isUsed) { viewModel.onLetterClick(letter) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isUsed) BgGray else Color.White,
                        shadowElevation = if (isUsed) 0.dp else 2.dp,
                        border = if (isUsed) null else androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                if (isUsed) "" else letter.char.uppercase(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Purple
                            )
                        }
                    }
                }
            }
        } else {
            // Correct word display
            Text(
                q.targetWord.uppercase(),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (q.isCorrect == true) Green else Red
            )
            Text(q.word.russian, fontSize = 18.sp, color = TextGray)
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = { viewModel.nextQuestion() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                Text("ДАЛЕЕ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }

        Spacer(Modifier.height(40.dp))

        // Hints and Actions
        if (!q.isChecked) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Manual check button
                if (q.assembledLetters.all { it != null }) {
                    Button(
                        onClick = { viewModel.checkWord() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Purple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ПРОВЕРИТЬ", fontWeight = FontWeight.Bold)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HintButton(Icons.Default.Translate, "Перевод", Modifier.weight(1f)) { viewModel.showTranslation() }
                    HintButton(Icons.Default.VolumeUp, "Аудио", Modifier.weight(1f)) { viewModel.playAudio() }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HintButton(Icons.Default.Lightbulb, "Первая буква", Modifier.weight(1f)) { viewModel.useFirstLetterHint() }
                    HintButton(Icons.Default.MenuBook, "Правило", Modifier.weight(1f)) { viewModel.showRuleHint() }
                }
            }
            
            if (state.translationHintVisible) {
                Text(
                    "Перевод: ${q.word.russian}",
                    modifier = Modifier.padding(top = 16.dp),
                    color = Purple,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            state.ruleHint?.let {
                Surface(
                    modifier = Modifier.padding(top = 16.dp),
                    color = Purple.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        it,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp,
                        color = TextMain,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun HintButton(icon: ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(44.dp).clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        color = Color.White
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(icon, null, tint = Purple, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextGray)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PalabraResults(state: PalabraState, onFinish: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🏆", fontSize = 80.sp)
        Text("Palabra Maestra", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextMain)
        Text("Уровень: ${state.selectedLevel.title}", fontSize = 16.sp, color = TextGray)
        
        Spacer(Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            PalabraResultStat("Очки", state.score.toString())
            PalabraResultStat("Precision", state.precisionCount.toString())
        }
        
        Spacer(Modifier.height(48.dp))
        
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

@Composable
fun PalabraResultStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Purple)
        Text(label, fontSize = 14.sp, color = TextGray)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PalabraFlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}
