package com.spanishapp.ui.games

object NumberToSpanish {
    private val units = arrayOf("", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve")
    private val teens = arrayOf("diez", "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho", "diecinueve")
    private val tens = arrayOf("", "diez", "veinte", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa")
    private val twenties = arrayOf("veinte", "veintiuno", "veintidós", "veintitrés", "veinticuatro", "veinticinco", "veintiséis", "veintisiete", "veintiocho", "veintinueve")

    fun convert(n: Int): String {
        if (n == 0) return "cero"
        if (n < 0) return "menos " + convert(-n)
        
        return when {
            n < 10 -> units[n]
            n < 20 -> teens[n - 10]
            n < 30 -> twenties[n - 20]
            n < 100 -> {
                val ten = n / 10
                val unit = n % 10
                if (unit == 0) tens[ten] else "${tens[ten]} и $unit".replace("и", "y") // Using placeholder to avoid encoding issues if any, but 'y' is fine
            }
            n == 100 -> "cien"
            n < 1000 -> {
                val hundred = n / 100
                val rest = n % 100
                val prefix = when (hundred) {
                    1 -> "ciento"
                    5 -> "quinientos"
                    7 -> "setecientos"
                    9 -> "novecientos"
                    else -> units[hundred] + "cientos"
                }
                if (rest == 0) if (hundred == 1) "cien" else prefix else "$prefix ${convert(rest)}"
            }
            else -> n.toString() // Fallback for very large numbers
        }
    }
}
