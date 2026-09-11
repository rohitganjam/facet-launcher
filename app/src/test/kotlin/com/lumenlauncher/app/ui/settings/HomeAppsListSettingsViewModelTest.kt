package com.lumenlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import com.lumenlauncher.app.data.DefaultAppRepository
import com.lumenlauncher.app.data.DefaultFavoriteAppRepository
import com.lumenlauncher.app.data.FavoriteAppRepository
import com.lumenlauncher.app.data.ProfileRepository
import com.lumenlauncher.app.data.SettingsRepository
import com.lumenlauncher.app.data.WallpaperRepository
import com.lumenlauncher.app.data.local.ProfileEntity
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.AppListVerticalAlignment
import com.lumenlauncher.app.data.model.AppRowPosition
import com.lumenlauncher.app.data.model.AppRowPresentation
import com.lumenlauncher.app.data.model.HomeWallpaper
import com.lumenlauncher.app.data.model.LauncherSettings
import com.lumenlauncher.app.data.model.ListContentMode
import com.lumenlauncher.app.domain.GetInstalledAppsUseCase
import com.lumenlauncher.app.domain.SelectPreviewAppsUseCase
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

/** A real (not Mockito) stub — plain Mockito can't reliably stub a `suspend` return. */
private fun fakeWallpaperRepository() =
    object : WallpaperRepository(mock(android.app.WallpaperManager::class.java)) {
        override suspend fun currentHomeWallpaper(): HomeWallpaper = HomeWallpaper.Unavailable
    }

@OptIn(ExperimentalCoroutinesApi::class)
class HomeAppsListSettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun appInfo(n: Int) = AppInfo(packageName = "com.example.$n", activityName = ".Main", label = "App $n", icon = null)

    private suspend fun createViewModel(
        profileId: Long? = null,
        favorites: List<AppInfo> = emptyList(),
        profiles: List<ProfileEntity> = emptyList(),
        settingsRepository: SettingsRepository = mock(SettingsRepository::class.java),
        profileRepository: ProfileRepository = mock(ProfileRepository::class.java),
        favoriteAppRepository: FavoriteAppRepository = mock(FavoriteAppRepository::class.java),
        defaultFavoriteAppRepository: DefaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java),
    ): HomeAppsListSettingsViewModel {
        val getInstalledApps = mock(GetInstalledAppsUseCase::class.java)
        val defaultAppRepository = mock(DefaultAppRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        `when`(profileRepository.observeProfiles()).thenReturn(MutableStateFlow(profiles))
        `when`(favoriteAppRepository.observeFavoritesForProfile(anyLong())).thenReturn(flowOf(favorites))
        `when`(defaultFavoriteAppRepository.observeDefaultFavorites()).thenReturn(flowOf(favorites))
        `when`(getInstalledApps.observe()).thenReturn(flowOf(emptyList()))
        `when`(defaultAppRepository.getDefaultAppPackages()).thenReturn(emptyList())
        return HomeAppsListSettingsViewModel(
            SavedStateHandle(profileId?.let { mapOf("profileId" to it) } ?: emptyMap()),
            settingsRepository,
            profileRepository,
            favoriteAppRepository,
            defaultFavoriteAppRepository,
            fakeWallpaperRepository(),
            defaultAppRepository,
            getInstalledApps,
            SelectPreviewAppsUseCase(),
        )
    }

    @Test
    fun `favorites flow through to uiState`() = runTest {
        val twoFavorites = (1..2).map(::appInfo)
        val viewModel = createViewModel(favorites = twoFavorites)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.favorites.size)
    }

    @Test
    fun `global-scoped setter writes to SettingsRepository`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setAppRowPosition(AppRowPosition.RIGHT)
        viewModel.setAppRowPresentation(AppRowPresentation.TEXT_ONLY)
        viewModel.setListContentMode(ListContentMode.RECENTS)
        viewModel.setAppsToShowCount(7)
        viewModel.setAppListVerticalAlignment(AppListVerticalAlignment.TOP)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setAppRowPosition(AppRowPosition.RIGHT)
        verify(settingsRepository).setAppRowPresentation(AppRowPresentation.TEXT_ONLY)
        verify(settingsRepository).setListContentMode(ListContentMode.RECENTS)
        verify(settingsRepository).setAppsToShowCount(7)
        verify(settingsRepository).setAppListVerticalAlignment(AppListVerticalAlignment.TOP)
    }

    @Test
    fun `profile-scoped setter writes to that profile's row, not SettingsRepository`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val profileRepository = mock(ProfileRepository::class.java)
        val profile = ProfileEntity(id = 7L, name = "Work", position = 0)
        `when`(profileRepository.getById(7L)).thenReturn(profile)
        val viewModel = createViewModel(
            profileId = 7L,
            profiles = listOf(profile),
            settingsRepository = settingsRepository,
            profileRepository = profileRepository,
        )

        viewModel.setAppRowPosition(AppRowPosition.RIGHT)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(profileRepository).setAppRowPosition(profile, AppRowPosition.RIGHT)
        verify(settingsRepository, never()).setAppRowPosition(AppRowPosition.RIGHT)
    }

    @Test
    fun `global reorderFavorites hits the default list`() = runTest {
        val defaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java)
        val viewModel = createViewModel(defaultFavoriteAppRepository = defaultFavoriteAppRepository)
        val reordered = listOf(appInfo(2), appInfo(1))

        viewModel.reorderFavorites(reordered)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(defaultFavoriteAppRepository).reorderFavorites(reordered)
    }

    @Test
    fun `profile-scoped reorderFavorites hits that profile's list`() = runTest {
        val favoriteAppRepository = mock(FavoriteAppRepository::class.java)
        val viewModel = createViewModel(profileId = 7L, favoriteAppRepository = favoriteAppRepository)
        val reordered = listOf(appInfo(2), appInfo(1))

        viewModel.reorderFavorites(reordered)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(favoriteAppRepository).reorderFavorites(7L, reordered)
    }
}
