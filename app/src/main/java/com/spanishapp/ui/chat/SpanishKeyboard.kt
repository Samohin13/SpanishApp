package com.spanishapp.ui.chat

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardCapslock
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay

/**
 * Pro-уровень Compose-клавиатура, реагирующая как Gboard:
 * фиксирует нажатие на DOWN (немедленно), не дожидаясь UP.
 *
 * Архитектура:
 *  • Все клавиши через detectTapGestures(onPress = { ... }) — fire on DOWN
 *  • Long-press через onLongPress callback (500ms)
 *  • Auto-repeat через LaunchedEffect(pressed) — пока кнопка зажата
 *  • Backspace: char-delete по тапу, char-delete repeat по long-press
 *  • Cursor: реальная мигающая каретка (overlay над BasicTextField)
 */

enum class KbLayout { ES, RU, NUM }

private data class KbKey(
    val label: String,
    val output: String = label,
    val accents: List<String> = emptyList(),
)

@Composable
fun SpanishKeyboard(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSend: () -> Unit,
    canSend: Boolean,
    layout: KbLayout = KbLayout.ES,
    onLayoutChange: (KbLayout) -> Unit = {},
    suggestions: List<String> = emptyList(),
    onPickSuggestion: (String) -> Unit = {},
    // v1.24.20: словарь для glide-matching. Если пуст — glide отключён.
    glideDictionary: List<String> = emptyList(),
    userWordFreq: Map<String, Int> = emptyMap(),
    modifier: Modifier = Modifier,
) {
    // v1.24.20: позиции клавиш для glide-typing.
    // KeyButton регистрирует свой Rect через onGloballyPositioned.
    val keyPositions = remember { mutableStateMapOf<String, androidx.compose.ui.geometry.Rect>() }
    var shifted by remember { mutableStateOf(true) }
    var capsLock by remember { mutableStateOf(false) }
    var collapsed by remember { mutableStateOf(false) }
    var lastShiftTap by remember { mutableStateOf(0L) }

    val haptic = LocalHapticFeedback.current
    val keyBg = MaterialTheme.colorScheme.surfaceContainerHighest
    val specialKeyBg = MaterialTheme.colorScheme.surfaceContainerHigh
    val textColor = MaterialTheme.colorScheme.onSurface
    val accent = Color(0xFFFF8A3D)

    val emit: (String) -> Unit = { s ->
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        val shiftedChar = KeyboardLogic.applyShift(
            s, shifted, capsLock, layout == KbLayout.NUM,
        )
        // v1.24.19: double-space → ". " (как iOS/Gboard).
        val newValue0 = if (shiftedChar == " " && KeyboardLogic.canDoubleSpacePeriod(value)) {
            KeyboardLogic.doubleSpaceToPeriod(value)
        } else {
            KeyboardLogic.insertAt(value, shiftedChar)
        }
        // v1.25.9: SpellChecker autocorrect on space.
        // Если ввели space или знак — проверяем последнее слово.
        // Если SpellChecker уверенно подсказывает замену → заменяем.
        val newValue = if ((shiftedChar == " " || shiftedChar == "." ||
                            shiftedChar == "," || shiftedChar == "!" || shiftedChar == "?")
            && layout != KbLayout.NUM) {
            applyAutocorrect(newValue0, layout, userWordFreq)
        } else newValue0
        onValueChange(newValue)
        if (shifted && !capsLock) shifted = false
        if (!capsLock && layout != KbLayout.NUM) {
            if (KeyboardLogic.shouldAutoCapAfter(newValue.text, newValue.selection.start)) {
                shifted = true
            }
        }
    }

    val rows: List<List<KbKey>> = when (layout) {
        KbLayout.ES -> esLetterRows()
        KbLayout.RU -> ruLetterRows()
        KbLayout.NUM -> numRows()
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // v1.24.14: collapsed handle переехал ВНИЗ клавы (в самый низ).
            // Верхняя полоса теперь — ВСЕГДА зарезервированная 40dp слот
            // под suggestions. Если подсказок нет — пустое место. Это значит:
            // когда юзер начинает печатать, suggestions появляются БЕЗ
            // расширения клавы → input field не прыгает.
            if (collapsed) {
                // В свёрнутом виде показываем тонкую полосу с шевроном для разворота
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            collapsed = false
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = "Развернуть",
                        tint = textColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp),
                    )
                }
                return@Column
            }

            // Suggestions slot — ВСЕГДА 32dp, чтобы инпут не прыгал.
            // 32dp = text 14sp + минимальный padding, без лишнего воздуха.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (suggestions.isNotEmpty()) {
                    SuggestionStrip(
                        suggestions = suggestions.take(3),
                        onPick = { sug ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onPickSuggestion(sug)
                        },
                        textColor = textColor,
                    )
                }
            }

            // Цифровой ряд
            if (layout != KbLayout.NUM) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    listOf("1","2","3","4","5","6","7","8","9","0").forEach { d ->
                        KeyButton(
                            label = d,
                            output = d,
                            accents = emptyList(),
                            bg = keyBg,
                            textColor = textColor.copy(alpha = 0.85f),
                            accent = accent,
                            modifier = Modifier.weight(1f),
                            heightDp = 32,
                            fontSp = 18,
                            onTap = emit,
                            haptic = haptic,
                        )
                    }
                }
            }

            // Основные буквенные ряды (внутри glide-overlay Box)
            GlideOverlay(
                keyPositions = keyPositions,
                glideDictionary = glideDictionary,
                userWordFreq = userWordFreq,
                value = value,
                onValueChange = onValueChange,
                isLetterLayout = layout != KbLayout.NUM,
                haptic = haptic,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rows.take(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            row.forEach { key ->
                                KeyButton(
                                    label = if ((shifted || capsLock) && layout != KbLayout.NUM)
                                        key.label.uppercase() else key.label,
                                    output = key.output,
                                    accents = key.accents,
                                    bg = keyBg,
                                    textColor = textColor,
                                    accent = accent,
                                    modifier = Modifier.weight(1f),
                                    onTap = emit,
                                    haptic = haptic,
                                    registerPositionKey = key.label,
                                    keyPositions = keyPositions,
                                )
                            }
                        }
                    }
                }
            }

            // 3-й ряд: shift + буквы + backspace
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (layout != KbLayout.NUM) {
                    SpecialKey(
                        bg = when {
                            capsLock -> accent
                            shifted -> accent.copy(alpha = 0.3f)
                            else -> specialKeyBg
                        },
                        modifier = Modifier.weight(1.4f),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val now = System.currentTimeMillis()
                            if (now - lastShiftTap < 300) {
                                capsLock = !capsLock
                                shifted = capsLock
                            } else {
                                if (capsLock) { capsLock = false; shifted = false }
                                else shifted = !shifted
                            }
                            lastShiftTap = now
                        },
                    ) {
                        Icon(
                            if (capsLock) Icons.Default.KeyboardCapslock
                            else Icons.Default.KeyboardArrowUp,
                            contentDescription = if (capsLock) "Caps" else "Shift",
                            tint = when {
                                capsLock -> Color.White
                                shifted -> accent
                                else -> textColor
                            },
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                rows[2].forEach { key ->
                    KeyButton(
                        label = if ((shifted || capsLock) && layout != KbLayout.NUM)
                            key.label.uppercase() else key.label,
                        output = key.output,
                        accents = key.accents,
                        bg = keyBg,
                        textColor = textColor,
                        accent = accent,
                        modifier = Modifier.weight(1f),
                        onTap = emit,
                        haptic = haptic,
                        // v1.25.2: row 3 буквы тоже регистрируем для glide
                        registerPositionKey = key.label,
                        keyPositions = keyPositions,
                    )
                }
                BackspaceKey(
                    bg = specialKeyBg,
                    textColor = textColor,
                    accent = accent,
                    modifier = Modifier.weight(1.4f),
                    onCharDelete = { onValueChange(KeyboardLogic.backspaceChar(value)) },
                    haptic = haptic,
                )
            }

            // 4-й ряд: 123 + globe + space (swipe) + . + send
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SpecialKey(
                    bg = specialKeyBg,
                    modifier = Modifier.weight(1.4f),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onLayoutChange(if (layout == KbLayout.NUM) KbLayout.ES else KbLayout.NUM)
                        shifted = false
                        capsLock = false
                    },
                ) {
                    Text(
                        if (layout == KbLayout.NUM) "ABC" else "!1#",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = textColor,
                    )
                }
                // v1.24.19: long-press на globe → выпадайка со всеми раскладками
                GlobeKey(
                    bg = specialKeyBg,
                    textColor = textColor,
                    accent = accent,
                    modifier = Modifier.weight(1.1f),
                    currentLayout = layout,
                    onShortCycle = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onLayoutChange(when (layout) {
                            KbLayout.ES -> KbLayout.RU
                            KbLayout.RU -> KbLayout.ES
                            KbLayout.NUM -> KbLayout.RU
                        })
                        shifted = false
                        capsLock = false
                    },
                    onPickLayout = { picked ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onLayoutChange(picked)
                        shifted = false
                        capsLock = false
                    },
                    haptic = haptic,
                )
                SpaceKey(
                    layout = layout,
                    bg = keyBg,
                    textColor = textColor,
                    haptic = haptic,
                    modifier = Modifier.weight(5f),
                    onTap = { emit(" ") },
                    onSwipe = { delta -> onValueChange(KeyboardLogic.moveCursor(value, delta)) },
                )
                KeyButton(
                    label = if (layout == KbLayout.NUM) "?" else ".",
                    output = if (layout == KbLayout.NUM) "?" else ".",
                    accents = emptyList(),
                    bg = specialKeyBg,
                    textColor = textColor,
                    accent = accent,
                    modifier = Modifier.weight(1f),
                    onTap = emit,
                    haptic = haptic,
                )
                SpecialKey(
                    bg = if (canSend) accent else specialKeyBg,
                    modifier = Modifier.weight(1.6f),
                    onClick = {
                        if (canSend) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSend()
                        }
                    },
                ) {
                    Icon(
                        if (canSend) Icons.AutoMirrored.Filled.Send
                        else Icons.AutoMirrored.Filled.KeyboardReturn,
                        contentDescription = if (canSend) "Send" else "Enter",
                        tint = if (canSend) Color.White else textColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            // Collapse-кнопка справа-снизу, компактно (Samsung-style)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 20.dp)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            collapsed = true
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Свернуть клавиатуру",
                        tint = textColor.copy(alpha = 0.45f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

/* ============================================================
   SUGGESTION STRIP
   ============================================================ */
@Composable
private fun SuggestionStrip(
    suggestions: List<String>,
    onPick: (String) -> Unit,
    textColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(32.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        suggestions.forEach { sug ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onPick(sug) },
                contentAlignment = Alignment.Center,
            ) {
                Text(sug, color = textColor, fontSize = 14.sp,
                    fontWeight = FontWeight.Medium, maxLines = 1)
            }
        }
    }
}

/* ============================================================
   KEY BUTTON — Gboard-style: FIRE ON DOWN, не на UP!
   - detectTapGestures(onPress) срабатывает на касание
   - onLongPress показывает акценты или запускает repeat
   - Press feedback через `pressed` state
   ============================================================ */
@Composable
private fun KeyButton(
    label: String,
    output: String,
    accents: List<String>,
    bg: Color,
    textColor: Color,
    accent: Color,
    modifier: Modifier = Modifier,
    heightDp: Int = 40,
    fontSp: Int = 21,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    onTap: (String) -> Unit,
    // v1.24.20: для glide-typing — клавиша регистрирует свою позицию.
    registerPositionKey: String? = null,
    keyPositions: androidx.compose.runtime.snapshots.SnapshotStateMap<
        String, androidx.compose.ui.geometry.Rect>? = null,
) {
    var showAccents by remember { mutableStateOf(false) }
    var pressed by remember { mutableStateOf(false) }
    var hoveredAccentIdx by remember { mutableStateOf(-1) }  // активный акцент при slide

    val bgColor by animateColorAsState(
        targetValue = if (pressed) accent.copy(alpha = 0.35f) else bg,
        animationSpec = tween(60),
        label = "key_bg",
    )
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(80),
        label = "key_scale",
    )

    val currentOnTap by rememberUpdatedState(onTap)
    val currentOutput by rememberUpdatedState(output)
    val currentAccents by rememberUpdatedState(accents)

    // Auto-repeat ТОЛЬКО для клавиш без accents
    LaunchedEffect(pressed) {
        if (pressed && currentAccents.isEmpty()) {
            delay(400)
            while (pressed) {
                currentOnTap(currentOutput)
                delay(50)
            }
        }
    }

    // v1.24.12: единый pointer flow для continuous accent gesture.
    // hold → popup → slide без отрыва → release на нужном accent.
    // awaitPointerEventScope даёт fine-grained контроль над событиями.
    val density = LocalDensity.current
    val accentKeyWidthPx = remember { with(density) { 48.dp.toPx() } }  // 44dp + 4dp gap
    val popupVerticalRangePx = remember { with(density) { (-72).dp.toPx() } }

    Box(
        modifier = modifier
            .height(heightDp.dp)
            .let { m ->
                if (registerPositionKey != null && keyPositions != null) {
                    m.onGloballyPositioned { coords ->
                        keyPositions[registerPositionKey] =
                            coords.boundsInRoot()
                    }
                } else m
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown()
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        // Fire on DOWN — Gboard стиль
                        if (currentAccents.isEmpty()) {
                            currentOnTap(currentOutput)
                        }
                        pressed = true
                        val startTime = System.currentTimeMillis()
                        var enteredAccentMode = false
                        var lastPos = down.position

                        try {
                            while (true) {
                                val event = androidx.compose.ui.input.pointer.PointerEventPass.Main
                                    .let { awaitPointerEvent(it) }
                                val change = event.changes.firstOrNull() ?: break
                                lastPos = change.position

                                if (!change.pressed) {
                                    // UP — финализируем
                                    if (enteredAccentMode) {
                                        if (hoveredAccentIdx in currentAccents.indices) {
                                            val variant = currentAccents[hoveredAccentIdx]
                                            // Применяем shift если label был uppercase
                                            val out = if (label != label.lowercase())
                                                variant.uppercase() else variant
                                            currentOnTap(out)
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                        // если палец вне popup → отмена (ничего не печатаем)
                                    } else if (currentAccents.isNotEmpty()) {
                                        // long-press не наступил, было обычное касание клавиши с accents
                                        currentOnTap(currentOutput)
                                    }
                                    pressed = false
                                    showAccents = false
                                    hoveredAccentIdx = -1
                                    break
                                }

                                // Pressed — check long-press timer и accent mode
                                val elapsed = System.currentTimeMillis() - startTime
                                if (!enteredAccentMode && elapsed > 320 && currentAccents.isNotEmpty()) {
                                    enteredAccentMode = true
                                    showAccents = true
                                    hoveredAccentIdx = currentAccents.size / 2  // дефолт — средняя клавиша
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }

                                if (enteredAccentMode && currentAccents.isNotEmpty()) {
                                    // Считаем над какой accent сейчас палец
                                    // Popup центрирован над клавишей, accent-row начинается на
                                    // (key_width - popup_width) / 2. Палец в координатах клавиши.
                                    val keyWidthPx = size.width.toFloat()
                                    val popupWidthPx = currentAccents.size * accentKeyWidthPx + 8.dp.toPx()
                                    val popupLeftRelToKey = (keyWidthPx - popupWidthPx) / 2f + 6.dp.toPx()
                                    val xInPopup = change.position.x - popupLeftRelToKey
                                    val rawIdx = (xInPopup / accentKeyWidthPx).toInt()
                                    val newIdx = rawIdx.coerceIn(0, currentAccents.size - 1)
                                    if (newIdx != hoveredAccentIdx) {
                                        hoveredAccentIdx = newIdx
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                }

                                change.consume()
                            }
                        } finally {
                            pressed = false
                            showAccents = false
                            hoveredAccentIdx = -1
                        }
                    }
                }
            }
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = textColor, fontSize = fontSp.sp, fontWeight = FontWeight.Medium)
        // v1.25.9: Samsung-style hint в верхнем-правом углу клавиши.
        // Показывает первый accent (что появится по long-press) — даёт
        // визуальный сигнал юзеру что доступно скрытое.
        if (accents.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 3.dp, end = 5.dp),
                contentAlignment = Alignment.TopEnd,
            ) {
                Text(
                    accents.first(),
                    color = textColor.copy(alpha = 0.45f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal,
                )
            }
        }

        if (showAccents && accents.isNotEmpty()) {
            val aboveProvider = remember {
                object : androidx.compose.ui.window.PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: androidx.compose.ui.unit.IntRect,
                        windowSize: androidx.compose.ui.unit.IntSize,
                        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
                        popupContentSize: androidx.compose.ui.unit.IntSize,
                    ): androidx.compose.ui.unit.IntOffset {
                        val x = (anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2)
                            .coerceIn(8, (windowSize.width - popupContentSize.width - 8).coerceAtLeast(8))
                        val y = (anchorBounds.top - popupContentSize.height - 12).coerceAtLeast(8)
                        return androidx.compose.ui.unit.IntOffset(x, y)
                    }
                }
            }
            // properties: focusable=false критично — иначе popup перехватит pointer events
            Popup(
                popupPositionProvider = aboveProvider,
                properties = PopupProperties(focusable = false),
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shadowElevation = 12.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        accents.forEachIndexed { idx, variant ->
                            val isHovered = idx == hoveredAccentIdx
                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 40.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isHovered) accent
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        if (isHovered) 0.dp else 1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        RoundedCornerShape(6.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    variant,
                                    color = if (isHovered) Color.White else textColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ============================================================
   BACKSPACE — char-delete fire-on-down, repeat ПОБУКВЕННО
   ============================================================ */
@Composable
private fun BackspaceKey(
    bg: Color,
    textColor: Color,
    accent: Color,
    modifier: Modifier = Modifier,
    onCharDelete: () -> Unit,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
) {
    var pressed by remember { mutableStateOf(false) }
    val bgColor by animateColorAsState(
        if (pressed) accent.copy(alpha = 0.35f) else bg,
        animationSpec = tween(60),
        label = "bs_bg",
    )

    // v1.24.10: rememberUpdatedState чтобы pointerInput захватывал
    // СВЕЖИЙ onCharDelete (он использует актуальный `value` из ViewModel).
    val currentDelete by rememberUpdatedState(onCharDelete)

    // Repeat только char-delete, бесконечно пока зажато
    LaunchedEffect(pressed) {
        if (pressed) {
            delay(400)
            while (pressed) {
                currentDelete()
                delay(45)
            }
        }
    }

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(bgColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { _ ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        currentDelete()      // FIRE on DOWN
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Default.Backspace,
            contentDescription = "Backspace",
            tint = textColor,
            modifier = Modifier.size(20.dp),
        )
    }
}

/* ============================================================
   SPACE KEY — swipe для перемещения курсора
   ============================================================ */
@Composable
private fun SpaceKey(
    layout: KbLayout,
    bg: Color,
    textColor: Color,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    modifier: Modifier = Modifier,
    onTap: () -> Unit,
    onSwipe: (delta: Int) -> Unit,
) {
    val density = LocalDensity.current
    val pxPerChar = with(density) { 10.dp.toPx() }
    var accumPx by remember { mutableStateOf(0f) }
    var didSwipe by remember { mutableStateOf(false) }
    var pressed by remember { mutableStateOf(false) }
    val bgColor by animateColorAsState(
        if (pressed) MaterialTheme.colorScheme.surfaceContainerHigh else bg,
        animationSpec = tween(60),
        label = "space_bg",
    )
    val currentTap by rememberUpdatedState(onTap)
    val currentSwipe by rememberUpdatedState(onSwipe)

    // v1.24.13: единый pointerInput для space-key.
    // Раньше было ДВА конкурирующих pointerInput (detectTap + detectDrag) →
    // на тапе вставлялся пробел И срабатывал drag-курсор одновременно.
    // Юзер: "при нажатии на пробел он разделяет слова а потом берется за курсор".
    // Теперь: единый awaitPointerEventScope — drag > 10dp = cursor mode, иначе = space.
    val SWIPE_THRESHOLD_PX = remember { with(density) { 10.dp.toPx() } }

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(bgColor)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown()
                        pressed = true
                        accumPx = 0f
                        didSwipe = false
                        try {
                            while (true) {
                                val event = awaitPointerEvent(
                                    androidx.compose.ui.input.pointer.PointerEventPass.Main
                                )
                                val change = event.changes.firstOrNull() ?: break
                                if (!change.pressed) {
                                    // UP — если НЕ свайпили, вставляем пробел
                                    if (!didSwipe) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        currentTap()
                                    }
                                    break
                                }
                                // Накапливаем смещение
                                val dx = change.position.x - down.position.x
                                if (!didSwipe && kotlin.math.abs(dx) > SWIPE_THRESHOLD_PX) {
                                    didSwipe = true
                                    accumPx = dx
                                }
                                if (didSwipe) {
                                    accumPx += change.positionChange().x
                                    if (kotlin.math.abs(accumPx) >= pxPerChar) {
                                        val delta = (accumPx / pxPerChar).toInt()
                                        accumPx -= delta * pxPerChar
                                        currentSwipe(delta)
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    change.consume()
                                }
                            }
                        } finally {
                            pressed = false
                            accumPx = 0f
                            didSwipe = false
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            when (layout) {
                KbLayout.ES -> "español"
                KbLayout.RU -> "русский"
                KbLayout.NUM -> "пробел"
            },
            color = textColor.copy(alpha = 0.6f),
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun SpecialKey(
    bg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val bgColor by animateColorAsState(
        if (pressed) bg.copy(alpha = (bg.alpha * 0.7f).coerceAtLeast(0.4f)) else bg,
        animationSpec = tween(60),
        label = "special_bg",
    )
    val currentClick by rememberUpdatedState(onClick)
    // v1.24.13: haptic был только на главных клавишах, на SpecialKey — нет.
    // Теперь каждая клавиша вибрирует при нажатии.
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(bgColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { _ ->
                        pressed = true
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        currentClick()
                        tryAwaitRelease()
                        pressed = false
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

// ── Раскладки ──────────────────────────────────────────────────

/* ============================================================
   GLIDE OVERLAY — детектор непрерывного ввода (swipe-typing)
   Слой над letter rows, ловит pointer events ПЕРВЫМ (PointerEventPass.Initial)
   - Если палец прошёл < 60dp за всё время → пропускает events детям (обычный tap)
   - Если > 60dp пути → активирует glide mode, consume'ит события
   - На UP в glide mode: матчит slow к словарю → вставляет слово
   ============================================================ */
@Composable
private fun GlideOverlay(
    keyPositions: androidx.compose.runtime.snapshots.SnapshotStateMap<
        String, androidx.compose.ui.geometry.Rect>,
    glideDictionary: List<String>,
    userWordFreq: Map<String, Int>,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    isLetterLayout: Boolean,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    // v1.25.8: threshold снижен 60dp → 35dp — glide активируется быстрее
    val glideThresholdPx = remember { with(density) { 25.dp.toPx() } }
    val currentValue by rememberUpdatedState(value)
    val currentOnChange by rememberUpdatedState(onValueChange)
    val currentDict by rememberUpdatedState(glideDictionary)
    val currentFreq by rememberUpdatedState(userWordFreq)
    val currentKeyPositions by rememberUpdatedState(keyPositions)

    // v1.25.2: visual trail — точки текущего glide рисуются Canvas-полилинией
    val trailPoints = remember { mutableStateListOf<androidx.compose.ui.geometry.Offset>() }
    var gliding by remember { mutableStateOf(false) }

    // v1.25.8 КРИТИЧНЫЙ FIX: trace.position в Local-координатах GlideOverlay,
    // а keyPositions.boundsInRoot() — в Root. Без offset они НЕ совпадают →
    // traceToLetters возвращал мусор → glide НЕ работал.
    var overlayRootOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    Box(
        modifier = Modifier
            .onGloballyPositioned { coords ->
                overlayRootOffset = coords.positionInRoot()
            }
            .pointerInput(Unit) {
            if (!isLetterLayout || currentDict.isEmpty()) return@pointerInput
            awaitPointerEventScope {
                while (true) {
                    val downEvent = awaitPointerEvent(PointerEventPass.Initial)
                    val downChange = downEvent.changes.firstOrNull { it.changedToDown() }
                        ?: continue
                    val rootStart = downChange.position
                    val trace = mutableListOf<androidx.compose.ui.geometry.Offset>()
                    trace.add(rootStart)
                    var glideActivated = false
                    // v1.25.2: snapshot value ПЕРЕД первым тапом KeyButton.
                    // Если glide завершится успешно → откатим к этому состоянию
                    // и вставим matched word (rollback "accidental" tap).
                    val valueAtDown = currentValue

                    while (true) {
                        val ev = awaitPointerEvent(PointerEventPass.Initial)
                        val ch = ev.changes.firstOrNull() ?: break
                        if (!ch.pressed) {
                            if (glideActivated) {
                                val letters = traceToLetters(
                                    trace, currentKeyPositions, overlayRootOffset,
                                )
                                val deduped = GlideMatcher.dedupeConsecutive(letters)
                                val matched = GlideMatcher.matchBestWord(
                                    deduped, currentDict, currentFreq,
                                )
                                if (matched != null) {
                                    // v1.25.2 ROLLBACK: используем valueAtDown,
                                    // не currentValue. KeyButton мог вставить
                                    // случайную букву при DOWN — отбрасываем.
                                    val newText = valueAtDown.text + matched + " "
                                    currentOnChange(
                                        TextFieldValue(
                                            newText,
                                            androidx.compose.ui.text.TextRange(newText.length),
                                        )
                                    )
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                ch.consume()
                            }
                            gliding = false
                            trailPoints.clear()
                            break
                        }
                        trace.add(ch.position)
                        if (!glideActivated) {
                            val dx = ch.position.x - rootStart.x
                            val dy = ch.position.y - rootStart.y
                            val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                            if (dist > glideThresholdPx) {
                                glideActivated = true
                                gliding = true
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                        if (glideActivated) {
                            trailPoints.add(ch.position)
                            // Ограничим длину trail чтобы не разрастался
                            if (trailPoints.size > 60) trailPoints.removeAt(0)
                            ch.consume()
                        }
                    }
                }
            }
        },
    ) {
        content()
        // v1.25.2: визуальный trail поверх клавиш (только во время glide)
        if (gliding && trailPoints.isNotEmpty()) {
            val accent = Color(0xFFFF8A3D)
            androidx.compose.foundation.Canvas(
                modifier = Modifier.matchParentSize(),
            ) {
                for (i in 1 until trailPoints.size) {
                    val from = trailPoints[i - 1]
                    val to = trailPoints[i]
                    val alpha = (i.toFloat() / trailPoints.size).coerceIn(0.15f, 0.8f)
                    drawLine(
                        color = accent.copy(alpha = alpha),
                        start = from,
                        end = to,
                        strokeWidth = 6f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    )
                }
            }
        }
    }
}

/**
 * Из последовательности точек palet'а — последовательность букв.
 * Snap каждой точки к ближайшей клавише по координатам.
 */
private fun traceToLetters(
    trace: List<androidx.compose.ui.geometry.Offset>,
    keyPositions: Map<String, androidx.compose.ui.geometry.Rect>,
    overlayRootOffset: androidx.compose.ui.geometry.Offset,
): List<Char> {
    if (keyPositions.isEmpty()) return emptyList()
    val letters = mutableListOf<Char>()
    // v1.25.8: КРИТИЧНЫЙ FIX. Конвертируем local-trace coords → root coords
    // через сложение с overlayRootOffset. Без этого snap-to-key возвращал
    // ближайшую клавишу относительно неправильного origin → мусор.
    for (pt in trace) {
        val rootX = pt.x + overlayRootOffset.x
        val rootY = pt.y + overlayRootOffset.y
        var bestKey: String? = null
        var bestDist = Float.MAX_VALUE
        for ((k, rect) in keyPositions) {
            val cx = rect.center.x
            val cy = rect.center.y
            val dx = rootX - cx
            val dy = rootY - cy
            val d = dx * dx + dy * dy
            if (d < bestDist) {
                bestDist = d
                bestKey = k
            }
        }
        bestKey?.firstOrNull()?.lowercaseChar()?.let { letters.add(it) }
    }
    return letters
}

/* ============================================================
   GLOBE KEY — tap = цикл ES↔RU, long-press = меню всех раскладок
   ============================================================ */
@Composable
private fun GlobeKey(
    bg: Color,
    textColor: Color,
    accent: Color,
    currentLayout: KbLayout,
    onShortCycle: () -> Unit,
    onPickLayout: (KbLayout) -> Unit,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    modifier: Modifier = Modifier,
) {
    var pressed by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val bgColor by animateColorAsState(
        if (pressed) bg.copy(alpha = (bg.alpha * 0.7f).coerceAtLeast(0.4f)) else bg,
        animationSpec = tween(60),
        label = "globe_bg",
    )
    val currentCycle by rememberUpdatedState(onShortCycle)
    val currentPick by rememberUpdatedState(onPickLayout)

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(bgColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { _ ->
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { currentCycle() },
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showMenu = true
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Default.Language,
            contentDescription = "Раскладка",
            tint = textColor,
            modifier = Modifier.size(20.dp),
        )

        if (showMenu) {
            // Popup сверху, по центру над клавишей
            val provider = remember {
                object : androidx.compose.ui.window.PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: androidx.compose.ui.unit.IntRect,
                        windowSize: androidx.compose.ui.unit.IntSize,
                        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
                        popupContentSize: androidx.compose.ui.unit.IntSize,
                    ): androidx.compose.ui.unit.IntOffset {
                        val x = (anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2)
                            .coerceIn(8, (windowSize.width - popupContentSize.width - 8).coerceAtLeast(8))
                        val y = (anchorBounds.top - popupContentSize.height - 8).coerceAtLeast(8)
                        return androidx.compose.ui.unit.IntOffset(x, y)
                    }
                }
            }
            Popup(
                popupPositionProvider = provider,
                onDismissRequest = { showMenu = false },
                properties = PopupProperties(focusable = true),
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shadowElevation = 12.dp,
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        val items = listOf(
                            Triple(KbLayout.ES, "🇪🇸", "Español"),
                            Triple(KbLayout.RU, "🇷🇺", "Русский"),
                            Triple(KbLayout.NUM, "🔢", "Цифры"),
                        )
                        items.forEach { (lay, flag, label) ->
                            val isActive = lay == currentLayout
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isActive) accent.copy(alpha = 0.2f)
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        currentPick(lay)
                                        showMenu = false
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Text(flag, fontSize = 16.sp)
                                    Text(
                                        label,
                                        color = if (isActive) accent else textColor,
                                        fontSize = 14.sp,
                                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun esLetterRows(): List<List<KbKey>> = listOf(
    listOf("q","w","e","r","t","y","u","i","o","p").map {
        KbKey(it, accents = esAccents(it))
    },
    listOf("a","s","d","f","g","h","j","k","l","ñ").map {
        KbKey(it, accents = esAccents(it))
    },
    listOf("z","x","c","v","b","n","m").map { KbKey(it) },
)

private fun esAccents(letter: String): List<String> = when (letter) {
    "a" -> listOf("á","à","ä","â")
    "e" -> listOf("é","è","ë","ê")
    "i" -> listOf("í","ï","î")
    "o" -> listOf("ó","ò","ö","ô")
    "u" -> listOf("ú","ü","û")
    "n" -> listOf("ñ")
    else -> emptyList()
}

private fun ruLetterRows(): List<List<KbKey>> = listOf(
    listOf("й","ц","у","к","е","н","г","ш","щ","з","х","ъ").map { KbKey(it) },
    listOf("ф","ы","в","а","п","р","о","л","д","ж","э").map { KbKey(it) },
    listOf("я","ч","с","м","и","т","ь","б","ю").map { KbKey(it) },
)

private fun numRows(): List<List<KbKey>> = listOf(
    listOf("1","2","3","4","5","6","7","8","9","0").map { KbKey(it) },
    listOf("@","#","$","¿","¡","&","*","(",")","-").map { KbKey(it) },
    listOf("+","\"","'",":",";",",","/","!").map { KbKey(it) },
)

/**
 * v1.25.9: Применить spell-check autocorrect к слову которое только что
 * было завершено (юзер нажал space / точку / знак).
 *
 * Logic:
 *  1. Найти границы только что введённого слова перед триггер-символом
 *  2. SpellChecker.check(word, language, userFreq)
 *  3. Если SpellSuggestion возвращён → заменить слово в тексте
 *  4. Курсор остаётся в позиции после триггер-символа
 *
 * Если SpellChecker возвращает null → возвращаем value as-is.
 */
private fun applyAutocorrect(
    value: TextFieldValue,
    layout: KbLayout,
    userWordFreq: Map<String, Int>,
): TextFieldValue {
    val text = value.text
    val cursor = value.selection.start
    if (cursor < 2) return value  // слишком короткий контекст

    // Триггер-символ (space/./,/!/?) уже в позиции cursor-1.
    // Слово — между предыдущим word-boundary и (cursor-1).
    val triggerPos = cursor - 1
    val triggerChar = text[triggerPos]
    if (!triggerChar.let { it == ' ' || it == '.' || it == ',' || it == '!' || it == '?' }) {
        return value
    }
    // Найти начало слова
    var wordStart = triggerPos
    while (wordStart > 0 && text[wordStart - 1].isLetter()) wordStart--
    if (wordStart >= triggerPos) return value  // пустое слово

    val word = text.substring(wordStart, triggerPos)
    if (word.length < 3) return value

    val language = if (layout == KbLayout.RU) "RU" else "ES"
    val suggestion = SpellChecker.check(word, language, userWordFreq) ?: return value

    // Замена слова. Курсор смещается на разницу длин.
    val newWord = if (word.first().isUpperCase()) {
        suggestion.correction.replaceFirstChar { it.titlecase() }
    } else {
        suggestion.correction
    }
    val newText = text.substring(0, wordStart) + newWord + text.substring(triggerPos)
    val deltaLength = newWord.length - word.length
    val newCursor = cursor + deltaLength
    return TextFieldValue(newText, TextRange(newCursor))
}
