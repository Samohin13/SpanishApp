package com.spanishapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spanishapp.data.db.entity.TheoryProgressEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO прогресса теории-карточек (v21).
 *
 * Используется:
 *   • LessonSessionScreen — показать «прочитано / открыть»
 *   • TheoryReaderScreen — пометить как прочитанное
 *   • Profile → раздел «Теория» — «прочитано N/200»
 */
@Dao
interface TheoryProgressDao {

    /** Все прочитанные теории для подсчёта прогресса. */
    @Query("SELECT * FROM theory_progress ORDER BY last_read_at DESC")
    fun observeAll(): Flow<List<TheoryProgressEntity>>

    /** Конкретная теория — для отображения «прочитано / нет». */
    @Query("SELECT * FROM theory_progress WHERE lesson_id = :lessonId")
    suspend fun getOne(lessonId: String): TheoryProgressEntity?

    /** Количество прочитанных — для бэйджа «42/200» в Profile. */
    @Query("SELECT COUNT(*) FROM theory_progress WHERE first_read_at > 0")
    fun observeReadCount(): Flow<Int>

    /**
     * Помечает теорию как прочитанную.
     * Если первое прочтение — заполняет first_read_at.
     * При повторных вызовах — обновляет last_read_at и read_count++.
     */
    @Query("""
        INSERT OR REPLACE INTO theory_progress (lesson_id, first_read_at, last_read_at, read_count)
        VALUES (
            :lessonId,
            COALESCE((SELECT first_read_at FROM theory_progress WHERE lesson_id = :lessonId), :now),
            :now,
            COALESCE((SELECT read_count FROM theory_progress WHERE lesson_id = :lessonId), 0) + 1
        )
    """)
    suspend fun markRead(lessonId: String, now: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TheoryProgressEntity)
}
