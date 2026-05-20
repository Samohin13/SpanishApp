package com.spanishapp.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*

@Composable
fun LessonIntroScreen(
    navController: NavHostController,
    unitId: Int,
    lessonIndex: Int,
    viewModel: LessonIntroViewModel
) {
    val haptic = com.spanishapp.ui.components.rememberCheckedHaptic()

    // Достаём данные из RoadmapData по индексам
    val unit   = remember(unitId) { RoadmapData.units.getOrNull(unitId - 1) }
    val lesson = remember(unit, lessonIndex) { unit?.lessons?.getOrNull(lessonIndex) }

    if (unit == null || lesson == null) {
        // popBackStack must run outside the composition phase, otherwise
        // NavController complains "Cannot popBackStack during composition".
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    // Per-block Lottie. Каждый из 16 блоков (4 × A1 + 4 × A2 + 4 × B1 + 4 × B2)
    // имеет свою анимацию — все 15 уроков внутри блока её разделяют.
    // Файлы лежат в res/raw/lottie_block_1..lottie_block_16.
    // Тип урока (vocab/grammar/quiz) больше не влияет — это давало 3 одинаковых
    // анимации на все 240 уроков, что и хотели заменить.
    val lottieRes = lottieForUnit(unit.id)

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    // Use the lesson's parent unit colour as accent so each block keeps
    // its own brand hue (Purple/Teal/Green/Orange) — was falling back to
    // MaterialTheme primary which made every block look identical.
    val accentColor = unit.color

    val description = when (lesson.type) {
        "vocab"   -> androidx.compose.ui.res.stringResource(com.spanishapp.R.string.lesson_intro_vocab)
        "grammar" -> androidx.compose.ui.res.stringResource(com.spanishapp.R.string.lesson_intro_grammar)
        "phrase"  -> androidx.compose.ui.res.stringResource(com.spanishapp.R.string.lesson_intro_phrase)
        else      -> androidx.compose.ui.res.stringResource(com.spanishapp.R.string.lesson_intro_quiz)
    }

    val cefrBadge = unit.cefrLevel  // "A1", "A2", "B1", "B2"

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // CEFR badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = cefrBadge,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(accentColor.copy(alpha = 0.05f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(140.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = lesson.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = unit.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(24.dp))

            // ── Карточка теории убрана из этого экрана в v1.3.3 ──
            // По фидбэку: юзеры её не замечали (теория терялась на экране
            // с большой кнопкой «Поехали»). Теперь теория автоматически
            // открывается оверлеем поверх первого экрана LessonSession,
            // и доступна по кнопке 📖 в TopBar в любой момент урока.

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RewardItem("✨ +15 XP", androidx.compose.ui.res.stringResource(com.spanishapp.R.string.lesson_reward_xp))
                    RewardItem("🔓", androidx.compose.ui.res.stringResource(com.spanishapp.R.string.lesson_reward_next))
                    RewardItem("🎯", androidx.compose.ui.res.stringResource(com.spanishapp.R.string.lesson_reward_progress))
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                    val route = buildActivityRoute(lesson, unit.cefrLevel, unitId, lessonIndex)
                    val lessonId = lesson.id ?: "u${unitId}_l${lessonIndex}"
                    val isCheckpoint = com.spanishapp.data.checkpoint.CheckpointContentData.byId(lessonId) != null

                    // v1.18.11 (BUG-025): для CHECKPOINT и CONTENT уроков — НЕ
                    // помечаем automark здесь. Эти типы имеют собственную
                    // completion-логику:
                    //   - Content (LessonContentScreen/Session) — на кнопку «Понятно»
                    //     или VictoryScreen
                    //   - Checkpoint — CheckpointSessionViewModel помечает при
                    //     accuracy ≥ 70%
                    //
                    // Для остальных (vocab/phrase/grammar/quiz) automark при
                    // старте остаётся — целевые экраны (flashcards/grammar/quiz)
                    // не имеют связи с lesson_progress и сами не помечают.
                    // Это TODO для будущего рефактора.
                    val hasOwnCompletion = isCheckpoint || lesson.type == "content"
                    if (!hasOwnCompletion) {
                        viewModel.markLessonComplete(unitId, lessonIndex)
                    }

                    navController.navigate(route) {
                        // Use substituted route — Nav Compose's popUpTo
                        // matches concrete routes, not pattern templates.
                        popUpTo("lesson_intro/$unitId/$lessonIndex") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(androidx.compose.ui.res.stringResource(com.spanishapp.R.string.lesson_start_button), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }

            TextButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(androidx.compose.ui.res.stringResource(com.spanishapp.R.string.lesson_later), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun buildActivityRoute(
    lesson: RoadmapLesson,
    cefrLevel: String,
    unitId: Int,
    lessonIndex: Int
): String {
    val cat = lesson.category
    val lessonId = lesson.id ?: "u${unitId}_l${lessonIndex}"

    // Если для этого урока есть checkpoint-сценарий — отправляем туда вместо обычной сессии.
    // Используется для всех 21 финалов блоков из xlsx (u1_l14, u4_l14, u8_l14 и т.д.).
    if (com.spanishapp.data.checkpoint.CheckpointContentData.byId(lessonId) != null) {
        return "checkpoint/$lessonId"
    }

    val hasSession = LessonContentData.lessons[lessonId] != null
    return when (lesson.type) {
        "content" -> if (hasSession) "lesson_session/$unitId/$lessonIndex"
                     else "lesson_content/$unitId/$lessonIndex"
        "vocab"   -> "flashcards_session?level=$cefrLevel&category=$cat&direction=ES_TO_RU"
        "phrase"  -> "flashcards_session?level=$cefrLevel&category=$cat&direction=MIXED"
        "grammar" -> "grammar"
        "quiz"    -> "quiz?type=$cat"
        else      -> "flashcards_session?level=$cefrLevel&category=$cat&direction=ES_TO_RU"
    }
}

@Composable
private fun RewardItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

// ─── Per-block Lottie animations ─────────────────────────────────────────────
//
// 16 блоков × 1 уникальная тематическая анимация = 16 файлов в res/raw/.
// Все скачаны с lottiefiles.com (Lottie Simple License, бесплатно).
//
//   "1"  → 🚀 rocket+fireworks         (A1 Взлёт)              — старт пути
//   "2"  → 👫 boy + girl               (A1 Мой мир)            — семья
//   "3"  → 🛒 groceries / еда          (A1 Действие)           — глаголы + еда
//   "4"  → 📍 pins + locations         (A1 Выживание)          — транспорт
//   "5"  → ⏱️ stopwatch                (A2 В прошлом)          — прошлое
//   "6"  → ⏳ countdown                (A2 Раньше и сейчас)    — время
//   "7"  → ✅ checkmark + stars        (A2 Сейчас и скоро)     — прогресс
//   "8"  → 🎉 confetti + frog          (A2 Мечты и планы)      — фан
//   "9"  → 🌊 waving character         (B1 Subjuntivo)         — желания
//   "10" → 🖱️ square + mouse          (B1 Condicional)        — выбор
//   "11" → 💕 heart eyes burst         (B1 Comunicación)       — эмоции
//   "12" → 🤖 robot                    (B1 Vocabulario)        — обучение
//   "13" → 🌀 frame layers             (B2 Subjuntivo Avanzado)— абстракция
//   "14" → 🎨 shape transformation     (B2 Pasiva)             — трансформации
//   "15" → 💬 abstract communication   (B2 Comunicación Formal)— общение
//   "16" → 👴 popeye / старик          (B2 Léxico y Cultura)   — литература
private fun lottieForUnit(unitId: String): Int {
    return when (unitId) {
        "1"  -> com.spanishapp.R.raw.lottie_block_1
        "2"  -> com.spanishapp.R.raw.lottie_block_2
        "3"  -> com.spanishapp.R.raw.lottie_block_3
        "4"  -> com.spanishapp.R.raw.lottie_block_4
        "5"  -> com.spanishapp.R.raw.lottie_block_5
        "6"  -> com.spanishapp.R.raw.lottie_block_6
        "7"  -> com.spanishapp.R.raw.lottie_block_7
        "8"  -> com.spanishapp.R.raw.lottie_block_8
        "9"  -> com.spanishapp.R.raw.lottie_block_9
        "10" -> com.spanishapp.R.raw.lottie_block_10
        "11" -> com.spanishapp.R.raw.lottie_block_11
        "12" -> com.spanishapp.R.raw.lottie_block_12
        "13" -> com.spanishapp.R.raw.lottie_block_13
        "14" -> com.spanishapp.R.raw.lottie_block_14
        "15" -> com.spanishapp.R.raw.lottie_block_15
        "16" -> com.spanishapp.R.raw.lottie_block_16
        else -> com.spanishapp.R.raw.lottie_block_1   // safe fallback
    }
}
