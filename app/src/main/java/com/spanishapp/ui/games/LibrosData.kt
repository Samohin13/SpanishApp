package com.spanishapp.ui.games

// ══════════════════════════════════════════════════════════════
//  LIBROS — статичный каталог 100 адаптированных рассказов
//  A1: 1–25  |  A2: 26–50  |  B1: 51–75  |  B2: 76–100
//  difficulty 1–5 точек внутри каждого блока
// ══════════════════════════════════════════════════════════════

data class Libro(
    val id: Int,
    val title: String,
    val level: String,       // "A1", "A2", "B1", "B2"
    val difficulty: Int,     // 1–5
    val topic: String,       // тема (по-русски)
    val text: String,        // текст рассказа (испанский)
    val questions: List<LibroQuestion>
)

data class LibroQuestion(
    val question: String,       // вопрос (испанский)
    val options: List<String>,  // ровно 3 варианта: A, B, C
    val correctIndex: Int       // 0=A, 1=B, 2=C
)

object LibrosData {

    const val PASS_CORRECT = 3  // нужно минимум 3 из 4 правильных

    val all: List<Libro> = listOf(

        // ══════════════════════════════════════════════
        //  БЛОК A1 — Principiante (рассказы 1–25)
        // ══════════════════════════════════════════════

        // ── Сложность 🔴⚪⚪⚪⚪ (1 точка) — уроки 1–5 и 12 ──

        Libro(
            id = 1,
            title = "Mi Casa",
            level = "A1", difficulty = 1, topic = "Дом / Быт",
            text = """
                Esta es mi casa. La casa es grande y bonita.
                Hay una sala, una cocina y dos dormitorios.
                En la sala hay un sofá rojo. En la cocina hay una mesa blanca.
                La cocina es pequeña pero moderna. Me gusta mucho mi casa.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Cómo es la casa?",
                    listOf("Pequeña y fea", "Grande y bonita", "Nueva y cara"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué hay en la sala?",
                    listOf("Una cama azul", "Un sofá rojo", "Una mesa grande"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo es la cocina?",
                    listOf("Grande y antigua", "Pequeña pero moderna", "Bonita y nueva"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo se siente la persona sobre su casa?",
                    listOf("No le gusta", "Le gusta mucho", "Quiere una casa nueva"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 2,
            title = "Max, el Perro",
            level = "A1", difficulty = 1, topic = "Животные",
            text = """
                Luis tiene un perro. El perro se llama Max.
                Max es negro y pequeño. Come arroz y carne todos los días.
                Max duerme en el jardín. Por las mañanas corre y juega.
                Luis quiere mucho a su perro.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Cómo se llama el perro?",
                    listOf("Rex", "Max", "Bruno"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿De qué color es Max?",
                    listOf("Blanco", "Marrón", "Negro"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Dónde duerme Max?",
                    listOf("En la cocina", "En el jardín", "En la cama de Luis"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué come Max?",
                    listOf("Pan y leche", "Arroz y carne", "Frutas y verduras"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 3,
            title = "El Desayuno de Ana",
            level = "A1", difficulty = 1, topic = "Еда / Быт",
            text = """
                Son las ocho de la mañana. Ana está en la cocina.
                Ella bebe un vaso de leche fría. También come pan con mantequilla.
                El pan está caliente y delicioso. Ana come una manzana verde.
                Es un buen desayuno para empezar el día.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Qué hora es?",
                    listOf("Las siete", "Las ocho", "Las nueve"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué bebe Ana?",
                    listOf("Café", "Zumo", "Leche"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Cómo está el pan?",
                    listOf("Frío", "Caliente y delicioso", "Duro"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿De qué color es la manzana?",
                    listOf("Roja", "Amarilla", "Verde"),
                    correctIndex = 2
                )
            )
        ),

        Libro(
            id = 4,
            title = "Mi Familia",
            level = "A1", difficulty = 1, topic = "Семья",
            text = """
                Me llamo Sara. Tengo una familia pequeña.
                Mi madre se llama Elena. Mi padre se llama Pablo.
                Tengo un hermano mayor. Su nombre es David.
                David tiene diecinueve años. Yo tengo quince años.
                Vivimos juntos en Madrid.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Cómo se llama la persona que habla?",
                    listOf("Elena", "Sara", "David"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo es la familia?",
                    listOf("Grande", "Pequeña", "Famosa"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuántos años tiene David?",
                    listOf("Quince", "Diecinueve", "Veinte"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Dónde vive la familia?",
                    listOf("En Barcelona", "En Valencia", "En Madrid"),
                    correctIndex = 2
                )
            )
        ),

        Libro(
            id = 5,
            title = "Buenos Días en la Oficina",
            level = "A1", difficulty = 1, topic = "Работа / Общение",
            text = """
                María llega a la oficina a las nueve.
                Dice: "¡Buenos días!" a todos sus compañeros.
                Su compañero Juan responde: "¡Buenos días, María!"
                María pone el café en la mesa. Los dos hablan un poco.
                El día empieza bien. Hay mucho trabajo hoy.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿A qué hora llega María?",
                    listOf("A las ocho", "A las nueve", "A las diez"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué dice María cuando llega?",
                    listOf("Buenas noches", "Buenos días", "Buenas tardes"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo se llama el compañero?",
                    listOf("Pedro", "Carlos", "Juan"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Qué pone María en la mesa?",
                    listOf("El café", "El libro", "El teléfono"),
                    correctIndex = 0
                )
            )
        ),

        // ── Сложность 🔴🔴⚪⚪⚪ (2 точки) — рассказы 6–11 ──

        Libro(
            id = 6,
            title = "En el Mercado",
            level = "A1", difficulty = 2, topic = "Покупки",
            text = """
                Rosa va al mercado todos los sábados por la mañana.
                Hoy compra verduras frescas: tomates, cebollas y pimientos.
                También compra fruta: manzanas y plátanos maduros.
                El vendedor es muy simpático y le da una naranja de regalo.
                Los precios son buenos hoy. Rosa paga con tarjeta y vuelve a casa contenta.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Cuándo va Rosa al mercado?",
                    listOf("Los domingos", "Los viernes", "Los sábados"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Qué fruta compra Rosa?",
                    listOf("Naranjas y uvas", "Manzanas y plátanos", "Peras y fresas"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué le da el vendedor de regalo?",
                    listOf("Un tomate", "Un plátano", "Una naranja"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Cómo paga Rosa?",
                    listOf("En efectivo", "Con cheque", "Con tarjeta"),
                    correctIndex = 2
                )
            )
        ),

        Libro(
            id = 7,
            title = "El Cumpleaños de Carlos",
            level = "A1", difficulty = 2, topic = "Праздники",
            text = """
                Hoy es el cumpleaños de Carlos. Cumple veinte años.
                Sus amigos organizan una fiesta sorpresa en su apartamento.
                Hay globos de muchos colores y una torta grande de chocolate.
                Todos cantan "Cumpleaños feliz" cuando Carlos entra.
                Carlos recibe muchos regalos: libros, ropa y una bicicleta nueva.
                Está muy contento y abraza a todos sus amigos.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Cuántos años cumple Carlos?",
                    listOf("Quince", "Veinte", "Veinticinco"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué tipo de fiesta es?",
                    listOf("Una fiesta normal", "Una fiesta sorpresa", "Una fiesta de trabajo"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿De qué es la torta?",
                    listOf("De vainilla", "De fresa", "De chocolate"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Qué regalo recibe Carlos entre los mencionados?",
                    listOf("Un teléfono", "Una bicicleta nueva", "Un televisor"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 8,
            title = "El Zoológico",
            level = "A1", difficulty = 2, topic = "Животные / Досуг",
            text = """
                Pablo visita el zoológico con su familia el domingo.
                Hay muchos animales: elefantes, jirafas y leones.
                Pablo observa a los pingüinos con mucha atención.
                Le gustan los pingüinos porque son divertidos y nadan muy rápido.
                Su hermana pequeña prefiere los monos. Los monos son muy ruidosos.
                Al final del día, Pablo está cansado pero muy feliz.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Con quién va Pablo al zoológico?",
                    listOf("Con sus amigos", "Con su familia", "Solo"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué animal prefiere Pablo?",
                    listOf("El elefante", "El mono", "El pingüino"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Por qué le gustan los pingüinos a Pablo?",
                    listOf("Son grandes y bonitos", "Son divertidos y nadan rápido", "Son tranquilos"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué animal prefiere la hermana de Pablo?",
                    listOf("Los leones", "Los monos", "Las jirafas"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 9,
            title = "El Almuerzo del Domingo",
            level = "A1", difficulty = 2, topic = "Еда / Семья",
            text = """
                Todos los domingos la familia Sánchez come junta en casa.
                La abuela Concha prepara la paella con mucho cariño.
                La paella tiene arroz, pollo, verduras de temporada y especias.
                El olor delicioso llena toda la casa. El abuelo pone la mesa.
                Cuando la comida está lista, todos se sientan y hablan de su semana.
                Es el momento favorito de toda la familia.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Cuándo come junta la familia?",
                    listOf("Los sábados", "Los domingos", "Los viernes"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Quién prepara la paella?",
                    listOf("La madre", "El abuelo", "La abuela Concha"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Qué hace el abuelo?",
                    listOf("Cocina la paella", "Pone la mesa", "Compra la comida"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿De qué habla la familia durante el almuerzo?",
                    listOf("De política", "De su semana", "De deportes"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 10,
            title = "Un Domingo en el Parque",
            level = "A1", difficulty = 2, topic = "Досуг / Природа",
            text = """
                La familia Gómez pasa los domingos en el parque grande de la ciudad.
                Los niños juegan al fútbol en el césped verde. Corren y ríen mucho.
                Los padres se sientan en un banco y leen el periódico.
                El perro de la familia corre detrás de una pelota amarilla.
                Hace buen tiempo y el cielo está azul y despejado.
                Al final, todos toman un helado de chocolate. Es un día perfecto.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Dónde pasa la familia los domingos?",
                    listOf("En la playa", "En el parque", "En el centro comercial"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué hacen los niños?",
                    listOf("Juegan al tenis", "Juegan al fútbol", "Nadan en la piscina"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué hacen los padres?",
                    listOf("Corren con los niños", "Juegan con el perro", "Leen el periódico"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Qué toman todos al final?",
                    listOf("Un café", "Una limonada", "Un helado de chocolate"),
                    correctIndex = 2
                )
            )
        ),

        Libro(
            id = 11,
            title = "El Viaje a Valencia",
            level = "A1", difficulty = 2, topic = "Путешествия",
            text = """
                Miguel y su amiga Laura viajan a Valencia en tren.
                El viaje desde Madrid dura casi tres horas.
                En el tren, Miguel lee un libro y Laura escucha música.
                Cuando llegan, buscan el hotel con el teléfono.
                Por la tarde, visitan la playa y el mercado central.
                Por la noche, prueban la auténtica paella valenciana en un restaurante.
                Laura dice que es el mejor viaje del año.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿En qué transporte viajan?",
                    listOf("En avión", "En tren", "En autobús"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuánto dura el viaje?",
                    listOf("Una hora", "Dos horas", "Casi tres horas"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Qué hace Miguel en el tren?",
                    listOf("Duerme", "Lee un libro", "Escucha música"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué comen por la noche?",
                    listOf("Tapas y vino", "Paella valenciana", "Bocadillos"),
                    correctIndex = 1
                )
            )
        ),

        // ── USER'S EXAMPLE #12 — Сложность 🔴⚪⚪⚪⚪ ──

        Libro(
            id = 12,
            title = "La Clase de Arte",
            level = "A1", difficulty = 1, topic = "Школа / Этика",
            text = """
                Hoy es lunes. En la clase de arte, los estudiantes dibujan.
                Pedro no tiene lápices. Su amiga Lucía tiene muchos lápices de colores.
                Lucía dice: "¿Quieres un lápiz, Pedro?"
                Ella ayuda a su amigo. Pedro está feliz.
                Es bueno compartir.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Qué día es hoy?",
                    listOf("Sábado", "Lunes", "Viernes"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué le falta a Pedro?",
                    listOf("Libros", "Lápices", "Papel"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Quién ayuda a Pedro?",
                    listOf("La profesora", "Lucía", "Nadie"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuál es el mensaje del texto?",
                    listOf("Es bueno compartir", "Es malo dibujar", "El arte es difícil"),
                    correctIndex = 0
                )
            )
        ),

        // ── USER'S EXAMPLE #13 — Сложность 🔴🔴🔴🔴⚪ ──

        Libro(
            id = 13,
            title = "El Chef Maestro",
            level = "A1", difficulty = 4, topic = "Кулинария / ТВ",
            text = """
                Bienvenidos a "El Chef Maestro".
                Hoy el desafío es difícil: ¡Cocinar una paella en 30 minutos!
                El concursante Carlos está muy nervioso.
                El aceite está caliente y el arroz espera en el plato.
                El jurado mira con mucha atención. Carlos necesita arroz, azafrán y mariscos.
                Los mariscos están frescos. El tiempo pasa muy rápido.
                ¿Es suficiente tiempo para una paella perfecta?
                ¡El reloj no para!
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Cómo se llama el programa?",
                    listOf("Cocina con Carlos", "El Chef Maestro", "Tiempo de Arroz"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuánto tiempo tienen para cocinar?",
                    listOf("Una hora", "Diez minutos", "Treinta minutos"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Cómo se siente Carlos?",
                    listOf("Muy tranquilo", "Nervioso", "Aburrido"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué ingrediente NO menciona el texto?",
                    listOf("Arroz", "Pollo", "Mariscos"),
                    correctIndex = 1
                )
            )
        ),

        // ── Сложность 🔴🔴🔴⚪⚪ (3 точки) — рассказы 14–16 ──

        Libro(
            id = 14,
            title = "En el Médico",
            level = "A1", difficulty = 3, topic = "Здоровье",
            text = """
                Pedro no se siente bien esta mañana. Le duele la cabeza y tiene fiebre.
                Su madre está preocupada y lo lleva al médico.
                En la consulta, el médico examina a Pedro con cuidado.
                Le pregunta: "¿Cuánto tiempo llevas mal?" Pedro responde: "Desde ayer."
                El médico le dice: "Tienes una infección leve. Necesitas descansar,
                beber mucha agua y tomar estas medicinas."
                Dos días después, Pedro está mucho mejor y vuelve a la escuela.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Qué síntomas tiene Pedro?",
                    listOf("Dolor de barriga y tos", "Dolor de cabeza y fiebre", "Dolor de espalda"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Quién lleva a Pedro al médico?",
                    listOf("Su padre", "Su madre", "Su hermano"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué le recomienda el médico?",
                    listOf("Operar y descansar", "Descansar, beber agua y tomar medicinas", "Solo tomar medicinas"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuándo vuelve Pedro a la escuela?",
                    listOf("Al día siguiente", "Dos días después", "Una semana después"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 15,
            title = "La Tienda de Ropa",
            level = "A1", difficulty = 3, topic = "Покупки / Мода",
            text = """
                Carla entra en una tienda de ropa con su amiga Marta.
                Busca un vestido elegante para la boda de su prima.
                La vendedora les muestra varios modelos: azul, rojo y blanco.
                A Carla le gusta mucho el vestido azul con flores pequeñas.
                Lo prueba en el probador. Le queda perfecto y está muy contenta.
                Pero el precio es alto: ciento veinte euros.
                Carla piensa un momento y decide comprarlo. Es para una ocasión especial.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Para qué ocasión busca Carla el vestido?",
                    listOf("Para una fiesta de cumpleaños", "Para la boda de su prima", "Para el trabajo"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué vestido elige Carla?",
                    listOf("El rojo con flores", "El blanco elegante", "El azul con flores"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Cuánto cuesta el vestido?",
                    listOf("Ochenta euros", "Cien euros", "Ciento veinte euros"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Qué decide hacer Carla al final?",
                    listOf("Comprarlo", "Volver otro día", "Comprar otro vestido"),
                    correctIndex = 0
                )
            )
        ),

        Libro(
            id = 16,
            title = "El Concierto",
            level = "A1", difficulty = 3, topic = "Музыка / Досуг",
            text = """
                Esta noche hay un concierto de rock en el estadio de la ciudad.
                Elena y su amiga van juntas. Esperan en la cola durante media hora.
                El concierto empieza a las nueve. El grupo toca muy bien.
                La música es fuerte y llena de energía. Elena canta todas las canciones
                porque conoce todas las letras. Su amiga baila sin parar.
                Al final, todos aplauden durante varios minutos.
                Elena dice que es el mejor concierto de su vida.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Qué tipo de concierto es?",
                    listOf("Pop", "Clásico", "Rock"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Cuánto tiempo esperan en la cola?",
                    listOf("Una hora", "Media hora", "Diez minutos"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué hace Elena durante el concierto?",
                    listOf("Solo escucha", "Canta todas las canciones", "Saca fotos"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué dice Elena al final?",
                    listOf("Que la música es demasiado fuerte", "Que es el mejor concierto de su vida", "Que prefiere el teatro"),
                    correctIndex = 1
                )
            )
        ),

        // ── Сложность 🔴🔴🔴🔴⚪ (4 точки) — рассказы 17–20 ──

        Libro(
            id = 17,
            title = "La Carta de Miguel",
            level = "A1", difficulty = 4, topic = "Общение / Дружба",
            text = """
                Miguel escribe una carta a su amigo Marco, que vive en Argentina.
                En la carta, habla de su nueva ciudad, sus estudios y sus nuevos amigos.
                También le pregunta cómo está la familia de Marco y si sigue tocando la guitarra.
                Miguel no usa el correo electrónico para las cosas importantes.
                Prefiere escribir a mano porque le parece más personal y especial.
                Dobla la carta, la pone en un sobre y escribe la dirección con cuidado.
                La carta tarda dos semanas en llegar a Buenos Aires.
                Miguel espera la respuesta de Marco con mucha ilusión.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Dónde vive el amigo Marco?",
                    listOf("En España", "En México", "En Argentina"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Por qué prefiere Miguel escribir a mano?",
                    listOf("Porque no tiene ordenador", "Porque es más personal y especial", "Porque es más rápido"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuánto tarda la carta en llegar?",
                    listOf("Una semana", "Dos semanas", "Un mes"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué pregunta Miguel sobre Marco?",
                    listOf("Si tiene trabajo nuevo", "Cómo está su familia y si toca la guitarra", "Si quiere visitar España"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 18,
            title = "El Nuevo Vecino",
            level = "A1", difficulty = 4, topic = "Социальная жизнь",
            text = """
                Un hombre joven llega al edificio con dos maletas grandes.
                Se llama Daniel y viene de Sevilla para trabajar en Madrid.
                Sus nuevos vecinos son muy simpáticos y lo reciben bien.
                La señora García, del tercero, le trae un pastel de bienvenida.
                El señor López le explica cómo funciona el ascensor y dónde está el supermercado.
                La niña del segundo piso le presenta a su gato naranja.
                Daniel está un poco nervioso pero muy contento con sus nuevos vecinos.
                Piensa que va a estar bien en su nueva ciudad.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿De dónde viene Daniel?",
                    listOf("De Barcelona", "De Valencia", "De Sevilla"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Qué le trae la señora García?",
                    listOf("Una botella de vino", "Un pastel de bienvenida", "Una planta"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué le explica el señor López?",
                    listOf("Cómo funciona el ascensor", "Las normas del edificio", "Dónde está el hospital"),
                    correctIndex = 0
                ),
                LibroQuestion(
                    "¿Cómo se siente Daniel al final?",
                    listOf("Triste y solitario", "Nervioso pero contento", "Muy enfadado"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 19,
            title = "La Cena con el Jefe",
            level = "A1", difficulty = 4, topic = "Работа / Еда",
            text = """
                Ana tiene una cena importante con su jefe esta noche.
                Van a un restaurante elegante en el centro de la ciudad.
                El restaurante está lleno de gente y la música es suave.
                El camarero les trae la carta y recomienda el menú del día.
                Ana pide pescado con verduras porque no come carne roja.
                Su jefe pide el plato especial de la casa. El vino es excelente.
                Durante la cena, hablan del nuevo proyecto de trabajo.
                Al final, el jefe paga la cuenta y le dice a Ana:
                "Eres muy importante para el equipo."
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Dónde cenan Ana y su jefe?",
                    listOf("En casa de Ana", "En un restaurante del centro", "En la oficina"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Por qué Ana pide pescado?",
                    listOf("Porque es más barato", "Porque no come carne roja", "Porque el camarero lo recomienda"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿De qué hablan durante la cena?",
                    listOf("Del tiempo libre", "Del nuevo proyecto de trabajo", "De la familia"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Quién paga la cuenta?",
                    listOf("Ana", "El jefe", "Los dos juntos"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 20,
            title = "El Partido de Fútbol",
            level = "A1", difficulty = 4, topic = "Спорт",
            text = """
                El equipo local juega contra el equipo visitante este sábado.
                Hay miles de aficionados en el estadio. El ambiente es increíble.
                El partido empieza a las cuatro de la tarde con mucha emoción.
                En el primer tiempo, los dos equipos juegan bien pero nadie marca.
                En el segundo tiempo, el delantero número nueve recibe el balón
                y chuta con fuerza. ¡Gooool! El estadio explota de alegría.
                Los aficionados saltan, cantan y abrazan a los desconocidos.
                El árbitro pita el final. El equipo local gana uno a cero.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Cuándo empieza el partido?",
                    listOf("A las dos de la tarde", "A las cuatro de la tarde", "A las seis"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué pasa en el primer tiempo?",
                    listOf("El equipo local marca dos goles", "Nadie marca ningún gol", "El equipo visitante gana"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué número lleva el delantero que marca el gol?",
                    listOf("El número siete", "El número diez", "El número nueve"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Cómo termina el partido?",
                    listOf("Empate cero a cero", "Equipo local gana uno a cero", "Equipo visitante gana"),
                    correctIndex = 1
                )
            )
        ),

        // ── Сложность 🔴🔴🔴🔴🔴 (5 точек) — рассказы 21–25 ──

        Libro(
            id = 21,
            title = "La Entrevista de Trabajo",
            level = "A1", difficulty = 5, topic = "Работа / Карьера",
            text = """
                Sofía lleva dos meses buscando trabajo. Por fin, tiene una entrevista
                importante en una empresa de tecnología en el centro de la ciudad.
                La noche anterior estudia las preguntas más comunes y prepara su currículum.
                Llega diez minutos antes de la hora. Está nerviosa pero preparada.
                La entrevistadora se llama Carmen y es muy profesional.
                Le pregunta sobre su experiencia, sus puntos fuertes y sus objetivos.
                Sofía responde con calma y confianza. Habla de sus proyectos anteriores.
                Al salir, no sabe si tiene el trabajo, pero está orgullosa de sí misma.
                Sabe que lo ha hecho lo mejor posible. Solo tiene que esperar.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Cuánto tiempo lleva Sofía buscando trabajo?",
                    listOf("Una semana", "Dos meses", "Seis meses"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuándo llega Sofía a la entrevista?",
                    listOf("Exactamente a la hora", "Diez minutos tarde", "Diez minutos antes"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Cómo responde Sofía a las preguntas?",
                    listOf("Con nerviosismo", "Con calma y confianza", "Muy rápido sin pensar"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo se siente Sofía al salir?",
                    listOf("Segura de tener el trabajo", "Orgullosa de sí misma", "Muy decepcionada"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 22,
            title = "Un Lunes Complicado",
            level = "A1", difficulty = 5, topic = "Городская жизнь / Транспорт",
            text = """
                El lunes por la mañana, Antonio descubre que el metro está en huelga.
                Sin metro, decide ir al trabajo en bicicleta por primera vez.
                Pedalea rápido por las calles. Pero en una curva difícil,
                no ve un semáforo en rojo y casi choca con un coche.
                El conductor del coche frena a tiempo. Por suerte, nadie se hace daño.
                Pero Antonio llega veinte minutos tarde a una reunión muy importante.
                Su jefe no está nada contento y le da una mirada seria.
                Por la tarde, Antonio busca en internet un carril bici más seguro.
                Decide que mañana saldrá media hora antes de casa para evitar problemas.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Por qué no puede usar el metro Antonio?",
                    listOf("Porque está cerrado por obras", "Porque está en huelga", "Porque no tiene dinero"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué casi sucede en la curva?",
                    listOf("Antonio se cae de la bicicleta", "Antonio choca con un coche", "Antonio pierde su mochila"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuántos minutos llega tarde Antonio?",
                    listOf("Diez minutos", "Treinta minutos", "Veinte minutos"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Qué decide hacer Antonio para el día siguiente?",
                    listOf("Ir en taxi", "Salir media hora antes", "Pedir el día libre"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 23,
            title = "El Tren de las Seis",
            level = "A1", difficulty = 5, topic = "Путешествия / Семья",
            text = """
                Eva siempre toma el tren de las seis del viernes para visitar a sus padres.
                Pero este viernes el tren está cancelado por obras urgentes en la vía.
                La pantalla del andén dice: "Servicio interrumpido. Disculpen las molestias."
                Eva busca alternativas rápidamente: el siguiente autobús sale dos horas después
                y llega muy tarde. El taxi es posible pero cuesta ochenta euros.
                Al final, llama a su hermano Marcos y le explica el problema.
                Marcos viene en coche sin dudar y la recoge en la estación.
                En el camino, hablan de muchas cosas: del trabajo, de los amigos, de la familia.
                Eva piensa que a veces los imprevistos tienen soluciones inesperadas y bonitas.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Cuándo toma normalmente el tren Eva?",
                    listOf("Los sábados por la mañana", "Los viernes a las seis", "Los domingos por la tarde"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Por qué está cancelado el tren?",
                    listOf("Por una huelga", "Por obras urgentes en la vía", "Por el mal tiempo"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuánto cuesta el taxi?",
                    listOf("Treinta euros", "Cincuenta euros", "Ochenta euros"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Quién ayuda a Eva al final?",
                    listOf("Un taxista", "Su hermano Marcos", "Sus padres"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 24,
            title = "La Fiesta Sorpresa",
            level = "A1", difficulty = 5, topic = "Праздники / Дружба",
            text = """
                Los amigos de Marcos planean una fiesta sorpresa para su cumpleaños.
                Cada persona tiene una tarea específica: Carmen compra la torta de tres pisos,
                Rubén decora el salón con globos y luces de colores,
                y Luis invita a más amigos sin que Marcos se entere de nada.
                El día del cumpleaños, engañan a Marcos diciéndole que van a cenar a un restaurante.
                Cuando Marcos abre la puerta del salón, todas las luces se encienden de repente.
                "¡SORPRESA!" gritan todos al mismo tiempo. Marcos no puede creerlo.
                Tiene los ojos llenos de lágrimas de alegría.
                "¡Esto es lo mejor que me ha pasado en la vida!", dice con una gran sonrisa.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Qué hace Carmen para la fiesta?",
                    listOf("Decora el salón", "Compra la torta", "Invita a más amigos"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo engañan a Marcos?",
                    listOf("Diciéndole que hay una reunión de trabajo", "Diciéndole que van a cenar a un restaurante", "Diciéndole que es un día normal"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué hace Rubén?",
                    listOf("Compra la comida", "Cocina el pastel", "Decora el salón con globos y luces"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Cómo reacciona Marcos?",
                    listOf("Se enfada con sus amigos", "Se va a casa", "Tiene lágrimas de alegría y dice que es lo mejor de su vida"),
                    correctIndex = 2
                )
            )
        ),

        Libro(
            id = 25,
            title = "Antes del Examen Final",
            level = "A1", difficulty = 5, topic = "Школа / Учёба",
            text = """
                Mañana Paula tiene el examen final de español. Esta noche estudia con intensidad.
                Repasa los verbos irregulares más importantes: ser, estar, ir, tener, hacer, poder.
                También practica los artículos, los pronombres y la conjugación de los tiempos verbales.
                A las doce de la noche, su madre entra en la habitación con una manzanilla caliente.
                "Duerme un poco, Paula. Un cerebro descansado funciona mucho mejor que uno cansado."
                Paula mira sus apuntes una vez más y cierra el libro lentamente.
                Sabe que ha estudiado todo lo posible. Confía en sí misma.
                Por la mañana, llega al examen tranquila, con el bolígrafo listo y la mente clara.
                Hará lo mejor que pueda. Es todo lo que se puede pedir.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Qué repasa Paula esta noche?",
                    listOf("Solo vocabulario de animales", "Verbos irregulares y gramática", "Las lecturas del libro de texto"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué le trae la madre a Paula?",
                    listOf("Un café con leche", "Una manzanilla caliente", "Un vaso de agua"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué consejo le da la madre?",
                    listOf("Que estudie más", "Que duerma un poco", "Que no se preocupe por el examen"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo llega Paula al examen por la mañana?",
                    listOf("Nerviosa y sin dormir", "Tranquila, con bolígrafo listo y mente clara", "Tarde y sin preparar"),
                    correctIndex = 1
                )
            )
        )

        // A2 (26–50), B1 (51–75), B2 (76–100) — добавляются в следующих сессиях
    )

    fun getById(id: Int): Libro? = all.firstOrNull { it.id == id }

    fun getByLevel(level: String): List<Libro> = all.filter { it.level == level }
}
