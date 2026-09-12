package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.ProfileRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.ProfileEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.LauncherSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class AddAppToFavoritesUseCaseTest {

    private val app = AppInfo(packageName = "com.example.mail", activityName = ".Main", label = "Mail", icon = null)

    private class Fixture {
        val settingsFlow = MutableStateFlow(LauncherSettings(activeProfileId = 1L))
        val settingsRepository = mock(SettingsRepository::class.java).also { `when`(it.settings).thenReturn(settingsFlow) }
        val profileRepository = mock(ProfileRepository::class.java)
        val favoriteAppRepository = mock(FavoriteAppRepository::class.java)
        val defaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java)

        val useCase = AddAppToFavoritesUseCase(settingsRepository, profileRepository, favoriteAppRepository, defaultFavoriteAppRepository)
    }

    @Test
    fun `writes to the launcher-wide default when the active profile isn't overriding favorites`() = runTest {
        // Given profile 1 active, not overriding its own favorites, with 2 already in the default list
        val fixture = Fixture()
        `when`(fixture.profileRepository.getById(1L)).thenReturn(ProfileEntity(id = 1L, name = "P1", position = 0, overridingFavorites = false))
        `when`(fixture.defaultFavoriteAppRepository.observeDefaultFavorites()).thenReturn(flowOf(listOf(app, app)))

        // When adding an app
        fixture.useCase(app)

        // Then it's appended to the default list, at the next position
        verify(fixture.defaultFavoriteAppRepository).addFavorite(app, 2)
    }

    @Test
    fun `writes to the active profile's own favorites when it's overriding`() = runTest {
        // Given profile 1 active, overriding its own favorites, with 1 already there
        val fixture = Fixture()
        `when`(fixture.profileRepository.getById(1L)).thenReturn(ProfileEntity(id = 1L, name = "P1", position = 0, overridingFavorites = true))
        `when`(fixture.favoriteAppRepository.observeFavoritesForProfile(1L)).thenReturn(flowOf(listOf(app)))

        // When adding an app
        fixture.useCase(app)

        // Then it's appended to profile 1's own list, not the default
        verify(fixture.favoriteAppRepository).addFavorite(1L, app, 1)
    }
}
