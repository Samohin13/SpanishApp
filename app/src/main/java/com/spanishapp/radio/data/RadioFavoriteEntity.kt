package com.spanishapp.radio.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "radio_favorites")
data class RadioFavoriteEntity(
    @PrimaryKey @ColumnInfo(name = "station_id") val stationId: String,
    @ColumnInfo(name = "added_at") val addedAt: Long = System.currentTimeMillis(),
)

@Dao
interface RadioFavoriteDao {
    @Query("SELECT station_id FROM radio_favorites ORDER BY added_at DESC")
    fun observeAllIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM radio_favorites WHERE station_id = :stationId)")
    fun observeIsFavorite(stationId: String): Flow<Boolean>

    @Query("INSERT OR IGNORE INTO radio_favorites (station_id, added_at) VALUES (:stationId, :now)")
    suspend fun add(stationId: String, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM radio_favorites WHERE station_id = :stationId")
    suspend fun remove(stationId: String)
}
