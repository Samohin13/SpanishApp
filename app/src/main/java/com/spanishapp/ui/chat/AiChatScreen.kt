package com.spanishapp.ui.chat

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
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
import com.spanishapp.R
import com.spanishapp.ui.theme.AppColors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject

/**
 * Распарсенная коррекция ошибки из CORRECTIONS_JSON:[{...}].
 * AiChatRepository вырезает блок из текста и сохраняет JSON в correctionJson поле.
 */
data class ChatCorrection(
    val original: String,
    val corrected: String,
    val explanation: String
)

private fun parseCorrections(json: String): List<ChatCorrection> {
    if (json.isBlank()) return emptyList()
    return runCatching {
        val arr = JSONArray(json)
        (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            ChatCorrection(
                original    = obj.optString("original"),
                corrected   = obj.optString("corrected"),
                explanation = obj.optString("explanation")
            )
        }.filter { it.original.isNotBlank() && it.corrected.isNotBlank() }
    }.getOrDefault(emptyList())
}

// ── ViewModel ─────────────────────────────────────────────────

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val repo: AiChatRepository,
    private val tts: SpanishTts,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val sessionId = "default"

    val messages: StateFlow<List<ChatMessageEntity>> = repo.getMessages(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    /**
     * In-flight assistant text being streamed token-by-token.
     * Empty when no streaming is happening or when the final message has been
     * persisted (then it appears in [messages]).
     */
    private val _streamingText = MutableStateFlow("")
    val streamingText: StateFlow<String> = _streamingText.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun send(text: String) {
        if (text.isBlank() || _isSending.value) return
        _isSending.value = true
        _streamingText.value = ""
        _error.value = null
        viewModelScope.launch {
            try {
                repo.streamMessage(text.trim(), sessionId).collect { progressive ->
                    // Strip the trailing CORRECTIONS_JSON marker for nicer display
                    // while streaming (it'll be parsed cleanly when persisted).
                    val display = progressive.substringBefore("CORRECTIONS_JSON:")
                    _streamingText.value = display
                }
            } catch (e: Exception) {
                _error.value = when {
                    e.message?.contains("401") == true -> appContext.getString(R.string.chat_error_invalid_key)
                    e.message?.contains("429") == true -> appContext.getString(R.string.chat_error_rate_limit)
                    e.message?.contains("network", ignoreCase = true) == true ||
                    e.message?.contains("timeout", ignoreCase = true) == true -> appContext.getString(R.string.chat_error_network)
                    else -> appContext.getString(R.string.chat_error_generic, e.message ?: "")
                }
            } finally {
                _streamingText.value = ""
                _isSending.value = false
            }
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
    val messages       by vm.messages.collectAsState()
    val isSending      by vm.isSending.collectAsState()
    val streamingText  by vm.streamingText.collectAsState()
    val error          by vm.error.collectAsState()
    var input     by remember { mutableStateOf("") }
    val haptic    = com.spanishapp.ui.components.rememberCheckedHaptic()

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    // Keep the streaming bubble in view as text grows.
    LaunchedEffect(streamingText) {
        if (streamingText.isNotEmpty()) {
            // Scroll to last index (streaming bubble is appended after messages).
            listState.animateScrollToItem(messages.size)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✨", fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(androidx.compose.ui.res.stringResource(com.spanishapp.R.string.chat_assistant_name), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(androidx.compose.ui.res.stringResource(com.spanishapp.R.string.chat_status_online), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        vm.newChat() 
                    }) {
                        Icon(Icons.Default.AddComment, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Box(modifier = Modifier.weight(1f)) {
                if (messages.isEmpty()) {
                    WelcomeHint(onSuggestion = { 
                        vm.send(it)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    })
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            // Each new message slides up + fades in. Existing
                            // messages reflow smoothly when a new one arrives.
                            ChatBubble(
                                message = msg,
                                onSpeak = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    vm.speak(msg.content)
                                },
                                modifier = Modifier.animateItem(
                                    fadeInSpec = tween(280),
                                    placementSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    ),
                                    fadeOutSpec = tween(180)
                                )
                            )
                        }
                        // Streaming preview: while Gemini is generating, show a
                        // ChatBubble with the partial text. Disappears when the
                        // final message lands in `messages`.
                        if (streamingText.isNotEmpty()) {
                            item(key = "streaming") {
                                ChatBubble(
                                    message = ChatMessageEntity(
                                        id = -1,
                                        role = "assistant",
                                        content = streamingText,
                                        sessionId = "default",
                                        correctionJson = ""
                                    ),
                                    onSpeak = { /* no-op while streaming */ },
                                    modifier = Modifier
                                )
                            }
                        } else if (isSending) {
                            item("typing") { TypingIndicator() }
                        }
                    }
                }
            }

            AnimatedVisibility(error != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(error ?: "", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        IconButton(onClick = vm::clearError) { Icon(Icons.Default.Close, stringResource(R.string.chat_close_cd)) }
                    }
                }
            }

            // Animated AI-style gradient border (yellow → orange → red, looping).
            // Using infiniteTransition phase shift to animate the gradient angle.
            val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "ai_border")
            val phase by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                    animation = androidx.compose.animation.core.tween(2400, easing = androidx.compose.animation.core.LinearEasing),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Restart
                ),
                label = "ai_border_phase"
            )
            val aiColors = listOf(
                Color(0xFFFFD600), // yellow
                Color(0xFFFF9800), // orange
                Color(0xFFFF3D00), // red
                Color(0xFFFF9800), // orange
                Color(0xFFFFD600)  // yellow (loop seamlessly)
            )

            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Animated gradient border around the text field.
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .drawBehind {
                                val borderWidth = 1.5.dp.toPx()
                                val cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx())
                                val brush = Brush.linearGradient(
                                    colors = aiColors,
                                    start = androidx.compose.ui.geometry.Offset(
                                        x = size.width * (phase - 0.5f),
                                        y = 0f
                                    ),
                                    end = androidx.compose.ui.geometry.Offset(
                                        x = size.width * (phase + 0.5f),
                                        y = size.height
                                    )
                                )
                                drawRoundRect(
                                    brush = brush,
                                    cornerRadius = cornerRadius,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = borderWidth)
                                )
                            }
                    ) {
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            placeholder = {
                                Text(
                                    androidx.compose.ui.res.stringResource(com.spanishapp.R.string.chat_message_placeholder),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 5,
                            enabled = !isSending,
                            colors = OutlinedTextFieldDefaults.colors(
                                // Hide the default OutlinedTextField border — we draw our own animated one.
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                disabledBorderColor = Color.Transparent,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    // Send button with the same animated gradient ring (when idle).
                    val sendActive = input.isNotBlank()
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .drawBehind {
                                val borderWidth = 1.5.dp.toPx()
                                val brush = Brush.linearGradient(
                                    colors = aiColors,
                                    start = androidx.compose.ui.geometry.Offset(
                                        x = size.width * (phase - 0.5f),
                                        y = 0f
                                    ),
                                    end = androidx.compose.ui.geometry.Offset(
                                        x = size.width * (phase + 0.5f),
                                        y = size.height
                                    )
                                )
                                drawCircle(
                                    brush = brush,
                                    radius = (size.minDimension - borderWidth) / 2f,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = borderWidth)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        FloatingActionButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                vm.send(input)
                                input = ""
                            },
                            containerColor = if (sendActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = if (sendActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                        ) {
                            if (isSending) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessageEntity,
    onSpeak: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val corrections = remember(message.correctionJson) { parseCorrections(message.correctionJson) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp, topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 20.dp
            ),
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = if (isUser) 0.dp else 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(
                    message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (!isUser) {
            IconButton(onClick = onSpeak, modifier = Modifier.padding(top = 2.dp).size(32.dp)) {
                Icon(Icons.Default.VolumeUp, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
        }

        // Карточки исправлений — показываем под bubble юзера, потому что
        // они относятся к ЕГО предыдущему сообщению (AI исправляет последнее
        // user-сообщение). Карточки прикреплены к ответу AI в БД, но
        // визуально удобнее, когда они сразу под сообщением AI.
        if (!isUser && corrections.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            corrections.forEach { c ->
                CorrectionCard(c)
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun CorrectionCard(correction: ChatCorrection) {
    val accent = Color(0xFFFF8C00)  // янтарный — как marker исправлений

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = accent.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.EditNote,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    androidx.compose.ui.res.stringResource(com.spanishapp.R.string.chat_correction_label),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accent
                )
            }
            Spacer(Modifier.height(6.dp))

            // Original (зачёркнуто)
            Text(
                correction.original,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
            )

            // Corrected (жирно, цветом акцента)
            Text(
                correction.corrected,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )

            if (correction.explanation.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    correction.explanation,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    Surface(
        shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(3) {
                val alpha by rememberInfiniteTransition(label = "").animateFloat(
                    initialValue = 0.3f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(600, delayMillis = it * 200), RepeatMode.Reverse),
                    label = ""
                )
                Box(Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = alpha)))
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun WelcomeHint(onSuggestion: (String) -> Unit) {
    val scrollState = androidx.compose.foundation.rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Icon + greeting ─────────────────────────────────────
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF6B35)),  // brand orange
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                painter = androidx.compose.ui.res.painterResource(com.spanishapp.R.drawable.ic_bull),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "¡Hola! Soy tu tutor personal.",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            androidx.compose.ui.res.stringResource(com.spanishapp.R.string.chat_welcome_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        // ── Capability cards ────────────────────────────────────
        val capabilities = listOf(
            Triple("💬", stringResource(com.spanishapp.R.string.chat_cap_answer_title),
                       stringResource(com.spanishapp.R.string.chat_cap_answer_desc)),
            Triple("✏️", stringResource(com.spanishapp.R.string.chat_cap_correct_title),
                       stringResource(com.spanishapp.R.string.chat_cap_correct_desc)),
            Triple("🔊", stringResource(com.spanishapp.R.string.chat_cap_speak_title),
                       stringResource(com.spanishapp.R.string.chat_cap_speak_desc)),
            Triple("🎯", stringResource(com.spanishapp.R.string.chat_cap_adapt_title),
                       stringResource(com.spanishapp.R.string.chat_cap_adapt_desc)),
            Triple("⚡", stringResource(com.spanishapp.R.string.chat_cap_stream_title),
                       stringResource(com.spanishapp.R.string.chat_cap_stream_desc))
        )
        capabilities.forEach { (emoji, title, desc) ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(emoji, fontSize = 22.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Try-it suggestions ──────────────────────────────────
        Text(
            stringResource(com.spanishapp.R.string.chat_try_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Spacer(Modifier.height(8.dp))
        val prompts = listOf(
            "Hola, soy nuevo en español",
            "¿Cómo se dice 'погода'?",
            "Tengo 25 anos y vivo en Moscu",
            "Объясни разницу между ser и estar",
            "Practiquemos el pretérito"
        )
        androidx.compose.foundation.layout.FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            prompts.forEach { prompt ->
                SuggestionChip(
                    onClick = { onSuggestion(prompt) },
                    label = { Text(prompt, fontSize = 13.sp) },
                    modifier = Modifier.padding(vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

