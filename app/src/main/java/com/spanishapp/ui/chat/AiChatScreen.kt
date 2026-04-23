package com.spanishapp.ui.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.spanishapp.data.db.entity.ChatMessageEntity
import com.spanishapp.data.repository.AiChatRepository
import com.spanishapp.service.SpanishTts
import com.spanishapp.ui.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── ViewModel ─────────────────────────────────────────────────

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val repo: AiChatRepository,
    private val tts: SpanishTts
) : ViewModel() {

    private val sessionId = "default"

    val messages: StateFlow<List<ChatMessageEntity>> = repo.getMessages(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun send(text: String) {
        if (text.isBlank() || _isSending.value) return
        _isSending.value = true
        _error.value = null
        viewModelScope.launch {
            val result = repo.sendMessage(text.trim(), sessionId)
            result.onFailure { e ->
                _error.value = when {
                    e.message?.contains("401") == true -> "Неверный API ключ Anthropic"
                    e.message?.contains("429") == true -> "Превышен лимит запросов, подожди"
                    e.message?.contains("network") == true ||
                    e.message?.contains("timeout") == true -> "Нет интернета"
                    else -> "Ошибка: ${e.message}"
                }
            }
            _isSending.value = false
        }
    }

    fun clearError() { _error.value = null }

    fun newChat() = viewModelScope.launch { repo.clearSession(sessionId) }

    fun speak(text: String) = viewModelScope.launch { tts.speak(text) }
}

// ── Screen ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    navController: NavHostController,
    vm: AiChatViewModel = hiltViewModel()
) {
    val messages  by vm.messages.collectAsState()
    val isSending by vm.isSending.collectAsState()
    val error     by vm.error.collectAsState()
    var input     by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("ИИ-репетитор")
                        Text(
                            "Практика испанского с Claude",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = vm::newChat) {
                        Icon(Icons.Default.AddComment, "Новый чат",
                             tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Messages ────────────────────────────────────
            Box(modifier = Modifier.weight(1f)) {
                if (messages.isEmpty()) {
                    WelcomeHint()
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            ChatBubble(
                                message = msg,
                                onSpeak = { vm.speak(msg.content) }
                            )
                        }
                        // Typing indicator
                        if (isSending) {
                            item { TypingIndicator() }
                        }
                    }
                }
            }

            // ── Error ───────────────────────────────────────
            AnimatedVisibility(error != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            error ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = vm::clearError) { Text("OK") }
                    }
                }
            }

            // ── Input ────────────────────────────────────────
            Surface(tonalElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text("Escribe en español o en ruso…") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        minLines = 1,
                        maxLines = 4,
                        enabled = !isSending
                    )
                    FilledIconButton(
                        onClick = { vm.send(input); input = "" },
                        enabled = input.isNotBlank() && !isSending,
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = AppColors.Terracotta
                        )
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, "Отправить")
                        }
                    }
                }
            }
        }
    }
}

// ── Chat bubble ───────────────────────────────────────────────

@Composable
private fun ChatBubble(message: ChatMessageEntity, onSpeak: () -> Unit) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            // Bot avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(50))
                    .background(AppColors.Terracotta.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Text("🤖", fontSize = 16.sp) }
            Spacer(Modifier.width(6.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp, topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 18.dp
                ),
                color = if (isUser)
                    AppColors.Terracotta.copy(alpha = 0.15f)
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        message.content,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // TTS button for assistant
            if (!isUser) {
                IconButton(
                    onClick = onSpeak,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.VolumeUp, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        if (isUser) {
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(50))
                    .background(AppColors.Teal.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Text("👤", fontSize = 16.sp) }
        }
    }
}

// ── Typing indicator ──────────────────────────────────────────

@Composable
private fun TypingIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(50))
                .background(AppColors.Terracotta.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) { Text("🤖", fontSize = 16.sp) }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}

// ── Welcome hint ─────────────────────────────────────────────

@Composable
private fun WelcomeHint() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🤖", fontSize = 56.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            "Привет! Я твой репетитор испанского.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Пиши мне на испанском или русском.\nЯ отвечу на испанском и исправлю ошибки.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        // Quick start prompts
        val prompts = listOf(
            "Hola, ¿cómo estás?",
            "Quiero practicar español",
            "Объясни разницу ser и estar"
        )
        prompts.forEach { prompt ->
            SuggestionChip(
                onClick = {},
                label = { Text(prompt, style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}
