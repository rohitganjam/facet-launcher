package com.facetlauncher.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteFolderPlacementDao {

    @Query("SELECT * FROM favorite_folder_placements WHERE facetId = :facetId ORDER BY position ASC")
    fun observeForFacet(facetId: Long): Flow<List<FavoriteFolderPlacementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(placement: FavoriteFolderPlacementEntity): Long

    @Query("DELETE FROM favorite_folder_placements WHERE facetId = :facetId AND folderId = :folderId")
    suspend fun deleteByFolderId(facetId: Long, folderId: Long)

    @Query("DELETE FROM favorite_folder_placements WHERE facetId = :facetId")
    suspend fun deleteAllForFacet(facetId: Long)

    /** F14 Backup & Restore — wipes every facet's favorite folder placements before restoring from a backup. */
    @Query("DELETE FROM favorite_folder_placements")
    suspend fun deleteAll()
}
