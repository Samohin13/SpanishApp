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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.spanishapp.R
import com.spanishapp.domain.games.GameId
import com.spanishapp.ui.games.common.LevelCompleteSheet
import com.spanishapp.ui.games.common.LevelMapScreen
import com.spanishapp.ui.adaptive.adaptiveContentWidth
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val ACCENT = Color(0xFFFF9500)
private val BgGray
    @Composable get() = MaterialTheme.colorScheme.background
private val TextMain
    @Composable get() = MaterialTheme.colorScheme.onSurface
private val TextGray
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val CardBorder
    @Composable get() = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
private val CardSurface
    @Composable get() = MaterialTheme.colorScheme.surfaceContainerHighest
private val Green = Color(0xFF4CAF50)
private val Red   = Color(0xFFF44336)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PalabraMaestraScreen(
    navController: NavHostController,
    viewModel: PalabraMaestraViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.showLevelMap -> {
            LevelMapScreen(
                gameId  = GameId.PALABRA,
                title   = stringResource(R.string.palabra_levels_title),
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
                        Text(stringResource(R.string.palabra_level_of, state.level), fontWeight = FontWeight.Bold)
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
        // v1.13.2: на планшете контент центрируется (cap 720dp)
        // чтобы UI не растягивался уродливо на 1280dp.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BgGray),
            contentAlignment = Alignment.TopCenter,
        ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveContentWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Прогресс
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.palabra_word_of, state.currentIndex + 1, state.questions.size),
                    color = ACCENT, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.palabra_score, state.score), color = ACCENT, fontWeight = FontWeight.Bold)
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
                    // Раньше тут был isWrongAuto — мгновенно подсвечивал каждую
                    // неправильную букву красным. Это превращало игру в exploit:
                    // юзер тыкал все буквы по очереди и собирал слово по цветам,
                    // вместо того чтобы реально думать. Цвет показываем только
                    // ПОСЛЕ полной проверки (когда слово собрано целиком и
                    // ViewModel выставил isChecked).
                    // Разрешаем тап для удаления буквы или сброса всего слова
                    // (когда auto-validate пометил всё как неправильное).
                    val tapEnabled = letter != null && (
                        !q.isChecked ||
                        (q.isChecked && q.isCorrect == false && state.isAutoValidate)
                    )
                    // v1.13.4: ячейки слова крупнее на планшете
                    val isWideCell = com.spanishapp.ui.adaptive.isWideScreen()
                    val cellSize = if (isWideCell) 64.dp else 45.dp
                    val cellFont = if (isWideCell) 30.sp else 22.sp
                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(cellSize)
                            .clickable(enabled = tapEnabled) {
                                viewModel.removeLetter(idx)
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            q.isChecked && q.isCorrect == true  -> Green
                            q.isChecked && q.isCorrect == false -> Red
                            letter != null -> ACCENT.copy(alpha = 0.07f)
                            else -> CardSurface
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (letter != null) ACCENT else CardBorder
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                letter?.char?.uppercase() ?: "",
                                fontSize = cellFont,
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
                        // v1.13.4: shuffled буквы крупнее на планшете
                        val isWideShuf = com.spanishapp.ui.adaptive.isWideScreen()
                        val shufSize = if (isWideShuf) 68.dp else 50.dp
                        val shufFont = if (isWideShuf) 30.sp else 24.sp
                        Surface(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(shufSize)
                                .clickable(enabled = !isUsed) { viewModel.onLetterClick(letter) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isUsed) BgGray else CardSurface,
                            shadowElevation = if (isUsed) 0.dp else 2.dp,
                            border = if (isUsed) null
                                     else androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    if (isUsed) "" else letter.char.uppercase(),
                                    fontSize = shufFont,
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
                    Text(stringResource(R.string.palabra_next), fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                            Text(stringResource(R.string.palabra_check), fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HintButton(Icons.Default.Translate, stringResource(R.string.palabra_hint_translation),
                            Modifier.weight(1f)) { viewModel.showTranslation() }
                        HintButton(Icons.AutoMirrored.Filled.VolumeUp, stringResource(R.string.palabra_hint_audio),
                            Modifier.weight(1f)) { viewModel.playAudio() }
                    }
                    if (state.params.hintsAllowed > 0) {
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            HintButton(Icons.Default.Lightbulb, stringResource(R.string.palabra_hint_first_letter),
                                Modifier.weight(1f)) { viewModel.useFirstLetterHint() }
                            HintButton(Icons.Default.MenuBook, stringResource(R.string.palabra_hint_rule),
                                Modifier.weight(1f)) { viewModel.showRuleHint() }
                        }
                    }
                }

                if (state.translationHintVisible) {
                    Text(
                        stringResource(R.string.palabra_translation_label, q.word.russian),
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
                            fontSize = 15.sp,
                            color = TextMain,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
        } // close adaptive Box
    }
}

@Composable
private fun HintButton(icon: ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    // v1.13.4: крупнее на планшете
    val isWide = com.spanishapp.ui.adaptive.isWideScreen()
    val btnHeight = if (isWide) 64.dp else 44.dp
    val iconSize = if (isWide) 26.dp else 18.dp
    val fontSp = if (isWide) 15.sp else 11.sp
    val gap = if (isWide) 8.dp else 4.dp

    Surface(
        modifier = modifier
            .height(btnHeight)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(if (isWide) 14.dp else 10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        color = CardSurface
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(icon, null, tint = ACCENT, modifier = Modifier.size(iconSize))
            Spacer(Modifier.width(gap))
            Text(label, fontSize = fontSp, fontWeight = FontWeight.Medium, color = TextGray)
        }
    }
}
