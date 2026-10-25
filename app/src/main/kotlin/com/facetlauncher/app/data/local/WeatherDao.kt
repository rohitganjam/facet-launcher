package com.facetlauncher.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Query("SELECT * FROM weather_hours ORDER BY timeEpochSecond ASC")
    fun observeHours(): Flow<List<WeatherHourEntity>>

    @Query("SELECT * FROM weather_hours ORDER BY timeEpochSecond ASC")
    suspend fun getHours(): List<WeatherHourEntity>

    @Query("DELETE FROM weather_hours")
    suspend fun clearHours()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHours(hours: List<WeatherHourEntity>)

    @Query("SELECT * FROM weather_meta WHERE id = 1")
    fun observeMeta(): Flow<WeatherMetaEntity?>

    @Query("SELECT * FROM weather_meta WHERE id = 1")
    suspend fun getMeta(): WeatherMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeta(meta: WeatherMetaEntity)
}
