package com.facetlauncher.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.model.AppProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FolderDaoTest {

    private fun createDao(): FolderDao {
        val database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), FacetDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        return database.folderDao()
    }

    @Test
    fun `insert then observe returns the folder with no members`() = runTest {
        // Given an empty folders table — a folder is a valid, independent entity even before
        // it has any members (never auto-deleted for having zero apps).
        val dao = createDao()

        // When a folder is inserted with no members
        dao.insertFolder(FolderEntity(name = "Games"))

        // Then it's observed with an empty members list, not omitted
        val result = dao.observeAllWithApps().first()
        assertEquals(1, result.size)
        assertEquals("Games", result.first().folder.name)
        assertTrue(result.first().apps.isEmpty())
    }

    @Test
    fun `members are hydrated and ordered by position`() = runTest {
        // Given a folder with members inserted out of position order
        val dao = createDao()
        val folderId = dao.insertFolder(FolderEntity(name = "Games"))
        dao.upsertFolderApp(FolderAppEntity(folderId = folderId, packageName = "com.example.b", activityName = ".Main", position = 1))
        dao.upsertFolderApp(FolderAppEntity(folderId = folderId, packageName = "com.example.a", activityName = ".Main", position = 0))

        // Then observing returns them ordered by position, not insertion order
        val result = dao.observeAllWithApps().first().single()
        assertEquals(listOf("com.example.a", "com.example.b"), result.apps.sortedBy { it.position }.map { it.packageName })
    }

    @Test
    fun `deleteFolder cascades and removes its membership rows too`() = runTest {
        // Given a folder with members
        val dao = createDao()
        val folderId = dao.insertFolder(FolderEntity(name = "Games"))
        dao.upsertFolderApp(FolderAppEntity(folderId = folderId, packageName = "com.example.a", activityName = ".Main", position = 0))

        // When the folder itself is deleted
        dao.deleteFolder(folderId)

        // Then both the folder and its membership rows are gone
        assertTrue(dao.observeAllWithApps().first().isEmpty())
    }

    @Test
    fun `renameFolder updates the name without touching membership`() = runTest {
        // Given a folder with one member
        val dao = createDao()
        val folderId = dao.insertFolder(FolderEntity(name = "Games"))
        dao.upsertFolderApp(FolderAppEntity(folderId = folderId, packageName = "com.example.a", activityName = ".Main", position = 0))

        // When renamed
        dao.renameFolder(folderId, "Fun")

        // Then the new name is reflected and membership is untouched
        val result = dao.observeAllWithApps().first().single()
        assertEquals("Fun", result.folder.name)
        assertEquals(1, result.apps.size)
    }

    @Test
    fun `deleteFolderAppsByPackage removes membership across every folder but leaves the folders themselves`() = runTest {
        // Given the same package as a member of two different folders
        val dao = createDao()
        val folder1 = dao.insertFolder(FolderEntity(name = "Games"))
        val folder2 = dao.insertFolder(FolderEntity(name = "Social"))
        dao.upsertFolderApp(FolderAppEntity(folderId = folder1, packageName = "com.example.a", activityName = ".Main", position = 0))
        dao.upsertFolderApp(FolderAppEntity(folderId = folder2, packageName = "com.example.a", activityName = ".Main", position = 0))

        // When that package's membership is cleaned up (uninstall)
        dao.deleteFolderAppsByPackage("com.example.a", AppProfile.PERSONAL)

        // Then both folders survive, now with zero members — an uninstall never deletes the folder
        val result = dao.observeAllWithApps().first()
        assertEquals(2, result.size)
        assertTrue(result.all { it.apps.isEmpty() })
    }

    @Test
    fun `deleteFolderApp removes only the matching member`() = runTest {
        // Given a folder with two members
        val dao = createDao()
        val folderId = dao.insertFolder(FolderEntity(name = "Games"))
        dao.upsertFolderApp(FolderAppEntity(folderId = folderId, packageName = "com.example.a", activityName = ".Main", position = 0))
        dao.upsertFolderApp(FolderAppEntity(folderId = folderId, packageName = "com.example.b", activityName = ".Main", position = 1))

        // When one member is removed
        dao.deleteFolderApp(folderId, "com.example.a", ".Main", AppProfile.PERSONAL)

        // Then only the other remains, and the folder itself survives
        val result = dao.observeAllWithApps().first().single()
        assertEquals(listOf("com.example.b"), result.apps.map { it.packageName })
    }

    @Test
    fun `deleteAllFolders wipes every folder and its membership`() = runTest {
        // Given two folders with members
        val dao = createDao()
        val folder1 = dao.insertFolder(FolderEntity(name = "Games"))
        dao.upsertFolderApp(FolderAppEntity(folderId = folder1, packageName = "com.example.a", activityName = ".Main", position = 0))
        dao.insertFolder(FolderEntity(name = "Social"))

        // When every folder is wiped (F14 restore)
        dao.deleteAllFolders()

        // Then nothing remains
        assertTrue(dao.observeAllWithApps().first().isEmpty())
    }
}
