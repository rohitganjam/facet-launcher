package com.facetlauncher.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.GLOBAL_OWNER_KEY

/**
 * Every Dock/Favorites app row. Per-list queries take the [PlacementList] plus the owner
 * ([GLOBAL_OWNER_KEY] or a facet id); uninstall and orphan-repair queries span every list and owner.
 */
@Dao
interface AppPlacementDao {
    @Query("SELECT * FROM app_placements WHERE list = :list AND ownerKey = :ownerKey ORDER BY position ASC")
    fun observe(list: PlacementList, ownerKey: Long): Flow<List<AppPlacementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(placement: AppPlacementEntity): Long

    @Query(
        "DELETE FROM app_placements WHERE list = :list AND ownerKey = :ownerKey " +
            "AND packageName = :packageName AND activityName = :activityName AND userId = :userId",
    )
    suspend fun deleteByComponent(list: PlacementList, ownerKey: Long, packageName: String, activityName: String, userId: Int)

    /** Uninstall cleanup — removes [packageName]'s entries belonging to [userId] from every list and owner, regardless of activity. */
    @Query("DELETE FROM app_placements WHERE packageName = :packageName AND userId = :userId")
    suspend fun deleteByPackage(packageName: String, userId: Int)

    /** Bulk cleanup for a whole profile vanishing (e.g. Work Profile unenrollment), across every list and owner. */
    @Query("DELETE FROM app_placements WHERE userId = :userId")
    suspend fun deleteByUserId(userId: Int)

    /** Wipes one list entirely — a facet's own, or the launcher-wide default. */
    @Query("DELETE FROM app_placements WHERE list = :list AND ownerKey = :ownerKey")
    suspend fun deleteAllForOwner(list: PlacementList, ownerKey: Long)

    /** Rows still at [Migrations.MIGRATION_19_20]'s `-1` sentinel, in every list and owner — see `RepairOrphanedProfileRowsUseCase`. */
    @Query("SELECT * FROM app_placements WHERE userId = -1")
    suspend fun getOrphaned(): List<AppPlacementEntity>
}
