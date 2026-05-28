package com.spanishapp.ui.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.spanishapp.domain.chat.ChatScenarios
import kotlinx.coroutines.launch

/* ============================================================
   ПАЛИТРА ESPEAK (точно из мокапа v4, соответствует Theme.kt)
   ============================================================ */
private object EspeakChat {
    val primary       = Color(0xFFFF6B35)  // AppColors.Purple
    val primary2      = Color(0xFFFF8B5C)  // AppColors.PurpleLight
    val primaryPale   = Color(0xFFFFF1E6)  // AppColors.PurplePale
    val primaryPill   = Color(0xFFFFDECF)
    val pink          = Color(0xFFD62867)  // AppColors.Pink
    val gold          = Color(0xFFFFB400)  // AppColors.Gold
    val goldPale      = Color(0xFFFFE7A6)
    val success       = Color(0xFF34C759)
    val error         = Color(0xFFFF3B30)
}

@OptIn(ExperimentalMaterial3Api::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Composable
fun AiChatScreen(
    navController: NavHostController,
    vm: AiChatViewModel = hiltViewModel(),
) {
    val messages       by vm.messages.collectAsStateWithLifecycle()
    val scenario       by vm.scenario.collectAsStateWithLifecycle()
    val isSending      by vm.isSending.collectAsStateWithLifecycle()
    val isListening    by vm.isListening.collectAsStateWithLifecycle()
    val voiceAmplitude by vm.voiceAmplitude.collectAsStateWithLifecycle()
    val error          by vm.error.collectAsStateWithLifecycle()
    val remaining      by vm.remainingMessages.collectAsStateWithLifecycle()
    val isPro          by vm.isPro.collectAsStateWithLifecycle()
    // v1.24.6: TextFieldValue для полноценной поддержки курсора/выделения.
    // BasicTextField(readOnly=true) НЕ вызывает системную клаву, но позволяет
    // tap-to-position cursor и selection-by-long-press — как в S26 Ultra.
    var inputValue by remember { mutableStateOf(androidx.compose.ui.text.input.TextFieldValue("")) }
    val input = inputValue.text

    // v1.24.17: приём scenario id из ChatArchiveScreen через savedStateHandle.
    // v1.24.18: если PRO-сценарий и free-юзер → paywall, не выбор.
    val currentBackStackEntry = navController.currentBackStackEntry
    LaunchedEffect(currentBackStackEntry, isPro) {
        val picked = currentBackStackEntry
            ?.savedStateHandle
            ?.get<String>("picked_scenario_id")
        if (picked != null) {
            val sc = ChatScenarios.byId(picked)
            if (sc.isPro && !isPro) {
                navController.navigate("paywall") { launchSingleTop = true }
            } else {
                vm.selectScenario(sc)
            }
            currentBackStackEntry.savedStateHandle.remove<String>("picked_scenario_id")
        }
    }

    // v1.24.11: RECORD_AUDIO permission flow для микрофона.
    // STT (SpeechRecognizer) падает с ERROR_INSUFFICIENT_PERMISSIONS без неё.
    val context = androidx.compose.ui.platform.LocalContext.current
    val onVoiceRecognized: (String) -> Unit = { recognized ->
        inputValue = androidx.compose.ui.text.input.TextFieldValue(
            text = recognized,
            selection = androidx.compose.ui.text.TextRange(recognized.length),
        )
    }
    // v1.24.16: язык STT определяется по ТЕКУЩЕЙ РАСКЛАДКЕ клавиатуры.
    // Lifted state — keyboard layout живёт в AiChatScreen, и SpanishKeyboard,
    // и mic используют одну и ту же переменную. RU клава → ru-RU, иначе es-ES.
    // Раньше детект шёл по содержимому input (пустое → es-ES всегда) — баг.
    var keyboardLayout by remember { mutableStateOf(KbLayout.ES) }

    // v1.25.0: mic FAB теперь = voice message recording (WhatsApp-style).
    // tap 1 → start recording → FAB меняется на send. tap 2 → stop+save.
    val voiceIsRecording by vm.voiceIsRecording.collectAsStateWithLifecycle()
    val voiceElapsedMs by vm.voiceElapsedMs.collectAsStateWithLifecycle()
    val voiceAmpRec by vm.voiceAmpRec.collectAsStateWithLifecycle()
    val micPermLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) vm.startVoiceRecord()
    }
    fun toggleMic() {
        if (voiceIsRecording) {
            vm.stopAndSendVoiceMessage()
            return
        }
        val granted = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (granted) vm.startVoiceRecord()
        else micPermLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
    }
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // Автоскролл к последнему сообщению
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    // Snackbar для ошибок
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let {
            snackbar.showSnackbar(it)
            vm.dismissError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ChatHeader(
                scenarioEmoji = scenario.emoji,
                scenarioTitle = scenario.title,
                limit = if (isPro) null else "$remaining/${com.spanishapp.service.AiChatLimiter.DAILY_LIMIT}",
                onBack = { navController.popBackStack() },
                onArchive = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    navController.navigate("chat_archive")
                },
                onNewChat = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    vm.clearCurrentSession()
                    inputValue = androidx.compose.ui.text.input.TextFieldValue("")
                },
            )
        },
        bottomBar = {
            Column {
                QuickChipsRow(onChipPrompt = { prompt ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    vm.send(prompt)
                })
                ChatComposer(
                    inputValue = inputValue,
                    onValueChange = { inputValue = it },
                    onSend = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        vm.send(input)
                        inputValue = androidx.compose.ui.text.input.TextFieldValue("")
                    },
                    onMic = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        toggleMic()
                    },
                    onCancelVoice = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        vm.cancelVoiceRecord()
                    },
                    isRecordingVoice = voiceIsRecording,
                    voiceRecordingMs = voiceElapsedMs,
                    voiceRecordingAmp = voiceAmpRec,
                    isListening = isListening,
                    voiceAmplitude = voiceAmplitude,
                )
                // v1.24.6: pro-уровень клавиатура с курсором, swipe-space, suggestions
                // v1.24.19: подсказки = user-learned (приоритет) + static dictionary.
                // Юзер набирает "ho" → если он уже отправлял "hola" много раз —
                // оно появится первым; иначе fallback на топ-частотные слова.
                val userFreqSnapshot by vm.userWordFrequency.freq.collectAsStateWithLifecycle()
                val suggestions = remember(input, userFreqSnapshot) {
                    val lastWord = input
                        .substringAfterLast(' ', missingDelimiterValue = input)
                        .substringAfterLast('\n', missingDelimiterValue = "")
                        .ifBlank { input.substringAfterLast(' ', missingDelimiterValue = input) }
                    val userSuggestions = vm.userWordFrequency.suggest(lastWord, 3)
                    val staticSuggestions = WordSuggester.suggest(input, 3)
                    (userSuggestions + staticSuggestions).distinct().take(3)
                }
                SpanishKeyboard(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    layout = keyboardLayout,
                    onLayoutChange = { keyboardLayout = it },
                    // v1.24.20: glide-typing — словарь = static + user-learned
                    glideDictionary = remember(userFreqSnapshot) {
                        (userFreqSnapshot.keys + WordSuggester.allWords()).distinct()
                    },
                    userWordFreq = userFreqSnapshot,
                    onSend = {
                        if (input.isNotBlank()) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            vm.send(input)
                            inputValue = androidx.compose.ui.text.input.TextFieldValue("")
                        }
                    },
                    canSend = input.isNotBlank() && !isSending,
                    suggestions = suggestions,
                    onPickSuggestion = { sug ->
                        val newText = WordSuggester.replaceLastWord(input, sug)
                        inputValue = androidx.compose.ui.text.input.TextFieldValue(
                            text = newText,
                            selection = androidx.compose.ui.text.TextRange(newText.length),
                        )
                    },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScenarioStrip(
                selectedId = scenario.id,
                isPro = isPro,
                onSelect = { vm.selectScenario(it) },
                onPaywall = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    navController.navigate("paywall") { launchSingleTop = true }
                },
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (messages.isEmpty()) {
                    item { WelcomeBubble(onSpeak = { vm.speak(it) }) }
                } else {
                    items(messages, key = { it.id }) { msg ->
                        ChatMessageItem(
                            role = msg.role,
                            content = msg.content,
                            correctionJson = msg.correctionJson,
                            onSpeak = { vm.speak(it) },
                            onCorrectionParse = { vm.corrections(it) },
                            audioPath = msg.audioPath,
                            audioDurationMs = msg.audioDurationMs,
                            voicePlayer = vm.voicePlayer,
                            onToggleVoicePlay = { vm.toggleVoicePlay(it) },
                        )
                    }
                }
                if (isSending) {
                    item { TypingIndicator() }
                }
            }
        }
    }

}

/* ============================================================
   HEADER — чистый Material3-стиль в фирменных цветах ESPEAK
   v1.24.5: аватарка emoji из TutorProfile, имя кастомизируется,
   стиль приведён к общему M3 без розовых градиентов.
   ============================================================ */
@Composable
private fun ChatHeader(
    scenarioEmoji: String,
    scenarioTitle: String,
    limit: String?,    // null = PRO, скрыть счётчик
    onBack: () -> Unit,
    onArchive: () -> Unit,
    onNewChat: () -> Unit,
) {
    Surface(
        modifier = Modifier.statusBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.5.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 8.dp, top = 6.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AppLogoAvatar(size = 40.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "ESPEAK",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "$scenarioEmoji $scenarioTitle",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                        )
                        if (limit != null) {
                            Spacer(Modifier.width(7.dp))
                            Box(
                                Modifier
                                    .width(0.5.dp)
                                    .height(11.dp)
                                    .background(MaterialTheme.colorScheme.outline)
                            )
                            Spacer(Modifier.width(7.dp))
                            Text(
                                limit,
                                fontSize = 11.5.sp,
                                color = EspeakChat.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            IconButton(onClick = onArchive) {
                Icon(
                    Icons.Default.History,
                    contentDescription = "Архив чатов",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onNewChat) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Новый чат",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AppLogoAvatar(size: androidx.compose.ui.unit.Dp) {
    // Лого приложения как аватарка в чате. Используем ic_splash_logo —
    // это фирменная иконка ESPEAK (mark из splash screen).
    Box(modifier = Modifier.size(size)) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(com.spanishapp.R.drawable.ic_splash_logo),
            contentDescription = "ESPEAK",
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
        )
        // Зелёная точка online
        Box(
            modifier = Modifier
                .size(11.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(EspeakChat.success)
            )
        }
    }
}

@Composable
private fun LevelPill(level: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(EspeakChat.gold)
            .padding(horizontal = 6.dp, vertical = 1.dp),
    ) {
        Text(
            level,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1A1100),
        )
    }
}

/* ============================================================
   SCENARIO STRIP — горизонтальный список сценариев
   ============================================================ */
@Composable
private fun ScenarioStrip(
    selectedId: String,
    isPro: Boolean,
    onSelect: (com.spanishapp.domain.chat.ChatScenario) -> Unit,
    onPaywall: () -> Unit,
) {
    // v1.24.18:
    //  • Auto-scroll к активному сценарию — раньше выбранный chip уезжал
    //    за экран если был не в начале списка
    //  • PRO scenarios: тап для free-юзера → paywall, не выбор
    //  • locked для free → soft alpha + lock иконка вместо 💎
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(selectedId) {
        val idx = ChatScenarios.all.indexOfFirst { it.id == selectedId }
        if (idx >= 0) listState.animateScrollToItem(idx)
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
    ) {
        items(ChatScenarios.all, key = { it.id }) { sc ->
            val isActive = sc.id == selectedId
            val locked = sc.isPro && !isPro
            val bg = when {
                isActive -> EspeakChat.primary
                locked -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                else -> MaterialTheme.colorScheme.surfaceContainerHighest
            }
            val fg = when {
                isActive -> Color.White
                locked -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                else -> MaterialTheme.colorScheme.onSurface
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bg)
                    .clickable {
                        if (locked) onPaywall()
                        else onSelect(sc)
                    }
                    .padding(start = 12.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(sc.emoji, fontSize = 15.sp)
                    Text(
                        sc.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = fg,
                    )
                    if (sc.isPro) {
                        Text(
                            if (locked) "🔒" else "💎",
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }
    }
}

/* ============================================================
   MESSAGE BUBBLE — AI слева с аватаром, USER справа градиент
   ============================================================ */
@Composable
private fun ChatMessageItem(
    role: String,
    content: String,
    correctionJson: String,
    onSpeak: (String) -> Unit,
    onCorrectionParse: (String) -> List<com.spanishapp.data.repository.ChatCorrection>,
    // v1.25.0: voice messages
    audioPath: String? = null,
    audioDurationMs: Long = 0L,
    voicePlayer: com.spanishapp.service.VoicePlayer? = null,
    onToggleVoicePlay: (String) -> Unit = {},
) {
    val isUser = role == "user"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!isUser) {
            AppLogoAvatar(size = 28.dp)
            Spacer(Modifier.width(6.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 290.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = if (isUser) 20.dp else 5.dp,
                    bottomEnd = if (isUser) 5.dp else 20.dp,
                ),
                color = Color.Transparent,
                modifier = Modifier.background(
                    brush = if (isUser)
                        Brush.linearGradient(listOf(EspeakChat.primary, EspeakChat.primary2))
                    else SolidColor(MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 5.dp,
                        bottomEnd = if (isUser) 5.dp else 20.dp,
                    ),
                ),
                shadowElevation = if (isUser) 0.dp else 1.dp,
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    // v1.25.0: голосовое сообщение → audio player вместо текста
                    if (!audioPath.isNullOrBlank() && voicePlayer != null) {
                        VoiceMessagePlayer(
                            audioPath = audioPath,
                            totalMs = audioDurationMs,
                            voicePlayer = voicePlayer,
                            onToggle = onToggleVoicePlay,
                            tintForUser = isUser,
                        )
                    } else {
                        // Парсим текст: ⟦RU⟧ отделяет испанскую и русскую части
                        val (esPart, ruPart) = splitBilingual(content)
                        BilingualText(
                            es = esPart,
                            ru = ruPart,
                            isUser = isUser,
                            onWordTap = { word -> onSpeak(word) },
                        )
                    }

                    if (!isUser && correctionJson.isNotBlank()) {
                        val corrections = onCorrectionParse(correctionJson)
                        if (corrections.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            CorrectionBlock(corrections)
                        }
                    }
                }
            }

            // Действия под bubble (только для AI)
            if (!isUser) {
                MessageActions(onSpeak = { onSpeak(content) })
            }
        }

        if (isUser) {
            Spacer(Modifier.width(6.dp))
            Box(Modifier.width(28.dp))
        }
    }
}

/* ============================================================
   VOICE MESSAGE PLAYER — play/pause + progress bar + duration
   ============================================================ */
@Composable
private fun VoiceMessagePlayer(
    audioPath: String,
    totalMs: Long,
    voicePlayer: com.spanishapp.service.VoicePlayer,
    onToggle: (String) -> Unit,
    tintForUser: Boolean,
) {
    val currentPath by voicePlayer.currentPath.collectAsStateWithLifecycle()
    val isPlaying by voicePlayer.isPlaying.collectAsStateWithLifecycle()
    val positionMs by voicePlayer.positionMs.collectAsStateWithLifecycle()
    val isThis = currentPath == audioPath

    val progress = if (isThis && totalMs > 0)
        (positionMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
    else 0f

    val fg = if (tintForUser) Color.White else MaterialTheme.colorScheme.onSurface
    val accent = if (tintForUser) Color.White else EspeakChat.primary
    val trackBg = if (tintForUser) Color.White.copy(alpha = 0.3f)
                  else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

    Row(
        modifier = Modifier.widthIn(min = 180.dp).padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Play/pause button
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.18f))
                .clickable { onToggle(audioPath) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (isThis && isPlaying) Icons.Default.Pause
                else Icons.Default.PlayArrow,
                contentDescription = if (isThis && isPlaying) "Пауза" else "Воспроизвести",
                tint = accent,
                modifier = Modifier.size(22.dp),
            )
        }
        // Progress bar + duration
        Column(modifier = Modifier.weight(1f)) {
            // Тонкий progress
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(trackBg),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accent),
                )
            }
            Spacer(Modifier.height(6.dp))
            val shownMs = if (isThis) positionMs else totalMs
            val mm = shownMs / 60000
            val ss = ((shownMs / 1000) % 60).toInt()
            Text(
                "%d:%02d".format(mm, ss),
                fontSize = 11.sp,
                color = fg.copy(alpha = 0.85f),
            )
        }
    }
}

private fun splitBilingual(text: String): Pair<String, String> {
    val idx = text.indexOf("⟦RU⟧")
    return if (idx == -1) text.trim() to ""
    else text.substring(0, idx).trim() to text.substring(idx + 4).trim()
}

@Composable
private fun BilingualText(
    es: String,
    ru: String,
    isUser: Boolean,
    onWordTap: (String) -> Unit,
) {
    val esColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
    val ruColor = if (isUser) Color.White.copy(alpha = 0.85f)
                  else MaterialTheme.colorScheme.onSurfaceVariant

    BoldClickableText(
        text = es,
        baseColor = esColor,
        boldColor = if (isUser) EspeakChat.goldPale else EspeakChat.primary,
        fontSize = 14.5.sp,
        onClick = onWordTap,
    )

    if (ru.isNotBlank()) {
        Spacer(Modifier.height(7.dp))
        HorizontalDivider(
            thickness = 0.5.dp,
            color = if (isUser) Color.White.copy(alpha = 0.28f)
                    else MaterialTheme.colorScheme.outlineVariant,
        )
        Spacer(Modifier.height(7.dp))
        Text(
            ru,
            fontSize = 12.5.sp,
            color = ruColor,
            lineHeight = 18.sp,
        )
    }
}

/**
 * Простой парсер **bold** → кликабельные слова с TTS.
 * Если нужен tap-to-translate popup — это слой выше через showBottomSheet.
 */
@Composable
private fun BoldClickableText(
    text: String,
    baseColor: Color,
    boldColor: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    onClick: (String) -> Unit,
) {
    val annotated = androidx.compose.ui.text.buildAnnotatedString {
        val regex = Regex("""\*\*(.+?)\*\*""")
        var lastEnd = 0
        regex.findAll(text).forEach { m ->
            append(text.substring(lastEnd, m.range.first))
            val word = m.groupValues[1]
            pushStringAnnotation(tag = "WORD", annotation = word)
            withStyle(
                androidx.compose.ui.text.SpanStyle(
                    color = boldColor,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline,
                )
            ) { append(word) }
            pop()
            lastEnd = m.range.last + 1
        }
        if (lastEnd < text.length) append(text.substring(lastEnd))
    }

    androidx.compose.foundation.text.ClickableText(
        text = annotated,
        style = TextStyle(
            color = baseColor,
            fontSize = fontSize,
            lineHeight = 20.sp,
        ),
        onClick = { offset ->
            annotated.getStringAnnotations("WORD", offset, offset)
                .firstOrNull()?.let { onClick(it.item) }
        },
    )
}

/* ============================================================
   CORRECTION BLOCK — золотая плашка с разбором ошибок
   ============================================================ */
@Composable
private fun CorrectionBlock(corrections: List<com.spanishapp.data.repository.ChatCorrection>) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(13.dp))
            .background(EspeakChat.goldPale)
            .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
    ) {
        Row {
            // Левая золотая полоса (как в HTML)
            Box(
                Modifier
                    .padding(end = 9.dp)
                    .width(3.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(EspeakChat.gold)
            )
            Column {
                Text(
                    "МИНИ-ПРАВКА",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFC24A1A),
                    letterSpacing = 0.6.sp,
                )
                Spacer(Modifier.height(4.dp))
                corrections.forEach { c ->
                    Row(modifier = Modifier.padding(top = 3.dp)) {
                        Text("• ", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            c.original,
                            fontSize = 12.5.sp,
                            color = EspeakChat.error,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = TextDecoration.LineThrough,
                        )
                        Text(" → ", fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            c.corrected,
                            fontSize = 12.5.sp,
                            color = EspeakChat.success,
                            fontWeight = FontWeight.Bold,
                        )
                        if (c.explanation.isNotBlank()) {
                            Text(
                                " · ${c.explanation}",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ============================================================
   MESSAGE ACTIONS — кнопки под AI bubble (speak / save)
   ============================================================ */
@Composable
private fun MessageActions(onSpeak: () -> Unit) {
    Row(
        modifier = Modifier.padding(start = 4.dp, top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        SmallIconAction(icon = Icons.Default.VolumeUp, onClick = onSpeak, label = "Озвучить")
    }
}

@Composable
private fun SmallIconAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    label: String,
) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        contentPadding = PaddingValues(horizontal = 7.dp, vertical = 4.dp),
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(13.dp))
    }
}

/* ============================================================
   TYPING INDICATOR — три пляшущих точки в AI-bubble
   ============================================================ */
@Composable
private fun TypingIndicator() {
    Row(verticalAlignment = Alignment.Bottom) {
        AppLogoAvatar(size = 28.dp)
        Spacer(Modifier.width(6.dp))
        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp, topEnd = 20.dp,
                bottomStart = 5.dp, bottomEnd = 20.dp,
            ),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                repeat(3) { i ->
                    val transition = rememberInfiniteTransition(label = "dot$i")
                    val scale by transition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse,
                            initialStartOffset = StartOffset(i * 150),
                        ),
                        label = "scale$i",
                    )
                    Box(
                        Modifier
                            .size(6.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(EspeakChat.primary)
                    )
                }
            }
        }
    }
}

/* ============================================================
   WELCOME BUBBLE — первое сообщение при пустом чате
   ============================================================ */
@Composable
private fun WelcomeBubble(onSpeak: (String) -> Unit) {
    val text = "¡Hola! Soy tu profesor de **ESPEAK**. ¿De qué quieres hablar hoy?"
    ChatMessageItem(
        role = "assistant",
        content = "$text ⟦RU⟧ Привет! Я твой преподаватель ESPEAK. О чём хочешь сегодня поговорить?",
        correctionJson = "",
        onSpeak = onSpeak,
        onCorrectionParse = { emptyList() },
    )
}

/* ============================================================
   QUICK CHIPS — таргетированные follow-up команды для AI
   v1.24.13: каждый chip отправляет ОСМЫСЛЕННЫЙ промт, не просто label.
   Gemini имеет контекст истории → может выполнить "Объясни проще /
   Дай пример / Дай упражнение / Говори медленнее / Перефразируй".
   ============================================================ */
private data class QuickChip(val emoji: String, val label: String, val prompt: String)

private val QUICK_CHIPS = listOf(
    QuickChip(
        emoji = "💡",
        label = "Объясни проще",
        prompt = "Объясни своё последнее сообщение проще: короткими фразами на уровне A1. " +
            "Используй базовую лексику и продублируй ключевые слова на русском.",
    ),
    QuickChip(
        emoji = "📝",
        label = "Дай пример",
        prompt = "Приведи 2–3 конкретных коротких примера на испанском по теме нашего разговора. " +
            "После каждого — перевод в [скобках].",
    ),
    QuickChip(
        emoji = "🎯",
        label = "Дай упражнение",
        prompt = "Дай мне небольшое упражнение на испанском по нашей текущей теме. " +
            "Один вопрос или мини-задание (заполнить пропуск, выбрать форму, перевести фразу).",
    ),
    QuickChip(
        emoji = "🐢",
        label = "Помедленнее",
        prompt = "Повтори свою мысль более простыми словами и короткими предложениями. " +
            "Снизь сложность лексики до A2.",
    ),
    QuickChip(
        emoji = "🔁",
        label = "Перефразируй",
        prompt = "Перефразируй своё последнее сообщение, используя другие слова и конструкции. " +
            "Сохрани смысл, измени формулировку — это помогает мне видеть разные варианты.",
    ),
)

@Composable
private fun QuickChipsRow(onChipPrompt: (String) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
    ) {
        items(QUICK_CHIPS) { chip ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(EspeakChat.primary.copy(alpha = 0.14f))
                    .border(
                        1.dp,
                        EspeakChat.primary.copy(alpha = 0.35f),
                        RoundedCornerShape(16.dp),
                    )
                    .clickable { onChipPrompt(chip.prompt) }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(chip.emoji, fontSize = 14.sp)
                    Text(
                        chip.label,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EspeakChat.primary,
                    )
                }
            }
        }
    }
}

/* ============================================================
   COMPOSER — поле ввода с микрофоном/send (auto-toggle)
   ============================================================ */
@Composable
private fun ChatComposer(
    inputValue: androidx.compose.ui.text.input.TextFieldValue,
    onValueChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
    onSend: () -> Unit,
    onMic: () -> Unit,
    onCancelVoice: () -> Unit = {},
    isRecordingVoice: Boolean = false,
    voiceRecordingMs: Long = 0L,
    voiceRecordingAmp: Float = 0f,
    isListening: Boolean,
    voiceAmplitude: Float,
) {
    val sendActive = inputValue.text.isNotBlank()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 4.dp, bottom = 14.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // v1.25.0: режим recording — pill заменяется на воспроизводящий timer/waveform/cancel
            if (isRecordingVoice) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFF6B0F0F).copy(alpha = 0.18f),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 42.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // ✗ отмена
                        IconButton(onClick = onCancelVoice, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Отменить запись",
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        // ● пульсирующая красная точка
                        val recPulse by rememberInfiniteTransition(label = "rec").animateFloat(
                            initialValue = 0.5f, targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                tween(700), RepeatMode.Reverse,
                            ),
                            label = "rec_dot",
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE53935).copy(alpha = recPulse)),
                        )
                        // mm:ss timer
                        val mm = (voiceRecordingMs / 60000)
                        val ss = ((voiceRecordingMs / 1000) % 60).toInt()
                        Text(
                            "%d:%02d".format(mm, ss),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        // тонкий waveform
                        VoiceWaveform(
                            amplitude = voiceRecordingAmp,
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp),
                        )
                    }
                }
            } else
            // Поле ввода — readOnly BasicTextField:
            // системная клава НЕ вызывается (readOnly), но курсор,
            // tap-to-position и long-press selection РАБОТАЮТ нативно.
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 42.dp),
            ) {
                if (isListening) {
                    VoiceWaveform(
                        amplitude = voiceAmplitude,
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                    )
                } else {
                    // v1.24.9: Своя мигающая каретка как overlay над BasicTextField.
                    // BasicTextField(readOnly=true) НЕ рисует курсор сам — Compose
                    // намеренно скрывает каретку в read-only. Поэтому мы:
                    //  1. Получаем TextLayoutResult через onTextLayout
                    //  2. Считаем cursor rect через layout.getCursorRect(selection.start)
                    //  3. Рисуем blinking Box в этой позиции
                    var textLayout by remember {
                        mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null)
                    }
                    val cursorBlink by rememberInfiniteTransition(label = "cursor").animateFloat(
                        initialValue = 1f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(530, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse,
                        ),
                        label = "cursor_alpha",
                    )
                    val density = LocalDensity.current
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 11.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (inputValue.text.isEmpty()) {
                            Text(
                                "Escribe a tu profesor(a)…",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 15.5.sp,
                            )
                        }
                        BasicTextField(
                            value = inputValue,
                            onValueChange = onValueChange,
                            readOnly = true,           // ← блокирует системную клаву!
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.5.sp,
                            ),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.Transparent),
                            maxLines = 5,
                            onTextLayout = { textLayout = it },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        // Кастомная каретка
                        textLayout?.let { layout ->
                            val cursorPos = inputValue.selection.start.coerceIn(0, inputValue.text.length)
                            val rect = runCatching { layout.getCursorRect(cursorPos) }.getOrNull()
                            if (rect != null) {
                                Box(
                                    modifier = Modifier
                                        .offset {
                                            androidx.compose.ui.unit.IntOffset(
                                                rect.left.toInt(),
                                                rect.top.toInt(),
                                            )
                                        }
                                        .size(
                                            width = 2.dp,
                                            height = with(density) { rect.height.toDp() },
                                        )
                                        .background(EspeakChat.primary.copy(alpha = cursorBlink)),
                                )
                            }
                        }
                    }
                }
            }

            // Кнопка: Send (если есть текст) / Stop+Send (если идёт запись) / Mic (если пусто)
            ActionButton(
                isSend = sendActive || isRecordingVoice,
                isListening = isListening,
                onClick = when {
                    sendActive -> onSend
                    isRecordingVoice -> onMic  // стоп + send
                    else -> onMic              // старт записи
                },
            )
        }
    }
}

@Composable
private fun ActionButton(
    isSend: Boolean,
    isListening: Boolean,
    onClick: () -> Unit,
) {
    val recordingPulse by rememberInfiniteTransition(label = "rec").animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "recPulse",
    )

    FloatingActionButton(
        onClick = onClick,
        containerColor = if (isListening) EspeakChat.error else EspeakChat.primary,
        contentColor = Color.White,
        modifier = Modifier
            .size(42.dp)
            .scale(if (isListening) recordingPulse else 1f),
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(2.dp, 4.dp),
    ) {
        Icon(
            imageVector = if (isSend) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
            contentDescription = if (isSend) "Отправить" else "Голос",
            modifier = Modifier.size(18.dp),
        )
    }
}

/* ============================================================
   VOICE WAVEFORM — 10 пляшущих баров по rmsDb
   ============================================================ */
@Composable
private fun VoiceWaveform(amplitude: Float, modifier: Modifier = Modifier) {
    val bars = 10
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        repeat(bars) { i ->
            val transition = rememberInfiniteTransition(label = "wave$i")
            val baseHeight by transition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(i * 80),
                ),
                label = "h$i",
            )
            val ampBoost = (amplitude.coerceIn(-2f, 10f) + 2f) / 12f
            val finalHeight = baseHeight * (0.4f + ampBoost * 0.6f)
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((30 * finalHeight).dp.coerceAtLeast(6.dp))
                    .clip(RoundedCornerShape(2.dp))
                    .background(EspeakChat.primary)
            )
        }
    }
}

