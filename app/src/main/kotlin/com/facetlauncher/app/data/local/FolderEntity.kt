package com.facetlauncher.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A folder's own identity — global, independent of any Dock/Favorites placement. No `position`
 * here: a folder's ordering only exists relative to wherever it's *placed* (see the four
 * `*FolderPlacementEntity` types), not as a property of the folder itself. A folder with zero
 * members is valid and persists until explicitly deleted — see [FolderRepository][com.facetlauncher.app.data.FolderRepository].
 */
@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)
