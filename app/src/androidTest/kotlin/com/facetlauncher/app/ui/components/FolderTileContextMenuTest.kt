package com.facetlauncher.app.ui.components

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.domain.QuickAddState
import com.facetlauncher.app.domain.QuickPlacementAction
import com.facetlauncher.app.ui.launcher.LocalHomePressedEvent
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.junit.Rule
import org.junit.Test

class FolderTileContextMenuTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val folder = Folder(id = 7L, name = "Games", apps = emptyList())

    private fun setContent(
        onDismissRequest: () -> Unit = {},
        onRename: (Long, String) -> Unit = { _, _ -> },
        homePressedEvent: SharedFlow<Unit>? = null,
        onRequestQuickAddState: suspend (Folder) -> QuickAddState = { QuickAddState() },
        onFavoritesAction: (Folder, QuickPlacementAction) -> Unit = { _, _ -> },
        onDockAction: (Folder, QuickPlacementAction) -> Unit = { _, _ -> },
    ) {
        composeRule.setContent {
            FacetLauncherTheme {
                CompositionLocalProvider(LocalHomePressedEvent provides homePressedEvent) {
                    FolderTileContextMenu(
                        folder = folder,
                        expanded = true,
                        onDismissRequest = onDismissRequest,
                        onRename = onRename,
                        onRequestQuickAddState = onRequestQuickAddState,
                        onFavoritesAction = onFavoritesAction,
                        onDockAction = onDockAction,
                    )
                }
            }
        }
    }

    @Test
    fun rendersFolderNameAndRenameRowWhenExpanded() {
        setContent()

        composeRule.onNodeWithText("Games").assertExists()
        composeRule.onNodeWithTag("folder_tile_context_menu_rename").assertExists()
    }

    @Test
    fun tappingRenameOpensRenameDialogAndSavingCallsOnRename() {
        var renamedId: Long? = null
        var renamedName: String? = null
        setContent(onRename = { id, name -> renamedId = id; renamedName = name })

        composeRule.onNodeWithTag("folder_tile_context_menu_rename").performClick()
        composeRule.onNodeWithTag("rename_dialog_field").performTextReplacement("Arcade")
        composeRule.onNodeWithTag("rename_dialog_save").performClick()

        assert(renamedId == 7L)
        assert(renamedName == "Arcade")
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
    fun addToFavoritesRowIsOmittedWhenFavoritesAlreadyAtTheCapAndFolderIsNotAMember() {
        setContent(onRequestQuickAddState = { QuickAddState(favoritesAction = null) })

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("folder_tile_context_menu_add_to_favorites").assertDoesNotExist()
    }

    @Test
    fun addToFavoritesRowReadsAddToFavoritesAndFiresOnFavoritesActionForTheLauncherWideDefault() {
        var actedFolder: Folder? = null
        var actedAction: QuickPlacementAction? = null
        var dismissed = false
        setContent(
            onRequestQuickAddState = { QuickAddState(favoritesAction = QuickPlacementAction.Add(facetName = null)) },
            onFavoritesAction = { folder, action -> actedFolder = folder; actedAction = action },
            onDismissRequest = { dismissed = true },
        )

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Add to Favorites").assertExists()
        composeRule.onNodeWithText("Global").assertExists()
        composeRule.onNodeWithTag("folder_tile_context_menu_add_to_favorites").performClick()

        assert(actedFolder == folder)
        assert(actedAction == QuickPlacementAction.Add(facetName = null))
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
    fun removeFromFavoritesRowReadsRemoveFromFavoritesWhenTheFolderIsAlreadyAMember() {
        var actedFolder: Folder? = null
        var actedAction: QuickPlacementAction? = null
        setContent(
            onRequestQuickAddState = { QuickAddState(favoritesAction = QuickPlacementAction.Remove(facetName = null)) },
            onFavoritesAction = { folder, action -> actedFolder = folder; actedAction = action },
        )

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Remove from Favorites").assertExists()
        composeRule.onNodeWithTag("folder_tile_context_menu_add_to_favorites").performClick()

        assert(actedFolder == folder)
        assert(actedAction == QuickPlacementAction.Remove(facetName = null))
    }

    @Test
    fun addToDockRowIsOmittedWhenDockAlreadyAtTheCapAndFolderIsNotAMember() {
        setContent(onRequestQuickAddState = { QuickAddState(dockAction = null) })

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("folder_tile_context_menu_add_to_dock").assertDoesNotExist()
    }

    @Test
    fun addToDockRowReadsAddToDockAndFiresOnDockActionForTheLauncherWideDefault() {
        var actedFolder: Folder? = null
        var actedAction: QuickPlacementAction? = null
        var dismissed = false
        setContent(
            onRequestQuickAddState = { QuickAddState(dockAction = QuickPlacementAction.Add(facetName = null)) },
            onDockAction = { folder, action -> actedFolder = folder; actedAction = action },
            onDismissRequest = { dismissed = true },
        )

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Add to Dock").assertExists()
        composeRule.onNodeWithText("Global").assertExists()
        composeRule.onNodeWithTag("folder_tile_context_menu_add_to_dock").performClick()

        assert(actedFolder == folder)
        assert(actedAction == QuickPlacementAction.Add(facetName = null))
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
    fun removeFromDockRowReadsRemoveFromDockWhenTheFolderIsAlreadyAMember() {
        var actedFolder: Folder? = null
        var actedAction: QuickPlacementAction? = null
        setContent(
            onRequestQuickAddState = { QuickAddState(dockAction = QuickPlacementAction.Remove(facetName = null)) },
            onDockAction = { folder, action -> actedFolder = folder; actedAction = action },
        )

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Remove from Dock").assertExists()
        composeRule.onNodeWithTag("folder_tile_context_menu_add_to_dock").performClick()

        assert(actedFolder == folder)
        assert(actedAction == QuickPlacementAction.Remove(facetName = null))
    }
}
