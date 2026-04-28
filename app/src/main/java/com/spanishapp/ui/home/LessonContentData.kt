package com.spanishapp.ui.home

// ══════════════════════════════════════════════════════════════
//  Статичное содержимое теоретических уроков.
//  Ключ = "u{unitId}_l{lessonIndex}" — совпадает с lesson_progress.
// ══════════════════════════════════════════════════════════════

data class LessonContent(
    val intro: String,
    val sections: List<LessonSection>
)

data class LessonSection(
    val heading: String,
    val items: List<LessonItem>
)

data class LessonItem(
    val left: String,
    val right: String,
    val note: String = ""
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
                        LessonItem("Ударение: предпоследний слог", "casa, hablar", "если нет знака ударения")
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

        // ── Блок 2, Урок 1: Глагол SER ────────────────────────
        "u2_l0" to LessonContent(
            intro = "SER = «быть». Используется для постоянных характеристик: профессия, национальность, характер, происхождение.",
            sections = listOf(
                LessonSection(
                    heading = "Спряжение SER",
                    items = listOf(
                        LessonItem("yo soy", "я есть", "Soy estudiante — Я студент"),
                        LessonItem("tú eres", "ты есть", "¿Eres español? — Ты испанец?"),
                        LessonItem("él / ella es", "он / она есть", "Ella es médica — Она врач"),
                        LessonItem("nosotros somos", "мы есть", "Somos rusos — Мы русские"),
                        LessonItem("vosotros sois", "вы есть (Испания)", "¿Sois hermanos? — Вы братья?"),
                        LessonItem("ellos / ellas son", "они есть", "Son muy amables — Они очень приятные")
                    )
                ),
                LessonSection(
                    heading = "Когда использовать SER",
                    items = listOf(
                        LessonItem("Национальность", "Soy ruso / española", ""),
                        LessonItem("Профессия", "Es médico / profesora", ""),
                        LessonItem("Характер", "Es inteligente y creativo", ""),
                        LessonItem("Происхождение", "Es de Madrid — он из Мадрида", ""),
                        LessonItem("Материал", "La mesa es de madera — из дерева", ""),
                        LessonItem("Время / дата", "Son las tres. Hoy es lunes.", "")
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
                        LessonItem("Местоимения часто опускают", "Hablo español = (Yo) hablo español", ""),
                        LessonItem("Usted — вежливое «ты»", "с начальником, незнакомым", ""),
                        LessonItem("Vosotros — только Испания", "В Латинской Америке говорят ustedes", "")
                    )
                )
            )
        ),

        // ── Блок 3, Урок 2: Глагол TENER ─────────────────────
        "u3_l1" to LessonContent(
            intro = "TENER = «иметь». Используется для обладания, возраста и ощущений (голод, холод, страх).",
            sections = listOf(
                LessonSection(
                    heading = "Спряжение TENER",
                    items = listOf(
                        LessonItem("yo tengo", "у меня есть", "Tengo un perro — У меня есть собака"),
                        LessonItem("tú tienes", "у тебя есть", "¿Tienes hermanos? — У тебя есть братья?"),
                        LessonItem("él / ella tiene", "у него / неё", "Tiene 25 años — Ему 25 лет"),
                        LessonItem("nosotros tenemos", "у нас есть", "Tenemos clase hoy — У нас сегодня занятие"),
                        LessonItem("vosotros tenéis", "у вас есть", ""),
                        LessonItem("ellos / ellas tienen", "у них есть", "Tienen mucho dinero — У них много денег")
                    )
                ),
                LessonSection(
                    heading = "Выражения с TENER (ощущения)",
                    items = listOf(
                        LessonItem("tener ... años", "...лет (возраст)", "Tengo 20 años"),
                        LessonItem("tener hambre", "быть голодным", "Tengo mucha hambre"),
                        LessonItem("tener sed", "хотеть пить", ""),
                        LessonItem("tener frío / calor", "замёрзнуть / жарко", ""),
                        LessonItem("tener sueño", "хотеть спать", ""),
                        LessonItem("tener miedo", "бояться", "Tengo miedo — Мне страшно"),
                        LessonItem("tener razón", "быть правым", "Tienes razón — Ты прав")
                    )
                )
            )
        ),

        // ── Блок 3, Урок 3: Притяжательные ───────────────────
        "u3_l2" to LessonContent(
            intro = "Притяжательные местоимения отвечают на вопрос «чей?». Они согласуются с существительным, а не с владельцем.",
            sections = listOf(
                LessonSection(
                    heading = "Краткие формы (перед существительным)",
                    items = listOf(
                        LessonItem("mi / mis", "мой / мои", "mi casa, mis amigos"),
                        LessonItem("tu / tus", "твой / твои", "tu libro, tus fotos"),
                        LessonItem("su / sus", "его / её / ваш / их", "su familia, sus padres"),
                        LessonItem("nuestro / nuestra", "наш / наша", "nuestro coche, nuestra casa"),
                        LessonItem("nuestros / nuestras", "наши", "nuestros hijos"),
                        LessonItem("vuestro / vuestra", "ваш (Испания)", "vuestro perro"),
                        LessonItem("su / sus", "их / ваш (мн.ч.)", "su ciudad, sus amigos")
                    )
                ),
                LessonSection(
                    heading = "Примеры в предложениях",
                    items = listOf(
                        LessonItem("Mi madre es profesora.", "Моя мама — учительница.", ""),
                        LessonItem("¿Cómo se llama tu perro?", "Как зовут твою собаку?", ""),
                        LessonItem("Nuestra casa es grande.", "Наш дом большой.", ""),
                        LessonItem("Sus hijos estudian mucho.", "Их дети много учатся.", "")
                    )
                )
            )
        ),

        // ── Блок 4, Урок 3: Глагол ESTAR ─────────────────────
        "u4_l2" to LessonContent(
            intro = "ESTAR = «быть», но для временных состояний и местонахождения. Не путай с SER!",
            sections = listOf(
                LessonSection(
                    heading = "Спряжение ESTAR",
                    items = listOf(
                        LessonItem("yo estoy", "я нахожусь / есть", "Estoy en casa — Я дома"),
                        LessonItem("tú estás", "ты", "¿Estás bien? — Ты в порядке?"),
                        LessonItem("él / ella está", "он / она", "Está cansado — Он устал"),
                        LessonItem("nosotros estamos", "мы", "Estamos listos — Мы готовы"),
                        LessonItem("vosotros estáis", "вы", "¿Estáis aquí? — Вы здесь?"),
                        LessonItem("ellos / ellas están", "они", "Están en Madrid — Они в Мадриде")
                    )
                ),
                LessonSection(
                    heading = "Когда использовать ESTAR",
                    items = listOf(
                        LessonItem("Местонахождение", "El libro está en la mesa — на столе", ""),
                        LessonItem("Временное состояние", "Hoy estoy cansado — сегодня устал", ""),
                        LessonItem("Эмоции", "Está muy contenta — очень довольна", ""),
                        LessonItem("ESTAR + gerundio", "Estoy comiendo — я ем (сейчас)", "")
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

        // ── Блок 5, Урок 3: Глагол GUSTAR ────────────────────
        "u5_l2" to LessonContent(
            intro = "GUSTAR = «нравиться». Субъект и объект меняются местами: «Мне нравится книга» → «La книга нравится мне».",
            sections = listOf(
                LessonSection(
                    heading = "Структура GUSTAR",
                    items = listOf(
                        LessonItem("me gusta + ед.ч.", "мне нравится", "Me gusta el café."),
                        LessonItem("me gustan + мн.ч.", "мне нравятся", "Me gustan las películas."),
                        LessonItem("te gusta/n", "тебе нравится/ятся", "¿Te gusta el fútbol?"),
                        LessonItem("le gusta/n", "ему / ей / Вам", "Le gustan los libros."),
                        LessonItem("nos gusta/n", "нам нравится", "Nos gusta bailar."),
                        LessonItem("os gusta/n", "вам (Испания)", "¿Os gusta la música?"),
                        LessonItem("les gusta/n", "им / Вам (мн.ч.)", "Les gustan las tapas.")
                    )
                ),
                LessonSection(
                    heading = "Глаголы, работающие как GUSTAR",
                    items = listOf(
                        LessonItem("encantar", "обожать", "Me encanta España."),
                        LessonItem("molestar", "раздражать", "Me molesta el ruido."),
                        LessonItem("doler", "болеть", "Me duele la cabeza."),
                        LessonItem("interesar", "интересовать", "Me interesan los idiomas.")
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
        ),

        // ── Блок 7, Урок 3: Глаголы -AR ──────────────────────
        "u7_l2" to LessonContent(
            intro = "Большинство испанских глаголов оканчивается на -AR. Их спряжение — самое регулярное и важное для начинающих.",
            sections = listOf(
                LessonSection(
                    heading = "Окончания -AR в Presente Indicativo",
                    items = listOf(
                        LessonItem("yo  →  -o", "hablo — я говорю", ""),
                        LessonItem("tú  →  -as", "hablas — ты говоришь", ""),
                        LessonItem("él/ella  →  -a", "habla — он/она говорит", ""),
                        LessonItem("nosotros  →  -amos", "hablamos — мы говорим", ""),
                        LessonItem("vosotros  →  -áis", "habláis — вы говорите", ""),
                        LessonItem("ellos  →  -an", "hablan — они говорят", "")
                    )
                ),
                LessonSection(
                    heading = "Популярные -AR глаголы",
                    items = listOf(
                        LessonItem("hablar", "говорить", "Hablo español."),
                        LessonItem("trabajar", "работать", "Trabajo en una oficina."),
                        LessonItem("estudiar", "учиться", "Estudio mucho."),
                        LessonItem("escuchar", "слушать", "Escucho música."),
                        LessonItem("comprar", "покупать", "Compro en el mercado."),
                        LessonItem("cocinar", "готовить", "¿Cocinas bien?"),
                        LessonItem("viajar", "путешествовать", "Viajo cada verano."),
                        LessonItem("caminar", "ходить пешком", "Camino al trabajo.")
                    )
                )
            )
        ),

        // ── Блок 8, Урок 4: Степени сравнения ────────────────
        "u8_l3" to LessonContent(
            intro = "В испанском три степени сравнения прилагательных: обычная, сравнительная и превосходная.",
            sections = listOf(
                LessonSection(
                    heading = "Сравнительная степень",
                    items = listOf(
                        LessonItem("más + adj. + que", "более...чем", "Madrid es más grande que Valencia."),
                        LessonItem("menos + adj. + que", "менее...чем", "Esto es menos caro que aquello."),
                        LessonItem("tan + adj. + como", "такой же...как", "Soy tan alto como tú.")
                    )
                ),
                LessonSection(
                    heading = "Превосходная степень",
                    items = listOf(
                        LessonItem("el/la más + adj.", "самый", "Es el más inteligente de la clase."),
                        LessonItem("el/la menos + adj.", "наименее", "Es la menos cara de todas.")
                    )
                ),
                LessonSection(
                    heading = "Неправильные формы",
                    items = listOf(
                        LessonItem("bueno → mejor", "хороший → лучший", "Este café es mejor."),
                        LessonItem("malo → peor", "плохой → худший", "Esto es peor."),
                        LessonItem("grande → mayor", "большой → старший/больший", "Mi hermano mayor."),
                        LessonItem("pequeño → menor", "маленький → младший/меньший", "Mi hermana menor.")
                    )
                )
            )
        ),

        // ── Блок 9, Урок 2: Глагол DOLER ─────────────────────
        "u9_l1" to LessonContent(
            intro = "DOLER = «болеть». Работает как GUSTAR — боль является субъектом. «У меня болит голова» = «La cabeza me duele».",
            sections = listOf(
                LessonSection(
                    heading = "Структура DOLER",
                    items = listOf(
                        LessonItem("me duele + ед.ч.", "у меня болит", "Me duele la cabeza — болит голова"),
                        LessonItem("me duelen + мн.ч.", "у меня болят", "Me duelen los pies — болят ноги"),
                        LessonItem("te duele/n", "у тебя болит/ят", "¿Te duele el estómago?"),
                        LessonItem("le duele/n", "у него / неё / Вас", "Le duele la garganta."),
                        LessonItem("nos duele/n", "у нас болит/ят", "Nos duelen los oídos.")
                    )
                ),
                LessonSection(
                    heading = "Части тела с DOLER",
                    items = listOf(
                        LessonItem("la cabeza", "голова", "Me duele la cabeza."),
                        LessonItem("el estómago", "живот", "¿Te duele el estómago?"),
                        LessonItem("la garganta", "горло", "Me duele la garganta."),
                        LessonItem("los ojos", "глаза", "Me duelen los ojos."),
                        LessonItem("la espalda", "спина", "Le duele la espalda."),
                        LessonItem("los pies", "ноги (ступни)", "Me duelen los pies.")
                    )
                )
            )
        ),

        // ── Блок 10, Урок 1: Возвратные глаголы ──────────────
        "u10_l0" to LessonContent(
            intro = "Возвратные глаголы — действие, которое субъект совершает над собой. Используются с местоимениями me, te, se, nos, os, se.",
            sections = listOf(
                LessonSection(
                    heading = "Возвратные местоимения",
                    items = listOf(
                        LessonItem("yo  →  me", "себя", "Me lavo — Я умываюсь"),
                        LessonItem("tú  →  te", "себя", "Te levantas — Ты встаёшь"),
                        LessonItem("él/ella  →  se", "себя", "Se ducha — Он/она принимает душ"),
                        LessonItem("nosotros  →  nos", "себя", "Nos vestimos — Мы одеваемся"),
                        LessonItem("vosotros  →  os", "себя", "Os acostáis — Вы ложитесь спать"),
                        LessonItem("ellos  →  se", "себя", "Se llaman Ana y Luis.")
                    )
                ),
                LessonSection(
                    heading = "Частые возвратные глаголы",
                    items = listOf(
                        LessonItem("levantarse", "вставать", "Me levanto a las 7."),
                        LessonItem("ducharse", "принимать душ", "Me ducho por la mañana."),
                        LessonItem("vestirse", "одеваться", "Se viste rápido."),
                        LessonItem("acostarse", "ложиться спать", "Nos acostamos tarde."),
                        LessonItem("lavarse", "мыться", "Te lavas las manos."),
                        LessonItem("llamarse", "называться / зовут", "Me llamo Iván.")
                    )
                )
            )
        ),

        // ── Блок 10, Урок 3: Глаголы -ER/-IR ─────────────────
        "u10_l2" to LessonContent(
            intro = "-ER и -IR глаголы спрягаются иначе, чем -AR: другие окончания, но схожие между собой.",
            sections = listOf(
                LessonSection(
                    heading = "Окончания -ER (comer)",
                    items = listOf(
                        LessonItem("yo  →  -o", "como — я ем", ""),
                        LessonItem("tú  →  -es", "comes — ты ешь", ""),
                        LessonItem("él/ella  →  -e", "come — он/она ест", ""),
                        LessonItem("nosotros  →  -emos", "comemos — мы едим", ""),
                        LessonItem("vosotros  →  -éis", "coméis — вы едите", ""),
                        LessonItem("ellos  →  -en", "comen — они едят", "")
                    )
                ),
                LessonSection(
                    heading = "Окончания -IR (vivir)",
                    items = listOf(
                        LessonItem("yo  →  -o", "vivo — я живу", ""),
                        LessonItem("tú  →  -es", "vives — ты живёшь", ""),
                        LessonItem("él/ella  →  -e", "vive — он/она живёт", ""),
                        LessonItem("nosotros  →  -imos", "vivimos — мы живём", "отличие от -ER!"),
                        LessonItem("vosotros  →  -ís", "vivís — вы живёте", ""),
                        LessonItem("ellos  →  -en", "viven — они живут", "")
                    )
                ),
                LessonSection(
                    heading = "Популярные -ER/-IR глаголы",
                    items = listOf(
                        LessonItem("comer", "есть / кушать", "Como a las dos."),
                        LessonItem("beber", "пить", "Bebes mucha agua."),
                        LessonItem("leer", "читать", "Lee muchos libros."),
                        LessonItem("vivir", "жить", "Vivo en Moscú."),
                        LessonItem("escribir", "писать", "Escribe un mensaje."),
                        LessonItem("abrir", "открывать", "Abre la ventana.")
                    )
                )
            )
        ),

        // ── Блок 11, Урок 2: TENER QUE / DEBER ───────────────
        "u11_l1" to LessonContent(
            intro = "Оба выражают обязанность, но по-разному. TENER QUE — разговорное «надо», DEBER — книжное «следует/обязан».",
            sections = listOf(
                LessonSection(
                    heading = "TENER QUE + infinitivo (надо, нужно)",
                    items = listOf(
                        LessonItem("Tengo que estudiar.", "Мне нужно учиться.", ""),
                        LessonItem("Tienes que descansar.", "Тебе нужно отдохнуть.", ""),
                        LessonItem("Tiene que hablar con él.", "Ему нужно поговорить с ним.", ""),
                        LessonItem("Tenemos que llegar a las 9.", "Нам нужно прийти в 9.", "")
                    )
                ),
                LessonSection(
                    heading = "DEBER + infinitivo (следует, обязан)",
                    items = listOf(
                        LessonItem("Debes llegar a tiempo.", "Ты должен приходить вовремя.", ""),
                        LessonItem("Debemos respetar las normas.", "Мы должны соблюдать правила.", ""),
                        LessonItem("No debes fumar aquí.", "Здесь нельзя курить.", "")
                    )
                ),
                LessonSection(
                    heading = "Отличие",
                    items = listOf(
                        LessonItem("TENER QUE", "внешняя необходимость (надо)", "Tengo que trabajar — надо работать"),
                        LessonItem("DEBER", "внутренняя обязанность (следует)", "Debes ser honesto — следует быть честным")
                    )
                )
            )
        ),

        // ── Блок 12, Урок 2: JUGAR ───────────────────────────
        "u12_l1" to LessonContent(
            intro = "JUGAR = «играть» (в спорт или игры). Это корнеизменяемый глагол: u→ue во всех формах, кроме nosotros и vosotros.",
            sections = listOf(
                LessonSection(
                    heading = "Спряжение JUGAR (u→ue)",
                    items = listOf(
                        LessonItem("yo juego", "я играю", ""),
                        LessonItem("tú juegas", "ты играешь", ""),
                        LessonItem("él / ella juega", "он/она играет", ""),
                        LessonItem("nosotros jugamos", "мы играем", "НЕТ изменения!"),
                        LessonItem("vosotros jugáis", "вы играете", "НЕТ изменения!"),
                        LessonItem("ellos / ellas juegan", "они играют", "")
                    )
                ),
                LessonSection(
                    heading = "JUGAR + al / a la + вид спорта",
                    items = listOf(
                        LessonItem("jugar al fútbol", "играть в футбол", "¿Juegas al fútbol?"),
                        LessonItem("jugar al tenis", "играть в теннис", ""),
                        LessonItem("jugar al baloncesto", "играть в баскетбол", ""),
                        LessonItem("jugar al ajedrez", "играть в шахматы", ""),
                        LessonItem("jugar a las cartas", "играть в карты", ""),
                        LessonItem("jugar a los videojuegos", "играть в видеоигры", "")
                    )
                )
            )
        ),

        // ── Блок 12, Урок 3: TOCAR ───────────────────────────
        "u12_l2" to LessonContent(
            intro = "TOCAR = «играть на музыкальном инструменте» (и «трогать»). Регулярный -AR глагол.",
            sections = listOf(
                LessonSection(
                    heading = "Спряжение TOCAR",
                    items = listOf(
                        LessonItem("yo toco", "я играю (на инструменте)", ""),
                        LessonItem("tú tocas", "ты играешь", ""),
                        LessonItem("él / ella toca", "он/она играет", ""),
                        LessonItem("nosotros tocamos", "мы играем", ""),
                        LessonItem("vosotros tocáis", "вы играете", ""),
                        LessonItem("ellos / ellas tocan", "они играют", "")
                    )
                ),
                LessonSection(
                    heading = "TOCAR + la/el + инструмент",
                    items = listOf(
                        LessonItem("tocar la guitarra", "играть на гитаре", "Toco la guitarra."),
                        LessonItem("tocar el piano", "играть на пианино", ""),
                        LessonItem("tocar el violín", "играть на скрипке", ""),
                        LessonItem("tocar la batería", "играть на барабанах", ""),
                        LessonItem("tocar la flauta", "играть на флейте", "")
                    )
                ),
                LessonSection(
                    heading = "JUGAR vs. TOCAR",
                    items = listOf(
                        LessonItem("JUGAR al fútbol", "спорт / игры", "jugar = играть в..."),
                        LessonItem("TOCAR la guitarra", "музыкальный инструмент", "tocar = играть на...")
                    )
                )
            )
        ),

        // ── Блок 13, Урок 4: SER vs. ESTAR ───────────────────
        "u13_l3" to LessonContent(
            intro = "SER и ESTAR оба переводятся «быть», но в разных ситуациях. SER — постоянное, ESTAR — временное или местонахождение.",
            sections = listOf(
                LessonSection(
                    heading = "SER (постоянные характеристики)",
                    items = listOf(
                        LessonItem("Профессия", "Soy médico.", ""),
                        LessonItem("Национальность", "Es española.", ""),
                        LessonItem("Характер (всегда)", "Es muy inteligente.", ""),
                        LessonItem("Материал", "La mesa es de madera.", ""),
                        LessonItem("Время / дата", "Son las tres. Hoy es lunes.", ""),
                        LessonItem("Владение", "Ese coche es de Juan.", "")
                    )
                ),
                LessonSection(
                    heading = "ESTAR (временное / местонахождение)",
                    items = listOf(
                        LessonItem("Местонахождение", "El banco está cerca.", ""),
                        LessonItem("Временное состояние", "Hoy estoy cansado.", ""),
                        LessonItem("Эмоция (сейчас)", "Está muy contenta hoy.", ""),
                        LessonItem("ESTAR + gerundio", "Estoy comiendo.", "")
                    )
                ),
                LessonSection(
                    heading = "Прилагательные, меняющие смысл",
                    items = listOf(
                        LessonItem("ser aburrido", "скучный (по характеру)", "Es una persona aburrida."),
                        LessonItem("estar aburrido", "скучать (сейчас)", "Estoy aburrido hoy."),
                        LessonItem("ser malo", "злой / плохой", "Es muy malo."),
                        LessonItem("estar malo", "болеть", "Está malo — он болен.")
                    )
                )
            )
        ),

        // ── Блок 15, Урок 1: Герундий ─────────────────────────
        "u15_l0" to LessonContent(
            intro = "Герундий = форма глагола на -ando/-iendo. Означает «делая» что-то. Используется с ESTAR для настоящего длительного.",
            sections = listOf(
                LessonSection(
                    heading = "Образование герундия",
                    items = listOf(
                        LessonItem("-AR → -ando", "hablar → hablando", "говоря"),
                        LessonItem("-ER → -iendo", "comer → comiendo", "едя"),
                        LessonItem("-IR → -iendo", "vivir → viviendo", "живя")
                    )
                ),
                LessonSection(
                    heading = "Неправильные герундии",
                    items = listOf(
                        LessonItem("leer → leyendo", "читая", ""),
                        LessonItem("oír → oyendo", "слыша", ""),
                        LessonItem("dormir → durmiendo", "спя", "корневое изменение o→u"),
                        LessonItem("pedir → pidiendo", "прося", "e→i"),
                        LessonItem("venir → viniendo", "приходя", ""),
                        LessonItem("ir → yendo", "идя", "")
                    )
                )
            )
        ),

        // ── Блок 15, Урок 2: ESTAR + gerundio ────────────────
        "u15_l1" to LessonContent(
            intro = "ESTAR + gerundio = действие, происходящее прямо сейчас. Аналог английского Present Continuous.",
            sections = listOf(
                LessonSection(
                    heading = "Структура",
                    items = listOf(
                        LessonItem("estoy + gerundio", "я сейчас делаю", "Estoy hablando — Я говорю"),
                        LessonItem("estás + gerundio", "ты сейчас делаешь", "Estás comiendo — Ты ешь"),
                        LessonItem("está + gerundio", "он/она сейчас делает", "Está durmiendo — Он/она спит"),
                        LessonItem("estamos + gerundio", "мы сейчас делаем", "Estamos estudiando español."),
                        LessonItem("estáis / están + gerundio", "вы/они сейчас делают", "")
                    )
                ),
                LessonSection(
                    heading = "Примеры",
                    items = listOf(
                        LessonItem("¿Qué estás haciendo?", "Что ты делаешь (сейчас)?", ""),
                        LessonItem("Estoy viendo una película.", "Я смотрю фильм.", ""),
                        LessonItem("Está durmiendo, no molestes.", "Он/она спит, не мешай.", ""),
                        LessonItem("Estamos esperando el autobús.", "Мы ждём автобус.", "")
                    )
                )
            )
        ),

        // ── Блок 15, Урок 3: Presente vs. ESTAR + gerundio ───
        "u15_l2" to LessonContent(
            intro = "Presente Indicativo — регулярные действия и факты. ESTAR + gerundio — то, что происходит именно сейчас.",
            sections = listOf(
                LessonSection(
                    heading = "Presente Indicativo (регулярно / вообще)",
                    items = listOf(
                        LessonItem("Trabajo en una oficina.", "Я работаю в офисе (вообще).", ""),
                        LessonItem("Como pizza los viernes.", "Я ем пиццу по пятницам.", ""),
                        LessonItem("Vivo en Moscú.", "Я живу в Москве.", "")
                    )
                ),
                LessonSection(
                    heading = "ESTAR + gerundio (прямо сейчас)",
                    items = listOf(
                        LessonItem("Estoy trabajando en casa.", "Я сейчас работаю дома.", ""),
                        LessonItem("Estoy comiendo pizza.", "Я сейчас ем пиццу.", ""),
                        LessonItem("¿Estás escuchando?", "Ты слушаешь? (сейчас)", "")
                    )
                ),
                LessonSection(
                    heading = "Маркеры времени",
                    items = listOf(
                        LessonItem("siempre / normalmente / todos los días", "→ Presente", ""),
                        LessonItem("ahora / ahora mismo / en este momento", "→ ESTAR + gerundio", "")
                    )
                )
            )
        ),

        // ── Блок 16, Урок 1: Participio ───────────────────────
        "u16_l0" to LessonContent(
            intro = "Participio pasado = причастие прошедшего времени. Используется в Pretérito Perfecto и пассивном залоге.",
            sections = listOf(
                LessonSection(
                    heading = "Образование правильных participios",
                    items = listOf(
                        LessonItem("-AR → -ado", "hablar → hablado", "говорить → говоривший"),
                        LessonItem("-ER → -ido", "comer → comido", "есть → съеденный"),
                        LessonItem("-IR → -ido", "vivir → vivido", "жить → живший")
                    )
                ),
                LessonSection(
                    heading = "Неправильные participios — обязательно запомни!",
                    items = listOf(
                        LessonItem("hacer → hecho", "сделать → сделанный", ""),
                        LessonItem("decir → dicho", "сказать → сказанный", ""),
                        LessonItem("ver → visto", "видеть → увиденный", ""),
                        LessonItem("escribir → escrito", "писать → написанный", ""),
                        LessonItem("poner → puesto", "положить → положенный", ""),
                        LessonItem("volver → vuelto", "вернуться → вернувшийся", ""),
                        LessonItem("abrir → abierto", "открыть → открытый", ""),
                        LessonItem("romper → roto", "сломать → сломанный", ""),
                        LessonItem("morir → muerto", "умереть → мёртвый", "")
                    )
                )
            )
        ),

        // ── Блок 16, Урок 2: Pretérito Perfecto ──────────────
        "u16_l1" to LessonContent(
            intro = "Pretérito Perfecto = то, что произошло недавно и связано с настоящим. Строится: HABER + participio.",
            sections = listOf(
                LessonSection(
                    heading = "Спряжение вспомогательного HABER",
                    items = listOf(
                        LessonItem("yo  →  he", "", "He comido — Я поел"),
                        LessonItem("tú  →  has", "", "Has viajado — Ты путешествовал(а)"),
                        LessonItem("él/ella  →  ha", "", "Ha llegado — Он/она пришёл(а)"),
                        LessonItem("nosotros  →  hemos", "", "Hemos estudiado — Мы учили"),
                        LessonItem("vosotros  →  habéis", "", ""),
                        LessonItem("ellos  →  han", "", "Han llamado — Они позвонили")
                    )
                ),
                LessonSection(
                    heading = "Маркеры Pretérito Perfecto",
                    items = listOf(
                        LessonItem("hoy", "сегодня", "Hoy he ido al médico."),
                        LessonItem("esta semana / mañana", "на этой неделе / утром", ""),
                        LessonItem("ya", "уже", "Ya he comido."),
                        LessonItem("todavía no", "ещё нет", "Todavía no he llegado."),
                        LessonItem("alguna vez", "когда-либо", "¿Has estado en España alguna vez?"),
                        LessonItem("nunca", "никогда", "Nunca he comido sushi.")
                    )
                )
            )
        ),

        // ── Блок 16, Урок 3: Irregulares hecho/dicho/visto ───
        "u16_l2" to LessonContent(
            intro = "Неправильные participios в составе Pretérito Perfecto. Participio всегда неизменен — только HABER спрягается!",
            sections = listOf(
                LessonSection(
                    heading = "Неправильные participios в предложениях",
                    items = listOf(
                        LessonItem("He hecho los deberes.", "Я сделал(а) домашнее задание.", ""),
                        LessonItem("¿Has dicho la verdad?", "Ты сказал(а) правду?", ""),
                        LessonItem("Ha visto esa película.", "Он/она посмотрел(а) этот фильм.", ""),
                        LessonItem("Hemos escrito el informe.", "Мы написали отчёт.", ""),
                        LessonItem("Han puesto la mesa.", "Они накрыли на стол.", ""),
                        LessonItem("¿Has vuelto ya?", "Ты уже вернулся?", ""),
                        LessonItem("La ventana está rota.", "Окно сломано.", "roto как прилагательное"),
                        LessonItem("He abierto la carta.", "Я открыл(а) письмо.", "")
                    )
                )
            )
        ),

        // ── Блок 17, Урок 1: Indefinido правильные ───────────
        "u17_l0" to LessonContent(
            intro = "Pretérito Indefinido = прошедшее завершённое. Конкретные события в прошлом: «вчера я пошёл», «в 2020 году они переехали».",
            sections = listOf(
                LessonSection(
                    heading = "Окончания -AR (hablar)",
                    items = listOf(
                        LessonItem("yo  →  -é", "hablé — я говорил", ""),
                        LessonItem("tú  →  -aste", "hablaste — ты говорил", ""),
                        LessonItem("él/ella  →  -ó", "habló — он/она говорил(а)", ""),
                        LessonItem("nosotros  →  -amos", "hablamos — мы говорили", "совпадает с presente!"),
                        LessonItem("vosotros  →  -asteis", "hablasteis — вы говорили", ""),
                        LessonItem("ellos  →  -aron", "hablaron — они говорили", "")
                    )
                ),
                LessonSection(
                    heading = "Окончания -ER/-IR (comer / vivir)",
                    items = listOf(
                        LessonItem("yo  →  -í", "comí / viví", ""),
                        LessonItem("tú  →  -iste", "comiste / viviste", ""),
                        LessonItem("él/ella  →  -ió", "comió / vivió", ""),
                        LessonItem("nosotros  →  -imos", "comimos / vivimos", ""),
                        LessonItem("vosotros  →  -isteis", "comisteis / vivisteis", ""),
                        LessonItem("ellos  →  -ieron", "comieron / vivieron", "")
                    )
                ),
                LessonSection(
                    heading = "Маркеры Indefinido",
                    items = listOf(
                        LessonItem("ayer", "вчера", "Ayer hablé con ella."),
                        LessonItem("la semana pasada", "на прошлой неделе", ""),
                        LessonItem("el año pasado / en 2020", "в прошлом году / в 2020", ""),
                        LessonItem("hace un mes", "месяц назад", "")
                    )
                )
            )
        ),

        // ── Блок 17, Урок 2: Indefinido неправильные ─────────
        "u17_l1" to LessonContent(
            intro = "Самые частые глаголы — неправильные в indefinido. Их нужно запомнить: SER и IR имеют одинаковые формы!",
            sections = listOf(
                LessonSection(
                    heading = "SER и IR (одинаковые формы!)",
                    items = listOf(
                        LessonItem("fui / fuiste / fue", "я/ты/он был(шёл)", ""),
                        LessonItem("fuimos / fuisteis / fueron", "мы/вы/они были(шли)", ""),
                        LessonItem("Ayer fui al médico.", "Вчера я ходил к врачу. (IR)", ""),
                        LessonItem("El viaje fue genial.", "Поездка была отличной. (SER)", "")
                    )
                ),
                LessonSection(
                    heading = "ESTAR, TENER, HACER",
                    items = listOf(
                        LessonItem("estar: estuv-", "estuve, estuviste, estuvo...", ""),
                        LessonItem("tener: tuv-", "tuve, tuviste, tuvo...", ""),
                        LessonItem("hacer: hic-/hiz-", "hice, hiciste, hizo...", "hizo (3-е лицо)")
                    )
                ),
                LessonSection(
                    heading = "PODER, QUERER, VENIR, DECIR",
                    items = listOf(
                        LessonItem("poder: pud-", "pude, pudiste, pudo...", ""),
                        LessonItem("querer: quis-", "quise, quisiste, quiso...", ""),
                        LessonItem("venir: vin-", "vine, viniste, vino...", ""),
                        LessonItem("decir: dij-", "dije, dijiste, dijo...", ""),
                        LessonItem("saber: sup-", "supe, supiste, supo...", ""),
                        LessonItem("dar: di", "di, diste, dio, dimos...", "")
                    )
                )
            )
        ),

        // ── Блок 18, Урок 1: Imperfecto (формы) ──────────────
        "u18_l0" to LessonContent(
            intro = "Pretérito Imperfecto = прошлое незавершённое. Привычки, описания, фон событий. Очень регулярное время!",
            sections = listOf(
                LessonSection(
                    heading = "Окончания -AR",
                    items = listOf(
                        LessonItem("yo  →  -aba", "hablaba — я говорил(а) (обычно)", ""),
                        LessonItem("tú  →  -abas", "hablabas", ""),
                        LessonItem("él/ella  →  -aba", "hablaba", ""),
                        LessonItem("nosotros  →  -ábamos", "hablábamos", ""),
                        LessonItem("vosotros  →  -abais", "hablabais", ""),
                        LessonItem("ellos  →  -aban", "hablaban", "")
                    )
                ),
                LessonSection(
                    heading = "Окончания -ER/-IR",
                    items = listOf(
                        LessonItem("yo  →  -ía", "comía / vivía", ""),
                        LessonItem("tú  →  -ías", "comías / vivías", ""),
                        LessonItem("él/ella  →  -ía", "comía / vivía", ""),
                        LessonItem("nosotros  →  -íamos", "comíamos / vivíamos", ""),
                        LessonItem("vosotros  →  -íais", "comíais / vivíais", ""),
                        LessonItem("ellos  →  -ían", "comían / vivían", "")
                    )
                ),
                LessonSection(
                    heading = "Три неправильных глагола",
                    items = listOf(
                        LessonItem("ser: era, eras, era, éramos, erais, eran", "", ""),
                        LessonItem("ir: iba, ibas, iba, íbamos, ibais, iban", "", ""),
                        LessonItem("ver: veía, veías, veía, veíamos, veíais, veían", "", "")
                    )
                )
            )
        ),

        // ── Блок 18, Урок 2: Привычки в прошлом ──────────────
        "u18_l1" to LessonContent(
            intro = "Imperfecto — идеальное время для рассказа о том, что регулярно делал раньше, в детстве, «бывало».",
            sections = listOf(
                LessonSection(
                    heading = "Слова-маркеры привычки",
                    items = listOf(
                        LessonItem("de niño/a", "в детстве", "De niña, me gustaban los gatos."),
                        LessonItem("de pequeño/a", "когда был(а) маленьким", ""),
                        LessonItem("antes", "раньше", "Antes vivíamos en el campo."),
                        LessonItem("siempre", "всегда (раньше)", "Siempre comía con mis abuelos."),
                        LessonItem("todos los días", "каждый день", "Todos los días iba al parque."),
                        LessonItem("cuando era joven", "когда был(а) молодым", "")
                    )
                ),
                LessonSection(
                    heading = "Примеры предложений",
                    items = listOf(
                        LessonItem("De niña, siempre comía con mis abuelos.", "В детстве я всегда ела с бабушкой и дедушкой.", ""),
                        LessonItem("Antes vivíamos en el campo.", "Раньше мы жили в деревне.", ""),
                        LessonItem("Cuando era pequeño, me gustaban los dinosaurios.", "Когда я был маленьким, мне нравились динозавры.", ""),
                        LessonItem("Todos los veranos íbamos a la playa.", "Каждое лето мы ездили на пляж.", "")
                    )
                )
            )
        ),

        // ── Блок 18, Урок 3: Описание прошлого ───────────────
        "u18_l2" to LessonContent(
            intro = "Imperfecto для описания обстановки, фона, состояний в прошлом. Противопоставляется Indefinido.",
            sections = listOf(
                LessonSection(
                    heading = "Описание с HABER",
                    items = listOf(
                        LessonItem("había", "был / была / были (безличное)", ""),
                        LessonItem("Había mucha gente en la calle.", "На улице было много людей.", ""),
                        LessonItem("No había luz.", "Не было света.", ""),
                        LessonItem("Había un restaurante cerca.", "Рядом был ресторан.", "")
                    )
                ),
                LessonSection(
                    heading = "Описание: SER, ESTAR, HACER",
                    items = listOf(
                        LessonItem("Era una noche tranquila.", "Это была спокойная ночь.", "SER"),
                        LessonItem("El cielo estaba nublado.", "Небо было пасмурным.", "ESTAR"),
                        LessonItem("Hacía mucho frío.", "Было очень холодно.", "HACER"),
                        LessonItem("Llovía y soplaba el viento.", "Шёл дождь и дул ветер.", "")
                    )
                ),
                LessonSection(
                    heading = "Indefinido vs. Imperfecto",
                    items = listOf(
                        LessonItem("Imperfecto = фон / описание", "Llovía y hacía frío.", ""),
                        LessonItem("Indefinido = событие на фоне", "De repente, sonó el teléfono.", "")
                    )
                )
            )
        ),

        // ── Блок 19, Урок 1: IR A + infinitivo ───────────────
        "u19_l0" to LessonContent(
            intro = "IR A + infinitivo = самый простой способ говорить о ближайшем будущем. Аналог «собираюсь сделать».",
            sections = listOf(
                LessonSection(
                    heading = "Структура",
                    items = listOf(
                        LessonItem("voy a + inf.", "я собираюсь", "Voy a estudiar — Я собираюсь учить"),
                        LessonItem("vas a + inf.", "ты собираешься", "¿Vas a comer? — Ты собираешься есть?"),
                        LessonItem("va a + inf.", "он/она собирается", "Va a llover — Будет дождь"),
                        LessonItem("vamos a + inf.", "мы собираемся", "Vamos a salir — Мы собираемся выйти"),
                        LessonItem("vais a / van a + inf.", "вы/они собираются", "")
                    )
                ),
                LessonSection(
                    heading = "Примеры",
                    items = listOf(
                        LessonItem("Esta noche voy a ver una película.", "Сегодня вечером собираюсь посмотреть фильм.", ""),
                        LessonItem("El fin de semana vamos a ir a la playa.", "На выходных поедем на пляж.", ""),
                        LessonItem("¿Qué vas a hacer mañana?", "Что ты собираешься делать завтра?", ""),
                        LessonItem("Va a hacer mucho calor.", "Будет очень жарко.", "")
                    )
                )
            )
        ),

        // ── Блок 19, Урок 2: Futuro Simple ───────────────────
        "u19_l1" to LessonContent(
            intro = "Futuro Simple = будущее время. Строится от инфинитива + окончания (одинаковые для -ar, -er, -ir).",
            sections = listOf(
                LessonSection(
                    heading = "Окончания Futuro (одинаковые для всех)",
                    items = listOf(
                        LessonItem("yo  →  -é", "hablaré — я буду говорить", ""),
                        LessonItem("tú  →  -ás", "hablarás — ты будешь говорить", ""),
                        LessonItem("él/ella  →  -á", "hablará — он/она будет говорить", ""),
                        LessonItem("nosotros  →  -emos", "hablaremos", ""),
                        LessonItem("vosotros  →  -éis", "hablaréis", ""),
                        LessonItem("ellos  →  -án", "hablarán", "")
                    )
                ),
                LessonSection(
                    heading = "Примеры",
                    items = listOf(
                        LessonItem("Mañana hablaré con el director.", "Завтра поговорю с директором.", ""),
                        LessonItem("El próximo año viviremos en España.", "В следующем году будем жить в Испании.", ""),
                        LessonItem("¿Vendrás a la fiesta?", "Ты придёшь на вечеринку?", "нерегулярный"),
                        LessonItem("Creo que lloverá.", "Думаю, будет дождь.", "")
                    )
                )
            )
        ),

        // ── Блок 19, Урок 3: Futuro неправильные ─────────────
        "u19_l2" to LessonContent(
            intro = "~12 глаголов имеют изменённую основу в Futuro, но окончания те же. Их нужно просто запомнить.",
            sections = listOf(
                LessonSection(
                    heading = "Неправильные основы Futuro",
                    items = listOf(
                        LessonItem("tener → tendr-", "tendré, tendrás, tendrá...", ""),
                        LessonItem("hacer → har-", "haré, harás, hará...", ""),
                        LessonItem("poder → podr-", "podré, podrás, podrá...", ""),
                        LessonItem("poner → pondr-", "pondré, pondrás...", ""),
                        LessonItem("decir → dir-", "diré, dirás, dirá...", ""),
                        LessonItem("venir → vendr-", "vendré, vendrás...", ""),
                        LessonItem("salir → saldr-", "saldré, saldrás...", ""),
                        LessonItem("saber → sabr-", "sabré, sabrás...", ""),
                        LessonItem("querer → querr-", "querré, querrás...", ""),
                        LessonItem("haber → habr-", "habrá (безличное)", "")
                    )
                ),
                LessonSection(
                    heading = "Примеры в речи",
                    items = listOf(
                        LessonItem("Tendré tiempo mañana.", "У меня будет время завтра.", ""),
                        LessonItem("¿Qué harás este verano?", "Что ты будешь делать этим летом?", ""),
                        LessonItem("Podrás hacerlo.", "Ты сможешь это сделать.", ""),
                        LessonItem("Vendré a las ocho.", "Приду в восемь.", "")
                    )
                )
            )
        ),

        // ── Блок 20, Урок 1: Condicional Simple ──────────────
        "u20_l0" to LessonContent(
            intro = "Condicional = «бы». Используется для гипотез, мечтаний, вежливых просьб. Окончания как Imperfecto -ía.",
            sections = listOf(
                LessonSection(
                    heading = "Окончания Condicional (от инфинитива)",
                    items = listOf(
                        LessonItem("yo  →  -ía", "hablaría — я бы говорил", ""),
                        LessonItem("tú  →  -ías", "hablarías — ты бы говорил", ""),
                        LessonItem("él/ella  →  -ía", "hablaría — он/она бы говорил(а)", ""),
                        LessonItem("nosotros  →  -íamos", "hablaríamos", ""),
                        LessonItem("vosotros  →  -íais", "hablaríais", ""),
                        LessonItem("ellos  →  -ían", "hablarían", "")
                    )
                ),
                LessonSection(
                    heading = "Неправильные (те же основы, что в Futuro)",
                    items = listOf(
                        LessonItem("tener → tendría", "", ""),
                        LessonItem("hacer → haría", "", ""),
                        LessonItem("poder → podría", "", ""),
                        LessonItem("decir → diría", "", ""),
                        LessonItem("venir → vendría", "", ""),
                        LessonItem("salir → saldría", "", "")
                    )
                )
            )
        ),

        // ── Блок 20, Урок 2: Советы с конционалем ────────────
        "u20_l1" to LessonContent(
            intro = "Конционал — мягкий и вежливый способ давать советы. Мягче, чем императив!",
            sections = listOf(
                LessonSection(
                    heading = "Советы (Yo en tu lugar / Yo que tú)",
                    items = listOf(
                        LessonItem("Yo (que tú) estudiaría más.", "На твоём месте я бы учил больше.", ""),
                        LessonItem("Yo en tu lugar, hablaría con él.", "Я бы поговорил с ним.", ""),
                        LessonItem("Deberías descansar un poco.", "Тебе следовало бы отдохнуть.", ""),
                        LessonItem("Yo no haría eso.", "Я бы этого не делал.", "")
                    )
                ),
                LessonSection(
                    heading = "Предложения и рекомендации",
                    items = listOf(
                        LessonItem("¿Y si...?", "А что если...?", "¿Y si llamaras a tu madre?"),
                        LessonItem("Podrías intentar...", "Ты мог(ла) бы попробовать...", ""),
                        LessonItem("Sería mejor que...", "Было бы лучше, если...", ""),
                        LessonItem("Lo mejor sería...", "Лучшим было бы...", "")
                    )
                )
            )
        ),

        // ── Блок 20, Урок 3: Вежливые просьбы ────────────────
        "u20_l2" to LessonContent(
            intro = "Конционал + PODER/QUERER = самый вежливый способ попросить что-то. Используй в магазинах, ресторанах, с незнакомыми.",
            sections = listOf(
                LessonSection(
                    heading = "Просьбы с ¿Podría / Podrías?",
                    items = listOf(
                        LessonItem("¿Podría hablar más despacio?", "Не могли бы вы говорить помедленнее?", "Ud."),
                        LessonItem("¿Podrías ayudarme?", "Не мог(ла) бы ты помочь мне?", "tú"),
                        LessonItem("¿Me podría traer la cuenta?", "Не могли бы вы принести счёт?", ""),
                        LessonItem("¿Podría repetir, por favor?", "Не могли бы вы повторить?", "")
                    )
                ),
                LessonSection(
                    heading = "Другие вежливые формулы",
                    items = listOf(
                        LessonItem("¿Le importaría...?", "Не возражали бы вы...?", ""),
                        LessonItem("Quisiera...", "Я бы хотел(а)...", "Quisiera una mesa para dos."),
                        LessonItem("Me gustaría reservar...", "Я бы хотел(а) забронировать...", ""),
                        LessonItem("¿Sería posible...?", "Было бы возможно...?", "")
                    )
                )
            )
        ),

        // ── Блок 21, Урок 1: Imperativo afirmativo tú ─────────
        "u21_l0" to LessonContent(
            intro = "Повелительное наклонение — команды и просьбы. Форма для «ты» (tú) = 3-е лицо ед.ч. Presente.",
            sections = listOf(
                LessonSection(
                    heading = "Императив tú (ты) — правильные глаголы",
                    items = listOf(
                        LessonItem("-AR: 3-е лицо", "hablar → habla — говори!", ""),
                        LessonItem("-ER: 3-е лицо", "comer → come — ешь!", ""),
                        LessonItem("-IR: 3-е лицо", "escribir → escribe — пиши!", "")
                    )
                ),
                LessonSection(
                    heading = "Неправильные императивы (tú)",
                    items = listOf(
                        LessonItem("ser → sé", "будь!", "¡Sé honesto!"),
                        LessonItem("ir → ve", "иди!", "¡Ve a casa!"),
                        LessonItem("tener → ten", "имей / держи!", "¡Ten cuidado! — Осторожно!"),
                        LessonItem("venir → ven", "приходи!", "¡Ven aquí!"),
                        LessonItem("hacer → haz", "делай!", "¡Haz los deberes!"),
                        LessonItem("decir → di", "говори / скажи!", "¡Dime la verdad!"),
                        LessonItem("poner → pon", "клади / ставь!", "¡Pon la mesa!"),
                        LessonItem("salir → sal", "выходи!", "¡Sal de aquí!")
                    )
                ),
                LessonSection(
                    heading = "Императив vosotros (вы, Испания)",
                    items = listOf(
                        LessonItem("-AR: inf. -r + d", "hablar → hablad — говорите!", ""),
                        LessonItem("-ER/-IR: inf. -r + d", "comer → comed / vivir → vivid", "")
                    )
                )
            )
        ),

        // ── Блок 21, Урок 2: Imperativo Usted/Ustedes ────────
        "u21_l1" to LessonContent(
            intro = "Для вежливого обращения (Usted/Ustedes) формы берутся из Presente Subjuntivo.",
            sections = listOf(
                LessonSection(
                    heading = "Usted (вежливый ед.ч.)",
                    items = listOf(
                        LessonItem("hablar → hable (Ud.)", "Говорите!", "¡Hable más despacio, por favor!"),
                        LessonItem("comer → coma (Ud.)", "Ешьте!", ""),
                        LessonItem("escribir → escriba (Ud.)", "Пишите!", ""),
                        LessonItem("venir → venga (Ud.)", "Приходите!", "")
                    )
                ),
                LessonSection(
                    heading = "Ustedes (мн.ч. — везде)",
                    items = listOf(
                        LessonItem("hablar → hablen", "Говорите!", "¡Hablen en español!"),
                        LessonItem("comer → coman", "Ешьте!", ""),
                        LessonItem("escribir → escriban", "Пишите!", "")
                    )
                ),
                LessonSection(
                    heading = "Неправильные для Ud./Uds.",
                    items = listOf(
                        LessonItem("ser → sea / sean", "", "¡Sea puntual!"),
                        LessonItem("ir → vaya / vayan", "", "¡Vaya a recepción!"),
                        LessonItem("tener → tenga / tengan", "", ""),
                        LessonItem("hacer → haga / hagan", "", ""),
                        LessonItem("decir → diga / digan", "", ""),
                        LessonItem("poner → ponga / pongan", "", "")
                    )
                )
            )
        ),

        // ── Блок 21, Урок 3: Imperativo negativo ─────────────
        "u21_l2" to LessonContent(
            intro = "Запрещение = no + Presente Subjuntivo для всех лиц. Никаких исключений!",
            sections = listOf(
                LessonSection(
                    heading = "Отрицательный императив (tú)",
                    items = listOf(
                        LessonItem("no hables", "не говори!", ""),
                        LessonItem("no comas", "не ешь!", ""),
                        LessonItem("no vayas", "не иди!", ""),
                        LessonItem("no seas", "не будь!", "¡No seas así!"),
                        LessonItem("no hagas", "не делай!", "¡No hagas eso!")
                    )
                ),
                LessonSection(
                    heading = "Отрицательный для Ud. / Uds.",
                    items = listOf(
                        LessonItem("no hable / no hablen", "не говорите", ""),
                        LessonItem("no coma / no coman", "не ешьте", ""),
                        LessonItem("no use / no usen", "не используйте", "")
                    )
                ),
                LessonSection(
                    heading = "Примеры из жизни",
                    items = listOf(
                        LessonItem("No hablen por teléfono en el cine.", "Не разговаривайте по телефону в кино.", ""),
                        LessonItem("No toques eso.", "Не трогай это.", ""),
                        LessonItem("No comas en clase.", "Не ешь в классе.", ""),
                        LessonItem("No llegues tarde.", "Не опаздывай.", "")
                    )
                )
            )
        ),

        // ── Блок 22, Урок 1: Выражение мнения ────────────────
        "u22_l0" to LessonContent(
            intro = "В испанском много фраз для выражения личного мнения. Используй их, чтобы звучать естественно.",
            sections = listOf(
                LessonSection(
                    heading = "Фразы для выражения мнения",
                    items = listOf(
                        LessonItem("Creo que...", "Я думаю, что...", "Creo que el español es útil."),
                        LessonItem("Pienso que...", "Я считаю, что...", ""),
                        LessonItem("En mi opinión...", "По моему мнению...", ""),
                        LessonItem("Me parece que...", "Мне кажется, что...", ""),
                        LessonItem("Desde mi punto de vista...", "С моей точки зрения...", ""),
                        LessonItem("Estoy convencido/a de que...", "Я убеждён(а), что...", "")
                    )
                ),
                LessonSection(
                    heading = "Примеры в диалоге",
                    items = listOf(
                        LessonItem("Creo que el español es muy útil.", "Я думаю, что испанский очень полезен.", ""),
                        LessonItem("En mi opinión, la música es importante.", "По-моему, музыка важна.", ""),
                        LessonItem("Me parece que tienes razón.", "Мне кажется, ты прав(а).", "")
                    )
                )
            )
        ),

        // ── Блок 22, Урок 2: Согласие и несогласие ───────────
        "u22_l1" to LessonContent(
            intro = "Как соглашаться или не соглашаться по-испански. Эти фразы нужны в любом разговоре.",
            sections = listOf(
                LessonSection(
                    heading = "Согласие",
                    items = listOf(
                        LessonItem("Estoy de acuerdo.", "Я согласен(на).", ""),
                        LessonItem("Tienes razón.", "Ты прав(а).", ""),
                        LessonItem("Exacto / Exactamente.", "Именно так.", ""),
                        LessonItem("Claro que sí.", "Конечно да.", ""),
                        LessonItem("Por supuesto.", "Разумеется.", "")
                    )
                ),
                LessonSection(
                    heading = "Несогласие",
                    items = listOf(
                        LessonItem("No estoy de acuerdo.", "Я не согласен(на).", ""),
                        LessonItem("No creo que sea así.", "Не думаю, что это так.", "+ субхунтив"),
                        LessonItem("Al contrario.", "Наоборот.", ""),
                        LessonItem("Perdona, pero...", "Прости, но...", "вежливое несогласие")
                    )
                ),
                LessonSection(
                    heading = "Частичное согласие",
                    items = listOf(
                        LessonItem("Sí, pero...", "Да, но...", ""),
                        LessonItem("Entiendo tu punto, aunque...", "Понимаю, хотя...", ""),
                        LessonItem("Depende...", "Зависит...", "универсальный ответ")
                    )
                )
            )
        ),

        // ── Блок 25, Урок 1: Subjuntivo — формы ──────────────
        "u25_l0" to LessonContent(
            intro = "Subjuntivo Presente = сослагательное наклонение. Строится от основы 1-го лица Presente (yo), меняются окончания.",
            sections = listOf(
                LessonSection(
                    heading = "Окончания -AR → -e (hablar)",
                    items = listOf(
                        LessonItem("yo  →  hable", "", ""),
                        LessonItem("tú  →  hables", "", ""),
                        LessonItem("él/ella  →  hable", "", ""),
                        LessonItem("nosotros  →  hablemos", "", ""),
                        LessonItem("vosotros  →  habléis", "", ""),
                        LessonItem("ellos  →  hablen", "", "")
                    )
                ),
                LessonSection(
                    heading = "Окончания -ER/-IR → -a (comer / vivir)",
                    items = listOf(
                        LessonItem("yo  →  coma / viva", "", ""),
                        LessonItem("tú  →  comas / vivas", "", ""),
                        LessonItem("él/ella  →  coma / viva", "", ""),
                        LessonItem("nosotros  →  comamos / vivamos", "", ""),
                        LessonItem("vosotros  →  comáis / viváis", "", ""),
                        LessonItem("ellos  →  coman / vivan", "", "")
                    )
                ),
                LessonSection(
                    heading = "Неправильные субхунтивы",
                    items = listOf(
                        LessonItem("ser → sea, seas, sea...", "", ""),
                        LessonItem("ir → vaya, vayas, vaya...", "", ""),
                        LessonItem("tener → tenga, tengas...", "основа от tengo", ""),
                        LessonItem("hacer → haga, hagas...", "основа от hago", ""),
                        LessonItem("decir → diga, digas...", "основа от digo", ""),
                        LessonItem("saber → sepa, sepas...", "неправильная основа", "")
                    )
                )
            )
        ),

        // ── Блок 25, Урок 2: Subjuntivo — желания ────────────
        "u25_l1" to LessonContent(
            intro = "После глаголов желания/рекомендации + «que» → Subjuntivo. Это работает, когда есть ДВА разных субъекта.",
            sections = listOf(
                LessonSection(
                    heading = "Глаголы желания + que + субхунтив",
                    items = listOf(
                        LessonItem("querer que", "хотеть, чтобы", "Quiero que vengas."),
                        LessonItem("desear que", "желать, чтобы", "Deseo que seas feliz."),
                        LessonItem("esperar que", "надеяться, что", "Espero que llegues a tiempo."),
                        LessonItem("pedir que", "просить, чтобы", "Te pido que me ayudes."),
                        LessonItem("recomendar que", "рекомендовать", "Te recomiendo que descanses.")
                    )
                ),
                LessonSection(
                    heading = "Один vs. два субъекта",
                    items = listOf(
                        LessonItem("Один субъект → infinitivo", "Quiero venir. (я хочу прийти)", ""),
                        LessonItem("Два субъекта → que + subj.", "Quiero que vengas. (хочу, чтобы ты пришёл)", "")
                    )
                ),
                LessonSection(
                    heading = "Примеры из жизни",
                    items = listOf(
                        LessonItem("Mi madre quiere que estudie más.", "Мама хочет, чтобы я учил(а) больше.", ""),
                        LessonItem("Espero que todo salga bien.", "Надеюсь, всё пройдёт хорошо.", ""),
                        LessonItem("Te pido que no llegues tarde.", "Прошу тебя не опаздывать.", "")
                    )
                )
            )
        ),

        // ── Блок 25, Урок 3: Subjuntivo — эмоции ────────────
        "u25_l2" to LessonContent(
            intro = "После глаголов и выражений эмоций + «que» → Subjuntivo.",
            sections = listOf(
                LessonSection(
                    heading = "Глаголы эмоций + que + субхунтив",
                    items = listOf(
                        LessonItem("Me alegra que...", "Я рад(а), что...", "Me alegra que estés aquí."),
                        LessonItem("Me sorprende que...", "Меня удивляет, что...", ""),
                        LessonItem("Es una pena que...", "Жаль, что...", "Es una pena que no puedas venir."),
                        LessonItem("Me molesta que...", "Меня раздражает, что...", ""),
                        LessonItem("Es bueno/malo que...", "Хорошо/плохо, что...", ""),
                        LessonItem("Temo que...", "Боюсь, что...", ""),
                        LessonItem("Me encanta que...", "Мне нравится, что...", "")
                    )
                ),
                LessonSection(
                    heading = "Примеры",
                    items = listOf(
                        LessonItem("Me alegra que estés aquí.", "Я рад(а), что ты здесь.", ""),
                        LessonItem("Es una pena que no puedas venir.", "Жаль, что ты не можешь прийти.", ""),
                        LessonItem("Me sorprende que no lo sepas.", "Меня удивляет, что ты этого не знаешь.", ""),
                        LessonItem("Es bueno que estudies español.", "Хорошо, что ты учишь испанский.", "")
                    )
                )
            )
        ),

        // ── Блок 26, Урок 1: Subjuntivo — сомнение ───────────
        "u26_l0" to LessonContent(
            intro = "После выражений сомнения и отрицания мнения → Subjuntivo. Логика: ты не уверен — используй субхунтив.",
            sections = listOf(
                LessonSection(
                    heading = "Выражения сомнения + субхунтив",
                    items = listOf(
                        LessonItem("No creo que...", "Я не думаю, что...", "No creo que tenga razón."),
                        LessonItem("Dudo que...", "Я сомневаюсь, что...", "Dudo que lleguen a tiempo."),
                        LessonItem("No estoy seguro/a de que...", "Я не уверен(а), что...", ""),
                        LessonItem("Es posible que...", "Возможно, что...", "Es posible que llueva mañana."),
                        LessonItem("Quizás / Tal vez + субхунтив", "Возможно...", "Quizás venga mañana.")
                    )
                ),
                LessonSection(
                    heading = "Indicativo vs. Subjuntivo",
                    items = listOf(
                        LessonItem("Creo que tiene razón.", "Я думаю, он прав. (Indicativo)", "уверенность → indicativo"),
                        LessonItem("No creo que tenga razón.", "Не думаю, что он прав. (Subjuntivo)", "сомнение → subjuntivo")
                    )
                )
            )
        ),

        // ── Блок 26, Урок 2: Cuando + subjuntivo ─────────────
        "u26_l1" to LessonContent(
            intro = "В придаточных времени с «cuando» при обращении к будущему → Subjuntivo! Для привычного → Indicativo.",
            sections = listOf(
                LessonSection(
                    heading = "Cuando: привычка vs. будущее",
                    items = listOf(
                        LessonItem("Cuando llueve, me quedo en casa.", "Когда идёт дождь, остаюсь дома. (привычка)", "Indicativo"),
                        LessonItem("Cuando llegues, llámame.", "Когда придёшь, позвони. (будущее)", "Subjuntivo")
                    )
                ),
                LessonSection(
                    heading = "Другие союзы + Subjuntivo (будущее)",
                    items = listOf(
                        LessonItem("cuando", "когда", "Cuando termines, sal."),
                        LessonItem("hasta que", "пока (не)", "Espera hasta que llegue."),
                        LessonItem("antes de que", "до того как", "Llama antes de que salga."),
                        LessonItem("en cuanto", "как только", "En cuanto llegue, te aviso."),
                        LessonItem("tan pronto como", "как только", "Tan pronto como pueda, iré.")
                    )
                )
            )
        ),

        // ── Блок 26, Урок 3: Para que / antes de que ─────────
        "u26_l2" to LessonContent(
            intro = "Союзы цели и условия всегда требуют Subjuntivo (два разных субъекта).",
            sections = listOf(
                LessonSection(
                    heading = "Para que (чтобы, с целью)",
                    items = listOf(
                        LessonItem("Te lo explico para que lo entiendas.", "Объясняю тебе, чтобы ты понял.", ""),
                        LessonItem("Hablo despacio para que me entiendan.", "Говорю медленно, чтобы поняли.", ""),
                        LessonItem("Te llamo para que no te preocupes.", "Звоню, чтобы ты не беспокоился.", "")
                    )
                ),
                LessonSection(
                    heading = "Antes de que (до того как)",
                    items = listOf(
                        LessonItem("Llama antes de que salga.", "Позвони до того, как он уйдёт.", ""),
                        LessonItem("Come algo antes de que te vayas.", "Поешь до того, как уйдёшь.", "")
                    )
                ),
                LessonSection(
                    heading = "Другие союзы, всегда + субхунтив",
                    items = listOf(
                        LessonItem("aunque (хотя / даже если)", "aunque sea difícil — хотя это и трудно", ""),
                        LessonItem("a menos que (если только не)", "a menos que llueva — если только не пойдёт дождь", ""),
                        LessonItem("con tal de que (при условии что)", "con tal de que vengas — при условии, что придёшь", "")
                    )
                )
            )
        ),

        // ── Блок 27, Урок 1: Si + presente → futuro ──────────
        "u27_l0" to LessonContent(
            intro = "Условные предложения 1-го типа — реальные условия с вероятным результатом в будущем.",
            sections = listOf(
                LessonSection(
                    heading = "Структура",
                    items = listOf(
                        LessonItem("Si + presente → futuro", "Если (сейчас) → то (в будущем)", ""),
                        LessonItem("Si + presente → imperativo", "Если → тогда (сделай)", ""),
                        LessonItem("Si + presente → puede(s)", "Если → можешь", "")
                    )
                ),
                LessonSection(
                    heading = "Примеры",
                    items = listOf(
                        LessonItem("Si estudias, aprobarás.", "Если будешь учить, сдашь.", ""),
                        LessonItem("Si tienes tiempo, llámame.", "Если у тебя есть время, позвони.", ""),
                        LessonItem("Si hace buen tiempo, iremos a la playa.", "Если будет хорошая погода, поедем на пляж.", ""),
                        LessonItem("Si quieres, puedes venir.", "Если хочешь, можешь прийти.", ""),
                        LessonItem("Si tienes hambre, come algo.", "Если голоден, поешь что-нибудь.", "")
                    )
                )
            )
        ),

        // ── Блок 27, Урок 2: Si + imperfecto subj. → cond. ───
        "u27_l1" to LessonContent(
            intro = "Условные 2-го типа — нереальные или маловероятные условия в настоящем/будущем.",
            sections = listOf(
                LessonSection(
                    heading = "Структура",
                    items = listOf(
                        LessonItem("Si + imperfecto subj. → condicional", "Если бы (нереально) → то бы...", ""),
                        LessonItem("Si tuviera dinero, viajaría.", "Если бы у меня были деньги, путешествовал бы.", "")
                    )
                ),
                LessonSection(
                    heading = "Imperfecto Subjuntivo (основные формы)",
                    items = listOf(
                        LessonItem("tener: tuviera", "Если бы у меня было...", ""),
                        LessonItem("ser: fuera", "Если бы я был(а)...", ""),
                        LessonItem("poder: pudiera", "Если бы мог(ла)...", ""),
                        LessonItem("hacer: hiciera", "Если бы делал(а)...", ""),
                        LessonItem("ir: fuera", "Если бы шёл(шла)...", ""),
                        LessonItem("saber: supiera", "Если бы знал(а)...", "")
                    )
                ),
                LessonSection(
                    heading = "Примеры",
                    items = listOf(
                        LessonItem("Si fuera rico, compraría una isla.", "Если бы был богатым, купил бы остров.", ""),
                        LessonItem("Si pudiera, estudiaría medicina.", "Если бы мог, учил бы медицину.", ""),
                        LessonItem("Si viviera en España, hablaría mejor.", "Если бы жил в Испании, говорил бы лучше.", "")
                    )
                )
            )
        ),

        // ── Блок 27, Урок 3: Si yo fuera... ──────────────────
        "u27_l2" to LessonContent(
            intro = "Популярные конструкции с Si yo fuera / Si tuviera для выражения мечтаний и гипотез. Типичный разговор!",
            sections = listOf(
                LessonSection(
                    heading = "Фразы-мечты (начало)",
                    items = listOf(
                        LessonItem("Si yo fuera presidente...", "Если бы я был президентом...", ""),
                        LessonItem("Si tuviera más tiempo...", "Если бы у меня было больше времени...", ""),
                        LessonItem("Si pudiera elegir...", "Если бы мог(ла) выбирать...", ""),
                        LessonItem("Si viviera en otro país...", "Если бы я жил(а) в другой стране...", ""),
                        LessonItem("Si supiera cocinar bien...", "Если бы умел(а) хорошо готовить...", "")
                    )
                ),
                LessonSection(
                    heading = "Полные предложения",
                    items = listOf(
                        LessonItem("Si yo fuera presidente, cambiaría muchas cosas.", "Если бы я был президентом, изменил бы многое.", ""),
                        LessonItem("Si tuviera más dinero, viajaría por el mundo.", "Если бы было больше денег, путешествовал бы по миру.", ""),
                        LessonItem("Si pudiera elegir, viviría en Barcelona.", "Если бы мог выбирать, жил бы в Барселоне.", ""),
                        LessonItem("Si viviera en otro país, aprendería otro idioma.", "Если бы жил в другой стране, учил бы ещё язык.", "")
                    )
                )
            )
        ),

        // ── Блок 28, Урок 1: Пассивный залог ─────────────────
        "u28_l0" to LessonContent(
            intro = "Пассивный залог = действие совершается над подлежащим. Строится: SER + participio (согл. с подлежащим).",
            sections = listOf(
                LessonSection(
                    heading = "Структура",
                    items = listOf(
                        LessonItem("SER (в нужном времени) + participio", "", ""),
                        LessonItem("El libro es escrito por el autor.", "Книга написана автором. (настоящее)", ""),
                        LessonItem("La carta fue enviada ayer.", "Письмо было отправлено вчера. (прошедшее)", ""),
                        LessonItem("El proyecto será terminado mañana.", "Проект будет завершён завтра. (будущее)", "")
                    )
                ),
                LessonSection(
                    heading = "Согласование participio с подлежащим",
                    items = listOf(
                        LessonItem("El problema fue resuelto.", "Задача решена. (м.р., ед.ч.)", ""),
                        LessonItem("La carta fue enviada.", "Письмо отправлено. (ж.р., ед.ч.)", ""),
                        LessonItem("Los libros fueron publicados.", "Книги опубликованы. (м.р., мн.ч.)", ""),
                        LessonItem("Las casas fueron construidas.", "Дома были построены. (ж.р., мн.ч.)", "")
                    )
                ),
                LessonSection(
                    heading = "Агент действия: por",
                    items = listOf(
                        LessonItem("por + кем совершено", "", ""),
                        LessonItem("La película fue dirigida por Almodóvar.", "Фильм был снят Альмодоваром.", ""),
                        LessonItem("El libro fue escrito por Cervantes.", "Книга написана Сервантесом.", "")
                    )
                )
            )
        ),

        // ── Блок 28, Урок 2: SE impersonal/pasiva ────────────
        "u28_l1" to LessonContent(
            intro = "SE + 3-е лицо — безличная конструкция. Никто конкретный не называется: «говорят», «продаётся», «здесь едят».",
            sections = listOf(
                LessonSection(
                    heading = "SE pasiva refleja (есть объект)",
                    items = listOf(
                        LessonItem("Se vende piso.", "Продаётся квартира. (ед.ч.)", ""),
                        LessonItem("Se alquilan habitaciones.", "Сдаются комнаты. (мн.ч.)", ""),
                        LessonItem("Se habla español.", "Здесь говорят по-испански.", ""),
                        LessonItem("Se busca cocinero.", "Требуется повар.", "объявление о работе"),
                        LessonItem("Se necesitan camareros.", "Требуются официанты.", "")
                    )
                ),
                LessonSection(
                    heading = "SE impersonal (общие действия)",
                    items = listOf(
                        LessonItem("Se come bien en España.", "В Испании хорошо едят.", ""),
                        LessonItem("Se trabaja mucho aquí.", "Здесь много работают.", ""),
                        LessonItem("Se dice que...", "Говорят, что...", "Se dice que va a llover."),
                        LessonItem("Se puede + infinitivo", "Можно...", "Aquí se puede aparcar.")
                    )
                )
            )
        ),

        // ── Блок 28, Урок 3: Косвенная речь ──────────────────
        "u28_l2" to LessonContent(
            intro = "Косвенная речь — передача чужих слов без кавычек. Времена смещаются! Presente → Imperfecto, Futuro → Condicional.",
            sections = listOf(
                LessonSection(
                    heading = "Передача утверждений (decir que)",
                    items = listOf(
                        LessonItem("«Estoy cansado.»", "→ Dijo que estaba cansado.", "Presente → Imperfecto"),
                        LessonItem("«Tengo hambre.»", "→ Dijo que tenía hambre.", ""),
                        LessonItem("«Vendré mañana.»", "→ Dijo que vendría al día siguiente.", "Futuro → Condicional"),
                        LessonItem("«He comido.»", "→ Dijo que había comido.", "Perfecto → Pluscuamperfecto")
                    )
                ),
                LessonSection(
                    heading = "Передача вопросов",
                    items = listOf(
                        LessonItem("«¿Vienes?»", "→ Preguntó si venía.", "si = если/ли"),
                        LessonItem("«¿Dónde vives?»", "→ Preguntó dónde vivía.", "")
                    )
                ),
                LessonSection(
                    heading = "Изменения слов-указателей",
                    items = listOf(
                        LessonItem("mañana → al día siguiente", "завтра → на следующий день", ""),
                        LessonItem("hoy → ese día", "сегодня → в тот день", ""),
                        LessonItem("aquí → allí", "здесь → там", ""),
                        LessonItem("esta semana → esa semana", "эта неделя → та неделя", "")
                    )
                )
            )
        )
    )
}
