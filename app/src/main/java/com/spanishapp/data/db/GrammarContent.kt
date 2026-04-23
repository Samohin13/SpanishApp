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
        )
    )
}
