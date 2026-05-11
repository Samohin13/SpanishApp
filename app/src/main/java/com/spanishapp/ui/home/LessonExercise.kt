package com.spanishapp.ui.home

enum class ExerciseType {
    MULTIPLE_CHOICE,   // 4 варианта, один верный
    FILL_BLANK,        // Вставь пропущенное слово
    TRANSLATE,         // Переведи фразу
    BUILD_SENTENCE,    // Составь предложение из слов
    SPEAKING,          // Произнеси слово — приложение проверяет

    // ── Phase 1 additions ──
    LISTEN_PICK,       // TTS играет слово → тапнуть нужный из 4 написанных
    ORDER_LETTERS,     // Тайлы букв в случайном порядке → собрать слово

    // ── Phase 2 additions ──
    MATCH_PAIRS,       // 4-6 пар (es↔ru): тап-слева → тап-справа, соединить
    TAP_MISSING_WORD   // Предложение с пропуском → тапнуть из 3 чипов
}

data class Exercise(
    val type: ExerciseType,
    val instruction: String,
    val question: String,
    val hint: String = "",
    val options: List<String> = emptyList(),   // для MULTIPLE_CHOICE, LISTEN_PICK, TAP_MISSING_WORD
    val words: List<String> = emptyList(),     // для BUILD_SENTENCE
    val correctAnswer: String,
    val explanation: String = "",
    val audioText: String = "",                // для LISTEN_PICK — что произносить TTS
    val pairs: List<Pair<String, String>> = emptyList()  // для MATCH_PAIRS — es↔ru
)

data class ExercisePlan(
    val title: String,
    val grammarNote: String = "",
    val exercises: List<Exercise>
)
