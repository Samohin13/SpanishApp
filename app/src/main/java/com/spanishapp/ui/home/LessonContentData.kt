package com.spanishapp.ui.home

// ══════════════════════════════════════════════════════════════
//  Статичное содержимое теоретических уроков.
//  Ключ = "u{unitId}_l{lessonIndex}" — совпадает с lesson_progress.
//  Блок 1 (u1): уроки l0–l14  |  Блоки 2-4: добавляются по мере готовности.
// ══════════════════════════════════════════════════════════════

data class LessonContent(
    val intro: String,
    val sections: List<LessonSection>,
    val exercises: List<Exercise> = emptyList()
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный вариант",
                    question = "Как читается слово «casa»?",
                    options = listOf("КА-са", "КЕЙ-са", "СА-са", "ЧА-са"),
                    correctAnswer = "КА-са",
                    explanation = "Испанская «a» всегда [а], «c» перед «a» = [к]. casa = «ка-са» — дом."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что означает это слово?",
                    question = "amigo",
                    options = listOf("друг", "враг", "брат", "учитель"),
                    correctAnswer = "друг",
                    explanation = "amigo — друг. Читается «а-ми-го»."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Сколько звуков в испанском слове?",
                    question = "Сколько разных звуков у гласных в испанском?",
                    options = listOf("5 — всегда одинаковые", "12 — как в английском", "3 — A, E, O", "7 — с дифтонгами"),
                    correctAnswer = "5 — всегда одинаковые",
                    explanation = "В испанском 5 гласных, и каждая всегда читается одинаково. Это огромный плюс!"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на испанский",
                    question = "Как по-испански «музыка»?",
                    options = listOf("música", "muzika", "musique", "musica"),
                    correctAnswer = "música",
                    explanation = "música — музыка. Тильда над «u» показывает ударение: МУ-си-ка."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный вариант",
                    question = "Как читается буква «V» в испанском?",
                    options = listOf("Как [б/в] — одинаково с B", "Как [в] — только звонкий", "Как [ф]", "Как [у]"),
                    correctAnswer = "Как [б/в] — одинаково с B",
                    explanation = "В испанском B и V звучат одинаково! vino читается «бино»."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как читается это слово?",
                    question = "gente",
                    options = listOf("[хэнтэ]", "[гэнтэ]", "[джэнтэ]", "[зэнтэ]"),
                    correctAnswer = "[хэнтэ]",
                    explanation = "G перед «e» и «i» читается как [х]. gente = «хэнтэ» — люди."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи слово",
                    question = "vida",
                    options = listOf("жизнь", "вода", "видео", "победа"),
                    correctAnswer = "жизнь",
                    explanation = "vida — жизнь. Читается «би-да» — D между гласными мягкий."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный ответ",
                    question = "Как читается «G» перед буквами A, O, U?",
                    options = listOf("Как [г]", "Как [х]", "Как [дж]", "Как [ч]"),
                    correctAnswer = "Как [г]",
                    explanation = "G + a/o/u = [г]. Пример: gato (кот) = «гато»."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный вариант",
                    question = "Как читается «H» в испанском?",
                    options = listOf("Молчит, не читается", "Как [х]", "Как [г]", "Как [h] в английском"),
                    correctAnswer = "Молчит, не читается",
                    explanation = "H в испанском всегда молчит! hola = «ола», hotel = «отэль»."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи слово",
                    question = "mañana",
                    options = listOf("завтра / утро", "вечер", "сегодня", "ночь"),
                    correctAnswer = "завтра / утро",
                    explanation = "mañana = завтра или утро. Ñ читается как «нь»: «маньяна»."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как читается это слово?",
                    question = "jamón",
                    options = listOf("[хамон]", "[джамон]", "[ямон]", "[гамон]"),
                    correctAnswer = "[хамон]",
                    explanation = "J всегда читается [х]. jamón — хамон (вяленая ветчина)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи слово",
                    question = "perro",
                    options = listOf("собака", "кошка", "птица", "рыба"),
                    correctAnswer = "собака",
                    explanation = "perro — собака. RR раскатистый: «пэ-рро». Одинарная R в середине слова мягче."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Где ударение?",
                    question = "Слово «casa» — куда падает ударение?",
                    options = listOf("CA-sa (предпоследний слог)", "ca-SA (последний слог)", "На оба слога", "Неизвестно"),
                    correctAnswer = "CA-sa (предпоследний слог)",
                    explanation = "casa оканчивается на гласную «a» → ударение на предпоследний слог: CA-sa."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Зачем стоит тильда?",
                    question = "Почему в слове «café» стоит тильда?",
                    options = listOf("Ударение на последнем слоге, но слово на гласную — исключение", "Просто украшение", "Слово иностранное", "Всегда ставят на e"),
                    correctAnswer = "Ударение на последнем слоге, но слово на гласную — исключение",
                    explanation = "По правилу слова на гласную ударяются на предпоследний. café нарушает → тильда."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи вопрос",
                    question = "¿Dónde?",
                    options = listOf("Где?", "Кто?", "Когда?", "Почему?"),
                    correctAnswer = "Где?",
                    explanation = "¿Dónde? = Где? Тильда над «o» показывает ударение и вопросительное значение."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Где ударение?",
                    question = "Слово «hablar» оканчивается на R (согласная). Где ударение?",
                    options = listOf("ha-BLAR (последний слог)", "HAB-lar (первый слог)", "hab-LAR (второй слог)", "Равное"),
                    correctAnswer = "ha-BLAR (последний слог)",
                    explanation = "Слова на согласную (не N/S) ударяются на последний слог. hablar = «а-БЛАР»."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Я — студент. (Yo) ___ estudiante.",
                    options = listOf("soy", "eres", "es", "son"),
                    correctAnswer = "soy",
                    explanation = "Yo soy estudiante — Я студент. SER для «yo» = soy."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Ты из Испании? ¿Tú ___ de España?",
                    options = listOf("eres", "soy", "es", "somos"),
                    correctAnswer = "eres",
                    explanation = "Tú eres — ты есть. «¿Tú eres de España?» — Ты из Испании?"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на русский",
                    question = "Él es médico.",
                    options = listOf("Он врач.", "Он болен.", "Он здесь.", "Он студент?"),
                    correctAnswer = "Он врач.",
                    explanation = "Él es médico — Он врач. SER используется для профессий."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на испанский",
                    question = "Она из России.",
                    options = listOf("Ella es de Rusia.", "Ella está de Rusia.", "Ella soy de Rusia.", "Ella eres de Rusia."),
                    correctAnswer = "Ella es de Rusia.",
                    explanation = "Ella es de Rusia. Происхождение выражается через SER + de + место."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Мы друзья. (Nosotros) ___ amigos.",
                    options = listOf("somos", "sois", "son", "soy"),
                    correctAnswer = "somos",
                    explanation = "Nosotros somos — мы есть. somos = форма SER для nosotros."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на русский",
                    question = "Ellos son estudiantes.",
                    options = listOf("Они студенты.", "Он студент.", "Мы студенты.", "Вы студенты."),
                    correctAnswer = "Они студенты.",
                    explanation = "Ellos son — они есть. son = форма SER для ellos/ellas/ustedes."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму (вежливо)",
                    question = "Вы из России? ¿Ustedes ___ de Rusia?",
                    options = listOf("son", "sois", "somos", "eres"),
                    correctAnswer = "son",
                    explanation = "Ustedes son — вежливая форма мн.ч. В Латинской Америке ustedes заменяет vosotros."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на испанский",
                    question = "Мы из Мадрида.",
                    options = listOf("Somos de Madrid.", "Son de Madrid.", "Sois de Madrid.", "Somos en Madrid."),
                    correctAnswer = "Somos de Madrid.",
                    explanation = "Somos de Madrid — мы из Мадрида. SER + de + место = происхождение."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильное местоимение",
                    question = "«Они» (смешанная группа) = ?",
                    options = listOf("ellos", "ellas", "nosotros", "ustedes"),
                    correctAnswer = "ellos",
                    explanation = "Смешанная группа или только мужчины = ellos. ellas — только для женщин."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что означает?",
                    question = "usted (Ud.)",
                    options = listOf("Вы (вежливо, один человек)", "ты", "Вы (мн.ч.)", "вы (Испания)"),
                    correctAnswer = "Вы (вежливо, один человек)",
                    explanation = "usted — вежливое «вы» для одного человека. Сокращается Ud. или Vd."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Нужно ли местоимение?",
                    question = "Как по-испански «я говорю» (без выделения)?",
                    options = listOf("Hablo.", "Yo hablo.", "Hablo yo.", "Yo lo hablo."),
                    correctAnswer = "Hablo.",
                    explanation = "Местоимение часто опускают — окончание -o уже показывает «я». Hablo = я говорю."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный вариант",
                    question = "«Вы» в Испании (неформально, мн.ч.) = ?",
                    options = listOf("vosotros", "ustedes", "usted", "ellos"),
                    correctAnswer = "vosotros",
                    explanation = "vosotros — неформальное мн.ч., только в Испании. В Латинской Америке говорят ustedes."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный артикль",
                    question = "___ libro (книга)",
                    options = listOf("el", "la", "los", "las"),
                    correctAnswer = "el",
                    explanation = "libro оканчивается на -o → мужской род → el libro."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный артикль",
                    question = "___ mano (рука)",
                    options = listOf("la", "el", "los", "un"),
                    correctAnswer = "la",
                    explanation = "la mano — исключение! Оканчивается на -o, но женский род. Запомни: la mano, la foto."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Определи род по окончанию",
                    question = "Слова на -ción (canción, nación) — какой род?",
                    options = listOf("Всегда женский", "Всегда мужской", "Зависит от слова", "Нет правила"),
                    correctAnswer = "Всегда женский",
                    explanation = "Слова на -ción, -sión, -dad всегда женского рода: la canción, la ciudad."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный артикль",
                    question = "___ problema (проблема)",
                    options = listOf("el", "la", "un", "una"),
                    correctAnswer = "el",
                    explanation = "el problema — исключение! Слова греч. происхождения на -ma мужского рода: el programa, el sistema."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный артикль",
                    question = "Я вижу собаку (упоминаю впервые). Veo ___ perro.",
                    options = listOf("un", "el", "los", "unos"),
                    correctAnswer = "un",
                    explanation = "Первое упоминание = неопределённый артикль. un perro — какая-то собака."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный вариант",
                    question = "Собака (уже знакомая) большая. ___ perro es grande.",
                    options = listOf("El", "Un", "Los", "Unos"),
                    correctAnswer = "El",
                    explanation = "Уже известный предмет = определённый артикль. El perro — та самая собака."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Когда артикль не нужен?",
                    question = "Я врач. Soy ___.",
                    options = listOf("médico (без артикля)", "el médico", "un médico", "los médicos"),
                    correctAnswer = "médico (без артикля)",
                    explanation = "После SER с профессией артикль не нужен: Soy médico, Eres estudiante."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на испанский",
                    question = "Несколько домов (неопределённо)",
                    options = listOf("unas casas", "las casas", "unos casas", "una casa"),
                    correctAnswer = "unas casas",
                    explanation = "unas casas — несколько домов. casa — женский род → unas."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "У меня есть кошка. (Yo) ___ un gato.",
                    options = listOf("tengo", "tienes", "tiene", "tenemos"),
                    correctAnswer = "tengo",
                    explanation = "Yo tengo — у меня есть. tengo = форма TENER для yo."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на русский",
                    question = "Ella tiene 20 años.",
                    options = listOf("Ей 20 лет.", "У неё 20 евро.", "Она 20-я.", "Она берёт 20."),
                    correctAnswer = "Ей 20 лет.",
                    explanation = "tener + años = возраст. В испанском говорят «иметь лет», не «быть»."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать?",
                    question = "Я голоден.",
                    options = listOf("Tengo hambre.", "Soy hambre.", "Estoy hambre.", "Hay hambre."),
                    correctAnswer = "Tengo hambre.",
                    explanation = "Tengo hambre — буквально «имею голод». Состояния через TENER: hambre, sed, miedo, sueño."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "У тебя есть братья? ¿___ hermanos?",
                    options = listOf("Tienes", "Tengo", "Tiene", "Tenes"),
                    correctAnswer = "Tienes",
                    explanation = "¿Tienes hermanos? tú tienes = ты имеешь."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "У нас большой дом. ___ una casa grande.",
                    options = listOf("Tenemos", "Tienen", "Tenéis", "Tener"),
                    correctAnswer = "Tenemos",
                    explanation = "Nosotros tenemos — у нас есть. -emos = окончание для nosotros."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на русский",
                    question = "Ellos tienen dos hijos.",
                    options = listOf("У них двое детей.", "Они двое детей.", "Его двое детей.", "У него двое детей."),
                    correctAnswer = "У них двое детей.",
                    explanation = "Ellos tienen dos hijos. hijo = сын/ребёнок, hijos = дети."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "¿___ ustedes reserva?",
                    options = listOf("Tienen", "Tenéis", "Tenemos", "Tienes"),
                    correctAnswer = "Tienen",
                    explanation = "Ustedes tienen — вежливое мн.ч. tienen = форма для ellos/ellas/ustedes."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать в Испании (неформально)?",
                    question = "У вас есть питомец? (vosotros)",
                    options = listOf("¿Tenéis mascota?", "¿Tienen mascota?", "¿Tienes mascota?", "¿Tenemos mascota?"),
                    correctAnswer = "¿Tenéis mascota?",
                    explanation = "Vosotros tenéis — форма для vosotros, используется в Испании."
                )
            )
        ),

        // u2_l6 — Притяжательные: mi, tu, su, nuestro/a
        "u2_l6" to LessonContent(
            intro = "Притяжательные местоимения показывают кому принадлежит предмет. В испанском они согласуются с предметом, а не с владельцем!",
            sections = listOf(
                LessonSection(
                    heading = "Притяжательные (перед существительным)",
                    items = listOf(
                        LessonItem("mi / mis", "мой / мои", "mi casa, mis libros"),
                        LessonItem("tu / tus", "твой / твои", "tu perro, tus amigos"),
                        LessonItem("su / sus", "его / её / Ваш", "su madre, sus hijos"),
                        LessonItem("nuestro/a / nuestros/as", "наш / наша / наши", "nuestro coche, nuestra casa"),
                        LessonItem("vuestro/a / vuestros/as", "ваш (Испания)", "vuestro hijo"),
                        LessonItem("su / sus", "их / Ваш (мн.ч.)", "su trabajo, sus ideas")
                    )
                ),
                LessonSection(
                    heading = "Важно: согласование с предметом",
                    items = listOf(
                        LessonItem("mi libro", "моя книга (м.р.)", ""),
                        LessonItem("mi casa", "мой дом (ж.р.)", "mi — не меняется!"),
                        LessonItem("nuestro hijo", "наш сын (м.р.)", ""),
                        LessonItem("nuestra hija", "наша дочь (ж.р.)", "nuestro/a меняется!")
                    )
                ),
                LessonSection(
                    heading = "Примеры",
                    items = listOf(
                        LessonItem("¿Cómo se llama tu perro?", "Как зовут твою собаку?", ""),
                        LessonItem("Mi madre es profesora.", "Моя мама — учительница.", ""),
                        LessonItem("Nuestros amigos son simpáticos.", "Наши друзья приятные.", ""),
                        LessonItem("¿Dónde está su casa?", "Где его/её/их дом?", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный вариант",
                    question = "___ libro es interesante. (моя книга)",
                    options = listOf("Mi", "Tu", "Su", "Mis"),
                    correctAnswer = "Mi",
                    explanation = "mi libro — моя книга. mi не меняется по роду: mi libro, mi casa."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный вариант",
                    question = "___ casa es grande. (наш дом — женский род)",
                    options = listOf("Nuestra", "Nuestro", "Su", "Mi"),
                    correctAnswer = "Nuestra",
                    explanation = "casa — женский род → nuestra casa. nuestro/nuestra меняется по роду предмета."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на испанский",
                    question = "Твои друзья симпатичные.",
                    options = listOf("Tus amigos son simpáticos.", "Tu amigos son simpáticos.", "Sus amigos son simpáticos.", "Mi amigos son simpáticos."),
                    correctAnswer = "Tus amigos son simpáticos.",
                    explanation = "amigos — множественное число → tus (мн.ч. от tu)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "С чем согласуется притяжательное?",
                    question = "«Наша» по-испански — nuestro или nuestra?",
                    options = listOf("Зависит от рода предмета", "Зависит от рода владельца", "Всегда nuestro", "Всегда nuestra"),
                    correctAnswer = "Зависит от рода предмета",
                    explanation = "nuestro hijo (сын — м.р.), nuestra hija (дочь — ж.р.). Согласуется с тем, о чём говорим."
                )
            )
        ),

        // u2_l8 — Согласование прилагательных: rojo/roja, blanco/blanca
        "u2_l8" to LessonContent(
            intro = "В испанском прилагательные согласуются с существительным по роду и числу. Это один из важнейших принципов грамматики!",
            sections = listOf(
                LessonSection(
                    heading = "Согласование по роду",
                    items = listOf(
                        LessonItem("el coche rojo", "красная машина (м.р.)", "-o для мужского"),
                        LessonItem("la casa roja", "красный дом (ж.р.)", "-a для женского"),
                        LessonItem("el gato negro", "чёрный кот", ""),
                        LessonItem("la gata negra", "чёрная кошка", "")
                    )
                ),
                LessonSection(
                    heading = "Прилагательные на -e и на согласную",
                    items = listOf(
                        LessonItem("grande", "большой / большая", "не меняется: el libro grande / la casa grande"),
                        LessonItem("inteligente", "умный / умная", "el chico inteligente / la chica inteligente"),
                        LessonItem("azul", "синий / синяя", "не меняется: el cielo azul / la flor azul"),
                        LessonItem("verde", "зелёный / зелёная", "el árbol verde / la hierba verde")
                    )
                ),
                LessonSection(
                    heading = "Множественное число",
                    items = listOf(
                        LessonItem("los coches rojos", "красные машины (м.р.)", "-os"),
                        LessonItem("las casas rojas", "красные дома (ж.р.)", "-as"),
                        LessonItem("los libros grandes", "большие книги", "-s после -e"),
                        LessonItem("las flores azules", "синие цветы", "-es после согласной")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "la casa ___ (красный)",
                    options = listOf("roja", "rojo", "rojos", "rojas"),
                    correctAnswer = "roja",
                    explanation = "casa — женский род → roja. -a для женского рода."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "el libro ___ (большой)",
                    options = listOf("grande", "granda", "grandes", "grando"),
                    correctAnswer = "grande",
                    explanation = "Прилагательные на -e не меняются по роду: el libro grande, la mesa grande."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на испанский",
                    question = "синие цветы (las flores)",
                    options = listOf("las flores azules", "las flores azul", "las flores azulos", "las flores azulas"),
                    correctAnswer = "las flores azules",
                    explanation = "azul — прилагательное на согласную. Мн.ч.: azul → azules."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный вариант",
                    question = "los gatos ___ (чёрный)",
                    options = listOf("negros", "negras", "negro", "negra"),
                    correctAnswer = "negros",
                    explanation = "los gatos — мужской род мн.ч. → negros (-os)."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Я дома. ___ en casa.",
                    options = listOf("Estoy", "Soy", "Estás", "Está"),
                    correctAnswer = "Estoy",
                    explanation = "Estoy en casa — я дома. ESTAR для местонахождения."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "SER или ESTAR?",
                    question = "Он устал. (временное) Él ___ cansado.",
                    options = listOf("está", "es", "estoy", "eres"),
                    correctAnswer = "está",
                    explanation = "Él está cansado — он устал сейчас. ESTAR = временное состояние."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "SER или ESTAR?",
                    question = "Он высокий. (постоянно) Él ___ alto.",
                    options = listOf("es", "está", "estoy", "son"),
                    correctAnswer = "es",
                    explanation = "Él es alto — он высокий (всегда). SER = постоянные качества."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на испанский",
                    question = "Книга на столе.",
                    options = listOf("El libro está en la mesa.", "El libro es en la mesa.", "La libro está en la mesa.", "El libro estoy en la mesa."),
                    correctAnswer = "El libro está en la mesa.",
                    explanation = "ESTAR для местонахождения предметов. está = он/она находится."
                )
            )
        ),

        // u2_l10 — Предлоги места: en, sobre, debajo de, al lado de
        "u2_l10" to LessonContent(
            intro = "Предлоги места помогают описать где находится предмет. Запомни 8 главных — и ты сможешь объяснить расположение чего угодно!",
            sections = listOf(
                LessonSection(
                    heading = "Основные предлоги места",
                    items = listOf(
                        LessonItem("en", "в, на", "El libro está en la mesa."),
                        LessonItem("sobre / encima de", "на (сверху), над", "El gato está sobre el sofá."),
                        LessonItem("debajo de", "под", "El perro está debajo de la cama."),
                        LessonItem("al lado de", "рядом с, около", "La tienda está al lado del banco."),
                        LessonItem("delante de", "перед", "El coche está delante de la casa."),
                        LessonItem("detrás de", "за, позади", "El jardín está detrás de la casa."),
                        LessonItem("entre", "между", "La farmacia está entre el banco y el cine."),
                        LessonItem("cerca de / lejos de", "рядом с / далеко от", "¿Está cerca de aquí?")
                    )
                ),
                LessonSection(
                    heading = "de + el = del",
                    items = listOf(
                        LessonItem("al lado de + el banco", "al lado del banco", "de+el = del"),
                        LessonItem("detrás de + el colegio", "detrás del colegio", ""),
                        LessonItem("de + la tienda", "de la tienda", "de+la НЕ сливается")
                    )
                ),
                LessonSection(
                    heading = "Диалог: ¿Dónde está...?",
                    items = listOf(
                        LessonItem("¿Dónde está el baño?", "Где туалет?", ""),
                        LessonItem("Está al final del pasillo.", "В конце коридора.", ""),
                        LessonItem("¿Hay una farmacia cerca?", "Есть аптека рядом?", ""),
                        LessonItem("Sí, está enfrente del hotel.", "Да, напротив отеля.", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный предлог",
                    question = "Кот на диване. El gato está ___ el sofá.",
                    options = listOf("sobre", "debajo de", "entre", "detrás de"),
                    correctAnswer = "sobre",
                    explanation = "sobre = на (сверху). El gato está sobre el sofá — кот на диване."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи предлог",
                    question = "debajo de",
                    options = listOf("под", "над", "рядом", "перед"),
                    correctAnswer = "под",
                    explanation = "debajo de = под. El perro está debajo de la mesa — собака под столом."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный вариант",
                    question = "al lado de + el banco = ?",
                    options = listOf("al lado del banco", "al lado de el banco", "al lado la banco", "al del banco"),
                    correctAnswer = "al lado del banco",
                    explanation = "de + el = del. Всегда сливается: del banco, del colegio."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на испанский",
                    question = "Аптека между банком и кино.",
                    options = listOf("La farmacia está entre el banco y el cine.", "La farmacia está al lado del banco y el cine.", "La farmacia está sobre el banco y el cine.", "La farmacia está enfrente del banco y el cine."),
                    correctAnswer = "La farmacia está entre el banco y el cine.",
                    explanation = "entre = между. entre A y B = между A и B."
                )
            )
        ),

        // u2_l13 — Множественное число: -s и -es
        "u2_l13" to LessonContent(
            intro = "Образовать множественное число в испанском несложно — два правила и несколько исключений.",
            sections = listOf(
                LessonSection(
                    heading = "Правило 1: + -s (после гласной)",
                    items = listOf(
                        LessonItem("libro → libros", "книга → книги", "на -o"),
                        LessonItem("casa → casas", "дом → дома", "на -a"),
                        LessonItem("coche → coches", "машина → машины", "на -e"),
                        LessonItem("tribu → tribus", "племя → племена", "на -u")
                    )
                ),
                LessonSection(
                    heading = "Правило 2: + -es (после согласной)",
                    items = listOf(
                        LessonItem("ciudad → ciudades", "город → города", "на -d"),
                        LessonItem("canción → canciones", "песня → песни", "на -n, тильда исчезает"),
                        LessonItem("papel → papeles", "бумага → бумаги", "на -l"),
                        LessonItem("árbol → árboles", "дерево → деревья", "на -l")
                    )
                ),
                LessonSection(
                    heading = "Артикли во мн.ч.",
                    items = listOf(
                        LessonItem("el libro → los libros", "книга → книги", "el → los"),
                        LessonItem("la casa → las casas", "дом → дома", "la → las"),
                        LessonItem("un perro → unos perros", "собака → собаки", "un → unos"),
                        LessonItem("una mesa → unas mesas", "стол → столы", "una → unas")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Образуй множественное число",
                    question = "libro → ?",
                    options = listOf("libros", "libres", "libroes", "libross"),
                    correctAnswer = "libros",
                    explanation = "libro оканчивается на гласную -o → добавляем -s → libros."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Образуй множественное число",
                    question = "ciudad → ?",
                    options = listOf("ciudades", "ciudads", "ciudas", "ciudades"),
                    correctAnswer = "ciudades",
                    explanation = "ciudad оканчивается на согласную -d → добавляем -es → ciudades."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный артикль мн.ч.",
                    question = "la casa → ___",
                    options = listOf("las casas", "los casas", "unas casas", "las cosas"),
                    correctAnswer = "las casas",
                    explanation = "la (ж.р.) → las во мн.ч. casa → casas (гласная + s)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что происходит с тильдой?",
                    question = "canción → canciones. Почему исчезла тильда?",
                    options = listOf("Ударение перешло на новый слог автоматически", "Это ошибка", "Тильда всегда исчезает во мн.ч.", "Слово изменило значение"),
                    correctAnswer = "Ударение перешло на новый слог автоматически",
                    explanation = "canción → canciones: добавился слог -es, ударение теперь падает правильно без тильды."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Я говорю по-испански. (Yo) ___ español.",
                    options = listOf("hablo", "hablas", "habla", "hablar"),
                    correctAnswer = "hablo",
                    explanation = "Yo + глагол -AR → -o: hablar → hablo."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Ты работаешь? ¿___ ?",
                    options = listOf("Trabajas", "Trabajo", "Trabaja", "Trabajar"),
                    correctAnswer = "Trabajas",
                    explanation = "Tú + глагол -AR → -as: trabajar → trabajas."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на испанский",
                    question = "Она танцует хорошо.",
                    options = listOf("Ella baila bien.", "Ella bailas bien.", "Ella bailo bien.", "Ella bailar bien."),
                    correctAnswer = "Ella baila bien.",
                    explanation = "Él/ella + глагол -AR → -a: bailar → baila."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что означает глагол?",
                    question = "escuchar",
                    options = listOf("слушать", "говорить", "смотреть", "читать"),
                    correctAnswer = "слушать",
                    explanation = "escuchar = слушать. yo escucho música — я слушаю музыку."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Мы говорим по-русски. ___ ruso.",
                    options = listOf("Hablamos", "Habláis", "Hablan", "Habla"),
                    correctAnswer = "Hablamos",
                    explanation = "Nosotros + -AR → -amos: hablar → hablamos."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Они работают здесь. ___ aquí.",
                    options = listOf("Trabajan", "Trabajamos", "Trabajáis", "Trabaja"),
                    correctAnswer = "Trabajan",
                    explanation = "Ellos + -AR → -an: trabajar → trabajan."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на испанский",
                    question = "Вы (Испания) танцуете хорошо.",
                    options = listOf("Bailáis bien.", "Bailan bien.", "Bailamos bien.", "Bailas bien."),
                    correctAnswer = "Bailáis bien.",
                    explanation = "Vosotros + -AR → -áis: bailar → bailáis."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Найди ошибку",
                    question = "Ellos estudian español. — правильно или нет?",
                    options = listOf("Правильно", "Должно быть «estudian»", "Должно быть «estudiamos»", "Должно быть «estudias»"),
                    correctAnswer = "Правильно",
                    explanation = "Ellos estudian — верно! ellos + -AR → -an."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Я ем пиццу. (Yo) ___ pizza.",
                    options = listOf("como", "comes", "come", "comer"),
                    correctAnswer = "como",
                    explanation = "Yo + глагол -ER → -o: comer → como."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на испанский",
                    question = "Он читает книгу.",
                    options = listOf("Él lee un libro.", "Él lees un libro.", "Él leo un libro.", "Él leen un libro."),
                    correctAnswer = "Él lee un libro.",
                    explanation = "Él + leer → lee. -ER: él/ella → -e."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Мы пьём воду. ___ agua.",
                    options = listOf("Bebemos", "Bebéis", "Beben", "Bebe"),
                    correctAnswer = "Bebemos",
                    explanation = "Nosotros + -ER → -emos: beber → bebemos."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что означает глагол?",
                    question = "aprender",
                    options = listOf("учить / учиться", "открывать", "бегать", "продавать"),
                    correctAnswer = "учить / учиться",
                    explanation = "aprender = учить, учиться. Estudio español y aprendo mucho — учу испанский и многому учусь."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Я живу в Москве. (Yo) ___ en Moscú.",
                    options = listOf("vivo", "vives", "vive", "vivir"),
                    correctAnswer = "vivo",
                    explanation = "Yo + -IR → -o: vivir → vivo."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "В чём отличие -IR от -ER?",
                    question = "«Мы» для -ER: com-emos. «Мы» для -IR: viv-___",
                    options = listOf("-imos", "-emos", "-amos", "-imos"),
                    correctAnswer = "-imos",
                    explanation = "-ER: nosotros -emos (comemos). -IR: nosotros -imos (vivimos). Только эти две формы отличаются."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на испанский",
                    question = "Она пишет письмо.",
                    options = listOf("Ella escribe una carta.", "Ella escribes una carta.", "Ella escribo una carta.", "Ella escriben una carta."),
                    correctAnswer = "Ella escribe una carta.",
                    explanation = "Ella + escribir → escribe. -IR: él/ella → -e."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что означает глагол?",
                    question = "abrir",
                    options = listOf("открывать", "закрывать", "писать", "получать"),
                    correctAnswer = "открывать",
                    explanation = "abrir = открывать. Abre la puerta — открой дверь."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Я хочу кофе. (Yo) ___ café.",
                    options = listOf("quiero", "quieres", "quiere", "queremos"),
                    correctAnswer = "quiero",
                    explanation = "Yo quiero — я хочу. E→IE: querer → quiero (но queremos без IE)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на русский",
                    question = "¿Qué quieres comer?",
                    options = listOf("Что ты хочешь поесть?", "Что ты можешь есть?", "Что ты ешь?", "Что ты купил?"),
                    correctAnswer = "Что ты хочешь поесть?",
                    explanation = "quieres comer = хочешь есть. QUERER + infinitivo = хотеть + сделать."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Они хотят выйти. ___ salir.",
                    options = listOf("Quieren", "Queremos", "Queréis", "Quiere"),
                    correctAnswer = "Quieren",
                    explanation = "Ellos quieren — они хотят. E→IE в ellos тоже: quieren."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать по-испански?",
                    question = "Я тебя люблю. (разговорное)",
                    options = listOf("Te quiero.", "Te quieres.", "Yo quiero tú.", "Quiero tú."),
                    correctAnswer = "Te quiero.",
                    explanation = "Te quiero — я тебя люблю (разговорное). Te = тебя (перед глаголом)."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Я не могу прийти. No ___ venir.",
                    options = listOf("puedo", "puedes", "puede", "podemos"),
                    correctAnswer = "puedo",
                    explanation = "Yo puedo — я могу. O→UE: poder → puedo."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на испанский (вежливо)",
                    question = "Вы можете повторить?",
                    options = listOf("¿Puede repetir?", "¿Puedo repetir?", "¿Puedes repetir?", "¿Podemos repetir?"),
                    correctAnswer = "¿Puede repetir?",
                    explanation = "¿Puede repetir? — вежливая просьба. usted puede = Вы можете."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Они могут помочь. ___ ayudar.",
                    options = listOf("Pueden", "Podemos", "Podéis", "Puede"),
                    correctAnswer = "Pueden",
                    explanation = "Ellos pueden — они могут. O→UE в ellos тоже: pueden."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как вежливо попросить?",
                    question = "Говори помедленнее, пожалуйста.",
                    options = listOf("Puedes hablar más despacio.", "Puedo hablar más despacio.", "Puede hablar más despacio.", "Podemos hablar más despacio."),
                    correctAnswer = "Puedes hablar más despacio.",
                    explanation = "Puedes hablar más despacio — ты можешь говорить помедленнее. Вежливая просьба."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «час дня»?",
                    question = "Сейчас 1:00.",
                    options = listOf("Es la una.", "Son las una.", "Es las una.", "Son la una."),
                    correctAnswer = "Es la una.",
                    explanation = "Только 1 час = Es la una (единственное число). Все остальные: Son las dos/tres..."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на русский",
                    question = "Son las tres y media.",
                    options = listOf("Три тридцать.", "Три пятнадцать.", "Без получаса четыре.", "Полтретьего."),
                    correctAnswer = "Три тридцать.",
                    explanation = "y media = и половина = +30 минут. Son las tres y media = 3:30."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать?",
                    question = "Встреча в 8 утра.",
                    options = listOf("La reunión es a las ocho de la mañana.", "La reunión es son las ocho.", "La reunión está a las ocho.", "La reunión es en las ocho."),
                    correctAnswer = "La reunión es a las ocho de la mañana.",
                    explanation = "a las ocho = в восемь. de la mañana = утра."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на русский",
                    question = "Son las cinco menos cuarto.",
                    options = listOf("Без четверти пять.", "Пять пятнадцать.", "Четыре сорок пять.", "Без пяти пять."),
                    correctAnswer = "Без четверти пять.",
                    explanation = "menos cuarto = минус четверть (15 минут). Son las cinco menos cuarto = 4:45."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильное вопросительное слово",
                    question = "___ estás? (Как ты?)",
                    options = listOf("¿Cómo", "¿Qué", "¿Dónde", "¿Cuándo"),
                    correctAnswer = "¿Cómo",
                    explanation = "¿Cómo estás? — Как ты? cómo = как."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи вопрос",
                    question = "¿Cuánto cuesta?",
                    options = listOf("Сколько стоит?", "Где это?", "Когда?", "Почему?"),
                    correctAnswer = "Сколько стоит?",
                    explanation = "¿Cuánto cuesta? = Сколько стоит? cuánto = сколько, costar = стоить."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный вопрос",
                    question = "Ты спрашиваешь «куда идёт человек»",
                    options = listOf("¿Adónde vas?", "¿Dónde estás?", "¿Cuándo vas?", "¿Por qué vas?"),
                    correctAnswer = "¿Adónde vas?",
                    explanation = "¿Adónde? = куда (движение). ¿Dónde? = где (положение)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи вопрос",
                    question = "¿Por qué estudias español?",
                    options = listOf("Почему ты учишь испанский?", "Для чего учишь испанский?", "Как учишь испанский?", "Когда учишь испанский?"),
                    correctAnswer = "Почему ты учишь испанский?",
                    explanation = "¿Por qué? = почему. ¿Para qué? = зачем/для чего."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на испанский",
                    question = "Я не говорю по-китайски.",
                    options = listOf("No hablo chino.", "Hablo no chino.", "No hablar chino.", "No hablas chino."),
                    correctAnswer = "No hablo chino.",
                    explanation = "No + глагол = отрицание. No стоит прямо перед глаголом."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на русский",
                    question = "No tengo nada.",
                    options = listOf("У меня ничего нет.", "У меня что-то есть.", "Я ничего не хочу.", "Мне ничего не надо."),
                    correctAnswer = "У меня ничего нет.",
                    explanation = "No tengo nada — двойное отрицание в испанском правильно! nada = ничего."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «никогда»?",
                    question = "Я никогда не ем мясо.",
                    options = listOf("No como carne nunca. / Nunca como carne.", "No como nada carne.", "Jamás como siempre carne.", "Tampoco como carne."),
                    correctAnswer = "No como carne nunca. / Nunca como carne.",
                    explanation = "nunca = никогда. Можно: Nunca como carne. или No como carne nunca."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что означает?",
                    question = "Yo tampoco.",
                    options = listOf("Я тоже нет.", "Я тоже да.", "Я не знаю.", "Я никогда."),
                    correctAnswer = "Я тоже нет.",
                    explanation = "tampoco = тоже нет. ¿No te gusta? — No me gusta. — Yo tampoco."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Я иду на работу. (Yo) ___ al trabajo.",
                    options = listOf("voy", "vas", "va", "vamos"),
                    correctAnswer = "voy",
                    explanation = "Yo voy — я иду. IR совсем нерегулярный: voy, vas, va, vamos, vais, van."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на русский",
                    question = "¿Adónde vas?",
                    options = listOf("Куда ты идёшь?", "Где ты?", "Как ты идёшь?", "Когда идёшь?"),
                    correctAnswer = "Куда ты идёшь?",
                    explanation = "¿Adónde vas? — куда ты идёшь? adónde = куда (движение)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Они идут в кино. ___ al cine.",
                    options = listOf("Van", "Vamos", "Vais", "Vas"),
                    correctAnswer = "Van",
                    explanation = "Ellos van — они идут."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать по-испански?",
                    question = "Пошли!",
                    options = listOf("¡Vamos!", "¡Van!", "¡Voy!", "¡Vas!"),
                    correctAnswer = "¡Vamos!",
                    explanation = "¡Vamos! — пошли / давай. Одна из самых частых фраз в испанском!"
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный вариант",
                    question = "Я иду в банк. Voy ___ banco.",
                    options = listOf("al", "a el", "a la", "en el"),
                    correctAnswer = "al",
                    explanation = "a + el = al. Voy al banco — я иду в банк."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на русский",
                    question = "¿Qué vas a hacer mañana?",
                    options = listOf("Что ты будешь делать завтра?", "Что ты делал вчера?", "Что ты делаешь сейчас?", "Что ты хочешь делать?"),
                    correctAnswer = "Что ты будешь делать завтра?",
                    explanation = "vas a hacer = собираешься делать (ближайшее будущее). mañana = завтра."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать о будущем?",
                    question = "Завтра будет дождь.",
                    options = listOf("Va a llover mañana.", "Voy a llover mañana.", "Llueve mañana.", "Va llover mañana."),
                    correctAnswer = "Va a llover mañana.",
                    explanation = "va a llover = будет дождить. IR A + infinitivo = ближайшее будущее."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный вариант",
                    question = "Я иду в магазин. Voy ___",
                    options = listOf("a la tienda.", "al tienda.", "en la tienda.", "a el tienda."),
                    correctAnswer = "a la tienda.",
                    explanation = "tienda — женский род, поэтому a la tienda (не al)."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Мне нравится кофе. Me ___ el café.",
                    options = listOf("gusta", "gustan", "gusto", "gustas"),
                    correctAnswer = "gusta",
                    explanation = "el café — единственное число → gusta. Глагол согласуется с тем, что нравится."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Мне нравятся кошки. Me ___ los gatos.",
                    options = listOf("gustan", "gusta", "gusto", "gustán"),
                    correctAnswer = "gustan",
                    explanation = "los gatos — множественное число → gustan."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на испанский",
                    question = "Тебе нравится музыка?",
                    options = listOf("¿Te gusta la música?", "¿Me gusta la música?", "¿Te gustan la música?", "¿Le gusta la música?"),
                    correctAnswer = "¿Te gusta la música?",
                    explanation = "te = тебе. la música — ед.ч. → gusta."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на русский",
                    question = "A él le gusta el fútbol.",
                    options = listOf("Ему нравится футбол.", "Он любит футбол.", "Он играет в футбол.", "Он смотрит футбол."),
                    correctAnswer = "Ему нравится футбол.",
                    explanation = "le gusta = ему/ей нравится. a él — уточнение что именно ему."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на русский",
                    question = "¡Me encanta España!",
                    options = listOf("Я обожаю Испанию!", "Мне нравится Испания.", "Я в Испании.", "Испания мне интересна."),
                    correctAnswer = "Я обожаю Испанию!",
                    explanation = "encantar = обожать, очень нравиться. Сильнее чем gustar."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "У меня болит голова. Me ___ la cabeza.",
                    options = listOf("duele", "duelen", "duelo", "dueles"),
                    correctAnswer = "duele",
                    explanation = "la cabeza — единственное число → duele. Me duele la cabeza."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "У меня болят ноги. Me ___ los pies.",
                    options = listOf("duelen", "duele", "duelo", "dueles"),
                    correctAnswer = "duelen",
                    explanation = "los pies — множественное число → duelen. Me duelen los pies."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что означает?",
                    question = "Te queda bien.",
                    options = listOf("Тебе идёт (одежда).", "Тебе хватает.", "Ты остаёшься.", "Тебе нравится."),
                    correctAnswer = "Тебе идёт (одежда).",
                    explanation = "quedar bien = идти (об одежде). Te queda bien — тебе идёт эта вещь."
                )
            )
        ),

        // ══════════════════════════════════════════════
        //  A2 БЛОК 1: В ПРОШЛОМ  (unitId=5)
        // ══════════════════════════════════════════════

        // u5_l0 — Pretérito Indefinido: введение
        "u5_l0" to LessonContent(
            intro = "Pretérito Indefinido — прошедшее время для завершённых действий. Вчера, на прошлой неделе, в детстве — всё через него!",
            sections = listOf(
                LessonSection(
                    heading = "Что такое Pretérito Indefinido?",
                    items = listOf(
                        LessonItem("Завершённое действие", "Ayer comí pizza.", "вчера я съел пиццу"),
                        LessonItem("Конкретный момент", "El lunes llamé.", "в понедельник я позвонил"),
                        LessonItem("Маркеры времени", "ayer, anteayer, el año pasado", "вчера, позавчера, в прошлом году"),
                        LessonItem("también:", "la semana pasada, en 2020", "на прошлой неделе, в 2020-м")
                    )
                ),
                LessonSection(
                    heading = "Окончания -AR (hablar)",
                    items = listOf(
                        LessonItem("yo", "hablé", "я поговорил"),
                        LessonItem("tú", "hablaste", "ты поговорил"),
                        LessonItem("él / ella", "habló", "он поговорил"),
                        LessonItem("nosotros", "hablamos", "мы поговорили"),
                        LessonItem("vosotros", "hablasteis", "вы поговорили"),
                        LessonItem("ellos", "hablaron", "они поговорили")
                    )
                ),
                LessonSection(
                    heading = "Первые фразы",
                    items = listOf(
                        LessonItem("Ayer llamé a mi madre.", "Вчера я позвонил маме.", ""),
                        LessonItem("¿Llegaste tarde?", "Ты опоздал?", ""),
                        LessonItem("Viajamos a España.", "Мы путешествовали в Испанию.", ""),
                        LessonItem("¿Cuándo llegaste?", "Когда ты приехал?", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Вчера я позвонил маме. Ayer ___ a mi madre.",
                    options = listOf("llamé", "llamo", "llamaba", "llamar"),
                    correctAnswer = "llamé",
                    explanation = "yo + -AR в P.Indefinido → -é: llamar → llamé. Тильда на é обязательна!"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на русский",
                    question = "¿Llegaste tarde?",
                    options = listOf("Ты опоздал?", "Ты опаздываешь?", "Ты опоздаешь?", "Ты часто опаздываешь?"),
                    correctAnswer = "Ты опоздал?",
                    explanation = "llegaste = ты прибыл/опоздал. tú + llegar → llegaste (P.Indefinido)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Какой маркер P.Indefinido?",
                    question = "Какое слово указывает на P.Indefinido?",
                    options = listOf("ayer (вчера)", "ahora (сейчас)", "siempre (всегда)", "mañana (завтра)"),
                    correctAnswer = "ayer (вчера)",
                    explanation = "ayer, la semana pasada, el año pasado → P.Indefinido (завершённое прошлое)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Мы поехали в Испанию. ___ a España.",
                    options = listOf("Viajamos", "Viajé", "Viajaste", "Viajaron"),
                    correctAnswer = "Viajamos",
                    explanation = "nosotros + viajar → viajamos. Внимание: форма nosotros совпадает с настоящим временем!"
                )
            )
        ),

        // u5_l1 — Regulares -AR
        "u5_l1" to LessonContent(
            intro = "Правильные глаголы -AR в Pretérito Indefinido. Запомни шесть окончаний — и сможешь рассказать о прошлом с сотнями глаголов!",
            sections = listOf(
                LessonSection(
                    heading = "Окончания -AR (hablar)",
                    items = listOf(
                        LessonItem("yo", "hablé", ""),
                        LessonItem("tú", "hablaste", ""),
                        LessonItem("él / ella / Ud.", "habló", ""),
                        LessonItem("nosotros", "hablamos", ""),
                        LessonItem("vosotros", "hablasteis", ""),
                        LessonItem("ellos / Uds.", "hablaron", "")
                    )
                ),
                LessonSection(
                    heading = "Примеры: trabajar, comprar, llamar",
                    items = listOf(
                        LessonItem("Trabajé mucho ayer.", "Я много работал вчера.", ""),
                        LessonItem("¿Compraste el libro?", "Ты купил книгу?", ""),
                        LessonItem("Ella llamó a las 8.", "Она позвонила в 8.", ""),
                        LessonItem("Llegaron tarde.", "Они опоздали.", "")
                    )
                ),
                LessonSection(
                    heading = "Частые -AR глаголы в прошлом",
                    items = listOf(
                        LessonItem("hablar → hablé", "поговорил", ""),
                        LessonItem("trabajar → trabajé", "поработал", ""),
                        LessonItem("comprar → compré", "купил", ""),
                        LessonItem("llamar → llamé", "позвонил", ""),
                        LessonItem("llegar → llegué", "прибыл", "g→gu перед e"),
                        LessonItem("empezar → empecé", "начал", "z→c перед e")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Он купил машину. Él ___ un coche.",
                    options = listOf("compró", "compré", "compraste", "compramos"),
                    correctAnswer = "compró",
                    explanation = "él + comprar → compró. Тильда на -ó для él/ella обязательна!"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на испанский",
                    question = "Они опоздали.",
                    options = listOf("Llegaron tarde.", "Llegó tarde.", "Llegaste tarde.", "Llegamos tarde."),
                    correctAnswer = "Llegaron tarde.",
                    explanation = "ellos + llegar → llegaron. -AR: ellos → -aron."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Я начал учить испанский. ___ a estudiar español.",
                    options = listOf("Empecé", "Empezé", "Empezó", "Empecemos"),
                    correctAnswer = "Empecé",
                    explanation = "empezar → empecé (z→c перед e). yo → -é."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на испанский",
                    question = "Ты много работал вчера?",
                    options = listOf("¿Trabajaste mucho ayer?", "¿Trabajé mucho ayer?", "¿Trabajó mucho ayer?", "¿Trabajabas mucho ayer?"),
                    correctAnswer = "¿Trabajaste mucho ayer?",
                    explanation = "tú + trabajar → trabajaste. ayer = вчера."
                )
            )
        ),

        // u5_l2 — Regulares -ER/-IR
        "u5_l2" to LessonContent(
            intro = "Глаголы -ER и -IR в Pretérito Indefinido имеют одинаковые окончания! Выучи одну таблицу для двух групп.",
            sections = listOf(
                LessonSection(
                    heading = "Окончания -ER/-IR (одинаковые!)",
                    items = listOf(
                        LessonItem("yo", "-í", "comí, viví"),
                        LessonItem("tú", "-iste", "comiste, viviste"),
                        LessonItem("él / ella", "-ió", "comió, vivió"),
                        LessonItem("nosotros", "-imos", "comimos, vivimos"),
                        LessonItem("vosotros", "-isteis", "comisteis, vivisteis"),
                        LessonItem("ellos", "-ieron", "comieron, vivieron")
                    )
                ),
                LessonSection(
                    heading = "Примеры",
                    items = listOf(
                        LessonItem("Comí paella ayer.", "Я ел паэлью вчера.", ""),
                        LessonItem("¿Bebiste vino?", "Ты пил вино?", ""),
                        LessonItem("Ella vivió en Madrid.", "Она жила в Мадриде.", ""),
                        LessonItem("Escribieron una carta.", "Они написали письмо.", "")
                    )
                ),
                LessonSection(
                    heading = "Частые -ER/-IR глаголы",
                    items = listOf(
                        LessonItem("comer → comí", "поел", ""),
                        LessonItem("beber → bebí", "выпил", ""),
                        LessonItem("vivir → viví", "жил", ""),
                        LessonItem("escribir → escribí", "написал", ""),
                        LessonItem("salir → salí", "вышел", ""),
                        LessonItem("recibir → recibí", "получил", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Я ел паэлью. ___ paella.",
                    options = listOf("Comí", "Como", "Comía", "Comer"),
                    correctAnswer = "Comí",
                    explanation = "yo + comer → comí. -ER/-IR: yo → -í (с тильдой)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Они написали письмо. ___ una carta.",
                    options = listOf("Escribieron", "Escribió", "Escribí", "Escribimos"),
                    correctAnswer = "Escribieron",
                    explanation = "ellos + escribir → escribieron. -ER/-IR: ellos → -ieron."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на испанский",
                    question = "Ты выпил кофе?",
                    options = listOf("¿Bebiste café?", "¿Bebí café?", "¿Bebió café?", "¿Bebías café?"),
                    correctAnswer = "¿Bebiste café?",
                    explanation = "tú + beber → bebiste. -ER: tú → -iste."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что общего у -ER и -IR?",
                    question = "Окончания -ER и -IR в P.Indefinido...",
                    options = listOf("Одинаковые для всех лиц", "Разные для yo", "Разные для nosotros", "Полностью разные"),
                    correctAnswer = "Одинаковые для всех лиц",
                    explanation = "comí = viví, comiste = viviste и т.д. -ER и -IR имеют одинаковые окончания в P.Indefinido!"
                )
            )
        ),

        // u5_l3 — SER vs ESTAR: ключевые различия
        "u5_l3" to LessonContent(
            intro = "SER vs ESTAR — главная головоломка испанского. Разберём все случаи с примерами раз и навсегда!",
            sections = listOf(
                LessonSection(
                    heading = "SER — постоянное, суть",
                    items = listOf(
                        LessonItem("Личность и характер", "Es simpático.", "он добрый (всегда)"),
                        LessonItem("Профессия", "Es médico.", "он врач"),
                        LessonItem("Происхождение", "Es de Rusia.", "он из России"),
                        LessonItem("Материал", "Es de madera.", "это деревянное"),
                        LessonItem("Время / дата", "Son las tres.", "три часа"),
                        LessonItem("Пассивный залог", "Fue construido en 1800.", "")
                    )
                ),
                LessonSection(
                    heading = "ESTAR — временное, состояние",
                    items = listOf(
                        LessonItem("Местонахождение", "Está en casa.", "он дома"),
                        LessonItem("Временное состояние", "Está cansado.", "он устал (сейчас)"),
                        LessonItem("Результат действия", "La puerta está abierta.", "дверь открыта"),
                        LessonItem("Gerundio", "Estoy comiendo.", "я ем (сейчас)"),
                        LessonItem("Настроение", "Está de buen humor.", "он в хорошем настроении")
                    )
                ),
                LessonSection(
                    heading = "Пары с разным значением",
                    items = listOf(
                        LessonItem("Es aburrido.", "Он скучный (характер).", "SER"),
                        LessonItem("Está aburrido.", "Ему скучно (сейчас).", "ESTAR"),
                        LessonItem("Es malo.", "Он плохой человек.", "SER"),
                        LessonItem("Está malo.", "Он болен.", "ESTAR"),
                        LessonItem("Es bueno.", "Он хороший.", "SER"),
                        LessonItem("Está bueno.", "Это вкусно.", "ESTAR")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "SER или ESTAR?",
                    question = "Он болен. Él ___ malo.",
                    options = listOf("está", "es", "estoy", "son"),
                    correctAnswer = "está",
                    explanation = "Está malo = он болен (временное). Es malo = он плохой (характер)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "SER или ESTAR?",
                    question = "Паэлья вкусная (сейчас). La paella ___ buena.",
                    options = listOf("está", "es", "estoy", "son"),
                    correctAnswer = "está",
                    explanation = "Está buena = вкусная (сейчас, на вкус). Es buena = хорошего качества (всегда)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "SER или ESTAR?",
                    question = "Он врач. Él ___ médico.",
                    options = listOf("es", "está", "son", "estoy"),
                    correctAnswer = "es",
                    explanation = "Профессия → SER. Es médico."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на испанский",
                    question = "Дверь открыта (результат).",
                    options = listOf("La puerta está abierta.", "La puerta es abierta.", "La puerta está abrir.", "La puerta es abierto."),
                    correctAnswer = "La puerta está abierta.",
                    explanation = "Результат действия → ESTAR. está abierta = открыта (кем-то открыли)."
                )
            )
        ),

        // u5_l6 — Irregulares: ir/ser → fui
        "u5_l6" to LessonContent(
            intro = "IR и SER в P.Indefinido совпадают полностью! Это самые нерегулярные глаголы — без тильд, без логики, просто запомни.",
            sections = listOf(
                LessonSection(
                    heading = "IR/SER в P.Indefinido (одинаковые!)",
                    items = listOf(
                        LessonItem("yo", "fui", "я пошёл / я был"),
                        LessonItem("tú", "fuiste", "ты пошёл / ты был"),
                        LessonItem("él / ella", "fue", "он пошёл / он был"),
                        LessonItem("nosotros", "fuimos", "мы пошли / мы были"),
                        LessonItem("vosotros", "fuisteis", "вы пошли / вы были"),
                        LessonItem("ellos", "fueron", "они пошли / они были")
                    )
                ),
                LessonSection(
                    heading = "Контекст подсказывает смысл",
                    items = listOf(
                        LessonItem("Fui al cine.", "Я пошёл в кино.", "IR — движение"),
                        LessonItem("Fue un gran día.", "Это был великий день.", "SER — характеристика"),
                        LessonItem("¿Fuiste a la fiesta?", "Ты ходил на вечеринку?", "IR"),
                        LessonItem("Fue muy difícil.", "Это было очень сложно.", "SER")
                    )
                ),
                LessonSection(
                    heading = "Внимание: нет тильды!",
                    items = listOf(
                        LessonItem("fui, fuiste, fue", "без тильды", "в отличие от regular глаголов"),
                        LessonItem("fuimos, fuisteis, fueron", "без тильды", ""),
                        LessonItem("Это исключение", "просто запомни", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Я ходил в кино. ___ al cine.",
                    options = listOf("Fui", "Fue", "Fuiste", "Fuimos"),
                    correctAnswer = "Fui",
                    explanation = "yo + IR/SER → fui. Без тильды!"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "IR или SER в этом предложении?",
                    question = "Fue un gran día.",
                    options = listOf("SER (это был великий день)", "IR (он пошёл в великий день)", "Оба варианта невозможны", "Трудно сказать без контекста"),
                    correctAnswer = "SER (это был великий день)",
                    explanation = "Fue un gran día — SER, характеристика дня. Контекст (un gran día) указывает на SER."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на испанский",
                    question = "Мы были в Мадриде.",
                    options = listOf("Fuimos a Madrid.", "Fueron a Madrid.", "Fuisteis a Madrid.", "Fue a Madrid."),
                    correctAnswer = "Fuimos a Madrid.",
                    explanation = "nosotros + IR/SER → fuimos."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Правда или нет?",
                    question = "IR и SER в P.Indefinido — разные формы.",
                    options = listOf("Неправда — формы одинаковые", "Правда — формы разные", "Только yo одинаково", "Только ellos одинаково"),
                    correctAnswer = "Неправда — формы одинаковые",
                    explanation = "fui, fuiste, fue, fuimos, fuisteis, fueron — одно для обоих глаголов!"
                )
            )
        ),

        // u5_l7 — Irregulares: tener → tuve, estar → estuve
        "u5_l7" to LessonContent(
            intro = "Группа нерегулярных с основой на -uv-: tener, estar, andar. Окончания без тильд — это важно!",
            sections = listOf(
                LessonSection(
                    heading = "TENER → tuv-",
                    items = listOf(
                        LessonItem("yo tuve", "у меня было", ""),
                        LessonItem("tú tuviste", "у тебя было", ""),
                        LessonItem("él tuvo", "у него было", ""),
                        LessonItem("nosotros tuvimos", "у нас было", ""),
                        LessonItem("ellos tuvieron", "у них было", "")
                    )
                ),
                LessonSection(
                    heading = "ESTAR → estuv-",
                    items = listOf(
                        LessonItem("yo estuve", "я был/находился", ""),
                        LessonItem("tú estuviste", "ты был", ""),
                        LessonItem("él estuvo", "он был", ""),
                        LessonItem("nosotros estuvimos", "мы были", ""),
                        LessonItem("ellos estuvieron", "они были", "")
                    )
                ),
                LessonSection(
                    heading = "Примеры",
                    items = listOf(
                        LessonItem("Tuve un problema.", "У меня была проблема.", ""),
                        LessonItem("¿Dónde estuviste?", "Где ты был?", ""),
                        LessonItem("Estuvo enfermo.", "Он был болен.", ""),
                        LessonItem("Tuvimos mucha suerte.", "Нам очень повезло.", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "У меня была проблема. ___ un problema.",
                    options = listOf("Tuve", "Tengo", "Tenía", "Tiene"),
                    correctAnswer = "Tuve",
                    explanation = "yo + tener → tuve (P.Indefinido). Основа tuv- + окончание без тильды."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на испанский",
                    question = "Где ты был?",
                    options = listOf("¿Dónde estuviste?", "¿Dónde estuve?", "¿Dónde estuvo?", "¿Dónde estabas?"),
                    correctAnswer = "¿Dónde estuviste?",
                    explanation = "tú + estar → estuviste. ¿Dónde estuviste? = где ты был?"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Они были в Барселоне. ___ en Barcelona.",
                    options = listOf("Estuvieron", "Estuvimos", "Estuvo", "Estuviste"),
                    correctAnswer = "Estuvieron",
                    explanation = "ellos + estar → estuvieron. -ieron = окончание для ellos."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Есть ли тильда?",
                    question = "yo + tener → tuv___",
                    options = listOf("tuve (без тильды)", "tuvé (с тильдой)", "túve (с тильдой)", "tuvê"),
                    correctAnswer = "tuve (без тильды)",
                    explanation = "Нерегулярные P.Indefinido не имеют тильды: tuve, tuviste, tuvo..."
                )
            )
        ),

        // u5_l8 — Irregulares: hacer → hice, querer → quise
        "u5_l8" to LessonContent(
            intro = "Ещё одна группа нерегулярных: hacer, querer, venir, poder. У каждого своя основа — запоминай парами.",
            sections = listOf(
                LessonSection(
                    heading = "HACER → hic-/hiz-",
                    items = listOf(
                        LessonItem("yo hice", "я сделал", ""),
                        LessonItem("tú hiciste", "ты сделал", ""),
                        LessonItem("él hizo", "он сделал", "c→z перед o"),
                        LessonItem("nosotros hicimos", "мы сделали", ""),
                        LessonItem("ellos hicieron", "они сделали", "")
                    )
                ),
                LessonSection(
                    heading = "QUERER → quis-",
                    items = listOf(
                        LessonItem("yo quise", "я хотел / я попытался", ""),
                        LessonItem("tú quisiste", "ты хотел", ""),
                        LessonItem("él quiso", "он хотел", ""),
                        LessonItem("ellos quisieron", "они хотели", "")
                    )
                ),
                LessonSection(
                    heading = "Другие нерегулярные",
                    items = listOf(
                        LessonItem("venir → vine", "пришёл", "yo vine"),
                        LessonItem("poder → pude", "смог", "yo pude"),
                        LessonItem("poner → puse", "положил", "yo puse"),
                        LessonItem("saber → supe", "узнал / знал", "yo supe")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Я сделал домашнюю работу. ___ los deberes.",
                    options = listOf("Hice", "Hizo", "Hacé", "Hacía"),
                    correctAnswer = "Hice",
                    explanation = "yo + hacer → hice. c сохраняется перед e и i."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Он не смог прийти. No ___ venir.",
                    options = listOf("pudo", "puede", "pude", "pudiste"),
                    correctAnswer = "pudo",
                    explanation = "él + poder → pudo (P.Indefinido). Основа pud-."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на испанский",
                    question = "Они пришли поздно.",
                    options = listOf("Vinieron tarde.", "Vinimos tarde.", "Vine tarde.", "Vino tarde."),
                    correctAnswer = "Vinieron tarde.",
                    explanation = "ellos + venir → vinieron."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что означает?",
                    question = "No quise ir.",
                    options = listOf("Я не захотел идти.", "Я не хочу идти.", "Я не мог идти.", "Я не пойду.", ),
                    correctAnswer = "Я не захотел идти.",
                    explanation = "quise = я хотел/попытался. No quise — я не захотел (и не пошёл)."
                )
            )
        ),

        // u5_l9 — Por vs Para
        "u5_l9" to LessonContent(
            intro = "Por и Para — оба переводятся «для/за/по/через», но используются в разных случаях. Запомни ключевые правила!",
            sections = listOf(
                LessonSection(
                    heading = "POR — причина, обмен, движение",
                    items = listOf(
                        LessonItem("Причина", "Lo hice por amor.", "сделал из-за любви"),
                        LessonItem("Благодарность / обмен", "Gracias por todo.", "спасибо за всё"),
                        LessonItem("Цена", "Lo compré por 10€.", "купил за 10 евро"),
                        LessonItem("Движение сквозь/по", "Paseamos por el parque.", "гуляли по парку"),
                        LessonItem("Длительность", "Estudié por dos horas.", "учился в течение 2 часов"),
                        LessonItem("Средство", "Llama por teléfono.", "звони по телефону")
                    )
                ),
                LessonSection(
                    heading = "PARA — цель, назначение, получатель",
                    items = listOf(
                        LessonItem("Цель / для чего", "Estudio para aprender.", "учусь чтобы выучить"),
                        LessonItem("Получатель", "Es para ti.", "это для тебя"),
                        LessonItem("Дедлайн", "Para el lunes.", "к понедельнику"),
                        LessonItem("Направление", "Salgo para Madrid.", "уезжаю в Мадрид"),
                        LessonItem("Мнение", "Para mí, es fácil.", "по-моему, это легко")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "POR или PARA?",
                    question = "Это для тебя. Es ___ ti.",
                    options = listOf("para", "por", "a", "de"),
                    correctAnswer = "para",
                    explanation = "para = для (получатель). Es para ti — это для тебя."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "POR или PARA?",
                    question = "Спасибо за всё. Gracias ___ todo.",
                    options = listOf("por", "para", "de", "a"),
                    correctAnswer = "por",
                    explanation = "por = за (благодарность/обмен). Gracias por todo."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "POR или PARA?",
                    question = "Я учусь чтобы найти работу. Estudio ___ encontrar trabajo.",
                    options = listOf("para", "por", "que", "con"),
                    correctAnswer = "para",
                    explanation = "para + infinitivo = цель действия. Estudio para encontrar trabajo."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи фразу",
                    question = "Paseamos por el parque.",
                    options = listOf("Мы гуляли по парку.", "Мы шли для парка.", "Мы гуляли ради парка.", "Мы прошли мимо парка."),
                    correctAnswer = "Мы гуляли по парку.",
                    explanation = "por = по (движение по территории). Paseamos por el parque — гуляли по парку."
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
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Я встаю в 7 утра. ___ a las 7.",
                    options = listOf("Me levanto", "Te levantas", "Se levanta", "Nos levantamos"),
                    correctAnswer = "Me levanto",
                    explanation = "me + levanto = я встаю. Возвратное местоимение me идёт перед глаголом."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на русский",
                    question = "¿Cómo te llamas?",
                    options = listOf("Как тебя зовут?", "Как ты себя чувствуешь?", "Куда ты идёшь?", "Как ты учишься?"),
                    correctAnswer = "Как тебя зовут?",
                    explanation = "llamarse = называться. ¿Cómo te llamas? — как тебя зовут? Me llamo... — меня зовут..."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный вариант",
                    question = "Она ложится спать поздно. ___ tarde.",
                    options = listOf("Se acuesta", "Me acuesto", "Te acuestas", "Nos acostamos"),
                    correctAnswer = "Se acuesta",
                    explanation = "se acuesta = он/она ложится спать. se = возвратное местоимение для él/ella."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать?",
                    question = "Я ухожу.",
                    options = listOf("Me voy.", "Se va.", "Te vas.", "Nos vamos."),
                    correctAnswer = "Me voy.",
                    explanation = "irse = уходить. Me voy — я ухожу. Очень частая фраза!"
                )
            )
        ),

        // ══════════════════════════════════════════════
        //  A1 БЛОК 1 — vocab-уроки
        // ══════════════════════════════════════════════

        // u1_l4 — Приветствия
        "u1_l4" to LessonContent(
            intro = "Как здороваться по-испански в любое время суток",
            sections = listOf(
                LessonSection(
                    heading = "Приветствия",
                    items = listOf(
                        LessonItem("¡Hola!", "Привет!", "универсально"),
                        LessonItem("Buenos días", "Доброе утро", "до 12:00"),
                        LessonItem("Buenas tardes", "Добрый день/вечер", "12:00–20:00"),
                        LessonItem("Buenas noches", "Добрый вечер/ночи", "после 20:00"),
                        LessonItem("¡Buenas!", "Привет! (коротко)", "разговорное")
                    )
                ),
                LessonSection(
                    heading = "Как дела?",
                    items = listOf(
                        LessonItem("¿Cómo estás?", "Как ты?", "неформально"),
                        LessonItem("¿Cómo está usted?", "Как вы?", "формально"),
                        LessonItem("(Muy) bien", "Хорошо / Очень хорошо", ""),
                        LessonItem("Regular", "Так себе / Нормально", ""),
                        LessonItem("Mal", "Плохо", ""),
                        LessonItem("¿Y tú?", "А ты?", "встречный вопрос")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Сейчас 9 утра. Как поздороваться?",
                    question = "— ___, señora García.",
                    options = listOf("Buenos días", "Buenas noches", "Buenas tardes", "Hasta luego"),
                    correctAnswer = "Buenos días",
                    explanation = "Buenos días = доброе утро (до 12:00)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи на русский",
                    question = "¿Cómo estás?",
                    options = listOf("Как ты?", "Кто ты?", "Где ты?", "Что ты делаешь?"),
                    correctAnswer = "Как ты?",
                    explanation = "¿Cómo estás? — как ты? (неформально). ¿Cómo está usted? — формально."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный ответ",
                    question = "— ¿Cómo estás? — ___, gracias.",
                    options = listOf("Muy bien", "Buenos días", "Hola", "Hasta luego"),
                    correctAnswer = "Muy bien",
                    explanation = "Muy bien = очень хорошо. Это стандартный ответ на ¿Cómo estás?"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что значит «¡Buenas!»?",
                    question = "¡Buenas! — это...",
                    options = listOf("Разговорное «привет»", "До свидания", "Спокойной ночи", "Доброе утро"),
                    correctAnswer = "Разговорное «привет»",
                    explanation = "¡Buenas! — короткое приветствие, используется в любое время суток."
                )
            )
        ),

        // u1_l5 — Прощания
        "u1_l5" to LessonContent(
            intro = "Как прощаться по-испански",
            sections = listOf(
                LessonSection(
                    heading = "Прощания",
                    items = listOf(
                        LessonItem("Adiós", "Пока / До свидания", "финальное прощание"),
                        LessonItem("Hasta luego", "До встречи / Пока", "скоро увидимся"),
                        LessonItem("Hasta mañana", "До завтра", ""),
                        LessonItem("Hasta pronto", "До скорого", ""),
                        LessonItem("Nos vemos", "Увидимся", "разговорное"),
                        LessonItem("Hasta el lunes", "До понедельника", "конкретный день")
                    )
                ),
                LessonSection(
                    heading = "Пожелания на прощание",
                    items = listOf(
                        LessonItem("Buenas noches", "Спокойной ночи", "уходя вечером"),
                        LessonItem("Que te vaya bien", "Всего хорошего", ""),
                        LessonItem("Cuídate", "Береги себя", ""),
                        LessonItem("Un abrazo", "Обнимаю", "в конце письма/чата")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «до завтра»?",
                    question = "— ¡___!",
                    options = listOf("Hasta mañana", "Hasta luego", "Adiós", "Nos vemos"),
                    correctAnswer = "Hasta mañana",
                    explanation = "Hasta mañana = до завтра. mañana = завтра."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Nos vemos",
                    options = listOf("Увидимся", "Мы видим", "Смотри на нас", "Пока навсегда"),
                    correctAnswer = "Увидимся",
                    explanation = "Nos vemos — буквально «мы видимся», разговорное «увидимся»."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Конец рабочего дня, уходишь домой. Что скажешь?",
                    question = "— ¡___, hasta mañana!",
                    options = listOf("Adiós", "Hola", "Buenos días", "¿Cómo estás?"),
                    correctAnswer = "Adiós",
                    explanation = "Adiós — до свидания. В паре с hasta mañana = пока, до завтра!"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «береги себя»?",
                    question = "— ¡___!",
                    options = listOf("Cuídate", "Hasta pronto", "Nos vemos", "Un abrazo"),
                    correctAnswer = "Cuídate",
                    explanation = "Cuídate = береги себя (от cuidar = заботиться, беречь)."
                )
            )
        ),

        // u1_l6 — Вежливые слова
        "u1_l6" to LessonContent(
            intro = "Вежливые слова — основа общения",
            sections = listOf(
                LessonSection(
                    heading = "Вежливость",
                    items = listOf(
                        LessonItem("Por favor", "Пожалуйста (просьба)", "¿Agua, por favor?"),
                        LessonItem("Gracias", "Спасибо", "¡Muchas gracias! — большое спасибо"),
                        LessonItem("De nada", "Пожалуйста (ответ)", "ответ на gracias"),
                        LessonItem("Perdón / Perdona", "Прости / Извини", "небольшая ошибка"),
                        LessonItem("Lo siento", "Мне жаль / Извините", "серьёзное извинение"),
                        LessonItem("Disculpe", "Простите", "формально, незнакомцу")
                    )
                ),
                LessonSection(
                    heading = "Полезные фразы",
                    items = listOf(
                        LessonItem("¿Puede repetir?", "Можете повторить?", ""),
                        LessonItem("No entiendo", "Я не понимаю", ""),
                        LessonItem("Más despacio", "Помедленнее", ""),
                        LessonItem("¿Cómo se dice...?", "Как сказать...?", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Случайно толкнул человека. Что скажешь?",
                    question = "— ¡___!",
                    options = listOf("Perdón", "Gracias", "De nada", "Por favor"),
                    correctAnswer = "Perdón",
                    explanation = "Perdón = прости/извини. Для небольших ошибок и случайных ситуаций."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Тебя поблагодарили: «Gracias». Ответь.",
                    question = "— Gracias. — ___.",
                    options = listOf("De nada", "Por favor", "Lo siento", "Perdón"),
                    correctAnswer = "De nada",
                    explanation = "De nada = пожалуйста (в ответ на «спасибо»). Буквально — «не за что»."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как попросить говорить медленнее?",
                    question = "— ___, por favor.",
                    options = listOf("Más despacio", "Más rápido", "Perdón", "No entiendo"),
                    correctAnswer = "Más despacio",
                    explanation = "Más despacio = помедленнее. despacio = медленно, más = больше."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Lo siento mucho.",
                    options = listOf("Мне очень жаль.", "Я много слышу.", "Мне не жаль.", "Это приятно."),
                    correctAnswer = "Мне очень жаль.",
                    explanation = "Lo siento = мне жаль. mucho = очень. Используется для серьёзных извинений."
                )
            )
        ),

        // u1_l12 — Страны и национальности
        "u1_l12" to LessonContent(
            intro = "Страны и национальности на испанском",
            sections = listOf(
                LessonSection(
                    heading = "Страны",
                    items = listOf(
                        LessonItem("España", "Испания", ""),
                        LessonItem("Rusia", "Россия", ""),
                        LessonItem("México", "Мексика", ""),
                        LessonItem("Francia", "Франция", ""),
                        LessonItem("Alemania", "Германия", ""),
                        LessonItem("Italia", "Италия", ""),
                        LessonItem("Estados Unidos", "США", ""),
                        LessonItem("China", "Китай", "")
                    )
                ),
                LessonSection(
                    heading = "Национальности (м/ж)",
                    items = listOf(
                        LessonItem("español / española", "испанец / испанка", "Soy español."),
                        LessonItem("ruso / rusa", "русский / русская", "Soy rusa."),
                        LessonItem("mexicano / mexicana", "мексиканец / мексиканка", ""),
                        LessonItem("francés / francesa", "француз / француженка", ""),
                        LessonItem("alemán / alemana", "немец / немка", ""),
                        LessonItem("italiano / italiana", "итальянец / итальянка", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «я русский»?",
                    question = "Soy ___.",
                    options = listOf("ruso", "rusa", "russo", "Rusia"),
                    correctAnswer = "ruso",
                    explanation = "ruso = русский (муж.), rusa = русская (жен.). Национальности пишутся с маленькой буквы."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи: «Она испанка»",
                    question = "Ella es ___.",
                    options = listOf("española", "español", "España", "espanol"),
                    correctAnswer = "española",
                    explanation = "española = испанка (жен.). español = испанец (муж.)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как называется страна на испанском?",
                    question = "Германия = ___",
                    options = listOf("Alemania", "Alemana", "Germania", "Germán"),
                    correctAnswer = "Alemania",
                    explanation = "Alemania = Германия. alemán/alemana = немец/немка."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи фразу",
                    question = "Soy de Francia.",
                    options = listOf("Я из Франции.", "Я француз.", "Я во Франции.", "Я еду во Францию."),
                    correctAnswer = "Я из Франции.",
                    explanation = "ser de + страна = быть родом из. Soy de Rusia = я из России."
                )
            )
        ),

        // u1_l13 — Числа 0–10
        "u1_l13" to LessonContent(
            intro = "Первые числа — основа всего",
            sections = listOf(
                LessonSection(
                    heading = "Числа 0–10",
                    items = listOf(
                        LessonItem("0 — cero", "ноль", ""),
                        LessonItem("1 — uno / una", "один / одна", "un libro, una mesa"),
                        LessonItem("2 — dos", "два", ""),
                        LessonItem("3 — tres", "три", ""),
                        LessonItem("4 — cuatro", "четыре", ""),
                        LessonItem("5 — cinco", "пять", ""),
                        LessonItem("6 — seis", "шесть", ""),
                        LessonItem("7 — siete", "семь", ""),
                        LessonItem("8 — ocho", "восемь", ""),
                        LessonItem("9 — nueve", "девять", ""),
                        LessonItem("10 — diez", "десять", "")
                    )
                ),
                LessonSection(
                    heading = "В речи",
                    items = listOf(
                        LessonItem("Tengo dos hermanos.", "У меня два брата.", ""),
                        LessonItem("Son las tres.", "Сейчас три часа.", ""),
                        LessonItem("Cinco euros, por favor.", "Пять евро, пожалуйста.", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Какое число?",
                    question = "siete = ?",
                    options = listOf("7", "6", "8", "9"),
                    correctAnswer = "7",
                    explanation = "siete = семь (7)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «четыре»?",
                    question = "4 = ___",
                    options = listOf("cuatro", "cinco", "tres", "catorce"),
                    correctAnswer = "cuatro",
                    explanation = "cuatro = четыре. Запомни: cuatro — похоже на «кватро»."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Tengo tres gatos.",
                    options = listOf("У меня три кота.", "Я вижу три кота.", "Три кота едят.", "Нет кота."),
                    correctAnswer = "У меня три кота.",
                    explanation = "tener = иметь. Tengo = у меня есть. tres = три. gato = кот."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Сколько?",
                    question = "dos + tres = ___",
                    options = listOf("cinco", "seis", "cuatro", "siete"),
                    correctAnswer = "cinco",
                    explanation = "dos (2) + tres (3) = cinco (5)."
                )
            )
        ),

        // ══════════════════════════════════════════════
        //  A1 БЛОК 2 — vocab-уроки
        // ══════════════════════════════════════════════

        // u2_l0 — Числа 11–20
        "u2_l0" to LessonContent(
            intro = "Числа от 11 до 20",
            sections = listOf(
                LessonSection(
                    heading = "Числа 11–20",
                    items = listOf(
                        LessonItem("11 — once", "одиннадцать", ""),
                        LessonItem("12 — doce", "двенадцать", ""),
                        LessonItem("13 — trece", "тринадцать", ""),
                        LessonItem("14 — catorce", "четырнадцать", ""),
                        LessonItem("15 — quince", "пятнадцать", ""),
                        LessonItem("16 — dieciséis", "шестнадцать", "diez+y+seis"),
                        LessonItem("17 — diecisiete", "семнадцать", ""),
                        LessonItem("18 — dieciocho", "восемнадцать", ""),
                        LessonItem("19 — diecinueve", "девятнадцать", ""),
                        LessonItem("20 — veinte", "двадцать", "")
                    )
                ),
                LessonSection(
                    heading = "Правило 16–19",
                    items = listOf(
                        LessonItem("dieci- + число", "16–19 пишутся слитно", "dieciséis, diecisiete..."),
                        LessonItem("dieciséis", "имеет акцент на é", "не dieciseis!")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Какое число?",
                    question = "quince = ?",
                    options = listOf("15", "14", "16", "50"),
                    correctAnswer = "15",
                    explanation = "quince = пятнадцать (15). Запомни: квинс."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как написать 18?",
                    question = "18 = ___",
                    options = listOf("dieciocho", "diecocho", "diez y ocho", "ochodiez"),
                    correctAnswer = "dieciocho",
                    explanation = "dieciocho — пишется слитно. 16–19 объединяются с приставкой dieci-."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Tengo doce años.",
                    options = listOf("Мне двенадцать лет.", "У меня двенадцать.", "Я двадцать лет.", "Мне двадцать два."),
                    correctAnswer = "Мне двенадцать лет.",
                    explanation = "doce = двенадцать. tener ... años = быть ... лет (буквально «иметь лет»)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что больше?",
                    question = "dieciséis или catorce?",
                    options = listOf("dieciséis (16)", "catorce (14)", "Они равны", "Не могу определить"),
                    correctAnswer = "dieciséis (16)",
                    explanation = "dieciséis = 16, catorce = 14. 16 > 14."
                )
            )
        ),

        // u2_l1 — Числа 21–100
        "u2_l1" to LessonContent(
            intro = "Числа от 21 до 100",
            sections = listOf(
                LessonSection(
                    heading = "Десятки",
                    items = listOf(
                        LessonItem("20 — veinte", "двадцать", ""),
                        LessonItem("30 — treinta", "тридцать", ""),
                        LessonItem("40 — cuarenta", "сорок", ""),
                        LessonItem("50 — cincuenta", "пятьдесят", ""),
                        LessonItem("60 — sesenta", "шестьдесят", ""),
                        LessonItem("70 — setenta", "семьдесят", ""),
                        LessonItem("80 — ochenta", "восемьдесят", ""),
                        LessonItem("90 — noventa", "девяносто", ""),
                        LessonItem("100 — cien", "сто", "")
                    )
                ),
                LessonSection(
                    heading = "Составные числа 21–29 и 31+",
                    items = listOf(
                        LessonItem("21 — veintiuno", "двадцать один", "слитно!"),
                        LessonItem("22 — veintidós", "двадцать два", "слитно!"),
                        LessonItem("31 — treinta y uno", "тридцать один", "через y"),
                        LessonItem("45 — cuarenta y cinco", "сорок пять", "через y"),
                        LessonItem("99 — noventa y nueve", "девяносто девять", "через y")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как написать 50?",
                    question = "50 = ___",
                    options = listOf("cincuenta", "cincodiez", "cinquenta", "cincenta"),
                    correctAnswer = "cincuenta",
                    explanation = "cincuenta = пятьдесят. Запомни написание: cinc-u-enta."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "treinta y dos",
                    options = listOf("32", "30", "22", "23"),
                    correctAnswer = "32",
                    explanation = "treinta (30) + y + dos (2) = 32. Числа 31–99 (кроме 21–29) строятся через y."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как правильно написать 25?",
                    question = "25 = ___",
                    options = listOf("veinticinco", "veinte y cinco", "veintecinco", "veinticincos"),
                    correctAnswer = "veinticinco",
                    explanation = "21–29 пишутся слитно: veintiuno, veintidós, veintitrés... veinticinco."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Сколько лет?",
                    question = "Tengo cuarenta y tres años.",
                    options = listOf("43 года", "40 лет", "34 года", "53 года"),
                    correctAnswer = "43 года",
                    explanation = "cuarenta (40) + y + tres (3) = 43."
                )
            )
        ),

        // u2_l4 — Семья 1
        "u2_l4" to LessonContent(
            intro = "Ближайшие родственники",
            sections = listOf(
                LessonSection(
                    heading = "Семья",
                    items = listOf(
                        LessonItem("el padre", "отец", "Mi padre se llama Carlos."),
                        LessonItem("la madre", "мать", "Mi madre trabaja."),
                        LessonItem("los padres", "родители", "Mis padres son simpáticos."),
                        LessonItem("el hermano", "брат", "Tengo un hermano."),
                        LessonItem("la hermana", "сестра", "Mi hermana tiene 10 años."),
                        LessonItem("el hijo", "сын", "Tengo dos hijos."),
                        LessonItem("la hija", "дочь", "Mi hija se llama Sofía."),
                        LessonItem("los hijos", "дети", "¿Tienes hijos?")
                    )
                ),
                LessonSection(
                    heading = "Семейное положение",
                    items = listOf(
                        LessonItem("el marido / el esposo", "муж", ""),
                        LessonItem("la mujer / la esposa", "жена", ""),
                        LessonItem("soltero/a", "холост / не замужем", ""),
                        LessonItem("casado/a", "женат / замужем", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «у меня есть брат»?",
                    question = "Tengo un ___.",
                    options = listOf("hermano", "hermana", "hijo", "padre"),
                    correctAnswer = "hermano",
                    explanation = "hermano = брат, hermana = сестра."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи: «мои родители»",
                    question = "___ padres",
                    options = listOf("Mis", "Mi", "Sus", "Tu"),
                    correctAnswer = "Mis",
                    explanation = "mis = мои (мн.ч.), mi = мой/моя (ед.ч.). padres = родители → mis padres."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что значит «los hijos»?",
                    question = "los hijos = ?",
                    options = listOf("дети / сыновья", "сын", "дочери", "братья"),
                    correctAnswer = "дети / сыновья",
                    explanation = "hijo = сын. los hijos = сыновья или дети (в общем смысле)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи вопрос",
                    question = "¿Tienes hermanos?",
                    options = listOf("У тебя есть братья/сёстры?", "Сколько лет твоему брату?", "Где твои братья?", "Ты любишь брата?"),
                    correctAnswer = "У тебя есть братья/сёстры?",
                    explanation = "¿Tienes...? = у тебя есть...? hermanos = братья (или братья и сёстры вместе)."
                )
            )
        ),

        // u2_l5 — Семья 2
        "u2_l5" to LessonContent(
            intro = "Расширенная семья: бабушки, дяди, кузены",
            sections = listOf(
                LessonSection(
                    heading = "Расширенная семья",
                    items = listOf(
                        LessonItem("el abuelo / la abuela", "дедушка / бабушка", "los abuelos = бабушка и дедушка"),
                        LessonItem("el tío / la tía", "дядя / тётя", "los tíos = тёти и дяди"),
                        LessonItem("el primo / la prima", "двоюродный брат/сестра", "mis primos = мои кузены"),
                        LessonItem("el sobrino / la sobrina", "племянник / племянница", ""),
                        LessonItem("el nieto / la nieta", "внук / внучка", "")
                    )
                ),
                LessonSection(
                    heading = "Описание семьи",
                    items = listOf(
                        LessonItem("grande", "большая (семья)", "Tengo una familia grande."),
                        LessonItem("pequeña", "маленькая", "Mi familia es pequeña."),
                        LessonItem("unida", "дружная", "Somos una familia unida."),
                        LessonItem("¿Cómo es tu familia?", "Какая у тебя семья?", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Кто такой «el abuelo»?",
                    question = "el abuelo = ?",
                    options = listOf("дедушка", "дядя", "племянник", "отец"),
                    correctAnswer = "дедушка",
                    explanation = "el abuelo = дедушка. la abuela = бабушка. los abuelos = бабушка с дедушкой."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «племянница»?",
                    question = "племянница = ___",
                    options = listOf("la sobrina", "la prima", "la nieta", "la tía"),
                    correctAnswer = "la sobrina",
                    explanation = "sobrina = племянница (жен.). sobrino = племянник (муж.)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Mis primos viven en Madrid.",
                    options = listOf("Мои кузены живут в Мадриде.", "Мои родители в Мадриде.", "Мои друзья живут.", "Мой брат в Мадриде."),
                    correctAnswer = "Мои кузены живут в Мадриде.",
                    explanation = "primos = кузены (двоюродные братья/сёстры). vivir = жить."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «у меня большая семья»?",
                    question = "Tengo una familia ___.",
                    options = listOf("grande", "pequeña", "unida", "nueva"),
                    correctAnswer = "grande",
                    explanation = "grande = большой/большая. pequeña = маленькая. Tengo una familia grande = у меня большая семья."
                )
            )
        ),

        // u2_l7 — Цвета
        "u2_l7" to LessonContent(
            intro = "Цвета на испанском — и их согласование",
            sections = listOf(
                LessonSection(
                    heading = "Основные цвета",
                    items = listOf(
                        LessonItem("rojo / roja", "красный / красная", "un coche rojo"),
                        LessonItem("azul", "синий / голубой", "не меняется по роду"),
                        LessonItem("verde", "зелёный", "не меняется по роду"),
                        LessonItem("amarillo / amarilla", "жёлтый / жёлтая", ""),
                        LessonItem("blanco / blanca", "белый / белая", ""),
                        LessonItem("negro / negra", "чёрный / чёрная", ""),
                        LessonItem("naranja", "оранжевый", "не меняется"),
                        LessonItem("rosa", "розовый", "не меняется"),
                        LessonItem("morado / morada", "фиолетовый", ""),
                        LessonItem("gris", "серый", "не меняется")
                    )
                ),
                LessonSection(
                    heading = "Правило согласования",
                    items = listOf(
                        LessonItem("-o/-a меняются", "rojo→roja, blanco→blanca", ""),
                        LessonItem("azul/verde/naranja", "одинаковы для м. и ж.", "un gato verde / una casa verde")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи: «красная машина»",
                    question = "un coche ___",
                    options = listOf("rojo", "roja", "roja coche", "rojо"),
                    correctAnswer = "rojo",
                    explanation = "coche (машина) — мужского рода → rojo (не roja). El coche es rojo."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Какой цвет не меняется по роду?",
                    question = "Выбери цвет, который одинаков для м. и ж. рода",
                    options = listOf("azul", "rojo", "blanco", "amarillo"),
                    correctAnswer = "azul",
                    explanation = "azul, verde, naranja, rosa, gris — не меняются. un libro azul / una silla azul."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «белая рубашка»?",
                    question = "una camisa ___",
                    options = listOf("blanca", "blanco", "blanc", "blancas"),
                    correctAnswer = "blanca",
                    explanation = "camisa (рубашка) — женского рода → blanca."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "El cielo es azul.",
                    options = listOf("Небо голубое.", "Небо зелёное.", "Море синее.", "Небо серое."),
                    correctAnswer = "Небо голубое.",
                    explanation = "el cielo = небо, azul = синий/голубой."
                )
            )
        ),

        // u2_l11 — Дом
        "u2_l11" to LessonContent(
            intro = "Комнаты и части дома",
            sections = listOf(
                LessonSection(
                    heading = "Комнаты",
                    items = listOf(
                        LessonItem("la sala / el salón", "гостиная", ""),
                        LessonItem("la cocina", "кухня", ""),
                        LessonItem("el dormitorio", "спальня", ""),
                        LessonItem("el baño", "ванная/туалет", ""),
                        LessonItem("el comedor", "столовая", ""),
                        LessonItem("el pasillo", "коридор", ""),
                        LessonItem("el jardín", "сад", ""),
                        LessonItem("el garaje", "гараж", ""),
                        LessonItem("la terraza", "терраса", "")
                    )
                ),
                LessonSection(
                    heading = "Типы жилья",
                    items = listOf(
                        LessonItem("el piso", "квартира", "Vivo en un piso."),
                        LessonItem("la casa", "дом", "Tengo una casa grande."),
                        LessonItem("el estudio", "студия (квартира)", ""),
                        LessonItem("el edificio", "здание / дом (здание)", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Где готовят еду?",
                    question = "Cocino en ___.",
                    options = listOf("la cocina", "el baño", "el dormitorio", "el salón"),
                    correctAnswer = "la cocina",
                    explanation = "la cocina = кухня. cocinar = готовить. cocino = я готовлю."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Mi dormitorio es pequeño.",
                    options = listOf("Моя спальня маленькая.", "Моя кухня маленькая.", "Мой дом маленький.", "Моя квартира маленькая."),
                    correctAnswer = "Моя спальня маленькая.",
                    explanation = "dormitorio = спальня. pequeño = маленький."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «квартира»?",
                    question = "квартира = ___",
                    options = listOf("el piso", "la casa", "el edificio", "el estudio"),
                    correctAnswer = "el piso",
                    explanation = "el piso = квартира (в Испании). la casa = дом (отдельный). В Латинской Америке говорят el departamento."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Где принимают душ?",
                    question = "Me ducho en ___.",
                    options = listOf("el baño", "la cocina", "el comedor", "el garaje"),
                    correctAnswer = "el baño",
                    explanation = "el baño = ванная/туалет. ducharse = принимать душ."
                )
            )
        ),

        // u2_l12 — Мебель
        "u2_l12" to LessonContent(
            intro = "Мебель и предметы интерьера",
            sections = listOf(
                LessonSection(
                    heading = "Мебель",
                    items = listOf(
                        LessonItem("el sofá", "диван", "El sofá es cómodo."),
                        LessonItem("la mesa", "стол", "La mesa es grande."),
                        LessonItem("la silla", "стул", "Hay cuatro sillas."),
                        LessonItem("la cama", "кровать", "Mi cama es cómoda."),
                        LessonItem("el armario", "шкаф", ""),
                        LessonItem("la estantería", "книжная полка", ""),
                        LessonItem("la lámpara", "лампа", ""),
                        LessonItem("la alfombra", "ковёр", "")
                    )
                ),
                LessonSection(
                    heading = "Глагол HAY (есть / имеется)",
                    items = listOf(
                        LessonItem("Hay una mesa.", "Есть стол.", ""),
                        LessonItem("Hay dos sillas.", "Есть два стула.", ""),
                        LessonItem("No hay armario.", "Нет шкафа.", ""),
                        LessonItem("¿Hay jardín?", "Есть сад?", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «в комнате есть диван»?",
                    question = "En el salón ___ un sofá.",
                    options = listOf("hay", "es", "está", "tiene"),
                    correctAnswer = "hay",
                    explanation = "hay = есть/имеется (безличная форма глагола haber). Hay una mesa — есть стол."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "No hay sillas.",
                    options = listOf("Нет стульев.", "Нет дивана.", "Нет стола.", "Нет кровати."),
                    correctAnswer = "Нет стульев.",
                    explanation = "silla = стул. No hay = нет (отрицание hay)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что такое «la cama»?",
                    question = "la cama = ?",
                    options = listOf("кровать", "стул", "диван", "шкаф"),
                    correctAnswer = "кровать",
                    explanation = "la cama = кровать. Me acuesto en la cama = я ложусь спать на кровать."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "¿Hay armario en tu dormitorio?",
                    options = listOf("В твоей спальне есть шкаф?", "Где твой шкаф?", "У тебя есть кровать?", "Какой у тебя диван?"),
                    correctAnswer = "В твоей спальне есть шкаф?",
                    explanation = "¿Hay...? = есть ли...? armario = шкаф. dormitorio = спальня."
                )
            )
        ),

        // ══════════════════════════════════════════════
        //  A1 БЛОК 3 — vocab-уроки
        // ══════════════════════════════════════════════

        // u3_l4 — Еда
        "u3_l4" to LessonContent(
            intro = "Продукты питания и еда",
            sections = listOf(
                LessonSection(
                    heading = "Продукты",
                    items = listOf(
                        LessonItem("el pan", "хлеб", ""),
                        LessonItem("la leche", "молоко", ""),
                        LessonItem("el agua (f.)", "вода", "el agua — исключение!"),
                        LessonItem("el café", "кофе", ""),
                        LessonItem("la fruta", "фрукты", ""),
                        LessonItem("la carne", "мясо", ""),
                        LessonItem("el pescado", "рыба", ""),
                        LessonItem("el arroz", "рис", ""),
                        LessonItem("la verdura", "овощи", ""),
                        LessonItem("el huevo", "яйцо", "")
                    )
                ),
                LessonSection(
                    heading = "Приёмы пищи",
                    items = listOf(
                        LessonItem("el desayuno", "завтрак", "desayunar = завтракать"),
                        LessonItem("el almuerzo / la comida", "обед", "comer = обедать"),
                        LessonItem("la cena", "ужин", "cenar = ужинать"),
                        LessonItem("tener hambre", "хотеть есть", "Tengo hambre."),
                        LessonItem("tener sed", "хотеть пить", "Tengo sed.")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Tengo hambre.",
                    options = listOf("Я хочу есть.", "Я хочу пить.", "Я сыт.", "Я устал."),
                    correctAnswer = "Я хочу есть.",
                    explanation = "tener hambre = хотеть есть (буквально «иметь голод»). Tengo hambre = я хочу есть."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что такое «el pescado»?",
                    question = "el pescado = ?",
                    options = listOf("рыба (еда)", "мясо", "рис", "хлеб"),
                    correctAnswer = "рыба (еда)",
                    explanation = "el pescado = рыба как блюдо/продукт. el pez = живая рыба в воде."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как называется ужин?",
                    question = "ужин = ___",
                    options = listOf("la cena", "el desayuno", "el almuerzo", "la comida"),
                    correctAnswer = "la cena",
                    explanation = "la cena = ужин. cenar = ужинать. Cenamos a las 9. = Мы ужинаем в 9."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Заполни пропуск",
                    question = "No como ___. Soy vegetariano.",
                    options = listOf("carne", "fruta", "arroz", "verdura"),
                    correctAnswer = "carne",
                    explanation = "carne = мясо. vegetariano = вегетарианец. No como carne = я не ем мясо."
                )
            )
        ),

        // u3_l5 — В ресторане
        "u3_l5" to LessonContent(
            intro = "Как заказать еду в ресторане",
            sections = listOf(
                LessonSection(
                    heading = "В ресторане",
                    items = listOf(
                        LessonItem("el menú", "меню", "¿Me trae el menú?"),
                        LessonItem("el plato", "блюдо / тарелка", "el plato del día = блюдо дня"),
                        LessonItem("la cuenta", "счёт", "¡La cuenta, por favor!"),
                        LessonItem("el camarero / la camarera", "официант/ка", ""),
                        LessonItem("pedir", "заказывать", "Quiero pedir..."),
                        LessonItem("la mesa", "стол", "Una mesa para dos.")
                    )
                ),
                LessonSection(
                    heading = "Полезные фразы",
                    items = listOf(
                        LessonItem("Una mesa para dos, por favor.", "Столик на двоих.", ""),
                        LessonItem("¿Qué recomienda?", "Что порекомендуете?", ""),
                        LessonItem("Quiero el plato del día.", "Я хочу блюдо дня.", ""),
                        LessonItem("¿Está incluido el servicio?", "Обслуживание включено?", ""),
                        LessonItem("¡Buen provecho!", "Приятного аппетита!", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как попросить счёт?",
                    question = "___, por favor.",
                    options = listOf("La cuenta", "El menú", "El plato", "La mesa"),
                    correctAnswer = "La cuenta",
                    explanation = "La cuenta = счёт. ¡La cuenta, por favor! — типичная фраза в ресторане."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «столик на двоих»?",
                    question = "Una ___ para dos, por favor.",
                    options = listOf("mesa", "cuenta", "menú", "plato"),
                    correctAnswer = "mesa",
                    explanation = "la mesa = стол. Una mesa para dos = столик на двоих."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "¿Qué recomienda?",
                    options = listOf("Что порекомендуете?", "Что вы хотите?", "Что есть в меню?", "Какой счёт?"),
                    correctAnswer = "Что порекомендуете?",
                    explanation = "recomendar = рекомендовать. ¿Qué recomienda? — вопрос официанту."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что значит «¡Buen provecho!»?",
                    question = "¡Buen provecho! = ?",
                    options = listOf("Приятного аппетита!", "Добрый день!", "Спасибо за ужин!", "Пожалуйста!"),
                    correctAnswer = "Приятного аппетита!",
                    explanation = "¡Buen provecho! = приятного аппетита! Говорят перед едой или проходя мимо обедающих."
                )
            )
        ),

        // u3_l9 — Дни недели
        "u3_l9" to LessonContent(
            intro = "Семь дней недели",
            sections = listOf(
                LessonSection(
                    heading = "Дни недели",
                    items = listOf(
                        LessonItem("lunes", "понедельник", ""),
                        LessonItem("martes", "вторник", ""),
                        LessonItem("miércoles", "среда", ""),
                        LessonItem("jueves", "четверг", ""),
                        LessonItem("viernes", "пятница", ""),
                        LessonItem("sábado", "суббота", ""),
                        LessonItem("domingo", "воскресенье", "")
                    )
                ),
                LessonSection(
                    heading = "Использование",
                    items = listOf(
                        LessonItem("el lunes", "в понедельник (один раз)", "El lunes tengo clase."),
                        LessonItem("los lunes", "по понедельникам", "Los lunes trabajo."),
                        LessonItem("el fin de semana", "выходные", ""),
                        LessonItem("entre semana", "в будни", ""),
                        LessonItem("¿Qué día es hoy?", "Какой сегодня день?", "Hoy es martes.")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Какой день идёт после пятницы?",
                    question = "viernes → ___",
                    options = listOf("sábado", "domingo", "lunes", "jueves"),
                    correctAnswer = "sábado",
                    explanation = "viernes (пятница) → sábado (суббота) → domingo (воскресенье)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Los lunes tengo inglés.",
                    options = listOf("По понедельникам у меня английский.", "В понедельник у меня английский.", "Каждый понедельник я работаю.", "Сегодня понедельник."),
                    correctAnswer = "По понедельникам у меня английский.",
                    explanation = "los lunes (с артиклем + мн.ч.) = по понедельникам (регулярно)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что такое «el fin de semana»?",
                    question = "el fin de semana = ?",
                    options = listOf("выходные", "конец месяца", "будни", "рабочая неделя"),
                    correctAnswer = "выходные",
                    explanation = "fin = конец, semana = неделя. el fin de semana = конец недели = выходные (суббота+воскресенье)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Какой день — miércoles?",
                    question = "miércoles = ?",
                    options = listOf("среда", "вторник", "четверг", "пятница"),
                    correctAnswer = "среда",
                    explanation = "miércoles = среда. Запомни: ми-ЭР-колес — звучит необычно!"
                )
            )
        ),

        // u3_l10 — Месяцы
        "u3_l10" to LessonContent(
            intro = "Двенадцать месяцев года",
            sections = listOf(
                LessonSection(
                    heading = "Месяцы",
                    items = listOf(
                        LessonItem("enero", "январь", ""),
                        LessonItem("febrero", "февраль", ""),
                        LessonItem("marzo", "март", ""),
                        LessonItem("abril", "апрель", ""),
                        LessonItem("mayo", "май", ""),
                        LessonItem("junio", "июнь", ""),
                        LessonItem("julio", "июль", ""),
                        LessonItem("agosto", "август", ""),
                        LessonItem("septiembre", "сентябрь", ""),
                        LessonItem("octubre", "октябрь", ""),
                        LessonItem("noviembre", "ноябрь", ""),
                        LessonItem("diciembre", "декабрь", "")
                    )
                ),
                LessonSection(
                    heading = "Даты и сезоны",
                    items = listOf(
                        LessonItem("¿Cuándo es tu cumpleaños?", "Когда твой день рождения?", ""),
                        LessonItem("El 5 de mayo.", "Пятого мая.", "число + de + месяц"),
                        LessonItem("en enero", "в январе", "предлог en + месяц"),
                        LessonItem("Nací en marzo.", "Я родился/лась в марте.", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Какой месяц восьмой?",
                    question = "Восьмой месяц = ___",
                    options = listOf("agosto", "julio", "junio", "septiembre"),
                    correctAnswer = "agosto",
                    explanation = "agosto = август (8-й месяц). julio = июль (7), septiembre = сентябрь (9)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «в апреле»?",
                    question = "___ abril",
                    options = listOf("en", "el", "de", "por"),
                    correctAnswer = "en",
                    explanation = "en + месяц = в (месяце). en abril = в апреле, en enero = в январе."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи дату",
                    question = "El 3 de febrero",
                    options = listOf("3 февраля", "3 марта", "23 февраля", "13 февраля"),
                    correctAnswer = "3 февраля",
                    explanation = "el 3 de febrero = третье февраля. Формула: el + число + de + месяц."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Mi cumpleaños es en diciembre.",
                    options = listOf("Мой день рождения в декабре.", "Я родился в декабре.", "Декабрь — мой любимый месяц.", "Рождество в декабре."),
                    correctAnswer = "Мой день рождения в декабре.",
                    explanation = "cumpleaños = день рождения. en diciembre = в декабре."
                )
            )
        ),

        // u3_l11 — Когда? hoy/mañana/ayer
        "u3_l11" to LessonContent(
            intro = "Слова для обозначения времени",
            sections = listOf(
                LessonSection(
                    heading = "Когда?",
                    items = listOf(
                        LessonItem("hoy", "сегодня", "Hoy es lunes."),
                        LessonItem("mañana", "завтра", "Mañana trabajo."),
                        LessonItem("ayer", "вчера", "Ayer fui al cine."),
                        LessonItem("ahora", "сейчас", "Ahora estoy en casa."),
                        LessonItem("después", "потом / после", "Después como."),
                        LessonItem("antes", "раньше / до", "Antes estudio."),
                        LessonItem("siempre", "всегда", "Siempre desayuno."),
                        LessonItem("nunca", "никогда", "Nunca como carne."),
                        LessonItem("a veces", "иногда", "A veces corro."),
                        LessonItem("ya", "уже", "Ya termino.")
                    )
                ),
                LessonSection(
                    heading = "Части дня",
                    items = listOf(
                        LessonItem("por la mañana", "утром", ""),
                        LessonItem("por la tarde", "днём/вечером", ""),
                        LessonItem("por la noche", "ночью", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Siempre desayuno a las ocho.",
                    options = listOf("Я всегда завтракаю в восемь.", "Я иногда завтракаю.", "Я никогда не завтракаю.", "Я уже завтракал."),
                    correctAnswer = "Я всегда завтракаю в восемь.",
                    explanation = "siempre = всегда. desayunar = завтракать. a las ocho = в восемь."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «вчера»?",
                    question = "___ fui al parque.",
                    options = listOf("Ayer", "Hoy", "Mañana", "Ahora"),
                    correctAnswer = "Ayer",
                    explanation = "Ayer = вчера. hoy = сегодня. mañana = завтра."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что значит «a veces»?",
                    question = "A veces como pizza.",
                    options = listOf("Иногда я ем пиццу.", "Всегда ем пиццу.", "Никогда не ем пиццу.", "Я ем пиццу сейчас."),
                    correctAnswer = "Иногда я ем пиццу.",
                    explanation = "a veces = иногда."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Mañana por la mañana tengo clase.",
                    options = listOf("Завтра утром у меня занятие.", "Сегодня утром занятие.", "Завтра вечером занятие.", "Утром я работаю."),
                    correctAnswer = "Завтра утром у меня занятие.",
                    explanation = "mañana = завтра. por la mañana = утром. tener clase = иметь занятие."
                )
            )
        ),

        // ══════════════════════════════════════════════
        //  A1 БЛОК 4 — vocab-уроки
        // ══════════════════════════════════════════════

        // u4_l0 — Транспорт
        "u4_l0" to LessonContent(
            intro = "Виды транспорта",
            sections = listOf(
                LessonSection(
                    heading = "Транспорт",
                    items = listOf(
                        LessonItem("el metro", "метро", "Voy en metro."),
                        LessonItem("el autobús", "автобус", "Tomo el autobús."),
                        LessonItem("el taxi", "такси", "Pido un taxi."),
                        LessonItem("el tren", "поезд", "El tren sale a las 9."),
                        LessonItem("el avión", "самолёт", "Viajo en avión."),
                        LessonItem("el coche / el carro", "машина", "Tengo coche."),
                        LessonItem("la bicicleta / la bici", "велосипед", "Voy en bici."),
                        LessonItem("a pie", "пешком", "Voy a pie.")
                    )
                ),
                LessonSection(
                    heading = "Предлог EN для транспорта",
                    items = listOf(
                        LessonItem("en metro", "на метро", ""),
                        LessonItem("en autobús", "на автобусе", ""),
                        LessonItem("en coche", "на машине", ""),
                        LessonItem("en tren", "на поезде", ""),
                        LessonItem("a pie", "пешком (исключение!)", "не «en pie»")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Voy al trabajo en metro.",
                    options = listOf("Я еду на работу на метро.", "Я иду на работу пешком.", "Я езжу на автобусе.", "Я живу рядом с метро."),
                    correctAnswer = "Я еду на работу на метро.",
                    explanation = "ir + en metro = ехать на метро. al trabajo = на работу."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «пешком»?",
                    question = "пешком = ___",
                    options = listOf("a pie", "en pie", "con pie", "por pie"),
                    correctAnswer = "a pie",
                    explanation = "a pie = пешком. Это исключение — другие виды транспорта идут с en."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «на велосипеде»?",
                    question = "Voy ___ bici.",
                    options = listOf("en", "a", "con", "de"),
                    correctAnswer = "en",
                    explanation = "en bici = на велосипеде. Все транспортные средства (кроме a pie) используют предлог en."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что значит «el avión»?",
                    question = "el avión = ?",
                    options = listOf("самолёт", "автобус", "поезд", "такси"),
                    correctAnswer = "самолёт",
                    explanation = "el avión = самолёт. viajar en avión = путешествовать самолётом."
                )
            )
        ),

        // u4_l3 — Дорога
        "u4_l3" to LessonContent(
            intro = "Как спросить дорогу и объяснить маршрут",
            sections = listOf(
                LessonSection(
                    heading = "Как добраться",
                    items = listOf(
                        LessonItem("¿Cómo llego a...?", "Как добраться до...?", ""),
                        LessonItem("girar / doblar a la derecha", "повернуть направо", ""),
                        LessonItem("girar / doblar a la izquierda", "повернуть налево", ""),
                        LessonItem("seguir recto / todo recto", "идти прямо", ""),
                        LessonItem("la calle", "улица", "en la calle Mayor"),
                        LessonItem("la plaza", "площадь", "la Plaza Mayor"),
                        LessonItem("la parada", "остановка", "la parada de metro"),
                        LessonItem("el semáforo", "светофор", "en el semáforo, gira")
                    )
                ),
                LessonSection(
                    heading = "Расстояние",
                    items = listOf(
                        LessonItem("cerca", "близко", "Está cerca."),
                        LessonItem("lejos", "далеко", "Está lejos."),
                        LessonItem("a cinco minutos", "в пяти минутах", ""),
                        LessonItem("al final de la calle", "в конце улицы", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как спросить дорогу до вокзала?",
                    question = "¿___ llego a la estación?",
                    options = listOf("Cómo", "Dónde", "Cuándo", "Quién"),
                    correctAnswer = "Cómo",
                    explanation = "¿Cómo llego a...? = как мне добраться до...? Cómo = как."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «повернуть направо»?",
                    question = "girar a la ___",
                    options = listOf("derecha", "izquierda", "recto", "calle"),
                    correctAnswer = "derecha",
                    explanation = "a la derecha = направо. a la izquierda = налево."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Sigue todo recto.",
                    options = listOf("Иди прямо.", "Поверни направо.", "Стоп.", "Вернись назад."),
                    correctAnswer = "Иди прямо.",
                    explanation = "seguir recto / todo recto = идти прямо."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что значит «está cerca»?",
                    question = "El metro está cerca.",
                    options = listOf("Метро близко.", "Метро далеко.", "Метро здесь.", "Метро за углом."),
                    correctAnswer = "Метро близко.",
                    explanation = "cerca = близко. lejos = далеко. Está cerca = это близко."
                )
            )
        ),

        // u4_l4 — Магазин
        "u4_l4" to LessonContent(
            intro = "Покупки и цены",
            sections = listOf(
                LessonSection(
                    heading = "В магазине",
                    items = listOf(
                        LessonItem("¿Cuánto cuesta?", "Сколько стоит?", "ед. число"),
                        LessonItem("¿Cuánto cuestan?", "Сколько стоят?", "мн. число"),
                        LessonItem("caro/a", "дорогой/ая", "Es muy caro."),
                        LessonItem("barato/a", "дешёвый/ая", "Es barato."),
                        LessonItem("el precio", "цена", "¿Cuál es el precio?"),
                        LessonItem("comprar", "покупать", "Quiero comprar..."),
                        LessonItem("vender", "продавать", "¿Venden pan aquí?"),
                        LessonItem("la tienda", "магазин", ""),
                        LessonItem("el supermercado", "супермаркет", "")
                    )
                ),
                LessonSection(
                    heading = "Диалог в магазине",
                    items = listOf(
                        LessonItem("¿En qué puedo ayudarle?", "Чем могу помочь?", "продавец"),
                        LessonItem("Busco...", "Я ищу...", "покупатель"),
                        LessonItem("¿Tiene...?", "У вас есть...?", ""),
                        LessonItem("Me lo llevo.", "Я это возьму.", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как спросить цену одного предмета?",
                    question = "¿Cuánto ___ esta camisa?",
                    options = listOf("cuesta", "cuestan", "vale", "es"),
                    correctAnswer = "cuesta",
                    explanation = "¿Cuánto cuesta? = сколько стоит? (ед.ч.). ¿Cuánto cuestan? = сколько стоят? (мн.ч.)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Es muy barato.",
                    options = listOf("Это очень дёшево.", "Это очень дорого.", "Это дёшево.", "Хорошая цена."),
                    correctAnswer = "Это очень дёшево.",
                    explanation = "barato = дешёвый/дёшево. muy = очень. caro = дорогой."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «я ищу»?",
                    question = "___ una camisa azul.",
                    options = listOf("Busco", "Compro", "Vendo", "Tengo"),
                    correctAnswer = "Busco",
                    explanation = "buscar = искать. Busco = я ищу."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что значит «Me lo llevo»?",
                    question = "Me lo llevo = ?",
                    options = listOf("Я это возьму.", "Мне это не нравится.", "Покажите другое.", "Это моё."),
                    correctAnswer = "Я это возьму.",
                    explanation = "Me lo llevo = я это беру/возьму. Говорят когда решили купить товар."
                )
            )
        ),

        // u4_l5 — Деньги
        "u4_l5" to LessonContent(
            intro = "Деньги и оплата",
            sections = listOf(
                LessonSection(
                    heading = "Деньги",
                    items = listOf(
                        LessonItem("el euro", "евро", ""),
                        LessonItem("el céntimo", "цент", "50 céntimos"),
                        LessonItem("el billete", "купюра / билет", "un billete de 20 euros"),
                        LessonItem("la moneda", "монета", ""),
                        LessonItem("el cambio", "сдача / обмен", "¿Tiene cambio?"),
                        LessonItem("la tarjeta", "карточка", "Pago con tarjeta."),
                        LessonItem("en efectivo", "наличными", "Pago en efectivo.")
                    )
                ),
                LessonSection(
                    heading = "Оплата",
                    items = listOf(
                        LessonItem("pagar", "платить", "¿Cómo paga?"),
                        LessonItem("¿Aceptan tarjeta?", "Принимаете карту?", ""),
                        LessonItem("¿Tiene cambio de 50?", "Есть сдача с 50?", ""),
                        LessonItem("Quédese con el cambio.", "Оставьте сдачу себе.", "чаевые")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «я плачу картой»?",
                    question = "Pago con ___.",
                    options = listOf("tarjeta", "billete", "cambio", "moneda"),
                    correctAnswer = "tarjeta",
                    explanation = "pagar con tarjeta = платить картой. pagar en efectivo = платить наличными."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что такое «el cambio»?",
                    question = "el cambio = ?",
                    options = listOf("сдача", "карта", "купюра", "монета"),
                    correctAnswer = "сдача",
                    explanation = "el cambio = сдача (или обмен валюты). ¿Tiene cambio? = У вас есть сдача?"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи вопрос",
                    question = "¿Aceptan tarjeta?",
                    options = listOf("Принимаете карту?", "Есть ли скидка?", "Сколько стоит?", "Где касса?"),
                    correctAnswer = "Принимаете карту?",
                    explanation = "aceptar = принимать. tarjeta = карточка. ¿Aceptan tarjeta? — частый вопрос в магазине."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «наличными»?",
                    question = "Pago ___.",
                    options = listOf("en efectivo", "con tarjeta", "con billete", "con moneda"),
                    correctAnswer = "en efectivo",
                    explanation = "en efectivo = наличными. con tarjeta = картой."
                )
            )
        ),

        // u4_l8 — Тело
        "u4_l8" to LessonContent(
            intro = "Части тела",
            sections = listOf(
                LessonSection(
                    heading = "Части тела",
                    items = listOf(
                        LessonItem("la cabeza", "голова", ""),
                        LessonItem("el ojo / los ojos", "глаз / глаза", ""),
                        LessonItem("la nariz", "нос", ""),
                        LessonItem("la boca", "рот", ""),
                        LessonItem("la oreja / la oreja", "ухо / уши", ""),
                        LessonItem("el cuello", "шея", ""),
                        LessonItem("el brazo", "рука (от плеча)", ""),
                        LessonItem("la mano", "рука (ладонь/кисть)", ""),
                        LessonItem("el dedo", "палец", ""),
                        LessonItem("la pierna", "нога (от бедра)", ""),
                        LessonItem("el pie", "стопа / нога", ""),
                        LessonItem("la espalda", "спина", "")
                    )
                ),
                LessonSection(
                    heading = "Рука vs Нога — внимание!",
                    items = listOf(
                        LessonItem("el brazo", "рука (вся рука)", "Me duele el brazo."),
                        LessonItem("la mano", "кисть руки", "Levanta la mano."),
                        LessonItem("la pierna", "нога (вся нога)", "Me duele la pierna."),
                        LessonItem("el pie", "стопа", "Tengo frío en los pies.")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «голова»?",
                    question = "голова = ___",
                    options = listOf("la cabeza", "la cara", "el cuello", "la espalda"),
                    correctAnswer = "la cabeza",
                    explanation = "la cabeza = голова."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Me duele el brazo.",
                    options = listOf("У меня болит рука.", "У меня болит нога.", "У меня болит голова.", "У меня болит спина."),
                    correctAnswer = "У меня болит рука.",
                    explanation = "doler = болеть. Me duele = у меня болит. el brazo = рука."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что такое «la mano»?",
                    question = "la mano = ?",
                    options = listOf("кисть руки", "вся рука", "палец", "плечо"),
                    correctAnswer = "кисть руки",
                    explanation = "la mano = кисть/рука (ладонь). el brazo = вся рука от плеча. Внимание: la mano — женского рода, несмотря на -o!"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Сколько пальцев?",
                    question = "Tengo diez ___.",
                    options = listOf("dedos", "manos", "pies", "brazos"),
                    correctAnswer = "dedos",
                    explanation = "dedo = палец. diez dedos = десять пальцев."
                )
            )
        ),

        // u4_l9 — Здоровье
        "u4_l9" to LessonContent(
            intro = "Как говорить о самочувствии и болезнях",
            sections = listOf(
                LessonSection(
                    heading = "Самочувствие",
                    items = listOf(
                        LessonItem("Me duele la cabeza.", "У меня болит голова.", ""),
                        LessonItem("Me duelen los pies.", "У меня болят ноги.", "мн. число → duelen"),
                        LessonItem("Tengo fiebre.", "У меня температура.", ""),
                        LessonItem("Tengo tos.", "У меня кашель.", ""),
                        LessonItem("Estoy enfermo/a.", "Я болен/больна.", ""),
                        LessonItem("Me encuentro mal.", "Я плохо себя чувствую.", "")
                    )
                ),
                LessonSection(
                    heading = "Медицина",
                    items = listOf(
                        LessonItem("el médico / la médica", "врач", "Voy al médico."),
                        LessonItem("la farmacia", "аптека", ""),
                        LessonItem("el medicamento", "лекарство", ""),
                        LessonItem("la pastilla", "таблетка", "Toma una pastilla."),
                        LessonItem("¿Qué le pasa?", "Что с вами?", "врач → пациент"),
                        LessonItem("¿Dónde le duele?", "Где болит?", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «у меня болит живот»?",
                    question = "Me ___ el estómago.",
                    options = listOf("duele", "duelen", "duelo", "dolor"),
                    correctAnswer = "duele",
                    explanation = "doler работает как gustar: me duele (ед.ч.) / me duelen (мн.ч.). el estómago = живот."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Tengo fiebre y tos.",
                    options = listOf("У меня температура и кашель.", "У меня головная боль.", "Я очень устал.", "У меня грипп."),
                    correctAnswer = "У меня температура и кашель.",
                    explanation = "fiebre = температура/жар. tos = кашель."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Куда идёшь если заболел?",
                    question = "Estoy enfermo. Voy ___.",
                    options = listOf("al médico", "a la farmacia", "al supermercado", "a casa"),
                    correctAnswer = "al médico",
                    explanation = "al médico = к врачу (al = a + el). Сначала к врачу, потом в аптеку (a la farmacia) за лекарствами."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как врач спросит «где болит»?",
                    question = "¿Dónde le ___?",
                    options = listOf("duele", "duelen", "duelo", "duela"),
                    correctAnswer = "duele",
                    explanation = "¿Dónde le duele? = где у вас болит? (формальное обращение)."
                )
            )
        ),

        // u4_l10 — Одежда
        "u4_l10" to LessonContent(
            intro = "Одежда и аксессуары",
            sections = listOf(
                LessonSection(
                    heading = "Одежда",
                    items = listOf(
                        LessonItem("la camisa", "рубашка", ""),
                        LessonItem("los pantalones", "брюки", "всегда мн. число"),
                        LessonItem("el vestido", "платье", ""),
                        LessonItem("la falda", "юбка", ""),
                        LessonItem("el abrigo", "пальто", ""),
                        LessonItem("la chaqueta", "пиджак / куртка", ""),
                        LessonItem("los zapatos", "туфли / ботинки", ""),
                        LessonItem("los calcetines", "носки", ""),
                        LessonItem("la camiseta", "футболка", ""),
                        LessonItem("los vaqueros", "джинсы", "")
                    )
                ),
                LessonSection(
                    heading = "Покупка одежды",
                    items = listOf(
                        LessonItem("¿Qué talla usa?", "Какой размер?", "продавец"),
                        LessonItem("la talla S/M/L", "размер S/M/L", ""),
                        LessonItem("¿Puedo probármelo?", "Могу примерить?", ""),
                        LessonItem("el probador", "примерочная", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что такое «los vaqueros»?",
                    question = "los vaqueros = ?",
                    options = listOf("джинсы", "брюки", "шорты", "носки"),
                    correctAnswer = "джинсы",
                    explanation = "los vaqueros = джинсы (в Испании). vaquero = ковбой, но vaqueros = джинсы!"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Llevo una camisa blanca.",
                    options = listOf("Я ношу белую рубашку.", "Я купил белую рубашку.", "Мне нравится белая рубашка.", "У меня белая рубашка."),
                    correctAnswer = "Я ношу белую рубашку.",
                    explanation = "llevar = носить (одежду). Llevo = я ношу."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как спросить про примерочную?",
                    question = "¿Dónde está el ___?",
                    options = listOf("probador", "talla", "vestido", "zapato"),
                    correctAnswer = "probador",
                    explanation = "el probador = примерочная. ¿Puedo probármelo? = Могу примерить?"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как называется пальто?",
                    question = "пальто = ___",
                    options = listOf("el abrigo", "la chaqueta", "el vestido", "la camisa"),
                    correctAnswer = "el abrigo",
                    explanation = "el abrigo = пальто. la chaqueta = пиджак или лёгкая куртка."
                )
            )
        ),

        // u4_l11 — Погода
        "u4_l11" to LessonContent(
            intro = "Как говорить о погоде",
            sections = listOf(
                LessonSection(
                    heading = "HACER + погода",
                    items = listOf(
                        LessonItem("Hace calor.", "Жарко.", ""),
                        LessonItem("Hace frío.", "Холодно.", ""),
                        LessonItem("Hace sol.", "Солнечно.", ""),
                        LessonItem("Hace viento.", "Ветрено.", ""),
                        LessonItem("Hace buen tiempo.", "Хорошая погода.", ""),
                        LessonItem("Hace mal tiempo.", "Плохая погода.", "")
                    )
                ),
                LessonSection(
                    heading = "Другие глаголы погоды",
                    items = listOf(
                        LessonItem("Llueve. / Está lloviendo.", "Идёт дождь.", "llover"),
                        LessonItem("Nieva. / Está nevando.", "Идёт снег.", "nevar"),
                        LessonItem("Está nublado.", "Пасмурно.", ""),
                        LessonItem("Está despejado.", "Ясно (нет облаков).", ""),
                        LessonItem("¿Qué tiempo hace?", "Какая погода?", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «холодно»?",
                    question = "Hace ___.",
                    options = listOf("frío", "calor", "sol", "viento"),
                    correctAnswer = "frío",
                    explanation = "Hace frío = холодно. Hace calor = жарко."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Está lloviendo.",
                    options = listOf("Идёт дождь.", "Идёт снег.", "Пасмурно.", "Ветрено."),
                    correctAnswer = "Идёт дождь.",
                    explanation = "llover = идти дождю. Está lloviendo = сейчас идёт дождь."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «хорошая погода»?",
                    question = "Hace ___ tiempo.",
                    options = listOf("buen", "mal", "mucho", "poco"),
                    correctAnswer = "buen",
                    explanation = "Hace buen tiempo = хорошая погода. Hace mal tiempo = плохая погода."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что значит «Está despejado»?",
                    question = "Está despejado = ?",
                    options = listOf("Ясно / Нет облаков", "Пасмурно", "Туман", "Ветрено"),
                    correctAnswer = "Ясно / Нет облаков",
                    explanation = "despejado = ясный (без облаков). nublado = облачный/пасмурный."
                )
            )
        ),

        // u4_l12 — Мой день
        "u4_l12" to LessonContent(
            intro = "Распорядок дня — глаголы повседневной жизни",
            sections = listOf(
                LessonSection(
                    heading = "Распорядок дня",
                    items = listOf(
                        LessonItem("levantarse", "вставать", "Me levanto a las 7."),
                        LessonItem("ducharse", "принимать душ", "Me ducho por la mañana."),
                        LessonItem("desayunar", "завтракать", "Desayuno a las 8."),
                        LessonItem("ir al trabajo / a clase", "идти на работу / учёбу", ""),
                        LessonItem("trabajar / estudiar", "работать / учиться", ""),
                        LessonItem("comer / almorzar", "обедать", "Como a las 14:00."),
                        LessonItem("hacer deporte", "заниматься спортом", ""),
                        LessonItem("cenar", "ужинать", "Cenamos a las 21:00."),
                        LessonItem("ver la tele", "смотреть телевизор", ""),
                        LessonItem("acostarse", "ложиться спать", "Me acuesto a las 23:00.")
                    )
                ),
                LessonSection(
                    heading = "Мой день — пример",
                    items = listOf(
                        LessonItem("Me levanto a las siete.", "Я встаю в семь.", ""),
                        LessonItem("Desayuno y voy al trabajo.", "Завтракаю и иду на работу.", ""),
                        LessonItem("Como a las dos.", "Обедаю в два.", ""),
                        LessonItem("Me acuesto a las once.", "Ложусь спать в одиннадцать.", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Первое действие утром — обычно?",
                    question = "Por la mañana, primero ___.",
                    options = listOf("me levanto", "me acuesto", "ceno", "trabajo"),
                    correctAnswer = "me levanto",
                    explanation = "levantarse = вставать. Me levanto = я встаю. Первое действие дня!"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Desayuno a las ocho.",
                    options = listOf("Я завтракаю в восемь.", "Я ужинаю в восемь.", "Я встаю в восемь.", "Я обедаю в восемь."),
                    correctAnswer = "Я завтракаю в восемь.",
                    explanation = "desayunar = завтракать. a las ocho = в восемь."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «я занимаюсь спортом»?",
                    question = "Hago ___.",
                    options = listOf("deporte", "tele", "trabajo", "ducha"),
                    correctAnswer = "deporte",
                    explanation = "hacer deporte = заниматься спортом. hago = я делаю."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Me acuesto tarde los viernes.",
                    options = listOf("По пятницам я ложусь поздно.", "В пятницу я встаю поздно.", "Я всегда сплю поздно.", "По пятницам я работаю."),
                    correctAnswer = "По пятницам я ложусь поздно.",
                    explanation = "acostarse = ложиться спать. tarde = поздно. los viernes = по пятницам."
                )
            )
        ),

        // ══════════════════════════════════════════════
        //  A2 БЛОК 1 — vocab-уроки
        // ══════════════════════════════════════════════

        "u5_l4" to LessonContent(
            intro = "Рассказываем о вчерашнем дне — первые истории в прошедшем",
            sections = listOf(
                LessonSection(
                    heading = "Маркеры прошедшего времени",
                    items = listOf(
                        LessonItem("ayer", "вчера", "Ayer comí pizza."),
                        LessonItem("anteayer", "позавчера", ""),
                        LessonItem("la semana pasada", "на прошлой неделе", ""),
                        LessonItem("el mes pasado", "в прошлом месяце", ""),
                        LessonItem("el año pasado", "в прошлом году", ""),
                        LessonItem("hace dos días", "два дня назад", ""),
                        LessonItem("por la mañana/tarde/noche", "утром/днём/ночью", "")
                    )
                ),
                LessonSection(
                    heading = "Мини-диалог: ¿Qué hiciste ayer?",
                    items = listOf(
                        LessonItem("¿Qué hiciste ayer?", "Что ты делал вчера?", ""),
                        LessonItem("Ayer me levanté tarde.", "Вчера я встал поздно.", ""),
                        LessonItem("Comí con mis amigos.", "Пообедал с друзьями.", ""),
                        LessonItem("Por la tarde fui al cine.", "Днём пошёл в кино.", ""),
                        LessonItem("Vi una película genial.", "Посмотрел отличный фильм.", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как спросить «что ты делал вчера»?",
                    question = "¿Qué ___ ayer?",
                    options = listOf("hiciste", "haces", "harás", "hacías"),
                    correctAnswer = "hiciste",
                    explanation = "hiciste = ты делал (P. Indefinido от hacer). ¿Qué hiciste ayer? — стандартный вопрос о прошлом."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "La semana pasada trabajé mucho.",
                    options = listOf("На прошлой неделе я много работал.", "На этой неделе я работаю много.", "На следующей неделе буду работать.", "Я всегда много работаю."),
                    correctAnswer = "На прошлой неделе я много работал.",
                    explanation = "la semana pasada = на прошлой неделе. trabajé = я работал (P. Indefinido)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери маркер прошедшего",
                    question = "___ fui al médico.",
                    options = listOf("Ayer", "Mañana", "Ahora", "Siempre"),
                    correctAnswer = "Ayer",
                    explanation = "Ayer = вчера — главный маркер Pretérito Indefinido."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Hace dos días llamé a mi madre.",
                    options = listOf("Два дня назад я позвонил маме.", "Через два дня позвоню маме.", "Два дня я звоню маме.", "Вчера позвонил маме."),
                    correctAnswer = "Два дня назад я позвонил маме.",
                    explanation = "hace dos días = два дня назад. llamé = я позвонил."
                )
            )
        ),

        "u5_l10" to LessonContent(
            intro = "Рассказываем о выходных — связный текст в прошедшем",
            sections = listOf(
                LessonSection(
                    heading = "Полезные глаголы для рассказа",
                    items = listOf(
                        LessonItem("salir → salí", "выйти / выйти", "Salí de casa a las 10."),
                        LessonItem("quedar con → quedé con", "встретиться с", "Quedé con Ana."),
                        LessonItem("pasear → paseé", "гулять", "Paseamos por el parque."),
                        LessonItem("volver → volví", "вернуться", "Volví a casa tarde."),
                        LessonItem("divertirse → me divertí", "веселиться", "Me divertí mucho."),
                        LessonItem("descansar → descansé", "отдыхать", "Descansé el domingo.")
                    )
                ),
                LessonSection(
                    heading = "Диалог: ¿Qué tal el fin de semana?",
                    items = listOf(
                        LessonItem("¿Qué tal el fin de semana?", "Как прошли выходные?", ""),
                        LessonItem("¡Muy bien! El sábado...", "Очень хорошо! В субботу...", ""),
                        LessonItem("quedé con mis amigos", "встретился с друзьями", ""),
                        LessonItem("fuimos a un restaurante", "мы пошли в ресторан", ""),
                        LessonItem("lo pasé genial", "я отлично провёл время", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «я вернулся домой»?",
                    question = "___ a casa.",
                    options = listOf("Volví", "Vuelvo", "Volvía", "Volver"),
                    correctAnswer = "Volví",
                    explanation = "volver → volví (P. Indefinido). Volví a casa = я вернулся домой."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "El domingo descansé todo el día.",
                    options = listOf("В воскресенье я отдыхал весь день.", "В субботу я работал весь день.", "В воскресенье я устал.", "Я отдыхаю каждое воскресенье."),
                    correctAnswer = "В воскресенье я отдыхал весь день.",
                    explanation = "descansé = я отдыхал. todo el día = весь день."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как спросить «как прошли выходные»?",
                    question = "¿Qué tal ___?",
                    options = listOf("el fin de semana", "la semana", "el lunes", "ayer"),
                    correctAnswer = "el fin de semana",
                    explanation = "el fin de semana = выходные. ¿Qué tal el fin de semana? — очень частый вопрос в понедельник."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи фразу",
                    question = "Lo pasé genial.",
                    options = listOf("Я отлично провёл время.", "Мне было плохо.", "Я устал.", "Мне понравилось немного."),
                    correctAnswer = "Я отлично провёл время.",
                    explanation = "pasarlo bien/genial/fatal = провести время хорошо/отлично/ужасно."
                )
            )
        ),

        "u5_l11" to LessonContent(
            intro = "Irregulares: poder → pude, saber → supe",
            sections = listOf(
                LessonSection(
                    heading = "Глаголы с основой на -ud-",
                    items = listOf(
                        LessonItem("poder → pud-", "pude, pudiste, pudo", "я смог, ты смог..."),
                        LessonItem("saber → sup-", "supe, supiste, supo", "я узнал, ты узнал..."),
                        LessonItem("poner → pus-", "puse, pusiste, puso", "я положил..."),
                        LessonItem("caber → cup-", "cupe, cupiste, cupo", "я поместился...")
                    )
                ),
                LessonSection(
                    heading = "Примеры в контексте",
                    items = listOf(
                        LessonItem("No pude dormir.", "Я не смог поспать.", ""),
                        LessonItem("¿Supiste la noticia?", "Ты узнал новость?", ""),
                        LessonItem("¿Dónde pusiste las llaves?", "Куда ты положил ключи?", ""),
                        LessonItem("Pude terminar a tiempo.", "Я смог закончить вовремя.", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Indefinido от PODER: yo",
                    question = "Ayer no ___ abrir la puerta.",
                    options = listOf("pude", "podí", "podé", "puedo"),
                    correctAnswer = "pude",
                    explanation = "poder → pude (Indefinido irregular). No pude = я не смог."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "¿Cuándo supiste la verdad?",
                    options = listOf("Когда ты узнал правду?", "Ты знаешь правду?", "Расскажи мне правду.", "Что ты знаешь?"),
                    correctAnswer = "Когда ты узнал правду?",
                    explanation = "saber → supe/supiste (Indefinido). supiste = ты узнал."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Indefinido от PONER: ella",
                    question = "Ella ___ el libro en la mesa.",
                    options = listOf("puso", "ponió", "ponó", "pone"),
                    correctAnswer = "puso",
                    explanation = "poner → puso (él/ella). pus- + o = puso."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Al final pude terminar el proyecto.",
                    options = listOf("В итоге я смог закончить проект.", "Я не смог закончить проект.", "Проект закончен.", "Мне нужно закончить проект."),
                    correctAnswer = "В итоге я смог закончить проект.",
                    explanation = "al final = в итоге/в конце концов. pude = я смог."
                )
            )
        ),

        "u5_l12" to LessonContent(
            intro = "Irregulares: dar, ver, decir, venir — особые формы",
            sections = listOf(
                LessonSection(
                    heading = "DAR и VER — короткие формы без акцента",
                    items = listOf(
                        LessonItem("dar: di, diste, dio", "я дал, ты дал, он дал", "dimos, disteis, dieron"),
                        LessonItem("ver: vi, viste, vio", "я видел, ты видел, он видел", "vimos, visteis, vieron"),
                        LessonItem("Без акцентов!", "di, vi (не dí, ví)", "односложные формы — без тильды")
                    )
                ),
                LessonSection(
                    heading = "DECIR и VENIR",
                    items = listOf(
                        LessonItem("decir → dij-", "dije, dijiste, dijo", "я сказал, ты сказал..."),
                        LessonItem("", "dijimos, dijisteis, dijeron", "мы, вы, они сказали"),
                        LessonItem("venir → vin-", "vine, viniste, vino", "я пришёл, ты пришёл..."),
                        LessonItem("", "vinimos, vinisteis, vinieron", "мы, вы, они пришли")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Indefinido от DAR: yo",
                    question = "Le ___ un regalo a mi madre.",
                    options = listOf("di", "dé", "daré", "daba"),
                    correctAnswer = "di",
                    explanation = "dar → di (yo, Indefinido). Без акцента! Le di = я дал ей."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "¿Qué dijiste?",
                    options = listOf("Что ты сказал?", "Что ты говоришь?", "Что ты скажешь?", "Скажи мне что-нибудь."),
                    correctAnswer = "Что ты сказал?",
                    explanation = "decir → dijiste (tú, Indefinido). ¿Qué dijiste? = что ты сказал?"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Indefinido от VENIR: ellos",
                    question = "Mis amigos ___ a la fiesta.",
                    options = listOf("vinieron", "venieron", "vineron", "vienen"),
                    correctAnswer = "vinieron",
                    explanation = "venir → vinieron (ellos). vin- + ieron = vinieron."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Vi una película increíble ayer.",
                    options = listOf("Вчера я посмотрел потрясающий фильм.", "Я смотрю потрясающий фильм.", "Я увижу потрясающий фильм.", "Фильм был потрясающим."),
                    correctAnswer = "Вчера я посмотрел потрясающий фильм.",
                    explanation = "ver → vi (yo, Indefinido). ayer = вчера. increíble = невероятный/потрясающий."
                )
            )
        ),

        "u5_l13" to LessonContent(
            intro = "Рассказ в прошлом — связный текст с Indefinido",
            sections = listOf(
                LessonSection(
                    heading = "Связки для рассказа",
                    items = listOf(
                        LessonItem("primero", "сначала", "Primero me levanté."),
                        LessonItem("después / luego", "потом / затем", "Después fui al trabajo."),
                        LessonItem("más tarde", "позже", "Más tarde comí."),
                        LessonItem("por la mañana/tarde/noche", "утром/днём/ночью", ""),
                        LessonItem("al final", "в конце / в итоге", "Al final volví a casa."),
                        LessonItem("de repente", "вдруг / внезапно", "De repente sonó el teléfono.")
                    )
                ),
                LessonSection(
                    heading = "Пример рассказа: Mi sábado",
                    items = listOf(
                        LessonItem("Me levanté a las 10.", "Я встал в 10.", ""),
                        LessonItem("Desayuné y salí a correr.", "Позавтракал и пошёл бегать.", ""),
                        LessonItem("Luego quedé con Ana.", "Потом встретился с Аной.", ""),
                        LessonItem("Fuimos al cine y vimos una peli.", "Мы пошли в кино и посмотрели фильм.", ""),
                        LessonItem("Al final cené en casa.", "В конце поужинал дома.", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери связку для начала рассказа",
                    question = "___, me duché y desayuné.",
                    options = listOf("Primero", "Al final", "De repente", "Luego"),
                    correctAnswer = "Primero",
                    explanation = "primero = сначала/во-первых. Используется для начала рассказа о последовательности событий."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "De repente empezó a llover.",
                    options = listOf("Вдруг начался дождь.", "Потом начался дождь.", "Наконец начался дождь.", "Сначала начался дождь."),
                    correctAnswer = "Вдруг начался дождь.",
                    explanation = "de repente = вдруг/внезапно. empezó a llover = начался дождь."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Восстанови порядок: что идёт последним?",
                    question = "Primero estudié, luego comí y ___...",
                    options = listOf("al final me acosté", "primero dormí", "de repente salí", "después desayuné"),
                    correctAnswer = "al final me acosté",
                    explanation = "al final = в конце/в итоге. Используется для завершения рассказа."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Más tarde fui al supermercado.",
                    options = listOf("Позже я пошёл в супермаркет.", "Сначала я пошёл в супермаркет.", "Вдруг я пошёл в супермаркет.", "Потом я не пошёл в магазин."),
                    correctAnswer = "Позже я пошёл в супермаркет.",
                    explanation = "más tarde = позже. fui = я пошёл (Indefinido от ir)."
                )
            )
        ),

        // ══════════════════════════════════════════════
        //  A2 БЛОК 2 (unitId=6)
        // ══════════════════════════════════════════════

        "u6_l0" to LessonContent(
            intro = "Imperfecto: описываем прошлое — глаголы на -AR",
            sections = listOf(
                LessonSection(
                    heading = "Окончания Imperfecto (-AR)",
                    items = listOf(
                        LessonItem("yo", "-aba → hablaba", "я говорил/а"),
                        LessonItem("tú", "-abas → hablabas", "ты говорил/а"),
                        LessonItem("él/ella", "-aba → hablaba", "он/она говорил/а"),
                        LessonItem("nosotros", "-ábamos → hablábamos", "мы говорили"),
                        LessonItem("vosotros", "-abais → hablabais", "вы говорили"),
                        LessonItem("ellos", "-aban → hablaban", "они говорили")
                    )
                ),
                LessonSection(
                    heading = "Когда используется Imperfecto",
                    items = listOf(
                        LessonItem("Описание в прошлом", "Era alto y tenía ojos azules.", "каким был"),
                        LessonItem("Привычные действия", "Cuando era niño, jugaba mucho.", "что делал обычно"),
                        LessonItem("Фон для события", "Llovía cuando salí.", "шёл дождь, когда я вышел"),
                        LessonItem("Маркеры: siempre, antes, cuando era niño", "всегда, раньше, когда был ребёнком", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Проспрягай: yo + trabajar (Imperfecto)",
                    question = "Antes yo ___ mucho.",
                    options = listOf("trabajaba", "trabajé", "trabajo", "trabajaré"),
                    correctAnswer = "trabajaba",
                    explanation = "trabajar → yo trabajaba (Imperfecto). Antes = раньше — маркер Imperfecto."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Cuando era niño, jugaba en el parque.",
                    options = listOf("Когда я был ребёнком, я играл в парке.", "Вчера я играл в парке.", "Ребёнок играет в парке.", "Я всегда играю в парке."),
                    correctAnswer = "Когда я был ребёнком, я играл в парке.",
                    explanation = "era niño = был ребёнком. jugaba = играл. cuando + Imperfecto — типичная конструкция."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Ellos siempre ___ juntos.",
                    options = listOf("estudiaban", "estudiaron", "estudian", "estudiarán"),
                    correctAnswer = "estudiaban",
                    explanation = "siempre + Imperfecto = привычное действие в прошлом. ellos estudiaban = они учились."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Какое окончание у nosotros в Imperfecto -AR?",
                    question = "nosotros habl___",
                    options = listOf("-ábamos", "-aban", "-abas", "-aba"),
                    correctAnswer = "-ábamos",
                    explanation = "nosotros hablábamos — единственная форма с ударением на á. Не забудь акцент!"
                )
            )
        ),

        "u6_l1" to LessonContent(
            intro = "Imperfecto: глаголы на -ER/-IR и три неправильных",
            sections = listOf(
                LessonSection(
                    heading = "Окончания Imperfecto (-ER/-IR)",
                    items = listOf(
                        LessonItem("yo", "-ía → comía / vivía", "я ел / жил"),
                        LessonItem("tú", "-ías → comías", "ты ел"),
                        LessonItem("él/ella", "-ía → comía", "он ел"),
                        LessonItem("nosotros", "-íamos → comíamos", "мы ели"),
                        LessonItem("vosotros", "-íais → comíais", "вы ели"),
                        LessonItem("ellos", "-ían → comían", "они ели")
                    )
                ),
                LessonSection(
                    heading = "Три неправильных глагола",
                    items = listOf(
                        LessonItem("SER: era, eras, era", "был/была/было", "éramos, erais, eran"),
                        LessonItem("IR: iba, ibas, iba", "шёл/шла/шло", "íbamos, ibais, iban"),
                        LessonItem("VER: veía, veías, veía", "видел/смотрел", "veíamos, veíais, veían")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Проспрягай: ella + vivir (Imperfecto)",
                    question = "Antes ella ___ en Madrid.",
                    options = listOf("vivía", "vivió", "vive", "vivirá"),
                    correctAnswer = "vivía",
                    explanation = "vivir → ella vivía (Imperfecto -IR). antes = раньше."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Неправильный глагол: SER",
                    question = "Cuando era joven, ___ muy activo.",
                    options = listOf("era", "fui", "soy", "seré"),
                    correctAnswer = "era",
                    explanation = "ser → era (Imperfecto). Описание черты характера в прошлом = Imperfecto."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "De niño, iba al colegio en bici.",
                    options = listOf("В детстве я ездил в школу на велосипеде.", "Вчера я поехал в школу.", "Я езжу в школу на велосипеде.", "Завтра поеду на велосипеде."),
                    correctAnswer = "В детстве я ездил в школу на велосипеде.",
                    explanation = "de niño = в детстве. iba = ехал/ходил (Imperfecto от ir). Привычное действие в прошлом."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму VER",
                    question = "Nosotros ___ mucha televisión.",
                    options = listOf("veíamos", "vimos", "vemos", "verémos"),
                    correctAnswer = "veíamos",
                    explanation = "ver → veíamos (Imperfecto). Привычное действие в прошлом — смотрели много телевизора."
                )
            )
        ),

        "u6_l2" to LessonContent(
            intro = "Indefinido vs Imperfecto — когда что использовать",
            sections = listOf(
                LessonSection(
                    heading = "Главное правило",
                    items = listOf(
                        LessonItem("Indefinido", "завершённое действие", "Ayer comí pizza. — Вчера я съел пиццу."),
                        LessonItem("Imperfecto", "описание / фон / привычка", "Cuando era niño, comía pizza. — В детстве ел пиццу."),
                        LessonItem("Вместе", "фон (Imp.) + событие (Ind.)", "Llovía cuando llegué. — Шёл дождь, когда я пришёл.")
                    )
                ),
                LessonSection(
                    heading = "Маркеры",
                    items = listOf(
                        LessonItem("Indefinido →", "ayer, el lunes, de repente, entonces", ""),
                        LessonItem("Imperfecto →", "siempre, antes, cuando era niño, normalmente", ""),
                        LessonItem("de repente", "вдруг (→ Indefinido)", "Caminaba cuando de repente cayó."),
                        LessonItem("mientras", "пока (→ Imperfecto)", "Mientras comía, sonó el teléfono.")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильное время",
                    question = "De repente ___ un ruido fuerte.",
                    options = listOf("escuché", "escuchaba", "escucho", "escucharé"),
                    correctAnswer = "escuché",
                    explanation = "de repente = вдруг — маркер Indefinido. Внезапное действие = Indefinido."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильное время",
                    question = "Cuando era pequeño, ___ mucho.",
                    options = listOf("lloraba", "lloré", "lloro", "lloraré"),
                    correctAnswer = "lloraba",
                    explanation = "cuando era pequeño = когда был маленьким — описание привычки = Imperfecto."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Фон + событие. Выбери оба глагола",
                    question = "Mientras ___ (leer), ___ (llamar) mi amigo.",
                    options = listOf("leía / llamó", "leí / llamaba", "leía / llamaba", "leí / llamó"),
                    correctAnswer = "leía / llamó",
                    explanation = "mientras + Imperfecto = фоновое действие. Событие прерывает = Indefinido. leía (фон) / llamó (событие)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Normalmente desayunaba en casa, pero ayer desayuné en la cafetería.",
                    options = listOf("Обычно я завтракал дома, но вчера позавтракал в кафе.", "Вчера я завтракал дома.", "Я всегда завтракаю в кафе.", "Раньше я не завтракал."),
                    correctAnswer = "Обычно я завтракал дома, но вчера позавтракал в кафе.",
                    explanation = "normalmente + Imperfecto (привычка) vs ayer + Indefinido (конкретный случай)."
                )
            )
        ),

        "u6_l3" to LessonContent(
            intro = "Описания из прошлого — Imperfecto в рассказе",
            sections = listOf(
                LessonSection(
                    heading = "Описываем людей и места в прошлом",
                    items = listOf(
                        LessonItem("Era alto y delgado.", "Он был высоким и худым.", "внешность"),
                        LessonItem("Tenía el pelo rubio.", "У него были светлые волосы.", ""),
                        LessonItem("Era muy simpático.", "Он был очень приятным.", "характер"),
                        LessonItem("La casa era pequeña.", "Дом был маленьким.", "место"),
                        LessonItem("Había mucha gente.", "Было много людей.", "hay → había"),
                        LessonItem("Hacía calor.", "Было жарко.", "погода в прошлом")
                    )
                ),
                LessonSection(
                    heading = "HAY → HABÍA (было / были)",
                    items = listOf(
                        LessonItem("Hay un parque.", "Есть парк.", "настоящее"),
                        LessonItem("Había un parque.", "Был парк.", "прошлое"),
                        LessonItem("Había mucha gente.", "Было много людей.", ""),
                        LessonItem("No había nada.", "Ничего не было.", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Опиши внешность в прошлом",
                    question = "Mi abuelo ___ muy alto.",
                    options = listOf("era", "fue", "es", "será"),
                    correctAnswer = "era",
                    explanation = "Описание внешности/характера в прошлом = Imperfecto. era = был."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи hay → прошлое",
                    question = "Antes ___ un cine aquí.",
                    options = listOf("había", "hubo", "hay", "habrá"),
                    correctAnswer = "había",
                    explanation = "hay (есть) → había (было) в Imperfecto. Описание того, что существовало."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Погода в прошлом",
                    question = "Ayer ___ mucho calor.",
                    options = listOf("hacía", "hizo", "hace", "hará"),
                    correctAnswer = "hacía",
                    explanation = "Погода как фон/описание = Imperfecto. hacía calor = было жарко."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "La ciudad era bonita y había muchos turistas.",
                    options = listOf("Город был красивым и было много туристов.", "Город красивый и много туристов.", "В городе было мало туристов.", "Город стал красивым."),
                    correctAnswer = "Город был красивым и было много туристов.",
                    explanation = "era bonita = был красивым (описание). había muchos turistas = было много туристов."
                )
            )
        ),

        "u6_l4" to LessonContent(
            intro = "Сравнение: más...que и menos...que",
            sections = listOf(
                LessonSection(
                    heading = "Сравнение превосходства и уступки",
                    items = listOf(
                        LessonItem("más + adj + que", "более ... чем", "más alto que = выше чем"),
                        LessonItem("menos + adj + que", "менее ... чем", "menos caro que = дешевле чем"),
                        LessonItem("más + adv + que", "больше ... чем", "más rápido que = быстрее чем"),
                        LessonItem("más + noun + que", "больше ... чем", "más dinero que = больше денег чем")
                    )
                ),
                LessonSection(
                    heading = "Неправильные формы сравнения",
                    items = listOf(
                        LessonItem("bueno → mejor", "хороший → лучше", "Este café es mejor."),
                        LessonItem("malo → peor", "плохой → хуже", "Este tiempo es peor."),
                        LessonItem("grande → mayor", "большой → старше/больше", "Mi hermano es mayor."),
                        LessonItem("pequeño → menor", "маленький → младше/меньше", "Soy menor que tú.")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Составь сравнение",
                    question = "Madrid es ___ grande ___ Valencia.",
                    options = listOf("más / que", "tan / como", "menos / que", "más / como"),
                    correctAnswer = "más / que",
                    explanation = "más + прилагательное + que = более ... чем. Madrid es más grande que Valencia."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Este restaurante es ___ que el otro.",
                    options = listOf("mejor", "más bueno", "bueno más", "más mejor"),
                    correctAnswer = "mejor",
                    explanation = "bueno → mejor (неправильная форма). Нельзя сказать «más bueno» — только mejor."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Mi hermana es menor que yo.",
                    options = listOf("Моя сестра младше меня.", "Моя сестра старше меня.", "Моя сестра маленькая.", "Моя сестра выше меня."),
                    correctAnswer = "Моя сестра младше меня.",
                    explanation = "menor = младше (pequeño → menor). mayor = старше."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Составь фразу",
                    question = "Tengo ___ dinero ___ tú.",
                    options = listOf("menos / que", "más / como", "menos / como", "tan / que"),
                    correctAnswer = "menos / que",
                    explanation = "menos + существительное + que = меньше ... чем. Tengo menos dinero que tú = у меня меньше денег, чем у тебя."
                )
            )
        ),

        "u6_l5" to LessonContent(
            intro = "Сравнение равенства: tan...como и tanto...como",
            sections = listOf(
                LessonSection(
                    heading = "tan + прилагательное/наречие + como",
                    items = listOf(
                        LessonItem("tan + adj + como", "такой же ... как", "tan alto como = такой же высокий как"),
                        LessonItem("Soy tan alto como tú.", "Я такой же высокий как ты.", ""),
                        LessonItem("Habla tan rápido como yo.", "Говорит так же быстро как я.", ""),
                        LessonItem("No es tan caro como piensas.", "Это не так дорого, как ты думаешь.", "")
                    )
                ),
                LessonSection(
                    heading = "tanto/a/os/as + существительное + como",
                    items = listOf(
                        LessonItem("tanto + masc. noun", "столько же ... как", "tanto tiempo como = столько же времени как"),
                        LessonItem("tanta + fem. noun", "столько же", "tanta paciencia como"),
                        LessonItem("tantos/tantas + plural", "столько же", "tantos amigos como yo"),
                        LessonItem("Tengo tanto dinero como tú.", "У меня столько же денег как у тебя.", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный вариант",
                    question = "Mi ciudad es ___ bonita ___ la tuya.",
                    options = listOf("tan / como", "más / que", "tanto / como", "tan / que"),
                    correctAnswer = "tan / como",
                    explanation = "tan + прилагательное + como = такой же ... как."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Согласуй tanto",
                    question = "No tengo ___ paciencia ___ ella.",
                    options = listOf("tanta / como", "tanto / como", "tan / como", "tantos / como"),
                    correctAnswer = "tanta / como",
                    explanation = "paciencia — женского рода → tanta. tanta paciencia como = столько же терпения как."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Él trabaja tan duro como su padre.",
                    options = listOf("Он работает так же усердно как его отец.", "Он работает усерднее отца.", "Он работает меньше отца.", "Отец работает усерднее."),
                    correctAnswer = "Он работает так же усердно как его отец.",
                    explanation = "tan + наречие (duro) + como = так же ... как."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Tengo ___ libros ___ tú.",
                    options = listOf("tantos / como", "tanto / como", "tanta / como", "tan / como"),
                    correctAnswer = "tantos / como",
                    explanation = "libros — мн. число муж. рода → tantos. tantos libros como = столько же книг как."
                )
            )
        ),

        "u6_l6" to LessonContent(
            intro = "Превосходная степень: el/la más, el mejor",
            sections = listOf(
                LessonSection(
                    heading = "Превосходная степень",
                    items = listOf(
                        LessonItem("el/la/los/las + más + adj", "самый/ая/ые", "el más alto = самый высокий"),
                        LessonItem("el/la/los/las + menos + adj", "наименее", "el menos caro = наименее дорогой"),
                        LessonItem("de + группа", "из (группы)", "el más alto de la clase = самый высокий в классе"),
                        LessonItem("Es el mejor de todos.", "Он лучший из всех.", "")
                    )
                ),
                LessonSection(
                    heading = "Неправильные превосходные",
                    items = listOf(
                        LessonItem("bueno → el mejor", "хороший → лучший", "el mejor restaurante"),
                        LessonItem("malo → el peor", "плохой → худший", "el peor día"),
                        LessonItem("grande → el mayor", "большой → самый старший/большой", "el mayor de los hermanos"),
                        LessonItem("pequeño → el menor", "маленький → самый младший", "la menor de la familia")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Составь превосходную степень",
                    question = "Es ___ película ___ año.",
                    options = listOf("la mejor / del", "la más buena / del", "la mejor / de el", "la más mejor / del"),
                    correctAnswer = "la mejor / del",
                    explanation = "buena → la mejor (неправильная форма). del = de + el. Es la mejor película del año."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Es el estudiante más inteligente de la clase.",
                    options = listOf("Он самый умный студент в классе.", "Он умнее всех студентов.", "Он умный студент.", "Студенты в классе умные."),
                    correctAnswer = "Он самый умный студент в классе.",
                    explanation = "el más inteligente de la clase = самый умный в классе. de = из (группы)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Ayer fue el ___ día de mi vida.",
                    options = listOf("peor", "más malo", "menor", "menos bueno"),
                    correctAnswer = "peor",
                    explanation = "malo → el peor (неправильная форма). «más malo» не используется."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Mi hermano mayor tiene 25 años.",
                    options = listOf("Моему старшему брату 25 лет.", "Мой большой брат имеет 25 лет.", "Моему младшему брату 25.", "Брат старше на 25 лет."),
                    correctAnswer = "Моему старшему брату 25 лет.",
                    explanation = "mayor = старший (из братьев/сестёр). el hermano mayor = старший брат."
                )
            )
        ),

        "u6_l7" to LessonContent(
            intro = "Прилагательные-описания людей и характера",
            sections = listOf(
                LessonSection(
                    heading = "Внешность",
                    items = listOf(
                        LessonItem("alto/a — bajo/a", "высокий — низкий", ""),
                        LessonItem("delgado/a — gordo/a", "худой — толстый", ""),
                        LessonItem("joven — mayor/viejo", "молодой — пожилой", ""),
                        LessonItem("guapo/a — feo/a", "красивый — некрасивый", ""),
                        LessonItem("el pelo rubio/moreno/pelirrojo", "светлые/тёмные/рыжие волосы", ""),
                        LessonItem("los ojos azules/verdes/marrones", "голубые/зелёные/карие глаза", "")
                    )
                ),
                LessonSection(
                    heading = "Характер",
                    items = listOf(
                        LessonItem("simpático/a", "приятный, милый", ""),
                        LessonItem("antipático/a", "неприятный", ""),
                        LessonItem("inteligente", "умный", ""),
                        LessonItem("trabajador/a", "трудолюбивый", ""),
                        LessonItem("perezoso/a", "ленивый", ""),
                        LessonItem("generoso/a — tacaño/a", "щедрый — жадный", ""),
                        LessonItem("divertido/a — aburrido/a", "весёлый — скучный", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «она высокая и стройная»?",
                    question = "Ella es ___ y ___.",
                    options = listOf("alta y delgada", "alto y delgado", "alta y delgado", "alto y delgada"),
                    correctAnswer = "alta y delgada",
                    explanation = "ella → женский род → alta (не alto), delgada (не delgado)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что значит «trabajador»?",
                    question = "Mi jefe es muy trabajador.",
                    options = listOf("Мой начальник очень трудолюбив.", "Мой начальник очень ленив.", "Мой начальник умный.", "Мой начальник строгий."),
                    correctAnswer = "Мой начальник очень трудолюбив.",
                    explanation = "trabajador = трудолюбивый (от trabajar = работать)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Опиши внешность",
                    question = "Tiene el pelo ___ y los ojos ___.",
                    options = listOf("rubio / azules", "rubios / azul", "rubio / azul", "rubia / azules"),
                    correctAnswer = "rubio / azules",
                    explanation = "el pelo rubio (ед.ч. муж.), los ojos azules (мн.ч. муж.). Прилагательные согласуются с существительным."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Антоним к «generoso»?",
                    question = "Lo contrario de generoso es ___.",
                    options = listOf("tacaño", "perezoso", "antipático", "aburrido"),
                    correctAnswer = "tacaño",
                    explanation = "generoso = щедрый ↔ tacaño = жадный/скупой."
                )
            )
        ),

        "u6_l8" to LessonContent(
            intro = "Прямые дополнения: местоимения lo, la, los, las",
            sections = listOf(
                LessonSection(
                    heading = "Местоимения прямого дополнения (OD)",
                    items = listOf(
                        LessonItem("lo", "его / это (муж.)", "¿El libro? Lo tengo."),
                        LessonItem("la", "её / это (жен.)", "¿La llave? La busco."),
                        LessonItem("los", "их (муж./смеш.)", "¿Los zapatos? Los compré."),
                        LessonItem("las", "их (жен.)", "¿Las llaves? Las perdí.")
                    )
                ),
                LessonSection(
                    heading = "Позиция в предложении",
                    items = listOf(
                        LessonItem("Перед спрягаемым глаголом", "Lo como. / La veo.", ""),
                        LessonItem("После инфинитива (слитно)", "Quiero verlo. / Voy a comprarlo.", ""),
                        LessonItem("После герундия (слитно)", "Estoy comiéndolo.", ""),
                        LessonItem("¿Lo ves?", "Ты это видишь?", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Замени: ¿Tienes el periódico?",
                    question = "Sí, ___ tengo.",
                    options = listOf("lo", "la", "los", "las"),
                    correctAnswer = "lo",
                    explanation = "el periódico — мужской род, ед.ч. → lo. Sí, lo tengo = да, он у меня есть."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Замени: ¿Compraste las entradas?",
                    question = "Sí, ___ compré ayer.",
                    options = listOf("las", "los", "la", "lo"),
                    correctAnswer = "las",
                    explanation = "las entradas — женский род, мн.ч. → las."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Позиция с инфинитивом",
                    question = "Necesito llamar a María. → Necesito ___.",
                    options = listOf("llamarla", "la llamar", "llamar la", "la llamarla"),
                    correctAnswer = "llamarla",
                    explanation = "После инфинитива местоимение присоединяется слитно: llamar + la = llamarla."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "¿El café? Ya lo he pedido.",
                    options = listOf("Кофе? Я уже его заказал.", "Кофе? Я закажу его.", "Кофе? Он вкусный.", "Кофе? Принесите его."),
                    correctAnswer = "Кофе? Я уже его заказал.",
                    explanation = "lo = его (заменяет el café). he pedido = заказал (P. Perfecto)."
                )
            )
        ),

        "u6_l9" to LessonContent(
            intro = "Косвенные дополнения: me, te, le, nos, os, les",
            sections = listOf(
                LessonSection(
                    heading = "Местоимения косвенного дополнения (OI)",
                    items = listOf(
                        LessonItem("me", "мне", "Me dices la verdad."),
                        LessonItem("te", "тебе", "Te doy un regalo."),
                        LessonItem("le", "ему / ей / Вам", "Le escribo una carta."),
                        LessonItem("nos", "нам", "Nos explica la lección."),
                        LessonItem("os", "вам", "Os cuento un secreto."),
                        LessonItem("les", "им / Вам (мн.)", "Les mando un mensaje.")
                    )
                ),
                LessonSection(
                    heading = "Глаголы, часто использующие OI",
                    items = listOf(
                        LessonItem("dar", "давать", "Te doy el libro."),
                        LessonItem("decir", "говорить", "Me dice la verdad."),
                        LessonItem("preguntar", "спрашивать", "Te pregunto algo."),
                        LessonItem("mandar / enviar", "отправлять", "Le mando un email."),
                        LessonItem("explicar", "объяснять", "Nos explica todo."),
                        LessonItem("gustar", "нравиться", "Me gusta el café.")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Вставь OI: «Я даю ТЕБЕ книгу»",
                    question = "___ doy el libro.",
                    options = listOf("Te", "Le", "Me", "Os"),
                    correctAnswer = "Te",
                    explanation = "te = тебе. Te doy el libro = я даю тебе книгу."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Le escribo un email a mi jefe.",
                    options = listOf("Я пишу email своему начальнику.", "Начальник пишет мне email.", "Мы пишем email.", "Ты пишешь email начальнику."),
                    correctAnswer = "Я пишу email своему начальнику.",
                    explanation = "le = ему (начальнику). escribo = я пишу. Часто OI дублируется: le ... a mi jefe."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери OI",
                    question = "El profesor ___ explica la gramática. (нам)",
                    options = listOf("nos", "les", "os", "me"),
                    correctAnswer = "nos",
                    explanation = "nos = нам. El profesor nos explica = учитель объясняет нам."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "¿Te puedo preguntar algo?",
                    options = listOf("Можно задать тебе вопрос?", "Ты можешь спросить меня?", "Мне можно спросить?", "Он спрашивает тебя."),
                    correctAnswer = "Можно задать тебе вопрос?",
                    explanation = "te = тебе. preguntar = спрашивать. ¿Te puedo preguntar? = можно тебя спросить?"
                )
            )
        ),

        "u6_l10" to LessonContent(
            intro = "Hace + tiempo + que — как давно что-то происходит",
            sections = listOf(
                LessonSection(
                    heading = "Конструкция hace... que",
                    items = listOf(
                        LessonItem("Hace + время + que + presente", "вот уже ... как (сейчас)", "Hace dos años que vivo aquí. = Я живу здесь уже два года."),
                        LessonItem("Hace + время + que + indefinido", "... назад (в прошлом)", "Hace dos años que llegué. = Два года назад я приехал."),
                        LessonItem("¿Cuánto tiempo hace que...?", "Как давно...?", "¿Cuánto tiempo hace que estudias español?"),
                        LessonItem("Desde hace + время", "в течение (альтернатива)", "Vivo aquí desde hace dos años.")
                    )
                ),
                LessonSection(
                    heading = "Примеры",
                    items = listOf(
                        LessonItem("Hace una hora que espero.", "Я жду уже час.", ""),
                        LessonItem("Hace un mes que no te veo.", "Я не видел тебя месяц.", ""),
                        LessonItem("Hace tres años que estudiamos.", "Мы учимся уже три года.", ""),
                        LessonItem("¿Cuánto hace que llegaste?", "Как давно ты приехал?", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Hace cinco años que vivo en Madrid.",
                    options = listOf("Я живу в Мадриде уже пять лет.", "Пять лет назад я жил в Мадриде.", "Я прожил в Мадриде пять лет.", "Пять лет я ездил в Мадрид."),
                    correctAnswer = "Я живу в Мадриде уже пять лет.",
                    explanation = "hace + tiempo + que + presente = вот уже ... как (действие продолжается сейчас)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как спросить «как давно ты учишь испанский»?",
                    question = "¿___ tiempo ___ que estudias español?",
                    options = listOf("Cuánto / hace", "Cuándo / hace", "Cuánto / es", "Qué / hace"),
                    correctAnswer = "Cuánto / hace",
                    explanation = "¿Cuánto tiempo hace que...? = как давно...? Стандартный вопрос."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный вариант",
                    question = "Hace dos días que no ___ (comer) bien.",
                    options = listOf("como", "comí", "comía", "comeré"),
                    correctAnswer = "como",
                    explanation = "hace + tiempo + que + presente = действие продолжается до сейчас. Я не ем хорошо уже два дня."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Hace una semana que llegué a España.",
                    options = listOf("Неделю назад я приехал в Испанию.", "Я живу в Испании уже неделю.", "Я приеду в Испанию через неделю.", "Неделю я езжу в Испанию."),
                    correctAnswer = "Неделю назад я приехал в Испанию.",
                    explanation = "hace + tiempo + que + indefinido = ... назад. llegué = приехал (P. Indefinido)."
                )
            )
        ),

        "u6_l11" to LessonContent(
            intro = "Одежда и мода — покупки и стиль",
            sections = listOf(
                LessonSection(
                    heading = "Одежда",
                    items = listOf(
                        LessonItem("la talla", "размер (одежды)", "¿Qué talla usas?"),
                        LessonItem("probarse", "примерять", "¿Puedo probármelo?"),
                        LessonItem("quedar bien/mal", "идти хорошо/плохо", "Te queda muy bien."),
                        LessonItem("quedar grande/pequeño", "быть большим/маленьким", "Me queda grande."),
                        LessonItem("estar de moda", "быть в моде", "Esto está de moda."),
                        LessonItem("pasado de moda", "немодный / устаревший", "")
                    )
                ),
                LessonSection(
                    heading = "В магазине одежды",
                    items = listOf(
                        LessonItem("¿Tiene en la talla M?", "Есть в размере M?", ""),
                        LessonItem("¿Lo tiene en otro color?", "Есть в другом цвете?", ""),
                        LessonItem("el descuento / las rebajas", "скидка / распродажа", "¡Hay rebajas!"),
                        LessonItem("¿Puedo devolver esto?", "Могу вернуть это?", ""),
                        LessonItem("el ticket/recibo", "чек", "Guarda el recibo.")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как спросить «это мне идёт»?",
                    question = "¿Me ___?",
                    options = listOf("queda bien", "está bien", "es bien", "va bien"),
                    correctAnswer = "queda bien",
                    explanation = "quedar bien = идти/подходить. ¿Me queda bien? = мне идёт? Te queda genial = тебе очень идёт."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Me queda grande. Necesito una talla menos.",
                    options = listOf("Мне велико. Нужен размер меньше.", "Мне мало. Нужен размер больше.", "Это мне идёт.", "Мне нравится этот размер."),
                    correctAnswer = "Мне велико. Нужен размер меньше.",
                    explanation = "quedar grande = быть большим. una talla menos = размер меньше."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что такое «las rebajas»?",
                    question = "¡Vamos de rebajas!",
                    options = listOf("Пойдём на распродажу!", "Пойдём за подарками!", "Пойдём в магазин!", "Пойдём за скидкой!"),
                    correctAnswer = "Пойдём на распродажу!",
                    explanation = "las rebajas = распродажа (сезонная). ¡Hay rebajas! = распродажа!"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "¿Lo tienen en otro color?",
                    options = listOf("Есть это в другом цвете?", "Сколько стоит этот цвет?", "Какого цвета эта вещь?", "Мне нравится этот цвет."),
                    correctAnswer = "Есть это в другом цвете?",
                    explanation = "¿Lo tienen en otro color? — стандартный вопрос в магазине одежды."
                )
            )
        ),

        "u6_l12" to LessonContent(
            intro = "Por vs Para — продвинутый уровень",
            sections = listOf(
                LessonSection(
                    heading = "POR — причина, обмен, движение, время",
                    items = listOf(
                        LessonItem("причина / мотив", "Lo hice por amor.", "сделал из-за любви"),
                        LessonItem("обмен", "Te lo cambio por este.", "меняю на это"),
                        LessonItem("движение сквозь", "Pasamos por Madrid.", "проехали через Мадрид"),
                        LessonItem("продолжительность", "Estudié por dos horas.", "учился два часа"),
                        LessonItem("приблизительное место", "Vive por aquí.", "живёт где-то тут"),
                        LessonItem("от имени / вместо", "Firmé por ella.", "подписал вместо неё")
                    )
                ),
                LessonSection(
                    heading = "PARA — цель, получатель, срок, направление",
                    items = listOf(
                        LessonItem("цель", "Estudio para aprender.", "чтобы выучить"),
                        LessonItem("получатель", "Este regalo es para ti.", "этот подарок тебе"),
                        LessonItem("срок", "Lo necesito para el lunes.", "к понедельнику"),
                        LessonItem("направление", "Salgo para Madrid.", "еду в Мадрид"),
                        LessonItem("мнение", "Para mí, es difícil.", "по-моему, сложно"),
                        LessonItem("профессия/назначение", "Es una taza para café.", "чашка для кофе")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери por или para",
                    question = "Este libro es ___ ti.",
                    options = listOf("para", "por", "a", "de"),
                    correctAnswer = "para",
                    explanation = "para + получатель = для кого предназначено. Este libro es para ti = эта книга для тебя."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Lo compré por diez euros.",
                    options = listOf("Я купил это за десять евро.", "Я куплю это для десяти евро.", "Это стоит десять евро.", "Я потратил десять евро."),
                    correctAnswer = "Я купил это за десять евро.",
                    explanation = "por + сумма = за (обмен). compré por diez euros = купил за десять евро."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный предлог",
                    question = "Necesito el informe ___ el viernes.",
                    options = listOf("para", "por", "en", "a"),
                    correctAnswer = "para",
                    explanation = "para + срок = к (дедлайн). para el viernes = к пятнице."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Pasamos por el centro histórico.",
                    options = listOf("Мы проехали через исторический центр.", "Мы остановились в центре.", "Мы живём в центре.", "Центр нам понравился."),
                    correctAnswer = "Мы проехали через исторический центр.",
                    explanation = "por + место = движение через/по. pasar por = проходить/проезжать через."
                )
            )
        ),

        "u6_l13" to LessonContent(
            intro = "Эмоции и чувства на испанском",
            sections = listOf(
                LessonSection(
                    heading = "Основные эмоции",
                    items = listOf(
                        LessonItem("la alegría / alegre", "радость / радостный", "Estoy alegre."),
                        LessonItem("la tristeza / triste", "грусть / грустный", "Estoy triste."),
                        LessonItem("el miedo / asustado/a", "страх / испуганный", "Tengo miedo."),
                        LessonItem("la sorpresa / sorprendido/a", "удивление / удивлённый", "Estoy sorprendido."),
                        LessonItem("el enfado / enfadado/a", "злость / злой", "Estoy enfadado."),
                        LessonItem("la vergüenza / avergonzado/a", "стыд / смущённый", "Tengo vergüenza."),
                        LessonItem("el orgullo / orgulloso/a", "гордость / гордый", "Estoy orgulloso.")
                    )
                ),
                LessonSection(
                    heading = "Выражение эмоций",
                    items = listOf(
                        LessonItem("Estoy + adj", "Я ... (временное состояние)", "Estoy nervioso."),
                        LessonItem("Tengo + noun", "У меня ... ", "Tengo miedo / vergüenza."),
                        LessonItem("Me siento + adj", "Я чувствую себя ...", "Me siento feliz."),
                        LessonItem("Me pone nervioso", "Это меня нервирует", "El tráfico me pone nervioso.")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «я боюсь»?",
                    question = "___ miedo.",
                    options = listOf("Tengo", "Estoy", "Me siento", "Soy"),
                    correctAnswer = "Tengo",
                    explanation = "tener miedo = бояться (буквально «иметь страх»). Tengo miedo = я боюсь."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Estoy muy emocionado por el viaje.",
                    options = listOf("Я очень взволнован путешествием.", "Я устал от путешествия.", "Путешествие меня раздражает.", "Я рад вернуться."),
                    correctAnswer = "Я очень взволнован путешествием.",
                    explanation = "emocionado = взволнованный/возбуждённый (в хорошем смысле). por = из-за."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что значит «vergüenza»?",
                    question = "Tengo vergüenza de hablar en público.",
                    options = listOf("Мне стыдно говорить публично.", "Я боюсь говорить публично.", "Мне грустно говорить публично.", "Я рад говорить публично."),
                    correctAnswer = "Мне стыдно говорить публично.",
                    explanation = "vergüenza = стыд/смущение. tener vergüenza = стыдиться."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный вариант",
                    question = "Me ___ feliz cuando estoy con mis amigos.",
                    options = listOf("siento", "estoy", "tengo", "soy"),
                    correctAnswer = "siento",
                    explanation = "sentirse = чувствовать себя. Me siento feliz = я чувствую себя счастливым."
                )
            )
        ),

        // ══════════════════════════════════════════════
        //  A2 БЛОК 3 (unitId=7)
        // ══════════════════════════════════════════════

        "u7_l0" to LessonContent(
            intro = "Pretérito Perfecto: действия связанные с настоящим",
            sections = listOf(
                LessonSection(
                    heading = "Образование: haber + participio",
                    items = listOf(
                        LessonItem("yo he", "hablado / comido / vivido", "я говорил/ел/жил"),
                        LessonItem("tú has", "hablado / comido / vivido", "ты говорил..."),
                        LessonItem("él/ella ha", "hablado / comido / vivido", "он/она..."),
                        LessonItem("nosotros hemos", "hablado / comido / vivido", "мы..."),
                        LessonItem("vosotros habéis", "hablado / comido / vivido", "вы..."),
                        LessonItem("ellos han", "hablado / comido / vivido", "они...")
                    )
                ),
                LessonSection(
                    heading = "Когда используется",
                    items = listOf(
                        LessonItem("hoy", "сегодня (день ещё не закончен)", "Hoy he comido pizza."),
                        LessonItem("esta semana/mañana", "на этой неделе/утром", "Esta semana he trabajado mucho."),
                        LessonItem("ya / todavía no", "уже / ещё не", "¿Ya has comido? — Todavía no."),
                        LessonItem("alguna vez / nunca", "когда-нибудь / никогда", "¿Has estado en Madrid alguna vez?")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Образуй Pretérito Perfecto: yo + comer",
                    question = "Hoy ___ paella.",
                    options = listOf("he comido", "comí", "como", "había comido"),
                    correctAnswer = "he comido",
                    explanation = "he + comido = P. Perfecto. hoy = сегодня (день не закончен) → P. Perfecto."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "¿Has estado alguna vez en Rusia?",
                    options = listOf("Ты когда-нибудь был в России?", "Ты был в России вчера?", "Ты живёшь в России?", "Ты едешь в Россию?"),
                    correctAnswer = "Ты когда-нибудь был в России?",
                    explanation = "alguna vez = когда-нибудь. ¿Has estado? = ты был? (P. Perfecto от estar)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Esta semana ___ mucho.",
                    options = listOf("he trabajado", "trabajé", "trabajo", "trabajaba"),
                    correctAnswer = "he trabajado",
                    explanation = "esta semana = на этой неделе (ещё идёт) → P. Perfecto."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Todavía no he desayunado.",
                    options = listOf("Я ещё не завтракал.", "Я уже позавтракал.", "Я никогда не завтракаю.", "Я не хочу завтракать."),
                    correctAnswer = "Я ещё не завтракал.",
                    explanation = "todavía no + P. Perfecto = ещё не (действие ожидается)."
                )
            )
        ),

        "u7_l1" to LessonContent(
            intro = "Participios irregulares — неправильные причастия",
            sections = listOf(
                LessonSection(
                    heading = "Самые важные неправильные participios",
                    items = listOf(
                        LessonItem("hacer → hecho", "делать → сделанный", "He hecho la tarea."),
                        LessonItem("decir → dicho", "говорить → сказанный", "¿Qué has dicho?"),
                        LessonItem("ver → visto", "видеть → увиденный", "He visto esa película."),
                        LessonItem("escribir → escrito", "писать → написанный", "He escrito un email."),
                        LessonItem("volver → vuelto", "возвращаться → вернувшийся", "Ha vuelto a casa."),
                        LessonItem("abrir → abierto", "открывать → открытый", "Han abierto la tienda."),
                        LessonItem("poner → puesto", "класть → положенный", "He puesto la mesa."),
                        LessonItem("romper → roto", "ломать → сломанный", "He roto el vaso.")
                    )
                ),
                LessonSection(
                    heading = "Ещё важные",
                    items = listOf(
                        LessonItem("morir → muerto", "умирать → умерший", ""),
                        LessonItem("cubrir → cubierto", "покрывать → покрытый", ""),
                        LessonItem("descubrir → descubierto", "открывать → открытый", "He descubierto algo."),
                        LessonItem("resolver → resuelto", "решать → решённый", "He resuelto el problema.")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Participio от HACER",
                    question = "He ___ los deberes.",
                    options = listOf("hecho", "hacido", "hachado", "haciendo"),
                    correctAnswer = "hecho",
                    explanation = "hacer → hecho (неправильное). He hecho = я сделал."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "¿Has visto esa serie?",
                    options = listOf("Ты видел этот сериал?", "Ты смотришь этот сериал?", "Ты будешь смотреть сериал?", "Сериал хороший?"),
                    correctAnswer = "Ты видел этот сериал?",
                    explanation = "ver → visto. ¿Has visto? = ты видел? (P. Perfecto)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Participio от ESCRIBIR",
                    question = "He ___ una carta.",
                    options = listOf("escrito", "escribido", "escribto", "escrito"),
                    correctAnswer = "escrito",
                    explanation = "escribir → escrito (неправильное). He escrito = я написал."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильное причастие",
                    question = "Han ___ el museo a las 9.",
                    options = listOf("abierto", "abrido", "abriendo", "abre"),
                    correctAnswer = "abierto",
                    explanation = "abrir → abierto (неправильное). Han abierto = они открыли."
                )
            )
        ),

        "u7_l2" to LessonContent(
            intro = "Perfecto vs Indefinido — когда что выбрать",
            sections = listOf(
                LessonSection(
                    heading = "Ключевое различие",
                    items = listOf(
                        LessonItem("P. Perfecto", "связь с настоящим", "Hoy he comido. (сегодня — день идёт)"),
                        LessonItem("P. Indefinido", "полностью в прошлом", "Ayer comí. (вчера — день закончен)"),
                        LessonItem("В Испании", "различие важно", "hoy→Perfecto, ayer→Indefinido"),
                        LessonItem("В Латин. Америке", "Perfecto редко", "чаще используют только Indefinido")
                    )
                ),
                LessonSection(
                    heading = "Маркеры",
                    items = listOf(
                        LessonItem("→ Perfecto", "hoy, esta semana, este mes, ya, todavía no, alguna vez, nunca, hace poco", ""),
                        LessonItem("→ Indefinido", "ayer, anteayer, el lunes, el año pasado, hace dos años, en 2020", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильное время",
                    question = "Este mes ___ tres libros.",
                    options = listOf("he leído", "leí", "leo", "leía"),
                    correctAnswer = "he leído",
                    explanation = "este mes = в этом месяце (ещё идёт) → P. Perfecto."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильное время",
                    question = "El año pasado ___ a París.",
                    options = listOf("fui", "he ido", "voy", "iba"),
                    correctAnswer = "fui",
                    explanation = "el año pasado = в прошлом году (закончено) → P. Indefinido."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи правильно",
                    question = "Nunca he probado el sushi.",
                    options = listOf("Я никогда не пробовал суши.", "Я вчера не пробовал суши.", "Я не пробую суши.", "Раньше я не ел суши."),
                    correctAnswer = "Я никогда не пробовал суши.",
                    explanation = "nunca + P. Perfecto = никогда не (опыт в жизни)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильное время",
                    question = "¿___ (tú) alguna vez flamenco?",
                    options = listOf("Has bailado", "Bailaste", "Bailas", "Bailabas"),
                    correctAnswer = "Has bailado",
                    explanation = "alguna vez = когда-нибудь → P. Perfecto. ¿Has bailado alguna vez? = ты когда-нибудь танцевал фламенко?"
                )
            )
        ),

        "u7_l3" to LessonContent(
            intro = "Ya, todavía, aún — уже, ещё, до сих пор",
            sections = listOf(
                LessonSection(
                    heading = "YA — уже",
                    items = listOf(
                        LessonItem("Ya he comido.", "Я уже поел.", "+ P. Perfecto"),
                        LessonItem("Ya lo sé.", "Я уже знаю.", "+ настоящее"),
                        LessonItem("¿Ya has llegado?", "Ты уже приехал?", "вопрос"),
                        LessonItem("Ya no trabajo aquí.", "Я больше здесь не работаю.", "ya no = больше не")
                    )
                ),
                LessonSection(
                    heading = "TODAVÍA / AÚN — ещё / до сих пор",
                    items = listOf(
                        LessonItem("Todavía no he comido.", "Я ещё не поел.", "todavía no = ещё не"),
                        LessonItem("Todavía vivo aquí.", "Я до сих пор живу здесь.", "действие продолжается"),
                        LessonItem("¿Todavía estudias?", "Ты ещё учишься?", ""),
                        LessonItem("Aún no está listo.", "Ещё не готово.", "aún = todavía")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "¿Ya has terminado?",
                    options = listOf("Ты уже закончил?", "Ты ещё не закончил?", "Ты закончишь?", "Когда закончишь?"),
                    correctAnswer = "Ты уже закончил?",
                    explanation = "ya = уже. ¿Ya has terminado? = ты уже закончил?"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «я ещё не готов»?",
                    question = "___ no estoy listo.",
                    options = listOf("Todavía", "Ya", "Siempre", "Nunca"),
                    correctAnswer = "Todavía",
                    explanation = "todavía no = ещё не. Todavía no estoy listo = я ещё не готов."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Ya no me gusta ese programa.",
                    options = listOf("Мне больше не нравится эта программа.", "Мне ещё нравится программа.", "Мне уже нравится программа.", "Эта программа мне никогда не нравилась."),
                    correctAnswer = "Мне больше не нравится эта программа.",
                    explanation = "ya no = больше не (действие прекратилось)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильное слово",
                    question = "¿___ vives en ese barrio?",
                    options = listOf("Todavía", "Ya no", "Nunca", "Siempre"),
                    correctAnswer = "Todavía",
                    explanation = "todavía = до сих пор / ещё. ¿Todavía vives ahí? = ты до сих пор живёшь там?"
                )
            )
        ),

        "u7_l4" to LessonContent(
            intro = "Estar + gerundio — действие прямо сейчас",
            sections = listOf(
                LessonSection(
                    heading = "Образование герундия",
                    items = listOf(
                        LessonItem("-AR → -ando", "hablar → hablando", "говорящий / говоря"),
                        LessonItem("-ER/-IR → -iendo", "comer → comiendo", "едящий / едя"),
                        LessonItem("-IR → -iendo", "vivir → viviendo", ""),
                        LessonItem("ir → yendo", "идти → идя", "неправильный"),
                        LessonItem("leer → leyendo", "читать → читая", "неправильный"),
                        LessonItem("dormir → durmiendo", "спать → спя", "неправильный")
                    )
                ),
                LessonSection(
                    heading = "Estar + gerundio = сейчас",
                    items = listOf(
                        LessonItem("Estoy comiendo.", "Я ем (сейчас).", ""),
                        LessonItem("Está durmiendo.", "Он/она спит (сейчас).", ""),
                        LessonItem("¿Qué estás haciendo?", "Что ты делаешь (сейчас)?", ""),
                        LessonItem("Estamos estudiando.", "Мы учимся (сейчас).", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Образуй герундий: trabajar",
                    question = "Estoy ___ en casa.",
                    options = listOf("trabajando", "trabajado", "trabajar", "trabajo"),
                    correctAnswer = "trabajando",
                    explanation = "trabajar → trabajando (-AR → -ando). Estoy trabajando = я работаю (сейчас)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "¿Qué estás haciendo?",
                    options = listOf("Что ты делаешь (сейчас)?", "Что ты делал?", "Что ты будешь делать?", "Что ты сделал?"),
                    correctAnswer = "Что ты делаешь (сейчас)?",
                    explanation = "estar + gerundio = действие в процессе прямо сейчас."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Неправильный герундий: leer",
                    question = "Está ___ un libro muy interesante.",
                    options = listOf("leyendo", "leendo", "leído", "leer"),
                    correctAnswer = "leyendo",
                    explanation = "leer → leyendo (неправильный). -iendo → leyendo (i → y между гласными)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Los niños están durmiendo.",
                    options = listOf("Дети спят (сейчас).", "Дети спали.", "Дети будут спать.", "Дети хотят спать."),
                    correctAnswer = "Дети спят (сейчас).",
                    explanation = "dormir → durmiendo (неправильный). están durmiendo = они спят прямо сейчас."
                )
            )
        ),

        "u7_l5" to LessonContent(
            intro = "Seguir + gerundio и Llevar + gerundio",
            sections = listOf(
                LessonSection(
                    heading = "SEGUIR + gerundio — продолжать делать",
                    items = listOf(
                        LessonItem("Sigo estudiando.", "Я продолжаю учиться.", ""),
                        LessonItem("Sigue lloviendo.", "Дождь всё ещё идёт.", ""),
                        LessonItem("¿Sigues viviendo aquí?", "Ты всё ещё живёшь здесь?", ""),
                        LessonItem("Seguimos trabajando juntos.", "Мы продолжаем работать вместе.", "")
                    )
                ),
                LessonSection(
                    heading = "LLEVAR + gerundio — вот уже ... как",
                    items = listOf(
                        LessonItem("Llevo dos horas esperando.", "Я жду уже два часа.", ""),
                        LessonItem("Lleva un año estudiando.", "Он учится уже год.", ""),
                        LessonItem("¿Cuánto llevas esperando?", "Сколько ты уже ждёшь?", ""),
                        LessonItem("Llevamos tres años casados.", "Мы женаты уже три года.", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Sigo sin entender.",
                    options = listOf("Я по-прежнему не понимаю.", "Я понял.", "Я продолжаю понимать.", "Я начал понимать."),
                    correctAnswer = "Я по-прежнему не понимаю.",
                    explanation = "seguir sin + infinitivo = по-прежнему не делать. Sigo sin entender = я до сих пор не понимаю."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Llevo tres horas estudiando.",
                    options = listOf("Я учусь уже три часа.", "Я учился три часа.", "Через три часа начну учиться.", "Я учился три раза."),
                    correctAnswer = "Я учусь уже три часа.",
                    explanation = "llevar + время + gerundio = действие длится уже ... Llevo tres horas = уже три часа."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный глагол",
                    question = "___ trabajando en la misma empresa desde 2018.",
                    options = listOf("Sigo", "Llevo", "Estoy", "He"),
                    correctAnswer = "Sigo",
                    explanation = "seguir + gerundio = продолжать делать. Sigo trabajando = я продолжаю работать."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как спросить «сколько ты уже ждёшь»?",
                    question = "¿Cuánto ___ esperando?",
                    options = listOf("llevas", "sigues", "estás", "has"),
                    correctAnswer = "llevas",
                    explanation = "llevar + gerundio = вот уже ... как. ¿Cuánto llevas esperando? = сколько ты уже ждёшь?"
                )
            )
        ),

        "u7_l6" to LessonContent(
            intro = "Работа и карьера — лексика",
            sections = listOf(
                LessonSection(
                    heading = "Работа и поиск",
                    items = listOf(
                        LessonItem("buscar empleo/trabajo", "искать работу", ""),
                        LessonItem("el currículum (vitae)", "резюме", "enviar el currículum"),
                        LessonItem("la entrevista de trabajo", "собеседование", "tener una entrevista"),
                        LessonItem("el sueldo / el salario", "зарплата", "¿Cuánto es el sueldo?"),
                        LessonItem("el contrato", "контракт", "firmar el contrato"),
                        LessonItem("la jornada completa/parcial", "полный/неполный рабочий день", ""),
                        LessonItem("el teletrabajo", "удалённая работа", "Trabajo en teletrabajo.")
                    )
                ),
                LessonSection(
                    heading = "На работе",
                    items = listOf(
                        LessonItem("el/la jefe/a", "начальник/ца", ""),
                        LessonItem("el/la compañero/a", "коллега", "mis compañeros de trabajo"),
                        LessonItem("la reunión", "совещание", "Tengo una reunión."),
                        LessonItem("el plazo", "срок / дедлайн", ""),
                        LessonItem("ascender", "повышать по службе", "Me han ascendido."),
                        LessonItem("despedir", "увольнять", "Lo han despedido.")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «собеседование»?",
                    question = "Mañana tengo una ___ de trabajo.",
                    options = listOf("entrevista", "reunión", "contrato", "currículum"),
                    correctAnswer = "entrevista",
                    explanation = "la entrevista de trabajo = собеседование."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Busco trabajo a jornada completa.",
                    options = listOf("Я ищу работу на полный день.", "Я работаю полный день.", "У меня полный рабочий день.", "Я хочу работать меньше."),
                    correctAnswer = "Я ищу работу на полный день.",
                    explanation = "buscar trabajo = искать работу. jornada completa = полный рабочий день."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что значит «Me han ascendido»?",
                    question = "Me han ascendido en el trabajo.",
                    options = listOf("Меня повысили на работе.", "Меня уволили.", "Меня нашли на работе.", "Я устроился на работу."),
                    correctAnswer = "Меня повысили на работе.",
                    explanation = "ascender = повышать. Me han ascendido = меня повысили (P. Perfecto пассивное значение)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как называется «удалённая работа»?",
                    question = "удалённая работа = ___",
                    options = listOf("el teletrabajo", "la jornada parcial", "el contrato", "el sueldo"),
                    correctAnswer = "el teletrabajo",
                    explanation = "el teletrabajo = удалённая работа / дистанционная работа."
                )
            )
        ),

        "u7_l7" to LessonContent(
            intro = "Imperativo afirmativo — даём команды и советы",
            sections = listOf(
                LessonSection(
                    heading = "Imperativo: tú (неформально)",
                    items = listOf(
                        LessonItem("hablar → habla", "говори", "¡Habla más despacio!"),
                        LessonItem("comer → come", "ешь", "¡Come la verdura!"),
                        LessonItem("escribir → escribe", "пиши", "¡Escribe tu nombre!"),
                        LessonItem("ir → ve", "иди", "¡Ve a casa!"),
                        LessonItem("ser → sé", "будь", "¡Sé amable!"),
                        LessonItem("tener → ten", "имей/возьми", "¡Ten cuidado!"),
                        LessonItem("venir → ven", "приходи", "¡Ven aquí!")
                    )
                ),
                LessonSection(
                    heading = "Imperativo: usted (формально)",
                    items = listOf(
                        LessonItem("hablar → hable", "говорите", "¡Hable más despacio!"),
                        LessonItem("comer → coma", "ешьте", ""),
                        LessonItem("ir → vaya", "идите", "¡Vaya todo recto!"),
                        LessonItem("Местоимения — после глагола", "¡Dímelo! ¡Espérame!", "присоединяются слитно")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Образуй imperativo tú: escuchar",
                    question = "¡___ bien!",
                    options = listOf("Escucha", "Escuche", "Escuchas", "Escuchad"),
                    correctAnswer = "Escucha",
                    explanation = "escuchar → escucha (imperativo tú). Как форма él/ella в presente."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Неправильный imperativo: venir",
                    question = "¡___ aquí ahora mismo!",
                    options = listOf("Ven", "Viene", "Venir", "Venid"),
                    correctAnswer = "Ven",
                    explanation = "venir → ven (неправильный императив). Запомни: di, haz, ve, pon, sal, sé, ten, ven."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "¡Ten cuidado!",
                    options = listOf("Осторожно! / Будь осторожен!", "Приходи сюда!", "Иди домой!", "Садись!"),
                    correctAnswer = "Осторожно! / Будь осторожен!",
                    explanation = "tener cuidado = быть осторожным. ten (imperative от tener) + cuidado."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Формальный императив: hablar",
                    question = "Por favor, ___ más despacio. (usted)",
                    options = listOf("hable", "habla", "hablar", "hablad"),
                    correctAnswer = "hable",
                    explanation = "hablar → hable (imperativo usted). Формальное обращение — окончание меняется."
                )
            )
        ),

        "u7_l8" to LessonContent(
            intro = "Imperativo negativo — запрещаем и предостерегаем",
            sections = listOf(
                LessonSection(
                    heading = "Образование: no + subjuntivo presente",
                    items = listOf(
                        LessonItem("hablar → no hables (tú)", "не говори", "¡No hables tan rápido!"),
                        LessonItem("comer → no comas (tú)", "не ешь", "¡No comas eso!"),
                        LessonItem("ir → no vayas (tú)", "не ходи", "¡No vayas allí!"),
                        LessonItem("ser → no seas (tú)", "не будь", "¡No seas impaciente!"),
                        LessonItem("hablar → no hable (usted)", "не говорите", "¡No hable tan fuerte!"),
                        LessonItem("ir → no vaya (usted)", "не идите", "")
                    )
                ),
                LessonSection(
                    heading = "Местоимения в отрицательном императиве",
                    items = listOf(
                        LessonItem("Affirmativo", "Dímelo. — Скажи мне это.", "местоимение после"),
                        LessonItem("Negativo", "No me lo digas. — Не говори мне.", "местоимение ДО"),
                        LessonItem("Tócalo. ↔ No lo toques.", "Потрогай. ↔ Не трогай.", ""),
                        LessonItem("Cómpralos. ↔ No los compres.", "Купи. ↔ Не покупай.", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Образуй отрицательный императив: hablar (tú)",
                    question = "¡No ___ en clase!",
                    options = listOf("hables", "hablas", "habla", "habléis"),
                    correctAnswer = "hables",
                    explanation = "imperativo negativo tú: no + subjuntivo → no hables."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "¡No seas tan impaciente!",
                    options = listOf("Не будь таким нетерпеливым!", "Не говори так быстро!", "Не уходи!", "Не будь так громко!"),
                    correctAnswer = "Не будь таким нетерпеливым!",
                    explanation = "ser → no seas (отриц. императив). impaciente = нетерпеливый."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Местоимения в отрицательном императиве",
                    question = "No ___ (decirme la verdad).",
                    options = listOf("me digas la verdad", "digas me la verdad", "dígame la verdad", "me diga la verdad"),
                    correctAnswer = "me digas la verdad",
                    explanation = "В отрицательном императиве местоимение стоит ДО глагола: no me digas."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Преобразуй в отрицательный",
                    question = "Cómpralo. → No ___.",
                    options = listOf("lo compres", "lo compras", "compres lo", "lo compre"),
                    correctAnswer = "lo compres",
                    explanation = "Cómpralo → No lo compres. Местоимение переходит ДО глагола: no lo compres."
                )
            )
        ),

        "u7_l9" to LessonContent(
            intro = "У врача — симптомы и медицинский визит",
            sections = listOf(
                LessonSection(
                    heading = "Симптомы",
                    items = listOf(
                        LessonItem("Me duele la cabeza.", "У меня болит голова.", ""),
                        LessonItem("Tengo fiebre (38°).", "У меня температура 38.", ""),
                        LessonItem("Tengo tos y mocos.", "У меня кашель и насморк.", ""),
                        LessonItem("Me duele la garganta.", "У меня болит горло.", ""),
                        LessonItem("Estoy mareado/a.", "У меня кружится голова.", ""),
                        LessonItem("Tengo náuseas.", "Меня тошнит.", ""),
                        LessonItem("Me he torcido el tobillo.", "Я подвернул лодыжку.", "")
                    )
                ),
                LessonSection(
                    heading = "Диалог с врачом",
                    items = listOf(
                        LessonItem("¿Qué le pasa?", "Что вас беспокоит?", "врач"),
                        LessonItem("¿Desde cuándo?", "С каких пор?", "врач"),
                        LessonItem("Desde hace tres días.", "Уже три дня.", "пациент"),
                        LessonItem("Le receto...", "Я вам выпишу...", "врач"),
                        LessonItem("Tome dos pastillas al día.", "Принимайте по 2 таблетки в день.", ""),
                        LessonItem("Guarde cama.", "Соблюдайте постельный режим.", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «у меня болит горло»?",
                    question = "Me duele ___.",
                    options = listOf("la garganta", "el estómago", "la cabeza", "el brazo"),
                    correctAnswer = "la garganta",
                    explanation = "la garganta = горло. Me duele la garganta = у меня болит горло."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Врач спрашивает. Переведи",
                    question = "¿Desde cuándo tiene estos síntomas?",
                    options = listOf("С каких пор у вас эти симптомы?", "Какие у вас симптомы?", "Как давно вы болеете?", "Что вас беспокоит?"),
                    correctAnswer = "С каких пор у вас эти симптомы?",
                    explanation = "¿Desde cuándo? = с каких пор? / с когда?"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Estoy mareado y tengo náuseas.",
                    options = listOf("У меня кружится голова и тошнит.", "У меня болит голова и кашель.", "Я устал и у меня насморк.", "Мне плохо и я боюсь."),
                    correctAnswer = "У меня кружится голова и тошнит.",
                    explanation = "mareado = с головокружением. náuseas = тошнота."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что значит «Guarde cama»?",
                    question = "El médico dice: «Guarde cama».",
                    options = listOf("Соблюдайте постельный режим.", "Выпейте таблетку.", "Идите домой.", "Отдыхайте больше."),
                    correctAnswer = "Соблюдайте постельный режим.",
                    explanation = "guardar cama = соблюдать постельный режим (буквально «хранить кровать»)."
                )
            )
        ),

        "u7_l10" to LessonContent(
            intro = "OD + OI вместе: te lo doy, se lo digo",
            sections = listOf(
                LessonSection(
                    heading = "Порядок местоимений",
                    items = listOf(
                        LessonItem("OI + OD + глагол", "me lo, te lo, se lo...", "сначала OI, потом OD"),
                        LessonItem("Te lo doy.", "Я даю тебе это.", "te=тебе, lo=это"),
                        LessonItem("Me la explica.", "Он объясняет мне это.", "me=мне, la=её"),
                        LessonItem("Nos los manda.", "Он отправляет нам их.", "nos=нам, los=их")
                    )
                ),
                LessonSection(
                    heading = "le/les → se перед lo/la/los/las",
                    items = listOf(
                        LessonItem("le + lo → se lo", "ему это", "Se lo digo. (не le lo)"),
                        LessonItem("les + la → se la", "им её", "Se la mando."),
                        LessonItem("¿A quién?", "Уточнение через a", "Se lo digo a mi madre."),
                        LessonItem("Con imperativo", "после глагола слитно", "¡Dáselo! = Отдай ему это!")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Замени: Doy el libro a ti.",
                    question = "___ doy.",
                    options = listOf("Te lo", "Lo te", "Me lo", "Se lo"),
                    correctAnswer = "Te lo",
                    explanation = "te = тебе (OI), lo = el libro (OD). Порядок: OI + OD → Te lo doy."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "le + lo = ?",
                    question = "Digo la verdad a él. → ___ digo.",
                    options = listOf("Se la", "Le la", "Lo le", "Se lo"),
                    correctAnswer = "Se la",
                    explanation = "le (ему) + la (la verdad) → se la. le/les всегда меняется на se перед lo/la/los/las."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "¿Me lo puedes explicar?",
                    options = listOf("Можешь мне это объяснить?", "Ты объяснишь мне это?", "Я могу тебе объяснить?", "Ты понимаешь меня?"),
                    correctAnswer = "Можешь мне это объяснить?",
                    explanation = "me = мне (OI), lo = это (OD). ¿Me lo puedes explicar? = можешь мне объяснить?"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Императив с местоимениями",
                    question = "Da el regalo a ella. → ¡___!",
                    options = listOf("¡Dáselo!", "¡Se lo da!", "¡Dalo se!", "¡Le dáselo!"),
                    correctAnswer = "¡Dáselo!",
                    explanation = "da + se + lo → Dáselo (слитно, ударение сохраняется). le → se перед lo."
                )
            )
        ),

        "u7_l11" to LessonContent(
            intro = "В путешествии — лексика отеля, билетов, туризма",
            sections = listOf(
                LessonSection(
                    heading = "Транспорт и билеты",
                    items = listOf(
                        LessonItem("el billete / el boleto", "билет", "un billete de ida y vuelta"),
                        LessonItem("ida y vuelta", "туда и обратно", ""),
                        LessonItem("solo de ida", "только в одну сторону", ""),
                        LessonItem("el vuelo", "рейс / полёт", "El vuelo sale a las 10."),
                        LessonItem("la salida / la llegada", "отправление / прибытие", ""),
                        LessonItem("facturar el equipaje", "сдать багаж", ""),
                        LessonItem("el equipaje de mano", "ручная кладь", "")
                    )
                ),
                LessonSection(
                    heading = "В отеле",
                    items = listOf(
                        LessonItem("la reserva", "бронирование", "Tengo una reserva."),
                        LessonItem("la habitación individual/doble", "одноместный/двухместный номер", ""),
                        LessonItem("el desayuno incluido", "завтрак включён", ""),
                        LessonItem("hacer el check-in/out", "заселиться / выселиться", ""),
                        LessonItem("¿A qué hora es el check-out?", "В котором часу выезд?", ""),
                        LessonItem("el pasaporte / el DNI", "паспорт / ID", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «билет туда-обратно»?",
                    question = "Quiero un billete de ___.",
                    options = listOf("ida y vuelta", "solo de ida", "vuelta sola", "ida o vuelta"),
                    correctAnswer = "ida y vuelta",
                    explanation = "ida y vuelta = туда и обратно. solo de ida = только в одну сторону."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "В отеле: что значит «la reserva»?",
                    question = "Tengo una reserva a nombre de García.",
                    options = listOf("У меня бронирование на имя Гарсия.", "У меня комната для Гарсия.", "Я резервирую на Гарсия.", "Мне нужна комната."),
                    correctAnswer = "У меня бронирование на имя Гарсия.",
                    explanation = "la reserva = бронирование. a nombre de = на имя."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "¿El desayuno está incluido?",
                    options = listOf("Завтрак включён?", "Во сколько завтрак?", "Где завтрак?", "Какой завтрак?"),
                    correctAnswer = "Завтрак включён?",
                    explanation = "incluido = включённый. ¿Está incluido? = включено? Важный вопрос в отеле!"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что такое «el equipaje de mano»?",
                    question = "el equipaje de mano = ?",
                    options = listOf("ручная кладь", "чемодан", "рюкзак", "багажное отделение"),
                    correctAnswer = "ручная кладь",
                    explanation = "equipaje de mano = ручная кладь (то, что берёшь в салон). facturar el equipaje = сдать багаж в багажное отделение."
                )
            )
        ),

        "u7_l12" to LessonContent(
            intro = "Придаточные предложения с QUE",
            sections = listOf(
                LessonSection(
                    heading = "Глаголы мнения + que",
                    items = listOf(
                        LessonItem("creer que", "думать / считать что", "Creo que tienes razón."),
                        LessonItem("pensar que", "думать что", "Pienso que es difícil."),
                        LessonItem("opinar que", "считать что", "Opino que es importante."),
                        LessonItem("saber que", "знать что", "Sé que estás cansado."),
                        LessonItem("decir que", "говорить что", "Dice que viene mañana.")
                    )
                ),
                LessonSection(
                    heading = "Глаголы чувств + que",
                    items = listOf(
                        LessonItem("esperar que", "надеяться что", "Espero que llegues pronto."),
                        LessonItem("alegrarse de que", "радоваться что", "Me alegra que estés bien."),
                        LessonItem("tener miedo de que", "бояться что", "Tengo miedo de que se enfade."),
                        LessonItem("¡Qué bien que...!", "Как хорошо что...!", "¡Qué bien que hayas venido!")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Creo que llueve.",
                    options = listOf("Я думаю, что идёт дождь.", "Я знаю, что идёт дождь.", "Я хочу, чтобы шёл дождь.", "Мне кажется, что я ошибся."),
                    correctAnswer = "Я думаю, что идёт дождь.",
                    explanation = "creer que = думать что. Creo que = я думаю что."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Составь предложение",
                    question = "Sé ___ estás ocupado.",
                    options = listOf("que", "si", "porque", "cuando"),
                    correctAnswer = "que",
                    explanation = "saber que = знать что. Sé que estás ocupado = я знаю, что ты занят."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Pienso que el español es interesante.",
                    options = listOf("Я думаю, что испанский интересный.", "Мне нравится испанский.", "Испанский — интересный язык.", "Я учу интересный испанский."),
                    correctAnswer = "Я думаю, что испанский интересный.",
                    explanation = "pensar que = думать что. Pienso que = я думаю что."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Dice que no puede venir.",
                    options = listOf("Он говорит, что не может прийти.", "Он не хочет приходить.", "Скажи ему, чтобы пришёл.", "Он говорит прийти."),
                    correctAnswer = "Он говорит, что не может прийти.",
                    explanation = "decir que = говорить что. Dice que = он говорит что."
                )
            )
        ),

        "u7_l13" to LessonContent(
            intro = "Испанская гастрономия — tapas, paella, tradición",
            sections = listOf(
                LessonSection(
                    heading = "Типичные блюда",
                    items = listOf(
                        LessonItem("las tapas", "тапас (закуски)", "Vamos de tapas."),
                        LessonItem("la paella", "паэлья (рис с морепродуктами)", ""),
                        LessonItem("la tortilla española", "испанский омлет с картошкой", ""),
                        LessonItem("el gazpacho", "гаспачо (холодный суп)", ""),
                        LessonItem("el jamón ibérico", "иберийский хамон", ""),
                        LessonItem("la sangría", "сангрия (напиток)", ""),
                        LessonItem("el churro", "чурро (жареное тесто)", "churros con chocolate")
                    )
                ),
                LessonSection(
                    heading = "Традиции еды в Испании",
                    items = listOf(
                        LessonItem("ir de tapas", "идти на тапас (бар-хоппинг)", ""),
                        LessonItem("la sobremesa", "беседа после еды за столом", "культурная традиция"),
                        LessonItem("el menú del día", "бизнес-ланч (1-е, 2-е, десерт)", "самый дешёвый обед"),
                        LessonItem("cenar tarde", "ужинать поздно (21–22 ч)", "испанская традиция"),
                        LessonItem("picar algo", "перекусить", "¿Picamos algo?")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что такое «la tortilla española»?",
                    question = "La tortilla española es...",
                    options = listOf("омлет с картошкой", "мексиканская лепёшка", "блин с начинкой", "суп"),
                    correctAnswer = "омлет с картошкой",
                    explanation = "La tortilla española = испанский омлет с картофелем и яйцами. Не путай с мексиканской tortilla!"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "¿Vamos de tapas esta tarde?",
                    options = listOf("Пойдём на тапас сегодня вечером?", "Пойдём в ресторан?", "Хочешь поесть дома?", "Закажем доставку?"),
                    correctAnswer = "Пойдём на тапас сегодня вечером?",
                    explanation = "ir de tapas = идти в бары есть тапас. Типичная испанская традиция."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что такое «el menú del día»?",
                    question = "el menú del día = ?",
                    options = listOf("комплексный обед (1е+2е+десерт)", "меню ресторана", "блюдо дня", "завтрак"),
                    correctAnswer = "комплексный обед (1е+2е+десерт)",
                    explanation = "el menú del día = комплексный обед в будни. Включает первое, второе, десерт и напиток. Очень выгодно!"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "En España se cena muy tarde.",
                    options = listOf("В Испании ужинают очень поздно.", "В Испании завтракают рано.", "В Испании обедают в обед.", "В Испании едят мало."),
                    correctAnswer = "В Испании ужинают очень поздно.",
                    explanation = "cenar = ужинать. tarde = поздно. se cena = ужинают (безличная конструкция)."
                )
            )
        ),

        // ══════════════════════════════════════════════
        //  A2 БЛОК 4 (unitId=8)
        // ══════════════════════════════════════════════

        "u8_l0" to LessonContent(
            intro = "Futuro Simple: говорим о будущем",
            sections = listOf(
                LessonSection(
                    heading = "Окончания Futuro Simple (все глаголы)",
                    items = listOf(
                        LessonItem("yo", "-é → hablaré", "я буду говорить"),
                        LessonItem("tú", "-ás → hablarás", "ты будешь говорить"),
                        LessonItem("él/ella", "-á → hablará", "он/она будет говорить"),
                        LessonItem("nosotros", "-emos → hablaremos", "мы будем говорить"),
                        LessonItem("vosotros", "-éis → hablaréis", "вы будете говорить"),
                        LessonItem("ellos", "-án → hablarán", "они будут говорить")
                    )
                ),
                LessonSection(
                    heading = "Когда использовать",
                    items = listOf(
                        LessonItem("Планы / предсказания", "Mañana lloverá.", "завтра будет дождь"),
                        LessonItem("Обещания", "Te llamaré esta noche.", "позвоню тебе"),
                        LessonItem("Маркеры: mañana, el año que viene, en el futuro", "", ""),
                        LessonItem("Для -ER/-IR так же!", "comeré, viviré", "основа инфинитива + окончание")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Образуй Futuro: yo + hablar",
                    question = "Mañana ___ con el jefe.",
                    options = listOf("hablaré", "hablé", "hablaría", "hablo"),
                    correctAnswer = "hablaré",
                    explanation = "hablar + é = hablaré. Futuro Simple: основа инфинитива + окончания."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "El año que viene viviremos en Madrid.",
                    options = listOf("В следующем году мы будем жить в Мадриде.", "В прошлом году мы жили в Мадриде.", "Сейчас мы живём в Мадриде.", "Мы хотим жить в Мадриде."),
                    correctAnswer = "В следующем году мы будем жить в Мадриде.",
                    explanation = "el año que viene = следующий год. viviremos = мы будем жить (Futuro от vivir)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму",
                    question = "Ellos ___ mañana por la mañana.",
                    options = listOf("llegarán", "llegaron", "llegan", "llegaban"),
                    correctAnswer = "llegarán",
                    explanation = "llegar + án = llegarán. mañana = завтра — маркер будущего."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Обещание в Futuro",
                    question = "Te ___ la verdad.",
                    options = listOf("diré", "dije", "digo", "decía"),
                    correctAnswer = "diré",
                    explanation = "decir → diré (неправильный Futuro). Te diré = я скажу тебе."
                )
            )
        ),

        "u8_l1" to LessonContent(
            intro = "Futuro irregular — неправильные глаголы",
            sections = listOf(
                LessonSection(
                    heading = "Неправильные основы Futuro",
                    items = listOf(
                        LessonItem("tener → tendr-", "tendré, tendrás...", "у меня будет"),
                        LessonItem("venir → vendr-", "vendré, vendrás...", "я приду"),
                        LessonItem("salir → saldr-", "saldré, saldrás...", "я выйду"),
                        LessonItem("poder → podr-", "podré, podrás...", "я смогу"),
                        LessonItem("poner → pondr-", "pondré, pondrás...", "я положу"),
                        LessonItem("saber → sabr-", "sabré, sabrás...", "я буду знать"),
                        LessonItem("hacer → har-", "haré, harás...", "я сделаю"),
                        LessonItem("decir → dir-", "diré, dirás...", "я скажу"),
                        LessonItem("haber → habr-", "habrá...", "будет (безличное)")
                    )
                ),
                LessonSection(
                    heading = "Примеры",
                    items = listOf(
                        LessonItem("¿Qué harás este finde?", "Что будешь делать на выходных?", ""),
                        LessonItem("Vendré a las 8.", "Я приду в 8.", ""),
                        LessonItem("No podremos ir.", "Мы не сможем пойти.", ""),
                        LessonItem("Habrá mucha gente.", "Будет много людей.", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Futuro от TENER: yo",
                    question = "El año que viene ___ más dinero.",
                    options = listOf("tendré", "teneré", "tengo", "tendría"),
                    correctAnswer = "tendré",
                    explanation = "tener → tendr- + é = tendré. Основа меняется: tener → tendr-."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "¿Podrás venir mañana?",
                    options = listOf("Сможешь прийти завтра?", "Ты придёшь завтра?", "Ты пришёл вчера?", "Хочешь прийти?"),
                    correctAnswer = "Сможешь прийти завтра?",
                    explanation = "poder → podrás (tú). ¿Podrás venir? = сможешь прийти?"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Futuro от HACER",
                    question = "¿Qué ___ este fin de semana?",
                    options = listOf("harás", "hacerás", "haces", "harías"),
                    correctAnswer = "harás",
                    explanation = "hacer → har- + ás = harás. ¿Qué harás? = что ты будешь делать?"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Безличное habrá",
                    question = "Mañana ___ una reunión importante.",
                    options = listOf("habrá", "hay", "había", "ha habido"),
                    correctAnswer = "habrá",
                    explanation = "haber → habrá (Futuro безличное). habrá = будет (как hay = есть сейчас)."
                )
            )
        ),

        "u8_l2" to LessonContent(
            intro = "Condicional Simple: вежливые просьбы и гипотезы",
            sections = listOf(
                LessonSection(
                    heading = "Образование: инфинитив + окончания",
                    items = listOf(
                        LessonItem("yo", "-ía → hablaría", "я бы говорил"),
                        LessonItem("tú", "-ías → hablarías", "ты бы говорил"),
                        LessonItem("él/ella", "-ía → hablaría", "он/она бы говорил"),
                        LessonItem("nosotros", "-íamos → hablaríamos", "мы бы говорили"),
                        LessonItem("vosotros", "-íais → hablaríais", "вы бы говорили"),
                        LessonItem("ellos", "-ían → hablarían", "они бы говорили")
                    )
                ),
                LessonSection(
                    heading = "Когда использовать",
                    items = listOf(
                        LessonItem("Вежливая просьба", "¿Podría ayudarme?", "Не могли бы вы помочь?"),
                        LessonItem("Совет", "Yo en tu lugar estudiaría más.", "Я бы на твоём месте..."),
                        LessonItem("Гипотеза", "Con dinero, viajaría.", "Если бы были деньги, путешествовал бы."),
                        LessonItem("Желание (вежливо)", "Me gustaría ir.", "Я бы хотел пойти.")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Вежливая просьба: образуй Condicional",
                    question = "¿___ (poder, usted) hablar más despacio?",
                    options = listOf("Podría", "Puede", "Podrá", "Pudo"),
                    correctAnswer = "Podría",
                    explanation = "poder → podría (Condicional). ¿Podría...? = не могли бы вы...? (вежливо)"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Me gustaría visitar Japón.",
                    options = listOf("Я бы хотел посетить Японию.", "Мне нравится Япония.", "Я хочу в Японию.", "Я посещу Японию."),
                    correctAnswer = "Я бы хотел посетить Японию.",
                    explanation = "me gustaría = я бы хотел (Condicional от gustar). Вежливее чем quiero."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Совет",
                    question = "Yo en tu lugar, ___ al médico.",
                    options = listOf("iría", "voy", "iré", "fui"),
                    correctAnswer = "iría",
                    explanation = "ir → iría (Condicional). Yo en tu lugar iría = я бы на твоём месте пошёл."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "¿Dónde vivirías si pudieras?",
                    options = listOf("Где бы ты жил, если бы мог?", "Где ты живёшь сейчас?", "Где ты будешь жить?", "Где ты жил раньше?"),
                    correctAnswer = "Где бы ты жил, если бы мог?",
                    explanation = "vivirías = ты бы жил (Condicional). si pudieras = если бы мог (Subjuntivo)."
                )
            )
        ),

        "u8_l3" to LessonContent(
            intro = "Condicional irregular — те же основы что и Futuro",
            sections = listOf(
                LessonSection(
                    heading = "Неправильные основы (как в Futuro)",
                    items = listOf(
                        LessonItem("tener → tendr-", "tendría, tendrías...", "я бы имел"),
                        LessonItem("venir → vendr-", "vendría, vendrías...", "я бы пришёл"),
                        LessonItem("salir → saldr-", "saldría...", "я бы вышел"),
                        LessonItem("poder → podr-", "podría...", "я бы смог"),
                        LessonItem("hacer → har-", "haría...", "я бы сделал"),
                        LessonItem("decir → dir-", "diría...", "я бы сказал"),
                        LessonItem("saber → sabr-", "sabría...", "я бы знал"),
                        LessonItem("haber → habr-", "habría...", "было бы")
                    )
                ),
                LessonSection(
                    heading = "Примеры",
                    items = listOf(
                        LessonItem("¿Qué harías tú?", "Что бы ты сделал?", ""),
                        LessonItem("Yo lo haría diferente.", "Я бы сделал это иначе.", ""),
                        LessonItem("No podría vivir sin música.", "Я бы не смог жить без музыки.", ""),
                        LessonItem("Habría más problemas.", "Было бы больше проблем.", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Condicional от HACER: yo",
                    question = "¿Qué ___ tú en mi lugar?",
                    options = listOf("harías", "hacerías", "haces", "hiciste"),
                    correctAnswer = "harías",
                    explanation = "hacer → har- + ías = harías. ¿Qué harías? = что бы ты сделал?"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "No podría vivir sin internet.",
                    options = listOf("Я бы не смог жить без интернета.", "Я не могу жить без интернета.", "Я не буду жить без интернета.", "Раньше я жил без интернета."),
                    correctAnswer = "Я бы не смог жить без интернета.",
                    explanation = "podría = я бы смог (Condicional). No podría = я бы не смог."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Condicional от VENIR",
                    question = "¿___ (tú) a la fiesta si te invitara?",
                    options = listOf("Vendrías", "Venirías", "Vienes", "Viniste"),
                    correctAnswer = "Vendrías",
                    explanation = "venir → vendr- + ías = vendrías. ¿Vendrías? = ты бы пришёл?"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Diría que es una buena idea.",
                    options = listOf("Я бы сказал, что это хорошая идея.", "Я говорю, что хорошая идея.", "Скажи, что это хорошая идея.", "Он скажет хорошую идею."),
                    correctAnswer = "Я бы сказал, что это хорошая идея.",
                    explanation = "diría = я бы сказал (Condicional от decir → dir-)."
                )
            )
        ),

        "u8_l4" to LessonContent(
            intro = "Si + presente → futuro: реальные условия",
            sections = listOf(
                LessonSection(
                    heading = "Реальное условие: Si + presente + futuro",
                    items = listOf(
                        LessonItem("Si + Presente Indicativo", "→ Futuro Simple", "реальное, возможное условие"),
                        LessonItem("Si llueve,", "no saldré.", "Если будет дождь, не выйду."),
                        LessonItem("Si tienes tiempo,", "llámame.", "Если есть время, позвони."),
                        LessonItem("Si comes bien,", "estarás sano.", "Если будешь есть хорошо, будешь здоров.")
                    )
                ),
                LessonSection(
                    heading = "Важные правила",
                    items = listOf(
                        LessonItem("После SI — никогда не Futuro!", "Si llueve (не lloverá)", "ошибка начинающих"),
                        LessonItem("Порядок частей", "можно менять местами", "No saldré si llueve."),
                        LessonItem("Si tengo dinero,", "viajaré.", "Если будут деньги, поеду."),
                        LessonItem("Si estudias,", "aprobarás.", "Если будешь учиться, сдашь.")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильную форму после SI",
                    question = "Si ___ tiempo, iré al gimnasio.",
                    options = listOf("tengo", "tendré", "tendría", "tuviera"),
                    correctAnswer = "tengo",
                    explanation = "После si + реальное условие → Presente Indicativo (не Futuro!). Si tengo = если у меня есть."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Si no estudias, no aprobarás el examen.",
                    options = listOf("Если не будешь учиться, не сдашь экзамен.", "Я не учусь и не сдам.", "Учись, чтобы сдать экзамен.", "Если не сдашь, учись."),
                    correctAnswer = "Если не будешь учиться, не сдашь экзамен.",
                    explanation = "Si no estudias (если не учишься) + no aprobarás (не сдашь). Реальное условие."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Составь условное предложение",
                    question = "Si hace buen tiempo, ___ a la playa.",
                    options = listOf("iremos", "íbamos", "iríamos", "fuimos"),
                    correctAnswer = "iremos",
                    explanation = "Si + presente → futuro. iremos = мы пойдём (Futuro от ir)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Te ayudaré si me lo pides.",
                    options = listOf("Я помогу тебе, если попросишь.", "Я помогаю тебе.", "Помоги мне, пожалуйста.", "Я бы помог, если бы попросил."),
                    correctAnswer = "Я помогу тебе, если попросишь.",
                    explanation = "te ayudaré = я помогу (Futuro). si me lo pides = если попросишь (Presente после si)."
                )
            )
        ),

        "u8_l5" to LessonContent(
            intro = "Мечты и планы — лексика и выражения",
            sections = listOf(
                LessonSection(
                    heading = "Выражение желаний",
                    items = listOf(
                        LessonItem("Me gustaría + inf", "Я бы хотел...", "Me gustaría viajar."),
                        LessonItem("Quisiera + inf", "Я бы хотел... (вежливо)", "Quisiera reservar una mesa."),
                        LessonItem("Espero + inf", "Я надеюсь...", "Espero aprobar el examen."),
                        LessonItem("Sueño con + inf/noun", "Мечтаю о...", "Sueño con vivir en España."),
                        LessonItem("Mi sueño es + inf", "Моя мечта — ...", "Mi sueño es ser escritor."),
                        LessonItem("Tengo ganas de + inf", "Мне хочется...", "Tengo ganas de salir.")
                    )
                ),
                LessonSection(
                    heading = "Планы на будущее",
                    items = listOf(
                        LessonItem("Pienso + inf", "Я планирую...", "Pienso estudiar medicina."),
                        LessonItem("Voy a + inf", "Я собираюсь...", "Voy a aprender chino."),
                        LessonItem("Tengo planes de + inf", "У меня планы...", "Tengo planes de mudarme."),
                        LessonItem("Ojalá + subjuntivo", "Хотелось бы...", "¡Ojalá llueva!")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи вежливо: «я бы хотел стол»",
                    question = "___ una mesa para dos.",
                    options = listOf("Quisiera", "Quiero", "Querría", "Quería"),
                    correctAnswer = "Quisiera",
                    explanation = "quisiera = я бы хотел (Imperfecto Subjuntivo от querer, вежливая форма). Используется в ресторанах, магазинах."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Sueño con vivir cerca del mar.",
                    options = listOf("Мечтаю жить рядом с морем.", "Я живу рядом с морем.", "Мне снится море.", "Хочу поехать к морю."),
                    correctAnswer = "Мечтаю жить рядом с морем.",
                    explanation = "soñar con = мечтать о. Sueño con vivir = мечтаю жить."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «мне хочется гулять»?",
                    question = "Tengo ___ de salir a pasear.",
                    options = listOf("ganas", "sueño", "planes", "miedo"),
                    correctAnswer = "ganas",
                    explanation = "tener ganas de + inf = хотеться, желать. Tengo ganas de = мне хочется."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Espero que todo salga bien.",
                    options = listOf("Надеюсь, что всё получится.", "Я ожидаю хорошего.", "Всё будет хорошо.", "Я надеялся на лучшее."),
                    correctAnswer = "Надеюсь, что всё получится.",
                    explanation = "esperar que = надеяться что. salga bien = получится (Subjuntivo от salir)."
                )
            )
        ),

        "u8_l6" to LessonContent(
            intro = "Неопределённые местоимения: algo, alguien, nada, nadie",
            sections = listOf(
                LessonSection(
                    heading = "Утвердительные",
                    items = listOf(
                        LessonItem("algo", "что-то / кое-что", "Tengo algo para ti."),
                        LessonItem("alguien", "кто-то / кое-кто", "Alguien llamó."),
                        LessonItem("algún/alguna", "какой-то / некоторый", "Algún día lo haré."),
                        LessonItem("algunos/algunas", "некоторые", "Algunos estudiantes llegaron.")
                    )
                ),
                LessonSection(
                    heading = "Отрицательные",
                    items = listOf(
                        LessonItem("nada", "ничего", "No tengo nada."),
                        LessonItem("nadie", "никто", "No hay nadie."),
                        LessonItem("ningún/ninguna", "никакой", "No tengo ningún problema."),
                        LessonItem("Двойное отрицание!", "No vino nadie. = Nadie vino.", "оба варианта правильны")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильное местоимение",
                    question = "¿Hay ___ en casa?",
                    options = listOf("alguien", "algo", "nadie", "nada"),
                    correctAnswer = "alguien",
                    explanation = "alguien = кто-то (о людях). algo = что-то (о вещах). ¿Hay alguien? = есть кто-нибудь?"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "No sé nada de español.",
                    options = listOf("Я ничего не знаю по-испански.", "Я знаю что-то по-испански.", "Я немного знаю испанский.", "Я знаю испанский."),
                    correctAnswer = "Я ничего не знаю по-испански.",
                    explanation = "nada = ничего. No sé nada = я ничего не знаю (двойное отрицание нормально в испанском)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Двойное отрицание",
                    question = "No vino ___ a la fiesta.",
                    options = listOf("nadie", "alguien", "alguno", "nada"),
                    correctAnswer = "nadie",
                    explanation = "No vino nadie = никто не пришёл. No + глагол + nadie — стандартное двойное отрицание."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Tengo algo importante que decirte.",
                    options = listOf("Мне есть что тебе сказать важное.", "Тебе нужно мне что-то сказать.", "Ничего важного нет.", "Я скажу тебе кое-что."),
                    correctAnswer = "Мне есть что тебе сказать важное.",
                    explanation = "algo = что-то/кое-что. Tengo algo que decirte = мне есть что тебе сказать."
                )
            )
        ),

        "u8_l7" to LessonContent(
            intro = "Вероятность: probablemente, quizás, a lo mejor",
            sections = listOf(
                LessonSection(
                    heading = "Выражение вероятности",
                    items = listOf(
                        LessonItem("a lo mejor + indicativo", "может быть (50/50)", "A lo mejor viene. = Может, придёт."),
                        LessonItem("quizás/quizá + indicativo", "может быть", "Quizás tiene razón."),
                        LessonItem("probablemente + indicativo", "вероятно", "Probablemente lloverá."),
                        LessonItem("seguramente + indicativo", "наверняка", "Seguramente está en casa.")
                    )
                ),
                LessonSection(
                    heading = "Степень уверенности",
                    items = listOf(
                        LessonItem("seguro (100%)", "точно", "Seguro que viene."),
                        LessonItem("seguramente (90%)", "наверняка", ""),
                        LessonItem("probablemente (70%)", "вероятно", ""),
                        LessonItem("quizás / a lo mejor (50%)", "может быть", ""),
                        LessonItem("tal vez (50%)", "возможно", "Tal vez tenga razón.")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "A lo mejor mañana no hay clase.",
                    options = listOf("Может, завтра не будет урока.", "Завтра точно не будет урока.", "Завтра урок отменили.", "Уроков больше не будет."),
                    correctAnswer = "Может, завтра не будет урока.",
                    explanation = "a lo mejor = может быть (примерно 50%). Не требует Subjuntivo!"
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери нужное слово",
                    question = "___ está en casa. No contesta al teléfono.",
                    options = listOf("Probablemente", "Seguro", "Nunca", "Siempre"),
                    correctAnswer = "Probablemente",
                    explanation = "probablemente = вероятно (70%). Вероятное объяснение ситуации."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Quizás tenga razón.",
                    options = listOf("Может, он прав.", "Он точно прав.", "Он не прав.", "Я думаю, он прав."),
                    correctAnswer = "Может, он прав.",
                    explanation = "quizás = может быть. tener razón = быть правым."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Какое выражение означает наибольшую уверенность?",
                    question = "Выбери выражение с наибольшей уверенностью:",
                    options = listOf("Seguramente", "Quizás", "A lo mejor", "Tal vez"),
                    correctAnswer = "Seguramente",
                    explanation = "seguramente = наверняка (~90%). quizás / a lo mejor / tal vez = может быть (~50%)."
                )
            )
        ),

        "u8_l8" to LessonContent(
            intro = "Транспорт и дорога — продвинутый уровень",
            sections = listOf(
                LessonSection(
                    heading = "Аренда и управление",
                    items = listOf(
                        LessonItem("alquilar un coche", "арендовать машину", "Quiero alquilar un coche."),
                        LessonItem("conducir / manejar", "водить машину", "¿Sabes conducir?"),
                        LessonItem("el carnet de conducir", "водительское удостоверение", ""),
                        LessonItem("aparcar / estacionar", "парковаться", "No se puede aparcar aquí."),
                        LessonItem("el aparcamiento", "парковка", "¿Hay aparcamiento cerca?"),
                        LessonItem("la gasolinera", "заправочная станция", ""),
                        LessonItem("el atasco / el embotellamiento", "пробка", "Hay mucho atasco.")
                    )
                ),
                LessonSection(
                    heading = "Общественный транспорт",
                    items = listOf(
                        LessonItem("el abono / la tarjeta de transporte", "проездной", ""),
                        LessonItem("transbordar", "делать пересадку", "Hay que transbordar en Sol."),
                        LessonItem("la línea", "линия (метро/автобус)", "la línea 2"),
                        LessonItem("perder el tren/autobús", "опоздать на поезд/автобус", "He perdido el tren."),
                        LessonItem("el retraso", "опоздание / задержка", "El tren lleva retraso.")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «водительское удостоверение»?",
                    question = "el ___ de conducir",
                    options = listOf("carnet", "billete", "abono", "permiso"),
                    correctAnswer = "carnet",
                    explanation = "el carnet de conducir = водительское удостоверение."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "He perdido el último metro.",
                    options = listOf("Я опоздал на последнее метро.", "Я нашёл метро.", "Метро закрыто.", "Я еду на метро."),
                    correctAnswer = "Я опоздал на последнее метро.",
                    explanation = "perder el metro/tren = опоздать на метро/поезд. He perdido = я опоздал (P. Perfecto)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что такое «el atasco»?",
                    question = "Hay mucho atasco en la autopista.",
                    options = listOf("Большая пробка на шоссе.", "Много машин на парковке.", "Авария на шоссе.", "Много людей на дороге."),
                    correctAnswer = "Большая пробка на шоссе.",
                    explanation = "el atasco = пробка. la autopista = шоссе/автострада."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "El tren lleva 20 minutos de retraso.",
                    options = listOf("Поезд опаздывает на 20 минут.", "Поезд едет 20 минут.", "Поезд прибыл на 20 минут раньше.", "Поезд отходит через 20 минут."),
                    correctAnswer = "Поезд опаздывает на 20 минут.",
                    explanation = "llevar retraso = опаздывать. 20 minutos de retraso = на 20 минут."
                )
            )
        ),

        "u8_l9" to LessonContent(
            intro = "Глаголы с предлогами: pensar en, soñar con, depender de...",
            sections = listOf(
                LessonSection(
                    heading = "Глаголы с предлогом EN",
                    items = listOf(
                        LessonItem("pensar en", "думать о", "Pienso en ti."),
                        LessonItem("confiar en", "доверять", "Confío en ti."),
                        LessonItem("quedarse en", "оставаться в", "Me quedo en casa."),
                        LessonItem("entrar en", "входить в", "Entré en la tienda.")
                    )
                ),
                LessonSection(
                    heading = "Глаголы с другими предлогами",
                    items = listOf(
                        LessonItem("soñar con", "мечтать о / сниться", "Sueño con viajar."),
                        LessonItem("depender de", "зависеть от", "Depende de ti."),
                        LessonItem("olvidarse de", "забывать о", "Me olvidé de llamarte."),
                        LessonItem("acordarse de", "вспоминать о", "¿Te acuerdas de mí?"),
                        LessonItem("hablar de", "говорить о", "Hablamos del viaje."),
                        LessonItem("preocuparse por", "беспокоиться о", "Me preocupo por ti."),
                        LessonItem("interesarse por", "интересоваться", "Me interesa la música.")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери предлог",
                    question = "Siempre pienso ___ mi familia.",
                    options = listOf("en", "de", "con", "por"),
                    correctAnswer = "en",
                    explanation = "pensar en = думать о. Siempre pienso en mi familia = я всегда думаю о своей семье."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Me olvidé de llamarte.",
                    options = listOf("Я забыл позвонить тебе.", "Я вспомнил о тебе.", "Я хотел позвонить.", "Я позвоню тебе."),
                    correctAnswer = "Я забыл позвонить тебе.",
                    explanation = "olvidarse de + inf = забыть (сделать что-то). Me olvidé = я забыл."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери предлог",
                    question = "Todo depende ___ ti.",
                    options = listOf("de", "en", "con", "por"),
                    correctAnswer = "de",
                    explanation = "depender de = зависеть от. Depende de ti = зависит от тебя."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "¿Te acuerdas de nuestra primera clase?",
                    options = listOf("Ты помнишь наш первый урок?", "Ты забыл первый урок?", "Когда был наш первый урок?", "Ты ходил на первый урок?"),
                    correctAnswer = "Ты помнишь наш первый урок?",
                    explanation = "acordarse de = помнить / вспоминать. ¿Te acuerdas de...? = ты помнишь...?"
                )
            )
        ),

        "u8_l10" to LessonContent(
            intro = "Природа, погода и окружающая среда",
            sections = listOf(
                LessonSection(
                    heading = "Природа",
                    items = listOf(
                        LessonItem("el campo", "деревня / поле", "Vivo en el campo."),
                        LessonItem("el mar / el océano", "море / океан", ""),
                        LessonItem("la montaña", "гора", "Vamos a la montaña."),
                        LessonItem("el bosque", "лес", ""),
                        LessonItem("el río", "река", ""),
                        LessonItem("la playa", "пляж", "Voy a la playa."),
                        LessonItem("el lago", "озеро", "")
                    )
                ),
                LessonSection(
                    heading = "Погода — продвинутый уровень",
                    items = listOf(
                        LessonItem("hace viento", "ветрено", "Hace mucho viento hoy."),
                        LessonItem("hay niebla", "туман", "Hay mucha niebla."),
                        LessonItem("hay tormenta", "гроза", "¡Hay tormenta!"),
                        LessonItem("granizar", "идти граду", "Está granizando."),
                        LessonItem("el pronóstico del tiempo", "прогноз погоды", ""),
                        LessonItem("grados (°C)", "градусы", "Hace 25 grados.")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Prefiero el campo a la ciudad.",
                    options = listOf("Я предпочитаю деревню городу.", "Мне нравится город.", "Я живу за городом.", "Деревня лучше."),
                    correctAnswer = "Я предпочитаю деревню городу.",
                    explanation = "preferir A a B = предпочитать A вместо B."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «идёт гроза»?",
                    question = "Hay ___.",
                    options = listOf("tormenta", "niebla", "viento", "granizo"),
                    correctAnswer = "tormenta",
                    explanation = "hay tormenta = идёт гроза. hay niebla = туман. hay granizo = идёт град."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Según el pronóstico, mañana lloverá.",
                    options = listOf("По прогнозу, завтра будет дождь.", "Сегодня идёт дождь.", "Вчера был дождь.", "Дождя не будет."),
                    correctAnswer = "По прогнозу, завтра будет дождь.",
                    explanation = "según = по (данным). el pronóstico = прогноз. lloverá = будет дождь (Futuro)."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что значит «hace 30 grados»?",
                    question = "Hoy hace 30 grados.",
                    options = listOf("Сегодня 30 градусов.", "Сегодня очень холодно.", "Сегодня 30 минут.", "Сегодня ветрено."),
                    correctAnswer = "Сегодня 30 градусов.",
                    explanation = "hacer X grados = быть X градусов (температура). Hace 30 grados = 30 градусов жары!"
                )
            )
        ),

        "u8_l11" to LessonContent(
            intro = "Cuantificadores: mucho, poco, bastante, demasiado",
            sections = listOf(
                LessonSection(
                    heading = "Количественные слова",
                    items = listOf(
                        LessonItem("mucho/a/os/as", "много", "Tengo mucho trabajo."),
                        LessonItem("poco/a/os/as", "мало", "Tengo poco tiempo."),
                        LessonItem("bastante/s", "достаточно / довольно", "Tengo bastante dinero."),
                        LessonItem("demasiado/a/os/as", "слишком много", "Comes demasiado."),
                        LessonItem("suficiente/s", "достаточно", "No tengo suficiente información.")
                    )
                ),
                LessonSection(
                    heading = "С прилагательными (не согласуются)",
                    items = listOf(
                        LessonItem("muy + adj", "очень", "Es muy interesante."),
                        LessonItem("bastante + adj", "довольно", "Es bastante fácil."),
                        LessonItem("demasiado + adj", "слишком", "Es demasiado caro."),
                        LessonItem("poco + adj", "не очень", "Es poco interesante."),
                        LessonItem("ВАЖНО: mucho → muy", "перед adj → muy", "muy bueno (не mucho bueno!)")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильный вариант",
                    question = "Esta película es ___ aburrida.",
                    options = listOf("muy", "mucho", "muchos", "mucha"),
                    correctAnswer = "muy",
                    explanation = "Перед прилагательным: muy (не mucho). mucho → muy перед adj."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Comes demasiado rápido.",
                    options = listOf("Ты ешь слишком быстро.", "Ты ешь очень быстро.", "Ты ешь достаточно быстро.", "Ты не ешь быстро."),
                    correctAnswer = "Ты ешь слишком быстро.",
                    explanation = "demasiado = слишком. Перед наречием не согласуется."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Выбери правильное слово",
                    question = "No tenemos ___ tiempo para terminar.",
                    options = listOf("suficiente", "bastante", "demasiado", "poco"),
                    correctAnswer = "suficiente",
                    explanation = "suficiente = достаточно. No tenemos suficiente tiempo = у нас недостаточно времени."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Hay bastante gente en la calle.",
                    options = listOf("На улице довольно много народу.", "На улице мало народу.", "На улице слишком много народу.", "На улице никого нет."),
                    correctAnswer = "На улице довольно много народу.",
                    explanation = "bastante = достаточно / довольно много. bastante gente = довольно много людей."
                )
            )
        ),

        "u8_l12" to LessonContent(
            intro = "Технологии и цифровой мир",
            sections = listOf(
                LessonSection(
                    heading = "Устройства и интернет",
                    items = listOf(
                        LessonItem("el móvil / el celular", "мобильный телефон", ""),
                        LessonItem("el ordenador / la computadora", "компьютер", ""),
                        LessonItem("la tableta", "планшет", ""),
                        LessonItem("el wifi / la wifi", "вайфай", "¿Hay wifi aquí?"),
                        LessonItem("la contraseña", "пароль", "¿Cuál es la contraseña?"),
                        LessonItem("descargar", "скачивать", "Descarga la app."),
                        LessonItem("la aplicación / la app", "приложение", "")
                    )
                ),
                LessonSection(
                    heading = "Социальные сети и общение",
                    items = listOf(
                        LessonItem("las redes sociales", "социальные сети", ""),
                        LessonItem("publicar / subir", "публиковать / загружать", "Subí una foto."),
                        LessonItem("el seguidor / el seguido", "подписчик / подписка", ""),
                        LessonItem("hacer una videollamada", "сделать видеозвонок", ""),
                        LessonItem("el mensaje de voz", "голосовое сообщение", ""),
                        LessonItem("el grupo de WhatsApp", "группа в WhatsApp", "")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как спросить пароль от вайфая?",
                    question = "¿Cuál es la ___ del wifi?",
                    options = listOf("contraseña", "aplicación", "red", "clave"),
                    correctAnswer = "contraseña",
                    explanation = "la contraseña = пароль. ¿Cuál es la contraseña del wifi? — стандартный вопрос в кафе."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Descarga la aplicación en tu móvil.",
                    options = listOf("Скачай приложение на телефон.", "Открой приложение.", "Удали приложение.", "Купи приложение."),
                    correctAnswer = "Скачай приложение на телефон.",
                    explanation = "descargar = скачивать. la aplicación = приложение."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что значит «subir una foto»?",
                    question = "Voy a subir esta foto a Instagram.",
                    options = listOf("Загружу эту фото в Instagram.", "Сделаю фото.", "Скачаю фото.", "Удалю фото."),
                    correctAnswer = "Загружу эту фото в Instagram.",
                    explanation = "subir = загружать (в интернет). publicar = публиковать."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "¿Hacemos una videollamada esta tarde?",
                    options = listOf("Созвонимся по видео сегодня вечером?", "Позвоним сегодня вечером?", "Увидимся сегодня вечером?", "Напишем сегодня вечером?"),
                    correctAnswer = "Созвонимся по видео сегодня вечером?",
                    explanation = "hacer una videollamada = сделать видеозвонок / созвониться по видео."
                )
            )
        ),

        "u8_l13" to LessonContent(
            intro = "Спорт и здоровый образ жизни",
            sections = listOf(
                LessonSection(
                    heading = "Спорт",
                    items = listOf(
                        LessonItem("hacer ejercicio", "заниматься спортом", "Hago ejercicio cada día."),
                        LessonItem("ir al gimnasio", "ходить в зал", "Voy al gimnasio 3 veces."),
                        LessonItem("correr / hacer running", "бегать", "Corro por el parque."),
                        LessonItem("nadar", "плавать", "Nado en la piscina."),
                        LessonItem("montar en bici", "кататься на велосипеде", ""),
                        LessonItem("jugar al fútbol/tenis", "играть в футбол/теннис", "Juego al fútbol."),
                        LessonItem("el partido", "матч / игра", "Ver un partido.")
                    )
                ),
                LessonSection(
                    heading = "Здоровый образ жизни",
                    items = listOf(
                        LessonItem("llevar una dieta sana", "соблюдать здоровую диету", ""),
                        LessonItem("comer equilibrado", "питаться сбалансированно", ""),
                        LessonItem("dormir bien", "хорошо спать", "Es importante dormir bien."),
                        LessonItem("el estrés", "стресс", "Tengo mucho estrés."),
                        LessonItem("relajarse", "расслабляться", "Necesito relajarme."),
                        LessonItem("estar en forma", "быть в форме", "Estoy en buena forma.")
                    )
                )
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Hago ejercicio tres veces a la semana.",
                    options = listOf("Я занимаюсь спортом три раза в неделю.", "Я хожу в зал три раза.", "Я бегаю три раза в неделю.", "Я плаваю три раза."),
                    correctAnswer = "Я занимаюсь спортом три раза в неделю.",
                    explanation = "hacer ejercicio = заниматься спортом. tres veces a la semana = три раза в неделю."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Как сказать «играть в теннис»?",
                    question = "Juego ___ tenis los sábados.",
                    options = listOf("al", "el", "a", "en"),
                    correctAnswer = "al",
                    explanation = "jugar al + вид спорта. al = a + el. Juego al tenis = я играю в теннис."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Переведи",
                    question = "Llevo una dieta sana y duermo bien.",
                    options = listOf("Я соблюдаю здоровую диету и хорошо сплю.", "Я ем много и сплю мало.", "Я на диете и не сплю.", "Я ем здоровое и занимаюсь спортом."),
                    correctAnswer = "Я соблюдаю здоровую диету и хорошо сплю.",
                    explanation = "llevar una dieta sana = соблюдать здоровую диету. dormir bien = хорошо спать."
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Что значит «estar en forma»?",
                    question = "Quiero estar en buena forma.",
                    options = listOf("Хочу быть в хорошей форме.", "Хочу похудеть.", "Хочу заниматься спортом.", "Хочу быть здоровым."),
                    correctAnswer = "Хочу быть в хорошей форме.",
                    explanation = "estar en forma = быть в форме (физически). estar en buena forma = быть в хорошей форме."
                )
            )
        )
    )
}
