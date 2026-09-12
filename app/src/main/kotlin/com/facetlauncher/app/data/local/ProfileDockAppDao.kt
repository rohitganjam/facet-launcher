package com.facetlauncher.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDockAppDao {

    @Query("SELECT * FROM profile_dock_apps WHERE profileId = :profileId ORDER BY position ASC")
    fun observeForProfile(profileId: Long): Flow<List<ProfileDockAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(dockApp: ProfileDockAppEntity): Long

    @Delete
    suspend fun delete(dockApp: ProfileDockAppEntity)

    @Query(
        "DELETE FROM profile_dock_apps WHERE profileId = :profileId " +
            "AND packageName = :packageName AND activityName = :activityName",
    )
    suspend fun deleteByComponent(profileId: Long, packageName: String, activityName: String)

    /** Uninstall cleanup — removes every profile's dock entry for [packageName], not just one. */
    @Query("DELETE FROM profile_dock_apps WHERE packageName = :packageName")
    suspend fun deleteByPackage(packageName: String)

    /** Wipes this profile's own dock entirely — used to seed a clean copy when switching to Override. */
    @Query("DELETE FROM profile_dock_apps WHERE profileId = :profileId")
    suspend fun deleteAllForProfile(profileId: Long)
}
