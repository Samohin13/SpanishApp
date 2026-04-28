package com.spanishapp.ui.home

// ══════════════════════════════════════════════════════════════
//  Статичное содержимое теоретических уроков.
//  Ключ = "u{unitId}_l{lessonIndex}" — совпадает с lesson_progress.
// ══════════════════════════════════════════════════════════════

data class LessonContent(
    val intro: String,                      // вводный абзац
    val sections: List<LessonSection>
)

data class LessonSection(
    val heading: String,
    val items: List<LessonItem>
)

data class LessonItem(
    val left: String,          // буква / правило / слово
    val right: String,         // произношение / перевод
    val note: String = ""      // доп. пометка (необязательно)
)

object LessonContentData {

    val lessons: Map<String, LessonContent> = mapOf(

        // ── Блок 1, Урок 1: Алфавит и произношение ────────────
        "u1_l0" to LessonContent(
            intro = "В испанском алфавите 27 букв. Большинство звучат похоже на русские, но есть несколько важных особенностей.",
            sections = listOf(
                LessonSection(
                    heading = "Алфавит — 27 букв",
                    items = listOf(
                        LessonItem("A  a", "[а]", "casa — дом"),
                        LessonItem("B  b", "[б]", "bebé — ребёнок"),
                        LessonItem("C  c", "[с] / [к]", "ciudad — город, café — кофе"),
                        LessonItem("D  d", "[д]", "día — день"),
                        LessonItem("E  e", "[э]", "español — испанский"),
                        LessonItem("F  f", "[ф]", "foto — фото"),
                        LessonItem("G  g", "[г] / [х]", "gato — кот, gente — люди"),
                        LessonItem("H  h", "молчит!", "hola — привет"),
                        LessonItem("I  i", "[и]", "isla — остров"),
                        LessonItem("J  j", "[х]", "jamón — хамон"),
                        LessonItem("K  k", "[к]", "kilómetro — километр"),
                        LessonItem("L  l", "[л]", "luna — луна"),
                        LessonItem("M  m", "[м]", "madre — мама"),
                        LessonItem("N  n", "[н]", "noche — ночь"),
                        LessonItem("Ñ  ñ", "[нь]", "España — Испания"),
                        LessonItem("O  o", "[о]", "ojo — глаз"),
                        LessonItem("P  p", "[п]", "padre — папа"),
                        LessonItem("Q  q", "[к]", "queso — сыр"),
                        LessonItem("R  r", "[р]", "rosa — роза"),
                        LessonItem("S  s", "[с]", "sol — солнце"),
                        LessonItem("T  t", "[т]", "tren — поезд"),
                        LessonItem("U  u", "[у]", "uva — виноград"),
                        LessonItem("V  v", "[б/в]", "vino — вино"),
                        LessonItem("W  w", "[в]", "whisky"),
                        LessonItem("X  x", "[кс] / [х]", "taxi — такси"),
                        LessonItem("Y  y", "[й/и]", "yo — я"),
                        LessonItem("Z  z", "[с] / [з]", "zapato — туфля")
                    )
                ),
                LessonSection(
                    heading = "Особые сочетания букв",
                    items = listOf(
                        LessonItem("CH", "[ч]", "chocolate — шоколад"),
                        LessonItem("LL", "[й/лье]", "llama — лама"),
                        LessonItem("RR", "раскатистый [р]", "perro — собака"),
                        LessonItem("GU", "[г]", "guerra — война"),
                        LessonItem("QU", "[к]", "quiero — хочу")
                    )
                ),
                LessonSection(
                    heading = "Главные правила",
                    items = listOf(
                        LessonItem("H всегда молчит", "hola = [óla]", ""),
                        LessonItem("C + e/i = [с]", "ciudad = [сиудад]", ""),
                        LessonItem("G + e/i = [х]", "gente = [хэнтэ]", ""),
                        LessonItem("J всегда [х]", "jamón = [хамон]", ""),
                        LessonItem("V = Б в начале слова", "vino = [бино]", ""),
                        LessonItem("Ударение: по умолчанию предпоследний слог", "casa, hablar", "")
                    )
                )
            )
        ),

        // ── Блок 1, Урок 2: Приветствия ───────────────────────
        "u1_l1" to LessonContent(
            intro = "Первые слова при знакомстве. Испанцы очень дружелюбны — приветствие важно!",
            sections = listOf(
                LessonSection(
                    heading = "Приветствия",
                    items = listOf(
                        LessonItem("Hola", "Привет", "универсальное"),
                        LessonItem("Buenos días", "Доброе утро", "до 12:00"),
                        LessonItem("Buenas tardes", "Добрый день", "12:00 – 20:00"),
                        LessonItem("Buenas noches", "Добрый вечер / Спокойной ночи", "после 20:00"),
                        LessonItem("¿Qué tal?", "Как дела?", "разговорное"),
                        LessonItem("¿Cómo estás?", "Как ты? (ты)", ""),
                        LessonItem("¿Cómo está usted?", "Как вы? (вежливо)", "")
                    )
                ),
                LessonSection(
                    heading = "Ответы",
                    items = listOf(
                        LessonItem("Bien, gracias", "Хорошо, спасибо", ""),
                        LessonItem("Muy bien", "Очень хорошо", ""),
                        LessonItem("Regular", "Так себе / Нормально", ""),
                        LessonItem("Mal", "Плохо", "")
                    )
                ),
                LessonSection(
                    heading = "Прощание",
                    items = listOf(
                        LessonItem("Adiós", "Пока / До свидания", ""),
                        LessonItem("Hasta luego", "До скорого", ""),
                        LessonItem("Hasta mañana", "До завтра", ""),
                        LessonItem("Hasta pronto", "До скорой встречи", "")
                    )
                )
            )
        ),

        // ── Блок 2, Урок 3: Личные местоимения ───────────────
        "u2_l2" to LessonContent(
            intro = "Личные местоимения — это я, ты, он, она... В испанском они часто опускаются, потому что глагол уже указывает на лицо.",
            sections = listOf(
                LessonSection(
                    heading = "Личные местоимения",
                    items = listOf(
                        LessonItem("yo", "я", ""),
                        LessonItem("tú", "ты", "неформальное"),
                        LessonItem("usted (Ud.)", "вы (ед.ч.)", "вежливое"),
                        LessonItem("él", "он", ""),
                        LessonItem("ella", "она", ""),
                        LessonItem("nosotros / nosotras", "мы", "м.р. / ж.р."),
                        LessonItem("vosotros / vosotras", "вы (мн.ч.)", "только в Испании"),
                        LessonItem("ustedes (Uds.)", "вы (мн.ч.)", "везде"),
                        LessonItem("ellos / ellas", "они", "м.р. / ж.р.")
                    )
                ),
                LessonSection(
                    heading = "Важные особенности",
                    items = listOf(
                        LessonItem("В испанском местоимения часто опускают", "Hablo español = (Yo) hablo español", ""),
                        LessonItem("Usted — вежливое «ты»", "с начальником, незнакомым человеком", ""),
                        LessonItem("Vosotros — только Испания", "В Латинской Америке говорят ustedes", "")
                    )
                )
            )
        ),

        // ── Блок 4, Урок 4: Предлоги места ───────────────────
        "u4_l3" to LessonContent(
            intro = "Предлоги места помогают объяснить, где находится предмет. В испанском они часто используются с глаголом ESTAR.",
            sections = listOf(
                LessonSection(
                    heading = "Предлоги места",
                    items = listOf(
                        LessonItem("en", "в, на", "El libro está en la mesa."),
                        LessonItem("sobre / encima de", "на (поверхности)", "El gato está sobre el sofá."),
                        LessonItem("debajo de", "под", "El perro está debajo de la silla."),
                        LessonItem("delante de", "перед", "El coche está delante de la casa."),
                        LessonItem("detrás de", "за, позади", "El jardín está detrás de la casa."),
                        LessonItem("al lado de", "рядом с", "El banco está al lado de la farmacia."),
                        LessonItem("entre", "между", "La tienda está entre el banco y el hotel."),
                        LessonItem("cerca de", "близко от", "El metro está cerca de aquí."),
                        LessonItem("lejos de", "далеко от", "El aeropuerto está lejos del centro.")
                    )
                )
            )
        ),

        // ── Блок 5, Урок 2: Артикли ───────────────────────────
        "u5_l1" to LessonContent(
            intro = "Артикль — маленькое слово перед существительным. В испанском все слова мужского или женского рода, и артикль это отражает.",
            sections = listOf(
                LessonSection(
                    heading = "Определённые артикли (the)",
                    items = listOf(
                        LessonItem("el", "м.р., ед.ч.", "el libro — книга"),
                        LessonItem("la", "ж.р., ед.ч.", "la casa — дом"),
                        LessonItem("los", "м.р., мн.ч.", "los libros — книги"),
                        LessonItem("las", "ж.р., мн.ч.", "las casas — дома")
                    )
                ),
                LessonSection(
                    heading = "Неопределённые артикли (a / an)",
                    items = listOf(
                        LessonItem("un", "м.р., ед.ч.", "un libro — какая-то книга"),
                        LessonItem("una", "ж.р., ед.ч.", "una casa — какой-то дом"),
                        LessonItem("unos", "м.р., мн.ч.", "unos libros — несколько книг"),
                        LessonItem("unas", "ж.р., мн.ч.", "unas casas — несколько домов")
                    )
                ),
                LessonSection(
                    heading = "Как определить род слова?",
                    items = listOf(
                        LessonItem("Слова на -o → мужской род", "el libro, el gato, el año", ""),
                        LessonItem("Слова на -a → женский род", "la casa, la mesa, la puerta", ""),
                        LessonItem("Исключения нужно запомнить", "el día, la mano, el mapa", "")
                    )
                )
            )
        ),

        // ── Блок 7, Урок 1: Время ─────────────────────────────
        "u7_l0" to LessonContent(
            intro = "Как спросить и сказать, который час, по-испански.",
            sections = listOf(
                LessonSection(
                    heading = "Как спросить время",
                    items = listOf(
                        LessonItem("¿Qué hora es?", "Который час?", ""),
                        LessonItem("¿A qué hora...?", "В котором часу...?", "¿A qué hora empieza?")
                    )
                ),
                LessonSection(
                    heading = "Как называть время",
                    items = listOf(
                        LessonItem("Es la una.", "Час дня.", "только 1:00"),
                        LessonItem("Son las dos.", "Два часа.", "2:00 – 12:00"),
                        LessonItem("Son las tres y media.", "Половина четвёртого.", "3:30"),
                        LessonItem("Son las cuatro y cuarto.", "Четверть пятого.", "4:15"),
                        LessonItem("Son las cinco menos cuarto.", "Без четверти пять.", "4:45"),
                        LessonItem("Son las doce en punto.", "Ровно двенадцать.", "12:00")
                    )
                ),
                LessonSection(
                    heading = "Утро / День / Вечер",
                    items = listOf(
                        LessonItem("de la mañana", "утра / AM", "las 9 de la mañana"),
                        LessonItem("de la tarde", "дня / вечера PM", "las 3 de la tarde"),
                        LessonItem("de la noche", "ночи", "las 11 de la noche")
                    )
                )
            )
        )
    )
}
