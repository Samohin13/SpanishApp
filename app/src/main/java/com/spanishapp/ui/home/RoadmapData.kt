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
                RoadmapLesson("👋 Hola / Buenos días / ¿Cómo estás?",         "content", "phrases"),
                // 6
                RoadmapLesson("👋 Adiós / Hasta luego / Hasta mañana",        "content", "phrases"),
                // 7
                RoadmapLesson("🙏 Por favor / Gracias / De nada / Perdón",    "content", "phrases"),
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
                RoadmapLesson("🌍 Страны: Soy ruso/rusa, de Rusia",           "content", "general"),
                // 14
                RoadmapLesson("🔢 Числа 0–10: cero, uno, dos… diez",          "content", "general"),
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
                RoadmapLesson("🔢 Числа 11–20: once, doce… veinte",           "content", "general"),
                // 17
                RoadmapLesson("🔢 Числа 21–100: veintiuno, treinta…",          "content", "general"),
                // 18
                RoadmapLesson("🟠 TENER: tengo, tienes, tiene",                "content", "general"),
                // 19
                RoadmapLesson("🟠 TENER: tenemos, tenéis, tienen (мн.)",       "content", "general"),
                // 20
                RoadmapLesson("👨‍👩‍👧 Семья 1: padre, madre, hermano, hijo",        "content", "familia"),
                // 21
                RoadmapLesson("👨‍👩‍👧 Семья 2: abuelo, tío, primo, sobrino",        "content", "familia"),
                // 22
                RoadmapLesson("📎 Притяжательные: mi, tu, su, nuestro/a",     "content", "general"),
                // 23
                RoadmapLesson("🎨 Цвета: rojo, azul, verde, amarillo…",        "content", "general"),
                // 24
                RoadmapLesson("🎨 Согласование: rojo/roja, blanco/blanca",    "content", "general"),
                // 25
                RoadmapLesson("📍 ESTAR: estoy, estás, está — где находишься", "content", "general"),
                // 26
                RoadmapLesson("📍 Предлоги: en/sobre/debajo/al lado de",       "content", "general"),
                // 27
                RoadmapLesson("🏠 Дом: sala, cocina, dormitorio, baño",        "content", "casa_hogar"),
                // 28
                RoadmapLesson("🛋️ Мебель: sofá, mesa, silla, cama, armario",  "content", "casa_hogar"),
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
                RoadmapLesson("🍞 Еда: pan, leche, agua, café, fruta, carne", "content", "comida_bebida"),
                // 36
                RoadmapLesson("🍽️ В ресторане: el menú, el plato, la cuenta", "content", "comida_bebida"),
                // 37
                RoadmapLesson("❤️ QUERER: quiero, quieres, quiere (хотеть)",   "content", "general"),
                // 38
                RoadmapLesson("💪 PODER: puedo, puedes, puede (мочь)",         "content", "general"),
                // 39
                RoadmapLesson("⏰ Время: ¿Qué hora es? Son las… Es la una",   "content", "general"),
                // 40
                RoadmapLesson("📅 Дни недели: lunes, martes… domingo",         "content", "general"),
                // 41
                RoadmapLesson("📅 Месяцы: enero, febrero… diciembre",          "content", "general"),
                // 42
                RoadmapLesson("⏱️ ¿Cuándo? hoy / mañana / ayer / ahora",      "content", "general"),
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
                RoadmapLesson("🚇 Транспорт: metro, autobús, taxi, tren",      "content", "viajes"),
                // 47
                RoadmapLesson("🏃 IR: voy, vas, va, vamos, vais, van",         "content", "general"),
                // 48
                RoadmapLesson("🏃 IR + A + lugar: voy al colegio",             "content", "general"),
                // 49
                RoadmapLesson("🗺️ Дорога: ¿Cómo llego? Gira / Sigue recto",  "content", "ciudad"),
                // 50
                RoadmapLesson("🛒 Магазин: ¿Cuánto cuesta? caro / barato",    "content", "compras"),
                // 51
                RoadmapLesson("💶 Деньги: el euro, el precio, ¿Tiene cambio?", "content", "compras"),
                // 52
                RoadmapLesson("❤️ GUSTAR: me gusta / me gustan",               "content", "general"),
                // 53
                RoadmapLesson("❤️ GUSTAR: te gusta / le gusta / nos gusta",   "content", "general"),
                // 54
                RoadmapLesson("🦴 Тело: cabeza, brazo, pierna, mano, ojo",     "content", "cuerpo"),
                // 55
                RoadmapLesson("🤒 Здоровье: Me duele… / Tengo fiebre",         "content", "salud"),
                // 56
                RoadmapLesson("👗 Одежда: camisa, pantalón, vestido, zapatos", "content", "ropa"),
                // 57
                RoadmapLesson("🌤️ Погода: Hace calor/frío / Llueve / Nieva",  "content", "general"),
                // 58
                RoadmapLesson("🌅 Мой день: me levanto, desayuno, trabajo…",   "content", "general"),
                // 59
                RoadmapLesson("🔄 Возвратные: levantarse, ducharse, acostarse","content", "general"),
                // 60  ФИНАЛЬНЫЙ БОСС
                RoadmapLesson("🏆 ФИНАЛЬНЫЙ БОСС: «Один день в Мадриде»",      "quiz",    "all")
            )
        ),

        // ══════════════════════════════════════════════
        //  A2: БЛОК 1 — В ПРОШЛОМ
        // ══════════════════════════════════════════════

        RoadmapUnit(
            id = "a2_1",
            title = "A2 · Блок 1: В прошлом",
            icon = "📅",
            description = "Pretérito Indefinido — рассказываем о прошлом",
            cefrLevel = "A2",
            color = Color(0xFF0277BD),
            lessons = listOf(
                RoadmapLesson("📅 Pretérito Indefinido: что это и когда",          "content", "general"),
                RoadmapLesson("📅 Regulares -AR: hablar → hablé, hablaste, habló", "content", "general"),
                RoadmapLesson("📅 Regulares -ER/-IR: comer → comí, vivir → viví",  "content", "general"),
                RoadmapLesson("🌍 Ser vs Estar: ключевые различия",                "content", "general"),
                RoadmapLesson("💬 ¿Qué hiciste ayer? — первые истории",            "content",   "phrases"),
                RoadmapLesson("🎯 Мини-тест: Regulares",                            "quiz",    "all"),
                RoadmapLesson("📅 Irregulares: ir/ser → fui/fue/fuimos",           "content", "general"),
                RoadmapLesson("📅 Irregulares: tener → tuve, estar → estuve",      "content", "general"),
                RoadmapLesson("📅 Irregulares: hacer → hice, querer → quise",      "content", "general"),
                RoadmapLesson("📝 Por vs Para: основы",                             "content", "general"),
                RoadmapLesson("💬 Диалог: «Расскажи о своих выходных»",            "content",   "phrases"),
                RoadmapLesson("🎯 Тест: Pretérito Indefinido полный",               "quiz",    "all")
            )
        ),

        // ══════════════════════════════════════════════
        //  A2: БЛОК 2 — РАНЬШЕ И СЕЙЧАС
        // ══════════════════════════════════════════════

        RoadmapUnit(
            id = "a2_2",
            title = "A2 · Блок 2: Раньше и сейчас",
            icon = "🕰️",
            description = "Imperfecto, сравнения, местоимения, hace...que",
            cefrLevel = "A2",
            color = Color(0xFF0277BD),
            lessons = listOf(
                RoadmapLesson("⏳ Imperfecto -AR: hablaba, trabajaba, estudiaba",   "content", "general"),
                RoadmapLesson("⏳ Imperfecto -ER/-IR: comía, vivía + ser/ir/ver",   "content", "general"),
                RoadmapLesson("⚡ Indefinido vs Imperfecto: когда что использовать","content", "general"),
                RoadmapLesson("📖 Описания из прошлого: era niño, tenía...",        "content",   "phrases"),
                RoadmapLesson("📊 Сравнение: más...que / menos...que",              "content", "general"),
                RoadmapLesson("📊 Сравнение: tan...como / tanto...como",            "content", "general"),
                RoadmapLesson("🏆 Превосходная степень: el más, el mejor",          "content", "general"),
                RoadmapLesson("🎯 Прилагательные-описания: alto, simpático, listo", "content",   "general"),
                RoadmapLesson("👆 Местоимения OD: lo, la, los, las",                "content", "general"),
                RoadmapLesson("👆 Местоимения OI: me, te, le, nos, os, les",       "content", "general"),
                RoadmapLesson("⏱️ Hace + tiempo + que: hace dos años que...",       "content", "general"),
                RoadmapLesson("✈️ В путешествии: hotel, billete, reserva",          "content",   "viajes"),
                RoadmapLesson("📝 Por vs Para: продвинутый уровень",                "content", "general"),
                RoadmapLesson("😊 Эмоции: alegría, tristeza, miedo, sorpresa",     "content",   "emociones"),
                RoadmapLesson("🏁 Чекпоинт: «Расскажи о своём детстве»",           "quiz",    "all")
            )
        ),

        // ══════════════════════════════════════════════
        //  A2: БЛОК 3 — СЕЙЧАС И СКОРО
        // ══════════════════════════════════════════════

        RoadmapUnit(
            id = "a2_3",
            title = "A2 · Блок 3: Сейчас и скоро",
            icon = "⚡",
            description = "Pretérito Perfecto, герундий, императив, работа",
            cefrLevel = "A2",
            color = Color(0xFF0277BD),
            lessons = listOf(
                RoadmapLesson("✅ Pretérito Perfecto: he comido, has vivido",       "content", "general"),
                RoadmapLesson("✅ Participios irregulares: hecho, dicho, visto",    "content", "general"),
                RoadmapLesson("🔀 Perfecto vs Indefinido: когда что использовать",  "content", "general"),
                RoadmapLesson("📌 Ya / Todavía / Aún: уже, ещё, до сих пор",       "content",   "phrases"),
                RoadmapLesson("🔄 Estar + gerundio: estoy comiendo (сейчас)",       "content", "general"),
                RoadmapLesson("🔄 Seguir + gerundio / Llevar + gerundio",           "content", "general"),
                RoadmapLesson("💼 Работа: buscar empleo, currículum, entrevista",   "content",   "trabajo"),
                RoadmapLesson("📢 Imperativo afirmativo: ¡habla! ¡come! ¡escribe!", "content", "general"),
                RoadmapLesson("🚫 Imperativo negativo: ¡no hables! ¡no comas!",    "content", "general"),
                RoadmapLesson("🏥 У врача: síntomas, me duele, tengo fiebre",       "content",   "salud"),
                RoadmapLesson("🔗 ОД + ОИ вместе: te lo doy, se lo digo",           "content", "general"),
                RoadmapLesson("👗 Одежда и мода: talla, probarse, quedar bien",     "content",   "ropa"),
                RoadmapLesson("🔗 Придаточные с que: creo que / pienso que",        "content", "general"),
                RoadmapLesson("🌿 Природа и погода: el campo, el mar, hace viento", "content",   "general"),
                RoadmapLesson("🏁 Чекпоинт: «Мой обычный день»",                   "quiz",    "all")
            )
        ),

        // ══════════════════════════════════════════════
        //  A2: БЛОК 4 — МЕЧТЫ И ПЛАНЫ
        // ══════════════════════════════════════════════

        RoadmapUnit(
            id = "a2_4",
            title = "A2 · Блок 4: Мечты и планы",
            icon = "🚀",
            description = "Futuro, Condicional, si-clauses, мечты",
            cefrLevel = "A2",
            color = Color(0xFF0277BD),
            lessons = listOf(
                RoadmapLesson("🔮 Futuro Simple -AR: hablaré, hablarás, hablará",  "content", "general"),
                RoadmapLesson("🔮 Futuro irregular: tendré, haré, vendré, iré",    "content", "general"),
                RoadmapLesson("💭 Condicional Simple: hablaría, comería",           "content", "general"),
                RoadmapLesson("💭 Condicional irregular: tendría, haría, vendría",  "content", "general"),
                RoadmapLesson("🔀 Si + presente + futuro: si llueve, no saldré",   "content", "general"),
                RoadmapLesson("✨ Планы и мечты: quisiera, me gustaría, espero",   "content",   "phrases"),
                RoadmapLesson("❓ Pronombres indefinidos: algo, alguien, nada",     "content", "general"),
                RoadmapLesson("🎲 Вероятность: probablemente, quizás, a lo mejor", "content", "general"),
                RoadmapLesson("🚗 Транспорт и дорога: alquilar, conducir, aparcar","content",   "viajes"),
                RoadmapLesson("🔗 Глаголы с предлогом: pensar en, soñar con",      "content", "general"),
                RoadmapLesson("🍽️ Гастрономия: tapas, paella, tortilla española", "content",   "comida_bebida"),
                RoadmapLesson("📏 Cuantificadores: mucho, poco, bastante, demasiado","content","general"),
                RoadmapLesson("📱 Технологии: app, wifi, contraseña, descargar",   "content",   "tecnologia"),
                RoadmapLesson("💪 Спорт и здоровье: hacer ejercicio, llevar dieta","content",   "salud"),
                RoadmapLesson("🏆 ФИНАЛЬНЫЙ БОСС A2: «Планирование путешествия»",  "quiz",    "all")
            )
        ),

        // ══════════════════════════════════════════════
        //  B1: БЛОК 1 — ВЫРАЖАЕМ МНЕНИЯ
        //  TODO: Удалить isPremium когда контент B1 готов
        // ══════════════════════════════════════════════

        RoadmapUnit(
            id = "b1_1",
            title = "B1 · Блок 1: Выражаем мнения",
            icon = "💭",
            description = "Subjuntivo Presente — желания, мнения, эмоции",
            cefrLevel = "B1",
            color = Color(0xFFE65100),
            lessons = listOf(
                RoadmapLesson("🔮 Presente de Subjuntivo: что это и зачем",        "content", "general"),
                RoadmapLesson("🔮 Regulares: que hable, que coma, que viva",        "content", "general"),
                RoadmapLesson("💭 Querer que / Esperar que / Necesitar que",        "content", "general"),
                RoadmapLesson("💭 Es importante que / Es necesario que",            "content", "general"),
                RoadmapLesson("😊 Ojalá + Subjuntivo: выражаем желания",           "vocab",   "phrases"),
                RoadmapLesson("🎯 Мини-тест: Subjuntivo",                           "quiz",    "all"),
                RoadmapLesson("🔮 Irregulares: ser → sea, ir → vaya, haber → haya","content", "general",  isPremium = true),
                RoadmapLesson("🔮 Dudar que / No creer que + Subjuntivo",          "content", "general",  isPremium = true),
                RoadmapLesson("🌍 Condicional simple: hablaría, comería",           "content", "general",  isPremium = true),
                RoadmapLesson("💬 Si tuviera tiempo... — гипотезы",                "vocab",   "phrases",  isPremium = true),
                RoadmapLesson("📝 Pero vs Sino vs Sin embargo",                     "content", "general",  isPremium = true),
                RoadmapLesson("🎯 Тест: Subjuntivo + Condicional",                  "quiz",    "all",      isPremium = true)
            )
        ),

        // ══════════════════════════════════════════════
        //  B2: БЛОК 1 — СЛОЖНЫЕ КОНСТРУКЦИИ
        //  TODO: Удалить isPremium когда контент B2 готов
        // ══════════════════════════════════════════════

        RoadmapUnit(
            id = "b2_1",
            title = "B2 · Блок 1: Сложные конструкции",
            icon = "🏆",
            description = "Subjuntivo Imperfecto, пассив, сложный синтаксис",
            cefrLevel = "B2",
            color = Color(0xFF6A1B9A),
            lessons = listOf(
                RoadmapLesson("🔮 Subjuntivo Imperfecto: -ra/-se форма",            "content", "general"),
                RoadmapLesson("🔮 Si + Subj.Imp. + Condicional: нереальные условия","content", "general"),
                RoadmapLesson("📝 Estilo indirecto: dijo que viniera",              "content", "general"),
                RoadmapLesson("🎭 Voz pasiva: fue construido / es conocido",        "content", "general"),
                RoadmapLesson("💬 Cláusulas relativas: que, quien, cuyo",           "vocab",   "phrases"),
                RoadmapLesson("🎯 Мини-тест: Imperfecto + Pasiva",                  "quiz",    "all"),
                RoadmapLesson("🔮 Subjuntivo Pluscuamperfecto: hubiera hablado",    "content", "general",  isPremium = true),
                RoadmapLesson("🔮 Si hubiera sabido... — полные гипотезы",          "content", "general",  isPremium = true),
                RoadmapLesson("📝 Perífrasis: llevar + ger., seguir + ger.",        "content", "general",  isPremium = true),
                RoadmapLesson("🎭 Ser vs Estar с participio: está hecho/es hecho",  "content", "general",  isPremium = true),
                RoadmapLesson("💬 Модизмы: no hay mal que... / a lo mejor",         "vocab",   "phrases",  isPremium = true),
                RoadmapLesson("🎯 Финальный тест уровня B2",                        "quiz",    "all",      isPremium = true)
            )
        )
    )
}
