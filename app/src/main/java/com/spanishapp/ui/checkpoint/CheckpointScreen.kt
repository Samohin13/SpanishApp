package com.spanishapp.ui.checkpoint

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.spanishapp.R
import com.spanishapp.domain.checkpoint.*
import com.spanishapp.ui.components.rememberCheckedHaptic
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBarsPadding

/**
 * v1.22.18: изображения вшиты в APK (drawable). Раньше грузились с Unsplash
 * runtime и тормозили + требовали интернет. Теперь instant load offline.
 * Все 32 изображения занимают ~1.5 MB в APK.
 */
private fun sceneImageRes(cpId: String): Int = when (cpId) {
    "cp1"  -> R.drawable.cp_scene_01
    "cp2"  -> R.drawable.cp_scene_02
    "cp3"  -> R.drawable.cp_scene_03
    "cp4"  -> R.drawable.cp_scene_04
    "cp5"  -> R.drawable.cp_scene_05
    "cp6"  -> R.drawable.cp_scene_06
    "cp7"  -> R.drawable.cp_scene_07
    "cp8"  -> R.drawable.cp_scene_08
    "cp9"  -> R.drawable.cp_scene_09
    "cp10" -> R.drawable.cp_scene_10
    "cp11" -> R.drawable.cp_scene_11
    "cp12" -> R.drawable.cp_scene_12
    "cp13" -> R.drawable.cp_scene_13
    "cp14" -> R.drawable.cp_scene_14
    "cp15" -> R.drawable.cp_scene_15
    "cp16" -> R.drawable.cp_scene_16
    else   -> R.drawable.cp_scene_01
}

private fun npcImageRes(npcId: String): Int = when (npcId) {
    "carlos"         -> R.drawable.npc_carlos
    "sra_lopez"      -> R.drawable.npc_sra_lopez
    "diego"          -> R.drawable.npc_diego
    "sergio"         -> R.drawable.npc_sergio
    "dra_martinez"   -> R.drawable.npc_dra_martinez
    "carmen"         -> R.drawable.npc_carmen
    "lucia"          -> R.drawable.npc_lucia
    "pablo"          -> R.drawable.npc_pablo
    "carmen_rec"     -> R.drawable.npc_carmen_rec
    "andres"         -> R.drawable.npc_andres
    "marta"          -> R.drawable.npc_marta
    "hans"           -> R.drawable.npc_hans
    "ana"            -> R.drawable.npc_ana
    "tia_rosa"       -> R.drawable.npc_tia_rosa
    "director_ramon" -> R.drawable.npc_director_ramon
    "ensemble"       -> R.drawable.npc_ensemble
    else             -> R.drawable.npc_carlos
}

private val OrangePrimary = Color(0xFFFF6B1A)
private val OrangePrimary2 = Color(0xFFFF8533)
private val RedDanger = Color(0xFFE54848)
private val GreenSuccess = Color(0xFF2EB872)
private val AccentYellow = Color(0xFFF4B400)

/**
 * Главный экран чекпоинта. Маршрутизирует по uiState:
 *  Intro → стартовый экран
 *  Playing → раунд с одним из 6 форматов
 *  Finished → результат
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckpointScreen(
    navController: NavHostController,
    checkpointId: String,
    viewModel: CheckpointViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(checkpointId) {
        viewModel.load(checkpointId)
    }

    when (val s = state) {
        is CheckpointUiState.Loading -> LoadingView()
        is CheckpointUiState.Error -> ErrorView(s.message) { navController.popBackStack() }
        is CheckpointUiState.Intro -> IntroView(
            data = s.data,
            onStart = { viewModel.startSession() },
            onBack = { navController.popBackStack() },
        )
        is CheckpointUiState.Playing -> PlayingView(
            state = s.state,
            viewModel = viewModel,
            onSubmit = { viewModel.submit(it) },
            onReplayAudio = { viewModel.replayNpcLine() },
            onBack = { navController.popBackStack() },
        )
        is CheckpointUiState.Finished -> ResultView(
            state = s.state,
            onExit = { navController.popBackStack() },
            onRetry = { viewModel.load(checkpointId) },
        )
    }
}

// ════════════════════════════════════════════════════════════════════
//  STATE VIEWS
// ════════════════════════════════════════════════════════════════════

@Composable
private fun LoadingView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorView(message: String, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = MaterialTheme.colorScheme.error, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onBack) { Text("Назад") }
        }
    }
}

@Composable
private fun IntroView(
    data: CheckpointData,
    onStart: () -> Unit,
    onBack: () -> Unit,
) {
    val scroll = rememberScrollState()
    val haptic = rememberCheckedHaptic()
    val ctx = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scroll)
        ) {
            // ── Hero image (фото сцены + gradient overlay) ─────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(ctx)
                        .data(sceneImageRes(data.id))
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                // Тёмный градиент снизу для читаемости
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.85f),
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY,
                            )
                        ),
                )
                // Back button круглый с блюр-фоном
                Surface(
                    modifier = Modifier
                        .padding(top = 14.dp, start = 14.dp)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .size(40.dp),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.55f),
                    onClick = onBack,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                // Бейдж «ЧЕКПОИНТ A1»
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = OrangePrimary,
                    modifier = Modifier.padding(bottom = 12.dp),
                ) {
                    Text(
                        "🏁 ЧЕКПОИНТ ${data.cefr}",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.4.sp,
                    )
                }

                // Title
                Text(
                    data.titleRu,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.3).sp,
                    lineHeight = 32.sp,
                )
                Spacer(Modifier.height(10.dp))

                // Description
                Text(
                    data.descriptionRu,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 21.sp,
                )

                // Stakes (красная плашка) — точно как в HTML
                if (data.stakesRu.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(RedDanger.copy(alpha = 0.12f))
                            .padding(start = 0.dp),
                    ) {
                        // Border-left визуализация
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .fillMaxHeight()
                                .background(RedDanger),
                        )
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                "ВНИМАНИЕ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = RedDanger,
                                letterSpacing = 1.2.sp,
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(data.stakesRu, fontSize = 13.sp, lineHeight = 19.sp)
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Info card (как в HTML)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                        InfoRow(icon = "📋", text = "${data.rounds.size} заданий · ~${data.rounds.size * 35 / 60} минут")
                        InfoRowDivider()
                        InfoRow(icon = "👤", text = "${data.npc.name}, ${data.npc.roleRu.lowercase()}")
                        InfoRowDivider()
                        InfoRow(icon = "🚩", text = "Pass: ${data.thresholds.bronzePercent}%")
                        InfoRowDivider()
                        InfoRow(icon = "🎁", text = "+${data.rewards.bronzeXp} XP · ${data.rewards.badgeNameRu}")
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Кнопка «Начать →» оранжевая gradient
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onStart()
                        },
                    color = Color.Transparent,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(listOf(OrangePrimary, OrangePrimary2))
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Начать",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.4.sp,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("→", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun InfoRow(icon: String, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, fontSize = 18.sp, modifier = Modifier.width(28.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun InfoRowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
    )
}

@Composable
private fun PlayingView(
    state: CheckpointState,
    viewModel: CheckpointViewModel,
    onSubmit: (String) -> Unit,
    onReplayAudio: () -> Unit,
    onBack: () -> Unit,
) {
    val round = state.currentRound ?: return
    val haptic = rememberCheckedHaptic()
    val ctx = LocalContext.current
    var answered by remember(state.currentRoundIndex) { mutableStateOf(false) }
    var userAnswer by remember(state.currentRoundIndex) { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // ── Scene top (фото + overlay) ───────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(ctx)
                    .data(sceneImageRes(state.data.id))
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            // Затемнение
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.45f),
                                Color.Black.copy(alpha = 0.15f),
                                Color.Black.copy(alpha = 0.85f),
                            )
                        )
                    ),
            )
            // Back button — круглый blur (с padding под статус-бар)
            Surface(
                modifier = Modifier
                    .padding(top = 12.dp, start = 12.dp)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .size(36.dp),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.55f),
                onClick = onBack,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
            // Sympathy stars (top-right)
            Surface(
                modifier = Modifier
                    .padding(top = 12.dp, end = 12.dp)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .align(Alignment.TopEnd),
                shape = RoundedCornerShape(14.dp),
                color = Color.Black.copy(alpha = 0.55f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "★".repeat(state.sympathyStars) + "☆".repeat(5 - state.sympathyStars),
                        color = AccentYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${state.sympathyStars}/5",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            // Place + round (bottom-left)
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(14.dp)) {
                Text(
                    state.data.scene.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp,
                )
                Text(
                    "Раунд ${state.currentRoundIndex + 1} / ${state.totalRounds}",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                )
            }
            // Progress dots (bottom-right)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(14.dp)
                    .width(100.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                repeat(state.totalRounds) { i ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                when {
                                    i < state.currentRoundIndex -> GreenSuccess
                                    i == state.currentRoundIndex -> Color.White
                                    else -> Color.White.copy(alpha = 0.32f)
                                }
                            )
                    )
                }
            }
        }

        // NPC bubble — с круглым аватаром и orange border
        if (round.npcLineEs != null && !round.audioOnly) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    // v1.22.17: увеличен аватар 44dp → 64dp, текст 16sp → 20sp,
                    // RU перевод 12sp → 14sp. NPC должен «доминировать» в раунде.
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(OrangePrimary),
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(ctx)
                                .data(npcImageRes(state.data.npc.id))
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(3.dp)
                                .clip(CircleShape),
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${state.data.npc.name.uppercase()} · ${state.data.npc.roleRu.uppercase()}",
                            fontSize = 11.sp,
                            color = OrangePrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            round.npcLineEs,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 26.sp,
                        )
                        if (round.npcLineRu != null) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                round.npcLineRu,
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = OrangePrimary.copy(alpha = 0.15f),
                        onClick = onReplayAudio,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Переслушать",
                                tint = OrangePrimary,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }
            }
        } else if (round.audioOnly) {
            // Audio-only — большая кнопка прослушать
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(listOf(Color(0xFFFF6B1A), Color(0xFFFF8533)))
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onReplayAudio()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Прослушать", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(4.dp))

            if (round.promptRu.isNotBlank()) {
                Text(
                    round.promptRu,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }

            when (round.format) {
                RoundFormat.CHOICE,
                RoundFormat.LISTEN -> ChoicePicker(
                    round = round,
                    enabled = !answered,
                    onPick = { picked ->
                        userAnswer = picked
                        answered = true
                        onSubmit(picked)
                    }
                )

                RoundFormat.CONJUGATE -> ConjugatePicker(
                    round = round,
                    enabled = !answered,
                    onPick = { picked ->
                        userAnswer = picked
                        answered = true
                        onSubmit(picked)
                    }
                )

                RoundFormat.BUILD -> SentenceBuilder(
                    round = round,
                    enabled = !answered,
                    onSubmit = { built ->
                        userAnswer = built
                        answered = true
                        onSubmit(built)
                    }
                )

                RoundFormat.TRANSLATE_RU_ES,
                RoundFormat.TRANSLATE_ES_RU -> TranslateInput(
                    round = round,
                    enabled = !answered,
                    onSubmit = { typed ->
                        userAnswer = typed
                        answered = true
                        onSubmit(typed)
                    }
                )

                RoundFormat.VOICE -> VoiceInput(
                    round = round,
                    enabled = !answered,
                    viewModel = viewModel,
                    onSubmit = { recognized ->
                        userAnswer = recognized
                        answered = true
                        onSubmit(recognized)
                    },
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  FORMAT RENDERERS
// ════════════════════════════════════════════════════════════════════

@Composable
private fun ChoicePicker(round: CheckpointRound, enabled: Boolean, onPick: (String) -> Unit) {
    val options = remember(round) {
        (round.distractors + round.correctAnswer).shuffled(kotlin.random.Random(round.round.toLong()))
    }
    val haptic = rememberCheckedHaptic()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEach { opt ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                onClick = {
                    if (enabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPick(opt)
                    }
                }
            ) {
                Text(
                    opt,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 22.sp,
                )
            }
        }
    }
}

@Composable
private fun ConjugatePicker(round: CheckpointRound, enabled: Boolean, onPick: (String) -> Unit) {
    val options = remember(round) {
        (round.distractors + round.correctAnswer).shuffled(kotlin.random.Random(round.round.toLong()))
    }
    val haptic = rememberCheckedHaptic()
    Column {
        // Sentence template
        if (round.sentenceTemplate.isNotBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
            ) {
                Text(
                    round.sentenceTemplate,
                    modifier = Modifier.padding(20.dp),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp,
                )
            }
            Spacer(Modifier.height(14.dp))
        }

        // 2-column grid for conjugation options
        val rows = options.chunked(2)
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { opt ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        onClick = {
                            if (enabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onPick(opt)
                            }
                        },
                    ) {
                        Text(
                            opt,
                            modifier = Modifier.padding(18.dp),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))   // pad odd row
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun SentenceBuilder(round: CheckpointRound, enabled: Boolean, onSubmit: (String) -> Unit) {
    val haptic = rememberCheckedHaptic()
    val bank = remember(round) {
        round.wordBank.shuffled(kotlin.random.Random(round.round.toLong()))
    }
    val placed = remember(round) { mutableStateListOf<String>() }
    val used = remember(round) { mutableStateListOf<Int>() }

    Column {
        // Target area
        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(min = 70.dp),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                placed.forEachIndexed { idx, word ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFF6B1A),
                        onClick = {
                            if (enabled) {
                                // Remove word, restore index in bank
                                val bankIdx = bank.indexOf(word)
                                used.remove(bankIdx)
                                placed.removeAt(idx)
                            }
                        }
                    ) {
                        Text(
                            word,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Bank
        androidx.compose.foundation.layout.FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            bank.forEachIndexed { idx, word ->
                if (idx !in used) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        onClick = {
                            if (enabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                placed.add(word)
                                used.add(idx)
                            }
                        }
                    ) {
                        Text(
                            word,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSubmit(placed.joinToString(" "))
            },
            enabled = placed.isNotEmpty() && enabled,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B1A)),
        ) {
            Text("Готово", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TranslateInput(round: CheckpointRound, enabled: Boolean, onSubmit: (String) -> Unit) {
    val haptic = rememberCheckedHaptic()
    var text by remember(round) { mutableStateOf(TextFieldValue("")) }

    Column {
        // Prompt
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (round.format == RoundFormat.TRANSLATE_RU_ES) "Русский → Испанский" else "Испанский → Русский",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (round.format == RoundFormat.TRANSLATE_RU_ES) round.promptTextRu else round.promptTextEs,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (round.hintRu.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Подсказка: ${round.hintRu}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Твой ответ...") },
            enabled = enabled,
            shape = RoundedCornerShape(14.dp),
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSubmit(text.text)
            },
            enabled = text.text.isNotBlank() && enabled,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B1A)),
        ) {
            Text("Проверить", fontWeight = FontWeight.Bold)
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  VOICE INPUT — STT через SpanishSpeechRecognizer
// ════════════════════════════════════════════════════════════════════
@Composable
private fun VoiceInput(
    round: CheckpointRound,
    enabled: Boolean,
    viewModel: CheckpointViewModel,
    onSubmit: (String) -> Unit,
) {
    val haptic = rememberCheckedHaptic()
    val context = LocalContext.current
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val rmsDb by viewModel.sttRmsDb.collectAsStateWithLifecycle()
    val recognizedText by viewModel.lastVoiceText.collectAsStateWithLifecycle()
    val voiceError by viewModel.voiceError.collectAsStateWithLifecycle()

    // Сбрасываем состояние при смене раунда
    LaunchedEffect(round.round) { viewModel.clearVoiceState() }

    // RECORD_AUDIO runtime permission flow — без него STT падает ERROR_INSUFFICIENT_PERMISSIONS
    val micPermLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startVoiceCapture()
    }
    fun launchMic() {
        val granted = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (granted) viewModel.startVoiceCapture()
        else micPermLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
    }

    // Когда STT вернул результат — submit и блокируем кнопку
    LaunchedEffect(recognizedText) {
        val txt = recognizedText
        if (txt != null && enabled) onSubmit(txt)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Промпт
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "ПРОИЗНЕСИ ВСЛУХ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B1A),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    round.correctAnswer,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                if (round.promptRu.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        round.promptRu,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Микрофон: пульсирующий круг когда слушает
        val micSize = if (isListening) {
            // rmsDb колеблется ~-2..10, нормализуем в 96..132 dp
            (96 + (rmsDb.coerceIn(0f, 10f) * 3.6f).toInt()).dp
        } else 96.dp

        Box(
            modifier = Modifier
                .size(micSize)
                .clip(CircleShape)
                .background(
                    if (isListening) Color(0xFFFF6B1A) else Color(0xFF2A2D33)
                )
                .clickable(enabled = enabled && !isListening) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    launchMic()
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = "Говори",
                modifier = Modifier.size(40.dp),
                tint = Color.White,
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = when {
                isListening -> "Слушаю..."
                recognizedText != null -> "Услышал: «$recognizedText»"
                voiceError != null -> voiceError!!
                else -> "Нажми на микрофон и произнеси фразу"
            },
            fontSize = 14.sp,
            color = when {
                voiceError != null -> Color(0xFFE54848)
                isListening -> Color(0xFFFF6B1A)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            fontWeight = if (isListening) FontWeight.Bold else FontWeight.Normal,
        )

        if (voiceError != null && enabled) {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    launchMic()
                },
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Попробовать ещё раз")
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════
//  RESULT VIEW
// ════════════════════════════════════════════════════════════════════

@Composable
private fun ResultView(
    state: CheckpointState,
    onExit: () -> Unit,
    onRetry: () -> Unit,
) {
    val outcome = state.outcome ?: return
    val haptic = rememberCheckedHaptic()
    val isPass = outcome is CheckpointOutcome.Pass

    val heroGradient = when {
        outcome is CheckpointOutcome.Pass && outcome.tier == "gold" -> listOf(Color(0xFFD97706), Color(0xFFFBBF24))
        outcome is CheckpointOutcome.Pass && outcome.tier == "silver" -> listOf(Color(0xFF6B7280), Color(0xFF9CA3AF))
        outcome is CheckpointOutcome.Pass -> listOf(Color(0xFF92400E), Color(0xFFB45309))
        else -> listOf(Color(0xFFD32F2F), Color(0xFF7D1414))
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // ── Hero (увеличенные размеры) ────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Brush.linearGradient(heroGradient)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
            ) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (isPass) "✓" else "✗",
                        fontSize = 48.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    when (outcome.tier) {
                        "gold" -> "¡Bienvenida!"
                        "silver" -> "Bienvenida"
                        "bronze" -> "Pase"
                        "near_pass" -> "Acompáñeme"
                        "low" -> "Sala aparte"
                        else -> "Espere aquí"
                    },
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.4).sp,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "${outcome.percent}% правильных · ${state.correctCount}/${state.totalRounds}",
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // ── NPC quote (крупнее) ────────────────────────
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "${state.data.npc.name.uppercase()} ГОВОРИТ",
                        fontSize = 12.sp,
                        color = OrangePrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        outcome.outcomeData.npcLineEs,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 24.sp,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        outcome.outcomeData.npcLineRu,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        lineHeight = 19.sp,
                    )
                    if (outcome.outcomeData.sceneDescriptionRu.isNotBlank()) {
                        Spacer(Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            outcome.outcomeData.sceneDescriptionRu,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            lineHeight = 21.sp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── Stats card ──────────────────────────────────
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    StatRow("Точность", "${outcome.percent}% · ${state.correctCount}/${state.totalRounds}")
                    if (outcome is CheckpointOutcome.Pass) {
                        StatRowDivider()
                        StatRow("Награда XP", "+${outcome.xpAwarded}")
                        StatRowDivider()
                        StatRow("Бейдж", state.data.rewards.badgeNameRu)
                    }
                }
            }

            // ── Слабые уроки (читаемый формат) ─────────────
            if (outcome is CheckpointOutcome.Fail && outcome.weakLessons.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                val lessons = remember(outcome.weakLessons) {
                    CheckpointLessonNames.parseAndDescribe(outcome.weakLessons)
                }
                if (lessons.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFE54848).copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RedDanger.copy(alpha = 0.3f)),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "ПОВТОРИ ЭТИ УРОКИ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = RedDanger,
                                letterSpacing = 0.8.sp,
                            )
                            Spacer(Modifier.height(10.dp))
                            lessons.forEach { l ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(RedDanger.copy(alpha = 0.18f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            l.number.toString(),
                                            color = RedDanger,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        l.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Actions (крупнее) ───────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onExit()
                    },
                color = Color.Transparent,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(OrangePrimary, OrangePrimary2))),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (isPass) "Продолжить →" else "Закрыть",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.4.sp,
                    )
                }
            }
            if (!isPass) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRetry()
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, OrangePrimary),
                ) {
                    Text(
                        "Попробовать снова",
                        color = OrangePrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatRowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
    )
}
