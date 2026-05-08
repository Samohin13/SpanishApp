package com.spanishapp.data.db

import com.spanishapp.data.db.entity.DialogueEntity

/**
 * 15 ситуационных диалогов для экрана "Диалоги".
 * Каждый диалог = JSON-массив строк вида:
 *   [{"speaker":"A","es":"Hola!","ru":"Привет!"},...]
 * A = первый говорящий (левый пузырь, бирюзовый)
 * B = второй говорящий (правый пузырь, терракотовый)
 */
object DialogueContent {

    fun getAll(): List<DialogueEntity> = listOf(

        // ── A1 ──────────────────────────────────────────────────

        DialogueEntity(
            id = 1, level = "A1",
            title = "Знакомство",
            situation = "Первая встреча — как зовут, откуда",
            linesJson = """[
  {"speaker":"A","es":"¡Hola! Me llamo Ana. ¿Y tú?","ru":"Привет! Меня зовут Ана. А тебя?"},
  {"speaker":"B","es":"Hola, Ana. Soy Dmitri.","ru":"Привет, Ана. Я Дмитрий."},
  {"speaker":"A","es":"¿De dónde eres, Dmitri?","ru":"Откуда ты, Дмитрий?"},
  {"speaker":"B","es":"Soy de Rusia. ¿Y tú?","ru":"Я из России. А ты?"},
  {"speaker":"A","es":"Yo soy de España, de Madrid.","ru":"Я из Испании, из Мадрида."},
  {"speaker":"B","es":"¡Qué bien! Mucho gusto.","ru":"Как здорово! Очень приятно."},
  {"speaker":"A","es":"El gusto es mío.","ru":"Мне тоже приятно."}
]"""
        ),

        DialogueEntity(
            id = 2, level = "A1",
            title = "В кафе",
            situation = "Заказ кофе и выпечки",
            linesJson = """[
  {"speaker":"A","es":"Buenos días. ¿Qué desea?","ru":"Доброе утро. Что желаете?"},
  {"speaker":"B","es":"Buenos días. Un café con leche, por favor.","ru":"Доброе утро. Кофе с молоком, пожалуйста."},
  {"speaker":"A","es":"¿Algo más?","ru":"Что-нибудь ещё?"},
  {"speaker":"B","es":"Sí, un croissant también, por favor.","ru":"Да, ещё круассан, пожалуйста."},
  {"speaker":"A","es":"¿Para aquí o para llevar?","ru":"Здесь или с собой?"},
  {"speaker":"B","es":"Para aquí, gracias.","ru":"Здесь, спасибо."},
  {"speaker":"A","es":"Son tres euros con cincuenta.","ru":"С вас три евро пятьдесят центов."},
  {"speaker":"B","es":"Aquí tiene.","ru":"Пожалуйста, держите."}
]"""
        ),

        DialogueEntity(
            id = 3, level = "A1",
            title = "Числа и возраст",
            situation = "Спрашиваем сколько лет и называем числа",
            linesJson = """[
  {"speaker":"A","es":"¿Cuántos años tienes?","ru":"Сколько тебе лет?"},
  {"speaker":"B","es":"Tengo veinticinco años. ¿Y tú?","ru":"Мне двадцать пять. А тебе?"},
  {"speaker":"A","es":"Yo tengo treinta y dos años.","ru":"Мне тридцать два года."},
  {"speaker":"B","es":"¿Y cuántos años tiene tu hermano?","ru":"А сколько лет твоему брату?"},
  {"speaker":"A","es":"Tiene dieciséis años.","ru":"Ему шестнадцать лет."},
  {"speaker":"B","es":"¡Es muy joven!","ru":"Он очень молодой!"}
]"""
        ),

        DialogueEntity(
            id = 4, level = "A1",
            title = "В магазине",
            situation = "Покупка одежды, цены",
            linesJson = """[
  {"speaker":"A","es":"Hola, ¿en qué puedo ayudarle?","ru":"Здравствуйте, чем могу помочь?"},
  {"speaker":"B","es":"Hola. Busco una camiseta azul.","ru":"Здравствуйте. Ищу синюю футболку."},
  {"speaker":"A","es":"¿Qué talla usa usted?","ru":"Какой у вас размер?"},
  {"speaker":"B","es":"Talla mediana, por favor.","ru":"Размер средний, пожалуйста."},
  {"speaker":"A","es":"Aquí tiene. ¿Le gusta este modelo?","ru":"Вот, пожалуйста. Вам нравится эта модель?"},
  {"speaker":"B","es":"Sí, me gusta. ¿Cuánto cuesta?","ru":"Да, нравится. Сколько стоит?"},
  {"speaker":"A","es":"Cuesta quince euros.","ru":"Стоит пятнадцать евро."},
  {"speaker":"B","es":"Perfecto. Me la llevo.","ru":"Отлично. Беру её."}
]"""
        ),

        DialogueEntity(
            id = 5, level = "A1",
            title = "Погода",
            situation = "Разговор о погоде",
            linesJson = """[
  {"speaker":"A","es":"¿Qué tiempo hace hoy?","ru":"Какая сегодня погода?"},
  {"speaker":"B","es":"Hoy hace mucho frío y llueve.","ru":"Сегодня очень холодно и идёт дождь."},
  {"speaker":"A","es":"¡Qué lástima! Yo quería ir al parque.","ru":"Как жаль! Я хотела пойти в парк."},
  {"speaker":"B","es":"Mañana va a hacer sol, dicen.","ru":"Говорят, завтра будет солнечно."},
  {"speaker":"A","es":"¡Perfecto! Entonces vamos mañana.","ru":"Отлично! Тогда пойдём завтра."},
  {"speaker":"B","es":"¡De acuerdo!","ru":"Договорились!"}
]"""
        ),

        // ── A2 ──────────────────────────────────────────────────

        DialogueEntity(
            id = 6, level = "A2",
            title = "В ресторане",
            situation = "Заказ блюд, проблема с едой",
            linesJson = """[
  {"speaker":"A","es":"Buenas tardes. ¿Tienen mesa para dos personas?","ru":"Добрый день. У вас есть столик на двоих?"},
  {"speaker":"B","es":"Sí, claro. Por aquí, por favor.","ru":"Да, конечно. Сюда, пожалуйста."},
  {"speaker":"A","es":"Gracias. ¿Nos trae la carta?","ru":"Спасибо. Принесёте нам меню?"},
  {"speaker":"B","es":"En seguida. ¿Qué van a tomar?","ru":"Сейчас. Что будете заказывать?"},
  {"speaker":"A","es":"Para mí, paella de mariscos.","ru":"Мне паэлья с морепродуктами."},
  {"speaker":"B","es":"Y yo quiero el menú del día.","ru":"А я хочу меню дня."},
  {"speaker":"A","es":"Disculpe, este plato está frío.","ru":"Извините, это блюдо холодное."},
  {"speaker":"B","es":"Mil disculpas. Ahora mismo le traigo otro.","ru":"Тысяча извинений. Сейчас принесу другое."}
]"""
        ),

        DialogueEntity(
            id = 7, level = "A2",
            title = "В аэропорту",
            situation = "Регистрация на рейс, багаж",
            linesJson = """[
  {"speaker":"A","es":"Buenos días. Su pasaporte, por favor.","ru":"Доброе утро. Ваш паспорт, пожалуйста."},
  {"speaker":"B","es":"Aquí tiene. Vuelo a Barcelona.","ru":"Пожалуйста. Рейс в Барселону."},
  {"speaker":"A","es":"¿Cuántas maletas factura?","ru":"Сколько чемоданов сдаёте в багаж?"},
  {"speaker":"B","es":"Solo una maleta.","ru":"Только один чемодан."},
  {"speaker":"A","es":"Pesa veintidós kilos. Está bien.","ru":"Весит двадцать два килограмма. Всё в порядке."},
  {"speaker":"B","es":"¿A qué hora embarca el avión?","ru":"В котором часу посадка на самолёт?"},
  {"speaker":"A","es":"La puerta de embarque es la B12. Abre a las diez.","ru":"Выход на посадку — B12. Открывается в десять."},
  {"speaker":"B","es":"Muchas gracias. ¡Buen día!","ru":"Большое спасибо. Хорошего дня!"}
]"""
        ),

        DialogueEntity(
            id = 8, level = "A2",
            title = "У врача",
            situation = "Жалобы на здоровье, назначение лечения",
            linesJson = """[
  {"speaker":"A","es":"Buenos días. ¿Qué le ocurre?","ru":"Доброе утро. Что вас беспокоит?"},
  {"speaker":"B","es":"Me duele mucho la cabeza y tengo fiebre.","ru":"У меня сильно болит голова и температура."},
  {"speaker":"A","es":"¿Desde cuándo se encuentra mal?","ru":"С каких пор вы плохо себя чувствуете?"},
  {"speaker":"B","es":"Desde ayer por la tarde.","ru":"С вчерашнего вечера."},
  {"speaker":"A","es":"Voy a tomarle la temperatura. Tiene 38,5.","ru":"Сейчас измерю температуру. У вас 38,5."},
  {"speaker":"B","es":"¿Es grave?","ru":"Это серьёзно?"},
  {"speaker":"A","es":"No se preocupe. Es un resfriado. Le receto un antibiótico.","ru":"Не волнуйтесь. Это простуда. Выписываю антибиотик."},
  {"speaker":"B","es":"Gracias, doctor.","ru":"Спасибо, доктор."}
]"""
        ),

        DialogueEntity(
            id = 9, level = "A2",
            title = "На работе",
            situation = "Собеседование на работу",
            linesJson = """[
  {"speaker":"A","es":"Buenas tardes. Siéntese, por favor.","ru":"Добрый день. Садитесь, пожалуйста."},
  {"speaker":"B","es":"Buenas tardes. Gracias por recibirme.","ru":"Добрый день. Спасибо, что приняли меня."},
  {"speaker":"A","es":"¿Cuántos años de experiencia tiene en este campo?","ru":"Сколько лет у вас опыта в этой области?"},
  {"speaker":"B","es":"Tengo cinco años de experiencia como diseñador gráfico.","ru":"У меня пять лет опыта в качестве графического дизайнера."},
  {"speaker":"A","es":"¿Habla otros idiomas además del español?","ru":"Вы говорите на других языках помимо испанского?"},
  {"speaker":"B","es":"Sí, hablo inglés y ruso con fluidez.","ru":"Да, я свободно говорю по-английски и по-русски."},
  {"speaker":"A","es":"Perfecto. Le llamaremos la próxima semana.","ru":"Отлично. Мы позвоним вам на следующей неделе."},
  {"speaker":"B","es":"Muchas gracias. ¡Hasta pronto!","ru":"Большое спасибо. До скорого!"}
]"""
        ),

        DialogueEntity(
            id = 10, level = "A2",
            title = "На улице",
            situation = "Спрашиваем дорогу",
            linesJson = """[
  {"speaker":"A","es":"Perdona, ¿sabes dónde está la estación de metro?","ru":"Извини, ты знаешь, где станция метро?"},
  {"speaker":"B","es":"Sí, claro. Sigue todo recto dos manzanas.","ru":"Да, конечно. Иди прямо два квартала."},
  {"speaker":"A","es":"¿Y luego?","ru":"И потом?"},
  {"speaker":"B","es":"Gira a la izquierda en el semáforo.","ru":"Поверни налево на светофоре."},
  {"speaker":"A","es":"¿Está lejos?","ru":"Это далеко?"},
  {"speaker":"B","es":"No, está a cinco minutos a pie.","ru":"Нет, это в пяти минутах пешком."},
  {"speaker":"A","es":"Muchísimas gracias.","ru":"Большое спасибо."},
  {"speaker":"B","es":"De nada. ¡Buen paseo!","ru":"Пожалуйста. Хорошей прогулки!"}
]"""
        ),

        // ── B1 ──────────────────────────────────────────────────

        DialogueEntity(
            id = 11, level = "B1",
            title = "Аренда квартиры",
            situation = "Переговоры с арендодателем",
            linesJson = """[
  {"speaker":"A","es":"Hola, llamo por el anuncio del piso en alquiler.","ru":"Здравствуйте, звоню по объявлению о сдаче квартиры."},
  {"speaker":"B","es":"Hola, sí. ¿Cuándo le gustaría verlo?","ru":"Здравствуйте, да. Когда хотите посмотреть?"},
  {"speaker":"A","es":"¿Podría ser este sábado por la mañana?","ru":"Можно в эту субботу утром?"},
  {"speaker":"B","es":"Por supuesto. ¿Le va bien a las once?","ru":"Конечно. Вам удобно в одиннадцать?"},
  {"speaker":"A","es":"Perfecto. ¿Incluye los gastos de comunidad?","ru":"Отлично. Коммунальные расходы включены?"},
  {"speaker":"B","es":"El agua y la basura sí, pero la luz y el gas no.","ru":"Вода и мусор да, но электричество и газ нет."},
  {"speaker":"A","es":"¿Hay posibilidad de negociar el precio?","ru":"Есть ли возможность договориться о цене?"},
  {"speaker":"B","es":"Podemos hablar si firma por un año.","ru":"Можем договориться, если подпишете на год."}
]"""
        ),

        DialogueEntity(
            id = 12, level = "B1",
            title = "Споры и мнения",
            situation = "Обсуждение фильма с другом",
            linesJson = """[
  {"speaker":"A","es":"¿Qué te pareció la película de anoche?","ru":"Как тебе вчерашний фильм?"},
  {"speaker":"B","es":"Sinceramente, me aburrió bastante. Fue demasiado lenta.","ru":"Честно говоря, мне было довольно скучно. Слишком медленная."},
  {"speaker":"A","es":"¿En serio? A mí me pareció fascinante. El guión era muy original.","ru":"Серьёзно? Мне показалось захватывающим. Сценарий очень оригинальный."},
  {"speaker":"B","es":"Tal vez, pero los personajes no me convencieron para nada.","ru":"Может быть, но персонажи меня совсем не убедили."},
  {"speaker":"A","es":"Creo que no entendiste el mensaje de fondo.","ru":"Думаю, ты не понял основную мысль."},
  {"speaker":"B","es":"Quizás tengas razón. ¿Me la explicas?","ru":"Возможно, ты права. Объяснишь мне?"},
  {"speaker":"A","es":"Claro. Trata sobre la soledad en las grandes ciudades.","ru":"Конечно. Это о одиночестве в больших городах."},
  {"speaker":"B","es":"Ah, visto así tiene más sentido.","ru":"О, с этой точки зрения смысла больше."}
]"""
        ),

        DialogueEntity(
            id = 13, level = "B1",
            title = "Технологии",
            situation = "Помощь с настройкой телефона",
            linesJson = """[
  {"speaker":"A","es":"Oye, ¿me puedes ayudar con el móvil? No me funciona el wifi.","ru":"Слушай, можешь помочь с телефоном? У меня не работает вайфай."},
  {"speaker":"B","es":"A ver... ¿Has reiniciado el router?","ru":"Посмотрим... Ты перезапускал роутер?"},
  {"speaker":"A","es":"Sí, pero sigue sin conectarse.","ru":"Да, но он по-прежнему не подключается."},
  {"speaker":"B","es":"Prueba a olvidar la red y vuelve a introducir la contraseña.","ru":"Попробуй забыть сеть и снова ввести пароль."},
  {"speaker":"A","es":"¡Funciona! ¿Cómo sabías eso?","ru":"Работает! Откуда ты это знал?"},
  {"speaker":"B","es":"Me pasó lo mismo la semana pasada. A veces la app guarda datos corruptos.","ru":"У меня было то же самое на прошлой неделе. Иногда приложение сохраняет повреждённые данные."},
  {"speaker":"A","es":"Tengo que aprender más de tecnología.","ru":"Надо больше разбираться в технологиях."},
  {"speaker":"B","es":"Hay tutoriales muy buenos en YouTube, te mando uno.","ru":"На YouTube есть отличные туториалы, пришлю тебе один."}
]"""
        ),

        DialogueEntity(
            id = 14, level = "B1",
            title = "Путешествие",
            situation = "Планирование отпуска с другом",
            linesJson = """[
  {"speaker":"A","es":"¿Ya has pensado adónde vamos este verano?","ru":"Ты уже подумал, куда едем этим летом?"},
  {"speaker":"B","es":"Estaba pensando en México. Siempre he querido ver Chichén Itzá.","ru":"Я думал о Мексике. Всегда хотел увидеть Чичен-Ицу."},
  {"speaker":"A","es":"¡Me parece genial! ¿Cuántos días tenemos?","ru":"Мне кажется, здорово! Сколько у нас дней?"},
  {"speaker":"B","es":"Dos semanas. Podríamos combinar playa y cultura.","ru":"Две недели. Можно совместить пляж и культуру."},
  {"speaker":"A","es":"Perfecto. Yo me encargo de buscar los vuelos.","ru":"Отлично. Я займусь поиском рейсов."},
  {"speaker":"B","es":"Y yo busco los hoteles y las excursiones.","ru":"А я поищу отели и экскурсии."},
  {"speaker":"A","es":"¿Cuánto queremos gastar aproximadamente?","ru":"Примерно сколько мы хотим потратить?"},
  {"speaker":"B","es":"Unos dos mil euros cada uno, ¿te parece?","ru":"Примерно две тысячи евро каждый, как тебе?"}
]"""
        ),

        DialogueEntity(
            id = 15, level = "B1",
            title = "Экология",
            situation = "Дискуссия об экологических проблемах",
            linesJson = """[
  {"speaker":"A","es":"¿Crees que el cambio climático es el mayor problema de nuestro tiempo?","ru":"Ты считаешь, что изменение климата — главная проблема нашего времени?"},
  {"speaker":"B","es":"Sin duda. Los datos científicos son muy claros.","ru":"Без сомнения. Научные данные очень ясны."},
  {"speaker":"A","es":"¿Pero qué puede hacer una persona sola?","ru":"Но что может сделать один человек?"},
  {"speaker":"B","es":"Mucho. Reducir el consumo de plástico, usar transporte público, comer menos carne.","ru":"Много. Сократить потребление пластика, пользоваться общественным транспортом, есть меньше мяса."},
  {"speaker":"A","es":"Tienes razón, pero también necesitamos cambios a nivel político.","ru":"Ты прав, но нам также нужны изменения на политическом уровне."},
  {"speaker":"B","es":"Completamente de acuerdo. El cambio individual y el colectivo deben ir juntos.","ru":"Полностью согласен. Индивидуальные и коллективные изменения должны идти вместе."},
  {"speaker":"A","es":"Me alegra que pensemos igual en esto.","ru":"Рада, что мы думаем одинаково в этом."},
  {"speaker":"B","es":"Es un tema demasiado importante para ignorarlo.","ru":"Это слишком важная тема, чтобы её игнорировать."}
]"""
        ),

        // ── A2 ──────────────────────────────────────────────────

        DialogueEntity(
            id = 16, level = "A2",
            title = "В аэропорту: регистрация",
            situation = "Сдача багажа на стойке регистрации",
            linesJson = """[
  {"speaker":"A","es":"Buenos días. Su pasaporte y billete, por favor.","ru":"Доброе утро. Ваш паспорт и билет, пожалуйста."},
  {"speaker":"B","es":"Aquí los tiene.","ru":"Вот, пожалуйста."},
  {"speaker":"A","es":"¿Cuántas maletas factura?","ru":"Сколько чемоданов сдаёте?"},
  {"speaker":"B","es":"Una maleta grande y una de mano.","ru":"Один большой чемодан и одну ручную кладь."},
  {"speaker":"A","es":"Ponga la maleta aquí, por favor. Pesa veinticinco kilos — está dentro del límite.","ru":"Поставьте чемодан сюда, пожалуйста. Весит двадцать пять килограммов — в пределах нормы."},
  {"speaker":"B","es":"¿La puerta de embarque?","ru":"Какой выход на посадку?"},
  {"speaker":"A","es":"Puerta B12. Embarque a las diez y media.","ru":"Выход B12. Посадка в десять тридцать."},
  {"speaker":"B","es":"Muchas gracias. ¡Buen día!","ru":"Большое спасибо. Хорошего дня!"}
]"""
        ),

        DialogueEntity(
            id = 17, level = "A2",
            title = "В отеле: заселение",
            situation = "Регистрация в отеле, вопросы про номер",
            linesJson = """[
  {"speaker":"A","es":"Buenas tardes. Tengo una reserva a nombre de Pavlov.","ru":"Добрый день. У меня бронь на имя Павлов."},
  {"speaker":"B","es":"Un momento, por favor… Sí, una habitación doble por tres noches.","ru":"Минутку, пожалуйста… Да, двухместный номер на три ночи."},
  {"speaker":"A","es":"¿La habitación tiene wifi?","ru":"В номере есть Wi-Fi?"},
  {"speaker":"B","es":"Sí, gratis. La contraseña está en la tarjeta de la habitación.","ru":"Да, бесплатный. Пароль на карточке номера."},
  {"speaker":"A","es":"¿A qué hora es el desayuno?","ru":"Во сколько завтрак?"},
  {"speaker":"B","es":"De siete a diez en el restaurante de la planta baja.","ru":"С семи до десяти в ресторане на первом этаже."},
  {"speaker":"A","es":"¿Y la salida?","ru":"А выселение?"},
  {"speaker":"B","es":"Antes de las doce. Aquí tiene su llave, habitación 305.","ru":"До двенадцати. Вот ваш ключ, номер 305."}
]"""
        ),

        DialogueEntity(
            id = 18, level = "A2",
            title = "У врача",
            situation = "Приём у врача, описание симптомов",
            linesJson = """[
  {"speaker":"A","es":"Buenos días. ¿Qué le pasa?","ru":"Доброе утро. Что вас беспокоит?"},
  {"speaker":"B","es":"Me duele mucho la cabeza desde ayer.","ru":"У меня сильно болит голова со вчерашнего дня."},
  {"speaker":"A","es":"¿Tiene fiebre?","ru":"У вас есть температура?"},
  {"speaker":"B","es":"Sí, tengo treinta y ocho grados.","ru":"Да, тридцать восемь."},
  {"speaker":"A","es":"¿Le duele la garganta también?","ru":"Горло тоже болит?"},
  {"speaker":"B","es":"Un poco, y tengo tos.","ru":"Немного, и есть кашель."},
  {"speaker":"A","es":"Parece una gripe. Le voy a recetar un antibiótico.","ru":"Похоже на грипп. Я выпишу вам антибиотик."},
  {"speaker":"B","es":"¿Cuánto tiempo lo tomo?","ru":"Сколько времени принимать?"},
  {"speaker":"A","es":"Una pastilla cada ocho horas durante una semana.","ru":"Одна таблетка каждые восемь часов в течение недели."}
]"""
        ),

        DialogueEntity(
            id = 19, level = "A2",
            title = "В ресторане",
            situation = "Заказ ужина, оплата",
            linesJson = """[
  {"speaker":"A","es":"Buenas noches. ¿Tienen mesa para dos?","ru":"Добрый вечер. У вас есть столик на двоих?"},
  {"speaker":"B","es":"Sí, junto a la ventana. Síganme, por favor.","ru":"Да, у окна. Следуйте за мной, пожалуйста."},
  {"speaker":"A","es":"Gracias. ¿Qué nos recomienda?","ru":"Спасибо. Что вы порекомендуете?"},
  {"speaker":"B","es":"La paella de mariscos está deliciosa hoy.","ru":"Паэлья с морепродуктами сегодня великолепна."},
  {"speaker":"A","es":"Perfecto. Una paella y una ensalada mixta, por favor.","ru":"Отлично. Одну паэлью и смешанный салат, пожалуйста."},
  {"speaker":"B","es":"¿Para beber?","ru":"Что будете пить?"},
  {"speaker":"A","es":"Una botella de vino tinto y agua sin gas.","ru":"Бутылку красного вина и воду без газа."},
  {"speaker":"B","es":"Enseguida se lo traigo.","ru":"Сейчас принесу."},
  {"speaker":"A","es":"La cuenta, por favor.","ru":"Счёт, пожалуйста."},
  {"speaker":"B","es":"Aquí tiene. Son cuarenta y dos euros.","ru":"Вот, пожалуйста. Сорок два евро."}
]"""
        ),

        DialogueEntity(
            id = 20, level = "A2",
            title = "На вокзале",
            situation = "Покупка билета на поезд",
            linesJson = """[
  {"speaker":"A","es":"Hola, quiero un billete a Sevilla, por favor.","ru":"Здравствуйте, мне билет до Севильи, пожалуйста."},
  {"speaker":"B","es":"¿Para hoy o para otro día?","ru":"На сегодня или на другой день?"},
  {"speaker":"A","es":"Para mañana por la mañana.","ru":"На завтра утром."},
  {"speaker":"B","es":"Hay un AVE a las nueve y otro a las once.","ru":"Есть AVE в девять и другой в одиннадцать."},
  {"speaker":"A","es":"El de las nueve, por favor. ¿Cuánto cuesta?","ru":"Тот, что в девять, пожалуйста. Сколько стоит?"},
  {"speaker":"B","es":"Setenta y ocho euros en clase turista.","ru":"Семьдесят восемь евро в эконом-классе."},
  {"speaker":"A","es":"¿Hay descuento para estudiantes?","ru":"Есть скидка для студентов?"},
  {"speaker":"B","es":"Sí, un veinticinco por ciento. ¿Tiene carnet?","ru":"Да, двадцать пять процентов. У вас есть студенческий?"},
  {"speaker":"A","es":"Sí, aquí tiene.","ru":"Да, вот."}
]"""
        ),

        DialogueEntity(
            id = 21, level = "A2",
            title = "Прокат машины",
            situation = "Аренда автомобиля в туристическом офисе",
            linesJson = """[
  {"speaker":"A","es":"Buenos días. Quería alquilar un coche.","ru":"Доброе утро. Я хотел бы взять машину в аренду."},
  {"speaker":"B","es":"¿Para cuántos días?","ru":"На сколько дней?"},
  {"speaker":"A","es":"Cinco días, hasta el sábado.","ru":"Пять дней, до субботы."},
  {"speaker":"B","es":"¿Qué tipo de coche prefiere?","ru":"Какой тип машины вы предпочитаете?"},
  {"speaker":"A","es":"Algo pequeño, automático si es posible.","ru":"Что-то маленькое, желательно автомат."},
  {"speaker":"B","es":"Tenemos un Renault Clio automático por treinta euros al día.","ru":"У нас есть Renault Clio с автоматом за тридцать евро в день."},
  {"speaker":"A","es":"¿El seguro está incluido?","ru":"Страховка включена?"},
  {"speaker":"B","es":"El seguro básico sí, pero el completo es diez euros más.","ru":"Базовая — да, но полная на десять евро дороже."},
  {"speaker":"A","es":"Quiero el seguro completo, por favor.","ru":"Хочу полную страховку, пожалуйста."}
]"""
        ),

        DialogueEntity(
            id = 22, level = "A2",
            title = "На почте",
            situation = "Отправка посылки",
            linesJson = """[
  {"speaker":"A","es":"Quiero enviar este paquete a Rusia.","ru":"Хочу отправить эту посылку в Россию."},
  {"speaker":"B","es":"¿Qué hay dentro?","ru":"Что внутри?"},
  {"speaker":"A","es":"Ropa y libros, regalos para mi familia.","ru":"Одежда и книги, подарки семье."},
  {"speaker":"B","es":"Pongamos en la aduana 'efectos personales'. ¿Urgente o ordinario?","ru":"Запишем в таможне 'личные вещи'. Срочно или обычно?"},
  {"speaker":"A","es":"¿Cuánto tarda cada uno?","ru":"Сколько идёт каждый вариант?"},
  {"speaker":"B","es":"Urgente, una semana. Ordinario, hasta tres.","ru":"Срочно — неделя. Обычно — до трёх."},
  {"speaker":"A","es":"Urgente entonces.","ru":"Тогда срочно."},
  {"speaker":"B","es":"Cuarenta y ocho euros con seguro hasta cien.","ru":"Сорок восемь евро со страховкой до ста."}
]"""
        ),

        DialogueEntity(
            id = 23, level = "A2",
            title = "Спросить дорогу",
            situation = "Как пройти к достопримечательности",
            linesJson = """[
  {"speaker":"A","es":"Perdone, ¿cómo se llega al museo del Prado?","ru":"Извините, как добраться до музея Прадо?"},
  {"speaker":"B","es":"Está cerca. Vaya recto dos calles, después gire a la derecha.","ru":"Это близко. Пройдите прямо два квартала, потом поверните направо."},
  {"speaker":"A","es":"¿Y luego?","ru":"А потом?"},
  {"speaker":"B","es":"Verá una plaza grande. El museo está al otro lado.","ru":"Увидите большую площадь. Музей на другой стороне."},
  {"speaker":"A","es":"¿Cuánto se tarda andando?","ru":"Сколько идти пешком?"},
  {"speaker":"B","es":"Unos diez minutos.","ru":"Около десяти минут."},
  {"speaker":"A","es":"Muchas gracias.","ru":"Большое спасибо."},
  {"speaker":"B","es":"De nada. ¡Que disfrute la visita!","ru":"Не за что. Приятного посещения!"}
]"""
        ),

        DialogueEntity(
            id = 24, level = "A2",
            title = "У парикмахера",
            situation = "Стрижка и обслуживание",
            linesJson = """[
  {"speaker":"A","es":"Buenas tardes. Tengo cita a las cuatro.","ru":"Добрый день. У меня запись на четыре."},
  {"speaker":"B","es":"Sí, claro. ¿Cómo quiere el corte?","ru":"Да, конечно. Какую стрижку хотите?"},
  {"speaker":"A","es":"No muy corto, sólo un poco más arreglado.","ru":"Не очень коротко, просто немного поаккуратнее."},
  {"speaker":"B","es":"¿Quiere también lavarse el pelo?","ru":"Хотите помыть голову?"},
  {"speaker":"A","es":"Sí, por favor.","ru":"Да, пожалуйста."},
  {"speaker":"B","es":"¿Le pongo gel o prefiere natural?","ru":"Нанести гель или предпочитаете естественно?"},
  {"speaker":"A","es":"Natural, gracias. ¿Cuánto es?","ru":"Естественно, спасибо. Сколько с меня?"},
  {"speaker":"B","es":"Veinte euros. ¿Paga con tarjeta o efectivo?","ru":"Двадцать евро. Оплачиваете картой или наличными?"}
]"""
        ),

        DialogueEntity(
            id = 25, level = "A2",
            title = "Жалоба в магазине",
            situation = "Возврат бракованного товара",
            linesJson = """[
  {"speaker":"A","es":"Compré esta camisa ayer, pero tiene un agujero.","ru":"Я купил эту рубашку вчера, но в ней дыра."},
  {"speaker":"B","es":"Lo siento mucho. ¿Tiene el ticket?","ru":"Очень сожалею. У вас есть чек?"},
  {"speaker":"A","es":"Sí, aquí tiene.","ru":"Да, вот."},
  {"speaker":"B","es":"¿Quiere cambiarla por otra o devolver el dinero?","ru":"Хотите заменить или вернуть деньги?"},
  {"speaker":"A","es":"Prefiero el reembolso, por favor.","ru":"Предпочитаю возврат, пожалуйста."},
  {"speaker":"B","es":"De acuerdo. Le devuelvo treinta euros.","ru":"Хорошо. Возвращаю вам тридцать евро."},
  {"speaker":"A","es":"Disculpe las molestias.","ru":"Простите за неудобства."},
  {"speaker":"B","es":"Al contrario, gracias por avisar.","ru":"Наоборот, спасибо что сказали."}
]"""
        )
    )
}
