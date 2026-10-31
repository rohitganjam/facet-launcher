package com.facetlauncher.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.facetlauncher.app.data.model.ClockAlignment
import kotlinx.coroutines.flow.Flow

@Dao
interface ClockStackItemDao {
    @Query("SELECT * FROM clock_stack_items WHERE facetId = :facetId ORDER BY position ASC, id ASC")
    fun observe(facetId: Long): Flow<List<ClockStackItemEntity>>

    @Query("SELECT * FROM clock_stack_items WHERE facetId = :facetId ORDER BY position ASC, id ASC")
    suspend fun getAll(facetId: Long): List<ClockStackItemEntity>

    @Query("SELECT * FROM clock_stack_items WHERE id = :id")
    suspend fun getById(id: Long): ClockStackItemEntity?

    @Insert
    suspend fun insert(item: ClockStackItemEntity): Long

    @Query("DELETE FROM clock_stack_items WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM clock_stack_items WHERE facetId = :facetId")
    suspend fun deleteAll(facetId: Long)

    @Query("UPDATE clock_stack_items SET position = :position WHERE id = :id")
    suspend fun setPosition(id: Long, position: Int)

    @Query("UPDATE clock_stack_items SET widthDp = :widthDp, heightDp = :heightDp WHERE id = :id")
    suspend fun setSize(id: Long, widthDp: Int, heightDp: Int)

    @Query("UPDATE clock_stack_items SET landscapeWidthDp = :widthDp, landscapeHeightDp = :heightDp WHERE id = :id")
    suspend fun setLandscapeSize(id: Long, widthDp: Int, heightDp: Int)

    @Query("UPDATE clock_stack_items SET alignment = :alignment WHERE id = :id")
    suspend fun setAlignment(id: Long, alignment: ClockAlignment?)

    @Query("UPDATE clock_stack_items SET landscapeAlignment = :alignment WHERE id = :id")
    suspend fun setLandscapeAlignment(id: Long, alignment: ClockAlignment?)
}
