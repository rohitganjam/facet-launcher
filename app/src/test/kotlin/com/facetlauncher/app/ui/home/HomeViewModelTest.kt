package com.facetlauncher.app.ui.home

import com.facetlauncher.app.data.DefaultLauncherRepository
import com.facetlauncher.app.data.NotificationShadeRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.FacetEntity
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
        facets: List<FacetEntity> = emptyList(),
        dockApps: List<PlacedItem> = emptyList(),
        favoriteItems: List<PlacedItem> = emptyList(),
        clockWidgetFacet: ClockWidgetFacetController = mock(ClockWidgetFacetController::class.java),
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
            mock(ClockWidgetHostController::class.java),
            clockWidgetFacet,
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
            mock(ClockWidgetHostController::class.java),
            mock(ClockWidgetFacetController::class.java),
        )

        // Then it's still loading well before the timeout
        testDispatcher.scheduler.advanceTimeBy(2_000)
        assertTrue(viewModel.uiState.value.isLoading)

        // When the timeout elapses with no real data having arrived
        testDispatcher.scheduler.advanceTimeBy(1_500)

        // Then it stops loading anyway
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `onClockZoneHeightCommit delegates to the settings repository when no facet is overriding`() = runTest {
        // Given a HomeViewModel with no active facet overriding the clock bundle
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = homeViewModel(mock(NotificationShadeRepository::class.java), settingsRepository)

        // When the clock's grab handle commits a new height
        viewModel.onClockZoneHeightCommit(123.4f)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then it delegates to the global settings repository rather than persisting anything itself
        verify(settingsRepository).setClockZoneHeight(123.4f)
    }

    @Test
    fun `onClockZoneHeightCommit delegates to the facet repository when the active facet overrides the clock bundle`() = runTest {
        // Given a HomeViewModel whose active facet has overrideClock = true
        val settingsRepository = mock(SettingsRepository::class.java)
        val facetRepository = mock(FacetRepository::class.java)
        val overridingFacet = FacetEntity(id = 1L, name = "Work", position = 0, overrideClock = true)
        val viewModel = homeViewModel(
            notificationShadeRepository = mock(NotificationShadeRepository::class.java),
            settingsRepository = settingsRepository,
            facetRepository = facetRepository,
            settings = LauncherSettings(activeFacetId = 1L),
            facets = listOf(overridingFacet),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // When the clock's grab handle commits a new height
        viewModel.onClockZoneHeightCommit(123.4f)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then it writes to this facet's own override instead of the global setting
        verify(facetRepository).setClockZoneHeight(overridingFacet, 123.4f)
    }

    @Test
    fun `onClockAlignmentCommit delegates to the settings repository when no facet is overriding`() = runTest {
        // Given a HomeViewModel with no active facet overriding the clock bundle
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = homeViewModel(mock(NotificationShadeRepository::class.java), settingsRepository)

        // When the alignment toolbar picks center
        viewModel.onClockAlignmentCommit(ClockAlignment.CENTER)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then it writes the global default
        verify(settingsRepository).setClockAlignment(ClockAlignment.CENTER)
    }

    @Test
    fun `onClockAlignmentCommit delegates to the facet repository when the active facet overrides the clock bundle`() = runTest {
        // Given a HomeViewModel whose active facet has overrideClock = true
        val settingsRepository = mock(SettingsRepository::class.java)
        val facetRepository = mock(FacetRepository::class.java)
        val overridingFacet = FacetEntity(id = 1L, name = "Work", position = 0, overrideClock = true)
        val viewModel = homeViewModel(
            notificationShadeRepository = mock(NotificationShadeRepository::class.java),
            settingsRepository = settingsRepository,
            facetRepository = facetRepository,
            settings = LauncherSettings(activeFacetId = 1L),
            facets = listOf(overridingFacet),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // When the alignment toolbar picks right
        viewModel.onClockAlignmentCommit(ClockAlignment.RIGHT)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then it writes this facet's own override, same as height and scale
        verify(facetRepository).setClockAlignment(overridingFacet, ClockAlignment.RIGHT)
        verify(settingsRepository, org.mockito.Mockito.never()).setClockAlignment(ClockAlignment.RIGHT)
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
