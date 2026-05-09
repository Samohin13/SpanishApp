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
    }

    fun byLevel(level: String): List<FlashcardSet> = all.filter { it.level == level }.sortedBy { it.order }

    fun byId(id: String): FlashcardSet? = all.firstOrNull { it.id == id }
}
