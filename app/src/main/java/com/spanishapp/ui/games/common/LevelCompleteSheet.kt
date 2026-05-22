package com.spanishapp.ui.games.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

private val STAR_GOLD  = Color(0xFFFFC107)
private val STAR_EMPTY = Color(0xFFE5E5EA)

/**
 * Полноэкранный диалог результата уровня — Duolingo стиль.
 */
@Composable
fun LevelCompleteSheet(
    level: Int,
    stars: Int,
    percent: Int,
    accent: Color,
    onRetry: () -> Unit,
    onNext: (() -> Unit)?,
    onExit: () -> Unit,
    /**
     * v1.22.4: режим «работа над ошибками». В этом режиме не показываем
     * звёзды (это не уровень), показываем счёт «N из M» и статус пула.
     */
    isMistakesPractice: Boolean = false,
    mistakesCorrect: Int = 0,
    mistakesTotal: Int = 0,
    mistakesPoolLeft: Int = 0,
) {
    // В практике ошибок звёзды не используются — passed = было ли хоть одно правильное.
    val passed = if (isMistakesPractice) mistakesCorrect > 0 else stars > 0

    // Звёзды появляются поочерёдно
    val starVisible = remember { List(3) { mutableStateOf(false) } }
    LaunchedEffect(Unit) {
        for (i in 0 until stars) {
            delay(300L + i * 250L)
            starVisible[i].value = true
        }
    }

    Dialog(
        onDismissRequest = onExit,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape         = RoundedCornerShape(28.dp),
                color         = Color.White,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier              = Modifier.padding(28.dp),
                    horizontalAlignment   = Alignment.CenterHorizontally
                ) {

                    // ── Цветная шапка ─────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .background(
                                Brush.horizontalGradient(
                                    if (passed) listOf(Color(0xFF58CC02), Color(0xFF89E219))
                                    else        listOf(Color(0xFFFF4B4B), Color(0xFFFF7043))
                                ),
                                shape = RoundedCornerShape(18.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text  = when {
                                    isMistakesPractice ->
                                        stringResource(com.spanishapp.R.string.mistakes_practice_done_title)
                                    passed ->
                                        stringResource(com.spanishapp.R.string.level_complete_passed_template, level)
                                    else  ->
                                        stringResource(com.spanishapp.R.string.level_complete_try_again)
                                },
                                fontSize   = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color      = Color.White,
                                textAlign  = TextAlign.Center
                            )
                            if (isMistakesPractice && mistakesTotal > 0) {
                                Text(
                                    "+${mistakesCorrect * 3} XP",
                                    fontSize = 13.sp,
                                    color    = Color.White.copy(alpha = 0.9f)
                                )
                            } else if (passed) {
                                Text(
                                    "+${(percent / 10) * 5} XP",
                                    fontSize = 13.sp,
                                    color    = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    if (!isMistakesPractice) {
                        // ── Три звезды (только в обычных уровнях) ─────
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            repeat(3) { i ->
                                AnimatedStar(
                                    filled  = i < stars,
                                    visible = if (i < stars) starVisible[i].value else true,
                                    size    = if (i == 1) 64.dp else 52.dp
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // ── Мотивационный текст + точность ────────
                        val motivText = when {
                            percent == 100 -> stringResource(com.spanishapp.R.string.level_complete_perfect)
                            percent >= 90  -> stringResource(com.spanishapp.R.string.level_complete_excellent)
                            percent >= 70  -> stringResource(com.spanishapp.R.string.level_complete_good)
                            percent >= 50  -> stringResource(com.spanishapp.R.string.level_complete_keep_going)
                            else           -> stringResource(com.spanishapp.R.string.level_complete_more_practice)
                        }
                        Text(
                            motivText,
                            fontSize   = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color      = if (passed) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(com.spanishapp.R.string.level_complete_accuracy_template, percent),
                            fontSize = 15.sp,
                            color    = Color(0xFF8E8E93)
                        )
                    } else {
                        // ── Mistakes practice: счёт «N из M» + статус пула ──
                        Text(
                            stringResource(
                                com.spanishapp.R.string.mistakes_practice_done_score,
                                mistakesCorrect,
                                mistakesTotal,
                            ),
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = if (passed) Color(0xFF2E7D32) else Color(0xFFC62828),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (mistakesPoolLeft <= 0)
                                stringResource(com.spanishapp.R.string.mistakes_practice_pool_empty)
                            else
                                stringResource(
                                    com.spanishapp.R.string.mistakes_practice_pool_left,
                                    mistakesPoolLeft,
                                    pluralForMistakes(mistakesPoolLeft),
                                ),
                            fontSize = 15.sp,
                            color    = Color(0xFF8E8E93),
                            textAlign = TextAlign.Center,
                        )
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── Кнопки ────────────────────────────────
                    if (isMistakesPractice) {
                        // В пуле остались слова → Продолжить (ещё пятёрка)
                        // Пул пуст → Закрыть.
                        if (mistakesPoolLeft > 0 && onNext != null) {
                            Button(
                                onClick  = onNext,
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                shape    = RoundedCornerShape(16.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = accent),
                            ) {
                                Text(
                                    stringResource(com.spanishapp.R.string.mistakes_practice_continue),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            TextButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    stringResource(com.spanishapp.R.string.mistakes_practice_done),
                                    fontSize = 15.sp,
                                    color = Color(0xFF8E8E93),
                                )
                            }
                        } else {
                            Button(
                                onClick  = onExit,
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                shape    = RoundedCornerShape(16.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = accent),
                            ) {
                                Text(
                                    stringResource(com.spanishapp.R.string.mistakes_practice_done),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                )
                            }
                        }
                    } else if (passed && onNext != null) {
                        Button(
                            onClick  = onNext,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape    = RoundedCornerShape(16.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = accent)
                        ) {
                            Text(stringResource(com.spanishapp.R.string.level_complete_next), fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp, letterSpacing = 0.5.sp)
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick  = onExit,
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape    = RoundedCornerShape(14.dp),
                                colors   = ButtonDefaults.outlinedButtonColors(contentColor = accent)
                            ) { Text(stringResource(com.spanishapp.R.string.level_complete_menu), fontWeight = FontWeight.SemiBold) }

                            OutlinedButton(
                                onClick  = onRetry,
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape    = RoundedCornerShape(14.dp),
                                colors   = ButtonDefaults.outlinedButtonColors(contentColor = accent)
                            ) { Text(stringResource(com.spanishapp.R.string.level_complete_again), fontWeight = FontWeight.SemiBold) }
                        }
                    } else {
                        Button(
                            onClick  = onRetry,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape    = RoundedCornerShape(16.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4B4B))
                        ) {
                            Text(stringResource(com.spanishapp.R.string.level_complete_try_again_button),
                                fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                        }
                        Spacer(Modifier.height(10.dp))
                        TextButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(com.spanishapp.R.string.level_complete_back_to_menu), fontSize = 15.sp, color = Color(0xFF8E8E93))
                        }
                    }
                }
            }
        }
    }
}

/** Простое склонение для русского — «слово / слова / слов». */
private fun pluralForMistakes(n: Int): String {
    val mod100 = n % 100
    val mod10 = n % 10
    return when {
        mod100 in 11..19 -> "слов"
        mod10 == 1 -> "слово"
        mod10 in 2..4 -> "слова"
        else -> "слов"
    }
}

// ── Анимированная звезда ─────────────────────────────────────

@Composable
private fun AnimatedStar(filled: Boolean, visible: Boolean, size: Dp) {
    val scale by animateFloatAsState(
        targetValue   = if (visible && filled) 1f else 0.55f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "star"
    )
    Icon(
        imageVector    = Icons.Default.Star,
        contentDescription = null,
        tint           = if (filled) STAR_GOLD else STAR_EMPTY,
        modifier       = Modifier.size(size).scale(scale)
    )
}
