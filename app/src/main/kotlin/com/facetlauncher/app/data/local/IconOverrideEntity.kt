package com.facetlauncher.app.data.local

import androidx.room.Entity
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.IconOverride
import com.facetlauncher.app.data.model.IconOverrideSource

/** One app's custom icon (see [IconOverride]). `userId` is the profile's `UserHandle.hashCode()`, as in placements. */
@Entity(tableName = "icon_overrides", primaryKeys = ["packageName", "activityName", "userId"])
data class IconOverrideEntity(
    val packageName: String,
    val activityName: String,
    val userId: Int,
    val profile: AppProfile,
    val source: String,
    val packPackage: String?,
    val drawableName: String?,
    val calendarPrefix: String?,
    val snapshotFile: String?,
    val updatedAt: Long,
)

fun IconOverrideEntity.toModel(): IconOverride = IconOverride(
    packageName = packageName,
    activityName = activityName,
    userId = userId,
    profile = profile,
    source = IconOverrideSource.entries.firstOrNull { it.name == source } ?: IconOverrideSource.OWN,
    packPackage = packPackage,
    drawableName = drawableName,
    calendarPrefix = calendarPrefix,
    snapshotFile = snapshotFile,
    updatedAt = updatedAt,
)

fun IconOverride.toEntity(): IconOverrideEntity = IconOverrideEntity(
    packageName = packageName,
    activityName = activityName,
    userId = userId,
    profile = profile,
    source = source.name,
    packPackage = packPackage,
    drawableName = drawableName,
    calendarPrefix = calendarPrefix,
    snapshotFile = snapshotFile,
    updatedAt = updatedAt,
)
