package com.spanishapp.ui.flashcards

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector
import com.spanishapp.R

data class CategoryInfo(val key: String, @StringRes val labelRes: Int, val icon: ImageVector)

object CategoryMeta {

    private val ALL = CategoryInfo("all", R.string.cat_all, Icons.Filled.Apps)

    private val MAP: Map<String, CategoryInfo> = listOf(
        CategoryInfo("acciones",        R.string.cat_acciones,        Icons.Filled.DirectionsRun),
        CategoryInfo("animales",        R.string.cat_animales,        Icons.Filled.Pets),
        CategoryInfo("arte",            R.string.cat_arte,            Icons.Filled.Palette),
        CategoryInfo("auxiliares",      R.string.cat_auxiliares,      Icons.Filled.Extension),
        CategoryInfo("calidad",         R.string.cat_calidad,         Icons.Filled.Star),
        CategoryInfo("cantidad",        R.string.cat_cantidad,        Icons.Filled.Numbers),
        CategoryInfo("casa",            R.string.cat_casa,            Icons.Filled.Home),
        CategoryInfo("ciudad",          R.string.cat_ciudad,          Icons.Filled.LocationCity),
        CategoryInfo("colores",         R.string.cat_colores,         Icons.Filled.ColorLens),
        CategoryInfo("comercio",        R.string.cat_comercio,        Icons.Filled.Storefront),
        CategoryInfo("comida",          R.string.cat_comida,          Icons.Filled.Restaurant),
        CategoryInfo("compras",         R.string.cat_compras,         Icons.Filled.ShoppingCart),
        CategoryInfo("comunicacion",    R.string.cat_comunicacion,    Icons.Filled.Chat),
        CategoryInfo("conocimiento",    R.string.cat_conocimiento,    Icons.AutoMirrored.Filled.MenuBook),
        CategoryInfo("cortesia",        R.string.cat_cortesia,        Icons.Filled.ThumbUp),
        CategoryInfo("cotidiano",       R.string.cat_cotidiano,       Icons.Filled.Today),
        CategoryInfo("creatividad",     R.string.cat_creatividad,     Icons.Filled.Lightbulb),
        CategoryInfo("cuerpo",          R.string.cat_cuerpo,          Icons.Filled.Accessibility),
        CategoryInfo("cultura",         R.string.cat_cultura,         Icons.Filled.TheaterComedy),
        CategoryInfo("deporte",         R.string.cat_deporte,         Icons.Filled.SportsSoccer),
        CategoryInfo("despedidas",      R.string.cat_despedidas,      Icons.AutoMirrored.Filled.Logout),
        CategoryInfo("educacion",       R.string.cat_educacion,       Icons.Filled.School),
        CategoryInfo("emociones",       R.string.cat_emociones,       Icons.Filled.Mood),
        CategoryInfo("entretenimiento", R.string.cat_entretenimiento, Icons.Filled.Movie),
        CategoryInfo("estados",         R.string.cat_estados,         Icons.Filled.EmojiEmotions),
        CategoryInfo("expresiones",     R.string.cat_expresiones,     Icons.Filled.RecordVoiceOver),
        CategoryInfo("familia_personas",R.string.cat_familia_personas,Icons.Filled.FamilyRestroom),
        CategoryInfo("finanzas",        R.string.cat_finanzas,        Icons.Filled.AttachMoney),
        CategoryInfo("fisico",          R.string.cat_fisico,          Icons.Filled.FitnessCenter),
        CategoryInfo("general",         R.string.cat_general,         Icons.Filled.Category),
        CategoryInfo("hotel",           R.string.cat_hotel,           Icons.Filled.Hotel),
        CategoryInfo("lugares",         R.string.cat_lugares,         Icons.Filled.Place),
        CategoryInfo("materiales",      R.string.cat_materiales,      Icons.Filled.Build),
        CategoryInfo("media",           R.string.cat_media,           Icons.Filled.PlayCircle),
        CategoryInfo("modal",           R.string.cat_modal,           Icons.Filled.HelpOutline),
        CategoryInfo("movimiento",      R.string.cat_movimiento,      Icons.Filled.DirectionsWalk),
        CategoryInfo("naturaleza",      R.string.cat_naturaleza,      Icons.Filled.Park),
        CategoryInfo("numeros",         R.string.cat_numeros,         Icons.Filled.Numbers),
        CategoryInfo("orden",           R.string.cat_orden,           Icons.Filled.FormatListNumbered),
        CategoryInfo("pensamiento",     R.string.cat_pensamiento,     Icons.Filled.Psychology),
        CategoryInfo("percepcion",      R.string.cat_percepcion,      Icons.Filled.Visibility),
        CategoryInfo("personal",        R.string.cat_personal,        Icons.Filled.Person),
        CategoryInfo("personas",        R.string.cat_personas,        Icons.Filled.People),
        CategoryInfo("precio",          R.string.cat_precio,          Icons.Filled.LocalOffer),
        CategoryInfo("preguntas",       R.string.cat_preguntas,       Icons.Filled.QuestionMark),
        CategoryInfo("profesiones",     R.string.cat_profesiones,     Icons.Filled.Work),
        CategoryInfo("redes_sociales",  R.string.cat_redes_sociales,  Icons.Filled.Share),
        CategoryInfo("reglas",          R.string.cat_reglas,          Icons.Filled.Gavel),
        CategoryInfo("restaurante",     R.string.cat_restaurante,     Icons.Filled.Restaurant),
        CategoryInfo("ropa",            R.string.cat_ropa,            Icons.Filled.Checkroom),
        CategoryInfo("salud",           R.string.cat_salud,           Icons.Filled.HealthAndSafety),
        CategoryInfo("saludos",         R.string.cat_saludos,         Icons.Filled.WavingHand),
        CategoryInfo("social",          R.string.cat_social,          Icons.Filled.Groups),
        CategoryInfo("sociedad",        R.string.cat_sociedad,        Icons.Filled.Groups),
        CategoryInfo("tamaño",          R.string.cat_tamano,          Icons.Filled.Straighten),
        CategoryInfo("tecnologia",      R.string.cat_tecnologia,      Icons.Filled.Computer),
        CategoryInfo("tiempo",          R.string.cat_tiempo,          Icons.Filled.AccessTime),
        CategoryInfo("trabajo",         R.string.cat_trabajo,         Icons.Filled.Work),
        CategoryInfo("transporte",      R.string.cat_transporte,      Icons.Filled.DirectionsCar),
        CategoryInfo("valor",           R.string.cat_valor,           Icons.Filled.Grade),
        CategoryInfo("velocidad",       R.string.cat_velocidad,       Icons.Filled.Speed),
        CategoryInfo("viaje",           R.string.cat_viaje,           Icons.Filled.Flight),
        CategoryInfo("vida",            R.string.cat_vida,            Icons.Filled.Favorite),
    ).associateBy { it.key }

    fun infoFor(key: String): CategoryInfo =
        if (key == "all") ALL
        else MAP[key] ?: CategoryInfo(
            key = key,
            labelRes = R.string.cat_unknown,
            icon = Icons.Filled.Category
        )
}
