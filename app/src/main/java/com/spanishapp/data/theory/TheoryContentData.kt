package com.spanishapp.data.theory

/**
 * Реестр всех теорий-карточек. Связан 1-к-1 с LessonContentData по lessonId.
 *
 * Phase 1: 10 теорий для блока 1.1 (u1_l0..u1_l9).
 * Дальше будем добавлять блоками по 10-15 теорий за раз.
 *
 * Для урока без теории `byLessonId` вернёт null — UI покажет пустую заглушку
 * «Теория для этого урока скоро появится».
 */
object TheoryContentData {

    /** Получить теорию по lessonId. null если ещё не написана. */
    fun byLessonId(lessonId: String): TheoryContent? = ALL[lessonId]

    /** Все теории — для экрана-библиотеки в Profile. */
    fun all(): List<TheoryContent> = ALL.values.toList()

    /** Сколько теорий написано всего. */
    fun count(): Int = ALL.size

    private val ALL: Map<String, TheoryContent> = mapOf(

        // ─────────────────────────────────────────────────────────────────
        // u1_l0 — Алфавит 1/3: A B C D E F G H I
        // v1.3.5: переписано под алфавит. Каждая буква = TheoryExample
        //         с TTS-озвучкой слова-примера.
        // ─────────────────────────────────────────────────────────────────
        "u1_l0" to TheoryContent(
            lessonId = "u1_l0",
            title = "Алфавит 1/3: A B C D E F G H I",
            subtitle = "9 букв алфавита со звуком и словом-примером. Тапни ▶ — услышишь.",
            emoji = "🔤",
            cefr = "A1",
            readMinutes = 3,
            sections = listOf(
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "Главное про испанские буквы",
                    body = "Каждая буква = один звук, без исключений. Гласные A E I O U всегда чистые. Главный сюрприз — H никогда не читается.",
                ),
                TheorySection(
                    type = TheorySectionType.EXAMPLES,
                    heading = "Буквы A–I — звук и пример",
                    examples = listOf(
                        TheoryExample("agua", "вода (на A)", "А — «а»: всегда чистое"),
                        TheoryExample("bueno", "хороший (на B)", "B — «бэ»: [б] / [в] между гласных"),
                        TheoryExample("casa", "дом (на C)", "C — «сэ»: [к] или [с] перед e/i"),
                        TheoryExample("día", "день (на D)", "D — «дэ»: [д] / мягкое [ð] между гласных"),
                        TheoryExample("elefante", "слон (на E)", "E — «э»: всегда чистое"),
                        TheoryExample("foto", "фото (на F)", "F — «эфэ»: [ф]"),
                        TheoryExample("gato", "кот (на G)", "G — «хэ»: [г] или [х] перед e/i"),
                        TheoryExample("hola", "привет (на H)", "H — «аче»: ВСЕГДА молчит!"),
                        TheoryExample("isla", "остров (на I)", "I — «и»: всегда чистое"),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.WARNING,
                    heading = "Запомни про H",
                    body = "Буква H **никогда не читается**. hola звучит как «о-ла», hotel как «о-тэль», hospital как «оспиталь». Просто игнорируй её при произношении.",
                ),
                TheorySection(
                    type = TheorySectionType.TIP,
                    heading = "💡 Лайфхак про C и G",
                    body = "C и G — «хамелеоны»:\n• C + a/o/u = [к]: casa, cosa, cuna\n• C + e/i = [с]: cinco, cero\n• G + a/o/u = [г]: gato, gusto\n• G + e/i = [х]: gente, gigante",
                ),
            ),
            keyTakeaways = listOf(
                "9 букв: A B C D E F G H I",
                "H никогда не читается",
                "Гласные A E I — всегда чистые",
                "C и G меняются перед e/i",
            ),
            relatedTheory = listOf("u1_l1", "u1_l2"),
        ),

        // ─────────────────────────────────────────────────────────────────
        // u1_l1 — Алфавит 2/3: J K L M N Ñ O P Q
        // v1.3.5: переписано под алфавит. Главная героиня — Ñ.
        // ─────────────────────────────────────────────────────────────────
        "u1_l1" to TheoryContent(
            lessonId = "u1_l1",
            title = "Алфавит 2/3: J K L M N Ñ O P Q",
            subtitle = "9 букв средней трети. Главное — J=[х] и Ñ=[нь].",
            emoji = "🔤",
            cefr = "A1",
            readMinutes = 3,
            sections = listOf(
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "Главное про эту треть",
                    body = "Тут два больших сюрприза:\n• J — всегда читается как русское [х]\n• Ñ — отдельная буква со звуком [нь], НЕ просто «n с тильдой»",
                ),
                TheorySection(
                    type = TheorySectionType.EXAMPLES,
                    heading = "Буквы J–Q — звук и пример",
                    examples = listOf(
                        TheoryExample("Japón", "Япония (на J)", "J — «хота»: всегда [х]"),
                        TheoryExample("kilo", "кило (на K)", "K — «ка»: редкая, только в заимствованиях"),
                        TheoryExample("luna", "луна (на L)", "L — «эле»: [л]"),
                        TheoryExample("madre", "мать (на M)", "M — «эме»: [м]"),
                        TheoryExample("noche", "ночь (на N)", "N — «эне»: [н]"),
                        TheoryExample("año", "год (на Ñ)", "Ñ — «энье»: отдельная буква, [нь]"),
                        TheoryExample("oro", "золото (на O)", "O — «о»: всегда чистое"),
                        TheoryExample("padre", "отец (на P)", "P — «пэ»: [п]"),
                        TheoryExample("queso", "сыр (на Q)", "Q — «ку»: всегда QUE/QUI = [кэ/ки]"),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.COMPARISON,
                    heading = "ano vs año — пример важности Ñ",
                    body = "Маленькая тильда (~) над n меняет звук И смысл слова. Всегда пиши Ñ когда нужно.",
                    comparison = TheoryComparison(
                        leftHeader = "ano",
                        rightHeader = "año",
                        pairs = listOf(
                            "обычная n — [ано]" to "Ñ — [а-ньо]",
                            "значит «анус»" to "значит «год»",
                            "tengo 20 anos ❌" to "tengo 20 años ✓",
                        ),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.TIP,
                    heading = "💡 Про букву Q",
                    body = "Q всегда идёт в паре с U: **QUE/QUI**. И это всегда [кэ/ки], никогда «кве/кви»:\n• queso = «кэ-со» (сыр)\n• quien = «кьен» (кто)\n• química = «кими-ка» (химия)",
                ),
            ),
            keyTakeaways = listOf(
                "9 букв: J K L M N Ñ O P Q",
                "J = всегда [х]",
                "Ñ — отдельная буква [нь]",
                "Q всегда в QUE/QUI = [кэ/ки]",
            ),
            relatedTheory = listOf("u1_l0", "u1_l2"),
        ),

        // ─────────────────────────────────────────────────────────────────
        // u1_l2 — Алфавит 3/3: R S T U V W X Y Z
        // v1.3.5: переписано под алфавит. Главное — R/RR и V (=B).
        // ─────────────────────────────────────────────────────────────────
        "u1_l2" to TheoryContent(
            lessonId = "u1_l2",
            title = "Алфавит 3/3: R S T U V W X Y Z",
            subtitle = "Финальные 9 букв. Главное — V звучит как B, а R в начале = RR.",
            emoji = "🔤",
            cefr = "A1",
            readMinutes = 3,
            sections = listOf(
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "Два главных правила этой трети",
                    body = "• V читается точно так же как B — оба [б/в]\n• R в начале слова или после N/L/S → длинно [рр], как RR",
                ),
                TheorySection(
                    type = TheorySectionType.EXAMPLES,
                    heading = "Буквы R–Z — звук и пример",
                    examples = listOf(
                        TheoryExample("rojo", "красный (на R)", "R — «эре»: в начале = [рр]"),
                        TheoryExample("sol", "солнце (на S)", "S — «эсе»: [с]"),
                        TheoryExample("tomate", "помидор (на T)", "T — «тэ»: [т]"),
                        TheoryExample("uno", "один (на U)", "U — «у»: всегда чистое"),
                        TheoryExample("vino", "вино (на V)", "V — «увэ»: ЗВУЧИТ КАК B!"),
                        TheoryExample("wifi", "вай-фай (на W)", "W — «увэ добле»: редкая"),
                        TheoryExample("taxi", "такси (на X)", "X — «экис»: [кс]"),
                        TheoryExample("yo", "я (на Y)", "Y — «и гриега»: [й] или [и]"),
                        TheoryExample("zapato", "ботинок (на Z)", "Z — «сэта»: [с], НЕ [з]!"),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.COMPARISON,
                    heading = "pero vs perro — короткая vs длинная R",
                    body = "Одна R и две R меняют смысл слова. Путать нельзя — это разные слова.",
                    comparison = TheoryComparison(
                        leftHeader = "pero",
                        rightHeader = "perro",
                        pairs = listOf(
                            "одна R — короткое [р]" to "две R — длинное [рр]",
                            "значит «но»" to "значит «собака»",
                            "Quiero, pero no puedo" to "El perro ladra",
                        ),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.TIP,
                    heading = "💡 Тренируй RR",
                    body = "Прижми кончик языка к нёбу за зубами и выдохни — язык сам начнёт вибрировать. Не получается сразу? Начни с «трр-трр-трр» и постепенно убирай Т.",
                ),
                TheorySection(
                    type = TheorySectionType.WARNING,
                    heading = "Z — это НЕ [з]",
                    body = "Русские часто читают Z как [з] (как в «зебра»). По-испански Z = [с]: zapato звучит «са-па-то», zorro — «со-рро». Никаких [з]!",
                ),
            ),
            keyTakeaways = listOf(
                "9 букв: R S T U V W X Y Z",
                "V = B (один звук!)",
                "R в начале слова = длинное RR",
                "Z = [с], НЕ [з]",
                "pero (но) ≠ perro (собака)",
            ),
            relatedTheory = listOf("u1_l0", "u1_l1"),
        ),

        // ─────────────────────────────────────────────────────────────────
        // u1_l3 — Ударение и тильда
        // ─────────────────────────────────────────────────────────────────
        "u1_l3" to TheoryContent(
            lessonId = "u1_l3",
            title = "Ударение в испанском — три правила",
            subtitle = "Тильда (´) показывает ударение, если оно «не по правилу».",
            emoji = "🎯",
            cefr = "A1",
            readMinutes = 4,
            sections = listOf(
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "Правило 1: гласная или N/S в конце",
                    body = "Если слово оканчивается на **гласную, N или S** — ударение падает на **предпоследний слог**.",
                ),
                TheorySection(
                    type = TheorySectionType.EXAMPLES,
                    examples = listOf(
                        TheoryExample("casa", "дом", "КА-са (на A)"),
                        TheoryExample("hablan", "они говорят", "А-блан (на A)"),
                        TheoryExample("amigos", "друзья", "а-МИ-гос (на И)"),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "Правило 2: согласная (кроме N/S) в конце",
                    body = "Если слово оканчивается на любую другую согласную — ударение на **последний слог**.",
                ),
                TheorySection(
                    type = TheorySectionType.EXAMPLES,
                    examples = listOf(
                        TheoryExample("hablar", "говорить", "а-БЛЯР"),
                        TheoryExample("ciudad", "город", "си-у-ДАД"),
                        TheoryExample("feliz", "счастливый", "фэ-ЛИС"),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "Правило 3: тильда ломает правило",
                    body = "Если ударение падает **не туда, куда положено** по правилам 1-2 — ставится **тильда (´)** над ударной гласной.",
                ),
                TheorySection(
                    type = TheorySectionType.EXAMPLES,
                    examples = listOf(
                        TheoryExample("café", "кофе", "ка-ФЭ — было бы «КА-фе»"),
                        TheoryExample("música", "музыка", "МУ-си-ка — на 3-й от конца"),
                        TheoryExample("inglés", "английский", "ин-ГЛЭС"),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.TIP,
                    heading = "💡 Полезный трюк",
                    body = "Если в слове есть **тильда — читай по тильде** и забудь про правила. Тильда «выигрывает» всегда: música, fácil, examen.",
                ),
                TheorySection(
                    type = TheorySectionType.WARNING,
                    heading = "Тильда меняет смысл!",
                    body = "**sí** (да) ≠ **si** (если)\n**tú** (ты) ≠ **tu** (твой)\n**él** (он) ≠ **el** (артикль)\n**qué** (что?) ≠ **que** (что/который)",
                ),
            ),
            keyTakeaways = listOf(
                "Гласная/N/S в конце → ударение на предпоследний",
                "Любая другая согласная → ударение на последний",
                "Тильда ломает правило и побеждает всегда",
                "Тильда может менять смысл (sí ≠ si)",
            ),
            relatedTheory = listOf("u1_l0", "u1_l1", "u1_l2"),
        ),

        // ─────────────────────────────────────────────────────────────────
        // u1_l4 — Приветствия
        // ─────────────────────────────────────────────────────────────────
        "u1_l4" to TheoryContent(
            lessonId = "u1_l4",
            title = "Приветствия — твои первые фразы",
            subtitle = "Hola работает всегда. Buenos días — только до полудня.",
            emoji = "👋",
            cefr = "A1",
            readMinutes = 3,
            sections = listOf(
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "Универсальное приветствие",
                    body = "**¡Hola!** работает в любое время суток, с любым человеком — другом, незнакомцем, начальником. Это самое безопасное приветствие.",
                ),
                TheorySection(
                    type = TheorySectionType.TABLE,
                    heading = "Время дня",
                    table = TheoryTable(
                        headers = listOf("Время", "Приветствие", "Когда"),
                        rows = listOf(
                            listOf("Утро / день", "Buenos días", "до 13:00 (до обеда)"),
                            listOf("День / вечер", "Buenas tardes", "13:00 — закат"),
                            listOf("Вечер / ночь", "Buenas noches", "после заката"),
                        ),
                        highlightedColumns = listOf(1),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.TIP,
                    heading = "💡 Заметь",
                    body = "**días** мужского рода → **buenos**\n**tardes / noches** женского → **buenas**\nПоэтому окончание -os/-as меняется. Это первая встреча с **согласованием** прилагательных.",
                ),
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "Как дела?",
                    body = "После приветствия обычно спрашивают «как дела». Стандартный обмен:\n— ¿Cómo estás? — Как ты? (на «ты»)\n— ¿Cómo está? — Как Вы? (формально)\n— Bien, gracias. ¿Y tú? — Хорошо, спасибо. А ты?",
                ),
                TheorySection(
                    type = TheorySectionType.EXAMPLES,
                    heading = "Реальные диалоги",
                    examples = listOf(
                        TheoryExample("¡Hola! ¿Qué tal?", "Привет! Как ты?", "разговорно"),
                        TheoryExample("Buenos días, señora.", "Доброе утро, госпожа.", "вежливо"),
                        TheoryExample("¿Cómo estás? — Bien, ¿y tú?", "Как ты? — Хорошо, а ты?", ""),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.WARNING,
                    body = "**Buenas noches** = и «добрый вечер», и «спокойной ночи». В Испании говорят перед сном и при встрече вечером — контекст подскажет.",
                ),
            ),
            keyTakeaways = listOf(
                "¡Hola! — работает всегда",
                "Buenos días до 13:00, потом Buenas tardes",
                "После заката — Buenas noches",
                "¿Cómo estás? (ты) / ¿Cómo está? (Вы)",
            ),
            relatedTheory = listOf("u1_l5", "u1_l6"),
        ),

        // ─────────────────────────────────────────────────────────────────
        // u1_l5 — Прощания
        // ─────────────────────────────────────────────────────────────────
        "u1_l5" to TheoryContent(
            lessonId = "u1_l5",
            title = "Прощания — Adiós и его братья",
            subtitle = "Adiós — навсегда. Hasta luego — на сегодня.",
            emoji = "👋",
            cefr = "A1",
            readMinutes = 3,
            sections = listOf(
                TheorySection(
                    type = TheorySectionType.TABLE,
                    heading = "Шкала прощаний",
                    table = TheoryTable(
                        headers = listOf("Фраза", "Перевод", "Когда использовать"),
                        rows = listOf(
                            listOf("¡Adiós!", "До свидания", "формально или надолго"),
                            listOf("¡Hasta luego!", "До скорого", "увидимся ещё сегодня"),
                            listOf("¡Hasta pronto!", "До встречи", "скоро увидимся"),
                            listOf("¡Hasta mañana!", "До завтра", "увидимся завтра"),
                            listOf("¡Chao! / ¡Chau!", "Пока!", "разговорно, с друзьями"),
                            listOf("¡Nos vemos!", "Увидимся!", "нейтрально, дружески"),
                        ),
                        highlightedColumns = listOf(0),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "Hasta + время",
                    body = "Конструкция **Hasta + что угодно** = «до …». Можно подставлять любое время:\n• Hasta el lunes — до понедельника\n• Hasta las cinco — до пяти\n• Hasta la próxima — до следующего раза",
                ),
                TheorySection(
                    type = TheorySectionType.TIP,
                    heading = "💡 Лайфхак",
                    body = "В Испании в неформальной речи **«Adiós»** часто говорят и при встрече, и при прощании — как русское «Привет» и «Пока» вместе. Не пугайся, если услышишь «Adiós» от соседа в подъезде утром.",
                ),
                TheorySection(
                    type = TheorySectionType.EXAMPLES,
                    examples = listOf(
                        TheoryExample("¡Hasta luego!", "До скорого!", "по работе, нейтрально"),
                        TheoryExample("¡Que tengas un buen día!", "Хорошего дня!", "вежливое расставание"),
                        TheoryExample("¡Cuídate!", "Береги себя!", "тепло, между друзьями"),
                    ),
                ),
            ),
            keyTakeaways = listOf(
                "Adiós — формально или «надолго»",
                "Hasta luego — стандартное «до скорого»",
                "Hasta + время = «до X»",
                "Chao, Nos vemos — для друзей",
            ),
            relatedTheory = listOf("u1_l4", "u1_l6"),
        ),

        // ─────────────────────────────────────────────────────────────────
        // u1_l6 — Вежливые слова
        // ─────────────────────────────────────────────────────────────────
        "u1_l6" to TheoryContent(
            lessonId = "u1_l6",
            title = "Por favor, gracias, perdón — три волшебных слова",
            subtitle = "С ними тебя поймут везде, даже если других слов не знаешь.",
            emoji = "🙏",
            cefr = "A1",
            readMinutes = 3,
            sections = listOf(
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "Минимальный набор вежливости",
                    body = "В Испании вежливость = **por favor + gracias + perdón**. Если ты добавляешь por favor к просьбе и говоришь gracias после — ты уже воспринимаешься как «свой», даже с одним словом испанского.",
                ),
                TheorySection(
                    type = TheorySectionType.TABLE,
                    heading = "Когда что говорить",
                    table = TheoryTable(
                        headers = listOf("Фраза", "Перевод", "Ситуация"),
                        rows = listOf(
                            listOf("Por favor", "Пожалуйста", "просишь о чём-либо"),
                            listOf("Gracias", "Спасибо", "получил что угодно"),
                            listOf("Muchas gracias", "Большое спасибо", "усиление"),
                            listOf("De nada", "Не за что", "ответ на gracias"),
                            listOf("Perdón / Perdona", "Извини", "лёгкое извинение"),
                            listOf("Lo siento", "Сожалею", "серьёзное извинение"),
                            listOf("Disculpe", "Извините", "обращение к незнакомцу"),
                        ),
                        highlightedColumns = listOf(0),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.COMPARISON,
                    heading = "Perdón vs Lo siento",
                    body = "Оба переводятся как «извини», но используются по-разному.",
                    comparison = TheoryComparison(
                        leftHeader = "Perdón",
                        rightHeader = "Lo siento",
                        pairs = listOf(
                            "наступил на ногу" to "услышал плохую новость",
                            "не расслышал" to "за серьёзный проступок",
                            "хочешь пройти" to "соболезнование",
                            "Perdón, ¿qué dijiste?" to "Lo siento, no quería ofenderte",
                        ),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "Привлечь внимание",
                    body = "Чтобы остановить незнакомца на улице — **Perdone** (формально) или **Disculpe** (нейтрально). Никогда не «¡Hola!» — это слишком фамильярно.",
                ),
                TheorySection(
                    type = TheorySectionType.EXAMPLES,
                    examples = listOf(
                        TheoryExample("Un café, por favor.", "Один кофе, пожалуйста.", "в баре"),
                        TheoryExample("Muchas gracias, muy amable.", "Большое спасибо, очень любезно.", ""),
                        TheoryExample("Disculpe, ¿dónde está el metro?", "Извините, где метро?", "к незнакомцу"),
                    ),
                ),
            ),
            keyTakeaways = listOf(
                "por favor + gracias = универсальная вежливость",
                "Perdón за мелочь, Lo siento за серьёзное",
                "Disculpe / Perdone — обращение к незнакомцу",
                "De nada — стандартный ответ на «спасибо»",
            ),
            relatedTheory = listOf("u1_l4", "u1_l5"),
        ),

        // ─────────────────────────────────────────────────────────────────
        // u1_l7 — SER: soy, eres, es
        // ─────────────────────────────────────────────────────────────────
        "u1_l10" to TheoryContent(
            lessonId = "u1_l10",
            title = "Глагол SER — быть постоянно",
            subtitle = "soy / eres / es — кто ты, откуда, кем работаешь.",
            emoji = "🆔",
            cefr = "A1",
            readMinutes = 5,
            sections = listOf(
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "Что такое SER",
                    body = "**SER** = «быть» в смысле **постоянных** характеристик: национальность, профессия, имя, личные качества, родство. Это не про настроение или местоположение (для этого есть ESTAR — следующая большая тема).",
                ),
                TheorySection(
                    type = TheorySectionType.TABLE,
                    heading = "Спряжение SER (единственное число)",
                    table = TheoryTable(
                        headers = listOf("Лицо", "Форма", "Пример"),
                        rows = listOf(
                            listOf("yo", "soy", "Soy ruso. — Я русский."),
                            listOf("tú", "eres", "Eres mi amigo. — Ты мой друг."),
                            listOf("él / ella / usted", "es", "Es médico. — Он врач."),
                        ),
                        highlightedColumns = listOf(1),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.MNEMONIC,
                    heading = "Запомни ритм",
                    body = "**SOY — ERES — ES**. Ритмично, как удары: «сой-эрэс-эс». Повтори 5 раз вслух — закрепится навсегда.",
                ),
                TheorySection(
                    type = TheorySectionType.EXAMPLES,
                    heading = "Когда использовать SER",
                    examples = listOf(
                        TheoryExample("Soy Pablo.", "Я — Пабло.", "имя — навсегда"),
                        TheoryExample("Eres alto.", "Ты высокий.", "качество"),
                        TheoryExample("Es ingeniero.", "Он инженер.", "профессия"),
                        TheoryExample("Es de Madrid.", "Он из Мадрида.", "происхождение"),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.TIP,
                    heading = "💡 Местоимение можно опускать",
                    body = "В испанском глагольное окончание само показывает лицо. Поэтому **«Yo soy ruso»** и **«Soy ruso»** — обе верны. Чаще говорят без yo / tú — короче и естественнее.",
                ),
                TheorySection(
                    type = TheorySectionType.WARNING,
                    heading = "Не путай с ESTAR",
                    body = "**Soy feliz** — я счастливый человек (характер).\n**Estoy feliz** — я счастлив сейчас (состояние).\nЭто будет в u1_l14. Пока — только постоянное.",
                ),
            ),
            keyTakeaways = listOf(
                "SER — для постоянных характеристик",
                "soy / eres / es — единственное число",
                "Имя, национальность, профессия — всегда SER",
                "Местоимение yo/tú можно опускать",
            ),
            relatedTheory = listOf("u1_l11", "u1_l7"),
        ),

        // ─────────────────────────────────────────────────────────────────
        // u1_l8 — SER: somos, sois, son
        // ─────────────────────────────────────────────────────────────────
        "u1_l11" to TheoryContent(
            lessonId = "u1_l11",
            title = "SER во множественном числе",
            subtitle = "somos / sois / son — мы, вы, они.",
            emoji = "👥",
            cefr = "A1",
            readMinutes = 4,
            sections = listOf(
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "Полная парадигма SER",
                    body = "Теперь добавляем три формы множественного. Спряжение SER — одно из самых неправильных, надо просто **выучить наизусть**.",
                ),
                TheorySection(
                    type = TheorySectionType.TABLE,
                    heading = "Все 6 форм SER",
                    table = TheoryTable(
                        headers = listOf("Лицо", "Форма", "Перевод"),
                        rows = listOf(
                            listOf("yo", "soy", "я есть"),
                            listOf("tú", "eres", "ты есть"),
                            listOf("él/ella/usted", "es", "он/она/Вы есть"),
                            listOf("nosotros/as", "somos", "мы есть"),
                            listOf("vosotros/as", "sois", "вы есть (Испания)"),
                            listOf("ellos/ellas/ustedes", "son", "они/Вы есть"),
                        ),
                        highlightedColumns = listOf(1),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.MNEMONIC,
                    heading = "Магическая шестёрка",
                    body = "**SOY-ERES-ES-SOMOS-SOIS-SON**\nПовторяй цепочкой, как стих. Через неделю это будет автоматически.",
                ),
                TheorySection(
                    type = TheorySectionType.EXAMPLES,
                    heading = "Множественное в действии",
                    examples = listOf(
                        TheoryExample("Somos amigos.", "Мы друзья.", ""),
                        TheoryExample("¿Sois de aquí?", "Вы отсюда? (Испания)", "vosotros"),
                        TheoryExample("Son estudiantes.", "Они студенты.", ""),
                        TheoryExample("Ustedes son muy amables.", "Вы (Вы все) очень любезны.", "Латинская Америка"),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.COMPARISON,
                    heading = "Vosotros vs Ustedes",
                    body = "В Испании и Латинской Америке множественное «вы» работает по-разному.",
                    comparison = TheoryComparison(
                        leftHeader = "Vosotros (Испания)",
                        rightHeader = "Ustedes (Латам)",
                        pairs = listOf(
                            "неформальное «вы» (друзья)" to "и неформальное, и формальное",
                            "vosotros sois" to "ustedes son",
                            "только Испания" to "вся Лат. Америка + формальное в Испании",
                        ),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.TIP,
                    heading = "💡 Если уезжаешь не в Испанию",
                    body = "В Латинской Америке **vosotros не используют вообще**. Достаточно выучить **ustedes son**. Но для Испании vosotros sois обязательно.",
                ),
            ),
            keyTakeaways = listOf(
                "soy-eres-es-somos-sois-son — все 6 форм",
                "vosotros sois — только Испания",
                "ustedes son — везде, формально и в Латам неформально",
                "Спряжение SER нерегулярное — учим наизусть",
            ),
            relatedTheory = listOf("u1_l10", "u1_l7"),
        ),

        // ─────────────────────────────────────────────────────────────────
        // u1_l9 — Личные местоимения
        // ─────────────────────────────────────────────────────────────────
        "u1_l7" to TheoryContent(
            lessonId = "u1_l7",
            title = "Личные местоимения",
            subtitle = "yo, tú, él, nosotros — но чаще их опускают.",
            emoji = "🙋",
            cefr = "A1",
            readMinutes = 4,
            sections = listOf(
                TheorySection(
                    type = TheorySectionType.TABLE,
                    heading = "Полная таблица местоимений",
                    table = TheoryTable(
                        headers = listOf("Местоимение", "Перевод", "Когда"),
                        rows = listOf(
                            listOf("yo", "я", "о себе"),
                            listOf("tú", "ты", "к другу/ребёнку"),
                            listOf("usted", "Вы", "формально, к незнакомому"),
                            listOf("él / ella", "он / она", ""),
                            listOf("nosotros / nosotras", "мы (м/ж)", "если все женщины — nosotras"),
                            listOf("vosotros / vosotras", "вы (Испания)", "к группе друзей"),
                            listOf("ustedes", "вы (мн)", "везде формально, в Латам и неформально"),
                            listOf("ellos / ellas", "они (м/ж)", ""),
                        ),
                        highlightedColumns = listOf(0),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "Главная фишка: их можно опустить",
                    body = "Окончание глагола **уже содержит лицо**. Поэтому испанец чаще скажет **«Soy ruso»**, а не **«Yo soy ruso»**. Местоимение добавляют только для **подчёркивания** или **противопоставления**.",
                ),
                TheorySection(
                    type = TheorySectionType.EXAMPLES,
                    heading = "Когда местоимение нужно",
                    examples = listOf(
                        TheoryExample("Yo soy ruso, ella es española.", "Я русский, а она испанка.", "противопоставление"),
                        TheoryExample("¿Tú? ¿En serio?", "Ты? Серьёзно?", "подчёркивание"),
                        TheoryExample("Soy estudiante.", "Я студент.", "yo не нужно"),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.COMPARISON,
                    heading = "Tú vs Usted",
                    body = "Главное социальное решение в испанском: на «ты» или на «Вы».",
                    comparison = TheoryComparison(
                        leftHeader = "Tú (на «ты»)",
                        rightHeader = "Usted (на «Вы»)",
                        pairs = listOf(
                            "друзья, семья" to "незнакомцы, начальство",
                            "дети" to "пожилые люди",
                            "сверстники" to "официальные ситуации",
                            "формы как 2-е лицо: eres" to "формы как 3-е лицо: es",
                        ),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.TIP,
                    heading = "💡 Региональные особенности",
                    body = "В **Аргентине, Уругвае, части ЦА** вместо tú используют **vos** с особыми формами (vos sos вместо tú eres). Это называется **voseo**. На карточках и в школах учат tú — это безопасно.",
                ),
                TheorySection(
                    type = TheorySectionType.WARNING,
                    heading = "Род важен!",
                    body = "**nosotros** = мы (только мужчины ИЛИ смешанная группа)\n**nosotras** = мы (только женщины)\nТо же с vosotros/as и ellos/ellas. Если хотя бы один мужчина в группе — мужской род.",
                ),
            ),
            keyTakeaways = listOf(
                "Местоимение часто опускают — окончание уже всё говорит",
                "tú — на «ты», usted — на «Вы»",
                "vosotros — только Испания",
                "Род: nosotros (м/смеш) vs nosotras (только ж)",
                "В Аргентине vos вместо tú — но tú везде поймут",
            ),
            relatedTheory = listOf("u1_l10", "u1_l11"),
        ),

        // ─────────────────────────────────────────────────────────────────
        // u1_l10 — Род существительных el/la
        // ─────────────────────────────────────────────────────────────────
        "u1_l8" to TheoryContent(
            lessonId = "u1_l8",
            title = "Род существительных — el или la",
            subtitle = "Окончание -o → м, -a → ж. Но есть исключения.",
            emoji = "⚤",
            cefr = "A1",
            readMinutes = 4,
            sections = listOf(
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "Главное правило",
                    body = "Каждое существительное в испанском — **мужского** или **женского** рода. Среднего нет. По окончанию обычно понятно:\n• -o → мужской (el libro)\n• -a → женский (la casa)",
                ),
                TheorySection(
                    type = TheorySectionType.TABLE,
                    heading = "Базовая таблица",
                    table = TheoryTable(
                        headers = listOf("Слово", "Род", "Артикль"),
                        rows = listOf(
                            listOf("libro", "м (-o)", "el libro"),
                            listOf("casa", "ж (-a)", "la casa"),
                            listOf("médico", "м", "el médico"),
                            listOf("médica", "ж", "la médica"),
                            listOf("amigo", "м", "el amigo"),
                            listOf("amiga", "ж", "la amiga"),
                        ),
                        highlightedColumns = listOf(2),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.WARNING,
                    heading = "Важные исключения",
                    body = "**el día** (день) — мужской, хотя оканчивается на -a!\n**la mano** (рука) — женский, хотя на -o!\n**el problema, el sistema, el tema** — мужские (греческого происхождения).",
                ),
                TheorySection(
                    type = TheorySectionType.TIP,
                    heading = "💡 Лайфхак",
                    body = "Если не знаешь род — почти всегда работает «-o = м, -a = ж». Запомни 5-10 исключений, и в 95% случаев будешь прав.",
                ),
                TheorySection(
                    type = TheorySectionType.EXAMPLES,
                    examples = listOf(
                        TheoryExample("el libro", "книга", "м: -o → el"),
                        TheoryExample("la casa", "дом", "ж: -a → la"),
                        TheoryExample("el día", "день", "ИСКЛ! м"),
                        TheoryExample("la mano", "рука", "ИСКЛ! ж"),
                    ),
                ),
            ),
            keyTakeaways = listOf(
                "В испанском только 2 рода: м и ж",
                "-o → мужской → el; -a → женский → la",
                "Запомни 5 исключений: el día, la mano, el problema, el sistema, el tema",
                "Артикль показывает род — учи слово сразу с артиклем",
            ),
            relatedTheory = listOf("u1_l9"),
        ),

        // ─────────────────────────────────────────────────────────────────
        // u1_l11 — Артикли el/la/un/una/los/las
        // ─────────────────────────────────────────────────────────────────
        "u1_l9" to TheoryContent(
            lessonId = "u1_l9",
            title = "Все 8 артиклей одной таблицей",
            subtitle = "Определённые el/la/los/las и неопределённые un/una/unos/unas.",
            emoji = "📰",
            cefr = "A1",
            readMinutes = 4,
            sections = listOf(
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "Два типа артиклей",
                    body = "**Определённый** (el/la) — известный объект: **el libro** = эта конкретная книга.\n**Неопределённый** (un/una) — какой-то: **un libro** = какая-то книга, любая.",
                ),
                TheorySection(
                    type = TheorySectionType.TABLE,
                    heading = "Все 8 форм",
                    table = TheoryTable(
                        headers = listOf("", "Опр.", "Неопр."),
                        rows = listOf(
                            listOf("м.ед.", "el", "un"),
                            listOf("ж.ед.", "la", "una"),
                            listOf("м.мн.", "los", "unos"),
                            listOf("ж.мн.", "las", "unas"),
                        ),
                        highlightedColumns = listOf(1, 2),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.COMPARISON,
                    heading = "el libro vs un libro",
                    body = "Когда какой использовать:",
                    comparison = TheoryComparison(
                        leftHeader = "el libro",
                        rightHeader = "un libro",
                        pairs = listOf(
                            "конкретный, известный" to "какой-то, любой",
                            "Dame el libro" to "Dame un libro",
                            "(который договорились)" to "(любой подойдёт)",
                            "второе упоминание" to "первое упоминание",
                        ),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.EXAMPLES,
                    heading = "В контексте",
                    examples = listOf(
                        TheoryExample("Quiero un café.", "Хочу (один) кофе.", "любой"),
                        TheoryExample("El café está frío.", "Кофе остыл.", "тот, что заказал"),
                        TheoryExample("Tengo unos amigos en Madrid.", "У меня есть друзья в Мадриде.", "несколько"),
                        TheoryExample("Las casas son grandes.", "Эти дома большие.", "конкретные"),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.TIP,
                    heading = "💡 Правило для жизни",
                    body = "**Первый раз** упоминаешь объект — un/una. **Дальше** — el/la (мы уже знаем о чём речь).",
                ),
            ),
            keyTakeaways = listOf(
                "Артикли согласуются по роду И числу",
                "el/la — конкретный; un/una — любой",
                "Множественное: los/las (опр.), unos/unas (неопр.)",
                "Первое упоминание → un, дальше → el",
            ),
            relatedTheory = listOf("u1_l8", "u1_l12"),
        ),

        // ─────────────────────────────────────────────────────────────────
        // u1_l12 — Страны и национальности
        // ─────────────────────────────────────────────────────────────────
        "u1_l12" to TheoryContent(
            lessonId = "u1_l12",
            title = "Страны и национальности",
            subtitle = "Soy ruso (м) / Soy rusa (ж). Soy de Rusia.",
            emoji = "🌍",
            cefr = "A1",
            readMinutes = 4,
            sections = listOf(
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "Два способа сказать откуда ты",
                    body = "1. **Soy + национальность**: Soy ruso. Soy española.\n2. **Soy de + страна**: Soy de Rusia. Soy de España.\n\nОба правильные. Первый чаще в разговоре.",
                ),
                TheorySection(
                    type = TheorySectionType.TABLE,
                    heading = "Страна → национальность",
                    table = TheoryTable(
                        headers = listOf("Страна", "Мужчина", "Женщина"),
                        rows = listOf(
                            listOf("Rusia", "ruso", "rusa"),
                            listOf("España", "español", "española"),
                            listOf("México", "mexicano", "mexicana"),
                            listOf("Francia", "francés", "francesa"),
                            listOf("Inglaterra", "inglés", "inglesa"),
                            listOf("Alemania", "alemán", "alemana"),
                            listOf("Italia", "italiano", "italiana"),
                            listOf("China", "chino", "china"),
                        ),
                        highlightedColumns = listOf(1, 2),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.WARNING,
                    heading = "Без заглавной!",
                    body = "Страны пишутся с **большой буквы**: Rusia, España.\nНациональности — с **маленькой**: ruso, español. Это в отличие от русского.",
                ),
                TheorySection(
                    type = TheorySectionType.EXAMPLES,
                    examples = listOf(
                        TheoryExample("Soy ruso.", "Я русский.", "мужчина"),
                        TheoryExample("Soy rusa.", "Я русская.", "женщина"),
                        TheoryExample("Soy de Rusia.", "Я из России.", "альтернатива"),
                        TheoryExample("¿De dónde eres?", "Откуда ты?", "вопрос"),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.TIP,
                    heading = "💡 Если страна неочевидна",
                    body = "Можно сочетать: **Soy ruso, de Moscú.** (Я русский, из Москвы.) Это точнее и звучит по-разговорному.",
                ),
            ),
            keyTakeaways = listOf(
                "Soy + национальность ИЛИ Soy de + страна",
                "Национальность согласуется по роду: -o/-a",
                "Страны с большой, национальности с маленькой буквы",
                "¿De dónde eres? — стандартный вопрос",
            ),
            relatedTheory = listOf("u1_l10", "u1_l13"),
        ),

        // ─────────────────────────────────────────────────────────────────
        // u1_l13 — Числа 0-10
        // ─────────────────────────────────────────────────────────────────
        "u1_l13" to TheoryContent(
            lessonId = "u1_l13",
            title = "Числа от 0 до 10",
            subtitle = "uno → un перед мужским сущ. (un café), una перед женским.",
            emoji = "🔢",
            cefr = "A1",
            readMinutes = 4,
            sections = listOf(
                TheorySection(
                    type = TheorySectionType.TABLE,
                    heading = "Все 11 чисел",
                    table = TheoryTable(
                        headers = listOf("Цифра", "Слово", "Произношение"),
                        rows = listOf(
                            listOf("0", "cero", "[сэ-ро]"),
                            listOf("1", "uno", "[у-но]"),
                            listOf("2", "dos", "[дос]"),
                            listOf("3", "tres", "[трэс]"),
                            listOf("4", "cuatro", "[куа-тро]"),
                            listOf("5", "cinco", "[синь-ко]"),
                            listOf("6", "seis", "[сэйс]"),
                            listOf("7", "siete", "[сьэ-тэ]"),
                            listOf("8", "ocho", "[о-чо]"),
                            listOf("9", "nueve", "[нуэ-вэ]"),
                            listOf("10", "diez", "[дьэс]"),
                        ),
                        highlightedColumns = listOf(1),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "Главная подстава: uno → un",
                    body = "Перед существительным **uno** теряет -o:\n• **un** café (один кофе) — мужской\n• **una** casa (один дом / одна) — женский\n\nСамо «uno» используется только при счёте: uno, dos, tres…",
                ),
                TheorySection(
                    type = TheorySectionType.MNEMONIC,
                    heading = "Запомни ритм",
                    body = "uno-dos-tres-cuatro-cinco — повторяй пятёрками. Через неделю будет автоматически как «раз-два-три» по-русски.",
                ),
                TheorySection(
                    type = TheorySectionType.EXAMPLES,
                    examples = listOf(
                        TheoryExample("Quiero un café.", "Хочу один кофе.", "uno → un"),
                        TheoryExample("Hay una casa.", "Есть один дом.", "una для ж"),
                        TheoryExample("Tengo dos hermanos.", "У меня два брата.", "от 2 — без изменений"),
                        TheoryExample("Cinco euros.", "Пять евро.", ""),
                    ),
                ),
            ),
            keyTakeaways = listOf(
                "0-10 учить наизусть как стих",
                "uno → un перед мужским сущ., una перед женским",
                "От 2 числа не меняются: dos libros, dos casas",
                "При счёте говорят uno (полная форма)",
            ),
            relatedTheory = listOf("u1_l13_5"),
        ),

        // ─────────────────────────────────────────────────────────────────
        // u1_l13_5 — Порядковые числительные (НОВЫЙ урок)
        // ─────────────────────────────────────────────────────────────────
        "u1_l13_5" to TheoryContent(
            lessonId = "u1_l13_5",
            title = "Порядковые числительные 1°–10°",
            subtitle = "primero / segundo / tercero. primero и tercero теряют -o перед сущ.",
            emoji = "🆕",
            cefr = "A1",
            readMinutes = 4,
            sections = listOf(
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "Что такое порядковые",
                    body = "Это «первый, второй, третий…» — указывают **порядок**. В отличие от количественных (один, два, три), они **согласуются по роду и числу** как обычные прилагательные.",
                ),
                TheorySection(
                    type = TheorySectionType.TABLE,
                    heading = "1°–10°",
                    table = TheoryTable(
                        headers = listOf("№", "М. форма", "Ж. форма"),
                        rows = listOf(
                            listOf("1°", "primero", "primera"),
                            listOf("2°", "segundo", "segunda"),
                            listOf("3°", "tercero", "tercera"),
                            listOf("4°", "cuarto", "cuarta"),
                            listOf("5°", "quinto", "quinta"),
                            listOf("6°", "sexto", "sexta"),
                            listOf("7°", "séptimo", "séptima"),
                            listOf("8°", "octavo", "octava"),
                            listOf("9°", "noveno", "novena"),
                            listOf("10°", "décimo", "décima"),
                        ),
                        highlightedColumns = listOf(1, 2),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.WARNING,
                    heading = "Главное правило: primero/tercero → primer/tercer",
                    body = "Перед мужским существительным **primero** и **tercero** теряют -o:\n• el **primer** día (первый день)\n• el **tercer** piso (третий этаж)\n\nЖенский род НЕ меняется: la **primera** vez (первый раз).",
                ),
                TheorySection(
                    type = TheorySectionType.EXAMPLES,
                    examples = listOf(
                        TheoryExample("el primer día", "первый день", "primero → primer"),
                        TheoryExample("el tercer piso", "третий этаж", "tercero → tercer"),
                        TheoryExample("la primera vez", "первый раз", "ж: без изменений"),
                        TheoryExample("mi segundo café", "мой второй кофе", "segundo НЕ теряет -o"),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.TIP,
                    heading = "💡 Только primero и tercero",
                    body = "Только два слова теряют -o: primero и tercero. Все остальные (segundo, cuarto, quinto…) — без изменений.",
                ),
                TheorySection(
                    type = TheorySectionType.MNEMONIC,
                    heading = "Запомни обозначение",
                    body = "Маленький кружок ° используется как «1°, 2°…» — это типографский символ для мужского рода (ordinal indicator). Для женского пишут «1ª, 2ª».",
                ),
            ),
            keyTakeaways = listOf(
                "Порядковые согласуются по роду как прилагательные",
                "primero и tercero → primer / tercer перед мужским сущ.",
                "Женские формы не меняются: primera, tercera",
                "После 10° используют редко — обычно говорят el día 11 («день 11»)",
            ),
            relatedTheory = listOf("u1_l13"),
        ),
    ) + extraTheories()

    // ───────────────────────────────────────────────────────────────────
    //  Helper для краткого создания теорий блоков 1.2..4.4 (Phase 2).
    //  Каждая теория = 3 секции: RULE + (опц. TABLE) + EXAMPLES + takeaways.
    // ───────────────────────────────────────────────────────────────────

    private fun t(
        id: String, title: String, subtitle: String = "",
        emoji: String = "📖", cefr: String = "A2", minutes: Int = 3,
        rule: String,
        tableHeaders: List<String> = emptyList(),
        tableRows: List<List<String>> = emptyList(),
        examples: List<Triple<String, String, String>> = emptyList(),
        tip: String = "",
        warning: String = "",
        takeaways: List<String> = emptyList(),
    ): Pair<String, TheoryContent> {
        val sections = mutableListOf<TheorySection>()
        sections += TheorySection(TheorySectionType.RULE, "Главное правило", body = rule)
        if (tableHeaders.isNotEmpty()) {
            sections += TheorySection(
                TheorySectionType.TABLE, heading = "Таблица",
                table = TheoryTable(headers = tableHeaders, rows = tableRows,
                    highlightedColumns = if (tableHeaders.size >= 2) listOf(1) else emptyList()),
            )
        }
        if (examples.isNotEmpty()) {
            sections += TheorySection(
                TheorySectionType.EXAMPLES, heading = "Примеры",
                examples = examples.map { (es, ru, n) -> TheoryExample(es, ru, n) },
            )
        }
        if (tip.isNotBlank()) {
            sections += TheorySection(TheorySectionType.TIP, heading = "💡 Лайфхак", body = tip)
        }
        if (warning.isNotBlank()) {
            sections += TheorySection(TheorySectionType.WARNING, heading = "⚠ Внимание", body = warning)
        }
        return id to TheoryContent(
            lessonId = id, title = title, subtitle = subtitle, emoji = emoji,
            cefr = cefr, readMinutes = minutes,
            sections = sections, keyTakeaways = takeaways,
        )
    }

    private fun extraTheories(): Map<String, TheoryContent> = mapOf(

        // ═══════════════════════════════════════════════════════════════
        //  БЛОК 1.2 «МОЙ МИР» — 15 теорий
        // ═══════════════════════════════════════════════════════════════
        t("u2_l6", "Числа 11–20", "11 нерегулярных чисел. Затем 16-19 = «10+ед» слитно.",
            emoji = "🔢", cefr = "A1", minutes = 3,
            rule = "Числа 11-15 — отдельные слова (once, doce, trece, catorce, quince). 16-19 пишутся слитно: dieciséis, diecisiete, dieciocho, diecinueve. 20 = veinte.",
            tableHeaders = listOf("Цифра", "Слово"),
            tableRows = listOf(listOf("11", "once"), listOf("12", "doce"), listOf("13", "trece"),
                listOf("14", "catorce"), listOf("15", "quince"), listOf("16", "dieciséis"),
                listOf("17", "diecisiete"), listOf("18", "dieciocho"), listOf("19", "diecinueve"),
                listOf("20", "veinte")),
            examples = listOf(
                Triple("Tengo quince euros.", "У меня 15 евро.", ""),
                Triple("Son las dieciséis.", "16 часов.", "формально"),
            ),
            takeaways = listOf("11-15 — отдельные слова", "16-19 — слитно diez+y+ед", "20 = veinte (с e!)")),

        t("u2_l7", "Числа 21–100", "21-29 слитно. 30-90 — десятки. 31+ через «y»: treinta y uno.",
            emoji = "💯", cefr = "A1", minutes = 3,
            rule = "Десятки: 30 treinta, 40 cuarenta, 50 cincuenta, 60 sesenta, 70 setenta, 80 ochenta, 90 noventa, 100 cien. 21-29 пишутся СЛИТНО (veintiuno-veintinueve). От 31 — через «y»: treinta y uno.",
            tableHeaders = listOf("Цифра", "Слово"),
            tableRows = listOf(listOf("21", "veintiuno"), listOf("25", "veinticinco"),
                listOf("30", "treinta"), listOf("31", "treinta y uno"), listOf("50", "cincuenta"),
                listOf("99", "noventa y nueve"), listOf("100", "cien")),
            examples = listOf(
                Triple("Cuesta cuarenta euros.", "Стоит 40 евро.", ""),
                Triple("Tengo treinta y dos años.", "Мне 32.", ""),
            ),
            warning = "31+ — РАЗДЕЛЬНО («treinta y uno»), не «treintaiuno»! Слитно — только 21-29.",
            takeaways = listOf("21-29 слитно", "31+ через y", "100 = cien (без s!)")),

        t("u2_l4", "TENER ед. ч.", "tengo / tienes / tiene — иметь / возраст / родство",
            emoji = "🟠", cefr = "A1", minutes = 3,
            rule = "TENER = «иметь» (нерегулярный). Для возраста: Tengo X años. Для родства: Tengo dos hermanos.",
            tableHeaders = listOf("Лицо", "Форма", "Пример"),
            tableRows = listOf(listOf("yo", "tengo", "Tengo un perro"),
                listOf("tú", "tienes", "Tienes razón"),
                listOf("él/ella/usted", "tiene", "Tiene 30 años")),
            examples = listOf(
                Triple("Tengo dos hermanos.", "У меня 2 брата.", ""),
                Triple("Tienes mucho tiempo.", "У тебя много времени.", ""),
            ),
            takeaways = listOf("yo tengo (НЕ teno)", "Возраст: tener X años", "Голод: tener hambre")),

        t("u2_l5", "TENER мн. ч.", "tenemos / tenéis / tienen",
            emoji = "🟠", cefr = "A1", minutes = 3,
            rule = "Множественное TENER: nosotros tenemos / vosotros tenéis (Испания) / ellos tienen.",
            tableHeaders = listOf("Лицо", "Форма"),
            tableRows = listOf(listOf("nosotros", "tenemos"), listOf("vosotros", "tenéis (Исп.)"),
                listOf("ellos/ustedes", "tienen")),
            examples = listOf(
                Triple("Tenemos una casa grande.", "У нас большой дом.", ""),
                Triple("¿Tenéis tiempo?", "У вас есть время? (Исп.)", ""),
            ),
            takeaways = listOf("nosotros tenemos", "vosotros tenéis — только Испания", "ellos tienen")),

        t("u2_l8", "Семья — основа", "padre, madre, hermano, hijo. Окончания -o/-a по роду.",
            emoji = "👨‍👩‍👧", cefr = "A1", minutes = 3,
            rule = "Базовая семья: padre/madre, hermano/hermana, hijo/hija. Множественное по мужскому: padres = родители, hermanos = братья (или смешанная группа).",
            tableHeaders = listOf("Семья", "М", "Ж"),
            tableRows = listOf(listOf("Родитель", "padre", "madre"),
                listOf("Брат/сестра", "hermano", "hermana"),
                listOf("Сын/дочь", "hijo", "hija")),
            examples = listOf(
                Triple("Mi padre es médico.", "Папа врач.", ""),
                Triple("Tengo dos hermanas.", "У меня 2 сестры.", ""),
            ),
            tip = "padres = «родители» (буквально «отцы», но имеют в виду оба пола).",
            takeaways = listOf("-o = м, -a = ж", "Мн.ч. через -s", "padres = родители")),

        t("u2_l9", "Расширенная семья", "abuelo, tío, primo, sobrino, nieto",
            emoji = "👵", cefr = "A1", minutes = 3,
            rule = "abuelo/abuela (дед/баб), tío/tía (дядя/тётя), primo/prima (двоюр.брат/сестра), sobrino/sobrina (племянник/ца), nieto/nieta (внук/внучка).",
            tableHeaders = listOf("Родство", "М", "Ж"),
            tableRows = listOf(listOf("Дед/баб", "abuelo", "abuela"),
                listOf("Дядя/тётя", "tío", "tía"),
                listOf("Двоюр", "primo", "prima"),
                listOf("Племянник", "sobrino", "sobrina"),
                listOf("Внук", "nieto", "nieta")),
            examples = listOf(
                Triple("Mi abuela es de Madrid.", "Бабушка из Мадрида.", ""),
                Triple("Tengo dos primos.", "У меня 2 двоюр. брата.", ""),
            ),
            takeaways = listOf("Все по роду -o/-a", "tía с тильдой", "abuelos = дедушка с бабушкой")),

        t("u2_l10", "Притяжательные mi/tu/su", "mi/tu/su — одна форма для м/ж. nuestro/a меняется.",
            emoji = "📎", cefr = "A1", minutes = 3,
            rule = "mi (мой/моя), tu (твой/твоя), su (его/её/Ваш) — НЕ меняются по роду. Меняются по числу: mis, tus, sus. nuestro/nuestra и vuestro/vuestra меняются по роду И числу.",
            tableHeaders = listOf("Лицо", "Ед.", "Мн."),
            tableRows = listOf(listOf("я", "mi", "mis"), listOf("ты", "tu", "tus"),
                listOf("он/она/Вы", "su", "sus"),
                listOf("мы", "nuestro/a", "nuestros/as"),
                listOf("они", "su", "sus")),
            examples = listOf(
                Triple("Mi madre, mis padres.", "Моя мама, мои родители.", ""),
                Triple("Nuestra casa es grande.", "Наш дом большой.", ""),
            ),
            warning = "Важно: tu (без тильды) = «твой», tú (с тильдой) = «ты». Разные слова!",
            takeaways = listOf("mi/tu/su не меняются по роду", "По числу: +s", "nuestro меняется по р И ч")),

        t("u2_l11", "Цвета", "rojo, azul, verde, amarillo, negro, blanco",
            emoji = "🎨", cefr = "A1", minutes = 3,
            rule = "Базовые цвета: rojo (красный), azul (синий), verde (зелёный), amarillo (жёлтый), negro (чёрный), blanco (белый), gris (серый), naranja (оранжевый), rosa (розовый).",
            examples = listOf(
                Triple("El cielo es azul.", "Небо синее.", ""),
                Triple("La rosa es roja.", "Роза красная.", ""),
            ),
            takeaways = listOf("На -o согласуются (rojo/roja)", "На -e/согласную не меняются", "naranja и rosa — несогласуемые")),

        t("u2_l12", "Согласование цветов", "rojo/roja, blanco/blanca. На -e/согласную НЕ меняются.",
            emoji = "🌈", cefr = "A1", minutes = 3,
            rule = "Цвета на -o согласуются по роду: rojo→roja, blanco→blanca, negro→negra. На -e (verde) или согласную (azul, gris, marrón) НЕ меняются. naranja и rosa тоже не меняются (это сущ.→adj).",
            tableHeaders = listOf("М", "Ж"),
            tableRows = listOf(listOf("rojo", "roja"), listOf("blanco", "blanca"),
                listOf("negro", "negra"), listOf("verde", "verde (не меняется)"),
                listOf("azul", "azul (не меняется)")),
            examples = listOf(
                Triple("La casa roja, el coche rojo.", "Красный дом/машина.", ""),
                Triple("La camisa verde, el coche verde.", "Зелёная рубашка/машина.", "не меняется"),
            ),
            takeaways = listOf("-o → -a", "verde, azul, gris не меняются", "Множ.: +s")),

        t("u3_l0", "ESTAR ед. ч.", "estoy / estás / está — для местоположения и состояния",
            emoji = "📍", cefr = "A1", minutes = 4,
            rule = "ESTAR = «быть/находиться» — для ВРЕМЕННЫХ состояний и местоположения. Не путать с SER (постоянное).",
            tableHeaders = listOf("Лицо", "Форма"),
            tableRows = listOf(listOf("yo", "estoy"), listOf("tú", "estás"), listOf("él/ella/usted", "está")),
            examples = listOf(
                Triple("Estoy en casa.", "Я дома.", "местоположение"),
                Triple("Estás cansado.", "Ты устал.", "состояние"),
                Triple("Está en Madrid.", "Он/она в Мадриде.", "местоположение"),
            ),
            warning = "Местоположение → ESTAR (не SER!). «Я в Мадриде» = Estoy en Madrid, НЕ Soy en Madrid.",
            takeaways = listOf("estoy/estás/está", "Местоположение и временное состояние", "Профессия/национальность → SER")),

        t("u3_l1", "Предлоги места", "en, sobre, debajo de, al lado de, entre",
            emoji = "📌", cefr = "A1", minutes = 3,
            rule = "Где находится: en (в/на), sobre (на поверхности), debajo de (под), al lado de (рядом с), entre (между), delante de (перед), detrás de (за/позади).",
            tableHeaders = listOf("Предлог", "Перевод"),
            tableRows = listOf(listOf("en", "в / на"), listOf("sobre", "на (поверх)"),
                listOf("debajo de", "под"), listOf("al lado de", "рядом с"),
                listOf("entre", "между"), listOf("delante de", "перед"), listOf("detrás de", "за")),
            examples = listOf(
                Triple("El gato está sobre la mesa.", "Кот на столе.", ""),
                Triple("Está al lado de la casa.", "Рядом с домом.", ""),
            ),
            takeaways = listOf("en — общее", "sobre = поверх", "Сложные предлоги ВСЕГДА с de")),

        t("u3_l2", "Комнаты дома", "sala, cocina, dormitorio, baño",
            emoji = "🏠", cefr = "A1", minutes = 3,
            rule = "Главные комнаты: la sala (гостиная), la cocina (кухня), el dormitorio (спальня), el baño (ванная), el comedor (столовая), el balcón (балкон), el pasillo (коридор).",
            examples = listOf(
                Triple("Estoy en la cocina.", "Я на кухне.", ""),
                Triple("Voy al baño.", "Иду в ванную.", "a + el → al"),
            ),
            takeaways = listOf("Род запоминать с артиклем", "Комнаты — повседневный словарь", "ir + a + место")),

        t("u3_l3", "Мебель", "sofá, mesa, silla, cama, armario",
            emoji = "🛋", cefr = "A1", minutes = 3,
            rule = "Базовая мебель: el sofá (диван), la mesa (стол), la silla (стул), la cama (кровать), el armario (шкаф), la lámpara (лампа), la nevera (холодильник).",
            examples = listOf(
                Triple("El libro está sobre la mesa.", "Книга на столе.", ""),
                Triple("Mi cama es nueva.", "Моя кровать новая.", ""),
            ),
            takeaways = listOf("sofá — м (с тильдой!)", "mesa, silla, cama — ж", "Мебель в сочетании с estar+место")),

        t("u2_l13", "Множественное число", "На гласную → +s; на согласную → +es; на -z → -ces.",
            emoji = "📚", cefr = "A1", minutes = 3,
            rule = "Главные правила: 1) Слово на гласную → добавь -s (libro→libros). 2) На согласную → -es (papel→papeles). 3) На -z → меняется на -c + es (luz→luces). 4) Тильда исчезает в exámen→exámenes — переносится по правилу.",
            tableHeaders = listOf("Конец", "Прибавь", "Пример"),
            tableRows = listOf(listOf("гласная", "+s", "casa → casas"),
                listOf("согласная", "+es", "papel → papeles"),
                listOf("-z", "z→c +es", "luz → luces")),
            examples = listOf(
                Triple("dos libros", "две книги", ""),
                Triple("tres luces", "3 света/огня", "z→c"),
            ),
            takeaways = listOf("Гласная: +s", "Согласная: +es", "luz → luces (не luzes!)")),

        // ═══════════════════════════════════════════════════════════════
        //  БЛОК 1.3 «ДЕЙСТВИЕ» — 17 теорий (включая u3_l5_5 и u3_l7_5)
        // ═══════════════════════════════════════════════════════════════
        t("u2_l0", "Глаголы -AR ед.ч.", "hablo / hablas / habla — окончания -o/-as/-a",
            emoji = "🔵", cefr = "A1", minutes = 3,
            rule = "Глаголы на -AR (hablar, trabajar, estudiar, comprar) — это 80% испанских глаголов. Окончания ед.ч.: -o, -as, -a.",
            tableHeaders = listOf("Лицо", "Окончание", "Пример (hablar)"),
            tableRows = listOf(listOf("yo", "-o", "hablo"), listOf("tú", "-as", "hablas"),
                listOf("él/ella/usted", "-a", "habla")),
            examples = listOf(
                Triple("Hablo español.", "Я говорю по-испански.", ""),
                Triple("Trabajas en Madrid.", "Ты работаешь в Мадриде.", ""),
            ),
            takeaways = listOf("yo -o, tú -as, él -a", "Применимо ко всем -AR", "80% глаголов")),

        t("u2_l1", "Глаголы -AR полное", "Все 6 форм: -o/-as/-a/-amos/-áis/-an",
            emoji = "🔵", cefr = "A1", minutes = 3,
            rule = "Полная парадигма -AR: окончания -o, -as, -a, -amos, -áis, -an. Один раз выучил — работает со всеми -AR.",
            tableHeaders = listOf("Лицо", "Окончание", "hablar"),
            tableRows = listOf(listOf("yo", "-o", "hablo"), listOf("tú", "-as", "hablas"),
                listOf("él", "-a", "habla"), listOf("nosotros", "-amos", "hablamos"),
                listOf("vosotros", "-áis", "habláis"), listOf("ellos", "-an", "hablan")),
            takeaways = listOf("Учи окончания, не каждое слово", "vosotros -áis с тильдой", "ellos / ustedes -an")),

        t("u2_l2", "Глаголы -ER", "como/comes/come/comemos/coméis/comen",
            emoji = "🔵", cefr = "A1", minutes = 3,
            rule = "Глаголы на -ER (comer, beber, leer): окончания -o, -es, -e, -emos, -éis, -en.",
            tableHeaders = listOf("Лицо", "Окончание"),
            tableRows = listOf(listOf("yo", "-o (como)"), listOf("tú", "-es (comes)"),
                listOf("él", "-e (come)"), listOf("nos", "-emos"),
                listOf("vos", "-éis"), listOf("ellos", "-en")),
            examples = listOf(
                Triple("Como pan.", "Я ем хлеб.", ""),
                Triple("Bebes agua.", "Ты пьёшь воду.", ""),
            ),
            takeaways = listOf("-er → -o/-es/-e", "Отличается от -ar только e/a", "leer → leo, lees, lee")),

        t("u2_l3", "Глаголы -IR", "vivo/vives/vive/vivimos/vivís/viven",
            emoji = "🔵", cefr = "A1", minutes = 3,
            rule = "Глаголы на -IR (vivir, escribir, abrir): окончания -o, -es, -e, -imos, -ís, -en. Отличаются от -ER только в nosotros (-imos вместо -emos) и vosotros (-ís).",
            tableHeaders = listOf("Лицо", "-ER", "-IR"),
            tableRows = listOf(listOf("nos", "-emos", "-imos"), listOf("vos", "-éis", "-ís")),
            examples = listOf(
                Triple("Vivimos en Madrid.", "Живём в Мадриде.", ""),
                Triple("Escribís cartas.", "Вы пишете письма.", ""),
            ),
            takeaways = listOf("-IR ≈ -ER", "Только nos/vos отличаются", "vivimos, не vivemos")),

        t("u3_l4", "Еда", "pan, leche, agua, café, fruta, carne",
            emoji = "🍞", cefr = "A1", minutes = 3,
            rule = "Базовая еда: el pan, la leche, el agua (ж, но el!), el café, la fruta, la carne, el queso, el pescado, el huevo, la sopa.",
            warning = "agua — женский род, но артикль EL для звучности (el agua, las aguas). Прилагательное согласуется по роду: el agua FRÍA.",
            takeaways = listOf("Запоминать с артиклем", "el agua — особый случай", "Базовые продукты — must-know")),

        t("u3_l5", "В ресторане", "el menú, el plato, la cuenta, la propina",
            emoji = "🍽", cefr = "A1", minutes = 3,
            rule = "Лексика ресторана: el menú (меню), el plato (блюдо/тарелка), la cuenta (счёт), la propina (чаевые), el camarero/la camarera (официант/ка), la bebida (напиток), el postre (десерт).",
            examples = listOf(
                Triple("La cuenta, por favor.", "Счёт, пожалуйста.", ""),
                Triple("¿Tiene el menú?", "У вас есть меню?", ""),
            ),
            takeaways = listOf("La cuenta por favor — must-know", "menú — м с тильдой", "Чаевые в Испании ~10%")),

        t("u3_l5_5", "🆕 Глагол hay", "«есть/имеется». Безличный, не меняется.",
            emoji = "🆕", cefr = "A1", minutes = 3,
            rule = "hay = безличная форма HABER. Означает «есть/имеется». НЕ меняется по числу: hay un libro / hay libros. После hay — НЕопределённый артикль (un/una/unos/unas) или ничего.",
            examples = listOf(
                Triple("Hay un café cerca.", "Есть кафе рядом.", ""),
                Triple("Hay tres libros.", "Есть 3 книги.", ""),
                Triple("¿Hay agua?", "Есть вода?", "вопрос"),
                Triple("No hay nada.", "Нет ничего.", "отрицание"),
            ),
            warning = "hay vs estar! hay вводит НОВЫЙ объект (что существует). está — где находится конкретный.",
            takeaways = listOf("hay = «есть/имеется»", "Не меняется", "После hay — un/una, НЕ el/la")),

        t("u3_l6", "QUERER", "quiero/quieres/quiere — хотеть. e→ie кроме nos/vos.",
            emoji = "❤️", cefr = "A1", minutes = 3,
            rule = "QUERER = «хотеть». Отклоняющийся: e→ie. Но в nosotros/vosotros отклонение НЕ работает: queremos / queréis.",
            tableHeaders = listOf("Лицо", "Форма"),
            tableRows = listOf(listOf("yo", "quiero"), listOf("tú", "quieres"), listOf("él", "quiere"),
                listOf("nos", "queremos (БЕЗ ie)"), listOf("vos", "queréis"), listOf("ellos", "quieren")),
            examples = listOf(
                Triple("Quiero un café.", "Хочу кофе.", ""),
                Triple("Queremos viajar.", "Хотим путешествовать.", "+ инф"),
            ),
            takeaways = listOf("e→ie кроме nos/vos", "querer + инф = хотеть делать", "querer + a + кого = любить кого-то")),

        t("u3_l7", "PODER", "puedo/puedes/puede — мочь. o→ue кроме nos/vos.",
            emoji = "💪", cefr = "A1", minutes = 3,
            rule = "PODER = «мочь». Отклоняющийся: o→ue. В nosotros/vosotros отклонение НЕ работает: podemos / podéis.",
            tableHeaders = listOf("Лицо", "Форма"),
            tableRows = listOf(listOf("yo", "puedo"), listOf("tú", "puedes"), listOf("él", "puede"),
                listOf("nos", "podemos (БЕЗ ue)"), listOf("vos", "podéis"), listOf("ellos", "pueden")),
            examples = listOf(
                Triple("¿Puedo entrar?", "Можно войти?", ""),
                Triple("Podemos ayudar.", "Можем помочь.", ""),
            ),
            takeaways = listOf("o→ue кроме nos/vos", "¿Puedo? — вежливое «можно?»", "poder + инф")),

        t("u3_l7_5", "🆕 Отклоняющиеся e→i", "pedir → pido. Только -IR глаголы. Третий тип отклонения.",
            emoji = "🆕", cefr = "A2", minutes = 4,
            rule = "Третий тип отклонения e→i — ТОЛЬКО для -IR глаголов: pedir (просить), servir (обслуживать), repetir (повторять), seguir (следовать), elegir (выбирать). В nosotros/vosotros отклонение НЕ работает.",
            tableHeaders = listOf("Лицо", "pedir", "servir"),
            tableRows = listOf(listOf("yo", "pido", "sirvo"), listOf("tú", "pides", "sirves"),
                listOf("él", "pide", "sirve"), listOf("nos", "pedimos", "servimos"),
                listOf("ellos", "piden", "sirven")),
            examples = listOf(
                Triple("Pido un café.", "Прошу/заказываю кофе.", ""),
                Triple("Sirven pizza.", "Подают пиццу.", ""),
            ),
            takeaways = listOf("Только -IR", "e→i кроме nos/vos", "decir тоже e→i + 1 лицо нерег: digo")),

        t("u3_l8", "Время — ¿Qué hora es?", "Es la una. Son las dos.",
            emoji = "⏰", cefr = "A1", minutes = 4,
            rule = "Для 1 часа: Es la una. Для 2+ часов: Son las + число. Минуты: y media (30), y cuarto (15), menos cuarto (45).",
            tableHeaders = listOf("Время", "Как сказать"),
            tableRows = listOf(listOf("1:00", "Es la una"), listOf("2:00", "Son las dos"),
                listOf("3:30", "Son las tres y media"), listOf("4:15", "Son las cuatro y cuarto"),
                listOf("5:45", "Son las seis menos cuarto")),
            takeaways = listOf("1 → Es la, 2+ → Son las", "media = 30, cuarto = 15", "menos cuarto = «без четверти»")),

        t("u3_l9", "Дни недели", "lunes, martes, miércoles, jueves, viernes, sábado, domingo",
            emoji = "📅", cefr = "A1", minutes = 2,
            rule = "Дни недели — С МАЛЕНЬКОЙ буквы (в отличие от русского). Все мужского рода: el lunes. Используются с артиклем: «el lunes voy a...» = «в понедельник иду...». Множественное «los lunes» = по понедельникам.",
            takeaways = listOf("С маленькой буквы", "Все М. рода", "el lunes = в понедельник; los lunes = по понедельникам")),

        t("u3_l10", "Месяцы", "enero..diciembre — 12 названий",
            emoji = "📆", cefr = "A1", minutes = 2,
            rule = "Месяцы пишутся С МАЛЕНЬКОЙ буквы. После «en» без артикля: en enero (в январе). Дата: el 15 de marzo de 2025.",
            takeaways = listOf("С маленькой буквы", "en + месяц (без de)", "Дата: el [число] de [месяц] de [год]")),

        t("u3_l11", "Наречия времени", "hoy, mañana, ayer, ahora, siempre, nunca",
            emoji = "⏱", cefr = "A1", minutes = 2,
            rule = "Время: hoy (сегодня), mañana (завтра), ayer (вчера), ahora (сейчас), antes (раньше), después (потом), siempre (всегда), nunca (никогда), a veces (иногда), pronto (скоро).",
            takeaways = listOf("mañana = «завтра» И «утро»", "siempre/nunca — антонимы", "Ставить впереди или после глагола")),

        t("u3_l12", "Вопросительные слова", "¿Qué? ¿Quién? ¿Dónde? ¿Cuándo? ¿Cuánto? ¿Cómo? ¿Por qué?",
            emoji = "❓", cefr = "A1", minutes = 3,
            rule = "Вопросительные слова — ВСЕГДА с тильдой (отличает от союза). ¿Qué? (что), ¿Quién? (кто), ¿Dónde? (где), ¿Cuándo? (когда), ¿Cuánto? (сколько), ¿Cómo? (как), ¿Por qué? (почему).",
            warning = "qué (с тильдой) = вопрос; que (без) = союз. ¿Dónde? vs donde (без тильды).",
            takeaways = listOf("Все с тильдой", "Перевёрнутый ¿ в начале", "Por qué (раздельно) vs porque (слитно — «потому что»)")),

        t("u3_l13", "Отрицание", "no + глагол. ДВОЙНОЕ отрицание: No tengo nada.",
            emoji = "🚫", cefr = "A1", minutes = 3,
            rule = "Главное отрицание: NO перед глаголом. Двойное отрицание ОБЯЗАТЕЛЬНО: «No tengo nada» (= «ничего нет», буквально «не имею ничего»). Но если nada/nadie/nunca стоит ВПЕРЕДИ — no не нужно: «Nunca trabajo» = «Nunca no trabajo» — НЕТ.",
            examples = listOf(
                Triple("No sé.", "Не знаю.", ""),
                Triple("No tengo nada.", "Ничего нет.", "двойное"),
                Triple("Nunca como carne.", "Никогда не ем мясо.", "Nunca впереди → no не нужно"),
            ),
            takeaways = listOf("no перед глаголом", "Двойное отрицание норма", "Nunca впереди → без no")),

        // ═══════════════════════════════════════════════════════════════
        //  БЛОК 1.4 «ВЫЖИВАНИЕ» — 16 теорий (с u4_l13_5)
        // ═══════════════════════════════════════════════════════════════
        t("u4_l2", "Транспорт", "metro, autobús, taxi, tren, coche, avión",
            emoji = "🚇", cefr = "A1", minutes = 2,
            rule = "Транспорт: el metro, el autobús (с тильдой!), el taxi, el tren, el coche, la bici, el avión, el barco. Конструкция «en + транспорт»: voy en metro, en coche.",
            takeaways = listOf("EN + транспорт", "autobús, avión — с тильдой", "Все мужского рода (искл. la bici)")),

        t("u4_l0", "IR полное", "voy/vas/va/vamos/vais/van",
            emoji = "🏃", cefr = "A1", minutes = 3,
            rule = "IR = «идти/ехать». Полностью неправильный.",
            tableHeaders = listOf("Лицо", "Форма"),
            tableRows = listOf(listOf("yo", "voy"), listOf("tú", "vas"), listOf("él", "va"),
                listOf("nos", "vamos"), listOf("vos", "vais"), listOf("ellos", "van")),
            examples = listOf(Triple("Voy a casa.", "Иду домой.", "")),
            takeaways = listOf("Полностью неправильный", "voy/vas/va/vamos/vais/van", "ir + a + место/цель")),

        t("u4_l1", "IR + A + место", "Voy al cine. a+el → al",
            emoji = "🎯", cefr = "A1", minutes = 3,
            rule = "Конструкция «IR + A + место». Особое правило: a + el СЛИВАЕТСЯ в AL. a + la / a + las / a + los — НЕ сливаются. К casa — без артикля: voy a casa.",
            examples = listOf(
                Triple("Voy al cine.", "В кино.", "a + el = al"),
                Triple("Voy a la tienda.", "В магазин.", "a + la НЕ сливается"),
                Triple("Voy a Madrid.", "В Мадрид.", "к городу — без артикля"),
            ),
            takeaways = listOf("a + el = al (ВСЕГДА)", "a + la — раздельно", "К casa без артикля")),

        t("u4_l3", "Дорога", "¿Cómo llego? gira / sigue recto / a la derecha",
            emoji = "🗺", cefr = "A1", minutes = 3,
            rule = "Спросить: ¿Cómo llego a...? Указания: gira a la derecha (поверни направо), gira a la izquierda (налево), sigue recto (иди прямо), cerca (близко), lejos (далеко).",
            takeaways = listOf("derecha = правая, izquierda = левая", "recto = прямо", "a la + сторона")),

        t("u4_l4", "Магазин", "¿Cuánto cuesta? caro / barato",
            emoji = "🛒", cefr = "A1", minutes = 3,
            rule = "В магазине: ¿Cuánto cuesta? (сколько стоит?), caro/cara (дорогой/ая), barato/barata (дёшевый/ая), comprar (покупать), vender (продавать), la talla (размер).",
            takeaways = listOf("¿Cuánto cuesta? — must-know", "caro/barato согласуются", "talla = размер")),

        t("u4_l5", "Деньги", "el euro, en efectivo / con tarjeta",
            emoji = "💶", cefr = "A1", minutes = 3,
            rule = "Оплата: el euro, el precio (цена), en efectivo (наличными), con tarjeta (картой), ¿Tiene cambio? (есть сдача?), la moneda (монета), el billete (купюра/билет).",
            takeaways = listOf("EN efectivo, CON tarjeta", "billete = купюра И билет", "Cambio = сдача и обмен")),

        t("u4_l6", "GUSTAR (1)", "ОБРАТНАЯ конструкция: me gusta / me gustan",
            emoji = "❤", cefr = "A1", minutes = 4,
            rule = "GUSTAR = «нравиться» — обратная конструкция. «Мне нравится» = me GUSTA + ед.ч. «Мне нравятся» = me GUSTAN + мн.ч. Глагол согласуется с тем ЧТО нравится, не с тем КОМУ.",
            examples = listOf(
                Triple("Me gusta el café.", "Мне нравится кофе.", "ед.ч. → gusta"),
                Triple("Me gustan los gatos.", "Мне нравятся коты.", "мн.ч. → gustan"),
                Triple("Me gusta leer.", "Нравится читать.", "инфинитив → gusta"),
            ),
            warning = "Согласование с ОБЪЕКТОМ, не с лицом! «Me gusta el café» (не «yo gusto»).",
            takeaways = listOf("Обратная конструкция", "gusta = ед, gustan = мн", "С инф → всегда gusta")),

        t("u4_l7", "GUSTAR (2)", "te/le/nos/os/les gusta — все местоимения",
            emoji = "❤", cefr = "A1", minutes = 3,
            rule = "Все формы GUSTAR: me gusta (мне), te gusta (тебе), le gusta (ему/ей/Вам), nos gusta (нам), os gusta (вам Исп.), les gusta (им/Вам мн).",
            tableHeaders = listOf("Местоим.", "Перевод"),
            tableRows = listOf(listOf("me", "мне"), listOf("te", "тебе"), listOf("le", "ему/ей/Вам"),
                listOf("nos", "нам"), listOf("os", "вам (Исп)"), listOf("les", "им/Вам мн")),
            takeaways = listOf("le для él/ella/usted", "les для ellos/ustedes", "Можно усилить: a mí me gusta")),

        t("u4_l8", "Тело", "cabeza, brazo, pierna, mano, ojo",
            emoji = "🦴", cefr = "A1", minutes = 3,
            rule = "Части тела: la cabeza (голова), el brazo (рука), la pierna (нога), la mano (рука-кисть, ИСКЛ! ж), el ojo (глаз), la boca (рот), la nariz (нос), el pelo (волосы), el pie (ступня).",
            warning = "la mano — ж род (исключение!). el día тоже исключение в обратную сторону.",
            takeaways = listOf("la mano — ж исключ", "doler как gustar: me duele la cabeza")),

        t("u4_l9", "Здоровье", "Me duele... / Tengo fiebre",
            emoji = "🤒", cefr = "A1", minutes = 3,
            rule = "Конструкции здоровья: «Me duele + la/el + часть тела» (у меня болит). «Tengo fiebre/tos/dolor» (у меня температура/кашель/боль). «Estoy enfermo/a» (я болен/больна).",
            examples = listOf(
                Triple("Me duele la cabeza.", "Голова болит.", ""),
                Triple("Tengo fiebre.", "У меня темпера.", ""),
            ),
            takeaways = listOf("doler как gustar", "tener + fiebre/tos (без артикля)", "estar + enfermo (состояние)")),

        t("u4_l10", "Одежда", "camisa, pantalón, vestido, zapatos",
            emoji = "👗", cefr = "A1", minutes = 3,
            rule = "Одежда: la camisa (рубашка), el pantalón (штаны), el vestido (платье), los zapatos (обувь — обычно мн.), la chaqueta (куртка), la camiseta (футболка), la falda (юбка).",
            takeaways = listOf("zapatos обычно во мн", "pantalón может быть «pantalones»", "Llevar (носить) + сущ.")),

        t("u4_l11", "Погода", "Hace calor/frío. Llueve. Nieva.",
            emoji = "🌤", cefr = "A1", minutes = 3,
            rule = "Погода через HACE + сущ.: Hace calor (жарко), Hace frío (холодно), Hace sol (солнечно), Hace viento (ветрено), Hace buen tiempo (хорошо). Безличные глаголы: Llueve (идёт дождь), Nieva (идёт снег).",
            warning = "«Hace calor», НЕ «Es calor»! Погода — через hace + сущ.",
            takeaways = listOf("hace + сущ для погоды", "llueve, nieva — безличные", "está nublado = облачно")),

        t("u4_l12", "Мой день", "me levanto, desayuno, trabajo, ceno, me acuesto",
            emoji = "🌅", cefr = "A1", minutes = 3,
            rule = "Распорядок: me levanto (встаю), me ducho (душ), desayuno (завтракаю), trabajo (работаю), como (обедаю), ceno (ужинаю), me acuesto (ложусь). Возвратные с местоим. me/te/se.",
            takeaways = listOf("Возвратные: me + verb", "comer = и есть и обедать", "cenar = ужинать")),

        t("u4_l13", "Возвратные глаголы", "levantarse, ducharse, acostarse — me/te/se/nos/os/se",
            emoji = "🔄", cefr = "A1", minutes = 4,
            rule = "Возвратные глаголы — действие на себя. Местоимение меняется по лицу: me/te/se/nos/os/se. Ставится ПЕРЕД глаголом: me levanto. С инфинитивом — может крепиться: querer levantarme.",
            tableHeaders = listOf("Лицо", "levantarse"),
            tableRows = listOf(listOf("yo", "me levanto"), listOf("tú", "te levantas"),
                listOf("él/ella/usted", "se levanta"), listOf("nos", "nos levantamos"),
                listOf("vos", "os levantáis"), listOf("ellos", "se levantan")),
            takeaways = listOf("Местоим перед глаголом", "С инф можно прикрепить", "se — для él/ella/ellos/ellas/usted")),

        t("u4_l13_5", "🆕 4 правила формы YO", "-go (tengo, hago), -zco (conozco), -y (estoy), полностью нерег. (sé)",
            emoji = "🆕", cefr = "A2", minutes = 4,
            rule = "Многие испанские глаголы имеют нерегулярную форму ТОЛЬКО в yo (1.л.ед.). 4 группы: 1) -go (tener→tengo, hacer→hago, salir→salgo, poner→pongo, decir→digo); 2) -zco для глаголов на -CER/-CIR (conocer→conozco, conducir→conduzco); 3) -y (estar→estoy, ser→soy, ir→voy, dar→doy); 4) полностью нерегулярные (saber→sé, ver→veo, caber→quepo).",
            examples = listOf(
                Triple("Tengo tiempo.", "У меня есть время.", "tener → tengo"),
                Triple("Conozco Madrid.", "Знаю Мадрид.", "conocer → conozco"),
                Triple("Sé la verdad.", "Знаю правду.", "saber → sé"),
            ),
            takeaways = listOf("4 группы yo-нерегулярных", "Только в 1.л.ед!", "Остальные лица — обычно регулярные")),

        // ═══════════════════════════════════════════════════════════════
        //  A2 · БЛОК 2.1 «В ПРОШЛОМ» — Pretérito Indefinido (16 теорий)
        // ═══════════════════════════════════════════════════════════════
        t("u5_l0", "Pretérito Indefinido — что это", "Простое прошедшее. Закончилось в прошлом.",
            emoji = "📅", cefr = "A2", minutes = 3,
            rule = "Pretérito Indefinido = простое прошедшее. Для законченных действий с конкретным временем. Маркеры: ayer (вчера), anoche, el lunes, en 2020, hace dos días.",
            takeaways = listOf("Действие закончилось", "Конкретное время", "Маркеры: ayer, en 2020")),

        t("u5_l1", "Indefinido -AR", "hablé/hablaste/habló/hablamos/hablasteis/hablaron",
            emoji = "📅", cefr = "A2", minutes = 3,
            rule = "Окончания -AR в Indefinido: -é, -aste, -ó, -amos, -asteis, -aron. Тильда обязательна на -é и -ó.",
            tableHeaders = listOf("Лицо", "hablar"),
            tableRows = listOf(listOf("yo", "hablé"), listOf("tú", "hablaste"), listOf("él", "habló"),
                listOf("nos", "hablamos"), listOf("vos", "hablasteis"), listOf("ellos", "hablaron")),
            takeaways = listOf("Тильды на yo/él", "Совпадает с Pres только в nosotros (hablamos)", "По контексту понятно")),

        t("u5_l2", "Indefinido -ER/-IR", "comí/comiste/comió. Одинаковые окончания!",
            emoji = "📅", cefr = "A2", minutes = 3,
            rule = "-ER и -IR в Indefinido — ОДИНАКОВЫЕ окончания: -í, -iste, -ió, -imos, -isteis, -ieron. Тильды на yo и él.",
            tableHeaders = listOf("Лицо", "comer", "vivir"),
            tableRows = listOf(listOf("yo", "comí", "viví"), listOf("tú", "comiste", "viviste"),
                listOf("él", "comió", "vivió"), listOf("nos", "comimos", "vivimos"),
                listOf("ellos", "comieron", "vivieron")),
            takeaways = listOf("-ER и -IR одинаково", "Тильды на yo/él", "vivir nos: vivimos = и Pres и Indef!")),

        t("u5_l3", "Ser vs Estar в прошлом", "fui = был (постоянно/пошёл) vs estuve = находился",
            emoji = "🌍", cefr = "A2", minutes = 4,
            rule = "В Indefinido: SER → fui/fuiste/fue/fuimos/fueron. ESTAR → estuve/estuviste/estuvo/estuvimos/estuvieron. fui используется ДВУМЯ глаголами: SER и IR — контекст подсказывает.",
            warning = "Местоположение в прошлом → ESTUVE (не fui). «Был в Мадриде» = Estuve en Madrid.",
            takeaways = listOf("fui = ser ИЛИ ir", "estuve = находился", "Контекст всегда показывает")),

        t("u5_l4", "Истории — связки", "primero, después, luego, al final, por la mañana",
            emoji = "💬", cefr = "A2", minutes = 3,
            rule = "Слова-связки для рассказа: primero (сначала), después (потом), luego (затем), al final (в конце), por la mañana (утром), por la tarde (днём), por la noche (вечером).",
            takeaways = listOf("Связки делают рассказ", "primero...después...al final", "por la mañana/tarde/noche")),

        t("u5_l5", "Мини-чекпоинт Indefinido", "Все правильные глаголы за один тест",
            emoji = "🎯", cefr = "A2", minutes = 3,
            rule = "Повторение: -AR (-é,-aste,-ó,-amos,-asteis,-aron) и -ER/-IR (-í,-iste,-ió,-imos,-isteis,-ieron).",
            takeaways = listOf("Учить окончания", "Тильды важны", "Проверка перед нерегулярными")),

        t("u5_l6", "Indef irreg: ir/ser → fui", "Одна форма для двух глаголов",
            emoji = "📅", cefr = "A2", minutes = 3,
            rule = "ir и ser в Indefinido имеют ОДИНАКОВУЮ форму: fui, fuiste, fue, fuimos, fueron. Контекст определяет смысл.",
            examples = listOf(
                Triple("Fui al cine.", "Я пошёл в кино.", "ir"),
                Triple("Fui estudiante.", "Я был студентом.", "ser"),
            ),
            takeaways = listOf("fui — ser ИЛИ ir", "fui+a+место = пошёл", "fui+проф/нац = был")),

        t("u5_l7", "Indef irreg: tener→tuve, estar→estuve",
            "Основа меняется (tuv-/estuv-). Окончания -e/-iste/-o/-imos/-ieron",
            emoji = "📅", cefr = "A2", minutes = 4,
            rule = "Нерегулярные Indefinido — особая основа + СВОИ окончания (без тильд!): -e, -iste, -o, -imos, -ieron. Группа -uv-: tener→tuv-, estar→estuv-, andar→anduv-, poder→pud-.",
            tableHeaders = listOf("Глагол", "Основа", "yo"),
            tableRows = listOf(listOf("tener", "tuv-", "tuve"), listOf("estar", "estuv-", "estuve"),
                listOf("andar", "anduv-", "anduve"), listOf("poder", "pud-", "pude")),
            takeaways = listOf("-uv- группа", "Без тильд (tuve, не tuvé)", "Окончания одинаковые для всех нерег.")),

        t("u5_l8", "Indef irreg: hacer→hice, querer→quise",
            "Группа с -i- в основе. hacer→hic-, querer→quis-",
            emoji = "📅", cefr = "A2", minutes = 4,
            rule = "Группа с -i- основой: hacer→hic-, querer→quis-, venir→vin-, decir→dij-. ВНИМАНИЕ: él hizo (с z!) — звуковое правило: hic+o → hizo (звук [исо]).",
            tableHeaders = listOf("Глагол", "yo", "él"),
            tableRows = listOf(listOf("hacer", "hice", "hizo"), listOf("querer", "quise", "quiso"),
                listOf("venir", "vine", "vino"), listOf("decir", "dije", "dijo")),
            warning = "él hizo (с z, не c!) — звуковое правило перед -o.",
            takeaways = listOf("hic-/quis-/vin-/dij-", "él hizo (с Z!)", "vino: «он пришёл» И «вино»")),

        t("u5_l8_5", "🆕 Pluscuamperfecto Indicativo", "había + participio. «Уже было сделано до...»",
            emoji = "🆕", cefr = "A2", minutes = 4,
            rule = "Pluscuamperfecto = haber в Imperfecto + participio. Для действия которое произошло ДО другого прошлого. «Уже...».",
            tableHeaders = listOf("Лицо", "haber Imp", "+ part"),
            tableRows = listOf(listOf("yo", "había", "comido"), listOf("tú", "habías", "vivido"),
                listOf("él", "había", "hecho"), listOf("nos", "habíamos", "ido"),
                listOf("ellos", "habían", "venido")),
            examples = listOf(
                Triple("Cuando llegué, ya había comido.", "Когда пришёл — уже поел.", ""),
            ),
            takeaways = listOf("ya + Pluscuamp", "haber Imperfect + part", "Прошлое в прошлом")),

        t("u5_l9", "Por vs Para — основы", "por = причина/способ, para = цель/получатель",
            emoji = "📝", cefr = "A2", minutes = 4,
            rule = "PARA: цель (Estudio para aprender), получатель (Para ti), срок (Para mañana). POR: причина (Gracias por todo), способ (Por teléfono), время (Por la mañana), за что (Pago por el café).",
            takeaways = listOf("para → цель/кому/срок", "por → причина/способ/период", "Учится практикой")),

        t("u5_l10", "Связный диалог", "Pretérito Indefinido + связки в рассказе",
            emoji = "💬", cefr = "A2", minutes = 3,
            rule = "Применяем Indefinido + связки в диалоге о выходных/командировке. Структура: маркер времени + действие + связка + следующее.",
            takeaways = listOf("Indefinido + связки", "Естественный flow", "Практика устных рассказов")),

        t("u5_l11", "Indef irreg: poder→pude, saber→supe",
            "Особый смысл: pude=смог (тогда), supe=узнал (момент)",
            emoji = "📅", cefr = "A2", minutes = 4,
            rule = "poder→pud-, saber→sup- в Indefinido. Тонкость: «pude» = смог (получилось в тот момент), «supe» = узнал (момент получения информации). В отличие от Imperfecto: podía = мог (вообще), sabía = знал.",
            takeaways = listOf("pude = смог тогда", "supe = узнал в момент", "Нюансы Indef vs Imp")),

        t("u5_l12", "Indef irreg: dar→di, ver→vi, decir→dije",
            "Очень короткие основы: d-, v-, dij-",
            emoji = "📅", cefr = "A2", minutes = 4,
            rule = "Очень короткие нерегулярные: dar → di/diste/dio/dimos/dieron (БЕЗ тильд!). ver → vi/viste/vio/vimos/vieron. decir → dije/dijiste/dijo/dijimos/dijeron.",
            warning = "dar Indef yo = di (просто «di»), не «dí» с тильдой! ver yo = vi (тоже без тильды).",
            takeaways = listOf("dar → di (без тильды)", "ver → vi (без тильды)", "decir → dije (j!)")),

        t("u5_l13", "Связный текст в прошлом", "porque, pero, entonces, por eso",
            emoji = "🗣", cefr = "A2", minutes = 3,
            rule = "Связки причинно-следственные: porque (потому что), pero (но), entonces (тогда), por eso (поэтому), después de (после того как), antes de (перед тем как).",
            takeaways = listOf("porque vs por qué", "pero для контраста", "por eso = поэтому")),

        t("u5_l14", "Тест Indefinido полный", "Regulares + irregulares + связки",
            emoji = "🎯", cefr = "A2", minutes = 3,
            rule = "Финальное повторение блока: все формы Indefinido + por/para + связки рассказа.",
            takeaways = listOf("Все Indef в одном", "Готов к Imperfecto", "Большой шаг A2")),

        // ═══════════════════════════════════════════════════════════════
        //  A2 · БЛОК 2.2 «РАНЬШЕ И СЕЙЧАС» — Imperfecto + сравнения (16)
        // ═══════════════════════════════════════════════════════════════
        t("u6_l0", "Imperfecto -AR", "hablaba/hablabas/hablábamos. Привычка в прошлом.",
            emoji = "⏳", cefr = "A2", minutes = 3,
            rule = "Imperfecto -AR: -aba, -abas, -aba, -ábamos, -abais, -aban. Для ПРИВЫЧКИ или ОПИСАНИЯ в прошлом. «Раньше...», «когда был...».",
            tableHeaders = listOf("Лицо", "hablar"),
            tableRows = listOf(listOf("yo", "hablaba"), listOf("tú", "hablabas"),
                listOf("él", "hablaba"), listOf("nos", "hablábamos"),
                listOf("vos", "hablabais"), listOf("ellos", "hablaban")),
            takeaways = listOf("-aba/-abas/-aba", "Привычка/описание", "Тильда только в nosotros")),

        t("u6_l1", "Imperfecto -ER/-IR + irreg", "comía/vivía + ser→era / ir→iba / ver→veía",
            emoji = "⏳", cefr = "A2", minutes = 3,
            rule = "-ER/-IR Imperfecto: -ía, -ías, -ía, -íamos, -íais, -ían. Тильды ВЕЗДЕ! Только 3 нерегулярных глагола: ser→era/eras/era, ir→iba/ibas/iba, ver→veía/veías/veía.",
            tableHeaders = listOf("Глагол", "yo", "tú"),
            tableRows = listOf(listOf("comer", "comía", "comías"), listOf("vivir", "vivía", "vivías"),
                listOf("ser", "era", "eras"), listOf("ir", "iba", "ibas"),
                listOf("ver", "veía", "veías")),
            takeaways = listOf("-ER/-IR одинаково: -ía", "Только 3 нерегулярных", "Тильды на í")),

        t("u6_l2", "Indefinido vs Imperfecto", "Indef = однократно. Imp = привычка/описание.",
            emoji = "⚡", cefr = "A2", minutes = 4,
            rule = "Главное правило выбора: Indefinido — для однократных событий с известным временем (ayer comí, en 2020 viajé). Imperfecto — для привычек (cada día comía), описаний (era alto), параллельных действий (mientras comía, leía).",
            tableHeaders = listOf("Тип действия", "Время"),
            tableRows = listOf(listOf("Однократное", "Indefinido"),
                listOf("Привычка", "Imperfecto"),
                listOf("Описание", "Imperfecto"),
                listOf("Параллельно", "Imperfecto + Imperfecto")),
            takeaways = listOf("Однократно → Indef", "Привычка/описание → Imp", "Часто оба в одном предложении")),

        t("u6_l3", "Описания прошлого", "Cuando era niño... — era/tenía/vivía/iba",
            emoji = "📖", cefr = "A2", minutes = 3,
            rule = "Описание прошлого через Imperfecto. Шаблон «Cuando era niño/a...» открывает рассказ о детстве. era + adj/проф, tenía + сущ, vivía en + место.",
            takeaways = listOf("Cuando era niño/a...", "era/tenía/vivía", "Imperfecto = «фон» истории")),

        t("u6_l4", "Сравнение más/menos que", "más alto que / menos caro que",
            emoji = "📊", cefr = "A2", minutes = 3,
            rule = "Больше/меньше: más + adj + que / menos + adj + que. Со сущ: más + сущ + que (más libros que tú). После сравн. — que, не de.",
            warning = "Перед числом — DE (не que): más de 100 = больше 100. más que 100 = неверно!",
            takeaways = listOf("más/menos + adj + que", "Перед числом — DE", "más libros que = больше книг чем")),

        t("u6_l5", "Сравнение tan/tanto como", "Равенство. tan + adj + como / tanto + сущ + como",
            emoji = "⚖", cefr = "A2", minutes = 3,
            rule = "Равенство: tan + adj/нар + como. tanto/a/os/as + сущ + como (согласуется по роду и числу). С прилагательным — tan, не tanto!",
            tableHeaders = listOf("Кейс", "Слово"),
            tableRows = listOf(listOf("С прилаг", "tan + adj"), listOf("С сущ м.ед", "tanto"),
                listOf("С сущ ж.ед", "tanta"), listOf("С сущ м.мн", "tantos"),
                listOf("С сущ ж.мн", "tantas")),
            takeaways = listOf("Adj → tan", "Сущ → tanto согл", "como — без тильды")),

        t("u6_l6", "Превосходная степень", "el más / el mejor / el peor",
            emoji = "🏆", cefr = "A2", minutes = 3,
            rule = "Превосх.: el/la/los/las + más + adj + de. Особые: el mejor (лучший), el peor (худший), el mayor (старший), el menor (младший). Не «más bueno» — а «mejor».",
            warning = "bueno → mejor, malo → peor (особые формы). Не «más bueno».",
            takeaways = listOf("el + más + adj", "el mejor / el peor — особые", "después: + de + группа")),

        t("u6_l7", "Прилагательные-описания", "alto, simpático, listo, guapo, joven, viejo",
            emoji = "🎯", cefr = "A2", minutes = 3,
            rule = "Описательные: alto (высокий), bajo (низкий), simpático (симпатичный), antipático, listo (умный), guapo (красивый), feo (уродливый), joven (молодой), viejo (старый). На -o согласуются.",
            takeaways = listOf("На -o → -a в ж", "joven, joven (не меняется по роду)", "После сущ: la chica simpática")),

        t("u6_l8", "Местоимения OD", "lo, la, los, las — его/её/их",
            emoji = "👆", cefr = "A2", minutes = 4,
            rule = "Прямое дополнение (OD) — что/кого. Заменяет сущ: lo (его, м.ед), la (её, ж.ед), los (их, м.мн), las (их, ж.мн). Ставится ПЕРЕД глаголом: «Lo veo». С инфинитивом — может крепиться: «Quiero verlo».",
            tableHeaders = listOf("Что заменяем", "OD"),
            tableRows = listOf(listOf("м.ед (el libro)", "lo"), listOf("ж.ед (la casa)", "la"),
                listOf("м.мн (los libros)", "los"), listOf("ж.мн (las casas)", "las")),
            takeaways = listOf("Перед глаголом", "С инф крепится: verlo", "Согласуется с заменяемым сущ")),

        t("u6_l9", "Местоимения OI", "me, te, le, nos, os, les — мне/тебе/ему",
            emoji = "👆", cefr = "A2", minutes = 4,
            rule = "Косвенное дополнение (OI) — кому/чему. me (мне), te (тебе), le (ему/ей/Вам), nos (нам), os (вам Исп), les (им/Вам мн). Ставится ПЕРЕД глаголом.",
            tableHeaders = listOf("Лицо", "OI"),
            tableRows = listOf(listOf("я", "me"), listOf("ты", "te"), listOf("он/она/Вы", "le"),
                listOf("мы", "nos"), listOf("вы Исп", "os"), listOf("они/Вы мн", "les")),
            takeaways = listOf("le для él/ella/usted", "les для ellos/ustedes", "Можно усилить: a él le digo")),

        t("u6_l9_5", "🆕 Двойные местоимения OD+OI", "Te lo doy. le+lo → SE LO!",
            emoji = "🆕", cefr = "A2", minutes = 4,
            rule = "Когда оба местоимения вместе: OI идёт ПЕРЕД OD. me lo, te la, nos los. ВАЖНОЕ ПРАВИЛО: если le/les + lo/la/los/las — le/les МЕНЯЕТСЯ на SE (избегаем lele): se lo dije (= «ему/ей это сказал»).",
            tableHeaders = listOf("Было", "Стало"),
            tableRows = listOf(listOf("le + lo", "se lo"), listOf("le + la", "se la"),
                listOf("les + lo", "se lo"), listOf("les + las", "se las")),
            takeaways = listOf("OI впереди OD", "le/les + l- → se + l-", "Никогда «le lo» — только «se lo»")),

        t("u6_l10", "Hace + tiempo + que", "Hace dos años que vivo aquí — живу 2 года",
            emoji = "⏱", cefr = "A2", minutes = 3,
            rule = "Конструкция «делаю уже X времени»: Hace + промежуток + que + Presente. Действие началось в прошлом и продолжается. Альтернатива: Llevo + время + gerundio (Llevo dos años viviendo).",
            examples = listOf(
                Triple("Hace 2 años que vivo aquí.", "Живу здесь 2 года.", ""),
                Triple("Hace mucho tiempo que estudio.", "Учусь долго.", ""),
            ),
            takeaways = listOf("Hace + время + que + Pres", "Действие продолжается", "Альт: Llevo + время + ger")),

        t("u6_l11", "Одежда и мода", "talla, probarse, queda bien",
            emoji = "👗", cefr = "A2", minutes = 3,
            rule = "Шопинг-фразы: ¿Qué talla? (размер), Quiero probármelo (примерить), Me queda bien/mal (хорошо/плохо сидит), Es ajustado (облегающий), Es ancho (широкий). probarse — возвратный.",
            takeaways = listOf("talla = размер", "probarse — возвратный", "Me queda + bien/mal")),

        t("u6_l12", "Por vs Para — продвинутый", "por la mañana, para siempre, por mí",
            emoji = "📝", cefr = "A2", minutes = 4,
            rule = "Сложные случаи: por la mañana/tarde/noche (период дня), por teléfono/email (способ), para siempre (навсегда), para mí/ti (для меня/тебя), por mí/ti (за меня/тебя — кто-то делает вместо).",
            takeaways = listOf("por — период/способ/за", "para — для/срок/цель", "Запоминать устойчивыми")),

        t("u6_l13", "Эмоции", "alegría, tristeza, miedo, sorpresa",
            emoji = "😊", cefr = "A2", minutes = 3,
            rule = "Эмоции (сущ): alegría, tristeza, miedo, sorpresa, enfado. Прилагательные: contento (доволен), triste (грустный), asustado (испуган), sorprendido, enfadado. С эмоциями — ESTAR (временное состояние).",
            warning = "Эмоции → ESTAR (Estoy triste). НЕ SER!",
            takeaways = listOf("Эмоции — временно → estar", "Сущ→adj: alegría→contento", "miedo: «tengo miedo» = боюсь")),

        t("u6_l14", "Чекпоинт детства", "Imperfecto + сравнения + OD/OI + эмоции",
            emoji = "🏁", cefr = "A2", minutes = 3,
            rule = "Финал блока 2.2: рассказ о детстве с использованием Imperfecto, сравнений, местоимений и эмоций.",
            takeaways = listOf("Все темы блока", "Готов к Perfecto", "Большой устный объём")),

        // ═══════════════════════════════════════════════════════════════
        //  A2 · БЛОК 2.3 «СЕЙЧАС И СКОРО» — Perfecto + Imperativo + ger (16)
        // ═══════════════════════════════════════════════════════════════
        t("u7_l0", "Pretérito Perfecto", "haber + part. he comido — «уже поел сегодня»",
            emoji = "✅", cefr = "A2", minutes = 4,
            rule = "Pretérito Perfecto = haber в Presente + participio. Для действий с СВЯЗЬЮ С НАСТОЯЩИМ: hoy, esta semana, este año, ya, todavía no.",
            tableHeaders = listOf("Лицо", "haber", "+ part"),
            tableRows = listOf(listOf("yo", "he", "comido"), listOf("tú", "has", "vivido"),
                listOf("él", "ha", "hablado"), listOf("nos", "hemos", "trabajado"),
                listOf("ellos", "han", "escrito")),
            takeaways = listOf("haber + part", "Связь с настоящим", "hoy/esta semana → Perfecto")),

        t("u7_l1", "Нерегулярные participio", "hecho, dicho, visto, escrito, abierto, vuelto",
            emoji = "📖", cefr = "A2", minutes = 3,
            rule = "Главные нерегулярные participios: hacer→hecho, decir→dicho, ver→visto, escribir→escrito, abrir→abierto, poner→puesto, volver→vuelto, romper→roto, morir→muerto, descubrir→descubierto.",
            takeaways = listOf("Учить наизусть", "Часто заканчиваются на -to", "В Pasiva те же формы")),

        t("u7_l2", "Perfecto vs Indefinido", "hoy/esta = Perfecto. ayer/en 2020 = Indef",
            emoji = "🔀", cefr = "A2", minutes = 4,
            rule = "Маркеры Perfecto: hoy, esta semana/mañana/tarde, este año, ya, todavía no, nunca, alguna vez. Маркеры Indefinido: ayer, anoche, la semana pasada, el lunes, en 2020, hace 5 años.",
            warning = "В Латам Perfecto используется реже. В Испании — чаще. Для уровня A2 учим испанский вариант.",
            takeaways = listOf("hoy/esta → Perfecto", "ayer/2020 → Indef", "ya/todavía → Perfecto")),

        t("u7_l3", "ya / todavía / aún", "уже / ещё / до сих пор",
            emoji = "📌", cefr = "A2", minutes = 3,
            rule = "ya = уже (положительно: «Ya he comido»). todavía no / aún no = ещё не. todavía / aún = до сих пор/ещё (продолжается). nunca = никогда. alguna vez = когда-нибудь (для опыта).",
            takeaways = listOf("ya — уже", "todavía no — ещё не", "alguna vez? = «когда-нибудь?»")),

        t("u7_l4", "Estar + gerundio", "estoy comiendo — делаю прямо сейчас",
            emoji = "🔄", cefr = "A2", minutes = 4,
            rule = "Estar + gerundio = действие В МОМЕНТ речи. Образование gerundio: -AR → -ando (hablando), -ER/-IR → -iendo (comiendo, viviendo).",
            tableHeaders = listOf("Глагол", "Gerundio"),
            tableRows = listOf(listOf("hablar", "hablando"), listOf("comer", "comiendo"),
                listOf("vivir", "viviendo"), listOf("ver", "viendo (нерег)"),
                listOf("decir", "diciendo (e→i)")),
            takeaways = listOf("estar + ger", "-ando / -iendo", "Только «прямо сейчас»")),

        t("u7_l5", "Seguir/Llevar + gerundio", "Sigo trabajando. Llevo 2 horas estudiando.",
            emoji = "🔄", cefr = "A2", minutes = 3,
            rule = "Seguir + gerundio = продолжать делать. Llevar + время + gerundio = делать уже X времени.",
            examples = listOf(
                Triple("Sigo trabajando aquí.", "Продолжаю работать.", ""),
                Triple("Llevo 5 años estudiando.", "Учусь уже 5 лет.", ""),
            ),
            takeaways = listOf("seguir + ger = продолжаю", "llevar + время + ger = уже X времени", "Альт hace+que: Hace 5 años que estudio")),

        t("u7_l5_5", "🆕 Императив нерегулярный (tú)", "di! haz! pon! sal! ven! ten! ve! sé!",
            emoji = "🆕", cefr = "A2", minutes = 4,
            rule = "8 неправильных Imperativo для tú: decir→di, hacer→haz, poner→pon, salir→sal, venir→ven, tener→ten, ir→ve, ser→sé. Эти формы НЕ совпадают с Pres.",
            tableHeaders = listOf("Глагол", "Imperat. tú"),
            tableRows = listOf(listOf("decir", "di"), listOf("hacer", "haz"), listOf("poner", "pon"),
                listOf("salir", "sal"), listOf("venir", "ven"), listOf("tener", "ten"),
                listOf("ir", "ve"), listOf("ser", "sé")),
            takeaways = listOf("8 особых форм", "Все короткие", "С местоим: dímelo, hazlo")),

        t("u7_l6", "Работа", "buscar empleo, currículum, entrevista, sueldo",
            emoji = "💼", cefr = "A2", minutes = 3,
            rule = "Лексика работы: buscar empleo (искать), el currículum (CV), la entrevista (собеседование), el contrato, el sueldo (зарплата), el jefe, la empresa, a tiempo completo (полный день), media jornada (полставки).",
            takeaways = listOf("CV = currículum", "entrevista = собес", "tiempo completo / media jornada")),

        t("u7_l7", "Imperativo + (tú)", "¡habla! ¡come! ¡escribe! Окончания -a/-e/-e",
            emoji = "📢", cefr = "A2", minutes = 3,
            rule = "Утвердительный Imperativo для tú: -AR → -a (¡habla!), -ER → -e (¡come!), -IR → -e (¡escribe!). Совпадает с 3 лицом ед.ч. Pres. Местоимения крепятся: ¡dímelo!",
            takeaways = listOf("-AR → -a", "-ER/-IR → -e", "Местоим крепятся в утв.")),

        t("u7_l8", "Imperativo - (tú)", "¡no hables! ¡no comas! Используется Subjuntivo!",
            emoji = "🚫", cefr = "A2", minutes = 4,
            rule = "Отрицательный Imperativo для tú = NO + Presente Subjuntivo (для tú). -AR → no -es, -ER/-IR → no -as. Местоимения СВОБОДНО (не крепятся): ¡no me lo digas!",
            tableHeaders = listOf("Глагол", "+ Imperat", "- Imperat"),
            tableRows = listOf(listOf("hablar", "habla", "no hables"),
                listOf("comer", "come", "no comas"), listOf("vivir", "vive", "no vivas"),
                listOf("hacer", "haz", "no hagas")),
            warning = "Отриц. Imperativo = Subjuntivo, а не -es/-as от Presente. ¡no comes! — НЕВЕРНО.",
            takeaways = listOf("no + Subj форма", "Местоим перед глаголом", "+ и - формы РАЗНЫЕ")),

        t("u7_l9", "У врача", "síntomas, dolor, receta, fiebre, tos",
            emoji = "🏥", cefr = "A2", minutes = 3,
            rule = "Лексика у врача: los síntomas, el dolor, la receta, la fiebre, la tos, estoy resfriado/a, me duele + часть тела, tengo fiebre/tos.",
            takeaways = listOf("Me duele + la cabeza", "Tengo fiebre", "estar resfriado")),

        t("u7_l10", "OD + OI вместе (повтор)", "te lo doy, se lo dije",
            emoji = "🔗", cefr = "A2", minutes = 3,
            rule = "Повторение блока 2.2 о двойных местоимениях. OI идёт ПЕРЕД OD. le/les + l- → se + l-.",
            takeaways = listOf("OI впереди OD", "le/les → se перед l-", "С инф крепится: dártelo")),

        t("u7_l11", "Путешествие", "hotel, billete, reserva, vuelo, equipaje",
            emoji = "✈", cefr = "A2", minutes = 3,
            rule = "В путешествии: el hotel, la habitación, la reserva, el billete (билет/купюра), el vuelo (рейс), el equipaje (багаж), el pasaporte, el aeropuerto, hacer la maleta (паковать чемодан).",
            takeaways = listOf("billete — и билет и купюра", "equipaje — багаж", "hacer la maleta")),

        t("u7_l12", "Придаточные с QUE", "Creo que es verdad. После creo/sé/pienso → Indic",
            emoji = "🔗", cefr = "A2", minutes = 3,
            rule = "Положительные глаголы мнения (creo que, pienso que, sé que, veo que, es verdad que) → Indicativo. Отрицательные (no creo que, no pienso que, dudo que) → Subjuntivo.",
            warning = "que (без тильды) = союз. qué (с тильдой) = вопрос. Разные слова!",
            takeaways = listOf("Положит → Indic", "Отриц → Subj (B1)", "que ≠ qué")),

        t("u7_l13", "Гастрономия Испании", "tapas, paella, tortilla española, jamón, churros",
            emoji = "🍽", cefr = "A2", minutes = 3,
            rule = "Знаменитые испанские блюда: tapas (закуски), paella (рис с морепродуктами), tortilla española (омлет с картошкой), gazpacho (холодный суп), jamón ibérico, churros, sangría.",
            takeaways = listOf("tapas — must-try", "tortilla española ≠ tortilla мексик", "Сангрия — фруктовое вино")),

        t("u7_l14", "Чекпоинт «Мой обычный день»", "Perfecto + Imperat + estar+ger",
            emoji = "🏁", cefr = "A2", minutes = 3,
            rule = "Финал блока 2.3: рассказ о дне с применением всех времён и конструкций.",
            takeaways = listOf("Все темы блока", "Готов к Futuro", "Сложный устный объём")),

        // ═══════════════════════════════════════════════════════════════
        //  A2 · БЛОК 2.4 «МЕЧТЫ И ПЛАНЫ» — Futuro + Cond + Si (15)
        // ═══════════════════════════════════════════════════════════════
        t("u8_l0", "Futuro Simple regular", "hablar + é/ás/á/emos/éis/án",
            emoji = "🔮", cefr = "A2", minutes = 3,
            rule = "Futuro Simple = весь ИНФИНИТИВ + окончание (-é/-ás/-á/-emos/-éis/-án). Одинаково для AR/ER/IR! Тильды на yo/él/nos.",
            tableHeaders = listOf("Лицо", "hablar", "comer"),
            tableRows = listOf(listOf("yo", "hablaré", "comeré"), listOf("tú", "hablarás", "comerás"),
                listOf("él", "hablará", "comerá"), listOf("nos", "hablaremos", "comeremos"),
                listOf("ellos", "hablarán", "comerán")),
            takeaways = listOf("Инфинитив + окончание", "AR/ER/IR одинаково!", "Тильды важны")),

        t("u8_l1", "Futuro irregular", "tendré, haré, vendré, iré — основа меняется",
            emoji = "🔮", cefr = "A2", minutes = 4,
            rule = "Нерегулярные Futuro — меняется ОСНОВА (окончания те же). 3 группы: 1) -dr- (tener→tendr-, salir→saldr-, poner→pondr-, venir→vendr-, valer→valdr-); 2) -r- (querer→querr-, poder→podr-, saber→sabr-, haber→habr-); 3) сокращ. (hacer→har-, decir→dir-).",
            tableHeaders = listOf("Глагол", "Основа", "yo"),
            tableRows = listOf(listOf("tener", "tendr-", "tendré"), listOf("hacer", "har-", "haré"),
                listOf("decir", "dir-", "diré"), listOf("venir", "vendr-", "vendré"),
                listOf("poder", "podr-", "podré")),
            takeaways = listOf("Те же окончания", "Меняется основа", "Те же что и в Cond")),

        t("u8_l2", "Condicional Simple", "hablaría — «бы». Инфинитив + ía/ías/...",
            emoji = "💭", cefr = "A2", minutes = 3,
            rule = "Condicional = «бы». Образование: ИНФИНИТИВ + -ía/-ías/-ía/-íamos/-íais/-ían. Одинаково для AR/ER/IR. Тильды ВЕЗДЕ на í.",
            tableHeaders = listOf("Лицо", "hablar"),
            tableRows = listOf(listOf("yo", "hablaría"), listOf("tú", "hablarías"),
                listOf("él", "hablaría"), listOf("nos", "hablaríamos"), listOf("ellos", "hablarían")),
            takeaways = listOf("Инф + -ía", "AR/ER/IR одинаково", "Тильды на всех -í")),

        t("u8_l3", "Condicional irregular", "tendría, haría, podría — те же основы что Futuro",
            emoji = "💭", cefr = "A2", minutes = 3,
            rule = "Нерегулярные Cond — те же основы что и в Futuro. tendría, haría, diría, vendría, podría, querría, sabría, habría.",
            takeaways = listOf("Те же основы что Futuro", "Окончания -ía", "Cond вежл: ¿Podrías?")),

        t("u8_l4", "Si + Pres + Futuro", "Si llueve, no iré — РЕАЛЬНОЕ условие (тип 1)",
            emoji = "🔀", cefr = "A2", minutes = 3,
            rule = "Условные предложения тип 1 (РЕАЛЬНЫЕ): Si + Presente + Futuro/Imperativo. После Si — НИКОГДА Futuro и НИКОГДА Cond.",
            warning = "После Si НЕ ставится Futuro! Si llueve (НЕ Si lloverá).",
            takeaways = listOf("Si + Pres → Fut", "После Si — Pres", "Тип 1 = реально может произойти")),

        t("u8_l5", "Планы и мечты", "quisiera, me gustaría, espero, voy a",
            emoji = "✨", cefr = "A2", minutes = 3,
            rule = "Конструкции мечт и планов: Quisiera (хотел бы — формал.), Me gustaría + inf (хотел бы), Espero + inf (надеюсь), Pienso + inf (планирую), Voy a + inf (собираюсь).",
            takeaways = listOf("Me gustaría — вежл.", "ir a + inf = собираюсь", "querer/esperar + inf")),

        t("u8_l6", "Indefinidos", "algo, alguien, nada, nadie, alguno, ningún",
            emoji = "❓", cefr = "A2", minutes = 3,
            rule = "Неопределённые: algo (что-то) ↔ nada (ничего). alguien (кто-то) ↔ nadie (никто). alguno/algún ↔ ningún. todo (всё), todos (все). Двойное отрицание: No tengo nada.",
            takeaways = listOf("algo/alguien — положит", "nada/nadie — отриц", "Двойное отриц обязательно")),

        t("u8_l7", "Вероятность", "probablemente, quizás, a lo mejor, tal vez",
            emoji = "🎲", cefr = "A2", minutes = 3,
            rule = "Уверенность: probablemente (вероятно), seguro que (точно). Сомнение: quizás/quizá/tal vez (может быть, обычно + Subj в B1+), a lo mejor (может — особый: ВСЕГДА Indicativo!).",
            warning = "a lo mejor + Indicativo (исключение). quizás/tal vez чаще + Subj.",
            takeaways = listOf("a lo mejor + Indic", "quizás/tal vez + Subj", "probablemente — нейтр")),

        t("u8_l8", "Авто", "alquilar, conducir, aparcar, atasco, gasolina",
            emoji = "🚗", cefr = "A2", minutes = 3,
            rule = "Лексика авто: alquilar un coche (арендовать), conducir (водить), aparcar (парковать), la gasolina (бензин), la autopista (магистраль), el atasco (пробка), el carnet de conducir (права).",
            takeaways = listOf("conducir — yo conduzco", "atasco = пробка", "carnet de conducir")),

        t("u8_l9", "Глаголы с предлогом", "pensar EN, soñar CON, casarse CON",
            emoji = "🔗", cefr = "A2", minutes = 3,
            rule = "Глаголы с фиксированным предлогом: pensar EN (думать о), soñar CON (мечтать о), enamorarse DE (влюбиться), casarse CON (жениться на), depender DE (зависеть от), preocuparse POR (волноваться о).",
            takeaways = listOf("pensar EN", "soñar CON", "Учить с глаголом")),

        t("u8_l10", "Природа", "campo, mar, montaña, bosque, lago, playa",
            emoji = "🌿", cefr = "A2", minutes = 2,
            rule = "Природа: el campo, el mar, la montaña, el bosque, el lago, el río, la playa, el cielo, las estrellas, la luna, el sol.",
            takeaways = listOf("el mar (м!)", "la montaña, la playa", "Природа — повседнев")),

        t("u8_l11", "Cuantificadores", "mucho, poco, bastante, demasiado, todo",
            emoji = "📏", cefr = "A2", minutes = 3,
            rule = "Количественные: mucho (много), poco (мало), bastante (достаточно), demasiado (слишком), todo (весь). Согласуются по роду и числу как обычные прилагательные. С глаголом — не меняются (наречие): Trabajo mucho.",
            warning = "С сущ → согласуется (mucha gente). С глаголом → не меняется (Trabajo mucho).",
            takeaways = listOf("С сущ согласуется", "С глаголом неизм", "demasiado = слишком")),

        t("u8_l12", "Технологии", "app, wifi, contraseña, descargar, móvil",
            emoji = "📱", cefr = "A2", minutes = 3,
            rule = "Современная лексика: la app/aplicación, el wifi, la contraseña (пароль), descargar (загружать), subir (выгружать), el correo electrónico, el ordenador (Исп) / el computador (Латам), el móvil / el celular.",
            takeaways = listOf("Регионы: ordenador/computador", "móvil/celular", "wifi — заимств")),

        t("u8_l13", "Спорт", "hacer ejercicio, correr, gimnasio, dieta",
            emoji = "💪", cefr = "A2", minutes = 2,
            rule = "Спорт: hacer ejercicio/deporte, correr (бегать), el gimnasio, llevar dieta, estar en forma, el yoga, la natación, perder peso (худеть).",
            takeaways = listOf("hacer + ejercicio/deporte", "ir al gimnasio", "estar en forma")),

        t("u8_l14", "Финал A2", "Futuro + Cond + Si + планы",
            emoji = "🏆", cefr = "A2", minutes = 3,
            rule = "Финальный чекпоинт A2: «Планирование путешествия». Все темы модуля.",
            takeaways = listOf("Завершение A2!", "190 уроков пройдено", "Готов к B1 (Subjuntivo)")),

        // ═══════════════════════════════════════════════════════════════
        //  B1 · БЛОК 3.1 «SUBJUNTIVO» — 16 теорий
        // ═══════════════════════════════════════════════════════════════
        t("u9_l0", "Subjuntivo — что это", "Наклонение НЕРЕАЛЬНОСТИ: желание, эмоция, сомнение, цель",
            emoji = "🔮", cefr = "B1", minutes = 4,
            rule = "Subjuntivo (сослагательное) — для НЕРЕАЛЬНОГО: желания, эмоции, сомнения, цели, рекомендации. Используется в придаточных после QUE.",
            tableHeaders = listOf("Триггер", "Пример"),
            tableRows = listOf(listOf("Желание", "Quiero que vengas"),
                listOf("Эмоция", "Me alegra que estés"),
                listOf("Сомнение", "Dudo que sea"),
                listOf("Цель", "Para que sepas"),
                listOf("Невыполнимое", "Ojalá llueva")),
            takeaways = listOf("Реальность → Indic", "Нереальность → Subj", "После QUE с триггером")),

        t("u9_l1", "Pres.Subj. -AR", "hable/hables/hable/hablemos/habléis/hablen",
            emoji = "🔮", cefr = "B1", minutes = 3,
            rule = "Pres.Subj. -AR: -e/-es/-e/-emos/-éis/-en. ИНВЕРТИРОВАНЫ относительно Pres.Indic. (там было -o/-as/-a).",
            tableHeaders = listOf("Лицо", "Indic", "Subj"),
            tableRows = listOf(listOf("yo", "hablo", "hable"), listOf("tú", "hablas", "hables"),
                listOf("él", "habla", "hable"), listOf("nos", "hablamos", "hablemos"),
                listOf("ellos", "hablan", "hablen")),
            takeaways = listOf("-AR → -e окончания", "Инверсия Indic", "Применимо ко всем -AR")),

        t("u9_l2", "Pres.Subj. -ER/-IR", "coma/comas/coma. viva/vivas/viva. -a окончания.",
            emoji = "🔮", cefr = "B1", minutes = 3,
            rule = "Pres.Subj. -ER/-IR: -a/-as/-a/-amos/-áis/-an. -ER и -IR одинаково. Снова инверсия (там было -e).",
            tableHeaders = listOf("Лицо", "comer", "vivir"),
            tableRows = listOf(listOf("yo", "coma", "viva"), listOf("tú", "comas", "vivas"),
                listOf("él", "coma", "viva"), listOf("nos", "comamos", "vivamos"),
                listOf("ellos", "coman", "vivan")),
            takeaways = listOf("-ER/-IR → -a окончания", "Одинаково для обеих", "От 1.л Indic + инверсия")),

        t("u9_l3", "Subj irregular", "ser→sea, ir→vaya, estar→esté, dar→dé, saber→sepa, haber→haya",
            emoji = "🔮", cefr = "B1", minutes = 4,
            rule = "Главные нерегулярные Subj: ser→sea, ir→vaya, estar→esté, dar→dé, saber→sepa, haber→haya, ver→vea.",
            tableHeaders = listOf("Глагол", "Subj yo"),
            tableRows = listOf(listOf("ser", "sea"), listOf("ir", "vaya"), listOf("estar", "esté"),
                listOf("dar", "dé"), listOf("saber", "sepa"), listOf("haber", "haya"),
                listOf("ver", "vea")),
            takeaways = listOf("Учить наизусть", "Все 6 форм по парадигме", "haya — для Pres.Perf.Subj.")),

        t("u9_l4", "Subj отклоняющиеся", "quiera (e→ie), pueda (o→ue) + venga (e→i+g)",
            emoji = "🔮", cefr = "B1", minutes = 3,
            rule = "Отклоняющиеся в Subj работают КАК В PRES но в nosotros/vosotros отклонение ТОЖЕ есть! Querer Subj: quiera/quieras/quiera/queramos/queráis/quieran (вот тут queramos без ie, как в Indic). poder Subj: pueda/puedas/pueda/podamos/podáis/puedan.",
            takeaways = listOf("Те же отклонения", "В nos/vos обычно как Pres", "venga — нерег в 1.л → во всех")),

        t("u9_l5", "Querer que + Subj", "Хочу чтобы. Если 2 субъекта — que+Subj. Если 1 — инф.",
            emoji = "💭", cefr = "B1", minutes = 4,
            rule = "querer + инф (1 субъект): Quiero ir. querer + que + Subj (2 субъекта): Quiero que vayas (хочу чтобы ты).",
            warning = "ВАЖНО: 1 субъект → инфинитив; 2 субъекта → que + Subj. «Quiero que yo vaya» — НЕВЕРНО!",
            takeaways = listOf("1 субъект — инф", "2 субъекта — que+Subj", "Желание → Subj")),

        t("u9_l6", "Esperar/Necesitar/Pedir + que",
            "Все триггеры волеизъявления → Subj",
            emoji = "💭", cefr = "B1", minutes = 3,
            rule = "Триггеры волеизъявления + que + Subj: esperar (надеяться), necesitar (нуждаться), pedir (просить), mandar (велеть), aconsejar (советовать), permitir, prohibir.",
            takeaways = listOf("Все волеизъявит → Subj", "Тоже 1 vs 2 субъекта", "Espero ir vs Espero que vengas")),

        t("u9_l7", "Безличные триггеры", "Es importante/necesario/posible/bueno que + Subj",
            emoji = "💡", cefr = "B1", minutes = 3,
            rule = "Безличные конструкции с QUE: Es importante/necesario/bueno/mejor/posible/imposible que + Subj.",
            warning = "Es verdad/cierto/seguro que + Indic (констатация факта). Все остальные «es ... que» → Subj.",
            takeaways = listOf("Es + adj + que → Subj", "Es verdad que → Indic (искл)", "Часто слышатся в речи")),

        t("u9_l8", "Эмоции + que → Subj", "Me alegra/Temo/Siento + que",
            emoji = "😊", cefr = "B1", minutes = 3,
            rule = "Глаголы эмоции + que: Me alegra (рад), Me gusta, Temo (боюсь), Siento (сожалею), Me sorprende (удивляет), Me molesta. ВСЕ → Subj.",
            takeaways = listOf("Эмоция + que → Subj", "Me alegra que estés", "Tonkost: Me alegro DE que (вариант)")),

        t("u9_l9", "Сомнение → Subj. Уверенность → Indic",
            "No creo que / Dudo que → Subj. Creo que → Indic.",
            emoji = "🤔", cefr = "B1", minutes = 4,
            rule = "Положительные creo/pienso/sé que → INDIC (уверенность). Отрицательные no creo/no pienso/dudo que → SUBJ (сомнение).",
            tableHeaders = listOf("Триггер", "Время"),
            tableRows = listOf(listOf("Creo que viene", "Indic"), listOf("No creo que venga", "Subj"),
                listOf("Sé que es", "Indic"), listOf("Dudo que sea", "Subj")),
            takeaways = listOf("Положит → Indic", "Отриц → Subj", "Логика: уверен vs сомневаюсь")),

        t("u9_l10", "Ojalá + Subj", "Дай Бог чтобы. Самый эмоциональный триггер.",
            emoji = "🌟", cefr = "B1", minutes = 3,
            rule = "Ojalá (от арабского «иншаллах») = «Дай Бог», «Вот бы». ВСЕГДА + Subj. Без que: Ojalá venga (НЕ «Ojalá que venga» — допустимо, но реже).",
            takeaways = listOf("Ojalá ВСЕГДА + Subj", "Без que чаще", "Pres.Subj — выполнимо, Imp.Subj — невыполнимо")),

        t("u9_l11", "Para que + Subj", "Чтобы (цель). Если 1 субъект — para + inf.",
            emoji = "🎯", cefr = "B1", minutes = 3,
            rule = "para que + Subj — цель в придаточном (2 субъекта). para + инф — цель если 1 субъект. a fin de que — формальнее.",
            examples = listOf(
                Triple("Trabajo para vivir.", "Работаю чтобы жить.", "1 субъект — para + inf"),
                Triple("Te lo digo para que sepas.", "Говорю чтоб знал.", "2 субъекта"),
            ),
            takeaways = listOf("para que + Subj (2 суб)", "para + inf (1 суб)", "Аналогия с querer/que")),

        t("u9_l11_5", "🆕 Antes de que + Subj",
            "Прежде чем (2 субъекта). antes de + inf (1 субъект).",
            emoji = "🆕", cefr = "B1", minutes = 3,
            rule = "antes de que + Subj — прежде чем кто-то. antes de + inf — прежде чем самому.",
            examples = listOf(
                Triple("Llámame antes de salir.", "Позвони перед выходом.", "1 субъект"),
                Triple("Llámame antes de que salgas.", "Позвони прежде чем уйти.", "2 субъекта"),
            ),
            takeaways = listOf("Время-будущее + 2 суб → Subj", "1 суб → de + inf", "Аналогия с para que")),

        t("u9_l12", "Cuando + Subj (для будущего)",
            "Cuando vengas... — будущее → Subj",
            emoji = "⏰", cefr = "B1", minutes = 4,
            rule = "Cuando + Subj — для БУДУЩИХ событий. Cuando + Indic — для прошлого/привычки. Тоже: en cuanto, hasta que, mientras, después de que.",
            tableHeaders = listOf("Контекст", "Время"),
            tableRows = listOf(listOf("Прошлое", "Cuando llegué (Indic)"),
                listOf("Привычка", "Cuando viene siempre (Indic)"),
                listOf("Будущее", "Cuando vengas mañana (Subj)")),
            takeaways = listOf("Прошлое/привычка → Indic", "Будущее → Subj", "Тоже en cuanto, hasta que")),

        t("u9_l13", "Aunque (Indic vs Subj)", "Indic = факт. Subj = гипотеза/будущее.",
            emoji = "📝", cefr = "B1", minutes = 3,
            rule = "Aunque + Indic = «хотя» (факт известен). Aunque + Subj = «даже если» (гипотеза или будущее).",
            examples = listOf(
                Triple("Aunque llueve, voy.", "Хотя дождь — иду (факт).", "Indic"),
                Triple("Aunque llueva, iré.", "Даже если будет дождь — пойду.", "Subj — гипотеза"),
            ),
            takeaways = listOf("Indic = факт", "Subj = гипот/будущее", "По смыслу выбираешь")),

        t("u9_l14", "Чекпоинт «Совет другу»", "Все триггеры Subj в одном",
            emoji = "🏁", cefr = "B1", minutes = 3,
            rule = "Финал блока 3.1: применяем все триггеры Subjuntivo в реальном диалоге.",
            takeaways = listOf("Все триггеры", "Готов к Cond+Si тип 2", "Большой шаг к B1")),

        // ═══════════════════════════════════════════════════════════════
        //  B1 · БЛОК 3.2 «CONDICIONAL» — 15 теорий
        // ═══════════════════════════════════════════════════════════════
        t("u10_l0", "Condicional — обзор", "«бы». Гипотеза, вежливость, совет.",
            emoji = "💫", cefr = "B1", minutes = 3,
            rule = "Cond используется для: 1) гипотезы (Yo lo haría), 2) вежливой просьбы (¿Podrías?), 3) совета (Yo en tu lugar iría), 4) воображения (Sería genial).",
            takeaways = listOf("«бы» во всех контекстах", "Вежливее Pres", "Часто пара с Si")),

        t("u10_l1", "Cond -AR", "hablaría — инфинитив + ía",
            emoji = "💫", cefr = "B1", minutes = 2,
            rule = "Cond -AR (как и -ER/-IR): инф + -ía/-ías/-ía/-íamos/-íais/-ían.",
            takeaways = listOf("Инф + -ía", "Тильда на í", "Все группы одинаково")),

        t("u10_l2", "Cond -ER/-IR", "Одинаковые окончания. comería, viviría",
            emoji = "💫", cefr = "B1", minutes = 2,
            rule = "Окончания Cond ОДИНАКОВЫ для AR/ER/IR: -ía/-ías/-ía/-íamos/-íais/-ían.",
            takeaways = listOf("-AR/-ER/-IR одинаково", "Запомни одну схему", "Применима ко всем правильным")),

        t("u10_l3", "Cond irreg 1: tener/poder/saber/haber",
            "Те же основы что Futuro + -ía",
            emoji = "⚡", cefr = "B1", minutes = 3,
            rule = "Cond нерегулярные = те же основы что в Futuro + окончания -ía. tendría, podría, sabría, habría.",
            takeaways = listOf("Те же основы что Futuro", "+ -ía окончания", "Учится связкой Fut/Cond")),

        t("u10_l4", "Cond irreg 2: hacer/querer/venir/salir",
            "haría, querría (с двумя rr!), vendría, saldría",
            emoji = "⚡", cefr = "B1", minutes = 3,
            rule = "Ещё нерегулярные: hacer→haría, querer→querría (С ДВУМЯ rr!), venir→vendría, salir→saldría, decir→diría.",
            warning = "querría (Cond) ≠ quería (Imp Indic). querría = «хотел БЫ», quería = «хотел вчера».",
            takeaways = listOf("querría с rr — Cond", "quería с одной — Imp", "Контекст подскажет")),

        t("u10_l5", "Si тип 1 (Реальное)", "Si + Pres + Fut. Si llueve, no iré.",
            emoji = "🔀", cefr = "B1", minutes = 3,
            rule = "Si тип 1 = РЕАЛЬНОЕ условие в будущем. Si + Presente + Futuro/Imperat/Pres.",
            warning = "После Si НЕ Futuro и НЕ Cond! Только Pres.",
            takeaways = listOf("Реальное → тип 1", "Si + Pres + Fut", "Никогда Si lloverá")),

        t("u10_l6", "Imperfecto Subjuntivo — обзор", "-ra и -se формы. Для гипотез типа 2.",
            emoji = "📚", cefr = "B1", minutes = 4,
            rule = "Imp.Subj — для нереальных условий (Si тип 2: Si tuviera...). 2 формы: -ra (чаще, разг.) и -se (формальнее). Значение одинаковое.",
            takeaways = listOf("2 формы: -ra/-se", "Одно значение", "Для Si тип 2")),

        t("u10_l7", "Imp.Subj. regular -ra", "hablar→hablara/hablaras/hablara/habláramos",
            emoji = "📚", cefr = "B1", minutes = 4,
            rule = "Imp.Subj. -ra: основа = 3.л.мн.ч. Indef минус -ron + -ra/-ras/-ra/-´ramos/-rais/-ran. Тильда в nosotros! hablaron→hablar+a→hablara.",
            takeaways = listOf("От 3.л.мн.ч. Indef", "-aron→ar+a; -ieron→ier+a", "Тильда в nosotros")),

        t("u10_l8", "Imp.Subj. irreg", "fuera, tuviera, hiciera, dijera, pudiera",
            emoji = "📚", cefr = "B1", minutes = 4,
            rule = "Нерег. Imp.Subj — от ОСНОВЫ Indef irreg. tener (tuvieron) → tuvier- → tuviera. ser/ir (fueron) → fuer- → fuera. hacer (hicieron) → hiciera. decir (dijeron) → dijera. poder (pudieron) → pudiera.",
            takeaways = listOf("От 3.л.мн Indef", "Все нерег глаголы", "fuera = ser ИЛИ ir")),

        t("u10_l9", "Si тип 2 (Гипотеза)", "Si + Imp.Subj + Cond. Si tuviera, viajaría.",
            emoji = "🔀", cefr = "B1", minutes = 4,
            rule = "Si тип 2 = НЕРЕАЛЬНАЯ или маловероятная гипотеза. Si + Imp.Subj. + Cond. Если бы..., то...",
            warning = "После Si — НЕ Cond! Si tuviera (НЕ Si tendría).",
            takeaways = listOf("Гипотеза → тип 2", "Si + Imp.Subj. + Cond", "Si tuviera, viajaría")),

        t("u10_l10", "Советы", "Yo en tu lugar / Yo que tú / deberías",
            emoji = "💡", cefr = "B1", minutes = 3,
            rule = "Советы через Cond: Yo en tu lugar + Cond, Yo que tú + Cond, Te aconsejo que + Subj, Deberías + inf (тебе следовало бы).",
            takeaways = listOf("Совет → Cond", "deber Cond = «следовало бы»", "Te aconsejo que + Subj")),

        t("u10_l11", "Вежливые просьбы", "¿Podrías? ¿Te importaría? Me gustaría",
            emoji = "🙏", cefr = "B1", minutes = 3,
            rule = "Вежливые формулы Cond: ¿Podrías ayudar? (мог бы помочь?), ¿Te importaría + inf? (не возражаешь?), ¿Sería posible...? Me gustaría... (хотел бы).",
            takeaways = listOf("¿Podrías? — стандарт", "¿Te importaría? — деликатно", "Me gustaría — желание")),

        t("u10_l12", "Quizás vs A lo mejor", "Quizás+Subj (стандарт). A lo mejor+Indic (особый!)",
            emoji = "❓", cefr = "B1", minutes = 3,
            rule = "Quizás/Tal vez/Acaso + Subj (стандартное). A lo mejor — ОСОБЫЙ случай: ВСЕГДА + Indicativo (несмотря на смысл «может»).",
            warning = "a lo mejor — единственный «возможно» с Indic. Запомнить!",
            takeaways = listOf("Quizás/Tal vez + Subj", "A lo mejor + Indic", "Acaso — книжно")),

        t("u10_l13", "Me gustaría que + Imp.Subj", "Хотел бы чтобы. Согласование Cond+Imp.Subj.",
            emoji = "❤", cefr = "B1", minutes = 3,
            rule = "Когда главное в Cond, придаточное в Imp.Subj. (согласование времён): Me gustaría que vinieras (хотел бы чтоб ты пришёл).",
            takeaways = listOf("Cond → Imp.Subj. в que", "Согласование времён", "Если 1 субъект — me gustaría + inf")),

        t("u10_l14", "Чекпоинт «Если бы я...»", "Cond + Imp.Subj + Si тип 2",
            emoji = "🌟", cefr = "B1", minutes = 3,
            rule = "Финал блока 3.2: гипотезы и мечты в одном диалоге.",
            takeaways = listOf("Все темы блока", "Готов к косв.речи", "Большой раздел B1 пройден")),

        // ═══════════════════════════════════════════════════════════════
        //  B1 · БЛОК 3.3 «КОММУНИКАЦИЯ» — 16 теорий
        // ═══════════════════════════════════════════════════════════════
        t("u11_l0", "Estilo indirecto — введение", "Косвенная речь. Сдвиг времён.",
            emoji = "💬", cefr = "B1", minutes = 4,
            rule = "Косвенная речь — пересказ слов другого без прямой цитаты. Главное правило: ВРЕМЯ СДВИГАЕТСЯ В ПРОШЛОЕ. Pres → Imp, Indef → Pluscuamp, Fut → Cond, Pres.Subj → Imp.Subj.",
            takeaways = listOf("Сдвиг в прошлое", "После dijo que / preguntó si", "Местоимения тоже меняются")),

        t("u11_l1", "Dijo que / Preguntó si", "Для пересказа. Утвердит → que. Да/Нет → si.",
            emoji = "💬", cefr = "B1", minutes = 3,
            rule = "Dijo que + Imp/Pluscuamp/Cond — пересказ утвердительной фразы. Preguntó si + Imp/... — пересказ да/нет вопроса. Preguntó qué/cuándo/dónde — пересказ wh-вопроса.",
            takeaways = listOf("Dijo que — утв.", "Preguntó si — да/нет вопрос", "Preguntó qué — wh-вопрос")),

        t("u11_l2", "Сдвиг времён", "Pres→Imp, Indef→Pluscuamp, Fut→Cond, Subj→Imp.Subj",
            emoji = "💬", cefr = "B1", minutes = 4,
            rule = "Полная таблица сдвигов в косв.речи (если главное в прошлом).",
            tableHeaders = listOf("Прямая речь", "Косвенная"),
            tableRows = listOf(listOf("voy (Pres)", "iba (Imp)"), listOf("fui (Indef)", "había ido"),
                listOf("iré (Fut)", "iría (Cond)"), listOf("vaya (Subj)", "fuera (Imp.Subj.)"),
                listOf("¡ven! (Imperat)", "que viniera (Imp.Subj.)")),
            takeaways = listOf("Pres → Imp", "Indef → Pluscuamp", "Fut → Cond")),

        t("u11_l3", "Косвенные приказы", "Pidió/Mandó que + Imp.Subj",
            emoji = "💬", cefr = "B1", minutes = 3,
            rule = "Imperativo в косв.речи → que + Imp.Subj. ¡Ven! → Me dijo que viniera. ¡Estudia! → Me pidió que estudiara.",
            takeaways = listOf("Imperat → que + Imp.Subj.", "Pidió/Mandó/Sugirió que", "Применимо к советам/приказам")),

        t("u11_l4", "Relativos: que/quien/donde",
            "que (универс), quien (для людей после предлога), donde (место)",
            emoji = "🔗", cefr = "B1", minutes = 4,
            rule = "Относительные местоимения: que (что/который — универсальное), quien/quienes (кто/кого — для людей, особенно после предлога), donde (где — для места), cuando (когда — для времени).",
            examples = listOf(
                Triple("El libro que leo.", "Книга которую читаю.", ""),
                Triple("El amigo con quien hablo.", "Друг с которым говорю.", "после предлога"),
                Triple("La casa donde vivo.", "Дом где живу.", ""),
            ),
            takeaways = listOf("que — универс", "quien — после предлога для людей", "donde — место")),

        t("u11_l5", "cuyo / el cual / lo cual",
            "cuyo (чей) согласуется. el cual — формальное. lo cual — про всю ситуацию.",
            emoji = "🔗", cefr = "B1", minutes = 4,
            rule = "cuyo/cuya/cuyos/cuyas — «чей», согласуется с владеемым (НЕ с владельцем). el cual/la cual/los cuales/las cuales — формальное «который». lo cual — относится ко всей предыдущей мысли.",
            examples = listOf(
                Triple("La mujer cuya casa es grande.", "Женщина чей дом большой.", "cuya — ж согл с casa"),
                Triple("Llegó tarde, lo cual me molestó.", "Пришёл поздно, что меня раздражило.", "lo cual"),
            ),
            takeaways = listOf("cuyo согласуется с тем что у владельца", "el cual — формал", "lo cual — про всю ситуацию")),

        t("u11_l5_5", "🆕 Lo + adjetivo", "Lo bueno, lo importante, lo mejor — абстрактное.",
            emoji = "🆕", cefr = "B1", minutes = 3,
            rule = "Lo + adj = нейтральное/абстрактное «то, что (adj)». Lo bueno = «хорошее», lo importante = «важное», lo mejor = «лучшее». Отличается от el bueno (конкретный человек) и la buena.",
            takeaways = listOf("Lo + adj — абстрактное", "el bueno — конкретный м", "Lo mejor / Lo peor — устойчивые")),

        t("u11_l6", "Voz pasiva", "ser + part. La carta fue escrita.",
            emoji = "🎭", cefr = "B1", minutes = 4,
            rule = "Пассивный залог через SER: ser + participio (+ por + агент). Participio согласуется с подлежащим по роду и числу. Используется реже чем в английском.",
            examples = listOf(
                Triple("La carta fue escrita por María.", "Письмо написано Марией.", ""),
                Triple("Las casas son construidas.", "Дома строятся.", ""),
            ),
            takeaways = listOf("ser + part", "Согласование part по роду/числу", "Часто заменяется SE-пассивом")),

        t("u11_l7", "Ser vs Estar + participio",
            "Ser+part = действие. Estar+part = РЕЗУЛЬТАТ/состояние.",
            emoji = "🎭", cefr = "B1", minutes = 4,
            rule = "ser + part = пассивное действие (был сделан кем-то). estar + part = состояние (готов/в состоянии). La puerta fue cerrada (закрыли) vs La puerta está cerrada (закрыта сейчас).",
            takeaways = listOf("ser = действие", "estar = состояние", "Разные смыслы")),

        t("u11_l8", "Llevar + gerundio", "Делаю уже X времени",
            emoji = "⚙", cefr = "B1", minutes = 3,
            rule = "Llevar + время + gerundio — продолжающееся действие. Альтернатива «hace X que + Pres». Llevo 3 horas estudiando = Hace 3 horas que estudio.",
            takeaways = listOf("Llevar + время + ger", "Альт hace+que", "Действие продолжается")),

        t("u11_l9", "Seguir/Continuar + ger", "Продолжать делать",
            emoji = "⚙", cefr = "B1", minutes = 2,
            rule = "Seguir/Continuar + gerundio = «продолжать (делать)». Sigo trabajando. Continúa lloviendo.",
            takeaways = listOf("seguir + ger", "continuar + ger (книжнее)", "Никогда + инф")),

        t("u11_l10", "Acabar de / Volver a", "Только что / снова",
            emoji = "⚙", cefr = "B1", minutes = 3,
            rule = "acabar de + inf = «только что» (недавнее прошлое). volver a + inf = «снова». Acabo de comer (только что поел). Vuelvo a llamar (снова звоню).",
            takeaways = listOf("acabar de = только что", "volver a = снова", "Обе с инф")),

        t("u11_l11", "Конекторы — обзор", "sin embargo, por lo tanto, además",
            emoji = "📝", cefr = "B1", minutes = 3,
            rule = "Базовые конекторы: sin embargo (однако), por lo tanto/por eso (поэтому), además (кроме того), por otro lado (с другой стороны), en cambio (напротив), por ejemplo (например).",
            takeaways = listOf("Структурируют речь", "Делают её формальнее", "Учить парами")),

        t("u11_l12", "Концессия", "aunque / a pesar de (que) / pese a",
            emoji = "📝", cefr = "B1", minutes = 3,
            rule = "Концессия (несмотря на): aunque + Indic/Subj, a pesar de + сущ/инф, a pesar de que + глагол, pese a (формал).",
            warning = "a pesar de + сущ: «a pesar de la lluvia». a pesar de QUE + глагол: «a pesar de que llueve».",
            takeaways = listOf("aunque — универс", "a pesar de + сущ", "pese a — книжно")),

        t("u11_l13", "Заключение", "en definitiva, en resumen, es decir, por ejemplo",
            emoji = "📝", cefr = "B1", minutes = 2,
            rule = "Конекторы заключения: en definitiva (в итоге), en resumen (короче), es decir (то есть), o sea (другими словами), por ejemplo (например), en otras palabras.",
            takeaways = listOf("Подытоживают", "es decir = то есть", "В заключении эссе")),

        t("u11_l14", "Чекпоинт «Интервью»", "Косв.речь + относит + perífrasis",
            emoji = "🏁", cefr = "B1", minutes = 3,
            rule = "Финал блока 3.3: HR-собеседование с применением всех тем коммуникации.",
            takeaways = listOf("Все темы блока", "Готов к лексике+стилю", "Большой объём")),

        // ═══════════════════════════════════════════════════════════════
        //  B1 · БЛОК 3.4 «СЛОВАРЬ И СТИЛЬ» — 16 теорий
        // ═══════════════════════════════════════════════════════════════
        t("u12_l0", "Лексика работы B1", "contrato indefinido, plantilla, baja, finiquito",
            emoji = "💼", cefr = "B1", minutes = 3,
            rule = "Профессиональная лексика: contrato indefinido (бессроч), plantilla (штат), estar de baja (на больничном), el finiquito (выплата при увольнении), jubilarse (на пенсию), el sindicato.",
            takeaways = listOf("Слова из реальной работы", "Полезно при поиске работы", "Запомни с контекстом")),

        t("u12_l1", "Формальная переписка", "Estimado, Adjunto, Le agradezco, Atentamente",
            emoji = "✉", cefr = "B1", minutes = 3,
            rule = "Формальные клише: Estimado/a (уважаемый), Adjunto envío (прилагаю), Le agradezco (благодарю), Quedo a su disposición (остаюсь в Вашем распоряж.), Atentamente / Saludos cordiales.",
            takeaways = listOf("Estimado — обращение", "Atentamente — подпись", "Le (формал) vs te (нет)")),

        t("u12_l2", "Медиа", "noticia, reportaje, periódico, telediario",
            emoji = "📰", cefr = "B1", minutes = 2,
            rule = "Медиа: la noticia (новость), el reportaje (репортаж), el editorial (ред. статья), el periódico (газета), la prensa (пресса), el telediario (теленовости), el titular (заголовок).",
            takeaways = listOf("Перио́дико с тильдой", "Telediario — на ТВ", "Titular — заголовок")),

        t("u12_l3", "Соцсети", "publicar, comentar, seguir, dar like, viral",
            emoji = "📱", cefr = "B1", minutes = 2,
            rule = "Соцсети: publicar (опубл), comentar, seguir (подписаться), dar like, compartir (поделиться), el seguidor, el hashtag, viral.",
            takeaways = listOf("Англ заимств: like, hashtag", "seguidor = подписчик", "viral — заимств")),

        t("u12_l4", "Здоровье B1", "síntoma, diagnóstico, urgencias, ingreso",
            emoji = "🏥", cefr = "B1", minutes = 3,
            rule = "Расширенная лексика медицины: el síntoma, el diagnóstico, las urgencias (скорая), la consulta (приём), el ingreso (госпитализация), operar, la cirugía.",
            takeaways = listOf("Сложнее A1 здоровья", "Запомни с примерами", "Полезно для жизни в Испании")),

        t("u12_l5", "У врача (B1)", "Me siento mal, mareado, toser",
            emoji = "🏥", cefr = "B1", minutes = 3,
            rule = "Описание состояния: me siento mal/bien, estoy mareado/a (кружится голова), toser/toso (кашлять), tener fiebre alta, necesito reposo (отдых).",
            takeaways = listOf("estar mareado", "toser — yo toso", "fiebre alta")),

        t("u12_l6", "Идиомы DAR", "dar igual, dar miedo, darse cuenta, darse prisa",
            emoji = "🎭", cefr = "B1", minutes = 4,
            rule = "Идиомы с DAR: me da igual (мне всё равно), dar miedo (пугать), darse cuenta (понимать), dar la vuelta (развернуться), darse prisa (торопиться), dar a luz (рожать).",
            takeaways = listOf("Конструкция MEMI DA", "Возвратные: darse cuenta", "Идиоматичны — учить целиком")),

        t("u12_l7", "Идиомы TENER", "tener ganas, razón, prisa, suerte, miedo",
            emoji = "🎭", cefr = "B1", minutes = 3,
            rule = "Идиомы с TENER: tener ganas de + inf (хотеть), tener razón (быть правым), tener en cuenta (учитывать), tener prisa, tener suerte, tener miedo, tener hambre/sed.",
            warning = "С TENER, не SER! «Tengo razón», НЕ «Soy razón».",
            takeaways = listOf("Все с TENER", "tener + сущ (без артикля обычно)", "Аналог англ «to be»")),

        t("u12_l8", "Идиомы HACER", "hacer falta, caso, ilusión, hacerse + adj",
            emoji = "🎭", cefr = "B1", minutes = 3,
            rule = "Идиомы с HACER: me hace falta (мне нужен), hacer caso (слушаться), me hace ilusión (мне приятно), hacer una pregunta (задать вопрос), hacerse rico (стать богатым).",
            takeaways = listOf("hacer falta = быть нужным", "hacerse — превращение", "hacer caso a кому-то")),

        t("u12_l9", "Идиомы LLEVAR", "llevar a cabo, la contraria, llevarse bien",
            emoji = "🎭", cefr = "B1", minutes = 3,
            rule = "LLEVAR: llevar a cabo (осуществить), llevar la contraria (спорить), llevarse bien con (ладить), llevar tiempo (отнимать время), llevar una vida + adj (вести жизнь).",
            takeaways = listOf("Многозначные идиомы", "llevarse bien — возвратный", "llevar a cabo = осуществить")),

        t("u12_l9_5", "🆕 Идиомы PONER/PONERSE",
            "ponerse rojo, poner verde, ponerse al día",
            emoji = "🆕", cefr = "B1", minutes = 3,
            rule = "PONER/PONERSE идиомы: ponerse + adj (стать каким-то: ponerse rojo = покраснеть), poner verde a alguien (ругать), ponerse al día (наверстать), poner de manifiesto (показать — книжн).",
            takeaways = listOf("ponerse — изменение состояния", "poner verde — разговорное", "Учить с контекстом")),

        t("u12_l10", "Регистр formal vs coloquial",
            "usted/tú, vale/está bien, automóvil/coche",
            emoji = "✍", cefr = "B1", minutes = 4,
            rule = "Формальный регистр: usted, automóvil/vehículo, actualmente, no obstante, agradezco. Разговорный: tú, coche, ahora, pero, gracias. Регистр определяет ситуация (с кем/о чём).",
            takeaways = listOf("По ситуации", "В формальном письме — formal", "Vale — только в разговоре")),

        t("u12_l11", "Заявление", "Por la presente, Solicito, A la atención de",
            emoji = "✍", cefr = "B1", minutes = 3,
            rule = "Структура заявления: A la atención de... (вниманию), Por la presente solicito... (настоящим прошу), Adjunto encontrará... (во вложении), Quedo a su disposición, Atentamente.",
            takeaways = listOf("Очень формальные клише", "Solicito = подаю заявление", "Atentamente — подпись")),

        t("u12_l12", "Дебаты", "Estoy de acuerdo, No estoy de acuerdo, Depende",
            emoji = "🗣", cefr = "B1", minutes = 3,
            rule = "Согласие/несогласие: Estoy de acuerdo (с ESTAR, не SER!), No estoy de acuerdo, Tienes razón en parte, No estoy seguro, Depende (зависит).",
            warning = "ESTAR de acuerdo, НЕ SER de acuerdo!",
            takeaways = listOf("ESTAR de acuerdo", "depende — без agreement", "Смягчающие фразы")),

        t("u12_l13", "Аргументация", "Por un lado, Por otro, En conclusión",
            emoji = "🗣", cefr = "B1", minutes = 3,
            rule = "Аргументация эссе: Por un lado... Por otro lado... (с одной/другой стороны), En primer lugar / En segundo lugar (во-первых/во-вторых), En conclusión (в заключение), Hay que tener en cuenta.",
            takeaways = listOf("Структура эссе", "По парам: лицо стороны", "Финал — En conclusión")),

        t("u12_l14", "Финал B1", "Заявление + интервью + дебаты",
            emoji = "🏆", cefr = "B1", minutes = 3,
            rule = "Финальный чекпоинт B1: применение всего материала в формальном контексте.",
            takeaways = listOf("Завершение B1", "254 урока пройдено!", "Готов к B2")),

        // ═══════════════════════════════════════════════════════════════
        //  B2 · БЛОК 4.1 «SUBJUNTIVO AVANZADO» — 16 теорий
        // ═══════════════════════════════════════════════════════════════
        t("u13_l0", "Imp.Subj. — обзор", "-ra и -se формы. Для гипотез типа 2.",
            emoji = "🔮", cefr = "B2", minutes = 3,
            rule = "Imperfecto Subjuntivo — повторение формы и применения. -ra (популярнее) и -se (формальнее) — синонимы.",
            takeaways = listOf("2 формы синонимичны", "Si тип 2: tuviera/tuviese", "Имел смысл «бы»")),

        t("u13_l1", "Imp.Subj. образование", "От 3.л.мн.ч. Indef → -ron + -ra/-se",
            emoji = "🔮", cefr = "B2", minutes = 3,
            rule = "Алгоритм: берёшь 3.л.мн.ч. Pretérito Indefinido (-aron, -ieron), убираешь -ron, добавляешь -ra/-ras/-ra/-´ramos/-rais/-ran (или -se формы).",
            takeaways = listOf("От 3.л.мн.ч. Indef", "Тильда в nosotros", "Все нерег глаголы по этому правилу")),

        t("u13_l2", "Si тип 2 (повтор)", "Si tuviera, viajaría",
            emoji = "🔮", cefr = "B2", minutes = 3,
            rule = "Si + Imp.Subj. + Cond — нереальная гипотеза в настоящем/будущем. Si tuviera dinero, viajaría más.",
            takeaways = listOf("Si + Imp.Subj. + Cond", "Никогда Si + Cond", "Тип 2 = маловероятно/невозможно")),

        t("u13_l3", "Ojalá + Imp.Subj.", "Невыполнимое желание. Ojalá supiera más.",
            emoji = "🔮", cefr = "B2", minutes = 3,
            rule = "Ojalá + Pres.Subj. = выполнимое желание (Ojalá venga). Ojalá + Imp.Subj. = НЕВЫПОЛНИМОЕ (Ojalá viniera = вот бы пришёл, но...).",
            takeaways = listOf("Pres.Subj. — выполнимо", "Imp.Subj. — невыполнимо", "Грамм. способ выразить отчаяние")),

        t("u13_l4", "Como si", "Как будто. ВСЕГДА + Imp.Subj.",
            emoji = "🔮", cefr = "B2", minutes = 3,
            rule = "Como si — нереальное сравнение. ВСЕГДА + Imp.Subj. (или Pluscuamp.Subj. для прошлого).",
            examples = listOf(
                Triple("Habla como si supiera todo.", "Говорит будто всё знает.", ""),
                Triple("Vive como si fuera rico.", "Живёт будто богат.", ""),
            ),
            takeaways = listOf("ВСЕГДА Imp.Subj.", "Нереальное сравнение", "Pluscuamp.Subj. для прошлого")),

        t("u13_l5", "Мини-чекпоинт Subj", "Все формы и применения",
            emoji = "🎯", cefr = "B2", minutes = 3,
            rule = "Повторение Imperfecto Subjuntivo + Si тип 2 + ojalá + como si.",
            takeaways = listOf("Закрепление Imp.Subj.", "Готов к Pluscuamp.Subj.", "Большой шаг B2")),

        t("u13_l5_5", "🆕 Quizás vs A lo mejor", "Оттенки уверенности",
            emoji = "🆕", cefr = "B2", minutes = 3,
            rule = "Quizás/Tal vez + Subj — стандартное «может быть». Если + Indic — больше уверенности. A lo mejor + Indic — особый разговорный (не Subj!). Acaso + Subj — книжно.",
            takeaways = listOf("Quizás+Subj стандарт", "A lo mejor+Indic", "Acaso — книжно")),

        t("u13_l6", "Pluscuamp Subj.", "hubiera + part. Si hubiera sabido...",
            emoji = "🔮", cefr = "B2", minutes = 4,
            rule = "Pluscuamperfecto de Subjuntivo = haber в Imp.Subj. (hubiera/hubiese) + participio. Для гипотез о ПРОШЛОМ.",
            tableHeaders = listOf("Лицо", "haber Imp.Subj.", "+ part"),
            tableRows = listOf(listOf("yo", "hubiera/hubiese", "ido"),
                listOf("tú", "hubieras", "comido"), listOf("él", "hubiera", "hecho"),
                listOf("nos", "hubiéramos", "visto"), listOf("ellos", "hubieran", "venido")),
            takeaways = listOf("haber Imp.Subj. + part", "Для прошлого", "Si тип 3 + сожаление")),

        t("u13_l7", "Si hubiera sabido (тип 3)",
            "Si + Pluscuamp.Subj + Cond.Comp. Сожаление о прошлом.",
            emoji = "🔮", cefr = "B2", minutes = 4,
            rule = "Si тип 3 = Si + Pluscuamp.Subj. + Cond.Compuesto (habría + part). Сожаление о ПРОШЛОМ. «Если бы тогда знал, сделал бы иначе».",
            takeaways = listOf("Si тип 3 — про прошлое", "Pluscuamp.Subj. + Cond.Comp.", "Сожаление")),

        t("u13_l8", "Condicional Compuesto", "habría + part. Сделал бы (тогда).",
            emoji = "🔮", cefr = "B2", minutes = 3,
            rule = "Condicional Compuesto = haber в Cond + participio. Используется в Si тип 3 и для прошлых гипотез.",
            tableHeaders = listOf("Лицо", "haber Cond", "+ part"),
            tableRows = listOf(listOf("yo", "habría", "hablado"), listOf("tú", "habrías", "comido"),
                listOf("él", "habría", "ido"), listOf("nos", "habríamos", "visto"),
                listOf("ellos", "habrían", "hecho")),
            takeaways = listOf("haber Cond + part", "«Сделал бы (тогда)»", "Главная часть в Si тип 3")),

        t("u13_l9", "Устойчивые формулы Subj",
            "Que yo sepa, Pase lo que pase, Sea como sea",
            emoji = "🔮", cefr = "B2", minutes = 3,
            rule = "Устойчивые формулы с Subj: Que yo sepa (насколько я знаю), Que yo recuerde (насколько помню), Cueste lo que cueste (чего бы ни стоило), Pase lo que pase (что бы ни случилось), Sea como sea (как бы ни было).",
            takeaways = listOf("Учить целиком", "Часто слышатся в речи", "Идиоматичны")),

        t("u13_l10", "Aunque оттенки", "Indic = факт. Subj = гипотеза/будущее.",
            emoji = "🔮", cefr = "B2", minutes = 3,
            rule = "Aunque + Indic — факт известен. Aunque + Subj — гипотеза или нереализованное будущее. Тонкий выбор по контексту.",
            takeaways = listOf("Известно → Indic", "Неизвестно/будущее → Subj", "Похоже на cuando")),

        t("u13_l11", "Subj в придаточных цели",
            "para que, a fin de que, con el objeto de que → Subj",
            emoji = "🔮", cefr = "B2", minutes = 3,
            rule = "Все триггеры цели + Subj: para que (чтобы — нейтр), a fin de que (с тем чтобы — формал), con el objeto de que (книжн).",
            takeaways = listOf("Все цели → Subj", "Если 1 субъект — para + inf", "Книжность нарастает")),

        t("u13_l12", "Subj в придаточных времени",
            "cuando/en cuanto/hasta que/mientras + Subj (для будущего)",
            emoji = "🔮", cefr = "B2", minutes = 3,
            rule = "Триггеры времени для БУДУЩЕГО + Subj: cuando (когда), en cuanto (как только), hasta que (пока не), mientras (пока), después de que (после того как).",
            takeaways = listOf("Будущее → Subj", "Прошлое/привычка → Indic", "Mientras особый — оба варианта")),

        t("u13_l13", "Subj в относительных",
            "Busco alguien que sepa — гипотет. референт",
            emoji = "🔮", cefr = "B2", minutes = 3,
            rule = "Если референт ГИПОТЕТИЧЕСКИЙ (любой, ищу не зная кого) — Subj. Если КОНКРЕТНЫЙ — Indic. Busco alguien que sepa (любой) vs Conozco alguien que sabe (конкретный).",
            takeaways = listOf("Гипотет → Subj", "Конкретный → Indic", "Cualquier что-то + Subj")),

        t("u13_l14", "Чекпоинт «Сожаления»", "Pluscuamp + Si тип 3 + Cond.Comp",
            emoji = "🏁", cefr = "B2", minutes = 3,
            rule = "Финал блока 4.1: рассказ о сожалениях с применением всех Subj-форм.",
            takeaways = listOf("Все Subj пройдены", "Готов к Pasiva+Perífrasis", "Большой шаг B2")),

        // ═══════════════════════════════════════════════════════════════
        //  B2 · БЛОК 4.2 «PASIVA Y PERÍFRASIS» — 16 теорий
        // ═══════════════════════════════════════════════════════════════
        t("u14_l0", "Voz pasiva con SER", "fue construido. Действие, кем что сделано.",
            emoji = "⚙", cefr = "B2", minutes = 3,
            rule = "Pasiva con SER + part. Используется реже чем в англ — испанцы предпочитают SE-пассив или активный залог. Подходит для формальных текстов.",
            takeaways = listOf("Формально", "ser + part + (por + agent)", "Согласование part")),

        t("u14_l1", "Estar + part = состояние",
            "está hecho — готов. Не «как сделано», а «в каком состоянии»",
            emoji = "⚙", cefr = "B2", minutes = 3,
            rule = "estar + part — РЕЗУЛЬТАТ или состояние. NO про действие. La puerta está cerrada (закрыта сейчас) vs fue cerrada (была закрыта кем-то).",
            takeaways = listOf("Состояние", "Не «кем»", "Можно с estar в любом времени")),

        t("u14_l2", "Se pasivo y se impersonal",
            "Se vende coche. Se dice que. Часто заменяет SER-пассив.",
            emoji = "⚙", cefr = "B2", minutes = 4,
            rule = "SE-пассив: Se + 3.л + сущ. Se vende coche / Se venden coches. SE-безличное: Se + 3.л.ед. — без subjекта (Se dice que..., Aquí se vive bien).",
            takeaways = listOf("SE — испанская альтернатива пассиву", "Se vende vs Se venden (по числу)", "Se impersonal — без подлежащего")),

        t("u14_l3", "Perífrasis: ir a / acabar de / volver a",
            "Воспоминание + 3 главные перифразы",
            emoji = "⚙", cefr = "B2", minutes = 3,
            rule = "ir a + inf — собираться (близкое будущее). acabar de + inf — только что. volver a + inf — снова.",
            takeaways = listOf("ir a — близкое будущее", "acabar de — недавнее прошлое", "volver a — повторение")),

        t("u14_l4", "Llevar + ger (повтор)", "Делаю уже X времени",
            emoji = "⚙", cefr = "B2", minutes = 2,
            rule = "Llevar + время + gerundio. Альтернатива hace+que.",
            takeaways = listOf("Llevo X tiempo + ger", "Альтернатива hace+que", "Действие продолжается")),

        t("u14_l5", "Мини-чекпоинт Pasiva+Perífrasis",
            "Применение основ", emoji = "🎯", cefr = "B2", minutes = 3,
            rule = "Закрепление основных пассивов и перифраз.",
            takeaways = listOf("Пассив + 3 перифразы", "Готов к продвинутым", "")),

        t("u14_l6", "Seguir / dejar de / ponerse a",
            "продолжать / перестать / приниматься",
            emoji = "⚙", cefr = "B2", minutes = 3,
            rule = "seguir + ger = продолжать. dejar de + inf = перестать. ponerse a + inf = приниматься (резко начать). Все продуктивные перифразы в речи.",
            takeaways = listOf("seguir + ger", "dejar de + inf", "ponerse a — резкое начало")),

        t("u14_l7", "Participio как прилагательное",
            "una puerta cerrada, un libro escrito",
            emoji = "⚙", cefr = "B2", minutes = 3,
            rule = "Participio (написан, закрыт, открыт) может работать как прилагательное. Согласуется по роду и числу: cerrado/cerrada/cerrados/cerradas.",
            takeaways = listOf("Согласование", "Часто после estar", "Перед сущ возможно но реже")),

        t("u14_l8", "Gerundio продвинутое",
            "habiendo llegado, estando enfermo — обстоятельства",
            emoji = "⚙", cefr = "B2", minutes = 4,
            rule = "Gerundio compuesto: habiendo + part = «сделав». Gerundio простой может быть обстоятельством причины/времени: Estando enfermo, no fui (будучи больным, не пошёл).",
            takeaways = listOf("habiendo + part", "Обстоятельство", "Книжный язык")),

        t("u14_l9", "Infinitivo как сущ",
            "El comer mucho es malo. Es importante estudiar.",
            emoji = "⚙", cefr = "B2", minutes = 3,
            rule = "Инфинитив как существительное: с артиклем (el comer es importante = есть важно) или без (Comer es necesario). После безличных «es + adj» — инфинитив.",
            takeaways = listOf("El + inf = тема/деятельность", "Es + adj + inf", "Альт сущ")),

        t("u14_l9_5", "🆕 Сложные предлоги",
            "a través de, en torno a, con respecto a, en lugar de",
            emoji = "🆕", cefr = "B2", minutes = 3,
            rule = "Сложные предлоги (несколько слов): a través de (через), en torno a (около), con respecto a (относительно), a pesar de (несмотря на), en lugar de (вместо), por medio de (посредством).",
            takeaways = listOf("Несколько слов", "ВСЕГДА с de/a", "Формальная речь")),

        t("u14_l10", "Relativos продвинутый повтор",
            "que, quien, cuyo, donde, lo cual",
            emoji = "💬", cefr = "B2", minutes = 3,
            rule = "Повторение относительных местоимений с акцентом на разницу: el cual (формал), cuyo (чей), lo cual (про всю ситуацию).",
            takeaways = listOf("el cual — формал", "cuyo — согласование", "lo cual — про ситуацию")),

        t("u14_l11", "Косв.речь: сдвиг указателей",
            "hoy → ese día, mañana → al día siguiente, este → ese",
            emoji = "💬", cefr = "B2", minutes = 4,
            rule = "В косв.речи меняются НЕ ТОЛЬКО времена, но и указатели места/времени: hoy→ese día, ayer→el día anterior, mañana→al día siguiente, aquí→allí, este→ese/aquel.",
            takeaways = listOf("Сдвиг указателей", "Время+место+указ", "Учить таблицу")),

        t("u14_l12", "Ser vs Estar — нюансы",
            "ser bueno vs estar bueno (хороший vs вкусный)",
            emoji = "💬", cefr = "B2", minutes = 4,
            rule = "Тонкие пары значений: ser bueno (хороший человек) vs estar bueno (вкусный/здоровый). ser listo (умный) vs estar listo (готов). ser malo vs estar malo (плохой vs больной). Контекст определяет.",
            takeaways = listOf("Прилагательное может менять смысл", "Учить пары", "Нюансы B2")),

        t("u14_l13", "Nominalización", "Глагол → сущ. decidir → decisión",
            emoji = "💬", cefr = "B2", minutes = 3,
            rule = "Превращение глагола в существительное: -ción (decidir→decisión, construir→construcción), -aje (aterrizar→aterrizaje), -encia (creer→creencia), -ido (sonido).",
            takeaways = listOf("Деловой/научный стиль", "-ción / -aje / -encia", "Учить целиком")),

        t("u14_l14", "Чекпоинт «Журналистика»",
            "Pasiva + perífrasis + ger/inf + относит",
            emoji = "🏁", cefr = "B2", minutes = 3,
            rule = "Финал блока 4.2: репортаж/расследование с применением всего материала.",
            takeaways = listOf("Все темы блока", "Готов к Comunicación Formal", "Большой объём B2")),

        // ═══════════════════════════════════════════════════════════════
        //  B2 · БЛОК 4.3 «COMUNICACIÓN FORMAL» — 16 теорий
        // ═══════════════════════════════════════════════════════════════
        t("u15_l0", "Регистры", "formal / neutro / coloquial — выбор по ситуации",
            emoji = "✍", cefr = "B2", minutes = 3,
            rule = "3 регистра: formal (с незнакомыми, по работе), neutro (медиа, нейтрально), coloquial (друзья, разговор). Регистр определяется ситуацией.",
            takeaways = listOf("По ситуации", "В письме — formal", "С друзьями — coloquial")),

        t("u15_l1", "Carta formal", "solicitud, queja, agradecimiento",
            emoji = "✍", cefr = "B2", minutes = 3,
            rule = "3 типа формальных писем: solicitud (запрос/заявление), queja (жалоба), agradecimiento (благодарность). Структура: обращение (Estimado/a), тело (Le escribo para...), заключение (Atentamente).",
            takeaways = listOf("3 типа писем", "Le ruego/Le agradezco", "Atentamente — финал")),

        t("u15_l2", "Informe escrito", "Введение / Основная часть / Заключение",
            emoji = "✍", cefr = "B2", minutes = 3,
            rule = "Структура отчёта: Introducción (что и зачем), Desarrollo (анализ + аргументы), Conclusión (вывод). Дополнительно: Resumen, Bibliografía.",
            takeaways = listOf("3 главных части", "Подзаголовки помогают", "Conclusión = главный вывод")),

        t("u15_l3", "Artículo de opinión", "Тезис → аргументы → заключение",
            emoji = "✍", cefr = "B2", minutes = 3,
            rule = "Структура мнения: tesis (моя позиция), argumentos (доказательства), ejemplos (примеры), conclusión (укрепление позиции).",
            takeaways = listOf("Сначала тезис", "Аргументы с примерами", "В финале — позиция")),

        t("u15_l4", "Конекторы контраста", "sin embargo, no obstante, en cambio",
            emoji = "🔗", cefr = "B2", minutes = 3,
            rule = "Контраст: sin embargo (однако), no obstante (тем не менее — формал), en cambio (напротив), por el contrario (наоборот), ahora bien (однако — книжн).",
            takeaways = listOf("По формальности нарастают", "no obstante > sin embargo", "ahora bien — книжн")),

        t("u15_l5", "Мини-чекпоинт Conectores", "Все конекторы и регистр",
            emoji = "🎯", cefr = "B2", minutes = 3,
            rule = "Закрепление конекторов и выбора регистра в формальных текстах.",
            takeaways = listOf("Конекторы + регистр", "Готов к продвинутым", "")),

        t("u15_l6", "Конекторы причины", "dado que, puesto que, ya que",
            emoji = "🔗", cefr = "B2", minutes = 3,
            rule = "Причина: porque (нейтр-разг), dado que (формал), puesto que (формал), ya que (нейтр-формал), debido a que (из-за того что).",
            takeaways = listOf("По формальности", "porque — самое нейтр", "dado que / debido a — формал")),

        t("u15_l7", "Конекторы следствия",
            "de ahí que (+Subj), de modo que, por consiguiente",
            emoji = "🔗", cefr = "B2", minutes = 3,
            rule = "Следствие: por lo tanto/por eso (поэтому), así que (так что — разг), de modo que (так что — формал), por consiguiente (следовательно — книжн), de ahí que (отсюда — ВСЕГДА + Subj).",
            warning = "de ahí que — ВСЕГДА + Subj!",
            takeaways = listOf("По формальности", "de ahí que + Subj", "Книжность нарастает")),

        t("u15_l8", "Конекторы уступки",
            "a pesar de (que), si bien, aun cuando, pese a",
            emoji = "🔗", cefr = "B2", minutes = 3,
            rule = "Уступка: aunque (универс), a pesar de + сущ/que + глагол, si bien (хотя — формал), aun cuando (даже когда), pese a (вопреки — книжн).",
            takeaways = listOf("aunque — универс", "a pesar de — выбор + сущ или que+verb", "pese a — книжн")),

        t("u15_l9", "Аргументация", "Тезис → доказательство → вывод",
            emoji = "🗣", cefr = "B2", minutes = 3,
            rule = "Структура аргумента: 1) tesis (что утверждаешь), 2) argumentos (почему), 3) ejemplos (доказательства), 4) conclusión (укрепление). Слова: demuestra, confirma, prueba.",
            takeaways = listOf("4 шага", "Слова: demostrar/confirmar", "Без «потому что — да»")),

        t("u15_l10", "Цитирование", "según, de acuerdo con, a juicio de",
            emoji = "📚", cefr = "B2", minutes = 3,
            rule = "Цитирование: según (согласно — нейтр), de acuerdo con (в соответствии — формал), a juicio de (по мнению — книжн). Глаголы: afirma, sostiene, plantea, considera.",
            takeaways = listOf("По формальности", "Глаголы цитирования", "В академ.письме обязательно")),

        t("u15_l11", "Latinismos", "per se, a posteriori, in situ, ad hoc",
            emoji = "📚", cefr = "B2", minutes = 3,
            rule = "Латинские выражения в исп: per se (сам по себе), a posteriori/a priori (после/до факта), in situ (на месте), ipso facto (тут же), ad hoc (специально), motu proprio (по своей воле).",
            takeaways = listOf("Книжный/академ", "Учить формы", "Не злоупотреблять")),

        t("u15_l11_5", "🆕 Полусоюзы", "mientras, aunque — Indic vs Subj",
            emoji = "🆕", cefr = "B2", minutes = 4,
            rule = "Тонкости: mientras + Indic = одновременно (Mientras estudio, escucho música). mientras + Subj = «до тех пор пока» (Mientras no venga, esperaré). aunque — Indic (факт) vs Subj (гипот).",
            takeaways = listOf("Одновр → Indic", "Условие → Subj", "Контекст определяет")),

        t("u15_l12", "Nominalización деловая",
            "implementación, aprobación, valoración, negociación",
            emoji = "📚", cefr = "B2", minutes = 3,
            rule = "Деловые/официальные сущ от глаголов: implementación (внедрение), aprobación, valoración, negociación, decisión, formación. Часто на -ción/-sión.",
            takeaways = listOf("Деловой стиль", "-ción / -sión", "Заменяют глаголы в формал.речи")),

        t("u15_l13", "Léxico académico",
            "demostrar, evidenciar, sostener, plantear, indagar",
            emoji = "📚", cefr = "B2", minutes = 3,
            rule = "Академическая лексика: demostrar (доказывать), evidenciar (указывать), sostener (поддерживать утверждение), plantear (ставить вопрос/проблему), indagar (исследовать), señalar (указывать).",
            takeaways = listOf("Академ.речь", "Заменяют простое «decir»", "В эссе/статьях")),

        t("u15_l14", "Чекпоинт «Аналитическая статья»",
            "Все формальные конструкции в одном",
            emoji = "🏁", cefr = "B2", minutes = 3,
            rule = "Финал блока 4.3: написание аналитической статьи с применением всех формальных средств.",
            takeaways = listOf("Все конекторы", "Готов к финалу B2", "Профессиональный уровень")),

        // ═══════════════════════════════════════════════════════════════
        //  B2 · БЛОК 4.4 «LÉXICO Y CULTURA» — 16 теорий, ФИНАЛ КУРСА
        // ═══════════════════════════════════════════════════════════════
        t("u16_l0", "Modismos B2", "a rajatabla, en boca de todos, de pies a cabeza",
            emoji = "🌟", cefr = "B2", minutes = 3,
            rule = "Идиомы B2: a rajatabla (строго), en boca de todos (на устах), de pies a cabeza (с ног до головы), a manos llenas (щедро), al pie de la letra (буквально).",
            takeaways = listOf("Учить целиком", "В разговоре украшают речь", "Идиоматичны — переводить нельзя")),

        t("u16_l1", "Modismos B2 (II)",
            "no hay mal que..., más vale prevenir, a buen entendedor",
            emoji = "🌟", cefr = "B2", minutes = 3,
            rule = "Поговорки: «No hay mal que por bien no venga» (нет худа без добра), «Más vale prevenir que curar» (лучше предотвратить), «A buen entendedor pocas palabras bastan» (умному и намёка хватит).",
            takeaways = listOf("Поговорки = культура", "В разговоре умных людей", "Учить полные формы")),

        t("u16_l2", "Refranes", "El que mucho abarca... / Más vale tarde...",
            emoji = "🌟", cefr = "B2", minutes = 3,
            rule = "Народные поговорки: «El que mucho abarca, poco aprieta» (много хочешь — мало получишь), «Más vale tarde que nunca», «Dime con quién andas y te diré quién eres» (скажи кто твой друг).",
            takeaways = listOf("Народная мудрость", "Узнаваемы носителями", "Можно усечённо: A quien madruga...")),

        t("u16_l3", "Эвфемизмы и дипломатия",
            "pasar a mejor vida, tercera edad, persona con discapacidad",
            emoji = "🌟", cefr = "B2", minutes = 3,
            rule = "Эвфемизмы — мягкие замены неприятных слов: pasar a mejor vida (умереть), tercera edad (старость), persona con discapacidad (инвалид), ajuste salarial (понижение зп), reestructuración (увольнения).",
            takeaways = listOf("Дипломатичность", "В формал.речи", "Заменяют грубое")),

        t("u16_l4", "Метафорический язык",
            "el corazón roto, un mar de problemas, luz al final",
            emoji = "🌟", cefr = "B2", minutes = 3,
            rule = "Метафоры: el corazón roto (разбитое сердце), un mar de problemas (море проблем), la luz al final del túnel (свет в конце тоннеля), tener la cabeza en las nubes (витать), ser uña y carne (не разлей вода).",
            takeaways = listOf("Образность", "В литературе и речи", "Часто переводимы")),

        t("u16_l4_5", "🆕 Falsos cognados deep dive",
            "embarazada≠embarrassed, sensible≠sensible, actual≠actual",
            emoji = "🆕", cefr = "B2", minutes = 4,
            rule = "Главные ложные друзья: embarazada=беременная (не «смущена»), sensible=чувствительный (не «разумный»→sensato), actual=нынешний (не «фактический»→real), constipado=простужен (не «запор»), éxito=успех (не «выход»→salida).",
            warning = "Embarazada — самая известная ловушка. «I'm embarrassed» — Tengo vergüenza / Me da vergüenza.",
            takeaways = listOf("5+ опасных пар", "Учить специально", "Может вызвать казус")),

        t("u16_l5", "Мини-чекпоинт Modismos+Refranes",
            "Закрепление идиом и поговорок",
            emoji = "🎯", cefr = "B2", minutes = 3,
            rule = "Применение идиом и поговорок в контексте.",
            takeaways = listOf("Идиомы + поговорки", "Готов к фин.чекпоинту", "")),

        t("u16_l6", "Latinoamericano vs España",
            "vos/tú, jugo/zumo, móvil/celular, computador/ordenador",
            emoji = "🌍", cefr = "B2", minutes = 3,
            rule = "Главные различия: tú (Исп) vs vos (Аргентина и др.), vosotros (Исп) vs только ustedes (Латам), zumo→jugo, móvil→celular, ordenador→computador, coche→carro, patata→papa.",
            takeaways = listOf("vosotros — только Испания", "Лексика регионов", "Понимают друг друга")),

        t("u16_l7", "Falsos amigos II",
            "realizar, molestar, asistir, ropa, pretender",
            emoji = "🌍", cefr = "B2", minutes = 3,
            rule = "Ещё falsos amigos: realizar=осуществить (не «realize»), molestar=мешать (не «harass»), asistir=присутствовать (не «помогать»→ayudar), ropa=одежда (не «rope»→cuerda), pretender=пытаться/претендовать (не «pretend»→fingir).",
            takeaways = listOf("Ещё опасные слова", "Учить с примерами", "Контекст важен")),

        t("u16_l8", "Diminutivos и aumentativos",
            "casita, perrito (-ito); hombrón, golazo (-ón/-azo)",
            emoji = "🌍", cefr = "B2", minutes = 3,
            rule = "Уменьшительные (-ito/-illo/-ico — ласково/маленько): casita, perrito, momentito. Увеличительные (-ón/-azo/-ote — большое/хвалебное): hombrón (большой мужик), golazo (крутой гол), librazo (классная книга).",
            takeaways = listOf("Эмоциональная окраска", "-ito = ласково/мало", "-azo = классно/крупно")),

        t("u16_l9", "Современная лексика",
            "startup, sostenibilidad, branding, networking",
            emoji = "📱", cefr = "B2", minutes = 2,
            rule = "Заимствования и неологизмы: startup, sostenibilidad (устойчивость), branding, networking, marketing, feedback, app, hashtag.",
            takeaways = listOf("Англицизмы в моде", "Не переводятся", "В деловой среде")),

        t("u16_l10", "Профлексика", "negocios, derecho, medicina, ingeniería",
            emoji = "💼", cefr = "B2", minutes = 2,
            rule = "Сферы: negocios (бизнес), derecho (право/юриспруденция), medicina, ingeniería, educación, marketing, finanzas.",
            takeaways = listOf("Базовые сферы", "У каждой свой жаргон", "Полезно для CV")),

        t("u16_l11", "Cultura hispana",
            "Cervantes, Picasso, García Márquez, flamenco, Día de Muertos",
            emoji = "🎨", cefr = "B2", minutes = 3,
            rule = "Главные имена/явления: Cervantes (автор Дон Кихота), Picasso, Dalí (художники), García Márquez (Сто лет одиночества), Borges, flamenco (танец), Día de los Muertos (Мексика), Real Madrid/Barça.",
            takeaways = listOf("Культурный контекст", "Литература + искусство + традиции", "Для общения с носителями")),

        t("u16_l12", "Tricky cases",
            "sino vs pero, también vs tampoco, por vs para",
            emoji = "🔤", cefr = "B2", minutes = 4,
            rule = "Тонкости: pero (но — нейтр) vs sino (а — после отрицания: No es Pablo SINO Juan). también (тоже — положит) vs tampoco (тоже не — отриц). por/para — повторение нюансов.",
            takeaways = listOf("После отриц → sino", "Согласие в отриц → tampoco", "Тонкости важны на B2")),

        t("u16_l13", "Орфография продвинутая",
            "Тильда на вопросах, b/v, h-немое, x/s",
            emoji = "🔤", cefr = "B2", minutes = 3,
            rule = "Главные правила: 1) Тильда на вопросительных qué/cuándo/dónde/cómo (отличает от союзов); 2) b/v одинаково звучат — учить написание; 3) h-немое (всегда пишется но не звучит); 4) x в начале часто [c] (xenofobia).",
            takeaways = listOf("Тильда на вопросах ОБЯЗАТ.", "b и v разные на письме", "h молчит но пишется")),

        t("u16_l14", "🏆 ФИНАЛ КУРСА",
            "Все темы — от Hola до анализа",
            emoji = "🏆", cefr = "B2", minutes = 5,
            rule = "Финальный чекпоинт: эмиграция в Испанию. Применяешь ВСЁ — от приветствий до сложного формального стиля.",
            takeaways = listOf("Завершение всего курса!", "254 урока пройдено", "🎉 ¡Felicidades!")),

        // ─────────────────────────────────────────────────────────────────
        // u16_l9_coloquial — Разговорные восклицания и междометия
        // Bonus card: то, что слышишь на улице в Испании каждый день.
        // ─────────────────────────────────────────────────────────────────
        "u16_l9_coloquial" to TheoryContent(
            lessonId = "u16_l9_coloquial",
            title = "Разговорные восклицания: ¡joder!, ¡coño!, ¡qué fuerte!",
            subtitle = "Что слышно от испанцев каждый день. Где можно, где нельзя.",
            emoji = "🗣️",
            cefr = "B2",
            readMinutes = 4,
            sections = listOf(
                TheorySection(
                    type = TheorySectionType.WARNING,
                    heading = "⚠ Где НЕЛЬЗЯ",
                    body = "Эти выражения — colloquial, иногда vulgar. **Не использовать** с usted, на работе, при детях. На приёме у врача — никогда. С друзьями за пивом — нормально. Услышишь сразу, как только пройдёшь паспортный контроль в Барахасе.",
                ),
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "3 уровня окраски",
                    body = "**Mild** (нейтрально-разговорное, можно почти везде): vale, tío/tía, qué fuerte, madre mía, flipar.\n**Medium** (только со своими): joder, hostia.\n**Strong** (буквально vulgar, но используется широко): coño. Запомни: чем «крепче» — тем уже круг, где это уместно.",
                ),
                TheorySection(
                    type = TheorySectionType.EXAMPLES,
                    heading = "Топ-8 выражений",
                    examples = listOf(
                        TheoryExample("¡Joder, qué tarde!", "Блин, как поздно!", "medium — «блин/чёрт», самое распространённое"),
                        TheoryExample("¡Coño, qué frío!", "Ё-моё, как холодно!", "medium-strong — буквально неприличное, но в Spain как «блин!»"),
                        TheoryExample("¡Hostia, qué pasada!", "Вот это да!", "medium — religious origin, «чёрт!» / «вот это да!»"),
                        TheoryExample("¡Qué fuerte!", "Нифига себе!", "mild — абсолютно нормально, даже на ТВ"),
                        TheoryExample("¡Madre mía!", "Мама дорогая!", "mild — универсальное удивление"),
                        TheoryExample("Vale, nos vemos.", "Окей, увидимся.", "neutral — «окей», маркер разговорной речи"),
                        TheoryExample("¿Qué pasa, tío?", "Чё как, чувак?", "mild — обращение к ровесникам (tía к девушке)"),
                        TheoryExample("Estoy flipando.", "Я офигеваю.", "mild — разговорный глагол, очень частый"),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.TIP,
                    heading = "💡 Как использовать без позора",
                    body = "1) **Слушай и копируй контекст**: если друг говорит joder — можно ответить тем же. Если коллега — не подхватывай.\n2) **Никогда не первым** с незнакомцами старше тебя.\n3) **Vale и qué fuerte** — безопасны почти везде, начинай с них.\n4) **Coño** — самое рискованное: в Spain mainstream, в Latam воспринимается жёстче.\n5) **Joder** в начале фразы = «блин», в середине = усилитель: «es la joder buena» — «это охрененно хорошо».",
                ),
            ),
            keyTakeaways = listOf(
                "Регистр решает всё — не использовать с usted",
                "Mild (vale, qué fuerte) — везде ок",
                "Joder / hostia — только со своими",
                "Coño в Spain ≠ coño в Latam (там жёстче)",
            ),
            relatedTheory = emptyList(),
        ),

        // ─────────────────────────────────────────────────────────────────
        // u16_l10_voseo — Voseo: аргентинский и уругвайский испанский
        // Bonus card: половина испаноязычного мира говорит «vos», а не «tú».
        // ─────────────────────────────────────────────────────────────────
        "u16_l10_voseo" to TheoryContent(
            lessonId = "u16_l10_voseo",
            title = "Voseo: tú vs vos. Аргентинский испанский",
            subtitle = "Почему аргентинец говорит «vos tenés» вместо «tú tienes».",
            emoji = "🇦🇷",
            cefr = "B2",
            readMinutes = 4,
            sections = listOf(
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "Что такое voseo",
                    body = "В **Аргентине, Уругвае, Парагвае**, частях **Боливии** и Центральной Америки используется **vos** вместо tú. Это не региональный диалект, а **национальный стандарт**. По радио, в кино, в учебниках, в газетах — vos. RAE (Real Academia) официально признаёт voseo нормой.",
                ),
                TheorySection(
                    type = TheorySectionType.EXAMPLES,
                    heading = "tú vs vos — сравнение спряжений",
                    examples = listOf(
                        TheoryExample("tú eres → vos sos", "ты есть (быть)", "ser"),
                        TheoryExample("tú tienes → vos tenés", "у тебя есть (иметь)", "tener"),
                        TheoryExample("tú puedes → vos podés", "ты можешь (мочь)", "poder"),
                        TheoryExample("tú quieres → vos querés", "ты хочешь (хотеть)", "querer"),
                        TheoryExample("tú vienes → vos venís", "ты приходишь (приходить)", "venir"),
                        TheoryExample("tú trabajas → vos trabajás", "ты работаешь (работать)", "trabajar"),
                        TheoryExample("¿Vos sabés qué hora es?", "Ты знаешь, который час?", "окончания -ás/-és/-ís + ударение на последний слог, без дифтонгизации"),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.TIP,
                    heading = "💡 Почему испанские учебники этого не учат",
                    body = "Потому что учебники **Spain-centric** — пишутся в Мадриде/Барселоне для европейского рынка. Но цифры говорят другое:\n• **~45 млн** носителей **vos** (Argentina 45M + Uruguay 3.4M + Paraguay 7M + части Центр.Америки)\n• **~47 млн** активно используют **tú** в Latam (Mexico, Colombia, Peru, Chile…)\n\nЭто **половина** испаноязычного мира. Если ты планируешь Latam — voseo критичен. Если только Spain — можно игнорировать.",
                ),
                TheorySection(
                    type = TheorySectionType.TEXT,
                    heading = "📍 В нашем курсе",
                    body = "В **Madrid Spanish** (наш курс) — только **tú**. Это позиция курса: один диалект, чтобы не путать новичка. Но если поедешь в **Buenos Aires** — учитывай: «¿Vos de dónde sos?» вместо «¿Tú de dónde eres?». Аргентинцы поймут tú, но сами так не говорят.",
                ),
            ),
            keyTakeaways = listOf(
                "vos = tú в Argentina, Uruguay, Paraguay и др.",
                "Окончания: -ás / -és / -ís (ударение на последний слог)",
                "~45 млн носителей — половина Latam",
                "Наш курс = Spain Spanish, vos только для справки",
            ),
            relatedTheory = emptyList(),
        ),

        // ═══════════════════════════════════════════════════════════════
        //  Phase 3 — расширенная теория (40 новых карточек 2026-05-24)
        //  Глубокая теория к ключевым грамматическим урокам.
        //  Каждая карточка: 4 секции (RULE + EXAMPLES + WARNING/TIP) + 4 takeaways.
        // ═══════════════════════════════════════════════════════════════

        // ───────── A1 (10) ─────────

        "u2_l0" to TheoryContent(
            lessonId = "u2_l0",
            title = "Артикли el / la / los / las",
            subtitle = "Определённый артикль — про конкретный, известный объект.",
            emoji = "📌", cefr = "A1", readMinutes = 4,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Что такое определённый артикль",
                    body = "**El / la / los / las** = «тот самый», знакомый собеседнику объект. Используется когда:\n• Об объекте уже говорили\n• Объект единственный (el sol, la luna)\n• Контекст ясен (el profesor — наш учитель)\n\nАртикль СОГЛАСУЕТСЯ с существительным по роду и числу."),
                TheorySection(TheorySectionType.TABLE, "4 формы артикля",
                    table = TheoryTable(
                        headers = listOf("Род / Число", "Ед.ч.", "Мн.ч."),
                        rows = listOf(
                            listOf("Мужской", "el", "los"),
                            listOf("Женский", "la", "las"),
                        ),
                        highlightedColumns = listOf(1, 2),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Примеры",
                    examples = listOf(
                        TheoryExample("el libro", "книга (м.)", "ед.ч. м.р."),
                        TheoryExample("la casa", "дом (ж.)", "ед.ч. ж.р."),
                        TheoryExample("los libros", "книги", "мн.ч. м.р."),
                        TheoryExample("las casas", "дома", "мн.ч. ж.р."),
                        TheoryExample("el agua fría", "холодная вода", "ж.р. но начинается на ударное a → el"),
                        TheoryExample("el sol brilla", "солнце светит", "единственное → el"),
                    )),
                TheorySection(TheorySectionType.WARNING, "⚠ Ловушка с «el agua»",
                    body = "Существительные женского рода, начинающиеся с **ударного a-/ha-**, в ед.ч. берут **el** для благозвучия: **el agua**, **el águila**, **el hambre**. Но прилагательное остаётся женское: el agua fría. Во мн.ч. возвращается la → **las aguas**."),
            ),
            keyTakeaways = listOf(
                "4 формы: el / la / los / las",
                "Согласуется с сущ. по роду и числу",
                "Указывает на конкретный, известный объект",
                "el agua (ж.р. но el в ед.ч.) — для благозвучия",
            ),
            relatedTheory = listOf("u2_l1"),
        ),

        "u2_l2" to TheoryContent(
            lessonId = "u2_l2",
            title = "Числа 0–30",
            subtitle = "Базовые числа: cero, uno, dos… veintiuno, veintidós… treinta.",
            emoji = "🔢", cefr = "A1", readMinutes = 4,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Структура счёта",
                    body = "Числа 0–15 — отдельные слова (учим наизусть). 16–19 пишутся **слитно** как diez+y+ед: dieci**séis**. 20 = veinte. 21–29 слитно: **veintiuno, veintidós**…  30 = treinta."),
                TheorySection(TheorySectionType.TABLE, "Все числа 0–30",
                    table = TheoryTable(
                        headers = listOf("№", "Слово"),
                        rows = listOf(
                            listOf("0", "cero"), listOf("1", "uno"), listOf("2", "dos"),
                            listOf("3", "tres"), listOf("4", "cuatro"), listOf("5", "cinco"),
                            listOf("10", "diez"), listOf("15", "quince"), listOf("16", "dieciséis"),
                            listOf("20", "veinte"), listOf("21", "veintiuno"), listOf("25", "veinticinco"),
                            listOf("30", "treinta"),
                        ),
                        highlightedColumns = listOf(1),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "В речи",
                    examples = listOf(
                        TheoryExample("Tengo veinticinco años.", "Мне 25 лет.", "слитно"),
                        TheoryExample("Son las once.", "11 часов.", ""),
                        TheoryExample("Hay treinta personas.", "Есть 30 человек.", ""),
                        TheoryExample("Veintiuno de mayo", "21 мая", "veintiuno → veintiún перед сущ. м.р."),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 uno → un / una",
                    body = "Перед существительным **uno** теряет -o: un libro (один). Перед ж.р. — una: **una mesa**. То же со сложными: **veintiún** libros, **veintiuna** mesas."),
            ),
            keyTakeaways = listOf(
                "0–15 — отдельные слова",
                "16–29 — слитное написание (dieciséis, veintiuno)",
                "30 = treinta (с e!)",
                "uno → un/una перед сущ.",
            ),
            relatedTheory = listOf("u2_l6", "u2_l7"),
        ),

        "u2_l8" to TheoryContent(
            lessonId = "u2_l8",
            title = "Множественное число существительных",
            subtitle = "+s после гласной, +es после согласной. Простое правило.",
            emoji = "📚", cefr = "A1", readMinutes = 3,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Два правила",
                    body = "1) Слово кончается на **гласную** (a/e/i/o/u) → добавляем **-s**: libro → libros, casa → casas.\n2) Слово кончается на **согласную** → добавляем **-es**: profesor → profesores, ciudad → ciudades.\n3) На -z → -ces: lápiz → lápices, vez → veces."),
                TheorySection(TheorySectionType.EXAMPLES, "Примеры",
                    examples = listOf(
                        TheoryExample("libro → libros", "книга → книги", "гласная + s"),
                        TheoryExample("mesa → mesas", "стол → столы", "гласная + s"),
                        TheoryExample("profesor → profesores", "учитель → учителя", "согл. + es"),
                        TheoryExample("ciudad → ciudades", "город → города", "согл. + es"),
                        TheoryExample("lápiz → lápices", "карандаш → карандаши", "z → ces!"),
                        TheoryExample("el lunes → los lunes", "понедельник → понедельники", "уже на -s: без изм."),
                    )),
                TheorySection(TheorySectionType.WARNING, "⚠ Тильды могут уйти/прийти",
                    body = "При добавлении -es ударение может перейти, и тильда исчезает:\n• examen → exámenes (тильда появилась)\n• joven → jóvenes (тильда появилась)\n• canción → canciones (тильда УШЛА — ударение естественное)"),
            ),
            keyTakeaways = listOf(
                "Гласная + s, согласная + es",
                "На -z → -ces (lápiz → lápices)",
                "Слова на -s без изменений (lunes, crisis)",
                "Тильды могут меняться при добавлении -es",
            ),
            relatedTheory = listOf("u2_l9"),
        ),

        "u2_l4" to TheoryContent(
            lessonId = "u2_l4",
            title = "Глагол TENER — иметь",
            subtitle = "tengo / tienes / tiene / tenemos / tenéis / tienen. Возраст, голод, обладание.",
            emoji = "🟠", cefr = "A1", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Зачем нужен TENER",
                    body = "TENER — нерегулярный глагол «иметь», но используется НАМНОГО шире, чем русское «иметь». В испанском через TENER говорят:\n• Возраст: **Tengo 25 años** (не «мне 25», а «имею 25 лет»)\n• Голод/жажду: **tengo hambre** (имею голод)\n• Холод/жару: **tengo frío** / **tengo calor**\n• Страх/сон: **tengo miedo** / **tengo sueño**\n• Обладание: tengo un perro"),
                TheorySection(TheorySectionType.TABLE, "Спряжение в настоящем",
                    table = TheoryTable(
                        headers = listOf("Лицо", "Форма", "Пример"),
                        rows = listOf(
                            listOf("yo", "tengo", "Tengo hambre"),
                            listOf("tú", "tienes", "Tienes razón"),
                            listOf("él/ella/usted", "tiene", "Tiene 30 años"),
                            listOf("nosotros", "tenemos", "Tenemos tiempo"),
                            listOf("vosotros", "tenéis", "¿Tenéis ganas?"),
                            listOf("ellos/ustedes", "tienen", "Tienen frío"),
                        ),
                        highlightedColumns = listOf(1),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Полезные выражения",
                    examples = listOf(
                        TheoryExample("Tengo veintiocho años.", "Мне 28 лет.", "возраст ВСЕГДА через tener"),
                        TheoryExample("Tengo sed.", "Я хочу пить.", "буквально: имею жажду"),
                        TheoryExample("Tenemos prisa.", "Мы спешим.", "tener prisa = спешить"),
                        TheoryExample("¿Tienes hijos?", "У тебя есть дети?", ""),
                        TheoryExample("Tiene razón.", "Он/она прав(а).", "tener razón = быть правым"),
                        TheoryExample("Tengo que estudiar.", "Я должен учиться.", "tener que + inf = обязанность"),
                    )),
                TheorySection(TheorySectionType.WARNING, "⚠ Не путай TENER и HABER",
                    body = "**Tengo un coche** = у меня есть машина (мой, владею).\n**Hay un coche** = есть какая-то машина (просто существует).\n\nTENER = обладание / личная характеристика. HAY = безличное «есть/имеется»."),
            ),
            keyTakeaways = listOf(
                "Возраст, голод, жажда — через TENER",
                "yo tengo (не «teno»), ellos tienen",
                "tener que + inf = должен (обязанность)",
                "tener vs hay: владею vs просто есть",
            ),
            relatedTheory = listOf("u2_l5"),
        ),

        "u2_l10" to TheoryContent(
            lessonId = "u2_l10",
            title = "Притяжательные mi / tu / su",
            subtitle = "Мой/твой/его. Главная ловушка — «su» = «его/её/Ваш/их».",
            emoji = "🔑", cefr = "A1", readMinutes = 4,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Короткая форма (перед сущ.)",
                    body = "**Mi** (мой), **tu** (твой), **su** (его/её/Ваш/их), **nuestro/-a** (наш), **vuestro/-a** (ваш), **su** (их/Ваш мн.ч.).\n\nСогласуются только по ЧИСЛУ: mi libro / mis libros. Кроме nuestro/vuestro — те ещё и по роду: nuestra casa / nuestros libros."),
                TheorySection(TheorySectionType.TABLE, "Все формы",
                    table = TheoryTable(
                        headers = listOf("Лицо", "Ед.ч.", "Мн.ч."),
                        rows = listOf(
                            listOf("мой", "mi", "mis"),
                            listOf("твой", "tu", "tus"),
                            listOf("его/её/Ваш", "su", "sus"),
                            listOf("наш", "nuestro/-a", "nuestros/-as"),
                            listOf("ваш", "vuestro/-a", "vuestros/-as"),
                            listOf("их/Ваш мн.", "su", "sus"),
                        ),
                        highlightedColumns = listOf(1, 2),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "В контексте",
                    examples = listOf(
                        TheoryExample("mi hermano", "мой брат", ""),
                        TheoryExample("mis hermanos", "мои братья", "по числу"),
                        TheoryExample("nuestra casa", "наш дом", "ж.р. ед.ч."),
                        TheoryExample("vuestros amigos", "ваши друзья", "м.р. мн.ч."),
                        TheoryExample("su perro", "его / её / Ваш / их пёс", "контекст решает!"),
                        TheoryExample("Mi madre y su coche", "Моя мама и её машина", "su = её (т.к. madre)"),
                    )),
                TheorySection(TheorySectionType.WARNING, "⚠ Главная проблема — «su»",
                    body = "**Su** = его, её, Ваш (от usted), их, Ваш (от ustedes) — целых **5 значений**! Если контекст неясен, добавляют уточнение:\n• su coche **de él** — его машина\n• su coche **de ella** — её машина\n• su coche **de usted** — Ваша машина"),
            ),
            keyTakeaways = listOf(
                "mi/tu/su согласуются ТОЛЬКО по числу",
                "nuestro/vuestro — и по роду тоже",
                "su = 5 значений (его/её/Ваш/их/Ваш мн.)",
                "Уточнение: «de él/ella/usted» если неясно",
            ),
            relatedTheory = listOf("u2_l11"),
        ),

        "u3_l6" to TheoryContent(
            lessonId = "u3_l6",
            title = "QUERER — хотеть / любить",
            subtitle = "quiero / quieres / quiere. Модальный глагол + любовь.",
            emoji = "❤", cefr = "A1", readMinutes = 4,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Два значения QUERER",
                    body = "**1) Хотеть** (модальный):\n• Querer + сущ.: Quiero un café.\n• Querer + inf.: Quiero hablar.\n• Querer que + Subjuntivo: Quiero que vengas.\n\n**2) Любить** (про людей и питомцев):\n• Te quiero. = Я тебя люблю.\n• Quiero a mis padres. = Я люблю родителей.\n\nЭто стем-чейндж глагол: **e → ie** во всех формах кроме nosotros/vosotros."),
                TheorySection(TheorySectionType.TABLE, "Спряжение",
                    table = TheoryTable(
                        headers = listOf("Лицо", "Форма"),
                        rows = listOf(
                            listOf("yo", "quiero"),
                            listOf("tú", "quieres"),
                            listOf("él/ella/usted", "quiere"),
                            listOf("nosotros", "queremos"),
                            listOf("vosotros", "queréis"),
                            listOf("ellos/ustedes", "quieren"),
                        ),
                        highlightedColumns = listOf(1),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "В речи",
                    examples = listOf(
                        TheoryExample("Quiero un café, por favor.", "Хочу кофе, пожалуйста.", ""),
                        TheoryExample("¿Qué quieres hacer?", "Что ты хочешь делать?", ""),
                        TheoryExample("Te quiero.", "Я тебя люблю.", "стандартное «люблю»"),
                        TheoryExample("Te amo.", "Я тебя люблю.", "более сильное / романтичное"),
                        TheoryExample("Quiero que estudies más.", "Хочу, чтобы ты больше учился.", "+ subjuntivo"),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 querer vs amar",
                    body = "**Te quiero** — повседневное «люблю» (родители, друзья, партнёр).\n**Te amo** — более интенсивное, романтичное.\n\nВ Spain «te quiero» чаще, в Latam «te amo» употребляется свободнее. Оба правильные — просто разная окраска."),
            ),
            keyTakeaways = listOf(
                "querer = хотеть И любить",
                "e → ie во всех формах кроме nos/vos",
                "querer + inf., querer que + subj.",
                "te quiero vs te amo — оттенок интенсивности",
            ),
            relatedTheory = listOf("u3_l7"),
        ),

        "u3_l7" to TheoryContent(
            lessonId = "u3_l7",
            title = "PODER — мочь / уметь / просить разрешения",
            subtitle = "puedo / puedes / puede. Способность + разрешение + вежливые просьбы.",
            emoji = "💪", cefr = "A1", readMinutes = 4,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Три функции PODER",
                    body = "1) **Способность**: Puedo nadar. (Я умею плавать.)\n2) **Возможность**: Hoy no puedo. (Сегодня не могу.)\n3) **Разрешение / просьба**: ¿Puedo pasar? / ¿Puedes ayudarme?\n\nСтем-чейндж: **o → ue** (кроме nos/vos)."),
                TheorySection(TheorySectionType.TABLE, "Спряжение",
                    table = TheoryTable(
                        headers = listOf("Лицо", "Форма"),
                        rows = listOf(
                            listOf("yo", "puedo"),
                            listOf("tú", "puedes"),
                            listOf("él/ella/usted", "puede"),
                            listOf("nosotros", "podemos"),
                            listOf("vosotros", "podéis"),
                            listOf("ellos/ustedes", "pueden"),
                        ),
                        highlightedColumns = listOf(1),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "В речи",
                    examples = listOf(
                        TheoryExample("¿Puedo entrar?", "Можно войти?", "разрешение"),
                        TheoryExample("No puedo dormir.", "Не могу уснуть.", "возможность"),
                        TheoryExample("¿Puedes pasarme el agua?", "Можешь передать воду?", "вежливая просьба"),
                        TheoryExample("Podemos ir al cine.", "Мы можем сходить в кино.", "предложение"),
                        TheoryExample("Pueden esperar aquí.", "Можете подождать здесь.", "разрешение мн."),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 poder vs saber",
                    body = "**Puedo nadar** = могу плавать (физически в данный момент, или вообще умею).\n**Sé nadar** = умею плавать (навык, обучен).\n\nЕсли акцент на навыке/обучении → **saber**. Если на возможности здесь и сейчас → **poder**.\n«Sé conducir pero hoy no puedo» — умею водить, но сегодня не могу."),
            ),
            keyTakeaways = listOf(
                "o → ue (puedo, puedes, puede)",
                "Способность + возможность + разрешение",
                "¿Puedo...? — вежливый запрос",
                "poder (физ. возможность) vs saber (навык)",
            ),
            relatedTheory = listOf("u3_l6"),
        ),

        "u3_l8" to TheoryContent(
            lessonId = "u3_l8",
            title = "Время: ¿Qué hora es?",
            subtitle = "Es la una vs Son las dos. y media / y cuarto / menos.",
            emoji = "🕐", cefr = "A1", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Главная формула",
                    body = "**Es la una** (1 час) — единственное число.\n**Son las dos / tres / ... / doce** — мн.ч. для всех остальных часов.\n\nМинуты:\n• …**y cinco / diez / cuarto / veinte / media** (после часа)\n• …**menos cinco / cuarto** (без скольких-то)\n\nПример: 14:15 = Son las dos y cuarto. 14:45 = Son las tres menos cuarto."),
                TheorySection(TheorySectionType.TABLE, "Шаблоны",
                    table = TheoryTable(
                        headers = listOf("Время", "Фраза"),
                        rows = listOf(
                            listOf("13:00", "Es la una"),
                            listOf("14:00", "Son las dos"),
                            listOf("14:15", "Son las dos y cuarto"),
                            listOf("14:30", "Son las dos y media"),
                            listOf("14:45", "Son las tres menos cuarto"),
                            listOf("12:00 день", "Es mediodía"),
                            listOf("00:00 ночь", "Es medianoche"),
                        ),
                        highlightedColumns = listOf(1),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "В диалогах",
                    examples = listOf(
                        TheoryExample("¿Qué hora es?", "Который час?", "стандартный вопрос"),
                        TheoryExample("Son las siete y media.", "Половина восьмого (7:30).", ""),
                        TheoryExample("Es la una en punto.", "Ровно час.", "en punto = ровно"),
                        TheoryExample("Son las tres menos diez.", "Без десяти три (2:50).", ""),
                        TheoryExample("A las ocho de la mañana.", "В 8 утра.", "de la mañana"),
                        TheoryExample("A las cinco de la tarde.", "В 5 вечера.", "de la tarde"),
                    )),
                TheorySection(TheorySectionType.WARNING, "⚠ es vs son + la vs las",
                    body = "**Es la una** (1 ед.ч., ж.р. — la hora).\n**Son las…** (мн.ч.). Никогда не «son la dos», только «son las dos».\n\nДля «в … часов» используется **a las…**: A las tres. (в 3 часа.)"),
            ),
            keyTakeaways = listOf(
                "Es la una / Son las dos+ ",
                "y cuarto, y media, menos cuarto",
                "A las X = в X часов",
                "de la mañana / tarde / noche — части суток",
            ),
            relatedTheory = listOf("u3_l9"),
        ),

        "u4_l0" to TheoryContent(
            lessonId = "u4_l0",
            title = "Глагол IR — идти / ехать",
            subtitle = "voy / vas / va. Воротный глагол: + a + место И + a + inf (будущее).",
            emoji = "🚶", cefr = "A1", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Два главных применения",
                    body = "1) **IR + a + место**: Voy a Madrid. (Еду в Мадрид.)\n2) **IR + a + инфинитив** = «futuro próximo» (ближайшее будущее): Voy a estudiar. (Собираюсь учиться / Буду учиться.)\n\nIR абсолютно нерегулярный — учим формы наизусть."),
                TheorySection(TheorySectionType.TABLE, "Спряжение",
                    table = TheoryTable(
                        headers = listOf("Лицо", "Форма"),
                        rows = listOf(
                            listOf("yo", "voy"),
                            listOf("tú", "vas"),
                            listOf("él/ella/usted", "va"),
                            listOf("nosotros", "vamos"),
                            listOf("vosotros", "vais"),
                            listOf("ellos/ustedes", "van"),
                        ),
                        highlightedColumns = listOf(1),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "В речи",
                    examples = listOf(
                        TheoryExample("Voy al supermercado.", "Иду в супермаркет.", "a + el = al"),
                        TheoryExample("¿Adónde vas?", "Куда ты идёшь?", "adónde = куда"),
                        TheoryExample("Vamos a la playa.", "Идём на пляж.", ""),
                        TheoryExample("Voy a estudiar mañana.", "Буду учиться завтра.", "futuro próximo"),
                        TheoryExample("Va a llover.", "Сейчас пойдёт дождь.", "близкое предсказание"),
                        TheoryExample("¡Vamos!", "Идём! / Пошли!", "побуждение"),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 «a + el = al»",
                    body = "Когда **a** встречает **el** — они **сливаются** в **al**: Voy **al** parque (не «a el»). Это обязательное правило (как французский «au»). С остальными артиклями: a la playa, a los amigos, a las tiendas — без слияния."),
            ),
            keyTakeaways = listOf(
                "voy / vas / va / vamos / vais / van",
                "IR + a + место (куда)",
                "IR + a + inf = ближайшее будущее",
                "a + el = al (обязательное слияние)",
            ),
            relatedTheory = listOf("u4_l1"),
        ),

        "u4_l6" to TheoryContent(
            lessonId = "u4_l6",
            title = "GUSTAR — нравиться (зеркальная конструкция)",
            subtitle = "Me gusta el café. Не «я люблю кофе», а «мне нравится кофе».",
            emoji = "💛", cefr = "A1", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Главная идея: ЗЕРКАЛО",
                    body = "В русском: **Я** люблю кофе (я = подлежащее).\nВ испанском: **Кофе** мне нравится (кофе = подлежащее).\n\nКонструкция:\n**(A mí) me gusta** + **то-что-нравится** (ед.ч. сущ. ИЛИ инфинитив)\n**(A mí) me gustan** + **они** (мн.ч. сущ.)\n\nСогласуется ГЛАГОЛ с тем, что нравится — а не с тем, кому нравится."),
                TheorySection(TheorySectionType.TABLE, "Местоимения OI",
                    table = TheoryTable(
                        headers = listOf("Кому", "Местоим.", "Пример"),
                        rows = listOf(
                            listOf("мне", "me", "Me gusta el sol"),
                            listOf("тебе", "te", "Te gusta el cine"),
                            listOf("ему/ей/Вам", "le", "Le gusta cantar"),
                            listOf("нам", "nos", "Nos gusta la playa"),
                            listOf("вам", "os", "Os gusta jugar"),
                            listOf("им/Вам мн.", "les", "Les gustan los libros"),
                        ),
                        highlightedColumns = listOf(1, 2),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Ед.ч. vs Мн.ч.",
                    examples = listOf(
                        TheoryExample("Me gusta el café.", "Мне нравится кофе.", "ед.ч. → gusta"),
                        TheoryExample("Me gustan los gatos.", "Мне нравятся коты.", "мн.ч. → gustan"),
                        TheoryExample("Me gusta bailar.", "Мне нравится танцевать.", "инф. → gusta"),
                        TheoryExample("¿Te gusta el fútbol?", "Тебе нравится футбол?", ""),
                        TheoryExample("A Juan le gusta leer.", "Хуану нравится читать.", "уточнение «a + имя»"),
                        TheoryExample("A mí me encanta.", "Мне это обожаю.", "encantar = сильнее gustar"),
                    )),
                TheorySection(TheorySectionType.WARNING, "⚠ Главная ошибка новичка",
                    body = "**Никогда** не говорят «Yo gusto el café» — это значило бы «Я нравлюсь кофе» 🤯.\n\nПравильно: **Me gusta el café**. Подлежащее — el café, а «me» = «мне» (косвенное дополнение).\n\nГлагол согласуется с тем, что нравится: gusta (ед.) / gustan (мн.).\n\nТочно так же работают: encantar, doler, parecer, faltar, interesar."),
            ),
            keyTakeaways = listOf(
                "Зеркало: «кофе нравится мне», не «я люблю кофе»",
                "me/te/le/nos/os/les + gusta (ед.) / gustan (мн.)",
                "Инфинитив → всегда gusta",
                "encantar, doler, parecer — та же логика",
            ),
            relatedTheory = listOf("u4_l7"),
        ),

        // ───────── A2 (10) ─────────

        "u5_l0" to TheoryContent(
            lessonId = "u5_l0",
            title = "Pretérito Indefinido — введение",
            subtitle = "Прошедшее «событие, что случилось». Ayer, anoche, la semana pasada.",
            emoji = "⏪", cefr = "A2", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Что такое Indefinido",
                    body = "**Pretérito Indefinido** (= pretérito perfecto simple) — прошедшее время для **завершённых** действий с **чётким временем**.\n\nКогда использовать:\n• Конкретный момент в прошлом: **ayer, anoche, en 2020**\n• Серия событий: «пришёл, увидел, победил»\n• Биография: «родился, учился, работал»\n• Закрытый период: «жил там 3 года»\n\nНЕ для описаний, привычек, фона — для этого Imperfecto."),
                TheorySection(TheorySectionType.TABLE, "Маркеры времени",
                    table = TheoryTable(
                        headers = listOf("Маркер", "Значение"),
                        rows = listOf(
                            listOf("ayer", "вчера"),
                            listOf("anoche", "прошлой ночью"),
                            listOf("anteayer", "позавчера"),
                            listOf("la semana pasada", "на прошлой неделе"),
                            listOf("el mes pasado", "в прошлом месяце"),
                            listOf("en 2020", "в 2020 году"),
                            listOf("hace 3 días", "3 дня назад"),
                        ),
                        highlightedColumns = listOf(1),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Типичные ситуации",
                    examples = listOf(
                        TheoryExample("Ayer comí pizza.", "Вчера я ел пиццу.", "конкретный день"),
                        TheoryExample("Llegué a las ocho.", "Я прибыл в 8.", "точное время"),
                        TheoryExample("Nací en Moscú.", "Я родился в Москве.", "биография"),
                        TheoryExample("Vivimos allí 5 años.", "Мы прожили там 5 лет.", "закрытый период"),
                        TheoryExample("Vine, vi, vencí.", "Пришёл, увидел, победил.", "серия событий"),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 Indefinido vs Perfecto",
                    body = "В Spain: **He comido hoy** (perfecto — сегодня, рядом). **Comí ayer** (indefinido — вчера, далеко).\n\nВ Latam: indefinido «съедает» perfecto: **Comí hoy** — нормально. Это важная региональная разница."),
            ),
            keyTakeaways = listOf(
                "Завершённое действие в прошлом с конкретным временем",
                "Маркеры: ayer, anoche, en 2020, hace X días",
                "Серия событий, биография, закрытый период",
                "Не путать с Imperfecto (фон, привычки)",
            ),
            relatedTheory = listOf("u5_l1", "u6_l2"),
        ),

        "u5_l1" to TheoryContent(
            lessonId = "u5_l1",
            title = "Indefinido — глаголы на -AR",
            subtitle = "hablé / hablaste / habló / hablamos / hablasteis / hablaron.",
            emoji = "🗣️", cefr = "A2", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Окончания -AR",
                    body = "Берём основу (hablar → habl-) и добавляем окончания:\n• yo: **-é** (ударение!)\n• tú: **-aste**\n• él: **-ó** (тильда!)\n• nosotros: **-amos** (как в настоящем!)\n• vosotros: **-asteis**\n• ellos: **-aron**\n\n⚠ Внимание на тильды: hablé, habló — без них значение меняется."),
                TheorySection(TheorySectionType.TABLE, "Спряжение HABLAR",
                    table = TheoryTable(
                        headers = listOf("Лицо", "Форма"),
                        rows = listOf(
                            listOf("yo", "hablé"),
                            listOf("tú", "hablaste"),
                            listOf("él/ella/usted", "habló"),
                            listOf("nosotros", "hablamos"),
                            listOf("vosotros", "hablasteis"),
                            listOf("ellos/ustedes", "hablaron"),
                        ),
                        highlightedColumns = listOf(1),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Примеры",
                    examples = listOf(
                        TheoryExample("Ayer hablé con mi madre.", "Вчера я говорил с мамой.", ""),
                        TheoryExample("¿Estudiaste para el examen?", "Ты готовился к экзамену?", ""),
                        TheoryExample("Compró un coche nuevo.", "Он/она купил(а) новую машину.", "tilde на ó!"),
                        TheoryExample("Trabajamos hasta tarde.", "Мы работали допоздна.", ""),
                        TheoryExample("Cantaron toda la noche.", "Они пели всю ночь.", ""),
                    )),
                TheorySection(TheorySectionType.WARNING, "⚠ Орфографические изменения",
                    body = "У некоторых глаголов 1 л. ед.ч. меняется написание для сохранения звука:\n• -car → -qué: bus**car** → bus**qué** (не «busé»)\n• -gar → -gué: lle**gar** → lle**gué**\n• -zar → -cé: empe**zar** → empe**cé**\n\nЭто косметика — звук остаётся, меняется только буква."),
            ),
            keyTakeaways = listOf(
                "-é / -aste / -ó / -amos / -asteis / -aron",
                "Тильды на -é и -ó обязательны",
                "nosotros: hablamos (как в настоящем!)",
                "Орфография: -qué, -gué, -cé в yo",
            ),
            relatedTheory = listOf("u5_l2"),
        ),

        "u5_l6" to TheoryContent(
            lessonId = "u5_l6",
            title = "IR и SER в Indefinido — идентичны!",
            subtitle = "fui / fuiste / fue / fuimos / fuisteis / fueron. Один набор форм — два глагола.",
            emoji = "🌀", cefr = "A2", readMinutes = 4,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Главная странность испанского",
                    body = "В Indefinido **IR** (идти) и **SER** (быть) имеют **ОДИНАКОВЫЕ** формы:\n\n**fui / fuiste / fue / fuimos / fuisteis / fueron**\n\nКонтекст подскажет, который из двух. Это уникальная особенность испанского — выучи раз и навсегда."),
                TheorySection(TheorySectionType.TABLE, "Одна таблица — два глагола",
                    table = TheoryTable(
                        headers = listOf("Лицо", "Форма"),
                        rows = listOf(
                            listOf("yo", "fui"),
                            listOf("tú", "fuiste"),
                            listOf("él/ella/usted", "fue"),
                            listOf("nosotros", "fuimos"),
                            listOf("vosotros", "fuisteis"),
                            listOf("ellos/ustedes", "fueron"),
                        ),
                        highlightedColumns = listOf(1),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "IR vs SER — контекст решает",
                    examples = listOf(
                        TheoryExample("Fui a Madrid.", "Я ездил в Мадрид.", "IR (есть «a» + место)"),
                        TheoryExample("Fui estudiante.", "Я был студентом.", "SER (профессия/состояние)"),
                        TheoryExample("Fue a la fiesta.", "Он(а) пошёл/пошла на вечеринку.", "IR"),
                        TheoryExample("Fue muy difícil.", "Это было очень сложно.", "SER"),
                        TheoryExample("Fuimos al cine.", "Мы пошли в кино.", "IR"),
                        TheoryExample("Fuimos felices.", "Мы были счастливы.", "SER"),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 Как отличить",
                    body = "Если после **fue/fui** идёт **a + место** → это **IR**: Fue a casa.\nЕсли идёт **прилагательное / существительное-роль** → это **SER**: Fue actor. / Fue genial.\n\nНикаких тильд на этих формах — fui (не «fuí»). Запомни."),
            ),
            keyTakeaways = listOf(
                "fui = я был И я ходил/ездил",
                "Один набор форм для IR и SER в Indefinido",
                "Контекст: + a + место → IR; + adj/prof → SER",
                "Без тильд: fui, fue (не fuí, fué)",
            ),
            relatedTheory = listOf("u5_l5"),
        ),

        "u6_l0" to TheoryContent(
            lessonId = "u6_l0",
            title = "Pretérito Imperfecto -AR",
            subtitle = "hablaba / hablabas / hablaba. Привычки, описания, фон прошлого.",
            emoji = "🌅", cefr = "A2", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Когда использовать Imperfecto",
                    body = "**Imperfecto** — это «фон» прошлого, в отличие от «события» (Indefinido):\n\n1) **Привычки**: «Когда был ребёнком, я играл…»\n2) **Описание**: «Дом был большой, имел сад…»\n3) **Фон параллельный другому событию**: «Я работал, когда зазвонил телефон.»\n4) **Возраст/время в прошлом**: «Когда мне было 5 лет…»\n5) **Чувства/мысли длительные**: «Я думал, что…»"),
                TheorySection(TheorySectionType.TABLE, "Окончания -AR",
                    table = TheoryTable(
                        headers = listOf("Лицо", "Форма"),
                        rows = listOf(
                            listOf("yo", "hablaba"),
                            listOf("tú", "hablabas"),
                            listOf("él/ella/usted", "hablaba"),
                            listOf("nosotros", "hablábamos"),
                            listOf("vosotros", "hablabais"),
                            listOf("ellos/ustedes", "hablaban"),
                        ),
                        highlightedColumns = listOf(1),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "В типичных ситуациях",
                    examples = listOf(
                        TheoryExample("De niño, jugaba al fútbol.", "В детстве я играл в футбол.", "привычка"),
                        TheoryExample("La casa era grande y tenía jardín.", "Дом был большой и имел сад.", "описание"),
                        TheoryExample("Hablaba por teléfono cuando llamaste.", "Я разговаривал по телефону, когда ты позвонил.", "фон"),
                        TheoryExample("Cuando tenía 10 años…", "Когда мне было 10 лет…", "возраст"),
                        TheoryExample("Estudiábamos juntos cada día.", "Мы учились вместе каждый день.", "привычка"),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 Маркеры Imperfecto",
                    body = "Подсказки что нужен Imperfecto:\n• **siempre, nunca, todos los días, cada año**\n• **antes, mientras, cuando era niño**\n• **a menudo, normalmente, generalmente**\n\nЕсли видишь любой из них → почти наверняка Imperfecto."),
            ),
            keyTakeaways = listOf(
                "-aba / -abas / -aba / -ábamos / -abais / -aban",
                "Привычки, описания, фон, возраст",
                "Тильда на nosotros: hablábamos",
                "Маркеры: siempre, antes, todos los días",
            ),
            relatedTheory = listOf("u6_l1", "u6_l2"),
        ),

        "u6_l1" to TheoryContent(
            lessonId = "u6_l1",
            title = "Imperfecto -ER / -IR + 3 нерегуляра",
            subtitle = "comía / vivía. Всего 3 нерегулярных: ser, ir, ver.",
            emoji = "🌄", cefr = "A2", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Окончания -ER / -IR",
                    body = "Глаголы на -ER и -IR имеют **одинаковые** окончания в Imperfecto:\n\n• yo: **-ía**\n• tú: **-ías**\n• él: **-ía**\n• nosotros: **-íamos**\n• vosotros: **-íais**\n• ellos: **-ían**\n\nТильда на **í** во всех формах — важно!"),
                TheorySection(TheorySectionType.TABLE, "Три нерегулярных глагола",
                    table = TheoryTable(
                        headers = listOf("Лицо", "SER", "IR", "VER"),
                        rows = listOf(
                            listOf("yo", "era", "iba", "veía"),
                            listOf("tú", "eras", "ibas", "veías"),
                            listOf("él", "era", "iba", "veía"),
                            listOf("nos.", "éramos", "íbamos", "veíamos"),
                            listOf("vos.", "erais", "ibais", "veíais"),
                            listOf("ellos", "eran", "iban", "veían"),
                        ),
                        highlightedColumns = listOf(1, 2, 3),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Примеры",
                    examples = listOf(
                        TheoryExample("Comía mucho de niño.", "В детстве я много ел.", "comer → comía"),
                        TheoryExample("Vivíamos en Madrid.", "Мы жили в Мадриде.", "vivir → vivíamos"),
                        TheoryExample("Era estudiante en 2010.", "Я был студентом в 2010.", "ser → era"),
                        TheoryExample("Iba al gimnasio cada día.", "Я ходил в спортзал каждый день.", "ir → iba"),
                        TheoryExample("Veían la tele toda la noche.", "Они смотрели телевизор всю ночь.", "ver → veían"),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 Только 3 нерегуляра!",
                    body = "В Imperfecto всего **3** нерегулярных глагола во всём языке:\n• **ser** → era, eras, era…\n• **ir** → iba, ibas, iba…\n• **ver** → veía, veías… (формально «полу-регулярный»)\n\nВсе остальные глаголы — строго по правилам. Это самое лёгкое время в испанском!"),
            ),
            keyTakeaways = listOf(
                "-ía / -ías / -ía / -íamos / -íais / -ían",
                "Тильда на í — всегда!",
                "Только 3 нерегуляра: ser, ir, ver",
                "Самое регулярное время в испанском",
            ),
            relatedTheory = listOf("u6_l0", "u6_l2"),
        ),

        "u6_l2" to TheoryContent(
            lessonId = "u6_l2",
            title = "Indefinido vs Imperfecto — главное сравнение",
            subtitle = "Событие vs фон. «Что случилось» vs «как было».",
            emoji = "⚖", cefr = "A2", readMinutes = 6,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Двое прошедших — зачем?",
                    body = "Индоевропейские языки часто различают **aspect** (вид) — но в русском это спрятано в приставках (делал/сделал). Испанский делит явно:\n\n• **Indefinido** = СОБЫТИЕ, точка на ленте времени, завершилось.\n• **Imperfecto** = ФОН, состояние, повторение, описание.\n\nОни часто работают **вместе**: фон + событие на нём."),
                TheorySection(TheorySectionType.COMPARISON, "Сравнение",
                    comparison = TheoryComparison(
                        leftHeader = "Indefinido (СОБЫТИЕ)",
                        rightHeader = "Imperfecto (ФОН)",
                        pairs = listOf(
                            "Завершено в момент Х" to "Длится / повторяется",
                            "ayer, anoche, en 2020" to "siempre, antes, todos los días",
                            "comí, llegué, vi" to "comía, llegaba, veía",
                            "«Зазвонил телефон»" to "«Я работал»",
                            "Биография: nací, estudié" to "Описание: era alto, tenía 5 años",
                            "Конкретный момент" to "Привычка, состояние, фон",
                        ),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Вместе в одном предложении",
                    examples = listOf(
                        TheoryExample("Hablaba por teléfono cuando llegó.", "Я разговаривал, когда он пришёл.", "Imp фон + Indef событие"),
                        TheoryExample("Cuando era niño, viví en Roma 2 años.", "Когда был ребёнком, прожил 2 года в Риме.", "Imp возраст + Indef закрытый период"),
                        TheoryExample("Llovía y decidimos quedarnos.", "Шёл дождь, и мы решили остаться.", "Imp погода + Indef решение"),
                        TheoryExample("La casa era grande y tenía jardín.", "Дом был большой и имел сад.", "Чистое описание — оба Imp"),
                        TheoryExample("Ayer comí mucho.", "Вчера я много ел.", "Чистое событие — Indef"),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 Тест: «Что случилось» vs «Как было»",
                    body = "Спроси себя: «Что **случилось**?» → Indefinido (llegué, comí, vi).\n«Как **было**?» / «Что **было** правдой?» → Imperfecto (era, tenía, había).\n\nЕсли можно вставить «вдруг, в этот момент» → Indef.\nЕсли можно вставить «обычно, всё время, на тот момент» → Imp."),
            ),
            keyTakeaways = listOf(
                "Indefinido = событие, Imperfecto = фон",
                "Часто вместе: «работал, когда зазвонил»",
                "Тест: «что случилось» vs «как было»",
                "Маркеры решают: ayer→Indef, siempre→Imp",
            ),
            relatedTheory = listOf("u6_l0", "u6_l1"),
        ),

        "u6_l8" to TheoryContent(
            lessonId = "u6_l8",
            title = "Местоимения OD: lo / la / los / las",
            subtitle = "Прямое дополнение. «Я его вижу» = Lo veo.",
            emoji = "👁", cefr = "A2", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Что такое OD",
                    body = "**Objeto Directo** (прямое дополнение) — ответ на «кого/что?» Без предлога.\n\n• Veo a Juan. → Lo veo. (вижу его)\n• Veo a María. → La veo. (вижу её)\n• Compro libros. → Los compro.\n• Compro manzanas. → Las compro.\n\nМестоимения 1/2 лица: **me, te, nos, os** (одинаковы для OD и OI)."),
                TheorySection(TheorySectionType.TABLE, "Формы",
                    table = TheoryTable(
                        headers = listOf("Лицо", "OD", "Пример"),
                        rows = listOf(
                            listOf("меня", "me", "Me ves"),
                            listOf("тебя", "te", "Te oigo"),
                            listOf("его/Вас(м)", "lo", "Lo conozco"),
                            listOf("её/Вас(ж)", "la", "La quiero"),
                            listOf("нас", "nos", "Nos invitan"),
                            listOf("вас", "os", "Os llamo"),
                            listOf("их (м)/Вас мн.", "los", "Los compro"),
                            listOf("их (ж)", "las", "Las veo"),
                        ),
                        highlightedColumns = listOf(1),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Позиция местоимения",
                    examples = listOf(
                        TheoryExample("Lo veo.", "Я его вижу.", "перед спряг. глаголом"),
                        TheoryExample("No lo veo.", "Не вижу его.", "после no"),
                        TheoryExample("Voy a verlo.", "Я собираюсь его увидеть.", "приклеилось к инф."),
                        TheoryExample("Lo voy a ver.", "Я собираюсь его увидеть.", "альтернатива — перед vamos"),
                        TheoryExample("Estoy viéndolo.", "Я смотрю на него.", "к герундию (с тильдой!)"),
                        TheoryExample("¡Cómpralo!", "Купи его!", "к утвердит. императиву"),
                    )),
                TheorySection(TheorySectionType.WARNING, "⚠ Leísmo (Spain)",
                    body = "В **Madrid Spanish** часто используют **le** вместо **lo** для людей-мужчин: «Le veo» = «Lo veo». Это **leísmo de persona** — принято RAE.\n\nВ Latam и грамматически строго: **lo veo** (его — мужчину).\n\nДля **вещей** всегда **lo/la** (никогда le)."),
            ),
            keyTakeaways = listOf(
                "OD = «кого/что?», без предлога",
                "lo/la/los/las (3 лицо), me/te/nos/os (1-2)",
                "Перед спряг. глаг., приклеено к inf/ger/imp+",
                "Leísmo в Spain — le вместо lo для мужчин",
            ),
            relatedTheory = listOf("u6_l9"),
        ),

        "u6_l9" to TheoryContent(
            lessonId = "u6_l9",
            title = "Местоимения OI: le / les",
            subtitle = "Косвенное дополнение. «Я ему дарю» = Le doy. Leísmo Spain vs Latam.",
            emoji = "🎁", cefr = "A2", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Что такое OI",
                    body = "**Objeto Indirecto** (косвенное дополнение) — ответ на «кому?/чему?». Часто с предлогом «a».\n\n• Doy un libro **a Juan**. → **Le** doy un libro.\n• Doy un libro **a María**. → **Le** doy un libro.\n• Doy libros **a mis amigos**. → **Les** doy libros.\n\nВажно: **le** одинаков для м.р. И ж.р. (в отличие от lo/la)."),
                TheorySection(TheorySectionType.TABLE, "Все формы OI",
                    table = TheoryTable(
                        headers = listOf("Лицо", "OI", "Пример"),
                        rows = listOf(
                            listOf("мне", "me", "Me das un regalo"),
                            listOf("тебе", "te", "Te digo la verdad"),
                            listOf("ему/ей/Вам", "le", "Le doy las llaves"),
                            listOf("нам", "nos", "Nos enseña español"),
                            listOf("вам", "os", "Os explico todo"),
                            listOf("им/Вам мн.", "les", "Les escribo un email"),
                        ),
                        highlightedColumns = listOf(1),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Le vs lo (важно!)",
                    examples = listOf(
                        TheoryExample("Le doy el libro a Juan.", "Я даю книгу Хуану.", "le = ему (OI)"),
                        TheoryExample("Lo veo en la calle.", "Я вижу его на улице.", "lo = его (OD)"),
                        TheoryExample("Les compré flores a mis padres.", "Я купил цветы родителям.", "les = им"),
                        TheoryExample("¿Me ayudas?", "Поможешь мне?", "me = OI (ayudar a alguien)"),
                        TheoryExample("Le gustan los gatos.", "Ему нравятся коты.", "gustar — всегда OI"),
                    )),
                TheorySection(TheorySectionType.WARNING, "⚠ Spain vs Latam: leísmo",
                    body = "В **Spain**: для мужчины-человека часто **le** заменяет **lo** (даже если OD):\n• Madrid: «**Le** vi en el bar» (его видел)\n• Latam: «**Lo** vi en el bar»\n\nRAE одобряет ТОЛЬКО **leísmo de persona masculino singular**. Всё остальное (la→le, los→les) — ошибка.\n\nДля вещей: ВСЕГДА lo/la, никогда le."),
            ),
            keyTakeaways = listOf(
                "OI = «кому/чему?», часто с «a»",
                "le (ед.) / les (мн.) — один для м.р. и ж.р.",
                "Gustar, doler, encantar — всегда OI",
                "Leísmo: Spain le ↔ Latam lo (для мужчин)",
            ),
            relatedTheory = listOf("u6_l8"),
        ),

        "u7_l0" to TheoryContent(
            lessonId = "u7_l0",
            title = "Pretérito Perfecto Compuesto",
            subtitle = "He comido / has visto / ha llegado. Действие связано с настоящим.",
            emoji = "✅", cefr = "A2", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Формула: HABER + participio",
                    body = "**HABER** (вспом. глагол) **+ participio pasado**:\n\n• he, has, ha, hemos, habéis, han\n• + причастие на **-ado** (для -AR): hablado, comprado\n• + причастие на **-ido** (для -ER/-IR): comido, vivido\n\nПричастие НЕ согласуется (в отличие от французского/итальянского). Всегда одна форма."),
                TheorySection(TheorySectionType.TABLE, "Спряжение HABER",
                    table = TheoryTable(
                        headers = listOf("Лицо", "HABER", "Полная форма"),
                        rows = listOf(
                            listOf("yo", "he", "he comido"),
                            listOf("tú", "has", "has comido"),
                            listOf("él/ella/usted", "ha", "ha comido"),
                            listOf("nosotros", "hemos", "hemos comido"),
                            listOf("vosotros", "habéis", "habéis comido"),
                            listOf("ellos/ustedes", "han", "han comido"),
                        ),
                        highlightedColumns = listOf(1, 2),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Когда использовать (Spain)",
                    examples = listOf(
                        TheoryExample("Hoy he comido pizza.", "Сегодня я ел пиццу.", "сегодня — рядом"),
                        TheoryExample("Esta semana hemos trabajado mucho.", "На этой неделе мы много работали.", "недавний период"),
                        TheoryExample("Ya he terminado.", "Я уже закончил.", "результат сейчас"),
                        TheoryExample("Nunca he estado en Japón.", "Я никогда не был в Японии.", "жизненный опыт"),
                        TheoryExample("¿Has visto la película?", "Ты видел этот фильм?", "опыт"),
                    )),
                TheorySection(TheorySectionType.WARNING, "⚠ Нерегулярные participios",
                    body = "Несколько глаголов имеют нерегулярное причастие:\n• abrir → **abierto**\n• decir → **dicho**\n• escribir → **escrito**\n• hacer → **hecho**\n• poner → **puesto**\n• ver → **visto**\n• volver → **vuelto**\n• romper → **roto**\n• morir → **muerto**\n\nЗапомнить как «топ-9 неправильных».\n\nВ **Latam** indefinido «съедает» perfecto: «Hoy comí» вместо «He comido». В Spain perfecto живёт."),
            ),
            keyTakeaways = listOf(
                "haber + participio (-ado/-ido)",
                "Сегодня, недавно, опыт, результат",
                "9 нерегулярных: abierto, dicho, hecho, visto…",
                "Spain: жив; Latam: indef. чаще",
            ),
            relatedTheory = listOf("u5_l0", "u7_l1"),
        ),

        "u8_l0" to TheoryContent(
            lessonId = "u8_l0",
            title = "Futuro Simple — будущее простое",
            subtitle = "hablaré / hablarás / hablará. Одна формула на все спряжения.",
            emoji = "🔮", cefr = "A2", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Формула: инфинитив + окончания",
                    body = "Futuro Simple — самое простое время. К **полному инфинитиву** (не основе!) добавляем окончания:\n\n• -**é**, -**ás**, -**á**, -**emos**, -**éis**, -**án**\n\nОдинаково для -AR, -ER, -IR. Тильды на 1, 2, 3, 6 формах."),
                TheorySection(TheorySectionType.TABLE, "Спряжение HABLAR",
                    table = TheoryTable(
                        headers = listOf("Лицо", "Форма"),
                        rows = listOf(
                            listOf("yo", "hablaré"),
                            listOf("tú", "hablarás"),
                            listOf("él/ella/usted", "hablará"),
                            listOf("nosotros", "hablaremos"),
                            listOf("vosotros", "hablaréis"),
                            listOf("ellos/ustedes", "hablarán"),
                        ),
                        highlightedColumns = listOf(1),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Применение",
                    examples = listOf(
                        TheoryExample("Mañana hablaré con ella.", "Завтра я поговорю с ней.", "будущее"),
                        TheoryExample("¿Vendrás a la fiesta?", "Ты придёшь на вечеринку?", "venir → vendr-"),
                        TheoryExample("Estaré en Madrid en julio.", "Я буду в Мадриде в июле.", ""),
                        TheoryExample("Serán las diez.", "Сейчас, наверное, 10.", "догадка (!)"),
                        TheoryExample("¿Quién será?", "Кто бы это мог быть?", "догадка о настоящем"),
                    )),
                TheorySection(TheorySectionType.WARNING, "⚠ 12 нерегулярных основ",
                    body = "12 глаголов меняют основу (НЕ окончания!):\n• decir → **dir-** (diré, dirás…)\n• hacer → **har-**\n• poder → **podr-**\n• poner → **pondr-**\n• querer → **querr-**\n• saber → **sabr-**\n• salir → **saldr-**\n• tener → **tendr-**\n• valer → **valdr-**\n• venir → **vendr-**\n• haber → **habr-**\n• caber → **cabr-**\n\nОкончания те же. Альтернатива: **ir a + inf** (бытовое будущее)."),
            ),
            keyTakeaways = listOf(
                "Инфинитив + -é/-ás/-á/-emos/-éis/-án",
                "Тильды на 4 формах из 6",
                "12 нерегулярных основ (tendré, vendré, haré)",
                "Также для догадок: «Serán las 10» = «Наверное, 10»",
            ),
            relatedTheory = listOf("u8_l1"),
        ),

        // ───────── B1 (10) ─────────

        "u9_l0" to TheoryContent(
            lessonId = "u9_l0",
            title = "Subjuntivo — введение",
            subtitle = "Не «время», а наклонение: желание, сомнение, эмоция, гипотеза.",
            emoji = "🌙", cefr = "B1", readMinutes = 6,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Что такое subjuntivo",
                    body = "Это НЕ время, а **наклонение** — параллельное Indicativo.\n\n**Indicativo** = факты, реальность: «Я знаю, что он работает.»\n**Subjuntivo** = желания, сомнения, эмоции, гипотезы: «Хочу, чтобы он работал. Сомневаюсь, что он работает.»\n\nТриггеры subjuntivo (запомни эти «домены»):\n• **Воля/желание**: quiero que, espero que, ojalá\n• **Эмоции**: me alegra que, me da pena que\n• **Сомнения**: dudo que, no creo que\n• **Безличные оценки**: es importante que, es posible que\n• **Цель**: para que, a fin de que"),
                TheorySection(TheorySectionType.COMPARISON, "Indicativo vs Subjuntivo",
                    comparison = TheoryComparison(
                        leftHeader = "Indicativo (факт)",
                        rightHeader = "Subjuntivo (отношение)",
                        pairs = listOf(
                            "Sé que viene." to "Quiero que venga.",
                            "Creo que es verdad." to "No creo que sea verdad.",
                            "Veo que trabaja." to "Me alegra que trabaje.",
                            "Es verdad que llueve." to "Es posible que llueva.",
                        ),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Простейшие триггеры",
                    examples = listOf(
                        TheoryExample("Quiero que vengas.", "Хочу, чтобы ты пришёл.", "querer que → subj"),
                        TheoryExample("Espero que estés bien.", "Надеюсь, у тебя всё хорошо.", "esperar que → subj"),
                        TheoryExample("Ojalá llueva.", "Хоть бы дождь пошёл.", "ojalá → subj"),
                        TheoryExample("Es importante que estudies.", "Важно, чтобы ты учился.", "безл. оценка"),
                        TheoryExample("Dudo que sea verdad.", "Сомневаюсь, что это правда.", "сомнение"),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 Главная подсказка",
                    body = "**«Que»** — это лакмусовая бумажка subjuntivo. Если перед que стоит триггер из «доменов» (воля/эмоция/сомнение) → subj.\n\nЕсли просто «creo que / sé que / es verdad que» (утверждение факта) → indic.\n\nЭто наклонение — главное препятствие на пути к B1. Не пугайся — освоишь блок за блоком."),
            ),
            keyTakeaways = listOf(
                "Subjuntivo = наклонение для не-фактов",
                "5 доменов: воля, эмоция, сомнение, оценка, цель",
                "Триггер обычно перед «que»",
                "Indicativo = факт, Subjuntivo = отношение",
            ),
            relatedTheory = listOf("u9_l1"),
        ),

        "u9_l1" to TheoryContent(
            lessonId = "u9_l1",
            title = "Presente de Subjuntivo — -AR",
            subtitle = "hable / hables / hable. Окончания «противоположных» гласных.",
            emoji = "🌙", cefr = "B1", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Правило «обмена гласных»",
                    body = "Простой трюк: глаголы на **-AR** в subjuntivo получают окончания на **-e** (как у -ER в Indicativo).\nГлаголы на -ER/-IR получают окончания на **-a**.\n\nДля -AR:\n• yo: **-e**\n• tú: **-es**\n• él: **-e**\n• nosotros: **-emos**\n• vosotros: **-éis**\n• ellos: **-en**\n\nБерём основу от **yo** в индикативе: hablo → habl-. Это работает и для нерегуляров (tengo → tenga, salgo → salga)."),
                TheorySection(TheorySectionType.TABLE, "Спряжение HABLAR (-AR)",
                    table = TheoryTable(
                        headers = listOf("Лицо", "Subjuntivo", "Indicativo"),
                        rows = listOf(
                            listOf("yo", "hable", "hablo"),
                            listOf("tú", "hables", "hablas"),
                            listOf("él", "hable", "habla"),
                            listOf("nosotros", "hablemos", "hablamos"),
                            listOf("vosotros", "habléis", "habláis"),
                            listOf("ellos", "hablen", "hablan"),
                        ),
                        highlightedColumns = listOf(1),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Применение",
                    examples = listOf(
                        TheoryExample("Quiero que hables más.", "Хочу, чтобы ты говорил больше.", "querer que + subj"),
                        TheoryExample("Es necesario que estudie.", "Нужно, чтобы я учился.", ""),
                        TheoryExample("Ojalá compre la casa.", "Хоть бы он купил дом.", ""),
                        TheoryExample("Espero que cantes en la fiesta.", "Надеюсь, ты споёшь на вечеринке.", ""),
                        TheoryExample("No creo que trabajen tanto.", "Не верю, что они так много работают.", "сомнение"),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 Орфография как в Indef",
                    body = "Те же мелкие изменения, что и в Indef yo-форме:\n• -**car** → -**que**: bus**car** → busque, busques…\n• -**gar** → -**gue**: lle**gar** → llegue\n• -**zar** → -**ce**: empe**zar** → empiece\n\nЭто косметика для сохранения звука."),
            ),
            keyTakeaways = listOf(
                "-AR глаголы → окончания на -e",
                "Берём основу от yo индикатива (hablo → habl-)",
                "Тильда только на vosotros (habléis)",
                "Орфография: -que/-gue/-ce",
            ),
            relatedTheory = listOf("u9_l0", "u9_l2"),
        ),

        "u9_l3" to TheoryContent(
            lessonId = "u9_l3",
            title = "Нерегулярные Subjuntivo: sea, vaya, tenga",
            subtitle = "ser → sea, ir → vaya, tener → tenga. Топ-6 нерегулярных.",
            emoji = "⚡", cefr = "B1", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Логика нерегулярности",
                    body = "Большинство нерегулярных subjuntivo берётся от **yo** в индикативе и применяет правила:\n• tengo → teng- → **tenga, tengas, tenga…**\n• hago → hag- → **haga, hagas, haga…**\n• digo → dig- → **diga, digas, diga…**\n\nНо 6 глаголов **полностью нерегулярны** — учим формы наизусть."),
                TheorySection(TheorySectionType.TABLE, "Топ-6 «диких» субъюнктивов",
                    table = TheoryTable(
                        headers = listOf("Глагол", "yo", "tú", "él/nosotros"),
                        rows = listOf(
                            listOf("ser", "sea", "seas", "sea / seamos"),
                            listOf("estar", "esté", "estés", "esté / estemos"),
                            listOf("ir", "vaya", "vayas", "vaya / vayamos"),
                            listOf("haber", "haya", "hayas", "haya / hayamos"),
                            listOf("saber", "sepa", "sepas", "sepa / sepamos"),
                            listOf("dar", "dé", "des", "dé / demos"),
                        ),
                        highlightedColumns = listOf(1, 2, 3),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "В речи",
                    examples = listOf(
                        TheoryExample("Quiero que seas feliz.", "Хочу, чтобы ты был счастлив.", "ser → sea"),
                        TheoryExample("Espero que vaya bien.", "Надеюсь, всё пройдёт хорошо.", "ir → vaya"),
                        TheoryExample("Es importante que tengas paciencia.", "Важно, чтобы у тебя было терпение.", "tener → tenga"),
                        TheoryExample("Ojalá esté en casa.", "Хоть бы он был дома.", "estar → esté"),
                        TheoryExample("No creo que sepan la respuesta.", "Не думаю, что они знают ответ.", "saber → sepan"),
                        TheoryExample("Dudo que haya nieve.", "Сомневаюсь, что есть снег.", "haber → haya"),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 Тильды на 1 ед. и 3 ед.",
                    body = "У глагола **dar**: 1 л. ед.ч. = **dé** (с тильдой!), а 1 л. мн. = **demos** (без).\nУ **estar**: yo / él **esté** (с тильдой), nosotros **estemos** (без).\n\nТильда нужна чтобы отличать от других слов: dé (от dar) ≠ de (предлог)."),
            ),
            keyTakeaways = listOf(
                "Большинство нерегуляров: от yo + правила",
                "6 «диких»: ser, estar, ir, haber, saber, dar",
                "sea / esté / vaya / haya / sepa / dé",
                "Тильда: dé, esté (отличие от de, este)",
            ),
            relatedTheory = listOf("u9_l1", "u9_l2"),
        ),

        "u9_l6" to TheoryContent(
            lessonId = "u9_l6",
            title = "Триггеры воли: quiero que, espero que",
            subtitle = "Когда субъект1 хочет/просит/советует субъекту2 что-то сделать.",
            emoji = "🎯", cefr = "B1", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Главное правило воли",
                    body = "Если **первый субъект** хочет/просит/требует, чтобы **второй субъект** что-то сделал → **que + subjuntivo**.\n\nЕсли субъекты СОВПАДАЮТ → **инфинитив** (без que!):\n• Quiero ir. = Я хочу пойти. (один субъект)\n• Quiero que vayas. = Хочу, чтобы ты пошёл. (разные субъекты)"),
                TheorySection(TheorySectionType.TABLE, "Топ-триггеры воли",
                    table = TheoryTable(
                        headers = listOf("Триггер", "Значение"),
                        rows = listOf(
                            listOf("querer que", "хотеть, чтобы"),
                            listOf("desear que", "желать, чтобы"),
                            listOf("esperar que", "надеяться, что"),
                            listOf("pedir que", "просить, чтобы"),
                            listOf("decir que (приказ)", "велеть"),
                            listOf("aconsejar que", "советовать"),
                            listOf("prohibir que", "запрещать"),
                            listOf("recomendar que", "рекомендовать"),
                            listOf("ojalá (que)", "хоть бы"),
                        ),
                        highlightedColumns = listOf(0),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Один субъект vs разные",
                    examples = listOf(
                        TheoryExample("Quiero estudiar.", "Хочу учиться.", "тот же субъект → inf"),
                        TheoryExample("Quiero que estudies.", "Хочу, чтобы ты учился.", "другой субъект → que+subj"),
                        TheoryExample("Espero ganar.", "Надеюсь выиграть.", "тот же"),
                        TheoryExample("Espero que ganes.", "Надеюсь, ты выиграешь.", "другой"),
                        TheoryExample("Te pido que vengas.", "Прошу тебя прийти.", ""),
                        TheoryExample("Ojalá llueva mañana.", "Хоть бы завтра пошёл дождь.", "ojalá ВСЕГДА + subj"),
                    )),
                TheorySection(TheorySectionType.WARNING, "⚠ Decir: совет vs информация",
                    body = "**Decir que** имеет 2 значения:\n• «Сообщать» (информация) → **indicativo**: Dice que viene. (Говорит, что приходит.)\n• «Велеть» (приказ) → **subjuntivo**: Dice que vengas. (Велит, чтобы ты пришёл.)\n\nКонтекст решает. Если можно подменить «приказывает» → subj."),
            ),
            keyTakeaways = listOf(
                "Один субъект → инфинитив (Quiero ir)",
                "Разные субъекты → que + subj (Quiero que vayas)",
                "Ojalá — всегда subjuntivo",
                "Decir: «сообщать» indic / «велеть» subj",
            ),
            relatedTheory = listOf("u9_l1", "u9_l3"),
        ),

        "u9_l9" to TheoryContent(
            lessonId = "u9_l9",
            title = "Сомнение и отрицание: creer que / no creer que",
            subtitle = "Утверждение → indic. Отрицание / сомнение → subj.",
            emoji = "🤔", cefr = "B1", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Правило креативного перевёртыша",
                    body = "Глаголы **creer / pensar / suponer / parecer** в утверждении выражают убеждение → **indicativo**:\n• **Creo que viene.** (Думаю, он придёт.)\n\nНо в **отрицании** или **вопросе с сомнением** → **subjuntivo**:\n• **No creo que venga.** (Не думаю, что придёт.)\n• **¿Crees que venga?** (Думаешь, придёт?) — если сомневаешься\n\nТак же работают **dudar que** (всегда subj), **negar que** (всегда subj)."),
                TheorySection(TheorySectionType.COMPARISON, "Утверждение vs Сомнение",
                    comparison = TheoryComparison(
                        leftHeader = "Indicativo (уверен)",
                        rightHeader = "Subjuntivo (сомнение)",
                        pairs = listOf(
                            "Creo que es verdad." to "No creo que sea verdad.",
                            "Pienso que viene." to "No pienso que venga.",
                            "Estoy seguro de que llega." to "No estoy seguro de que llegue.",
                            "Es verdad que estudia." to "No es verdad que estudie.",
                            "Es obvio que sabe." to "No es obvio que sepa.",
                        ),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "В контексте",
                    examples = listOf(
                        TheoryExample("Creo que está en casa.", "Думаю, он дома.", "уверен → indic"),
                        TheoryExample("No creo que esté en casa.", "Не думаю, что он дома.", "не уверен → subj"),
                        TheoryExample("Dudo que tenga razón.", "Сомневаюсь, что он прав.", "dudar → всегда subj"),
                        TheoryExample("Niego que sea verdad.", "Отрицаю, что это правда.", "negar → subj"),
                        TheoryExample("Es posible que llueva.", "Возможно, пойдёт дождь.", "possible → subj"),
                        TheoryExample("Es seguro que viene.", "Точно придёт.", "seguro → indic"),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 «Tal vez / quizás / probablemente»",
                    body = "Это «гибкие» триггеры — могут идти и с indic, и с subj в зависимости от вашей уверенности:\n• Quizás **viene** (вероятно — больше уверен).\n• Quizás **venga** (может быть — меньше уверен).\n\nВ современной речи subj чаще. Используй subj как безопасный выбор."),
            ),
            keyTakeaways = listOf(
                "Creo que + indic / No creo que + subj",
                "Dudar / negar — всегда subj",
                "Es posible/probable/imposible que → subj",
                "Tal vez/quizás — гибкие (чаще subj)",
            ),
            relatedTheory = listOf("u9_l6"),
        ),

        "u9_l12" to TheoryContent(
            lessonId = "u9_l12",
            title = "Cuando + indicativo vs subjuntivo",
            subtitle = "Привычка / прошлое → indic. Будущее → subj.",
            emoji = "⏰", cefr = "B1", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Простое правило временных союзов",
                    body = "**Cuando, en cuanto, mientras, hasta que, tan pronto como** + время:\n\n• **Привычка / прошлое** (уже случилось / случается обычно) → **indicativo**:\n  Cuando llego a casa, ceno. (Когда прихожу домой, ужинаю — привычка.)\n  Cuando llegué, cené. (Когда пришёл, поужинал.)\n\n• **Будущее** (ещё не случилось — гипотеза) → **subjuntivo**:\n  Cuando llegue a casa, cenaré. (Когда приду домой [потом], поужинаю.)"),
                TheorySection(TheorySectionType.COMPARISON, "Привычка vs Будущее",
                    comparison = TheoryComparison(
                        leftHeader = "Indicativo (привычка/прошлое)",
                        rightHeader = "Subjuntivo (будущее)",
                        pairs = listOf(
                            "Cuando llueve, llevo paraguas." to "Cuando llueva, llevaré paraguas.",
                            "Cuando llegó, cenó." to "Cuando llegue, cenará.",
                            "En cuanto termina, llama." to "En cuanto termine, llamará.",
                            "Mientras estudio, escucho música." to "Mientras estudies, no salgas.",
                        ),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Применение",
                    examples = listOf(
                        TheoryExample("Cuando voy a Madrid, visito el Prado.", "Когда езжу в Мадрид, посещаю Прадо.", "привычка → indic"),
                        TheoryExample("Cuando vaya a Madrid, visitaré el Prado.", "Когда поеду в Мадрид [как-нибудь], посещу Прадо.", "будущее → subj"),
                        TheoryExample("Llámame cuando llegues.", "Позвони мне, когда придёшь.", "будущее"),
                        TheoryExample("Hasta que no termines, no comas.", "Пока не закончишь, не ешь.", ""),
                        TheoryExample("En cuanto pueda, te aviso.", "Как только смогу, дам знать.", ""),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 Тест: «уже / ещё нет»",
                    body = "Спроси: «Это **уже** происходит / происходило / привычка?» → **indicativo**.\n«Это **ещё не** случилось — гипотетическое будущее?» → **subjuntivo**.\n\nТо же касается **aunque, donde, como, según** — гибкие, зависит от факт vs гипотеза."),
            ),
            keyTakeaways = listOf(
                "Cuando + indic (привычка/прошлое)",
                "Cuando + subj (будущее ещё не случилось)",
                "En cuanto / hasta que / mientras — то же правило",
                "Тест: уже случилось? → indic. Ещё нет? → subj.",
            ),
            relatedTheory = listOf("u9_l11", "u10_l5"),
        ),

        "u10_l5" to TheoryContent(
            lessonId = "u10_l5",
            title = "Условные «Si» — Тип 1: реальное условие",
            subtitle = "Si + presente + presente/futuro/imperativo. «Если сделаешь — получишь».",
            emoji = "1️⃣", cefr = "B1", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Формула типа 1",
                    body = "**Si + presente indicativo, + presente / futuro / imperativo.**\n\nИспользуется для **реальных условий** — то, что может произойти:\n• Si estudias, apruebas. (Если учишься, сдаёшь.)\n• Si estudias, aprobarás. (Если будешь учиться, сдашь.)\n• Si tienes hambre, come. (Если голоден, ешь.)\n\n⚠ **НИКОГДА** после «si» (тип 1) не ставится subjuntivo и не ставится futuro. Только indic. presente."),
                TheorySection(TheorySectionType.TABLE, "Структура",
                    table = TheoryTable(
                        headers = listOf("Если-часть", "Результат"),
                        rows = listOf(
                            listOf("Si + presente", "presente"),
                            listOf("Si + presente", "futuro"),
                            listOf("Si + presente", "imperativo"),
                        ),
                        highlightedColumns = listOf(0, 1),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Все варианты",
                    examples = listOf(
                        TheoryExample("Si llueve, me quedo en casa.", "Если идёт дождь, остаюсь дома.", "pres + pres"),
                        TheoryExample("Si llueve, me quedaré en casa.", "Если пойдёт дождь, останусь дома.", "pres + fut"),
                        TheoryExample("Si llueve, quédate en casa.", "Если идёт дождь, останься дома.", "pres + imper"),
                        TheoryExample("Si tienes tiempo, llámame.", "Если будет время, позвони.", ""),
                        TheoryExample("Si quieres, vamos juntos.", "Если хочешь, пойдём вместе.", ""),
                    )),
                TheorySection(TheorySectionType.WARNING, "⚠ Никогда «si comerás»",
                    body = "**Главная ошибка**: после «si» в типе 1 ставится **только presente indicativo**, никогда не futuro!\n\n❌ Si **comerás** mucho…\n✅ Si **comes** mucho…\n\nЭто отличает испанский от русского/английского. «Если будешь есть» = «Si comes», а не «Si comerás»."),
            ),
            keyTakeaways = listOf(
                "Si + presente, + presente/futuro/imperativo",
                "Реальное, возможное условие",
                "После si — никогда subj, никогда fut",
                "Порядок частей свободный (Si X, Y / Y si X)",
            ),
            relatedTheory = listOf("u10_l9"),
        ),

        "u10_l9" to TheoryContent(
            lessonId = "u10_l9",
            title = "Условные «Si» — Тип 2: гипотеза",
            subtitle = "Si + Imperfecto Subjuntivo + Condicional. «Если бы — то бы».",
            emoji = "2️⃣", cefr = "B1", readMinutes = 6,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Формула типа 2",
                    body = "**Si + Imperfecto Subjuntivo, + Condicional Simple.**\n\nИспользуется для **гипотезы** или **противоречия реальности** в настоящем/будущем:\n• Si tuviera dinero, viajaría. (Если бы у меня были деньги, я бы путешествовал.)\n• Si fueras yo, ¿qué harías? (Если бы ты был мной, что бы ты делал?)\n\nImperfecto Subjuntivo: **-ara/-aras/-ara** (для -AR) или **-iera/-ieras/-iera** (для -ER/-IR).\nCondicional: инфинитив + **-ía/-ías/-ía/-íamos/-íais/-ían**."),
                TheorySection(TheorySectionType.TABLE, "Структура",
                    table = TheoryTable(
                        headers = listOf("Если-часть", "Результат"),
                        rows = listOf(
                            listOf("Si + Imp.Subj (-ara/-iera)", "Condicional (-ía)"),
                        ),
                        highlightedColumns = listOf(0, 1),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Применение",
                    examples = listOf(
                        TheoryExample("Si tuviera tiempo, te ayudaría.", "Если бы было время, я бы помог.", "tener → tuviera"),
                        TheoryExample("Si fuera rico, compraría una casa grande.", "Если бы был богат, купил бы большой дом.", "ser → fuera"),
                        TheoryExample("Si pudieras viajar, ¿adónde irías?", "Если бы мог путешествовать, куда бы поехал?", "poder → pudieras"),
                        TheoryExample("Si supiera la respuesta, te la diría.", "Если бы знал ответ, сказал бы.", "saber → supiera"),
                        TheoryExample("Si vivieras aquí, sería más fácil.", "Если бы ты жил здесь, было бы проще.", ""),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 Imperfecto Subjuntivo формы",
                    body = "Берётся от 3 л. мн.ч. Indefinido, отбрасывается -ron и добавляется -ra/-ras/-ra/-´ramos/-rais/-ran:\n• hablaron → habla-ra, habla-ras, habla-ra…\n• comieron → comie-ra, comie-ras…\n• tuvieron → tuvie-ra, tuvie-ras…\n• fueron → fue-ra, fue-ras…\n\nЕсть параллельная форма с -se (-ase/-iese) — литературная, реже в речи."),
            ),
            keyTakeaways = listOf(
                "Si + Imp.Subj + Condicional",
                "Гипотеза или противоречие настоящему",
                "tuviera, fuera, pudiera, supiera",
                "Альтернативная форма: -ase/-iese (книжная)",
            ),
            relatedTheory = listOf("u10_l5", "u10_l9_5"),
        ),

        "u10_l9_5" to TheoryContent(
            lessonId = "u10_l9_5",
            title = "Условные «Si» — Тип 3: упущенный шанс",
            subtitle = "Si + Pluscuamp Subj + Cond Compuesto. «Если бы было — то было бы».",
            emoji = "3️⃣", cefr = "B1", readMinutes = 6,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Формула типа 3",
                    body = "**Si + Pluscuamperfecto Subjuntivo, + Condicional Compuesto.**\n\nИспользуется для **прошлого, которое не случилось** — упущенные шансы, сожаления:\n• Si hubiera estudiado, habría aprobado. (Если бы я учился, я бы сдал — но не учился и не сдал.)\n\nФормулы вспомогательных:\n• Pluscuamp.Subj: **hubiera/hubieras… + participio**\n• Cond.Compuesto: **habría/habrías… + participio**"),
                TheorySection(TheorySectionType.TABLE, "Структура и вспомогательные",
                    table = TheoryTable(
                        headers = listOf("Часть", "Формула", "Пример"),
                        rows = listOf(
                            listOf("Si-часть", "hubiera/-ras/-ra... + part.", "hubiera estudiado"),
                            listOf("Результат", "habría/-as/-a... + part.", "habría aprobado"),
                        ),
                        highlightedColumns = listOf(1),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Сожаления о прошлом",
                    examples = listOf(
                        TheoryExample("Si hubiera estudiado, habría aprobado.", "Если бы я учился, я бы сдал.", "не учился → не сдал"),
                        TheoryExample("Si me lo hubieras dicho, te habría ayudado.", "Если бы ты мне сказал, я бы помог.", "не сказал → не помог"),
                        TheoryExample("Si hubiéramos salido antes, no habríamos perdido el tren.", "Если бы мы вышли раньше, не опоздали бы на поезд.", ""),
                        TheoryExample("Si no hubiera llovido, habríamos ido al parque.", "Если бы не пошёл дождь, мы бы пошли в парк.", ""),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 Смешанный тип (тип 4)",
                    body = "Можно смешивать **прошлое условие + настоящий результат**:\n• Si hubiera estudiado más, **ahora tendría** un mejor trabajo. (Если бы я больше учился в прошлом, сейчас у меня была бы лучшая работа.)\n\nЭто Si + Pluscuamp.Subj + **Condicional Simple** (а не compuesto). Прошлое влияет на настоящее.\n\nВ разговорной речи иногда используется **hubiera ... hubiera** в обеих частях («Si hubiera sabido, hubiera venido») — приемлемо в Latam, формально неправильно."),
            ),
            keyTakeaways = listOf(
                "Si + hubiera + part., + habría + part.",
                "Упущенный шанс в прошлом",
                "Тип 4 (mixed): hubiera + Condicional Simple (для настоящего)",
                "Hubiera ↔ habría — взаимозаменяемы в Latam",
            ),
            relatedTheory = listOf("u10_l5", "u10_l9"),
        ),

        "u11_l5_6" to TheoryContent(
            lessonId = "u11_l5_6",
            title = "Косвенные вопросы: Me preguntó si / dónde / qué",
            subtitle = "Я спросил, придёт ли он. Перевод вопроса в придаточное.",
            emoji = "❓", cefr = "B1", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Прямой vs косвенный вопрос",
                    body = "**Прямой**: «¿Vas a venir?»\n**Косвенный**: «Me preguntó **si voy a venir**.»\n\nПравила:\n1) Вопрос **да/нет** → **si** (если, ли): ¿Vienes? → Me preguntó si vengo.\n2) Вопрос с **qué/dónde/cuándo/cómo/por qué/quién** → то же слово (с тильдой!): ¿Dónde vives? → Me preguntó dónde vivo.\n3) **Времена сдвигаются назад** при пересказе в прошлом."),
                TheorySection(TheorySectionType.TABLE, "Сдвиг времён",
                    table = TheoryTable(
                        headers = listOf("Прямая речь", "Косвенная (в прошлом)"),
                        rows = listOf(
                            listOf("Presente (vengo)", "Imperfecto (venía)"),
                            listOf("Indef (vine)", "Pluscuamp (había venido)"),
                            listOf("Futuro (vendré)", "Condicional (vendría)"),
                            listOf("Imperativo (¡Ven!)", "Imperf.Subj (que viniera)"),
                        ),
                        highlightedColumns = listOf(1),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Конкретные сдвиги",
                    examples = listOf(
                        TheoryExample("«¿Trabajas?» → Me preguntó si trabajaba.", "«Ты работаешь?» — Спросил, работаю ли.", "Pres → Imp"),
                        TheoryExample("«¿Dónde vives?» → Me preguntó dónde vivía.", "«Где живёшь?» — Спросил, где живу.", "тильда сохраняется!"),
                        TheoryExample("«¿Qué hiciste?» → Me preguntó qué había hecho.", "«Что делал?» — Спросил, что я делал.", "Indef → Pluscuamp"),
                        TheoryExample("«¿Vendrás?» → Me preguntó si vendría.", "«Придёшь?» — Спросил, приду ли.", "Fut → Cond"),
                        TheoryExample("«¡Ven aquí!» → Me dijo que viniera.", "«Иди сюда!» — Велел, чтобы я пришёл.", "Imp → Imp.Subj"),
                    )),
                TheorySection(TheorySectionType.WARNING, "⚠ Тильда сохраняется",
                    body = "В косвенных вопросах **тильда на qué/dónde/cómo/cuándo/quién** **сохраняется**! Это знак вопроса:\n• Me dijo **qué** quería. (что хотел)\n• Me dijo **que** quería helado. (что хотел мороженого)\n\nDIFF: «qué» = вопросительное «что», «que» = союз «что». Без тильды смысл меняется!"),
            ),
            keyTakeaways = listOf(
                "Да/нет → si (если)",
                "Qué/dónde/cuándo… — сохраняют тильду",
                "Сдвиг времён в прошлом: pres→imp, indef→pluscuamp",
                "Императив → que + Imp.Subj",
            ),
            relatedTheory = listOf("u11_l5", "u11_l5_5"),
        ),

        // ───────── B2 (10) ─────────

        "u13_l1" to TheoryContent(
            lessonId = "u13_l1",
            title = "Imperfecto de Subjuntivo — образование",
            subtitle = "hablara / comiera / viviera. Две формы: -ara/-ase. От 3 л. мн. Indef.",
            emoji = "🌙", cefr = "B2", readMinutes = 6,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Главное правило формирования",
                    body = "Берём **3 л. мн.ч. Indefinido**, отбрасываем **-ron** и добавляем окончания.\n\nЕсть **две** параллельные формы:\n• **-ra**: hablaron → habla**ra**, habla**ras**, habla**ra**, habl**áramos**, habla**rais**, habla**ran**\n• **-se**: hablaron → habla**se**, habla**ses**, habla**se**, habl**ásemos**, habla**seis**, habla**sen**\n\nОни **идентичны** по значению. -ra чаще в речи, -se — в литературе/формально."),
                TheorySection(TheorySectionType.TABLE, "Топ-нерегуляры (от Indef.)",
                    table = TheoryTable(
                        headers = listOf("Глагол", "3 л. мн. Indef", "Imp.Subj"),
                        rows = listOf(
                            listOf("ser/ir", "fueron", "fuera / fuese"),
                            listOf("tener", "tuvieron", "tuviera / tuviese"),
                            listOf("hacer", "hicieron", "hiciera / hiciese"),
                            listOf("decir", "dijeron", "dijera / dijese"),
                            listOf("poder", "pudieron", "pudiera / pudiese"),
                            listOf("querer", "quisieron", "quisiera / quisiese"),
                            listOf("venir", "vinieron", "viniera / viniese"),
                            listOf("estar", "estuvieron", "estuviera / estuviese"),
                            listOf("saber", "supieron", "supiera / supiese"),
                        ),
                        highlightedColumns = listOf(2),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Применение",
                    examples = listOf(
                        TheoryExample("Quería que vinieras.", "Хотел, чтобы ты пришёл.", "согласование времён"),
                        TheoryExample("Si tuviera dinero…", "Если бы у меня были деньги…", "условный тип 2"),
                        TheoryExample("Me pidió que le ayudara.", "Попросил, чтобы я помог.", ""),
                        TheoryExample("Era importante que estudiaras.", "Было важно, чтобы ты учился.", ""),
                        TheoryExample("Ojalá pudiéramos viajar.", "Хоть бы мы могли путешествовать.", "невыполнимое желание"),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 Где употребляется",
                    body = "Imperfecto Subjuntivo нужен когда **главное предложение в прошлом** + триггер subj:\n• Quería que **viniera** (а не «venga» — потому что главное «quería» в прошлом)\n• Также: si + Imp.Subj (условный тип 2)\n• Вежливая просьба: **Quisiera** un café. (= Querría) — мягче чем «Quiero»."),
            ),
            keyTakeaways = listOf(
                "От 3 л. мн. Indef. (- ron + -ra/-se)",
                "Две формы: -ra (речь) / -se (книжно)",
                "Согласование времён: pasado → Imp.Subj",
                "Quisiera = вежливое «я бы хотел»",
            ),
            relatedTheory = listOf("u9_l3", "u10_l9"),
        ),

        "u13_l6" to TheoryContent(
            lessonId = "u13_l6",
            title = "Pluscuamperfecto de Subjuntivo",
            subtitle = "hubiera comido / hubiese visto. Сожаление о прошлом + согласование.",
            emoji = "⌛", cefr = "B2", readMinutes = 6,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Формула",
                    body = "**Imperfecto Subjuntivo de HABER + participio pasado**:\n• **hubiera** / **hubiese** + comido\n• **hubieras** / **hubieses** + visto\n• **hubiera** / **hubiese** + llegado\n• etc.\n\nВыражает **гипотетическое прошлое** или **сожаление**. Аналог «if I had done» в английском."),
                TheorySection(TheorySectionType.TABLE, "Спряжение HABER (Imp.Subj)",
                    table = TheoryTable(
                        headers = listOf("Лицо", "-ra форма", "-se форма"),
                        rows = listOf(
                            listOf("yo", "hubiera", "hubiese"),
                            listOf("tú", "hubieras", "hubieses"),
                            listOf("él", "hubiera", "hubiese"),
                            listOf("nosotros", "hubiéramos", "hubiésemos"),
                            listOf("vosotros", "hubierais", "hubieseis"),
                            listOf("ellos", "hubieran", "hubiesen"),
                        ),
                        highlightedColumns = listOf(1, 2),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Сожаления и условия",
                    examples = listOf(
                        TheoryExample("Si hubiera estudiado, habría aprobado.", "Если бы я учился, сдал бы.", "условный тип 3"),
                        TheoryExample("Ojalá hubiera venido.", "Хоть бы он пришёл (но не пришёл).", "невыполнимое прошлое"),
                        TheoryExample("Quería que hubieras llegado antes.", "Я хотел, чтобы ты пришёл раньше.", "согласование"),
                        TheoryExample("No creía que lo hubiese hecho.", "Я не верил, что он это сделал.", ""),
                        TheoryExample("Aunque hubieras estudiado, no habrías aprobado.", "Даже если бы ты учился, не сдал бы.", "уступка"),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 Hubiera = habría (разговорное)",
                    body = "В разговорной речи Latam **hubiera** часто заменяет **habría** в результате условного:\n• Si hubiera podido, **hubiera ido**. (вместо «habría ido»)\n\nЭто принято RAE как допустимое. В Spain чаще соблюдают «hubiera ... habría». В письменной речи рекомендуется правильный вариант."),
            ),
            keyTakeaways = listOf(
                "hubiera/hubiese + participio",
                "Условный тип 3: Si + hub.subj + habría",
                "Ojalá hubiera... — невыполнимое прошлое",
                "Hubiera ≈ habría (разг., Latam)",
            ),
            relatedTheory = listOf("u10_l9_5", "u13_l1"),
        ),

        "u13_l10" to TheoryContent(
            lessonId = "u13_l10",
            title = "Aunque + indicativo vs subjuntivo",
            subtitle = "Aunque llueve (факт). Aunque llueva (гипотеза/уступка).",
            emoji = "🌧", cefr = "B2", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Двойственный союз",
                    body = "**Aunque** = «хотя / даже если». Может вводить **факт** (indicativo) или **гипотезу/уступку** (subjuntivo):\n\n• **Aunque llueve, salgo.** = Хотя дождь (идёт прямо сейчас — факт), выхожу.\n• **Aunque llueva, saldré.** = Даже если пойдёт дождь (гипотеза), выйду.\n• **Aunque haya llovido, salgo.** = Даже если шёл дождь (не подтверждено), выхожу.\n• **Aunque lloviera, saldría.** = Даже если бы шёл дождь, я бы вышел."),
                TheorySection(TheorySectionType.COMPARISON, "Indic vs Subj с aunque",
                    comparison = TheoryComparison(
                        leftHeader = "Aunque + Indic (факт)",
                        rightHeader = "Aunque + Subj (уступка)",
                        pairs = listOf(
                            "Aunque es difícil, lo hago." to "Aunque sea difícil, lo haré.",
                            "Aunque tiene dinero, no compra." to "Aunque tenga dinero, no compraría.",
                            "Aunque llovió ayer, salí." to "Aunque hubiera llovido, habría salido.",
                            "(Факт известен.)" to "(Гипотеза / уступка.)",
                        ),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Разные эпохи",
                    examples = listOf(
                        TheoryExample("Aunque tengo hambre, no como.", "Хотя голоден, не ем.", "факт, настоящее"),
                        TheoryExample("Aunque tenga hambre, no comeré.", "Даже если буду голоден, не буду есть.", "гипотеза будущ."),
                        TheoryExample("Aunque está cansado, sigue trabajando.", "Хотя устал, продолжает.", "факт"),
                        TheoryExample("Aunque esté cansado, seguirá.", "Даже если устанет, продолжит.", "гипотеза"),
                        TheoryExample("Aunque fuera rico, no sería feliz.", "Даже если бы был богат, не был бы счастлив.", "контрфакт"),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 Тест «информация vs уступка»",
                    body = "Если **слушатель уже знает** факт или ты его подтверждаешь → **indic**.\nЕсли **подаёшь как гипотезу / возможность / уступку**, даже если знаешь правду → **subj**.\n\nТо же правило для **a pesar de que, por más que, por mucho que**."),
            ),
            keyTakeaways = listOf(
                "Aunque + indic = факт",
                "Aunque + subj = гипотеза / уступка",
                "В прошлом: aunque + Imp.Subj или Pluscuamp.Subj",
                "A pesar de que, por más que — то же правило",
            ),
            relatedTheory = listOf("u9_l12", "u15_l8"),
        ),

        "u14_l0" to TheoryContent(
            lessonId = "u14_l0",
            title = "Пассив с SER — pasiva propia",
            subtitle = "Fue construido por Gaudí. Книжный, формальный, с агентом «por».",
            emoji = "📰", cefr = "B2", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Структура pasiva con ser",
                    body = "**Sujeto + ser + participio + (por + agente)**\n\nПричастие **СОГЛАСУЕТСЯ** с подлежащим по роду и числу:\n• La carta **fue escrita** por María. (carta — ж.р. ед.ч. → escrita)\n• Los libros **fueron escritos** por el autor.\n\nИспользуется в **формальном/книжном** стиле — новости, юридические тексты. В разговоре чаще используют активный залог или «se pasiva»."),
                TheorySection(TheorySectionType.TABLE, "Согласование причастия",
                    table = TheoryTable(
                        headers = listOf("Подлежащее", "ser+participio"),
                        rows = listOf(
                            listOf("la novela (ж.ед.)", "fue escrita"),
                            listOf("el cuadro (м.ед.)", "fue pintado"),
                            listOf("las casas (ж.мн.)", "fueron construidas"),
                            listOf("los premios (м.мн.)", "fueron otorgados"),
                        ),
                        highlightedColumns = listOf(1),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "В новостных стилях",
                    examples = listOf(
                        TheoryExample("El cuadro fue pintado por Picasso.", "Картина была написана Пикассо.", "формально"),
                        TheoryExample("La ley fue aprobada ayer.", "Закон был принят вчера.", "новости"),
                        TheoryExample("Los autores fueron premiados en Madrid.", "Авторов наградили в Мадриде.", ""),
                        TheoryExample("La casa será vendida pronto.", "Дом скоро будет продан.", "будущее"),
                        TheoryExample("El libro ha sido traducido a 5 idiomas.", "Книга была переведена на 5 языков.", "perfecto"),
                    )),
                TheorySection(TheorySectionType.WARNING, "⚠ Используется реже, чем кажется",
                    body = "Испанский **не любит** пассив с ser. Чаще используется:\n1) **Se pasiva**: Se construyó la casa. (Дом был построен.)\n2) **3 л. мн.ч.**: Construyeron la casa. (Построили дом.) — неопределённое лицо.\n\nSer-пассив — в основном для новостей, академических текстов. Когда есть **agente** (por + кто) — ser-пассив более уместен."),
            ),
            keyTakeaways = listOf(
                "Ser + participio + (por + agente)",
                "Causaron — причастие согласуется!",
                "Формальный, книжный регистр",
                "Чаще заменяется на «se» или 3 л. мн.",
            ),
            relatedTheory = listOf("u14_l1", "u14_l2"),
        ),

        "u14_l2" to TheoryContent(
            lessonId = "u14_l2",
            title = "Pasiva refleja vs SE impersonal",
            subtitle = "Se vende casa (пассивный). Se vive bien (безличный).",
            emoji = "🔄", cefr = "B2", readMinutes = 6,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Два разных «se»",
                    body = "**1) Pasiva refleja**: SE + глагол в 3 л. (ед./мн. в зависимости от подлежащего).\n• Se vende casa. (Продаётся дом — casa = подлежащее ед.)\n• Se venden casas. (Продаются дома — casas = подлеж. мн.)\n\n**2) SE impersonal**: SE + глагол в 3 л. ед.ч. ВСЕГДА. Подлежащего нет.\n• Se vive bien aquí. (Здесь хорошо живётся.)\n• Se habla mucho de eso. (Об этом много говорят.)\n\nГлавная разница: pasiva — есть **подлежащее-вещь**, impersonal — нет подлежащего."),
                TheorySection(TheorySectionType.COMPARISON, "Pasiva refleja vs SE impersonal",
                    comparison = TheoryComparison(
                        leftHeader = "Pasiva refleja",
                        rightHeader = "SE impersonal",
                        pairs = listOf(
                            "Se vende casa." to "Se vive bien.",
                            "Se construyen carreteras." to "Se trabaja mucho.",
                            "Подлежащее есть (вещь)" to "Подлежащего нет",
                            "Глагол согласуется с подл." to "Глагол всегда в 3 ед.",
                            "Se rompió la copa." to "Se come bien en Italia.",
                        ),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Реальные надписи",
                    examples = listOf(
                        TheoryExample("Se alquila piso.", "Сдаётся квартира.", "pasiva ед."),
                        TheoryExample("Se buscan camareros.", "Требуются официанты.", "pasiva мн."),
                        TheoryExample("Se habla español.", "Здесь говорят на испанском.", "impers"),
                        TheoryExample("Se vive bien en España.", "В Испании хорошо живётся.", "impers"),
                        TheoryExample("Se come bien aquí.", "Здесь хорошо кормят.", "impers"),
                        TheoryExample("¿Cómo se dice esto?", "Как это говорится?", "impers — фикс. оборот"),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 С людьми — добавляется «a»",
                    body = "Если объект — **человек(и)**, в se-impersonal добавляется **a**:\n• Se ve **a** los niños. (Видны дети.)\n• Se busca **a** un sospechoso. (Ищут подозреваемого.)\n\nЭто различает «se ve a María» (видят Марию — impers) от «se ven María y Juan» (видны М. и Ю. — pasiva). С людьми чаще impers."),
            ),
            keyTakeaways = listOf(
                "Pasiva refleja: подлежащее есть, глагол согл.",
                "SE impersonal: подлежащего нет, всегда 3 ед.",
                "С людьми → «a + лицо», impersonal",
                "«Se vende casa» vs «Se vive bien»",
            ),
            relatedTheory = listOf("u14_l0", "u14_l1"),
        ),

        "u14_l6_5" to TheoryContent(
            lessonId = "u14_l6_5",
            title = "Deber vs Deber de — обязанность vs предположение",
            subtitle = "Debes estudiar (должен). Debe de ser tarde (наверное).",
            emoji = "🤷", cefr = "B2", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Два очень разных значения",
                    body = "**Deber + infinitivo** = **обязанность, долженствование**:\n• Debes estudiar. (Ты должен учиться.)\n• Debemos respetar las reglas. (Мы должны соблюдать правила.)\n\n**Deber de + infinitivo** = **предположение, догадка**:\n• Debe de ser tarde. (Наверное, поздно.)\n• Deben de estar cansados. (Они, наверное, устали.)\n\n⚠ В разговоре границу часто **стирают** — носители смешивают. Но в письменной речи различай!"),
                TheorySection(TheorySectionType.COMPARISON, "Обязанность vs Предположение",
                    comparison = TheoryComparison(
                        leftHeader = "Deber (обязанность)",
                        rightHeader = "Deber de (предположение)",
                        pairs = listOf(
                            "Debes ir al médico." to "Debe de ir al médico (мне кажется, идёт).",
                            "Debemos trabajar." to "Deben de trabajar mucho (наверное).",
                            "= должен" to "= наверное / по-видимому",
                            "Совет / обязанность" to "Догадка о факте",
                        ),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Контекст решает",
                    examples = listOf(
                        TheoryExample("Debes estudiar más.", "Ты должен больше учиться.", "обязанность"),
                        TheoryExample("Debe de estar enfermo.", "Он, наверное, болен.", "предположение"),
                        TheoryExample("Debemos respetar a los demás.", "Мы должны уважать других.", "моральный долг"),
                        TheoryExample("Deben de ser las diez ya.", "Наверное, уже 10.", "догадка о времени"),
                        TheoryExample("No debes mentir.", "Ты не должен лгать.", "запрет"),
                        TheoryExample("No debe de saberlo.", "Он, наверное, не знает.", "догадка отриц."),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 Как запомнить",
                    body = "**De** — это «приблизительно, около». Когда добавляем «de» к deber — получается «около обязан» = «предположительно». Без «de» — чистая обязанность.\n\nАльтернативы для догадок:\n• Tiene que ser tarde. (Должно быть, поздно.)\n• Quizás sea tarde. (Может быть.)\n• Probablemente sea tarde."),
            ),
            keyTakeaways = listOf(
                "Deber + inf = обязанность",
                "Deber de + inf = предположение",
                "В разговоре границу размывают",
                "Tener que + inf — тоже для догадок",
            ),
            relatedTheory = listOf("u14_l6", "u14_l7"),
        ),

        "u15_l4_5" to TheoryContent(
            lessonId = "u15_l4_5",
            title = "Sino vs sino que vs no sólo… sino también",
            subtitle = "Pero vs sino: главное правило отрицания.",
            emoji = "↔", cefr = "B2", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Три похожих оборота",
                    body = "**Sino** = «а» (после отрицания, для контраста):\n• No es Pablo, **sino** Juan. (Это не Пабло, а Хуан.)\n• No quiero té, sino café.\n\n**Sino que** = «а» (когда после следует глагол в личной форме):\n• No estudia, **sino que** trabaja. (Он не учится, а работает.)\n\n**No sólo… sino también** = «не только… но и»:\n• No sólo habla español, **sino también** francés."),
                TheorySection(TheorySectionType.COMPARISON, "Pero vs Sino",
                    comparison = TheoryComparison(
                        leftHeader = "Pero (но)",
                        rightHeader = "Sino (а)",
                        pairs = listOf(
                            "Es difícil, pero divertido." to "No es fácil, sino difícil.",
                            "Без отрицания в 1-й части" to "Только после отрицания",
                            "«Дополнение»" to "«Замена / контраст»",
                            "+ любая часть речи" to "+ существ./прилаг./нареч.",
                        ),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Чёткое различие",
                    examples = listOf(
                        TheoryExample("No es alto, sino bajo.", "Он не высокий, а низкий.", "sino"),
                        TheoryExample("Es difícil, pero útil.", "Сложно, но полезно.", "pero — нет отриц."),
                        TheoryExample("No vino, sino que se quedó.", "Он не пришёл, а остался.", "sino que (с глаг.)"),
                        TheoryExample("No sólo es inteligente, sino también amable.", "Не только умный, но и добрый.", "no sólo... sino también"),
                        TheoryExample("No quiero ir al cine, sino al teatro.", "Не хочу в кино, а в театр.", ""),
                    )),
                TheorySection(TheorySectionType.WARNING, "⚠ Sino vs Si no",
                    body = "Не путать с **si no** (если не) — раздельно!\n• **Sino** = а (после no).\n• **Si no** = если не: Si no estudias, no aprobarás.\n\nDIFF: «No te lo digo, **sino** a Juan» (Говорю не тебе, а Хуану) vs «**Si no** te lo digo, ¿quién?» (Если я тебе не скажу, кто?)."),
            ),
            keyTakeaways = listOf(
                "Pero = но (без отрицания в 1-й части)",
                "Sino = а (только после отрицания)",
                "Sino que — если дальше идёт глагол",
                "Sino ≠ si no (раздельно — «если не»)",
            ),
            relatedTheory = listOf("u15_l4", "u15_l5"),
        ),

        "u15_l8" to TheoryContent(
            lessonId = "u15_l8",
            title = "Уступительные коннекторы B2",
            subtitle = "Aunque, a pesar de, si bien, por más que. Контраст и уступка.",
            emoji = "🔀", cefr = "B2", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Палитра уступок",
                    body = "Все эти союзы передают идею «несмотря на», но требуют разную грамматику:\n\n• **Aunque** + indic/subj (см. u13_l10)\n• **A pesar de que** + indic/subj\n• **A pesar de** + сущ. / inf (без que)\n• **Si bien** + indic (книжно)\n• **Por más que / por mucho que** + indic (факт) / subj (гипотеза)\n• **Pese a** + сущ. / inf"),
                TheorySection(TheorySectionType.TABLE, "Конструкции",
                    table = TheoryTable(
                        headers = listOf("Союз", "После него", "Регистр"),
                        rows = listOf(
                            listOf("aunque", "indic / subj", "нейтр."),
                            listOf("a pesar de que", "indic / subj", "формальн."),
                            listOf("a pesar de", "сущ. / inf", "формальн."),
                            listOf("si bien", "indic", "книжно"),
                            listOf("por más que", "indic / subj", "разг."),
                            listOf("pese a (que)", "сущ./inf или indic/subj", "формальн."),
                        ),
                        highlightedColumns = listOf(1),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Сравнение в речи",
                    examples = listOf(
                        TheoryExample("Aunque llueve, salgo.", "Хотя идёт дождь, выхожу.", "нейтр."),
                        TheoryExample("A pesar de la lluvia, salgo.", "Несмотря на дождь, выхожу.", "+ сущ."),
                        TheoryExample("A pesar de llover, salgo.", "Несмотря на то, что идёт дождь.", "+ inf"),
                        TheoryExample("Si bien es difícil, vale la pena.", "Хотя сложно, оно того стоит.", "книжно"),
                        TheoryExample("Por más que estudio, no apruebo.", "Сколько ни учусь, не сдаю.", "уступка"),
                        TheoryExample("Pese a estar cansado, fue al gym.", "Несмотря на усталость, пошёл в зал.", "+ inf"),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 Регистры",
                    body = "**Aunque** — самый универсальный, можно везде.\n**A pesar de** — формальный аналог, чаще на письме.\n**Si bien** — книжный, академический.\n**Por más que** — разговорный, эмоциональный.\n**Pese a** — литературный, краткий."),
            ),
            keyTakeaways = listOf(
                "Aunque — универсальный",
                "A pesar de + сущ./inf, a pesar de que + verb",
                "Si bien — книжный, всегда + indic",
                "Por más que — разговорный, indic/subj",
            ),
            relatedTheory = listOf("u13_l10", "u15_l4_5"),
        ),

        "u16_l6_5" to TheoryContent(
            lessonId = "u16_l6_5",
            title = "Voseo: Subjuntivo и Imperativo",
            subtitle = "Vos tengás (Ц.Америка) vs Vos tengas (Argentina). Imperativo: ¡tené! ¡vení!",
            emoji = "🇦🇷", cefr = "B2", readMinutes = 5,
            sections = listOf(
                TheorySection(TheorySectionType.RULE, "Voseo в Subjuntivo",
                    body = "В **Argentina/Uruguay** vos в subj использует те же формы, что **tú**:\n• Quiero que vos **tengas** razón. (как tú tengas)\n\nВ **Центральной Америке** (Costa Rica, Honduras и др.) — отдельные формы с ударением на окончание:\n• Quiero que vos **tengás** razón.\n• Espero que **digás** la verdad.\n\nЭто **зональный voseo**. Argentina — самый известный, но не единственный."),
                TheorySection(TheorySectionType.TABLE, "Voseo Imperativo (положит.)",
                    table = TheoryTable(
                        headers = listOf("Глагол", "tú", "vos (RP)"),
                        rows = listOf(
                            listOf("tener", "¡ten!", "¡tené!"),
                            listOf("venir", "¡ven!", "¡vení!"),
                            listOf("salir", "¡sal!", "¡salí!"),
                            listOf("hacer", "¡haz!", "¡hacé!"),
                            listOf("decir", "¡di!", "¡decí!"),
                            listOf("ir", "¡ve!", "¡andá! (вместо ¡vé!)"),
                            listOf("ser", "¡sé!", "¡sé!"),
                        ),
                        highlightedColumns = listOf(2),
                    )),
                TheorySection(TheorySectionType.EXAMPLES, "Звучит на улицах BA",
                    examples = listOf(
                        TheoryExample("¡Vení acá!", "Иди сюда!", "vos imperativo"),
                        TheoryExample("¡Decímelo!", "Скажи мне это!", "decí + me + lo"),
                        TheoryExample("¡Andá a dormir!", "Иди спать!", "ir → andá (особый случай)"),
                        TheoryExample("Quiero que tengas un buen día.", "Хочу, чтобы у тебя был хороший день.", "Argentina — как tú"),
                        TheoryExample("Quiero que tengás un buen día.", "(то же в Costa Rica)", "Centr.Am — отдельная форма"),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 Только Argentina = RP (Rioplatense)",
                    body = "**Voseo Rioplatense** (Argentina + Uruguay + часть Paraguay) — самый распространённый и заметный. Если поедешь в Buenos Aires, услышишь vos постоянно.\n\nВ Subj они используют tú-формы (tengas, vengas). В Imperativo — особые с ударением (¡tené!, ¡vení!).\n\nВ Spain никогда не услышишь vos — там только tú. Vosotros используется именно как «вы мн.ч. неформ.»."),
            ),
            keyTakeaways = listOf(
                "Argentina vos Subj = tú Subj (tengas, vengas)",
                "Центр.Америка vos Subj = -és (tengás)",
                "Imperativo vos: tené, vení, decí, andá",
                "Spain никогда vos — всегда tú/vosotros",
            ),
            relatedTheory = listOf("u16_l10_voseo"),
        ),

        "u16_l11_5" to TheoryContent(
            lessonId = "u16_l11_5",
            title = "Культура Latam: sobremesa, tertulia, ustedeo",
            subtitle = "За что любить испаноязычную культуру и где не попасть впросак.",
            emoji = "🌎", cefr = "B2", readMinutes = 6,
            sections = listOf(
                TheorySection(TheorySectionType.TEXT, "Sobremesa — святое время",
                    body = "**Sobremesa** = «после-обед» — время за столом ПОСЛЕ еды, когда уже не едят, а просто разговаривают, пьют кофе, спорят, смеются. Может длиться **2-3 часа**.\n\nЭто столько же часть культуры, сколько сам обед. Уйти сразу после еды — невежливо. На семейных обедах sobremesa может длиться до вечера."),
                TheorySection(TheorySectionType.TEXT, "Tertulia — клуб разговоров",
                    body = "**Tertulia** — регулярная встреча друзей в кафе для разговоров (политика, искусство, философия). Традиция XIX века, сохранилась в Spain и Mexico.\n\nЕсть знаменитые **tertulias literarias** — встречи писателей. Современный аналог — те самые круглые столы по ТВ («tertulia política»)."),
                TheorySection(TheorySectionType.RULE, "Ustedeo — Колумбия и не только",
                    body = "В **Bogotá** (Колумбия) и части Costa Rica — **ustedeo**: usted используется НЕ только для формальности, но и с **близкими** — семьёй, детьми, питомцами.\n\n• Madre a su hijo: «Mi amor, ¿usted quiere comer?» (Любимый, ты хочешь поесть?)\n\nЭто **не** холодная формальность — наоборот, тепло и интимно. Если приедешь в Bogotá и тебе говорят «usted» — это не дистанция, это норма."),
                TheorySection(TheorySectionType.EXAMPLES, "Слова, которых нет в Spain",
                    examples = listOf(
                        TheoryExample("la merienda", "лёгкий перекус (5-6 вечера)", "Spain — обязательно у детей"),
                        TheoryExample("el aguinaldo", "13-я зарплата на Рождество", "Latam обязательна по закону"),
                        TheoryExample("el chamo (Venezuela)", "парень / чувак", ""),
                        TheoryExample("el bacán (Chile/Peru)", "клёво / классный", ""),
                        TheoryExample("la onda (Mexico)", "вибес / настроение", "¿Qué onda? = что нового?"),
                        TheoryExample("la pulpería (Costa Rica)", "магазинчик у дома", ""),
                    )),
                TheorySection(TheorySectionType.TIP, "💡 «Ahorita» — ловушка",
                    body = "**Ahorita** (уменьш. от ahora) звучит как «прямо сейчас», но значит РАЗНОЕ:\n• **Mexico**: «через минуту, скоро» (~через 30 мин)\n• **Dominicana**: «недавно» (вернулся пару минут назад)\n• **Argentina**: не используется\n\nЕсли мексиканец говорит «ahorita vengo» — не жди немедленно. Это может быть через 20 мин или час."),
            ),
            keyTakeaways = listOf(
                "Sobremesa — 2-3 часа разговоров после еды",
                "Tertulia — встречи в кафе для разговоров",
                "Ustedeo (Bogotá) — usted с близкими",
                "Ahorita ≠ прямо сейчас (Mexico — через 30 мин)",
            ),
            relatedTheory = listOf("u16_l10_voseo", "u16_l9_coloquial"),
        ),

    )
}
