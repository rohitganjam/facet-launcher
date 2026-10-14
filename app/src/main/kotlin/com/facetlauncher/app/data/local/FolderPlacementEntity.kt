package com.facetlauncher.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A folder placed into a Dock or Favorites list at [position] — the launcher-wide default when
 * [facetId] is null, else that facet's own list. Shares the list's `position` ordering space with
 * [AppPlacementEntity] (merged in Kotlin by the placement repositories, not enforced by Room). A
 * folder occupies at most one slot per list. See [AppPlacementEntity] for why [ownerKey] is stored.
 */
@Entity(
    tableName = "folder_placements",
    foreignKeys = [
        ForeignKey(entity = FacetEntity::class, parentColumns = ["id"], childColumns = ["facetId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = FolderEntity::class, parentColumns = ["id"], childColumns = ["folderId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [
        Index(value = ["list", "ownerKey", "folderId"], unique = true),
        Index(value = ["facetId"]),
        Index(value = ["folderId"]),
    ],
)
data class FolderPlacementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val list: PlacementList,
    val facetId: Long? = null,
    val ownerKey: Long = facetId ?: GLOBAL_OWNER_KEY,
    val folderId: Long,
    val position: Int,
)
