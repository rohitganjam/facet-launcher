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
            "AND packageName = :packageName AND activityName = :activityName AND profile = :profile",
    )
    suspend fun deleteByComponent(facetId: Long, packageName: String, activityName: String, profile: AppProfile)

    /** Uninstall cleanup — removes every facet's dock entry for [packageName] in [profile], not just one. */
    @Query("DELETE FROM facet_dock_apps WHERE packageName = :packageName AND profile = :profile")
    suspend fun deleteByPackage(packageName: String, profile: AppProfile)

    /** Bulk cleanup for a whole profile vanishing (e.g. Work Profile unenrollment). */
    @Query("DELETE FROM facet_dock_apps WHERE profile = :profile")
    suspend fun deleteByProfile(profile: AppProfile)

    /** Wipes this facet's own dock entirely — used to seed a clean copy when switching to Override. */
    @Query("DELETE FROM facet_dock_apps WHERE facetId = :facetId")
    suspend fun deleteAllForFacet(facetId: Long)
}
