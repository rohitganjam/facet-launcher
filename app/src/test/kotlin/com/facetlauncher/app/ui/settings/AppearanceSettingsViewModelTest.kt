package com.facetlauncher.app.ui.settings

import com.facetlauncher.app.data.DefaultAppRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.IconRenderMode
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ThemeMode
import com.facetlauncher.app.data.model.WallpaperAccentRole
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import com.facetlauncher.app.domain.SelectPreviewAppsUseCase
import com.facetlauncher.app.ui.theme.AccentSwatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class AppearanceSettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    /** A real (not Mockito) stub — plain Mockito can't reliably stub a `suspend` return. */
    private fun fakeWallpaperRepository(value: HomeWallpaper = HomeWallpaper.Unavailable) =
        object : WallpaperRepository(mock(android.app.WallpaperManager::class.java)) {
            override suspend fun currentHomeWallpaper(): HomeWallpaper = value
        }

    private suspend fun createViewModel(
        settingsRepository: SettingsRepository = mock(SettingsRepository::class.java),
        getInstalledApps: GetInstalledAppsUseCase = mock(GetInstalledAppsUseCase::class.java),
        defaultAppRepository: DefaultAppRepository = mock(DefaultAppRepository::class.java),
        wallpaperRepository: WallpaperRepository = fakeWallpaperRepository(),
    ): AppearanceSettingsViewModel {
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        `when`(getInstalledApps.observe()).thenReturn(flowOf(emptyList()))
        `when`(defaultAppRepository.getDefaultAppPackages()).thenReturn(emptyList())
        return AppearanceSettingsViewModel(settingsRepository, getInstalledApps, defaultAppRepository, wallpaperRepository, SelectPreviewAppsUseCase())
    }

    @Test
    fun `changing theme mode calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setThemeMode(ThemeMode.DARK)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setThemeMode(ThemeMode.DARK)
    }

    @Test
    fun `changing accent source calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setAccentFromSystem(false)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setAccentFromSystem(false)
    }

    @Test
    fun `changing custom accent swatch calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setCustomAccentSwatch(AccentSwatch.TEAL)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setCustomAccentSwatch("TEAL")
    }

    @Test
    fun `changing wallpaper accent role calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setWallpaperAccentRole(WallpaperAccentRole.TERTIARY)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setWallpaperAccentRole(WallpaperAccentRole.TERTIARY)
    }

    @Test
    fun `changing icon render mode calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setIconRenderMode(IconRenderMode.MONOCHROME_BLACK_WHITE)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setIconRenderMode(IconRenderMode.MONOCHROME_BLACK_WHITE)
    }

    @Test
    fun `changing launcher font option calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setLauncherFontOption(LauncherFontOption.NOTO_SANS)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setLauncherFontOption(LauncherFontOption.NOTO_SANS)
    }

    @Test
    fun `changing app label color option calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setAppLabelColorOption(ClockColorOption.THEME_INVERTED)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setAppLabelColorOption(ClockColorOption.THEME_INVERTED)
    }

    @Test
    fun `changing home apps font weight calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setHomeAppsFontWeight(FontWeightOption.SEMI_BOLD)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setHomeAppsFontWeight(FontWeightOption.SEMI_BOLD)
    }

    @Test
    fun `home wallpaper reflects the repository value once loaded`() = runTest {
        val tones = HomeWallpaper.Tones(listOf(0x33FF0000))
        val viewModel = createViewModel(wallpaperRepository = fakeWallpaperRepository(tones))

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(tones, viewModel.homeWallpaper.value)
    }
}
