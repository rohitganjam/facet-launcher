package com.facetlauncher.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A folder placed into the launcher-wide default Dock, at [position] — shares that same
 * `dock_apps`-position ordering space (merged in Kotlin by `DockAppRepository`, not enforced
 * across tables by Room), the same way a standalone [DockAppEntity] does. Unique on [folderId]
 * alone — a folder occupies at most one slot in this particular list.
 */
@Entity(
    tableName = "dock_folder_placements",
    foreignKeys = [
        ForeignKey(entity = FolderEntity::class, parentColumns = ["id"], childColumns = ["folderId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index(value = ["folderId"], unique = true)],
)
data class DockFolderPlacementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folderId: Long,
    val position: Int,
)
