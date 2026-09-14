package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.PlacedItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class AddFolderToFavoritesUseCaseTest {

    private val app = AppInfo(packageName = "com.example.mail", activityName = ".Main", label = "Mail", icon = null)
    private val folder = Folder(id = 9L, name = "Games", apps = listOf(app))

    private class Fixture {
        val settingsFlow = MutableStateFlow(LauncherSettings(activeFacetId = 1L))
        val settingsRepository = mock(SettingsRepository::class.java).also { `when`(it.settings).thenReturn(settingsFlow) }
        val facetRepository = mock(FacetRepository::class.java)
        val favoriteAppRepository = mock(FavoriteAppRepository::class.java)
        val defaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java)

        val useCase = AddFolderToFavoritesUseCase(settingsRepository, facetRepository, favoriteAppRepository, defaultFavoriteAppRepository)
    }

    @Test
    fun `places into the launcher-wide default favorites when the active facet isn't overriding`() = runTest {
        val fixture = Fixture()
        `when`(fixture.facetRepository.getById(1L)).thenReturn(FacetEntity(id = 1L, name = "P1", position = 0, overridingFavorites = false))
        `when`(fixture.defaultFavoriteAppRepository.observeDefaultItems()).thenReturn(flowOf(listOf(PlacedItem.SingleApp(app))))

        fixture.useCase(folder)

        verify(fixture.defaultFavoriteAppRepository).placeFolder(9L, 1)
    }

    @Test
    fun `places into the active facet's own favorites when it's overriding`() = runTest {
        val fixture = Fixture()
        `when`(fixture.facetRepository.getById(1L)).thenReturn(FacetEntity(id = 1L, name = "P1", position = 0, overridingFavorites = true))
        `when`(fixture.favoriteAppRepository.observeFavoriteItems(1L)).thenReturn(flowOf(emptyList()))

        fixture.useCase(folder)

        verify(fixture.favoriteAppRepository).placeFolder(1L, 9L, 0)
    }
}
