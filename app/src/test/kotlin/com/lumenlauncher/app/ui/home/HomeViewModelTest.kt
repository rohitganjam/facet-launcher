package com.lumenlauncher.app.ui.home

import com.lumenlauncher.app.data.NotificationShadeRepository
import com.lumenlauncher.app.data.model.LauncherSettings
import com.lumenlauncher.app.domain.HomeScreenState
import com.lumenlauncher.app.domain.ObserveHomeScreenStateUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

/**
 * The notification-shade platform call itself is covered by [com.lumenlauncher.app.data.NotificationShadeRepositoryTest];
 * this only checks that Home's swipe-down gesture (wired in
 * [com.lumenlauncher.app.ui.launcher.HomeDrawerRoute]) reaches the repository through the
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

    private fun homeViewModel(notificationShadeRepository: NotificationShadeRepository): HomeViewModel {
        val observeHomeScreenState = mock(ObserveHomeScreenStateUseCase::class.java)
        `when`(observeHomeScreenState.invoke()).thenReturn(
            flowOf(
                HomeScreenState(
                    settings = LauncherSettings(),
                    dockApps = emptyList(),
                    appListItems = emptyList(),
                    profiles = emptyList(),
                    usageAccessGranted = true,
                    calendarEvents = emptyList(),
                    badgeCounts = emptyMap(),
                ),
            ),
        )
        return HomeViewModel(observeHomeScreenState, notificationShadeRepository)
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
}
