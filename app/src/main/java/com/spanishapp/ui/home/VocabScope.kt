package com.spanishapp.ui.home

/**
 * Vocab Scope — концепция «все слова, доступные к текущему уроку».
 *
 * Введено в курсе v1.2.0 (Phase 0). Каждый урок объявляет, какие НОВЫЕ
 * слова он вводит. ExerciseGenerator при генерации фолбэк-упражнений
 * не имеет права использовать слова **за пределами scope** — иначе юзер
 * увидит незнакомое слово и решит что забыл.
 *
 * ## Кумулятивная семантика
 *
 * `wordsForLesson("u1_l5")` = объединение всех `newWords` от u1_l0 до u1_l5
 * включительно.
 *
 * ## Источник правды
 *
 * Маппинг строится здесь вручную — по столбцу «Vocab scope» из
 * `docs/curriculum/ESPEAK_Curriculum.xlsx`. По мере переписывания блоков
 * (Phase 1.1, 1.2, ...) этот объект наполняется.
 *
 * ## Использование
 *
 * ```kotlin
 * val scope = VocabScope.wordsForLesson("u1_l7")
 * val distractors = scope.shuffled().take(3)        // отвлекающие варианты
 * val pool = scope.filter { it.cefr == "A1" }       // словарный пул для MATCH_PAIRS
 * ```
 */
object VocabScope {

    /**
     * Слово в scope: испанская форма + русский перевод + lessonId-источник.
     * Хранится строкой а не WordEntity чтобы не зависеть от Room в чисто-данных тестах.
     */
    data class ScopeWord(
        val spanish: String,
        val russian: String,
        /** lessonId урока, в котором слово ВПЕРВЫЕ появилось. */
        val introducedIn: String,
        /** Категория: "greeting" / "number" / "noun_person" / etc. */
        val category: String = "general",
    )

    /**
     * Маппинг: lessonId → список НОВЫХ слов, введённых в этом уроке.
     * НЕ кумулятивный — только новые. Кумулятивную выборку даёт `wordsForLesson()`.
     *
     * Заполнение идёт блоками: блок 1.1 → 1.2 → ... По мере наполнения
     * `wordsForLesson` начнёт возвращать всё больше слов.
     */
    private val newWordsByLesson: Map<String, List<ScopeWord>> = buildMap {
        // ═══════════════════════════════════════════════════════════════
        // Блок 1.1 «Взлёт» — заполнен по xlsx
        // ═══════════════════════════════════════════════════════════════

        // u1_l0..u1_l3 — фонетика. Из примеров в теории слова попадают в scope:
        put("u1_l0", listOf(
            ScopeWord("casa", "дом", "u1_l0", "noun"),
            ScopeWord("mes", "месяц", "u1_l0", "noun"),
            ScopeWord("isla", "остров", "u1_l0", "noun"),
            ScopeWord("ojo", "глаз", "u1_l0", "noun"),
            ScopeWord("uva", "виноград", "u1_l0", "noun"),
        ))
        put("u1_l1", listOf(
            ScopeWord("vino", "вино", "u1_l1", "noun"),
            ScopeWord("gato", "кот", "u1_l1", "noun"),
            ScopeWord("gente", "люди", "u1_l1", "noun"),
            ScopeWord("cada", "каждый", "u1_l1", "adjective"),
        ))
        put("u1_l2", listOf(
            ScopeWord("hola", "привет", "u1_l2", "greeting"),
            ScopeWord("año", "год", "u1_l2", "noun"),
            ScopeWord("perro", "собака", "u1_l2", "noun"),
            ScopeWord("pero", "но", "u1_l2", "conjunction"),
            ScopeWord("Japón", "Япония", "u1_l2", "country"),
        ))
        put("u1_l3", listOf(
            ScopeWord("café", "кофе", "u1_l3", "noun"),
            ScopeWord("música", "музыка", "u1_l3", "noun"),
            ScopeWord("español", "испанский", "u1_l3", "adjective"),
            ScopeWord("ciudad", "город", "u1_l3", "noun"),
        ))
        // u1_l4 — Приветствия
        put("u1_l4", listOf(
            ScopeWord("buenos días", "доброе утро", "u1_l4", "greeting"),
            ScopeWord("buenas tardes", "добрый день", "u1_l4", "greeting"),
            ScopeWord("buenas noches", "добрый вечер", "u1_l4", "greeting"),
            ScopeWord("¿Cómo estás?", "как ты?", "u1_l4", "phrase"),
            ScopeWord("bien", "хорошо", "u1_l4", "adverb"),
            ScopeWord("gracias", "спасибо", "u1_l4", "phrase"),
        ))
        // u1_l5 — Прощания
        put("u1_l5", listOf(
            ScopeWord("adiós", "до свидания", "u1_l5", "greeting"),
            ScopeWord("hasta luego", "до скорого", "u1_l5", "phrase"),
            ScopeWord("hasta mañana", "до завтра", "u1_l5", "phrase"),
            ScopeWord("hasta pronto", "до встречи", "u1_l5", "phrase"),
            ScopeWord("chao", "пока", "u1_l5", "greeting"),
            ScopeWord("nos vemos", "увидимся", "u1_l5", "phrase"),
            ScopeWord("mañana", "завтра / утро", "u1_l5", "noun"),
            ScopeWord("María", "Мария", "u1_l5", "name"),
            ScopeWord("lunes", "понедельник", "u1_l5", "noun"),
        ))
        // u1_l6 — Вежливость
        put("u1_l6", listOf(
            ScopeWord("por favor", "пожалуйста", "u1_l6", "phrase"),
            ScopeWord("muchas gracias", "большое спасибо", "u1_l6", "phrase"),
            ScopeWord("de nada", "не за что", "u1_l6", "phrase"),
            ScopeWord("perdón", "извини", "u1_l6", "phrase"),
            ScopeWord("lo siento", "сожалею", "u1_l6", "phrase"),
            ScopeWord("disculpe", "извините", "u1_l6", "phrase"),
            ScopeWord("un café", "один кофе", "u1_l6", "phrase"),
        ))
        // u1_l7 — SER soy/eres/es
        put("u1_l7", listOf(
            ScopeWord("soy", "я есть", "u1_l7", "verb"),
            ScopeWord("eres", "ты есть", "u1_l7", "verb"),
            ScopeWord("es", "он/она есть", "u1_l7", "verb"),
            ScopeWord("ruso", "русский (м)", "u1_l7", "nationality"),
            ScopeWord("Pablo", "Павел", "u1_l7", "name"),
            ScopeWord("amigo", "друг", "u1_l7", "noun"),
            ScopeWord("alto", "высокий", "u1_l7", "adjective"),
            ScopeWord("médico", "врач (м)", "u1_l7", "profession"),
            ScopeWord("ingeniero", "инженер", "u1_l7", "profession"),
        ))
        // u1_l8 — SER somos/sois/son
        put("u1_l8", listOf(
            ScopeWord("somos", "мы есть", "u1_l8", "verb"),
            ScopeWord("sois", "вы есть (Исп.)", "u1_l8", "verb"),
            ScopeWord("son", "они есть", "u1_l8", "verb"),
            ScopeWord("amigos", "друзья", "u1_l8", "noun"),
            ScopeWord("estudiantes", "студенты", "u1_l8", "noun"),
            ScopeWord("amables", "любезные", "u1_l8", "adjective"),
            ScopeWord("aquí", "здесь", "u1_l8", "adverb"),
        ))
        // u1_l9 — Местоимения
        put("u1_l9", listOf(
            ScopeWord("yo", "я", "u1_l9", "pronoun"),
            ScopeWord("tú", "ты", "u1_l9", "pronoun"),
            ScopeWord("usted", "Вы (формально)", "u1_l9", "pronoun"),
            ScopeWord("él", "он", "u1_l9", "pronoun"),
            ScopeWord("ella", "она", "u1_l9", "pronoun"),
            ScopeWord("nosotros", "мы (м)", "u1_l9", "pronoun"),
            ScopeWord("nosotras", "мы (ж)", "u1_l9", "pronoun"),
            ScopeWord("vosotros", "вы (м, Исп.)", "u1_l9", "pronoun"),
            ScopeWord("ellos", "они (м)", "u1_l9", "pronoun"),
            ScopeWord("ellas", "они (ж)", "u1_l9", "pronoun"),
            ScopeWord("hermano", "брат", "u1_l9", "noun"),
            ScopeWord("rusas", "русские (ж)", "u1_l9", "nationality"),
        ))
        // u1_l10 — Род el/la
        put("u1_l10", listOf(
            ScopeWord("el", "м.артикль (опр.)", "u1_l10", "article"),
            ScopeWord("la", "ж.артикль (опр.)", "u1_l10", "article"),
            ScopeWord("libro", "книга", "u1_l10", "noun"),
            ScopeWord("día", "день", "u1_l10", "noun"),
            ScopeWord("mano", "рука", "u1_l10", "noun"),
            ScopeWord("médica", "врач (ж)", "u1_l10", "profession"),
        ))
        // u1_l11 — Артикли
        put("u1_l11", listOf(
            ScopeWord("un", "м.артикль (неопр.)", "u1_l11", "article"),
            ScopeWord("una", "ж.артикль (неопр.)", "u1_l11", "article"),
            ScopeWord("los", "м.мн.артикль (опр.)", "u1_l11", "article"),
            ScopeWord("las", "ж.мн.артикль (опр.)", "u1_l11", "article"),
            ScopeWord("unos", "м.мн.артикль (неопр.)", "u1_l11", "article"),
            ScopeWord("unas", "ж.мн.артикль (неопр.)", "u1_l11", "article"),
            ScopeWord("libros", "книги", "u1_l11", "noun"),
            ScopeWord("casas", "дома", "u1_l11", "noun"),
        ))
        // u1_l12 — Страны и национальности
        put("u1_l12", listOf(
            ScopeWord("Rusia", "Россия", "u1_l12", "country"),
            ScopeWord("España", "Испания", "u1_l12", "country"),
            ScopeWord("México", "Мексика", "u1_l12", "country"),
            ScopeWord("Francia", "Франция", "u1_l12", "country"),
            ScopeWord("Inglaterra", "Англия", "u1_l12", "country"),
            ScopeWord("rusa", "русская", "u1_l12", "nationality"),
            ScopeWord("española", "испанка", "u1_l12", "nationality"),
            ScopeWord("mexicano", "мексиканец", "u1_l12", "nationality"),
            ScopeWord("mexicana", "мексиканка", "u1_l12", "nationality"),
            ScopeWord("francés", "француз", "u1_l12", "nationality"),
            ScopeWord("francesa", "француженка", "u1_l12", "nationality"),
            ScopeWord("inglés", "англичанин", "u1_l12", "nationality"),
            ScopeWord("inglesa", "англичанка", "u1_l12", "nationality"),
            ScopeWord("de", "из / от", "u1_l12", "preposition"),
        ))
        // u1_l13 — Числа 0-10
        put("u1_l13", listOf(
            ScopeWord("cero", "ноль", "u1_l13", "number"),
            ScopeWord("uno", "один", "u1_l13", "number"),
            ScopeWord("dos", "два", "u1_l13", "number"),
            ScopeWord("tres", "три", "u1_l13", "number"),
            ScopeWord("cuatro", "четыре", "u1_l13", "number"),
            ScopeWord("cinco", "пять", "u1_l13", "number"),
            ScopeWord("seis", "шесть", "u1_l13", "number"),
            ScopeWord("siete", "семь", "u1_l13", "number"),
            ScopeWord("ocho", "восемь", "u1_l13", "number"),
            ScopeWord("nueve", "девять", "u1_l13", "number"),
            ScopeWord("diez", "десять", "u1_l13", "number"),
        ))
        // ═══════════════════════════════════════════════════════════════
        // Блок 1.2 «Мой мир» — числа 11-100, TENER, семья, цвета, ESTAR, дом
        // ═══════════════════════════════════════════════════════════════
        put("u2_l0", listOf(
            ScopeWord("once", "одиннадцать", "u2_l0", "number"),
            ScopeWord("doce", "двенадцать", "u2_l0", "number"),
            ScopeWord("trece", "тринадцать", "u2_l0", "number"),
            ScopeWord("catorce", "четырнадцать", "u2_l0", "number"),
            ScopeWord("quince", "пятнадцать", "u2_l0", "number"),
            ScopeWord("dieciséis", "шестнадцать", "u2_l0", "number"),
            ScopeWord("diecisiete", "семнадцать", "u2_l0", "number"),
            ScopeWord("dieciocho", "восемнадцать", "u2_l0", "number"),
            ScopeWord("diecinueve", "девятнадцать", "u2_l0", "number"),
            ScopeWord("veinte", "двадцать", "u2_l0", "number"),
            ScopeWord("euros", "евро", "u2_l0", "noun"),
            ScopeWord("años", "годы", "u2_l0", "noun"),
        ))
        put("u2_l1", listOf(
            ScopeWord("veintiuno", "21", "u2_l1", "number"),
            ScopeWord("veinticinco", "25", "u2_l1", "number"),
            ScopeWord("treinta", "30", "u2_l1", "number"),
            ScopeWord("cuarenta", "40", "u2_l1", "number"),
            ScopeWord("cincuenta", "50", "u2_l1", "number"),
            ScopeWord("sesenta", "60", "u2_l1", "number"),
            ScopeWord("setenta", "70", "u2_l1", "number"),
            ScopeWord("ochenta", "80", "u2_l1", "number"),
            ScopeWord("noventa", "90", "u2_l1", "number"),
            ScopeWord("cien", "100", "u2_l1", "number"),
            ScopeWord("y", "и", "u2_l1", "conjunction"),
        ))
        put("u2_l2", listOf(
            ScopeWord("tengo", "у меня есть", "u2_l2", "verb"),
            ScopeWord("tienes", "у тебя есть", "u2_l2", "verb"),
            ScopeWord("tiene", "у него/неё есть", "u2_l2", "verb"),
            ScopeWord("razón", "правота", "u2_l2", "noun"),
        ))
        put("u2_l3", listOf(
            ScopeWord("tenemos", "у нас есть", "u2_l3", "verb"),
            ScopeWord("tenéis", "у вас есть (Исп.)", "u2_l3", "verb"),
            ScopeWord("tienen", "у них есть", "u2_l3", "verb"),
            ScopeWord("hijas", "дочери", "u2_l3", "noun"),
        ))
        put("u2_l4", listOf(
            ScopeWord("padre", "отец", "u2_l4", "family"),
            ScopeWord("madre", "мать", "u2_l4", "family"),
            ScopeWord("hermana", "сестра", "u2_l4", "family"),
            ScopeWord("hija", "дочь", "u2_l4", "family"),
            ScopeWord("hermanos", "братья", "u2_l4", "family"),
            ScopeWord("padres", "родители", "u2_l4", "family"),
        ))
        put("u2_l5", listOf(
            ScopeWord("abuelo", "дед", "u2_l5", "family"),
            ScopeWord("abuela", "бабушка", "u2_l5", "family"),
            ScopeWord("tío", "дядя", "u2_l5", "family"),
            ScopeWord("tía", "тётя", "u2_l5", "family"),
            ScopeWord("primo", "двоюр.брат", "u2_l5", "family"),
            ScopeWord("prima", "двоюр.сестра", "u2_l5", "family"),
            ScopeWord("sobrino", "племянник", "u2_l5", "family"),
            ScopeWord("sobrina", "племянница", "u2_l5", "family"),
            ScopeWord("nieto", "внук", "u2_l5", "family"),
            ScopeWord("nieta", "внучка", "u2_l5", "family"),
            ScopeWord("Carmen", "Кармен", "u2_l5", "name"),
            ScopeWord("tíos", "дяди", "u2_l5", "family"),
            ScopeWord("Madrid", "Мадрид", "u2_l5", "city"),
        ))
        put("u2_l6", listOf(
            ScopeWord("mi", "мой/моя", "u2_l6", "possessive"),
            ScopeWord("mis", "мои", "u2_l6", "possessive"),
            ScopeWord("tu", "твой/твоя", "u2_l6", "possessive"),
            ScopeWord("su", "его/её/Ваш", "u2_l6", "possessive"),
            ScopeWord("nuestro", "наш", "u2_l6", "possessive"),
            ScopeWord("nuestra", "наша", "u2_l6", "possessive"),
            ScopeWord("vuestro", "ваш (Исп.)", "u2_l6", "possessive"),
        ))
        put("u2_l7", listOf(
            ScopeWord("rojo", "красный", "u2_l7", "color"),
            ScopeWord("azul", "синий", "u2_l7", "color"),
            ScopeWord("verde", "зелёный", "u2_l7", "color"),
            ScopeWord("amarillo", "жёлтый", "u2_l7", "color"),
            ScopeWord("negro", "чёрный", "u2_l7", "color"),
            ScopeWord("blanco", "белый", "u2_l7", "color"),
            ScopeWord("gris", "серый", "u2_l7", "color"),
            ScopeWord("naranja", "оранжевый", "u2_l7", "color"),
            ScopeWord("rosa", "розовый", "u2_l7", "color"),
            ScopeWord("marrón", "коричневый", "u2_l7", "color"),
            ScopeWord("cielo", "небо", "u2_l7", "noun"),
        ))
        put("u2_l8", listOf(
            ScopeWord("roja", "красная", "u2_l8", "color"),
            ScopeWord("blanca", "белая", "u2_l8", "color"),
            ScopeWord("negra", "чёрная", "u2_l8", "color"),
            ScopeWord("amarilla", "жёлтая", "u2_l8", "color"),
            ScopeWord("coche", "машина", "u2_l8", "noun"),
        ))
        put("u2_l9", listOf(
            ScopeWord("estoy", "я нахожусь", "u2_l9", "verb"),
            ScopeWord("estás", "ты находишься", "u2_l9", "verb"),
            ScopeWord("está", "он/она находится", "u2_l9", "verb"),
            ScopeWord("cansado", "усталый", "u2_l9", "adjective"),
            ScopeWord("contento", "довольный", "u2_l9", "adjective"),
            ScopeWord("en", "в / на", "u2_l9", "preposition"),
        ))
        put("u2_l10", listOf(
            ScopeWord("sobre", "на (поверх)", "u2_l10", "preposition"),
            ScopeWord("debajo de", "под", "u2_l10", "preposition"),
            ScopeWord("al lado de", "рядом с", "u2_l10", "preposition"),
            ScopeWord("entre", "между", "u2_l10", "preposition"),
            ScopeWord("delante de", "перед", "u2_l10", "preposition"),
            ScopeWord("detrás de", "позади", "u2_l10", "preposition"),
            ScopeWord("oficina", "офис", "u2_l10", "noun"),
            ScopeWord("lámpara", "лампа", "u2_l10", "noun"),
        ))
        put("u2_l11", listOf(
            ScopeWord("sala", "гостиная", "u2_l11", "room"),
            ScopeWord("cocina", "кухня", "u2_l11", "room"),
            ScopeWord("dormitorio", "спальня", "u2_l11", "room"),
            ScopeWord("baño", "ванная", "u2_l11", "room"),
            ScopeWord("comedor", "столовая", "u2_l11", "room"),
            ScopeWord("balcón", "балкон", "u2_l11", "room"),
            ScopeWord("pasillo", "коридор", "u2_l11", "room"),
        ))
        put("u2_l12", listOf(
            ScopeWord("sofá", "диван", "u2_l12", "furniture"),
            ScopeWord("mesa", "стол", "u2_l12", "furniture"),
            ScopeWord("silla", "стул", "u2_l12", "furniture"),
            ScopeWord("cama", "кровать", "u2_l12", "furniture"),
            ScopeWord("armario", "шкаф", "u2_l12", "furniture"),
            ScopeWord("nevera", "холодильник", "u2_l12", "furniture"),
        ))
        put("u2_l13", listOf(
            ScopeWord("casas", "дома", "u2_l13", "noun"),
            ScopeWord("papel", "бумага", "u2_l13", "noun"),
            ScopeWord("papeles", "бумаги", "u2_l13", "noun"),
            ScopeWord("luz", "свет", "u2_l13", "noun"),
            ScopeWord("luces", "огни", "u2_l13", "noun"),
            ScopeWord("hermanas", "сёстры", "u2_l13", "family"),
        ))
        put("u2_l14", listOf(
            ScopeWord("piso", "квартира / этаж", "u2_l14", "noun"),
            ScopeWord("habitaciones", "комнаты", "u2_l14", "noun"),
            ScopeWord("trescientos", "триста", "u2_l14", "number"),
            ScopeWord("anuncio", "объявление", "u2_l14", "noun"),
            ScopeWord("dueña", "хозяйка", "u2_l14", "noun"),
            ScopeWord("mes", "месяц", "u2_l14", "noun"),
            ScopeWord("centro", "центр", "u2_l14", "noun"),
            ScopeWord("planta", "этаж (зд.)", "u2_l14", "noun"),
            ScopeWord("dormitorios", "спальни", "u2_l14", "room"),
            ScopeWord("perfecto", "отлично", "u2_l14", "phrase"),
            ScopeWord("grande", "большой", "u2_l14", "adjective"),
        ))

        // ═══════════════════════════════════════════════════════════════
        // Блок 1.3 «Действие» — глаголы AR/ER/IR + еда + querer/poder + e→i + время
        // ═══════════════════════════════════════════════════════════════
        put("u3_l0", listOf(
            ScopeWord("hablo", "говорю", "u3_l0", "verb"),
            ScopeWord("hablas", "говоришь", "u3_l0", "verb"),
            ScopeWord("habla", "говорит", "u3_l0", "verb"),
            ScopeWord("trabajo", "работаю", "u3_l0", "verb"),
            ScopeWord("trabajas", "работаешь", "u3_l0", "verb"),
            ScopeWord("trabaja", "работает", "u3_l0", "verb"),
        ))
        put("u3_l1", listOf(
            ScopeWord("hablamos", "говорим", "u3_l1", "verb"),
            ScopeWord("habláis", "говорите (Исп)", "u3_l1", "verb"),
            ScopeWord("hablan", "говорят", "u3_l1", "verb"),
            ScopeWord("trabajamos", "работаем", "u3_l1", "verb"),
            ScopeWord("trabajan", "работают", "u3_l1", "verb"),
        ))
        put("u3_l2", listOf(
            ScopeWord("comer", "есть", "u3_l2", "verb"),
            ScopeWord("como", "ем", "u3_l2", "verb"),
            ScopeWord("comes", "ешь", "u3_l2", "verb"),
            ScopeWord("come", "ест", "u3_l2", "verb"),
            ScopeWord("comemos", "едим", "u3_l2", "verb"),
            ScopeWord("comen", "едят", "u3_l2", "verb"),
            ScopeWord("beber", "пить", "u3_l2", "verb"),
            ScopeWord("bebo", "пью", "u3_l2", "verb"),
            ScopeWord("bebemos", "пьём", "u3_l2", "verb"),
            ScopeWord("leer", "читать", "u3_l2", "verb"),
            ScopeWord("leo", "читаю", "u3_l2", "verb"),
            ScopeWord("lees", "читаешь", "u3_l2", "verb"),
        ))
        put("u3_l3", listOf(
            ScopeWord("vivir", "жить", "u3_l3", "verb"),
            ScopeWord("vivo", "живу", "u3_l3", "verb"),
            ScopeWord("vives", "живёшь", "u3_l3", "verb"),
            ScopeWord("vive", "живёт", "u3_l3", "verb"),
            ScopeWord("vivimos", "живём", "u3_l3", "verb"),
            ScopeWord("escribir", "писать", "u3_l3", "verb"),
            ScopeWord("escriben", "пишут", "u3_l3", "verb"),
            ScopeWord("carta", "письмо", "u3_l3", "noun"),
            ScopeWord("Moscú", "Москва", "u3_l3", "city"),
        ))
        put("u3_l4", listOf(
            ScopeWord("pan", "хлеб", "u3_l4", "food"),
            ScopeWord("leche", "молоко", "u3_l4", "food"),
            ScopeWord("agua", "вода", "u3_l4", "food"),
            ScopeWord("fruta", "фрукты", "u3_l4", "food"),
            ScopeWord("carne", "мясо", "u3_l4", "food"),
            ScopeWord("queso", "сыр", "u3_l4", "food"),
            ScopeWord("pescado", "рыба", "u3_l4", "food"),
            ScopeWord("huevo", "яйцо", "u3_l4", "food"),
            ScopeWord("sopa", "суп", "u3_l4", "food"),
        ))
        put("u3_l5", listOf(
            ScopeWord("menú", "меню", "u3_l5", "noun"),
            ScopeWord("plato", "блюдо", "u3_l5", "noun"),
            ScopeWord("cuenta", "счёт", "u3_l5", "noun"),
            ScopeWord("propina", "чаевые", "u3_l5", "noun"),
            ScopeWord("camarero", "официант", "u3_l5", "noun"),
            ScopeWord("camarera", "официантка", "u3_l5", "noun"),
            ScopeWord("bebida", "напиток", "u3_l5", "noun"),
            ScopeWord("postre", "десерт", "u3_l5", "noun"),
            ScopeWord("cliente", "клиент", "u3_l5", "noun"),
            ScopeWord("claro", "конечно", "u3_l5", "adverb"),
        ))
        put("u3_l5_5", listOf(
            ScopeWord("hay", "есть/имеется", "u3_l5_5", "verb"),
            ScopeWord("nada", "ничего", "u3_l5_5", "pronoun"),
            ScopeWord("cerca", "близко", "u3_l5_5", "adverb"),
            ScopeWord("allí", "там", "u3_l5_5", "adverb"),
            ScopeWord("libre", "свободный", "u3_l5_5", "adjective"),
        ))
        put("u3_l6", listOf(
            ScopeWord("querer", "хотеть", "u3_l6", "verb"),
            ScopeWord("quiero", "хочу", "u3_l6", "verb"),
            ScopeWord("quieres", "хочешь", "u3_l6", "verb"),
            ScopeWord("quiere", "хочет", "u3_l6", "verb"),
            ScopeWord("queremos", "хотим", "u3_l6", "verb"),
            ScopeWord("quieren", "хотят", "u3_l6", "verb"),
        ))
        put("u3_l7", listOf(
            ScopeWord("poder", "мочь", "u3_l7", "verb"),
            ScopeWord("puedo", "могу", "u3_l7", "verb"),
            ScopeWord("puedes", "можешь", "u3_l7", "verb"),
            ScopeWord("puede", "может", "u3_l7", "verb"),
            ScopeWord("podemos", "можем", "u3_l7", "verb"),
            ScopeWord("pueden", "могут", "u3_l7", "verb"),
            ScopeWord("ayudar", "помогать", "u3_l7", "verb"),
            ScopeWord("pedir", "просить", "u3_l7", "verb"),
        ))
        put("u3_l7_5", listOf(
            ScopeWord("pido", "прошу", "u3_l7_5", "verb"),
            ScopeWord("pides", "просишь", "u3_l7_5", "verb"),
            ScopeWord("pide", "просит", "u3_l7_5", "verb"),
            ScopeWord("pedimos", "просим", "u3_l7_5", "verb"),
            ScopeWord("piden", "просят", "u3_l7_5", "verb"),
            ScopeWord("servir", "обслуживать", "u3_l7_5", "verb"),
            ScopeWord("repetir", "повторять", "u3_l7_5", "verb"),
            ScopeWord("decir", "говорить", "u3_l7_5", "verb"),
            ScopeWord("digo", "говорю", "u3_l7_5", "verb"),
        ))
        put("u3_l8", listOf(
            ScopeWord("hora", "час", "u3_l8", "time"),
            ScopeWord("media", "половина", "u3_l8", "time"),
            ScopeWord("cuarto", "четверть", "u3_l8", "time"),
            ScopeWord("menos", "минус (в часах: «без»)", "u3_l8", "preposition"),
        ))
        put("u3_l9", listOf(
            ScopeWord("lunes", "понедельник", "u3_l9", "day"),
            ScopeWord("martes", "вторник", "u3_l9", "day"),
            ScopeWord("miércoles", "среда", "u3_l9", "day"),
            ScopeWord("jueves", "четверг", "u3_l9", "day"),
            ScopeWord("viernes", "пятница", "u3_l9", "day"),
            ScopeWord("sábado", "суббота", "u3_l9", "day"),
            ScopeWord("domingo", "воскресенье", "u3_l9", "day"),
        ))
        put("u3_l10", listOf(
            ScopeWord("enero", "январь", "u3_l10", "month"),
            ScopeWord("febrero", "февраль", "u3_l10", "month"),
            ScopeWord("marzo", "март", "u3_l10", "month"),
            ScopeWord("abril", "апрель", "u3_l10", "month"),
            ScopeWord("mayo", "май", "u3_l10", "month"),
            ScopeWord("junio", "июнь", "u3_l10", "month"),
            ScopeWord("julio", "июль", "u3_l10", "month"),
            ScopeWord("agosto", "август", "u3_l10", "month"),
            ScopeWord("septiembre", "сентябрь", "u3_l10", "month"),
            ScopeWord("octubre", "октябрь", "u3_l10", "month"),
            ScopeWord("noviembre", "ноябрь", "u3_l10", "month"),
            ScopeWord("diciembre", "декабрь", "u3_l10", "month"),
            ScopeWord("cumpleaños", "день рождения", "u3_l10", "noun"),
            ScopeWord("nací", "я родился", "u3_l10", "verb"),
            ScopeWord("viajas", "ты путешествуешь", "u3_l10", "verb"),
        ))
        put("u3_l11", listOf(
            ScopeWord("hoy", "сегодня", "u3_l11", "adverb"),
            ScopeWord("ayer", "вчера", "u3_l11", "adverb"),
            ScopeWord("ahora", "сейчас", "u3_l11", "adverb"),
            ScopeWord("siempre", "всегда", "u3_l11", "adverb"),
            ScopeWord("nunca", "никогда", "u3_l11", "adverb"),
            ScopeWord("a veces", "иногда", "u3_l11", "adverb"),
            ScopeWord("pronto", "скоро", "u3_l11", "adverb"),
        ))
        put("u3_l12", listOf(
            ScopeWord("qué", "что?", "u3_l12", "interrogative"),
            ScopeWord("quién", "кто?", "u3_l12", "interrogative"),
            ScopeWord("dónde", "где?", "u3_l12", "interrogative"),
            ScopeWord("cuándo", "когда?", "u3_l12", "interrogative"),
            ScopeWord("cuánto", "сколько?", "u3_l12", "interrogative"),
            ScopeWord("cómo", "как?", "u3_l12", "interrogative"),
            ScopeWord("por qué", "почему?", "u3_l12", "interrogative"),
            ScopeWord("cuesta", "стоит", "u3_l12", "verb"),
        ))
        put("u3_l13", listOf(
            ScopeWord("no", "нет / не", "u3_l13", "particle"),
            ScopeWord("nadie", "никто", "u3_l13", "pronoun"),
            ScopeWord("jamás", "никогда (усил.)", "u3_l13", "adverb"),
            ScopeWord("dinero", "деньги", "u3_l13", "noun"),
            ScopeWord("sé", "знаю", "u3_l13", "verb"),
            ScopeWord("vino (verb)", "пришёл", "u3_l13", "verb"),
        ))
        put("u3_l14", listOf(
            ScopeWord("restaurante", "ресторан", "u3_l14", "noun"),
            ScopeWord("abre", "открывает(ся)", "u3_l14", "verb"),
            ScopeWord("cerrado", "закрытый", "u3_l14", "adjective"),
            ScopeWord("desea", "желает", "u3_l14", "verb"),
        ))

        // u1_l13_5 — Порядковые числительные
        put("u1_l13_5", listOf(
            ScopeWord("primero", "первый", "u1_l13_5", "ordinal"),
            ScopeWord("primer", "первый (м.перед сущ.)", "u1_l13_5", "ordinal"),
            ScopeWord("primera", "первая", "u1_l13_5", "ordinal"),
            ScopeWord("segundo", "второй", "u1_l13_5", "ordinal"),
            ScopeWord("segunda", "вторая", "u1_l13_5", "ordinal"),
            ScopeWord("tercero", "третий", "u1_l13_5", "ordinal"),
            ScopeWord("tercer", "третий (м.перед сущ.)", "u1_l13_5", "ordinal"),
            ScopeWord("tercera", "третья", "u1_l13_5", "ordinal"),
            ScopeWord("cuarto", "четвёртый", "u1_l13_5", "ordinal"),
            ScopeWord("quinto", "пятый", "u1_l13_5", "ordinal"),
            ScopeWord("sexto", "шестой", "u1_l13_5", "ordinal"),
            ScopeWord("séptimo", "седьмой", "u1_l13_5", "ordinal"),
            ScopeWord("octavo", "восьмой", "u1_l13_5", "ordinal"),
            ScopeWord("noveno", "девятый", "u1_l13_5", "ordinal"),
            ScopeWord("décimo", "десятый", "u1_l13_5", "ordinal"),
            ScopeWord("piso", "этаж", "u1_l13_5", "noun"),
            ScopeWord("vez", "раз", "u1_l13_5", "noun"),
            ScopeWord("hijo", "сын", "u1_l13_5", "noun"),
        ))
    }

    /**
     * Порядок уроков для расчёта кумулятивного scope.
     * Источник — RoadmapData. Здесь дублируется чтобы избежать круговой зависимости
     * (RoadmapData может зависеть от VocabScope, но не наоборот).
     *
     * При добавлении нового урока (например u1_l13_5) он вставляется ПО ПОРЯДКУ
     * после своего prerequisite — это гарантирует что scope расчёт остаётся правильным.
     */
    private val lessonOrder: List<String> = buildList {
        // A1 · Блок 1
        addAll(listOf("u1_l0", "u1_l1", "u1_l2", "u1_l3"))                  // фонетика
        addAll(listOf("u1_l4", "u1_l5", "u1_l6"))                            // приветствия / вежливость
        addAll(listOf("u1_l7", "u1_l8", "u1_l9"))                            // SER + местоимения
        addAll(listOf("u1_l10", "u1_l11"))                                   // род + артикли
        addAll(listOf("u1_l12", "u1_l13", "u1_l13_5", "u1_l14"))            // страны + числа + порядковые + checkpoint
        // A1 · Блок 2 «Мой мир»
        addAll(listOf("u2_l0", "u2_l1"))                                     // числа 11-100
        addAll(listOf("u2_l2", "u2_l3"))                                     // TENER
        addAll(listOf("u2_l4", "u2_l5"))                                     // семья
        addAll(listOf("u2_l6"))                                              // притяжательные
        addAll(listOf("u2_l7", "u2_l8"))                                     // цвета + согласование
        addAll(listOf("u2_l9", "u2_l10"))                                    // ESTAR + предлоги места
        addAll(listOf("u2_l11", "u2_l12", "u2_l13", "u2_l14"))              // дом + мебель + мн.ч. + checkpoint
        // A1 · Блок 3 «Действие»
        addAll(listOf("u3_l0", "u3_l1", "u3_l2", "u3_l3"))                  // глаголы AR/ER/IR
        addAll(listOf("u3_l4", "u3_l5", "u3_l5_5"))                          // еда + ресторан + hay
        addAll(listOf("u3_l6", "u3_l7", "u3_l7_5"))                          // querer + poder + e→i
        addAll(listOf("u3_l8", "u3_l9", "u3_l10", "u3_l11"))                // время + дни + месяцы + наречия
        addAll(listOf("u3_l12", "u3_l13", "u3_l14"))                        // вопросы + отрицание + checkpoint
        // (остальные блоки добавляются по мере прохождения)
    }

    /**
     * Все слова доступные к моменту изучения данного урока (включая сам урок).
     *
     * Если lessonId не найден — возвращает пустой Set (безопасно для генератора).
     */
    fun wordsForLesson(lessonId: String): List<ScopeWord> {
        val idx = lessonOrder.indexOf(lessonId)
        if (idx < 0) return emptyList()
        return lessonOrder.take(idx + 1).flatMap { newWordsByLesson[it].orEmpty() }
    }

    /**
     * Только НОВЫЕ слова, введённые в этом уроке. Используется для MATCH_PAIRS
     * и других упражнений где надо тренировать именно сегодняшний материал.
     */
    fun newWordsInLesson(lessonId: String): List<ScopeWord> =
        newWordsByLesson[lessonId].orEmpty()

    /**
     * Сколько слов уже доступно к концу указанного урока.
     * Используется для статистики и UI «вы знаете 47 слов».
     */
    fun cumulativeWordCountAt(lessonId: String): Int = wordsForLesson(lessonId).size

    /** Все слова курса (на максимальной точке). Для тестов и стат-экранов. */
    fun allWords(): List<ScopeWord> = newWordsByLesson.values.flatten()
}
