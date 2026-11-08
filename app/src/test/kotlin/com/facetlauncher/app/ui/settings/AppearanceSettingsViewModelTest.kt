package com.facetlauncher.app.ui.settings

import com.facetlauncher.app.eqOf
import com.facetlauncher.app.LandscapeTransformCaptor
import com.facetlauncher.app.data.model.LandscapeOverrides
import com.facetlauncher.app.data.model.AppListColumnAlignment
import androidx.lifecycle.SavedStateHandle
import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.CalendarRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.NotificationAccessRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.encode
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.model.ClockStackItem
import com.facetlauncher.app.data.model.ClockCalendarConfig
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
import com.facetlauncher.app.data.model.IconShape
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
import com.facetlauncher.app.data.model.PlacementTarget
import com.facetlauncher.app.domain.ApplyFacetDefaultsUseCase
import com.facetlauncher.app.domain.FacetDefaultsScope
import com.facetlauncher.app.domain.ObserveActiveFacetItemsUseCase
import com.facetlauncher.app.domain.ObservePlacedItemsUseCase
import com.facetlauncher.app.data.model.PlacementList

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
        facets: List<Facet> = emptyList(),
        settings: LauncherSettings = LauncherSettings(),
        calendarGranted: Boolean = false,
        facetFavorites: List<PlacedItem> = emptyList(),
        activeFacetFavorites: List<PlacedItem> = emptyList(),
        facetDockItems: List<PlacedItem> = emptyList(),
        activeFacetDockItems: List<PlacedItem> = emptyList(),
        settingsRepository: SettingsRepository = mock(SettingsRepository::class.java),
        facetRepository: FacetRepository = mock(FacetRepository::class.java),
        observePlacedItems: ObservePlacedItemsUseCase = mock(ObservePlacedItemsUseCase::class.java),
        observeActiveFacetItems: ObserveActiveFacetItemsUseCase = mock(ObserveActiveFacetItemsUseCase::class.java),
        calendarRepository: CalendarRepository = mock(CalendarRepository::class.java),
        calendarPermissionRepository: CalendarPermissionRepository = mock(CalendarPermissionRepository::class.java),
        wallpaperRepository: WallpaperRepository = fakeWallpaperRepository(),
        notificationAccessRepository: NotificationAccessRepository = mock(NotificationAccessRepository::class.java),
        applyFacetDefaults: ApplyFacetDefaultsUseCase = mock(ApplyFacetDefaultsUseCase::class.java),
    ): AppearanceSettingsViewModel {
        // Favorites/dock/calendar-grant are plain value params, stubbed only here, rather than
        // letting a test build and pre-stub its own mock before passing it in — Mockito's
        // last-stub-wins silently overwrote a pre-call stub with this helper's own internal
        // default the first time this was tried (see chat history); mirrors
        // `DockSettingsViewModelTest.createViewModel`'s own `dockItems: List<PlacedItem>` param.
        `when`(settingsRepository.settings).thenReturn(flowOf(settings))
        `when`(facetRepository.observeFacets()).thenReturn(MutableStateFlow(facets))
        facetId?.let {
            `when`(observePlacedItems.items(PlacementTarget(PlacementList.FAVORITES, it))).thenReturn(flowOf(facetFavorites))
            `when`(observePlacedItems.items(PlacementTarget(PlacementList.DOCK, it))).thenReturn(flowOf(facetDockItems))
        }
        `when`(observeActiveFacetItems(PlacementList.FAVORITES)).thenReturn(flowOf(activeFacetFavorites))
        `when`(observeActiveFacetItems(PlacementList.DOCK)).thenReturn(flowOf(activeFacetDockItems))
        `when`(calendarPermissionRepository.isGranted()).thenReturn(calendarGranted)
        return AppearanceSettingsViewModel(
            SavedStateHandle(facetId?.let { mapOf("facetId" to it) } ?: emptyMap()),
            settingsRepository,
            facetRepository,
            observePlacedItems,
            observeActiveFacetItems,
            calendarRepository,
            calendarPermissionRepository,
            wallpaperRepository,
            notificationAccessRepository,
            applyFacetDefaults,
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
    fun `changing icon shape calls the repository setter`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setIconShape(IconShape.CIRCLE)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setIconShape(IconShape.CIRCLE)
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
    fun `look fields are the facet's own values`() = runTest {
        // A facet with explicit look values already chosen from this screen resolves to them
        val facet = Facet(
            id = 9L, name = "Work", position = 0,
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
    fun `a facet keeps its own look when the launcher's defaults differ`() = runTest {
        val facet = Facet(id = 4L, name = "Work", position = 0, dockDisplayMode = DockDisplayMode.ICONS)
        val viewModel = createViewModel(facetId = 4L, facets = listOf(facet), settings = LauncherSettings(dockDisplayMode = DockDisplayMode.TEXT))
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(DockDisplayMode.ICONS, viewModel.uiState.value.dockDisplayMode)
    }

    @Test
    fun `launcher scope consumes the active facet's favorites and dock, not mock data`() = runTest {
        val viewModel = createViewModel(activeFacetFavorites = listOf(app(1), app(2)), activeFacetDockItems = listOf(app(3)))
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isFacetScoped)
        assertEquals(listOf(app(1), app(2)), viewModel.uiState.value.favorites)
        assertEquals(listOf(app(3)), viewModel.uiState.value.dockItems)
    }

    @Test
    fun `facet scope consumes that facet's own favorites and dock`() = runTest {
        val facet = Facet(id = 6L, name = "Work", position = 0)
        val viewModel = createViewModel(
            facetId = 6L,
            facets = listOf(facet),
            facetFavorites = listOf(app(9)),
            activeFacetFavorites = listOf(app(1)),
            facetDockItems = listOf(app(8)),
            activeFacetDockItems = listOf(app(2)),
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(app(9)), viewModel.uiState.value.favorites)
        assertEquals(listOf(app(8)), viewModel.uiState.value.dockItems)
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
        `when`(calendarRepository.getUpcomingEvents(null, true)).thenReturn(listOf(event))
        val viewModel = createViewModel(calendarRepository = calendarRepository, calendarGranted = true)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(event), viewModel.uiState.value.calendarEvents)
    }

    // --- notification access (drives the Notifications row's subtext) ---

    @Test
    fun `notification access is false in the state until it is granted`() = runTest {
        // Given notification access not granted
        val notificationAccessRepository = mock(NotificationAccessRepository::class.java)
        `when`(notificationAccessRepository.isGranted()).thenReturn(false)
        val viewModel = createViewModel(notificationAccessRepository = notificationAccessRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the state says so
        assertEquals(false, viewModel.uiState.value.notificationAccessGranted)
    }

    @Test
    fun `refreshing notification access picks up a grant made in system settings`() = runTest {
        // Given access not granted at first
        val notificationAccessRepository = mock(NotificationAccessRepository::class.java)
        `when`(notificationAccessRepository.isGranted()).thenReturn(false)
        val viewModel = createViewModel(notificationAccessRepository = notificationAccessRepository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        // When the user grants it and the screen resumes
        `when`(notificationAccessRepository.isGranted()).thenReturn(true)
        viewModel.refreshNotificationAccess()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the state flips to granted
        assertEquals(true, viewModel.uiState.value.notificationAccessGranted)
    }

    @Test
    fun `calendar access is false in the state until it is granted`() = runTest {
        // Given calendar permission not granted
        val viewModel = createViewModel(calendarGranted = false)
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the state says so
        assertEquals(false, viewModel.uiState.value.calendarAccessGranted)
    }

    @Test
    fun `refreshing calendar access picks up a grant made from the calendar page`() = runTest {
        // Given calendar permission not granted at first (and a calendar repository that can answer once it is)
        val calendarPermissionRepository = mock(CalendarPermissionRepository::class.java)
        val calendarRepository = mock(CalendarRepository::class.java)
        `when`(calendarRepository.getUpcomingEvents(null, true)).thenReturn(emptyList())
        val viewModel = createViewModel(
            calendarGranted = false,
            calendarPermissionRepository = calendarPermissionRepository,
            calendarRepository = calendarRepository,
        )
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        // When the user grants it and the screen resumes
        `when`(calendarPermissionRepository.isGranted()).thenReturn(true)
        viewModel.refreshCalendarAccess()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the state flips to granted
        assertEquals(true, viewModel.uiState.value.calendarAccessGranted)
    }

    // --- helpers ---

    private fun facetWithClock(config: ClockWidgetConfig) = Facet(
        id = 1L, name = "Work", position = 0,
        clockStack = listOf(ClockStackItem(id = 3, facetId = 1, position = 0, kind = ClockStackItemKind.CLOCK, nativeWidgetConfig = NativeWidgetConfig(clock = config).encode())),
    )

    @Test
    fun `calendar summary reads the facet's clock's own calendars`() {
        // Given a facet whose clock has its own calendar choices, and different launcher-wide ones
        val state = AppearanceSettingsUiState(
            settings = LauncherSettings(showAllDayEvents = true, selectedCalendarIds = setOf("cal-2", "cal-3")),
            facet = facetWithClock(ClockWidgetConfig(calendar = ClockCalendarConfig(showAllDay = false, calendarIds = setOf("cal-1")))),
        )

        // Then the summary follows the clock's values
        assertEquals(false, state.effectiveShowAllDayEvents)
        assertEquals(setOf("cal-1"), state.selectedCalendarIds)
    }

    @Test
    fun `a clock on every calendar has no explicit selection`() {
        // Given a clock left on all calendars
        val state = AppearanceSettingsUiState(facet = facetWithClock(ClockWidgetConfig()))

        // Then the selection is null, which the row words as all calendars
        assertEquals(null, state.selectedCalendarIds)
    }

    @Test
    fun `setHideStatusBar writes to SettingsRepository`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = createViewModel(settingsRepository = settingsRepository)

        viewModel.setHideStatusBar(true)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setHideStatusBar(true)
    }

    private fun app(n: Int) = PlacedItem.SingleApp(AppInfo(packageName = "com.example.$n", activityName = ".Main", label = "App $n", icon = null))
}
