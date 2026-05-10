package com.spanishapp.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.spanishapp.ui.theme.AppColors

private data class Question(
    val text: String,
    val options: List<String>,
    val correctIndex: Int
)

// 20 вопросов: 5 A1 + 5 A2 + 5 B1 + 5 B2.
// Случайно перемешиваем при каждом запуске чтобы юзер не запоминал
// порядок при повторном прохождении.
private val ALL_QUESTIONS: List<Question> = listOf(
    // ── A1: базовая лексика и простая грамматика ──
    Question("Что значит «familia»?", listOf("Еда", "Семья", "Работа", "Город"), 1),
    Question("Как сказать «Здравствуйте» по-испански?", listOf("Gracias", "Adiós", "Hola", "Por favor"), 2),
    Question("«Yo ___ estudiante.» Какой глагол?", listOf("soy", "estoy", "tengo", "voy"), 0),
    Question("Что значит «¿De dónde eres?»", listOf("Сколько тебе лет?", "Откуда ты?", "Как тебя зовут?", "Что делаешь?"), 1),
    Question("Выберите правильную форму: «Mi madre ___ médica.»", listOf("es", "está", "hay", "tiene"), 0),

    // ── A2: прошедшее время, частые конструкции ──
    Question("«Ayer yo ___ al mercado.» (ir)", listOf("voy", "iré", "vaya", "fui"), 3),
    Question("Что значит «¿Cuánto cuesta?»", listOf("Как тебя зовут?", "Который час?", "Где находится?", "Сколько стоит?"), 3),
    Question("«Esta camisa es ___ que la otra.»", listOf("más bonita", "muy bonita", "tan bonita", "bonitísima"), 0),
    Question("Какое значение у «Acabo de comer»?", listOf("Я скоро поем", "Я только что поел", "Я ем", "Я не буду есть"), 1),
    Question("Выберите перевод «давай побудем здесь»: «___ aquí.»", listOf("Quedémonos", "Nos quedaríamos", "Quedamos", "Nos quedaron"), 0),

    // ── B1: subjuntivo, condicional, более тонкие нюансы ──
    Question("«Es importante que tú ___ (estudiar)»", listOf("estudias", "estudies", "estudiará", "estudié"), 1),
    Question("Что значит «a lo mejor»?", listOf("Никогда", "Всегда", "Возможно", "Обязательно"), 2),
    Question("«Si tuviera dinero, ___ a Japón.»", listOf("voy", "iría", "iba", "vaya"), 1),
    Question("«Cuando ___ a casa, llámame.» (будущее)", listOf("llegas", "llegues", "llegabas", "llegarás"), 1),
    Question("Выберите перевод «не было ничего вкусного»: «No había nada ___»", listOf("rico", "ricas", "rica", "rico que comer"), 3),

    // ── B2: продвинутая грамматика и идиомы ──
    Question("«No hay mal que por bien no venga» означает:", listOf("Всё проходит", "Чем хуже, тем лучше", "Нет худа без добра", "Удача переменчива"), 2),
    Question("«Si hubiera sabido, ___ antes»", listOf("vendría", "vengo", "vine", "habría venido"), 3),
    Question("Что значит «echar de menos»?", listOf("Скучать по чему-то", "Уменьшать", "Отказываться", "Бросать"), 0),
    Question("«Una vez que ___ el informe, lo enviaremos.»", listOf("terminamos", "terminemos", "termináramos", "terminaríamos"), 1),
    Question("«Por mucho que ___, no me convencerás.»", listOf("dices", "digas", "decías", "dirás"), 1),
)

// Перемешиваем единожды на старте теста — список затем используется как есть.
private val QUESTIONS: List<Question> = ALL_QUESTIONS.shuffled()

private fun calcLevel(correct: Int) = when {
    // 20 вопросов — пороги пропорциональны (75/55/30%).
    correct >= 15 -> "B2"
    correct >= 11 -> "B1"
    correct >= 6  -> "A2"
    else          -> "A1"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacementTestScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var selectedIndex by remember { mutableIntStateOf(-1) }
    var answered by remember { mutableStateOf(false) }
    var showAbortDialog by remember { mutableStateOf(false) }

    val question = QUESTIONS[currentIndex]
    val progress = (currentIndex + 1).toFloat() / QUESTIONS.size

    if (showAbortDialog) {
        AlertDialog(
            onDismissRequest = { showAbortDialog = false },
            title = { Text(androidx.compose.ui.res.stringResource(com.spanishapp.R.string.placement_abort_title)) },
            text = { Text(androidx.compose.ui.res.stringResource(com.spanishapp.R.string.placement_abort_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showAbortDialog = false
                    navController.popBackStack()
                }) { Text(androidx.compose.ui.res.stringResource(com.spanishapp.R.string.placement_abort_confirm), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showAbortDialog = false }) {
                    Text(androidx.compose.ui.res.stringResource(com.spanishapp.R.string.placement_abort_continue))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    com.spanishapp.ui.components.AnimatedScreenTitle(
                        text = "🎯 ${currentIndex + 1} / ${QUESTIONS.size}",
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentIndex == 0 && !answered) navController.popBackStack()
                        else showAbortDialog = true
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            "✓ $correctCount",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
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
            .padding(24.dp)
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = AppColors.Purple,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(Modifier.height(40.dp))

        Text(
            question.text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            lineHeight = 28.sp
        )

        Spacer(Modifier.height(32.dp))

        val unselectedSurface = MaterialTheme.colorScheme.surface
        val unselectedBorder  = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        val selectedBg        = MaterialTheme.colorScheme.primaryContainer
        val selectedFg        = MaterialTheme.colorScheme.onPrimaryContainer
        val selectedBorder    = MaterialTheme.colorScheme.primary
        // Solid colors for post-check state — readable on both light & dark.
        val correctBg   = Color(0xFF1B5E20)   // dark green
        val correctFg   = Color.White
        val wrongBg     = Color(0xFF8B0000)   // dark red
        val wrongFg     = Color.White
        val onSurface   = MaterialTheme.colorScheme.onSurface

        question.options.forEachIndexed { index, option ->
            val isCorrect      = index == question.correctIndex
            val isPickedWrong  = index == selectedIndex && !isCorrect

            val bgColor = when {
                !answered -> if (selectedIndex == index) selectedBg else unselectedSurface
                isCorrect -> correctBg
                isPickedWrong -> wrongBg
                else -> unselectedSurface
            }
            val borderColor = when {
                !answered -> if (selectedIndex == index) selectedBorder else unselectedBorder
                isCorrect -> correctBg
                isPickedWrong -> wrongBg
                else -> unselectedBorder
            }
            val textColor = when {
                !answered -> if (selectedIndex == index) selectedFg else onSurface
                isCorrect -> correctFg
                isPickedWrong -> wrongFg
                else -> onSurface
            }
            val borderWidth = if (!answered && selectedIndex == index) 2.dp else 1.5.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
                    .clickable(enabled = !answered) {
                        selectedIndex = index
                        answered = true
                        if (index == question.correctIndex) correctCount++
                    }
                    .padding(16.dp)
            ) {
                Text(option, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = textColor)
            }
        }

        Spacer(Modifier.weight(1f))

        AnimatedVisibility(visible = answered) {
            Button(
                onClick = {
                    if (currentIndex < QUESTIONS.size - 1) {
                        currentIndex++
                        selectedIndex = -1
                        answered = false
                    } else {
                        val level = calcLevel(correctCount)
                        navController.navigate("placement_result/$level") {
                            popUpTo("placement_test") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple)
            ) {
                Text(
                    if (currentIndex < QUESTIONS.size - 1)
                        androidx.compose.ui.res.stringResource(com.spanishapp.R.string.placement_next_question)
                    else
                        androidx.compose.ui.res.stringResource(com.spanishapp.R.string.placement_show_result),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    }
}

@Composable
fun PlacementResultScreen(
    navController: NavHostController,
    level: String,
    viewModel: AuthViewModel = hiltViewModel()
) {

    val (emoji, title, description) = when (level) {
        "B2" -> Triple("🏆", "Впечатляет!", "Ты на продвинутом уровне.\nПрограмма настроена на B2.")
        "B1" -> Triple("🚀", "Ты уже многое знаешь!", "Хороший средний уровень.\nПрограмма настроена на B1.")
        "A2" -> Triple("⭐", "Хорошая база!", "Ты знаешь основы испанского.\nПрограмма настроена на A2.")
        else -> Triple("🌱", "Отличное начало!", "Всё начинается с первого шага.\nПрограмма настроена на A1.")
    }

    val isUpcomingLevel = level in listOf("A2", "B1", "B2")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 72.sp)
        Spacer(Modifier.height(24.dp))
        Text(
            title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .background(AppColors.PurplePale, RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                "Уровень $level",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.Purple
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(
            description,
            fontSize = 15.sp,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        if (isUpcomingLevel) {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .background(Color(0xFFFFF8E1), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    "⏳ Контент $level скоро появится.\nПока начнём с повторения основ на A1 — это всегда полезно!",
                    fontSize = 13.sp,
                    color = Color(0xFF795548),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = {
                viewModel.selectLevel(level)
                viewModel.completeOnboarding()
                navController.navigate("home") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple)
        ) {
            Text("Начать обучение", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = {
            navController.navigate("level_selection") {
                popUpTo("placement_result/$level") { inclusive = true }
            }
        }) {
            Text(androidx.compose.ui.res.stringResource(com.spanishapp.R.string.placement_change_level), color = AppColors.TextSecondary)
        }
    }
}
