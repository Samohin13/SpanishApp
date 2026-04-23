package com.spanishapp.ui.dialogues

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.spanishapp.data.db.dao.DialogueDao
import com.spanishapp.data.db.entity.DialogueEntity
import com.spanishapp.service.SpanishTts
import com.spanishapp.ui.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject

// ── ViewModel ─────────────────────────────────────────────────

@HiltViewModel
class DialoguesViewModel @Inject constructor(
    private val dialogueDao: DialogueDao,
    private val tts: SpanishTts
) : ViewModel() {

    private val _level = MutableStateFlow("A1")
    val level: StateFlow<String> = _level.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val dialogues: StateFlow<List<DialogueEntity>> = _level
        .flatMapLatest { dialogueDao.getByLevel(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setLevel(l: String) { _level.value = l }

    fun markCompleted(dialogue: DialogueEntity) = viewModelScope.launch {
        dialogueDao.update(dialogue.copy(isCompleted = true))
    }

    fun speak(text: String) = tts.speak(text)
}

// ── Screen ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialoguesScreen(
    navController: NavHostController,
    vm: DialoguesViewModel = hiltViewModel()
) {
    val level     by vm.level.collectAsState()
    val dialogues by vm.dialogues.collectAsState()
    val levels    = listOf("A1", "A2", "B1", "B2")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Диалоги") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Фильтр уровней
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(levels) { lvl ->
                    FilterChip(
                        selected = level == lvl,
                        onClick  = { vm.setLevel(lvl) },
                        label    = { Text(lvl) }
                    )
                }
            }

            if (dialogues.isEmpty()) {
                // Пустое состояние — контент ещё не добавлен
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text("💬", fontSize = 56.sp)
                        Text(
                            "Диалоги для уровня $level",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Скоро здесь появятся ситуационные диалоги:\nв ресторане, в магазине, в аэропорту и другие.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AppColors.Info.copy(alpha = 0.1f)
                        ) {
                            Text(
                                "🚧 В разработке",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = AppColors.Info
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(dialogues, key = { it.id }) { dialogue ->
                        DialogueCard(
                            dialogue    = dialogue,
                            onSpeak     = { vm.speak(it) },
                            onComplete  = { vm.markCompleted(dialogue) }
                        )
                    }
                }
            }
        }
    }
}

// ── Карточка диалога ──────────────────────────────────────────

@Composable
private fun DialogueCard(
    dialogue: DialogueEntity,
    onSpeak: (String) -> Unit,
    onComplete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Парсим JSON строки диалога
    val lines: List<DialogueLine> = remember(dialogue.linesJson) {
        parseDialogueLines(dialogue.linesJson)
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (dialogue.isCompleted)
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.surface,
        tonalElevation = if (dialogue.isCompleted) 0.dp else 1.dp,
        modifier = Modifier.fillMaxWidth().animateContentSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Иконка ситуации
                Surface(
                    shape = CircleShape,
                    color = AppColors.Teal.copy(alpha = 0.12f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("💬", fontSize = 20.sp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(dialogue.title, style = MaterialTheme.typography.titleMedium,
                             fontWeight = FontWeight.Bold)
                        if (dialogue.isCompleted) {
                            Icon(Icons.Default.CheckCircle, null,
                                 tint = AppColors.Teal, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(dialogue.situation, style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Кнопка разворота
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.PlayArrow else Icons.Default.PlayArrow,
                        null,
                        tint = AppColors.Teal
                    )
                }
            }

            // Строки диалога
            if (expanded && lines.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    lines.forEach { line ->
                        DialogueLineRow(line, onSpeak)
                    }
                }

                if (!dialogue.isCompleted) {
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("✅ Диалог пройден") }
                }
            }
        }
    }
}

// ── Строка диалога ────────────────────────────────────────────

data class DialogueLine(
    val speaker: String,   // "A" или "B"
    val es: String,
    val ru: String
)

fun parseDialogueLines(json: String): List<DialogueLine> = runCatching {
    val arr = JSONArray(json)
    (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        DialogueLine(
            speaker = obj.optString("speaker", "A"),
            es      = obj.optString("es", ""),
            ru      = obj.optString("ru", "")
        )
    }
}.getOrDefault(emptyList())

@Composable
private fun DialogueLineRow(line: DialogueLine, onSpeak: (String) -> Unit) {
    val isA = line.speaker == "A"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isA) Arrangement.Start else Arrangement.End
    ) {
        if (isA) {
            // Аватар спикера A
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AppColors.Teal.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) { Text("A", fontWeight = FontWeight.Bold, color = AppColors.Teal, fontSize = 12.sp) }
            Spacer(Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = if (isA) 4.dp else 14.dp,
                topEnd   = if (isA) 14.dp else 4.dp,
                bottomStart = 14.dp,
                bottomEnd   = 14.dp
            ),
            color = if (isA) AppColors.Teal.copy(alpha = 0.1f)
                    else AppColors.Terracotta.copy(alpha = 0.1f),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        line.es,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onSpeak(line.es) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, null,
                             modifier = Modifier.size(16.dp),
                             tint = if (isA) AppColors.Teal else AppColors.Terracotta)
                    }
                }
                Text(
                    line.ru,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (!isA) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AppColors.Terracotta.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) { Text("B", fontWeight = FontWeight.Bold, color = AppColors.Terracotta, fontSize = 12.sp) }
        }
    }
}
