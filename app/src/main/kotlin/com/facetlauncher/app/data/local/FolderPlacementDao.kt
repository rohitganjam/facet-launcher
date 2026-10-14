package com.facetlauncher.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.facetlauncher.app.data.model.PlacementList

@Dao
interface FolderPlacementDao {
    @Query("SELECT * FROM folder_placements WHERE list = :list AND ownerKey = :ownerKey ORDER BY position ASC")
    fun observe(list: PlacementList, ownerKey: Long): Flow<List<FolderPlacementEntity>>

    /** Safe to also use for a position-only rewrite (reorder) — nothing foreign-keys to a placement row's own `id`, so `REPLACE`'s delete-then-insert has no cascade side effect. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(placement: FolderPlacementEntity): Long

    @Query("DELETE FROM folder_placements WHERE list = :list AND ownerKey = :ownerKey AND folderId = :folderId")
    suspend fun deleteByFolderId(list: PlacementList, ownerKey: Long, folderId: Long)

    /** Wipes one list's folder placements — a facet's own, or the launcher-wide default. */
    @Query("DELETE FROM folder_placements WHERE list = :list AND ownerKey = :ownerKey")
    suspend fun deleteAllForOwner(list: PlacementList, ownerKey: Long)
}
