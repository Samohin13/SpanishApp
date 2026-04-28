package com.spanishapp.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val spanish: String,
    val russian: String,
    val example: String = "",
    val level: String = "A1",
    val category: String = "general",
    @ColumnInfo(name = "word_type") val wordType: String = "noun",
    @ColumnInfo(name = "audio_url") val audioUrl: String = "",
    @ColumnInfo(name = "ease_factor") val easeFactor: Float = 2.5f,
    val interval: Int = 1,
    val repetitions: Int = 0,
    @ColumnInfo(name = "next_review") val nextReview: Long = 0L,
    @ColumnInfo(name = "is_learned") val isLearned: Boolean = false,
    @ColumnInfo(name = "total_reviews") val totalReviews: Int = 0,
    @ColumnInfo(name = "correct_reviews") val correctReviews: Int = 0,
    // "" = обычный, "irregular" = неправильный, "stem" = с изменением корня
    @ColumnInfo(name = "verb_subtype") val verbSubtype: String = ""
)

@Entity(tableName = "conjugations")
data class ConjugationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val verb: String,
    val tense: String,
    val yo: String,
    val tu: String,
    val el: String,
    val nosotros: String,
    val vosotros: String,
    val ellos: String,
    @ColumnInfo(name = "is_irregular") val isIrregular: Boolean = false,
    val note: String = ""
)

// ── Пользовательские списки слов ──────────────────────────────

@Entity(tableName = "word_lists")
data class WordListEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "color_index") val colorIndex: Int = 0,   // 0-7, цвет иконки
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "word_count") val wordCount: Int = 0       // денормализованный счётчик
)

@Entity(
    tableName = "word_list_entries",
    foreignKeys = [
        ForeignKey(entity = WordListEntity::class, parentColumns = ["id"], childColumns = ["list_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = WordEntity::class,     parentColumns = ["id"], childColumns = ["word_id"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["list_id", "word_id"], unique = true)]
)
data class WordListEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "list_id") val listId: Int,
    @ColumnInfo(name = "word_id") val wordId: Int,
    @ColumnInfo(name = "added_at") val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val topic: String,
    val level: String,
    @ColumnInfo(name = "content_json") val contentJson: String,
    @ColumnInfo(name = "xp_reward") val xpReward: Int = 10,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false,
    @ColumnInfo(name = "completed_at") val completedAt: Long = 0L,
    val category: String = "grammar"
)

@Entity(tableName = "dialogues")
data class DialogueEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val situation: String,
    val level: String,
    @ColumnInfo(name = "lines_json") val linesJson: String,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false,
    @ColumnInfo(name = "best_score") val bestScore: Int = 0
)

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val userId: Int = 1,
    @ColumnInfo(name = "display_name") val displayName: String = "Estudiante",
    @ColumnInfo(name = "total_xp") val totalXp: Int = 0,
    val level: Int = 1,
    @ColumnInfo(name = "current_streak") val currentStreak: Int = 0,
    @ColumnInfo(name = "longest_streak") val longestStreak: Int = 0,
    @ColumnInfo(name = "last_study_date") val lastStudyDate: Long = 0L,
    @ColumnInfo(name = "words_learned") val wordsLearned: Int = 0,
    @ColumnInfo(name = "lessons_completed") val lessonsCompleted: Int = 0,
    @ColumnInfo(name = "dialogues_completed") val dialoguesCompleted: Int = 0,
    @ColumnInfo(name = "total_study_minutes") val totalStudyMinutes: Int = 0,
    @ColumnInfo(name = "daily_goal_minutes") val dailyGoalMinutes: Int = 10,
    @ColumnInfo(name = "current_level") val currentLevel: String = "A1",
    @ColumnInfo(name = "avatar_index") val avatarIndex: Int = 0,
    @ColumnInfo(name = "sync_token") val syncToken: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "session_id") val sessionId: String = "default",
    @ColumnInfo(name = "correction_json") val correctionJson: String = ""
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "title_ru") val titleRu: String,
    @ColumnInfo(name = "description_ru") val descriptionRu: String,
    @ColumnInfo(name = "icon_name") val iconName: String,
    @ColumnInfo(name = "xp_reward") val xpReward: Int,
    @ColumnInfo(name = "is_unlocked") val isUnlocked: Boolean = false,
    @ColumnInfo(name = "unlocked_at") val unlockedAt: Long = 0L,
    val requirement: Int = 0,
    @ColumnInfo(name = "requirement_type") val requirementType: String = ""
)

@Entity(tableName = "daily_words")
data class DailyWordEntity(
    @PrimaryKey val date: String,
    @ColumnInfo(name = "word_id") val wordId: Int,
    @ColumnInfo(name = "was_practiced") val wasPracticed: Boolean = false
)

// Прогресс уроков роадмапа: какие уроки из 30 блоков пройдены
@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey val lessonKey: String,           // "u1_l0", "u5_l2", …
    @ColumnInfo(name = "unit_id") val unitId: Int,
    @ColumnInfo(name = "lesson_index") val lessonIndex: Int,
    @ColumnInfo(name = "completed_at") val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "article_level_progress")
data class ArticleLevelProgressEntity(
    @PrimaryKey val levelId: Int, // 1-100
    val stars: Int = 0, // 0-3
    val isUnlocked: Boolean = false,
    @ColumnInfo(name = "best_score") val bestScore: Int = 0
)