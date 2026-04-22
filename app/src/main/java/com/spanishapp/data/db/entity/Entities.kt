package com.spanishapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// ─────────────────────────────────────────────────────────────
// WORD  —  core vocabulary unit with SM-2 fields
// ─────────────────────────────────────────────────────────────
@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val spanish: String,
    val russian: String,
    val example: String = "",
    val level: String = "A1",           // A1 | A2 | B1 | B2
    val category: String = "general",
    val wordType: String = "noun",      // noun | verb | adjective | phrase
    val audioUrl: String = "",          // optional remote audio; empty = use TTS

    // ── SM-2 spaced repetition ──────────────────────────────
    val easeFactor: Float = 2.5f,
    val interval: Int = 1,              // days until next review
    val repetitions: Int = 0,
    val nextReview: Long = 0L,          // epoch ms
    val isLearned: Boolean = false,
    val totalReviews: Int = 0,
    val correctReviews: Int = 0
)

// ─────────────────────────────────────────────────────────────
// CONJUGATION  —  verb conjugation tables
// ─────────────────────────────────────────────────────────────
@Entity(tableName = "conjugations")
data class ConjugationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val verb: String,                   // infinitive, e.g. "hablar"
    val tense: String,                  // presente | preterito | futuro | imperfecto | subjuntivo
    val yo: String,
    val tu: String,
    val el: String,
    val nosotros: String,
    val vosotros: String,
    val ellos: String,
    val isIrregular: Boolean = false,
    val note: String = ""               // e.g. "stem-changing e→ie"
)

// ─────────────────────────────────────────────────────────────
// LESSON  —  structured grammar / topic lesson
// ─────────────────────────────────────────────────────────────
@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val topic: String,
    val level: String,
    val contentJson: String,            // JSON: list of LessonBlock (theory + exercises)
    val xpReward: Int = 10,
    val isCompleted: Boolean = false,
    val completedAt: Long = 0L,
    val category: String = "grammar"    // grammar | dialogue | culture | pronunciation
)

// ─────────────────────────────────────────────────────────────
// DIALOGUE  —  situational mini-dialogue
// ─────────────────────────────────────────────────────────────
@Entity(tableName = "dialogues")
data class DialogueEntity(
    @PrimaryKey val id: Int,
    val title: String,                  // "En el restaurante"
    val situation: String,              // context description
    val level: String,
    val linesJson: String,              // JSON: list of DialogueLine
    val isCompleted: Boolean = false,
    val bestScore: Int = 0              // 0–100
)

// ─────────────────────────────────────────────────────────────
// USER PROGRESS  —  single-row profile + gamification
// ─────────────────────────────────────────────────────────────
@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val userId: Int = 1,
    val displayName: String = "Estudiante",
    val totalXp: Int = 0,
    val level: Int = 1,                 // app level 1–50
    val currentStreak: Int = 0,         // days in a row
    val longestStreak: Int = 0,
    val lastStudyDate: Long = 0L,
    val wordsLearned: Int = 0,
    val lessonsCompleted: Int = 0,
    val dialoguesCompleted: Int = 0,
    val totalStudyMinutes: Int = 0,
    val dailyGoalMinutes: Int = 10,
    val currentLevel: String = "A1",    // Spanish level
    val avatarIndex: Int = 0,
    val syncToken: String = ""          // for cloud sync
)

// ─────────────────────────────────────────────────────────────
// CHAT MESSAGE  —  AI conversation history
// ─────────────────────────────────────────────────────────────
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String,                   // "user" | "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sessionId: String = "default",
    val correctionJson: String = ""     // JSON list of GrammarCorrection if role=assistant
)

// ─────────────────────────────────────────────────────────────
// ACHIEVEMENT  —  badges & achievements
// ─────────────────────────────────────────────────────────────
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,         // e.g. "streak_7", "words_100"
    val titleRu: String,
    val descriptionRu: String,
    val iconName: String,               // maps to drawable resource name
    val xpReward: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0L,
    val requirement: Int = 0,           // threshold value
    val requirementType: String = ""    // "streak" | "words" | "lessons" | "xp"
)

// ─────────────────────────────────────────────────────────────
// DAILY WORD  —  word of the day log
// ─────────────────────────────────────────────────────────────
@Entity(tableName = "daily_words")
data class DailyWordEntity(
    @PrimaryKey val date: String,       // "2025-04-21"
    val wordId: Int,
    val wasPracticed: Boolean = false
)