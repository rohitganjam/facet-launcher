package com.facetlauncher.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.facetlauncher.app.data.model.AppProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface FacetDockAppDao {

    @Query("SELECT * FROM facet_dock_apps WHERE facetId = :facetId ORDER BY position ASC")
    fun observeForFacet(facetId: Long): Flow<List<FacetDockAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(dockApp: FacetDockAppEntity): Long

    @Delete
    suspend fun delete(dockApp: FacetDockAppEntity)

    @Query(
        "DELETE FROM facet_dock_apps WHERE facetId = :facetId " +
            "AND packageName = :packageName AND activityName = :activityName AND userId = :userId",
    )
    suspend fun deleteByComponent(facetId: Long, packageName: String, activityName: String, userId: Int)

    /** Uninstall cleanup — removes every facet's dock entry for [packageName] belonging to [userId], not just one. */
    @Query("DELETE FROM facet_dock_apps WHERE packageName = :packageName AND userId = :userId")
    suspend fun deleteByPackage(packageName: String, userId: Int)

    /** Bulk cleanup for a whole profile vanishing (e.g. Work Profile unenrollment). */
    @Query("DELETE FROM facet_dock_apps WHERE userId = :userId")
    suspend fun deleteByUserId(userId: Int)

    /** Wipes this facet's own dock entirely — used to seed a clean copy when switching to Override. */
    @Query("DELETE FROM facet_dock_apps WHERE facetId = :facetId")
    suspend fun deleteAllForFacet(facetId: Long)

    /** Rows still at [Migrations.MIGRATION_19_20]'s `-1` sentinel — see `RepairOrphanedProfileRowsUseCase`. */
    @Query("SELECT * FROM facet_dock_apps WHERE userId = -1")
    suspend fun getOrphaned(): List<FacetDockAppEntity>
}
