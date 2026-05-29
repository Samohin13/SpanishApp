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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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
 * v1.25.16: расширен с **bold** на 4 типа inline-форматирования:
 *  • `**bold**` → жирный
 *  • `_italic_` → курсив
 *  • `~strike~` → зачёркнутый
 *  • `` `mono` `` → моноширинный
 * Plus line-level: строки начинающиеся на `> ` → цитата (отступ + italic).
 *
 * Несогласованные маркеры остаются как литералы. Алгоритм линейный.
 */
internal fun parseInlineMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    // Сначала обрабатываем quote per-line, потом inline-маркеры
    val lines = text.split("\n")
    for ((idx, line) in lines.withIndex()) {
        if (line.startsWith("> ")) {
            // Quote: italic + индент + светлее
            pushStyle(SpanStyle(
                fontStyle = FontStyle.Italic,
                color = Color(0xFF9CA3AF),
            ))
            append("  ")
            renderInline(this, line.substring(2))
            pop()
        } else {
            renderInline(this, line)
        }
        if (idx < lines.size - 1) append("\n")
    }
}

private fun renderInline(builder: androidx.compose.ui.text.AnnotatedString.Builder, text: String) {
    var i = 0
    while (i < text.length) {
        // Жирный (** двойные звёздочки) — приоритет наивысший
        if (i + 1 < text.length && text[i] == '*' && text[i + 1] == '*') {
            val close = text.indexOf("**", i + 2)
            if (close > i + 2) {
                builder.pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                builder.append(text.substring(i + 2, close))
                builder.pop()
                i = close + 2
                continue
            }
        }
        // Моноширинный (`backtick`)
        if (text[i] == '`') {
            val close = text.indexOf('`', i + 1)
            if (close > i + 1) {
                builder.pushStyle(SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFFFB85C),
                ))
                builder.append(text.substring(i + 1, close))
                builder.pop()
                i = close + 1
                continue
            }
        }
        // Зачёркнутый (~tilde~)
        if (text[i] == '~') {
            val close = text.indexOf('~', i + 1)
            if (close > i + 1) {
                builder.pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                builder.append(text.substring(i + 1, close))
                builder.pop()
                i = close + 1
                continue
            }
        }
        // Курсив (_underscore_)
        if (text[i] == '_' &&
            (i == 0 || !text[i - 1].isLetterOrDigit())) {
            val close = text.indexOf('_', i + 1)
            if (close > i + 1 &&
                (close + 1 >= text.length || !text[close + 1].isLetterOrDigit())) {
                builder.pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                builder.append(text.substring(i + 1, close))
                builder.pop()
                i = close + 1
                continue
            }
        }
        builder.append(text[i])
        i++
    }
}
