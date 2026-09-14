package com.facetlauncher.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A folder placed into the launcher-wide default Favorites list — mirrors [DockFolderPlacementEntity] exactly, on the Favorites side. */
@Entity(
    tableName = "default_favorite_folder_placements",
    foreignKeys = [
        ForeignKey(entity = FolderEntity::class, parentColumns = ["id"], childColumns = ["folderId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index(value = ["folderId"], unique = true)],
)
data class DefaultFavoriteFolderPlacementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folderId: Long,
    val position: Int,
)
