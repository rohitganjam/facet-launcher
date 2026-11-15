package com.facetlauncher.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface LaunchHistoryDao {
    @Query("SELECT * FROM app_launches ORDER BY lastLaunchedAt DESC")
    suspend fun lastLaunches(): List<AppLaunchEntity>

    @Query(
        "SELECT packageName, activityName, userId, SUM(count) AS total FROM app_launch_days WHERE epochDay >= :sinceEpochDay " +
            "GROUP BY packageName, activityName, userId",
    )
    suspend fun launchCountsSince(sinceEpochDay: Long): List<AppLaunchCount>

    @Query(
        "SELECT packageName, activityName, userId, SUM(count) AS total FROM app_launch_days WHERE epochDay >= :sinceEpochDay " +
            "GROUP BY packageName, activityName, userId",
    )
    fun observeLaunchCountsSince(sinceEpochDay: Long): Flow<List<AppLaunchCount>>

    @Query("SELECT * FROM search_picks")
    fun observeSearchPicks(): Flow<List<SearchPickEntity>>

    @Query(
        "INSERT OR REPLACE INTO app_launches (packageName, activityName, userId, lastLaunchedAt) " +
            "VALUES (:packageName, :activityName, :userId, :at)",
    )
    suspend fun setLastLaunched(packageName: String, activityName: String, userId: Int, at: Long)

    @Query(
        "INSERT INTO app_launch_days (packageName, activityName, userId, epochDay, count) VALUES (:packageName, :activityName, :userId, :epochDay, 1) " +
            "ON CONFLICT(packageName, activityName, userId, epochDay) DO UPDATE SET count = count + 1",
    )
    suspend fun incrementDay(packageName: String, activityName: String, userId: Int, epochDay: Long)

    @Query("DELETE FROM app_launch_days WHERE epochDay < :beforeEpochDay")
    suspend fun pruneDaysBefore(beforeEpochDay: Long)

    @Query(
        "INSERT INTO search_picks (query, packageName, activityName, userId, count, lastPickedAt) VALUES (:query, :packageName, :activityName, :userId, 1, :at) " +
            "ON CONFLICT(query, packageName, activityName, userId) DO UPDATE SET count = count + 1, lastPickedAt = :at",
    )
    suspend fun incrementPick(query: String, packageName: String, activityName: String, userId: Int, at: Long)

    /** Keeps the [keep] most recently used picks. */
    @Query("DELETE FROM search_picks WHERE rowid NOT IN (SELECT rowid FROM search_picks ORDER BY lastPickedAt DESC LIMIT :keep)")
    suspend fun trimPicks(keep: Int)

    @Transaction
    suspend fun recordLaunch(packageName: String, activityName: String, userId: Int, at: Long, epochDay: Long, keepDaysFrom: Long) {
        setLastLaunched(packageName, activityName, userId, at)
        incrementDay(packageName, activityName, userId, epochDay)
        pruneDaysBefore(keepDaysFrom)
    }

    @Transaction
    suspend fun recordPick(query: String, packageName: String, activityName: String, userId: Int, at: Long, keep: Int) {
        incrementPick(query, packageName, activityName, userId, at)
        trimPicks(keep)
    }

    @Transaction
    suspend fun removeByPackage(packageName: String, userId: Int) {
        deleteLaunches(packageName, userId)
        deleteDays(packageName, userId)
        deletePicks(packageName, userId)
    }

    @Transaction
    suspend fun removeByUserId(userId: Int) {
        deleteLaunchesForUser(userId)
        deleteDaysForUser(userId)
        deletePicksForUser(userId)
    }

    @Query("DELETE FROM app_launches WHERE packageName = :packageName AND userId = :userId")
    suspend fun deleteLaunches(packageName: String, userId: Int)

    @Query("DELETE FROM app_launch_days WHERE packageName = :packageName AND userId = :userId")
    suspend fun deleteDays(packageName: String, userId: Int)

    @Query("DELETE FROM search_picks WHERE packageName = :packageName AND userId = :userId")
    suspend fun deletePicks(packageName: String, userId: Int)

    @Query("DELETE FROM app_launches WHERE userId = :userId")
    suspend fun deleteLaunchesForUser(userId: Int)

    @Query("DELETE FROM app_launch_days WHERE userId = :userId")
    suspend fun deleteDaysForUser(userId: Int)

    @Query("DELETE FROM search_picks WHERE userId = :userId")
    suspend fun deletePicksForUser(userId: Int)
}
