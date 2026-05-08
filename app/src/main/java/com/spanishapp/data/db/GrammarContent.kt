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
        )
    )
}
