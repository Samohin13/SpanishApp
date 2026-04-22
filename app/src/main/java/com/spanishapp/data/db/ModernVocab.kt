package com.spanishapp.data.db

import com.spanishapp.data.db.entity.WordEntity

/**
 * Современная разговорная и цифровая лексика — дополняет классический словарь
 * из assets/spanish_vocab.json. Короткие живые примеры употребления.
 */
object ModernVocab {

    private fun w(
        es: String, ru: String, ex: String,
        level: String = "A1", category: String, type: String = "noun"
    ) = WordEntity(
        spanish = es, russian = ru, example = ex,
        level = level, category = category, wordType = type
    )

    val entries: List<WordEntity> = listOf(
        // ── Цифровая жизнь / tecnología ──────────────────────────
        w("el móvil", "мобильный / телефон", "¿Me pasas el móvil?", category = "tecnologia"),
        w("la app", "приложение", "Descargué una app nueva.", category = "tecnologia"),
        w("el wifi", "вай-фай", "¿Cuál es la clave del wifi?", category = "tecnologia"),
        w("la contraseña", "пароль", "Olvidé mi contraseña.", category = "tecnologia"),
        w("el usuario", "имя пользователя", "Dame tu usuario.", category = "tecnologia"),
        w("la cuenta", "аккаунт / счёт", "Abrí una cuenta nueva.", category = "tecnologia"),
        w("el enlace", "ссылка", "Te mando el enlace.", category = "tecnologia"),
        w("la pantalla", "экран", "Se rompió la pantalla.", category = "tecnologia"),
        w("el cargador", "зарядка", "¿Tienes un cargador?", category = "tecnologia"),
        w("la batería", "батарея", "No tengo batería.", category = "tecnologia"),
        w("los datos", "мобильные данные", "Se me acabaron los datos.", category = "tecnologia"),
        w("el correo", "эл. почта", "Te mando un correo.", category = "tecnologia"),
        w("el mensaje", "сообщение", "No vi tu mensaje.", category = "comunicacion"),
        w("el chat", "чат", "Estamos en un chat juntos.", category = "comunicacion"),
        w("el grupo", "чат-группа", "Te añado al grupo.", category = "comunicacion"),

        // ── Соцсети ──────────────────────────────────────────────
        w("la red social", "соцсеть", "Paso mucho en redes sociales.", category = "redes_sociales"),
        w("la publicación", "пост", "Me gustó tu publicación.", category = "redes_sociales"),
        w("la historia", "история (сторис)", "Subí una historia.", category = "redes_sociales"),
        w("el seguidor", "подписчик", "Tiene mil seguidores.", category = "redes_sociales"),
        w("el me gusta", "лайк", "Le di me gusta.", category = "redes_sociales"),
        w("el comentario", "комментарий", "Déjame un comentario.", category = "redes_sociales"),
        w("la selfi", "селфи", "Nos hicimos una selfi.", category = "redes_sociales"),

        // ── Глаголы (digital verbs) ─────────────────────────────
        w("chatear", "переписываться", "Chateamos toda la noche.", category = "comunicacion", type = "verb"),
        w("subir", "загружать (в сеть)", "Subí las fotos a la nube.", category = "tecnologia", type = "verb"),
        w("descargar", "скачивать", "Voy a descargar la peli.", category = "tecnologia", type = "verb"),
        w("compartir", "делиться (репост)", "Comparte el enlace, porfa.", category = "comunicacion", type = "verb"),
        w("seguir", "подписаться / следить", "Sígueme en Insta.", category = "redes_sociales", type = "verb"),
        w("bloquear", "заблокировать", "La bloqueé en todas partes.", category = "redes_sociales", type = "verb"),
        w("publicar", "публиковать", "Acabo de publicar una foto.", category = "redes_sociales", type = "verb"),
        w("enviar", "отправить", "Envíame la ubicación.", category = "comunicacion", type = "verb"),
        w("conectar", "подключаться", "No puedo conectar al wifi.", category = "tecnologia", type = "verb"),
        w("escanear", "сканировать", "Escanea el código QR.", category = "tecnologia", type = "verb"),
        w("guardar", "сохранить", "Guarda el archivo antes.", category = "tecnologia", type = "verb"),

        // ── Стриминг и развлечения ──────────────────────────────
        w("la peli", "киношка (разг.)", "¿Vemos una peli esta noche?", category = "entretenimiento"),
        w("la serie", "сериал", "Esta serie engancha mucho.", category = "entretenimiento"),
        w("el episodio", "эпизод", "Ya vi dos episodios.", category = "entretenimiento"),
        w("el capítulo", "серия / глава", "El capítulo final es brutal.", category = "entretenimiento"),
        w("el streaming", "стриминг", "Lo veo en streaming.", category = "entretenimiento"),
        w("el videojuego", "видеоигра", "Jugamos un videojuego nuevo.", category = "entretenimiento"),
        w("el meme", "мем", "Me mandó un meme súper gracioso.", category = "entretenimiento"),

        // ── Разговорный сленг Испании ───────────────────────────
        w("guay", "классно / круто", "¡Qué guay tu ropa!", level = "A2", category = "expresiones", type = "adjective"),
        w("molar", "нравиться (разг.)", "Me mola esta canción.", level = "A2", category = "expresiones", type = "verb"),
        w("vale", "ок / ладно", "Vale, nos vemos a las ocho.", category = "expresiones", type = "phrase"),
        w("tío", "чувак", "Tío, no me lo puedo creer.", level = "A2", category = "expresiones"),
        w("tía", "чувиха", "Esa tía es muy maja.", level = "A2", category = "expresiones"),
        w("flipar", "офигевать", "Estoy flipando con esto.", level = "B1", category = "expresiones", type = "verb"),
        w("currar", "пахать / работать", "Curro mucho esta semana.", level = "B1", category = "expresiones", type = "verb"),
        w("la pasta", "бабки (деньги)", "No tengo pasta este mes.", level = "B1", category = "expresiones"),
        w("majo", "приятный / милый", "Tu hermano es muy majo.", level = "A2", category = "expresiones", type = "adjective"),
        w("mogollón", "куча / дофига", "Había mogollón de gente.", level = "B1", category = "expresiones"),

        // ── Быт и работа сейчас ─────────────────────────────────
        w("el teletrabajo", "удалёнка", "Trabajo en teletrabajo.", level = "A2", category = "trabajo"),
        w("la reunión", "встреча / митинг", "Tengo una reunión online.", level = "A2", category = "trabajo"),
        w("la videollamada", "видеозвонок", "Hacemos una videollamada.", level = "A2", category = "comunicacion"),
        w("la tarjeta", "карта (банковская)", "Pago con tarjeta.", category = "finanzas"),
        w("la transferencia", "перевод (банк.)", "Te hago una transferencia.", level = "A2", category = "finanzas"),
        w("el pedido", "заказ", "Llegó mi pedido.", category = "compras"),
        w("el repartidor", "курьер", "El repartidor está abajo.", level = "A2", category = "compras")
    )
}
