package com.facetlauncher.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AutomationRuleDao {

    @Query("SELECT * FROM automation_rules ORDER BY position ASC, id ASC")
    fun observeAll(): Flow<List<AutomationRuleEntity>>

    @Query("SELECT * FROM automation_rules WHERE id = :id")
    suspend fun getById(id: Long): AutomationRuleEntity?

    @Query("SELECT COALESCE(MAX(position), -1) FROM automation_rules")
    suspend fun maxPosition(): Int

    @Insert
    suspend fun insert(rule: AutomationRuleEntity): Long

    @Update
    suspend fun update(rule: AutomationRuleEntity)

    @Query("UPDATE automation_rules SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("DELETE FROM automation_rules WHERE id = :id")
    suspend fun deleteById(id: Long)
}
