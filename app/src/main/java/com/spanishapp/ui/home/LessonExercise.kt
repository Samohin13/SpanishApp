package com.spanishapp.ui.home

enum class ExerciseType {
    MULTIPLE_CHOICE,   // 4 варианта, один верный
    FILL_BLANK,        // Вставь пропущенное слово
    TRANSLATE,         // Переведи фразу
    BUILD_SENTENCE     // Составь предложение из слов
}

data class Exercise(
    val type: ExerciseType,
    val instruction: String,
    val question: String,
    val hint: String = "",
    val options: List<String> = emptyList(),   // для MULTIPLE_CHOICE
    val words: List<String> = emptyList(),     // для BUILD_SENTENCE
    val correctAnswer: String,
    val explanation: String = ""
)

data class ExercisePlan(
    val title: String,
    val grammarNote: String = "",
    val exercises: List<Exercise>
)
