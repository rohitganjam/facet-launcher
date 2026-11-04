package com.facetlauncher.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetPlacementDao {

    @Query("SELECT * FROM widget_placements")
    fun observeAll(): Flow<List<WidgetPlacementEntity>>

    @Query("SELECT * FROM widget_placements WHERE appWidgetId = :appWidgetId LIMIT 1")
    suspend fun getById(appWidgetId: Int): WidgetPlacementEntity?

    @Query("SELECT MIN(appWidgetId) FROM widget_placements")
    suspend fun minAppWidgetId(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(placement: WidgetPlacementEntity)

    @Query("DELETE FROM widget_placements WHERE appWidgetId = :appWidgetId")
    suspend fun deleteById(appWidgetId: Int)

    @Query("UPDATE widget_placements SET nativeWidgetConfig = :config WHERE appWidgetId = :appWidgetId")
    suspend fun updateConfig(appWidgetId: Int, config: String?)

    @Query("DELETE FROM widget_placements WHERE nativeWidgetType IS NOT NULL")
    suspend fun deleteAllNative()

    @Delete
    suspend fun delete(placement: WidgetPlacementEntity)
}
