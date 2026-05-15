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

    fun allLessons(): Map<String, LessonContent> = blockA1_1()

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
}
