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
        // u1_l0 — Гласные: A, E, I, O, U
        // ─────────────────────────────────────────────────────────────────
        "u1_l0" to TheoryContent(
            lessonId = "u1_l0",
            title = "5 гласных — фундамент испанского",
            subtitle = "Каждая буква = один звук. Без исключений.",
            emoji = "🔤",
            cefr = "A1",
            readMinutes = 3,
            sections = listOf(
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "Главное правило",
                    body = "В испанском **5 гласных букв**, и каждая всегда читается одинаково. Это огромный плюс — нет «закрытых» и «открытых» слогов как в английском.",
                ),
                TheorySection(
                    type = TheorySectionType.TABLE,
                    heading = "Таблица гласных",
                    table = TheoryTable(
                        headers = listOf("Буква", "Звук", "Пример"),
                        rows = listOf(
                            listOf("A a", "[а]", "casa — дом"),
                            listOf("E e", "[э]", "mes — месяц"),
                            listOf("I i", "[и]", "isla — остров"),
                            listOf("O o", "[о]", "ojo — глаз"),
                            listOf("U u", "[у]", "luna — луна"),
                        ),
                        highlightedColumns = listOf(1),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.EXAMPLES,
                    heading = "Послушай и повтори",
                    examples = listOf(
                        TheoryExample("amigo", "друг", "а-ми-го"),
                        TheoryExample("español", "испанский", "эс-па-ньол"),
                        TheoryExample("música", "музыка", "му-си-ка"),
                        TheoryExample("océano", "океан", "о-сэ-а-но"),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.MNEMONIC,
                    heading = "Запомни одной фразой",
                    body = "**A-Э-И-О-У** — пять звуков, чёткие как удары метронома. Никаких «у» вместо «о» (как в английском lOve = «лав»).",
                ),
                TheorySection(
                    type = TheorySectionType.WARNING,
                    heading = "Главная ошибка новичков",
                    body = "Не «глотай» гласные. Русское «малако» по-испански прозвучало бы странно: каждая «о» в испанском **полноценно открытая**. Скажи casa как **«ка-са»**, не «к'са».",
                ),
            ),
            keyTakeaways = listOf(
                "5 гласных, каждая всегда звучит одинаково",
                "A=[а], E=[э], I=[и], O=[о], U=[у]",
                "Гласные не редуцируются — произноси чётко",
                "Слоги в основном открытые: ка-са, не «кса»",
            ),
            relatedTheory = listOf("u1_l1", "u1_l2"),
        ),

        // ─────────────────────────────────────────────────────────────────
        // u1_l1 — Согласные: B/V, D, G
        // ─────────────────────────────────────────────────────────────────
        "u1_l1" to TheoryContent(
            lessonId = "u1_l1",
            title = "B, V, D, G — три коварных согласных",
            subtitle = "B и V звучат одинаково. G меняется перед E/I.",
            emoji = "🗣",
            cefr = "A1",
            readMinutes = 4,
            sections = listOf(
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "B = V",
                    body = "В испанском **B и V — это один и тот же звук** [б/в]. Никакой разницы. `vino` (вино) и `bino` звучали бы одинаково. Различай только на письме.",
                ),
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "G меняется перед E и I",
                    body = "Буква **G** ведёт себя как хамелеон:\n• перед **a / o / u** → твёрдое [г]: gato, gota, gusto\n• перед **e / i** → горловое [х]: gente, gigante",
                ),
                TheorySection(
                    type = TheorySectionType.TABLE,
                    heading = "Шпаргалка по G",
                    table = TheoryTable(
                        headers = listOf("Сочетание", "Звук", "Пример"),
                        rows = listOf(
                            listOf("ga", "[га]", "gato — кот"),
                            listOf("go", "[го]", "gota — капля"),
                            listOf("gu", "[гу]", "gusto — вкус"),
                            listOf("ge", "[хэ]", "gente — люди"),
                            listOf("gi", "[хи]", "gigante — гигант"),
                            listOf("gue", "[гэ]", "guerra — война"),
                            listOf("gui", "[ги]", "guitarra — гитара"),
                        ),
                        highlightedColumns = listOf(1),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.TIP,
                    heading = "💡 Лайфхак с U",
                    body = "Если нужно сказать **[ги]** или **[гэ]**, испанцы вставляют **немую U**: gue, gui. Эта U не читается — она просто «защищает» G от превращения в [х].",
                ),
                TheorySection(
                    type = TheorySectionType.EXAMPLES,
                    heading = "Потренируйся",
                    examples = listOf(
                        TheoryExample("vivir", "жить", "[бибир]"),
                        TheoryExample("general", "генерал", "[хэнэраль]"),
                        TheoryExample("guitarra", "гитара", "[гитарра]"),
                        TheoryExample("dado", "кубик", "[ðаðо] — мягкое D"),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.WARNING,
                    body = "Не пытайся «правильно» произнести V как [в]. Испанец сразу поймёт, что ты учил язык по учебнику, а не от носителя. **B и V — один звук, точка.**",
                ),
            ),
            keyTakeaways = listOf(
                "B и V — один звук [б/в]",
                "G + a/o/u = [г]; G + e/i = [х]",
                "GUE/GUI — U немая, чтобы сохранить [г]",
                "D между гласными — мягкое [ð] почти как английское «th»",
            ),
            relatedTheory = listOf("u1_l0", "u1_l2"),
        ),

        // ─────────────────────────────────────────────────────────────────
        // u1_l2 — H, J, Ñ, RR
        // ─────────────────────────────────────────────────────────────────
        "u1_l2" to TheoryContent(
            lessonId = "u1_l2",
            title = "H, J, Ñ, RR — четыре звуковых сюрприза",
            subtitle = "H молчит · J=[х] · Ñ=[нь] · RR=[р-р-р]",
            emoji = "🤫",
            cefr = "A1",
            readMinutes = 4,
            sections = listOf(
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "H никогда не читается",
                    body = "**H полностью немая.** `hola` = «о-ла», `hotel` = «о-тэль», `hablar` = «а-бляр». Просто игнорируй её.",
                ),
                TheorySection(
                    type = TheorySectionType.RULE,
                    heading = "J — горловой [х]",
                    body = "Буква **J** всегда читается как горловое русское [х] (как в «хочу»). `jamón` = «ха-мон», `Juan` = «ху-ан».",
                ),
                TheorySection(
                    type = TheorySectionType.TABLE,
                    heading = "Все четыре сюрприза",
                    table = TheoryTable(
                        headers = listOf("Буква", "Звук", "Пример"),
                        rows = listOf(
                            listOf("H h", "молчит", "hola — «ола»"),
                            listOf("J j", "[х]", "jefe — «хэ-фэ» (босс)"),
                            listOf("Ñ ñ", "[нь]", "año — «а-ньо» (год)"),
                            listOf("RR", "[р-р]", "perro — «пэр-ро» (собака)"),
                        ),
                        highlightedColumns = listOf(1),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.COMPARISON,
                    heading = "PERO vs PERRO",
                    body = "Одна R и две R меняют смысл слова. Это **смыслоразличительная пара** — путать нельзя.",
                    comparison = TheoryComparison(
                        leftHeader = "pero",
                        rightHeader = "perro",
                        pairs = listOf(
                            "одна R — короткое [р]" to "две R — длинное [р-р-р]",
                            "значит «но»" to "значит «собака»",
                            "Quiero, pero no puedo" to "El perro ladra",
                        ),
                    ),
                ),
                TheorySection(
                    type = TheorySectionType.TIP,
                    heading = "💡 Как тренировать RR",
                    body = "Зажми кончик языка к нёбу за зубами и выдохни — язык сам начнёт вибрировать. Не получается за раз? Начни с «трр-трр-трр» и постепенно убирай Т.",
                ),
                TheorySection(
                    type = TheorySectionType.EXAMPLES,
                    heading = "Послушай разницу",
                    examples = listOf(
                        TheoryExample("hola", "привет", "H молчит"),
                        TheoryExample("Japón", "Япония", "[ха-пон]"),
                        TheoryExample("España", "Испания", "[эс-па-нья]"),
                        TheoryExample("perro", "собака", "[пэр-ро]"),
                    ),
                ),
            ),
            keyTakeaways = listOf(
                "H — всегда молчит",
                "J — всегда [х]",
                "Ñ — это «нь», отдельная буква",
                "RR — длинное вибрирующее [р-р]",
                "pero (но) ≠ perro (собака)",
            ),
            relatedTheory = listOf("u1_l3"),
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
        "u1_l7" to TheoryContent(
            lessonId = "u1_l7",
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
            relatedTheory = listOf("u1_l8", "u1_l9"),
        ),

        // ─────────────────────────────────────────────────────────────────
        // u1_l8 — SER: somos, sois, son
        // ─────────────────────────────────────────────────────────────────
        "u1_l8" to TheoryContent(
            lessonId = "u1_l8",
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
            relatedTheory = listOf("u1_l7", "u1_l9"),
        ),

        // ─────────────────────────────────────────────────────────────────
        // u1_l9 — Личные местоимения
        // ─────────────────────────────────────────────────────────────────
        "u1_l9" to TheoryContent(
            lessonId = "u1_l9",
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
            relatedTheory = listOf("u1_l7", "u1_l8"),
        ),
    )
}
