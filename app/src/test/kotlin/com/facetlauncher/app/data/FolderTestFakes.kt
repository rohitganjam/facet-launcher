package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.DefaultFavoriteFolderPlacementDao
import com.facetlauncher.app.data.local.DefaultFavoriteFolderPlacementEntity
import com.facetlauncher.app.data.local.DockFolderPlacementDao
import com.facetlauncher.app.data.local.DockFolderPlacementEntity
import com.facetlauncher.app.data.local.FacetDockFolderPlacementDao
import com.facetlauncher.app.data.local.FacetDockFolderPlacementEntity
import com.facetlauncher.app.data.local.FavoriteFolderPlacementDao
import com.facetlauncher.app.data.local.FavoriteFolderPlacementEntity
import com.facetlauncher.app.data.local.FolderAppEntity
import com.facetlauncher.app.data.local.FolderDao
import com.facetlauncher.app.data.local.FolderEntity
import com.facetlauncher.app.data.local.FolderWithApps
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
            it.folderId == toStore.folderId && it.packageName == toStore.packageName && it.activityName == toStore.activityName
        } + toStore
        return id
    }

    override suspend fun deleteFolderApp(folderId: Long, packageName: String, activityName: String) {
        folderApps.value = folderApps.value.filterNot { it.folderId == folderId && it.packageName == packageName && it.activityName == activityName }
    }

    override suspend fun deleteFolderAppsByPackage(packageName: String) {
        folderApps.value = folderApps.value.filterNot { it.packageName == packageName }
    }
}

internal class FakeDockFolderPlacementDao : DockFolderPlacementDao {
    private var nextId = 1L
    private val state = MutableStateFlow<List<DockFolderPlacementEntity>>(emptyList())

    override fun observeAll(): Flow<List<DockFolderPlacementEntity>> = state

    override suspend fun upsert(placement: DockFolderPlacementEntity): Long {
        val id = if (placement.id != 0L) placement.id else nextId++
        state.value = state.value.filterNot { it.folderId == placement.folderId } + placement.copy(id = id)
        return id
    }

    override suspend fun deleteByFolderId(folderId: Long) {
        state.value = state.value.filterNot { it.folderId == folderId }
    }

    override suspend fun deleteAll() {
        state.value = emptyList()
    }
}

internal class FakeDefaultFavoriteFolderPlacementDao : DefaultFavoriteFolderPlacementDao {
    private var nextId = 1L
    private val state = MutableStateFlow<List<DefaultFavoriteFolderPlacementEntity>>(emptyList())

    override fun observeAll(): Flow<List<DefaultFavoriteFolderPlacementEntity>> = state

    override suspend fun upsert(placement: DefaultFavoriteFolderPlacementEntity): Long {
        val id = if (placement.id != 0L) placement.id else nextId++
        state.value = state.value.filterNot { it.folderId == placement.folderId } + placement.copy(id = id)
        return id
    }

    override suspend fun deleteByFolderId(folderId: Long) {
        state.value = state.value.filterNot { it.folderId == folderId }
    }

    override suspend fun deleteAll() {
        state.value = emptyList()
    }
}

internal class FakeFacetDockFolderPlacementDao : FacetDockFolderPlacementDao {
    private var nextId = 1L
    private val state = MutableStateFlow<List<FacetDockFolderPlacementEntity>>(emptyList())

    override fun observeForFacet(facetId: Long): Flow<List<FacetDockFolderPlacementEntity>> =
        state.map { entries -> entries.filter { it.facetId == facetId } }

    override suspend fun upsert(placement: FacetDockFolderPlacementEntity): Long {
        val id = if (placement.id != 0L) placement.id else nextId++
        state.value = state.value.filterNot {
            it.facetId == placement.facetId && it.folderId == placement.folderId
        } + placement.copy(id = id)
        return id
    }

    override suspend fun deleteByFolderId(facetId: Long, folderId: Long) {
        state.value = state.value.filterNot { it.facetId == facetId && it.folderId == folderId }
    }

    override suspend fun deleteAllForFacet(facetId: Long) {
        state.value = state.value.filterNot { it.facetId == facetId }
    }

    override suspend fun deleteAll() {
        state.value = emptyList()
    }
}

internal class FakeFavoriteFolderPlacementDao : FavoriteFolderPlacementDao {
    private var nextId = 1L
    private val state = MutableStateFlow<List<FavoriteFolderPlacementEntity>>(emptyList())

    override fun observeForFacet(facetId: Long): Flow<List<FavoriteFolderPlacementEntity>> =
        state.map { entries -> entries.filter { it.facetId == facetId } }

    override suspend fun upsert(placement: FavoriteFolderPlacementEntity): Long {
        val id = if (placement.id != 0L) placement.id else nextId++
        state.value = state.value.filterNot {
            it.facetId == placement.facetId && it.folderId == placement.folderId
        } + placement.copy(id = id)
        return id
    }

    override suspend fun deleteByFolderId(facetId: Long, folderId: Long) {
        state.value = state.value.filterNot { it.facetId == facetId && it.folderId == folderId }
    }

    override suspend fun deleteAllForFacet(facetId: Long) {
        state.value = state.value.filterNot { it.facetId == facetId }
    }

    override suspend fun deleteAll() {
        state.value = emptyList()
    }
}
