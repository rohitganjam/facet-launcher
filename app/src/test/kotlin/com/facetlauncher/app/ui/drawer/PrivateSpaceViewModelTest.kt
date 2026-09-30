package com.facetlauncher.app.ui.drawer

import com.facetlauncher.app.data.PrivateSpaceRepository
import com.facetlauncher.app.data.PrivateSpaceState
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.RecentlyInstalledPosition
import com.facetlauncher.app.domain.RankBySearchRelevanceUseCase
import com.facetlauncher.app.domain.RecentlyInstalledAppsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

/** [RobolectricTestRunner] purely because [AppInfo]'s default `userHandle` calls `Process.myUserHandle()`, which needs a shadow. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PrivateSpaceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun appInfo(letter: Char, firstInstallTime: Long = 0L) = AppInfo(
        packageName = "com.example.$letter",
        activityName = ".Main",
        label = "$letter App",
        icon = null,
        firstInstallTime = firstInstallTime,
    )

    private fun viewModel(
        apps: List<AppInfo> = emptyList(),
        settings: LauncherSettings = LauncherSettings(),
    ): PrivateSpaceViewModel {
        val privateSpaceRepository = mock(PrivateSpaceRepository::class.java)
        `when`(privateSpaceRepository.observePrivateSpaceState()).thenReturn(flowOf(PrivateSpaceState.Unlocked))
        `when`(privateSpaceRepository.observePrivateSpaceApps()).thenReturn(flowOf(apps))
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(settings))
        return PrivateSpaceViewModel(privateSpaceRepository, RankBySearchRelevanceUseCase(), RecentlyInstalledAppsUseCase(), settingsRepository)
    }

    @Test
    fun `recentlyInstalledApps reflects the 72 hour filter over Private Space's own apps`() = runTest {
        val recent = appInfo('a', firstInstallTime = System.currentTimeMillis())
        val old = appInfo('b', firstInstallTime = 0L)
        val vm = viewModel(apps = listOf(recent, old))
        // WhileSubscribed StateFlows only start collecting their upstream once something actually
        // subscribes — reading .value alone never triggers that (see CalendarSettingsViewModelTest's
        // identical backgroundScope.launch { ... .collect {} } precedent).
        backgroundScope.launch { vm.recentlyInstalledApps.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(recent), vm.recentlyInstalledApps.value)
    }

    @Test
    fun `recentlyInstalledApps is empty when the setting is off`() = runTest {
        val recent = appInfo('a', firstInstallTime = System.currentTimeMillis())
        val vm = viewModel(apps = listOf(recent), settings = LauncherSettings(recentlyInstalledPosition = RecentlyInstalledPosition.DO_NOT_SHOW))
        backgroundScope.launch { vm.recentlyInstalledApps.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(emptyList<AppInfo>(), vm.recentlyInstalledApps.value)
    }

    @Test
    fun `recentlyInstalledApps is empty while searching`() = runTest {
        val recent = appInfo('a', firstInstallTime = System.currentTimeMillis())
        val vm = viewModel(apps = listOf(recent))
        backgroundScope.launch { vm.recentlyInstalledApps.collect {} }
        vm.onQueryChange("a")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(emptyList<AppInfo>(), vm.recentlyInstalledApps.value)
    }

    @Test
    fun `recentlyInstalledApps is independent of the filtered search results`() = runTest {
        val recent = appInfo('a', firstInstallTime = System.currentTimeMillis())
        val other = appInfo('b', firstInstallTime = 0L)
        val vm = viewModel(apps = listOf(recent, other))
        backgroundScope.launch { vm.filteredApps.collect {} }
        backgroundScope.launch { vm.recentlyInstalledApps.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        // filteredApps includes both (no query yet); recentlyInstalledApps is scoped separately
        assertEquals(listOf(recent, other), vm.filteredApps.value)
        assertEquals(listOf(recent), vm.recentlyInstalledApps.value)
    }
}
