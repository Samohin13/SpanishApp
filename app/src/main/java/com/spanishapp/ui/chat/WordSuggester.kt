package com.spanishapp.ui.chat

/**
 * Простой prefix-based suggester для подсказок слов в клавиатуре.
 *
 * Без ML / dictionary download — встроенный список топ-частотных
 * испанских и русских слов. Покрывает основные слова которые
 * юзер пишет (приветствия, частые глаголы, базовая лексика).
 *
 * Используется в SpanishKeyboard: над клавой 3 чипа с лучшими
 * подсказками по началу текущего слова.
 */
object WordSuggester {

    /** Все слова словаря — для glide-typing matcher'а. */
    fun allWords(): List<String> = ES_WORDS + RU_WORDS

    /**
     * v1.25.10: подсказки берутся из ExpandedDictionary (~3500 слов),
     * не из базовых ES_WORDS/RU_WORDS (~400). Это значит при наборе
     * "при" Samsung-style сразу пять кандидатов: привет, приходить,
     * прикольно, приехать, приготовить.
     *
     * Сортировка: сначала наиболее частотные (топ списка), потом
     * остальные. Дубликаты исключены через distinct() в ExpandedDictionary.
     */
    fun suggest(input: String, max: Int = 3): List<String> {
        val word = currentWord(input).lowercase()
        if (word.isBlank()) return emptyList()
        val isRu = word.first() in 'а'..'я' || word.first() == 'ё'
        val pool = if (isRu) ExpandedDictionary.RU else ExpandedDictionary.ES
        return pool.asSequence()
            .filter { it.startsWith(word) && it != word }
            .take(max)
            .toList()
    }

    /** Заменить последнее слово на полное предложенное + пробел. */
    fun replaceLastWord(input: String, replacement: String): String {
        val lastSpace = input.lastIndexOfAny(charArrayOf(' ', '\n')) + 1
        return input.substring(0, lastSpace) + replacement + " "
    }

    private fun currentWord(input: String): String {
        if (input.isBlank()) return ""
        val lastSpace = input.lastIndexOfAny(charArrayOf(' ', '\n', '\t'))
        return input.substring(lastSpace + 1)
    }

    // ── Топ-частотные испанские (≈200 самых) ──
    private val ES_WORDS = listOf(
        "hola", "gracias", "por favor", "buenos días", "buenas tardes", "buenas noches",
        "adiós", "hasta luego", "hasta mañana", "sí", "no", "tal vez", "claro",
        "perdón", "lo siento", "de nada", "qué", "quién", "cuándo", "dónde", "cómo",
        "por qué", "cuál", "cuánto", "cuántos", "cuánta", "cuántas",
        "soy", "eres", "es", "somos", "sois", "son", "estoy", "estás", "está",
        "estamos", "estáis", "están", "tengo", "tienes", "tiene", "tenemos",
        "tenéis", "tienen", "quiero", "quieres", "quiere", "queremos", "queréis",
        "quieren", "puedo", "puedes", "puede", "podemos", "podéis", "pueden",
        "hablo", "hablas", "habla", "hablamos", "habláis", "hablan",
        "voy", "vas", "va", "vamos", "vais", "van", "hago", "haces", "hace",
        "hacemos", "hacéis", "hacen", "veo", "ves", "ve", "vemos", "veis", "ven",
        "como", "comes", "come", "comemos", "coméis", "comen", "bebo", "bebes",
        "bebe", "bebemos", "bebéis", "beben",
        "casa", "comida", "agua", "trabajo", "amigo", "amiga", "familia", "hijo",
        "hija", "madre", "padre", "hermano", "hermana", "novio", "novia",
        "tiempo", "día", "noche", "mañana", "tarde", "año", "mes", "semana",
        "hora", "minuto", "momento", "vez", "veces", "hoy", "ayer", "siempre",
        "nunca", "ahora", "después", "antes", "luego", "pronto", "tarde",
        "muy", "mucho", "poco", "bastante", "demasiado", "más", "menos", "tan",
        "bien", "mal", "mejor", "peor", "grande", "pequeño", "alto", "bajo",
        "bonito", "feo", "bueno", "malo", "nuevo", "viejo", "joven", "rápido",
        "lento", "fácil", "difícil", "importante", "interesante", "feliz",
        "triste", "cansado", "enfadado", "español", "inglés", "ruso", "alemán",
        "francés", "italiano", "ciudad", "país", "calle", "tienda", "restaurante",
        "hotel", "aeropuerto", "estación", "hospital", "escuela", "universidad",
        "trabajo", "oficina", "parque", "playa", "montaña", "mar", "río",
        "café", "té", "vino", "cerveza", "leche", "pan", "queso", "carne", "pescado",
        "fruta", "verdura", "pollo", "huevo", "arroz", "pasta",
        "necesito", "necesitas", "necesita", "creo", "crees", "cree", "pienso",
        "piensas", "piensa", "sé", "sabes", "sabe", "conozco", "conoces", "conoce",
        "entiendo", "entiendes", "entiende", "aprendo", "aprendes", "aprende",
        "estudio", "estudias", "estudia", "leo", "lees", "lee", "escribo",
        "escribes", "escribe", "escucho", "escuchas", "escucha", "miro", "miras",
        "mira", "encuentro", "encuentras", "encuentra", "busco", "buscas", "busca",
    )

    // ── Топ-частотные русские (≈200) ──
    private val RU_WORDS = listOf(
        "привет", "пока", "спасибо", "пожалуйста", "извини", "извините",
        "доброе утро", "добрый день", "добрый вечер", "доброй ночи",
        "да", "нет", "может быть", "конечно", "не знаю",
        "что", "кто", "когда", "где", "куда", "откуда", "как", "почему",
        "сколько", "какой", "какая", "какое", "какие",
        "я", "ты", "он", "она", "оно", "мы", "вы", "они",
        "мой", "твой", "его", "её", "наш", "ваш", "их",
        "хочу", "хочешь", "хочет", "хотим", "хотите", "хотят",
        "могу", "можешь", "может", "можем", "можете", "могут",
        "есть", "был", "была", "было", "были", "буду", "будешь", "будет",
        "будем", "будете", "будут",
        "иду", "идёшь", "идёт", "идём", "идёте", "идут",
        "говорю", "говоришь", "говорит", "говорим", "говорите", "говорят",
        "знаю", "знаешь", "знает", "знаем", "знаете", "знают",
        "думаю", "думаешь", "думает", "думаем", "думаете", "думают",
        "понимаю", "понимаешь", "понимает", "понимаем", "понимаете", "понимают",
        "вижу", "видишь", "видит", "видим", "видите", "видят",
        "делаю", "делаешь", "делает", "делаем", "делаете", "делают",
        "люблю", "любишь", "любит", "любим", "любите", "любят",
        "учу", "учишь", "учит", "учим", "учите", "учат",
        "читаю", "читаешь", "читает", "читаем", "читаете", "читают",
        "пишу", "пишешь", "пишет", "пишем", "пишете", "пишут",
        "слушаю", "слушаешь", "слушает", "слушаем", "слушаете", "слушают",
        "смотрю", "смотришь", "смотрит", "смотрим", "смотрите", "смотрят",
        "хорошо", "плохо", "лучше", "хуже", "большой", "маленький",
        "новый", "старый", "молодой", "быстрый", "медленный", "лёгкий",
        "трудный", "важный", "интересный", "красивый", "счастливый",
        "грустный", "уставший", "сердитый",
        "сегодня", "вчера", "завтра", "всегда", "никогда", "сейчас",
        "потом", "раньше", "позже", "скоро", "поздно",
        "очень", "много", "мало", "достаточно", "слишком", "более", "менее",
        "дом", "семья", "сын", "дочь", "мама", "папа", "брат", "сестра",
        "друг", "подруга", "работа", "школа", "университет", "город",
        "страна", "улица", "магазин", "ресторан", "кафе", "отель",
        "вода", "еда", "кофе", "чай", "хлеб", "молоко", "сыр", "мясо",
        "рыба", "фрукты", "овощи", "испанский", "английский", "русский",
        "учить", "понимать", "говорить", "слушать", "читать", "писать",
        "урок", "слово", "грамматика", "тест", "ответ", "вопрос",
    )
}
