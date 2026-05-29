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
import androidx.compose.ui.zIndex
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    // v1.25.44: glideDictionary убран вместе с glide-typing.
    // userWordFreq оставлен — нужен для SpellChecker autocorrect.
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

            // v1.25.25: ещё компактнее (26→23dp) + подсказки крупнее (16→20sp).
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(23.dp),
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
                            // v1.25.38: цифры 38→34dp (синхронно с уменьшением букв 49→44).
                            heightDp = 34,
                            fontSp = 20,
                            onTap = emit,
                            haptic = haptic,
                        )
                    }
                }
            }

            // v1.25.44: glide-typing удалён (юзер: "убери это свайп вообще
            // и линию тоже"). 3 ряда букв теперь в обычном Column без overlay.
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
                    // 3-й ряд внутри glide-overlay: shift + буквы z-m + backspace
                    // v1.25.40: уже Shift/Backspace (юзер: "сделай не такими широкими").
                    //  RU mobile: spec=1.0 — уже = ширине буквы, дальше уменьшать нельзя
                    //  ES:        spec=1.2 — было 1.5, теперь чуть шире буквы (на ~20%).
                    //             Row 3 letters получаются на ~6% шире row 1-2 letters
                    //             (что заметно меньше чем при 1.5 — там были той же ширины
                    //             но Shift/Backspace в 1.5× выглядели массивно).
                    val specialWeight = if (layout == KbLayout.RU) 1f else 1.2f
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
                                modifier = Modifier.weight(specialWeight),
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
                                registerPositionKey = key.label,
                                keyPositions = keyPositions,
                            )
                        }
                        BackspaceKey(
                            bg = specialKeyBg,
                            textColor = textColor,
                            accent = accent,
                            modifier = Modifier.weight(specialWeight),
                            onCharDelete = { onValueChange(KeyboardLogic.backspaceChar(value)) },
                            haptic = haptic,
                        )
                    }
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
                        // v1.25.41: 13→17sp (юзер: "тут слишком мелко"). Близко к
                        // буквенным 23sp но достаточно тонко чтобы вместить 3 символа.
                        fontSize = 17.sp,
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
                // v1.25.16: bottom-right ВСЕГДА Enter (newline), не Send.
                // Send только на input pill справа — двух кнопок отправки нет.
                SpecialKey(
                    bg = specialKeyBg,
                    modifier = Modifier.weight(1.6f),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        emit("\n")
                    },
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardReturn,
                        contentDescription = "Перенос строки",
                        tint = textColor,
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
    // v1.25.25: ещё компактнее (26→23dp) + текст 20sp SemiBold.
    Row(
        modifier = Modifier.fillMaxWidth().height(23.dp),
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
                Text(sug, color = textColor, fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold, maxLines = 1)
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
    // v1.25.38: высота 49→44dp (юзер: "у самсунга меньше"). 44dp = iOS
    // minimum touch target (44pt), визуально ближе к Samsung'у. Чуть ниже
    // Material 48dp guideline — компромисс ради proportional aspect ratio.
    heightDp: Int = 44,
    fontSp: Int = 23,
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

    // v1.25.13: auto-repeat для букв УБРАН. Раньше задерживал glide:
    // палец проходит через клавишу → задерживается на 400ms → срабатывает
    // auto-repeat и вставляются повторы (видно как "hhhhhh..."), всё это
    // ломало glide-typing. Letters auto-repeat ОЧЕНЬ редко нужен в UX.
    // Backspace и numbers могут иметь — но это в отдельных композаблах.

    // v1.24.12: единый pointer flow для continuous accent gesture.
    // hold → popup → slide без отрыва → release на нужном accent.
    // awaitPointerEventScope даёт fine-grained контроль над событиями.
    val density = LocalDensity.current
    val accentKeyWidthPx = remember { with(density) { 48.dp.toPx() } }  // 44dp + 4dp gap
    val popupVerticalRangePx = remember { with(density) { (-72).dp.toPx() } }

    // v1.25.34: revert hit area hack — он разорвал визуальную однородность
    // (rows 1-2 без gap+inner padding vs row 3 с gap без padding). Юзер: "в
    // третьем ряду чёрт пойми что". Визуал сейчас как Samsung — все рядки
    // одинаковые. Hit area expansion отложен (искать решение без visual cost).
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
                // v1.25.26 CRITICAL FIX: long-press теперь через независимый
                // launch + delay. Раньше проверялось `elapsed > 200ms` ВНУТРИ
                // event loop'а — но awaitPointerEvent() возвращается ТОЛЬКО
                // когда приходит event. Если палец стоит неподвижно — никаких
                // events → проверка не срабатывает → picker не появляется.
                // Это объясняло баг "то работает то нет, нужно жать с силой".
                // Теперь таймер крутится в отдельном launch — не зависит от
                // того дёргается палец или нет.
                kotlinx.coroutines.coroutineScope {
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitFirstDown()
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            // Fire on DOWN — Gboard стиль
                            if (currentAccents.isEmpty()) {
                                currentOnTap(currentOutput)
                            }
                            pressed = true
                            var enteredAccentMode = false

                            // Запускаем независимый long-press таймер.
                            // v1.25.43: 200→350ms (iOS standard, даёт palcu время
                            // на начало glide без срабатывания picker'а).
                            val longPressJob = this@coroutineScope.launch {
                                kotlinx.coroutines.delay(350)
                                if (currentAccents.isNotEmpty()) {
                                    enteredAccentMode = true
                                    showAccents = true
                                    hoveredAccentIdx = 0
                                    haptic.performHapticFeedback(
                                        HapticFeedbackType.LongPress
                                    )
                                }
                            }

                            // v1.25.42 + v1.25.43:
                            //  - 8dp → cancel long-press timer (если ещё не выстрелил)
                            //  - 25dp → force-dismiss picker если уже показался + reset state
                            val movementCancelPx = with(density) { 8.dp.toPx() }
                            val glideForceDismissPx = with(density) { 25.dp.toPx() }
                            val downPos = down.position

                            try {
                                while (true) {
                                    val event = androidx.compose.ui.input.pointer.PointerEventPass.Main
                                        .let { awaitPointerEvent(it) }
                                    val change = event.changes.firstOrNull() ?: break

                                    // v1.25.42/43: cancel или force-dismiss по дистанции
                                    if (change.pressed) {
                                        val dx = change.position.x - downPos.x
                                        val dy = change.position.y - downPos.y
                                        val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                                        if (!enteredAccentMode && dist > movementCancelPx) {
                                            longPressJob.cancel()
                                        }
                                        // v1.25.43: даже если picker УЖЕ открылся, при
                                        // продолжающемся glide-движении (>25dp) принудительно
                                        // закрываем его и сбрасываем mode — события уйдут
                                        // в GlideOverlay.
                                        if (enteredAccentMode && dist > glideForceDismissPx) {
                                            enteredAccentMode = false
                                            showAccents = false
                                            hoveredAccentIdx = -1
                                        }
                                    }

                                    if (!change.pressed) {
                                        // UP — финализируем
                                        longPressJob.cancel()
                                        if (enteredAccentMode) {
                                            if (hoveredAccentIdx in currentAccents.indices) {
                                                val variant = currentAccents[hoveredAccentIdx]
                                                val out = if (label != label.lowercase())
                                                    variant.uppercase() else variant
                                                currentOnTap(out)
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        } else if (currentAccents.isNotEmpty()) {
                                            // обычный tap клавиши с accents
                                            currentOnTap(currentOutput)
                                        }
                                        pressed = false
                                        showAccents = false
                                        hoveredAccentIdx = -1
                                        break
                                    }

                                    if (enteredAccentMode && currentAccents.isNotEmpty()) {
                                        // Slide tracking в picker
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
                                longPressJob.cancel()
                                pressed = false
                                showAccents = false
                                hoveredAccentIdx = -1
                            }
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
        // v1.25.38: буква по центру + hint обратно в TOP-END угол (юзер вернул).
        // Шрифт Normal (не Medium) + hint мелкий и бледный — как у Samsung.
        Text(
            label,
            color = textColor,
            fontSize = fontSp.sp,
            fontWeight = FontWeight.Normal,
        )
        if (accents.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 2.dp, end = 4.dp),
                contentAlignment = Alignment.TopEnd,
            ) {
                // v1.25.39: hint крупнее (10→12sp) + alpha 0.5→0.7 + Medium weight.
                // Юзер: "сделай чуть крупнее и заметнее".
                Text(
                    accents.first(),
                    color = textColor.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
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
            .height(44.dp)  // v1.25.38: 49→44dp ближе к Samsung
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
            .height(44.dp)  // v1.25.38: 49→44dp ближе к Samsung
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
            .height(44.dp)  // v1.25.38: 49→44dp ближе к Samsung
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

// v1.25.44: GlideOverlay + traceToLetters удалены (юзер: "убери это
// свайп вообще и линию тоже"). Glide-typing был экспериментальной
// фичей, конфликтовал с long-press для спец-символов и постоянно
// требовал тюнинга. Убран для стабильности и предсказуемого UX.
//
// Что заменяет glide для быстрого ввода:
//  - prefix-based suggestions (3 чипа над клавой) с bigram-усилением
//  - SpellChecker autocorrect on space
//  - long-press для спец-символов


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
            .height(44.dp)  // v1.25.38: 49→44dp ближе к Samsung
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

// v1.25.20: Samsung-style accent hints на ВСЕХ буквах.
// Раньше hint показывался только на гласных (a,e,i,o,u,n).
// Теперь каждая буква имеет либо испанский accent (a,e,i,o,u,n),
// либо спец-символ (+, ×, ÷, !, @, #, $, % и т.д.) доступный
// через long-press. Юзер видит маленькую подсказку в углу каждой
// клавиши — как на Samsung Keyboard.
private fun esLetterRows(): List<List<KbKey>> = listOf(
    listOf("q","w","e","r","t","y","u","i","o","p").map {
        KbKey(it, accents = esAccents(it))
    },
    listOf("a","s","d","f","g","h","j","k","l","ñ").map {
        KbKey(it, accents = esAccents(it))
    },
    listOf("z","x","c","v","b","n","m").map {
        KbKey(it, accents = esAccents(it))
    },
)

// v1.25.45: расположение спец-символов как у Samsung mobile ES.
// Юзер: "нужно как на скриншоте, это вариант и стандарт мировой".
//
// Для гласных и Ñ — испанский accent ПЕРВЫМ (Samsung на гласных тоже
// показывает accent-marker ´, мы показываем готовый é/á/í/ó/ú чтобы
// юзер видел useful char), Samsung-символ ВТОРЫМ.
// Для согласных — точно Samsung mapping.
private fun esAccents(letter: String): List<String> = when (letter) {
    // ── Row 1: Q W E R T Y U I O P ────────────────────
    "q" -> listOf("+")
    "w" -> listOf("×")
    "e" -> listOf("é","è","ë","ê")           // Samsung: ´
    "r" -> listOf("÷")
    "t" -> listOf("=")                          // было —
    "y" -> listOf("/")                          // было ~
    "u" -> listOf("ú","ü","û","<")           // Samsung: <
    "i" -> listOf("í","ï","î",">")           // Samsung: >
    "o" -> listOf("ó","ò","ö","ô","[")       // Samsung: [
    "p" -> listOf("]")                          // было [
    // ── Row 2: A S D F G H J K L Ñ ────────────────────
    "a" -> listOf("á","à","ä","â","!")       // Samsung: !
    "s" -> listOf("@")
    "d" -> listOf("#")
    "f" -> listOf("€")                          // было $ £ ¥
    "g" -> listOf("%")
    "h" -> listOf("^")                          // было \
    "j" -> listOf("&")
    "k" -> listOf("*")                          // было ( {
    "l" -> listOf("(")                          // было ) }
    "ñ" -> listOf(")")                          // было = ≠
    // ── Row 3: Z X C V B N M ──────────────────────────
    "z" -> listOf("\"")
    "x" -> listOf("'")
    "c" -> listOf(":")
    "v" -> listOf(";")
    "b" -> listOf("/")
    "n" -> listOf("ñ","?")                   // Samsung: ?
    "m" -> listOf("?")                          // было ! ¡
    else -> emptyList()
}

// v1.25.20: Samsung-style спец-символы на русской раскладке.
// v1.25.35: Mobile-layout — row 1 = 11 клавиш (без "ъ"), как у Samsung.
// "ъ" теперь доступен через long-press на "ь" (это стандарт мобильных
// клавиатур RU). Юзер: "у самсунга 11 кнопок, у нас 12 — компьютерная
// раскладка надо мобильную".
private fun ruLetterRows(): List<List<KbKey>> = listOf(
    listOf("й","ц","у","к","е","н","г","ш","щ","з","х").map {
        KbKey(it, accents = ruAccents(it))
    },
    listOf("ф","ы","в","а","п","р","о","л","д","ж","э").map {
        KbKey(it, accents = ruAccents(it))
    },
    listOf("я","ч","с","м","и","т","ь","б","ю").map {
        KbKey(it, accents = ruAccents(it))
    },
)

private fun ruAccents(letter: String): List<String> = when (letter) {
    // Row 1
    "й" -> listOf("+")
    "ц" -> listOf("×")
    "у" -> listOf("~")
    "к" -> listOf("÷")
    "е" -> listOf("ё","—")
    "н" -> listOf("—","–")
    "г" -> listOf("¨")
    "ш" -> listOf("^")
    "щ" -> listOf("[","{")
    "з" -> listOf("]","}")
    "х" -> listOf("§","№")
    // v1.25.35: "ъ" больше нет как отдельной клавиши — accent на "ь" (см. ниже)
    // Row 2
    "ф" -> listOf("!","¡")
    "ы" -> listOf("@")
    "в" -> listOf("#")
    "а" -> listOf("€")
    "п" -> listOf("%")
    "р" -> listOf("₽")
    "о" -> listOf("&")
    "л" -> listOf("*")
    "д" -> listOf("(","{")
    "ж" -> listOf(")","}")
    "э" -> listOf("=","≠")
    // Row 3
    "я" -> listOf("\"","«","»")
    "ч" -> listOf("'","`")
    "с" -> listOf("$")
    "м" -> listOf(":")
    "и" -> listOf(";")
    "т" -> listOf("/","\\")
    // v1.25.35: "ъ" ПЕРВЫМ (видно в hint, доступен тапом-удержанием —
    // как у Samsung mobile RU layout).
    "ь" -> listOf("ъ","?","¿")
    "б" -> listOf("<","≤")
    "ю" -> listOf(">","≥")
    else -> emptyList()
}

// v1.25.18: NUM-раскладка расширена — _, =, |, \\, [, ], {, },
// европейская валюта, типографские кавычки и др.
// Доступно через long-press accents (как у Samsung).
private fun numRows(): List<List<KbKey>> = listOf(
    listOf(
        KbKey("1", accents = listOf("!", "¹")),
        KbKey("2", accents = listOf("@", "²")),
        KbKey("3", accents = listOf("#", "³", "№")),
        KbKey("4", accents = listOf("$", "€", "₽")),
        KbKey("5", accents = listOf("%", "‰")),
        KbKey("6", accents = listOf("^", "<")),
        KbKey("7", accents = listOf("&", ">")),
        KbKey("8", accents = listOf("*", "°")),
        KbKey("9", accents = listOf("(", "[", "{")),
        KbKey("0", accents = listOf(")", "]", "}")),
    ),
    listOf(
        KbKey("@"),
        KbKey("#"),
        KbKey("$"),
        KbKey("_", accents = listOf("—", "–", "~")),
        KbKey("&"),
        KbKey("-", accents = listOf("—", "–")),
        KbKey("+", accents = listOf("±", "×", "÷")),
        KbKey("(", accents = listOf("[", "{")),
        KbKey(")", accents = listOf("]", "}")),
        KbKey("=", accents = listOf("≠", "≈")),
    ),
    listOf(
        KbKey("*"),
        KbKey("\"", accents = listOf("«", "»", "“", "”")),
        KbKey("'", accents = listOf("`", "‘", "’")),
        KbKey(":"),
        KbKey(";"),
        KbKey(",", accents = listOf("<")),
        KbKey(".", accents = listOf(">")),
        KbKey("/", accents = listOf("\\", "|")),
        KbKey("!", accents = listOf("¡")),
        KbKey("?", accents = listOf("¿")),
    ),
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
