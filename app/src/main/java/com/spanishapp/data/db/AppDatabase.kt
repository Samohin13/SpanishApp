package com.spanishapp.data.db

import android.content.Context
import androidx.room.*
import com.spanishapp.data.db.dao.*
import com.spanishapp.data.db.entity.*

@Database(
    entities = [
        WordEntity::class,
        ConjugationEntity::class,
        LessonEntity::class,
        DialogueEntity::class,
        UserProgressEntity::class,
        ChatMessageEntity::class,
        AchievementEntity::class,
        DailyWordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun conjugationDao(): ConjugationDao
    abstract fun lessonDao(): LessonDao
    abstract fun dialogueDao(): DialogueDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun achievementDao(): AchievementDao
    abstract fun dailyWordDao(): DailyWordDao
}