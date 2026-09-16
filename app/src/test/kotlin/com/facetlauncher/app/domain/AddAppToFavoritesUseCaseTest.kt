package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.PlacedItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AddAppToFavoritesUseCaseTest {

    private val app = AppInfo(packageName = "com.example.mail", activityName = ".Main", label = "Mail", icon = null)

    private class Fixture {
        val settingsFlow = MutableStateFlow(LauncherSettings(activeFacetId = 1L))
        val settingsRepository = mock(SettingsRepository::class.java).also { `when`(it.settings).thenReturn(settingsFlow) }
        val facetRepository = mock(FacetRepository::class.java)
        val favoriteAppRepository = mock(FavoriteAppRepository::class.java)
        val defaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java)

        val useCase = AddAppToFavoritesUseCase(settingsRepository, facetRepository, favoriteAppRepository, defaultFavoriteAppRepository)
    }

    @Test
    fun `writes to the launcher-wide default when the active facet isn't overriding favorites`() = runTest {
        // Given facet 1 active, not overriding its own favorites, with 2 already in the default list
        val fixture = Fixture()
        `when`(fixture.facetRepository.getById(1L)).thenReturn(FacetEntity(id = 1L, name = "P1", position = 0, overridingFavorites = false))
        `when`(fixture.defaultFavoriteAppRepository.observeDefaultItems()).thenReturn(flowOf(listOf(app, app).map { PlacedItem.SingleApp(it) }))

        // When adding an app
        fixture.useCase(app)

        // Then it's appended to the default list, at the next position
        verify(fixture.defaultFavoriteAppRepository).addFavorite(app, 2)
    }

    @Test
    fun `writes to the active facet's own favorites when it's overriding`() = runTest {
        // Given facet 1 active, overriding its own favorites, with 1 already there
        val fixture = Fixture()
        `when`(fixture.facetRepository.getById(1L)).thenReturn(FacetEntity(id = 1L, name = "P1", position = 0, overridingFavorites = true))
        `when`(fixture.favoriteAppRepository.observeFavoriteItems(1L)).thenReturn(flowOf(listOf(app).map { PlacedItem.SingleApp(it) }))

        // When adding an app
        fixture.useCase(app)

        // Then it's appended to facet 1's own list, not the default
        verify(fixture.favoriteAppRepository).addFavorite(1L, app, 1)
    }
}
