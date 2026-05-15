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
