package com.spanishapp.ui.chat

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
 * Профессиональная встроенная Compose-клавиатура.
 * v1.24.8: переписана для НАДЁЖНОСТИ — combinedClickable + InteractionSource
 * вместо хрупких detectTapGestures.
 *
 * Фичи:
 *  • Цифровой ряд 1-0 всегда сверху
 *  • Курсор + tap-to-position + long-press selection (через BasicTextField readOnly)
 *  • Swipe по space — двигает курсор
 *  • Long-press backspace — auto-repeat → word-delete
 *  • Long-press на любой клавише без accents — auto-repeat
 *  • Caps Lock = двойной тап Shift
 *  • Auto-capitalize первой буквы + после знаков . ! ?
 *  • Подсказки слов: 3 чипа над клавой
 *  • Popup акцентов СВЕРХУ над клавишей (long-press a/e/i/o/u)
 *  • Press feedback через MutableInteractionSource (нативно)
 *  • Свёртывание/разворачивание
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
    var shifted by remember { mutableStateOf(true) }     // auto-cap at start
    var capsLock by remember { mutableStateOf(false) }
    var collapsed by remember { mutableStateOf(false) }
    var lastShiftTap by remember { mutableStateOf(0L) }

    val haptic = LocalHapticFeedback.current

    val keyBg = MaterialTheme.colorScheme.surfaceContainerHighest
    val specialKeyBg = MaterialTheme.colorScheme.surfaceContainerHigh
    val textColor = MaterialTheme.colorScheme.onSurface
    val accent = Color(0xFFFF8A3D)

    // ── Авто-кап после знака ──
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

    fun backspaceWord(v: TextFieldValue): TextFieldValue {
        val t = v.text
        val sel = v.selection
        if (sel.start == 0 && sel.end == 0) return v
        var i = sel.start
        while (i > 0 && t[i - 1].isWhitespace()) i--
        while (i > 0 && !t[i - 1].isWhitespace()) i--
        return TextFieldValue(
            t.substring(0, i) + t.substring(sel.end),
            TextRange(i),
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

            // ── Подсказки ──
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

            // ── Цифровой ряд ──
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

            // ── Основные буквенные/цифровые ряды (первые 2) ──
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

            // ── 3-й ряд: shift + буквы + backspace ──
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
                    onWordDelete = { onValueChange(backspaceWord(value)) },
                    haptic = haptic,
                )
            }

            // ── 4-й ряд: 123 + globe + space (swipe!) + . + send ──
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
    accent: Color,
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
   KEY BUTTON — combinedClickable + InteractionSource (надёжно!)
   ============================================================ */
@OptIn(ExperimentalFoundationApi::class)
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val bgColor by animateColorAsState(
        targetValue = if (isPressed) accent.copy(alpha = 0.35f) else bg,
        label = "key_bg",
    )

    // Auto-repeat для клавиш без accents
    LaunchedEffect(isPressed, accents.isEmpty()) {
        if (isPressed && accents.isEmpty()) {
            delay(500)
            while (isPressed) {
                onTap(output)
                delay(60)
            }
        }
    }

    Box(
        modifier = modifier
            .height(heightDp.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(bgColor)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onTap(output) },
                onLongClick = if (accents.isNotEmpty()) {
                    {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showAccents = true
                    }
                } else null,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = textColor,
            fontSize = fontSp.sp,
            fontWeight = FontWeight.Medium,
        )

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
                                        // Применяем shift как label показывает
                                        val out = if (label != label.lowercase()) variant.uppercase() else variant
                                        onTap(out)
                                        showAccents = false
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    variant,
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
   BACKSPACE KEY — auto-repeat → word-delete (через InteractionSource)
   ============================================================ */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BackspaceKey(
    bg: Color,
    textColor: Color,
    accent: Color,
    modifier: Modifier = Modifier,
    onCharDelete: () -> Unit,
    onWordDelete: () -> Unit,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val bgColor by animateColorAsState(
        targetValue = if (isPressed) accent.copy(alpha = 0.35f) else bg,
        label = "bs_bg",
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(450)  // initial delay перед repeat
            var charCount = 0
            while (isPressed && charCount < 6) {
                onCharDelete()
                charCount++
                delay(70)
            }
            // word-delete пока зажато
            while (isPressed) {
                onWordDelete()
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                delay(180)
            }
        }
    }

    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(bgColor)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCharDelete()
                },
                onLongClick = {
                    // первое срабатывание long-press — uжe в LaunchedEffect через isPressed
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
            ),
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
    val pxPerChar = with(density) { 10.dp.toPx() }
    var accumPx by remember { mutableStateOf(0f) }
    var didSwipe by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val bgColor by animateColorAsState(
        if (isPressed) MaterialTheme.colorScheme.surfaceContainerHigh else bg,
        label = "space_bg",
    )

    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(bgColor)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        accumPx = 0f
                        didSwipe = false
                    },
                    onDragEnd = {
                        if (!didSwipe) onTap()
                        accumPx = 0f
                        didSwipe = false
                    },
                    onDragCancel = {
                        accumPx = 0f
                        didSwipe = false
                    },
                    onDrag = { _, drag ->
                        accumPx += drag.x
                        if (kotlin.math.abs(accumPx) > pxPerChar) {
                            val delta = (accumPx / pxPerChar).toInt()
                            accumPx -= delta * pxPerChar
                            onSwipe(delta)
                            didSwipe = true
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                if (!didSwipe) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTap()
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val bgColor by animateColorAsState(
        if (isPressed) bg.copy(alpha = (bg.alpha * 0.7f).coerceAtLeast(0.4f)) else bg,
        label = "special_bg",
    )
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
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
