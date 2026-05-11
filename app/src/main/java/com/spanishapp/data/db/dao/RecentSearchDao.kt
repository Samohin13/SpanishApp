package com.spanishapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spanishapp.data.db.entity.RecentSearchEntity
import com.spanishapp.data.db.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentSearchDao {

    @Query("SELECT * FROM recent_searches ORDER BY opened_at DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<RecentSearchEntity>

    /** Recently-opened words, joined with the words table — for the home tile. */
    @Query("""
        SELECT w.* FROM words w
        INNER JOIN recent_searches r ON r.wordId = w.id
        ORDER BY r.opened_at DESC LIMIT :limit
    """)
    fun observeRecentWords(limit: Int): Flow<List<WordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RecentSearchEntity)

    @Query("DELETE FROM recent_searches")
    suspend fun deleteAll()
}
