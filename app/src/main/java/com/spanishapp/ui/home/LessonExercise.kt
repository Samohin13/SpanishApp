package com.spanishapp.ui.home

enum class ExerciseType {
    MULTIPLE_CHOICE,   // 4 варианта, один верный
    FILL_BLANK,        // Вставь пропущенное слово
    TRANSLATE,         // Переведи фразу
    BUILD_SENTENCE,    // Составь предложение из слов
    SPEAKING,          // Произнеси слово — приложение проверяет

    // ── Phase 1 additions ──
    LISTEN_PICK,       // TTS играет слово → тапнуть нужный из 4 написанных
    ORDER_LETTERS      // Тайлы букв в случайном порядке → собрать слово
}

data class Exercise(
    val type: ExerciseType,
    val instruction: String,
    val question: String,
    val hint: String = "",
    val options: List<String> = emptyList(),   // для MULTIPLE_CHOICE, LISTEN_PICK
    val words: List<String> = emptyList(),     // для BUILD_SENTENCE
    val correctAnswer: String,
    val explanation: String = "",
    val audioText: String = ""                 // для LISTEN_PICK — что произносить TTS
)

data class ExercisePlan(
    val title: String,
    val grammarNote: String = "",
    val exercises: List<Exercise>
)
