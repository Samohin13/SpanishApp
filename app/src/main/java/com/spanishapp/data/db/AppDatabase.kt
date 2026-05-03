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
        WordListEntryEntity::class,
        ArticleLevelProgressEntity::class,
        ArticleWordEntity::class,
        LessonProgressEntity::class,
        LibroProgressEntity::class
    ],
    version = 8,
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
    abstract fun articleGameDao(): ArticleGameDao
    abstract fun lessonProgressDao(): LessonProgressDao
    abstract fun libroProgressDao(): LibroProgressDao

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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Удалить дубликаты: оставить строку с MIN(id) для каждого уникального слова
                db.execSQL("""
                    DELETE FROM words WHERE id NOT IN (
                        SELECT MIN(id) FROM words GROUP BY lower(trim(spanish))
                    )
                """)
                // 2. Добавить колонку verb_subtype для пометок глаголов
                db.execSQL("ALTER TABLE words ADD COLUMN verb_subtype TEXT NOT NULL DEFAULT ''")
                // 3. Пометить неправильные глаголы
                db.execSQL("""
                    UPDATE words SET verb_subtype = 'irregular'
                    WHERE lower(trim(spanish)) IN (
                        'ser','estar','ir','haber','ver','dar','saber',
                        'tener','poder','querer','poner','venir','decir','hacer','traer',
                        'salir','caer','caber','valer','oír','reír','freír','asir',
                        'obtener','mantener','contener','retener','detener','sostener',
                        'componer','proponer','exponer','disponer','oponer','suponer','imponer','reponer',
                        'contradecir','predecir','bendecir','maldecir',
                        'construir','destruir','incluir','excluir','contribuir','distribuir',
                        'disminuir','influir','constituir','sustituir','atribuir','concluir','huir','instruir',
                        'satisfacer','deshacer','rehacer','contraer','distraer','extraer','abstraer','atraer',
                        'proveer','leer','creer','traer','sobresalir','intervenir','convenir','prevenir'
                    )
                    AND word_type = 'verb'
                """)
                // 4. Пометить глаголы с изменением корня (отклоняющиеся)
                db.execSQL("""
                    UPDATE words SET verb_subtype = 'stem'
                    WHERE lower(trim(spanish)) IN (
                        'entender','perder','encender','defender','extender',
                        'sentir','preferir','mentir','convertir','divertir','sugerir','requerir',
                        'advertir','herir','consentir','referir','hervir','invertir',
                        'pensar','empezar','comenzar','cerrar','calentar','despertar',
                        'recomendar','atravesar','confesar','negar','sentar','regar','sembrar',
                        'enterrar','gobernar','plegar','apretar','tropezar',
                        'dormir','volver','encontrar','contar','recordar','costar','mostrar',
                        'mover','resolver','devolver','llover','soler','probar','volar','rogar',
                        'oler','morder','envolver','revolver','apostar','almorzar','colgar',
                        'demostrar','consolar','comprobar','renovar','torcer',
                        'pedir','repetir','seguir','servir','elegir','conseguir','perseguir',
                        'vestir','medir','sonreír','corregir','competir','impedir','gemir',
                        'rendir','teñir','ceñir','fregar','jugar'
                    )
                    AND word_type = 'verb'
                    AND verb_subtype = ''
                """)
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS article_level_progress (
                        levelId INTEGER PRIMARY KEY NOT NULL,
                        stars INTEGER NOT NULL DEFAULT 0,
                        isUnlocked INTEGER NOT NULL DEFAULT 0,
                        best_score INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS lesson_progress (
                        lesson_key TEXT PRIMARY KEY NOT NULL,
                        unit_id INTEGER NOT NULL,
                        lesson_index INTEGER NOT NULL,
                        completed_at INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS libro_progress (
                        libro_id INTEGER PRIMARY KEY NOT NULL,
                        is_completed INTEGER NOT NULL DEFAULT 0,
                        best_score INTEGER NOT NULL DEFAULT 0,
                        completed_at INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Пересоздаём lesson_progress — предыдущая миграция могла создать
                // таблицу с неправильными именами колонок (до добавления @ColumnInfo)
                db.execSQL("DROP TABLE IF EXISTS lesson_progress")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS lesson_progress (
                        lesson_key TEXT PRIMARY KEY NOT NULL,
                        unit_id INTEGER NOT NULL,
                        lesson_index INTEGER NOT NULL,
                        completed_at INTEGER NOT NULL DEFAULT 0
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS article_words (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        word TEXT NOT NULL,
                        article TEXT NOT NULL,
                        level TEXT NOT NULL,
                        rule_hint TEXT NOT NULL,
                        error_weight INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_word_list_entries_word_id ON word_list_entries(word_id)")
            }
        }
    }
}
