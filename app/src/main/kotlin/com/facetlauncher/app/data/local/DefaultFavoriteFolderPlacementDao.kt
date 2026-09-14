package com.facetlauncher.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DefaultFavoriteFolderPlacementDao {

    @Query("SELECT * FROM default_favorite_folder_placements ORDER BY position ASC")
    fun observeAll(): Flow<List<DefaultFavoriteFolderPlacementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(placement: DefaultFavoriteFolderPlacementEntity): Long

    @Query("DELETE FROM default_favorite_folder_placements WHERE folderId = :folderId")
    suspend fun deleteByFolderId(folderId: Long)

    /** F14 Backup & Restore — wipes this list's folder placements before restoring from a backup. */
    @Query("DELETE FROM default_favorite_folder_placements")
    suspend fun deleteAll()
}
