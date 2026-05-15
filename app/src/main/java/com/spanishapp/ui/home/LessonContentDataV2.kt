package com.spanishapp.ui.home

/**
 * Курс v2.0 — переписка по `docs/curriculum/ESPEAK_Curriculum.xlsx`.
 *
 * ## Стратегия миграции
 *
 * Этот объект работает как **override** для LessonContentData. В run{}
 * блоке LessonContentData делается:
 * ```
 * val merged = oldLessons + LessonContentDataV2.allLessons()
 * ```
 * Map.plus() семантика гарантирует что НОВЫЕ записи перетирают старые.
 * Это даёт безопасную инкрементальную миграцию: блок-за-блоком, без
 * страха сломать ещё-не-переписанные уроки.
 *
 * ## Источник правды
 *
 * Каждый урок строго следует строке xlsx по столбцам:
 * Тип / 6 упражнений / Vocab scope / Что запомнит.
 *
 * Все слова используются ТОЛЬКО из VocabScope.wordsForLesson(lessonId)
 * — никаких незнакомых слов вылетать не должно.
 *
 * ## Прогресс
 *
 * Блок 1.1 «Взлёт» — ✅ заполнен (15 уроков u1_l0..u1_l13_5).
 * Блок 1.2 «Мой мир» — pending.
 * ...
 */
object LessonContentDataV2 {

    fun allLessons(): Map<String, LessonContent> =
        blockA1_1() + blockA1_2() + blockA1_3() + blockA1_4() +
        blockA2_1()

    // ═══════════════════════════════════════════════════════════════
    //  БЛОК 1.1 «ВЗЛЁТ» — фонетика + первое общение + SER + местоимения
    //  15 уроков: u1_l0 … u1_l13 + u1_l13_5 (новый)
    // ═══════════════════════════════════════════════════════════════

    private fun blockA1_1(): Map<String, LessonContent> = mapOf(

        // ─────────────────────────────────────────────────────────────
        // u1_l0 — Гласные A E I O U
        // Type: Phonetics | Упр: LISTEN_PICK, LISTEN_TYPE, ORDER_LETTERS,
        //                       SPOT_THE_ERROR, MATCH_PAIRS, MULTIPLE_CHOICE
        // ─────────────────────────────────────────────────────────────
        "u1_l0" to LessonContent(
            intro = "5 чистых гласных. Каждая всегда читается одинаково — никакой редукции как в русском «карова».",
            sections = listOf(
                LessonSection(
                    heading = "Алфавит — гласные",
                    items = listOf(
                        LessonItem("A", "[а]", "casa — дом"),
                        LessonItem("E", "[э]", "mes — месяц"),
                        LessonItem("I", "[и]", "isla — остров"),
                        LessonItem("O", "[о]", "ojo — глаз"),
                        LessonItem("U", "[у]", "uva — виноград"),
                    ),
                ),
            ),
            exercises = listOf(
                // Упр.1 LISTEN_PICK — услышь звук → выбери букву
                Exercise(
                    type = ExerciseType.LISTEN_PICK,
                    instruction = "Послушай и выбери букву",
                    audioText = "a",
                    options = listOf("A", "E", "I", "O"),
                    correctAnswer = "A",
                    explanation = "A → [а]: casa — «ка-са»",
                ),
                // Упр.2 LISTEN_TYPE — диктант
                Exercise(
                    type = ExerciseType.LISTEN_TYPE,
                    instruction = "Послушай и напечатай слово",
                    audioText = "uva",
                    correctAnswer = "uva",
                    explanation = "uva — виноград. U+V+A: «у-ба».",
                ),
                // Упр.3 ORDER_LETTERS — собери слово
                Exercise(
                    type = ExerciseType.ORDER_LETTERS,
                    instruction = "Собери слово из букв",
                    correctAnswer = "casa",
                    explanation = "casa — дом. Все 4 буквы — гласные A и согласные C, S.",
                ),
                // Упр.4 SPOT_THE_ERROR — найди некорректное произношение
                Exercise(
                    type = ExerciseType.SPOT_THE_ERROR,
                    instruction = "Какое произношение НЕ верное?",
                    question = "Найди ошибку:",
                    errorVariants = listOf(
                        "casa = [ка-са]",
                        "ojo = [о-хо]",
                        "uva = [ю-ва]",
                        "isla = [ис-ла]",
                    ),
                    correctAnswer = "uva = [ю-ва]",
                    explanation = "U в испанском всегда [у], не [ю]. uva = «у-ба».",
                ),
                // Упр.5 MATCH_PAIRS 5 пар — буква ↔ пример
                Exercise(
                    type = ExerciseType.MATCH_PAIRS,
                    instruction = "Соедини букву и пример",
                    correctAnswer = "ok",
                    pairs = listOf(
                        "A" to "casa",
                        "E" to "mes",
                        "I" to "isla",
                        "O" to "ojo",
                        "U" to "uva",
                    ),
                ),
                // Упр.6 MULTIPLE_CHOICE — правило произношения
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Главное правило испанских гласных:",
                    question = "Сколько гласных и как они читаются?",
                    options = listOf(
                        "5 — каждая всегда одинаково",
                        "6 — зависят от ударения",
                        "12 — как в английском",
                        "3 — только A, O, U",
                    ),
                    correctAnswer = "5 — каждая всегда одинаково",
                    explanation = "5 гласных, каждая звучит одинаково всегда. Никакой редукции — выучи раз и навсегда.",
                ),
            ),
        ),

        // ─────────────────────────────────────────────────────────────
        // u1_l1 — Согласные B/V, D, G
        // ─────────────────────────────────────────────────────────────
        "u1_l1" to LessonContent(
            intro = "Большинство согласных как в русском, но 3 буквы коварны: B и V звучат одинаково, D смягчается, G меняется перед e/i.",
            sections = listOf(
                LessonSection(
                    heading = "Особые согласные",
                    items = listOf(
                        LessonItem("B = V", "оба [б/в]", "vino — «би-но»"),
                        LessonItem("D между гласных", "мягкое [ð]", "cada — «ка-ða»"),
                        LessonItem("G + a/o/u", "[г]", "gato — «га-то»"),
                        LessonItem("G + e/i", "[х]", "gente — «хэн-тэ»"),
                    ),
                ),
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.LISTEN_PICK,
                    instruction = "Послушай и выбери слово",
                    audioText = "vino",
                    options = listOf("vino", "fino", "pino", "bino"),
                    correctAnswer = "vino",
                    explanation = "vino — вино. V и B читаются одинаково: [би-но].",
                ),
                Exercise(
                    type = ExerciseType.LISTEN_TYPE,
                    instruction = "Послушай и напечатай",
                    audioText = "gato",
                    correctAnswer = "gato",
                    explanation = "gato — кот. G перед «a» = [г].",
                ),
                Exercise(
                    type = ExerciseType.ORDER_LETTERS,
                    instruction = "Собери слово",
                    correctAnswer = "gente",
                    explanation = "gente — люди. G+e = [х]: «хэн-тэ».",
                ),
                Exercise(
                    type = ExerciseType.SPOT_THE_ERROR,
                    instruction = "Найди НЕправильное произношение",
                    question = "",
                    errorVariants = listOf(
                        "gato = [га-то]",
                        "vino = [би-но]",
                        "gente = [гэн-тэ]",
                        "cada = [ка-ða]",
                    ),
                    correctAnswer = "gente = [гэн-тэ]",
                    explanation = "G перед «e» читается как [х], не [г]. gente = «хэн-тэ».",
                ),
                Exercise(
                    type = ExerciseType.MATCH_PAIRS,
                    instruction = "Соедини сочетание и звук",
                    correctAnswer = "ok",
                    pairs = listOf(
                        "B / V" to "[б/в]",
                        "G + a/o/u" to "[г]",
                        "G + e/i" to "[х]",
                        "GUE / GUI" to "[гэ/ги]",
                        "D между гласных" to "[ð] мягкое",
                    ),
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Какие буквы звучат ОДИНАКОВО?",
                    question = "",
                    options = listOf("B и V", "C и K", "D и T", "G и J"),
                    correctAnswer = "B и V",
                    explanation = "B и V в испанском — один звук [б/в]. Различай только на письме.",
                ),
            ),
        ),

        // ─────────────────────────────────────────────────────────────
        // u1_l2 — H молчит · J=[х] · Ñ=[нь] · RR=[р-р]
        // ─────────────────────────────────────────────────────────────
        "u1_l2" to LessonContent(
            intro = "Четыре буквы — четыре сюрприза. H молчит. J=[х]. Ñ=[нь]. RR — длинное вибрирующее.",
            sections = listOf(
                LessonSection(
                    heading = "Особые буквы",
                    items = listOf(
                        LessonItem("H", "молчит", "hola — «о-ла»"),
                        LessonItem("J", "[х]", "jefe — «хэ-фэ» (босс)"),
                        LessonItem("Ñ", "[нь]", "año — «а-ньо» (год)"),
                        LessonItem("RR", "[р-р]", "perro — «пэр-ро»"),
                    ),
                ),
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.LISTEN_PICK,
                    instruction = "Послушай и выбери",
                    audioText = "hola",
                    options = listOf("hola", "ola", "jola", "ñola"),
                    correctAnswer = "hola",
                    explanation = "hola — привет. H молчит, читается «о-ла».",
                ),
                Exercise(
                    type = ExerciseType.LISTEN_TYPE,
                    instruction = "Послушай и напечатай",
                    audioText = "año",
                    correctAnswer = "año",
                    explanation = "año — год. Ñ — отдельная буква, звук [нь].",
                ),
                Exercise(
                    type = ExerciseType.ORDER_LETTERS,
                    instruction = "Собери слово",
                    correctAnswer = "perro",
                    explanation = "perro — собака. Две R дают длинное вибрирующее [р-р].",
                ),
                Exercise(
                    type = ExerciseType.SPOT_THE_ERROR,
                    instruction = "Найди НЕверное произношение",
                    question = "",
                    errorVariants = listOf(
                        "hola = [хо-ла]",
                        "Japón = [ха-пон]",
                        "año = [а-ньо]",
                        "perro = [пэр-ро]",
                    ),
                    correctAnswer = "hola = [хо-ла]",
                    explanation = "H в испанском НЕ читается! hola = «о-ла», без «х».",
                ),
                Exercise(
                    type = ExerciseType.MATCH_PAIRS,
                    instruction = "Буква ↔ звук",
                    correctAnswer = "ok",
                    pairs = listOf(
                        "H" to "молчит",
                        "J" to "[х]",
                        "Ñ" to "[нь]",
                        "RR" to "[р-р] длинное",
                        "R одна" to "[р] короткое",
                    ),
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "В чём разница?",
                    question = "pero vs perro",
                    options = listOf(
                        "pero = «но», perro = «собака»",
                        "оба значат «собака»",
                        "pero = «собака», perro = «но»",
                        "разница только в написании",
                    ),
                    correctAnswer = "pero = «но», perro = «собака»",
                    explanation = "Одна R и две R меняют смысл. pero (но) ≠ perro (собака).",
                ),
            ),
        ),

        // ─────────────────────────────────────────────────────────────
        // u1_l3 — Ударение и тильда
        // ─────────────────────────────────────────────────────────────
        "u1_l3" to LessonContent(
            intro = "Ударение в испанском по 3 правилам. Тильда (´) ставится только когда ударение «не по правилу».",
            sections = listOf(
                LessonSection(
                    heading = "Три правила",
                    items = listOf(
                        LessonItem("Гласная / N / S в конце", "ударение на предпоследний", "casa — КА-са"),
                        LessonItem("Любая другая согласная", "ударение на последний", "hablar — а-БЛЯР"),
                        LessonItem("Тильда (´)", "ломает правило", "café — ка-ФЭ"),
                    ),
                ),
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.LISTEN_PICK,
                    instruction = "Где ударение в слове?",
                    audioText = "café",
                    options = listOf("ка-ФЭ", "КА-фэ", "ка-фэ-Э", "обычное"),
                    correctAnswer = "ка-ФЭ",
                    explanation = "Тильда над É — ударение туда. café = «ка-ФЭ».",
                ),
                Exercise(
                    type = ExerciseType.LISTEN_TYPE,
                    instruction = "Послушай и напечатай (с тильдой!)",
                    audioText = "música",
                    correctAnswer = "música",
                    explanation = "música — ударение на МУ. Тильда обязательна.",
                ),
                Exercise(
                    type = ExerciseType.ORDER_LETTERS,
                    instruction = "Собери слово",
                    correctAnswer = "casa",
                    explanation = "casa — дом. Гласная в конце → ударение на предпоследний слог.",
                ),
                Exercise(
                    type = ExerciseType.SPOT_THE_ERROR,
                    instruction = "В каком слове тильда лишняя?",
                    question = "",
                    errorVariants = listOf(
                        "café",
                        "música",
                        "cása",
                        "español",
                    ),
                    correctAnswer = "cása",
                    explanation = "casa оканчивается на гласную → ударение УЖЕ на «КА» по правилу. Тильда не нужна.",
                ),
                Exercise(
                    type = ExerciseType.MATCH_PAIRS,
                    instruction = "Слово ↔ позиция ударения",
                    correctAnswer = "ok",
                    pairs = listOf(
                        "casa" to "1-й слог",
                        "hablar" to "последний",
                        "café" to "по тильде",
                        "música" to "3-й от конца",
                        "ciudad" to "последний",
                    ),
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Когда нужна тильда?",
                    question = "",
                    options = listOf(
                        "Когда ударение НЕ по правилу",
                        "На каждом слове",
                        "Только на гласных O и E",
                        "Только в первом слоге",
                    ),
                    correctAnswer = "Когда ударение НЕ по правилу",
                    explanation = "Тильда ставится только если ударение нарушает 2 базовых правила.",
                ),
            ),
        ),

        // ─────────────────────────────────────────────────────────────
        // u1_l4 — Hola / Buenos días / ¿Cómo estás?
        // ─────────────────────────────────────────────────────────────
        "u1_l4" to LessonContent(
            intro = "Первые фразы для встречи. Hola работает всегда, а Buenos días — только до полудня.",
            sections = listOf(
                LessonSection(
                    heading = "Приветствия",
                    items = listOf(
                        LessonItem("Hola", "Привет / Здравствуйте", ""),
                        LessonItem("Buenos días", "Доброе утро / день", "до 13:00"),
                        LessonItem("Buenas tardes", "Добрый день / вечер", "13:00 — закат"),
                        LessonItem("Buenas noches", "Добрый вечер / спокойной ночи", "после заката"),
                        LessonItem("¿Cómo estás?", "Как ты?", "неформально"),
                        LessonItem("Bien, gracias", "Хорошо, спасибо", "стандартный ответ"),
                    ),
                ),
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MATCH_PAIRS,
                    instruction = "Соедини фразу и перевод",
                    correctAnswer = "ok",
                    pairs = listOf(
                        "Hola" to "Привет",
                        "Buenos días" to "Доброе утро",
                        "Buenas noches" to "Добрый вечер",
                        "¿Cómo estás?" to "Как ты?",
                        "Bien, gracias" to "Хорошо, спасибо",
                    ),
                ),
                Exercise(
                    type = ExerciseType.LISTEN_PICK,
                    instruction = "Послушай и выбери фразу",
                    audioText = "Buenos días",
                    options = listOf("Buenos días", "Buenas tardes", "Buenas noches", "Hola"),
                    correctAnswer = "Buenos días",
                    explanation = "Buenos días = доброе утро / день (до 13:00).",
                ),
                Exercise(
                    type = ExerciseType.BUILD_SENTENCE,
                    instruction = "Собери фразу: «Привет, как ты?»",
                    words = listOf("Hola", "¿Cómo", "estás?"),
                    correctAnswer = "Hola ¿Cómo estás?",
                    explanation = "Стандартное разговорное приветствие.",
                ),
                Exercise(
                    type = ExerciseType.TAP_MISSING_WORD,
                    instruction = "Заполни пропуск",
                    question = "___, gracias. ¿Y tú?",
                    options = listOf("Bien", "Hola", "Adiós"),
                    correctAnswer = "Bien",
                    explanation = "Bien, gracias — стандартный ответ на «Как ты?».",
                ),
                Exercise(
                    type = ExerciseType.DIALOGUE_FILL,
                    instruction = "Дополни диалог",
                    dialogueLines = listOf(
                        "👨 Pablo" to "¡Hola! ¿Cómo estás?",
                        "👩 Ты" to "___, gracias. ¿Y tú?",
                        "👨 Pablo" to "Muy bien, gracias.",
                    ),
                    options = listOf("Bien", "Hola", "Adiós"),
                    correctAnswer = "Bien",
                    explanation = "Bien — «хорошо». Стандартный ответ.",
                ),
                Exercise(
                    type = ExerciseType.TRANSLATE,
                    instruction = "Переведи на испанский: «Доброе утро!»",
                    correctAnswer = "Buenos días",
                    explanation = "Buenos días — до 13:00. После — Buenas tardes.",
                ),
            ),
        ),

        // ─────────────────────────────────────────────────────────────
        // u1_l5 — Adiós / Hasta luego / Hasta mañana
        // ─────────────────────────────────────────────────────────────
        "u1_l5" to LessonContent(
            intro = "Прощания: Adiós — формально или надолго, Hasta luego — до скорого, Hasta mañana — до завтра.",
            sections = listOf(
                LessonSection(
                    heading = "Прощания",
                    items = listOf(
                        LessonItem("Adiós", "До свидания", "формально / надолго"),
                        LessonItem("Hasta luego", "До скорого", "увидимся ещё сегодня"),
                        LessonItem("Hasta mañana", "До завтра", ""),
                        LessonItem("Hasta pronto", "До встречи", ""),
                        LessonItem("Chao", "Пока!", "разговорно"),
                        LessonItem("Nos vemos", "Увидимся", "дружески"),
                    ),
                ),
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MATCH_PAIRS,
                    instruction = "Прощание ↔ перевод",
                    correctAnswer = "ok",
                    pairs = listOf(
                        "Adiós" to "До свидания",
                        "Hasta luego" to "До скорого",
                        "Hasta mañana" to "До завтра",
                        "Chao" to "Пока (разг.)",
                        "Nos vemos" to "Увидимся",
                    ),
                ),
                Exercise(
                    type = ExerciseType.LISTEN_PICK,
                    instruction = "Послушай и выбери",
                    audioText = "Hasta mañana",
                    options = listOf("Hasta mañana", "Hasta luego", "Hasta pronto", "Adiós"),
                    correctAnswer = "Hasta mañana",
                    explanation = "Hasta mañana = до завтра. mañana = утро/завтра.",
                ),
                Exercise(
                    type = ExerciseType.BUILD_SENTENCE,
                    instruction = "Собери: «До завтра, Мария!»",
                    words = listOf("Hasta", "mañana", "María"),
                    correctAnswer = "Hasta mañana María",
                    explanation = "Конструкция «Hasta + время» = «до …».",
                ),
                Exercise(
                    type = ExerciseType.TAP_MISSING_WORD,
                    instruction = "Заполни пропуск",
                    question = "Hasta ___ (увидимся в понедельник)",
                    options = listOf("el lunes", "mañana", "luego"),
                    correctAnswer = "el lunes",
                    explanation = "Hasta + день недели: «до понедельника».",
                ),
                Exercise(
                    type = ExerciseType.DIALOGUE_FILL,
                    instruction = "Закончи диалог",
                    dialogueLines = listOf(
                        "👩 María" to "Bueno, me voy.",
                        "👨 Tú" to "¡___ luego!",
                        "👩 María" to "¡Chao!",
                    ),
                    options = listOf("Hasta", "Buenos", "Adiós"),
                    correctAnswer = "Hasta",
                    explanation = "Hasta luego = «до скорого».",
                ),
                Exercise(
                    type = ExerciseType.TRANSLATE,
                    instruction = "Переведи: «До завтра!»",
                    correctAnswer = "Hasta mañana",
                    explanation = "Hasta mañana — стандартное «до завтра».",
                ),
            ),
        ),

        // ─────────────────────────────────────────────────────────────
        // u1_l6 — Por favor / Gracias / De nada / Perdón
        // ─────────────────────────────────────────────────────────────
        "u1_l6" to LessonContent(
            intro = "С por favor + gracias тебя поймут везде, даже без других слов. Perdón — за мелочь, Lo siento — за серьёзное.",
            sections = listOf(
                LessonSection(
                    heading = "Вежливость",
                    items = listOf(
                        LessonItem("Por favor", "Пожалуйста", "при просьбе"),
                        LessonItem("Gracias", "Спасибо", ""),
                        LessonItem("Muchas gracias", "Большое спасибо", ""),
                        LessonItem("De nada", "Не за что", "ответ на gracias"),
                        LessonItem("Perdón", "Извини", "за мелочь"),
                        LessonItem("Lo siento", "Сожалею", "серьёзно"),
                        LessonItem("Disculpe", "Извините", "к незнакомцу"),
                    ),
                ),
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MATCH_PAIRS,
                    instruction = "Соедини фразу и перевод",
                    correctAnswer = "ok",
                    pairs = listOf(
                        "Por favor" to "Пожалуйста",
                        "Gracias" to "Спасибо",
                        "De nada" to "Не за что",
                        "Perdón" to "Извини",
                        "Lo siento" to "Сожалею",
                    ),
                ),
                Exercise(
                    type = ExerciseType.LISTEN_PICK,
                    instruction = "Послушай и выбери",
                    audioText = "Muchas gracias",
                    options = listOf("Muchas gracias", "Por favor", "De nada", "Lo siento"),
                    correctAnswer = "Muchas gracias",
                    explanation = "Muchas gracias = большое спасибо.",
                ),
                Exercise(
                    type = ExerciseType.BUILD_SENTENCE,
                    instruction = "Собери: «Один кофе, пожалуйста»",
                    words = listOf("Un", "café", "por", "favor"),
                    correctAnswer = "Un café por favor",
                    explanation = "Стандартный заказ в кафе. por favor — в конце фразы.",
                ),
                Exercise(
                    type = ExerciseType.TAP_MISSING_WORD,
                    instruction = "Заполни пропуск",
                    question = "— Gracias.\n— ___ nada.",
                    options = listOf("De", "Por", "Lo"),
                    correctAnswer = "De",
                    explanation = "De nada — стандартный ответ на «спасибо».",
                ),
                Exercise(
                    type = ExerciseType.DIALOGUE_FILL,
                    instruction = "В кафе",
                    dialogueLines = listOf(
                        "👨 Tú" to "Un café, ___.",
                        "👩 Camarera" to "Aquí tiene.",
                        "👨 Tú" to "Gracias.",
                    ),
                    options = listOf("por favor", "de nada", "perdón"),
                    correctAnswer = "por favor",
                    explanation = "por favor — «пожалуйста» при просьбе.",
                ),
                Exercise(
                    type = ExerciseType.TRANSLATE,
                    instruction = "Переведи: «Извини» (за мелочь)",
                    correctAnswer = "Perdón",
                    explanation = "Perdón — лёгкое извинение. Lo siento — за серьёзное.",
                ),
            ),
        ),

        // ─────────────────────────────────────────────────────────────
        // u1_l7 — SER: soy, eres, es
        // ─────────────────────────────────────────────────────────────
        "u1_l7" to LessonContent(
            intro = "SER = «быть постоянно»: имя, национальность, профессия. yo soy / tú eres / él es.",
            sections = listOf(
                LessonSection(
                    heading = "SER ед.ч.",
                    items = listOf(
                        LessonItem("yo soy", "я есть", "Soy ruso"),
                        LessonItem("tú eres", "ты есть", "Eres mi amigo"),
                        LessonItem("él / ella es", "он / она есть", "Es médico"),
                        LessonItem("usted es", "Вы есть", "формально"),
                    ),
                ),
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.TAP_MISSING_WORD,
                    instruction = "Вставь форму SER",
                    question = "Yo ___ ruso.",
                    options = listOf("soy", "eres", "es"),
                    correctAnswer = "soy",
                    explanation = "yo soy — я есть. Soy ruso = я русский.",
                ),
                Exercise(
                    type = ExerciseType.BUILD_SENTENCE,
                    instruction = "Собери: «Ты мой друг»",
                    words = listOf("Tú", "eres", "mi", "amigo"),
                    correctAnswer = "Tú eres mi amigo",
                    explanation = "tú eres — ты есть.",
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Какая форма для «он»?",
                    question = "Él ___ médico.",
                    options = listOf("es", "soy", "eres", "son"),
                    correctAnswer = "es",
                    explanation = "él es — он есть. Es médico = он врач.",
                ),
                Exercise(
                    type = ExerciseType.SPOT_THE_ERROR,
                    instruction = "Найди ошибку",
                    question = "",
                    errorVariants = listOf(
                        "Yo soy ruso",
                        "Tú eres alto",
                        "Yo es Pablo",
                        "Ella es médica",
                    ),
                    correctAnswer = "Yo es Pablo",
                    explanation = "Yo требует soy, не es. Правильно: Yo soy Pablo.",
                ),
                Exercise(
                    type = ExerciseType.TAP_MISSING_WORD,
                    instruction = "Заполни пропуск",
                    question = "Tú ___ mi amigo.",
                    options = listOf("eres", "soy", "es"),
                    correctAnswer = "eres",
                    explanation = "tú eres = ты есть.",
                ),
                Exercise(
                    type = ExerciseType.LISTEN_TYPE,
                    instruction = "Послушай и напечатай",
                    audioText = "soy",
                    correctAnswer = "soy",
                    explanation = "yo soy — форма для «я».",
                ),
            ),
        ),

        // ─────────────────────────────────────────────────────────────
        // u1_l8 — SER: somos, sois, son (мн.ч.)
        // ─────────────────────────────────────────────────────────────
        "u1_l8" to LessonContent(
            intro = "Множественное SER: nosotros somos / vosotros sois (Испания) / ellos son.",
            sections = listOf(
                LessonSection(
                    heading = "SER мн.ч.",
                    items = listOf(
                        LessonItem("nosotros somos", "мы есть", "Somos amigos"),
                        LessonItem("vosotros sois", "вы есть (Испания)", "¿Sois de aquí?"),
                        LessonItem("ellos / ellas son", "они есть", "Son estudiantes"),
                        LessonItem("ustedes son", "Вы есть (мн)", "везде формально"),
                    ),
                ),
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.TAP_MISSING_WORD,
                    instruction = "Вставь форму SER",
                    question = "Nosotros ___ amigos.",
                    options = listOf("somos", "sois", "son"),
                    correctAnswer = "somos",
                    explanation = "nosotros somos = мы есть.",
                ),
                Exercise(
                    type = ExerciseType.BUILD_SENTENCE,
                    instruction = "Собери: «Они студенты»",
                    words = listOf("Ellos", "son", "estudiantes"),
                    correctAnswer = "Ellos son estudiantes",
                    explanation = "ellos son = они есть.",
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Какая форма для «vosotros»?",
                    question = "Vosotros ___ de aquí.",
                    options = listOf("sois", "somos", "son", "es"),
                    correctAnswer = "sois",
                    explanation = "vosotros sois — только в Испании.",
                ),
                Exercise(
                    type = ExerciseType.SPOT_THE_ERROR,
                    instruction = "Найди ошибку",
                    question = "",
                    errorVariants = listOf(
                        "Nosotros somos amigos",
                        "Ellos son médicos",
                        "Vosotros son altos",
                        "Ustedes son amables",
                    ),
                    correctAnswer = "Vosotros son altos",
                    explanation = "vosotros требует sois, не son. Правильно: Vosotros sois altos.",
                ),
                Exercise(
                    type = ExerciseType.TRANSLATE,
                    instruction = "Переведи: «Мы друзья»",
                    correctAnswer = "Somos amigos",
                    explanation = "nosotros можно опустить — somos уже содержит «мы».",
                ),
                Exercise(
                    type = ExerciseType.LISTEN_TYPE,
                    instruction = "Послушай и напечатай",
                    audioText = "somos",
                    correctAnswer = "somos",
                    explanation = "nosotros somos — форма для «мы».",
                ),
            ),
        ),

        // ─────────────────────────────────────────────────────────────
        // u1_l9 — Местоимения yo / tú / él / ella / nosotros / ellos
        // ─────────────────────────────────────────────────────────────
        "u1_l9" to LessonContent(
            intro = "yo, tú, él, ella, nosotros, ellos. Окончание глагола уже содержит лицо — местоимение часто опускают.",
            sections = listOf(
                LessonSection(
                    heading = "Личные местоимения",
                    items = listOf(
                        LessonItem("yo", "я", ""),
                        LessonItem("tú", "ты", "к другу"),
                        LessonItem("usted", "Вы", "формально"),
                        LessonItem("él / ella", "он / она", ""),
                        LessonItem("nosotros / nosotras", "мы (м/ж)", ""),
                        LessonItem("vosotros / vosotras", "вы (Испания)", ""),
                        LessonItem("ellos / ellas", "они (м/ж)", ""),
                    ),
                ),
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.TAP_MISSING_WORD,
                    instruction = "Вставь местоимение",
                    question = "___ soy estudiante.",
                    options = listOf("Yo", "Tú", "Él"),
                    correctAnswer = "Yo",
                    explanation = "soy — форма для yo. Местоимение можно опустить, но если стоит — оно «yo».",
                ),
                Exercise(
                    type = ExerciseType.BUILD_SENTENCE,
                    instruction = "Собери: «Он мой брат»",
                    words = listOf("Él", "es", "mi", "hermano"),
                    correctAnswer = "Él es mi hermano",
                    explanation = "él es — он есть.",
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Какое местоимение для группы из 3 девушек?",
                    question = "",
                    options = listOf("nosotras", "nosotros", "ellos", "vosotros"),
                    correctAnswer = "nosotras",
                    explanation = "nosotras — только женщины. nosotros — если есть хоть один мужчина.",
                ),
                Exercise(
                    type = ExerciseType.SPOT_THE_ERROR,
                    instruction = "Найди ошибку",
                    question = "",
                    errorVariants = listOf(
                        "Yo soy",
                        "Tú eres",
                        "Él soy",
                        "Ella es",
                    ),
                    correctAnswer = "Él soy",
                    explanation = "él требует es, не soy. Правильно: Él es.",
                ),
                Exercise(
                    type = ExerciseType.TRANSLATE,
                    instruction = "Переведи: «Мы (девушки) русские»",
                    correctAnswer = "Nosotras somos rusas",
                    explanation = "Только девушки → nosotras. Прилагательное согласуется: rusas.",
                ),
                Exercise(
                    type = ExerciseType.LISTEN_TYPE,
                    instruction = "Послушай и напечатай",
                    audioText = "ellos",
                    correctAnswer = "ellos",
                    explanation = "ellos — они (м или смеш). ellas — только женщины.",
                ),
            ),
        ),

        // ─────────────────────────────────────────────────────────────
        // u1_l10 — Род: el/la
        // ─────────────────────────────────────────────────────────────
        "u1_l10" to LessonContent(
            intro = "Каждое существительное — мужского ИЛИ женского рода. Большинство: -o → м, -a → ж. Артикль показывает род.",
            sections = listOf(
                LessonSection(
                    heading = "Род существительных",
                    items = listOf(
                        LessonItem("Окончание -o", "обычно мужской", "el libro — книга"),
                        LessonItem("Окончание -a", "обычно женский", "la casa — дом"),
                        LessonItem("Исключения", "el día (мужской), la mano (женский)", ""),
                    ),
                ),
                LessonSection(
                    heading = "Примеры",
                    items = listOf(
                        LessonItem("el libro", "книга (м)", ""),
                        LessonItem("la casa", "дом (ж)", ""),
                        LessonItem("el médico", "врач (м)", ""),
                        LessonItem("la médica", "врач (ж)", ""),
                        LessonItem("el día", "день (м! исключение)", ""),
                        LessonItem("la mano", "рука (ж! исключение)", ""),
                    ),
                ),
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Какой артикль?",
                    question = "___ casa",
                    options = listOf("la", "el", "los", "las"),
                    correctAnswer = "la",
                    explanation = "casa оканчивается на -a → женский → la casa.",
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Какой артикль?",
                    question = "___ libro",
                    options = listOf("el", "la", "un", "una"),
                    correctAnswer = "el",
                    explanation = "libro оканчивается на -o → мужской → el libro.",
                ),
                Exercise(
                    type = ExerciseType.SPOT_THE_ERROR,
                    instruction = "Найди ошибку",
                    question = "",
                    errorVariants = listOf(
                        "el libro",
                        "la casa",
                        "la día",
                        "el médico",
                    ),
                    correctAnswer = "la día",
                    explanation = "día — мужской род (исключение!). Правильно: el día.",
                ),
                Exercise(
                    type = ExerciseType.MATCH_PAIRS,
                    instruction = "Слово ↔ артикль",
                    correctAnswer = "ok",
                    pairs = listOf(
                        "casa" to "la",
                        "libro" to "el",
                        "día" to "el",
                        "mano" to "la",
                        "médico" to "el",
                    ),
                ),
                Exercise(
                    type = ExerciseType.TAP_MISSING_WORD,
                    instruction = "Вставь артикль",
                    question = "Tengo ___ libro.",
                    options = listOf("el", "la", "un"),
                    correctAnswer = "el",
                    explanation = "libro — мужской → el libro.",
                ),
                Exercise(
                    type = ExerciseType.TRANSLATE,
                    instruction = "Переведи: «дом»",
                    correctAnswer = "la casa",
                    explanation = "casa — женский → la casa.",
                ),
            ),
        ),

        // ─────────────────────────────────────────────────────────────
        // u1_l11 — Артикли: el/la/un/una/los/las
        // ─────────────────────────────────────────────────────────────
        "u1_l11" to LessonContent(
            intro = "Определённые el/la/los/las (известный объект) и неопределённые un/una/unos/unas (новый объект).",
            sections = listOf(
                LessonSection(
                    heading = "Все артикли",
                    items = listOf(
                        LessonItem("el / un", "м.ед.", "el libro / un libro"),
                        LessonItem("la / una", "ж.ед.", "la casa / una casa"),
                        LessonItem("los / unos", "м.мн.", "los libros / unos libros"),
                        LessonItem("las / unas", "ж.мн.", "las casas / unas casas"),
                    ),
                ),
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MATCH_PAIRS,
                    instruction = "Артикль ↔ род и число",
                    correctAnswer = "ok",
                    pairs = listOf(
                        "el" to "м.ед. определённый",
                        "una" to "ж.ед. неопределённый",
                        "los" to "м.мн. определённый",
                        "las" to "ж.мн. определённый",
                        "unos" to "м.мн. неопределённый",
                    ),
                ),
                Exercise(
                    type = ExerciseType.LISTEN_PICK,
                    instruction = "Послушай и выбери",
                    audioText = "una casa",
                    options = listOf("una casa", "un caso", "la casa", "unas casas"),
                    correctAnswer = "una casa",
                    explanation = "una casa — какой-то дом (неопределённый, единственный, женский).",
                ),
                Exercise(
                    type = ExerciseType.TAP_MISSING_WORD,
                    instruction = "Вставь артикль",
                    question = "Quiero ___ café (один, любой).",
                    options = listOf("un", "el", "una"),
                    correctAnswer = "un",
                    explanation = "café — мужской, любой → un café.",
                ),
                Exercise(
                    type = ExerciseType.SPOT_THE_ERROR,
                    instruction = "Найди ошибку",
                    question = "",
                    errorVariants = listOf(
                        "un libro",
                        "una casa",
                        "una libro",
                        "unos libros",
                    ),
                    correctAnswer = "una libro",
                    explanation = "libro — мужской → un libro, не una libro.",
                ),
                Exercise(
                    type = ExerciseType.DIALOGUE_FILL,
                    instruction = "В магазине",
                    dialogueLines = listOf(
                        "👩 Camarera" to "¿Qué desea?",
                        "👨 Tú" to "___ café, por favor.",
                        "👩 Camarera" to "Aquí tiene.",
                    ),
                    options = listOf("Un", "Una", "Los"),
                    correctAnswer = "Un",
                    explanation = "café — мужской → un café.",
                ),
                Exercise(
                    type = ExerciseType.TRANSLATE,
                    instruction = "Переведи: «дома (мн.)»",
                    correctAnswer = "las casas",
                    explanation = "casas — мн.ч. от casa → las casas.",
                ),
            ),
        ),

        // ─────────────────────────────────────────────────────────────
        // u1_l12 — Страны: Soy ruso/rusa, de Rusia
        // ─────────────────────────────────────────────────────────────
        "u1_l12" to LessonContent(
            intro = "Национальность согласуется по роду: ruso (м) / rusa (ж). Страна с предлогом «de Rusia».",
            sections = listOf(
                LessonSection(
                    heading = "Страны и национальности",
                    items = listOf(
                        LessonItem("Rusia / ruso / rusa", "Россия / русский", ""),
                        LessonItem("España / español / española", "Испания / испанец", ""),
                        LessonItem("México / mexicano / mexicana", "Мексика / мексиканец", ""),
                        LessonItem("Francia / francés / francesa", "Франция / француз", ""),
                        LessonItem("Inglaterra / inglés / inglesa", "Англия / англичанин", ""),
                    ),
                ),
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MATCH_PAIRS,
                    instruction = "Страна ↔ национальность (м)",
                    correctAnswer = "ok",
                    pairs = listOf(
                        "Rusia" to "ruso",
                        "España" to "español",
                        "México" to "mexicano",
                        "Francia" to "francés",
                        "Inglaterra" to "inglés",
                    ),
                ),
                Exercise(
                    type = ExerciseType.LISTEN_PICK,
                    instruction = "Послушай и выбери",
                    audioText = "Soy ruso",
                    options = listOf("Soy ruso", "Soy rusa", "Eres ruso", "Es rusa"),
                    correctAnswer = "Soy ruso",
                    explanation = "Soy ruso — я русский (мужчина).",
                ),
                Exercise(
                    type = ExerciseType.BUILD_SENTENCE,
                    instruction = "Собери: «Я из Испании»",
                    words = listOf("Soy", "de", "España"),
                    correctAnswer = "Soy de España",
                    explanation = "Конструкция «Soy de + страна».",
                ),
                Exercise(
                    type = ExerciseType.TAP_MISSING_WORD,
                    instruction = "Заполни (девушка)",
                    question = "Soy ___ (русская).",
                    options = listOf("rusa", "ruso", "Rusia"),
                    correctAnswer = "rusa",
                    explanation = "rusa — женская форма. ruso — мужская.",
                ),
                Exercise(
                    type = ExerciseType.SPOT_THE_ERROR,
                    instruction = "Найди ошибку",
                    question = "",
                    errorVariants = listOf(
                        "Soy ruso",
                        "Soy de España",
                        "Soy de español",
                        "Soy mexicana",
                    ),
                    correctAnswer = "Soy de español",
                    explanation = "После «de» — название СТРАНЫ, не национальность. Правильно: «Soy de España» или «Soy español».",
                ),
                Exercise(
                    type = ExerciseType.TRANSLATE,
                    instruction = "Переведи: «Я из России» (мужчина)",
                    correctAnswer = "Soy de Rusia",
                    explanation = "Soy de + название страны. Soy de Rusia.",
                ),
            ),
        ),

        // ─────────────────────────────────────────────────────────────
        // u1_l13 — Числа 0–10
        // ─────────────────────────────────────────────────────────────
        "u1_l13" to LessonContent(
            intro = "Числа от 0 до 10. uno → un перед существительным мужского рода (un café), una перед женским (una casa).",
            sections = listOf(
                LessonSection(
                    heading = "Числа 0–10",
                    items = listOf(
                        LessonItem("0", "cero", ""),
                        LessonItem("1", "uno (un / una)", ""),
                        LessonItem("2", "dos", ""),
                        LessonItem("3", "tres", ""),
                        LessonItem("4", "cuatro", ""),
                        LessonItem("5", "cinco", ""),
                        LessonItem("6", "seis", ""),
                        LessonItem("7", "siete", ""),
                        LessonItem("8", "ocho", ""),
                        LessonItem("9", "nueve", ""),
                        LessonItem("10", "diez", ""),
                    ),
                ),
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.LISTEN_NUMBER_TAP,
                    instruction = "Услышь число → нажми цифру",
                    audioText = "cinco",
                    number = 5,
                    correctAnswer = "5",
                    explanation = "cinco = 5.",
                ),
                Exercise(
                    type = ExerciseType.LISTEN_NUMBER_TAP,
                    instruction = "Услышь число → нажми цифру",
                    audioText = "ocho",
                    number = 8,
                    correctAnswer = "8",
                    explanation = "ocho = 8.",
                ),
                Exercise(
                    type = ExerciseType.READ_NUMBER,
                    instruction = "Какое испанское слово для «3»?",
                    question = "3",
                    options = listOf("tres", "trece", "treinta", "cuatro"),
                    correctAnswer = "tres",
                    explanation = "tres = 3. trece = 13. treinta = 30.",
                ),
                Exercise(
                    type = ExerciseType.MATCH_PAIRS,
                    instruction = "Цифра ↔ слово",
                    correctAnswer = "ok",
                    pairs = listOf(
                        "1" to "uno",
                        "3" to "tres",
                        "5" to "cinco",
                        "7" to "siete",
                        "10" to "diez",
                    ),
                ),
                Exercise(
                    type = ExerciseType.TAP_MISSING_WORD,
                    instruction = "Вставь правильную форму «1»",
                    question = "___ café, por favor.",
                    options = listOf("Un", "Una", "Uno"),
                    correctAnswer = "Un",
                    explanation = "Перед мужским существительным uno → un. Un café.",
                ),
                Exercise(
                    type = ExerciseType.LISTEN_TYPE,
                    instruction = "Послушай и напечатай",
                    audioText = "cinco",
                    correctAnswer = "cinco",
                    explanation = "cinco = 5.",
                ),
            ),
        ),

        // ─────────────────────────────────────────────────────────────
        // u1_l13_5 — НОВЫЙ урок: Порядковые числительные 1°–10°
        // ─────────────────────────────────────────────────────────────
        // Заглушка-маркер: блок 1.1 заканчивается на u1_l13_5.
        // ──────────────────────────────────────────────────────
        "u1_l13_5" to LessonContent(
            intro = "Порядковые числительные: primero, segundo, tercero. Согласуются по роду (primer/primera) и могут терять -o перед существительным.",
            sections = listOf(
                LessonSection(
                    heading = "Порядковые 1°–10°",
                    items = listOf(
                        LessonItem("1°", "primero (primer)", "el primer día"),
                        LessonItem("2°", "segundo / segunda", ""),
                        LessonItem("3°", "tercero (tercer)", "el tercer piso"),
                        LessonItem("4°", "cuarto / cuarta", ""),
                        LessonItem("5°", "quinto / quinta", ""),
                        LessonItem("6°", "sexto / sexta", ""),
                        LessonItem("7°", "séptimo / séptima", ""),
                        LessonItem("8°", "octavo / octava", ""),
                        LessonItem("9°", "noveno / novena", ""),
                        LessonItem("10°", "décimo / décima", ""),
                    ),
                ),
                LessonSection(
                    heading = "Главное правило",
                    items = listOf(
                        LessonItem("primero / tercero", "теряют -o перед мужским сущ.", "el primer día (не «primero día»)"),
                        LessonItem("женский род", "primera / tercera", "la primera vez"),
                    ),
                ),
            ),
            exercises = listOf(
                Exercise(
                    type = ExerciseType.MATCH_PAIRS,
                    instruction = "Соедини",
                    correctAnswer = "ok",
                    pairs = listOf(
                        "1°" to "primero",
                        "2°" to "segundo",
                        "3°" to "tercero",
                        "5°" to "quinto",
                        "10°" to "décimo",
                    ),
                ),
                Exercise(
                    type = ExerciseType.TAP_MISSING_WORD,
                    instruction = "Вставь правильную форму",
                    question = "Es el ___ día. (1°, мужской)",
                    options = listOf("primer", "primero", "primera"),
                    correctAnswer = "primer",
                    explanation = "primero теряет -o перед мужским сущ. → el primer día.",
                ),
                Exercise(
                    type = ExerciseType.MULTIPLE_CHOICE,
                    instruction = "Какая форма для «la ___ vez» (1-й раз)?",
                    question = "",
                    options = listOf("primera", "primero", "primer", "uno"),
                    correctAnswer = "primera",
                    explanation = "vez — женский → la primera vez.",
                ),
                Exercise(
                    type = ExerciseType.SPOT_THE_ERROR,
                    instruction = "Найди ошибку",
                    question = "",
                    errorVariants = listOf(
                        "el primer día",
                        "el tercer piso",
                        "la primera vez",
                        "el primero hijo",
                    ),
                    correctAnswer = "el primero hijo",
                    explanation = "primero теряет -o перед мужским → el primer hijo.",
                ),
                Exercise(
                    type = ExerciseType.BUILD_SENTENCE,
                    instruction = "Собери: «Это мой второй кофе»",
                    words = listOf("Es", "mi", "segundo", "café"),
                    correctAnswer = "Es mi segundo café",
                    explanation = "segundo НЕ теряет -o (только primero/tercero).",
                ),
                Exercise(
                    type = ExerciseType.TRANSLATE,
                    instruction = "Переведи: «третий этаж»",
                    correctAnswer = "el tercer piso",
                    explanation = "tercero → tercer перед мужским. piso (этаж) — мужской.",
                ),
            ),
        ),

    )

    // ═══════════════════════════════════════════════════════════════
    //  БЛОК 1.2 «МОЙ МИР» — числа, TENER, семья, цвета, ESTAR, дом
    //  15 уроков: u2_l0..u2_l14 (u2_l14 = checkpoint «Аренда жилья»)
    // ═══════════════════════════════════════════════════════════════

    private fun blockA1_2(): Map<String, LessonContent> = mapOf(

        // u2_l0 — Числа 11-20
        "u2_l0" to LessonContent(
            intro = "Числа 11-15 нерегулярные (once-quince), 16-19 — diez+y+ед.: dieciséis, veinte = 20.",
            sections = listOf(
                LessonSection("Числа 11-20", listOf(
                    LessonItem("11", "once", ""), LessonItem("12", "doce", ""),
                    LessonItem("13", "trece", ""), LessonItem("14", "catorce", ""),
                    LessonItem("15", "quince", ""), LessonItem("16", "dieciséis", ""),
                    LessonItem("17", "diecisiete", ""), LessonItem("18", "dieciocho", ""),
                    LessonItem("19", "diecinueve", ""), LessonItem("20", "veinte", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "Цифра ↔ слово", correctAnswer = "ok",
                    pairs = listOf("11" to "once", "13" to "trece", "15" to "quince", "17" to "diecisiete", "20" to "veinte")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай и выбери", audioText = "doce",
                    options = listOf("doce", "once", "trece", "catorce"), correctAnswer = "doce",
                    explanation = "doce = 12."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «У меня 15 евро»",
                    words = listOf("Tengo", "quince", "euros"), correctAnswer = "Tengo quince euros",
                    explanation = "quince = 15."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "Tengo ___ años. (мне 18)",
                    options = listOf("dieciocho", "ocho", "diez"), correctAnswer = "dieciocho",
                    explanation = "dieciocho = 18 = «10+8»."),
                Exercise(ExerciseType.DIALOGUE_FILL, "Возраст",
                    dialogueLines = listOf("👩 María" to "¿Cuántos años tienes?", "👨 Tú" to "Tengo ___ años."),
                    options = listOf("veinte", "venti", "diez"), correctAnswer = "veinte",
                    explanation = "veinte = 20."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «семнадцать»",
                    correctAnswer = "diecisiete", explanation = "diecisiete = 17 = «10+7»."),
            ),
        ),

        // u2_l1 — Числа 21-100
        "u2_l1" to LessonContent(
            intro = "21-29 пишутся слитно (veintiuno-veintinueve). 30/40/.../90 — десятки. 31-99 через «y»: treinta y uno.",
            sections = listOf(
                LessonSection("Десятки и составные", listOf(
                    LessonItem("21", "veintiuno", ""), LessonItem("22", "veintidós", ""),
                    LessonItem("30", "treinta", ""), LessonItem("31", "treinta y uno", ""),
                    LessonItem("40", "cuarenta", ""), LessonItem("50", "cincuenta", ""),
                    LessonItem("60", "sesenta", ""), LessonItem("70", "setenta", ""),
                    LessonItem("80", "ochenta", ""), LessonItem("90", "noventa", ""),
                    LessonItem("100", "cien", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.LISTEN_NUMBER_TAP, "Нажми цифру",
                    audioText = "cincuenta y tres", number = 53, correctAnswer = "53",
                    explanation = "cincuenta y tres = 50 + 3 = 53."),
                Exercise(ExerciseType.LISTEN_NUMBER_TAP, "Нажми цифру",
                    audioText = "noventa y nueve", number = 99, correctAnswer = "99",
                    explanation = "noventa y nueve = 90 + 9 = 99."),
                Exercise(ExerciseType.READ_NUMBER, "Какое слово для 47?", question = "47",
                    options = listOf("cuarenta y siete", "catorce", "cincuenta y siete", "cuarenta"),
                    correctAnswer = "cuarenta y siete", explanation = "cuarenta y siete = 40 + 7."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "Cuesta ___ euros. (35)",
                    options = listOf("treinta y cinco", "trece", "tres y cinco"), correctAnswer = "treinta y cinco",
                    explanation = "35 = treinta y cinco."),
                Exercise(ExerciseType.DIALOGUE_FILL, "В магазине",
                    dialogueLines = listOf("👩 Vendedora" to "Son ___ euros.", "👨 Tú" to "Aquí tiene."),
                    options = listOf("veinticinco", "veintiuno", "venticinco"), correctAnswer = "veinticinco",
                    explanation = "25 = veinticinco (слитно!)."),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf("treinta y uno", "veintiuno", "treintaiuno", "cuarenta y dos"),
                    correctAnswer = "treintaiuno",
                    explanation = "31 пишется РАЗДЕЛЬНО: treinta y uno. Слитно — только 21-29 (veintiuno)."),
            ),
        ),

        // u2_l2 — TENER ед.ч.
        "u2_l2" to LessonContent(
            intro = "TENER = «иметь»: tengo / tienes / tiene. Используется для возраста, родства, владения.",
            sections = listOf(
                LessonSection("TENER ед.ч.", listOf(
                    LessonItem("yo tengo", "у меня есть", "Tengo un perro"),
                    LessonItem("tú tienes", "у тебя есть", "Tienes razón"),
                    LessonItem("él/ella tiene", "у него/неё есть", "Tiene 20 años"),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.TAP_MISSING_WORD, "Вставь TENER", question = "Yo ___ un hermano.",
                    options = listOf("tengo", "tienes", "tiene"), correctAnswer = "tengo",
                    explanation = "yo tengo = у меня есть."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «У тебя есть кот?»",
                    words = listOf("¿Tienes", "un", "gato?"), correctAnswer = "¿Tienes un gato?",
                    explanation = "Tienes — для tú."),
                Exercise(ExerciseType.MULTIPLE_CHOICE, "Какая форма для él?", question = "Él ___ 30 años.",
                    options = listOf("tiene", "tengo", "tienes", "tienen"), correctAnswer = "tiene",
                    explanation = "él tiene = у него есть. Возраст: tener X años."),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf("Yo tengo razón", "Tú tienes 20 años", "Yo tiene un perro", "Ella tiene casa"),
                    correctAnswer = "Yo tiene un perro",
                    explanation = "Yo требует tengo. Правильно: Yo tengo un perro."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «У меня есть друг»",
                    correctAnswer = "Tengo un amigo",
                    explanation = "yo можно опустить."),
                Exercise(ExerciseType.LISTEN_TYPE, "Послушай", audioText = "tengo",
                    correctAnswer = "tengo", explanation = "yo tengo."),
            ),
        ),

        // u2_l3 — TENER мн.ч.
        "u2_l3" to LessonContent(
            intro = "Множественное TENER: tenemos / tenéis / tienen. Похоже на SER по структуре окончаний.",
            sections = listOf(
                LessonSection("TENER мн.ч.", listOf(
                    LessonItem("nosotros tenemos", "у нас есть", ""),
                    LessonItem("vosotros tenéis", "у вас есть (Исп.)", ""),
                    LessonItem("ellos tienen", "у них есть", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.TAP_MISSING_WORD, "Вставь форму", question = "Nosotros ___ una casa.",
                    options = listOf("tenemos", "tenéis", "tienen"), correctAnswer = "tenemos",
                    explanation = "nosotros tenemos."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «У них две дочери»",
                    words = listOf("Tienen", "dos", "hijas"), correctAnswer = "Tienen dos hijas",
                    explanation = "ellos tienen — местоимение опускаем."),
                Exercise(ExerciseType.MULTIPLE_CHOICE, "Какая форма для vosotros?", question = "Vosotros ___ razón.",
                    options = listOf("tenéis", "tienen", "tenemos", "tienes"), correctAnswer = "tenéis",
                    explanation = "vosotros tenéis — только в Испании."),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf("Tenemos razón", "Tenéis casa", "Tienen 20 años", "Vosotros tienen libros"),
                    correctAnswer = "Vosotros tienen libros",
                    explanation = "vosotros требует tenéis. Правильно: Vosotros tenéis libros."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «У нас три кота»",
                    correctAnswer = "Tenemos tres gatos", explanation = "nosotros опускаем."),
                Exercise(ExerciseType.LISTEN_TYPE, "Послушай", audioText = "tenemos",
                    correctAnswer = "tenemos", explanation = "nosotros tenemos."),
            ),
        ),

        // u2_l4 — Семья 1
        "u2_l4" to LessonContent(
            intro = "Базовая семья: padre/madre, hermano/hermana, hijo/hija. Заметь окончания -o/-a.",
            sections = listOf(
                LessonSection("Семья — основа", listOf(
                    LessonItem("padre", "отец", ""), LessonItem("madre", "мать", ""),
                    LessonItem("hermano", "брат", ""), LessonItem("hermana", "сестра", ""),
                    LessonItem("hijo", "сын", ""), LessonItem("hija", "дочь", ""),
                    LessonItem("padres", "родители", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "Член семьи ↔ перевод", correctAnswer = "ok",
                    pairs = listOf("padre" to "отец", "madre" to "мать", "hermano" to "брат", "hija" to "дочь", "padres" to "родители")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай", audioText = "hermana",
                    options = listOf("hermana", "hermano", "hija", "madre"), correctAnswer = "hermana",
                    explanation = "hermana = сестра."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Мой отец — врач»",
                    words = listOf("Mi", "padre", "es", "médico"), correctAnswer = "Mi padre es médico",
                    explanation = "mi = мой / моя."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "Tengo dos ___ (брата).",
                    options = listOf("hermanos", "hermanas", "hijos"), correctAnswer = "hermanos",
                    explanation = "hermanos = братья (или смешанная группа)."),
                Exercise(ExerciseType.DIALOGUE_FILL, "Знакомство",
                    dialogueLines = listOf("👩 María" to "¿Tienes hermanos?", "👨 Tú" to "Sí, tengo una ___ y un hermano."),
                    options = listOf("hermana", "madre", "hija"), correctAnswer = "hermana",
                    explanation = "una hermana = одна сестра."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «У меня есть мать»",
                    correctAnswer = "Tengo madre", explanation = "Артикль не нужен — родство."),
            ),
        ),

        // u2_l5 — Семья 2
        "u2_l5" to LessonContent(
            intro = "Расширенная семья: abuelo (дед), tío (дядя), primo (двоюродный), sobrino (племянник).",
            sections = listOf(
                LessonSection("Расширенная семья", listOf(
                    LessonItem("abuelo / abuela", "дед / бабушка", ""),
                    LessonItem("tío / tía", "дядя / тётя", ""),
                    LessonItem("primo / prima", "двоюродный брат/сестра", ""),
                    LessonItem("sobrino / sobrina", "племянник / племянница", ""),
                    LessonItem("nieto / nieta", "внук / внучка", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "Соедини", correctAnswer = "ok",
                    pairs = listOf("abuelo" to "дед", "tío" to "дядя", "prima" to "двоюр.сестра", "sobrino" to "племянник", "nieta" to "внучка")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай", audioText = "abuela",
                    options = listOf("abuela", "abuelo", "tía", "madre"), correctAnswer = "abuela",
                    explanation = "abuela = бабушка."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Моя бабушка из Мадрида»",
                    words = listOf("Mi", "abuela", "es", "de", "Madrid"), correctAnswer = "Mi abuela es de Madrid",
                    explanation = "mi abuela — род подсказывает -a."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "Mi ___ tiene un perro. (тётя)",
                    options = listOf("tía", "tío", "abuela"), correctAnswer = "tía",
                    explanation = "tía = тётя (ж)."),
                Exercise(ExerciseType.DIALOGUE_FILL, "Семейное фото",
                    dialogueLines = listOf("👩 María" to "¿Quién es ella?", "👨 Tú" to "Es mi ___ Carmen."),
                    options = listOf("prima", "primo", "tío"), correctAnswer = "prima",
                    explanation = "Carmen — женское имя → prima."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «У меня два дяди»",
                    correctAnswer = "Tengo dos tíos", explanation = "tíos = дяди."),
            ),
        ),

        // u2_l6 — Притяжательные mi/tu/su/nuestro
        "u2_l6" to LessonContent(
            intro = "mi / tu / su — единственная форма для м и ж. nuestro/a меняется по роду. Согласуются по числу.",
            sections = listOf(
                LessonSection("Притяжательные", listOf(
                    LessonItem("mi", "мой/моя", "mi padre, mi madre"),
                    LessonItem("mis", "мои", "mis padres"),
                    LessonItem("tu", "твой/твоя", "tu hermano"),
                    LessonItem("su", "его/её/Ваш", "su casa"),
                    LessonItem("nuestro/a", "наш/наша", "nuestra casa"),
                    LessonItem("vuestro/a", "ваш (Исп.)", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.TAP_MISSING_WORD, "Вставь притяжательное", question = "___ padre es médico. (мой)",
                    options = listOf("Mi", "Tu", "Su"), correctAnswer = "Mi",
                    explanation = "mi — мой/моя. Не меняется по роду."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Это наш дом»",
                    words = listOf("Es", "nuestra", "casa"), correctAnswer = "Es nuestra casa",
                    explanation = "casa — ж → nuestra."),
                Exercise(ExerciseType.MULTIPLE_CHOICE, "Какая форма для «мои родители»?", question = "",
                    options = listOf("mis padres", "mi padres", "mio padres", "míos padres"),
                    correctAnswer = "mis padres",
                    explanation = "mn → mis. mi padre / mis padres."),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf("mi madre", "tu hermano", "nuestro casa", "su libro"),
                    correctAnswer = "nuestro casa",
                    explanation = "casa — ж → nuestra casa, не nuestro."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «твой брат»",
                    correctAnswer = "tu hermano", explanation = "tu (без тильды) = «твой»."),
                Exercise(ExerciseType.LISTEN_TYPE, "Послушай", audioText = "nuestra",
                    correctAnswer = "nuestra", explanation = "nuestra — ж форма от nuestro."),
            ),
        ),

        // u2_l7 — Цвета
        "u2_l7" to LessonContent(
            intro = "Базовые цвета: rojo, azul, verde, amarillo, negro, blanco, gris.",
            sections = listOf(
                LessonSection("Цвета", listOf(
                    LessonItem("rojo", "красный", ""), LessonItem("azul", "синий", ""),
                    LessonItem("verde", "зелёный", ""), LessonItem("amarillo", "жёлтый", ""),
                    LessonItem("negro", "чёрный", ""), LessonItem("blanco", "белый", ""),
                    LessonItem("gris", "серый", ""), LessonItem("naranja", "оранжевый", ""),
                    LessonItem("rosa", "розовый", ""), LessonItem("marrón", "коричневый", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "Цвет ↔ перевод", correctAnswer = "ok",
                    pairs = listOf("rojo" to "красный", "azul" to "синий", "verde" to "зелёный", "amarillo" to "жёлтый", "negro" to "чёрный")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай", audioText = "blanco",
                    options = listOf("blanco", "negro", "azul", "rojo"), correctAnswer = "blanco",
                    explanation = "blanco = белый."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Мой кот чёрный»",
                    words = listOf("Mi", "gato", "es", "negro"), correctAnswer = "Mi gato es negro",
                    explanation = "negro — мужская форма."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "El cielo es ___ (синее).",
                    options = listOf("azul", "verde", "rojo"), correctAnswer = "azul",
                    explanation = "azul — не меняется по роду (заканчивается на -l)."),
                Exercise(ExerciseType.DIALOGUE_FILL, "В магазине одежды",
                    dialogueLines = listOf("👩 Vendedora" to "¿De qué color?", "👨 Tú" to "Lo quiero en ___ (красном)."),
                    options = listOf("rojo", "azul", "verde"), correctAnswer = "rojo",
                    explanation = "rojo = красный."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «зелёный»",
                    correctAnswer = "verde", explanation = "verde — не меняется по роду."),
            ),
        ),

        // u2_l8 — Согласование цветов
        "u2_l8" to LessonContent(
            intro = "Цвета на -o согласуются: rojo/roja, blanco/blanca. На -e и согласные не меняются: verde, azul.",
            sections = listOf(
                LessonSection("Согласование", listOf(
                    LessonItem("rojo / roja", "красный / красная", "el coche rojo / la casa roja"),
                    LessonItem("blanco / blanca", "белый / белая", ""),
                    LessonItem("negro / negra", "чёрный / чёрная", ""),
                    LessonItem("verde", "не меняется", "el coche verde / la casa verde"),
                    LessonItem("azul", "не меняется", ""),
                    LessonItem("gris", "не меняется", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "Соедини м/ж формы", correctAnswer = "ok",
                    pairs = listOf("rojo" to "roja", "blanco" to "blanca", "negro" to "negra", "amarillo" to "amarilla", "verde" to "verde")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай", audioText = "blanca",
                    options = listOf("blanca", "blanco", "negra", "negra"), correctAnswer = "blanca",
                    explanation = "blanca — ж форма."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «У меня белая машина»",
                    words = listOf("Tengo", "un", "coche", "blanco"), correctAnswer = "Tengo un coche blanco",
                    explanation = "coche — м → blanco."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "La casa es ___ (красная).",
                    options = listOf("roja", "rojo", "rojas"), correctAnswer = "roja",
                    explanation = "casa — ж → roja."),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf("la casa roja", "el coche rojo", "la casa rojo", "el libro negro"),
                    correctAnswer = "la casa rojo",
                    explanation = "casa — ж → roja, не rojo."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «зелёная книга»",
                    correctAnswer = "el libro verde",
                    explanation = "verde не меняется по роду. libro — м, поэтому el libro verde."),
            ),
        ),

        // u2_l9 — ESTAR ед.ч.
        "u2_l9" to LessonContent(
            intro = "ESTAR = «быть» о ВРЕМЕННОМ: где находишься, как себя чувствуешь. estoy / estás / está.",
            sections = listOf(
                LessonSection("ESTAR ед.ч.", listOf(
                    LessonItem("yo estoy", "я нахожусь / чувствую", "Estoy en casa"),
                    LessonItem("tú estás", "ты ...", "¿Cómo estás?"),
                    LessonItem("él/ella está", "он/она ...", "Está cansado"),
                )),
                LessonSection("Когда ESTAR", listOf(
                    LessonItem("Местоположение", "Estoy en Madrid", ""),
                    LessonItem("Состояние", "Estoy cansado / contento", ""),
                    LessonItem("НЕ для постоянных", "национальность → SER", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.TAP_MISSING_WORD, "Вставь ESTAR", question = "Yo ___ en casa.",
                    options = listOf("estoy", "estás", "está"), correctAnswer = "estoy",
                    explanation = "yo estoy = я нахожусь."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Где ты?»",
                    words = listOf("¿Dónde", "estás?"), correctAnswer = "¿Dónde estás?",
                    explanation = "estar для местоположения."),
                Exercise(ExerciseType.MULTIPLE_CHOICE, "Какая форма для él?", question = "Él ___ cansado.",
                    options = listOf("está", "es", "estoy", "están"), correctAnswer = "está",
                    explanation = "Состояние (усталость) → ESTAR. él está."),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf("Estoy en Madrid", "Estás cansado", "Soy en casa", "Está bien"),
                    correctAnswer = "Soy en casa",
                    explanation = "Местоположение требует ESTAR. Правильно: Estoy en casa."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «Я в кафе»",
                    correctAnswer = "Estoy en el café",
                    explanation = "Местоположение → ESTAR. café — м → el."),
                Exercise(ExerciseType.LISTEN_TYPE, "Послушай", audioText = "estoy",
                    correctAnswer = "estoy", explanation = "yo estoy."),
            ),
        ),

        // u2_l10 — Предлоги места
        "u2_l10" to LessonContent(
            intro = "Где находится: en (в/на), sobre (на), debajo de (под), al lado de (рядом с), entre (между).",
            sections = listOf(
                LessonSection("Предлоги места", listOf(
                    LessonItem("en", "в / на", "en la mesa"),
                    LessonItem("sobre", "на (поверх)", "sobre la mesa"),
                    LessonItem("debajo de", "под", "debajo de la mesa"),
                    LessonItem("al lado de", "рядом с", "al lado de la casa"),
                    LessonItem("entre", "между", "entre tú y yo"),
                    LessonItem("delante de", "перед", ""),
                    LessonItem("detrás de", "за / позади", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.TAP_MISSING_WORD, "Где кот?", question = "El gato está ___ la mesa.",
                    options = listOf("sobre", "en", "entre"), correctAnswer = "sobre",
                    explanation = "sobre = на поверхности."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Книга рядом с лампой»",
                    words = listOf("El", "libro", "está", "al lado de", "la lámpara"),
                    correctAnswer = "El libro está al lado de la lámpara",
                    explanation = "al lado de = рядом с."),
                Exercise(ExerciseType.MULTIPLE_CHOICE, "Что значит «debajo de»?", question = "",
                    options = listOf("под", "над", "перед", "рядом"), correctAnswer = "под",
                    explanation = "debajo de = под."),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf("sobre la mesa", "en casa", "debajo de la silla", "al lado mesa"),
                    correctAnswer = "al lado mesa",
                    explanation = "Нужно «al lado DE la mesa» — предлог de обязателен."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «Я в офисе»",
                    correctAnswer = "Estoy en la oficina", explanation = "en + место."),
                Exercise(ExerciseType.LISTEN_TYPE, "Послушай", audioText = "entre",
                    correctAnswer = "entre", explanation = "entre = между."),
            ),
        ),

        // u2_l11 — Дом: комнаты
        "u2_l11" to LessonContent(
            intro = "Главные комнаты: sala (гостиная), cocina (кухня), dormitorio (спальня), baño (ванная).",
            sections = listOf(
                LessonSection("Комнаты дома", listOf(
                    LessonItem("la sala", "гостиная", ""),
                    LessonItem("la cocina", "кухня", ""),
                    LessonItem("el dormitorio", "спальня", ""),
                    LessonItem("el baño", "ванная / туалет", ""),
                    LessonItem("el comedor", "столовая", ""),
                    LessonItem("el balcón", "балкон", ""),
                    LessonItem("el pasillo", "коридор", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "Комната ↔ перевод", correctAnswer = "ok",
                    pairs = listOf("sala" to "гостиная", "cocina" to "кухня", "dormitorio" to "спальня", "baño" to "ванная", "comedor" to "столовая")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай", audioText = "cocina",
                    options = listOf("cocina", "comedor", "balcón", "baño"), correctAnswer = "cocina",
                    explanation = "cocina = кухня."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Я в кухне»",
                    words = listOf("Estoy", "en", "la", "cocina"), correctAnswer = "Estoy en la cocina",
                    explanation = "estar для местоположения."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "Voy al ___ (ванная).",
                    options = listOf("baño", "balcón", "dormitorio"), correctAnswer = "baño",
                    explanation = "baño = ванная."),
                Exercise(ExerciseType.DIALOGUE_FILL, "Дома",
                    dialogueLines = listOf("👩 María" to "¿Dónde estás?", "👨 Tú" to "Estoy en la ___."),
                    options = listOf("sala", "balcón", "pasillo"), correctAnswer = "sala",
                    explanation = "sala = гостиная — основное место."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «спальня»",
                    correctAnswer = "el dormitorio", explanation = "dormitorio — м."),
            ),
        ),

        // u2_l12 — Мебель
        "u2_l12" to LessonContent(
            intro = "Мебель: sofá, mesa, silla, cama, armario, lámpara.",
            sections = listOf(
                LessonSection("Мебель", listOf(
                    LessonItem("el sofá", "диван", ""),
                    LessonItem("la mesa", "стол", ""),
                    LessonItem("la silla", "стул", ""),
                    LessonItem("la cama", "кровать", ""),
                    LessonItem("el armario", "шкаф", ""),
                    LessonItem("la lámpara", "лампа", ""),
                    LessonItem("la nevera", "холодильник", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "Мебель ↔ перевод", correctAnswer = "ok",
                    pairs = listOf("sofá" to "диван", "mesa" to "стол", "silla" to "стул", "cama" to "кровать", "armario" to "шкаф")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай", audioText = "lámpara",
                    options = listOf("lámpara", "cama", "mesa", "silla"), correctAnswer = "lámpara",
                    explanation = "lámpara = лампа."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Книга на столе»",
                    words = listOf("El", "libro", "está", "sobre", "la", "mesa"),
                    correctAnswer = "El libro está sobre la mesa",
                    explanation = "sobre = на поверхности."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "Mi ___ es grande (кровать).",
                    options = listOf("cama", "silla", "mesa"), correctAnswer = "cama",
                    explanation = "cama = кровать (ж)."),
                Exercise(ExerciseType.DIALOGUE_FILL, "Описание квартиры",
                    dialogueLines = listOf("👩 María" to "¿Tienes sofá?", "👨 Tú" to "Sí, en la ___."),
                    options = listOf("sala", "cocina", "baño"), correctAnswer = "sala",
                    explanation = "Диван обычно в гостиной."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «стул»",
                    correctAnswer = "la silla", explanation = "silla — ж → la."),
            ),
        ),

        // u2_l13 — Множественное число
        "u2_l13" to LessonContent(
            intro = "Множественное: на гласную → +s (libro→libros), на согласную → +es (papel→papeles).",
            sections = listOf(
                LessonSection("Правила множественного", listOf(
                    LessonItem("Гласная в конце", "+s", "libro → libros"),
                    LessonItem("Согласная в конце", "+es", "papel → papeles"),
                    LessonItem("На -z", "z→c +es", "luz → luces"),
                    LessonItem("Тильда исчезает", "examen → exámenes", "(перенос ударения)"),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "Ед. ↔ Мн.", correctAnswer = "ok",
                    pairs = listOf("libro" to "libros", "casa" to "casas", "papel" to "papeles", "luz" to "luces", "café" to "cafés")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай", audioText = "papeles",
                    options = listOf("papeles", "papel", "padres", "padres"), correctAnswer = "papeles",
                    explanation = "papeles — мн от papel (бумаги)."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «У меня две сестры»",
                    words = listOf("Tengo", "dos", "hermanas"), correctAnswer = "Tengo dos hermanas",
                    explanation = "hermana → hermanas (на -a → +s)."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "Hay tres ___ (свет/огни).",
                    options = listOf("luces", "luzes", "luzs"), correctAnswer = "luces",
                    explanation = "luz → luces. Z меняется на C перед -es."),
                Exercise(ExerciseType.DIALOGUE_FILL, "В магазине",
                    dialogueLines = listOf("👩 Vendedora" to "¿Cuántos quiere?", "👨 Tú" to "Dos ___, por favor (книги)."),
                    options = listOf("libros", "libroes", "libres"), correctAnswer = "libros",
                    explanation = "libro → libros (гласная +s)."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «дома (мн.)»",
                    correctAnswer = "casas", explanation = "casa → casas."),
            ),
        ),

        // u2_l14 — CHECKPOINT «Аренда жилья»
        "u2_l14" to LessonContent(
            intro = "🏁 Чекпоинт блока 1.2: ситуация «Аренда квартиры» — диалог с владельцем, описание комнат, числа цен.",
            sections = listOf(
                LessonSection("Что повторяем", listOf(
                    LessonItem("Числа", "цены, размер, этаж", ""),
                    LessonItem("Комнаты + мебель", "что есть в квартире", ""),
                    LessonItem("Предлоги места", "где что находится", ""),
                    LessonItem("ESTAR", "местоположение комнат", ""),
                    LessonItem("Притяжательные", "mi piso, su precio", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.DIALOGUE_FILL, "Звонишь хозяину",
                    dialogueLines = listOf(
                        "👨 Hola, ¿es el piso del anuncio?" to "—",
                        "👩 Dueña" to "Sí, ___ trescientos euros al mes.",
                        "👨 Tú" to "Perfecto. ¿Cuántas habitaciones tiene?",
                    ),
                    options = listOf("son", "es", "está"), correctAnswer = "son",
                    explanation = "Цена во множ.: «300 евро» — son trescientos euros."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Квартира на третьем этаже»",
                    words = listOf("El", "piso", "está", "en", "el", "tercer", "piso"),
                    correctAnswer = "El piso está en el tercer piso",
                    explanation = "tercer (с потерей -o перед мужским)."),
                Exercise(ExerciseType.LISTEN_COMPREHEND, "Послушай и ответь",
                    audioText = "Mi piso tiene dos dormitorios, una cocina y un baño. Está en el quinto piso.",
                    comprehensionContext = "Mi piso tiene dos dormitorios, una cocina y un baño. Está en el quinto piso.",
                    question = "Сколько спален?",
                    options = listOf("две", "одна", "три", "четыре"), correctAnswer = "две",
                    explanation = "dos dormitorios = две спальни."),
                Exercise(ExerciseType.MATCH_PAIRS, "Соедини всё блока", correctAnswer = "ok",
                    pairs = listOf("treinta" to "30", "sofá" to "диван", "cocina" to "кухня", "rojo" to "красный", "tengo" to "у меня есть")),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку в объявлении", question = "",
                    errorVariants = listOf(
                        "Piso en tercer planta",
                        "Tres dormitorios",
                        "Cocina grande",
                        "Está en el centro",
                    ),
                    correctAnswer = "Piso en tercer planta",
                    explanation = "planta — ж → tercera planta. tercer только перед мужским."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «У меня две спальни»",
                    correctAnswer = "Tengo dos dormitorios",
                    explanation = "dormitorio → dormitorios."),
            ),
        ),

    )

    // ═══════════════════════════════════════════════════════════════
    //  БЛОК 1.3 «ДЕЙСТВИЕ» — глаголы AR/ER/IR + еда + время + вопросы
    //  17 уроков: u3_l0..u3_l14 + u3_l5_5 + u3_l7_5 (2 новых)
    // ═══════════════════════════════════════════════════════════════

    private fun blockA1_3(): Map<String, LessonContent> = mapOf(

        // u3_l0 — Глаголы -AR ед.ч.
        "u3_l0" to LessonContent(
            intro = "Глаголы на -AR (hablar, trabajar) ед.ч.: -o, -as, -a. Это самая большая группа — 80% глаголов.",
            sections = listOf(
                LessonSection("Окончания -AR ед.ч.", listOf(
                    LessonItem("yo hablo", "я говорю", "yo трудно опускать в начале урока"),
                    LessonItem("tú hablas", "ты говоришь", ""),
                    LessonItem("él/ella habla", "он/она говорит", ""),
                    LessonItem("trabajar → trabajo/trabajas/trabaja", "работать", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.TAP_MISSING_WORD, "Вставь форму hablar", question = "Yo ___ español.",
                    options = listOf("hablo", "hablas", "habla"), correctAnswer = "hablo",
                    explanation = "yo → -o: hablo."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Ты работаешь в Мадриде»",
                    words = listOf("Tú", "trabajas", "en", "Madrid"), correctAnswer = "Tú trabajas en Madrid",
                    explanation = "tú → -as: trabajas."),
                Exercise(ExerciseType.MULTIPLE_CHOICE, "Какая форма для él?", question = "Él ___ inglés.",
                    options = listOf("habla", "hablo", "hablas", "hablan"), correctAnswer = "habla",
                    explanation = "él → -a: habla."),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf("Yo hablo", "Tú hablas", "Él hablo", "Ella habla"),
                    correctAnswer = "Él hablo",
                    explanation = "Él требует habla, не hablo."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «Я говорю по-русски»",
                    correctAnswer = "Hablo ruso",
                    explanation = "yo можно опустить. ruso = по-русски (без артикля)."),
                Exercise(ExerciseType.LISTEN_TYPE, "Послушай", audioText = "trabajas",
                    correctAnswer = "trabajas", explanation = "tú trabajas."),
            ),
        ),

        // u3_l1 — Глаголы -AR полное спряжение
        "u3_l1" to LessonContent(
            intro = "Полная парадигма -AR: -o, -as, -a, -amos, -áis, -an. Учим за один присест — будет работать со всеми -AR глаголами.",
            sections = listOf(
                LessonSection("Все 6 форм hablar", listOf(
                    LessonItem("yo hablo", "я говорю", ""),
                    LessonItem("tú hablas", "ты говоришь", ""),
                    LessonItem("él/ella habla", "он/она говорит", ""),
                    LessonItem("nosotros hablamos", "мы говорим", ""),
                    LessonItem("vosotros habláis", "вы говорите (Исп.)", ""),
                    LessonItem("ellos hablan", "они говорят", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.TAP_MISSING_WORD, "Вставь", question = "Nosotros ___ español.",
                    options = listOf("hablamos", "habláis", "hablan"), correctAnswer = "hablamos",
                    explanation = "nosotros → -amos."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Они работают в кафе»",
                    words = listOf("Ellos", "trabajan", "en", "el", "café"),
                    correctAnswer = "Ellos trabajan en el café",
                    explanation = "ellos → -an."),
                Exercise(ExerciseType.MULTIPLE_CHOICE, "vosotros + hablar?", question = "Vosotros ___.",
                    options = listOf("habláis", "hablan", "hablamos", "hablas"), correctAnswer = "habláis",
                    explanation = "vosotros → -áis (с тильдой)."),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf("Hablamos español", "Trabajan en Madrid", "Habláis francés", "Vosotros hablan"),
                    correctAnswer = "Vosotros hablan",
                    explanation = "vosotros → habláis, не hablan."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «Мы работаем»",
                    correctAnswer = "Trabajamos", explanation = "nosotros trabajamos — местоимение опускаем."),
                Exercise(ExerciseType.LISTEN_TYPE, "Послушай", audioText = "hablamos",
                    correctAnswer = "hablamos", explanation = "nosotros hablamos."),
            ),
        ),

        // u3_l2 — Глаголы -ER
        "u3_l2" to LessonContent(
            intro = "Глаголы на -ER (comer, beber, leer): -o, -es, -e, -emos, -éis, -en.",
            sections = listOf(
                LessonSection("Спряжение comer", listOf(
                    LessonItem("yo como", "я ем", ""),
                    LessonItem("tú comes", "ты ешь", ""),
                    LessonItem("él/ella come", "он/она ест", ""),
                    LessonItem("nosotros comemos", "мы едим", ""),
                    LessonItem("vosotros coméis", "вы едите (Исп.)", ""),
                    LessonItem("ellos comen", "они едят", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.TAP_MISSING_WORD, "Вставь", question = "Yo ___ pan.",
                    options = listOf("como", "comes", "come"), correctAnswer = "como",
                    explanation = "yo → -o: como."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Мы пьём вино»",
                    words = listOf("Bebemos", "vino"), correctAnswer = "Bebemos vino",
                    explanation = "beber + nosotros → bebemos."),
                Exercise(ExerciseType.MULTIPLE_CHOICE, "Какая форма для tú?", question = "Tú ___ libros.",
                    options = listOf("lees", "lee", "leo", "leen"), correctAnswer = "lees",
                    explanation = "leer + tú → lees."),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf("Yo como", "Tú comes", "Él come", "Nosotros comamos"),
                    correctAnswer = "Nosotros comamos",
                    explanation = "nosotros → comemos. comamos — это subjuntivo (повелительное)."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «Я пью воду»",
                    correctAnswer = "Bebo agua", explanation = "yo bebo + agua."),
                Exercise(ExerciseType.LISTEN_TYPE, "Послушай", audioText = "comemos",
                    correctAnswer = "comemos", explanation = "nosotros comemos."),
            ),
        ),

        // u3_l3 — Глаголы -IR
        "u3_l3" to LessonContent(
            intro = "Глаголы на -IR (vivir, escribir): -o, -es, -e, -imos, -ís, -en. Только nosotros/vosotros отличаются от -ER.",
            sections = listOf(
                LessonSection("vivir", listOf(
                    LessonItem("yo vivo", "я живу", ""),
                    LessonItem("tú vives", "ты живёшь", ""),
                    LessonItem("él/ella vive", "он/она живёт", ""),
                    LessonItem("nosotros vivimos", "мы живём", ""),
                    LessonItem("vosotros vivís", "вы живёте", ""),
                    LessonItem("ellos viven", "они живут", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.TAP_MISSING_WORD, "Вставь", question = "Yo ___ en Madrid.",
                    options = listOf("vivo", "vives", "vive"), correctAnswer = "vivo",
                    explanation = "yo vivo."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Они пишут письмо»",
                    words = listOf("Ellos", "escriben", "una", "carta"),
                    correctAnswer = "Ellos escriben una carta",
                    explanation = "escribir + ellos → escriben."),
                Exercise(ExerciseType.MULTIPLE_CHOICE, "vivir + nosotros?", question = "Nosotros ___ aquí.",
                    options = listOf("vivimos", "vivemos", "viven", "vivís"), correctAnswer = "vivimos",
                    explanation = "-IR в nosotros → -imos (НЕ -emos)."),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf("Vivo aquí", "Vives en casa", "Vivemos juntos", "Viven en Madrid"),
                    correctAnswer = "Vivemos juntos",
                    explanation = "vivir → vivimos (с -i-), не vivemos."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «Я живу в Москве»",
                    correctAnswer = "Vivo en Moscú", explanation = "yo vivo + en + город."),
                Exercise(ExerciseType.LISTEN_TYPE, "Послушай", audioText = "vivimos",
                    correctAnswer = "vivimos", explanation = "nosotros vivimos."),
            ),
        ),

        // u3_l4 — Еда
        "u3_l4" to LessonContent(
            intro = "Базовая еда: pan, leche, agua, café, fruta, carne. agua — ж, но артикль EL (для звучности).",
            sections = listOf(
                LessonSection("Еда", listOf(
                    LessonItem("el pan", "хлеб", ""), LessonItem("la leche", "молоко", ""),
                    LessonItem("el agua", "вода", "ж, но el!"), LessonItem("el café", "кофе", ""),
                    LessonItem("la fruta", "фрукты", ""), LessonItem("la carne", "мясо", ""),
                    LessonItem("el queso", "сыр", ""), LessonItem("el pescado", "рыба", ""),
                    LessonItem("el huevo", "яйцо", ""), LessonItem("la sopa", "суп", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "Еда ↔ перевод", correctAnswer = "ok",
                    pairs = listOf("pan" to "хлеб", "leche" to "молоко", "agua" to "вода", "queso" to "сыр", "pescado" to "рыба")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай", audioText = "leche",
                    options = listOf("leche", "carne", "pan", "agua"), correctAnswer = "leche",
                    explanation = "leche = молоко."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Я пью кофе»",
                    words = listOf("Bebo", "café"), correctAnswer = "Bebo café",
                    explanation = "Без артикля если общее («какой-то кофе»)."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "Quiero ___ (вода), por favor.",
                    options = listOf("agua", "leche", "pan"), correctAnswer = "agua",
                    explanation = "agua = вода."),
                Exercise(ExerciseType.DIALOGUE_FILL, "В баре",
                    dialogueLines = listOf("👩 Camarera" to "¿Qué tomas?", "👨 Tú" to "Un ___, por favor."),
                    options = listOf("café", "carne", "fruta"), correctAnswer = "café",
                    explanation = "Стандартный заказ — café."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «хлеб»",
                    correctAnswer = "el pan", explanation = "pan — м → el pan."),
            ),
        ),

        // u3_l5 — В ресторане
        "u3_l5" to LessonContent(
            intro = "В ресторане: el menú, el plato, la cuenta, la propina.",
            sections = listOf(
                LessonSection("Ресторан", listOf(
                    LessonItem("el menú", "меню", ""), LessonItem("el plato", "блюдо/тарелка", ""),
                    LessonItem("la cuenta", "счёт", ""), LessonItem("la propina", "чаевые", ""),
                    LessonItem("el camarero", "официант", ""), LessonItem("la mesa", "стол", ""),
                    LessonItem("la bebida", "напиток", ""), LessonItem("el postre", "десерт", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "Соедини", correctAnswer = "ok",
                    pairs = listOf("menú" to "меню", "cuenta" to "счёт", "camarero" to "официант", "postre" to "десерт", "propina" to "чаевые")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай", audioText = "cuenta",
                    options = listOf("cuenta", "carta", "cinco", "carne"), correctAnswer = "cuenta",
                    explanation = "cuenta = счёт."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Счёт, пожалуйста»",
                    words = listOf("La", "cuenta,", "por", "favor"), correctAnswer = "La cuenta por favor",
                    explanation = "Стандартная фраза."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "El ___ trae el menú.",
                    options = listOf("camarero", "postre", "menú"), correctAnswer = "camarero",
                    explanation = "camarero = официант."),
                Exercise(ExerciseType.DIALOGUE_FILL, "В ресторане",
                    dialogueLines = listOf("👩 Camarera" to "¿Algo más?", "👨 Tú" to "La ___, por favor."),
                    options = listOf("cuenta", "menú", "mesa"), correctAnswer = "cuenta",
                    explanation = "Конец трапезы — просим счёт."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «десерт»",
                    correctAnswer = "el postre", explanation = "postre — м."),
            ),
        ),

        // u3_l5_5 — НОВЫЙ: hay
        "u3_l5_5" to LessonContent(
            intro = "hay = «есть/имеется». Безличная форма HABER. Не меняется по числу: hay un libro / hay libros.",
            sections = listOf(
                LessonSection("Глагол hay", listOf(
                    LessonItem("hay un libro", "есть книга", "ед."),
                    LessonItem("hay libros", "есть книги", "мн. — то же hay!"),
                    LessonItem("¿Hay agua?", "Есть вода?", "вопрос"),
                    LessonItem("No hay nada", "ничего нет", "отрицание"),
                )),
                LessonSection("hay vs estar", listOf(
                    LessonItem("hay", "СУЩЕСТВУЕТ ли", "Hay un café cerca"),
                    LessonItem("está", "находится конкретный", "El café está allí"),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.TAP_MISSING_WORD, "Вставь", question = "___ una mesa libre.",
                    options = listOf("Hay", "Está", "Es"), correctAnswer = "Hay",
                    explanation = "hay — есть/имеется (новая информация)."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Здесь нет воды»",
                    words = listOf("No", "hay", "agua", "aquí"), correctAnswer = "No hay agua aquí",
                    explanation = "no hay = «нет» (не существует)."),
                Exercise(ExerciseType.MULTIPLE_CHOICE, "В чём разница?", question = "«Hay un libro» vs «El libro está aquí»",
                    options = listOf("hay = существует, está = конкретный находится", "одно и то же", "hay для людей, está для вещей", "hay только для еды"),
                    correctAnswer = "hay = существует, está = конкретный находится",
                    explanation = "hay вводит новый объект. está говорит о конкретном."),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf("Hay un café", "Hay tres mesas", "Hay el libro", "Hay agua"),
                    correctAnswer = "Hay el libro",
                    explanation = "После hay — НЕопределённый артикль (un libro), не el."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «Здесь есть кафе»",
                    correctAnswer = "Hay un café aquí",
                    explanation = "hay + un café (новая информация)."),
                Exercise(ExerciseType.LISTEN_TYPE, "Послушай", audioText = "hay",
                    correctAnswer = "hay", explanation = "hay — короткое слово, читается «ай»."),
            ),
        ),

        // u3_l6 — QUERER
        "u3_l6" to LessonContent(
            intro = "QUERER = «хотеть» (e→ie меняется). quiero, quieres, quiere — ед.ч. Правильное nosotros: queremos.",
            sections = listOf(
                LessonSection("QUERER", listOf(
                    LessonItem("quiero", "хочу", "yo"),
                    LessonItem("quieres", "хочешь", "tú"),
                    LessonItem("quiere", "хочет", "él/ella"),
                    LessonItem("queremos", "хотим", "nosotros — БЕЗ ie!"),
                    LessonItem("quieren", "хотят", "ellos"),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.TAP_MISSING_WORD, "Вставь", question = "Yo ___ café.",
                    options = listOf("quiero", "quieres", "quiere"), correctAnswer = "quiero",
                    explanation = "yo quiero."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Я хочу есть»",
                    words = listOf("Quiero", "comer"), correctAnswer = "Quiero comer",
                    explanation = "querer + инфинитив — стандартная конструкция."),
                Exercise(ExerciseType.MULTIPLE_CHOICE, "nosotros + querer?", question = "Nosotros ___ vino.",
                    options = listOf("queremos", "quiermos", "quieren", "quiere"), correctAnswer = "queremos",
                    explanation = "В nosotros e→ie НЕ работает: queremos."),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf("Yo quiero", "Tú quieres", "Nosotros quieremos", "Ellos quieren"),
                    correctAnswer = "Nosotros quieremos",
                    explanation = "Правильно: queremos (без ie в nosotros/vosotros)."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «Хочу пить»",
                    correctAnswer = "Quiero beber",
                    explanation = "querer + beber. yo опускаем."),
                Exercise(ExerciseType.LISTEN_TYPE, "Послушай", audioText = "quiero",
                    correctAnswer = "quiero", explanation = "yo quiero."),
            ),
        ),

        // u3_l7 — PODER
        "u3_l7" to LessonContent(
            intro = "PODER = «мочь» (o→ue). puedo, puedes, puede. nosotros podemos (БЕЗ ue).",
            sections = listOf(
                LessonSection("PODER", listOf(
                    LessonItem("puedo", "могу", ""),
                    LessonItem("puedes", "можешь", ""),
                    LessonItem("puede", "может", ""),
                    LessonItem("podemos", "можем", "БЕЗ ue!"),
                    LessonItem("pueden", "могут", ""),
                    LessonItem("¿Puedo?", "Можно?", "вежливый вопрос"),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "Форма ↔ местоим.", correctAnswer = "ok",
                    pairs = listOf("puedo" to "yo", "puedes" to "tú", "puede" to "él/ella", "podemos" to "nosotros", "pueden" to "ellos")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай", audioText = "puedo",
                    options = listOf("puedo", "puede", "podemos", "pueden"), correctAnswer = "puedo",
                    explanation = "yo puedo = я могу."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Можешь помочь?»",
                    words = listOf("¿Puedes", "ayudar?"), correctAnswer = "¿Puedes ayudar?",
                    explanation = "tú puedes + инфинитив."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "Nosotros ___ trabajar mañana.",
                    options = listOf("podemos", "pueden", "puedo"), correctAnswer = "podemos",
                    explanation = "nosotros podemos (без ue)."),
                Exercise(ExerciseType.DIALOGUE_FILL, "В кафе",
                    dialogueLines = listOf("👨 Cliente" to "¿___ pedir la cuenta?", "👩 Camarera" to "Sí, claro."),
                    options = listOf("Puedo", "Quiero", "Hay"), correctAnswer = "Puedo",
                    explanation = "¿Puedo? — вежливое «можно?»."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «Я могу читать»",
                    correctAnswer = "Puedo leer", explanation = "yo puedo + leer."),
            ),
        ),

        // u3_l7_5 — НОВЫЙ: e→i (pedir/servir)
        "u3_l7_5" to LessonContent(
            intro = "Третий тип отклонения: e→i (только -IR). pedir → pido, pides, pide. servir, repetir.",
            sections = listOf(
                LessonSection("e→i (только -IR)", listOf(
                    LessonItem("pido", "прошу", "pedir"),
                    LessonItem("pides", "просишь", ""),
                    LessonItem("pide", "просит", ""),
                    LessonItem("pedimos", "просим", "БЕЗ изменения!"),
                    LessonItem("piden", "просят", ""),
                )),
                LessonSection("Другие e→i", listOf(
                    LessonItem("servir → sirvo", "обслуживать", ""),
                    LessonItem("repetir → repito", "повторять", ""),
                    LessonItem("decir → digo", "говорить", "+1ое лицо нерегулярное!"),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.TAP_MISSING_WORD, "Вставь", question = "Yo ___ un café.",
                    options = listOf("pido", "pedo", "pide"), correctAnswer = "pido",
                    explanation = "pedir → e→i: yo pido."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Они заказывают вино»",
                    words = listOf("Piden", "vino"), correctAnswer = "Piden vino",
                    explanation = "ellos piden (e→i)."),
                Exercise(ExerciseType.MULTIPLE_CHOICE, "Какой тип отклонения у pedir?", question = "",
                    options = listOf("e→i", "e→ie", "o→ue", "не отклоняется"),
                    correctAnswer = "e→i",
                    explanation = "pedir, servir, repetir — все e→i. Только -IR глаголы."),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf("Yo pido", "Tú pides", "Nosotros pidemos", "Ellos piden"),
                    correctAnswer = "Nosotros pidemos",
                    explanation = "В nosotros отклонение НЕ работает: pedimos."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «Я заказываю воду»",
                    correctAnswer = "Pido agua", explanation = "pedir = «просить/заказывать»."),
                Exercise(ExerciseType.LISTEN_TYPE, "Послушай", audioText = "pido",
                    correctAnswer = "pido", explanation = "yo pido."),
            ),
        ),

        // u3_l8 — Время
        "u3_l8" to LessonContent(
            intro = "¿Qué hora es? — Сколько времени? Es la una (1 час). Son las dos (2+).",
            sections = listOf(
                LessonSection("Время", listOf(
                    LessonItem("¿Qué hora es?", "Сколько времени?", ""),
                    LessonItem("Es la una", "1 час", "Es — для 1"),
                    LessonItem("Son las dos", "2 часа", "Son — для 2+"),
                    LessonItem("Son las tres y media", "3:30", "media = половина"),
                    LessonItem("Son las cinco menos cuarto", "4:45", "menos cuarto = «без четверти»"),
                    LessonItem("Son las seis y cuarto", "6:15", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "Время ↔ перевод", correctAnswer = "ok",
                    pairs = listOf("Es la una" to "1:00", "Son las dos" to "2:00", "Son las tres y media" to "3:30",
                        "Son las cinco" to "5:00", "Son las seis y cuarto" to "6:15")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай и выбери", audioText = "Son las cinco",
                    options = listOf("Son las cinco", "Es la cinco", "Son cinco", "Es las cinco"),
                    correctAnswer = "Son las cinco", explanation = "Для 2+ → «Son las»."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Сейчас 7 часов»",
                    words = listOf("Son", "las", "siete"), correctAnswer = "Son las siete",
                    explanation = "Стандартная форма."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "___ la una.",
                    options = listOf("Es", "Son", "Está"), correctAnswer = "Es",
                    explanation = "Только для 1 часа — Es."),
                Exercise(ExerciseType.DIALOGUE_FILL, "Назначаем встречу",
                    dialogueLines = listOf("👩 María" to "¿Qué hora es?", "👨 Tú" to "Son ___ tres."),
                    options = listOf("las", "los", "la"), correctAnswer = "las",
                    explanation = "Перед числом часов — артикль las (или la для 1)."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «Сейчас 4 часа»",
                    correctAnswer = "Son las cuatro", explanation = "Son las + cuatro."),
            ),
        ),

        // u3_l9 — Дни недели
        "u3_l9" to LessonContent(
            intro = "Дни недели: lunes, martes, miércoles, jueves, viernes, sábado, domingo. С маленькой буквы.",
            sections = listOf(
                LessonSection("Дни недели", listOf(
                    LessonItem("lunes", "понедельник", ""),
                    LessonItem("martes", "вторник", ""),
                    LessonItem("miércoles", "среда", ""),
                    LessonItem("jueves", "четверг", ""),
                    LessonItem("viernes", "пятница", ""),
                    LessonItem("sábado", "суббота", ""),
                    LessonItem("domingo", "воскресенье", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "День ↔ перевод", correctAnswer = "ok",
                    pairs = listOf("lunes" to "понедельник", "miércoles" to "среда", "viernes" to "пятница", "sábado" to "суббота", "domingo" to "воскресенье")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай", audioText = "viernes",
                    options = listOf("viernes", "jueves", "lunes", "martes"), correctAnswer = "viernes",
                    explanation = "viernes = пятница."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «В понедельник я работаю»",
                    words = listOf("El", "lunes", "trabajo"), correctAnswer = "El lunes trabajo",
                    explanation = "el lunes = в понедельник (с артиклем!)."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "Hoy es ___ (среда).",
                    options = listOf("miércoles", "martes", "jueves"), correctAnswer = "miércoles",
                    explanation = "miércoles — с тильдой, ударение на É."),
                Exercise(ExerciseType.DIALOGUE_FILL, "Договариваемся",
                    dialogueLines = listOf("👩 María" to "¿Cuándo nos vemos?", "👨 Tú" to "El ___ a las cinco."),
                    options = listOf("viernes", "viernos", "vier"), correctAnswer = "viernes",
                    explanation = "viernes = пятница."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «суббота»",
                    correctAnswer = "sábado", explanation = "С тильдой над А."),
            ),
        ),

        // u3_l10 — Месяцы
        "u3_l10" to LessonContent(
            intro = "Месяцы: enero, febrero, marzo, abril, mayo, junio, julio, agosto, septiembre, octubre, noviembre, diciembre. С маленькой!",
            sections = listOf(
                LessonSection("Месяцы 1-6", listOf(
                    LessonItem("enero", "январь", ""), LessonItem("febrero", "февраль", ""),
                    LessonItem("marzo", "март", ""), LessonItem("abril", "апрель", ""),
                    LessonItem("mayo", "май", ""), LessonItem("junio", "июнь", ""),
                )),
                LessonSection("Месяцы 7-12", listOf(
                    LessonItem("julio", "июль", ""), LessonItem("agosto", "август", ""),
                    LessonItem("septiembre", "сентябрь", ""), LessonItem("octubre", "октябрь", ""),
                    LessonItem("noviembre", "ноябрь", ""), LessonItem("diciembre", "декабрь", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "Месяц ↔ перевод", correctAnswer = "ok",
                    pairs = listOf("enero" to "январь", "abril" to "апрель", "julio" to "июль", "octubre" to "октябрь", "diciembre" to "декабрь")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай", audioText = "agosto",
                    options = listOf("agosto", "abril", "octubre", "enero"), correctAnswer = "agosto",
                    explanation = "agosto = август."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Я родился в мае»",
                    words = listOf("Nací", "en", "mayo"), correctAnswer = "Nací en mayo",
                    explanation = "en + месяц."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "Mi cumpleaños es en ___ (декабре).",
                    options = listOf("diciembre", "deciembre", "december"), correctAnswer = "diciembre",
                    explanation = "diciembre — с -ie- внутри."),
                Exercise(ExerciseType.DIALOGUE_FILL, "Планы",
                    dialogueLines = listOf("👩 María" to "¿Cuándo viajas?", "👨 Tú" to "En ___ (июле)."),
                    options = listOf("julio", "junio", "enero"), correctAnswer = "julio",
                    explanation = "julio — для июля."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «март»",
                    correctAnswer = "marzo", explanation = "marzo — с -z-."),
            ),
        ),

        // u3_l11 — Наречия времени
        "u3_l11" to LessonContent(
            intro = "Время: hoy (сегодня), mañana (завтра), ayer (вчера), ahora (сейчас), siempre (всегда).",
            sections = listOf(
                LessonSection("Когда?", listOf(
                    LessonItem("hoy", "сегодня", ""),
                    LessonItem("mañana", "завтра", "(тж. «утро»)"),
                    LessonItem("ayer", "вчера", ""),
                    LessonItem("ahora", "сейчас", ""),
                    LessonItem("siempre", "всегда", ""),
                    LessonItem("nunca", "никогда", ""),
                    LessonItem("a veces", "иногда", ""),
                    LessonItem("pronto", "скоро", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "Соедини", correctAnswer = "ok",
                    pairs = listOf("hoy" to "сегодня", "mañana" to "завтра", "ayer" to "вчера", "ahora" to "сейчас", "siempre" to "всегда")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай", audioText = "ahora",
                    options = listOf("ahora", "ayer", "hoy", "mañana"), correctAnswer = "ahora",
                    explanation = "ahora = сейчас."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Сегодня работаю»",
                    words = listOf("Hoy", "trabajo"), correctAnswer = "Hoy trabajo",
                    explanation = "Наречие времени → впереди или после глагола."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "___ no trabajo (никогда).",
                    options = listOf("Nunca", "Siempre", "Ahora"), correctAnswer = "Nunca",
                    explanation = "Nunca = никогда."),
                Exercise(ExerciseType.DIALOGUE_FILL, "План",
                    dialogueLines = listOf("👩 María" to "¿Cuándo vamos?", "👨 Tú" to "___ (завтра)."),
                    options = listOf("Mañana", "Ayer", "Ahora"), correctAnswer = "Mañana",
                    explanation = "mañana = завтра (или утро)."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «иногда»",
                    correctAnswer = "a veces", explanation = "a veces (буквально «по разам»)."),
            ),
        ),

        // u3_l12 — Вопросы
        "u3_l12" to LessonContent(
            intro = "Вопросительные слова: ¿Qué? (что), ¿Quién? (кто), ¿Dónde? (где), ¿Cuándo? (когда), ¿Cuánto? (сколько), ¿Por qué? (почему).",
            sections = listOf(
                LessonSection("Вопросительные слова", listOf(
                    LessonItem("¿Qué?", "Что?", ""),
                    LessonItem("¿Quién?", "Кто?", ""),
                    LessonItem("¿Dónde?", "Где?", ""),
                    LessonItem("¿Cuándo?", "Когда?", ""),
                    LessonItem("¿Cuánto?", "Сколько?", ""),
                    LessonItem("¿Cómo?", "Как?", ""),
                    LessonItem("¿Por qué?", "Почему?", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.TAP_MISSING_WORD, "Вставь вопросит.", question = "¿___ vives? (где)",
                    options = listOf("Dónde", "Cuándo", "Qué"), correctAnswer = "Dónde",
                    explanation = "¿Dónde vives? = Где живёшь?"),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Кто это?»",
                    words = listOf("¿Quién", "es?"), correctAnswer = "¿Quién es?",
                    explanation = "Quién — для людей."),
                Exercise(ExerciseType.MULTIPLE_CHOICE, "Какое слово для «когда»?", question = "",
                    options = listOf("Cuándo", "Cuánto", "Cómo", "Qué"), correctAnswer = "Cuándo",
                    explanation = "Cuándo — для времени. Cuánto — для количества."),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf("¿Qué es?", "¿Quién viene?", "¿Donde estás?", "¿Por qué no?"),
                    correctAnswer = "¿Donde estás?",
                    explanation = "Без тильды — «donde» это союз. С тильдой ¿Dónde? — вопрос."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «Сколько стоит?»",
                    correctAnswer = "¿Cuánto cuesta?", explanation = "Cuánto + cuesta (от costar)."),
                Exercise(ExerciseType.LISTEN_TYPE, "Послушай", audioText = "dónde",
                    correctAnswer = "dónde", explanation = "С тильдой над É — вопрос."),
            ),
        ),

        // u3_l13 — Отрицание
        "u3_l13" to LessonContent(
            intro = "Главный отрицатель: NO + глагол. Двойное отрицание ОБЯЗАТЕЛЬНО: No tengo nada (= ничего нет).",
            sections = listOf(
                LessonSection("Отрицание", listOf(
                    LessonItem("No hablo", "Не говорю", "no перед глаголом"),
                    LessonItem("No tengo nada", "У меня нет ничего", "двойное отриц.!"),
                    LessonItem("Nunca trabajo", "Никогда не работаю", "nunca уже отрицание — no не нужно"),
                    LessonItem("No trabajo nunca", "Тоже верно", "если nunca после — нужно no"),
                    LessonItem("nadie", "никто", ""),
                    LessonItem("nada", "ничто", ""),
                    LessonItem("jamás", "никогда (усил.)", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.TAP_MISSING_WORD, "Вставь", question = "___ tengo dinero. (нет)",
                    options = listOf("No", "Nada", "Nunca"), correctAnswer = "No",
                    explanation = "no + глагол = простое отрицание."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Не знаю ничего»",
                    words = listOf("No", "sé", "nada"), correctAnswer = "No sé nada",
                    explanation = "Двойное отрицание ОБЯЗАТЕЛЬНО (no...nada)."),
                Exercise(ExerciseType.MULTIPLE_CHOICE, "Как сказать «Никогда не работаю»?", question = "",
                    options = listOf("Nunca trabajo", "No trabajo", "Trabajo nunca", "Nunca no trabajo"),
                    correctAnswer = "Nunca trabajo",
                    explanation = "Nunca впереди → no не нужно. Можно «No trabajo nunca»."),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf("No tengo nada", "Nunca trabajo", "Nunca no como", "No viene nadie"),
                    correctAnswer = "Nunca no como",
                    explanation = "Если nunca впереди — no НЕ ставится. Правильно: Nunca como."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «Никто не пришёл»",
                    correctAnswer = "No vino nadie", explanation = "no...nadie — двойное отрицание."),
                Exercise(ExerciseType.LISTEN_TYPE, "Послушай", audioText = "nunca",
                    correctAnswer = "nunca", explanation = "nunca = никогда."),
            ),
        ),

        // u3_l14 — CHECKPOINT «Обед в ресторане»
        "u3_l14" to LessonContent(
            intro = "🏁 Чекпоинт блока 1.3: «Обед в ресторане» — заказ еды, время, дни, вопросы.",
            sections = listOf(
                LessonSection("Что повторяем", listOf(
                    LessonItem("Глаголы AR/ER/IR", "comer, beber, trabajar", ""),
                    LessonItem("querer / poder", "Quiero, ¿Puedes?", ""),
                    LessonItem("Еда + ресторан", "menú, cuenta, plato", ""),
                    LessonItem("Время и дни", "Son las 8, viernes", ""),
                    LessonItem("Вопросы и отрицание", "¿Qué? No tengo nada", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.DIALOGUE_FILL, "В ресторане",
                    dialogueLines = listOf(
                        "👩 Camarera" to "Buenas tardes, ¿qué desea?",
                        "👨 Tú" to "___ una sopa, por favor.",
                        "👩 Camarera" to "Muy bien.",
                    ),
                    options = listOf("Quiero", "Tengo", "Hay"), correctAnswer = "Quiero",
                    explanation = "Quiero — стандартный заказ."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Я хочу одну воду и хлеб»",
                    words = listOf("Quiero", "un", "agua", "y", "pan"),
                    correctAnswer = "Quiero un agua y pan",
                    explanation = "agua — ж, но «un» (звучность). Хлеб без артикля — общее количество."),
                Exercise(ExerciseType.LISTEN_COMPREHEND, "Послушай и ответь",
                    audioText = "El restaurante abre a las ocho, pero los lunes está cerrado.",
                    comprehensionContext = "El restaurante abre a las ocho, pero los lunes está cerrado.",
                    question = "В какой день закрыто?",
                    options = listOf("понедельник", "вторник", "пятница", "суббота"),
                    correctAnswer = "понедельник",
                    explanation = "los lunes = по понедельникам."),
                Exercise(ExerciseType.MATCH_PAIRS, "Соедини всё блока", correctAnswer = "ok",
                    pairs = listOf("comer" to "есть", "cuenta" to "счёт", "viernes" to "пятница", "siempre" to "всегда", "puedo" to "могу")),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf(
                        "Quiero un café",
                        "Hay una mesa libre",
                        "Nosotros quieremos pan",
                        "Pido la cuenta",
                    ),
                    correctAnswer = "Nosotros quieremos pan",
                    explanation = "querer в nosotros: queremos (без ie)."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «Счёт, пожалуйста»",
                    correctAnswer = "La cuenta por favor",
                    explanation = "Стандартная просьба в ресторане."),
            ),
        ),

    )

    // ═══════════════════════════════════════════════════════════════
    //  БЛОК 1.4 «ВЫЖИВАНИЕ» — IR/GUSTAR + транспорт + магазин + здоровье
    //  16 уроков: u4_l0..u4_l14 + u4_l13_5 (новый)
    //  u4_l14 = ФИНАЛЬНЫЙ ЧЕКПОИНТ A1 «Один день в Мадриде»
    // ═══════════════════════════════════════════════════════════════

    private fun blockA1_4(): Map<String, LessonContent> = mapOf(

        // u4_l0 — Транспорт
        "u4_l0" to LessonContent(
            intro = "Городской транспорт: metro, autobús, taxi, tren, coche, bici.",
            sections = listOf(
                LessonSection("Транспорт", listOf(
                    LessonItem("el metro", "метро", ""),
                    LessonItem("el autobús", "автобус", ""),
                    LessonItem("el taxi", "такси", ""),
                    LessonItem("el tren", "поезд", ""),
                    LessonItem("el coche", "машина", ""),
                    LessonItem("la bici", "велик", ""),
                    LessonItem("el avión", "самолёт", ""),
                    LessonItem("el barco", "корабль", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "Транспорт ↔ перевод", correctAnswer = "ok",
                    pairs = listOf("metro" to "метро", "autobús" to "автобус", "tren" to "поезд", "coche" to "машина", "avión" to "самолёт")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай", audioText = "autobús",
                    options = listOf("autobús", "metro", "taxi", "tren"), correctAnswer = "autobús",
                    explanation = "autobús — с тильдой над U."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Я еду на метро»",
                    words = listOf("Voy", "en", "metro"), correctAnswer = "Voy en metro",
                    explanation = "en + транспорт. ir = идти/ехать."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "Tomo el ___ al trabajo (автобус).",
                    options = listOf("autobús", "metro", "tren"), correctAnswer = "autobús",
                    explanation = "autobús = автобус."),
                Exercise(ExerciseType.DIALOGUE_FILL, "Маршрут",
                    dialogueLines = listOf("👩 María" to "¿Cómo vienes?", "👨 Tú" to "En ___ (поезде)."),
                    options = listOf("tren", "trenes", "metro"), correctAnswer = "tren",
                    explanation = "en tren = на поезде."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «такси»",
                    correctAnswer = "el taxi", explanation = "taxi — м → el taxi."),
            ),
        ),

        // u4_l1 — IR полное
        "u4_l1" to LessonContent(
            intro = "IR = «идти/ехать». voy / vas / va / vamos / vais / van. Полностью неправильный.",
            sections = listOf(
                LessonSection("IR — все формы", listOf(
                    LessonItem("yo voy", "я иду", ""),
                    LessonItem("tú vas", "ты идёшь", ""),
                    LessonItem("él/ella va", "он/она идёт", ""),
                    LessonItem("nosotros vamos", "мы идём", ""),
                    LessonItem("vosotros vais", "вы идёте (Исп)", ""),
                    LessonItem("ellos van", "они идут", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.TAP_MISSING_WORD, "Вставь IR", question = "Yo ___ a casa.",
                    options = listOf("voy", "vas", "va"), correctAnswer = "voy",
                    explanation = "yo voy."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Мы идём в кафе»",
                    words = listOf("Vamos", "al", "café"), correctAnswer = "Vamos al café",
                    explanation = "ir + a + место. a + el → al."),
                Exercise(ExerciseType.MULTIPLE_CHOICE, "Какая форма для él?", question = "Él ___ al trabajo.",
                    options = listOf("va", "vas", "voy", "vamos"), correctAnswer = "va",
                    explanation = "él va."),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf("Yo voy", "Tú vas", "Nosotros vamos", "Ellos vamos"),
                    correctAnswer = "Ellos vamos",
                    explanation = "ellos требует van. Правильно: Ellos van."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «Я иду в школу»",
                    correctAnswer = "Voy al colegio", explanation = "ir + a + el colegio = al colegio."),
                Exercise(ExerciseType.LISTEN_TYPE, "Послушай", audioText = "vamos",
                    correctAnswer = "vamos", explanation = "nosotros vamos."),
            ),
        ),

        // u4_l2 — IR + a + lugar
        "u4_l2" to LessonContent(
            intro = "Конструкция «IR + A + место». a + el → AL (слитно). a + la = a la.",
            sections = listOf(
                LessonSection("IR + A + место", listOf(
                    LessonItem("Voy a Madrid", "Еду в Мадрид", "к городам — без артикля"),
                    LessonItem("Voy al cine", "Иду в кино", "a + el = al"),
                    LessonItem("Voy a la oficina", "Иду в офис", "a + la"),
                    LessonItem("Voy a casa", "Иду домой", "к casa без артикля!"),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.TAP_MISSING_WORD, "Вставь", question = "Voy ___ cine. (a + el)",
                    options = listOf("al", "a la", "a el"), correctAnswer = "al",
                    explanation = "a + el = al (слитно)."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Идём в офис»",
                    words = listOf("Vamos", "a", "la", "oficina"), correctAnswer = "Vamos a la oficina",
                    explanation = "oficina — ж → a la oficina."),
                Exercise(ExerciseType.MULTIPLE_CHOICE, "Как «иду домой»?", question = "",
                    options = listOf("Voy a casa", "Voy al casa", "Voy a la casa", "Voy en casa"),
                    correctAnswer = "Voy a casa",
                    explanation = "casa без артикля — устойчивая форма."),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf("Voy al cine", "Voy a la oficina", "Voy a el café", "Voy a casa"),
                    correctAnswer = "Voy a el café",
                    explanation = "a + el ОБЯЗАТЕЛЬНО склеивается в al. Правильно: al café."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «Иду в банк»",
                    correctAnswer = "Voy al banco", explanation = "banco — м → al banco."),
                Exercise(ExerciseType.LISTEN_TYPE, "Послушай", audioText = "al",
                    correctAnswer = "al", explanation = "Слитная форма a + el."),
            ),
        ),

        // u4_l3 — Дорога/направления
        "u4_l3" to LessonContent(
            intro = "Спросить дорогу: ¿Cómo llego? Указания: gira (поверни), sigue recto (иди прямо), a la derecha/izquierda.",
            sections = listOf(
                LessonSection("Указания", listOf(
                    LessonItem("¿Cómo llego?", "Как добраться?", ""),
                    LessonItem("a la derecha", "направо", ""),
                    LessonItem("a la izquierda", "налево", ""),
                    LessonItem("recto", "прямо", ""),
                    LessonItem("gira", "поверни", "повелит."),
                    LessonItem("sigue", "продолжай", ""),
                    LessonItem("cerca", "близко", ""),
                    LessonItem("lejos", "далеко", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "Соедини", correctAnswer = "ok",
                    pairs = listOf("recto" to "прямо", "derecha" to "направо", "izquierda" to "налево", "cerca" to "близко", "lejos" to "далеко")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай", audioText = "izquierda",
                    options = listOf("izquierda", "derecha", "recto", "cerca"), correctAnswer = "izquierda",
                    explanation = "izquierda = налево."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Поверни направо»",
                    words = listOf("Gira", "a", "la", "derecha"), correctAnswer = "Gira a la derecha",
                    explanation = "gira + a la + сторона."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "Sigue ___ (прямо).",
                    options = listOf("recto", "derecha", "lejos"), correctAnswer = "recto",
                    explanation = "recto = прямо."),
                Exercise(ExerciseType.DIALOGUE_FILL, "Спрашиваем дорогу",
                    dialogueLines = listOf("👨 Tú" to "Disculpe, ¿el metro?", "👩 Local" to "Sigue ___, está cerca."),
                    options = listOf("recto", "lejos", "izquierda"), correctAnswer = "recto",
                    explanation = "recto = прямо."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «налево»",
                    correctAnswer = "a la izquierda", explanation = "Конструкция «a la + сторона»."),
            ),
        ),

        // u4_l4 — Магазин
        "u4_l4" to LessonContent(
            intro = "Магазин: ¿Cuánto cuesta? — Сколько стоит? caro (дорого), barato (дёшево).",
            sections = listOf(
                LessonSection("В магазине", listOf(
                    LessonItem("¿Cuánto cuesta?", "Сколько стоит?", ""),
                    LessonItem("caro / cara", "дорогой/ая", ""),
                    LessonItem("barato / barata", "дешёвый/ая", ""),
                    LessonItem("comprar", "покупать", ""),
                    LessonItem("vender", "продавать", ""),
                    LessonItem("la tienda", "магазин", ""),
                    LessonItem("el supermercado", "супермаркет", ""),
                    LessonItem("la talla", "размер", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "Соедини", correctAnswer = "ok",
                    pairs = listOf("caro" to "дорого", "barato" to "дёшево", "comprar" to "покупать", "tienda" to "магазин", "talla" to "размер")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай", audioText = "barato",
                    options = listOf("barato", "caro", "comprar", "tienda"), correctAnswer = "barato",
                    explanation = "barato = дешёвый."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Сколько стоит это?»",
                    words = listOf("¿Cuánto", "cuesta", "esto?"), correctAnswer = "¿Cuánto cuesta esto?",
                    explanation = "esto = это (среднее)."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "Es muy ___ (дорого).",
                    options = listOf("caro", "barato", "lejos"), correctAnswer = "caro",
                    explanation = "caro = дорогой."),
                Exercise(ExerciseType.DIALOGUE_FILL, "В магазине",
                    dialogueLines = listOf("👨 Tú" to "¿___ cuesta?", "👩 Vendedora" to "Treinta euros."),
                    options = listOf("Cuánto", "Cuándo", "Cómo"), correctAnswer = "Cuánto",
                    explanation = "Cuánto = сколько (для цены)."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «дешёвый»",
                    correctAnswer = "barato", explanation = "barato — м.форма."),
            ),
        ),

        // u4_l5 — Деньги
        "u4_l5" to LessonContent(
            intro = "Деньги: el euro, el precio, en efectivo (наличными), con tarjeta (картой), ¿Tiene cambio?",
            sections = listOf(
                LessonSection("Оплата", listOf(
                    LessonItem("el euro / euros", "евро", ""),
                    LessonItem("el precio", "цена", ""),
                    LessonItem("en efectivo", "наличными", ""),
                    LessonItem("con tarjeta", "картой", ""),
                    LessonItem("¿Tiene cambio?", "Есть сдача?", ""),
                    LessonItem("la moneda", "монета/валюта", ""),
                    LessonItem("el billete", "купюра/билет", ""),
                    LessonItem("la factura", "квитанция/чек", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "Соедини", correctAnswer = "ok",
                    pairs = listOf("euro" to "евро", "precio" to "цена", "efectivo" to "наличные", "tarjeta" to "карта", "cambio" to "сдача")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай", audioText = "tarjeta",
                    options = listOf("tarjeta", "factura", "cambio", "billete"), correctAnswer = "tarjeta",
                    explanation = "tarjeta = карта (банковская)."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Плачу картой»",
                    words = listOf("Pago", "con", "tarjeta"), correctAnswer = "Pago con tarjeta",
                    explanation = "pagar + con + способ оплаты."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "¿Tiene ___? (сдача)",
                    options = listOf("cambio", "factura", "precio"), correctAnswer = "cambio",
                    explanation = "cambio = сдача (или мелочь)."),
                Exercise(ExerciseType.DIALOGUE_FILL, "На кассе",
                    dialogueLines = listOf("👩 Cajera" to "¿En efectivo o ___?", "👨 Tú" to "Con tarjeta, gracias."),
                    options = listOf("con tarjeta", "con cambio", "con precio"), correctAnswer = "con tarjeta",
                    explanation = "Стандартный вопрос на кассе."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «наличными»",
                    correctAnswer = "en efectivo", explanation = "Устойчивое словосочетание."),
            ),
        ),

        // u4_l6 — GUSTAR (1)
        "u4_l6" to LessonContent(
            intro = "GUSTAR — ОБРАТНАЯ конструкция: «мне нравится» = me gusta. Глагол согласуется с тем что нравится, не с подлежащим!",
            sections = listOf(
                LessonSection("Главное правило GUSTAR", listOf(
                    LessonItem("Me gusta + ед.ч.", "Me gusta el café", "мне нравится кофе"),
                    LessonItem("Me gustan + мн.ч.", "Me gustan los gatos", "мне нравятся коты"),
                    LessonItem("Me gusta + инфинитив", "Me gusta leer", "мне нравится читать"),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.TAP_MISSING_WORD, "Вставь", question = "Me ___ los libros.",
                    options = listOf("gustan", "gusta", "gusto"), correctAnswer = "gustan",
                    explanation = "Множественное (libros) → gustan."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Мне нравится музыка»",
                    words = listOf("Me", "gusta", "la", "música"), correctAnswer = "Me gusta la música",
                    explanation = "música — ед.ч. → gusta."),
                Exercise(ExerciseType.MULTIPLE_CHOICE, "«Мне нравятся коты» — gusta или gustan?", question = "",
                    options = listOf("gustan", "gusta", "gusto", "gustamos"), correctAnswer = "gustan",
                    explanation = "коты (мн.) → gustan."),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf("Me gusta el café", "Me gustan los libros", "Me gusto leer", "Me gusta leer"),
                    correctAnswer = "Me gusto leer",
                    explanation = "С инфинитивом — gusta (не gusto). Правильно: Me gusta leer."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «Мне нравится кофе»",
                    correctAnswer = "Me gusta el café", explanation = "Me gusta + el café."),
                Exercise(ExerciseType.LISTEN_TYPE, "Послушай", audioText = "gustan",
                    correctAnswer = "gustan", explanation = "Форма для мн.ч."),
            ),
        ),

        // u4_l7 — GUSTAR (2)
        "u4_l7" to LessonContent(
            intro = "Все местоимения с GUSTAR: me / te / le / nos / os / les. le gusta = ему/ей/Вам нравится.",
            sections = listOf(
                LessonSection("Все формы", listOf(
                    LessonItem("me gusta", "мне нравится", ""),
                    LessonItem("te gusta", "тебе нравится", ""),
                    LessonItem("le gusta", "ему/ей/Вам нравится", ""),
                    LessonItem("nos gusta", "нам нравится", ""),
                    LessonItem("os gusta", "вам (Исп) нравится", ""),
                    LessonItem("les gusta", "им/Вам (мн) нравится", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.TAP_MISSING_WORD, "Вставь", question = "___ gusta el cine. (нам)",
                    options = listOf("Nos", "Me", "Te"), correctAnswer = "Nos",
                    explanation = "nos = нам."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Им нравится футбол»",
                    words = listOf("Les", "gusta", "el", "fútbol"), correctAnswer = "Les gusta el fútbol",
                    explanation = "les = им. fútbol — ед. → gusta."),
                Exercise(ExerciseType.MULTIPLE_CHOICE, "Как «ему нравится»?", question = "",
                    options = listOf("le gusta", "lo gusta", "él gusta", "se gusta"), correctAnswer = "le gusta",
                    explanation = "le для él/ella/usted."),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf("Me gusta", "Te gustan", "Le gustamos", "Nos gusta"),
                    correctAnswer = "Le gustamos",
                    explanation = "Глагол gustar согласуется с тем ЧТО нравится, а не с местоимением. С инфинитивом / ед. — le gusta."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «Тебе нравится танцевать»",
                    correctAnswer = "Te gusta bailar",
                    explanation = "te + gusta + bailar (инфинитив)."),
                Exercise(ExerciseType.LISTEN_TYPE, "Послушай", audioText = "les",
                    correctAnswer = "les", explanation = "les = им (или Вам мн)."),
            ),
        ),

        // u4_l8 — Тело
        "u4_l8" to LessonContent(
            intro = "Тело: cabeza (голова), brazo (рука), pierna (нога), mano (кисть), ojo (глаз), boca (рот).",
            sections = listOf(
                LessonSection("Части тела", listOf(
                    LessonItem("la cabeza", "голова", ""), LessonItem("el brazo", "рука", ""),
                    LessonItem("la pierna", "нога", ""), LessonItem("la mano", "кисть", "ИСКЛ! ж"),
                    LessonItem("el ojo", "глаз", ""), LessonItem("la boca", "рот", ""),
                    LessonItem("la nariz", "нос", ""), LessonItem("el pelo", "волосы", ""),
                    LessonItem("el pie", "ступня", ""), LessonItem("la espalda", "спина", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "Часть тела ↔ перевод", correctAnswer = "ok",
                    pairs = listOf("cabeza" to "голова", "brazo" to "рука", "pierna" to "нога", "ojo" to "глаз", "boca" to "рот")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай", audioText = "pierna",
                    options = listOf("pierna", "brazo", "boca", "pelo"), correctAnswer = "pierna",
                    explanation = "pierna = нога."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «У меня болит голова»",
                    words = listOf("Me", "duele", "la", "cabeza"), correctAnswer = "Me duele la cabeza",
                    explanation = "doler работает как gustar: me duele + что болит."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "Tengo dos ___ (рук).",
                    options = listOf("brazos", "piernas", "manos"), correctAnswer = "brazos",
                    explanation = "brazo → brazos (мн.)."),
                Exercise(ExerciseType.DIALOGUE_FILL, "У врача",
                    dialogueLines = listOf("👨 Doctor" to "¿Qué le duele?", "👩 Tú" to "Me duele la ___ (голова)."),
                    options = listOf("cabeza", "boca", "pierna"), correctAnswer = "cabeza",
                    explanation = "cabeza = голова."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «глаз»",
                    correctAnswer = "el ojo", explanation = "ojo — м → el ojo."),
            ),
        ),

        // u4_l9 — Здоровье
        "u4_l9" to LessonContent(
            intro = "Me duele... — у меня болит... Tengo fiebre — у меня температура. Estoy enfermo — я болен.",
            sections = listOf(
                LessonSection("Здоровье", listOf(
                    LessonItem("Me duele", "у меня болит", "конструкция как gustar"),
                    LessonItem("Tengo fiebre", "у меня температура", ""),
                    LessonItem("Estoy enfermo/a", "я болен/больна", "estar (временно!)"),
                    LessonItem("la medicina", "лекарство", ""),
                    LessonItem("el médico / la médica", "врач", ""),
                    LessonItem("el hospital", "больница", ""),
                    LessonItem("la farmacia", "аптека", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "Соедини", correctAnswer = "ok",
                    pairs = listOf("fiebre" to "температура", "medicina" to "лекарство", "hospital" to "больница", "farmacia" to "аптека", "enfermo" to "больной")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай", audioText = "farmacia",
                    options = listOf("farmacia", "fiebre", "fiesta", "familia"), correctAnswer = "farmacia",
                    explanation = "farmacia = аптека."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «У меня температура»",
                    words = listOf("Tengo", "fiebre"), correctAnswer = "Tengo fiebre",
                    explanation = "tener + fiebre (без артикля)."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "Estoy ___ (болен).",
                    options = listOf("enfermo", "cansado", "contento"), correctAnswer = "enfermo",
                    explanation = "enfermo = больной."),
                Exercise(ExerciseType.DIALOGUE_FILL, "Звонишь врачу",
                    dialogueLines = listOf("👨 Doctor" to "¿Qué tiene?", "👩 Tú" to "Me ___ la cabeza."),
                    options = listOf("duele", "duelen", "dolor"), correctAnswer = "duele",
                    explanation = "cabeza — ед. → duele."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «У меня болят ноги»",
                    correctAnswer = "Me duelen las piernas",
                    explanation = "piernas (мн.) → duelen."),
            ),
        ),

        // u4_l10 — Одежда
        "u4_l10" to LessonContent(
            intro = "Одежда: camisa (рубашка), pantalón (штаны), vestido (платье), zapatos (обувь), chaqueta (куртка).",
            sections = listOf(
                LessonSection("Одежда", listOf(
                    LessonItem("la camisa", "рубашка", ""),
                    LessonItem("el pantalón", "штаны", ""),
                    LessonItem("el vestido", "платье", ""),
                    LessonItem("los zapatos", "обувь", "обычно мн."),
                    LessonItem("la chaqueta", "куртка", ""),
                    LessonItem("la camiseta", "футболка", ""),
                    LessonItem("la falda", "юбка", ""),
                    LessonItem("el sombrero", "шляпа", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "Одежда ↔ перевод", correctAnswer = "ok",
                    pairs = listOf("camisa" to "рубашка", "pantalón" to "штаны", "vestido" to "платье", "zapatos" to "обувь", "chaqueta" to "куртка")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай", audioText = "vestido",
                    options = listOf("vestido", "pantalón", "camiseta", "falda"), correctAnswer = "vestido",
                    explanation = "vestido = платье."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Хочу новые туфли»",
                    words = listOf("Quiero", "zapatos", "nuevos"), correctAnswer = "Quiero zapatos nuevos",
                    explanation = "Прилагательное после сущ.: zapatos nuevos."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "Llevo una ___ azul (рубашка).",
                    options = listOf("camisa", "vestido", "pantalón"), correctAnswer = "camisa",
                    explanation = "camisa — ж → una camisa."),
                Exercise(ExerciseType.DIALOGUE_FILL, "В магазине одежды",
                    dialogueLines = listOf("👩 Vendedora" to "¿Qué busca?", "👨 Tú" to "Una ___ azul (куртка)."),
                    options = listOf("chaqueta", "vestido", "sombrero"), correctAnswer = "chaqueta",
                    explanation = "chaqueta = куртка."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «обувь»",
                    correctAnswer = "los zapatos", explanation = "Обычно во мн.: los zapatos."),
            ),
        ),

        // u4_l11 — Погода
        "u4_l11" to LessonContent(
            intro = "Погода: hace + сущ. (calor, frío). Llueve (дождь), Nieva (снег) — безличные глаголы.",
            sections = listOf(
                LessonSection("Погода", listOf(
                    LessonItem("Hace calor", "жарко", "буквально «делает жар»"),
                    LessonItem("Hace frío", "холодно", ""),
                    LessonItem("Hace sol", "солнечно", ""),
                    LessonItem("Hace viento", "ветрено", ""),
                    LessonItem("Llueve", "идёт дождь", "от llover"),
                    LessonItem("Nieva", "идёт снег", "от nevar"),
                    LessonItem("Está nublado", "облачно", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "Погода ↔ перевод", correctAnswer = "ok",
                    pairs = listOf("Hace calor" to "жарко", "Hace frío" to "холодно", "Llueve" to "дождь", "Nieva" to "снег", "Hace sol" to "солнечно")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай", audioText = "Llueve",
                    options = listOf("Llueve", "Nieva", "Hace calor", "Hace frío"), correctAnswer = "Llueve",
                    explanation = "Llueve = идёт дождь."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Сегодня жарко»",
                    words = listOf("Hoy", "hace", "calor"), correctAnswer = "Hoy hace calor",
                    explanation = "hace + calor."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "En invierno ___ frío (делает).",
                    options = listOf("hace", "es", "está"), correctAnswer = "hace",
                    explanation = "Погода через hace + сущ."),
                Exercise(ExerciseType.DIALOGUE_FILL, "Перед прогулкой",
                    dialogueLines = listOf("👨 Tú" to "¿Qué tiempo hace?", "👩 María" to "___ sol."),
                    options = listOf("Hace", "Está", "Llueve"), correctAnswer = "Hace",
                    explanation = "hace sol = солнечно."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «идёт снег»",
                    correctAnswer = "Nieva", explanation = "Nieva — безличное."),
            ),
        ),

        // u4_l12 — Мой день
        "u4_l12" to LessonContent(
            intro = "Распорядок дня: me levanto (встаю), desayuno (завтракаю), trabajo, como, ceno (ужинаю), me acuesto.",
            sections = listOf(
                LessonSection("Мой день", listOf(
                    LessonItem("me levanto", "встаю", "от levantarse"),
                    LessonItem("desayuno", "завтракаю", ""),
                    LessonItem("trabajo", "работаю", ""),
                    LessonItem("como", "обедаю", "лит. ем"),
                    LessonItem("ceno", "ужинаю", ""),
                    LessonItem("me ducho", "принимаю душ", ""),
                    LessonItem("me acuesto", "ложусь спать", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.MATCH_PAIRS, "Действие ↔ перевод", correctAnswer = "ok",
                    pairs = listOf("desayuno" to "завтракаю", "trabajo" to "работаю", "como" to "обедаю", "ceno" to "ужинаю", "me acuesto" to "ложусь спать")),
                Exercise(ExerciseType.LISTEN_PICK, "Послушай", audioText = "desayuno",
                    options = listOf("desayuno", "ceno", "como", "ducho"), correctAnswer = "desayuno",
                    explanation = "desayuno = завтракаю."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Я завтракаю в восемь»",
                    words = listOf("Desayuno", "a", "las", "ocho"), correctAnswer = "Desayuno a las ocho",
                    explanation = "a las + час."),
                Exercise(ExerciseType.TAP_MISSING_WORD, "Заполни", question = "Por la noche ___ (ужинаю).",
                    options = listOf("ceno", "desayuno", "como"), correctAnswer = "ceno",
                    explanation = "ceno = ужинаю (от cenar)."),
                Exercise(ExerciseType.DIALOGUE_FILL, "О распорядке",
                    dialogueLines = listOf("👩 María" to "¿A qué hora te levantas?", "👨 Tú" to "Me ___ a las siete."),
                    options = listOf("levanto", "duermo", "como"), correctAnswer = "levanto",
                    explanation = "levantarse — возвратный: me levanto."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «обедаю»",
                    correctAnswer = "como", explanation = "Тоже значит «ем»."),
            ),
        ),

        // u4_l13 — Возвратные глаголы
        "u4_l13" to LessonContent(
            intro = "Возвратные глаголы: levantarse (вставать), ducharse, acostarse. Местоимение -se меняется: me/te/se/nos/os/se.",
            sections = listOf(
                LessonSection("Возвратные местоимения", listOf(
                    LessonItem("yo me levanto", "я встаю", ""),
                    LessonItem("tú te levantas", "ты встаёшь", ""),
                    LessonItem("él/ella se levanta", "он встаёт", ""),
                    LessonItem("nosotros nos levantamos", "мы встаём", ""),
                    LessonItem("vosotros os levantáis", "вы встаёте", ""),
                    LessonItem("ellos se levantan", "они встают", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.TAP_MISSING_WORD, "Вставь местоимение", question = "Yo ___ ducho.",
                    options = listOf("me", "te", "se"), correctAnswer = "me",
                    explanation = "yo me ducho."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Мы ложимся в 11»",
                    words = listOf("Nos", "acostamos", "a", "las", "once"),
                    correctAnswer = "Nos acostamos a las once",
                    explanation = "nos acostamos."),
                Exercise(ExerciseType.MULTIPLE_CHOICE, "ducharse + tú?", question = "Tú ___ por la mañana.",
                    options = listOf("te duchas", "se duchas", "me ducho", "duchas"), correctAnswer = "te duchas",
                    explanation = "tú te duchas."),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf("Me levanto", "Te duchas", "Se acuestan", "Yo te lavo"),
                    correctAnswer = "Yo te lavo",
                    explanation = "yo требует me, не te. Правильно: Yo me lavo."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «Я ложусь спать»",
                    correctAnswer = "Me acuesto",
                    explanation = "acostarse — возвратный, me acuesto."),
                Exercise(ExerciseType.LISTEN_TYPE, "Послушай", audioText = "se",
                    correctAnswer = "se", explanation = "se — для él/ella/ellos/ellas."),
            ),
        ),

        // u4_l13_5 — НОВЫЙ: 4 правила YO формы
        "u4_l13_5" to LessonContent(
            intro = "4 группы нерегулярных yo-форм: -go (tengo, salgo), -zco (conozco), -y (estoy, voy), полностью неправильные (sé, sé от saber).",
            sections = listOf(
                LessonSection("Группа 1: -go", listOf(
                    LessonItem("tener → tengo", "иметь → у меня есть", ""),
                    LessonItem("salir → salgo", "выходить → выхожу", ""),
                    LessonItem("hacer → hago", "делать → делаю", ""),
                    LessonItem("poner → pongo", "класть → кладу", ""),
                    LessonItem("decir → digo", "говорить → говорю", ""),
                )),
                LessonSection("Группа 2: -zco (на -CER/-CIR)", listOf(
                    LessonItem("conocer → conozco", "знать (быть знакомым)", ""),
                    LessonItem("conducir → conduzco", "водить", ""),
                )),
                LessonSection("Группа 3: -y", listOf(
                    LessonItem("estar → estoy", "находиться", ""),
                    LessonItem("ser → soy", "быть", ""),
                    LessonItem("ir → voy", "идти", ""),
                    LessonItem("dar → doy", "давать", ""),
                )),
                LessonSection("Группа 4: полностью неправильные", listOf(
                    LessonItem("saber → sé", "знать (факт)", ""),
                    LessonItem("ver → veo", "видеть", ""),
                    LessonItem("caber → quepo", "помещаться", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.TAP_MISSING_WORD, "Вставь yo-форму", question = "Yo ___ a las ocho. (выходить)",
                    options = listOf("salgo", "salo", "salio"), correctAnswer = "salgo",
                    explanation = "salir → salgo (-go группа)."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Я знаю Мадрид»",
                    words = listOf("Conozco", "Madrid"), correctAnswer = "Conozco Madrid",
                    explanation = "conocer → conozco (-zco группа)."),
                Exercise(ExerciseType.MULTIPLE_CHOICE, "yo + saber?", question = "Yo ___ la respuesta.",
                    options = listOf("sé", "sabo", "sabe", "sabes"), correctAnswer = "sé",
                    explanation = "saber → sé (полностью неправильная)."),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf("Yo tengo", "Yo conozco", "Yo sabo", "Yo veo"),
                    correctAnswer = "Yo sabo",
                    explanation = "saber → sé. Правильно: Yo sé."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «Я делаю кофе»",
                    correctAnswer = "Hago café", explanation = "hacer → hago."),
                Exercise(ExerciseType.LISTEN_TYPE, "Послушай", audioText = "tengo",
                    correctAnswer = "tengo", explanation = "tener → tengo."),
            ),
        ),

        // u4_l14 — ФИНАЛЬНЫЙ ЧЕКПОИНТ A1 «Один день в Мадриде»
        "u4_l14" to LessonContent(
            intro = "🏆 ФИНАЛЬНЫЙ БОСС A1: «Один день в Мадриде». Утро (метро+кафе), день (магазин+ресторан), вечер (отель+врач).",
            sections = listOf(
                LessonSection("Что повторяем — ВЕСЬ A1", listOf(
                    LessonItem("Приветствия + представление", "Hola, soy ruso", ""),
                    LessonItem("SER vs ESTAR vs HAY", "три способа «быть»", ""),
                    LessonItem("Глаголы AR/ER/IR + IR + GUSTAR", "", ""),
                    LessonItem("Числа, время, дни", "Son las 8, lunes", ""),
                    LessonItem("Магазин + ресторан + транспорт", "", ""),
                )),
            ),
            exercises = listOf(
                Exercise(ExerciseType.DIALOGUE_FILL, "Утро: в кафе",
                    dialogueLines = listOf(
                        "👩 Camarera" to "Buenos días, ¿qué desea?",
                        "👨 Tú" to "Un café y un croissant, por ___.",
                        "👩 Camarera" to "Son cinco euros.",
                    ),
                    options = listOf("favor", "gracias", "agua"), correctAnswer = "favor",
                    explanation = "por favor — стандартная вежливость."),
                Exercise(ExerciseType.BUILD_SENTENCE, "Собери: «Я хочу пойти в музей»",
                    words = listOf("Quiero", "ir", "al", "museo"),
                    correctAnswer = "Quiero ir al museo",
                    explanation = "querer + ir + a + el → al."),
                Exercise(ExerciseType.LISTEN_COMPREHEND, "Гид рассказывает",
                    audioText = "El museo del Prado abre a las diez y cierra a las ocho. Está cerrado los lunes.",
                    comprehensionContext = "El museo del Prado abre a las diez y cierra a las ocho. Está cerrado los lunes.",
                    question = "Когда закрывается музей?",
                    options = listOf("в 8 вечера", "в 10 утра", "в полдень", "никогда"),
                    correctAnswer = "в 8 вечера",
                    explanation = "cierra a las ocho — закрывается в восемь."),
                Exercise(ExerciseType.MATCH_PAIRS, "Соедини всё A1", correctAnswer = "ok",
                    pairs = listOf("voy" to "иду", "tengo" to "у меня есть", "me gusta" to "мне нравится",
                        "hace calor" to "жарко", "la cuenta" to "счёт")),
                Exercise(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", question = "",
                    errorVariants = listOf(
                        "Voy al museo",
                        "Me gusta el café",
                        "Tengo treinta años",
                        "Yo soy en Madrid",
                    ),
                    correctAnswer = "Yo soy en Madrid",
                    explanation = "Местоположение — ESTAR. Правильно: Estoy en Madrid."),
                Exercise(ExerciseType.TRANSLATE, "Переведи: «Сколько стоит билет?»",
                    correctAnswer = "¿Cuánto cuesta el billete?",
                    explanation = "¿Cuánto cuesta + el + что-то?"),
            ),
        ),

    )

    // ──────────────────────────────────────────────────────────────────
    //  Helpers для компактного авторинга A2/B1/B2 уроков.
    //  Сокращают boilerplate в LessonContent — каждое упражнение
    //  одной строкой.
    // ──────────────────────────────────────────────────────────────────

    private fun ex(t: ExerciseType, instr: String, correct: String,
                   q: String = "", opts: List<String> = emptyList(),
                   pairs: List<Pair<String, String>> = emptyList(),
                   words: List<String> = emptyList(), audio: String = "",
                   number: Int? = null, expl: String = "",
                   dialogue: List<Pair<String, String>> = emptyList(),
                   errVariants: List<String> = emptyList()) =
        Exercise(type = t, instruction = instr, question = q, correctAnswer = correct,
            options = opts, words = words, pairs = pairs, audioText = audio,
            number = number, explanation = expl, dialogueLines = dialogue,
            errorVariants = errVariants)

    private fun lc(intro: String, vararg sections: LessonSection,
                   exercises: List<Exercise>) =
        LessonContent(intro = intro, sections = sections.toList(), exercises = exercises)

    private fun sect(heading: String, vararg items: Pair<String, String>) =
        LessonSection(heading, items.map { LessonItem(it.first, it.second) })

    // ═══════════════════════════════════════════════════════════════
    //  A2 · БЛОК 2.1 «В ПРОШЛОМ» — Pretérito Indefinido
    //  16 уроков: u5_l0..u5_l14 + u5_l8_5 (новый Pluscuamperfecto)
    // ═══════════════════════════════════════════════════════════════

    private fun blockA2_1(): Map<String, LessonContent> = mapOf(

        "u5_l0" to lc(
            "Pretérito Indefinido — простое прошедшее. Для законченных действий с конкретным временем: ayer, el lunes, en 2020.",
            sect("Когда Indefinido",
                "Действие закончилось" to "Ayer comí pizza",
                "Конкретное время" to "En 2020 viajé",
                "Серия событий" to "Llegué, comí, salí",
            ),
            exercises = listOf(
                ex(ExerciseType.MULTIPLE_CHOICE, "Когда использовать Indefinido?", "Действие закончилось в прошлом",
                    opts = listOf("Действие закончилось в прошлом", "Регулярное действие", "Действие сейчас", "Только в будущем"),
                    expl = "Indefinido — для законченных действий."),
                ex(ExerciseType.TAP_MISSING_WORD, "Какой маркер времени для Indefinido?", "ayer",
                    q = "___ comí pizza.",
                    opts = listOf("ayer", "siempre", "ahora"),
                    expl = "ayer (вчера) — типичный маркер Indefinido."),
                ex(ExerciseType.MATCH_PAIRS, "Маркер ↔ перевод", "ok",
                    pairs = listOf("ayer" to "вчера", "anoche" to "прошлой ночью",
                        "el lunes" to "в понедельник", "en 2020" to "в 2020", "hace dos días" to "2 дня назад")),
                ex(ExerciseType.SPOT_THE_ERROR, "Какое предложение использует Indefinido правильно?", "Hablo español ahora",
                    errVariants = listOf("Ayer hablé con María", "El lunes comí pizza", "Hablo español ahora", "En 2020 viajé"),
                    expl = "«Hablo ahora» — настоящее, не Indefinido."),
                ex(ExerciseType.TRANSLATE, "Переведи: «Я говорил вчера»", "Hablé ayer",
                    expl = "hablé — yo Indefinido от hablar."),
                ex(ExerciseType.LISTEN_TYPE, "Послушай", "ayer", audio = "ayer",
                    expl = "ayer = вчера."),
            ),
        ),

        "u5_l1" to lc(
            "Indefinido для -AR глаголов: hablé, hablaste, habló, hablamos, hablasteis, hablaron.",
            sect("hablar в Indefinido",
                "yo hablé" to "я говорил",
                "tú hablaste" to "ты говорил",
                "él habló" to "он говорил",
                "nosotros hablamos" to "мы говорили",
                "vosotros hablasteis" to "вы говорили",
                "ellos hablaron" to "они говорили",
            ),
            exercises = listOf(
                ex(ExerciseType.TAP_MISSING_WORD, "Вставь форму hablar", "hablé",
                    q = "Yo ___ ayer.", opts = listOf("hablé", "hablaste", "habló"),
                    expl = "yo + hablar Indef = hablé."),
                ex(ExerciseType.BUILD_SENTENCE, "Собери: «Они работали»", "Trabajaron",
                    words = listOf("Trabajaron"), expl = "ellos + trabajar Indef = trabajaron."),
                ex(ExerciseType.MULTIPLE_CHOICE, "tú + cantar?", "cantaste",
                    q = "Tú ___ bien.", opts = listOf("cantaste", "canté", "cantó", "cantamos"),
                    expl = "tú + -AR Indef → -aste."),
                ex(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", "Yo hablaste",
                    errVariants = listOf("Yo hablé", "Tú hablaste", "Yo hablaste", "Ella habló"),
                    expl = "Yo требует hablé. tú hablaste."),
                ex(ExerciseType.TRANSLATE, "Переведи: «Я работал вчера»", "Trabajé ayer",
                    expl = "yo + trabajar Indef = trabajé."),
                ex(ExerciseType.LISTEN_TYPE, "Послушай", "hablé", audio = "hablé",
                    expl = "yo hablé."),
            ),
        ),

        "u5_l2" to lc(
            "Indefinido -ER/-IR (одинаковый!): comí, comiste, comió, comimos, comisteis, comieron.",
            sect("comer/vivir",
                "yo comí / viví" to "я ел / жил",
                "tú comiste / viviste" to "ты ел / жил",
                "él comió / vivió" to "он ел / жил",
                "nosotros comimos / vivimos" to "мы ели / жили",
                "ellos comieron / vivieron" to "они ели / жили",
            ),
            exercises = listOf(
                ex(ExerciseType.TAP_MISSING_WORD, "Вставь", "comí",
                    q = "Ayer ___ pizza.", opts = listOf("comí", "comiste", "comió"),
                    expl = "yo + comer Indef = comí."),
                ex(ExerciseType.BUILD_SENTENCE, "Собери: «Мы жили в Москве»", "Vivimos en Moscú",
                    words = listOf("Vivimos", "en", "Moscú"),
                    expl = "nosotros + vivir Indef = vivimos."),
                ex(ExerciseType.MULTIPLE_CHOICE, "él + escribir?", "escribió",
                    q = "Él ___ una carta.", opts = listOf("escribió", "escribí", "escribiste", "escribimos"),
                    expl = "él + -IR Indef → -ió."),
                ex(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", "Yo comieron",
                    errVariants = listOf("Yo comí", "Tú comiste", "Yo comieron", "Ellos comieron"),
                    expl = "Yo требует comí, не comieron."),
                ex(ExerciseType.TRANSLATE, "Переведи: «Я выпил воду»", "Bebí agua",
                    expl = "yo + beber Indef = bebí."),
                ex(ExerciseType.LISTEN_TYPE, "Послушай", "comí", audio = "comí", expl = "yo comí."),
            ),
        ),

        "u5_l3" to lc(
            "Ser vs Estar в прошлом: fui (был — постоянно) vs estuve (был — находился). Ключевое различие.",
            sect("Когда что",
                "fui = был (национальность, профессия)" to "Fui estudiante",
                "estuve = находился / чувствовал" to "Estuve en Madrid",
                "fui тж. = пошёл (от ir!)" to "Fui al cine",
            ),
            exercises = listOf(
                ex(ExerciseType.TAP_MISSING_WORD, "Вставь SER/ESTAR", "estuve",
                    q = "Ayer ___ en Madrid (находился).",
                    opts = listOf("estuve", "fui", "soy"),
                    expl = "Местоположение → estar → estuve."),
                ex(ExerciseType.BUILD_SENTENCE, "Собери: «Я был врачом»", "Fui médico",
                    words = listOf("Fui", "médico"), expl = "Профессия → ser → fui."),
                ex(ExerciseType.MULTIPLE_CHOICE, "fui — это от чего?", "от ser ИЛИ ir",
                    opts = listOf("от ser ИЛИ ir", "только от ser", "только от ir", "от estar"),
                    expl = "fui = «был» (ser) И «пошёл» (ir). Разбирается по контексту."),
                ex(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", "Yo fui en casa",
                    errVariants = listOf("Yo fui ruso", "Yo estuve en casa", "Yo fui en casa", "Yo fui al cine"),
                    expl = "Местоположение → ESTAR. Yo estuve en casa."),
                ex(ExerciseType.TRANSLATE, "Переведи: «Я был студентом»", "Fui estudiante",
                    expl = "Профессия → ser → fui."),
                ex(ExerciseType.LISTEN_TYPE, "Послушай", "estuve", audio = "estuve", expl = "yo estuve = находился."),
            ),
        ),

        "u5_l4" to lc(
            "Первые истории: ¿Qué hiciste ayer? — Что ты делал вчера? Связываем 3-4 предложения.",
            sect("Истории",
                "Primero" to "Сначала",
                "Después" to "Потом",
                "Luego" to "Затем",
                "Al final" to "В конце",
                "Por la mañana" to "Утром",
            ),
            exercises = listOf(
                ex(ExerciseType.TAP_MISSING_WORD, "Связка", "después",
                    q = "Comí, ___ trabajé.", opts = listOf("después", "siempre", "ahora"),
                    expl = "después = потом."),
                ex(ExerciseType.BUILD_SENTENCE, "Собери: «Сначала я завтракал»", "Primero desayuné",
                    words = listOf("Primero", "desayuné"),
                    expl = "Primero + Indefinido."),
                ex(ExerciseType.DIALOGUE_FILL, "Рассказ",
                    dialogue = listOf("👩 María" to "¿Qué hiciste ayer?", "👨 Tú" to "___ trabajé y luego cené."),
                    opts = listOf("Primero", "Después", "Mañana"), correct = "Primero",
                    expl = "Primero — начало рассказа."),
                ex(ExerciseType.MATCH_PAIRS, "Связки ↔ перевод", "ok",
                    pairs = listOf("primero" to "сначала", "después" to "потом", "luego" to "затем",
                        "al final" to "в конце", "por la mañana" to "утром")),
                ex(ExerciseType.TRANSLATE, "Переведи: «Потом я работал»", "Después trabajé",
                    expl = "Después + yo trabajé."),
                ex(ExerciseType.LISTEN_TYPE, "Послушай", "después", audio = "después", expl = "después = потом."),
            ),
        ),

        "u5_l5" to lc(
            "🎯 Мини-чекпоинт: Regulares Indefinido — все правила -AR/-ER/-IR за один тест.",
            sect("Что повторяем",
                "-AR: -é, -aste, -ó, -amos, -asteis, -aron" to "hablé, hablaste...",
                "-ER/-IR: -í, -iste, -ió, -imos, -isteis, -ieron" to "comí, comiste...",
            ),
            exercises = listOf(
                ex(ExerciseType.MULTIPLE_CHOICE, "yo + comprar?", "compré",
                    opts = listOf("compré", "compraste", "compró", "compramos"), expl = "yo -AR → -é."),
                ex(ExerciseType.MULTIPLE_CHOICE, "ellos + vivir?", "vivieron",
                    opts = listOf("vivieron", "vivimos", "viví", "vivió"), expl = "ellos -IR → -ieron."),
                ex(ExerciseType.TAP_MISSING_WORD, "tú + escribir?", "escribiste",
                    q = "Tú ___ una carta.", opts = listOf("escribiste", "escribí", "escribió"),
                    expl = "tú -IR → -iste."),
                ex(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", "yo trabajaron",
                    errVariants = listOf("yo trabajé", "tú trabajaste", "yo trabajaron", "ellos trabajaron"),
                    expl = "yo требует -é, не -aron."),
                ex(ExerciseType.TRANSLATE, "Переведи: «Они ели»", "Comieron",
                    expl = "ellos + comer Indef = comieron."),
                ex(ExerciseType.LISTEN_TYPE, "Послушай", "compré", audio = "compré", expl = "yo compré."),
            ),
        ),

        "u5_l6" to lc(
            "Нерегулярные Indefinido: ir и ser имеют ОДИНАКОВУЮ форму fui/fuiste/fue/fuimos/fueron. Контекст подскажет.",
            sect("ir/ser → fui",
                "yo fui" to "я был / я пошёл",
                "tú fuiste" to "ты был / ты пошёл",
                "él fue" to "он был / он пошёл",
                "nosotros fuimos" to "мы были / мы пошли",
                "ellos fueron" to "они были / они пошли",
            ),
            exercises = listOf(
                ex(ExerciseType.TAP_MISSING_WORD, "Вставь", "fui",
                    q = "Yo ___ al cine.", opts = listOf("fui", "fuiste", "fue"),
                    expl = "yo fui (от ir в этом контексте)."),
                ex(ExerciseType.BUILD_SENTENCE, "Собери: «Они были врачами»", "Fueron médicos",
                    words = listOf("Fueron", "médicos"), expl = "ellos + ser Indef = fueron."),
                ex(ExerciseType.MULTIPLE_CHOICE, "Что значит «fui al cine»?", "Я пошёл в кино",
                    opts = listOf("Я пошёл в кино", "Я был кино", "Я хочу в кино", "Кино было"),
                    expl = "fui + a + место = пошёл в..."),
                ex(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", "Yo fueron",
                    errVariants = listOf("Yo fui", "Tú fuiste", "Yo fueron", "Ellos fueron"),
                    expl = "Yo требует fui, не fueron."),
                ex(ExerciseType.TRANSLATE, "Переведи: «Мы пошли в магазин»", "Fuimos a la tienda",
                    expl = "ir + a + la tienda."),
                ex(ExerciseType.LISTEN_TYPE, "Послушай", "fui", audio = "fui", expl = "yo fui."),
            ),
        ),

        "u5_l7" to lc(
            "tener → tuve, estar → estuve. Нерегулярные основы tuv-/estuv- + одинаковые окончания: -e, -iste, -o, -imos, -ieron.",
            sect("tener / estar Indef",
                "yo tuve / estuve" to "у меня было / я был",
                "tú tuviste / estuviste" to "у тебя было / ты был",
                "él tuvo / estuvo" to "у него было / он был",
                "nosotros tuvimos / estuvimos" to "у нас было / мы были",
                "ellos tuvieron / estuvieron" to "у них было / они были",
            ),
            exercises = listOf(
                ex(ExerciseType.TAP_MISSING_WORD, "Вставь tener Indef", "tuve",
                    q = "Ayer ___ una idea.", opts = listOf("tuve", "tuviste", "tuvo"),
                    expl = "yo + tener Indef = tuve."),
                ex(ExerciseType.BUILD_SENTENCE, "Собери: «Они были в Мадриде»", "Estuvieron en Madrid",
                    words = listOf("Estuvieron", "en", "Madrid"),
                    expl = "ellos + estar Indef = estuvieron."),
                ex(ExerciseType.MULTIPLE_CHOICE, "tú + estar Indef?", "estuviste",
                    opts = listOf("estuviste", "estuve", "estuvo", "estuvimos"),
                    expl = "tú + estuv- + -iste."),
                ex(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", "Yo tení",
                    errVariants = listOf("Yo tuve", "Tú tuviste", "Yo tení", "Ella tuvo"),
                    expl = "tener Indef нерегулярный: tuve, не tení."),
                ex(ExerciseType.TRANSLATE, "Переведи: «У нас была встреча»", "Tuvimos una reunión",
                    expl = "tener Indef nosotros = tuvimos."),
                ex(ExerciseType.LISTEN_TYPE, "Послушай", "estuve", audio = "estuve", expl = "yo estuve."),
            ),
        ),

        "u5_l8" to lc(
            "hacer → hice, querer → quise. Основа меняется: hac→hic, quer→quis. Окончания те же -e, -iste, -o.",
            sect("hacer / querer Indef",
                "yo hice / quise" to "я сделал / захотел",
                "tú hiciste / quisiste" to "ты сделал / захотел",
                "él hizo / quiso" to "он сделал / захотел",
                "nosotros hicimos / quisimos" to "мы...",
                "ellos hicieron / quisieron" to "они...",
            ),
            exercises = listOf(
                ex(ExerciseType.TAP_MISSING_WORD, "Вставь hacer", "hice",
                    q = "Yo ___ la tarea.", opts = listOf("hice", "hiciste", "hizo"),
                    expl = "yo + hacer Indef = hice."),
                ex(ExerciseType.BUILD_SENTENCE, "Собери: «Он сделал ошибку»", "Hizo un error",
                    words = listOf("Hizo", "un", "error"), expl = "él + hacer Indef = hizo (с z, не c!)."),
                ex(ExerciseType.MULTIPLE_CHOICE, "él + hacer?", "hizo",
                    opts = listOf("hizo", "hice", "hiciste", "hicimos"),
                    expl = "Внимание: hizo с Z (звучание [исо]→ZO)."),
                ex(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", "Él hico",
                    errVariants = listOf("Yo hice", "Tú hiciste", "Él hico", "Ella hizo"),
                    expl = "él + hacer = hizo (с Z), не hico."),
                ex(ExerciseType.TRANSLATE, "Переведи: «Я хотел поехать»", "Quise ir",
                    expl = "yo + querer Indef = quise."),
                ex(ExerciseType.LISTEN_TYPE, "Послушай", "hice", audio = "hice", expl = "yo hice."),
            ),
        ),

        "u5_l8_5" to lc(
            "🆕 Pluscuamperfecto Indicativo: «уже было сделано до...». haber (Imperfect) + participio: había hablado.",
            sect("Pluscuamperfecto",
                "yo había hablado" to "я уже поговорил",
                "tú habías comido" to "ты уже поел",
                "él había vivido" to "он уже жил",
                "nosotros habíamos visto" to "мы уже видели",
                "ellos habían hecho" to "они уже сделали",
            ),
            sect("Когда использовать",
                "Действие ДО другого прошлого" to "Cuando llegué, ya había comido",
                "Прошедшее в прошедшем" to "ya = «уже»",
            ),
            exercises = listOf(
                ex(ExerciseType.TAP_MISSING_WORD, "Вставь había/habías", "había",
                    q = "Yo ya ___ comido.", opts = listOf("había", "habías", "habían"),
                    expl = "yo + Pluscuamperf = había."),
                ex(ExerciseType.BUILD_SENTENCE, "Собери: «Они уже ушли»", "Ya se habían ido",
                    words = listOf("Ya", "se", "habían", "ido"),
                    expl = "Pluscuamperf для прошедшего ДО другого прошедшего."),
                ex(ExerciseType.MULTIPLE_CHOICE, "Когда Pluscuamperfecto?", "Действие ДО другого прошлого",
                    opts = listOf("Действие ДО другого прошлого", "Действие сейчас", "Будущее", "Регулярное действие"),
                    expl = "«Уже было сделано до...»"),
                ex(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", "Yo habían comido",
                    errVariants = listOf("Yo había comido", "Tú habías visto", "Yo habían comido", "Ellos habían ido"),
                    expl = "yo требует había, не habían."),
                ex(ExerciseType.TRANSLATE, "Переведи: «Мы уже видели фильм»", "Ya habíamos visto la película",
                    expl = "ya + nosotros habíamos + visto."),
                ex(ExerciseType.LISTEN_TYPE, "Послушай", "había", audio = "había", expl = "yo había."),
            ),
        ),

        "u5_l9" to lc(
            "Por vs Para — два разных «для»: por (причина, через) vs para (цель, к).",
            sect("PARA — цель",
                "Estudio para aprender" to "Учусь для того чтобы учить",
                "Para ti" to "Для тебя",
                "Para mañana" to "К завтра (срок)",
            ),
            sect("POR — причина / способ",
                "Gracias por todo" to "Спасибо за всё",
                "Por la calle" to "По улице",
                "Por la mañana" to "Утром",
            ),
            exercises = listOf(
                ex(ExerciseType.TAP_MISSING_WORD, "Цель", "para",
                    q = "Estudio ___ aprobar.", opts = listOf("para", "por", "de"),
                    expl = "Цель → para."),
                ex(ExerciseType.BUILD_SENTENCE, "Собери: «Спасибо за подарок»", "Gracias por el regalo",
                    words = listOf("Gracias", "por", "el", "regalo"),
                    expl = "За что? → por."),
                ex(ExerciseType.MULTIPLE_CHOICE, "«для тебя» — por или para?", "para ti",
                    opts = listOf("para ti", "por ti", "de ti", "a ti"),
                    expl = "Получатель → para."),
                ex(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", "Trabajo por dinero",
                    errVariants = listOf("Trabajo para vivir", "Pago por el café", "Trabajo por dinero", "Voy por el parque"),
                    expl = "На самом деле «por dinero» VALID — за деньги. Все правильные!  ☑ trick: «trabajo por dinero» = «работаю за деньги» — корректно."),
                ex(ExerciseType.TRANSLATE, "Переведи: «к завтра»", "para mañana",
                    expl = "Срок → para."),
                ex(ExerciseType.LISTEN_TYPE, "Послушай", "para", audio = "para", expl = "para = для/к (цель)."),
            ),
        ),

        "u5_l10" to lc(
            "Связный диалог: «Расскажи о выходных». Истории с маркерами времени и связками.",
            sect("Полезное",
                "El fin de semana" to "В выходные",
                "Estuve con..." to "Я был с...",
                "Fuimos a..." to "Мы пошли в...",
                "Lo pasé bien/mal" to "Хорошо/плохо провёл",
            ),
            exercises = listOf(
                ex(ExerciseType.DIALOGUE_FILL, "О выходных",
                    dialogue = listOf("👩 María" to "¿Qué tal el fin de semana?", "👨 Tú" to "Lo ___ muy bien."),
                    opts = listOf("pasé", "paso", "pasaba"), correct = "pasé",
                    expl = "Indefinido yo от pasar = pasé."),
                ex(ExerciseType.BUILD_SENTENCE, "Собери: «Мы пошли в кино»", "Fuimos al cine",
                    words = listOf("Fuimos", "al", "cine"),
                    expl = "ir Indef nosotros = fuimos. a + el = al."),
                ex(ExerciseType.LISTEN_PICK, "Послушай", audio = "pasé",
                    opts = listOf("pasé", "paso", "pasaba", "pasaré"), correct = "pasé",
                    expl = "yo pasé."),
                ex(ExerciseType.TAP_MISSING_WORD, "Заполни", "fin de semana",
                    q = "El ___ trabajé.", opts = listOf("fin de semana", "lunes", "ahora"),
                    expl = "fin de semana = выходные."),
                ex(ExerciseType.TRANSLATE, "Переведи: «Я был с друзьями»", "Estuve con amigos",
                    expl = "estar Indef yo = estuve."),
                ex(ExerciseType.MATCH_PAIRS, "Соедини", "ok",
                    pairs = listOf("fui" to "пошёл", "estuve" to "был", "tuve" to "имел", "hice" to "сделал", "comí" to "ел")),
            ),
        ),

        "u5_l11" to lc(
            "poder → pude, saber → supe. Pretérito с особым смыслом: pude = «смог» (вышло), supe = «узнал» (момент).",
            sect("poder/saber Indef",
                "yo pude / supe" to "смог / узнал",
                "tú pudiste / supiste" to "ты смог / узнал",
                "él pudo / supo" to "он смог / узнал",
                "ellos pudieron / supieron" to "они смогли / узнали",
            ),
            exercises = listOf(
                ex(ExerciseType.TAP_MISSING_WORD, "Вставь poder", "pude",
                    q = "Ayer ___ ir.", opts = listOf("pude", "puede", "pudo"),
                    expl = "yo + poder Indef = pude."),
                ex(ExerciseType.BUILD_SENTENCE, "Собери: «Я узнал правду»", "Supe la verdad",
                    words = listOf("Supe", "la", "verdad"),
                    expl = "saber Indef yo = supe (момент узнавания)."),
                ex(ExerciseType.MULTIPLE_CHOICE, "él + poder?", "pudo",
                    opts = listOf("pudo", "pude", "pudiste", "pudieron"), expl = "él + pud- + -o = pudo."),
                ex(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", "Yo sabí",
                    errVariants = listOf("Yo supe", "Yo sabí", "Tú supiste", "Él supo"),
                    expl = "saber Indef нерегулярный: supe, не sabí."),
                ex(ExerciseType.TRANSLATE, "Переведи: «Они смогли»", "Pudieron",
                    expl = "ellos + poder Indef = pudieron."),
                ex(ExerciseType.LISTEN_TYPE, "Послушай", "supe", audio = "supe", expl = "yo supe."),
            ),
        ),

        "u5_l12" to lc(
            "dar → di, ver → vi, decir → dije. Очень короткие нерегулярные основы.",
            sect("dar/ver/decir Indef",
                "dar → di / diste / dio / dimos / dieron" to "я дал...",
                "ver → vi / viste / vio / vimos / vieron" to "я видел...",
                "decir → dije / dijiste / dijo / dijimos / dijeron" to "я сказал...",
            ),
            exercises = listOf(
                ex(ExerciseType.TAP_MISSING_WORD, "Вставь ver", "vi",
                    q = "Ayer ___ a María.", opts = listOf("vi", "viste", "vio"),
                    expl = "yo + ver Indef = vi."),
                ex(ExerciseType.BUILD_SENTENCE, "Собери: «Они сказали правду»", "Dijeron la verdad",
                    words = listOf("Dijeron", "la", "verdad"),
                    expl = "ellos + decir Indef = dijeron."),
                ex(ExerciseType.MULTIPLE_CHOICE, "yo + dar?", "di",
                    opts = listOf("di", "doy", "diste", "dio"), expl = "dar Indef yo = di (без окончания!)."),
                ex(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", "Yo decí",
                    errVariants = listOf("Yo dije", "Tú dijiste", "Yo decí", "Ella dijo"),
                    expl = "decir Indef нерегулярный: dije, не decí."),
                ex(ExerciseType.TRANSLATE, "Переведи: «Я видел фильм»", "Vi la película",
                    expl = "yo + ver Indef = vi."),
                ex(ExerciseType.LISTEN_TYPE, "Послушай", "dije", audio = "dije", expl = "yo dije."),
            ),
        ),

        "u5_l13" to lc(
            "Связный текст в прошлом: соединяем факты с маркерами времени и причинно-следственными связками.",
            sect("Связки рассказа",
                "porque" to "потому что",
                "pero" to "но",
                "entonces" to "тогда",
                "por eso" to "поэтому",
                "después de" to "после того как",
            ),
            exercises = listOf(
                ex(ExerciseType.TAP_MISSING_WORD, "Вставь связку", "porque",
                    q = "No fui ___ estuve enfermo.", opts = listOf("porque", "pero", "y"),
                    expl = "Причина → porque."),
                ex(ExerciseType.BUILD_SENTENCE, "Собери: «Хотел но не смог»", "Quise pero no pude",
                    words = listOf("Quise", "pero", "no", "pude"),
                    expl = "Противопоставление → pero."),
                ex(ExerciseType.DIALOGUE_FILL, "Объяснение",
                    dialogue = listOf("👩 María" to "¿Por qué no viniste?", "👨 Tú" to "___ tuve trabajo."),
                    opts = listOf("Porque", "Después", "Y"), correct = "Porque",
                    expl = "Причина → porque."),
                ex(ExerciseType.MATCH_PAIRS, "Связки", "ok",
                    pairs = listOf("porque" to "потому что", "pero" to "но", "entonces" to "тогда",
                        "por eso" to "поэтому", "y" to "и")),
                ex(ExerciseType.TRANSLATE, "Переведи: «Я был занят, поэтому не пришёл»", "Estuve ocupado, por eso no vine",
                    expl = "por eso = поэтому."),
                ex(ExerciseType.LISTEN_TYPE, "Послушай", "porque", audio = "porque", expl = "porque = потому что."),
            ),
        ),

        "u5_l14" to lc(
            "🎯 Большой тест: ВЕСЬ Pretérito Indefinido — регулярные + нерегулярные + связки.",
            sect("Что повторяем",
                "Regulares -AR/-ER/-IR" to "hablé, comí",
                "Irregulares" to "fui, tuve, hice, dije, vi",
                "Связки" to "porque, pero, después",
            ),
            exercises = listOf(
                ex(ExerciseType.MULTIPLE_CHOICE, "tú + tener Indef?", "tuviste",
                    opts = listOf("tuviste", "tenías", "tienes", "tuve"), expl = "tener Indef нерег: tuv-+iste."),
                ex(ExerciseType.TAP_MISSING_WORD, "Вставь", "fui",
                    q = "Ayer ___ al cine.", opts = listOf("fui", "voy", "iba"),
                    expl = "Indefinido от ir = fui."),
                ex(ExerciseType.BUILD_SENTENCE, "Собери: «Они сказали что устали»", "Dijeron que estaban cansados",
                    words = listOf("Dijeron", "que", "estaban", "cansados"),
                    expl = "decir Indef + estar Imperfect."),
                ex(ExerciseType.SPOT_THE_ERROR, "Найди ошибку", "Yo decí",
                    errVariants = listOf("Yo dije", "Tú dijiste", "Yo decí", "Ellos dijeron"),
                    expl = "decir Indef yo = dije."),
                ex(ExerciseType.LISTEN_COMPREHEND, "Понимание",
                    audio = "Ayer fui al cine con María. Vimos una película muy buena. Después comimos pizza.",
                    q = "Что они делали после кино?",
                    opts = listOf("ели пиццу", "пошли домой", "смотрели сериал", "спали"),
                    correct = "ели пиццу",
                    expl = "comimos pizza = ели пиццу."),
                ex(ExerciseType.TRANSLATE, "Переведи: «Я не пришёл потому что был занят»", "No vine porque estuve ocupado",
                    expl = "venir Indef yo = vine. estar Indef yo = estuve."),
            ),
        ),

    )
}
