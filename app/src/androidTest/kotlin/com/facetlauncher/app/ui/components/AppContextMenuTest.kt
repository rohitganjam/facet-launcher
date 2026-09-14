package com.facetlauncher.app.ui.components

import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppShortcut
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.domain.QuickAddState
import com.facetlauncher.app.domain.QuickPlacementAction
import com.facetlauncher.app.ui.launcher.LocalHomePressedEvent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
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
        onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut> = { emptyList() },
        onLaunchShortcut: (AppShortcut) -> Unit = {},
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
    ) {
        composeRule.setContent {
            FacetLauncherTheme {
                CompositionLocalProvider(LocalHomePressedEvent provides homePressedEvent) {
                    AppContextMenu(
                        app = app,
                        expanded = true,
                        onDismissRequest = onDismissRequest,
                        onRequestShortcuts = onRequestShortcuts,
                        onLaunchShortcut = onLaunchShortcut,
                        onRequestQuickAddState = onRequestQuickAddState,
                        onFavoritesAction = onFavoritesAction,
                        onDockAction = onDockAction,
                        folderCandidates = folderCandidates,
                        onCreateFolder = onCreateFolder,
                        onAddToFolder = onAddToFolder,
                        removeFromFolderId = removeFromFolderId,
                        onRemoveFromFolder = onRemoveFromFolder,
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
    fun tappingAppInfoLaunchesApplicationDetailsSettingsForThisPackage() {
        setContent()

        composeRule.onNodeWithTag("app_context_menu_app_info").performClick()

        Intents.intended(
            allOf(
                hasAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS),
                hasData(Uri.fromParts("package", app.packageName, null)),
            ),
        )
    }

    @Test
    fun tappingUninstallLaunchesActionDeleteForThisPackage() {
        setContent()

        composeRule.onNodeWithTag("app_context_menu_uninstall").performClick()

        Intents.intended(
            allOf(
                hasAction(android.content.Intent.ACTION_DELETE),
                hasData(Uri.fromParts("package", app.packageName, null)),
            ),
        )
    }

    @Test
    fun quickActionsSectionIsOmittedWhenTheAppPublishesNoShortcuts() {
        setContent(onRequestShortcuts = { emptyList() })

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("app_context_menu_shortcut_compose").assertDoesNotExist()
    }

    @Test
    fun quickActionRowRendersAndFiresOnLaunchShortcutWhenTapped() {
        var launched: AppShortcut? = null
        val shortcut = AppShortcut(id = "compose", packageName = app.packageName, label = "Compose")
        var dismissed = false
        setContent(
            onRequestShortcuts = { listOf(shortcut) },
            onLaunchShortcut = { launched = it },
            onDismissRequest = { dismissed = true },
        )

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("app_context_menu_shortcut_compose").assertExists() }.isSuccess
        }
        composeRule.onNodeWithText("Compose").assertExists()
        composeRule.onNodeWithTag("app_context_menu_shortcut_compose").performClick()

        assert(launched == shortcut)
        assert(dismissed)
    }

    @Test
    fun shortcutsBeyondTheCapAreNotShown() {
        val shortcuts = (1..5).map { AppShortcut(id = "shortcut_$it", packageName = app.packageName, label = "Shortcut $it") }
        setContent(onRequestShortcuts = { shortcuts })

        composeRule.waitUntil(timeoutMillis = 3_000) {
            runCatching { composeRule.onNodeWithTag("app_context_menu_shortcut_shortcut_3").assertExists() }.isSuccess
        }
        composeRule.onNodeWithTag("app_context_menu_shortcut_shortcut_4").assertDoesNotExist()
        composeRule.onNodeWithTag("app_context_menu_shortcut_shortcut_5").assertDoesNotExist()
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
        // Each candidate row shows its own app-count subheading, not just the name.
        composeRule.onNodeWithText("1 apps").assertExists()
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
}
