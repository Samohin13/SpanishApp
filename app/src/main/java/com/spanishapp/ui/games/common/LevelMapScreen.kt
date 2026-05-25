package com.spanishapp.ui.games.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spanishapp.R
import com.spanishapp.data.db.entity.GameLevelProgressEntity
import com.spanishapp.domain.games.GameLevelManager
import com.spanishapp.domain.games.LevelDifficulty
import com.spanishapp.domain.games.LevelMode
import kotlinx.coroutines.launch

/**
 * v1.22.4: единица измерения в пуле ошибок. WORDS — для словесных игр
 * (Articles, Speed, Palabra), TASKS — для Math (там «задания», не «слова»).
 */
enum class MistakesUnit { WORDS, TASKS }

/**
 * Универсальный экран выбора уровня (10×10 сетка). Используется всеми играми.
 *
 * @param gameId       идентификатор игры (см. GameId)
 * @param title        название игры в шапке
 * @param accent       цвет акцента (соответствует карточке игры)
 * @param manager      инжектится из ViewModel
 * @param onBack       вернуться к списку игр
 * @param onLevelStart колбэк на старт уровня
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelMapScreen(
    gameId: String,
    title: String,
    accent: Color,
    manager: GameLevelManager,
    onBack: () -> Unit,
    onLevelStart: (Int) -> Unit,
    /** v1.22.0: счётчик ошибок и колбэк на запуск режима практики. */
    mistakesCount: Int = 0,
    onMistakesPractice: () -> Unit = {},
    /**
     * v1.22.4: единица измерения в пуле ошибок — WORDS для словесных игр,
     * TASKS для Calculo (математика).
     */
    mistakesUnit: MistakesUnit = MistakesUnit.WORDS,
    /**
     * v1.23.0: для free-юзеров доступны только первые 10 уровней.
     * Уровни 11+ показываются как заблокированные с 🔒, тап ведёт на paywall.
     */
    isPro: Boolean = false,
    onPaywall: () -> Unit = {},
) {
    var progress by remember { mutableStateOf<Map<Int, GameLevelProgressEntity>>(emptyMap()) }
    var nextLevel by remember { mutableIntStateOf(1) }
    var totalStars by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val haptic = com.spanishapp.ui.components.rememberCheckedHaptic()

    LaunchedEffect(gameId) {
        progress = manager.getProgressMap(gameId)
        nextLevel = manager.nextLevel(gameId)
        totalStars = manager.totalStars(gameId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, fontWeight = FontWeight.Bold)
                        Text(
                            "★ $totalStars / 300",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // ── v1.22.0: Карточка «Работа над ошибками» ───────────
            if (mistakesCount > 0) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFF6B35).copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(0xFFFF6B35).copy(alpha = 0.5f)
                    ),
                    onClick = {
                        haptic.performHapticFeedback(
                            androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress
                        )
                        onMistakesPractice()
                    },
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("📝", fontSize = 28.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.mistakes_practice_title),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                stringResource(
                                    when (mistakesUnit) {
                                        MistakesUnit.WORDS -> R.string.mistakes_practice_card_subtitle_words
                                        MistakesUnit.TASKS -> R.string.mistakes_practice_card_subtitle_tasks
                                    },
                                    mistakesCount,
                                    pluralFor(mistakesCount, mistakesUnit),
                                ),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Surface(
                            color = Color(0xFFFF6B35),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(
                                "$mistakesCount",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items((1..100).toList()) { level ->
                val entry = progress[level]
                val unlockedByProgress = level <= nextLevel
                // v1.23.0: free → max 10 уровней. PRO → все 100.
                val unlockedByPro = isPro || level <= 10
                val unlocked = unlockedByProgress && unlockedByPro
                val isNext = level == nextLevel

                    GameLevelCell(
                        level    = level,
                        stars    = entry?.stars ?: 0,
                        unlocked = unlocked,
                        isNext   = isNext,
                        accent   = accent,
                        onClick  = {
                            haptic.performHapticFeedback(
                                androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress
                            )
                            when {
                                // Тап на PRO-locked (юзер прошёл бы по прогрессу, но free) → paywall
                                unlockedByProgress && !unlockedByPro -> onPaywall()
                                unlocked -> scope.launch { onLevelStart(level) }
                                else -> { /* progression locked — нет действия */ }
                            }
                        }
                    )
                }
            }
        }
    }
}

/** Простое склонение «слово / слова / слов» для русского. */
internal fun pluralFor(n: Int, unit: MistakesUnit): String {
    val mod100 = n % 100
    val mod10 = n % 10
    return when (unit) {
        MistakesUnit.WORDS -> when {
            mod100 in 11..19 -> "слов"
            mod10 == 1 -> "слово"
            mod10 in 2..4 -> "слова"
            else -> "слов"
        }
        MistakesUnit.TASKS -> when {
            mod100 in 11..19 -> "заданий"
            mod10 == 1 -> "задание"
            mod10 in 2..4 -> "задания"
            else -> "заданий"
        }
    }
}

/**
 * Re-usable level tile shared by all games. Crossword imports this so its
 * level grid matches Articulos/Math/Speed/etc. visually.
 */
@Composable
fun GameLevelCell(
    level: Int,
    stars: Int,
    unlocked: Boolean,
    isNext: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    val params = LevelDifficulty.forLevel(level)
    val cs = MaterialTheme.colorScheme
    val bgColor = when {
        !unlocked -> cs.surfaceVariant
        stars > 0 -> accent.copy(alpha = 0.18f)
        isNext    -> accent
        else      -> cs.surface
    }
    val textColor = when {
        !unlocked -> cs.onSurfaceVariant.copy(alpha = 0.5f)
        isNext    -> Color.White
        else      -> cs.onSurface
    }
    val lockTint = cs.onSurfaceVariant.copy(alpha = 0.5f)
    val starOff = cs.outline.copy(alpha = 0.4f)

    Surface(
        shape    = RoundedCornerShape(14.dp),
        color    = bgColor,
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(enabled = unlocked, onClick = onClick),
        shadowElevation = if (isNext) 4.dp else 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (!unlocked) {
                Icon(Icons.Default.Lock, null,
                    tint = lockTint,
                    modifier = Modifier.size(20.dp))
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = level.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    if (stars > 0) {
                        Row {
                            repeat(3) { i ->
                                Icon(
                                    Icons.Default.Star, null,
                                    tint = if (i < stars) Color(0xFFFFC107)
                                           else starOff,
                                    modifier = Modifier.size(8.dp)
                                )
                            }
                        }
                    } else if (isNext) {
                        // подпись режима для следующего уровня
                        Text(
                            params.mode.label(),
                            fontSize = 8.sp,
                            color = textColor.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

private fun LevelMode.label(): String = when (this) {
    LevelMode.TUTORIAL -> "TUT"
    LevelMode.EASY     -> "EASY"
    LevelMode.NORMAL   -> "NORM"
    LevelMode.HARD     -> "HARD"
    LevelMode.EXPERT   -> "EXP"
    LevelMode.MASTER   -> "★"
}

// ════════════════════════════════════════════════════════════
//  v1.23.0: ProGate — Hilt EntryPoint для доступа к
//  SubscriptionManager из Composable без необходимости менять
//  каждый VM отдельной игры. Возвращает текущее значение isPro
//  как Compose State.
// ════════════════════════════════════════════════════════════

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface ProGateEntryPoint {
    fun subscriptionManager(): com.spanishapp.service.SubscriptionManager
}

@Composable
fun rememberIsProState(): State<Boolean> {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sm = remember {
        dagger.hilt.android.EntryPointAccessors
            .fromApplication(context.applicationContext, ProGateEntryPoint::class.java)
            .subscriptionManager()
    }
    return sm.isProActive.collectAsState(initial = false)
}
