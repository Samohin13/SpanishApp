package com.spanishapp.data.db

import android.content.Context
import com.spanishapp.data.db.entity.*
import com.spanishapp.data.repository.ConjugationData
import com.spanishapp.service.AchievementManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase,
    private val achievementManager: AchievementManager
) {
    suspend fun seedIfNeeded() = withContext(Dispatchers.IO) {
        seedWords()
        seedConjugations()
        seedUserProgress()
        seedAchievements()
        seedDailyWord()
    }

    // ── Vocabulary from assets/spanish_vocab.json ────────────
    private suspend fun seedWords() {
        if (db.wordDao().getCount() > 0) return

        val json = context.assets.open("spanish_vocab.json")
            .bufferedReader().readText()
        val root = JSONObject(json)
        val words = mutableListOf<WordEntity>()

        fun parseSection(key: String, type: String) {
            val arr = root.getJSONArray(key)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                words += WordEntity(
                    spanish  = obj.getString("spanish"),
                    russian  = obj.getString("russian"),
                    example  = obj.optString("example", ""),
                    level    = obj.optString("level", "A1"),
                    category = obj.optString("category", "general"),
                    wordType = type
                )
            }
        }

        parseSection("nouns",      "noun")
        parseSection("verbs",      "verb")
        parseSection("adjectives", "adjective")
        parseSection("phrases",    "phrase")

        words += ModernVocab.entries

        db.wordDao().insertAll(words)
    }

    // ── Conjugation tables ────────────────────────────────────
    private suspend fun seedConjugations() {
        if (db.conjugationDao().getCount() > 0) return
        db.conjugationDao().insertAll(ConjugationData.getAll())
    }

    // ── Default user profile ──────────────────────────────────
    private suspend fun seedUserProgress() {
        val existing = db.userProgressDao().getProgressOnce()
        if (existing != null) return
        db.userProgressDao().insert(
            UserProgressEntity(
                displayName      = "Estudiante",
                dailyGoalMinutes = 10,
                currentLevel     = "A1"
            )
        )
    }

    // ── Achievements ──────────────────────────────────────────
    private suspend fun seedAchievements() {
        if (db.achievementDao().getCount() > 0) return
        db.achievementDao().insertAll(achievementManager.defaultAchievements)
    }

    // ── Word of the day ───────────────────────────────────────
    private suspend fun seedDailyWord() {
        val today = LocalDate.now().toString()
        if (db.dailyWordDao().getForDate(today) != null) return

        // Pick a random A1 word for day 1
        val count = db.wordDao().getCount()
        if (count == 0) return
        val randomId = (1..minOf(count, 50)).random()
        db.dailyWordDao().upsert(DailyWordEntity(date = today, wordId = randomId))
    }
}