package com.facetlauncher.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profiles ORDER BY position ASC")
    fun observeAll(): Flow<List<ProfileEntity>>

    @Insert
    suspend fun insert(profile: ProfileEntity): Long

    /**
     * Plain SQL `UPDATE ... WHERE id = ?` — unlike an `INSERT OR REPLACE`-based upsert, this never
     * deletes+reinserts the row, so it can't trigger the `favorite_apps`/`profile_dock_apps`
     * `ON DELETE CASCADE` and silently wipe those lists on an unrelated settings change.
     */
    @Update
    suspend fun update(profile: ProfileEntity)

    @Delete
    suspend fun delete(profile: ProfileEntity)

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ProfileEntity?

    /** F14 Backup & Restore — wipes every profile (cascades to `favorite_apps` via its FK) before restoring from a backup. */
    @Query("DELETE FROM profiles")
    suspend fun deleteAll()
}
