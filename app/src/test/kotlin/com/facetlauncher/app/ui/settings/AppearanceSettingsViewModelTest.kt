package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.FontScaleOption
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.IconRenderMode
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.ThemeMode
import com.facetlauncher.app.data.model.WallpaperAccentRole
import com.facetlauncher.app.ui.theme.AccentSwatch
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
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
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
        facetId: Long? = null,
        facets: List<FacetEntity> = emptyList(),
        settings: LauncherSettings = LauncherSettings(),
        calendarGranted: Boolean = false,
        facetFavorites: List<PlacedItem> = emptyList(),
        globalFavorites: List<PlacedItem> = emptyList(),
        facetDockItems: List<PlacedItem> = emptyList(),
        globalDockItems: List<PlacedItem> = emptyList(),
        settingsRepository: SettingsRepository = mock(SettingsRepository::class.java),
        facetRepository: FacetRepository = mock(FacetRepository::class.java),
        favoriteAppRepository: FavoriteAppRepository = mock(FavoriteAppRepository::class.java),
        defaultFavoriteAppRepository: DefaultFavoriteAppRepository = mock(DefaultFavoriteAppRepository::class.java),
        dockAppRepository: DockAppRepository = mock(DockAppRepository::class.java),
        facetDockAppRepository: FacetDockAppRepository = mock(FacetDockAppRepository::class.java),
        calendarRepository: CalendarRepository = mock(CalendarRepository::class.java),
        calendarPermissionRepository: CalendarPermissionRepository = mock(CalendarPermissionRepository::class.java),
        wallpaperRepository: WallpaperRepository = fakeWallpaperRepository(),
    ): AppearanceSettingsViewModel {
        // Favorites/dock/calendar-grant are plain value params, stubbed only here, rather than
        // letting a test build and pre-stub its own mock before passing it in — Mockito's
        // last-stub-wins silently overwrote a pre-call stub with this helper's own internal
        // default the first time this was tried (see chat history); mirrors
        // `DockSettingsViewModelTest.createViewModel`'s own `dockItems: List<PlacedItem>` param.
        `when`(settingsRepository.settings).thenReturn(flowOf(settings))
        `when`(facetRepository.observeFacets()).thenReturn(MutableStateFlow(facets))
        `when`(favoriteAppRepository.observeFavoriteItems(anyLong())).thenReturn(flowOf(facetFavorites))
        `when`(defaultFavoriteAppRepository.observeDefaultItems()).thenReturn(flowOf(globalFavorites))
        `when`(dockAppRepository.observeDockItems()).thenReturn(flowOf(globalDockItems))
        `when`(facetDockAppRepository.observeDockItems(anyLong())).thenReturn(flowOf(facetDockItems))
        `when`(calendarPermissionRepository.isGranted()).thenReturn(calendarGranted)
        return AppearanceSettingsViewModel(
            SavedStateHandle(facetId?.let { mapOf("facetId" to it) } ?: emptyMap()),
            settingsRepository,
            facetRepository,
            favoriteAppRepository,
            defaultFavoriteAppRepository,
            dockAppRepository,
            facetDockAppRepository,
            calendarRepository,
            calendarPermissionRepository,
            wallpaperRepository,
        )
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
    fun `changing font scale option calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setFontScaleOption(FontScaleOption.LARGE)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setFontScaleOption(FontScaleOption.LARGE)
    }

    @Test
    fun `home wallpaper reflects the repository value once loaded`() = runTest {
        val tones = HomeWallpaper.Tones(listOf(0x33FF0000))
        val viewModel = createViewModel(wallpaperRepository = fakeWallpaperRepository(tones))

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(tones, viewModel.homeWallpaper.value)
    }

    @Test
    fun `global-scoped look setters write to SettingsRepository`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setDockDisplayMode(DockDisplayMode.TEXT)
        viewModel.setAppRowPosition(AppRowPosition.RIGHT)
        viewModel.setAppRowPresentation(AppRowPresentation.TEXT_ONLY)
        viewModel.setAppListVerticalAlignment(AppListVerticalAlignment.TOP)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setDockDisplayMode(DockDisplayMode.TEXT)
        verify(settingsRepository).setAppRowPosition(AppRowPosition.RIGHT)
        verify(settingsRepository).setAppRowPresentation(AppRowPresentation.TEXT_ONLY)
        verify(settingsRepository).setAppListVerticalAlignment(AppListVerticalAlignment.TOP)
    }

    @Test
    fun `facet-scoped look setters write to that facet's row, not SettingsRepository`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val facetRepository = mock(FacetRepository::class.java)
        val facet = FacetEntity(id = 7L, name = "Work", position = 0)
        `when`(facetRepository.getById(7L)).thenReturn(facet)
        val viewModel = createViewModel(
            facetId = 7L,
            facets = listOf(facet),
            settingsRepository = settingsRepository,
            facetRepository = facetRepository,
        )

        viewModel.setDockDisplayMode(DockDisplayMode.TEXT)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(facetRepository).setDockDisplayMode(facet, DockDisplayMode.TEXT)
        verify(settingsRepository, org.mockito.Mockito.never()).setDockDisplayMode(DockDisplayMode.TEXT)
    }

    @Test
    fun `look fields resolve independently of overrideDock and overrideApps via their own LAUNCHER_DEFAULT sentinel`() = runTest {
        // A facet not overriding dock/apps content at all, but with explicit look values already
        // chosen from this screen — it still resolves to them (see chat history: these fields no
        // longer gate on overrideDock/overrideApps, unlike Dock's/Home-Apps-List's own content).
        val facet = FacetEntity(
            id = 9L, name = "Work", position = 0, overrideDock = false, overrideApps = false,
            dockDisplayMode = DockDisplayMode.TEXT, appRowPosition = AppRowPosition.RIGHT,
            appRowPresentation = AppRowPresentation.TEXT_ONLY, appListVerticalAlignment = AppListVerticalAlignment.TOP,
        )
        val viewModel = createViewModel(facetId = 9L, facets = listOf(facet))
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.isFacetScoped)
        assertEquals(DockDisplayMode.TEXT, viewModel.uiState.value.dockDisplayMode)
        assertEquals(AppRowPosition.RIGHT, viewModel.uiState.value.appRowPosition)
        assertEquals(AppRowPresentation.TEXT_ONLY, viewModel.uiState.value.appRowPresentation)
        assertEquals(AppListVerticalAlignment.TOP, viewModel.uiState.value.appListVerticalAlignment)
    }

    @Test
    fun `a facet with LAUNCHER_DEFAULT look fields inherits the global values`() = runTest {
        val facet = FacetEntity(id = 4L, name = "Work", position = 0)
        val viewModel = createViewModel(facetId = 4L, facets = listOf(facet), settings = LauncherSettings(dockDisplayMode = DockDisplayMode.TEXT))
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(DockDisplayMode.TEXT, viewModel.uiState.value.dockDisplayMode)
    }

    private fun app(n: Int) = PlacedItem.SingleApp(AppInfo(packageName = "com.example.$n", activityName = ".Main", label = "App $n", icon = null))

    @Test
    fun `global scope consumes the launcher-wide default favorites and dock, not mock data`() = runTest {
        val viewModel = createViewModel(globalFavorites = listOf(app(1), app(2)), globalDockItems = listOf(app(3)))
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isFacetScoped)
        assertEquals(listOf(app(1), app(2)), viewModel.uiState.value.effectiveFavorites)
        assertEquals(listOf(app(3)), viewModel.uiState.value.effectiveDockItems)
    }

    @Test
    fun `facet scope not overriding consumes the resolved default, not its own stale rows`() = runTest {
        val facet = FacetEntity(id = 6L, name = "Work", position = 0, overrideApps = false, overrideDock = false)
        val viewModel = createViewModel(
            facetId = 6L,
            facets = listOf(facet),
            facetFavorites = listOf(app(9)),
            globalFavorites = listOf(app(1)),
            facetDockItems = listOf(app(8)),
            globalDockItems = listOf(app(2)),
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(app(1)), viewModel.uiState.value.effectiveFavorites)
        assertEquals(listOf(app(2)), viewModel.uiState.value.effectiveDockItems)
    }

    @Test
    fun `facet scope overriding consumes that facet's own favorites and dock`() = runTest {
        val facet = FacetEntity(id = 6L, name = "Work", position = 0, overrideApps = true, overrideDock = true)
        val viewModel = createViewModel(
            facetId = 6L,
            facets = listOf(facet),
            facetFavorites = listOf(app(9)),
            globalFavorites = listOf(app(1)),
            facetDockItems = listOf(app(8)),
            globalDockItems = listOf(app(2)),
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(app(9)), viewModel.uiState.value.effectiveFavorites)
        assertEquals(listOf(app(8)), viewModel.uiState.value.effectiveDockItems)
    }

    @Test
    fun `calendar events stay empty without calendar permission, never a sample fallback`() = runTest {
        val calendarRepository = mock(CalendarRepository::class.java)
        val viewModel = createViewModel(calendarRepository = calendarRepository, calendarGranted = false)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(emptyList<CalendarEvent>(), viewModel.uiState.value.calendarEvents)
    }

    @Test
    fun `calendar events come from the real repository when permission is granted`() = runTest {
        val event = CalendarEvent(id = 1, calendarId = "1", title = "Standup", startTimeMillis = 0, endTimeMillis = 1, isAllDay = false)
        val calendarRepository = mock(CalendarRepository::class.java)
        `when`(calendarRepository.getTodayEvents(null, true)).thenReturn(listOf(event))
        val viewModel = createViewModel(calendarRepository = calendarRepository, calendarGranted = true)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(event), viewModel.uiState.value.calendarEvents)
    }
}
