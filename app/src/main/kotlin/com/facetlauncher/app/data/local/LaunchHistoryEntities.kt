package com.facetlauncher.app.data.local

import androidx.room.Entity
import androidx.room.Index

/** When an app (one activity in one profile) was last opened from Facet. `userId` is the profile's `UserHandle.hashCode()`, as in placements. */
@Entity(tableName = "app_launches", primaryKeys = ["packageName", "activityName", "userId"])
data class AppLaunchEntity(
    val packageName: String,
    val activityName: String,
    val userId: Int,
    val lastLaunchedAt: Long,
)

/** How many times an app was opened from Facet on one day ([epochDay]); rows older than the Most used window are pruned. */
@Entity(
    tableName = "app_launch_days",
    primaryKeys = ["packageName", "activityName", "userId", "epochDay"],
    indices = [Index(value = ["epochDay"])],
)
data class AppLaunchDayEntity(
    val packageName: String,
    val activityName: String,
    val userId: Int,
    val epochDay: Long,
    val count: Int,
)

/** How many times an app was opened from drawer search for one normalized [query]. */
@Entity(
    tableName = "search_picks",
    primaryKeys = ["query", "packageName", "activityName", "userId"],
    indices = [Index(value = ["lastPickedAt"])],
)
data class SearchPickEntity(
    val query: String,
    val packageName: String,
    val activityName: String,
    val userId: Int,
    val count: Int,
    val lastPickedAt: Long,
)

/** One app's launches summed over a window, from [LaunchHistoryDao.launchCountsSince]. */
data class AppLaunchCount(
    val packageName: String,
    val activityName: String,
    val userId: Int,
    val total: Int,
)
