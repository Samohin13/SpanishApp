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
 * val scope = VocabScope.wordsForLesson("u1_l10")
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

        // v1.3.4: u1_l0..u1_l2 — алфавит по 9 букв за урок. Слова-примеры
        // подобраны так, чтобы все буквы были из этого урока.
        // u1_l0 — алфавит A-I
        put("u1_l0", listOf(
            ScopeWord("agua", "вода", "u1_l0", "noun"),
            ScopeWord("bueno", "хороший", "u1_l0", "adjective"),
            ScopeWord("casa", "дом", "u1_l0", "noun"),
            ScopeWord("cinco", "пять", "u1_l0", "number"),
            ScopeWord("día", "день", "u1_l0", "noun"),
            ScopeWord("elefante", "слон", "u1_l0", "noun"),
            ScopeWord("foto", "фото", "u1_l0", "noun"),
            ScopeWord("gato", "кот", "u1_l0", "noun"),
            ScopeWord("gente", "люди", "u1_l0", "noun"),
            ScopeWord("hola", "привет", "u1_l0", "greeting"),
            ScopeWord("isla", "остров", "u1_l0", "noun"),
        ))
        // u1_l1 — алфавит J-Q
        put("u1_l1", listOf(
            ScopeWord("Japón", "Япония", "u1_l1", "country"),
            ScopeWord("kilo", "кило", "u1_l1", "noun"),
            ScopeWord("luna", "луна", "u1_l1", "noun"),
            ScopeWord("madre", "мать", "u1_l1", "family"),
            ScopeWord("noche", "ночь", "u1_l1", "noun"),
            ScopeWord("año", "год", "u1_l1", "noun"),
            ScopeWord("oro", "золото", "u1_l1", "noun"),
            ScopeWord("padre", "отец", "u1_l1", "family"),
            ScopeWord("queso", "сыр", "u1_l1", "food"),
        ))
        // u1_l2 — алфавит R-Z
        put("u1_l2", listOf(
            ScopeWord("rojo", "красный", "u1_l2", "color"),
            ScopeWord("perro", "собака", "u1_l2", "animal"),
            ScopeWord("pero", "но", "u1_l2", "conjunction"),
            ScopeWord("sol", "солнце", "u1_l2", "noun"),
            ScopeWord("tomate", "помидор", "u1_l2", "food"),
            ScopeWord("uno", "один", "u1_l2", "number"),
            ScopeWord("vino", "вино", "u1_l2", "food"),
            ScopeWord("wifi", "вай-фай", "u1_l2", "noun"),
            ScopeWord("taxi", "такси", "u1_l2", "noun"),
            ScopeWord("yo", "я", "u1_l2", "pronoun"),
            ScopeWord("zapato", "ботинок", "u1_l2", "noun"),
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
        put("u1_l10", listOf(
            ScopeWord("soy", "я есть", "u1_l10", "verb"),
            ScopeWord("eres", "ты есть", "u1_l10", "verb"),
            ScopeWord("es", "он/она есть", "u1_l10", "verb"),
            ScopeWord("ruso", "русский (м)", "u1_l10", "nationality"),
            ScopeWord("Pablo", "Павел", "u1_l10", "name"),
            ScopeWord("amigo", "друг", "u1_l10", "noun"),
            ScopeWord("alto", "высокий", "u1_l10", "adjective"),
            ScopeWord("médico", "врач (м)", "u1_l10", "profession"),
            ScopeWord("ingeniero", "инженер", "u1_l10", "profession"),
        ))
        // u1_l8 — SER somos/sois/son
        put("u1_l11", listOf(
            ScopeWord("somos", "мы есть", "u1_l11", "verb"),
            ScopeWord("sois", "вы есть (Исп.)", "u1_l11", "verb"),
            ScopeWord("son", "они есть", "u1_l11", "verb"),
            ScopeWord("amigos", "друзья", "u1_l11", "noun"),
            ScopeWord("estudiantes", "студенты", "u1_l11", "noun"),
            ScopeWord("amables", "любезные", "u1_l11", "adjective"),
            ScopeWord("aquí", "здесь", "u1_l11", "adverb"),
        ))
        // u1_l9 — Местоимения
        put("u1_l7", listOf(
            ScopeWord("yo", "я", "u1_l7", "pronoun"),
            ScopeWord("tú", "ты", "u1_l7", "pronoun"),
            ScopeWord("usted", "Вы (формально)", "u1_l7", "pronoun"),
            ScopeWord("él", "он", "u1_l7", "pronoun"),
            ScopeWord("ella", "она", "u1_l7", "pronoun"),
            ScopeWord("nosotros", "мы (м)", "u1_l7", "pronoun"),
            ScopeWord("nosotras", "мы (ж)", "u1_l7", "pronoun"),
            ScopeWord("vosotros", "вы (м, Исп.)", "u1_l7", "pronoun"),
            ScopeWord("ellos", "они (м)", "u1_l7", "pronoun"),
            ScopeWord("ellas", "они (ж)", "u1_l7", "pronoun"),
            ScopeWord("hermano", "брат", "u1_l7", "noun"),
            ScopeWord("rusas", "русские (ж)", "u1_l7", "nationality"),
        ))
        // u1_l10 — Род el/la
        put("u1_l8", listOf(
            ScopeWord("el", "м.артикль (опр.)", "u1_l8", "article"),
            ScopeWord("la", "ж.артикль (опр.)", "u1_l8", "article"),
            ScopeWord("libro", "книга", "u1_l8", "noun"),
            ScopeWord("día", "день", "u1_l8", "noun"),
            ScopeWord("mano", "рука", "u1_l8", "noun"),
            ScopeWord("médica", "врач (ж)", "u1_l8", "profession"),
        ))
        // u1_l11 — Артикли
        put("u1_l9", listOf(
            ScopeWord("un", "м.артикль (неопр.)", "u1_l9", "article"),
            ScopeWord("una", "ж.артикль (неопр.)", "u1_l9", "article"),
            ScopeWord("los", "м.мн.артикль (опр.)", "u1_l9", "article"),
            ScopeWord("las", "ж.мн.артикль (опр.)", "u1_l9", "article"),
            ScopeWord("unos", "м.мн.артикль (неопр.)", "u1_l9", "article"),
            ScopeWord("unas", "ж.мн.артикль (неопр.)", "u1_l9", "article"),
            ScopeWord("libros", "книги", "u1_l9", "noun"),
            ScopeWord("casas", "дома", "u1_l9", "noun"),
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
        put("u2_l6", listOf(
            ScopeWord("once", "одиннадцать", "u2_l6", "number"),
            ScopeWord("doce", "двенадцать", "u2_l6", "number"),
            ScopeWord("trece", "тринадцать", "u2_l6", "number"),
            ScopeWord("catorce", "четырнадцать", "u2_l6", "number"),
            ScopeWord("quince", "пятнадцать", "u2_l6", "number"),
            ScopeWord("dieciséis", "шестнадцать", "u2_l6", "number"),
            ScopeWord("diecisiete", "семнадцать", "u2_l6", "number"),
            ScopeWord("dieciocho", "восемнадцать", "u2_l6", "number"),
            ScopeWord("diecinueve", "девятнадцать", "u2_l6", "number"),
            ScopeWord("veinte", "двадцать", "u2_l6", "number"),
            ScopeWord("euros", "евро", "u2_l6", "noun"),
            ScopeWord("años", "годы", "u2_l6", "noun"),
        ))
        put("u2_l7", listOf(
            ScopeWord("veintiuno", "21", "u2_l7", "number"),
            ScopeWord("veinticinco", "25", "u2_l7", "number"),
            ScopeWord("treinta", "30", "u2_l7", "number"),
            ScopeWord("cuarenta", "40", "u2_l7", "number"),
            ScopeWord("cincuenta", "50", "u2_l7", "number"),
            ScopeWord("sesenta", "60", "u2_l7", "number"),
            ScopeWord("setenta", "70", "u2_l7", "number"),
            ScopeWord("ochenta", "80", "u2_l7", "number"),
            ScopeWord("noventa", "90", "u2_l7", "number"),
            ScopeWord("cien", "100", "u2_l7", "number"),
            ScopeWord("y", "и", "u2_l7", "conjunction"),
        ))
        put("u2_l4", listOf(
            ScopeWord("tengo", "у меня есть", "u2_l4", "verb"),
            ScopeWord("tienes", "у тебя есть", "u2_l4", "verb"),
            ScopeWord("tiene", "у него/неё есть", "u2_l4", "verb"),
            ScopeWord("razón", "правота", "u2_l4", "noun"),
        ))
        put("u2_l5", listOf(
            ScopeWord("tenemos", "у нас есть", "u2_l5", "verb"),
            ScopeWord("tenéis", "у вас есть (Исп.)", "u2_l5", "verb"),
            ScopeWord("tienen", "у них есть", "u2_l5", "verb"),
            ScopeWord("hijas", "дочери", "u2_l5", "noun"),
        ))
        put("u2_l8", listOf(
            ScopeWord("padre", "отец", "u2_l8", "family"),
            ScopeWord("madre", "мать", "u2_l8", "family"),
            ScopeWord("hermana", "сестра", "u2_l8", "family"),
            ScopeWord("hija", "дочь", "u2_l8", "family"),
            ScopeWord("hermanos", "братья", "u2_l8", "family"),
            ScopeWord("padres", "родители", "u2_l8", "family"),
        ))
        put("u2_l9", listOf(
            ScopeWord("abuelo", "дед", "u2_l9", "family"),
            ScopeWord("abuela", "бабушка", "u2_l9", "family"),
            ScopeWord("tío", "дядя", "u2_l9", "family"),
            ScopeWord("tía", "тётя", "u2_l9", "family"),
            ScopeWord("primo", "двоюр.брат", "u2_l9", "family"),
            ScopeWord("prima", "двоюр.сестра", "u2_l9", "family"),
            ScopeWord("sobrino", "племянник", "u2_l9", "family"),
            ScopeWord("sobrina", "племянница", "u2_l9", "family"),
            ScopeWord("nieto", "внук", "u2_l9", "family"),
            ScopeWord("nieta", "внучка", "u2_l9", "family"),
            ScopeWord("Carmen", "Кармен", "u2_l9", "name"),
            ScopeWord("tíos", "дяди", "u2_l9", "family"),
            ScopeWord("Madrid", "Мадрид", "u2_l9", "city"),
        ))
        put("u2_l10", listOf(
            ScopeWord("mi", "мой/моя", "u2_l10", "possessive"),
            ScopeWord("mis", "мои", "u2_l10", "possessive"),
            ScopeWord("tu", "твой/твоя", "u2_l10", "possessive"),
            ScopeWord("su", "его/её/Ваш", "u2_l10", "possessive"),
            ScopeWord("nuestro", "наш", "u2_l10", "possessive"),
            ScopeWord("nuestra", "наша", "u2_l10", "possessive"),
            ScopeWord("vuestro", "ваш (Исп.)", "u2_l10", "possessive"),
        ))
        put("u2_l11", listOf(
            ScopeWord("rojo", "красный", "u2_l11", "color"),
            ScopeWord("azul", "синий", "u2_l11", "color"),
            ScopeWord("verde", "зелёный", "u2_l11", "color"),
            ScopeWord("amarillo", "жёлтый", "u2_l11", "color"),
            ScopeWord("negro", "чёрный", "u2_l11", "color"),
            ScopeWord("blanco", "белый", "u2_l11", "color"),
            ScopeWord("gris", "серый", "u2_l11", "color"),
            ScopeWord("naranja", "оранжевый", "u2_l11", "color"),
            ScopeWord("rosa", "розовый", "u2_l11", "color"),
            ScopeWord("marrón", "коричневый", "u2_l11", "color"),
            ScopeWord("cielo", "небо", "u2_l11", "noun"),
        ))
        put("u2_l12", listOf(
            ScopeWord("roja", "красная", "u2_l12", "color"),
            ScopeWord("blanca", "белая", "u2_l12", "color"),
            ScopeWord("negra", "чёрная", "u2_l12", "color"),
            ScopeWord("amarilla", "жёлтая", "u2_l12", "color"),
            ScopeWord("coche", "машина", "u2_l12", "noun"),
        ))
        put("u3_l0", listOf(
            ScopeWord("estoy", "я нахожусь", "u3_l0", "verb"),
            ScopeWord("estás", "ты находишься", "u3_l0", "verb"),
            ScopeWord("está", "он/она находится", "u3_l0", "verb"),
            ScopeWord("cansado", "усталый", "u3_l0", "adjective"),
            ScopeWord("contento", "довольный", "u3_l0", "adjective"),
            ScopeWord("en", "в / на", "u3_l0", "preposition"),
        ))
        put("u3_l1", listOf(
            ScopeWord("sobre", "на (поверх)", "u3_l1", "preposition"),
            ScopeWord("debajo de", "под", "u3_l1", "preposition"),
            ScopeWord("al lado de", "рядом с", "u3_l1", "preposition"),
            ScopeWord("entre", "между", "u3_l1", "preposition"),
            ScopeWord("delante de", "перед", "u3_l1", "preposition"),
            ScopeWord("detrás de", "позади", "u3_l1", "preposition"),
            ScopeWord("oficina", "офис", "u3_l1", "noun"),
            ScopeWord("lámpara", "лампа", "u3_l1", "noun"),
        ))
        put("u3_l2", listOf(
            ScopeWord("sala", "гостиная", "u3_l2", "room"),
            ScopeWord("cocina", "кухня", "u3_l2", "room"),
            ScopeWord("dormitorio", "спальня", "u3_l2", "room"),
            ScopeWord("baño", "ванная", "u3_l2", "room"),
            ScopeWord("comedor", "столовая", "u3_l2", "room"),
            ScopeWord("balcón", "балкон", "u3_l2", "room"),
            ScopeWord("pasillo", "коридор", "u3_l2", "room"),
        ))
        put("u3_l3", listOf(
            ScopeWord("sofá", "диван", "u3_l3", "furniture"),
            ScopeWord("mesa", "стол", "u3_l3", "furniture"),
            ScopeWord("silla", "стул", "u3_l3", "furniture"),
            ScopeWord("cama", "кровать", "u3_l3", "furniture"),
            ScopeWord("armario", "шкаф", "u3_l3", "furniture"),
            ScopeWord("nevera", "холодильник", "u3_l3", "furniture"),
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
        put("u2_l0", listOf(
            ScopeWord("hablo", "говорю", "u2_l0", "verb"),
            ScopeWord("hablas", "говоришь", "u2_l0", "verb"),
            ScopeWord("habla", "говорит", "u2_l0", "verb"),
            ScopeWord("trabajo", "работаю", "u2_l0", "verb"),
            ScopeWord("trabajas", "работаешь", "u2_l0", "verb"),
            ScopeWord("trabaja", "работает", "u2_l0", "verb"),
        ))
        put("u2_l1", listOf(
            ScopeWord("hablamos", "говорим", "u2_l1", "verb"),
            ScopeWord("habláis", "говорите (Исп)", "u2_l1", "verb"),
            ScopeWord("hablan", "говорят", "u2_l1", "verb"),
            ScopeWord("trabajamos", "работаем", "u2_l1", "verb"),
            ScopeWord("trabajan", "работают", "u2_l1", "verb"),
        ))
        put("u2_l2", listOf(
            ScopeWord("comer", "есть", "u2_l2", "verb"),
            ScopeWord("como", "ем", "u2_l2", "verb"),
            ScopeWord("comes", "ешь", "u2_l2", "verb"),
            ScopeWord("come", "ест", "u2_l2", "verb"),
            ScopeWord("comemos", "едим", "u2_l2", "verb"),
            ScopeWord("comen", "едят", "u2_l2", "verb"),
            ScopeWord("beber", "пить", "u2_l2", "verb"),
            ScopeWord("bebo", "пью", "u2_l2", "verb"),
            ScopeWord("bebemos", "пьём", "u2_l2", "verb"),
            ScopeWord("leer", "читать", "u2_l2", "verb"),
            ScopeWord("leo", "читаю", "u2_l2", "verb"),
            ScopeWord("lees", "читаешь", "u2_l2", "verb"),
        ))
        put("u2_l3", listOf(
            ScopeWord("vivir", "жить", "u2_l3", "verb"),
            ScopeWord("vivo", "живу", "u2_l3", "verb"),
            ScopeWord("vives", "живёшь", "u2_l3", "verb"),
            ScopeWord("vive", "живёт", "u2_l3", "verb"),
            ScopeWord("vivimos", "живём", "u2_l3", "verb"),
            ScopeWord("escribir", "писать", "u2_l3", "verb"),
            ScopeWord("escriben", "пишут", "u2_l3", "verb"),
            ScopeWord("carta", "письмо", "u2_l3", "noun"),
            ScopeWord("Moscú", "Москва", "u2_l3", "city"),
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

        // ═══════════════════════════════════════════════════════════════
        // Блок 1.4 «Выживание» — IR + GUSTAR + транспорт + магазин + здоровье
        // ═══════════════════════════════════════════════════════════════
        put("u4_l2", listOf(
            ScopeWord("metro", "метро", "u4_l2", "transport"),
            ScopeWord("autobús", "автобус", "u4_l2", "transport"),
            ScopeWord("taxi", "такси", "u4_l2", "transport"),
            ScopeWord("tren", "поезд", "u4_l2", "transport"),
            ScopeWord("bici", "велик", "u4_l2", "transport"),
            ScopeWord("avión", "самолёт", "u4_l2", "transport"),
            ScopeWord("barco", "корабль", "u4_l2", "transport"),
            ScopeWord("tomo", "беру (использую)", "u4_l2", "verb"),
        ))
        put("u4_l0", listOf(
            ScopeWord("voy", "иду", "u4_l0", "verb"),
            ScopeWord("vas", "идёшь", "u4_l0", "verb"),
            ScopeWord("va", "идёт", "u4_l0", "verb"),
            ScopeWord("vamos", "идём", "u4_l0", "verb"),
            ScopeWord("vais", "идёте (Исп)", "u4_l0", "verb"),
            ScopeWord("van", "идут", "u4_l0", "verb"),
            ScopeWord("ir", "идти", "u4_l0", "verb"),
        ))
        put("u4_l1", listOf(
            ScopeWord("al", "к (a+el)", "u4_l1", "preposition"),
            ScopeWord("cine", "кино", "u4_l1", "noun"),
            ScopeWord("colegio", "школа", "u4_l1", "noun"),
            ScopeWord("banco", "банк", "u4_l1", "noun"),
        ))
        put("u4_l3", listOf(
            ScopeWord("derecha", "правая", "u4_l3", "noun"),
            ScopeWord("izquierda", "левая", "u4_l3", "noun"),
            ScopeWord("recto", "прямо", "u4_l3", "adverb"),
            ScopeWord("gira", "поверни", "u4_l3", "verb"),
            ScopeWord("sigue", "продолжай", "u4_l3", "verb"),
            ScopeWord("lejos", "далеко", "u4_l3", "adverb"),
            ScopeWord("llego", "добираюсь", "u4_l3", "verb"),
        ))
        put("u4_l4", listOf(
            ScopeWord("caro", "дорогой", "u4_l4", "adjective"),
            ScopeWord("barato", "дешёвый", "u4_l4", "adjective"),
            ScopeWord("comprar", "покупать", "u4_l4", "verb"),
            ScopeWord("vender", "продавать", "u4_l4", "verb"),
            ScopeWord("tienda", "магазин", "u4_l4", "noun"),
            ScopeWord("supermercado", "супермаркет", "u4_l4", "noun"),
            ScopeWord("talla", "размер", "u4_l4", "noun"),
            ScopeWord("esto", "это", "u4_l4", "pronoun"),
            ScopeWord("muy", "очень", "u4_l4", "adverb"),
        ))
        put("u4_l5", listOf(
            ScopeWord("euro", "евро", "u4_l5", "money"),
            ScopeWord("precio", "цена", "u4_l5", "noun"),
            ScopeWord("efectivo", "наличные", "u4_l5", "money"),
            ScopeWord("tarjeta", "карта", "u4_l5", "money"),
            ScopeWord("cambio", "сдача", "u4_l5", "noun"),
            ScopeWord("moneda", "монета", "u4_l5", "money"),
            ScopeWord("billete", "купюра/билет", "u4_l5", "money"),
            ScopeWord("factura", "квитанция", "u4_l5", "noun"),
            ScopeWord("pago", "плачу", "u4_l5", "verb"),
        ))
        put("u4_l6", listOf(
            ScopeWord("me gusta", "мне нравится", "u4_l6", "verb"),
            ScopeWord("me gustan", "мне нравятся (мн)", "u4_l6", "verb"),
            ScopeWord("gustar", "нравиться", "u4_l6", "verb"),
            ScopeWord("gatos", "коты", "u4_l6", "animal"),
        ))
        put("u4_l7", listOf(
            ScopeWord("te gusta", "тебе нравится", "u4_l7", "verb"),
            ScopeWord("le gusta", "ему/ей нравится", "u4_l7", "verb"),
            ScopeWord("nos gusta", "нам нравится", "u4_l7", "verb"),
            ScopeWord("os gusta", "вам (Исп) нравится", "u4_l7", "verb"),
            ScopeWord("les gusta", "им нравится", "u4_l7", "verb"),
            ScopeWord("fútbol", "футбол", "u4_l7", "noun"),
            ScopeWord("bailar", "танцевать", "u4_l7", "verb"),
        ))
        put("u4_l8", listOf(
            ScopeWord("cabeza", "голова", "u4_l8", "body"),
            ScopeWord("brazo", "рука", "u4_l8", "body"),
            ScopeWord("pierna", "нога", "u4_l8", "body"),
            ScopeWord("boca", "рот", "u4_l8", "body"),
            ScopeWord("nariz", "нос", "u4_l8", "body"),
            ScopeWord("pelo", "волосы", "u4_l8", "body"),
            ScopeWord("pie", "ступня", "u4_l8", "body"),
            ScopeWord("espalda", "спина", "u4_l8", "body"),
            ScopeWord("brazos", "руки", "u4_l8", "body"),
            ScopeWord("piernas", "ноги", "u4_l8", "body"),
            ScopeWord("duele", "болит", "u4_l8", "verb"),
            ScopeWord("duelen", "болят", "u4_l8", "verb"),
            ScopeWord("doctor", "доктор", "u4_l8", "noun"),
        ))
        put("u4_l9", listOf(
            ScopeWord("fiebre", "температура", "u4_l9", "health"),
            ScopeWord("enfermo", "больной", "u4_l9", "adjective"),
            ScopeWord("medicina", "лекарство", "u4_l9", "health"),
            ScopeWord("hospital", "больница", "u4_l9", "place"),
            ScopeWord("farmacia", "аптека", "u4_l9", "place"),
        ))
        put("u4_l10", listOf(
            ScopeWord("camisa", "рубашка", "u4_l10", "clothes"),
            ScopeWord("pantalón", "штаны", "u4_l10", "clothes"),
            ScopeWord("vestido", "платье", "u4_l10", "clothes"),
            ScopeWord("zapatos", "обувь", "u4_l10", "clothes"),
            ScopeWord("chaqueta", "куртка", "u4_l10", "clothes"),
            ScopeWord("camiseta", "футболка", "u4_l10", "clothes"),
            ScopeWord("falda", "юбка", "u4_l10", "clothes"),
            ScopeWord("sombrero", "шляпа", "u4_l10", "clothes"),
            ScopeWord("nuevos", "новые", "u4_l10", "adjective"),
            ScopeWord("llevo", "ношу", "u4_l10", "verb"),
            ScopeWord("busca", "ищет", "u4_l10", "verb"),
        ))
        put("u4_l11", listOf(
            ScopeWord("hace calor", "жарко", "u4_l11", "weather"),
            ScopeWord("hace frío", "холодно", "u4_l11", "weather"),
            ScopeWord("hace sol", "солнечно", "u4_l11", "weather"),
            ScopeWord("hace viento", "ветрено", "u4_l11", "weather"),
            ScopeWord("llueve", "идёт дождь", "u4_l11", "weather"),
            ScopeWord("nieva", "идёт снег", "u4_l11", "weather"),
            ScopeWord("nublado", "облачно", "u4_l11", "weather"),
            ScopeWord("invierno", "зима", "u4_l11", "season"),
            ScopeWord("tiempo", "погода/время", "u4_l11", "noun"),
        ))
        put("u4_l12", listOf(
            ScopeWord("desayuno", "завтракаю", "u4_l12", "verb"),
            ScopeWord("ceno", "ужинаю", "u4_l12", "verb"),
            ScopeWord("ducho", "принимаю душ", "u4_l12", "verb"),
            ScopeWord("noche", "ночь", "u4_l12", "noun"),
        ))
        put("u4_l13", listOf(
            ScopeWord("me", "меня", "u4_l13", "pronoun"),
            ScopeWord("te", "тебя", "u4_l13", "pronoun"),
            ScopeWord("se", "себя", "u4_l13", "pronoun"),
            ScopeWord("nos", "нас", "u4_l13", "pronoun"),
            ScopeWord("os", "вас (Исп)", "u4_l13", "pronoun"),
            ScopeWord("levantarse", "вставать", "u4_l13", "verb"),
            ScopeWord("ducharse", "принимать душ", "u4_l13", "verb"),
            ScopeWord("acostarse", "ложиться спать", "u4_l13", "verb"),
            ScopeWord("levanto", "встаю", "u4_l13", "verb"),
            ScopeWord("acuesto", "ложусь спать", "u4_l13", "verb"),
            ScopeWord("acostamos", "ложимся", "u4_l13", "verb"),
            ScopeWord("duchas", "принимаешь душ", "u4_l13", "verb"),
            ScopeWord("acuestan", "ложатся", "u4_l13", "verb"),
            ScopeWord("lavo", "мою", "u4_l13", "verb"),
            ScopeWord("once", "11 (час)", "u4_l13", "number"),
        ))
        put("u4_l13_5", listOf(
            ScopeWord("salgo", "выхожу", "u4_l13_5", "verb"),
            ScopeWord("hago", "делаю", "u4_l13_5", "verb"),
            ScopeWord("pongo", "кладу", "u4_l13_5", "verb"),
            ScopeWord("conozco", "знаю (зн)", "u4_l13_5", "verb"),
            ScopeWord("conduzco", "вожу", "u4_l13_5", "verb"),
            ScopeWord("doy", "даю", "u4_l13_5", "verb"),
            ScopeWord("veo", "вижу", "u4_l13_5", "verb"),
            ScopeWord("quepo", "помещаюсь", "u4_l13_5", "verb"),
            ScopeWord("respuesta", "ответ", "u4_l13_5", "noun"),
        ))
        put("u4_l14", listOf(
            ScopeWord("museo", "музей", "u4_l14", "place"),
            ScopeWord("Prado", "Прадо", "u4_l14", "name"),
            ScopeWord("croissant", "круассан", "u4_l14", "food"),
            ScopeWord("billete", "билет/купюра", "u4_l14", "noun"),
        ))

        // ═══════════════════════════════════════════════════════════════
        // A2 · Блок 2.1 «В прошлом» — Pretérito Indefinido
        // ═══════════════════════════════════════════════════════════════
        put("u5_l0", listOf(
            ScopeWord("anoche", "прошлой ночью", "u5_l0", "adverb"),
            ScopeWord("hace dos días", "2 дня назад", "u5_l0", "phrase"),
            ScopeWord("pizza", "пицца", "u5_l0", "food"),
        ))
        put("u5_l1", listOf(
            ScopeWord("hablé", "говорил (я)", "u5_l1", "verb"),
            ScopeWord("hablaste", "говорил (ты)", "u5_l1", "verb"),
            ScopeWord("habló", "говорил (он)", "u5_l1", "verb"),
            ScopeWord("hablaron", "говорили (они)", "u5_l1", "verb"),
            ScopeWord("trabajé", "работал (я)", "u5_l1", "verb"),
            ScopeWord("trabajaron", "работали (они)", "u5_l1", "verb"),
            ScopeWord("cantar", "петь", "u5_l1", "verb"),
            ScopeWord("cantaste", "пел (ты)", "u5_l1", "verb"),
        ))
        put("u5_l2", listOf(
            ScopeWord("comí", "ел (я)", "u5_l2", "verb"),
            ScopeWord("comiste", "ел (ты)", "u5_l2", "verb"),
            ScopeWord("comió", "ел (он)", "u5_l2", "verb"),
            ScopeWord("vivió", "жил (он)", "u5_l2", "verb"),
            ScopeWord("escribió", "писал (он)", "u5_l2", "verb"),
            ScopeWord("bebí", "пил (я)", "u5_l2", "verb"),
        ))
        put("u5_l3", listOf(
            ScopeWord("fui", "был / пошёл (я)", "u5_l3", "verb"),
            ScopeWord("estuve", "находился (я)", "u5_l3", "verb"),
        ))
        put("u5_l4", listOf(
            ScopeWord("primero", "сначала", "u5_l4", "adverb"),
            ScopeWord("después", "потом", "u5_l4", "adverb"),
            ScopeWord("luego", "затем", "u5_l4", "adverb"),
            ScopeWord("al final", "в конце", "u5_l4", "phrase"),
            ScopeWord("por la mañana", "утром", "u5_l4", "phrase"),
            ScopeWord("desayuné", "завтракал (я)", "u5_l4", "verb"),
            ScopeWord("hiciste", "ты сделал", "u5_l4", "verb"),
            ScopeWord("cené", "ужинал (я)", "u5_l4", "verb"),
        ))
        put("u5_l5", listOf(
            ScopeWord("compré", "купил (я)", "u5_l5", "verb"),
            ScopeWord("compraste", "купил (ты)", "u5_l5", "verb"),
            ScopeWord("compró", "купил (он)", "u5_l5", "verb"),
            ScopeWord("compramos", "купили (мы)", "u5_l5", "verb"),
            ScopeWord("vivieron", "жили (они)", "u5_l5", "verb"),
            ScopeWord("comieron", "ели (они)", "u5_l5", "verb"),
            ScopeWord("escribiste", "писал (ты)", "u5_l5", "verb"),
        ))
        put("u5_l6", listOf(
            ScopeWord("fuiste", "был / пошёл (ты)", "u5_l6", "verb"),
            ScopeWord("fue", "был / пошёл (он)", "u5_l6", "verb"),
            ScopeWord("fuimos", "были / пошли (мы)", "u5_l6", "verb"),
            ScopeWord("fueron", "были / пошли (они)", "u5_l6", "verb"),
        ))
        put("u5_l7", listOf(
            ScopeWord("tuve", "у меня было", "u5_l7", "verb"),
            ScopeWord("tuviste", "у тебя было", "u5_l7", "verb"),
            ScopeWord("tuvo", "у него было", "u5_l7", "verb"),
            ScopeWord("tuvimos", "у нас было", "u5_l7", "verb"),
            ScopeWord("tuvieron", "у них было", "u5_l7", "verb"),
            ScopeWord("estuviste", "ты находился", "u5_l7", "verb"),
            ScopeWord("estuvo", "он находился", "u5_l7", "verb"),
            ScopeWord("estuvieron", "они находились", "u5_l7", "verb"),
            ScopeWord("idea", "идея", "u5_l7", "noun"),
            ScopeWord("reunión", "встреча", "u5_l7", "noun"),
        ))
        put("u5_l8", listOf(
            ScopeWord("hice", "сделал (я)", "u5_l8", "verb"),
            ScopeWord("hiciste", "сделал (ты)", "u5_l8", "verb"),
            ScopeWord("hizo", "сделал (он)", "u5_l8", "verb"),
            ScopeWord("hicieron", "сделали (они)", "u5_l8", "verb"),
            ScopeWord("quise", "захотел (я)", "u5_l8", "verb"),
            ScopeWord("quisiste", "захотел (ты)", "u5_l8", "verb"),
            ScopeWord("tarea", "задание", "u5_l8", "noun"),
            ScopeWord("error", "ошибка", "u5_l8", "noun"),
        ))
        put("u5_l8_5", listOf(
            ScopeWord("había", "уже было (я)", "u5_l8_5", "verb"),
            ScopeWord("habías", "уже было (ты)", "u5_l8_5", "verb"),
            ScopeWord("habíamos", "уже было (мы)", "u5_l8_5", "verb"),
            ScopeWord("habían", "уже было (они)", "u5_l8_5", "verb"),
            ScopeWord("ya", "уже", "u5_l8_5", "adverb"),
            ScopeWord("ido", "ушедший", "u5_l8_5", "participle"),
            ScopeWord("visto", "виденный", "u5_l8_5", "participle"),
            ScopeWord("hecho", "сделанный", "u5_l8_5", "participle"),
            ScopeWord("película", "фильм", "u5_l8_5", "noun"),
        ))
        put("u5_l9", listOf(
            ScopeWord("para", "для / к (цель)", "u5_l9", "preposition"),
            ScopeWord("por", "за / по / через", "u5_l9", "preposition"),
            ScopeWord("aprobar", "сдать / одобрить", "u5_l9", "verb"),
            ScopeWord("regalo", "подарок", "u5_l9", "noun"),
            ScopeWord("vivir", "жить", "u5_l9", "verb"),
            ScopeWord("calle", "улица", "u5_l9", "noun"),
            ScopeWord("parque", "парк", "u5_l9", "noun"),
        ))
        put("u5_l10", listOf(
            ScopeWord("fin de semana", "выходные", "u5_l10", "phrase"),
            ScopeWord("pasé", "провёл", "u5_l10", "verb"),
            ScopeWord("paso", "провожу", "u5_l10", "verb"),
        ))
        put("u5_l11", listOf(
            ScopeWord("pude", "смог (я)", "u5_l11", "verb"),
            ScopeWord("pudiste", "смог (ты)", "u5_l11", "verb"),
            ScopeWord("pudo", "смог (он)", "u5_l11", "verb"),
            ScopeWord("pudieron", "смогли (они)", "u5_l11", "verb"),
            ScopeWord("supe", "узнал (я)", "u5_l11", "verb"),
            ScopeWord("supiste", "узнал (ты)", "u5_l11", "verb"),
            ScopeWord("supo", "узнал (он)", "u5_l11", "verb"),
            ScopeWord("verdad", "правда", "u5_l11", "noun"),
            ScopeWord("saber", "знать (факт)", "u5_l11", "verb"),
        ))
        put("u5_l12", listOf(
            ScopeWord("di", "дал (я)", "u5_l12", "verb"),
            ScopeWord("diste", "дал (ты)", "u5_l12", "verb"),
            ScopeWord("dio", "дал (он)", "u5_l12", "verb"),
            ScopeWord("dieron", "дали (они)", "u5_l12", "verb"),
            ScopeWord("vi", "видел (я)", "u5_l12", "verb"),
            ScopeWord("viste", "видел (ты)", "u5_l12", "verb"),
            ScopeWord("vio", "видел (он)", "u5_l12", "verb"),
            ScopeWord("vimos", "видели (мы)", "u5_l12", "verb"),
            ScopeWord("dije", "сказал (я)", "u5_l12", "verb"),
            ScopeWord("dijiste", "сказал (ты)", "u5_l12", "verb"),
            ScopeWord("dijo", "сказал (он)", "u5_l12", "verb"),
            ScopeWord("dijeron", "сказали (они)", "u5_l12", "verb"),
        ))
        put("u5_l13", listOf(
            ScopeWord("porque", "потому что", "u5_l13", "conjunction"),
            ScopeWord("entonces", "тогда", "u5_l13", "adverb"),
            ScopeWord("por eso", "поэтому", "u5_l13", "phrase"),
            ScopeWord("después de", "после того как", "u5_l13", "phrase"),
            ScopeWord("ocupado", "занятой", "u5_l13", "adjective"),
            ScopeWord("vine", "пришёл (я)", "u5_l13", "verb"),
            ScopeWord("viniste", "пришёл (ты)", "u5_l13", "verb"),
            ScopeWord("vino (verb)", "пришёл (он)", "u5_l13", "verb"),
        ))
        put("u5_l14", listOf(
            ScopeWord("estaban", "они находились (Imperfect)", "u5_l14", "verb"),
            ScopeWord("muy buena", "очень хорошая", "u5_l14", "phrase"),
            ScopeWord("comimos", "ели (мы)", "u5_l14", "verb"),
        ))

        // ═══════════════════════════════════════════════════════════════
        // A2 · Блок 2.2 «Раньше и сейчас» — Imperfecto + сравнения + OD/OI
        // ═══════════════════════════════════════════════════════════════
        put("u6_l0", listOf(
            ScopeWord("hablaba", "говорил (Imp)", "u6_l0", "verb"),
            ScopeWord("hablabas", "говорил ты", "u6_l0", "verb"),
            ScopeWord("hablábamos", "говорили мы", "u6_l0", "verb"),
            ScopeWord("hablaban", "говорили они", "u6_l0", "verb"),
            ScopeWord("trabajaba", "работал (Imp)", "u6_l0", "verb"),
            ScopeWord("estudiaba", "учился", "u6_l0", "verb"),
            ScopeWord("estudiabas", "учился ты", "u6_l0", "verb"),
            ScopeWord("antes", "раньше", "u6_l0", "adverb"),
            ScopeWord("a menudo", "часто", "u6_l0", "phrase"),
            ScopeWord("iba", "ходил (Imp)", "u6_l0", "verb"),
        ))
        put("u6_l1", listOf(
            ScopeWord("comía", "ел (Imp)", "u6_l1", "verb"),
            ScopeWord("comíamos", "ели (Imp)", "u6_l1", "verb"),
            ScopeWord("vivía", "жил (Imp)", "u6_l1", "verb"),
            ScopeWord("era", "был (Imp)", "u6_l1", "verb"),
            ScopeWord("eras", "был ты", "u6_l1", "verb"),
            ScopeWord("ibas", "ходил ты", "u6_l1", "verb"),
            ScopeWord("veía", "видел", "u6_l1", "verb"),
            ScopeWord("cada día", "каждый день", "u6_l1", "phrase"),
            ScopeWord("jugaba", "играл", "u6_l1", "verb"),
        ))
        put("u6_l2", listOf(
            ScopeWord("todos los días", "все дни", "u6_l2", "phrase"),
            ScopeWord("dormí", "спал (Indef)", "u6_l2", "verb"),
            ScopeWord("dormir", "спать", "u6_l2", "verb"),
            ScopeWord("mal", "плохо", "u6_l2", "adverb"),
        ))
        put("u6_l3", listOf(
            ScopeWord("teníamos", "имели мы (Imp)", "u6_l3", "verb"),
            ScopeWord("íbamos", "ходили мы (Imp)", "u6_l3", "verb"),
            ScopeWord("delgado", "худой", "u6_l3", "adjective"),
            ScopeWord("pueblo", "посёлок", "u6_l3", "noun"),
            ScopeWord("conocía", "знал (Imp)", "u6_l3", "verb"),
        ))
        put("u6_l4", listOf(
            ScopeWord("más", "больше", "u6_l4", "comparative"),
            ScopeWord("menos", "меньше", "u6_l4", "comparative"),
            ScopeWord("que", "чем (сравн.)", "u6_l4", "comparative"),
        ))
        put("u6_l5", listOf(
            ScopeWord("tan", "так / такой же", "u6_l5", "comparative"),
            ScopeWord("tanto", "столько (м)", "u6_l5", "comparative"),
            ScopeWord("tanta", "столько (ж)", "u6_l5", "comparative"),
            ScopeWord("tantas", "столько (ж.мн)", "u6_l5", "comparative"),
            ScopeWord("como", "как (сравн.)", "u6_l5", "comparative"),
            ScopeWord("bonita", "красивая", "u6_l5", "adjective"),
        ))
        put("u6_l6", listOf(
            ScopeWord("el más", "самый", "u6_l6", "superlative"),
            ScopeWord("el mejor", "лучший", "u6_l6", "superlative"),
            ScopeWord("la mejor", "лучшая", "u6_l6", "superlative"),
            ScopeWord("el peor", "худший", "u6_l6", "superlative"),
            ScopeWord("la peor", "худшая", "u6_l6", "superlative"),
            ScopeWord("mundo", "мир", "u6_l6", "noun"),
            ScopeWord("grupo", "группа", "u6_l6", "noun"),
        ))
        put("u6_l7", listOf(
            ScopeWord("simpático", "симпатичный", "u6_l7", "adjective"),
            ScopeWord("simpática", "симпатичная", "u6_l7", "adjective"),
            ScopeWord("listo", "умный", "u6_l7", "adjective"),
            ScopeWord("lista", "умная", "u6_l7", "adjective"),
            ScopeWord("guapo", "красивый", "u6_l7", "adjective"),
            ScopeWord("feo", "уродливый", "u6_l7", "adjective"),
            ScopeWord("joven", "молодой", "u6_l7", "adjective"),
            ScopeWord("viejo", "старый", "u6_l7", "adjective"),
        ))
        put("u6_l8", listOf(
            ScopeWord("lo", "его / это", "u6_l8", "pronoun"),
            ScopeWord("la", "её", "u6_l8", "pronoun"),
            ScopeWord("los", "их (м)", "u6_l8", "pronoun"),
            ScopeWord("las", "их (ж)", "u6_l8", "pronoun"),
            ScopeWord("verlo", "видеть его", "u6_l8", "verb"),
        ))
        put("u6_l9", listOf(
            ScopeWord("le", "ему/ей", "u6_l9", "pronoun"),
            ScopeWord("les", "им", "u6_l9", "pronoun"),
            ScopeWord("dieron", "дали", "u6_l9", "verb"),
        ))
        put("u6_l9_5", listOf(
            ScopeWord("se lo", "ему это", "u6_l9_5", "pronoun"),
            ScopeWord("se la", "ему её", "u6_l9_5", "pronoun"),
            ScopeWord("te lo", "тебе это", "u6_l9_5", "pronoun"),
            ScopeWord("doy", "даю", "u6_l9_5", "verb"),
            ScopeWord("mandar", "посылать", "u6_l9_5", "verb"),
            ScopeWord("mandé", "послал я", "u6_l9_5", "verb"),
            ScopeWord("mandó", "послал он", "u6_l9_5", "verb"),
        ))
        put("u6_l10", listOf(
            ScopeWord("hace tiempo que", "уже как", "u6_l10", "phrase"),
            ScopeWord("espero", "жду", "u6_l10", "verb"),
            ScopeWord("llamo", "звоню", "u6_l10", "verb"),
            ScopeWord("minutos", "минуты", "u6_l10", "noun"),
            ScopeWord("años", "годы", "u6_l10", "noun"),
        ))
        put("u6_l11", listOf(
            ScopeWord("probarse", "примерить", "u6_l11", "verb"),
            ScopeWord("probarme", "примерить мне", "u6_l11", "verb"),
            ScopeWord("queda", "сидит / остаётся", "u6_l11", "verb"),
            ScopeWord("queda bien", "хорошо сидит", "u6_l11", "phrase"),
            ScopeWord("queda mal", "плохо сидит", "u6_l11", "phrase"),
            ScopeWord("ajustado", "облегающий", "u6_l11", "adjective"),
            ScopeWord("ancho", "широкий", "u6_l11", "adjective"),
            ScopeWord("usa", "использует", "u6_l11", "verb"),
        ))
        put("u6_l12", listOf(
            ScopeWord("por la mañana", "утром", "u6_l12", "phrase"),
            ScopeWord("por teléfono", "по телефону", "u6_l12", "phrase"),
            ScopeWord("para siempre", "навсегда", "u6_l12", "phrase"),
            ScopeWord("para mí", "для меня", "u6_l12", "phrase"),
            ScopeWord("para ti", "для тебя", "u6_l12", "phrase"),
            ScopeWord("hablamos", "мы говорим", "u6_l12", "verb"),
            ScopeWord("email", "почта", "u6_l12", "noun"),
        ))
        put("u6_l13", listOf(
            ScopeWord("alegría", "радость", "u6_l13", "emotion"),
            ScopeWord("tristeza", "грусть", "u6_l13", "emotion"),
            ScopeWord("miedo", "страх", "u6_l13", "emotion"),
            ScopeWord("sorpresa", "удивление", "u6_l13", "emotion"),
            ScopeWord("enfado", "злость", "u6_l13", "emotion"),
            ScopeWord("triste", "грустный", "u6_l13", "adjective"),
            ScopeWord("asustado", "испуганный", "u6_l13", "adjective"),
            ScopeWord("sorprendido", "удивлённый", "u6_l13", "adjective"),
            ScopeWord("enfadado", "злой", "u6_l13", "adjective"),
            ScopeWord("asustada", "испуганная", "u6_l13", "adjective"),
        ))
        put("u6_l14", listOf(
            ScopeWord("abuelos", "дедушка и бабушка", "u6_l14", "family"),
        ))

        // ═══════════════════════════════════════════════════════════════
        // A2 · Блок 2.3 «Сейчас и скоро» — Perfecto + Imperativo + герундий
        // ═══════════════════════════════════════════════════════════════
        put("u7_l0", listOf(
            ScopeWord("he", "я (Perfecto)", "u7_l0", "verb"),
            ScopeWord("has", "ты (Perfecto)", "u7_l0", "verb"),
            ScopeWord("ha", "он (Perfecto)", "u7_l0", "verb"),
            ScopeWord("hemos", "мы (Perfecto)", "u7_l0", "verb"),
            ScopeWord("han", "они (Perfecto)", "u7_l0", "verb"),
            ScopeWord("comido", "съеденный", "u7_l0", "participle"),
            ScopeWord("hablado", "сказанный", "u7_l0", "participle"),
            ScopeWord("trabajado", "проработанный", "u7_l0", "participle"),
            ScopeWord("vivido", "прожитый", "u7_l0", "participle"),
            ScopeWord("terminado", "законченный", "u7_l0", "participle"),
        ))
        put("u7_l1", listOf(
            ScopeWord("dicho", "сказанный", "u7_l1", "participle"),
            ScopeWord("escrito", "написанный", "u7_l1", "participle"),
            ScopeWord("abierto", "открытый", "u7_l1", "participle"),
            ScopeWord("puesto", "положенный", "u7_l1", "participle"),
            ScopeWord("vuelto", "возвращённый", "u7_l1", "participle"),
            ScopeWord("muerto", "умерший", "u7_l1", "participle"),
            ScopeWord("puerta", "дверь", "u7_l1", "noun"),
        ))
        put("u7_l2", listOf(
            ScopeWord("esta semana", "на этой неделе", "u7_l2", "phrase"),
            ScopeWord("este año", "в этом году", "u7_l2", "phrase"),
            ScopeWord("la semana pasada", "на прошлой неделе", "u7_l2", "phrase"),
        ))
        put("u7_l3", listOf(
            ScopeWord("todavía", "ещё", "u7_l3", "adverb"),
            ScopeWord("aún", "ещё / до сих пор", "u7_l3", "adverb"),
            ScopeWord("alguna vez", "когда-нибудь", "u7_l3", "phrase"),
            ScopeWord("dos veces", "два раза", "u7_l3", "phrase"),
            ScopeWord("estado", "побывал (часть)", "u7_l3", "participle"),
            ScopeWord("París", "Париж", "u7_l3", "city"),
        ))
        put("u7_l4", listOf(
            ScopeWord("comiendo", "едящий", "u7_l4", "gerund"),
            ScopeWord("hablando", "говорящий", "u7_l4", "gerund"),
            ScopeWord("trabajando", "работающий", "u7_l4", "gerund"),
            ScopeWord("viviendo", "живущий", "u7_l4", "gerund"),
            ScopeWord("escribiendo", "пишущий", "u7_l4", "gerund"),
            ScopeWord("viendo", "видящий", "u7_l4", "gerund"),
        ))
        put("u7_l5", listOf(
            ScopeWord("sigo", "продолжаю", "u7_l5", "verb"),
            ScopeWord("seguir", "продолжать", "u7_l5", "verb"),
            ScopeWord("llevo", "у меня уже", "u7_l5", "verb"),
            ScopeWord("llevar", "нести / иметь время", "u7_l5", "verb"),
            ScopeWord("estudiando", "учащийся", "u7_l5", "gerund"),
            ScopeWord("horas", "часы", "u7_l5", "noun"),
        ))
        put("u7_l5_5", listOf(
            ScopeWord("haz", "сделай!", "u7_l5_5", "imperative"),
            ScopeWord("di", "скажи!", "u7_l5_5", "imperative"),
            ScopeWord("ven", "приди!", "u7_l5_5", "imperative"),
            ScopeWord("pon", "положи!", "u7_l5_5", "imperative"),
            ScopeWord("sal", "выйди!", "u7_l5_5", "imperative"),
            ScopeWord("ten", "имей!", "u7_l5_5", "imperative"),
            ScopeWord("ve", "иди!", "u7_l5_5", "imperative"),
            ScopeWord("sé", "будь!", "u7_l5_5", "imperative"),
        ))
        put("u7_l6", listOf(
            ScopeWord("buscar", "искать", "u7_l6", "verb"),
            ScopeWord("busco", "ищу", "u7_l6", "verb"),
            ScopeWord("buscas", "ищешь", "u7_l6", "verb"),
            ScopeWord("empleo", "работа", "u7_l6", "noun"),
            ScopeWord("currículum", "резюме", "u7_l6", "noun"),
            ScopeWord("entrevista", "собеседование", "u7_l6", "noun"),
            ScopeWord("contrato", "контракт", "u7_l6", "noun"),
            ScopeWord("sueldo", "зарплата", "u7_l6", "noun"),
            ScopeWord("jefe", "начальник", "u7_l6", "noun"),
            ScopeWord("empresa", "компания", "u7_l6", "noun"),
            ScopeWord("a tiempo completo", "полный день", "u7_l6", "phrase"),
            ScopeWord("buen", "хороший (м перед сущ)", "u7_l6", "adjective"),
        ))
        put("u7_l7", listOf(
            ScopeWord("habla", "говори!", "u7_l7", "imperative"),
            ScopeWord("come", "ешь!", "u7_l7", "imperative"),
            ScopeWord("escribe", "пиши!", "u7_l7", "imperative"),
            ScopeWord("trabaja", "работай!", "u7_l7", "imperative"),
            ScopeWord("abre", "открой!", "u7_l7", "imperative"),
            ScopeWord("verduras", "овощи", "u7_l7", "food"),
            ScopeWord("despacio", "медленно", "u7_l7", "adverb"),
            ScopeWord("así", "так", "u7_l7", "adverb"),
        ))
        put("u7_l8", listOf(
            ScopeWord("hables", "говори (Subj)", "u7_l8", "verb"),
            ScopeWord("comas", "ешь (Subj)", "u7_l8", "verb"),
            ScopeWord("escribas", "пиши (Subj)", "u7_l8", "verb"),
            ScopeWord("hagas", "делай (Subj)", "u7_l8", "verb"),
        ))
        put("u7_l9", listOf(
            ScopeWord("síntomas", "симптомы", "u7_l9", "health"),
            ScopeWord("tos", "кашель", "u7_l9", "health"),
            ScopeWord("dolor", "боль", "u7_l9", "health"),
            ScopeWord("receta", "рецепт", "u7_l9", "health"),
            ScopeWord("resfriado", "простужен", "u7_l9", "adjective"),
            ScopeWord("garganta", "горло", "u7_l9", "body"),
            ScopeWord("salud", "здоровье", "u7_l9", "health"),
        ))
        put("u7_l10", listOf(
            ScopeWord("enseño", "показываю / преподаю", "u7_l10", "verb"),
            ScopeWord("expliqué", "объяснил я", "u7_l10", "verb"),
            ScopeWord("explicar", "объяснять", "u7_l10", "verb"),
            ScopeWord("se las", "им их", "u7_l10", "pronoun"),
        ))
        put("u7_l11", listOf(
            ScopeWord("habitación", "номер / комната", "u7_l11", "noun"),
            ScopeWord("reserva", "бронь", "u7_l11", "noun"),
            ScopeWord("vuelo", "рейс", "u7_l11", "noun"),
            ScopeWord("equipaje", "багаж", "u7_l11", "noun"),
            ScopeWord("pasaporte", "паспорт", "u7_l11", "noun"),
            ScopeWord("aeropuerto", "аэропорт", "u7_l11", "place"),
            ScopeWord("hotel", "отель", "u7_l11", "place"),
            ScopeWord("reservar", "бронировать", "u7_l11", "verb"),
            ScopeWord("recepcionista", "администратор", "u7_l11", "noun"),
            ScopeWord("nombre", "имя", "u7_l11", "noun"),
        ))
        put("u7_l12", listOf(
            ScopeWord("creo", "думаю", "u7_l12", "verb"),
            ScopeWord("pienso", "считаю", "u7_l12", "verb"),
            ScopeWord("veo", "вижу", "u7_l12", "verb"),
            ScopeWord("es verdad", "это правда", "u7_l12", "phrase"),
        ))
        put("u7_l13", listOf(
            ScopeWord("tapas", "тапас", "u7_l13", "food"),
            ScopeWord("paella", "паэлья", "u7_l13", "food"),
            ScopeWord("tortilla española", "испанский омлет", "u7_l13", "food"),
            ScopeWord("gazpacho", "холодный суп", "u7_l13", "food"),
            ScopeWord("jamón", "хамон", "u7_l13", "food"),
            ScopeWord("sangría", "сангрия", "u7_l13", "food"),
            ScopeWord("churros", "чуррос", "u7_l13", "food"),
            ScopeWord("probar", "пробовать", "u7_l13", "verb"),
            ScopeWord("patatas", "картошка", "u7_l13", "food"),
        ))
        put("u7_l14", listOf(
            ScopeWord("ido", "ходил (part)", "u7_l14", "participle"),
            ScopeWord("vuelto", "вернулся", "u7_l14", "participle"),
            ScopeWord("tarde", "поздно", "u7_l14", "adverb"),
            ScopeWord("ocupado", "занятой", "u7_l14", "adjective"),
        ))

        // ═══════════════════════════════════════════════════════════════
        // A2 · Блок 2.4 «Мечты и планы» — Futuro + Condicional + ФИНАЛ A2
        // ═══════════════════════════════════════════════════════════════
        put("u8_l0", listOf(
            ScopeWord("hablaré", "буду говорить", "u8_l0", "verb"),
            ScopeWord("hablarás", "будешь говорить", "u8_l0", "verb"),
            ScopeWord("hablará", "будет говорить", "u8_l0", "verb"),
            ScopeWord("hablaremos", "будем говорить", "u8_l0", "verb"),
            ScopeWord("hablarán", "будут говорить", "u8_l0", "verb"),
            ScopeWord("comerás", "будешь есть", "u8_l0", "verb"),
            ScopeWord("trabajaré", "буду работать", "u8_l0", "verb"),
            ScopeWord("iremos", "поедем", "u8_l0", "verb"),
        ))
        put("u8_l1", listOf(
            ScopeWord("tendré", "буду иметь", "u8_l1", "verb"),
            ScopeWord("haré", "сделаю", "u8_l1", "verb"),
            ScopeWord("vendré", "приду", "u8_l1", "verb"),
            ScopeWord("diré", "скажу", "u8_l1", "verb"),
            ScopeWord("saldré", "выйду", "u8_l1", "verb"),
            ScopeWord("podré", "смогу", "u8_l1", "verb"),
            ScopeWord("podrán", "смогут", "u8_l1", "verb"),
            ScopeWord("sabré", "буду знать", "u8_l1", "verb"),
            ScopeWord("querré", "захочу", "u8_l1", "verb"),
        ))
        put("u8_l2", listOf(
            ScopeWord("hablaría", "говорил бы", "u8_l2", "verb"),
            ScopeWord("hablarías", "говорил бы (ты)", "u8_l2", "verb"),
            ScopeWord("hablaríamos", "говорили бы", "u8_l2", "verb"),
            ScopeWord("comerías", "ел бы (ты)", "u8_l2", "verb"),
            ScopeWord("iría", "пошёл бы", "u8_l2", "verb"),
            ScopeWord("invitaría", "пригласил бы", "u8_l2", "verb"),
            ScopeWord("querría", "хотел бы", "u8_l2", "verb"),
        ))
        put("u8_l3", listOf(
            ScopeWord("tendría", "имел бы", "u8_l3", "verb"),
            ScopeWord("haría", "сделал бы", "u8_l3", "verb"),
            ScopeWord("vendría", "пришёл бы", "u8_l3", "verb"),
            ScopeWord("diría", "сказал бы", "u8_l3", "verb"),
            ScopeWord("podría", "мог бы", "u8_l3", "verb"),
            ScopeWord("ayudarme", "помочь мне", "u8_l3", "verb"),
        ))
        put("u8_l4", listOf(
            ScopeWord("si", "если", "u8_l4", "conjunction"),
            ScopeWord("vengo", "приду я", "u8_l4", "verb"),
            ScopeWord("invito", "приглашаю", "u8_l4", "verb"),
            ScopeWord("saldré", "выйду", "u8_l4", "verb"),
            ScopeWord("llámame", "позвони мне", "u8_l4", "imperative"),
        ))
        put("u8_l5", listOf(
            ScopeWord("quisiera", "хотел бы", "u8_l5", "verb"),
            ScopeWord("me gustaría", "мне бы хотелось", "u8_l5", "phrase"),
            ScopeWord("espero", "надеюсь", "u8_l5", "verb"),
            ScopeWord("voy a", "собираюсь", "u8_l5", "phrase"),
            ScopeWord("aprender", "учиться", "u8_l5", "verb"),
            ScopeWord("chino", "китайский", "u8_l5", "language"),
        ))
        put("u8_l6", listOf(
            ScopeWord("algo", "что-то", "u8_l6", "pronoun"),
            ScopeWord("alguien", "кто-то", "u8_l6", "pronoun"),
            ScopeWord("ningún", "ни один (м.перед)", "u8_l6", "pronoun"),
            ScopeWord("ninguno", "ни один", "u8_l6", "pronoun"),
            ScopeWord("alguno", "какой-то", "u8_l6", "pronoun"),
            ScopeWord("todo", "всё / весь", "u8_l6", "pronoun"),
            ScopeWord("todos", "все", "u8_l6", "pronoun"),
            ScopeWord("varios", "несколько", "u8_l6", "pronoun"),
            ScopeWord("bueno", "хороший", "u8_l6", "adjective"),
        ))
        put("u8_l7", listOf(
            ScopeWord("probablemente", "вероятно", "u8_l7", "adverb"),
            ScopeWord("quizás", "может быть", "u8_l7", "adverb"),
            ScopeWord("a lo mejor", "может быть (разг)", "u8_l7", "phrase"),
            ScopeWord("tal vez", "возможно", "u8_l7", "phrase"),
            ScopeWord("seguro que", "точно (что)", "u8_l7", "phrase"),
            ScopeWord("venga", "придёт (Subj)", "u8_l7", "verb"),
        ))
        put("u8_l8", listOf(
            ScopeWord("alquilar", "арендовать", "u8_l8", "verb"),
            ScopeWord("conducir", "водить", "u8_l8", "verb"),
            ScopeWord("aparcar", "парковать", "u8_l8", "verb"),
            ScopeWord("gasolina", "бензин", "u8_l8", "noun"),
            ScopeWord("autopista", "автомагистраль", "u8_l8", "noun"),
            ScopeWord("tráfico", "движение", "u8_l8", "noun"),
            ScopeWord("atasco", "пробка", "u8_l8", "noun"),
            ScopeWord("carnet", "права", "u8_l8", "noun"),
        ))
        put("u8_l9", listOf(
            ScopeWord("pensar en", "думать о", "u8_l9", "phrase"),
            ScopeWord("soñar con", "мечтать о", "u8_l9", "phrase"),
            ScopeWord("enamorarse de", "влюбиться в", "u8_l9", "phrase"),
            ScopeWord("casarse con", "жениться на", "u8_l9", "phrase"),
            ScopeWord("depender de", "зависеть от", "u8_l9", "phrase"),
            ScopeWord("preocuparse por", "волноваться о", "u8_l9", "phrase"),
            ScopeWord("sueño", "я мечтаю", "u8_l9", "verb"),
            ScopeWord("depende", "зависит", "u8_l9", "verb"),
        ))
        put("u8_l10", listOf(
            ScopeWord("campo", "поле / деревня", "u8_l10", "place"),
            ScopeWord("mar", "море", "u8_l10", "place"),
            ScopeWord("montaña", "гора", "u8_l10", "place"),
            ScopeWord("bosque", "лес", "u8_l10", "place"),
            ScopeWord("lago", "озеро", "u8_l10", "place"),
            ScopeWord("río", "река", "u8_l10", "place"),
            ScopeWord("playa", "пляж", "u8_l10", "place"),
            ScopeWord("verano", "лето", "u8_l10", "season"),
            ScopeWord("sobre todo", "особенно", "u8_l10", "phrase"),
        ))
        put("u8_l11", listOf(
            ScopeWord("mucho", "много (м)", "u8_l11", "quantifier"),
            ScopeWord("mucha", "много (ж)", "u8_l11", "quantifier"),
            ScopeWord("poco", "мало (м)", "u8_l11", "quantifier"),
            ScopeWord("bastante", "достаточно", "u8_l11", "quantifier"),
            ScopeWord("demasiado", "слишком", "u8_l11", "quantifier"),
            ScopeWord("hambre", "голод", "u8_l11", "noun"),
        ))
        put("u8_l12", listOf(
            ScopeWord("app", "приложение", "u8_l12", "tech"),
            ScopeWord("aplicación", "приложение", "u8_l12", "tech"),
            ScopeWord("wifi", "вайфай", "u8_l12", "tech"),
            ScopeWord("contraseña", "пароль", "u8_l12", "tech"),
            ScopeWord("descargar", "загружать", "u8_l12", "verb"),
            ScopeWord("subir", "выгружать", "u8_l12", "verb"),
            ScopeWord("correo electrónico", "э-почта", "u8_l12", "tech"),
            ScopeWord("ordenador", "компьютер", "u8_l12", "tech"),
            ScopeWord("móvil", "мобильник", "u8_l12", "tech"),
            ScopeWord("computador", "комп (Латам)", "u8_l12", "tech"),
        ))
        put("u8_l13", listOf(
            ScopeWord("hacer ejercicio", "заниматься", "u8_l13", "phrase"),
            ScopeWord("correr", "бегать", "u8_l13", "verb"),
            ScopeWord("corro", "я бегаю", "u8_l13", "verb"),
            ScopeWord("gimnasio", "тренажёрный зал", "u8_l13", "place"),
            ScopeWord("dieta", "диета", "u8_l13", "noun"),
            ScopeWord("forma", "форма", "u8_l13", "noun"),
            ScopeWord("yoga", "йога", "u8_l13", "noun"),
            ScopeWord("natación", "плавание", "u8_l13", "noun"),
            ScopeWord("perder peso", "худеть", "u8_l13", "phrase"),
            ScopeWord("deporte", "спорт", "u8_l13", "noun"),
            ScopeWord("ejercicio", "упражнение", "u8_l13", "noun"),
        ))
        put("u8_l14", listOf(
            ScopeWord("Barcelona", "Барселона", "u8_l14", "city"),
            ScopeWord("visitaremos", "посетим", "u8_l14", "verb"),
            ScopeWord("año próximo", "следующий год", "u8_l14", "phrase"),
            ScopeWord("familia", "семья", "u8_l14", "noun"),
        ))

        // ═══════════════════════════════════════════════════════════════
        // B1 · Блок 3.1 «Subjuntivo» — сослагательное наклонение
        // ═══════════════════════════════════════════════════════════════
        put("u9_l0", listOf(
            ScopeWord("subjuntivo", "сослагательное", "u9_l0", "concept"),
        ))
        put("u9_l1", listOf(
            ScopeWord("hable", "(чтобы) говорил", "u9_l1", "verb"),
            ScopeWord("hables", "(чтобы) ты говорил", "u9_l1", "verb"),
            ScopeWord("hablen", "(чтобы) они говорили", "u9_l1", "verb"),
            ScopeWord("trabajes", "(чтобы) ты работал", "u9_l1", "verb"),
            ScopeWord("trabajéis", "(чтобы) вы работали", "u9_l1", "verb"),
            ScopeWord("hablemos", "(чтобы) мы говорили", "u9_l1", "verb"),
        ))
        put("u9_l2", listOf(
            ScopeWord("coma", "(чтобы) ел", "u9_l2", "verb"),
            ScopeWord("comas", "(чтобы) ты ел", "u9_l2", "verb"),
            ScopeWord("vivan", "(чтобы) жили", "u9_l2", "verb"),
            ScopeWord("escriba", "(чтобы) писал", "u9_l2", "verb"),
            ScopeWord("felices", "счастливые", "u9_l2", "adjective"),
        ))
        put("u9_l3", listOf(
            ScopeWord("sea", "(чтобы) был", "u9_l3", "verb"),
            ScopeWord("vaya", "(чтобы) шёл", "u9_l3", "verb"),
            ScopeWord("vayas", "(чтобы) ты шёл", "u9_l3", "verb"),
            ScopeWord("vayamos", "(чтобы) мы шли", "u9_l3", "verb"),
            ScopeWord("esté", "(чтобы) был", "u9_l3", "verb"),
            ScopeWord("estés", "(чтобы) ты был", "u9_l3", "verb"),
            ScopeWord("dé", "(чтобы) дал", "u9_l3", "verb"),
            ScopeWord("des", "(чтобы) ты дал", "u9_l3", "verb"),
            ScopeWord("sepa", "(чтобы) знал", "u9_l3", "verb"),
            ScopeWord("haya", "(чтобы) было/имел", "u9_l3", "verb"),
            ScopeWord("vea", "(чтобы) видел", "u9_l3", "verb"),
        ))
        put("u9_l4", listOf(
            ScopeWord("quiera", "(чтобы) хотел", "u9_l4", "verb"),
            ScopeWord("quieras", "(чтобы) ты хотел", "u9_l4", "verb"),
            ScopeWord("quieran", "(чтобы) хотели", "u9_l4", "verb"),
            ScopeWord("pueda", "(чтобы) мог", "u9_l4", "verb"),
            ScopeWord("puedas", "(чтобы) ты мог", "u9_l4", "verb"),
            ScopeWord("venga", "(чтобы) пришёл", "u9_l4", "verb"),
            ScopeWord("vengas", "(чтобы) ты пришёл", "u9_l4", "verb"),
            ScopeWord("vengan", "(чтобы) пришли", "u9_l4", "verb"),
        ))
        put("u9_l5", listOf(
            ScopeWord("Quiero que", "Хочу чтобы", "u9_l5", "phrase"),
            ScopeWord("fiesta", "вечеринка", "u9_l5", "noun"),
            ScopeWord("te quedes", "(чтобы) ты остался", "u9_l5", "verb"),
            ScopeWord("quedarse", "оставаться", "u9_l5", "verb"),
        ))
        put("u9_l6", listOf(
            ScopeWord("necesitar", "нуждаться", "u9_l6", "verb"),
            ScopeWord("necesito", "мне нужно", "u9_l6", "verb"),
            ScopeWord("ayudes", "(чтобы) ты помог", "u9_l6", "verb"),
            ScopeWord("escuches", "(чтобы) ты слушал", "u9_l6", "verb"),
            ScopeWord("ayuda", "помощь", "u9_l6", "noun"),
        ))
        put("u9_l7", listOf(
            ScopeWord("Es importante que", "важно чтобы", "u9_l7", "phrase"),
            ScopeWord("Es necesario que", "необходимо чтобы", "u9_l7", "phrase"),
            ScopeWord("Es bueno que", "хорошо что", "u9_l7", "phrase"),
            ScopeWord("Es mejor que", "лучше чтобы", "u9_l7", "phrase"),
            ScopeWord("Es posible que", "возможно что", "u9_l7", "phrase"),
            ScopeWord("llueva", "(чтобы) был дождь", "u9_l7", "verb"),
            ScopeWord("esperes", "(чтобы) ты ждал", "u9_l7", "verb"),
        ))
        put("u9_l8", listOf(
            ScopeWord("Me alegra que", "рад что", "u9_l8", "phrase"),
            ScopeWord("Temo que", "боюсь что", "u9_l8", "phrase"),
            ScopeWord("Siento que", "сожалею что", "u9_l8", "phrase"),
            ScopeWord("vinieras", "(чтобы) ты пришёл (Imp)", "u9_l8", "verb"),
            ScopeWord("nervioso", "нервный", "u9_l8", "adjective"),
        ))
        put("u9_l9", listOf(
            ScopeWord("dudo", "сомневаюсь", "u9_l9", "verb"),
            ScopeWord("dudar", "сомневаться", "u9_l9", "verb"),
            ScopeWord("No creo que", "не думаю что", "u9_l9", "phrase"),
            ScopeWord("niego", "отрицаю", "u9_l9", "verb"),
            ScopeWord("cierto", "верный/истинный", "u9_l9", "adjective"),
        ))
        put("u9_l10", listOf(
            ScopeWord("ojalá", "вот бы / дай Бог", "u9_l10", "particle"),
            ScopeWord("tengas", "(чтобы) ты имел", "u9_l10", "verb"),
            ScopeWord("suerte", "удача", "u9_l10", "noun"),
        ))
        put("u9_l11", listOf(
            ScopeWord("para que", "чтобы (цель)", "u9_l11", "phrase"),
            ScopeWord("sepas", "(чтобы) ты знал", "u9_l11", "verb"),
            ScopeWord("entiendas", "(чтобы) ты понял", "u9_l11", "verb"),
            ScopeWord("entender", "понимать", "u9_l11", "verb"),
            ScopeWord("aprenda", "(чтобы) учил", "u9_l11", "verb"),
        ))
        put("u9_l11_5", listOf(
            ScopeWord("antes de que", "прежде чем (2 субъекта)", "u9_l11_5", "phrase"),
            ScopeWord("antes de", "перед (1 субъект+inf)", "u9_l11_5", "phrase"),
            ScopeWord("salgas", "(чтобы) ты вышел", "u9_l11_5", "verb"),
        ))
        put("u9_l12", listOf(
            ScopeWord("cuando", "когда", "u9_l12", "conjunction"),
            ScopeWord("veremos", "увидим (Fut)", "u9_l12", "verb"),
        ))
        put("u9_l13", listOf(
            ScopeWord("aunque", "хотя / даже если", "u9_l13", "conjunction"),
            ScopeWord("difícil", "трудный", "u9_l13", "adjective"),
            ScopeWord("intentaré", "попробую", "u9_l13", "verb"),
        ))
        put("u9_l14", listOf(
            ScopeWord("aconsejo", "советую", "u9_l14", "verb"),
            ScopeWord("aconsejar", "советовать", "u9_l14", "verb"),
            ScopeWord("éxito", "успех", "u9_l14", "noun"),
            ScopeWord("consigas", "(чтобы) ты добился", "u9_l14", "verb"),
            ScopeWord("conseguir", "добиваться", "u9_l14", "verb"),
            ScopeWord("tranquila", "спокойная", "u9_l14", "adjective"),
        ))

        // ═══════════════════════════════════════════════════════════════
        // B1 · Блок 3.2 «Condicional» — гипотезы, советы, вежливость
        // ═══════════════════════════════════════════════════════════════
        put("u10_l0", listOf(
            ScopeWord("comería", "ел бы", "u10_l0", "verb"),
            ScopeWord("hablaría", "говорил бы", "u10_l0", "verb"),
        ))
        put("u10_l1", listOf(
            ScopeWord("hablarías", "говорил бы (ты)", "u10_l1", "verb"),
            ScopeWord("hablaríamos", "говорили бы", "u10_l1", "verb"),
            ScopeWord("hablarían", "говорили бы (они)", "u10_l1", "verb"),
            ScopeWord("trabajaría", "работал бы", "u10_l1", "verb"),
            ScopeWord("trabajarían", "работали бы", "u10_l1", "verb"),
            ScopeWord("estudiarías", "учился бы", "u10_l1", "verb"),
        ))
        put("u10_l2", listOf(
            ScopeWord("comerías", "ел бы (ты)", "u10_l2", "verb"),
            ScopeWord("comeríamos", "ели бы", "u10_l2", "verb"),
            ScopeWord("viviría", "жил бы", "u10_l2", "verb"),
            ScopeWord("vivirían", "жили бы", "u10_l2", "verb"),
            ScopeWord("beberían", "пили бы", "u10_l2", "verb"),
        ))
        put("u10_l3", listOf(
            ScopeWord("habría", "имел бы (вспом)", "u10_l3", "verb"),
            ScopeWord("pondría", "положил бы", "u10_l3", "verb"),
            ScopeWord("podrías", "мог бы (ты)", "u10_l3", "verb"),
            ScopeWord("sabría", "знал бы", "u10_l3", "verb"),
        ))
        put("u10_l4", listOf(
            ScopeWord("haría", "сделал бы", "u10_l4", "verb"),
            ScopeWord("querría", "хотел бы (Cond)", "u10_l4", "verb"),
            ScopeWord("vendría", "пришёл бы", "u10_l4", "verb"),
            ScopeWord("saldría", "вышел бы", "u10_l4", "verb"),
            ScopeWord("diría", "сказал бы", "u10_l4", "verb"),
            ScopeWord("posible", "возможный", "u10_l4", "adjective"),
        ))
        put("u10_l5", listOf(
            ScopeWord("Si", "если", "u10_l5", "conjunction"),
            ScopeWord("vienes", "приходишь", "u10_l5", "verb"),
            ScopeWord("comeré", "поем", "u10_l5", "verb"),
        ))
        put("u10_l6", listOf(
            ScopeWord("hablara", "(чтобы) говорил (Imp Subj)", "u10_l6", "verb"),
            ScopeWord("hablase", "(чтобы) говорил (Imp Subj 2)", "u10_l6", "verb"),
            ScopeWord("tuviera", "(чтобы) имел", "u10_l6", "verb"),
            ScopeWord("quisiera", "хотел бы (вежл)", "u10_l6", "verb"),
        ))
        put("u10_l7", listOf(
            ScopeWord("hablaras", "(чтобы) ты говорил", "u10_l7", "verb"),
            ScopeWord("habláramos", "(чтобы) мы говорили", "u10_l7", "verb"),
            ScopeWord("hablaran", "(чтобы) говорили", "u10_l7", "verb"),
            ScopeWord("comiera", "(чтобы) ел", "u10_l7", "verb"),
            ScopeWord("vivieran", "(чтобы) жили", "u10_l7", "verb"),
            ScopeWord("vivieras", "(чтобы) ты жил", "u10_l7", "verb"),
            ScopeWord("trabajara", "(чтобы) работал", "u10_l7", "verb"),
        ))
        put("u10_l8", listOf(
            ScopeWord("fuera", "(чтобы) был / шёл (Imp Subj)", "u10_l8", "verb"),
            ScopeWord("fueras", "(чтобы) ты был", "u10_l8", "verb"),
            ScopeWord("hiciera", "(чтобы) сделал", "u10_l8", "verb"),
            ScopeWord("dijera", "(чтобы) сказал", "u10_l8", "verb"),
            ScopeWord("estuviera", "(чтобы) находился", "u10_l8", "verb"),
            ScopeWord("pudiera", "(чтобы) мог", "u10_l8", "verb"),
            ScopeWord("supiera", "(чтобы) знал", "u10_l8", "verb"),
        ))
        put("u10_l9", listOf(
            ScopeWord("viajaría", "путешествовал бы", "u10_l9", "verb"),
            ScopeWord("rico", "богатый", "u10_l9", "adjective"),
            ScopeWord("millón", "миллион", "u10_l9", "noun"),
        ))
        put("u10_l10", listOf(
            ScopeWord("en tu lugar", "на твоём месте", "u10_l10", "phrase"),
            ScopeWord("Yo que tú", "я бы на твоём месте", "u10_l10", "phrase"),
            ScopeWord("recomiendo", "советую", "u10_l10", "verb"),
            ScopeWord("recomendar", "советовать", "u10_l10", "verb"),
            ScopeWord("debería", "следовало бы", "u10_l10", "verb"),
            ScopeWord("deberías", "тебе следовало бы", "u10_l10", "verb"),
            ScopeWord("me quedaría", "я бы остался", "u10_l10", "verb"),
        ))
        put("u10_l11", listOf(
            ScopeWord("¿Te importaría?", "не возражаешь?", "u10_l11", "phrase"),
            ScopeWord("¿Podrías?", "не мог бы?", "u10_l11", "phrase"),
            ScopeWord("¿Sería posible?", "возможно ли?", "u10_l11", "phrase"),
            ScopeWord("Me gustaría", "мне бы хотелось", "u10_l11", "phrase"),
            ScopeWord("cerrar", "закрывать", "u10_l11", "verb"),
            ScopeWord("esperar", "ждать", "u10_l11", "verb"),
        ))
        put("u10_l12", listOf(
            ScopeWord("vinieras", "(чтобы) ты пришёл", "u10_l12", "verb"),
            ScopeWord("estudiaras", "(чтобы) ты учился", "u10_l12", "verb"),
            ScopeWord("te quedaras", "(чтобы) ты остался", "u10_l12", "verb"),
        ))
        put("u10_l13", listOf(
            ScopeWord("futuro", "будущее", "u10_l13", "noun"),
        ))
        put("u10_l14", listOf(
            ScopeWord("ayudaría", "помог бы", "u10_l14", "verb"),
            ScopeWord("te ayudaría", "я бы тебе помог", "u10_l14", "verb"),
        ))

        // ═══════════════════════════════════════════════════════════════
        // B1 · Блок 3.3 «Коммуникация» — косв.речь + relativos + perífrasis
        // ═══════════════════════════════════════════════════════════════
        put("u11_l0", listOf(
            ScopeWord("estilo indirecto", "косвенная речь", "u11_l0", "concept"),
            ScopeWord("iba", "шёл (Imp)", "u11_l0", "verb"),
            ScopeWord("quería", "хотел (Imp)", "u11_l0", "verb"),
            ScopeWord("hablaba", "говорил (Imp)", "u11_l0", "verb"),
        ))
        put("u11_l1", listOf(
            ScopeWord("dijo", "сказал", "u11_l1", "verb"),
            ScopeWord("preguntó", "спросил", "u11_l1", "verb"),
            ScopeWord("estaba", "находился (Imp)", "u11_l1", "verb"),
        ))
        put("u11_l2", listOf(
            ScopeWord("había ido", "уже сходил", "u11_l2", "verb"),
            ScopeWord("habría", "имел бы (Cond)", "u11_l2", "verb"),
        ))
        put("u11_l3", listOf(
            ScopeWord("pidió", "попросил", "u11_l3", "verb"),
            ScopeWord("mandó", "велел", "u11_l3", "verb"),
            ScopeWord("sugerió", "предложил", "u11_l3", "verb"),
            ScopeWord("ayudara", "(чтобы) помог (Imp Subj)", "u11_l3", "verb"),
            ScopeWord("estudiaran", "(чтобы) учились", "u11_l3", "verb"),
            ScopeWord("callaran", "(чтобы) замолчали", "u11_l3", "verb"),
            ScopeWord("esperáramos", "(чтобы) мы подождали", "u11_l3", "verb"),
        ))
        put("u11_l4", listOf(
            ScopeWord("quien", "кто/который", "u11_l4", "relative"),
            ScopeWord("donde", "где", "u11_l4", "relative"),
            ScopeWord("cuando (rel)", "когда (отн)", "u11_l4", "relative"),
            ScopeWord("interesante", "интересный", "u11_l4", "adjective"),
        ))
        put("u11_l5", listOf(
            ScopeWord("cuyo", "чей (м)", "u11_l5", "relative"),
            ScopeWord("cuya", "чья", "u11_l5", "relative"),
            ScopeWord("el cual", "который (форм)", "u11_l5", "relative"),
            ScopeWord("la cual", "которая (форм)", "u11_l5", "relative"),
            ScopeWord("lo cual", "что (всё пред)", "u11_l5", "relative"),
        ))
        put("u11_l5_5", listOf(
            ScopeWord("lo bueno", "хорошее", "u11_l5_5", "phrase"),
            ScopeWord("lo importante", "важное", "u11_l5_5", "phrase"),
            ScopeWord("lo difícil", "трудное", "u11_l5_5", "phrase"),
            ScopeWord("lo mejor", "лучшее", "u11_l5_5", "phrase"),
            ScopeWord("lo peor", "худшее", "u11_l5_5", "phrase"),
            ScopeWord("lo mío", "моё (дело)", "u11_l5_5", "phrase"),
        ))
        put("u11_l6", listOf(
            ScopeWord("escrito", "написанный", "u11_l6", "participle"),
            ScopeWord("enviada", "отправленная", "u11_l6", "participle"),
            ScopeWord("construidas", "построенные", "u11_l6", "participle"),
            ScopeWord("director", "директор", "u11_l6", "noun"),
            ScopeWord("autor", "автор", "u11_l6", "noun"),
        ))
        put("u11_l7", listOf(
            ScopeWord("hecho (part)", "сделанный", "u11_l7", "participle"),
            ScopeWord("hecha", "сделанная", "u11_l7", "participle"),
            ScopeWord("abierta", "открытая", "u11_l7", "participle"),
            ScopeWord("cerrada", "закрытая", "u11_l7", "participle"),
            ScopeWord("cerrado", "закрытый", "u11_l7", "participle"),
        ))
        put("u11_l8", listOf(
            ScopeWord("esperándote", "жду тебя (ger+pron)", "u11_l8", "verb"),
        ))
        put("u11_l9", listOf(
            ScopeWord("continuar", "продолжать", "u11_l9", "verb"),
            ScopeWord("continúa", "продолжает", "u11_l9", "verb"),
            ScopeWord("aprendiendo", "учащийся", "u11_l9", "gerund"),
            ScopeWord("lloviendo", "идущий дождь", "u11_l9", "gerund"),
        ))
        put("u11_l10", listOf(
            ScopeWord("acabar de", "только что", "u11_l10", "phrase"),
            ScopeWord("acabo de", "только что я", "u11_l10", "phrase"),
            ScopeWord("acaba de", "только что он", "u11_l10", "phrase"),
            ScopeWord("volver a", "снова", "u11_l10", "phrase"),
            ScopeWord("vuelvo a", "снова я", "u11_l10", "phrase"),
            ScopeWord("vuelve a", "снова он", "u11_l10", "phrase"),
        ))
        put("u11_l11", listOf(
            ScopeWord("sin embargo", "однако", "u11_l11", "connector"),
            ScopeWord("por lo tanto", "поэтому", "u11_l11", "connector"),
            ScopeWord("además", "кроме того", "u11_l11", "connector"),
            ScopeWord("por otro lado", "с другой стороны", "u11_l11", "connector"),
            ScopeWord("en cambio", "напротив", "u11_l11", "connector"),
            ScopeWord("aprueba", "сдаёт", "u11_l11", "verb"),
        ))
        put("u11_l12", listOf(
            ScopeWord("a pesar de", "несмотря на", "u11_l12", "phrase"),
            ScopeWord("a pesar de que", "несмотря на то что", "u11_l12", "phrase"),
            ScopeWord("lluvia", "дождь", "u11_l12", "weather"),
        ))
        put("u11_l13", listOf(
            ScopeWord("en definitiva", "в итоге", "u11_l13", "connector"),
            ScopeWord("en resumen", "короче", "u11_l13", "connector"),
            ScopeWord("es decir", "то есть", "u11_l13", "connector"),
            ScopeWord("por ejemplo", "например", "u11_l13", "connector"),
            ScopeWord("en otras palabras", "другими словами", "u11_l13", "connector"),
            ScopeWord("dentro de", "через (время)", "u11_l13", "phrase"),
            ScopeWord("salimos", "выходим/выйдем", "u11_l13", "verb"),
        ))
        put("u11_l14", listOf(
            ScopeWord("periodista", "журналист", "u11_l14", "profession"),
            ScopeWord("contratar", "нанимать", "u11_l14", "verb"),
            ScopeWord("contrataría", "наняли бы", "u11_l14", "verb"),
            ScopeWord("crecido", "выросший", "u11_l14", "participle"),
            ScopeWord("personal", "персонал", "u11_l14", "noun"),
            ScopeWord("terminar", "заканчивать", "u11_l14", "verb"),
        ))

        // ═══════════════════════════════════════════════════════════════
        // B1 · Блок 3.4 «Словарь и стиль» — лексика B1 + идиомы + регистр
        // ═══════════════════════════════════════════════════════════════
        put("u12_l0", listOf(
            ScopeWord("contrato indefinido", "бессрочный контракт", "u12_l0", "work"),
            ScopeWord("plantilla", "штат", "u12_l0", "work"),
            ScopeWord("baja", "больничный", "u12_l0", "work"),
            ScopeWord("finiquito", "выплата при увольнении", "u12_l0", "work"),
            ScopeWord("jubilarse", "уйти на пенсию", "u12_l0", "verb"),
            ScopeWord("sindicato", "профсоюз", "u12_l0", "work"),
        ))
        put("u12_l1", listOf(
            ScopeWord("Estimado", "уважаемый", "u12_l1", "formal"),
            ScopeWord("Adjunto", "прилагаю", "u12_l1", "formal"),
            ScopeWord("agradezco", "благодарю", "u12_l1", "verb"),
            ScopeWord("Atentamente", "с уважением", "u12_l1", "formal"),
            ScopeWord("Saludos cordiales", "с наилучшими", "u12_l1", "formal"),
            ScopeWord("Quedo a su disposición", "остаюсь в Вашем распоряжении", "u12_l1", "formal"),
            ScopeWord("CV", "резюме", "u12_l1", "noun"),
            ScopeWord("envío", "отправляю", "u12_l1", "verb"),
        ))
        put("u12_l2", listOf(
            ScopeWord("noticia", "новость", "u12_l2", "media"),
            ScopeWord("reportaje", "репортаж", "u12_l2", "media"),
            ScopeWord("editorial", "редакционная", "u12_l2", "media"),
            ScopeWord("periódico", "газета", "u12_l2", "media"),
            ScopeWord("prensa", "пресса", "u12_l2", "media"),
            ScopeWord("telediario", "теленовости", "u12_l2", "media"),
            ScopeWord("titular", "заголовок", "u12_l2", "media"),
            ScopeWord("impactante", "впечатляющий", "u12_l2", "adjective"),
        ))
        put("u12_l3", listOf(
            ScopeWord("publicar", "публиковать", "u12_l3", "verb"),
            ScopeWord("comentar", "комментир.", "u12_l3", "verb"),
            ScopeWord("compartir", "делиться", "u12_l3", "verb"),
            ScopeWord("seguidor", "подписчик", "u12_l3", "noun"),
            ScopeWord("seguidores", "подписчики", "u12_l3", "noun"),
            ScopeWord("hashtag", "хэштег", "u12_l3", "tech"),
            ScopeWord("viral", "вирусный", "u12_l3", "adjective"),
            ScopeWord("dar like", "лайкнуть", "u12_l3", "phrase"),
            ScopeWord("foto", "фото", "u12_l3", "noun"),
        ))
        put("u12_l4", listOf(
            ScopeWord("síntoma", "симптом", "u12_l4", "health"),
            ScopeWord("síntomas", "симптомы", "u12_l4", "health"),
            ScopeWord("diagnóstico", "диагноз", "u12_l4", "health"),
            ScopeWord("urgencias", "скорая", "u12_l4", "health"),
            ScopeWord("consulta", "приём", "u12_l4", "health"),
            ScopeWord("ingreso", "госпитализ.", "u12_l4", "health"),
            ScopeWord("operar", "оперировать", "u12_l4", "verb"),
            ScopeWord("cirugía", "операция", "u12_l4", "health"),
        ))
        put("u12_l5", listOf(
            ScopeWord("mareado", "головокружение", "u12_l5", "adjective"),
            ScopeWord("toser", "кашлять", "u12_l5", "verb"),
            ScopeWord("toso", "я кашляю", "u12_l5", "verb"),
            ScopeWord("sentirse", "чувствовать себя", "u12_l5", "verb"),
            ScopeWord("siento", "чувствую", "u12_l5", "verb"),
            ScopeWord("siente", "чувствует", "u12_l5", "verb"),
            ScopeWord("reposo", "отдых", "u12_l5", "noun"),
            ScopeWord("alta", "высокая", "u12_l5", "adjective"),
        ))
        put("u12_l6", listOf(
            ScopeWord("dar igual", "всё равно", "u12_l6", "idiom"),
            ScopeWord("dar miedo", "пугать", "u12_l6", "idiom"),
            ScopeWord("darse cuenta", "понимать", "u12_l6", "idiom"),
            ScopeWord("dar la vuelta", "развернуться", "u12_l6", "idiom"),
            ScopeWord("darse prisa", "спешить", "u12_l6", "idiom"),
            ScopeWord("arañas", "пауки", "u12_l6", "animal"),
        ))
        put("u12_l7", listOf(
            ScopeWord("tener ganas", "хотеть", "u12_l7", "idiom"),
            ScopeWord("tener razón", "быть правым", "u12_l7", "idiom"),
            ScopeWord("tener en cuenta", "учитывать", "u12_l7", "idiom"),
            ScopeWord("tener prisa", "спешить", "u12_l7", "idiom"),
            ScopeWord("tener suerte", "везти", "u12_l7", "idiom"),
            ScopeWord("tener miedo", "бояться", "u12_l7", "idiom"),
        ))
        put("u12_l8", listOf(
            ScopeWord("hacer falta", "быть нужным", "u12_l8", "idiom"),
            ScopeWord("hacer caso", "слушаться", "u12_l8", "idiom"),
            ScopeWord("hacer ilusión", "приятно", "u12_l8", "idiom"),
            ScopeWord("hacerse rico", "стать богат", "u12_l8", "idiom"),
        ))
        put("u12_l9", listOf(
            ScopeWord("llevar a cabo", "осуществить", "u12_l9", "idiom"),
            ScopeWord("llevar la contraria", "спорить", "u12_l9", "idiom"),
            ScopeWord("llevarse bien", "ладить", "u12_l9", "idiom"),
            ScopeWord("llevarse mal", "не ладить", "u12_l9", "idiom"),
            ScopeWord("plan", "план", "u12_l9", "noun"),
        ))
        put("u12_l9_5", listOf(
            ScopeWord("ponerse rojo", "покраснеть", "u12_l9_5", "idiom"),
            ScopeWord("ponerse triste", "загрустить", "u12_l9_5", "idiom"),
            ScopeWord("ponerse de pie", "встать", "u12_l9_5", "idiom"),
            ScopeWord("poner verde", "ругать (разг)", "u12_l9_5", "idiom"),
            ScopeWord("poner de manifiesto", "показать (книжн)", "u12_l9_5", "idiom"),
            ScopeWord("ponerse al día", "наверстать", "u12_l9_5", "idiom"),
            ScopeWord("vergüenza", "стыд", "u12_l9_5", "noun"),
            ScopeWord("me pongo", "становлюсь", "u12_l9_5", "verb"),
            ScopeWord("me puse", "стал я (Indef)", "u12_l9_5", "verb"),
        ))
        put("u12_l10", listOf(
            ScopeWord("vale", "ок (разг)", "u12_l10", "phrase"),
            ScopeWord("automóvil", "автомобиль (форм)", "u12_l10", "vehicle"),
            ScopeWord("vehículo", "тс", "u12_l10", "vehicle"),
            ScopeWord("actualmente", "сейчас (форм)", "u12_l10", "adverb"),
            ScopeWord("no obstante", "тем не менее (форм)", "u12_l10", "connector"),
            ScopeWord("vos", "ты (Аргентина)", "u12_l10", "pronoun"),
        ))
        put("u12_l11", listOf(
            ScopeWord("Por la presente", "настоящим", "u12_l11", "formal"),
            ScopeWord("Solicito", "прошу", "u12_l11", "formal"),
            ScopeWord("Adjunto encontrará", "во вложении найдёте", "u12_l11", "formal"),
            ScopeWord("A la atención de", "вниманию", "u12_l11", "formal"),
            ScopeWord("solicitar", "подавать заявление", "u12_l11", "verb"),
            ScopeWord("puesto", "должность", "u12_l11", "noun"),
        ))
        put("u12_l12", listOf(
            ScopeWord("Estoy de acuerdo", "согласен", "u12_l12", "phrase"),
            ScopeWord("No estoy de acuerdo", "не согласен", "u12_l12", "phrase"),
            ScopeWord("Depende", "зависит", "u12_l12", "phrase"),
            ScopeWord("En parte", "отчасти", "u12_l12", "phrase"),
            ScopeWord("totalmente", "полностью", "u12_l12", "adverb"),
            ScopeWord("urgente", "срочный", "u12_l12", "adjective"),
            ScopeWord("cambio climático", "изменение климата", "u12_l12", "phrase"),
        ))
        put("u12_l13", listOf(
            ScopeWord("Por un lado", "с одной стороны", "u12_l13", "connector"),
            ScopeWord("Por otro lado", "с другой стороны", "u12_l13", "connector"),
            ScopeWord("En primer lugar", "во-первых", "u12_l13", "connector"),
            ScopeWord("En segundo lugar", "во-вторых", "u12_l13", "connector"),
            ScopeWord("En conclusión", "в заключение", "u12_l13", "connector"),
            ScopeWord("Hay que tener en cuenta", "надо учитывать", "u12_l13", "phrase"),
        ))
        put("u12_l14", listOf(
            ScopeWord("entrevistador", "интервьюер", "u12_l14", "profession"),
            ScopeWord("formar parte", "быть частью", "u12_l14", "phrase"),
            ScopeWord("candidatura", "кандидатура", "u12_l14", "noun"),
            ScopeWord("compañeros", "коллеги", "u12_l14", "noun"),
            ScopeWord("marketing", "маркетинг", "u12_l14", "noun"),
        ))

        // ═══════════════════════════════════════════════════════════════
        // B2 · Блок 4.1 «Subjuntivo Avanzado» — Imp+Pluscuamp Subj, Cond.Comp.
        // ═══════════════════════════════════════════════════════════════
        put("u13_l0", listOf(
            ScopeWord("hablase", "(чтобы) говорил (-se)", "u13_l0", "verb"),
            ScopeWord("comiese", "(чтобы) ел (-se)", "u13_l0", "verb"),
            ScopeWord("viviese", "(чтобы) жил (-se)", "u13_l0", "verb"),
            ScopeWord("tuviese", "(чтобы) имел (-se)", "u13_l0", "verb"),
            ScopeWord("fuese", "(чтобы) был (-se)", "u13_l0", "verb"),
        ))
        put("u13_l1", listOf(
            ScopeWord("hubiera", "(чтобы) имел (вспом)", "u13_l1", "verb"),
        ))
        put("u13_l2", listOf(
            ScopeWord("genial", "круто", "u13_l2", "adjective"),
        ))
        put("u13_l3", listOf(
            ScopeWord("respuesta", "ответ", "u13_l3", "noun"),
            ScopeWord("estuviera", "(чтобы) находился", "u13_l3", "verb"),
        ))
        put("u13_l4", listOf(
            ScopeWord("como si", "как будто", "u13_l4", "phrase"),
            ScopeWord("conociera", "(чтобы) знал (Imp Subj)", "u13_l4", "verb"),
        ))
        put("u13_l5", listOf(
            ScopeWord("vinieran", "(чтобы) пришли", "u13_l5", "verb"),
        ))
        put("u13_l5_5", listOf(
            ScopeWord("acaso", "разве (книжн)", "u13_l5_5", "adverb"),
        ))
        put("u13_l6", listOf(
            ScopeWord("hubieras", "ты бы имел", "u13_l6", "verb"),
            ScopeWord("hubiéramos", "мы бы имели", "u13_l6", "verb"),
            ScopeWord("hubieran", "они бы имели", "u13_l6", "verb"),
            ScopeWord("hubiese", "(чтобы) имел (-se)", "u13_l6", "verb"),
            ScopeWord("estudiado", "выученный", "u13_l6", "participle"),
            ScopeWord("sabido", "знаемый", "u13_l6", "participle"),
            ScopeWord("fantasma", "привидение", "u13_l6", "noun"),
        ))
        put("u13_l7", listOf(
            ScopeWord("habría sabido", "знал бы (тогда)", "u13_l7", "verb"),
            ScopeWord("habría hecho", "сделал бы", "u13_l7", "verb"),
            ScopeWord("habría ido", "пошёл бы", "u13_l7", "verb"),
            ScopeWord("habría ayudado", "помог бы", "u13_l7", "verb"),
            ScopeWord("habríamos ido", "мы бы пошли", "u13_l7", "verb"),
        ))
        put("u13_l8", listOf(
            ScopeWord("habría", "я бы (вспом)", "u13_l8", "verb"),
            ScopeWord("habrías", "ты бы", "u13_l8", "verb"),
            ScopeWord("habría (él)", "он бы", "u13_l8", "verb"),
            ScopeWord("habríamos", "мы бы", "u13_l8", "verb"),
            ScopeWord("habrían", "они бы", "u13_l8", "verb"),
            ScopeWord("habría visto", "видел бы", "u13_l8", "verb"),
        ))
        put("u13_l9", listOf(
            ScopeWord("Que yo sepa", "насколько знаю", "u13_l9", "phrase"),
            ScopeWord("Que yo recuerde", "насколько помню", "u13_l9", "phrase"),
            ScopeWord("Cueste lo que cueste", "чего бы ни стоило", "u13_l9", "phrase"),
            ScopeWord("Pase lo que pase", "что бы ни случилось", "u13_l9", "phrase"),
            ScopeWord("Sea como sea", "как бы ни было", "u13_l9", "phrase"),
        ))
        put("u13_l10", listOf(
            ScopeWord("me canse", "(чтобы) устал", "u13_l10", "verb"),
        ))
        put("u13_l11", listOf(
            ScopeWord("a fin de que", "с тем чтобы", "u13_l11", "phrase"),
            ScopeWord("con el objeto de que", "с целью чтобы", "u13_l11", "phrase"),
            ScopeWord("estudien", "(чтобы) учились", "u13_l11", "verb"),
            ScopeWord("entiendan", "(чтобы) поняли", "u13_l11", "verb"),
        ))
        put("u13_l12", listOf(
            ScopeWord("en cuanto", "как только", "u13_l12", "phrase"),
            ScopeWord("hasta que", "пока не", "u13_l12", "phrase"),
            ScopeWord("mientras", "пока", "u13_l12", "phrase"),
            ScopeWord("llegues", "(когда) придёшь", "u13_l12", "verb"),
            ScopeWord("vuelvas", "(когда) вернёшься", "u13_l12", "verb"),
            ScopeWord("aviso", "сообщаю", "u13_l12", "verb"),
        ))
        put("u13_l13", listOf(
            ScopeWord("cualquier", "любой", "u13_l13", "adjective"),
            ScopeWord("cualquier cosa", "что угодно", "u13_l13", "phrase"),
            ScopeWord("jardín", "сад", "u13_l13", "noun"),
            ScopeWord("ayude", "(чтобы) помог", "u13_l13", "verb"),
            ScopeWord("digas", "(чтобы) сказал", "u13_l13", "verb"),
        ))
        put("u13_l14", listOf(
            ScopeWord("arrepentirse", "сожалеть", "u13_l14", "verb"),
            ScopeWord("me arrepiento", "сожалею", "u13_l14", "verb"),
            ScopeWord("examen", "экзамен", "u13_l14", "noun"),
            ScopeWord("aprobado", "сдан", "u13_l14", "participle"),
        ))

        // ═══════════════════════════════════════════════════════════════
        // B2 · Блок 4.2 «Pasiva y Perífrasis» — 16 уроков
        // ═══════════════════════════════════════════════════════════════
        put("u14_l0", listOf(
            ScopeWord("construido", "построенный", "u14_l0", "participle"),
            ScopeWord("ley", "закон", "u14_l0", "noun"),
            ScopeWord("aprobada", "одобренная", "u14_l0", "participle"),
        ))
        put("u14_l1", listOf(
            ScopeWord("cerrada (estado)", "закрыта (состояние)", "u14_l1", "participle"),
            ScopeWord("ventana", "окно", "u14_l1", "noun"),
        ))
        put("u14_l2", listOf(
            ScopeWord("se vende", "продаётся", "u14_l2", "verb"),
            ScopeWord("se venden", "продаются", "u14_l2", "verb"),
            ScopeWord("se dice", "говорят", "u14_l2", "verb"),
            ScopeWord("se fuma", "курят", "u14_l2", "verb"),
            ScopeWord("fumar", "курить", "u14_l2", "verb"),
        ))
        put("u14_l3", listOf(
            ScopeWord("vuelvo a llamar", "снова звоню", "u14_l3", "phrase"),
        ))
        put("u14_l4", listOf(
            ScopeWord("llevo trabajando", "работаю уже", "u14_l4", "phrase"),
        ))
        put("u14_l5", listOf(
            ScopeWord("se vendieron", "продались", "u14_l5", "verb"),
        ))
        put("u14_l6", listOf(
            ScopeWord("dejar de", "перестать", "u14_l6", "phrase"),
            ScopeWord("dejé", "оставил", "u14_l6", "verb"),
            ScopeWord("ponerse a", "приниматься", "u14_l6", "phrase"),
            ScopeWord("me puse a", "принялся", "u14_l6", "verb"),
            ScopeWord("llorar", "плакать", "u14_l6", "verb"),
        ))
        put("u14_l7", listOf(
            ScopeWord("abiertas", "открытые", "u14_l7", "participle"),
            ScopeWord("cerradas", "закрытые", "u14_l7", "participle"),
        ))
        put("u14_l8", listOf(
            ScopeWord("habiendo", "имеючи (ger)", "u14_l8", "gerund"),
            ScopeWord("habiendo llegado", "прибыв", "u14_l8", "phrase"),
            ScopeWord("habiendo dicho", "сказав", "u14_l8", "phrase"),
            ScopeWord("estando", "будучи", "u14_l8", "gerund"),
            ScopeWord("me fui", "я ушёл", "u14_l8", "verb"),
        ))
        put("u14_l9", listOf(
            ScopeWord("daña", "вредит", "u14_l9", "verb"),
            ScopeWord("dañar", "вредить", "u14_l9", "verb"),
            ScopeWord("salud", "здоровье", "u14_l9", "noun"),
        ))
        put("u14_l9_5", listOf(
            ScopeWord("a través de", "через", "u14_l9_5", "preposition"),
            ScopeWord("en torno a", "около", "u14_l9_5", "preposition"),
            ScopeWord("con respecto a", "относительно", "u14_l9_5", "preposition"),
            ScopeWord("en lugar de", "вместо", "u14_l9_5", "preposition"),
            ScopeWord("aprendí", "выучил я", "u14_l9_5", "verb"),
        ))
        put("u14_l10", listOf(
            ScopeWord("lugar", "место", "u14_l10", "noun"),
        ))
        put("u14_l11", listOf(
            ScopeWord("ese día", "тот день", "u14_l11", "phrase"),
            ScopeWord("al día siguiente", "на следующий день", "u14_l11", "phrase"),
            ScopeWord("el día anterior", "за день до", "u14_l11", "phrase"),
            ScopeWord("siguiente", "следующий", "u14_l11", "adjective"),
            ScopeWord("anterior", "предыдущий", "u14_l11", "adjective"),
        ))
        put("u14_l12", listOf(
            ScopeWord("ser bueno", "хороший человек", "u14_l12", "phrase"),
            ScopeWord("estar bueno", "вкусный/здоровый", "u14_l12", "phrase"),
            ScopeWord("ser listo", "умный", "u14_l12", "phrase"),
            ScopeWord("estar listo", "готов", "u14_l12", "phrase"),
            ScopeWord("está bueno", "вкусный", "u14_l12", "phrase"),
        ))
        put("u14_l13", listOf(
            ScopeWord("decisión", "решение", "u14_l13", "noun"),
            ScopeWord("estudio", "учёба", "u14_l13", "noun"),
            ScopeWord("construcción", "стройка", "u14_l13", "noun"),
            ScopeWord("venta", "продажа", "u14_l13", "noun"),
            ScopeWord("creencia", "вера/убеждение", "u14_l13", "noun"),
            ScopeWord("tomada", "принятая", "u14_l13", "participle"),
            ScopeWord("tomé", "я взял/принял", "u14_l13", "verb"),
        ))
        put("u14_l14", listOf(
            ScopeWord("estamos abiertos", "мы открыты", "u14_l14", "phrase"),
            ScopeWord("museo", "музей", "u14_l14", "noun"),
        ))

        // ═══════════════════════════════════════════════════════════════
        // B2 · Блок 4.3 «Comunicación Formal» — 16 уроков
        // ═══════════════════════════════════════════════════════════════
        put("u15_l0", listOf(
            ScopeWord("formal", "формальный", "u15_l0", "register"),
            ScopeWord("neutro", "нейтральный", "u15_l0", "register"),
            ScopeWord("coloquial", "разговорный", "u15_l0", "register"),
        ))
        put("u15_l1", listOf(
            ScopeWord("queja", "жалоба", "u15_l1", "noun"),
            ScopeWord("agradecimiento", "благодарность", "u15_l1", "noun"),
            ScopeWord("Le ruego", "прошу Вас (формал)", "u15_l1", "phrase"),
            ScopeWord("solucionar", "решать (проблему)", "u15_l1", "verb"),
            ScopeWord("problema", "проблема", "u15_l1", "noun"),
            ScopeWord("ruego", "прошу", "u15_l1", "verb"),
        ))
        put("u15_l2", listOf(
            ScopeWord("informe", "отчёт", "u15_l2", "noun"),
            ScopeWord("Introducción", "введение", "u15_l2", "noun"),
            ScopeWord("Desarrollo", "основная часть", "u15_l2", "noun"),
            ScopeWord("Conclusión", "заключение", "u15_l2", "noun"),
            ScopeWord("Bibliografía", "источники", "u15_l2", "noun"),
            ScopeWord("Resumen", "резюме", "u15_l2", "noun"),
            ScopeWord("se presenta", "представляется", "u15_l2", "verb"),
            ScopeWord("de este modo", "таким образом", "u15_l2", "phrase"),
        ))
        put("u15_l3", listOf(
            ScopeWord("tesis", "тезис", "u15_l3", "noun"),
            ScopeWord("argumentos", "аргументы", "u15_l3", "noun"),
            ScopeWord("ejemplos", "примеры", "u15_l3", "noun"),
            ScopeWord("Considero que", "считаю что", "u15_l3", "phrase"),
            ScopeWord("En mi opinión", "по моему мнению", "u15_l3", "phrase"),
            ScopeWord("Pienso que", "думаю что", "u15_l3", "phrase"),
        ))
        put("u15_l4", listOf(
            ScopeWord("no obstante", "тем не менее", "u15_l4", "connector"),
            ScopeWord("ahora bien", "однако (книжн)", "u15_l4", "connector"),
            ScopeWord("por el contrario", "напротив", "u15_l4", "connector"),
        ))
        put("u15_l5", listOf(
            ScopeWord("intentaré", "попробую", "u15_l5", "verb"),
        ))
        put("u15_l6", listOf(
            ScopeWord("dado que", "учитывая что", "u15_l6", "connector"),
            ScopeWord("puesto que", "поскольку", "u15_l6", "connector"),
            ScopeWord("ya que", "так как", "u15_l6", "connector"),
            ScopeWord("debido a", "из-за", "u15_l6", "connector"),
            ScopeWord("debido a que", "из-за того что", "u15_l6", "connector"),
        ))
        put("u15_l7", listOf(
            ScopeWord("de ahí que", "отсюда (+Subj)", "u15_l7", "connector"),
            ScopeWord("de modo que", "так что", "u15_l7", "connector"),
            ScopeWord("así que", "так что (разг)", "u15_l7", "connector"),
            ScopeWord("por consiguiente", "следовательно", "u15_l7", "connector"),
        ))
        put("u15_l8", listOf(
            ScopeWord("si bien", "хотя (формал)", "u15_l8", "connector"),
            ScopeWord("aun cuando", "даже когда", "u15_l8", "connector"),
            ScopeWord("pese a", "вопреки", "u15_l8", "connector"),
        ))
        put("u15_l9", listOf(
            ScopeWord("demuestra", "доказывает", "u15_l9", "verb"),
            ScopeWord("demostrar", "доказывать", "u15_l9", "verb"),
            ScopeWord("confirma", "подтверждает", "u15_l9", "verb"),
            ScopeWord("confirmar", "подтверждать", "u15_l9", "verb"),
            ScopeWord("punto", "точка/пункт", "u15_l9", "noun"),
            ScopeWord("estudios", "исследования", "u15_l9", "noun"),
        ))
        put("u15_l10", listOf(
            ScopeWord("según", "согласно", "u15_l10", "preposition"),
            ScopeWord("de acuerdo con", "в соответствии", "u15_l10", "phrase"),
            ScopeWord("a juicio de", "по мнению", "u15_l10", "phrase"),
            ScopeWord("afirma", "утверждает", "u15_l10", "verb"),
            ScopeWord("sostiene", "поддерживает", "u15_l10", "verb"),
            ScopeWord("expertos", "эксперты", "u15_l10", "noun"),
        ))
        put("u15_l11", listOf(
            ScopeWord("per se", "сам по себе", "u15_l11", "latin"),
            ScopeWord("a posteriori", "после факта", "u15_l11", "latin"),
            ScopeWord("in situ", "на месте", "u15_l11", "latin"),
            ScopeWord("ipso facto", "тут же", "u15_l11", "latin"),
            ScopeWord("ad hoc", "специально", "u15_l11", "latin"),
        ))
        put("u15_l11_5", listOf(
            ScopeWord("mientras (Indic)", "пока (одноврем)", "u15_l11_5", "phrase"),
            ScopeWord("mientras (Subj)", "до тех пор пока", "u15_l11_5", "phrase"),
        ))
        put("u15_l12", listOf(
            ScopeWord("implementación", "внедрение", "u15_l12", "noun"),
            ScopeWord("aprobación", "одобрение", "u15_l12", "noun"),
            ScopeWord("valoración", "оценка", "u15_l12", "noun"),
            ScopeWord("negociación", "переговоры", "u15_l12", "noun"),
            ScopeWord("aprobar", "одобрять", "u15_l12", "verb"),
        ))
        put("u15_l13", listOf(
            ScopeWord("evidenciar", "указывать", "u15_l13", "verb"),
            ScopeWord("sostener", "поддерживать", "u15_l13", "verb"),
            ScopeWord("plantear", "ставить (вопрос)", "u15_l13", "verb"),
            ScopeWord("indagar", "исследовать", "u15_l13", "verb"),
        ))
        put("u15_l14", listOf(
            ScopeWord("proyecto", "проект", "u15_l14", "noun"),
            ScopeWord("éxito", "успех", "u15_l14", "noun"),
            ScopeWord("dificultades", "трудности", "u15_l14", "noun"),
            ScopeWord("últimos", "последние", "u15_l14", "adjective"),
        ))

        // ═══════════════════════════════════════════════════════════════
        // B2 · Блок 4.4 «Léxico y Cultura» — финал курса
        // ═══════════════════════════════════════════════════════════════
        put("u16_l0", listOf(
            ScopeWord("a rajatabla", "строго", "u16_l0", "modism"),
            ScopeWord("en boca de todos", "на устах", "u16_l0", "modism"),
            ScopeWord("de pies a cabeza", "с ног до головы", "u16_l0", "modism"),
            ScopeWord("a manos llenas", "щедро", "u16_l0", "modism"),
            ScopeWord("al pie de la letra", "буквально", "u16_l0", "modism"),
        ))
        put("u16_l1", listOf(
            ScopeWord("no hay mal que por bien no venga", "нет худа без добра", "u16_l1", "saying"),
            ScopeWord("más vale prevenir", "лучше предотвратить", "u16_l1", "saying"),
            ScopeWord("a buen entendedor", "умному намёк", "u16_l1", "saying"),
            ScopeWord("prevenir", "предотвращать", "u16_l1", "verb"),
        ))
        put("u16_l2", listOf(
            ScopeWord("El que mucho abarca, poco aprieta", "много хочешь — мало получишь", "u16_l2", "saying"),
            ScopeWord("Más vale tarde que nunca", "лучше поздно", "u16_l2", "saying"),
            ScopeWord("Dime con quién andas", "скажи кто твой друг", "u16_l2", "saying"),
            ScopeWord("Quien siembra vientos", "кто сеет ветер", "u16_l2", "saying"),
            ScopeWord("Donde fueres", "куда поедешь", "u16_l2", "saying"),
            ScopeWord("refrán", "поговорка", "u16_l2", "noun"),
        ))
        put("u16_l3", listOf(
            ScopeWord("pasar a mejor vida", "уйти из жизни", "u16_l3", "euphemism"),
            ScopeWord("tercera edad", "пожилой возраст", "u16_l3", "euphemism"),
            ScopeWord("persona con discapacidad", "человек с инвалидностью", "u16_l3", "euphemism"),
            ScopeWord("ajuste salarial", "корректировка зп", "u16_l3", "euphemism"),
            ScopeWord("reestructuración", "реструктуризация", "u16_l3", "euphemism"),
            ScopeWord("fallecer", "скончаться", "u16_l3", "verb"),
        ))
        put("u16_l4", listOf(
            ScopeWord("corazón roto", "разбитое сердце", "u16_l4", "metaphor"),
            ScopeWord("mar de problemas", "море проблем", "u16_l4", "metaphor"),
            ScopeWord("luz al final del túnel", "свет в конце тоннеля", "u16_l4", "metaphor"),
            ScopeWord("cabeza en las nubes", "витать в облаках", "u16_l4", "metaphor"),
            ScopeWord("ser uña y carne", "не разлей вода", "u16_l4", "metaphor"),
            ScopeWord("túnel", "тоннель", "u16_l4", "noun"),
        ))
        put("u16_l4_5", listOf(
            ScopeWord("embarazada", "беременная", "u16_l4_5", "false_friend"),
            ScopeWord("avergonzada", "смущённая", "u16_l4_5", "adjective"),
            ScopeWord("sensible", "чувствительный", "u16_l4_5", "false_friend"),
            ScopeWord("sensato", "разумный", "u16_l4_5", "adjective"),
            ScopeWord("actual", "нынешний", "u16_l4_5", "false_friend"),
            ScopeWord("constipado", "простуженный", "u16_l4_5", "false_friend"),
            ScopeWord("éxito", "успех (НЕ выход)", "u16_l4_5", "false_friend"),
            ScopeWord("negocio", "бизнес", "u16_l4_5", "noun"),
        ))
        put("u16_l5", listOf(
            ScopeWord("modismo", "идиома", "u16_l5", "noun"),
        ))
        put("u16_l6", listOf(
            ScopeWord("vos sos", "ты есть (Аргентина)", "u16_l6", "regional"),
            ScopeWord("jugo", "сок (Латам)", "u16_l6", "regional"),
            ScopeWord("celular", "мобильник (Латам)", "u16_l6", "regional"),
            ScopeWord("computador", "комп (Латам)", "u16_l6", "regional"),
            ScopeWord("carro", "машина (Латам)", "u16_l6", "regional"),
            ScopeWord("papa", "картофель (Латам)", "u16_l6", "regional"),
        ))
        put("u16_l7", listOf(
            ScopeWord("realizar", "осуществить", "u16_l7", "false_friend"),
            ScopeWord("molestar", "мешать", "u16_l7", "false_friend"),
            ScopeWord("asistir", "присутствовать", "u16_l7", "false_friend"),
            ScopeWord("asistí", "присутствовал я", "u16_l7", "verb"),
            ScopeWord("ropa", "одежда (НЕ rope)", "u16_l7", "false_friend"),
            ScopeWord("pretender", "пытаться", "u16_l7", "false_friend"),
            ScopeWord("no me molestes", "не мешай мне", "u16_l7", "phrase"),
        ))
        put("u16_l8", listOf(
            ScopeWord("casita", "домик", "u16_l8", "diminutive"),
            ScopeWord("perrito", "собачка", "u16_l8", "diminutive"),
            ScopeWord("hombrón", "большой мужик", "u16_l8", "augmentative"),
            ScopeWord("golazo", "крутой гол", "u16_l8", "augmentative"),
            ScopeWord("amiguito", "дружок", "u16_l8", "diminutive"),
            ScopeWord("abuelito", "дедуля", "u16_l8", "diminutive"),
        ))
        put("u16_l9", listOf(
            ScopeWord("startup", "стартап", "u16_l9", "modern"),
            ScopeWord("sostenibilidad", "устойчивость", "u16_l9", "modern"),
            ScopeWord("branding", "брендинг", "u16_l9", "modern"),
            ScopeWord("networking", "связи (бизнес)", "u16_l9", "modern"),
            ScopeWord("feedback", "обратная связь", "u16_l9", "modern"),
            ScopeWord("conferencia", "конференция", "u16_l9", "noun"),
        ))
        put("u16_l10", listOf(
            ScopeWord("negocios", "бизнес", "u16_l10", "professional"),
            ScopeWord("derecho", "право", "u16_l10", "professional"),
            ScopeWord("ingeniería", "инженерия", "u16_l10", "professional"),
            ScopeWord("educación", "образование", "u16_l10", "professional"),
            ScopeWord("universidad", "университет", "u16_l10", "noun"),
        ))
        put("u16_l11", listOf(
            ScopeWord("Cervantes", "Сервантес", "u16_l11", "culture"),
            ScopeWord("Picasso", "Пикассо", "u16_l11", "culture"),
            ScopeWord("García Márquez", "Гарсиа Маркес", "u16_l11", "culture"),
            ScopeWord("flamenco", "фламенко", "u16_l11", "culture"),
            ScopeWord("Día de los Muertos", "День мёртвых", "u16_l11", "culture"),
            ScopeWord("Don Quijote", "Дон Кихот", "u16_l11", "culture"),
        ))
        put("u16_l12", listOf(
            ScopeWord("sino", "а / но (после отриц)", "u16_l12", "conjunction"),
            ScopeWord("también", "тоже", "u16_l12", "adverb"),
            ScopeWord("tampoco", "тоже не", "u16_l12", "adverb"),
            ScopeWord("líquido", "жидкость", "u16_l12", "noun"),
            ScopeWord("gas", "газ", "u16_l12", "noun"),
        ))
        put("u16_l13", listOf(
            ScopeWord("qué (тильда)", "что (вопр)", "u16_l13", "interrogative"),
            ScopeWord("cuándo (тильда)", "когда (вопр)", "u16_l13", "interrogative"),
            ScopeWord("dónde (тильда)", "где (вопр)", "u16_l13", "interrogative"),
            ScopeWord("cómo (тильда)", "как (вопр)", "u16_l13", "interrogative"),
            ScopeWord("pasó", "произошло", "u16_l13", "verb"),
        ))
        put("u16_l14", listOf(
            ScopeWord("perseverante", "настойчивый", "u16_l14", "adjective"),
            ScopeWord("Diría", "сказал бы", "u16_l14", "verb"),
            ScopeWord("describirse", "описать себя", "u16_l14", "verb"),
            ScopeWord("conseguido", "достигнутый", "u16_l14", "participle"),
            ScopeWord("conseguir", "достигать", "u16_l14", "verb"),
            ScopeWord("agradecer", "благодарить", "u16_l14", "verb"),
            ScopeWord("Felicidades", "поздравления", "u16_l14", "phrase"),
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
        addAll(listOf("u1_l10", "u1_l11", "u1_l7"))                            // SER + местоимения
        addAll(listOf("u1_l8", "u1_l9"))                                   // род + артикли
        addAll(listOf("u1_l12", "u1_l13", "u1_l13_5", "u1_l14"))            // страны + числа + порядковые + checkpoint
        // A1 · Блок 2 «Мой мир»
        addAll(listOf("u2_l6", "u2_l7"))                                     // числа 11-100
        addAll(listOf("u2_l4", "u2_l5"))                                     // TENER
        addAll(listOf("u2_l8", "u2_l9"))                                     // семья
        addAll(listOf("u2_l10"))                                              // притяжательные
        addAll(listOf("u2_l11", "u2_l12"))                                     // цвета + согласование
        addAll(listOf("u3_l0", "u3_l1"))                                    // ESTAR + предлоги места
        addAll(listOf("u3_l2", "u3_l3", "u2_l13", "u2_l14"))              // дом + мебель + мн.ч. + checkpoint
        // A1 · Блок 3 «Действие»
        addAll(listOf("u2_l0", "u2_l1", "u2_l2", "u2_l3"))                  // глаголы AR/ER/IR
        addAll(listOf("u3_l4", "u3_l5", "u3_l5_5"))                          // еда + ресторан + hay
        addAll(listOf("u3_l6", "u3_l7", "u3_l7_5"))                          // querer + poder + e→i
        addAll(listOf("u3_l8", "u3_l9", "u3_l10", "u3_l11"))                // время + дни + месяцы + наречия
        addAll(listOf("u3_l12", "u3_l13", "u3_l14"))                        // вопросы + отрицание + checkpoint
        // A1 · Блок 4 «Выживание»
        addAll(listOf("u4_l2", "u4_l0", "u4_l1", "u4_l3"))                  // транспорт + IR + дорога
        addAll(listOf("u4_l4", "u4_l5"))                                     // магазин + деньги
        addAll(listOf("u4_l6", "u4_l7"))                                     // GUSTAR
        addAll(listOf("u4_l8", "u4_l9"))                                     // тело + здоровье
        addAll(listOf("u4_l10", "u4_l11"))                                  // одежда + погода
        addAll(listOf("u4_l12", "u4_l13", "u4_l13_5"))                      // мой день + возвратные + yo формы
        addAll(listOf("u4_l14"))                                             // ФИНАЛЬНЫЙ A1 checkpoint
        // A2 · Блок 2.1 «В прошлом» — Pretérito Indefinido
        addAll(listOf("u5_l0", "u5_l1", "u5_l2", "u5_l3"))                  // intro + AR + ER/IR + ser/estar
        addAll(listOf("u5_l4", "u5_l5"))                                     // истории + mini-checkpoint
        addAll(listOf("u5_l6", "u5_l7", "u5_l8", "u5_l8_5"))                // ir/ser, tener/estar, hacer/querer + Pluscuamp
        addAll(listOf("u5_l9", "u5_l10"))                                    // por/para + диалог
        addAll(listOf("u5_l11", "u5_l12", "u5_l13", "u5_l14"))              // poder/saber, dar/ver/decir, связки, big test
        // A2 · Блок 2.2 «Раньше и сейчас» — Imperfecto + сравнения + OD/OI
        addAll(listOf("u6_l0", "u6_l1", "u6_l2", "u6_l3"))                  // Imperfecto AR/ER/IR + ser/ir + vs Indef + описания
        addAll(listOf("u6_l4", "u6_l5", "u6_l6"))                            // сравнения и превосх.
        addAll(listOf("u6_l7"))                                              // прилагательные
        addAll(listOf("u6_l8", "u6_l9", "u6_l9_5"))                          // OD + OI + двойные
        addAll(listOf("u6_l10", "u6_l11", "u6_l12"))                         // hace+que, мода, por/para
        addAll(listOf("u6_l13", "u6_l14"))                                   // эмоции + checkpoint
        // A2 · Блок 2.3 «Сейчас и скоро» — Perfecto + Imperativo + герундий
        addAll(listOf("u7_l0", "u7_l1", "u7_l2", "u7_l3"))                  // Perfecto + irreg part + vs Indef + ya/todavía
        addAll(listOf("u7_l4", "u7_l5", "u7_l5_5"))                          // estar+ger + seguir/llevar + Imperativo irreg
        addAll(listOf("u7_l6", "u7_l7", "u7_l8"))                           // работа + Imperativo +/-
        addAll(listOf("u7_l9", "u7_l10", "u7_l11"))                         // врач + OD/OI + путешествие
        addAll(listOf("u7_l12", "u7_l13", "u7_l14"))                        // creo que + кухня + checkpoint
        // A2 · Блок 2.4 «Мечты и планы» — Futuro + Condicional
        addAll(listOf("u8_l0", "u8_l1", "u8_l2", "u8_l3"))                  // Futuro reg+irreg, Cond reg+irreg
        addAll(listOf("u8_l4", "u8_l5"))                                     // si + planes
        addAll(listOf("u8_l6", "u8_l7"))                                     // indefinidos + вероятность
        addAll(listOf("u8_l8", "u8_l9"))                                     // авто + предлоги
        addAll(listOf("u8_l10", "u8_l11"))                                  // природа + cuantific.
        addAll(listOf("u8_l12", "u8_l13"))                                  // tech + спорт
        addAll(listOf("u8_l14"))                                             // ФИНАЛ A2 checkpoint
        // B1 · Блок 3.1 «Subjuntivo»
        addAll(listOf("u9_l0", "u9_l1", "u9_l2", "u9_l3", "u9_l4"))         // intro + спряжения Subj
        addAll(listOf("u9_l5", "u9_l6", "u9_l7", "u9_l8", "u9_l9"))         // триггеры
        addAll(listOf("u9_l10", "u9_l11", "u9_l11_5"))                      // ojalá + para que + antes de que
        addAll(listOf("u9_l12", "u9_l13", "u9_l14"))                        // cuando + aunque + checkpoint
        // B1 · Блок 3.2 «Condicional»
        addAll(listOf("u10_l0", "u10_l1", "u10_l2", "u10_l3", "u10_l4"))    // Cond intro + reg + irreg
        addAll(listOf("u10_l5"))                                             // Si тип 1
        addAll(listOf("u10_l6", "u10_l7", "u10_l8", "u10_l9"))              // Imp.Subj + Si тип 2
        addAll(listOf("u10_l10", "u10_l11", "u10_l12", "u10_l13", "u10_l14")) // советы + вежл + quizás + me gustaría que + checkpoint
        // B1 · Блок 3.3 «Коммуникация»
        addAll(listOf("u11_l0", "u11_l1", "u11_l2", "u11_l3"))              // косв.речь
        addAll(listOf("u11_l4", "u11_l5", "u11_l5_5"))                       // относ. + cuyo + lo+adj
        addAll(listOf("u11_l6", "u11_l7"))                                   // pasiva
        addAll(listOf("u11_l8", "u11_l9", "u11_l10"))                        // perífrasis
        addAll(listOf("u11_l11", "u11_l12", "u11_l13", "u11_l14"))          // конекторы + checkpoint
        // B1 · Блок 3.4 «Словарь и стиль»
        addAll(listOf("u12_l0", "u12_l1", "u12_l2", "u12_l3"))              // лексика B1 (работа, письма, медиа, соцсети)
        addAll(listOf("u12_l4", "u12_l5"))                                   // здоровье
        addAll(listOf("u12_l6", "u12_l7", "u12_l8", "u12_l9", "u12_l9_5")) // идиомы dar/tener/hacer/llevar/poner
        addAll(listOf("u12_l10", "u12_l11"))                                // регистр + заявление
        addAll(listOf("u12_l12", "u12_l13", "u12_l14"))                     // дебаты + аргументация + ФИНАЛ B1
        // B2 · Блок 4.1 «Subjuntivo Avanzado»
        addAll(listOf("u13_l0", "u13_l1", "u13_l2", "u13_l3", "u13_l4", "u13_l5", "u13_l5_5"))
        addAll(listOf("u13_l6", "u13_l7", "u13_l8"))                        // Pluscuamp Subj + Cond.Comp.
        addAll(listOf("u13_l9", "u13_l10"))                                 // устойчивые + aunque
        addAll(listOf("u13_l11", "u13_l12", "u13_l13", "u13_l14"))         // придаточные + checkpoint
        // B2 · Блок 4.2 «Pasiva y Perífrasis»
        addAll(listOf("u14_l0", "u14_l1", "u14_l2"))                        // pasiva ser/estar/se
        addAll(listOf("u14_l3", "u14_l4", "u14_l5"))                        // perífrasis базовые + mini-test
        addAll(listOf("u14_l6", "u14_l7", "u14_l8", "u14_l9", "u14_l9_5"))  // ещё perífrasis + сложные предлоги
        addAll(listOf("u14_l10", "u14_l11", "u14_l12", "u14_l13", "u14_l14")) // повторение + checkpoint
        // B2 · Блок 4.3 «Comunicación Formal»
        addAll(listOf("u15_l0", "u15_l1", "u15_l2", "u15_l3"))              // регистры + типы текстов
        addAll(listOf("u15_l4", "u15_l5", "u15_l6", "u15_l7", "u15_l8"))    // конекторы все
        addAll(listOf("u15_l9", "u15_l10", "u15_l11", "u15_l11_5"))         // аргументация + латинизмы
        addAll(listOf("u15_l12", "u15_l13", "u15_l14"))                     // nominalización + академ + checkpoint
        // B2 · Блок 4.4 «Léxico y Cultura» — финал курса
        addAll(listOf("u16_l0", "u16_l1", "u16_l2", "u16_l3", "u16_l4", "u16_l4_5", "u16_l5"))
        addAll(listOf("u16_l6", "u16_l7", "u16_l8"))                        // регионал + falsos amigos + diminut
        addAll(listOf("u16_l9", "u16_l10", "u16_l11"))                      // современная + проф + культура
        addAll(listOf("u16_l12", "u16_l13"))                                // tricky cases + орфография
        addAll(listOf("u16_l14"))                                            // 🏆 ФИНАЛЬНЫЙ ЧЕКПОИНТ B2
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
