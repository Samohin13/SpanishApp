package com.spanishapp.data.db.dao

import androidx.room.*
import com.spanishapp.data.db.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(words: List<WordEntity>)

    @Update
    suspend fun update(word: WordEntity)

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getCount(): Int

    /**
     * Wipes user-generated study state on every word but keeps the dictionary
     * rows themselves. Used by Settings → Reset progress so the user sees a
     * clean slate (no gold cups, no isLearned flags, no SM-2 history).
     */
    @Query("""
        UPDATE words SET
            correct_reviews = 0,
            total_reviews   = 0,
            is_learned      = 0,
            ease_factor     = 2.5,
            repetitions     = 0,
            interval        = 1,
            next_review     = 0,
            last_rating_at  = 0
    """)
    suspend fun resetAllStats()

    @Query("SELECT * FROM words WHERE next_review <= :now ORDER BY next_review ASC LIMIT :limit")
    fun getDueWords(now: Long = System.currentTimeMillis(), limit: Int = 30): Flow<List<WordEntity>>

    @Query("SELECT * FROM words ORDER BY spanish ASC LIMIT :limit")
    fun getAllWords(limit: Int = 12000): Flow<List<WordEntity>>

    // Все испанские слова в нижнем регистре — для дедупликации при досеве
    @Query("SELECT lower(trim(spanish)) FROM words")
    suspend fun getAllSpanishLower(): List<String>

    // Все verb-слова из словаря — для рулз-движка спряжений в тренажёре
    @Query("SELECT spanish FROM words WHERE word_type = 'verb' ORDER BY spanish")
    suspend fun getAllDictionaryVerbs(): List<String>

    @Query("SELECT id FROM words WHERE level = 'A1' ORDER BY id")
    suspend fun getA1WordIds(): List<Int>

    @Query("SELECT * FROM words WHERE total_reviews > 0 ORDER BY RANDOM() LIMIT :limit")
    suspend fun getAllWordsOnce(limit: Int): List<WordEntity>

    /** v1.25.28: ВСЕ слова которые юзер хоть как-то трогал (для VocabAggregator). */
    @Query("SELECT * FROM words WHERE total_reviews > 0 OR is_learned = 1 OR repetitions > 0")
    suspend fun getAllStudiedWords(): List<WordEntity>

    /** v1.25.28: lookup batch — для каждого spanish из chat возвращает WordEntity если есть. */
    @Query("SELECT * FROM words WHERE lower(trim(spanish)) IN (:spanishWords)")
    suspend fun getWordsBySpanishBatch(spanishWords: List<String>): List<WordEntity>

    // Случайные слова БЕЗ фильтра по прогрессу — для игр (работает с нуля)
    @Query("SELECT * FROM words ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomWords(limit: Int): List<WordEntity>

    // Детерминированная выборка для кроссворда: фиксированный порядок по id,
    // перемешивается в Kotlin с seed = level → один уровень = один кроссворд всегда
    @Query("SELECT * FROM words ORDER BY id ASC LIMIT :limit")
    suspend fun getWordsOrdered(limit: Int): List<WordEntity>

    // Скользящее окно для кроссворда: каждый уровень получает свой уникальный срез
    @Query("SELECT * FROM words ORDER BY id ASC LIMIT :limit OFFSET :offset")
    suspend fun getWordsOrderedWithOffset(limit: Int, offset: Int): List<WordEntity>

    // Скользящее окно с фильтром по CEFR-уровню — для кроссворда
    @Query("SELECT * FROM words WHERE level IN (:levels) ORDER BY id ASC LIMIT :limit OFFSET :offset")
    suspend fun getWordsByCefrWithOffset(levels: List<String>, limit: Int, offset: Int): List<WordEntity>

    // Для виджета (синхронный вызов на allowMainThreadQueries)
    @Query("SELECT * FROM words WHERE level = :level ORDER BY id ASC")
    fun getWordsByLevelSync(level: String): List<WordEntity>

    @Query("SELECT * FROM words WHERE repetitions = 0 AND level = :level ORDER BY RANDOM() LIMIT :limit")
    fun getNewWords(level: String, limit: Int = 10): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE word_type = :type ORDER BY RANDOM() LIMIT :limit")
    fun getByType(type: String, limit: Int = 50): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE level = :level AND word_type = :type ORDER BY RANDOM() LIMIT :limit")
    fun getByLevelAndType(level: String, type: String, limit: Int = 30): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE category = :category ORDER BY RANDOM() LIMIT :limit")
    fun getByCategory(category: String, limit: Int = 50): Flow<List<WordEntity>>

    // SQLite's LIKE is ASCII-only case-insensitive by default — Cyrillic
     // queries miss capitalized matches ("Дом" vs "дом"). Folding both sides
     // with lower() fixes Russian search at the cost of one extra pass per row.
    @Query("""
        SELECT * FROM words
        WHERE lower(spanish) LIKE '%' || lower(:q) || '%'
           OR lower(russian) LIKE '%' || lower(:q) || '%'
        ORDER BY CASE WHEN lower(spanish) LIKE lower(:q) || '%' THEN 0 ELSE 1 END
        LIMIT 80
    """)
    fun search(q: String): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE lower(trim(spanish)) = :q LIMIT 1")
    suspend fun findBySpanish(q: String): WordEntity?

    /** Lookup a batch of words by their Spanish surface forms (already lowercased). */
    @Query("SELECT * FROM words WHERE lower(trim(spanish)) IN (:words)")
    suspend fun findBySpanishMany(words: List<String>): List<WordEntity>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getById(id: Int): WordEntity?

    @Query("SELECT * FROM words WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): WordEntity?

    /** Per-word rating cooldown: stamps last time this word granted skill_rating. */
    @Query("UPDATE words SET last_rating_at = :ts WHERE id = :wordId")
    suspend fun updateLastRatingAt(wordId: Int, ts: Long)

    /** Patch a word's Russian translation by its Spanish surface form. */
    @Query("UPDATE words SET russian = :russian WHERE lower(trim(spanish)) = lower(trim(:spanish))")
    suspend fun patchRussian(spanish: String, russian: String)

    @Query("SELECT COUNT(*) FROM words WHERE is_learned = 1")
    fun learnedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM words WHERE word_type = :type AND is_learned = 1")
    fun learnedCountByType(type: String): Flow<Int>

    /** Stats — слова «в работе»: уже видели (repetitions>0), но ещё не закреплены SM-2. */
    @Query("SELECT COUNT(*) FROM words WHERE repetitions > 0 AND is_learned = 0")
    fun inProgressCount(): Flow<Int>

    /** Stats — слова «не тронуты»: total_reviews=0 (юзер их ни разу не видел). */
    @Query("SELECT COUNT(*) FROM words WHERE total_reviews = 0")
    fun untouchedCount(): Flow<Int>

    @Query("SELECT DISTINCT category FROM words WHERE word_type = :type ORDER BY category")
    fun categoriesForType(type: String): Flow<List<String>>

    @Query("SELECT * FROM words WHERE total_reviews > 3 AND (correct_reviews * 1.0 / total_reviews) < 0.6 ORDER BY (correct_reviews * 1.0 / total_reviews) ASC LIMIT 20")
    fun getWeakWords(): Flow<List<WordEntity>>

    // ── Flashcards session helpers (suspend, one-shot) ─────────
    @Query("""
        SELECT * FROM words
        WHERE next_review <= :now
          AND level = :level
          AND (:category = 'all' OR category = :category)
          AND repetitions > 0
        ORDER BY next_review ASC
        LIMIT :limit
    """)
    suspend fun getDueForSession(
        level: String,
        category: String,
        limit: Int,
        now: Long = System.currentTimeMillis()
    ): List<WordEntity>

    @Query("""
        SELECT * FROM words
        WHERE repetitions = 0
          AND level = :level
          AND (:category = 'all' OR category = :category)
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getNewForSession(
        level: String,
        category: String,
        limit: Int
    ): List<WordEntity>

    @Query("""
        SELECT * FROM words
        WHERE total_reviews > 2
          AND (correct_reviews * 1.0 / total_reviews) < 0.6
          AND (:category = 'all' OR category = :category)
        ORDER BY (correct_reviews * 1.0 / total_reviews) ASC
        LIMIT :limit
    """)
    suspend fun getWeakForSession(category: String, limit: Int): List<WordEntity>

    @Query("SELECT DISTINCT category FROM words WHERE level = :level ORDER BY category")
    suspend fun categoriesForLevel(level: String): List<String>

    /** Count of "weak" words across all levels (accuracy < 60%, ≥3 reviews). */
    @Query("""
        SELECT COUNT(*) FROM words
        WHERE total_reviews > 2
          AND (correct_reviews * 1.0 / total_reviews) < 0.6
    """)
    suspend fun countWeakAll(): Int

    /** Количество слов в пуле практики (точность < 60%, ≥1 повторение). */
    @Query("""
        SELECT COUNT(*) FROM words
        WHERE total_reviews > 0
          AND (correct_reviews * 1.0 / total_reviews) < 0.6
    """)
    suspend fun countPracticePool(): Int

    /**
     * Smarter distractor query: prefer words from the SAME category at the
     * same level (so "De nada" / category=expressions doesn't get distractors
     * like "сердце" / "Средиземное море"). Falls back to any same-level word
     * if the category has too few entries.
     */
    @Query("""
        SELECT * FROM words
        WHERE level = :level AND id != :excludeId AND category = :category
        ORDER BY RANDOM() LIMIT :limit
    """)
    suspend fun randomDistractorsSameCategory(
        level: String,
        category: String,
        excludeId: Int,
        limit: Int
    ): List<WordEntity>

    /** Count of "due" words (need review per SM-2: nextReview ≤ now). */
    @Query("""
        SELECT COUNT(*) FROM words
        WHERE total_reviews > 0 AND next_review > 0 AND next_review <= :now
    """)
    suspend fun countDue(now: Long): Int

    /** Random distractors at the same level (used to build multiple-choice options). */
    @Query("""
        SELECT * FROM words
        WHERE level = :level AND id != :excludeId
        ORDER BY RANDOM() LIMIT :limit
    """)
    suspend fun randomDistractors(level: String, excludeId: Int, limit: Int): List<WordEntity>

    /** All weak words (no category filter), ordered worst-first. */
    @Query("""
        SELECT * FROM words
        WHERE total_reviews > 2
          AND (correct_reviews * 1.0 / total_reviews) < 0.6
        ORDER BY (correct_reviews * 1.0 / total_reviews) ASC
        LIMIT :limit
    """)
    suspend fun getAllWeak(limit: Int): List<WordEntity>

    /**
     * Practice pool — слабые слова (точность < 60%).
     * Практика работает с тем, что плохо знаешь, а не с тем, что уже выучил.
     */
    @Query("""
        SELECT * FROM words
        WHERE total_reviews > 0
          AND (correct_reviews * 1.0 / total_reviews) < 0.6
        ORDER BY (correct_reviews * 1.0 / total_reviews) ASC, total_reviews DESC
        LIMIT :limit
    """)
    suspend fun getPracticePool(limit: Int): List<WordEntity>

    /** Шаткие слова (60–80%) — фоллбэк, если слабых мало. */
    @Query("""
        SELECT * FROM words
        WHERE total_reviews > 0
          AND (correct_reviews * 1.0 / total_reviews) >= 0.6
          AND (correct_reviews * 1.0 / total_reviews) < 0.8
        ORDER BY (correct_reviews * 1.0 / total_reviews) ASC
        LIMIT :limit
    """)
    suspend fun getShakyPool(limit: Int): List<WordEntity>

    /** Любые просмотренные слова — крайний фоллбэк для новых юзеров. */
    @Query("""
        SELECT * FROM words WHERE total_reviews > 0
        ORDER BY total_reviews ASC LIMIT :limit
    """)
    suspend fun getAnyReviewedPool(limit: Int): List<WordEntity>

    // ── Level mastery (for unlock progression) ────────────────
    @Query("SELECT COUNT(*) FROM words WHERE level = :level")
    suspend fun countByLevel(level: String): Int

    @Query("""
        SELECT COUNT(*) FROM words
        WHERE level = :level
          AND repetitions > 0
          AND interval >= :minIntervalDays
    """)
    suspend fun countMasteredByLevel(level: String, minIntervalDays: Int = 7): Int

    @Query("SELECT COUNT(*) FROM words WHERE level = :level AND category = :category")
    suspend fun countByLevelAndCategory(level: String, category: String): Int

    @Query("""
        SELECT COUNT(*) FROM words
        WHERE level = :level
          AND category = :category
          AND repetitions > 0
          AND interval >= :minIntervalDays
    """)
    suspend fun countMasteredByLevelAndCategory(level: String, category: String, minIntervalDays: Int = 7): Int

    // ── Mastery rating: aggregate per category ───────────────
    @Query("""
        SELECT category AS category,
               COUNT(*) AS total,
               SUM(CASE WHEN is_learned = 1 THEN 1 ELSE 0 END) AS learned,
               COALESCE(SUM(total_reviews), 0) AS totalReviews,
               COALESCE(SUM(correct_reviews), 0) AS correctReviews
        FROM words
        GROUP BY category
    """)
    suspend fun getCategoryStats(): List<CategoryStatsRow>

    @Query("""
        SELECT category AS category,
               COUNT(*) AS total,
               SUM(CASE WHEN is_learned = 1 THEN 1 ELSE 0 END) AS learned,
               COALESCE(SUM(total_reviews), 0) AS totalReviews,
               COALESCE(SUM(correct_reviews), 0) AS correctReviews
        FROM words
        WHERE level = :level
        GROUP BY category
    """)
    suspend fun getCategoryStatsForLevel(level: String): List<CategoryStatsRow>
}

data class CategoryStatsRow(
    val category: String,
    val total: Int,
    val learned: Int,
    val totalReviews: Int,
    val correctReviews: Int
)

@Dao
interface ConjugationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(conjugations: List<ConjugationEntity>)

    @Query("SELECT * FROM conjugations WHERE verb = :verb ORDER BY tense")
    fun getForVerb(verb: String): Flow<List<ConjugationEntity>>

    @Query("SELECT * FROM conjugations WHERE tense = :tense ORDER BY RANDOM() LIMIT :limit")
    fun getByTense(tense: String, limit: Int = 10): Flow<List<ConjugationEntity>>

    @Query("SELECT DISTINCT verb FROM conjugations ORDER BY verb")
    fun getAllVerbs(): Flow<List<String>>

    @Query("SELECT DISTINCT verb FROM conjugations WHERE is_irregular = 1 ORDER BY verb")
    fun getIrregularVerbs(): Flow<List<String>>

    @Query("SELECT * FROM conjugations WHERE is_irregular = 1 ORDER BY RANDOM() LIMIT :limit")
    fun getIrregular(limit: Int = 20): Flow<List<ConjugationEntity>>

    @Query("SELECT * FROM conjugations")
    suspend fun getAll(): List<ConjugationEntity>

    @Query("SELECT COUNT(*) FROM conjugations")
    suspend fun getCount(): Int
}

@Dao
interface LessonDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(lessons: List<LessonEntity>)

    @Update
    suspend fun update(lesson: LessonEntity)

    @Query("SELECT * FROM lessons WHERE level = :level ORDER BY id")
    fun getByLevel(level: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE is_completed = 0 ORDER BY level, id LIMIT 5")
    fun getNextLessons(): Flow<List<LessonEntity>>

    @Query("SELECT COUNT(*) FROM lessons WHERE is_completed = 1")
    fun completedCount(): Flow<Int>

    @Query("SELECT * FROM lessons WHERE id = :id")
    suspend fun getById(id: Int): LessonEntity?

    @Query("SELECT COUNT(*) FROM lessons")
    suspend fun getCount(): Int
}

@Dao
interface DialogueDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(dialogues: List<DialogueEntity>)

    @Update
    suspend fun update(dialogue: DialogueEntity)

    @Query("SELECT * FROM dialogues WHERE level = :level ORDER BY id")
    fun getByLevel(level: String): Flow<List<DialogueEntity>>

    @Query("SELECT * FROM dialogues WHERE id = :id")
    suspend fun getById(id: Int): DialogueEntity?

    @Query("SELECT COUNT(*) FROM dialogues WHERE is_completed = 1")
    fun completedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM dialogues")
    suspend fun getCount(): Int
}

@Dao
interface UserProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: UserProgressEntity)

    @Update
    suspend fun update(progress: UserProgressEntity)

    @Query("SELECT * FROM user_progress LIMIT 1")
    fun getProgress(): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_progress LIMIT 1")
    suspend fun getProgressOnce(): UserProgressEntity?

    // v1.25.97 FIX (audit C1): убран `lessons_completed = lessons_completed + 1`.
    // Этот метод — единственный write-path XpTracker.add(), который зовут ВСЕ
    // источники XP (игры, книги, теория, WOD, произношение, флэшкарты...).
    // Каждый из них инкрементил фейковый «+1 урок»: ачивки lesson_first/lesson_10
    // разблокировались за минуты, реальный урок считался дважды
    // (LessonIntroViewModel ведёт счётчик явно), статистика/Firestore-синк врали.
    @Query("UPDATE user_progress SET total_xp = total_xp + :xp, words_learned = words_learned + :words")
    suspend fun addXpAndWords(xp: Int, words: Int)

    @Query("UPDATE user_progress SET current_streak = :streak, longest_streak = MAX(longest_streak, :streak), last_study_date = :date")
    suspend fun updateStreak(streak: Int, date: Long)

    @Query("UPDATE user_progress SET total_study_minutes = total_study_minutes + :minutes")
    suspend fun addStudyTime(minutes: Int)

    // ── Rating system ────────────────────────────────────────
    @Query("""
        UPDATE user_progress SET
            skill_rating = :rating,
            peak_skill_rating = MAX(peak_skill_rating, :rating),
            last_rating_update = :ts,
            current_league = :league,
            peak_league = MAX(peak_league, :league)
    """)
    suspend fun updateSkillRating(rating: Int, league: Int, ts: Long)

    /** Обновляет дневной счётчик прироста рейтинга (для daily cap). */
    @Query("""
        UPDATE user_progress SET
            daily_rating_gain      = :addedToday,
            daily_rating_gain_date = :date
    """)
    suspend fun bumpDailyRatingGain(date: String, addedToday: Int)

    @Query("UPDATE user_progress SET leaderboard_opt_in = :enabled")
    suspend fun setLeaderboardOptIn(enabled: Boolean)

    @Query("UPDATE user_progress SET display_name = :name")
    suspend fun updateDisplayName(name: String)

    // ── Streak system v2 (freezes) ──────────────────────────
    @Query("""
        UPDATE user_progress SET
            current_streak = :streak,
            longest_streak = MAX(longest_streak, :streak),
            last_study_date = :lastStudyMs,
            streak_freezes_available = :freezes,
            last_streak_update_date = :lastUpdateDate,
            weekly_freeze_reset_date = :resetDate
    """)
    suspend fun updateStreakFull(
        streak: Int,
        lastStudyMs: Long,
        freezes: Int,
        lastUpdateDate: String,
        resetDate: String
    )

    @Query("UPDATE user_progress SET daily_goal_minutes = :minutes")
    suspend fun updateDailyGoal(minutes: Int)

    // ── Word-of-Day streak (added in v19) ────────────────────
    @Query("""
        UPDATE user_progress SET
            wod_streak = :streak,
            wod_longest_streak = MAX(wod_longest_streak, :streak),
            wod_last_date = :lastDateMs
    """)
    suspend fun updateWodStreak(streak: Int, lastDateMs: Long)
}

@Dao
interface ChatMessageDao {
    @Insert
    suspend fun insert(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_messages WHERE session_id = :sessionId ORDER BY timestamp ASC")
    fun getSession(sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE session_id = :sessionId ORDER BY timestamp ASC")
    suspend fun getSessionOnce(sessionId: String): List<ChatMessageEntity>

    @Query("DELETE FROM chat_messages WHERE session_id = :sessionId")
    suspend fun clearSession(sessionId: String)

    @Query("SELECT DISTINCT session_id FROM chat_messages")
    suspend fun getAllSessions(): List<String>

    /** v1.24.17: метаданные сессий для архива чатов. */
    data class SessionMeta(
        @ColumnInfo(name = "session_id") val sessionId: String,
        @ColumnInfo(name = "msg_count") val msgCount: Int,
        @ColumnInfo(name = "last_ts") val lastTs: Long,
        @ColumnInfo(name = "last_content") val lastContent: String,
    )

    @Query("""
        SELECT session_id,
               COUNT(*) AS msg_count,
               MAX(timestamp) AS last_ts,
               (SELECT content FROM chat_messages m2
                WHERE m2.session_id = m1.session_id
                ORDER BY m2.timestamp DESC LIMIT 1) AS last_content
        FROM chat_messages m1
        GROUP BY session_id
        ORDER BY last_ts DESC
    """)
    fun observeSessionsMeta(): Flow<List<SessionMeta>>

    /** Stats screen — сколько сообщений в чате с указанного момента (любой роли). */
    @Query("SELECT COUNT(*) FROM chat_messages WHERE timestamp >= :since")
    fun observeCountSince(since: Long): Flow<Int>

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAll()
}

@Dao
interface AchievementDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(achievements: List<AchievementEntity>)

    @Update
    suspend fun update(achievement: AchievementEntity)

    // v1.25.97 (audit): атомарный анлок — WHERE is_unlocked = 0 + счётчик
    // затронутых строк. Два конкурентных checkAndUnlock (RatingUpdater на
    // каждый ответ + StreakService + ViewModels) больше не могут разблокировать
    // одну ачивку дважды (двойная нотификация + двойные +5 💡 + двойной XP).
    @Query("UPDATE achievements SET is_unlocked = 1, unlocked_at = :ts WHERE id = :id AND is_unlocked = 0")
    suspend fun unlockIfLocked(id: String, ts: Long): Int

    @Query("SELECT * FROM achievements ORDER BY is_unlocked DESC, xp_reward DESC")
    fun getAll(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE is_unlocked = 0 AND requirement_type = :type")
    suspend fun getLockedByType(type: String): List<AchievementEntity>

    @Query("SELECT COUNT(*) FROM achievements WHERE is_unlocked = 1")
    fun unlockedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getCount(): Int

    /**
     * Обновить текст/иконку/XP по id, СОХРАНИВ is_unlocked и unlocked_at.
     * Используется в seedAchievements при апгрейде версии — чтобы новые
     * описания подтягивались, но прогресс юзера не сбрасывался.
     */
    @Query("""
        UPDATE achievements
        SET title_ru = :title,
            description_ru = :description,
            icon_name = :iconName,
            xp_reward = :xpReward,
            requirement = :requirement,
            requirement_type = :requirementType
        WHERE id = :id
    """)
    suspend fun updateMetaById(
        id: String,
        title: String,
        description: String,
        iconName: String,
        xpReward: Int,
        requirement: Int,
        requirementType: String,
    )

    /** Locks every achievement again. Used by Settings → Reset progress. */
    @Query("UPDATE achievements SET is_unlocked = 0, unlocked_at = 0")
    suspend fun resetAll()
}

@Dao
interface DailyWordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(dailyWord: DailyWordEntity)

    @Query("SELECT * FROM daily_words WHERE date = :date")
    suspend fun getForDate(date: String): DailyWordEntity?

    /**
     * v1.17.8: reactive вариант для HomeViewModel. Нужен чтобы UI обновлялся
     * автоматически когда seedDailyWord()/ensureDailyWordExists() закончит
     * INSERT (после subscribe). Раньше flow {} читал один раз → race condition.
     */
    @Query("SELECT * FROM daily_words WHERE date = :date")
    fun observeForDate(date: String): kotlinx.coroutines.flow.Flow<DailyWordEntity?>

    @Query("UPDATE daily_words SET was_practiced = 1 WHERE date = :date")
    suspend fun markPracticed(date: String)
}

// ── Пользовательские списки ────────────────────────────────────

@Dao
interface WordListDao {

    // ── Списки ────────────────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: WordListEntity): Long

    @Update
    suspend fun updateList(list: WordListEntity)

    @Delete
    suspend fun deleteList(list: WordListEntity)

    @Query("SELECT * FROM word_lists ORDER BY created_at DESC")
    fun getAllLists(): Flow<List<WordListEntity>>

    @Query("SELECT * FROM word_lists ORDER BY created_at DESC")
    suspend fun getAllListsOnce(): List<WordListEntity>

    @Query("SELECT COUNT(*) FROM word_lists")
    suspend fun getListCount(): Int

    @Query("UPDATE word_lists SET word_count = (SELECT COUNT(*) FROM word_list_entries WHERE list_id = :listId) WHERE id = :listId")
    suspend fun refreshWordCount(listId: Int)

    // ── Слова в списке ────────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addEntry(entry: WordListEntryEntity)

    @Query("DELETE FROM word_list_entries WHERE list_id = :listId AND word_id = :wordId")
    suspend fun removeEntry(listId: Int, wordId: Int)

    @Query("SELECT w.* FROM words w INNER JOIN word_list_entries e ON w.id = e.word_id WHERE e.list_id = :listId ORDER BY e.added_at DESC")
    fun getWordsInList(listId: Int): Flow<List<WordEntity>>

    @Query("SELECT w.* FROM words w INNER JOIN word_list_entries e ON w.id = e.word_id WHERE e.list_id = :listId ORDER BY e.added_at DESC")
    suspend fun getWordsInListOnce(listId: Int): List<WordEntity>

    @Query("SELECT COUNT(*) FROM word_list_entries WHERE list_id = :listId AND word_id = :wordId")
    suspend fun isWordInList(listId: Int, wordId: Int): Int

    // Возвращает id всех списков, в которых есть это слово
    @Query("SELECT list_id FROM word_list_entries WHERE word_id = :wordId")
    suspend fun getListIdsForWord(wordId: Int): List<Int>

    @Query("SELECT COUNT(*) FROM word_list_entries WHERE list_id = :listId")
    suspend fun countWordsInList(listId: Int): Int

    @Query("DELETE FROM word_list_entries")
    suspend fun deleteAllEntries()

    @Query("DELETE FROM word_lists")
    suspend fun deleteAllLists()
}

@Dao
interface LessonProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markComplete(progress: LessonProgressEntity)

    @Query("SELECT lesson_key FROM lesson_progress")
    fun getAllCompletedKeys(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM lesson_progress WHERE unit_id = :unitId")
    suspend fun completedCountForUnit(unitId: Int): Int

    @Query("SELECT COUNT(*) > 0 FROM lesson_progress WHERE completed_at >= :since")
    suspend fun anyCompletedSince(since: Long): Boolean

    /** Stats screen — сколько уроков завершено с указанного момента. */
    @Query("SELECT COUNT(*) FROM lesson_progress WHERE completed_at >= :since")
    fun observeCountSince(since: Long): Flow<Int>

    /** Idempotency check для markLessonComplete — был ли этот урок уже отмечен. */
    @Query("SELECT COUNT(*) > 0 FROM lesson_progress WHERE lesson_key = :key")
    suspend fun isAlreadyCompleted(key: String): Boolean

    @Query("DELETE FROM lesson_progress")
    suspend fun deleteAll()
}

/**
 * История ВСЕХ событий завершения уроков (с повторами) — для Stats screen.
 * Отдельно от `lesson_progress` (там только уникальные уроки для ачивок).
 */
@Dao
interface LessonCompletionHistoryDao {
    @Insert
    suspend fun record(event: LessonCompletionEventEntity)

    /** Stats — сколько уроков (включая повторы) завершено с указанного момента. */
    @Query("SELECT COUNT(*) FROM lesson_completion_history WHERE completed_at >= :since")
    fun observeCountSince(since: Long): Flow<Int>

    @Query("DELETE FROM lesson_completion_history")
    suspend fun deleteAll()
}

/**
 * Per-activity time log (Stats breakdown: реальные минуты по типу).
 * Хуки в Composable экранах (TrackActivity) пишут одну сессию на каждый
 * заход юзера через DisposableEffect.onDispose.
 */
@Dao
interface ActivityTimeLogDao {
    @Insert
    suspend fun insert(event: ActivityTimeLogEntity)

    /** Минут по типу активности с указанного момента. NULL→0 через COALESCE. */
    @Query("""
        SELECT COALESCE(SUM(ended_at - started_at), 0) / 60000
        FROM activity_time_log
        WHERE activity_type = :type AND started_at >= :since
    """)
    fun observeMinutesSince(type: String, since: Long): Flow<Long>

    @Query("DELETE FROM activity_time_log")
    suspend fun deleteAll()
}

@Dao
interface ArticleGameDao {
    @Query("SELECT * FROM article_level_progress ORDER BY levelId ASC")
    fun getAllProgress(): Flow<List<ArticleLevelProgressEntity>>

    @Query("SELECT * FROM article_level_progress WHERE levelId = :levelId")
    suspend fun getProgress(levelId: String): ArticleLevelProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: ArticleLevelProgressEntity)

    @Query("UPDATE article_level_progress SET isUnlocked = 1 WHERE levelId = :levelId")
    suspend fun unlockLevel(levelId: String)

    @Query("DELETE FROM article_level_progress")
    suspend fun deleteAllProgress()

    // ── Детерминированный набор слов для уровня (1..100), без RANDOM ──
    @Query("SELECT * FROM article_words WHERE level_num = :levelNum ORDER BY position ASC")
    suspend fun getWordsForGameLevel(levelNum: Int): List<ArticleWordEntity>

    // Совместимость: CEFR-фильтр для старых вызовов (используется тестами/просмотром).
    @Query("SELECT * FROM article_words WHERE level = :level ORDER BY level_num ASC, position ASC LIMIT :limit")
    suspend fun getWordsForLevel(level: String, limit: Int = 10): List<ArticleWordEntity>

    @Query("SELECT * FROM article_words WHERE level IN (:levels) ORDER BY level_num ASC, position ASC LIMIT :limit")
    suspend fun getWordsForLevels(levels: List<String>, limit: Int = 10): List<ArticleWordEntity>

    @Query("SELECT COUNT(*) FROM article_words WHERE level IN (:levels)")
    suspend fun countForLevels(levels: List<String>): Int

    /** Найти ArticleWordEntity по слову (для режима «Работа над ошибками»). */
    @Query("SELECT * FROM article_words WHERE word = :word LIMIT 1")
    suspend fun findByWord(word: String): ArticleWordEntity?

    @Update
    suspend fun updateWord(word: ArticleWordEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWords(words: List<ArticleWordEntity>)

    @Query("SELECT COUNT(*) FROM article_words")
    suspend fun getWordCount(): Int

    @Query("DELETE FROM article_words")
    suspend fun deleteAllWords()
}

@Dao
interface LibroProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: LibroProgressEntity)

    @Query("SELECT * FROM libro_progress")
    fun getAll(): Flow<List<LibroProgressEntity>>

    @Query("SELECT * FROM libro_progress WHERE libro_id = :id")
    suspend fun getById(id: Int): LibroProgressEntity?

    @Query("DELETE FROM libro_progress")
    suspend fun deleteAll()
}

@Dao
interface DailyXpDao {
    /** Прибавить N XP к указанному дню (создаёт строку если её нет). */
    @Query("""
        INSERT INTO daily_xp(day, xp, minutes) VALUES(:day, :amount, 0)
        ON CONFLICT(day) DO UPDATE SET xp = xp + :amount
    """)
    suspend fun addXp(day: String, amount: Int)

    /** Прибавить N минут к указанному дню. */
    @Query("""
        INSERT INTO daily_xp(day, xp, minutes) VALUES(:day, 0, :amount)
        ON CONFLICT(day) DO UPDATE SET minutes = minutes + :amount
    """)
    suspend fun addMinutes(day: String, amount: Int)

    /** Все дни (для графиков). Сортировка по дню по возрастанию. */
    @Query("SELECT * FROM daily_xp ORDER BY day ASC")
    fun observeAll(): Flow<List<DailyXpEntity>>

    /** Последние N дней (включая сегодня). */
    @Query("SELECT * FROM daily_xp WHERE day >= :sinceDay ORDER BY day ASC")
    fun observeSince(sinceDay: String): Flow<List<DailyXpEntity>>

    @Query("DELETE FROM daily_xp")
    suspend fun deleteAll()
}

@Dao
interface GameLevelProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: GameLevelProgressEntity)

    @Query("SELECT * FROM game_level_progress WHERE game_id = :gameId ORDER BY level_num ASC")
    fun observeForGame(gameId: String): Flow<List<GameLevelProgressEntity>>

    @Query("SELECT * FROM game_level_progress WHERE game_id = :gameId ORDER BY level_num ASC")
    suspend fun getForGame(gameId: String): List<GameLevelProgressEntity>

    @Query("SELECT * FROM game_level_progress WHERE game_id = :gameId AND level_num = :level LIMIT 1")
    suspend fun getOne(gameId: String, level: Int): GameLevelProgressEntity?

    @Query("""
        SELECT COALESCE(MAX(level_num), 0) FROM game_level_progress
        WHERE game_id = :gameId AND stars > 0
    """)
    suspend fun maxClearedLevel(gameId: String): Int

    @Query("SELECT COALESCE(SUM(stars), 0) FROM game_level_progress WHERE game_id = :gameId")
    suspend fun totalStars(gameId: String): Int

    /** Stats screen — сколько уровней любых игр пройдено с указанного момента. */
    @Query("SELECT COUNT(*) FROM game_level_progress WHERE completed_at >= :since AND stars > 0")
    fun observeCountSince(since: Long): Flow<Int>

    @Query("DELETE FROM game_level_progress")
    suspend fun deleteAll()
}

@Dao
interface FlashcardSetProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: FlashcardSetProgressEntity)

    @Query("SELECT * FROM flashcard_set_progress")
    fun observeAll(): Flow<List<FlashcardSetProgressEntity>>

    @Query("SELECT * FROM flashcard_set_progress")
    suspend fun getAll(): List<FlashcardSetProgressEntity>

    @Query("SELECT * FROM flashcard_set_progress WHERE set_id = :setId LIMIT 1")
    suspend fun getOne(setId: String): FlashcardSetProgressEntity?

    @Query("DELETE FROM flashcard_set_progress")
    suspend fun deleteAll()
}

/**
 * «Работа над ошибками» — слова и задания в которых юзер ошибался.
 * Общий DAO на все 4 игры (Articles, Speed, PalabraMaestra, Math) — игры
 * различаются полем `gameId`.
 */
@Dao
interface GameMistakesDao {
    /**
     * Зарегистрировать ошибку. Если запись уже была — increment attempts +
     * обновить lastSeenAt и displayHint/displayMain (на случай если перевод
     * изменился между запусками).
     */
    @Query("""
        INSERT INTO game_mistakes (game_id, item_id, display_hint, display_main, attempts, added_at, last_seen_at)
        VALUES (:gameId, :itemId, :hint, :main, 1, :now, :now)
        ON CONFLICT (game_id, item_id) DO UPDATE SET
            attempts      = attempts + 1,
            last_seen_at  = :now,
            display_hint  = :hint,
            display_main  = :main
    """)
    suspend fun recordMistake(
        gameId: String,
        itemId: String,
        hint: String,
        main: String,
        now: Long = System.currentTimeMillis(),
    )

    @Query("SELECT * FROM game_mistakes WHERE game_id = :gameId ORDER BY last_seen_at ASC LIMIT :limit")
    suspend fun getNextBatch(gameId: String, limit: Int = 5): List<GameMistakeEntity>

    @Query("SELECT * FROM game_mistakes WHERE game_id = :gameId ORDER BY last_seen_at DESC")
    fun observeAll(gameId: String): Flow<List<GameMistakeEntity>>

    @Query("SELECT COUNT(*) FROM game_mistakes WHERE game_id = :gameId")
    fun observeCount(gameId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM game_mistakes WHERE game_id = :gameId")
    suspend fun count(gameId: String): Int

    /** При правильном ответе в режиме практики — удаляем запись. */
    @Query("DELETE FROM game_mistakes WHERE game_id = :gameId AND item_id = :itemId")
    suspend fun removeMistake(gameId: String, itemId: String)

    /** Сбросить ошибки для игры (юзер может сбросить вручную). */
    @Query("DELETE FROM game_mistakes WHERE game_id = :gameId")
    suspend fun clearForGame(gameId: String)

    @Query("DELETE FROM game_mistakes")
    suspend fun deleteAll()
}

/**
 * v1.25.28 — DAO для агрегированного словарного запаса юзера.
 * Read обычно через Flow для UI VocabScreen, upsert батчем
 * из VocabAggregatorWorker.
 *
 * См. UserVocabStateEntity + docs/VOCAB_TRACKING_PLAN.md.
 */
@Dao
interface UserVocabStateDao {
    @androidx.room.Upsert
    suspend fun upsertAll(entries: List<UserVocabStateEntity>)

    @androidx.room.Upsert
    suspend fun upsert(entry: UserVocabStateEntity)

    @Query("SELECT * FROM user_vocab_state ORDER BY last_seen_at DESC")
    fun observeAll(): Flow<List<UserVocabStateEntity>>

    @Query("SELECT * FROM user_vocab_state WHERE status = :status ORDER BY score DESC")
    fun observeByStatus(status: String): Flow<List<UserVocabStateEntity>>

    @Query("SELECT * FROM user_vocab_state WHERE cefr = :cefr ORDER BY score DESC")
    fun observeByCefr(cefr: String): Flow<List<UserVocabStateEntity>>

    @Query("SELECT COUNT(*) FROM user_vocab_state WHERE status != 'UNKNOWN'")
    fun observeKnownCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM user_vocab_state WHERE updated_at >= :since AND status != 'UNKNOWN'")
    fun observeAddedSince(since: Long): Flow<Int>

    @Query("SELECT status, COUNT(*) as cnt FROM user_vocab_state WHERE status != 'UNKNOWN' GROUP BY status")
    fun observeStatusCounts(): Flow<List<StatusCount>>

    @Query("SELECT cefr, COUNT(*) as cnt FROM user_vocab_state WHERE status != 'UNKNOWN' AND cefr IS NOT NULL GROUP BY cefr")
    fun observeCefrCounts(): Flow<List<CefrCount>>

    @Query("SELECT * FROM user_vocab_state WHERE usage_count > 0 ORDER BY usage_count DESC LIMIT :limit")
    fun observeTopUsed(limit: Int = 8): Flow<List<UserVocabStateEntity>>

    @Query("""
        SELECT * FROM user_vocab_state
        WHERE status IN ('LEARNING','PRODUCING')
          AND last_seen_at < :thresholdMs
        ORDER BY last_seen_at ASC
        LIMIT :limit
    """)
    fun observeForgotten(thresholdMs: Long, limit: Int = 10): Flow<List<UserVocabStateEntity>>

    @Query("SELECT * FROM user_vocab_state WHERE word = :word LIMIT 1")
    suspend fun getByWord(word: String): UserVocabStateEntity?

    @Query("SELECT * FROM user_vocab_state")
    suspend fun getAll(): List<UserVocabStateEntity>

    @Query("DELETE FROM user_vocab_state")
    suspend fun deleteAll()
}

/** Row-projection для observeStatusCounts. */
data class StatusCount(
    val status: String,
    val cnt: Int,
)

/** Row-projection для observeCefrCounts. */
data class CefrCount(
    val cefr: String?,
    val cnt: Int,
)

/**
 * v1.25.78 VERB-4: DAO для пула слабых глагольных форм.
 */
@Dao
interface WeakVerbDao {

    @Query("SELECT * FROM weak_verbs ORDER BY error_count DESC, last_error_at DESC LIMIT :limit")
    suspend fun topWeak(limit: Int = 30): List<WeakVerbEntity>

    @Query("SELECT * FROM weak_verbs ORDER BY last_error_at DESC")
    fun observeAll(): Flow<List<WeakVerbEntity>>

    @Query("SELECT COUNT(*) FROM weak_verbs")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WeakVerbEntity)

    // v1.25.97 FIX (audit): REPLACE-upsert всегда писал error_count = 1 —
    // счётчик не накапливался, и `topWeak ORDER BY error_count DESC` был
    // бессмысленным. Инкрементируем существующую строку; INSERT — если новой.
    @Query("UPDATE weak_verbs SET error_count = error_count + 1, last_error_at = :ts WHERE `key` = :key")
    suspend fun bumpErrorCount(key: String, ts: Long): Int

    @Query("DELETE FROM weak_verbs WHERE `key` = :key")
    suspend fun deleteByKey(key: String)

    @Query("DELETE FROM weak_verbs")
    suspend fun deleteAll()
}
