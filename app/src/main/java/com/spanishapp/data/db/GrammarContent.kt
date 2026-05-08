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
        )
    )
}
