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
    @ColumnInfo(name = "verb_subtype") val verbSubtype: String = "",
    // ── Per-word rating cooldown (added in v17) ─────────────
    // Timestamp последнего применения skill_rating от этого слова.
    // Защита от гринда: одно слово даёт рейтинг максимум раз в 24ч.
    @ColumnInfo(name = "last_rating_at") val lastRatingAt: Long = 0L
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
    indices = [
        Index(value = ["list_id", "word_id"], unique = true),
        Index(value = ["word_id"])
    ]
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
    @ColumnInfo(name = "sync_token") val syncToken: String = "",
    // ── Rating system (v9 added, v20: старт с 0 вместо 1000) ─────
    @ColumnInfo(name = "skill_rating") val skillRating: Int = 0,
    @ColumnInfo(name = "peak_skill_rating") val peakSkillRating: Int = 0,
    @ColumnInfo(name = "last_rating_update") val lastRatingUpdate: Long = 0L,
    @ColumnInfo(name = "current_league") val currentLeague: Int = 1,
    @ColumnInfo(name = "peak_league") val peakLeague: Int = 1,
    // v1.25.87: AUTO-ENROLL в leaderboard. Раньше юзер должен был сам тыкнуть
    // «Присоединиться» — большинство тестеров не доходили (23 тестера → 2 в
    // leaderboard). Теперь новые юзеры автоматически в рейтинге. Opt-out через
    // кнопку «Выйти из лидерборда» в LeaderboardScreen.
    @ColumnInfo(name = "leaderboard_opt_in") val leaderboardOptIn: Boolean = true,
    // ── Streak freezes (added in v14) ────────────────────────
    @ColumnInfo(name = "streak_freezes_available") val streakFreezesAvailable: Int = 2,
    @ColumnInfo(name = "last_streak_update_date") val lastStreakUpdateDate: String = "",
    @ColumnInfo(name = "weekly_freeze_reset_date") val weeklyFreezeResetDate: String = "",
    // ── Daily rating cap (added in v16) ──────────────────────
    // Tracks how much rating the user has gained today so we can cap it
    // (prevents marathon-grinding Madrid in a single weekend).
    @ColumnInfo(name = "daily_rating_gain") val dailyRatingGain: Int = 0,
    @ColumnInfo(name = "daily_rating_gain_date") val dailyRatingGainDate: String = "",
    // ── Word-of-Day streak (added in v19) ────────────────────
    // Отдельный счётчик «дней подряд закрепления Слова дня».
    // Не путать с currentStreak (любая активность). wodLastDate —
    // эпохальный день (UTC), 0 = ни разу не закрепляли.
    @ColumnInfo(name = "wod_streak") val wodStreak: Int = 0,
    @ColumnInfo(name = "wod_longest_streak") val wodLongestStreak: Int = 0,
    @ColumnInfo(name = "wod_last_date") val wodLastDate: Long = 0L,
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "session_id") val sessionId: String = "default",
    @ColumnInfo(name = "correction_json") val correctionJson: String = "",
    // v1.25.0 (db v28): voice messages — путь к .m4a файлу в filesDir
    // и длительность для UI. Если null → обычное текстовое сообщение.
    @ColumnInfo(name = "audio_path") val audioPath: String? = null,
    @ColumnInfo(name = "audio_duration_ms") val audioDurationMs: Long = 0L,
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
    @PrimaryKey @ColumnInfo(name = "lesson_key") val lessonKey: String,  // "u1_l0", "u5_l2", …
    @ColumnInfo(name = "unit_id") val unitId: Int,
    @ColumnInfo(name = "lesson_index") val lessonIndex: Int,
    @ColumnInfo(name = "completed_at") val completedAt: Long = System.currentTimeMillis()
)

/**
 * История ВСЕХ прохождений уроков (включая повторы). Добавлена в v26 для
 * экрана Stats — там нужно считать «уроков за неделю» с учётом повторов,
 * тогда как `lesson_progress` хранит только первое прохождение каждого
 * уникального урока (для ачивок «N уроков завершено»).
 *
 *  • `lesson_progress` (PK = lesson_key) — уникальные уроки, для ачивок
 *  • `lesson_completion_history` (PK = autoincrement id) — все события
 */
@Entity(
    tableName = "lesson_completion_history",
    indices = [androidx.room.Index(value = ["completed_at"])],
)
data class LessonCompletionEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "lesson_key")   val lessonKey: String,
    @ColumnInfo(name = "completed_at") val completedAt: Long = System.currentTimeMillis(),
)

/**
 * Per-activity time log (добавлен в v27 для честного breakdown в Stats).
 *
 * Одна сессия = один заход юзера в учебный экран. `activity_type` —
 * одна из: LESSON / FLASHCARDS / GAME / BOOK / CHAT. Радио НЕ пишется
 * сюда — у него отдельный источник (`radio_listening_session`).
 *
 * Stats screen использует `SUM((ended_at - started_at) / 60_000)` по
 * `activity_type` с фильтром `started_at >= periodStart` чтобы дать
 * реальную картину «на что ушло время» вместо эмпирических baseline.
 */
@Entity(
    tableName = "activity_time_log",
    indices = [
        androidx.room.Index(value = ["started_at"]),
        androidx.room.Index(value = ["activity_type"]),
    ],
)
data class ActivityTimeLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "activity_type") val activityType: String,
    @ColumnInfo(name = "started_at")    val startedAt: Long,
    @ColumnInfo(name = "ended_at")      val endedAt: Long,
)

@Entity(tableName = "article_words")
data class ArticleWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val article: String,                                       // "el" / "la" / "los" / "las"
    val level: String,                                          // CEFR: A1, A2, B1, B2, C1
    @ColumnInfo(name = "rule_hint") val ruleHint: String,
    @ColumnInfo(name = "error_weight") var errorWeight: Int = 0,
    @ColumnInfo(name = "level_num") val levelNum: Int = 0,      // 1..100 — игровой уровень
    @ColumnInfo(name = "position") val position: Int = 0,       // 0..9 — порядок внутри уровня
    @ColumnInfo(name = "is_plural") val isPlural: Boolean = false,
    val russian: String = "",
    val block: String = ""                                       // напр. "A1-base", "exceptions-ma"
)

@Entity(tableName = "article_level_progress")
data class ArticleLevelProgressEntity(
    @PrimaryKey val levelId: String, // "A1", "A2", "B1", "B2", "C1"
    val stars: Int = 0,
    val isUnlocked: Boolean = false,
    @ColumnInfo(name = "best_score") val bestScore: Int = 0
)

// Прогресс чтения: какие рассказы из Libros прочитаны и с каким результатом
@Entity(tableName = "libro_progress")
data class LibroProgressEntity(
    @PrimaryKey @ColumnInfo(name = "libro_id") val libroId: Int,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false,
    @ColumnInfo(name = "best_score") val bestScore: Int = 0,   // % правильных (0–100)
    @ColumnInfo(name = "completed_at") val completedAt: Long = 0L
)

// Дневное накопление XP — для графика прогресса в Profile.
// Один день = одна строка. Ключ — день в формате yyyy-MM-dd.
@Entity(tableName = "daily_xp")
data class DailyXpEntity(
    @PrimaryKey @ColumnInfo(name = "day") val day: String,  // "2026-05-08"
    @ColumnInfo(name = "xp") val xp: Int = 0,
    @ColumnInfo(name = "minutes") val minutes: Int = 0
)

// Универсальный прогресс уровней игр (100 уровней на каждую игру).
// Композитный ключ: (gameId, levelNum)
@Entity(
    tableName = "game_level_progress",
    primaryKeys = ["game_id", "level_num"]
)
data class GameLevelProgressEntity(
    @ColumnInfo(name = "game_id")    val gameId: String,        // "articles" / "speed" / ...
    @ColumnInfo(name = "level_num")  val levelNum: Int,         // 1..100
    @ColumnInfo(name = "stars")      val stars: Int = 0,        // 0..3
    @ColumnInfo(name = "best_score") val bestScore: Int = 0,    // % правильных (0..100)
    @ColumnInfo(name = "completed_at") val completedAt: Long = 0L
)

/**
 * «Работа над ошибками» (v25) — слова/задания которые юзер сделал неверно.
 *
 * Логика:
 *  • Юзер ошибся в игре → запись добавляется (или updated если уже есть).
 *  • Юзер заходит в режим «работа над ошибками» → видит свои ошибки группами по 5.
 *  • Верный ответ в режиме практики → запись УДАЛЯЕТСЯ из таблицы.
 *  • Неверный → attempts++ , остаётся в таблице.
 *
 * itemId — гибкое поле:
 *  • Articles: испанское слово без артикля ("perro")
 *  • Speed:    word_id из words-таблицы (как строка)
 *  • PalabraMaestra: испанское слово
 *  • Math:     текст выражения ("3 + 5")
 *
 * Композитный ключ (gameId, itemId) — одна запись на каждое слово/задание
 * в каждой игре. Если юзер опять ошибся в том же → просто attempts++.
 */
@Entity(
    tableName = "game_mistakes",
    primaryKeys = ["game_id", "item_id"]
)
data class GameMistakeEntity(
    @ColumnInfo(name = "game_id")     val gameId: String,
    @ColumnInfo(name = "item_id")     val itemId: String,
    /** Доп.поле для отображения: например для Articles это перевод "собака". */
    @ColumnInfo(name = "display_hint") val displayHint: String = "",
    /** Доп.поле для отображения: например "el perro" с артиклем. */
    @ColumnInfo(name = "display_main") val displayMain: String = "",
    @ColumnInfo(name = "attempts")    val attempts: Int = 1,
    @ColumnInfo(name = "added_at")    val addedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_seen_at") val lastSeenAt: Long = System.currentTimeMillis()
)

/**
 * Прогресс прохождения flashcard-сета (Daily Sets system).
 * Один сет = один ID из FlashcardSetData. Mastered count считается на лету
 * по таблице `words` (isLearned), здесь храним только звёзды и timestamp.
 */
@Entity(tableName = "flashcard_set_progress")
data class FlashcardSetProgressEntity(
    @PrimaryKey @ColumnInfo(name = "set_id") val setId: String,
    @ColumnInfo(name = "stars") val stars: Int = 0,                 // 0..3
    @ColumnInfo(name = "best_percent") val bestPercent: Int = 0,    // 0..100
    @ColumnInfo(name = "completed_at") val completedAt: Long = 0L
)

/**
 * История закрепления «Слова дня» (added in v19).
 * Одна запись = одно завершённое прохождение WoD-флоу пользователем.
 * Используется для:
 *   • Расчёта wod_streak (сколько дней подряд)
 *   • Коллекции «Слова, выученные через WoD» в Профиле
 *   • Возможных будущих фич (повторные карточки выученного, статистика)
 */
@Entity(tableName = "wod_history")
data class WodHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "word_id")       val wordId: Int,
    @ColumnInfo(name = "spanish")       val spanish: String,
    @ColumnInfo(name = "russian")       val russian: String,
    @ColumnInfo(name = "level")         val level: String,
    @ColumnInfo(name = "practiced_at")  val practicedAt: Long = System.currentTimeMillis(),
)

/**
 * Прогресс прочтения теории-карточки (added in v21).
 * Одна запись = один открытый и прочитанный TheoryContent.
 *
 * Используется для:
 *   • Карточки в LessonSession показывает «Прочитано / Открыть»
 *   • Spaced repetition: через 1/3/7/30 дней показать «Освежить?»
 *   • Подсчёт «прочитано N/200» в разделе «Теория» в Профиле
 */
@Entity(tableName = "theory_progress")
data class TheoryProgressEntity(
    /** ID урока — ровно как в LessonContentData. Одна запись на одну теорию. */
    @PrimaryKey @ColumnInfo(name = "lesson_id") val lessonId: String,
    /** Когда впервые прочитана (мс эпоха). 0 = ещё не открывал. */
    @ColumnInfo(name = "first_read_at") val firstReadAt: Long = 0L,
    /** Когда последний раз перечитывалась. */
    @ColumnInfo(name = "last_read_at")  val lastReadAt: Long = 0L,
    /** Сколько раз открывал (для статистики). */
    @ColumnInfo(name = "read_count")    val readCount: Int = 0,
)

/**
 * v1.25.28 — «Мой словарный запас».
 *
 * Агрегированное состояние знания слова. Считается VocabAggregator'ом
 * из нескольких источников (WordEntity SM-2, UserWordFrequency чат-usage,
 * lesson_progress, game_mistakes и т.д.) и материализуется здесь для
 * быстрых запросов в VocabScreen.
 *
 * Связь с WordEntity:
 *  - `word` (PK) = lowercase spanish form
 *  - `wordId` = optional FK на words.id (null если слово только в чате,
 *    нет в основном словаре)
 *
 * status:
 *  - "UNKNOWN"    — никогда не встречал (обычно не пишем в эту таблицу)
 *  - "SEEN"       — видел в уроке, не закреплено
 *  - "LEARNING"   — в SM-2 пуле, EF растёт
 *  - "PRODUCING"  — использует сам в чате
 *  - "MASTERED"   — EF≥2.5 + usage≥10
 *
 * См. docs/VOCAB_TRACKING_PLAN.md.
 */
@Entity(
    tableName = "user_vocab_state",
    indices = [
        Index(value = ["status"]),
        Index(value = ["cefr"]),
        Index(value = ["last_seen_at"]),
    ],
)
data class UserVocabStateEntity(
    /** Lowercased испанская форма слова. */
    @PrimaryKey @ColumnInfo(name = "word") val word: String,
    /** FK на words.id если слово в основном словаре, null иначе. */
    @ColumnInfo(name = "word_id") val wordId: Int? = null,
    /** CEFR уровень из WordEntity.level или null. */
    @ColumnInfo(name = "cefr") val cefr: String? = null,
    /** VocabStatus enum как String (для портабельности). */
    @ColumnInfo(name = "status") val status: String,
    /** Итоговый score 0.0 — 1.0 после weighted aggregation. */
    @ColumnInfo(name = "score") val score: Float,
    /** Скольких раз юзер сам написал в чате (из UserWordFrequency unigram). */
    @ColumnInfo(name = "usage_count") val usageCount: Int = 0,
    /** Скольких раз AI поправил юзера (из chat_messages corrections JSON). */
    @ColumnInfo(name = "corrections_count") val correctionsCount: Int = 0,
    /** SM-2 ease factor (1.3 — 2.5+) если слово во флэшкартах. */
    @ColumnInfo(name = "flashcard_ef") val flashcardEf: Float = 0f,
    /** Когда последний раз юзер взаимодействовал со словом (any source). */
    @ColumnInfo(name = "last_seen_at") val lastSeenAt: Long = 0L,
    /** Timestamp последнего пересчёта VocabAggregator'ом. */
    @ColumnInfo(name = "updated_at") val updatedAt: Long = 0L,
)


/**
 * v1.25.78 VERB-4: пул слабых глагольных форм для повторения.
 *
 * После ошибки в Verbos-тренажёре формируется запись (verb+tense+pronounIdx).
 * При успешном повторе errorCount уменьшается; при errorCount<=0 запись
 * удаляется из пула. Экран WeakVerbsScreen предлагает сессию из этого пула
 * (приоритет — последние ошибки).
 */
@Entity(
    tableName = "weak_verbs",
    indices = [
        Index(value = ["last_error_at"]),
        Index(value = ["error_count"]),
    ],
)
data class WeakVerbEntity(
    /** Композитный ключ: "verb|tense|pronounIdx", например "hablar|preterito|2". */
    @PrimaryKey @ColumnInfo(name = "key") val key: String,
    @ColumnInfo(name = "verb") val verb: String,
    @ColumnInfo(name = "tense") val tense: String,
    @ColumnInfo(name = "pronoun_index") val pronounIndex: Int,
    @ColumnInfo(name = "correct_form") val correctForm: String,
    @ColumnInfo(name = "error_count") val errorCount: Int = 1,
    @ColumnInfo(name = "last_error_at") val lastErrorAt: Long = System.currentTimeMillis(),
)
