package com.lumenlauncher.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * The launcher-wide default Favorites list — mirrors [DockAppEntity] exactly (global, no
 * per-profile scoping): the baseline Favorites shown by any profile that isn't overriding its
 * own, edited from Settings' own "Default favorites" card rather than from a profile.
 */
@Entity(
    tableName = "default_favorite_apps",
    indices = [Index(value = ["packageName", "activityName"], unique = true)],
)
data class DefaultFavoriteAppEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val activityName: String,
    val position: Int,
)
