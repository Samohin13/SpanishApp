package com.spanishapp.data.db

import com.spanishapp.data.db.entity.WordEntity

/**
 * VocabExpansion13 — ~320 фраз:
 *   - разговорные A1–B2 (~180)
 *   - устойчивые выражения / идиомы B2–C1 (~90)
 *   - 18+ разговорные (~50, категория adult_expresiones)
 */
object VocabExpansion13 {

    private fun ph(es: String, ru: String, ex: String = "", level: String = "B1", cat: String = "expresiones") =
        WordEntity(spanish = es, russian = ru, example = ex, level = level, category = cat, wordType = "phrase")

    private fun adult(es: String, ru: String, ex: String = "", level: String = "B2") =
        WordEntity(spanish = es, russian = ru, example = ex, level = level, category = "adult_expresiones", wordType = "phrase")

    val entries: List<WordEntity> get() = cotidianas + despedidas_saludos +
        emociones_ph + viajes_ph + trabajo_ph + comida_ph +
        idiomas_es + idiomas_b2c1 + adulto

    // ── ПОВСЕДНЕВНЫЕ ФРАЗЫ A1/A2 ─────────────────────────────────
    private val cotidianas = listOf(
        ph("¿Qué tal te va?",           "Как дела?",                    "¿Qué tal te va? — Bien, gracias.", "A1"),
        ph("¡Hasta pronto!",            "До скорого!",                  "¡Hasta pronto, nos vemos!",         "A1"),
        ph("¿De dónde eres?",           "Откуда ты?",                   "¿De dónde eres? — De Madrid.",      "A1"),
        ph("¿Cuánto cuesta?",           "Сколько стоит?",               "¿Cuánto cuesta esto?",              "A1"),
        ph("No entiendo",               "Я не понимаю",                 "No entiendo, ¿puedes repetir?",     "A1"),
        ph("¿Puedes hablar más despacio?","Можешь говорить медленнее?", "Por favor, más despacio.",          "A1"),
        ph("¿Dónde está el baño?",      "Где туалет?",                  "Perdona, ¿dónde está el baño?",     "A1"),
        ph("¿Me puedes ayudar?",        "Можешь мне помочь?",           "¿Me puedes ayudar con esto?",       "A1"),
        ph("No hay problema",           "Нет проблем",                  "¿Te molesta? — No hay problema.",   "A1"),
        ph("¡Qué bueno!",               "Как здорово! / Как вкусно!",   "¡Qué bueno está este plato!",       "A1"),
        ph("¿A qué hora?",              "В котором часу?",              "¿A qué hora quedamos?",             "A1"),
        ph("Me llamo...",               "Меня зовут...",                "Me llamo Ana, ¿y tú?",              "A1"),
        ph("¿Cómo se dice en español?", "Как это по-испански?",         "¿Cómo se dice 'thank you'?",        "A1"),
        ph("¿Qué significa?",           "Что это значит?",              "¿Qué significa esta palabra?",      "A1"),
        ph("Tengo hambre",              "Я голоден/голодна",            "Tengo hambre, ¿comemos?",           "A1"),
        ph("Tengo sed",                 "Я хочу пить",                  "Tengo sed, dame agua.",              "A1"),
        ph("¡Qué lástima!",             "Как жаль!",                    "¡Qué lástima que no puedas venir!", "A1"),
        ph("Con permiso",               "Разрешите пройти",             "Con permiso, quiero salir.",        "A1"),
        ph("¡Enhorabuena!",             "Поздравляю!",                  "¡Enhorabuena por el trabajo!",      "A2"),
        ph("¡Ánimo!",                   "Не сдавайся! / Давай!",        "¡Ánimo, tú puedes!",                "A2"),
        ph("¡Qué pena!",                "Как жаль!",                    "¡Qué pena que llueva hoy!",         "A2"),
        ph("¿Qué pasa?",                "Что происходит?",              "¿Qué pasa aquí?",                   "A2"),
        ph("Depende",                   "Зависит",                      "¿Vas a ir? — Depende.",             "A2"),
        ph("Más o menos",               "Более-менее",                  "¿Cómo estás? — Más o menos.",       "A2"),
        ph("Tal vez",                   "Возможно / Может быть",        "Tal vez llueva mañana.",             "A2"),
        ph("A ver",                     "Посмотрим / Ну-ка",            "A ver qué pasa.",                   "A2"),
        ph("Al final",                  "В конце концов",               "Al final todo salió bien.",          "A2"),
        ph("De todas formas",           "Во всяком случае",             "De todas formas, gracias.",          "A2"),
        ph("Me da igual",               "Мне всё равно",                "¿Rojo o azul? — Me da igual.",      "A2"),
        ph("¿Qué quieres decir?",       "Что ты имеешь в виду?",        "No entiendo. ¿Qué quieres decir?",  "A2"),
        ph("¡Venga ya!",                "Да ладно! / Не может быть!",   "¡Venga ya! No me lo creo.",         "A2"),
        ph("Ni idea",                   "Понятия не имею",              "¿Dónde está? — Ni idea.",           "A2"),
        ph("¡Claro que sí!",            "Конечно!",                     "¿Puedo pasar? — ¡Claro que sí!",   "A2"),
        ph("Para nada",                 "Совсем нет",                   "¿Te molesta? — Para nada.",         "A2"),
        ph("¡Eso es!",                  "Именно! / Вот именно!",        "¡Eso es! Lo has entendido.",        "A2"),
    )

    // ── ПРИВЕТСТВИЯ / ПРОЩАНИЯ ────────────────────────────────────
    private val despedidas_saludos = listOf(
        ph("¿Qué hay de nuevo?",        "Что нового?",                  "¡Hola! ¿Qué hay de nuevo?",         "A2"),
        ph("¿Qué es de tu vida?",       "Как ты живёшь? / Что слышно?", "Hacía tiempo. ¿Qué es de tu vida?", "B1"),
        ph("¡Cuánto tiempo sin verte!", "Сколько лет, сколько зим!",    "¡Cuánto tiempo sin verte, amigo!",  "B1"),
        ph("Que te vaya bien",          "Удачи тебе",                   "Adiós, que te vaya bien.",           "A2"),
        ph("Hasta otra",                "До следующего раза",           "Fue un placer. ¡Hasta otra!",        "A2"),
        ph("Que descanses",             "Отдыхай / Спокойной ночи",     "Buenas noches, que descanses.",      "A2"),
        ph("Que aproveche",             "Приятного аппетита",           "¡Que aproveche!",                   "A1"),
        ph("Que te mejores",            "Поправляйся",                  "Estás enfermo. ¡Que te mejores!",   "A2"),
        ph("Que tengas suerte",         "Желаю удачи",                  "Mañana tienes examen. ¡Suerte!",    "A2"),
    )

    // ── ФРАЗЫ ОБ ЭМОЦИЯХ / ОЩУЩЕНИЯХ ────────────────────────────
    private val emociones_ph = listOf(
        ph("Estoy harto de...",         "Я сыт по горло...",            "Estoy harto de esperar.",            "B1"),
        ph("Me pone de los nervios",    "Это меня нервирует",           "El ruido me pone de los nervios.",   "B1"),
        ph("Se me hace la boca agua",   "Слюнки текут",                 "Al ver ese pastel se me hace la boca agua.", "B1"),
        ph("Se me olvidó",              "Я забыл/а",                    "Se me olvidó el móvil en casa.",     "A2"),
        ph("Me importa un bledo",       "Мне наплевать",                "¿Qué piensa él? Me importa un bledo.","B2"),
        ph("No puedo más",              "Я больше не могу",             "Estoy agotado. No puedo más.",       "B1"),
        ph("Me cae bien",               "Он/она мне нравится (как человек)","Me cae muy bien tu amigo.",    "B1"),
        ph("Me cae mal",                "Он/она мне неприятен/а",       "Ese vecino me cae mal.",             "B1"),
        ph("Tiene mala leche",          "Он злой / не в духе",          "Hoy tiene muy mala leche.",          "B2"),
        ph("Estoy en las nubes",        "Я витаю в облаках",            "No me escucha, está en las nubes.",  "B2"),
        ph("Me da miedo",               "Мне страшно",                  "Me da miedo la oscuridad.",          "A2"),
        ph("Me da vergüenza",           "Мне стыдно",                   "Me da vergüenza preguntar.",         "B1"),
        ph("Estoy hasta las narices",   "Мне всё надоело",              "Estoy hasta las narices del trabajo.","B2"),
        ph("Ponerse en el lugar de",    "Поставить себя на место кого-то","Ponte en mi lugar.",               "B2"),
        ph("A mí me parece que",        "Мне кажется, что",             "A mí me parece que tiene razón.",   "B1"),
    )

    // ── ФРАЗЫ О ПУТЕШЕСТВИЯХ ──────────────────────────────────────
    private val viajes_ph = listOf(
        ph("¿Cómo se llega a...?",      "Как добраться до...?",         "¿Cómo se llega al centro?",          "A2"),
        ph("¿Está lejos de aquí?",      "Это далеко отсюда?",           "¿Está lejos el museo?",              "A2"),
        ph("Gira a la derecha",         "Поверни направо",              "Gira a la derecha en el semáforo.",  "A2"),
        ph("Todo recto",                "Прямо",                        "Sigue todo recto.",                  "A1"),
        ph("Quisiera una habitación",   "Я хотел бы номер",             "Quisiera una habitación doble.",     "A2"),
        ph("¿Está incluido el desayuno?","Завтрак включён?",            "¿Está incluido el desayuno?",        "B1"),
        ph("Me han robado",             "Меня обокрали",                "¡Me han robado la cartera!",         "B1"),
        ph("¿Dónde está la embajada?",  "Где находится посольство?",    "¿Dónde está la embajada rusa?",      "B1"),
        ph("He perdido el pasaporte",   "Я потерял паспорт",            "He perdido el pasaporte, ¡ayuda!",   "B1"),
        ph("¿Hay conexión WiFi?",       "Есть WiFi?",                   "¿Hay conexión WiFi en el hotel?",    "A2"),
        ph("¿Dónde puedo cambiar dinero?","Где можно обменять деньги?", "¿Dónde puedo cambiar euros?",        "A2"),
    )

    // ── РАБОЧИЕ ФРАЗЫ ────────────────────────────────────────────
    private val trabajo_ph = listOf(
        ph("Tengo una reunión",         "У меня встреча",               "Tengo una reunión a las diez.",      "B1"),
        ph("Estoy de baja",             "Я на больничном",              "Esta semana estoy de baja.",          "B1"),
        ph("Pedir un aumento",          "Просить о повышении зарплаты", "Voy a pedir un aumento.",            "B1"),
        ph("Hacer horas extra",         "Работать сверхурочно",         "Esta semana hago horas extra.",       "B1"),
        ph("Dar de alta",               "Оформить (в системе / на работу)","Te damos de alta en el sistema.", "B2"),
        ph("Darse de baja",             "Отписаться / уволиться",       "Me di de baja del gimnasio.",        "B2"),
        ph("Firmar un contrato",        "Подписать договор",            "Mañana firmo el contrato.",           "B1"),
        ph("Estar en paro",             "Быть безработным",             "Lleva meses en paro.",               "B1"),
        ph("Cubrir el expediente",      "Соблюдать формальности",       "Solo cubrimos el expediente.",        "C1"),
        ph("Pasar el marrón",           "Свалить неприятное дело на другого","Me pasó el marrón del informe.", "B2"),
        ph("Trabajar en equipo",        "Работать в команде",           "Hay que trabajar en equipo.",        "B1"),
        ph("Hacer una propuesta",       "Сделать предложение",          "Hizo una propuesta excelente.",       "B1"),
    )

    // ── ФРАЗЫ О ЕДЕ / РЕСТОРАНЕ ──────────────────────────────────
    private val comida_ph = listOf(
        ph("¿Qué me recomienda?",       "Что порекомендуете?",          "¿Qué me recomienda de la carta?",    "A2"),
        ph("La cuenta, por favor",      "Счёт, пожалуйста",             "La cuenta, por favor.",              "A1"),
        ph("¿Está incluido el servicio?","Обслуживание включено?",      "¿Está incluido el servicio?",        "B1"),
        ph("Soy alérgico a...",         "У меня аллергия на...",        "Soy alérgico al marisco.",           "B1"),
        ph("¿Tiene opción vegetariana?","Есть вегетарианское?",         "¿Tiene opción vegetariana?",         "A2"),
        ph("Sin gluten",                "Без глютена",                  "¿Tiene platos sin gluten?",          "B1"),
        ph("Para llevar",               "С собой / на вынос",           "¿Me lo pone para llevar?",           "A2"),
        ph("Está para chuparse los dedos","Пальчики оближешь",          "¡Este plato está para chuparse los dedos!", "B1"),
        ph("Tiene buena pinta",         "Выглядит аппетитно",           "La tarta tiene muy buena pinta.",    "B1"),
        ph("¿Está hecho al punto?",     "Средней прожарки?",            "¿El filete está al punto?",          "B2"),
    )

    // ── ИДИОМЫ / УСТОЙЧИВЫЕ ВЫРАЖЕНИЯ B1/B2 ─────────────────────
    private val idiomas_es = listOf(
        ph("No hay mal que por bien no venga","Нет худа без добра",     "Perdí el trabajo pero encontré otro mejor.", "B2"),
        ph("A mal tiempo buena cara",   "Встречай трудности с улыбкой", "A mal tiempo buena cara.",           "B2"),
        ph("El que la sigue la consigue","Терпение и труд всё перетрут","El que la sigue la consigue.",        "B2"),
        ph("Más vale tarde que nunca",  "Лучше поздно, чем никогда",   "Llegó tarde. Más vale tarde que nunca.", "B1"),
        ph("Camarón que se duerme se lo lleva la corriente","Кто зевает — тот проигрывает","Hay que actuar ya.", "B2"),
        ph("A quien madruga Dios le ayuda","Кто рано встаёт, тому Бог даёт","A quien madruga Dios le ayuda.","B2"),
        ph("En casa del herrero cuchillo de palo","Сапожник без сапог","En casa del herrero cuchillo de palo.","B2"),
        ph("No hay que llorar sobre la leche derramada","Что случилось, то случилось","Ya no hay remedio.",   "B2"),
        ph("Más sabe el diablo por viejo que por diablo","Опыт — лучший учитель","La experiencia enseña.",   "B2"),
        ph("Dime con quién andas y te diré quién eres","Скажи мне кто твой друг","Elige bien tus amigos.",   "B2"),
        ph("Ojos que no ven corazón que no siente","С глаз долой — из сердца вон","Fuera de la vista...",    "B2"),
        ph("No todo lo que brilla es oro","Не всё то золото, что блестит","Cuidado con las apariencias.",   "B2"),
        ph("Costar un ojo de la cara",  "Стоить бешеных денег",        "Este abrigo me costó un ojo de la cara.","B1"),
        ph("Estar en las últimas",      "Быть при последнем издыхании","La empresa está en las últimas.",    "B2"),
        ph("Ponerse las pilas",         "Взяться за дело",              "¡Ponte las pilas si quieres aprobar!","B1"),
        ph("Dar en el clavo",           "Попасть в точку",              "Diste en el clavo con ese argumento.","B2"),
        ph("Meter la pata",             "Облажаться / промахнуться",    "Metí la pata con esa respuesta.",    "B1"),
        ph("Tener mano izquierda",      "Уметь обходиться с людьми",   "Necesitas más mano izquierda.",      "B2"),
        ph("Estar al loro",             "Быть в курсе / быть настороже","Está siempre al loro.",             "B2"),
        ph("Hacer la vista gorda",      "Закрыть глаза на что-то",     "El jefe hizo la vista gorda.",       "B2"),
        ph("Quedarse sin palabras",     "Лишиться дара речи",          "Me quedé sin palabras.",             "B1"),
        ph("Perder los papeles",        "Потерять самообладание",       "Perdió los papeles en la reunión.",  "B2"),
        ph("Ir al grano",               "Переходить к делу",            "Ve al grano, por favor.",            "B1"),
        ph("Echar de menos",            "Скучать по...",                "Te echo mucho de menos.",            "B1"),
        ph("Sentar la cabeza",          "Образумиться / остепениться",  "Ya es hora de sentar la cabeza.",    "B2"),
        ph("Tomar el pelo",             "Разыгрывать / дурачить",       "Me estás tomando el pelo.",          "B1"),
        ph("Andarse con rodeos",        "Говорить обиняками",           "No te andes con rodeos.",            "B2"),
        ph("Coger el toro por los cuernos","Брать быка за рога",       "Hay que coger el toro por los cuernos.","B2"),
        ph("Dar la vuelta a la tortilla","Перевернуть всё с ног на голову","Dieron la vuelta a la tortilla.", "B2"),
        ph("Tener entre ceja y ceja",   "Иметь в голове / быть одержимым","Lo tiene entre ceja y ceja.",      "B2"),
    )

    // ── ИДИОМЫ C1 ─────────────────────────────────────────────────
    private val idiomas_b2c1 = listOf(
        ph("Nadar entre dos aguas",     "Сидеть на двух стульях",       "No puedes nadar entre dos aguas.",   "C1"),
        ph("Poner el dedo en la llaga", "Попасть в больное место",      "Pusiste el dedo en la llaga.",       "C1"),
        ph("Sacar los colores",         "Заставить покраснеть",         "Me sacó los colores delante de todos.","C1"),
        ph("Cortar por lo sano",        "Принять решительные меры",     "Hay que cortar por lo sano.",        "C1"),
        ph("Estar a la altura",         "Быть на высоте / соответствовать","No estuvo a la altura.",          "C1"),
        ph("Pagar el pato",             "Быть крайним / отдуваться",    "Siempre pago el pato yo.",           "B2"),
        ph("Rizar el rizo",             "Усложнять и без того сложное", "Ya rizamos el rizo.",               "C1"),
        ph("Hacer de tripas corazón",   "Превозмочь себя",              "Hizo de tripas corazón y habló.",    "C1"),
        ph("No dar palo al agua",       "Палец о палец не ударить",     "No da palo al agua ese chico.",      "B2"),
        ph("Tener los pies en la tierra","Стоять ногами на земле",      "Hay que tener los pies en la tierra.","C1"),
        ph("Estar con el agua al cuello","Быть по горло в проблемах",   "Estamos con el agua al cuello.",     "C1"),
        ph("No dar el brazo a torcer",  "Не уступать / стоять на своём","No dio el brazo a torcer.",          "C1"),
        ph("Llevarse el gato al agua",  "Добиться своего",              "Al final se llevó el gato al agua.", "C1"),
        ph("Estar en boca de todos",    "Быть у всех на устах",         "El escándalo está en boca de todos.","C1"),
        ph("Ver los toros desde la barrera","Быть сторонним наблюдателем","Siempre ve los toros desde la barrera.","C1"),
        ph("Tirar la toalla",           "Сдаться",                      "Tiró la toalla demasiado pronto.",   "B2"),
        ph("Ser pan comido",            "Это проще простого",           "Este examen es pan comido.",         "B1"),
        ph("Dar palos de ciego",        "Действовать наугад",           "Estamos dando palos de ciego.",      "C1"),
        ph("Armarse la gorda",          "Разгореться скандалу",         "Se armó la gorda en la reunión.",    "B2"),
        ph("Poner en jaque",            "Поставить в тупик / угрожать", "La crisis pone en jaque al sistema.","C1"),
    )

    // ── 18+ РАЗГОВОРНЫЕ ФРАЗЫ И СЛОВА ────────────────────────────
    // Категория adult_expresiones — помечается значком 18+ в интерфейсе
    private val adulto = listOf(
        adult("¡Joder!",                "Блин! / Чёрт! (груб.)",        "¡Joder, qué frío hace!"),
        adult("¡Hostia!",               "Ёлки-палки! / Вот это да! (груб.)","¡Hostia, no me lo esperaba!"),
        adult("¡Coño!",                 "Ёлки! / Чёрт! (груб.)",       "¡Coño, otra vez tarde!"),
        adult("¡Me cago en todo!",      "Чёрт возьми! (очень груб.)",   "¡Me cago en todo, perdí las llaves!"),
        adult("¡Qué cabrón!",           "Вот скотина! (груб.)",         "¡Qué cabrón, me mintió!"),
        adult("¡La madre que te parió!","Чёрт тебя дери! (очень груб.)","¡La madre que te parió!"),
        adult("¡Me importa una mierda!","Мне плевать! (груб.)",         "¡Me importa una mierda lo que dices!"),
        adult("Eres un/a gilipollas",   "Ты идиот/идиотка (груб.)",     "¡Eres un gilipollas!"),
        adult("¡Que te jodan!",         "Иди ты! / Пошёл ты! (груб.)", "¡Que te jodan!"),
        adult("¡Vete a la mierda!",     "Иди к чёрту! (груб.)",         "¡Vete a la mierda!"),
        adult("Estar hasta los cojones","Быть сытым по горло (груб.)",  "Estoy hasta los cojones de esperar."),
        adult("¡Ostras!",               "Блин! / Вот это да! (мягкое)", "¡Ostras, qué susto me diste!"),
        adult("¡Mecachis!",             "Ёлки-палки! (мягкое ругательство)","¡Mecachis, se me olvidó!"),
        adult("¡Leches!",               "Чёрт! (мягкое груб.)",         "¡Leches, otra vez lo mismo!"),
        adult("¡Anda ya!",              "Да ладно! / Не верю! (разг.)", "¡Anda ya! ¿En serio lo dices?"),
        adult("Ser un pesado",          "Быть занудой / надоедой",      "¡Qué pesado eres!"),
        adult("Dar asco",               "Вызывать отвращение",          "Eso me da asco."),
        adult("Ser un cabrón",          "Быть скотиной / козлом",       "Ese tipo es un cabrón."),
        adult("Tocarse los huevos",     "Бездельничать (груб.)",        "Se pasa el día tocándose los huevos."),
        adult("Tener huevos",           "Иметь смелость (груб.)",       "Hay que tener huevos para hacerlo."),
        adult("Ser un coñazo",          "Быть невыносимой скукой",      "Esta película es un coñazo."),
        adult("Mandar a la mierda",     "Послать куда подальше",        "Lo mandé a la mierda."),
        adult("¡Qué putada!",           "Вот облом! / Как некстати!",   "¡Qué putada, se canceló el vuelo!"),
        adult("Ser una putada",         "Быть настоящим облом",         "Es una putada trabajar los domingos."),
        adult("No me jodas",            "Не выдумывай / Ты серьёзно?!", "¿No me jodas? ¿Es verdad?"),
        adult("Liarse",                 "Заниматься сексом / путаться (разг.)","Se liaron en la fiesta."),
        adult("Enrollarse",             "Целоваться / флиртовать (разг.)","Se enrollaron toda la noche."),
        adult("Echar un polvo",         "Заниматься сексом (вульг.)",   ""),
        adult("Ir de putas",            "Идти к проституткам (вульг.)", ""),
        adult("El condón",              "презерватив",                  "Usa condón.",                         "B1"),
        adult("La regla",               "менструация",                  "Tiene la regla.",                     "B1"),
        adult("El embarazo",            "беременность",                 "El embarazo dura nueve meses.",       "B1"),
        adult("Quedarse embarazada",    "забеременеть",                 "Se quedó embarazada.",                "B1"),
        adult("El aborto",              "аборт",                        "El aborto es un tema polémico.",      "B2"),
        adult("La borrachera",          "пьянство / опьянение",         "Vaya borrachera lleva.",              "B1"),
        adult("Estar borracho",         "быть пьяным",                  "Está borracho, no le hagas caso.",    "B1"),
        adult("Ponerse ciego",          "напиться в хлам",              "Se puso ciego en la fiesta.",         "B2"),
        adult("El resacón",             "тяжёлое похмелье",             "Tiene un resacón brutal.",            "B2"),
        adult("La resaca",              "похмелье",                     "La resaca le duró todo el día.",      "B1"),
        adult("Ir de fiesta",           "идти тусоваться",              "Vamos de fiesta esta noche.",         "A2"),
        adult("Liarse a golpes",        "схватиться / подраться",       "Se liaron a golpes en el bar.",       "B2"),
        adult("El porro",               "косяк (марихуана) (разг.)",    "Se fumó un porro.",                   "B2"),
        adult("Las drogas blandas",     "лёгкие наркотики",             "Debate sobre las drogas blandas.",    "B2"),
        adult("Estar colocado",         "быть под кайфом",              "Está colocado, no conduzcas.",        "B2"),
        adult("El chantaje",            "шантаж",                       "Lo acusaron de chantaje.",            "B2"),
        adult("La soborno",             "взятка",                       "Le ofrecieron un soborno.",           "B2"),
        adult("Hacer la vista gorda ante la corrupción","закрывать глаза на коррупцию","Lo denunció.","C1"),
        adult("El pitorreo",            "издевательство / насмешка",    "Se rió a pitorreo de todos.",         "B2"),
        adult("Cachondo",               "смешной / возбуждённый (двусм.)","Es un tío muy cachondo.",           "B2"),
        adult("Salir de marcha",        "идти гулять / тусить",         "Esta noche salimos de marcha.",       "B1"),
        adult("Tener morro",            "иметь наглость",               "¡Qué morro tiene!",                  "B2"),
    )
}
