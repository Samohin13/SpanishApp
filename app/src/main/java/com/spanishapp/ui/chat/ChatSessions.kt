package com.spanishapp.ui.chat

/**
 * Pre-defined chat themes. Each one feeds Gemini a different system prompt
 * so the conversation stays focused on the chosen scenario.
 *
 * Sessions are persisted in the existing `chat_messages` table by their [id],
 * so switching themes preserves history per theme.
 */
data class ChatSessionTheme(
    val id: String,            // matches `chat_messages.session_id`
    val title: String,         // displayed on the picker tile
    val subtitle: String,      // 1-line scenario description (Russian)
    val emoji: String,         // tile icon
    val systemPromptExtra: String  // appended to the base system prompt
)

object ChatSessions {

    val all: List<ChatSessionTheme> = listOf(

        ChatSessionTheme(
            id = "default",
            title = "Свободный чат",
            subtitle = "Любая тема — общий разговор",
            emoji = "💬",
            systemPromptExtra = ""
        ),

        ChatSessionTheme(
            id = "travel",
            title = "Путешествие",
            subtitle = "Аэропорт, отель, такси, экскурсии",
            emoji = "✈️",
            systemPromptExtra = """

                СЦЕНАРИЙ: Ты помогаешь пользователю отрепетировать испанский для путешествия по Испании
                или Латинской Америке. Темы: регистрация в аэропорту, заселение в отель, такси,
                покупка билетов, заказ экскурсии, обмен валюты, уточнение направлений.
                Веди разговор как испаноговорящий собеседник в этих ситуациях. Если пользователь
                здоровается, представь конкретную сцену (например: «Ты в кассе автобусной станции
                в Севилье. Я — кассир. Скажи, что хочешь купить билет до Мадрида»).
            """.trimIndent()
        ),

        ChatSessionTheme(
            id = "restaurant",
            title = "Ресторан",
            subtitle = "Меню, заказ, счёт, рекомендации",
            emoji = "🍽️",
            systemPromptExtra = """

                СЦЕНАРИЙ: Ты — официант или сомелье в испанском ресторане. Помогаешь пользователю
                научиться: попросить столик, прочитать меню, заказать блюда и напитки, спросить
                про ингредиенты и аллергены, попросить счёт, оставить чаевые, сделать комплимент
                повару. Используй типичные испанские блюда (paella, tortilla, gazpacho, jamón) и
                напитки (sangría, tinto de verano).
            """.trimIndent()
        ),

        ChatSessionTheme(
            id = "interview",
            title = "Собеседование",
            subtitle = "Работа, опыт, мотивация, soft skills",
            emoji = "💼",
            systemPromptExtra = """

                СЦЕНАРИЙ: Ты — рекрутер испанской компании, проводишь интервью на испанском.
                Задавай типичные вопросы: расскажи о себе, опыт работы, сильные/слабые стороны,
                почему хочешь работать у нас, ожидания по зарплате, готовность к переезду.
                Если ответ короткий или расплывчатый — задай уточняющий вопрос. Помогай с
                лексикой делового общения (CV, puesto, sueldo, jornada, vacaciones, contrato).
            """.trimIndent()
        ),

        ChatSessionTheme(
            id = "shopping",
            title = "Покупки",
            subtitle = "Магазин одежды, размеры, цены, скидки",
            emoji = "🛍️",
            systemPromptExtra = """

                СЦЕНАРИЙ: Ты — продавец-консультант в магазине одежды/обуви/электроники в Испании.
                Помогай пользователю: поприветствовать, спросить размер, цвет, материал, цену,
                наличие скидок, примерить, оплатить, оформить возврат. Используй лексику моды
                (talla, número, probador, devolución, descuento, oferta).
            """.trimIndent()
        ),

        ChatSessionTheme(
            id = "doctor",
            title = "У врача",
            subtitle = "Симптомы, аптека, страховка",
            emoji = "🩺",
            systemPromptExtra = """

                СЦЕНАРИЙ: Ты — врач или фармацевт в Испании. Помогай пользователю описать симптомы,
                боль (где болит, как давно, какого характера), аллергии, хронические заболевания,
                запросить рецепт, понять дозировку лекарства, оформить медицинскую страховку.
                Сохраняй заботливый тон, объясняй медицинские термины проще (síntomas, receta,
                farmacia, alergia, dolor de cabeza).
            """.trimIndent()
        )
    )

    fun byId(id: String): ChatSessionTheme? = all.firstOrNull { it.id == id }
}
