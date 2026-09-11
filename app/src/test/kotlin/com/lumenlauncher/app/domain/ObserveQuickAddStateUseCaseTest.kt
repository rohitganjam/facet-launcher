package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.DockAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import com.lumenlauncher.app.data.ProfileDockAppRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.LauncherSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ObserveQuickAddStateUseCaseTest {

    private fun appInfo(letter: Char) =
        AppInfo(packageName = "com.example.$letter", activityName = ".Main", label = "$letter App", icon = null)

    private class Fixture {
        val settingsFlow = MutableStateFlow(LauncherSettings(activeProfileId = 1L))
        val settingsRepository = mock(SettingsRepository::class.java).also { `when`(it.settings).thenReturn(settingsFlow) }
        val profilesFlow = MutableStateFlow<List<ProfileEntity>>(listOf(ProfileEntity(id = 1L, name = "P1", position = 0)))
        val profileRepository = mock(ProfileRepository::class.java).also { `when`(it.observeProfiles()).thenReturn(profilesFlow) }
        val favoriteAppRepository = mock(FavoriteAppRepository::class.java)
        val defaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java)
            .also { `when`(it.observeDefaultFavorites()).thenReturn(flowOf(emptyList())) }
        val dockAppRepository = mock(DockAppRepository::class.java).also { `when`(it.observeDockApps()).thenReturn(flowOf(emptyList())) }
        val profileDockAppRepository = mock(ProfileDockAppRepository::class.java)

        val useCase = ObserveQuickAddStateUseCase(
            settingsRepository,
            profileRepository,
            favoriteAppRepository,
            defaultFavoriteAppRepository,
            dockAppRepository,
            profileDockAppRepository,
        )
    }

    @Test
    fun `shows Add to Favorites against the launcher-wide default when the active profile isn't overriding`() = runTest {
        val fixture = Fixture()

        val result = fixture.useCase().first()

        assertEquals(false, result.favoritesOverride)
    }

    @Test
    fun `shows Add to profile favorites when the active profile overrides its own`() = runTest {
        val fixture = Fixture()
        fixture.profilesFlow.value = listOf(ProfileEntity(id = 1L, name = "P1", position = 0, overridingFavorites = true))
        `when`(fixture.favoriteAppRepository.observeFavoritesForProfile(1L)).thenReturn(flowOf(emptyList()))

        val result = fixture.useCase().first()

        assertEquals(true, result.favoritesOverride)
    }

    @Test
    fun `hides Add to Favorites once the effective list is already at the cap`() = runTest {
        val fixture = Fixture()
        val fullFavorites = (1..6).map { appInfo('a' + it) }
        `when`(fixture.defaultFavoriteAppRepository.observeDefaultFavorites()).thenReturn(flowOf(fullFavorites))

        val result = fixture.useCase().first()

        assertEquals(null, result.favoritesOverride)
    }

    @Test
    fun `shows Add to Dock against the launcher-wide default when the active profile isn't overriding`() = runTest {
        val fixture = Fixture()

        val result = fixture.useCase().first()

        assertEquals(false, result.dockOverride)
    }

    @Test
    fun `shows Add to profile dock when the active profile overrides its own`() = runTest {
        val fixture = Fixture()
        fixture.profilesFlow.value = listOf(ProfileEntity(id = 1L, name = "P1", position = 0, overrideDock = true))
        `when`(fixture.profileDockAppRepository.observeDockAppsForProfile(1L)).thenReturn(flowOf(emptyList()))

        val result = fixture.useCase().first()

        assertEquals(true, result.dockOverride)
    }

    @Test
    fun `hides Add to Dock once the effective dock is already at the cap`() = runTest {
        val fixture = Fixture()
        val fullDock = (1..5).map { appInfo('a' + it) }
        `when`(fixture.dockAppRepository.observeDockApps()).thenReturn(flowOf(fullDock))

        val result = fixture.useCase().first()

        assertEquals(null, result.dockOverride)
    }
}
