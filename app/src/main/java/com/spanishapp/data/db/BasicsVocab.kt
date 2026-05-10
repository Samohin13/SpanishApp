package com.spanishapp.data.db

import com.spanishapp.data.db.entity.WordEntity

/**
 * BasicsVocab — фундаментальные слова A1, отсутствовавшие в основном словаре:
 * личные местоимения, числа 1–20, вопросительные слова, базовые ответы (hola/sí/no).
 * Подключается в DatabaseSeeder.seedWords().
 */
object BasicsVocab {

    private fun w(es: String, ru: String, ex: String, level: String, cat: String, type: String = "noun") =
        WordEntity(spanish = es, russian = ru, example = ex, level = level, category = cat, wordType = type)

    val entries: List<WordEntity> = listOf(
        // ── Личные местоимения ──
        w("yo", "я", "Yo soy estudiante.", "A1", "personal", "pronoun"),
        w("tú", "ты", "¿Cómo te llamas tú?", "A1", "personal", "pronoun"),
        w("él", "он", "Él vive en Madrid.", "A1", "personal", "pronoun"),
        w("ella", "она", "Ella es mi hermana.", "A1", "personal", "pronoun"),
        w("nosotros", "мы (м.р.)", "Nosotros estudiamos español.", "A1", "personal", "pronoun"),
        w("nosotras", "мы (ж.р.)", "Nosotras somos amigas.", "A1", "personal", "pronoun"),
        w("vosotros", "вы (м.р., Испания)", "¿Vosotros venís?", "A1", "personal", "pronoun"),
        w("vosotras", "вы (ж.р., Испания)", "Vosotras sois inteligentes.", "A1", "personal", "pronoun"),
        w("ellos", "они (м.р.)", "Ellos juegan al fútbol.", "A1", "personal", "pronoun"),
        w("ellas", "они (ж.р.)", "Ellas cantan bien.", "A1", "personal", "pronoun"),
        w("usted", "вы (вежливо)", "¿Cómo está usted?", "A1", "personal", "pronoun"),
        w("ustedes", "вы (мн.ч.)", "Ustedes son bienvenidos.", "A1", "personal", "pronoun"),

        // ── Притяжательные ──
        w("mi", "мой/моя", "Mi casa es grande.", "A1", "personal", "pronoun"),
        w("tu", "твой/твоя", "Tu libro está aquí.", "A1", "personal", "pronoun"),
        w("su", "его/её/их/ваш", "Su coche es nuevo.", "A1", "personal", "pronoun"),
        w("nuestro", "наш", "Nuestro perro es fiel.", "A1", "personal", "pronoun"),

        // ── Числа 1–20 ──
        w("uno", "один", "Tengo uno solo.", "A1", "numeros", "number"),
        w("dos", "два", "Dos cafés, por favor.", "A1", "numeros", "number"),
        w("tres", "три", "Tres amigos vienen.", "A1", "numeros", "number"),
        w("cuatro", "четыре", "La mesa tiene cuatro patas.", "A1", "numeros", "number"),
        w("cinco", "пять", "Son las cinco.", "A1", "numeros", "number"),
        w("seis", "шесть", "Seis huevos en la caja.", "A1", "numeros", "number"),
        w("siete", "семь", "Siete días a la semana.", "A1", "numeros", "number"),
        w("ocho", "восемь", "Ocho horas de sueño.", "A1", "numeros", "number"),
        w("nueve", "девять", "Tengo nueve años.", "A1", "numeros", "number"),
        w("diez", "десять", "Diez minutos más.", "A1", "numeros", "number"),
        w("once", "одиннадцать", "Son las once.", "A1", "numeros", "number"),
        w("doce", "двенадцать", "Doce meses al año.", "A1", "numeros", "number"),
        w("trece", "тринадцать", "El número trece.", "A1", "numeros", "number"),
        w("catorce", "четырнадцать", "Catorce de febrero.", "A1", "numeros", "number"),
        w("quince", "пятнадцать", "Quince euros, por favor.", "A1", "numeros", "number"),
        w("dieciséis", "шестнадцать", "Tengo dieciséis años.", "A1", "numeros", "number"),
        w("diecisiete", "семнадцать", "Diecisiete grados afuera.", "A1", "numeros", "number"),
        w("dieciocho", "восемнадцать", "Dieciocho velas en la torta.", "A1", "numeros", "number"),
        w("diecinueve", "девятнадцать", "Diecinueve días de viaje.", "A1", "numeros", "number"),
        w("veinte", "двадцать", "Veinte estudiantes en clase.", "A1", "numeros", "number"),

        // ── Вопросительные слова ──
        w("qué", "что/какой", "¿Qué quieres?", "A1", "preguntas", "interrogative"),
        w("quién", "кто", "¿Quién es?", "A1", "preguntas", "interrogative"),
        w("dónde", "где", "¿Dónde vives?", "A1", "preguntas", "interrogative"),
        w("cuándo", "когда", "¿Cuándo llegas?", "A1", "preguntas", "interrogative"),
        w("cómo", "как", "¿Cómo estás?", "A1", "preguntas", "interrogative"),
        w("por qué", "почему", "¿Por qué lloras?", "A1", "preguntas", "interrogative"),
        w("cuál", "какой/который", "¿Cuál prefieres?", "A1", "preguntas", "interrogative"),
        w("cuánto", "сколько", "¿Cuánto cuesta?", "A1", "preguntas", "interrogative"),
        w("cuántos", "сколько (мн.ч.)", "¿Cuántos años tienes?", "A1", "preguntas", "interrogative"),

        // ── Базовые ответы / приветствия (отсутствовавшие) ──
        w("hola", "привет", "¡Hola! ¿Cómo estás?", "A1", "saludos", "phrase"),
        w("sí", "да", "Sí, claro.", "A1", "respuestas", "adverb"),
        w("no", "нет", "No, gracias.", "A1", "respuestas", "adverb")
    )
}
