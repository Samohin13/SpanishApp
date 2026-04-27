package com.spanishapp.ui.home

import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════════
//  ROADMAP — 30 блоков по методике Instituto Cervantes / CEFR
//  A1: 1-10 · A2: 11-18 · B1: 19-25 · B2: 26-30
// ══════════════════════════════════════════════════════════════

object RoadmapData {
    val units = listOf(

        // ── A1 ── PRIMEROS PASOS ─────────────────────────────────

        RoadmapUnit("1", "¡Hola, mundo!", "Алфавит, звуки, первые слова", "👋",
            false, 0f, Color(0xFF43A047), listOf(
                RoadmapLesson("Алфавит и произношение", "vocab", "general"),
                RoadmapLesson("Приветствия: Hola / Buenos días", "vocab", "phrases"),
                RoadmapLesson("Числа 0–20 и возраст", "vocab", "general"),
                RoadmapLesson("Тест: Первый контакт", "quiz", "general")
            )),

        RoadmapUnit("2", "¿Quién eres?", "Личность, страны, глагол SER", "🙋",
            true, 0f, Color(0xFF4CAF50), listOf(
                RoadmapLesson("Глагол SER: soy, eres, es...", "grammar", "verbs"),
                RoadmapLesson("Страны и национальности", "vocab", "general"),
                RoadmapLesson("Личные местоимения yo/tú/él", "grammar", "general"),
                RoadmapLesson("Диалог: Знакомство", "quiz", "phrases")
            )),

        RoadmapUnit("3", "Mi Familia", "Семья, глагол TENER, притяжательные", "👨‍👩‍👧",
            true, 0f, Color(0xFF66BB6A), listOf(
                RoadmapLesson("Члены семьи: padre, madre, hermano...", "vocab", "familia"),
                RoadmapLesson("Глагол TENER: tengo, tienes...", "grammar", "verbs"),
                RoadmapLesson("Притяжательные: mi, tu, su, nuestro", "grammar", "general"),
                RoadmapLesson("Рассказ о семье", "quiz", "familia")
            )),

        RoadmapUnit("4", "En Casa", "Дом, мебель, глагол ESTAR", "🏠",
            true, 0f, Color(0xFF8BC34A), listOf(
                RoadmapLesson("Комнаты дома: sala, cocina, dormitorio", "vocab", "casa_hogar"),
                RoadmapLesson("Мебель и предметы", "vocab", "casa_hogar"),
                RoadmapLesson("Глагол ESTAR: dónde está...", "grammar", "verbs"),
                RoadmapLesson("Предлоги места: en, sobre, debajo", "grammar", "prepositions")
            )),

        RoadmapUnit("5", "¡A Comer!", "Еда, напитки, артикли, GUSTAR", "🍽️",
            true, 0f, Color(0xFFA5D63C), listOf(
                RoadmapLesson("Продукты и блюда", "vocab", "comida_bebida"),
                RoadmapLesson("Артикли el/la/un/una/los/las", "grammar", "general"),
                RoadmapLesson("Глагол GUSTAR: me gusta / me gustan", "grammar", "verbs"),
                RoadmapLesson("В кафе: ¿Qué vas a tomar?", "quiz", "comida_bebida")
            )),

        RoadmapUnit("6", "La Ciudad", "Город, транспорт, направления", "🏙️",
            true, 0f, Color(0xFFFFC107), listOf(
                RoadmapLesson("Места в городе: banco, farmacia, mercado", "vocab", "ciudad"),
                RoadmapLesson("Транспорт: metro, autobús, taxi", "vocab", "viajes"),
                RoadmapLesson("Числа 21–1000 и деньги", "vocab", "general"),
                RoadmapLesson("Как пройти: gira, sigue recto", "phrase", "phrases")
            )),

        RoadmapUnit("7", "¿Qué hora es?", "Время, дни, глаголы на -AR", "⏰",
            true, 0f, Color(0xFFFFB300), listOf(
                RoadmapLesson("Время: ¿Qué hora es? / Son las...", "grammar", "general"),
                RoadmapLesson("Дни недели и месяцы", "vocab", "general"),
                RoadmapLesson("Глаголы -AR в presente: hablar, trabajar", "grammar", "verbs"),
                RoadmapLesson("Мой типичный день", "quiz", "general")
            )),

        RoadmapUnit("8", "Ropa y Colores", "Одежда, цвета, покупки", "👗",
            true, 0f, Color(0xFFFF9800), listOf(
                RoadmapLesson("Одежда: camisa, pantalón, vestido", "vocab", "ropa"),
                RoadmapLesson("Цвета и согласование прилагательных", "vocab", "general"),
                RoadmapLesson("В магазине: ¿Cuánto cuesta?", "phrase", "compras"),
                RoadmapLesson("Степени: más / menos / tan... como", "grammar", "general")
            )),

        RoadmapUnit("9", "Cuerpo y Salud", "Тело, здоровье, у врача", "🏥",
            true, 0f, Color(0xFFFF7043), listOf(
                RoadmapLesson("Части тела: cabeza, brazo, pierna", "vocab", "cuerpo"),
                RoadmapLesson("Глагол DOLER: me duele / me duelen", "grammar", "verbs"),
                RoadmapLesson("Симптомы: tengo fiebre, me duele...", "vocab", "salud"),
                RoadmapLesson("Диалог: У врача", "quiz", "salud")
            )),

        RoadmapUnit("10", "Mi Rutina Diaria", "Возвратные глаголы, наречия частоты", "🌅",
            true, 0f, Color(0xFFFF5722), listOf(
                RoadmapLesson("Возвратные глаголы: levantarse, ducharse", "grammar", "verbs"),
                RoadmapLesson("Наречия частоты: siempre, nunca, a veces", "vocab", "general"),
                RoadmapLesson("Глаголы -ER/-IR: comer, vivir, escribir", "grammar", "verbs"),
                RoadmapLesson("✅ Тест уровня A1", "quiz", "all")
            )),

        // ── A2 ── EN MARCHA ──────────────────────────────────────

        RoadmapUnit("11", "Trabajo y Estudio", "Профессии, обязанности, DEBER/TENER QUE", "💼",
            true, 0f, Color(0xFFE91E63), listOf(
                RoadmapLesson("Профессии: médico, profesor, abogado", "vocab", "profesiones"),
                RoadmapLesson("TENER QUE / DEBER + infinitivo", "grammar", "verbs"),
                RoadmapLesson("Место работы и учёбы", "vocab", "trabajo"),
                RoadmapLesson("Объявление о вакансии", "phrase", "trabajo")
            )),

        RoadmapUnit("12", "Tiempo Libre", "Хобби, спорт, JUGAR и TOCAR", "🎸",
            true, 0f, Color(0xFFC2185B), listOf(
                RoadmapLesson("Хобби и увлечения", "vocab", "ocio"),
                RoadmapLesson("Спорт: JUGAR al fútbol / tenis", "grammar", "verbs"),
                RoadmapLesson("Музыка: TOCAR la guitarra / el piano", "grammar", "verbs"),
                RoadmapLesson("Мои выходные", "quiz", "ocio")
            )),

        RoadmapUnit("13", "Naturaleza y Animales", "Природа, климат, живые существа", "🌿",
            true, 0f, Color(0xFF9C27B0), listOf(
                RoadmapLesson("Природа: bosque, río, montaña, mar", "vocab", "naturaleza"),
                RoadmapLesson("Животные: domésticos y salvajes", "vocab", "animales"),
                RoadmapLesson("Погода и сезоны: Hace calor / llueve", "vocab", "clima"),
                RoadmapLesson("Описание: SER vs. ESTAR + adj.", "grammar", "verbs")
            )),

        RoadmapUnit("14", "¡De Viaje!", "Путешествия, аэропорт, отель", "✈️",
            true, 0f, Color(0xFF7B1FA2), listOf(
                RoadmapLesson("Аэропорт: maleta, vuelo, embarque", "vocab", "viajes"),
                RoadmapLesson("Отель: reserva, habitación, recepción", "vocab", "viajes"),
                RoadmapLesson("Бронирование по телефону", "phrase", "viajes"),
                RoadmapLesson("Диалог: В аэропорту", "quiz", "viajes")
            )),

        RoadmapUnit("15", "¿Qué estás haciendo?", "Presente continuo, герундий", "🔄",
            true, 0f, Color(0xFF512DA8), listOf(
                RoadmapLesson("Герундий: hablando, comiendo, viviendo", "grammar", "verbs"),
                RoadmapLesson("ESTAR + gerundio: está durmiendo", "grammar", "verbs"),
                RoadmapLesson("Сейчас vs. обычно: diferencia de uso", "grammar", "verbs"),
                RoadmapLesson("Что сейчас происходит?", "quiz", "verbs")
            )),

        RoadmapUnit("16", "El Pasado: Perfecto", "Pretérito Perfecto, haber + participio", "✅",
            true, 0f, Color(0xFF303F9F), listOf(
                RoadmapLesson("Participio: hablado, comido, ido", "grammar", "verbs"),
                RoadmapLesson("Haber + participio: he comido", "grammar", "verbs"),
                RoadmapLesson("Participios irregulares: hecho, dicho, visto", "grammar", "verbs"),
                RoadmapLesson("Маркеры: hoy, esta semana, ya, todavía no", "quiz", "verbs")
            )),

        RoadmapUnit("17", "El Pasado: Indefinido", "Pretérito Indefinido, события прошлого", "📅",
            true, 0f, Color(0xFF1976D2), listOf(
                RoadmapLesson("Правильные глаголы: -é, -aste, -ó...", "grammar", "verbs"),
                RoadmapLesson("Неправильные: ser/ir, estar, tener, hacer", "grammar", "verbs"),
                RoadmapLesson("Маркеры: ayer, la semana pasada, en 2020", "vocab", "general"),
                RoadmapLesson("Рассказ о вчерашнем дне", "quiz", "verbs")
            )),

        RoadmapUnit("18", "El Pasado: Imperfecto", "Привычки прошлого, описания, контраст", "🕰️",
            true, 0f, Color(0xFF0288D1), listOf(
                RoadmapLesson("Формы: -aba/-ía, era, tenía, hacía", "grammar", "verbs"),
                RoadmapLesson("Привычки в детстве: de niño/a siempre...", "phrase", "phrases"),
                RoadmapLesson("Описание в прошлом: había, era, estaba", "grammar", "verbs"),
                RoadmapLesson("✅ Indefinido vs. Imperfecto — тест A2", "quiz", "verbs")
            )),

        // ── B1 ── AVANZANDO ──────────────────────────────────────

        RoadmapUnit("19", "El Futuro", "Futuro simple и IR A + infinitivo", "🚀",
            true, 0f, Color(0xFF00796B), listOf(
                RoadmapLesson("IR A + infinitivo: voy a estudiar", "grammar", "verbs"),
                RoadmapLesson("Futuro Simple: hablaré, comeré, viviré", "grammar", "verbs"),
                RoadmapLesson("Неправильные: tendré, haré, podré, diré", "grammar", "verbs"),
                RoadmapLesson("Планы и предсказания", "quiz", "verbs")
            )),

        RoadmapUnit("20", "El Condicional", "Бы: советы, мечты, вежливость", "💭",
            true, 0f, Color(0xFF00897B), listOf(
                RoadmapLesson("Формы: hablaría, comería, viviría", "grammar", "verbs"),
                RoadmapLesson("Советы: Yo (que tú) estudiaría más...", "phrase", "phrases"),
                RoadmapLesson("Вежливые просьбы: ¿Podría ayudarme?", "phrase", "phrases"),
                RoadmapLesson("Мечты и желания", "quiz", "verbs")
            )),

        RoadmapUnit("21", "¡Hazlo!", "Повелительное наклонение", "📢",
            true, 0f, Color(0xFF388E3C), listOf(
                RoadmapLesson("Imperativo afirmativo: habla, come, escribe", "grammar", "verbs"),
                RoadmapLesson("Usted/Ustedes: hable, coman", "grammar", "verbs"),
                RoadmapLesson("Imperativo negativo: no hables, no comas", "grammar", "verbs"),
                RoadmapLesson("Рецепты и инструкции", "quiz", "verbs")
            )),

        RoadmapUnit("22", "Opiniones y Debate", "Мнение, аргументация, характер", "🗣️",
            true, 0f, Color(0xFF689F38), listOf(
                RoadmapLesson("Выражение мнения: creo que, pienso que", "phrase", "phrases"),
                RoadmapLesson("Согласие/несогласие: (no) estoy de acuerdo", "phrase", "phrases"),
                RoadmapLesson("Характер и личность: honesto, creativo", "vocab", "sentimientos"),
                RoadmapLesson("Мини-дебаты: тема экологии", "quiz", "phrases")
            )),

        RoadmapUnit("23", "Tecnología y Redes", "Интернет, гаджеты, современный язык", "📱",
            true, 0f, Color(0xFFF57F17), listOf(
                RoadmapLesson("Гаджеты и интернет: app, wifi, contraseña", "vocab", "tecnologia"),
                RoadmapLesson("Соцсети: seguidor, me gusta, publicar", "vocab", "tecnologia"),
                RoadmapLesson("Современный сленг: guay, molar, tío", "vocab", "expresiones"),
                RoadmapLesson("Переписка в мессенджере", "quiz", "tecnologia")
            )),

        RoadmapUnit("24", "Cultura Hispana", "Традиции, праздники, разные испанские", "💃",
            true, 0f, Color(0xFFE65100), listOf(
                RoadmapLesson("Праздники: Navidad, Semana Santa, Carnaval", "vocab", "cultura"),
                RoadmapLesson("Латинская Америка: страны и особенности", "vocab", "cultura"),
                RoadmapLesson("Еда и культура: tapas, paella, mate", "vocab", "comida_bebida"),
                RoadmapLesson("Фламенко, сиеста и другие символы", "quiz", "cultura")
            )),

        RoadmapUnit("25", "Subjuntivo: Inicio", "Субхунтив настоящего: желания, эмоции", "🌀",
            true, 0f, Color(0xFFBF360C), listOf(
                RoadmapLesson("Формы субхунтива: hable, coma, viva", "grammar", "verbs"),
                RoadmapLesson("Желания: quiero que vengas, espero que...", "grammar", "verbs"),
                RoadmapLesson("Эмоции + субхунтив: me alegra que...", "grammar", "verbs"),
                RoadmapLesson("✅ Тест уровня B1", "quiz", "verbs")
            )),

        // ── B2 ── MAESTRÍA ────────────────────────────────────────

        RoadmapUnit("26", "Subjuntivo: Duda", "Субхунтив: сомнение, отрицание", "❓",
            true, 0f, Color(0xFF6A1B9A), listOf(
                RoadmapLesson("Сомнение: no creo que, dudo que...", "grammar", "verbs"),
                RoadmapLesson("Cuando + субхунтив (будущее)", "grammar", "verbs"),
                RoadmapLesson("Para que, antes de que + субхунтив", "grammar", "verbs"),
                RoadmapLesson("Практика: реальные диалоги B2", "quiz", "verbs")
            )),

        RoadmapUnit("27", "Si... (Condiciones)", "Условные предложения 1-го и 2-го типа", "🔀",
            true, 0f, Color(0xFF4A148C), listOf(
                RoadmapLesson("1-й тип: Si + presente → futuro", "grammar", "verbs"),
                RoadmapLesson("2-й тип: Si + imperfecto subj. → condicional", "grammar", "verbs"),
                RoadmapLesson("Si yo fuera... / Si tuviera...", "phrase", "phrases"),
                RoadmapLesson("Контрфактические ситуации", "quiz", "verbs")
            )),

        RoadmapUnit("28", "Estilo y Registro", "Пассивный залог, косвенная речь", "✍️",
            true, 0f, Color(0xFF1A237E), listOf(
                RoadmapLesson("Пассивный залог: ser + participio", "grammar", "verbs"),
                RoadmapLesson("SE impersonal: se dice, se vende", "grammar", "verbs"),
                RoadmapLesson("Косвенная речь: dijo que / preguntó si", "grammar", "verbs"),
                RoadmapLesson("Формальный vs. разговорный стиль", "quiz", "phrases")
            )),

        RoadmapUnit("29", "El Mundo Actual", "Общество, экология, СМИ, дебаты", "🌍",
            true, 0f, Color(0xFF0D47A1), listOf(
                RoadmapLesson("Экология: cambio climático, reciclaje", "vocab", "ecologia"),
                RoadmapLesson("Политика и общество: democracia, derechos", "vocab", "politica"),
                RoadmapLesson("СМИ: el titular, la noticia, el reportaje", "vocab", "medios"),
                RoadmapLesson("Дебаты: аргументы за и против", "quiz", "phrases")
            )),

        RoadmapUnit("30", "¡Lo Logré!", "Финальный экзамен A1→B2", "🎓",
            true, 0f, Color(0xFF212121), listOf(
                RoadmapLesson("Финальный словарный марафон", "vocab", "all"),
                RoadmapLesson("Финальный грамматический тест", "grammar", "all"),
                RoadmapLesson("Комплексный диалог: всё вместе", "quiz", "all"),
                RoadmapLesson("🏆 Сертификат — ¡Enhorabuena!", "quiz", "all")
            ))
    )
}
