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
        LibroProgressEntity::class,
        GameLevelProgressEntity::class,
        DailyXpEntity::class,
        FlashcardSetProgressEntity::class,
        RecentSearchEntity::class,
        WeeklyLeagueStateEntity::class,
        WodHistoryEntity::class,
        TheoryProgressEntity::class,
        com.spanishapp.radio.data.RadioFavoriteEntity::class,
        com.spanishapp.radio.data.RadioCatalogEntity::class,
        com.spanishapp.radio.data.RadioListeningSessionEntity::class,
        com.spanishapp.radio.data.RadioWordCatchEntity::class,
        GameMistakeEntity::class,
        LessonCompletionEventEntity::class,
        ActivityTimeLogEntity::class,
        UserVocabStateEntity::class,
        WeakVerbEntity::class,  // v1.25.78 VERB-4
    ],
    version = 32,
    // v1.26.1: включён экспорт схемы (app/schemas/) — с этой версии каждая
    // новая версия БД сохраняет JSON-схему, что даёт MigrationTestHelper-тесты
    // для будущих миграций. Историю v1..v31 восстановить нельзя.
    exportSchema = true
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
    abstract fun gameLevelProgressDao(): GameLevelProgressDao
    abstract fun dailyXpDao(): DailyXpDao
    abstract fun flashcardSetProgressDao(): FlashcardSetProgressDao
    abstract fun recentSearchDao(): RecentSearchDao
    abstract fun weeklyLeagueDao(): WeeklyLeagueDao
    abstract fun wodHistoryDao(): WodHistoryDao
    abstract fun theoryProgressDao(): TheoryProgressDao
    abstract fun radioFavoriteDao(): com.spanishapp.radio.data.RadioFavoriteDao
    abstract fun radioCatalogDao(): com.spanishapp.radio.data.RadioCatalogDao
    abstract fun radioListeningDao(): com.spanishapp.radio.data.RadioListeningDao
    abstract fun gameMistakesDao(): GameMistakesDao
    abstract fun lessonCompletionHistoryDao(): LessonCompletionHistoryDao
    abstract fun activityTimeLogDao(): ActivityTimeLogDao
    abstract fun userVocabStateDao(): UserVocabStateDao
    abstract fun weakVerbDao(): WeakVerbDao  // v1.25.78 VERB-4
    // radioWordCatchDao() удалён в v1.11.7 — фича «Поймал слово!» выпилена в v1.9.0.
    // Абстрактный метод оставался без Hilt-провайдера → ЛЮБОЙ @Inject его =
    // crash на старте (Dagger graph MissingBinding). Entity RadioWordCatchEntity
    // оставлена в @Database — таблица в БД пустая, удалять ради 0 байт не стоит риска.

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

        // ── v9: рейтинговая система (skill_rating, peak, lastUpdate, league, opt-in) ──
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_progress ADD COLUMN skill_rating INTEGER NOT NULL DEFAULT 1000")
                db.execSQL("ALTER TABLE user_progress ADD COLUMN peak_skill_rating INTEGER NOT NULL DEFAULT 1000")
                db.execSQL("ALTER TABLE user_progress ADD COLUMN last_rating_update INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_progress ADD COLUMN current_league INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_progress ADD COLUMN peak_league INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_progress ADD COLUMN leaderboard_opt_in INTEGER NOT NULL DEFAULT 0")
            }
        }

        // ── v10: универсальная таблица прогресса 100 уровней на игру ──
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS game_level_progress (
                        game_id TEXT NOT NULL,
                        level_num INTEGER NOT NULL,
                        stars INTEGER NOT NULL DEFAULT 0,
                        best_score INTEGER NOT NULL DEFAULT 0,
                        completed_at INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(game_id, level_num)
                    )
                """.trimIndent())
            }
        }

        // ── v11: пересобираем article_words под 100-уровневую структуру ──
        // Старая таблица была сидирована автогенерацией из словаря с RANDOM.
        // Новая — детерминированная: levelNum (1..100), position (0..9), is_plural, russian, block.
        // Прогресс уровней (article_level_progress) — отдельная таблица, не трогаем.
        // ВАЖНО: схема CREATE TABLE должна 1-в-1 совпадать с тем, что Room генерирует
        // для @Entity, иначе Room упадёт с "Migration didn't properly handle".
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS article_words")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS article_words (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        word TEXT NOT NULL,
                        article TEXT NOT NULL,
                        level TEXT NOT NULL,
                        rule_hint TEXT NOT NULL,
                        error_weight INTEGER NOT NULL,
                        level_num INTEGER NOT NULL,
                        position INTEGER NOT NULL,
                        is_plural INTEGER NOT NULL,
                        russian TEXT NOT NULL,
                        block TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        // ── v12: дневная история XP для графика прогресса в Profile ──
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS daily_xp (
                        day TEXT PRIMARY KEY NOT NULL,
                        xp INTEGER NOT NULL DEFAULT 0,
                        minutes INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        // ── v14: streak freezes (2 в неделю, восполняются по понедельникам) ──
        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_progress ADD COLUMN streak_freezes_available INTEGER NOT NULL DEFAULT 2")
                db.execSQL("ALTER TABLE user_progress ADD COLUMN last_streak_update_date TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_progress ADD COLUMN weekly_freeze_reset_date TEXT NOT NULL DEFAULT ''")
            }
        }

        // ── v15: история открытий слов в словаре (для бенто-плитки на главной) ──
        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS recent_searches (
                        wordId INTEGER PRIMARY KEY NOT NULL,
                        opened_at INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        // ── v16: daily rating cap fields on user_progress ──
        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_progress ADD COLUMN daily_rating_gain INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_progress ADD COLUMN daily_rating_gain_date TEXT NOT NULL DEFAULT ''")
            }
        }

        // ── v17: per-word rating cooldown (one rating event per word per 24h) ──
        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE words ADD COLUMN last_rating_at INTEGER NOT NULL DEFAULT 0")
            }
        }

        // ── v18: Weekly Leagues (Duolingo-style 30-person cohorts) ──
        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS weekly_league_state (
                        userId INTEGER PRIMARY KEY NOT NULL,
                        current_tier INTEGER NOT NULL DEFAULT 1,
                        current_week_start TEXT NOT NULL DEFAULT '',
                        current_week_xp INTEGER NOT NULL DEFAULT 0,
                        cohort_id TEXT NOT NULL DEFAULT '',
                        last_finalized_week TEXT NOT NULL DEFAULT '',
                        opted_in INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        // ── v21: theory_progress — справочник под каждый практический урок ──
        // Phase 1 нового дизайна курса (1.2.0): теория-карточки.
        // Каждый практический урок получает справочную карточку, прогресс
        // прочтения трекается отдельно от прохождения практики.
        // ── v27: activity_time_log — per-activity timestamps для честного
        //   breakdown в Stats screen. Раньше минуты «на что ушло время»
        //   считались по эмпирическим baseline (lessonsCount * 7 и т.п.),
        //   теперь — реальные суммы из (ended_at - started_at). ──
        val MIGRATION_26_27 = object : Migration(26, 27) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS activity_time_log (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        activity_type TEXT NOT NULL,
                        started_at INTEGER NOT NULL,
                        ended_at INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_activity_time_log_started_at ON activity_time_log(started_at)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_activity_time_log_activity_type ON activity_time_log(activity_type)")
            }
        }

        // ── v28: voice messages — добавляем audio_path + audio_duration_ms ──
        //   к chat_messages для WhatsApp-style голосовых. Nullable путь:
        //   NULL → обычное текстовое; не-NULL → .m4a в filesDir/voice_messages/.
        val MIGRATION_27_28 = object : Migration(27, 28) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE chat_messages ADD COLUMN audio_path TEXT")
                db.execSQL("ALTER TABLE chat_messages ADD COLUMN audio_duration_ms INTEGER NOT NULL DEFAULT 0")
            }
        }

        // ── v29: user_vocab_state — агрегированный словарный запас юзера
        //   (см. docs/VOCAB_TRACKING_PLAN.md, v1.25.28). Исправлено в v1.25.30:
        //   убраны DEFAULT 0 (Entity их не объявляла → schema validation fail).
        val MIGRATION_28_29 = object : Migration(28, 29) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_vocab_state (
                        word TEXT NOT NULL PRIMARY KEY,
                        word_id INTEGER,
                        cefr TEXT,
                        status TEXT NOT NULL,
                        score REAL NOT NULL,
                        usage_count INTEGER NOT NULL,
                        corrections_count INTEGER NOT NULL,
                        flashcard_ef REAL NOT NULL,
                        last_seen_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_user_vocab_state_status ON user_vocab_state(status)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_user_vocab_state_cefr ON user_vocab_state(cefr)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_user_vocab_state_last_seen ON user_vocab_state(last_seen_at)")
            }
        }

        // ── v32: AUTO-ENROLL в leaderboard (v1.25.87). Раньше default=0
        //   (opt-out) → большинство тестеров не доходили до Leaderboard
        //   tab и не нажимали «Присоединиться». Из 23 в лиге было 2.
        //   Теперь default=1 (opt-in) + миграция: всем существующим
        //   юзерам тоже выставляем opt_in=1. Opt-out через кнопку
        //   «Выйти из лидерборда». ──
        val MIGRATION_31_32 = object : Migration(31, 32) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE user_progress SET leaderboard_opt_in = 1")
            }
        }

        // ── v31: weak_verbs — VERB-4 (v1.25.78). Пул ошибочных глагольных
        //   форм для переучивания. Заполняется из VerbViewModel.submitAnswer
        //   при неверном ответе. WeakVerbsScreen читает топ через DAO. ──
        val MIGRATION_30_31 = object : Migration(30, 31) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS weak_verbs (
                        `key` TEXT NOT NULL PRIMARY KEY,
                        verb TEXT NOT NULL,
                        tense TEXT NOT NULL,
                        pronoun_index INTEGER NOT NULL,
                        correct_form TEXT NOT NULL,
                        error_count INTEGER NOT NULL,
                        last_error_at INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_weak_verbs_last_error_at ON weak_verbs(last_error_at)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_weak_verbs_error_count ON weak_verbs(error_count)")
            }
        }

        // ── v30: HOTFIX. Юзеры которые поставили v1.25.28 имеют сломанную
        //   user_vocab_state со схемой DEFAULT 0 → краш schema validation.
        //   Дроп + recreate с правильной схемой. Данных потерять не страшно —
        //   их и не было (worker не успевал отработать т.к. app падал). ──
        val MIGRATION_29_30 = object : Migration(29, 30) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS user_vocab_state")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_vocab_state (
                        word TEXT NOT NULL PRIMARY KEY,
                        word_id INTEGER,
                        cefr TEXT,
                        status TEXT NOT NULL,
                        score REAL NOT NULL,
                        usage_count INTEGER NOT NULL,
                        corrections_count INTEGER NOT NULL,
                        flashcard_ef REAL NOT NULL,
                        last_seen_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_user_vocab_state_status ON user_vocab_state(status)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_user_vocab_state_cefr ON user_vocab_state(cefr)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_user_vocab_state_last_seen ON user_vocab_state(last_seen_at)")
            }
        }

        // ── v26: lesson_completion_history — все события прохождения уроков
        //   (включая повторы) для Stats screen. lesson_progress остаётся
        //   уникальным (для ачивок), эта таблица хранит каждое событие. ──
        val MIGRATION_25_26 = object : Migration(25, 26) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS lesson_completion_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        lesson_key TEXT NOT NULL,
                        completed_at INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_lesson_completion_history_completed_at ON lesson_completion_history(completed_at)")
            }
        }

        // ── v25: game_mistakes — «Работа над ошибками» во всех 4 играх (1.22.0) ──
        val MIGRATION_24_25 = object : Migration(24, 25) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS game_mistakes (
                        game_id      TEXT NOT NULL,
                        item_id      TEXT NOT NULL,
                        display_hint TEXT NOT NULL DEFAULT '',
                        display_main TEXT NOT NULL DEFAULT '',
                        attempts     INTEGER NOT NULL DEFAULT 1,
                        added_at     INTEGER NOT NULL,
                        last_seen_at INTEGER NOT NULL,
                        PRIMARY KEY (game_id, item_id)
                    )
                """.trimIndent())
            }
        }

        // ── v24: radio_listening_session + radio_word_catch (Stats 1.8.0) ──
        val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS radio_listening_session (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        started_at INTEGER NOT NULL,
                        ended_at INTEGER NOT NULL,
                        station_id TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS radio_word_catch (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        caught_at INTEGER NOT NULL,
                        station_id TEXT NOT NULL,
                        word_text TEXT
                    )
                """.trimIndent())
            }
        }

        // ── v23: radio_catalog — динамический каталог (auto-discovery 1.7.0) ──
        val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS radio_catalog (
                        station_id TEXT NOT NULL PRIMARY KEY,
                        short_code TEXT NOT NULL,
                        name TEXT NOT NULL,
                        program TEXT NOT NULL,
                        frequency REAL NOT NULL,
                        country TEXT NOT NULL,
                        genre TEXT NOT NULL,
                        level TEXT NOT NULL,
                        stream_url TEXT NOT NULL,
                        bitrate INTEGER NOT NULL,
                        user_country TEXT NOT NULL,
                        fetched_at INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        // ── v22: radio_favorites — избранные радиостанции (1.6.2) ──
        val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS radio_favorites (
                        station_id TEXT NOT NULL PRIMARY KEY,
                        added_at INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // ВАЖНО: НЕ создавать индекс — @Entity не объявляет его через
                // indices = [], а Room при старте валидирует схему. Лишний индекс
                // вызвал бы IllegalStateException «expected: ... found: ...».
                // Таблица маленькая (≤ 200 записей теорий) — ORDER BY работает быстро без индекса.
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS theory_progress (
                        lesson_id TEXT NOT NULL PRIMARY KEY,
                        first_read_at INTEGER NOT NULL DEFAULT 0,
                        last_read_at INTEGER NOT NULL DEFAULT 0,
                        read_count INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        // ── v20: рейтинг с 0 — обнуление skillRating всем юзерам ──
        // Старая система стартовала с 1000 → юзеры получали бесплатно
        // половину пути до Мадрида. Новая стартует с 0. Мигрируем
        // существующих юзеров через сброс: skillRating = 0, peak = 0,
        // currentLeague = 1 (Aldea). Потеряют прогресс, но получат
        // честный старт по новой системе.
        val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE user_progress SET skill_rating = 0")
                db.execSQL("UPDATE user_progress SET peak_skill_rating = 0")
                db.execSQL("UPDATE user_progress SET current_league = 1")
                db.execSQL("UPDATE user_progress SET peak_league = 1")
                db.execSQL("UPDATE user_progress SET daily_rating_gain = 0")
            }
        }

        // ── v19: WoD streak + история закреплений «Слова дня» ──
        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Поля WoD-стрика в user_progress
                db.execSQL("ALTER TABLE user_progress ADD COLUMN wod_streak INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_progress ADD COLUMN wod_longest_streak INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_progress ADD COLUMN wod_last_date INTEGER NOT NULL DEFAULT 0")
                // Таблица истории
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS wod_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        word_id INTEGER NOT NULL,
                        spanish TEXT NOT NULL,
                        russian TEXT NOT NULL,
                        level TEXT NOT NULL,
                        practiced_at INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_wod_history_practiced_at ON wod_history(practiced_at DESC)")
            }
        }

        // ── v13: прогресс по flashcard-сетам (Daily Sets) ──
        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS flashcard_set_progress (
                        set_id TEXT PRIMARY KEY NOT NULL,
                        stars INTEGER NOT NULL DEFAULT 0,
                        best_percent INTEGER NOT NULL DEFAULT 0,
                        completed_at INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        /**
         * v1.26.1: ЕДИНЫЙ список всех миграций. Раньше он дублировался руками в
         * 7 местах (AppModule + 4 воркера + 2 виджета) — пропуск одной при
         * добавлении новой = краш в release (destructive fallback только в debug).
         * Теперь все точки вызывают `.addMigrations(*ALL_MIGRATIONS)`: одна
         * правка вместо семи. ВАЖНО: массив объявлен ПОСЛЕ всех MIGRATION_*
         * (companion инициализируется сверху вниз — иначе элементы были бы null).
         * При добавлении новой миграции: (1) объявить MIGRATION_x_y выше,
         * (2) дописать её сюда, (3) поднять version у @Database. Всё.
         */
        val ALL_MIGRATIONS: Array<Migration> = arrayOf(
            MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
            MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
            MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13,
            MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17,
            MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21,
            MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24, MIGRATION_24_25,
            MIGRATION_25_26, MIGRATION_26_27, MIGRATION_27_28, MIGRATION_28_29,
            MIGRATION_29_30, MIGRATION_30_31, MIGRATION_31_32,
        )
    }
}
