package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.AppPlacementDao
import com.facetlauncher.app.data.local.AppPlacementEntity
import com.facetlauncher.app.data.local.FolderAppEntity
import com.facetlauncher.app.data.local.FolderDao
import com.facetlauncher.app.data.local.FolderEntity
import com.facetlauncher.app.data.local.FolderPlacementDao
import com.facetlauncher.app.data.local.FolderPlacementEntity
import com.facetlauncher.app.data.local.FolderWithApps
import com.facetlauncher.app.data.local.PlacementList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** Shared in-memory fakes for the folder-placement DAOs, reused across this package's repository tests — simpler than mocking every DAO method for each test's needs. */

/** [deleteFolder] simulates Room's real cascade delete of its membership rows. No [deleteEmptyFolders]-equivalent — a 0-app folder is never auto-deleted. */
internal class FakeFolderDao : FolderDao {
    private var nextFolderId = 1L
    private var nextFolderAppId = 1L
    private val folders = MutableStateFlow<List<FolderEntity>>(emptyList())
    private val folderApps = MutableStateFlow<List<FolderAppEntity>>(emptyList())

    override fun observeAllWithApps(): Flow<List<FolderWithApps>> =
        combine(folders, folderApps) { folderList, appList ->
            folderList.map { folder -> FolderWithApps(folder, appList.filter { it.folderId == folder.id }) }
        }

    override suspend fun insertFolder(folder: FolderEntity): Long {
        val id = if (folder.id != 0L) folder.id else nextFolderId++
        folders.value = folders.value.filterNot { it.id == id } + folder.copy(id = id)
        return id
    }

    override suspend fun renameFolder(folderId: Long, name: String) {
        folders.value = folders.value.map { if (it.id == folderId) it.copy(name = name) else it }
    }

    override suspend fun deleteFolder(folderId: Long) {
        folders.value = folders.value.filterNot { it.id == folderId }
        folderApps.value = folderApps.value.filterNot { it.folderId == folderId }
    }

    override suspend fun deleteAllFolders() {
        folders.value = emptyList()
        folderApps.value = emptyList()
    }

    override suspend fun upsertFolderApp(folderApp: FolderAppEntity): Long {
        val id = if (folderApp.id != 0L) folderApp.id else nextFolderAppId++
        val toStore = folderApp.copy(id = id)
        folderApps.value = folderApps.value.filterNot {
            it.folderId == toStore.folderId && it.packageName == toStore.packageName && it.activityName == toStore.activityName && it.userId == toStore.userId
        } + toStore
        return id
    }

    override suspend fun deleteFolderApp(folderId: Long, packageName: String, activityName: String, userId: Int) {
        folderApps.value = folderApps.value.filterNot { it.folderId == folderId && it.packageName == packageName && it.activityName == activityName && it.userId == userId }
    }

    override suspend fun deleteFolderAppsByPackage(packageName: String, userId: Int) {
        folderApps.value = folderApps.value.filterNot { it.packageName == packageName && it.userId == userId }
    }

    override suspend fun deleteFolderAppsByUserId(userId: Int) {
        folderApps.value = folderApps.value.filterNot { it.userId == userId }
    }

    override suspend fun getOrphanedFolderApps(): List<FolderAppEntity> = folderApps.value.filter { it.userId == -1 }
}

/** In-memory [FolderPlacementDao]: `upsert` replaces on the real table's unique key `(list, ownerKey, folderId)`. */
internal class FakeFolderPlacementDao : FolderPlacementDao {
    private var nextId = 1L
    private val state = MutableStateFlow<List<FolderPlacementEntity>>(emptyList())

    override fun observe(list: PlacementList, ownerKey: Long): Flow<List<FolderPlacementEntity>> =
        state.map { rows -> rows.filter { it.list == list && it.ownerKey == ownerKey }.sortedBy { it.position } }

    override suspend fun upsert(placement: FolderPlacementEntity): Long {
        val id = if (placement.id != 0L) placement.id else nextId++
        state.value = state.value.filterNot {
            it.id == id || (it.list == placement.list && it.ownerKey == placement.ownerKey && it.folderId == placement.folderId)
        } + placement.copy(id = id)
        return id
    }

    override suspend fun deleteByFolderId(list: PlacementList, ownerKey: Long, folderId: Long) {
        state.value = state.value.filterNot { it.list == list && it.ownerKey == ownerKey && it.folderId == folderId }
    }

    override suspend fun deleteAllForOwner(list: PlacementList, ownerKey: Long) {
        state.value = state.value.filterNot { it.list == list && it.ownerKey == ownerKey }
    }
}

/** In-memory [AppPlacementDao]: `upsert` replaces on the real table's unique key `(list, ownerKey, packageName, activityName, userId)`. */
internal class FakeAppPlacementDao : AppPlacementDao {
    private var nextId = 1L
    private val state = MutableStateFlow<List<AppPlacementEntity>>(emptyList())

    override fun observe(list: PlacementList, ownerKey: Long): Flow<List<AppPlacementEntity>> =
        state.map { rows -> rows.filter { it.list == list && it.ownerKey == ownerKey }.sortedBy { it.position } }

    override suspend fun upsert(placement: AppPlacementEntity): Long {
        val id = if (placement.id != 0L) placement.id else nextId++
        state.value = state.value.filterNot {
            it.id == id || (
                it.list == placement.list && it.ownerKey == placement.ownerKey && it.packageName == placement.packageName &&
                    it.activityName == placement.activityName && it.userId == placement.userId
                )
        } + placement.copy(id = id)
        return id
    }

    override suspend fun deleteByComponent(list: PlacementList, ownerKey: Long, packageName: String, activityName: String, userId: Int) {
        state.value = state.value.filterNot {
            it.list == list && it.ownerKey == ownerKey && it.packageName == packageName && it.activityName == activityName && it.userId == userId
        }
    }

    override suspend fun deleteByPackage(list: PlacementList, global: Boolean, packageName: String, userId: Int) {
        state.value = state.value.filterNot { it.list == list && (it.ownerKey == 0L) == global && it.packageName == packageName && it.userId == userId }
    }

    override suspend fun deleteByUserId(list: PlacementList, global: Boolean, userId: Int) {
        state.value = state.value.filterNot { it.list == list && (it.ownerKey == 0L) == global && it.userId == userId }
    }

    override suspend fun deleteAllForOwner(list: PlacementList, ownerKey: Long) {
        state.value = state.value.filterNot { it.list == list && it.ownerKey == ownerKey }
    }

    override suspend fun getOrphaned(list: PlacementList, global: Boolean): List<AppPlacementEntity> =
        state.value.filter { it.list == list && (it.ownerKey == 0L) == global && it.userId == -1 }
}
