package com.lumenlauncher.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DefaultFavoriteAppDao {

    @Query("SELECT * FROM default_favorite_apps ORDER BY position ASC")
    fun observeAll(): Flow<List<DefaultFavoriteAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(defaultFavoriteApp: DefaultFavoriteAppEntity): Long

    @Delete
    suspend fun delete(defaultFavoriteApp: DefaultFavoriteAppEntity)

    @Query("DELETE FROM default_favorite_apps WHERE packageName = :packageName AND activityName = :activityName")
    suspend fun deleteByComponent(packageName: String, activityName: String)

    /** Uninstall cleanup — removes the default-favorite entry for [packageName] regardless of activity. */
    @Query("DELETE FROM default_favorite_apps WHERE packageName = :packageName")
    suspend fun deleteByPackage(packageName: String)
}
