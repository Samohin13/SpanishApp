package com.spanishapp.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per word that the user has recently opened in the dictionary
 * (or any word-detail surface). The id is the word id; opening the same
 * word again just bumps `opened_at`. Five most-recent rows feed the home
 * screen "Recent searches" bento tile.
 *
 * Added in DB v15.
 */
@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey val wordId: Int,
    @ColumnInfo(name = "opened_at") val openedAt: Long
)
