package com.spanishapp.ui.flashcards

/**
 * One themed pack of ~15-20 Spanish words. Sets replace the previous
 * "74 categories per level" structure with curated, evenly-sized chunks
 * a learner can actually finish in 5-10 minutes.
 *
 * Word strings here MUST match the `spanish` column in the DB (with article
 * for nouns, e.g. "el gato"). At runtime the screen resolves them via WordDao.
 *
 * Sets unlock progressively in [order]: set N+1 opens when set N hits ≥70%
 * mastered words.
 *
 * @param id          stable identifier like "a1_set_01_greetings".
 * @param level       CEFR level — "A1" / "A2" / "B1" / "B2".
 * @param order       1-based position in the level's chain.
 * @param title       human-friendly Russian title shown on the tile.
 * @param emoji       single visual hook used as the tile icon.
 * @param wordsSpanish exact Spanish strings to look up in the word table.
 */
data class FlashcardSet(
    val id: String,
    val level: String,
    val order: Int,
    val title: String,
    val emoji: String,
    val wordsSpanish: List<String>
)

object FlashcardSetData {

    /** Threshold for "set complete enough to unlock the next one". */
    const val UNLOCK_RATIO = 0.7f

    val all: List<FlashcardSet> = buildList {

        // ════════════════════════════════════════════════════════
        //  A1 — основы (20 starter sets, ~15-20 words each)
        // ════════════════════════════════════════════════════════

        add(FlashcardSet("a1_01_greetings", "A1", 1, "Приветствия и знакомство", "👋",
            listOf("hola", "buenos días", "buenas tardes", "buenas noches",
                   "adiós", "hasta luego", "hasta mañana", "por favor",
                   "gracias", "de nada", "perdón", "lo siento",
                   "sí", "no", "tal vez", "claro")))

        add(FlashcardSet("a1_02_pronouns", "A1", 2, "Личные местоимения", "👤",
            listOf("yo", "tú", "él", "ella", "nosotros", "nosotras",
                   "vosotros", "vosotras", "ellos", "ellas", "usted", "ustedes",
                   "mi", "tu", "su", "nuestro")))

        add(FlashcardSet("a1_03_family", "A1", 3, "Семья", "👨‍👩‍👧",
            listOf("la familia", "el padre", "la madre", "el hermano",
                   "la hermana", "el hijo", "la hija", "el abuelo",
                   "la abuela", "el tío", "la tía", "el primo",
                   "la prima", "el esposo", "la esposa", "el bebé")))

        add(FlashcardSet("a1_04_numbers_1_20", "A1", 4, "Числа 1-20", "🔢",
            listOf("uno", "dos", "tres", "cuatro", "cinco",
                   "seis", "siete", "ocho", "nueve", "diez",
                   "once", "doce", "trece", "catorce", "quince",
                   "dieciséis", "diecisiete", "dieciocho", "diecinueve", "veinte")))

        add(FlashcardSet("a1_05_colors", "A1", 5, "Цвета", "🎨",
            listOf("rojo", "azul", "verde", "amarillo", "negro",
                   "blanco", "gris", "rosa", "naranja", "morado",
                   "marrón", "el color")))

        add(FlashcardSet("a1_06_days_week", "A1", 6, "Дни недели", "📅",
            listOf("lunes", "martes", "miércoles", "jueves", "viernes",
                   "sábado", "domingo", "el día", "la semana", "hoy",
                   "mañana", "ayer", "el fin de semana")))

        add(FlashcardSet("a1_07_food_basic", "A1", 7, "Еда — основа", "🍞",
            listOf("la comida", "el pan", "el agua", "la leche", "el café",
                   "el té", "el huevo", "el queso", "la fruta", "la manzana",
                   "el plátano", "la carne", "el pescado", "el arroz", "la sopa",
                   "el desayuno", "la cena")))

        add(FlashcardSet("a1_08_drinks", "A1", 8, "Напитки", "🥤",
            listOf("el agua", "el café", "el té", "la leche", "el jugo",
                   "el zumo", "la cerveza", "el vino", "el refresco")))

        add(FlashcardSet("a1_09_house", "A1", 9, "Дом и комнаты", "🏠",
            listOf("la casa", "el piso", "la cocina", "el dormitorio",
                   "el baño", "el salón", "la sala", "la puerta",
                   "la ventana", "la mesa", "la silla", "la cama",
                   "el sofá", "la lámpara")))

        add(FlashcardSet("a1_10_clothes", "A1", 10, "Одежда", "👕",
            listOf("la camisa", "los pantalones", "la falda", "el vestido",
                   "los zapatos", "el sombrero", "la chaqueta", "el abrigo",
                   "los calcetines", "la ropa", "la corbata", "el cinturón")))

        add(FlashcardSet("a1_11_body", "A1", 11, "Тело", "🧍",
            listOf("la cabeza", "la cara", "el ojo", "la nariz", "la boca",
                   "la oreja", "el brazo", "la mano", "el dedo", "la pierna",
                   "el pie", "el corazón", "el pelo", "el diente")))

        add(FlashcardSet("a1_12_verbs_basic", "A1", 12, "Базовые глаголы", "⚡",
            listOf("ser", "estar", "tener", "hacer", "ir",
                   "venir", "ver", "decir", "dar", "saber",
                   "querer", "poder", "hablar", "comer", "vivir")))

        add(FlashcardSet("a1_13_animals", "A1", 13, "Животные", "🐾",
            listOf("el perro", "el gato", "el pájaro", "el pez", "el caballo",
                   "la vaca", "el cerdo", "el pollo", "el conejo", "el ratón",
                   "el oso", "el león", "el tigre", "el elefante")))

        add(FlashcardSet("a1_14_weather", "A1", 14, "Погода", "☀️",
            listOf("el sol", "la lluvia", "la nieve", "el viento", "el frío",
                   "el calor", "la nube", "el cielo", "la temperatura",
                   "el tiempo")))

        add(FlashcardSet("a1_15_city", "A1", 15, "Город", "🏙️",
            listOf("la ciudad", "la calle", "la plaza", "el parque",
                   "la tienda", "el mercado", "el restaurante", "el hotel",
                   "el banco", "el hospital", "la escuela", "el museo",
                   "la iglesia", "el aeropuerto")))

        add(FlashcardSet("a1_16_transport", "A1", 16, "Транспорт", "🚗",
            listOf("el coche", "el autobús", "el tren", "el metro",
                   "el avión", "el barco", "la bicicleta", "la moto",
                   "el taxi")))

        add(FlashcardSet("a1_17_time", "A1", 17, "Время и часы", "⏰",
            listOf("la hora", "el minuto", "el segundo", "la mañana",
                   "la tarde", "la noche", "ahora", "siempre",
                   "nunca", "temprano", "tarde", "pronto")))

        add(FlashcardSet("a1_18_school", "A1", 18, "Школа и учёба", "📚",
            listOf("la escuela", "la clase", "el libro", "el cuaderno",
                   "el lápiz", "el bolígrafo", "el estudiante", "el profesor",
                   "la profesora", "la lección", "el examen", "la pregunta",
                   "la respuesta")))

        add(FlashcardSet("a1_19_emotions", "A1", 19, "Эмоции", "😊",
            listOf("feliz", "triste", "contento", "enfadado", "cansado",
                   "aburrido", "nervioso", "tranquilo", "asustado",
                   "el amor", "la alegría", "el miedo")))

        add(FlashcardSet("a1_20_questions", "A1", 20, "Вопросительные слова", "❓",
            listOf("qué", "quién", "dónde", "cuándo", "cómo",
                   "por qué", "cuál", "cuánto", "cuántos")))

        // ════════════════════════════════════════════════════════
        //  A2 — расширение базы (20 sets)
        // ════════════════════════════════════════════════════════

        add(FlashcardSet("a2_01", "A2", 1, "Работа и офис", "💼",
            listOf("la oficina", "el jefe", "la jefa", "el empleado",
                   "el contrato", "el sueldo", "la reunión", "el horario",
                   "el cliente", "la empresa", "trabajar", "ganar",
                   "despedir", "contratar", "el proyecto", "el correo electrónico",
                   "el colega", "el currículum", "el ascenso")))

        add(FlashcardSet("a2_02", "A2", 2, "Профессии", "👨‍🔧",
            listOf("el médico", "el profesor", "el ingeniero", "el abogado",
                   "el cocinero", "el camarero", "el taxista", "el policía",
                   "el bombero", "el dentista", "el periodista", "el secretario",
                   "el mecánico", "el agricultor", "el peluquero", "el panadero",
                   "el actor", "el escritor")))

        add(FlashcardSet("a2_03", "A2", 3, "В ресторане", "🍽️",
            listOf("el restaurante", "el camarero", "el menú", "la cuenta",
                   "la propina", "la mesa", "el plato", "el tenedor",
                   "la cuchara", "el cuchillo", "el vaso", "la servilleta",
                   "la ensalada", "la sopa", "el postre", "reservar",
                   "pedir", "la bebida")))

        add(FlashcardSet("a2_04", "A2", 4, "Покупки и магазины", "🛍️",
            listOf("la tienda", "el mercado", "el supermercado", "el producto",
                   "la marca", "la oferta", "el descuento", "el escaparate",
                   "el probador", "el recibo", "la caja", "el cliente",
                   "comprar", "vender", "pagar", "el carrito",
                   "el repartidor", "el hipermercado")))

        add(FlashcardSet("a2_05", "A2", 5, "Деньги", "💶",
            listOf("el dinero", "el euro", "la moneda", "el billete",
                   "la tarjeta", "el banco", "la cuenta", "el cheque",
                   "el pago", "la factura", "el cambio", "caro",
                   "barato", "pagar", "gastar", "ahorrar",
                   "prestar", "pedir prestado", "el ingreso")))

        add(FlashcardSet("a2_06", "A2", 6, "Транспорт детально", "🚆",
            listOf("el billete", "el andén", "la estación", "el aeropuerto",
                   "el aparcamiento", "el semáforo", "el paso de cebra", "el accidente",
                   "el cinturón de seguridad", "el carné de conducir", "la gasolinera", "la autopista",
                   "el vuelo", "el ferri", "el equipaje", "el retraso",
                   "el motor", "el volante")))

        add(FlashcardSet("a2_07", "A2", 7, "Гостиница и путешествие", "🏨",
            listOf("el hotel", "la habitación", "la recepción", "la llave",
                   "el pasaporte", "el equipaje", "la maleta", "el viajero",
                   "el turista", "el destino", "el suvenir", "el camping",
                   "el spa", "el documento de identidad", "el cambio de moneda", "el embarque",
                   "el protector solar", "la atracción turística")))

        add(FlashcardSet("a2_08", "A2", 8, "Здоровье и врач", "🩺",
            listOf("el médico", "el hospital", "el dolor", "la receta",
                   "el antibiótico", "el jarabe", "el chequeo", "descansar",
                   "el botiquín", "el embarazo", "el entrenamiento", "el estilo de vida",
                   "débil", "adelgazar", "ducharse", "el dolor de espalda",
                   "el dolor de muelas", "el corte")))

        add(FlashcardSet("a2_09", "A2", 9, "Хобби и спорт", "🏋️",
            listOf("el deporte", "el fútbol", "el baloncesto", "el tenis",
                   "el boxeo", "el karate", "el maratón", "el atleta",
                   "el aficionado", "el ataque", "el pase", "el penalti",
                   "el pilates", "el senderismo", "la cancha de fútbol", "el béisbol",
                   "el hockey", "el snowboard")))

        add(FlashcardSet("a2_10", "A2", 10, "Музыка и кино", "🎬",
            listOf("la música", "la canción", "el concierto", "el escenario",
                   "el público", "la guitarra eléctrica", "la guitarra acústica", "el saxofón",
                   "la trompeta", "el tango", "el reggae", "el videoclip",
                   "el vinilo", "el género musical", "el instrumento musical", "el teatro",
                   "la entrada", "aplaudir")))

        add(FlashcardSet("a2_11", "A2", 11, "Технологии и устройства", "📱",
            listOf("el ordenador", "el móvil", "la pantalla", "el teclado",
                   "el ratón", "internet", "el wifi", "el archivo PDF",
                   "el GPS", "el antivirus", "el auricular", "conectar",
                   "borrar", "el botón de enviar", "el 5G", "el 4G",
                   "el archivo MP3", "el bug")))

        add(FlashcardSet("a2_12", "A2", 12, "Дом — мебель и комнаты", "🛋️",
            listOf("el apartamento", "el balcón", "el dormitorio", "el escritorio",
                   "el espejo grande", "el horno", "el lavavajillas", "la aspiradora",
                   "la bombilla", "el grifo", "el interruptor", "la cerradura",
                   "el frigorifico", "el secador de pelo", "la cama de matrimonio", "el cuarto",
                   "el pasillo", "decorar")))

        add(FlashcardSet("a2_13", "A2", 13, "Кулинария и приготовление", "👨‍🍳",
            listOf("el aceite de oliva", "el ingrediente", "el vinagre", "el congelador",
                   "la sartén", "la olla", "la cucharada", "la cucharadita",
                   "la nevera", "la tostadora", "la licuadora", "la rebanada",
                   "la rodaja", "freír", "hornear", "pelar",
                   "rallar", "revolver", "la paella")))

        add(FlashcardSet("a2_14", "A2", 14, "Эмоции продвинутые", "😢",
            listOf("el amor", "el cariño", "el deseo", "el enfado",
                   "el orgullo", "el humor", "la alegría", "la felicidad pura",
                   "la culpa", "la lástima", "la pereza", "la emoción",
                   "celoso", "asustado", "avergonzado", "desilusionado",
                   "emocionado", "enfadado", "alegre", "agradecido")))

        add(FlashcardSet("a2_15", "A2", 15, "Описания людей", "🙋",
            listOf("amable", "simpatico", "antipatico", "generoso",
                   "paciente", "curioso", "creativo", "sociable",
                   "honesto", "grosero", "calvo", "pelirrojo",
                   "activo", "cariñoso", "cobarde", "educado",
                   "maleducado", "mentiroso", "tacaño", "valiente")))

        add(FlashcardSet("a2_16", "A2", 16, "Описания вещей", "🔶",
            listOf("antiguo", "artificial", "artesanal", "automático",
                   "colorido", "delicioso", "dinámico", "elástico",
                   "emocionante", "esencial", "espacioso", "especial",
                   "estable", "compacto", "crujiente", "cremoso",
                   "desagradable", "diferente", "electrónico")))

        add(FlashcardSet("a2_17", "A2", 17, "Природа и пейзажи", "🌳",
            listOf("el cielo estrellado", "el clima", "el espacio", "el cometa",
                   "el girasol", "el clavel", "el manzano", "el cerezo",
                   "el limonero", "el naranjo", "el pino", "el pico",
                   "el rebaño", "el parque nacional", "el rayo de sol", "el barro",
                   "despejado", "congelado")))

        add(FlashcardSet("a2_18", "A2", 18, "Праздники и события", "🎉",
            listOf("la fiesta", "el regalo", "la sorpresa", "el cumpleaños",
                   "la boda", "el aniversario", "la Navidad", "celebrar",
                   "invitar", "felicitar", "el concierto", "la entrada",
                   "la discoteca", "Las Fallas", "San Fermín", "la atracción turística",
                   "disfrutar", "emocionarse")))

        add(FlashcardSet("a2_19", "A2", 19, "Образование детально", "🎓",
            listOf("la escuela", "la clase", "el examen", "el estudiante",
                   "el profesor", "la lección", "la pregunta", "la respuesta",
                   "el aula", "el cuestionario", "el ejercicio", "el examen final",
                   "el examen oral", "el certificado", "el conocimiento", "el bloc de notas",
                   "el calendario escolar", "aprobar", "corregir", "concentrarse")))

        add(FlashcardSet("a2_20", "A2", 20, "Современные выражения", "💬",
            listOf("guay", "chulo", "majo", "mono",
                   "colega", "tío", "tía", "mega",
                   "Por cierto", "Por suerte", "a propósito", "por casualidad",
                   "A lo mejor", "De pronto", "Estar de moda", "Hacer falta",
                   "no tener ni idea", "pasarlo bien", "dar la mano", "hacer caso")))

        // ════════════════════════════════════════════════════════
        //  B1 — абстрактные темы (20 sets)
        // ════════════════════════════════════════════════════════

        add(FlashcardSet("b1_01", "B1", 1, "Мнения и убеждения", "💭",
            listOf("argumentar", "convencer", "debatir", "criticar",
                   "afirmar", "confirmar", "opinar", "la opinión",
                   "el motivo", "la idea", "creer", "parecer",
                   "dudar", "resolver", "el pensamiento", "la decisión",
                   "defender", "explicar")))

        add(FlashcardSet("b1_02", "B1", 2, "Социальные проблемы", "⚖️",
            listOf("el conflicto", "la pobreza", "la desigualdad", "la discriminación",
                   "el racismo", "la inmigración", "el ciudadano", "el extranjero",
                   "la libertad", "la justicia", "la igualdad", "la paz",
                   "la sociedad", "la guerra", "el activista", "el voluntariado")))

        add(FlashcardSet("b1_03", "B1", 3, "Окружающая среда", "🌍",
            listOf("el cambio climático", "la contaminación", "reciclar", "contaminar",
                   "los residuos", "la energía solar", "el plástico de un solo uso", "la inundación",
                   "el incendio forestal", "el ecologista", "proteger", "la naturaleza",
                   "el medio ambiente", "el clima", "el planeta", "la ecología")))

        add(FlashcardSet("b1_04", "B1", 4, "Политика", "🏛️",
            listOf("el gobierno", "el presidente", "el ministro", "el partido",
                   "el político", "el alcalde", "el candidato", "el voto",
                   "la ley", "la democracia", "el conservador", "el escaño",
                   "el embajador", "votar", "elegir", "el activista",
                   "el atentado", "el demócrata")))

        add(FlashcardSet("b1_05", "B1", 5, "Бизнес и экономика", "📈",
            listOf("el comercio", "el contable", "el emprendedor", "el inversor",
                   "el presupuesto", "el préstamo", "el salario mínimo", "la economía",
                   "el mercado", "la empresa", "la inversión", "el cliente",
                   "el producto", "vender", "comprar", "negociar",
                   "exportar", "importar", "ahorrar")))

        add(FlashcardSet("b1_06", "B1", 6, "Реклама и маркетинг", "📢",
            listOf("la publicidad", "el anuncio", "la marca", "el cliente",
                   "el consumidor", "el producto", "el logo", "la campaña",
                   "el descuento", "la promoción", "la oferta", "el escaparate",
                   "el banner", "vender", "comprar", "el influencer",
                   "la publicación", "el contenido")))

        add(FlashcardSet("b1_07", "B1", 7, "Журналистика и медиа", "📰",
            listOf("el periodista", "el reportaje", "la noticia", "el periódico",
                   "la revista", "el canal", "el programa", "la radio",
                   "la televisión", "el podcast", "el contenido", "el corresponsal",
                   "el comunicado", "el editorial", "la suscripción", "el trending topic",
                   "el canal de YouTube", "publicar")))

        add(FlashcardSet("b1_08", "B1", 8, "Юриспруденция", "⚖️",
            listOf("el abogado", "el juez", "el tribunal", "el juicio",
                   "el contrato", "el delito", "el crimen", "el sospechoso",
                   "el testigo", "la denuncia", "la cárcel", "el veredicto",
                   "el acusado", "acusar", "denunciar", "la apelación",
                   "la cláusula", "el atraco", "el chantaje", "la herencia")))

        add(FlashcardSet("b1_09", "B1", 9, "Психология", "🧠",
            listOf("la mente", "la emoción", "el estrés", "la motivación",
                   "la confianza", "la autoestima", "la empatía", "la fobia",
                   "el bienestar", "la frustración", "la meditación", "la terapia",
                   "el psicólogo", "el terapeuta", "el trauma", "la adicción",
                   "extrovertido", "introvertido", "meditar", "superar")))

        add(FlashcardSet("b1_10", "B1", 10, "Философия и идеи", "🤔",
            listOf("la idea", "la verdad", "la mentira", "la realidad",
                   "el sentido", "la conciencia", "creer", "pensar",
                   "reflexionar", "la duda", "la lógica", "la teoría",
                   "la pregunta", "la respuesta", "el conocimiento", "el pensamiento",
                   "comprender", "imaginar")))

        add(FlashcardSet("b1_11", "B1", 11, "История", "📜",
            listOf("el siglo", "la historia", "el pasado", "el rey",
                   "la reina", "el imperio", "la guerra", "la paz",
                   "el héroe", "la batalla", "el arqueólogo", "la civilización",
                   "la cultura", "la tradición", "la dinastía", "el palacio",
                   "la conquista", "la independencia")))

        add(FlashcardSet("b1_12", "B1", 12, "Литература и письмо", "📖",
            listOf("el libro", "la novela", "el cuento", "el poema",
                   "la rima", "el autor", "el escritor", "el clásico",
                   "el protagonista", "la ciencia ficción", "el lector", "el capítulo",
                   "la página", "el título", "publicar", "leer",
                   "escribir", "la biblioteca", "el editor", "el verso")))

        add(FlashcardSet("b1_13", "B1", 13, "Изобразительное искусство", "🎨",
            listOf("el arte abstracto", "el autorretrato", "el cubismo", "el impresionismo",
                   "el surrealismo", "el minimalismo", "el mural", "el lienzo",
                   "el carboncillo", "el pincel fino", "el pop art", "el barroco",
                   "el coleccionista de arte", "el caballete", "el mosaico", "el pastel",
                   "la pintura", "el cuadro")))

        add(FlashcardSet("b1_14", "B1", 14, "Театр и перформанс", "🎭",
            listOf("el teatro", "el escenario", "el actor", "la actriz",
                   "el director", "el público", "el ensayo", "la obra",
                   "la comedia", "la tragedia", "el drama", "el bailarín",
                   "el estreno", "el camerino", "la danza", "el ballet",
                   "interpretar", "aplaudir")))

        add(FlashcardSet("b1_15", "B1", 15, "Спорт профессиональный", "🏆",
            listOf("el campeón", "el entrenador", "el deportista", "el partido",
                   "el equipo", "el estadio", "competir", "ganar",
                   "perder", "el atletismo", "el ciclismo", "el alpinismo",
                   "el balonmano", "el bádminton", "el delantero", "el defensa",
                   "el portero", "el árbitro", "el gol", "el empate")))

        add(FlashcardSet("b1_16", "B1", 16, "Наука и исследования", "🔬",
            listOf("el experimento", "el laboratorio", "el científico", "el descubrimiento",
                   "la teoría", "la hipótesis", "investigar", "descubrir",
                   "el átomo", "el electrón", "el método científico", "el telescopio",
                   "el cohete", "el big bang", "el experimento", "el dióxido de carbono",
                   "el reino animal", "el reino vegetal")))

        add(FlashcardSet("b1_17", "B1", 17, "Технологии глубже", "💻",
            listOf("el algoritmo", "el código", "la programación", "el hardware",
                   "el software", "el archivo PNG", "el correo no deseado", "el adjunto",
                   "el dominio", "el escáner", "el chatbot", "el creador de contenido",
                   "el código abierto", "el directo", "el feed", "el hacker",
                   "el livestream", "el malware")))

        add(FlashcardSet("b1_18", "B1", 18, "Здоровье и образ жизни", "🥗",
            listOf("la salud", "la dieta", "el ejercicio", "el gimnasio",
                   "adelgazar", "engordar", "descansar", "dormir",
                   "el bienestar", "el estrés", "la meditación", "el yoga",
                   "saludable", "sano", "la nutrición", "la alimentación",
                   "vegetariano", "vegano", "el médico", "la farmacia")))

        add(FlashcardSet("b1_19", "B1", 19, "Волонтёрство и НКО", "🤝",
            listOf("el voluntario", "el voluntariado", "la donación", "la caridad",
                   "ayudar", "participar", "contribuir", "la fundación",
                   "el proyecto", "la comunidad autónoma", "el ciudadano", "la sociedad",
                   "la igualdad", "la justicia", "la libertad", "organizar",
                   "la participación", "la voluntad")))

        add(FlashcardSet("b1_20", "B1", 20, "Молодёжный сленг", "🔥",
            listOf("alucinante", "brutal", "flipar", "currar",
                   "mogollón", "guapísimo", "estar harto", "la resaca",
                   "el compi", "desde luego", "Llevarse bien", "Sin más",
                   "Estoy hecho polvo", "Por desgracia", "Darse cuenta", "Tener resaca",
                   "rollo", "¡Qué pasada!", "¡Qué fuerte!")))

        // ════════════════════════════════════════════════════════
        //  B2 — продвинутый язык (20 sets)
        // ════════════════════════════════════════════════════════

        add(FlashcardSet("b2_01", "B2", 1, "Идиоматические выражения", "🎯",
            listOf("Hacer caso omiso", "Hacer la vista gorda", "Estar al loro", "Estoy en las nubes",
                   "Estoy hasta las narices", "Andarse con rodeos", "Coger el toro por los cuernos", "Pagar el pato",
                   "Sentar la cabeza", "Perder los papeles", "Tener entre ceja y ceja", "Estar en las últimas",
                   "No dar palo al agua", "Dar la vuelta a la tortilla", "Me importa un bledo", "Armarse la gorda",
                   "flipante", "la guinda del pastel")))

        add(FlashcardSet("b2_02", "B2", 2, "Деловой испанский", "💼",
            listOf("el contrato", "la empresa", "el cliente", "el proyecto",
                   "la reunión", "el balance contable", "el dividendo", "el flujo de caja",
                   "el mercado laboral", "el ROI", "el PIB", "el corredor de seguros",
                   "financiar", "el plan de pensiones", "el préstamo hipotecario", "negociar",
                   "el inversor", "el plazo de amortización")))

        add(FlashcardSet("b2_03", "B2", 3, "Юридический язык", "⚖️",
            listOf("el abogado", "el juez", "el fiscal", "el notario",
                   "el testamento", "el acusado", "el demandado", "el demandante",
                   "el alegato", "la condena", "la sentencia", "absolver",
                   "condenar", "juzgar", "el derecho administrativo", "el derecho constitucional",
                   "el derecho internacional", "el derecho mercantil", "la difamación", "la calumnia")))

        add(FlashcardSet("b2_04", "B2", 4, "Медицинский язык", "🏥",
            listOf("el cirujano", "el ECG", "el TDAH", "el TOC",
                   "el aneurisma", "el ansiolítico", "el antipirético", "el bypass",
                   "el cateterismo", "el colesterol", "el encefalograma", "el ingreso hospitalario",
                   "el lifting facial", "el linfoma", "el lupus", "el nefrólogo",
                   "el obstetra", "el odontólogo", "el implante mamario", "el aminoácido")))

        add(FlashcardSet("b2_05", "B2", 5, "Финансы и инвестиции", "💹",
            listOf("el PIB", "el ROI", "el dividendo", "el dividendo accionario",
                   "el balance contable", "el blanqueo de dinero", "el desfalco", "el fraude fiscal",
                   "el flujo de caja", "el plazo de amortización", "el oligopolio", "el mercado laboral",
                   "el moroso", "la bancarrota", "el pasivo", "el superávit",
                   "la balanza comercial", "el descubierto", "financiar", "el repunte")))

        add(FlashcardSet("b2_06", "B2", 6, "Экологическая повестка", "♻️",
            listOf("el ecosistema", "el efecto invernadero", "la biodiversidad", "la capa de ozono",
                   "la deforestación", "la energía eólica", "la extinción", "la huella de carbono",
                   "la sequía", "las emisiones", "las energías renovables", "el compostaje",
                   "el vertedero", "preservar", "reforestar", "sostenible")))

        add(FlashcardSet("b2_07", "B2", 7, "Этические дилеммы", "🤲",
            listOf("el dilema moral", "la moral", "la ética", "la conciencia",
                   "la racionalidad", "objetivo", "subjetivo", "racional",
                   "irracional", "la paradoja", "la utopía", "el pensamiento crítico",
                   "la existencia", "la filosofía", "abstracto", "el filósofo")))

        add(FlashcardSet("b2_08", "B2", 8, "Искусственный интеллект", "🤖",
            listOf("la inteligencia artificial", "el algoritmo", "el aprendizaje automático", "el modelo de lenguaje",
                   "la automatización", "el chatbot", "el ciberataque", "la ciberseguridad",
                   "el cifrado", "la encriptación", "la huella digital", "la criptomoneda",
                   "el repositorio", "el firmware", "el hardware", "el software",
                   "la API", "el periférico")))

        add(FlashcardSet("b2_09", "B2", 9, "Социология", "👥",
            listOf("la sociedad", "el ciudadano", "la cultura", "la identidad",
                   "la comunidad autónoma", "la igualdad", "la desigualdad", "la discriminación",
                   "la inmigración", "la pobreza", "la clase social", "la generación",
                   "el conflicto", "la familia monoparental", "la brecha generacional", "el multiculturalismo",
                   "el internacionalismo", "contribuir")))

        add(FlashcardSet("b2_10", "B2", 10, "Антропология и культура", "🌐",
            listOf("la civilización", "la cultura", "la tradición", "la identidad",
                   "el ritual", "el patrimonio", "el folklore", "la genealogía",
                   "la dinastía", "el rito de paso", "la lengua", "el dialecto",
                   "la herencia", "el feudo", "el medievo", "el patrimonio",
                   "el ascendente", "la espiritualidad oriental")))

        add(FlashcardSet("b2_11", "B2", 11, "Лингвистика", "🗣️",
            listOf("la lengua", "el dialecto", "el bilingüismo", "el bilingüe",
                   "el plurilingüismo", "el políglota", "el acento", "el acento agudo",
                   "el acento grave", "el verbo auxiliar", "el verbo intransitivo", "el verbo modal",
                   "el verbo transitivo", "el adjetivo posesivo", "la oración compuesta", "la oración coordinada",
                   "la oración subordinada", "la diéresis", "el homónimo", "la declinación")))

        add(FlashcardSet("b2_12", "B2", 12, "Кино — критика и теория", "🎥",
            listOf("el cineasta", "el guión", "la banda sonora", "el plano americano",
                   "el plano detalle", "el contrapicado", "el picado", "el ojo de pez",
                   "el difusor", "el reflector", "el balance de blancos", "el chroma key",
                   "el plano secuencia", "el guion cinematográfico", "el CGI", "la postproducción",
                   "el especialista", "interpretar")))

        add(FlashcardSet("b2_13", "B2", 13, "Современная литература", "📚",
            listOf("el manuscrito", "el narrador", "el antagonista", "el argumento",
                   "el clímax", "el desenlace", "el flashback", "el género literario",
                   "la novela histórica", "la prosa", "el verso", "la metáfora",
                   "la ironía", "el realismo mágico", "el símbolo", "literario",
                   "narrar", "narrativo", "redactar", "la editorial")))

        add(FlashcardSet("b2_14", "B2", 14, "Музыкальная теория", "🎼",
            listOf("componer", "el bombo", "el charango", "el bis",
                   "la banda sonora", "el cuarteto", "el coro", "el solista",
                   "el compositor", "el sintetizador", "el flamenco", "el folk",
                   "el funk", "el viento", "el contrabajo", "el clarinete",
                   "el bajo", "el aria", "el ritmo", "el álbum")))

        add(FlashcardSet("b2_15", "B2", 15, "Архитектура и дизайн", "🏛️",
            listOf("la fachada", "la urbanización", "la vivienda de protección oficial", "el parquet",
                   "el arte conceptual", "el arte figurativo", "el atelier", "el muralismo",
                   "el dadaísmo", "el expresionismo", "el futurismo", "el barroco",
                   "el cubismo", "el minimalismo", "el mosaico", "el lienzo",
                   "interpretar", "componer")))

        add(FlashcardSet("b2_16", "B2", 16, "Урбанистика", "🏙️",
            listOf("la urbanización", "la ciudad", "el barrio", "la fachada",
                   "la vivienda de protección oficial", "el rascacielos", "el monumento histórico", "el ayuntamiento",
                   "la plaza", "el parque público", "el centro histórico", "el barrio antiguo",
                   "el callejón", "la capital", "el edificio de oficinas", "la avenida",
                   "el puente", "el paseo")))

        add(FlashcardSet("b2_17", "B2", 17, "Гастрономия", "🍷",
            listOf("degustar", "el aceite de oliva", "la paella", "la sangría",
                   "el gazpacho", "la tortilla española", "la tapa", "el vino tinto",
                   "el vino blanco", "el vino rosado", "la sartén", "la olla",
                   "el ingrediente", "la cucharada", "la freidora", "el congelador",
                   "freír", "hornear", "pelar", "la rebanada")))

        add(FlashcardSet("b2_18", "B2", 18, "Дипломатия", "🤝",
            listOf("el embajador", "el cónsul", "el tratado", "la alianza",
                   "la guerra", "la paz", "el conflicto", "la república",
                   "la monarquía", "la civilización", "el imperio", "la conquista",
                   "la colonización", "colonizar", "conquistar", "el armisticio",
                   "la posguerra", "la primera guerra mundial", "la guerra civil")))

        add(FlashcardSet("b2_19", "B2", 19, "Журналистские расследования", "📰",
            listOf("el periodista", "la noticia falsa", "la opinión pública", "la libertad de prensa",
                   "la transmisión en vivo", "la redacción", "el redactor", "censurar",
                   "difundir", "el reportaje", "el corresponsal", "el comunicado",
                   "el editorial", "el podcast", "el contenido", "el creador de contenido",
                   "publicar")))

        add(FlashcardSet("b2_20", "B2", 20, "Цифровая культура и приватность", "🔒",
            listOf("la ciberseguridad", "el ciberataque", "el cifrado", "la encriptación",
                   "la huella digital", "la criptomoneda", "la inteligencia artificial", "el algoritmo",
                   "el cookie de sesión", "el cortafuegos", "el ransomware", "el ciberacoso",
                   "el grooming", "el certificado digital", "el ancho de banda", "el SEO",
                   "el DNS", "el modelo de lenguaje")))

    }

    fun byLevel(level: String): List<FlashcardSet> = all.filter { it.level == level }.sortedBy { it.order }

    fun byId(id: String): FlashcardSet? = all.firstOrNull { it.id == id }
}
