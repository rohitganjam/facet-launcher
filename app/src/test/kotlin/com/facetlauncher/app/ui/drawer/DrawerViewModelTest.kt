package com.facetlauncher.app.ui.drawer

import com.facetlauncher.app.data.AppShortcutRepository
import com.facetlauncher.app.data.ContactPermissionRepository
import com.facetlauncher.app.data.ContactRepository
import com.facetlauncher.app.data.NotificationAccessRepository
import com.facetlauncher.app.data.NotificationBadgeRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.SystemSettingsRepository
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.SettingsSearchEntry
import com.facetlauncher.app.domain.AddAppToDockUseCase
import com.facetlauncher.app.domain.AddAppToFavoritesUseCase
import com.facetlauncher.app.domain.ObserveQuickAddStateUseCase
import com.facetlauncher.app.domain.QuickAddState
import com.facetlauncher.app.domain.RankBySearchRelevanceUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Covers [DrawerViewModel.showContactsPermissionPrompt] — the search-drawer analogue of Home's
 * usage-access prompt (see chat history) — and [DrawerViewModel.settingsResults]'s own
 * enabled/query gating. [DrawerViewModel.contactResults] and the other fetch-on-open methods are
 * exercised indirectly through this ViewModel elsewhere/on-device.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DrawerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun drawerViewModel(
        searchContactsEnabled: Boolean = false,
        contactsPermissionGranted: Boolean = false,
        searchSettingsEnabled: Boolean = false,
        systemSettingsRepository: SystemSettingsRepository = mock(SystemSettingsRepository::class.java),
    ): DrawerViewModel {
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(
            flowOf(LauncherSettings(searchContactsEnabled = searchContactsEnabled, searchSettingsEnabled = searchSettingsEnabled)),
        )

        val contactPermissionRepository = mock(ContactPermissionRepository::class.java)
        `when`(contactPermissionRepository.isGranted()).thenReturn(contactsPermissionGranted)

        val notificationBadgeRepository = mock(NotificationBadgeRepository::class.java)
        `when`(notificationBadgeRepository.badgeCounts).thenReturn(MutableStateFlow(emptyMap()))

        val observeQuickAddState = mock(ObserveQuickAddStateUseCase::class.java)
        `when`(observeQuickAddState.invoke()).thenReturn(flowOf(QuickAddState()))

        return DrawerViewModel(
            settingsRepository = settingsRepository,
            contactPermissionRepository = contactPermissionRepository,
            contactRepository = mock(ContactRepository::class.java),
            systemSettingsRepository = systemSettingsRepository,
            appShortcutRepository = mock(AppShortcutRepository::class.java),
            notificationBadgeRepository = notificationBadgeRepository,
            notificationAccessRepository = mock(NotificationAccessRepository::class.java),
            rankBySearchRelevance = RankBySearchRelevanceUseCase(),
            observeQuickAddState = observeQuickAddState,
            addAppToFavorites = mock(AddAppToFavoritesUseCase::class.java),
            addAppToDock = mock(AddAppToDockUseCase::class.java),
        )
    }

    @Test
    fun `contacts permission prompt is hidden with a blank query even when access isn't granted`() = runTest {
        // Given the setting is on but access isn't granted
        val viewModel = drawerViewModel(searchContactsEnabled = true, contactsPermissionGranted = false)
        backgroundScope.launch { viewModel.showContactsPermissionPrompt.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then the prompt stays hidden while there's no real search query yet
        assertEquals(false, viewModel.showContactsPermissionPrompt.value)
    }

    @Test
    fun `contacts permission prompt is hidden once a query is typed if the setting is off`() = runTest {
        val viewModel = drawerViewModel(searchContactsEnabled = false, contactsPermissionGranted = false)
        backgroundScope.launch { viewModel.showContactsPermissionPrompt.collect {} }

        viewModel.onQueryChanged("Ann")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, viewModel.showContactsPermissionPrompt.value)
    }

    @Test
    fun `contacts permission prompt is hidden once a query is typed if access is already granted`() = runTest {
        val viewModel = drawerViewModel(searchContactsEnabled = true, contactsPermissionGranted = true)
        backgroundScope.launch { viewModel.showContactsPermissionPrompt.collect {} }

        viewModel.onQueryChanged("Ann")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, viewModel.showContactsPermissionPrompt.value)
    }

    @Test
    fun `contacts permission prompt shows once a query is typed while enabled and ungranted`() = runTest {
        val viewModel = drawerViewModel(searchContactsEnabled = true, contactsPermissionGranted = false)
        backgroundScope.launch { viewModel.showContactsPermissionPrompt.collect {} }

        viewModel.onQueryChanged("Ann")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, viewModel.showContactsPermissionPrompt.value)
    }

    @Test
    fun `contacts permission prompt stays hidden once dismissed even though access still isn't granted`() = runTest {
        val viewModel = drawerViewModel(searchContactsEnabled = true, contactsPermissionGranted = false)
        backgroundScope.launch { viewModel.showContactsPermissionPrompt.collect {} }
        viewModel.onQueryChanged("Ann")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(true, viewModel.showContactsPermissionPrompt.value)

        // When the user taps the prompt's own button
        viewModel.dismissContactsPermissionPrompt()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then it stays hidden regardless of whether the permission actually ends up granted
        assertEquals(false, viewModel.showContactsPermissionPrompt.value)
    }

    @Test
    fun `settings results stay empty with a blank query even when the setting is on`() = runTest {
        val viewModel = drawerViewModel(searchSettingsEnabled = true)
        backgroundScope.launch { viewModel.settingsResults.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(emptyList<Any>(), viewModel.settingsResults.value)
    }

    @Test
    fun `settings results stay empty once a query is typed if the setting is off`() = runTest {
        val systemSettingsRepository = mock(SystemSettingsRepository::class.java)
        `when`(systemSettingsRepository.search("wifi")).thenReturn(listOf(SettingsSearchEntry(id = "wifi", label = "Wi-Fi", action = "android.settings.WIFI_SETTINGS")))
        val viewModel = drawerViewModel(searchSettingsEnabled = false, systemSettingsRepository = systemSettingsRepository)
        backgroundScope.launch { viewModel.settingsResults.collect {} }

        viewModel.onQueryChanged("wifi")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(emptyList<Any>(), viewModel.settingsResults.value)
    }

    @Test
    fun `settings results surface matches once a query is typed while enabled`() = runTest {
        val entry = SettingsSearchEntry(id = "wifi", label = "Wi-Fi", action = "android.settings.WIFI_SETTINGS")
        val systemSettingsRepository = mock(SystemSettingsRepository::class.java)
        `when`(systemSettingsRepository.search("wifi")).thenReturn(listOf(entry))
        val viewModel = drawerViewModel(searchSettingsEnabled = true, systemSettingsRepository = systemSettingsRepository)
        backgroundScope.launch { viewModel.settingsResults.collect {} }

        viewModel.onQueryChanged("wifi")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(entry), viewModel.settingsResults.value)
    }
}
