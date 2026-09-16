package com.facetlauncher.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.facetlauncher.app.data.model.AppProfile

/**
 * The launcher-wide default Favorites list — mirrors [DockAppEntity] exactly (global, no
 * per-facet scoping): the baseline Favorites shown by any facet that isn't overriding its
 * own, edited from Settings' own "Default favorites" card rather than from a facet.
 */
@Entity(
    tableName = "default_favorite_apps",
    indices = [Index(value = ["packageName", "activityName", "userId"], unique = true)],
)
data class DefaultFavoriteAppEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val activityName: String,
    val position: Int,
    val profile: AppProfile = AppProfile.PERSONAL,
    /** The real `UserHandle.hashCode()` this app came from (`getIdentifier()` itself is hidden API, not in the public SDK — see [Migrations.MIGRATION_19_20]) — the actual identity key; [profile] is display-only. */
    val userId: Int = -1,
)
