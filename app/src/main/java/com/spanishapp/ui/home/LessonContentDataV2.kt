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

    fun allLessons(): Map<String, LessonContent> = blockA1_1() + blockA1_2()

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
}
