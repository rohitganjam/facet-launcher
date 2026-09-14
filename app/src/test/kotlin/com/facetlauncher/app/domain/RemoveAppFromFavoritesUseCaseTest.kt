package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.LauncherSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class RemoveAppFromFavoritesUseCaseTest {

    private val app = AppInfo(packageName = "com.example.mail", activityName = ".Main", label = "Mail", icon = null)

    private class Fixture {
        val settingsFlow = MutableStateFlow(LauncherSettings(activeFacetId = 1L))
        val settingsRepository = mock(SettingsRepository::class.java).also { `when`(it.settings).thenReturn(settingsFlow) }
        val facetRepository = mock(FacetRepository::class.java)
        val favoriteAppRepository = mock(FavoriteAppRepository::class.java)
        val defaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java)

        val useCase = RemoveAppFromFavoritesUseCase(settingsRepository, facetRepository, favoriteAppRepository, defaultFavoriteAppRepository)
    }

    @Test
    fun `removes from the launcher-wide default when the active facet isn't overriding favorites`() = runTest {
        val fixture = Fixture()
        `when`(fixture.facetRepository.getById(1L)).thenReturn(FacetEntity(id = 1L, name = "P1", position = 0, overridingFavorites = false))

        fixture.useCase(app)

        verify(fixture.defaultFavoriteAppRepository).removeFavorite(app)
    }

    @Test
    fun `removes from the active facet's own favorites when it's overriding`() = runTest {
        val fixture = Fixture()
        `when`(fixture.facetRepository.getById(1L)).thenReturn(FacetEntity(id = 1L, name = "P1", position = 0, overridingFavorites = true))

        fixture.useCase(app)

        verify(fixture.favoriteAppRepository).removeFavorite(1L, app)
    }
}
