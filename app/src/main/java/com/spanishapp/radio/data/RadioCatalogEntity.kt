package com.spanishapp.radio.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Динамически подобранная станция — получена из radio-browser.info API
 * и проверена probe-запросом из устройства юзера.
 *
 * Используется ВМЕСТО хардкоженного списка StationRepository,
 * если этот кэш не пуст и свежий (≤ 7 дней).
 */
@Entity(tableName = "radio_catalog")
data class RadioCatalogEntity(
    @PrimaryKey @ColumnInfo(name = "station_id") val stationId: String,
    @ColumnInfo(name = "short_code") val shortCode: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "program") val program: String,
    @ColumnInfo(name = "frequency") val frequency: Float,
    @ColumnInfo(name = "country") val country: String,  // SPAIN / MEXICO / ARGENTINA
    @ColumnInfo(name = "genre") val genre: String,      // MUSIC / TALK / NEWS / SPORTS / CULTURE
    @ColumnInfo(name = "level") val level: String,      // A2 / B1 / B2
    @ColumnInfo(name = "stream_url") val streamUrl: String,
    @ColumnInfo(name = "bitrate") val bitrate: Int,
    @ColumnInfo(name = "user_country") val userCountry: String,  // KZ / RU / ES / ...
    @ColumnInfo(name = "fetched_at") val fetchedAt: Long,
)

@Dao
interface RadioCatalogDao {
    @Query("SELECT * FROM radio_catalog WHERE country = :country ORDER BY frequency")
    fun observeByCountry(country: String): Flow<List<RadioCatalogEntity>>

    @Query("SELECT COUNT(*) FROM radio_catalog")
    suspend fun count(): Int

    @Query("SELECT MAX(fetched_at) FROM radio_catalog")
    suspend fun lastFetchedAt(): Long?

    @Query("INSERT OR REPLACE INTO radio_catalog VALUES (:stationId, :shortCode, :name, :program, :frequency, :country, :genre, :level, :streamUrl, :bitrate, :userCountry, :fetchedAt)")
    suspend fun upsert(
        stationId: String, shortCode: String, name: String, program: String,
        frequency: Float, country: String, genre: String, level: String,
        streamUrl: String, bitrate: Int, userCountry: String, fetchedAt: Long,
    )

    @Query("DELETE FROM radio_catalog")
    suspend fun clear()

    /** Перевести RadioCatalogEntity → Station (для UI). */
    @Query("SELECT * FROM radio_catalog ORDER BY country, frequency")
    suspend fun getAll(): List<RadioCatalogEntity>
}

fun RadioCatalogEntity.toStation(): Station = Station(
    id = stationId,
    shortCode = shortCode,
    name = name,
    program = program,
    frequency = frequency,
    country = Country.valueOf(country),
    genre = Genre.valueOf(genre),
    level = CefrLevel.valueOf(level),
    streamUrl = streamUrl,
)
