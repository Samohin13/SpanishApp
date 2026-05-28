package com.spanishapp.ui.chat

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
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
    suggestions: List<String> = emptyList(),
    onPickSuggestion: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var layout by remember { mutableStateOf(KbLayout.ES) }
    var shifted by remember { mutableStateOf(true) }
    var capsLock by remember { mutableStateOf(false) }
    var collapsed by remember { mutableStateOf(false) }
    var lastShiftTap by remember { mutableStateOf(0L) }

    val haptic = LocalHapticFeedback.current
    val keyBg = MaterialTheme.colorScheme.surfaceContainerHighest
    val specialKeyBg = MaterialTheme.colorScheme.surfaceContainerHigh
    val textColor = MaterialTheme.colorScheme.onSurface
    val accent = Color(0xFFFF8A3D)

    fun shouldAutoCapAfter(text: String, pos: Int): Boolean {
        if (pos == 0) return true
        val tail = text.substring((pos - 2).coerceAtLeast(0), pos)
        return tail.length >= 2 && tail[1] == ' ' && tail[0] in setOf('.', '!', '?')
    }

    fun shiftedStr(s: String): String =
        if ((shifted || capsLock) && layout != KbLayout.NUM) s.uppercase() else s

    fun insertAt(v: TextFieldValue, s: String): TextFieldValue {
        val t = v.text
        val sel = v.selection
        val newText = t.substring(0, sel.start) + s + t.substring(sel.end)
        return TextFieldValue(newText, TextRange(sel.start + s.length))
    }

    fun backspaceChar(v: TextFieldValue): TextFieldValue {
        val t = v.text
        val sel = v.selection
        if (sel.start != sel.end) {
            return TextFieldValue(
                t.substring(0, sel.start) + t.substring(sel.end),
                TextRange(sel.start),
            )
        }
        if (sel.start == 0) return v
        return TextFieldValue(
            t.substring(0, sel.start - 1) + t.substring(sel.start),
            TextRange(sel.start - 1),
        )
    }

    fun moveCursor(v: TextFieldValue, delta: Int): TextFieldValue {
        val newPos = (v.selection.start + delta).coerceIn(0, v.text.length)
        return TextFieldValue(v.text, TextRange(newPos))
    }

    val emit: (String) -> Unit = { s ->
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        val newValue = insertAt(value, shiftedStr(s))
        onValueChange(newValue)
        if (shifted && !capsLock) shifted = false
        if (!capsLock && layout != KbLayout.NUM) {
            if (shouldAutoCapAfter(newValue.text, newValue.selection.start)) shifted = true
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
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        collapsed = !collapsed
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (collapsed) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (collapsed) "Развернуть" else "Свернуть",
                    tint = textColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp),
                )
            }

            if (collapsed) {
                Spacer(Modifier.height(4.dp))
                return@Column
            }

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

            // Цифровой ряд
            if (layout != KbLayout.NUM) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                            heightDp = 40,
                            fontSp = 16,
                            onTap = emit,
                            haptic = haptic,
                        )
                    }
                }
            }

            // Основные буквенные ряды
            rows.take(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                        )
                    }
                }
            }

            // 3-й ряд: shift + буквы + backspace
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                    )
                }
                BackspaceKey(
                    bg = specialKeyBg,
                    textColor = textColor,
                    accent = accent,
                    modifier = Modifier.weight(1.4f),
                    onCharDelete = { onValueChange(backspaceChar(value)) },
                    haptic = haptic,
                )
            }

            // 4-й ряд: 123 + globe + space (swipe) + . + send
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                SpecialKey(
                    bg = specialKeyBg,
                    modifier = Modifier.weight(1.4f),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        layout = if (layout == KbLayout.NUM) KbLayout.ES else KbLayout.NUM
                        shifted = false
                        capsLock = false
                    },
                ) {
                    Text(
                        if (layout == KbLayout.NUM) "ABC" else "123",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = textColor,
                    )
                }
                SpecialKey(
                    bg = specialKeyBg,
                    modifier = Modifier.weight(1.1f),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        layout = when (layout) {
                            KbLayout.ES -> KbLayout.RU
                            KbLayout.RU -> KbLayout.ES
                            KbLayout.NUM -> KbLayout.RU
                        }
                        shifted = false
                        capsLock = false
                    },
                ) {
                    Icon(
                        Icons.Default.Language,
                        contentDescription = "Раскладка",
                        tint = textColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
                SpaceKey(
                    layout = layout,
                    bg = keyBg,
                    textColor = textColor,
                    haptic = haptic,
                    modifier = Modifier.weight(5f),
                    onTap = { emit(" ") },
                    onSwipe = { delta -> onValueChange(moveCursor(value, delta)) },
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
            Spacer(Modifier.height(4.dp))
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
        modifier = Modifier.fillMaxWidth().height(40.dp),
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
    heightDp: Int = 50,
    fontSp: Int = 19,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    onTap: (String) -> Unit,
) {
    var showAccents by remember { mutableStateOf(false) }
    var pressed by remember { mutableStateOf(false) }
    val bgColor by animateColorAsState(
        targetValue = if (pressed) accent.copy(alpha = 0.35f) else bg,
        animationSpec = tween(60),
        label = "key_bg",
    )

    // v1.24.10: rememberUpdatedState — закрепляет ссылки на актуальные
    // версии callbacks/values. pointerInput(Unit) теперь стабильна — НЕ
    // пересоздаётся при изменении shifted/state, и захватывает СВЕЖИЙ onTap
    // через эту ссылку. Это и есть фикс "печать стопится через 3 символа":
    // раньше pointerInput захватывал старый onTap closure → emit вызывал
    // устаревший value/state → новые буквы терялись.
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

    Box(
        modifier = modifier
            .height(heightDp.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(bgColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    // КЛЮЧЕВОЕ: onPress срабатывает на DOWN — мгновенно!
                    onPress = { _ ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        currentOnTap(currentOutput)  // <-- fire immediately!
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    // Long-press — показ accents (если есть). Не вмешивается в onPress.
                    onLongPress = {
                        if (currentAccents.isNotEmpty()) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showAccents = true
                        }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = textColor, fontSize = fontSp.sp, fontWeight = FontWeight.Medium)

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
            Popup(
                popupPositionProvider = aboveProvider,
                onDismissRequest = { showAccents = false },
                properties = PopupProperties(focusable = true),
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shadowElevation = 8.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        accents.forEach { variant ->
                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 50.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        RoundedCornerShape(6.dp),
                                    )
                                    .clickable {
                                        val out = if (label != label.lowercase())
                                            variant.uppercase() else variant
                                        onTap(out)
                                        showAccents = false
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(variant, color = textColor, fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold)
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
            .height(50.dp)
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

    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(bgColor)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        pressed = true
                        accumPx = 0f
                        didSwipe = false
                    },
                    onDragEnd = {
                        if (!didSwipe) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            currentTap()
                        }
                        pressed = false
                        accumPx = 0f
                        didSwipe = false
                    },
                    onDragCancel = {
                        pressed = false
                        accumPx = 0f
                        didSwipe = false
                    },
                    onDrag = { _, drag ->
                        accumPx += drag.x
                        if (kotlin.math.abs(accumPx) > pxPerChar) {
                            val delta = (accumPx / pxPerChar).toInt()
                            accumPx -= delta * pxPerChar
                            currentSwipe(delta)
                            didSwipe = true
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { _ ->
                        pressed = true
                        if (!didSwipe) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            currentTap()
                        }
                        tryAwaitRelease()
                        pressed = false
                    },
                )
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
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(bgColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { _ ->
                        pressed = true
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
