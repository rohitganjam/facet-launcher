package com.lumenlauncher.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DockAppDao {

    @Query("SELECT * FROM dock_apps ORDER BY position ASC")
    fun observeAll(): Flow<List<DockAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(dockApp: DockAppEntity): Long

    @Delete
    suspend fun delete(dockApp: DockAppEntity)

    @Query("DELETE FROM dock_apps WHERE packageName = :packageName AND activityName = :activityName")
    suspend fun deleteByComponent(packageName: String, activityName: String)

    /** Uninstall cleanup — removes the dock entry for [packageName] regardless of activity. */
    @Query("DELETE FROM dock_apps WHERE packageName = :packageName")
    suspend fun deleteByPackage(packageName: String)

    /** F14 Backup & Restore — wipes the whole dock before restoring from a backup. */
    @Query("DELETE FROM dock_apps")
    suspend fun deleteAll()
}
