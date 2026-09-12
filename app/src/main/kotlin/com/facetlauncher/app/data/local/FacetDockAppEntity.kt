package com.facetlauncher.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One facet's own dock app list — mirrors [FavoriteAppEntity] exactly, just for the dock:
 * the per-facet counterpart to the launcher-wide [DockAppEntity], shown only while that
 * facet's [FacetEntity.overrideDock] is set.
 */
@Entity(
    tableName = "facet_dock_apps",
    foreignKeys = [
        ForeignKey(
            entity = FacetEntity::class,
            parentColumns = ["id"],
            childColumns = ["facetId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["facetId", "packageName", "activityName"], unique = true)],
)
data class FacetDockAppEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val facetId: Long,
    val packageName: String,
    val activityName: String,
    val position: Int,
)
