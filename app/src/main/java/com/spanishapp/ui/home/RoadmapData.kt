package com.spanishapp.ui.home

import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════════
//  ROADMAP — 30 блоков по методике Instituto Cervantes / CEFR
//  A1: 1-10 · A2: 11-18 · B1: 19-25 · B2: 26-30
//
//  isLocked и progress НЕ задаются здесь —
//  они вычисляются в HomeViewModel из таблицы lesson_progress.
// ══════════════════════════════════════════════════════════════

object RoadmapData {
    val units = listOf(

        // ── A1 ── PRIMEROS PASOS ─────────────────────────────────

        RoadmapUnit(
            id = "1", title = "¡Hola, mundo!", icon = "👋",
            description = "Алфавит, звуки, первые слова",
            cefrLevel = "A1", color = Color(0xFF43A047),
            lessons = listOf(
                RoadmapLesson("Алфавит и произношение",          "content", "general"),
                RoadmapLesson("Приветствия: Hola / Buenos días", "content", "phrases"),
                RoadmapLesson("Числа 0–20 и возраст",            "vocab",   "general"),
                RoadmapLesson("Тест: Первый контакт",            "quiz",    "general")
            )
        ),

        RoadmapUnit(
            id = "2", title = "¿Quién eres?", icon = "🙋",
            description = "Личность, страны, глагол SER",
            cefrLevel = "A1", color = Color(0xFF4CAF50),
            lessons = listOf(
                RoadmapLesson("Глагол SER: soy, eres, es...",  "content", "general"),
                RoadmapLesson("Страны и национальности",        "vocab",   "general"),
                RoadmapLesson("Личные местоимения yo/tú/él",   "content", "general"),
                RoadmapLesson("Диалог: Знакомство",             "quiz",    "phrases")
            )
        ),

        RoadmapUnit(
            id = "3", title = "Mi Familia", icon = "👨‍👩‍👧",
            description = "Семья, глагол TENER, притяжательные",
            cefrLevel = "A1", color = Color(0xFF66BB6A),
            lessons = listOf(
                RoadmapLesson("Члены семьи: padre, madre, hermano...", "vocab",    "familia"),
                RoadmapLesson("Глагол TENER: tengo, tienes...",        "content",  "general"),
                RoadmapLesson("Притяжательные: mi, tu, su, nuestro",   "content",  "general"),
                RoadmapLesson("Рассказ о семье",                        "quiz",     "familia")
            )
        ),

        RoadmapUnit(
            id = "4", title = "En Casa", icon = "🏠",
            description = "Дом, мебель, глагол ESTAR",
            cefrLevel = "A1", color = Color(0xFF8BC34A),
            lessons = listOf(
                RoadmapLesson("Комнаты: sala, cocina, dormitorio",    "vocab",   "casa_hogar"),
                RoadmapLesson("Мебель и предметы",                     "vocab",   "casa_hogar"),
                RoadmapLesson("Глагол ESTAR: ¿dónde está...?",        "content", "general"),
                RoadmapLesson("Предлоги места: en, sobre, debajo",     "content", "general")
            )
        ),

        RoadmapUnit(
            id = "5", title = "¡A Comer!", icon = "🍽️",
            description = "Еда, напитки, артикли, GUSTAR",
            cefrLevel = "A1", color = Color(0xFFA5D63C),
            lessons = listOf(
                RoadmapLesson("Продукты и блюда",                      "vocab",   "comida_bebida"),
                RoadmapLesson("Артикли el/la/un/una/los/las",          "content", "general"),
                RoadmapLesson("Глагол GUSTAR: me gusta / me gustan",   "content", "general"),
                RoadmapLesson("В кафе: ¿Qué vas a tomar?",            "quiz",    "comida_bebida")
            )
        ),

        RoadmapUnit(
            id = "6", title = "La Ciudad", icon = "🏙️",
            description = "Город, транспорт, направления",
            cefrLevel = "A1", color = Color(0xFFFFC107),
            lessons = listOf(
                RoadmapLesson("Места в городе: banco, farmacia, mercado", "vocab",  "ciudad"),
                RoadmapLesson("Транспорт: metro, autobús, taxi",          "vocab",  "viajes"),
                RoadmapLesson("Числа 21–1000 и деньги",                   "vocab",  "general"),
                RoadmapLesson("Как пройти: gira, sigue recto",            "phrase", "phrases")
            )
        ),

        RoadmapUnit(
            id = "7", title = "¿Qué hora es?", icon = "⏰",
            description = "Время, дни, глаголы на -AR",
            cefrLevel = "A1", color = Color(0xFFFFB300),
            lessons = listOf(
                RoadmapLesson("Время: ¿Qué hora es? / Son las...", "content", "general"),
                RoadmapLesson("Дни недели и месяцы",               "vocab",   "general"),
                RoadmapLesson("Глаголы -AR: hablar, trabajar",     "content", "general"),
                RoadmapLesson("Мой типичный день",                  "quiz",    "general")
            )
        ),

        RoadmapUnit(
            id = "8", title = "Ropa y Colores", icon = "👗",
            description = "Одежда, цвета, покупки",
            cefrLevel = "A1", color = Color(0xFFFF9800),
            lessons = listOf(
                RoadmapLesson("Одежда: camisa, pantalón, vestido",  "vocab",   "ropa"),
                RoadmapLesson("Цвета и согласование прилагательных", "vocab",  "general"),
                RoadmapLesson("В магазине: ¿Cuánto cuesta?",        "phrase",  "compras"),
                RoadmapLesson("Степени: más / menos / tan... como", "content", "general")
            )
        ),

        RoadmapUnit(
            id = "9", title = "Cuerpo y Salud", icon = "🏥",
            description = "Тело, здоровье, у врача",
            cefrLevel = "A1", color = Color(0xFFFF7043),
            lessons = listOf(
                RoadmapLesson("Части тела: cabeza, brazo, pierna", "vocab",   "cuerpo"),
                RoadmapLesson("Глагол DOLER: me duele / me duelen","content", "general"),
                RoadmapLesson("Симптомы: tengo fiebre, me duele...", "vocab", "salud"),
                RoadmapLesson("Диалог: У врача",                    "quiz",   "salud")
            )
        ),

        RoadmapUnit(
            id = "10", title = "Mi Rutina Diaria", icon = "🌅",
            description = "Возвратные глаголы, наречия частоты",
            cefrLevel = "A1", color = Color(0xFFFF5722),
            lessons = listOf(
                RoadmapLesson("Возвратные: levantarse, ducharse",          "content", "general"),
                RoadmapLesson("Наречия: siempre, nunca, a veces",          "vocab",   "general"),
                RoadmapLesson("Глаголы -ER/-IR: comer, vivir, escribir",   "content", "general"),
                RoadmapLesson("✅ Тест уровня A1",                          "quiz",    "all")
            )
        ),

        // ── A2 ── EN MARCHA ──────────────────────────────────────

        RoadmapUnit(
            id = "11", title = "Trabajo y Estudio", icon = "💼",
            description = "Профессии, обязанности, DEBER/TENER QUE",
            cefrLevel = "A2", color = Color(0xFFE91E63),
            lessons = listOf(
                RoadmapLesson("Профессии: médico, profesor, abogado",  "vocab",   "profesiones"),
                RoadmapLesson("TENER QUE / DEBER + infinitivo",        "content", "general"),
                RoadmapLesson("Место работы и учёбы",                   "vocab",   "trabajo"),
                RoadmapLesson("Объявление о вакансии",                  "phrase",  "trabajo")
            )
        ),

        RoadmapUnit(
            id = "12", title = "Tiempo Libre", icon = "🎸",
            description = "Хобби, спорт, JUGAR и TOCAR",
            cefrLevel = "A2", color = Color(0xFFC2185B),
            lessons = listOf(
                RoadmapLesson("Хобби и увлечения",                     "vocab",   "ocio"),
                RoadmapLesson("Спорт: JUGAR al fútbol / tenis",       "content", "general"),
                RoadmapLesson("Музыка: TOCAR la guitarra / el piano", "content", "general"),
                RoadmapLesson("Мои выходные",                           "quiz",    "ocio")
            )
        ),

        RoadmapUnit(
            id = "13", title = "Naturaleza y Animales", icon = "🌿",
            description = "Природа, климат, живые существа",
            cefrLevel = "A2", color = Color(0xFF9C27B0),
            lessons = listOf(
                RoadmapLesson("Природа: bosque, río, montaña, mar",  "vocab",   "naturaleza"),
                RoadmapLesson("Животные: domésticos y salvajes",      "vocab",   "animales"),
                RoadmapLesson("Погода: Hace calor / llueve",          "vocab",   "clima"),
                RoadmapLesson("SER vs. ESTAR + adj.",                  "content", "general")
            )
        ),

        RoadmapUnit(
            id = "14", title = "¡De Viaje!", icon = "✈️",
            description = "Путешествия, аэропорт, отель",
            cefrLevel = "A2", color = Color(0xFF7B1FA2),
            lessons = listOf(
                RoadmapLesson("Аэропорт: maleta, vuelo, embarque",    "vocab",   "viajes"),
                RoadmapLesson("Отель: reserva, habitación, recepción","vocab",   "viajes"),
                RoadmapLesson("Бронирование по телефону",              "phrase",  "viajes"),
                RoadmapLesson("Диалог: В аэропорту",                   "quiz",    "viajes")
            )
        ),

        RoadmapUnit(
            id = "15", title = "¿Qué estás haciendo?", icon = "🔄",
            description = "Presente continuo, герундий",
            cefrLevel = "A2", color = Color(0xFF512DA8),
            lessons = listOf(
                RoadmapLesson("Герундий: hablando, comiendo, viviendo","content", "general"),
                RoadmapLesson("ESTAR + gerundio: está durmiendo",      "content", "general"),
                RoadmapLesson("Сейчас vs. обычно: diferencia de uso", "content", "general"),
                RoadmapLesson("Что сейчас происходит?",                "quiz",    "general")
            )
        ),

        RoadmapUnit(
            id = "16", title = "El Pasado: Perfecto", icon = "✅",
            description = "Pretérito Perfecto, haber + participio",
            cefrLevel = "A2", color = Color(0xFF303F9F),
            lessons = listOf(
                RoadmapLesson("Participio: hablado, comido, ido",               "content", "general"),
                RoadmapLesson("Haber + participio: he comido",                  "content", "general"),
                RoadmapLesson("Irregulares: hecho, dicho, visto",               "content", "general"),
                RoadmapLesson("Маркеры: hoy, esta semana, ya, todavía no",     "quiz",    "general")
            )
        ),

        RoadmapUnit(
            id = "17", title = "El Pasado: Indefinido", icon = "📅",
            description = "Pretérito Indefinido, события прошлого",
            cefrLevel = "A2", color = Color(0xFF1976D2),
            lessons = listOf(
                RoadmapLesson("Правильные: -é, -aste, -ó...",              "content", "general"),
                RoadmapLesson("Неправильные: ser/ir, estar, tener, hacer", "content", "general"),
                RoadmapLesson("Маркеры: ayer, la semana pasada, en 2020",  "vocab",   "general"),
                RoadmapLesson("Рассказ о вчерашнем дне",                   "quiz",    "general")
            )
        ),

        RoadmapUnit(
            id = "18", title = "El Pasado: Imperfecto", icon = "🕰️",
            description = "Привычки прошлого, описания, контраст",
            cefrLevel = "A2", color = Color(0xFF0288D1),
            lessons = listOf(
                RoadmapLesson("Формы: -aba/-ía, era, tenía, hacía",    "content", "general"),
                RoadmapLesson("Привычки: de niño/a siempre...",         "content", "general"),
                RoadmapLesson("Описание: había, era, estaba",           "content", "general"),
                RoadmapLesson("✅ Indefinido vs. Imperfecto — тест A2", "quiz",    "general")
            )
        ),

        // ── B1 ── AVANZANDO ──────────────────────────────────────

        RoadmapUnit(
            id = "19", title = "El Futuro", icon = "🚀",
            description = "Futuro simple и IR A + infinitivo",
            cefrLevel = "B1", color = Color(0xFF00796B),
            lessons = listOf(
                RoadmapLesson("IR A + infinitivo: voy a estudiar",        "content", "general"),
                RoadmapLesson("Futuro Simple: hablaré, comeré, viviré",   "content", "general"),
                RoadmapLesson("Неправильные: tendré, haré, podré, diré", "content", "general"),
                RoadmapLesson("Планы и предсказания",                      "quiz",    "general")
            )
        ),

        RoadmapUnit(
            id = "20", title = "El Condicional", icon = "💭",
            description = "«Бы»: советы, мечты, вежливость",
            cefrLevel = "B1", color = Color(0xFF00897B),
            lessons = listOf(
                RoadmapLesson("Формы: hablaría, comería, viviría",      "content", "general"),
                RoadmapLesson("Советы: Yo (que tú) estudiaría más...",  "content", "general"),
                RoadmapLesson("Просьбы: ¿Podría ayudarme?",            "content", "general"),
                RoadmapLesson("Мечты и желания",                         "quiz",    "general")
            )
        ),

        RoadmapUnit(
            id = "21", title = "¡Hazlo!", icon = "📢",
            description = "Повелительное наклонение",
            cefrLevel = "B1", color = Color(0xFF388E3C),
            lessons = listOf(
                RoadmapLesson("Imperativo afirmativo: habla, come",  "content", "general"),
                RoadmapLesson("Usted/Ustedes: hable, coman",         "content", "general"),
                RoadmapLesson("Imperativo negativo: no hables",      "content", "general"),
                RoadmapLesson("Рецепты и инструкции",                 "quiz",    "general")
            )
        ),

        RoadmapUnit(
            id = "22", title = "Opiniones y Debate", icon = "🗣️",
            description = "Мнение, аргументация, характер",
            cefrLevel = "B1", color = Color(0xFF689F38),
            lessons = listOf(
                RoadmapLesson("Мнение: creo que, pienso que",         "content", "general"),
                RoadmapLesson("Согласие: (no) estoy de acuerdo",      "content", "general"),
                RoadmapLesson("Характер: honesto, creativo, amable",  "vocab",   "sentimientos"),
                RoadmapLesson("Мини-дебаты: тема экологии",            "quiz",    "phrases")
            )
        ),

        RoadmapUnit(
            id = "23", title = "Tecnología y Redes", icon = "📱",
            description = "Интернет, гаджеты, современный язык",
            cefrLevel = "B1", color = Color(0xFFF57F17),
            lessons = listOf(
                RoadmapLesson("Гаджеты: app, wifi, contraseña",       "vocab",  "tecnologia"),
                RoadmapLesson("Соцсети: seguidor, me gusta, publicar","vocab",  "tecnologia"),
                RoadmapLesson("Сленг: guay, molar, tío, flipar",      "vocab",  "expresiones"),
                RoadmapLesson("Переписка в мессенджере",               "quiz",   "tecnologia")
            )
        ),

        RoadmapUnit(
            id = "24", title = "Cultura Hispana", icon = "💃",
            description = "Традиции, праздники, испанские культуры",
            cefrLevel = "B1", color = Color(0xFFE65100),
            lessons = listOf(
                RoadmapLesson("Праздники: Navidad, Semana Santa",        "vocab", "cultura"),
                RoadmapLesson("Латинская Америка: страны и особенности", "vocab", "cultura"),
                RoadmapLesson("Еда: tapas, paella, mate",                "vocab", "comida_bebida"),
                RoadmapLesson("Фламенко, сиеста и символы Испании",      "quiz",  "cultura")
            )
        ),

        RoadmapUnit(
            id = "25", title = "Subjuntivo: Inicio", icon = "🌀",
            description = "Субхунтив настоящего: желания, эмоции",
            cefrLevel = "B1", color = Color(0xFFBF360C),
            lessons = listOf(
                RoadmapLesson("Формы: hable, coma, viva",                "content", "general"),
                RoadmapLesson("Желания: quiero que vengas, espero que...", "content", "general"),
                RoadmapLesson("Эмоции + субхунтив: me alegra que...",    "content", "general"),
                RoadmapLesson("✅ Тест уровня B1",                        "quiz",    "all")
            )
        ),

        // ── B2 ── MAESTRÍA ────────────────────────────────────────

        RoadmapUnit(
            id = "26", title = "Subjuntivo: Duda", icon = "❓",
            description = "Субхунтив: сомнение, отрицание",
            cefrLevel = "B2", color = Color(0xFF6A1B9A),
            lessons = listOf(
                RoadmapLesson("Сомнение: no creo que, dudo que...",  "content", "general"),
                RoadmapLesson("Cuando + субхунтив (будущее)",         "content", "general"),
                RoadmapLesson("Para que, antes de que + субхунтив", "content", "general"),
                RoadmapLesson("Практика: реальные диалоги B2",        "quiz",    "general")
            )
        ),

        RoadmapUnit(
            id = "27", title = "Si... (Condiciones)", icon = "🔀",
            description = "Условные предложения 1-го и 2-го типа",
            cefrLevel = "B2", color = Color(0xFF4A148C),
            lessons = listOf(
                RoadmapLesson("1-й тип: Si + presente → futuro",         "content", "general"),
                RoadmapLesson("2-й тип: Si + imperfecto subj. → cond.", "content", "general"),
                RoadmapLesson("Si yo fuera... / Si tuviera...",           "content", "general"),
                RoadmapLesson("Контрфактические ситуации",                "quiz",    "general")
            )
        ),

        RoadmapUnit(
            id = "28", title = "Estilo y Registro", icon = "✍️",
            description = "Пассивный залог, косвенная речь",
            cefrLevel = "B2", color = Color(0xFF1A237E),
            lessons = listOf(
                RoadmapLesson("Пассивный залог: ser + participio",       "content", "general"),
                RoadmapLesson("SE impersonal: se dice, se vende",        "content", "general"),
                RoadmapLesson("Косвенная речь: dijo que / preguntó si", "content", "general"),
                RoadmapLesson("Формальный vs. разговорный стиль",        "quiz",    "phrases")
            )
        ),

        RoadmapUnit(
            id = "29", title = "El Mundo Actual", icon = "🌍",
            description = "Общество, экология, СМИ, дебаты",
            cefrLevel = "B2", color = Color(0xFF0D47A1),
            lessons = listOf(
                RoadmapLesson("Экология: cambio climático, reciclaje",   "vocab", "ecologia"),
                RoadmapLesson("Политика: democracia, derechos",           "vocab", "politica"),
                RoadmapLesson("СМИ: el titular, la noticia, el reportaje","vocab", "medios"),
                RoadmapLesson("Дебаты: аргументы за и против",           "quiz",  "phrases")
            )
        ),

        RoadmapUnit(
            id = "30", title = "¡Lo Logré!", icon = "🎓",
            description = "Финальный экзамен A1→B2",
            cefrLevel = "B2", color = Color(0xFF212121),
            lessons = listOf(
                RoadmapLesson("Финальный словарный марафон",             "vocab", "all"),
                RoadmapLesson("Финальный грамматический тест",           "quiz",  "all"),
                RoadmapLesson("Комплексный диалог: всё вместе",         "quiz",  "all"),
                RoadmapLesson("🏆 Сертификат — ¡Enhorabuena!",         "quiz",  "all")
            )
        )
    )
}
