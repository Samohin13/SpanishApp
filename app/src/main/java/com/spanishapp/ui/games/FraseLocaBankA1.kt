package com.spanishapp.ui.games

/**
 * Frase Loca — банк A1 (уровни 1–25, темы 1–5).
 * Диалект-канон: Испания (Мадрид). Пунктуация приклеена к словам.
 * Ловушки на уровнях 1–10 не активируются (см. trapLimitForLevel),
 * но авторски заданы — включаются при перепрохождении окном выше.
 */
internal object FraseLocaBankA1 {

    private val SALUDOS = FraseTheme(
        id = "saludos",
        title = "Приветствие и знакомство",
        cefr = "A1",
        phrases = listOf(
            flp("Доброе утро!", "¡Buenos días!"),
            flp("Очень приятно!", "¡Mucho gusto!"),
            flp("Привет! Как дела?", "¡Hola! ¿Qué tal?"),
            flp("Где ты живёшь?", "¿Dónde vives?",
                flt("vive", "«Ты» — окончание -es: vives. Vive — это «он/она»")),
            flp("Меня зовут Ана.", "Me llamo Ana.",
                flt("llamas", "«Меня зовут» = me llamo. Llamas — форма «ты»")),
            flp("Как тебя зовут?", "¿Cómo te llamas?",
                flt("llamo", "«Тебя зовут» = te llamas. Llamo — форма «я»")),
            flp("Я из России.", "Soy de Rusia.",
                flt("Estoy", "Происхождение — всегда SER: soy de Rusia")),
            flp("Пока! До завтра!", "¡Adiós! ¡Hasta mañana!",
                flt("manana!", "Не теряй ñ: mañana. Manana — не слово")),
            flp("Ты говоришь по-испански?", "¿Hablas español?",
                flt("Hablo", "Вопрос к «ты» — hablas. Hablo — «я говорю»")),
            flp("Добрый день, сеньор Лопес.", "Buenas tardes, señor López.",
                flt("Buenos", "Tardes — женский род: buenAs tardes")),
            flp("Я немного говорю по-испански.", "Hablo un poco de español.",
                flt("pequeño", "«Немного» = un poco. Pequeño — маленький по размеру")),
            flp("Я живу в Мадриде с моей семьёй.", "Vivo en Madrid con mi familia.",
                flt("a", "Живу В городе = en Madrid, не a")),
        )
    )

    private val FAMILIA = FraseTheme(
        id = "familia",
        title = "Семья и дом",
        cefr = "A1",
        phrases = listOf(
            flp("Это моя мама.", "Esta es mi madre.",
                flt("Este", "Madre — женский род: estA")),
            flp("У меня два брата.", "Tengo dos hermanos.",
                flt("Tiene", "«У меня» = tengo. Tiene — «у него/неё»")),
            flp("Мой дом большой.", "Mi casa es grande.",
                flt("está", "Размер — постоянное качество: SER")),
            flp("У нас есть собака.", "Tenemos un perro.",
                flt("una", "Perro — мужской род: un perro")),
            flp("Моя сестра высокая.", "Mi hermana es alta.",
                flt("alto", "Hermana — женский род: altA")),
            flp("Бабушка живёт с нами.", "La abuela vive con nosotros.",
                flt("El", "Abuela — женский род: LA abuela")),
            flp("Мои родители много работают.", "Mis padres trabajan mucho.",
                flt("trabaja", "Padres — множественное число: trabajaN")),
            flp("В моей комнате есть окно.", "En mi habitación hay una ventana.",
                flt("es", "«Есть, имеется» = HAY, не es")),
            flp("У моего брата трое детей.", "Mi hermano tiene tres hijos.",
                flt("tengo", "«У брата» — он: tiene. Tengo — «у меня»")),
            flp("Кухня маленькая, но светлая.", "La cocina es pequeña, pero luminosa.",
                flt("pequeño,", "Cocina — женский род: pequeñA")),
            flp("Ванная рядом с кухней.", "El baño está al lado de la cocina.",
                flt("es", "Расположение — всегда ESTAR: está al lado")),
            flp("Это дом моих бабушки и дедушки.", "Esta es la casa de mis abuelos.",
                flt("sus", "«Моих» = mis. Sus — «его/её/их»")),
        )
    )

    private val COMIDA = FraseTheme(
        id = "comida",
        title = "Еда и кафе",
        cefr = "A1",
        phrases = listOf(
            flp("Счёт, пожалуйста.", "La cuenta, por favor.",
                flt("El", "Cuenta — женский род: LA cuenta")),
            flp("Очень вкусно!", "¡Está muy rico!",
                flt("Es", "Вкус этого блюда сейчас — ESTAR: está rico")),
            flp("Я хочу кофе с молоком.", "Quiero un café con leche.",
                flt("una", "Café — мужской род: un café")),
            flp("Мне нравится паэлья.", "Me gusta la paella.",
                flt("gusto", "Gustar работает «наоборот»: me gustA la paella")),
            flp("Хлеб стоит один евро.", "El pan cuesta un euro.",
                flt("cuestan", "El pan — единственное число: cuestA")),
            flp("Мне нравятся эти тапас.", "Me gustan estas tapas.",
                flt("gusta", "Tapas — множественное: me gustaN")),
            flp("Что ты будешь пить?", "¿Qué vas a beber?",
                flt("bebes?", "После vas a — инфинитив: beber")),
            flp("Вода холодная.", "El agua está fría.",
                flt("La", "Agua — ж.р., но артикль EL (ударная а-): el agua fría")),
            flp("Я завтракаю дома в восемь.", "Desayuno en casa a las ocho.",
                flt("por", "Время «во сколько» = a las ocho")),
            flp("Официант, меню, пожалуйста.", "Camarero, la carta, por favor.",
                flt("menú,", "Список блюд в Испании = la carta. El menú — комплексный обед")),
            flp("Я вегетарианец, я не ем мясо.", "Soy vegetariano, no como carne.",
                flt("Estoy", "Кто ты по сути — SER: soy vegetariano")),
            flp("В Испании ужинают поздно.", "En España se cena tarde.",
                flt("cenan", "Безличное «ужинают» = se cena")),
        )
    )

    private val CIUDAD = FraseTheme(
        id = "ciudad",
        title = "Город и транспорт",
        cefr = "A1",
        phrases = listOf(
            flp("Где метро?", "¿Dónde está el metro?",
                flt("es", "Местоположение — всегда ESTAR: está"),
                flt("la", "Metro — мужской род: el metro")),
            flp("Банк далеко отсюда?", "¿El banco está lejos de aquí?",
                flt("es", "Местоположение — ESTAR: está lejos")),
            flp("Я иду в парк пешком.", "Voy al parque a pie.",
                flt("con", "Пешком = a pie, не «con pie»")),
            flp("Сколько стоит билет?", "¿Cuánto cuesta el billete?",
                flt("Cuántos", "«Сколько стоит» = cuánto cuesta (ед.ч.)")),
            flp("Автобус приезжает в девять.", "El autobús llega a las nueve.",
                flt("en", "Время = a las nueve; llegar A")),
            flp("Поезд прибывает на вокзал.", "El tren llega a la estación.",
                flt("en", "Llegar A la estación — не «en»")),
            flp("Мне нужно такси в аэропорт.", "Necesito un taxi al aeropuerto.",
                flt("a", "a + el сливаются в AL: al aeropuerto")),
            flp("Я жду автобус на остановке.", "Espero el autobús en la parada.",
                flt("por", "Esperar без предлога: espero el autobús")),
            flp("Музей закрыт по понедельникам.", "El museo está cerrado los lunes.",
                flt("es", "Состояние «закрыт» — ESTAR"),
                flt("en", "«По понедельникам» = los lunes, без предлога")),
            flp("Центр города очень красивый.", "El centro de la ciudad es muy bonito.",
                flt("está", "Постоянное качество — SER: es bonito")),
            flp("Как мне дойти до площади Майор?", "¿Cómo llego a la Plaza Mayor?",
                flt("en", "Llegar A — «до»")),
            flp("Поверни направо на второй улице.", "Gira a la derecha en la segunda calle.",
                flt("derecho", "Направо = a la derecha. Derecho — «прямо»")),
        )
    )

    private val COMPRAS = FraseTheme(
        id = "compras",
        title = "Покупки",
        cefr = "A1",
        phrases = listOf(
            flp("Сколько это стоит?", "¿Cuánto cuesta esto?",
                flt("cuestan", "Esto — единственное число: cuestA")),
            flp("Есть скидка?", "¿Hay algún descuento?",
                flt("Es", "«Есть, имеется» = HAY")),
            flp("Я плачу картой.", "Pago con tarjeta.",
                flt("por", "Платить картой = con tarjeta")),
            flp("Эта рубашка очень дорогая.", "Esta camisa es muy cara.",
                flt("caro.", "Camisa — женский род: carA"),
                flt("está", "Цена как свойство — SER: es cara")),
            flp("Магазин открывается в десять.", "La tienda abre a las diez.",
                flt("El", "Tienda — женский род: LA tienda")),
            flp("Есть размер побольше?", "¿Hay una talla más grande?",
                flt("mayor?", "Про размер — más grande. Mayor — про возраст")),
            flp("Беру это, пожалуйста.", "Me llevo esto, por favor.",
                flt("para", "«Пожалуйста» = POR favor")),
            flp("Эти туфли очень удобные.", "Estos zapatos son muy cómodos.",
                flt("Estas", "Zapatos — мужской род: estOs")),
            flp("Я ищу подарок для моей мамы.", "Busco un regalo para mi madre.",
                flt("por", "Получатель подарка = PARA mi madre")),
            flp("Могу я примерить эти брюки?", "¿Puedo probarme estos pantalones?",
                flt("probar", "Примерять НА СЕБЯ = probarSE: probarme")),
            flp("Я хотел бы вернуть эту футболку.", "Quería devolver esta camiseta.",
                flt("este", "Camiseta — женский род: estA")),
            flp("Примерочные в глубине справа.", "Los probadores están al fondo a la derecha.",
                flt("son", "Расположение — ESTAR: están")),
        )
    )

    val themes: List<FraseTheme> = listOf(SALUDOS, FAMILIA, COMIDA, CIUDAD, COMPRAS)
}
