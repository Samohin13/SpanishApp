package com.spanishapp.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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

private val ACCENT = Color(0xFFE040FB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedGameScreen(
    navController: NavHostController,
    viewModel: SpeedViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val haptic = com.spanishapp.ui.components.rememberCheckedHaptic()

    when {
        state.showLevelMap -> {
            LevelMapScreen(
                gameId  = GameId.SPEED,
                title   = stringResource(R.string.speed_levels_title),
                accent  = ACCENT,
                manager = viewModel.levelManager,
                onBack  = { navController.popBackStack() },
                onLevelStart = { lvl -> viewModel.startLevel(lvl) }
            )
        }
        state.isGameOver -> {
            SpeedGameContent(state, viewModel, haptic, onBack = { viewModel.openLevelMap() })
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
        else -> SpeedGameContent(state, viewModel, haptic,
            onBack = { viewModel.openLevelMap() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpeedGameContent(
    state: SpeedPremiumState,
    viewModel: SpeedViewModel,
    haptic: HapticFeedback,
    onBack: () -> Unit
) {
    LaunchedEffect(state.timeLeft) {
        if (state.timeLeft in 0.01f..0.2f && !state.isGameOver) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.speed_level_of, state.level), fontWeight = FontWeight.Bold)
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
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Шапка: серия и XP ────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalFireDepartment, null, tint = Color(0xFFFF9500))
                    Text("${state.streak}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    if (state.multiplier > 1f) {
                        Text(" ×${"%.1f".format(state.multiplier)}",
                            color = Color(0xFFFF9500), fontWeight = FontWeight.Bold)
                    }
                }
                Text(stringResource(R.string.speed_round_of, state.currentRound, state.totalRounds),
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.speed_xp, state.score), fontWeight = FontWeight.Bold, color = ACCENT)
            }

            Spacer(Modifier.height(16.dp))

            // ── Таймер ───────────────────────────────────────
            if (state.params.timePerRoundSec > 0f) {
                LinearProgressIndicator(
                    progress = { state.timeLeft },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = if (state.timeLeft < 0.3f) Color.Red else ACCENT,
                    trackColor = MaterialTheme.colorScheme.outline
                )
            } else {
                Text(stringResource(R.string.speed_no_timer), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.weight(0.5f))

            // ── Слово ────────────────────────────────────────
            state.currentWord?.let { word ->
                Text(
                    word.spanish,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 48.sp
                )
            }

            Spacer(Modifier.weight(0.5f))

            // ── Варианты ─────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.options.forEach { option ->
                    val isCorrectShown = state.lastCorrect != null &&
                                          option == state.currentWord?.russian
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clickable(enabled = state.lastCorrect == null) {
                                viewModel.submitAnswer(option)
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = when {
                            isCorrectShown -> Color(0xFF4CAF50).copy(alpha = 0.20f)
                            else            -> MaterialTheme.colorScheme.surface
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCorrectShown) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(option, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
