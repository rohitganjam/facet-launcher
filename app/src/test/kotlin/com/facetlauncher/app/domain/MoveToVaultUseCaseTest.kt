package com.facetlauncher.app.domain

import android.os.UserHandle
import com.facetlauncher.app.data.AppVaultRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.LaunchHistoryRepository
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.InOrder
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`

class MoveToVaultUseCaseTest {

    private val vault = mock(AppVaultRepository::class.java)
    private val placements = mock(PlacementRepository::class.java)
    private val folders = mock(FolderRepository::class.java)
    private val history = mock(LaunchHistoryRepository::class.java)
    private val useCase = MoveToVaultUseCase(vault, placements, folders, history)

    private val user: UserHandle = mock(UserHandle::class.java)
    private val userId = user.hashCode()

    private fun app(name: String) =
        AppInfo(packageName = "com.example.$name", activityName = ".Main", label = name, icon = null, profile = AppProfile.PERSONAL, userHandle = user)

    @Test
    fun `moving apps adds their keys first, then clears placements, folder rows and history for each`() = runTest {
        // Given two apps
        val chat = app("chat")
        val maps = app("maps")

        // When they are moved into the vault
        useCase(listOf(chat, maps))

        // Then the vault entry is written before anything is removed
        val order: InOrder = inOrder(vault, placements, folders, history)
        order.verify(vault).add(listOf("com.example.chat|$userId", "com.example.maps|$userId"))
        order.verify(placements).removeByPackage("com.example.chat", userId)
        verify(folders).removeByPackage("com.example.chat", userId)
        verify(history).removeByPackage("com.example.chat", userId)
        verify(placements).removeByPackage("com.example.maps", userId)
        verify(folders).removeByPackage("com.example.maps", userId)
        verify(history).removeByPackage("com.example.maps", userId)
    }

    @Test
    fun `placed count counts apps on Home or in a folder, not ones only in the drawer`() = runTest {
        // Given chat is a favorite, maps is in a folder, notes is nowhere
        val chat = app("chat")
        val maps = app("maps")
        val notes = app("notes")
        `when`(placements.countByPackage(chat.packageName, userId)).thenReturn(2)
        `when`(folders.countByPackage(maps.packageName, userId)).thenReturn(1)

        // Then two of the three are placed
        assertEquals(2, useCase.placedCount(listOf(chat, maps, notes)))
    }

    @Test
    fun `moving nothing writes nothing`() = runTest {
        useCase(emptyList())

        verifyNoInteractions(placements, folders, history)
    }
}
