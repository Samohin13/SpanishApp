package com.spanishapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spanishapp.data.db.entity.WodHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * История закрепления «Слова дня». Одна запись = одно завершённое
 * прохождение WoD-флоу пользователем.
 *
 * Используется:
 *   • При расчёте WoD-стрика (был ли вчера? было ли уже сегодня?)
 *   • На экране «Коллекция Слов дня» в Профиле
 */
@Dao
interface WodHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WodHistoryEntity): Long

    /** Все записи в обратном хронологическом порядке (новые сверху). */
    @Query("SELECT * FROM wod_history ORDER BY practiced_at DESC")
    fun observeAll(): Flow<List<WodHistoryEntity>>

    /** Лимитированная версия для виджета в Профиле. */
    @Query("SELECT * FROM wod_history ORDER BY practiced_at DESC LIMIT :limit")
    suspend fun recent(limit: Int = 10): List<WodHistoryEntity>

    /** Общее количество выученных слов через WoD. */
    @Query("SELECT COUNT(*) FROM wod_history")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM wod_history")
    suspend fun count(): Int

    /** Самая последняя запись (для расчёта стрика). */
    @Query("SELECT * FROM wod_history ORDER BY practiced_at DESC LIMIT 1")
    suspend fun mostRecent(): WodHistoryEntity?

    /** Проверить, было ли уже сегодня (по эпохальному дню UTC). */
    @Query("""
        SELECT COUNT(*) FROM wod_history
        WHERE practiced_at >= :startOfDayMs AND practiced_at < :endOfDayMs
    """)
    suspend fun countInRange(startOfDayMs: Long, endOfDayMs: Long): Int
}
