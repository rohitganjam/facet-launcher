package com.facetlauncher.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface IconOverrideDao {
    @Query("SELECT * FROM icon_overrides")
    fun observeAll(): Flow<List<IconOverrideEntity>>

    @Query("SELECT * FROM icon_overrides")
    suspend fun getAll(): List<IconOverrideEntity>

    @Query("SELECT * FROM icon_overrides WHERE packageName = :packageName AND activityName = :activityName AND userId = :userId")
    suspend fun get(packageName: String, activityName: String, userId: Int): IconOverrideEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: IconOverrideEntity)

    @Query("DELETE FROM icon_overrides WHERE packageName = :packageName AND activityName = :activityName AND userId = :userId")
    suspend fun delete(packageName: String, activityName: String, userId: Int)

    @Query("DELETE FROM icon_overrides WHERE packageName = :packageName AND userId = :userId")
    suspend fun deleteByPackage(packageName: String, userId: Int)

    @Query("DELETE FROM icon_overrides WHERE userId = :userId")
    suspend fun deleteByUserId(userId: Int)

    @Query("DELETE FROM icon_overrides")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(entities: List<IconOverrideEntity>) {
        deleteAll()
        entities.forEach { upsert(it) }
    }
}
