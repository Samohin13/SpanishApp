package com.spanishapp.ui.games

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.spanishapp.R
import com.spanishapp.domain.games.GameId
import com.spanishapp.ui.games.common.ComboBadge
import com.spanishapp.ui.games.common.ConfettiEffect
import com.spanishapp.ui.games.common.LevelCompleteSheet
import com.spanishapp.ui.games.common.LevelMapScreen
import com.spanishapp.ui.games.common.ProgressDots
import com.spanishapp.ui.games.common.rememberShakeOffset

/** Фирменный акцент Frase Loca — оранжевый градиентной иконки. */
private val ACCENT = Color(0xFFFF8A3D)
private val TRAP_RED = Color(0xFFE53935)
private val OK_GREEN = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FraseLocaScreen(
    navController: NavHostController,
    viewModel: FraseLocaViewModel = hiltViewModel()
) {
    com.spanishapp.service.TrackActivity(com.spanishapp.service.ActivityType.GAME)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val mistakesCount by viewModel.mistakesCount.collectAsStateWithLifecycle()
    val isPro by com.spanishapp.ui.games.common.rememberIsProState()

    when {
        state.showLevelMap -> {
            LevelMapScreen(
                gameId  = GameId.FRASE,
                title   = stringResource(R.string.frase_levels_title),
                accent  = ACCENT,
                manager = viewModel.levelManager,
                onBack  = { navController.popBackStack() },
                onLevelStart = { lvl -> viewModel.startLevel(lvl) },
                mistakesCount = mistakesCount,
                onMistakesPractice = { viewModel.startMistakesPractice() },
                mistakesUnit = com.spanishapp.ui.games.common.MistakesUnit.TASKS,
                isPro     = isPro,
                onPaywall = { navController.navigate("paywall") { launchSingleTop = true } },
            )
        }
        state.isGameOver -> {
            FraseLocaContent(state, viewModel, onBack = { viewModel.openLevelMap() })
            LevelCompleteSheet(
                level   = state.level,
                stars   = state.finalStars,
                percent = state.finalPercent,
                accent  = ACCENT,
                onRetry = { viewModel.startLevel(state.level) },
                onNext  = when {
                    state.isMistakesPractice && mistakesCount > 0 -> {
                        { viewModel.startMistakesPractice() }
                    }
                    !state.isMistakesPractice && state.finalStars > 0 && state.level < 100 -> {
                        if (!isPro && state.level + 1 > com.spanishapp.ui.games.common.FREE_GAME_LEVELS) {
                            { navController.navigate("paywall") { launchSingleTop = true } }
                        } else {
                            { viewModel.startLevel(state.level + 1) }
                        }
                    }
                    else -> null
                },
                onExit  = { viewModel.openLevelMap() },
                isMistakesPractice = state.isMistakesPractice,
                mistakesCorrect = state.cleanCount,
                mistakesTotal = state.totalRounds,
                mistakesPoolLeft = mistakesCount,
            )
        }
        else -> FraseLocaContent(state, viewModel, onBack = { viewModel.openLevelMap() })
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FraseLocaContent(
    state: FraseLocaState,
    viewModel: FraseLocaViewModel,
    onBack: () -> Unit
) {
    var showRules by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        if (state.isMistakesPractice) {
                            Text(stringResource(R.string.mistakes_practice_title), fontWeight = FontWeight.Bold)
                            Text(
                                "${state.currentRound.coerceAtLeast(1)} / ${state.totalRounds}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(stringResource(R.string.frase_level_of, state.level), fontWeight = FontWeight.Bold)
                            Text(
                                "${state.cefr} · ${state.themeTitle}",
                                fontSize = 11.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    // Правила игры
                    IconButton(onClick = { showRules = true }) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = stringResource(R.string.frase_rules_title),
                            tint = ACCENT,
                        )
                    }
                    // Жизни (только в обычном уровне)
                    if (!state.isMistakesPractice) {
                        Text(
                            buildString {
                                repeat(state.lives) { append("❤️") }
                                repeat(3 - state.lives) { append("🖤") }
                            },
                            fontSize = 14.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        var confettiKey by remember { mutableIntStateOf(0) }
        if (showRules) {
            AlertDialog(
                onDismissRequest = { showRules = false },
                title = { Text(stringResource(R.string.frase_rules_title), fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        stringResource(R.string.frase_rules_body),
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showRules = false }) {
                        Text(stringResource(R.string.frase_rules_ok), fontWeight = FontWeight.Bold, color = ACCENT)
                    }
                },
            )
        }
        var showNoHints by remember { mutableStateOf(false) }
        if (showNoHints) {
            AlertDialog(
                onDismissRequest = { showNoHints = false },
                title = { Text(stringResource(R.string.hint_bank_empty_title)) },
                text = { Text(stringResource(R.string.hint_bank_empty_msg)) },
                confirmButton = {
                    TextButton(onClick = { showNoHints = false }) { Text("OK") }
                },
            )
        }
        val hapticVm: com.spanishapp.ui.components.HapticPrefViewModel =
            androidx.hilt.navigation.compose.hiltViewModel()
        val hapticPercent by hapticVm.intensity.collectAsStateWithLifecycle()

        // Тактильный фидбэк: конфетти на чисто собранную фразу, tick на ошибку
        LaunchedEffect(state.answerHistory.size) {
            if (state.answerHistory.lastOrNull() == true) {
                confettiKey++
                hapticVm.vibrator.pulse(hapticPercent)
            }
        }
        LaunchedEffect(state.wrongTapCount) {
            if (state.wrongTapCount > 0) {
                hapticVm.vibrator.tick((hapticPercent * 130 / 100).coerceAtMost(100))
            }
        }

        val shakeX = rememberShakeOffset(
            trigger = state.wrongTapCount,
            isWrong = state.wrongTapCount > 0
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Шапка: счёт + раунд
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.frase_score, state.score), fontWeight = FontWeight.Bold, color = ACCENT)
                    Text("${state.currentRound} / ${state.totalRounds}", color = Color.Gray)
                }

                ProgressDots(
                    history = state.answerHistory,
                    total   = state.totalRounds,
                    accent  = ACCENT
                )

                AnimatedVisibility(visible = state.streak >= 2, enter = fadeIn(), exit = fadeOut()) {
                    ComboBadge(streak = state.streak, accentColor = ACCENT)
                }

                // Русский промпт
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            stringResource(R.string.frase_prompt_label).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            state.promptRu,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 26.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Зона сборки (с тряской при ошибке)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(x = shakeX.dp)
                        .heightIn(min = 84.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.5.dp,
                        when {
                            state.phraseSolved -> OK_GREEN
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        }
                    )
                ) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (state.placed.isEmpty()) {
                            Text(
                                stringResource(R.string.frase_tap_hint),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        state.placed.forEachIndexed { idx, tile ->
                            // Тап по поставленной плитке — вернуть в пул
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ACCENT.copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, ACCENT.copy(alpha = 0.45f)),
                                modifier = Modifier.clickable(enabled = !state.phraseSolved) {
                                    viewModel.unplaceTile(idx)
                                }
                            ) {
                                Text(
                                    tile.word,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ACCENT
                                )
                            }
                        }
                    }
                }

                // Фидбэк: ловушка / неверный порядок / фраза собрана
                when {
                    state.checkFailed && state.trapMessage == null -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = TRAP_RED.copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, TRAP_RED.copy(alpha = 0.45f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    stringResource(R.string.frase_wrong_check_title),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TRAP_RED
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    stringResource(R.string.frase_wrong_check_body),
                                    fontSize = 13.5.sp,
                                    lineHeight = 19.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    state.trapMessage != null -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = TRAP_RED.copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, TRAP_RED.copy(alpha = 0.45f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    stringResource(R.string.frase_trap_title),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TRAP_RED
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    state.trapMessage,
                                    fontSize = 13.5.sp,
                                    lineHeight = 19.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    state.phraseSolved -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = OK_GREEN.copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, OK_GREEN.copy(alpha = 0.45f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        stringResource(R.string.frase_solved),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = OK_GREEN
                                    )
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        state.tokens.joinToString(" "),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                IconButton(onClick = { viewModel.speakPhrase() }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = null,
                                        tint = OK_GREEN
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(2.dp))

                // Пул плиток
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.tiles.forEach { tile ->
                        val alpha = if (tile.used) 0.22f else 1f
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = alpha),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f * alpha)
                            ),
                            shadowElevation = if (tile.used) 0.dp else 2.dp,
                            modifier = Modifier.clickable(enabled = !tile.used && !state.phraseSolved) {
                                viewModel.tapTile(tile.id)
                            }
                        ) {
                            Text(
                                tile.word,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                            )
                        }
                    }
                }

                // ── Подсказка + Проверить ───────────────────
                if (!state.phraseSolved && !state.isGameOver) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!state.isMistakesPractice) {
                            val hintBalance by viewModel.hintBalance.collectAsStateWithLifecycle()
                            OutlinedButton(
                                onClick = { viewModel.useHint { showNoHints = true } },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, ACCENT.copy(alpha = 0.5f)),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Text(
                                    stringResource(R.string.frase_hint_word) + "  💡$hintBalance",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ACCENT
                                )
                            }
                        }
                        Button(
                            onClick = { viewModel.checkPhrase() },
                            enabled = state.placed.size == state.tokens.size,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ACCENT)
                        ) {
                            Text(
                                stringResource(R.string.frase_check),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(60.dp))
            }

            ConfettiEffect(trigger = confettiKey)
        }
    }
}
