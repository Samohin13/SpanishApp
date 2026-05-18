package com.spanishapp.ui.components

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

/**
 * v1.13.5: рендерит inline-markdown — пока только **bold**.
 *
 * Юзер увидел в теории/грамматике сырые звёздочки `**si**` потому что
 * Compose Text() рендерит их как литералы. Решение — парсим строку и
 * для текста между парой `**` применяем SpanStyle(FontWeight.Bold).
 *
 * Не используем стороннюю markdown-библиотеку: контент у нас простой
 * (только **bold**), доп зависимость 200KB не оправдана.
 *
 * ## Использование
 * ```kotlin
 * MarkdownText(
 *     text = "Это **важно** для **уровня A1**",
 *     fontSize = 16.sp,
 * )
 * ```
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    style: TextStyle = LocalTextStyleOrDefault(),
) {
    val annotated = remember(text) { parseInlineMarkdown(text) }
    Text(
        text = annotated,
        modifier = modifier,
        fontSize = fontSize,
        lineHeight = lineHeight,
        color = color,
        fontWeight = fontWeight,
        maxLines = maxLines,
        overflow = overflow,
        style = style,
    )
}

@Composable
private fun LocalTextStyleOrDefault(): TextStyle =
    androidx.compose.material3.LocalTextStyle.current

/**
 * Парсит строку: пары `**` → текст между ними получает Bold.
 * Несогласованные `**` (без пары) остаются как литералы.
 *
 * Алгоритм линейный O(n). Не поддерживает вложенность (она нам не нужна),
 * не поддерживает `_italic_` (контент не использует).
 */
internal fun parseInlineMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    val marker = "**"
    var i = 0
    while (i < text.length) {
        val open = text.indexOf(marker, i)
        if (open < 0) {
            append(text.substring(i))
            break
        }
        // Append plain text перед маркером
        if (open > i) append(text.substring(i, open))
        // Ищем закрывающую пару
        val close = text.indexOf(marker, open + marker.length)
        if (close < 0) {
            // Не нашли пару — литералим остаток как обычный текст
            append(text.substring(open))
            break
        }
        // Применяем Bold к контенту между **
        val boldContent = text.substring(open + marker.length, close)
        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
        append(boldContent)
        pop()
        i = close + marker.length
    }
}
