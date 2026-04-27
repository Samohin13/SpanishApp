package com.spanishapp.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class SentenceItem(
    val en: String,
    val es: String,
    val cloze: String,
    val difficulty: Int,
    val audio: String
)

@Singleton
class SentencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }

    val all: List<SentenceItem> by lazy {
        val text = context.assets.open("sentences.json").bufferedReader().readText()
        json.decodeFromString<List<SentenceItem>>(text)
    }

    fun getSession(count: Int = 10, maxDiff: Int = Int.MAX_VALUE): List<SentenceItem> =
        all.filter { it.difficulty <= maxDiff && it.cloze.isNotBlank() }
            .shuffled()
            .take(count)

    fun distractors(exclude: String, count: Int = 3): List<String> =
        all.map { it.cloze }
            .filter { it.isNotBlank() && it.lowercase() != exclude.lowercase() }
            .distinct()
            .shuffled()
            .take(count)
}
