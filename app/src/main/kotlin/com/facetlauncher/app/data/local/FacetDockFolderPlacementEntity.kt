package com.facetlauncher.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** The per-facet counterpart to [DockFolderPlacementEntity] — a folder placed into one facet's own overriding Dock. Unique on `(facetId, folderId)`: a folder occupies at most one slot in a given facet's own dock. */
@Entity(
    tableName = "facet_dock_folder_placements",
    foreignKeys = [
        ForeignKey(entity = FacetEntity::class, parentColumns = ["id"], childColumns = ["facetId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = FolderEntity::class, parentColumns = ["id"], childColumns = ["folderId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index(value = ["facetId", "folderId"], unique = true)],
)
data class FacetDockFolderPlacementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val facetId: Long,
    val folderId: Long,
    val position: Int,
)
