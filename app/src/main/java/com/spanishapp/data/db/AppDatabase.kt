package com.spanishapp.data.db

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        DailyWordEntity::class,
        WordListEntity::class,
        WordListEntryEntity::class
    ],
    version = 2,
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
    abstract fun wordListDao(): WordListDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS word_lists (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        color_index INTEGER NOT NULL DEFAULT 0,
                        created_at INTEGER NOT NULL DEFAULT 0,
                        word_count INTEGER NOT NULL DEFAULT 0
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS word_list_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        list_id INTEGER NOT NULL,
                        word_id INTEGER NOT NULL,
                        added_at INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(list_id) REFERENCES word_lists(id) ON DELETE CASCADE,
                        FOREIGN KEY(word_id) REFERENCES words(id) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_word_list_entries_list_id_word_id ON word_list_entries(list_id, word_id)")
            }
        }
    }
}
