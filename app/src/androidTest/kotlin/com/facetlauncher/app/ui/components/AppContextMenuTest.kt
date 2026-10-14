package com.facetlauncher.app.ui.components

import android.app.Activity
import android.app.Instrumentation
import android.net.Uri
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.height
import androidx.test.espresso.Espresso
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.NotificationInfo
import com.facetlauncher.app.domain.AppNotificationsState
import com.facetlauncher.app.domain.QuickAddState
import com.facetlauncher.app.domain.QuickPlacementAction
import com.facetlauncher.app.ui.launcher.LocalHomePressedEvent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flowOf
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AppContextMenuTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val app = AppInfo(packageName = "com.example.mail", activityName = ".Main", label = "Mail", icon = null)

    @Before
    fun setUp() = Intents.init()

    @After
    fun tearDown() = Intents.release()

    private fun setContent(
        app: AppInfo = this.app,
        onDismissRequest: () -> Unit = {},
        homePressedEvent: SharedFlow<Unit>? = null,
        onRequestQuickAddState: suspend (AppInfo) -> QuickAddState = { QuickAddState() },
        onFavoritesAction: (AppInfo, QuickPlacementAction) -> Unit = { _, _ -> },
        onDockAction: (AppInfo, QuickPlacementAction) -> Unit = { _, _ -> },
        folderCandidates: List<Folder>? = null,
        onCreateFolder: (AppInfo, String) -> Unit = { _, _ -> },
        onAddToFolder: (AppInfo, Long) -> Unit = { _, _ -> },
        removeFromFolderId: Long? = null,
        onRemoveFromFolder: (AppInfo, Long) -> Unit = { _, _ -> },
        appMenuNotifications: AppMenuNotifications = AppMenuNotifications.None,
        onAppInfo: (AppInfo) -> Unit = {},
    ) {
        composeRule.setContent {
            FacetLauncherTheme {
                CompositionLocalProvider(LocalHomePressedEvent provides homePressedEvent) {
                    AppContextMenu(
                        app = app,
                        expanded = true,
                        onDismissRequest = onDismissRequest,
                        onRequestQuickAddState = onRequestQuickAddState,
                        onFavoritesAction = onFavoritesAction,
                        onDockAction = onDockAction,
                        folderCandidates = folderCandidates,
                        onCreateFolder = onCreateFolder,
                        onAddToFolder = onAddToFolder,
                        removeFromFolderId = removeFromFolderId,
                        onRemoveFromFolder = onRemoveFromFolder,
                        onAppInfo = onAppInfo,
                        appMenuNotifications = appMenuNotifications,
                    )
                }
            }
        }
    }

    @Test
    fun rendersHeaderAndCoreActionsWhenExpanded() {
        setContent()

        composeRule.onNodeWithText("Mail").assertExists()
        composeRule.onNodeWithTag("app_context_menu_app_info").assertExists()
        composeRule.onNodeWithTag("app_context_menu_uninstall").assertExists()
    }

    @Test
    fun headerShowsNoProfileBadgeEvenForAWorkApp() {
        setContent(app = app.copy(profile = AppProfile.WORK))

        composeRule.onNodeWithTag("app_context_menu_work_badge").assertDoesNotExist()
        composeRule.onNodeWithTag("app_context_menu_other_badge").assertDoesNotExist()
        composeRule.onNodeWithText("Mail").assertExists()
    }

    @Test
    fun tappingAppInfoInvokesTheOnAppInfoCallbackForThisApp() {
        // AppContextMenu no longer launches Settings.ACTION_APPLICATION_DETAILS_SETTINGS inline —
        // it delegates to AppRepository.openAppDetails (via the onAppInfo callback), which targets
        // the tapped app's own real profile through LauncherApps.startAppDetailsActivity rather
        // than an ambiguous, profile-less Settings intent. See AppRepository.openAppDetails's own doc.
        var infoRequestedFor: AppInfo? = null
        setContent(onAppInfo = { infoRequestedFor = it })

        composeRule.onNodeWithTag("app_context_menu_app_info").performClick()

        assertEquals(app, infoRequestedFor)
    }

    @Test
    fun tappingUninstallLaunchesActionDeleteForThisPackage() {
        setContent()
        // Stub the real system Uninstall confirmation before it fires — app's package
        // ("com.example.mail") isn't actually installed on the test device, so unstubbed this
        // would launch the real PackageInstaller, which shows a real "app isn't installed" dialog
        // that nothing in the test ever dismisses (previously left the test stuck on that dialog
        // for ~30s before some external timeout finally let it proceed — see chat history).
        // Intents.intended below only checks the recorded intent history, which stubbing doesn't
        // affect, so the assertion is unchanged.
        Intents.intending(hasAction(android.content.Intent.ACTION_DELETE))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null))

        composeRule.onNodeWithTag("app_context_menu_uninstall").performClick()

        Intents.intended(
            allOf(
                hasAction(android.content.Intent.ACTION_DELETE),
                hasData(Uri.fromParts("package", app.packageName, null)),
            ),
        )
    }

    @Test
    fun addToFavoritesRowIsOmittedWhenFavoritesAlreadyAtTheCap() {
        setContent(onRequestQuickAddState = { QuickAddState(favoritesAction = null) })

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("app_context_menu_add_to_favorites").assertDoesNotExist()
    }

    @Test
    fun addToFavoritesRowReadsAddToFavoritesAndFiresOnFavoritesActionForTheLauncherWideDefault() {
        var addedApp: AppInfo? = null
        var addedAction: QuickPlacementAction? = null
        var dismissed = false
        setContent(
            onRequestQuickAddState = { QuickAddState(favoritesAction = QuickPlacementAction.Add(facetName = null)) },
            onFavoritesAction = { app, action -> addedApp = app; addedAction = action },
            onDismissRequest = { dismissed = true },
        )

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Add to Favorites").assertExists()
        composeRule.onNodeWithText("Global").assertExists()
        composeRule.onNodeWithTag("app_context_menu_add_to_favorites").performClick()

        assert(addedApp == app)
        assert(addedAction == QuickPlacementAction.Add(facetName = null))
        assert(dismissed)
    }

    @Test
    fun addToFavoritesRowShowsTheFacetNameBadgeWhenTheActiveFacetOverridesItsOwn() {
        setContent(onRequestQuickAddState = { QuickAddState(favoritesAction = QuickPlacementAction.Add(facetName = "Work")) })

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Add to Favorites").assertExists()
        composeRule.onNodeWithText("Work").assertExists()
    }

    @Test
    fun removeFromFavoritesRowReadsRemoveFromFavoritesAndFiresOnFavoritesActionForTheLauncherWideDefault() {
        var removedApp: AppInfo? = null
        var removedAction: QuickPlacementAction? = null
        var dismissed = false
        setContent(
            onRequestQuickAddState = { QuickAddState(favoritesAction = QuickPlacementAction.Remove(facetName = null)) },
            onFavoritesAction = { app, action -> removedApp = app; removedAction = action },
            onDismissRequest = { dismissed = true },
        )

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Remove from Favorites").assertExists()
        composeRule.onNodeWithText("Global").assertExists()
        composeRule.onNodeWithTag("app_context_menu_add_to_favorites").performClick()

        assert(removedApp == app)
        assert(removedAction == QuickPlacementAction.Remove(facetName = null))
        assert(dismissed)
    }

    @Test
    fun removeFromFavoritesRowShowsTheFacetNameBadgeWhenTheActiveFacetOverridesItsOwn() {
        setContent(onRequestQuickAddState = { QuickAddState(favoritesAction = QuickPlacementAction.Remove(facetName = "Work")) })

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Remove from Favorites").assertExists()
        composeRule.onNodeWithText("Work").assertExists()
    }

    @Test
    fun addToDockRowIsOmittedWhenDockAlreadyAtTheCap() {
        setContent(onRequestQuickAddState = { QuickAddState(dockAction = null) })

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("app_context_menu_add_to_dock").assertDoesNotExist()
    }

    @Test
    fun addToDockRowReadsAddToDockAndFiresOnDockActionForTheLauncherWideDefault() {
        var addedApp: AppInfo? = null
        var addedAction: QuickPlacementAction? = null
        var dismissed = false
        setContent(
            onRequestQuickAddState = { QuickAddState(dockAction = QuickPlacementAction.Add(facetName = null)) },
            onDockAction = { app, action -> addedApp = app; addedAction = action },
            onDismissRequest = { dismissed = true },
        )

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Add to Dock").assertExists()
        composeRule.onNodeWithText("Global").assertExists()
        composeRule.onNodeWithTag("app_context_menu_add_to_dock").performClick()

        assert(addedApp == app)
        assert(addedAction == QuickPlacementAction.Add(facetName = null))
        assert(dismissed)
    }

    @Test
    fun addToDockRowShowsTheFacetNameBadgeWhenTheActiveFacetOverridesItsOwn() {
        setContent(onRequestQuickAddState = { QuickAddState(dockAction = QuickPlacementAction.Add(facetName = "Work")) })

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Add to Dock").assertExists()
        composeRule.onNodeWithText("Work").assertExists()
    }

    @Test
    fun removeFromDockRowReadsRemoveFromDockAndFiresOnDockActionForTheLauncherWideDefault() {
        var removedApp: AppInfo? = null
        var removedAction: QuickPlacementAction? = null
        var dismissed = false
        setContent(
            onRequestQuickAddState = { QuickAddState(dockAction = QuickPlacementAction.Remove(facetName = null)) },
            onDockAction = { app, action -> removedApp = app; removedAction = action },
            onDismissRequest = { dismissed = true },
        )

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Remove from Dock").assertExists()
        composeRule.onNodeWithText("Global").assertExists()
        composeRule.onNodeWithTag("app_context_menu_add_to_dock").performClick()

        assert(removedApp == app)
        assert(removedAction == QuickPlacementAction.Remove(facetName = null))
        assert(dismissed)
    }

    @Test
    fun removeFromDockRowShowsTheFacetNameBadgeWhenTheActiveFacetOverridesItsOwn() {
        setContent(onRequestQuickAddState = { QuickAddState(dockAction = QuickPlacementAction.Remove(facetName = "Work")) })

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Remove from Dock").assertExists()
        composeRule.onNodeWithText("Work").assertExists()
    }

    @Test
    fun systemBackDismissesTheSheet() {
        var dismissed = false
        setContent(onDismissRequest = { dismissed = true })

        Espresso.pressBack()

        assert(dismissed)
    }

    @Test
    fun homePressedEventDismissesTheSheet() {
        var dismissed = false
        val homePressedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        setContent(onDismissRequest = { dismissed = true }, homePressedEvent = homePressedEvent)

        homePressedEvent.tryEmit(Unit)
        composeRule.waitUntil(timeoutMillis = 3_000) { dismissed }

        assert(dismissed)
    }

    @Test
    fun addToFolderRowIsHiddenWhenFolderCandidatesIsNull() {
        setContent(folderCandidates = null)

        composeRule.onNodeWithTag("app_context_menu_add_to_folder").assertDoesNotExist()
    }

    @Test
    fun tappingAddToFolderSlidesToTheFolderPageListingExistingFolders() {
        val folder = Folder(id = 1L, name = "Games", apps = listOf(app))
        setContent(folderCandidates = listOf(folder))

        composeRule.onNodeWithTag("app_context_menu_add_to_folder").performClick()

        composeRule.onNodeWithText("Add to folder").assertExists()
        composeRule.onNodeWithTag("app_context_menu_create_folder").assertExists()
        composeRule.onNodeWithTag("app_context_menu_folder_1").assertExists()
        composeRule.onNodeWithText("Games").assertExists()
        // Each candidate row shows its own app-count subheading, not just the name — singular
        // "1 app", not "1 apps" (pluralStringResource, not raw string concatenation — see chat
        // history: this folder-app-count text used to be built as "${size} apps" unconditionally).
        composeRule.onNodeWithText("1 app").assertExists()
    }

    @Test
    fun tappingAnExistingFolderPreviewsItRatherThanAddingImmediately() {
        var addedApp: AppInfo? = null
        var addedFolderId: Long? = null
        val folder = Folder(id = 7L, name = "Games", apps = emptyList())
        setContent(
            folderCandidates = listOf(folder),
            onAddToFolder = { app, folderId -> addedApp = app; addedFolderId = folderId },
        )

        composeRule.onNodeWithTag("app_context_menu_add_to_folder").performClick()
        composeRule.onNodeWithTag("app_context_menu_folder_7").performClick()

        // The folder's own contents preview opens — showing what's already there — not an
        // immediate add. The pending app isn't added yet. It renders as a Dialog on top of the
        // still-open ModalBottomSheet (by design — see AppContextMenu's own doc comment), so the
        // folder-candidate row's "Games" text is still in the tree too; scope the lookup to the
        // preview itself rather than matching both. The preview's header Text gets merged into an
        // ancestor's clickable semantics node, so this needs the unmerged tree to see it as a
        // descendant of the tagged Column.
        composeRule.onNode(
            hasTestTag("folder_contents_sheet") and hasAnyDescendant(hasText("Games")),
            useUnmergedTree = true,
        ).assertExists()
        assert(addedApp == null)
        assert(addedFolderId == null)
    }

    @Test
    fun tappingAddInTheFolderPreviewCallsOnAddToFolderAndDismissesBothSheets() {
        var addedApp: AppInfo? = null
        var addedFolderId: Long? = null
        var dismissed = false
        val folder = Folder(id = 7L, name = "Games", apps = emptyList())
        setContent(
            folderCandidates = listOf(folder),
            onAddToFolder = { app, folderId -> addedApp = app; addedFolderId = folderId },
            onDismissRequest = { dismissed = true },
        )

        composeRule.onNodeWithTag("app_context_menu_add_to_folder").performClick()
        composeRule.onNodeWithTag("app_context_menu_folder_7").performClick()
        composeRule.onNodeWithTag("folder_contents_sheet_add").performClick()

        assert(addedApp == app)
        assert(addedFolderId == 7L)
        assert(dismissed)
    }

    @Test
    fun folderPreviewBackChevronReturnsToTheFolderListWithoutDismissing() {
        var dismissed = false
        val folder = Folder(id = 7L, name = "Games", apps = emptyList())
        setContent(folderCandidates = listOf(folder), onDismissRequest = { dismissed = true })

        composeRule.onNodeWithTag("app_context_menu_add_to_folder").performClick()
        composeRule.onNodeWithTag("app_context_menu_folder_7").performClick()
        composeRule.onNodeWithTag("folder_contents_sheet_back").performClick()

        composeRule.onNodeWithTag("folder_contents_sheet").assertDoesNotExist()
        composeRule.onNodeWithTag("app_context_menu_folder_7").assertExists()
        assert(!dismissed)
    }

    @Test
    fun tappingCreateNewFolderOpensRenameDialogAndSavingCallsOnCreateFolder() {
        var createdApp: AppInfo? = null
        var createdName: String? = null
        var dismissed = false
        setContent(
            folderCandidates = emptyList(),
            onCreateFolder = { app, name -> createdApp = app; createdName = name },
            onDismissRequest = { dismissed = true },
        )

        composeRule.onNodeWithTag("app_context_menu_add_to_folder").performClick()
        composeRule.onNodeWithTag("app_context_menu_create_folder").performClick()
        composeRule.onNodeWithTag("rename_dialog_field").performTextInput("Games")
        composeRule.onNodeWithTag("rename_dialog_save").performClick()

        assert(createdApp == app)
        assert(createdName == "Games")
        assert(dismissed)
    }

    @Test
    fun folderPageBackChevronReturnsToTheMainPageWithoutDismissing() {
        var dismissed = false
        setContent(folderCandidates = emptyList(), onDismissRequest = { dismissed = true })

        composeRule.onNodeWithTag("app_context_menu_add_to_folder").performClick()
        composeRule.onNodeWithTag("app_context_menu_folder_page_back").performClick()

        composeRule.onNodeWithTag("app_context_menu_app_info").assertExists()
        assert(!dismissed)
    }

    @Test
    fun removeFromFolderRowIsShownInsteadOfAddToFolderWhenRemoveFromFolderIdIsSet() {
        var removedApp: AppInfo? = null
        var removedFolderId: Long? = null
        var dismissed = false
        setContent(
            removeFromFolderId = 3L,
            onRemoveFromFolder = { app, folderId -> removedApp = app; removedFolderId = folderId },
            onDismissRequest = { dismissed = true },
        )

        composeRule.onNodeWithTag("app_context_menu_add_to_folder").assertDoesNotExist()
        composeRule.onNodeWithTag("app_context_menu_remove_from_folder").performClick()

        assert(removedApp == app)
        assert(removedFolderId == 3L)
        assert(dismissed)
    }

    @Test
    fun appInfoAndUninstallAreIconButtonsWithScreenReaderLabels() {
        setContent()

        composeRule.onNodeWithContentDescription("App info").assertExists()
        composeRule.onNodeWithContentDescription("Uninstall").assertExists()
    }

    @Test
    fun tappingAppInfoAlsoDismissesTheSheet() {
        var dismissed = false
        setContent(onDismissRequest = { dismissed = true })

        composeRule.onNodeWithTag("app_context_menu_app_info").performClick()

        assert(dismissed)
    }

    @Test
    fun aPlacementTileDescribesItsActionAndListToScreenReaders() {
        setContent(
            onRequestQuickAddState = {
                QuickAddState(
                    favoritesAction = QuickPlacementAction.Add(facetName = null),
                    dockAction = QuickPlacementAction.Remove(facetName = "Work"),
                )
            },
        )

        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Add to Favorites, Global").assertExists()
        composeRule.onNodeWithContentDescription("Remove from Dock, Work").assertExists()
    }

    @Test
    fun theFolderTileNamesNoListSoItsDescriptionIsJustItsLabel() {
        setContent(folderCandidates = emptyList())

        composeRule.onNodeWithContentDescription("Add to folder").assertExists()
    }

    @Test
    fun noNotificationZoneIsShownWhenTheSettingIsOff() {
        setContent(appMenuNotifications = AppMenuNotifications(observe = { flowOf(AppNotificationsState(enabled = false)) }))

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Notifications").assertDoesNotExist()
        composeRule.onNodeWithTag("app_notifications_clear_all").assertDoesNotExist()
        composeRule.onNodeWithTag("app_notifications_list").assertDoesNotExist()
        composeRule.onNodeWithTag("app_notifications_empty").assertDoesNotExist()
        composeRule.onNodeWithTag("app_notifications_enable").assertDoesNotExist()
    }

    @Test
    fun theZoneStaysWithAnEmptyStateAndAGreyedClearAllWhenThereAreNoNotifications() {
        setContent(appMenuNotifications = AppMenuNotifications(observe = { flowOf(AppNotificationsState()) }))

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("app_notifications_empty").assertExists() }.isSuccess
        }
        composeRule.onNodeWithText("Notifications").assertExists()
        composeRule.onNodeWithText("No notifications").assertExists()
        composeRule.onNodeWithTag("app_notifications_clear_all").assertExists().assertIsNotEnabled()
        composeRule.onNodeWithTag("app_notifications_list").assertDoesNotExist()
        composeRule.onNodeWithTag("app_notifications_enable").assertDoesNotExist()
    }

    @Test
    fun clearAllIsEnabledWhenThereAreNotificationsToClear() {
        setContent(appMenuNotifications = AppMenuNotifications(observe = { flowOf(AppNotificationsState(twoNotifications)) }))

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("app_notifications_list").assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag("app_notifications_clear_all").assertIsEnabled()
        composeRule.onNodeWithTag("app_notifications_empty").assertDoesNotExist()
    }

    @Test
    fun theEmptyStateTurnsIntoTheListWhenANotificationArrives() {
        val state = MutableStateFlow(AppNotificationsState())
        setContent(appMenuNotifications = AppMenuNotifications(observe = { state }))
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("app_notifications_empty").assertExists() }.isSuccess
        }

        state.value = AppNotificationsState(twoNotifications)

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("app_notification_k1").assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag("app_notifications_empty").assertDoesNotExist()
        composeRule.onNodeWithTag("app_notifications_clear_all").assertIsEnabled()
    }

    @Test
    fun theAppsOwnNotificationsAreObservedAndListedWithACount() {
        var observedApp: AppInfo? = null
        setContent(
            appMenuNotifications = AppMenuNotifications(
                observe = { observedApp = it; flowOf(AppNotificationsState(twoNotifications)) },
            ),
        )

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("app_notifications_list").assertExists() }.isSuccess
        }
        assertEquals(app, observedApp)
        composeRule.onNodeWithText("Priya").assertExists()
        composeRule.onNodeWithText("Are we still on?").assertExists()
        composeRule.onNodeWithText("Notifications · 2").assertExists()
    }

    @Test
    fun tappingANotificationOpensItAndClosesTheSheet() {
        var opened: NotificationInfo? = null
        var dismissed = false
        setContent(
            onDismissRequest = { dismissed = true },
            appMenuNotifications = AppMenuNotifications(
                observe = { flowOf(AppNotificationsState(twoNotifications)) },
                onOpen = { opened = it },
            ),
        )

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("app_notification_k1").assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag("app_notification_k1").performClick()

        assertEquals(twoNotifications[0], opened)
        assert(dismissed)
    }

    @Test
    fun clearAllDismissesEveryListedNotificationAndKeepsTheSheetOpen() {
        var dismissedKeys: List<String>? = null
        var sheetDismissed = false
        setContent(
            onDismissRequest = { sheetDismissed = true },
            appMenuNotifications = AppMenuNotifications(
                observe = { flowOf(AppNotificationsState(twoNotifications)) },
                onDismiss = { dismissedKeys = it; true },
            ),
        )

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("app_notifications_clear_all").assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag("app_notifications_clear_all").performClick()

        assertEquals(listOf("k1", "k2"), dismissedKeys)
        assert(!sheetDismissed)
    }

    @Test
    fun swipingANotificationAwayDismissesOnlyThatOne() {
        var dismissedKeys: List<String>? = null
        setContent(
            appMenuNotifications = AppMenuNotifications(
                observe = { flowOf(AppNotificationsState(twoNotifications)) },
                onDismiss = { dismissedKeys = it; true },
            ),
        )

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("app_notification_k2").assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag("app_notification_k2").performTouchInput { swipeLeft() }

        composeRule.waitUntil(timeoutMillis = 3_000) { dismissedKeys != null }
        assertEquals(listOf("k2"), dismissedKeys)
    }

    @Test
    fun theListFollowsNotificationsThatArriveWhileTheSheetIsOpen() {
        val state = MutableStateFlow(AppNotificationsState(twoNotifications.take(1)))
        setContent(appMenuNotifications = AppMenuNotifications(observe = { state }))

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("app_notification_k1").assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag("app_notification_k2").assertDoesNotExist()

        // When a second notification arrives
        state.value = AppNotificationsState(twoNotifications)

        // Then it appears without reopening the sheet
        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("app_notification_k2").assertExists() }.isSuccess
        }
    }

    @Test
    fun missingAccessShowsAnEnableRowThatClosesTheSheetAndAsksForAccess() {
        var asked = false
        var dismissed = false
        setContent(
            onDismissRequest = { dismissed = true },
            appMenuNotifications = AppMenuNotifications(
                observe = { flowOf(AppNotificationsState(needsAccess = true)) },
                onEnableAccess = { asked = true },
            ),
        )

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("app_notifications_enable").assertExists() }.isSuccess
        }
        composeRule.onNodeWithText("Notifications need access from system settings.").assertExists()
        composeRule.onNodeWithText("Open settings").assertExists()
        composeRule.onNodeWithText("Notifications").assertExists()
        composeRule.onNodeWithTag("app_notifications_clear_all").assertIsNotEnabled()
        composeRule.onNodeWithTag("app_notifications_list").assertDoesNotExist()
        composeRule.onNodeWithTag("app_notifications_empty").assertDoesNotExist()
        composeRule.onNodeWithTag("app_notifications_enable").performClick()

        assert(asked)
        assert(dismissed)
    }

    @Test
    fun manyNotificationsScrollInsideAWindowOfAboutTwoCards() {
        val many = (1..6).map { notification("k$it", "Sender $it", "Message $it") }
        setContent(appMenuNotifications = AppMenuNotifications(observe = { flowOf(AppNotificationsState(many)) }))

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("app_notification_k1").assertExists() }.isSuccess
        }
        composeRule.waitForIdle()

        // Then the window is roughly two cards and a peek tall, not six cards tall
        val cardHeight = composeRule.onNodeWithTag("app_notification_k1").getBoundsInRoot().height
        val windowHeight = composeRule.onNodeWithTag("app_notifications_list").getBoundsInRoot().height
        assert(windowHeight >= cardHeight * 2) { "window $windowHeight should hold two cards of $cardHeight" }
        assert(windowHeight <= cardHeight * 3) { "window $windowHeight should not show three whole cards of $cardHeight" }

        // And the last one is reachable by scrolling it
        composeRule.onNodeWithTag("app_notifications_list").performScrollToNode(hasTestTag("app_notification_k6"))
        composeRule.onNodeWithTag("app_notification_k6").assertIsDisplayed()
    }

    @Test
    fun aNotificationShowsItsAgeAsACompactLabel() {
        val twoMinutesOld = notification("k9", "Priya", "Hi").copy(postTime = System.currentTimeMillis() - 125_000L)
        setContent(appMenuNotifications = AppMenuNotifications(observe = { flowOf(AppNotificationsState(listOf(twoMinutesOld))) }))

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("app_notification_k9").assertExists() }.isSuccess
        }

        composeRule.onNodeWithText("2m").assertExists()
    }

    private val twoNotifications = listOf(
        notification("k1", "Priya", "Are we still on?"),
        notification("k2", "Team chat", "Build is green"),
    )

    private fun notification(key: String, title: String, text: String) =
        NotificationInfo(packageName = app.packageName, silent = false, key = key, title = title, text = text, postTime = 1L)
}
