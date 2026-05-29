package com.spanishapp.ui.chat

/**
 * Расширенный словарь для подсказок + spell-check.
 *
 * ES: ~1500 топ-частотных испанских слов из корпуса CREA (Real Academia
 * Española) + спряжения распространённых глаголов.
 *
 * RU: ~1000 топ-частотных русских слов из НКРЯ (Национальный корпус).
 *
 * Все lowercase. Используется в WordSuggester (prefix) и SpellChecker
 * (Levenshtein с frequency boost).
 */
object ExpandedDictionary {

    /**
     * Объединение базовых из WordSuggester + расширения здесь = ~1700 слов.
     * Возвращает уже distinct.
     */
    val ES: List<String> by lazy { (BASE_ES + EXTRA_ES).distinct() }
    val RU: List<String> by lazy { (BASE_RU + EXTRA_RU).distinct() }

    // ── Базовые из WordSuggester (для совместимости — обновляется отдельно) ──
    private val BASE_ES = WordSuggester.allWords().filter { it.first() in 'a'..'z' || it.first() == 'ñ' }
    private val BASE_RU = WordSuggester.allWords().filter { it.first() in 'а'..'я' || it.first() == 'ё' }

    // ── ES top-1500 (топ-частотность из CREA) ──
    private val EXTRA_ES = listOf(
        // Глаголы — все формы топ-100
        "ser", "ir", "tener", "haber", "estar", "hacer", "decir", "ver", "dar",
        "saber", "querer", "llegar", "pasar", "deber", "poner", "parecer",
        "quedar", "creer", "hablar", "llevar", "dejar", "seguir", "encontrar",
        "llamar", "venir", "pensar", "salir", "volver", "tomar", "conocer",
        "vivir", "sentir", "tratar", "mirar", "contar", "empezar", "esperar",
        "buscar", "existir", "entrar", "trabajar", "escribir", "perder",
        "producir", "ocurrir", "entender", "pedir", "recibir", "recordar",
        "terminar", "permitir", "aparecer", "conseguir", "comenzar", "servir",
        "sacar", "necesitar", "mantener", "resultar", "leer", "caer", "cambiar",
        "presentar", "crear", "abrir", "considerar", "oír", "acabar", "convertir",
        "ganar", "formar", "traer", "partir", "morir", "aceptar", "realizar",
        "suponer", "comprender", "lograr", "explicar", "preguntar", "tocar",
        "reconocer", "estudiar", "alcanzar", "nacer", "dirigir", "correr",
        "utilizar", "pagar", "ayudar", "gustar", "jugar", "escuchar", "cumplir",
        "ofrecer", "descubrir", "levantar", "intentar", "usar", "decidir",
        "olvidar", "subir", "bajar", "abrir", "cerrar", "abrir", "comprar",
        "vender", "viajar", "cantar", "bailar", "dormir", "despertar", "soñar",
        "amar", "odiar", "esperar", "preferir", "elegir", "evitar", "intentar",

        // Существительные топ-200
        "cosa", "vida", "vez", "tiempo", "mundo", "casa", "hombre", "mujer",
        "niño", "niña", "parte", "lugar", "manera", "forma", "estado", "país",
        "ciudad", "pueblo", "calle", "plaza", "parque", "jardín", "edificio",
        "habitación", "cuarto", "cocina", "baño", "comedor", "salón", "puerta",
        "ventana", "mesa", "silla", "cama", "sofá", "lámpara", "espejo", "cuadro",
        "libro", "papel", "lápiz", "bolígrafo", "cuaderno", "mochila", "bolsa",
        "ropa", "camisa", "pantalón", "vestido", "falda", "chaqueta", "abrigo",
        "zapatos", "calcetines", "sombrero", "gafas", "reloj", "anillo", "collar",
        "comida", "desayuno", "almuerzo", "cena", "merienda", "pan", "leche",
        "café", "té", "agua", "jugo", "vino", "cerveza", "azúcar", "sal",
        "aceite", "vinagre", "huevo", "queso", "mantequilla", "yogur", "carne",
        "pollo", "pescado", "jamón", "salchicha", "verdura", "fruta", "manzana",
        "naranja", "plátano", "uva", "pera", "fresa", "limón", "tomate",
        "patata", "cebolla", "ajo", "zanahoria", "lechuga", "espinaca",
        "color", "rojo", "azul", "verde", "amarillo", "blanco", "negro",
        "gris", "marrón", "rosa", "morado", "naranja",
        "número", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete",
        "ocho", "nueve", "diez", "once", "doce", "trece", "catorce", "quince",
        "veinte", "treinta", "cuarenta", "cincuenta", "cien", "mil", "millón",

        // Прилагательные топ-150
        "grande", "pequeño", "alto", "bajo", "ancho", "estrecho", "largo", "corto",
        "rápido", "lento", "fuerte", "débil", "joven", "viejo", "nuevo", "antiguo",
        "moderno", "tradicional", "rico", "pobre", "bueno", "malo", "mejor",
        "peor", "bonito", "feo", "guapo", "atractivo", "elegante", "sencillo",
        "complicado", "fácil", "difícil", "posible", "imposible", "importante",
        "interesante", "aburrido", "divertido", "alegre", "triste", "feliz",
        "enfadado", "tranquilo", "nervioso", "preocupado", "cansado", "descansado",
        "enfermo", "sano", "cálido", "caliente", "frío", "templado", "fresco",
        "seco", "húmedo", "limpio", "sucio", "claro", "oscuro", "brillante",
        "opaco", "duro", "blando", "suave", "áspero", "ligero", "pesado",

        // Места + travel
        "aeropuerto", "vuelo", "avión", "tren", "estación", "metro", "autobús",
        "parada", "taxi", "coche", "moto", "bicicleta", "barco", "puerto",
        "hotel", "albergue", "habitación", "reserva", "recepción", "llave",
        "playa", "mar", "océano", "lago", "río", "montaña", "valle", "bosque",
        "desierto", "isla", "país", "frontera", "pasaporte", "visa", "maleta",
        "equipaje", "mapa", "guía", "turista", "viaje", "vacaciones", "excursión",

        // Технологии
        "ordenador", "computadora", "móvil", "teléfono", "tableta", "internet",
        "wifi", "correo", "email", "mensaje", "foto", "vídeo", "música", "canción",
        "película", "serie", "noticia", "periódico", "revista", "libro",

        // Тело
        "cabeza", "cara", "ojo", "nariz", "boca", "diente", "lengua", "oreja",
        "cuello", "hombro", "brazo", "mano", "dedo", "pecho", "espalda", "pierna",
        "rodilla", "pie", "corazón", "cerebro", "sangre", "hueso", "piel",

        // Эмоции
        "amor", "odio", "miedo", "alegría", "tristeza", "sorpresa", "enfado",
        "rabia", "calma", "paz", "esperanza", "duda", "fe", "confianza",

        // Часто употребляемые слова разные
        "ahora", "después", "antes", "siempre", "nunca", "todavía", "ya",
        "todo", "nada", "algo", "alguien", "nadie", "alguno", "ninguno",
        "mismo", "otro", "varios", "cada", "todo", "ambos",
        "aquí", "ahí", "allí", "cerca", "lejos", "dentro", "fuera", "arriba",
        "abajo", "delante", "detrás", "izquierda", "derecha",

        // Subjunctivo (важно для B2!)
        "sea", "seas", "estés", "esté", "haya", "tengas", "tenga", "pueda", "puedas",
        "quiera", "quieras", "vaya", "vayas", "sepa", "sepas", "diga", "digas",
        "venga", "vengas", "haga", "hagas", "vea", "veas", "dé", "des",
    )

    // ── RU top-1000 ──
    private val EXTRA_RU = listOf(
        // v1.25.61: добавлены частые императивы + разговорные формы
        // (юзер набирал "давай" → autocorrect менял на "диван").
        "давай", "давайте", "пошли", "пойдём", "идём", "хватит", "стой", "стоп",
        "смотри", "слушай", "скажи", "скажите", "напиши", "напишите", "спроси",
        "сделай", "сделайте", "помоги", "помогите", "подожди", "подождите",
        "погоди", "ладно", "ок", "норм", "круто", "класс", "супер", "ужас",
        "блин", "вау", "ого", "опа", "упс", "ну", "ага", "угу", "не-а",
        "пока-пока", "ща", "щас", "чё", "чо", "тут", "там", "здесь", "вот",
        "наверное", "возможно", "точно", "вряд ли", "разумеется", "конечно же",
        // Глаголы — самые частые
        "быть", "мочь", "сказать", "говорить", "знать", "стать", "есть", "хотеть",
        "видеть", "идти", "думать", "делать", "понимать", "жить", "смотреть",
        "ждать", "взять", "брать", "найти", "искать", "стоять", "сидеть",
        "лежать", "бежать", "ехать", "ходить", "ездить", "плыть", "лететь",
        "получать", "давать", "брать", "иметь", "владеть", "купить", "продать",
        "платить", "тратить", "зарабатывать", "копить", "учиться", "учить",
        "узнавать", "помнить", "забыть", "вспомнить", "представить", "решить",
        "выбрать", "согласиться", "ответить", "спросить", "позвонить", "написать",
        "прочитать", "посмотреть", "послушать", "услышать", "увидеть", "встретить",
        "проводить", "уйти", "приехать", "приходить", "уезжать", "вернуться",
        "оставаться", "остановиться", "продолжить", "закончить", "начать",
        "открыть", "закрыть", "включить", "выключить", "поставить", "положить",
        "взять", "отдать", "принести", "увезти", "помочь", "поддержать",

        // Существительные
        "человек", "люди", "ребёнок", "дети", "мужчина", "женщина", "парень",
        "девушка", "друг", "подруга", "семья", "родители", "отец", "мать",
        "брат", "сестра", "сын", "дочь", "муж", "жена", "бабушка", "дедушка",
        "тетя", "дядя", "племянник", "племянница", "внук", "внучка",
        "дом", "квартира", "комната", "кухня", "ванная", "туалет", "коридор",
        "балкон", "окно", "дверь", "стена", "пол", "потолок", "лестница",
        "стол", "стул", "кресло", "диван", "кровать", "шкаф", "полка", "лампа",
        "телевизор", "холодильник", "плита", "микроволновка", "стиральная",
        "город", "село", "деревня", "посёлок", "улица", "переулок", "проспект",
        "площадь", "парк", "сад", "лес", "поле", "река", "озеро", "море", "океан",
        "гора", "холм", "берег", "пляж",
        "работа", "профессия", "должность", "коллега", "начальник", "сотрудник",
        "офис", "фабрика", "магазин", "банк", "школа", "университет", "больница",
        "поликлиника", "аптека", "ресторан", "кафе", "бар", "гостиница", "отель",
        "вокзал", "аэропорт", "остановка", "станция", "метро",
        "еда", "пища", "завтрак", "обед", "ужин", "перекус", "хлеб", "масло",
        "молоко", "сыр", "мясо", "рыба", "курица", "колбаса", "сосиска", "яйцо",
        "крупа", "рис", "макароны", "картошка", "капуста", "морковь", "лук",
        "чеснок", "помидор", "огурец", "перец", "салат", "суп", "борщ", "пюре",
        "пирог", "торт", "печенье", "конфета", "шоколад", "мороженое",
        "напиток", "вода", "чай", "кофе", "сок", "молоко", "пиво", "вино",
        "одежда", "куртка", "пальто", "плащ", "костюм", "рубашка", "футболка",
        "свитер", "брюки", "джинсы", "юбка", "платье", "шорты", "носки",
        "перчатки", "шапка", "шарф", "обувь", "ботинки", "сапоги", "кроссовки",
        "туфли", "сандалии", "тапочки",
        "время", "час", "минута", "секунда", "день", "ночь", "утро", "вечер",
        "неделя", "месяц", "год", "век",
        "цвет", "красный", "оранжевый", "жёлтый", "зелёный", "голубой", "синий",
        "фиолетовый", "белый", "чёрный", "серый", "коричневый", "розовый",

        // Числа
        "один", "два", "три", "четыре", "пять", "шесть", "семь", "восемь",
        "девять", "десять", "одиннадцать", "двенадцать", "двадцать", "тридцать",
        "сорок", "пятьдесят", "сто", "тысяча", "миллион",

        // Прилагательные часто
        "хороший", "плохой", "большой", "маленький", "высокий", "низкий", "длинный",
        "короткий", "широкий", "узкий", "толстый", "тонкий", "тяжёлый", "лёгкий",
        "быстрый", "медленный", "сильный", "слабый", "молодой", "старый", "новый",
        "красивый", "уродливый", "умный", "глупый", "добрый", "злой", "весёлый",
        "грустный", "счастливый", "несчастный", "здоровый", "больной",
        "богатый", "бедный", "дорогой", "дешёвый", "горячий", "холодный", "тёплый",
        "сухой", "мокрый", "чистый", "грязный", "светлый", "тёмный", "яркий",

        // Местоимения, союзы, частицы
        "тогда", "если", "когда", "пока", "потому", "поэтому", "однако", "хотя",
        "потому что", "так как", "ведь", "вдруг", "вообще", "впрочем", "конечно",
        "может", "может быть", "наверное", "точно", "обязательно", "ничего",
        "пожалуйста", "извини", "извините", "благодарю",
    )
}
