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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
    val tutor          by vm.tutorProfile.collectAsStateWithLifecycle()

    // v1.24.5: при первом заходе показать setup, если профиль не настроен.
    var showSetup by remember(tutor.configured) { mutableStateOf(!tutor.configured) }
    var showEdit by remember { mutableStateOf(false) }

    // v1.24.6: TextFieldValue для полноценной поддержки курсора/выделения.
    // BasicTextField(readOnly=true) НЕ вызывает системную клаву, но позволяет
    // tap-to-position cursor и selection-by-long-press — как в S26 Ultra.
    var inputValue by remember { mutableStateOf(androidx.compose.ui.text.input.TextFieldValue("")) }
    val input = inputValue.text
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
                tutorName = tutor.name,
                tutorAvatar = tutor.avatar,
                scenarioEmoji = scenario.emoji,
                scenarioTitle = scenario.title,
                level = "B1",
                limit = "47/50",
                onBack = { navController.popBackStack() },
                onEditTutor = { showEdit = true },
                onNewChat = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    vm.clearCurrentSession()
                },
            )
        },
        bottomBar = {
            Column {
                QuickChipsRow(onChip = { suggestion ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    vm.send(suggestion)
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
                        vm.startVoice { recognized ->
                            inputValue = androidx.compose.ui.text.input.TextFieldValue(
                                text = recognized,
                                selection = androidx.compose.ui.text.TextRange(recognized.length),
                            )
                        }
                    },
                    isListening = isListening,
                    voiceAmplitude = voiceAmplitude,
                )
                // v1.24.6: pro-уровень клавиатура с курсором, swipe-space, suggestions
                val suggestions = remember(input) { WordSuggester.suggest(input) }
                SpanishKeyboard(
                    value = inputValue,
                    onValueChange = { inputValue = it },
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
                onSelect = { vm.selectScenario(it) },
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
                    item { WelcomeBubble(tutorName = tutor.name, tutorAvatar = tutor.avatar, onSpeak = { vm.speak(it) }) }
                } else {
                    items(messages, key = { it.id }) { msg ->
                        ChatMessageItem(
                            role = msg.role,
                            content = msg.content,
                            correctionJson = msg.correctionJson,
                            tutorAvatar = tutor.avatar,
                            onSpeak = { vm.speak(it) },
                            onCorrectionParse = { vm.corrections(it) },
                        )
                    }
                }
                if (isSending) {
                    item { TypingIndicator(tutorAvatar = tutor.avatar) }
                }
            }
        }
    }

    // v1.24.5: первичная настройка наставника при первом заходе
    if (showSetup) {
        TutorSetupDialog(
            initialName = tutor.name.takeIf { it != "Tutor" } ?: "",
            initialAvatar = tutor.avatar,
            isFirstTime = true,
            onDismiss = {
                // При первом запуске пропустить — сохраняем дефолты "Tutor"/🤖
                vm.saveTutorProfile("Tutor", "🤖")
                showSetup = false
            },
            onSave = { name, avatar ->
                vm.saveTutorProfile(name, avatar)
                showSetup = false
            },
        )
    }

    // Редактирование профиля (по тапу на имя/аватар в header)
    if (showEdit) {
        TutorSetupDialog(
            initialName = tutor.name,
            initialAvatar = tutor.avatar,
            isFirstTime = false,
            onDismiss = { showEdit = false },
            onSave = { name, avatar ->
                vm.saveTutorProfile(name, avatar)
                showEdit = false
            },
        )
    }
}

/* ============================================================
   HEADER — чистый Material3-стиль в фирменных цветах ESPEAK
   v1.24.5: аватарка emoji из TutorProfile, имя кастомизируется,
   стиль приведён к общему M3 без розовых градиентов.
   ============================================================ */
@Composable
private fun ChatHeader(
    tutorName: String,
    tutorAvatar: String,
    scenarioEmoji: String,
    scenarioTitle: String,
    level: String,
    limit: String,
    onBack: () -> Unit,
    onEditTutor: () -> Unit,
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
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onEditTutor)
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TutorAvatarEmoji(emoji = tutorAvatar, size = 40.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            tutorName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.width(6.dp))
                        LevelPill(level)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "$scenarioEmoji $scenarioTitle",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                        )
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
private fun TutorAvatarEmoji(emoji: String, size: androidx.compose.ui.unit.Dp) {
    // v1.24.5: emoji-аватар из TutorProfile. Без розового градиента —
    // используем фирменный оранжевый-в-сурface стиль (общий с приложением).
    Box(modifier = Modifier.size(size)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(EspeakChat.primary.copy(alpha = 0.18f))
                .border(
                    width = 1.5.dp,
                    color = EspeakChat.primary,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = (size.value * 0.55f).sp)
        }
        // Маленькая зелёная точка "online"
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
    onSelect: (com.spanishapp.domain.chat.ChatScenario) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
    ) {
        items(ChatScenarios.all, key = { it.id }) { sc ->
            val isActive = sc.id == selectedId
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isActive) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.surface,
                border = if (!isActive)
                    androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
                else null,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onSelect(sc) },
            ) {
                Box {
                    Row(
                        modifier = Modifier.padding(start = 9.dp, end = 12.dp, top = 7.dp, bottom = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text(sc.emoji, fontSize = 14.sp)
                        Text(
                            sc.title,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isActive) MaterialTheme.colorScheme.surface
                                    else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    if (sc.isPro && !isActive) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(EspeakChat.gold),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("💎", fontSize = 9.sp)
                        }
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
    tutorAvatar: String = "🤖",
    onSpeak: (String) -> Unit,
    onCorrectionParse: (String) -> List<com.spanishapp.data.repository.ChatCorrection>,
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
            TutorAvatarSmall(emoji = tutorAvatar, size = 28.dp)
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
                    // Парсим текст: ⟦RU⟧ отделяет испанскую и русскую части
                    val (esPart, ruPart) = splitBilingual(content)
                    BilingualText(
                        es = esPart,
                        ru = ruPart,
                        isUser = isUser,
                        onWordTap = { word -> onSpeak(word) },
                    )

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
private fun TypingIndicator(tutorAvatar: String = "🤖") {
    Row(verticalAlignment = Alignment.Bottom) {
        TutorAvatarSmall(emoji = tutorAvatar, size = 28.dp)
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
private fun WelcomeBubble(tutorName: String, tutorAvatar: String, onSpeak: (String) -> Unit) {
    val text = "¡Hola! Soy **$tutorName**, tu profesor(a) de español. ¿De qué quieres hablar hoy?"
    ChatMessageItem(
        role = "assistant",
        content = "$text ⟦RU⟧ Привет! Я $tutorName, твой преподаватель испанского. О чём хочешь сегодня поговорить?",
        correctionJson = "",
        tutorAvatar = tutorAvatar,
        onSpeak = onSpeak,
        onCorrectionParse = { emptyList() },
    )
}

/* ============================================================
   QUICK CHIPS — над инпутом, готовые промты
   ============================================================ */
@Composable
private fun QuickChipsRow(onChip: (String) -> Unit) {
    val chips = listOf(
        "💡" to "Объясни проще",
        "📝" to "Дай пример",
        "🎯" to "Дай мне упражнение",
        "🐢" to "Говори помедленнее",
        "🔁" to "Перефразируй",
    )
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
    ) {
        items(chips) { (emo, text) ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = EspeakChat.primaryPale,
                modifier = Modifier.clickable { onChip(text) },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(emo, fontSize = 13.sp)
                    Text(
                        text,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC24A1A),
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
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(EspeakChat.primary),
                            maxLines = 5,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            // Кнопка: Send (если есть текст) или Mic (если пусто)
            ActionButton(
                isSend = sendActive,
                isListening = isListening,
                onClick = if (sendActive) onSend else onMic,
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

/* ============================================================
   TUTOR SMALL AVATAR — emoji-аватарка в M3-стиле для bubble
   ============================================================ */
@Composable
private fun TutorAvatarSmall(emoji: String, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(EspeakChat.primary.copy(alpha = 0.18f))
            .border(1.dp, EspeakChat.primary.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, fontSize = (size.value * 0.55f).sp)
    }
}

/* ============================================================
   TUTOR SETUP DIALOG — выбор имени и аватарки наставника
   ============================================================ */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun TutorSetupDialog(
    initialName: String,
    initialAvatar: String,
    isFirstTime: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, avatar: String) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var avatar by remember { mutableStateOf(initialAvatar) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isFirstTime) "Познакомься с наставником"
                else "Настрой наставника",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                Text(
                    if (isFirstTime)
                        "Выбери имя и аватар. Наставник запомнит и встретит тебя при следующем заходе."
                    else "Изменить имя или аватар.",
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))

                // Текущая аватарка крупно по центру
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(EspeakChat.primary.copy(alpha = 0.18f))
                            .border(2.dp, EspeakChat.primary, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(avatar, fontSize = 40.sp)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Поле имени (используем системную клаву только в setup —
                // это разовая операция, не основной чат)
                androidx.compose.material3.OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 20) name = it },
                    label = { Text("Имя наставника") },
                    placeholder = { Text("Tutor, Lucía, Maestro, …") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    "Аватар",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    com.spanishapp.data.prefs.TutorProfilePreferences.AVATARS.forEach { emo ->
                        val selected = emo == avatar
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) EspeakChat.primary.copy(alpha = 0.25f)
                                    else MaterialTheme.colorScheme.surfaceContainerHighest
                                )
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) EspeakChat.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = CircleShape,
                                )
                                .clickable { avatar = emo },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(emo, fontSize = 22.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = { onSave(name.ifBlank { "Tutor" }, avatar) }
            ) {
                Text(if (isFirstTime) "Готово" else "Сохранить", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = if (!isFirstTime) {
            { androidx.compose.material3.TextButton(onClick = onDismiss) { Text("Отмена") } }
        } else null,
    )
}
