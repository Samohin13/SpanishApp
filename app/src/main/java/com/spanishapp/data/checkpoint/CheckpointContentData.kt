package com.spanishapp.data.checkpoint

/**
 * Реестр чекпоинт-сценариев. Заполняется по мере прохождения блоков.
 *
 * Phase 0 (текущая): пустой реестр + API. Сами сценарии добавляются
 * вместе с соответствующими блоками:
 *   • cp_a1_4 — после блока 1.4 (финал A1, «Прилёт в Мадрид»)
 *   • cp_a2_3 — после блока 2.3 (история на работе)
 *   • cp_b1_4 — после блока 3.4 (полноценный диалог-конфликт)
 *   • cp_b2_4 — финал курса (большой 18-актный сценарий)
 *
 * Источник правды — лист «Чекпоинты — сценарии» в xlsx (21 сценарий).
 */
object CheckpointContentData {

    /** Получить чекпоинт по ID. */
    fun byId(id: String): CheckpointContent? = ALL[id]

    /** Все чекпоинты для библиотеки. */
    fun all(): List<CheckpointContent> = ALL.values.toList()

    /** Чекпоинты конкретного блока (обычно 0 или 1). */
    fun forBlock(blockId: String): List<CheckpointContent> =
        ALL.values.filter { it.blockId == blockId }

    private val ALL: Map<String, CheckpointContent> = emptyMap()
}
