package com.facetlauncher.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteAppDao {

    @Query("SELECT * FROM favorite_apps WHERE profileId = :profileId ORDER BY position ASC")
    fun observeForProfile(profileId: Long): Flow<List<FavoriteAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(favoriteApp: FavoriteAppEntity): Long

    @Delete
    suspend fun delete(favoriteApp: FavoriteAppEntity)

    @Query(
        "DELETE FROM favorite_apps WHERE profileId = :profileId " +
            "AND packageName = :packageName AND activityName = :activityName",
    )
    suspend fun deleteByComponent(profileId: Long, packageName: String, activityName: String)

    /** Uninstall cleanup — removes every profile's favorite entry for [packageName], not just one. */
    @Query("DELETE FROM favorite_apps WHERE packageName = :packageName")
    suspend fun deleteByPackage(packageName: String)

    /** Wipes this profile's own favorites entirely — used to seed a clean copy when switching to Override. */
    @Query("DELETE FROM favorite_apps WHERE profileId = :profileId")
    suspend fun deleteAllForProfile(profileId: Long)
}
