package com.spanishapp.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Состояние недельной лиги пользователя (Duolingo-style).
 *
 * Параллельная система к skillRating: каждый понедельник 00:00 UTC
 * пользователь попадает в когорту из 30 человек, неделю соревнуется
 * по weeklyXP. В воскресенье 23:59 UTC: топ-7 → +1 tier, низ-7 → -1 tier,
 * середина 16 — остаются.
 *
 * Tier (1..8) использует те же города что и skillRating-лиги, но
 * семантика другая — это weekly-bracket tier, НЕ привязан к skillRating.
 */
@Entity(tableName = "weekly_league_state")
data class WeeklyLeagueStateEntity(
    @PrimaryKey val userId: Int = 1,
    @ColumnInfo(name = "current_tier")        val currentTier: Int = 1,
    @ColumnInfo(name = "current_week_start")  val currentWeekStart: String = "",  // ISO Monday date "YYYY-MM-DD"
    @ColumnInfo(name = "current_week_xp")     val currentWeekXp: Int = 0,
    @ColumnInfo(name = "cohort_id")           val cohortId: String = "",
    @ColumnInfo(name = "last_finalized_week") val lastFinalizedWeek: String = "",
    @ColumnInfo(name = "opted_in")            val optedIn: Boolean = false
)
