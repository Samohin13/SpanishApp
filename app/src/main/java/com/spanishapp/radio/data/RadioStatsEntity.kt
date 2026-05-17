package com.spanishapp.radio.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Одна сессия прослушивания радио.
 * Создаётся когда player.isPlaying = true, закрывается на pause/stop.
 */
@Entity(tableName = "radio_listening_session")
data class RadioListeningSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "ended_at") val endedAt: Long,
    @ColumnInfo(name = "station_id") val stationId: String,
)

@Dao
interface RadioListeningDao {
    @Query("INSERT INTO radio_listening_session (started_at, ended_at, station_id) VALUES (:start, :end, :stationId)")
    suspend fun insert(start: Long, end: Long, stationId: String)

    /** Сумма секунд прослушано за всё время. */
    @Query("SELECT COALESCE(SUM(ended_at - started_at), 0) / 1000 FROM radio_listening_session")
    fun observeTotalSeconds(): Flow<Long>

    /** Секунд прослушано с указанного момента (для проверки дневной нормы). */
    @Query("SELECT COALESCE(SUM(ended_at - started_at), 0) / 1000 FROM radio_listening_session WHERE started_at >= :sinceMs")
    fun observeSecondsSince(sinceMs: Long): Flow<Long>

    /** Дни (timestamps midnight) когда было хоть какое-то прослушивание. */
    @Query("SELECT DISTINCT (started_at / 86400000) FROM radio_listening_session ORDER BY 1 DESC LIMIT 60")
    suspend fun activeDayBuckets(): List<Long>

    /** Топ-3 станции по сумме времени. */
    @Query("""
        SELECT station_id, SUM(ended_at - started_at) / 1000 as secs
        FROM radio_listening_session
        GROUP BY station_id
        ORDER BY secs DESC LIMIT 3
    """)
    fun observeTopStations(): Flow<List<TopStation>>
}

data class TopStation(
    val station_id: String,
    val secs: Long,
)

/**
 * Юзер тапнул «Поймал слово!». Записываем момент + станцию.
 * Позже можно расширить полем word_text (если юзер впишет слово).
 */
@Entity(tableName = "radio_word_catch")
data class RadioWordCatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "caught_at") val caughtAt: Long,
    @ColumnInfo(name = "station_id") val stationId: String,
    @ColumnInfo(name = "word_text") val wordText: String? = null,
)

@Dao
interface RadioWordCatchDao {
    @Query("INSERT INTO radio_word_catch (caught_at, station_id, word_text) VALUES (:at, :stationId, :word)")
    suspend fun insert(at: Long, stationId: String, word: String? = null)

    @Query("SELECT COUNT(*) FROM radio_word_catch")
    fun observeTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM radio_word_catch WHERE caught_at >= :sinceMs")
    suspend fun countSince(sinceMs: Long): Int
}
