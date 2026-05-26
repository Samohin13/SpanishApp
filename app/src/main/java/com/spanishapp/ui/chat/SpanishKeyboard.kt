package com.spanishapp.ui.chat

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlinx.coroutines.launch

/**
 * Профессиональная Compose-клавиатура для AI Chat.
 * Уровень: Samsung S-flagship / iOS keyboard.
 *
 * Фичи (v1.24.6):
 *  • Цифровой ряд 1-0 всегда сверху (в ES/RU)
 *  • Курсор + выделение через readOnly BasicTextField
 *  • Swipe по space — двигает курсор влево/вправо
 *  • Long-press backspace — auto-repeat → word-delete
 *  • Long-press на любой клавише — auto-repeat
 *  • Caps lock через double-tap shift
 *  • Auto-capitalize первой буквы + после . ! ?
 *  • Auto-space после знака
 *  • Press-feedback (scale + tint)
 *  • Подсказки слов: 3 чипа над клавой по prefix
 *  • Свёртывание/разворачивание
 *  • Popup акцентов сверху над клавишей (long-press a/e/i/o/u)
 */

enum class KbLayout { ES, RU, NUM }

private data class KbKey(
    val label: String,
    val output: String = label,
    val accents: List<String> = emptyList(),
)

/**
 * Состояние клавиатуры передаётся через TextFieldValue (text + cursor + selection).
 * Все операции возвращают новое TextFieldValue — иммутабельный pattern.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
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
    var shifted by remember { mutableStateOf(true) }   // start with auto-cap
    var capsLock by remember { mutableStateOf(false) }
    var collapsed by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val keyBg = MaterialTheme.colorScheme.surfaceContainerHighest
    val specialKeyBg = MaterialTheme.colorScheme.surfaceContainerHigh
    val textColor = MaterialTheme.colorScheme.onSurface
    val accent = Color(0xFFFF8A3D)

    // ── Логика автокапитализации после знака ──
    fun shouldAutoCapAfter(text: String, pos: Int): Boolean {
        if (pos == 0) return true
        // Берём 2 символа до курсора
        val tail = text.substring((pos - 2).coerceAtLeast(0), pos)
        return tail.length >= 2 && tail[1] == ' ' && tail[0] in setOf('.', '!', '?')
    }

    // ── Применяем shift к строке если нужно ──
    fun shifted(s: String): String =
        if ((shifted || capsLock) && layout != KbLayout.NUM) s.uppercase() else s

    // ── Вставка символа в позицию курсора ──
    fun insertAt(v: TextFieldValue, s: String): TextFieldValue {
        val t = v.text
        val sel = v.selection
        val newText = t.substring(0, sel.start) + s + t.substring(sel.end)
        val newCursor = sel.start + s.length
        return TextFieldValue(newText, TextRange(newCursor))
    }

    // ── Backspace: удалить символ перед курсором или выделение ──
    fun backspaceChar(v: TextFieldValue): TextFieldValue {
        val t = v.text
        val sel = v.selection
        if (sel.start != sel.end) {
            // Удалить выделение
            val newText = t.substring(0, sel.start) + t.substring(sel.end)
            return TextFieldValue(newText, TextRange(sel.start))
        }
        if (sel.start == 0) return v
        val newText = t.substring(0, sel.start - 1) + t.substring(sel.start)
        return TextFieldValue(newText, TextRange(sel.start - 1))
    }

    // ── Backspace word: удалить слово перед курсором ──
    fun backspaceWord(v: TextFieldValue): TextFieldValue {
        val t = v.text
        val sel = v.selection
        if (sel.start == 0 && sel.end == 0) return v
        // Находим границу слова: пропускаем пробелы, потом не-пробелы
        var i = sel.start
        while (i > 0 && t[i - 1].isWhitespace()) i--
        while (i > 0 && !t[i - 1].isWhitespace()) i--
        val newText = t.substring(0, i) + t.substring(sel.end)
        return TextFieldValue(newText, TextRange(i))
    }

    // ── Двигаем курсор на delta ──
    fun moveCursor(v: TextFieldValue, delta: Int): TextFieldValue {
        val newPos = (v.selection.start + delta).coerceIn(0, v.text.length)
        return TextFieldValue(v.text, TextRange(newPos))
    }

    // ── Emit символа с учётом shift/auto-cap ──
    fun emit(s: String) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        val toInsert = shifted(s)
        val newValue = insertAt(value, toInsert)
        onValueChange(newValue)
        // Сбрасываем one-shot shift (caps lock остаётся)
        if (shifted && !capsLock) shifted = false
        // После пробела/знака — проверим нужна ли авто-капитализация для следующей
        if (!capsLock && layout != KbLayout.NUM) {
            val text = newValue.text
            val pos = newValue.selection.start
            if (shouldAutoCapAfter(text, pos)) shifted = true
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
            // ── Handle для сворачивания ──
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

            // ── Подсказки (3 чипа) ──
            if (suggestions.isNotEmpty()) {
                SuggestionStrip(
                    suggestions = suggestions.take(3),
                    onPick = { sug ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPickSuggestion(sug)
                    },
                    textColor = textColor,
                    accent = accent,
                )
            }

            // ── Цифровой ряд (всегда сверху в ES/RU) ──
            if (layout != KbLayout.NUM) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    listOf("1","2","3","4","5","6","7","8","9","0").forEach { d ->
                        KeyButton(
                            key = KbKey(d),
                            shifted = false,  // цифры не shifted
                            layout = layout,
                            bg = keyBg,
                            textColor = textColor.copy(alpha = 0.8f),
                            accent = accent,
                            haptic = haptic,
                            scope = scope,
                            modifier = Modifier.weight(1f),
                            heightDp = 38,  // цифровой ряд чуть тоньше
                            fontSp = 16,
                            onTap = { emit(it) },
                        )
                    }
                }
            }

            // ── Буквенные / NUM-ряды (первые 2) ──
            rows.take(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    row.forEach { key ->
                        KeyButton(
                            key = key,
                            shifted = shifted || capsLock,
                            layout = layout,
                            bg = keyBg,
                            textColor = textColor,
                            accent = accent,
                            haptic = haptic,
                            scope = scope,
                            modifier = Modifier.weight(1f),
                            onTap = { emit(it) },
                        )
                    }
                }
            }

            // ── 3-й ряд: shift + буквы + backspace ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (layout != KbLayout.NUM) {
                    // Shift / Caps Lock — двойной тап = caps lock
                    var lastShiftTap by remember { mutableStateOf(0L) }
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
                                // Double-tap → caps lock
                                capsLock = !capsLock
                                shifted = capsLock
                            } else {
                                if (capsLock) {
                                    capsLock = false
                                    shifted = false
                                } else {
                                    shifted = !shifted
                                }
                            }
                            lastShiftTap = now
                        },
                    ) {
                        Icon(
                            if (capsLock) Icons.Default.KeyboardCapslock
                            else Icons.Default.KeyboardArrowUp,
                            contentDescription = if (capsLock) "Caps Lock" else "Shift",
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
                        key = key,
                        shifted = shifted || capsLock,
                        layout = layout,
                        bg = keyBg,
                        textColor = textColor,
                        accent = accent,
                        haptic = haptic,
                        scope = scope,
                        modifier = Modifier.weight(1f),
                        onTap = { emit(it) },
                    )
                }
                // Backspace с long-press auto-repeat + word-delete
                BackspaceKey(
                    bg = specialKeyBg,
                    textColor = textColor,
                    haptic = haptic,
                    scope = scope,
                    modifier = Modifier.weight(1.4f),
                    onTapDelete = { onValueChange(backspaceChar(value)) },
                    onWordDelete = { onValueChange(backspaceWord(value)) },
                )
            }

            // ── 4-й ряд: 123/ABC + globe + space (swipe!) + send ──
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
                // Space с SWIPE cursor control (S-series signature)
                SpaceKey(
                    layout = layout,
                    bg = keyBg,
                    textColor = textColor,
                    haptic = haptic,
                    modifier = Modifier.weight(5f),
                    onTap = { emit(" ") },
                    onSwipe = { delta ->
                        // delta — пиксели сдвига. Переводим в позиции (примерно 1 char на 10dp)
                        onValueChange(moveCursor(value, delta))
                    },
                )
                // . или ?
                KeyButton(
                    key = KbKey(if (layout == KbLayout.NUM) "?" else "."),
                    shifted = false,
                    layout = layout,
                    bg = specialKeyBg,
                    textColor = textColor,
                    accent = accent,
                    haptic = haptic,
                    scope = scope,
                    modifier = Modifier.weight(1f),
                    onTap = { emit(it) },
                )
                // Send / Enter
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
                        contentDescription = if (canSend) "Отправить" else "Enter",
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
   SUGGESTION STRIP — три чипа над клавой
   ============================================================ */
@Composable
private fun SuggestionStrip(
    suggestions: List<String>,
    onPick: (String) -> Unit,
    textColor: Color,
    accent: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
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
                Text(
                    sug,
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
    }
}

/* ============================================================
   KEY BUTTON — с press-feedback (scale + tint) + auto-repeat
   ============================================================ */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KeyButton(
    key: KbKey,
    shifted: Boolean,
    layout: KbLayout,
    bg: Color,
    textColor: Color,
    accent: Color,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    scope: kotlinx.coroutines.CoroutineScope,
    modifier: Modifier = Modifier,
    heightDp: Int = 50,
    fontSp: Int = 19,
    onTap: (String) -> Unit,
) {
    var showAccents by remember { mutableStateOf(false) }
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        label = "press_scale",
    )

    val displayLabel = remember(key, shifted, layout) {
        if (shifted && layout != KbLayout.NUM) key.label.uppercase() else key.label
    }

    Box(
        modifier = modifier
            .height(heightDp.dp)
            .graphicsLayer { scaleX = pressScale; scaleY = pressScale }
            .clip(RoundedCornerShape(6.dp))
            .background(if (pressed) accent.copy(alpha = 0.25f) else bg)
            .pointerInput(key, shifted) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        val released = tryAwaitRelease()
                        pressed = false
                        if (released) onTap(key.output)
                    },
                    onLongPress = {
                        if (key.accents.isNotEmpty()) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showAccents = true
                        } else {
                            // Auto-repeat key
                            scope.launch {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                delay(300)
                                while (pressed) {
                                    onTap(key.output)
                                    delay(70)
                                }
                            }
                        }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            displayLabel,
            color = textColor,
            fontSize = fontSp.sp,
            fontWeight = FontWeight.Medium,
        )

        if (showAccents && key.accents.isNotEmpty()) {
            // Popup сверху над клавишей
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
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        key.accents.forEach { variant ->
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
                                        val out = if (shifted) variant.uppercase() else variant
                                        onTap(out)
                                        showAccents = false
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    if (shifted) variant.uppercase() else variant,
                                    color = textColor,
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
   BACKSPACE KEY — auto-repeat + word-delete на long-press
   ============================================================ */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BackspaceKey(
    bg: Color,
    textColor: Color,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    scope: kotlinx.coroutines.CoroutineScope,
    modifier: Modifier = Modifier,
    onTapDelete: () -> Unit,
    onWordDelete: () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        onTapDelete()  // первый символ сразу
                        val released = tryAwaitRelease()
                        pressed = false
                    },
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch {
                            // Сначала 5 символов с интервалом 80ms
                            var charCount = 0
                            while (pressed && charCount < 5) {
                                delay(80)
                                if (!pressed) break
                                onTapDelete()
                                charCount++
                            }
                            // Потом word-delete каждые 150ms
                            while (pressed) {
                                delay(150)
                                if (!pressed) break
                                onWordDelete()
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
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
   SPACE KEY — поддерживает swipe для перемещения курсора
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
    // Накопленный сдвиг в пикселях → конвертируем в позиции
    var accumPx by remember { mutableStateOf(0f) }
    val pxPerChar = with(density) { 10.dp.toPx() }
    var didSwipe by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        accumPx = 0f
                        didSwipe = false
                    },
                    onDragEnd = {
                        if (!didSwipe) onTap()  // если не свайпили — обычный тап = пробел
                        accumPx = 0f
                        didSwipe = false
                    },
                    onDragCancel = {
                        accumPx = 0f
                        didSwipe = false
                    },
                    onDrag = { _, dragAmount ->
                        // Только горизонтальный свайп
                        accumPx += dragAmount.x
                        if (kotlin.math.abs(accumPx) > pxPerChar) {
                            val delta = (accumPx / pxPerChar).toInt()
                            accumPx -= delta * pxPerChar
                            onSwipe(delta)
                            didSwipe = true
                        }
                    },
                )
            }
            .pointerInput(Unit) {
                // Отдельный детектор для простого тапа (без drag)
                detectTapGestures(onTap = {
                    if (!didSwipe) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onTap()
                    }
                })
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
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable(onClick = onClick),
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
