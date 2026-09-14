package com.facetlauncher.app.data

import com.facetlauncher.app.data.model.AppInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class FolderRepositoryTest {

    private fun appInfo(letter: Char) =
        AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)

    @Test
    fun `createFolder yields a visible, hydrated 0-app folder`() = runTest {
        // Given no folders
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(emptyList()))
        val repository = FolderRepository(FakeFolderDao(), appRepository)

        // When a folder is created with no members
        val folderId = repository.createFolder("Games")

        // Then it's immediately visible, with an empty (not omitted) app list — a folder is
        // never hidden or auto-deleted for having zero apps.
        val folders = repository.observeFolders().first()
        assertEquals(1, folders.size)
        assertEquals("Games", folders.single().name)
        assertEquals(folderId, folders.single().id)
        assertTrue(folders.single().apps.isEmpty())
    }

    @Test
    fun `deleteFolder removes it from a live observeFolders emission`() = runTest {
        // Given an existing folder, observed live
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(emptyList()))
        val repository = FolderRepository(FakeFolderDao(), appRepository)
        val folderId = repository.createFolder("Games")

        val emissions = Channel<Int>(Channel.UNLIMITED)
        val collectJob = launch { repository.observeFolders().collect { emissions.send(it.size) } }
        assertEquals(1, emissions.receive())

        // When it's deleted
        repository.deleteFolder(folderId)

        // Then the live flow reflects the removal without needing to resubscribe
        assertEquals(0, emissions.receive())
        collectJob.cancel()
    }

    @Test
    fun `addAppToFolder and removeAppFromFolder never delete the folder, even back at zero members`() = runTest {
        // Given a folder with one installed member
        val app = appInfo('a')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(app)))
        val repository = FolderRepository(FakeFolderDao(), appRepository)
        val folderId = repository.createFolder("Games")
        repository.addAppToFolder(folderId, app)
        assertEquals(1, repository.observeFolders().first().single().apps.size)

        // When its only member is removed
        repository.removeAppFromFolder(folderId, app)

        // Then the folder still exists, now with zero apps — not deleted
        val folders = repository.observeFolders().first()
        assertEquals(1, folders.size)
        assertTrue(folders.single().apps.isEmpty())
    }

    @Test
    fun `an entry whose app is no longer installed is filtered out of a folder's own list`() = runTest {
        // Given a folder member that AppRepository no longer reports as installed
        val installedApps = MutableStateFlow(listOf(appInfo('a')))
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(installedApps)
        val repository = FolderRepository(FakeFolderDao(), appRepository)
        val folderId = repository.createFolder("Games")
        repository.addAppToFolder(folderId, appInfo('a'))
        assertEquals(1, repository.observeFolders().first().single().apps.size)

        // When the app is reported uninstalled
        installedApps.value = emptyList()

        // Then it's collapsed out live, but the folder (and its underlying membership row) survives
        assertTrue(repository.observeFolders().first().single().apps.isEmpty())
    }

    @Test
    fun `removeByPackage clears that package's membership across every folder without deleting them`() = runTest {
        // Given the same package as a member of two folders
        val app = appInfo('a')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(app)))
        val repository = FolderRepository(FakeFolderDao(), appRepository)
        val folder1 = repository.createFolder("Games")
        val folder2 = repository.createFolder("Social")
        repository.addAppToFolder(folder1, app)
        repository.addAppToFolder(folder2, app)

        // When that package is cleaned up (uninstall)
        repository.removeByPackage(app.packageName)

        // Then both folders survive, now with zero members
        val folders = repository.observeFolders().first()
        assertEquals(2, folders.size)
        assertTrue(folders.all { it.apps.isEmpty() })
    }

    @Test
    fun `renameFolder updates the name`() = runTest {
        // Given an existing folder
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(emptyList()))
        val repository = FolderRepository(FakeFolderDao(), appRepository)
        val folderId = repository.createFolder("Games")

        // When renamed
        repository.renameFolder(folderId, "Fun")

        // Then the new name is reflected
        assertEquals("Fun", repository.observeFolders().first().single().name)
    }

    @Test
    fun `reorderFolderApps rewrites position to match the given order`() = runTest {
        // Given a folder with three apps added in a-b-c order
        val a = appInfo('a')
        val b = appInfo('b')
        val c = appInfo('c')
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(listOf(a, b, c)))
        val repository = FolderRepository(FakeFolderDao(), appRepository)
        val folderId = repository.createFolder("Games")
        repository.addAppToFolder(folderId, a)
        repository.addAppToFolder(folderId, b)
        repository.addAppToFolder(folderId, c)
        assertEquals(listOf("a", "b", "c"), repository.observeFolders().first().single().apps.map { it.packageName.last().toString() })

        // When reordered to c-a-b
        repository.reorderFolderApps(folderId, listOf(c, a, b))

        // Then the folder's own hydrated app list reflects the new order
        val reordered = repository.observeFolders().first().single().apps.map { it.packageName.last().toString() }
        assertEquals(listOf("c", "a", "b"), reordered)
    }

    @Test
    fun `deleteAllFolders wipes every folder`() = runTest {
        // Given two folders
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeInstalledApps()).thenReturn(flowOf(emptyList()))
        val repository = FolderRepository(FakeFolderDao(), appRepository)
        repository.createFolder("Games")
        repository.createFolder("Social")

        // When every folder is wiped (F14 restore)
        repository.deleteAllFolders()

        // Then nothing remains
        assertTrue(repository.observeFolders().first().isEmpty())
    }
}
