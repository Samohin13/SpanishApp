package com.spanishapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.spanishapp.data.db.dao.UserProgressDao
import com.spanishapp.data.db.entity.UserProgressEntity
import com.spanishapp.data.prefs.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── ViewModel ─────────────────────────────────────────────────

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userProgressDao: UserProgressDao,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _progress = MutableStateFlow(UserProgressEntity())
    val progress: StateFlow<UserProgressEntity> = _progress.asStateFlow()

    val ttsEnabled: StateFlow<Boolean> = appPreferences.ttsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    init {
        viewModelScope.launch {
            val p = userProgressDao.getProgressOnce()
            if (p != null) _progress.value = p
        }
    }

    fun save(name: String, level: String, goalMinutes: Int) = viewModelScope.launch {
        val p = userProgressDao.getProgressOnce() ?: UserProgressEntity()
        userProgressDao.update(
            p.copy(
                displayName      = name.trim().ifBlank { "Estudiante" },
                currentLevel     = level,
                dailyGoalMinutes = goalMinutes.coerceIn(5, 120)
            )
        )
    }

    fun toggleTts(enabled: Boolean) = viewModelScope.launch {
        appPreferences.setTtsEnabled(enabled)
    }
}

// ── Screen ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    vm: SettingsViewModel = hiltViewModel()
) {
    val progress  by vm.progress.collectAsState()
    val ttsEnabled by vm.ttsEnabled.collectAsState()

    var name        by remember(progress.displayName) { mutableStateOf(progress.displayName) }
    var level       by remember(progress.currentLevel) { mutableStateOf(progress.currentLevel) }
    var goalMinutes by remember(progress.dailyGoalMinutes) { mutableStateOf(progress.dailyGoalMinutes) }

    val levels = listOf("A1", "A2", "B1", "B2")
    val goalOptions = listOf(5, 10, 15, 20, 30)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        vm.save(name, level, goalMinutes)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Check, "Сохранить")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Name ─────────────────────────────────────────
            SettingsSection("Профиль") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Твоё имя") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── Sound toggle ──────────────────────────────────
            SettingsSection("Звук") {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "🔊 Озвучка (TTS)",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                if (ttsEnabled) "Включена — слова озвучиваются"
                                else "Выключена — без звука",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = ttsEnabled,
                            onCheckedChange = { vm.toggleTts(it) }
                        )
                    }
                }
            }

            // ── Spanish level ─────────────────────────────────
            SettingsSection("Уровень испанского") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    levels.forEach { lvl ->
                        val desc = when (lvl) {
                            "A1" -> "Начинающий — базовые слова и фразы"
                            "A2" -> "Элементарный — простые разговоры"
                            "B1" -> "Средний — свободное общение на знакомые темы"
                            "B2" -> "Выше среднего — сложные тексты и дискуссии"
                            else -> ""
                        }
                        Surface(
                            onClick = { level = lvl },
                            shape = RoundedCornerShape(14.dp),
                            color = if (level == lvl)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface,
                            tonalElevation = if (level == lvl) 0.dp else 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(lvl, fontWeight = FontWeight.Bold,
                                         style = MaterialTheme.typography.bodyLarge)
                                    Text(desc, style = MaterialTheme.typography.bodySmall,
                                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                RadioButton(selected = level == lvl, onClick = { level = lvl })
                            }
                        }
                    }
                }
            }

            // ── Daily goal ────────────────────────────────────
            SettingsSection("Дневная цель") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    goalOptions.forEach { min ->
                        Surface(
                            onClick = { goalMinutes = min },
                            shape = RoundedCornerShape(14.dp),
                            color = if (goalMinutes == min)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface,
                            tonalElevation = if (goalMinutes == min) 0.dp else 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val label = when (min) {
                                    5  -> "5 минут — Лёгкий старт"
                                    10 -> "10 минут — Рекомендуем"
                                    15 -> "15 минут — Интенсивно"
                                    20 -> "20 минут — Серьёзный подход"
                                    30 -> "30 минут — Профессионал"
                                    else -> "$min минут"
                                }
                                Text(label, style = MaterialTheme.typography.bodyMedium,
                                     fontWeight = if (goalMinutes == min) FontWeight.SemiBold
                                                  else FontWeight.Normal)
                                RadioButton(selected = goalMinutes == min,
                                            onClick = { goalMinutes = min })
                            }
                        }
                    }
                }
            }

            // ── Save button ───────────────────────────────────
            Button(
                onClick = {
                    vm.save(name, level, goalMinutes)
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(8.dp))
                Text("Сохранить", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        content()
    }
}
