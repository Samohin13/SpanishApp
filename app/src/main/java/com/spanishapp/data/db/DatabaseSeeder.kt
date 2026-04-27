package com.spanishapp.data.db

import android.content.Context
import com.spanishapp.data.db.entity.*
import com.spanishapp.data.repository.ConjugationData
import com.spanishapp.data.repository.ConjugationData2
import com.spanishapp.data.repository.ConjugationData3
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
    companion object {
        // Порог для досева. При добавлении новых паков увеличивать.
        // JSON ~1084 + ModernVocab ~55 + ExtendedVocab ~700 +
        // VocabExpansion1 ~1100 + VocabExpansion2 ~1100 + VocabExpansion3 ~1100 = ~5100
        const val VOCAB_TARGET = 5000
    }

    suspend fun seedIfNeeded() = withContext(Dispatchers.IO) {
        seedWords()
        seedConjugations()
        seedUserProgress()
        seedAchievements()
        seedLessons()
        seedDailyWord()
        seedDialogues()
    }

    // ── Vocabulary from assets/spanish_vocab.json ────────────
    // Использует IGNORE-стратегию вставки — безопасно вызывать повторно.
    // Досевает слова если в БД меньше чем VOCAB_TARGET.
    private suspend fun seedWords() {
        val current = db.wordDao().getCount()
        if (current >= VOCAB_TARGET) return

        val json = context.assets.open("spanish_vocab.json")
            .bufferedReader().readText()
        val root = JSONObject(json)
        val words = mutableListOf<WordEntity>()

        fun parseSection(key: String, type: String) {
            if (!root.has(key)) return
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

        parseSection("nouns",        "noun")
        parseSection("verbs",        "verb")
        parseSection("adjectives",   "adjective")
        parseSection("phrases",      "phrase")
        parseSection("adverbs",      "adverb")
        parseSection("prepositions", "preposition")

        words += ModernVocab.entries
        words += ExtendedVocab.entries
        words += VocabExpansion1.entries
        words += VocabExpansion2.entries
        words += VocabExpansion3.entries

        // insertAll с IGNORE — не перезапишет уже существующие слова (прогресс сохранится)
        db.wordDao().insertAll(words)
    }

    // ── Conjugation tables ────────────────────────────────────
    private suspend fun seedConjugations() {
        if (db.conjugationDao().getCount() > 0) return
        db.conjugationDao().insertAll(ConjugationData.getAll() + ConjugationData2.getAll() + ConjugationData3.getAll())
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

    // ── Grammar lessons ───────────────────────────────────────
    private suspend fun seedLessons() {
        if (db.lessonDao().getCount() > 0) return
        db.lessonDao().insertAll(GrammarContent.getAll())
    }

    // ── Dialogues ─────────────────────────────────────────────
    private suspend fun seedDialogues() {
        if (db.dialogueDao().getCount() > 0) return
        db.dialogueDao().insertAll(DialogueContent.getAll())
    }

    // ── Word of the day ───────────────────────────────────────
    private suspend fun seedDailyWord() {
        val today = LocalDate.now().toString()
        if (db.dailyWordDao().getForDate(today) != null) return

        val a1Words = db.wordDao().getA1WordIds()
        if (a1Words.isEmpty()) return
        val dayOfYear = LocalDate.now().dayOfYear
        val wordId = a1Words[dayOfYear % a1Words.size]
        db.dailyWordDao().upsert(DailyWordEntity(date = today, wordId = wordId))
    }
}
