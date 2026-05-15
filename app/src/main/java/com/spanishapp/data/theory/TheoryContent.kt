package com.spanishapp.data.theory

/**
 * Грамматический/лексический справочник под каждый практический урок.
 *
 * Связь 1-к-1:
 *   • LessonContent (u1_l7) — что юзер ДЕЛАЕТ (упражнения)
 *   • TheoryContent (u1_l7) — почему это так (теория, таблицы, мнемоники)
 *
 * Открывается по тапу карточки «📖 Теория к уроку» вверху каждого
 * практического урока ИЛИ из отдельного раздела «Справка» в профиле.
 *
 * Идемпотентность: после прочтения сохраняется TheoryProgressEntity
 * чтобы вторая открытие показывало «прочитано N дней назад» с возможностью
 * освежить через 1/3/7 дней (spaced repetition).
 */
data class TheoryContent(
    /** ID урока к которому относится: "u1_l7" — ровно как в LessonContentData. */
    val lessonId: String,

    /** Заголовок справки. Пример: "Глагол SER — быть постоянно". */
    val title: String,

    /**
     * Короткий поясняющий подзаголовок (1 строка).
     * Пример: "Soy / eres / es — кто ты, откуда, кем работаешь."
     */
    val subtitle: String = "",

    /** Эмодзи карточки. Помогает визуально различать темы. */
    val emoji: String = "📖",

    /** CEFR уровень: A1 / A2 / B1 / B2. */
    val cefr: String = "A1",

    /**
     * Ожидаемое время чтения в минутах.
     * Карточка показывает "⏱ 3 мин" чтобы юзер мог решить открыть или нет.
     */
    val readMinutes: Int = 3,

    /** Секции теории. Рисуются по порядку — каждая со своим типом отображения. */
    val sections: List<TheorySection>,

    /**
     * Ключевые тейк-эвэи. Финальный блок «📝 Что важно запомнить»
     * на 3-5 пунктов. Лучше всего запоминаются после прочтения.
     */
    val keyTakeaways: List<String> = emptyList(),

    /** ID связанных теорий — кнопки «👉 Дальше» внизу. */
    val relatedTheory: List<String> = emptyList(),
)

/**
 * Одна секция теории. Тип определяет как Composable её рисует.
 */
data class TheorySection(
    val type: TheorySectionType,
    /** Заголовок секции (опционален для TEXT и EXAMPLES). */
    val heading: String = "",
    /** Текст для TEXT/TIP/WARNING. Поддерживает **bold** через простой parser. */
    val body: String = "",
    /** Таблица для TABLE. */
    val table: TheoryTable? = null,
    /** Список примеров для EXAMPLES — с TTS озвучкой. */
    val examples: List<TheoryExample> = emptyList(),
    /** Сравнение для COMPARISON — два столбца «X vs Y». */
    val comparison: TheoryComparison? = null,
)

enum class TheorySectionType {
    /** Простой текст с заголовком. */
    TEXT,

    /**
     * Главное правило в выделенном блоке.
     * Рисуется как карточка с акцентным цветом.
     */
    RULE,

    /** Таблица (склонения, спряжения, окончания). */
    TABLE,

    /** Примеры с TTS-озвучкой. */
    EXAMPLES,

    /** Мнемоника — короткая запоминающаяся фраза. */
    MNEMONIC,

    /** Совет / лайфхак (💡). */
    TIP,

    /** Предупреждение об ошибке (⚠). */
    WARNING,

    /** Сравнение X vs Y в две колонки. */
    COMPARISON,
}

/**
 * Таблица в теории. Например для спряжения SER:
 *   headers = ["Лицо", "Форма"]
 *   rows = [
 *     ["yo", "soy"],
 *     ["tú", "eres"],
 *     ...
 *   ]
 *   highlightedColumns = [1] — выделить вторую колонку акцентом
 */
data class TheoryTable(
    val headers: List<String>,
    val rows: List<List<String>>,
    /** Индексы колонок которые надо подсветить акцентным цветом (окончания). */
    val highlightedColumns: List<Int> = emptyList(),
)

/**
 * Один пример с TTS-озвучкой. Юзер тапает на испанскую часть → играет TTS.
 */
data class TheoryExample(
    val spanish: String,
    val russian: String,
    /** Контекст-подсказка: «формальное» / «разговорное» / «только Испания». */
    val note: String = "",
)

/**
 * Сравнение двух явлений. Например «Indefinido vs Imperfecto»:
 *   leftHeader = "Indefinido"
 *   rightHeader = "Imperfecto"
 *   pairs = [
 *     ["событие, что произошло", "фон, как было"],
 *     ["ayer", "todos los días"],
 *     ...
 *   ]
 */
data class TheoryComparison(
    val leftHeader: String,
    val rightHeader: String,
    val pairs: List<Pair<String, String>>,
)
