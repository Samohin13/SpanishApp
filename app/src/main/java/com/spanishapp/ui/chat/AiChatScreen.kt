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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.spanishapp.data.db.entity.ChatMessageEntity
import com.spanishapp.data.repository.AiChatRepository
import com.spanishapp.service.SpanishTts
import com.spanishapp.R
import com.spanishapp.ui.adaptive.adaptiveContentWidth
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
    private val stt: com.spanishapp.service.SpanishSpeechRecognizer,
    private val limiter: com.spanishapp.service.AiChatLimiter,
    private val remoteTts: com.spanishapp.service.RemoteTtsService,
    private val authRepository: com.spanishapp.data.repository.AuthRepository,
    @ApplicationContext private val appContext: Context,
    savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {

    /** Сколько сообщений осталось до дневного лимита (50/день). */
    val remainingMessages: StateFlow<Int> = limiter.remainingToday
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.spanishapp.service.AiChatLimiter.DAILY_LIMIT)

    /** v1.18.38: выбранный фон чата (ChatWallpapers.id). */
    val wallpaperId: StateFlow<String> = authRepository.chatWallpaper
        .map { it ?: com.spanishapp.domain.chat.ChatWallpapers.DEFAULT_ID }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.spanishapp.domain.chat.ChatWallpapers.DEFAULT_ID)

    fun setWallpaper(id: String) = viewModelScope.launch {
        authRepository.setChatWallpaper(id)
    }

    /** Read from nav arg `sessionId={...}`. Defaults to free chat. */
    val sessionId: String = savedStateHandle.get<String>("sessionId") ?: "default"
    val theme: ChatSessionTheme = ChatSessions.byId(sessionId) ?: ChatSessions.all.first()

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
            // Дневной лимит — 50 сообщений / день. Защита и для юзера
            // (от случайного перерасхода) и для общего ключа Gemini
            // (от выжирания дневной квоты одним юзером).
            if (limiter.isExhausted()) {
                _error.value = "Достигнут дневной лимит ${com.spanishapp.service.AiChatLimiter.DAILY_LIMIT} сообщений. " +
                        "Лимит обновится завтра."
                _isSending.value = false
                return@launch
            }
            // v1.18.5: race condition fix v2. Раньше (v1.18.1) был один loop
            // в finally который держал _isSending=true ДО 1 сек ожидая Room
            // flow. Это блокировало кнопку отправки + индикатор «загрузка»
            // висел лишнюю секунду → юзер ощущал что ИИ медленный.
            //
            // Теперь:
            //  - Сразу после collect() освобождаем UI (isSending=false).
            //    Юзер может отправлять новое сообщение моментально.
            //  - Короткий 200ms guard ТОЛЬКО на streamingText — защита от
            //    100-300ms flicker когда Room ещё не эмитнул финальный INSERT.
            //  - В catch — всё чистим сразу, никакого ожидания.
            val messageCountBefore = messages.value.size
            try {
                repo.streamMessage(text.trim(), sessionId, theme.systemPromptExtra).collect { progressive ->
                    // Strip the trailing CORRECTIONS_JSON marker for nicer display.
                    val display = progressive
                        .substringBefore("CORRECTIONS_JSON:")
                        .substringBefore("PROFILE_UPDATE_JSON:")
                    _streamingText.value = display
                }
                limiter.increment()
                // Освобождаем UI сразу — юзер может отправлять следующее.
                _isSending.value = false
                // Короткий guard от flicker (макс 200ms).
                val deadline = System.currentTimeMillis() + 200L
                while (
                    messages.value.size <= messageCountBefore + 1 &&
                    System.currentTimeMillis() < deadline
                ) {
                    kotlinx.coroutines.delay(20)
                }
                _streamingText.value = ""
            } catch (e: Exception) {
                // v1.18.6: на 429 (rate limit) пробуем один авто-retry через 6 сек
                // — обычно достаточно для сброса квоты Gemini Flash (15 RPM).
                val is429 = e.message?.contains("429") == true
                if (is429) {
                    _streamingText.value = ""
                    kotlinx.coroutines.delay(6000)
                    try {
                        repo.streamMessage(text.trim(), sessionId, theme.systemPromptExtra).collect { progressive ->
                            val display = progressive
                        .substringBefore("CORRECTIONS_JSON:")
                        .substringBefore("PROFILE_UPDATE_JSON:")
                            _streamingText.value = display
                        }
                        limiter.increment()
                        _isSending.value = false
                        kotlinx.coroutines.delay(200)
                        _streamingText.value = ""
                        return@launch
                    } catch (e2: Exception) {
                        // Retry тоже упал — показываем ошибку
                        _error.value = if (e2.message?.contains("429") == true)
                            appContext.getString(R.string.chat_error_rate_limit)
                        else
                            appContext.getString(R.string.chat_error_generic, e2.message ?: "")
                    }
                } else {
                    _error.value = when {
                        e.message?.contains("401") == true -> appContext.getString(R.string.chat_error_invalid_key)
                        e.message?.contains("network", ignoreCase = true) == true ||
                        e.message?.contains("timeout", ignoreCase = true) == true -> appContext.getString(R.string.chat_error_network)
                        else -> appContext.getString(R.string.chat_error_generic, e.message ?: "")
                    }
                }
                _streamingText.value = ""
                _isSending.value = false
            }
        }
    }

    fun clearError() { _error.value = null }

    fun newChat() = viewModelScope.launch { repo.clearSession(sessionId) }

    val isListening: StateFlow<Boolean> = stt.isListening
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /**
     * Records a single Spanish utterance via [SpanishSpeechRecognizer].
     * On success, emits the recognized text via [onResult] so the caller
     * can drop it into the input field. Errors are surfaced through [error].
     */
    fun startVoice(onResult: (String) -> Unit) {
        if (isListening.value) return
        viewModelScope.launch {
            // Default chat (general tutor) → user types in Russian, so dictate
            // in ru-RU. Roleplay scenarios (waiter / hotel / doctor) expect the
            // user to PRACTICE Spanish, so switch the recognizer to es-ES on
            // those — otherwise the engine would mis-transcribe Spanish words
            // via Russian phonetics.
            val lang = if (theme.id == "default") "ru-RU" else "es-ES"
            when (val r = stt.listenOnce(language = lang)) {
                is com.spanishapp.service.SpeechResult.Success -> onResult(r.text)
                is com.spanishapp.service.SpeechResult.Error -> {
                    if (!r.isSilence) _error.value = r.message
                }
                is com.spanishapp.service.SpeechResult.Cancelled -> { /* user cancelled */ }
            }
        }
    }

    /**
     * v1.18.18: premium TTS с сегментацией ru/es и toggle-stop.
     * Если уже играет — повторный тап останавливает (toggle).
     * Иначе чистит текст и запускает воспроизведение.
     */
    val isSpeaking: StateFlow<Boolean> = remoteTts.isPlaying

    fun speak(text: String) {
        // Toggle: если уже говорит — стоп
        if (remoteTts.isPlaying.value) {
            remoteTts.stop()
            tts.stop()
            return
        }
        val cleaned = sanitizeForSpeech(text)
        if (cleaned.isBlank()) return
        val ok = remoteTts.speak(cleaned)
        if (!ok) {
            // Fallback на системный TTS если remote недоступен
            viewModelScope.launch { tts.speak(text, fullMixed = true) }
        }
    }

    private fun sanitizeForSpeech(raw: String): String {
        var s = raw
        // Markdown **bold** → текст без звёздочек
        s = s.replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1")
        // [перевод] — удаляем (юзер уже видит в UI)
        s = s.replace(Regex("\\[[^\\]]+\\]"), "")
        // Эмодзи и misc symbols
        s = s.replace(Regex("[\\p{So}\\p{Sk}]"), "")
        // Surrogates (multi-codepoint emoji)
        s = s.replace(Regex("[\\uD800-\\uDFFF]"), "")
        // Хвосты системных markers
        s = s.substringBefore("CORRECTIONS_JSON:").substringBefore("PROFILE_UPDATE_JSON:")
        // Сжать whitespace
        s = s.replace(Regex("\\s+"), " ").trim()
        return s
    }

    /** v1.18.4: real-time amplitude от mic для waveform visualizer. */
    val voiceAmplitude: StateFlow<Float> = stt.rmsDb
}

// ── Screen ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    navController: NavHostController,
    vm: AiChatViewModel = hiltViewModel()
) {
    val messages       by vm.messages.collectAsStateWithLifecycle()
    val isSending      by vm.isSending.collectAsStateWithLifecycle()
    val streamingText  by vm.streamingText.collectAsStateWithLifecycle()
    val error          by vm.error.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }
    var showWallpaperPicker by remember { mutableStateOf(false) }
    val wallpaperId by vm.wallpaperId.collectAsStateWithLifecycle()
    val wallpaper = com.spanishapp.domain.chat.ChatWallpapers.byId(wallpaperId)
    val isListening by vm.isListening.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var input     by remember { mutableStateOf("") }
    val haptic    = com.spanishapp.ui.components.rememberCheckedHaptic()
    val voiceAmplitude by vm.voiceAmplitude.collectAsStateWithLifecycle()

    // RECORD_AUDIO permission flow for the mic button.
    val micPermLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) vm.startVoice { recognized -> input = recognized }
    }
    fun launchVoiceInput() {
        val granted = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (granted) {
            vm.startVoice { recognized -> input = recognized }
        } else {
            micPermLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    val listState = rememberLazyListState()

    // Single coalesced auto-scroll. Two separate LaunchedEffects (one on
    // messages.size, one on streamingText) were racing each other on every
    // streamed chunk, producing visible jitter. One effect keyed on both
    // signals lets the scheduler dedupe properly.
    LaunchedEffect(messages.size, streamingText.length) {
        val target = if (streamingText.isNotEmpty()) messages.size
                     else (messages.size - 1).coerceAtLeast(0)
        if (messages.isNotEmpty() || streamingText.isNotEmpty()) {
            listState.animateScrollToItem(target)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
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
                            // Theme emoji (e.g., ✈️ for travel) instead of generic ✨.
                            Text(vm.theme.emoji, fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                vm.theme.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            // Индикатор оставшихся сообщений за день (50/день).
                            // Заменил «Online» — это полезнее: юзер видит лимит
                            // ещё до того как упрётся в него.
                            val remaining by vm.remainingMessages.collectAsStateWithLifecycle()
                            val limitColor = when {
                                remaining > 20 -> MaterialTheme.colorScheme.primary
                                remaining > 5  -> Color(0xFFFFA000)   // янтарь
                                else           -> Color(0xFFE53935)   // красный
                            }
                            Text(
                                "Осталось $remaining/${com.spanishapp.service.AiChatLimiter.DAILY_LIMIT} сообщений сегодня",
                                style = MaterialTheme.typography.labelSmall,
                                color = limitColor,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showWallpaperPicker = true
                        }
                    ) {
                        Icon(
                            Icons.Default.Wallpaper,
                            contentDescription = "Сменить фон",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            // Only ask to confirm if there's actually a history to wipe.
                            if (messages.isEmpty()) vm.newChat() else showClearDialog = true
                        }
                    ) {
                        Icon(Icons.Default.AddComment, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    ) { padding ->
        // v1.18.36: bottom inset не консьюмим из Scaffold — input Surface
        // сам уходит под навбар (как в WhatsApp), а Row внутри respects
        // nav inset через navigationBarsPadding.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = padding.calculateTopPadding(),
                    start = padding.calculateStartPadding(androidx.compose.ui.platform.LocalLayoutDirection.current),
                    end = padding.calculateEndPadding(androidx.compose.ui.platform.LocalLayoutDirection.current),
                )
        ) {
            ChatWallpaperBackground(
                wallpaper = wallpaper,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            ) {
                if (messages.isEmpty()) {
                    WelcomeHint(onSuggestion = { 
                        vm.send(it)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    })
                } else {
                    // v1.15.0 P2: cap 720dp на планшете чтобы chat
                    // не растягивался на всю ширину 1280dp (читать
                    // длинные строки тяжело — readability rule).
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .adaptiveContentWidth()
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
                                onSpeakWord = { word ->
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    vm.speak(word)
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
                                    onSpeakWord = { word ->
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        vm.speak(word)
                                    },
                                    modifier = Modifier
                                )
                            }
                        } else if (isSending) {
                            item("typing") { TypingIndicator() }
                        }

                        // Quick-replies under the most recent assistant message
                        // (only when not currently streaming and last msg is from AI).
                        val lastIsAssistant = messages.lastOrNull()?.role == "assistant"
                        if (lastIsAssistant && !isSending && streamingText.isEmpty()) {
                            item("quick_replies") {
                                QuickReplies(
                                    onPick = { preset ->
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        vm.send(preset)
                                    }
                                )
                            }
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

            // v1.18.1: Telegram/WhatsApp-style input bar — без анимированных
            // обводок (раньше юзер видел постоянное мерцание yellow→orange→red).
            // Чистый surface фон + округлый pill-input + одна кнопка справа
            // (микрофон когда пусто, send когда есть текст).
            val sendActive = input.isNotBlank()
            // Pulse для активного recording — единственная анимация в баре.
            val micPulse by androidx.compose.animation.core.rememberInfiniteTransition(label = "mic_pulse").animateFloat(
                initialValue = 1f,
                targetValue = 1.18f,
                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                    animation = androidx.compose.animation.core.tween(550, easing = androidx.compose.animation.core.LinearEasing),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                ),
                label = "mic_pulse_anim"
            )

            // v1.18.41: возвращён компактный input-bar контейнер (как в WhatsApp).
            // Тонкая плашка-Surface под pill и кнопкой, минимальные отступы.
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // ── Pill-input: TextField или waveform когда listening ──
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shadowElevation = 2.dp,
                    ) {
                        if (isListening) {
                            VoiceWaveform(
                                amplitude = voiceAmplitude,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                            )
                        } else {
                            // v1.18.40: BasicTextField вместо OutlinedTextField — у того
                            // жёсткий min-height 56dp. Сейчас pill тонкий как в WhatsApp.
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 12.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                if (input.isEmpty()) {
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(com.spanishapp.R.string.chat_message_placeholder),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 15.sp,
                                    )
                                }
                                androidx.compose.foundation.text.BasicTextField(
                                    value = input,
                                    onValueChange = { input = it },
                                    enabled = !isSending,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp,
                                    ),
                                    cursorBrush = androidx.compose.ui.graphics.SolidColor(
                                        MaterialTheme.colorScheme.primary
                                    ),
                                    maxLines = 5,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }

                    // ── Кнопка действия: микрофон или send (как в Telegram) ──
                    val (action, icon, cd) = when {
                        sendActive -> Triple(
                            {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                vm.send(input)
                                input = ""
                            },
                            Icons.AutoMirrored.Filled.Send,
                            stringResource(com.spanishapp.R.string.chat_send_cd),
                        )
                        else -> Triple(
                            {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                launchVoiceInput()
                            },
                            Icons.Default.Mic,
                            stringResource(com.spanishapp.R.string.chat_voice_input_cd),
                        )
                    }
                    FloatingActionButton(
                        onClick = { if (!isSending && (sendActive || !isListening)) action() },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(44.dp)
                            .scale(if (isListening && !sendActive) micPulse else 1f),
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(2.dp, 4.dp),
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Icon(icon, contentDescription = cd, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    }

    if (showWallpaperPicker) {
        ChatWallpaperPickerSheet(
            currentId = wallpaperId,
            onPick = { id ->
                vm.setWallpaper(id)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            onDismiss = { showWallpaperPicker = false },
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(com.spanishapp.R.string.chat_clear_title)) },
            text  = { Text(stringResource(com.spanishapp.R.string.chat_clear_text)) },
            confirmButton = {
                TextButton(onClick = {
                    vm.newChat()
                    showClearDialog = false
                }) {
                    Text(
                        stringResource(com.spanishapp.R.string.chat_clear_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(com.spanishapp.R.string.btn_cancel))
                }
            }
        )
    }
}

/**
 * Lightweight markdown for chat: makes **bold** parts bold and
 * highlights [translations in brackets] with the brand color.
 * Strips the literal asterisks/brackets from the visible text.
 */
/** Tag attached to **bold** spans so we can look them up on tap and play TTS. */
private const val SPEAK_TAG = "speak"

private fun renderChatMarkdown(
    text: String,
    accentColor: Color,
    bracketColor: Color
): AnnotatedString =
    buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            val rest = text.substring(i)
            // **bold** — Spanish words/phrases. Mark with annotation for tap-to-speak.
            val boldMatch = Regex("""^\*\*(.+?)\*\*""").find(rest)
            if (boldMatch != null) {
                val word = boldMatch.groupValues[1]
                pushStringAnnotation(tag = SPEAK_TAG, annotation = word)
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    )
                ) {
                    append(word)
                }
                pop()
                i += boldMatch.value.length
                continue
            }
            // [translation] — Russian gloss in brackets, slightly muted.
            val brMatch = Regex("""^\[([^\[\]]+)\]""").find(rest)
            if (brMatch != null) {
                withStyle(SpanStyle(color = bracketColor, fontSize = 14.sp)) {
                    append("[${brMatch.groupValues[1]}]")
                }
                i += brMatch.value.length
                continue
            }
            append(text[i])
            i++
        }
    }

@Composable
private fun ChatBubble(
    message: ChatMessageEntity,
    onSpeak: () -> Unit,
    onSpeakWord: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val corrections = remember(message.correctionJson) { parseCorrections(message.correctionJson) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // AI messages get a bull avatar to the left so the conversation
        // looks like a chat with a character, not a wall of text.
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF6B35)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(com.spanishapp.R.drawable.ic_bull),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 22.dp, topEnd = 22.dp,
                    bottomStart = if (isUser) 22.dp else 6.dp,
                    bottomEnd = if (isUser) 6.dp else 22.dp
                ),
                color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                shadowElevation = if (isUser) 0.dp else 2.dp,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                    if (isUser) {
                        // v1.15.0 P1: MarkdownText вместо plain AnnotatedString.
                        // Юзер может писать **bold** в своих сообщениях — рендерим правильно.
                        com.spanishapp.ui.components.MarkdownText(
                            text = message.content,
                            fontSize = 16.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        // AI message — bold Spanish words are tappable for TTS.
                        val accent = MaterialTheme.colorScheme.primary
                        val onSurfaceMuted = MaterialTheme.colorScheme.onSurfaceVariant
                        val annotated = remember(message.content, accent, onSurfaceMuted) {
                            renderChatMarkdown(message.content, accent, onSurfaceMuted)
                        }
                        androidx.compose.foundation.text.ClickableText(
                            text = annotated,
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 16.sp,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            onClick = { offset ->
                                annotated.getStringAnnotations(SPEAK_TAG, offset, offset)
                                    .firstOrNull()?.let { ann -> onSpeakWord(ann.item) }
                            }
                        )
                    }
                }
            }
        }

        if (!isUser) {
            IconButton(
                onClick = onSpeak,
                modifier = Modifier.padding(top = 4.dp, start = 46.dp).size(32.dp)
            ) {
                Icon(Icons.Default.VolumeUp, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
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
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
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

/**
 * Quick-reply chips that appear under the most recent AI message.
 * Each preset is sent verbatim — Gemini interprets them in the current
 * conversation context, so "Объясни проще" automatically refers to the
 * preceding assistant message.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun QuickReplies(onPick: (String) -> Unit) {
    val presets = listOf(
        "💡 " to stringResource(com.spanishapp.R.string.chat_quick_simpler),
        "📝 " to stringResource(com.spanishapp.R.string.chat_quick_example),
        "🔍 " to stringResource(com.spanishapp.R.string.chat_quick_check),
        "🎯 " to stringResource(com.spanishapp.R.string.chat_quick_practice)
    )
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        presets.forEach { (emoji, text) ->
            SuggestionChip(
                onClick = { onPick(text) },
                label = { Text("$emoji$text", fontSize = 13.sp) },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════
//  Voice Waveform (v1.18.6 — Gemini Live / ChatGPT voice style)
// ════════════════════════════════════════════════════════════════

/**
 * Pulsing centered bars — индикатор «ИИ слушает».
 *
 * v1.18.4 был scroll-стиль (как Telegram playback) — bars бежали слева
 * направо. Юзер: «звуковая волна куда-то бежит». Заменил на pulsing
 * static bars как у Gemini Live / ChatGPT voice mode:
 *  - 5 широких столбиков, фиксированные позиции (по центру)
 *  - Высота КАЖДОГО независимо пульсирует на основе current amplitude
 *  - Smooth animation через animateFloat (не дёргаются)
 *  - Центральный bar больше реагирует, крайние мягче (envelope)
 *  - Brand primary, alpha по громкости
 */
@Composable
private fun VoiceWaveform(
    amplitude: Float,
    modifier: Modifier = Modifier,
) {
    val brand = MaterialTheme.colorScheme.primary
    // Нормализация rmsDb (-2..10) → 0..1
    val normalized = ((amplitude + 2f) / 12f).coerceIn(0f, 1f)

    // Envelope per bar — крайние bars менее реактивные чем центральные.
    // 5 столбиков: коэффициенты от center к краям.
    val envelopes = listOf(0.55f, 0.85f, 1.0f, 0.85f, 0.55f)

    // Smooth animation на каждый bar
    val animated = envelopes.map { env ->
        val target = (0.15f + normalized * env).coerceIn(0.15f, 1f)
        animateFloatAsState(
            targetValue = target,
            animationSpec = tween(
                durationMillis = 140,
                easing = androidx.compose.animation.core.FastOutSlowInEasing,
            ),
            label = "bar_height",
        ).value
    }

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val centerY = h / 2f
        val bars = envelopes.size
        val barWidth = 6.dp.toPx()
        val gap = 10.dp.toPx()
        val totalWidth = bars * barWidth + (bars - 1) * gap
        val startX = (w - totalWidth) / 2f
        val maxHeight = h * 0.78f

        for (i in 0 until bars) {
            val barHeight = animated[i] * maxHeight
            val x = startX + i * (barWidth + gap)
            drawRoundRect(
                color = brand,
                topLeft = androidx.compose.ui.geometry.Offset(x, centerY - barHeight / 2f),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f),
                alpha = (0.5f + normalized * 0.5f).coerceAtMost(1f),
            )
        }
    }
}

