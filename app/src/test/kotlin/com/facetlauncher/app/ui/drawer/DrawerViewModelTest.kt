package com.facetlauncher.app.ui.drawer

import android.app.PendingIntent
import android.content.Intent
import android.os.UserHandle
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.ContactPermissionRepository
import com.facetlauncher.app.data.ContactRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.NotificationActionController
import com.facetlauncher.app.data.NotificationAccessRepository
import com.facetlauncher.app.data.NotificationBadgeRepository
import com.facetlauncher.app.data.NotificationOpener
import com.facetlauncher.app.data.PrivateSpaceRepository
import com.facetlauncher.app.data.PrivateSpaceState
import com.facetlauncher.app.data.SecureFolderRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.SystemSettingsRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NotificationInfo
import com.facetlauncher.app.data.model.SettingsSearchEntry
import com.facetlauncher.app.domain.AppNotificationsState
import com.facetlauncher.app.domain.ObserveAppNotificationsUseCase
import com.facetlauncher.app.domain.RankBySearchRelevanceUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
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
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import com.facetlauncher.app.domain.AddToActiveListUseCase
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.domain.QuickPlacementAction
import com.facetlauncher.app.domain.RemoveFromActiveListUseCase
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.PlacedItem

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
        // Exposed so a test can hand in its own mock and verify a write against it afterward
        // (see `enableContactSearch turns the search-contacts setting on`).
        settingsRepository: SettingsRepository = mock(SettingsRepository::class.java).also {
            `when`(it.settings).thenReturn(
                flowOf(LauncherSettings(searchContactsEnabled = searchContactsEnabled, searchSettingsEnabled = searchSettingsEnabled)),
            )
        },
        secureFolderIntent: Intent? = null,
        privateSpaceState: PrivateSpaceState = PrivateSpaceState.NotConfigured,
        notificationActionController: NotificationActionController = NotificationActionController(),
        notificationOpener: NotificationOpener = mock(NotificationOpener::class.java),
        observeAppNotifications: ObserveAppNotificationsUseCase = mock(ObserveAppNotificationsUseCase::class.java),
        addToActiveList: AddToActiveListUseCase = mock(AddToActiveListUseCase::class.java),
        removeFromActiveList: RemoveFromActiveListUseCase = mock(RemoveFromActiveListUseCase::class.java),
    ): DrawerViewModel {
        val contactPermissionRepository = mock(ContactPermissionRepository::class.java)
        `when`(contactPermissionRepository.isGranted()).thenReturn(contactsPermissionGranted)

        val notificationBadgeRepository = mock(NotificationBadgeRepository::class.java)
        `when`(notificationBadgeRepository.badgeCounts).thenReturn(MutableStateFlow(emptyMap()))

        val folderRepository = mock(FolderRepository::class.java)
        `when`(folderRepository.observeFolders()).thenReturn(flowOf(emptyList()))

        val secureFolderRepository = mock(SecureFolderRepository::class.java)
        `when`(secureFolderRepository.launchIntent()).thenReturn(secureFolderIntent)

        val privateSpaceRepository = mock(PrivateSpaceRepository::class.java)
        `when`(privateSpaceRepository.observePrivateSpaceState()).thenReturn(flowOf(privateSpaceState))

        return DrawerViewModel(
            appRepository = mock(AppRepository::class.java),
            settingsRepository = settingsRepository,
            contactPermissionRepository = contactPermissionRepository,
            contactRepository = mock(ContactRepository::class.java),
            systemSettingsRepository = systemSettingsRepository,
            notificationBadgeRepository = notificationBadgeRepository,
            notificationAccessRepository = mock(NotificationAccessRepository::class.java),
            rankBySearchRelevance = RankBySearchRelevanceUseCase(),
            addToActiveList = addToActiveList,
            removeFromActiveList = removeFromActiveList,
            folderRepository = folderRepository,
            secureFolderRepository = secureFolderRepository,
            privateSpaceRepository = privateSpaceRepository,
            notificationActionController = notificationActionController,
            notificationOpener = notificationOpener,
            observeAppNotificationsUseCase = observeAppNotifications,
        )
    }

    private val chatApp = AppInfo(packageName = "com.example.chat", activityName = ".Main", label = "Chat", icon = null, userHandle = mock(UserHandle::class.java))
    private val message = NotificationInfo(packageName = "com.example.chat", silent = false, key = "k1", title = "Priya", text = "Hi", contentIntent = mock(PendingIntent::class.java))

    @Test
    fun `observeAppNotifications asks the use case for this app's package and profile`() = runTest {
        // Given a use case that reports one notification for the app
        val useCase = mock(ObserveAppNotificationsUseCase::class.java)
        `when`(useCase(chatApp.packageName, chatApp.userHandle)).thenReturn(flowOf(AppNotificationsState(listOf(message))))
        val viewModel = drawerViewModel(observeAppNotifications = useCase)

        // When observing the app's notifications
        val state = viewModel.observeAppNotifications(chatApp).first()

        // Then the use case's state comes through
        assertEquals(AppNotificationsState(listOf(message)), state)
    }

    @Test
    fun `dismissNotifications reports whether the listener took the request`() {
        // Given a listener that is connected
        val controller = NotificationActionController()
        val dismissed = mutableListOf<List<String>>()
        controller.attach { dismissed += it }
        val viewModel = drawerViewModel(notificationActionController = controller)

        // When dismissing, then the keys reach it and success is reported
        assertEquals(true, viewModel.dismissNotifications(listOf("k1", "k2")))
        assertEquals(listOf(listOf("k1", "k2")), dismissed)

        // And with the listener gone, nothing is dismissed and failure is reported
        controller.detach()
        assertEquals(false, viewModel.dismissNotifications(listOf("k1")))
    }

    @Test
    fun `openNotification sends the intent and clears a notification that asked to auto-cancel`() {
        // Given an auto-cancel notification, a listener that is connected, and an intent that sends
        val opener = mock(NotificationOpener::class.java)
        `when`(opener.open(message.contentIntent)).thenReturn(true)
        val controller = NotificationActionController()
        val dismissed = mutableListOf<List<String>>()
        controller.attach { dismissed += it }
        val viewModel = drawerViewModel(notificationOpener = opener, notificationActionController = controller)

        // When opening it
        viewModel.openNotification(message.copy(autoCancel = true))

        // Then it is cleared, as the notification shade would
        assertEquals(listOf(listOf("k1")), dismissed)
    }

    @Test
    fun `openNotification leaves a notification that did not ask to auto-cancel`() {
        // Given a notification that stays after being opened
        val opener = mock(NotificationOpener::class.java)
        `when`(opener.open(message.contentIntent)).thenReturn(true)
        val controller = NotificationActionController()
        val dismissed = mutableListOf<List<String>>()
        controller.attach { dismissed += it }
        val viewModel = drawerViewModel(notificationOpener = opener, notificationActionController = controller)

        // When opening it, then nothing is dismissed
        viewModel.openNotification(message.copy(autoCancel = false))
        assertEquals(emptyList<List<String>>(), dismissed)
    }

    @Test
    fun `openNotification does not clear a notification whose intent could not be sent`() {
        // Given an intent that fails to send, as when the app has since cancelled it
        val opener = mock(NotificationOpener::class.java)
        `when`(opener.open(message.contentIntent)).thenReturn(false)
        val controller = NotificationActionController()
        val dismissed = mutableListOf<List<String>>()
        controller.attach { dismissed += it }
        val viewModel = drawerViewModel(notificationOpener = opener, notificationActionController = controller)

        // When opening an auto-cancel notification, then it is left alone
        viewModel.openNotification(message.copy(autoCancel = true))
        assertEquals(emptyList<List<String>>(), dismissed)
    }

    @Test
    fun `openNotification never clears a persistent notification even if it asked to auto-cancel`() {
        // Given an intent that sends, and a notification that is both auto-cancel and persistent
        val opener = mock(NotificationOpener::class.java)
        `when`(opener.open(message.contentIntent)).thenReturn(true)
        val controller = NotificationActionController()
        val dismissed = mutableListOf<List<String>>()
        controller.attach { dismissed += it }
        val viewModel = drawerViewModel(notificationOpener = opener, notificationActionController = controller)

        // When opening it, then it is opened but left in place, since the listener can't dismiss it
        viewModel.openNotification(message.copy(autoCancel = true, isClearable = false))
        assertEquals(emptyList<List<String>>(), dismissed)
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

        viewModel.onQueryChange("Ann")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, viewModel.showContactsPermissionPrompt.value)
    }

    @Test
    fun `contacts permission prompt is hidden once a query is typed if access is already granted`() = runTest {
        val viewModel = drawerViewModel(searchContactsEnabled = true, contactsPermissionGranted = true)
        backgroundScope.launch { viewModel.showContactsPermissionPrompt.collect {} }

        viewModel.onQueryChange("Ann")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, viewModel.showContactsPermissionPrompt.value)
    }

    @Test
    fun `contacts permission prompt shows once a query is typed while enabled and ungranted`() = runTest {
        val viewModel = drawerViewModel(searchContactsEnabled = true, contactsPermissionGranted = false)
        backgroundScope.launch { viewModel.showContactsPermissionPrompt.collect {} }

        viewModel.onQueryChange("Ann")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, viewModel.showContactsPermissionPrompt.value)
    }

    @Test
    fun `contacts permission prompt stays hidden once dismissed even though access still isn't granted`() = runTest {
        val viewModel = drawerViewModel(searchContactsEnabled = true, contactsPermissionGranted = false)
        backgroundScope.launch { viewModel.showContactsPermissionPrompt.collect {} }
        viewModel.onQueryChange("Ann")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(true, viewModel.showContactsPermissionPrompt.value)

        // When the user taps the prompt's own button
        viewModel.dismissContactsPermissionPrompt()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then it stays hidden regardless of whether the permission actually ends up granted
        assertEquals(false, viewModel.showContactsPermissionPrompt.value)
    }

    @Test
    fun `contacts setting prompt is hidden with a blank query even when access is granted and the setting is off`() = runTest {
        val viewModel = drawerViewModel(searchContactsEnabled = false, contactsPermissionGranted = true)
        backgroundScope.launch { viewModel.showContactsSettingPrompt.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, viewModel.showContactsSettingPrompt.value)
    }

    @Test
    fun `contacts setting prompt is hidden once a query is typed if the setting is already on`() = runTest {
        val viewModel = drawerViewModel(searchContactsEnabled = true, contactsPermissionGranted = true)
        backgroundScope.launch { viewModel.showContactsSettingPrompt.collect {} }

        viewModel.onQueryChange("Ann")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, viewModel.showContactsSettingPrompt.value)
    }

    @Test
    fun `contacts setting prompt is hidden once a query is typed if access isn't granted either`() = runTest {
        val viewModel = drawerViewModel(searchContactsEnabled = false, contactsPermissionGranted = false)
        backgroundScope.launch { viewModel.showContactsSettingPrompt.collect {} }

        viewModel.onQueryChange("Ann")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, viewModel.showContactsSettingPrompt.value)
    }

    @Test
    fun `contacts setting prompt shows once a query is typed while access is granted and the setting is off`() = runTest {
        val viewModel = drawerViewModel(searchContactsEnabled = false, contactsPermissionGranted = true)
        backgroundScope.launch { viewModel.showContactsSettingPrompt.collect {} }

        viewModel.onQueryChange("Ann")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, viewModel.showContactsSettingPrompt.value)
    }

    @Test
    fun `contacts setting prompt stays hidden once enableContactSearch is called`() = runTest {
        val viewModel = drawerViewModel(searchContactsEnabled = false, contactsPermissionGranted = true)
        backgroundScope.launch { viewModel.showContactsSettingPrompt.collect {} }
        viewModel.onQueryChange("Ann")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(true, viewModel.showContactsSettingPrompt.value)

        // When the user taps the prompt's own button
        viewModel.enableContactSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then it stays hidden immediately, not just once the setting write is reflected back
        assertEquals(false, viewModel.showContactsSettingPrompt.value)
    }

    @Test
    fun `enableContactSearch turns the search-contacts setting on`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings(searchContactsEnabled = false)))
        val viewModel = drawerViewModel(contactsPermissionGranted = true, settingsRepository = settingsRepository)

        viewModel.enableContactSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(settingsRepository).setSearchContactsEnabled(true)
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

        viewModel.onQueryChange("wifi")
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

        viewModel.onQueryChange("wifi")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(entry), viewModel.settingsResults.value)
    }

    @Test
    fun `secure folder intent is null when Secure Folder isn't installed`() {
        val viewModel = drawerViewModel(secureFolderIntent = null)

        assertEquals(null, viewModel.secureFolderIntent)
    }

    @Test
    fun `secure folder intent surfaces the repository's launch intent when installed`() {
        val intent = mock(Intent::class.java)
        val viewModel = drawerViewModel(secureFolderIntent = intent)

        assertEquals(intent, viewModel.secureFolderIntent)
    }

    @Test
    fun `private space state defaults to NotConfigured before the repository's flow emits`() = runTest {
        val viewModel = drawerViewModel()

        assertEquals(PrivateSpaceState.NotConfigured, viewModel.privateSpaceState.value)
    }

    @Test
    fun `private space state surfaces the repository's own state once collected`() = runTest {
        val viewModel = drawerViewModel(privateSpaceState = PrivateSpaceState.Locked)
        backgroundScope.launch { viewModel.privateSpaceState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(PrivateSpaceState.Locked, viewModel.privateSpaceState.value)
    }

    @Test
    fun `the four long-press add actions go to the matching list through AddToActiveListUseCase`() = runTest {
        // Given a drawer with a mocked add use case, an app and a folder
        val addToActiveList = mock(AddToActiveListUseCase::class.java)
        val viewModel = drawerViewModel(addToActiveList = addToActiveList)
        val folder = Folder(id = 3L, name = "Games", apps = emptyList())

        // When the four Add actions fire
        viewModel.onFavoritesAction(chatApp, QuickPlacementAction.Add(null))
        viewModel.onDockAction(chatApp, QuickPlacementAction.Add(null))
        viewModel.onFolderFavoritesAction(folder, QuickPlacementAction.Add(null))
        viewModel.onFolderDockAction(folder, QuickPlacementAction.Add(null))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then each reaches the use case with its own list and item
        verify(addToActiveList).invoke(PlacementList.FAVORITES, PlacedItem.SingleApp(chatApp))
        verify(addToActiveList).invoke(PlacementList.DOCK, PlacedItem.SingleApp(chatApp))
        verify(addToActiveList).invoke(PlacementList.FAVORITES, PlacedItem.FolderItem(folder))
        verify(addToActiveList).invoke(PlacementList.DOCK, PlacedItem.FolderItem(folder))
    }

    @Test
    fun `the four long-press remove actions go to the matching list through RemoveFromActiveListUseCase`() = runTest {
        // Given a drawer with a mocked remove use case, an app and a folder
        val removeFromActiveList = mock(RemoveFromActiveListUseCase::class.java)
        val viewModel = drawerViewModel(removeFromActiveList = removeFromActiveList)
        val folder = Folder(id = 3L, name = "Games", apps = emptyList())

        // When the four Remove actions fire
        viewModel.onFavoritesAction(chatApp, QuickPlacementAction.Remove(null))
        viewModel.onDockAction(chatApp, QuickPlacementAction.Remove(null))
        viewModel.onFolderFavoritesAction(folder, QuickPlacementAction.Remove(null))
        viewModel.onFolderDockAction(folder, QuickPlacementAction.Remove(null))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then each reaches the use case with its own list and item
        verify(removeFromActiveList).invoke(PlacementList.FAVORITES, PlacedItem.SingleApp(chatApp))
        verify(removeFromActiveList).invoke(PlacementList.DOCK, PlacedItem.SingleApp(chatApp))
        verify(removeFromActiveList).invoke(PlacementList.FAVORITES, PlacedItem.FolderItem(folder))
        verify(removeFromActiveList).invoke(PlacementList.DOCK, PlacedItem.FolderItem(folder))
    }
}
