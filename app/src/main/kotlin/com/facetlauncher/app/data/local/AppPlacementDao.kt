package com.facetlauncher.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Every Dock/Favorites app row. A query takes the [PlacementList] plus either one owner
 * ([GLOBAL_OWNER_KEY] or a facet id) or a `global` flag that picks the launcher-wide rows (true) or
 * every facet's rows (false).
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

    /** Uninstall cleanup — removes [packageName]'s entries belonging to [userId] regardless of activity. */
    @Query("DELETE FROM app_placements WHERE list = :list AND (ownerKey = 0) = :global AND packageName = :packageName AND userId = :userId")
    suspend fun deleteByPackage(list: PlacementList, global: Boolean, packageName: String, userId: Int)

    /** Bulk cleanup for a whole profile vanishing (e.g. Work Profile unenrollment). */
    @Query("DELETE FROM app_placements WHERE list = :list AND (ownerKey = 0) = :global AND userId = :userId")
    suspend fun deleteByUserId(list: PlacementList, global: Boolean, userId: Int)

    /** Wipes one list entirely — a facet's own, or the launcher-wide default. */
    @Query("DELETE FROM app_placements WHERE list = :list AND ownerKey = :ownerKey")
    suspend fun deleteAllForOwner(list: PlacementList, ownerKey: Long)

    /** Rows still at [Migrations.MIGRATION_19_20]'s `-1` sentinel — see `RepairOrphanedProfileRowsUseCase`. */
    @Query("SELECT * FROM app_placements WHERE list = :list AND (ownerKey = 0) = :global AND userId = -1")
    suspend fun getOrphaned(list: PlacementList, global: Boolean): List<AppPlacementEntity>
}
