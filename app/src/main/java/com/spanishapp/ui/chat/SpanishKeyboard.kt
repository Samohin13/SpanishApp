package com.spanishapp.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape

/**
 * Встроенная Compose-клавиатура для AI Chat.
 *
 * Заменяет системную клавиатуру полностью — никакого IME-инсета,
 * никакого Samsung emoji-toolbar'а, никаких проблем с layout.
 *
 * Раскладки:
 *  • ES (испанский QWERTY): q w e r t y u i o p / a s d f g h j k l ñ
 *    с диакритикой по long-press: a→á, e→é, i→í, o→ó, u→ú
 *  • RU (русский ЙЦУКЕН): й ц у к е н г ш щ з х ъ
 *  • NUM (цифры/знаки): 1-0, ¿ ¡ @ # $ & * ( ) и т.д.
 *
 * Shift, backspace, переключатель раскладки (🌐), пробел, send/enter
 * — в нижнем ряду.
 */

private enum class KbLayout { ES, RU, NUM }

private data class KbKey(
    val label: String,
    val output: String = label,
    val accents: List<String> = emptyList(),
    val weight: Float = 1f,
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun SpanishKeyboard(
    onKey: (String) -> Unit,
    onBackspace: () -> Unit,
    onSend: () -> Unit,
    canSend: Boolean,
    modifier: Modifier = Modifier,
) {
    var layout by remember { mutableStateOf(KbLayout.ES) }
    var shifted by remember { mutableStateOf(false) }

    val keyBg = MaterialTheme.colorScheme.surfaceContainerHighest
    val specialKeyBg = MaterialTheme.colorScheme.surfaceContainerHigh
    val textColor = MaterialTheme.colorScheme.onSurface
    val accent = Color(0xFFFF8A3D)
    val haptic = LocalHapticFeedback.current

    fun emit(s: String) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onKey(if (shifted && layout != KbLayout.NUM) s.uppercase() else s)
        if (shifted && layout != KbLayout.NUM) shifted = false  // one-shot shift
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
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Буквенные / цифровые ряды (первые 2)
            rows.take(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    row.forEach { key ->
                        KeyButton(
                            key = key,
                            shifted = shifted,
                            layout = layout,
                            bg = keyBg,
                            textColor = textColor,
                            modifier = Modifier.weight(key.weight),
                            onTap = { emit(it) },
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
                        bg = if (shifted) accent.copy(alpha = 0.25f) else specialKeyBg,
                        modifier = Modifier.weight(1.4f),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            shifted = !shifted
                        },
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "Shift",
                            tint = if (shifted) accent else textColor,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                rows[2].forEach { key ->
                    KeyButton(
                        key = key,
                        shifted = shifted,
                        layout = layout,
                        bg = keyBg,
                        textColor = textColor,
                        modifier = Modifier.weight(key.weight),
                        onTap = { emit(it) },
                    )
                }
                SpecialKey(
                    bg = specialKeyBg,
                    modifier = Modifier.weight(1.4f),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onBackspace()
                    },
                ) {
                    Icon(
                        Icons.Default.Backspace,
                        contentDescription = "Backspace",
                        tint = textColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // 4-й ряд: 123/ABC + globe + space + send
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
                    },
                ) {
                    Icon(
                        Icons.Default.Language,
                        contentDescription = "Раскладка",
                        tint = textColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
                // Пробел — крупный
                SpecialKey(
                    bg = keyBg,
                    modifier = Modifier.weight(5f),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onKey(" ")
                    },
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
                // . и , отдельно для удобства
                KeyButton(
                    key = KbKey(if (layout == KbLayout.NUM) "?" else "."),
                    shifted = false,
                    layout = layout,
                    bg = specialKeyBg,
                    textColor = textColor,
                    modifier = Modifier.weight(1f),
                    onTap = { emit(it) },
                )
                // Send/Enter
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
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KeyButton(
    key: KbKey,
    shifted: Boolean,
    layout: KbLayout,
    bg: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onTap: (String) -> Unit,
) {
    var showAccents by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val displayLabel = remember(key, shifted, layout) {
        if (shifted && layout != KbLayout.NUM) key.label.uppercase() else key.label
    }

    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .combinedClickable(
                onClick = { onTap(key.output) },
                onLongClick = if (key.accents.isNotEmpty()) {
                    {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showAccents = true
                    }
                } else null,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            displayLabel,
            color = textColor,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
        )

        if (showAccents && key.accents.isNotEmpty()) {
            Popup(
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
                            val out = if (shifted) variant.uppercase() else variant
                            Box(
                                modifier = Modifier
                                    .size(width = 38.dp, height = 42.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        RoundedCornerShape(6.dp),
                                    )
                                    .combinedClickable(
                                        onClick = {
                                            onTap(out)
                                            showAccents = false
                                        },
                                    ),
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

@Composable
private fun SpecialKey(
    bg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .height(42.dp)
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

@OptIn(ExperimentalFoundationApi::class)
private fun numRows(): List<List<KbKey>> = listOf(
    listOf("1","2","3","4","5","6","7","8","9","0").map { KbKey(it) },
    listOf("@","#","$","¿","¡","&","*","(",")","-").map { KbKey(it) },
    listOf("+","\"","'",":",";",",","/","!").map { KbKey(it) },
)
