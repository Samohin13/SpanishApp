package com.spanishapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spanishapp.data.db.entity.WeeklyLeagueStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyLeagueDao {
    @Query("SELECT * FROM weekly_league_state WHERE userId = 1 LIMIT 1")
    fun observe(): Flow<WeeklyLeagueStateEntity?>

    @Query("SELECT * FROM weekly_league_state WHERE userId = 1 LIMIT 1")
    suspend fun get(): WeeklyLeagueStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: WeeklyLeagueStateEntity)

    @Query("UPDATE weekly_league_state SET current_week_xp = current_week_xp + :delta WHERE userId = 1")
    suspend fun bumpWeekXp(delta: Int)

    @Query("UPDATE weekly_league_state SET opted_in = :enabled WHERE userId = 1")
    suspend fun setOptedIn(enabled: Boolean)
}
