package com.spanishapp.ui.settings

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── ViewModel ─────────────────────────────────────────────────

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userProgressDao: UserProgressDao,
    private val appPreferences: AppPreferences
) : ViewModel() {

    val progress: StateFlow<UserProgressEntity> = userProgressDao.getProgress()
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserProgressEntity())

    val ttsEnabled: StateFlow<Boolean> = appPreferences.ttsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = true)

    fun toggleTts(enabled: Boolean) = viewModelScope.launch {
        appPreferences.setTtsEnabled(enabled)
    }

    fun updateName(name: String) = viewModelScope.launch {
        val p = progress.value
        userProgressDao.update(p.copy(displayName = name.trim().ifBlank { "Estudiante" }))
    }

    fun updateLevel(level: String) = viewModelScope.launch {
        val p = progress.value
        userProgressDao.update(p.copy(currentLevel = level))
    }

    fun updateGoal(minutes: Int) = viewModelScope.launch {
        val p = progress.value
        userProgressDao.update(p.copy(dailyGoalMinutes = minutes))
    }
}

// ── Screen ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    vm: SettingsViewModel = hiltViewModel()
) {
    val progress   by vm.progress.collectAsState()
    val ttsEnabled by vm.ttsEnabled.collectAsState()
    val context    = LocalContext.current

    // Dialog states
    var showNameDialog by remember { mutableStateOf(false) }
    var showLevelDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
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
                .padding(horizontal = 16.dp)
        ) {
            SettingsHeader("Профиль")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsItem(
                        title = "Имя пользователя",
                        summary = progress.displayName,
                        icon = Icons.Default.Person,
                        onClick = { showNameDialog = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingsItem(
                        title = "Уровень испанского",
                        summary = when (progress.currentLevel) {
                            "A1" -> "A1 — Начинающий"
                            "A2" -> "A2 — Элементарный"
                            "B1" -> "B1 — Средний"
                            "B2" -> "B2 — Выше среднего"
                            else -> progress.currentLevel
                        },
                        icon = Icons.Default.Translate,
                        onClick = { showLevelDialog = true }
                    )
                }
            }

            SettingsHeader("Обучение")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsItem(
                        title = "Дневная цель",
                        summary = "${progress.dailyGoalMinutes} минут в день",
                        icon = Icons.Default.Timer,
                        onClick = { showGoalDialog = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingsItem(
                        title = "Озвучка (TTS)",
                        summary = if (ttsEnabled) "Слова озвучиваются" else "Без звука",
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        trailing = {
                            Switch(
                                checked = ttsEnabled,
                                onCheckedChange = { vm.toggleTts(it) }
                            )
                        },
                        onClick = { vm.toggleTts(!ttsEnabled) }
                    )
                }
            }

            SettingsHeader("Приложение")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsItem(
                        title = "Поделиться",
                        summary = "Рассказать друзьям про HablaRu",
                        icon = Icons.Default.Share,
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "HablaRu — учи испанский!")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Привет! Я учу испанский с приложением HablaRu 🇪🇸\n" +
                                    "Карточки, игры, тренажёр произношения — всё бесплатно!\n" +
                                    "Попробуй и ты!"
                                )
                            }
                            context.startActivity(Intent.createChooser(intent, "Поделиться HablaRu"))
                        }
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }

    // ── Dialogs ───────────────────────────────────────────────

    if (showNameDialog) {
        NameEditDialog(
            initialName = progress.displayName,
            onDismiss = { showNameDialog = false }) { 
                vm.updateName(it)
                showNameDialog = false
            }
    }

    if (showLevelDialog) {
        SingleChoiceDialog(
            title = "Уровень испанского",
            options = listOf(
                "A1" to "Начинающий",
                "A2" to "Элементарный",
                "B1" to "Средний",
                "B2" to "Выше среднего"
            ),
            selectedOption = progress.currentLevel,
            onDismiss = { showLevelDialog = false },
            onSelect = {
                vm.updateLevel(it)
                showLevelDialog = false
            }
        )
    }

    if (showGoalDialog) {
        SingleChoiceDialog(
            title = "Дневная цель",
            options = listOf(
                "5" to "5 минут — Легко",
                "10" to "10 минут — Оптимально",
                "15" to "15 минут — Интенсивно",
                "20" to "20 минут — Серьёзно",
                "30" to "30 минут — Профи"
            ),
            selectedOption = progress.dailyGoalMinutes.toString(),
            onDismiss = { showGoalDialog = false },
            onSelect = {
                vm.updateGoal(it.toInt())
                showGoalDialog = false
            }
        )
    }

}

// ── Components ────────────────────────────────────────────────

@Composable
private fun SettingsHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    title: String,
    summary: String? = null,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (summary != null) {
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (trailing != null) {
                Spacer(Modifier.width(8.dp))
                trailing()
            }
        }
    }
}

@Composable
private fun NameEditDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Имя пользователя") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
private fun SingleChoiceDialog(
    title: String,
    options: List<Pair<String, String>>,
    selectedOption: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                options.forEach { (key, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(key) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (key == selectedOption),
                            onClick = { onSelect(key) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        }
    )
}
