package com.spanishapp.ui.chat

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Чистые функции редактирования текста, используемые SpanishKeyboard.
 * Выделены отдельно для unit-тестирования (без Compose runtime).
 *
 * Все функции возвращают новое [TextFieldValue] — иммутабельный паттерн.
 * Поддерживают курсор (TextRange.collapsed) и выделение (start != end).
 */
object KeyboardLogic {

    /** Вставить строку в позицию курсора. Заменяет выделение если оно есть. */
    fun insertAt(v: TextFieldValue, s: String): TextFieldValue {
        val t = v.text
        val sel = v.selection
        val newText = t.substring(0, sel.start) + s + t.substring(sel.end)
        return TextFieldValue(newText, TextRange(sel.start + s.length))
    }

    /** Удалить символ перед курсором, или выделение, или вернуть как есть если в начале. */
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

    /** Передвинуть курсор на delta символов влево/вправо. */
    fun moveCursor(v: TextFieldValue, delta: Int): TextFieldValue {
        val newPos = (v.selection.start + delta).coerceIn(0, v.text.length)
        return TextFieldValue(v.text, TextRange(newPos))
    }

    /**
     * Нужно ли авто-капитализировать следующую букву?
     * true:
     *  • курсор в начале текста
     *  • или предыдущие 2 символа = "[.!?] "
     */
    fun shouldAutoCapAfter(text: String, pos: Int): Boolean {
        if (pos == 0) return true
        val start = (pos - 2).coerceAtLeast(0)
        val tail = text.substring(start, pos)
        return tail.length >= 2 && tail[1] == ' ' && tail[0] in setOf('.', '!', '?')
    }

    /** Применить shift к строке если включён shift или caps-lock и слой буквенный. */
    fun applyShift(s: String, shifted: Boolean, capsLock: Boolean, isNumericLayout: Boolean): String {
        return if ((shifted || capsLock) && !isNumericLayout) s.uppercase() else s
    }

    /**
     * Можно ли применить double-space → period замену?
     * Условия: курсор collapsed, символ перед курсором — пробел, перед пробелом —
     * буква/цифра (не пробел / не знак), и пробел не является первым символом текста.
     * Это match'ит "abc " + space → "abc. ".
     */
    fun canDoubleSpacePeriod(v: TextFieldValue): Boolean {
        val t = v.text
        val pos = v.selection.start
        if (!v.selection.collapsed) return false
        if (pos < 2) return false
        if (t[pos - 1] != ' ') return false
        val prev = t[pos - 2]
        // Букву/цифру оставляем, знаки уже стоящие в конце (. , ! ?) пропускаем
        return prev.isLetterOrDigit()
    }

    /**
     * Заменяет trailing пробел перед курсором на ". " (точка + пробел).
     * Курсор передвигается на 1 позицию вперёд (т.к. был " ", стал ". ").
     */
    fun doubleSpaceToPeriod(v: TextFieldValue): TextFieldValue {
        val t = v.text
        val pos = v.selection.start
        // Заменяем символ позиции (pos-1) с " " на ". "
        val newText = t.substring(0, pos - 1) + ". " + t.substring(pos)
        return TextFieldValue(newText, TextRange(pos + 1))
    }

    // ── Раскладки (data only, без Compose) ──

    fun esRows(): List<List<String>> = listOf(
        listOf("q","w","e","r","t","y","u","i","o","p"),
        listOf("a","s","d","f","g","h","j","k","l","ñ"),
        listOf("z","x","c","v","b","n","m"),
    )

    fun ruRows(): List<List<String>> = listOf(
        listOf("й","ц","у","к","е","н","г","ш","щ","з","х","ъ"),
        listOf("ф","ы","в","а","п","р","о","л","д","ж","э"),
        listOf("я","ч","с","м","и","т","ь","б","ю"),
    )

    fun numRows(): List<List<String>> = listOf(
        listOf("1","2","3","4","5","6","7","8","9","0"),
        listOf("@","#","$","¿","¡","&","*","(",")","-"),
        listOf("+","\"","'",":",";",",","/","!"),
    )

    fun esAccents(letter: String): List<String> = when (letter) {
        "a" -> listOf("á","à","ä","â")
        "e" -> listOf("é","è","ë","ê")
        "i" -> listOf("í","ï","î")
        "o" -> listOf("ó","ò","ö","ô")
        "u" -> listOf("ú","ü","û")
        "n" -> listOf("ñ")
        else -> emptyList()
    }
}
