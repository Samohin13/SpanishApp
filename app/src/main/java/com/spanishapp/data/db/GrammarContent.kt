package com.spanishapp.data.db

import com.spanishapp.data.db.entity.LessonEntity

object GrammarContent {

    fun getAll(): List<LessonEntity> = listOf(

        // ══════════════════════════════════════════
        // A1
        // ══════════════════════════════════════════
        LessonEntity(
            id = 1, level = "A1", category = "grammar",
            title = "Артикли: el, la, los, las",
            topic = "Определённые артикли",
            xpReward = 15,
            contentJson = """
            {
              "theory": "В испанском есть мужской и женский род. Определённый артикль 'the' переводится как el (м.р.), la (ж.р.), los (м.р. мн.ч.), las (ж.р. мн.ч.).",
              "rules": [
                "el libro — книга (м.р.)",
                "la casa — дом (ж.р.)",
                "los libros — книги",
                "las casas — дома"
              ],
              "tip": "Слова на -o обычно мужского рода, на -a — женского. Исключения: el día (день), la mano (рука).",
              "examples": [
                {"es": "El perro es grande.", "ru": "Собака большая."},
                {"es": "La chica habla español.", "ru": "Девочка говорит по-испански."},
                {"es": "Los niños juegan.", "ru": "Дети играют."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 2, level = "A1", category = "grammar",
            title = "Ser vs Estar — быть",
            topic = "Глаголы ser и estar",
            xpReward = 20,
            contentJson = """
            {
              "theory": "Оба глагола значат 'быть', но используются в разных ситуациях. SER — постоянные качества. ESTAR — временные состояния и местоположение.",
              "rules": [
                "SER: национальность, профессия, характер, происхождение",
                "ESTAR: эмоции, здоровье, местонахождение, временное состояние"
              ],
              "tip": "Запомни: ESTAR = Emoción, STAdo (состояние), luGAR (место).",
              "examples": [
                {"es": "Soy ruso. — я русский (постоянно)", "ru": "SER — национальность"},
                {"es": "Estoy cansado. — я устал (сейчас)", "ru": "ESTAR — временное состояние"},
                {"es": "El café está frío. — кофе холодный (сейчас)", "ru": "ESTAR — временное"},
                {"es": "El hielo es frío. — лёд холодный (всегда)", "ru": "SER — постоянное"}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 3, level = "A1", category = "grammar",
            title = "Presente Indicativo",
            topic = "Настоящее время",
            xpReward = 20,
            contentJson = """
            {
              "theory": "Presente Indicativo используется для действий, которые происходят сейчас, регулярно или всегда. Глаголы делятся на 3 группы: -ar, -er, -ir.",
              "rules": [
                "hablar (говорить): hablo, hablas, habla, hablamos, habláis, hablan",
                "comer (есть): como, comes, come, comemos, coméis, comen",
                "vivir (жить): vivo, vives, vive, vivimos, vivís, viven"
              ],
              "tip": "Окончания -o всегда для 'yo' (я). Запомни это — поможет с другими временами.",
              "examples": [
                {"es": "Hablo español todos los días.", "ru": "Я говорю по-испански каждый день."},
                {"es": "¿Dónde vives?", "ru": "Где ты живёшь?"},
                {"es": "Comemos a las dos.", "ru": "Мы едим в два часа."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 4, level = "A1", category = "grammar",
            title = "Числа 1–100",
            topic = "Los números",
            xpReward = 10,
            contentJson = """
            {
              "theory": "Числа в испанском — основа для всего: цены, время, возраст.",
              "rules": [
                "1–10: uno, dos, tres, cuatro, cinco, seis, siete, ocho, nueve, diez",
                "11–15: once, doce, trece, catorce, quince",
                "16–19: dieciséis, diecisiete, dieciocho, diecinueve",
                "20, 30…: veinte, treinta, cuarenta, cincuenta, sesenta, setenta, ochenta, noventa",
                "100: cien / ciento"
              ],
              "tip": "21–29 пишутся слитно: veintiuno, veintidós... С 31 раздельно: treinta y uno.",
              "examples": [
                {"es": "Tengo veinticinco años.", "ru": "Мне двадцать пять лет."},
                {"es": "Son cincuenta euros.", "ru": "Это пятьдесят евро."},
                {"es": "Vivo en el piso treinta y dos.", "ru": "Я живу на тридцать втором этаже."}
              ]
            }
            """.trimIndent()
        ),

        // ══════════════════════════════════════════
        // A2
        // ══════════════════════════════════════════
        LessonEntity(
            id = 5, level = "A2", category = "grammar",
            title = "Pretérito Indefinido",
            topic = "Прошедшее время (завершённое)",
            xpReward = 25,
            contentJson = """
            {
              "theory": "Pretérito Indefinido используется для конкретных завершённых действий в прошлом. 'Я поел', 'она пришла', 'мы поехали'.",
              "rules": [
                "hablar → hablé, hablaste, habló, hablamos, hablasteis, hablaron",
                "comer → comí, comiste, comió, comimos, comisteis, comieron",
                "Неправильные: ser/ir → fui, fuiste, fue; tener → tuve; hacer → hice"
              ],
              "tip": "Маркеры этого времени: ayer (вчера), la semana pasada (на прошлой неделе), en 2020.",
              "examples": [
                {"es": "Ayer comí pizza.", "ru": "Вчера я ел пиццу."},
                {"es": "¿Dónde fuiste el verano pasado?", "ru": "Куда ты ездил прошлым летом?"},
                {"es": "Llegué tarde al trabajo.", "ru": "Я опоздал на работу."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 6, level = "A2", category = "grammar",
            title = "Pretérito Imperfecto",
            topic = "Прошедшее время (незавершённое)",
            xpReward = 25,
            contentJson = """
            {
              "theory": "Imperfecto описывает: 1) привычные действия в прошлом, 2) фон/описание прошлого, 3) незавершённые действия.",
              "rules": [
                "hablar → hablaba, hablabas, hablaba, hablábamos, hablabais, hablaban",
                "comer → comía, comías, comía, comíamos, comíais, comían",
                "Неправильные только: ser (era), ir (iba), ver (veía)"
              ],
              "tip": "Маркеры: siempre (всегда), antes (раньше), cuando era niño (когда я был ребёнком), todos los días (каждый день).",
              "examples": [
                {"es": "De niño, jugaba al fútbol todos los días.", "ru": "В детстве я играл в футбол каждый день."},
                {"es": "Cuando llegué, ella dormía.", "ru": "Когда я пришёл, она спала."},
                {"es": "Antes vivía en Moscú.", "ru": "Раньше я жил в Москве."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 7, level = "A2", category = "grammar",
            title = "Reflexive verbs — Возвратные глаголы",
            topic = "Verbos reflexivos",
            xpReward = 20,
            contentJson = """
            {
              "theory": "Возвратные глаголы обозначают действие, направленное на себя. Они используются с местоимениями: me, te, se, nos, os, se.",
              "rules": [
                "levantarse (вставать): me levanto, te levantas, se levanta...",
                "llamarse (называться): me llamo, te llamas, se llama...",
                "ducharse (душ): me ducho, te duchas, se ducha..."
              ],
              "tip": "Если глагол оканчивается на -se в инфинитиве — он возвратный. Местоимение меняется по лицу.",
              "examples": [
                {"es": "Me llamo Alejandro.", "ru": "Меня зовут Алехандро."},
                {"es": "Me levanto a las siete.", "ru": "Я встаю в семь."},
                {"es": "¿A qué hora te acuestas?", "ru": "В котором часу ты ложишься спать?"}
              ]
            }
            """.trimIndent()
        ),

        // ══════════════════════════════════════════
        // B1
        // ══════════════════════════════════════════
        LessonEntity(
            id = 8, level = "B1", category = "grammar",
            title = "Subjuntivo Presente",
            topic = "Сослагательное наклонение",
            xpReward = 35,
            contentJson = """
            {
              "theory": "Subjuntivo выражает субъективность: желания, сомнения, эмоции, рекомендации. Используется в придаточных предложениях после определённых выражений.",
              "rules": [
                "Образование: берём yo-форму presente, убираем -o, добавляем окончания",
                "hablar → hable, hables, hable, hablemos, habléis, hablen",
                "comer → coma, comas, coma, comamos, comáis, coman",
                "Триггеры: querer que, esperar que, recomendar que, es importante que"
              ],
              "tip": "Запомни: WEIRDO — Wishes, Emotions, Impersonal, Recommendations, Doubt/Denial, Ojala.",
              "examples": [
                {"es": "Quiero que vengas.", "ru": "Я хочу, чтобы ты пришёл."},
                {"es": "Es importante que estudies.", "ru": "Важно, чтобы ты учился."},
                {"es": "Espero que todo salga bien.", "ru": "Надеюсь, всё пройдёт хорошо."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 9, level = "B1", category = "grammar",
            title = "Futuro Simple",
            topic = "Будущее время",
            xpReward = 20,
            contentJson = """
            {
              "theory": "Futuro Simple выражает действия в будущем, предположения и обещания. Образуется от инфинитива + окончания.",
              "rules": [
                "Окончания для всех глаголов: -é, -ás, -á, -emos, -éis, -án",
                "hablar → hablaré, hablarás, hablará...",
                "Неправильные основы: tener→tendr-, poder→podr-, hacer→har-, decir→dir-"
              ],
              "tip": "Futuro также используют для предположений о настоящем: '¿Dónde estará?' = 'Где же он (наверное) находится?'",
              "examples": [
                {"es": "Mañana hablaré con el jefe.", "ru": "Завтра я поговорю с начальником."},
                {"es": "¿Vendrás a la fiesta?", "ru": "Ты придёшь на вечеринку?"},
                {"es": "Tendrá unos cuarenta años.", "ru": "Ему, наверное, лет сорок."}
              ]
            }
            """.trimIndent()
        ),

        // ── A2 (продолжение) ────────────────────────────────────
        LessonEntity(
            id = 10, level = "A2", category = "grammar",
            title = "Pretérito Perfecto",
            topic = "Прошедшее перфектное (haber + participio)",
            xpReward = 25,
            contentJson = """
            {
              "theory": "Pretérito Perfecto описывает действия в прошлом, связанные с настоящим. 'Я уже поел', 'я никогда не был в Мадриде'. Состоит из haber (настоящее) + причастие (-ado/-ido).",
              "rules": [
                "haber: he, has, ha, hemos, habéis, han",
                "Причастие: hablar→hablado, comer→comido, vivir→vivido",
                "Неправильные: hacer→hecho, decir→dicho, ver→visto, escribir→escrito, abrir→abierto, poner→puesto"
              ],
              "tip": "Маркеры: hoy (сегодня), esta semana (на этой неделе), ya (уже), todavía no (ещё не), nunca (никогда), alguna vez (когда-либо).",
              "examples": [
                {"es": "Hoy he comido paella.", "ru": "Сегодня я ел паэлью."},
                {"es": "¿Has visto la nueva película?", "ru": "Ты видел новый фильм?"},
                {"es": "Nunca he estado en Madrid.", "ru": "Я никогда не был в Мадриде."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 11, level = "A2", category = "grammar",
            title = "Imperativo — Повелительное наклонение",
            topic = "Команды и просьбы",
            xpReward = 22,
            contentJson = """
            {
              "theory": "Imperativo используется для команд, советов и инструкций. Утвердительная и отрицательная формы образуются по-разному.",
              "rules": [
                "Утвердительная (tú): hablar→habla, comer→come, vivir→vive (= 3-е лицо ед.ч.)",
                "Утвердительная (usted): hable, coma, viva (= subjuntivo)",
                "Отрицательная: всегда subjuntivo с no — no hables, no comas",
                "Неправильные tú: ven (venir), pon (poner), sal (salir), haz (hacer), di (decir), ten (tener), sé (ser), ve (ir)"
              ],
              "tip": "Местоимения присоединяются к концу утвердительной формы: dímelo (скажи мне это). С отрицательной — перед глаголом: no me lo digas.",
              "examples": [
                {"es": "¡Habla más alto, por favor!", "ru": "Говори громче, пожалуйста!"},
                {"es": "No comas tan rápido.", "ru": "Не ешь так быстро."},
                {"es": "Por favor, ven aquí.", "ru": "Пожалуйста, иди сюда."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 12, level = "A2", category = "grammar",
            title = "Comparativo y Superlativo",
            topic = "Сравнения и превосходная степень",
            xpReward = 18,
            contentJson = """
            {
              "theory": "Сравнения в испанском строятся через más/menos + прилагательное + que. Превосходная степень — el/la más + прилагательное + de.",
              "rules": [
                "Больше/меньше: más alto que, menos alto que",
                "Одинаково: tan alto como (такой же высокий как)",
                "Превосходная: el más alto de la clase (самый высокий в классе)",
                "Неправильные: bueno→mejor, malo→peor, grande→mayor, pequeño→menor"
              ],
              "tip": "Ловушка: 'más bueno' допустимо для морального качества (más bueno que el pan = очень добрый), но обычно говорят 'mejor'.",
              "examples": [
                {"es": "Madrid es más grande que Barcelona.", "ru": "Мадрид больше, чем Барселона."},
                {"es": "Este libro es el más interesante de todos.", "ru": "Эта книга самая интересная из всех."},
                {"es": "Mi hermano es tan alto como mi padre.", "ru": "Мой брат такой же высокий, как и мой отец."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 13, level = "A2", category = "grammar",
            title = "Por vs Para",
            topic = "Два предлога 'для'",
            xpReward = 22,
            contentJson = """
            {
              "theory": "Por и para — оба переводятся как 'для', но используются в разных ситуациях. Это самая частая ловушка для русскоговорящих.",
              "rules": [
                "POR: причина, время в течение, перемещение через, обмен/цена, средство",
                "PARA: цель, получатель, направление, дедлайн, мнение",
                "POR la mañana — утром (в течение)",
                "PARA mañana — к завтра (дедлайн)"
              ],
              "tip": "Запомни простой пример: 'Estudio español POR mi novia' (из-за неё) vs 'Estudio español PARA viajar' (чтобы путешествовать).",
              "examples": [
                {"es": "Este regalo es para ti.", "ru": "Этот подарок для тебя. (получатель)"},
                {"es": "Lo hago por amor.", "ru": "Я делаю это из любви. (причина)"},
                {"es": "Camino por el parque.", "ru": "Я иду через парк. (перемещение)"},
                {"es": "Necesito el informe para el lunes.", "ru": "Мне нужен отчёт к понедельнику. (дедлайн)"}
              ]
            }
            """.trimIndent()
        ),

        // ── B1 (продолжение) ────────────────────────────────────
        LessonEntity(
            id = 14, level = "B1", category = "grammar",
            title = "Condicional Simple",
            topic = "Условное наклонение",
            xpReward = 28,
            contentJson = """
            {
              "theory": "Condicional выражает гипотетические ситуации, вежливые просьбы, советы и действия в прошлом, ожидаемые в будущем. Образуется от инфинитива + окончания.",
              "rules": [
                "Окончания: -ía, -ías, -ía, -íamos, -íais, -ían",
                "hablar → hablaría, hablarías, hablaría...",
                "Неправильные основы такие же как в Futuro: tendr-, podr-, har-, dir-",
                "Использование: Si tuviera dinero, viajaría (Если бы у меня были деньги, я бы путешествовал)"
              ],
              "tip": "Очень вежливая форма: '¿Podría ayudarme?' (Не могли бы вы мне помочь?) vs прямая 'puede ayudarme'.",
              "examples": [
                {"es": "Me gustaría un café, por favor.", "ru": "Я бы хотел кофе, пожалуйста."},
                {"es": "Yo en tu lugar hablaría con él.", "ru": "На твоём месте я бы поговорил с ним."},
                {"es": "Dijo que vendría mañana.", "ru": "Он сказал, что придёт завтра."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 15, level = "B1", category = "grammar",
            title = "Pretérito Pluscuamperfecto",
            topic = "Предпрошедшее время",
            xpReward = 25,
            contentJson = """
            {
              "theory": "Pluscuamperfecto описывает действие, которое произошло раньше другого действия в прошлом. 'Я уже поел, когда она пришла'.",
              "rules": [
                "Состав: imperfecto от haber + participio",
                "había, habías, había, habíamos, habíais, habían + hablado/comido/vivido",
                "Те же неправильные participios: hecho, dicho, visto, escrito"
              ],
              "tip": "По-русски часто переводится как 'уже было сделано'. Маркеры: ya (уже), antes de que, cuando.",
              "examples": [
                {"es": "Cuando llegué, ella ya había salido.", "ru": "Когда я пришёл, она уже ушла."},
                {"es": "Nunca había probado sushi antes de ese día.", "ru": "Я никогда не пробовал суши до того дня."},
                {"es": "Me dijo que había estudiado en Salamanca.", "ru": "Он сказал, что учился в Саламанке."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 16, level = "B1", category = "grammar",
            title = "Subjuntivo después de conjunciones",
            topic = "Subjuntivo с союзами",
            xpReward = 30,
            contentJson = """
            {
              "theory": "После определённых союзов почти всегда стоит Subjuntivo. Это касается выражений цели, времени (будущего), уступок, условий.",
              "rules": [
                "Цель: para que + subj. (чтобы)",
                "Будущее время: cuando, hasta que, en cuanto + subj.",
                "Уступка: aunque + subj. (даже если)",
                "Условие: a menos que (если только не), con tal de que (при условии что)"
              ],
              "tip": "Cuando + indicativo = регулярное действие. Cuando + subjuntivo = будущее. 'Cuando llegues, llámame' (когда придёшь, позвони).",
              "examples": [
                {"es": "Te ayudo para que aprendas más rápido.", "ru": "Я помогаю тебе, чтобы ты быстрее учился."},
                {"es": "Cuando tengas tiempo, llámame.", "ru": "Когда у тебя будет время, позвони мне."},
                {"es": "Iré a la fiesta aunque no me inviten.", "ru": "Я пойду на вечеринку, даже если меня не пригласят."}
              ]
            }
            """.trimIndent()
        ),

        // ── B2 ─────────────────────────────────────────────────
        LessonEntity(
            id = 17, level = "B2", category = "grammar",
            title = "Subjuntivo Imperfecto",
            topic = "Сослагательное прошедшее",
            xpReward = 35,
            contentJson = """
            {
              "theory": "Subjuntivo Imperfecto используется в условных предложениях с гипотетическими ситуациями ('если бы я был богатым'), вежливых формах и прошлом контексте.",
              "rules": [
                "Образование: 3-е лицо мн.ч. Pretérito Indefinido без -on + ra/se",
                "hablar → hablaron → hablara/hablase",
                "tener → tuvieron → tuviera/tuviese",
                "Формы -ra и -se синонимичны, -ra чаще в современном языке"
              ],
              "tip": "Классическая конструкция: 'Si + imperfecto subj., condicional' — Si tuviera tiempo, viajaría más.",
              "examples": [
                {"es": "Si fuera rico, compraría una casa en España.", "ru": "Если бы я был богат, купил бы дом в Испании."},
                {"es": "Quería que vinieras a la fiesta.", "ru": "Я хотел, чтобы ты пришёл на вечеринку."},
                {"es": "Quisiera reservar una mesa.", "ru": "Я хотел бы забронировать столик. (вежливо)"}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 18, level = "B2", category = "grammar",
            title = "Voz pasiva",
            topic = "Страдательный залог",
            xpReward = 28,
            contentJson = """
            {
              "theory": "В испанском есть два способа выражения страдательного залога: с глаголом ser (формально) и с местоимением se (более естественно).",
              "rules": [
                "Ser + participio: 'La carta fue escrita por Juan' (письмо было написано Хуаном)",
                "Pasiva refleja с se: 'Se vende coche' (продаётся машина)",
                "Se impersonal: 'Se habla español aquí' (здесь говорят по-испански)"
              ],
              "tip": "В разговорной речи испанцы редко используют ser + participio — предпочитают активный залог или se-конструкции.",
              "examples": [
                {"es": "El edificio fue construido en 1920.", "ru": "Здание было построено в 1920 году."},
                {"es": "Se venden libros usados aquí.", "ru": "Здесь продаются подержанные книги."},
                {"es": "En España se cena tarde.", "ru": "В Испании ужинают поздно."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 19, level = "B2", category = "grammar",
            title = "Estilo indirecto",
            topic = "Косвенная речь",
            xpReward = 32,
            contentJson = """
            {
              "theory": "При передаче чужих слов времена 'сдвигаются' назад: presente → imperfecto, indefinido → pluscuamperfecto, futuro → condicional.",
              "rules": [
                "Direct: 'Estudio español' → Indirect: dijo que estudiaba español",
                "Direct: 'Estudié ayer' → dijo que había estudiado ayer",
                "Direct: 'Estudiaré mañana' → dijo que estudiaría al día siguiente",
                "Imperativo → que + subjuntivo: 'Ven' → me dijo que viniera"
              ],
              "tip": "Меняй местоимения и временные маркеры: hoy→aquel día, ayer→el día anterior, mañana→al día siguiente.",
              "examples": [
                {"es": "Me dijo que vendría más tarde.", "ru": "Он сказал, что придёт позже."},
                {"es": "Preguntó si yo había visto la película.", "ru": "Он спросил, видел ли я фильм."},
                {"es": "Le pidió que la llamara.", "ru": "Он попросил, чтобы она ему позвонила."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 20, level = "B2", category = "grammar",
            title = "Perífrasis verbales",
            topic = "Глагольные конструкции",
            xpReward = 26,
            contentJson = """
            {
              "theory": "Перифразы — устойчивые сочетания вспомогательного глагола + инфинитива/герундия/причастия. Они выражают оттенки модальности и аспекта.",
              "rules": [
                "ir a + inf — собираться (близкое будущее): voy a comer",
                "estar + gerundio — длящееся действие: estoy comiendo",
                "acabar de + inf — только что: acabo de llegar",
                "volver a + inf — снова: vuelvo a leer este libro",
                "tener que + inf — должен: tengo que estudiar",
                "deber + inf — следует: debes descansar",
                "soler + inf — обычно: suelo desayunar a las ocho"
              ],
              "tip": "Hay que + inf — безличное 'нужно/надо' (без подлежащего): hay que estudiar.",
              "examples": [
                {"es": "Acabo de llegar a casa.", "ru": "Я только что пришёл домой."},
                {"es": "Estamos viendo una película.", "ru": "Мы (сейчас) смотрим фильм."},
                {"es": "Tienes que descansar más.", "ru": "Тебе нужно больше отдыхать."},
                {"es": "Suelo correr por las mañanas.", "ru": "Я обычно бегаю по утрам."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 21, level = "A2", category = "grammar",
            title = "Gerundio: estar + -ndo",
            topic = "Настоящее длительное время",
            xpReward = 15,
            contentJson = """
            {
              "theory": "Конструкция estar + герундий описывает действие, которое происходит прямо сейчас или в данный период жизни. Аналог английского Present Continuous.",
              "rules": [
                "Формула: estar (в нужном времени) + герундий",
                "-ar → -ando: hablar → hablando",
                "-er/-ir → -iendo: comer → comiendo, vivir → viviendo",
                "Неправильные: leer → leyendo, dormir → durmiendo, decir → diciendo, pedir → pidiendo",
                "Местоимения присоединяются к концу или ставятся перед estar: estoy lavándome / me estoy lavando"
              ],
              "tip": "В отличие от английского, estar + -ndo НЕ используется для будущего: 'завтра еду' = mañana voy, а не estoy yendo.",
              "examples": [
                {"es": "¿Qué estás haciendo? — Estoy viendo una serie.", "ru": "Что ты делаешь? — Смотрю сериал."},
                {"es": "Mis padres están viviendo en Valencia este año.", "ru": "Мои родители в этом году живут в Валенсии."},
                {"es": "Perdona, te estaba escribiendo justo ahora.", "ru": "Извини, я как раз тебе писал."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 22, level = "A2", category = "grammar",
            title = "Pronombres OD/OI: lo, la, le",
            topic = "Местоимения дополнения",
            xpReward = 15,
            contentJson = """
            {
              "theory": "Местоимения заменяют прямое (кого? что?) и косвенное (кому? чему?) дополнение, чтобы не повторять существительное. Ставятся перед спрягаемым глаголом.",
              "rules": [
                "Прямое (OD): me, te, lo/la, nos, os, los/las",
                "Косвенное (OI): me, te, le, nos, os, les",
                "Порядок: OI + OD + глагол: 'Te lo digo' (Я тебе это говорю)",
                "le/les + lo/la/los/las → se lo: 'Se lo doy' (Я ему это даю), а не 'le lo doy'",
                "С инфинитивом, герундием, императивом — присоединяются: dímelo, dándomelo"
              ],
              "tip": "Запомни: 'se lo' появляется только когда le/les встречается с lo/la — это правило благозвучия, не путать с возвратным se.",
              "examples": [
                {"es": "¿Has visto a María? — Sí, la vi ayer.", "ru": "Ты видел Марию? — Да, я её вчера видел."},
                {"es": "Le compré un regalo a mi hermano.", "ru": "Я купил подарок своему брату."},
                {"es": "Ese libro, ¿me lo prestas?", "ru": "Эту книгу одолжишь мне?"}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 23, level = "A2", category = "grammar",
            title = "Hace + tiempo + que / desde hace",
            topic = "Выражения времени",
            xpReward = 15,
            contentJson = """
            {
              "theory": "Эти конструкции отвечают на вопрос 'как давно?' и описывают действие, которое началось в прошлом и длится до сих пор.",
              "rules": [
                "Hace + время + que + глагол в настоящем: 'Hace dos años que vivo aquí'",
                "Глагол в настоящем + desde hace + время: 'Vivo aquí desde hace dos años'",
                "Оба варианта эквивалентны и переводятся одинаково",
                "Для прошлого: hace + время = 'тому назад': 'Llegué hace una hora' (час назад)",
                "Вопрос: ¿Cuánto (tiempo) hace que...? — Сколько времени...?"
              ],
              "tip": "В русском говорим 'я живу здесь два года', а в испанском обязательно либо 'hace dos años que vivo', либо 'vivo desde hace dos años'.",
              "examples": [
                {"es": "Hace tres años que estudio español.", "ru": "Я учу испанский уже три года."},
                {"es": "No la veo desde hace meses.", "ru": "Я не видел её несколько месяцев."},
                {"es": "¿Cuánto hace que conoces a Pablo?", "ru": "Сколько времени ты знаешь Пабло?"}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 24, level = "A2", category = "grammar",
            title = "Adverbios en -mente",
            topic = "Образование наречий",
            xpReward = 15,
            contentJson = """
            {
              "theory": "Многие наречия образа действия образуются от прилагательных с помощью суффикса -mente, аналогично английскому -ly или русскому -о/-но.",
              "rules": [
                "Берём форму прилагательного женского рода + -mente: rápida → rápidamente",
                "Если прилагательное одинаково в м.р. и ж.р., добавляем -mente напрямую: fácil → fácilmente",
                "Ударение прилагательного сохраняется: rápido (с акутом) → rápidamente",
                "При перечислении -mente пишем только у последнего: 'habla clara y rápidamente'",
                "Не все наречия на -mente — есть bien, mal, despacio, deprisa и др."
              ],
              "tip": "Не злоупотребляй -mente: испанцы часто заменяют их выражениями 'con + существительное' — 'con cuidado' вместо 'cuidadosamente'.",
              "examples": [
                {"es": "Habla muy rápidamente, no le entiendo.", "ru": "Он говорит очень быстро, я его не понимаю."},
                {"es": "Resolvió el problema fácilmente.", "ru": "Он легко решил задачу."},
                {"es": "Lo hice clara y honestamente.", "ru": "Я сделал это ясно и честно."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 25, level = "A2", category = "grammar",
            title = "Indefinido vs Imperfecto",
            topic = "Выбор прошедшего времени",
            xpReward = 15,
            contentJson = """
            {
              "theory": "Главная сложность испанского прошедшего: какое время выбрать. Indefinido — конкретное завершённое действие. Imperfecto — фон, привычка, длящееся состояние в прошлом.",
              "rules": [
                "Indefinido: один раз, в конкретный момент: 'Ayer comí paella'",
                "Imperfecto: регулярно, привычка: 'De niño comía paella todos los domingos'",
                "Imperfecto: описание фона, погоды, возраста: 'Hacía frío, tenía 10 años'",
                "Часто вместе: фон (imperfecto) + событие (indefinido): 'Llovía cuando salí'",
                "Маркеры indefinido: ayer, anoche, el lunes, en 2020. Маркеры imperfecto: siempre, todos los días, mientras, cuando era pequeño"
              ],
              "tip": "Правило кадра и фильма: imperfecto — это фон (что было), indefinido — действие на этом фоне (что произошло).",
              "examples": [
                {"es": "Cuando era niño, jugaba al fútbol todos los días.", "ru": "В детстве я играл в футбол каждый день."},
                {"es": "Estaba cenando cuando me llamaste.", "ru": "Я ужинал, когда ты мне позвонил."},
                {"es": "El año pasado fui a México tres veces.", "ru": "В прошлом году я три раза ездил в Мексику."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 26, level = "B1", category = "grammar",
            title = "Si + condicional: real e irreal",
            topic = "Условные предложения",
            xpReward = 20,
            contentJson = """
            {
              "theory": "Условные предложения с si описывают, что произойдёт при выполнении условия. Различаем реальные (возможные) и нереальные (гипотетические) ситуации настоящего.",
              "rules": [
                "Реальное условие (1 тип): Si + presente, presente/futuro/imperativo",
                "Пример 1 типа: 'Si tengo tiempo, voy al cine' (если будет время — пойду)",
                "Нереальное условие (2 тип): Si + imperfecto de subjuntivo, condicional simple",
                "Пример 2 типа: 'Si tuviera tiempo, iría al cine' (если бы было время — пошёл бы)",
                "После si НИКОГДА не ставится presente de subjuntivo и condicional"
              ],
              "tip": "Запомни: 'si tendría' — грубая ошибка. После si — либо presente (реально), либо imperfecto subjuntivo (гипотетика).",
              "examples": [
                {"es": "Si llueve mañana, nos quedamos en casa.", "ru": "Если завтра пойдёт дождь, останемся дома."},
                {"es": "Si fuera rico, viajaría por todo el mundo.", "ru": "Если бы я был богат, путешествовал бы по всему миру."},
                {"es": "Si supiera la respuesta, te la diría.", "ru": "Если бы я знал ответ, я бы тебе сказал."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 27, level = "B1", category = "grammar",
            title = "Pasiva refleja: se + verbo",
            topic = "Безличный пассив с se",
            xpReward = 20,
            contentJson = """
            {
              "theory": "В испанском вместо классической пассивной конструкции (ser + participio) гораздо чаще используется пассив с se. Особенно в объявлениях, инструкциях и обобщениях.",
              "rules": [
                "Формула: se + глагол в 3 л. ед. или мн. числа",
                "Глагол согласуется с подлежащим: 'Se vende coche' / 'Se venden coches'",
                "Безличное se (без подлежащего): 'Aquí se come bien' (здесь хорошо кормят)",
                "Используется когда исполнитель неважен или неизвестен",
                "Не путать с возвратным se: 'se lava' (моется) vs 'se lavan los platos' (моются тарелки)"
              ],
              "tip": "Если можно подставить 'кто-то' или 'люди вообще' — нужен пассив с se. Это самый распространённый способ обобщения в испанском.",
              "examples": [
                {"es": "Se habla español en veintiún países.", "ru": "На испанском говорят в двадцати одной стране."},
                {"es": "Se necesitan camareros con experiencia.", "ru": "Требуются официанты с опытом."},
                {"es": "Aquí se vive muy tranquilo.", "ru": "Здесь живётся очень спокойно."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 28, level = "B1", category = "grammar",
            title = "Quizás, tal vez + Subjuntivo",
            topic = "Выражения сомнения",
            xpReward = 20,
            contentJson = """
            {
              "theory": "Выражения сомнения и предположения часто требуют subjuntivo, потому что сообщают о неуверенности говорящего, а не о факте.",
              "rules": [
                "Quizás / Tal vez / Acaso + subjuntivo (если сомнение): 'Quizás venga' (может, придёт)",
                "Те же слова + indicativo (если уверенность выше): 'Quizás viene' (наверное, придёт)",
                "Posiblemente / Probablemente + чаще subjuntivo: 'Probablemente llueva mañana'",
                "A lo mejor + ВСЕГДА indicativo: 'A lo mejor viene' — исключение!",
                "Puede que + ВСЕГДА subjuntivo: 'Puede que tengas razón'"
              ],
              "tip": "Запомни исключение: 'a lo mejor' — самое разговорное, но единственное из этой группы, требующее indicativo.",
              "examples": [
                {"es": "Quizás María no venga a la fiesta.", "ru": "Возможно, Мария не придёт на вечеринку."},
                {"es": "Puede que tengas razón en eso.", "ru": "Может быть, ты в этом прав."},
                {"es": "A lo mejor nos vemos el sábado.", "ru": "Может, увидимся в субботу."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 29, level = "B1", category = "grammar",
            title = "Pronombres relativos: que, quien, cuyo",
            topic = "Относительные местоимения",
            xpReward = 20,
            contentJson = """
            {
              "theory": "Относительные местоимения соединяют главное и придаточное предложения. Соответствуют русским 'который, кто, чей, где'.",
              "rules": [
                "que — самое универсальное: 'el libro que leo' (книга, которую я читаю)",
                "quien / quienes — только о людях, после предлога: 'la chica con quien hablé'",
                "el/la/los/las que — после предлога или для уточнения: 'la casa en la que vivo'",
                "donde — место: 'el bar donde nos conocimos'",
                "cuyo/cuya/cuyos/cuyas — притяжательное 'чей': 'el escritor cuyas novelas leo'"
              ],
              "tip": "cuyo согласуется с тем, чем владеют, а не с владельцем: 'el chico cuya hermana...' (не cuyo!).",
              "examples": [
                {"es": "El piso que alquilamos es muy luminoso.", "ru": "Квартира, которую мы снимаем, очень светлая."},
                {"es": "Es el amigo con quien fui a Barcelona.", "ru": "Это друг, с которым я ездил в Барселону."},
                {"es": "Conozco a un escritor cuyos libros son geniales.", "ru": "Я знаю писателя, чьи книги прекрасны."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 30, level = "B1", category = "grammar",
            title = "Estilo indirecto en pasado",
            topic = "Косвенная речь в прошлом",
            xpReward = 20,
            contentJson = """
            {
              "theory": "Когда передаём чужие слова глаголом в прошлом (dijo, preguntó), времена в придаточном смещаются назад — как в английском backshift.",
              "rules": [
                "Presente → Imperfecto: 'Tengo hambre' → Dijo que tenía hambre",
                "Pretérito perfecto / indefinido → Pluscuamperfecto: 'He llegado' → Dijo que había llegado",
                "Futuro → Condicional: 'Vendré' → Dijo que vendría",
                "Imperativo → Imperfecto de subjuntivo: 'Ven aquí' → Me dijo que viniera",
                "Маркеры тоже сдвигаются: hoy → aquel día, mañana → al día siguiente, aquí → allí"
              ],
              "tip": "Imperfecto и pluscuamperfecto в косвенной речи НЕ меняются — они уже 'самые прошлые'.",
              "examples": [
                {"es": "Me dijo que estaba muy cansado.", "ru": "Он сказал, что очень устал."},
                {"es": "Preguntó si habíamos visto la película.", "ru": "Он спросил, видели ли мы фильм."},
                {"es": "Le pedí que me llamara al día siguiente.", "ru": "Я попросил его позвонить мне на следующий день."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 31, level = "B2", category = "grammar",
            title = "Subjuntivo Pluscuamperfecto",
            topic = "Сослагательное предпрошедшее",
            xpReward = 25,
            contentJson = """
            {
              "theory": "Pluscuamperfecto de subjuntivo (hubiera/hubiese hablado) выражает гипотетическое действие в прошлом — то, что могло, но не произошло. Используется в условных нереальных предложениях прошлого.",
              "rules": [
                "Формула: hubiera/hubiese + participio (hablado, comido, vivido)",
                "Формы 'hubiera' и 'hubiese' взаимозаменяемы, но 'hubiera' встречается чаще",
                "Условие нереальности в прошлом (3 тип): Si + pluscuamp. subj., condicional compuesto",
                "Пример: 'Si hubiera estudiado, habría aprobado' (Если бы я учился, сдал бы)",
                "Также после ojalá о прошлом: 'Ojalá hubiera venido' (если бы он пришёл)"
              ],
              "tip": "В разговорной речи во второй части часто используют ту же форму: 'Si hubiera sabido, hubiera ido' вместо 'habría ido'.",
              "examples": [
                {"es": "Si hubiera salido antes, no habría perdido el tren.", "ru": "Если бы я вышел раньше, не опоздал бы на поезд."},
                {"es": "Ojalá hubieras estado allí con nosotros.", "ru": "Жаль, что тебя там не было с нами."},
                {"es": "No creo que ella hubiera dicho eso.", "ru": "Не верю, что она могла такое сказать."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 32, level = "B2", category = "grammar",
            title = "Concordancia de tiempos",
            topic = "Согласование времён",
            xpReward = 25,
            contentJson = """
            {
              "theory": "Когда главное предложение в прошедшем времени и требует subjuntivo, придаточное тоже сдвигается в прошлое subjuntivo. Это последовательность времён, аналогичная английскому sequence of tenses.",
              "rules": [
                "Главное в presente → придаточное в presente subj.: 'Quiero que vengas'",
                "Главное в pretérito/imperfecto → придаточное в imperfecto subj.: 'Quería que vinieras'",
                "Главное в presente, действие до него → pretérito perfecto subj.: 'No creo que haya venido'",
                "Главное в прошлом, действие ещё раньше → pluscuamperfecto subj.: 'No creía que hubiera venido'",
                "Condicional в главном → imperfecto subj. в придаточном: 'Me gustaría que vinieras'"
              ],
              "tip": "Простое правило: presente тянет за собой presente subj., прошлое тянет imperfecto subj. Никогда не смешивай 'quería que vengas'.",
              "examples": [
                {"es": "Mi madre quería que estudiara medicina.", "ru": "Моя мама хотела, чтобы я изучал медицину."},
                {"es": "No creía que hubieras llegado tan pronto.", "ru": "Я не думал, что ты так быстро приедешь."},
                {"es": "Me encantaría que vinieras a la boda.", "ru": "Мне бы очень хотелось, чтобы ты пришёл на свадьбу."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 33, level = "B2", category = "grammar",
            title = "Conectores discursivos",
            topic = "Дискурсивные коннекторы",
            xpReward = 25,
            contentJson = """
            {
              "theory": "Коннекторы связывают идеи в тексте и придают речи логическую структуру. Особенно важны в письменной речи и формальных дискуссиях на B2.",
              "rules": [
                "Противопоставление: sin embargo, no obstante, aun así (тем не менее, однако)",
                "Следствие: por (lo) tanto, por consiguiente, en consecuencia (следовательно)",
                "Причина: ya que, puesto que, dado que (поскольку)",
                "Добавление: además, asimismo, por otra parte (кроме того, с другой стороны)",
                "Резюме: en resumen, en definitiva, en conclusión (в итоге, в заключение)"
              ],
              "tip": "В формальном тексте 'pero' заменяй на 'sin embargo' или 'no obstante', а 'así que' — на 'por (lo) tanto'. Это сразу повышает уровень речи.",
              "examples": [
                {"es": "El proyecto es complicado; no obstante, lo terminaremos a tiempo.", "ru": "Проект сложный, однако мы закончим его вовремя."},
                {"es": "Llovió mucho; por consiguiente, se canceló el partido.", "ru": "Шёл сильный дождь, следовательно, матч отменили."},
                {"es": "En definitiva, la decisión depende de ti.", "ru": "В конечном счёте решение зависит от тебя."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 34, level = "B2", category = "grammar",
            title = "Aunque + indicativo / subjuntivo",
            topic = "Уступительные предложения",
            xpReward = 25,
            contentJson = """
            {
              "theory": "Aunque ('хотя', 'даже если') может вводить уступительное придаточное и с indicativo, и с subjuntivo. Выбор меняет смысл: реальный факт или гипотеза.",
              "rules": [
                "Aunque + indicativo — реальный факт: 'Aunque llueve, salgo' (хотя идёт дождь, я выхожу)",
                "Aunque + subjuntivo — предположение или уступка: 'Aunque llueva, saldré' (даже если пойдёт дождь)",
                "A pesar de que — синоним, те же правила выбора времени",
                "Por mucho/más que + subjuntivo: 'Por mucho que insistas, no iré' (как бы ты ни настаивал)",
                "Aun cuando — более книжный аналог aunque"
              ],
              "tip": "Тест: если событие УЖЕ происходит и ты это знаешь — indicativo. Если 'допустим, что...' или 'даже если бы...' — subjuntivo.",
              "examples": [
                {"es": "Aunque está cansado, sigue trabajando.", "ru": "Хотя он устал, продолжает работать."},
                {"es": "Aunque sea tarde, voy a llamarla.", "ru": "Даже если уже поздно, я ей позвоню."},
                {"es": "Por mucho que estudies, ese examen es difícil.", "ru": "Сколько бы ты ни учил, этот экзамен сложный."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 35, level = "B2", category = "grammar",
            title = "Lo + adjetivo / lo que / lo cual",
            topic = "Артикль среднего рода lo",
            xpReward = 25,
            contentJson = """
            {
              "theory": "В испанском есть 'нейтральный' артикль lo, который превращает прилагательные и причастия в абстрактные существительные, а также вводит обобщённые относительные конструкции.",
              "rules": [
                "Lo + прилагательное = абстрактное понятие: 'lo bueno' (хорошее, то, что хорошо)",
                "Lo + de + сущ. = 'та история с...': 'lo de ayer' (та вчерашняя история)",
                "Lo que = 'то, что' (без конкретного антецедента): 'No entiendo lo que dices'",
                "Lo cual = 'что' (отсылка к целой фразе): 'Llegó tarde, lo cual me molestó'",
                "Lo más / lo menos + adj. = превосходная степень в среднем роде: 'lo más importante'"
              ],
              "tip": "lo que ставится в начале или после глагола, lo cual — только после запятой и отсылает ко всей предыдущей идее, а не к конкретному слову.",
              "examples": [
                {"es": "Lo importante es no rendirse.", "ru": "Важное — не сдаваться."},
                {"es": "No me creí lo que me contaste.", "ru": "Я не поверил тому, что ты мне рассказал."},
                {"es": "Llegó dos horas tarde, lo cual fue muy grosero.", "ru": "Он опоздал на два часа, что было очень грубо."}
              ]
            }
            """.trimIndent()
        ),

        // ══════════════════════════════════════════
        // A1 (расширение, сессия 10)
        // ══════════════════════════════════════════
        LessonEntity(
            id = 36, level = "A1", category = "grammar",
            title = "Неопределённые артикли: un, una, unos, unas",
            topic = "Artículos indeterminados",
            xpReward = 15,
            contentJson = """
            {
              "theory": "Неопределённый артикль соответствует русскому 'один/какой-то' или английскому 'a/some'. Используется, когда говорим о предмете впервые или неконкретно.",
              "rules": [
                "un — мужской род, единственное число: un libro (книга)",
                "una — женский род, единственное число: una casa (дом)",
                "unos — мужской род, множественное число: unos libros (несколько книг)",
                "unas — женский род, множественное число: unas casas (несколько домов)"
              ],
              "tip": "Если предмет уже упомянут или известен — используй определённый артикль (el/la). 'Tengo un perro. El perro se llama Max'.",
              "examples": [
                {"es": "Quiero un café, por favor.", "ru": "Я хочу кофе, пожалуйста."},
                {"es": "Hay una farmacia cerca.", "ru": "Рядом есть аптека."},
                {"es": "Compré unos zapatos nuevos.", "ru": "Я купил новые туфли."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 37, level = "A1", category = "grammar",
            title = "Hay — есть, имеется",
            topic = "Безличный глагол hay",
            xpReward = 15,
            contentJson = """
            {
              "theory": "Hay — особая безличная форма глагола haber. Переводится как 'есть, имеется' и не меняется по лицам и числам. С единственным и множественным числом — всегда hay.",
              "rules": [
                "Hay + un/una/unos/unas/числа: hay un libro, hay tres libros",
                "Не используется с определённым артиклем: говорим 'el libro está aquí', а не 'hay el libro'",
                "Вопрос: ¿Hay…? — Есть ли…?",
                "Отрицание: No hay — нет (в смысле отсутствует)"
              ],
              "tip": "Запомни разницу: HAY = существование (есть в принципе), ESTÁ = местонахождение (находится конкретно где-то).",
              "examples": [
                {"es": "Hay un gato en el jardín.", "ru": "В саду есть кот."},
                {"es": "¿Hay leche en la nevera?", "ru": "В холодильнике есть молоко?"},
                {"es": "No hay nadie en casa.", "ru": "Никого нет дома."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 38, level = "A1", category = "grammar",
            title = "Притяжательные: mi, tu, su, nuestro",
            topic = "Adjetivos posesivos",
            xpReward = 15,
            contentJson = """
            {
              "theory": "Притяжательные прилагательные показывают, кому принадлежит предмет. Согласуются с существительным в числе, а nuestro/vuestro — ещё и в роде.",
              "rules": [
                "mi (мой/моя), mis (мои): mi libro, mis libros",
                "tu (твой/твоя), tus: tu casa, tus casas",
                "su (его/её/их/Ваш), sus: su perro, sus perros",
                "nuestro/nuestra/nuestros/nuestras (наш): nuestra casa, nuestros amigos",
                "vuestro/vuestra/vuestros/vuestras (ваш — в Испании, мн.ч.)"
              ],
              "tip": "Su может означать 'его, её, их, Ваш' — смысл уточняется по контексту или через 'de él / de ella / de ellos'.",
              "examples": [
                {"es": "Mi madre se llama Ana.", "ru": "Мою маму зовут Анна."},
                {"es": "¿Dónde está tu coche?", "ru": "Где твоя машина?"},
                {"es": "Nuestros amigos viven en Madrid.", "ru": "Наши друзья живут в Мадриде."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 39, level = "A1", category = "grammar",
            title = "Указательные: este, ese, aquel",
            topic = "Demostrativos",
            xpReward = 15,
            contentJson = """
            {
              "theory": "Указательные местоимения показывают расстояние до предмета относительно говорящего: близко, чуть дальше, далеко.",
              "rules": [
                "este/esta/estos/estas — этот (рядом со мной)",
                "ese/esa/esos/esas — тот (рядом с собеседником)",
                "aquel/aquella/aquellos/aquellas — тот (далеко от обоих)",
                "Согласование с существительным в роде и числе: este libro, esta casa, estos libros, estas casas"
              ],
              "tip": "Нейтральные формы esto, eso, aquello (без рода) указывают на ситуацию или нечто абстрактное: '¿Qué es esto?' (что это?).",
              "examples": [
                {"es": "Este café está muy bueno.", "ru": "Этот кофе очень вкусный."},
                {"es": "¿Cuánto cuesta esa camiseta?", "ru": "Сколько стоит та футболка?"},
                {"es": "Aquellas montañas son los Pirineos.", "ru": "Те (далёкие) горы — Пиренеи."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 40, level = "A1", category = "grammar",
            title = "Глагол tener и tener que",
            topic = "Иметь / должен",
            xpReward = 15,
            contentJson = """
            {
              "theory": "Tener — один из самых важных глаголов: 'иметь'. Используется и для возраста, и для ощущений, и для долженствования через 'tener que + инфинитив'.",
              "rules": [
                "Спряжение: tengo, tienes, tiene, tenemos, tenéis, tienen",
                "Возраст: tengo 25 años (мне 25 лет — буквально 'имею 25 лет')",
                "Ощущения: tener hambre/sed/frío/calor/sueño/miedo",
                "Долженствование: tener que + инфинитив = 'должен сделать': tengo que estudiar"
              ],
              "tip": "В испанском 'я голодный' = tengo hambre (имею голод), а не 'soy hambriento'. Это калька, которой нужно избегать.",
              "examples": [
                {"es": "Tengo dos hermanos.", "ru": "У меня два брата."},
                {"es": "¿Cuántos años tienes?", "ru": "Сколько тебе лет?"},
                {"es": "Tengo que ir al médico.", "ru": "Мне нужно к врачу."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 41, level = "A1", category = "grammar",
            title = "Отрицание: no, nada, nunca, nadie",
            topic = "Negación",
            xpReward = 15,
            contentJson = """
            {
              "theory": "Самое простое отрицание — поставить 'no' перед глаголом. Также есть отрицательные слова nada, nunca, nadie, ninguno — и часто используется двойное отрицание.",
              "rules": [
                "No + глагол: no hablo inglés (я не говорю по-английски)",
                "Nada — ничего, nadie — никто, nunca — никогда, ninguno — никакой",
                "Двойное отрицание: 'no como nada' (я ничего не ем) — 'no' обязательно, если отрицательное слово стоит после глагола",
                "Если отрицательное слово стоит перед глаголом — 'no' не нужно: 'Nunca como pizza'"
              ],
              "tip": "Двойное отрицание в испанском — это норма, а не ошибка. 'No veo a nadie' буквально 'не вижу никого' = 'никого не вижу'.",
              "examples": [
                {"es": "No tengo dinero.", "ru": "У меня нет денег."},
                {"es": "Nunca bebo café por la noche.", "ru": "Я никогда не пью кофе ночью."},
                {"es": "No hay nadie en la oficina.", "ru": "В офисе никого нет."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 42, level = "A1", category = "grammar",
            title = "Вопросительные слова",
            topic = "Qué, dónde, cómo, cuándo, por qué",
            xpReward = 15,
            contentJson = """
            {
              "theory": "Вопросительные слова в испанском всегда пишутся с акутом (´). Они стоят в начале вопроса, который оформляется перевёрнутым вопросительным знаком ¿ в начале и ? в конце.",
              "rules": [
                "qué — что/какой: ¿Qué quieres?",
                "dónde — где, a dónde — куда, de dónde — откуда",
                "cómo — как: ¿Cómo estás?",
                "cuándo — когда, cuánto/cuánta/cuántos/cuántas — сколько",
                "quién/quiénes — кто, por qué — почему (ответ: porque — потому что)"
              ],
              "tip": "Не путай 'por qué' (почему, раздельно с акутом) и 'porque' (потому что, слитно без акута).",
              "examples": [
                {"es": "¿Cómo te llamas?", "ru": "Как тебя зовут?"},
                {"es": "¿Dónde vives?", "ru": "Где ты живёшь?"},
                {"es": "¿Por qué estudias español?", "ru": "Почему ты учишь испанский?"}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 43, level = "A1", category = "grammar",
            title = "Множественное число существительных",
            topic = "Plural de los sustantivos",
            xpReward = 15,
            contentJson = """
            {
              "theory": "Множественное число в испанском образуется добавлением -s или -es в зависимости от того, на что оканчивается слово.",
              "rules": [
                "Слово на гласную → +s: libro → libros, casa → casas",
                "Слово на согласную → +es: profesor → profesores, ciudad → ciudades",
                "Слово на -z → меняем z на c и +es: lápiz → lápices, vez → veces",
                "Слово на -s без ударения на последнем слоге не меняется: el lunes → los lunes"
              ],
              "tip": "Артикль тоже меняется: el → los, la → las, un → unos, una → unas.",
              "examples": [
                {"es": "Tengo dos perros.", "ru": "У меня две собаки."},
                {"es": "Los profesores son simpáticos.", "ru": "Преподаватели приятные."},
                {"es": "Necesito tres lápices.", "ru": "Мне нужны три карандаша."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 44, level = "A1", category = "grammar",
            title = "Прилагательные: согласование",
            topic = "Concordancia de los adjetivos",
            xpReward = 15,
            contentJson = """
            {
              "theory": "Прилагательные в испанском согласуются с существительным в роде и числе. Обычно ставятся ПОСЛЕ существительного, в отличие от русского.",
              "rules": [
                "Прилагательное на -o меняется по родам: alto → alta, alto → altos → altas",
                "Прилагательное на -e или согласную не меняется по родам: grande → grande, fácil → fácil; во мн.ч. +s или +es",
                "Национальности на согласную добавляют -a в ж.р.: español → española, francés → francesa",
                "Порядок: существительное + прилагательное (la casa blanca, не 'la blanca casa')"
              ],
              "tip": "Несколько прилагательных, описывающих чувства/оценку, могут стоять перед существительным для эмоциональной окраски: 'una buena idea' (хорошая идея).",
              "examples": [
                {"es": "Una chica simpática.", "ru": "Симпатичная девушка."},
                {"es": "Los coches rojos son bonitos.", "ru": "Красные машины красивые."},
                {"es": "Mis amigas españolas son divertidas.", "ru": "Мои испанские подруги весёлые."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 45, level = "A1", category = "grammar",
            title = "Gustar — мне нравится",
            topic = "Глагол с косвенным дополнением",
            xpReward = 15,
            contentJson = """
            {
              "theory": "Gustar работает не как русское 'нравиться'. Буквально: 'мне нравится X' = 'X нравится мне'. Глагол согласуется с тем, что нравится, а не с тем, кому.",
              "rules": [
                "Формула: (a + кому) + me/te/le/nos/os/les + gusta/gustan + что",
                "gusta — для одного предмета или инфинитива: me gusta el café, me gusta leer",
                "gustan — для множественного числа: me gustan los libros",
                "Усиление через 'a mí, a ti, a él': 'A mí me gusta el cine'",
                "Похожие глаголы: encantar (обожать), interesar (интересовать), doler (болеть)"
              ],
              "tip": "Не говори 'yo gusto el café' — это значит 'я нравлюсь кофе'. Правильно: 'me gusta el café'.",
              "examples": [
                {"es": "Me gusta la música latina.", "ru": "Мне нравится латинская музыка."},
                {"es": "A mi hermano le gustan los videojuegos.", "ru": "Моему брату нравятся видеоигры."},
                {"es": "Nos encanta viajar.", "ru": "Мы обожаем путешествовать."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 46, level = "A1", category = "grammar",
            title = "Ir + a + infinitivo",
            topic = "Ближайшее будущее",
            xpReward = 15,
            contentJson = """
            {
              "theory": "Конструкция 'ir + a + инфинитив' выражает планы и ближайшее будущее. Аналог английского 'to be going to' и русского 'собираться сделать'.",
              "rules": [
                "Спряжение ir: voy, vas, va, vamos, vais, van",
                "Формула: ir (в настоящем) + a + инфинитив",
                "Используется для запланированных и близких будущих действий",
                "Маркеры: mañana, esta tarde, el próximo año, dentro de…"
              ],
              "tip": "На уровне A1/A2 эта конструкция гораздо популярнее настоящего futuro simple (hablaré). В разговорной речи испанцы говорят 'voy a hablar', а не 'hablaré'.",
              "examples": [
                {"es": "Voy a estudiar esta noche.", "ru": "Я буду учиться сегодня вечером."},
                {"es": "¿Qué vas a hacer mañana?", "ru": "Что ты собираешься делать завтра?"},
                {"es": "Vamos a comer paella.", "ru": "Мы будем есть паэлью."}
              ]
            }
            """.trimIndent()
        ),

        // ══════════════════════════════════════════
        // A2 (расширение, сессия 10)
        // ══════════════════════════════════════════
        LessonEntity(
            id = 47, level = "A2", category = "grammar",
            title = "Pretérito Indefinido — неправильные глаголы",
            topic = "Verbos irregulares en indefinido",
            xpReward = 20,
            contentJson = """
            {
              "theory": "Самые частые глаголы в Pretérito Indefinido — неправильные. Их формы нужно запомнить наизусть, потому что они встречаются в каждом разговоре о прошлом.",
              "rules": [
                "ser/ir (одинаковые формы): fui, fuiste, fue, fuimos, fuisteis, fueron",
                "tener: tuve, tuviste, tuvo, tuvimos, tuvisteis, tuvieron",
                "estar: estuve, estuviste, estuvo, estuvimos, estuvisteis, estuvieron",
                "hacer: hice, hiciste, hizo, hicimos, hicisteis, hicieron (зам. c→z в 'hizo')",
                "decir: dije, dijiste, dijo, dijimos, dijisteis, dijeron (без -i- в 3 л. мн.ч.)"
              ],
              "tip": "Неправильные глаголы Indefinido не имеют ударения на окончании в 1 и 3 лице ед.ч.: TÚve, TÚvo, а не 'tuvé' / 'tuvó'.",
              "examples": [
                {"es": "Ayer fui al cine con María.", "ru": "Вчера я ходил в кино с Марией."},
                {"es": "¿Qué hiciste el fin de semana?", "ru": "Что ты делал в выходные?"},
                {"es": "Tuvimos un problema con el coche.", "ru": "У нас была проблема с машиной."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 48, level = "A2", category = "grammar",
            title = "Combinación de pronombres: se lo",
            topic = "Двойные местоимения",
            xpReward = 20,
            contentJson = """
            {
              "theory": "Когда в предложении два местоимения (косвенное + прямое), они идут в порядке КОСВЕННОЕ + ПРЯМОЕ. Если оба в третьем лице, le/les превращается в se.",
              "rules": [
                "Порядок: me/te/le/nos/os/les + me/te/lo/la/nos/os/los/las",
                "Le/les + lo/la/los/las → se lo / se la / se los / se las (звуковое правило)",
                "Стоят перед спрягаемым глаголом: 'Te lo digo' (я тебе это говорю)",
                "С инфинитивом, герундием, императивом — присоединяются: dártelo, dándotelo, dámelo"
              ],
              "tip": "Если хочется уточнить, кому именно — добавь 'a él / a ella / a ellos': 'Se lo doy a ella' (даю это именно ей).",
              "examples": [
                {"es": "¿El libro? Ya te lo di ayer.", "ru": "Книга? Я тебе её уже отдал вчера."},
                {"es": "Se lo dije a mi madre.", "ru": "Я сказал это своей маме."},
                {"es": "Quiero comprártelo para tu cumpleaños.", "ru": "Хочу купить это тебе на день рождения."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 49, level = "A2", category = "grammar",
            title = "Muy vs Mucho",
            topic = "Очень / много",
            xpReward = 20,
            contentJson = """
            {
              "theory": "Muy и mucho — оба переводятся как 'очень/много', но используются по-разному. Это одна из частых ошибок начинающих.",
              "rules": [
                "MUY + прилагательное / наречие: muy alto, muy rápido, muy bien",
                "MUCHO как наречие после глагола: trabajo mucho",
                "MUCHO/A/OS/AS + существительное (согласуется): mucho dinero, mucha gente, muchos libros, muchas casas",
                "Не говорим 'muy mucho' и 'mucho bueno' — это ошибки"
              ],
              "tip": "Простой тест: muy = very (с прилагательным/наречием), mucho = a lot / many (с существительным или после глагола).",
              "examples": [
                {"es": "Esta película es muy interesante.", "ru": "Этот фильм очень интересный."},
                {"es": "Trabajo mucho los lunes.", "ru": "По понедельникам я много работаю."},
                {"es": "Hay mucha gente en el parque.", "ru": "В парке много людей."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 50, level = "A2", category = "grammar",
            title = "Saber vs Conocer",
            topic = "Два глагола 'знать'",
            xpReward = 20,
            contentJson = """
            {
              "theory": "В испанском 'знать' выражается двумя разными глаголами в зависимости от типа знания: фактическое (saber) или знакомство/опыт (conocer).",
              "rules": [
                "SABER — знать факт, информацию, как что-то делать: sé la respuesta, sé nadar",
                "CONOCER — быть знакомым с человеком, городом, вещью: conozco a María, conozco Madrid",
                "Saber + инфинитив = уметь: sé cocinar (умею готовить)",
                "Conocer + личное 'a' для людей: conozco a tu hermano",
                "В Indefinido меняется смысл: 'supe' = узнал (получил информацию), 'conocí' = познакомился"
              ],
              "tip": "Если можно перевести как 'я в курсе' / 'я умею' — saber. Если 'я знаком с' / 'я был там' — conocer.",
              "examples": [
                {"es": "Sé hablar tres idiomas.", "ru": "Я умею говорить на трёх языках."},
                {"es": "Conozco a su familia desde hace años.", "ru": "Я знаком с его семьёй уже много лет."},
                {"es": "No sé dónde está el restaurante.", "ru": "Я не знаю, где находится ресторан."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 51, level = "A2", category = "grammar",
            title = "Preposiciones: a, de, en, con",
            topic = "Базовые предлоги",
            xpReward = 20,
            contentJson = """
            {
              "theory": "Базовые предлоги a, de, en, con образуют огромное количество фраз. Их использование часто отличается от русского — нужно запоминать конкретные конструкции.",
              "rules": [
                "A — направление, время, личное a перед людьми: voy a Madrid, a las tres, veo a María",
                "DE — принадлежность, происхождение, материал: el libro de Juan, soy de Rusia, de madera",
                "EN — местонахождение внутри, время (месяц/год), транспорт: en casa, en julio, en coche",
                "CON — с (вместе): con mi amigo, café con leche, escribir con bolígrafo",
                "A + el → al (al cine), de + el → del (del profesor) — обязательное слияние"
              ],
              "tip": "Запоминай не отдельные предлоги, а целые конструкции: 'pensar EN', 'soñar CON', 'depender DE'. Это сэкономит силы.",
              "examples": [
                {"es": "Voy al supermercado con mi madre.", "ru": "Я иду в супермаркет с мамой."},
                {"es": "Soy de España y vivo en Francia.", "ru": "Я из Испании и живу во Франции."},
                {"es": "El regalo es de mi abuela.", "ru": "Подарок от моей бабушки."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 52, level = "A2", category = "grammar",
            title = "Verbos con cambio vocálico (e→ie, o→ue)",
            topic = "Глаголы с изменением гласной",
            xpReward = 20,
            contentJson = """
            {
              "theory": "Многие глаголы в настоящем времени меняют гласную в корне в ударных слогах — это касается всех лиц, кроме nosotros и vosotros.",
              "rules": [
                "e → ie: querer → quiero, quieres, quiere, queremos, queréis, quieren",
                "o → ue: poder → puedo, puedes, puede, podemos, podéis, pueden",
                "u → ue (только jugar): juego, juegas, juega, jugamos, jugáis, juegan",
                "e → i (только -ir глаголы): pedir → pido, pides, pide, pedimos, pedís, piden",
                "Nosotros и vosotros сохраняют корень: queremos, podemos, jugamos"
              ],
              "tip": "Это правило ударных слогов: ударение падает на корень → есть изменение, ударение падает на окончание → корень обычный. Поэтому формы 'мы/вы' остаются неизменёнными.",
              "examples": [
                {"es": "Quiero un café con leche.", "ru": "Я хочу кофе с молоком."},
                {"es": "No puedo venir hoy.", "ru": "Я не могу прийти сегодня."},
                {"es": "Los niños juegan en el parque.", "ru": "Дети играют в парке."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 53, level = "A2", category = "grammar",
            title = "Acabar de + infinitivo",
            topic = "Только что сделал",
            xpReward = 20,
            contentJson = """
            {
              "theory": "Перифраза 'acabar de + инфинитив' означает 'только что сделать что-то'. Описывает действие, которое произошло прямо сейчас или в недавнем прошлом.",
              "rules": [
                "Формула: acabar (в настоящем) + de + инфинитив",
                "Спряжение acabar: acabo, acabas, acaba, acabamos, acabáis, acaban",
                "В imperfecto означает 'только что сделал' в прошлом: acababa de salir cuando llamaste",
                "Не путать с 'terminar de + inf' (закончить делать что-то)"
              ],
              "tip": "Альтернатива в русском: 'я только что вышел' = acabo de salir. Это очень разговорная и частая конструкция.",
              "examples": [
                {"es": "Acabo de llegar a casa.", "ru": "Я только что пришёл домой."},
                {"es": "Acabamos de ver una película genial.", "ru": "Мы только что посмотрели отличный фильм."},
                {"es": "Acababa de salir cuando empezó a llover.", "ru": "Я только что вышел, когда пошёл дождь."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 54, level = "A2", category = "grammar",
            title = "Marcadores temporales del pasado",
            topic = "Маркеры прошедшего времени",
            xpReward = 20,
            contentJson = """
            {
              "theory": "Маркеры времени помогают выбрать правильное прошедшее время. Каждое прошедшее время имеет свой набор 'индикаторов', которые сразу подсказывают, что выбрать.",
              "rules": [
                "Pretérito Indefinido: ayer, anoche, anteayer, el lunes pasado, en 2020, hace dos años",
                "Pretérito Perfecto: hoy, esta mañana, esta semana, este mes, ya, todavía no, alguna vez, nunca",
                "Pretérito Imperfecto: siempre, normalmente, todos los días, mientras, cuando era niño, antes",
                "Pluscuamperfecto: ya + había hecho, antes de que, cuando llegué, ya había..."
              ],
              "tip": "Если видишь в предложении 'hoy' или 'esta semana' — почти всегда Pretérito Perfecto. Если 'ayer' — почти всегда Indefinido.",
              "examples": [
                {"es": "Esta mañana he desayunado tostadas.", "ru": "Сегодня утром я ел тосты на завтрак."},
                {"es": "Ayer fui a la playa.", "ru": "Вчера я ходил на пляж."},
                {"es": "De pequeño siempre jugaba al fútbol.", "ru": "В детстве я всегда играл в футбол."}
              ]
            }
            """.trimIndent()
        ),

        // ══════════════════════════════════════════
        // B1 (расширение, сессия 10)
        // ══════════════════════════════════════════
        LessonEntity(
            id = 55, level = "B1", category = "grammar",
            title = "Subjuntivo con verbos de deseo",
            topic = "Желание + subjuntivo",
            xpReward = 25,
            contentJson = """
            {
              "theory": "Глаголы желания, требования и просьбы (querer, desear, esperar, pedir, rogar) требуют Subjuntivo в придаточном предложении, если у главного и придаточного разные подлежащие.",
              "rules": [
                "Querer que + subjuntivo: 'Quiero que vengas'",
                "Esperar que + subjuntivo: 'Espero que estés bien'",
                "Pedir / rogar / exigir que + subjuntivo: 'Te pido que me escuches'",
                "ВАЖНО: если подлежащее одно — используется инфинитив без 'que': 'Quiero ir' (я хочу идти), 'Quiero que vayas' (хочу, чтобы ты шёл)"
              ],
              "tip": "Главное правило: разные подлежащие в главном и придаточном → 'que' + subjuntivo. Одно подлежащее → просто инфинитив.",
              "examples": [
                {"es": "Quiero que me digas la verdad.", "ru": "Я хочу, чтобы ты сказал мне правду."},
                {"es": "Espero que tengas un buen viaje.", "ru": "Надеюсь, у тебя будет хорошее путешествие."},
                {"es": "Te pido que tengas paciencia.", "ru": "Прошу тебя иметь терпение."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 56, level = "B1", category = "grammar",
            title = "Subjuntivo con verbos de emoción",
            topic = "Эмоции + subjuntivo",
            xpReward = 25,
            contentJson = """
            {
              "theory": "Глаголы и выражения эмоций требуют Subjuntivo: alegrarse, lamentar, sentir, tener miedo, dar pena, и безличные конструкции 'es + adj. + que'.",
              "rules": [
                "Alegrarse de que + subj.: 'Me alegro de que vengas'",
                "Sentir que / lamentar que + subj.: 'Siento que no puedas venir'",
                "Tener miedo de que + subj.: 'Tengo miedo de que se enfade'",
                "Безличные: es bueno/malo/triste/raro/curioso que + subj.: 'Es raro que no llame'",
                "Если подлежащее одно — инфинитив без que: 'Me alegro de venir' vs 'Me alegro de que vengas'"
              ],
              "tip": "Эмоции — это субъективная реакция, не факт, поэтому нужен subjuntivo. Сравни: 'Es verdad que viene' (indic., факт) vs 'Es raro que venga' (subj., реакция).",
              "examples": [
                {"es": "Me alegro mucho de que estés aquí.", "ru": "Я очень рад, что ты здесь."},
                {"es": "Es una pena que no podamos vernos.", "ru": "Жаль, что мы не можем увидеться."},
                {"es": "Tengo miedo de que llueva mañana.", "ru": "Я боюсь, что завтра пойдёт дождь."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 57, level = "B1", category = "grammar",
            title = "Subjuntivo con duda y negación",
            topic = "Сомнение и отрицание",
            xpReward = 25,
            contentJson = """
            {
              "theory": "Выражения сомнения, отрицания мнения и отрицание истинности требуют Subjuntivo. Утверждение того же типа — Indicativo. Это одно из ключевых правил выбора наклонения.",
              "rules": [
                "No creer que / no pensar que + subj.: 'No creo que sea verdad'",
                "Dudar que + subj.: 'Dudo que venga'",
                "Negar que + subj.: 'Niego que sea su culpa'",
                "Утверждение → indicativo: 'Creo que es verdad', 'Pienso que viene'",
                "Вопрос с creer/pensar — выбор зависит от ожидания: '¿Crees que viene?' (нейтрально) / '¿Crees que venga?' (сомнение)"
              ],
              "tip": "Запомни как переключатель: добавил 'no' к 'creo/pienso' → надо переключить indicativo на subjuntivo.",
              "examples": [
                {"es": "No creo que tenga razón.", "ru": "Не думаю, что он прав."},
                {"es": "Dudo que llegue a tiempo.", "ru": "Сомневаюсь, что он успеет вовремя."},
                {"es": "No es verdad que esté enfermo.", "ru": "Неправда, что он болен."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 58, level = "B1", category = "grammar",
            title = "Imperativo negativo",
            topic = "Отрицательный императив",
            xpReward = 25,
            contentJson = """
            {
              "theory": "Отрицательный императив образуется иначе, чем утвердительный: используются формы Subjuntivo Presente. Ставится 'no' перед глаголом.",
              "rules": [
                "Tú: no + 2 л. ед.ч. subj.: no hables, no comas, no vengas",
                "Usted: no + 3 л. ед.ч. subj.: no hable, no coma, no venga",
                "Vosotros: no + 2 л. мн.ч. subj.: no habléis, no comáis, no vengáis",
                "Ustedes: no + 3 л. мн.ч. subj.: no hablen, no coman, no vengan",
                "Местоимения СТАВЯТСЯ ПЕРЕД глаголом, не присоединяются: 'no me lo digas'"
              ],
              "tip": "В утвердительном императиве местоимения присоединяются (dímelo), в отрицательном — отделяются и ставятся перед (no me lo digas). Главная ловушка для русскоговорящих.",
              "examples": [
                {"es": "No hables tan rápido.", "ru": "Не говори так быстро."},
                {"es": "No te preocupes, todo va a estar bien.", "ru": "Не переживай, всё будет хорошо."},
                {"es": "No me lo cuentes ahora.", "ru": "Не рассказывай мне это сейчас."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 59, level = "B1", category = "grammar",
            title = "Por vs Para — продвинутые случаи",
            topic = "Тонкости предлогов",
            xpReward = 25,
            contentJson = """
            {
              "theory": "На уровне B1 por и para выходят за рамки базовых правил. Появляются устойчивые выражения и тонкие смысловые различия, которые меняют значение фразы.",
              "rules": [
                "POR с действующим лицом в пассиве: 'fue escrito POR Cervantes'",
                "POR в обмен/замена: 'pago 10 euros por el libro', 'gracias por todo'",
                "POR + время приблизительно: 'por la mañana', 'por una hora'",
                "PARA + мнение: 'para mí, esto es difícil' (по-моему)",
                "PARA + сравнение/неожиданность: 'para ser nuevo, lo hace bien' (для новичка он хорош)"
              ],
              "tip": "Устойчивые выражения: por favor, por supuesto, por fin, por cierto, para siempre, para nada, no es para tanto. Учить как готовые блоки.",
              "examples": [
                {"es": "Para mí, este es el mejor restaurante.", "ru": "По-моему, это лучший ресторан."},
                {"es": "Te llamo por lo del trabajo.", "ru": "Звоню тебе по поводу работы."},
                {"es": "Para ser principiante, hablas muy bien.", "ru": "Для начинающего ты говоришь очень хорошо."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 60, level = "B1", category = "grammar",
            title = "Ojalá + subjuntivo",
            topic = "Выражение надежды",
            xpReward = 25,
            contentJson = """
            {
              "theory": "Слово ojalá (от арабского 'если на то воля Аллаха') выражает надежду или желание. Всегда используется с Subjuntivo. Время subjuntivo меняет вероятность.",
              "rules": [
                "Ojalá + presente subj. = надежда на возможное будущее: 'Ojalá venga' (надеюсь, придёт)",
                "Ojalá + imperfecto subj. = маловероятное / гипотетическое: 'Ojalá viniera' (хотел бы я, чтобы он пришёл)",
                "Ojalá + pretérito perfecto subj. = надежда на уже произошедшее: 'Ojalá haya aprobado' (надеюсь, сдал)",
                "Ojalá + pluscuamperfecto subj. = сожаление о прошлом: 'Ojalá hubiera estudiado más' (если бы я больше учился)",
                "Опционально с 'que': 'Ojalá (que) venga' — оба варианта правильны"
              ],
              "tip": "Ojalá переводится как 'хоть бы', 'если бы', 'дай бог'. Запомни лестницу вероятности: presente = реально, imperfecto = мечта, pluscuamperfecto = сожаление.",
              "examples": [
                {"es": "Ojalá haga buen tiempo mañana.", "ru": "Хоть бы завтра была хорошая погода."},
                {"es": "Ojalá tuviera más tiempo libre.", "ru": "Хотел бы я иметь больше свободного времени."},
                {"es": "Ojalá hubiera ido a la fiesta.", "ru": "Жаль, что я не пошёл на вечеринку."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 61, level = "B1", category = "grammar",
            title = "Antes de / después de + infinitivo",
            topic = "Временные конструкции",
            xpReward = 25,
            contentJson = """
            {
              "theory": "Чтобы выразить 'до того, как' и 'после того, как' с одним подлежащим, используется инфинитив. С разными подлежащими — придаточное с subjuntivo (для будущего) или indicativo (для прошлого/настоящего).",
              "rules": [
                "Antes de + инфинитив (одно подлежащее): 'Antes de salir, cierro la puerta'",
                "Antes de que + subjuntivo (разные подлежащие): 'Antes de que salgas, cierra la puerta'",
                "Después de + инфинитив (одно подлежащее): 'Después de comer, descansé'",
                "Después de que + indicativo (прошлое) или subjuntivo (будущее): 'Después de que llegó' / 'Después de que llegue'",
                "Al + инфинитив = когда: 'Al llegar a casa, vi el mensaje' (когда я пришёл домой)"
              ],
              "tip": "Запомни: 'antes de QUE' всегда требует subjuntivo, потому что это ещё не произошло.",
              "examples": [
                {"es": "Antes de acostarme, leo un poco.", "ru": "Перед тем как лечь, я немного читаю."},
                {"es": "Antes de que te vayas, dime una cosa.", "ru": "Прежде чем ты уйдёшь, скажи мне одну вещь."},
                {"es": "Después de cenar fuimos al parque.", "ru": "После ужина мы пошли в парк."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 62, level = "B1", category = "grammar",
            title = "Estar + participio",
            topic = "Состояние и результат",
            xpReward = 25,
            contentJson = """
            {
              "theory": "Конструкция 'estar + причастие' описывает состояние, которое является результатом завершённого действия. Не путать с пассивной конструкцией 'ser + participio'.",
              "rules": [
                "Estar + participio = результат, состояние: 'La puerta está cerrada' (дверь закрыта — состояние)",
                "Ser + participio = действие, пассивный залог: 'La puerta fue cerrada por Juan' (дверь была закрыта Хуаном — действие)",
                "Причастие согласуется в роде и числе: las puertas están cerradas",
                "Часто используется с глаголами окончания/состояния: cansado, dormido, casado, roto, abierto"
              ],
              "tip": "Тест: можно ли заменить 'estar + part.' на прилагательное? Да → это состояние (estoy cansado = устал). С 'ser' было бы действие.",
              "examples": [
                {"es": "La ventana está abierta.", "ru": "Окно открыто."},
                {"es": "Estoy muy preocupado por ti.", "ru": "Я очень переживаю за тебя."},
                {"es": "Las cartas ya están escritas.", "ru": "Письма уже написаны."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 63, level = "B1", category = "grammar",
            title = "Verbos de cambio: ponerse, volverse, hacerse",
            topic = "Глаголы становления",
            xpReward = 25,
            contentJson = """
            {
              "theory": "В испанском нет одного глагола 'становиться'. Вместо этого есть несколько глаголов, выбор которых зависит от типа изменения: эмоциональное, постепенное, добровольное.",
              "rules": [
                "Ponerse + adj. = временное состояние, эмоция: ponerse triste, ponerse rojo (краснеть)",
                "Volverse + adj. = радикальное / непроизвольное изменение: se volvió loco, se volvió cínico",
                "Hacerse + sust./adj. = добровольное / постепенное: se hizo médico, se hizo rico",
                "Llegar a ser + sust. = достичь чего-то после усилий: llegó a ser presidente",
                "Convertirse en + sust. = превратиться в: se convirtió en una estrella"
              ],
              "tip": "Шпаргалка: ponerse = на минутку (эмоция), volverse = резко (характер), hacerse = по своей воле (профессия), llegar a ser = после долгого пути.",
              "examples": [
                {"es": "Cuando lo vio, se puso muy nervioso.", "ru": "Когда он его увидел, очень разнервничался."},
                {"es": "Mi tío se hizo abogado a los 40 años.", "ru": "Мой дядя стал адвокатом в 40 лет."},
                {"es": "La situación se volvió insoportable.", "ru": "Ситуация стала невыносимой."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 64, level = "B1", category = "grammar",
            title = "Llevar + tiempo + gerundio",
            topic = "Длительность действия",
            xpReward = 25,
            contentJson = """
            {
              "theory": "Конструкция 'llevar + время + герундий' выражает длительность действия, которое началось в прошлом и продолжается в настоящем. Очень частая разговорная конструкция.",
              "rules": [
                "Формула: llevar (в нужном времени) + период времени + gerundio",
                "Llevo dos años estudiando español = я учу испанский уже два года",
                "Llevo 10 minutos esperando = я жду 10 минут",
                "В прошлом: imperfecto de llevar: llevaba dos años viviendo allí (я жил там уже два года)",
                "Эквивалент: 'hace + tiempo + que + presente' / 'presente + desde hace + tiempo'"
              ],
              "tip": "Все три варианта эквивалентны: 'Llevo dos años aquí' = 'Hace dos años que estoy aquí' = 'Estoy aquí desde hace dos años'.",
              "examples": [
                {"es": "Llevo media hora esperando el autobús.", "ru": "Я жду автобус уже полчаса."},
                {"es": "Llevamos cinco años casados.", "ru": "Мы женаты уже пять лет."},
                {"es": "Llevaba mucho tiempo soñando con este viaje.", "ru": "Я давно мечтал об этом путешествии."}
              ]
            }
            """.trimIndent()
        ),

        // ══════════════════════════════════════════
        // B2 (расширение, сессия 10)
        // ══════════════════════════════════════════
        LessonEntity(
            id = 65, level = "B2", category = "grammar",
            title = "Condicional Compuesto",
            topic = "Сложное условное",
            xpReward = 30,
            contentJson = """
            {
              "theory": "Condicional Compuesto (habría hablado) выражает действие, которое могло бы быть завершено в прошлом, но не произошло. Используется в нереальных условиях прошлого и для гипотез.",
              "rules": [
                "Формула: condicional simple от haber + participio",
                "habría, habrías, habría, habríamos, habríais, habrían + hablado/comido/vivido",
                "Главное использование — в условных 3 типа: 'Si hubiera estudiado, habría aprobado'",
                "Также для предположений о завершённых действиях прошлого: 'Habría sido las nueve' (наверное, было девять)",
                "В разговорной речи часто заменяется на pluscuamperfecto subj.: 'hubiera aprobado'"
              ],
              "tip": "Парная конструкция: Si + pluscuamperfecto subj., condicional compuesto. Это шаблон сожалений о прошлом.",
              "examples": [
                {"es": "Si hubiera salido antes, no habría llegado tarde.", "ru": "Если бы я вышел раньше, не опоздал бы."},
                {"es": "Te habría llamado, pero perdí tu número.", "ru": "Я бы тебе позвонил, но потерял твой номер."},
                {"es": "¿Qué habrías hecho en mi lugar?", "ru": "Что бы ты сделал на моём месте?"}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 66, level = "B2", category = "grammar",
            title = "Pretérito Perfecto de Subjuntivo",
            topic = "Haya hablado",
            xpReward = 30,
            contentJson = """
            {
              "theory": "Subjuntivo Perfecto (haya hablado) описывает завершённые действия в контексте, требующем subjuntivo: эмоция, сомнение, отрицание о уже произошедшем событии.",
              "rules": [
                "Формула: presente de subj. от haber + participio",
                "haya, hayas, haya, hayamos, hayáis, hayan + hablado/comido/vivido",
                "Используется когда главное в настоящем/будущем, а действие придаточного УЖЕ произошло",
                "Триггеры: no creo que haya..., me alegro de que hayas..., es posible que haya...",
                "Сравни: 'No creo que venga' (он придёт) vs 'No creo que haya venido' (он уже пришёл)"
              ],
              "tip": "Используй когда нужно выразить субъективное отношение (сомнение, радость, страх) к УЖЕ совершённому действию.",
              "examples": [
                {"es": "Me alegro de que hayas venido.", "ru": "Я рад, что ты пришёл."},
                {"es": "No creo que hayan terminado todavía.", "ru": "Не думаю, что они уже закончили."},
                {"es": "Es posible que haya perdido el tren.", "ru": "Возможно, он опоздал на поезд."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 67, level = "B2", category = "grammar",
            title = "Perífrasis con infinitivo",
            topic = "ponerse a / dejar de / volver a",
            xpReward = 30,
            contentJson = """
            {
              "theory": "Перифразы с инфинитивом передают тонкие смысловые оттенки начала, конца, повтора и неизбежности действия. Они часто заменяют целые предложения и обогащают речь.",
              "rules": [
                "Ponerse a + inf = начать (внезапно): se puso a llorar (заплакал)",
                "Empezar / comenzar a + inf = начать (нейтрально): empezó a llover",
                "Dejar de + inf = перестать: dejó de fumar (бросил курить)",
                "Volver a + inf = снова: vuelvo a leerlo (перечитываю)",
                "Llegar a + inf = (так и) сделать (после усилий): llegó a entenderlo",
                "Tener que / haber de + inf = должен (обязательство)"
              ],
              "tip": "Сравни нюансы: 'empezar a llorar' (нейтрально начал плакать), 'ponerse a llorar' (вдруг разрыдался), 'echarse a llorar' (резко разревелся).",
              "examples": [
                {"es": "De repente se puso a gritar.", "ru": "Вдруг он начал кричать."},
                {"es": "Tienes que dejar de preocuparte tanto.", "ru": "Тебе нужно перестать так переживать."},
                {"es": "Vuelve a explicármelo, por favor.", "ru": "Объясни мне это ещё раз, пожалуйста."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 68, level = "B2", category = "grammar",
            title = "Perífrasis con gerundio",
            topic = "seguir / llevar / andar + gerundio",
            xpReward = 30,
            contentJson = """
            {
              "theory": "Перифразы с герундием выражают длящиеся, повторяющиеся или продолжающиеся действия. Каждая имеет свой оттенок смысла.",
              "rules": [
                "Estar + gerundio = действие в процессе: estoy comiendo",
                "Seguir / continuar + gerundio = продолжать делать: sigo estudiando",
                "Llevar + tiempo + gerundio = длительность с момента начала: llevo años estudiando",
                "Ir + gerundio = постепенное развитие: va mejorando (постепенно улучшается)",
                "Andar + gerundio = делать туда-сюда / без особого порядка: anda buscando trabajo",
                "Venir + gerundio = делать с давних пор до настоящего: venimos diciéndolo desde hace meses"
              ],
              "tip": "Различай: 'voy aprendiendo' (постепенно учу) vs 'sigo aprendiendo' (продолжаю учить) vs 'llevo años aprendiendo' (учу годами).",
              "examples": [
                {"es": "Sigo trabajando en el mismo proyecto.", "ru": "Я продолжаю работать над тем же проектом."},
                {"es": "Anda diciendo tonterías últimamente.", "ru": "Он в последнее время несёт глупости."},
                {"es": "El paciente va recuperándose poco a poco.", "ru": "Пациент потихоньку восстанавливается."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 69, level = "B2", category = "grammar",
            title = "Perífrasis con participio",
            topic = "tener / llevar + participio",
            xpReward = 30,
            contentJson = """
            {
              "theory": "Перифразы с причастием подчёркивают результат накопленного действия. В отличие от 'haber + part.' (нейтральное прошедшее), эти конструкции акцентируют объём проделанного.",
              "rules": [
                "Tener + participio = накопленный результат: tengo escritos tres libros (я уже написал три книги)",
                "Llevar + participio = аналогично, акцент на количество: llevo leídas 200 páginas",
                "Dejar + participio = оставить в каком-то состоянии: dejó hecho el trabajo",
                "Ir + participio (только пассив) = сделанные / готовые: van vendidas 100 entradas",
                "Причастие согласуется с прямым дополнением в роде и числе"
              ],
              "tip": "Различай: 'he escrito tres libros' (нейтрально — написал) vs 'tengo escritos tres libros' (акцент на достижение, процесс).",
              "examples": [
                {"es": "Llevo estudiados cinco capítulos.", "ru": "Я выучил уже пять глав."},
                {"es": "Tengo preparada la cena.", "ru": "Ужин у меня уже готов."},
                {"es": "Dejé acabados todos los informes.", "ru": "Я оставил все отчёты завершёнными."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 70, level = "B2", category = "grammar",
            title = "Construcciones impersonales",
            topic = "Безличные конструкции",
            xpReward = 30,
            contentJson = """
            {
              "theory": "Безличные конструкции в испанском передают действия без указания субъекта. Используются для обобщений, новостей, объявлений и формальных текстов.",
              "rules": [
                "Se + 3 л. ед.ч. (без подлежащего): 'Se vive bien aquí' (здесь хорошо живётся)",
                "Se + 3 л. ед.ч./мн.ч. (пассив с агентом-объектом): 'Se vende un coche' / 'Se venden coches'",
                "3 л. мн.ч. без подлежащего: 'Dicen que va a llover' (говорят, что пойдёт дождь)",
                "Uno / una + 3 л. ед.ч.: 'Uno nunca sabe' (никогда не знаешь)",
                "Hay que + inf = надо: hay que estudiar"
              ],
              "tip": "Когда не знаешь или не важен 'кто', выбирай безличность: 'se dice que' звучит более естественно, чем 'la gente dice que'.",
              "examples": [
                {"es": "Se come muy bien en este restaurante.", "ru": "В этом ресторане очень хорошо кормят."},
                {"es": "Dicen que va a subir el precio.", "ru": "Говорят, что цена вырастет."},
                {"es": "Hay que tener paciencia.", "ru": "Надо иметь терпение."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 71, level = "B2", category = "grammar",
            title = "A pesar de / pese a",
            topic = "Уступительные обороты",
            xpReward = 30,
            contentJson = """
            {
              "theory": "Конструкции a pesar de и pese a означают 'несмотря на'. Они могут вводить как существительное / инфинитив, так и придаточное предложение с выбором между indicativo и subjuntivo.",
              "rules": [
                "A pesar de / pese a + sust.: 'A pesar de la lluvia, salimos'",
                "A pesar de / pese a + inf.: 'A pesar de estar cansado, fui'",
                "A pesar de que / pese a que + indic. (реальный факт): 'A pesar de que llueve, salgo'",
                "A pesar de que / pese a que + subj. (предположение/уступка): 'A pesar de que llueva, saldré'",
                "Синонимы: aun cuando, si bien (более книжные)"
              ],
              "tip": "В формальной речи a pesar de звучит лучше, чем aunque. В газетных статьях и эссе они почти всегда заменяют простое 'aunque'.",
              "examples": [
                {"es": "A pesar de la crisis, la empresa creció.", "ru": "Несмотря на кризис, компания выросла."},
                {"es": "Pese a que sabía la verdad, no dijo nada.", "ru": "Хотя он знал правду, ничего не сказал."},
                {"es": "Iré a pesar de que no me apetezca.", "ru": "Я пойду, даже если мне не захочется."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 72, level = "B2", category = "grammar",
            title = "Ser vs Estar — продвинутые случаи",
            topic = "Идиомы и тонкости",
            xpReward = 30,
            contentJson = """
            {
              "theory": "С одним и тем же прилагательным ser и estar дают разный смысл. На уровне B2 это уже не правило 'постоянное/временное', а разные значения слова.",
              "rules": [
                "Ser bueno = быть хорошим (по характеру) / Estar bueno = быть вкусным или (разг.) привлекательным",
                "Ser listo = быть умным / Estar listo = быть готовым",
                "Ser aburrido = быть скучным / Estar aburrido = скучать",
                "Ser rico = быть богатым / Estar rico = быть вкусным",
                "Идиомы: ser un cero a la izquierda (быть никем), estar como una cabra (быть с приветом)",
                "Estar de + профессия = временно работать кем-то: estoy de camarero este verano"
              ],
              "tip": "Если сомневаешься — спрашивай себя 'это качество (ser) или состояние (estar)?'. 'Pedro es callado' — он молчун по натуре. 'Pedro está callado' — он сейчас молчит.",
              "examples": [
                {"es": "Esta sopa está riquísima.", "ru": "Этот суп невероятно вкусный."},
                {"es": "Mi hermano es muy listo, siempre saca buenas notas.", "ru": "Мой брат очень умный, всегда получает хорошие оценки."},
                {"es": "Tu primo está como una cabra.", "ru": "Твой двоюродный брат не в себе."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 73, level = "B2", category = "grammar",
            title = "Estilo indirecto avanzado",
            topic = "Косвенная речь — нюансы",
            xpReward = 30,
            contentJson = """
            {
              "theory": "На B2 косвенная речь требует точного согласования всех времён, изменения местоимений, временных и пространственных маркеров, а также понимания, какие времена не сдвигаются.",
              "rules": [
                "Imperfecto и Pluscuamperfecto в косвенной речи НЕ меняются (они уже самые прошлые)",
                "Subjuntivo тоже сдвигается: presente subj. → imperfecto subj.",
                "Условные предложения сохраняют структуру: 'si tuviera, iría' → 'dijo que si tuviera, iría'",
                "Маркеры: hoy → aquel día, ayer → el día anterior, mañana → al día siguiente, aquí → allí, esto → eso",
                "Изменения глагола: traer/llevar, venir/ir в зависимости от позиции говорящего"
              ],
              "tip": "Полезный приём: представь, что передаёшь сообщение кому-то ещё. Тогда автоматически меняешь 'я' на 'он/она' и сдвигаешь всё во времени.",
              "examples": [
                {"es": "Me dijo: 'Te llamaré mañana' → Me dijo que me llamaría al día siguiente.", "ru": "Он сказал, что позвонит мне на следующий день."},
                {"es": "Preguntó si yo había leído ese libro.", "ru": "Он спросил, читал ли я ту книгу."},
                {"es": "Comentó que si tuviera tiempo, vendría a vernos.", "ru": "Он заметил, что если бы у него было время, он бы пришёл к нам."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 74, level = "B2", category = "grammar",
            title = "Pronombres relativos avanzados",
            topic = "el cual, lo cual, cuyo",
            xpReward = 30,
            contentJson = """
            {
              "theory": "На B2 относительные местоимения используются точнее: el cual для уточнения, lo cual для отсылки к фразе, cuyo для принадлежности. Особенно важны после предлогов и в письменной речи.",
              "rules": [
                "El/la/los/las + cual + ставится после длинных предлогов: 'la persona detrás de la cual está sentado'",
                "Lo cual = отсылка к целой ситуации (не к отдельному слову): 'Llegó tarde, lo cual me molestó'",
                "Lo que = 'то, что' без конкретного антецедента: 'No sé lo que quiero'",
                "Cuyo/a/os/as согласуется с тем, чем владеют: 'el escritor cuyas novelas leo' (книги, не писатель)",
                "В разговорной речи 'el cual' редко — заменяется на 'que' или 'el que'"
              ],
              "tip": "Lo cual нельзя начинать предложение — оно всегда после запятой. Lo que может начинать: 'Lo que más me gusta es viajar'.",
              "examples": [
                {"es": "Vivimos en una casa cuyas ventanas dan al mar.", "ru": "Мы живём в доме, окна которого выходят на море."},
                {"es": "No vino, lo cual me sorprendió mucho.", "ru": "Он не пришёл, что меня очень удивило."},
                {"es": "Es el motivo por el cual tomé esta decisión.", "ru": "Это причина, по которой я принял это решение."}
              ]
            }
            """.trimIndent()
        ),
        LessonEntity(
            id = 75, level = "B2", category = "grammar",
            title = "Expresiones idiomáticas comunes",
            topic = "Частые идиомы",
            xpReward = 30,
            contentJson = """
            {
              "theory": "Идиомы — это устойчивые выражения, смысл которых не выводится из значений отдельных слов. Их знание принципиально отличает уровень B2 от B1: речь становится живой и естественной.",
              "rules": [
                "Tomar el pelo a alguien = подшучивать, разыгрывать (буквально: 'тянуть за волосы')",
                "Estar en las nubes = витать в облаках, быть невнимательным",
                "Echar una mano = помочь (буквально: 'протянуть руку')",
                "No tener pelos en la lengua = говорить прямо, без обиняков",
                "Ser pan comido = проще простого (буквально: 'это съеденный хлеб')",
                "Costar un ojo de la cara = стоить очень дорого",
                "Dar en el clavo = попасть в точку"
              ],
              "tip": "Учи идиомы блоками по 3-5 штук в неделю. Слушай испанские сериалы и подкасты — там идиомы встречаются в естественном контексте.",
              "examples": [
                {"es": "¿Me estás tomando el pelo?", "ru": "Ты надо мной издеваешься?"},
                {"es": "Ese examen fue pan comido.", "ru": "Тот экзамен был проще простого."},
                {"es": "¿Me echas una mano con esto?", "ru": "Поможешь мне с этим?"}
              ]
            }
            """.trimIndent()
        )
    )
}
