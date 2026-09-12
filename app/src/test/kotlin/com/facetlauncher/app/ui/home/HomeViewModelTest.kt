package com.facetlauncher.app.ui.home

import com.facetlauncher.app.data.NotificationShadeRepository
import com.facetlauncher.app.data.ProfileRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.local.ProfileEntity
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.domain.ClockAccessoryState
import com.facetlauncher.app.domain.HomeScreenState
import com.facetlauncher.app.domain.ObserveHomeScreenStateUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

/**
 * The notification-shade platform call itself is covered by [com.facetlauncher.app.data.NotificationShadeRepositoryTest];
 * this only checks that Home's swipe-down gesture (wired in
 * [com.facetlauncher.app.ui.launcher.HomeDrawerRoute]) reaches the repository through the
 * ViewModel rather than skipping it.
 */
@OptIn(ExperimentalCoroutinesApi::class)
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
        profileRepository: ProfileRepository = mock(ProfileRepository::class.java),
        settings: LauncherSettings = LauncherSettings(),
        profiles: List<ProfileEntity> = emptyList(),
    ): HomeViewModel {
        val observeHomeScreenState = mock(ObserveHomeScreenStateUseCase::class.java)
        `when`(observeHomeScreenState.invoke()).thenReturn(
            flowOf(
                HomeScreenState(
                    settings = settings,
                    dockApps = emptyList(),
                    appListItems = emptyList(),
                    profiles = profiles,
                    usageAccessGranted = true,
                    calendarEvents = emptyList(),
                    badgeCounts = emptyMap(),
                    clockAccessories = ClockAccessoryState(nextAlarmMillis = null, batteryPercent = 0, isCharging = false),
                ),
            ),
        )
        return HomeViewModel(observeHomeScreenState, notificationShadeRepository, settingsRepository, profileRepository)
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
        val viewModel = HomeViewModel(
            observeHomeScreenState,
            mock(NotificationShadeRepository::class.java),
            mock(SettingsRepository::class.java),
            mock(ProfileRepository::class.java),
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
    fun `onClockZoneHeightCommit delegates to the settings repository when no profile is overriding`() = runTest {
        // Given a HomeViewModel with no active profile overriding the clock bundle
        val settingsRepository = mock(SettingsRepository::class.java)
        val viewModel = homeViewModel(mock(NotificationShadeRepository::class.java), settingsRepository)

        // When the clock's grab handle commits a new height
        viewModel.onClockZoneHeightCommit(123.4f)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then it delegates to the global settings repository rather than persisting anything itself
        verify(settingsRepository).setClockZoneHeight(123.4f)
    }

    @Test
    fun `onClockZoneHeightCommit delegates to the profile repository when the active profile overrides the clock bundle`() = runTest {
        // Given a HomeViewModel whose active profile has overrideClock = true
        val settingsRepository = mock(SettingsRepository::class.java)
        val profileRepository = mock(ProfileRepository::class.java)
        val overridingProfile = ProfileEntity(id = 1L, name = "Work", position = 0, overrideClock = true)
        val viewModel = homeViewModel(
            notificationShadeRepository = mock(NotificationShadeRepository::class.java),
            settingsRepository = settingsRepository,
            profileRepository = profileRepository,
            settings = LauncherSettings(activeProfileId = 1L),
            profiles = listOf(overridingProfile),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // When the clock's grab handle commits a new height
        viewModel.onClockZoneHeightCommit(123.4f)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then it writes to this profile's own override instead of the global setting
        verify(profileRepository).setClockZoneHeight(overridingProfile, 123.4f)
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
}
