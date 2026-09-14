package com.facetlauncher.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.facetlauncher.app.data.model.AppProfile

@Entity(
    tableName = "dock_apps",
    indices = [Index(value = ["packageName", "activityName", "profile"], unique = true)],
)
data class DockAppEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val activityName: String,
    val position: Int,
    val profile: AppProfile = AppProfile.PERSONAL,
)
