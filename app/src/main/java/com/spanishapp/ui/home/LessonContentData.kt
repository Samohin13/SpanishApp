package com.spanishapp.ui.home

// ══════════════════════════════════════════════════════════════
//  Статичное содержимое теоретических уроков.
//  Ключ = "u{unitId}_l{lessonIndex}" — совпадает с lesson_progress.
//  Блок 1 (u1): уроки l0–l14  |  Блоки 2-4: добавляются по мере готовности.
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

        // ══════════════════════════════════════════════
        //  БЛОК 1: ВЗЛЁТ
        // ══════════════════════════════════════════════

        // u1_l0 — Гласные: A, E, I, O, U
        "u1_l0" to LessonContent(
            intro = "В испанском языке 5 гласных — они всегда читаются одинаково, без исключений. Это огромное преимущество перед английским!",
            sections = listOf(
                LessonSection(
                    heading = "5 гласных — 5 правил",
                    items = listOf(
                        LessonItem("A  a", "[а]", "casa — дом, mamá — мама"),
                        LessonItem("E  e", "[э]", "mes — месяц, leche — молоко"),
                        LessonItem("I  i", "[и]", "isla — остров, libro — книга"),
                        LessonItem("O  o", "[о]", "ojo — глаз, sol — солнце"),
                        LessonItem("U  u", "[у]", "uva — виноград, luna — луна")
                    )
                ),
                LessonSection(
                    heading = "Главные правила",
                    items = listOf(
                        LessonItem("Каждая буква = один звук", "всегда, без исключений", ""),
                        LessonItem("Гласные не «глотаются»", "ka-sa, не «ks»", ""),
                        LessonItem("Слоги открытые", "ма-ма, пе-рро", "")
                    )
                ),
                LessonSection(
                    heading = "Практика: прочитай вслух",
                    items = listOf(
                        LessonItem("a-mi-go", "друг", "а-ми-го"),
                        LessonItem("es-pa-ñol", "испанский", "эс-па-ньол"),
                        LessonItem("mu-si-ca", "музыка", "му-си-ка"),
                        LessonItem("o-ce-a-no", "океан", "о-сэ-а-но")
                    )
                )
            )
        ),

        // u1_l1 — Согласные: B/V, D, G — испанские секреты
        "u1_l1" to LessonContent(
            intro = "Большинство согласных читаются как в русском, но три буквы ведут себя особенно — запомни их сразу!",
            sections = listOf(
                LessonSection(
                    heading = "Особые согласные",
                    items = listOf(
                        LessonItem("B и V", "звучат одинаково как [б/в]", "vino — вино читается «бино»"),
                        LessonItem("D между гласными", "мягкий [д], почти [ð]", "cada — «када» (мягко)"),
                        LessonItem("G + e/i", "звучит как [х]", "gente — «хэнтэ» (люди)"),
                        LessonItem("G + a/o/u", "звучит как [г]", "gato — «гато» (кот)")
                    )
                ),
                LessonSection(
                    heading = "Простые согласные",
                    items = listOf(
                        LessonItem("P  p", "[п]", "padre — папа"),
                        LessonItem("T  t", "[т]", "tren — поезд"),
                        LessonItem("M  m", "[м]", "madre — мама"),
                        LessonItem("N  n", "[н]", "noche — ночь"),
                        LessonItem("L  l", "[л]", "luna — луна"),
                        LessonItem("S  s", "[с]", "sol — солнце"),
                        LessonItem("F  f", "[ф]", "foto — фото"),
                        LessonItem("C + a/o/u", "[к]", "casa — дом"),
                        LessonItem("C + e/i", "[с]", "ciudad — город")
                    )
                ),
                LessonSection(
                    heading = "Читаем слова",
                    items = listOf(
                        LessonItem("bueno", "хорошо", "бу-э-но"),
                        LessonItem("vida", "жизнь", "би-да"),
                        LessonItem("general", "генерал", "хэ-нэ-раль"),
                        LessonItem("coche", "машина", "ко-чэ")
                    )
                )
            )
        ),

        // u1_l2 — H молчит · J=[х] · Ñ=[нь] · RR=[рр]
        "u1_l2" to LessonContent(
            intro = "Четыре буквы — четыре секрета испанского произношения. Выучи их сейчас, и тебя поймут носители языка!",
            sections = listOf(
                LessonSection(
                    heading = "H — молчащая буква",
                    items = listOf(
                        LessonItem("H всегда молчит", "hola = [óла]", ""),
                        LessonItem("hablar", "[аблар]", "говорить"),
                        LessonItem("hotel", "[отэль]", "отель"),
                        LessonItem("hijo", "[ихо]", "сын")
                    )
                ),
                LessonSection(
                    heading = "J — всегда [х]",
                    items = listOf(
                        LessonItem("jamón", "[хамон]", "хамон (ветчина)"),
                        LessonItem("julio", "[хулио]", "июль"),
                        LessonItem("trabajo", "[трабахо]", "работа")
                    )
                ),
                LessonSection(
                    heading = "Ñ — как русское «нь»",
                    items = listOf(
                        LessonItem("España", "[эспанья]", "Испания"),
                        LessonItem("señor", "[сэньор]", "господин"),
                        LessonItem("mañana", "[маньяна]", "завтра / утро")
                    )
                ),
                LessonSection(
                    heading = "RR — раскатистый [рр]",
                    items = listOf(
                        LessonItem("perro", "[пэрро]", "собака"),
                        LessonItem("arroz", "[аррос]", "рис"),
                        LessonItem("R в начале слова", "тоже раскатистый", "Rosa — [Рроса]")
                    )
                )
            )
        ),

        // u1_l3 — Ударение и тильда
        "u1_l3" to LessonContent(
            intro = "В испанском есть два простых правила ударения. Если слово им не следует — ставится тильда. Больше никаких секретов!",
            sections = listOf(
                LessonSection(
                    heading = "Правило 1: слово без тильды",
                    items = listOf(
                        LessonItem("Оканчивается на гласную", "ударение на предпоследний слог", "casa → CA-sa"),
                        LessonItem("Оканчивается на N или S", "ударение на предпоследний слог", "joven → JO-ven"),
                        LessonItem("Оканчивается на согласную", "ударение на последний слог", "hablar → ha-BLAR")
                    )
                ),
                LessonSection(
                    heading = "Правило 2: тильда = исключение",
                    items = listOf(
                        LessonItem("café", "[ка-ФЭ]", "нарушает правило → тильда"),
                        LessonItem("mamá", "[ма-МА]", "ударение на последний слог"),
                        LessonItem("médico", "[МЭ-ди-ко]", "ударение на третий слог"),
                        LessonItem("fácil", "[ФА-силь]", "лёгкий")
                    )
                ),
                LessonSection(
                    heading = "Тильда в вопросах",
                    items = listOf(
                        LessonItem("¿qué?", "что?", ""),
                        LessonItem("¿cómo?", "как?", ""),
                        LessonItem("¿dónde?", "где?", ""),
                        LessonItem("¿quién?", "кто?", ""),
                        LessonItem("¿cuándo?", "когда?", "")
                    )
                )
            )
        ),

        // u1_l7 — SER: soy, eres, es
        "u1_l7" to LessonContent(
            intro = "SER — один из важнейших глаголов испанского. Он означает «быть» и используется для описания постоянных качеств: имя, национальность, профессия.",
            sections = listOf(
                LessonSection(
                    heading = "Спряжение SER (единственное число)",
                    items = listOf(
                        LessonItem("yo", "soy", "я есть / я — ..."),
                        LessonItem("tú", "eres", "ты есть / ты — ..."),
                        LessonItem("él / ella", "es", "он/она есть / он — ...")
                    )
                ),
                LessonSection(
                    heading = "Примеры с SER",
                    items = listOf(
                        LessonItem("Yo soy ruso.", "Я русский.", ""),
                        LessonItem("¿Tú eres estudiante?", "Ты студент?", ""),
                        LessonItem("Él es médico.", "Он врач.", ""),
                        LessonItem("Ella es de España.", "Она из Испании.", ""),
                        LessonItem("¿De dónde eres?", "Откуда ты?", "")
                    )
                ),
                LessonSection(
                    heading = "Когда используем SER",
                    items = listOf(
                        LessonItem("Имя", "Me llamo / Soy Ana", ""),
                        LessonItem("Национальность", "Soy ruso/rusa", ""),
                        LessonItem("Профессия", "Soy ingeniero", ""),
                        LessonItem("Происхождение", "Soy de Moscú", ""),
                        LessonItem("Характеристика", "Soy alto/a, simpático/a", "")
                    )
                )
            )
        ),

        // u1_l8 — SER: somos, sois, son
        "u1_l8" to LessonContent(
            intro = "Продолжаем SER! Теперь множественное число — «мы», «вы», «они». Это даст тебе полную таблицу первого глагола.",
            sections = listOf(
                LessonSection(
                    heading = "Полная таблица SER",
                    items = listOf(
                        LessonItem("yo", "soy", "я"),
                        LessonItem("tú", "eres", "ты"),
                        LessonItem("él / ella / usted", "es", "он / она / Вы"),
                        LessonItem("nosotros/as", "somos", "мы"),
                        LessonItem("vosotros/as", "sois", "вы (Испания)"),
                        LessonItem("ellos / ellas / ustedes", "son", "они / Вы (все)")
                    )
                ),
                LessonSection(
                    heading = "Примеры: множественное число",
                    items = listOf(
                        LessonItem("Nosotros somos amigos.", "Мы друзья.", ""),
                        LessonItem("¿Vosotros sois hermanos?", "Вы братья?", "в Испании"),
                        LessonItem("Ellos son estudiantes.", "Они студенты.", ""),
                        LessonItem("¿Ustedes son de Rusia?", "Вы из России?", "вежливая форма"),
                        LessonItem("Somos de Madrid.", "Мы из Мадрида.", "")
                    )
                ),
                LessonSection(
                    heading = "Usted vs tú",
                    items = listOf(
                        LessonItem("tú", "неформальное «ты»", "с друзьями, ровесниками"),
                        LessonItem("usted (Ud.)", "формальное «вы»", "с незнакомыми, старшими"),
                        LessonItem("vosotros", "«вы» мн.ч. в Испании", ""),
                        LessonItem("ustedes", "«вы» мн.ч. везде", "универсально")
                    )
                )
            )
        ),

        // u1_l9 — Личные местоимения
        "u1_l9" to LessonContent(
            intro = "В испанском личные местоимения часто опускают — глагол уже говорит кто действует. Но знать их нужно!",
            sections = listOf(
                LessonSection(
                    heading = "Личные местоимения",
                    items = listOf(
                        LessonItem("yo", "я", ""),
                        LessonItem("tú", "ты", "неформально"),
                        LessonItem("él", "он", ""),
                        LessonItem("ella", "она", ""),
                        LessonItem("usted (Ud.)", "Вы", "формально, сокр. Ud."),
                        LessonItem("nosotros", "мы", "муж. род или смешанная группа"),
                        LessonItem("nosotras", "мы", "только женщины"),
                        LessonItem("vosotros", "вы", "Испания, неформально"),
                        LessonItem("ellos", "они", "мужчины или смешанная группа"),
                        LessonItem("ellas", "они", "только женщины"),
                        LessonItem("ustedes (Uds.)", "вы / Вы", "все страны, любая обстановка")
                    )
                ),
                LessonSection(
                    heading = "Когда местоимение нужно?",
                    items = listOf(
                        LessonItem("Для контраста", "Yo hablo, tú escuchas.", "я говорю, ты слушаешь"),
                        LessonItem("Для ударения", "¡Yo soy Ana!", "именно я"),
                        LessonItem("В остальных случаях", "обычно опускают", "hablo = я говорю (и так ясно)")
                    )
                )
            )
        ),

        // u1_l10 — Род: el/la — мужской и женский
        "u1_l10" to LessonContent(
            intro = "В испанском у каждого существительного есть род — мужской или женский. Артикль el/la помогает его определить.",
            sections = listOf(
                LessonSection(
                    heading = "Мужской род (el)",
                    items = listOf(
                        LessonItem("Обычно оканчивается на -o", "el libro — книга", ""),
                        LessonItem("el padre", "отец", ""),
                        LessonItem("el hombre", "мужчина", ""),
                        LessonItem("el día", "день", "исключение: -a, но мужской!"),
                        LessonItem("el problema", "проблема", "исключение: -a, но мужской!")
                    )
                ),
                LessonSection(
                    heading = "Женский род (la)",
                    items = listOf(
                        LessonItem("Обычно оканчивается на -a", "la casa — дом", ""),
                        LessonItem("la madre", "мать", ""),
                        LessonItem("la mujer", "женщина", ""),
                        LessonItem("la flor", "цветок", "исключение: согласная, но женский"),
                        LessonItem("la mano", "рука", "исключение: -o, но женский!")
                    )
                ),
                LessonSection(
                    heading = "Подсказки",
                    items = listOf(
                        LessonItem("-ción, -sión, -dad", "женский род", "la ciudad, la canción"),
                        LessonItem("-ema, -ama", "мужской род", "el problema, el programa"),
                        LessonItem("Запоминай с артиклем", "el/la + слово", "сразу будешь знать род")
                    )
                )
            )
        ),

        // u1_l11 — Артикли: el/la/un/una/los/las
        "u1_l11" to LessonContent(
            intro = "Испанские артикли — как в русском «этот/этого» vs «один/какой-то». Определённый (el/la) = конкретный предмет. Неопределённый (un/una) = один из многих.",
            sections = listOf(
                LessonSection(
                    heading = "Определённые артикли (конкретный предмет)",
                    items = listOf(
                        LessonItem("el", "мужской ед.ч.", "el libro — эта книга"),
                        LessonItem("la", "женский ед.ч.", "la casa — этот дом"),
                        LessonItem("los", "мужской мн.ч.", "los libros — эти книги"),
                        LessonItem("las", "женский мн.ч.", "las casas — эти дома")
                    )
                ),
                LessonSection(
                    heading = "Неопределённые артикли (один из многих)",
                    items = listOf(
                        LessonItem("un", "мужской ед.ч.", "un libro — какая-то книга"),
                        LessonItem("una", "женский ед.ч.", "una casa — какой-то дом"),
                        LessonItem("unos", "мужской мн.ч.", "unos libros — несколько книг"),
                        LessonItem("unas", "женский мн.ч.", "unas casas — несколько домов")
                    )
                ),
                LessonSection(
                    heading = "Примеры в речи",
                    items = listOf(
                        LessonItem("Tengo un perro.", "У меня есть собака.", "впервые упоминаем"),
                        LessonItem("El perro es grande.", "Собака большая.", "уже известна"),
                        LessonItem("Quiero una manzana.", "Хочу яблоко.", "любое"),
                        LessonItem("Dame la manzana.", "Дай мне это яблоко.", "конкретное"),
                        LessonItem("Soy médico.", "Я врач.", "профессия — без артикля!")
                    )
                )
            )
        ),

        // ══════════════════════════════════════════════
        //  БЛОК 2: МОЙ МИР
        // ══════════════════════════════════════════════

        // u2_l2 — TENER: tengo, tienes, tiene
        "u2_l2" to LessonContent(
            intro = "TENER означает «иметь» — второй по важности глагол после SER. Он нужен чтобы говорить о том, что у тебя есть.",
            sections = listOf(
                LessonSection(
                    heading = "Спряжение TENER (ед. число)",
                    items = listOf(
                        LessonItem("yo", "tengo", "у меня есть"),
                        LessonItem("tú", "tienes", "у тебя есть"),
                        LessonItem("él / ella", "tiene", "у него/неё есть")
                    )
                ),
                LessonSection(
                    heading = "Примеры",
                    items = listOf(
                        LessonItem("Tengo un perro.", "У меня есть собака.", ""),
                        LessonItem("¿Tienes hermanos?", "У тебя есть братья?", ""),
                        LessonItem("Ella tiene 25 años.", "Ей 25 лет.", "возраст — через TENER!"),
                        LessonItem("¿Cuántos años tienes?", "Сколько тебе лет?", "")
                    )
                ),
                LessonSection(
                    heading = "TENER + sustantivo sin artículo",
                    items = listOf(
                        LessonItem("Tengo hambre.", "Я голоден/голодна.", ""),
                        LessonItem("Tengo sed.", "Я хочу пить.", ""),
                        LessonItem("Tengo miedo.", "Мне страшно.", ""),
                        LessonItem("Tengo sueño.", "Я хочу спать.", ""),
                        LessonItem("Tengo prisa.", "Я тороплюсь.", "")
                    )
                )
            )
        ),

        // u2_l3 — TENER: множественное число
        "u2_l3" to LessonContent(
            intro = "Полная таблица TENER — теперь ты можешь говорить о том, что есть у любого человека.",
            sections = listOf(
                LessonSection(
                    heading = "Полная таблица TENER",
                    items = listOf(
                        LessonItem("yo", "tengo", ""),
                        LessonItem("tú", "tienes", ""),
                        LessonItem("él / ella / Ud.", "tiene", ""),
                        LessonItem("nosotros/as", "tenemos", ""),
                        LessonItem("vosotros/as", "tenéis", ""),
                        LessonItem("ellos / ellas / Uds.", "tienen", "")
                    )
                ),
                LessonSection(
                    heading = "Применяем",
                    items = listOf(
                        LessonItem("Tenemos una casa grande.", "У нас большой дом.", ""),
                        LessonItem("¿Tenéis mascota?", "У вас есть питомец?", "Испания"),
                        LessonItem("Ellos tienen dos hijos.", "У них двое детей.", ""),
                        LessonItem("¿Ustedes tienen reserva?", "У вас есть бронь?", "")
                    )
                )
            )
        ),

        // u2_l7 — Глагол ESTAR: местонахождение
        "u2_l9" to LessonContent(
            intro = "ESTAR — второй глагол «быть». Он используется для местонахождения и временных состояний. SER vs ESTAR — важнейшее различие испанского!",
            sections = listOf(
                LessonSection(
                    heading = "Спряжение ESTAR",
                    items = listOf(
                        LessonItem("yo", "estoy", ""),
                        LessonItem("tú", "estás", ""),
                        LessonItem("él / ella", "está", ""),
                        LessonItem("nosotros", "estamos", ""),
                        LessonItem("vosotros", "estáis", ""),
                        LessonItem("ellos", "están", "")
                    )
                ),
                LessonSection(
                    heading = "ESTAR = где? + как сейчас?",
                    items = listOf(
                        LessonItem("¿Dónde estás?", "Где ты?", ""),
                        LessonItem("Estoy en casa.", "Я дома.", ""),
                        LessonItem("El libro está en la mesa.", "Книга на столе.", ""),
                        LessonItem("Estoy cansado/a.", "Я устал/а.", "временное состояние"),
                        LessonItem("Estoy bien.", "Я в порядке.", "")
                    )
                ),
                LessonSection(
                    heading = "SER vs ESTAR",
                    items = listOf(
                        LessonItem("Es alto.", "Он высокий. (всегда)", "SER = постоянно"),
                        LessonItem("Está cansado.", "Он устал. (сейчас)", "ESTAR = временно"),
                        LessonItem("Es médico.", "Он врач. (профессия)", "SER = идентичность"),
                        LessonItem("Está en Madrid.", "Он в Мадриде. (место)", "ESTAR = положение")
                    )
                )
            )
        ),

        // ══════════════════════════════════════════════
        //  БЛОК 3: ДЕЙСТВИЕ
        // ══════════════════════════════════════════════

        // u3_l0 — Глаголы -AR (yo/tú/él)
        "u3_l0" to LessonContent(
            intro = "Глаголы на -AR — самая большая группа испанских глаголов. Выучи одну таблицу — и ты сможешь спрягать сотни глаголов!",
            sections = listOf(
                LessonSection(
                    heading = "Окончания -AR (ед. число)",
                    items = listOf(
                        LessonItem("yo", "-o", "hablo — я говорю"),
                        LessonItem("tú", "-as", "hablas — ты говоришь"),
                        LessonItem("él / ella", "-a", "habla — он говорит")
                    )
                ),
                LessonSection(
                    heading = "Популярные глаголы -AR",
                    items = listOf(
                        LessonItem("hablar", "говорить", "yo hablo"),
                        LessonItem("trabajar", "работать", "yo trabajo"),
                        LessonItem("estudiar", "учиться", "yo estudio"),
                        LessonItem("escuchar", "слушать", "yo escucho"),
                        LessonItem("bailar", "танцевать", "yo bailo"),
                        LessonItem("llamar", "звонить / называть", "yo llamo"),
                        LessonItem("comprar", "покупать", "yo compro"),
                        LessonItem("caminar", "идти / гулять", "yo camino")
                    )
                ),
                LessonSection(
                    heading = "Фразы",
                    items = listOf(
                        LessonItem("Hablo español.", "Я говорю по-испански.", ""),
                        LessonItem("¿Hablas inglés?", "Ты говоришь по-английски?", ""),
                        LessonItem("Trabajo en una oficina.", "Я работаю в офисе.", ""),
                        LessonItem("Estudio español.", "Я учу испанский.", "")
                    )
                )
            )
        ),

        // u3_l1 — Глаголы -AR: полное спряжение
        "u3_l1" to LessonContent(
            intro = "Теперь добавим множественное число. После этого урока ты можешь говорить о любом действии с любым субъектом!",
            sections = listOf(
                LessonSection(
                    heading = "Полная таблица -AR (hablar)",
                    items = listOf(
                        LessonItem("yo", "hablo", ""),
                        LessonItem("tú", "hablas", ""),
                        LessonItem("él/ella/Ud.", "habla", ""),
                        LessonItem("nosotros/as", "hablamos", ""),
                        LessonItem("vosotros/as", "habláis", ""),
                        LessonItem("ellos/Uds.", "hablan", "")
                    )
                ),
                LessonSection(
                    heading = "Пример: trabajar (работать)",
                    items = listOf(
                        LessonItem("yo", "trabajo", ""),
                        LessonItem("tú", "trabajas", ""),
                        LessonItem("él", "trabaja", ""),
                        LessonItem("nosotros", "trabajamos", ""),
                        LessonItem("vosotros", "trabajáis", ""),
                        LessonItem("ellos", "trabajan", "")
                    )
                )
            )
        ),

        // u3_l2 — Глаголы -ER
        "u3_l2" to LessonContent(
            intro = "Глаголы на -ER — вторая по размеру группа. Окончания чуть отличаются от -AR, но логика та же!",
            sections = listOf(
                LessonSection(
                    heading = "Окончания -ER (comer)",
                    items = listOf(
                        LessonItem("yo", "como", ""),
                        LessonItem("tú", "comes", ""),
                        LessonItem("él / ella", "come", ""),
                        LessonItem("nosotros", "comemos", ""),
                        LessonItem("vosotros", "coméis", ""),
                        LessonItem("ellos", "comen", "")
                    )
                ),
                LessonSection(
                    heading = "Глаголы -ER",
                    items = listOf(
                        LessonItem("comer", "есть / кушать", "yo como"),
                        LessonItem("beber", "пить", "yo bebo"),
                        LessonItem("leer", "читать", "yo leo"),
                        LessonItem("correr", "бегать", "yo corro"),
                        LessonItem("vender", "продавать", "yo vendo"),
                        LessonItem("aprender", "учить / учиться", "yo aprendo")
                    )
                )
            )
        ),

        // u3_l3 — Глаголы -IR
        "u3_l3" to LessonContent(
            intro = "Глаголы на -IR — третья группа, самая маленькая. Они почти как -ER, только в «мы» и «вы» есть разница.",
            sections = listOf(
                LessonSection(
                    heading = "Окончания -IR (vivir)",
                    items = listOf(
                        LessonItem("yo", "vivo", ""),
                        LessonItem("tú", "vives", ""),
                        LessonItem("él / ella", "vive", ""),
                        LessonItem("nosotros", "vivimos", "← отличие от -ER"),
                        LessonItem("vosotros", "vivís", "← отличие от -ER"),
                        LessonItem("ellos", "viven", "")
                    )
                ),
                LessonSection(
                    heading = "Глаголы -IR",
                    items = listOf(
                        LessonItem("vivir", "жить", "yo vivo"),
                        LessonItem("escribir", "писать", "yo escribo"),
                        LessonItem("abrir", "открывать", "yo abro"),
                        LessonItem("subir", "подниматься", "yo subo"),
                        LessonItem("decidir", "решать", "yo decido"),
                        LessonItem("recibir", "получать", "yo recibo")
                    )
                )
            )
        ),

        // u3_l6 — QUERER
        "u3_l6" to LessonContent(
            intro = "QUERER (хотеть / любить) — нерегулярный глагол с чередованием E→IE. Это один из самых нужных глаголов в испанском!",
            sections = listOf(
                LessonSection(
                    heading = "Спряжение QUERER",
                    items = listOf(
                        LessonItem("yo", "quiero", "хочу"),
                        LessonItem("tú", "quieres", "хочешь"),
                        LessonItem("él/ella", "quiere", "хочет"),
                        LessonItem("nosotros", "queremos", "хотим ← без IE"),
                        LessonItem("vosotros", "queréis", "хотите ← без IE"),
                        LessonItem("ellos", "quieren", "хотят")
                    )
                ),
                LessonSection(
                    heading = "Применяем",
                    items = listOf(
                        LessonItem("Quiero café.", "Хочу кофе.", ""),
                        LessonItem("¿Qué quieres comer?", "Что ты хочешь поесть?", ""),
                        LessonItem("Quiero ser médico.", "Хочу стать врачом.", ""),
                        LessonItem("Te quiero.", "Я тебя люблю.", "разговорное"),
                        LessonItem("¿Quieres salir?", "Хочешь выйти?", "")
                    )
                )
            )
        ),

        // u3_l7 — PODER
        "u3_l7" to LessonContent(
            intro = "PODER (мочь, уметь, иметь возможность) — нерегулярный глагол с O→UE. Незаменим для вежливых просьб!",
            sections = listOf(
                LessonSection(
                    heading = "Спряжение PODER",
                    items = listOf(
                        LessonItem("yo", "puedo", "могу"),
                        LessonItem("tú", "puedes", "можешь"),
                        LessonItem("él/ella", "puede", "может"),
                        LessonItem("nosotros", "podemos", "можем ← без UE"),
                        LessonItem("vosotros", "podéis", "можете ← без UE"),
                        LessonItem("ellos", "pueden", "могут")
                    )
                ),
                LessonSection(
                    heading = "Применяем",
                    items = listOf(
                        LessonItem("¿Puedo ayudarte?", "Я могу тебе помочь?", ""),
                        LessonItem("¿Puede repetir, por favor?", "Вы можете повторить?", "вежливо"),
                        LessonItem("No puedo venir.", "Я не могу прийти.", ""),
                        LessonItem("¿Podemos sentarnos?", "Можем сесть?", ""),
                        LessonItem("Puedes hablar más despacio.", "Говори помедленнее.", "")
                    )
                )
            )
        ),

        // u3_l8 — Время: ¿Qué hora es?
        "u3_l8" to LessonContent(
            intro = "Спросить время в испанском — просто! Нужно знать числительные и одну конструкцию.",
            sections = listOf(
                LessonSection(
                    heading = "Как спросить время",
                    items = listOf(
                        LessonItem("¿Qué hora es?", "Который час?", ""),
                        LessonItem("¿Tienes hora?", "У тебя есть время?", "разговорное")
                    )
                ),
                LessonSection(
                    heading = "Как ответить",
                    items = listOf(
                        LessonItem("Es la una.", "Час дня.", "только с la una"),
                        LessonItem("Son las dos.", "Два часа.", "остальные — son las..."),
                        LessonItem("Son las tres y media.", "Три тридцать.", "+media = +30 мин"),
                        LessonItem("Son las cuatro y cuarto.", "Четыре пятнадцать.", "+cuarto = +15 мин"),
                        LessonItem("Son las cinco menos cuarto.", "Без четверти пять.", "menos = минус"),
                        LessonItem("Son las doce del mediodía.", "Полдень.", ""),
                        LessonItem("Es medianoche.", "Полночь.", "")
                    )
                ),
                LessonSection(
                    heading = "Когда?",
                    items = listOf(
                        LessonItem("a las ocho", "в восемь часов", "A las + tiempo"),
                        LessonItem("a la una y media", "в половине второго", ""),
                        LessonItem("por la mañana", "утром", ""),
                        LessonItem("por la tarde", "днём / вечером", ""),
                        LessonItem("por la noche", "ночью / поздним вечером", "")
                    )
                )
            )
        ),

        // u3_l12 — Вопросительные слова
        "u3_l12" to LessonContent(
            intro = "Вопросительные слова — ключ к любому разговору. Выучи их — и ты сможешь спросить о чём угодно!",
            sections = listOf(
                LessonSection(
                    heading = "Вопросительные слова",
                    items = listOf(
                        LessonItem("¿Qué?", "Что? Какой?", "¿Qué haces? — Что ты делаешь?"),
                        LessonItem("¿Quién? / ¿Quiénes?", "Кто?", "¿Quién es? — Кто это?"),
                        LessonItem("¿Dónde?", "Где?", "¿Dónde vives? — Где ты живёшь?"),
                        LessonItem("¿Adónde?", "Куда?", "¿Adónde vas? — Куда идёшь?"),
                        LessonItem("¿Cuándo?", "Когда?", "¿Cuándo llegas? — Когда приедешь?"),
                        LessonItem("¿Cómo?", "Как?", "¿Cómo estás? — Как ты?"),
                        LessonItem("¿Cuánto/a?", "Сколько?", "¿Cuánto cuesta? — Сколько стоит?"),
                        LessonItem("¿Cuántos/as?", "Сколько? (мн.ч.)", "¿Cuántos años? — Сколько лет?"),
                        LessonItem("¿Por qué?", "Почему?", "¿Por qué estudias español?"),
                        LessonItem("¿Para qué?", "Зачем? Для чего?", ""),
                        LessonItem("¿Cuál? / ¿Cuáles?", "Какой? Который?", "¿Cuál prefieres?")
                    )
                )
            )
        ),

        // u3_l13 — Отрицание
        "u3_l13" to LessonContent(
            intro = "Отрицание в испанском проще, чем в русском — одно «no» перед глаголом решает всё!",
            sections = listOf(
                LessonSection(
                    heading = "Простое отрицание",
                    items = listOf(
                        LessonItem("No + глагол", "отрицание", "No hablo chino. — Я не говорю по-китайски."),
                        LessonItem("No tengo hambre.", "Я не голоден.", ""),
                        LessonItem("No entiendo.", "Я не понимаю.", ""),
                        LessonItem("No sé.", "Не знаю.", "")
                    )
                ),
                LessonSection(
                    heading = "Усилители отрицания",
                    items = listOf(
                        LessonItem("nunca", "никогда", "Nunca como carne."),
                        LessonItem("jamás", "никогда (усиленное)", "¡Jamás!"),
                        LessonItem("nada", "ничего", "No tengo nada."),
                        LessonItem("nadie", "никто", "No hay nadie aquí."),
                        LessonItem("tampoco", "тоже нет", "Yo tampoco.")
                    )
                ),
                LessonSection(
                    heading = "Двойное отрицание — нормально!",
                    items = listOf(
                        LessonItem("No conozco a nadie.", "Я не знаю никого.", "в испанском — правильно!"),
                        LessonItem("No tengo nada.", "У меня нет ничего.", ""),
                        LessonItem("No voy nunca.", "Я никогда не иду.", "")
                    )
                )
            )
        ),

        // ══════════════════════════════════════════════
        //  БЛОК 4: ВЫЖИВАНИЕ
        // ══════════════════════════════════════════════

        // u4_l1 — IR: voy, vas, va...
        "u4_l1" to LessonContent(
            intro = "IR (идти / ехать / лететь) — один из самых нерегулярных, но и самых нужных глаголов. Запомни его отдельно!",
            sections = listOf(
                LessonSection(
                    heading = "Спряжение IR",
                    items = listOf(
                        LessonItem("yo", "voy", "я иду / еду"),
                        LessonItem("tú", "vas", "ты идёшь"),
                        LessonItem("él / ella", "va", "он/она идёт"),
                        LessonItem("nosotros", "vamos", "мы идём"),
                        LessonItem("vosotros", "vais", "вы идёте"),
                        LessonItem("ellos", "van", "они идут")
                    )
                ),
                LessonSection(
                    heading = "¡Vamos! — ключевая фраза",
                    items = listOf(
                        LessonItem("¡Vamos!", "Пошли! Давай!", ""),
                        LessonItem("¿Adónde vas?", "Куда ты идёшь?", ""),
                        LessonItem("Voy al trabajo.", "Я иду на работу.", ""),
                        LessonItem("Van al cine.", "Они идут в кино.", ""),
                        LessonItem("¿Vais de vacaciones?", "Вы едете в отпуск?", "")
                    )
                )
            )
        ),

        // u4_l2 — IR + A + lugar
        "u4_l2" to LessonContent(
            intro = "IR + A + место — самый частый способ сказать «я иду/еду куда-то». Плюс: IR A + infinitivo = «собираюсь сделать» (ближайшее будущее)!",
            sections = listOf(
                LessonSection(
                    heading = "IR + A + lugar",
                    items = listOf(
                        LessonItem("Voy a la tienda.", "Я иду в магазин.", ""),
                        LessonItem("Vas al colegio.", "Ты идёшь в школу.", "al = a + el"),
                        LessonItem("Ella va a casa.", "Она идёт домой.", "a casa — без артикля"),
                        LessonItem("Vamos al parque.", "Мы идём в парк.", ""),
                        LessonItem("Van al aeropuerto.", "Они едут в аэропорт.", "")
                    )
                ),
                LessonSection(
                    heading = "IR A + infinitivo = будущее",
                    items = listOf(
                        LessonItem("Voy a comer.", "Я собираюсь поесть.", ""),
                        LessonItem("¿Qué vas a hacer?", "Что ты собираешься делать?", ""),
                        LessonItem("Va a llover.", "Будет дождь.", ""),
                        LessonItem("Vamos a estudiar.", "Мы будем учиться.", ""),
                        LessonItem("¡Voy a aprenderlo!", "Я это выучу!", "")
                    )
                ),
                LessonSection(
                    heading = "A + el = al",
                    items = listOf(
                        LessonItem("a + el banco", "al banco", "в банк"),
                        LessonItem("a + el mercado", "al mercado", "на рынок"),
                        LessonItem("a + la tienda", "a la tienda", "без слияния"),
                        LessonItem("a + la escuela", "a la escuela", "в школу")
                    )
                )
            )
        ),

        // u4_l6 — GUSTAR: me gusta / me gustan
        "u4_l6" to LessonContent(
            intro = "GUSTAR работает необычно: «Мне нравится» = «Me gusta» (буквально: «нравится мне»). Глагол согласуется с тем, что нравится!",
            sections = listOf(
                LessonSection(
                    heading = "Структура GUSTAR",
                    items = listOf(
                        LessonItem("Me gusta el café.", "Мне нравится кофе.", "1 предмет → gusta"),
                        LessonItem("Me gustan los gatos.", "Мне нравятся кошки.", "мн.ч. → gustan"),
                        LessonItem("Me gusta bailar.", "Мне нравится танцевать.", "инфинитив → gusta")
                    )
                ),
                LessonSection(
                    heading = "Все лица",
                    items = listOf(
                        LessonItem("Me gusta / gustan", "мне нравится/нравятся", "yo"),
                        LessonItem("Te gusta / gustan", "тебе нравится/нравятся", "tú"),
                        LessonItem("Le gusta / gustan", "ему/ей нравится", "él/ella"),
                        LessonItem("Nos gusta / gustan", "нам нравится", "nosotros"),
                        LessonItem("Os gusta / gustan", "вам нравится", "vosotros"),
                        LessonItem("Les gusta / gustan", "им нравится", "ellos")
                    )
                ),
                LessonSection(
                    heading = "Примеры",
                    items = listOf(
                        LessonItem("¿Te gusta la música?", "Тебе нравится музыка?", ""),
                        LessonItem("No me gusta el frío.", "Мне не нравится холод.", ""),
                        LessonItem("Les gustan las películas.", "Им нравятся фильмы.", ""),
                        LessonItem("A mí me gusta mucho.", "Мне очень нравится.", "a mí = усиление"),
                        LessonItem("A él le gusta el fútbol.", "Ему нравится футбол.", "")
                    )
                )
            )
        ),

        // u4_l7 — GUSTAR: расширение
        "u4_l7" to LessonContent(
            intro = "Как GUSTAR: другие глаголы чувств и ощущений. Encantar, molestar, interesar — они работают точно так же!",
            sections = listOf(
                LessonSection(
                    heading = "Глаголы типа GUSTAR",
                    items = listOf(
                        LessonItem("encantar", "обожать / очень нравиться", "Me encanta el chocolate."),
                        LessonItem("molestar", "раздражать / беспокоить", "Me molesta el ruido."),
                        LessonItem("interesar", "интересовать", "Me interesa la historia."),
                        LessonItem("doler", "болеть", "Me duele la cabeza."),
                        LessonItem("parecer", "казаться / представляться", "Me parece bien."),
                        LessonItem("quedar", "оставаться / подходить", "Te queda bien. — Тебе идёт.")
                    )
                ),
                LessonSection(
                    heading = "Практика",
                    items = listOf(
                        LessonItem("¡Me encanta España!", "Я обожаю Испанию!", ""),
                        LessonItem("¿Te interesa el arte?", "Тебе интересно искусство?", ""),
                        LessonItem("Me duelen los pies.", "У меня болят ноги.", ""),
                        LessonItem("Nos parece una buena idea.", "Нам кажется, это хорошая идея.", "")
                    )
                )
            )
        ),

        // u4_l13 — Возвратные глаголы
        "u4_l13" to LessonContent(
            intro = "Возвратные глаголы (verbos reflexivos) описывают действия, которые направлены на себя. Они всегда идут с местоимением.",
            sections = listOf(
                LessonSection(
                    heading = "Возвратные местоимения",
                    items = listOf(
                        LessonItem("yo", "me", ""),
                        LessonItem("tú", "te", ""),
                        LessonItem("él/ella", "se", ""),
                        LessonItem("nosotros", "nos", ""),
                        LessonItem("vosotros", "os", ""),
                        LessonItem("ellos", "se", "")
                    )
                ),
                LessonSection(
                    heading = "Пример: levantarse (вставать)",
                    items = listOf(
                        LessonItem("me levanto", "я встаю", ""),
                        LessonItem("te levantas", "ты встаёшь", ""),
                        LessonItem("se levanta", "он/она встаёт", ""),
                        LessonItem("nos levantamos", "мы встаём", ""),
                        LessonItem("os levantáis", "вы встаёте", ""),
                        LessonItem("se levantan", "они встают", "")
                    )
                ),
                LessonSection(
                    heading = "Частые возвратные глаголы",
                    items = listOf(
                        LessonItem("levantarse", "вставать", "Me levanto a las 7."),
                        LessonItem("ducharse", "принимать душ", "Me ducho por la mañana."),
                        LessonItem("vestirse", "одеваться", "Me visto rápido."),
                        LessonItem("acostarse", "ложиться спать", "Me acuesto tarde."),
                        LessonItem("llamarse", "называться", "Me llamo Ana."),
                        LessonItem("sentarse", "садиться", "Siéntate. — Садись."),
                        LessonItem("irse", "уходить", "Me voy. — Я ухожу.")
                    )
                )
            )
        )
    )
}
