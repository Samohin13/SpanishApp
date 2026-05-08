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
        ),

        // ══════════════════════════════════════════════
        //  БЛОК A2 — Elemental (рассказы 26–50)
        // ══════════════════════════════════════════════

        // ── Сложность 🔴⚪⚪⚪⚪ (1 точка) — рассказы 26–30 ──

        Libro(
            id = 26,
            title = "El Primer Día de Trabajo",
            level = "A2", difficulty = 1, topic = "Работа / Офис",
            text = """
                Hoy es el primer día de trabajo de Carlos. Llegó temprano a la oficina.
                Su jefa se llama Elena. Ella le mostró su escritorio y su ordenador.
                Carlos conoció a sus compañeros. Todos fueron muy simpáticos con él.
                A las dos comieron juntos en el restaurante de la empresa.
                Carlos estaba un poco nervioso, pero al final del día se sintió bien.
                «Creo que me va a gustar este trabajo», pensó.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Cómo se llama la jefa de Carlos?",
                    listOf("María", "Elena", "Laura"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo fueron los compañeros con Carlos?",
                    listOf("Antipáticos", "Indiferentes", "Muy simpáticos"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Dónde comieron a las dos?",
                    listOf("En casa de Carlos", "En un bar del barrio", "En el restaurante de la empresa"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Cómo se sintió Carlos al final del día?",
                    listOf("Muy cansado y triste", "Bien y con ganas de volver", "Decepcionado con el trabajo"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 27,
            title = "De Compras en el Mercado",
            level = "A2", difficulty = 1, topic = "Покупки / Рынок",
            text = """
                El sábado por la mañana Marta fue al mercado del barrio.
                Compró tomates, cebollas y dos kilos de naranjas.
                También quiso comprar queso, pero era demasiado caro.
                El vendedor le recomendó un queso más barato que también estaba bueno.
                Marta lo probó y le gustó mucho. Compró medio kilo.
                Gastó quince euros en total y volvió a casa muy contenta.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Cuándo fue Marta al mercado?",
                    listOf("El viernes por la tarde", "El sábado por la mañana", "El domingo al mediodía"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Por qué Marta no compró el primer queso?",
                    listOf("Era demasiado caro", "No le gustó el sabor", "Ya tenía queso en casa"),
                    correctIndex = 0
                ),
                LibroQuestion(
                    "¿Cuánto queso compró Marta al final?",
                    listOf("Un kilo", "Dos kilos", "Medio kilo"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Cuánto dinero gastó Marta?",
                    listOf("Diez euros", "Quince euros", "Veinte euros"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 28,
            title = "Una Tarde en el Parque",
            level = "A2", difficulty = 1, topic = "Свободное время",
            text = """
                Ayer por la tarde Daniel fue al parque con su hijo pequeño, Tomás.
                Tomás quería subir a los columpios. Luego corrió por el césped verde.
                Daniel se sentó en un banco y leyó su libro favorito.
                Después los dos comieron un helado de chocolate.
                Cuando llegó la noche, volvieron a casa cansados pero felices.
                A Tomás le encantó la tarde en el parque.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Con quién fue Daniel al parque?",
                    listOf("Con su esposa", "Con su hijo Tomás", "Con sus amigos"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué hizo Daniel mientras Tomás jugaba?",
                    listOf("Durmió en el banco", "Habló por teléfono", "Leyó su libro"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Qué helado comieron?",
                    listOf("De fresa", "De vainilla", "De chocolate"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Cómo volvieron a casa?",
                    listOf("En autobús, muy aburridos", "Cansados pero felices", "Enfadados por la lluvia"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 29,
            title = "La Llamada Perdida",
            level = "A2", difficulty = 1, topic = "Технологии / Общение",
            text = """
                Sofía miró su móvil y vio tres llamadas perdidas de su amiga Paula.
                Intentó llamarla, pero el número estaba ocupado.
                Le mandó un mensaje de texto: «Hola, ¿todo bien? Llámame cuando puedas.»
                Paula respondió rápido: «¡Sí! Tengo una noticia increíble. Te cuento luego.»
                Sofía se quedó muy curiosa. Esperó dos horas hasta que Paula la llamó.
                La noticia era que Paula había conseguido trabajo en París.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Cuántas llamadas perdidas tenía Sofía?",
                    listOf("Una", "Dos", "Tres"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Por qué no pudo hablar con Paula inmediatamente?",
                    listOf("Paula no tenía móvil", "El número estaba ocupado", "Sofía no tenía batería"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuánto tiempo esperó Sofía?",
                    listOf("Diez minutos", "Una hora", "Dos horas"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Cuál era la noticia de Paula?",
                    listOf("Se casaba en verano", "Había conseguido trabajo en París", "Iba a tener un hijo"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 30,
            title = "El Vuelo con Retraso",
            level = "A2", difficulty = 1, topic = "Путешествия / Аэропорт",
            text = """
                Javier llegó al aeropuerto a las seis de la mañana.
                Su vuelo a Barcelona salía a las ocho. Pasó el control de seguridad sin problemas.
                En la puerta de embarque le dijeron que el vuelo tenía dos horas de retraso.
                Se sentó en una cafetería y pidió un café con leche y un croissant.
                Leyó el periódico y escuchó música. El tiempo pasó rápido.
                Por fin embarcó y llegó a Barcelona a mediodía, un poco tarde pero tranquilo.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿A qué hora llegó Javier al aeropuerto?",
                    listOf("A las cinco", "A las seis", "A las siete"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuánto retraso tenía el vuelo?",
                    listOf("Una hora", "Dos horas", "Tres horas"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué pidió Javier en la cafetería?",
                    listOf("Un zumo y una tostada", "Un café con leche y un croissant", "Un bocadillo y agua"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo llegó Javier a Barcelona?",
                    listOf("Muy estresado y enfadado", "Un poco tarde pero tranquilo", "Sin equipaje y nervioso"),
                    correctIndex = 1
                )
            )
        ),

        // ── Сложность 🔴🔴⚪⚪⚪ (2 точки) — рассказы 31–35 ──

        Libro(
            id = 31,
            title = "El Apartamento Nuevo",
            level = "A2", difficulty = 2, topic = "Жильё / Переезд",
            text = """
                Laura y su novio Miguel acaban de alquilar un apartamento en el centro de la ciudad.
                El piso tiene dos habitaciones, una cocina moderna y un balcón con vistas al parque.
                El alquiler cuesta novecientos euros al mes, pero entre los dos es asequible.
                El primer fin de semana limpiaron todo y montaron los muebles que compraron en una tienda de segunda mano.
                La sala quedó muy acogedora con el sofá nuevo y las plantas que trajo Laura.
                Sus amigos vinieron a cenar el sábado y les encantó el apartamento.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Dónde está el apartamento?",
                    listOf("En las afueras de la ciudad", "En el centro de la ciudad", "Cerca del aeropuerto"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuánto cuesta el alquiler?",
                    listOf("Setecientos euros", "Ochocientos euros", "Novecientos euros"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Dónde compraron los muebles?",
                    listOf("En una tienda de lujo", "En una tienda de segunda mano", "Los recibieron de regalo"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué hicieron el sábado?",
                    listOf("Fueron a un restaurante", "Recibieron a sus amigos a cenar", "Viajaron a otra ciudad"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 32,
            title = "Aprendiendo a Cocinar",
            level = "A2", difficulty = 2, topic = "Кулинария",
            text = """
                Roberto siempre comía en restaurantes porque no sabía cocinar.
                Un día decidió aprender y se apuntó a un curso de cocina los jueves por la noche.
                El primer día aprendió a hacer una tortilla española. Le salió bastante bien.
                La semana siguiente preparó una sopa de verduras con ajo y pimentón.
                Su compañera de piso, Ana, la probó y dijo que estaba deliciosa.
                Ahora Roberto cocina tres veces por semana y ha ahorrado mucho dinero.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Por qué Roberto siempre comía en restaurantes?",
                    listOf("Le encantaba salir a comer", "No sabía cocinar", "Su cocina estaba rota"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué aprendió a hacer el primer día del curso?",
                    listOf("Una paella valenciana", "Una sopa de verduras", "Una tortilla española"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Qué dijo Ana sobre la sopa?",
                    listOf("Que estaba demasiado salada", "Que estaba deliciosa", "Que necesitaba más ajo"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuál es el resultado positivo del curso para Roberto?",
                    listOf("Tiene más amigos ahora", "Ha ahorrado mucho dinero", "Le ofrecieron trabajo en un restaurante"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 33,
            title = "El Partido de Fútbol",
            level = "A2", difficulty = 2, topic = "Спорт / Досуг",
            text = """
                El domingo había un partido importante entre el Real Madrid y el Barcelona.
                Diego y sus amigos se reunieron en casa de Pablo para verlo.
                Pablo preparó bocadillos y refrescos para todos.
                El partido estuvo muy emocionante. En el minuto ochenta y cinco el Madrid marcó el gol del empate.
                Al final el partido terminó uno a uno. Diego estaba un poco decepcionado porque quería que ganara el Madrid.
                Sin embargo, todos disfrutaron mucho de la tarde juntos.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Dónde vieron el partido?",
                    listOf("En un bar del barrio", "En el estadio", "En casa de Pablo"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿En qué minuto marcó el Madrid?",
                    listOf("En el minuto setenta", "En el minuto ochenta y cinco", "En el minuto noventa"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo terminó el partido?",
                    listOf("Dos a cero para el Barcelona", "Uno a uno", "Tres a dos para el Madrid"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo se sintió Diego con el resultado?",
                    listOf("Muy contento", "Un poco decepcionado", "Completamente indiferente"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 34,
            title = "El Médico y el Catarro",
            level = "A2", difficulty = 2, topic = "Здоровье",
            text = """
                El lunes Inés se levantó con dolor de garganta y fiebre de treinta y ocho grados.
                Llamó a su trabajo para decir que no podía ir y pidió cita con el médico.
                La doctora la examinó y le dijo que tenía un catarro fuerte.
                Le recetó un antiinflamatorio y le recomendó descansar, beber mucho líquido y no salir de casa.
                Inés compró los medicamentos en la farmacia y pasó dos días en cama viendo series.
                El miércoles ya se encontraba mucho mejor y el jueves volvió al trabajo.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Qué síntomas tenía Inés el lunes?",
                    listOf("Dolor de cabeza y tos", "Dolor de garganta y fiebre", "Dolor de estómago y mareos"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué diagnóstico le dio la doctora?",
                    listOf("Una alergia primaveral", "Una infección de oído", "Un catarro fuerte"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Qué hizo Inés durante los dos días en cama?",
                    listOf("Leyó novelas", "Vio series", "Durmió todo el tiempo"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuándo volvió Inés al trabajo?",
                    listOf("El martes", "El miércoles", "El jueves"),
                    correctIndex = 2
                )
            )
        ),

        Libro(
            id = 35,
            title = "El Restaurante Equivocado",
            level = "A2", difficulty = 2, topic = "Рестораны / Ситуации",
            text = """
                Ana y Carlos quedaron para cenar en el restaurante «La Bodeguita» a las nueve.
                Carlos llegó puntual, pero no vio a Ana. Esperó veinte minutos y la llamó.
                Ana estaba en «La Bodeguita», pero en la calle Mayor, no en la calle del Sol.
                Había dos restaurantes con el mismo nombre en la ciudad.
                Carlos caminó diez minutos hasta el otro restaurante y al fin se encontraron.
                Se rieron mucho del malentendido y disfrutaron de una cena estupenda.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿A qué hora quedaron Ana y Carlos?",
                    listOf("A las ocho", "A las nueve", "A las diez"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuál fue el problema?",
                    listOf("Ana se olvidó de la cena", "Había dos restaurantes con el mismo nombre", "El restaurante estaba cerrado"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Dónde estaba el restaurante de Ana?",
                    listOf("En la calle del Sol", "En la calle Mayor", "En la plaza Central"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo reaccionaron al encontrarse?",
                    listOf("Estaban enfadados", "Se rieron del malentendido", "Decidieron irse a casa"),
                    correctIndex = 1
                )
            )
        ),

        // ── Сложность 🔴🔴🔴⚪⚪ (3 точки) — рассказы 36–40 ──

        Libro(
            id = 36,
            title = "El Intercambio de Idiomas",
            level = "A2", difficulty = 3, topic = "Языки / Учёба",
            text = """
                Lucía estudia inglés en la universidad y quiere mejorar su conversación.
                Encontró a un chico inglés llamado James que estudia español y busca un intercambio.
                Quedaron una vez por semana en una cafetería del centro.
                Cada sesión tenía dos partes: media hora en español y media hora en inglés.
                Al principio los dos se equivocaban mucho, pero con el tiempo mejoraron bastante.
                Después de tres meses, Lucía ya podía hablar inglés con mucha más fluidez.
                James, por su parte, aprendió muchas expresiones coloquiales del español.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Qué busca James en el intercambio?",
                    listOf("Mejorar su francés", "Practicar su español", "Aprender a cocinar"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Con qué frecuencia quedaban?",
                    listOf("Todos los días", "Una vez por semana", "Dos veces al mes"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo estaba organizada cada sesión?",
                    listOf("Solo en inglés durante una hora", "Media hora en español y media en inglés", "Una hora en un idioma según el día"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué aprendió James principalmente?",
                    listOf("Gramática avanzada", "Muchas expresiones coloquiales", "El acento de Madrid"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 37,
            title = "La Entrevista de Trabajo",
            level = "A2", difficulty = 3, topic = "Работа / Карьера",
            text = """
                Marcos recibió un correo electrónico con una invitación a una entrevista de trabajo.
                La empresa fabricaba software y buscaba un diseñador gráfico con experiencia.
                Marcos preparó su portfolio y practicó las respuestas a posibles preguntas.
                El día de la entrevista se puso su mejor camisa y llegó diez minutos antes.
                La entrevistadora le preguntó sobre sus proyectos anteriores y sus puntos fuertes.
                Marcos respondió con calma y confianza. Al salir, se sentía muy optimista.
                Tres días después, la empresa lo llamó para ofrecerle el puesto.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Para qué puesto era la entrevista?",
                    listOf("Programador web", "Diseñador gráfico", "Director de marketing"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué preparó Marcos antes de la entrevista?",
                    listOf("Una carta de presentación", "Su portfolio y respuestas", "Un contrato de trabajo"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuándo llegó Marcos a la entrevista?",
                    listOf("Exactamente a la hora", "Diez minutos antes", "Un poco tarde"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuál fue el resultado?",
                    listOf("Le dijeron que no era suficientemente bueno", "Lo llamaron para ofrecerle el puesto", "Tuvo que hacer una segunda entrevista"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 38,
            title = "El Viaje a Sevilla",
            level = "A2", difficulty = 3, topic = "Путешествия / Испания",
            text = """
                Durante las vacaciones de primavera, Carmen y su hermana viajaron a Sevilla en tren.
                El viaje duró dos horas y media desde Madrid. Llegaron al hotel a mediodía.
                Por la tarde visitaron la Giralda y el barrio de Santa Cruz con sus calles estrechas y flores.
                Al día siguiente fueron a ver la Plaza de España y comieron tapas en un bar típico.
                Probaron el gazpacho, las croquetas y el jamón ibérico. Todo estaba riquísimo.
                En el tren de vuelta Carmen dijo: «Sevilla es una ciudad preciosa. Hay que volver.»
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Cómo viajaron Carmen y su hermana a Sevilla?",
                    listOf("En avión", "En coche", "En tren"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Qué visitaron la primera tarde?",
                    listOf("La Plaza de España y el Alcázar", "La Giralda y el barrio de Santa Cruz", "El Parque de María Luisa"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué comida probaron en el bar típico?",
                    listOf("Paella y sangría", "Gazpacho, croquetas y jamón ibérico", "Tortilla y ensalada mixta"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué dijo Carmen en el tren de vuelta?",
                    listOf("Que estaba cansada del viaje", "Que Sevilla era preciosa y había que volver", "Que prefería Madrid"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 39,
            title = "La Bicicleta Nueva",
            level = "A2", difficulty = 3, topic = "Транспорт / Город",
            text = """
                Pablo quería dejar de ir al trabajo en coche y empezar a usar la bicicleta.
                La ciudad había construido nuevos carriles bici por todo el centro.
                Fue a una tienda de bicicletas y pidió consejo al dependiente.
                Después de probar tres modelos diferentes, eligió una bicicleta de ciudad con cambios.
                Costó doscientos ochenta euros, más un casco y una cadena antirrobo.
                La primera semana fue un poco difícil por las cuestas, pero Pablo se acostumbró rápido.
                Ahora llega al trabajo en veinte minutos, hace ejercicio y gasta menos en gasolina.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Por qué quería Pablo usar la bicicleta?",
                    listOf("Porque no tenía coche", "Para dejar de ir en coche al trabajo", "Porque el transporte público era muy caro"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuántos modelos probó antes de elegir?",
                    listOf("Dos", "Tres", "Cuatro"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué compró además de la bicicleta?",
                    listOf("Un impermeable y un mapa", "Un casco y una cadena antirrobo", "Guantes y una mochila"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuáles son los beneficios para Pablo?",
                    listOf("Solo ahorra tiempo en el camino", "Llega antes, hace ejercicio y gasta menos en gasolina", "Tiene más tiempo libre por las tardes"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 40,
            title = "Una Noche de Lluvia",
            level = "A2", difficulty = 3, topic = "Погода / Рассказ",
            text = """
                Era una noche de noviembre y llovía mucho en la ciudad.
                Elena volvía a casa después de una cena con sus amigas.
                No llevaba paraguas y el autobús no llegaba. Estaba empapada y tenía frío.
                Un taxi se paró a su lado. El taxista le preguntó adónde iba.
                Elena le dijo su dirección y subió al coche aliviada.
                Durante el trayecto el taxista le habló del partido de fútbol de esa noche.
                Al llegar a casa Elena le dio una propina generosa y entró corriendo a darse una ducha caliente.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Por qué estaba mojada Elena?",
                    listOf("Había cruzado un río", "No llevaba paraguas y llovía mucho", "Se había caído en un charco"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Por qué no cogió el autobús?",
                    listOf("El autobús no llegaba", "No tenía dinero para el billete", "La parada estaba muy lejos"),
                    correctIndex = 0
                ),
                LibroQuestion(
                    "¿De qué habló el taxista durante el trayecto?",
                    listOf("Del tiempo y la lluvia", "Del partido de fútbol de esa noche", "De su familia y sus viajes"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué hizo Elena nada más llegar a casa?",
                    listOf("Llamó a sus amigas", "Se acostó directamente", "Entró corriendo a darse una ducha"),
                    correctIndex = 2
                )
            )
        ),

        // ── Сложность 🔴🔴🔴🔴⚪ (4 точки) — рассказы 41–45 ──

        Libro(
            id = 41,
            title = "El Vecino Misterioso",
            level = "A2", difficulty = 4, topic = "Соседи / Рассказ",
            text = """
                Desde hacía un mes, Rosa notaba cosas extrañas en su edificio.
                El vecino del quinto piso llegaba siempre a las tres de la madrugada con una maleta grande.
                Nunca saludaba a nadie y tenía las persianas siempre bajadas.
                Rosa le contó sus sospechas a su marido, que se reía y le decía que no imaginara cosas.
                Un día en el ascensor Rosa le preguntó directamente al vecino cómo se llamaba.
                Él respondió con una sonrisa: «Me llamo Andrés. Soy músico de jazz y toco en un club nocturno.»
                Rosa se sintió un poco tonta, pero también se alegró de que todo tuviera una explicación sencilla.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Qué le parecía extraño a Rosa sobre el vecino?",
                    listOf("Llegaba muy tarde con una maleta y tenía las persianas bajadas", "Hacía ruido por las noches", "Nunca pagaba los gastos del edificio"),
                    correctIndex = 0
                ),
                LibroQuestion(
                    "¿Cómo reaccionaba el marido de Rosa?",
                    listOf("Compartía sus sospechas", "Se reía y le decía que no imaginara cosas", "Fue a hablar con el vecino"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo se enteró Rosa de la verdad?",
                    listOf("Lo buscó en internet", "Le preguntó directamente en el ascensor", "El vecino le escribió una nota"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿A qué se dedica Andrés?",
                    listOf("Es actor de teatro", "Es músico de jazz", "Es cocinero nocturno"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 42,
            title = "El Examen de Conducir",
            level = "A2", difficulty = 4, topic = "Транспорт / Жизненные ситуации",
            text = """
                Después de dos años de autoescuela, Natalia por fin se presentó al examen práctico de conducir.
                Había suspendido el teórico dos veces, pero lo aprobó a la tercera.
                El día del examen estaba nerviosa. El examinador se sentó a su lado con una tableta.
                Natalia arrancó el coche sin problemas. Hizo bien el aparcamiento en paralelo y respetó todas las señales.
                Pero al final de la prueba se pasó un semáforo en ámbar y el examinador lo anotó.
                Al terminar, él dijo: «Ha cometido una infracción leve, pero en general ha conducido bien. Aprobada.»
                Natalia llamó a su madre llorando de alegría.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Cuántas veces suspendió Natalia el examen teórico?",
                    listOf("Una vez", "Dos veces", "Tres veces"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué hizo bien Natalia durante el examen?",
                    listOf("Solo el aparcamiento en paralelo", "El aparcamiento y respetar señales", "Toda la prueba sin errores"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuál fue su error?",
                    listOf("Chocó contra un bordillo", "Se pasó un semáforo en ámbar", "Frenó de golpe en la autopista"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuál fue el resultado del examen?",
                    listOf("Suspendida por la infracción", "Aprobada a pesar de la infracción leve", "Tendría que repetir solo esa maniobra"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 43,
            title = "El Festival de Música",
            level = "A2", difficulty = 4, topic = "Музыка / Культура",
            text = """
                El verano pasado Hugo y sus amigos fueron al festival de música más grande de España.
                Compraron las entradas dos meses antes porque se agotaban muy rápido.
                El festival duró tres días. Había escenarios con rock, electrónica y flamenco.
                El primer día vieron a su grupo favorito y cantaron todas las canciones.
                Dormían en tiendas de campaña en un área de acampada cerca del recinto.
                El calor era intenso, pero el ambiente era increíble.
                Hugo dijo que fue la mejor experiencia de su vida y que ya quería volver al año siguiente.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Con cuánta antelación compraron las entradas?",
                    listOf("Una semana antes", "Un mes antes", "Dos meses antes"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿Qué géneros musicales había en el festival?",
                    listOf("Pop, reggaeton y hip-hop", "Rock, electrónica y flamenco", "Jazz, blues y soul"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Dónde dormían Hugo y sus amigos?",
                    listOf("En un hotel cercano", "En tiendas de campaña en el área de acampada", "En la furgoneta de un amigo"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo resumió Hugo la experiencia?",
                    listOf("Dijo que había sido demasiado caro y cansado", "Dijo que fue la mejor experiencia de su vida", "Dijo que prefería los conciertos pequeños"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 44,
            title = "El Piso de Estudiantes",
            level = "A2", difficulty = 4, topic = "Студенческая жизнь",
            text = """
                Valentina llegó a Madrid para estudiar periodismo y alquiló una habitación en un piso compartido.
                Vivía con tres estudiantes más: uno de Alemania, uno de México y una chica de Valencia.
                Al principio había pequeños conflictos sobre la limpieza y la música a altas horas.
                Hicieron una lista de normas: cada uno limpiaba la cocina una vez por semana y el silencio era obligatorio después de medianoche.
                Con el tiempo se hicieron muy buenos amigos. Cocinaban juntos los domingos y se ayudaban con los estudios.
                Valentina dice que esa época fue muy difícil al principio pero increíblemente enriquecedora.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Qué estudia Valentina?",
                    listOf("Arquitectura", "Periodismo", "Medicina"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuáles eran los conflictos al principio?",
                    listOf("El dinero del alquiler y los gastos", "La limpieza y la música a altas horas", "Los horarios del baño y la nevera"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué decía la norma sobre el silencio?",
                    listOf("Silencio a partir de las once", "Silencio obligatorio después de medianoche", "No había norma sobre el ruido"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué hacían los domingos?",
                    listOf("Iban al mercado juntos", "Cocinaban juntos y se ayudaban con los estudios", "Cada uno salía por su cuenta"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 45,
            title = "La Receta de la Abuela",
            level = "A2", difficulty = 4, topic = "Семья / Кулинария",
            text = """
                Cada verano, la familia de Antonio se reunía en el pueblo para hacer la receta de la abuela: arroz con leche.
                La abuela Carmen usaba solo ingredientes naturales: arroz de grano corto, leche entera, azúcar, limón y canela.
                El proceso tardaba casi dos horas a fuego muy lento, removiendo constantemente.
                Antonio aprendió a hacerlo con ella a los doce años.
                Cuando la abuela murió, Antonio se convirtió en el guardián de la receta.
                Cada Navidad la prepara para toda la familia. Nadie puede creer que sabe igual que la de la abuela.
                «El secreto», dice Antonio, «es la paciencia y el amor que le pones.»
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Cuál es el plato tradicional de la familia?",
                    listOf("Paella valenciana", "Arroz con leche", "Cocido madrileño"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuánto tiempo tardaba el proceso de cocción?",
                    listOf("Unos treinta minutos", "Casi una hora", "Casi dos horas"),
                    correctIndex = 2
                ),
                LibroQuestion(
                    "¿A qué edad aprendió Antonio la receta?",
                    listOf("A los diez años", "A los doce años", "A los quince años"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuál dice Antonio que es el secreto de la receta?",
                    listOf("Los ingredientes de alta calidad", "La paciencia y el amor que le pones", "El tipo de cacerola que se usa"),
                    correctIndex = 1
                )
            )
        ),

        // ── Сложность 🔴🔴🔴🔴🔴 (5 точек) — рассказы 46–50 ──

        Libro(
            id = 46,
            title = "El Emprendedor",
            level = "A2", difficulty = 5, topic = "Бизнес / Мечты",
            text = """
                Después de diez años trabajando para una empresa multinacional, Alejandro decidió crear su propio negocio.
                Siempre había soñado con una tienda de café de especialidad donde también se vendieran libros de segunda mano.
                Ahorró durante dos años y buscó un local en el barrio más bohemio de la ciudad.
                El primer mes fue durísimo: pocos clientes, muchos gastos y dudas constantes.
                Sin embargo, una reseña positiva de una cuenta popular de Instagram cambió todo.
                En tres semanas el local se llenaba todos los días y Alejandro tuvo que contratar a dos personas más.
                Hoy, dos años después, tiene tres sucursales y está escribiendo un libro sobre emprendimiento.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Cuál era el sueño de Alejandro?",
                    listOf("Una librería de textos académicos", "Una tienda de café de especialidad con libros de segunda mano", "Un restaurante de comida internacional"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué cambió la situación del negocio?",
                    listOf("Una entrevista en televisión", "Una reseña positiva en Instagram", "Una reducción de los precios"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuánto tiempo ahorró Alejandro antes de abrir?",
                    listOf("Un año", "Dos años", "Cinco años"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿En qué situación está Alejandro dos años después de abrir?",
                    listOf("Cerró el negocio por las deudas", "Tiene tres sucursales y escribe un libro", "Volvió a su trabajo anterior"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 47,
            title = "El Voluntario",
            level = "A2", difficulty = 5, topic = "Волонтёрство / Общество",
            text = """
                Cuando Irene se jubiló a los sesenta y cinco años, decidió que quería hacer algo útil con su tiempo libre.
                Se apuntó como voluntaria en una asociación que enseñaba español a inmigrantes recién llegados.
                Los primeros días fueron un desafío: los alumnos hablaban distintos idiomas y tenían niveles muy diferentes.
                Irene preparaba materiales personalizados para cada grupo y pasaba horas buscando recursos online.
                Poco a poco los estudiantes progresaron. Uno de ellos, Ahmed, aprendió tan rápido que consiguió trabajo en tres meses.
                Irene recibió una carta de agradecimiento del director de la asociación.
                «Nunca pensé», dijo ella, «que aprender podría dar tanto como enseñar.»
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Qué hacía Irene como voluntaria?",
                    listOf("Repartía comida en un banco de alimentos", "Enseñaba español a inmigrantes", "Cuidaba a personas mayores"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuál era la dificultad principal al principio?",
                    listOf("No había materiales disponibles", "Los alumnos hablaban distintos idiomas y tenían niveles diferentes", "La asociación no le pagaba suficiente"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué logró Ahmed gracias a su aprendizaje?",
                    listOf("Conseguir trabajo en tres meses", "Empezar una empresa propia", "Aprender un tercer idioma"),
                    correctIndex = 0
                ),
                LibroQuestion(
                    "¿Qué reflexión hace Irene al final?",
                    listOf("Que enseñar era demasiado difícil para ella", "Que aprender podría dar tanto como enseñar", "Que prefería trabajar con niños"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 48,
            title = "La Carta al Futuro",
            level = "A2", difficulty = 5, topic = "Рефлексия / Время",
            text = """
                En su último año de instituto, la profesora de lengua pidió a los alumnos que escribieran una carta para ellos mismos, que se abriría diez años después.
                Claudia escribió sobre sus sueños: quería ser veterinaria, vivir cerca del mar y tener un perro grande.
                Diez años más tarde, Claudia trabajaba como veterinaria en una clínica de Valencia, vivía a quinientos metros del mar y tenía un labrador llamado Nemo.
                Cuando abrió el sobre y leyó la carta, se echó a reír y a llorar a la vez.
                No todo había sido fácil: los estudios fueron muy duros y hubo momentos en que quiso rendirse.
                Pero al leer sus propias palabras de diecisiete años, sintió una profunda gratitud por no haber abandonado.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Qué pidió la profesora que hicieran los alumnos?",
                    listOf("Un diario de toda la semana", "Una carta para ellos mismos que se abriría en diez años", "Una redacción sobre sus recuerdos de la infancia"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuáles eran los sueños de Claudia en el instituto?",
                    listOf("Ser médica, vivir en Madrid y tener un gato", "Ser veterinaria, vivir cerca del mar y tener un perro grande", "Ser bióloga, viajar por el mundo y aprender idiomas"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo reaccionó Claudia al leer la carta diez años después?",
                    listOf("Se sorprendió porque no recordaba haberla escrito", "Se echó a reír y a llorar a la vez", "La leyó con indiferencia porque ya no le importaban esas cosas"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué sintió Claudia al releer sus palabras de los diecisiete años?",
                    listOf("Vergüenza de lo ingenua que había sido", "Una profunda gratitud por no haber abandonado", "Tristeza porque no había conseguido todos sus sueños"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 49,
            title = "El Puente Roto",
            level = "A2", difficulty = 5, topic = "Дружба / Примирение",
            text = """
                Rodrigo y su mejor amigo Sergio no se hablaban desde hacía ocho meses.
                Todo empezó por una tontería: una discusión sobre dinero que debía Rodrigo por un viaje compartido.
                Rodrigo pensaba que ya lo había devuelto; Sergio decía que no.
                Sus amigos comunes intentaron mediar, pero los dos eran muy orgullosos.
                Un día Rodrigo encontró un mensaje antiguo en el que efectivamente decía que pagaría más tarde.
                En lugar de ponerse a la defensiva, le mandó el mensaje a Sergio con un sencillo texto: «Tenías razón. Lo siento.»
                Sergio respondió en diez minutos: «Yo también lo siento. ¿Tomamos algo esta semana?»
                Esa misma tarde estaban sentados en su bar de siempre, como si nada hubiera pasado.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Por qué Rodrigo y Sergio no se hablaban?",
                    listOf("Rodrigo salió con la exnovia de Sergio", "Una discusión sobre dinero de un viaje compartido", "Un malentendido sobre un trabajo en común"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué encontró Rodrigo que cambió la situación?",
                    listOf("El recibo del banco con la transferencia", "Un mensaje antiguo donde decía que pagaría más tarde", "Una nota que Sergio le había dejado hace meses"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo reaccionó Rodrigo al descubrir que Sergio tenía razón?",
                    listOf("Se enfadó aún más", "Le mandó el mensaje con una disculpa sencilla", "Pidió a un amigo que lo explicara"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuánto tardó Sergio en responder?",
                    listOf("Tres días", "Una hora", "Diez minutos"),
                    correctIndex = 2
                )
            )
        ),

        Libro(
            id = 50,
            title = "La Promesa del Maratón",
            level = "A2", difficulty = 5, topic = "Спорт / Личный рост",
            text = """
                Después de un chequeo médico, el doctor le dijo a Beatriz que necesitaba hacer más ejercicio.
                Beatriz nunca había sido deportista, pero decidió inscribirse en un maratón popular de su ciudad que se celebraría en seis meses.
                Empezó a entrenar tres veces por semana: al principio apenas podía correr diez minutos seguidos.
                Siguió un plan de entrenamiento progresivo que encontró en internet y poco a poco fue mejorando.
                Tuvo momentos de duda, sobre todo cuando se lesionó el tobillo en el cuarto mes y tuvo que parar dos semanas.
                El día del maratón, Beatriz cruzó la meta en cuatro horas y cuarenta y tres minutos, llorando y sonriendo al mismo tiempo.
                «Lo más importante no era el tiempo», le dijo a su familia, «sino demostrarme que podía.»
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Por qué decidió Beatriz correr un maratón?",
                    listOf("Un amigo la retó a hacerlo", "El doctor le dijo que necesitaba más ejercicio", "Siempre había querido ser corredora profesional"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué problema tuvo en el cuarto mes?",
                    listOf("Se cansó del entrenamiento y quiso rendirse", "Se lesionó el tobillo y tuvo que parar dos semanas", "Enfermó de gripe y no pudo entrenar"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿En cuánto tiempo terminó Beatriz el maratón?",
                    listOf("En tres horas y veinte minutos", "En cuatro horas y cuarenta y tres minutos", "En cinco horas y diez minutos"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuál era para Beatriz lo más importante del maratón?",
                    listOf("Terminar antes que los demás participantes", "Demostrarse a sí misma que podía", "Bajar de las cinco horas"),
                    correctIndex = 1
                )
            )
        )

        // ══════════════════════════════════════════════
        //  БЛОК B1 — Intermedio (рассказы 51–60)
        // ══════════════════════════════════════════════

        Libro(
            id = 51,
            title = "El Cambio de Trabajo",
            level = "B1", difficulty = 1, topic = "Работа / Решения",
            text = """
                Tras diez años en la misma empresa, Elena se sentía estancada profesionalmente.
                Cada lunes, cuando entraba en la oficina, sentía que no aprendía nada nuevo desde hacía años.
                Aunque el sueldo era cómodo y sus compañeros eran agradables, algo dentro de ella le decía que era el momento de cambiar.
                Empezó a actualizar su currículum por las noches y a hacer entrevistas en otras empresas durante los descansos para almorzar.
                Después de tres meses de búsqueda, recibió una oferta interesante en una startup tecnológica con menos sueldo pero más posibilidades de crecer.
                Su madre le dijo que estaba loca, pero su pareja la apoyó.
                Hoy, dos años después, Elena dirige un equipo de quince personas y no se arrepiente de haber dado el salto.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Por qué Elena decidió cambiar de trabajo?",
                    listOf("Porque su sueldo era bajo", "Porque se sentía estancada profesionalmente", "Porque no se llevaba bien con sus compañeros"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuándo actualizaba su currículum?",
                    listOf("Durante el horario de trabajo", "Por las noches", "Los fines de semana"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo reaccionó su madre?",
                    listOf("La apoyó completamente", "Le dijo que estaba loca", "No quiso opinar"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuál es la situación de Elena ahora?",
                    listOf("Quiere volver al trabajo anterior", "Dirige un equipo de quince personas", "Está buscando trabajo otra vez"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 52,
            title = "El Reencuentro",
            level = "B1", difficulty = 1, topic = "Дружба / Воспоминания",
            text = """
                Veinte años después de terminar el colegio, Carlos recibió una invitación al reencuentro de su clase.
                Al principio dudó si ir: había perdido el contacto con casi todos y no sabía si tendría algo en común con ellos.
                Finalmente decidió asistir, más por curiosidad que por entusiasmo.
                Cuando llegó al restaurante donde se celebraba la cena, apenas reconoció a algunos compañeros: el tiempo había cambiado mucho a todos.
                Hablando con su antiguo mejor amigo Pablo, descubrió que ahora vivía en la misma calle que él, a solo tres edificios de distancia.
                «¿Cómo es posible que llevemos cinco años de vecinos sin saberlo?», se rieron los dos.
                Esa noche Carlos se dio cuenta de que algunas amistades, aunque parezcan perdidas, simplemente esperan el momento adecuado para volver.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Por qué dudaba Carlos en ir al reencuentro?",
                    listOf("Estaba muy ocupado con el trabajo", "Había perdido el contacto con casi todos", "No le caía bien su antigua clase"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué descubrió Carlos al hablar con Pablo?",
                    listOf("Que vivía en la misma calle a tres edificios", "Que se había mudado a otro país", "Que se había casado con una compañera de clase"),
                    correctIndex = 0
                ),
                LibroQuestion(
                    "¿Cuánto tiempo llevaban siendo vecinos sin saberlo?",
                    listOf("Dos años", "Cinco años", "Diez años"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué moraleja saca Carlos al final?",
                    listOf("Que es mejor no asistir a reuniones del pasado", "Que algunas amistades esperan el momento adecuado para volver", "Que el tiempo cambia demasiado a las personas"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 53,
            title = "El Viaje en Tren",
            level = "B1", difficulty = 2, topic = "Путешествия / Знакомства",
            text = """
                Aquel viaje en tren de Madrid a Barcelona iba a ser largo, pero Sofía estaba contenta de tener tiempo para leer.
                A los pocos minutos de salir, una mujer mayor se sentó frente a ella y empezó a hablar sin parar.
                Sofía intentó concentrarse en su libro, pero la mujer tenía historias fascinantes: había trabajado como traductora en la ONU durante treinta años.
                Sin darse cuenta, Sofía cerró su libro y empezó a hacer preguntas.
                La mujer le contó cómo había aprendido cinco idiomas, los lugares donde había vivido y los líderes mundiales que había conocido.
                Cuando el tren llegó a Barcelona, Sofía sintió que había vivido un curso de historia contemporánea en tres horas.
                «Hay viajes que no son por la distancia que recorres, sino por la gente que conoces», escribió esa misma noche en su diario.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Qué planeaba hacer Sofía durante el viaje?",
                    listOf("Trabajar en su ordenador", "Leer su libro", "Dormir todo el camino"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿En qué había trabajado la mujer mayor?",
                    listOf("Como profesora de idiomas", "Como traductora en la ONU", "Como diplomática en Europa"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuántos idiomas hablaba la mujer?",
                    listOf("Tres", "Cinco", "Siete"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué reflexión escribió Sofía en su diario?",
                    listOf("Que prefiere viajar sola y leer", "Que algunos viajes son por la gente que conoces, no la distancia", "Que la próxima vez tomará un avión"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 54,
            title = "La Receta de la Abuela",
            level = "B1", difficulty = 2, topic = "Семья / Кухня",
            text = """
                Después de la muerte de su abuela, Andrés heredó una caja con cartas, fotos y, lo más importante, un cuaderno con recetas escritas a mano.
                La paella de su abuela era legendaria en la familia y nadie había logrado reproducirla nunca, ni siquiera su madre.
                Andrés decidió intentarlo. Compró todos los ingredientes que indicaba la receta y siguió las instrucciones al pie de la letra.
                El resultado fue decepcionante: el arroz quedó demasiado seco y faltaba algo del sabor que él recordaba.
                Llamó a su tía Lola para pedirle consejo. Ella se rió por teléfono y le dijo: «Hijo, las recetas de tu abuela siempre tenían un ingrediente secreto que nunca escribía: probaba el plato a media cocción y ajustaba a ojo».
                Andrés volvió a intentarlo varias veces. A la cuarta paella, su madre lo probó y por primera vez en años, lloró de emoción.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Qué heredó Andrés de su abuela?",
                    listOf("La casa familiar", "Una caja con cartas, fotos y un cuaderno de recetas", "Su colección de joyas"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Por qué no salía bien la paella la primera vez?",
                    listOf("Faltaba un ingrediente clave", "El arroz estaba seco y faltaba sabor", "Usó el sartén equivocado"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuál era el secreto de la abuela según la tía Lola?",
                    listOf("Usar mariscos especiales", "Probar el plato a media cocción y ajustar a ojo", "Cocinar siempre en domingo"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué hizo finalmente la madre de Andrés?",
                    listOf("Lo abrazó y le dio un consejo", "Lloró de emoción al probar la paella", "Le pidió que no cocinara más"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 55,
            title = "La Mudanza",
            level = "B1", difficulty = 2, topic = "Жизнь / Перемены",
            text = """
                Cuando le ofrecieron un ascenso que implicaba mudarse de Sevilla a Bilbao, Jorge tuvo dos semanas para decidir.
                Sevilla era su ciudad: el sol, los amigos, el bar de toda la vida donde lo conocían por su nombre.
                Bilbao significaba lluvia, frío y empezar de cero a los treinta y cinco años.
                Habló con su pareja, que aceptó acompañarle, y eso le dio el empujón final.
                La primera semana en Bilbao fue difícil: la lluvia constante le deprimía y la comida le parecía demasiado pesada.
                Pero un sábado fue al casco viejo, comió pintxos en una taberna llena de gente y, por primera vez, sintió que podría llamar a Bilbao «su nueva casa».
                Tres años después, cuando la empresa le ofreció volver a Sevilla, Jorge dijo que no: ya no era el mismo y Bilbao tampoco lo era.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Cuánto tiempo tuvo Jorge para decidir?",
                    listOf("Dos semanas", "Un mes", "Tres días"),
                    correctIndex = 0
                ),
                LibroQuestion(
                    "¿Qué le ayudó a tomar la decisión?",
                    listOf("Que su madre lo apoyó", "Que su pareja aceptó acompañarle", "Que le ofrecieron mucho dinero"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuándo empezó a sentirse en casa en Bilbao?",
                    listOf("El primer día", "Un sábado en el casco viejo comiendo pintxos", "Después de seis meses"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué hizo cuando le ofrecieron volver a Sevilla?",
                    listOf("Aceptó inmediatamente", "Dijo que no", "Pidió más tiempo para pensar"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 56,
            title = "El Idioma Olvidado",
            level = "B1", difficulty = 3, topic = "Язык / Идентичность",
            text = """
                Marta había nacido en España de padres polacos pero nunca había aprendido polaco bien: solo entendía algunas palabras del cocinar de su madre.
                Cuando murió su abuela en Cracovia y Marta viajó al funeral, se sintió completamente extranjera entre su propia familia.
                Sus primos hablaban polaco con velocidad y ella solo podía sonreír y asentir.
                A su regreso a Madrid, Marta se inscribió en clases de polaco para principiantes, aunque tenía treinta y dos años.
                El primer año fue frustrante: la gramática del polaco era brutal y a veces se preguntaba por qué se torturaba.
                Después de tres años de estudio, Marta volvió a Polonia para Navidad y mantuvo su primera conversación completa en polaco con su tío.
                Él le dijo, con lágrimas en los ojos: «Tu abuela estaría tan orgullosa».
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿De dónde son los padres de Marta?",
                    listOf("De Polonia", "De Rusia", "De República Checa"),
                    correctIndex = 0
                ),
                LibroQuestion(
                    "¿Cuándo decidió aprender polaco?",
                    listOf("De pequeña, en el colegio", "Después del funeral de su abuela", "Cuando se mudó a Cracovia"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo describe ella el primer año de estudio?",
                    listOf("Fácil y divertido", "Frustrante por la gramática brutal", "Aburrido pero útil"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué le dijo su tío en Cracovia?",
                    listOf("Que hablaba mejor que sus primos", "Que su abuela estaría orgullosa", "Que debería volver a vivir en Polonia"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 57,
            title = "El Pequeño Negocio",
            level = "B1", difficulty = 3, topic = "Предпринимательство",
            text = """
                Después de quedarse sin trabajo durante la pandemia, Roberto abrió una pequeña panadería artesanal en su barrio.
                Las primeras semanas casi no entraba nadie y Roberto comía pan duro a diario, pensando en cerrar.
                Una vecina jubilada empezó a venir cada mañana y a recomendar la panadería a sus amigas del grupo de gimnasia.
                Poco a poco, el boca a boca hizo su trabajo y la cola por las mañanas empezó a salir hasta la calle.
                Roberto contrató a una joven estudiante para los fines de semana y empezó a hacer talleres de pan los sábados por la tarde.
                Tres años después, Roberto ya tenía dos panaderías y rechazaba ofertas de cadenas grandes que querían comprar su marca.
                «Mi marca soy yo y mi barrio», explicaba a quien le preguntaba por qué no aceptaba.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Por qué Roberto abrió la panadería?",
                    listOf("Era su sueño desde niño", "Se quedó sin trabajo durante la pandemia", "Heredó el local"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Quién empezó a recomendar el negocio?",
                    listOf("Una vecina jubilada", "Un crítico gastronómico", "Su antigua jefa"),
                    correctIndex = 0
                ),
                LibroQuestion(
                    "¿Cuántas panaderías tiene tres años después?",
                    listOf("Una", "Dos", "Cinco"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Por qué rechaza las ofertas de las cadenas grandes?",
                    listOf("Porque ofrecen poco dinero", "Porque su marca es él y su barrio", "Porque quiere jubilarse pronto"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 58,
            title = "La Carta Encontrada",
            level = "B1", difficulty = 4, topic = "Семейная история",
            text = """
                Mientras vaciaba el desván de la casa de su padre, recientemente fallecido, Lucía encontró un sobre amarillento entre las páginas de un libro viejo.
                Era una carta escrita en 1962, dirigida a su madre y firmada con un nombre que ella no reconocía: Tomás.
                La carta hablaba de un amor imposible y terminaba con la frase: «Si no me esperas, lo entenderé. Pero no te olvidaré jamás».
                Lucía siempre había creído que sus padres se habían enamorado en el colegio y nunca habían amado a nadie más.
                Decidió no decir nada a su madre, ya con ochenta y cinco años, para no abrir heridas viejas.
                Pero la carta cambió la forma en que veía a su madre: ya no la imaginaba solo como ama de casa, sino como una joven mujer que había tomado una decisión difícil hacía sesenta años.
                Guardó la carta en su propio cajón y a veces la leía, intentando entender mejor a la mujer que la había criado.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Dónde encontró Lucía la carta?",
                    listOf("En un cajón de la cocina", "Entre las páginas de un libro viejo", "En el bolsillo de un abrigo"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿De qué año era la carta?",
                    listOf("De 1952", "De 1962", "De 1972"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Por qué Lucía no le dijo nada a su madre?",
                    listOf("Estaba enfadada con ella", "Para no abrir heridas viejas", "No supo cómo hacerlo"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo cambió la carta su forma de ver a su madre?",
                    listOf("Ya no la respetaba igual", "La imaginaba como una joven que tomó una decisión difícil", "Decidió alejarse de ella"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 59,
            title = "El Voluntariado",
            level = "B1", difficulty = 4, topic = "Социальная жизнь",
            text = """
                Cuando se jubiló a los sesenta y cinco años, Antonio se sintió perdido: cuarenta años trabajando como profesor habían terminado de un día para otro.
                Sus hijos vivían fuera, su esposa todavía trabajaba y él pasaba las mañanas mirando la televisión sin saber qué hacer.
                Una amiga le recomendó que se ofreciera como voluntario en una asociación que enseñaba a leer a adultos sin estudios.
                Al principio se resistió: «Soy demasiado mayor para empezar algo así», decía.
                Pero la primera clase con cinco hombres de unos cuarenta años, todos avergonzados de no saber leer, le rompió el corazón y le devolvió un sentido a su tiempo.
                Dos años después, Antonio dedica veinte horas semanales al voluntariado y dice que estos son los años más útiles de su vida.
                «Cada vez que un alumno lee una palabra entera por primera vez, vuelvo a sentir lo que me hizo ser profesor», explica.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Por qué se sintió perdido Antonio al jubilarse?",
                    listOf("No tenía amigos cerca", "Cuarenta años de trabajo terminaron de golpe", "Su esposa se había ido"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Quién le sugirió el voluntariado?",
                    listOf("Su esposa", "Una amiga", "Su antiguo jefe"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿A quién enseña en la asociación?",
                    listOf("A niños con dificultades", "A adultos sin estudios", "A inmigrantes recién llegados"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuántas horas dedica ahora al voluntariado?",
                    listOf("Cinco horas", "Diez horas", "Veinte horas"),
                    correctIndex = 2
                )
            )
        ),

        Libro(
            id = 60,
            title = "El Concierto Inesperado",
            level = "B1", difficulty = 5, topic = "Музыка / Дружба",
            text = """
                Manuel y Diego habían tocado juntos en una banda de rock cuando tenían veinte años, pero la vida los había llevado por caminos diferentes.
                Treinta años después, Diego, ya enfermo de cáncer, le mandó a Manuel un mensaje pidiéndole un favor: tocar juntos una vez más antes de que fuera demasiado tarde.
                Manuel hacía dos décadas que no cogía la guitarra, pero aceptó sin dudar.
                Pasaron tres semanas ensayando en el salón de la casa de Diego, recordando canciones que creían haber olvidado.
                El concierto se organizó en un pequeño bar del barrio, con cincuenta personas que conocían su historia: amigos, familiares, antiguos compañeros del colegio.
                Diego apenas podía cantar al final por la enfermedad, pero los ojos le brillaban como cuando tenían veinte años.
                Manuel guardó la grabación de aquella noche en una caja especial y, después de la muerte de Diego seis meses después, no pudo escucharla durante mucho tiempo.
                Ahora, cuando la pone, sonríe en lugar de llorar.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Cuántos años llevaba Manuel sin tocar la guitarra?",
                    listOf("Una década", "Dos décadas", "Tres décadas"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Dónde tocaron el concierto?",
                    listOf("En un teatro grande", "En un pequeño bar del barrio", "En la casa de Diego"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Por qué Diego apenas podía cantar?",
                    listOf("Estaba muy nervioso", "Estaba enfermo de cáncer", "Había perdido la voz por el ensayo"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué hace Manuel ahora cuando escucha la grabación?",
                    listOf("Sonríe en lugar de llorar", "No puede escucharla todavía", "La comparte en redes sociales"),
                    correctIndex = 0
                )
            )
        ),

        // ══════════════════════════════════════════════
        //  БЛОК B2 — Avanzado (рассказы 61–65)
        // ══════════════════════════════════════════════

        Libro(
            id = 61,
            title = "El Algoritmo de la Soledad",
            level = "B2", difficulty = 1, topic = "Технологии / Общество",
            text = """
                Cuando Natalia cumplió cuarenta años, decidió hacer un experimento: pasaría un mes entero sin redes sociales para entender cómo había cambiado su vida desde que se había hecho dependiente de ellas.
                Los primeros días fueron desconcertantes; sentía un impulso constante de coger el móvil cada pocos minutos, aunque no había nada que mirar.
                A medida que pasaban las semanas, empezó a darse cuenta de que durante años había estado consumiendo vidas ajenas mientras la suya pasaba en silencio frente a una pantalla.
                Recuperó hábitos que había abandonado: leer novelas, llamar a amigos por teléfono, salir a caminar sin documentar el paseo en historias de Instagram.
                Cuando, al cabo de un mes, volvió a abrir las aplicaciones, le sorprendió la cantidad de notificaciones acumuladas y, sobre todo, lo poco que importaba ya nada de aquello.
                Natalia decidió no volver a publicar en redes y, aunque sus seguidores fueron disminuyendo, ella sintió que recuperaba algo más valioso: el control sobre su propia atención.
                «No hemos perdido el contacto unos con otros», escribió en su diario. «Hemos perdido el contacto con nosotros mismos».
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿En qué consistía el experimento de Natalia?",
                    listOf("Pasar un mes sin redes sociales", "Vivir un mes sin internet", "Estar un mes sin móvil"),
                    correctIndex = 0
                ),
                LibroQuestion(
                    "¿Qué descubrió durante ese mes?",
                    listOf("Que extrañaba mucho a sus contactos", "Que había estado consumiendo vidas ajenas mientras la suya pasaba en silencio", "Que sus amigos no la echaban de menos"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué hizo después del experimento?",
                    listOf("Volvió a publicar como antes", "Decidió no volver a publicar en redes", "Borró todas las aplicaciones para siempre"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuál es la conclusión de Natalia en su diario?",
                    listOf("Hemos perdido el contacto con nosotros mismos", "Las redes son útiles si se usan con moderación", "Es imposible vivir sin tecnología hoy"),
                    correctIndex = 0
                )
            )
        ),

        Libro(
            id = 62,
            title = "La Tierra que Ya No Era",
            level = "B2", difficulty = 2, topic = "Эмиграция / Идентичность",
            text = """
                Después de veintidós años viviendo en Madrid, Julián volvió a Buenos Aires para el funeral de su padre.
                Había salido de Argentina huyendo de la crisis económica, dejando atrás un país que para él significaba imposibilidad.
                Cuando aterrizó, esperaba reconocer las calles de su infancia, pero la ciudad había cambiado tanto que se sintió extranjero en su propio barrio.
                El edificio donde había crecido tenía un código de seguridad que él no conocía, los kioscos donde compraba caramelos eran ahora cafeterías de moda, y sus antiguos amigos del colegio hablaban con un acento que ya no sonaba al suyo.
                Tras dos semanas, Julián entendió que la nostalgia que había sentido durante años no era por una Buenos Aires real, sino por una Buenos Aires de su memoria que ya no existía y probablemente nunca había existido del todo.
                Volvió a Madrid con una sensación extraña: no se sentía completamente español, pero ya no podía decir que era argentino tampoco.
                Era algo nuevo, sin nombre, que solo entienden los que han vivido entre dos países durante demasiado tiempo.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Por qué había salido Julián de Argentina?",
                    listOf("Por trabajo", "Huyendo de la crisis económica", "Por amor"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo se sintió al volver a su barrio?",
                    listOf("En casa, como si no se hubiera ido", "Extranjero en su propio barrio", "Profundamente feliz"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué entiende Julián sobre su nostalgia?",
                    listOf("Que era una pérdida real de su patria", "Que era por una Buenos Aires que ya no existía y quizás nunca existió del todo", "Que era una emoción inventada"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo describe su identidad al final?",
                    listOf("Completamente española", "Completamente argentina", "Algo nuevo, sin nombre, propio de quien vive entre dos países"),
                    correctIndex = 2
                )
            )
        ),

        Libro(
            id = 63,
            title = "El Libro que lo Cambió Todo",
            level = "B2", difficulty = 3, topic = "Литература / Личностный рост",
            text = """
                Existen libros que se leen y se olvidan, libros que entretienen unas horas y luego quedan en una estantería como simple decoración.
                Pero hay otros, mucho menos frecuentes, que llegan a la vida de uno en el momento exacto y lo dejan diferente para siempre.
                Para Inés, ese libro fue una novela rusa del siglo diecinueve que le regaló un profesor cuando tenía dieciocho años y atravesaba una crisis existencial sin saberlo.
                Lo leyó en tres semanas, subrayando frases hasta el punto de que el ejemplar se volvió ilegible, y al terminarlo se dio cuenta de que ya no podía continuar estudiando la carrera que había elegido por presión familiar.
                Cambió a Filosofía a los pocos meses, una decisión que sus padres tardaron años en aceptar.
                Veinte años después, Inés es profesora universitaria y conserva aquel libro destrozado en su mesilla, no por sentimentalismo sino como recordatorio de que las palabras adecuadas en el momento adecuado pueden alterar el rumbo de una existencia más que cualquier consejo, terapia o conversación profunda.
                A veces, cuando algún alumno suyo se siente perdido, Inés le presta el libro sin decir mucho.
                Algunos lo devuelven impasibles; otros, transformados.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Cuándo leyó Inés el libro que cambió su vida?",
                    listOf("A los veintiocho años, recién casada", "A los dieciocho, en una crisis existencial sin saberlo", "A los treinta, antes de empezar a enseñar"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué decisión tomó después de leerlo?",
                    listOf("Empezar a escribir su propia novela", "Cambiar de carrera a Filosofía", "Mudarse a otro país"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo reaccionaron sus padres?",
                    listOf("Inmediatamente la apoyaron", "Tardaron años en aceptar la decisión", "Nunca se lo perdonaron"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué hace Inés ahora con el libro?",
                    listOf("Lo guarda en el sótano", "Lo presta a alumnos perdidos sin decir mucho", "Lo regaló hace años"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 64,
            title = "El Peso de la Elección",
            level = "B2", difficulty = 4, topic = "Психология / Современная жизнь",
            text = """
                En el supermercado del barrio había, cuando él era niño, tres tipos de aceite y dos marcas de pan.
                Hoy, frente a la estantería del aceite, Ricardo cuenta veintinueve variedades distintas y siente, no la libertad que se supone que da la abundancia, sino una especie de parálisis silenciosa que le cuesta describir.
                Lleva años leyendo sobre la llamada «paradoja de la elección»: la idea de que más opciones, lejos de hacernos más felices, nos generan ansiedad y arrepentimiento porque siempre podemos imaginar que la opción no elegida habría sido mejor.
                En su trabajo como diseñador de productos, Ricardo intenta aplicar esta idea reduciendo intencionadamente las variantes que ofrece a sus clientes.
                Sus jefes lo critican: «La gente quiere elegir», le dicen.
                Pero cuando lanza una nueva línea con tres opciones en lugar de quince, las ventas suben un treinta por ciento y los clientes reportan estar más satisfechos.
                Ricardo no pretende haber descubierto nada nuevo, simplemente recuerda en silencio una verdad que parecemos haber olvidado: que demasiada libertad de elección puede pesar tanto como la falta de ella.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Qué siente Ricardo frente a las veintinueve variedades de aceite?",
                    listOf("Felicidad por la abundancia", "Una parálisis silenciosa difícil de describir", "Curiosidad por probar todas"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿En qué consiste la «paradoja de la elección»?",
                    listOf("En que más opciones siempre nos hacen más felices", "En que más opciones generan ansiedad y arrepentimiento", "En que las marcas eligen por nosotros"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué pasó cuando lanzó tres opciones en lugar de quince?",
                    listOf("Las ventas bajaron drásticamente", "Las ventas subieron un treinta por ciento y los clientes estaban más satisfechos", "No hubo ningún cambio"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cuál es la verdad que parecemos haber olvidado, según el texto?",
                    listOf("Que la abundancia es siempre buena", "Que demasiada libertad de elección puede pesar tanto como la falta de ella", "Que los productos artesanales son mejores"),
                    correctIndex = 1
                )
            )
        ),

        Libro(
            id = 65,
            title = "Memoria de un Verano que No Recuerdo",
            level = "B2", difficulty = 5, topic = "Память / Фотография",
            text = """
                Mi madre conserva en un álbum una fotografía de un verano de mi infancia que yo, por más que lo intento, no logro recordar.
                En la imagen aparezco con seis o siete años, sonriendo en la orilla del mar, sosteniendo un pez pequeño que, según ella, había pescado yo solo después de horas de paciencia.
                Cuando me cuenta esa historia, lo hace con tanto detalle que durante un tiempo creí recordarla yo también: el sabor del bocadillo de aquella mañana, la sensación del sol quemándome los hombros, el orgullo al ver el pez moverse en el cubo.
                Pero un día comprendí que mi memoria de aquel día no era mi memoria, sino la memoria de mi madre prestada a través de la repetición.
                Aquello me dejó pensando durante semanas: cuántos de mis recuerdos más vívidos son realmente míos y cuántos son construcciones colectivas, fotografías miradas mil veces hasta volverse experiencia, anécdotas familiares contadas tantas veces que terminamos creyendo haberlas vivido.
                Quizá la identidad sea menos un archivo personal que un préstamo permanente que hacemos con la gente que nos rodea, y la memoria, lejos de ser una cámara, sea más bien un colectivo de voces que se confunden con la nuestra hasta que ya no sabemos cuál era la original.
                Cierro el álbum con cierta inquietud, pero también con una extraña gratitud: tal vez no recuerde aquel verano, pero gracias a mi madre ese día existe en alguna parte, aunque no sea exactamente dentro de mí.
            """.trimIndent(),
            questions = listOf(
                LibroQuestion(
                    "¿Por qué el narrador no recuerda aquel verano por sí mismo?",
                    listOf("Porque era demasiado pequeño y olvidó todo", "Porque su memoria del día es la memoria prestada de su madre", "Porque su madre le inventó la historia"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Qué reflexión le provoca esa fotografía?",
                    listOf("Que las fotos nunca son fiables", "Que muchos recuerdos podrían ser construcciones colectivas más que personales", "Que quiere ser fotógrafo"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Cómo describe la identidad al final del texto?",
                    listOf("Como un archivo personal completo", "Como un préstamo permanente con la gente que nos rodea", "Como un secreto guardado"),
                    correctIndex = 1
                ),
                LibroQuestion(
                    "¿Con qué sentimiento cierra el álbum?",
                    listOf("Con pura tristeza", "Con inquietud y también extraña gratitud", "Con indiferencia total"),
                    correctIndex = 1
                )
            )
        )
    )

    fun getById(id: Int): Libro? = all.firstOrNull { it.id == id }

    fun getByLevel(level: String): List<Libro> = all.filter { it.level == level }
}
