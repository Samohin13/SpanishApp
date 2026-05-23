package com.spanishapp.domain.checkpoint

/**
 * Маппинг lesson_id из JSON чекпоинтов → читаемое название урока для UI.
 *
 * Используется на экране результата чекпоинта в разделе «Слабые уроки» —
 * чтобы юзер видел «Урок 13 · Страны», а не «u1_l12».
 *
 * v1.22.12: для блока 1 (uроки 1-15 курса A1).
 * Когда появятся блоки 2-4 — расширим этот map.
 */
object CheckpointLessonNames {

    /** Имя урока (с номером для юзера, считается с 1). */
    data class LessonInfo(val number: Int, val title: String) {
        fun display(): String = "Урок $number · $title"
    }

    private val byId: Map<String, LessonInfo> = mapOf(
        "u1_l0"    to LessonInfo(1,  "Алфавит 1/3 (A-I)"),
        "u1_l1"    to LessonInfo(2,  "Алфавит 2/3 (J-Q)"),
        "u1_l2"    to LessonInfo(3,  "Алфавит 3/3 (R-Z)"),
        "u1_l3"    to LessonInfo(4,  "Ударение и тильда"),
        "u1_l4"    to LessonInfo(5,  "Приветствия"),
        "u1_l5"    to LessonInfo(6,  "Прощания"),
        "u1_l6"    to LessonInfo(7,  "Вежливость (gracias, por favor)"),
        "u1_l7"    to LessonInfo(8,  "Местоимения"),
        "u1_l8"    to LessonInfo(9,  "Род: el/la"),
        "u1_l9"    to LessonInfo(10, "Артикли el/la/un/una"),
        "u1_l10"   to LessonInfo(11, "SER: soy, eres, es"),
        "u1_l11"   to LessonInfo(12, "SER: somos, sois, son"),
        "u1_l12"   to LessonInfo(13, "Национальности"),
        "u1_l13"   to LessonInfo(14, "Числа 0-20"),
        "u1_l13_5" to LessonInfo(15, "Порядковые числа"),
        // Блок 2 (когда появится)
        "u2_l0"    to LessonInfo(1,  "Регулярные глаголы -AR"),
        "u2_l1"    to LessonInfo(2,  "Регулярные глаголы -ER"),
        "u2_l2"    to LessonInfo(3,  "Регулярные глаголы -IR"),
        "u2_l4"    to LessonInfo(5,  "TENER ед. ч."),
        "u2_l5"    to LessonInfo(6,  "TENER мн. ч."),
        "u2_l7"    to LessonInfo(8,  "Числа 21-100"),
        "u2_l8"    to LessonInfo(9,  "Семья"),
        "u2_l10"   to LessonInfo(11, "Притяжательные mi/tu"),
        "u2_l11"   to LessonInfo(12, "Цвета"),
        "u2_l12"   to LessonInfo(13, "Согласование цветов"),
        "u2_l13"   to LessonInfo(14, "Множественное число"),
        "u3_l0"    to LessonInfo(1,  "ESTAR ед. ч."),
        "u3_l1"    to LessonInfo(2,  "Предлоги места"),
        "u3_l3"    to LessonInfo(4,  "Мебель"),
        "u3_l4"    to LessonInfo(5,  "Еда"),
        "u3_l5_5"  to LessonInfo(7,  "hay (есть в наличии)"),
        "u3_l6"    to LessonInfo(8,  "QUERER (хотеть)"),
        "u3_l7"    to LessonInfo(9,  "PODER (мочь)"),
        "u3_l8"    to LessonInfo(10, "Время (qué hora es)"),
        "u3_l9"    to LessonInfo(11, "Дни недели"),
        "u3_l11"   to LessonInfo(13, "Наречия времени"),
        "u3_l13"   to LessonInfo(15, "Отрицание"),
        "u4_l0"    to LessonInfo(1,  "IR (идти/ехать)"),
        "u4_l1"    to LessonInfo(2,  "IR + a + место"),
        "u4_l6"    to LessonInfo(7,  "GUSTAR (нравиться)"),
        "u4_l7"    to LessonInfo(8,  "GUSTAR мн. ч."),
        "u4_l8"    to LessonInfo(9,  "Части тела"),
        "u4_l9"    to LessonInfo(10, "Здоровье (me duele)"),
        "u4_l11"   to LessonInfo(12, "Погода (hace frío)"),
        "u4_l12"   to LessonInfo(13, "Мой день (рутина)"),
        "u4_l13"   to LessonInfo(14, "Возвратные глаголы"),
    )

    /**
     * Парсит строку tested_lesson вида "u1_l10" или "u1_l10 + u1_l12"
     * и возвращает список читаемых имён. Дублирующиеся отфильтрованы.
     */
    fun parseAndDescribe(rawList: List<String>): List<LessonInfo> {
        return rawList
            .flatMap { it.split("+", ",", ";").map { s -> s.trim() } }
            .map { it.substringBefore(" ").trim() }   // обрезаем «(блок 2)»-приписки
            .filter { it.isNotBlank() }
            .distinct()
            .mapNotNull { byId[it] }
            .sortedBy { it.number }
    }
}
