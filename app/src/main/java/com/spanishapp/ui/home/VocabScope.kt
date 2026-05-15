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
        // Блок 1.1 «Взлёт» (u1_l0..u1_l13_5) — заполняется в Phase 1.1
        // ═══════════════════════════════════════════════════════════════

        // u1_l0..u1_l3 — фонетика, новых слов словаря не вводят (учат буквы)
        // (но фонетические примеры доступны в теории, не учитываются как vocab)

        // u1_l4 — Приветствия
        // u1_l5 — Прощания
        // u1_l6 — Вежливость
        // u1_l7..u1_l9 — SER + местоимения
        // u1_l10..u1_l11 — Род + артикли
        // u1_l12 — Страны
        // u1_l13 — Числа 0-10
        // u1_l13_5 — Порядковые

        // (заполняется в коммите блока 1.1 — здесь оставлен скелет)
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
