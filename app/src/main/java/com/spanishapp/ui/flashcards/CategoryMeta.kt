package com.spanishapp.ui.flashcards

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryInfo(val key: String, val label: String, val icon: ImageVector)

object CategoryMeta {

    private val ALL = CategoryInfo("all", "Все", Icons.Filled.Apps)

    private val MAP: Map<String, CategoryInfo> = listOf(
        CategoryInfo("acciones",        "Действия",       Icons.Filled.DirectionsRun),
        CategoryInfo("animales",        "Животные",       Icons.Filled.Pets),
        CategoryInfo("arte",            "Искусство",      Icons.Filled.Palette),
        CategoryInfo("auxiliares",      "Вспомогательные",Icons.Filled.Extension),
        CategoryInfo("calidad",         "Качество",       Icons.Filled.Star),
        CategoryInfo("cantidad",        "Количество",     Icons.Filled.Numbers),
        CategoryInfo("casa",            "Дом",            Icons.Filled.Home),
        CategoryInfo("ciudad",          "Город",          Icons.Filled.LocationCity),
        CategoryInfo("colores",         "Цвета",          Icons.Filled.ColorLens),
        CategoryInfo("comercio",        "Коммерция",      Icons.Filled.Storefront),
        CategoryInfo("comida",          "Еда",            Icons.Filled.Restaurant),
        CategoryInfo("compras",         "Покупки",        Icons.Filled.ShoppingCart),
        CategoryInfo("comunicacion",    "Общение",        Icons.Filled.Chat),
        CategoryInfo("conocimiento",    "Знания",         Icons.Filled.MenuBook),
        CategoryInfo("cortesia",        "Вежливость",     Icons.Filled.ThumbUp),
        CategoryInfo("cotidiano",       "Повседневное",   Icons.Filled.Today),
        CategoryInfo("creatividad",     "Творчество",     Icons.Filled.Lightbulb),
        CategoryInfo("cuerpo",          "Тело",           Icons.Filled.Accessibility),
        CategoryInfo("cultura",         "Культура",       Icons.Filled.TheaterComedy),
        CategoryInfo("deporte",         "Спорт",          Icons.Filled.SportsSoccer),
        CategoryInfo("despedidas",      "Прощания",       Icons.Filled.Logout),
        CategoryInfo("educacion",       "Образование",    Icons.Filled.School),
        CategoryInfo("emociones",       "Эмоции",         Icons.Filled.Mood),
        CategoryInfo("entretenimiento", "Развлечения",    Icons.Filled.Movie),
        CategoryInfo("estados",         "Состояния",      Icons.Filled.EmojiEmotions),
        CategoryInfo("expresiones",     "Выражения",      Icons.Filled.RecordVoiceOver),
        CategoryInfo("familia_personas","Семья",          Icons.Filled.FamilyRestroom),
        CategoryInfo("finanzas",        "Финансы",        Icons.Filled.AttachMoney),
        CategoryInfo("fisico",          "Физ. состояние", Icons.Filled.FitnessCenter),
        CategoryInfo("general",         "Общее",          Icons.Filled.Category),
        CategoryInfo("hotel",           "Отель",          Icons.Filled.Hotel),
        CategoryInfo("lugares",         "Места",          Icons.Filled.Place),
        CategoryInfo("materiales",      "Материалы",      Icons.Filled.Build),
        CategoryInfo("media",           "Медиа",          Icons.Filled.PlayCircle),
        CategoryInfo("modal",           "Модальные",      Icons.Filled.HelpOutline),
        CategoryInfo("movimiento",      "Движение",       Icons.Filled.DirectionsWalk),
        CategoryInfo("naturaleza",      "Природа",        Icons.Filled.Park),
        CategoryInfo("numeros",         "Числа",          Icons.Filled.Numbers),
        CategoryInfo("orden",           "Порядок",        Icons.Filled.FormatListNumbered),
        CategoryInfo("pensamiento",     "Мышление",       Icons.Filled.Psychology),
        CategoryInfo("percepcion",      "Восприятие",     Icons.Filled.Visibility),
        CategoryInfo("personal",        "Личное",         Icons.Filled.Person),
        CategoryInfo("personas",        "Люди",           Icons.Filled.People),
        CategoryInfo("precio",          "Цена",           Icons.Filled.LocalOffer),
        CategoryInfo("preguntas",       "Вопросы",        Icons.Filled.QuestionMark),
        CategoryInfo("profesiones",     "Профессии",      Icons.Filled.Work),
        CategoryInfo("redes_sociales",  "Соцсети",        Icons.Filled.Share),
        CategoryInfo("reglas",          "Правила",        Icons.Filled.Gavel),
        CategoryInfo("restaurante",     "Ресторан",       Icons.Filled.Restaurant),
        CategoryInfo("ropa",            "Одежда",         Icons.Filled.Checkroom),
        CategoryInfo("salud",           "Здоровье",       Icons.Filled.HealthAndSafety),
        CategoryInfo("saludos",         "Приветствия",    Icons.Filled.WavingHand),
        CategoryInfo("social",          "Социальное",     Icons.Filled.Groups),
        CategoryInfo("sociedad",        "Общество",       Icons.Filled.Groups),
        CategoryInfo("tamaño",          "Размер",         Icons.Filled.Straighten),
        CategoryInfo("tecnologia",      "Технологии",     Icons.Filled.Computer),
        CategoryInfo("tiempo",          "Время",          Icons.Filled.AccessTime),
        CategoryInfo("trabajo",         "Работа",         Icons.Filled.Work),
        CategoryInfo("transporte",      "Транспорт",      Icons.Filled.DirectionsCar),
        CategoryInfo("valor",           "Ценность",       Icons.Filled.Grade),
        CategoryInfo("velocidad",       "Скорость",       Icons.Filled.Speed),
        CategoryInfo("viaje",           "Путешествие",    Icons.Filled.Flight),
        CategoryInfo("vida",            "Жизнь",          Icons.Filled.Favorite),
    ).associateBy { it.key }

    fun infoFor(key: String): CategoryInfo =
        if (key == "all") ALL
        else MAP[key] ?: CategoryInfo(
            key = key,
            label = key.replaceFirstChar { it.titlecase() },
            icon = Icons.Filled.Category
        )
}
