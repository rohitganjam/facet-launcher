package com.facetlauncher.app.data.local

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.facetlauncher.app.data.model.AppProfile
import kotlinx.coroutines.flow.Flow

data class FolderWithApps(
    @Embedded val folder: FolderEntity,
    @Relation(parentColumn = "id", entityColumn = "folderId")
    val apps: List<FolderAppEntity>,
)

@Dao
interface FolderDao {

    @Transaction
    @Query("SELECT * FROM folders")
    fun observeAllWithApps(): Flow<List<FolderWithApps>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Query("UPDATE folders SET name = :name WHERE id = :folderId")
    suspend fun renameFolder(folderId: Long, name: String)

    /** Cascades through `folder_apps` and every placement table via their own foreign keys. */
    @Query("DELETE FROM folders WHERE id = :folderId")
    suspend fun deleteFolder(folderId: Long)

    /** F14 Backup & Restore — wipes every folder (and, via cascade, its membership + every placement) before restoring from a backup. */
    @Query("DELETE FROM folders")
    suspend fun deleteAllFolders()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFolderApp(folderApp: FolderAppEntity): Long

    @Query("DELETE FROM folder_apps WHERE folderId = :folderId AND packageName = :packageName AND activityName = :activityName AND profile = :profile")
    suspend fun deleteFolderApp(folderId: Long, packageName: String, activityName: String, profile: AppProfile)

    /** Uninstall cleanup — removes every folder's membership row for [packageName] in [profile] regardless of activity. Deliberately does not touch the folder itself — a folder emptied by an uninstall stays a valid, visible, 0-app folder. */
    @Query("DELETE FROM folder_apps WHERE packageName = :packageName AND profile = :profile")
    suspend fun deleteFolderAppsByPackage(packageName: String, profile: AppProfile)

    /** Bulk cleanup for a whole profile vanishing (e.g. Work Profile unenrollment). */
    @Query("DELETE FROM folder_apps WHERE profile = :profile")
    suspend fun deleteFolderAppsByProfile(profile: AppProfile)
}
