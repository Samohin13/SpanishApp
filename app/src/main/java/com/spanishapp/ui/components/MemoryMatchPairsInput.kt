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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Memory-game стиль для MATCH_PAIRS.
 *
 * 3 фазы:
 *   1. LEARN — 10 секунд показываем 5 пар «слово ↔ перевод» с возможностью прослушать TTS
 *   2. COUNTDOWN — 3 ... 2 ... 1 (перемешиваем карточки)
 *   3. PLAY — 10 карточек в случайном порядке, юзер ищет пары
 *
 * Если в pairs > 5 — автоматически разбивается на раунды по 5.
 * Между раундами — короткая плашка «Раунд 1 пройден ✓».
 *
 * onFinish вызывается в конце последнего раунда с общим количеством ошибок.
 */
@Composable
fun MemoryMatchPairsInput(
    allPairs: List<Pair<String, String>>,
    accentColor: Color = Color(0xFFFF5722),
    learnSeconds: Int = 10,
    onPlayWord: ((String) -> Unit)? = null,
    onFinish: (totalErrors: Int) -> Unit,
) {
    // Разбиваем на раунды по 5 пар
    val rounds = remember(allPairs) {
        allPairs.chunked(5).filter { it.isNotEmpty() }
    }
    var currentRound by remember { mutableStateOf(0) }
    var totalErrors by remember { mutableStateOf(0) }
    var showRoundDone by remember { mutableStateOf(false) }

    if (currentRound >= rounds.size) {
        // Все раунды пройдены — onFinish
        LaunchedEffect(Unit) {
            delay(100)
            onFinish(totalErrors)
        }
        return
    }

    if (showRoundDone) {
        RoundDoneBanner(roundNum = currentRound, totalRounds = rounds.size, accentColor = accentColor) {
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
                currentRound++  // → triggers onFinish above
            }
        },
    )
}

// ═══════════════════════════════════════════════════════════════════════
//  Один раунд: 3 фазы
// ═══════════════════════════════════════════════════════════════════════

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
    var secondsLeft by remember(pairs) { mutableStateOf(learnSeconds) }

    // Phase 1: Timer на Learn
    LaunchedEffect(phase) {
        if (phase == Phase.LEARN) {
            for (s in learnSeconds downTo 1) {
                secondsLeft = s
                delay(1000)
            }
            phase = Phase.COUNTDOWN
        }
    }

    // Phase 2: Countdown 3-2-1
    LaunchedEffect(phase) {
        if (phase == Phase.COUNTDOWN) {
            for (n in 3 downTo 1) {
                countdownValue = n
                delay(900)
            }
            phase = Phase.PLAY
        }
    }

    Column {
        // Header — какой раунд
        if (totalRounds > 1) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("🧠", fontSize = 16.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    "Раунд $roundNum из $totalRounds",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                )
            }
        }

        AnimatedContent(
            targetState = phase,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "phase",
        ) { p ->
            when (p) {
                Phase.LEARN -> LearnPhase(pairs, secondsLeft, learnSeconds, accentColor, onPlayWord)
                Phase.COUNTDOWN -> CountdownPhase(countdownValue, accentColor)
                Phase.PLAY -> PlayPhase(pairs, accentColor, onRoundDone)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  PHASE 1: LEARN
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun LearnPhase(
    pairs: List<Pair<String, String>>,
    secondsLeft: Int,
    learnSeconds: Int,
    accentColor: Color,
    onPlayWord: ((String) -> Unit)?,
) {
    Column {
        // Big banner — что делать
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFFFC107).copy(alpha = 0.95f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "📚 Запоминай ${pairs.size} пар!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF424242),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Через ${secondsLeft} сек перемешаем",
                    fontSize = 12.sp,
                    color = Color(0xFF616161),
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        // Timer-bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE0E0E0))
        ) {
            val timerProgress by animateFloatAsState(
                targetValue = secondsLeft / learnSeconds.toFloat(),
                animationSpec = tween(900),
                label = "timer",
            )
            val timerColor = when {
                secondsLeft <= 3 -> Color(0xFFFF5252)
                secondsLeft <= 6 -> Color(0xFFFFC107)
                else -> Color(0xFF4CAF50)
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(timerProgress)
                    .background(timerColor)
            )
        }
        Spacer(Modifier.height(14.dp))

        // Pairs list
        pairs.forEachIndexed { idx, (es, ru) ->
            LearnPairCard(es = es, ru = ru, accentColor = accentColor, onPlay = onPlayWord, index = idx)
            if (idx < pairs.size - 1) Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun LearnPairCard(
    es: String,
    ru: String,
    accentColor: Color,
    onPlay: ((String) -> Unit)?,
    index: Int,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 100L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(250)) +
            slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = tween(300)),
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFFEF9F5),
            border = androidx.compose.foundation.BorderStroke(2.dp, accentColor.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    es,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                Text("→", fontSize = 20.sp, color = Color(0xFF999999))
                Text(
                    ru,
                    fontSize = 16.sp,
                    color = Color(0xFF333333),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                if (onPlay != null) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                            .clickable { onPlay(es) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("▶", fontSize = 14.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  PHASE 2: COUNTDOWN
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun CountdownPhase(value: Int, accentColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A1A1A), Color(0xFF333333))
                )
            ),
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
                fontSize = 140.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFFC107),
                modifier = Modifier.scale(scale),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "📚 Готовься!",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  PHASE 3: PLAY
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun PlayPhase(
    pairs: List<Pair<String, String>>,
    accentColor: Color,
    onRoundDone: (errors: Int) -> Unit,
) {
    // 10 карточек: 5 испанских + 5 переводов в случайном порядке
    data class Card(val text: String, val pairId: Int, val isEs: Boolean)

    val cards = remember(pairs) {
        pairs.flatMapIndexed { i, (es, ru) ->
            listOf(Card(es, i, isEs = true), Card(ru, i, isEs = false))
        }.shuffled()
    }

    var selected by remember(pairs) { mutableStateOf<Card?>(null) }
    val matched = remember(pairs) { mutableStateListOf<Int>() }
    var wrongPair by remember(pairs) { mutableStateOf<Pair<Card, Card>?>(null) }
    var errors by remember(pairs) { mutableStateOf(0) }

    // Если все пары собраны — конец раунда
    LaunchedEffect(matched.size) {
        if (matched.size == pairs.size) {
            delay(400)
            onRoundDone(errors)
        }
    }

    // Авто-сброс неправильной пары через 600 мс
    LaunchedEffect(wrongPair) {
        if (wrongPair != null) {
            delay(700)
            wrongPair = null
        }
    }

    Column {
        // Header — счётчик ошибок
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    "🎮 Найди ${pairs.size} пар!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(12.dp),
                )
            }
            if (errors > 0) {
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFFEBEE),
                ) {
                    Text(
                        "Ошибки: $errors",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
        }

        // Grid 2 × N
        val rows = cards.chunked(2)
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { card ->
                    val isMatched = card.pairId in matched
                    val isSelected = selected == card
                    val isWrong = wrongPair?.let { (a, b) -> card == a || card == b } == true

                    PlayCard(
                        text = card.text,
                        isEs = card.isEs,
                        isMatched = isMatched,
                        isSelected = isSelected,
                        isWrong = isWrong,
                        accentColor = accentColor,
                        onClick = {
                            if (isMatched || isWrong) return@PlayCard
                            val cur = selected
                            if (cur == null) {
                                selected = card
                            } else if (cur == card) {
                                selected = null   // отмена выбора
                            } else if (cur.pairId == card.pairId) {
                                // Совпало!
                                matched.add(card.pairId)
                                selected = null
                            } else {
                                // Промах!
                                errors++
                                wrongPair = cur to card
                                selected = null
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayCard(
    text: String,
    isEs: Boolean,
    isMatched: Boolean,
    isSelected: Boolean,
    isWrong: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = when {
            isSelected -> 1.05f
            isMatched -> 0.95f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale",
    )
    val (bg, border, textColor) = when {
        isMatched -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF4CAF50),
            Color(0xFF2E7D32),
        )
        isWrong -> Triple(
            Color(0xFFFFEBEE),
            Color(0xFFFF5252),
            Color(0xFFC62828),
        )
        isSelected -> Triple(
            Color(0xFFFFF3E0),
            accentColor,
            accentColor,
        )
        else -> Triple(
            Color.White,
            Color(0xFFE0E0E0),
            if (isEs) accentColor else Color(0xFF333333),
        )
    }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(2.dp, border),
        shadowElevation = if (isSelected) 6.dp else 2.dp,
        modifier = modifier
            .scale(scale)
            .heightIn(min = 64.dp)
            .clickable(enabled = !isMatched && !isWrong, onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text,
                    fontSize = if (text.length > 10) 13.sp else 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                )
                if (isMatched) {
                    Spacer(Modifier.width(4.dp))
                    Text("✓", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  Round-done banner — между раундами
// ═══════════════════════════════════════════════════════════════════════

@Composable
private fun RoundDoneBanner(
    roundNum: Int,
    totalRounds: Int,
    accentColor: Color,
    onNext: () -> Unit,
) {
    LaunchedEffect(roundNum) {
        delay(1800)
        onNext()
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎉", fontSize = 80.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "Раунд ${roundNum + 1} пройден!",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (roundNum + 1 < totalRounds) "Раунд ${roundNum + 2}/${totalRounds} запускается..." else "",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f),
            )
        }
    }
}
