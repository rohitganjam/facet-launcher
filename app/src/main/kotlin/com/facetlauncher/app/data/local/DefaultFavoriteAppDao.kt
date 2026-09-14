package com.facetlauncher.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.facetlauncher.app.data.model.AppProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface DefaultFavoriteAppDao {

    @Query("SELECT * FROM default_favorite_apps ORDER BY position ASC")
    fun observeAll(): Flow<List<DefaultFavoriteAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(defaultFavoriteApp: DefaultFavoriteAppEntity): Long

    @Delete
    suspend fun delete(defaultFavoriteApp: DefaultFavoriteAppEntity)

    @Query("DELETE FROM default_favorite_apps WHERE packageName = :packageName AND activityName = :activityName AND profile = :profile")
    suspend fun deleteByComponent(packageName: String, activityName: String, profile: AppProfile)

    /** Uninstall cleanup — removes the default-favorite entry for [packageName] in [profile] regardless of activity. */
    @Query("DELETE FROM default_favorite_apps WHERE packageName = :packageName AND profile = :profile")
    suspend fun deleteByPackage(packageName: String, profile: AppProfile)

    /** Bulk cleanup for a whole profile vanishing (e.g. Work Profile unenrollment). */
    @Query("DELETE FROM default_favorite_apps WHERE profile = :profile")
    suspend fun deleteByProfile(profile: AppProfile)

    /** F14 Backup & Restore — wipes the whole list before restoring from a backup. */
    @Query("DELETE FROM default_favorite_apps")
    suspend fun deleteAll()
}
