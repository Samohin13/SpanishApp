package com.spanishapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val Green = Color(0xFF4CAF50)
private val Red   = Color(0xFFE53935)

/**
 * Memory-game для MATCH_PAIRS.
 *
 * 3 фазы:
 *   1. LEARN — N секунд показываем пары es↔ru (можно прослушать)
 *   2. COUNTDOWN — 3-2-1
 *   3. PLAY — 2 СТРОГИХ колонки: слева es, справа ru. Шафл независимый.
 *      Тапаешь по одной с каждой стороны → проверка. Совпала — Green ✓ и disable.
 *      Не совпала — короткая красная подсветка, потом сброс.
 *
 * Если pairs > 5 — раунды по 5.
 *
 * Стиль строго совпадает с другими input-ами в LessonSessionScreen:
 *   нейтрал = surfaceVariant.alpha=0.4
 *   selected = accent.alpha=0.1 + border 2dp
 *   matched = Green.alpha=0.12 + border Green
 *   wrong   = Red.alpha=0.12 + border Red
 */
@Composable
fun MemoryMatchPairsInput(
    allPairs: List<Pair<String, String>>,
    accentColor: Color = Color(0xFFFF5722),
    learnSeconds: Int = 10,
    onPlayWord: ((String) -> Unit)? = null,
    onFinish: (totalErrors: Int) -> Unit,
) {
    val rounds = remember(allPairs) { allPairs.chunked(5).filter { it.isNotEmpty() } }
    var currentRound by remember { mutableStateOf(0) }
    var totalErrors by remember { mutableStateOf(0) }
    var showRoundDone by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    // Финальный экран — всегда явная кнопка «Дальше»
    if (finished) {
        FinishBanner(
            totalErrors = totalErrors,
            totalPairs = allPairs.size,
            accentColor = accentColor,
            onContinue = { onFinish(totalErrors) },
        )
        return
    }

    if (showRoundDone) {
        RoundDoneBanner(
            roundNum = currentRound,
            totalRounds = rounds.size,
            accentColor = accentColor,
        ) {
            showRoundDone = false
            currentRound++
        }
        return
    }

    RoundContent(
        pairs = rounds[currentRound],
        roundNum = currentRound + 1,
        totalRounds = rounds.size,
        accentColor = accentColor,
        learnSeconds = learnSeconds,
        onPlayWord = onPlayWord,
        onRoundDone = { errs ->
            totalErrors += errs
            if (currentRound + 1 < rounds.size) {
                showRoundDone = true
            } else {
                finished = true
            }
        },
    )
}

private enum class Phase { LEARN, COUNTDOWN, PLAY }

@Composable
private fun RoundContent(
    pairs: List<Pair<String, String>>,
    roundNum: Int,
    totalRounds: Int,
    accentColor: Color,
    learnSeconds: Int,
    onPlayWord: ((String) -> Unit)?,
    onRoundDone: (errors: Int) -> Unit,
) {
    var phase by remember(pairs) { mutableStateOf(Phase.LEARN) }
    var countdownValue by remember(pairs) { mutableStateOf(3) }
    var errors by remember(pairs) { mutableStateOf(0) }

    // LEARN таймер
    LaunchedEffect(phase, pairs) {
        if (phase == Phase.LEARN) {
            delay(learnSeconds * 1000L)
            phase = Phase.COUNTDOWN
        }
    }
    // COUNTDOWN таймер
    LaunchedEffect(phase) {
        if (phase == Phase.COUNTDOWN) {
            for (i in 3 downTo 1) {
                countdownValue = i
                delay(700)
            }
            phase = Phase.PLAY
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Заголовок раунда (только если раундов > 1)
        if (totalRounds > 1) {
            Text(
                "Раунд $roundNum из $totalRounds",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        when (phase) {
            Phase.LEARN -> LearnPhase(
                pairs = pairs,
                accentColor = accentColor,
                learnSeconds = learnSeconds,
                onPlayWord = onPlayWord,
            )
            Phase.COUNTDOWN -> CountdownPhase(
                value = countdownValue,
                accentColor = accentColor,
            )
            Phase.PLAY -> PlayPhase(
                pairs = pairs,
                accentColor = accentColor,
                onPlayWord = onPlayWord,
                onWrong = { errors++ },
                onDone = { onRoundDone(errors) },
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  LEARN — список пар es↔ru с TTS
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun LearnPhase(
    pairs: List<Pair<String, String>>,
    accentColor: Color,
    learnSeconds: Int,
    onPlayWord: ((String) -> Unit)?,
) {
    var elapsed by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        repeat(learnSeconds) {
            delay(1000)
            elapsed++
        }
    }
    val progress = (elapsed.toFloat() / learnSeconds).coerceIn(0f, 1f)

    Column {
        // Хедер раунда — в общем стиле, без жёлтых банеров
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = accentColor.copy(alpha = 0.10f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    "📚 Запоминай ${pairs.size} пар",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Через ${(learnSeconds - elapsed).coerceAtLeast(0)} сек — игра",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                // Прогресс-полоска времени
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(accentColor),
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        pairs.forEach { (es, ru) ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        es,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "→",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                    Text(
                        ru,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f),
                    )
                    if (onPlayWord != null) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                                .clickable { onPlayWord(es) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("🔊", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  COUNTDOWN — 3 ... 2 ... 1
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun CountdownPhase(value: Int, accentColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(accentColor.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val scale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "countdownScale",
            )
            Text(
                value.toString(),
                fontSize = 120.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                modifier = Modifier.scale(scale),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Перемешиваем…",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  PLAY — 2 строгие колонки: слева es, справа ru
// ═══════════════════════════════════════════════════════════════════════

private enum class CardState { IDLE, SELECTED, MATCHED, WRONG }

@Composable
private fun PlayPhase(
    pairs: List<Pair<String, String>>,
    accentColor: Color,
    onPlayWord: ((String) -> Unit)?,
    onWrong: () -> Unit,
    onDone: () -> Unit,
) {
    // Шафлим лево и право независимо. seed=pairs hashCode чтобы стабильно в рамках раунда.
    val leftIndices = remember(pairs) { pairs.indices.shuffled() }
    val rightIndices = remember(pairs) { pairs.indices.shuffled() }

    val leftStates = remember(pairs) {
        mutableStateListOf<CardState>().apply { repeat(pairs.size) { add(CardState.IDLE) } }
    }
    val rightStates = remember(pairs) {
        mutableStateListOf<CardState>().apply { repeat(pairs.size) { add(CardState.IDLE) } }
    }

    var selectedLeft  by remember(pairs) { mutableStateOf<Int?>(null) }
    var selectedRight by remember(pairs) { mutableStateOf<Int?>(null) }
    var matchedCount  by remember(pairs) { mutableStateOf(0) }
    var lockInput     by remember(pairs) { mutableStateOf(false) }

    // Когда выбраны обе стороны — проверка
    LaunchedEffect(selectedLeft, selectedRight) {
        val l = selectedLeft
        val r = selectedRight
        if (l != null && r != null) {
            lockInput = true
            if (l == r) {
                // Match
                leftStates[l] = CardState.MATCHED
                rightStates[r] = CardState.MATCHED
                matchedCount++
                delay(400)
                selectedLeft = null
                selectedRight = null
                if (matchedCount >= pairs.size) {
                    delay(300)
                    onDone()
                }
            } else {
                leftStates[l] = CardState.WRONG
                rightStates[r] = CardState.WRONG
                onWrong()
                delay(700)
                if (leftStates[l] == CardState.WRONG) leftStates[l] = CardState.IDLE
                if (rightStates[r] == CardState.WRONG) rightStates[r] = CardState.IDLE
                selectedLeft = null
                selectedRight = null
            }
            lockInput = false
        }
    }

    Column(Modifier.fillMaxWidth()) {
        // Маленький заголовок
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = accentColor.copy(alpha = 0.10f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "🎯 Найди ${pairs.size} пар",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "$matchedCount / ${pairs.size}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (matchedCount == pairs.size) Green else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Две колонки
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // ЛЕВО: испанский
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                leftIndices.forEach { pairIdx ->
                    val es = pairs[pairIdx].first
                    val state = leftStates[pairIdx]
                    PairCard(
                        text = es,
                        isEs = true,
                        state = state,
                        accentColor = accentColor,
                        enabled = !lockInput && state != CardState.MATCHED,
                        onClick = {
                            if (state == CardState.MATCHED) return@PairCard
                            // Снимаем предыдущий select-left если был
                            selectedLeft?.let { prev ->
                                if (prev != pairIdx && leftStates[prev] == CardState.SELECTED) {
                                    leftStates[prev] = CardState.IDLE
                                }
                            }
                            leftStates[pairIdx] = CardState.SELECTED
                            selectedLeft = pairIdx
                            onPlayWord?.invoke(es)
                        },
                    )
                }
            }
            // ПРАВО: русский
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rightIndices.forEach { pairIdx ->
                    val ru = pairs[pairIdx].second
                    val state = rightStates[pairIdx]
                    PairCard(
                        text = ru,
                        isEs = false,
                        state = state,
                        accentColor = accentColor,
                        enabled = !lockInput && state != CardState.MATCHED,
                        onClick = {
                            if (state == CardState.MATCHED) return@PairCard
                            selectedRight?.let { prev ->
                                if (prev != pairIdx && rightStates[prev] == CardState.SELECTED) {
                                    rightStates[prev] = CardState.IDLE
                                }
                            }
                            rightStates[pairIdx] = CardState.SELECTED
                            selectedRight = pairIdx
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PairCard(
    text: String,
    isEs: Boolean,
    state: CardState,
    accentColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = when (state) {
        CardState.IDLE     -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        CardState.SELECTED -> accentColor.copy(alpha = 0.12f)
        CardState.MATCHED  -> Green.copy(alpha = 0.14f)
        CardState.WRONG    -> Red.copy(alpha = 0.14f)
    }
    val borderColor = when (state) {
        CardState.IDLE     -> Color.Transparent
        CardState.SELECTED -> accentColor
        CardState.MATCHED  -> Green
        CardState.WRONG    -> Red
    }
    val borderWidth = if (state == CardState.IDLE) 0.dp else 2.dp
    val textColor = when {
        state == CardState.MATCHED -> Green
        isEs                       -> accentColor
        else                       -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        modifier = Modifier
            .fillMaxWidth()
            .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                // Большие буквы (≤2 символа) → крупный шрифт для алфавитных уроков
                val isLetter = text.length <= 2
                Text(
                    text,
                    fontSize = if (isLetter) 28.sp else 15.sp,
                    fontWeight = if (isEs || isLetter) FontWeight.Bold else FontWeight.Medium,
                    color = textColor,
                    textAlign = TextAlign.Center,
                )
                if (state == CardState.MATCHED) {
                    Spacer(Modifier.width(6.dp))
                    Text("✓", color = Green, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                } else if (state == CardState.WRONG) {
                    Spacer(Modifier.width(6.dp))
                    Text("✗", color = Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  RoundDoneBanner
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun FinishBanner(
    totalErrors: Int,
    totalPairs: Int,
    accentColor: Color,
    onContinue: () -> Unit,
) {
    val perfect = totalErrors == 0
    val accuracyPercent = if (totalPairs > 0) {
        ((totalPairs.toFloat() / (totalPairs + totalErrors)) * 100).toInt().coerceIn(0, 100)
    } else 100

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Green.copy(alpha = 0.10f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                if (perfect) "🏆" else "✅",
                fontSize = 56.sp,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                if (perfect) "Идеально! Все пары с первой попытки" else "Все пары найдены",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "$totalPairs пар · точность $accuracyPercent%",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(18.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = accentColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onContinue),
            ) {
                Text(
                    "Дальше →",
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

@Composable
private fun RoundDoneBanner(
    roundNum: Int,
    totalRounds: Int,
    accentColor: Color,
    onContinue: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Green.copy(alpha = 0.10f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("✓", fontSize = 36.sp, color = Green, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Раунд $roundNum из $totalRounds пройден",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accentColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onContinue),
            ) {
                Text(
                    "Дальше →",
                    modifier = Modifier
                        .padding(vertical = 14.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
            }
        }
    }
}
