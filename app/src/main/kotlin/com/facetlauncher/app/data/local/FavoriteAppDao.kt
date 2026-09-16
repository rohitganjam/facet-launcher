package com.facetlauncher.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.facetlauncher.app.data.model.AppProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteAppDao {

    @Query("SELECT * FROM favorite_apps WHERE facetId = :facetId ORDER BY position ASC")
    fun observeForFacet(facetId: Long): Flow<List<FavoriteAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(favoriteApp: FavoriteAppEntity): Long

    @Delete
    suspend fun delete(favoriteApp: FavoriteAppEntity)

    @Query(
        "DELETE FROM favorite_apps WHERE facetId = :facetId " +
            "AND packageName = :packageName AND activityName = :activityName AND userId = :userId",
    )
    suspend fun deleteByComponent(facetId: Long, packageName: String, activityName: String, userId: Int)

    /**
     * Uninstall cleanup — removes every facet's favorite entry for [packageName] belonging to
     * [userId], not just one. Scoped by the real per-user id (not [AppProfile]) so uninstalling a
     * Work Profile/clone-profile app can't delete a personal favorite that happens to share the
     * same package name (a duplicate-installed app).
     */
    @Query("DELETE FROM favorite_apps WHERE packageName = :packageName AND userId = :userId")
    suspend fun deleteByPackage(packageName: String, userId: Int)

    /** Bulk cleanup for a whole profile vanishing (e.g. Work Profile unenrollment) — see [com.facetlauncher.app.data.AppRepository.observeProfileRemoved]. */
    @Query("DELETE FROM favorite_apps WHERE userId = :userId")
    suspend fun deleteByUserId(userId: Int)

    /** Wipes this facet's own favorites entirely — used to seed a clean copy when switching to Override. */
    @Query("DELETE FROM favorite_apps WHERE facetId = :facetId")
    suspend fun deleteAllForFacet(facetId: Long)

    /** Rows still at [Migrations.MIGRATION_19_20]'s `-1` sentinel — see `RepairOrphanedProfileRowsUseCase`. */
    @Query("SELECT * FROM favorite_apps WHERE userId = -1")
    suspend fun getOrphaned(): List<FavoriteAppEntity>
}
