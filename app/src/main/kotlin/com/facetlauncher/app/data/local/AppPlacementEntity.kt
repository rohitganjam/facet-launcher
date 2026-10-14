package com.facetlauncher.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.GLOBAL_OWNER_KEY

/**
 * One app in a Dock or Favorites list: the launcher-wide default when [facetId] is null, else that
 * facet's own overriding list. Replaces the four former per-list tables (`dock_apps`,
 * `facet_dock_apps`, `favorite_apps`, `default_favorite_apps`).
 *
 * [ownerKey] is [facetId] or [GLOBAL_OWNER_KEY] — a stored copy because SQLite treats NULLs as
 * distinct in a unique index, so uniqueness for the launcher-wide rows has to be on a non-null column.
 */
@Entity(
    tableName = "app_placements",
    foreignKeys = [
        ForeignKey(
            entity = FacetEntity::class,
            parentColumns = ["id"],
            childColumns = ["facetId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["list", "ownerKey", "packageName", "activityName", "userId"], unique = true),
        Index(value = ["facetId"]),
    ],
)
data class AppPlacementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val list: PlacementList,
    val facetId: Long? = null,
    val ownerKey: Long = facetId ?: GLOBAL_OWNER_KEY,
    val packageName: String,
    val activityName: String,
    val position: Int,
    val profile: AppProfile = AppProfile.PERSONAL,
    /** The real `UserHandle.hashCode()` this app came from (`getIdentifier()` itself is hidden API, not in the public SDK — see [Migrations.MIGRATION_19_20]) — the actual identity key; [profile] is display-only. */
    val userId: Int = -1,
)
