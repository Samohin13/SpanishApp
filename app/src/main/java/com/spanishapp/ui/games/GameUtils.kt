package com.spanishapp.ui.games

fun stripArticle(word: String): String {
    val articles = listOf("el ", "la ", "un ", "una ", "los ", "las ", "unos ", "unas ")
    var result = word.trim()
    for (article in articles) {
        if (result.lowercase().startsWith(article)) {
            return result.substring(article.length).trim()
        }
    }
    return result
}

fun getCorrectArticle(wordWithArticle: String): String {
    val lower = wordWithArticle.trim().lowercase()
    return when {
        lower.startsWith("el ") -> "el"
        lower.startsWith("la ") -> "la"
        lower.startsWith("un ") -> "un"
        lower.startsWith("una ") -> "una"
        lower.startsWith("los ") -> "los"
        lower.startsWith("las ") -> "las"
        lower.startsWith("unos ") -> "unos"
        lower.startsWith("unas ") -> "unas"
        else -> ""
    }
}

fun guessArticle(word: String): String {
    val w = word.trim().lowercase()
    
    // Исключения (мужской род на -а)
    val masculineExceptions = listOf(
        "día", "mapa", "idioma", "problema", "tema", 
        "sistema", "clima", "planeta", "sofá", "tranvía"
    )
    if (w in masculineExceptions) return "el"
    
    // Исключения (женский род на -о)
    val feminineExceptions = listOf("mano", "radio", "foto", "moto")
    if (w in feminineExceptions) return "la"

    return when {
        w.endsWith("a") || w.endsWith("ción") || w.endsWith("sión") || 
        w.endsWith("dad") || w.endsWith("tad") || w.endsWith("tud") || 
        w.endsWith("umbre") -> "la"
        
        w.endsWith("o") || w.endsWith("or") || w.endsWith("aje") || 
        w.endsWith("an") || w.endsWith("al") || w.endsWith("es") -> "el"
        
        else -> "el" // Fallback
    }
}
