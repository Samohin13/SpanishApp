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

    @Query("SELECT * FROM words WHERE repetitions = 0 AND level = :level ORDER BY RANDOM() LIMIT :limit")
    fun getNewWords(level: String, limit: Int = 10): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE word_type = :type ORDER BY RANDOM() LIMIT :limit")
    fun getByType(type: String, limit: Int = 50): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE level = :level AND word_type = :type ORDER BY RANDOM() LIMIT :limit")
    fun getByLevelAndType(level: String, type: String, limit: Int = 30): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE category = :category ORDER BY RANDOM() LIMIT :limit")
    fun getByCategory(category: String, limit: Int = 50): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE spanish LIKE '%' || :q || '%' OR russian LIKE '%' || :q || '%' ORDER BY CASE WHEN spanish LIKE :q || '%' THEN 0 ELSE 1 END LIMIT 40")
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

    @Query("UPDATE user_progress SET total_xp = total_xp + :xp, words_learned = words_learned + :words")
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