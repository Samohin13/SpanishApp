package com.spanishapp.domain.checkpoint

/**
 * Маппинг lesson_id из JSON чекпоинтов → читаемое название урока для UI.
 *
 * Используется на экране результата чекпоинта в разделе «Слабые уроки» —
 * чтобы юзер видел «Урок 13 · Страны», а не «u1_l12».
 *
 * v1.22.15: расширено для всех 16 блоков курса (A1→B2). 149 уроков
 * покрыты + плейсхолдеры для редких _5 вариантов.
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
        "u4_l4"    to LessonInfo(5,  "Магазин: ¿Cuánto cuesta?"),
        "u4_l5"    to LessonInfo(6,  "Деньги: euro, precio, efectivo"),

        // Блок 5 (u5) · A2 «В прошлом»
        "u5_l0"    to LessonInfo(1,  "Pretérito Indefinido — введение"),
        "u5_l1"    to LessonInfo(2,  "Indefinido -AR"),
        "u5_l2"    to LessonInfo(3,  "Indefinido -ER/-IR"),
        "u5_l3"    to LessonInfo(4,  "Ser vs Estar в прошлом"),
        "u5_l4"    to LessonInfo(5,  "Истории: ¿Qué hiciste ayer?"),
        "u5_l6"    to LessonInfo(7,  "Indefinido: ir и ser (fui)"),
        "u5_l7"    to LessonInfo(8,  "Indefinido: tener, estar"),
        "u5_l8"    to LessonInfo(9,  "Indefinido: hacer, querer"),
        "u5_l8_5"  to LessonInfo(10, "Pluscuamperfecto Indicativo"),
        "u5_l9"    to LessonInfo(11, "Por vs Para — введение"),
        "u5_l13"   to LessonInfo(15, "Связный текст в прошлом"),

        // Блок 6 (u6) · A2 «Раньше и сейчас»
        "u6_l1"    to LessonInfo(2,  "Imperfecto -ER/-IR + irregulares"),
        "u6_l2"    to LessonInfo(3,  "Indefinido vs Imperfecto"),
        "u6_l3"    to LessonInfo(4,  "Описания прошлого: era / tenía"),
        "u6_l4"    to LessonInfo(5,  "Сравнение más / menos que"),
        "u6_l5"    to LessonInfo(6,  "Равенство: tan / tanto como"),
        "u6_l6"    to LessonInfo(7,  "Превосходная степень"),
        "u6_l8"    to LessonInfo(9,  "Местоимения OD: lo / la / los"),
        "u6_l9"    to LessonInfo(10, "Местоимения OI: me / te / le"),
        "u6_l9_5"  to LessonInfo(11, "Двойные местоимения: se lo"),
        "u6_l11"   to LessonInfo(13, "Одежда и мода"),
        "u6_l13"   to LessonInfo(15, "Эмоции: contento, triste"),

        // Блок 7 (u7) · A2 «Сейчас и скоро»
        "u7_l0"    to LessonInfo(1,  "Pretérito Perfecto"),
        "u7_l1"    to LessonInfo(2,  "Нерегулярные participio"),
        "u7_l2"    to LessonInfo(3,  "Perfecto vs Indefinido"),
        "u7_l3"    to LessonInfo(4,  "ya / todavía / aún"),
        "u7_l4"    to LessonInfo(5,  "Estar + gerundio"),
        "u7_l5"    to LessonInfo(6,  "Seguir / Llevar + gerundio"),
        "u7_l5_5"  to LessonInfo(7,  "Imperativo нерегулярный (tú)"),
        "u7_l6"    to LessonInfo(8,  "Работа: лексика"),
        "u7_l7"    to LessonInfo(9,  "Imperativo утвердительный (tú)"),
        "u7_l8"    to LessonInfo(10, "Imperativo отрицательный"),
        "u7_l10"   to LessonInfo(12, "OD + OI: двойные местоимения"),

        // Блок 8 (u8) · A2 «Мечты и планы»
        "u8_l0"    to LessonInfo(1,  "Futuro Simple"),
        "u8_l1"    to LessonInfo(2,  "Futuro irregular"),
        "u8_l3"    to LessonInfo(4,  "Condicional irregular"),
        "u8_l4"    to LessonInfo(5,  "Si тип 1: реальные условия"),
        "u8_l5"    to LessonInfo(6,  "Планы и мечты"),

        // Блок 9 (u9) · B1 «Subjuntivo»
        "u9_l0"    to LessonInfo(1,  "Subjuntivo — введение"),
        "u9_l1"    to LessonInfo(2,  "Subjuntivo Presente -AR"),
        "u9_l3"    to LessonInfo(4,  "Нерегулярные Subj"),
        "u9_l4"    to LessonInfo(5,  "Отклоняющиеся Subj: e→ie, o→ue"),
        "u9_l6"    to LessonInfo(7,  "Триггеры волеизъявления"),
        "u9_l7"    to LessonInfo(8,  "Безличные триггеры + Subj"),
        "u9_l8"    to LessonInfo(9,  "Эмоции + Subj"),
        "u9_l9"    to LessonInfo(10, "Сомнение + Subj"),
        "u9_l10"   to LessonInfo(11, "Ojalá + Subj"),

        // Блок 10 (u10) · B1 «Condicional avanzado»
        "u10_l0"   to LessonInfo(1,  "Condicional — введение"),
        "u10_l4"   to LessonInfo(5,  "Cond irregular 2: hacer / querer"),
        "u10_l8"   to LessonInfo(9,  "Imperfecto Subj irregular"),
        "u10_l9"   to LessonInfo(10, "Si тип 2: гипотеза"),
        "u10_l10"  to LessonInfo(11, "Советы: Yo en tu lugar..."),
        "u10_l11"  to LessonInfo(12, "Вежливые просьбы: ¿Podrías?"),
        "u10_l12"  to LessonInfo(13, "Quizás / Tal vez"),
        "u10_l13"  to LessonInfo(14, "Me gustaría que + Subj"),

        // Блок 11 (u11) · B1 «Коммуникация»
        "u11_l0"   to LessonInfo(1,  "Estilo indirecto — введение"),
        "u11_l1"   to LessonInfo(2,  "Dijo que / Preguntó si"),
        "u11_l2"   to LessonInfo(3,  "Сдвиг времён в косв. речи"),
        "u11_l3"   to LessonInfo(4,  "Косвенные приказы + Imp.Subj"),
        "u11_l4"   to LessonInfo(5,  "Относительные: que/quien/donde"),
        "u11_l5"   to LessonInfo(6,  "cuyo / el cual / lo cual"),
        "u11_l5_5" to LessonInfo(7,  "Lo + adjetivo"),
        "u11_l8"   to LessonInfo(10, "Llevar + gerundio"),
        "u11_l9"   to LessonInfo(11, "Seguir / continuar + gerundio"),
        "u11_l10"  to LessonInfo(12, "Acabar de / Volver a + inf"),

        // Блок 12 (u12) · B1 «Словарь и стиль»
        "u12_l1"   to LessonInfo(2,  "Формальная переписка"),
        "u12_l6"   to LessonInfo(7,  "Идиомы с DAR"),
        "u12_l9"   to LessonInfo(10, "Идиомы с LLEVAR"),
        "u12_l10"  to LessonInfo(12, "Регистр: формал vs разговорн"),
        "u12_l14"  to LessonInfo(16, "Финал B1: заявление + интервью"),

        // Блок 13 (u13) · B2 «Subjuntivo Avanzado»
        "u13_l0"   to LessonInfo(1,  "Imperfecto de Subjuntivo: повтор"),
        "u13_l1"   to LessonInfo(2,  "Образование Imp.Subj."),
        "u13_l2"   to LessonInfo(3,  "Si + Imp.Subj. + Condicional"),
        "u13_l3"   to LessonInfo(4,  "Ojalá + Imp.Subj."),
        "u13_l4"   to LessonInfo(5,  "Como si + Imp.Subj."),
        "u13_l5_5" to LessonInfo(7,  "Quizás vs A lo mejor"),
        "u13_l6"   to LessonInfo(8,  "Pluscuamperfecto de Subjuntivo"),
        "u13_l7"   to LessonInfo(9,  "Si тип 3: сожаление о прошлом"),
        "u13_l8"   to LessonInfo(10, "Condicional Compuesto"),
        "u13_l9"   to LessonInfo(11, "Устойчивые формулы Subj"),
        "u13_l10"  to LessonInfo(12, "Aunque: Indic vs Subj"),
        "u13_l11"  to LessonInfo(13, "Subj в придаточных цели"),
        "u13_l12"  to LessonInfo(14, "Subj в придаточных времени"),
        "u13_l13"  to LessonInfo(15, "Subj в относительных придат."),

        // Блок 14 (u14) · B2 «Pasiva y perífrasis»
        "u14_l0"   to LessonInfo(1,  "Voz pasiva con SER"),
        "u14_l1"   to LessonInfo(2,  "Estar + participio (результат)"),
        "u14_l2"   to LessonInfo(3,  "SE-пассив и SE-безличное"),
        "u14_l3"   to LessonInfo(4,  "Perífrasis: ir a / acabar de"),
        "u14_l4"   to LessonInfo(5,  "Llevar + gerundio (B2)"),
        "u14_l6"   to LessonInfo(7,  "Perífrasis: seguir / dejar"),
        "u14_l8"   to LessonInfo(9,  "Gerundio продвинутое"),
        "u14_l11"  to LessonInfo(13, "Косвенная речь: сдвиг времён"),

        // Блок 15 (u15) · B2 «Comunicación formal»
        "u15_l0"   to LessonInfo(1,  "Регистры: formal / coloquial"),
        "u15_l1"   to LessonInfo(2,  "Carta formal: запросы / жалобы"),
        "u15_l2"   to LessonInfo(3,  "Informe escrito: структура"),
        "u15_l4"   to LessonInfo(5,  "Конекторы контраста"),
        "u15_l6"   to LessonInfo(7,  "Конекторы причины"),
        "u15_l7"   to LessonInfo(8,  "Конекторы следствия + Subj"),
        "u15_l8"   to LessonInfo(9,  "Конекторы уступки"),

        // Блок 16 (u16) · B2 «Léxico y cultura»
        "u16_l0"   to LessonInfo(1,  "Modismos B2: a rajatabla..."),
        "u16_l4"   to LessonInfo(5,  "Метафорический язык"),
        "u16_l11"  to LessonInfo(14, "Cultura hispana"),
        "u16_l12"  to LessonInfo(15, "Tricky cases: sino / por / para"),
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
