package com.spanishapp.data.db

import com.spanishapp.data.db.entity.WordEntity

/**
 * VocabExpansion2 — уровни B2/C1 (~1100 слов)
 * Темы: философия, история, литература, академический язык,
 *       профессии, туризм, семья/отношения, еда (детальнее),
 *       музыка, числа/мат, природа/животные, путешествия
 */
object VocabExpansion2 {

    private fun w(es: String, ru: String, ex: String = "", level: String = "B2", cat: String = "general", type: String = "noun") =
        WordEntity(spanish = es, russian = ru, example = ex, level = level, category = cat, wordType = type)
    private fun v(es: String, ru: String, ex: String = "", level: String = "B2", cat: String = "general") = w(es, ru, ex, level, cat, "verb")
    private fun adj(es: String, ru: String, ex: String = "", level: String = "B2", cat: String = "general") = w(es, ru, ex, level, cat, "adjective")
    private fun adv(es: String, ru: String, ex: String = "", level: String = "B2", cat: String = "general") = w(es, ru, ex, level, cat, "adverb")
    private fun ph(es: String, ru: String, ex: String = "", level: String = "B2") = w(es, ru, ex, level, "expresiones", "phrase")

    val entries: List<WordEntity> get() = filosofia + historia + literatura + academico +
        profesiones + turismo + familia + musica + naturaleza + animales +
        viajes + ciencias_sociales + verbosB2 + adjetivosB2 + expresionesB2 + bodega

    // ── FILOSOFÍA / PENSAMIENTO ───────────────────────────────────
    private val filosofia = listOf(
        w("la filosofía",        "философия",            "La filosofía busca la verdad.",         "B2", "filosofia"),
        w("la ética",            "этика",                "La ética guía nuestras decisiones.",    "B2", "filosofia"),
        w("la moral",            "мораль",               "La moral varía según la cultura.",      "B2", "filosofia"),
        w("la conciencia",       "сознание/совесть",     "Su conciencia le impide mentir.",       "B2", "filosofia"),
        w("la existencia",       "существование",        "La existencia humana es compleja.",     "B2", "filosofia"),
        w("el libre albedrío",   "свобода воли",         "El libre albedrío es debatido.",        "C1", "filosofia"),
        w("el relativismo",      "релятивизм",           "El relativismo rechaza la verdad absoluta.","C1","filosofia"),
        w("el utilitarismo",     "утилитаризм",          "El utilitarismo busca el bien mayor.",  "C1", "filosofia"),
        w("la metafísica",       "метафизика",           "La metafísica estudia el ser.",         "C1", "filosofia"),
        w("la epistemología",    "эпистемология",        "La epistemología estudia el conocimiento.","C1","filosofia"),
        w("el dilema moral",     "нравственная дилемма", "Afronta un dilema moral difícil.",      "B2", "filosofia"),
        w("la racionalidad",     "рациональность",       "La racionalidad nos distingue.",        "B2", "filosofia"),
        w("el pensamiento crítico","критическое мышление","Desarrolla el pensamiento crítico.",  "B2", "filosofia"),
        w("la paradoja",         "парадокс",             "Es una paradoja interesante.",          "B2", "filosofia"),
        w("la utopía",           "утопия",               "Su proyecto es una utopía.",            "B2", "filosofia"),
        adj("abstracto",         "абстрактный",          "El arte abstracto es subjetivo.",       "B2", "filosofia"),
        adj("subjetivo",         "субъективный",         "La belleza es subjetiva.",              "B2", "filosofia"),
        adj("objetivo",          "объективный",          "Busca un análisis objetivo.",           "B2", "filosofia"),
        adj("racional",          "рациональный",         "Toma decisiones racionales.",           "B2", "filosofia"),
        adj("irracional",        "иррациональный",       "El miedo puede ser irracional.",        "B2", "filosofia")
    )

    // ── HISTORIA ─────────────────────────────────────────────────
    private val historia = listOf(
        w("la civilización",     "цивилизация",          "La civilización maya es fascinante.",   "B2", "historia"),
        w("el imperio",          "империя",              "El Imperio Romano duró siglos.",        "B2", "historia"),
        w("la conquista",        "завоевание",           "La conquista de América fue brutal.",   "B2", "historia"),
        w("la colonización",     "колонизация",          "La colonización cambió el mundo.",      "B2", "historia"),
        w("la revolución",       "революция",            "La Revolución Francesa fue crucial.",   "B2", "historia"),
        w("la guerra civil",     "гражданская война",    "La guerra civil española duró tres años.","B2","historia"),
        w("la dictadura",        "диктатура",            "Vivieron bajo la dictadura de Franco.", "B2", "historia"),
        w("la transición",       "переход (к демократии)","La transición española fue pacífica.", "B2", "historia"),
        w("el siglo",            "век/столетие",         "El siglo XXI trae nuevos retos.",       "B1", "historia"),
        w("la era",              "эра/эпоха",            "Entramos en la era digital.",           "B2", "historia"),
        w("el arqueólogo",       "археолог",             "El arqueólogo descubrió ruinas.",       "B1", "historia"),
        w("el yacimiento",       "раскопки/месторождение","El yacimiento tiene miles de años.",   "B2", "historia"),
        w("la monarquía",        "монархия",             "España es una monarquía constitucional.","B2","historia"),
        w("la república",        "республика",           "Proclamaron la república.",             "B2", "historia"),
        w("el feudalismo",       "феодализм",            "El feudalismo dominó la Edad Media.",   "B2", "historia"),
        w("el renacimiento",     "Возрождение/Ренессанс","El Renacimiento nació en Italia.",      "B2", "historia"),
        w("la ilustración",      "Просвещение",          "La Ilustración valoró la razón.",       "B2", "historia"),
        w("la primera guerra mundial","Первая мировая война","La Gran Guerra mató millones.",     "B2", "historia"),
        w("el Holocausto",       "Холокост",             "El Holocausto fue un genocidio.",       "B2", "historia"),
        w("la posguerra",        "послевоенное время",   "La posguerra fue muy difícil.",         "B2", "historia"),
        w("el tratado",          "договор/соглашение",   "Firmaron un tratado de paz.",           "B2", "historia"),
        w("la alianza",          "альянс/союз",          "Formaron una alianza militar.",         "B2", "historia"),
        w("el armisticio",       "перемирие",            "El armisticio puso fin a la guerra.",   "B2", "historia"),
        v("conquistar",          "завоёвывать",          "Conquistaron nuevos territorios.",      "B2", "historia"),
        v("colonizar",           "колонизировать",       "Colonizaron América en el siglo XVI.",  "B2", "historia")
    )

    // ── LITERATURA / ESCRITURA ────────────────────────────────────
    private val literatura = listOf(
        w("el protagonista",     "главный герой",        "El protagonista sufre mucho.",          "B1", "literatura"),
        w("el antagonista",      "антагонист/злодей",    "El antagonista es complejo.",           "B2", "literatura"),
        w("el narrador",         "рассказчик",           "El narrador es en primera persona.",    "B2", "literatura"),
        w("el argumento",        "сюжет",                "El argumento de la novela es sencillo.","B2","literatura"),
        w("el desenlace",        "развязка",             "El desenlace sorprendió a todos.",      "B2", "literatura"),
        w("el clímax",           "кульминация",          "El clímax llega en el tercer acto.",    "B2", "literatura"),
        w("el flashback",        "флэшбэк",              "El flashback revela su pasado.",        "B2", "literatura"),
        w("la metáfora",         "метафора",             "Usa la metáfora de la luz.",            "B2", "literatura"),
        w("la ironía",           "ирония",               "El autor usa la ironía para criticar.", "B2", "literatura"),
        w("el símbolo",          "символ",               "La paloma es símbolo de paz.",          "B2", "literatura"),
        w("el género literario", "литературный жанр",    "El thriller es su género favorito.",    "B2", "literatura"),
        w("la prosa",            "проза",                "Escribe en prosa clara.",               "B2", "literatura"),
        w("el verso",            "стихи/стих",           "El poema tiene diez versos.",           "B2", "literatura"),
        w("la rima",             "рифма",                "La rima es perfecta.",                  "B1", "literatura"),
        w("la novela histórica", "исторический роман",   "La novela histórica mezcla realidad.",  "B2", "literatura"),
        w("la ciencia ficción",  "научная фантастика",   "La ciencia ficción imagina el futuro.", "B1", "literatura"),
        w("el realismo mágico",  "магический реализм",   "García Márquez creó el realismo mágico.","B2","literatura"),
        w("el clásico",          "классика",             "Don Quijote es un clásico.",            "B1", "literatura"),
        w("la editorial",        "издательство",         "La editorial publicó la novela.",       "B2", "literatura"),
        w("el manuscrito",       "рукопись",             "El manuscrito tiene 500 años.",         "B2", "literatura"),
        adj("literario",         "литературный",         "Tiene gran talento literario.",         "B2", "literatura"),
        adj("narrativo",         "повествовательный",    "Su estilo narrativo es único.",         "B2", "literatura"),
        v("narrar",              "повествовать/рассказывать","Narra la historia con detalle.",    "B2", "literatura"),
        v("redactar",            "составлять/писать",    "Redacta el informe en dos horas.",      "B2", "literatura"),
        w("el cuento",           "рассказ/сказка",       "El cuento tiene moraleja.",             "B1", "literatura")
    )

    // ── VOCABULARIO ACADÉMICO ──────────────────────────────────────
    private val academico = listOf(
        w("la metodología",      "методология",          "La metodología del estudio es clara.",  "C1", "academico"),
        w("el marco teórico",    "теоретическая база",   "Presenta el marco teórico.",            "C1", "academico"),
        w("la bibliografía",     "библиография",         "La bibliografía tiene 50 fuentes.",     "B2", "academico"),
        w("la fuente primaria",  "первоисточник",        "Cita fuentes primarias del siglo XIX.", "C1", "academico"),
        w("la tesis doctoral",   "докторская диссертация","Su tesis doctoral tardó cinco años.",  "B2", "academico"),
        w("el máster",           "магистратура",         "Estudia un máster en economía.",        "B1", "academico"),
        w("la beca",             "стипендия/грант",      "Ganó una beca Erasmus.",                "B1", "academico"),
        w("el trabajo de fin de grado","дипломная работа","Defiende el trabajo de fin de grado.", "B2", "academico"),
        w("el plagio",           "плагиат",              "El plagio está prohibido.",             "B2", "academico"),
        w("la conferencia",      "конференция",          "Asistió a una conferencia internacional.","B1","academico"),
        w("el congreso",         "съезд/конгресс",       "Presenta en el congreso anual.",        "B2", "academico"),
        w("la ponencia",         "доклад",               "Su ponencia fue muy aplaudida.",        "C1", "academico"),
        w("el abstract",         "аннотация",            "El abstract resume el estudio.",        "C1", "academico"),
        w("la variable",         "переменная",           "Controlan las variables del experimento.","B2","academico"),
        w("la muestra",          "выборка",              "La muestra incluye 200 personas.",      "C1", "academico"),
        w("el resultado",        "результат",            "Los resultados son concluyentes.",      "B1", "academico"),
        w("la conclusión",       "вывод/заключение",     "La conclusión resume el estudio.",      "B1", "academico"),
        v("citar",               "цитировать",           "Cita a varios autores.",                "B2", "academico"),
        v("argumentar",          "аргументировать",      "Argumenta su posición con datos.",      "B2", "academico"),
        v("demostrar",           "доказывать",           "Demuestra la hipótesis.",               "B2", "academico"),
        v("refutar",             "опровергать",          "Refuta los argumentos contrarios.",     "C1", "academico"),
        v("sintetizar",          "синтезировать",        "Sintetiza las ideas principales.",      "C1", "academico"),
        v("contrastar",          "сопоставлять",         "Contrasta dos teorías.",                "B2", "academico"),
        v("exponer",             "излагать/представлять","Expone sus ideas con claridad.",        "B2", "academico"),
        w("la revisión por pares","рецензирование",      "El artículo pasa revisión por pares.",  "C1", "academico")
    )

    // ── PROFESIONES ───────────────────────────────────────────────
    private val profesiones = listOf(
        w("el ingeniero",        "инженер",              "El ingeniero diseña el puente.",        "B1", "trabajo"),
        w("el arquitecto",       "архитектор",           "El arquitecto ganó el concurso.",       "B1", "trabajo"),
        w("el médico de cabecera","терапевт",            "Mi médico de cabecera es amable.",      "B1", "salud"),
        w("el cirujano",         "хирург",               "El cirujano opera mañana.",             "B2", "salud"),
        w("el enfermero",        "медбрат/медсестра",    "El enfermero cuida al paciente.",       "B1", "salud"),
        w("el farmacéutico",     "фармацевт",            "El farmacéutico da el medicamento.",    "B1", "salud"),
        w("el veterinario",      "ветеринар",            "El veterinario vacuna al perro.",       "B1", "trabajo"),
        w("el informático",      "IT-специалист",        "El informático repara el servidor.",    "B1", "tecnologia"),
        w("el diseñador gráfico","графический дизайнер", "El diseñador gráfico crea logos.",     "B1", "trabajo"),
        w("el contable",         "бухгалтер",            "El contable lleva las cuentas.",        "B1", "economia"),
        w("el economista",       "экономист",            "El economista analiza la inflación.",   "B2", "economia"),
        w("el sociólogo",        "социолог",             "El sociólogo estudia la sociedad.",     "B2", "trabajo"),
        w("el filósofo",         "философ",              "El filósofo cuestiona todo.",           "B2", "filosofia"),
        w("el lingüista",        "лингвист",             "El lingüista estudia idiomas.",         "B2", "trabajo"),
        w("el historiador",      "историк",              "El historiador investiga el pasado.",   "B2", "historia"),
        w("el político",         "политик",              "El político hace campaña.",             "B1", "politica"),
        w("el diplomático",      "дипломат",             "El diplomático negocia el tratado.",    "B2", "politica"),
        w("el chef",             "шеф-повар",            "El chef creó un menú innovador.",       "B1", "cocina"),
        w("el sommelier",        "сомелье",              "El sommelier recomienda el vino.",      "B2", "cocina"),
        w("el guía turístico",   "туристический гид",    "El guía explica la historia.",          "B1", "viajes"),
        w("el traductor",        "переводчик",           "El traductor domina cinco idiomas.",    "B1", "trabajo"),
        w("el intérprete",       "устный переводчик",    "El intérprete traduce en directo.",     "B2", "trabajo"),
        w("el redactor",         "редактор/автор",       "El redactor escribe los artículos.",    "B2", "medios"),
        w("el community manager","СММ-менеджер",         "El community manager gestiona redes.",  "B2", "tecnologia"),
        w("el coach",            "коуч",                 "El coach le ayuda a mejorar.",         "B2", "trabajo"),
        w("el terapeuta ocupacional","трудотерапевт",    "El terapeuta ocupacional ayuda a volver.","B2","salud"),
        w("el notario",          "нотариус",             "El notario firma el documento.",        "B2", "derecho"),
        w("el procurador",       "прокурор/поверенный",  "El procurador representa al cliente.",  "C1", "derecho"),
        w("el funcionario",      "государственный чиновник","El funcionario gestiona el trámite.","B1","politica"),
        w("el inspector",        "инспектор",            "El inspector revisa la empresa.",       "B2", "trabajo")
    )

    // ── TURISMO / VIAJES ──────────────────────────────────────────
    private val turismo = listOf(
        w("el vuelo de conexión","стыковочный рейс",     "Tengo un vuelo de conexión en Madrid.", "B1", "viajes"),
        w("la escala",           "пересадка",            "El vuelo tiene escala en Lisboa.",      "B1", "viajes"),
        w("el equipaje de mano", "ручная кладь",         "Solo llevo equipaje de mano.",          "B1", "viajes"),
        w("la maleta",           "чемодан",              "La maleta pesa 23 kilos.",               "A2", "viajes"),
        w("el check-in",         "регистрация (в отеле)","El check-in es a las 14h.",             "B1", "viajes"),
        w("el check-out",        "выезд из отеля",       "El check-out es antes de las 12h.",     "B1", "viajes"),
        w("la tarjeta de embarque","посадочный талон",   "Imprime la tarjeta de embarque.",       "B1", "viajes"),
        w("el pasaporte",        "паспорт",              "Necesita pasaporte vigente.",            "A2", "viajes"),
        w("el visado",           "виза",                 "Solicita el visado con antelación.",    "B1", "viajes"),
        w("la aduana",           "таможня",              "Pasa por la aduana sin problemas.",     "B1", "viajes"),
        w("el control de seguridad","досмотр",           "El control de seguridad es estricto.",  "B1", "viajes"),
        w("el albergue",         "хостел",               "Se queda en un albergue barato.",       "B1", "viajes"),
        w("la pensión",          "пансион (мини-отель)", "La pensión está en el centro.",         "B1", "viajes"),
        w("el hotel de lujo",    "роскошный отель",      "Se alojan en un hotel de lujo.",        "B1", "viajes"),
        w("el resort",           "курорт/resort",        "El resort tiene spa y playa.",          "B1", "viajes"),
        w("el crucero",          "круиз",                "Hicieron un crucero por el Mediterráneo.","B1","viajes"),
        w("el itinerario",       "маршрут/программа",    "El itinerario incluye tres ciudades.",  "B1", "viajes"),
        w("el presupuesto de viaje","бюджет на путешествие","El presupuesto de viaje es ajustado.","B1","viajes"),
        w("el seguro de viaje",  "туристическая страховка","Contrata un seguro de viaje.",       "B1", "viajes"),
        w("el turismo sostenible","устойчивый туризм",   "Prefieren el turismo sostenible.",      "B2", "viajes"),
        w("el turismo rural",    "сельский туризм",      "El turismo rural crece en España.",     "B1", "viajes"),
        w("el mochilero",        "путешественник с рюкзаком","Es mochilero y recorre el mundo.",  "B1", "viajes"),
        w("la excursión",        "экскурсия",            "Hacen una excursión al volcán.",        "A2", "viajes"),
        v("hospedarse",          "останавливаться (в отеле)","Se hospedan en una casa rural.",   "B1", "viajes"),
        v("explorar",            "исследовать/изучать",  "Exploran el casco antiguo.",            "B1", "viajes"),
        v("recorrer",            "объездить/пройти",     "Recorren toda la costa en bici.",       "B1", "viajes"),
        v("facturar",            "сдавать багаж",        "Factura la maleta en el mostrador.",    "B1", "viajes"),
        w("la jet lag",          "смена часовых поясов", "Sufre jet lag tras el viaje.",          "B2", "viajes"),
        w("el turista",          "турист",               "Los turistas llenan la plaza.",         "A2", "viajes"),
        w("la temporada alta",   "высокий сезон",        "En temporada alta los precios suben.",  "B1", "viajes")
    )

    // ── FAMILIA / RELACIONES ──────────────────────────────────────
    private val familia = listOf(
        w("el cónyuge",          "супруг/супруга",       "Su cónyuge la apoya.",                  "B1", "familia"),
        w("la pareja de hecho",  "гражданский партнёр",  "Son pareja de hecho desde 2018.",       "B2", "familia"),
        w("el matrimonio",       "брак/супружество",     "El matrimonio requiere compromiso.",     "B1", "familia"),
        w("el divorcio",         "развод",               "El divorcio fue amistoso.",             "B1", "familia"),
        w("la custodia",         "опека",                "La custodia es compartida.",            "B2", "familia"),
        w("el adoptado",         "усыновлённый",         "El niño adoptado se integra bien.",     "B2", "familia"),
        w("la familia monoparental","неполная семья",    "Vivió en una familia monoparental.",    "B2", "familia"),
        w("el conflicto familiar","семейный конфликт",   "Resuelven el conflicto sin gritos.",    "B1", "familia"),
        w("la reconciliación",   "примирение",           "La reconciliación fue emotiva.",        "B2", "familia"),
        w("la brecha generacional","разрыв поколений",   "La brecha generacional es real.",       "B2", "familia"),
        w("el nieto",            "внук",                 "Los nietos visitan a los abuelos.",     "A2", "familia"),
        w("el bisabuelo",        "прадед",               "Su bisabuelo vivió 95 años.",           "B1", "familia"),
        w("el padrino",          "крёстный",             "El padrino fue a la boda.",             "B1", "familia"),
        w("la madrina",          "крёстная",             "La madrina le regaló un libro.",        "B1", "familia"),
        w("el suegro",           "свёкор/тесть",         "El suegro es muy amable.",              "B1", "familia"),
        w("la suegra",           "свекровь/тёща",        "La suegra cocina muy bien.",            "B1", "familia"),
        w("el cuñado",           "шурин/деверь",         "Mi cuñado es médico.",                  "B1", "familia"),
        w("el sobrino",          "племянник",            "El sobrino tiene dos años.",             "A2", "familia"),
        w("el gemelo",           "близнец",              "Son gemelos idénticos.",                "B1", "familia"),
        w("el hogar",            "домашний очаг/дом",    "El hogar es donde está el amor.",       "B1", "familia"),
        v("casarse",             "жениться/выходить замуж","Se casaron en la playa.",             "A2", "familia"),
        v("divorciarse",         "разводиться",          "Se divorciaron tras diez años.",        "B1", "familia"),
        v("reconciliarse",       "мириться",             "Se reconciliaron después del conflicto.","B2","familia"),
        v("adoptar",             "усыновлять",           "Adoptaron a un niño de tres años.",     "B2", "familia"),
        w("la familia extensa",  "расширенная семья",    "La familia extensa se reúne en Navidad.","B1","familia")
    )

    // ── MÚSICA ───────────────────────────────────────────────────
    private val musica = listOf(
        w("el compositor",       "композитор",           "El compositor escribió la sinfonía.",   "B1", "musica"),
        w("la orquesta",         "оркестр",              "La orquesta toca en el auditorio.",     "B1", "musica"),
        w("el director de orquesta","дирижёр",           "El director marca el ritmo.",           "B2", "musica"),
        w("la partitura",        "партитура",            "El músico lee la partitura.",           "B2", "musica"),
        w("el acorde",           "аккорд",               "El acorde final es perfecto.",          "B2", "musica"),
        w("la melodía",          "мелодия",              "La melodía es pegadiza.",               "B1", "musica"),
        w("el ritmo",            "ритм",                 "El ritmo de la salsa es enérgico.",     "B1", "musica"),
        w("el coro",             "хор/припев",           "El coro canta en la catedral.",         "B1", "musica"),
        w("la letra",            "текст песни",          "La letra de la canción es poética.",    "B1", "musica"),
        w("el estribillo",       "припев",               "El estribillo se repite tres veces.",   "B2", "musica"),
        w("la tonalidad",        "тональность",          "La tonalidad es menor.",                "C1", "musica"),
        w("el concierto",        "концерт",              "El concierto dura dos horas.",          "A2", "musica"),
        w("la gira",             "гастроли/турне",       "La gira recorre diez países.",          "B1", "musica"),
        w("el álbum",            "альбом",               "El álbum tuvo gran éxito.",             "B1", "musica"),
        w("el sencillo",         "сингл",                "El sencillo llegó al número uno.",      "B2", "musica"),
        w("el streaming",        "стриминг",             "Escucha música en streaming.",          "B1", "tecnologia"),
        w("la plataforma musical","музыкальная платформа","Spotify es una plataforma musical.",   "B1", "musica"),
        w("la guitarra flamenca","гитара фламенко",      "Toca la guitarra flamenca.",            "B1", "musica"),
        w("el bajo",             "бас-гитара",           "El bajo marca el ritmo.",               "B1", "musica"),
        w("la batería",          "ударные",              "Toca la batería en un grupo.",          "B1", "musica"),
        w("los auriculares",     "наушники",             "Escucha música con auriculares.",       "A2", "musica"),
        v("componer",            "сочинять",             "Compone canciones desde los 12 años.",  "B2", "musica"),
        v("ensayar",             "репетировать",         "El grupo ensaya cada semana.",          "B1", "musica"),
        v("afinar",              "настраивать (инструмент)","Afina la guitarra antes del concierto.","B2","musica"),
        w("el festival de música","музыкальный фестиваль","El Primavera Sound es un festival clave.","B1","musica")
    )

    // ── NATURALEZA ────────────────────────────────────────────────
    private val naturaleza = listOf(
        w("la cordillera",       "горная цепь",          "La cordillera de los Andes es larga.",  "B2", "naturaleza"),
        w("el glaciar",          "ледник",               "El glaciar se derrite por el calor.",   "B2", "naturaleza"),
        w("el volcán",           "вулкан",               "El Teide es el volcán más alto.",       "B1", "naturaleza"),
        w("la laguna",           "лагуна/озеро",         "La laguna es de agua cristalina.",      "B1", "naturaleza"),
        w("el arrecife",         "риф",                  "El arrecife de coral protege la costa.","B2","naturaleza"),
        w("el pantano",          "водохранилище/болото", "El pantano está lleno de agua.",        "B1", "naturaleza"),
        w("la marisma",          "болото/солончак",      "Las marismas tienen mucha biodiversidad.","B2","naturaleza"),
        w("la catarata",         "водопад",              "Las cataratas del Niágara impresionan.", "B1", "naturaleza"),
        w("el cañón",            "каньон",               "El Gran Cañón tiene millones de años.", "B1", "naturaleza"),
        w("la meseta",           "плато/плоскогорье",    "La meseta central es árida.",           "B2", "naturaleza"),
        w("el huracán",          "ураган",               "El huracán devastó la costa.",          "B1", "naturaleza"),
        w("el tornado",          "торнадо",              "El tornado destruyó varios pueblos.",   "B1", "naturaleza"),
        w("el tsunami",          "цунами",               "El tsunami afectó a miles de personas.","B2","naturaleza"),
        w("el terremoto",        "землетрясение",        "El terremoto fue de magnitud 6.",       "B1", "naturaleza"),
        w("la erupción",         "извержение",           "La erupción del volcán dura semanas.",  "B2", "naturaleza"),
        w("el desierto",         "пустыня",              "El Sahara es el mayor desierto.",       "B1", "naturaleza"),
        w("la selva",            "джунгли/тропический лес","La selva amazónica es pulmón del mundo.","B1","naturaleza"),
        w("la tundra",           "тундра",               "En la tundra no crecen árboles.",       "B2", "naturaleza"),
        w("la sabana",           "саванна",              "La sabana africana tiene leones.",      "B2", "naturaleza"),
        w("el manglar",          "мангровый лес",        "El manglar protege la costa.",          "C1", "naturaleza"),
        v("erupcionar",          "извергаться",          "El volcán erupcionó de noche.",         "B2", "naturaleza"),
        adj("árido",             "засушливый/аридный",   "El clima árido dificulta la agricultura.","B2","naturaleza"),
        adj("fértil",            "плодородный",          "La tierra es muy fértil.",              "B1", "naturaleza"),
        adj("escarpado",         "крутой/обрывистый",    "El terreno es muy escarpado.",          "B2", "naturaleza"),
        w("el hábitat",          "среда обитания",       "El hábitat del oso panda es reducido.", "B2", "naturaleza")
    )

    // ── ANIMALES ──────────────────────────────────────────────────
    private val animales = listOf(
        w("el lince ibérico",    "иберийская рысь",      "El lince ibérico está en peligro.",     "B2", "animales"),
        w("el flamenco",         "фламинго",             "Los flamencos son rosas.",              "B1", "animales"),
        w("el toro",             "бык",                  "El toro es símbolo de España.",         "A2", "animales"),
        w("la cigüeña",          "аист",                 "Las cigüeñas vuelven en primavera.",    "B1", "animales"),
        w("el águila",           "орёл",                 "El águila real es majestuosa.",         "B1", "animales"),
        w("la ballena",          "кит",                  "La ballena azul es el mayor animal.",   "B1", "animales"),
        w("el delfín",           "дельфин",              "Los delfines son muy inteligentes.",    "B1", "animales"),
        w("el tiburón",          "акула",                "El tiburón es un depredador.",          "B1", "animales"),
        w("el pulpo",            "осьминог",             "El pulpo cambia de color.",             "B1", "animales"),
        w("la medusa",           "медуза",               "La medusa puede picar.",               "B1", "animales"),
        w("el cóndor",           "кондор",               "El cóndor vuela muy alto.",            "B2", "animales"),
        w("la cobra",            "кобра",                "La cobra es muy venenosa.",            "B2", "animales"),
        w("el camaleón",         "хамелеон",             "El camaleón cambia de color.",         "B1", "animales"),
        w("la tortuga marina",   "морская черепаха",     "La tortuga marina pone huevos en la playa.","B2","animales"),
        w("el pingüino",         "пингвин",              "El pingüino vive en la Antártida.",    "B1", "animales"),
        w("el oso polar",        "белый медведь",        "El oso polar se adapta al frío.",      "B1", "animales"),
        w("el lobo",             "волк",                 "El lobo vive en manada.",              "B1", "animales"),
        w("el jabalí",           "кабан",                "El jabalí vive en el bosque.",         "B1", "animales"),
        w("el venado",           "олень",                "El venado tiene cuernos grandes.",     "B1", "animales"),
        w("la lechuza",          "сова",                 "La lechuza caza de noche.",            "B1", "animales"),
        w("el murciélago",       "летучая мышь",         "El murciélago duerme boca abajo.",     "B1", "animales"),
        w("el escorpión",        "скорпион",             "El escorpión vive en el desierto.",    "B2", "animales"),
        adj("depredador",        "хищный",               "El lobo es un animal depredador.",     "B2", "animales"),
        adj("nocturno",          "ночной",               "El búho es un animal nocturno.",       "B1", "animales"),
        w("la manada",           "стая/стадо",           "El lobo vive en manada.",              "B2", "animales")
    )

    // ── VIAJES / TRANSPORTE (DETALLADO) ───────────────────────────
    private val viajes = listOf(
        w("el AVE",              "высокоскоростной поезд","El AVE conecta Madrid y Barcelona.",    "B1", "transporte"),
        w("el tren de alta velocidad","скоростной поезд","El tren de alta velocidad es rápido.", "B1", "transporte"),
        w("el ferry",            "паром",                "El ferry cruza el estrecho.",            "A2", "transporte"),
        w("el tranvía",          "трамвай",              "El tranvía recorre el centro.",          "A2", "transporte"),
        w("el funicular",        "фуникулёр",            "El funicular sube la montaña.",          "B1", "transporte"),
        w("el teleférico",       "канатная дорога",       "El teleférico tiene vistas increíbles.", "B1", "transporte"),
        w("el carril bici",      "велодорожка",           "La ciudad tiene muchos carriles bici.", "B1", "transporte"),
        w("el patinete eléctrico","электросамокат",       "Alquila un patinete eléctrico.",         "B1", "transporte"),
        w("el coche compartido", "каршеринг",             "Usan coche compartido para ahorrar.",    "B2", "transporte"),
        w("el parking",          "парковка",              "El parking está lleno.",                 "A2", "transporte"),
        w("la gasolinera",       "заправка",              "Para en la gasolinera.",                 "A2", "transporte"),
        w("el peaje",            "платная дорога/шлагбаум","El peaje de la autopista cuesta 5€.", "B1", "transporte"),
        w("el atasco",           "пробка",                "Hay atasco en la entrada a Madrid.",     "B1", "transporte"),
        w("la autopista",        "автомагистраль",        "La autopista está libre.",               "A2", "transporte"),
        w("la carretera comarcal","региональная дорога",  "La carretera comarcal es estrecha.",     "B2", "transporte"),
        w("el cinturón de seguridad","ремень безопасности","Abróchate el cinturón de seguridad.",  "A2", "transporte"),
        w("el GPS",              "навигатор/GPS",         "El GPS da instrucciones.",              "A2", "tecnologia"),
        w("el vuelo chárter",    "чартерный рейс",       "El vuelo chárter es más barato.",       "B2", "viajes"),
        w("la aerolínea",        "авиакомпания",         "La aerolínea canceló el vuelo.",         "B1", "viajes"),
        w("el retraso",          "задержка",             "El tren tiene retraso.",                 "B1", "transporte"),
        v("conducir",            "водить/управлять (авто)","Conduce con precaución.",              "A2", "transporte"),
        v("aparcar",             "парковаться",          "Aparca el coche en el garaje.",          "B1", "transporte"),
        v("adelantar",           "обгонять",             "No adelantes en curva.",                 "B1", "transporte"),
        v("circular",            "двигаться/ехать",      "Circula por la izquierda en UK.",        "B2", "transporte"),
        w("el control de alcoholemia","проверка алкоголя","El control de alcoholemia es estricto.","B1","transporte")
    )

    // ── CIENCIAS SOCIALES ─────────────────────────────────────────
    private val ciencias_sociales = listOf(
        w("la sociología",       "социология",           "La sociología estudia la sociedad.",    "B2", "academico"),
        w("la antropología",     "антропология",         "La antropología analiza culturas.",     "B2", "academico"),
        w("la psicología social","социальная психология", "La psicología social estudia grupos.", "B2", "psicologia"),
        w("la globalización",    "глобализация",         "La globalización conecta economías.",   "B2", "economia"),
        w("la desigualdad",      "неравенство",          "La desigualdad social crece.",          "B2", "politica"),
        w("la pobreza",          "бедность",             "La pobreza afecta a millones.",         "B1", "politica"),
        w("la exclusión social", "социальная изоляция",  "Lucha contra la exclusión social.",     "B2", "politica"),
        w("la integración",      "интеграция",           "La integración de inmigrantes es clave.","B2","politica"),
        w("el multiculturalismo","мультикультурализм",    "El multiculturalismo enriquece.",       "B2", "cultura"),
        w("el feminismo",        "феминизм",             "El feminismo busca la igualdad.",       "B2", "politica"),
        w("la igualdad de género","гендерное равенство",  "La igualdad de género avanza.",        "B2", "politica"),
        w("la brecha salarial",  "разрыв в зарплатах",   "La brecha salarial es injusta.",        "B2", "economia"),
        w("los estereotipos",    "стереотипы",           "Los estereotipos limitan a las personas.","B2","cultura"),
        w("el prejuicio",        "предубеждение",        "El prejuicio bloquea la razón.",        "B2", "cultura"),
        w("la discriminación",   "дискриминация",        "La discriminación está prohibida.",     "B1", "derecho"),
        w("el racismo",          "расизм",               "El racismo es un problema global.",     "B1", "politica"),
        w("la xenofobia",        "ксенофобия",           "La xenofobia genera conflictos.",       "B2", "politica"),
        w("la solidaridad",      "солидарность",         "La solidaridad une a las personas.",    "B1", "cultura"),
        w("el voluntariado",     "волонтёрство",         "Hace voluntariado los fines de semana.","B1","cultura"),
        w("la ONG",              "НКО/НГО",              "La ONG ayuda en zonas de conflicto.",   "B1", "politica")
    )

    // ── VERBOS B2 ────────────────────────────────────────────────
    private val verbosB2 = listOf(
        v("abogar",              "отстаивать/выступать за","Aboga por los derechos del niño.",    "B2"),
        v("abstenerse",          "воздерживаться",       "Se abstiene de votar.",                 "B2"),
        v("acatar",              "подчиняться/выполнять","Acata la decisión judicial.",           "B2"),
        v("acoger",              "принимать/приютить",   "Acogen a familias refugiadas.",         "B2"),
        v("adquirir",            "приобретать",          "Adquiere habilidades nuevas.",          "B2"),
        v("afligir",             "огорчать/удручать",    "La noticia le aflige mucho.",           "C1"),
        v("agudizar",            "обострять",            "La crisis agudiza la pobreza.",         "C1"),
        v("aludir",              "ссылаться/намекать",   "Alude al problema sin nombrarlo.",      "C1"),
        v("amparar",             "защищать/укрывать",    "La ley ampara a los trabajadores.",     "C1"),
        v("anteponer",           "ставить на первое место","Antepone la familia al trabajo.",    "C1"),
        v("aportar",             "вносить",              "Aporta soluciones creativas.",          "B2"),
        v("cuestionar",          "ставить под вопрос",   "Cuestiona las normas establecidas.",    "B2"),
        v("descartar",           "исключать/отвергать",  "Descarta esa posibilidad.",             "B2"),
        v("desempeñar",          "выполнять/занимать (пост)","Desempeña un papel clave.",        "C1"),
        v("disuadir",            "отговаривать",         "Le disuade de tomar esa decisión.",     "C1"),
        v("erradicar",           "искоренять",           "Intentan erradicar la pobreza.",        "C1"),
        v("fortalecer",          "укреплять",            "La crisis fortaleció su amistad.",      "B2"),
        v("implicar",            "подразумевать/вовлекать","El trabajo implica viajar mucho.",    "B2"),
        v("impulsar",            "продвигать/стимулировать","Impulsa la digitalización.",        "B2"),
        v("matizar",             "уточнять/делать нюансы","Matiza su opinión tras el debate.",    "C1"),
        v("obviar",              "избегать/обходить",    "Obvia los detalles innecesarios.",      "C1"),
        v("plantear",            "ставить (вопрос)",     "Plantea una pregunta interesante.",     "B2"),
        v("prescindir",          "обходиться без",       "Prescinde del coche en la ciudad.",     "C1"),
        v("promover",            "продвигать/способствовать","Promueve la lectura entre jóvenes.","B2"),
        v("reclamar",            "требовать/претендовать","Reclama sus derechos laborales.",      "B2"),
        v("replantear",          "пересматривать",       "Replantea su estrategia.",              "C1"),
        v("resignarse",          "смиряться",            "Se resigna a la situación.",            "B2"),
        v("reivindicar",         "отстаивать/требовать", "Reivindica sus derechos.",             "B2"),
        v("trascender",          "выходить за рамки",    "El caso trasciende lo local.",          "C1"),
        v("vulnerar",            "нарушать/ущемлять",    "Vulnera los derechos del trabajador.",  "C1")
    )

    // ── ADJETIVOS B2 ──────────────────────────────────────────────
    private val adjetivosB2 = listOf(
        adj("arduo",             "тяжёлый/трудный",      "Es un trabajo arduo.",                  "B2"),
        adj("audaz",             "смелый/дерзкий",       "Es una decisión audaz.",                "B2"),
        adj("autónomo",          "автономный/независимый","Trabaja de manera autónoma.",          "B2"),
        adj("cauteloso",         "осторожный",           "Es cauteloso antes de actuar.",         "B2"),
        adj("coherente",         "последовательный",     "Su argumento es coherente.",            "B2"),
        adj("comprensible",      "понятный",             "La explicación es comprensible.",       "B2"),
        adj("conciso",           "лаконичный",           "El informe es conciso y claro.",        "B2"),
        adj("contundente",       "убедительный/весомый", "Un argumento contundente.",             "B2"),
        adj("delicado",          "деликатный/хрупкий",   "Es un asunto muy delicado.",            "B1"),
        adj("desafiante",        "вызывающий/сложный",   "Es una tarea desafiante.",              "B2"),
        adj("escaso",            "скудный/редкий",       "Los recursos son escasos.",             "B2"),
        adj("estricto",          "строгий",              "El profesor es muy estricto.",          "B1"),
        adj("exhaustivo",        "исчерпывающий",        "Hace un análisis exhaustivo.",          "C1"),
        adj("fluido",            "плавный/беглый",       "Habla español de manera fluida.",       "B2"),
        adj("frívolo",           "легкомысленный",       "Es una persona frívola.",              "B2"),
        adj("implacable",        "беспощадный",          "Es implacable con sus críticos.",       "C1"),
        adj("íntegro",           "честный/порядочный",   "Es una persona íntegra.",              "B2"),
        adj("lúcido",            "ясный/проницательный", "Tiene una mente lúcida.",               "C1"),
        adj("minucioso",         "дотошный/тщательный",  "Es minucioso en su trabajo.",           "B2"),
        adj("perspicaz",         "проницательный",       "Es un analista muy perspicaz.",         "C1"),
        adj("polifacético",      "многогранный",         "Es un artista polifacético.",           "C1"),
        adj("pragmático",        "прагматичный",         "Tiene un enfoque pragmático.",          "B2"),
        adj("prudente",          "осторожный/благоразумный","Es prudente en sus decisiones.",     "B1"),
        adj("radical",           "радикальный",          "Sus ideas son radicales.",              "B2"),
        adj("riguroso",          "строгий/точный",       "El método es riguroso.",               "B2")
    )

    // ── EXPRESIONES AVANZADAS ─────────────────────────────────────
    private val expresionesB2 = listOf(
        ph("a raíz de",          "в результате",         "A raíz del accidente, cambió.",         "B2"),
        ph("a sabiendas",        "сознательно/зная",     "Lo hizo a sabiendas del riesgo.",       "C1"),
        ph("a tenor de",         "судя по/в соответствии","A tenor de los datos, hay que actuar.","C1"),
        ph("con miras a",        "с целью",              "Con miras a mejorar la situación.",     "B2"),
        ph("con todo",           "тем не менее",         "Con todo, siguió adelante.",            "B2"),
        ph("cuesta arriba",      "с трудом/в гору",      "La recuperación es cuesta arriba.",     "B2"),
        ph("dar pie a",          "давать повод",         "Su actitud dio pie a rumores.",         "C1"),
        ph("de antemano",        "заблаговременно",      "Lo sabía de antemano.",                 "B2"),
        ph("de cara a",          "с учётом/для",         "De cara al futuro, hay oportunidades.", "B2"),
        ph("en aras de",         "во имя/ради",          "En aras de la paz, negocia.",           "C1"),
        ph("en lo que respecta a","что касается",        "En lo que respecta a educación...",     "B2"),
        ph("en pos de",          "в поисках/вслед за",   "Viajó en pos de un sueño.",            "C1"),
        ph("no obstante",        "тем не менее",         "No obstante, la situación mejora.",     "B2"),
        ph("pese a ello",        "несмотря на это",      "Pese a ello, sigue adelante.",          "B2"),
        ph("por ende",           "следовательно",        "Por ende, la medida es necesaria.",     "C1"),
        ph("sin ir más lejos",   "к примеру/не ходя далеко","Sin ir más lejos, ayer sucedió.",   "B2"),
        ph("bajo ningún concepto","ни при каких обстоятельствах","Bajo ningún concepto acepto eso.","B2"),
        ph("a grandes rasgos",   "в общих чертах",       "A grandes rasgos, el plan funciona.",   "B2"),
        ph("de buenas a primeras","вдруг/неожиданно",    "De buenas a primeras, cambió de idea.", "C1"),
        ph("ni mucho menos",     "отнюдь нет",           "No es fácil, ni mucho menos.",          "B2")
    )

    // ── BODEGA / VINOS (CULTURA ESPAÑOLA) ─────────────────────────
    private val bodega = listOf(
        w("la uva",              "виноград",             "La uva riojana es excelente.",          "A2", "cocina"),
        w("la vendimia",         "сбор винограда",       "La vendimia es en septiembre.",         "B1", "cultura"),
        w("la bodega",           "винный погреб/винодельня","La bodega guarda vinos añejos.",     "B1", "cocina"),
        w("el vino tinto",       "красное вино",         "El vino tinto de Rioja es famoso.",     "A2", "cocina"),
        w("el vino blanco",      "белое вино",           "El vino blanco va bien con el pescado.","A2","cocina"),
        w("el vino rosado",      "розовое вино",         "El vino rosado es refrescante.",        "A2", "cocina"),
        w("la denominación de origen","наименование места происхождения","La DO Rioja es prestigiosa.","B2","cocina"),
        w("el maridaje",         "сочетание вина с едой","El maridaje perfecto es vital.",        "B2", "cocina"),
        w("la cata de vinos",    "дегустация вин",       "Asiste a una cata de vinos.",           "B2", "cultura"),
        w("el sommelier",        "сомелье",              "El sommelier recomienda el Ribera.",     "B2", "cocina"),
        w("la sangría",          "сангрия",              "La sangría es la bebida de verano.",    "A2", "cocina"),
        w("la sidra",            "сидр",                 "La sidra asturiana es espumosa.",       "B1", "cocina"),
        w("el cava",             "кава (игристое вино)", "El cava se bebe en Nochevieja.",        "B1", "cocina"),
        w("el jerez",            "херес",                "El jerez es típico de Andalucía.",      "B1", "cocina"),
        w("la cerveza artesanal","крафтовое пиво",       "Las cervezas artesanales son populares.","B1","cocina")
    )
}
