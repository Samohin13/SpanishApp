package com.spanishapp.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Translate
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
import com.spanishapp.domain.games.GameId
import com.spanishapp.ui.games.common.LevelCompleteSheet
import com.spanishapp.ui.games.common.LevelMapScreen

private val ACCENT = Color(0xFFFF9500)
private val BgGray = Color(0xFFF8F8FA)
private val TextMain = Color(0xFF1A1A1A)
private val TextGray = Color(0xFF8E8E93)
private val CardBorder = Color(0xFFE5E5EA)
private val Green = Color(0xFF4CAF50)
private val Red   = Color(0xFFF44336)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PalabraMaestraScreen(
    navController: NavHostController,
    viewModel: PalabraMaestraViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    when {
        state.showLevelMap -> {
            LevelMapScreen(
                gameId  = GameId.PALABRA,
                title   = "Palabra Maestra · уровни",
                accent  = ACCENT,
                manager = viewModel.levelManager,
                onBack  = { navController.popBackStack() },
                onLevelStart = { lvl -> viewModel.startLevel(lvl) }
            )
        }
        state.isGameOver -> {
            PalabraActiveGame(state, viewModel, onBack = { viewModel.openLevelMap() })
            LevelCompleteSheet(
                level   = state.level,
                stars   = state.finalStars,
                percent = state.finalPercent,
                accent  = ACCENT,
                onRetry = { viewModel.startLevel(state.level) },
                onNext  = if (state.finalStars > 0 && state.level < 100)
                              { { viewModel.startLevel(state.level + 1) } }
                          else null,
                onExit  = { viewModel.openLevelMap() }
            )
        }
        else -> PalabraActiveGame(state, viewModel, onBack = { viewModel.openLevelMap() })
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PalabraActiveGame(
    state: PalabraState,
    viewModel: PalabraMaestraViewModel,
    onBack: () -> Unit
) {
    val q = state.questions.getOrNull(state.currentIndex) ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Уровень ${state.level} / 100", fontWeight = FontWeight.Bold)
                        Text("${state.params.cefr.joinToString("+")} · ${state.params.mode.name.lowercase()}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Прогресс
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Слово ${state.currentIndex + 1}/${state.questions.size}",
                    color = ACCENT, fontWeight = FontWeight.Bold)
                Text("Очки: ${state.score}", color = ACCENT, fontWeight = FontWeight.Bold)
            }

            LinearProgressIndicator(
                progress = { (state.currentIndex + 1).toFloat() / state.questions.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = ACCENT,
                trackColor = CardBorder
            )

            Spacer(Modifier.height(16.dp))

            // ── Слоты для собранного слова ───────────────────
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                q.assembledLetters.forEachIndexed { idx, letter ->
                    val isWrongAuto = state.isAutoValidate && letter != null &&
                        letter.char.lowercase() != q.targetWord.getOrNull(idx)?.toString()?.lowercase()
                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(45.dp)
                            .clickable(enabled = !q.isChecked && letter != null) {
                                viewModel.removeLetter(idx)
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            q.isChecked && q.isCorrect == true  -> Green
                            q.isChecked && q.isCorrect == false -> Red
                            isWrongAuto -> Red.copy(alpha = 0.4f)
                            letter != null -> ACCENT.copy(alpha = 0.07f)
                            else -> Color.White
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (letter != null) ACCENT else CardBorder
                        )
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

            Spacer(Modifier.height(28.dp))

            // ── Перемешанные буквы ───────────────────────────
            if (!q.isChecked) {
                FlowRow(
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
                            border = if (isUsed) null
                                     else androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    if (isUsed) "" else letter.char.uppercase(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ACCENT
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    q.targetWord.uppercase(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (q.isCorrect == true) Green else Red
                )
                Text(q.word.russian, fontSize = 18.sp, color = TextGray)

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.nextQuestion() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ACCENT)
                ) {
                    Text("ДАЛЕЕ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Подсказки и проверка ─────────────────────────
            if (!q.isChecked) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!state.isAutoValidate && q.assembledLetters.all { it != null }) {
                        Button(
                            onClick = { viewModel.checkWord() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ACCENT),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("ПРОВЕРИТЬ", fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HintButton(Icons.Default.Translate, "Перевод",
                            Modifier.weight(1f)) { viewModel.showTranslation() }
                        HintButton(Icons.AutoMirrored.Filled.VolumeUp, "Аудио",
                            Modifier.weight(1f)) { viewModel.playAudio() }
                    }
                    if (state.params.hintsAllowed > 0) {
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            HintButton(Icons.Default.Lightbulb, "1-я буква",
                                Modifier.weight(1f)) { viewModel.useFirstLetterHint() }
                            HintButton(Icons.Default.MenuBook, "Правило",
                                Modifier.weight(1f)) { viewModel.showRuleHint() }
                        }
                    }
                }

                if (state.translationHintVisible) {
                    Text(
                        "Перевод: ${q.word.russian}",
                        modifier = Modifier.padding(top = 14.dp),
                        color = ACCENT,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                state.ruleHint?.let {
                    Surface(
                        modifier = Modifier.padding(top = 14.dp),
                        color = ACCENT.copy(alpha = 0.07f),
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

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HintButton(icon: ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        color = Color.White
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(icon, null, tint = ACCENT, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextGray)
        }
    }
}
