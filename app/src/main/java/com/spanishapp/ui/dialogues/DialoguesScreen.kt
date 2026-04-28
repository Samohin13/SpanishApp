package com.spanishapp.ui.dialogues

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Диалоги", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
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
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(levels) { lvl ->
                    val isSelected = level == lvl
                    FilterChip(
                        selected = isSelected,
                        onClick  = { vm.setLevel(lvl) },
                        label    = { Text(lvl, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        shape    = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.Teal.copy(alpha = 0.15f),
                            selectedLabelColor = AppColors.Teal
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            selectedBorderColor = AppColors.Teal
                        )
                    )
                }
            }

            if (dialogues.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text("💬", fontSize = 64.sp)
                        Text(
                            "Уровень $level в разработке",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Скоро здесь появятся живые диалоги для практики языка.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(dialogues, key = { it.id }) { dialogue ->
                        DialogueCardContent(
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

@Composable
private fun DialogueCardContent(
    dialogue: DialogueEntity,
    onSpeak: (String) -> Unit,
    onComplete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "rotation")

    val lines = remember(dialogue.linesJson) { parseDialogueLines(dialogue.linesJson) }

    Surface(
        onClick = { expanded = !expanded },
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (dialogue.isCompleted) AppColors.Teal.copy(alpha = 0.3f) 
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth().animateContentSize()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(AppColors.Teal.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (dialogue.isCompleted) "✅" else "💬", fontSize = 22.sp)
                }
                
                Spacer(Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        dialogue.title, 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (dialogue.isCompleted) AppColors.Teal else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        dialogue.situation, 
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    null,
                    modifier = Modifier.graphicsLayer { rotationZ = rotation },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            if (expanded && lines.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    lines.forEach { line ->
                        DialogueLineItem(line, onSpeak)
                    }
                }

                if (!dialogue.isCompleted) {
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { 
                            onComplete()
                            expanded = false
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Teal)
                    ) {
                        Text("ЗАВЕРШИТЬ ПРАКТИКУ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class DialogueLine(val speaker: String, val es: String, val ru: String)

fun parseDialogueLines(json: String): List<DialogueLine> = runCatching {
    val arr = JSONArray(json)
    (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        DialogueLine(
            speaker = obj.optString("speaker", "A"),
            es      = obj.getString("es"),
            ru      = obj.getString("ru")
        )
    }
}.getOrDefault(emptyList())

@Composable
private fun DialogueLineItem(line: DialogueLine, onSpeak: (String) -> Unit) {
    val isA = line.speaker == "A"
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isA) Arrangement.Start else Arrangement.End
    ) {
        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isA) Alignment.Start else Alignment.End
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (isA) 4.dp else 16.dp,
                    topEnd   = if (isA) 16.dp else 4.dp,
                    bottomStart = 16.dp,
                    bottomEnd   = 16.dp
                ),
                color = if (isA) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.clickable { onSpeak(line.es) }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            line.es,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        line.ru,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
