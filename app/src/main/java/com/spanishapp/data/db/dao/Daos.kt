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

    @Query("SELECT * FROM words WHERE next_review <= :now ORDER BY next_review ASC LIMIT :limit")
    fun getDueWords(now: Long = System.currentTimeMillis(), limit: Int = 30): Flow<List<WordEntity>>

    @Query("SELECT * FROM words ORDER BY spanish ASC LIMIT :limit")
    fun getAllWords(limit: Int = 12000): Flow<List<WordEntity>>

    // Все испанские слова в нижнем регистре — для дедупликации при досеве
    @Query("SELECT lower(trim(spanish)) FROM words")
    suspend fun getAllSpanishLower(): List<String>

    @Query("SELECT id FROM words WHERE level = 'A1' ORDER BY id")
    suspend fun getA1WordIds(): List<Int>

    @Query("SELECT * FROM words WHERE total_reviews > 0 ORDER BY RANDOM() LIMIT :limit")
    suspend fun getAllWordsOnce(limit: Int): List<WordEntity>

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

    @Query("SELECT * FROM words WHERE spanish LIKE '%' || :q || '%' OR russian LIKE '%' || :q || '%' ORDER BY CASE WHEN spanish LIKE :q || '%' THEN 0 ELSE 1 END LIMIT 80")
    fun search(q: String): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getById(id: Int): WordEntity?

    @Query("SELECT COUNT(*) FROM words WHERE is_learned = 1")
    fun learnedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM words WHERE word_type = :type AND is_learned = 1")
    fun learnedCountByType(type: String): Flow<Int>

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
}

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

    @Query("UPDATE user_progress SET total_xp = total_xp + :xp, words_learned = words_learned + :words, lessons_completed = lessons_completed + 1")
    suspend fun addXpAndWords(xp: Int, words: Int)

    @Query("UPDATE user_progress SET current_streak = :streak, longest_streak = MAX(longest_streak, :streak), last_study_date = :date")
    suspend fun updateStreak(streak: Int, date: Long)

    @Query("UPDATE user_progress SET total_study_minutes = total_study_minutes + :minutes")
    suspend fun addStudyTime(minutes: Int)
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
}

@Dao
interface AchievementDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(achievements: List<AchievementEntity>)

    @Update
    suspend fun update(achievement: AchievementEntity)

    @Query("SELECT * FROM achievements ORDER BY is_unlocked DESC, xp_reward DESC")
    fun getAll(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE is_unlocked = 0 AND requirement_type = :type")
    suspend fun getLockedByType(type: String): List<AchievementEntity>

    @Query("SELECT COUNT(*) FROM achievements WHERE is_unlocked = 1")
    fun unlockedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getCount(): Int
}

@Dao
interface DailyWordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(dailyWord: DailyWordEntity)

    @Query("SELECT * FROM daily_words WHERE date = :date")
    suspend fun getForDate(date: String): DailyWordEntity?

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
}

@Dao
interface LessonProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markComplete(progress: LessonProgressEntity)

    @Query("SELECT lesson_key FROM lesson_progress")
    fun getAllCompletedKeys(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM lesson_progress WHERE unit_id = :unitId")
    suspend fun completedCountForUnit(unitId: Int): Int
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

    @Query("SELECT * FROM article_words WHERE level = :level ORDER BY error_weight DESC, RANDOM() LIMIT :limit")
    suspend fun getWordsForLevel(level: String, limit: Int = 10): List<ArticleWordEntity>

    @Update
    suspend fun updateWord(word: ArticleWordEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWords(words: List<ArticleWordEntity>)

    @Query("SELECT COUNT(*) FROM article_words")
    suspend fun getWordCount(): Int
}

@Dao
interface LibroProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: LibroProgressEntity)

    @Query("SELECT * FROM libro_progress")
    fun getAll(): Flow<List<LibroProgressEntity>>

    @Query("SELECT * FROM libro_progress WHERE libro_id = :id")
    suspend fun getById(id: Int): LibroProgressEntity?
}
