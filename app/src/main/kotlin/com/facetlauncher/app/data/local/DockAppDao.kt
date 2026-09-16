package com.facetlauncher.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.facetlauncher.app.data.model.AppProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface DockAppDao {

    @Query("SELECT * FROM dock_apps ORDER BY position ASC")
    fun observeAll(): Flow<List<DockAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(dockApp: DockAppEntity): Long

    @Delete
    suspend fun delete(dockApp: DockAppEntity)

    @Query("DELETE FROM dock_apps WHERE packageName = :packageName AND activityName = :activityName AND userId = :userId")
    suspend fun deleteByComponent(packageName: String, activityName: String, userId: Int)

    /** Uninstall cleanup — removes the dock entry for [packageName] belonging to [userId] regardless of activity. */
    @Query("DELETE FROM dock_apps WHERE packageName = :packageName AND userId = :userId")
    suspend fun deleteByPackage(packageName: String, userId: Int)

    /** Bulk cleanup for a whole profile vanishing (e.g. Work Profile unenrollment). */
    @Query("DELETE FROM dock_apps WHERE userId = :userId")
    suspend fun deleteByUserId(userId: Int)

    /** F14 Backup & Restore — wipes the whole dock before restoring from a backup. */
    @Query("DELETE FROM dock_apps")
    suspend fun deleteAll()

    /** Rows still at [Migrations.MIGRATION_19_20]'s `-1` sentinel — see `RepairOrphanedProfileRowsUseCase`. */
    @Query("SELECT * FROM dock_apps WHERE userId = -1")
    suspend fun getOrphaned(): List<DockAppEntity>
}
