package com.spanishapp.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.spanishapp.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.spanishapp.ui.theme.AppColors
import com.spanishapp.util.AuthValidator

// ── 1. Имя ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameEntryScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var touched by remember { mutableStateOf(false) }
    val nameError = if (touched) AuthValidator.getNameError(name.trim()) else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Знакомство") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.btn_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.onboarding_name_question), fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.onboarding_name_subtitle),
                fontSize = 14.sp,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { newValue ->
                    // Имя — максимум 20 символов, блокируем дальнейший ввод.
                    if (newValue.length <= 20) name = newValue
                    touched = true
                },
                label = { Text(stringResource(R.string.onboarding_name_label)) },
                placeholder = { Text(stringResource(R.string.onboarding_name_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, null) },
                isError = nameError != null,
                supportingText = {
                    if (nameError != null) {
                        Text(nameError)
                    } else {
                        Text("${name.length}/20", color = AppColors.TextSecondary)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = {
                    val trimmed = name.trim()
                    if (AuthValidator.getNameError(trimmed) == null) {
                        viewModel.updateName(trimmed)
                        navController.navigate("age_selection")
                    } else {
                        touched = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = name.isNotBlank() && nameError == null
            ) {
                Text("Далее")
            }
        }
    }
}

// ── 2. Возраст ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeSelectionScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var age by remember { mutableIntStateOf(18) }
    val isYoung = age < 13

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Возраст") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.btn_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.onboarding_age_question), fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.onboarding_age_subtitle),
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))

            Text(
                "$age ${stringResource(R.string.onboarding_age_years)}",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )

            Slider(
                value = age.toFloat(),
                onValueChange = { age = it.toInt() },
                valueRange = 5f..99f,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            // Подсказка для младше 13 — GDPR / детская конфиденциальность.
            if (isYoung) {
                Spacer(Modifier.height(24.dp))
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        stringResource(R.string.onboarding_under13_hint),
                        fontSize = 13.sp,
                        color = Color(0xFF6A4F00),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(64.dp))

            Button(
                onClick = {
                    viewModel.updateAge(age)
                    navController.navigate("reason_selection")
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Далее")
            }
        }
    }
}

// ── 3. Причина изучения ────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReasonSelectionScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val reasons = listOf(
        "✈️ Путешествия",
        "💼 Работа / Карьера",
        "🧠 Саморазвитие",
        "🎓 Учёба",
        "❤️ Общение / Семья",
        "🎸 Хобби / Культура"
    )
    var selectedReason by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Цель обучения") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.btn_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.onboarding_reason_question),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            reasons.forEach { reason ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { selectedReason = reason },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedReason == reason)
                            MaterialTheme.colorScheme.primaryContainer
                        else Color.White
                    ),
                    border = if (selectedReason == reason) null
                        else androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(0.4f)
                        )
                ) {
                    // Card containerColor is hardcoded white, so the text must be explicitly
                    // dark — otherwise it inherits onSurface from the dark theme and becomes
                    // unreadable (white-on-white).
                    Text(
                        reason,
                        modifier = Modifier.padding(20.dp),
                        fontSize = 18.sp,
                        color = AppColors.TextPrimary
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    selectedReason?.let {
                        viewModel.updateReason(it)
                        navController.navigate("knowledge_check")
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = selectedReason != null
            ) {
                Text("Далее")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── 4. Уже знаешь / с нуля ─────────────────────────────────────────

private const val PLACEMENT_TEST_QUESTIONS = 20

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeCheckScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Знание языка") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.btn_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppColors.BgWhite)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🇪🇸", fontSize = 64.sp)
            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.onboarding_knowledge_question),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.onboarding_knowledge_subtitle),
                fontSize = 14.sp,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(48.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.selectLevel("A1")
                        viewModel.completeOnboarding()
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                colors = CardDefaults.cardColors(containerColor = AppColors.PurplePale),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🌱", fontSize = 32.sp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        // Card has a light peach background — force dark text so it stays
                        // readable in dark theme too.
                        Text(
                            stringResource(R.string.onboarding_kc_zero),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AppColors.TextPrimary
                        )
                        Text(
                            stringResource(R.string.onboarding_kc_zero_sub),
                            fontSize = 13.sp,
                            color = AppColors.TextSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("placement_test") },
                colors = CardDefaults.cardColors(containerColor = AppColors.Purple),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎯", fontSize = 32.sp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            stringResource(R.string.onboarding_kc_some),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            stringResource(R.string.onboarding_kc_some_sub, PLACEMENT_TEST_QUESTIONS),
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }
    }
}
