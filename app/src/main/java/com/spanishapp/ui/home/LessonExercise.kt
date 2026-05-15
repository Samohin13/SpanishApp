package com.spanishapp.ui.home

/**
 * Все типы упражнений в LessonSession. Расширено в Phase 0 курса v1.2.0
 * по спецификации `docs/curriculum/ESPEAK_Curriculum.xlsx`.
 *
 * При добавлении нового типа обязательно:
 *   1. Добавить enum-значение здесь
 *   2. Добавить нужные поля в Exercise (если требуются)
 *   3. Добавить рендер-Composable в LessonSessionScreen.kt
 *   4. (опц) Добавить генератор-fallback в ExerciseGenerator.kt
 */
enum class ExerciseType {
    MULTIPLE_CHOICE,   // 4 варианта, один верный
    FILL_BLANK,        // Вставь пропущенное слово (legacy — преим. TAP_MISSING_WORD)
    TRANSLATE,         // Переведи фразу (печатанием)
    BUILD_SENTENCE,    // Составь предложение из тайлов
    SPEAKING,          // Произнеси слово — STT проверяет

    // ── Phase 1 additions ──
    LISTEN_PICK,       // TTS играет слово → тапнуть нужный из 4 написанных
    ORDER_LETTERS,     // Тайлы букв в случайном порядке → собрать слово

    // ── Phase 2 additions ──
    MATCH_PAIRS,       // 5 пар × 2 раунда (10 пар суммарно): es↔ru — соединить тапами
    TAP_MISSING_WORD,  // Предложение с пропуском → тапнуть из 3-4 чипов

    // ── Phase 3 additions ──
    LISTEN_TYPE,       // Диктант: TTS играет → напечатать что слышишь

    // ── Phase 4 additions ──
    CONJUGATION_GRID,  // Полная таблица спряжений: yo/tú/él/nosotros/vosotros/ellos

    // ── Phase 0 курса v1.2.0 (новые из xlsx) ────────────────
    /** Услышь число → нажми цифру на цифровом грид-панели (1-100). */
    LISTEN_NUMBER_TAP,

    /** Видишь цифру (5) → выбери испанское слово (cinco) из 4 вариантов. */
    READ_NUMBER,

    /**
     * TTS играет длинную фразу/мини-диалог → отвечаешь на вопрос на понимание.
     * Тренирует comprehension вместо одиночных слов.
     */
    LISTEN_COMPREHEND,

    /**
     * Мини-диалог из 2-4 реплик с одним пропуском.
     * Юзер видит контекст диалога, должен выбрать подходящую реплику.
     */
    DIALOGUE_FILL,

    /**
     * TTS играет фразу → юзер повторяет в микрофон (STT).
     * Похоже на SPEAKING, но обязательно с прослушиванием эталона до.
     */
    SPEAK_REPEAT,

    /**
     * 3-4 варианта одной фразы — найди ту где ОШИБКА.
     * Тренирует distinction (например «Yo soy» vs «Yo es» vs «Yo eres»).
     */
    SPOT_THE_ERROR,
}

/**
 * Один пункт упражнения. Все поля опциональны кроме type/instruction/correctAnswer
 * чтобы можно было использовать одну data class для всех типов.
 */
data class Exercise(
    val type: ExerciseType,
    val instruction: String,
    val question: String = "",
    val hint: String = "",
    val options: List<String> = emptyList(),
    val words: List<String> = emptyList(),
    val correctAnswer: String,
    val explanation: String = "",
    val audioText: String = "",
    val pairs: List<Pair<String, String>> = emptyList(),
    val conjugationForms: List<String> = emptyList(),

    // ── Phase 0 курса v1.2.0 ────────────────────────────────

    /**
     * Для LISTEN_NUMBER_TAP: число которое надо нажать (1-100).
     * Также для READ_NUMBER: цифра которую показывает экран.
     */
    val number: Int? = null,

    /**
     * Для DIALOGUE_FILL: реплики диалога. Каждая реплика — Pair(speaker, text).
     * Одна из реплик содержит маркер «___» — это пропуск.
     * Юзер выбирает заполнение из options.
     */
    val dialogueLines: List<Pair<String, String>> = emptyList(),

    /**
     * Для SPOT_THE_ERROR: 3-4 варианта одной фразы.
     * options[i] = вариант, correctAnswer = тот вариант где ОШИБКА (его надо найти).
     * explanation объясняет в чём ошибка.
     */
    val errorVariants: List<String> = emptyList(),

    /**
     * Для LISTEN_COMPREHEND: длинная фраза для прослушивания (TTS).
     * Используется audioText для аудио, question для вопроса по содержанию,
     * options для вариантов ответа.
     */
    val comprehensionContext: String = "",
)

data class ExercisePlan(
    val title: String,
    val grammarNote: String = "",
    val exercises: List<Exercise>
)
