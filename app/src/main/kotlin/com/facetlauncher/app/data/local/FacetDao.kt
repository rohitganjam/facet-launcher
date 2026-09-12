package com.facetlauncher.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FacetDao {

    @Query("SELECT * FROM facets ORDER BY position ASC")
    fun observeAll(): Flow<List<FacetEntity>>

    @Insert
    suspend fun insert(facet: FacetEntity): Long

    /**
     * Plain SQL `UPDATE ... WHERE id = ?` — unlike an `INSERT OR REPLACE`-based upsert, this never
     * deletes+reinserts the row, so it can't trigger the `favorite_apps`/`facet_dock_apps`
     * `ON DELETE CASCADE` and silently wipe those lists on an unrelated settings change.
     */
    @Update
    suspend fun update(facet: FacetEntity)

    @Delete
    suspend fun delete(facet: FacetEntity)

    @Query("SELECT * FROM facets WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): FacetEntity?

    /** F14 Backup & Restore — wipes every facet (cascades to `favorite_apps` via its FK) before restoring from a backup. */
    @Query("DELETE FROM facets")
    suspend fun deleteAll()
}
