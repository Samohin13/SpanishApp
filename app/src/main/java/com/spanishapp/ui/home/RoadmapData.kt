package com.spanishapp.ui.home

import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════════
//  ПОЛНЫЙ КАТАЛОГ: МОДУЛЬ А1 (60 МИКРО-УРОКОВ)
//  Блок 1: Взлёт (1-15) · Блок 2: Мой мир (16-30)
//  Блок 3: Действие (31-45) · Блок 4: Выживание (46-60)
//
//  isLocked и progress НЕ задаются здесь —
//  они вычисляются в HomeViewModel из таблицы lesson_progress.
// ══════════════════════════════════════════════════════════════

object RoadmapData {
    val units = listOf(

        // ══════════════════════════════════════════════
        //  БЛОК 1: ВЗЛЁТ  (уроки 1–15)
        // ══════════════════════════════════════════════

        RoadmapUnit(
            id = "1",
            title = "Блок 1: Взлёт",
            icon = "🚀",
            description = "Произношение, SER, числа, первые слова",
            cefrLevel = "A1",
            color = Color(0xFF7C4DFF),
            lessons = listOf(
                // 1
                RoadmapLesson("🔤 Гласные: A, E, I, O, U",                  "content", "general"),
                // 2
                RoadmapLesson("🔤 Согласные: B/V, D, G — испанские секреты","content", "general"),
                // 3
                RoadmapLesson("🔤 H молчит · J=[х] · Ñ=[нь] · RR=[рр]",    "content", "general"),
                // 4
                RoadmapLesson("🔤 Ударение и тильда",                         "content", "general"),
                // 5
                RoadmapLesson("👋 Hola / Buenos días / ¿Cómo estás?",         "vocab",   "phrases"),
                // 6
                RoadmapLesson("👋 Adiós / Hasta luego / Hasta mañana",        "vocab",   "phrases"),
                // 7
                RoadmapLesson("🙏 Por favor / Gracias / De nada / Perdón",    "vocab",   "phrases"),
                // 8
                RoadmapLesson("🟣 SER: soy, eres, es (yo / tú / él)",         "content", "general"),
                // 9
                RoadmapLesson("🟣 SER: somos, sois, son (мн. число)",         "content", "general"),
                // 10
                RoadmapLesson("👤 Местоимения: yo tú él ella nosotros ellos", "content", "general"),
                // 11
                RoadmapLesson("⚤ Род: el/la — мужской и женский",            "content", "general"),
                // 12
                RoadmapLesson("📰 Артикли: el/la/un/una/los/las",             "content", "general"),
                // 13
                RoadmapLesson("🌍 Страны: Soy ruso/rusa, de Rusia",           "vocab",   "general"),
                // 14
                RoadmapLesson("🔢 Числа 0–10: cero, uno, dos… diez",          "vocab",   "general"),
                // 15  ЧЕКПОИНТ
                RoadmapLesson("🏁 Чекпоинт: «Паспортный контроль»",           "quiz",    "all")
            )
        ),

        // ══════════════════════════════════════════════
        //  БЛОК 2: МОЙ МИР  (уроки 16–30)
        // ══════════════════════════════════════════════

        RoadmapUnit(
            id = "2",
            title = "Блок 2: Мой мир",
            icon = "🏠",
            description = "Семья, дом, цвета, TENER и ESTAR",
            cefrLevel = "A1",
            color = Color(0xFF00BCD4),
            lessons = listOf(
                // 16
                RoadmapLesson("🔢 Числа 11–20: once, doce… veinte",           "vocab",   "general"),
                // 17
                RoadmapLesson("🔢 Числа 21–100: veintiuno, treinta…",          "vocab",   "general"),
                // 18
                RoadmapLesson("🟠 TENER: tengo, tienes, tiene",                "content", "general"),
                // 19
                RoadmapLesson("🟠 TENER: tenemos, tenéis, tienen (мн.)",       "content", "general"),
                // 20
                RoadmapLesson("👨‍👩‍👧 Семья 1: padre, madre, hermano, hijo",        "vocab",   "familia"),
                // 21
                RoadmapLesson("👨‍👩‍👧 Семья 2: abuelo, tío, primo, sobrino",        "vocab",   "familia"),
                // 22
                RoadmapLesson("📎 Притяжательные: mi, tu, su, nuestro/a",     "content", "general"),
                // 23
                RoadmapLesson("🎨 Цвета: rojo, azul, verde, amarillo…",        "vocab",   "general"),
                // 24
                RoadmapLesson("🎨 Согласование: rojo/roja, blanco/blanca",    "content", "general"),
                // 25
                RoadmapLesson("📍 ESTAR: estoy, estás, está — где находишься", "content", "general"),
                // 26
                RoadmapLesson("📍 Предлоги: en/sobre/debajo/al lado de",       "content", "general"),
                // 27
                RoadmapLesson("🏠 Дом: sala, cocina, dormitorio, baño",        "vocab",   "casa_hogar"),
                // 28
                RoadmapLesson("🛋️ Мебель: sofá, mesa, silla, cama, armario",  "vocab",   "casa_hogar"),
                // 29
                RoadmapLesson("📚 Множественное число: -s и -es",              "content", "general"),
                // 30  ЧЕКПОИНТ
                RoadmapLesson("🏁 Чекпоинт: «Аренда жилья»",                   "quiz",    "all")
            )
        ),

        // ══════════════════════════════════════════════
        //  БЛОК 3: ДЕЙСТВИЕ  (уроки 31–45)
        // ══════════════════════════════════════════════

        RoadmapUnit(
            id = "3",
            title = "Блок 3: Действие",
            icon = "⚡",
            description = "Глаголы -AR/-ER/-IR, еда, QUERER, время",
            cefrLevel = "A1",
            color = Color(0xFF4CAF50),
            lessons = listOf(
                // 31
                RoadmapLesson("🔵 Глаголы -AR: hablar, trabajar — yo/tú/él",  "content", "general"),
                // 32
                RoadmapLesson("🔵 Глаголы -AR: полное спряжение",              "content", "general"),
                // 33
                RoadmapLesson("🔵 Глаголы -ER: comer, beber, leer",           "content", "general"),
                // 34
                RoadmapLesson("🔵 Глаголы -IR: vivir, escribir, abrir",       "content", "general"),
                // 35
                RoadmapLesson("🍞 Еда: pan, leche, agua, café, fruta, carne", "vocab",   "comida_bebida"),
                // 36
                RoadmapLesson("🍽️ В ресторане: el menú, el plato, la cuenta", "vocab",   "comida_bebida"),
                // 37
                RoadmapLesson("❤️ QUERER: quiero, quieres, quiere (хотеть)",   "content", "general"),
                // 38
                RoadmapLesson("💪 PODER: puedo, puedes, puede (мочь)",         "content", "general"),
                // 39
                RoadmapLesson("⏰ Время: ¿Qué hora es? Son las… Es la una",   "content", "general"),
                // 40
                RoadmapLesson("📅 Дни недели: lunes, martes… domingo",         "vocab",   "general"),
                // 41
                RoadmapLesson("📅 Месяцы: enero, febrero… diciembre",          "vocab",   "general"),
                // 42
                RoadmapLesson("⏱️ ¿Cuándo? hoy / mañana / ayer / ahora",      "vocab",   "general"),
                // 43
                RoadmapLesson("❓ Вопросы: ¿Qué? ¿Quién? ¿Dónde? ¿Cuánto?",  "content", "general"),
                // 44
                RoadmapLesson("🚫 Отрицание: No + глагол / nunca / jamás",    "content", "general"),
                // 45  ЧЕКПОИНТ
                RoadmapLesson("🏁 Чекпоинт: «Обед в ресторане»",               "quiz",    "all")
            )
        ),

        // ══════════════════════════════════════════════
        //  БЛОК 4: ВЫЖИВАНИЕ  (уроки 46–60)
        // ══════════════════════════════════════════════

        RoadmapUnit(
            id = "4",
            title = "Блок 4: Выживание",
            icon = "🗺️",
            description = "Транспорт, IR, шопинг, GUSTAR, тело, финальный босс",
            cefrLevel = "A1",
            color = Color(0xFFFF6F00),
            lessons = listOf(
                // 46
                RoadmapLesson("🚇 Транспорт: metro, autobús, taxi, tren",      "vocab",   "viajes"),
                // 47
                RoadmapLesson("🏃 IR: voy, vas, va, vamos, vais, van",         "content", "general"),
                // 48
                RoadmapLesson("🏃 IR + A + lugar: voy al colegio",             "content", "general"),
                // 49
                RoadmapLesson("🗺️ Дорога: ¿Cómo llego? Gira / Sigue recto",  "vocab",   "ciudad"),
                // 50
                RoadmapLesson("🛒 Магазин: ¿Cuánto cuesta? caro / barato",    "vocab",   "compras"),
                // 51
                RoadmapLesson("💶 Деньги: el euro, el precio, ¿Tiene cambio?", "vocab",   "compras"),
                // 52
                RoadmapLesson("❤️ GUSTAR: me gusta / me gustan",               "content", "general"),
                // 53
                RoadmapLesson("❤️ GUSTAR: te gusta / le gusta / nos gusta",   "content", "general"),
                // 54
                RoadmapLesson("🦴 Тело: cabeza, brazo, pierna, mano, ojo",     "vocab",   "cuerpo"),
                // 55
                RoadmapLesson("🤒 Здоровье: Me duele… / Tengo fiebre",         "vocab",   "salud"),
                // 56
                RoadmapLesson("👗 Одежда: camisa, pantalón, vestido, zapatos", "vocab",   "ropa"),
                // 57
                RoadmapLesson("🌤️ Погода: Hace calor/frío / Llueve / Nieva",  "vocab",   "general"),
                // 58
                RoadmapLesson("🌅 Мой день: me levanto, desayuno, trabajo…",   "vocab",   "general"),
                // 59
                RoadmapLesson("🔄 Возвратные: levantarse, ducharse, acostarse","content", "general"),
                // 60  ФИНАЛЬНЫЙ БОСС
                RoadmapLesson("🏆 ФИНАЛЬНЫЙ БОСС: «Один день в Мадриде»",      "quiz",    "all")
            )
        )
    )
}
