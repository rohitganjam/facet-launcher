package com.facetlauncher.app.ui.home

import com.facetlauncher.app.data.widgetItem
import com.facetlauncher.app.domain.ClockPlacement
import com.facetlauncher.app.PlacementChangeCaptor
import com.facetlauncher.app.eqOf
import com.facetlauncher.app.LandscapeTransformCaptor
import com.facetlauncher.app.data.model.LandscapeOverrides
import com.facetlauncher.app.data.EntitlementRepository
import com.facetlauncher.app.data.DefaultLauncherRepository
import com.facetlauncher.app.data.NotificationShadeRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.MediaSessionRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.domain.ClockAccessoryState
import com.facetlauncher.app.domain.HomeScreenState
import com.facetlauncher.app.domain.ObserveHomeScreenStateUseCase
import com.facetlauncher.app.domain.ObserveQuickAddStateUseCase
import com.facetlauncher.app.domain.QuickPlacementAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * The notification-shade platform call itself is covered by [com.facetlauncher.app.data.NotificationShadeRepositoryTest];
 * this only checks that Home's swipe-down gesture (wired in
 * [com.facetlauncher.app.ui.launcher.HomeDrawerRoute]) reaches the repository through the
 * ViewModel rather than skipping it.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun homeViewModel(
        notificationShadeRepository: NotificationShadeRepository,
        settingsRepository: SettingsRepository = mock(SettingsRepository::class.java),
        facetRepository: FacetRepository = mock(FacetRepository::class.java),
        defaultLauncherRepository: DefaultLauncherRepository = mock(DefaultLauncherRepository::class.java).also {
            runBlocking { `when`(it.isDefaultLauncher()).thenReturn(false) }
        },
        settings: LauncherSettings = LauncherSettings(),
        facets: List<Facet> = emptyList(),
        dockApps: List<PlacedItem> = emptyList(),
        favoriteItems: List<PlacedItem> = emptyList(),
        clockWidgetFacet: ClockWidgetFacetController = mock(ClockWidgetFacetController::class.java),
        mediaSessionRepository: MediaSessionRepository = mock(MediaSessionRepository::class.java),
    ): HomeViewModel {
        val observeHomeScreenState = mock(ObserveHomeScreenStateUseCase::class.java)
        `when`(observeHomeScreenState.invoke()).thenReturn(
            flowOf(
                HomeScreenState(
                    settings = settings,
                    dockApps = dockApps,
                    appListItems = emptyList(),
                    favoriteItems = favoriteItems,
                    facets = facets,
                    usageAccessGranted = true,
                    calendarEvents = emptyList(),
                    badgeCounts = emptyMap(),
                    clockAccessories = ClockAccessoryState(nextAlarmMillis = null, batteryPercent = 0, isCharging = false),
                    mediaPlayer = null,
                ),
            ),
        )
        return HomeViewModel(
            observeHomeScreenState,
            notificationShadeRepository,
            settingsRepository,
            facetRepository,
            defaultLauncherRepository,
            ObserveQuickAddStateUseCase(),
            mediaSessionRepository,
            mock(ClockWidgetHostController::class.java),
            clockWidgetFacet,
            EntitlementRepository(),
        )
    }

    @Test
    fun `expandNotificationShade delegates to the repository`() = runTest {
        // Given a HomeViewModel wired to a mocked shade repository
        val notificationShadeRepository = mock(NotificationShadeRepository::class.java)
        val viewModel = homeViewModel(notificationShadeRepository)

        // When Home's swipe-down gesture fires
        viewModel.expandNotificationShade()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then it delegates to the repository rather than reaching the platform directly
        verify(notificationShadeRepository).expand()
    }

    @Test
    fun `media transport actions delegate to the media session repository`() = runTest {
        // Given a HomeViewModel wired to a mocked media session repository
        val mediaSessionRepository = mock(MediaSessionRepository::class.java)
        val viewModel = homeViewModel(mock(NotificationShadeRepository::class.java), mediaSessionRepository = mediaSessionRepository)

        // When each transport action fires
        viewModel.onMediaPrevious()
        viewModel.onMediaPlayPause()
        viewModel.onMediaNext()
        viewModel.onMediaOpen()

        // Then each goes straight to the repository
        verify(mediaSessionRepository).skipToPrevious()
        verify(mediaSessionRepository).togglePlayPause()
        verify(mediaSessionRepository).skipToNext()
        verify(mediaSessionRepository).openPlayer()
    }

    @Test
    fun `the clock page a facet last showed is remembered, apart for each facet`() = runTest {
        // Given an active facet 1
        val viewModel = homeViewModel(mock(NotificationShadeRepository::class.java), settings = LauncherSettings(activeFacetId = 1L))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(null, viewModel.uiState.value.visibleClockItemId)

        // When it shows its second page
        viewModel.onClockPageChange(itemId = 42L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the state remembers that page for this facet
        assertEquals(42L, viewModel.uiState.value.visibleClockItemId)
    }

    @Test
    fun `is loading until the use case emits, then reflects the real state`() = runTest {
        // Given a HomeViewModel wired to the fake state from homeViewModel(), which emits immediately
        val viewModel = homeViewModel(mock(NotificationShadeRepository::class.java))

        // Then immediately after construction it's still loading (the combine coroutine hasn't run yet)
        assertTrue(viewModel.uiState.value.isLoading)

        // When the pending coroutine work is allowed to complete
        testDispatcher.scheduler.advanceUntilIdle()

        // Then it's no longer loading
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `stops loading on its own after the timeout even if the underlying flow never emits`() = runTest {
        // Given a use case whose flow never emits (simulating a stuck/broken data source) — this
        // is the actual home screen, so it must never stay blank forever (see chat history).
        val observeHomeScreenState = mock(ObserveHomeScreenStateUseCase::class.java)
        `when`(observeHomeScreenState.invoke()).thenReturn(MutableSharedFlow())
        val defaultLauncherRepository = mock(DefaultLauncherRepository::class.java)
        `when`(defaultLauncherRepository.isDefaultLauncher()).thenReturn(false)
        val viewModel = HomeViewModel(
            observeHomeScreenState,
            mock(NotificationShadeRepository::class.java),
            mock(SettingsRepository::class.java),
            mock(FacetRepository::class.java),
            defaultLauncherRepository,
            ObserveQuickAddStateUseCase(),
            mock(MediaSessionRepository::class.java),
            mock(ClockWidgetHostController::class.java),
            mock(ClockWidgetFacetController::class.java),
            EntitlementRepository(),
        )

        // Then it's still loading well before the timeout
        testDispatcher.scheduler.advanceTimeBy(2_000)
        assertTrue(viewModel.uiState.value.isLoading)

        // When the timeout elapses with no real data having arrived
        testDispatcher.scheduler.advanceTimeBy(1_500)

        // Then it stops loading anyway
        assertFalse(viewModel.uiState.value.isLoading)
    }

    private val shown = ClockPlacement(alignment = ClockAlignment.LEFT, zoneHeightDp = null, scale = 0.8f)

    private fun placementViewModel(
        facets: List<Facet> = listOf(Facet(id = 1L, name = "Work", position = 0)),
        controller: ClockWidgetFacetController,
    ) = homeViewModel(
        notificationShadeRepository = mock(NotificationShadeRepository::class.java),
        settings = LauncherSettings(activeFacetId = 1L),
        facets = facets,
        clockWidgetFacet = controller,
    ).also { testDispatcher.scheduler.advanceUntilIdle() }

    @Test
    fun `the divider commit goes to the active facet's own placement`() = runTest {
        // Given a facet that does not override the clock
        val controller = mock(ClockWidgetFacetController::class.java)
        val viewModel = placementViewModel(controller = controller)

        // When the clock's grab handle commits a new height
        viewModel.onClockZoneHeightCommit(123.4f)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then it is saved as the facet's own divider, in portrait, and nothing else changes
        val change = PlacementChangeCaptor()
        verify(controller).commitPlacement(eqOf(1L), eqOf(HomeLayoutMode.PORTRAIT), change.capture())
        assertEquals(shown.copy(zoneHeightDp = 123.4f), change.applied(shown))
    }

    @Test
    fun `scale and alignment commits change only their own value`() = runTest {
        val controller = mock(ClockWidgetFacetController::class.java)
        val viewModel = placementViewModel(controller = controller)

        viewModel.onClockScaleCommit(1.4f)
        viewModel.onClockAlignmentCommit(ClockAlignment.CENTER)
        testDispatcher.scheduler.advanceUntilIdle()

        val change = PlacementChangeCaptor()
        verify(controller, org.mockito.Mockito.times(2)).commitPlacement(eqOf(1L), eqOf(HomeLayoutMode.PORTRAIT), change.capture())
        assertEquals(listOf(shown.copy(scale = 1.4f), shown.copy(alignment = ClockAlignment.CENTER)), change.appliedEach(shown))
    }

    @Test
    fun `the placement commits go to the facet even when it overrides the clock, and never to the launcher settings`() = runTest {
        // Given an active facet that overrides the clock style
        val controller = mock(ClockWidgetFacetController::class.java)
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = homeViewModel(
            notificationShadeRepository = mock(NotificationShadeRepository::class.java),
            settingsRepository = settingsRepository,
            settings = LauncherSettings(activeFacetId = 1L),
            facets = listOf(Facet(id = 1L, name = "Work", position = 0, overrideClock = true)),
            clockWidgetFacet = controller,
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // When the alignment is committed
        viewModel.onClockAlignmentCommit(ClockAlignment.RIGHT)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then it goes through the facet's placement path, with no write to the launcher-wide settings
        verify(controller).commitPlacement(eqOf(1L), eqOf(HomeLayoutMode.PORTRAIT), PlacementChangeCaptor().capture())
        org.mockito.Mockito.verifyNoInteractions(settingsRepository)
    }

    @Test
    fun `landscape commits pass the landscape mode`() = runTest {
        val controller = mock(ClockWidgetFacetController::class.java)
        val viewModel = placementViewModel(controller = controller)

        viewModel.onClockZoneHeightCommit(310f, HomeLayoutMode.LANDSCAPE)
        testDispatcher.scheduler.advanceUntilIdle()

        val change = PlacementChangeCaptor()
        verify(controller).commitPlacement(eqOf(1L), eqOf(HomeLayoutMode.LANDSCAPE), change.capture())
        assertEquals(shown.copy(zoneHeightDp = 310f), change.applied(shown))
    }

    @Test
    fun `a widget page keeps its own alignment, saved against the page`() = runTest {
        // Given the visible clock page is a widget
        val controller = mock(ClockWidgetFacetController::class.java)
        val facet = Facet(id = 1L, name = "Work", position = 0, clockStack = listOf(widgetItem(41, id = 7L, facetId = 1L)))
        val viewModel = placementViewModel(facets = listOf(facet), controller = controller)

        // When the alignment is committed, in portrait and in landscape
        viewModel.onClockAlignmentCommit(ClockAlignment.CENTER)
        viewModel.onClockAlignmentCommit(ClockAlignment.RIGHT, HomeLayoutMode.LANDSCAPE)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then each is saved on the widget page, not on the facet's placement
        verify(controller).commitPageAlignment(7L, ClockAlignment.CENTER, HomeLayoutMode.PORTRAIT)
        verify(controller).commitPageAlignment(7L, ClockAlignment.RIGHT, HomeLayoutMode.LANDSCAPE)
        verify(controller, org.mockito.Mockito.never()).commitPlacement(eqOf(1L), eqOf(HomeLayoutMode.PORTRAIT), PlacementChangeCaptor().capture())
    }

    @Test
    fun `an already-default launcher retires the set-default prompt on its own`() = runTest {
        // Given Facet already holds the home role
        val settingsRepository = mock(SettingsRepository::class.java)
        val defaultLauncherRepository = mock(DefaultLauncherRepository::class.java)
        `when`(defaultLauncherRepository.isDefaultLauncher()).thenReturn(true)
        val viewModel = homeViewModel(mock(NotificationShadeRepository::class.java), settingsRepository, defaultLauncherRepository = defaultLauncherRepository)

        // When the check resolves
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the prompt is marked seen (so it never appears later) and the state reflects the answer
        verify(settingsRepository).markCoachMarkSeen("HOME_SET_DEFAULT_PROMPT")
        assertEquals(true, viewModel.uiState.value.isDefaultLauncher)
    }

    @Test
    fun `a launcher that is not the default leaves the set-default prompt pending`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = homeViewModel(mock(NotificationShadeRepository::class.java), settingsRepository)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository, org.mockito.Mockito.never()).markCoachMarkSeen("HOME_SET_DEFAULT_PROMPT")
        assertEquals(false, viewModel.uiState.value.isDefaultLauncher)
    }

    @Test
    fun `dismissGestureHint marks the HOME_GESTURES coach mark seen`() = runTest {
        // Given a HomeViewModel
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = homeViewModel(mock(NotificationShadeRepository::class.java), settingsRepository = settingsRepository)

        // When the gesture hint is dismissed (first gesture or "Got it")
        viewModel.dismissGestureHint()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then it persists via the same coach-mark mechanism the hint's visibility reads from
        verify(settingsRepository).markCoachMarkSeen("HOME_GESTURES")
    }

    @Test
    fun `quickAddStateForApp resolves synchronously against the already-live uiState`() = runTest {
        // Given a HomeViewModel whose dock already contains this app
        val app = AppInfo(packageName = "com.example.a", activityName = ".Main", label = "A", icon = null)
        val viewModel = homeViewModel(mock(NotificationShadeRepository::class.java), dockApps = listOf(PlacedItem.SingleApp(app)))
        testDispatcher.scheduler.advanceUntilIdle()

        // When its long-press menu asks for quick-add state
        val result = viewModel.quickAddStateForApp(app)

        // Then it resolves Remove from Dock without any further coroutine work — no repository
        // read, since the answer was already sitting in uiState (see chat history: this used to
        // be a fresh suspend fetch on every open, causing the sheet to visibly reflow).
        assertEquals(QuickPlacementAction.Remove(facetName = null), result.dockAction)
    }
}
