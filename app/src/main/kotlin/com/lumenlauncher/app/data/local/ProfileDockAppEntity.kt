package com.lumenlauncher.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One profile's own dock app list — mirrors [FavoriteAppEntity] exactly, just for the dock:
 * the per-profile counterpart to the launcher-wide [DockAppEntity], shown only while that
 * profile's [ProfileEntity.overrideDock] is set.
 */
@Entity(
    tableName = "profile_dock_apps",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["profileId", "packageName", "activityName"], unique = true)],
)
data class ProfileDockAppEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val packageName: String,
    val activityName: String,
    val position: Int,
)
