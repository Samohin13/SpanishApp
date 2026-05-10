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
            listOf("hola", "buenos días", "buenas tardes", "buenas noches", "adiós",
                   "hasta luego", "hasta mañana", "por favor", "gracias", "de nada",
                   "perdón", "lo siento", "sí", "no", "tal vez",
                   "claro")))

        add(FlashcardSet("a1_02_pronouns", "A1", 2, "Личные местоимения", "👤",
            listOf("yo", "tú", "él", "ella", "nosotros",
                   "nosotras", "vosotros", "vosotras", "ellos", "ellas",
                   "usted", "ustedes", "mi", "tu", "su",
                   "nuestro")))

        add(FlashcardSet("a1_03_family", "A1", 3, "Семья", "👨‍👩‍👧",
            listOf("la familia", "el padre", "la madre", "el hermano", "la hermana",
                   "el hijo", "la hija", "el abuelo", "la abuela", "el tío",
                   "la tía", "el primo", "la prima", "el esposo", "la esposa",
                   "el bebé")))

        add(FlashcardSet("a1_04_numbers_1_20", "A1", 4, "Числа 1-20", "🔢",
            listOf("uno", "dos", "tres", "cuatro", "cinco",
                   "seis", "siete", "ocho", "nueve", "diez",
                   "once", "doce", "trece", "catorce", "quince",
                   "dieciséis", "diecisiete", "dieciocho", "diecinueve", "veinte")))

        add(FlashcardSet("a1_05_colors", "A1", 5, "Цвета", "🎨",
            listOf("rojo", "azul", "verde", "amarillo", "negro",
                   "blanco", "gris", "rosa", "naranja", "morado",
                   "marrón", "el color",
                   "el rojo", "¡Claro que sí!", "¡Claro que no!")))

        add(FlashcardSet("a1_06_days_week", "A1", 6, "Дни недели", "📅",
            listOf("lunes", "martes", "miércoles", "jueves", "viernes",
                   "sábado", "domingo", "el día", "la semana", "hoy",
                   "mañana", "ayer", "el fin de semana",
                   "el lunes", "el calendario", "el mediodía", "el reloj", "¿A qué hora?")))

        add(FlashcardSet("a1_07_food_basic", "A1", 7, "Еда — основа", "🍞",
            listOf("la comida", "el pan", "el agua", "la leche", "el café",
                   "el té", "el huevo", "el queso", "la fruta", "la manzana",
                   "el plátano", "la carne", "el pescado", "el arroz", "la sopa",
                   "el desayuno", "la cena")))

        add(FlashcardSet("a1_08_drinks", "A1", 8, "Напитки", "🥤",
            listOf("el jugo", "el zumo", "la cerveza", "el vino", "el refresco",
                   "el agua con gas", "el batido", "el café con leche", "la limonada", "el chocolate",
                   "el agua sin gas", "el zumo natural", "el agua mineral", "el chocolate caliente", "el té negro",
                   "el té verde")))

        add(FlashcardSet("a1_09_house", "A1", 9, "Дом и комнаты", "🏠",
            listOf("la casa", "el piso", "la cocina", "el dormitorio", "el baño",
                   "el salón", "la sala", "la puerta", "la ventana", "la mesa",
                   "la silla", "la cama", "el sofá", "la lámpara")))

        add(FlashcardSet("a1_10_clothes", "A1", 10, "Одежда", "👕",
            listOf("la camisa", "los pantalones", "la falda", "el vestido", "los zapatos",
                   "el sombrero", "la chaqueta", "el abrigo", "los calcetines", "la ropa",
                   "la corbata", "el cinturón",
                   "el bolso", "el gorro", "el jersey", "la bufanda", "la mochila",
                   "la sudadera")))

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
                   "el calor", "la nube", "el cielo", "la temperatura", "el tiempo",
                   "lluvioso", "soleado", "el paraguas", "el invierno", "el verano",
                   "la primavera", "el otoño")))

        add(FlashcardSet("a1_15_city", "A1", 15, "Город", "🏙️",
            listOf("la ciudad", "la calle", "la plaza", "el parque", "la tienda",
                   "el mercado", "el restaurante", "el hotel", "el banco", "el hospital",
                   "la escuela", "el museo", "la iglesia", "el aeropuerto")))

        add(FlashcardSet("a1_16_transport", "A1", 16, "Транспорт", "🚗",
            listOf("el coche", "el autobús", "el tren", "el metro", "el avión",
                   "el barco", "la bicicleta", "la moto", "el taxi",
                   "conducir", "volar", "el viaje", "las vacaciones", "viajar",
                   "el mapa", "la postal", "el boleto")))

        add(FlashcardSet("a1_17_time", "A1", 17, "Время и часы", "⏰",
            listOf("la hora", "el minuto", "el segundo", "la mañana", "la tarde",
                   "la noche", "ahora", "siempre", "nunca", "temprano",
                   "tarde", "pronto",
                   "antes", "después", "luego", "la media noche", "el reloj de pared")))

        add(FlashcardSet("a1_18_school", "A1", 18, "Школа и учёба", "📚",
            listOf("la clase", "el libro", "el cuaderno", "el lápiz", "el bolígrafo",
                   "el estudiante", "el profesor", "la profesora", "la lección", "el examen",
                   "la pregunta", "la respuesta",
                   "aprender", "estudiar", "el colegio", "el diccionario", "la palabra",
                   "la nota")))

        add(FlashcardSet("a1_19_emotions", "A1", 19, "Эмоции", "😊",
            listOf("feliz", "triste", "contento", "enfadado", "cansado",
                   "aburrido", "nervioso", "tranquilo", "asustado", "el amor",
                   "la alegría", "el miedo",
                   "amar", "gustar", "llorar", "reír", "la felicidad",
                   "la amistad")))

        add(FlashcardSet("a1_20_questions", "A1", 20, "Вопросительные слова", "❓",
            listOf("qué", "quién", "dónde", "cuándo", "cómo",
                   "por qué", "cuál", "cuánto", "cuántos",
                   "¿Por qué?", "¿Para qué?", "¿Cómo se dice...?", "¿Qué significa...?", "¿De dónde eres?",
                   "¿Cuánto cuesta?", "¿Qué tal?", "¿Puedes repetir?")))

        // ════════════════════════════════════════════════════════
        //  A2 — расширение базы (20 sets)
        // ════════════════════════════════════════════════════════

        add(FlashcardSet("a2_01", "A2", 1, "Работа и офис", "💼",
            listOf("la oficina", "el jefe", "la jefa", "el empleado", "el contrato",
                   "el sueldo", "la reunión", "el horario", "el cliente", "la empresa",
                   "trabajar", "ganar", "despedir", "contratar", "el proyecto",
                   "el correo electrónico", "el colega", "el currículum", "el ascenso")))

        add(FlashcardSet("a2_02", "A2", 2, "Профессии", "👨‍🔧",
            listOf("el médico", "el ingeniero", "el abogado", "el cocinero", "el camarero",
                   "el taxista", "el policía", "el bombero", "el dentista", "el periodista",
                   "el secretario", "el mecánico", "el agricultor", "el peluquero", "el panadero",
                   "el actor", "el escritor")))

        add(FlashcardSet("a2_03", "A2", 3, "В ресторане", "🍽️",
            listOf("el menú", "la cuenta", "la propina", "el plato", "el tenedor",
                   "la cuchara", "el cuchillo", "el vaso", "la servilleta", "la ensalada",
                   "el postre", "reservar", "pedir", "la bebida")))

        add(FlashcardSet("a2_04", "A2", 4, "Покупки и магазины", "🛍️",
            listOf("el supermercado", "el producto", "la marca", "la oferta", "el descuento",
                   "el escaparate", "el probador", "el recibo", "la caja", "comprar",
                   "vender", "pagar", "el carrito", "el repartidor", "el hipermercado")))

        add(FlashcardSet("a2_05", "A2", 5, "Деньги", "💶",
            listOf("el dinero", "el euro", "la moneda", "el billete", "la tarjeta",
                   "el cheque", "el pago", "la factura", "el cambio", "caro",
                   "barato", "gastar", "ahorrar", "prestar", "pedir prestado",
                   "el ingreso")))

        add(FlashcardSet("a2_06", "A2", 6, "Транспорт детально", "🚆",
            listOf("el andén", "la estación", "el aparcamiento", "el semáforo", "el paso de cebra",
                   "el accidente", "el cinturón de seguridad", "el carné de conducir", "la gasolinera", "la autopista",
                   "el vuelo", "el ferri", "el equipaje", "el retraso", "el motor",
                   "el volante")))

        add(FlashcardSet("a2_07", "A2", 7, "Гостиница и путешествие", "🏨",
            listOf("la habitación", "la recepción", "la llave", "el pasaporte", "la maleta",
                   "el viajero", "el turista", "el destino", "el suvenir", "el camping",
                   "el spa", "el documento de identidad", "el cambio de moneda", "el embarque", "el protector solar",
                   "la atracción turística")))

        add(FlashcardSet("a2_08", "A2", 8, "Здоровье и врач", "🩺",
            listOf("el dolor", "la receta", "el antibiótico", "el jarabe", "el chequeo",
                   "descansar", "el botiquín", "el embarazo", "el entrenamiento", "el estilo de vida",
                   "débil", "adelgazar", "ducharse", "el dolor de espalda", "el dolor de muelas",
                   "el corte")))

        add(FlashcardSet("a2_09", "A2", 9, "Хобби и спорт", "🏋️",
            listOf("el deporte", "el fútbol", "el baloncesto", "el tenis", "el boxeo",
                   "el karate", "el maratón", "el atleta", "el aficionado", "el ataque",
                   "el pase", "el penalti", "el pilates", "el senderismo", "la cancha de fútbol",
                   "el béisbol", "el hockey", "el snowboard")))

        add(FlashcardSet("a2_10", "A2", 10, "Музыка и кино", "🎬",
            listOf("la música", "la canción", "el concierto", "el escenario", "el público",
                   "la guitarra eléctrica", "la guitarra acústica", "el saxofón", "la trompeta", "el tango",
                   "el reggae", "el videoclip", "el vinilo", "el género musical", "el instrumento musical",
                   "el teatro", "la entrada", "aplaudir")))

        add(FlashcardSet("a2_11", "A2", 11, "Технологии и устройства", "📱",
            listOf("el ordenador", "el móvil", "la pantalla", "el teclado", "internet",
                   "el wifi", "el archivo PDF", "el GPS", "el antivirus", "el auricular",
                   "conectar", "borrar", "el botón de enviar", "el 5G", "el 4G",
                   "el archivo MP3", "el bug")))

        add(FlashcardSet("a2_12", "A2", 12, "Дом — мебель и комнаты", "🛋️",
            listOf("el apartamento", "el balcón", "el escritorio", "el espejo grande", "el horno",
                   "el lavavajillas", "la aspiradora", "la bombilla", "el grifo", "el interruptor",
                   "la cerradura", "el frigorifico", "el secador de pelo", "la cama de matrimonio", "el cuarto",
                   "el pasillo", "decorar")))

        add(FlashcardSet("a2_13", "A2", 13, "Кулинария и приготовление", "👨‍🍳",
            listOf("el aceite de oliva", "el ingrediente", "el vinagre", "el congelador", "la sartén",
                   "la olla", "la cucharada", "la cucharadita", "la nevera", "la tostadora",
                   "la licuadora", "la rebanada", "la rodaja", "freír", "hornear",
                   "pelar", "rallar", "revolver", "la paella")))

        add(FlashcardSet("a2_14", "A2", 14, "Эмоции продвинутые", "😢",
            listOf("el cariño", "el deseo", "el enfado", "el orgullo", "el humor",
                   "la felicidad pura", "la culpa", "la lástima", "la pereza", "la emoción",
                   "celoso", "avergonzado", "desilusionado", "emocionado", "alegre",
                   "agradecido")))

        add(FlashcardSet("a2_15", "A2", 15, "Описания людей", "🙋",
            listOf("amable", "simpatico", "antipatico", "generoso", "paciente",
                   "curioso", "creativo", "sociable", "honesto", "grosero",
                   "calvo", "pelirrojo", "activo", "cariñoso", "cobarde",
                   "educado", "maleducado", "mentiroso", "tacaño", "valiente")))

        add(FlashcardSet("a2_16", "A2", 16, "Описания вещей", "🔶",
            listOf("antiguo", "artificial", "artesanal", "automático", "colorido",
                   "delicioso", "dinámico", "elástico", "emocionante", "esencial",
                   "espacioso", "especial", "estable", "compacto", "crujiente",
                   "cremoso", "desagradable", "diferente", "electrónico")))

        add(FlashcardSet("a2_17", "A2", 17, "Природа и пейзажи", "🌳",
            listOf("el cielo estrellado", "el clima", "el espacio", "el cometa", "el girasol",
                   "el clavel", "el manzano", "el cerezo", "el limonero", "el naranjo",
                   "el pino", "el pico", "el rebaño", "el parque nacional", "el rayo de sol",
                   "el barro", "despejado", "congelado")))

        add(FlashcardSet("a2_18", "A2", 18, "Праздники и события", "🎉",
            listOf("la fiesta", "el regalo", "la sorpresa", "el cumpleaños", "la boda",
                   "el aniversario", "la Navidad", "celebrar", "invitar", "felicitar",
                   "la discoteca", "Las Fallas", "San Fermín", "disfrutar", "emocionarse")))

        add(FlashcardSet("a2_19", "A2", 19, "Образование детально", "🎓",
            listOf("el aula", "el cuestionario", "el ejercicio", "el examen final", "el examen oral",
                   "el certificado", "el conocimiento", "el bloc de notas", "el calendario escolar", "aprobar",
                   "corregir", "concentrarse",
                   "enseñar", "entender", "practicar", "repetir", "traducir",
                   "el texto", "el vocabulario", "la pronunciación")))

        add(FlashcardSet("a2_20", "A2", 20, "Современные выражения", "💬",
            listOf("guay", "chulo", "majo", "mono", "colega",
                   "tío", "tía", "mega", "Por cierto", "Por suerte",
                   "a propósito", "por casualidad", "A lo mejor", "De pronto", "Estar de moda",
                   "Hacer falta", "no tener ni idea", "pasarlo bien", "dar la mano", "hacer caso")))

        // ════════════════════════════════════════════════════════
        //  B1 — абстрактные темы (20 sets)
        // ════════════════════════════════════════════════════════

        add(FlashcardSet("b1_01", "B1", 1, "Мнения и убеждения", "💭",
            listOf("argumentar", "convencer", "debatir", "criticar", "afirmar",
                   "confirmar", "opinar", "la opinión", "el motivo", "la idea",
                   "creer", "parecer", "dudar", "resolver", "el pensamiento",
                   "la decisión", "defender", "explicar")))

        add(FlashcardSet("b1_02", "B1", 2, "Социальные проблемы", "⚖️",
            listOf("el conflicto", "la pobreza", "la desigualdad", "la discriminación", "el racismo",
                   "la inmigración", "el ciudadano", "el extranjero", "la libertad", "la justicia",
                   "la igualdad", "la paz", "la sociedad", "la guerra", "el activista",
                   "el voluntariado")))

        add(FlashcardSet("b1_03", "B1", 3, "Окружающая среда", "🌍",
            listOf("el cambio climático", "la contaminación", "reciclar", "contaminar", "los residuos",
                   "la energía solar", "el plástico de un solo uso", "la inundación", "el incendio forestal", "el ecologista",
                   "proteger", "la naturaleza", "el medio ambiente", "el planeta", "la ecología")))

        add(FlashcardSet("b1_04", "B1", 4, "Политика", "🏛️",
            listOf("el gobierno", "el presidente", "el ministro", "el partido", "el político",
                   "el alcalde", "el candidato", "el voto", "la ley", "la democracia",
                   "el conservador", "el escaño", "el embajador", "votar", "elegir",
                   "el atentado", "el demócrata")))

        add(FlashcardSet("b1_05", "B1", 5, "Бизнес и экономика", "📈",
            listOf("el comercio", "el contable", "el emprendedor", "el inversor", "el presupuesto",
                   "el préstamo", "el salario mínimo", "la economía", "la inversión", "negociar",
                   "exportar", "importar",
                   "la exportación", "la hipoteca", "la importación", "la nómina", "la startup",
                   "el préstamo personal")))

        add(FlashcardSet("b1_06", "B1", 6, "Реклама и маркетинг", "📢",
            listOf("la publicidad", "el anuncio", "el consumidor", "el logo", "la campaña",
                   "la promoción", "el banner", "el influencer", "la publicación", "el contenido",
                   "el eslogan", "el logotipo", "la marca personal", "el suscriptor", "la oferta y demanda",
                   "el feedback", "el folleto turístico", "el inventario")))

        add(FlashcardSet("b1_07", "B1", 7, "Журналистика и медиа", "📰",
            listOf("el reportaje", "la noticia", "el periódico", "la revista", "el canal",
                   "el programa", "la radio", "la televisión", "el podcast", "el corresponsal",
                   "el comunicado", "el editorial", "la suscripción", "el trending topic", "el canal de YouTube",
                   "publicar")))

        add(FlashcardSet("b1_08", "B1", 8, "Юриспруденция", "⚖️",
            listOf("el juez", "el tribunal", "el juicio", "el delito", "el crimen",
                   "el sospechoso", "el testigo", "la denuncia", "la cárcel", "el veredicto",
                   "el acusado", "acusar", "denunciar", "la apelación", "la cláusula",
                   "el atraco", "el chantaje", "la herencia")))

        add(FlashcardSet("b1_09", "B1", 9, "Психология", "🧠",
            listOf("la mente", "el estrés", "la motivación", "la confianza", "la autoestima",
                   "la empatía", "la fobia", "el bienestar", "la frustración", "la meditación",
                   "la terapia", "el psicólogo", "el terapeuta", "el trauma", "la adicción",
                   "extrovertido", "introvertido", "meditar", "superar")))

        add(FlashcardSet("b1_10", "B1", 10, "Философия и идеи", "🤔",
            listOf("la verdad", "la mentira", "la realidad", "el sentido", "la conciencia",
                   "pensar", "reflexionar", "la duda", "la lógica", "la teoría",
                   "comprender", "imaginar",
                   "el intelecto", "el saber", "la sabiduría", "la virtud", "el razonamiento",
                   "la moraleja")))

        add(FlashcardSet("b1_11", "B1", 11, "История", "📜",
            listOf("el siglo", "la historia", "el pasado", "el rey", "la reina",
                   "el imperio", "el héroe", "la batalla", "el arqueólogo", "la civilización",
                   "la cultura", "la tradición", "la dinastía", "el palacio", "la conquista",
                   "la independencia")))

        add(FlashcardSet("b1_12", "B1", 12, "Литература и письмо", "📖",
            listOf("la novela", "el cuento", "el poema", "la rima", "el autor",
                   "el clásico", "el protagonista", "la ciencia ficción", "el lector", "el capítulo",
                   "la página", "el título", "leer", "escribir", "la biblioteca",
                   "el editor", "el verso")))

        add(FlashcardSet("b1_13", "B1", 13, "Изобразительное искусство", "🎨",
            listOf("el arte abstracto", "el autorretrato", "el cubismo", "el impresionismo", "el surrealismo",
                   "el minimalismo", "el mural", "el lienzo", "el carboncillo", "el pincel fino",
                   "el pop art", "el barroco", "el coleccionista de arte", "el caballete", "el mosaico",
                   "el pastel", "la pintura", "el cuadro")))

        add(FlashcardSet("b1_14", "B1", 14, "Театр и перформанс", "🎭",
            listOf("la actriz", "el director", "el ensayo", "la obra", "la comedia",
                   "la tragedia", "el drama", "el bailarín", "el estreno", "el camerino",
                   "la danza", "el ballet", "interpretar",
                   "ensayar", "la coreografía", "la obra de teatro", "el guionista", "la butaca",
                   "la ovación")))

        add(FlashcardSet("b1_15", "B1", 15, "Спорт профессиональный", "🏆",
            listOf("el campeón", "el entrenador", "el deportista", "el equipo", "el estadio",
                   "competir", "perder", "el atletismo", "el ciclismo", "el alpinismo",
                   "el balonmano", "el bádminton", "el delantero", "el defensa", "el portero",
                   "el árbitro", "el gol", "el empate")))

        add(FlashcardSet("b1_16", "B1", 16, "Наука и исследования", "🔬",
            listOf("el experimento", "el laboratorio", "el científico", "el descubrimiento", "la hipótesis",
                   "investigar", "descubrir", "el átomo", "el electrón", "el método científico",
                   "el telescopio", "el cohete", "el big bang", "el dióxido de carbono", "el reino animal",
                   "el reino vegetal")))

        add(FlashcardSet("b1_17", "B1", 17, "Технологии глубже", "💻",
            listOf("el algoritmo", "el código", "la programación", "el hardware", "el software",
                   "el archivo PNG", "el correo no deseado", "el adjunto", "el dominio", "el escáner",
                   "el chatbot", "el creador de contenido", "el código abierto", "el directo", "el feed",
                   "el hacker", "el livestream", "el malware")))

        add(FlashcardSet("b1_18", "B1", 18, "Здоровье и образ жизни", "🥗",
            listOf("la salud", "la dieta", "el gimnasio", "engordar", "dormir",
                   "el yoga", "saludable", "sano", "la nutrición", "la alimentación",
                   "vegetariano", "vegano", "la farmacia",
                   "el insomnio", "la deshidratación", "la hidratación", "la dieta mediterránea", "el suplemento alimenticio")))

        add(FlashcardSet("b1_19", "B1", 19, "Волонтёрство и НКО", "🤝",
            listOf("el voluntario", "la donación", "la caridad", "ayudar", "participar",
                   "contribuir", "la fundación", "la comunidad autónoma", "organizar", "la participación",
                   "la voluntad",
                   "la ONG", "el donativo", "la beneficencia", "la solidaridad humana", "la sociedad civil",
                   "el compromiso social", "la ayuda mutua")))

        add(FlashcardSet("b1_20", "B1", 20, "Молодёжный сленг", "🔥",
            listOf("alucinante", "brutal", "flipar", "currar", "mogollón",
                   "guapísimo", "estar harto", "la resaca", "el compi", "desde luego",
                   "Llevarse bien", "Sin más", "Estoy hecho polvo", "Por desgracia", "Darse cuenta",
                   "Tener resaca", "rollo", "¡Qué pasada!", "¡Qué fuerte!")))

        // ════════════════════════════════════════════════════════
        //  B2 — продвинутый язык (20 sets)
        // ════════════════════════════════════════════════════════

        add(FlashcardSet("b2_01", "B2", 1, "Идиоматические выражения", "🎯",
            listOf("Hacer caso omiso", "Hacer la vista gorda", "Estar al loro", "Estoy en las nubes", "Estoy hasta las narices",
                   "Andarse con rodeos", "Coger el toro por los cuernos", "Pagar el pato", "Sentar la cabeza", "Perder los papeles",
                   "Tener entre ceja y ceja", "Estar en las últimas", "No dar palo al agua", "Dar la vuelta a la tortilla", "Me importa un bledo",
                   "Armarse la gorda", "flipante", "la guinda del pastel")))

        add(FlashcardSet("b2_02", "B2", 2, "Деловой испанский", "💼",
            listOf("el balance contable", "el dividendo", "el flujo de caja", "el mercado laboral", "el ROI",
                   "el PIB", "el corredor de seguros", "financiar", "el plan de pensiones", "el préstamo hipotecario",
                   "el plazo de amortización",
                   "el coach", "el KPI", "el dashboard", "la retroalimentación", "la evaluación de desempeño",
                   "el indicador clave", "la dimisión", "el subordinado")))

        add(FlashcardSet("b2_03", "B2", 3, "Юридический язык", "⚖️",
            listOf("el fiscal", "el notario", "el testamento", "el demandado", "el demandante",
                   "el alegato", "la condena", "la sentencia", "absolver", "condenar",
                   "juzgar", "el derecho administrativo", "el derecho constitucional", "el derecho internacional", "el derecho mercantil",
                   "la difamación", "la calumnia")))

        add(FlashcardSet("b2_04", "B2", 4, "Медицинский язык", "🏥",
            listOf("el cirujano", "el ECG", "el TDAH", "el TOC", "el aneurisma",
                   "el ansiolítico", "el antipirético", "el bypass", "el cateterismo", "el colesterol",
                   "el encefalograma", "el ingreso hospitalario", "el lifting facial", "el linfoma", "el lupus",
                   "el nefrólogo", "el obstetra", "el odontólogo", "el implante mamario", "el aminoácido")))

        add(FlashcardSet("b2_05", "B2", 5, "Финансы и инвестиции", "💹",
            listOf("el dividendo accionario", "el blanqueo de dinero", "el desfalco", "el fraude fiscal", "el oligopolio",
                   "el moroso", "la bancarrota", "el pasivo", "el superávit", "la balanza comercial",
                   "el descubierto", "el repunte",
                   "la inflación", "la deuda", "la recesión", "la deflación", "la diversificación",
                   "la cartera de inversiones", "la rentabilidad anual")))

        add(FlashcardSet("b2_06", "B2", 6, "Экологическая повестка", "♻️",
            listOf("el ecosistema", "el efecto invernadero", "la biodiversidad", "la capa de ozono", "la deforestación",
                   "la energía eólica", "la extinción", "la huella de carbono", "la sequía", "las emisiones",
                   "las energías renovables", "el compostaje", "el vertedero", "preservar", "reforestar",
                   "sostenible")))

        add(FlashcardSet("b2_07", "B2", 7, "Этические дилеммы", "🤲",
            listOf("el dilema moral", "la moral", "la ética", "la racionalidad", "objetivo",
                   "subjetivo", "racional", "irracional", "la paradoja", "la utopía",
                   "el pensamiento crítico", "la existencia", "la filosofía", "abstracto", "el filósofo")))

        add(FlashcardSet("b2_08", "B2", 8, "Искусственный интеллект", "🤖",
            listOf("la inteligencia artificial", "el aprendizaje automático", "el modelo de lenguaje", "la automatización", "el ciberataque",
                   "la ciberseguridad", "el cifrado", "la encriptación", "la huella digital", "la criptomoneda",
                   "el repositorio", "el firmware", "la API", "el periférico")))

        add(FlashcardSet("b2_09", "B2", 9, "Социология", "👥",
            listOf("la identidad", "la clase social", "la generación", "la familia monoparental", "la brecha generacional",
                   "el multiculturalismo", "el internacionalismo",
                   "el feminismo", "la igualdad de género", "la exclusión social", "la marginación", "la cuota de género",
                   "la psicología social", "la brecha digital", "la reinserción social", "la minoría parlamentaria")))

        add(FlashcardSet("b2_10", "B2", 10, "Антропология и культура", "🌐",
            listOf("el ritual", "el patrimonio", "el folklore", "la genealogía", "el rito de paso",
                   "la lengua", "el dialecto", "el feudo", "el medievo", "el ascendente",
                   "la espiritualidad oriental",
                   "el prejuicio", "los estereotipos", "la subvención", "la zarzuela", "el toreo",
                   "el agnóstico", "el ateísmo", "el hinduismo", "el protestantismo")))

        add(FlashcardSet("b2_11", "B2", 11, "Лингвистика", "🗣️",
            listOf("el bilingüismo", "el bilingüe", "el plurilingüismo", "el políglota", "el acento",
                   "el acento agudo", "el acento grave", "el verbo auxiliar", "el verbo intransitivo", "el verbo modal",
                   "el verbo transitivo", "el adjetivo posesivo", "la oración compuesta", "la oración coordinada", "la oración subordinada",
                   "la diéresis", "el homónimo", "la declinación")))

        add(FlashcardSet("b2_12", "B2", 12, "Кино — критика и теория", "🎥",
            listOf("el cineasta", "el guión", "la banda sonora", "el plano americano", "el plano detalle",
                   "el contrapicado", "el picado", "el ojo de pez", "el difusor", "el reflector",
                   "el balance de blancos", "el chroma key", "el plano secuencia", "el guion cinematográfico", "el CGI",
                   "la postproducción", "el especialista")))

        add(FlashcardSet("b2_13", "B2", 13, "Современная литература", "📚",
            listOf("el manuscrito", "el narrador", "el antagonista", "el argumento", "el clímax",
                   "el desenlace", "el flashback", "el género literario", "la novela histórica", "la prosa",
                   "la metáfora", "la ironía", "el realismo mágico", "el símbolo", "literario",
                   "narrar", "narrativo", "redactar", "la editorial")))

        add(FlashcardSet("b2_14", "B2", 14, "Музыкальная теория", "🎼",
            listOf("componer", "el bombo", "el charango", "el bis", "el cuarteto",
                   "el coro", "el solista", "el compositor", "el sintetizador", "el flamenco",
                   "el folk", "el funk", "el contrabajo", "el clarinete", "el bajo",
                   "el aria", "el ritmo", "el álbum")))

        add(FlashcardSet("b2_15", "B2", 15, "Архитектура и дизайн", "🏛️",
            listOf("la fachada", "la urbanización", "la vivienda de protección oficial", "el parquet", "el arte conceptual",
                   "el arte figurativo", "el atelier", "el muralismo", "el dadaísmo", "el expresionismo",
                   "el futurismo",
                   "el interiorismo", "el estilo bohemio", "el estilo escandinavo", "el andamio", "el armazón",
                   "el fundamento", "el techo falso", "el hormigonado", "el acero")))

        add(FlashcardSet("b2_16", "B2", 16, "Урбанистика", "🏙️",
            listOf("el barrio", "el rascacielos", "el monumento histórico", "el ayuntamiento", "el parque público",
                   "el centro histórico", "el barrio antiguo", "el callejón", "la capital", "el edificio de oficinas",
                   "la avenida", "el puente", "el paseo",
                   "la planificación urbana", "el ensanche", "el chalé adosado", "la rotonda con fuente", "la depuradora",
                   "la planta de tratamiento")))

        add(FlashcardSet("b2_17", "B2", 17, "Гастрономия", "🍷",
            listOf("degustar", "la sangría", "el gazpacho", "la tortilla española", "la tapa",
                   "el vino tinto", "el vino blanco", "el vino rosado", "la freidora",
                   "la trufa", "la ostra", "el lenguado", "la vieira", "la morcilla",
                   "el ajoblanco", "la viticultura", "el enólogo", "el sabor umami", "el regusto",
                   "la decantación")))

        add(FlashcardSet("b2_18", "B2", 18, "Дипломатия", "🤝",
            listOf("el cónsul", "el tratado", "la alianza", "la república", "la monarquía",
                   "la colonización", "colonizar", "conquistar", "el armisticio", "la posguerra",
                   "la primera guerra mundial", "la guerra civil",
                   "el diplomático", "la diplomacia internacional", "la política exterior", "el solicitante de asilo", "el desplazado",
                   "el bloque soviético", "la perestroika", "la diáspora")))

        add(FlashcardSet("b2_19", "B2", 19, "Журналистские расследования", "📰",
            listOf("la noticia falsa", "la opinión pública", "la libertad de prensa", "la transmisión en vivo", "la redacción",
                   "el redactor", "censurar", "difundir",
                   "demostrar", "negar", "la moción de censura", "la prueba forense", "el aforismo",
                   "la paráfrasis", "la infiltración", "el rehén")))

        add(FlashcardSet("b2_20", "B2", 20, "Цифровая культура и приватность", "🔒",
            listOf("el cookie de sesión", "el cortafuegos", "el ransomware", "el ciberacoso", "el grooming",
                   "el certificado digital", "el ancho de banda", "el SEO", "el DNS",
                   "el community manager", "la red neuronal", "el hipervínculo", "el commit", "el debug",
                   "la copia espejo", "la latencia", "la web responsiva")))

        // ════════════════════════════════════════════════════════
        //  Дополнительные verb-only сеты (≥5 verb-сетов на уровень)
        // ════════════════════════════════════════════════════════

        add(FlashcardSet("a1_21_verbs_daily", "A1", 21, "Глаголы повседневных действий", "🚶",
            listOf("escuchar", "mirar", "abrir", "cerrar", "beber",
                   "jugar", "llegar", "salir", "entrar", "llevar",
                   "tomar", "encontrar", "esperar", "buscar", "tocar")))

        add(FlashcardSet("a1_22_verbs_motion", "A1", 22, "Глаголы движения", "🏃",
            listOf("correr", "saltar", "sentarse", "levantarse", "quitarse",
                   "traer", "andar", "nadar", "pasear", "bailar",
                   "caer", "manejar", "coger", "nacer")))

        add(FlashcardSet("a1_23_verbs_speech", "A1", 23, "Глаголы общения и речи", "💬",
            listOf("preguntar", "responder", "llamar", "contar", "saludar",
                   "disculpar", "agradecer", "presentar", "sonreír", "oír",
                   "describir", "besar", "abrazar", "cantar")))

        add(FlashcardSet("a1_24_verbs_home", "A1", 24, "Глаголы дома и быта", "🏠",
            listOf("cocinar", "limpiar", "peinarse", "acostarse", "cepillarse",
                   "despertar", "preparar", "poner", "quitar", "romper",
                   "mezclar", "levantar", "olvidar", "recordar (memoria)")))

        add(FlashcardSet("a2_21_verbs_routine", "A2", 21, "Возвратные глаголы рутины", "🪥",
            listOf("despertarse", "dormirse", "afeitarse", "maquillarse", "prepararse",
                   "ponerse", "relajarse", "equivocarse", "quejarse", "disculparse",
                   "mudarse", "quedarse", "reunirse", "olvidarse", "enfadarse")))

        add(FlashcardSet("a2_22_verbs_travel", "A2", 22, "Глаголы путешествий и движения", "✈️",
            listOf("alquilar", "cancelar", "continuar", "girar", "cruzar",
                   "parar", "aparecer", "desaparecer", "retirarse", "visitar",
                   "navegar", "montar", "planear", "tardar", "avanzar")))

        add(FlashcardSet("a2_23_verbs_kitchen", "A2", 23, "Глаголы кухни и приготовления", "🍳",
            listOf("calentar", "cenar", "almorzar", "desayunar", "masticar",
                   "tragar", "probar", "envolver", "colgar", "fotografiar",
                   "dibujar", "pintar", "planchar", "barrer", "encender", "apagar")))

        add(FlashcardSet("a2_24_verbs_money", "A2", 24, "Глаголы покупок и обмена", "💸",
            listOf("cobrar", "devolver", "permitir", "prohibir", "incluir",
                   "obtener", "guardar", "prometer", "firmar", "entregar",
                   "enviar", "mandar", "repartir", "compartir", "separar", "dividir")))

        add(FlashcardSet("a2_25_verbs_health", "A2", 25, "Глаголы здоровья и тела", "🩹",
            listOf("recetar", "recuperarse", "enfermar", "asustarse", "toser",
                   "sangrar", "sudar", "bostezar", "roncar", "caerse",
                   "dañar", "esconderse", "salvar", "sobrevivir", "solucionar", "reparar")))

        add(FlashcardSet("b1_21_verbs_thought", "B1", 21, "Глаголы мнений и размышлений", "🧐",
            listOf("considerar", "suponer", "asegurar", "confesar", "confiar",
                   "confundir", "observar", "contemplar", "plantear", "predecir",
                   "preferir", "pretender", "revelar", "indicar", "mencionar",
                   "informar", "expresar")))

        add(FlashcardSet("b1_22_verbs_emotion", "B1", 22, "Глаголы эмоций и чувств", "💖",
            listOf("aburrir", "animar", "divertirse", "enorgullecerse", "avergonzarse",
                   "arrepentirse", "desanimar", "interesar", "sufrir", "temer",
                   "preocuparse", "enfrentarse", "desafiar", "simular", "disimular",
                   "engañar")))

        add(FlashcardSet("b1_23_verbs_change", "B1", 23, "Глаголы перемен и развития", "🔄",
            listOf("adaptar", "adaptarse", "convertirse", "desarrollarse", "envejecer",
                   "madurar", "evolucionar", "renacer", "aflojar", "intensificar",
                   "incrementar", "reforzar", "minimizar", "optimizar", "perfeccionar")))

        add(FlashcardSet("b1_24_verbs_achieve", "B1", 24, "Глаголы достижений и попыток", "🎯",
            listOf("conseguir", "lograr", "aprovechar", "luchar", "derrotar",
                   "fracasar", "rechazar", "persistir", "perseverar", "destacar",
                   "escalar", "escapar", "impedir", "huir", "aceptar")))

        add(FlashcardSet("b1_25_verbs_business", "B1", 25, "Глаголы бизнеса и работы", "💼",
            listOf("actuar", "controlar", "autorizar", "dimitir", "jubilarse",
                   "programar", "planificar", "promover", "proponer", "proyectar",
                   "gobernar", "ejercer", "ejecutar", "emprender", "invertir",
                   "liquidar", "negarse")))

        add(FlashcardSet("b2_21_verbs_analysis", "B2", 21, "Глаголы анализа и критики", "🔍",
            listOf("cuestionar", "contrastar", "deliberar", "determinar", "esclarecer",
                   "exponer", "ponderar", "puntualizar", "recalcar", "sopesar",
                   "vislumbrar", "entrever", "ojear", "citar", "categorizar",
                   "establecer")))

        add(FlashcardSet("b2_22_verbs_legal", "B2", 22, "Глаголы юридического и делового языка", "⚖️",
            listOf("abogar", "acatar", "adoptar", "adquirir", "exigir",
                   "garantizar", "homologar", "implementar", "oficializar", "ratificar",
                   "reclamar", "reivindicar", "restituir", "retribuir", "saldar",
                   "suplir", "vedar")))

        add(FlashcardSet("b2_23_verbs_science", "B2", 23, "Научные глаголы", "🔬",
            listOf("circular", "coexistir", "emerger", "erupcionar", "expirar",
                   "generar", "hallar", "implicar", "perdurar", "perecer",
                   "quebrar", "refinar", "reformar", "rejuvenecer", "semejar",
                   "asemejar", "subsistir")))

        add(FlashcardSet("b2_24_verbs_modal", "B2", 24, "Сложные модальные и идиоматические", "🌀",
            listOf("abstenerse", "aportar", "acoger", "afianzar", "afrontar",
                   "alentar", "desertar", "descartar", "resignarse", "reconciliarse",
                   "rememorar", "remunerar", "reconocer", "obsequiar", "operar",
                   "pensionar")))

        add(FlashcardSet("b2_25_verbs_abstract", "B2", 25, "Глаголы абстрактных действий", "🌐",
            listOf("fomentar", "impulsar", "facilitar", "fortalecer", "maximizar",
                   "inhabilitar", "estandarizar", "sofreír", "escalfar", "rehogar",
                   "garabatear", "bocetar", "esquematizar", "ensamblar", "edificar",
                   "derruir")))

        // ════════════════════════════════════════════════════════
        //  Сессия 12 — +5 verb-сетов и +3 тематических на каждый уровень
        // ════════════════════════════════════════════════════════

        // ── A1: 5 verb sets ──
        add(FlashcardSet("a1_25_verbs_movement", "A1", 25, "🚶 Глаголы движения", "🚶",
            listOf("caminar", "correr", "saltar", "bailar")))
        add(FlashcardSet("a1_26_verbs_eating", "A1", 26, "🍴 Глаголы еды", "🍴",
            listOf("comer", "beber", "cocinar", "probar")))
        add(FlashcardSet("a1_27_verbs_home", "A1", 27, "🛏 Глаголы дома", "🛏",
            listOf("dormir", "lavarse", "vestirse", "levantarse")))
        add(FlashcardSet("a1_28_verbs_communication", "A1", 28, "💬 Глаголы общения", "💬",
            listOf("hablar", "escuchar", "preguntar", "responder")))
        add(FlashcardSet("a1_29_verbs_feelings", "A1", 29, "❤️ Глаголы чувств", "❤️",
            listOf("amar", "odiar", "gustar", "preferir")))

        // ── A1: 3 themed sets ──
        add(FlashcardSet("a1_30_theme_art", "A1", 30, "🎨 Искусство (basics)", "🎨",
            listOf("el arte", "el museo", "el pintor", "el cuadro",
                   "la música", "el cantante", "bailar", "cantar")))
        add(FlashcardSet("a1_31_theme_business", "A1", 31, "💼 Работа (basics)", "💼",
            listOf("el trabajo", "la oficina", "el jefe", "el sueldo",
                   "trabajar", "ganar", "el horario", "la reunión")))
        add(FlashcardSet("a1_32_theme_travel", "A1", 32, "✈️ Путешествия+", "✈️",
            listOf("el aeropuerto", "el pasaporte", "el equipaje", "el vuelo",
                   "el hotel", "la habitación", "el mapa", "la maleta")))

        // ── A2: 5 verb sets ──
        add(FlashcardSet("a2_26_verbs_learning", "A2", 26, "🏫 Глаголы учёбы", "🏫",
            listOf("estudiar", "aprender", "enseñar", "practicar")))
        add(FlashcardSet("a2_27_verbs_work", "A2", 27, "💼 Глаголы работы", "💼",
            listOf("trabajar", "ganar", "pagar", "vender")))
        add(FlashcardSet("a2_28_verbs_road", "A2", 28, "🚗 Глаголы дороги", "🚗",
            listOf("conducir", "viajar", "volar", "aterrizar")))
        add(FlashcardSet("a2_29_verbs_goals", "A2", 29, "🎯 Глаголы цели", "🎯",
            listOf("intentar", "lograr", "decidir", "cambiar")))
        add(FlashcardSet("a2_30_verbs_cooperation", "A2", 30, "🤝 Глаголы взаимодействия", "🤝",
            listOf("ayudar", "compartir", "regalar", "prestar")))

        // ── A2: 3 themed sets ──
        add(FlashcardSet("a2_31_theme_art", "A2", 31, "🎨 Искусство (artes)", "🎨",
            listOf("el cine", "la película", "el director", "el actor",
                   "la actriz", "el estreno", "la entrada", "el escenario")))
        add(FlashcardSet("a2_32_theme_business", "A2", 32, "💼 Бизнес (A2)", "💼",
            listOf("el cliente", "la empresa", "el contrato", "el proyecto",
                   "la oferta", "comprar", "vender", "el precio")))
        add(FlashcardSet("a2_33_theme_travel", "A2", 33, "✈️ Путешествия (A2)", "✈️",
            listOf("la frontera", "el visado", "la reserva", "el guía",
                   "la excursión", "el monumento", "la playa", "bronceado")))

        // ── B1: 5 verb sets ──
        add(FlashcardSet("b1_26_verbs_thought", "B1", 26, "🧠 Глаголы мысли", "🧠",
            listOf("pensar", "creer", "dudar", "recordar")))
        add(FlashcardSet("b1_27_verbs_analysis", "B1", 27, "❓ Глаголы анализа", "❓",
            listOf("analizar", "comparar", "explicar", "entender")))
        add(FlashcardSet("b1_28_verbs_emotions", "B1", 28, "🎭 Глаголы эмоций (B1)", "🎭",
            listOf("enfadarse", "alegrarse", "preocuparse", "aburrirse")))
        add(FlashcardSet("b1_29_verbs_desire", "B1", 29, "🤔 Глаголы желания", "🤔",
            listOf("querer", "desear", "esperar", "necesitar")))
        add(FlashcardSet("b1_30_verbs_discussion", "B1", 30, "🗣 Глаголы дискуссии", "🗣",
            listOf("discutir", "opinar", "convencer", "sugerir")))

        // ── B1: 3 themed sets ──
        add(FlashcardSet("b1_31_theme_art", "B1", 31, "🎨 Искусство (B1)", "🎨",
            listOf("la exposición", "la galería", "la escultura", "el retrato",
                   "la obra", "el estilo", "inspirar", "crear")))
        add(FlashcardSet("b1_32_theme_business", "B1", 32, "💼 Бизнес (B1)", "💼",
            listOf("la negociación", "la reunión", "el presupuesto", "el inversor",
                   "la estrategia", "el mercado", "el beneficio", "la pérdida")))
        add(FlashcardSet("b1_33_theme_travel", "B1", 33, "✈️ Путешествия (B1)", "✈️",
            listOf("la aduana", "el itinerario", "la cancelación", "el retraso",
                   "el viajero", "la mochila", "la aventura", "el albergue")))

        // ── B2: 5 verb sets ──
        add(FlashcardSet("b2_26_verbs_argumentation", "B2", 26, "📊 Глаголы аргументации", "📊",
            listOf("argumentar", "demostrar", "refutar", "concluir")))
        add(FlashcardSet("b2_27_verbs_change", "B2", 27, "🌐 Глаголы изменений", "🌐",
            listOf("desarrollar", "evolucionar", "transformar", "mejorar")))
        add(FlashcardSet("b2_28_verbs_decisions", "B2", 28, "⚖️ Глаголы решений", "⚖️",
            listOf("juzgar", "evaluar", "proponer", "rechazar")))
        add(FlashcardSet("b2_29_verbs_achievement", "B2", 29, "🎓 Глаголы достижений", "🎓",
            listOf("conseguir", "alcanzar", "superar", "dominar")))
        add(FlashcardSet("b2_30_verbs_problem", "B2", 30, "🔧 Глаголы решения проблем", "🔧",
            listOf("resolver", "arreglar", "enfrentarse", "gestionar")))

        // ── B2: 3 themed sets ──
        add(FlashcardSet("b2_31_theme_art", "B2", 31, "🎨 Искусство (B2)", "🎨",
            listOf("el patrimonio", "la vanguardia", "la corriente", "el matiz",
                   "la composición", "la perspectiva", "el legado", "el genio")))
        add(FlashcardSet("b2_32_theme_business", "B2", 32, "💼 Бизнес (B2)", "💼",
            listOf("la fusión", "la adquisición", "el accionista", "la rentabilidad",
                   "el consejero", "la directiva", "la auditoría", "la quiebra")))
        add(FlashcardSet("b2_33_theme_travel", "B2", 33, "✈️ Путешествия (B2)", "✈️",
            listOf("el itinerante", "el destino", "la transición", "el visado",
                   "el periplo", "la inmersión", "el alojamiento", "el trayecto")))

    }

    fun byLevel(level: String): List<FlashcardSet> = all.filter { it.level == level }.sortedBy { it.order }

    fun byId(id: String): FlashcardSet? = all.firstOrNull { it.id == id }
}
