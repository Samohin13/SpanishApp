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
            color = Color(0xFF5B21B6),
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
            color = Color(0xFF7C3AED),
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
            color = Color(0xFF9333EA),
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
            color = Color(0xFFA855F7),
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
            id = "5",
            title = "A2 · Блок 1: В прошлом",
            icon = "📅",
            description = "Pretérito Indefinido — рассказываем о прошлом",
            cefrLevel = "A2",
            color = Color(0xFF0E7490),
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
                RoadmapLesson("💬 Диалог: «Расскажи о своих выходных»",            "content", "phrases"),
                RoadmapLesson("📅 Irregulares: poder → pude, saber → supe",        "content", "general"),
                RoadmapLesson("📅 Irregulares: dar → di, ver → vi, decir → dije",  "content", "general"),
                RoadmapLesson("🗣️ Рассказ в прошлом: связный текст",               "content", "general"),
                RoadmapLesson("🎯 Тест: Pretérito Indefinido полный",               "quiz",    "all")
            )
        ),

        // ══════════════════════════════════════════════
        //  A2: БЛОК 2 — РАНЬШЕ И СЕЙЧАС
        // ══════════════════════════════════════════════

        RoadmapUnit(
            id = "6",
            title = "A2 · Блок 2: Раньше и сейчас",
            icon = "🕰️",
            description = "Imperfecto, сравнения, местоимения, hace...que",
            cefrLevel = "A2",
            color = Color(0xFF0891B2),
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
                RoadmapLesson("👗 Одежда и мода: talla, probarse, quedar bien",     "content",   "ropa"),
                RoadmapLesson("📝 Por vs Para: продвинутый уровень",                "content", "general"),
                RoadmapLesson("😊 Эмоции: alegría, tristeza, miedo, sorpresa",     "content",   "emociones"),
                RoadmapLesson("🏁 Чекпоинт: «Расскажи о своём детстве»",           "quiz",    "all")
            )
        ),

        // ══════════════════════════════════════════════
        //  A2: БЛОК 3 — СЕЙЧАС И СКОРО
        // ══════════════════════════════════════════════

        RoadmapUnit(
            id = "7",
            title = "A2 · Блок 3: Сейчас и скоро",
            icon = "⚡",
            description = "Pretérito Perfecto, герундий, императив, работа",
            cefrLevel = "A2",
            color = Color(0xFF06B6D4),
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
                RoadmapLesson("✈️ В путешествии: hotel, billete, reserva",          "content",   "viajes"),
                RoadmapLesson("🔗 Придаточные с que: creo que / pienso que",        "content", "general"),
                RoadmapLesson("🍽️ Гастрономия: tapas, paella, tortilla española",  "content",   "comida_bebida"),
                RoadmapLesson("🏁 Чекпоинт: «Мой обычный день»",                   "quiz",    "all")
            )
        ),

        // ══════════════════════════════════════════════
        //  A2: БЛОК 4 — МЕЧТЫ И ПЛАНЫ
        // ══════════════════════════════════════════════

        RoadmapUnit(
            id = "8",
            title = "A2 · Блок 4: Мечты и планы",
            icon = "🚀",
            description = "Futuro, Condicional, si-clauses, мечты",
            cefrLevel = "A2",
            color = Color(0xFF22D3EE),
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
                RoadmapLesson("🌿 Природа и погода: el campo, el mar, hace viento", "content",   "general"),
                RoadmapLesson("📏 Cuantificadores: mucho, poco, bastante, demasiado","content","general"),
                RoadmapLesson("📱 Технологии: app, wifi, contraseña, descargar",   "content",   "tecnologia"),
                RoadmapLesson("💪 Спорт и здоровье: hacer ejercicio, llevar dieta","content",   "salud"),
                RoadmapLesson("🏆 ФИНАЛЬНЫЙ БОСС A2: «Планирование путешествия»",  "quiz",    "all")
            )
        ),

        // ══════════════════════════════════════════════
        //  БЛОК 1 B1: SUBJUNTIVO PRESENTE  (уроки 1–15)
        // ══════════════════════════════════════════════

        RoadmapUnit(
            id = "9",
            title = "Блок 1: Subjuntivo",
            icon = "🔮",
            description = "Presente de Subjuntivo — желания, мнения, эмоции, сомнения",
            cefrLevel = "B1",
            color = Color(0xFF047857),
            lessons = listOf(
                // 1
                RoadmapLesson("🔮 Subjuntivo: что это и зачем",                     "content", "general"),
                // 2
                RoadmapLesson("🔮 Regulares -AR: hablar → hable",                   "content", "general"),
                // 3
                RoadmapLesson("🔮 Regulares -ER/-IR: comer → coma, vivir → viva",   "content", "general"),
                // 4
                RoadmapLesson("🔮 Irregulares: ser → sea, ir → vaya, estar → esté", "content", "general"),
                // 5
                RoadmapLesson("🔮 Irregulares: e→ie, o→ue (querer, poder)",         "content", "general"),
                // 6
                RoadmapLesson("💭 Querer que + Subjuntivo",                          "content", "general"),
                // 7
                RoadmapLesson("💭 Esperar / Necesitar / Pedir que",                  "content", "general"),
                // 8
                RoadmapLesson("💡 Es importante / necesario / bueno que",            "content", "general"),
                // 9
                RoadmapLesson("😊 Me alegra que / Temo que — эмоции",               "content", "general"),
                // 10
                RoadmapLesson("🚫 No creer que / Dudar que — сомнение",             "content", "general"),
                // 11
                RoadmapLesson("🌟 Ojalá + Subjuntivo — мечты и надежды",            "content", "phrases"),
                // 12
                RoadmapLesson("🎯 Para que + Subjuntivo — цель",                     "content", "general"),
                // 13
                RoadmapLesson("⏰ Cuando + Subjuntivo — будущее время",              "content", "general"),
                // 14
                RoadmapLesson("📝 Aunque: факт vs гипотеза",                         "content", "general"),
                // 15  ЧЕКПОИНТ
                RoadmapLesson("🏁 Чекпоинт: «Совет другу»",                         "quiz",    "all")
            )
        ),

        // ══════════════════════════════════════════════
        //  БЛОК 2 B1: CONDICIONAL E HIPÓTESIS  (уроки 16–30)
        // ══════════════════════════════════════════════

        RoadmapUnit(
            id = "10",
            title = "Блок 2: Condicional",
            icon = "💫",
            description = "Condicional Simple, Si-клаузы, Imperfecto de Subjuntivo",
            cefrLevel = "B1",
            color = Color(0xFF059669),
            lessons = listOf(
                // 16
                RoadmapLesson("💫 Condicional Simple: hablaría, comería",            "content", "general"),
                // 17
                RoadmapLesson("💫 Regulares -AR: hablar → hablaría",                 "content", "general"),
                // 18
                RoadmapLesson("💫 Regulares -ER/-IR: comer → comería",               "content", "general"),
                // 19
                RoadmapLesson("⚡ Irregulares 1: tener/poder/saber/haber",            "content", "general"),
                // 20
                RoadmapLesson("⚡ Irregulares 2: hacer/querer/venir/salir",           "content", "general"),
                // 21
                RoadmapLesson("🔀 Si + Presente + Futuro (тип 1: реальное)",          "content", "general"),
                // 22
                RoadmapLesson("📚 Imperfecto de Subjuntivo: введение",               "content", "general"),
                // 23
                RoadmapLesson("📚 Imperfecto Subj. regulares: -ra формы",            "content", "general"),
                // 24
                RoadmapLesson("📚 Imperfecto Subj. irregulares: fuera, tuviera",     "content", "general"),
                // 25
                RoadmapLesson("🔀 Si + Imp.Subj. + Condicional (тип 2: гипотеза)",   "content", "general"),
                // 26
                RoadmapLesson("💡 Советы: Yo en tu lugar... / Yo que tú...",          "content", "phrases"),
                // 27
                RoadmapLesson("🙏 Вежливость: ¿Podrías...? ¿Te importaría...?",      "content", "phrases"),
                // 28
                RoadmapLesson("❓ Quizás / Tal vez + Subj. o Ind.",                   "content", "general"),
                // 29
                RoadmapLesson("❤️ Me gustaría que + Subjuntivo",                     "content", "general"),
                // 30  ЧЕКПОИНТ
                RoadmapLesson("🏁 Чекпоинт: «Если бы я...»",                         "quiz",    "all")
            )
        ),

        // ══════════════════════════════════════════════
        //  БЛОК 3 B1: COMUNICACIÓN AVANZADA  (уроки 31–45)
        // ══════════════════════════════════════════════

        RoadmapUnit(
            id = "11",
            title = "Блок 3: Коммуникация",
            icon = "🗣️",
            description = "Косвенная речь, относительные придаточные, пассив, perífrasis",
            cefrLevel = "B1",
            color = Color(0xFF10B981),
            lessons = listOf(
                // 31
                RoadmapLesson("💬 Estilo indirecto: введение",                        "content", "general"),
                // 32
                RoadmapLesson("💬 Dijo que... / Preguntó si... (настоящее → прошлое)","content", "general"),
                // 33
                RoadmapLesson("💬 Изменение времён в косвенной речи",                 "content", "general"),
                // 34
                RoadmapLesson("💬 Косвенные приказы: pidió que + Imp.Subj.",          "content", "general"),
                // 35
                RoadmapLesson("🔗 Cláusulas relativas: que, quien, donde",            "content", "general"),
                // 36
                RoadmapLesson("🔗 Cuyo / el cual / lo cual",                          "content", "general"),
                // 37
                RoadmapLesson("🎭 Voz pasiva: ser + participio",                      "content", "general"),
                // 38
                RoadmapLesson("🎭 Ser vs Estar + participio: es hecho / está hecho",  "content", "general"),
                // 39
                RoadmapLesson("⚙️ Perífrasis: llevar + gerundio",                     "content", "general"),
                // 40
                RoadmapLesson("⚙️ Perífrasis: seguir/continuar + gerundio",           "content", "general"),
                // 41
                RoadmapLesson("⚙️ Perífrasis: acabar de / volver a + infinitivo",     "content", "general"),
                // 42
                RoadmapLesson("📝 Conectores: sin embargo / por lo tanto / además",   "content", "general"),
                // 43
                RoadmapLesson("📝 Concesión: aunque / a pesar de (que)",              "content", "general"),
                // 44
                RoadmapLesson("📝 Conclusión: en definitiva / en resumen / es decir", "content", "general"),
                // 45  ЧЕКПОИНТ
                RoadmapLesson("🏁 Чекпоинт: «Интервью»",                              "quiz",    "all")
            )
        ),

        // ══════════════════════════════════════════════
        //  БЛОК 4 B1: VOCABULARIO Y EXPRESIÓN  (уроки 46–60)
        // ══════════════════════════════════════════════

        RoadmapUnit(
            id = "12",
            title = "Блок 4: Словарь и стиль",
            icon = "📖",
            description = "Деловой язык, идиомы, СМИ, здоровье, финальный чекпоинт B1",
            cefrLevel = "B1",
            color = Color(0xFF34D399),
            lessons = listOf(
                // 46
                RoadmapLesson("💼 Trabajo: entrevista, empresa, contrato",             "content", "trabajo"),
                // 47
                RoadmapLesson("💼 Correo formal: estimado / adjunto / agradezco",      "content", "trabajo"),
                // 48
                RoadmapLesson("📰 Medios: noticias, reportaje, editorial",             "content", "general"),
                // 49
                RoadmapLesson("📱 Redes sociales: publicar, comentar, seguir",         "content", "tecnologia"),
                // 50
                RoadmapLesson("🏥 Salud: síntoma, diagnóstico, receta, urgencias",     "content", "salud"),
                // 51
                RoadmapLesson("🏥 En el médico: me duele / tengo fiebre / estoy mal",  "content", "salud"),
                // 52
                RoadmapLesson("🎭 Modismos con DAR: dar igual / dar miedo / darse cuenta","content","general"),
                // 53
                RoadmapLesson("🎭 Modismos con TENER: tener ganas / razón / en cuenta","content","general"),
                // 54
                RoadmapLesson("🎭 Modismos con HACER: hacer falta / caso / ilusión",   "content","general"),
                // 55
                RoadmapLesson("🎭 Modismos con LLEVAR: llevar a cabo / la contraria",  "content","general"),
                // 56
                RoadmapLesson("✍️ Registro formal vs coloquial: diferencias clave",    "content","general"),
                // 57
                RoadmapLesson("✍️ Carta de solicitud: estructura y fórmulas",          "content","general"),
                // 58
                RoadmapLesson("🗣️ Debatir: expresar acuerdo / desacuerdo / matizar",  "content","general"),
                // 59
                RoadmapLesson("🗣️ Argumentar: por un lado... por otro lado...",        "content","general"),
                // 60  ФИНАЛЬНЫЙ ЧЕКПОИНТ B1
                RoadmapLesson("🏆 Чекпоинт «Финал B1»",                               "quiz",   "all")
            )
        ),

        // ── B2 ──────────────────────────────────────────────────────
        RoadmapUnit(
            id = "13",
            title = "Блок 1: Subjuntivo Avanzado",
            icon = "🔮",
            description = "Imperfecto y Pluscuamperfecto de Subjuntivo — гипотезы, желания в прошлом, нереальные условия",
            cefrLevel = "B2",
            color = Color(0xFF9F1239),
            lessons = listOf(
                RoadmapLesson("🔮 Subjuntivo Imperfecto: -ra и -se формы",              "content", "general"),
                RoadmapLesson("🔮 Образование: tablara/hablase, irregulares",           "content", "general"),
                RoadmapLesson("🔮 Si + Imp.Subj. + Condicional: нереальные условия",   "content", "general"),
                RoadmapLesson("🔮 Ojalá + Imp.Subj.: «Если бы только...»",             "content", "general"),
                RoadmapLesson("🔮 Como si... — «как будто»",                            "content", "general"),
                RoadmapLesson("🎯 Мини-тест: Subjuntivo Imperfecto",                    "quiz",    "all"),
                RoadmapLesson("🔮 Pluscuamperfecto de Subjuntivo: hubiera + participio","content", "general"),
                RoadmapLesson("🔮 Si hubiera sabido... — сожаление о прошлом",         "content", "general"),
                RoadmapLesson("🔮 Condicional Compuesto: habría viajado",               "content", "general"),
                RoadmapLesson("🔮 Que yo sepa / que yo recuerde — устойчивые формы",   "content", "general"),
                RoadmapLesson("🔮 Aunque + Subj. vs Indicativo: оттенки смысла",       "content", "general"),
                RoadmapLesson("🔮 Subj. в придаточных цели: para que, a fin de que",   "content", "general"),
                RoadmapLesson("🔮 Subj. в придаточных времени: cuando llegues",        "content", "general"),
                RoadmapLesson("🔮 Subj. в придаточных относительных: alguien que sepa","content", "general"),
                RoadmapLesson("🏆 Чекпоинт Блока 1 B2",                                "quiz",    "all")
            )
        ),

        RoadmapUnit(
            id = "14",
            title = "Блок 2: Pasiva y Perífrasis",
            icon = "⚙️",
            description = "Пассивный залог, перифразы, сложные глагольные конструкции",
            cefrLevel = "B2",
            color = Color(0xFFBE185D),
            lessons = listOf(
                RoadmapLesson("⚙️ Voz pasiva con SER: fue construido",                 "content", "general"),
                RoadmapLesson("⚙️ Estar + participio: está hecho / están cerrados",    "content", "general"),
                RoadmapLesson("⚙️ Se pasivo y se impersonal",                          "content", "general"),
                RoadmapLesson("⚙️ Perífrasis: ir a / acabar de / volver a",            "content", "general"),
                RoadmapLesson("⚙️ Perífrasis: llevar + gerundio",                      "content", "general"),
                RoadmapLesson("🎯 Мини-тест: Pasiva y Perífrasis",                     "quiz",    "all"),
                RoadmapLesson("⚙️ Perífrasis: seguir / dejar de / ponerse a",          "content", "general"),
                RoadmapLesson("⚙️ Participio como adjetivo: una puerta cerrada",       "content", "general"),
                RoadmapLesson("⚙️ Gerundio: usos avanzados — habiendo llegado",        "content", "general"),
                RoadmapLesson("⚙️ Infinitivo: como sujeto y complemento",              "content", "general"),
                RoadmapLesson("💬 Cláusulas relativas: que, quien, cuyo, donde",       "content", "general"),
                RoadmapLesson("💬 Estilo indirecto: cambio de tiempos verbales",       "content", "general"),
                RoadmapLesson("💬 Ser vs Estar: нюансы и сложные случаи",              "content", "general"),
                RoadmapLesson("💬 Nominalización: de verbo a sustantivo",              "content", "general"),
                RoadmapLesson("🏆 Чекпоинт Блока 2 B2",                                "quiz",    "all")
            )
        ),

        RoadmapUnit(
            id = "15",
            title = "Блок 3: Comunicación Formal",
            icon = "✍️",
            description = "Академический стиль, официальные письма, продвинутые коннекторы",
            cefrLevel = "B2",
            color = Color(0xFFDB2777),
            lessons = listOf(
                RoadmapLesson("✍️ Регистры: formal, neutro, coloquial",                "content", "general"),
                RoadmapLesson("✍️ Carta formal: solicitud, queja, agradecimiento",     "content", "general"),
                RoadmapLesson("✍️ Informe escrito: структура и обороты",               "content", "general"),
                RoadmapLesson("✍️ Artículo de opinión: структура",                    "content", "general"),
                RoadmapLesson("🔗 Коннекторы контраста: sin embargo, no obstante",    "content", "general"),
                RoadmapLesson("🎯 Мини-тест: Conectores y Registro",                   "quiz",    "all"),
                RoadmapLesson("🔗 Коннекторы причины: dado que, puesto que, ya que",   "content", "general"),
                RoadmapLesson("🔗 Коннекторы следствия: de ahí que, de modo que",     "content", "general"),
                RoadmapLesson("🔗 Коннекторы уступки: a pesar de que, si bien",       "content", "general"),
                RoadmapLesson("🗣️ Аргументация: тезис, доказательство, вывод",        "content", "general"),
                RoadmapLesson("🗣️ Цитирование: según, de acuerdo con, a juicio de",   "content", "general"),
                RoadmapLesson("📚 Latinismos y cultismos: per se, a posteriori",       "content", "general"),
                RoadmapLesson("📚 Nominalización в деловом языке",                    "content", "general"),
                RoadmapLesson("📚 Léxico académico: demostrar, evidenciar, sostener",  "content", "general"),
                RoadmapLesson("🏆 Чекпоинт Блока 3 B2",                                "quiz",    "all")
            )
        ),

        RoadmapUnit(
            id = "16",
            title = "Блок 4: Léxico y Cultura",
            icon = "🌟",
            description = "Идиомы, пословицы, ложные друзья, латиноамериканский испанский, финальный чекпоинт B2",
            cefrLevel = "B2",
            color = Color(0xFFE11D48),
            lessons = listOf(
                RoadmapLesson("🌟 Modismos B2: a rajatabla, en boca de todos",         "content", "general"),
                RoadmapLesson("🌟 Modismos B2: no hay mal que... / a lo mejor",        "content", "general"),
                RoadmapLesson("🌟 Refranes: El que mucho abarca...",                   "content", "general"),
                RoadmapLesson("🌟 Eufemismos y lenguaje diplomático",                  "content", "general"),
                RoadmapLesson("🌟 Lenguaje metafórico y figurado",                     "content", "general"),
                RoadmapLesson("🎯 Мини-тест: Modismos y Refranes",                     "quiz",    "all"),
                RoadmapLesson("🌍 Español latinoamericano: основные различия",         "content", "general"),
                RoadmapLesson("🌍 Falsos amigos: embarazada, sensible, actual",        "content", "general"),
                RoadmapLesson("🌍 Diminutivos y aumentativos: casita / hombrón",       "content", "general"),
                RoadmapLesson("📱 Léxico moderno: startup, sostenibilidad, branding",  "content", "general"),
                RoadmapLesson("💼 Léxico profesional: negocios, derecho, medicina",    "content", "general"),
                RoadmapLesson("🎨 Cultura hispana: literatura, arte, historia",        "content", "general"),
                RoadmapLesson("🔤 Tricky cases: sino/pero, también/tampoco, por/para", "content", "general"),
                RoadmapLesson("🔤 Ortografía y puntuación avanzada",                   "content", "general"),
                RoadmapLesson("🏆 ФИНАЛЬНЫЙ ЧЕКПОИНТ B2",                              "quiz",    "all")
            )
        )
    )
}
