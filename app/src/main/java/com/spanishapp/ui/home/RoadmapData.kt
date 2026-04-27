package com.spanishapp.ui.home

import androidx.compose.ui.graphics.Color

object RoadmapData {
    val units = listOf(
        RoadmapUnit("1", "¡Hola!", "Первые слова и приветствия", "👋", false, 0f, Color(0xFF4CAF50),
            listOf(RoadmapLesson("Алфавит и звуки", "vocab", "general"), RoadmapLesson("Приветствия", "vocab", "general"), RoadmapLesson("Тест: База", "quiz", "general"))),
        
        RoadmapUnit("2", "Números", "Счёт до 100 и время", "🔢", false, 0f, Color(0xFF8BC34A),
            listOf(RoadmapLesson("Числа 1-20", "vocab", "general"), RoadmapLesson("Десятки", "vocab", "general"), RoadmapLesson("Который час?", "grammar", "general"))),

        RoadmapUnit("3", "Mi Familia", "Семья и родственники", "🏠", false, 0f, Color(0xFFFF9800),
            listOf(RoadmapLesson("Члены семьи", "vocab", "familia_personas"), RoadmapLesson("Описание людей", "vocab", "adjectives"), RoadmapLesson("Притяжательные местоимения", "grammar", "general"))),

        RoadmapUnit("4", "En la Ciudad", "Город и транспорт", "🏙️", true, 0f, Color(0xFFFFC107),
            listOf(RoadmapLesson("Места в городе", "vocab", "lugares"), RoadmapLesson("Транспорт", "vocab", "viajes"), RoadmapLesson("Предлоги места", "grammar", "prepositions"))),

        RoadmapUnit("5", "Comida", "Еда, продукты и напитки", "🍎", true, 0f, Color(0xFFFF5722),
            listOf(RoadmapLesson("Фрукты и овощи", "vocab", "comida_bebida"), RoadmapLesson("В супермаркете", "vocab", "comida_bebida"), RoadmapLesson("Глагол Gustar", "grammar", "verbs"))),

        RoadmapUnit("6", "En el Restaurante", "Заказ еды и вежливость", "🥘", true, 0f, Color(0xFFE91E63),
            listOf(RoadmapLesson("Меню", "vocab", "comida_bebida"), RoadmapLesson("Вежливые фразы", "phrase", "phrases"), RoadmapLesson("Диалог: Обед", "quiz", "comida_bebida"))),

        RoadmapUnit("7", "Mi Casa", "Дом, мебель и уют", "🛋️", true, 0f, Color(0xFF9C27B0),
            listOf(RoadmapLesson("Комнаты", "vocab", "casa_hogar"), RoadmapLesson("Мебель", "vocab", "casa_hogar"), RoadmapLesson("Глагол Estar", "grammar", "verbs"))),

        RoadmapUnit("8", "Ropa", "Одежда и покупки", "👕", true, 0f, Color(0xFF673AB7),
            listOf(RoadmapLesson("Предметы одежды", "vocab", "ropa_accesorios"), RoadmapLesson("Цвета", "vocab", "general"), RoadmapLesson("В магазине", "phrase", "compras"))),

        RoadmapUnit("9", "Cuerpo Humano", "Тело и здоровье", "🦴", true, 0f, Color(0xFF3F51B5),
            listOf(RoadmapLesson("Части тела", "vocab", "cuerpo_salud"), RoadmapLesson("У врача", "phrase", "cuerpo_salud"), RoadmapLesson("Глагол Doler", "grammar", "verbs"))),

        RoadmapUnit("10", "Rutina Diaria", "Распорядок дня", "⏰", true, 0f, Color(0xFF2196F3),
            listOf(RoadmapLesson("Утро и вечер", "vocab", "rutina"), RoadmapLesson("Возвратные глаголы", "grammar", "verbs"), RoadmapLesson("Тест: Мой день", "quiz", "rutina"))),

        RoadmapUnit("11", "Trabajo", "Профессии и офис", "💼", true, 0f, Color(0xFF03A9F4),
            listOf(RoadmapLesson("Профессии", "vocab", "profesiones_trabajo"), RoadmapLesson("В офисе", "vocab", "profesiones_trabajo"), RoadmapLesson("Поиск работы", "phrase", "profesiones_trabajo"))),

        RoadmapUnit("12", "Tiempo Libre", "Хобби и увлечения", "🎸", true, 0f, Color(0xFF00BCD4),
            listOf(RoadmapLesson("Музыка и кино", "vocab", "ocio_entretenimiento"), RoadmapLesson("Спорт", "vocab", "deportes"), RoadmapLesson("Глагол Jugar", "grammar", "verbs"))),

        RoadmapUnit("13", "Naturaleza", "Природа и животные", "🌲", true, 0f, Color(0xFF009688),
            listOf(RoadmapLesson("Лес и море", "vocab", "naturaleza"), RoadmapLesson("Дикие животные", "vocab", "animales"), RoadmapLesson("Домашние питомцы", "vocab", "animales"))),

        RoadmapUnit("14", "Viajes", "Путешествия и аэропорт", "✈️", true, 0f, Color(0xFF00796B),
            listOf(RoadmapLesson("В аэропорту", "vocab", "viajes"), RoadmapLesson("Отель", "vocab", "viajes"), RoadmapLesson("Бронирование", "phrase", "viajes"))),

        RoadmapUnit("15", "Clima", "Погода и сезоны", "🌦️", true, 0f, Color(0xFF388E3C),
            listOf(RoadmapLesson("Сезоны года", "vocab", "tiempo_atmosferico"), RoadmapLesson("Прогноз погоды", "vocab", "tiempo_atmosferico"), RoadmapLesson("Безличные глаголы", "grammar", "verbs"))),

        RoadmapUnit("16", "Presente Especial", "Глаголы с отклонением", "🔄", true, 0f, Color(0xFF689F38),
            listOf(RoadmapLesson("Группа e->ie", "grammar", "verbs"), RoadmapLesson("Группа o->ue", "grammar", "verbs"), RoadmapLesson("Практика: Настоящее", "quiz", "verbs"))),

        RoadmapUnit("17", "Educación", "Учеба и университет", "🎓", true, 0f, Color(0xFF8BC34A),
            listOf(RoadmapLesson("Предметы", "vocab", "educacion"), RoadmapLesson("В классе", "vocab", "educacion"), RoadmapLesson("Экзамены", "phrase", "educacion"))),

        RoadmapUnit("18", "Tecnología", "Гаджеты и интернет", "💻", true, 0f, Color(0xFFCDDC39),
            listOf(RoadmapLesson("Компьютер", "vocab", "tecnologia"), RoadmapLesson("Социальные сети", "vocab", "tecnologia"), RoadmapLesson("Герундий", "grammar", "verbs"))),

        RoadmapUnit("19", "Sentimientos", "Чувства и эмоции", "🎭", true, 0f, Color(0xFFFFEB3B),
            listOf(RoadmapLesson("Эмоции", "vocab", "sentimientos"), RoadmapLesson("Характер", "vocab", "adjectives"), RoadmapLesson("Выражение мнения", "phrase", "phrases"))),

        RoadmapUnit("20", "Pasado: Perfecto", "Прошедшее (Perfecto)", "✅", true, 0f, Color(0xFFFFC107),
            listOf(RoadmapLesson("Причастия", "grammar", "verbs"), RoadmapLesson("Глагол Haber", "grammar", "verbs"), RoadmapLesson("Тест: Я сделал", "quiz", "verbs"))),

        RoadmapUnit("21", "Pasado: Indefinido", "Прошедшее (Indefinido)", "📅", true, 0f, Color(0xFFFFB300),
            listOf(RoadmapLesson("Правильные формы", "grammar", "verbs"), RoadmapLesson("Маркеры времени", "vocab", "general"), RoadmapLesson("Истории", "phrase", "phrases"))),

        RoadmapUnit("22", "Pasado: Imperfecto", "Прошедшее (Imperfecto)", "🕰️", true, 0f, Color(0xFFF57C00),
            listOf(RoadmapLesson("Описание в прошлом", "grammar", "verbs"), RoadmapLesson("Привычки", "phrase", "phrases"), RoadmapLesson("Контраст времен", "grammar", "verbs"))),

        RoadmapUnit("23", "Futuro", "Будущее время", "🚀", true, 0f, Color(0xFFE64A19),
            listOf(RoadmapLesson("Простые формы", "grammar", "verbs"), RoadmapLesson("Планы на завтра", "phrase", "viajes"), RoadmapLesson("Предсказания", "quiz", "verbs"))),

        RoadmapUnit("24", "Condicional", "Условное наклонение", "🤝", true, 0f, Color(0xFFD32F2F),
            listOf(RoadmapLesson("Я бы хотел...", "grammar", "verbs"), RoadmapLesson("Советы", "phrase", "phrases"), RoadmapLesson("Вежливость 2.0", "grammar", "verbs"))),

        RoadmapUnit("25", "Imperativo", "Повелительное (Команды)", "📢", true, 0f, Color(0xFFC2185B),
            listOf(RoadmapLesson("Ты-формы", "grammar", "verbs"), RoadmapLesson("Вы-формы", "grammar", "verbs"), RoadmapLesson("Рецепты", "phrase", "comida_bebida"))),

        RoadmapUnit("26", "Subjuntivo 1", "Субхунтив: Желания", "🔮", true, 0f, Color(0xFF7B1FA2),
            listOf(RoadmapLesson("Введение", "grammar", "verbs"), RoadmapLesson("Quiero que...", "phrase", "phrases"), RoadmapLesson("Тест: Эмоции", "quiz", "verbs"))),

        RoadmapUnit("27", "Subjuntivo 2", "Субхунтив: Сомнения", "❓", true, 0f, Color(0xFF512DA8),
            listOf(RoadmapLesson("No creo que...", "grammar", "verbs"), RoadmapLesson("Возможность", "phrase", "phrases"), RoadmapLesson("Практика", "quiz", "verbs"))),

        RoadmapUnit("28", "Cultura", "Культура Испании", "💃", true, 0f, Color(0xFF303F9F),
            listOf(RoadmapLesson("Праздники", "vocab", "general"), RoadmapLesson("Традиции", "phrase", "phrases"), RoadmapLesson("Регионы", "vocab", "general"))),

        RoadmapUnit("29", "Medios de Comunicación", "СМИ и новости", "📰", true, 0f, Color(0xFF1976D2),
            listOf(RoadmapLesson("Газеты и ТВ", "vocab", "general"), RoadmapLesson("Интервью", "phrase", "profesiones_trabajo"), RoadmapLesson("Пассивный залог", "grammar", "verbs"))),

        RoadmapUnit("30", "Examen Final", "Финальный экзамен", "🎓", true, 0f, Color(0xFF212121),
            listOf(RoadmapLesson("Вся лексика", "vocab", "all"), RoadmapLesson("Вся грамматика", "grammar", "all"), RoadmapLesson("ДИПЛОМ", "quiz", "all")))
    )
}
