package com.facetlauncher.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DockFolderPlacementDao {

    @Query("SELECT * FROM dock_folder_placements ORDER BY position ASC")
    fun observeAll(): Flow<List<DockFolderPlacementEntity>>

    /** Safe to also use for a position-only rewrite (reorder) — unlike a folder's own row, nothing foreign-keys to a placement row's own `id`, so `REPLACE`'s delete-then-insert here has no cascade side effect. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(placement: DockFolderPlacementEntity): Long

    @Query("DELETE FROM dock_folder_placements WHERE folderId = :folderId")
    suspend fun deleteByFolderId(folderId: Long)

    /** F14 Backup & Restore — wipes this list's folder placements before restoring from a backup. */
    @Query("DELETE FROM dock_folder_placements")
    suspend fun deleteAll()
}
