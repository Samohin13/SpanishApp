package com.spanishapp.domain.chat

/**
 * Сценарии для ИИ-чата. Каждый меняет system prompt для Gemini, чтобы
 * диалог оставался в выбранном контексте.
 *
 * История хранится отдельно для каждого сценария по [id] — это session_id
 * в таблице chat_messages.
 *
 * id строки соответствуют id в мокапе HTML (для аналитики).
 */
data class ChatScenario(
    val id: String,
    val title: String,
    val emoji: String,
    val isPro: Boolean,
    val systemPromptExtra: String,
)

object ChatScenarios {

    val DEFAULT = ChatScenario(
        id = "default",
        title = "Свободный чат",
        emoji = "💬",
        isPro = false,
        systemPromptExtra = ""
    )

    val all: List<ChatScenario> = listOf(
        DEFAULT,
        ChatScenario(
            id = "travel",
            title = "Путешествие",
            emoji = "✈️",
            isPro = true,
            systemPromptExtra = """
                СЦЕНАРИЙ: Помогаешь пользователю репетировать испанский для поездки.
                Темы: аэропорт, отель, такси, экскурсии, обмен валюты.
                Веди диалог как реальный собеседник (кассир, портье, водитель).
                Если пользователь здоровается — сразу предложи конкретную сцену.
            """.trimIndent()
        ),
        ChatScenario(
            id = "restaurant",
            title = "Ресторан",
            emoji = "🍽️",
            isPro = true,
            systemPromptExtra = """
                СЦЕНАРИЙ: Ты — официант в испанском ресторане. Помогаешь:
                заказать столик, прочитать меню, заказать блюда и напитки,
                спросить про аллергены, попросить счёт. Используй типичные блюда
                (paella, tortilla, gazpacho, jamón) и напитки (sangría).
            """.trimIndent()
        ),
        ChatScenario(
            id = "interview",
            title = "Собеседование",
            emoji = "💼",
            isPro = true,
            systemPromptExtra = """
                СЦЕНАРИЙ: Ты — рекрутер испанской компании. Задавай типичные
                вопросы интервью: о себе, опыт, сильные стороны, мотивация,
                ожидания по зарплате. На короткий ответ задавай уточняющие.
                Лексика делового общения (CV, puesto, sueldo, jornada).
            """.trimIndent()
        ),
        ChatScenario(
            id = "shopping",
            title = "Покупки",
            emoji = "🛍️",
            isPro = true,
            systemPromptExtra = """
                СЦЕНАРИЙ: Ты — продавец в магазине одежды/обуви в Испании.
                Помогай: размер, цвет, материал, цена, примерка, скидки,
                возврат. Лексика (talla, número, probador, devolución, oferta).
            """.trimIndent()
        ),
        ChatScenario(
            id = "doctor",
            title = "У врача",
            emoji = "🩺",
            isPro = true,
            systemPromptExtra = """
                СЦЕНАРИЙ: Ты — врач или фармацевт в Испании. Помогай описать
                симптомы, локализовать боль, узнать про аллергии, оформить рецепт.
                Заботливый тон. Лексика (síntomas, receta, alergia, dolor).
            """.trimIndent()
        ),
    )

    fun byId(id: String): ChatScenario =
        all.firstOrNull { it.id == id } ?: DEFAULT
}
