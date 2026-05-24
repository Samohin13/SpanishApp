package com.spanishapp.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val BrandOrange = Color(0xFFFF6B1A)
private val BrandOrangeDeep = Color(0xFFFF8A3D)
private val DarkBg = Color(0xFF0F0F11)
private val SurfaceDark = Color(0xFF1C1C1E)
private val TextDim = Color(0xFFAEAEB2)

/**
 * First-launch onboarding container.
 *
 * Renders one of 5 steps via [AnimatedContent] with a horizontal slide
 * transition. When the VM emits `completed=true` the host's [onFinished]
 * callback fires — MainActivity uses it to swap in [SpanishAppRoot].
 *
 * @param onFinished called with the optional adaptive route (e.g.
 * `"lesson_session/5/0"`) — host should `navigate(route)` after the
 * standard auth/home flow lands. `null` means "use the default flow".
 */
@Composable
fun OnboardingScreen(
    onFinished: (adaptiveRoute: String?) -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.completed) {
        if (state.completed) onFinished(state.adaptiveRoute)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = DarkBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            // Top bar: progress dots + skip
            OnboardingTopBar(
                currentStep = state.step,
                onSkip = { viewModel.skip() },
            )
            Spacer(Modifier.height(16.dp))

            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    (slideInHorizontally(tween(280)) { it } + fadeIn(tween(280))) togetherWith
                        (slideOutHorizontally(tween(280)) { -it } + fadeOut(tween(280)))
                },
                label = "onboarding_step",
                modifier = Modifier.fillMaxSize(),
            ) { step ->
                when (step) {
                    OnboardingStep.Welcome     -> WelcomeStep(onNext = viewModel::next)
                    OnboardingStep.LevelSelect -> LevelSelectStep(
                        selected = state.selectedLevel,
                        onSelect = viewModel::selectLevel,
                        onNext = viewModel::next,
                    )
                    OnboardingStep.Commitment  -> CommitmentStep(
                        selected = state.dailyMinutes,
                        onSelect = viewModel::selectDailyMinutes,
                        onNext = viewModel::next,
                    )
                    OnboardingStep.FirstWin    -> FirstWinStep(
                        solved = state.cognatesSolved,
                        onCorrect = viewModel::cognateSolved,
                        onAllDone = {
                            viewModel.awardFirstWinXp()
                            viewModel.next()
                        },
                    )
                    OnboardingStep.Done        -> DoneStep(onFinish = viewModel::finish)
                }
            }
        }
    }
}

@Composable
private fun OnboardingTopBar(currentStep: OnboardingStep, onSkip: () -> Unit) {
    val steps = OnboardingStep.values()
    val currentIdx = steps.indexOf(currentStep)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            steps.forEachIndexed { idx, _ ->
                val active = idx <= currentIdx
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .width(if (active) 24.dp else 12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (active) BrandOrange else SurfaceDark),
                )
            }
        }
        Spacer(Modifier.weight(1f))
        if (currentStep != OnboardingStep.Done) {
            TextButton(onClick = onSkip) {
                Text("Пропустить", color = TextDim, fontSize = 14.sp)
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────
//  STEP 1: WELCOME
// ──────────────────────────────────────────────────────────────────────

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .background(BrandOrange),
            contentAlignment = Alignment.Center,
        ) {
            Text("🇪🇸", fontSize = 64.sp)
        }
        Spacer(Modifier.height(32.dp))
        Text(
            "Привет!",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Выучи испанский за 5 минут\nв день — без скуки",
            fontSize = 18.sp,
            color = TextDim,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp,
        )
        Spacer(Modifier.height(48.dp))
        PrimaryButton(text = "Начнём!", onClick = onNext)
    }
}

// ──────────────────────────────────────────────────────────────────────
//  STEP 2: LEVEL SELECT
// ──────────────────────────────────────────────────────────────────────

private data class LevelOption(
    val code: String,
    val emoji: String,
    val title: String,
    val subtitle: String,
)

private val LEVEL_OPTIONS = listOf(
    LevelOption(OnboardingPrefs.LEVEL_BEGINNER, "🌱",
        "Совсем новичок", "Никогда не учил испанский"),
    LevelOption(OnboardingPrefs.LEVEL_BASICS, "👋",
        "Знаю основы", "Hola, gracias, números"),
    LevelOption(OnboardingPrefs.LEVEL_A1, "📖",
        "Уровень A1", "Простые предложения в настоящем"),
    LevelOption(OnboardingPrefs.LEVEL_A2, "💬",
        "Уровень A2", "Понимаю и разговариваю в прошедшем"),
)

@Composable
private fun LevelSelectStep(
    selected: String,
    onSelect: (String) -> Unit,
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            "Уже знаешь испанский?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Подберём подходящий старт",
            fontSize = 16.sp,
            color = TextDim,
        )
        Spacer(Modifier.height(24.dp))
        LEVEL_OPTIONS.forEach { opt ->
            LevelCard(
                option = opt,
                isSelected = opt.code == selected,
                onClick = { onSelect(opt.code) },
            )
            Spacer(Modifier.height(12.dp))
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton(text = "Дальше", onClick = onNext)
    }
}

@Composable
private fun LevelCard(option: LevelOption, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) BrandOrange else Color(0xFF2A2A2D)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(width = if (isSelected) 2.dp else 1.dp, color = borderColor,
                shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(option.emoji, fontSize = 32.sp)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                option.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
            Spacer(Modifier.height(2.dp))
            Text(option.subtitle, fontSize = 13.sp, color = TextDim)
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(BrandOrange),
                contentAlignment = Alignment.Center,
            ) {
                Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────
//  STEP 3: COMMITMENT
// ──────────────────────────────────────────────────────────────────────

private val MINUTE_OPTIONS = listOf(5, 10, 20, 30, 60)

@Composable
private fun CommitmentStep(
    selected: Int,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Сколько минут в день?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Постоянство важнее объёма",
            fontSize = 16.sp,
            color = TextDim,
        )
        Spacer(Modifier.height(24.dp))
        MINUTE_OPTIONS.forEach { minutes ->
            MinuteCard(
                minutes = minutes,
                isSelected = minutes == selected,
                onClick = { onSelect(minutes) },
            )
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton(text = "Дальше", onClick = onNext)
    }
}

@Composable
private fun MinuteCard(minutes: Int, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) BrandOrange else Color(0xFF2A2A2D)
    val label = when (minutes) {
        5  -> "Лайтово"
        10 -> "Серьёзно"
        20 -> "Интенсив"
        30 -> "Эксперт"
        60 -> "Безумие"
        else -> ""
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(width = if (isSelected) 2.dp else 1.dp, color = borderColor,
                shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "$minutes мин",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) BrandOrange else Color.White,
        )
        Spacer(Modifier.width(16.dp))
        Text(label, fontSize = 15.sp, color = TextDim, modifier = Modifier.weight(1f))
        if (isSelected) {
            Text("✓", color = BrandOrange, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ──────────────────────────────────────────────────────────────────────
//  STEP 4: FIRST WIN — 5 super-easy cognates
// ──────────────────────────────────────────────────────────────────────

private data class Cognate(
    val english: String,
    val spanish: String,
    /** Hint shown above input — teaches an easy phonetic rule. */
    val hint: String,
)

private val COGNATES = listOf(
    Cognate("HOTEL",   "hotel",    "Буква H в испанском не читается"),
    Cognate("BANANA",  "banana",   "Звучит почти как по-русски"),
    Cognate("TAXI",    "taxi",     "Международное слово"),
    Cognate("MUSIC",   "música",   "Под ударением — ú"),
    Cognate("PROBLEM", "problema", "В испанском чаще оканчиваются на -a/-o"),
)

@Composable
private fun FirstWinStep(
    solved: Int,
    onCorrect: () -> Unit,
    onAllDone: () -> Unit,
) {
    // index in COGNATES being shown right now
    val idx = solved.coerceAtMost(COGNATES.lastIndex)
    val current = COGNATES[idx]

    // User input — saveable across rotation
    var input by rememberSaveable(idx) { mutableStateOf("") }
    var showError by remember(idx) { mutableStateOf(false) }

    // Auto-advance when all 5 solved
    LaunchedEffect(solved) {
        if (solved >= 5) onAllDone()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Твоя первая победа",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Эти слова ты уже знаешь — переведи на испанский",
            fontSize = 15.sp,
            color = TextDim,
        )
        Spacer(Modifier.height(20.dp))

        // Progress: 5 dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(5) { i ->
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (i < solved) BrandOrange else SurfaceDark),
                )
            }
        }

        Spacer(Modifier.height(36.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceDark)
                .padding(24.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()) {
                Text(
                    current.english,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                )
                Spacer(Modifier.height(8.dp))
                Text(current.hint, fontSize = 13.sp, color = BrandOrangeDeep,
                    textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        input = it
                        showError = false
                    },
                    placeholder = { Text("по-испански...", color = TextDim) },
                    singleLine = true,
                    isError = showError,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF2A2A2D),
                        unfocusedContainerColor = Color(0xFF2A2A2D),
                        focusedIndicatorColor = BrandOrange,
                        unfocusedIndicatorColor = Color(0xFF3A3A3C),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (showError) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Подсказка: ${current.spanish}",
                        fontSize = 13.sp,
                        color = Color(0xFFFF6B6B),
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Always-accept "Не знаю" advances without crediting solve
            TextButton(
                onClick = {
                    // reveal answer + advance counter (still counts as "shown")
                    showError = true
                    onCorrect()
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("Не знаю", color = TextDim, fontSize = 15.sp)
            }
            Button(
                onClick = {
                    val normalized = input.trim().lowercase()
                        .replace("á", "a").replace("é", "e")
                        .replace("í", "i").replace("ó", "o")
                        .replace("ú", "u")
                    val expected = current.spanish.lowercase()
                        .replace("á", "a").replace("é", "e")
                        .replace("í", "i").replace("ó", "o")
                        .replace("ú", "u")
                    if (normalized == expected) {
                        onCorrect()
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier.weight(2f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Проверить", color = Color.White,
                    fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────
//  STEP 5: DONE
// ──────────────────────────────────────────────────────────────────────

@Composable
private fun DoneStep(onFinish: () -> Unit) {
    val context = LocalContext.current

    // Notification permission launcher (Android 13+)
    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* result not blocking — best-effort */ }

    // Ask once when this screen appears
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val perm = Manifest.permission.POST_NOTIFICATIONS
            val granted = ContextCompat.checkSelfPermission(context, perm) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) notifPermLauncher.launch(perm)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🎉", fontSize = 80.sp)
        Spacer(Modifier.height(24.dp))
        Text(
            "+50 XP",
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BrandOrange,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Отлично! Уже выучил 5 слов",
            fontSize = 20.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Завтра выучишь ещё 10",
            fontSize = 16.sp,
            color = TextDim,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(48.dp))
        PrimaryButton(text = "Поехали!", onClick = onFinish)
    }
}

// ──────────────────────────────────────────────────────────────────────
//  Shared widgets
// ──────────────────────────────────────────────────────────────────────

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
        shape = RoundedCornerShape(16.dp),
    ) {
        Text(text, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}
