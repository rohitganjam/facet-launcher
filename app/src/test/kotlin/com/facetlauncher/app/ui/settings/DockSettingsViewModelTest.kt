package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.ProfileDockAppRepository
import com.facetlauncher.app.data.ProfileRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.local.ProfileEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.LauncherSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class DockSettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun appInfo(n: Int) = AppInfo(packageName = "com.example.$n", activityName = ".Main", label = "App $n", icon = null)

    /** A real (not Mockito) stub — plain Mockito can't reliably stub a `suspend` return. */
    private fun fakeWallpaperRepository() =
        object : WallpaperRepository(mock(android.app.WallpaperManager::class.java)) {
            override suspend fun currentHomeWallpaper(): HomeWallpaper = HomeWallpaper.Unavailable
        }

    private fun createViewModel(
        profileId: Long? = null,
        dockApps: List<AppInfo> = emptyList(),
        profiles: List<ProfileEntity> = emptyList(),
        settingsRepository: SettingsRepository = mock(SettingsRepository::class.java),
        profileRepository: ProfileRepository = mock(ProfileRepository::class.java),
        dockAppRepository: DockAppRepository = mock(DockAppRepository::class.java),
        profileDockAppRepository: ProfileDockAppRepository = mock(ProfileDockAppRepository::class.java),
    ): DockSettingsViewModel {
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        `when`(profileRepository.observeProfiles()).thenReturn(MutableStateFlow(profiles))
        `when`(dockAppRepository.observeDockApps()).thenReturn(flowOf(dockApps))
        `when`(profileDockAppRepository.observeDockAppsForProfile(anyLong())).thenReturn(flowOf(dockApps))
        return DockSettingsViewModel(
            SavedStateHandle(profileId?.let { mapOf("profileId" to it) } ?: emptyMap()),
            settingsRepository,
            profileRepository,
            dockAppRepository,
            profileDockAppRepository,
            fakeWallpaperRepository(),
        )
    }

    @Test
    fun `dock apps flow through to uiState`() = runTest {
        val fiveApps = (1..5).map(::appInfo)
        val viewModel = createViewModel(dockApps = fiveApps)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(5, viewModel.uiState.value.dockApps.size)
    }

    @Test
    fun `global-scoped setters write to SettingsRepository and DockAppRepository`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val dockAppRepository = mock(DockAppRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository, dockAppRepository = dockAppRepository)
        val reordered = listOf(appInfo(2), appInfo(1))

        viewModel.setDockDisplayMode(DockDisplayMode.TEXT)
        viewModel.reorderDockApps(reordered)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setDockDisplayMode(DockDisplayMode.TEXT)
        verify(dockAppRepository).reorderDockApps(reordered)
    }

    @Test
    fun `profile-scoped setters write to that profile, not the launcher-wide dock`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val profileRepository = mock(ProfileRepository::class.java)
        val profileDockAppRepository = mock(ProfileDockAppRepository::class.java)
        `when`(profileDockAppRepository.observeDockAppsForProfile(anyLong())).thenReturn(flowOf(emptyList()))
        val profile = ProfileEntity(id = 7L, name = "Work", position = 0)
        `when`(profileRepository.getById(7L)).thenReturn(profile)
        val viewModel = createViewModel(
            profileId = 7L,
            profiles = listOf(profile),
            settingsRepository = settingsRepository,
            profileRepository = profileRepository,
            profileDockAppRepository = profileDockAppRepository,
        )
        val reordered = listOf(appInfo(2), appInfo(1))

        viewModel.setDockDisplayMode(DockDisplayMode.TEXT)
        viewModel.reorderDockApps(reordered)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(profileRepository).setDockDisplayMode(profile, DockDisplayMode.TEXT)
        verify(profileDockAppRepository).reorderDockApps(7L, reordered)
        verify(settingsRepository, never()).setDockDisplayMode(DockDisplayMode.TEXT)
    }
}
