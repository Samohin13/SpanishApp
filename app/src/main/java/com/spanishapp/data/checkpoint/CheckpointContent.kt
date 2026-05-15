package com.spanishapp.data.checkpoint

/**
 * Чекпоинт-сценарий: длинная мини-история на проверку всего пройденного блока.
 *
 * Структура (из `docs/curriculum/ESPEAK_Curriculum.xlsx`, лист «Чекпоинты — сценарии»):
 *   • 21 сценарий суммарно — по одному на конце большинства блоков (1.3, 1.4, 2.1, ...)
 *   • Каждый сценарий = 3 сцены × 6 актов = 18 интерактивных шагов
 *   • Сценарий длится 8-15 минут, открывается после прохождения всех уроков блока
 *   • Награда: значительный XP бонус + специальное достижение
 *
 * Каждый акт — это:
 *   • Текст ситуации (что происходит)
 *   • NPC реплика (опц.)
 *   • Один из 6 типов выбора:
 *     - PICK_PHRASE — выбрать подходящую фразу из 3
 *     - SAY_OUT_LOUD — произнести фразу (STT)
 *     - TYPE_REPLY — напечатать ответ
 *     - LISTEN_AND_PICK — услышать NPC → выбрать ответ
 *     - BUILD_REPLY — собрать ответ из тайлов
 *     - WHAT_HAPPENS — выбрать что произойдёт дальше (сюжетная развилка)
 */
data class CheckpointContent(
    /** ID чекпоинта: "cp_a1_1" (после блока 1.1), "cp_a1_2", ... */
    val id: String,

    /** Заголовок: «🎯 Знакомство в кафе» или «✈ Прилёт в Мадрид». */
    val title: String,

    /** Подзаголовок-сеттинг: «Ты впервые в Барселоне. Зашёл в кафе…». */
    val intro: String,

    /** Эмодзи для карточки. */
    val emoji: String = "🎯",

    /** Уровень CEFR. */
    val cefr: String = "A1",

    /** К какому блоку относится: «1.1» / «1.4» / «2.3» — для роутинга. */
    val blockId: String,

    /** Ожидаемое время прохождения (минуты). */
    val durationMinutes: Int = 10,

    /** Какие уроки должны быть пройдены ДО чекпоинта. Гарантия что vocab знаком. */
    val prerequisites: List<String> = emptyList(),

    /** Три сцены — части истории. */
    val scenes: List<CheckpointScene>,

    /** Финальный текст-награда после прохождения. */
    val outroSuccess: String = "¡Lo lograste! Ты прошёл сценарий.",
    val outroPartial: String = "Молодец, ты завершил историю. Часть фраз можно подтянуть.",

    /** Бонус XP за полное прохождение. */
    val bonusXp: Int = 80,

    /** ID достижения которое разблокируется (если есть). */
    val achievementId: String? = null,
)

/**
 * Одна сцена — часть истории. Обычно 6 актов внутри.
 *
 * Пример сцены:
 *   title = "Сцена 2: Заказ"
 *   setting = "Бариста подходит к твоему столику…"
 *   acts = listOf(act6, act7, ..., act11)
 */
data class CheckpointScene(
    val title: String,
    /** Описание сеттинга сцены (1-2 предложения). */
    val setting: String,
    val acts: List<CheckpointAct>,
)

/**
 * Один акт — интерактивный шаг.
 */
data class CheckpointAct(
    /** Тип взаимодействия. */
    val type: CheckpointActType,

    /**
     * Текст ситуации (нарратив): «Бариста смотрит на тебя и ждёт».
     * Рисуется как «narrator» строка.
     */
    val narration: String = "",

    /** NPC реплика (если есть): «¿Qué desea tomar?». */
    val npcSpeaker: String = "",
    val npcLine: String = "",

    /**
     * Перевод NPC реплики (показывается по тапу или сразу под испанской).
     */
    val npcTranslation: String = "",

    /**
     * Варианты ответа юзера. Для PICK_PHRASE / LISTEN_AND_PICK / WHAT_HAPPENS.
     * Каждая опция: испанский ответ + русский перевод + правильный ли это вариант.
     */
    val options: List<CheckpointOption> = emptyList(),

    /**
     * Для TYPE_REPLY / BUILD_REPLY / SAY_OUT_LOUD —
     * правильный ответ который ожидается.
     */
    val expectedReply: String = "",

    /** Для BUILD_REPLY — слова которые нужно собрать в правильном порядке. */
    val replyTokens: List<String> = emptyList(),

    /** Подсказка-хинт (опц.) — показывается по «🛟 Подсказка». */
    val hint: String = "",

    /** Объяснение после ответа: что было верно/нет. */
    val explanation: String = "",
)

enum class CheckpointActType {
    /** Выбрать одну реплику из 3-4 вариантов. */
    PICK_PHRASE,

    /** Произнести фразу в микрофон. */
    SAY_OUT_LOUD,

    /** Напечатать ответ. */
    TYPE_REPLY,

    /** Услышать NPC реплику (TTS) → выбрать правильный ответ. */
    LISTEN_AND_PICK,

    /** Собрать свой ответ из тайлов слов. */
    BUILD_REPLY,

    /** Сюжетная развилка — выбрать что произойдёт. Все варианты «правильные». */
    WHAT_HAPPENS,

    /** Чисто-нарративный шаг без выбора (для перехода между сценами). */
    NARRATION_ONLY,
}

/**
 * Один вариант ответа.
 */
data class CheckpointOption(
    /** Испанский текст реплики. */
    val spanish: String,
    /** Русский перевод (показывается под испанским). */
    val russian: String = "",
    /** Правильный? (для PICK_PHRASE) */
    val isCorrect: Boolean = false,
    /** Краткое объяснение почему верно/нет. */
    val explanation: String = "",
)
