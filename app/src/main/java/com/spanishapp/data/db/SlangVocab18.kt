package com.spanishapp.data.db

import com.spanishapp.data.db.entity.WordEntity

/**
 * SlangVocab18 — Ненормативная лексика и жесткий сленг (18+)
 */
object SlangVocab18 {

    private fun w(es: String, ru: String, level: String = "B1", type: String = "noun") =
        WordEntity(spanish = es, russian = ru, level = level, category = "slang_18", wordType = type)

    val entries: List<WordEntity> get() = words + phrases

    private val words = listOf(
        w("mierda", "дерьмо", "A2"),
        w("joder", "черт возьми / бля (глагол)", "A2", "verb"),
        w("cabrón", "козел / сволочь", "B1"),
        w("gilipollas", "придурок / мудак", "B1"),
        w("cojones", "яйца (мужские) / мужество", "B1"),
        w("coño", "блин / пизда", "B1"),
        w("puta", "шлюха", "A2"),
        w("puto", "чертов / гребаный", "A2", "adjective"),
        w("maricón", "педик / трус", "B2"),
        w("pendejo", "идиот / лох", "B1"),
        w("carajo", "хрен / черт", "B1"),
        w("chingar", "трахаться / портить (Лат. Ам.)", "B1", "verb"),
        w("hostia", "черт / удар (Исп.)", "B1"),
        w("pijo", "пижон / мажор", "B1"),
        w("capullo", "придурок / головка", "B1"),
        w("boludo", "тупица / придурок (Арг.)", "B1"),
        w("culiao", "гребаный (Чили)", "B2"),
        w("guiri", "иностранец (пренебр. Исп.)", "B1"),
        w("tonto del culo", "круглый дурак", "B1")
    )

    private val phrases = listOf(
        w("¡Vete a la mierda!", "пошел ты в задницу", "A2", "phrase"),
        w("¡Me cago en todo!", "проклятье (букв. срал я на всё)", "B1", "phrase"),
        w("¡Qué te den!", "да пошел ты", "B1", "phrase"),
        w("No me jodas", "не беси меня / не ври мне", "B1", "phrase"),
        w("¡A tomar por culo!", "пошло оно всё в жопу", "B2", "phrase"),
        w("Estoy hasta los cojones", "я задолбался / мне всё осточертело", "B2", "phrase"),
        w("Hijo de puta", "сукин сын", "A2", "phrase"),
        w("Me importa un carajo", "мне плевать / мне по хрену", "B1", "phrase"),
        w("Vete al carajo", "иди к черту", "B1", "phrase"),
        w("Manda huevos", "ну и дела (выражение недовольства)", "B2", "phrase")
    )
}
